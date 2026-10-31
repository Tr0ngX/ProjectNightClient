package night.module.impl.core;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerData.Type;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import night.Night;
import night.events.EventTarget;
import night.events.impl.ClientDisconnectEvent;
import night.events.impl.PacketReceiveEvent;
import night.events.impl.SettingChangeEvent;
import night.events.impl.TickEvent;
import night.gui.ClickGuiScreen;
import night.module.Module;
import night.module.ModuleInfo;
import night.module.impl.player.SpeedMine;
import night.pingbypass.ClientInputForwarder;
import night.pingbypass.PingBypassState;
import night.pingbypass.protocol.PingBypassPacket;
import night.pingbypass.protocol.PingBypassPayload;
import night.pingbypass.protocol.c2s.C2SConnectServerPacket;
import night.pingbypass.protocol.c2s.C2SFriendSyncPacket;
import night.pingbypass.protocol.c2s.C2SMineBlockPacket;
import night.pingbypass.protocol.c2s.C2SModuleTogglePacket;
import night.pingbypass.protocol.c2s.C2SPasswordResponsePacket;
import night.pingbypass.protocol.c2s.C2SSettingUpdatePacket;
import night.pingbypass.protocol.c2s.C2SWindowClickPacket;
import night.pingbypass.protocol.s2c.S2CHeldSlotSyncPacket;
import night.pingbypass.protocol.s2c.S2CMineProgressPacket;
import night.pingbypass.protocol.s2c.S2CModuleStatePacket;
import night.pingbypass.protocol.s2c.S2CProxyDisconnectPacket;
import night.pingbypass.protocol.s2c.S2CRenderPlacePosPacket;
import night.pingbypass.protocol.s2c.S2CSettingSyncPacket;
import night.pingbypass.protocol.s2c.S2CStatusMessagePacket;
import night.setting.Setting;
import night.setting.impl.BooleanSetting;
import night.setting.impl.ColorSetting;
import night.setting.impl.ModeSetting;
import night.setting.impl.NumberSetting;
import night.setting.impl.StringSetting;
import night.util.render.PlaceRenderEntry;

@ModuleInfo(name = "PingBypass", description = "Connects to a PingBypass proxy server for low-latency combat.", category = Module.Category.CORE)
public class PingBypassModule extends Module {
   public final StringSetting iP = new StringSetting("IP", "The IP address of the PingBypass proxy server.", "127.0.0.1");
   public final NumberSetting port = new NumberSetting("Port", "The port of the PingBypass proxy server.", 25565, 1, 65535);
   public final StringSetting password = new StringSetting("Password", "The password for the PingBypass proxy server.", "");
   public final StringSetting server = new StringSetting(
      "Server", "The target Minecraft server to join through the proxy (e.g. mc.hypixel.net or mc.hypixel.net:25565).", ""
   );
   private volatile boolean enabled;
   public volatile int i;
   private volatile String k;
   private final Map<String, Boolean> l = new ConcurrentHashMap<>();
   private final Map<String, String> m = new ConcurrentHashMap<>();
   private volatile Connection n;
   private final List<PingBypassModule.PendingPacket> o = Collections.synchronizedList(new ArrayList<>());
   private int p = 10;
   private ClientInputForwarder q;

   @Override
   public void onEnable() {
      if (Night.pingBypassConfig != null && Night.pingBypassConfig.isServerMode()) {
         Night.chatManager.sendError("Cannot enable PingBypass on a PingBypass server!");
         this.setEnabled(false, false);
      } else if (mc.gui.overlay() != null) {
         Night.LOGGER.info("[PingBypass] Skipping auto-connect during startup");
      } else if (Night.configManager != null && Night.configManager.isLoading()) {
         Night.LOGGER.info("[PingBypass] Skipping auto-connect during config load");
      } else {
         if (mc.gui.screen() instanceof ClickGuiScreen) {
            mc.gui.setScreen(null);
         }

         this.l.clear();
         this.m.clear();
         this.k = null;
         this.enabled = false;
         if (mc.level != null) {
            mc.level.disconnect(Component.literal("PingBypass enabled."));
         }

         if (mc.getConnection() != null) {
            mc.getConnection().getConnection().disconnect(Component.literal("PingBypass enabled."));
         }

         String var1 = this.iP.getValue();
         int var2 = this.port.getValue().intValue();
         ServerAddress var3 = new ServerAddress(var1, var2);
         ServerData var4 = new ServerData("PingBypass Proxy", var3.getHost() + ":" + var3.getPort(), Type.OTHER);
         ConnectScreen.startConnecting(new TitleScreen(), mc, var3, var4, false, null);
      }
   }

