package night.module.impl.combat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.events.EventTarget;
import night.events.impl.BlockBreakProgressEvent;
import night.events.impl.ClientDisconnectEvent;
import night.events.impl.PacketReceiveEvent;
import night.events.impl.PacketSendEvent;
import night.events.impl.PlayerJumpEvent;
import night.events.impl.UpdateWalkingPlayerEvent;
import night.module.Module;
import night.module.ModuleInfo;
import night.module.impl.kingmc.AutoShop;
import night.module.impl.movement.HitboxDesync;
import night.module.impl.movement.Speed;
import night.module.impl.movement.Step;
import night.module.impl.movement.TickShift;
import night.module.impl.player.KeyAction;
import night.security.LicenseService;
import night.setting.impl.BooleanSetting;
import night.setting.impl.ModeSetting;
import night.setting.impl.NumberSetting;
import night.util.inventory.InventoryUtil;
import night.util.module.ModuleRegistryHelper;
import night.util.rotation.RotationMathUtil;
import night.util.world.BlockInteractionUtil;
import night.util.world.EntityUtil;
import night.util.world.HoleUtil;
import night.util.world.PacketAndRenderUtil;
import night.util.world.PositionUtil;

@ModuleInfo(name = "Surround", description = "Automatically places blocks at your feet to prevent crystal damage.", category = Module.Category.COMBAT)
public class Surround extends Module {
   public ModeSetting switchSetting = new ModeSetting(
      "Switch", "The mode that will be used for automatically switching to necessary items.", "Silent", InventoryUtil.a
   );
   public ModeSetting timing;
   public NumberSetting bPT;
   public NumberSetting range;
   public BooleanSetting await;
   public BooleanSetting rotate;
   public BooleanSetting timer;
   public BooleanSetting strictDirection;
   public BooleanSetting airPlace;
   public BooleanSetting attack;
   public BooleanSetting attackRotate;
   public NumberSetting attackRange;
   public NumberSetting attackAge;
   public BooleanSetting attackMultiTask;
   public BooleanSetting attackSwing;
   public BooleanSetting center;
   public BooleanSetting floor;
   public BooleanSetting underFeet;
   public BooleanSetting extension;
   public BooleanSetting x;
   public BooleanSetting y;
   public BooleanSetting z;
   public BooleanSetting edge;
   public BooleanSetting advoidHelp;
   public NumberSetting advoidHelpHealth;
   public BooleanSetting whileEating;
   public BooleanSetting advanced;
   public BooleanSetting centerOnTP;
   public BooleanSetting predict;
   private int R;
   private boolean S;
   public BooleanSetting selfDisable;
   public BooleanSetting itemDisable;
   public BooleanSetting jumpDisable;
   public BooleanSetting pearlDisable;
   public BooleanSetting chorusDisable;
   public BooleanSetting rubberbandDisable;
   public BooleanSetting stepToggle;
   public BooleanSetting speedToggle;
   public BooleanSetting tickShiftToggle;
   public BooleanSetting render;
   private Set<BlockPos> T;
   private BlockPos U;
   private int V;
   private boolean W;
   private final Map<Surround.CrystalBreakEntry, Surround.MinedBlockTracker> X;
   private static final Direction[] Y;
   private long Z;

