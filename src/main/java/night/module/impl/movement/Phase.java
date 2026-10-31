package night.module.impl.movement;

import java.util.List;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.CarpetBlock;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.ScaffoldingBlock;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.TallFlowerBlock;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.TripWireBlock;
import net.minecraft.world.level.block.TripWireHookBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.WebBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import night.Night;
import night.manager.ChatManager;
import night.module.Module;
import night.module.ModuleInfo;
import night.module.Module.Category;
import night.module.access.SprintAccess;
import night.module.impl.player.MultiTask;
import night.setting.impl.BooleanSetting;
import night.setting.impl.ModeSetting;
import night.setting.impl.BooleanSetting.BooleanCondition;
import night.setting.impl.ModeSetting.ModeCondition;
import night.util.inventory.InventoryUtil;
import night.util.network.PacketPredictionUtil;
import night.util.world.BlockInteractionUtil;
import night.util.world.PacketAndRenderUtil;

@ModuleInfo(
   name = "Phase",
   description = "Throws an ender pearl to phase into blocks. KingMC Crawl mode requires crawling away from the target block before throwing.",
   category = Category.MOVEMENT
)
public class Phase extends Module {
   public ModeSetting logic;
   public ModeSetting swap;
   public BooleanSetting crawl;
   public BooleanSetting antiVoid;
   public BooleanSetting debug;
   public BooleanSetting s121;
   public BooleanSetting place;
   public BooleanSetting placeRender;
   public ModeSetting nCPMode;
   public ModeSetting switchSetting;
   public BooleanSetting autoRemove;
   private static final double p = 0.5;
   private static final double q = 0.5;
   private int r;
   private BlockPos s;

   public Phase() {
      String[] var1 = new String[]{"KingMC", "Grim", "NCP"};
      this.logic = new ModeSetting(
         "Logic",
         "Phase bypass targeting logic: KingMC (bedrock = Grim behavior, crawl requires crawling away from block before throwing), Grim, or NCP.",
         "KingMC",
         var1
      );
      this.swap = new ModeSetting("Swap", "The mode that will be used for switching to the pearl before throwing it.", "Silent", InventoryUtil.a);
      this.crawl = new BooleanSetting("Crawl", "Crawl phase mode. In KingMC mode, crawl away from the target block before throwing.", false);
      this.antiVoid = new BooleanSetting("AntiVoid", "Prevents phasing into or through blocks that lead directly into the void.", true);
      this.debug = new BooleanSetting(
         "Debug", "Chat-prints pos/yaw/pitch/target for every throw, so a failed (popped-up) attempt can be matched back to its exact numbers.", false
      );
      this.s121 = new BooleanSetting(
         "1.21+",
         "For 1.21.2+ servers where pearls keep momentum: aim off camera only and freeze horizontal movement through the teleport so held keys don't pop you onto the block.",
         false
      );
      this.place = new BooleanSetting("Place", "Places a blastproof block at the phase target if it is empty before throwing the pearl.", false);
      BooleanSetting var2 = this.place;
      this.placeRender = new BooleanSetting("PlaceRender", "Renders the placed block preview.", new BooleanCondition(var2, true), true);
      ModeSetting var4 = this.logic;
      String[] var5 = new String[]{"NCP"};
      ModeCondition var3 = new ModeCondition(var4, var5);
      String[] var6 = new String[]{"FireCharge", "FlintAndSteel", "Cobweb"};
      this.nCPMode = new ModeSetting("NCP-Mode", "Item used to bypass pearl distance checks on NCP.", var3, "FireCharge", var6);
      ModeSetting var8 = this.logic;
      String[] var9 = new String[]{"NCP"};
      ModeCondition var7 = new ModeCondition(var8, var9);
      String[] var10 = new String[]{"Normal", "Silent", "AltPickup", "AltSwap"};
      this.switchSetting = new ModeSetting("Switch", "The mode that will be used for switching to the bypass item.", var7, "Silent", var10);
      ModeSetting var11 = this.logic;
      String[] var12 = new String[]{"NCP"};
      this.autoRemove = new BooleanSetting(
         "AutoRemove", "Automatically removes the bypass block/fire once you have phased.", new ModeCondition(var11, var12), true
      );
      this.r = 0;
      this.s = null;
   }

   private boolean a_bool_Entity(Entity var1) {
      if (var1.getInBlockState().is(Blocks.COBWEB)) {
         return true;
      }

      double var2 = var1.getBoundingBox().minX;
      double var4 = var1.getBoundingBox().minY;
      double var6 = var1.getBoundingBox().minZ;
      BlockPos var8 = BlockPos.containing(var2, var4, var6);
      double var9 = var1.getBoundingBox().maxX;
      double var11 = var1.getBoundingBox().maxY;
      double var13 = var1.getBoundingBox().maxZ;
      BlockPos var15 = BlockPos.containing(var9, var11, var13);

      for (BlockPos var17 : BlockPos.betweenClosed(var8, var15)) {
         Level var18 = var1.level();
         BlockPos var19 = var17;
         if (var18.getBlockState(var19).is(Blocks.COBWEB)) {
            return true;
         }
      }

      return false;
   }

   private boolean b_bool_Entity(Entity var1) {
      if (var1.getInBlockState().is(Blocks.LADDER)) {
         return true;
      }

      double var2 = var1.getBoundingBox().minX;
      double var4 = var1.getBoundingBox().minY;
      double var6 = var1.getBoundingBox().minZ;
      BlockPos var8 = BlockPos.containing(var2, var4, var6);
      double var9 = var1.getBoundingBox().maxX;
      double var11 = var1.getBoundingBox().maxY;
      double var13 = var1.getBoundingBox().maxZ;
      BlockPos var15 = BlockPos.containing(var9, var11, var13);

      for (BlockPos var17 : BlockPos.betweenClosed(var8, var15)) {
         Level var18 = var1.level();
         BlockPos var19 = var17;
         if (var18.getBlockState(var19).is(Blocks.LADDER)) {
            return true;
         }
      }

      return false;
   }

   private boolean a_bool_BlockPos(BlockPos var1) {
      if (mc.level == null) {
         return true;
      }

      BlockState var2 = mc.level.getBlockState(var1);
      return this.a_bool_BlockState_BlockPos(var2, var1);
   }

