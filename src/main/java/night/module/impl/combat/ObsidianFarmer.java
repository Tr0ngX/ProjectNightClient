package night.module.impl.combat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import night.Night;
import night.events.EventTarget;
import night.events.impl.UpdateWalkingPlayerEvent;
import night.module.Module;
import night.module.ModuleInfo;
import night.module.impl.player.SpeedMine;
import night.setting.impl.BooleanSetting;
import night.setting.impl.ModeSetting;
import night.setting.impl.NumberSetting;
import night.util.inventory.InventoryUtil;
import night.util.math.TimerUtil;
import night.util.rotation.RotationMathUtil;
import night.util.world.BlockInteractionUtil;
import night.util.world.EntityUtil;
import night.util.world.PacketAndRenderUtil;

@ModuleInfo(name = "ObsidianFarmer", description = "Places and mines ender chests using SpeedMine.", category = Module.Category.COMBAT, proxySupported = true)
public class ObsidianFarmer extends Module {
   public ModeSetting switchSetting = new ModeSetting(
      "Switch", "The mode that will be used for automatically switching to necessary items.", "Silent", InventoryUtil.a
   );
   public BooleanSetting rotate = new BooleanSetting("Rotate", "Rotates toward each block before placing it.", true);
   public BooleanSetting strictDirection = new BooleanSetting("StrictDirection", "Only places and mines using directions that face you.", false);
   public BooleanSetting airPlace = new BooleanSetting("AirPlace", "Places blocks in the air without needing neighboring blocks.", false);
   public BooleanSetting crystalDestruction = new BooleanSetting("CrystalDestruction", "Destroys any crystals that interfere with block placement.", true);
   public NumberSetting range;
   public NumberSetting mineRange;
   public NumberSetting wallsRange;
   public NumberSetting delay;
   public NumberSetting mineDelay;
   public NumberSetting retryDelay;
   public BooleanSetting requirePacketMine;
   public BooleanSetting support;
   public BooleanSetting swing;
   public BooleanSetting whileEating;
   public BooleanSetting onlyGround;
   public BooleanSetting render;
   private final TimerUtil v;
   private final TimerUtil w;
   private final TimerUtil x;
   private BlockPos y;
   private ObsidianFarmer.Stage z;

   public ObsidianFarmer() {
      Double var1 = 5.0;
      Double var2 = 1.0;
      Double var3 = 6.0;
      this.range = new NumberSetting("Range", "The maximum range at which blocks will be placed.", var1, var2, var3);
      Double var4 = 5.0;
      Double var5 = 1.0;
      Double var6 = 8.0;
      this.mineRange = new NumberSetting("MineRange", "The maximum range at which ender chests will be mined.", var4, var5, var6);
      Double var7 = 3.5;
      Double var8 = 1.0;
      Double var9 = 8.0;
      this.wallsRange = new NumberSetting("WallsRange", "The maximum range through walls for mining.", var7, var8, var9);
      Integer var10 = 2;
      Integer var11 = 0;
      Integer var12 = 20;
      this.delay = new NumberSetting("Delay", "The delay in ticks between placements.", var10, var11, var12);
      Integer var13 = 1;
      Integer var14 = 0;
      Integer var15 = 20;
      this.mineDelay = new NumberSetting("MineDelay", "The delay in ticks before mining.", var13, var14, var15);
      Integer var16 = 1;
      Integer var17 = 1;
      Integer var18 = 20;
      this.retryDelay = new NumberSetting("RetryDelay", "The delay in ticks before retrying a failed mine attempt.", var16, var17, var18);
      this.requirePacketMine = new BooleanSetting("RequirePacketMine", "Only mines when SpeedMine is enabled.", true);
      this.support = new BooleanSetting("Support", "Places a supporting block under the ender chest if needed.", true);
      this.swing = new BooleanSetting("Swing", "Swings your hand when starting to break blocks.", true);
      this.whileEating = new BooleanSetting("WhileEating", "Places and mines blocks while eating.", true);
      this.onlyGround = new BooleanSetting("OnlyGround", "Only works while on ground.", false);
      this.render = new BooleanSetting("Render", "Whether or not to render place and mine positions.", true);
      this.v = new TimerUtil();
      this.w = new TimerUtil();
      this.x = new TimerUtil();
      this.z = ObsidianFarmer.Stage.Place;
   }

   @Override
   public void onEnable() {
      this.y = null;
      this.z = ObsidianFarmer.Stage.Place;
      this.v.reset();
      this.w.reset();
      this.x.reset();
   }

