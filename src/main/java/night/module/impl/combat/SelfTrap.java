package night.module.impl.combat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.events.EventTarget;
import night.events.impl.PacketReceiveEvent;
import night.events.impl.PacketSendEvent;
import night.events.impl.UpdateWalkingPlayerEvent;
import night.module.Module;
import night.module.ModuleInfo;
import night.setting.impl.BooleanSetting;
import night.setting.impl.ModeSetting;
import night.setting.impl.NumberSetting;
import night.util.concurrent.ThreadManager;
import night.util.inventory.InventoryUtil;
import night.util.module.ModuleRegistryHelper;
import night.util.rotation.RotationMathUtil;
import night.util.world.BlockInteractionUtil;
import night.util.world.EntityUtil;
import night.util.world.HoleUtil;
import night.util.world.PacketAndRenderUtil;

@ModuleInfo(
   name = "SelfTrap",
   description = "Automatically places blocks around you to prevent other people from getting inside your hole.",
   category = Module.Category.COMBAT,
   proxySupported = true
)
public class SelfTrap extends Module {
   public ModeSetting switchSetting = new ModeSetting(
      "Switch", "The mode that will be used for automatically switching to necessary items.", "Silent", InventoryUtil.a
   );
   public ModeSetting mode;
   public BooleanSetting head;
   public BooleanSetting asynchronous;
   public NumberSetting bPT;
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
   public BooleanSetting antiStep;
   public BooleanSetting antiBomb;
   public BooleanSetting holeCheck;
   public ModeSetting x;
   public BooleanSetting y;
   public BooleanSetting z;
   public BooleanSetting render;
   private int B;
   private boolean C;
   private List<BlockPos> D;
   private BlockPos E;
   private boolean F;
   private int G;

   public SelfTrap() {
      String[] var1 = new String[]{"Partial", "Full"};
      this.mode = new ModeSetting("Mode", "The offsets that will be used when trapping.", "Partial", var1);
      ModeSetting var3 = this.mode;
      String[] var4 = new String[]{"Full"};
      ModeSetting.ModeCondition var2 = new ModeSetting.ModeCondition(var3, var4);
      this.head = new BooleanSetting("Head", "Whether or not to cover the block on the players head.", var2, true);
      this.asynchronous = new BooleanSetting("Asynchronous", "Performs calculations on separate threads.", true);
      Integer var5 = 4;
      Integer var6 = 1;
      Integer var7 = 8;
      this.bPT = new NumberSetting("BPT", "The maximum number of blocks to place per tick.", var5, var6, var7);
      this.await = new BooleanSetting("Await", "Waits for blocks to be registered by the client before placing on them.", false);
      this.rotate = new BooleanSetting("Rotate", "Rotates toward each block before placing it.", true);
      this.timer = new BooleanSetting("Timer", "Runs the game at 1.08x while enabled -- pays GrimAC Timer balance back between placement bursts.", false);
      this.strictDirection = new BooleanSetting("StrictDirection", "Only places using directions that face you.", false);
      this.airPlace = new BooleanSetting("AirPlace", "Places blocks in the air without needing neighboring blocks.", false);
      this.attack = new BooleanSetting("Attack", "Attacks any crystal that interferes with your trap.", false);
      BooleanSetting var8 = this.attack;
      this.attackRotate = new BooleanSetting(
         "AttackRotate", "Rotate", "Rotates toward the crystal before attacking it.", new BooleanSetting.BooleanCondition(var8, true), true
      );
      BooleanSetting var10 = this.attack;
      BooleanSetting.BooleanCondition var9 = new BooleanSetting.BooleanCondition(var10, true);
      Float var11 = 3.0F;
      Float var12 = 1.0F;
      Float var13 = 6.0F;
      this.attackRange = new NumberSetting("AttackRange", "Range", "The maximum range at which crystals will be attacked.", var9, var11, var12, var13);
      BooleanSetting var15 = this.attack;
      BooleanSetting.BooleanCondition var14 = new BooleanSetting.BooleanCondition(var15, true);
      Integer var16 = 5;
      Integer var17 = 0;
      Integer var18 = 20;
      this.attackAge = new NumberSetting(
         "AttackAge", "Age", "The minimum age (in ticks) a crystal must reach before being attacked.", var14, var16, var17, var18
      );
      BooleanSetting var19 = this.attack;
      this.attackMultiTask = new BooleanSetting(
         "AttackMultiTask", "Multitask", "Allows attacking while an item is already in use.", new BooleanSetting.BooleanCondition(var19, true), true
      );
      BooleanSetting var20 = this.attack;
      this.attackSwing = new BooleanSetting(
         "AttackSwing", "Swing", "Sends a swing packet whenever attacking a crystal.", new BooleanSetting.BooleanCondition(var20, true), true
      );
      this.antiStep = new BooleanSetting("AntiStep", "Adds additional blocks that prevent anyone from stepping out of the hole.", false);
      this.antiBomb = new BooleanSetting("AntiBomb", "Places an extra block above your head to prevent you from getting bombed.", false);
      this.holeCheck = new BooleanSetting("HoleCheck", "Only self traps whenever you are in a hole.", false);
      String[] var21 = new String[]{"None", "NCP", "Grim"};
      this.x = new ModeSetting("AutoJump", "Automatically jumps to place high blocks when on-ground eye height is too low for StrictDirection.", "Grim", var21);
      this.y = new BooleanSetting("WhileEating", "Places blocks normally while eating.", true);
      this.z = new BooleanSetting("SelfDisable", "Toggles off the module once it is finished with placing.", false);
      this.render = new BooleanSetting("Render", "Whether or not to render the place position.", true);
      this.B = 0;
      this.C = false;
      this.D = new ArrayList<>();
      this.E = null;
      this.F = false;
      this.G = 0;
   }

