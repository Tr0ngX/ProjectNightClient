package night.pingbypass.module.impl;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import lombok.Generated;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ExperienceBottleItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.events.EventTarget;
import night.events.impl.BlockPlaceEvent;
import night.events.impl.ClientConnectEvent;
import night.events.impl.EntitySpawnEvent;
import night.events.impl.PacketReceiveEvent;
import night.events.impl.PacketSendEvent;
import night.events.impl.PlayerDeathEvent;
import night.module.impl.combat.CrystalRaytraceHelper;
import night.module.impl.combat.Suicide;
import night.module.impl.movement.Phase;
import night.module.impl.player.KeyAction;
import night.module.impl.player.SpeedMine;
import night.pingbypass.module.ProxyModule;
import night.setting.Setting;
import night.setting.impl.BooleanSetting;
import night.setting.impl.ModeSetting;
import night.setting.impl.NumberSetting;
import night.util.MinecraftInstance;
import night.util.inventory.InventoryUtil;
import night.util.math.RollingCounter;
import night.util.math.TimerUtil;
import night.util.network.PacketPredictionUtil;
import night.util.rotation.RotationMathUtil;
import night.util.world.BlockInteractionUtil;
import night.util.world.DamageUtil;
import night.util.world.EntityUtil;
import night.util.world.PacketAndRenderUtil;
import night.util.world.PositionUtil;

public class ProxyAutoCrystal extends ProxyModule implements MinecraftInstance {
   public BooleanSetting attack;
   public NumberSetting attackSpeed;
   public NumberSetting attackRange;
   public NumberSetting attackWallsRange;
   public ModeSetting none;
   public BooleanSetting instant;
   public BooleanSetting inhibit;
   public BooleanSetting place;
   public NumberSetting placeSpeed;
   public NumberSetting placeRange;
   public NumberSetting placeWallsRange;
   public ModeSetting nativeSetting;
   public BooleanSetting blockDestruction;
   public ModeSetting none2;
   public BooleanSetting swapBack;
   public NumberSetting swapDelay;
   public ModeSetting strong;
   public ModeSetting normal;
   public ModeSetting defaultSetting;
   public BooleanSetting yawStep;
   public NumberSetting yawStepThreshold;
   public BooleanSetting raytrace;
   public NumberSetting extrapolation;
   public NumberSetting x;
   public BooleanSetting y;
   public BooleanSetting z;
   public BooleanSetting pauseOnPearl;
   public BooleanSetting pauseOnXP;
   public BooleanSetting gameLoop;
   public NumberSetting loopDelay;
   public ModeSetting both;
   public BooleanSetting godSync;
   public NumberSetting predictions;
   public NumberSetting offset;
   public ModeSetting normal2;
   public BooleanSetting fast;
   public BooleanSetting antiKick;
   public NumberSetting kickThreshold;
   public ModeSetting dynamic;
   public ModeSetting normal3;
   public NumberSetting faceplaceDelay;
   public BooleanSetting healthPlace;
   public NumberSetting health;
   public BooleanSetting armorPlace;
   public NumberSetting percentage;
   public NumberSetting minimumDamage;
   public NumberSetting maximumSelfDamage;
   public NumberSetting lethalMultiplier;
   public BooleanSetting antiSuicide;
   public BooleanSetting ignoreTerrain;
   private final ExecutorService Y;
   private volatile Future<?> Z;
   private Runnable aa;
   private Runnable ab;
   private final Map<Integer, Long> ac;
   private final Map<BlockPos, Long> ad;
   private final Map<BlockPos, Long> ae;
   private final TimerUtil af;
   private final TimerUtil ag;
   private final TimerUtil ah;
   private final TimerUtil ai;
   private final TimerUtil aj;
   private int ak;
   private long al;
   private long am;
   private boolean an;
   private boolean ao;
   private boolean ap;
   private boolean aq;
   private Player ar;
   private EndCrystal as;
   private ProxyAutoCrystal.PlaceTarget at;
   private ProxyAutoCrystal.PlaceTarget au;
   private String av;
   private int aw;
   private String ax;
   private final RollingCounter ay;
   private int az;
   private int aA;
   private int aB;
   private int aC;
   private static final float aD = 0.5F;
   private long aE;
   private long aF;

