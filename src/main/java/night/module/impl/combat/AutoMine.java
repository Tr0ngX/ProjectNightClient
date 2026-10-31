package night.module.impl.combat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.events.EventTarget;
import night.events.impl.ClientDisconnectEvent;
import night.events.impl.UpdateWalkingPlayerEvent;
import night.module.Module;
import night.module.ModuleInfo;
import night.module.impl.player.SpeedMine;
import night.pingbypass.PingBypassState;
import night.setting.impl.BooleanSetting;
import night.setting.impl.ModeSetting;
import night.util.inventory.InventoryUtil;
import night.util.world.BlockInteractionUtil;
import night.util.world.DamageUtil;
import night.util.world.EntityUtil;
import night.util.world.HoleUtil;
import night.util.world.PositionUtil;

@ModuleInfo(name = "AutoMine", description = "Manages which blocks SpeedMine should mine to defeat your opponents.", category = Module.Category.COMBAT)
public class AutoMine extends Module {
   public ModeSetting priority;
   public ModeSetting logic;
   public BooleanSetting raytrace;
   public BooleanSetting civBreak;
   public BooleanSetting rotate;
   public BooleanSetting ignoreNaked;
   public BooleanSetting await;
   public BooleanSetting avoidSharing;
   public BooleanSetting terrain;
   public BooleanSetting terrainPlace;
   public BooleanSetting ironPickaxe;
   public BooleanSetting antiCrawl;
   public BooleanSetting face;
   private final List<BlockPos> r;
   private AutoMine.MineCandidate s;
   private UUID t;
   private BlockPos u;

   public AutoMine() {
      String[] var1 = new String[]{"Leg", "Head"};
      this.priority = new ModeSetting("Priority", "Which area of the target to prioritize mining.", "Leg", var1);
      String[] var2 = new String[]{"Grim", "NCP"};
      this.logic = new ModeSetting("Logic", "Auto mining targeting logic.", "Grim", var2);
      this.raytrace = new BooleanSetting("Raytrace", "Only mines blocks that are in line of sight (visible).", false);
      BooleanSetting var3 = this.raytrace;
      this.civBreak = new BooleanSetting(
         "CivBreak",
         "Priority=Leg only: when Raytrace can't see any leg-level surround block, mines the block above it instead. Turn on AutoCrystal's own MineIgnore setting for the crystal pre-place/detonate combo -- this module only builds+mines the obsidian.",
         new BooleanSetting.BooleanCondition(var3, true),
         false
      );
      BooleanSetting var4 = this.civBreak;
      this.rotate = new BooleanSetting("Rotate", "Rotates toward each block before placing it.", new BooleanSetting.BooleanCondition(var4, true), true);
      this.ignoreNaked = new BooleanSetting("IgnoreNaked", "Ignore Naked, even that player wears elytra only", false);
      this.await = new BooleanSetting(
         "Await", "After leaving a half-mined pair, the new pair waits until the old secondary really breaks (max 4s) instead of 0.5s.", false
      );
      this.avoidSharing = new BooleanSetting("AvoidSharing", "Avoids mining blocks that are part of your own surround or safety ring.", true);
      this.terrain = new BooleanSetting(
         "Terrain", "Automatically mines terrain blocks to place obsidian when target has no crystal base or is fluid-covered.", false
      );
      BooleanSetting var6 = this.terrain;
      BooleanSetting.BooleanCondition var5 = new BooleanSetting.BooleanCondition(var6, true);
      this.terrainPlace = new BooleanSetting("TerrainPlace", "Automatically places obsidian once the terrain block is broken.", var5, true);
      this.ironPickaxe = new BooleanSetting("IronPickaxe", "Uses iron pickaxe after obsidian matures to prevent obsidian item drops.", false);
      this.antiCrawl = new BooleanSetting(
         "AntiCrawl", "While crawling, mines the block above/below your feet to stand back up instead of staying trapped.", true
      );
      this.face = new BooleanSetting("Face", "Mines the block stuck in your face to rescue you from face traps or phased blocks.", true);
      this.r = new ArrayList<>();
      this.s = null;
      this.t = null;
      this.u = null;
   }

   public boolean v() {
      return this.raytrace.getValue();
   }

   public boolean a_bool_BlockPos(BlockPos var1) {
      boolean var2;
      if (this.civBreak.getValue() && this.u != null && this.u.equals(var1)) {
         var2 = true;
      } else {
         var2 = false;
      }

      return var2;
   }

   private void x() {
      this.u = null;
   }

   private SpeedMine y() {
      return Night.moduleManager.getModule(SpeedMine.class);
   }

   @EventTarget
   public void a_void_ClientDisconnectEvent(ClientDisconnectEvent var1) {
      this.r.clear();
      this.s = null;
      this.t = null;
      this.x();
   }

   @Override
   public void onEnable() {
      this.r.clear();
      this.s = null;
      this.t = null;
      this.x();
   }

   @Override
   public void onDisable() {
      this.r.clear();
      this.s = null;
      this.t = null;
      this.x();
      SpeedMine var1 = this.y();
      if (var1 != null) {
         var1.b_void_bool(false);
         var1.G();
      }
   }

   private boolean c(BlockPos var1) {
      if (var1 != null && mc.level != null) {
         Block var2 = mc.level.getBlockState(var1).getBlock();
         boolean var3;
         if (var2 != Blocks.OBSIDIAN && var2 != Blocks.BEDROCK) {
            var3 = false;
         } else {
            var3 = true;
         }

         return var3;
      } else {
         return false;
      }
   }

   private boolean a_bool_BlockPos_SpeedMine(BlockPos var1, SpeedMine var2) {
      BlockState var3 = mc.level.getBlockState(var1);
      return var3.canBeReplaced() ? false : !(var3.getBlock().defaultDestroyTime() < 0.0F) || var2.s6b.getValue() && var3.getBlock() == Blocks.BEDROCK;
   }

   private boolean b(Player var1) {
      return EntityUtil.a_bool_Entity(var1);
   }