   @Override
   public void onDisable() {
      this.y = null;
      this.z = ObsidianFarmer.Stage.Place;
   }

   @EventTarget
   public void a_void_UpdateWalkingPlayerEvent(UpdateWalkingPlayerEvent var1) {
      if (!this.isProxyDelegated()) {
         if (!this.nullCheck()) {
            LocalPlayer var2 = mc.player;
            ClientLevel var3 = mc.level;
            MultiPlayerGameMode var4 = mc.gameMode;
            if (var2 != null && var3 != null && var4 != null) {
               if (this.whileEating.getValue() || !EntityUtil.a()) {
                  if (!this.onlyGround.getValue() || var2.onGround()) {
                     LocalPlayer var5 = var2;
                     ClientLevel var6 = var3;
                     this.a_void_LocalPlayer_ClientLevel(var5, var6);
                     if (this.y != null) {
                        ClientLevel var7 = var3;
                        BlockPos var8 = this.y;
                        if (var7.getBlockState(var8).is(Blocks.ENDER_CHEST)) {
                           this.z = ObsidianFarmer.Stage.Mine;
                           if (this.requirePacketMine.getValue() && !this.v()) {
                              return;
                           }

                           LocalPlayer var9 = var2;
                           ClientLevel var10 = var3;
                           MultiPlayerGameMode var11 = var4;
                           BlockPos var12 = this.y;
                           this.a_void_LocalPlayer_ClientLevel_MultiPlayerGameMode_BlockPos(var9, var10, var11, var12);
                           return;
                        }
                     }

                     this.z = ObsidianFarmer.Stage.Place;
                     this.y = null;
                     LocalPlayer var13 = var2;
                     ClientLevel var14 = var3;
                     this.c(var13, var14);
                  }
               }
            }
         }
      }
   }

   private boolean v() {
      if (Night.moduleManager == null) {
         return false;
      }

      SpeedMine var1 = Night.moduleManager.getModule(SpeedMine.class);
      boolean var2;
      if (var1 != null && var1.isEnabled()) {
         var2 = true;
      } else {
         var2 = false;
      }

      return var2;
   }

   private void a_void_LocalPlayer_ClientLevel(LocalPlayer var1, ClientLevel var2) {
      if (this.y != null) {
         BlockPos var3 = this.y;
         if (!var2.getBlockState(var3).is(Blocks.ENDER_CHEST)) {
            this.y = null;
         }
      } else {
         BlockPos var4 = this.b(var1, var2);
         this.y = var4;
      }
   }

   private BlockPos b(LocalPlayer var1, ClientLevel var2) {
      double var3 = this.range.getValue().doubleValue();
      double var5 = this.mineRange.getValue().doubleValue();
      double var7 = Math.max(var3, var5);
      double var9 = var7 * var7;
      int var11 = (int)Math.ceil(var7);
      BlockPos var12 = var1.blockPosition();
      BlockPos var13 = null;
      double var14 = Double.MAX_VALUE;
      int var16 = -var11;

      while (true) {
         int var17 = var16;
         int var18 = var11;
         if (var17 > var18) {
            return var13;
         }

         int var19 = -var11;

         while (true) {
            int var20 = var19;
            int var21 = var11;
            if (var20 > var21) {
               var16++;
               break;
            }

            int var22 = -var11;

            while (true) {
               int var23 = var22;
               int var24 = var11;
               if (var23 > var24) {
                  var19++;
                  break;
               }

               BlockPos var25 = var12;
               int var26 = var16;
               int var27 = var19;
               int var28 = var22;
               BlockPos var29 = var25.offset(var26, var27, var28);
               if (var2.getBlockState(var29).is(Blocks.ENDER_CHEST)) {
                  Vec3 var30 = Vec3.atCenterOf(var29);
                  double var31 = var1.distanceToSqr(var30);
                  double var33 = var9;
                  if (!(var31 > var33)) {
                     double var35 = var31;
                     double var37 = var14;
                     if (var35 < var37) {
                        var14 = var31;
                        var13 = var29.immutable();
                     }
                  }
               }

               var22++;
            }
         }
      }
   }