   public ProxyAutoCrystal() {
      super("AutoCrystal");
      BooleanSetting var1 = new BooleanSetting("Attack", "Automatically attacks crystals that are deemed safe.", true);
      this.attack = var1;
      Float var3 = 20.0F;
      Float var4 = 0.1F;
      Float var5 = 20.0F;
      NumberSetting var2 = new NumberSetting("AttackSpeed", "The speed at which crystals will be attacked.", var3, var4, var5);
      this.attackSpeed = var2;
      Double var7 = 4.5;
      Double var8 = 0.0;
      Double var9 = 8.0;
      NumberSetting var6 = new NumberSetting("AttackRange", "The maximum distance at which crystals will be attacked.", var7, var8, var9);
      this.attackRange = var6;
      Double var11 = 4.5;
      Double var12 = 0.0;
      Double var13 = 8.0;
      NumberSetting var10 = new NumberSetting("AttackWallsRange", "The maximum distance at which crystals will be attacked through walls.", var11, var12, var13);
      this.attackWallsRange = var10;
      String[] var15 = new String[]{"None", null, null};
      byte var16 = 1;
      String var17 = "Normal";
      var15[var16] = var17;
      byte var18 = 2;
      String var19 = "Silent";
      var15[var18] = var19;
      ModeSetting var14 = new ModeSetting("AntiWeakness", "Allows you to attack crystals when weaknessed.", "None", var15);
      this.none = var14;
      BooleanSetting var20 = new BooleanSetting("Instant", "Instantly attacks crystals once they spawn.", true);
      this.instant = var20;
      BooleanSetting var21 = new BooleanSetting("Inhibit", "Prevents excessive attacks on crystals by blacklisting crystals when attacking them.", true);
      this.inhibit = var21;
      boolean var23 = true;
      BooleanSetting var22 = new BooleanSetting("Place", "Automatically places crystals on positions that are deemed safe and lethal enough.", var23);
      this.place = var22;
      Float var25 = 20.0F;
      Float var26 = 0.1F;
      Float var27 = 20.0F;
      NumberSetting var24 = new NumberSetting("PlaceSpeed", "The speed at which crystals will be placed.", var25, var26, var27);
      this.placeSpeed = var24;
      Double var29 = 4.5;
      Double var30 = 0.0;
      Double var31 = 8.0;
      NumberSetting var28 = new NumberSetting("PlaceRange", "The maximum distance at which positions will be placed on.", var29, var30, var31);
      this.placeRange = var28;
      Double var33 = 4.5;
      Double var34 = 0.0;
      Double var35 = 8.0;
      NumberSetting var32 = new NumberSetting(
         "PlaceWallsRange", "The maximum distance at which positions will be placed on through walls.", var33, var34, var35
      );
      this.placeWallsRange = var32;
      String var37 = "Native";
      String[] var38 = new String[2];
      byte var39 = 0;
      String var40 = "Native";
      var38[var39] = var40;
      byte var41 = 1;
      String var42 = "Protocol";
      var38[var41] = var42;
      ModeSetting var36 = new ModeSetting("Placements", "The version of the game that will be used for crystal placement calculations.", var37, var38);
      this.nativeSetting = var36;
      boolean var44 = true;
      BooleanSetting var43 = new BooleanSetting("BlockDestruction", "Places crystals on top of mined blocks in order to damage opponents.", var44);
      this.blockDestruction = var43;
      String var46 = "None";
      String[] var47 = new String[4];
      byte var48 = 0;
      String var49 = "None";
      var47[var48] = var49;
      byte var50 = 1;
      String var51 = "Normal";
      var47[var50] = var51;
      byte var52 = 2;
      String var53 = "Silent";
      var47[var52] = var53;
      byte var54 = 3;
      String var55 = "AltSwap";
      var47[var54] = var55;
      ModeSetting var45 = new ModeSetting("Switch", "Automatically switches to a crystal if you aren't currently holding one.", var46, var47);
      this.none2 = var45;
      boolean var57 = false;
      BooleanSetting var56 = new BooleanSetting(
         "SwapBack", "Switches back to the item you were holding before the module started switching to crystals.", var57
      );
      this.swapBack = var56;
      Integer var59 = 0;
      Integer var60 = 0;
      Integer var61 = 20;
      NumberSetting var58 = new NumberSetting("SwapDelay", "The delay in ticks after swapping before placing or attacking crystals.", var59, var60, var61);
      this.swapDelay = var58;
      String var63 = "Strong";
      String[] var64 = new String[3];
      byte var65 = 0;
      String var66 = "None";
      var64[var65] = var66;
      byte var67 = 1;
      String var68 = "Strict";
      var64[var67] = var68;
      byte var69 = 2;
      String var70 = "Strong";
      var64[var69] = var70;
      ModeSetting var62 = new ModeSetting("Sequential", "The sequence that the module's processes will be run in.", var63, var64);
      this.strong = var62;
      String var72 = "Normal";
      String[] var73 = new String[4];
      byte var74 = 0;
      String var75 = "None";
      var73[var74] = var75;
      byte var76 = 1;
      String var77 = "Normal";
      var73[var76] = var77;
      byte var78 = 2;
      var73[var78] = "Packet";
      byte var79 = 3;
      var73[var79] = "Silent";
      ModeSetting var71 = new ModeSetting("Rotate", "Automatically rotates to the crystal whenever attacking or placing.", var72, var73);
      this.normal = var71;
      String var81 = "Default";
      String[] var82 = new String[6];
      byte var83 = 0;
      var82[var83] = "Default";
      byte var84 = 1;
      var82[var84] = "None";
      byte var85 = 2;
      var82[var85] = "Packet";
      byte var86 = 3;
      var82[var86] = "Mainhand";
      byte var87 = 4;
      var82[var87] = "Offhand";
      byte var88 = 5;
      var82[var88] = "Both";
      ModeSetting var80 = new ModeSetting("Swing", "The hand that will be used for swinging.", var81, var82);
      this.defaultSetting = var80;
      boolean var90 = false;
      BooleanSetting var89 = new BooleanSetting("YawStep", "Performs your rotations over multiple ticks.", var90);
      this.yawStep = var89;
      Integer var92 = 75;
      Integer var93 = 1;
      Integer var94 = 180;
      NumberSetting var91 = new NumberSetting("YawStepThreshold", "The threshold in order for yaw to be modified.", var92, var93, var94);
      this.yawStepThreshold = var91;
      boolean var96 = false;
      BooleanSetting var95 = new BooleanSetting("Raytrace", "Avoids attacking or placing any crystals through walls.", var96);
      this.raytrace = var95;
      Integer var98 = 0;
      Integer var99 = 0;
      Integer var100 = 20;
      NumberSetting var97 = new NumberSetting("Extrapolation", "Extrapolates the target's position to calculate positions ahead of time.", var98, var99, var100);
      this.extrapolation = var97;
      Double var102 = 10.0;
      Double var103 = 0.0;
      Double var104 = 24.0;
      NumberSetting var101 = new NumberSetting("EnemyRange", "The maximum distance at which enemies can be at.", var102, var103, var104);
      this.x = var101;
      boolean var106 = false;
      BooleanSetting var105 = new BooleanSetting(
         "IgnoreNaked", "Ignores naked players (even if wearing only elytra), but still targets them if they are holding an End Crystal.", var106
      );
      this.y = var105;
      boolean var108 = false;
      BooleanSetting var107 = new BooleanSetting(
         "ChestBreak", "Prevents other players from getting obsidian from ender chests by destroying the dropped items.", var108
      );
      this.z = var107;
      boolean var110 = true;
      BooleanSetting var109 = new BooleanSetting(
         "PauseOnPearl", "Pauses AutoCrystal when Phase is active, KeyAction pearl is used, or when throwing an ender pearl manually.", var110
      );
      this.pauseOnPearl = var109;
      boolean var112 = true;
      BooleanSetting var111 = new BooleanSetting(
         "PauseOnXP", "Pauses AutoCrystal when throwing experience bottles manually or when KeyAction XP is active.", var112
      );
      this.pauseOnXP = var111;
      boolean var114 = false;
      BooleanSetting var113 = new BooleanSetting("GameLoop", "Runs the module on loop instead of ticks.", var114);
      this.gameLoop = var113;
      Integer var116 = 50;
      Integer var117 = 0;
      Integer var118 = 1000;
      NumberSetting var115 = new NumberSetting("LoopDelay", "The delay that has to be waited out before running the module again.", var116, var117, var118);
      this.loopDelay = var115;
      String var120 = "Both";
      String[] var121 = new String[4];
      byte var122 = 0;
      var121[var122] = "None";
      byte var123 = 1;
      var121[var123] = "Attack";
      byte var124 = 2;
      var121[var124] = "Place";
      byte var125 = 3;
      var121[var125] = "Both";
      ModeSetting var119 = new ModeSetting("WhileEating", "Places and attacks crystal while eating or using items.", var120, var121);
      this.both = var119;
      boolean var127 = false;
      BooleanSetting var126 = new BooleanSetting("GodSync", "Makes the attacking way faster by predicting entity IDs.", var127);
      this.godSync = var126;
      Integer var129 = 10;
      Integer var130 = 1;
      Integer var131 = 20;
      NumberSetting var128 = new NumberSetting("Predictions", "The amount of predictions that will be done after placing.", var129, var130, var131);
      this.predictions = var128;
      Integer var133 = 0;
      Integer var134 = 0;
      Integer var135 = 2;
      NumberSetting var132 = new NumberSetting("Offset", "The amount that the last entity ID should be offset by.", var133, var134, var135);
      this.offset = var132;
      String var137 = "Normal";
      String[] var138 = new String[3];
      byte var139 = 0;
      var138[var139] = "None";
      byte var140 = 1;
      var138[var140] = "Normal";
      byte var141 = 2;
      var138[var141] = "Strict";
      ModeSetting var136 = new ModeSetting("GodSwing", "The swinging that will be done for each predicted attack.", var137, var138);
      this.normal2 = var136;
      boolean var143 = false;
      BooleanSetting var142 = new BooleanSetting("Fast", "Improves the speed of the prediction calculations at the cost of stability.", var143);
      this.fast = var142;
      boolean var145 = false;
      BooleanSetting var144 = new BooleanSetting("AntiKick", "Prevents you from getting kicked by attacking invalid entity IDs.", var145);
      this.antiKick = var144;
      Integer var147 = 5;
      Integer var148 = 1;
      Integer var149 = 10;
      NumberSetting var146 = new NumberSetting("KickThreshold", "The tick threshold for the kick prevention.", var147, var148, var149);
      this.kickThreshold = var146;
      String var151 = "Dynamic";
      String[] var152 = new String[3];
      byte var153 = 0;
      var152[var153] = "None";
      byte var154 = 1;
      var152[var154] = "Dynamic";
      byte var155 = 2;
      var152[var155] = "Always";
      ModeSetting var150 = new ModeSetting("FaceplaceMode", "The checks that will be done in order to faceplace.", var151, var152);
      this.dynamic = var150;
      String var157 = "Normal";
      String[] var158 = new String[2];
      byte var159 = 0;
      var158[var159] = "Normal";
      byte var160 = 1;
      var158[var160] = "Custom";
      ModeSetting var156 = new ModeSetting("FaceplaceSpeed", "The speed that players will be faceplaced at.", var157, var158);
      this.normal3 = var156;
      Integer var162 = 11;
      Integer var163 = 0;
      Integer var164 = 20;
      NumberSetting var161 = new NumberSetting("FaceplaceDelay", "The ticks that have to be waited for before faceplacing again.", var162, var163, var164);
      this.faceplaceDelay = var161;
      boolean var166 = true;
      BooleanSetting var165 = new BooleanSetting("HealthPlace", "Whether or not to faceplace when the target's health is low.", var166);
      this.healthPlace = var165;
      Float var168 = 8.0F;
      Float var169 = 0.0F;
      Float var170 = 36.0F;
      NumberSetting var167 = new NumberSetting(
         "Health", "The health that the target needs to be at in order for the module to start faceplacing.", var168, var169, var170
      );
      this.health = var167;
      boolean var172 = true;
      BooleanSetting var171 = new BooleanSetting("ArmorPlace", "Whether or not to faceplace when the target's armor is low on durability.", var172);
      this.armorPlace = var171;
      Integer var174 = 10;
      Integer var175 = 1;
      Integer var176 = 100;
      NumberSetting var173 = new NumberSetting(
         "Percentage", "The percentage that one of the target's armor pieces need to be at in order to start faceplacing.", var174, var175, var176
      );
      this.percentage = var173;
      Double var178 = 6.0;
      Double var179 = 0.0;
      Double var180 = 36.0;
      NumberSetting var177 = new NumberSetting("MinimumDamage", "The minimum damage that has to be dealt to enemies.", var178, var179, var180);
      this.minimumDamage = var177;
      Double var182 = 10.0;
      Double var183 = 0.0;
      Double var184 = 36.0;
      NumberSetting var181 = new NumberSetting("MaximumSelfDamage", "The maximum damage that can be dealt to you by crystals.", var182, var183, var184);
      this.maximumSelfDamage = var181;
      Float var186 = 1.5F;
      Float var187 = 0.0F;
      Float var188 = 4.0F;
      NumberSetting var185 = new NumberSetting(
         "LethalMultiplier", "The amount of crystals that the target has to be killed by in order to ignore minimum damage.", var186, var187, var188
      );
      this.lethalMultiplier = var185;
      boolean var190 = true;
      BooleanSetting var189 = new BooleanSetting("AntiSuicide", "Prevents crystals from accidentally killing you when you're low on health.", var190);
      this.antiSuicide = var189;
      boolean var192 = true;
      BooleanSetting var191 = new BooleanSetting("IgnoreTerrain", "Ignores terrain that can be destroyed when calculating damage.", var192);
      this.ignoreTerrain = var191;
      ExecutorService var193 = Executors.newSingleThreadExecutor();
      this.Y = var193;
      this.Z = null;
      this.aa = null;
      this.ab = null;
      ConcurrentHashMap var194 = new ConcurrentHashMap();
      this.ac = var194;
      ConcurrentHashMap var195 = new ConcurrentHashMap();
      this.ad = var195;
      ConcurrentHashMap var196 = new ConcurrentHashMap();
      this.ae = var196;
      TimerUtil var197 = new TimerUtil();
      this.af = var197;
      TimerUtil var198 = new TimerUtil();
      this.ag = var198;
      TimerUtil var199 = new TimerUtil();
      this.ah = var199;
      TimerUtil var200 = new TimerUtil();
      this.ai = var200;
      TimerUtil var201 = new TimerUtil();
      this.aj = var201;
      this.ak = -1;
      long var202 = 0L;
      this.al = var202;
      long var204 = 0L;
      this.am = var204;
      this.an = false;
      this.ao = true;
      this.ap = false;
      this.aq = false;
      this.ar = null;
      this.as = null;
      this.at = null;
      this.au = null;
      this.av = "0.00ms";
      this.aw = 0;
      this.ax = "0.00";
      RollingCounter var206 = new RollingCounter();
      this.ay = var206;
      this.az = 0;
      this.aA = -100000;
      this.aB = 0;
      this.aC = -1;
      long var207 = 0L;
      this.aE = var207;
      long var209 = 0L;
      this.aF = var209;
   }

   @Override
   public void onEnable() {
      Night.EVENT_BUS.register(this);
   }

   @Override
   public void onDisable() {
      Night.EVENT_BUS.unregister(this);
      if (this.Z != null) {
         this.Z.cancel(false);
         this.Z = null;
      }

      if (this.aC != -1) {
         InventoryUtil.d(this.aC);
         this.aC = -1;
      }

      this.aa = null;
      this.ab = null;
      Night.renderPlacementManager.setCrystalTarget(null);
      this.ac.clear();
      this.ad.clear();
      this.ae.clear();
      this.ap = false;
      this.aq = false;
      this.ar = null;
      this.at = null;
      this.au = null;
      this.av = "0.00ms";
      this.aw = 0;
      this.ax = "0.00";
      this.ay.reset();
      this.aA = -100000;
   }

