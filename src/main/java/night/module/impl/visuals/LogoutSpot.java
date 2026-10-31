package night.module.impl.visuals;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.events.EventTarget;
import night.events.impl.ClientConnectEvent;
import night.events.impl.ClientDisconnectEvent;
import night.events.impl.EntitySpawnEvent;
import night.events.impl.PacketReceiveEvent;
import night.events.impl.PlayerDeathEvent;
import night.events.impl.PlayerJoinEvent;
import night.events.impl.PlayerLeaveEvent;
import night.events.impl.Render3DEvent;
import night.events.impl.ServerConnectEvent;
import night.events.impl.TickEvent;
import night.mixins.accessors.LimbAnimatorAccessor;
import night.module.Module;
import night.module.ModuleInfo;
import night.module.impl.core.IgnoreNaked;
import night.module.impl.misc.FakePlayer;
import night.setting.impl.BooleanSetting;
import night.setting.impl.ColorSetting;
import night.setting.impl.ModeSetting;
import night.setting.impl.NumberSetting;
import night.util.color.ColorUtil;
import night.util.render.Render3DUtil;
import night.util.world.EntityUtil;
import night.util.world.PacketAndRenderUtil;

@ModuleInfo(name = "LogoutSpot", description = "Renders a frozen chams model and nametag where players logged out.", category = Module.Category.VISUALS)
public class LogoutSpot extends Module {
   public ModeSetting mode = new ModeSetting("Mode", "The rendering that will be applied to the logout chams.", "Both", new String[]{"Fill", "Outline", "Both"});
   public ColorSetting fillColor = new ColorSetting(
      "FillColor", "The color used for the fill rendering.", new ModeSetting.ModeCondition(this.mode, "Fill", "Both"), ColorUtil.b()
   );
   public ColorSetting outlineColor = new ColorSetting(
      "OutlineColor", "The color used for the outline rendering.", new ModeSetting.ModeCondition(this.mode, "Outline", "Both"), ColorUtil.c()
   );
   public BooleanSetting nametag = new BooleanSetting("Nametag", "Renders player info above the logout spot.", true);
   public BooleanSetting health = new BooleanSetting("Health", "Renders the health of the logged out player.", true);
   public BooleanSetting totemPops = new BooleanSetting("TotemPops", "Renders the popped totems count of the logged out player.", true);
   public NumberSetting scale = new NumberSetting("Scale", "The scaling applied to the nametag rendering.", 30, 10, 100);
   public BooleanSetting debug = new BooleanSetting("Debug", "Logs every step of logout detection to chat.", false);
   static final EquipmentSlot[] m = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
   private final Map<UUID, LogoutSpot.LogoutEntry> n = new ConcurrentHashMap<>();
   private final Map<RemotePlayer, LogoutSpot.LogoutEntry> o = new ConcurrentHashMap<>();
   private final Map<UUID, LogoutSpot.PlayerSnapshot> p = new ConcurrentHashMap<>();
   private static final long q = 500L;
   private static final long r = 10000L;
   private final Map<UUID, Long> s = new ConcurrentHashMap<>();
   private int t = -80000;
   private static final long u = 1500L;
   private final Map<UUID, Long> v = new ConcurrentHashMap<>();
   private final Map<UUID, Long> w = new ConcurrentHashMap<>();
   private final Map<Integer, UUID> x = new ConcurrentHashMap<>();
   private long y = 0L;
   private Vec3 z = null;

   private void b_void_String(String var1) {
      if (this.debug.getValue() && Night.chatManager != null) {
         Night.chatManager.sendTaggedMessage(var1, this.getName());
      }
   }

   private boolean a_bool_UUID(UUID var1) {
      if (var1 == null) {
         return false;
      }

      Long var2 = this.w.get(var1);
      if (var2 != null && System.currentTimeMillis() - var2 < 12000L) {
         return true;
      }

      for (UUID var4 : (Iterable<UUID>)(Object)this.x.values()) {
         if (var1.equals(var4)) {
            return true;
         }
      }

      return false;
   }

   private void y() {
      this.y = System.currentTimeMillis();
      this.v.clear();
      this.p.clear();
      this.s.clear();
   }