   private boolean a_bool_BlockState_BlockPos(BlockState var1, BlockPos var2) {
      if (var1 != null && !var1.isAir() && !var1.canBeReplaced()) {
         if (mc.level != null) {
            ClientLevel var3 = mc.level;
            VoxelShape var4 = var1.getCollisionShape(var3, var2);
            if (var4.isEmpty()) {
               return true;
            }

            if (mc.player != null) {
               double var5 = mc.player.getBoundingBox().minY;
               double var7 = var2.getY();
               double var9 = var4.max(Axis.Y);
               double var11 = var7 + var9;
               double var13 = var5;
               double var15 = var11;
               double var17 = 0.125;
               double var19 = var15 - var17;
               if (var13 >= var19) {
                  return true;
               }
            }
         }

         Block var21 = var1.getBlock();
         if (!(var21 instanceof VineBlock)
            && !(var21 instanceof BaseRailBlock)
            && !(var21 instanceof WebBlock)
            && !(var21 instanceof LadderBlock)
            && !(var21 instanceof ScaffoldingBlock)
            && !(var21 instanceof TorchBlock)
            && !(var21 instanceof CarpetBlock)
            && !(var21 instanceof FlowerBlock)
            && !(var21 instanceof TallFlowerBlock)
            && !(var21 instanceof FlowerPotBlock)
            && !(var21 instanceof MushroomBlock)
            && !(var21 instanceof ButtonBlock)
            && !(var21 instanceof LeverBlock)
            && !(var21 instanceof PressurePlateBlock)
            && !(var21 instanceof SignBlock)
            && !(var21 instanceof BannerBlock)
            && !(var21 instanceof TripWireBlock)
            && !(var21 instanceof TripWireHookBlock)) {
            try {
               Block var22 = var21;
               Identifier var23 = BuiltInRegistries.BLOCK.getKey(var22);
               if (var23 != null) {
                  String var24 = var23.getPath().toLowerCase();
                  if (var24.contains("vine")
                     || var24.contains("rail")
                     || var24.contains("petal")
                     || var24.contains("litter")
                     || var24.contains("flower")
                     || var24.contains("grass")
                     || var24.contains("fern")
                     || var24.contains("sapling")
                     || var24.contains("mushroom")
                     || var24.contains("fungus")
                     || var24.contains("roots")
                     || var24.contains("sprouts")
                     || var24.contains("bush")
                     || var24.contains("lichen")
                     || var24.contains("sculk_vein")
                     || var24.contains("moss")
                     || var24.contains("carpet")
                     || var24.contains("torch")
                     || var24.contains("wire")
                     || var24.contains("button")
                     || var24.contains("lever")
                     || var24.contains("plate")
                     || var24.contains("sign")
                     || var24.contains("pad")
                     || var24.contains("sugar_cane")
                     || var24.contains("kelp")
                     || var24.contains("seagrass")) {
                     return true;
                  }
               }
            } catch (Exception var26) {
            }

            return false;
         } else {
            return true;
         }
      } else {
         return true;
      }
   }

   private boolean v() {
      if (mc.player == null) {
         return false;
      }

      boolean var1;
      if (mc.player.getPose() != Pose.SWIMMING && !mc.player.isVisuallyCrawling() && !(mc.player.getEyeHeight() <= 1.0F)) {
         var1 = false;
      } else {
         var1 = true;
      }

      return var1;
   }