   public Surround() {
      String[] var1 = new String[]{"Vanilla", "Sequential"};
      this.timing = new ModeSetting("Timing", "The timing that will be used in replacing broken surround blocks.", "Sequential", var1);
      Integer var2 = 4;
      Integer var3 = 1;
      Integer var4 = 8;
      this.bPT = new NumberSetting("BPT", "The maximum number of blocks to place per tick.", var2, var3, var4);
      Double var5 = 5.0;
      Double var6 = 0.0;
      Double var7 = 12.0;
      this.range = new NumberSetting("Range", "The maximum range at which the blocks will be placed at.", var5, var6, var7);
      this.await = new BooleanSetting("Await", "Waits for blocks to be registered by the client before placing on them.", false);
      this.rotate = new BooleanSetting("Rotate", "Rotates toward each block before placing it.", true);
      this.timer = new BooleanSetting("Timer", "Runs the game at 1.08x while enabled -- pays GrimAC Timer balance back between placement bursts.", false);
      this.strictDirection = new BooleanSetting("StrictDirection", "Only places using directions that face you.", false);
      this.airPlace = new BooleanSetting("AirPlace", "Places blocks in the air without needing neighboring blocks.", false);
      this.attack = new BooleanSetting("Attack", "Attacks any crystal that interferes with your surround.", false);
      BooleanSetting var9 = this.attack;
      BooleanSetting.BooleanCondition var8 = new BooleanSetting.BooleanCondition(var9, true);
      this.attackRotate = new BooleanSetting("AttackRotate", "Rotate", "Rotates toward the crystal before attacking it.", var8, true);
      BooleanSetting var11 = this.attack;
      BooleanSetting.BooleanCondition var10 = new BooleanSetting.BooleanCondition(var11, true);
      Float var12 = 3.0F;
      Float var13 = 1.0F;
      Float var14 = 6.0F;
      this.attackRange = new NumberSetting("AttackRange", "Range", "The maximum range at which crystals will be attacked.", var10, var12, var13, var14);
      BooleanSetting var16 = this.attack;
      BooleanSetting.BooleanCondition var15 = new BooleanSetting.BooleanCondition(var16, true);
      Integer var17 = 5;
      Integer var18 = 0;
      Integer var19 = 20;
      this.attackAge = new NumberSetting(
         "AttackAge", "Age", "The minimum age (in ticks) a crystal must reach before being attacked.", var15, var17, var18, var19
      );
      BooleanSetting var21 = this.attack;
      BooleanSetting.BooleanCondition var20 = new BooleanSetting.BooleanCondition(var21, true);
      this.attackMultiTask = new BooleanSetting("AttackMultiTask", "Multitask", "Allows attacking while an item is already in use.", var20, true);
      BooleanSetting var23 = this.attack;
      BooleanSetting.BooleanCondition var22 = new BooleanSetting.BooleanCondition(var23, true);
      this.attackSwing = new BooleanSetting("AttackSwing", "Swing", "Sends a swing packet whenever attacking a crystal.", var22, true);
      this.center = new BooleanSetting("Center", "Puts you in the center of the block when you surround.", false);
      this.floor = new BooleanSetting("Floor", "Places blocks under your feet as well.", true);
      BooleanSetting var24 = this.floor;
      this.underFeet = new BooleanSetting(
         "UnderFeet", "Replaces the block directly under your feet when it breaks.", new BooleanSetting.BooleanCondition(var24, true), true
      );
      this.extension = new BooleanSetting("Extension", "Extends the surround if there are entities obstructing block placement.", true);
      this.x = new BooleanSetting("Blocker", "Pre-places extra blocks where an enemy is digging toward you.", true);
      BooleanSetting var25 = this.x;
      this.y = new BooleanSetting("Up", "Blocker: places above/below the edge being mined through.", new BooleanSetting.BooleanCondition(var25, true), true);
      BooleanSetting var26 = this.x;
      this.z = new BooleanSetting(
         "Corner", "Blocker: places the 2 corner cells left/right of the edge being mined through.", new BooleanSetting.BooleanCondition(var26, true), true
      );
      BooleanSetting var27 = this.x;
      this.edge = new BooleanSetting(
         "Edge", "Blocker: places the cell straight out in front of the edge being mined through.", new BooleanSetting.BooleanCondition(var27, true), true
      );
      this.advoidHelp = new BooleanSetting("AdvoidHelp", "Skips placements that would also wall in a nearby enemy.", true);
      BooleanSetting var29 = this.advoidHelp;
      BooleanSetting.BooleanCondition var28 = new BooleanSetting.BooleanCondition(var29, true);
      Float var30 = 10.0F;
      Float var31 = 0.0F;
      Float var32 = 20.0F;
      this.advoidHelpHealth = new NumberSetting(
         "AdvoidHelpHealth", "Below this health, place every block regardless -- ignores AdvoidHelp entirely.", var28, var30, var31, var32
      );
      this.whileEating = new BooleanSetting("WhileEating", "Places blocks normally while eating.", true);
      BooleanSetting var33 = this.whileEating;
      this.advanced = new BooleanSetting(
         "Advanced",
         "While WhileEating is off, keeps placing through an eat pause as long as your surround still has an open gap (stops once sealed, or if you're stuck inside a block).",
         new BooleanSetting.BooleanCondition(var33, false),
         false
      );
      this.centerOnTP = new BooleanSetting("CenterOnTP", "Centers you if you have just teleported to surround against crystals easier.", true);
      this.predict = new BooleanSetting(
         "Predict",
         "Replaces a surround block instantly the moment we see the server's own break packet for it, instead of waiting for the next normal placement cycle.",
         true
      );
      this.R = 0;
      this.S = false;
      this.selfDisable = new BooleanSetting("SelfDisable", "Toggles off the module once it is finished with placing.", false);
      this.itemDisable = new BooleanSetting("ItemDisable", "Toggles off the module when no valid items are found in your hotbar/inventory.", true);
      this.jumpDisable = new BooleanSetting("JumpDisable", "Toggles off the module whenever your Y level changes.", true);
      this.pearlDisable = new BooleanSetting("PearlDisable", "Toggles off surround whenever you throw or land an ender pearl.", false);
      this.chorusDisable = new BooleanSetting("ChorusDisable", "Toggles off surround whenever you eat a chorus fruit.", false);
      this.rubberbandDisable = new BooleanSetting("RubberbandDisable", "Toggles off surround whenever you get rubberbanded/setback by the server.", false);
      this.stepToggle = new BooleanSetting("StepToggle", "Toggles the step module when you surround.", false);
      this.speedToggle = new BooleanSetting("SpeedToggle", "Toggles the speed module when you surround.", false);
      this.tickShiftToggle = new BooleanSetting("TickShiftToggle", "Toggles the TickShift module when you surround.", false);
      this.render = new BooleanSetting("Render", "Whether or not to render the place position.", true);
      this.T = new HashSet<>();
      this.U = null;
      this.V = 0;
      this.W = false;
      this.X = new HashMap<>();
      long var34 = 0L;
      this.Z = var34;
   }

   @Override
   public String getDisplayInfo() {
      return "Packet: " + this.R;
   }

   @Override
   public void setEnabled(boolean var1, boolean var2) {
      LicenseService var3 = Night.licenseService;
      long var4;
      if (var3 != null && var3.getRole() != null) {
         var4 = var3.getHeartbeatAgeMs();
      } else {
         var4 = Long.MAX_VALUE;
      }

      long var6 = var4;
      long var8 = 300000L;
      long var10 = var6 - var8;
      long var12 = -1L;
      int var14 = (int)((var10 ^ var12) >>> 63);
      boolean var15;
      if (!var1 && (var14 != 1 || !this.isEnabled())) {
         var15 = false;
      } else {
         var15 = true;
      }

      super.setEnabled(var15, var2);
   }

   @Override
   public void onEnable() {
      if (mc.player != null && mc.level != null) {
         this.R = 0;
         BlockPos var1 = PositionUtil.a_BlockPos_Entity(mc.player);
         this.U = var1;
         this.W = false;
         this.X.clear();
         if (this.stepToggle.getValue() && Night.moduleManager.getModule(Step.class).isEnabled()) {
            Night.moduleManager.getModule(Step.class).setEnabled(false);
         }

         if (this.speedToggle.getValue() && Night.moduleManager.getModule(Speed.class).isEnabled()) {
            Night.moduleManager.getModule(Speed.class).setEnabled(false);
         }

         if (this.tickShiftToggle.getValue() && Night.moduleManager.getModule(TickShift.class).isEnabled()) {
            Night.moduleManager.getModule(TickShift.class).setEnabled(false);
         }

         if (this.center.getValue()) {
            LocalPlayer var2 = mc.player;
            double var3 = this.U.getX();
            double var5 = 0.5;
            double var7 = var3 + var5;
            double var9 = this.U.getY();
            double var11 = this.U.getZ();
            double var13 = 0.5;
            double var15 = var11 + var13;
            var2.setPos(var7, var9, var15);
         }

         if (this.timer.getValue()) {
            ModuleRegistryHelper.a(this);
         }
      }
   }

   @Override
   public void onDisable() {
      ModuleRegistryHelper.b(this);
      this.T.clear();
      this.U = null;
      this.S = false;
      this.W = false;
      this.X.clear();
      this.V = 0;
   }

   @EventTarget
   public void a_void_BlockBreakProgressEvent(BlockBreakProgressEvent var1) {
      if (this.x.getValue() && mc.player != null) {
         if (!EntityUtil.a_bool_int(var1.getBreakerId())) {
            boolean var2 = false;

            for (BlockPos var4 : (Iterable<BlockPos>)(Object)this.y()) {
               int var5 = var1.getPos().getY();
               int var6 = var4.getY();
               if (var5 == var6) {
                  int var7 = var1.getPos().getX();
                  int var8 = var4.getX();
                  int var9 = var7 - var8;
                  int var10 = var1.getPos().getZ();
                  int var11 = var4.getZ();
                  int var12 = var10 - var11;
                  int var13 = Math.abs(var9);
                  int var14 = Math.abs(var12);
                  if (var13 + var14 == 1) {
                     var2 = true;
                     break;
                  }
               }
            }

            if (var2) {
               int var15 = var1.getBreakerId();
               BlockPos var16 = var1.getPos();
               Surround.CrystalBreakEntry var17 = new Surround.CrystalBreakEntry(var15, var16);
               Map var18 = this.X;
               Surround.CrystalBreakEntry var19 = var17;
               Surround.MinedBlockTracker var20 = (Surround.MinedBlockTracker)var18.get(var19);
               if (var20 == null) {
                  Map var21 = this.X;
                  Surround.CrystalBreakEntry var22 = var17;
                  BlockPos var23 = var1.getPos();
                  var21.put(var22, new Surround.MinedBlockTracker(var23));
               } else {
                  Surround.MinedBlockTracker var24 = var20;
                  long var25 = System.currentTimeMillis();
                  var24.d = var25;
               }
            }
         }
      }
   }