   private boolean c(Player var1) {
      if (var1 != null && mc.level != null) {
         for (BlockPos var3 : (Iterable<BlockPos>)(Object)EntityUtil.b_List_Entity(var1)) {
            ClientLevel var4 = mc.level;
            BlockPos var5 = var3;
            if (var4.getBlockState(var5).is(Blocks.BEDROCK)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private BlockPos a_BlockPos_Player_SpeedMine(Player var1, SpeedMine var2) {
      if (var1 != null && mc.level != null) {
         if (!this.logic.getValue().equalsIgnoreCase("Grim")) {
            return null;
         }

         if (!this.c(var1)) {
            return null;
         }

         int var3;
         int var8;
         label78: {
            var3 = Mth.floor(var1.getY());
            if (!var1.isVisuallyCrawling()) {
               double var4 = var1.getBoundingBox().getYsize();
               double var6 = 1.0;
               if (!(var4 <= var6)) {
                  var8 = var3 + 2;
                  break label78;
               }
            }

            var8 = var3 + 1;
         }

         int var9 = var8;
         SpeedMine.MineTask var10 = var2.v();
         if (var10 != null && var10.f()) {
            int var11 = var10.a().getY();
            int var12 = var9;
            if (var11 == var12) {
               BlockPos var13 = var10.a();
               if (var2.a_bool_BlockPos_Player(var13, var1)) {
                  return var10.a();
               }
            }
         }

         ArrayList var14 = new ArrayList();

         for (BlockPos var16 : (Iterable<BlockPos>)(Object)HoleUtil.a_List_Entity(var1)) {
            int var17 = var16.getX();
            int var18 = var9;
            int var19 = var16.getZ();
            BlockPos var20 = new BlockPos(var17, var18, var19);
            ArrayList var21 = var14;
            BlockPos var22 = var20;
            if (!var21.contains(var22)) {
               ArrayList var23 = var14;
               BlockPos var24 = var20;
               var23.add(var24);
            }
         }

         BlockPos var25 = var1.blockPosition();
         int var26 = var9;
         int var27 = var3;
         int var28 = var26 - var27;
         BlockPos var29 = var25.above(var28);
         ArrayList var30 = var14;
         BlockPos var31 = var29;
         if (!var30.contains(var31)) {
            ArrayList var32 = var14;
            BlockPos var33 = var29;
            var32.add(var33);
         }

         double var34 = var1.getX();
         double var36 = var1.getBoundingBox().maxY;
         double var38 = 0.2;
         double var40 = var36 + var38;
         double var42 = var1.getZ();
         BlockPos var44 = BlockPos.containing(var34, var40, var42);
         ArrayList var45 = var14;
         BlockPos var46 = var44;
         if (!var45.contains(var46)) {
            ArrayList var47 = var14;
            BlockPos var48 = var44;
            var47.add(var48);
         }

         var14.sort(Comparator.comparingDouble(var1x -> {
            LocalPlayer var2x = mc.player;
            Vec3 var3x = Vec3.atCenterOf((BlockPos)var1x);
            double var4x = var2x.distanceToSqr(var3x);
            if (this.v() && BlockInteractionUtil.f((BlockPos)var1x)) {
               double var6x = var4x;
               double var8x = 1000.0;
               var4x = var6x - var8x;
            }

            return var4x;
         }));

         for (BlockPos var50 : (Iterable<BlockPos>)(Object)var14) {
            if (!var2.d_bool_BlockPos(var50) && (!this.v() || BlockInteractionUtil.f(var50))) {
               ClientLevel var51 = mc.level;
               BlockPos var52 = var50;
               BlockState var53 = var51.getBlockState(var52);
               if (!var53.canBeReplaced()) {
                  BlockPos[] var54 = new BlockPos[1];
                  BlockPos var55 = var50;
                  var54[0] = var55;
                  if (BlockInteractionUtil.a_bool_BlockPosArr(var54) && !var53.is(Blocks.COBWEB)) {
                     BlockPos var56 = var50;
                     if (var2.b_bool_BlockPos(var56)) {
                        return var50;
                     }
                  }
               }
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private BlockPos z() {
      if (mc.player == null || mc.level == null) {
         return null;
      }

      if (mc.player.isVisuallyCrawling()) {
         return null;
      }

      BlockPos var1 = BlockPos.containing(mc.player.getEyePosition());
      BlockPos[] var2 = new BlockPos[1];
      BlockPos var3 = var1;
      var2[0] = var3;
      if (BlockInteractionUtil.a_bool_BlockPosArr(var2)) {
         BlockPos[] var4 = new BlockPos[1];
         BlockPos var5 = var1;
         var4[0] = var5;
         if (!BlockInteractionUtil.b_bool_BlockPosArr(var4)) {
            return var1;
         }
      }

      BlockPos var6 = mc.player.blockPosition().above();
      BlockPos[] var7 = new BlockPos[1];
      BlockPos var8 = var6;
      var7[0] = var8;
      if (BlockInteractionUtil.a_bool_BlockPosArr(var7)) {
         BlockPos[] var9 = new BlockPos[1];
         BlockPos var10 = var6;
         var9[0] = var10;
         if (!BlockInteractionUtil.b_bool_BlockPosArr(var9)) {
            return var6;
         }
      }

      return null;
   }

   public boolean b(BlockPos var1) {
      if (this.terrain.getValue() && this.s != null && var1 != null) {
         BlockPos var2 = this.s.pos();
         if (var1.equals(var2) && this.c(var1)) {
            return true;
         }
      }

      return false;
   }

   private boolean b_bool_Player_SpeedMine(Player var1, SpeedMine var2) {
      BlockPos var3 = var1.blockPosition();
      boolean var4 = false;

      for (Direction var6 : Plane.HORIZONTAL) {
         BlockPos var7 = var3;
         Direction var8 = var6;
         BlockPos var9 = var7.relative(var8);
         BlockPos var10 = var9.below();
         BlockPos var11 = var9;
         if (!var2.d_bool_BlockPos(var11)) {
            BlockPos var12 = var10;
            if (!var2.d_bool_BlockPos(var12)) {
               ClientLevel var13 = mc.level;
               BlockPos var14 = var9;
               BlockState var15 = var13.getBlockState(var14);
               if (!(var15.getBlock().defaultDestroyTime() < 0.0F) || var2.s6b.getValue() && var15.getBlock() == Blocks.BEDROCK) {
                  var4 = true;
                  ClientLevel var16 = mc.level;
                  BlockPos var17 = var10;
                  BlockState var18 = var16.getBlockState(var17);
                  boolean var19;
                  if (var18.getBlock() != Blocks.OBSIDIAN && var18.getBlock() != Blocks.BEDROCK) {
                     var19 = false;
                  } else {
                     var19 = true;
                  }

                  if (!var19) {
                     return false;
                  }
               }
            }
         }
      }

      return var4;
   }

   public AutoMine.MineCandidate a_MineCandidate_Player(Player var1) {
      SpeedMine var2 = this.y();
      if (var1 != null && mc.level != null && mc.player != null && var2 != null) {
         SpeedMine var3 = var2;
         if (this.b_bool_Player_SpeedMine(var1, var3)) {
            this.s = null;
            this.t = null;
            return null;
         }

         UUID var4 = var1.getUUID();
         BlockPos var5 = HoleUtil.b(var1);
         if (this.s != null) {
            UUID var6 = var4;
            UUID var7 = this.t;
            if (var6.equals(var7)) {
               BlockPos var8;
               BlockPos var9;
               boolean var14;
               label161: {
                  var8 = this.s.a();
                  var9 = this.s.pos();
                  SpeedMine var10 = var2;
                  BlockPos var11 = var8;
                  if (!var10.d_bool_BlockPos(var11)) {
                     SpeedMine var12 = var2;
                     BlockPos var13 = var9;
                     if (!var12.d_bool_BlockPos(var13)) {
                        var14 = true;
                        break label161;
                     }
                  }

                  var14 = false;
               }

               boolean var15 = var14;
               BlockPos var16 = var5;
               BlockPos var17 = var8;
               boolean var18;
               if (var16.distManhattan(var17) <= 2) {
                  var18 = true;
               } else {
                  var18 = false;
               }

               boolean var19;
               BlockState var25;
               boolean var28;
               label155: {
                  var19 = var18;
                  ClientLevel var20 = mc.level;
                  BlockPos var21 = var8;
                  BlockState var22 = var20.getBlockState(var21);
                  ClientLevel var23 = mc.level;
                  BlockPos var24 = var9;
                  var25 = var23.getBlockState(var24);
                  if (!(var22.getBlock().defaultDestroyTime() >= 0.0F) && (!var2.s6b.getValue() || var22.getBlock() != Blocks.BEDROCK)) {
                     SpeedMine var26 = var2;
                     BlockPos var27 = var8;
                     if (!var26.c_bool_BlockPos(var27)) {
                        var28 = false;
                        break label155;
                     }
                  }

                  var28 = true;
               }

               boolean var29;
               boolean var32;
               label146: {
                  var29 = var28;
                  if (!(var25.getBlock().defaultDestroyTime() >= 0.0F) && var25.getBlock() != Blocks.BEDROCK && var25.getBlock() != Blocks.OBSIDIAN) {
                     List var30 = this.r;
                     BlockPos var31 = var9;
                     if (!var30.contains(var31)) {
                        var32 = false;
                        break label146;
                     }
                  }

                  var32 = true;
               }

               boolean var33 = var32;
               if (var15 && var19 && var29 && var33) {
                  return this.s;
               }
            }
         }

         HashSet var34 = new HashSet();
         if (this.avoidSharing.getValue()) {
            HashSet var35 = var34;
            HashSet var36 = HoleUtil.a_HashSet_Player_bool_bool_bool(mc.player, true, false, true);
            var35.addAll(var36);

            for (BlockPos var38 : (Iterable<BlockPos>)(Object)HoleUtil.a_List_Entity(mc.player)) {
               for (Direction var40 : Plane.HORIZONTAL) {
                  HashSet var41 = var34;
                  BlockPos var42 = var38;
                  Direction var43 = var40;
                  BlockPos var44 = var42.relative(var43);
                  var41.add(var44);
               }
            }
         }

         AutoMine.MineCandidate var45 = null;
         double var46 = Double.MAX_VALUE;

         for (Direction var49 : Plane.HORIZONTAL) {
            BlockPos var50 = var5;
            Direction var51 = var49;
            BlockPos var52 = var50.relative(var51);
            BlockPos var53 = var52.below();
            SpeedMine var54 = var2;
            BlockPos var55 = var52;
            if (!var54.d_bool_BlockPos(var55)) {
               SpeedMine var56 = var2;
               BlockPos var57 = var53;
               if (!var56.d_bool_BlockPos(var57)) {
                  ClientLevel var58 = mc.level;
                  BlockPos var59 = var52;
                  BlockState var60 = var58.getBlockState(var59);
                  ClientLevel var61 = mc.level;
                  BlockPos var62 = var53;
                  BlockState var63 = var61.getBlockState(var62);
                  if (var60.getBlock().defaultDestroyTime() < 0.0F) {
                     SpeedMine var64 = var2;
                     BlockPos var65 = var52;
                     if (!var64.c_bool_BlockPos(var65)) {
                        continue;
                     }
                  }

                  if (var63.getBlock().defaultDestroyTime() < 0.0F && var63.getBlock() != Blocks.BEDROCK && var63.getBlock() != Blocks.OBSIDIAN) {
                     List var66 = this.r;
                     BlockPos var67 = var53;
                     if (!var66.contains(var67)) {
                        continue;
                     }
                  }

                  double var68;
                  var68 = 0.0;
                  label119:
                  if (this.avoidSharing.getValue()) {
                     HashSet var70 = var34;
                     BlockPos var71 = var52;
                     if (!var70.contains(var71)) {
                        HashSet var72 = var34;
                        BlockPos var73 = var53;
                        if (!var72.contains(var73)) {
                           break label119;
                        }
                     }

                     double var74 = var68;
                     double var76 = 100000.0;
                     var68 = var74 + var76;
                  }

                  boolean var78;
                  if (var63.getBlock() != Blocks.OBSIDIAN && var63.getBlock() != Blocks.BEDROCK) {
                     var78 = false;
                  } else {
                     var78 = true;
                  }

                  if (var78) {
                     double var79 = var68;
                     double var81 = 20000.0;
                     var68 = var79 - var81;
                  } else if (!var63.canBeReplaced()) {
                     double var83 = var68;
                     double var85 = var63.getBlock().defaultDestroyTime();
                     double var87 = 10.0;
                     double var89 = var85 * var87;
                     var68 = var83 + var89;
                  }

                  if (!var60.canBeReplaced()) {
                     double var91 = var68;
                     double var93 = 10000.0;
                     var68 = var91 - var93;
                  }

                  boolean var99;
                  label107: {
                     SpeedMine var95 = var2;
                     BlockPos var96 = var52;
                     if (!var95.c_bool_BlockPos(var96)) {
                        SpeedMine var97 = var2;
                        BlockPos var98 = var53;
                        if (!var97.c_bool_BlockPos(var98)) {
                           var99 = false;
                           break label107;
                        }
                     }

                     var99 = true;
                  }

                  if (var99) {
                     double var100 = var68;
                     double var102 = 50000.0;
                     var68 = var100 - var102;
                  }

                  double var104 = var68;
                  LocalPlayer var106 = mc.player;
                  Vec3 var107 = Vec3.atCenterOf((BlockPos)var52);
                  double var108 = var106.distanceToSqr(var107);
                  double var110 = var104 + var108;
                  double var112 = var46;
                  if (var110 < var112) {
                     var46 = var110;
                     Direction var114 = var49;
                     BlockPos var115 = var52;
                     BlockPos var116 = var53;
                     double var117 = var110;
                     var45 = new AutoMine.MineCandidate(var114, var115, var116, var117);
                  }
               }
            }
         }

         AutoMine.MineCandidate var119 = var45;
         this.s = var119;
         UUID var120;
         if (var45 != null) {
            var120 = var4;
         } else {
            var120 = null;
         }

         this.t = var120;
         return var45;
      } else {
         this.s = null;
         this.t = null;
         return null;
      }
   }

   public void w() {
      if (this.terrainPlace.getValue() && !this.r.isEmpty()) {
         if (mc.player != null && mc.level != null) {
            SpeedMine var1 = this.y();
            if (var1 != null) {
               if (var1.whileEating.getValue() || !EntityUtil.a()) {
                  int var2;
                  if (var1.switchSetting.getValue().equalsIgnoreCase("None")) {
                     var2 = -1;
                  } else {
                     Item var3 = Items.OBSIDIAN;
                     int var4;
                     if (!var1.switchSetting.getValue().equalsIgnoreCase("AltSwap") && !var1.switchSetting.getValue().equalsIgnoreCase("AltPickup")) {
                        var4 = InventoryUtil.d;
                     } else {
                        var4 = InventoryUtil.f;
                     }

                     var2 = InventoryUtil.a_int_Item_int_int(var3, 0, var4);
                  }

                  int var5 = (byte)var2;
                  if (var5 == -1) {
                     var5 = InventoryUtil.b(0, 8);
                  }

                  if (var5 != -1) {
                     ArrayList var6 = new ArrayList();
                     List var7 = this.r;

                     for (BlockPos var9 : (Iterable<BlockPos>)(Object)new ArrayList(var7)) {
                        SpeedMine var10 = var1;
                        BlockPos var11 = var9;
                        if (var10.d_bool_BlockPos(var11)) {
                           ArrayList var12 = var6;
                           BlockPos var13 = var9;
                           var12.add(var13);
                        } else {
                           ClientLevel var14 = mc.level;
                           BlockPos var15 = var9;
                           BlockState var16 = var14.getBlockState(var15);
                           if (var16.getBlock() == Blocks.OBSIDIAN) {
                              ArrayList var17 = var6;
                              BlockPos var18 = var9;
                              var17.add(var18);
                              var1.a_void_Predicate(var1x -> {
                                 boolean var2x;
                                 if (!var1x.equals(var9)) {
                                    var2x = true;
                                 } else {
                                    var2x = false;
                                 }

                                 return var2x;
                              });
                              var1.b_void_Predicate(var1x -> {
                                 boolean var2x;
                                 if (!var1x.equals(var9)) {
                                    var2x = true;
                                 } else {
                                    var2x = false;
                                 }

                                 return var2x;
                              });
                           } else if (var16.canBeReplaced() && BlockInteractionUtil.c_bool_BlockPos(var9)) {
                              Direction var19 = BlockInteractionUtil.b_Direction_BlockPos_bool(var9, false);
                              if (var19 == null) {
                                 var19 = BlockInteractionUtil.c(var9, true);
                              }

                              if (var19 != null) {
                                 int var20 = mc.player.getInventory().getSelectedSlot();
                                 String var21 = var1.switchSetting.getValue();
                                 int var22 = var5;
                                 int var23 = var20;
                                 InventoryUtil.a_bool_String_int_int(var21, var22, var23);
                                 BlockPos var24 = var9;
                                 Direction var25 = var19;
                                 InteractionHand var26 = InteractionHand.MAIN_HAND;
                                 boolean var27;
                                 if (!var1.rotate.getValue().equalsIgnoreCase("Packet")
                                    && !var1.rotate.getValue().equalsIgnoreCase("Normal")
                                    && !var1.rotate.getValue().equalsIgnoreCase("Silent")) {
                                    var27 = false;
                                 } else {
                                    var27 = true;
                                 }

                                 boolean var28;
                                 if (!var1.none2.getValue().equalsIgnoreCase("Both") && !var1.none2.getValue().equalsIgnoreCase("Fill")) {
                                    var28 = false;
                                 } else {
                                    var28 = true;
                                 }

                                 boolean var29 = BlockInteractionUtil.a(var24, var25, var26, var27, true, var28);
                                 String var30 = var1.switchSetting.getValue();
                                 int var31 = var5;
                                 int var32 = var20;
                                 InventoryUtil.b(var30, var31, var32);
                                 if (var29) {
                                    ArrayList var33 = var6;
                                    BlockPos var34 = var9;
                                    var33.add(var34);
                                    var1.a_void_Predicate(var1x -> {
                                       boolean var2x;
                                       if (!var1x.equals(var9)) {
                                          var2x = true;
                                       } else {
                                          var2x = false;
                                       }

                                       return var2x;
                                    });
                                    var1.b_void_Predicate(var1x -> {
                                       boolean var2x;
                                       if (!var1x.equals(var9)) {
                                          var2x = true;
                                       } else {
                                          var2x = false;
                                       }

                                       return var2x;
                                    });
                                 }
                              }
                           }
                        }
                     }

                     ArrayList var35 = var6;
                     List var36 = this.r;
                     var35.forEach(var36::remove);
                  }
               }
            }
         }
      }
   }

   @EventTarget
   public void a_void_UpdateWalkingPlayerEvent(UpdateWalkingPlayerEvent var1) {
      double var2 = EntityUtil.b_double_int(5);
      double var4 = 0.9;
      if (!(var2 < var4)) {
         if (!PingBypassState.a()) {
            if (mc.player != null && mc.level != null) {
               SpeedMine var6 = this.y();
               if (var6 != null && var6.isEnabled()) {
                  if (this.antiCrawl.getValue() && mc.player.isVisuallyCrawling()) {
                     BlockPos var25;
                     label446: {
                        BlockPos var7;
                        label445: {
                           var7 = mc.player.blockPosition();
                           BlockPos[] var8 = new BlockPos[1];
                           byte var9 = 0;
                           BlockPos var10 = var7.below();
                           var8[var9] = var10;
                           if (BlockInteractionUtil.a_bool_BlockPosArr(var8)) {
                              BlockPos[] var11 = new BlockPos[1];
                              byte var12 = 0;
                              BlockPos var13 = var7.below();
                              var11[var12] = var13;
                              if (!BlockInteractionUtil.b_bool_BlockPosArr(var11)) {
                                 BlockPos[] var14 = new BlockPos[1];
                                 byte var15 = 0;
                                 BlockPos var16 = var7;
                                 byte var17 = 2;
                                 BlockPos var18 = var16.below(var17);
                                 var14[var15] = var18;
                                 if (!BlockInteractionUtil.b_bool_BlockPosArr(var14)) {
                                    break label445;
                                 }

                                 BlockPos var19 = var7;
                                 byte var20 = 2;
                                 BlockPos var21 = var19.below(var20);
                                 double var22 = 1.0;
                                 boolean var24 = false;
                                 if (HoleUtil.a_HoleInfo_BlockPos_double_bool(var21, var22, var24) != null) {
                                    break label445;
                                 }
                              }
                           }

                           var25 = var7.above();
                           break label446;
                        }

                        var25 = var7.below();
                     }

                     SpeedMine var26 = var6;
                     BlockPos var27 = var25;
                     if (var26.b_bool_BlockPos(var27)) {
                        SpeedMine var28 = var6;
                        BlockPos var29 = var25;
                        if (!var28.d_bool_BlockPos(var29)) {
                           SpeedMine var30 = var6;
                           BlockPos var31 = var25;
                           if (var30.c_bool_BlockPos(var31)) {
                              return;
                           }

                           SpeedMine var32 = var6;
                           BlockPos var33 = var25;
                           byte var34 = 0;
                           var32.a_bool_BlockPos_int(var33, var34);
                           return;
                        }
                     }
                  }

                  if (this.face.getValue()) {
                     BlockPos var35 = this.z();
                     if (var35 != null) {
                        SpeedMine var36 = var6;
                        BlockPos var37 = var35;
                        if (var36.b_bool_BlockPos(var37)) {
                           SpeedMine var38 = var6;
                           BlockPos var39 = var35;
                           if (!var38.d_bool_BlockPos(var39)) {
                              SpeedMine var40 = var6;
                              BlockPos var41 = var35;
                              if (var40.c_bool_BlockPos(var41)) {
                                 return;
                              }

                              SpeedMine var42 = var6;
                              BlockPos var43 = var35;
                              byte var44 = 0;
                              var42.a_bool_BlockPos_int(var43, var44);
                              return;
                           }
                        }
                     }
                  }

                  AutoMine var45 = this;
                  SpeedMine var46 = var6;
                  if (!var45.a_bool_SpeedMine(var46)) {
                     SpeedMine.MineTask var47;
                     SpeedMine.FadeAnimation var48;
                     BlockPos var49;
                     BlockPos var50;
                     BlockPos var51;
                     var47 = var6.v();
                     var48 = var6.w();
                     var49 = BlockPos.containing(mc.player.getEyePosition());
                     var50 = mc.player.blockPosition().above();
                     var51 = mc.player.blockPosition();
                     label433:
                     if (var47 != null && var47.g() == 0) {
                        BlockPos var52 = var47.a();
                        BlockPos var53 = var49;
                        if (!var52.equals(var53)) {
                           BlockPos var54 = var47.a();
                           BlockPos var55 = var50;
                           if (!var54.equals(var55)) {
                              BlockPos var56 = var47.a();
                              BlockPos var57 = var51;
                              if (!var56.equals(var57)) {
                                 BlockPos var58 = var47.a();
                                 BlockPos var59 = var51.below();
                                 if (!var58.equals(var59)) {
                                    break label433;
                                 }
                              }
                           }
                        }

                        ClientLevel var60 = mc.level;
                        BlockPos var61 = var47.a();
                        if (var60.getBlockState(var61).canBeReplaced()) {
                           var6.A();
                           var47 = null;
                        }
                     }

                     label424:
                     if (var48 != null && var48.b() == 0) {
                        BlockPos var62 = var48.a();
                        BlockPos var63 = var49;
                        if (!var62.equals(var63)) {
                           BlockPos var64 = var48.a();
                           BlockPos var65 = var50;
                           if (!var64.equals(var65)) {
                              BlockPos var66 = var48.a();
                              BlockPos var67 = var51;
                              if (!var66.equals(var67)) {
                                 BlockPos var68 = var48.a();
                                 BlockPos var69 = var51.below();
                                 if (!var68.equals(var69)) {
                                    break label424;
                                 }
                              }
                           }
                        }

                        ClientLevel var70 = mc.level;
                        BlockPos var71 = var48.a();
                        if (var70.getBlockState(var71).canBeReplaced()) {
                           var6.B();
                           var48 = null;
                        }
                     }

                     SpeedMine.TargetMineEntry var72 = var6.H();
                     SpeedMine.TargetMineEntry var73;
                     if (var72 == null || this.ignoreNaked.getValue() && EntityUtil.b_bool_Player(var72.player())) {
                        var73 = null;
                     } else {
                        var73 = var72;
                     }

                     SpeedMine.TargetMineEntry var74 = var73;
                     BlockPos var75;
                     if (var48 != null) {
                        var75 = var48.a();
                     } else {
                        var75 = null;
                     }

                     BlockPos var76 = var75;
                     int var77;
                     if (var48 != null) {
                        var77 = var48.b();
                     } else {
                        var77 = 0;
                     }

                     int var78 = var77;
                     if (var47 != null && var47.g() > 0) {
                        BlockPos[] var79 = new BlockPos[1];
                        byte var80 = 0;
                        BlockPos var81 = var47.a();
                        var79[var80] = var81;
                        if (!BlockInteractionUtil.b_bool_BlockPosArr(var79)) {
                           return;
                        }
                     }

                     if (var76 != null && var78 > 0) {
                        BlockPos[] var82 = new BlockPos[1];
                        byte var83 = 0;
                        BlockPos var84 = var76;
                        var82[var83] = var84;
                        if (!BlockInteractionUtil.b_bool_BlockPosArr(var82)) {
                           return;
                        }
                     }

                     if (var47 != null && var47.g() == 0 && var6.s6b.getValue()) {
                        Block var85 = var47.b().getBlock();
                        Block var86 = Blocks.BEDROCK;
                        if (var85 == var86 && var74 != null) {
                           SpeedMine var87 = var6;
                           BlockPos var88 = var47.a();
                           Player var89 = var74.player();
                           if (var87.a_bool_BlockPos_Player(var88, var89)) {
                              return;
                           }
                        }
                     }

                     if (var74 != null) {
                        AutoMine var90 = this;
                        Player var91 = var74.player();
                        SpeedMine.MineTask var92 = var47;
                        SpeedMine.FadeAnimation var93 = var48;
                        SpeedMine var94 = var6;
                        if (var90.a_bool_Player_MineTask_FadeAnimation_SpeedMine(var91, var92, var93, var94)) {
                           return;
                        }
                     }

                     if (var6.doubleSetting.getValue()) {
                        if (!var6.z()) {
                           return;
                        }

                        if (var74 == null) {
                           SpeedMine var95 = var6;
                           boolean var96 = false;
                           var95.b_void_bool(var96);
                        }

                        if (var74 != null) {
                           ArrayList var97 = new ArrayList();
                           ArrayList var98 = var97;
                           List var99 = HoleUtil.a_List_Entity(var74.player());
                           ArrayList var100 = var98;
                           List var101 = var99;
                           var100.addAll(var101);
                           Player var102 = var74.player();
                           boolean var103 = true;
                           boolean var104 = false;
                           boolean var105 = true;
                           HashSet var106 = HoleUtil.a_HashSet_Player_bool_bool_bool(var102, var103, var104, var105);
                           ArrayList var107 = var98;
                           HashSet var108 = var106;
                           var107.addAll(var108);

                           for (BlockPos var110 : (Iterable<BlockPos>)(Object)var106) {
                              ArrayList var111 = var98;
                              BlockPos var112 = var110.below();
                              var111.add(var112);

                              for (Direction var114 : Plane.HORIZONTAL) {
                                 ArrayList var115 = var98;
                                 BlockPos var116 = var110;
                                 Direction var117 = var114;
                                 BlockPos var118 = var116.relative(var117).below();
                                 var115.add(var118);
                              }
                           }

                           for (BlockPos var120 : (Iterable<BlockPos>)(Object)var99) {
                              for (Direction var122 : Plane.HORIZONTAL) {
                                 ArrayList var123 = var98;
                                 BlockPos var124 = var120;
                                 Direction var125 = var122;
                                 BlockPos var126 = var124.relative(var125);
                                 var123.add(var126);
                                 ArrayList var127 = var98;
                                 BlockPos var128 = var120;
                                 Direction var129 = var122;
                                 BlockPos var130 = var128.relative(var129).below();
                                 var127.add(var130);
                              }

                              ArrayList var131 = var98;
                              BlockPos var132 = var120.above();
                              var131.add(var132);
                              ArrayList var133 = var98;
                              BlockPos var134 = var120.below();
                              var133.add(var134);
                           }

                           AutoMine.MineCandidate var137;
                           if (this.terrain.getValue()) {
                              AutoMine var135 = this;
                              Player var136 = var74.player();
                              var137 = var135.a_MineCandidate_Player(var136);
                           } else {
                              var137 = null;
                           }

                           AutoMine.MineCandidate var138 = var137;
                           if (var138 != null) {
                              ArrayList var139 = var98;
                              BlockPos var140 = var138.a();
                              var139.add(var140);
                              ArrayList var141 = var98;
                              BlockPos var142 = var138.pos();
                              var141.add(var142);
                           }

                           String var143 = this.logic.getValue();
                           String var144 = "Grim";
                           BlockPos var148;
                           if (var143.equalsIgnoreCase(var144)) {
                              AutoMine var145 = this;
                              Player var146 = var74.player();
                              SpeedMine var147 = var6;
                              var148 = var145.a_BlockPos_Player_SpeedMine(var146, var147);
                           } else {
                              var148 = null;
                           }

                           BlockPos var149 = var148;
                           if (var149 != null) {
                              ArrayList var150 = var98;
                              BlockPos var151 = var149;
                              var150.add(var151);
                           }

                           if (var47 != null && var47.h()) {
                              AutoMine var152 = this;
                              BlockPos var153 = var47.a();
                              if (var152.c(var153)) {
                                 var6.A();
                                 var47 = null;
                              }
                           }

                           SpeedMine var154 = var6;
                           boolean var155 = true;
                           var154.b_void_bool(var155);
                           if (var47 != null && var47.g() == 0 && !var47.h()) {
                              SpeedMine var156 = var6;
                              BlockPos var157 = var47.a();
                              Player var158 = var74.player();
                              if (!var156.a_bool_BlockPos_Player(var157, var158)) {
                                 var6.A();
                                 Object var159 = null;
                              } else if (!var47.f()) {
                                 BlockPos var160;
                                 boolean var165;
                                 label302: {
                                    var160 = var47.a();
                                    List var161 = HoleUtil.a_List_Entity(var74.player());
                                    BlockPos var162 = var160;
                                    if (var161.contains(var162)) {
                                       ClientLevel var163 = mc.level;
                                       BlockPos var164 = var160;
                                       if (var163.getBlockState(var164).canBeReplaced()) {
                                          var165 = true;
                                          break label302;
                                       }
                                    }

                                    var165 = false;
                                 }

                                 boolean var166 = var165;
                                 ArrayList var167 = var98;
                                 BlockPos var168 = var160;
                                 if (!var167.contains(var168) || var166) {
                                    var6.A();
                                    Object var169 = null;
                                 }
                              }
                           }

                           if (var48 != null && var48.b() == 0 && !var48.d()) {
                              BlockPos var170;
                              boolean var175;
                              label285: {
                                 var170 = var48.a();
                                 List var171 = HoleUtil.a_List_Entity(var74.player());
                                 BlockPos var172 = var170;
                                 if (var171.contains(var172)) {
                                    ClientLevel var173 = mc.level;
                                    BlockPos var174 = var170;
                                    if (var173.getBlockState(var174).canBeReplaced()) {
                                       var175 = true;
                                       break label285;
                                    }
                                 }

                                 var175 = false;
                              }

                              boolean var176 = var175;
                              ArrayList var177 = var98;
                              BlockPos var178 = var170;
                              if (!var177.contains(var178) || var176) {
                                 var6.B();
                                 Object var179 = null;
                              }
                           }

                           AutoMine.MineCandidate var180 = var138;
                           AutoMine var181 = this;
                           AutoMine.MineCandidate var182 = var180;
                           SpeedMine.TargetMineEntry var183 = var74;
                           SpeedMine var184 = var6;
                           Runnable var185 = () -> {
                              if (this.terrain.getValue()) {
                                 if (var182 != null) {
                                    BlockPos var4x = var182.pos();
                                    BlockPos var5 = var182.a();
                                    ClientLevel var6x = mc.level;
                                    BlockPos var7x = var4x;
                                    BlockState var8x = var6x.getBlockState(var7x);
                                    ClientLevel var9x = mc.level;
                                    BlockPos var10x = var5;
                                    BlockState var11x = var9x.getBlockState(var10x);
                                    boolean var12x;
                                    if (var8x.getBlock() != Blocks.OBSIDIAN && var8x.getBlock() != Blocks.BEDROCK) {
                                       var12x = false;
                                    } else {
                                       var12x = true;
                                    }

                                    boolean var13x = var12x;
                                    if (!var13x && this.terrainPlace.getValue()) {
                                       List var14x = this.r;
                                       BlockPos var15x = var4x;
                                       if (!var14x.contains(var15x)) {
                                          List var16x = this.r;
                                          BlockPos var17x = var4x;
                                          var16x.add(var17x);
                                       }
                                    }

                                    Player var18x = var183.player();
                                    if (this.b(var18x)) {
                                       if (var184.D()) {
                                          BlockPos var19x = var184.v().a();
                                          BlockPos var20x = var4x;
                                          if (!var19x.equals(var20x)) {
                                             BlockPos var21x = var19x;
                                             BlockPos var22x = var5;
                                             if (!var21x.equals(var22x)) {
                                                return;
                                             }
                                          }
                                       }

                                       boolean var32x;
                                       if (var184.v() != null && var184.w() != null) {
                                          var32x = false;
                                       } else {
                                          var32x = true;
                                       }

                                       boolean var33x = var32x;
                                       if (!var13x && !var8x.canBeReplaced()) {
                                          BlockPos var34x = var4x;
                                          if (!var184.c_bool_BlockPos(var34x) && var33x) {
                                             BlockPos var35x = var4x;
                                             var184.a_bool_BlockPos_int(var35x, 0);
                                             SpeedMine.MineTask var36x = var184.v();
                                             if (var36x != null) {
                                                BlockPos var37x = var36x.a();
                                                BlockPos var38x = var4x;
                                                if (var37x.equals(var38x)) {
                                                   var36x.a_void_bool(true);
                                                }
                                             }
                                          }
                                       } else if (var13x && !var11x.canBeReplaced()) {
                                          boolean var42x;
                                          label87: {
                                             SpeedMine.MineTask var39x = var184.v();
                                             if (var39x != null) {
                                                BlockPos var40x = var39x.a();
                                                BlockPos var41x = var5;
                                                if (var40x.equals(var41x)) {
                                                   var42x = true;
                                                   break label87;
                                                }
                                             }

                                             var42x = false;
                                          }

                                          if (!var42x) {
                                             BlockPos var43x = var5;
                                             var184.a_bool_BlockPos_int(var43x, 0);
                                             SpeedMine.MineTask var44x = var184.v();
                                             if (var44x != null) {
                                                BlockPos var45x = var44x.a();
                                                BlockPos var46x = var5;
                                                if (var45x.equals(var46x)) {
                                                   var44x.b_void_bool(true);
                                                }
                                             }
                                          }
                                       }
                                    } else {
                                       if (!var13x && !var8x.canBeReplaced()) {
                                          BlockPos var23 = var4x;
                                          if (!var184.c_bool_BlockPos(var23)) {
                                             BlockPos var24x = var4x;
                                             var184.a_bool_BlockPos_int(var24x, 0);
                                          }
                                       }

                                       if (!var11x.canBeReplaced()) {
                                          BlockPos var25x = var5;
                                          if (!var184.c_bool_BlockPos(var25x)) {
                                             if (var13x && var184.doubleSetting.getValue()) {
                                                BlockPos var26x = HoleUtil.a_HashSet_Player_bool_bool_bool(var183.player(), true, false, true)
                                                   .stream()
                                                   .filter(var1xx -> {
                                                      boolean var2xx;
                                                      if (!var1xx.equals(var5)) {
                                                         var2xx = true;
                                                      } else {
                                                         var2xx = false;
                                                      }

                                                      return var2xx;
                                                   })
                                                   .filter(var0 -> {
                                                      boolean var1xx;
                                                      if (!mc.level.getBlockState(var0).canBeReplaced()) {
                                                         var1xx = true;
                                                      } else {
                                                         var1xx = false;
                                                      }

                                                      return var1xx;
                                                   })
                                                   .filter(var1xx -> {
                                                      boolean var2xx;
                                                      if (!var184.c_bool_BlockPos(var1xx) && !var184.a_bool_BlockPos(var1xx) && !var184.d_bool_BlockPos(var1xx)
                                                         )
                                                       {
                                                         var2xx = true;
                                                      } else {
                                                         var2xx = false;
                                                      }

                                                      return var2xx;
                                                   })
                                                   .min(Comparator.comparingDouble(var0 -> {
                                                      LocalPlayer var1xx = mc.player;
                                                      Vec3 var2xx = Vec3.atCenterOf((BlockPos)var0);
                                                      return var1xx.distanceToSqr(var2xx);
                                                   }))
                                                   .orElse(null);
                                                if (var26x != null) {
                                                   BlockPos var27x = var26x;
                                                   var184.a_bool_BlockPos_int(var27x, 0);
                                                }
                                             }

                                             BlockPos var28x = var5;
                                             var184.a_bool_BlockPos_int(var28x, 0);
                                             SpeedMine.MineTask var29x = var184.v();
                                             if (var29x != null) {
                                                BlockPos var30x = var29x.a();
                                                BlockPos var31x = var5;
                                                if (var30x.equals(var31x)) {
                                                   var29x.b_void_bool(true);
                                                }
                                             }
                                          }
                                       }
                                    }
                                 }
                              }
                           };
                           AutoMine var186 = this;
                           SpeedMine var187 = var6;
                           SpeedMine.TargetMineEntry var188 = var74;
                           AutoMine.MineCandidate var189 = var180;
                           Runnable var190 = () -> {
                              SpeedMine.MineTask var4x;
                              boolean var7x;
                              label163: {
                                 var4x = var187.v();
                                 if (var4x != null && var187.D()) {
                                    List var5 = HoleUtil.a_List_Entity(var188.player());
                                    BlockPos var6x = var4x.a();
                                    if (var5.contains(var6x)) {
                                       var7x = true;
                                       break label163;
                                    }
                                 }

                                 var7x = false;
                              }

                              boolean var8x = var7x;
                              if (!var187.D() || var8x) {
                                 if (var4x == null || !var187.s6b.getValue() || var4x.b().getBlock() != Blocks.BEDROCK) {
                                    if (HoleUtil.a_List_Entity(var188.player()).stream().filter(var0 -> {
                                       boolean var1xx;
                                       if (!mc.level.getBlockState(var0).canBeReplaced()) {
                                          var1xx = true;
                                       } else {
                                          var1xx = false;
                                       }

                                       return var1xx;
                                    }).toList().size() < 2) {
                                       if (!this.terrain.getValue() || var4x == null || !var4x.i() || var4x.j()) {
                                          label151:
                                          if (this.terrain.getValue() && var189 != null) {
                                             ClientLevel var9x = mc.level;
                                             BlockPos var10x = var189.pos();
                                             BlockState var11x = var9x.getBlockState(var10x);
                                             boolean var12x;
                                             if (var11x.getBlock() != Blocks.OBSIDIAN && var11x.getBlock() != Blocks.BEDROCK) {
                                                var12x = false;
                                             } else {
                                                var12x = true;
                                             }

                                             if (!var12x) {
                                                return;
                                             }

                                             BlockPos var13x = var189.pos();
                                             if (!var187.c_bool_BlockPos(var13x)) {
                                                BlockPos var14x = var189.a();
                                                if (!var187.c_bool_BlockPos(var14x)) {
                                                   break label151;
                                                }
                                             }

                                             if (var187.E()) {
                                                return;
                                             }
                                          }

                                          if (this.logic.getValue().equals("NCP")) {
                                             for (BlockPos var16x : HoleUtil.a_HashSet_Player_bool_bool_bool(var188.player(), true, false, true)
                                                .stream()
                                                .filter(var0 -> {
                                                   boolean var1xx;
                                                   if (!mc.level.getBlockState(var0).canBeReplaced()) {
                                                      var1xx = true;
                                                   } else {
                                                      var1xx = false;
                                                   }

                                                   return var1xx;
                                                })
                                                .filter(var1xx -> {
                                                   boolean var2xx;
                                                   if (this.v() && !BlockInteractionUtil.f(var1xx)) {
                                                      var2xx = false;
                                                   } else {
                                                      var2xx = true;
                                                   }

                                                   return var2xx;
                                                })
                                                .toList()) {
                                                if (var187.E()) {
                                                   break;
                                                }

                                                BlockPos var17x = var16x;
                                                if (!var187.c_bool_BlockPos(var17x)) {
                                                   BlockPos var18x = var16x;
                                                   if (!var187.a_bool_BlockPos(var18x)) {
                                                      BlockPos var19x = var16x;
                                                      if (!var187.d_bool_BlockPos(var19x)) {
                                                         BlockPos var20x = var16x;
                                                         var187.a_bool_BlockPos_int(var20x, 0);
                                                      }
                                                   }
                                                }
                                             }
                                          } else {
                                             HashSet var21x = new HashSet();
                                             if (this.avoidSharing.getValue() && mc.player != null) {
                                                HashSet var22x = var21x;
                                                HashSet var23 = HoleUtil.a_HashSet_Player_bool_bool_bool(mc.player, true, false, true);
                                                var22x.addAll(var23);

                                                for (BlockPos var25x : (Iterable<BlockPos>)(Object)HoleUtil.a_List_Entity(mc.player)) {
                                                   for (Direction var27x : Plane.HORIZONTAL) {
                                                      HashSet var28x = var21x;
                                                      BlockPos var29x = var25x;
                                                      Direction var30x = var27x;
                                                      BlockPos var31x = var29x.relative(var30x);
                                                      var28x.add(var31x);
                                                   }
                                                }
                                             }

                                             HashSet var32x = var21x;
                                             List var33x = HoleUtil.a_HashSet_Player_bool_bool_bool(var188.player(), true, false, true)
                                                .stream()
                                                .filter(var0 -> {
                                                   boolean var1xx;
                                                   if (!mc.level.getBlockState(var0).canBeReplaced()) {
                                                      var1xx = true;
                                                   } else {
                                                      var1xx = false;
                                                   }

                                                   return var1xx;
                                                })
                                                .filter(var1xx -> {
                                                   boolean var2xx;
                                                   if (this.v() && !BlockInteractionUtil.f(var1xx)) {
                                                      var2xx = false;
                                                   } else {
                                                      var2xx = true;
                                                   }

                                                   return var2xx;
                                                })
                                                .sorted(Comparator.comparingDouble(var4xx -> {
                                                   double var5x;
                                                   if (this.avoidSharing.getValue() && var32x.contains(var4xx)) {
                                                      var5x = 100000.0;
                                                   } else {
                                                      var5x = 0.0;
                                                   }

                                                   double var7xx = var5x;
                                                   if (this.v() && !BlockInteractionUtil.f(var4xx)) {
                                                      double var9xx = var7xx;
                                                      double var11xx = 500000.0;
                                                      var7xx = var9xx + var11xx;
                                                   } else if (BlockInteractionUtil.f(var4xx)) {
                                                      double var13xx = var7xx;
                                                      double var15x = 50000.0;
                                                      var7xx = var13xx - var15x;
                                                   }

                                                   if (var187.s6b.getValue() && mc.level.getBlockState(var4xx).getBlock() == Blocks.BEDROCK) {
                                                      double var17xx = var7xx;
                                                      double var19xx = 200000.0;
                                                      var7xx = var17xx - var19xx;
                                                   }

                                                   label62: {
                                                      if (this.terrain.getValue() && var189 != null) {
                                                         BlockPos var21xx = var189.a();
                                                         if (var4xx.equals(var21xx)) {
                                                            double var22xx = var7xx;
                                                            double var24x = 100000.0;
                                                            var7xx = var22xx - var24x;
                                                            break label62;
                                                         }
                                                      }

                                                      BlockPos var26x = var4xx.below();
                                                      ClientLevel var27xx = mc.level;
                                                      BlockPos var28xx = var26x;
                                                      BlockState var29xx = var27xx.getBlockState(var28xx);
                                                      boolean var30xx;
                                                      if (var29xx.getBlock() != Blocks.OBSIDIAN && var29xx.getBlock() != Blocks.BEDROCK) {
                                                         var30xx = false;
                                                      } else {
                                                         var30xx = true;
                                                      }

                                                      boolean var31xx = var30xx;
                                                      BlockPos var32xx = var26x;
                                                      if (var187.c_bool_BlockPos(var32xx)) {
                                                         double var33xx = var7xx;
                                                         double var35x = 50000.0;
                                                         var7xx = var33xx - var35x;
                                                      } else if (var31xx) {
                                                         double var37xx = var7xx;
                                                         double var39xx = 20000.0;
                                                         var7xx = var37xx - var39xx;
                                                      } else if (this.terrain.getValue()) {
                                                         double var41xx = var7xx;
                                                         double var43xx = 50000.0;
                                                         var7xx = var41xx + var43xx;
                                                      }
                                                   }

                                                   double var45xx = var7xx;
                                                   LocalPlayer var47x = mc.player;
                                                   Vec3 var48x = Vec3.atCenterOf((BlockPos)var4xx);
                                                   double var49x = var47x.distanceToSqr(var48x);
                                                   return var45xx + var49x;
                                                }))
                                                .toList();
                                             boolean var34x = var8x;

                                             for (BlockPos var36x : (Iterable<BlockPos>)(Object)var33x) {
                                                if (!var34x && var187.E()) {
                                                   break;
                                                }

                                                BlockPos var37x = var36x;
                                                if (!var187.c_bool_BlockPos(var37x)) {
                                                   BlockPos var38x = var36x;
                                                   if (var187.a_bool_BlockPos(var38x)) {
                                                      continue;
                                                   }

                                                   BlockPos var39x = var36x;
                                                   if (var187.d_bool_BlockPos(var39x)) {
                                                      continue;
                                                   }

                                                   boolean var42x;
                                                   label117: {
                                                      if (var187.s6b.getValue()) {
                                                         ClientLevel var40x = mc.level;
                                                         BlockPos var41x = var36x;
                                                         if (var40x.getBlockState(var41x).getBlock() == Blocks.BEDROCK) {
                                                            var42x = true;
                                                            break label117;
                                                         }
                                                      }

                                                      var42x = false;
                                                   }

                                                   if (var42x && var187.doubleSetting.getValue() && var187.v() == null) {
                                                      BlockPos var43x = ((List<BlockPos>)var33x).stream()
                                                         .filter(var1xx -> {
                                                            boolean var2xx;
                                                            if (!var1xx.equals(var36x)) {
                                                               var2xx = true;
                                                            } else {
                                                               var2xx = false;
                                                            }

                                                            return var2xx;
                                                         })
                                                         .filter(var0 -> {
                                                            boolean var1xx;
                                                            if (mc.level.getBlockState(var0).getBlock() != Blocks.BEDROCK) {
                                                               var1xx = true;
                                                            } else {
                                                               var1xx = false;
                                                            }

                                                            return var1xx;
                                                         })
                                                         .filter(
                                                            var1xx -> {
                                                               boolean var2xx;
                                                               if (!var187.c_bool_BlockPos(var1xx)
                                                                  && !var187.a_bool_BlockPos(var1xx)
                                                                  && !var187.d_bool_BlockPos(var1xx)) {
                                                                  var2xx = true;
                                                               } else {
                                                                  var2xx = false;
                                                               }

                                                               return var2xx;
                                                            }
                                                         )
                                                         .findFirst()
                                                         .orElse(null);
                                                      if (var43x != null) {
                                                         BlockPos var44x = var43x;
                                                         var187.a_bool_BlockPos_int(var44x, 0);
                                                      }
                                                   }

                                                   BlockPos var45x = var36x;
                                                   var187.a_bool_BlockPos_int(var45x, 0);
                                                   var34x = false;
                                                }

                                                if (!var34x && var187.E()) {
                                                   break;
                                                }
                                             }
                                          }
                                       }
                                    }
                                 }
                              }
                           };
                           AutoMine var191 = this;
                           SpeedMine var192 = var6;
                           SpeedMine.TargetMineEntry var193 = var74;
                           Runnable var194 = () -> {
                              SpeedMine.MineTask var3 = var192.v();
                              if (!this.terrain.getValue() || var3 == null || !var3.i() || var3.j()) {
                                 int var4x = Mth.floor(var193.player().getY());

                                 for (BlockPos var6x : HoleUtil.a_List_Entity(var193.player()).stream().filter(var0 -> {
                                    boolean var1xx;
                                    if (!mc.level.getBlockState(var0).canBeReplaced()) {
                                       var1xx = true;
                                    } else {
                                       var1xx = false;
                                    }

                                    return var1xx;
                                 }).filter(var1xx -> {
                                    boolean var2xx;
                                    if (this.v() && !BlockInteractionUtil.f(var1xx)) {
                                       var2xx = false;
                                    } else {
                                       var2xx = true;
                                    }

                                    return var2xx;
                                 }).sorted(Comparator.comparingDouble(var1xx -> {
                                    double var2xx;
                                    if (var1xx.getY() > var4x) {
                                       var2xx = 1.0;
                                    } else {
                                       var2xx = 0.0;
                                    }

                                    return var2xx;
                                 })).toList()) {
                                    if (!var192.a_bool_BlockPos(var6x)) {
                                       BlockPos var7x = var6x;
                                       if (!var192.d_bool_BlockPos(var7x)) {
                                          if (var192.F()) {
                                             break;
                                          }

                                          BlockPos var8x = var6x;
                                          var192.a_bool_BlockPos_int(var8x, 0);
                                          if (var192.F()) {
                                             break;
                                          }
                                       }
                                    }
                                 }
                              }
                           };
                           BlockPos var195 = null;
                           String var196 = this.priority.getValue();
                           String var197 = "Head";
                           if (var196.equalsIgnoreCase(var197)) {
                              AutoMine var198 = this;
                              Player var199 = var74.player();
                              SpeedMine var200 = var6;
                              var195 = var198.c_BlockPos_Player_SpeedMine(var199, var200);
                           }

                           if (var195 == null) {
                              String var201 = this.logic.getValue();
                              String var202 = "Grim";
                              if (var201.equalsIgnoreCase(var202)) {
                                 AutoMine var203 = this;
                                 Player var204 = var74.player();
                                 SpeedMine var205 = var6;
                                 var195 = var203.a_BlockPos_Player_SpeedMine(var204, var205);
                              }
                           }

                           if (var195 != null) {
                              SpeedMine var206 = var6;
                              BlockPos var207 = var195;
                              if (!var206.c_bool_BlockPos(var207)) {
                                 SpeedMine var208 = var6;
                                 BlockPos var209 = var195;
                                 byte var210 = 0;
                                 var208.a_bool_BlockPos_int(var209, var210);
                              }
                           }

                           if (var195 == null) {
                              var194.run();
                           }

                           if (var195 == null && this.terrain.getValue()) {
                              var185.run();
                           }

                           if (var195 == null) {
                              var190.run();
                           }
                        }
                     } else {
                        BlockPos var211 = null;
                        BlockPos var212 = null;
                        boolean var213 = false;
                        if (var74 == null) {
                           return;
                        }

                        if (var47 != null && var47.g() == 0 && !var47.h()) {
                           boolean var217;
                           boolean var228;
                           label384: {
                              SpeedMine var214 = var6;
                              BlockPos var215 = var47.a();
                              Player var216 = var74.player();
                              var217 = var214.a_bool_BlockPos_Player(var215, var216);
                              List var218 = HoleUtil.a_List_Entity(var74.player());
                              SpeedMine.MineTask var219 = var47;
                              BlockPos var220 = var219.a();
                              boolean var221 = var218.contains(var220);
                              if (var221) {
                                 Minecraft var222 = mc;
                                 ClientLevel var223 = var222.level;
                                 SpeedMine.MineTask var224 = var47;
                                 BlockPos var225 = var224.a();
                                 BlockState var226 = var223.getBlockState(var225);
                                 boolean var227 = var226.canBeReplaced();
                                 if (var227) {
                                    var228 = true;
                                    break label384;
                                 }
                              }

                              var228 = false;
                           }

                           boolean var229 = var228;
                           boolean var230 = var217;
                           if (!var230) {
                              SpeedMine var231 = var6;
                              var231.A();
                              Object var232 = null;
                              var47 = (SpeedMine.MineTask)var232;
                           } else {
                              boolean var233 = var229;
                              if (var233) {
                                 SpeedMine var234 = var6;
                                 var234.A();
                                 Object var235 = null;
                                 var47 = (SpeedMine.MineTask)var235;
                              } else {
                                 SpeedMine var236 = var6;
                                 BooleanSetting var237 = var236.s6b;
                                 boolean var238 = var237.getValue();
                                 if (var238) {
                                    SpeedMine.MineTask var239 = var47;
                                    BlockState var240 = var239.b();
                                    Block var241 = var240.getBlock();
                                    Block var242 = Blocks.BEDROCK;
                                    if (var241 == var242) {
                                       return;
                                    }
                                 }
                              }
                           }
                        }

                        AutoMine var243 = this;
                        ModeSetting var244 = var243.priority;
                        String var245 = var244.getValue();
                        String var246 = "Head";
                        boolean var247 = var245.equalsIgnoreCase(var246);
                        if (var247) {
                           AutoMine var248 = this;
                           SpeedMine.TargetMineEntry var249 = var74;
                           Player var250 = var249.player();
                           SpeedMine var251 = var6;
                           BlockPos var252 = var248.c_BlockPos_Player_SpeedMine(var250, var251);
                           var211 = var252;
                        }

                        BlockPos var253 = var211;
                        if (var253 == null) {
                           AutoMine var254 = this;
                           ModeSetting var255 = var254.logic;
                           String var256 = var255.getValue();
                           String var257 = "Grim";
                           boolean var258 = var256.equalsIgnoreCase(var257);
                           if (var258) {
                              AutoMine var259 = this;
                              SpeedMine.TargetMineEntry var260 = var74;
                              Player var261 = var260.player();
                              SpeedMine var262 = var6;
                              BlockPos var263 = var259.a_BlockPos_Player_SpeedMine(var261, var262);
                              var212 = var263;
                              BlockPos var264 = var212;
                              var211 = var264;
                           }
                        }

                        BlockPos var265 = var211;
                        if (var265 == null) {
                           AutoMine var266 = this;
                           BooleanSetting var267 = var266.terrain;
                           boolean var268 = var267.getValue();
                           if (var268) {
                              AutoMine var269 = this;
                              SpeedMine.TargetMineEntry var270 = var74;
                              Player var271 = var270.player();
                              AutoMine.MineCandidate var272 = var269.a_MineCandidate_Player(var271);
                              AutoMine.MineCandidate var273 = var272;
                              AutoMine.MineCandidate var274 = var273;
                              label368:
                              if (var274 != null) {
                                 BlockState var280;
                                 BlockState var286;
                                 boolean var293;
                                 label366: {
                                    Minecraft var275 = mc;
                                    ClientLevel var276 = var275.level;
                                    AutoMine.MineCandidate var277 = var273;
                                    BlockPos var278 = var277.pos();
                                    BlockState var279 = var276.getBlockState(var278);
                                    var280 = var279;
                                    Minecraft var281 = mc;
                                    ClientLevel var282 = var281.level;
                                    AutoMine.MineCandidate var283 = var273;
                                    BlockPos var284 = var283.a();
                                    BlockState var285 = var282.getBlockState(var284);
                                    var286 = var285;
                                    BlockState var287 = var280;
                                    Block var288 = var287.getBlock();
                                    Block var289 = Blocks.OBSIDIAN;
                                    if (var288 != var289) {
                                       BlockState var290 = var280;
                                       Block var291 = var290.getBlock();
                                       Block var292 = Blocks.BEDROCK;
                                       if (var291 != var292) {
                                          var293 = false;
                                          break label366;
                                       }
                                    }

                                    var293 = true;
                                 }

                                 boolean var294 = var293;
                                 boolean var295 = var294;
                                 if (!var295) {
                                    BlockState var296 = var280;
                                    boolean var297 = var296.canBeReplaced();
                                    if (!var297) {
                                       AutoMine.MineCandidate var298 = var273;
                                       BlockPos var299 = var298.pos();
                                       var211 = var299;
                                       boolean var300 = true;
                                       var213 = var300;
                                       AutoMine var301 = this;
                                       BooleanSetting var302 = var301.terrainPlace;
                                       boolean var303 = var302.getValue();
                                       if (var303) {
                                          AutoMine var304 = this;
                                          List var305 = var304.r;
                                          BlockPos var306 = var211;
                                          boolean var307 = var305.contains(var306);
                                          if (!var307) {
                                             AutoMine var308 = this;
                                             List var309 = var308.r;
                                             BlockPos var310 = var211;
                                             var309.add(var310);
                                          }
                                       }
                                       break label368;
                                    }
                                 }

                                 BlockState var311 = var286;
                                 boolean var312 = var311.canBeReplaced();
                                 if (!var312) {
                                    AutoMine.MineCandidate var313 = var273;
                                    BlockPos var314 = var313.a();
                                    var211 = var314;
                                 }
                              }
                           }
                        }

                        BlockPos var315 = var211;
                        if (var315 == null) {
                           SpeedMine.TargetMineEntry var316 = var74;
                           Player var317 = var316.player();
                           double var318 = var317.getY();
                           int var320 = Mth.floor(var318);
                           int var321 = var320;
                           SpeedMine.TargetMineEntry var322 = var74;
                           Player var323 = var322.player();
                           List var324 = HoleUtil.a_List_Entity(var323);
                           Stream<BlockPos> var325 = var324.stream();
                           int var326 = var321;
                           Predicate<BlockPos> var327 = var1x -> {
                              boolean var2x;
                              if (var1x.getY() <= var326) {
                                 var2x = true;
                              } else {
                                 var2x = false;
                              }

                              return var2x;
                           };
                           Stream<BlockPos> var328 = var325.filter(var327);
                           Predicate<BlockPos> var329 = var0 -> {
                              boolean var1x;
                              if (!mc.level.getBlockState(var0).canBeReplaced()) {
                                 var1x = true;
                              } else {
                                 var1x = false;
                              }

                              return var1x;
                           };
                           Stream<BlockPos> var330 = var328.filter(var329);
                           AutoMine var331 = this;
                           Predicate<BlockPos> var332 = var1x -> {
                              boolean var2x;
                              if (this.v() && !BlockInteractionUtil.f((BlockPos)var1x)) {
                                 var2x = false;
                              } else {
                                 var2x = true;
                              }

                              return var2x;
                           };
                           Stream<BlockPos> var333 = var330.filter(var332);
                           List var334 = var333.toList();
                           List var335 = var334;
                           List var336 = var335;
                           boolean var337 = var336.isEmpty();
                           BlockPos var341;
                           if (!var337) {
                              List var338 = var335;
                              byte var339 = 0;
                              Object var340 = var338.get(var339);
                              var341 = (BlockPos)var340;
                           } else {
                              SpeedMine.TargetMineEntry var342 = var74;
                              var341 = var342.b();
                           }

                           var211 = var341;
                           BlockPos var343 = var211;
                           if (var343 != null) {
                              AutoMine var344 = this;
                              boolean var345 = var344.v();
                              if (var345) {
                                 BlockPos var346 = var211;
                                 boolean var347 = BlockInteractionUtil.f(var346);
                                 if (!var347) {
                                    Object var348 = null;
                                    var211 = (BlockPos)var348;
                                 }
                              }
                           }
                        }

                        BlockPos var349 = var211;
                        if (var349 == null) {
                           return;
                        }

                        SpeedMine.MineTask var350 = var47;
                        if (var350 != null) {
                           BlockPos var351 = var211;
                           SpeedMine.MineTask var352 = var47;
                           BlockPos var353 = var352.a();
                           boolean var354 = var351.equals(var353);
                           if (var354) {
                              return;
                           }
                        }

                        SpeedMine.MineTask var355 = var47;
                        if (var355 != null) {
                           SpeedMine.MineTask var356 = var47;
                           int var357 = var356.g();
                           if (var357 == 0) {
                              SpeedMine.MineTask var358 = var47;
                              boolean var359 = var358.h();
                              if (!var359) {
                                 SpeedMine var360 = var6;
                                 SpeedMine.MineTask var361 = var47;
                                 BlockPos var362 = var361.a();
                                 SpeedMine.TargetMineEntry var363 = var74;
                                 Player var364 = var363.player();
                                 boolean var365 = var360.a_bool_BlockPos_Player(var362, var364);
                                 if (var365) {
                                    BlockPos var366 = var212;
                                    if (var366 == null) {
                                       return;
                                    }

                                    BlockPos var367 = var211;
                                    BlockPos var368 = var212;
                                    boolean var369 = var367.equals(var368);
                                    if (!var369) {
                                       return;
                                    }
                                 }
                              }
                           }
                        }

                        SpeedMine var370 = var6;
                        BlockPos var371 = var211;
                        byte var372 = 0;
                        var370.a_bool_BlockPos_int(var371, var372);
                        boolean var373 = var213;
                        if (var373) {
                           SpeedMine var374 = var6;
                           SpeedMine.MineTask var375 = var374.v();
                           SpeedMine.MineTask var376 = var375;
                           SpeedMine.MineTask var377 = var376;
                           if (var377 != null) {
                              SpeedMine.MineTask var378 = var376;
                              BlockPos var379 = var378.a();
                              BlockPos var380 = var211;
                              boolean var381 = var379.equals(var380);
                              if (var381) {
                                 SpeedMine.MineTask var382 = var376;
                                 boolean var383 = true;
                                 var382.a_void_bool(var383);
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

   private boolean a_bool_Player_MineTask_FadeAnimation_SpeedMine(Player var1, SpeedMine.MineTask var2, SpeedMine.FadeAnimation var3, SpeedMine var4) {
      if (!var1.onGround() && !EntityUtil.d_bool_Entity(var1)) {
         BlockPos var5;
         if (var2 != null && var2.g() == 0) {
            var5 = var2.a();
         } else {
            var5 = null;
         }

         if (!this.a_bool_BlockPos_Player_SpeedMine(var5, var1, var4)) {
            BlockPos var6;
            if (var3 != null && var3.b() == 0) {
               var6 = var3.a();
            } else {
               var6 = null;
            }

            if (!this.a_bool_BlockPos_Player_SpeedMine(var6, var1, var4)) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private boolean a_bool_BlockPos_Player_SpeedMine(BlockPos var1, Player var2, SpeedMine var3) {
      if (var1 != null) {
         int var4 = var1.getY();
         int var5 = Mth.floor(var2.getY()) - 2;
         if (var4 >= var5) {
            if (!var3.a_bool_BlockPos_Player(var1, var2)) {
               BlockPos var6 = var1.above();
               if (!var3.a_bool_BlockPos_Player(var6, var2)) {
                  BlockPos var7 = var1.above(2);
                  if (!var3.a_bool_BlockPos_Player(var7, var2)) {
                     return false;
                  }
               }
            }

            return true;
         }
      }

      return false;
   }

   private BlockPos c_BlockPos_Player_SpeedMine(Player var1, SpeedMine var2) {
      if (var1 != null && mc.level != null) {
         int var3 = Mth.floor(var1.getY()) + 2;
         SpeedMine.MineTask var4 = var2.v();
         if (var4 != null && var4.f()) {
            int var5 = var4.a().getY();
            int var6 = var3;
            if (var5 == var6) {
               BlockPos var7 = var4.a();
               if (var2.a_bool_BlockPos_Player(var7, var1)) {
                  return var4.a();
               }
            }
         }

         for (BlockPos var9 : (Iterable<BlockPos>)(Object)HoleUtil.a_List_Entity(var1)) {
            int var10 = var9.getX();
            int var11 = var3;
            int var12 = var9.getZ();
            BlockPos var13 = new BlockPos(var10, var11, var12);
            if (var2.b_bool_BlockPos(var13)) {
               BlockPos var14 = var13;
               if (!var2.d_bool_BlockPos(var14) && (!this.v() || BlockInteractionUtil.f(var13))) {
                  ClientLevel var15 = mc.level;
                  BlockPos var16 = var13;
                  if (!var15.getBlockState(var16).canBeReplaced()) {
                     return var13;
                  }
               }
            }
         }

         BlockPos var17 = var1.blockPosition().above(2);
         if (var2.b_bool_BlockPos(var17)) {
            BlockPos var18 = var17;
            if (!var2.d_bool_BlockPos(var18) && (!this.v() || BlockInteractionUtil.f(var17))) {
               ClientLevel var19 = mc.level;
               BlockPos var20 = var17;
               if (!var19.getBlockState(var20).canBeReplaced()) {
                  return var17;
               }
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private boolean a_bool_SpeedMine(SpeedMine var1) {
      if (this.priority.getValue().equalsIgnoreCase("Leg") && this.raytrace.getValue() && this.civBreak.getValue()) {
         SpeedMine.TargetMineEntry var2 = var1.H();
         Player var3;
         if (var2 != null) {
            var3 = var2.player();
         } else {
            var3 = null;
         }

         Player var4 = var3;
         if (var4 == null || EntityUtil.c_bool_Entity(var4)) {
            this.x();
            return false;
         }

         if (!HoleUtil.a_bool_Player(var4)) {
            this.x();
            return false;
         }

         HashSet<BlockPos> var5 = HoleUtil.a_HashSet_Player_bool_bool_bool(var4, true, false, true);
         if (this.u != null) {
            HashSet var6 = var5;
            BlockPos var7 = this.u.below();
            if (var6.contains(var7)) {
               Player var16 = var4;
               if (!this.b(var16)) {
                  Player var17 = var4;
                  BlockPos var18 = this.u;
                  if (!this.a_bool_SpeedMine_Player_BlockPos(var1, var17, var18)) {
                     BlockPos var19 = this.u;
                     return this.a_bool_SpeedMine_BlockPos(var1, var19);
                  }
               }

               this.x();
               return false;
            }

            this.x();
         }

         boolean var8 = var5.stream().anyMatch(var0 -> {
            boolean var1x;
            if (!mc.level.getBlockState(var0).canBeReplaced()) {
               var1x = true;
            } else {
               var1x = false;
            }

            return var1x;
         });
         boolean var9 = var5.stream().anyMatch(var2x -> {
            boolean var3x;
            if (this.a_bool_BlockPos_SpeedMine(var2x, var1) && BlockInteractionUtil.f(var2x)) {
               var3x = true;
            } else {
               var3x = false;
            }

            return var3x;
         });
         if (var8 && !var9) {
            Player var10 = var4;
            if (this.b(var10)) {
               return false;
            }

            BlockPos var11 = null;
            Iterator var12 = var5.iterator();

            while (var12.hasNext()) {
               BlockPos var13 = ((BlockPos)var12.next()).above();
               if (!var1.d_bool_BlockPos(var13) && BlockInteractionUtil.f(var13)) {
                  var11 = var13;
                  break;
               }
            }

            if (var11 == null) {
               return false;
            }

            BlockPos var14 = var11;
            this.u = var14;
            BlockPos var15 = var11;
            return this.a_bool_SpeedMine_BlockPos(var1, var15);
         } else {
            return false;
         }
      } else {
         this.x();
         return false;
      }
   }

   private boolean a_bool_SpeedMine_Player_BlockPos(SpeedMine var1, Player var2, BlockPos var3) {
      AutoCrystal var4 = Night.moduleManager.getModule(AutoCrystal.class);
      if (var4 == null) {
         return false;
      }

      float var5 = var4.v();
      if (var5 <= 0.0F) {
         return false;
      }

      BlockPos var6 = var3.above();
      int var7 = var4.extrapolation.getValue().intValue();
      AABB var8 = PositionUtil.a(var2, var7);
      BlockPos var9 = var6;
      boolean var10 = var4.ignoreTerrain.getValue();
      float var11 = DamageUtil.a(var2, var8, var9, var3, var10);
      float var12 = var5;
      float var13 = var11;
      boolean var14;
      if (var12 > var13) {
         var14 = true;
      } else {
         var14 = false;
      }

      return var14;
   }

   private boolean a_bool_SpeedMine_BlockPos(SpeedMine var1, BlockPos var2) {
      if (mc.level.getBlockState(var2).canBeReplaced()) {
         this.b_void_SpeedMine_BlockPos(var1, var2);
         return true;
      }

      if (!var1.c_bool_BlockPos(var2) && var1.b_bool_BlockPos(var2) && !var1.d_bool_BlockPos(var2)) {
         var1.a_bool_BlockPos_int(var2, 0);
      }

      return true;
   }

   private void b_void_SpeedMine_BlockPos(SpeedMine var1, BlockPos var2) {
      if (BlockInteractionUtil.c_bool_BlockPos(var2)) {
         AutoCrystal var3 = Night.moduleManager.getModule(AutoCrystal.class);
         String var4;
         if (var3 != null) {
            var4 = var3.switchSetting.getValue();
         } else {
            var4 = "Silent";
         }

         String var5 = var4;
         boolean var6 = var5.equalsIgnoreCase("AltSwap");
         Item var7 = Items.OBSIDIAN;
         int var8;
         if (var6) {
            var8 = InventoryUtil.f;
         } else {
            var8 = InventoryUtil.d;
         }

         int var9 = InventoryUtil.a_int_Item_int_int(var7, 0, var8);
         if (var9 == -1) {
            var9 = InventoryUtil.b(0, 8);
         }

         if (var9 != -1) {
            Direction var10 = BlockInteractionUtil.b_Direction_BlockPos_bool(var2, false);
            if (var10 == null) {
               var10 = BlockInteractionUtil.c(var2, true);
            }

            if (var10 != null) {
               int var11 = mc.player.getInventory().getSelectedSlot();
               String var12 = var5;
               int var13 = var9;
               int var14 = var11;
               InventoryUtil.a_bool_String_int_int(var12, var13, var14);
               Direction var15 = var10;
               InteractionHand var16 = InteractionHand.MAIN_HAND;
               boolean var17 = this.rotate.getValue();
               boolean var18;
               if (!var1.none2.getValue().equalsIgnoreCase("Both") && !var1.none2.getValue().equalsIgnoreCase("Fill")) {
                  var18 = false;
               } else {
                  var18 = true;
               }

               BlockInteractionUtil.a(var2, var15, var16, var17, true, var18);
               String var19 = var5;
               int var20 = var9;
               int var21 = var11;
               InventoryUtil.b(var19, var20, var21);
            }
         }
      }
   }

   public record MineCandidate(Direction direction, BlockPos pos, BlockPos pos2, double distance) {
      public MineCandidate(Direction direction, BlockPos pos, BlockPos pos2, double distance) {
         this.direction = direction;
         this.pos = pos;
         this.pos2 = pos2;
         double var6 = distance;
         this.distance = var6;
      }

      public BlockPos a() {
         return this.pos;
      }

      public BlockPos pos() {
         return this.pos2;
      }
   }
}
