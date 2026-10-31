package night.module.impl.combat;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import lombok.Generated;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.Direction.Plane;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import night.Night;
import night.events.EventTarget;
import night.events.impl.BlockPlaceEvent;
import night.events.impl.ClientConnectEvent;
import night.events.impl.EntitySpawnEvent;
import night.events.impl.ItemUseEvent;
import night.events.impl.MotionSyncEvent;
import night.events.impl.PacketReceiveEvent;
import night.events.impl.PacketSendEvent;
import night.events.impl.PlayerDeathEvent;
import night.events.impl.UpdateWalkingPlayerEvent;
import night.events.impl.MotionSyncEvent.Post;
import night.manager.ChatManager;
import night.manager.RotationManager;
import night.module.Module;
import night.module.ModuleInfo;
import night.module.Module.Category;
import night.module.impl.combat.CrystalRaytraceHelper.HitCandidate;
import night.module.impl.kingmc.AutoShop;
import night.module.impl.movement.Phase;
import night.module.impl.player.KeyAction;
import night.module.impl.player.SpeedMine;
import night.module.impl.player.SpeedMine.FadeAnimation;
import night.module.impl.player.SpeedMine.MineTask;
import night.pingbypass.PingBypassState;
import night.security.LicenseService;
import night.setting.impl.BooleanSetting;
import night.setting.impl.CategorySetting;
import night.setting.impl.ColorSetting;
import night.setting.impl.ModeSetting;
import night.setting.impl.NumberSetting;
import night.setting.impl.BooleanSetting.BooleanCondition;
import night.setting.impl.CategorySetting.CategoryCondition;
import night.setting.impl.ColorSetting.ColorValue;
import night.setting.impl.ModeSetting.ModeCondition;
import night.util.color.ColorUtil;
import night.util.inventory.InventoryUtil;
import night.util.math.RollingCounter;
import night.util.math.RotationJitterUtil;
import night.util.math.TimerUtil;
import night.util.network.PacketPredictionUtil;
import night.util.render.EspShaderRenderer;
import night.util.rotation.RotationMathUtil;
import night.util.world.BlockInteractionUtil;
import night.util.world.DamageUtil;
import night.util.world.EntityUtil;
import night.util.world.PacketAndRenderUtil;
import night.util.world.PositionUtil;

@ModuleInfo(name = "AutoCrystal", description = "Automatically places and attacks crystals to annihilate your opponents.", category = Category.COMBAT)
public class AutoCrystal extends Module {
   public CategorySetting attackCategory = new CategorySetting("Attack", "The category for settings related to attacking crystals.");
   public BooleanSetting attack;
   public NumberSetting attackSpeed;
   public NumberSetting attackRange;
   public NumberSetting attackWallsRange;
   public ModeSetting antiWeakness;
   public BooleanSetting instant;
   public BooleanSetting inhibit;
   public CategorySetting placeCategory;
   public BooleanSetting place;
   public NumberSetting placeSpeed;
   public NumberSetting placeRange;
   public NumberSetting placeWallsRange;
   public ModeSetting placements;
   public BooleanSetting blockDestruction;
   public ModeSetting switchSetting;
   public BooleanSetting swapBack;
   public NumberSetting swapDelay;
   public NumberSetting multiPlace;
   public CategorySetting x;
   public BooleanSetting y;
   public NumberSetting z;
   private final RotationJitterUtil bd;
   public ModeSetting sequential;
   public ModeSetting rotate;
   public ModeSetting swing;
   public BooleanSetting yawStep;
   public NumberSetting yawStepThreshold;
   public BooleanSetting raytrace;
   public BooleanSetting losDebug;
   public ModeSetting target;
   public NumberSetting extrapolation;
   public NumberSetting enemyRange;
   public BooleanSetting ignoreNaked;
   public BooleanSetting chestBreak;
   public BooleanSetting mineIgnore;
   public NumberSetting mineIgnoreTicks;
   public BooleanSetting asynchronous;
   public BooleanSetting pauseOnSecondaryMine;
   public BooleanSetting awaitMine;
   public NumberSetting awaitMineTicks;
   public BooleanSetting pauseOnPearl;
   public BooleanSetting pauseOnXP;
   public BooleanSetting gameLoop;
   public NumberSetting loopDelay;
   public ModeSetting whileEating;
   public CategorySetting predictionCategory;
   public BooleanSetting godSync;
   public NumberSetting predictions;
   public NumberSetting offset;
   public ModeSetting godSwing;
   public BooleanSetting fast;
   public BooleanSetting antiKick;
   public NumberSetting kickThreshold;
   public CategorySetting faceplaceCategory;
   public ModeSetting faceplaceMode;
   public ModeSetting faceplaceSpeed;
   public NumberSetting faceplaceDelay;
   public BooleanSetting healthPlace;
   public NumberSetting health;
   public BooleanSetting armorPlace;
   public NumberSetting percentage;
   public CategorySetting basePlaceCategory;
   public BooleanSetting basePlace;
   public ModeSetting baseSwitch;
   public BooleanSetting baseAirPlace;
   public BooleanSetting baseStrictDirection;
   public NumberSetting baseDamage;
   public NumberSetting baseRatio;
   public NumberSetting baseRadius;
   public NumberSetting baseRange;
   public NumberSetting baseDelay;
   public BooleanSetting baseRotate;
   public CategorySetting damageCategory;
   public NumberSetting minimumDamage;
   public NumberSetting maximumSelfDamage;
   public NumberSetting lethalMultiplier;
   public BooleanSetting antiSuicide;
   public BooleanSetting ignoreTerrain;
   public CategorySetting renderCategory;
   public ModeSetting animation2;
   public ModeSetting mode;
   public NumberSetting duration;
   public NumberSetting smoothness;
   public ModeSetting renderMode;
   public ColorSetting fillColorUp;
   public ColorSetting fillColorDown;
   public ColorSetting outlineColorUp;
   public ColorSetting outlineColorDown;
   public BooleanSetting renderDamage;
   public BooleanSetting icon;
   public NumberSetting iconScale;
   public NumberSetting iconRadius;
   public ColorSetting iconColor;
   private static final String[] be = EspShaderRenderer.a;
   private static final String[] bf;
   public ModeSetting shader;
   public NumberSetting shaderSpeed;
   public NumberSetting shaderOpacity;
   public BooleanSetting shaderDistanceScaling;
   public NumberSetting shaderStep;
   public ColorSetting shaderColor1;
   public ColorSetting shaderColor2;
   public ColorSetting shaderColor3;
   public ColorSetting shaderColor4;
   public ColorSetting shaderGlowColor;
   private final ExecutorService bi;
   private volatile Future<?> bj;
   private volatile boolean bk;
   private volatile Set<BlockPos> bl;
   private Runnable bm;
   private Runnable bn;
   private final ScheduledExecutorService bo;
   private final Queue<Long> bp;
   private long bq;
   private long br;
   private final Map<Integer, Long> bs;
   private final Map<BlockPos, Long> bt;
   private final Map<BlockPos, Long> bu;
   private final TimerUtil bv;
   private final TimerUtil bw;
   private final TimerUtil bx;
   private final TimerUtil by;
   private final TimerUtil bz;
   private final TimerUtil bA;
   private int bB;
   private long bC;
   private long bD;
   private boolean bE;
   private boolean bF;
   private boolean bG;
   private boolean bH;
   private Player bI;
   private EndCrystal bJ;
   private AutoCrystal.PlaceTarget bK;
   private AutoCrystal.PlaceTarget bL;
   private AutoCrystal.PlaceTarget bM;
   private BlockPos bN;
   private BlockPos bO;
   private long bP;
   private boolean bQ;
   private String bR;
   private int bS;
   private String bT;
   private final RollingCounter bU;
   private int bV;
   private int bW;
   private int bX;
   private volatile List<Player> bY;
   private volatile long bZ;
   private static final long ca = 50L;
   private volatile String cb;
   private volatile long cc;
   private static final long cd = 5000L;
   private int ce;
   private int cf;
   private static final float cg = 0.85F;
   private long ch;
   private long ci;
   private volatile List<BlockPos> cj;