   @Override
   public List<Setting> getSettings() {
      Setting[] var1 = new Setting[47];
      BooleanSetting var2 = this.attack;
      var1[0] = var2;
      NumberSetting var3 = this.attackSpeed;
      var1[1] = var3;
      NumberSetting var4 = this.attackRange;
      var1[2] = var4;
      NumberSetting var5 = this.attackWallsRange;
      var1[3] = var5;
      ModeSetting var6 = this.none;
      var1[4] = var6;
      BooleanSetting var7 = this.instant;
      var1[5] = var7;
      BooleanSetting var8 = this.inhibit;
      var1[6] = var8;
      BooleanSetting var9 = this.place;
      var1[7] = var9;
      NumberSetting var10 = this.placeSpeed;
      var1[8] = var10;
      NumberSetting var11 = this.placeRange;
      var1[9] = var11;
      NumberSetting var12 = this.placeWallsRange;
      var1[10] = var12;
      ModeSetting var13 = this.nativeSetting;
      var1[11] = var13;
      BooleanSetting var14 = this.blockDestruction;
      var1[12] = var14;
      ModeSetting var15 = this.none2;
      var1[13] = var15;
      BooleanSetting var16 = this.swapBack;
      var1[14] = var16;
      NumberSetting var17 = this.swapDelay;
      var1[15] = var17;
      ModeSetting var18 = this.strong;
      var1[16] = var18;
      ModeSetting var19 = this.normal;
      var1[17] = var19;
      ModeSetting var20 = this.defaultSetting;
      var1[18] = var20;
      BooleanSetting var21 = this.yawStep;
      var1[19] = var21;
      NumberSetting var22 = this.yawStepThreshold;
      var1[20] = var22;
      BooleanSetting var23 = this.raytrace;
      var1[21] = var23;
      NumberSetting var24 = this.extrapolation;
      var1[22] = var24;
      NumberSetting var25 = this.x;
      var1[23] = var25;
      BooleanSetting var26 = this.z;
      var1[24] = var26;
      BooleanSetting var27 = this.gameLoop;
      var1[25] = var27;
      NumberSetting var28 = this.loopDelay;
      var1[26] = var28;
      ModeSetting var29 = this.both;
      var1[27] = var29;
      BooleanSetting var30 = this.godSync;
      var1[28] = var30;
      NumberSetting var31 = this.predictions;
      var1[29] = var31;
      NumberSetting var32 = this.offset;
      var1[30] = var32;
      ModeSetting var33 = this.normal2;
      var1[31] = var33;
      BooleanSetting var34 = this.fast;
      var1[32] = var34;
      BooleanSetting var35 = this.antiKick;
      var1[33] = var35;
      NumberSetting var36 = this.kickThreshold;
      var1[34] = var36;
      ModeSetting var37 = this.dynamic;
      var1[35] = var37;
      ModeSetting var38 = this.normal3;
      var1[36] = var38;
      NumberSetting var39 = this.faceplaceDelay;
      var1[37] = var39;
      BooleanSetting var40 = this.healthPlace;
      var1[38] = var40;
      NumberSetting var41 = this.health;
      var1[39] = var41;
      BooleanSetting var42 = this.armorPlace;
      var1[40] = var42;
      NumberSetting var43 = this.percentage;
      var1[41] = var43;
      NumberSetting var44 = this.minimumDamage;
      var1[42] = var44;
      NumberSetting var45 = this.maximumSelfDamage;
      var1[43] = var45;
      NumberSetting var46 = this.lethalMultiplier;
      var1[44] = var46;
      BooleanSetting var47 = this.antiSuicide;
      var1[45] = var47;
      BooleanSetting var48 = this.ignoreTerrain;
      var1[46] = var48;
      return List.of(var1);
   }

   private boolean j() {
      if (mc.player != null && mc.level != null) {
         return !mc.player.isAlive() || mc.player.isDeadOrDying() || mc.player.getHealth() <= 0.0F ? true : mc.gui.screen() instanceof DeathScreen;
      } else {
         return true;
      }
   }

   @Override
   public void onTick() {
      if (this.j()) {
         this.aa = null;
         this.ab = null;
         this.ar = null;
         this.at = null;
         this.as = null;
         this.au = null;
         this.ad.clear();
         this.ac.clear();
         this.ae.clear();
         Night.renderPlacementManager.setCrystalTarget(null);
      } else {
         int var1 = mc.player.getInventory().getSelectedSlot();
         if (this.ak != -1) {
            int var2 = this.ak;
            int var3 = var1;
            if (var2 != var3) {
               this.aj.reset();
            }
         }

         int var4 = var1;
         this.ak = var4;
         long var5 = 50L;
         this.ac.entrySet().removeIf(var2x -> {
            long var3x = System.currentTimeMillis();
            long var5x = var2x.getValue();
            long var7x = var3x - var5x;
            long var9x = Night.serverManager.getPing();
            long var11x = 2L;
            long var13x = var9x * var11x;
            long var15x = var5;
            long var17x = Math.max(var13x, var15x);
            boolean var19;
            if (var7x > var17x) {
               var19 = true;
            } else {
               var19 = false;
            }

            return var19;
         });
         long var7 = Night.serverManager.getPing();
         long var9 = 2L;
         long var11 = var7 * var9;
         long var13 = 500L;
         long var15 = Math.max(var11, var13);
         float var17 = this.attackSpeed.getValue().floatValue();
         long var18 = (long)((20.0F - var17) * 50.0F);
         long var20 = var15 + var18;
         this.ad.entrySet().removeIf(var2x -> {
            long var3x = System.currentTimeMillis();
            long var5x = var2x.getValue();
            long var7x = var3x - var5x;
            long var9x = var20;
            boolean var11x;
            if (var7x > var9x) {
               var11x = true;
            } else {
               var11x = false;
            }

            return var11x;
         });
         this.ae.entrySet().removeIf(var2x -> {
            long var3x = System.currentTimeMillis();
            long var5x = var2x.getValue();
            long var7x = var3x - var5x;
            long var9x = Night.serverManager.getPing();
            long var11x = 2L;
            long var13x = var9x * var11x;
            long var15x = var5;
            long var17x = Math.max(var13x, var15x);
            boolean var19;
            if (var7x > var17x) {
               var19 = true;
            } else {
               var19 = false;
            }

            return var19;
         });
         int var22 = this.ay.getCount();
         this.az = var22;
         Runnable var23 = () -> {
            long var1x = System.nanoTime();
            EndCrystal var3x = this.m();
            this.as = var3x;
            ProxyAutoCrystal.PlaceTarget var4x = this.a_PlaceTarget_BlockPos(null);
            this.at = var4x;
            long var5x = System.nanoTime();
            long var7x = var1x;
            long var9x = var5x - var7x;
            DecimalFormat var11x = new DecimalFormat("0.00");
            double var12 = var9x;
            double var14 = 1000000.0;
            double var16 = var12 / var14;
            this.av = var11x.format(var16) + "ms";
            int var18x;
            if (this.at == null) {
               var18x = 0;
            } else {
               var18x = this.at.g();
            }

            this.aw = var18x;
            String var19;
            if (this.at == null) {
               var19 = "0.00";
            } else {
               DecimalFormat var20x = new DecimalFormat("0.00");
               double var21 = this.at.f();
               var19 = var20x.format(var21);
            }

            this.ax = var19;
            Player var23x;
            if (this.at == null) {
               var23x = null;
            } else {
               var23x = this.at.c();
            }

            this.ar = var23x;
            if (this.blockDestruction.getValue()) {
               SpeedMine var24x = Night.moduleManager.getModule(SpeedMine.class);
               BlockPos var25x = null;
               if (var24x.v() != null && var24x.v().e()) {
                  var25x = var24x.v().a();
               }

               if (var25x != null) {
                  BlockPos var26x = var25x;
                  ProxyAutoCrystal.PlaceTarget var27x = this.a_PlaceTarget_BlockPos(var26x);
                  this.au = var27x;
               }
            }
         };
         if (this.Z == null || this.Z.isDone()) {
            ExecutorService var24 = this.Y;
            Runnable var25 = var23;
            Future var26 = var24.submit(var25);
            this.Z = var26;
         }

         if (this.gameLoop.getValue()) {
            TimerUtil var27 = this.ai;
            Long var28 = this.loopDelay.getValue().longValue();
            if (!var27.passedMs(var28)) {
               return;
            }

            this.ai.reset();
         }

         this.k();
         if (this.aa != null) {
            this.aa.run();
            this.aa = null;
         }

         if (this.ab != null) {
            this.ab.run();
            this.ab = null;
         }
      }
   }

   private void k() {
      if (!this.j() && !this.o() && !this.p()) {
         this.aa = null;
         this.ab = null;
         if (this.strong.getValue().equalsIgnoreCase("None")) {
            if (this.an) {
               this.an = false;
               this.ao = true;
               this.l();
               return;
            }

            if (this.ao) {
               this.an = true;
               this.ao = false;
               this.b_void_bool(false);
            }
         } else {
            if (this.attack.getValue()) {
               this.l();
            }

            if (this.place.getValue()) {
               this.b_void_bool(false);
            }
         }
      }
   }

