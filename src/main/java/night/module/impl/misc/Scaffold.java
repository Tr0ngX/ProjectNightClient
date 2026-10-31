package night.module.impl.misc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.events.EventTarget;
import night.events.impl.PacketReceiveEvent;
import night.events.impl.UpdateWalkingPlayerEvent;
import night.module.Module;
import night.module.ModuleInfo;
import night.setting.impl.BooleanSetting;
import night.setting.impl.ModeSetting;
import night.setting.impl.NumberSetting;
import night.util.inventory.InventoryUtil;
import night.util.world.BlockInteractionUtil;
import night.util.world.PacketAndRenderUtil;

@ModuleInfo(name = "Scaffold", description = "Places blocks under the player to bridge automatically.", category = Module.Category.MISCELLANEOUS)
public class Scaffold extends Module {
   public ModeSetting switchSetting = new ModeSetting("Switch", "The mode that will be used for automatically switching to blocks.", "Silent", InventoryUtil.a);
   public NumberSetting range;
   public BooleanSetting keepY;
   public BooleanSetting downwards;
   public BooleanSetting tower;
   public NumberSetting towerSpeed;
   public BooleanSetting rotate;
   public BooleanSetting strictDirection;
   public BooleanSetting airPlace;
   public NumberSetting delay;
   public BooleanSetting render;
   private int p;
   private BlockPos q;
   private Block r;
   private int s;
   private final Map<BlockPos, Long> t;

   public Scaffold() {
      Double var1 = 4.0;
      Double var2 = 1.0;
      Double var3 = 6.0;
      this.range = new NumberSetting("Range", "Range to place blocks.", var1, var2, var3);
      this.keepY = new BooleanSetting("KeepY", "Maintains the player's Y level while moving.", false);
      this.downwards = new BooleanSetting("Downwards", "Places blocks below your feet when sneaking.", true);
      this.tower = new BooleanSetting("Tower", "Quickly towers up when jumping.", true);
      BooleanSetting var5 = this.tower;
      BooleanSetting.BooleanCondition var4 = new BooleanSetting.BooleanCondition(var5, true);
      Double var6 = 0.42;
      Double var7 = 0.1;
      Double var8 = 1.0;
      this.towerSpeed = new NumberSetting("TowerSpeed", "Vertical tower speed.", var4, var6, var7, var8);
      this.rotate = new BooleanSetting("Rotate", "Rotates towards block placements.", true);
      this.strictDirection = new BooleanSetting("StrictDirection", "Only places using directions facing you.", false);
      this.airPlace = new BooleanSetting("AirPlace", "Places blocks in the air without support.", false);
      Integer var9 = 0;
      Integer var10 = 0;
      Integer var11 = 10;
      this.delay = new NumberSetting("Delay", "Delay in ticks between placements.", var9, var10, var11);
      this.render = new BooleanSetting("Render", "Renders placed positions.", true);
      this.p = Integer.MIN_VALUE;
      this.q = null;
      this.r = null;
      this.s = 0;
      this.t = new HashMap<>();
   }

   @Override
   public void onEnable() {
      this.p = Integer.MIN_VALUE;
      this.q = null;
      this.s = 0;
      this.t.clear();
      BlockInteractionUtil.a();
   }

   @Override
   public void onDisable() {
      this.p = Integer.MIN_VALUE;
      this.q = null;
      this.r = null;
      this.t.clear();
      BlockInteractionUtil.a();
   }