   @Override
   public String getDisplayInfo() {
      return "Packet: " + this.B;
   }

   @EventTarget
   public void a_void_UpdateWalkingPlayerEvent(UpdateWalkingPlayerEvent var1) {
      if (!this.isProxyDelegated()) {
         if (this.y.getValue() || !EntityUtil.a()) {
            Runnable var2 = () -> {
               label185: {
                  this.G = 0;
                  if (this.switchSetting.getValue().equalsIgnoreCase("None")) {
                     Item var1x = mc.player.getMainHandItem().getItem();
                     if (!(var1x instanceof BlockItem)) {
                        break label185;
                     }

                     Block var2x = ((BlockItem)var1x).getBlock();
                     if (!this.a_bool_Block(var2x)) {
                        break label185;
                     }
                  }

                  if (mc.player.onGround()) {
                     BlockPos var4 = mc.player.blockPosition();
                     this.E = var4;
                     this.F = false;
                  }

                  BlockPos var5 = this.E;
                  if (this.a_bool_BlockPos(var5)) {
                     if (this.z.getValue() && mc.player.onGround() && !this.F) {
                        this.setEnabled(false);
                     }

                     return;
                  }

                  if (!this.holeCheck.getValue() || HoleUtil.a_bool_Player(mc.player) || !this.x.getValue().equalsIgnoreCase("None") && !mc.player.onGround()) {
                     byte var6;
                     if (!this.switchSetting.getValue().equalsIgnoreCase("AltSwap") && !this.switchSetting.getValue().equalsIgnoreCase("AltPickup")) {
                        var6 = 8;
                     } else {
                        var6 = 35;
                     }

                     int var7 = this.a(0, var6);
                     int var8 = mc.player.getInventory().getSelectedSlot();
                     if (var7 == -1) {
                        String var9 = this.getName();
                        Night.chatManager.sendTaggedMessage("No blast-proof blocks could be found in your hotbar.", var9);
                        this.setEnabled(false);
                        this.D = new ArrayList<>();
                        return;
                     }

                     LocalPlayer var10 = mc.player;
                     BlockPos var11 = this.E;
                     boolean var12 = this.mode.getValue().equalsIgnoreCase("Partial");
                     boolean var13 = this.head.getValue();
                     boolean var14 = this.antiStep.getValue();
                     boolean var15 = this.antiBomb.getValue();
                     boolean var16 = this.strictDirection.getValue();
                     boolean var17 = this.airPlace.getValue();
                     List var18 = HoleUtil.a(var10, var11, var12, var13, var14, var15, var16, var17)
                        .stream()
                        .filter(var0 -> BlockInteractionUtil.a_bool_BlockPos_bool(var0, true))
                        .toList();
                     this.D = var18;
                     if (this.attack.getValue()) {
                        this.v();
                     }

                     if (this.D.isEmpty()) {
                        if (this.z.getValue() && mc.player.onGround() && !this.F) {
                           BlockPos var19 = this.E;
                           if (this.a_bool_BlockPos(var19)) {
                              this.setEnabled(false);
                           }
                        }

                        return;
                     }

                     if (!this.x.getValue().equalsIgnoreCase("None") && this.strictDirection.getValue() && mc.player.onGround() && !this.airPlace.getValue()) {
                        boolean var20 = false;

                        for (BlockPos var22 : (Iterable<BlockPos>)(Object)this.D) {
                           int var23 = var22.getY();
                           int var24 = mc.player.getBlockY() + 2;
                           if (var23 >= var24) {
                              BlockPos var25 = var22;
                              boolean var26 = this.strictDirection.getValue();
                              if (BlockInteractionUtil.b_Direction_BlockPos_bool(var25, var26) == null) {
                                 var20 = true;
                                 break;
                              }
                           }
                        }

                        if (var20) {
                           this.F = true;
                           mc.execute(() -> {
                              if (mc.player != null && mc.player.onGround()) {
                                 mc.player.jumpFromGround();
                              }
                           });
                        }
                     }

                     this.C = true;
                     Night.rotationManager.beginBatch();

                     label135: {
                        try {
                           int var27 = this.G;
                           double var28 = this.bPT.getValue().intValue();
                           double var30 = InventoryUtil.f(7);
                           int var32 = (int)(var28 * var30);
                           if (var27 >= var32) {
                              break label135;
                           }

                           String var33 = this.switchSetting.getValue();
                           int var34 = var7;
                           int var35 = var8;
                           InventoryUtil.a_bool_String_int_int(var33, var34, var35);
                           ArrayList var36 = new ArrayList();
                           ArrayList var37 = var36;

                           for (BlockPos var39 : (Iterable<BlockPos>)(Object)this.D) {
                              int var40 = this.G;
                              double var41 = this.bPT.getValue().intValue();
                              double var43 = InventoryUtil.f(7);
                              int var45 = (int)(var41 * var43);
                              if (var40 >= var45) {
                                 break;
                              }

                              if (this.attack.getValue()) {
                                 ClientLevel var46 = mc.level;
                                 BlockPos var48 = var39;
                                 AABB var47 = new AABB(var48);
                                 double var49 = 0.5;
                                 AABB var51 = var47.inflate(var49);

                                 for (EndCrystal var53 : (Iterable<EndCrystal>)(Object)var46.getEntitiesOfClass(EndCrystal.class, var51)) {
                                    if (var53.isAlive()) {
                                       ClientPacketListener var54 = mc.getConnection();
                                       int var55 = var53.getId();
                                       var54.send(new ServerboundAttackPacket(var55));
                                       if (this.attackSwing.getValue()) {
                                          ClientPacketListener var56 = mc.getConnection();
                                          Packet var57 = PacketAndRenderUtil.a_Packet_InteractionHand(InteractionHand.MAIN_HAND);
                                          var56.send(var57);
                                       }
                                    }
                                 }
                              }

                              BlockPos var58 = var39;
                              ArrayList var59 = var37;
                              boolean var60 = this.strictDirection.getValue();
                              Direction var61 = BlockInteractionUtil.b_Direction_BlockPos_List_bool(var58, var59, var60);
                              if (var61 == null) {
                                 if (this.airPlace.getValue()) {
                                    BlockPos var62 = var39;
                                    boolean var63 = this.rotate.getValue();
                                    boolean var64 = this.render.getValue();
                                    BlockInteractionUtil.a_void_BlockPos_InteractionHand_bool_bool(var62, InteractionHand.MAIN_HAND, var63, var64);
                                    ArrayList var65 = var37;
                                    BlockPos var66 = var39;
                                    var65.add(var66);
                                    int var67 = this.G + 1;
                                    this.G = var67;
                                 } else {
                                    BlockPos var68 = var39;
                                    ArrayList var69 = var37;
                                    boolean var70 = this.strictDirection.getValue();
                                    BlockPos var71 = BlockInteractionUtil.a_BlockPos_BlockPos_List_bool_int(var68, var69, var70, 2);
                                    if (var71 != null) {
                                       BlockPos var72 = var71;
                                       ArrayList var73 = var37;
                                       boolean var74 = this.strictDirection.getValue();
                                       Direction var75 = BlockInteractionUtil.b_Direction_BlockPos_List_bool(var72, var73, var74);
                                       if (var75 != null) {
                                          BlockPos var76 = var71;
                                          Direction var77 = var75;
                                          boolean var78 = this.rotate.getValue();
                                          boolean var79 = this.render.getValue();
                                          if (BlockInteractionUtil.a(var76, var77, InteractionHand.MAIN_HAND, var78, true, var79)) {
                                             ArrayList var80 = var37;
                                             BlockPos var81 = var71;
                                             var80.add(var81);
                                             int var82 = this.G + 1;
                                             this.G = var82;
                                          }
                                       }
                                    }
                                 }
                              } else {
                                 BlockPos var83 = var39;
                                 Direction var84 = var61;
                                 boolean var85 = this.rotate.getValue();
                                 boolean var86 = this.render.getValue();
                                 if (BlockInteractionUtil.a(var83, var84, InteractionHand.MAIN_HAND, var85, true, var86)) {
                                    ArrayList var87 = var37;
                                    BlockPos var88 = var39;
                                    var87.add(var88);
                                    int var89 = this.G + 1;
                                    this.G = var89;
                                 }
                              }
                           }

                           if (!mc.player.onGround() && this.x.getValue().equalsIgnoreCase("NCP")) {
                              mc.execute(() -> {
                                 if (mc.player != null && !mc.player.onGround()) {
                                    LocalPlayer var0 = mc.player;
                                    double var1xx = mc.player.getDeltaMovement().x;
                                    double var3x = -0.42;
                                    double var5x = mc.player.getDeltaMovement().z;
                                    var0.setDeltaMovement(new Vec3(var1xx, var3x, var5x));
                                 }
                              });
                           }

                           String var90 = this.switchSetting.getValue();
                           int var91 = var7;
                           int var92 = var8;
                           InventoryUtil.b(var90, var91, var92);
                           if (this.z.getValue() && mc.player.onGround() && !this.F) {
                              BlockPos var93 = this.E;
                              if (this.a_bool_BlockPos(var93)) {
                                 this.setEnabled(false);
                              }
                           }
                        } catch (Throwable var95) {
                           Night.rotationManager.endBatch();
                           this.C = false;
                           throw var95;
                        }

                        Night.rotationManager.endBatch();
                        this.C = false;
                        return;
                     }

                     Night.rotationManager.endBatch();
                     this.C = false;
                     return;
                  }

                  return;
               }

               String var3 = this.getName();
               Night.chatManager.sendTaggedMessage("You are currently not holding any blast-proof blocks.", var3);
               this.setEnabled(false);
               this.D = new ArrayList<>();
            };
            if (this.asynchronous.getValue()) {
               ThreadManager.a(var2);
            } else {
               var2.run();
            }
         }
      }
   }