   public AutoCrystal() {
      CategorySetting var1 = this.attackCategory;
      this.attack = new BooleanSetting("Attack", "Enabled", "Automatically attacks crystals that are deemed safe.", new CategoryCondition(var1), true);
      CategorySetting var3 = this.attackCategory;
      CategoryCondition var2 = new CategoryCondition(var3);
      Float var4 = 20.0F;
      Float var5 = 0.1F;
      Float var6 = 20.0F;
      this.attackSpeed = new NumberSetting("AttackSpeed", "Speed", "The speed at which crystals will be attacked.", var2, var4, var5, var6);
      CategorySetting var8 = this.attackCategory;
      CategoryCondition var7 = new CategoryCondition(var8);
      Double var9 = 4.5;
      Double var10 = 0.0;
      Double var11 = 8.0;
      this.attackRange = new NumberSetting("AttackRange", "Range", "The maximum distance at which crystals will be attacked.", var7, var9, var10, var11);
      CategorySetting var13 = this.attackCategory;
      CategoryCondition var12 = new CategoryCondition(var13);
      Double var14 = 4.5;
      Double var15 = 0.0;
      Double var16 = 8.0;
      this.attackWallsRange = new NumberSetting(
         "AttackWallsRange", "WallsRange", "The maximum distance at which crystals will be attacked through walls.", var12, var14, var15, var16
      );
      CategorySetting var18 = this.attackCategory;
      CategoryCondition var17 = new CategoryCondition(var18);
      String[] var19 = new String[]{"None", "Normal", "Silent"};
      this.antiWeakness = new ModeSetting("AntiWeakness", "Allows you to attack crystals when weaknessed.", var17, "None", var19);
      CategorySetting var21 = this.attackCategory;
      CategoryCondition var20 = new CategoryCondition(var21);
      this.instant = new BooleanSetting("Instant", "Instantly attacks crystals once they spawn.", var20, true);
      CategorySetting var23 = this.attackCategory;
      CategoryCondition var22 = new CategoryCondition(var23);
      this.inhibit = new BooleanSetting("Inhibit", "Prevents excessive attacks on crystals by blacklisting crystals when attacking them.", var22, true);
      this.placeCategory = new CategorySetting("Place", "The category for settings related to placing crystals.");
      CategorySetting var25 = this.placeCategory;
      CategoryCondition var24 = new CategoryCondition(var25);
      this.place = new BooleanSetting("Place", "Enabled", "Automatically places crystals on positions that are deemed safe and lethal enough.", var24, true);
      CategorySetting var27 = this.placeCategory;
      CategoryCondition var26 = new CategoryCondition(var27);
      Float var28 = 10.0F;
      Float var29 = 0.1F;
      Float var30 = 20.0F;
      this.placeSpeed = new NumberSetting("PlaceSpeed", "Speed", "The speed at which crystals will be placed.", var26, var28, var29, var30);
      CategorySetting var32 = this.placeCategory;
      CategoryCondition var31 = new CategoryCondition(var32);
      Double var33 = 4.5;
      Double var34 = 0.0;
      Double var35 = 8.0;
      this.placeRange = new NumberSetting("PlaceRange", "Range", "The maximum distance at which positions will be placed on.", var31, var33, var34, var35);
      CategorySetting var37 = this.placeCategory;
      CategoryCondition var36 = new CategoryCondition(var37);
      Double var38 = 4.5;
      Double var39 = 0.0;
      Double var40 = 8.0;
      this.placeWallsRange = new NumberSetting(
         "PlaceWallsRange", "WallsRange", "The maximum distance at which positions will be placed on through walls.", var36, var38, var39, var40
      );
      CategorySetting var42 = this.placeCategory;
      CategoryCondition var41 = new CategoryCondition(var42);
      String[] var43 = new String[]{"Native", "Protocol"};
      this.placements = new ModeSetting("Placements", "The version of the game that will be used for crystal placement calculations.", var41, "Native", var43);
      CategorySetting var45 = this.placeCategory;
      CategoryCondition var44 = new CategoryCondition(var45);
      this.blockDestruction = new BooleanSetting("BlockDestruction", "Places crystals on top of mined blocks in order to damage opponents.", var44, true);
      CategorySetting var47 = this.placeCategory;
      CategoryCondition var46 = new CategoryCondition(var47);
      String[] var48 = new String[]{"None", "Normal", "Silent", "AltSwap"};
      this.switchSetting = new ModeSetting("Switch", "Automatically switches to a crystal if you aren't currently holding one.", var46, "None", var48);
      ModeSetting var50 = this.switchSetting;
      String[] var51 = new String[]{"Normal"};
      ModeCondition var49 = new ModeCondition(var50, var51);
      this.swapBack = new BooleanSetting(
         "SwapBack", "Switches back to the item you were holding before the module started switching to crystals, once the module is disabled.", var49, false
      );
      CategorySetting var53 = this.placeCategory;
      CategoryCondition var52 = new CategoryCondition(var53);
      Integer var54 = 0;
      Integer var55 = 0;
      Integer var56 = 20;
      this.swapDelay = new NumberSetting(
         "SwapDelay", "Delay", "The delay in ticks after swapping before placing or attacking crystals.", var52, var54, var55, var56
      );
      CategorySetting var58 = this.placeCategory;
      CategoryCondition var57 = new CategoryCondition(var58);
      Integer var59 = 1;
      Integer var60 = 1;
      Integer var61 = 5;
      this.multiPlace = new NumberSetting("MultiPlace", "How many crystals are placed at once.", var57, var59, var60, var61);
      this.x = new CategorySetting("Miscellaneous", "The category for all miscellaneous settings.");
      CategorySetting var63 = this.x;
      CategoryCondition var62 = new CategoryCondition(var63);
      this.y = new BooleanSetting(
         "LegitJitler",
         "Humanized crystal clicks: random delay/pacing, a different aim point inside the crystal/obsidian face every click, never-repeated rotations, pitch moving between place and break, occasional misses. Server-side only with Rotate Packet/Silent.",
         var62,
         false
      );
      BooleanSetting var65 = this.y;
      BooleanCondition var64 = new BooleanCondition(var65, true);
      Integer var66 = 4;
      Integer var67 = 0;
      Integer var68 = 25;
      this.z = new NumberSetting("FailChance", "Fail", "LegitJitler: percent of clicks that miss (swing only, no place/attack).", var64, var66, var67, var68);
      this.bd = new RotationJitterUtil();
      CategorySetting var70 = this.x;
      CategoryCondition var69 = new CategoryCondition(var70);
      String[] var71 = new String[]{"None", "Strict", "Strong"};
      this.sequential = new ModeSetting("Sequential", "The sequence that the module's processes will be run in.", var69, "Strong", var71);
      CategorySetting var73 = this.x;
      CategoryCondition var72 = new CategoryCondition(var73);
      String[] var74 = new String[]{"None", "Normal", "Packet", "Silent"};
      this.rotate = new ModeSetting("Rotate", "Automatically rotates to the crystal whenever attacking or placing.", var72, "Normal", var74);
      CategorySetting var76 = this.x;
      CategoryCondition var75 = new CategoryCondition(var76);
      String[] var77 = new String[]{"Default", "None", "Packet", "Mainhand", "Offhand", "Both"};
      this.swing = new ModeSetting("Swing", "The hand that will be used for swinging.", var75, "Default", var77);
      CategorySetting var79 = this.x;
      CategoryCondition var78 = new CategoryCondition(var79);
      this.yawStep = new BooleanSetting("YawStep", "Performs your rotations over multiple ticks.", var78, false);
      BooleanSetting var81 = this.yawStep;
      BooleanCondition var80 = new BooleanCondition(var81, true);
      Integer var82 = 75;
      Integer var83 = 1;
      Integer var84 = 180;
      this.yawStepThreshold = new NumberSetting("YawStepThreshold", "Threshold", "The threshold in order for yaw to be modified.", var80, var82, var83, var84);
      CategorySetting var86 = this.x;
      CategoryCondition var85 = new CategoryCondition(var86);
      this.raytrace = new BooleanSetting("Raytrace", "Avoids attacking or placing any crystals through walls.", var85, false);
      CategorySetting var88 = this.x;
      CategoryCondition var87 = new CategoryCondition(var88);
      this.losDebug = new BooleanSetting(
         "LosDebug", "Debug", "Prints once a second why the block you are aiming at passes/fails the place gates (LOS steps + damage).", var87, false
      );
      CategorySetting var90 = this.x;
      CategoryCondition var89 = new CategoryCondition(var90);
      String[] var91 = new String[]{"All", "Nearest", "Farthest", "Health"};
      this.target = new ModeSetting(
         "Target",
         "Which player to target when multiple are in range -- narrowing this to one collapses the per-position player scan from O(players) to O(1), the actual fix for multitarget FPS drops.",
         var89,
         "All",
         var91
      );
      CategorySetting var93 = this.x;
      CategoryCondition var92 = new CategoryCondition(var93);
      Integer var94 = 0;
      Integer var95 = 0;
      Integer var96 = 20;
      this.extrapolation = new NumberSetting(
         "Extrapolation", "Extrapolates the target's position to calculate positions ahead of time.", var92, var94, var95, var96
      );
      CategorySetting var98 = this.x;
      CategoryCondition var97 = new CategoryCondition(var98);
      Double var99 = 10.0;
      Double var100 = 0.0;
      Double var101 = 24.0;
      this.enemyRange = new NumberSetting("EnemyRange", "The maximum distance at which enemies can be at.", var97, var99, var100, var101);
      CategorySetting var103 = this.x;
      CategoryCondition var102 = new CategoryCondition(var103);
      this.ignoreNaked = new BooleanSetting(
         "IgnoreNaked", "Ignores naked players (even if wearing only elytra), but still targets them if they are holding an End Crystal.", var102, false
      );
      CategorySetting var105 = this.x;
      CategoryCondition var104 = new CategoryCondition(var105);
      this.chestBreak = new BooleanSetting(
         "ChestBreak", "Prevents other players from getting obsidian from ender chests by destroying the dropped items.", var104, false
      );
      CategorySetting var107 = this.x;
      CategoryCondition var106 = new CategoryCondition(var107);
      this.mineIgnore = new BooleanSetting(
         "MineIgnore", "Pre-places a crystal on the block SpeedMine is about to break, and detonates it the instant that block is gone.", var106, false
      );
      BooleanSetting var109 = this.mineIgnore;
      BooleanCondition var108 = new BooleanCondition(var109, true);
      Integer var110 = 3;
      Integer var111 = 0;
      Integer var112 = 10;
      this.mineIgnoreTicks = new NumberSetting(
         "MineIgnoreTicks", "Tick", "How many ticks before the block breaks to place the crystal.", var108, var110, var111, var112
      );
      CategorySetting var114 = this.x;
      CategoryCondition var113 = new CategoryCondition(var114);
      this.asynchronous = new BooleanSetting("Asynchronous", "Performs calculations on separate threads.", var113, true);
      CategorySetting var116 = this.x;
      CategoryCondition var115 = new CategoryCondition(var116);
      this.pauseOnSecondaryMine = new BooleanSetting(
         "PauseOnSecondaryMine", "Pauses AutoCrystal for one tick while SpeedMine's secondary block is about to break, so it finishes first.", var115, true
      );
      CategorySetting var118 = this.x;
      CategoryCondition var117 = new CategoryCondition(var118);
      this.awaitMine = new BooleanSetting(
         "AwaitMine", "Pauses AutoCrystal when AutoMine selects a target block so AutoMine can lock and mine properly.", var117, false
      );
      BooleanSetting var120 = this.awaitMine;
      BooleanCondition var119 = new BooleanCondition(var120, true);
      Integer var121 = 2;
      Integer var122 = 1;
      Integer var123 = 10;
      this.awaitMineTicks = new NumberSetting(
         "AwaitMineTicks", "Ticks", "The number of ticks to wait for AutoMine to lock target.", var119, var121, var122, var123
      );
      CategorySetting var125 = this.x;
      CategoryCondition var124 = new CategoryCondition(var125);
      this.pauseOnPearl = new BooleanSetting(
         "PauseOnPearl", "Pauses AutoCrystal when Phase is active, KeyAction pearl is used, or when throwing an ender pearl manually.", var124, true
      );
      CategorySetting var127 = this.x;
      CategoryCondition var126 = new CategoryCondition(var127);
      this.pauseOnXP = new BooleanSetting(
         "PauseOnXP", "Pauses AutoCrystal when throwing experience bottles manually or when KeyAction XP is active.", var126, true
      );
      CategorySetting var129 = this.x;
      CategoryCondition var128 = new CategoryCondition(var129);
      this.gameLoop = new BooleanSetting("GameLoop", "Runs the module on loop instead of ticks.", var128, false);
      BooleanSetting var131 = this.gameLoop;
      BooleanCondition var130 = new BooleanCondition(var131, true);
      Integer var132 = 50;
      Integer var133 = 0;
      Integer var134 = 1000;
      this.loopDelay = new NumberSetting("LoopDelay", "The delay that has to be waited out before running the module again.", var130, var132, var133, var134);
      CategorySetting var136 = this.x;
      CategoryCondition var135 = new CategoryCondition(var136);
      String[] var137 = new String[]{"None", "Attack", "Place", "Both"};
      this.whileEating = new ModeSetting("WhileEating", "Places and attacks crystal while eating or using items.", var135, "Both", var137);
      this.predictionCategory = new CategorySetting("Prediction", "The category for settings related to attack prediction.");
      CategorySetting var139 = this.predictionCategory;
      CategoryCondition var138 = new CategoryCondition(var139);
      this.godSync = new BooleanSetting("GodSync", "Makes the attacking way faster by predicting entity IDs.", var138, false);
      CategorySetting var141 = this.predictionCategory;
      CategoryCondition var140 = new CategoryCondition(var141);
      Integer var142 = 10;
      Integer var143 = 1;
      Integer var144 = 20;
      this.predictions = new NumberSetting("Predictions", "The amount of predictions that will be done after placing.", var140, var142, var143, var144);
      CategorySetting var146 = this.predictionCategory;
      CategoryCondition var145 = new CategoryCondition(var146);
      Integer var147 = 0;
      Integer var148 = 0;
      Integer var149 = 2;
      this.offset = new NumberSetting("Offset", "The amount that the last entity ID should be offset by.", var145, var147, var148, var149);
      CategorySetting var151 = this.predictionCategory;
      CategoryCondition var150 = new CategoryCondition(var151);
      String[] var152 = new String[]{"None", "Normal", "Strict"};
      this.godSwing = new ModeSetting("GodSwing", "Swing", "The swinging that will be done for each predicted attack.", var150, "Normal", var152);
      CategorySetting var154 = this.predictionCategory;
      CategoryCondition var153 = new CategoryCondition(var154);
      this.fast = new BooleanSetting("Fast", "Improves the speed of the prediction calculations at the cost of stability.", var153, false);
      CategorySetting var156 = this.predictionCategory;
      CategoryCondition var155 = new CategoryCondition(var156);
      this.antiKick = new BooleanSetting("AntiKick", "Prevents you from getting kicked by attacking invalid entity IDs.", var155, false);
      BooleanSetting var158 = this.antiKick;
      BooleanCondition var157 = new BooleanCondition(var158, true);
      Integer var159 = 5;
      Integer var160 = 1;
      Integer var161 = 10;
      this.kickThreshold = new NumberSetting("KickThreshold", "Threshold", "The tick threshold for the kick prevention.", var157, var159, var160, var161);
      this.faceplaceCategory = new CategorySetting("Faceplace", "The category for settings relating to faceplacing.");
      CategorySetting var163 = this.faceplaceCategory;
      CategoryCondition var162 = new CategoryCondition(var163);
      String[] var164 = new String[]{"None", "Dynamic", "Always"};
      this.faceplaceMode = new ModeSetting("FaceplaceMode", "Mode", "The checks that will be done in order to faceplace.", var162, "Dynamic", var164);
      CategorySetting var166 = this.faceplaceCategory;
      CategoryCondition var165 = new CategoryCondition(var166);
      String[] var167 = new String[]{"Normal", "Custom"};
      this.faceplaceSpeed = new ModeSetting("FaceplaceSpeed", "Speed", "The speed that players will be faceplaced at.", var165, "Normal", var167);
      ModeSetting var169 = this.faceplaceSpeed;
      String[] var170 = new String[]{"Custom"};
      ModeCondition var168 = new ModeCondition(var169, var170);
      Integer var171 = 11;
      Integer var172 = 0;
      Integer var173 = 20;
      this.faceplaceDelay = new NumberSetting(
         "FaceplaceDelay", "Delay", "The ticks that have to be waited for before faceplacing again.", var168, var171, var172, var173
      );
      ModeSetting var175 = this.faceplaceMode;
      String[] var176 = new String[]{"Dynamic"};
      ModeCondition var174 = new ModeCondition(var175, var176);
      this.healthPlace = new BooleanSetting("HealthPlace", "Whether or not to faceplace when the target's health is low.", var174, true);
      BooleanSetting var178 = this.healthPlace;
      BooleanCondition var177 = new BooleanCondition(var178, true);
      Float var179 = 8.0F;
      Float var180 = 0.0F;
      Float var181 = 36.0F;
      this.health = new NumberSetting(
         "Health", "The health that the target needs to be at in order for the module to start faceplacing.", var177, var179, var180, var181
      );
      ModeSetting var183 = this.faceplaceMode;
      String[] var184 = new String[]{"Dynamic"};
      ModeCondition var182 = new ModeCondition(var183, var184);
      this.armorPlace = new BooleanSetting("ArmorPlace", "Whether or not to faceplace when the target's armor is low on durability.", var182, true);
      BooleanSetting var186 = this.armorPlace;
      BooleanCondition var185 = new BooleanCondition(var186, true);
      Integer var187 = 10;
      Integer var188 = 1;
      Integer var189 = 100;
      this.percentage = new NumberSetting(
         "Percentage", "The percentage that one of the target's armor pieces need to be at in order to start faceplacing.", var185, var187, var188, var189
      );
      this.basePlaceCategory = new CategorySetting(
         "BasePlace", "The category for settings related to placing base obsidian blocks for elevated crystal attacks."
      );
      CategorySetting var191 = this.basePlaceCategory;
      CategoryCondition var190 = new CategoryCondition(var191);
      this.basePlace = new BooleanSetting("BasePlace", "Enabled", "Automatically places obsidian base blocks under/next to targets.", var190, true);
      CategorySetting var193 = this.basePlaceCategory;
      CategoryCondition var192 = new CategoryCondition(var193);
      String[] var194 = new String[]{"None", "Normal", "Silent", "AltSwap"};
      this.baseSwitch = new ModeSetting("BaseSwitch", "Switch", "Switch mode for placing base blocks.", var192, "Silent", var194);
      CategorySetting var196 = this.basePlaceCategory;
      CategoryCondition var195 = new CategoryCondition(var196);
      this.baseAirPlace = new BooleanSetting("BaseAirPlace", "AirPlace", "Allows placing base blocks in the air without neighboring blocks.", var195, false);
      CategorySetting var198 = this.basePlaceCategory;
      CategoryCondition var197 = new CategoryCondition(var198);
      this.baseStrictDirection = new BooleanSetting("BaseStrictDirection", "StrictDirection", "Only places using directions that face you.", var197, false);
      CategorySetting var200 = this.basePlaceCategory;
      CategoryCondition var199 = new CategoryCondition(var200);
      Double var201 = 6.0;
      Double var202 = 0.0;
      Double var203 = 36.0;
      this.baseDamage = new NumberSetting(
         "BaseDamage", "Min Damage", "The minimum damage a crystal on the base must deal to the target.", var199, var201, var202, var203
      );
      CategorySetting var205 = this.basePlaceCategory;
      CategoryCondition var204 = new CategoryCondition(var205);
      Double var206 = 1.5;
      Double var207 = 1.0;
      Double var208 = 3.0;
      this.baseRatio = new NumberSetting(
         "BaseRatio", "Ratio", "Damage ratio required over existing standard crystal placement to trigger baseplace.", var204, var206, var207, var208
      );
      CategorySetting var210 = this.basePlaceCategory;
      CategoryCondition var209 = new CategoryCondition(var210);
      Double var211 = 3.0;
      Double var212 = 1.0;
      Double var213 = 6.0;
      this.baseRadius = new NumberSetting(
         "BaseRadius", "Scan Radius", "The horizontal radius around the target to scan for base placements.", var209, var211, var212, var213
      );
      CategorySetting var215 = this.basePlaceCategory;
      CategoryCondition var214 = new CategoryCondition(var215);
      Double var216 = 4.5;
      Double var217 = 0.0;
      Double var218 = 6.0;
      this.baseRange = new NumberSetting("BaseRange", "Range", "The maximum distance at which base blocks will be placed.", var214, var216, var217, var218);
      CategorySetting var220 = this.basePlaceCategory;
      CategoryCondition var219 = new CategoryCondition(var220);
      Integer var221 = 0;
      Integer var222 = 0;
      Integer var223 = 10;
      this.baseDelay = new NumberSetting("BaseDelay", "Delay", "Delay in ticks between placing base blocks.", var219, var221, var222, var223);
      CategorySetting var225 = this.basePlaceCategory;
      CategoryCondition var224 = new CategoryCondition(var225);
      this.baseRotate = new BooleanSetting("BaseRotate", "Rotate", "Rotates when placing base blocks.", var224, false);
      this.damageCategory = new CategorySetting("Damage", "The category for settings related to damage calculations.");
      CategorySetting var227 = this.damageCategory;
      CategoryCondition var226 = new CategoryCondition(var227);
      Double var228 = 6.0;
      Double var229 = 0.0;
      Double var230 = 36.0;
      this.minimumDamage = new NumberSetting("MinimumDamage", "Minimum", "The minimum damage that has to be dealt to enemies.", var226, var228, var229, var230);
      CategorySetting var232 = this.damageCategory;
      CategoryCondition var231 = new CategoryCondition(var232);
      Double var233 = 10.0;
      Double var234 = 0.0;
      Double var235 = 36.0;
      this.maximumSelfDamage = new NumberSetting(
         "MaximumSelfDamage", "MaximumSelf", "The maximum damage that can be dealt to you by crystals.", var231, var233, var234, var235
      );
      CategorySetting var237 = this.damageCategory;
      CategoryCondition var236 = new CategoryCondition(var237);
      Float var238 = 1.5F;
      Float var239 = 0.0F;
      Float var240 = 4.0F;
      this.lethalMultiplier = new NumberSetting(
         "LethalMultiplier", "The amount of crystals that the target has to be killed by in order to ignore minimum damage.", var236, var238, var239, var240
      );
      CategorySetting var242 = this.damageCategory;
      CategoryCondition var241 = new CategoryCondition(var242);
      this.antiSuicide = new BooleanSetting("AntiSuicide", "Prevents crystals from accidentally killing you when you're low on health.", var241, true);
      CategorySetting var244 = this.damageCategory;
      CategoryCondition var243 = new CategoryCondition(var244);
      this.ignoreTerrain = new BooleanSetting("IgnoreTerrain", "Ignores terrain that can be destroyed when calculating damage.", var243, true);
      this.renderCategory = new CategorySetting("Render", "Contains all of the settings relating to position rendering.");
      CategorySetting var246 = this.renderCategory;
      CategoryCondition var245 = new CategoryCondition(var246);
      String[] var247 = new String[]{"Static", "Slide"};
      this.animation2 = new ModeSetting("Animation", "The animation that will be applied to the rendering.", var245, "Static", var247);
      CategorySetting var249 = this.renderCategory;
      CategoryCondition var248 = new CategoryCondition(var249);
      String[] var250 = new String[]{"Fade", "Shrink"};
      this.mode = new ModeSetting("Mode", "The mode for the auto crystal render.", var248, "Fade", var250);
      CategorySetting var252 = this.renderCategory;
      CategoryCondition var251 = new CategoryCondition(var252);
      Integer var253 = 300;
      Integer var254 = 0;
      Integer var255 = 1000;
      this.duration = new NumberSetting("Duration", "The duration for the place render.", var251, var253, var254, var255);
      CategorySetting var257 = this.renderCategory;
      CategoryCondition var256 = new CategoryCondition(var257);
      Integer var258 = 1;
      Integer var259 = 0;
      Integer var260 = 20;
      this.smoothness = new NumberSetting("Smoothness", "The smoothness for the slide while place position is changing.", var256, var258, var259, var260);
      CategorySetting var262 = this.renderCategory;
      CategoryCondition var261 = new CategoryCondition(var262);
      String[] var263 = new String[]{"None", "Fill", "Outline", "Both"};
      this.renderMode = new ModeSetting("RenderMode", "The rendering that will be applied to the target position.", var261, "Both", var263);
      ModeSetting var265 = this.renderMode;
      String[] var266 = new String[]{"Fill", "Both"};
      ModeCondition var264 = new ModeCondition(var265, var266);
      ColorValue var267 = ColorUtil.b();
      this.fillColorUp = new ColorSetting("FillColorUp", "The color that will be used for the fill gradiant upper part rendering.", var264, var267);
      ModeSetting var269 = this.renderMode;
      String[] var270 = new String[]{"Fill", "Both"};
      ModeCondition var268 = new ModeCondition(var269, var270);
      ColorValue var271 = ColorUtil.b();
      this.fillColorDown = new ColorSetting("FillColorDown", "The color that will be used for the fill gradiant lower part rendering.", var268, var271);
      ModeSetting var273 = this.renderMode;
      String[] var274 = new String[]{"Outline", "Both"};
      ModeCondition var272 = new ModeCondition(var273, var274);
      ColorValue var275 = ColorUtil.c();
      this.outlineColorUp = new ColorSetting("OutlineColorUp", "The color that will be used for the outline gradiant upper part rendering.", var272, var275);
      ModeSetting var277 = this.renderMode;
      String[] var278 = new String[]{"Outline", "Both"};
      ModeCondition var276 = new ModeCondition(var277, var278);
      ColorValue var279 = ColorUtil.c();
      this.outlineColorDown = new ColorSetting("OutlineColorDown", "The color that will be used for the outline gradiant lower part rendering.", var276, var279);
      CategorySetting var280 = this.renderCategory;
      this.renderDamage = new BooleanSetting(
         "RenderDamage", "Damage", "Renders the damage that the position will do to the opponent.", new CategoryCondition(var280), false
      );
      CategorySetting var281 = this.renderCategory;
      this.icon = new BooleanSetting("Icon", "Renders a customizable crystal icon on the rendered position.", new CategoryCondition(var281), false);
      BooleanSetting var283 = this.icon;
      BooleanCondition var282 = new BooleanCondition(var283, true);
      Integer var284 = 3;
      Integer var285 = 1;
      Integer var286 = 5;
      this.iconScale = new NumberSetting("IconScale", "The scaling that will be applied to the crystal icon rendering.", var282, var284, var285, var286);
      BooleanSetting var288 = this.icon;
      BooleanCondition var287 = new BooleanCondition(var288, true);
      Float var289 = 2.0F;
      Float var290 = 0.0F;
      Float var291 = 5.0F;
      this.iconRadius = new NumberSetting("IconRadius", "The difference between the outer circle and the inner circle.", var287, var289, var290, var291);
      BooleanSetting var293 = this.icon;
      BooleanCondition var292 = new BooleanCondition(var293, true);
      ColorValue var294 = ColorUtil.a();
      this.iconColor = new ColorSetting("IconColor", "The color that will be used for the crystal icon rendering.", var292, var294);
      CategorySetting var295 = this.renderCategory;
      this.shader = new ModeSetting(
         "Shader", "The animated shader that will be drawn on the target position instead of a flat color.", new CategoryCondition(var295), "None", be
      );
      ModeSetting var297 = this.shader;
      ModeCondition var296 = new ModeCondition(var297, bf);
      Float var298 = 1.0F;
      Float var299 = 0.1F;
      Float var300 = 10.0F;
      this.shaderSpeed = new NumberSetting("ShaderSpeed", "Speed", "The speed at which the shader animates.", var296, var298, var299, var300);
      ModeSetting var302 = this.shader;
      ModeCondition var301 = new ModeCondition(var302, bf);
      Integer var303 = 100;
      Integer var304 = 0;
      Integer var305 = 100;
      this.shaderOpacity = new NumberSetting("ShaderOpacity", "Opacity", "The opacity of the shader rendering.", var301, var303, var304, var305);
      ModeSetting var306 = this.shader;
      this.shaderDistanceScaling = new BooleanSetting(
         "ShaderDistanceScaling", "DistanceScaling", "Scales the shader pattern by your distance to the position.", new ModeCondition(var306, bf), false
      );
      ModeSetting var308 = this.shader;
      String[] var309 = new String[]{"Gradient"};
      ModeCondition var307 = new ModeCondition(var308, var309);
      Float var310 = 50.0F;
      Float var311 = 0.1F;
      Float var312 = 200.0F;
      this.shaderStep = new NumberSetting("ShaderStep", "Step", "The size of the gradient bands.", var307, var310, var311, var312);
      ModeSetting var313 = this.shader;
      String[] var314 = new String[]{"Gradient"};
      this.shaderColor1 = new ColorSetting(
         "ShaderColor1", "The first gradient color.", new ModeCondition(var313, var314), new ColorValue(new Color(255, 0, 255, 255), false, false)
      );
      ModeSetting var315 = this.shader;
      String[] var316 = new String[]{"Gradient"};
      this.shaderColor2 = new ColorSetting(
         "ShaderColor2", "The second gradient color.", new ModeCondition(var315, var316), new ColorValue(new Color(255, 0, 0, 255), false, false)
      );
      ModeSetting var317 = this.shader;
      String[] var318 = new String[]{"Gradient"};
      this.shaderColor3 = new ColorSetting(
         "ShaderColor3", "The third gradient color.", new ModeCondition(var317, var318), new ColorValue(new Color(0, 255, 0, 255), false, false)
      );
      ModeSetting var319 = this.shader;
      String[] var320 = new String[]{"Gradient"};
      this.shaderColor4 = new ColorSetting(
         "ShaderColor4", "The fourth gradient color.", new ModeCondition(var319, var320), new ColorValue(new Color(0, 0, 255, 255), false, false)
      );
      ModeSetting var322 = this.shader;
      String[] var323 = new String[]{"Glowing"};
      ModeCondition var321 = new ModeCondition(var322, var323);
      ColorValue var324 = new ColorValue(new Color(255, 0, 255, 255), false, false);
      this.shaderGlowColor = new ColorSetting("ShaderGlowColor", "GlowColor", "The color that the glow shader will be tinted with.", var321, var324);
      ExecutorService var325 = Executors.newSingleThreadExecutor();
      this.bi = var325;
      this.bj = null;
      this.bk = false;
      Set var326 = Set.of();
      this.bl = var326;
      this.bm = null;
      this.bn = null;
      ScheduledExecutorService var327 = Executors.newSingleThreadScheduledExecutor(var0 -> {
         Thread var1x = new Thread(var0, "Night-AutoCrystal-Pacing");
         var1x.setDaemon(true);
         return var1x;
      });
      this.bo = var327;
      this.bp = new ConcurrentLinkedQueue<>();
      long var328 = 0L;
      this.bq = var328;
      long var330 = 0L;
      this.br = var330;
      this.bs = new ConcurrentHashMap<>();
      this.bt = new ConcurrentHashMap<>();
      this.bu = new ConcurrentHashMap<>();
      this.bv = new TimerUtil();
      this.bw = new TimerUtil();
      this.bx = new TimerUtil();
      this.by = new TimerUtil();
      this.bz = new TimerUtil();
      this.bA = new TimerUtil();
      this.bB = -1;
      long var332 = 0L;
      this.bC = var332;
      long var334 = 0L;
      this.bD = var334;
      this.bE = false;
      this.bF = true;
      this.bG = false;
      this.bH = false;
      this.bI = null;
      this.bJ = null;
      this.bK = null;
      this.bL = null;
      this.bM = null;
      this.bN = null;
      this.bO = null;
      long var336 = 0L;
      this.bP = var336;
      this.bQ = false;
      this.bR = "0.00ms";
      this.bS = 0;
      this.bT = "0.00";
      this.bU = new RollingCounter();
      this.bV = 0;
      this.bW = -100000;
      this.bX = 0;
      List var338 = Collections.emptyList();
      this.bY = var338;
      long var339 = 0L;
      this.bZ = var339;
      this.cb = "-";
      long var341 = 0L;
      this.cc = var341;
      this.ce = 0;
      this.cf = -1;
      long var343 = 0L;
      this.ch = var343;
      long var345 = 0L;
      this.ci = var345;
      List var347 = List.of();
      this.cj = var347;
   }

   private float y() {
      LicenseService var1 = Night.licenseService;
      long var2;
      if (var1 != null && var1.getRole() != null) {
         var2 = var1.getHeartbeatAgeMs();
      } else {
         var2 = Long.MAX_VALUE;
      }

      long var4 = var2;
      long var6 = 300000L;
      long var8 = var4 - var6;
      long var10 = -1L;
      int var12 = (int)((var8 ^ var10) >>> 63);
      float var13 = this.attackSpeed.getValue().floatValue();
      float var14 = (float)DamageUtil.a_double_int(0);
      float var15 = var13 * var14 * 50.0F;
      float var16 = 1000.0F - var15;
      ModeSetting var17 = this.rotate;
      String[] var18 = new String[2];
      String var19 = this.rotate.getValue();
      var18[0] = var19;
      var18[1] = "None";
      int var20 = var12;
      String var21 = var18[var20];
      var17.setValue(var21);
      float var22 = var16;
      int var23 = var12;
      float var24 = 1 - var23;
      float var25 = var22 * var24;
      float var26 = var12;
      double var27 = ThreadLocalRandom.current().nextDouble();
      double var29 = 1000.0;
      float var31 = (float)(var27 * var29);
      float var32 = var26 * var31;
      float var33 = var25 + var32;
      if (this.y.getValue()) {
         float var34 = var33;
         float var35 = (float)this.bq;
         var33 = var34 + var35;
      }

      float var36 = var33;
      return Math.max(0.0F, var36);
   }

   private float z() {
      float var1 = this.placeSpeed.getValue().floatValue();
      float var2 = (float)DamageUtil.a_double_int(0);
      float var3 = var1 * var2 * 50.0F;
      float var4 = 1000.0F - var3;
      LicenseService var5 = Night.licenseService;
      long var6;
      if (var5 != null && var5.getRole() != null) {
         var6 = var5.getHeartbeatAgeMs();
      } else {
         var6 = Long.MAX_VALUE;
      }

      long var8 = var6;
      long var10 = 300000L;
      long var12 = var8 - var10;
      long var14 = -1L;
      int var16 = (int)((var12 ^ var14) >>> 63);
      float var17 = var4;
      int var18 = var16;
      float var19 = 1 - var18;
      float var20 = var17 * var19;
      float var21 = var16;
      double var22 = ThreadLocalRandom.current().nextDouble();
      double var24 = 1000.0;
      float var26 = (float)(var22 * var24);
      float var27 = var21 * var26;
      float var28 = var20 + var27;
      if (this.y.getValue()) {
         float var29 = var28;
         float var30 = (float)this.br;
         var28 = var29 + var30;
      }

      float var31 = var28;
      return Math.max(0.0F, var31);
   }

   private void A() {
      double var1 = Math.random();
      double var3 = 6.0;
      long var5 = (long)(var1 * var3);
      this.bq = var5;
   }

   private void B() {
      double var1 = Math.random();
      double var3 = 6.0;
      long var5 = (long)(var1 * var3);
      this.br = var5;
   }

