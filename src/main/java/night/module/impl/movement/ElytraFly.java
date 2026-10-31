package night.module.impl.movement;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.Deque;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundPingPacket;
import net.minecraft.network.protocol.common.ServerboundPongPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket.Action;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import night.Night;
import night.events.EventTarget;
import night.events.impl.InputUpdateEvent;
import night.events.impl.MotionSyncEvent;
import night.events.impl.PacketReceiveEvent;
import night.events.impl.PacketSendEvent;
import night.events.impl.PushOutOfBlocksEvent;
import night.events.impl.UpdateWalkingPlayerEvent;
import night.mixins.accessors.EntityAccessor;
import night.mixins.accessors.EntityFlagAccessor;
import night.mixins.accessors.PlayerMoveC2SPacketAccessor;
import night.module.Module;
import night.module.ModuleInfo;
import night.module.access.FreecamAccess;
import night.module.impl.combat.AutoMace;
import night.setting.impl.BooleanSetting;
import night.setting.impl.ModeSetting;
import night.setting.impl.NumberSetting;
import night.util.inventory.InventoryUtil;
import night.util.math.TimerUtil;
import night.util.network.PacketPredictionUtil;
import night.util.rotation.RotationMathUtil;
import night.utils.minecraft.BaritoneUtils;

@ModuleInfo(name = "ElytraFly", description = "Allows you to fly using an elytra without fireworks.", category = Module.Category.MOVEMENT)
public class ElytraFly extends Module {
   public ModeSetting mode;
   public NumberSetting horizontal;
   public NumberSetting vertical;
   public BooleanSetting moveVertically;
   public BooleanSetting takeoff;
   public BooleanSetting autoPitch;
   public NumberSetting pitch;
   public BooleanSetting minimizePackets;
   public NumberSetting flagPause;
   public BooleanSetting fakeLag;
   public BooleanSetting passObstacles;
   public NumberSetting obstacleLookAhead;
   public BooleanSetting standUpToPass;
   public BooleanSetting putOnElytra;
   public BooleanSetting bypass;
   public NumberSetting bypassTicks;
   public ModeSetting control;
   public NumberSetting turnSpeed;
   public NumberSetting fireworksDelay;
   public ModeSetting x;
   public BooleanSetting y;
   public BooleanSetting z;
   public NumberSetting grimDelay;
   public BooleanSetting muteElytra;
   private boolean E;
   private float F;
   private float G;
   private boolean H;
   private int I;
   private float J;
   private boolean K;
   private long L;
   private double M;
   private static final long N = 300L;
   private boolean O;
   private long P;
   private boolean Q;
   private boolean R;
   private int S;
   private int T;
   private static final int U = 2;
   private int V;
   private final TimerUtil W;
   private float X;
   private boolean Y;
   private boolean Z;
   private boolean aa;
   private boolean ab;
   private int ac;
   private boolean ad;
   private double ae;
   private double af;
   private final Deque<Packet<?>> ag;
   private final Deque<ClientboundPingPacket> ah;
   private boolean ai;
   private boolean aj;
   public static volatile boolean C = false;
   public static volatile float D = 0.0F;
   private boolean ak;
   private volatile int al;
   private Vec3 am;
   private Vec3 an;
   private double ao;
   private Vec3 ap;
   private Vec3 aq;
   private static final double ar = 0.063;
   private static final boolean as = true;
   private static final double at = 2.0;
   private static final double au = 45.0;
   private int av;
   private ItemStack aw;
   private ItemStack ax;

   public ElytraFly() {
      String[] var1 = new String[]{"Control", "ControlRocket", "Bounce"};
      this.mode = new ModeSetting("Mode", "The mode that will be used for elytra flying.", "ControlRocket", var1);
      ModeSetting var3 = this.mode;
      String[] var4 = new String[]{"Control"};
      ModeSetting.ModeCondition var2 = new ModeSetting.ModeCondition(var3, var4);
      Float var5 = 2.0F;
      Float var6 = 0.1F;
      Float var7 = 10.0F;
      this.horizontal = new NumberSetting("Horizontal", "The speed at which you will be flying horizontally.", var2, var5, var6, var7);
      ModeSetting var9 = this.mode;
      String[] var10 = new String[]{"Control"};
      ModeSetting.ModeCondition var8 = new ModeSetting.ModeCondition(var9, var10);
      Float var11 = 1.0F;
      Float var12 = 0.1F;
      Float var13 = 10.0F;
      this.vertical = new NumberSetting("Vertical", "The speed at which you will be flying vertically.", var8, var11, var12, var13);
      ModeSetting var15 = this.mode;
      String[] var16 = new String[]{"Control"};
      ModeSetting.ModeCondition var14 = new ModeSetting.ModeCondition(var15, var16);
      this.moveVertically = new BooleanSetting("MoveVertically", "Whether or not to allow for vertical movement.", var14, true);
      ModeSetting var18 = this.mode;
      String[] var19 = new String[]{"Bounce"};
      ModeSetting.ModeCondition var17 = new ModeSetting.ModeCondition(var18, var19);
      this.takeoff = new BooleanSetting("Takeoff", "Automatically jumps and initiates gliding when grounded with an elytra equipped.", var17, true);
      ModeSetting var21 = this.mode;
      String[] var22 = new String[]{"Bounce"};
      ModeSetting.ModeCondition var20 = new ModeSetting.ModeCondition(var21, var22);
      this.autoPitch = new BooleanSetting(
         "AutoPitch", "Automatically pitches the player's rotation down while gliding to bounce at faster speeds.", var20, true
      );
      ModeSetting var24 = this.mode;
      String[] var25 = new String[]{"Bounce"};
      ModeSetting.ModeCondition var23 = new ModeSetting.ModeCondition(var24, var25);
      Float var26 = 72.0F;
      Float var27 = 0.0F;
      Float var28 = 90.0F;
      this.pitch = new NumberSetting("Pitch", "The pitch angle to look down when auto pitch is active.", var23, var26, var27, var28);
      ModeSetting var30 = this.mode;
      String[] var31 = new String[]{"Bounce"};
      ModeSetting.ModeCondition var29 = new ModeSetting.ModeCondition(var30, var31);
      this.minimizePackets = new BooleanSetting("MinimizePackets", "Send the bare minimum to start flying, nothing extra.", var29, true);
      ModeSetting var33 = this.mode;
      String[] var34 = new String[]{"Bounce"};
      ModeSetting.ModeCondition var32 = new ModeSetting.ModeCondition(var33, var34);
      Float var35 = 5.0F;
      Float var36 = 0.0F;
      Float var37 = 100.0F;
      this.flagPause = new NumberSetting("FlagPause", "How long (in ticks) to pause if the server flags you for a movement check.", var32, var35, var36, var37);
      ModeSetting var39 = this.mode;
      String[] var40 = new String[]{"Bounce"};
      ModeSetting.ModeCondition var38 = new ModeSetting.ModeCondition(var39, var40);
      this.fakeLag = new BooleanSetting("FakeLag", "Emulates the player lagging to allow flying in 1x2 tunnels.", var38, true);
      ModeSetting var42 = this.mode;
      String[] var43 = new String[]{"Bounce"};
      ModeSetting.ModeCondition var41 = new ModeSetting.ModeCondition(var42, var43);
      this.passObstacles = new BooleanSetting("PassObstacles", "Automatically paths around obstacles in the flight line using Baritone.", var41, false);
      ModeSetting var45 = this.mode;
      String[] var46 = new String[]{"Bounce"};
      ModeSetting.ModeCondition var44 = new ModeSetting.ModeCondition(var45, var46);
      Float var47 = 8.0F;
      Float var48 = 0.0F;
      Float var49 = 50.0F;
      this.obstacleLookAhead = new NumberSetting(
         "ObstacleLookAhead", "Blocks to look ahead for obstacles / step by when pathing around one.", var44, var47, var48, var49
      );
      ModeSetting var51 = this.mode;
      String[] var52 = new String[]{"Bounce"};
      ModeSetting.ModeCondition var50 = new ModeSetting.ModeCondition(var51, var52);
      this.standUpToPass = new BooleanSetting(
         "StandUpToPass", "Stops gliding (stands up) while Baritone navigates around an obstacle, then automatically takes off again once clear.", var50, true
      );
      ModeSetting var54 = this.mode;
      String[] var55 = new String[]{"Bounce"};
      ModeSetting.ModeCondition var53 = new ModeSetting.ModeCondition(var54, var55);
      this.putOnElytra = new BooleanSetting("PutOnElytra", "Safety net: if the elytra ends up off the chest slot, immediately equip it back.", var53, true);
      ModeSetting var57 = this.mode;
      String[] var58 = new String[]{"Bounce"};
      ModeSetting.ModeCondition var56 = new ModeSetting.ModeCondition(var57, var58);
      this.bypass = new BooleanSetting("Bypass", "Bypass for 2b and other servers (may be)", var56, false);
      BooleanSetting var60 = this.bypass;
      BooleanSetting.BooleanCondition var59 = new BooleanSetting.BooleanCondition(var60, true);
      Float var61 = 3.0F;
      Float var62 = 0.0F;
      Float var63 = 20.0F;
      this.bypassTicks = new NumberSetting("BypassTicks", "How many ticks before/after the recast to blink.", var59, var61, var62, var63);
      ModeSetting var65 = this.mode;
      String[] var66 = new String[]{"ControlRocket"};
      ModeSetting.ModeCondition var64 = new ModeSetting.ModeCondition(var65, var66);
      String[] var67 = new String[]{"Key", "Mouse"};
      this.control = new ModeSetting("Control", "Steering mode: Key (WASD flight + FreeLook camera) or Mouse (fly where camera looks).", var64, "Key", var67);
      ModeSetting var69 = this.mode;
      String[] var70 = new String[]{"ControlRocket"};
      ModeSetting.ModeCondition var68 = new ModeSetting.ModeCondition(var69, var70);
      Float var71 = 25.0F;
      Float var72 = 1.0F;
      Float var73 = 180.0F;
      this.turnSpeed = new NumberSetting("TurnSpeed", "Maximum rotation change per tick.", var68, var71, var72, var73);
      ModeSetting var75 = this.mode;
      String[] var76 = new String[]{"ControlRocket"};
      ModeSetting.ModeCondition var74 = new ModeSetting.ModeCondition(var75, var76);
      Integer var77 = 0;
      Integer var78 = 0;
      Integer var79 = 3;
      this.fireworksDelay = new NumberSetting("FireworksDelay", "Delay between every fire", var74, var77, var78, var79);
      ModeSetting var81 = this.mode;
      String[] var82 = new String[]{"ControlRocket"};
      ModeSetting.ModeCondition var80 = new ModeSetting.ModeCondition(var81, var82);
      String[] var83 = new String[]{"Full", "WithFirework", "None"};
      this.x = new ModeSetting("FlipFlopMode", "How to hold position when not pressing movement keys.", var80, "Full", var83);
      ModeSetting var85 = this.mode;
      String[] var86 = new String[]{"ControlRocket"};
      ModeSetting.ModeCondition var84 = new ModeSetting.ModeCondition(var85, var86);
      this.y = new BooleanSetting("Inventory", "Allows using fireworks from anywhere in the inventory, not just the hotbar.", var84, true);
      ModeSetting var88 = this.mode;
      String[] var89 = new String[]{"ControlRocket"};
      ModeSetting.ModeCondition var87 = new ModeSetting.ModeCondition(var88, var89);
      this.z = new BooleanSetting("GrimV3", "Cycles the elytra out of the chest slot so it never loses durability.", var87, true);
      ModeSetting var91 = this.mode;
      String[] var92 = new String[]{"ControlRocket"};
      ModeSetting.ModeCondition var90 = new ModeSetting.ModeCondition(var91, var92);
      Float var93 = 10.0F;
      Float var94 = 1.0F;
      Float var95 = 18.0F;
      this.grimDelay = new NumberSetting("GrimDelay", "Gliding ticks between each GrimV3 swap-out.", var90, var93, var94, var95);
      ModeSetting var97 = this.mode;
      String[] var98 = new String[]{"ControlRocket"};
      ModeSetting.ModeCondition var96 = new ModeSetting.ModeCondition(var97, var98);
      this.muteElytra = new BooleanSetting("MuteElytra", "Mutes the armor-equip sound when ControlRocket swaps between elytra and chestplate.", var96, true);
      double var99 = -1.0;
      this.M = var99;
      this.T = 99;
      this.V = -1000;
      this.W = new TimerUtil();
      this.Y = false;
      this.Z = false;
      this.aa = false;
      this.ab = false;
      this.ac = 0;
      this.ad = false;
      double var101 = 0.0;
      this.ae = var101;
      double var103 = 0.0;
      this.af = var103;
      this.ag = new ArrayDeque<>();
      this.ah = new ArrayDeque<>();
      this.ai = false;
      this.aj = false;
      this.ak = false;
      this.al = 0;
      this.am = Vec3.ZERO;
      this.an = Vec3.ZERO;
      double var105 = -1.0;
      this.ao = var105;
      this.ap = Vec3.ZERO;
      this.aq = null;
      this.av = -1;
      this.aw = ItemStack.EMPTY;
      this.ax = null;
   }