   @EventTarget
   public void a_void_PlayerJumpEvent(PlayerJumpEvent var1) {
      if (this.jumpDisable.getValue()) {
         this.setEnabled(false);
      }
   }

   @EventTarget
   public void a_void_UpdateWalkingPlayerEvent(UpdateWalkingPlayerEvent var1) {
      if (mc.player != null && mc.level != null) {
         if (this.W) {
            BlockPos var2 = PositionUtil.a_BlockPos_Entity(mc.player);
            LocalPlayer var3 = mc.player;
            double var4 = var2.getX();
            double var6 = 0.5;
            double var8 = var4 + var6;
            double var10 = mc.player.getY();
            double var12 = var2.getZ();
            double var14 = 0.5;
            double var16 = var12 + var14;
            var3.setPos(var8, var10, var16);
            BlockPos var18 = var2;
            this.U = var18;
            this.W = false;
         }

         label219: {
            if (this.jumpDisable.getValue()) {
               double var19 = mc.player.fallDistance;
               double var21 = 2.0;
               if (var19 > var21) {
                  break label219;
               }

               if (Night.moduleManager.getModule(Step.class).isEnabled() || Night.moduleManager.getModule(Speed.class).isEnabled()) {
                  if (this.U == null) {
                     break label219;
                  }

                  int var23 = this.U.getY();
                  int var24 = PositionUtil.a_BlockPos_Entity(mc.player).getY();
                  if (var23 != var24) {
                     break label219;
                  }
               }
            }

            if (!this.whileEating.getValue() && EntityUtil.a() && this.z()) {
               return;
            }

            label221: {
               this.V = 0;
               if (this.switchSetting.getValue().equalsIgnoreCase("None")) {
                  Item var25 = mc.player.getMainHandItem().getItem();
                  if (!(var25 instanceof BlockItem)) {
                     break label221;
                  }

                  Block var26 = ((BlockItem)var25).getBlock();
                  if (!this.a_bool_Block(var26)) {
                     break label221;
                  }
               }

               byte var32;
               label189: {
                  String var28 = this.switchSetting.getValue();
                  String var29 = "AltSwap";
                  if (!var28.equalsIgnoreCase(var29)) {
                     String var30 = this.switchSetting.getValue();
                     String var31 = "AltPickup";
                     if (!var30.equalsIgnoreCase(var31)) {
                        var32 = 8;
                        break label189;
                     }
                  }

                  var32 = 35;
               }

               int var33 = this.a_int_int_int(0, var32);
               int var34 = mc.player.getInventory().getSelectedSlot();
               if (var33 == -1) {
                  if (this.itemDisable.getValue()) {
                     String var35 = this.getName();
                     Night.chatManager.sendTaggedMessage("No blast-proof blocks could be found in your inventory.", var35);
                     this.setEnabled(false);
                  }

                  this.T.clear();
                  return;
               }

               LocalPlayer var36 = mc.player;
               boolean var37 = this.extension.getValue();
               boolean var38 = this.floor.getValue();
               boolean var39 = this.underFeet.getValue();
               boolean var40 = false;
               HashSet var41 = HoleUtil.a(var36, var37, var38, var39, var40);
               this.T = var41;
               if (this.x.getValue()) {
                  this.w();
               }

               if (this.attack.getValue()) {
                  this.v();
               }

               Set var42;
               if (this.advoidHelp.getValue()) {
                  var42 = this.x();
               } else {
                  var42 = Set.of();
               }

               Set var43 = var42;
               Stream<BlockPos> var44 = this.T.stream();
               Predicate<BlockPos> var45 = var1x -> {
                  LocalPlayer var2x = mc.player;
                  Vec3 var3x = Vec3.atCenterOf(var1x);
                  double var4x = var2x.distanceToSqr(var3x);
                  double var6x = Mth.square(this.range.getValue().doubleValue());
                  boolean var8x;
                  if (var4x <= var6x) {
                     var8x = true;
                  } else {
                     var8x = false;
                  }

                  return var8x;
               };
               Stream<BlockPos> var46 = var44.filter(var45);
               Predicate<BlockPos> var47 = var0 -> {
                  boolean var1x;
                  if (!BlockInteractionUtil.c_bool_BlockPos(var0) && BlockInteractionUtil.e_bool_BlockPos(var0)) {
                     var1x = true;
                  } else {
                     var1x = false;
                  }

                  return var1x;
               };
               Stream<BlockPos> var48 = var46.filter(var47);
               Consumer<BlockPos> var49 = var0 -> BlockInteractionUtil.a_void_BlockPos_Direction(var0, Direction.UP);
               var48.forEach(var49);
               HitboxDesync var50 = Night.moduleManager.getModule(HitboxDesync.class);
               Stream<BlockPos> var52 = this.T.stream();
               Predicate<BlockPos> var53 = var1x -> {
                  LocalPlayer var2x = mc.player;
                  Vec3 var3x = Vec3.atCenterOf(var1x);
                  double var4x = var2x.distanceToSqr(var3x);
                  double var6x = Mth.square(this.range.getValue().doubleValue());
                  boolean var8x;
                  if (var4x <= var6x) {
                     var8x = true;
                  } else {
                     var8x = false;
                  }

                  return var8x;
               };
               Stream<BlockPos> var54 = var52.filter(var53);
               Predicate<BlockPos> var55 = var1x -> {
                  boolean var2x;
                  if (var50 != null && var50.isEnabled() && !var50.close.getValue()) {
                     var2x = true;
                  } else {
                     var2x = false;
                  }

                  return BlockInteractionUtil.a_bool_BlockPos_bool(var1x, var2x);
               };
               Stream<BlockPos> var56 = var54.filter(var55);
               Predicate<BlockPos> var57 = var1x -> {
                  boolean var2x;
                  if (!var43.contains(var1x)) {
                     var2x = true;
                  } else {
                     var2x = false;
                  }

                  return var2x;
               };
               List var58 = var56.filter(var57).toList();
               ArrayList<BlockPos> var51 = new ArrayList<>(var58);
               ArrayList<BlockPos> var59 = var51;
               Vec3 var60 = mc.player.position();
               Vec3 var61 = mc.player.getDeltaMovement();
               double var62 = 0.5;
               Vec3 var64 = var61.scale(var62);
               Vec3 var65 = var60.add(var64);
               ArrayList<BlockPos> var66 = var59;
               Comparator<BlockPos> var67 = (var1x, var2x) -> {
                  int var3x = var1x.getY();
                  int var4x = var2x.getY();
                  int var5 = Integer.compare(var3x, var4x);
                  if (var5 != 0) {
                     return var5;
                  }

                  boolean var6x;
                  if (BlockInteractionUtil.b_Direction_BlockPos_bool(var1x, false) != null) {
                     var6x = true;
                  } else {
                     var6x = false;
                  }

                  boolean var7 = var6x;
                  boolean var8x;
                  if (BlockInteractionUtil.b_Direction_BlockPos_bool(var2x, false) != null) {
                     var8x = true;
                  } else {
                     var8x = false;
                  }

                  boolean var9 = var8x;
                  boolean var10x = var7;
                  boolean var11 = var9;
                  if (var10x != var11) {
                     byte var12x;
                     if (var7) {
                        var12x = -1;
                     } else {
                        var12x = 1;
                     }

                     return var12x;
                  } else {
                     double var13 = Vec3.atCenterOf(var1x).distanceToSqr(var65);
                     double var15 = Vec3.atCenterOf(var2x).distanceToSqr(var65);
                     return Double.compare(var13, var15);
                  }
               };
               var66.sort(var67);
               KeyAction var69;
               if (Night.moduleManager != null) {
                  Class<KeyAction> var68 = KeyAction.class;
                  var69 = Night.moduleManager.getModule(var68);
               } else {
                  var69 = null;
               }

               label222: {
                  KeyAction var70 = var69;
                  if (this.pearlDisable.getValue()) {
                     if (var70 != null && var70.w()) {
                        break label222;
                     }

                     if (mc.player.isUsingItem()) {
                        Item var71 = mc.player.getUseItem().getItem();
                        Item var72 = Items.ENDER_PEARL;
                        if (var71 == var72) {
                           break label222;
                        }
                     }
                  }

                  if (this.chorusDisable.getValue() && mc.player.isUsingItem()) {
                     Item var74 = mc.player.getUseItem().getItem();
                     Item var75 = Items.CHORUS_FRUIT;
                     if (var74 == var75) {
                        boolean var76 = false;
                        this.setEnabled(var76);
                        return;
                     }
                  }

                  if (var70 != null && var70.w()) {
                     return;
                  }

                  if (var59.isEmpty()) {
                     if (this.selfDisable.getValue()) {
                        boolean var77 = false;
                        this.setEnabled(var77);
                     }

                     return;
                  }

                  boolean var78 = true;
                  this.S = var78;
                  Night.rotationManager.beginBatch();

                  try {
                     String var79 = this.switchSetting.getValue();
                     int var80 = var33;
                     int var81 = var34;
                     InventoryUtil.a_bool_String_int_int(var79, var80, var81);
                     ArrayList var82 = new ArrayList();
                     ArrayList var83 = var82;

                     for (BlockPos var85 : (Iterable<BlockPos>)(Object)var59) {
                        int var86 = this.V;
                        double var87 = this.bPT.getValue().intValue();
                        double var89 = BlockInteractionUtil.b_double_int(1);
                        int var91 = (int)(var87 * var89);
                        if (var86 >= var91) {
                           break;
                        }

                        if (this.attack.getValue()) {
                           ClientLevel var92 = mc.level;
                           Class<EndCrystal> var93 = EndCrystal.class;
                           BlockPos var95 = var85;
                           AABB var94 = new AABB(var95);
                           double var96 = 0.5;
                           AABB var98 = var94.inflate(var96);

                           for (EndCrystal var100 : (Iterable<EndCrystal>)(Object)var92.getEntitiesOfClass(var93, var98)) {
                              if (var100.isAlive()) {
                                 ClientPacketListener var101 = mc.getConnection();
                                 int var103 = var100.getId();
                                 ServerboundAttackPacket var102 = new ServerboundAttackPacket(var103);
                                 var101.send(var102);
                                 if (this.attackSwing.getValue()) {
                                    ClientPacketListener var104 = mc.getConnection();
                                    Packet var105 = PacketAndRenderUtil.a_Packet_InteractionHand(InteractionHand.MAIN_HAND);
                                    var104.send(var105);
                                 }
                              }
                           }
                        }

                        BlockPos var106 = var85;
                        ArrayList var107 = var83;
                        boolean var108 = this.strictDirection.getValue();
                        List var109 = BlockInteractionUtil.a_List_BlockPos_List_bool(var106, var107, var108);
                        if (!var109.isEmpty()) {
                           boolean var140 = false;

                           for (Direction var142 : (Iterable<Direction>)(Object)var109) {
                              BlockPos var143 = var85;
                              Direction var144 = var142;
                              InteractionHand var145 = InteractionHand.MAIN_HAND;
                              boolean var146 = this.rotate.getValue();
                              boolean var147 = true;
                              boolean var148 = this.render.getValue();
                              if (BlockInteractionUtil.a(var143, var144, var145, var146, var147, var148)) {
                                 ArrayList var149 = var83;
                                 BlockPos var150 = var85;
                                 var149.add(var150);
                                 long var151 = System.currentTimeMillis();
                                 this.Z = var151;
                                 int var153 = this.V;
                                 byte var154 = 1;
                                 int var155 = var153 + var154;
                                 this.V = var155;
                                 var140 = true;
                                 break;
                              }
                           }

                           if (!var140) {
                              if (this.airPlace.getValue() && !this.strictDirection.getValue()) {
                                 BlockPos var156 = var85;
                                 this.a_void_BlockPos(var156);
                                 ArrayList var157 = var83;
                                 BlockPos var158 = var85;
                                 var157.add(var158);
                                 long var159 = System.currentTimeMillis();
                                 this.Z = var159;
                                 int var161 = this.V;
                                 byte var162 = 1;
                                 int var163 = var161 + var162;
                                 this.V = var163;
                              } else {
                                 BlockPos var164 = var85;
                                 ArrayList var165 = var83;
                                 boolean var166 = this.strictDirection.getValue();
                                 byte var167 = 2;
                                 BlockPos var168 = BlockInteractionUtil.a_BlockPos_BlockPos_List_bool_int(var164, var165, var166, var167);
                                 if (var168 != null) {
                                    BlockPos var169 = var168;
                                    ArrayList var170 = var83;
                                    boolean var171 = this.strictDirection.getValue();
                                    Direction var172 = BlockInteractionUtil.b_Direction_BlockPos_List_bool(var169, var170, var171);
                                    if (var172 != null) {
                                       BlockPos var173 = var168;
                                       Direction var174 = var172;
                                       InteractionHand var175 = InteractionHand.MAIN_HAND;
                                       boolean var176 = this.rotate.getValue();
                                       boolean var177 = true;
                                       boolean var178 = this.render.getValue();
                                       if (BlockInteractionUtil.a(var173, var174, var175, var176, var177, var178)) {
                                          ArrayList var179 = var83;
                                          BlockPos var180 = var168;
                                          var179.add(var180);
                                          long var181 = System.currentTimeMillis();
                                          this.Z = var181;
                                          int var183 = this.V;
                                          byte var184 = 1;
                                          int var185 = var183 + var184;
                                          this.V = var185;
                                       }
                                    }
                                 }
                              }
                           }
                        } else if (this.airPlace.getValue() && !this.strictDirection.getValue()) {
                           BlockPos var110 = var85;
                           this.a_void_BlockPos(var110);
                           ArrayList var111 = var83;
                           BlockPos var112 = var85;
                           var111.add(var112);
                           long var113 = System.currentTimeMillis();
                           this.Z = var113;
                           int var115 = this.V;
                           byte var116 = 1;
                           int var117 = var115 + var116;
                           this.V = var117;
                        } else {
                           BlockPos var118 = var85;
                           ArrayList var119 = var83;
                           boolean var120 = this.strictDirection.getValue();
                           byte var121 = 2;
                           BlockPos var122 = BlockInteractionUtil.a_BlockPos_BlockPos_List_bool_int(var118, var119, var120, var121);
                           if (var122 != null) {
                              BlockPos var123 = var122;
                              ArrayList var124 = var83;
                              boolean var125 = this.strictDirection.getValue();
                              Direction var126 = BlockInteractionUtil.b_Direction_BlockPos_List_bool(var123, var124, var125);
                              if (var126 != null) {
                                 BlockPos var127 = var122;
                                 Direction var128 = var126;
                                 InteractionHand var129 = InteractionHand.MAIN_HAND;
                                 boolean var130 = this.rotate.getValue();
                                 boolean var131 = true;
                                 boolean var132 = this.render.getValue();
                                 if (BlockInteractionUtil.a(var127, var128, var129, var130, var131, var132)) {
                                    ArrayList var133 = var83;
                                    BlockPos var134 = var122;
                                    var133.add(var134);
                                    long var135 = System.currentTimeMillis();
                                    this.Z = var135;
                                    int var137 = this.V;
                                    byte var138 = 1;
                                    int var139 = var137 + var138;
                                    this.V = var139;
                                 }
                              }
                           }
                        }
                     }

                     String var186 = this.switchSetting.getValue();
                     int var187 = var33;
                     int var188 = var34;
                     InventoryUtil.b(var186, var187, var188);
                  } catch (Throwable var192) {
                     Night.rotationManager.endBatch();
                     boolean var191 = false;
                     this.S = var191;
                     throw var192;
                  }

                  Night.rotationManager.endBatch();
                  boolean var189 = false;
                  this.S = var189;
                  return;
               }

               boolean var73 = false;
               this.setEnabled(var73);
               return;
            }

            if (this.itemDisable.getValue()) {
               String var27 = this.getName();
               Night.chatManager.sendTaggedMessage("You are currently not holding any blast-proof blocks.", var27);
               this.setEnabled(false);
            }

            this.T.clear();
            return;
         }

         this.setEnabled(false);
      }
   }