   @EventTarget
   public void a(UpdateWalkingPlayerEvent var1) {
      if (mc.player != null && mc.level != null) {
         int var2 = this.s;
         int var3 = this.delay.getValue().intValue();
         if (var2 < var3) {
            int var4 = this.s + 1;
            this.s = var4;
         } else {
            byte var5;
            if (!this.switchSetting.getValue().equalsIgnoreCase("AltSwap") && !this.switchSetting.getValue().equalsIgnoreCase("AltPickup")) {
               var5 = 8;
            } else {
               var5 = 35;
            }

            byte var6 = var5;
            int var7 = this.a_int_int_int(0, var6);
            if (var7 == -1) {
               Input var8;
               if (mc.player.input != null) {
                  var8 = mc.player.input.keyPresses;
               } else {
                  var8 = Input.EMPTY;
               }

               Input var9 = var8;
               if (!var9.shift()) {
                  ClientInput var10 = mc.player.input;
                  boolean var11 = var9.forward();
                  boolean var12 = var9.backward();
                  boolean var13 = var9.left();
                  boolean var14 = var9.right();
                  boolean var15 = var9.jump();
                  boolean var16 = var9.sprint();
                  var10.keyPresses = new Input(var11, var12, var13, var14, var15, true, var16);
               }
            } else {
               if (this.tower.getValue() && mc.options.keyJump.isDown() && !this.v()) {
                  LocalPlayer var17 = mc.player;
                  double var18 = mc.player.getDeltaMovement().x;
                  double var20 = this.towerSpeed.getValue().doubleValue();
                  double var22 = mc.player.getDeltaMovement().z;
                  var17.setDeltaMovement(var18, var20, var22);
               }

               int var24 = (int)Math.floor(mc.player.getY());
               if (this.keepY.getValue() && this.v()) {
                  label83: {
                     if (!mc.player.onGround()) {
                        int var25 = this.p;
                        int var26 = mc.level.getMinY();
                        if (var25 >= var26) {
                           break label83;
                        }
                     }

                     int var27 = var24;
                     this.p = var27;
                  }

                  var24 = this.p;
               } else if (this.downwards.getValue() && mc.options.keyShift.isDown()) {
                  var24 = mc.player.getBlockY() - 1;
               } else {
                  int var28 = var24;
                  this.p = var28;
               }

               int var29 = mc.player.getBlockX();
               int var30 = var24;
               int var31 = mc.player.getBlockZ();
               BlockPos var32 = new BlockPos(var29, var30, var31).below();
               List var33 = this.d(var32);
               if (!var33.isEmpty()) {
                  int var34 = mc.player.getInventory().getSelectedSlot();
                  String var35 = this.switchSetting.getValue();
                  int var36 = var7;
                  int var37 = var34;
                  if (InventoryUtil.a_bool_String_int_int(var35, var36, var37)) {
                     for (BlockPos var39 : (Iterable<BlockPos>)(Object)var33) {
                        ClientLevel var40 = mc.level;
                        BlockPos var41 = var39;
                        if (var40.getBlockState(var41).canBeReplaced()) {
                           BlockPos var42 = var39;
                           if (!this.b(var42)) {
                              BlockPos var43 = var39;
                              boolean var44 = this.strictDirection.getValue();
                              Direction var45 = BlockInteractionUtil.b_Direction_BlockPos_bool(var43, var44);
                              if (var45 == null) {
                                 if (this.airPlace.getValue()) {
                                    BlockPos var46 = var39;
                                    InteractionHand var47 = InteractionHand.MAIN_HAND;
                                    String var48;
                                    if (this.rotate.getValue()) {
                                       var48 = "Normal";
                                    } else {
                                       var48 = "None";
                                    }

                                    boolean var49 = this.render.getValue();
                                    BlockInteractionUtil.a_void_BlockPos_InteractionHand_String_bool(var46, var47, var48, var49);
                                    BlockPos var50 = var39;
                                    this.q = var50;
                                    BlockPos var51 = var39;
                                    this.a(var51);
                                    BlockPos var52 = var39;
                                    this.c(var52);
                                 }
                              } else {
                                 BlockPos var53 = var39;
                                 Direction var54 = var45;
                                 boolean var55 = this.rotate.getValue();
                                 boolean var56 = this.render.getValue();
                                 if (BlockInteractionUtil.a(var53, var54, InteractionHand.MAIN_HAND, var55, false, var56)) {
                                    BlockPos var57 = var39;
                                    this.q = var57;
                                    BlockPos var58 = var39;
                                    this.a(var58);
                                    BlockPos var59 = var39;
                                    this.c(var59);
                                 }
                              }
                           }
                        }
                     }

                     String var60 = this.switchSetting.getValue();
                     int var61 = var7;
                     int var62 = var34;
                     InventoryUtil.b(var60, var61, var62);
                     this.s = 0;
                  }
               }
            }
         }
      }
   }

