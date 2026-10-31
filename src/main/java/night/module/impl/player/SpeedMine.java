package night.module.impl.player;

import com.mojang.blaze3d.vertex.PoseStack;
import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import lombok.Generated;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.PosRot;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.Rot;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.events.EventTarget;
import night.events.impl.AttackBlockEvent;
import night.events.impl.BlockPlaceEvent;
import night.events.impl.ClientDisconnectEvent;
import night.events.impl.PacketReceiveEvent;
import night.events.impl.PacketSendEvent;
import night.events.impl.Render3DEvent;
import night.events.impl.UpdateWalkingPlayerEvent;
import night.manager.RotationManager;
import night.mixins.accessors.ClientWorldAccessor;
import night.mixins.accessors.PlayerMoveC2SPacketAccessor;
import night.module.Module;
import night.module.ModuleInfo;
import night.module.impl.combat.AutoCrystal;
import night.module.impl.combat.AutoMine;
import night.module.impl.combat.AutoTrap;
import night.module.impl.combat.Mainhand;
import night.module.impl.visuals.LogoutSpot;
import night.module.impl.visuals.PopChams;
import night.pingbypass.PingBypassState;
import night.pingbypass.network.RubberbandTracker;
import night.pingbypass.protocol.PingBypassPayload;
import night.pingbypass.protocol.s2c.S2CMineProgressPacket;
import night.security.LicenseService;
import night.setting.impl.BooleanSetting;
import night.setting.impl.CategorySetting;
import night.setting.impl.ColorSetting;
import night.setting.impl.ModeSetting;
import night.setting.impl.NumberSetting;
import night.setting.impl.WhitelistSetting;
import night.util.MinecraftInstance;
import night.util.color.ColorUtil;
import night.util.inventory.InventoryUtil;
import night.util.math.TimerUtil;
import night.util.network.PacketPredictionUtil;
import night.util.render.Render3DUtil;
import night.util.rotation.RotationMathUtil;
import night.util.world.BlockInteractionUtil;
import night.util.world.EntityUtil;
import night.util.world.HoleUtil;
import night.util.world.PacketAndRenderUtil;

@ModuleInfo(name = "SpeedMine", description = "Automatically mines blocks at a faster speed using packets.", category = Module.Category.PLAYER)
public class SpeedMine extends Module {
   public ModeSetting switchSetting = new ModeSetting(
      "Switch", "The mode that will be used for automatically switching to the fastest item.", "Silent", InventoryUtil.a
   );
   public NumberSetting range;
   public NumberSetting speed;
   public BooleanSetting farReach;
   public BooleanSetting s6b;
   public ModeSetting rotate;
   public BooleanSetting switchReset;
   public BooleanSetting doubleSetting;
   public BooleanSetting shift;
   public ModeSetting none;
   public NumberSetting fast;
   public NumberSetting fast2;
   public BooleanSetting fast3;
   public CategorySetting onGroundCheckCategory;
   public BooleanSetting nCP;
   public BooleanSetting airCheck;
   public BooleanSetting liquidCheck;
   public BooleanSetting grim;
   public BooleanSetting whileEating;
   public ModeSetting x;
   public WhitelistSetting y;
   public CategorySetting z;
   public ModeSetting none2;
   public ModeSetting fill;
   public ModeSetting fill2;
   public ColorSetting fill3;
   public ColorSetting outline;
   public ModeSetting none3;
   public ColorSetting custom;
   private SpeedMine.MineTask M;
   private SpeedMine.FadeAnimation N;
   private BlockPos O;
   private final LinkedHashSet<BlockPos> P;
   private static final int Q = 10;
   private static final int R = 3;
   private static final int S = 10;
   int T;
   int U;
   private static final int V = 80;
   private boolean W;
   private boolean X;
   private boolean Y;
   final TimerUtil Z;
   private static final long aa = 200L;
   final TimerUtil ab;
   private double ac;
   private long ad;
   private static final int ae = 6;
   private int af;
   volatile boolean ag;
   volatile long ah;
   boolean ai;
   public final Object H;
   private static final long aj = 750L;
   public volatile BlockPos I;
   public volatile float J;
   public volatile BlockPos K;
   public volatile float L;
   private volatile float ak;
   private volatile long al;
   private volatile long am;
   private volatile float an;
   private volatile long ao;
   private volatile long ap;
   private static final int aq = 2000;

   public SpeedMine() {
      Double var1 = 6.0;
      Double var2 = 0.0;
      Double var3 = 8.0;
      this.range = new NumberSetting("Range", "The maximum distance at which blocks will be mined.", var1, var2, var3);
      Double var4 = 1.0;
      Double var5 = 0.7;
      Double var6 = 1.0;
      this.speed = new NumberSetting("Speed", "The speed at which the module will mine blocks.", var4, var5, var6);
      this.farReach = new BooleanSetting("FarReach", "Bypass for Duration 0.7 on Grim. Try this if normal AutoMine doesn't work.", false);
      this.s6b = new BooleanSetting("6b", "Allows mining bedrock after 40 ticks and enables instant-mining when broken.", false);
      String[] var7 = new String[]{"None", "Normal", "Silent"};
      this.rotate = new ModeSetting("Rotate", "Automatically rotates to the block when mining it.", "Normal", var7);
      ModeSetting var8 = this.switchSetting;
      String[] var9 = new String[]{"None", "AltSwap", "AltPickup"};
      this.switchReset = new BooleanSetting("SwitchReset", "Resets the mining when switching slots.", new ModeSetting.ModeCondition(var8, var9), true);
      BooleanSetting var10 = new BooleanSetting("Double", "Allows the mining of 2 blocks at the same time.", false);
      this.doubleSetting = var10;
      BooleanSetting var12 = this.doubleSetting;
      BooleanSetting var11 = new BooleanSetting(
         "Shift",
         "Selects the block behind according to crosshair as secondary when clicking while holding shift.",
         new BooleanSetting.BooleanCondition(var12, true),
         true
      );
      this.shift = var11;
      String[] var14 = new String[]{"None", "Fast", "Instant"};
      ModeSetting var13 = new ModeSetting("Rebreak", "Automatically re-mines blocks once they have been replaced.", "None", var14);
      this.none = var13;
      ModeSetting var17 = this.none;
      String[] var18 = new String[]{"Fast", "Instant"};
      ModeSetting.ModeCondition var16 = new ModeSetting.ModeCondition(var17, var18);
      Integer var19 = 0;
      Integer var20 = 0;
      Integer var21 = 20;
      NumberSetting var15 = new NumberSetting("InstantDelay", "The amount of time that has to pass before instantly mining blocks.", var16, var19, var20, var21);
      this.fast = var15;
      ModeSetting var24 = this.none;
      String[] var25 = new String[]{"Fast", "Instant"};
      ModeSetting.ModeCondition var23 = new ModeSetting.ModeCondition(var24, var25);
      Integer var26 = 60;
      Integer var27 = 0;
      Integer var28 = 100;
      NumberSetting var22 = new NumberSetting(
         "InstantTimeout", "The amount of time that cancel instantly mine while no block to mine.", var23, var26, var27, var28
      );
      this.fast2 = var22;
      ModeSetting var30 = this.none;
      String[] var31 = new String[]{"Fast", "Instant"};
      BooleanSetting var29 = new BooleanSetting(
         "Async",
         "Keeps instantly re-firing even while the target position is currently air, instead of waiting for it to solidify.",
         new ModeSetting.ModeCondition(var30, var31),
         false
      );
      this.fast3 = var29;
      CategorySetting var32 = new CategorySetting(
         "OnGroundCheck", "Claims onGround on outgoing movement packets while mining, bypassing the not-on-the-ground mining speed penalty."
      );
      this.onGroundCheckCategory = var32;
      CategorySetting var35 = this.onGroundCheckCategory;
      CategorySetting.CategoryCondition var34 = new CategorySetting.CategoryCondition(var35);
      BooleanSetting var33 = new BooleanSetting(
         "NCP", "Enables the onGround claim below. Turn off for servers that don't penalize air/liquid mining.", var34, false
      );
      this.nCP = var33;
      CategorySetting var38 = this.onGroundCheckCategory;
      CategorySetting.CategoryCondition var37 = new CategorySetting.CategoryCondition(var38);
      BooleanSetting var36 = new BooleanSetting("AirCheck", "Claims onGround while airborne and mining.", var37, false);
      this.airCheck = var36;
      CategorySetting var41 = this.onGroundCheckCategory;
      CategorySetting.CategoryCondition var40 = new CategorySetting.CategoryCondition(var41);
      BooleanSetting var39 = new BooleanSetting("LiquidCheck", "Claims onGround while in liquid and mining.", var40, false);
      this.liquidCheck = var39;
      BooleanSetting var42 = new BooleanSetting("Grim", "Adds a bypass catered to the Grim anticheat.", false);
      this.grim = var42;
      BooleanSetting var43 = new BooleanSetting("WhileEating", "Mines blocks while eating.", true);
      this.whileEating = var43;
      String[] var45 = new String[]{"All", "WhiteList", "BlackList"};
      ModeSetting var44 = new ModeSetting(
         "Mode", "All = mine every block. WhiteList = mine only listed blocks. BlackList = mine every block except listed.", "All", var45
      );
      this.x = var44;
      WhitelistSetting var46 = new WhitelistSetting("List", "Blocks the WhiteList/BlackList mode compares against.", WhitelistSetting.WhitelistType.BLOCKS);
      this.y = var46;
      CategorySetting var47 = new CategorySetting("Render", "The category containing all settings related to rendering.");
      this.z = var47;
      CategorySetting var50 = this.z;
      CategorySetting.CategoryCondition var49 = new CategorySetting.CategoryCondition(var50);
      String[] var51 = new String[]{"None", "Fill", "Outline", "Both"};
      ModeSetting var48 = new ModeSetting("Render", "Mode", "The rendering that will be applied to the blocks highlighted.", var49, "Both", var51);
      this.none2 = var48;
      ModeSetting var54 = this.none2;
      String[] var55 = new String[]{"Fill", "Outline", "Both"};
      ModeSetting.ModeCondition var53 = new ModeSetting.ModeCondition(var54, var55);
      String[] var56 = new String[]{"None", "Expand", "Rise"};
      ModeSetting var52 = new ModeSetting("Animation", "The animation that will be used when rendering the block mining progress.", var53, "Expand", var56);
      this.fill = var52;
      ModeSetting var59 = this.none2;
      String[] var60 = new String[]{"Fill", "Outline", "Both"};
      ModeSetting.ModeCondition var58 = new ModeSetting.ModeCondition(var59, var60);
      String[] var61 = new String[]{"Static", "Smooth", "Custom"};
      ModeSetting var57 = new ModeSetting("Color", "The color that will be used when rendering the block mining.", var58, "Smooth", var61);
      this.fill2 = var57;
      ModeSetting var64 = this.none2;
      String[] var65 = new String[]{"Fill", "Both"};
      ModeSetting.ModeCondition var63 = new ModeSetting.ModeCondition(var64, var65);
      ColorSetting.ColorValue var66 = ColorUtil.b();
      ColorSetting var62 = new ColorSetting("FillColor", "The color used for the fill rendering.", var63, var66);
      this.fill3 = var62;
      ModeSetting var69 = this.none2;
      String[] var70 = new String[]{"Outline", "Both"};
      ModeSetting.ModeCondition var68 = new ModeSetting.ModeCondition(var69, var70);
      ColorSetting.ColorValue var71 = ColorUtil.c();
      ColorSetting var67 = new ColorSetting("OutlineColor", "The color used for the outline rendering.", var68, var71);
      this.outline = var67;
      CategorySetting var74 = this.z;
      CategorySetting.CategoryCondition var73 = new CategorySetting.CategoryCondition(var74);
      String[] var75 = new String[]{"None", "Default", "Custom"};
      ModeSetting var72 = new ModeSetting("InstantRender", "Instant", "The color that will be used for rendering instantly mined blocks.", var73, "None", var75);
      this.none3 = var72;
      ModeSetting var78 = this.none3;
      String[] var79 = new String[]{"Custom"};
      ModeSetting.ModeCondition var77 = new ModeSetting.ModeCondition(var78, var79);
      ColorSetting.ColorValue var80 = new ColorSetting.ColorValue(new Color(148, 0, 211), false, false);
      ColorSetting var76 = new ColorSetting("InstantColor", "The custom color used for instantly mined blocks.", var77, var80);
      this.custom = var76;
      this.M = null;
      this.N = null;
      this.O = null;
      LinkedHashSet var81 = new LinkedHashSet();
      this.P = var81;
      this.T = -1;
      this.U = -1;
      this.W = false;
      this.X = false;
      this.Y = false;
      TimerUtil var82 = new TimerUtil();
      this.Z = var82;
      TimerUtil var83 = new TimerUtil();
      this.ab = var83;
      double var84 = 0.0;
      this.ac = var84;
      long var86 = 0L;
      this.ad = var86;
      this.af = 0;
      this.ag = false;
      long var88 = 0L;
      this.ah = var88;
      this.ai = false;
      Object var90 = new Object();
      this.H = var90;
      this.I = null;
      this.J = 0.0F;
      this.K = null;
      this.L = 0.0F;
      this.ak = 0.0F;
      long var91 = 0L;
      this.al = var91;
      long var93 = 50L;
      this.am = var93;
      this.an = 0.0F;
      long var95 = 0L;
      this.ao = var95;
      long var97 = 50L;
      this.ap = var97;
   }

   public SpeedMine.MineTask v() {
      return this.M;
   }

   public SpeedMine.FadeAnimation w() {
      return this.N;
   }

   public void b_void_bool(boolean var1) {
      this.W = var1;
   }

   private boolean I() {
      boolean var1;
      if (this.N != null) {
         var1 = true;
      } else {
         var1 = false;
      }

      return var1;
   }

   boolean J() {
      if (this.whileEating.getValue() || !this.ag && (mc.player == null || !mc.player.isUsingItem() && !EntityUtil.a())) {
         if (!this.farReach.getValue()) {
            return true;
         }

         boolean var1;
         if (this.af == 0 && this.K()) {
            var1 = true;
         } else {
            var1 = false;
         }

         return var1;
      } else {
         return false;
      }
   }

   private boolean K() {
      long var1 = System.currentTimeMillis();
      long var3 = this.ad;
      long var5 = var1 - var3;
      long var7 = 275L;
      if (var5 >= var7) {
         return true;
      }

      long var9 = 300L;
      long var11 = var5;
      long var13 = var9 - var11;
      byte var15;
      if (this.farReach.getValue()) {
         var15 = 2;
      } else {
         var15 = 1;
      }

      long var16 = var15;
      double var18 = var13 * var16;
      double var20 = this.ac;
      double var22 = var18;
      double var24 = var20 + var22;
      double var26 = 900.0;
      boolean var28;
      if (var24 <= var26) {
         var28 = true;
      } else {
         var28 = false;
      }

      return var28;
   }

   void b_void_int(int var1) {
      long var2 = System.currentTimeMillis();
      long var4 = this.ad;
      long var6 = var2 - var4;

      for (int var8 = 0; var8 < var1; var8++) {
         long var9 = var6;
         long var11 = 275L;
         if (var9 >= var11) {
            double var13 = this.ac;
            double var15 = 0.9;
            double var17 = var13 * var15;
            this.ac = var17;
         } else {
            double var27 = this.ac;
            long var29 = 300L;
            long var31 = var6;
            double var33 = var29 - var31;
            double var35 = var27 + var33;
            this.ac = var35;
         }
      }

      double var19 = this.ac;
      double var21 = -1000.0;
      double var23 = 1000.0;
      double var25 = Mth.clamp(var19, var21, var23);
      this.ac = var25;
   }

   private void L() {
      this.d_void_bool(false);
   }

   void d_void_bool(boolean var1) {
      long var2 = System.currentTimeMillis();
      this.ad = var2;
      if (var1) {
         this.af = 6;
      }
   }

   public BlockPos x() {
      BlockPos var1;
      if (this.M != null && this.M.e()) {
         var1 = this.M.a();
      } else {
         var1 = null;
      }

      return var1;
   }

   public boolean y() {
      return this.ag;
   }

   public void c_void_bool(boolean var1) {
      this.ag = var1;
      if (var1) {
         long var2 = System.currentTimeMillis();
         this.ah = var2;
      } else {
         this.ai = true;
      }
   }

   public void a_void_BlockPos_float_BlockPos_float(BlockPos var1, float var2, BlockPos var3, float var4) {
      long var5 = System.currentTimeMillis();
      boolean var7;
      if (var1 == null) {
         if (this.I != null) {
            var7 = true;
         } else {
            var7 = false;
         }
      } else {
         BlockPos var8 = this.I;
         if (!var1.equals(var8)) {
            var7 = true;
         } else {
            var7 = false;
         }
      }

      float var9;
      if (var7) {
         var9 = var2;
      } else {
         var9 = this.J;
      }

      this.ak = var9;
      long var10 = var5;
      long var12 = this.al;
      long var14 = var10 - var12;
      long var16 = 1L;
      long var18 = 500L;
      long var20 = Mth.clamp(var14, var16, var18);
      this.am = var20;
      long var22 = var5;
      this.al = var22;
      this.I = var1;
      this.J = var2;
      boolean var24;
      if (var3 == null) {
         if (this.K != null) {
            var24 = true;
         } else {
            var24 = false;
         }
      } else {
         BlockPos var25 = this.K;
         if (!var3.equals(var25)) {
            var24 = true;
         } else {
            var24 = false;
         }
      }

      float var26;
      if (var24) {
         var26 = var4;
      } else {
         var26 = this.L;
      }

      this.an = var26;
      long var27 = var5;
      long var29 = this.ao;
      long var31 = var27 - var29;
      long var33 = 1L;
      long var35 = 500L;
      long var37 = Mth.clamp(var31, var33, var35);
      this.ap = var37;
      long var39 = var5;
      this.ao = var39;
      this.K = var3;
      this.L = var4;
   }