   private void a_void_BlockPos(BlockPos var1) {
      boolean var2 = this.rotate.getValue();
      boolean var3 = this.render.getValue();
      BlockInteractionUtil.a_void_BlockPos_InteractionHand_bool_bool(var1, InteractionHand.MAIN_HAND, var2, var3);
   }

   @EventTarget
   public void a_void_PacketReceiveEvent(PacketReceiveEvent var1) {
      if (mc.player != null && mc.level != null) {
         ClientboundOpenScreenPacket var3 = null;
         label120: {
            if (var1.getPacket() instanceof ClientboundOpenScreenPacket _osp && (var3 = _osp) != null) {
               AutoShop var4 = Night.moduleManager.getModule(AutoShop.class);
               if (var4 != null && var4.x()) {
                  return;
               }

               Module var5 = Night.moduleManager.getModuleByName("AutoEnchant");
               if (var5 != null && var5.isEnabled()) {
                  return;
               }

               if (this.S) {
                  break label120;
               }

               long var6 = System.currentTimeMillis();
               long var8 = this.Z;
               long var10 = var6 - var8;
               long var12 = 1000L;
               if (var10 < var12) {
                  break label120;
               }

               long var14 = System.currentTimeMillis();
               long var16 = BlockInteractionUtil.b;
               long var18 = var14 - var16;
               long var20 = 1000L;
               if (var18 < var20) {
                  break label120;
               }
            }

            if (var1.getPacket() instanceof ClientboundPlayerPositionPacket var25) {
               if (this.rubberbandDisable.getValue()) {
                  Vec3 var26 = mc.player.position();
                  double var27 = var25.change().position().x;
                  double var29 = var25.change().position().y;
                  double var31 = var25.change().position().z;
                  Vec3 var33 = var26;
                  double var34 = var27;
                  double var36 = var29;
                  double var38 = var31;
                  double var40 = var33.distanceToSqr(var34, var36, var38);
                  double var42 = 1.0;
                  if (var40 >= var42) {
                     this.setEnabled(false);
                     return;
                  }
               }

               if (this.centerOnTP.getValue() && mc.player.isUsingItem() && mc.player.getUseItem().getItem() == Items.CHORUS_FRUIT) {
                  if (this.chorusDisable.getValue()) {
                     this.setEnabled(false);
                     return;
                  }

                  this.W = true;
               }
            }

            if (this.attack.getValue() && var1.getPacket() instanceof ClientboundAddEntityPacket var45 && var45.getType() == EntityTypes.END_CRYSTAL) {
               double var46 = var45.getX();
               double var48 = var45.getY();
               double var50 = var45.getZ();
               Vec3 var52 = new Vec3(var46, var48, var50);
               double var53 = var52.x;
               double var55 = 1.0;
               double var57 = var53 - var55;
               double var59 = var52.y;
               double var61 = var52.z;
               double var63 = 1.0;
               double var65 = var61 - var63;
               double var67 = var52.x;
               double var69 = 1.0;
               double var71 = var67 + var69;
               double var73 = var52.y;
               double var75 = 2.0;
               double var77 = var73 + var75;
               double var79 = var52.z;
               double var81 = 1.0;
               double var83 = var79 + var81;
               AABB var85 = new AABB(var57, var59, var65, var71, var77, var83);
               boolean var86 = false;
               Iterator var87 = this.T.iterator();

               label96: {
                  double var93;
                  double var95;
                  do {
                     if (!var87.hasNext()) {
                        break label96;
                     }

                     BlockPos var88 = (BlockPos)var87.next();
                     AABB var89 = var85;
                     BlockPos var90 = var88;
                     if (var89.intersects(new AABB(var90))) {
                        break;
                     }

                     Vec3 var91 = var52;
                     Vec3 var92 = Vec3.atCenterOf(var88);
                     var93 = var91.distanceToSqr(var92);
                     var95 = 4.0;
                  } while (!(var93 <= var95));

                  var86 = true;
               }

               if (var86) {
                  Vec3 var97 = mc.player.getEyePosition();
                  Vec3 var98 = var52;
                  double var99 = var97.distanceToSqr(var98);
                  double var101 = Mth.square(this.attackRange.getValue().doubleValue());
                  if (var99 <= var101 && (this.attackMultiTask.getValue() || !mc.player.isUsingItem())) {
                     ClientPacketListener var103 = mc.getConnection();
                     int var104 = var45.getId();
                     var103.send(new ServerboundAttackPacket(var104));
                     if (this.attackSwing.getValue()) {
                        ClientPacketListener var105 = mc.getConnection();
                        Packet var106 = PacketAndRenderUtil.a_Packet_InteractionHand(InteractionHand.MAIN_HAND);
                        var105.send(var106);
                     }
                  }
               }
            }

            if (!this.timing.getValue().equalsIgnoreCase("Sequential")) {
               return;
            }

            if (!this.predict.getValue()) {
               return;
            }

            if (var1.getPacket() instanceof ClientboundBlockUpdatePacket var108) {
               if (var108.getBlockState().isAir()) {
                  Set var109 = this.T;
                  BlockPos var110 = var108.getPos();
                  if (var109.contains(var110)) {
                     BlockPos var111 = var108.getPos();
                     this.b(var111);
                  }
               }
            } else if (var1.getPacket() instanceof ClientboundSectionBlocksUpdatePacket var113) {
               ArrayList var114 = new ArrayList();
               ClientboundSectionBlocksUpdatePacket var115 = var113;
               ArrayList var116 = var114;
               var115.runUpdates((var2, var3x) -> {
                  if (var3x.isAir() && this.T.contains(var2)) {
                     BlockPos var4x = var2.immutable();
                     var116.add(var4x);
                  }
               });

               for (BlockPos var118 : (Iterable<BlockPos>)(Object)var114) {
                  this.b(var118);
               }
            }

            return;
         }
         if (var3 != null) {
            var1.setCancelled(true);
            ClientPacketListener var22 = mc.getConnection();
            int var23 = var3.getContainerId();
            var22.send(new ServerboundContainerClosePacket(var23));
         }
      }
   }