   @EventTarget
   public void a_void_PacketReceiveEvent(PacketReceiveEvent var1) {
      if (mc.player != null && mc.level != null) {
         if (this.attack.getValue() && var1.getPacket() instanceof ClientboundAddEntityPacket var3 && var3.getType() == EntityTypes.END_CRYSTAL) {
            double var4 = var3.getX();
            double var6 = var3.getY();
            double var8 = var3.getZ();
            Vec3 var10 = new Vec3(var4, var6, var8);
            double var11 = var10.x;
            double var13 = 1.0;
            double var15 = var11 - var13;
            double var17 = var10.y;
            double var19 = var10.z;
            double var21 = 1.0;
            double var23 = var19 - var21;
            double var25 = var10.x;
            double var27 = 1.0;
            double var29 = var25 + var27;
            double var31 = var10.y;
            double var33 = 2.0;
            double var35 = var31 + var33;
            double var37 = var10.z;
            double var39 = 1.0;
            double var41 = var37 + var39;
            AABB var43 = new AABB(var15, var17, var23, var29, var35, var41);
            boolean var44 = false;
            Iterator var45 = this.D.iterator();

            label37: {
               double var51;
               double var53;
               do {
                  if (!var45.hasNext()) {
                     break label37;
                  }

                  BlockPos var46 = (BlockPos)var45.next();
                  AABB var47 = var43;
                  BlockPos var48 = var46;
                  if (var47.intersects(new AABB(var48))) {
                     break;
                  }

                  Vec3 var49 = var10;
                  Vec3 var50 = Vec3.atCenterOf(var46);
                  var51 = var49.distanceToSqr(var50);
                  var53 = 4.0;
               } while (!(var51 <= var53));

               var44 = true;
            }

            if (var44) {
               Vec3 var55 = mc.player.getEyePosition();
               Vec3 var56 = var10;
               double var57 = var55.distanceToSqr(var56);
               double var59 = Mth.square(this.attackRange.getValue().doubleValue());
               if (var57 <= var59 && (this.attackMultiTask.getValue() || !mc.player.isUsingItem())) {
                  ClientPacketListener var61 = mc.getConnection();
                  int var62 = var3.getId();
                  var61.send(new ServerboundAttackPacket(var62));
                  if (this.attackSwing.getValue()) {
                     ClientPacketListener var63 = mc.getConnection();
                     Packet var64 = PacketAndRenderUtil.a_Packet_InteractionHand(InteractionHand.MAIN_HAND);
                     var63.send(var64);
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

               for (BlockPos var18 : (Iterable<BlockPos>)(Object)this.D) {
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

   @Override
   public void onEnable() {
      if (this.timer.getValue()) {
         ModuleRegistryHelper.a(this);
      }

      if (mc.player != null && mc.level != null) {
         this.B = 0;
         this.D.clear();
         BlockPos var1 = mc.player.blockPosition();
         this.E = var1;
         this.F = false;
      }
   }

   @Override
   public void onDisable() {
      ModuleRegistryHelper.b(this);
      this.D = new ArrayList<>();
      this.E = null;
      this.F = false;
   }

   private boolean a_bool_BlockPos(BlockPos var1) {
      if (mc.level != null && var1 != null) {
         if (this.mode.getValue().equalsIgnoreCase("Partial")) {
            BlockPos var2 = var1.offset(0, 2, 0);
            ClientLevel var3 = mc.level;
            BlockPos var4 = var2;
            boolean var5;
            if (!var3.getBlockState(var4).canBeReplaced()) {
               var5 = true;
            } else {
               var5 = false;
            }

            return var5;
         } else {
            LocalPlayer var6 = mc.player;
            boolean var7 = this.head.getValue();
            boolean var8 = this.antiStep.getValue();
            boolean var9 = this.antiBomb.getValue();
            boolean var10 = this.strictDirection.getValue();
            boolean var11 = this.airPlace.getValue();

            for (BlockPos var13 : (Iterable<BlockPos>)(Object)HoleUtil.a(var6, var1, false, var7, var8, var9, var10, var11)) {
               ClientLevel var14 = mc.level;
               BlockPos var15 = var13;
               if (var14.getBlockState(var15).canBeReplaced()) {
                  return false;
               }
            }

            return true;
         }
      } else {
         return false;
      }
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

   private int a(int var1, int var2) {
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
      if (this.C) {
         int var2 = this.B + 1;
         this.B = var2;
      }
   }
}
