package night.module;

import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import net.minecraft.ChatFormatting;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import night.Night;
import night.events.impl.ModuleToggleEvent;
import night.pingbypass.PingBypassState;
import night.pingbypass.module.ProxyModuleManager;
import night.pingbypass.protocol.PingBypassPayload;
import night.pingbypass.protocol.c2s.C2SModuleTogglePacket;
import night.pingbypass.protocol.s2c.S2CModuleStatePacket;
import night.setting.Setting;
import night.setting.impl.BooleanSetting;
import night.setting.impl.CategorySetting;
import night.setting.impl.ColorSetting;
import night.setting.impl.KeybindSetting;
import night.setting.impl.ModeSetting;
import night.setting.impl.NumberSetting;
import night.setting.impl.PositionSetting;
import night.setting.impl.StringSetting;
import night.setting.impl.WhitelistSetting;
import night.util.MinecraftInstance;
import night.util.animation.Animation;
import night.util.animation.EasingUtil;
import night.util.color.ChatColorUtil;
import night.util.inventory.InventoryUtil;

public abstract class Module implements MinecraftInstance {
   private final String name;
   private final String description;
   private final Module.Category category;
   private final boolean persistent;
   private final boolean proxySupported;
   private boolean enabled;
   private final List<Setting> settings;
   public BooleanSetting chatNotify;
   public BooleanSetting drawn;
   public KeybindSetting bind;
   public ModeSetting proxyMode;
   private final Animation animation;

   public Module() {
      ModuleInfo var1 = this.getClass().getAnnotation(ModuleInfo.class);
      String var2 = var1.name();
      this.name = var2;
      String var3 = var1.description();
      this.description = var3;
      Module.Category var4 = var1.category();
      this.category = var4;
      boolean var5 = var1.persistent();
      this.persistent = var5;
      boolean var6 = var1.proxySupported();
      this.proxySupported = var6;
      boolean var7 = var1.enabled();
      this.enabled = var7;
      this.settings = new ArrayList<>();
      this.animation = new Animation(300, EasingUtil.Easing.EASE_IN_OUT_CUBIC);
      this.chatNotify = new BooleanSetting("ChatNotify", "Notifies you in chat whenever the module gets toggled on or off.", true);
      boolean var8 = var1.drawn();
      this.drawn = new BooleanSetting("Drawn", "Renders the module's name on the HUD's module list.", var8);
      int var9 = var1.bind();
      this.bind = new KeybindSetting("Bind", "The keybind that toggles the module on and off.", var9);
      if (this.proxySupported) {
         String[] var10 = new String[]{"Auto", "Proxy", "Local"};
         this.proxyMode = new ModeSetting("ProxyMode", "Where this module executes when connected to a PingBypass proxy.", "Auto", var10);
      }

      if (this.persistent) {
         this.enabled = true;
      }

      if (this.enabled) {
         Night.EVENT_BUS.register(this);
      }
   }

   public boolean nullCheck() {
      boolean var1;
      if (mc.player != null && mc.level != null) {
         var1 = false;
      } else {
         var1 = true;
      }

      return var1;
   }

   public boolean isProxyDelegated() {
      if (!this.proxySupported || this.proxyMode == null) {
         return false;
      }

      if (!PingBypassState.d) {
         return false;
      }

      if (Night.pingBypassConfig != null && Night.pingBypassConfig.isServerMode()) {
         return false;
      }

      return switch (this.proxyMode.getValue()) {
         case "Auto", "Proxy" -> true;
         case "Local" -> false;
         default -> true;
      };
   }

   public boolean shouldDelegateToProxy() {
      return this.isProxyDelegated();
   }

   public boolean isRunningOnProxyServer() {
      if (!this.proxySupported) {
         return false;
      } else if (!PingBypassState.d) {
         return false;
      } else {
         return Night.pingBypassConfig != null && Night.pingBypassConfig.isServerMode()
            ? this.proxyMode == null || !this.proxyMode.getValue().equals("Local")
            : false;
      }
   }

   public void onEnable() {
   }

   public void onDisable() {
   }

   public String getDisplayInfo() {
      return "";
   }

   public String getProxySuffix() {
      return this.isProxyDelegated() && Night.pingBypassConfig != null && Night.pingBypassConfig.isServerMode() ? " \u00a7d[PB]" : "";
   }

   private boolean modulePrivate_v() {
      return this.isProxyDelegated();
   }

   public void setEnabled(boolean var1) {
      this.setEnabled(var1, true);
   }