   private void a(BlockPos var1) {
      if (this.r != null) {
         Block var2 = this.r;
         BlockInteractionUtil.a_void_BlockPos_Block(var1, var2);
      }
   }

   @EventTarget
   public void a(PacketReceiveEvent var1) {
      if (var1.getPacket() instanceof ClientboundBlockUpdatePacket var3) {
         BlockInteractionUtil.a_void_BlockPos(var3.getPos());
         Map var4 = this.t;
         BlockPos var5 = var3.getPos();
         var4.remove(var5);
      } else {
         Packet var6 = var1.getPacket();
         if (var6 instanceof ClientboundSectionBlocksUpdatePacket) {
            ((ClientboundSectionBlocksUpdatePacket)var6).runUpdates((var1x, var2) -> {
               BlockPos var3x = var1x.immutable();
               BlockInteractionUtil.a_void_BlockPos(var3x);
               Map var4x = this.t;
               BlockPos var5x = var3x;
               var4x.remove(var5x);
            });
         }
      }
   }

   private boolean b(BlockPos var1) {
      Long var2 = this.t.get(var1);
      if (var2 == null) {
         return false;
      }

      long var3 = 50L;
      long var5 = Night.serverManager.getPing();
      long var7 = 2L;
      long var9 = var5 * var7;
      long var11 = Math.max(var3, var9);
      long var13 = System.currentTimeMillis();
      long var15 = var2;
      long var17 = var13 - var15;
      long var19 = var11;
      if (var17 < var19) {
         return true;
      }

      this.t.remove(var1);
      return false;
   }

   private void c(BlockPos var1) {
      Map var2 = this.t;
      Long var3 = System.currentTimeMillis();
      var2.put(var1, var3);
   }

   private boolean v() {
      double var1 = Math.abs(mc.player.getDeltaMovement().x);
      double var3 = 0.05;
      if (!(var1 > var3)) {
         double var5 = Math.abs(mc.player.getDeltaMovement().z);
         double var7 = 0.05;
         if (!(var5 > var7)) {
            return false;
         }
      }

      return true;
   }

   private int a_int_int_int(int var1, int var2) {
      for (int var3 = var1; var3 <= var2; var3++) {
         Inventory var4 = mc.player.getInventory();
         int var5 = var3;
         ItemStack var6 = var4.getItem(var5);
         if (!var6.isEmpty()) {
            Item var7 = var6.getItem();
            if (var7 instanceof BlockItem) {
               Block var8 = ((BlockItem)var7).getBlock();
               if (!var8.defaultBlockState().canBeReplaced() && PacketAndRenderUtil.a_bool_BlockState(var8.defaultBlockState())) {
                  Block var9 = var8;
                  this.r = var9;
                  return var3;
               }
            }
         }
      }

      return -1;
   }