   private float a_float_float_float_long_long(float var1, float var2, long var3, long var5) {
      long var7 = System.currentTimeMillis();
      long var9 = var3;
      float var11 = (float)(var7 - var9);
      float var12 = (float)var5;
      float var13 = Mth.clamp(var11 / var12, 0.0F, 1.0F);
      float var14 = var2 - var1;
      float var15 = var13;
      float var16 = var14 * var15;
      return var1 + var16;
   }

   public boolean a_bool_BlockPos_int(BlockPos var1, int var2) {
      if (!this.e(var1)) {
         return false;
      }

      if ((this.M == null || !this.M.a().equals(var1)) && (this.N == null || !this.N.a().equals(var1))) {
         if (this.M != null && var2 <= 0 && this.s6b.getValue() && this.M.p()) {
            int var3 = this.M.g();
            if (var2 <= var3) {
               return false;
            }
         }

         boolean var4;
         if (!this.doubleSetting.getValue() || !this.W && var2 <= 0) {
            var4 = false;
         } else {
            var4 = true;
         }

         boolean var5 = var4;
         if (!var5 && this.N() && this.O()) {
            return false;
         }

         if (var5) {
            if (this.O()) {
               return false;
            }

            SpeedMine.FadeAnimation var6;
            if (this.N == null && this.M != null) {
               var6 = this.M.s();
            } else {
               var6 = null;
            }

            SpeedMine.FadeAnimation var7 = var6;
            if (var7 != null) {
               SpeedMine.FadeAnimation var8 = var7;
               this.N = var8;
            } else if (this.M != null) {
               this.M.n();
            }

            this.M = new SpeedMine.MineTask(var1, var2);
            boolean var9 = this.I();
            this.X = var9;
         } else {
            if (this.M != null) {
               this.M.n();
            }

            this.M = new SpeedMine.MineTask(var1, var2);
            this.X = false;
         }

         return true;
      } else {
         return true;
      }
   }

   public boolean b(BlockPos var1, int var2) {
      if (this.M != null && this.M.h()) {
         return false;
      }

      if (!this.e(var1)) {
         return false;
      }

      if (this.s6b.getValue() && mc.level.getBlockState(var1).getBlock() == Blocks.BEDROCK) {
         return false;
      }

      if (this.O()) {
         return false;
      }

      if (this.N != null) {
         return this.N.a().equals(var1);
      }

      if (this.M != null && this.M.a().equals(var1)) {
         return true;
      }

      SpeedMine.FadeAnimation var3 = new SpeedMine.MineTask(var1, var2).s();
      if (var3 == null) {
         return false;
      }

      SpeedMine.FadeAnimation var4 = var3;
      this.N = var4;
      return true;
   }

   public boolean z() {
      TimerUtil var1 = this.ab;
      Long var2 = 50L;
      return var1.passedMs(var2);
   }

   public void a_void_Predicate(Predicate<BlockPos> var1) {
      if (this.M != null && this.M.g() == 0) {
         BlockPos var2 = this.M.a();
         if (!var1.test(var2)) {
            this.A();
         }
      }
   }

   public void b_void_Predicate(Predicate<BlockPos> var1) {
      if (this.N != null && this.N.b() == 0) {
         BlockPos var2 = this.N.a();
         if (!var1.test(var2)) {
            this.B();
         }
      }
   }

   public void A() {
      if (this.M != null) {
         this.M.n();
         this.M = null;
         this.X = false;
      }
   }

   public void B() {
      if (this.N != null) {
         SpeedMine.FadeAnimation var1 = this.N;
         this.a_void_FadeAnimation(var1);
         this.N.h();
         this.N = null;
      }
   }

   boolean M() {
      Mainhand var1 = Night.moduleManager.getModule(Mainhand.class);
      boolean var2;
      if (var1 != null && var1.x()) {
         var2 = true;
      } else {
         var2 = false;
      }

      return var2;
   }

   private void a_void_FadeAnimation(SpeedMine.FadeAnimation var1) {
      if (!var1.i) {
         ClientLevel var2 = mc.level;
         BlockPos var3 = var1.a();
         if (!var2.getBlockState(var3).canBeReplaced()) {
            BlockPos var4 = var1.a();
            this.O = var4;
         }
      }
   }

   boolean N() {
      AutoMine var1 = Night.moduleManager.getModule(AutoMine.class);
      boolean var2;
      if (var1 != null && var1.isEnabled() && var1.await.getValue()) {
         var2 = true;
      } else {
         var2 = false;
      }

      return var2;
   }

   private boolean O() {
      if (this.N != null) {
         return this.N.i;
      } else if (this.O == null) {
         return false;
      } else {
         ClientLevel var1 = mc.level;
         BlockPos var2 = this.O;
         BlockState var3 = var1.getBlockState(var2);
         if (var3.canBeReplaced()) {
            this.O = null;
            return false;
         } else {
            BlockPos var4 = this.O;
            BlockState var5 = var3;
            this.N = new SpeedMine.FadeAnimation(var4, 0, var5, 1.0F);
            this.N.i = true;
            this.O = null;
            return true;
         }
      }
   }

   public boolean C() {
      if (this.M != null) {
         ClientLevel var1 = mc.level;
         BlockPos var2 = this.M.a();
         if (!var1.getBlockState(var2).canBeReplaced()) {
            return true;
         }
      }

      return false;
   }