   @Override
   public void onEnable() {
      if (mc.player != null && mc.level != null) {
         BlockPos[] var1 = new BlockPos[2];
         BlockPos var2 = mc.player.blockPosition();
         var1[0] = var2;
         BlockPos var3 = mc.player.blockPosition().below();
         var1[1] = var3;
         BlockPos[] var4 = var1;
         int var5 = var4.length;
         int var6 = 0;

         while (true) {
            int var7 = var6;
            int var8 = var5;
            if (var7 >= var8) {
               boolean var18 = this.logic.getValue().equalsIgnoreCase("KingMC");
               boolean var19 = this.v();
               if (!this.crawl.getValue() && var19 && !var18) {
                  this.setEnabled(false);
                  return;
               }

               boolean var20;
               if (this.crawl.getValue() && var19) {
                  var20 = true;
               } else {
                  var20 = false;
               }

               boolean var21 = var20;
               boolean var22;
               if (var21 && var19) {
                  var22 = true;
               } else {
                  var22 = false;
               }

               boolean var23 = var22;
               LocalPlayer var24 = mc.player;
               boolean var25 = this.a_bool_Entity(var24);
               LocalPlayer var26 = mc.player;
               boolean var27 = this.b_bool_Entity(var26);
               if (!var23 && !var25 && !var27) {
                  BlockPos var28 = mc.player.blockPosition();
                  if (!this.a_bool_BlockPos(var28)) {
                     this.setEnabled(false);
                     return;
                  }
               }

               if (mc.player.isCrouching()) {
                  this.setEnabled(false);
                  return;
               }

               if (mc.player.getCooldowns().isOnCooldown(new ItemStack(Items.ENDER_PEARL))) {
                  this.setEnabled(false);
                  return;
               }

               MultiTask var29 = Night.moduleManager.getModule(MultiTask.class);
               boolean var30;
               if (var29 != null && var29.isEnabled() && var29.pearl.getValue() && mc.player.isUsingItem()) {
                  var30 = true;
               } else {
                  var30 = false;
               }

               String var31;
               if (var30) {
                  var31 = "AltSwap";
               } else {
                  var31 = this.swap.getValue();
               }

               String var32 = var31;
               Item var33 = Items.ENDER_PEARL;
               byte var34;
               if (!var32.equalsIgnoreCase("AltSwap") && !var32.equalsIgnoreCase("AltPickup")) {
                  var34 = 8;
               } else {
                  var34 = 35;
               }

               int var35 = InventoryUtil.a_int_Item_int_int(var33, 0, var34);
               int var36 = mc.player.getInventory().getSelectedSlot();
               boolean var37 = false;
               if (var35 == -1) {
                  String var38 = this.getName();
                  Night.chatManager.sendTaggedMessage("No pearls could be found in your hotbar.", var38);
                  this.setEnabled(false);
                  return;
               }

               mc.player.getYRot();
               mc.player.getXRot();
               BlockPos var39 = mc.player.blockPosition().below();
               float var40 = Mth.wrapDegrees(this.x());
               float var41 = (var40 % 90.0F + 90.0F) % 90.0F;
               float var42 = 90.0F - var41;
               boolean var43;
               if (Math.min(var41, var42) < 22.5F) {
                  var43 = true;
               } else {
                  var43 = false;
               }

               boolean var44 = var43;
               boolean var45 = this.w();
               Vec3 var47;
               if (var18 && !var45) {
                  if (var21) {
                     float var46 = var40;
                     var47 = this.b_Vec3_float(var46);
                  } else {
                     float var50 = var40;
                     var47 = this.a_Vec3_float(var50);
                  }
               } else {
                  Vec3 var49;
                  if (var44) {
                     float var48 = var40;
                     var49 = this.c_Vec3_float(var48);
                  } else {
                     float var51 = var40;
                     var49 = this.d(var51);
                  }

                  var47 = var49;
               }

               if (this.antiVoid.getValue()) {
                  Vec3 var52 = var47;
                  float var53 = var40;
                  if (this.a_bool_Vec3_float(var52, var53)) {
                     String var54 = this.getName();
                     Night.chatManager.sendTaggedMessage("Phase cancelled to prevent falling into the void.", var54);
                     if (mc.getSoundManager() != null) {
                        SoundManager var55 = mc.getSoundManager();
                        SimpleSoundInstance var56 = SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_PLING, 1.5F);
                        var55.play(var56);
                     }

                     this.setEnabled(false);
                     return;
                  }
               }

               Vec3 var57 = this.A();
               Vec3 var58 = var47;
               Vec3 var59 = var57;
               Vec3 var60 = var58.subtract(var59);
               float var61 = this.b_float_Vec3(var60);
               float var62 = var61;
               float var63 = var40;
               if (Math.abs(Mth.wrapDegrees(var62 - var63)) > 85.0F) {
                  var61 = var40;
               }

               float var64 = var61;
               Vec3 var65 = var60;
               float var66 = this.c_float_Vec3(var65);
               if (this.debug.getValue()) {
                  int var67 = this.r + 1;
                  this.r = var67;
                  ChatManager var68 = Night.chatManager;
                  Object[] var69 = new Object[12];
                  Integer var70 = this.r;
                  var69[0] = var70;
                  String var71 = this.logic.getValue();
                  var69[1] = var71;
                  String var72;
                  if (var18 && var45) {
                     var72 = "/Bedrock";
                  } else if (var18 && var21) {
                     var72 = "/Crawl";
                  } else if (var18) {
                     var72 = "/Standing";
                  } else {
                     var72 = "";
                  }

                  var69[2] = var72;
                  Double var73 = mc.player.getX();
                  var69[3] = var73;
                  Double var74 = mc.player.getY();
                  var69[4] = var74;
                  Double var75 = mc.player.getZ();
                  var69[5] = var75;
                  Float var76 = var64;
                  var69[6] = var76;
                  Float var77 = var66;
                  var69[7] = var77;
                  Double var78 = var47.x;
                  var69[8] = var78;
                  Double var79 = var47.y;
                  var69[9] = var79;
                  Double var80 = var47.z;
                  var69[10] = var80;
                  String var81;
                  if (var44) {
                     var81 = "straight";
                  } else {
                     var81 = "corner";
                  }

                  var69[11] = var81;
                  String var82 = String.format("#%d [%s%s] pos=(%.4f, %.4f, %.4f) yaw=%.2f pitch=%.2f target=(%.4f, %.4f, %.4f) sector=%s", var69);
                  String var83 = this.getName();
                  var68.sendTaggedMessage(var82, var83);
               }

               if (this.logic.getValue().equalsIgnoreCase("NCP")) {
                  BlockPos var84 = mc.player.blockPosition();
                  if (this.a_bool_BlockPos(var84) || var25 || var27) {
                     ClientLevel var85 = mc.level;
                     BlockPos var86 = var39;
                     if (!var85.getBlockState(var86).canBeReplaced()) {
                        Item var90 = switch (this.nCPMode.getValue()) {
                           case "FlintAndSteel" -> Items.FLINT_AND_STEEL;
                           case "Cobweb" -> Items.COBWEB;
                           default -> Items.FIRE_CHARGE;
                        };
                        int var91 = InventoryUtil.c;
                        byte var92;
                        if (!this.switchSetting.getValue().equalsIgnoreCase("AltSwap") && !this.switchSetting.getValue().equalsIgnoreCase("AltPickup")) {
                           var92 = 8;
                        } else {
                           var92 = 35;
                        }

                        int var93 = InventoryUtil.a_int_Item_int_int(var90, var91, var92);
                        if (var93 != -1) {
                           float var94 = var64;
                           Night.rotationManager.sendRotationPacket(var94, 90.0F, true);
                           String var95 = this.switchSetting.getValue();
                           int var96 = var93;
                           int var97 = var36;
                           InventoryUtil.a_bool_String_int_int(var95, var96, var97);
                           PacketPredictionUtil.a_void_SequencedPacketCreator(var1x -> {
                              Vec3 var3x = Vec3.atCenterOf(var39);
                              double var4x = 0.0;
                              double var6x = 1.0;
                              double var8x = 0.0;
                              Vec3 var10x = var3x.add(var4x, var6x, var8x);
                              BlockHitResult var2x = new BlockHitResult(var10x, Direction.UP, var39, false);
                              return new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, var2x, var1x);
                           });
                           ClientPacketListener var98 = mc.getConnection();
                           Packet var99 = PacketAndRenderUtil.a_Packet_InteractionHand(InteractionHand.MAIN_HAND);
                           var98.send(var99);
                           String var100 = this.switchSetting.getValue();
                           int var101 = var93;
                           int var102 = var36;
                           InventoryUtil.b(var100, var101, var102);
                           var37 = true;
                        }
                     }
                  }
               }

               if (this.place.getValue()) {
                  Vec3 var103 = var47;
                  float var104 = var40;
                  boolean var105 = var21;
                  BlockPos var106 = this.a_BlockPos_Vec3_float_bool(var103, var104, var105);
                  label194:
                  if (var106 != null) {
                     ClientLevel var107 = mc.level;
                     BlockPos var108 = var106;
                     BlockState var109 = var107.getBlockState(var108);
                     if (!var109.canBeReplaced() && !var109.isAir()) {
                        BlockState var110 = var109;
                        BlockPos var111 = var106;
                        if (!this.a_bool_BlockState_BlockPos(var110, var111)) {
                           break label194;
                        }
                     }

                     BlockPos var112 = var106;
                     if (this.b_bool_BlockPos(var112)) {
                        if (this.debug.getValue()) {
                           Object[] var113 = new Object[3];
                           Integer var114 = var106.getX();
                           var113[0] = var114;
                           Integer var115 = var106.getY();
                           var113[1] = var115;
                           Integer var116 = var106.getZ();
                           var113[2] = var116;
                           String var117 = String.format("Phase cancelled: player is stepping on target position at (%d, %d, %d)", var113);
                           String var118 = this.getName();
                           Night.chatManager.sendTaggedMessage(var117, var118);
                        }

                        this.setEnabled(false);
                        return;
                     }

                     BlockPos var119 = var106;
                     if (!this.c_bool_BlockPos(var119)) {
                        this.setEnabled(false);
                        return;
                     }

                     var36 = mc.player.getInventory().getSelectedSlot();
                  }
               }

               float var120 = var64;
               float var121 = var66;
               Vec3 var122 = var57;
               PearlAngle var123 = this.a_PearlAngle_float_float_Vec3(var120, var121, var122);
               double var124;
               if (var123 == null) {
                  var124 = Double.NaN;
               } else {
                  Vec3 var126 = var123.vec();
                  var124 = this.a_double_Vec3(var126);
               }

               double var127 = var124;
               if (this.debug.getValue()) {
                  ChatManager var129 = Night.chatManager;
                  String var134;
                  if (var123 == null) {
                     Object[] var130 = new Object[3];
                     Double var131 = var57.x;
                     var130[0] = var131;
                     Double var132 = var57.y;
                     var130[1] = var132;
                     Double var133 = var57.z;
                     var130[2] = var133;
                     var134 = String.format("predict: no hit in 20 ticks, shooterVel=(%.3f, %.3f, %.3f)", var130);
                  } else {
                     Object[] var135 = new Object[8];
                     Integer var136 = var123.count();
                     var135[0] = var136;
                     Double var137 = var123.vec().x;
                     var135[1] = var137;
                     Double var138 = var123.vec().y;
                     var135[2] = var138;
                     Double var139 = var123.vec().z;
                     var135[3] = var139;
                     double var140 = var127;
                     double var142 = mc.player.getY();
                     Double var144 = var140 - var142;
                     var135[4] = var144;
                     Double var145 = var57.x;
                     var135[5] = var145;
                     Double var146 = var57.y;
                     var135[6] = var146;
                     Double var147 = var57.z;
                     var135[7] = var147;
                     var134 = String.format("predict: hit tick %d, tp=(%.4f, %.4f, %.4f), land +%.3f, shooterVel=(%.3f, %.3f, %.3f)", var135);
                  }

                  String var148 = this.getName();
                  var129.sendTaggedMessage(var134, var148);
               }

               if (var123 != null) {
                  double var149 = var127;
                  double var151 = mc.player.getY();
                  double var153 = 0.5;
                  double var155 = var151 + var153;
                  if (!(var149 > var155)) {
                     float var168 = var64;
                     float var169 = var66;
                     Night.rotationManager.sendRotationPacket(var168, var169, true);
                     String var170 = var32;
                     int var171 = var35;
                     int var172 = var36;
                     InventoryUtil.a_bool_String_int_int(var170, var171, var172);
                     float var173 = var64;
                     float var174 = var66;
                     PacketPredictionUtil.a_void_SequencedPacketCreator(var2x -> new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, var2x, var173, var174));
                     ClientPacketListener var175 = mc.getConnection();
                     Packet var176 = PacketAndRenderUtil.a_Packet_InteractionHand(InteractionHand.MAIN_HAND);
                     var175.send(var176);
                     String var177 = var32;
                     int var178 = var35;
                     int var179 = var36;
                     InventoryUtil.b(var177, var178, var179);
                     if (this.debug.getValue()) {
                        double var180 = mc.player.getY();
                        Night.worldStateManager.b = var180;
                        long var182 = System.currentTimeMillis();
                        long var184 = 3000L;
                        long var186 = var182 + var184;
                        Night.worldStateManager.a = var186;
                     }

                     if (var37 && this.autoRemove.getValue()) {
                        PacketPredictionUtil.a_void_SequencedPacketCreator(var0 -> {
                           BlockPos var1x = mc.player.blockPosition();
                           return new ServerboundPlayerActionPacket(Action.START_DESTROY_BLOCK, var1x, Direction.UP, var0);
                        });
                        PacketPredictionUtil.a_void_SequencedPacketCreator(var0 -> {
                           BlockPos var1x = mc.player.blockPosition();
                           return new ServerboundPlayerActionPacket(Action.STOP_DESTROY_BLOCK, var1x, Direction.UP, var0);
                        });
                        ClientPacketListener var188 = mc.getConnection();
                        Packet var189 = PacketAndRenderUtil.a_Packet_InteractionHand(InteractionHand.MAIN_HAND);
                        var188.send(var189);
                     }

                     if (this.s121.getValue()) {
                        LocalPlayer var190 = mc.player;
                        double var191 = 0.0;
                        double var193 = mc.player.getDeltaMovement().y;
                        double var195 = 0.0;
                        var190.setDeltaMovement(var191, var193, var195);
                     }

                     this.setEnabled(false);
                     return;
                  }
               }

               ChatManager var157 = Night.chatManager;
               String var158;
               if (var123 == null) {
                  var158 = "Phase cancelled: pearl wouldn't land.";
               } else {
                  Object[] var159 = new Object[3];
                  double var160 = var127;
                  double var162 = mc.player.getY();
                  Double var164 = var160 - var162;
                  var159[0] = var164;
                  Integer var165 = var123.count();
                  var159[1] = var165;
                  Double var166 = var57.horizontalDistance();
                  var159[2] = var166;
                  var158 = String.format("Phase cancelled: would pop up +%.2f (hit tick %d, server vel %.2f).", var159);
               }

               String var167 = this.getName();
               var157.sendTaggedMessage(var158, var167);
               this.setEnabled(false);
               return;
            }