   private void c(LocalPlayer var1, ClientLevel var2) {
      TimerUtil var3 = this.v;
      long var4 = this.delay.getValue().intValue();
      long var6 = 50L;
      Long var8 = var4 * var6;
      if (var3.passedMs(var8)) {
         BlockPos var9 = this.d(var1, var2);
         if (var9 != null) {
            int var10 = this.b(var1);
            if (var10 != -1) {
               int var11 = var1.getInventory().getSelectedSlot();
               Night.rotationManager.beginBatch();
               boolean var12 = false;

               boolean var17;
               try {
                  String var13 = this.switchSetting.getValue();
                  int var14 = var10;
                  int var15 = var11;
                  InventoryUtil.a_bool_String_int_int(var13, var14, var15);
                  BlockPos var16 = var9;
                  var17 = this.a_bool_BlockPos(var16);
                  if (!var17 && this.support.getValue()) {
                     BlockPos var18 = var9.below();
                     if (this.a(var1, var2, var18)) {
                        byte var19;
                        if (!this.switchSetting.getValue().equalsIgnoreCase("AltSwap") && !this.switchSetting.getValue().equalsIgnoreCase("AltPickup")) {
                           var19 = 8;
                        } else {
                           var19 = 35;
                        }

                        int var20 = InventoryUtil.b(0, var19);
                        if (var20 != -1) {
                           String var21 = this.switchSetting.getValue();
                           int var22 = var20;
                           int var23 = var11;
                           InventoryUtil.a_bool_String_int_int(var21, var22, var23);
                           BlockPos var24 = var18;
                           if (this.a_bool_BlockPos(var24)) {
                              BlockPos var25 = var9;
                              if (this.a(var1, var2, var25)) {
                                 String var26 = this.switchSetting.getValue();
                                 int var27 = var10;
                                 int var28 = var11;
                                 InventoryUtil.a_bool_String_int_int(var26, var27, var28);
                                 BlockPos var29 = var9;
                                 var17 = this.a_bool_BlockPos(var29);
                              }
                           }
                        }
                     }
                  }

                  String var30 = this.switchSetting.getValue();
                  int var31 = var10;
                  int var32 = var11;
                  InventoryUtil.b(var30, var31, var32);
               } catch (Throwable var38) {
                  Night.rotationManager.endBatch();
                  throw var38;
               }

               Night.rotationManager.endBatch();
               this.v.reset();
               if (var17) {
                  BlockPos var33 = var9;
                  BlockPos var34;
                  if (var2.getBlockState(var33).is(Blocks.ENDER_CHEST)) {
                     var34 = var9.immutable();
                  } else {
                     var34 = this.b(var1, var2);
                  }

                  BlockPos var35 = var34;
                  if (var35 != null) {
                     BlockPos var36 = var35;
                     this.y = var36;
                     this.z = ObsidianFarmer.Stage.Mine;
                     this.w.reset();
                     this.x.reset();
                  }
               }
            }
         }
      }
   }

   private boolean a_bool_BlockPos(BlockPos var1) {
      boolean var2 = this.strictDirection.getValue();
      Direction var3 = BlockInteractionUtil.b_Direction_BlockPos_bool(var1, var2);
      if (var3 != null) {
         Direction var4 = var3;
         boolean var5 = this.rotate.getValue();
         boolean var6 = this.crystalDestruction.getValue();
         boolean var7 = this.render.getValue();
         return BlockInteractionUtil.a(var1, var4, InteractionHand.MAIN_HAND, var5, var6, var7);
      } else if (this.airPlace.getValue()) {
         boolean var8 = this.rotate.getValue();
         boolean var9 = this.render.getValue();
         BlockInteractionUtil.a_void_BlockPos_InteractionHand_bool_bool(var1, InteractionHand.MAIN_HAND, var8, var9);
         return true;
      } else {
         return false;
      }
   }

   private void a_void_LocalPlayer_ClientLevel_MultiPlayerGameMode_BlockPos(LocalPlayer var1, ClientLevel var2, MultiPlayerGameMode var3, BlockPos var4) {
      TimerUtil var5 = this.w;
      long var6 = this.mineDelay.getValue().intValue();
      long var8 = 50L;
      Long var10 = var6 * var8;
      if (var5.passedMs(var10)) {
         TimerUtil var11 = this.x;
         long var12 = this.retryDelay.getValue().intValue();
         long var14 = 50L;
         Long var16 = var12 * var14;
         if (var11.passedMs(var16)) {
            if (!this.b(var1, var2, var4)) {
               this.y = null;
               this.z = ObsidianFarmer.Stage.Place;
            } else {
               Direction var17 = this.c(var1, var2, var4);
               if (!this.strictDirection.getValue() || var17 != null) {
                  Direction var18;
                  if (var17 == null) {
                     var18 = Direction.UP;
                  } else {
                     var18 = var17;
                  }

                  Direction var19 = var18;
                  Vec3 var20 = this.a_Vec3_BlockPos_Direction(var4, var19);
                  if (this.rotate.getValue()) {
                     float[] var21 = RotationMathUtil.a(var20);
                     float var22 = var21[0];
                     float var23 = var21[1];
                     if (!Night.rotationManager.canRotate(var22, var23)) {
                        return;
                     }
                  }

                  Direction var24 = var19;
                  var3.startDestroyBlock(var4, var24);
                  if (this.swing.getValue()) {
                     PacketAndRenderUtil.a(var1, InteractionHand.MAIN_HAND);
                  }

                  this.x.reset();
                  this.w.reset();
               }
            }
         }
      }
   }