   @Override
   public void onDisable() {
      if (mc.level != null) {
         mc.level.disconnect(Component.literal("PingBypass disabled."));
      }

      if (mc.getConnection() != null) {
         mc.getConnection().getConnection().disconnect(Component.literal("PingBypass disabled."));
      }

      this.i = 0;
      this.k = null;
      this.n = null;
      PingBypassState.d = false;
      if (this.q != null) {
         this.q.b();
         this.q = null;
      }

      this.l.clear();
      this.m.clear();
   }

   @Override
   public String getDisplayInfo() {
      return this.i + "ms";
   }

   @EventTarget
   public void a_void_ClientDisconnectEvent(ClientDisconnectEvent var1) {
      if (this.isEnabled() && PingBypassState.d) {
         mc.execute(() -> this.setEnabled(false, false));
      }
   }

   @EventTarget
   public void a_void_SettingChangeEvent(SettingChangeEvent var1) {
      if (this.isEnabled() && PingBypassState.d) {
         if (Night.pingBypassConfig == null || !Night.pingBypassConfig.isServerMode()) {
            Setting var2 = var1.getSetting();

            for (Module var4 : (Iterable<Module>)(Object)Night.moduleManager.getModules()) {
               if (var4.isProxySupported() && var4.getSettings().contains(var2)) {
                  String var5 = this.a_String_Setting(var2);
                  if (var5 == null) {
                     return;
                  }

                  Connection var6 = this.n;
                  if (var6 == null && mc.getConnection() != null) {
                     var6 = mc.getConnection().getConnection();
                  }

                  if (var6 != null && var6.isConnected()) {
                     var6.send(
                        new ServerboundCustomPayloadPacket(PingBypassPayload.fromPacket(new C2SSettingUpdatePacket(var4.getName(), var2.getName(), var5)))
                     );
                  }

                  return;
               }
            }
         }
      }
   }

   private String a_String_Setting(Setting var1) {
      if (var1 instanceof BooleanSetting var7) {
         return String.valueOf(var7.getValue());
      } else if (var1 instanceof NumberSetting var6) {
         return var6.getValue().toString();
      } else if (var1 instanceof ModeSetting var5) {
         return var5.getValue();
      } else if (var1 instanceof StringSetting var4) {
         return var4.getValue();
      } else if (var1 instanceof ColorSetting var2) {
         Color var3 = var2.getColorValue().getColor();
         return var3.getRed() + "," + var3.getGreen() + "," + var3.getBlue() + "," + var3.getAlpha() + "," + var2.isSync() + "," + var2.isRainbow();
      } else {
         return null;
      }
   }

   @EventTarget
   public void a_void_PacketReceiveEvent(PacketReceiveEvent var1) {
      if (var1.getPacket() instanceof ClientboundCustomPayloadPacket var3) {
         if (var3.payload() instanceof PingBypassPayload var4) {
            if (var1.getConnection() != null) {
               this.n = var1.getConnection();
            }

            FriendlyByteBuf var5 = var4.toBuffer();

            try {
               this.a_void_FriendlyByteBuf(var5);
            } catch (Exception var10) {
               Night.LOGGER.warn("[PingBypass] Failed to handle S2C packet", var10);
            } finally {
               var5.release();
            }
         }
      }
   }