   private void b(BlockPos var1) {
      int var2 = this.V;
      double var3 = this.bPT.getValue().intValue();
      double var5 = BlockInteractionUtil.b_double_int(1);
      int var7 = (int)(var3 * var5);
      if (var2 < var7) {
         if (this.whileEating.getValue() || !EntityUtil.a() || !this.z()) {
            HitboxDesync var8 = Night.moduleManager.getModule(HitboxDesync.class);
            boolean var9;
            if (var8 != null && var8.isEnabled() && !var8.close.getValue()) {
               var9 = true;
            } else {
               var9 = false;
            }

            boolean var10 = var9;
            if (BlockInteractionUtil.a_bool_BlockPos_bool(var1, var10)) {
               byte var11;
               if (!this.switchSetting.getValue().equalsIgnoreCase("AltSwap") && !this.switchSetting.getValue().equalsIgnoreCase("AltPickup")) {
                  var11 = 8;
               } else {
                  var11 = 35;
               }

               int var12 = this.a_int_int_int(0, var11);
               int var13 = mc.player.getInventory().getSelectedSlot();
               if (var12 != -1) {
                  boolean var14 = this.strictDirection.getValue();
                  Direction var15 = BlockInteractionUtil.b_Direction_BlockPos_bool(var1, var14);
                  if (var15 == null) {
                     label60: {
                        if (this.airPlace.getValue() && !this.strictDirection.getValue()) {
                           Night.rotationManager.beginBatch();

                           try {
                              String var16 = this.switchSetting.getValue();
                              int var17 = var12;
                              int var18 = var13;
                              if (!InventoryUtil.a_bool_String_int_int(var16, var17, var18)) {
                                 break label60;
                              }

                              this.a_void_BlockPos(var1);
                              int var32 = this.V + 1;
                              this.V = var32;
                              long var33 = System.currentTimeMillis();
                              this.Z = var33;
                              String var35 = this.switchSetting.getValue();
                              int var36 = var12;
                              int var37 = var13;
                              InventoryUtil.b(var35, var36, var37);
                           } catch (Throwable var39) {
                              Night.rotationManager.endBatch();
                              throw var39;
                           }

                           Night.rotationManager.endBatch();
                        }

                        return;
                     }

                     Night.rotationManager.endBatch();
                  } else {
                     Night.rotationManager.beginBatch();

                     label67: {
                        try {
                           String var19 = this.switchSetting.getValue();
                           int var20 = var12;
                           int var21 = var13;
                           if (!InventoryUtil.a_bool_String_int_int(var19, var20, var21)) {
                              break label67;
                           }

                           Direction var22 = var15;
                           boolean var23 = this.rotate.getValue();
                           boolean var24 = this.render.getValue();
                           BlockInteractionUtil.a(var1, var22, InteractionHand.MAIN_HAND, var23, true, var24);
                           int var25 = this.V + 1;
                           this.V = var25;
                           long var26 = System.currentTimeMillis();
                           this.Z = var26;
                           String var28 = this.switchSetting.getValue();
                           int var29 = var12;
                           int var30 = var13;
                           InventoryUtil.b(var28, var29, var30);
                        } catch (Throwable var40) {
                           Night.rotationManager.endBatch();
                           throw var40;
                        }

                        Night.rotationManager.endBatch();
                        return;
                     }

                     Night.rotationManager.endBatch();
                  }
               }
            }
         }
      }
   }