            BlockPos[] var9 = var4;
            int var10 = var6;
            BlockPos var11 = var9[var10];
            ClientLevel var12 = mc.level;
            BlockPos var13 = var11;
            if (var12.getBlockState(var13).is(Blocks.SCAFFOLDING)) {
               PacketPredictionUtil.a_void_SequencedPacketCreator(
                  var1x -> new ServerboundPlayerActionPacket(Action.START_DESTROY_BLOCK, var11, Direction.UP, var1x)
               );
               PacketPredictionUtil.a_void_SequencedPacketCreator(
                  var1x -> new ServerboundPlayerActionPacket(Action.STOP_DESTROY_BLOCK, var11, Direction.UP, var1x)
               );
               ClientPacketListener var14 = mc.getConnection();
               Packet var15 = PacketAndRenderUtil.a_Packet_InteractionHand(InteractionHand.MAIN_HAND);
               var14.send(var15);
               ClientLevel var16 = mc.level;
               BlockPos var17 = var11;
               var16.removeBlock(var17, false);
            }

            var6++;
         }
      } else {
         this.setEnabled(false);
      }
   }

   private boolean w() {
      if (mc.player != null && mc.level != null) {
         double var1 = mc.player.getBoundingBox().minY;
         double var3 = mc.player.getX();
         double var5 = var1;
         double var7 = 0.1;
         double var9 = var5 - var7;
         double var11 = mc.player.getZ();
         BlockPos var13 = BlockPos.containing(var3, var9, var11);
         ClientLevel var14 = mc.level;
         BlockPos var15 = var13;
         if (var14.getBlockState(var15).is(Blocks.BEDROCK)) {
            return true;
         }

         ClientLevel var16 = mc.level;
         BlockPos var17 = mc.player.blockPosition().below();
         if (var16.getBlockState(var17).is(Blocks.BEDROCK)) {
            return true;
         }

         ClientLevel var18 = mc.level;
         BlockPos var19 = mc.player.blockPosition();
         return var18.getBlockState(var19).is(Blocks.BEDROCK);
      } else {
         return false;
      }
   }

   private float x() {
      if (mc.player == null) {
         return 0.0F;
      }

      if (Night.moduleManager != null) {
         SprintAccess var1 = (SprintAccess)Night.moduleManager.getModuleByName("Sprint");
         if (var1 != null && var1.isGrimSprintActive()) {
            return var1.getSprintYaw();
         }
      }

      return mc.player.getYRot();
   }

   private Vec3 a_Vec3_float(float var1) {
      double var2 = mc.player.getX();
      double var4 = mc.player.getY();
      double var6 = mc.player.getZ();
      double var8 = Math.toRadians(var1);
      double var10 = -Math.sin(var8);
      double var12 = 0.0;
      double var14 = Math.cos(var8);
      Vec3 var16 = new Vec3(var10, var12, var14).normalize();
      float var17 = (var1 % 90.0F + 90.0F) % 90.0F;
      float var18 = 90.0F - var17;
      boolean var19;
      if (Math.min(var17, var18) < 22.5F) {
         var19 = true;
      } else {
         var19 = false;
      }

      boolean var20 = var19;
      double var21 = 0.165;
      if (var20) {
         double var23 = Math.abs(var16.z);
         double var25 = Math.abs(var16.x);
         boolean var27;
         if (var23 > var25) {
            var27 = true;
         } else {
            var27 = false;
         }

         if (var27) {
            double var154 = var16.z;
            double var156 = 0.0;
            double var162;
            if (var154 > var156) {
               double var158 = Math.floor(var6);
               double var160 = 1.0;
               var162 = var158 + var160;
            } else {
               var162 = Math.floor(var6);
            }

            double var228 = var162;
            double var230 = var6;
            double var232 = Math.abs(var228 - var230);
            double var234 = var16.z;
            double var236 = 0.0;
            double var266;
            if (var234 > var236) {
               double var238 = var232;
               double var240 = 0.165;
               double var246;
               if (var238 > var240) {
                  double var242 = var228;
                  double var244 = 0.165;
                  var246 = var242 - var244;
               } else {
                  double var258 = var6;
                  double var260 = var232;
                  double var262 = 0.5;
                  double var264 = var260 * var262;
                  var246 = var258 + var264;
               }

               var266 = var246;
            } else {
               double var248 = var232;
               double var250 = 0.165;
               double var256;
               if (var248 > var250) {
                  double var252 = var228;
                  double var254 = 0.165;
                  var256 = var252 + var254;
               } else {
                  double var268 = var6;
                  double var270 = var232;
                  double var272 = 0.5;
                  double var274 = var270 * var272;
                  var256 = var268 - var274;
               }

               var266 = var256;
            }

            double var276 = var2;
            double var278 = var4;
            double var280 = var266;
            return new Vec3(var276, var278, var280);
         } else {
            double var164 = var16.x;
            double var166 = 0.0;
            double var172;
            if (var164 > var166) {
               double var168 = Math.floor(var2);
               double var170 = 1.0;
               var172 = var168 + var170;
            } else {
               var172 = Math.floor(var2);
            }

            double var174 = var172;
            double var176 = var2;
            double var178 = Math.abs(var174 - var176);
            double var180 = var16.x;
            double var182 = 0.0;
            double var212;
            if (var180 > var182) {
               double var184 = var178;
               double var186 = 0.165;
               double var192;
               if (var184 > var186) {
                  double var188 = var174;
                  double var190 = 0.165;
                  var192 = var188 - var190;
               } else {
                  double var204 = var2;
                  double var206 = var178;
                  double var208 = 0.5;
                  double var210 = var206 * var208;
                  var192 = var204 + var210;
               }

               var212 = var192;
            } else {
               double var194 = var178;
               double var196 = 0.165;
               double var202;
               if (var194 > var196) {
                  double var198 = var174;
                  double var200 = 0.165;
                  var202 = var198 + var200;
               } else {
                  double var214 = var2;
                  double var216 = var178;
                  double var218 = 0.5;
                  double var220 = var216 * var218;
                  var202 = var214 - var220;
               }

               var212 = var202;
            }

            double var222 = var212;
            double var224 = var4;
            double var226 = var6;
            return new Vec3(var222, var224, var226);
         }
      } else {
         double var28 = var16.x;
         double var30 = 0.0;
         double var36;
         if (var28 > var30) {
            double var32 = Math.floor(var2);
            double var34 = 1.0;
            var36 = var32 + var34;
         } else {
            var36 = Math.floor(var2);
         }

         double var38 = var36;
         double var40 = var16.z;
         double var42 = 0.0;
         double var48;
         if (var40 > var42) {
            double var44 = Math.floor(var6);
            double var46 = 1.0;
            var48 = var44 + var46;
         } else {
            var48 = Math.floor(var6);
         }

         double var50 = var48;
         double var52 = var38;
         double var54 = var2;
         double var56 = Math.abs(var52 - var54);
         double var58 = var50;
         double var60 = var6;
         double var62 = Math.abs(var58 - var60);
         double var64 = var16.x;
         double var66 = 0.0;
         double var86;
         if (var64 > var66) {
            double var68 = var56;
            double var70 = 0.165;
            double var76;
            if (var68 > var70) {
               double var72 = var38;
               double var74 = 0.165;
               var76 = var72 - var74;
            } else {
               double var78 = var2;
               double var80 = var56;
               double var82 = 0.5;
               double var84 = var80 * var82;
               var76 = var78 + var84;
            }

            var86 = var76;
         } else {
            double var88 = var56;
            double var90 = 0.165;
            double var96;
            if (var88 > var90) {
               double var92 = var38;
               double var94 = 0.165;
               var96 = var92 + var94;
            } else {
               double var98 = var2;
               double var100 = var56;
               double var102 = 0.5;
               double var104 = var100 * var102;
               var96 = var98 - var104;
            }

            var86 = var96;
         }

         double var106 = var16.z;
         double var108 = 0.0;
         double var138;
         if (var106 > var108) {
            double var110 = var62;
            double var112 = 0.165;
            double var118;
            if (var110 > var112) {
               double var114 = var50;
               double var116 = 0.165;
               var118 = var114 - var116;
            } else {
               double var130 = var6;
               double var132 = var62;
               double var134 = 0.5;
               double var136 = var132 * var134;
               var118 = var130 + var136;
            }

            var138 = var118;
         } else {
            double var120 = var62;
            double var122 = 0.165;
            double var128;
            if (var120 > var122) {
               double var124 = var50;
               double var126 = 0.165;
               var128 = var124 + var126;
            } else {
               double var140 = var6;
               double var142 = var62;
               double var144 = 0.5;
               double var146 = var142 * var144;
               var128 = var140 - var146;
            }

            var138 = var128;
         }

         double var148 = var86;
         double var150 = var4;
         double var152 = var138;
         return new Vec3(var148, var150, var152);
      }
   }

   private Vec3 b_Vec3_float(float var1) {
      Vec3 var2 = this.z();
      float var3 = mc.player.getYRot();
      double var4 = Math.toRadians(var3);
      double var6 = -Math.sin(var4);
      double var8 = 0.0;
      double var10 = Math.cos(var4);
      Vec3 var12 = new Vec3(var6, var8, var10).normalize();
      ClientLevel var13 = mc.level;
      Vec3 var14 = var2;
      Vec3 var15 = var2;
      Vec3 var16 = var12;
      double var17 = 4.0;
      Vec3 var19 = var16.scale(var17);
      Vec3 var20 = var15.add(var19);
      LocalPlayer var21 = mc.player;
      BlockHitResult var22 = var13.clip(new ClipContext(var14, var20, net.minecraft.world.level.ClipContext.Block.COLLIDER, Fluid.NONE, var21));
      Vec3 var23;
      if (var22 != null && var22.getType() == Type.BLOCK) {
         var1 = var3;
         var23 = var12;
      } else {
         double var24 = Math.toRadians(var1);
         double var26 = -Math.sin(var24);
         double var28 = 0.0;
         double var30 = Math.cos(var24);
         var23 = new Vec3(var26, var28, var30).normalize();
         ClientLevel var32 = mc.level;
         Vec3 var33 = var2;
         Vec3 var34 = var2;
         Vec3 var35 = var23;
         double var36 = 4.0;
         Vec3 var38 = var35.scale(var36);
         Vec3 var39 = var34.add(var38);
         LocalPlayer var40 = mc.player;
         var22 = var32.clip(new ClipContext(var33, var39, net.minecraft.world.level.ClipContext.Block.COLLIDER, Fluid.NONE, var40));
      }

      Object var41 = null;
      Object var42 = null;
      Vec3 var43 = null;
      BlockPos var45;
      Direction var46;
      if (var22 != null && var22.getType() == Type.BLOCK) {
         BlockHitResult var44 = var22;
         var45 = var44.getBlockPos();
         var46 = var44.getDirection();
         var43 = var44.getLocation();
      } else {
         Direction var47 = Direction.fromYRot(var1);
         BlockPos var48 = mc.player.blockPosition();
         Direction var49 = var47;
         var45 = var48.relative(var49, 1);
         var46 = var47.getOpposite();
      }

      BlockPos var50 = var45;
      this.s = var50;
      double var51 = var45.getY();
      double var53 = 0.52;
      double var55 = var51 + var53;
      float var57 = (var1 % 90.0F + 90.0F) % 90.0F;
      float var58 = 90.0F - var57;
      boolean var59;
      if (Math.min(var57, var58) < 22.5F) {
         var59 = true;
      } else {
         var59 = false;
      }

      if (var59 && var46 != null && var46.getAxis().isHorizontal()) {
         double var68;
         double var78;
         label53:
         if (var46.getAxis() == Axis.Z) {
            double var60;
            if (var46 == Direction.NORTH) {
               var60 = var45.getZ();
            } else {
               double var80 = var45.getZ();
               double var82 = 1.0;
               var60 = var80 + var82;
            }

            var78 = var60;
            if (var43 != null) {
               double var84 = var43.x;
               double var86 = Math.round(var43.x);
               double var88 = Math.abs(var84 - var86);
               double var90 = 0.25;
               if (var88 < var90) {
                  var68 = Math.round(var43.x);
                  break label53;
               }
            }

            double var92 = var45.getX();
            double var94 = 0.5;
            var68 = var92 + var94;
         } else {
            label72: {
               double var62;
               if (var46 == Direction.WEST) {
                  var62 = var45.getX();
               } else {
                  double var64 = var45.getX();
                  double var66 = 1.0;
                  var62 = var64 + var66;
               }

               var68 = var62;
               if (var43 != null) {
                  double var70 = var43.z;
                  double var72 = Math.round(var43.z);
                  double var74 = Math.abs(var70 - var72);
                  double var76 = 0.25;
                  if (var74 < var76) {
                     var78 = Math.round(var43.z);
                     break label72;
                  }
               }

               double var96 = var45.getZ();
               double var98 = 0.5;
               var78 = var96 + var98;
            }
         }

         double var100 = var68;
         double var102 = var55;
         double var104 = var78;
         return new Vec3(var100, var102, var104);
      } else {
         double var106;
         double var108;
         if (var43 != null) {
            var106 = Math.round(var43.x);
            var108 = Math.round(var43.z);
         } else {
            double var110 = mc.player.getX();
            double var112 = var23.x;
            var106 = Math.round(var110 + var112);
            double var114 = mc.player.getZ();
            double var116 = var23.z;
            var108 = Math.round(var114 + var116);
         }

         double var118 = var106;
         double var120 = var55;
         double var122 = var108;
         return new Vec3(var118, var120, var122);
      }
   }

   private Vec3 y() {
      double var1 = mc.player.getX();
      double var3 = mc.player.getZ();
      double var5;
      if (this.crawl.getValue() && this.v()) {
         var5 = mc.player.blockPosition().below().getY();
      } else {
         double var7 = mc.player.getY();
         double var9 = 0.5;
         var5 = var7 - var9;
      }

      double var11 = var5;
      double var13 = Math.round(var1);
      double var15 = Math.round(var3);
      double var17 = var13;
      double var19 = var1;
      double var21 = var17 - var19;
      double var23 = var15;
      double var25 = var3;
      double var27 = var23 - var25;
      double var29 = Math.abs(var21);
      double var31 = 0.5;
      if (var29 <= var31) {
         double var33 = Math.abs(var27);
         double var35 = 0.5;
         if (var33 <= var35) {
            double var37 = var1;
            double var39 = var21;
            double var41 = -0.5;
            double var43 = 0.5;
            double var45 = Mth.clamp(var39, var41, var43);
            double var47 = var37 + var45;
            double var49 = var11;
            double var51 = var3;
            double var53 = var27;
            double var55 = -0.5;
            double var57 = 0.5;
            double var59 = Mth.clamp(var53, var55, var57);
            double var61 = var51 + var59;
            return new Vec3(var47, var49, var61);
         }
      }

      double var63 = Math.PI / 13;
      double var65 = Math.PI / 4;
      double var67 = var1;
      double var69 = var1;
      double var71 = Math.floor(var1);
      double var73 = Math.PI / 13;
      double var75 = var71 + var73;
      double var77 = Math.floor(var1);
      double var79 = Math.PI / 4;
      double var81 = var77 + var79;
      double var83 = this.a_double_double_double_double(var69, var75, var81);
      double var85 = var1;
      double var87 = var83 - var85;
      double var89 = -0.2;
      double var91 = 0.2;
      double var93 = Mth.clamp(var87, var89, var91);
      double var95 = var67 + var93;
      double var97 = var3;
      double var99 = var3;
      double var101 = Math.floor(var3);
      double var103 = Math.PI / 13;
      double var105 = var101 + var103;
      double var107 = Math.floor(var3);
      double var109 = Math.PI / 4;
      double var111 = var107 + var109;
      double var113 = this.a_double_double_double_double(var99, var105, var111);
      double var115 = var3;
      double var117 = var113 - var115;
      double var119 = -0.2;
      double var121 = 0.2;
      double var123 = Mth.clamp(var117, var119, var121);
      double var125 = var97 + var123;
      double var127 = var95;
      double var129 = var11;
      double var131 = var125;
      return new Vec3(var127, var129, var131);
   }

   private double a_double_double_double_double(double var1, double var3, double var5) {
      double var7 = var1;
      double var9 = var3;
      double var11 = var7 - var9;
      double var13 = var5;
      double var15 = var1;
      double var17 = var13 - var15;
      double var19;
      if (var11 > var17) {
         var19 = var5;
      } else {
         var19 = var3;
      }

      return var19;
   }

   private Vec3 c_Vec3_float(float var1) {
      double var2 = mc.player.getX();
      double var4 = mc.player.getZ();
      double var6;
      if (this.crawl.getValue() && this.v()) {
         var6 = mc.player.blockPosition().below().getY();
      } else {
         double var8 = mc.player.getY();
         double var10 = 0.5;
         var6 = var8 - var10;
      }

      double var12 = var6;
      double var14 = Math.toRadians(var1);
      double var16 = -Math.sin(var14);
      double var18 = 0.0;
      double var20 = Math.cos(var14);
      Vec3 var22 = new Vec3(var16, var18, var20).normalize();
      double var23 = Math.abs(var22.z);
      double var25 = Math.abs(var22.x);
      boolean var27;
      if (var23 > var25) {
         var27 = true;
      } else {
         var27 = false;
      }

      if (var27) {
         double var28 = var22.z;
         double var30 = 0.0;
         double var36;
         if (var28 > var30) {
            double var32 = Math.floor(var4);
            double var34 = 1.0;
            var36 = var32 + var34;
         } else {
            var36 = Math.floor(var4);
         }

         double var38 = var36;
         double var40 = var2;
         double var42 = var12;
         double var44 = var38;
         return new Vec3(var40, var42, var44);
      } else {
         double var46 = var22.x;
         double var48 = 0.0;
         double var54;
         if (var46 > var48) {
            double var50 = Math.floor(var2);
            double var52 = 1.0;
            var54 = var50 + var52;
         } else {
            var54 = Math.floor(var2);
         }

         double var56 = var54;
         double var58 = var12;
         double var60 = var4;
         return new Vec3(var56, var58, var60);
      }
   }

   private Vec3 d(float var1) {
      double var2 = mc.player.getX();
      double var4 = mc.player.getZ();
      double var6;
      if (this.crawl.getValue() && this.v()) {
         var6 = mc.player.blockPosition().below().getY();
      } else {
         double var8 = mc.player.getY();
         double var10 = 0.5;
         var6 = var8 - var10;
      }

      double var12 = var6;
      double var14 = Math.toRadians(var1);
      double var16 = -Math.sin(var14);
      double var18 = 0.0;
      double var20 = Math.cos(var14);
      Vec3 var22 = new Vec3(var16, var18, var20).normalize();
      double var23 = var22.x;
      double var25 = 0.0;
      double var31;
      if (var23 > var25) {
         double var27 = Math.floor(var2);
         double var29 = 1.0;
         var31 = var27 + var29;
      } else {
         var31 = Math.floor(var2);
      }

      double var33 = var31;
      double var35 = var22.z;
      double var37 = 0.0;
      double var43;
      if (var35 > var37) {
         double var39 = Math.floor(var4);
         double var41 = 1.0;
         var43 = var39 + var41;
      } else {
         var43 = Math.floor(var4);
      }

      double var45 = var43;
      double var47 = var33;
      double var49 = var12;
      double var51 = var45;
      return new Vec3(var47, var49, var51);
   }

   private Vec3 z() {
      if (mc.player == null) {
         return Vec3.ZERO;
      }

      double var1 = mc.player.getEyeY();
      double var3 = 0.1;
      double var5 = var1 - var3;
      double var7 = mc.player.getX();
      double var9 = var5;
      double var11 = mc.player.getZ();
      return new Vec3(var7, var9, var11);
   }

   private Vec3 A() {
      Vec3 var1;
      if (PacketPredictionUtil.b()) {
         var1 = Night.worldStateManager.getLastVelocity();
      } else {
         var1 = mc.player.getDeltaMovement();
      }

      Vec3 var2 = var1;
      double var3 = var2.x;
      double var5;
      if (mc.player.onGround()) {
         var5 = 0.0;
      } else {
         var5 = var2.y;
      }

      double var7 = var2.z;
      return new Vec3(var3, var5, var7);
   }

   private PearlAngle a_PearlAngle_float_float_Vec3(float var1, float var2, Vec3 var3) {
      boolean var4 = PacketPredictionUtil.b();
      float var5 = var1 * (float) (Math.PI / 180.0);
      float var6 = var2 * (float) (Math.PI / 180.0);
      float var7 = Mth.sin(var5);
      float var8 = 0.0F - var7;
      float var9 = Mth.cos(var6);
      double var10 = var8 * var9;
      float var12 = Mth.sin(var6);
      double var13 = 0.0F - var12;
      float var15 = Mth.cos(var5);
      float var16 = Mth.cos(var6);
      double var17 = var15 * var16;
      Vec3 var19 = new Vec3(var10, var13, var17).normalize();
      double var20 = 1.5;
      Vec3 var22 = var19.scale(var20).add(var3);
      Vec3 var23 = this.z();

      for (int var24 = 1; var24 <= 20; var24++) {
         if (!var4) {
            Vec3 var25 = var22;
            double var26 = 0.0;
            double var28 = 0.03;
            double var30 = 0.0;
            Vec3 var32 = var25.subtract(var26, var28, var30);
            double var33 = 0.99;
            var22 = var32.scale(var33);
         }

         ClientLevel var35 = mc.level;
         Vec3 var36 = var23;
         Vec3 var37 = var23;
         Vec3 var38 = var22;
         Vec3 var39 = var37.add(var38);
         LocalPlayer var40 = mc.player;
         if (var35.clip(new ClipContext(var36, var39, net.minecraft.world.level.ClipContext.Block.COLLIDER, Fluid.NONE, var40)).getType() != Type.MISS) {
            Vec3 var41 = var23;
            int var42 = var24;
            return new PearlAngle(var41, var42);
         }

         Vec3 var43 = var23;
         Vec3 var44 = var22;
         var23 = var43.add(var44);
         if (var4) {
            Vec3 var45 = var22;
            double var46 = 0.99;
            Vec3 var48 = var45.scale(var46);
            double var49 = 0.0;
            double var51 = 0.03;
            double var53 = 0.0;
            var22 = var48.subtract(var49, var51, var53);
         }
      }

      return null;
   }

   private double a_double_Vec3(Vec3 var1) {
      AABB var2 = mc.player.getBoundingBox();
      Vec3 var3 = mc.player.position();
      Vec3 var4 = var1.subtract(var3);
      AABB var5 = var2.move(var4);
      double var6 = var1.y;
      double var8 = mc.player.getY();
      double var10 = var6 - var8;
      double var12 = 1.0;
      double var14 = var10 + var12;
      double var16 = 0.0;
      if (var14 <= var16) {
         return var1.y;
      }

      double var18 = var1.y;
      AABB var20 = var5;
      ClientLevel var21 = mc.level;
      LocalPlayer var22 = mc.player;
      AABB var23 = var5;
      double var24 = 0.0;
      double var26 = -var14;
      double var28 = 0.0;
      AABB var30 = var23.expandTowards(var24, var26, var28);
      Iterable var31 = var21.getBlockCollisions(var22, var30);
      double var32 = -var14;
      double var34 = Shapes.collide(Axis.Y, var20, var31, var32);
      return var18 + var34;
   }

   private float b_float_Vec3(Vec3 var1) {
      Vec3 var2 = this.z();
      Vec3 var3 = var1.subtract(var2);
      double var4 = -var3.x;
      double var6 = var3.z;
      return (float)Math.toDegrees(Math.atan2(var4, var6));
   }

   private float c_float_Vec3(Vec3 var1) {
      Vec3 var2 = this.z();
      double var3 = var1.x;
      double var5 = var2.x;
      double var7 = var3 - var5;
      double var9 = var1.z;
      double var11 = var2.z;
      double var13 = var9 - var11;
      double var15 = Math.hypot(var7, var13);
      double var17 = var1.y;
      double var19 = var2.y;
      double var21 = var17 - var19;
      double var23 = var15;
      double var25 = 1.0E-4;
      if (var23 < var25) {
         double var27 = var21;
         double var29 = 0.0;
         float var31;
         if (var27 < var29) {
            var31 = 85.0F;
         } else {
            var31 = -85.0F;
         }

         return var31;
      } else {
         double var32 = -85.0;
         double var34 = 89.9;

         for (int var36 = 0; var36 < 30; var36++) {
            double var37 = var32;
            double var39 = var34;
            double var41 = var37 + var39;
            double var43 = 0.5;
            double var45 = var41 * var43;
            double var47 = var15;
            double var49 = a_double_double_double(var45, var47);
            double var51 = var21;
            if (var49 < var51) {
               var34 = var45;
            } else {
               var32 = var45;
            }
         }

         double var53 = var32;
         double var55 = var34;
         double var57 = var53 + var55;
         double var59 = 0.5;
         return (float)(var57 * var59);
      }
   }

   private static double a_double_double_double(double var0, double var2) {
      double var4 = Math.toRadians(var0);
      double var6 = 1.5;
      double var8 = Math.cos(var4);
      double var10 = var6 * var8;
      double var12 = -1.5;
      double var14 = Math.sin(var4);
      double var16 = var12 * var14;
      double var18 = 0.0;
      double var20 = 0.0;

      for (int var22 = 0; var22 < 100; var22++) {
         double var23 = var18;
         double var25 = var20;
         double var27 = var18;
         double var29 = var10;
         var18 = var27 + var29;
         double var31 = var20;
         double var33 = var16;
         var20 = var31 + var33;
         double var35 = var18;
         double var37 = var2;
         if (var35 >= var37) {
            double var39 = var2;
            double var41 = var23;
            double var43 = var39 - var41;
            double var45 = var18;
            double var47 = var23;
            double var49 = var45 - var47;
            double var51 = var43 / var49;
            double var53 = var25;
            double var55 = var20;
            double var57 = var25;
            double var59 = var55 - var57;
            double var61 = var51;
            double var63 = var59 * var61;
            return var53 + var63;
         }

         double var65 = var10;
         double var67 = 0.99;
         var10 = var65 * var67;
         double var69 = var16;
         double var71 = 0.99;
         double var73 = var69 * var71;
         double var75 = 0.03;
         var16 = var73 - var75;
      }

      return var20;
   }

   private boolean b(double var1, double var3, double var5) {
      if (mc.level == null) {
         return false;
      }

      int var7 = mc.level.getMinY();
      int var8 = (int)Math.floor(var3);
      int var9 = var7;
      if (var8 < var9) {
         return true;
      }

      int var10 = var8;

      while (true) {
         int var11 = var10;
         int var12 = var7;
         if (var11 < var12) {
            return true;
         }

         double var13 = var1;
         double var15 = var10;
         double var17 = var5;
         BlockPos var19 = BlockPos.containing(var13, var15, var17);
         ClientLevel var20 = mc.level;
         BlockPos var21 = var19;
         BlockState var22 = var20.getBlockState(var21);
         if (!var22.isAir()) {
            BlockState var23 = var22;
            ClientLevel var24 = mc.level;
            BlockPos var25 = var19;
            if (!var23.getCollisionShape(var24, var25).isEmpty()) {
               return false;
            }
         }

         var10 += -1;
      }
   }

   private boolean a_bool_Vec3_float(Vec3 var1, float var2) {
      if (mc.player != null && mc.level != null) {
         double var3 = mc.player.getX();
         double var5 = mc.player.getY();
         double var7 = mc.player.getZ();
         int var9 = mc.level.getMinY();
         double var10 = var5;
         double var12 = var9;
         if (var10 < var12) {
            return true;
         }

         double var14 = Math.toRadians(var2);
         double var16 = -Math.sin(var14);
         double var18 = 0.0;
         double var20 = Math.cos(var14);
         Vec3 var22 = new Vec3(var16, var18, var20).normalize();
         double var23 = var1.x;
         double var25 = var1.y;
         double var27 = var1.z;
         BlockPos var29 = BlockPos.containing(var23, var25, var27);
         double var30 = var3;
         double var32 = var22.x;
         double var34 = 0.5;
         double var36 = var32 * var34;
         double var38 = var30 + var36;
         double var40 = var5;
         double var42 = var7;
         double var44 = var22.z;
         double var46 = 0.5;
         double var48 = var44 * var46;
         double var50 = var42 + var48;
         BlockPos var52 = BlockPos.containing(var38, var40, var50);
         double var53 = var29.getX();
         double var55 = 0.5;
         double var57 = var53 + var55;
         double var59 = var1.y;
         double var61 = var29.getZ();
         double var63 = 0.5;
         double var65 = var61 + var63;
         boolean var67;
         if (!this.b(var57, var59, var65)) {
            var67 = true;
         } else {
            var67 = false;
         }

         boolean var68 = var67;
         double var69 = var52.getX();
         double var71 = 0.5;
         double var73 = var69 + var71;
         double var75 = var5;
         double var77 = var52.getZ();
         double var79 = 0.5;
         double var81 = var77 + var79;
         boolean var83;
         if (!this.b(var73, var75, var81)) {
            var83 = true;
         } else {
            var83 = false;
         }

         boolean var84 = var83;
         boolean var85;
         if (!var68 && !var84) {
            var85 = true;
         } else {
            var85 = false;
         }

         return var85;
      } else {
         return false;
      }
   }

   private boolean a_bool_Block(Block var1) {
      boolean var2;
      if (var1 != Blocks.OBSIDIAN
         && var1 != Blocks.CRYING_OBSIDIAN
         && var1 != Blocks.RESPAWN_ANCHOR
         && var1 != Blocks.NETHERITE_BLOCK
         && var1 != Blocks.ANCIENT_DEBRIS
         && var1 != Blocks.ENDER_CHEST
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

   private BlockPos a_BlockPos_Vec3_float_bool(Vec3 var1, float var2, boolean var3) {
      if (mc.player == null || mc.level == null) {
         return null;
      }

      if (var3 && this.s != null) {
         return this.s;
      }

      Direction var4 = Direction.fromYRot(var2);
      BlockPos var5 = mc.player.blockPosition();
      Direction var6 = var4;
      return var5.relative(var6);
   }

   private boolean b_bool_BlockPos(BlockPos var1) {
      if (mc.player == null) {
         return false;
      }

      AABB var2 = mc.player.getBoundingBox();
      AABB var3 = new AABB(var1);
      AABB var4 = var2;
      double var5 = 0.01;
      AABB var7 = var4.deflate(var5);
      AABB var8 = var3;
      if (var7.intersects(var8)) {
         return true;
      }

      boolean var29;
      label32: {
         AABB var9 = var2;
         double var10 = 0.01;
         AABB var12 = var9.deflate(var10);
         double var13 = var12.minX;
         double var15 = var3.maxX;
         if (var13 < var15) {
            double var17 = var12.maxX;
            double var19 = var3.minX;
            if (var17 > var19) {
               double var21 = var12.minZ;
               double var23 = var3.maxZ;
               if (var21 < var23) {
                  double var25 = var12.maxZ;
                  double var27 = var3.minZ;
                  if (var25 > var27) {
                     var29 = true;
                     break label32;
                  }
               }
            }
         }

         var29 = false;
      }

      if (var29) {
         double var30 = var2.minY;
         double var32 = var3.minY;
         double var34 = 0.05;
         double var36 = var32 - var34;
         if (var30 >= var36) {
            double var38 = var2.minY;
            double var40 = var3.maxY;
            double var42 = 0.1;
            double var44 = var40 + var42;
            if (var38 <= var44) {
               return true;
            }
         }
      }

      return false;
   }

   private boolean c_bool_BlockPos(BlockPos var1) {
      if (var1 == null || mc.player == null || mc.level == null) {
         return false;
      }

      if (this.b_bool_BlockPos(var1)) {
         if (this.debug.getValue()) {
            Object[] var2 = new Object[3];
            Integer var3 = var1.getX();
            var2[0] = var3;
            Integer var4 = var1.getY();
            var2[1] = var4;
            Integer var5 = var1.getZ();
            var2[2] = var5;
            String var6 = String.format("Phase placement cancelled: player is stepping on (%d, %d, %d)", var2);
            String var7 = this.getName();
            Night.chatManager.sendTaggedMessage(var6, var7);
         }

         return false;
      } else {
         BlockState var8 = mc.level.getBlockState(var1);
         if (!var8.canBeReplaced() && !var8.isAir()) {
            BlockState var9 = var8;
            if (!this.a_bool_BlockState_BlockPos(var9, var1)) {
               return true;
            }
         }

         int var10 = this.a_int_int_int(0, 8);
         if (var10 == -1) {
            String var11 = this.getName();
            Night.chatManager.sendTaggedMessage("No blast-proof blocks could be found in your hotbar.", var11);
            return false;
         }

         Inventory var12 = mc.player.getInventory();
         int var13 = var10;
         Item var14 = var12.getItem(var13).getItem();
         Block var15;
         if (var14 instanceof BlockItem) {
            var15 = ((BlockItem)var14).getBlock();
         } else {
            var15 = Blocks.OBSIDIAN;
         }

         Block var16 = var15;
         int var17 = mc.player.getInventory().getSelectedSlot();
         boolean var18 = false;
         List var19 = List.of();
         List var20 = BlockInteractionUtil.a_List_BlockPos_List_bool(var1, var19, false);
         if (!var20.isEmpty()) {
            int var21 = var10;
            int var22 = var17;
            InventoryUtil.a_bool_String_int_int("Silent", var21, var22);

            for (Direction var24 : (Iterable<Direction>)(Object)var20) {
               boolean var25 = this.placeRender.getValue();
               if (BlockInteractionUtil.a(var1, var24, InteractionHand.MAIN_HAND, "Grim", true, var25)) {
                  var18 = true;
                  break;
               }
            }

            int var26 = var10;
            int var27 = var17;
            InventoryUtil.b("Silent", var26, var27);
         } else {
            int var28 = var10;
            int var29 = var17;
            InventoryUtil.a_bool_String_int_int("Silent", var28, var29);
            boolean var30 = this.placeRender.getValue();
            BlockInteractionUtil.a_void_BlockPos_InteractionHand_String_bool(var1, InteractionHand.MAIN_HAND, "Grim", var30);
            var18 = true;
            int var31 = var10;
            int var32 = var17;
            InventoryUtil.b("Silent", var31, var32);
         }

         if (var18) {
            Block var33 = var16;
            BlockInteractionUtil.a_void_BlockPos_Block(var1, var33);
            if (this.debug.getValue()) {
               Object[] var34 = new Object[4];
               String var35 = var16.getName().getString();
               var34[0] = var35;
               Integer var36 = var1.getX();
               var34[1] = var36;
               Integer var37 = var1.getY();
               var34[2] = var37;
               Integer var38 = var1.getZ();
               var34[3] = var38;
               String var39 = String.format("Placed %s at (%d, %d, %d) before phasing", var34);
               String var40 = this.getName();
               Night.chatManager.sendTaggedMessage(var39, var40);
            }
         }

         return var18;
      }
   }

   private record PearlAngle(Vec3 vec, int count) {}
}