   public void setEnabled(boolean var1, boolean var2) {
      if (!this.persistent) {
         boolean var3 = this.enabled;
         if (var1 != var3) {
            if (!var1 || Night.licenseService != null && Night.licenseService.isUnlocked()) {
               label68: {
                  this.enabled = var1;
                  boolean var4 = this.enabled;
                  Night.EVENT_BUS.post(new ModuleToggleEvent(this, var4));
                  if (!this.isProxyDelegated()) {
                     String var5 = this.name;
                     if (!ProxyModuleManager.SUPPORTED_MODULES.contains(var5)) {
                        break label68;
                     }
                  }

                  if (var2 && Night.pingBypassConfig != null && !Night.pingBypassConfig.isServerMode()) {
                     boolean var6 = this.enabled;
                     this.sendToggleToProxy(var6);
                  }
               }

               if (this.proxySupported
                  && Night.pingBypassConfig != null
                  && Night.pingBypassConfig.isServerMode()
                  && PingBypassState.d
                  && Night.pingBypassServer != null) {
                  String var7 = this.name;
                  boolean var8 = this.enabled;
                  PingBypassPayload var9 = PingBypassPayload.fromPacket(new S2CModuleStatePacket(var7, var8));
                  ClientboundCustomPayloadPacket var10 = new ClientboundCustomPayloadPacket(var9);

                  for (Connection var12 : (Iterable<Connection>)(Object)Night.pingBypassServer.getConnections()) {
                     if (var12.isConnected()) {
                        Connection var13 = var12;
                        ClientboundCustomPayloadPacket var14 = var10;
                        var13.send(var14);
                     }
                  }
               }

               if (this.enabled) {
                  this.animation.setEasing(EasingUtil.Easing.EASE_IN_OUT_CUBIC);
                  if (var2 && this.chatNotify.getValue()) {
                     String var15 = String.valueOf(ChatColorUtil.a());
                     String var16 = this.name;
                     String var17 = String.valueOf(ChatColorUtil.b());
                     String var18 = String.valueOf(ChatFormatting.GREEN);
                     String var19 = String.valueOf(ChatColorUtil.b());
                     Night.chatManager
                        .sendReplaceableMessage(var15 + var16 + var17 + " = " + var18 + "true" + var19 + ";", "toggle-" + this.getName().toLowerCase());
                  }

                  this.onEnable();
                  if (this.enabled) {
                     Night.EVENT_BUS.register(this);
                  }
               } else {
                  this.animation.setEasing(EasingUtil.Easing.EASE_IN_OUT_CUBIC);
                  Night.EVENT_BUS.unregister(this);
                  this.onDisable();
                  InventoryUtil.e();
                  if (var2 && this.chatNotify.getValue()) {
                     String var20 = String.valueOf(ChatColorUtil.a());
                     String var21 = this.name;
                     String var22 = String.valueOf(ChatColorUtil.b());
                     String var23 = String.valueOf(ChatFormatting.RED);
                     String var24 = String.valueOf(ChatColorUtil.b());
                     Night.chatManager
                        .sendReplaceableMessage(var20 + var21 + var22 + " = " + var23 + "false" + var24 + ";", "toggle-" + this.getName().toLowerCase());
                  }
               }
            } else {
               if (var2) {
                  Night.chatManager.sendError("Modules are locked \u2014 see the Night dashboard.");
               }
            }
         }
      }
   }

   public int getKeybind() {
      return this.bind.getKey();
   }

   public void setKeybind(int var1) {
      this.bind.setKey(var1);
   }

   public void resetSettings() {
      for (Setting var2 : (Iterable<Setting>)(Object)this.settings) {
         if (var2 instanceof BooleanSetting) {
            ((BooleanSetting)var2).reset();
         }

         if (var2 instanceof NumberSetting) {
            ((NumberSetting)var2).reset();
         }

         if (var2 instanceof ModeSetting) {
            ((ModeSetting)var2).reset();
         }

         if (var2 instanceof StringSetting) {
            ((StringSetting)var2).reset();
         }

         if (var2 instanceof KeybindSetting) {
            ((KeybindSetting)var2).reset();
         }

         if (var2 instanceof WhitelistSetting) {
            ((WhitelistSetting)var2).clear();
         }

         if (var2 instanceof ColorSetting) {
            ((ColorSetting)var2).reset();
         }

         if (var2 instanceof PositionSetting) {
            ((PositionSetting)var2).reset();
         }
      }
   }

   public Setting getSetting(String var1) {
      return this.settings.stream().filter(var1x -> {
         boolean var2;
         if (var1x.getName().equalsIgnoreCase(var1) && !(var1x instanceof CategorySetting)) {
            var2 = true;
         } else {
            var2 = false;
         }

         return var2;
      }).findFirst().orElse(null);
   }

   private void sendToggleToProxy(boolean var1) {
      try {
         if (mc.getConnection() != null) {
            String var2 = this.name;
            PingBypassPayload var3 = PingBypassPayload.fromPacket(new C2SModuleTogglePacket(var2, var1));
            Connection var4 = mc.getConnection().getConnection();
            PingBypassPayload var5 = var3;
            var4.send(new ServerboundCustomPayloadPacket(var5));
         }
      } catch (Exception var7) {
         Night.LOGGER.warn("[PingBypass] Failed to send module toggle to proxy", var7);
      }
   }

   @Generated
   public String getName() {
      return this.name;
   }

   @Generated
   public String getDescription() {
      return this.description;
   }

   @Generated
   public Module.Category getCategory() {
      return this.category;
   }

   @Generated
   public boolean isPersistent() {
      return this.persistent;
   }

   @Generated
   public boolean isProxySupported() {
      return this.proxySupported;
   }

   @Generated
   public boolean isEnabled() {
      return this.enabled;
   }

   @Generated
   public List<Setting> getSettings() {
      return this.settings;
   }

   @Generated
   public BooleanSetting getChatNotify() {
      return this.chatNotify;
   }

   @Generated
   public BooleanSetting getDrawn() {
      return this.drawn;
   }

   @Generated
   public ModeSetting getProxyMode() {
      return this.proxyMode;
   }

   @Generated
   public Animation getAnimation() {
      return this.animation;
   }

   public enum Category {
      COMBAT("Combat"),
      PLAYER("Player"),
      VISUALS("Visuals"),
      MOVEMENT("Movement"),
      MISCELLANEOUS("Miscellaneous"),
      CORE("Core"),
      KINGMC("KingMC");

      private final String name;

      Category(String var3) {
         this.name = var3;
      }

      @Generated
      public String getName() {
         return this.name;
      }
   }
}