   private BlockPos d(LocalPlayer var1, ClientLevel var2) {
      List<BlockPos> var3 = this.a_List_LocalPlayer(var1);
      var3.removeIf(var3x -> {
         boolean var4x;
         if (!this.a(var1, var2, var3x)) {
            var4x = true;
         } else {
            var4x = false;
         }

         return var4x;
      });
      var3.sort(Comparator.comparingDouble(var1x -> {
         Vec3 var2x = Vec3.atCenterOf(var1x);
         return var1.distanceToSqr(var2x);
      }));
      BlockPos var4;
      if (var3.isEmpty()) {
         var4 = null;
      } else {
         var4 = ((BlockPos)var3.getFirst()).immutable();
      }

      return var4;
   }

   private List<BlockPos> a_List_LocalPlayer(LocalPlayer var1) {
      LinkedHashSet var2 = new LinkedHashSet();
      BlockPos var3 = var1.blockPosition();
      Direction var4 = var1.getDirection();
      LinkedHashSet var5 = var2;
      BlockPos var6 = var3;
      Direction var7 = var4;
      BlockPos var8 = var6.relative(var7);
      var5.add(var8);
      LinkedHashSet var9 = var2;
      BlockPos var10 = var3;
      Direction var11 = var4;
      BlockPos var12 = var10.relative(var11).below();
      var9.add(var12);

      for (Direction var14 : Plane.HORIZONTAL) {
         LinkedHashSet var15 = var2;
         BlockPos var16 = var3;
         Direction var17 = var14;
         BlockPos var18 = var16.relative(var17);
         var15.add(var18);
      }

      for (Direction var20 : Plane.HORIZONTAL) {
         LinkedHashSet var21 = var2;
         BlockPos var22 = var3;
         Direction var23 = var20;
         BlockPos var24 = var22.relative(var23).below();
         var21.add(var24);
      }

      LinkedHashSet var25 = var2;
      BlockPos var26 = var3.below();
      var25.add(var26);
      LinkedHashSet var27 = var2;
      return new ArrayList<>(var27);
   }

   private boolean a(LocalPlayer var1, ClientLevel var2, BlockPos var3) {
      if (var2.isOutsideBuildHeight(var3)) {
         return false;
      }

      BlockState var4 = var2.getBlockState(var3);
      if (!var4.isAir() && !var4.canBeReplaced()) {
         return false;
      }

      Vec3 var5 = var1.getEyePosition();
      Vec3 var6 = Vec3.atCenterOf(var3);
      double var7 = var5.distanceToSqr(var6);
      double var9 = this.range.getValue().doubleValue();
      double var11 = this.range.getValue().doubleValue();
      double var13 = var9 * var11;
      if (var7 > var13) {
         return false;
      }

      AABB var15 = new AABB(var3);
      AABB var16 = var1.getBoundingBox();
      AABB var17 = var15;
      if (var16.intersects(var17)) {
         return false;
      }

      AABB var18 = var15;
      if (!var2.getEntitiesOfClass(Entity.class, var18, var0 -> {
         boolean var1x;
         if (!(var0 instanceof ItemEntity)) {
            var1x = true;
         } else {
            var1x = false;
         }

         return var1x;
      }).isEmpty()) {
         return false;
      }

      if (!this.airPlace.getValue()) {
         boolean var19 = this.strictDirection.getValue();
         if (BlockInteractionUtil.b_Direction_BlockPos_bool(var3, var19) == null) {
            return false;
         }
      }

      return true;
   }