   private List<BlockPos> d(BlockPos var1) {
      ArrayList var2 = new ArrayList();
      if (this.airPlace.getValue()) {
         if (this.f(var1)) {
            var2.add(var1);
         }

         return var2;
      } else {
         ArrayList var3 = var2;
         this.a_void_BlockPos_List(var1, var3);
         if (this.q != null) {
            int var4 = this.q.getX();
            int var5 = this.q.getY();
            int var6 = this.q.getZ();
            int var7 = var1.getX();
            int var8 = var1.getY();
            int var9 = var1.getZ();
            int var10 = var7;
            int var11 = var4;
            int var12 = var10 - var11;
            int var13 = var8;
            int var14 = var5;
            int var15 = var13 - var14;
            int var16 = var9;
            int var17 = var6;
            int var18 = var16 - var17;
            int var19 = Integer.compare(var12, 0);
            int var20 = Integer.compare(var15, 0);
            int var21 = Integer.compare(var18, 0);
            int var22 = Math.abs(var12);
            int var23 = Math.abs(var15);
            int var24 = Math.abs(var18);
            int var25 = var22 << 1;
            int var26 = var23 << 1;
            int var27 = var24 << 1;
            int var28 = 0;
            int var29 = var22;
            int var30 = var23;
            if (var29 >= var30) {
               int var31 = var22;
               int var32 = var24;
               if (var31 >= var32) {
                  int var33 = var26;
                  int var34 = var22;
                  int var35 = var33 - var34;
                  int var36 = var27;
                  int var37 = var22;
                  int var38 = var36 - var37;

                  while (true) {
                     int var39 = var4;
                     int var40 = var5;
                     int var41 = var6;
                     BlockPos var42 = new BlockPos(var39, var40, var41);
                     ArrayList var43 = var2;
                     this.a_void_BlockPos_List(var42, var43);
                     if (++var28 > 8) {
                        return var2;
                     }

                     int var44 = var4;
                     int var45 = var7;
                     if (var44 == var45) {
                        int var46 = var5;
                        int var47 = var8;
                        if (var46 == var47) {
                           int var48 = var6;
                           int var49 = var9;
                           if (var48 == var49) {
                              return var2;
                           }
                        }
                     }

                     if (var35 >= 0) {
                        int var50 = var5;
                        int var51 = var20;
                        var5 = var50 + var51;
                        int var52 = var35;
                        int var53 = var25;
                        var35 = var52 - var53;
                     }

                     if (var38 >= 0) {
                        int var54 = var6;
                        int var55 = var21;
                        var6 = var54 + var55;
                        int var56 = var38;
                        int var57 = var25;
                        var38 = var56 - var57;
                     }

                     int var58 = var4;
                     int var59 = var19;
                     var4 = var58 + var59;
                     int var60 = var35;
                     int var61 = var26;
                     var35 = var60 + var61;
                     int var62 = var38;
                     int var63 = var27;
                     var38 = var62 + var63;
                  }
               }
            }

            int var64 = var23;
            int var65 = var22;
            if (var64 >= var65) {
               int var66 = var23;
               int var67 = var24;
               if (var66 >= var67) {
                  int var68 = var25;
                  int var69 = var23;
                  int var70 = var68 - var69;
                  int var71 = var27;
                  int var72 = var23;
                  int var73 = var71 - var72;

                  while (true) {
                     int var74 = var4;
                     int var75 = var5;
                     int var76 = var6;
                     BlockPos var77 = new BlockPos(var74, var75, var76);
                     ArrayList var78 = var2;
                     this.a_void_BlockPos_List(var77, var78);
                     if (++var28 > 8) {
                        return var2;
                     }

                     int var79 = var4;
                     int var80 = var7;
                     if (var79 == var80) {
                        int var81 = var5;
                        int var82 = var8;
                        if (var81 == var82) {
                           int var83 = var6;
                           int var84 = var9;
                           if (var83 == var84) {
                              return var2;
                           }
                        }
                     }

                     if (var70 >= 0) {
                        int var85 = var4;
                        int var86 = var19;
                        var4 = var85 + var86;
                        int var87 = var70;
                        int var88 = var26;
                        var70 = var87 - var88;
                     }

                     if (var73 >= 0) {
                        int var89 = var6;
                        int var90 = var21;
                        var6 = var89 + var90;
                        int var91 = var73;
                        int var92 = var26;
                        var73 = var91 - var92;
                     }

                     int var93 = var5;
                     int var94 = var20;
                     var5 = var93 + var94;
                     int var95 = var70;
                     int var96 = var25;
                     var70 = var95 + var96;
                     int var97 = var73;
                     int var98 = var27;
                     var73 = var97 + var98;
                  }
               }
            }

            int var99 = var25;
            int var100 = var24;
            int var101 = var99 - var100;
            int var102 = var26;
            int var103 = var24;
            int var104 = var102 - var103;

            while (true) {
               int var105 = var4;
               int var106 = var5;
               int var107 = var6;
               BlockPos var108 = new BlockPos(var105, var106, var107);
               ArrayList var109 = var2;
               this.a_void_BlockPos_List(var108, var109);
               if (++var28 > 8) {
                  break;
               }

               int var110 = var4;
               int var111 = var7;
               if (var110 == var111) {
                  int var112 = var5;
                  int var113 = var8;
                  if (var112 == var113) {
                     int var114 = var6;
                     int var115 = var9;
                     if (var114 == var115) {
                        break;
                     }
                  }
               }

               if (var101 >= 0) {
                  int var116 = var4;
                  int var117 = var19;
                  var4 = var116 + var117;
                  int var118 = var101;
                  int var119 = var27;
                  var101 = var118 - var119;
               }

               if (var104 >= 0) {
                  int var120 = var5;
                  int var121 = var20;
                  var5 = var120 + var121;
                  int var122 = var104;
                  int var123 = var27;
                  var104 = var122 - var123;
               }

               int var124 = var6;
               int var125 = var21;
               var6 = var124 + var125;
               int var126 = var101;
               int var127 = var25;
               var101 = var126 + var127;
               int var128 = var104;
               int var129 = var26;
               var104 = var128 + var129;
            }
         }

         return var2;
      }
   }