   public float v() {
      return this.F;
   }

   public float w() {
      return this.G;
   }

   public boolean x() {
      if (mc.player != null && this.E) {
         if (this.isEnabled() && this.mode.getValue().equalsIgnoreCase("ControlRocket")) {
            boolean var1;
            if (!this.control.getValue().equalsIgnoreCase("Key") || !mc.player.isFallFlying() && !this.K()) {
               var1 = false;
            } else {
               var1 = true;
            }

            return var1;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public boolean y() {
      return this.E;
   }

   public void z() {
      this.E = false;
   }

   public void a_void_double_double(double var1, double var3) {
      if (!this.E && mc.player != null) {
         float var5 = mc.player.getYRot();
         this.F = var5;
         float var6 = mc.player.getXRot();
         this.G = var6;
         this.E = true;
      }

      float var7 = this.F;
      double var8 = var1;
      double var10 = 0.15;
      float var12 = (float)(var8 * var10);
      float var13 = var7 + var12;
      this.F = var13;
      float var14 = this.G;
      double var15 = var3;
      double var17 = 0.15;
      float var19 = (float)(var15 * var17);
      float var20 = Mth.clamp(var14 + var19, -90.0F, 90.0F);
      this.G = var20;
   }

   public boolean A() {
      boolean var1;
      if (this.isEnabled() && this.mode.getValue().equalsIgnoreCase("Bounce")) {
         var1 = true;
      } else {
         var1 = false;
      }

      return var1;
   }

   public boolean B() {
      boolean var1;
      if (this.A() && this.autoPitch.getValue() && this.P() && !this.R()) {
         var1 = true;
      } else {
         var1 = false;
      }

      return var1;
   }

   public float C() {
      return this.pitch.getValue().floatValue();
   }

   public boolean D() {
      boolean var1;
      if (!this.isEnabled() || !this.mode.getValue().equalsIgnoreCase("Bounce") || !this.Y && !this.Z) {
         var1 = false;
      } else {
         var1 = true;
      }

      return var1;
   }

   public void E() {
      this.Y = false;
      this.Z = false;
   }

   public boolean F() {
      return false;
   }

   public float G() {
      return 0.0F;
   }

   @Override
   public void onEnable() {
      if (mc.player != null) {
         if (this.mode.getValue().equalsIgnoreCase("Bounce")) {
            this.aw();
            double var1 = mc.player.getY();
            this.ae = var1;
            double var3 = mc.player.getY();
            this.af = var3;
            D = mc.player.getXRot();
            Vec3 var5 = mc.player.position();
            this.am = var5;
            Vec3 var6 = mc.player.position();
            this.an = var6;
            Vec3 var7 = mc.player.position();
            this.ap = var7;
            this.aq = null;
         } else {
            this.V();
            if (this.mode.getValue().equalsIgnoreCase("ControlRocket")) {
               if (this.z.getValue()) {
                  if (mc.player.getItemBySlot(EquipmentSlot.CHEST).getItem() == Items.ELYTRA) {
                     int var10 = this.af();
                     if (var10 != -1) {
                        InventoryUtil.a(var10, 6);
                     }
                  }
               } else if (mc.player.getItemBySlot(EquipmentSlot.CHEST).getItem() != Items.ELYTRA) {
                  int var8 = InventoryUtil.a_int_Item(Items.ELYTRA);
                  if (var8 == -1) {
                     String var9 = this.getName();
                     Night.chatManager.sendTaggedMessage("No elytra found in your inventory.", var9);
                     this.setEnabled(false);
                  } else {
                     InventoryUtil.a(var8, 6);
                  }
               }
            }
         }
      }
   }

   @Override
   public void onDisable() {
      if (this.mode.getValue().equalsIgnoreCase("Bounce")) {
         this.aw();
         if (this.aq != null) {
            this.ap();
         }

         this.aq = null;
         this.av();
         C = false;
         if (mc.player != null && mc.player.onGround()) {
            if (mc.player.getPose() == Pose.FALL_FLYING || mc.player.getPose() == Pose.SWIMMING) {
               mc.player.setPose(Pose.STANDING);
            }

            EntityFlagAccessor var1 = (EntityFlagAccessor)mc.player;
            int var2 = EntityFlagAccessor.getFlagFallFlying();
            var1.invokeSetSharedFlag(var2, false);
         }
      } else {
         if (mc.player != null && this.E) {
            LocalPlayer var3 = mc.player;
            float var4 = this.F;
            var3.setYRot(var4);
            LocalPlayer var5 = mc.player;
            float var6 = this.G;
            var5.setXRot(var6);
         }

         this.V();
         this.S = 0;
         this.Q = false;
         this.R = false;
         this.av = -1;
         this.ax = null;
         this.aw = ItemStack.EMPTY;
         if (mc.player != null && mc.player.onGround()) {
            if (mc.player.getPose() == Pose.FALL_FLYING || mc.player.getPose() == Pose.SWIMMING) {
               mc.player.setPose(Pose.STANDING);
            }

            ((EntityAccessor)mc.player).invokeSetSharedFlag(7, false);
         }

         if (mc.player != null) {
            mc.player.getAbilities().flying = false;
            mc.player.getAbilities().setFlyingSpeed(0.05F);
         }
      }
   }

   @EventTarget
   public void a_void_InputUpdateEvent(InputUpdateEvent var1) {
      if (this.isEnabled() && this.mode.getValue().equalsIgnoreCase("Bounce")) {
         if (this.P() || this.Z) {
            this.Z = false;
            if (!this.R()) {
               var1.jumping = true;
            }
         }
      }
   }

   private void V() {
      this.H = false;
      this.I = 0;
      long var1 = 0L;
      this.L = var1;
      double var3 = -1.0;
      this.M = var3;
      this.E = false;
   }

   @EventTarget
   public void a_void_PushOutOfBlocksEvent(PushOutOfBlocksEvent var1) {
      if (mc.player != null && mc.level != null && mc.player.isFallFlying()) {
         if (this.mode.getValue().equalsIgnoreCase("Control")) {
            var1.setCancelled(true);
            if (mc.player.input.getMoveVector().y == 0.0F && mc.player.input.getMoveVector().x == 0.0F) {
               LocalPlayer var2 = mc.player;
               double var3 = 0.0;
               double var5 = mc.player.getDeltaMovement().y;
               double var7 = 0.0;
               var2.setDeltaMovement(new Vec3(var3, var5, var7));
            } else {
               this.X = 12.0F;
               double var9 = Math.cos(Math.toRadians(mc.player.getYRot() + 90.0F));
               double var11 = Math.sin(Math.toRadians(mc.player.getYRot() + 90.0F));
               LocalPlayer var13 = mc.player;
               double var14 = mc.player.input.getMoveVector().y;
               double var16 = this.horizontal.getValue().doubleValue();
               double var18 = var14 * var16;
               double var20 = var9;
               double var22 = var18 * var20;
               double var24 = mc.player.input.getMoveVector().x;
               double var26 = this.horizontal.getValue().doubleValue();
               double var28 = var24 * var26;
               double var30 = var11;
               double var32 = var28 * var30;
               double var34 = var22 + var32;
               double var36 = mc.player.getDeltaMovement().y;
               double var38 = mc.player.input.getMoveVector().y;
               double var40 = this.horizontal.getValue().doubleValue();
               double var42 = var38 * var40;
               double var44 = var11;
               double var46 = var42 * var44;
               double var48 = mc.player.input.getMoveVector().x;
               double var50 = this.horizontal.getValue().doubleValue();
               double var52 = var48 * var50;
               double var54 = var9;
               double var56 = var52 * var54;
               double var58 = var46 - var56;
               var13.setDeltaMovement(new Vec3(var34, var36, var58));
            }

            LocalPlayer var60 = mc.player;
            double var61 = mc.player.getDeltaMovement().x;
            double var63 = 0.0;
            double var65 = mc.player.getDeltaMovement().z;
            var60.setDeltaMovement(new Vec3(var61, var63, var65));
            boolean var67 = ((FreecamAccess)Night.moduleManager.getModuleByName("Freecam")).isEnabled();
            if (this.moveVertically.getValue() && !var67) {
               if (mc.options.keyJump.isDown()) {
                  LocalPlayer var68 = mc.player;
                  double var69 = mc.player.getDeltaMovement().x;
                  double var71 = this.vertical.getValue().doubleValue();
                  double var73 = mc.player.getDeltaMovement().z;
                  var68.setDeltaMovement(new Vec3(var69, var71, var73));
                  this.X = -51.0F;
               } else if (mc.options.keyShift.isDown()) {
                  LocalPlayer var75 = mc.player;
                  double var76 = mc.player.getDeltaMovement().x;
                  double var78 = -this.vertical.getValue().doubleValue();
                  double var80 = mc.player.getDeltaMovement().z;
                  var75.setDeltaMovement(new Vec3(var76, var78, var80));
                  this.X = 0.0F;
               }
            }
         }
      }
   }

   @EventTarget
   public void a_void_PacketReceiveEvent(PacketReceiveEvent var1) {
      if (mc.player != null && mc.level != null) {
         if (this.mode.getValue().equalsIgnoreCase("Bounce")) {
            if (var1.getPacket() instanceof ClientboundPingPacket var3) {
               boolean var4;
               if ((!this.fakeLag.getValue() || !this.Q() || !this.as()) && (!this.bypass.getValue() || this.al <= 0)) {
                  var4 = false;
               } else {
                  var4 = true;
               }

               if (var4) {
                  Deque var23 = this.ah;
                  ClientboundPingPacket var24 = var3;
                  var23.add(var24);
                  var1.setCancelled(true);
               }

               return;
            }

            if (var1.getPacket() instanceof ClientboundPlayerPositionPacket) {
               int var5 = this.flagPause.getValue().intValue();
               this.ac = var5;
               this.Y = false;
               this.aa = false;
               this.ab = false;
               if (this.ax()) {
                  this.b_void_String("FLAG PAUSE: server corrected position, holding for " + this.ac + " ticks");
               }

               if (this.passObstacles.getValue() && BaritoneUtils.isAvailable()) {
                  Vec3 var6 = this.an();
                  Vec3 var7 = mc.player.position();
                  Vec3 var8 = var6;
                  Vec3 var9 = this.a_Vec3_Vec3_Vec3(var7, var8);
                  Vec3 var10 = var6;
                  mc.execute(() -> {
                     if (mc.player != null) {
                        double var3x = this.obstacleLookAhead.getValue().doubleValue();
                        Vec3 var5x = var10.scale(var3x);
                        Vec3 var6x = var9.add(var5x);
                        this.a_void_Vec3_Vec3_bool(var6x, var10, false);
                     }
                  });
               }
            }
         }

         boolean var11 = this.ad();
         if (this.mode.getValue().equalsIgnoreCase("ControlRocket") && this.muteElytra.getValue() || var11) {
            TimerUtil var12 = this.W;
            Integer var13 = 500;
            if (!var12.passedMs(var13)) {
               Packet var14 = var1.getPacket();
               if (var14 instanceof ClientboundSoundPacket
                  && ((SoundEvent)((ClientboundSoundPacket)var14).getSound().value()).location().getPath().startsWith("item.armor.equip")) {
                  var1.setCancelled(true);
               }
            }
         }

         if (this.mode.getValue().equalsIgnoreCase("Control")
            && var1.getPacket() instanceof ServerboundMovePlayerPacket var16
            && var16.hasRotation()
            && mc.player.isFallFlying()) {
            if (mc.options.keyLeft.isDown()) {
               PlayerMoveC2SPacketAccessor var17 = (PlayerMoveC2SPacketAccessor)var16;
               float var18 = var16.getYRot(0.0F) - 90.0F;
               var17.setYaw(var18);
            }

            if (mc.options.keyRight.isDown()) {
               PlayerMoveC2SPacketAccessor var19 = (PlayerMoveC2SPacketAccessor)var16;
               float var20 = var16.getYRot(0.0F) + 90.0F;
               var19.setYaw(var20);
            }

            PlayerMoveC2SPacketAccessor var21 = (PlayerMoveC2SPacketAccessor)var16;
            float var22 = this.X;
            var21.setPitch(var22);
         }
      }
   }

   @EventTarget
   public void a_void_PacketSendEvent(PacketSendEvent var1) {
      if (mc.player != null) {
         if (this.mode.getValue().equalsIgnoreCase("Bounce")) {
            if (!this.ak) {
               if (var1.getPacket() != null) {
                  if (var1.getPacket().getClass().getSimpleName().startsWith("Serverbound")) {
                     boolean var2;
                     if (this.fakeLag.getValue() && this.Q() && this.as()) {
                        var2 = true;
                     } else {
                        var2 = false;
                     }

                     boolean var3 = var2;
                     boolean var4;
                     if (!this.bypass.getValue()
                        || this.al <= 0
                        || !(var1.getPacket() instanceof ServerboundMovePlayerPacket) && !(var1.getPacket() instanceof ServerboundPongPacket)) {
                        var4 = false;
                     } else {
                        var4 = true;
                     }

                     boolean var5 = var4;
                     boolean var6;
                     if (!var3 && !var5) {
                        var6 = false;
                     } else {
                        var6 = true;
                     }

                     if (var6) {
                        Deque var7 = this.ag;
                        Packet var8 = var1.getPacket();
                        var7.add(var8);
                        var1.setCancelled(true);
                     } else {
                        this.av();
                     }
                  }
               }
            }
         }
      }
   }

   @EventTarget
   public void a_void_UpdateWalkingPlayerEvent(UpdateWalkingPlayerEvent var1) {
      if (mc.player != null && mc.level != null) {
         if (this.mode.getValue().equalsIgnoreCase("Bounce")) {
            if (this.putOnElytra.getValue()) {
               this.ah();
            }

            this.ag();
         } else if (this.mode.getValue().equalsIgnoreCase("ControlRocket")) {
            this.W();
         }
      }
   }

   private void W() {
      if (!this.z.getValue() && mc.player.getItemBySlot(EquipmentSlot.CHEST).getItem() != Items.ELYTRA) {
         int var1 = InventoryUtil.a_int_Item(Items.ELYTRA);
         if (var1 != -1) {
            InventoryUtil.a(var1, 6);
            if (!mc.player.onGround()) {
               ClientPacketListener var2 = mc.getConnection();
               LocalPlayer var3 = mc.player;
               var2.send(new ServerboundPlayerCommandPacket(var3, Action.START_FALL_FLYING));
            }
         }
      } else {
         boolean var4 = mc.options.keyJump.isDown();
         if (var4 && !this.O) {
            boolean var13;
            label69: {
               if (!this.control.getValue().equalsIgnoreCase("Key")) {
                  long var5 = System.currentTimeMillis();
                  long var7 = this.P;
                  long var9 = var5 - var7;
                  long var11 = 300L;
                  if (var9 > var11) {
                     var13 = false;
                     break label69;
                  }
               }

               var13 = true;
            }

            if (var13 && !mc.player.isFallFlying()) {
               this.Q = true;
               Vec3 var14 = mc.player.getDeltaMovement();
               LocalPlayer var15 = mc.player;
               double var16 = var14.x;
               double var18 = var14.y;
               double var20 = 0.45;
               double var22 = Math.max(var18, var20);
               double var24 = var14.z;
               var15.setDeltaMovement(var16, var22, var24);
            }

            long var26 = System.currentTimeMillis();
            this.P = var26;
         }

         boolean var28 = var4;
         this.O = var28;
         if (mc.player.onGround() && !this.Q) {
            boolean var29 = this.R;
            this.R = false;
            this.S = 0;
            if (var29 && this.E) {
               LocalPlayer var30 = mc.player;
               float var31 = this.F;
               var30.setYRot(var31);
               LocalPlayer var32 = mc.player;
               float var33 = this.G;
               var32.setXRot(var33);
            }

            this.V();
            if (mc.player.getPose() == Pose.FALL_FLYING || mc.player.getPose() == Pose.SWIMMING) {
               mc.player.setPose(Pose.STANDING);
            }

            ((EntityAccessor)mc.player).invokeSetSharedFlag(7, false);
         }

         if (mc.player.isFallFlying()) {
            this.Q = false;
         } else {
            label54:
            if (!mc.player.onGround()) {
               if (!this.Q) {
                  double var34 = mc.player.getDeltaMovement().y;
                  double var36 = 0.0;
                  if (!(var34 < var36)) {
                     break label54;
                  }
               }

               this.J();
               this.Q = false;
            }
         }

         this.ae();
         if (!mc.player.isFallFlying()) {
            this.V();
         } else {
            this.X();
         }
      }
   }

   private void X() {
      if (this.control.getValue().equalsIgnoreCase("Key")) {
         if (!this.E) {
            float var1 = mc.player.getYRot();
            this.F = var1;
            float var2 = mc.player.getXRot();
            this.G = var2;
            this.E = true;
         }

         Vec3 var3 = this.Y();
         boolean var4 = this.H;
         double var5 = var3.lengthSqr();
         double var7 = 1.0E-4;
         boolean var9;
         if (var5 < var7) {
            var9 = true;
         } else {
            var9 = false;
         }

         this.H = var9;
         if (this.H && !var4) {
            float var10 = mc.player.getYRot();
            this.J = var10;
            this.K = false;
         }

         boolean var11 = true;
         if (this.H) {
            boolean var12 = this.Z();

            var11 = switch (this.x.getValue()) {
               case "Full" -> true;
               case "WithFirework" -> var12;
               case "None" -> false;
               default -> true;
            };
         }

         int var16;
         if (this.H) {
            var16 = this.I + 1;
         } else {
            var16 = 0;
         }

         this.I = var16;
         if (!this.H || var11) {
            this.I();
         }

         if (this.H) {
            if (var11) {
               float var17;
               if (this.K) {
                  var17 = this.J + 180.0F;
               } else {
                  var17 = this.J;
               }

               float var18 = var17;
               this.b_void_float_float(var18, 0.0F);
               boolean var19;
               if (!this.K) {
                  var19 = true;
               } else {
                  var19 = false;
               }

               this.K = var19;
            }
         } else {
            if (var4 && !this.Z()) {
               mc.player.setDeltaMovement(Vec3.ZERO);
            }

            Vec3 var20 = mc.player.getEyePosition();
            double var21 = var20.x;
            double var23 = var3.x;
            double var25 = var21 + var23;
            double var27 = var20.y;
            double var29 = var3.y;
            double var31 = var27 + var29;
            double var33 = var20.z;
            double var35 = var3.z;
            double var37 = var33 + var35;
            float[] var39 = RotationMathUtil.a(var25, var31, var37);
            float var40 = var39[0];
            float var41 = var39[1];
            this.b_void_float_float(var40, var41);
         }
      } else {
         this.E = false;
         float var42 = mc.player.getYRot();
         float var43 = mc.player.getXRot();
         float var44 = var42;
         float var45 = var43;
         this.b_void_float_float(var44, var45);
         this.I();
      }
   }

   private Vec3 Y() {
      float var1;
      if (this.E) {
         var1 = this.F;
      } else {
         var1 = mc.player.getYRot();
      }

      float var2 = var1;
      Vec3 var3 = Vec3.ZERO;
      if (mc.options.keyUp.isDown()) {
         Vec3 var4 = var3;
         Vec3 var5 = a_Vec3_float(var2);
         var3 = var4.add(var5);
      }

      if (mc.options.keyDown.isDown()) {
         Vec3 var6 = var3;
         Vec3 var7 = a_Vec3_float(var2 + 180.0F);
         var3 = var6.add(var7);
      }

      if (mc.options.keyLeft.isDown()) {
         Vec3 var8 = var3;
         Vec3 var9 = a_Vec3_float(var2 - 90.0F);
         var3 = var8.add(var9);
      }

      if (mc.options.keyRight.isDown()) {
         Vec3 var10 = var3;
         Vec3 var11 = a_Vec3_float(var2 + 90.0F);
         var3 = var10.add(var11);
      }

      if (mc.options.keyJump.isDown()) {
         Vec3 var12 = var3;
         double var13 = 0.0;
         double var15 = 1.0;
         double var17 = 0.0;
         var3 = var12.add(var13, var15, var17);
      }

      if (mc.options.keyShift.isDown()) {
         Vec3 var19 = var3;
         double var20 = 0.0;
         double var22 = -1.0;
         double var24 = 0.0;
         var3 = var19.add(var20, var22, var24);
      }

      return var3;
   }

   private static Vec3 a_Vec3_float(float var0) {
      double var1 = Math.toRadians(var0);
      double var3 = -Math.sin(var1);
      double var5 = 0.0;
      double var7 = Math.cos(var1);
      return new Vec3(var3, var5, var7);
   }

   private void b_void_float_float(float var1, float var2) {
      float var3 = this.turnSpeed.getValue().floatValue();
      float var4 = mc.player.getYRot();
      float var5 = mc.player.getXRot();
      float var6 = var4;
      float var7 = Mth.wrapDegrees(var1 - var6);
      float var8 = var3;
      float var9 = 0.0F - var8;
      float var10 = var3;
      float var11 = Mth.clamp(var7, var9, var10);
      float var12 = var5;
      float var13 = var2 - var12;
      float var14 = var3;
      float var15 = 0.0F - var14;
      float var16 = var3;
      float var17 = Mth.clamp(var13, var15, var16);
      LocalPlayer var18 = mc.player;
      float var19 = var4;
      float var20 = var11;
      float var21 = var19 + var20;
      var18.setYRot(var21);
      LocalPlayer var22 = mc.player;
      float var23 = var5;
      float var24 = var17;
      float var25 = var23 + var24;
      var22.setXRot(var25);
      LocalPlayer var26 = mc.player;
      float var27 = var4;
      float var28 = var11;
      float var29 = var27 + var28;
      var26.setYBodyRot(var29);
      LocalPlayer var30 = mc.player;
      float var31 = var4;
      float var32 = var11;
      float var33 = var31 + var32;
      var30.setYHeadRot(var33);
   }

   private boolean Z() {
      if (mc.player == null) {
         return false;
      }

      if (!mc.player.getOffhandItem().is(Items.FIREWORK_ROCKET) && !mc.player.getMainHandItem().is(Items.FIREWORK_ROCKET)) {
         for (int var1 = 0; var1 <= 8; var1++) {
            Inventory var2 = mc.player.getInventory();
            int var3 = var1;
            if (var2.getItem(var3).is(Items.FIREWORK_ROCKET)) {
               return true;
            }
         }

         if (this.y.getValue()) {
            for (int var4 = 9; var4 <= 35; var4++) {
               Inventory var5 = mc.player.getInventory();
               int var6 = var4;
               if (var5.getItem(var6).is(Items.FIREWORK_ROCKET)) {
                  return true;
               }
            }
         }

         return false;
      } else {
         return true;
      }
   }

   private int aa() {
      if (mc.player == null) {
         return -1;
      }

      if (mc.player.getMainHandItem().is(Items.FIREWORK_ROCKET)) {
         return mc.player.getInventory().getSelectedSlot();
      }

      for (int var1 = 0; var1 <= 8; var1++) {
         Inventory var2 = mc.player.getInventory();
         int var3 = var1;
         if (var2.getItem(var3).is(Items.FIREWORK_ROCKET)) {
            return var1;
         }
      }

      if (this.y.getValue()) {
         for (int var4 = 9; var4 <= 35; var4++) {
            Inventory var5 = mc.player.getInventory();
            int var6 = var4;
            if (var5.getItem(var6).is(Items.FIREWORK_ROCKET)) {
               return var4;
            }
         }
      }

      return -1;
   }

   public void H() {
      long var1 = 0L;
      this.L = var1;
      double var3 = -1.0;
      this.M = var3;
   }

   public void I() {
      if (mc.player != null) {
         float var1 = mc.player.getYRot();
         float var2 = mc.player.getXRot();
         this.a_void_float_float(var1, var2);
      }
   }

   public void a_void_float_float(float var1, float var2) {
      if (mc.player != null && mc.getConnection() != null) {
         if (!mc.player.isUsingItem() || mc.player.getUsedItemHand() != InteractionHand.MAIN_HAND) {
            long var3 = System.currentTimeMillis();
            long var5 = this.L;
            double var7 = var3 - var5;
            double var9 = 1000.0;
            double var11 = var7 / var9;
            double var13 = this.M;
            double var15 = 0.0;
            double var17;
            if (var13 < var15) {
               var17 = 0.0;
            } else {
               double var19 = this.M;
               double var21 = this.fireworksDelay.getValue().floatValue();
               var17 = var19 + var21;
            }

            double var23 = var17;
            long var25 = this.L;
            long var27 = 0L;
            if (var25 != var27) {
               double var29 = var11;
               double var31 = var23;
               if (var29 < var31 && this.ab()) {
                  return;
               }
            }

            if (mc.player.getOffhandItem().is(Items.FIREWORK_ROCKET)) {
               Fireworks var33 = (Fireworks)mc.player.getOffhandItem().get(DataComponents.FIREWORKS);
               int var34;
               if (var33 != null) {
                  var34 = var33.flightDuration();
               } else {
                  var34 = 1;
               }

               double var35 = (byte)var34;
               double var37 = 0.5;
               double var39 = var35 * var37;
               double var41 = 0.5;
               double var43 = var39 + var41;
               this.M = var43;
               if (this.ac()) {
                  PacketPredictionUtil.a_void_SequencedPacketCreator(var2x -> new ServerboundUseItemPacket(InteractionHand.OFF_HAND, var2x, var1, var2));
                  long var45 = System.currentTimeMillis();
                  this.L = var45;
               }
            } else {
               int var47 = this.aa();
               if (var47 != -1) {
                  Inventory var48 = mc.player.getInventory();
                  int var49 = var47;
                  Fireworks var50 = (Fireworks)var48.getItem(var49).get(DataComponents.FIREWORKS);
                  int var51;
                  if (var50 != null) {
                     var51 = var50.flightDuration();
                  } else {
                     var51 = 1;
                  }

                  double var52 = var51;
                  double var54 = 0.5;
                  double var56 = var52 * var54;
                  double var58 = 0.5;
                  double var60 = var56 + var58;
                  this.M = var60;
                  if (var47 <= 8 || this.y.getValue()) {
                     if (this.ac()) {
                        int var62 = mc.player.getInventory().getSelectedSlot();
                        if (var47 >= 0 && var47 <= 8) {
                           int var63 = var47;
                           int var64 = var62;
                           if (InventoryUtil.a_bool_String_int_int("Silent", var63, var64)) {
                              PacketPredictionUtil.a_void_SequencedPacketCreator(
                                 var2x -> new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, var2x, var1, var2)
                              );
                              int var65 = var47;
                              int var66 = var62;
                              InventoryUtil.b("Silent", var65, var66);
                              long var67 = System.currentTimeMillis();
                              this.L = var67;
                           }
                        } else if (this.y.getValue()) {
                           int var69 = mc.player.getInventory().getSelectedSlot();
                           int var70 = var47;
                           int var71 = var69;
                           InventoryUtil.c("Swap", var70, var71);
                           PacketPredictionUtil.a_void_SequencedPacketCreator(
                              var2x -> new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, var2x, var1, var2)
                           );
                           int var72 = var47;
                           int var73 = var69;
                           InventoryUtil.c("Swap", var72, var73);
                           long var74 = System.currentTimeMillis();
                           this.L = var74;
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private boolean ab() {
      if (mc.player != null && mc.level != null) {
         ClientLevel var1 = mc.level;
         LocalPlayer var2 = mc.player;
         AABB var3 = mc.player.getBoundingBox();
         double var4 = 2.0;
         AABB var6 = var3.inflate(var4);
         boolean var7;
         if (!var1.getEntities(var2, var6, var0 -> var0 instanceof FireworkRocketEntity).isEmpty()) {
            var7 = true;
         } else {
            var7 = false;
         }

         return var7;
      } else {
         return false;
      }
   }

   private boolean ac() {
      return this.z.getValue() && mc.player.getItemBySlot(EquipmentSlot.CHEST).getItem() != Items.ELYTRA ? this.J() : true;
   }

   private boolean ad() {
      AutoMace var1 = Night.moduleManager.getModule(AutoMace.class);
      boolean var2;
      if (var1 != null && var1.isEnabled() && var1.fakeFly.getValue()) {
         var2 = true;
      } else {
         var2 = false;
      }

      return var2;
   }

   private void ae() {
      if (this.z.getValue() && !mc.player.onGround()) {
         int var1 = this.S;
         int var2 = var1 + 1;
         this.S = var2;
         int var3 = this.grimDelay.getValue().intValue();
         if (var1 >= var3) {
            this.S = 0;
            this.J();
         }
      }
   }

   public boolean J() {
      return this.b_bool_bool(false);
   }

   public boolean b_bool_bool(boolean var1) {
      this.R = true;
      if (mc.player == null) {
         return false;
      }

      if (mc.player.getItemBySlot(EquipmentSlot.CHEST).getItem() == Items.ELYTRA) {
         ClientPacketListener var2 = mc.getConnection();
         LocalPlayer var3 = mc.player;
         var2.send(new ServerboundPlayerCommandPacket(var3, Action.START_FALL_FLYING));
         return true;
      }

      int var4 = mc.player.tickCount;
      int var5 = this.V;
      if (var4 >= var5) {
         int var6 = var4;
         int var7 = this.V;
         if (var6 - var7 < 2) {
            return false;
         }
      }

      int var8 = InventoryUtil.a_int_Item(Items.ELYTRA);
      if (var8 == -1) {
         return false;
      }

      int var9 = var4;
      this.V = var9;
      Inventory var10 = mc.player.getInventory();
      int var11 = var8;
      ItemStack var12 = var10.getItem(var11).copy();
      ItemStack var13 = mc.player.getItemBySlot(EquipmentSlot.CHEST).copy();
      int var14 = var8;
      this.av = var14;
      ItemStack var15 = var12;
      this.ax = var15;
      ItemStack var16 = var13;
      this.aw = var16;
      this.W.reset();
      InventoryUtil.a(var8, 6);
      ClientPacketListener var17 = mc.getConnection();
      LocalPlayer var18 = mc.player;
      var17.send(new ServerboundPlayerCommandPacket(var18, Action.START_FALL_FLYING));
      InventoryUtil.a(var8, 6);
      this.av = -1;
      this.ax = null;
      this.aw = ItemStack.EMPTY;
      return true;
   }

   public boolean K() {
      boolean var1;
      if (this.isEnabled()
         && this.mode.getValue().equalsIgnoreCase("ControlRocket")
         && this.z.getValue()
         && this.R
         && mc.player != null
         && !mc.player.onGround()) {
         var1 = true;
      } else {
         var1 = false;
      }

      return var1;
   }

   private int af() {
      for (int var1 = InventoryUtil.c; var1 <= InventoryUtil.f; var1++) {
         Inventory var2 = mc.player.getInventory();
         int var3 = var1;
         ItemStack var4 = var2.getItem(var3);
         if (var4.getItem() != Items.ELYTRA) {
            Equippable var5 = (Equippable)var4.get(DataComponents.EQUIPPABLE);
            if (var5 != null && var5.slot() == EquipmentSlot.CHEST) {
               return var1;
            }
         }
      }

      return -1;
   }

   public int L() {
      return this.av;
   }

   public ItemStack M() {
      return this.aw;
   }

   public ItemStack N() {
      return this.ax;
   }

   @EventTarget
   public void a_void_Post(MotionSyncEvent.Post var1) {
      if (mc.player != null && mc.getConnection() != null) {
         if (this.mode.getValue().equalsIgnoreCase("Bounce")) {
            Vec3 var2 = this.an;
            this.am = var2;
            Vec3 var3 = mc.player.position();
            this.an = var3;
            if (this.ax()) {
               double var4 = this.ao;
               double var6 = 0.0;
               if (var4 >= var6) {
                  double var8 = this.ar();
                  double var10 = this.ao;
                  double var12 = var8;
                  double var14 = var10 - var12;
                  boolean var16 = mc.player.horizontalCollision;
                  if (!var16) {
                     double var17 = this.ao;
                     double var19 = 5.0;
                     if (var17 > var19) {
                        double var21 = var14;
                        double var23 = this.ao;
                        double var25 = 0.25;
                        double var27 = var23 * var25;
                        if (var21 > var27) {
                           Object[] var29 = new Object[11];
                           double var30 = this.ao;
                           double var32 = 3.6;
                           Double var34 = var30 * var32;
                           var29[0] = var34;
                           double var35 = var8;
                           double var37 = 3.6;
                           Double var39 = var35 * var37;
                           var29[1] = var39;
                           double var40 = var14;
                           double var42 = this.ao;
                           double var44 = var40 / var42;
                           double var46 = 100.0;
                           Double var48 = var44 * var46;
                           var29[2] = var48;
                           Boolean var49 = mc.player.onGround();
                           var29[3] = var49;
                           Boolean var50 = var16;
                           var29[4] = var50;
                           Boolean var51 = this.P();
                           var29[5] = var51;
                           Boolean var52 = mc.player.isFallFlying();
                           var29[6] = var52;
                           Integer var53 = this.ac;
                           var29[7] = var53;
                           Double var54 = mc.player.getX();
                           var29[8] = var54;
                           Double var55 = mc.player.getY();
                           var29[9] = var55;
                           Double var56 = mc.player.getZ();
                           var29[10] = var56;
                           String var57 = String.format(
                              "SPEED DROP: %.1f -> %.1f km/h (-%.0f%%) onGround=%b horizontalCollision=%b gliding=%b fallFlying=%b flagPauseTicksLeft=%d pos=%.2f,%.2f,%.2f",
                              var29
                           );
                           this.b_void_String(var57);
                        }
                     }
                  }

                  double var58 = var8;
                  this.ao = var58;
                  return;
               }
            }

            if (this.ax()) {
               double var60 = this.ar();
               this.ao = var60;
            } else {
               double var62 = -1.0;
               this.ao = var62;
            }
         }
      }
   }

   private void ag() {
      if (mc.player != null && mc.level != null && mc.getConnection() != null) {
         if (!this.U()) {
            this.aw();
            this.av();
         } else {
            if (this.bypass.getValue()) {
               if (this.al > 0) {
                  int var1 = this.al - 1;
                  this.al = var1;
               }

               if (this.at()) {
                  int var2 = this.al;
                  int var3 = this.bypassTicks.getValue().intValue();
                  int var4 = Math.max(var2, var3);
                  this.al = var4;
               }
            }

            if (this.aq()) {
               this.Y = false;
            } else if (this.ac > 0) {
               int var5 = this.ac - 1;
               this.ac = var5;
            } else if (!this.R()) {
               if (!this.P()) {
                  if (this.takeoff.getValue() && this.S()) {
                     if (this.T()) {
                        this.O();
                     } else {
                        double var9 = Math.toRadians(mc.player.getYRot());
                        double var11 = -Math.cos(var9);
                        double var13 = -Math.sin(var9);
                        Vec3 var15 = mc.player.getDeltaMovement();
                        double var16 = var15.x;
                        double var18 = var11;
                        double var20 = var16 * var18;
                        double var22 = var15.z;
                        double var24 = var13;
                        double var26 = var22 * var24;
                        double var28 = Math.abs(var20 + var26);
                        double var30 = 0.001;
                        if (var28 >= var30) {
                           return;
                        }

                        this.Z = true;
                     }
                  }
               } else {
                  EntityFlagAccessor var6 = (EntityFlagAccessor)mc.player;
                  int var7 = EntityFlagAccessor.getFlagFallFlying();
                  boolean var8 = var6.invokeGetSharedFlag(var7);
                  if (!this.minimizePackets.getValue() || !var8) {
                     this.O();
                  }
               }
            }
         }
      }
   }

   public void O() {
      if (mc.player != null && mc.getConnection() != null) {
         EntityFlagAccessor var1 = (EntityFlagAccessor)mc.player;
         int var2 = EntityFlagAccessor.getFlagFallFlying();
         boolean var3 = var1.invokeGetSharedFlag(var2);
         EntityFlagAccessor var4 = (EntityFlagAccessor)mc.player;
         int var5 = EntityFlagAccessor.getFlagFallFlying();
         var4.invokeSetSharedFlag(var5, true);
         ClientPacketListener var6 = mc.getConnection();
         LocalPlayer var7 = mc.player;
         var6.send(new ServerboundPlayerCommandPacket(var7, Action.START_FALL_FLYING));
         if (this.bypass.getValue()) {
            int var8 = this.bypassTicks.getValue().intValue();
            this.al = var8;
         }

         if (this.ax()) {
            double var9 = this.ar();
            Object[] var11 = new Object[5];
            Double var12 = var9;
            var11[0] = var12;
            double var13 = var9;
            double var15 = 3.6;
            Double var17 = var13 * var15;
            var11[1] = var17;
            Boolean var18 = mc.player.onGround();
            var11[2] = var18;
            Boolean var19 = var3;
            var11[3] = var19;
            Integer var20 = this.ac;
            var11[4] = var20;
            String var21 = String.format("START FLY: %.2f m/s (%.1f km/h) onGround=%b rawFlying=%b flagPauseTicksLeft=%d", var11);
            this.b_void_String(var21);
         }
      }
   }

   private void ah() {
      if (this.putOnElytra.getValue()) {
         if (mc.gameMode != null && mc.player != null && mc.getConnection() != null) {
            AbstractContainerMenu var1 = mc.player.containerMenu;
            InventoryMenu var2 = mc.player.inventoryMenu;
            if (var1 == var2 && mc.player.inventoryMenu.getCarried().getItem() == Items.ELYTRA) {
               this.ai();
            } else {
               if (mc.player.getItemBySlot(EquipmentSlot.CHEST).getItem() != Items.ELYTRA) {
                  int var3 = this.aj();
                  if (var3 != -1) {
                     int var4 = var3;
                     this.b_void_int(var4);
                  }
               }
            }
         }
      }
   }

   private void ai() {
      int var1 = mc.player.inventoryMenu.containerId;
      MultiPlayerGameMode var2 = mc.gameMode;
      int var3 = var1;
      LocalPlayer var4 = mc.player;
      var2.handleContainerInput(var3, 6, 0, ContainerInput.PICKUP, var4);
      if (mc.player.inventoryMenu.getCarried().getItem() == Items.ELYTRA) {
         int var5 = this.ak();
         if (var5 != -1) {
            MultiPlayerGameMode var6 = mc.gameMode;
            int var7 = var1;
            int var8 = var5;
            LocalPlayer var9 = mc.player;
            var6.handleContainerInput(var7, var8, 0, ContainerInput.PICKUP, var9);
         } else {
            int var10 = this.al();
            if (var10 != -1) {
               MultiPlayerGameMode var11 = mc.gameMode;
               int var12 = var1;
               int var13 = var10;
               LocalPlayer var14 = mc.player;
               var11.handleContainerInput(var12, var13, 0, ContainerInput.PICKUP, var14);
            }
         }
      }
   }

   private void b_void_int(int var1) {
      int var2 = mc.player.inventoryMenu.containerId;
      MultiPlayerGameMode var3 = mc.gameMode;
      int var4 = var2;
      LocalPlayer var5 = mc.player;
      var3.handleContainerInput(var4, var1, 0, ContainerInput.PICKUP, var5);
      MultiPlayerGameMode var6 = mc.gameMode;
      int var7 = var2;
      LocalPlayer var8 = mc.player;
      var6.handleContainerInput(var7, 6, 0, ContainerInput.PICKUP, var8);
      MultiPlayerGameMode var9 = mc.gameMode;
      int var10 = var2;
      LocalPlayer var11 = mc.player;
      var9.handleContainerInput(var10, var1, 0, ContainerInput.PICKUP, var11);
   }

   private int aj() {
      for (int var1 = 9; var1 <= 44; var1++) {
         InventoryMenu var2 = mc.player.inventoryMenu;
         int var3 = var1;
         if (var2.getSlot(var3).getItem().getItem() == Items.ELYTRA) {
            return var1;
         }
      }

      return -1;
   }

   private int ak() {
      for (int var1 = 9; var1 <= 44; var1++) {
         InventoryMenu var2 = mc.player.inventoryMenu;
         int var3 = var1;
         if (var2.getSlot(var3).getItem().isEmpty()) {
            return var1;
         }
      }

      return -1;
   }

   private int al() {
      for (int var1 = 9; var1 <= 44; var1++) {
         InventoryMenu var2 = mc.player.inventoryMenu;
         int var3 = var1;
         if (!var2.getSlot(var3).getItem().isEmpty()) {
            return var1;
         }
      }

      return -1;
   }

   public boolean P() {
      if (mc.player == null) {
         return false;
      }

      EntityFlagAccessor var1 = (EntityFlagAccessor)mc.player;
      int var2 = EntityFlagAccessor.getFlagFallFlying();
      boolean var3 = var1.invokeGetSharedFlag(var2);
      if (this.ad && !this.R()) {
         return true;
      }

      boolean var4 = var3;
      this.ad = var4;
      return var3;
   }

   public boolean Q() {
      return this.P();
   }

   public boolean R() {
      boolean var1;
      if (this.ac <= 0 && !BaritoneUtils.isActive()) {
         var1 = false;
      } else {
         var1 = true;
      }

      return var1;
   }

   public boolean S() {
      if (mc.player == null) {
         return false;
      }

      boolean var1;
      if ((mc.player.onGround() || !this.P())
         && !mc.player.getAbilities().flying
         && !mc.player.onClimbable()
         && !mc.player.isInWater()
         && !mc.player.isPassenger()
         && !mc.player.hasEffect(MobEffects.LEVITATION)
         && this.am()) {
         var1 = true;
      } else {
         var1 = false;
      }

      return var1;
   }

   public boolean T() {
      if (mc.player == null) {
         return false;
      }

      boolean var1;
      if (!this.P() && !mc.player.onGround() && !mc.player.onClimbable() && !mc.player.isInWater() && !mc.player.hasEffect(MobEffects.LEVITATION) && this.am()) {
         var1 = true;
      } else {
         var1 = false;
      }

      return var1;
   }

   public boolean U() {
      return this.am();
   }

   private boolean am() {
      return mc.player == null ? false : LivingEntity.canGlideUsing(mc.player.getItemBySlot(EquipmentSlot.CHEST), EquipmentSlot.CHEST);
   }

   private Vec3 an() {
      if (mc.player == null) {
         double var1 = 0.0;
         double var3 = 0.0;
         double var5 = 1.0;
         return new Vec3(var1, var3, var5);
      }

      Vec3 var7 = mc.player.position();
      Vec3 var8 = this.ap;
      Vec3 var9 = var7.subtract(var8);
      double var10 = var9.lengthSqr();
      double var12 = 1.0E-12;
      Vec3 var14;
      if (var10 > var12) {
         var14 = var9.normalize();
      } else {
         double var15 = 0.0;
         double var17 = 0.0;
         double var19 = 1.0;
         var14 = new Vec3(var15, var17, var19);
      }

      Vec3 var21 = var14;
      double var22 = var21.x;
      double var24 = var21.z;
      double var26 = var22;
      double var28 = Math.toDegrees(Math.atan2(var24, var26));
      double var30 = 360.0;
      double var32 = var28 % var30;
      double var34 = 360.0;
      double var36 = var32 + var34;
      double var38 = 360.0;
      double var40 = var36 % var38;
      double var42 = 45.0;
      double var44 = Math.round(var40 / var42);
      double var46 = 45.0;
      double var48 = var44 * var46;
      double var50 = 360.0;
      double var52 = var48 % var50;
      double var54 = 360.0;
      double var56 = var52 + var54;
      double var58 = 360.0;
      double var60 = Math.toRadians(var56 % var58);
      double var62 = var22;
      double var64 = var22;
      double var66 = var62 * var64;
      double var68 = var24;
      double var70 = var24;
      double var72 = var68 * var70;
      double var74 = Math.sqrt(var66 + var72);
      double var76 = Math.cos(var60);
      double var78 = var74;
      double var80 = var76 * var78;
      double var82 = 0.0;
      double var84 = Math.sin(var60);
      double var86 = var74;
      double var88 = var84 * var86;
      return new Vec3(var80, var82, var88);
   }

   private Vec3 a_Vec3_Vec3_Vec3(Vec3 var1, Vec3 var2) {
      Vec3 var3 = this.ap;
      Vec3 var4 = var1.subtract(var3);
      double var5 = var2.dot(var2);
      double var7 = 1.0E-9;
      double var13;
      if (var5 > var7) {
         double var9 = var4.dot(var2);
         double var11 = var5;
         var13 = var9 / var11;
      } else {
         var13 = 0.0;
      }

      double var15 = var13;
      Vec3 var17 = this.ap;
      double var18 = var15;
      Vec3 var20 = var2.scale(var18);
      return var17.add(var20);
   }

   private boolean b_bool_Vec3_Vec3(Vec3 var1, Vec3 var2) {
      if (mc.level != null && mc.player != null) {
         double var3 = this.obstacleLookAhead.getValue().doubleValue();
         double var5 = 0.0;
         if (var3 <= var5) {
            return false;
         }

         double var7 = var2.length();
         double var9 = 1.0E-6;
         Vec3 var11;
         if (var7 > var9) {
            var11 = var2.normalize();
         } else {
            double var12 = 0.0;
            double var14 = 0.0;
            double var16 = 1.0;
            var11 = new Vec3(var12, var14, var16);
         }

         Vec3 var18 = var11;
         double var19 = var3;
         Vec3 var21 = var18.scale(var19);
         Vec3 var22 = var1.add(var21);
         ClientLevel var23 = mc.level;
         Vec3 var24 = var22;
         LocalPlayer var25 = mc.player;
         boolean var26;
         if (var23.clip(new ClipContext(var1, var24, Block.COLLIDER, Fluid.NONE, var25)).getType() == Type.BLOCK) {
            var26 = true;
         } else {
            var26 = false;
         }

         return var26;
      } else {
         return false;
      }
   }

   private boolean c(Vec3 var1, Vec3 var2) {
      if (mc.level == null) {
         return false;
      }

      double var3 = var1.x;
      double var5 = var1.y;
      double var7 = var1.z;
      BlockPos var9 = BlockPos.containing(var3, var5, var7).below();
      ClientLevel var10 = mc.level;
      BlockPos var11 = var9;
      BlockState var12 = var10.getBlockState(var11);
      ClientLevel var13 = mc.level;
      BlockPos var14 = var9;
      if (!var12.isFaceSturdy(var13, var14, Direction.UP)) {
         return true;
      }

      double var15 = 0.0;
      double var17 = 0.063;
      double var19 = 0.0;
      Vec3 var21 = var1.add(var15, var17, var19);
      if (this.b_bool_Vec3_Vec3(var21, var2)) {
         return true;
      }

      double var22 = 0.0;
      double var24 = 1.01;
      double var26 = 0.0;
      Vec3 var28 = var1.add(var22, var24, var26);
      if (this.b_bool_Vec3_Vec3(var28, var2)) {
         return true;
      }

      double var29 = 0.0;
      double var31 = 1.99;
      double var33 = 0.0;
      Vec3 var35 = var1.add(var29, var31, var33);
      return this.b_bool_Vec3_Vec3(var35, var2);
   }

   private void a_void_Vec3_Vec3_bool(Vec3 var1, Vec3 var2, boolean var3) {
      double var4 = 1.0;
      double var6 = this.obstacleLookAhead.getValue().doubleValue();
      double var8 = Math.max(var4, var6);
      double var10 = var2.length();
      double var12 = 1.0E-6;
      Vec3 var14;
      if (var10 > var12) {
         var14 = var2.normalize();
      } else {
         double var15 = 0.0;
         double var17 = 0.0;
         double var19 = 1.0;
         var14 = new Vec3(var15, var17, var19);
      }

      Vec3 var21 = var14;
      boolean var22;
      if (!var3) {
         var22 = true;
      } else {
         var22 = false;
      }

      boolean var23 = var22;
      Vec3 var24 = var1;
      int var25 = 0;

      while (true) {
         if (!var23) {
            Vec3 var26 = var24;
            Vec3 var27 = var21;
            if (!this.c(var26, var27)) {
               break;
            }
         }

         int var28 = var25++;
         if (var28 >= 200) {
            break;
         }

         Vec3 var29 = var24;
         Vec3 var30 = var21;
         double var31 = var8;
         Vec3 var33 = var30.scale(var31);
         var24 = var29.add(var33);
         var23 = false;
      }

      Vec3 var34 = var24;
      this.a_void_Vec3(var34);
   }

   private void a_void_Vec3(Vec3 var1) {
      this.aq = var1;
      if (this.standUpToPass.getValue()) {
         this.ao();
      }

      try {
         Object var2 = BaritoneUtils.getPrimaryBaritone();
         if (var2 == null) {
            return;
         }

         double var3 = var1.x;
         double var5 = var1.y;
         double var7 = var1.z;
         BlockPos var9 = BlockPos.containing(var3, var5, var7);
         Class var10 = Class.forName("baritone.api.pathing.goals.Goal");
         Class var11 = Class.forName("baritone.api.pathing.goals.GoalGetToBlock");
         Class[] var12 = new Class[]{BlockPos.class};
         Constructor var13 = var11.getConstructor(var12);
         Object[] var14 = new Object[1];
         BlockPos var15 = var9;
         var14[0] = var15;
         Object var16 = var13.newInstance(var14);
         Class var17 = var2.getClass();
         Class[] var18 = new Class[0];
         Method var19 = var17.getMethod("getCustomGoalProcess", var18);
         Object var20 = var2;
         Object[] var21 = new Object[0];
         Object var22 = var19.invoke(var20, var21);
         Class var23 = var22.getClass();
         Class[] var24 = new Class[1];
         Class var25 = var10;
         var24[0] = var25;
         Method var26 = var23.getMethod("setGoalAndPath", var24);
         Object var27 = var22;
         Object[] var28 = new Object[1];
         Object var29 = var16;
         var28[0] = var29;
         var26.invoke(var27, var28);
         if (this.ax()) {
            this.b_void_String("OBSTACLE PASS: goal set to " + var9);
         }
      } catch (Throwable var31) {
         if (this.ax()) {
            this.b_void_String("OBSTACLE PASS FAILED: " + var31);
         }
      }
   }

   private void ao() {
      if (mc.player != null && mc.player.isFallFlying()) {
         if (mc.player.getItemBySlot(EquipmentSlot.CHEST).getItem() == Items.ELYTRA) {
            int var1 = this.ak();
            if (var1 == -1) {
               EntityFlagAccessor var2 = (EntityFlagAccessor)mc.player;
               int var3 = EntityFlagAccessor.getFlagFallFlying();
               var2.invokeSetSharedFlag(var3, false);
               if (this.ax()) {
                  this.b_void_String("STAND-UP-TO-PASS: inventory full, fell back to flag-only clear");
               }
            } else {
               int var4 = mc.player.inventoryMenu.containerId;
               MultiPlayerGameMode var5 = mc.gameMode;
               int var6 = var4;
               LocalPlayer var7 = mc.player;
               var5.handleContainerInput(var6, 6, 0, ContainerInput.PICKUP, var7);
               MultiPlayerGameMode var8 = mc.gameMode;
               int var9 = var4;
               int var10 = var1;
               LocalPlayer var11 = mc.player;
               var8.handleContainerInput(var9, var10, 0, ContainerInput.PICKUP, var11);
               MultiPlayerGameMode var12 = mc.gameMode;
               int var13 = var4;
               int var14 = var1;
               LocalPlayer var15 = mc.player;
               var12.handleContainerInput(var13, var14, 0, ContainerInput.PICKUP, var15);
               MultiPlayerGameMode var16 = mc.gameMode;
               int var17 = var4;
               LocalPlayer var18 = mc.player;
               var16.handleContainerInput(var17, 6, 0, ContainerInput.PICKUP, var18);
               if (this.ax()) {
                  this.b_void_String("STAND-UP-TO-PASS: unequipped/re-equipped elytra for obstacle passing");
               }
            }
         }
      }
   }

   private void ap() {
      try {
         Object var1 = BaritoneUtils.getPrimaryBaritone();
         if (var1 == null) {
            return;
         }

         Class var2 = var1.getClass();
         Class[] var3 = new Class[0];
         Method var4 = var2.getMethod("getPathingBehavior", var3);
         Object var5 = var1;
         Object[] var6 = new Object[0];
         Object var7 = var4.invoke(var5, var6);
         Class var8 = var7.getClass();
         Class[] var9 = new Class[0];
         Method var10 = var8.getMethod("cancelEverything", var9);
         Object var11 = var7;
         Object[] var12 = new Object[0];
         var10.invoke(var11, var12);
      } catch (Throwable var14) {
      }
   }

   private boolean aq() {
      if (mc.player == null) {
         return false;
      }

      if (!this.passObstacles.getValue()) {
         return false;
      }

      if (!BaritoneUtils.isAvailable()) {
         return false;
      }

      if (!BaritoneUtils.isActive()) {
         this.aq = null;
      }

      Vec3 var1 = mc.player.position();
      double var2 = var1.x;
      double var4 = this.ap.x;
      double var6 = var2 - var4;
      double var8 = 2.0;
      double var10 = Math.pow(var6, var8);
      double var12 = var1.z;
      double var14 = this.ap.z;
      double var16 = var12 - var14;
      double var18 = 2.0;
      double var20 = Math.pow(var16, var18);
      double var22 = Math.sqrt(var10 + var20);
      double var24 = 0.1;
      if (var22 <= var24) {
         return false;
      }

      Vec3 var26 = this.an();
      Vec3 var27 = var1;
      Vec3 var28 = var26;
      Vec3 var29 = this.a_Vec3_Vec3_Vec3(var27, var28);
      if (this.aq != null) {
         if (this.standUpToPass.getValue()) {
            this.ao();
         }

         Vec3 var30 = this.aq;
         Vec3 var31 = var26;
         if (this.c(var30, var31)) {
            Vec3 var32 = this.aq;
            Vec3 var33 = var26;
            this.a_void_Vec3_Vec3_bool(var32, var33, false);
         }

         return true;
      } else {
         if (!mc.player.onGround()) {
            return false;
         }

         double var34 = this.ar();
         double var36 = 0.01;
         boolean var38;
         if (var34 < var36) {
            var38 = true;
         } else {
            var38 = false;
         }

         boolean var39 = var38;
         if (this.P() && var39) {
            Vec3 var40 = var29;
            Vec3 var41 = var26;
            this.a_void_Vec3_Vec3_bool(var40, var41, false);
            return true;
         } else {
            double var42 = var1.x;
            double var44 = var29.y;
            double var46 = var1.z;
            Vec3 var48 = new Vec3(var42, var44, var46);
            Vec3 var49 = var29;
            double var50 = var48.distanceTo(var49);
            double var52 = 0.0;
            double var54 = var1.y;
            double var56 = var29.y;
            double var58 = var54 - var56;
            double var60 = Math.min(var52, var58);
            double var62 = var50 + var60;
            double var64 = 2.0;
            if (var62 > var64) {
               Vec3 var66 = var29;
               Vec3 var67 = var26;
               this.a_void_Vec3_Vec3_bool(var66, var67, true);
               return true;
            } else {
               Vec3 var68 = var48;
               Vec3 var69 = var26;
               if (this.c(var68, var69)) {
                  Vec3 var70 = var29;
                  Vec3 var71 = var26;
                  this.a_void_Vec3_Vec3_bool(var70, var71, false);
                  return true;
               } else {
                  return BaritoneUtils.isActive();
               }
            }
         }
      }
   }

   private double ar() {
      Vec3 var1 = this.an;
      Vec3 var2 = this.am;
      Vec3 var3 = var1.subtract(var2);
      double var4 = var3.x;
      double var6 = var3.x;
      double var8 = var4 * var6;
      double var10 = var3.z;
      double var12 = var3.z;
      double var14 = var10 * var12;
      double var16 = Math.sqrt(var8 + var14);
      double var18 = 20.0;
      return var16 * var18;
   }

   private boolean as() {
      if (mc.player == null) {
         return false;
      }

      double var1 = mc.player.getY();
      double var3 = this.ae;
      double var5 = var1 - var3;
      double var7 = 0.163;
      boolean var9;
      if (var5 < var7) {
         var9 = true;
      } else {
         var9 = false;
      }

      return var9;
   }

   private boolean at() {
      if (mc.level != null && mc.player != null) {
         Vec3 var1 = mc.player.getDeltaMovement();
         double var2 = var1.y;
         double var4 = -0.01;
         if (var2 >= var4) {
            return false;
         }

         Vec3 var6 = mc.player.position();
         double var7 = 0.0;
         double var9 = -8.0;
         double var11 = 0.0;
         Vec3 var13 = var6.add(var7, var9, var11);
         ClientLevel var14 = mc.level;
         Vec3 var15 = var6;
         Vec3 var16 = var13;
         LocalPlayer var17 = mc.player;
         BlockHitResult var18 = var14.clip(new ClipContext(var15, var16, Block.COLLIDER, Fluid.NONE, var17));
         if (var18.getType() != Type.BLOCK) {
            return false;
         }

         double var19 = var6.y;
         double var21 = var18.getLocation().y;
         double var23 = var19 - var21;
         double var25 = Math.abs(var1.y);
         double var27 = var23 / var25;
         double var29 = this.bypassTicks.getValue().doubleValue();
         boolean var31;
         if (var27 <= var29) {
            var31 = true;
         } else {
            var31 = false;
         }

         return var31;
      } else {
         return false;
      }
   }

   private double au() {
      if (mc.player == null) {
         return 0.0;
      }

      Vec3 var1 = mc.player.getDeltaMovement();
      double var2 = var1.x;
      double var4 = var1.x;
      double var6 = var2 * var4;
      double var8 = var1.z;
      double var10 = var1.z;
      double var12 = var8 * var10;
      double var14 = Math.sqrt(var6 + var12);
      double var16 = 20.0;
      return var14 * var16;
   }

   private void av() {
      if (!this.ak) {
         this.ak = true;

         label45: {
            try {
               if (mc.getConnection() == null) {
                  this.ag.clear();
                  this.ah.clear();
                  break label45;
               }

               if (!this.ag.isEmpty()) {
                  Connection var1 = mc.getConnection().getConnection();
                  if (var1 != null && var1.isConnected()) {
                     while (true) {
                        Packet var2 = this.ag.poll();
                        if (var2 == null) {
                           break;
                        }

                        Connection var3 = var1;
                        Packet var4 = var2;
                        var3.send(var4, null, true);
                     }
                  } else {
                     this.ag.clear();
                  }
               }

               if (!this.ah.isEmpty()) {
                  while (true) {
                     ClientboundPingPacket var5 = this.ah.poll();
                     if (var5 == null) {
                        break;
                     }

                     ClientboundPingPacket var6 = var5;
                     ClientPacketListener var7 = mc.getConnection();
                     var6.handle(var7);
                  }
               }
            } catch (Throwable var9) {
               this.ak = false;
               throw var9;
            }

            this.ak = false;
            return;
         }

         this.ak = false;
      }
   }

   private void aw() {
      this.Y = false;
      this.Z = false;
      this.aa = false;
      this.ab = false;
      this.ac = 0;
      this.ad = false;
      this.aj = false;
      this.ai = false;
      this.al = 0;
   }

   private void b_void_String(String var1) {
      if (mc.player != null) {
         Night.chatManager.sendTaggedMessage(var1, "EBounce+");
      }
   }

   private boolean ax() {
      BounceDebug var1 = Night.moduleManager.getModule(BounceDebug.class);
      boolean var2;
      if (var1 != null && var1.isEnabled()) {
         var2 = true;
      } else {
         var2 = false;
      }

      return var2;
   }

   @Override
   public String getDisplayInfo() {
      return this.mode.getValue();
   }
}