   private double a_double_Entity(Entity var1) {
      AABB var2 = var1.getBoundingBox();
      Vec3 var3 = mc.player.getEyePosition();
      double var4 = var2.distanceToSqr(var3);
      AABB var6 = var2;
      Vec3 var7 = Night.positionManager.getServerEyePosition();
      double var8 = var6.distanceToSqr(var7);
      return Math.max(var4, var8);
   }

   private boolean C() {
      if (!this.y.getValue()) {
         return false;
      }

      long var1 = System.currentTimeMillis();

      while (!this.bp.isEmpty()) {
         long var3 = var1;
         long var5 = this.bp.peek();
         long var7 = var3 - var5;
         long var9 = 1000L;
         if (var7 <= var9) {
            break;
         }

         this.bp.poll();
      }

      boolean var11;
      if (this.bp.size() >= 36) {
         var11 = true;
      } else {
         var11 = false;
      }

      return var11;
   }

   private void D() {
      if (this.y.getValue()) {
         Queue var1 = this.bp;
         Long var2 = System.currentTimeMillis();
         var1.add(var2);
      }
   }

   private boolean E() {
      if (mc.player != null && mc.level != null) {
         return !mc.player.isAlive() || mc.player.isDeadOrDying() || mc.player.getHealth() <= 0.0F ? true : mc.gui.screen() instanceof DeathScreen;
      } else {
         return true;
      }
   }

   @EventTarget
   public void a_void_UpdateWalkingPlayerEvent(UpdateWalkingPlayerEvent var1) {
      if (!PingBypassState.a()) {
         AutoShop var2 = (AutoShop)Night.moduleManager.getModule(AutoShop.class);
         if (var2 == null || !var2.x()) {
            if (this.E()) {
               this.bm = null;
               this.bn = null;
               this.bI = null;
               this.bK = null;
               this.bJ = null;
               this.bL = null;
               this.bt.clear();
               this.bs.clear();
               this.bu.clear();
               Night.renderPlacementManager.setCrystalTarget(null);
            } else {
               int var3 = mc.player.getInventory().getSelectedSlot();
               if (this.bB != -1) {
                  int var4 = this.bB;
                  int var5 = var3;
                  if (var4 != var5) {
                     this.bz.reset();
                  }
               }

               int var6 = var3;
               this.bB = var6;
               long var7 = 50L;
               this.bs.entrySet().removeIf(var2x -> {
                  long var3x = System.currentTimeMillis();
                  long var5x = var2x.getValue();
                  long var7x = var3x - var5x;
                  long var9x = Night.serverManager.getPing();
                  long var11x = 2L;
                  long var13x = var9x * var11x;
                  long var15x = var7;
                  long var17x = Math.max(var13x, var15x);
                  boolean var19x;
                  if (var7x > var17x) {
                     var19x = true;
                  } else {
                     var19x = false;
                  }

                  return var19x;
               });
               long var9 = Night.serverManager.getPing();
               long var11 = 2L;
               long var13 = var9 * var11;
               long var15 = 500L;
               long var17 = Math.max(var13, var15);
               float var19 = this.attackSpeed.getValue().floatValue();
               long var20 = (long)((20.0F - var19) * 50.0F);
               long var22 = var17 + var20;
               this.bt.entrySet().removeIf(var2x -> {
                  long var3x = System.currentTimeMillis();
                  long var5x = var2x.getValue();
                  long var7x = var3x - var5x;
                  long var9x = var22;
                  boolean var11x;
                  if (var7x > var9x) {
                     var11x = true;
                  } else {
                     var11x = false;
                  }

                  return var11x;
               });
               this.bu.entrySet().removeIf(var2x -> {
                  long var3x = System.currentTimeMillis();
                  long var5x = var2x.getValue();
                  long var7x = var3x - var5x;
                  long var9x = Night.serverManager.getPing();
                  long var11x = 2L;
                  long var13x = var9x * var11x;
                  long var15x = var7;
                  long var17x = Math.max(var13x, var15x);
                  boolean var19x;
                  if (var7x > var17x) {
                     var19x = true;
                  } else {
                     var19x = false;
                  }

                  return var19x;
               });
               int var24 = this.bU.getCount();
               this.bV = var24;
               if (this.losDebug.getValue()) {
                  TimerUtil var25 = this.bA;
                  Long var26 = 1000L;
                  if (var25.passedMs(var26)) {
                     this.bA.reset();
                     this.F();
                  }
               }

               if (!this.gameLoop.getValue()) {
                  this.H();
               }
            }
         }
      }
   }

   private void F() {
      Vec3 var1 = mc.player.getEyePosition();
      ClientLevel var2 = mc.level;
      Vec3 var3 = var1;
      Vec3 var4 = var1;
      Vec3 var5 = mc.player.getViewVector(1.0F);
      double var6 = 8.0;
      Vec3 var8 = var5.scale(var6);
      Vec3 var9 = var4.add(var8);
      LocalPlayer var10 = mc.player;
      BlockHitResult var11 = var2.clip(new ClipContext(var3, var9, Block.OUTLINE, Fluid.NONE, var10));
      if (var11 instanceof BlockHitResult var12 && var11.getType() == Type.BLOCK) {
         BlockPos var13;
         BlockHitResult var18;
         boolean var21;
         label128: {
            var13 = var12.getBlockPos();
            ClientLevel var14 = mc.level;
            Vec3 var15 = var1;
            Vec3 var16 = Vec3.atCenterOf(var13);
            LocalPlayer var17 = mc.player;
            var18 = var14.clip(new ClipContext(var15, var16, Block.COLLIDER, Fluid.NONE, var17));
            label113:
            if (var18.getType() != Type.MISS) {
               if (var18 instanceof BlockHitResult) {
                  BlockPos var19 = var18.getBlockPos();
                  BlockPos var20 = var13;
                  if (var19.equals(var20)) {
                     break label113;
                  }
               }

               var21 = false;
               break label128;
            }

            var21 = true;
         }

         boolean var22;
         boolean var48;
         label129: {
            var22 = var21;
            ClientLevel var23 = mc.level;
            Vec3 var24 = var1;
            double var26 = var13.getX();
            double var28 = 0.5;
            double var30 = var26 + var28;
            double var32 = var13.getY();
            double var34 = 1.0;
            double var36 = var32 + var34;
            double var38 = var13.getZ();
            double var40 = 0.5;
            double var42 = var38 + var40;
            Vec3 var25 = new Vec3(var30, var36, var42);
            LocalPlayer var44 = mc.player;
            BlockHitResult var45 = var23.clip(new ClipContext(var24, var25, Block.COLLIDER, Fluid.NONE, var44));
            label104:
            if (var45.getType() != Type.MISS) {
               if (var45 instanceof BlockHitResult) {
                  BlockPos var46 = var45.getBlockPos();
                  BlockPos var47 = var13;
                  if (var46.equals(var47)) {
                     break label104;
                  }
               }

               var48 = false;
               break label129;
            }

            var48 = true;
         }

         boolean var49 = var48;
         boolean var50 = CrystalRaytraceHelper.b(var13);
         String var52;
         if (var18 instanceof BlockHitResult var51 && var18.getType() == Type.BLOCK) {
            var52 = var51.getBlockPos().toShortString();
         } else {
            var52 = "-";
         }

         String var53 = var52;
         LocalPlayer var54 = mc.player;
         LocalPlayer var55 = mc.player;
         int var56 = this.extrapolation.getValue().intValue();
         AABB var57 = PositionUtil.a(var55, var56);
         BlockPos var58 = var13;
         boolean var59 = this.ignoreTerrain.getValue();
         float var60 = DamageUtil.a(var54, var57, var58, null, var59);
         Player var61 = this.bI;
         float var62;
         if (var61 == null) {
            var62 = -1.0F;
         } else {
            Player var63 = var61;
            Player var64 = var61;
            int var65 = this.extrapolation.getValue().intValue();
            AABB var66 = PositionUtil.a(var64, var65);
            BlockPos var67 = var13;
            boolean var68 = this.ignoreTerrain.getValue();
            var62 = DamageUtil.a(var63, var66, var67, null, var68);
         }

         float var69 = var62;
         BlockPos var70;
         if (this.bK == null) {
            var70 = null;
         } else {
            var70 = this.bK.b();
         }

         BlockPos var71;
         ChatManager var72;
         String var73;
         boolean var74;
         boolean var75;
         boolean var76;
         String var77;
         String var82;
         boolean var87;
         label91: {
            var71 = var70;
            var72 = Night.chatManager;
            var73 = var13.toShortString();
            var74 = var22;
            var75 = var49;
            var76 = var50;
            var77 = var53;
            Object[] var78 = new Object[1];
            Vec3 var79 = var1;
            Vec3 var80 = Vec3.atCenterOf(var13);
            Double var81 = Math.sqrt(var79.distanceToSqr(var80));
            var78[0] = var81;
            var82 = String.format("%.2f", var78);
            ClientLevel var83 = mc.level;
            BlockPos var84 = var13;
            if (var83.getBlockState(var84).getBlock() != Blocks.OBSIDIAN) {
               ClientLevel var85 = mc.level;
               BlockPos var86 = var13;
               if (var85.getBlockState(var86).getBlock() != Blocks.BEDROCK) {
                  var87 = false;
                  break label91;
               }
            }

            var87 = true;
         }

         ClientLevel var88 = mc.level;
         BlockPos var89 = var13.above();
         boolean var90 = var88.getBlockState(var89).isAir();
         Object[] var91 = new Object[1];
         Float var92 = var60;
         var91[0] = var92;
         String var93 = String.format("%.1f", var91);
         Object[] var94 = new Object[1];
         Float var95 = var69;
         var94[0] = var95;
         String var96 = String.format("%.1f", var94);
         String var97;
         if (var71 == null) {
            var97 = "none";
         } else {
            var97 = var71.toShortString();
         }

         String var98 = this.bR;
         String var99 = this.getName();
         var72.sendTaggedMessage(
            var73
               + " los="
               + var74
               + "/"
               + var75
               + "/"
               + var76
               + " blockedBy="
               + var77
               + " dist="
               + var82
               + " base="
               + var87
               + " air="
               + var90
               + " self="
               + var93
               + " dmg="
               + var96
               + " picked="
               + var97
               + " calc="
               + var98,
            var99
         );
         EndCrystal var100 = null;
         double var101 = Double.MAX_VALUE;

         for (Entity var104 : (Iterable<Entity>)(Object)mc.level.entitiesForRendering()) {
            if (var104 instanceof EndCrystal var105 && var105.isAlive()) {
               AABB var106 = var105.getBoundingBox();
               Vec3 var107 = var1;
               double var108 = var106.distanceToSqr(var107);
               double var110 = var101;
               if (var108 < var110) {
                  var101 = var108;
                  var100 = var105;
               }
            }
         }

         if (var100 != null) {
            LocalPlayer var112 = mc.player;
            LocalPlayer var113 = mc.player;
            int var114 = this.extrapolation.getValue().intValue();
            AABB var115 = PositionUtil.a(var113, var114);
            EndCrystal var116 = var100;
            boolean var117 = this.ignoreTerrain.getValue();
            float var118 = DamageUtil.a_float_Entity_AABB_EndCrystal_bool(var112, var115, var116, var117);
            boolean var119 = ((Suicide)Night.moduleManager.getModule(Suicide.class)).isEnabled();
            float var120 = var118;
            float var121 = this.maximumSelfDamage.getValue().floatValue();
            boolean var122;
            if (var120 > var121) {
               var122 = true;
            } else {
               var122 = false;
            }

            boolean var123;
            boolean var128;
            label77: {
               var123 = var122;
               if (this.antiSuicide.getValue()) {
                  float var124 = var118;
                  float var125 = mc.player.getHealth();
                  float var126 = mc.player.getAbsorptionAmount();
                  float var127 = var125 + var126;
                  if (var124 > var127) {
                     var128 = true;
                     break label77;
                  }
               }

               var128 = false;
            }

            boolean var129 = var128;
            ChatManager var130 = Night.chatManager;
            String var131 = var100.blockPosition().toShortString();
            Object[] var132 = new Object[1];
            Float var133 = var118;
            var132[0] = var133;
            String var134 = String.format("%.1f", var132);
            float var135 = this.maximumSelfDamage.getValue().floatValue();
            boolean var136 = var119;
            boolean var137 = var123;
            boolean var138 = this.antiSuicide.getValue();
            boolean var139 = var129;
            boolean var140;
            if (!var119 && (var123 || var129)) {
               var140 = false;
            } else {
               var140 = true;
            }

            String var141 = this.getName();
            var130.sendTaggedMessage(
               "nearestCrystal="
                  + var131
                  + " attackSelf="
                  + var134
                  + " maxSelf="
                  + var135
                  + " suicideOn="
                  + var136
                  + " overMax="
                  + var137
                  + " antiSuicide="
                  + var138
                  + " overHp="
                  + var139
                  + " gatePasses="
                  + var140,
               var141
            );
         }
      }
   }

   @EventTarget
   public void a_void_MotionSyncEvent(MotionSyncEvent var1) {
      if (!PingBypassState.a()) {
         if (!this.E()) {
            this.O();
            Set var2 = Set.copyOf(Night.worldStateManager.getPlacedPositions());
            this.bl = var2;
            if (this.asynchronous.getValue()) {
               if (!this.bk || this.bj == null || this.bj.isDone() || this.bj.isCancelled()) {
                  this.bk = true;
                  Future var3 = this.bi.submit(this::G);
                  this.bj = var3;
               }
            } else {
               this.bk = false;
               Set var4 = this.bl;
               this.a_void_Set_bool(var4, false);
            }
         }
      }
   }

   private void G() {
      if (this.bk && this.asynchronous.getValue() && this.isEnabled()) {
         label89: {
            try {
               try {
                  if (mc.player != null && mc.level != null) {
                     Set var1 = this.bl;
                     this.a_void_Set_bool(var1, true);
                  }
                  break label89;
               } catch (Throwable var19) {
                  this.a_void_String_Throwable("asyncLoopStep", var19);
               }
            } catch (Throwable var20) {
               if (this.bk && this.asynchronous.getValue() && this.isEnabled()) {
                  try {
                     Thread.sleep(5L);
                     Future var10 = this.bi.submit(this::G);
                     this.bj = var10;
                  } catch (InterruptedException var13) {
                     Thread.currentThread().interrupt();
                     this.bk = false;
                  } catch (Throwable var14) {
                     this.bk = false;
                  }
               } else {
                  this.bk = false;
               }

               throw var20;
            }

            if (this.bk && this.asynchronous.getValue() && this.isEnabled()) {
               try {
                  Thread.sleep(5L);
                  Future var6 = this.bi.submit(this::G);
                  this.bj = var6;
               } catch (InterruptedException var15) {
                  Thread.currentThread().interrupt();
                  this.bk = false;
               } catch (Throwable var16) {
                  this.bk = false;
               }

               return;
            }

            this.bk = false;
            return;
         }

         if (this.bk && this.asynchronous.getValue() && this.isEnabled()) {
            try {
               Thread.sleep(5L);
               Future var2 = this.bi.submit(this::G);
               this.bj = var2;
            } catch (InterruptedException var17) {
               Thread.currentThread().interrupt();
               this.bk = false;
            } catch (Throwable var18) {
               this.bk = false;
            }
         } else {
            this.bk = false;
         }
      } else {
         this.bk = false;
      }
   }

   private void a_void_String_Throwable(String var1, Throwable var2) {
      long var3 = System.currentTimeMillis();
      long var5 = this.cc;
      long var7 = var3 - var5;
      long var9 = 5000L;
      if (var7 >= var9) {
         long var11 = var3;
         this.cc = var11;
         Object[] var13 = new Object[]{var1, null, null};
         Long var14 = 5000L;
         var13[1] = var14;
         var13[2] = var2;
         Night.LOGGER.warn("AutoCrystal {} threw (throttled {}ms) -- calc frozen until this stops", var13);
      }
   }

   private void a_void_Set_bool(Set<BlockPos> var1, boolean var2) {
      long var3 = System.nanoTime();

      try {
         EndCrystal var5 = this.N();
         this.bJ = var5;
         AutoCrystal.PlaceTarget var6 = this.a_PlaceTarget_BlockPos_Set(null, var1);
         this.bK = var6;
      } catch (Throwable var35) {
         this.cb = "ERROR";
         this.a_void_String_Throwable("runCalculation", var35);
         return;
      }

      long var9 = System.nanoTime();
      long var11 = var3;
      long var13 = var9 - var11;
      DecimalFormat var15 = new DecimalFormat("0.00");
      double var16 = var13;
      double var18 = 1000000.0;
      double var20 = var16 / var18;
      this.bR = var15.format(var20) + "ms";
      int var22;
      if (this.bK == null) {
         var22 = 0;
      } else {
         var22 = this.bK.g();
      }

      this.bS = var22;
      String var23;
      if (this.bK == null) {
         var23 = "0.00";
      } else {
         DecimalFormat var24 = new DecimalFormat("0.00");
         double var25 = this.bK.f();
         var23 = var24.format(var25);
      }

      this.bT = var23;
      Player var27;
      if (this.bK == null) {
         var27 = null;
      } else {
         var27 = this.bK.c();
      }

      this.bI = var27;
      if (this.blockDestruction.getValue() && var2) {
         SpeedMine var28 = (SpeedMine)Night.moduleManager.getModule(SpeedMine.class);
         BlockPos var29 = null;
         if (var28.v() != null && var28.v().e()) {
            var29 = var28.v().a();
         }

         if (var29 != null) {
            BlockPos var30 = var29;
            AutoCrystal.PlaceTarget var31 = this.a_PlaceTarget_BlockPos_Set(var30, var1);
            this.bL = var31;
         }

         BlockPos var32 = null;
         if (var28.farReach.getValue()) {
            if (var28.w() != null && var28.w().c()) {
               var32 = var28.w().a();
            }
         } else if (var28.w() != null && var28.w().c()) {
            var32 = var28.w().a();
         }

         if (var32 != null) {
            BlockPos var33 = var32;
            AutoCrystal.PlaceTarget var34 = this.a_PlaceTarget_BlockPos_Set(var33, var1);
            this.bM = var34;
         }
      }
   }

   @EventTarget
   public void a_void_ItemUseEvent(ItemUseEvent var1) {
      if (!PingBypassState.a()) {
         if (!this.E()) {
            if (this.gameLoop.getValue()) {
               TimerUtil var2 = this.by;
               Long var3 = this.loopDelay.getValue().longValue();
               if (var2.passedMs(var3)) {
                  this.by.reset();
                  this.H();
                  if (this.bm != null) {
                     this.bm.run();
                     this.bm = null;
                  }

                  if (this.bn != null) {
                     this.bn.run();
                     this.bn = null;
                  }
               }
            }
         }
      }
   }

   private void H() {
      if (!this.E()) {
         if (!this.I() && !this.J()) {
            if (!this.K() && !this.L()) {
               this.bm = null;
               this.bn = null;
               if (this.basePlace.getValue()) {
                  int var1 = this.ce;
                  int var2 = this.baseDelay.getValue().intValue();
                  if (var1 < var2) {
                     int var3 = this.ce + 1;
                     this.ce = var3;
                  } else if (this.S()) {
                     this.ce = 0;
                  }
               }

               if (this.sequential.getValue().equalsIgnoreCase("None")) {
                  if (this.bE) {
                     this.bE = false;
                     this.bF = true;
                     this.M();
                     return;
                  }

                  if (this.bF) {
                     this.bE = true;
                     this.bF = false;
                     this.b_void_bool(false);
                  }
               } else {
                  if (this.attack.getValue()) {
                     this.M();
                  }

                  if (this.place.getValue()) {
                     this.b_void_bool(false);
                  }
               }
            }
         }
      }
   }

   private boolean I() {
      if (!this.pauseOnSecondaryMine.getValue()) {
         return false;
      }

      SpeedMine var1 = (SpeedMine)Night.moduleManager.getModule(SpeedMine.class);
      if (var1 == null) {
         return false;
      }

      if (var1.farReach.getValue()) {
         FadeAnimation var2 = var1.w();
         boolean var3;
         if (var2 != null && var2.c() && var2.f() >= 0.85F) {
            var3 = true;
         } else {
            var3 = false;
         }

         return var3;
      } else {
         FadeAnimation var4 = var1.w();
         boolean var5;
         if (var4 != null && var4.c() && var4.f() >= 0.85F) {
            var5 = true;
         } else {
            var5 = false;
         }

         return var5;
      }
   }

   private boolean J() {
      if (!this.awaitMine.getValue()) {
         return false;
      }

      SpeedMine var1;
      if (Night.moduleManager != null) {
         var1 = (SpeedMine)Night.moduleManager.getModule(SpeedMine.class);
      } else {
         var1 = null;
      }

      SpeedMine var2 = var1;
      if (var2 == null) {
         return false;
      }

      MineTask var3 = var2.v();
      if (var3 != null && var3.e()) {
         int var4 = var3.d();
         int var5 = this.awaitMineTicks.getValue().intValue();
         if (var4 < var5) {
            return true;
         }
      }

      return false;
   }