   public boolean a_bool_UUID_String(UUID var1, String var2) {
      if (var1 == null && var2 == null) {
         return false;
      }

      if (mc.level != null) {
         try {
            for (Player var4 : (Iterable<Player>)(Object)mc.level.players()) {
               if (var4 != null && !this.a_bool_Entity(var4)) {
                  if (var1 != null && var1.equals(var4.getUUID())) {
                     return true;
                  }

                  if (var2 != null && var4.getName() != null && var2.equalsIgnoreCase(var4.getName().getString())) {
                     return true;
                  }
               }
            }
         } catch (Exception var5) {
         }
      }

      if (mc.getConnection() != null) {
         if (var1 != null && mc.getConnection().getPlayerInfo(var1) != null) {
            return true;
         }

         if (var2 != null && mc.getConnection().getPlayerInfo(var2) != null) {
            return true;
         }

         for (PlayerInfo var7 : (Iterable<PlayerInfo>)(Object)mc.getConnection().getOnlinePlayers()) {
            if (var7 != null && var7.getProfile() != null) {
               if (var1 != null && var1.equals(var7.getProfile().id())) {
                  return true;
               }

               if (var2 != null && var2.equalsIgnoreCase(var7.getProfile().name())) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   @EventTarget
   public void a_void_TickEvent(TickEvent var1) {
      if (mc.level != null && mc.player != null) {
         if (this.z != null && mc.player.position().distanceToSqr(this.z) > 100.0) {
            this.y();
         }

         this.z = mc.player.position();
         long var2 = System.currentTimeMillis();
         this.w.entrySet().removeIf(var2x -> var2 - var2x.getValue() > 15000L);
         this.x.entrySet().removeIf(var0 -> mc.level.getEntity(var0.getKey()) == null);
         FakePlayer var4 = Night.moduleManager != null ? Night.moduleManager.getModule(FakePlayer.class) : null;
         RemotePlayer var5 = var4 != null && var4.isEnabled() ? var4.z() : null;
         boolean var6 = Night.moduleManager.getModule(IgnoreNaked.class).isEnabled();

         for (Player var8 : (Iterable<Player>)(Object)new ArrayList(mc.level.players())) {
            if (var8 != mc.player && var8 != var5 && !this.a_bool_Entity(var8) && var8.isAlive() && (!var6 || !EntityUtil.b_bool_Player(var8))) {
               UUID var9 = var8.getUUID();
               this.v.remove(var9);
               if (this.n.containsKey(var9)) {
                  this.c_void_UUID(var9);
               }

               boolean var10 = var8.getMainHandItem().getItem() == Items.ENDER_PEARL || var8.getOffhandItem().getItem() == Items.ENDER_PEARL;
               if (var10 && (PacketAndRenderUtil.a_bool_LivingEntity(var8) || var8.isUsingItem())) {
                  this.w.put(var9, var2);
               }

               int var11 = Night.worldStateManager != null ? Night.worldStateManager.getTotemPops().getOrDefault(var9, 0) : 0;
               this.p.put(var9, new LogoutSpot.PlayerSnapshot(var8, var11));
               this.s.put(var9, var2);
            }
         }

         this.p.keySet().removeIf(var3 -> !this.v.containsKey(var3) && var2 - this.s.getOrDefault(var3, 0L) > 10000L);
         this.s.keySet().removeIf(var1x -> !this.p.containsKey(var1x));
         if (!this.v.isEmpty()) {
            for (Entry var14 : (Iterable<Entry>)(Object)this.v.entrySet()) {
               UUID var16 = (UUID)var14.getKey();
               LogoutSpot.PlayerSnapshot var18 = this.p.get(var16);
               String var20 = var18 != null ? var18.a : null;
               if (this.a_bool_UUID_String(var16, var20)) {
                  this.b_void_String(var20 + ": logout cancelled (still in world or tab list)");
                  this.v.remove(var16);
               } else if (this.a_bool_UUID(var16)) {
                  this.b_void_String(var20 + ": logout cancelled (pearled recently)");
                  this.v.remove(var16);
               } else if (var2 - (Long)var14.getValue() >= 1500L) {
                  this.v.remove(var16);
                  this.b_void_String(var20 + ": spawning logout spot (tracked=" + (var18 != null) + ")");
                  this.b_void_UUID(var16);
               }
            }
         }

         if (!this.n.isEmpty()) {
            for (Entry var15 : (Iterable<Entry>)(Object)this.n.entrySet()) {
               UUID var17 = (UUID)var15.getKey();
               LogoutSpot.LogoutEntry var19 = (LogoutSpot.LogoutEntry)var15.getValue();
               String var21 = var19.b != null ? var19.b.a : null;
               if (this.a_bool_UUID_String(var17, var21)) {
                  this.c_void_UUID(var17);
               }
            }
         }
      }
   }

   @EventTarget
   public void a_void_PlayerLeaveEvent(PlayerLeaveEvent var1) {
      if (this.isEnabled() && mc.level != null && mc.player != null) {
         UUID var2 = var1.getUuid();
         if (var2 != null && !var2.equals(mc.player.getUUID())) {
            String var3 = var1.getName() != null ? var1.getName() : var2.toString();
            if (System.currentTimeMillis() - this.y < 3000L) {
               this.b_void_String(var3 + ": tab-remove ignored (you teleported <3s ago)");
            } else {
               FakePlayer var4 = Night.moduleManager != null ? Night.moduleManager.getModule(FakePlayer.class) : null;
               if (var4 == null || !var4.isEnabled() || var4.z() == null || !var2.equals(var4.z().getUUID())) {
                  Long var5 = this.s.get(var2);
                  if (!this.p.containsKey(var2) || var5 == null || System.currentTimeMillis() - var5 > 500L) {
                     this.b_void_String(
                        var3
                           + ": tab-remove ignored (tracked="
                           + this.p.containsKey(var2)
                           + ", lastSeen="
                           + (var5 == null ? "never" : System.currentTimeMillis() - var5 + "ms ago")
                           + ")"
                     );
                  } else if (this.a_bool_UUID(var2)) {
                     this.b_void_String(var3 + ": tab-remove ignored (pearled recently)");
                     this.p.remove(var2);
                  } else {
                     this.b_void_String(var3 + ": logout pending (1500ms debounce)");
                     this.v.put(var2, System.currentTimeMillis());
                  }
               }
            }
         }
      }
   }

   private void b_void_UUID(UUID var1) {
      LogoutSpot.PlayerSnapshot var2 = this.p.get(var1);
      if (var2 != null) {
         mc.execute(() -> {
            if (mc.level != null && !this.n.containsKey(var1)) {
               if (!this.a_bool_UUID_String(var1, var2.a)) {
                  if (!this.a_bool_UUID(var1)) {
                     GameProfile var3 = new GameProfile(UUID.randomUUID(), var2.a);
                     RemotePlayer var4 = new RemotePlayer(mc.level, var3);
                     var4.setId(this.t--);
                     var4.setPos(var2.b, var2.c, var2.d);
                     var4.xo = var2.e;
                     var4.yo = var2.f;
                     var4.zo = var2.g;
                     var4.xOld = var2.b;
                     var4.yOld = var2.c;
                     var4.zOld = var2.d;
                     var4.setYRot(var2.h);
                     var4.setXRot(var2.i);
                     var4.yRotO = var2.h;
                     var4.xRotO = var2.i;
                     var4.setYHeadRot(var2.j);
                     var4.yHeadRotO = var2.j;
                     var4.yBodyRot = var2.k;
                     var4.yBodyRotO = var2.k;
                     var4.setPose(var2.l);
                     var4.setShiftKeyDown(var2.m);
                     var4.setSwimming(var2.n);
                     var4.setSprinting(var2.o);
                     var4.refreshDimensions();
                     var4.walkAnimation.setSpeed(var2.p);
                     ((LimbAnimatorAccessor)var4.walkAnimation).setPos(var2.q);
                     var2.r.a(var4);
                     var4.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                     var4.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);

                     for (EquipmentSlot var8 : m) {
                        var4.setItemSlot(var8, var2.u.getOrDefault(var8, ItemStack.EMPTY).copy());
                     }

                     mc.level.addEntity(var4);
                     LogoutSpot.LogoutEntry var9 = new LogoutSpot.LogoutEntry(var4, var2);
                     this.n.put(var1, var9);
                     this.o.put(var4, var9);
                  }
               }
            }
         });
      }
   }

   @EventTarget
   public void a_void_PlayerJoinEvent(PlayerJoinEvent var1) {
      UUID var2 = var1.getUuid();
      if (var2 != null) {
         this.v.remove(var2);
         if (this.n.containsKey(var2)) {
            mc.execute(() -> this.c_void_UUID(var2));
         }
      }
   }

   @EventTarget
   public void a_void_PlayerDeathEvent(PlayerDeathEvent var1) {
      if (var1.getPlayer() == mc.player) {
         this.z();
      }
   }

   @EventTarget
   public void a_void_ClientDisconnectEvent(ClientDisconnectEvent var1) {
      this.z();
   }

   @EventTarget
   public void a_void_ClientConnectEvent(ClientConnectEvent var1) {
      this.z();
   }

   @EventTarget
   public void a_void_ServerConnectEvent(ServerConnectEvent var1) {
      this.z();
   }

   @EventTarget
   public void a_void_EntitySpawnEvent(EntitySpawnEvent var1) {
      if (mc.level != null) {
         if (var1.getEntity() instanceof ThrownEnderpearl var3) {
            UUID var10 = null;
            if (var3.getOwner() instanceof Player var5) {
               if (var5 != mc.player && !this.a_bool_Entity(var5)) {
                  var10 = var5.getUUID();
               }
            } else {
               double var11 = 36.0;

               for (Player var7 : (Iterable<Player>)(Object)mc.level.players()) {
                  if (var7 != mc.player && !this.a_bool_Entity(var7)) {
                     double var8 = var7.distanceToSqr(var3.position());
                     if (var8 < var11) {
                        var11 = var8;
                        var10 = var7.getUUID();
                     }
                  }
               }
            }

            if (var10 != null) {
               this.w.put(var10, System.currentTimeMillis());
               this.x.put(var3.getId(), var10);
            }
         }
      }
   }

   @EventTarget
   public void a_void_PacketReceiveEvent(PacketReceiveEvent var1) {
      if (var1.getPacket() instanceof ClientboundRespawnPacket) {
         mc.execute(this::z);
      } else {
         if (var1.getPacket() instanceof ClientboundPlayerPositionPacket var3 && mc.player != null) {
            Vec3 var10 = mc.player.position();
            double var4 = var3.change().position().x();
            double var6 = var3.change().position().y();
            double var8 = var3.change().position().z();
            if (var10.distanceToSqr(var4, var6, var8) > 100.0) {
               mc.execute(this::y);
            }
         }
      }
   }

   @EventTarget
   public void a_void_Post(Render3DEvent.Post var1) {
      if (this.isEnabled() && mc.level != null && mc.player != null) {
         if (this.nametag.getValue() && !this.n.isEmpty()) {
            PoseStack var2 = var1.getMatrices();

            for (LogoutSpot.LogoutEntry var4 : (Iterable<LogoutSpot.LogoutEntry>)(Object)this.n.values()) {
               RemotePlayer var5 = var4.a;
               if (var5 != null) {
                  double var6 = var5.getX();
                  double var8 = var5.getY() + (var5.isShiftKeyDown() ? 1.9F : 2.1F);
                  double var10 = var5.getZ();
                  Vec3 var12 = new Vec3(
                     var6 - mc.gameRenderer.mainCamera().position().x,
                     var8 - mc.gameRenderer.mainCamera().position().y,
                     var10 - mc.gameRenderer.mainCamera().position().z
                  );
                  float var13 = (float)Math.sqrt(mc.gameRenderer.mainCamera().position().distanceToSqr(var6, var8, var10));
                  float var14 = 0.0018F + this.scale.getValue().intValue() / 10000.0F * var13;
                  if (var13 <= 8.0F) {
                     var14 = 0.0245F;
                  }

                  var2.pushPose();
                  var2.translate(var12.x, var12.y, var12.z);
                  PacketAndRenderUtil.a(var2, mc.gameRenderer.mainCamera().rotation());
                  var2.scale(var14, -var14, var14);
                  StringBuilder var15 = new StringBuilder(var4.b.a);
                  if (this.health.getValue()) {
                     float var16 = var4.b.v + var4.b.w;
                     var15.append(" HP: ").append(new DecimalFormat("0.0").format(var16));
                  }

                  if (this.totemPops.getValue() && var4.b.x > 0) {
                     var15.append(" -").append(var4.b.x);
                  }

                  String var19 = var15.toString();
                  int var17 = Night.fontManager.getStringWidth(var19);
                  int var18 = Night.fontManager.getFontHeight();
                  Render3DUtil.a(var2, -var17 / 2.0F - 2.0F, -var18 - 1, var17 / 2.0F + 2.0F, 1.0F, new Color(0, 0, 0, 120));
                  Render3DUtil.b(var2, -var17 / 2.0F - 2.0F, -var18 - 1, var17 / 2.0F + 2.0F, 1.0F, new Color(0, 0, 0, 160));
                  Render3DUtil.a_void_List_List_bool(Render3DUtil.b, Render3DUtil.c, false);
                  Render3DUtil.b.clear();
                  Render3DUtil.c.clear();
                  Night.fontManager.drawPlainWorldText(var2, var19, -var17 / 2, -var18, ColorUtil.a(this.fillColor.getColor(), 255));
                  var2.popPose();
               }
            }
         }
      }
   }

   @Override
   public void onDisable() {
      this.z();
   }

   private void c_void_UUID(UUID var1) {
      LogoutSpot.LogoutEntry var2 = this.n.remove(var1);
      if (var2 != null) {
         this.o.remove(var2.a);
         this.d_void_RemotePlayer(var2.a);
      }
   }

   private void z() {
      this.n.values().forEach(var1 -> this.d_void_RemotePlayer(var1.a));
      this.n.clear();
      this.o.clear();
      this.p.clear();
      this.s.clear();
      this.v.clear();
      this.w.clear();
      this.x.clear();
      this.z = null;
   }

   private void d_void_RemotePlayer(RemotePlayer var1) {
      if (var1 != null) {
         Runnable var2 = () -> {
            if (mc.level != null) {
               mc.level.removeEntity(var1.getId(), RemovalReason.DISCARDED);
            }
         };
         if (mc.isSameThread()) {
            var2.run();
         } else {
            mc.execute(var2);
         }
      }
   }

   public boolean a_bool_Entity(Entity var1) {
      return var1 instanceof RemotePlayer && this.o.containsKey(var1);
   }

   public Collection<RemotePlayer> v() {
      return this.o.keySet();
   }

   public LogoutSpot.LogoutEntry a_LogoutEntry_RemotePlayer(RemotePlayer var1) {
      return this.o.get(var1);
   }

   public boolean w() {
      return this.mode.getValue().equals("Fill") || this.mode.getValue().equals("Both");
   }

   public boolean x() {
      return this.mode.getValue().equals("Outline") || this.mode.getValue().equals("Both");
   }

   public Color b_Color_RemotePlayer(RemotePlayer var1) {
      return this.fillColor.getColor();
   }

   public Color c_Color_RemotePlayer(RemotePlayer var1) {
      return this.outlineColor.getColor();
   }

   public static class LogoutEntry {
      public final RemotePlayer a;
      public final LogoutSpot.PlayerSnapshot b;
      public final long c;

      public LogoutEntry(RemotePlayer var1, LogoutSpot.PlayerSnapshot var2) {
         this.a = var1;
         this.b = var2;
         this.c = System.currentTimeMillis();
      }
   }

   public static class PlayerSnapshot {
      public final String a;
      public final double b;
      public final double c;
      public final double d;
      public final double e;
      public final double f;
      public final double g;
      public final float h;
      public final float i;
      public final float j;
      public final float k;
      public final Pose l;
      public final boolean m;
      public final boolean n;
      public final boolean o;
      public final float p;
      public final float q;
      public final PacketAndRenderUtil.SwingSnapshot r;
      public final ItemStack s;
      public final ItemStack t;
      public final Map<EquipmentSlot, ItemStack> u = new ConcurrentHashMap<>();
      public final float v;
      public final float w;
      public final int x;

      public PlayerSnapshot(Player var1, int var2) {
         this.a = var1.getName().getString();
         this.b = var1.getX();
         this.c = var1.getY();
         this.d = var1.getZ();
         this.e = var1.xo;
         this.f = var1.yo;
         this.g = var1.zo;
         this.h = var1.getYRot();
         this.i = var1.getXRot();
         this.j = var1.getYHeadRot();
         this.k = var1.yBodyRot;
         this.l = var1.getPose();
         this.m = var1.isShiftKeyDown();
         this.n = var1.isSwimming();
         this.o = var1.isSprinting();
         this.p = var1.walkAnimation.speed();
         this.q = var1.walkAnimation.position();
         this.r = PacketAndRenderUtil.b_SwingSnapshot_LivingEntity(var1);
         this.s = var1.getMainHandItem().copy();
         this.t = var1.getOffhandItem().copy();

         for (EquipmentSlot var6 : (Iterable<EquipmentSlot>)(Object)LogoutSpot.m) {
            this.u.put(var6, var1.getItemBySlot(var6).copy());
         }

         this.v = var1.getHealth();
         this.w = var1.getAbsorptionAmount();
         this.x = var2;
      }
   }
}