   private void a_void_FriendlyByteBuf(FriendlyByteBuf var1) {
      int var2 = var1.readVarInt();
      if (var2 == 1) {
         this.A();
      } else if (var2 == 5) {
         this.a_void_S2CModuleStatePacket(new S2CModuleStatePacket(var1));
      } else if (var2 == 6) {
         this.a_void_S2CSettingSyncPacket(new S2CSettingSyncPacket(var1));
      } else if (var2 == 7) {
         this.a_void_S2CStatusMessagePacket(new S2CStatusMessagePacket(var1));
      } else if (var2 == 8) {
         this.a_void_S2CProxyDisconnectPacket(new S2CProxyDisconnectPacket(var1));
      } else if (var2 == 10) {
         this.a_void_S2CRenderPlacePosPacket(new S2CRenderPlacePosPacket(var1));
      } else if (var2 == 11) {
         this.a_void_C2SMineBlockPacket(new C2SMineBlockPacket(var1));
      } else if (var2 == 12) {
         this.a_void_S2CMineProgressPacket(new S2CMineProgressPacket(var1));
      } else if (var2 == 13) {
         this.a_void_S2CHeldSlotSyncPacket(new S2CHeldSlotSyncPacket(var1));
      } else if (var2 == 16) {
         this.a_void_C2SWindowClickPacket(new C2SWindowClickPacket(var1));
      }
   }

   private void A() {
      Connection var1 = this.n;
      if (var1 == null && mc.getConnection() != null) {
         var1 = mc.getConnection().getConnection();
      }

      if (var1 != null && var1.isConnected()) {
         this.a_void_Connection_PingBypassPacket(var1, new C2SPasswordResponsePacket(this.password.getValue()));
         Night.LOGGER.info("[PingBypass] Sent password response to proxy");
         this.a_void_Connection(var1);
      } else {
         Night.LOGGER.warn("[PingBypass] Cannot send password response \u2014 no active connection");
      }
   }

   private void a_void_Connection(Connection var1) {
      if (!this.enabled) {
         String var2 = this.server.getValue();
         if (var2 != null && !var2.isBlank()) {
            int var3 = 25565;
            String var5;
            if (var2.contains(":")) {
               String[] var4 = var2.split(":", 2);
               var5 = var4[0];

               try {
                  var3 = Integer.parseInt(var4[1]);
               } catch (NumberFormatException var7) {
                  Night.LOGGER.warn("[PingBypass] Invalid port in server setting: {}", var2);
                  return;
               }
            } else {
               var5 = var2;
            }

            this.enabled = true;
            this.a_void_Connection_PingBypassPacket(var1, new C2SConnectServerPacket(var5, var3));
            Night.LOGGER.info("[PingBypass] Sent join request to proxy: {}:{}", var5, var3);
         }
      }
   }

   private void a_void_Connection_PingBypassPacket(Connection var1, PingBypassPacket var2) {
      PingBypassPayload var3 = PingBypassPayload.fromPacket(var2);
      this.o.add(new PingBypassModule.PendingPacket(var3, var1));
      this.p = 10;
   }

   @EventTarget
   public void a_void_TickEvent(TickEvent var1) {
      if (!this.o.isEmpty()) {
         if (this.p > 0) {
            this.p--;
         } else {
            Iterator var2 = this.o.iterator();

            while (var2.hasNext()) {
               PingBypassModule.PendingPacket var3 = (PingBypassModule.PendingPacket)var2.next();
               if (!var3.connection.isConnected()) {
                  var2.remove();
               } else {
                  try {
                     var3.connection.send(new ServerboundCustomPayloadPacket(var3.pingBypassPayload));
                     var2.remove();
                     Night.LOGGER.info("[PingBypass] Sent deferred payload via vanilla networking");
                  } catch (IllegalStateException var5) {
                     this.p = 5;
                  }
               }
            }
         }
      }
   }

   private void a_void_S2CModuleStatePacket(S2CModuleStatePacket var1) {
      this.l.put(var1.getModuleName(), var1.isEnabled());
      Module var2 = Night.moduleManager.getModuleByName(var1.getModuleName());
      if (var2 != null && var2.isEnabled() != var1.isEnabled()) {
         mc.execute(() -> var2.setEnabled(var1.isEnabled(), false));
      }

      Night.LOGGER.debug("[PingBypass] Module {} state synced to {}", var1.getModuleName(), var1.isEnabled());
   }

   private void a_void_S2CSettingSyncPacket(S2CSettingSyncPacket var1) {
      this.m.put(var1.getModuleName() + "." + var1.getSettingName(), var1.getValue());
      Night.LOGGER.debug("[PingBypass] Setting {}.{} updated to {}", new Object[]{var1.getModuleName(), var1.getSettingName(), var1.getValue()});
   }