   private void v() {
      Night.rotationManager.beginBatch();
      ClientLevel var1 = mc.level;
      BlockPos var3 = mc.player.blockPosition();
      AABB var2 = new AABB(var3);
      double var4 = this.attackRange.getValue().doubleValue();
      double var6 = 3.0;
      double var8 = var4 + var6;
      AABB var10 = var2.inflate(var8);

      for (EndCrystal var12 : var1.getEntitiesOfClass(EndCrystal.class, var10)) {
         if (var12.isAlive() && !var12.isRemoved()) {
            int var13 = var12.tickCount;
            int var14 = this.attackAge.getValue().intValue();
            if (var13 >= var14) {
               AABB var15 = var12.getBoundingBox();
               boolean var16 = false;

               for (BlockPos var18 : (Iterable<BlockPos>)(Object)this.T) {
                  ClientLevel var19 = mc.level;
                  BlockPos var20 = var18;
                  if (var19.getBlockState(var20).canBeReplaced()) {
                     BlockPos var21 = var18;
                     AABB var22 = var15;
                     if (new AABB(var21).intersects(var22)) {
                        var16 = true;
                        break;
                     }
                  }
               }

               if (var16) {
                  AABB var23 = var15;
                  Vec3 var24 = mc.player.getEyePosition();
                  double var25 = var23.distanceToSqr(var24);
                  double var27 = this.attackRange.getValue().doubleValue();
                  double var29 = this.attackRange.getValue().doubleValue();
                  double var31 = var27 * var29;
                  if (!(var25 > var31) && (this.attackMultiTask.getValue() || !mc.player.isUsingItem())) {
                     if (this.attackRotate.getValue()) {
                        float[] var33 = RotationMathUtil.a(var12.getBoundingBox().getCenter());
                        float var34 = var33[0];
                        float var35 = var33[1];
                        Night.rotationManager.sendOrBatchPosRot(var34, var35);
                        AABB var36 = var15;
                        Vec3 var37 = mc.player.getEyePosition();
                        if (!var36.contains(var37) && !BlockInteractionUtil.a_bool_Vec3(var12.getBoundingBox().getCenter())) {
                           continue;
                        }
                     }

                     ClientPacketListener var38 = mc.getConnection();
                     int var39 = var12.getId();
                     var38.send(new ServerboundAttackPacket(var39));
                     if (this.attackSwing.getValue()) {
                        ClientPacketListener var40 = mc.getConnection();
                        Packet var41 = PacketAndRenderUtil.a_Packet_InteractionHand(InteractionHand.MAIN_HAND);
                        var40.send(var41);
                     }
                  }
               }
            }
         }
      }

      Night.rotationManager.endBatch();
   }