   private void a_void_BlockPos_List(BlockPos var1, List<BlockPos> var2) {
      if (mc.level.getBlockState(var1).canBeReplaced()) {
         boolean var3 = this.strictDirection.getValue();
         if (BlockInteractionUtil.b_Direction_BlockPos_bool(var1, var3) != null) {
            if (!var2.contains(var1) && this.f(var1)) {
               var2.add(var1);
            }
         } else {
            BlockPos var4 = this.e(var1);
            if (var4 != null) {
               BlockPos var5 = var4;
               if (!var2.contains(var5)) {
                  BlockPos var6 = var4;
                  if (this.f(var6)) {
                     BlockPos var7 = var4;
                     var2.add(var7);
                  }
               }
            } else {
               BlockPos var8 = var1.below();
               int var9 = 0;

               while (true) {
                  int var10 = var9++;
                  if (var10 >= 3) {
                     break;
                  }

                  ClientLevel var11 = mc.level;
                  BlockPos var12 = var8;
                  if (!var11.getBlockState(var12).canBeReplaced()) {
                     break;
                  }

                  BlockPos var13 = var8;
                  boolean var14 = this.strictDirection.getValue();
                  if (BlockInteractionUtil.b_Direction_BlockPos_bool(var13, var14) != null) {
                     BlockPos var15 = var8;
                     if (!var2.contains(var15)) {
                        BlockPos var16 = var8;
                        if (this.f(var16)) {
                           BlockPos var17 = var8;
                           var2.add(var17);
                        }
                     }
                     break;
                  }

                  var8 = var8.below();
               }
            }

            if (!var2.contains(var1) && this.f(var1)) {
               var2.add(var1);
            }
         }
      }
   }

   private BlockPos e(BlockPos var1) {
      for (Direction var3 : Plane.HORIZONTAL) {
         BlockPos var4 = var1.relative(var3);
         boolean var5 = this.strictDirection.getValue();
         if (BlockInteractionUtil.b_Direction_BlockPos_bool(var4, var5) != null) {
            return var4;
         }
      }

      return null;
   }

   private boolean f(BlockPos var1) {
      LocalPlayer var2 = mc.player;
      Vec3 var3 = Vec3.atCenterOf(var1);
      double var4 = var2.distanceToSqr(var3);
      double var6 = this.range.getValue().doubleValue();
      double var8 = this.range.getValue().doubleValue();
      double var10 = var6 * var8;
      boolean var12;
      if (var4 <= var10) {
         var12 = true;
      } else {
         var12 = false;
      }

      return var12;
   }
}