   public boolean D() {
      if (this.M != null && this.M.f()) {
         ClientLevel var1 = mc.level;
         BlockPos var2 = this.M.a();
         if (!var1.getBlockState(var2).canBeReplaced() && !this.s6b.getValue()) {
            return false;
         }

         SpeedMine.TargetMineEntry var3 = this.H();
         if (var3 != null) {
            BlockPos var4 = this.M.a();
            Player var5 = var3.player();
            if (this.a_bool_BlockPos_Player(var4, var5)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public boolean E() {
      if (this.M != null && this.s6b.getValue() && this.M.p()) {
         return true;
      }

      boolean var1;
      if (!this.C() && !this.D() || this.W && !this.I() && !this.X) {
         var1 = false;
      } else {
         var1 = true;
      }

      return var1;
   }

   public boolean F() {
      boolean var1;
      if (!this.C() || this.W && !this.I() && !this.X) {
         var1 = false;
      } else {
         var1 = true;
      }

      return var1;
   }

   @EventTarget
   public void a_void_UpdateWalkingPlayerEvent(UpdateWalkingPlayerEvent var1) {
      if (!this.Q()) {
         if (mc.player != null && mc.level != null) {
            LicenseService var2 = Night.licenseService;
            long var3;
            if (var2 != null && var2.getRole() != null) {
               var3 = var2.getHeartbeatAgeMs();
            } else {
               var3 = Long.MAX_VALUE;
            }

            long var5 = var3;
            long var7 = 300000L;
            long var9 = var5 - var7;
            long var11 = -1L;
            long var13 = (var9 ^ var11) >>> 63;
            long var15 = 0L;
            if (var13 == var15) {
               if (this.af > 0) {
                  int var17 = this.af - 1;
                  this.af = var17;
               }

               if (!this.doubleSetting.getValue()) {
                  this.W = false;
               }

               if (this.N != null && !this.doubleSetting.getValue()) {
                  this.N.h();
                  this.N = null;
               } else if (this.N != null && this.N.g()) {
                  SpeedMine.FadeAnimation var18 = this.N;
                  this.a_void_FadeAnimation(var18);
                  this.N = null;
               }

               if (!this.P.isEmpty() && (this.N == null || !this.N.i) && (this.M == null || this.M.m)) {
                  BlockPos var19 = this.P.iterator().next();
                  LinkedHashSet var20 = this.P;
                  BlockPos var21 = var19;
                  var20.remove(var21);
                  ClientLevel var22 = mc.level;
                  BlockPos var23 = var19;
                  if (!var22.getBlockState(var23).canBeReplaced()) {
                     BlockPos var24 = var19;
                     this.a_bool_BlockPos_int(var24, 1);
                  }
               }

               if (this.M != null && this.M.l()) {
                  this.M = null;
                  this.X = false;
               }

               if (this.P()) {
                  this.R();
               }

               AutoMine var25;
               if (Night.moduleManager != null) {
                  var25 = Night.moduleManager.getModule(AutoMine.class);
               } else {
                  var25 = null;
               }

               AutoMine var26 = var25;
               if (var26 != null) {
                  var26.w();
               }

               if (this.M != null && var26 != null) {
                  AutoMine var27 = var26;
                  BlockPos var28 = this.M.a();
                  if (var27.b(var28)) {
                     this.M.n();
                     this.M = null;
                     this.X = false;
                  }
               }
            }
         }
      }
   }

   @EventTarget
   public void a_void_Render3DEvent(Render3DEvent var1) {
      if (mc.player != null && mc.level != null) {
         if (this.Q()) {
            PoseStack var2 = var1.getMatrices();
            this.a_void_PoseStack(var2);
         } else {
            if (this.doubleSetting.getValue() && this.N != null) {
               SpeedMine.FadeAnimation var3 = this.N;
               PoseStack var4 = var1.getMatrices();
               var3.a(var4);
            }

            if (this.M != null) {
               SpeedMine.MineTask var5 = this.M;
               PoseStack var6 = var1.getMatrices();
               var5.a_void_PoseStack(var6);
            }
         }
      }
   }

   @EventTarget
   public void a_void_PacketReceiveEvent(PacketReceiveEvent var1) {
      if (mc.player != null && mc.level != null) {
         if (var1.getPacket() instanceof ClientboundBlockUpdatePacket var3) {
            SpeedMine.MineTask var4 = this.M;
            if (var4 != null) {
               BlockPos var5 = var3.getPos();
               BlockPos var6 = var4.a();
               if (var5.equals(var6) && var3.getBlockState().canBeReplaced()) {
                  var4.t();
               }
            }
         } else {
            if (var1.getPacket() instanceof ClientboundSectionBlocksUpdatePacket var8) {
               SpeedMine.MineTask var9 = this.M;
               if (var9 == null) {
                  return;
               }

               BlockPos var10 = var9.a();
               boolean[] var11 = new boolean[]{false};
               boolean[] var12 = var11;
               ClientboundSectionBlocksUpdatePacket var13 = var8;
               BlockPos var14 = var10;
               boolean[] var15 = var12;
               var13.runUpdates((var2, var3x) -> {
                  if (var2.equals(var14) && var3x.canBeReplaced()) {
                     var15[0] = true;
                  }
               });
               if (var12[0]) {
                  var9.t();
               }
            }
         }
      }
   }

   @EventTarget
   public void a_void_Post(PacketSendEvent.Post var1) {
      if (!this.Q()) {
         if (mc.player != null && mc.level != null) {
            if (!this.Y) {
               if (var1.getPacket() instanceof ServerboundSetCarriedItemPacket
                  && this.switchReset.getValue()
                  && (this.switchSetting.getValue().equalsIgnoreCase("AltSwap") || this.switchSetting.getValue().equalsIgnoreCase("AltPickup"))) {
                  this.Y = true;

                  try {
                     if (this.M != null) {
                        this.M.n();
                        this.M.w();
                     }
                  } catch (Throwable var11) {
                     this.Y = false;
                     throw var11;
                  }

                  this.Y = false;
               }

               if (!(
                  var1.getPacket() instanceof ServerboundPlayerActionPacket var4
                     && var4.getAction() == Action.START_DESTROY_BLOCK
                     && var4.getPos().getY() > 2000
               )) {
                  if (var1.getPacket() instanceof ServerboundMovePlayerPacket var6 && this.nCP.getValue() && this.M != null) {
                     boolean var7;
                     if (this.airCheck.getValue() && !mc.player.onGround() && !mc.player.isInWater()) {
                        var7 = true;
                     } else {
                        var7 = false;
                     }

                     boolean var8 = var7;
                     boolean var9;
                     if (this.liquidCheck.getValue() && mc.player.isInWater()) {
                        var9 = true;
                     } else {
                        var9 = false;
                     }

                     boolean var10 = var9;
                     if (var8 || var10) {
                        ((PlayerMoveC2SPacketAccessor)var6).setOnGround(true);
                     }
                  }
               }
            }
         }
      }
   }

   @EventTarget
   public void a_void_AttackBlockEvent(AttackBlockEvent var1) {
      if (!this.Q()) {
         if (mc.player != null && mc.level != null) {
            BlockPos var2 = var1.getPos();
            if (this.doubleSetting.getValue() && this.shift.getValue() && (mc.options.keyShift.isDown() || mc.player.isShiftKeyDown())) {
               Direction var23;
               label60: {
                  Vec3 var3 = mc.player.getLookAngle();
                  double var4 = Math.abs(var3.x);
                  double var6 = Math.abs(var3.y);
                  double var8 = Math.abs(var3.z);
                  double var10 = var6;
                  double var12 = var4;
                  if (var10 > var12) {
                     double var14 = var6;
                     double var16 = var8;
                     if (var14 > var16) {
                        double var18 = var3.y;
                        double var20 = 0.0;
                        Direction var22;
                        if (var18 > var20) {
                           var22 = Direction.UP;
                        } else {
                           var22 = Direction.DOWN;
                        }

                        var23 = var22;
                        break label60;
                     }
                  }

                  double var24 = var4;
                  double var26 = var8;
                  if (var24 > var26) {
                     double var28 = var3.x;
                     double var30 = 0.0;
                     Direction var32;
                     if (var28 > var30) {
                        var32 = Direction.EAST;
                     } else {
                        var32 = Direction.WEST;
                     }

                     var23 = var32;
                  } else {
                     double var33 = var3.z;
                     double var35 = 0.0;
                     Direction var37;
                     if (var33 > var35) {
                        var37 = Direction.SOUTH;
                     } else {
                        var37 = Direction.NORTH;
                     }

                     var23 = var37;
                  }
               }

               BlockPos var38 = var2;
               Direction var39 = var23;
               BlockPos var40 = var38.relative(var39);
               if (this.b_bool_BlockPos(var40)) {
                  BlockPos var41 = var40;
                  if (!this.d_bool_BlockPos(var41)) {
                     ClientLevel var42 = mc.level;
                     BlockPos var43 = var40;
                     if (!var42.getBlockState(var43).canBeReplaced()) {
                        BlockPos var44 = var40;
                        this.a_bool_BlockPos_int(var44, 1);
                     }
                  }
               }
            }

            BlockPos var45 = var2;
            if (this.a_bool_BlockPos_int(var45, 1)) {
               var1.setCancelled(true);
            } else if (this.N != null && this.N.i) {
               LinkedHashSet var46 = this.P;
               BlockPos var47 = var2;
               var46.add(var47);
               if (this.P.size() > 2) {
                  LinkedHashSet var48 = this.P;
                  Object var49 = this.P.iterator().next();
                  var48.remove(var49);
               }

               var1.setCancelled(true);
            }
         }
      }
   }

   @EventTarget
   public void a_void_ClientDisconnectEvent(ClientDisconnectEvent var1) {
      this.M = null;
      this.N = null;
      this.O = null;
      this.P.clear();
      this.W = false;
      this.X = false;
   }

   @Override
   public void onEnable() {
      this.W = false;
      this.X = false;
   }

   @Override
   public void onDisable() {
      this.W = false;
      this.X = false;
      if (!this.Q()) {
         this.G();
         if (mc.player != null && mc.level != null) {
            if (this.N != null) {
               SpeedMine.FadeAnimation var1 = this.N;
               this.a_void_FadeAnimation(var1);
               this.N.h();
            }

            this.N = null;
            if (this.M != null) {
               this.M.n();
               this.M = null;
            }

            this.c_void_bool(false);
         } else {
            this.M = null;
            this.N = null;
         }
      }
   }

   public void G() {
      if (this.M != null) {
         this.M.k();
      }

      if (mc.player != null) {
         int var1 = InventoryUtil.a_int_Item_int_int(Items.IRON_PICKAXE, InventoryUtil.c, InventoryUtil.d);
         if (var1 != -1) {
            BlockState var2 = Blocks.OBSIDIAN.defaultBlockState();
            int var3 = InventoryUtil.d + 1;
            int var4 = InventoryUtil.a_int_BlockState_int_int(var2, var3, InventoryUtil.f);
            if (var4 == -1) {
               var4 = InventoryUtil.d(InventoryUtil.d + 1, InventoryUtil.f);
            }

            if (var4 != -1 && var4 > InventoryUtil.d) {
               int var5 = var4;
               int var6 = var1;
               InventoryUtil.c("Swap", var5, var6);
            }
         }
      }
   }

   @Override
   public String getDisplayInfo() {
      String var1;
      if (this.M == null) {
         var1 = "0.0";
      } else {
         DecimalFormat var2 = new DecimalFormat("0.0");
         float var3 = this.M.c();
         float var4 = this.M.F();
         double var5 = var3 / var4;
         var1 = var2.format(var5);
      }

      String var7 = var1;
      String var8;
      if (this.N != null && this.doubleSetting.getValue()) {
         DecimalFormat var9 = new DecimalFormat("0.0");
         float var10 = this.N.f();
         float var11 = this.N.e();
         double var12 = var10 / var11;
         var8 = ", " + var9.format(var12);
      } else {
         var8 = "";
      }

      String var14 = var8;
      String var15 = var7;
      String var16 = var14;
      return var15 + var16;
   }

   private boolean e(BlockPos var1) {
      if (mc.gameMode.getPlayerMode() != GameType.CREATIVE && mc.gameMode.getPlayerMode() != GameType.SPECTATOR) {
         if (mc.level.getBlockState(var1).getBlock().defaultDestroyTime() != -1.0F
            || this.s6b.getValue() && mc.level.getBlockState(var1).getBlock() == Blocks.BEDROCK) {
            AutoMine var2;
            if (Night.moduleManager != null) {
               var2 = Night.moduleManager.getModule(AutoMine.class);
            } else {
               var2 = null;
            }

            AutoMine var3 = var2;
            if (var3 != null && var3.b(var1)) {
               return false;
            }

            WhitelistSetting var4 = this.y;
            Block var5 = mc.level.getBlockState(var1).getBlock();
            boolean var6 = var4.contains(var5);

            if (!(switch (this.x.getValue()) {
               case "WhiteList" -> var6;
               case "BlackList" -> {
                  if (!var6) {
                     yield true;
                  } else {
                     yield false;
                  }
               }
               default -> true;
            })) {
               return false;
            }

            Vec3 var10 = mc.player.getEyePosition();
            Vec3 var11 = Vec3.atCenterOf(var1);
            double var12 = var10.distanceToSqr(var11);
            double var14 = Mth.square(this.range.getValue().doubleValue());
            boolean var16;
            if (!(var12 > var14)) {
               var16 = true;
            } else {
               var16 = false;
            }

            return var16;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public boolean a_bool_BlockPos(BlockPos var1) {
      return !this.b_bool_BlockPos(var1) ? true : this.c_bool_BlockPos(var1);
   }

   public boolean b_bool_BlockPos(BlockPos var1) {
      if (var1 == null) {
         return false;
      }

      if (mc.level.getBlockState(var1).getBlock().defaultDestroyTime() != -1.0F
         || this.s6b.getValue() && mc.level.getBlockState(var1).getBlock() == Blocks.BEDROCK) {
         boolean var2;
         if (!mc.level.getBlockState(var1).getBlock().equals(Blocks.COBWEB)) {
            var2 = true;
         } else {
            var2 = false;
         }

         return var2;
      } else {
         return false;
      }
   }

   public boolean c_bool_BlockPos(BlockPos var1) {
      if (var1 == null) {
         return true;
      }

      if (this.M != null && this.M.a().equals(var1)) {
         return true;
      }

      boolean var2;
      if (this.N != null && this.N.a().equals(var1)) {
         var2 = true;
      } else {
         var2 = false;
      }

      return var2;
   }

   public boolean a_bool_BlockPos_Player(BlockPos var1, Player var2) {
      if (var1 != null && var2 != null && mc.level != null) {
         AABB var3 = var2.getBoundingBox();
         int var4 = Mth.floor(var2.getY());
         int var5 = var4;

         while (true) {
            int var6 = var5;
            int var7 = var4 + 2;
            if (var6 > var7) {
               return false;
            }

            int var8 = Mth.floor(var3.minX);

            while (true) {
               int var9 = var8;
               int var10 = Mth.ceil(var3.maxX);
               if (var9 >= var10) {
                  var5++;
                  break;
               }

               int var11 = Mth.floor(var3.minZ);

               while (true) {
                  int var12 = var11;
                  int var13 = Mth.ceil(var3.maxZ);
                  if (var12 >= var13) {
                     var8++;
                     break;
                  }

                  int var14 = var8;
                  int var15 = var5;
                  int var16 = var11;
                  BlockPos var17 = new BlockPos(var14, var15, var16);
                  if (var1.equals(var17)) {
                     return true;
                  }

                  int var18 = var5;
                  int var19 = var4 + 1;
                  if (var18 <= var19) {
                     for (Direction var21 : Plane.HORIZONTAL) {
                        BlockPos var22 = var17;
                        Direction var23 = var21;
                        BlockPos var24 = var22.relative(var23);
                        if (var1.equals(var24)) {
                           return true;
                        }
                     }
                  }

                  var11++;
               }
            }
         }
      } else {
         return false;
      }
   }

   public boolean d_bool_BlockPos(BlockPos var1) {
      if (var1 == null) {
         return true;
      }

      Vec3 var2 = mc.player.getEyePosition();
      Vec3 var3 = Vec3.atCenterOf(var1);
      double var4 = var2.distanceToSqr(var3);
      double var6 = Mth.square(this.range.getValue().doubleValue());
      boolean var8;
      if (var4 > var6) {
         var8 = true;
      } else {
         var8 = false;
      }

      return var8;
   }

   public SpeedMine.TargetMineEntry H() {
      LogoutSpot var1;
      if (Night.moduleManager != null) {
         var1 = Night.moduleManager.getModule(LogoutSpot.class);
      } else {
         var1 = null;
      }

      LogoutSpot var2 = var1;
      PopChams var3;
      if (Night.moduleManager != null) {
         var3 = Night.moduleManager.getModule(PopChams.class);
      } else {
         var3 = null;
      }

      PopChams var4 = var3;
      List var5 = mc.level.players();
      ArrayList var6 = new ArrayList(var5);
      if (var2 != null && var2.isEnabled()) {
         for (Player var8 : (Iterable<Player>)(Object)var2.v()) {
            if (var8 != null) {
               ArrayList var9 = var6;
               Player var10 = var8;
               if (!var9.contains(var10)) {
                  ArrayList var11 = var6;
                  Player var12 = var8;
                  var11.add(var12);
               }
            }
         }
      }

      SpeedMine.TargetMineEntry var13 = null;

      for (Player var15 : (Iterable<Player>)(Object)var6) {
         LocalPlayer var16 = mc.player;
         if (var15 != var16) {
            if (var4 != null) {
               PopChams var17 = var4;
               Player var18 = var15;
               if (var17.a_bool_Entity(var18)) {
                  continue;
               }
            }

            boolean var19 = EntityUtil.c_bool_Entity(var15);
            if (var19 || var15.isAlive() && !(var15.getHealth() <= 0.0F)) {
               LocalPlayer var20 = mc.player;
               Player var21 = var15;
               double var22 = var20.distanceToSqr(var21);
               double var24 = this.range.getValue().doubleValue();
               double var26 = 2.0;
               double var28 = Mth.square(var24 + var26);
               if (!(var22 > var28)) {
                  label78: {
                     if (var2 != null) {
                        LogoutSpot var30 = var2;
                        Player var31 = var15;
                        if (var30.a_bool_Entity(var31)) {
                           LogoutSpot var32 = var2;
                           RemotePlayer var33 = (RemotePlayer)var15;
                           LogoutSpot.LogoutEntry var34 = var32.a_LogoutEntry_RemotePlayer(var33);
                           if (var34 != null) {
                              String var35 = var34.b.a;
                              if (Night.friendManager.isFriend(var35)) {
                                 continue;
                              }
                           }
                           break label78;
                        }
                     }

                     String var36 = var15.getName().getString();
                     if (Night.friendManager.isFriend(var36)) {
                        continue;
                     }
                  }

                  Player var37 = var15;
                  List var38 = this.a_List_Player(var37);
                  BlockPos var39 = this.a_BlockPos_List(var38);
                  if (this.doubleSetting.getValue() || !var38.isEmpty() && var39 != null) {
                     if (var13 == null) {
                        Player var40 = var15;
                        List var41 = var38;
                        BlockPos var42 = var39;
                        var13 = new SpeedMine.TargetMineEntry(var40, var41, var42);
                     } else {
                        boolean var43 = EntityUtil.c_bool_Entity(var13.player());
                        boolean var44 = var19;
                        if (var43 != var44) {
                           if (var43) {
                              Player var45 = var15;
                              List var46 = var38;
                              BlockPos var47 = var39;
                              var13 = new SpeedMine.TargetMineEntry(var45, var46, var47);
                           }
                        } else {
                           LocalPlayer var48 = mc.player;
                           Player var49 = var15;
                           double var50 = var48.distanceToSqr(var49);
                           LocalPlayer var52 = mc.player;
                           Player var53 = var13.player();
                           double var54 = var52.distanceToSqr(var53);
                           if (var50 < var54) {
                              Player var56 = var15;
                              List var57 = var38;
                              BlockPos var58 = var39;
                              var13 = new SpeedMine.TargetMineEntry(var56, var57, var58);
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      return var13;
   }

   private BlockPos a_BlockPos_List(List<SpeedMine.MineCandidate> var1) {
      BlockPos var2 = null;
      double var3 = 0.0;

      for (SpeedMine.MineCandidate var6 : (Iterable<SpeedMine.MineCandidate>)(Object)var1) {
         if (!this.doubleSetting.getValue() || var6.flag()) {
            BlockPos var7 = var6.pos();
            if (this.f(var7)) {
               if (HoleUtil.a_bool_Player(mc.player)) {
                  HashSet var8 = HoleUtil.a_HashSet_Player_bool_bool_bool(mc.player, true, false, true);
                  BlockPos var9 = var6.pos();
                  if (var8.contains(var9)) {
                     continue;
                  }
               }

               double var10 = 0.0;
               if (this.s6b.getValue()) {
                  ClientLevel var12 = mc.level;
                  BlockPos var13 = var6.pos();
                  if (var12.getBlockState(var13).getBlock() == Blocks.BEDROCK) {
                     if (var6.flag()) {
                        double var14 = var10;
                        double var16 = 1000.0;
                        var10 = var14 + var16;
                     } else {
                        double var18 = var10;
                        double var20 = 1000.0;
                        var10 = var18 - var20;
                     }
                  }
               }

               double var26;
               if (var6.flag()) {
                  double var22 = var10;
                  double var24 = 5.0;
                  var26 = var22 + var24;
                  ClientLevel var28 = mc.level;
                  BlockPos var29 = var6.pos();
                  if (var28.getBlockState(var29).getBlock() == Blocks.ENDER_CHEST) {
                     double var30 = var26;
                     double var32 = 0.95;
                     var26 = var30 + var32;
                  } else if (BlockInteractionUtil.d_bool_BlockPos(var6.pos().offset(0, 1, 0))) {
                     double var34 = var26;
                     double var36 = 0.35;
                     var26 = var34 + var36;
                  }

                  BlockPos var38 = var6.pos();
                  if (this.g(var38)) {
                     double var39 = var26;
                     double var41 = 0.6;
                     var26 = var39 + var41;
                  }
               } else {
                  ClientLevel var43 = mc.level;
                  BlockPos var44 = var6.pos();
                  if (var43.getBlockState(var44).getBlock() == Blocks.ENDER_CHEST) {
                     double var45 = var10;
                     double var47 = 2.0;
                     var26 = var45 - var47;
                  } else if (BlockInteractionUtil.d_bool_BlockPos(var6.pos().offset(0, 1, 0))) {
                     double var49 = var10;
                     double var51 = 0.75;
                     var26 = var49 + var51;
                  } else {
                     double var53 = var10;
                     double var55 = 2.0;
                     var26 = var53 - var55;
                  }
               }

               double var57 = var26;
               double var59 = var3;
               if (var57 >= var59) {
                  var2 = var6.pos();
                  var3 = var26;
               }
            }
         }
      }

      return var2;
   }

   private List<SpeedMine.MineCandidate> a_List_Player(Player var1) {
      ArrayList var2 = new ArrayList();

      for (BlockPos var4 : (Iterable<BlockPos>)(Object)HoleUtil.a_HashSet_Player_bool_bool_bool(var1, true, false, true)) {
         ArrayList var5 = var2;
         BlockPos var6 = var4;
         var5.add(new SpeedMine.MineCandidate(var6, true));
         if (!this.doubleSetting.getValue()) {
            ArrayList var7 = var2;
            BlockPos var8 = var4.offset(0, 1, 0);
            var7.add(new SpeedMine.MineCandidate(var8, false));
         }
      }

      if (!this.doubleSetting.getValue()) {
         ArrayList var9 = var2;
         BlockPos var10 = var1.blockPosition().offset(0, 2, 0);
         var9.add(new SpeedMine.MineCandidate(var10, false));
      }

      return var2;
   }

   private boolean f(BlockPos var1) {
      if (mc.level.getBlockState(var1).canBeReplaced()) {
         return false;
      }

      if (mc.level.getBlockState(var1).getBlock().defaultDestroyTime() != -1.0F
         || this.s6b.getValue() && mc.level.getBlockState(var1).getBlock() == Blocks.BEDROCK) {
         boolean var2;
         if (!this.d_bool_BlockPos(var1)) {
            var2 = true;
         } else {
            var2 = false;
         }

         return var2;
      } else {
         return false;
      }
   }

   private boolean g(BlockPos var1) {
      for (Direction var3 : Plane.HORIZONTAL) {
         if (BlockInteractionUtil.c_bool_BlockPos(var1.relative(var3))) {
            return true;
         }
      }

      return false;
   }

   boolean P() {
      boolean var1;
      if (PingBypassState.d && Night.pingBypassConfig != null && Night.pingBypassConfig.isServerMode() && Night.pingBypassServer != null) {
         var1 = true;
      } else {
         var1 = false;
      }

      return var1;
   }

   private boolean Q() {
      return PingBypassState.a();
   }

   private void R() {
      BlockPos var1;
      if (this.M != null) {
         var1 = this.M.a();
      } else {
         var1 = null;
      }

      float var4;
      if (this.M != null) {
         float var2 = this.M.c();
         float var3 = this.M.F();
         var4 = var2 / var3;
      } else {
         var4 = 0.0F;
      }

      BlockPos var5;
      if (this.N != null) {
         var5 = this.N.a();
      } else {
         var5 = null;
      }

      float var8;
      if (this.N != null) {
         float var6 = this.N.f();
         float var7 = this.N.e();
         var8 = var6 / var7;
      } else {
         var8 = 0.0F;
      }

      PingBypassPayload var9 = PingBypassPayload.fromPacket(new S2CMineProgressPacket(var1, var4, var5, var8));
      ClientboundCustomPayloadPacket var10 = new ClientboundCustomPayloadPacket(var9);

      for (Connection var12 : (Iterable<Connection>)(Object)Night.pingBypassServer.getConnections()) {
         if (var12.isConnected()) {
            Connection var13 = var12;
            ClientboundCustomPayloadPacket var14 = var10;
            var13.send(var14);
         }
      }
   }

   private void a_void_PoseStack(PoseStack var1) {
      if (this.I != null) {
         float var2 = this.ak;
         float var3 = this.J;
         long var4 = this.al;
         long var6 = this.am;
         float var8 = this.a_float_float_float_long_long(var2, var3, var4, var6);
         BlockPos var9 = this.I;
         float var10 = var8;
         this.a(var1, var9, var10);
      }

      if (this.K != null && this.doubleSetting.getValue()) {
         float var11 = this.an;
         float var12 = this.L;
         long var13 = this.ao;
         long var15 = this.ap;
         float var17 = this.a_float_float_float_long_long(var11, var12, var13, var15);
         BlockPos var18 = this.K;
         float var19 = var17;
         this.a(var1, var18, var19);
      }
   }

   private void a(PoseStack var1, BlockPos var2, float var3) {
      if (!mc.level.getBlockState(var2).canBeReplaced()) {
         AABB var4 = new AABB(var2);
         if (this.fill.getValue().equalsIgnoreCase("Expand")) {
            AABB var5 = new AABB(var2);
            double var6 = 0.5;
            AABB var8 = var5.deflate(var6);
            double var9 = var3;
            double var11 = 2.0;
            double var13 = var9 / var11;
            double var15 = 0.0;
            double var17 = 0.5;
            double var19 = Mth.clamp(var13, var15, var17);
            var4 = var8.inflate(var19);
         }

         if (this.fill.getValue().equalsIgnoreCase("Rise")) {
            double var21 = var2.getX();
            double var23 = var2.getY();
            double var25 = var2.getZ();
            double var27 = var2.getX();
            double var29 = 1.0;
            double var31 = var27 + var29;
            double var33 = var2.getY() + var3;
            double var35 = var2.getZ();
            double var37 = 1.0;
            double var39 = var35 + var37;
            var4 = new AABB(var21, var23, var25, var31, var33, var39);
         }

         Color var41 = this.fill3.getColor();
         Color var42 = this.outline.getColor();
         if (this.fill2.getValue().equalsIgnoreCase("Static")) {
            double var43 = var3;
            double var45 = 0.9;
            Color var47;
            if (var43 >= var45) {
               int var48 = this.fill3.getAlpha();
               var47 = new Color(0, 255, 0, var48);
            } else {
               int var49 = this.fill3.getAlpha();
               var47 = new Color(255, 0, 0, var49);
            }

            var41 = var47;
            double var50 = var3;
            double var52 = 0.9;
            Color var54;
            if (var50 >= var52) {
               int var55 = this.outline.getAlpha();
               var54 = new Color(0, 255, 0, var55);
            } else {
               int var56 = this.outline.getAlpha();
               var54 = new Color(255, 0, 0, var56);
            }

            var42 = var54;
         } else if (this.fill2.getValue().equalsIgnoreCase("Smooth")) {
            int var57 = (int)(Mth.clamp(var3, 0.0F, 1.0F) * 255.0F);
            int var58 = 255 - var57;
            int var59 = (int)(Mth.clamp(var3, 0.0F, 1.0F) * 255.0F);
            int var60 = this.fill3.getAlpha();
            var41 = new Color(var58, var59, 0, var60);
            int var61 = (int)(Mth.clamp(var3, 0.0F, 1.0F) * 255.0F);
            int var62 = 255 - var61;
            int var63 = (int)(Mth.clamp(var3, 0.0F, 1.0F) * 255.0F);
            int var64 = this.outline.getAlpha();
            var42 = new Color(var62, var63, 0, var64);
         }

         if (this.none2.getValue().equalsIgnoreCase("Fill") || this.none2.getValue().equalsIgnoreCase("Both")) {
            AABB var65 = var4;
            Color var66 = var41;
            Render3DUtil.a_void_PoseStack_AABB_Color(var1, var65, var66);
         }

         if (this.none2.getValue().equalsIgnoreCase("Outline") || this.none2.getValue().equalsIgnoreCase("Both")) {
            AABB var67 = var4;
            Color var68 = var42;
            Render3DUtil.b(var1, var67, var68);
         }
      }
   }

   void a_void_Packet(Packet<?> var1) {
      if (this.P()) {
         Connection var2 = Night.pingBypassServer.getBackendConnection();
         if (var2 != null && var2.isConnected()) {
            RubberbandTracker.a(() -> var2.send(var1));
            return;
         }
      }

      mc.getConnection().send(var1);
   }

   <T extends Packet<?>> void a_void_IntFunction(IntFunction<T> var1) {
      if (this.P()) {
         BlockStatePredictionHandler var2 = ((ClientWorldAccessor)mc.level).invokeGetPendingUpdateManager().startPredicting();

         try {
            int var3 = var2.currentSequence();
            Packet var4 = (Packet)var1.apply(var3);
            this.a_void_Packet(var4);
         } catch (Throwable var10) {
            if (var2 != null) {
               try {
                  var2.close();
               } catch (Throwable var9) {
                  var10.addSuppressed(var9);
               }
            }

            throw var10;
         }

         if (var2 != null) {
            var2.close();
         }
      } else {
         PacketPredictionUtil.a_void_SequencedPacketCreator(var1x -> (Packet<ServerGamePacketListener>)var1.apply(var1x));
      }
   }

   void a(Action var1, BlockPos var2, Direction var3) {
      this.a_void_Action_BlockPos_Direction_bool(var1, var2, var3, false);
   }

   private void a_void_Action_BlockPos_Direction_bool(Action var1, BlockPos var2, Direction var3, boolean var4) {
      if (var1 == Action.STOP_DESTROY_BLOCK) {
         this.d_void_bool(var4);
      }

      BlockStatePredictionHandler var5 = ((ClientWorldAccessor)mc.level).invokeGetPendingUpdateManager().startPredicting();

      try {
         int var6 = var5.currentSequence();
         this.a_void_Packet(new ServerboundPlayerActionPacket(var1, var2, var3, var6));
      } catch (Throwable var12) {
         if (var5 != null) {
            try {
               var5.close();
            } catch (Throwable var11) {
               var12.addSuppressed(var11);
            }
         }

         throw var12;
      }

      if (var5 != null) {
         var5.close();
      }
   }

   public class FadeAnimation {
      private final BlockPos b;
      private final int c;
      private BlockState d;
      private float e;
      private float f;
      private int g;
      private boolean h;
      boolean i;
      private int j;

      FadeAnimation(BlockPos var2, int var3, BlockState var4, float var5) {
         this.b = var2;
         this.c = var3;
         this.d = var4;
         this.e = var5;
         this.f = var5;
      }

      public BlockPos a() {
         return this.b;
      }

      public int b() {
         return this.c;
      }

      public boolean c() {
         return true;
      }

      public boolean d() {
         return this.h;
      }

      public float e() {
         float var1 = (float)BlockInteractionUtil.b_double_int(2);
         return 1.0F / var1;
      }

      public float f() {
         return Mth.clamp(this.e, 0.0F, 1.0F);
      }

      public boolean g() {
         boolean var1;
         if (this.i && SpeedMine.this.N()) {
            var1 = true;
         } else {
            var1 = false;
         }

         label111: {
            boolean var2 = var1;
            if (this.i) {
               int var3 = this.g;
               byte var4;
               if (var2) {
                  var4 = 80;
               } else {
                  var4 = 10;
               }

               if (var3 > var4) {
                  break label111;
               }
            } else {
               SpeedMine var5 = SpeedMine.this;
               BlockPos var6 = this.b;
               if (var5.d_bool_BlockPos(var6)) {
                  break label111;
               }
            }

            boolean var7;
            if (MinecraftInstance.mc.player == null || !MinecraftInstance.mc.player.isUsingItem() && !EntityUtil.a()) {
               var7 = false;
            } else {
               var7 = true;
            }

            boolean var8 = var7;
            if (SpeedMine.this.ag && !var8) {
               long var9 = System.currentTimeMillis();
               long var11 = SpeedMine.this.ah;
               long var13 = var9 - var11;
               long var15 = 750L;
               if (var13 >= var15) {
                  SpeedMine.this.c_void_bool(false);
               }
            }

            ClientLevel var17 = MinecraftInstance.mc.level;
            BlockPos var18 = this.b;
            BlockState var19 = var17.getBlockState(var18);
            if (var19.canBeReplaced()) {
               BlockPos var20 = this.b;
               Night.EVENT_BUS.post(new BlockPlaceEvent(var20));
               SpeedMine.this.ab.reset();
               this.h();
               return true;
            }

            BlockState var21 = var19;
            this.d = var21;
            int var22 = InventoryUtil.a_int_BlockState_int_int(this.d, InventoryUtil.c, InventoryUtil.d);
            if (var22 == -1) {
               var22 = MinecraftInstance.mc.player.getInventory().getSelectedSlot();
            }

            BlockState var23 = this.d;
            int var24 = var22;
            float var25 = BlockInteractionUtil.a_float_BlockState_int(var23, var24);
            float var26 = Night.worldStateManager.getTimerMultiplier();
            float var27 = var25 / var26;
            if (var27 <= 0.0F) {
               this.h();
               return true;
            }

            int var28 = this.g + 1;
            this.g = var28;
            float var29 = this.e;
            this.f = var29;
            float var30 = this.e;
            float var31 = var27;
            float var32 = var30 + var31;
            this.e = var32;
            boolean var33;
            if (!SpeedMine.this.whileEating.getValue() && var8 && !SpeedMine.this.switchSetting.getValue().equalsIgnoreCase("None")) {
               var33 = false;
            } else {
               var33 = true;
            }

            boolean var34 = var33;
            boolean var35 = SpeedMine.this.M();
            if (!this.h) {
               float var36 = this.e;
               float var37 = var27;
               if (var36 + var37 >= 1.0F && var34) {
                  int var38 = var22;
                  this.a(var38);
               }
            }

            if (this.h && var35) {
               int var39 = this.j + 1;
               this.j = var39;
               if (var39 >= 3) {
                  this.h();
                  return true;
               }
            }

            if (!var2 && !this.h && !var35) {
               float var40 = this.e;
               float var41 = var27 * 10.0F;
               float var42 = 1.0F + var41;
               if (var40 >= var42) {
                  this.h();
                  return true;
               }
            }

            if (this.g > 80) {
               this.h();
               return true;
            }

            return false;
         }

         this.h();
         return true;
      }

      private void a(int var1) {
         if (!SpeedMine.this.switchSetting.getValue().equalsIgnoreCase("None")) {
            int var2 = MinecraftInstance.mc.player.getInventory().getSelectedSlot();
            this.h = true;
            if (var2 != var1) {
               SpeedMine var3 = SpeedMine.this;
               int var4 = var2;
               var3.U = var4;
               SpeedMine.this.T = var1;
               if (SpeedMine.this.P()) {
                  SpeedMine.this.a_void_Packet(new ServerboundSetCarriedItemPacket(var1));
               } else {
                  String var5 = SpeedMine.this.switchSetting.getValue();
                  int var6 = var2;
                  InventoryUtil.a_bool_String_int_int(var5, var1, var6);
               }
            }
         }
      }

      void h() {
         if (this.h) {
            this.h = false;
            if (SpeedMine.this.T != -1) {
               int var1 = SpeedMine.this.U;
               int var2 = SpeedMine.this.T;
               SpeedMine.this.T = -1;
               SpeedMine.this.U = -1;
               if (MinecraftInstance.mc.player != null) {
                  if (SpeedMine.this.switchSetting.getValue().equalsIgnoreCase("Normal")) {
                     int var3 = MinecraftInstance.mc.player.getInventory().getSelectedSlot();
                     int var4 = var1;
                     if (var3 == var4) {
                        return;
                     }
                  }

                  if (SpeedMine.this.P()) {
                     SpeedMine var5 = SpeedMine.this;
                     int var6 = var1;
                     var5.a_void_Packet(new ServerboundSetCarriedItemPacket(var6));
                  } else {
                     String var7 = SpeedMine.this.switchSetting.getValue();
                     int var8 = var2;
                     int var9 = var1;
                     InventoryUtil.b(var7, var8, var9);
                  }
               }
            }
         }
      }

      public void a(PoseStack var1) {
         ClientLevel var2 = MinecraftInstance.mc.level;
         BlockPos var3 = this.b;
         if (!var2.getBlockState(var3).canBeReplaced()) {
            float var4 = MinecraftInstance.mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
            float var5 = this.f;
            float var6 = this.e;
            double var7 = Mth.clamp(Mth.lerp(var4, var5, var6), 0.0F, 1.0F);
            BlockPos var9 = this.b;
            AABB var10 = new AABB(var9);
            if (SpeedMine.this.fill.getValue().equalsIgnoreCase("Expand")) {
               BlockPos var12 = this.b;
               AABB var11 = new AABB(var12);
               double var13 = 0.5;
               AABB var15 = var11.deflate(var13);
               double var16 = var7;
               double var18 = 2.0;
               double var20 = var16 / var18;
               double var22 = 0.0;
               double var24 = 0.5;
               double var26 = Mth.clamp(var20, var22, var24);
               var10 = var15.inflate(var26);
            }

            if (SpeedMine.this.fill.getValue().equalsIgnoreCase("Rise")) {
               double var28 = this.b.getX();
               double var30 = this.b.getY();
               double var32 = this.b.getZ();
               double var34 = this.b.getX();
               double var36 = 1.0;
               double var38 = var34 + var36;
               double var40 = this.b.getY();
               double var42 = var7;
               double var44 = var40 + var42;
               double var46 = this.b.getZ();
               double var48 = 1.0;
               double var50 = var46 + var48;
               var10 = new AABB(var28, var30, var32, var38, var44, var50);
            }

            Color var52 = SpeedMine.this.fill3.getColor();
            Color var53 = SpeedMine.this.outline.getColor();
            if (SpeedMine.this.fill2.getValue().equalsIgnoreCase("Static")) {
               double var54 = var7;
               double var56 = 0.9;
               Color var58;
               if (var54 >= var56) {
                  int var59 = SpeedMine.this.fill3.getAlpha();
                  var58 = new Color(0, 255, 0, var59);
               } else {
                  int var60 = SpeedMine.this.fill3.getAlpha();
                  var58 = new Color(255, 0, 0, var60);
               }

               var52 = var58;
               double var61 = var7;
               double var63 = 0.9;
               Color var65;
               if (var61 >= var63) {
                  int var66 = SpeedMine.this.outline.getAlpha();
                  var65 = new Color(0, 255, 0, var66);
               } else {
                  int var67 = SpeedMine.this.outline.getAlpha();
                  var65 = new Color(255, 0, 0, var67);
               }

               var53 = var65;
            } else if (SpeedMine.this.fill2.getValue().equalsIgnoreCase("Smooth")) {
               double var68 = var7;
               double var70 = 255.0;
               int var72 = (int)(var68 * var70);
               int var73 = 255 - var72;
               double var74 = var7;
               double var76 = 255.0;
               int var78 = (int)(var74 * var76);
               int var79 = SpeedMine.this.fill3.getAlpha();
               var52 = new Color(var73, var78, 0, var79);
               double var80 = var7;
               double var82 = 255.0;
               int var84 = (int)(var80 * var82);
               int var85 = 255 - var84;
               double var86 = var7;
               double var88 = 255.0;
               int var90 = (int)(var86 * var88);
               int var91 = SpeedMine.this.outline.getAlpha();
               var53 = new Color(var85, var90, 0, var91);
            }

            if (SpeedMine.this.none2.getValue().equalsIgnoreCase("Fill") || SpeedMine.this.none2.getValue().equalsIgnoreCase("Both")) {
               AABB var92 = var10;
               Color var93 = var52;
               Render3DUtil.a_void_PoseStack_AABB_Color(var1, var92, var93);
            }

            if (SpeedMine.this.none2.getValue().equalsIgnoreCase("Outline") || SpeedMine.this.none2.getValue().equalsIgnoreCase("Both")) {
               AABB var94 = var10;
               Color var95 = var53;
               Render3DUtil.b(var1, var94, var95);
            }
         }
      }
   }

   public record MineCandidate(BlockPos pos, boolean flag) {
   }

   public class MineTask {
      private final BlockPos b;
      private BlockState c;
      private final int d;
      private float e;
      private float f;
      private int g;
      private int h;
      private boolean i;
      private long j;
      private boolean k;
      private int l = -1;
      boolean m;
      private int n;
      private int o = -1;
      private final TimerUtil p = new TimerUtil();
      private float q = 0.0F;
      private boolean r = false;
      private int s = -1;
      private int t = -1;
      private boolean u;
      private boolean v;
      private final boolean w;

      public MineTask(BlockPos var2, int var3) {
         this.b = var2;
         BlockState var4 = MinecraftInstance.mc.level.getBlockState(var2);
         this.c = var4;
         boolean var5;
         if (this.c.getBlock() == Blocks.BEDROCK) {
            var5 = true;
         } else {
            var5 = false;
         }

         this.w = var5;
         this.d = var3;
         this.w();
      }

      public BlockPos a() {
         return this.b;
      }

      public BlockState b() {
         return this.c;
      }

      public float c() {
         return this.e;
      }

      public int d() {
         return this.h;
      }

      public boolean e() {
         return this.i;
      }

      public boolean f() {
         return this.k;
      }

      public int g() {
         return this.d;
      }

      public boolean h() {
         return this.u;
      }

      public void a_void_bool(boolean var1) {
         this.u = var1;
      }

      public boolean i() {
         return this.v;
      }

      public void b_void_bool(boolean var1) {
         this.v = var1;
      }

      public boolean j() {
         return this.m;
      }

      private int a_int_BlockState_int(BlockState var1, int var2) {
         AutoMine var3;
         if (Night.moduleManager != null) {
            var3 = Night.moduleManager.getModule(AutoMine.class);
         } else {
            var3 = null;
         }

         AutoMine var4 = var3;
         if (var4 != null && var4.isEnabled() && var4.ironPickaxe.getValue() && this.r) {
            int var5 = InventoryUtil.a_int_Item_int_int(Items.IRON_PICKAXE, InventoryUtil.c, InventoryUtil.d);
            if (var5 != -1) {
               return var5;
            }
         }

         return var2;
      }

      private long q() {
         long var1 = SpeedMine.this.fast.getValue().longValue();
         long var3 = 50L;
         long var5 = var1 * var3;
         if (this.b == null) {
            return var5;
         }

         SpeedMine.TargetMineEntry var7 = SpeedMine.this.H();
         if (var7 != null && var7.player() != null) {
            boolean var13;
            label58: {
               int var8 = this.b.getY();
               int var9 = Mth.floor(var7.player().getY()) + 2;
               if (var8 == var9) {
                  SpeedMine var10 = SpeedMine.this;
                  BlockPos var11 = this.b;
                  Player var12 = var7.player();
                  if (var10.a_bool_BlockPos_Player(var11, var12)) {
                     var13 = true;
                     break label58;
                  }
               }

               var13 = false;
            }

            if (!var13) {
               return var5;
            }

            AutoTrap var14;
            if (Night.moduleManager != null) {
               var14 = Night.moduleManager.getModule(AutoTrap.class);
            } else {
               var14 = null;
            }

            AutoTrap var15 = var14;
            AutoMine var16;
            if (Night.moduleManager != null) {
               var16 = Night.moduleManager.getModule(AutoMine.class);
            } else {
               var16 = null;
            }

            AutoMine var17 = var16;
            boolean var18;
            if (var15 != null && var15.isEnabled()) {
               var18 = true;
            } else {
               var18 = false;
            }

            boolean var19 = var18;
            boolean var20;
            if (var17 != null && var17.isEnabled() && var17.priority.getValue().equalsIgnoreCase("Head")) {
               var20 = true;
            } else {
               var20 = false;
            }

            boolean var21 = var20;
            if (var19 && var21) {
               long var22 = var5;
               long var24 = Night.serverManager.getPing();
               return Math.max(var22, var24);
            } else {
               return var5;
            }
         } else {
            return var5;
         }
      }

      private void r() {
         AutoMine var1;
         if (Night.moduleManager != null) {
            var1 = Night.moduleManager.getModule(AutoMine.class);
         } else {
            var1 = null;
         }

         AutoMine var2 = var1;
         boolean var3;
         if (var2 != null && var2.isEnabled() && var2.ironPickaxe.getValue()) {
            var3 = true;
         } else {
            var3 = false;
         }

         if (var3 && !this.r) {
            int var4 = InventoryUtil.a_int_Item_int_int(Items.IRON_PICKAXE, InventoryUtil.c, InventoryUtil.f);
            if (var4 != -1 && this.c != null && !this.c.isAir()) {
               BlockState var5 = this.c;
               int var6 = var4;
               float var7 = BlockInteractionUtil.a_float_BlockState_int(var5, var6);
               float var8 = Night.worldStateManager.getTimerMultiplier();
               float var9 = var7 / var8;
               float var10 = this.q;
               float var11 = var9;
               float var12 = var10 + var11;
               float var13 = this.F();
               float var14 = Mth.clamp(var12, 0.0F, var13);
               this.q = var14;
               float var15 = this.q;
               float var16 = this.F();
               if (var15 >= var16) {
                  int var17 = InventoryUtil.a_int_BlockState_int_int(this.c, InventoryUtil.c, InventoryUtil.d);
                  if (var17 == -1) {
                     var17 = MinecraftInstance.mc.player.getInventory().getSelectedSlot();
                  }

                  if (var4 > InventoryUtil.d) {
                     int var18 = var4;
                     int var19 = var17;
                     InventoryUtil.c("Swap", var18, var19);
                     this.r = true;
                     int var20 = var4;
                     this.s = var20;
                     int var21 = var17;
                     this.t = var21;
                  } else if (var4 != -1) {
                     this.r = true;
                     this.s = -1;
                     int var22 = var4;
                     this.t = var22;
                  }
               }
            }
         }
      }

      public void k() {
         if (this.r) {
            if (this.s > InventoryUtil.d && this.t >= 0 && this.t <= InventoryUtil.d) {
               if (MinecraftInstance.mc.player != null) {
                  Inventory var1 = MinecraftInstance.mc.player.getInventory();
                  int var2 = this.t;
                  if (var1.getItem(var2).is(Items.IRON_PICKAXE)) {
                     int var3 = this.s;
                     int var4 = this.t;
                     InventoryUtil.c("Swap", var3, var4);
                  }
               }
            } else if (MinecraftInstance.mc.player != null) {
               int var5 = InventoryUtil.a_int_Item_int_int(Items.IRON_PICKAXE, InventoryUtil.c, InventoryUtil.d);
               if (var5 != -1) {
                  BlockState var6;
                  if (this.c != null && !this.c.isAir()) {
                     var6 = this.c;
                  } else {
                     var6 = Blocks.OBSIDIAN.defaultBlockState();
                  }

                  BlockState var7 = var6;
                  int var8;
                  if (this.s > InventoryUtil.d) {
                     var8 = this.s;
                  } else {
                     BlockState var9 = var7;
                     int var10 = InventoryUtil.d + 1;
                     var8 = InventoryUtil.a_int_BlockState_int_int(var9, var10, InventoryUtil.f);
                  }

                  int var11 = var8;
                  if (var11 == -1) {
                     var11 = InventoryUtil.d(InventoryUtil.d + 1, InventoryUtil.f);
                  }

                  if (var11 != -1 && var11 > InventoryUtil.d) {
                     int var12 = var11;
                     int var13 = var5;
                     InventoryUtil.c("Swap", var12, var13);
                  }
               }
            }

            this.r = false;
            this.s = -1;
            this.t = -1;
            this.q = 0.0F;
         }
      }

      SpeedMine.FadeAnimation s() {
         this.k();
         if (!this.m) {
            return null;
         }

         ClientLevel var1 = MinecraftInstance.mc.level;
         BlockPos var2 = this.b;
         BlockState var3 = var1.getBlockState(var2);
         if (var3.canBeReplaced()) {
            return null;
         }

         if (SpeedMine.this.s6b.getValue() && var3.getBlock() == Blocks.BEDROCK) {
            return null;
         }

         if (!this.k) {
            float var4 = this.e;
            float var5 = this.F();
            if (var4 < var5) {
               Direction var6 = BlockInteractionUtil.c(this.b, true);
               int var7;
               if (SpeedMine.this.switchSetting.getValue().equalsIgnoreCase("None")) {
                  var7 = -1;
               } else {
                  BlockState var8 = var3;
                  int var9 = InventoryUtil.c;
                  int var10;
                  if (!SpeedMine.this.switchSetting.getValue().equalsIgnoreCase("AltSwap")
                     && !SpeedMine.this.switchSetting.getValue().equalsIgnoreCase("AltPickup")) {
                     var10 = InventoryUtil.d;
                  } else {
                     var10 = InventoryUtil.f;
                  }

                  var7 = InventoryUtil.a_int_BlockState_int_int(var8, var9, var10);
               }

               int var11 = (byte)var7;
               if (var11 == -1) {
                  var11 = MinecraftInstance.mc.player.getInventory().getSelectedSlot();
               }

               Direction var12 = var6;
               int var13 = var11;
               this.a_void_Direction_int_bool(var12, var13, true);
            }
         }

         SpeedMine var14 = SpeedMine.this;
         BlockPos var15 = this.b;
         int var16 = this.d;
         BlockState var17 = var3;
         float var18 = this.e;
         return var14.new FadeAnimation(var15, var16, var17, var18);
      }

      void t() {
         int var1 = this.n + 1;
         this.n = var1;
      }

      private boolean u() {
         int var1 = this.n;
         int var2 = this.o;
         if (var1 != var2) {
            this.p.reset();
            int var3 = this.n;
            this.o = var3;
         }

         TimerUtil var4 = this.p;
         Long var5 = 200L;
         return var4.passedMs(var5);
      }

      private boolean v() {
         ClientLevel var1 = MinecraftInstance.mc.level;
         BlockPos var2 = this.b;
         boolean var3;
         if (var1.getBlockState(var2).canBeReplaced() && this.u()) {
            var3 = true;
         } else {
            var3 = false;
         }

         return var3;
      }

      void w() {
         synchronized (SpeedMine.this.H) {
            if (SpeedMine.this.J()) {
               this.m();
            }
         }
      }

      private boolean x() {
         if (!this.k || !SpeedMine.this.none.getValue().equalsIgnoreCase("Fast")) {
            return false;
         }

         if (SpeedMine.this.s6b.getValue() && this.c.getBlock() == Blocks.BEDROCK) {
            return false;
         }

         if (this.n == 0 && !this.u()) {
            return true;
         }

         if (SpeedMine.this.farReach.getValue()) {
            this.w();
         } else {
            this.m();
         }

         return true;
      }

      public boolean l() {
         SpeedMine var1 = SpeedMine.this;
         BlockPos var2 = this.b;
         if (var1.d_bool_BlockPos(var2)) {
            this.n();
            return true;
         }

         this.r();
         boolean var3;
         if (MinecraftInstance.mc.player == null || !MinecraftInstance.mc.player.isUsingItem() && !EntityUtil.a()) {
            var3 = false;
         } else {
            var3 = true;
         }

         boolean var4 = var3;
         if (SpeedMine.this.ag && !var4) {
            long var5 = System.currentTimeMillis();
            long var7 = SpeedMine.this.ah;
            long var9 = var5 - var7;
            long var11 = 750L;
            if (var9 >= var11) {
               SpeedMine var13 = SpeedMine.this;
               boolean var14 = false;
               var13.c_void_bool(var14);
            }
         }

         if (SpeedMine.this.ai && SpeedMine.this.P()) {
            SpeedMine var15 = SpeedMine.this;
            boolean var16 = false;
            var15.ai = var16;
            SpeedMine.MineTask var17 = this;
            boolean var18 = false;
            var17.m = var18;
         }

         if (!this.m) {
            ClientLevel var19 = MinecraftInstance.mc.level;
            BlockPos var20 = this.b;
            if (var19.getBlockState(var20).canBeReplaced()) {
               return true;
            }

            SpeedMine.MineTask var222 = this;
            var222.w();
            return false;
         } else {
            if (!SpeedMine.this.farReach.getValue()) {
               return this.y();
            }

            ClientLevel var21 = MinecraftInstance.mc.level;
            BlockPos var22 = this.b;
            if (var21.getBlockState(var22).canBeReplaced()) {
               if (this.n == 0) {
                  this.t();
               }

               if (SpeedMine.this.s6b.getValue()) {
                  Block var23 = this.c.getBlock();
                  Block var24 = Blocks.BEDROCK;
                  if (var23 == var24) {
                     SpeedMine.MineTask var25 = this;
                     boolean var26 = true;
                     var25.k = var26;
                     return false;
                  }
               }

               String var164 = SpeedMine.this.none.getValue();
               String var165 = "Fast";
               if (var164.equalsIgnoreCase(var165)) {
                  SpeedMine.MineTask var166 = this;
                  float var167 = 0.0F;
                  var166.e = var167;
                  SpeedMine.MineTask var168 = this;
                  float var169 = 0.0F;
                  var168.f = var169;
               }

               if (this.k) {
                  if (SpeedMine.this.fast3.getValue()) {
                     String var170 = SpeedMine.this.none.getValue();
                     String var171 = "Fast";
                     if (!var170.equalsIgnoreCase(var171)) {
                        if ((SpeedMine.this.whileEating.getValue() || !EntityUtil.a()) && !this.C()) {
                           TimerUtil var172 = SpeedMine.this.Z;
                           Long var173 = this.q();
                           if (var172.passedMs(var173)) {
                              BlockPos var174 = this.b;
                              boolean var175 = true;
                              Direction var176 = BlockInteractionUtil.c(var174, var175);
                              String var177 = SpeedMine.this.switchSetting.getValue();
                              String var178 = "None";
                              int var179;
                              if (var177.equalsIgnoreCase(var178)) {
                                 var179 = -1;
                              } else {
                                 BlockState var180;
                                 int var181;
                                 int var186;
                                 label178: {
                                    var180 = this.c;
                                    var181 = InventoryUtil.c;
                                    String var182 = SpeedMine.this.switchSetting.getValue();
                                    String var183 = "AltSwap";
                                    if (!var182.equalsIgnoreCase(var183)) {
                                       String var184 = SpeedMine.this.switchSetting.getValue();
                                       String var185 = "AltPickup";
                                       if (!var184.equalsIgnoreCase(var185)) {
                                          var186 = InventoryUtil.d;
                                          break label178;
                                       }
                                    }

                                    var186 = InventoryUtil.f;
                                 }

                                 var179 = InventoryUtil.a_int_BlockState_int_int(var180, var181, var186);
                              }

                              int var187 = var179;
                              int var188 = var187;
                              byte var189 = -1;
                              if (var188 == var189) {
                                 var187 = MinecraftInstance.mc.player.getInventory().getSelectedSlot();
                              }

                              SpeedMine.MineTask var190 = this;
                              Direction var191 = var176;
                              SpeedMine.MineTask var192 = this;
                              BlockState var193 = this.c;
                              int var194 = var187;
                              int var195 = var192.a_int_BlockState_int(var193, var194);
                              boolean var196 = false;
                              boolean var197 = false;
                              var190.a(var191, var195, var196, var197);
                              SpeedMine.this.Z.reset();
                              SpeedMine.MineTask var198 = this;
                              int var199 = var198.g;
                              byte var200 = 1;
                              int var201 = var199 + var200;
                              var198.g = var201;
                           }
                        }

                        return false;
                     }
                  }

                  if (this.v()) {
                     return false;
                  }

                  boolean var214;
                  label193: {
                     long var202 = SpeedMine.this.fast2.getValue().longValue();
                     long var204 = var202;
                     long var206 = 0L;
                     if (var204 > var206) {
                        TimerUtil var208 = SpeedMine.this.Z;
                        long var209 = var202;
                        long var211 = 50L;
                        Long var213 = var209 * var211;
                        if (var208.passedMs(var213)) {
                           var214 = true;
                           break label193;
                        }
                     }

                     var214 = false;
                  }

                  if (!var214) {
                     return false;
                  }
               }

               if (SpeedMine.this.P()) {
                  SpeedMine var215 = SpeedMine.this;
                  LocalPlayer var217 = MinecraftInstance.mc.player;
                  Inventory var218 = var217.getInventory();
                  int var219 = var218.getSelectedSlot();
                  ServerboundSetCarriedItemPacket var216 = new ServerboundSetCarriedItemPacket(var219);
                  var215.a_void_Packet(var216);
               }

               SpeedMine.MineTask var220 = this;
               var220.n();
               return true;
            } else {
               BlockPos var27 = this.b;
               boolean var28 = true;
               Direction var29 = BlockInteractionUtil.c(var27, var28);
               ClientLevel var30 = MinecraftInstance.mc.level;
               BlockPos var31 = this.b;
               BlockState var32 = var30.getBlockState(var31);
               if (!var32.canBeReplaced() && this.c.canBeReplaced()) {
                  this.p.reset();
               }

               if (!var32.canBeReplaced()) {
                  Block var33 = var32.getBlock();
                  Block var34 = this.c.getBlock();
                  if (var33 != var34) {
                     SpeedMine.MineTask var35 = this;
                     BlockState var36 = var32;
                     var35.c = var36;
                  }
               }

               if (this.i) {
                  if (this.x()) {
                     return false;
                  }

                  SpeedMine.MineTask var37 = this;
                  int var38 = var37.h;
                  byte var39 = 1;
                  int var40 = var38 + var39;
                  var37.h = var40;
                  String var41 = SpeedMine.this.switchSetting.getValue();
                  String var42 = "None";
                  int var43;
                  if (var41.equalsIgnoreCase(var42)) {
                     var43 = -1;
                  } else {
                     BlockState var44;
                     int var45;
                     int var50;
                     label258: {
                        var44 = this.c;
                        var45 = InventoryUtil.c;
                        String var46 = SpeedMine.this.switchSetting.getValue();
                        String var47 = "AltSwap";
                        if (!var46.equalsIgnoreCase(var47)) {
                           String var48 = SpeedMine.this.switchSetting.getValue();
                           String var49 = "AltPickup";
                           if (!var48.equalsIgnoreCase(var49)) {
                              var50 = InventoryUtil.d;
                              break label258;
                           }
                        }

                        var50 = InventoryUtil.f;
                     }

                     var43 = InventoryUtil.a_int_BlockState_int_int(var44, var45, var50);
                  }

                  int var51 = var43;
                  int var52 = var51;
                  byte var53 = -1;
                  if (var52 == var53) {
                     var51 = MinecraftInstance.mc.player.getInventory().getSelectedSlot();
                  }

                  float var56;
                  label250: {
                     if (SpeedMine.this.s6b.getValue()) {
                        Block var54 = this.c.getBlock();
                        Block var55 = Blocks.BEDROCK;
                        if (var54 == var55) {
                           var56 = 0.025F;
                           break label250;
                        }
                     }

                     BlockState var57 = this.c;
                     int var58 = var51;
                     float var59 = BlockInteractionUtil.a_float_BlockState_int(var57, var58);
                     float var60 = Night.worldStateManager.getTimerMultiplier();
                     var56 = var59 / var60;
                  }

                  float var61 = var56;
                  if (this.k) {
                     SpeedMine.MineTask var62 = this;
                     float var63 = this.F();
                     var62.f = var63;
                     SpeedMine.MineTask var64 = this;
                     float var65 = this.F();
                     var64.e = var65;
                  } else {
                     SpeedMine.MineTask var66 = this;
                     float var67 = this.e;
                     var66.f = var67;
                     SpeedMine.MineTask var68 = this;
                     float var69 = this.e;
                     float var70 = var61;
                     float var71 = var69 + var70;
                     float var72 = 0.0F;
                     float var73 = this.F();
                     float var74 = Mth.clamp(var71, var72, var73);
                     var68.e = var74;
                  }

                  if (SpeedMine.this.s6b.getValue()) {
                     Block var75 = this.c.getBlock();
                     Block var76 = Blocks.BEDROCK;
                     if (var75 == var76) {
                        if (SpeedMine.this.P()) {
                           SpeedMine var77 = SpeedMine.this;
                           Packet var78 = PacketAndRenderUtil.a_Packet_InteractionHand(InteractionHand.MAIN_HAND);
                           var77.a_void_Packet(var78);
                        } else {
                           ClientPacketListener var79 = MinecraftInstance.mc.getConnection();
                           Packet var80 = PacketAndRenderUtil.a_Packet_InteractionHand(InteractionHand.MAIN_HAND);
                           var79.send(var80);
                        }

                        LocalPlayer var81 = MinecraftInstance.mc.player;
                        InteractionHand var82 = InteractionHand.MAIN_HAND;
                        PacketAndRenderUtil.a(var81, var82);
                     }
                  }

                  String var83 = SpeedMine.this.rotate.getValue();
                  String var84 = "Normal";
                  if (var83.equalsIgnoreCase(var84)) {
                     float var85 = this.e;
                     float var86 = var61;
                     float var87 = 2.0F;
                     float var88 = var86 * var87;
                     float var89 = var85 + var88;
                     float var90 = this.F();
                     if (var89 >= var90) {
                        BlockPos var91 = this.b;
                        Direction var92 = var29;
                        float[] var93 = RotationMathUtil.a(BlockInteractionUtil.b_Vec3_BlockPos_Direction(var91, var92));
                        if (SpeedMine.this.P()) {
                           SpeedMine var94 = SpeedMine.this;
                           double var96 = MinecraftInstance.mc.player.getX();
                           double var98 = MinecraftInstance.mc.player.getY();
                           double var100 = MinecraftInstance.mc.player.getZ();
                           float[] var102 = var93;
                           byte var103 = 0;
                           float var104 = var102[var103];
                           float[] var105 = var93;
                           byte var106 = 1;
                           float var107 = var105[var106];
                           boolean var108 = MinecraftInstance.mc.player.onGround();
                           boolean var109 = MinecraftInstance.mc.player.horizontalCollision;
                           PosRot var95 = new PosRot(var96, var98, var100, var104, var107, var108, var109);
                           var94.a_void_Packet(var95);
                        } else {
                           RotationManager var110 = Night.rotationManager;
                           float[] var111 = var93;
                           RotationManager var112 = Night.rotationManager;
                           SpeedMine var113 = SpeedMine.this;
                           int var114 = var112.getModulePriority(var113);
                           var110.rotate(var111, var114);
                        }
                     }
                  }

                  boolean var115;
                  if (MinecraftInstance.mc.player == null || !MinecraftInstance.mc.player.isUsingItem() && !EntityUtil.a()) {
                     var115 = false;
                  } else {
                     var115 = true;
                  }

                  boolean var116 = var115;
                  boolean var117;
                  if (!SpeedMine.this.ag && (SpeedMine.this.whileEating.getValue() || !var116)) {
                     var117 = false;
                  } else {
                     var117 = true;
                  }

                  boolean var118 = var117;
                  float var119 = this.e;
                  float var120 = this.F();
                  if (var119 >= var120 && !var32.canBeReplaced() && !var118) {
                     if (this.k && this.n == 0 && this.u()) {
                        this.m();
                        return false;
                     }

                     if (this.k && !SpeedMine.this.fast3.getValue()) {
                        TimerUtil var121 = SpeedMine.this.Z;
                        long var122 = SpeedMine.this.fast.getValue().longValue();
                        long var124 = 50L;
                        Long var126 = var122 * var124;
                        if (!var121.passedMs(var126)) {
                           return false;
                        }
                     }

                     if (!this.B()) {
                        SpeedMine.MineTask var127 = this;
                        Direction var128 = var29;
                        SpeedMine.MineTask var129 = this;
                        BlockState var130 = this.c;
                        int var131 = var51;
                        int var132 = var129.a_int_BlockState_int(var130, var131);
                        boolean var133 = false;
                        boolean var134;
                        if (!this.k) {
                           var134 = true;
                        } else {
                           var134 = false;
                        }

                        var127.a(var128, var132, var133, var134);
                        if (!this.k) {
                           SpeedMine.this.ab.reset();
                        }

                        SpeedMine.MineTask var135 = this;
                        int var136 = var135.g;
                        byte var137 = 1;
                        int var138 = var136 + var137;
                        var135.g = var138;
                        String var139 = SpeedMine.this.none.getValue();
                        String var140 = "None";
                        if ((!var139.equalsIgnoreCase(var140) || SpeedMine.this.s6b.getValue()) && !this.u) {
                           SpeedMine.MineTask var146 = this;
                           boolean var147 = true;
                           var146.k = var147;
                           String var148 = SpeedMine.this.none.getValue();
                           String var149 = "Fast";
                           if (var148.equalsIgnoreCase(var149)) {
                              SpeedMine.MineTask var150 = this;
                              float var151 = 0.0F;
                              var150.e = var151;
                              SpeedMine.MineTask var152 = this;
                              float var153 = 0.0F;
                              var152.f = var153;
                           }

                           SpeedMine.this.Z.reset();
                           this.p.reset();
                        } else {
                           SpeedMine.MineTask var141 = this;
                           boolean var142 = false;
                           var141.i = var142;
                           SpeedMine.MineTask var143 = this;
                           long var144 = System.currentTimeMillis();
                           var143.j = var144;
                        }
                     }

                     return false;
                  }
               } else {
                  ClientLevel var154 = MinecraftInstance.mc.level;
                  BlockPos var155 = this.b;
                  if (!var154.getBlockState(var155).canBeReplaced()) {
                     if (this.g != 0) {
                        long var156 = System.currentTimeMillis();
                        long var158 = this.j;
                        long var160 = var156 - var158;
                        long var162 = 150L;
                        if (var160 < var162) {
                           return false;
                        }
                     }

                     this.w();
                  }
               }

               return false;
            }
         }
      }

      private boolean y() {
         this.r();
         ClientLevel var1 = MinecraftInstance.mc.level;
         BlockPos var2 = this.b;
         if (var1.getBlockState(var2).canBeReplaced()) {
            if (this.n == 0) {
               this.t();
            }

            if (SpeedMine.this.s6b.getValue() && this.c.getBlock() == Blocks.BEDROCK) {
               this.k = true;
               return false;
            }

            if (this.k) {
               if (SpeedMine.this.fast3.getValue() && !SpeedMine.this.none.getValue().equalsIgnoreCase("Fast")) {
                  if ((SpeedMine.this.whileEating.getValue() || !EntityUtil.a()) && !this.C()) {
                     TimerUtil var103 = SpeedMine.this.Z;
                     Long var104 = this.q();
                     if (var103.passedMs(var104)) {
                        Direction var105 = BlockInteractionUtil.c(this.b, true);
                        int var106;
                        if (SpeedMine.this.switchSetting.getValue().equalsIgnoreCase("None")) {
                           var106 = -1;
                        } else {
                           BlockState var107;
                           int var108;
                           int var113;
                           label131: {
                              var107 = this.c;
                              var108 = InventoryUtil.c;
                              String var109 = SpeedMine.this.switchSetting.getValue();
                              String var110 = "AltSwap";
                              if (!var109.equalsIgnoreCase(var110)) {
                                 String var111 = SpeedMine.this.switchSetting.getValue();
                                 String var112 = "AltPickup";
                                 if (!var111.equalsIgnoreCase(var112)) {
                                    var113 = InventoryUtil.d;
                                    break label131;
                                 }
                              }

                              var113 = InventoryUtil.f;
                           }

                           var106 = InventoryUtil.a_int_BlockState_int_int(var107, var108, var113);
                        }

                        int var128 = var106;
                        if (var128 == -1) {
                           var128 = MinecraftInstance.mc.player.getInventory().getSelectedSlot();
                        }

                        Direction var129 = var105;
                        BlockState var130 = this.c;
                        int var131 = var128;
                        int var132 = this.a_int_BlockState_int(var130, var131);
                        this.a_void_Direction_int(var129, var132);
                        SpeedMine.this.Z.reset();
                        int var133 = this.g;
                        int var134 = var133 + 1;
                        this.g = var134;
                     }
                  }

                  return false;
               }

               if (this.v()) {
                  return false;
               }

               boolean var124;
               label146: {
                  long var114 = SpeedMine.this.fast2.getValue().longValue();
                  long var116 = 0L;
                  if (var114 > var116) {
                     TimerUtil var118 = SpeedMine.this.Z;
                     long var119 = var114;
                     long var121 = 50L;
                     Long var123 = var119 * var121;
                     if (var118.passedMs(var123)) {
                        var124 = true;
                        break label146;
                     }
                  }

                  var124 = false;
               }

               if (!var124) {
                  return false;
               }
            }

            if (SpeedMine.this.P()) {
               SpeedMine var125 = SpeedMine.this;
               int var127 = MinecraftInstance.mc.player.getInventory().getSelectedSlot();
               ServerboundSetCarriedItemPacket var126 = new ServerboundSetCarriedItemPacket(var127);
               var125.a_void_Packet(var126);
            }

            this.n();
            return true;
         } else {
            Direction var3 = BlockInteractionUtil.c(this.b, true);
            ClientLevel var4 = MinecraftInstance.mc.level;
            BlockPos var5 = this.b;
            BlockState var6 = var4.getBlockState(var5);
            if (!var6.canBeReplaced() && this.c.canBeReplaced()) {
               this.p.reset();
            }

            if (!var6.canBeReplaced()) {
               Block var7 = var6.getBlock();
               Block var8 = this.c.getBlock();
               if (var7 != var8) {
                  SpeedMine.MineTask var9 = this;
                  BlockState var10 = var6;
                  var9.c = var10;
               }
            }

            if (this.i) {
               if (this.x()) {
                  return false;
               }

               SpeedMine.MineTask var11 = this;
               int var12 = var11.h;
               byte var13 = 1;
               int var14 = var12 + var13;
               var11.h = var14;
               int var15;
               if (SpeedMine.this.switchSetting.getValue().equalsIgnoreCase("None")) {
                  var15 = -1;
               } else {
                  BlockState var16;
                  int var17;
                  int var22;
                  label206: {
                     var16 = this.c;
                     var17 = InventoryUtil.c;
                     String var18 = SpeedMine.this.switchSetting.getValue();
                     String var19 = "AltSwap";
                     if (!var18.equalsIgnoreCase(var19)) {
                        String var20 = SpeedMine.this.switchSetting.getValue();
                        String var21 = "AltPickup";
                        if (!var20.equalsIgnoreCase(var21)) {
                           var22 = InventoryUtil.d;
                           break label206;
                        }
                     }

                     var22 = InventoryUtil.f;
                  }

                  var15 = InventoryUtil.a_int_BlockState_int_int(var16, var17, var22);
               }

               int var23 = var15;
               if (var23 == -1) {
                  var23 = MinecraftInstance.mc.player.getInventory().getSelectedSlot();
               }

               float var24;
               if (SpeedMine.this.s6b.getValue() && this.c.getBlock() == Blocks.BEDROCK) {
                  var24 = 0.025F;
               } else {
                  BlockState var25 = this.c;
                  int var26 = var23;
                  float var27 = BlockInteractionUtil.a_float_BlockState_int(var25, var26);
                  float var28 = Night.worldStateManager.getTimerMultiplier();
                  var24 = var27 / var28;
               }

               float var29 = var24;
               if (this.k) {
                  SpeedMine.MineTask var30 = this;
                  float var31 = this.F();
                  var30.f = var31;
                  float var32 = this.F();
                  this.e = var32;
               } else {
                  SpeedMine.MineTask var33 = this;
                  float var34 = this.e;
                  var33.f = var34;
                  SpeedMine.MineTask var35 = this;
                  float var36 = this.e;
                  float var37 = var29;
                  float var38 = var36 + var37;
                  float var39 = 0.0F;
                  float var40 = this.F();
                  float var41 = Mth.clamp(var38, var39, var40);
                  var35.e = var41;
               }

               if (SpeedMine.this.s6b.getValue() && this.c.getBlock() == Blocks.BEDROCK) {
                  if (SpeedMine.this.P()) {
                     SpeedMine var42 = SpeedMine.this;
                     Packet var43 = PacketAndRenderUtil.a_Packet_InteractionHand(InteractionHand.MAIN_HAND);
                     var42.a_void_Packet(var43);
                  } else {
                     ClientPacketListener var44 = MinecraftInstance.mc.getConnection();
                     Packet var45 = PacketAndRenderUtil.a_Packet_InteractionHand(InteractionHand.MAIN_HAND);
                     var44.send(var45);
                  }

                  if (MinecraftInstance.mc.player == null
                     || !MinecraftInstance.mc.player.isUsingItem()
                     || MinecraftInstance.mc.player.getUsedItemHand() != InteractionHand.MAIN_HAND) {
                     PacketAndRenderUtil.a(MinecraftInstance.mc.player, InteractionHand.MAIN_HAND);
                  }
               }

               if (SpeedMine.this.rotate.getValue().equalsIgnoreCase("Normal")) {
                  float var46 = this.e;
                  float var47 = var29;
                  float var48 = 2.0F;
                  float var49 = var47 * var48;
                  float var50 = var46 + var49;
                  float var51 = this.F();
                  if (var50 >= var51) {
                     BlockPos var52 = this.b;
                     Direction var53 = var3;
                     float[] var54 = RotationMathUtil.a(BlockInteractionUtil.b_Vec3_BlockPos_Direction(var52, var53));
                     if (SpeedMine.this.P()) {
                        SpeedMine var55 = SpeedMine.this;
                        double var57 = MinecraftInstance.mc.player.getX();
                        double var59 = MinecraftInstance.mc.player.getY();
                        double var61 = MinecraftInstance.mc.player.getZ();
                        float var63 = var54[0];
                        float var64 = var54[1];
                        boolean var65 = MinecraftInstance.mc.player.onGround();
                        boolean var66 = MinecraftInstance.mc.player.horizontalCollision;
                        PosRot var56 = new PosRot(var57, var59, var61, var63, var64, var65, var66);
                        var55.a_void_Packet(var56);
                     } else {
                        RotationManager var67 = Night.rotationManager;
                        float[] var68 = var54;
                        RotationManager var69 = Night.rotationManager;
                        SpeedMine var70 = SpeedMine.this;
                        int var71 = var69.getModulePriority(var70);
                        var67.rotate(var68, var71);
                     }
                  }
               }

               float var72 = this.e;
               float var73 = this.F();
               if (var72 >= var73 && !var6.canBeReplaced() && (SpeedMine.this.whileEating.getValue() || !EntityUtil.a())) {
                  if (this.k && this.n == 0 && this.u()) {
                     this.m();
                     return false;
                  }

                  if (this.k && !SpeedMine.this.fast3.getValue()) {
                     TimerUtil var74 = SpeedMine.this.Z;
                     long var75 = SpeedMine.this.fast.getValue().longValue();
                     long var77 = 50L;
                     Long var79 = var75 * var77;
                     if (!var74.passedMs(var79)) {
                        return false;
                     }
                  }

                  if (!this.B()) {
                     SpeedMine.MineTask var80 = this;
                     Direction var81 = var3;
                     SpeedMine.MineTask var82 = this;
                     BlockState var83 = this.c;
                     int var84 = var23;
                     int var85 = var82.a_int_BlockState_int(var83, var84);
                     var80.a_void_Direction_int(var81, var85);
                     SpeedMine.MineTask var86 = this;
                     int var87 = var86.g;
                     byte var88 = 1;
                     int var89 = var87 + var88;
                     var86.g = var89;
                     if ((!SpeedMine.this.none.getValue().equalsIgnoreCase("None") || SpeedMine.this.s6b.getValue()) && !this.u) {
                        this.k = true;
                        if (SpeedMine.this.none.getValue().equalsIgnoreCase("Fast")) {
                           this.e = 0.0F;
                           this.f = 0.0F;
                        }

                        SpeedMine.this.Z.reset();
                        this.p.reset();
                     } else {
                        this.i = false;
                        SpeedMine.MineTask var90 = this;
                        long var91 = System.currentTimeMillis();
                        var90.j = var91;
                     }
                  }

                  return false;
               }
            } else {
               ClientLevel var93 = MinecraftInstance.mc.level;
               BlockPos var94 = this.b;
               if (!var93.getBlockState(var94).canBeReplaced()) {
                  if (this.g != 0) {
                     long var95 = System.currentTimeMillis();
                     long var97 = this.j;
                     long var99 = var95 - var97;
                     long var101 = 150L;
                     if (var99 < var101) {
                        return false;
                     }
                  }

                  this.m();
               }
            }

            return false;
         }
      }

      private boolean z() {
         AutoTrap var1;
         if (Night.moduleManager != null) {
            var1 = Night.moduleManager.getModule(AutoTrap.class);
         } else {
            var1 = null;
         }

         AutoTrap var2 = var1;
         if (var2 != null && var2.isEnabled()) {
            AutoCrystal var3;
            if (Night.moduleManager != null) {
               var3 = Night.moduleManager.getModule(AutoCrystal.class);
            } else {
               var3 = null;
            }

            AutoCrystal var4 = var3;
            if (var4 != null && var4.isEnabled()) {
               SpeedMine.TargetMineEntry var5 = SpeedMine.this.H();
               if (var5 != null && var5.player() != null) {
                  int var6 = Mth.floor(var5.player().getY());
                  int var7 = this.b.getY();
                  int var8 = var6 + 2;
                  if (var7 >= var8) {
                     double var9 = this.b.getX();
                     double var11 = 0.5;
                     double var13 = var9 + var11;
                     double var15 = var5.player().getX();
                     double var17 = Math.abs(var13 - var15);
                     double var19 = 1.3;
                     if (var17 < var19) {
                        double var21 = this.b.getZ();
                        double var23 = 0.5;
                        double var25 = var21 + var23;
                        double var27 = var5.player().getZ();
                        double var29 = Math.abs(var25 - var27);
                        double var31 = 1.3;
                        if (var29 < var31) {
                           return true;
                        }
                     }
                  }

                  return false;
               } else {
                  return false;
               }
            } else {
               return false;
            }
         } else {
            return false;
         }
      }

      private boolean A() {
         AutoMine var1;
         if (Night.moduleManager != null) {
            var1 = Night.moduleManager.getModule(AutoMine.class);
         } else {
            var1 = null;
         }

         AutoMine var2 = var1;
         if (var2 != null && var2.isEnabled()) {
            AutoMine var3 = var2;
            BlockPos var4 = this.b;
            if (var3.a_bool_BlockPos(var4)) {
               return true;
            }
         }

         return false;
      }

      private boolean B() {
         AutoCrystal var1;
         if (Night.moduleManager != null) {
            var1 = Night.moduleManager.getModule(AutoCrystal.class);
         } else {
            var1 = null;
         }

         AutoCrystal var2 = var1;
         if (var2 != null && var2.isEnabled()) {
            AutoCrystal var3 = var2;
            BlockPos var4 = this.b;
            if (var3.b_bool_BlockPos(var4)) {
               return true;
            }
         }

         return false;
      }

      private boolean C() {
         boolean var1;
         if (!this.z() && !this.A()) {
            var1 = false;
         } else {
            var1 = true;
         }

         return var1;
      }

      private void a_void_BlockPos(BlockPos var1) {
         if (MinecraftInstance.mc.player != null && MinecraftInstance.mc.level != null && MinecraftInstance.mc.getConnection() != null) {
            if (this.k) {
               AutoTrap var2;
               if (Night.moduleManager != null) {
                  var2 = Night.moduleManager.getModule(AutoTrap.class);
               } else {
                  var2 = null;
               }

               AutoTrap var3 = var2;
               if (var3 != null && var3.isEnabled()) {
                  AutoCrystal var4;
                  if (Night.moduleManager != null) {
                     var4 = Night.moduleManager.getModule(AutoCrystal.class);
                  } else {
                     var4 = null;
                  }

                  AutoCrystal var5 = var4;
                  if (var5 != null && var5.isEnabled()) {
                     if (MinecraftInstance.mc.level.getBlockState(var1).getBlock() == Blocks.OBSIDIAN) {
                        SpeedMine.TargetMineEntry var6 = SpeedMine.this.H();
                        if (var6 != null && var6.player() != null) {
                           boolean var34;
                           label75: {
                              int var7 = Mth.floor(var6.player().getY());
                              int var8 = var1.getY();
                              int var9 = var7 + 2;
                              if (var8 >= var9) {
                                 double var10 = var1.getX();
                                 double var12 = 0.5;
                                 double var14 = var10 + var12;
                                 double var16 = var6.player().getX();
                                 double var18 = Math.abs(var14 - var16);
                                 double var20 = 1.3;
                                 if (var18 < var20) {
                                    double var22 = var1.getZ();
                                    double var24 = 0.5;
                                    double var26 = var22 + var24;
                                    double var28 = var6.player().getZ();
                                    double var30 = Math.abs(var26 - var28);
                                    double var32 = 1.3;
                                    if (var30 < var32) {
                                       var34 = true;
                                       break label75;
                                    }
                                 }
                              }

                              var34 = false;
                           }

                           if (var34) {
                              if (BlockInteractionUtil.d_bool_BlockPos(var1.above())) {
                                 int var35 = InventoryUtil.b_int_Item(Items.END_CRYSTAL);
                                 boolean var36;
                                 if (MinecraftInstance.mc.player.getOffhandItem().getItem() == Items.END_CRYSTAL) {
                                    var36 = true;
                                 } else {
                                    var36 = false;
                                 }

                                 boolean var37 = var36;
                                 if (var37 || var35 != -1) {
                                    InteractionHand var38;
                                    if (var37) {
                                       var38 = InteractionHand.OFF_HAND;
                                    } else {
                                       var38 = InteractionHand.MAIN_HAND;
                                    }

                                    InteractionHand var39 = var38;
                                    double var40 = var1.getX();
                                    double var42 = 0.5;
                                    double var44 = var40 + var42;
                                    double var46 = var1.getY();
                                    double var48 = 1.0;
                                    double var50 = var46 + var48;
                                    double var52 = var1.getZ();
                                    double var54 = 0.5;
                                    double var56 = var52 + var54;
                                    Vec3 var58 = new Vec3(var44, var50, var56);
                                    BlockHitResult var59 = new BlockHitResult(var58, Direction.UP, var1, false);
                                    if (SpeedMine.this.P()) {
                                       if (var37) {
                                          SpeedMine var60 = SpeedMine.this;
                                          InteractionHand var61 = var39;
                                          BlockHitResult var62 = var59;
                                          var60.a_void_IntFunction(var2x -> new ServerboundUseItemOnPacket(var61, var62, var2x));
                                          SpeedMine var63 = SpeedMine.this;
                                          Packet var64 = PacketAndRenderUtil.a_Packet_InteractionHand(var39);
                                          var63.a_void_Packet(var64);
                                       } else {
                                          int var69 = MinecraftInstance.mc.player.getInventory().getSelectedSlot();
                                          SpeedMine var70 = SpeedMine.this;
                                          int var71 = var35;
                                          var70.a_void_Packet(new ServerboundSetCarriedItemPacket(var71));
                                          SpeedMine var72 = SpeedMine.this;
                                          InteractionHand var73 = var39;
                                          BlockHitResult var74 = var59;
                                          var72.a_void_IntFunction(var2x -> new ServerboundUseItemOnPacket(var73, var74, var2x));
                                          SpeedMine var75 = SpeedMine.this;
                                          Packet var76 = PacketAndRenderUtil.a_Packet_InteractionHand(var39);
                                          var75.a_void_Packet(var76);
                                          SpeedMine var77 = SpeedMine.this;
                                          int var78 = var69;
                                          var77.a_void_Packet(new ServerboundSetCarriedItemPacket(var78));
                                       }
                                    } else if (var37) {
                                       InteractionHand var65 = var39;
                                       BlockHitResult var66 = var59;
                                       PacketPredictionUtil.a_void_SequencedPacketCreator(var2x -> new ServerboundUseItemOnPacket(var65, var66, var2x));
                                       ClientPacketListener var67 = MinecraftInstance.mc.getConnection();
                                       Packet var68 = PacketAndRenderUtil.a_Packet_InteractionHand(var39);
                                       var67.send(var68);
                                    } else {
                                       int var79 = MinecraftInstance.mc.player.getInventory().getSelectedSlot();
                                       int var80 = var35;
                                       int var81 = var79;
                                       InventoryUtil.a_bool_String_int_int("Silent", var80, var81);
                                       InteractionHand var82 = var39;
                                       BlockHitResult var83 = var59;
                                       PacketPredictionUtil.a_void_SequencedPacketCreator(var2x -> new ServerboundUseItemOnPacket(var82, var83, var2x));
                                       ClientPacketListener var84 = MinecraftInstance.mc.getConnection();
                                       Packet var85 = PacketAndRenderUtil.a_Packet_InteractionHand(var39);
                                       var84.send(var85);
                                       int var86 = var35;
                                       int var87 = var79;
                                       InventoryUtil.b("Silent", var86, var87);
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      private void a_void_Direction_int(Direction var1, int var2) {
         BlockPos var3 = this.b;
         this.a_void_BlockPos(var3);
         BlockPos var4 = this.b;
         Night.EVENT_BUS.post(new BlockPlaceEvent(var4));
         if (SpeedMine.this.rotate.getValue().equalsIgnoreCase("Silent")) {
            float[] var5 = RotationMathUtil.a(BlockInteractionUtil.b_Vec3_BlockPos_Direction(this.b, var1));
            if (SpeedMine.this.P()) {
               SpeedMine var6 = SpeedMine.this;
               float var7 = var5[0];
               float var8 = var5[1];
               boolean var9 = MinecraftInstance.mc.player.onGround();
               boolean var10 = MinecraftInstance.mc.player.horizontalCollision;
               var6.a_void_Packet(new Rot(var7, var8, var9, var10));
            } else {
               float[] var11 = var5;
               Night.rotationManager.rotateByMode("Silent", var11);
            }
         }

         int var12 = MinecraftInstance.mc.player.getInventory().getSelectedSlot();
         if (SpeedMine.this.P()) {
            int var13;
            if (SpeedMine.this.switchSetting.getValue().equalsIgnoreCase("None")) {
               var13 = -1;
            } else {
               var13 = var2;
            }

            int var14;
            boolean var17;
            label43: {
               var14 = var13;
               if (var14 != -1) {
                  int var15 = var14;
                  int var16 = var12;
                  if (var15 != var16) {
                     var17 = true;
                     break label43;
                  }
               }

               var17 = false;
            }

            boolean var18 = var17;
            if (var18) {
               SpeedMine var19 = SpeedMine.this;
               int var20 = var14;
               var19.a_void_Packet(new ServerboundSetCarriedItemPacket(var20));
            }

            SpeedMine var21 = SpeedMine.this;
            Packet var22 = PacketAndRenderUtil.a_Packet_InteractionHand(InteractionHand.MAIN_HAND);
            var21.a_void_Packet(var22);
            BlockPos var23 = this.b;
            this.a_void_BlockPos_Direction_bool(var23, var1, true);
            if (SpeedMine.this.grim.getValue()) {
               SpeedMine var24 = SpeedMine.this;
               BlockPos var25 = this.b.above(500);
               var24.a_void_Packet(new ServerboundPlayerActionPacket(Action.ABORT_DESTROY_BLOCK, var25, var1));
            }

            if (var18) {
               SpeedMine var26 = SpeedMine.this;
               int var27 = var12;
               var26.a_void_Packet(new ServerboundSetCarriedItemPacket(var27));
            }
         } else {
            String var28 = SpeedMine.this.switchSetting.getValue();
            int var29 = var12;
            InventoryUtil.a_bool_String_int_int(var28, var2, var29);
            ClientPacketListener var30 = MinecraftInstance.mc.getConnection();
            Packet var31 = PacketAndRenderUtil.a_Packet_InteractionHand(InteractionHand.MAIN_HAND);
            var30.send(var31);
            BlockPos var32 = this.b;
            this.a_void_BlockPos_Direction_bool(var32, var1, true);
            if (SpeedMine.this.grim.getValue()) {
               ClientPacketListener var33 = MinecraftInstance.mc.getConnection();
               BlockPos var34 = this.b.above(500);
               var33.send(new ServerboundPlayerActionPacket(Action.ABORT_DESTROY_BLOCK, var34, var1));
            }

            ClientLevel var35 = MinecraftInstance.mc.level;
            BlockPos var36 = this.b;
            if (var35.getBlockState(var36).getBlock() == Blocks.BEDROCK) {
               PacketAndRenderUtil.a(MinecraftInstance.mc.player, InteractionHand.MAIN_HAND);
            }

            String var37 = SpeedMine.this.switchSetting.getValue();
            int var38 = var12;
            InventoryUtil.b(var37, var2, var38);
         }
      }

      private void a_void_Direction_int_bool(Direction var1, int var2, boolean var3) {
         boolean var4;
         if (!var3) {
            var4 = true;
         } else {
            var4 = false;
         }

         this.a(var1, var2, var3, var4);
      }

      private void a(Direction var1, int var2, boolean var3, boolean var4) {
         if (!var3) {
            BlockPos var5 = this.b;
            this.a_void_BlockPos(var5);
            BlockPos var6 = this.b;
            Night.EVENT_BUS.post(new BlockPlaceEvent(var6));
         }

         if (SpeedMine.this.rotate.getValue().equalsIgnoreCase("Silent")) {
            float[] var7 = RotationMathUtil.a(BlockInteractionUtil.b_Vec3_BlockPos_Direction(this.b, var1));
            if (SpeedMine.this.P()) {
               SpeedMine var8 = SpeedMine.this;
               float var9 = var7[0];
               float var10 = var7[1];
               boolean var11 = MinecraftInstance.mc.player.onGround();
               boolean var12 = MinecraftInstance.mc.player.horizontalCollision;
               var8.a_void_Packet(new Rot(var9, var10, var11, var12));
            } else {
               float[] var13 = var7;
               Night.rotationManager.rotateByMode("Silent", var13);
            }
         }

         int var14 = MinecraftInstance.mc.player.getInventory().getSelectedSlot();
         if (SpeedMine.this.P()) {
            int var15;
            if (this.l != -1) {
               var15 = this.l;
            } else {
               var15 = var14;
            }

            int var16 = var15;
            int var17;
            if (SpeedMine.this.switchSetting.getValue().equalsIgnoreCase("None")) {
               var17 = -1;
            } else {
               var17 = var2;
            }

            int var18 = var17;
            if (var18 != -1) {
               SpeedMine var19 = SpeedMine.this;
               int var20 = var18;
               var19.a_void_Packet(new ServerboundSetCarriedItemPacket(var20));
               if (SpeedMine.this.switchSetting.getValue().equalsIgnoreCase("Normal")) {
                  Inventory var21 = MinecraftInstance.mc.player.getInventory();
                  int var22 = var18;
                  var21.setSelectedSlot(var22);
               }
            }

            SpeedMine var23 = SpeedMine.this;
            Packet var24 = PacketAndRenderUtil.a_Packet_InteractionHand(InteractionHand.MAIN_HAND);
            var23.a_void_Packet(var24);
            BlockPos var25 = this.b;
            boolean var26;
            if (!var3) {
               var26 = true;
            } else {
               var26 = false;
            }

            this.a(var25, var1, var26, var4);
            if (SpeedMine.this.grim.getValue()) {
               SpeedMine var27 = SpeedMine.this;
               BlockPos var28 = this.b.above(500);
               var27.a_void_Packet(new ServerboundPlayerActionPacket(Action.ABORT_DESTROY_BLOCK, var28, var1));
            }

            ClientLevel var29 = MinecraftInstance.mc.level;
            BlockPos var30 = this.b;
            if (var29.getBlockState(var30).getBlock() == Blocks.BEDROCK) {
               PacketAndRenderUtil.a(MinecraftInstance.mc.player, InteractionHand.MAIN_HAND);
            }

            if (var18 != -1) {
               SpeedMine var31 = SpeedMine.this;
               int var32 = var16;
               var31.a_void_Packet(new ServerboundSetCarriedItemPacket(var32));
               if (SpeedMine.this.switchSetting.getValue().equalsIgnoreCase("Normal")) {
                  Inventory var33 = MinecraftInstance.mc.player.getInventory();
                  int var34 = var16;
                  var33.setSelectedSlot(var34);
               }
            }
         } else {
            String var35 = SpeedMine.this.switchSetting.getValue();
            int var36 = var14;
            InventoryUtil.a_bool_String_int_int(var35, var2, var36);
            ClientPacketListener var37 = MinecraftInstance.mc.getConnection();
            Packet var38 = PacketAndRenderUtil.a_Packet_InteractionHand(InteractionHand.MAIN_HAND);
            var37.send(var38);
            BlockPos var39 = this.b;
            boolean var40;
            if (!var3) {
               var40 = true;
            } else {
               var40 = false;
            }

            this.a(var39, var1, var40, var4);
            if (SpeedMine.this.grim.getValue()) {
               ClientPacketListener var41 = MinecraftInstance.mc.getConnection();
               BlockPos var42 = this.b.above(500);
               var41.send(new ServerboundPlayerActionPacket(Action.ABORT_DESTROY_BLOCK, var42, var1));
            }

            ClientLevel var43 = MinecraftInstance.mc.level;
            BlockPos var44 = this.b;
            if (var43.getBlockState(var44).getBlock() == Blocks.BEDROCK) {
               PacketAndRenderUtil.a(MinecraftInstance.mc.player, InteractionHand.MAIN_HAND);
            }

            String var45 = SpeedMine.this.switchSetting.getValue();
            int var46 = var14;
            InventoryUtil.b(var45, var2, var46);
         }
      }

      private void a_void_BlockPos_Direction_bool(BlockPos var1, Direction var2, boolean var3) {
         this.a(var1, var2, var3, false);
      }

      private void a(BlockPos var1, Direction var2, boolean var3, boolean var4) {
         BlockStatePredictionHandler var5 = ((ClientWorldAccessor)MinecraftInstance.mc.level).invokeGetPendingUpdateManager().startPredicting();

         try {
            int var6 = var5.currentSequence();
            ServerboundPlayerActionPacket var7 = new ServerboundPlayerActionPacket(Action.STOP_DESTROY_BLOCK, var1, var2, var6);
            SpeedMine.this.d_void_bool(var4);
            if (SpeedMine.this.P()) {
               SpeedMine var8 = SpeedMine.this;
               ServerboundPlayerActionPacket var9 = var7;
               var8.a_void_Packet(var9);
            } else {
               ClientPacketListener var10 = MinecraftInstance.mc.getConnection();
               ServerboundPlayerActionPacket var11 = var7;
               var10.send(var11);
            }
         } catch (Throwable var17) {
            if (var5 != null) {
               try {
                  var5.close();
               } catch (Throwable var16) {
                  var17.addSuppressed(var16);
               }
            }

            throw var17;
         }

         if (var5 != null) {
            var5.close();
         }
      }

      public void a_void_PoseStack(PoseStack var1) {
         ClientLevel var2 = MinecraftInstance.mc.level;
         BlockPos var3 = this.b;
         if (!var2.getBlockState(var3).canBeReplaced() || this.k) {
            BlockPos var4 = this.b;
            AABB var5 = new AABB(var4);
            ClientLevel var6 = MinecraftInstance.mc.level;
            BlockPos var7 = this.b;
            boolean var8;
            if (var6.getBlockState(var7).canBeReplaced() && this.k) {
               var8 = true;
            } else {
               var8 = false;
            }

            double var11;
            if (var8) {
               float var9 = this.e;
               float var10 = this.F();
               var11 = var9 / var10;
            } else {
               float var13 = MinecraftInstance.mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
               float var14 = this.f;
               float var15 = this.F();
               float var16 = var14 / var15;
               float var17 = this.e;
               float var18 = this.F();
               float var19 = var17 / var18;
               var11 = Mth.clamp(Mth.lerp(var13, var16, var19), 0.0F, 1.0F);
            }

            double var20 = var11;
            if (SpeedMine.this.fill.getValue().equalsIgnoreCase("Expand")) {
               BlockPos var23 = this.b;
               AABB var22 = new AABB(var23);
               double var24 = 0.5;
               AABB var26 = var22.deflate(var24);
               double var27 = var20;
               double var29 = 2.0;
               double var31 = var27 / var29;
               double var33 = 0.0;
               double var35 = 0.5;
               double var37 = Mth.clamp(var31, var33, var35);
               var5 = var26.inflate(var37);
            }

            if (SpeedMine.this.fill.getValue().equalsIgnoreCase("Rise")) {
               double var39 = this.b.getX();
               double var41 = this.b.getY();
               double var43 = this.b.getZ();
               double var45 = this.b.getX();
               double var47 = 1.0;
               double var49 = var45 + var47;
               double var51 = this.b.getY();
               double var53 = var20;
               double var55 = var51 + var53;
               double var57 = this.b.getZ();
               double var59 = 1.0;
               double var61 = var57 + var59;
               var5 = new AABB(var39, var41, var43, var49, var55, var61);
            }

            Color var63 = SpeedMine.this.fill3.getColor();
            Color var64 = SpeedMine.this.outline.getColor();
            if (SpeedMine.this.fill2.getValue().equalsIgnoreCase("Static")) {
               double var65 = var20;
               double var67 = 0.9;
               Color var69;
               if (var65 >= var67) {
                  int var70 = SpeedMine.this.fill3.getAlpha();
                  var69 = new Color(0, 255, 0, var70);
               } else {
                  int var71 = SpeedMine.this.fill3.getAlpha();
                  var69 = new Color(255, 0, 0, var71);
               }

               var63 = var69;
               double var72 = var20;
               double var74 = 0.9;
               Color var76;
               if (var72 >= var74) {
                  int var77 = SpeedMine.this.outline.getAlpha();
                  var76 = new Color(0, 255, 0, var77);
               } else {
                  int var78 = SpeedMine.this.outline.getAlpha();
                  var76 = new Color(255, 0, 0, var78);
               }

               var64 = var76;
            } else if (SpeedMine.this.fill2.getValue().equalsIgnoreCase("Smooth")) {
               double var79 = var20;
               double var81 = 0.0;
               double var83 = 1.0;
               double var85 = Mth.clamp(var79, var81, var83);
               double var87 = 255.0;
               int var89 = (int)(var85 * var87);
               int var90 = 255 - var89;
               double var91 = var20;
               double var93 = 0.0;
               double var95 = 1.0;
               double var97 = Mth.clamp(var91, var93, var95);
               double var99 = 255.0;
               int var101 = (int)(var97 * var99);
               int var102 = SpeedMine.this.fill3.getAlpha();
               var63 = new Color(var90, var101, 0, var102);
               double var103 = var20;
               double var105 = 0.0;
               double var107 = 1.0;
               double var109 = Mth.clamp(var103, var105, var107);
               double var111 = 255.0;
               int var113 = (int)(var109 * var111);
               int var114 = 255 - var113;
               double var115 = var20;
               double var117 = 0.0;
               double var119 = 1.0;
               double var121 = Mth.clamp(var115, var117, var119);
               double var123 = 255.0;
               int var125 = (int)(var121 * var123);
               int var126 = SpeedMine.this.outline.getAlpha();
               var64 = new Color(var114, var125, 0, var126);
            }

            double var127 = var20;
            double var129 = this.F();
            if (var127 >= var129 && this.k && SpeedMine.this.none3.getValue().equalsIgnoreCase("Custom")) {
               Color var131 = SpeedMine.this.custom.getColor();
               int var132 = SpeedMine.this.fill3.getAlpha();
               var63 = ColorUtil.a(var131, var132);
               Color var133 = SpeedMine.this.custom.getColor();
               int var134 = SpeedMine.this.outline.getAlpha();
               var64 = ColorUtil.a(var133, var134);
            }

            if (SpeedMine.this.none2.getValue().equalsIgnoreCase("Fill") || SpeedMine.this.none2.getValue().equalsIgnoreCase("Both")) {
               AABB var135 = var5;
               Color var136 = var63;
               Render3DUtil.a_void_PoseStack_AABB_Color(var1, var135, var136);
            }

            if (SpeedMine.this.none2.getValue().equalsIgnoreCase("Outline") || SpeedMine.this.none2.getValue().equalsIgnoreCase("Both")) {
               AABB var137 = var5;
               Color var138 = var64;
               Render3DUtil.b(var1, var137, var138);
            }
         }
      }

      public void m() {
         if (!SpeedMine.this.farReach.getValue()) {
            this.D();
         } else {
            SpeedMine var1 = SpeedMine.this;
            byte var2;
            if (SpeedMine.this.farReach.getValue()) {
               var2 = 2;
            } else {
               var2 = 1;
            }

            var1.b_void_int(var2);
            Direction var3 = BlockInteractionUtil.c(this.b, true);
            if (SpeedMine.this.P()) {
               int var4 = MinecraftInstance.mc.player.getInventory().getSelectedSlot();
               this.l = var4;
               if (SpeedMine.this.farReach.getValue()) {
                  SpeedMine var5 = SpeedMine.this;
                  BlockPos var6 = this.b;
                  Direction var7 = var3;
                  var5.a(Action.START_DESTROY_BLOCK, var6, var7);
                  BlockPos var8 = this.G();
                  SpeedMine var9 = SpeedMine.this;
                  BlockPos var10 = var8;
                  Direction var11 = BlockInteractionUtil.c(var8, true);
                  var9.a(Action.START_DESTROY_BLOCK, var10, var11);
               } else {
                  SpeedMine var12 = SpeedMine.this;
                  Direction var13 = var3;
                  var12.a_void_IntFunction(var2x -> {
                     BlockPos var3x = this.b;
                     return new ServerboundPlayerActionPacket(Action.START_DESTROY_BLOCK, var3x, var13, var2x);
                  });
               }

               SpeedMine var14 = SpeedMine.this;
               Packet var15 = PacketAndRenderUtil.a_Packet_InteractionHand(InteractionHand.MAIN_HAND);
               var14.a_void_Packet(var15);
            } else {
               if (SpeedMine.this.farReach.getValue()) {
                  SpeedMine var16 = SpeedMine.this;
                  BlockPos var17 = this.b;
                  Direction var18 = var3;
                  var16.a(Action.START_DESTROY_BLOCK, var17, var18);
                  BlockPos var19 = this.G();
                  SpeedMine var20 = SpeedMine.this;
                  BlockPos var21 = var19;
                  Direction var22 = BlockInteractionUtil.c(var19, true);
                  var20.a(Action.START_DESTROY_BLOCK, var21, var22);
               } else {
                  Direction var23 = var3;
                  PacketPredictionUtil.a_void_SequencedPacketCreator(var2x -> {
                     BlockPos var3x = this.b;
                     return new ServerboundPlayerActionPacket(Action.START_DESTROY_BLOCK, var3x, var23, var2x);
                  });
               }

               ClientPacketListener var24 = MinecraftInstance.mc.getConnection();
               Packet var25 = PacketAndRenderUtil.a_Packet_InteractionHand(InteractionHand.MAIN_HAND);
               var24.send(var25);
            }

            if (this.c.getBlock() == Blocks.BEDROCK) {
               PacketAndRenderUtil.a(MinecraftInstance.mc.player, InteractionHand.MAIN_HAND);
            }

            this.k();
            this.e = 0.0F;
            this.f = 0.0F;
            this.q = 0.0F;
            this.r = false;
            this.s = -1;
            this.t = -1;
            this.g = 0;
            this.h = 0;
            this.i = true;
            this.k = false;
            this.m = true;
            this.n = 0;
            this.o = -1;
            this.p.reset();
         }
      }

      private void D() {
         Direction var1 = BlockInteractionUtil.c(this.b, true);
         if (SpeedMine.this.P()) {
            int var2;
            if (SpeedMine.this.switchSetting.getValue().equalsIgnoreCase("None")) {
               var2 = -1;
            } else {
               BlockState var6 = this.c;
               int var7 = InventoryUtil.c;
               int var8;
               if (!SpeedMine.this.switchSetting.getValue().equalsIgnoreCase("AltSwap")
                  && !SpeedMine.this.switchSetting.getValue().equalsIgnoreCase("AltPickup")) {
                  var8 = InventoryUtil.d;
               } else {
                  var8 = InventoryUtil.f;
               }

               var2 = InventoryUtil.a_int_BlockState_int_int(var6, var7, var8);
            }

            int var9;
            boolean var12;
            label40: {
               var9 = var2;
               if (var9 != -1) {
                  int var10 = var9;
                  int var11 = MinecraftInstance.mc.player.getInventory().getSelectedSlot();
                  if (var10 != var11) {
                     var12 = true;
                     break label40;
                  }
               }

               var12 = false;
            }

            if (var12) {
               SpeedMine var13 = SpeedMine.this;
               int var14 = var9;
               var13.a_void_Packet(new ServerboundSetCarriedItemPacket(var14));
            }

            if (SpeedMine.this.doubleSetting.getValue()) {
               SpeedMine var15 = SpeedMine.this;
               Direction var16 = var1;
               var15.a_void_IntFunction(var2x -> {
                  BlockPos var3x = this.b;
                  return new ServerboundPlayerActionPacket(Action.STOP_DESTROY_BLOCK, var3x, var16, var2x);
               });
               SpeedMine var17 = SpeedMine.this;
               Direction var18 = var1;
               var17.a_void_IntFunction(var2x -> {
                  BlockPos var3x = this.b;
                  return new ServerboundPlayerActionPacket(Action.START_DESTROY_BLOCK, var3x, var18, var2x);
               });
               SpeedMine var19 = SpeedMine.this;
               Direction var20 = var1;
               var19.a_void_IntFunction(var2x -> {
                  BlockPos var3x = this.b;
                  return new ServerboundPlayerActionPacket(Action.STOP_DESTROY_BLOCK, var3x, var20, var2x);
               });
            } else {
               SpeedMine var21 = SpeedMine.this;
               Direction var22 = var1;
               var21.a_void_IntFunction(var2x -> {
                  BlockPos var3x = this.b;
                  return new ServerboundPlayerActionPacket(Action.START_DESTROY_BLOCK, var3x, var22, var2x);
               });
            }

            SpeedMine var23 = SpeedMine.this;
            Packet var24 = PacketAndRenderUtil.a_Packet_InteractionHand(InteractionHand.MAIN_HAND);
            var23.a_void_Packet(var24);
         } else {
            if (SpeedMine.this.doubleSetting.getValue()) {
               Direction var3 = var1;
               PacketPredictionUtil.a_void_SequencedPacketCreator(var2x -> {
                  BlockPos var3x = this.b;
                  return new ServerboundPlayerActionPacket(Action.STOP_DESTROY_BLOCK, var3x, var3, var2x);
               });
               Direction var4 = var1;
               PacketPredictionUtil.a_void_SequencedPacketCreator(var2x -> {
                  BlockPos var3x = this.b;
                  return new ServerboundPlayerActionPacket(Action.START_DESTROY_BLOCK, var3x, var4, var2x);
               });
               Direction var5 = var1;
               PacketPredictionUtil.a_void_SequencedPacketCreator(var2x -> {
                  BlockPos var3x = this.b;
                  return new ServerboundPlayerActionPacket(Action.STOP_DESTROY_BLOCK, var3x, var5, var2x);
               });
            } else {
               Direction var25 = var1;
               PacketPredictionUtil.a_void_SequencedPacketCreator(var2x -> {
                  BlockPos var3x = this.b;
                  return new ServerboundPlayerActionPacket(Action.START_DESTROY_BLOCK, var3x, var25, var2x);
               });
            }

            ClientPacketListener var26 = MinecraftInstance.mc.getConnection();
            Packet var27 = PacketAndRenderUtil.a_Packet_InteractionHand(InteractionHand.MAIN_HAND);
            var26.send(var27);
         }

         if (this.c.getBlock() == Blocks.BEDROCK) {
            PacketAndRenderUtil.a(MinecraftInstance.mc.player, InteractionHand.MAIN_HAND);
         }

         this.k();
         this.e = 0.0F;
         this.f = 0.0F;
         this.q = 0.0F;
         this.r = false;
         this.s = -1;
         this.t = -1;
         this.g = 0;
         this.h = 0;
         this.i = true;
         this.k = false;
         this.m = true;
         this.n = 0;
         this.o = -1;
         this.p.reset();
      }

      public void n() {
         if (!SpeedMine.this.farReach.getValue()) {
            this.E();
         } else {
            if (!SpeedMine.this.doubleSetting.getValue() && this.m) {
               if (SpeedMine.this.farReach.getValue()) {
                  SpeedMine var1 = SpeedMine.this;
                  BlockPos var2 = this.b;
                  Direction var3 = BlockInteractionUtil.c(this.b, true);
                  var1.a(Action.ABORT_DESTROY_BLOCK, var2, var3);
                  SpeedMine var4 = SpeedMine.this;
                  Packet var5 = PacketAndRenderUtil.a_Packet_InteractionHand(InteractionHand.MAIN_HAND);
                  var4.a_void_Packet(var5);
               } else if (SpeedMine.this.P()) {
                  SpeedMine.this.a_void_IntFunction(var1x -> {
                     BlockPos var2x = this.b;
                     Direction var3x = BlockInteractionUtil.c(this.b, true);
                     return new ServerboundPlayerActionPacket(Action.ABORT_DESTROY_BLOCK, var2x, var3x, var1x);
                  });
                  SpeedMine var6 = SpeedMine.this;
                  Packet var7 = PacketAndRenderUtil.a_Packet_InteractionHand(InteractionHand.MAIN_HAND);
                  var6.a_void_Packet(var7);
               } else {
                  PacketPredictionUtil.a_void_SequencedPacketCreator(var1x -> {
                     BlockPos var2x = this.b;
                     Direction var3x = BlockInteractionUtil.c(this.b, true);
                     return new ServerboundPlayerActionPacket(Action.ABORT_DESTROY_BLOCK, var2x, var3x, var1x);
                  });
                  ClientPacketListener var8 = MinecraftInstance.mc.getConnection();
                  Packet var9 = PacketAndRenderUtil.a_Packet_InteractionHand(InteractionHand.MAIN_HAND);
                  var8.send(var9);
               }
            }

            if (SpeedMine.this.P() && this.l != -1) {
               SpeedMine var10 = SpeedMine.this;
               int var11 = this.l;
               var10.a_void_Packet(new ServerboundSetCarriedItemPacket(var11));
               Inventory var12 = MinecraftInstance.mc.player.getInventory();
               int var13 = this.l;
               var12.setSelectedSlot(var13);
               this.l = -1;
            }

            this.k();
            this.e = 0.0F;
            this.f = 0.0F;
            this.q = 0.0F;
            this.r = false;
            this.s = -1;
            this.t = -1;
            this.g = 0;
            this.i = false;
            this.m = false;
            this.k = false;
         }
      }

      private void E() {
         if (!SpeedMine.this.doubleSetting.getValue()) {
            Direction var1 = BlockInteractionUtil.c(this.b, true);
            if (SpeedMine.this.P()) {
               SpeedMine var2 = SpeedMine.this;
               Direction var3 = var1;
               var2.a_void_IntFunction(var2x -> {
                  BlockPos var3x = this.b;
                  return new ServerboundPlayerActionPacket(Action.ABORT_DESTROY_BLOCK, var3x, var3, var2x);
               });
               SpeedMine var4 = SpeedMine.this;
               Packet var5 = PacketAndRenderUtil.a_Packet_InteractionHand(InteractionHand.MAIN_HAND);
               var4.a_void_Packet(var5);
            } else {
               Direction var6 = var1;
               PacketPredictionUtil.a_void_SequencedPacketCreator(var2x -> {
                  BlockPos var3x = this.b;
                  return new ServerboundPlayerActionPacket(Action.ABORT_DESTROY_BLOCK, var3x, var6, var2x);
               });
               ClientPacketListener var7 = MinecraftInstance.mc.getConnection();
               Packet var8 = PacketAndRenderUtil.a_Packet_InteractionHand(InteractionHand.MAIN_HAND);
               var7.send(var8);
            }
         }

         if (SpeedMine.this.P()) {
            SpeedMine var9 = SpeedMine.this;
            int var10 = MinecraftInstance.mc.player.getInventory().getSelectedSlot();
            var9.a_void_Packet(new ServerboundSetCarriedItemPacket(var10));
         }

         this.k();
         this.e = 0.0F;
         this.f = 0.0F;
         this.q = 0.0F;
         this.r = false;
         this.s = -1;
         this.t = -1;
         this.g = 0;
         this.i = false;
         this.m = false;
         this.k = false;
      }

      float F() {
         return SpeedMine.this.speed.getValue().floatValue();
      }

      private BlockPos G() {
         return this.b.below(2000);
      }

      public int o() {
         if (!this.i) {
            return Integer.MAX_VALUE;
         }

         if (SpeedMine.this.s6b.getValue() && this.c.getBlock() == Blocks.BEDROCK) {
            float var1 = this.F();
            float var2 = this.e;
            int var3 = Math.round((var1 - var2) / 0.025F);
            return Math.max(0, var3);
         }

         int var4 = InventoryUtil.a_int_BlockState_int_int(this.c, InventoryUtil.c, InventoryUtil.d);
         if (var4 == -1) {
            var4 = MinecraftInstance.mc.player.getInventory().getSelectedSlot();
         }

         BlockState var5 = this.c;
         int var6 = var4;
         float var7 = BlockInteractionUtil.a_float_BlockState_int(var5, var6);
         float var8 = Night.worldStateManager.getTimerMultiplier();
         float var9 = var7 / var8;
         if (var9 <= 0.0F) {
            return Integer.MAX_VALUE;
         }

         float var10 = this.F();
         float var11 = this.e;
         float var12 = var10 - var11;
         float var13 = var9;
         int var14 = Math.round(var12 / var13);
         return Math.max(0, var14);
      }

      @Generated
      public boolean p() {
         return this.w;
      }
   }

   public record TargetMineEntry(Player player, List<SpeedMine.MineCandidate> list, BlockPos pos) {
      public BlockPos b() {
         return this.pos;
      }
   }
}