   private void w() {
      this.X.entrySet().removeIf(var0 -> {
         Surround.MinedBlockTracker var1x = var0.getValue();
         ClientLevel var2x = mc.level;
         int var3x = var0.getKey().count();
         if (var2x.getEntity(var3x) != null) {
            ClientLevel var4x = mc.level;
            BlockPos var5 = var1x.a;
            if (!var4x.getBlockState(var5).canBeReplaced()) {
               long var6x = System.currentTimeMillis();
               long var8x = var1x.d;
               long var10x = var6x - var8x;
               long var12x = 1000L;
               if (var10x <= var12x) {
                  return false;
               }
            }
         }

         return true;
      });
      Iterator var1 = this.X.values().iterator();

      while (true) {
         Surround.MinedBlockTracker var2;
         long var8;
         long var10;
         do {
            if (!var1.hasNext()) {
               return;
            }

            var2 = (Surround.MinedBlockTracker)var1.next();
            int var3 = var2.c + 1;
            var2.c = var3;
            if (var2.c >= 2) {
               break;
            }

            long var4 = System.currentTimeMillis();
            long var6 = var2.b;
            var8 = var4 - var6;
            var10 = 100L;
         } while (var8 < var10);

         BlockPos var12 = var2.a;

         for (BlockPos var14 : (Iterable<BlockPos>)(Object)this.y()) {
            int var15 = var12.getY();
            int var16 = var14.getY();
            if (var15 == var16) {
               int var17 = var12.getX();
               int var18 = var14.getX();
               int var19 = var17 - var18;
               int var20 = var12.getZ();
               int var21 = var14.getZ();
               int var22 = var20 - var21;
               int var23 = Math.abs(var19);
               int var24 = Math.abs(var22);
               if (var23 + var24 == 1) {
                  if (this.y.getValue()) {
                     Set var25 = this.T;
                     BlockPos var26 = var12.above();
                     var25.add(var26);
                     Set var27 = this.T;
                     BlockPos var28 = var12.below();
                     var27.add(var28);
                  }

                  if (this.z.getValue()) {
                     if (var19 != 0) {
                        Set var29 = this.T;
                        BlockPos var30 = var14;
                        int var31 = var19;
                        BlockPos var32 = var30.offset(var31, 0, 1);
                        var29.add(var32);
                        Set var33 = this.T;
                        BlockPos var34 = var14;
                        int var35 = var19;
                        BlockPos var36 = var34.offset(var35, 0, -1);
                        var33.add(var36);
                     } else {
                        Set var37 = this.T;
                        BlockPos var38 = var14;
                        int var39 = var22;
                        BlockPos var40 = var38.offset(1, 0, var39);
                        var37.add(var40);
                        Set var41 = this.T;
                        BlockPos var42 = var14;
                        int var43 = var22;
                        BlockPos var44 = var42.offset(-1, 0, var43);
                        var41.add(var44);
                     }
                  }

                  if (this.edge.getValue()) {
                     if (var19 != 0) {
                        Set var45 = this.T;
                        BlockPos var46 = var14;
                        int var47 = var19 * 2;
                        BlockPos var48 = var46.offset(var47, 0, 0);
                        var45.add(var48);
                     } else {
                        Set var49 = this.T;
                        BlockPos var50 = var14;
                        int var51 = var22 * 2;
                        BlockPos var52 = var50.offset(0, 0, var51);
                        var49.add(var52);
                     }
                  }
               }
            }
         }
      }
   }