   private void a_void_S2CStatusMessagePacket(S2CStatusMessagePacket var1) {
      Night.chatManager.sendError("[PingBypass] " + var1.getMessage());
   }

   private void a_void_S2CProxyDisconnectPacket(S2CProxyDisconnectPacket var1) {
      this.k = var1.getReason();
      PingBypassState.d = true;
      if (this.q == null) {
         this.q = new ClientInputForwarder();
         this.q.a();
      }

      this.v();
      Night.chatManager.sendMessage("[PingBypass] Proxy connected to: " + var1.getReason());
   }

   public void v() {
      Connection var1 = this.n;
      if (var1 == null && mc.getConnection() != null) {
         var1 = mc.getConnection().getConnection();
      }

      if (var1 != null && var1.isConnected()) {
         int var2 = 0;

         for (Module var4 : (Iterable<Module>)(Object)Night.moduleManager.getModules()) {
            if (var4.isProxySupported()) {
               var1.send(new ServerboundCustomPayloadPacket(PingBypassPayload.fromPacket(new C2SModuleTogglePacket(var4.getName(), var4.isEnabled()))));

               for (Setting var6 : var4.getSettings()) {
                  String var7 = this.a_String_Setting(var6);
                  if (var7 != null) {
                     var1.send(
                        new ServerboundCustomPayloadPacket(PingBypassPayload.fromPacket(new C2SSettingUpdatePacket(var4.getName(), var6.getName(), var7)))
                     );
                     var2++;
                  }
               }
            }
         }

         Night.LOGGER.info("[PingBypass] Synced {} settings to proxy", var2);
         this.w();
      }
   }

   public void w() {
      Connection var1 = this.n;
      if (var1 == null && mc.getConnection() != null) {
         var1 = mc.getConnection().getConnection();
      }

      if (var1 != null && var1.isConnected()) {
         var1.send(new ServerboundCustomPayloadPacket(PingBypassPayload.fromPacket(new C2SFriendSyncPacket(new ArrayList<>(Night.friendManager.getFriends())))));
      }
   }

   private void a_void_S2CRenderPlacePosPacket(S2CRenderPlacePosPacket var1) {
      Night.renderPlacementManager.setCrystalTarget(var1.getPos());
   }

   private void a_void_C2SMineBlockPacket(C2SMineBlockPacket var1) {
      PlaceRenderEntry var2 = new PlaceRenderEntry(var1.getPos());
      if (!Night.renderPlacementManager.placedBlocks.contains(var2)) {
         Night.renderPlacementManager.placedBlocks.add(var2);
      }
   }

   private void a_void_S2CMineProgressPacket(S2CMineProgressPacket var1) {
      SpeedMine var2 = Night.moduleManager.getModule(SpeedMine.class);
      if (var2 != null) {
         var2.a_void_BlockPos_float_BlockPos_float(var1.getPrimaryPos(), var1.getPrimaryProgress(), var1.getSecondaryPos(), var1.getSecondaryProgress());
      }
   }

   private void a_void_S2CHeldSlotSyncPacket(S2CHeldSlotSyncPacket var1) {
      if (mc.player != null) {
         mc.player.getInventory().setSelectedSlot(var1.getSlot());
      }
   }

   private void a_void_C2SWindowClickPacket(C2SWindowClickPacket var1) {
      mc.execute(() -> {
         if (mc.player != null && mc.player.containerMenu.containerId == var1.getContainerId()) {
            try {
               mc.player.containerMenu.clicked(var1.getSlotNum(), var1.getButtonNum(), var1.getClickType(), mc.player);
            } catch (Exception var2) {
               Night.LOGGER.warn("[PingBypass] Failed to replay proxy window click", var2);
            }
         }
      });
   }

   public String x() {
      return this.k;
   }

   public Map<String, Boolean> y() {
      return this.l;
   }

   public Map<String, String> z() {
      return this.m;
   }

   public boolean b_bool_String(String var1) {
      return this.l.getOrDefault(var1, false);
   }

   public String a_String_String_String(String var1, String var2) {
      return this.m.get(var1 + "." + var2);
   }

   record PendingPacket(PingBypassPayload pingBypassPayload, Connection connection) {
   }
}