   private boolean b(LocalPlayer var1, ClientLevel var2, BlockPos var3) {
      if (!var2.getBlockState(var3).is(Blocks.ENDER_CHEST)) {
         return false;
      }

      double var4 = this.mineRange.getValue().doubleValue();
      double var6 = this.wallsRange.getValue().doubleValue();
      if (!this.a(var1, var2, var3, var4, var6)) {
         return false;
      }

      boolean var8;
      if (this.strictDirection.getValue() && this.c(var1, var2, var3) == null) {
         var8 = false;
      } else {
         var8 = true;
      }

      return var8;
   }

   private boolean a(LocalPlayer var1, ClientLevel var2, BlockPos var3, double var4, double var6) {
      Vec3 var8 = var1.getEyePosition();
      Vec3 var9 = Vec3.atCenterOf(var3);
      Vec3 var10 = var8;
      Vec3 var11 = var9;
      double var12 = var10.distanceTo(var11);
      double var14 = var4;
      if (var12 <= var14) {
         Vec3 var16 = var8;
         Vec3 var17 = var9;
         if (this.a_bool_LocalPlayer_ClientLevel_Vec3_Vec3(var1, var2, var16, var17)) {
            return true;
         }
      }

      double var18 = var12;
      double var20 = var6;
      boolean var22;
      if (var18 <= var20) {
         var22 = true;
      } else {
         var22 = false;
      }

      return var22;
   }

   private boolean a_bool_LocalPlayer_ClientLevel_Vec3_Vec3(LocalPlayer var1, ClientLevel var2, Vec3 var3, Vec3 var4) {
      boolean var5;
      if (var2.clip(new ClipContext(var3, var4, Block.COLLIDER, Fluid.NONE, var1)).getType() == Type.MISS) {
         var5 = true;
      } else {
         var5 = false;
      }

      return var5;
   }

   private Direction c(LocalPlayer var1, ClientLevel var2, BlockPos var3) {
      Vec3 var4 = var1.getEyePosition();
      Direction var5 = null;
      double var6 = Double.MAX_VALUE;
      Direction[] var8 = Direction.values();
      int var9 = var8.length;
      int var10 = 0;

      while (true) {
         int var11 = var10;
         int var12 = var9;
         if (var11 >= var12) {
            return var5;
         }

         Direction[] var13 = var8;
         int var14 = var10;
         Direction var15 = var13[var14];
         Vec3 var16 = this.a_Vec3_BlockPos_Direction(var3, var15);
         Vec3 var17 = var4;
         Vec3 var18 = var16;
         double var19 = var17.distanceTo(var18);
         double var21 = var6;
         label19:
         if (!(var19 >= var21)) {
            if (this.strictDirection.getValue()) {
               Vec3 var23 = var4;
               Vec3 var24 = var16;
               if (!this.a_bool_LocalPlayer_ClientLevel_Vec3_Vec3(var1, var2, var23, var24)) {
                  break label19;
               }
            }

            var6 = var19;
            var5 = var15;
         }

         var10++;
      }
   }

   private Vec3 a_Vec3_BlockPos_Direction(BlockPos var1, Direction var2) {
      Vec3 var3 = Vec3.atCenterOf(var1);
      double var4 = var2.getStepX();
      double var6 = 0.5;
      double var8 = var4 * var6;
      double var10 = var2.getStepY();
      double var12 = 0.5;
      double var14 = var10 * var12;
      double var16 = var2.getStepZ();
      double var18 = 0.5;
      double var20 = var16 * var18;
      return var3.add(var8, var14, var20);
   }

   private int b(LocalPlayer var1) {
      byte var2;
      if (!this.switchSetting.getValue().equalsIgnoreCase("AltSwap") && !this.switchSetting.getValue().equalsIgnoreCase("AltPickup")) {
         var2 = 8;
      } else {
         var2 = 35;
      }

      byte var3 = var2;
      int var4 = var1.getInventory().getSelectedSlot();
      Inventory var5 = var1.getInventory();
      int var6 = var4;
      if (var5.getItem(var6).is(Items.ENDER_CHEST)) {
         return var4;
      }

      byte var7 = var3;
      return InventoryUtil.a_int_Item_int_int(Items.ENDER_CHEST, 0, var7);
   }

   @Override
   public String getDisplayInfo() {
      if (this.y != null) {
         String var1 = String.valueOf(ChatFormatting.GREEN);
         String var2 = this.z.name();
         String var3 = String.valueOf(ChatFormatting.RESET);
         return var1 + var2 + var3;
      } else {
         return "Place";
      }
   }

   enum Stage {
      Place,
      Mine;
   }
}