   private boolean K() {
      if (!this.pauseOnPearl.getValue()) {
         return false;
      }

      Phase var1;
      if (Night.moduleManager != null) {
         var1 = (Phase)Night.moduleManager.getModule(Phase.class);
      } else {
         var1 = null;
      }

      Phase var2 = var1;
      if (var2 != null && var2.isEnabled()) {
         return true;
      }

      KeyAction var3;
      if (Night.moduleManager != null) {
         var3 = (KeyAction)Night.moduleManager.getModule(KeyAction.class);
      } else {
         var3 = null;
      }

      KeyAction var4 = var3;
      if (var4 != null && var4.w()) {
         return true;
      }

      long var5 = System.currentTimeMillis();
      long var7 = this.ch;
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

   private boolean L() {
      if (!this.pauseOnXP.getValue()) {
         return false;
      }

      KeyAction var1;
      if (Night.moduleManager != null) {
         var1 = (KeyAction)Night.moduleManager.getModule(KeyAction.class);
      } else {
         var1 = null;
      }

      KeyAction var2 = var1;
      if (var2 != null && var2.v()) {
         return true;
      }

      long var3 = System.currentTimeMillis();
      long var5 = this.ci;
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

   @EventTarget
   public void a_void_Post(Post var1) {
      if (!PingBypassState.a()) {
         if (!this.E()) {
            if (this.bm != null) {
               this.bm.run();
               this.bm = null;
            }

            if (this.bn != null) {
               if (this.y.getValue()) {
                  long var2 = 10L;
                  double var4 = Math.random();
                  double var6 = 11.0;
                  long var8 = (long)(var4 * var6);
                  long var10 = var2 + var8;
                  Runnable var12 = this.bn;
                  this.bn = null;
                  ScheduledExecutorService var13 = this.bo;
                  Runnable var14 = var12;
                  long var15 = var10;
                  var13.schedule(() -> {
                     if (mc.player != null && mc.level != null && this.isEnabled()) {
                        mc.execute(var14);
                     }
                  }, var15, TimeUnit.MILLISECONDS);
               } else {
                  this.bn.run();
                  this.bn = null;
               }
            }
         }
      }
   }

   @EventTarget
   public void a_void_EntitySpawnEvent(EntitySpawnEvent var1) {
      if (!PingBypassState.a()) {
         if (!this.E() && !this.R().isEmpty()) {
            if (!this.b_bool_String("Attack")) {
               if (this.attack.getValue() && this.instant.getValue()) {
                  TimerUtil var2 = this.bv;
                  Float var3 = this.y();
                  if (var2.passedMs(var3)) {
                     if (var1.getEntity() instanceof EndCrystal var5) {
                        if (this.inhibit.getValue()) {
                           Map var6 = this.bs;
                           Integer var7 = var5.getId();
                           if (var6.containsKey(var7)) {
                              return;
                           }
                        }

                        EndCrystal var8 = var5;
                        if (!this.a_bool_EndCrystal(var8)) {
                           BlockPos var9 = var5.blockPosition().below();
                           Map var10 = this.bt;
                           BlockPos var11 = var9;
                           if (!var10.containsKey(var11)) {
                              double var12 = var5.getX();
                              double var14 = var5.getY();
                              double var16 = 0.5;
                              double var18 = var14 - var16;
                              double var20 = var5.getZ();
                              var9 = BlockPos.containing(var12, var18, var20);
                           }

                           Map var22 = this.bt;
                           BlockPos var23 = var9;
                           if (var22.containsKey(var23)) {
                              EndCrystal var24 = var5;
                              double var25 = this.a_double_Entity(var24);
                              double var27 = Mth.square(this.attackRange.getValue().doubleValue());
                              if (!(var25 > var27)) {
                                 WorldBorder var29 = mc.level.getWorldBorder();
                                 BlockPos var30 = var5.blockPosition();
                                 if (var29.isWithinBounds(var30)) {
                                    boolean var36;
                                    label84: {
                                       if (!this.raytrace.getValue()) {
                                          EndCrystal var31 = var5;
                                          double var32 = this.a_double_Entity(var31);
                                          double var34 = Mth.square(this.attackWallsRange.getValue().doubleValue());
                                          if (!(var32 > var34)) {
                                             var36 = false;
                                             break label84;
                                          }
                                       }

                                       var36 = true;
                                    }

                                    if (!var36 || BlockInteractionUtil.a_bool_Entity(var5)) {
                                       if (!((Suicide)Night.moduleManager.getModule(Suicide.class)).isEnabled()) {
                                          LocalPlayer var37 = mc.player;
                                          LocalPlayer var38 = mc.player;
                                          int var39 = this.extrapolation.getValue().intValue();
                                          AABB var40 = PositionUtil.a(var38, var39);
                                          EndCrystal var41 = var5;
                                          boolean var42 = this.ignoreTerrain.getValue();
                                          float var43 = DamageUtil.a_float_Entity_AABB_EndCrystal_bool(var37, var40, var41, var42);
                                          float var44 = this.maximumSelfDamage.getValue().floatValue();
                                          if (var43 > var44) {
                                             return;
                                          }

                                          if (this.antiSuicide.getValue()) {
                                             float var45 = var43;
                                             float var46 = mc.player.getHealth();
                                             float var47 = mc.player.getAbsorptionAmount();
                                             float var48 = var46 + var47;
                                             if (var45 > var48) {
                                                return;
                                             }
                                          }
                                       }

                                       EndCrystal var49 = var5;
                                       float[] var50 = RotationMathUtil.a(this.b_Vec3_Entity(var49));
                                       if (this.rotate.getValue().equalsIgnoreCase("Packet") || this.rotate.getValue().equalsIgnoreCase("Silent")) {
                                          float[] var51 = var50;
                                          this.a_void_floatArr(var51);
                                       }

                                       if (this.rotate.getValue().equalsIgnoreCase("Normal")) {
                                          EndCrystal var52 = var5;
                                          Vec3 var53 = this.b_Vec3_Entity(var52);
                                          float[] var54 = this.a_floatArr_Vec3(var53);
                                          int var55 = Night.rotationManager.getModulePriority(this);
                                          Night.rotationManager.rotate(var54, var55);
                                       }

                                       EndCrystal var56 = var5;
                                       this.b_void_EndCrystal(var56);
                                       this.bG = true;
                                       if (this.sequential.getValue().equalsIgnoreCase("Strong")) {
                                          if (this.y.getValue()) {
                                             long var57 = 10L;
                                             double var59 = Math.random();
                                             double var61 = 11.0;
                                             long var63 = (long)(var59 * var61);
                                             long var65 = var57 + var63;
                                             ScheduledExecutorService var67 = this.bo;
                                             long var68 = var65;
                                             var67.schedule(() -> {
                                                if (mc.player != null && mc.level != null && this.isEnabled()) {
                                                   mc.execute(() -> {
                                                      this.b_void_bool(true);
                                                      this.bn = null;
                                                   });
                                                }
                                             }, var68, TimeUnit.MILLISECONDS);
                                          } else {
                                             this.b_void_bool(true);
                                             this.bn = null;
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
         }
      }
   }

   @EventTarget
   public void a_void_BlockPlaceEvent(BlockPlaceEvent var1) {
      if (!PingBypassState.a()) {
         if (!this.E() && !this.R().isEmpty()) {
            if (!this.b_bool_String("Place")) {
               boolean var4;
               label190: {
                  this.bX = 0;
                  if (this.mineIgnore.getValue() && var1.getPos() != null) {
                     BlockPos var2 = var1.getPos();
                     BlockPos var3 = this.bN;
                     if (var2.equals(var3) && this.bO != null) {
                        var4 = true;
                        break label190;
                     }
                  }

                  var4 = false;
               }

               boolean var5 = var4;
               if (this.mineIgnore.getValue() && var1.getPos() != null) {
                  BlockPos var6 = var1.getPos();
                  BlockPos var7 = this.bN;
                  if (var6.equals(var7)) {
                     this.Q();
                  }
               }

               if (!var5) {
                  if (this.blockDestruction.getValue()) {
                     TimerUtil var8 = this.bw;
                     Float var9 = this.z();
                     if (var8.passedMs(var9)) {
                        BlockPos var10 = var1.getPos();
                        if (var10 != null) {
                           int var11 = InventoryUtil.b_int_Item(Items.END_CRYSTAL);
                           int var12 = mc.player.getInventory().getSelectedSlot();
                           boolean var13 = false;
                           if (this.switchSetting.getValue().equalsIgnoreCase("None")
                              || var11 != -1
                              || mc.player.getMainHandItem().getItem() == Items.END_CRYSTAL
                              || mc.player.getOffhandItem().getItem() == Items.END_CRYSTAL) {
                              AutoCrystal.PlaceTarget var14;
                              if (this.bL == null) {
                                 var14 = null;
                              } else {
                                 var14 = this.bL.a();
                              }

                              AutoCrystal.PlaceTarget var15 = var14;
                              AutoCrystal.PlaceTarget var16;
                              if (this.bM == null) {
                                 var16 = null;
                              } else {
                                 var16 = this.bM.a();
                              }

                              AutoCrystal.PlaceTarget var20;
                              label199: {
                                 AutoCrystal.PlaceTarget var17 = var16;
                                 if (var15 != null && var15.b() != null) {
                                    BlockPos var18 = var10;
                                    BlockPos var19 = var15.e();
                                    if (var18.equals(var19)) {
                                       var20 = var15;
                                       break label199;
                                    }
                                 }

                                 if (var17 != null && var17.b() != null) {
                                    BlockPos var21 = var10;
                                    BlockPos var22 = var17.e();
                                    if (var21.equals(var22)) {
                                       var20 = var17;
                                       break label199;
                                    }
                                 }

                                 if (!this.asynchronous.getValue()) {
                                    BlockPos var23 = var10;
                                    var20 = this.d_PlaceTarget_BlockPos(var23);
                                 } else {
                                    AutoCrystal.PlaceTarget var24;
                                    if (var15 != null) {
                                       var24 = var15;
                                    } else {
                                       var24 = var17;
                                    }

                                    var20 = var24;
                                 }
                              }

                              if (var20 != null && var20.b() != null && !(var20.f() <= 0.0F)) {
                                 BlockPos var25 = var20.b();
                                 Vec3 var26 = mc.player.getEyePosition();
                                 Vec3 var27 = Vec3.atCenterOf(var25);
                                 double var28 = var26.distanceToSqr(var27);
                                 double var30 = Mth.square(this.placeRange.getValue().doubleValue());
                                 if (var28 > var30) {
                                    Night.renderPlacementManager.setCrystalTarget(null);
                                 } else {
                                    boolean var39;
                                    label157: {
                                       BlockPos var32 = var25;
                                       Night.renderPlacementManager.setCrystalTarget(var32);
                                       if (!this.raytrace.getValue()) {
                                          Vec3 var33 = mc.player.getEyePosition();
                                          Vec3 var34 = Vec3.atCenterOf(var25);
                                          double var35 = var33.distanceToSqr(var34);
                                          double var37 = Mth.square(this.placeWallsRange.getValue().doubleValue());
                                          if (!(var35 > var37)) {
                                             var39 = false;
                                             break label157;
                                          }
                                       }

                                       var39 = true;
                                    }

                                    if (!var39 || BlockInteractionUtil.f(var25)) {
                                       EndCrystal var40 = null;
                                       ClientLevel var41 = mc.level;
                                       Entity var42 = (Entity)null;
                                       BlockPos var43 = var25.above();

                                       for (Entity var45 : (Iterable<Entity>)(Object)var41.getEntities(var42, new AABB(var43), var0 -> true)) {
                                          if (var45 instanceof EndCrystal) {
                                             var40 = (EndCrystal)var45;
                                             break;
                                          }
                                       }

                                       if (var40 != null) {
                                          EndCrystal var46 = var40;
                                          if (this.a_bool_EndCrystal(var46)) {
                                             Night.renderPlacementManager.setCrystalTarget(null);
                                          } else if (!this.attack.getValue()) {
                                             Night.renderPlacementManager.setCrystalTarget(null);
                                          } else {
                                             EndCrystal var61 = var40;
                                             float[] var62 = RotationMathUtil.a(this.b_Vec3_Entity(var61));
                                             if (this.rotate.getValue().equalsIgnoreCase("Normal")) {
                                                float[] var63 = var62;
                                                int var64 = Night.rotationManager.getModulePriority(this);
                                                Night.rotationManager.rotate(var63, this, var64);
                                             }

                                             if (this.rotate.getValue().equalsIgnoreCase("Packet") || this.rotate.getValue().equalsIgnoreCase("Silent")) {
                                                float[] var65 = var62;
                                                this.a_void_floatArr(var65);
                                             }

                                             EndCrystal var66 = var40;
                                             this.b_void_EndCrystal(var66);
                                          }
                                       } else {
                                          CrystalRaytraceHelper.a(var25);
                                          BlockPos var47 = var25;
                                          Vec3 var48 = this.h_Vec3_BlockPos(var47);
                                          float[] var49 = this.a_floatArr_Vec3(var48);
                                          if (this.rotate.getValue().equalsIgnoreCase("Normal")) {
                                             float[] var50 = var49;
                                             int var51 = Night.rotationManager.getModulePriority(this) + 1;
                                             Night.rotationManager.rotate(var50, this, var51);
                                          }

                                          if (this.rotate.getValue().equalsIgnoreCase("Packet") || this.rotate.getValue().equalsIgnoreCase("Silent")) {
                                             float[] var52 = var49;
                                             this.a_void_floatArr(var52);
                                          }

                                          if (!this.switchSetting.getValue().equalsIgnoreCase("None")
                                             && mc.player.getOffhandItem().getItem() != Items.END_CRYSTAL) {
                                             if (this.switchSetting.getValue().equalsIgnoreCase("Normal") && this.swapBack.getValue() && this.cf == -1) {
                                                int var53 = var12;
                                                this.cf = var53;
                                             }

                                             String var54 = this.switchSetting.getValue();
                                             int var55 = var11;
                                             int var56 = var12;
                                             InventoryUtil.a_bool_String_int_int(var54, var55, var56);
                                             var13 = true;
                                          }

                                          BlockPos var57 = var25;
                                          this.e_void_BlockPos(var57);
                                          if (var13) {
                                             String var58 = this.switchSetting.getValue();
                                             int var59 = var11;
                                             int var60 = var12;
                                             InventoryUtil.b(var58, var59, var60);
                                          }
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
         }
      }
   }

   @EventTarget
   public void a_void_PacketReceiveEvent(PacketReceiveEvent var1) {
      if (!PingBypassState.a()) {
         if (mc.player != null && mc.level != null) {
            if (var1.getPacket() instanceof ClientboundAddEntityPacket var3) {
               int var4 = var3.getId();
               int var5 = this.bW;
               if (var4 > var5) {
                  int var6 = var3.getId();
                  this.bW = var6;
               }

               double var7 = var3.getX();
               double var9 = var3.getY();
               double var11 = var3.getZ();
               BlockPos var13 = BlockPos.containing(var7, var9, var11).offset(0, -1, 0);
               Map var14 = this.bu;
               BlockPos var15 = var13;
               if (var14.containsKey(var15)) {
                  TimerUtil var16 = this.bx;
                  long var17 = this.faceplaceDelay.getValue().longValue();
                  long var19 = 50L;
                  Long var21 = var17 * var19;
                  if (var16.passedMs(var21)) {
                     this.bx.reset();
                  }

                  Map var22 = this.bu;
                  BlockPos var23 = var13;
                  var22.remove(var23);
                  this.bU.increment();
                  int var24 = this.bU.getCount();
                  this.bV = var24;
               }
            }
         }
      }
   }

   @EventTarget
   public void a_void_PacketSendEvent(PacketSendEvent var1) {
      if (var1.getPacket() instanceof ServerboundSetCarriedItemPacket) {
         this.bz.reset();
      }

      if (var1.getPacket() instanceof ServerboundUseItemPacket && mc.player != null) {
         if (mc.player.getMainHandItem().getItem() == Items.ENDER_PEARL || mc.player.getOffhandItem().getItem() == Items.ENDER_PEARL) {
            long var2 = System.currentTimeMillis();
            this.ch = var2;
         } else if (mc.player.getMainHandItem().getItem() == Items.EXPERIENCE_BOTTLE || mc.player.getOffhandItem().getItem() == Items.EXPERIENCE_BOTTLE) {
            long var4 = System.currentTimeMillis();
            this.ci = var4;
         }
      }
   }

   @EventTarget
   public void a_void_PlayerDeathEvent(PlayerDeathEvent var1) {
      if (!PingBypassState.a()) {
         this.bX = 0;
         this.bm = null;
         this.bn = null;
         this.bI = null;
         this.bK = null;
         this.bJ = null;
         this.bL = null;
         this.bt.clear();
         this.bs.clear();
         this.bu.clear();
         Night.renderPlacementManager.setCrystalTarget(null);
      }
   }

   @EventTarget
   public void a_void_ClientConnectEvent(ClientConnectEvent var1) {
      if (!PingBypassState.a()) {
         this.bW = -100000;
      }
   }

   public void onDisable() {
      this.bk = false;
      this.bd.a();
      if (this.bj != null) {
         this.bj.cancel(false);
         this.bj = null;
      }

      if (this.cf != -1) {
         InventoryUtil.d(this.cf);
         this.cf = -1;
      }

      this.bm = null;
      this.bn = null;
      Night.renderPlacementManager.setCrystalTarget(null);
      this.bs.clear();
      this.bt.clear();
      this.bu.clear();
      this.bG = false;
      this.bH = false;
      this.bI = null;
      this.bK = null;
      this.bL = null;
      this.bR = "0.00ms";
      this.bS = 0;
      this.bT = "0.00";
      this.cb = "-";
      this.bU.reset();
      this.bp.clear();
      this.bW = -100000;
      this.bY.clear();
      long var1 = 0L;
      this.bZ = var1;
   }

   public String getDisplayInfo() {
      String var1 = this.bR;
      int var2 = this.bS;
      String var3 = this.bT;
      int var4 = this.bV;
      String var5 = this.cb;
      return var1 + ", " + var2 + ", " + var3 + ", " + var4 + ", " + var5;
   }

   public boolean a_bool_BlockPos(BlockPos var1) {
      if (var1 != null && mc.level != null) {
         ClientLevel var2 = mc.level;
         Entity var3 = (Entity)null;
         AABB var4 = new AABB(var1);
         double var5 = 1.0;
         AABB var7 = var4.inflate(var5);
         boolean var8;
         if (!var2.getEntities(var3, var7, var0 -> {
            boolean var1x;
            if (var0 instanceof EndCrystal && var0.isAlive()) {
               var1x = true;
            } else {
               var1x = false;
            }

            return var1x;
         }).isEmpty()) {
            var8 = true;
         } else {
            var8 = false;
         }

         return var8;
      } else {
         return false;
      }
   }

   private boolean c_bool_BlockPos(BlockPos var1) {
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
            var2 = (SpeedMine)Night.moduleManager.getModule(SpeedMine.class);
         } else {
            var2 = null;
         }

         SpeedMine var3 = var2;
         if (var3 != null && var3.isEnabled()) {
            MineTask var4 = var3.v();
            if (var4 != null && var4.a() != null) {
               BlockPos var5 = var4.a();
               if (this.a_bool_EndCrystal_BlockPos(var1, var5)) {
                  if (this.attack.getValue()) {
                     BlockPos var6 = var5;
                     if (this.c_bool_BlockPos(var6)) {
                        return false;
                     }
                  }

                  return true;
               }
            }

            FadeAnimation var7 = var3.w();
            if (var7 != null && var7.a() != null) {
               BlockPos var8 = var7.a();
               if (this.a_bool_EndCrystal_BlockPos(var1, var8)) {
                  if (this.attack.getValue()) {
                     BlockPos var9 = var8;
                     if (this.c_bool_BlockPos(var9)) {
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

   private void M() {
      if (!this.E()) {
         if (!this.b_bool_String("Attack")) {
            if (!this.C()) {
               if (this.R().isEmpty()) {
                  this.bJ = null;
               } else {
                  EndCrystal var1;
                  boolean var2;
                  var1 = null;
                  var2 = false;
                  AutoCrystal.PlaceTarget var3 = this.bK;
                  label282:
                  if (var3 != null && var3.b() == null && var3.d() != null && !var3.d().isEmpty()) {
                     Iterator var4 = var3.d().iterator();
                     EndCrystal var6 = null;

                     while (true) {
                        if (!var4.hasNext()) {
                           break label282;
                        }

                        Entity var5 = (Entity)var4.next();
                        if (var5 instanceof EndCrystal _ec6 && (var6 = _ec6).isAlive()) {
                           EndCrystal var7 = var6;
                           if (!this.a_bool_EndCrystal(var7)) {
                              EndCrystal var8 = var6;
                              double var9 = this.a_double_Entity(var8);
                              double var11 = Mth.square(this.attackRange.getValue().doubleValue());
                              if (!(var9 > var11)) {
                                 WorldBorder var13 = mc.level.getWorldBorder();
                                 BlockPos var14 = var6.blockPosition();
                                 if (var13.isWithinBounds(var14)) {
                                    boolean var20;
                                    label259: {
                                       if (!this.raytrace.getValue()) {
                                          EndCrystal var15 = var6;
                                          double var16 = this.a_double_Entity(var15);
                                          double var18 = Mth.square(this.attackWallsRange.getValue().doubleValue());
                                          if (!(var16 > var18)) {
                                             var20 = false;
                                             break label259;
                                          }
                                       }

                                       var20 = true;
                                    }

                                    if (!var20 || BlockInteractionUtil.a_bool_Entity(var6)) {
                                       if (((Suicide)Night.moduleManager.getModule(Suicide.class)).isEnabled()) {
                                          break;
                                       }

                                       LocalPlayer var21 = mc.player;
                                       LocalPlayer var22 = mc.player;
                                       int var23 = this.extrapolation.getValue().intValue();
                                       AABB var24 = PositionUtil.a(var22, var23);
                                       EndCrystal var25 = var6;
                                       boolean var26 = this.ignoreTerrain.getValue();
                                       float var27 = DamageUtil.a_float_Entity_AABB_EndCrystal_bool(var21, var24, var25, var26);
                                       float var28 = this.maximumSelfDamage.getValue().floatValue();
                                       if (!(var27 > var28)) {
                                          if (!this.antiSuicide.getValue()) {
                                             break;
                                          }

                                          float var29 = var27;
                                          float var30 = mc.player.getHealth();
                                          float var31 = mc.player.getAbsorptionAmount();
                                          float var32 = var30 + var31;
                                          if (!(var29 > var32)) {
                                             break;
                                          }
                                       }
                                    }
                                 }
                              }
                           }
                        }
                     }

                     var1 = var6;
                     var2 = true;
                  }

                  if (var1 == null) {
                     var1 = this.bJ;
                  }

                  if (var1 == null) {
                     float var33 = -1.0F;
                     List var34 = this.R();
                     List var35 = this.a_List_List(var34);
                     Iterator var36 = mc.level.entitiesForRendering().iterator();

                     EndCrystal var38 = null;
                     label229:
                     while (true) {
                        while (true) {
                           while (true) {
                              if (!var36.hasNext()) {
                                 break label229;
                              }

                              Entity var37 = (Entity)var36.next();
                              if (var37 instanceof EndCrystal _ec38 && (var38 = _ec38).isAlive()) {
                                 EndCrystal var39 = var38;
                                 if (!this.a_bool_EndCrystal(var39)) {
                                    if (!this.inhibit.getValue()) {
                                       break;
                                    }

                                    Map var40 = this.bs;
                                    Integer var41 = var38.getId();
                                    if (!var40.containsKey(var41)) {
                                       break;
                                    }

                                    long var42 = System.currentTimeMillis();
                                    Map var44 = this.bs;
                                    Integer var45 = var38.getId();
                                    long var46 = (Long)var44.get(var45);
                                    long var48 = var42 - var46;
                                    long var50 = Night.serverManager.getPing();
                                    long var52 = 2L;
                                    long var54 = var50 * var52;
                                    long var56 = 100L;
                                    long var58 = Math.max(var54, var56);
                                    if (var48 >= var58) {
                                       break;
                                    }
                                 }
                              }
                           }

                           BlockPos var60 = var38.blockPosition().below();
                           Map var61 = this.bt;
                           BlockPos var62 = var60;
                           if (!var61.containsKey(var62)) {
                              double var63 = var38.getX();
                              double var65 = var38.getY();
                              double var67 = 0.5;
                              double var69 = var65 - var67;
                              double var71 = var38.getZ();
                              var60 = BlockPos.containing(var63, var69, var71);
                           }

                           Map var73 = this.bt;
                           BlockPos var74 = var60;
                           if (var73.containsKey(var74)) {
                              EndCrystal var75 = var38;
                              double var76 = this.a_double_Entity(var75);
                              double var78 = Mth.square(this.attackRange.getValue().doubleValue());
                              if (!(var76 > var78)) {
                                 WorldBorder var80 = mc.level.getWorldBorder();
                                 BlockPos var81 = var38.blockPosition();
                                 if (var80.isWithinBounds(var81)) {
                                    boolean var87;
                                    label204: {
                                       if (!this.raytrace.getValue()) {
                                          EndCrystal var82 = var38;
                                          double var83 = this.a_double_Entity(var82);
                                          double var85 = Mth.square(this.attackWallsRange.getValue().doubleValue());
                                          if (!(var83 > var85)) {
                                             var87 = false;
                                             break label204;
                                          }
                                       }

                                       var87 = true;
                                    }

                                    if (!var87 || BlockInteractionUtil.a_bool_Entity(var38)) {
                                       if (((Suicide)Night.moduleManager.getModule(Suicide.class)).isEnabled()) {
                                          break;
                                       }

                                       LocalPlayer var88 = mc.player;
                                       LocalPlayer var89 = mc.player;
                                       int var90 = this.extrapolation.getValue().intValue();
                                       AABB var91 = PositionUtil.a(var89, var90);
                                       EndCrystal var92 = var38;
                                       boolean var93 = this.ignoreTerrain.getValue();
                                       float var94 = DamageUtil.a_float_Entity_AABB_EndCrystal_bool(var88, var91, var92, var93);
                                       float var95 = this.maximumSelfDamage.getValue().floatValue();
                                       if (!(var94 > var95)) {
                                          if (!this.antiSuicide.getValue()) {
                                             break;
                                          }

                                          float var96 = var94;
                                          float var97 = mc.player.getHealth();
                                          float var98 = mc.player.getAbsorptionAmount();
                                          float var99 = var97 + var98;
                                          if (!(var96 > var99)) {
                                             break;
                                          }
                                       }
                                    }
                                 }
                              }
                           }
                        }

                        float var100 = 0.0F;

                        for (Player var102 : (Iterable<Player>)(Object)var35) {
                           int var103 = this.extrapolation.getValue().intValue();
                           AABB var104 = PositionUtil.a(var102, var103);
                           EndCrystal var105 = var38;
                           boolean var106 = this.ignoreTerrain.getValue();
                           float var107 = DamageUtil.a_float_Entity_AABB_EndCrystal_bool(var102, var104, var105, var106);
                           float var108 = var100;
                           if (var107 > var108) {
                              var100 = var107;
                           }
                        }

                        float var109 = var100;
                        float var110 = var33;
                        if (var109 > var110) {
                           var33 = var100;
                           var1 = var38;
                           var2 = true;
                        }
                     }
                  }

                  if (var1 != null) {
                     EndCrystal var111 = var1;
                     if (!this.a_bool_EndCrystal(var111)) {
                        if (!((Suicide)Night.moduleManager.getModule(Suicide.class)).isEnabled()) {
                           LocalPlayer var112 = mc.player;
                           LocalPlayer var113 = mc.player;
                           int var114 = this.extrapolation.getValue().intValue();
                           AABB var115 = PositionUtil.a(var113, var114);
                           EndCrystal var116 = var111;
                           boolean var117 = this.ignoreTerrain.getValue();
                           float var118 = DamageUtil.a_float_Entity_AABB_EndCrystal_bool(var112, var115, var116, var117);
                           float var119 = this.maximumSelfDamage.getValue().floatValue();
                           if (var118 > var119) {
                              return;
                           }

                           if (this.antiSuicide.getValue()) {
                              float var120 = var118;
                              float var121 = mc.player.getHealth();
                              float var122 = mc.player.getAbsorptionAmount();
                              float var123 = var121 + var122;
                              if (var120 > var123) {
                                 return;
                              }
                           }
                        }

                        EndCrystal var124 = var111;
                        Vec3 var125 = this.b_Vec3_Entity(var124);
                        float[] var126 = this.a_floatArr_Vec3(var125);
                        if (this.rotate.getValue().equalsIgnoreCase("Normal")) {
                           float[] var127 = var126;
                           int var128 = Night.rotationManager.getModulePriority(this);
                           Night.rotationManager.rotate(var127, var128);
                        }

                        if (this.swapDelay.getValue().intValue() > 0) {
                           TimerUtil var129 = this.bz;
                           long var130 = this.swapDelay.getValue().longValue();
                           long var132 = 50L;
                           Long var134 = var130 * var132;
                           if (!var129.passedMs(var134)) {
                              return;
                           }
                        }

                        TimerUtil var135 = this.bv;
                        Float var136 = this.y();
                        if (var135.passedMs(var136)) {
                           ClientLevel var137 = mc.level;
                           int var138 = var111.getId();
                           Entity var139 = var137.getEntity(var138);
                           String var140 = null;
                           if (var139 == null) {
                              var140 = "entity-gone";
                           } else if (!(var139 instanceof EndCrystal)) {
                              var140 = "not-end-crystal";
                           } else if (!((EndCrystal)var139).isAlive()) {
                              var140 = "dead";
                           } else {
                              label152: {
                                 if (this.inhibit.getValue() && !var2) {
                                    Map var141 = this.bs;
                                    Integer var142 = var139.getId();
                                    if (var141.containsKey(var142)) {
                                       var140 = "inhibit";
                                       break label152;
                                    }
                                 }

                                 Entity var143 = var139;
                                 double var144 = this.a_double_Entity(var143);
                                 double var146 = Mth.square(this.attackRange.getValue().doubleValue());
                                 if (var144 > var146) {
                                    var140 = "range";
                                 } else {
                                    WorldBorder var148 = mc.level.getWorldBorder();
                                    BlockPos var149 = var139.blockPosition();
                                    if (!var148.isWithinBounds(var149)) {
                                       var140 = "border";
                                    } else {
                                       boolean var155;
                                       label144: {
                                          if (!this.raytrace.getValue()) {
                                             Entity var150 = var139;
                                             double var151 = this.a_double_Entity(var150);
                                             double var153 = Mth.square(this.attackWallsRange.getValue().doubleValue());
                                             if (!(var151 > var153)) {
                                                var155 = false;
                                                break label144;
                                             }
                                          }

                                          var155 = true;
                                       }

                                       if (var155 && !BlockInteractionUtil.a_bool_Entity(var139)) {
                                          var140 = "cannot-see";
                                       }
                                    }
                                 }
                              }
                           }

                           if (var140 == null) {
                              EndCrystal var156 = var111;
                              this.bm = () -> {
                                 if (this.rotate.getValue().equalsIgnoreCase("Packet") || this.rotate.getValue().equalsIgnoreCase("Silent")) {
                                    float[] var2x = RotationMathUtil.a(this.b_Vec3_Entity(var156));
                                    this.a_void_floatArr(var2x);
                                 }

                                 this.b_void_EndCrystal(var156);
                              };
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private void b_void_bool(boolean var1) {
      if (!this.E()) {
         if (!this.b_bool_String("Place")) {
            if (!this.C()) {
               AutoCrystal.PlaceTarget var2;
               if (this.bK == null) {
                  var2 = null;
               } else {
                  var2 = this.bK.a();
               }

               AutoCrystal.PlaceTarget var3 = var2;
               if (var3 != null && var3.b() != null) {
                  if (var3.c() != null && var3.c().isAlive() && !var3.c().isDeadOrDying() && !(var3.c().getHealth() <= 0.0F)) {
                     int var4 = InventoryUtil.b_int_Item(Items.END_CRYSTAL);
                     int var5 = mc.player.getInventory().getSelectedSlot();
                     if (this.switchSetting.getValue().equalsIgnoreCase("None")
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
                                 if (this.placements.getValue().equalsIgnoreCase("Protocol")) {
                                    ClientLevel var22 = mc.level;
                                    BlockPos var23 = var6.offset(0, 2, 0);
                                    if (!var22.getBlockState(var23).isAir()) {
                                       return;
                                    }
                                 }

                                 boolean var30;
                                 label235: {
                                    if (!this.raytrace.getValue()) {
                                       Vec3 var24 = mc.player.getEyePosition();
                                       Vec3 var25 = Vec3.atCenterOf(var6);
                                       double var26 = var24.distanceToSqr(var25);
                                       double var28 = Mth.square(this.placeWallsRange.getValue().doubleValue());
                                       if (!(var26 > var28)) {
                                          var30 = false;
                                          break label235;
                                       }
                                    }

                                    var30 = true;
                                 }

                                 if (!var30 || BlockInteractionUtil.f(var6)) {
                                    ClientLevel var31 = mc.level;
                                    Entity var32 = (Entity)null;
                                    BlockPos var33 = var6;
                                    AABB var34 = this.j_AABB_BlockPos(var33);
                                    if (!var31.getEntities(var32, var34, var0 -> true).stream().anyMatch(var0 -> {
                                       boolean var1x;
                                       if (var0.isAlive() && !(var0 instanceof ExperienceOrb) && !(var0 instanceof EndCrystal)) {
                                          var1x = true;
                                       } else {
                                          var1x = false;
                                       }

                                       return var1x;
                                    })) {
                                       EndCrystal var35 = null;
                                       ClientLevel var36 = mc.level;
                                       Entity var37 = (Entity)null;
                                       BlockPos var38 = var6;
                                       AABB var39 = this.j_AABB_BlockPos(var38);
                                       Iterator var40 = var36.getEntities(var37, var39, var0 -> {
                                          boolean var1x;
                                          if (var0 instanceof EndCrystal && var0.isAlive()) {
                                             var1x = true;
                                          } else {
                                             var1x = false;
                                          }

                                          return var1x;
                                       }).iterator();
                                       if (var40.hasNext()) {
                                          var35 = (EndCrystal)var40.next();
                                       }

                                       if (var35 != null) {
                                          if (this.attack.getValue()) {
                                             EndCrystal var41 = var35;
                                             if (!this.a_bool_EndCrystal(var41)) {
                                                if (!((Suicide)Night.moduleManager.getModule(Suicide.class)).isEnabled()) {
                                                   LocalPlayer var42 = mc.player;
                                                   LocalPlayer var43 = mc.player;
                                                   int var44 = this.extrapolation.getValue().intValue();
                                                   AABB var45 = PositionUtil.a(var43, var44);
                                                   EndCrystal var46 = var35;
                                                   boolean var47 = this.ignoreTerrain.getValue();
                                                   float var48 = DamageUtil.a_float_Entity_AABB_EndCrystal_bool(var42, var45, var46, var47);
                                                   float var49 = this.maximumSelfDamage.getValue().floatValue();
                                                   if (var48 <= var49) {
                                                      if (this.antiSuicide.getValue()) {
                                                         float var50 = var48;
                                                         float var51 = mc.player.getHealth();
                                                         float var52 = mc.player.getAbsorptionAmount();
                                                         float var53 = var51 + var52;
                                                         if (!(var50 <= var53)) {
                                                            return;
                                                         }
                                                      }

                                                      EndCrystal var54 = var35;
                                                      Vec3 var55 = this.b_Vec3_Entity(var54);
                                                      float[] var56 = this.a_floatArr_Vec3(var55);
                                                      if (this.rotate.getValue().equalsIgnoreCase("Normal")) {
                                                         float[] var57 = var56;
                                                         int var58 = Night.rotationManager.getModulePriority(this);
                                                         Night.rotationManager.rotate(var57, var58);
                                                      }

                                                      if (this.rotate.getValue().equalsIgnoreCase("Packet")
                                                         || this.rotate.getValue().equalsIgnoreCase("Silent")) {
                                                         EndCrystal var59 = var35;
                                                         float[] var60 = RotationMathUtil.a(this.b_Vec3_Entity(var59));
                                                         this.a_void_floatArr(var60);
                                                      }

                                                      EndCrystal var61 = var35;
                                                      this.b_void_EndCrystal(var61);
                                                   }
                                                } else {
                                                   EndCrystal var62 = var35;
                                                   Vec3 var63 = this.b_Vec3_Entity(var62);
                                                   float[] var64 = this.a_floatArr_Vec3(var63);
                                                   if (this.rotate.getValue().equalsIgnoreCase("Normal")) {
                                                      float[] var65 = var64;
                                                      int var66 = Night.rotationManager.getModulePriority(this);
                                                      Night.rotationManager.rotate(var65, var66);
                                                   }

                                                   if (this.rotate.getValue().equalsIgnoreCase("Packet") || this.rotate.getValue().equalsIgnoreCase("Silent")) {
                                                      EndCrystal var67 = var35;
                                                      float[] var68 = RotationMathUtil.a(this.b_Vec3_Entity(var67));
                                                      this.a_void_floatArr(var68);
                                                   }

                                                   EndCrystal var69 = var35;
                                                   this.b_void_EndCrystal(var69);
                                                }
                                             }
                                          }
                                       } else {
                                          for (Entity var71 : (Iterable<Entity>)(Object)mc.level.entitiesForRendering()) {
                                             if (var71 instanceof EndCrystal var72 && var72.isAlive()) {
                                                EndCrystal var73 = var72;
                                                if (!this.a_bool_EndCrystal(var73)) {
                                                   BlockPos var74 = var72.blockPosition().below();
                                                   Map var75 = this.bt;
                                                   BlockPos var76 = var74;
                                                   if (!var75.containsKey(var76)) {
                                                      double var77 = var72.getX();
                                                      double var79 = var72.getY();
                                                      double var81 = 0.5;
                                                      double var83 = var79 - var81;
                                                      double var85 = var72.getZ();
                                                      var74 = BlockPos.containing(var77, var83, var85);
                                                   }

                                                   Map var87 = this.bt;
                                                   BlockPos var88 = var74;
                                                   if (var87.containsKey(var88)) {
                                                      EndCrystal var89 = var72;
                                                      double var90 = this.a_double_Entity(var89);
                                                      double var92 = Mth.square(this.attackRange.getValue().doubleValue());
                                                      boolean var94;
                                                      if (var90 <= var92) {
                                                         var94 = true;
                                                      } else {
                                                         var94 = false;
                                                      }

                                                      boolean var95;
                                                      boolean var100;
                                                      label221: {
                                                         var95 = var94;
                                                         if (!this.raytrace.getValue()) {
                                                            double var96 = var90;
                                                            double var98 = Mth.square(this.attackWallsRange.getValue().doubleValue());
                                                            if (!(var96 > var98)) {
                                                               var100 = false;
                                                               break label221;
                                                            }
                                                         }

                                                         var100 = true;
                                                      }

                                                      boolean var101 = var100;
                                                      boolean var102;
                                                      if (!var95 || var101 && !BlockInteractionUtil.a_bool_Entity(var72)) {
                                                         var102 = false;
                                                      } else {
                                                         var102 = true;
                                                      }

                                                      if (var102) {
                                                         if (this.attack.getValue()) {
                                                            if (!((Suicide)Night.moduleManager.getModule(Suicide.class)).isEnabled()) {
                                                               LocalPlayer var103 = mc.player;
                                                               LocalPlayer var104 = mc.player;
                                                               int var105 = this.extrapolation.getValue().intValue();
                                                               AABB var106 = PositionUtil.a(var104, var105);
                                                               EndCrystal var107 = var72;
                                                               boolean var108 = this.ignoreTerrain.getValue();
                                                               float var109 = DamageUtil.a_float_Entity_AABB_EndCrystal_bool(var103, var106, var107, var108);
                                                               float var110 = this.maximumSelfDamage.getValue().floatValue();
                                                               if (var109 <= var110) {
                                                                  if (this.antiSuicide.getValue()) {
                                                                     float var111 = var109;
                                                                     float var112 = mc.player.getHealth();
                                                                     float var113 = mc.player.getAbsorptionAmount();
                                                                     float var114 = var112 + var113;
                                                                     if (!(var111 <= var114)) {
                                                                        return;
                                                                     }
                                                                  }

                                                                  EndCrystal var115 = var72;
                                                                  Vec3 var116 = this.b_Vec3_Entity(var115);
                                                                  float[] var117 = this.a_floatArr_Vec3(var116);
                                                                  if (this.rotate.getValue().equalsIgnoreCase("Normal")) {
                                                                     float[] var118 = var117;
                                                                     int var119 = Night.rotationManager.getModulePriority(this);
                                                                     Night.rotationManager.rotate(var118, var119);
                                                                  }

                                                                  if (this.rotate.getValue().equalsIgnoreCase("Packet")
                                                                     || this.rotate.getValue().equalsIgnoreCase("Silent")) {
                                                                     EndCrystal var120 = var72;
                                                                     float[] var121 = RotationMathUtil.a(this.b_Vec3_Entity(var120));
                                                                     this.a_void_floatArr(var121);
                                                                  }

                                                                  EndCrystal var122 = var72;
                                                                  this.b_void_EndCrystal(var122);
                                                               }
                                                            } else {
                                                               EndCrystal var147 = var72;
                                                               Vec3 var148 = this.b_Vec3_Entity(var147);
                                                               float[] var149 = this.a_floatArr_Vec3(var148);
                                                               if (this.rotate.getValue().equalsIgnoreCase("Normal")) {
                                                                  float[] var150 = var149;
                                                                  int var151 = Night.rotationManager.getModulePriority(this);
                                                                  Night.rotationManager.rotate(var150, var151);
                                                               }

                                                               if (this.rotate.getValue().equalsIgnoreCase("Packet")
                                                                  || this.rotate.getValue().equalsIgnoreCase("Silent")) {
                                                                  EndCrystal var152 = var72;
                                                                  float[] var153 = RotationMathUtil.a(this.b_Vec3_Entity(var152));
                                                                  this.a_void_floatArr(var153);
                                                               }

                                                               EndCrystal var154 = var72;
                                                               this.b_void_EndCrystal(var154);
                                                            }
                                                         }

                                                         return;
                                                      }

                                                      BlockPos var123 = var74;
                                                      BlockPos var124 = var6;
                                                      if (var123.equals(var124)) {
                                                         return;
                                                      }
                                                   }
                                                }
                                             }
                                          }

                                          int var125 = this.T();
                                          int var126 = this.multiPlace.getValue().intValue();
                                          if (var125 < var126) {
                                             List var127 = this.cj;
                                             CrystalRaytraceHelper.a(var6);
                                             BlockPos var128 = var6;
                                             Vec3 var129 = this.h_Vec3_BlockPos(var128);
                                             float[] var130 = this.a_floatArr_Vec3(var129);
                                             if (this.rotate.getValue().equalsIgnoreCase("Normal")) {
                                                float[] var131 = var130;
                                                int var132 = Night.rotationManager.getModulePriority(this);
                                                Night.rotationManager.rotate(var131, var132);
                                             }

                                             if (this.swapDelay.getValue().intValue() > 0) {
                                                TimerUtil var133 = this.bz;
                                                long var134 = this.swapDelay.getValue().longValue();
                                                long var136 = 50L;
                                                Long var138 = var134 * var136;
                                                if (!var133.passedMs(var138)) {
                                                   return;
                                                }
                                             }

                                             TimerUtil var139 = this.bw;
                                             Float var140 = this.z();
                                             if (var139.passedMs(var140)) {
                                                if (!var1 && this.bH) {
                                                   this.bH = false;
                                                } else {
                                                   Vec3 var141 = var129;
                                                   int var142 = var5;
                                                   int var143 = var4;
                                                   BlockPos var144 = var6;
                                                   int var145 = var125;
                                                   List var146 = var127;
                                                   this.bn = () -> {
                                                      boolean var7x = false;
                                                      if (this.rotate.getValue().equalsIgnoreCase("Packet")
                                                         || this.rotate.getValue().equalsIgnoreCase("Silent")) {
                                                         float[] var8x = RotationMathUtil.a(var141);
                                                         this.a_void_floatArr(var8x);
                                                      }

                                                      if (mc.player.getOffhandItem().getItem() != Items.END_CRYSTAL) {
                                                         if (this.switchSetting.getValue().equalsIgnoreCase("Normal")
                                                            && this.swapBack.getValue()
                                                            && this.cf == -1) {
                                                            this.cf = var142;
                                                         }

                                                         InventoryUtil.a_bool_String_int_int(this.switchSetting.getValue(), var143, var142);
                                                         var7x = true;
                                                      }

                                                      this.e_void_BlockPos(var144);
                                                      int var9x = this.multiPlace.getValue().intValue() - var145 - 1;

                                                      for (BlockPos var11x : (Iterable<BlockPos>)(Object)var146) {
                                                         int var12 = var9x;
                                                         var9x += -1;
                                                         if (var12 <= 0) {
                                                            break;
                                                         }

                                                         BlockPos var42x = var11x;
                                                         if (this.g_bool_BlockPos(var42x)) {
                                                            if (this.rotate.getValue().equalsIgnoreCase("Packet")
                                                               || this.rotate.getValue().equalsIgnoreCase("Silent")) {
                                                               BlockPos var43x = var11x;
                                                               float[] var44x = RotationMathUtil.a(this.h_Vec3_BlockPos(var43x));
                                                               this.a_void_floatArr(var44x);
                                                            }

                                                            BlockPos var45x = var11x;
                                                            this.e_void_BlockPos(var45x);
                                                         }
                                                      }

                                                      if (var7x) {
                                                         InventoryUtil.b(this.switchSetting.getValue(), var143, var142);
                                                      }

                                                      if (this.godSync.getValue() && !this.y.getValue()) {
                                                         boolean var13x;
                                                         if (this.antiKick.getValue()
                                                            && (
                                                               mc.player.getMainHandItem().getItem() instanceof ExperienceBottleItem
                                                                  || mc.player.getOffhandItem().getItem() instanceof ExperienceBottleItem
                                                                  || ((KeyAction)Night.moduleManager.getModule(KeyAction.class)).v()
                                                            )) {
                                                            var13x = false;
                                                         } else {
                                                            var13x = true;
                                                         }

                                                         label128: {
                                                            boolean var14x = var13x;
                                                            if (this.antiKick.getValue()) {
                                                               int var15x = this.bX;
                                                               int var16x = this.kickThreshold.getValue().intValue();
                                                               if (var15x <= var16x) {
                                                                  break label128;
                                                               }
                                                            }

                                                            if (var14x) {
                                                               if (!this.fast.getValue()) {
                                                                  for (Entity var18x : (Iterable<Entity>)(Object)mc.level.entitiesForRendering()) {
                                                                     int var19x = var18x.getId();
                                                                     int var20x = this.bW;
                                                                     if (var19x > var20x) {
                                                                        int var21x = var18x.getId();
                                                                        this.bW = var21x;
                                                                     }
                                                                  }
                                                               }

                                                               int var22x = this.offset.getValue().intValue();
                                                               int var23x = 1 - var22x;

                                                               while (true) {
                                                                  int var24x = var23x;
                                                                  int var25x = this.predictions.getValue().intValue();
                                                                  if (var24x >= var25x) {
                                                                     if (this.godSwing.getValue().equals("Normal")) {
                                                                        ClientPacketListener var39x = mc.getConnection();
                                                                        Packet var40x = PacketAndRenderUtil.a_Packet_InteractionHand(InteractionHand.MAIN_HAND);
                                                                        var39x.send(var40x);
                                                                     }
                                                                     break;
                                                                  }

                                                                  ClientLevel var26x = mc.level;
                                                                  int var27 = this.bW;
                                                                  Entity var28x = var26x.getEntity(var27);
                                                                  if (var28x == null || var28x instanceof EndCrystal) {
                                                                     int var29 = this.bW;
                                                                     int var30x = var23x;
                                                                     int var31x = var29 + var30x;
                                                                     ClientPacketListener var32x = mc.getConnection();
                                                                     int var33x = var31x;
                                                                     var32x.send(new ServerboundAttackPacket(var33x));
                                                                     if (this.godSwing.getValue().equals("Strict")) {
                                                                        ClientPacketListener var34x = mc.getConnection();
                                                                        Packet var35x = PacketAndRenderUtil.a_Packet_InteractionHand(InteractionHand.MAIN_HAND);
                                                                        var34x.send(var35x);
                                                                     }

                                                                     Map var36x = this.bs;
                                                                     Integer var37x = var31x;
                                                                     Long var38x = System.currentTimeMillis();
                                                                     var36x.put(var37x, var38x);
                                                                  }

                                                                  var23x++;
                                                               }
                                                            }
                                                         }

                                                         int var41x = this.bX + 1;
                                                         this.bX = var41x;
                                                      }
                                                   };
                                                   if (var1) {
                                                      this.bn.run();
                                                      this.bn = null;
                                                      this.bH = true;
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
                  } else {
                     this.bK = null;
                     this.bI = null;
                     Night.renderPlacementManager.setCrystalTarget(null);
                  }
               } else {
                  Night.renderPlacementManager.setCrystalTarget(null);
               }
            }
         }
      }
   }

   private EndCrystal N() {
      if (!this.attack.getValue()) {
         return null;
      }

      if (this.b_bool_String("Attack")) {
         return null;
      }

      List var1 = this.R();
      List var2 = this.a_List_List(var1);
      if (var2.isEmpty()) {
         return null;
      }

      EndCrystal var3 = null;
      float var4 = 0.0F;

      for (Entity var6 : (Iterable<Entity>)(Object)mc.level.entitiesForRendering()) {
         if (var6 instanceof EndCrystal var7 && var7.isAlive()) {
            EndCrystal var8 = var7;
            if (!this.a_bool_EndCrystal(var8)) {
               if (this.inhibit.getValue()) {
                  Map var9 = this.bs;
                  Integer var10 = var6.getId();
                  if (var9.containsKey(var10)) {
                     continue;
                  }
               }

               EndCrystal var11 = var7;
               double var12 = this.a_double_Entity(var11);
               double var14 = Mth.square(this.attackRange.getValue().doubleValue());
               if (!(var12 > var14)) {
                  WorldBorder var16 = mc.level.getWorldBorder();
                  BlockPos var17 = var7.blockPosition();
                  if (var16.isWithinBounds(var17)) {
                     boolean var23;
                     label94: {
                        if (!this.raytrace.getValue()) {
                           EndCrystal var18 = var7;
                           double var19 = this.a_double_Entity(var18);
                           double var21 = Mth.square(this.attackWallsRange.getValue().doubleValue());
                           if (!(var19 > var21)) {
                              var23 = false;
                              break label94;
                           }
                        }

                        var23 = true;
                     }

                     if (!var23 || BlockInteractionUtil.a_bool_Entity(var7)) {
                        if (!((Suicide)Night.moduleManager.getModule(Suicide.class)).isEnabled()) {
                           LocalPlayer var24 = mc.player;
                           LocalPlayer var25 = mc.player;
                           int var26 = this.extrapolation.getValue().intValue();
                           AABB var27 = PositionUtil.a(var25, var26);
                           EndCrystal var28 = var7;
                           boolean var29 = this.ignoreTerrain.getValue();
                           float var30 = DamageUtil.a_float_Entity_AABB_EndCrystal_bool(var24, var27, var28, var29);
                           float var31 = this.maximumSelfDamage.getValue().floatValue();
                           if (var30 > var31) {
                              continue;
                           }

                           if (this.antiSuicide.getValue()) {
                              float var32 = var30;
                              float var33 = mc.player.getHealth();
                              float var34 = mc.player.getAbsorptionAmount();
                              float var35 = var33 + var34;
                              if (var32 > var35) {
                                 continue;
                              }
                           }
                        }

                        boolean var36 = false;
                        Iterator var37 = var2.iterator();

                        while (true) {
                           if (var37.hasNext()) {
                              Player var38 = (Player)var37.next();
                              int var39 = this.extrapolation.getValue().intValue();
                              AABB var40 = PositionUtil.a(var38, var39);
                              EndCrystal var41 = var7;
                              boolean var42 = this.ignoreTerrain.getValue();
                              float var43 = DamageUtil.a_float_Entity_AABB_EndCrystal_bool(var38, var40, var41, var42);
                              Player var44 = var38;
                              float var45 = this.minimumDamage.getValue().floatValue();
                              float var46 = this.a_float_Player_float(var44, var45);
                              if (var43 < var46) {
                                 float var47 = var43;
                                 float var48 = var38.getHealth();
                                 float var49 = var38.getAbsorptionAmount();
                                 float var50 = var48 + var49;
                                 if (var47 < var50) {
                                    float var51 = var43;
                                    float var52 = this.lethalMultiplier.getValue().floatValue();
                                    float var53 = 1.0F + var52;
                                    float var54 = var51 * var53;
                                    float var55 = var38.getHealth();
                                    float var56 = var38.getAbsorptionAmount();
                                    float var57 = var55 + var56;
                                    if (!(var54 >= var57)) {
                                       continue;
                                    }
                                 }
                              }

                              float var58 = var43;
                              float var59 = var4;
                              if (!(var58 > var59)) {
                                 float var60 = var43;
                                 float var61 = var38.getHealth();
                                 float var62 = var38.getAbsorptionAmount();
                                 float var63 = var61 + var62;
                                 if (!(var60 > var63)) {
                                    continue;
                                 }
                              }

                              var3 = var7;
                              var4 = var43;
                              float var64 = var43;
                              float var65 = var38.getHealth();
                              float var66 = var38.getAbsorptionAmount();
                              float var67 = var65 + var66;
                              if (!(var64 > var67)) {
                                 continue;
                              }

                              var36 = true;
                           }

                           if (var36) {
                              return var3;
                           }
                           break;
                        }
                     }
                  }
               }
            }
         }
      }

      return var3;
   }

   private AutoCrystal.PlaceTarget d_PlaceTarget_BlockPos(BlockPos var1) {
      Set var2 = Night.worldStateManager.getPlacedPositions();
      return this.a_PlaceTarget_BlockPos_Set(var1, var2);
   }

   private AutoCrystal.PlaceTarget a_PlaceTarget_BlockPos_Set(BlockPos var1, Set<BlockPos> var2) {
      if (var1 == null) {
         return this.a_PlaceTarget_BlockPos_Set_bool(null, var2, false);
      }

      AutoCrystal.PlaceTarget var3 = this.a_PlaceTarget_BlockPos_Set_bool(var1, var2, true);
      return var3 != null && var3.b() != null ? var3 : this.a_PlaceTarget_BlockPos_Set_bool(var1, var2, false);
   }

   private AutoCrystal.PlaceTarget a_PlaceTarget_BlockPos_Set_bool(BlockPos var1, Set<BlockPos> var2, boolean var3) {
      boolean var4;
      if (var1 == null) {
         var4 = true;
      } else {
         var4 = false;
      }

      boolean var5 = var4;
      if (!this.place.getValue()) {
         if (var5) {
            this.cb = "PLACE_OFF";
         }

         return null;
      } else if (this.b_bool_String("Place")) {
         if (var5) {
            this.cb = "PAUSED";
         }

         return null;
      } else if ((this.switchSetting.getValue().equalsIgnoreCase("None") || InventoryUtil.b_int_Item(Items.END_CRYSTAL) == -1)
         && mc.player.getMainHandItem().getItem() != Items.END_CRYSTAL
         && mc.player.getOffhandItem().getItem() != Items.END_CRYSTAL) {
         if (var5) {
            this.cb = "NO_CRYSTAL";
         }

         return null;
      } else {
         List var6 = this.R();
         List var7 = this.a_List_List(var6);
         if (var7.isEmpty()) {
            if (var5) {
               this.cb = "NO_PLAYERS";
            }

            return null;
         } else {
            BlockPos var8 = null;
            Player var9 = null;
            float var10 = 0.0F;
            float var11 = 0.0F;
            BlockPos var12;
            if (this.bK == null) {
               var12 = null;
            } else {
               var12 = this.bK.b();
            }

            BlockPos var13 = var12;
            float var14 = 0.5F;
            ArrayList var15 = new ArrayList();
            int var16 = 0;
            int var17 = this.multiPlace.getValue().intValue();
            ArrayList var18;
            if (var5 && var17 > 1) {
               var18 = new ArrayList();
            } else {
               var18 = null;
            }

            ArrayList var19 = var18;
            int var20 = 0;

            while (true) {
               int var21 = var20;
               double var22 = this.placeRange.getValue().doubleValue();
               double var24 = this.placeWallsRange.getValue().doubleValue();
               double var26 = Math.max(var22, var24);
               int var28 = Night.worldStateManager.getSpherePointCount(var26);
               if (var21 >= var28) {
                  if (var5) {
                     ArrayList var178 = var19;
                     BlockPos var179 = var8;
                     int var180 = var17 - 1;
                     List var181 = this.a_List_List_BlockPos_int(var178, var179, var180);
                     this.cj = var181;
                  }

                  if (var8 == null) {
                     if (var5) {
                        String var182;
                        if (var16 == 0) {
                           var182 = "NO_SPOT";
                        } else {
                           var182 = "NO_DAMAGE";
                        }

                        this.cb = var182;
                     }

                     ArrayList var183 = var15;
                     int var184 = var16;
                     return new AutoCrystal.PlaceTarget(null, null, var183, null, 0.0F, var184);
                  }

                  if (var5) {
                     this.cb = "OK";
                  }

                  BlockPos var185 = var8;
                  Player var186 = var9;
                  ArrayList var187 = var15;
                  float var188 = var11;
                  int var189 = var16;
                  return new AutoCrystal.PlaceTarget(var185, var186, var187, var1, var188, var189);
               }

               BlockPos var29 = mc.player.blockPosition();
               int var30 = var20;
               Vec3i var31 = Night.worldStateManager.getSphereOffset(var30);
               BlockPos var32 = var29.offset(var31);
               Vec3 var33 = mc.player.getEyePosition();
               Vec3 var34 = Vec3.atCenterOf(var32);
               double var35 = var33.distanceToSqr(var34);
               double var37 = Mth.square(this.placeRange.getValue().doubleValue());
               label178:
               if (!(var35 > var37)) {
                  if (var3) {
                     int var39 = var32.getX();
                     int var40 = var1.getX();
                     if (Math.abs(var39 - var40) > 1) {
                        break label178;
                     }

                     int var41 = var32.getY();
                     int var42 = var1.getY();
                     if (Math.abs(var41 - var42) > 1) {
                        break label178;
                     }

                     int var43 = var32.getZ();
                     int var44 = var1.getZ();
                     if (Math.abs(var43 - var44) > 1) {
                        break label178;
                     }
                  }

                  WorldBorder var45 = mc.level.getWorldBorder();
                  BlockPos var46 = var32;
                  label172:
                  if (var45.isWithinBounds(var46)) {
                     ClientLevel var47 = mc.level;
                     BlockPos var48 = var32;
                     if (var47.getBlockState(var48).getBlock() != Blocks.OBSIDIAN) {
                        ClientLevel var49 = mc.level;
                        BlockPos var50 = var32;
                        if (var49.getBlockState(var50).getBlock() != Blocks.BEDROCK) {
                           break label172;
                        }
                     }

                     ClientLevel var51 = mc.level;
                     BlockPos var52 = var32.offset(0, 1, 0);
                     label169:
                     if (var51.getBlockState(var52).isAir()) {
                        if (this.placements.getValue().equalsIgnoreCase("Protocol")) {
                           ClientLevel var53 = mc.level;
                           BlockPos var54 = var32.offset(0, 2, 0);
                           if (!var53.getBlockState(var54).isAir()) {
                              break label169;
                           }
                        }

                        boolean var61;
                        label213: {
                           if (!this.raytrace.getValue()) {
                              Vec3 var55 = mc.player.getEyePosition();
                              Vec3 var56 = Vec3.atCenterOf(var32);
                              double var57 = var55.distanceToSqr(var56);
                              double var59 = Mth.square(this.placeWallsRange.getValue().doubleValue());
                              if (!(var57 > var59)) {
                                 var61 = false;
                                 break label213;
                              }
                           }

                           var61 = true;
                        }

                        if (!var61 || BlockInteractionUtil.f(var32)) {
                           double var62 = var32.getX() - 1;
                           double var64 = var32.getY() + 1;
                           double var66 = var32.getZ() - 1;
                           double var68 = var32.getX() + 2;
                           double var70 = var32.getY() + 3;
                           double var72 = var32.getZ() + 2;
                           AABB var74 = new AABB(var62, var64, var66, var68, var70, var72);
                           if (!var2.stream().anyMatch(var1x -> var74.intersects(new AABB(var1x)))) {
                              ClientLevel var75 = mc.level;
                              Entity var76 = (Entity)null;
                              BlockPos var77 = var32;
                              AABB var78 = this.j_AABB_BlockPos(var77);
                              List var79 = var75.getEntities(var76, var78, var0 -> {
                                 boolean var1x;
                                 if (var0.isAlive() && !(var0 instanceof ExperienceOrb)) {
                                    var1x = true;
                                 } else {
                                    var1x = false;
                                 }

                                 return var1x;
                              });
                              boolean var80 = false;
                              ArrayList var81 = new ArrayList();

                              for (Entity var83 : (Iterable<Entity>)(Object)var79) {
                                 if (!(var83 instanceof EndCrystal var84)) {
                                    var80 = true;
                                    break;
                                 }

                                 if (this.a_bool_EndCrystal(var84)) {
                                    var80 = true;
                                    break;
                                 }

                                 BlockPos var85 = var84.blockPosition().below();
                                 Map var86 = this.bt;
                                 BlockPos var87 = var85;
                                 if (!var86.containsKey(var87)) {
                                    double var88 = var84.getX();
                                    double var90 = var84.getY();
                                    double var92 = 0.5;
                                    double var94 = var90 - var92;
                                    double var96 = var84.getZ();
                                    var85 = BlockPos.containing(var88, var94, var96);
                                 }

                                 Map var98 = this.bt;
                                 BlockPos var99 = var85;
                                 if (var98.containsKey(var99)) {
                                    int var100 = var84.tickCount;
                                    int var101 = this.attackSpeed.getValue().intValue();
                                    int var102 = 20 - var101 + 15;
                                    if (var100 < var102) {
                                       continue;
                                    }
                                 }

                                 ArrayList var103 = var81;
                                 EndCrystal var104 = var84;
                                 var103.add(var104);
                              }

                              if (!var80) {
                                 double var105 = Mth.square(10.0);
                                 boolean var107 = false;

                                 for (Player var109 : (Iterable<Player>)(Object)var7) {
                                    double var110 = var32.getX();
                                    double var112 = 0.5;
                                    double var114 = var110 + var112;
                                    double var116 = var32.getY();
                                    double var118 = 1.0;
                                    double var120 = var116 + var118;
                                    double var122 = var32.getZ();
                                    double var124 = 0.5;
                                    double var126 = var122 + var124;
                                    double var128 = var109.distanceToSqr(var114, var120, var126);
                                    double var130 = var105;
                                    if (var128 <= var130) {
                                       var107 = true;
                                       break;
                                    }
                                 }

                                 label158:
                                 if (var107) {
                                    if (!((Suicide)Night.moduleManager.getModule(Suicide.class)).isEnabled()) {
                                       LocalPlayer var132 = mc.player;
                                       LocalPlayer var133 = mc.player;
                                       int var134 = this.extrapolation.getValue().intValue();
                                       AABB var135 = PositionUtil.a(var133, var134);
                                       BlockPos var136 = var32;
                                       boolean var137 = this.ignoreTerrain.getValue();
                                       float var138 = DamageUtil.a(var132, var135, var136, var1, var137);
                                       float var139 = this.maximumSelfDamage.getValue().floatValue();
                                       if (var138 > var139) {
                                          break label158;
                                       }

                                       if (this.antiSuicide.getValue()) {
                                          float var140 = var138;
                                          float var141 = mc.player.getHealth();
                                          float var142 = mc.player.getAbsorptionAmount();
                                          float var143 = var141 + var142;
                                          if (var140 > var143) {
                                             break label158;
                                          }
                                       }
                                    }

                                    for (Player var145 : (Iterable<Player>)(Object)var7) {
                                       var16++;
                                       Player var146 = var145;
                                       Player var147 = var145;
                                       int var148 = this.extrapolation.getValue().intValue();
                                       AABB var149 = PositionUtil.a(var147, var148);
                                       BlockPos var150 = var32;
                                       boolean var151 = this.ignoreTerrain.getValue();
                                       float var152 = DamageUtil.a(var146, var149, var150, var1, var151);
                                       Player var153 = var145;
                                       float var154 = this.minimumDamage.getValue().floatValue();
                                       float var155 = this.a_float_Player_float(var153, var154);
                                       if (var152 < var155) {
                                          float var156 = var152;
                                          float var157 = var145.getHealth();
                                          float var158 = var145.getAbsorptionAmount();
                                          float var159 = var157 + var158;
                                          if (var156 < var159) {
                                             float var160 = var152;
                                             float var161 = this.lethalMultiplier.getValue().floatValue();
                                             float var162 = 1.0F + var161;
                                             float var163 = var160 * var162;
                                             float var164 = var145.getHealth();
                                             float var165 = var145.getAbsorptionAmount();
                                             float var166 = var164 + var165;
                                             if (!(var163 >= var166)) {
                                                continue;
                                             }
                                          }
                                       }

                                       if (!(var152 <= 0.0F)) {
                                          if (var1 == null && !var81.isEmpty()) {
                                             ArrayList var167 = var15;
                                             Entity var168 = (Entity)var81.getFirst();
                                             var167.add(var168);
                                             break;
                                          }

                                          if (var19 != null) {
                                             ArrayList var169 = var19;
                                             BlockPos var170 = var32;
                                             float var171 = var152;
                                             var169.add(new AutoCrystal.DamageResult(var170, var171));
                                          }

                                          float var172 = var152;
                                          BlockPos var173 = var32;
                                          BlockPos var174 = var13;
                                          float var175;
                                          if (var173.equals(var174)) {
                                             var175 = 0.5F;
                                          } else {
                                             var175 = 0.0F;
                                          }

                                          float var176 = var172 + var175;
                                          float var177 = var10;
                                          if (var176 > var177) {
                                             var8 = var32;
                                             var9 = var145;
                                             var10 = var176;
                                             var11 = var152;
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

               var20++;
            }
         }
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

   private void b_void_EndCrystal(EndCrystal var1) {
      if (var1 != null && var1.isAlive() && !var1.isRemoved()) {
         if (!this.C()) {
            if (!this.a_bool_EndCrystal(var1)) {
               if (!((Suicide)Night.moduleManager.getModule(Suicide.class)).isEnabled()) {
                  LocalPlayer var2 = mc.player;
                  LocalPlayer var3 = mc.player;
                  int var4 = this.extrapolation.getValue().intValue();
                  AABB var5 = PositionUtil.a(var3, var4);
                  boolean var6 = this.ignoreTerrain.getValue();
                  float var7 = DamageUtil.a_float_Entity_AABB_EndCrystal_bool(var2, var5, var1, var6);
                  float var8 = this.maximumSelfDamage.getValue().floatValue();
                  if (var7 > var8) {
                     return;
                  }

                  if (this.antiSuicide.getValue()) {
                     float var9 = var7;
                     float var10 = mc.player.getHealth();
                     float var11 = mc.player.getAbsorptionAmount();
                     float var12 = var10 + var11;
                     if (var9 > var12) {
                        return;
                     }
                  }
               }

               int var13 = mc.player.getInventory().getSelectedSlot();
               int var14 = -1;
               if (!this.antiWeakness.getValue().equalsIgnoreCase("None") && mc.player.hasEffect(MobEffects.WEAKNESS)) {
                  int var15 = InventoryUtil.c(InventoryUtil.c, InventoryUtil.d);
                  if (var15 != -1) {
                     String var16 = this.antiWeakness.getValue();
                     int var17 = var15;
                     int var18 = var13;
                     InventoryUtil.a_bool_String_int_int(var16, var17, var18);
                     var14 = var15;
                  }
               }

               if (this.y.getValue()) {
                  RotationJitterUtil var19 = this.bd;
                  double var20 = this.z.getValue().doubleValue();
                  if (var19.a_bool_double(var20)) {
                     ClientPacketListener var22 = mc.getConnection();
                     Packet var23 = PacketAndRenderUtil.a_Packet_InteractionHand(InteractionHand.MAIN_HAND);
                     var22.send(var23);
                     if (var14 != -1) {
                        String var24 = this.antiWeakness.getValue();
                        int var25 = var14;
                        int var26 = var13;
                        InventoryUtil.b(var24, var25, var26);
                     }

                     this.bv.reset();
                     this.A();
                     return;
                  }
               }

               ClientPacketListener var27 = mc.getConnection();
               int var28 = var1.getId();
               var27.send(new ServerboundAttackPacket(var28));
               ClientPacketListener var29 = mc.getConnection();
               Packet var30 = PacketAndRenderUtil.a_Packet_InteractionHand(InteractionHand.MAIN_HAND);
               var29.send(var30);
               if (var14 != -1) {
                  String var31 = this.antiWeakness.getValue();
                  int var32 = var14;
                  int var33 = var13;
                  InventoryUtil.b(var31, var32, var33);
               }

               Map var34 = this.bs;
               Integer var35 = var1.getId();
               Long var36 = System.currentTimeMillis();
               var34.put(var35, var36);
               this.bv.reset();
               this.A();
               this.D();
               long var37 = this.bD;
               long var39 = 1L;
               long var41 = var37 + var39;
               this.bD = var41;
            }
         }
      }
   }

   private void e_void_BlockPos(BlockPos var1) {
      if (!this.C()) {
         InteractionHand var2;
         if (mc.player.getOffhandItem().getItem() == Items.END_CRYSTAL) {
            var2 = InteractionHand.OFF_HAND;
         } else {
            var2 = InteractionHand.MAIN_HAND;
         }

         InteractionHand var3 = var2;
         HitCandidate var4 = CrystalRaytraceHelper.a(var1);
         if (this.y.getValue()) {
            RotationJitterUtil var5 = this.bd;
            double var6 = this.z.getValue().doubleValue();
            if (var5.a_bool_double(var6)) {
               ClientPacketListener var8 = mc.getConnection();
               Packet var9 = PacketAndRenderUtil.a_Packet_InteractionHand(var3);
               var8.send(var9);
               this.bd.a_void_BlockPos(var1);
               this.bw.reset();
               this.B();
               return;
            }
         }

         Vec3 var10 = this.h_Vec3_BlockPos(var1);
         this.bd.a_void_BlockPos(var1);
         InteractionHand var11 = var3;
         Vec3 var12 = var10;
         HitCandidate var13 = var4;
         PacketPredictionUtil.a_void_SequencedPacketCreator(var4x -> {
            Direction var6x = var13.a;
            BlockHitResult var5x = new BlockHitResult(var12, var6x, var1, false);
            return new ServerboundUseItemOnPacket(var11, var5x, var4x);
         });
         switch (this.swing.getValue()) {
            case "Default":
               LocalPlayer var18 = mc.player;
               InteractionHand var19 = var3;
               PacketAndRenderUtil.a(var18, var19);
               break;
            case "Packet":
               ClientPacketListener var16 = mc.getConnection();
               Packet var17 = PacketAndRenderUtil.a_Packet_InteractionHand(var3);
               var16.send(var17);
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

         Map var20 = this.bt;
         Long var21 = System.currentTimeMillis();
         var20.put(var1, var21);
         Map var22 = this.bu;
         Long var23 = System.currentTimeMillis();
         var22.put(var1, var23);
         this.bw.reset();
         this.B();
         this.D();
         long var24 = this.bC;
         long var26 = 1L;
         long var28 = var24 + var26;
         this.bC = var28;
      }
   }

   private void O() {
      if (!this.mineIgnore.getValue()) {
         this.P();
      } else {
         MineTask var1 = ((SpeedMine)Night.moduleManager.getModule(SpeedMine.class)).v();
         if (var1 != null && var1.e()) {
            BlockPos var2 = var1.a();
            BlockPos var3 = this.bN;
            if (!var2.equals(var3)) {
               this.P();
               BlockPos var4 = var2;
               this.bN = var4;
            }

            ClientLevel var5 = mc.level;
            BlockPos var6 = var2;
            if (var5.getBlockState(var6).canBeReplaced()) {
               long var7 = 0L;
               this.bP = var7;
            }

            if (this.bO == null) {
               int var9 = var1.o();
               int var10 = this.mineIgnoreTicks.getValue().intValue();
               if (var9 <= var10) {
                  BlockPos var11 = var2;
                  this.f_void_BlockPos(var11);
               }
            }
         } else {
            this.P();
         }
      }
   }

   private void P() {
      this.bN = null;
      this.bO = null;
      long var1 = 0L;
      this.bP = var1;
      this.bQ = false;
   }

   public boolean b_bool_BlockPos(BlockPos var1) {
      if (this.mineIgnore.getValue() && var1 != null && mc.level != null) {
         if (this.bO == null) {
            BlockPos var2 = this.bN;
            if (var1.equals(var2)) {
               if (mc.level.getBlockState(var1).canBeReplaced()) {
                  return false;
               }

               long var3 = System.currentTimeMillis();
               long var5 = this.bP;
               long var7 = 0L;
               if (var5 == var7) {
                  long var9 = var3;
                  this.bP = var9;
                  this.bQ = false;
               }

               if (this.bQ) {
                  return false;
               }

               long var11 = var3;
               long var13 = this.bP;
               long var15 = var11 - var13;
               long var17 = 100L;
               long var19 = this.mineIgnoreTicks.getValue().intValue();
               long var21 = 50L;
               long var23 = var19 * var21;
               long var25 = Math.max(var17, var23);
               boolean var27;
               if (var15 < var25) {
                  var27 = true;
               } else {
                  var27 = false;
               }

               return var27;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private void f_void_BlockPos(BlockPos var1) {
      this.bQ = false;
      if (this.b_bool_String("Place")) {
         this.bQ = true;
      } else if (!this.C()) {
         TimerUtil var2 = this.bw;
         Float var3 = this.z();
         if (var2.passedMs(var3)) {
            List var4 = this.R();
            List var5 = this.a_List_List(var4);
            ArrayList<Player> var6 = new ArrayList<>();

            for (Player var8 : (Iterable<Player>)(Object)var5) {
               LocalPlayer var9 = mc.player;
               if (!var8.equals(var9)) {
                  BlockPos var10 = var8.blockPosition();
                  int var11 = var1.getX();
                  int var12 = var10.getX();
                  if (Math.abs(var11 - var12) <= 1) {
                     int var13 = var1.getZ();
                     int var14 = var10.getZ();
                     if (Math.abs(var13 - var14) <= 1) {
                        int var15 = var1.getY();
                        int var16 = var10.getY();
                        if (Math.abs(var15 - var16) <= 1) {
                           ArrayList var17 = var6;
                           Player var18 = var8;
                           var17.add(var18);
                        }
                     }
                  }
               }
            }

            if (var6.isEmpty()) {
               this.bQ = true;
            } else {
               int var19 = InventoryUtil.b_int_Item(Items.END_CRYSTAL);
               int var20 = mc.player.getInventory().getSelectedSlot();
               if (!this.switchSetting.getValue().equalsIgnoreCase("None")
                  && var19 == -1
                  && mc.player.getMainHandItem().getItem() != Items.END_CRYSTAL
                  && mc.player.getOffhandItem().getItem() != Items.END_CRYSTAL) {
                  this.bQ = true;
               } else {
                  BlockPos var21 = null;
                  List var22 = var5;
                  if (this.a_float_BlockPos_BlockPos_List(var1, var1, var22) >= 0.0F) {
                     var21 = var1;
                  } else {
                     BlockPos var23 = var1.below();
                     List var24 = var5;
                     if (this.a_float_BlockPos_BlockPos_List(var23, var1, var24) >= 0.0F) {
                        var21 = var1.below();
                     } else {
                        float var25 = -1.0F;

                        for (Direction var27 : Plane.HORIZONTAL) {
                           BlockPos var28 = var1.relative(var27);
                           if (!var6.stream().anyMatch(var1x -> {
                              int var2x = var1x.blockPosition().getX();
                              int var3x = var28.getX();
                              if (var2x == var3x) {
                                 int var4x = var1x.blockPosition().getZ();
                                 int var5x = var28.getZ();
                                 if (var4x == var5x) {
                                    return true;
                                 }
                              }

                              return false;
                           })) {
                              BlockPos var29 = var28.below();
                              List var30 = var5;
                              float var31 = this.a_float_BlockPos_BlockPos_List(var29, var1, var30);
                              float var32 = var25;
                              if (var31 > var32) {
                                 var25 = var31;
                                 var21 = var29;
                              }
                           }
                        }
                     }
                  }

                  if (var21 == null) {
                     this.bQ = true;
                  } else {
                     CrystalRaytraceHelper.a(var21);
                     BlockPos var33 = var21;
                     Vec3 var34 = this.h_Vec3_BlockPos(var33);
                     if (this.rotate.getValue().equalsIgnoreCase("Normal")) {
                        Vec3 var35 = var34;
                        float[] var36 = this.a_floatArr_Vec3(var35);
                        int var37 = Night.rotationManager.getModulePriority(this);
                        Night.rotationManager.rotate(var36, var37);
                     }

                     if (this.rotate.getValue().equalsIgnoreCase("Packet") || this.rotate.getValue().equalsIgnoreCase("Silent")) {
                        float[] var38 = RotationMathUtil.a(var34);
                        this.a_void_floatArr(var38);
                     }

                     boolean var39 = false;
                     if (mc.player.getOffhandItem().getItem() != Items.END_CRYSTAL) {
                        if (this.switchSetting.getValue().equalsIgnoreCase("Normal") && this.swapBack.getValue() && this.cf == -1) {
                           int var40 = var20;
                           this.cf = var40;
                        }

                        String var41 = this.switchSetting.getValue();
                        int var42 = var19;
                        int var43 = var20;
                        InventoryUtil.a_bool_String_int_int(var41, var42, var43);
                        var39 = true;
                     }

                     BlockPos var44 = var21;
                     this.e_void_BlockPos(var44);
                     if (var39) {
                        String var45 = this.switchSetting.getValue();
                        int var46 = var19;
                        int var47 = var20;
                        InventoryUtil.b(var45, var46, var47);
                     }

                     BlockPos var48 = var21;
                     this.bO = var48;
                     long var49 = 0L;
                     this.bP = var49;
                  }
               }
            }
         }
      }
   }

   private float a_float_BlockPos_BlockPos_List(BlockPos var1, BlockPos var2, List<Player> var3) {
      net.minecraft.world.level.block.Block var4 = mc.level.getBlockState(var1).getBlock();
      if (var4 != Blocks.OBSIDIAN && var4 != Blocks.BEDROCK) {
         return -1.0F;
      }

      BlockPos var5 = var1.above();
      ClientLevel var6 = mc.level;
      BlockPos var7 = var5;
      if (!var6.getBlockState(var7).isAir()) {
         return -1.0F;
      }

      if (this.placements.getValue().equalsIgnoreCase("Protocol")) {
         ClientLevel var8 = mc.level;
         BlockPos var9 = var5.above();
         if (!var8.getBlockState(var9).isAir()) {
            return -1.0F;
         }
      }

      Vec3 var10 = mc.player.getEyePosition();
      Vec3 var11 = Vec3.atCenterOf(var1);
      double var12 = var10.distanceToSqr(var11);
      double var14 = Mth.square(this.placeRange.getValue().doubleValue());
      if (var12 > var14) {
         return -1.0F;
      }

      if (!mc.level.getWorldBorder().isWithinBounds(var1)) {
         return -1.0F;
      }

      label64: {
         if (!this.raytrace.getValue()) {
            double var16 = var12;
            double var18 = Mth.square(this.placeWallsRange.getValue().doubleValue());
            if (!(var16 > var18)) {
               break label64;
            }
         }

         if (!BlockInteractionUtil.f(var1)) {
            return -1.0F;
         }
      }

      double var20 = var5.getX();
      double var22 = var5.getY();
      double var24 = var5.getZ();
      double var26 = var5.getX() + 1;
      double var28 = var5.getY() + 2;
      double var30 = var5.getZ() + 1;
      AABB var32 = new AABB(var20, var22, var24, var26, var28, var30);
      ClientLevel var33 = mc.level;
      Entity var34 = (Entity)null;
      AABB var35 = var32;
      if (!var33.getEntities(var34, var35, var0 -> {
         boolean var1x;
         if (var0.isAlive() && !(var0 instanceof ExperienceOrb) && !(var0 instanceof EndCrystal)) {
            var1x = true;
         } else {
            var1x = false;
         }

         return var1x;
      }).isEmpty()) {
         return -1.0F;
      }

      double var36 = var1.getX() - 1;
      double var38 = var1.getY() + 1;
      double var40 = var1.getZ() - 1;
      double var42 = var1.getX() + 2;
      double var44 = var1.getY() + 3;
      double var46 = var1.getZ() + 2;
      AABB var48 = new AABB(var36, var38, var40, var42, var44, var46);
      if (Night.worldStateManager.getPlacedPositions().stream().anyMatch(var1x -> var48.intersects(new AABB(var1x)))) {
         return -1.0F;
      }

      if (!((Suicide)Night.moduleManager.getModule(Suicide.class)).isEnabled()) {
         LocalPlayer var49 = mc.player;
         boolean var50 = this.ignoreTerrain.getValue();
         float var51 = DamageUtil.a(var49, null, var1, null, var50);
         float var52 = this.maximumSelfDamage.getValue().floatValue();
         if (var51 > var52) {
            return -1.0F;
         }

         if (this.antiSuicide.getValue()) {
            float var53 = var51;
            float var54 = mc.player.getHealth();
            float var55 = mc.player.getAbsorptionAmount();
            float var56 = var54 + var55;
            if (var53 > var56) {
               return -1.0F;
            }
         }
      }

      float var57 = -1.0F;

      for (Player var59 : (Iterable<Player>)(Object)var3) {
         LocalPlayer var60 = mc.player;
         if (!var59.equals(var60)) {
            Player var61 = var59;
            Player var62 = var59;
            int var63 = this.extrapolation.getValue().intValue();
            AABB var64 = PositionUtil.a(var62, var63);
            boolean var65 = this.ignoreTerrain.getValue();
            float var66 = DamageUtil.a(var61, var64, var1, var2, var65);
            Player var67 = var59;
            float var68 = this.minimumDamage.getValue().floatValue();
            float var69 = this.a_float_Player_float(var67, var68);
            if (var66 >= var69) {
               float var70 = var66;
               float var71 = var57;
               if (var70 > var71) {
                  var57 = var66;
               }
            }
         }
      }

      return var57;
   }

   private void Q() {
      if (!this.b_bool_String("Attack")) {
         BlockPos var1 = this.bO;
         this.P();
         if (var1 != null) {
            if (this.attack.getValue()) {
               for (Entity var3 : (Iterable<Entity>)(Object)mc.level.entitiesForRendering()) {
                  if (var3 instanceof EndCrystal var4 && var4.isAlive()) {
                     BlockPos var5 = var4.blockPosition().below();
                     BlockPos var6 = var1;
                     if (var5.equals(var6)) {
                        if (this.rotate.getValue().equalsIgnoreCase("Normal")) {
                           EndCrystal var7 = var4;
                           Vec3 var8 = this.b_Vec3_Entity(var7);
                           float[] var9 = this.a_floatArr_Vec3(var8);
                           int var10 = Night.rotationManager.getModulePriority(this);
                           Night.rotationManager.rotate(var9, var10);
                        }

                        if (this.rotate.getValue().equalsIgnoreCase("Packet") || this.rotate.getValue().equalsIgnoreCase("Silent")) {
                           EndCrystal var11 = var4;
                           float[] var12 = RotationMathUtil.a(this.b_Vec3_Entity(var11));
                           this.a_void_floatArr(var12);
                        }

                        EndCrystal var13 = var4;
                        this.b_void_EndCrystal(var13);
                        return;
                     }
                  }
               }
            }
         }
      }
   }

   private List<Player> R() {
      if (((Suicide)Night.moduleManager.getModule(Suicide.class)).isEnabled()) {
         return List.of(mc.player);
      }

      long var1 = System.currentTimeMillis();
      long var3 = this.bZ;
      long var5 = var1 - var3;
      long var7 = 50L;
      if (var5 < var7) {
         return this.bY;
      }

      ArrayList var9 = new ArrayList();

      for (Player var11 : (Iterable<Player>)(Object)mc.level.players()) {
         LocalPlayer var12 = mc.player;
         if (var11 != var12 && !EntityUtil.c_bool_Entity(var11) && var11.isAlive()) {
            LocalPlayer var13 = mc.player;
            Player var14 = var11;
            double var15 = var13.distanceToSqr(var14);
            double var17 = Mth.square(this.enemyRange.getValue().doubleValue());
            if (!(var15 > var17)) {
               String var19 = var11.getName().getString();
               if (!Night.friendManager.isFriend(var19)) {
                  boolean var20;
                  if (var11.getMainHandItem().getItem() != Items.END_CRYSTAL && var11.getOffhandItem().getItem() != Items.END_CRYSTAL) {
                     var20 = false;
                  } else {
                     var20 = true;
                  }

                  boolean var21 = var20;
                  if (!this.ignoreNaked.getValue() || !EntityUtil.b_bool_Player(var11) || var21) {
                     ArrayList var22 = var9;
                     Player var23 = var11;
                     var22.add(var23);
                  }
               }
            }
         }
      }

      ArrayList var24 = var9;
      this.bY = var24;
      long var25 = var1;
      this.bZ = var25;
      return var9;
   }

   private List<Player> a_List_List(List<Player> var1) {
      if (var1.size() > 1 && !this.target.getValue().equalsIgnoreCase("All")) {
         Player var2 = null;
         double var3 = 0.0;

         for (Player var6 : var1) {
            double var11;
            switch (this.target.getValue()) {
               case "Nearest":
                  LocalPlayer var13 = mc.player;
                  Player var14 = var6;
                  var11 = -var13.distanceToSqr(var14);
                  break;
               case "Farthest":
                  LocalPlayer var9 = mc.player;
                  Player var10 = var6;
                  var11 = var9.distanceToSqr(var10);
                  break;
               default:
                  float var15 = var6.getHealth();
                  float var16 = var6.getAbsorptionAmount();
                  float var17 = var15 + var16;
                  var11 = 0.0F - var17;
            }

            double var18 = var11;
            if (var2 != null) {
               double var20 = var18;
               double var22 = var3;
               if (!(var20 > var22)) {
                  continue;
               }
            }

            var2 = var6;
            var3 = var18;
         }

         return List.of(var2);
      } else {
         return var1;
      }
   }

   private boolean b_bool_String(String var1) {
      if (this.K()) {
         return true;
      }

      if (this.L()) {
         return true;
      }

      if (this.awaitMine.getValue() && this.J()) {
         return true;
      }

      boolean var2;
      if (!this.whileEating.getValue().equalsIgnoreCase("None")
         && (!var1.equalsIgnoreCase("Attack") || !this.whileEating.getValue().equalsIgnoreCase("Place"))
         && (!var1.equalsIgnoreCase("Place") || !this.whileEating.getValue().equalsIgnoreCase("Attack"))) {
         var2 = false;
      } else {
         var2 = true;
      }

      boolean var3;
      if (var2 && EntityUtil.a()) {
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

      if (this.chestBreak.getValue()) {
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

      if (this.faceplaceMode.getValue().equalsIgnoreCase("None")) {
         return var2;
      }

      if (!this.faceplaceSpeed.getValue().equalsIgnoreCase("Normal")) {
         TimerUtil var17 = this.bx;
         long var18 = this.faceplaceDelay.getValue().longValue();
         long var20 = 50L;
         Long var22 = var18 * var20;
         if (!var17.passedMs(var22)) {
            return var2;
         }
      }

      if (this.faceplaceMode.getValue().equalsIgnoreCase("Always")) {
         return Math.min(var2, 2.0F);
      }

      if (this.faceplaceMode.getValue().equalsIgnoreCase("Dynamic") && this.healthPlace.getValue()) {
         float var23 = var1.getHealth();
         float var24 = var1.getAbsorptionAmount();
         float var25 = var23 + var24;
         float var26 = this.health.getValue().floatValue();
         if (var25 <= var26) {
            return Math.min(var2, 2.0F);
         }
      }

      if (this.faceplaceMode.getValue().equalsIgnoreCase("Dynamic") && this.armorPlace.getValue()) {
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

   private boolean S() {
      if (this.E()) {
         return false;
      }

      if (!this.basePlace.getValue()) {
         return false;
      }

      if (this.b_bool_String("Place")) {
         return false;
      }

      Player var1 = this.bI;
      if (var1 == null) {
         double var2 = Double.MAX_VALUE;

         for (Player var5 : (Iterable<Player>)(Object)mc.level.players()) {
            if (!EntityUtil.c_bool_Entity(var5)) {
               Player var6 = var5;
               LocalPlayer var7 = mc.player;
               if (var6 != var7 && var5.isAlive()) {
                  String var8 = var5.getName().getString();
                  if (!Night.friendManager.isFriend(var8)) {
                     LocalPlayer var9 = mc.player;
                     Player var10 = var5;
                     double var11 = var9.distanceToSqr(var10);
                     double var13 = Mth.square(this.enemyRange.getValue().doubleValue());
                     if (var11 <= var13) {
                        double var15 = var11;
                        double var17 = var2;
                        if (var15 < var17) {
                           var2 = var11;
                           var1 = var5;
                        }
                     }
                  }
               }
            }
         }
      }

      if (var1 == null) {
         return false;
      }

      AutoCrystal.PlaceTarget var19 = this.bK;
      float var20;
      if (var19 != null && var19.b() != null) {
         var20 = var19.f();
      } else {
         var20 = 0.0F;
      }

      float var21 = var20;
      float var22 = var1.getHealth();
      float var23 = var1.getAbsorptionAmount();
      float var24 = var22 + var23;
      if (var21 >= var24) {
         return false;
      }

      BlockPos var25 = PositionUtil.a_BlockPos_Entity(var1);
      double var26 = this.baseRadius.getValue().doubleValue();
      int var28 = (int)Math.ceil(var26);
      int var29 = var25.getY() - 1;
      int var30 = var25.getY() - 3;
      LinkedHashSet var31 = new LinkedHashSet();
      int var32 = var29;

      while (true) {
         int var33 = var32;
         int var34 = var30;
         if (var33 < var34) {
            BlockPos var63 = null;
            float var64 = 0.0F;
            double var65 = Double.MAX_VALUE;

            for (BlockPos var68 : (Iterable<BlockPos>)(Object)var31) {
               if (this.i_bool_BlockPos(var68)) {
                  ClientLevel var69 = mc.level;
                  BlockPos var70 = var68;
                  BlockState var71 = var69.getBlockState(var70);
                  if (var71.getBlock() != Blocks.OBSIDIAN && var71.getBlock() != Blocks.BEDROCK) {
                     if (!((Suicide)Night.moduleManager.getModule(Suicide.class)).isEnabled()) {
                        LocalPlayer var72 = mc.player;
                        LocalPlayer var73 = mc.player;
                        int var74 = this.extrapolation.getValue().intValue();
                        AABB var75 = PositionUtil.a(var73, var74);
                        BlockPos var76 = var68;
                        boolean var77 = this.ignoreTerrain.getValue();
                        float var78 = DamageUtil.a(var72, var75, var76, null, var77);
                        float var79 = this.maximumSelfDamage.getValue().floatValue();
                        if (var78 > var79) {
                           continue;
                        }

                        if (this.antiSuicide.getValue()) {
                           float var80 = var78;
                           float var81 = mc.player.getHealth();
                           float var82 = mc.player.getAbsorptionAmount();
                           float var83 = var81 + var82;
                           if (var80 > var83) {
                              continue;
                           }
                        }
                     }

                     Player var84 = var1;
                     Player var85 = var1;
                     int var86 = this.extrapolation.getValue().intValue();
                     AABB var87 = PositionUtil.a(var85, var86);
                     BlockPos var88 = var68;
                     boolean var89 = this.ignoreTerrain.getValue();
                     float var90 = DamageUtil.a(var84, var87, var88, null, var89);
                     float var91 = this.baseDamage.getValue().floatValue();
                     if (!(var90 < var91)) {
                        float var92 = var90;
                        Player var93 = var1;
                        float var94 = this.minimumDamage.getValue().floatValue();
                        float var95 = this.a_float_Player_float(var93, var94);
                        if (var92 < var95) {
                           float var96 = var90;
                           float var97 = var1.getHealth();
                           float var98 = var1.getAbsorptionAmount();
                           float var99 = var97 + var98;
                           if (var96 < var99) {
                              float var100 = var90;
                              float var101 = this.lethalMultiplier.getValue().floatValue();
                              float var102 = 1.0F + var101;
                              float var103 = var100 * var102;
                              float var104 = var1.getHealth();
                              float var105 = var1.getAbsorptionAmount();
                              float var106 = var104 + var105;
                              if (!(var103 >= var106)) {
                                 continue;
                              }
                           }
                        }

                        if (var21 > 0.0F) {
                           float var107 = var90;
                           float var108 = var21;
                           float var109 = this.baseRatio.getValue().floatValue();
                           float var110 = var108 * var109;
                           if (var107 < var110) {
                              continue;
                           }
                        }

                        double var111 = var90;
                        double var113 = -10.0;
                        double var115 = var111 * var113;
                        LocalPlayer var117 = mc.player;
                        Vec3 var118 = Vec3.atCenterOf(var68);
                        double var119 = var117.distanceToSqr(var118);
                        double var121 = var115 + var119;
                        Player var123 = var1;
                        Vec3 var124 = Vec3.atCenterOf(var68);
                        double var125 = var123.distanceToSqr(var124);
                        double var127 = var121 + var125;
                        double var129 = var65;
                        if (var127 < var129) {
                           var65 = var127;
                           var63 = var68;
                           var64 = var90;
                        }
                     }
                  }
               }
            }

            if (var63 == null) {
               return false;
            }

            int var131;
            if (this.baseSwitch.getValue().equalsIgnoreCase("None")) {
               var131 = -1;
            } else {
               Item var132 = Items.OBSIDIAN;
               int var133;
               if (!this.baseSwitch.getValue().equalsIgnoreCase("AltSwap") && !this.baseSwitch.getValue().equalsIgnoreCase("AltPickup")) {
                  var133 = InventoryUtil.d;
               } else {
                  var133 = InventoryUtil.f;
               }

               var131 = InventoryUtil.a_int_Item_int_int(var132, 0, var133);
            }

            byte var134 = (byte)var131;
            if (var134 == -1) {
               return false;
            }

            BlockPos var135 = var63;
            boolean var136 = this.baseStrictDirection.getValue();
            Direction var137 = BlockInteractionUtil.b_Direction_BlockPos_bool(var135, var136);
            if (var137 == null && !this.baseAirPlace.getValue()) {
               return false;
            }

            int var138 = mc.player.getInventory().getSelectedSlot();
            String var139 = this.baseSwitch.getValue();
            byte var140 = var134;
            int var141 = var138;
            if (!InventoryUtil.a_bool_String_int_int(var139, var140, var141)) {
               return false;
            }

            boolean var151;
            if (var137 != null) {
               BlockPos var142 = var63;
               Direction var143 = var137;
               InteractionHand var144 = InteractionHand.MAIN_HAND;
               String var145;
               if (this.baseRotate.getValue()) {
                  var145 = "Normal";
               } else {
                  var145 = "None";
               }

               boolean var150;
               if (!this.renderMode.getValue().equalsIgnoreCase("Both") && !this.renderMode.getValue().equalsIgnoreCase("Fill")) {
                  var150 = false;
               } else {
                  var150 = true;
               }

               var151 = BlockInteractionUtil.a(var142, var143, var144, var145, true, var150);
            } else {
               BlockPos var146 = var63;
               InteractionHand var147 = InteractionHand.MAIN_HAND;
               String var148;
               if (this.baseRotate.getValue()) {
                  var148 = "Normal";
               } else {
                  var148 = "None";
               }

               boolean var149;
               if (!this.renderMode.getValue().equalsIgnoreCase("Both") && !this.renderMode.getValue().equalsIgnoreCase("Fill")) {
                  var149 = false;
               } else {
                  var149 = true;
               }

               BlockInteractionUtil.a_void_BlockPos_InteractionHand_String_bool(var146, var147, var148, var149);
               var151 = true;
            }

            String var152 = this.baseSwitch.getValue();
            byte var153 = var134;
            int var154 = var138;
            InventoryUtil.b(var152, var153, var154);
            if (var151) {
               BlockPos var155 = var63;
               Player var156 = var1;
               List var157 = Collections.emptyList();
               float var158 = var64;
               this.bK = new AutoCrystal.PlaceTarget(var155, var156, var157, null, var158, 1);
               if (this.place.getValue()) {
                  int var159 = InventoryUtil.b_int_Item(Items.END_CRYSTAL);
                  int var160 = mc.player.getInventory().getSelectedSlot();
                  boolean var161;
                  if (mc.player.getOffhandItem().getItem() != Items.END_CRYSTAL
                     && mc.player.getMainHandItem().getItem() != Items.END_CRYSTAL
                     && (var159 == -1 || this.switchSetting.getValue().equalsIgnoreCase("None"))) {
                     var161 = false;
                  } else {
                     var161 = true;
                  }

                  if (var161) {
                     CrystalRaytraceHelper.a(var63);
                     BlockPos var162 = var63;
                     Vec3 var163 = this.h_Vec3_BlockPos(var162);
                     if (!this.rotate.getValue().equalsIgnoreCase("Normal") && !this.baseRotate.getValue()) {
                        if (this.rotate.getValue().equalsIgnoreCase("Packet") || this.rotate.getValue().equalsIgnoreCase("Silent")) {
                           float[] var167 = RotationMathUtil.a(var163);
                           this.a_void_floatArr(var167);
                        }
                     } else {
                        Vec3 var164 = var163;
                        float[] var165 = this.a_floatArr_Vec3(var164);
                        int var166 = Night.rotationManager.getModulePriority(this);
                        Night.rotationManager.rotate(var165, var166);
                     }

                     boolean var168 = false;
                     if (mc.player.getOffhandItem().getItem() != Items.END_CRYSTAL && var159 != -1) {
                        String var169 = this.switchSetting.getValue();
                        int var170 = var159;
                        int var171 = var160;
                        InventoryUtil.a_bool_String_int_int(var169, var170, var171);
                        var168 = true;
                     }

                     BlockPos var172 = var63;
                     this.e_void_BlockPos(var172);
                     if (var168) {
                        String var173 = this.switchSetting.getValue();
                        int var174 = var159;
                        int var175 = var160;
                        InventoryUtil.b(var173, var174, var175);
                     }
                  }
               }

               BlockPos var176 = var63;
               Night.renderPlacementManager.setCrystalTarget(var176);
            }

            return var151;
         }

         int var35 = -var28;

         while (true) {
            int var36 = var35;
            int var37 = var28;
            if (var36 > var37) {
               var32 += -1;
               break;
            }

            int var38 = -var28;

            while (true) {
               int var39 = var38;
               int var40 = var28;
               if (var39 > var40) {
                  var35++;
                  break;
               }

               int var41 = var35;
               int var42 = var35;
               int var43 = var41 * var42;
               int var44 = var38;
               int var45 = var38;
               int var46 = var44 * var45;
               double var47 = var43 + var46;
               double var49 = var26;
               double var51 = var26;
               double var53 = var49 * var51;
               if (!(var47 > var53)) {
                  LinkedHashSet var55 = var31;
                  int var56 = var25.getX();
                  int var57 = var35;
                  int var58 = var56 + var57;
                  int var59 = var32;
                  int var60 = var25.getZ();
                  int var61 = var38;
                  int var62 = var60 + var61;
                  var55.add(new BlockPos(var58, var59, var62));
               }

               var38++;
            }
         }
      }
   }

   private List<BlockPos> a_List_List_BlockPos_int(List<AutoCrystal.DamageResult> var1, BlockPos var2, int var3) {
      if (var1 != null && var2 != null && var3 > 0) {
         var1.sort((var0, var1x) -> {
            float var2x = var1x.value();
            float var3x = var0.value();
            return Float.compare(var2x, var3x);
         });
         ArrayList<BlockPos> var4 = new ArrayList<>();
         var4.add(var2);
         ArrayList var5 = new ArrayList();

         for (AutoCrystal.DamageResult var7 : var1) {
            if (var5.size() >= var3) {
               break;
            }

            BlockPos var8 = var7.pos();
            if (!var4.stream().anyMatch(var1x -> {
               int var2x = var1x.getX();
               int var3x = var8.getX();
               if (Math.abs(var2x - var3x) <= 1) {
                  int var4x = var1x.getY();
                  int var5x = var8.getY();
                  if (Math.abs(var4x - var5x) <= 1) {
                     int var6 = var1x.getZ();
                     int var7x = var8.getZ();
                     if (Math.abs(var6 - var7x) <= 1) {
                        return true;
                     }
                  }
               }

               return false;
            })) {
               ArrayList var9 = var4;
               BlockPos var10 = var8;
               var9.add(var10);
               ArrayList var11 = var5;
               BlockPos var12 = var8;
               var11.add(var12);
            }
         }

         return var5;
      } else {
         return List.of();
      }
   }

   private boolean g_bool_BlockPos(BlockPos var1) {
      if (mc.level != null && mc.player != null) {
         if (!mc.level.getWorldBorder().isWithinBounds(var1)) {
            return false;
         }

         net.minecraft.world.level.block.Block var2 = mc.level.getBlockState(var1).getBlock();
         if (var2 != Blocks.OBSIDIAN && var2 != Blocks.BEDROCK) {
            return false;
         }

         ClientLevel var3 = mc.level;
         BlockPos var4 = var1.above();
         if (!var3.getBlockState(var4).isAir()) {
            return false;
         }

         if (this.placements.getValue().equalsIgnoreCase("Protocol")) {
            ClientLevel var5 = mc.level;
            BlockPos var6 = var1.above(2);
            if (!var5.getBlockState(var6).isAir()) {
               return false;
            }
         }

         Vec3 var7 = mc.player.getEyePosition();
         Vec3 var8 = Vec3.atCenterOf(var1);
         double var9 = var7.distanceToSqr(var8);
         double var11 = Mth.square(this.placeRange.getValue().doubleValue());
         if (var9 > var11) {
            return false;
         }

         label33: {
            if (!this.raytrace.getValue()) {
               double var13 = var9;
               double var15 = Mth.square(this.placeWallsRange.getValue().doubleValue());
               if (!(var13 > var15)) {
                  break label33;
               }
            }

            if (!BlockInteractionUtil.f(var1)) {
               return false;
            }
         }

         ClientLevel var17 = mc.level;
         Entity var18 = (Entity)null;
         AABB var19 = this.j_AABB_BlockPos(var1);
         return var17.getEntities(var18, var19, var0 -> {
            boolean var1x;
            if (var0.isAlive() && !(var0 instanceof ExperienceOrb)) {
               var1x = true;
            } else {
               var1x = false;
            }

            return var1x;
         }).isEmpty();
      } else {
         return false;
      }
   }

   private Vec3 h_Vec3_BlockPos(BlockPos var1) {
      HitCandidate var2 = CrystalRaytraceHelper.a(var1);
      Vec3 var3;
      if (var2 != null && var2.b != null) {
         var3 = var2.b;
      } else {
         Vec3 var4 = Vec3.atCenterOf(var1);
         double var5 = 0.0;
         double var7 = 0.5;
         double var9 = 0.0;
         var3 = var4.add(var5, var7, var9);
      }

      Vec3 var11 = var3;
      if (this.y.getValue() && var2 != null && var2.a != null) {
         RotationJitterUtil var12 = this.bd;
         Direction var13 = var2.a;
         Vec3 var14 = var11;
         return var12.a(var1, var13, var14);
      } else {
         return var11;
      }
   }

   private Vec3 b_Vec3_Entity(Entity var1) {
      Vec3 var2;
      if (this.y.getValue()) {
         var2 = this.bd.a_Vec3_Entity(var1);
      } else {
         var2 = Vec3.atCenterOf(var1.blockPosition());
      }

      return var2;
   }

   private void a_void_floatArr(float[] var1) {
      RotationManager var2 = Night.rotationManager;
      String var3 = this.rotate.getValue();
      float[] var4;
      if (this.y.getValue()) {
         var4 = this.bd.a_floatArr_floatArr(var1);
      } else {
         var4 = var1;
      }

      var2.rotateByMode(var3, var4);
   }

   private int T() {
      int var1 = 0;

      for (Entity var3 : (Iterable<Entity>)(Object)mc.level.entitiesForRendering()) {
         if (var3 instanceof EndCrystal var4 && var4.isAlive()) {
            EndCrystal var5 = var4;
            if (this.a_bool_EndCrystal(var5)) {
               BlockPos var6 = var4.blockPosition().below();
               Map var7 = this.bt;
               BlockPos var8 = var6;
               if (!var7.containsKey(var8)) {
                  double var9 = var4.getX();
                  double var11 = var4.getY();
                  double var13 = 0.5;
                  double var15 = var11 - var13;
                  double var17 = var4.getZ();
                  var6 = BlockPos.containing(var9, var15, var17);
               }

               Map var19 = this.bt;
               BlockPos var20 = var6;
               if (var19.containsKey(var20)) {
                  var1++;
               }
            }
         }
      }

      return var1;
   }

   private boolean i_bool_BlockPos(BlockPos var1) {
      if (mc.level == null || mc.player == null) {
         return false;
      }

      if (mc.level.isOutsideBuildHeight(var1)) {
         return false;
      }

      LocalPlayer var2 = mc.player;
      Vec3 var3 = Vec3.atCenterOf(var1);
      double var4 = var2.distanceToSqr(var3);
      double var6 = Mth.square(this.baseRange.getValue().doubleValue());
      if (var4 > var6) {
         return false;
      }

      Vec3 var8 = mc.player.getEyePosition();
      Vec3 var9 = Vec3.atCenterOf(var1);
      double var10 = var8.distanceToSqr(var9);
      double var12 = Mth.square(this.placeRange.getValue().doubleValue());
      if (var10 > var12) {
         return false;
      }

      boolean var20;
      label55: {
         if (!this.raytrace.getValue()) {
            Vec3 var14 = mc.player.getEyePosition();
            Vec3 var15 = Vec3.atCenterOf(var1);
            double var16 = var14.distanceToSqr(var15);
            double var18 = Mth.square(this.placeWallsRange.getValue().doubleValue());
            if (!(var16 > var18)) {
               var20 = false;
               break label55;
            }
         }

         var20 = true;
      }

      if (var20 && !BlockInteractionUtil.f(var1)) {
         return false;
      }

      if (!mc.level.getBlockState(var1).canBeReplaced()) {
         return false;
      }

      if (!BlockInteractionUtil.c_bool_BlockPos(var1)) {
         return false;
      }

      if (!this.baseAirPlace.getValue()) {
         boolean var21 = this.baseStrictDirection.getValue();
         if (BlockInteractionUtil.b_Direction_BlockPos_bool(var1, var21) == null) {
            return false;
         }
      }

      BlockPos var22 = var1.above();
      ClientLevel var23 = mc.level;
      BlockPos var24 = var22;
      if (var23.getBlockState(var24).canBeReplaced()) {
         ClientLevel var25 = mc.level;
         BlockPos var26 = var22;
         if (var25.getFluidState(var26).isEmpty()) {
            if (this.placements.getValue().equalsIgnoreCase("Protocol")) {
               ClientLevel var27 = mc.level;
               BlockPos var28 = var1.above(2);
               if (!var27.getBlockState(var28).canBeReplaced()) {
                  return false;
               }
            }

            ClientLevel var29 = mc.level;
            Entity var30 = (Entity)null;
            AABB var31 = this.j_AABB_BlockPos(var1);
            return var29.getEntities(var30, var31, var0 -> {
               boolean var1x;
               if (var0.isAlive() && !(var0 instanceof ExperienceOrb) && !(var0 instanceof EndCrystal)) {
                  var1x = true;
               } else {
                  var1x = false;
               }

               return var1x;
            }).isEmpty();
         }
      }

      return false;
   }

   private AABB j_AABB_BlockPos(BlockPos var1) {
      BlockPos var2 = var1.offset(0, 1, 0);
      return new AABB(var2);
   }

   public float v() {
      float var1;
      if (this.bK == null) {
         var1 = 0.0F;
      } else {
         var1 = this.bK.f();
      }

      return var1;
   }

   @Generated
   public Player w() {
      return this.bI;
   }

   @Generated
   public String x() {
      return this.bT;
   }

   static {
      int var0 = be.length;
      bf = Arrays.copyOfRange(be, 1, var0);
   }

   record DamageResult(BlockPos pos, float value) {
   }

   public static class PlaceTarget {
      private BlockPos a;
      private Player b;
      private List<Entity> c;
      private BlockPos d;
      private float e;
      private int f;

      public AutoCrystal.PlaceTarget a() {
         BlockPos var1 = this.a;
         Player var2 = this.b;
         List var3 = this.c;
         BlockPos var4 = this.d;
         float var5 = this.e;
         int var6 = this.f;
         return new AutoCrystal.PlaceTarget(var1, var2, var3, var4, var5, var6);
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