   @EventTarget
   public void a_void_EntitySpawnEvent(EntitySpawnEvent var1) {
      if (this.isEnabled() && !this.j() && !this.n().isEmpty() && !this.b_bool_String("Attack")) {
         if (this.attack.getValue() && this.instant.getValue()) {
            TimerUtil var2 = this.af;
            float var3 = this.attackSpeed.getValue().floatValue() * 50.0F;
            Float var4 = 1000.0F - var3;
            if (var2.passedMs(var4)) {
               if (var1.getEntity() instanceof EndCrystal var6) {
                  if (this.inhibit.getValue()) {
                     Map var7 = this.ac;
                     Integer var8 = var6.getId();
                     if (var7.containsKey(var8)) {
                        return;
                     }
                  }

                  Map var9 = this.ad;
                  BlockPos var10 = var6.blockPosition().below();
                  if (var9.containsKey(var10)) {
                     AABB var11 = var6.getBoundingBox();
                     Vec3 var12 = mc.player.getEyePosition();
                     double var13 = var11.distanceToSqr(var12);
                     double var15 = Mth.square(this.attackRange.getValue().doubleValue());
                     if (!(var13 > var15)) {
                        WorldBorder var17 = mc.level.getWorldBorder();
                        BlockPos var18 = var6.blockPosition();
                        if (var17.isWithinBounds(var18)) {
                           if (!BlockInteractionUtil.a_bool_Entity(var6)) {
                              if (this.raytrace.getValue()) {
                                 return;
                              }

                              AABB var19 = var6.getBoundingBox();
                              Vec3 var20 = mc.player.getEyePosition();
                              double var21 = var19.distanceToSqr(var20);
                              double var23 = Mth.square(this.attackWallsRange.getValue().doubleValue());
                              if (var21 > var23) {
                                 return;
                              }
                           }

                           if (!this.normal.getValue().equalsIgnoreCase("None")) {
                              String var25 = this.normal.getValue();
                              float[] var26 = RotationMathUtil.a(Vec3.atCenterOf(var6.blockPosition()));
                              Night.rotationManager.rotateByMode(var25, var26);
                           }

                           EndCrystal var27 = var6;
                           this.b_void_EndCrystal(var27);
                           this.ap = true;
                           if (this.strong.getValue().equalsIgnoreCase("Strong")) {
                              this.b_void_bool(true);
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   @EventTarget
   public void a_void_BlockPlaceEvent(BlockPlaceEvent var1) {
      if (this.isEnabled() && !this.j() && !this.n().isEmpty() && !this.b_bool_String("Place")) {
         this.aB = 0;
         if (this.blockDestruction.getValue()) {
            TimerUtil var2 = this.ag;
            float var3 = this.placeSpeed.getValue().floatValue() * 50.0F;
            Float var4 = 1000.0F - var3;
            if (var2.passedMs(var4)) {
               BlockPos var5 = var1.getPos();
               if (var5 != null) {
                  int var6 = InventoryUtil.b_int_Item(Items.END_CRYSTAL);
                  int var7 = mc.player.getInventory().getSelectedSlot();
                  boolean var8 = false;
                  if (this.none2.getValue().equalsIgnoreCase("None")
                     || var6 != -1
                     || mc.player.getMainHandItem().getItem() == Items.END_CRYSTAL
                     || mc.player.getOffhandItem().getItem() == Items.END_CRYSTAL) {
                     ProxyAutoCrystal.PlaceTarget var9;
                     if (this.au == null) {
                        var9 = null;
                     } else {
                        var9 = this.au.a();
                     }

                     ProxyAutoCrystal.PlaceTarget var10;
                     label151: {
                        var10 = var9;
                        if (var10 != null) {
                           if (var10.b() == null) {
                              break label151;
                           }

                           BlockPos var11 = var5;
                           BlockPos var12 = var10.e();
                           if (var11.equals(var12)) {
                              break label151;
                           }
                        }

                        BlockPos var13 = var5;
                        var10 = this.a_PlaceTarget_BlockPos(var13);
                     }

                     if (var10 != null && var10.b() != null && !(var10.f() <= 0.0F)) {
                        BlockPos var14 = var10.b();
                        Vec3 var15 = mc.player.getEyePosition();
                        Vec3 var16 = Vec3.atCenterOf(var14);
                        double var17 = var15.distanceToSqr(var16);
                        double var19 = Mth.square(this.placeRange.getValue().doubleValue());
                        if (var17 > var19) {
                           Night.renderPlacementManager.setCrystalTarget(null);
                        } else {
                           BlockPos var21 = var14;
                           Night.renderPlacementManager.setCrystalTarget(var21);
                           if (!BlockInteractionUtil.f(var14)) {
                              if (this.raytrace.getValue()) {
                                 return;
                              }

                              Vec3 var22 = mc.player.getEyePosition();
                              Vec3 var23 = Vec3.atCenterOf(var14);
                              double var24 = var22.distanceToSqr(var23);
                              double var26 = Mth.square(this.placeWallsRange.getValue().doubleValue());
                              if (var24 > var26) {
                                 return;
                              }
                           }

                           EndCrystal var28 = null;
                           ClientLevel var29 = mc.level;
                           Entity var30 = (Entity)null;
                           BlockPos var31 = var14.above();

                           for (Entity var33 : (Iterable<Entity>)(Object)var29.getEntities(var30, new AABB(var31), var0 -> true)) {
                              if (var33 instanceof EndCrystal) {
                                 var28 = (EndCrystal)var33;
                                 break;
                              }
                           }

                           if (var28 != null) {
                              EndCrystal var34 = var28;
                              if (this.a_bool_EndCrystal(var34)) {
                                 Night.renderPlacementManager.setCrystalTarget(null);
                              } else if (!this.attack.getValue()) {
                                 Night.renderPlacementManager.setCrystalTarget(null);
                              } else {
                                 if (!this.normal.getValue().equalsIgnoreCase("None")) {
                                    String var58 = this.normal.getValue();
                                    float[] var59 = RotationMathUtil.a(var28);
                                    Night.rotationManager.rotateByMode(var58, var59);
                                 }

                                 EndCrystal var60 = var28;
                                 this.b_void_EndCrystal(var60);
                              }
                           } else {
                              CrystalRaytraceHelper.HitCandidate var35 = CrystalRaytraceHelper.a(var14);
                              Vec3 var36;
                              if (var35 != null && var35.b != null) {
                                 var36 = var35.b;
                              } else {
                                 Vec3 var37 = Vec3.atCenterOf(var14);
                                 double var38 = 0.0;
                                 double var40 = 0.5;
                                 double var42 = 0.0;
                                 var36 = var37.add(var38, var40, var42);
                              }

                              Vec3 var44 = var36;
                              if (!this.normal.getValue().equalsIgnoreCase("None")) {
                                 String var45 = this.normal.getValue();
                                 float[] var46 = RotationMathUtil.a(var44);
                                 Night.rotationManager.rotateByMode(var45, var46);
                              }

                              SpeedMine var47 = Night.moduleManager.getModule(SpeedMine.class);
                              boolean var48;
                              if (!var47.switchReset.getValue()
                                 || !var47.switchSetting.getValue().equalsIgnoreCase("Normal")
                                    && !var47.switchSetting.getValue().equalsIgnoreCase("AltSwap")
                                    && !var47.switchSetting.getValue().equalsIgnoreCase("AltPickup")) {
                                 var48 = false;
                              } else {
                                 var48 = true;
                              }

                              boolean var49 = var48;
                              if (!this.none2.getValue().equalsIgnoreCase("None") && mc.player.getOffhandItem().getItem() != Items.END_CRYSTAL) {
                                 if (!var49 && this.none2.getValue().equalsIgnoreCase("Normal") && this.swapBack.getValue() && this.aC == -1) {
                                    int var50 = var7;
                                    this.aC = var50;
                                 }

                                 String var51;
                                 if (var49) {
                                    var51 = "AltSwap";
                                 } else {
                                    var51 = this.none2.getValue();
                                 }

                                 int var52 = var6;
                                 int var53 = var7;
                                 InventoryUtil.a_bool_String_int_int(var51, var52, var53);
                                 var8 = true;
                              }

                              BlockPos var54 = var14;
                              this.c_void_BlockPos(var54);
                              if (var8) {
                                 String var55;
                                 if (var49) {
                                    var55 = "AltSwap";
                                 } else {
                                    var55 = this.none2.getValue();
                                 }

                                 int var56 = var6;
                                 int var57 = var7;
                                 InventoryUtil.b(var55, var56, var57);
                              }
                           }
                        }
                     } else {
                        Night.renderPlacementManager.setCrystalTarget(null);
                     }
                  }
               }
            }
         }
      }
   }

   @EventTarget
   public void a_void_PacketReceiveEvent(PacketReceiveEvent var1) {
      if (mc.player != null && mc.level != null) {
         if (var1.getPacket() instanceof ClientboundAddEntityPacket var3) {
            int var4 = var3.getId();
            int var5 = this.aA;
            if (var4 > var5) {
               int var6 = var3.getId();
               this.aA = var6;
            }

            double var7 = var3.getX();
            double var9 = var3.getY();
            double var11 = var3.getZ();
            BlockPos var13 = BlockPos.containing(var7, var9, var11).offset(0, -1, 0);
            Map var14 = this.ae;
            BlockPos var15 = var13;
            if (var14.containsKey(var15)) {
               TimerUtil var16 = this.ah;
               long var17 = this.faceplaceDelay.getValue().longValue();
               long var19 = 50L;
               Long var21 = var17 * var19;
               if (var16.passedMs(var21)) {
                  this.ah.reset();
               }

               Map var22 = this.ae;
               BlockPos var23 = var13;
               var22.remove(var23);
               this.ay.increment();
               int var24 = this.ay.getCount();
               this.az = var24;
            }
         }
      }
   }

   @EventTarget
   public void a_void_PlayerDeathEvent(PlayerDeathEvent var1) {
      this.aB = 0;
      this.aa = null;
      this.ab = null;
      this.ar = null;
      this.at = null;
      this.as = null;
      this.au = null;
      this.ad.clear();
      this.ac.clear();
      this.ae.clear();
      Night.renderPlacementManager.setCrystalTarget(null);
   }

   @EventTarget
   public void a_void_PacketSendEvent(PacketSendEvent var1) {
      if (var1.getPacket() instanceof ServerboundSetCarriedItemPacket) {
         this.aj.reset();
      }

      if (var1.getPacket() instanceof ServerboundUseItemPacket && mc.player != null) {
         if (mc.player.getMainHandItem().getItem() == Items.ENDER_PEARL || mc.player.getOffhandItem().getItem() == Items.ENDER_PEARL) {
            long var2 = System.currentTimeMillis();
            this.aE = var2;
         } else if (mc.player.getMainHandItem().getItem() == Items.EXPERIENCE_BOTTLE || mc.player.getOffhandItem().getItem() == Items.EXPERIENCE_BOTTLE) {
            long var4 = System.currentTimeMillis();
            this.aF = var4;
         }
      }
   }

   @EventTarget
   public void a_void_ClientConnectEvent(ClientConnectEvent var1) {
      this.aA = -100000;
   }

   public String g() {
      String var1 = this.av;
      int var2 = this.aw;
      String var3 = this.ax;
      int var4 = this.az;
      return var1 + ", " + var2 + ", " + var3 + ", " + var4;
   }

   private void l() {
      if (!this.j() && !this.b_bool_String("Attack")) {
         if (this.n().isEmpty()) {
            this.as = null;
         } else {
            EndCrystal var1 = null;
            ProxyAutoCrystal.PlaceTarget var2 = this.at;
            boolean var3;
            if (var2 != null && var2.b() == null && var2.c != null && !var2.c.isEmpty()) {
               var3 = true;
            } else {
               var3 = false;
            }

            boolean var4 = var3;
            Iterable var5;
            if (var4) {
               var5 = var2.c;
            } else {
               var5 = mc.level.entitiesForRendering();
            }

            for (Entity var7 : (Iterable<Entity>)(Object)var5) {
               if (var7 instanceof EndCrystal var8 && var8.isAlive()) {
                  if (this.inhibit.getValue()) {
                     Map var9 = this.ac;
                     Integer var10 = var7.getId();
                     if (var9.containsKey(var10)) {
                        continue;
                     }
                  }

                  if (!var4) {
                     Map var11 = this.ad;
                     BlockPos var12 = var8.blockPosition().below();
                     if (!var11.containsKey(var12)) {
                        continue;
                     }
                  }

                  AABB var13 = var8.getBoundingBox();
                  Vec3 var14 = mc.player.getEyePosition();
                  double var15 = var13.distanceToSqr(var14);
                  double var17 = Mth.square(this.attackRange.getValue().doubleValue());
                  if (!(var15 > var17)) {
                     WorldBorder var19 = mc.level.getWorldBorder();
                     BlockPos var20 = var8.blockPosition();
                     if (var19.isWithinBounds(var20)) {
                        if (!BlockInteractionUtil.a_bool_Entity(var8)) {
                           if (this.raytrace.getValue()) {
                              continue;
                           }

                           AABB var21 = var8.getBoundingBox();
                           Vec3 var22 = mc.player.getEyePosition();
                           double var23 = var21.distanceToSqr(var22);
                           double var25 = Mth.square(this.attackWallsRange.getValue().doubleValue());
                           if (var23 > var25) {
                              continue;
                           }
                        }

                        var1 = var8;
                        break;
                     }
                  }
               }
            }

            EndCrystal var27;
            if (var1 == null) {
               var27 = this.as;
            } else {
               var27 = var1;
            }

            EndCrystal var28 = var27;
            if (var28 != null) {
               if (this.swapDelay.getValue().intValue() > 0) {
                  TimerUtil var29 = this.aj;
                  long var30 = this.swapDelay.getValue().longValue();
                  long var32 = 50L;
                  Long var34 = var30 * var32;
                  if (!var29.passedMs(var34)) {
                     return;
                  }
               }

               TimerUtil var35 = this.af;
               float var36 = this.attackSpeed.getValue().floatValue() * 50.0F;
               Float var37 = 1000.0F - var36;
               if (var35.passedMs(var37) && !this.ap) {
                  ClientLevel var38 = mc.level;
                  int var39 = var28.getId();
                  Entity var40 = var38.getEntity(var39);
                  String var41 = null;
                  if (var40 == null) {
                     var41 = "entity-gone";
                  } else if (!(var40 instanceof EndCrystal)) {
                     var41 = "not-end-crystal";
                  } else if (!((EndCrystal)var40).isAlive()) {
                     var41 = "dead";
                  } else {
                     label91: {
                        if (this.inhibit.getValue()) {
                           Map var42 = this.ac;
                           Integer var43 = var40.getId();
                           if (var42.containsKey(var43)) {
                              var41 = "inhibit";
                              break label91;
                           }
                        }

                        AABB var44 = var40.getBoundingBox();
                        Vec3 var45 = mc.player.getEyePosition();
                        double var46 = var44.distanceToSqr(var45);
                        double var48 = Mth.square(this.attackRange.getValue().doubleValue());
                        if (var46 > var48) {
                           var41 = "range";
                        } else {
                           WorldBorder var50 = mc.level.getWorldBorder();
                           BlockPos var51 = var40.blockPosition();
                           if (!var50.isWithinBounds(var51)) {
                              var41 = "border";
                           } else {
                              label85:
                              if (!BlockInteractionUtil.a_bool_Entity(var40)) {
                                 if (!this.raytrace.getValue()) {
                                    AABB var52 = var40.getBoundingBox();
                                    Vec3 var53 = mc.player.getEyePosition();
                                    double var54 = var52.distanceToSqr(var53);
                                    double var56 = Mth.square(this.attackWallsRange.getValue().doubleValue());
                                    if (!(var54 > var56)) {
                                       break label85;
                                    }
                                 }

                                 var41 = "cannot-see";
                              }
                           }
                        }
                     }
                  }

                  if (var41 == null) {
                     EndCrystal var58 = var28;
                     this.aa = () -> {
                        if (!this.normal.getValue().equalsIgnoreCase("None")) {
                           String var2x = this.normal.getValue();
                           float[] var3x = RotationMathUtil.a(Vec3.atCenterOf(var58.blockPosition()));
                           Night.rotationManager.rotateByMode(var2x, var3x);
                        }

                        this.b_void_EndCrystal(var58);
                     };
                  }
               } else {
                  if (this.ap) {
                     this.ap = false;
                  }
               }
            }
         }
      }
   }

   private void b_void_bool(boolean var1) {
      if (!this.j() && !this.b_bool_String("Place")) {
         ProxyAutoCrystal.PlaceTarget var2;
         if (this.at == null) {
            var2 = null;
         } else {
            var2 = this.at.a();
         }

         ProxyAutoCrystal.PlaceTarget var3 = var2;
         if (var3 != null && var3.b() != null) {
            if (var3.c() != null && var3.c().isAlive() && !var3.c().isDeadOrDying() && !(var3.c().getHealth() <= 0.0F)) {
               int var4 = InventoryUtil.b_int_Item(Items.END_CRYSTAL);
               int var5 = mc.player.getInventory().getSelectedSlot();
               if (this.none2.getValue().equalsIgnoreCase("None")
                  || var4 != -1
                  || mc.player.getMainHandItem().getItem() == Items.END_CRYSTAL
                  || mc.player.getOffhandItem().getItem() == Items.END_CRYSTAL) {
                  BlockPos var6 = var3.b();
                  Vec3 var7 = mc.player.getEyePosition();
                  Vec3 var8 = Vec3.atCenterOf(var6);
                  double var9 = var7.distanceToSqr(var8);
                  double var11 = Mth.square(this.placeRange.getValue().doubleValue());
                  if (var9 > var11) {
                     Night.renderPlacementManager.setCrystalTarget(null);
                  } else {
                     BlockPos var13 = var6;
                     Night.renderPlacementManager.setCrystalTarget(var13);
                     WorldBorder var14 = mc.level.getWorldBorder();
                     BlockPos var15 = var6;
                     if (var14.isWithinBounds(var15)) {
                        ClientLevel var16 = mc.level;
                        BlockPos var17 = var6;
                        if (var16.getBlockState(var17).getBlock() != Blocks.OBSIDIAN) {
                           ClientLevel var18 = mc.level;
                           BlockPos var19 = var6;
                           if (var18.getBlockState(var19).getBlock() != Blocks.BEDROCK) {
                              return;
                           }
                        }

                        ClientLevel var20 = mc.level;
                        BlockPos var21 = var6.offset(0, 1, 0);
                        if (var20.getBlockState(var21).isAir()) {
                           if (this.nativeSetting.getValue().equalsIgnoreCase("Protocol")) {
                              ClientLevel var22 = mc.level;
                              BlockPos var23 = var6.offset(0, 2, 0);
                              if (!var22.getBlockState(var23).isAir()) {
                                 return;
                              }
                           }

                           if (!BlockInteractionUtil.f(var6)) {
                              if (this.raytrace.getValue()) {
                                 return;
                              }

                              Vec3 var24 = mc.player.getEyePosition();
                              Vec3 var25 = Vec3.atCenterOf(var6);
                              double var26 = var24.distanceToSqr(var25);
                              double var28 = Mth.square(this.placeWallsRange.getValue().doubleValue());
                              if (var26 > var28) {
                                 return;
                              }
                           }

                           ClientLevel var30 = mc.level;
                           Entity var31 = (Entity)null;
                           BlockPos var32 = var6.offset(0, 1, 0);
                           if (!var30.getEntities(var31, new AABB(var32), var0 -> true).stream().anyMatch(var0 -> {
                              boolean var1x;
                              if (var0.isAlive() && !(var0 instanceof ExperienceOrb) && !(var0 instanceof EndCrystal)) {
                                 var1x = true;
                              } else {
                                 var1x = false;
                              }

                              return var1x;
                           })) {
                              if (this.swapDelay.getValue().intValue() > 0) {
                                 TimerUtil var33 = this.aj;
                                 long var34 = this.swapDelay.getValue().longValue();
                                 long var36 = 50L;
                                 Long var38 = var34 * var36;
                                 if (!var33.passedMs(var38)) {
                                    return;
                                 }
                              }

                              TimerUtil var39 = this.ag;
                              float var40 = this.placeSpeed.getValue().floatValue() * 50.0F;
                              Float var41 = 1000.0F - var40;
                              if (var39.passedMs(var41)) {
                                 if (!var1 && this.aq) {
                                    this.aq = false;
                                 } else {
                                    BlockPos var42 = var6;
                                    int var43 = var5;
                                    int var44 = var4;
                                    this.ab = () -> {
                                       boolean var4x = false;
                                       CrystalRaytraceHelper.HitCandidate var5x = CrystalRaytraceHelper.a(var42);
                                       Vec3 var6x;
                                       if (var5x != null && var5x.b != null) {
                                          var6x = var5x.b;
                                       } else {
                                          Vec3 var7x = Vec3.atCenterOf(var42);
                                          double var8x = 0.0;
                                          double var10 = 0.5;
                                          double var12 = 0.0;
                                          var6x = var7x.add(var8x, var10, var12);
                                       }

                                       Vec3 var14x = var6x;
                                       if (!this.normal.getValue().equalsIgnoreCase("None")) {
                                          String var15x = this.normal.getValue();
                                          float[] var16x = RotationMathUtil.a(var14x);
                                          Night.rotationManager.rotateByMode(var15x, var16x);
                                       }

                                       if (mc.player.getOffhandItem().getItem() != Items.END_CRYSTAL) {
                                          if (this.none2.getValue().equalsIgnoreCase("Normal") && this.swapBack.getValue() && this.aC == -1) {
                                             this.aC = var43;
                                          }

                                          InventoryUtil.a_bool_String_int_int(this.none2.getValue(), var44, var43);
                                          var4x = true;
                                       }

                                       this.c_void_BlockPos(var42);
                                       if (var4x) {
                                          InventoryUtil.b(this.none2.getValue(), var44, var43);
                                       }

                                       if (this.godSync.getValue()) {
                                          boolean var17x;
                                          if (this.antiKick.getValue()
                                             && (
                                                mc.player.getMainHandItem().getItem() instanceof ExperienceBottleItem
                                                   || mc.player.getOffhandItem().getItem() instanceof ExperienceBottleItem
                                                   || Night.moduleManager.getModule(KeyAction.class).v()
                                             )) {
                                             var17x = false;
                                          } else {
                                             var17x = true;
                                          }

                                          label106: {
                                             boolean var18x = var17x;
                                             if (this.antiKick.getValue()) {
                                                int var19x = this.aB;
                                                int var20x = this.kickThreshold.getValue().intValue();
                                                if (var19x <= var20x) {
                                                   break label106;
                                                }
                                             }

                                             if (var18x) {
                                                if (!this.fast.getValue()) {
                                                   for (Entity var22x : (Iterable<Entity>)(Object)mc.level.entitiesForRendering()) {
                                                      int var23x = var22x.getId();
                                                      int var24x = this.aA;
                                                      if (var23x > var24x) {
                                                         int var25x = var22x.getId();
                                                         this.aA = var25x;
                                                      }
                                                   }
                                                }

                                                int var26x = this.offset.getValue().intValue();
                                                int var27 = 1 - var26x;

                                                while (true) {
                                                   int var28x = var27;
                                                   int var29 = this.predictions.getValue().intValue();
                                                   if (var28x >= var29) {
                                                      if (this.normal2.getValue().equals("Normal")) {
                                                         ClientPacketListener var43x = mc.getConnection();
                                                         Packet var44x = PacketAndRenderUtil.a_Packet_InteractionHand(InteractionHand.MAIN_HAND);
                                                         var43x.send(var44x);
                                                      }
                                                      break;
                                                   }

                                                   ClientLevel var30x = mc.level;
                                                   int var31x = this.aA;
                                                   Entity var32x = var30x.getEntity(var31x);
                                                   if (var32x == null || var32x instanceof EndCrystal) {
                                                      int var33x = this.aA;
                                                      int var34x = var27;
                                                      int var35 = var33x + var34x;
                                                      ClientPacketListener var36x = mc.getConnection();
                                                      int var37 = var35;
                                                      var36x.send(new ServerboundAttackPacket(var37));
                                                      if (this.normal2.getValue().equals("Strict")) {
                                                         ClientPacketListener var38x = mc.getConnection();
                                                         Packet var39x = PacketAndRenderUtil.a_Packet_InteractionHand(InteractionHand.MAIN_HAND);
                                                         var38x.send(var39x);
                                                      }

                                                      Map var40x = this.ac;
                                                      Integer var41x = var35;
                                                      Long var42x = System.currentTimeMillis();
                                                      var40x.put(var41x, var42x);
                                                   }

                                                   var27++;
                                                }
                                             }
                                          }

                                          int var45 = this.aB + 1;
                                          this.aB = var45;
                                       }
                                    };
                                    if (var1) {
                                       this.ab.run();
                                       this.ab = null;
                                       this.aq = true;
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            } else {
               this.at = null;
               this.ar = null;
               Night.renderPlacementManager.setCrystalTarget(null);
            }
         } else {
            Night.renderPlacementManager.setCrystalTarget(null);
         }
      }
   }

   private EndCrystal m() {
      if (!this.attack.getValue()) {
         return null;
      }

      if (this.b_bool_String("Attack")) {
         return null;
      }

      List var1 = this.n();
      if (var1.isEmpty()) {
         return null;
      }

      EndCrystal var2 = null;
      float var3 = 0.0F;

      for (Entity var5 : (Iterable<Entity>)(Object)mc.level.entitiesForRendering()) {
         if (var5 instanceof EndCrystal var6 && var6.isAlive()) {
            if (this.inhibit.getValue()) {
               Map var7 = this.ac;
               Integer var8 = var5.getId();
               if (var7.containsKey(var8)) {
                  continue;
               }
            }

            AABB var9 = var6.getBoundingBox();
            Vec3 var10 = mc.player.getEyePosition();
            double var11 = var9.distanceToSqr(var10);
            double var13 = Mth.square(this.attackRange.getValue().doubleValue());
            if (!(var11 > var13)) {
               WorldBorder var15 = mc.level.getWorldBorder();
               BlockPos var16 = var6.blockPosition();
               if (var15.isWithinBounds(var16)) {
                  if (!BlockInteractionUtil.a_bool_Entity(var6)) {
                     if (this.raytrace.getValue()) {
                        continue;
                     }

                     AABB var17 = var6.getBoundingBox();
                     Vec3 var18 = mc.player.getEyePosition();
                     double var19 = var17.distanceToSqr(var18);
                     double var21 = Mth.square(this.attackWallsRange.getValue().doubleValue());
                     if (var19 > var21) {
                        continue;
                     }
                  }

                  if (!Night.moduleManager.getModule(Suicide.class).isEnabled()) {
                     LocalPlayer var23 = mc.player;
                     EndCrystal var24 = var6;
                     boolean var25 = this.ignoreTerrain.getValue();
                     float var26 = DamageUtil.a_float_Entity_AABB_EndCrystal_bool(var23, null, var24, var25);
                     float var27 = this.maximumSelfDamage.getValue().floatValue();
                     if (var26 > var27) {
                        continue;
                     }

                     if (this.antiSuicide.getValue()) {
                        float var28 = var26;
                        float var29 = mc.player.getHealth();
                        float var30 = mc.player.getAbsorptionAmount();
                        float var31 = var29 + var30;
                        if (var28 > var31) {
                           continue;
                        }
                     }
                  }

                  boolean var32 = false;
                  Iterator var33 = var1.iterator();

                  while (true) {
                     if (var33.hasNext()) {
                        Player var34 = (Player)var33.next();
                        int var35 = this.extrapolation.getValue().intValue();
                        AABB var36 = PositionUtil.a(var34, var35);
                        EndCrystal var37 = var6;
                        boolean var38 = this.ignoreTerrain.getValue();
                        float var39 = DamageUtil.a_float_Entity_AABB_EndCrystal_bool(var34, var36, var37, var38);
                        Player var40 = var34;
                        float var41 = this.minimumDamage.getValue().floatValue();
                        float var42 = this.a_float_Player_float(var40, var41);
                        if (var39 < var42) {
                           float var43 = var39;
                           float var44 = var34.getHealth();
                           float var45 = var34.getAbsorptionAmount();
                           float var46 = var44 + var45;
                           if (var43 < var46) {
                              float var47 = var39;
                              float var48 = this.lethalMultiplier.getValue().floatValue();
                              float var49 = 1.0F + var48;
                              float var50 = var47 * var49;
                              float var51 = var34.getHealth();
                              float var52 = var34.getAbsorptionAmount();
                              float var53 = var51 + var52;
                              if (!(var50 >= var53)) {
                                 continue;
                              }
                           }
                        }

                        float var54 = var39;
                        float var55 = var3;
                        if (!(var54 > var55)) {
                           float var56 = var39;
                           float var57 = var34.getHealth();
                           float var58 = var34.getAbsorptionAmount();
                           float var59 = var57 + var58;
                           if (!(var56 > var59)) {
                              continue;
                           }
                        }

                        var2 = var6;
                        var3 = var39;
                        float var60 = var39;
                        float var61 = var34.getHealth();
                        float var62 = var34.getAbsorptionAmount();
                        float var63 = var61 + var62;
                        if (!(var60 > var63)) {
                           continue;
                        }

                        var32 = true;
                     }

                     if (var32) {
                        return var2;
                     }
                     break;
                  }
               }
            }
         }
      }

      return var2;
   }

   private ProxyAutoCrystal.PlaceTarget a_PlaceTarget_BlockPos(BlockPos var1) {
      if (!this.place.getValue()) {
         return null;
      }

      if (!this.b_bool_String("Place")
         && (
            !this.none2.getValue().equalsIgnoreCase("None") && InventoryUtil.b_int_Item(Items.END_CRYSTAL) != -1
               || mc.player.getMainHandItem().getItem() == Items.END_CRYSTAL
               || mc.player.getOffhandItem().getItem() == Items.END_CRYSTAL
         )) {
         List var2 = this.n();
         if (var2.isEmpty()) {
            return null;
         }

         BlockPos var3 = null;
         Player var4 = null;
         ArrayList var5 = new ArrayList();
         float var6 = 0.0F;
         BlockPos var7;
         if (this.at == null) {
            var7 = null;
         } else {
            var7 = this.at.b();
         }

         BlockPos var8 = var7;
         int var9 = 0;
         int var10 = 0;

         while (true) {
            int var11 = var10;
            double var12 = this.placeRange.getValue().doubleValue();
            double var14 = this.placeWallsRange.getValue().doubleValue();
            double var16 = Math.max(var12, var14);
            int var18 = Night.worldStateManager.getSpherePointCount(var16);
            if (var11 >= var18) {
               break;
            }

            BlockPos var19 = mc.player.blockPosition();
            int var20 = var10;
            Vec3i var21 = Night.worldStateManager.getSphereOffset(var20);
            BlockPos var22 = var19.offset(var21);
            Vec3 var23 = mc.player.getEyePosition();
            Vec3 var24 = Vec3.atCenterOf(var22);
            double var25 = var23.distanceToSqr(var24);
            double var27 = Mth.square(this.placeRange.getValue().doubleValue());
            if (!(var25 > var27)) {
               WorldBorder var29 = mc.level.getWorldBorder();
               BlockPos var30 = var22;
               label117:
               if (var29.isWithinBounds(var30)) {
                  ClientLevel var31 = mc.level;
                  BlockPos var32 = var22;
                  if (var31.getBlockState(var32).getBlock() != Blocks.OBSIDIAN) {
                     ClientLevel var33 = mc.level;
                     BlockPos var34 = var22;
                     if (var33.getBlockState(var34).getBlock() != Blocks.BEDROCK) {
                        break label117;
                     }
                  }

                  ClientLevel var35 = mc.level;
                  BlockPos var36 = var22.offset(0, 1, 0);
                  label112:
                  if (var35.getBlockState(var36).isAir()) {
                     if (this.nativeSetting.getValue().equalsIgnoreCase("Protocol")) {
                        ClientLevel var37 = mc.level;
                        BlockPos var38 = var22.offset(0, 2, 0);
                        if (!var37.getBlockState(var38).isAir()) {
                           break label112;
                        }
                     }

                     if (!BlockInteractionUtil.f(var22)) {
                        if (this.raytrace.getValue()) {
                           break label112;
                        }

                        Vec3 var39 = mc.player.getEyePosition();
                        Vec3 var40 = Vec3.atCenterOf(var22);
                        double var41 = var39.distanceToSqr(var40);
                        double var43 = Mth.square(this.placeWallsRange.getValue().doubleValue());
                        if (var41 > var43) {
                           break label112;
                        }
                     }

                     ClientLevel var45 = mc.level;
                     Entity var46 = (Entity)null;
                     BlockPos var47 = var22.offset(0, 1, 0);
                     label101:
                     if (!var45.getEntities(var46, new AABB(var47), var0 -> true).stream().anyMatch(var0 -> {
                        boolean var1x;
                        if (var0.isAlive() && !(var0 instanceof ExperienceOrb) && !(var0 instanceof EndCrystal)) {
                           var1x = true;
                        } else {
                           var1x = false;
                        }

                        return var1x;
                     })) {
                        ClientLevel var48 = mc.level;
                        Entity var49 = (Entity)null;
                        BlockPos var50 = var22.offset(0, 1, 0);
                        List var51 = var48.getEntities(var49, new AABB(var50), var0 -> true).stream().filter(var1x -> {
                           if (var1x instanceof EndCrystal var2x) {
                              Map var3x = this.ad;
                              BlockPos var4x = var2x.blockPosition().below();
                              if (!var3x.containsKey(var4x)) {
                                 return true;
                              }

                              int var5x = var2x.tickCount;
                              int var6x = this.attackSpeed.getValue().intValue();
                              int var7x = 20 - var6x + 15;
                              if (var5x >= var7x) {
                                 return true;
                              }
                           }

                           return false;
                        }).toList();
                        if (!Night.moduleManager.getModule(Suicide.class).isEnabled()) {
                           LocalPlayer var52 = mc.player;
                           BlockPos var53 = var22;
                           boolean var54 = this.ignoreTerrain.getValue();
                           float var55 = DamageUtil.a(var52, null, var53, var1, var54);
                           float var56 = this.maximumSelfDamage.getValue().floatValue();
                           if (var55 > var56) {
                              break label101;
                           }

                           if (this.antiSuicide.getValue()) {
                              float var57 = var55;
                              float var58 = mc.player.getHealth();
                              float var59 = mc.player.getAbsorptionAmount();
                              float var60 = var58 + var59;
                              if (var57 > var60) {
                                 break label101;
                              }
                           }
                        }

                        boolean var61 = false;

                        for (Player var63 : (Iterable<Player>)(Object)var2) {
                           var9++;
                           Player var64 = var63;
                           Player var65 = var63;
                           int var66 = this.extrapolation.getValue().intValue();
                           AABB var67 = PositionUtil.a(var65, var66);
                           BlockPos var68 = var22;
                           boolean var69 = this.ignoreTerrain.getValue();
                           float var70 = DamageUtil.a(var64, var67, var68, var1, var69);
                           Player var71 = var63;
                           float var72 = this.minimumDamage.getValue().floatValue();
                           float var73 = this.a_float_Player_float(var71, var72);
                           if (var70 < var73) {
                              float var74 = var70;
                              float var75 = var63.getHealth();
                              float var76 = var63.getAbsorptionAmount();
                              float var77 = var75 + var76;
                              if (var74 < var77) {
                                 float var78 = var70;
                                 float var79 = this.lethalMultiplier.getValue().floatValue();
                                 float var80 = 1.0F + var79;
                                 float var81 = var78 * var80;
                                 float var82 = var63.getHealth();
                                 float var83 = var63.getAbsorptionAmount();
                                 float var84 = var82 + var83;
                                 if (!(var81 >= var84)) {
                                    continue;
                                 }
                              }
                           }

                           if (var1 == null && !var51.isEmpty()) {
                              ArrayList var85 = var5;
                              Entity var86 = (Entity)var51.getFirst();
                              var85.add(var86);
                              break;
                           }

                           float var87 = var70;
                           BlockPos var88 = var22;
                           BlockPos var89 = var8;
                           float var90;
                           if (var88.equals(var89)) {
                              var90 = 0.5F;
                           } else {
                              var90 = 0.0F;
                           }

                           float var91 = var87 + var90;
                           float var92 = var6;
                           if (!(var91 > var92)) {
                              float var93 = var70;
                              float var94 = var63.getHealth();
                              float var95 = var63.getAbsorptionAmount();
                              float var96 = var94 + var95;
                              if (!(var93 > var96)) {
                                 continue;
                              }
                           }

                           var3 = var22;
                           var4 = var63;
                           var6 = var91;
                           float var97 = var70;
                           float var98 = var63.getHealth();
                           float var99 = var63.getAbsorptionAmount();
                           float var100 = var98 + var99;
                           if (var97 > var100) {
                              var61 = true;
                              break;
                           }
                        }

                        if (var61) {
                           break;
                        }
                     }
                  }
               }
            }

            var10++;
         }

         if (var3 == null) {
            ArrayList var101 = var5;
            int var102 = var9;
            return new ProxyAutoCrystal.PlaceTarget(null, null, var101, null, 0.0F, var102);
         } else {
            BlockPos var103 = var3;
            Player var104 = var4;
            ArrayList var105 = var5;
            float var106 = var6;
            int var107 = var9;
            return new ProxyAutoCrystal.PlaceTarget(var103, var104, var105, var1, var106, var107);
         }
      } else {
         return null;
      }
   }

   private float[] a_floatArr_Vec3(Vec3 var1) {
      float[] var2 = RotationMathUtil.a(var1);
      if (this.yawStep.getValue()) {
         float var3 = Night.rotationManager.getServerYaw();
         float var4 = var2[0];
         float var5 = var3 - var4;
         if (Math.abs(var5) > 180.0F) {
            float var6 = var5;
            float var7;
            if (var5 > 0.0F) {
               var7 = -360.0F;
            } else {
               var7 = 360.0F;
            }

            var5 = var6 + var7;
         }

         byte var8;
         if (var5 > 0.0F) {
            var8 = -1;
         } else {
            var8 = 1;
         }

         float var9 = var8;
         float var10 = this.yawStepThreshold.getValue().floatValue();
         float var11 = var9 * var10;
         float var12 = Math.abs(var5);
         float var13 = this.yawStepThreshold.getValue().floatValue();
         float var16;
         if (var12 > var13) {
            float var14 = Night.rotationManager.getServerYaw();
            float var15 = var11;
            var16 = var14 + var15;
         } else {
            var16 = var2[0];
         }

         float[] var17 = var2;
         float var18 = var16;
         var17[0] = var18;
      }

      return var2;
   }

   private boolean b_bool_BlockPos(BlockPos var1) {
      if (var1 != null && mc.level != null) {
         AABB var2 = new AABB(var1);
         double var3 = 1.5;
         double var5 = 2.0;
         double var7 = 1.5;
         AABB var9 = var2.inflate(var3, var5, var7);

         for (Player var11 : (Iterable<Player>)(Object)mc.level.players()) {
            LocalPlayer var12 = mc.player;
            if (!var11.equals(var12) && !EntityUtil.c_bool_Entity(var11) && var11.isAlive()) {
               if (Night.friendManager != null) {
                  String var13 = var11.getName().getString();
                  if (Night.friendManager.isFriend(var13)) {
                     continue;
                  }
               }

               AABB var14 = var11.getBoundingBox();
               AABB var15 = var9;
               if (var14.intersects(var15)) {
                  return true;
               }
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private boolean a_bool_EndCrystal_BlockPos(EndCrystal var1, BlockPos var2) {
      if (var1 != null && var2 != null) {
         BlockPos var3 = var1.blockPosition().below();
         int var4 = var3.getX();
         int var5 = var2.getX();
         if (Math.abs(var4 - var5) <= 2) {
            int var6 = var3.getZ();
            int var7 = var2.getZ();
            if (Math.abs(var6 - var7) <= 2) {
               int var8 = var3.getY();
               int var9 = var2.getY();
               if (Math.abs(var8 - var9) <= 2) {
                  return true;
               }
            }
         }

         AABB var10 = var1.getBoundingBox();
         Vec3 var11 = Vec3.atCenterOf(var2);
         double var12 = var10.distanceToSqr(var11);
         double var14 = 9.0;
         boolean var16;
         if (var12 <= var14) {
            var16 = true;
         } else {
            var16 = false;
         }

         return var16;
      } else {
         return false;
      }
   }

   private boolean a_bool_EndCrystal(EndCrystal var1) {
      if (var1 != null && mc.level != null && mc.player != null) {
         SpeedMine var2;
         if (Night.moduleManager != null) {
            var2 = Night.moduleManager.getModule(SpeedMine.class);
         } else {
            var2 = null;
         }

         SpeedMine var3 = var2;
         if (var3 != null && var3.isEnabled()) {
            SpeedMine.MineTask var4 = var3.v();
            if (var4 != null && var4.a() != null) {
               BlockPos var5 = var4.a();
               if (this.a_bool_EndCrystal_BlockPos(var1, var5)) {
                  if (this.attack.getValue()) {
                     BlockPos var6 = var5;
                     if (this.b_bool_BlockPos(var6)) {
                        return false;
                     }
                  }

                  return true;
               }
            }

            SpeedMine.FadeAnimation var7 = var3.w();
            if (var7 != null && var7.a() != null) {
               BlockPos var8 = var7.a();
               if (this.a_bool_EndCrystal_BlockPos(var1, var8)) {
                  if (this.attack.getValue()) {
                     BlockPos var9 = var8;
                     if (this.b_bool_BlockPos(var9)) {
                        return false;
                     }
                  }

                  return true;
               }
            }

            return false;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private void b_void_EndCrystal(EndCrystal var1) {
      if (var1 != null && var1.isAlive() && !var1.isRemoved()) {
         if (!this.a_bool_EndCrystal(var1)) {
            if (!Night.moduleManager.getModule(Suicide.class).isEnabled()) {
               LocalPlayer var2 = mc.player;
               boolean var3 = this.ignoreTerrain.getValue();
               float var4 = DamageUtil.a_float_Entity_AABB_EndCrystal_bool(var2, null, var1, var3);
               float var5 = this.maximumSelfDamage.getValue().floatValue();
               if (var4 > var5) {
                  return;
               }

               if (this.antiSuicide.getValue()) {
                  float var6 = var4;
                  float var7 = mc.player.getHealth();
                  float var8 = mc.player.getAbsorptionAmount();
                  float var9 = var7 + var8;
                  if (var6 > var9) {
                     return;
                  }
               }
            }

            int var10 = mc.player.getInventory().getSelectedSlot();
            int var11 = -1;
            if (!this.none.getValue().equalsIgnoreCase("None") && mc.player.hasEffect(MobEffects.WEAKNESS)) {
               int var12 = InventoryUtil.c(InventoryUtil.c, InventoryUtil.d);
               if (var12 != -1) {
                  String var13 = this.none.getValue();
                  int var14 = var12;
                  int var15 = var10;
                  InventoryUtil.a_bool_String_int_int(var13, var14, var15);
                  var11 = var12;
               }
            }

            ClientPacketListener var16 = mc.getConnection();
            int var17 = var1.getId();
            var16.send(new ServerboundAttackPacket(var17));
            ClientPacketListener var18 = mc.getConnection();
            Packet var19 = PacketAndRenderUtil.a_Packet_InteractionHand(InteractionHand.MAIN_HAND);
            var18.send(var19);
            if (var11 != -1) {
               String var20 = this.none.getValue();
               int var21 = var11;
               int var22 = var10;
               InventoryUtil.b(var20, var21, var22);
            }

            Map var23 = this.ac;
            Integer var24 = var1.getId();
            Long var25 = System.currentTimeMillis();
            var23.put(var24, var25);
            this.af.reset();
            long var26 = this.am;
            long var28 = 1L;
            long var30 = var26 + var28;
            this.am = var30;
         }
      }
   }

   private void c_void_BlockPos(BlockPos var1) {
      InteractionHand var2;
      if (mc.player.getOffhandItem().getItem() == Items.END_CRYSTAL) {
         var2 = InteractionHand.OFF_HAND;
      } else {
         var2 = InteractionHand.MAIN_HAND;
      }

      InteractionHand var3 = var2;
      CrystalRaytraceHelper.HitCandidate var4 = CrystalRaytraceHelper.a(var1);
      InteractionHand var5 = var3;
      CrystalRaytraceHelper.HitCandidate var6 = var4;
      PacketPredictionUtil.a_void_SequencedPacketCreator(var3x -> {
         Vec3 var5x = var6.b;
         Direction var6x = var6.a;
         BlockHitResult var4x = new BlockHitResult(var5x, var6x, var1, false);
         return new ServerboundUseItemOnPacket(var5, var4x, var3x);
      });
      switch (this.defaultSetting.getValue()) {
         case "Default":
            LocalPlayer var9 = mc.player;
            InteractionHand var10 = var3;
            PacketAndRenderUtil.a(var9, var10);
            break;
         case "Packet":
            ClientPacketListener var11 = mc.getConnection();
            Packet var12 = PacketAndRenderUtil.a_Packet_InteractionHand(var3);
            var11.send(var12);
            break;
         case "Mainhand":
            PacketAndRenderUtil.a(mc.player, InteractionHand.MAIN_HAND);
            break;
         case "Offhand":
            PacketAndRenderUtil.a(mc.player, InteractionHand.OFF_HAND);
            break;
         case "Both":
            PacketAndRenderUtil.a(mc.player, InteractionHand.MAIN_HAND);
            PacketAndRenderUtil.a(mc.player, InteractionHand.OFF_HAND);
      }

      Map var13 = this.ad;
      Long var14 = System.currentTimeMillis();
      var13.put(var1, var14);
      Map var15 = this.ae;
      Long var16 = System.currentTimeMillis();
      var15.put(var1, var16);
      this.ag.reset();
      long var17 = this.al;
      long var19 = 1L;
      long var21 = var17 + var19;
      this.al = var21;
   }

   private List<Player> n() {
      ArrayList var1 = new ArrayList();
      if (Night.moduleManager.getModule(Suicide.class).isEnabled()) {
         ArrayList var2 = var1;
         LocalPlayer var3 = mc.player;
         var2.add(var3);
         return var1;
      }

      for (Player var5 : (Iterable<Player>)(Object)mc.level.players()) {
         LocalPlayer var6 = mc.player;
         if (var5 != var6 && !EntityUtil.c_bool_Entity(var5) && var5.isAlive()) {
            LocalPlayer var7 = mc.player;
            Player var8 = var5;
            double var9 = var7.distanceToSqr(var8);
            double var11 = Mth.square(this.x.getValue().doubleValue());
            if (!(var9 > var11)) {
               String var13 = var5.getName().getString();
               if (!Night.friendManager.isFriend(var13)) {
                  boolean var14;
                  if (var5.getMainHandItem().getItem() != Items.END_CRYSTAL && var5.getOffhandItem().getItem() != Items.END_CRYSTAL) {
                     var14 = false;
                  } else {
                     var14 = true;
                  }

                  boolean var15 = var14;
                  if (!this.y.getValue() || !EntityUtil.b_bool_Player(var5) || var15) {
                     ArrayList var16 = var1;
                     Player var17 = var5;
                     var16.add(var17);
                  }
               }
            }
         }
      }

      return var1;
   }

   private boolean o() {
      if (!this.pauseOnPearl.getValue()) {
         return false;
      }

      Phase var1;
      if (Night.moduleManager != null) {
         var1 = Night.moduleManager.getModule(Phase.class);
      } else {
         var1 = null;
      }

      Phase var2 = var1;
      if (var2 != null && var2.isEnabled()) {
         return true;
      }

      KeyAction var3;
      if (Night.moduleManager != null) {
         var3 = Night.moduleManager.getModule(KeyAction.class);
      } else {
         var3 = null;
      }

      KeyAction var4 = var3;
      if (var4 != null && var4.w()) {
         return true;
      }

      long var5 = System.currentTimeMillis();
      long var7 = this.aE;
      long var9 = var5 - var7;
      long var11 = 250L;
      if (var9 < var11) {
         return true;
      }

      if (mc.player != null) {
         boolean var13;
         if (mc.player.getMainHandItem().getItem() != Items.ENDER_PEARL && mc.player.getOffhandItem().getItem() != Items.ENDER_PEARL) {
            var13 = false;
         } else {
            var13 = true;
         }

         if (var13 && (mc.options.keyUse.isDown() || mc.player.isUsingItem())) {
            return true;
         }
      }

      return false;
   }

   private boolean p() {
      if (!this.pauseOnXP.getValue()) {
         return false;
      }

      KeyAction var1;
      if (Night.moduleManager != null) {
         var1 = Night.moduleManager.getModule(KeyAction.class);
      } else {
         var1 = null;
      }

      KeyAction var2 = var1;
      if (var2 != null && var2.v()) {
         return true;
      }

      long var3 = System.currentTimeMillis();
      long var5 = this.aF;
      long var7 = var3 - var5;
      long var9 = 250L;
      if (var7 < var9) {
         return true;
      }

      if (mc.player != null) {
         boolean var11;
         if (mc.player.getMainHandItem().getItem() != Items.EXPERIENCE_BOTTLE && mc.player.getOffhandItem().getItem() != Items.EXPERIENCE_BOTTLE) {
            var11 = false;
         } else {
            var11 = true;
         }

         if (var11 && (mc.options.keyUse.isDown() || mc.player.isUsingItem())) {
            return true;
         }
      }

      return false;
   }

   private boolean b_bool_String(String var1) {
      if (this.o()) {
         return true;
      }

      if (this.p()) {
         return true;
      }

      boolean var2;
      if (!this.both.getValue().equalsIgnoreCase("None")
         && (!var1.equalsIgnoreCase("Attack") || !this.both.getValue().equalsIgnoreCase("Place"))
         && (!var1.equalsIgnoreCase("Place") || !this.both.getValue().equalsIgnoreCase("Attack"))) {
         var2 = false;
      } else {
         var2 = true;
      }

      boolean var3;
      if (var2 && mc.player.isUsingItem()) {
         var3 = true;
      } else {
         var3 = false;
      }

      return var3;
   }

   private float a_float_Player_float(Player var1, float var2) {
      if (var1 == null) {
         return var2;
      }

      if (this.z.getValue()) {
         ClientLevel var3 = mc.level;
         Entity var4 = (Entity)null;
         BlockPos var6 = var1.blockPosition();
         AABB var5 = new AABB(var6);
         double var7 = 1.0;
         AABB var9 = var5.inflate(var7);
         if (var3.getEntities(var4, var9, var0 -> true).stream().anyMatch(var1x -> {
            if (var1x instanceof ItemEntity var2x && var2x.getItem().getItem() == Items.OBSIDIAN && var2x.getItem().getCount() >= 8) {
               int var3x = var2x.tickCount;
               int var4x = Night.serverManager.getPingTicks();
               int var5x = 2 + var4x;
               int var6x = this.placeSpeed.getValue().intValue();
               int var7x = 20 - var6x;
               int var8 = var5x + var7x;
               if (var3x <= var8) {
                  return true;
               }
            }

            return false;
         })) {
            ClientLevel var10 = mc.level;
            Entity var11 = (Entity)null;
            BlockPos var13 = mc.player.blockPosition();
            AABB var12 = new AABB(var13);
            double var14 = 1.0;
            AABB var16 = var12.inflate(var14);
            if (!var10.getEntities(var11, var16, var0 -> true).stream().anyMatch(var1x -> {
               if (var1x instanceof ItemEntity var2x && var2x.getItem().getItem() == Items.OBSIDIAN && var2x.getItem().getCount() >= 8) {
                  int var3x = var2x.tickCount;
                  int var4x = Night.serverManager.getPingTicks();
                  int var5x = 2 + var4x;
                  int var6x = this.placeSpeed.getValue().intValue();
                  int var7x = 20 - var6x;
                  int var8 = var5x + var7x;
                  if (var3x <= var8) {
                     return true;
                  }
               }

               return false;
            })) {
               return 2.0F;
            }
         }
      }

      if (this.dynamic.getValue().equalsIgnoreCase("None")) {
         return var2;
      }

      if (!this.normal3.getValue().equalsIgnoreCase("Normal")) {
         TimerUtil var17 = this.ah;
         long var18 = this.faceplaceDelay.getValue().longValue();
         long var20 = 50L;
         Long var22 = var18 * var20;
         if (!var17.passedMs(var22)) {
            return var2;
         }
      }

      if (this.dynamic.getValue().equalsIgnoreCase("Always")) {
         return Math.min(var2, 2.0F);
      }

      if (this.dynamic.getValue().equalsIgnoreCase("Dynamic") && this.healthPlace.getValue()) {
         float var23 = var1.getHealth();
         float var24 = var1.getAbsorptionAmount();
         float var25 = var23 + var24;
         float var26 = this.health.getValue().floatValue();
         if (var25 <= var26) {
            return Math.min(var2, 2.0F);
         }
      }

      if (this.dynamic.getValue().equalsIgnoreCase("Dynamic") && this.armorPlace.getValue()) {
         EquipmentSlot[] var27 = new EquipmentSlot[]{EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD};
         EquipmentSlot[] var28 = var27;
         int var29 = var28.length;
         int var30 = 0;

         while (true) {
            int var31 = var30;
            int var32 = var29;
            if (var31 >= var32) {
               break;
            }

            EquipmentSlot[] var33 = var28;
            int var34 = var30;
            EquipmentSlot var35 = var33[var34];
            ItemStack var36 = var1.getItemBySlot(var35);
            if (!var36.isEmpty() && var36.get(DataComponents.EQUIPPABLE) != null) {
               int var37 = var36.getMaxDamage();
               int var38 = var36.getDamageValue();
               double var39 = var37 - var38;
               double var41 = 100.0;
               double var43 = var39 * var41;
               double var45 = var36.getMaxDamage();
               long var47 = Math.round(var43 / var45);
               long var49 = this.percentage.getValue().intValue();
               if (var47 <= var49) {
                  return Math.min(var2, 2.0F);
               }
            }

            var30++;
         }
      }

      return var2;
   }

   @Generated
   public Player h() {
      return this.ar;
   }

   @Generated
   public String i() {
      return this.ax;
   }

   public static class PlaceTarget {
      private BlockPos a;
      private Player b;
      List<Entity> c;
      private BlockPos d;
      private float e;
      private int f;

      public ProxyAutoCrystal.PlaceTarget a() {
         BlockPos var1 = this.a;
         Player var2 = this.b;
         List var3 = this.c;
         BlockPos var4 = this.d;
         float var5 = this.e;
         int var6 = this.f;
         return new ProxyAutoCrystal.PlaceTarget(var1, var2, var3, var4, var5, var6);
      }

      @Generated
      public BlockPos b() {
         return this.a;
      }

      @Generated
      public Player c() {
         return this.b;
      }

      @Generated
      public List<Entity> d() {
         return this.c;
      }

      @Generated
      public BlockPos e() {
         return this.d;
      }

      @Generated
      public float f() {
         return this.e;
      }

      @Generated
      public int g() {
         return this.f;
      }

      @Generated
      public PlaceTarget(BlockPos var1, Player var2, List<Entity> var3, BlockPos var4, float var5, int var6) {
         this.a = var1;
         this.b = var2;
         this.c = var3;
         this.d = var4;
         this.e = var5;
         this.f = var6;
      }
   }
}