   private Set<BlockPos> x() {
      Set var1 = this.y();
      float var2 = mc.player.getHealth();
      float var3 = this.advoidHelpHealth.getValue().floatValue();
      if (var2 < var3) {
         return Set.of();
      }

      HashSet var4 = new HashSet();

      for (Player var6 : (Iterable<Player>)(Object)mc.level.players()) {
         LocalPlayer var7 = mc.player;
         if (var6 != var7) {
            String var8 = var6.getName().getString();
            if (!Night.friendManager.isFriend(var8)) {
               LocalPlayer var9 = mc.player;
               Player var10 = var6;
               double var11 = var9.distanceToSqr(var10);
               double var13 = 25.0;
               if (!(var11 > var13)) {
                  ClientLevel var15 = mc.level;
                  BlockPos var16 = var6.blockPosition();
                  if (var15.getBlockState(var16).canBeReplaced()) {
                     Player var17 = var6;

                     for (BlockPos var19 : (Iterable<BlockPos>)(Object)this.a_Set_Player(var17)) {
                        Direction[] var20 = Y;
                        int var21 = var20.length;
                        int var22 = 0;

                        while (true) {
                           int var23 = var22;
                           int var24 = var21;
                           if (var23 >= var24) {
                              HashSet var34 = var4;
                              BlockPos var35 = var19.below();
                              var34.add(var35);
                              HashSet var36 = var4;
                              BlockPos var37 = var19;
                              var36.add(var37);
                              break;
                           }

                           Direction[] var25 = var20;
                           int var26 = var22;
                           Direction var27 = var25[var26];
                           BlockPos var28 = var19;
                           Direction var29 = var27;
                           BlockPos var30 = var28.relative(var29);
                           Player var31 = var6;
                           Set var32 = this.a_Set_Player(var31);
                           BlockPos var33 = var30;
                           if (!var32.contains(var33)) {
                              HashSet var38 = var4;
                              BlockPos var39 = var30;
                              var38.add(var39);
                           }

                           var22++;
                        }
                     }
                  }
               }
            }
         }
      }

      if (var4.isEmpty()) {
         return Set.of();
      }

      HashSet var40 = new HashSet();

      for (BlockPos var42 : (Iterable<BlockPos>)(Object)var4) {
         boolean var43 = false;
         Iterator var44 = var1.iterator();

         label69: {
            BlockPos var58;
            BlockPos var59;
            do {
               if (!var44.hasNext()) {
                  break label69;
               }

               BlockPos var45 = (BlockPos)var44.next();
               BlockPos var46 = var42;
               BlockPos var47 = var45;
               if (var46.equals(var47)) {
                  break;
               }

               BlockPos var48 = var42;
               BlockPos var49 = var45.above();
               if (var48.equals(var49)) {
                  break;
               }

               BlockPos var50 = var42;
               BlockPos var51 = var45.below();
               if (var50.equals(var51)) {
                  break;
               }

               BlockPos var52 = var42;
               BlockPos var53 = var45.north();
               if (var52.equals(var53)) {
                  break;
               }

               BlockPos var54 = var42;
               BlockPos var55 = var45.south();
               if (var54.equals(var55)) {
                  break;
               }

               BlockPos var56 = var42;
               BlockPos var57 = var45.east();
               if (var56.equals(var57)) {
                  break;
               }

               var58 = var42;
               var59 = var45.west();
            } while (!var58.equals(var59));

            var43 = true;
         }

         if (!var43) {
            HashSet var60 = var40;
            BlockPos var61 = var42;
            var60.add(var61);
         }
      }

      return var40;
   }

   private Set<BlockPos> y() {
      LocalPlayer var1 = mc.player;
      return this.a_Set_Player(var1);
   }

   private Set<BlockPos> a_Set_Player(Player var1) {
      HashSet var2 = new HashSet();
      AABB var3 = var1.getBoundingBox();
      int var4 = var1.blockPosition().getY();
      int var5 = (int)Math.floor(var3.minX);

      while (true) {
         double var6 = var5;
         double var8 = Math.ceil(var3.maxX);
         if (!(var6 < var8)) {
            return var2;
         }

         int var10 = (int)Math.floor(var3.minZ);

         while (true) {
            double var11 = var10;
            double var13 = Math.ceil(var3.maxZ);
            if (!(var11 < var13)) {
               var5++;
               break;
            }

            HashSet var15 = var2;
            int var16 = var5;
            int var17 = var4;
            int var18 = var10;
            var15.add(new BlockPos(var16, var17, var18));
            var10++;
         }
      }
   }

   @EventTarget
   public void a_void_ClientDisconnectEvent(ClientDisconnectEvent var1) {
      this.setEnabled(false);
   }

   private boolean z() {
      if (!this.advanced.getValue()) {
         return true;
      }

      if (EntityUtil.a_bool_Entity(mc.player)) {
         return true;
      }

      LocalPlayer var1 = mc.player;
      boolean var2 = this.extension.getValue();
      boolean var3 = this.floor.getValue();
      boolean var4 = this.underFeet.getValue();
      boolean var5;
      if (!HoleUtil.a(var1, var2, var3, var4, false).stream().filter(BlockInteractionUtil::c_bool_BlockPos).anyMatch(var1x -> {
         boolean var2x = this.strictDirection.getValue();
         boolean var3x;
         if (BlockInteractionUtil.b_Direction_BlockPos_bool(var1x, var2x) == null && !this.airPlace.getValue()) {
            var3x = false;
         } else {
            var3x = true;
         }

         return var3x;
      })) {
         var5 = true;
      } else {
         var5 = false;
      }

      return var5;
   }

   private boolean a_bool_Block(Block var1) {
      boolean var2;
      if (var1 != Blocks.OBSIDIAN
         && var1 != Blocks.ENDER_CHEST
         && var1 != Blocks.CRYING_OBSIDIAN
         && var1 != Blocks.NETHERITE_BLOCK
         && var1 != Blocks.RESPAWN_ANCHOR
         && var1 != Blocks.ANCIENT_DEBRIS
         && var1 != Blocks.ENCHANTING_TABLE
         && var1 != Blocks.ANVIL
         && var1 != Blocks.CHIPPED_ANVIL
         && var1 != Blocks.DAMAGED_ANVIL) {
         var2 = false;
      } else {
         var2 = true;
      }

      return var2;
   }

   private int a_int_int_int(int var1, int var2) {
      float var3 = -1.0F;
      int var4 = -1;

      for (int var5 = var1; var5 <= var2; var5++) {
         Inventory var6 = mc.player.getInventory();
         int var7 = var5;
         Item var8 = var6.getItem(var7).getItem();
         if (var8 instanceof BlockItem) {
            Block var9 = ((BlockItem)var8).getBlock();
            if (this.a_bool_Block(var9)) {
               float var10 = var9.defaultDestroyTime();
               if (var10 == -1.0F) {
                  return var5;
               }

               float var11 = var10;
               float var12 = var3;
               if (var11 > var12) {
                  var3 = var10;
                  var4 = var5;
               }
            }
         }
      }

      return var4;
   }

   @EventTarget
   public void a_void_Post(PacketSendEvent.Post var1) {
      if (this.S) {
         int var2 = this.R + 1;
         this.R = var2;
      }
   }

   static {
      Direction[] var0 = new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
      Y = var0;
   }

   record CrystalBreakEntry(int count, BlockPos pos) {
   }

   static class MinedBlockTracker {
      final BlockPos a;
      final long b;
      int c;
      long d;

      MinedBlockTracker(BlockPos var1) {
         this.a = var1;
         long var2 = System.currentTimeMillis();
         this.b = var2;
         this.c = 0;
         long var4 = System.currentTimeMillis();
         this.d = var4;
      }
   }
}
