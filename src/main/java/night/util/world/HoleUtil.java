package night.util.world;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.module.impl.movement.HitboxDesync;
import night.util.MinecraftInstance;

public class HoleUtil implements MinecraftInstance {
   private static final Vec3i[] a = new Vec3i[]{new Vec3i(0, -1, 0), new Vec3i(1, 0, 0), new Vec3i(-1, 0, 0), new Vec3i(0, 0, 1), new Vec3i(0, 0, -1)};
   private static final Vec3i[] b = new Vec3i[]{new Vec3i(-1, 0, 0), new Vec3i(1, 0, 0), new Vec3i(0, 0, -1), new Vec3i(0, 0, 1), new Vec3i(0, -1, 0)};
   private static final Vec3i[] c = new Vec3i[]{
      new Vec3i(-1, 0, 0),
      new Vec3i(0, 0, -1),
      new Vec3i(0, 0, 1),
      new Vec3i(0, -1, 0),
      new Vec3i(2, 0, 0),
      new Vec3i(1, 0, -1),
      new Vec3i(1, 0, 1),
      new Vec3i(1, -1, 0)
   };
   private static final Vec3i[] d = new Vec3i[]{
      new Vec3i(0, 0, -1),
      new Vec3i(-1, 0, 0),
      new Vec3i(1, 0, 0),
      new Vec3i(0, -1, 0),
      new Vec3i(0, 0, 2),
      new Vec3i(-1, 0, 1),
      new Vec3i(1, 0, 1),
      new Vec3i(0, -1, 1)
   };
   private static final Vec3i[] e = new Vec3i[]{
      new Vec3i(-1, 0, 0),
      new Vec3i(0, 0, -1),
      new Vec3i(0, -1, 0),
      new Vec3i(2, 0, 0),
      new Vec3i(1, 0, -1),
      new Vec3i(1, -1, 0),
      new Vec3i(-1, 0, 1),
      new Vec3i(0, 0, 2),
      new Vec3i(0, -1, 1),
      new Vec3i(2, 0, 1),
      new Vec3i(1, 0, 2),
      new Vec3i(1, -1, 1)
   };

   public static boolean a_bool_Player(Player var0) {
      return a_HashSet_Player_bool_bool_bool(var0, true, true, false).stream().noneMatch(var0x -> mc.level.getBlockState(var0x).canBeReplaced());
   }

   public static List<BlockPos> a_List_Entity(Entity var0) {
      if (var0 != null && mc.level != null) {
         ArrayList var1 = new ArrayList();
         AABB var2 = var0.getBoundingBox();
         if (var0.isSwimming()
            || var0.isVisuallySwimming()
            || var0.isVisuallyCrawling()
            || var0 instanceof LivingEntity var3 && var3.isFallFlying()
            || var0.getPose() == Pose.SWIMMING
            || var0.getPose() == Pose.FALL_FLYING
            || var2.maxY - var2.minY <= 1.0) {
            float var34 = var0.getYRot();
            double var4 = Math.toRadians(var34);
            double var6 = -Math.sin(var4);
            double var8 = Math.cos(var4);
            double var10 = var0.getX();
            double var12 = var0.getY();
            double var14 = var0.getZ();

            for (double var16 = -0.9; var16 <= 0.9; var16 += 0.2) {
               for (double var18 = -0.3; var18 <= 0.3; var18 += 0.3) {
                  double var20 = var10 + var6 * var16 - var8 * var18;
                  double var22 = var14 + var8 * var16 + var6 * var18;
                  BlockPos var24 = new BlockPos((int)Math.floor(var20), (int)Math.floor(var12), (int)Math.floor(var22));
                  if (!var1.contains(var24)) {
                     var1.add(var24);
                  }
               }
            }

            int var41 = (int)Math.floor(var2.minX);
            int var25 = (int)Math.ceil(var2.maxX);
            int var42 = (int)Math.floor(var2.minY);
            int var26 = (int)Math.ceil(var2.maxY);
            int var43 = (int)Math.floor(var2.minZ);
            int var27 = (int)Math.ceil(var2.maxZ);

            for (int var44 = var41; var44 < var25; var44++) {
               for (int var28 = var42; var28 < var26; var28++) {
                  for (int var45 = var43; var45 < var27; var45++) {
                     BlockPos var29 = new BlockPos(var44, var28, var45);
                     if (!var1.contains(var29)) {
                        var1.add(var29);
                     }
                  }
               }
            }
         } else {
            int var35 = (int)Math.floor(var2.minX);
            int var36 = (int)Math.ceil(var2.maxX);
            int var30 = (int)Math.floor(var2.minY);
            int var37 = (int)Math.floor(var2.minZ);
            int var31 = (int)Math.ceil(var2.maxZ);
            int var38 = (int)Math.ceil(var2.maxY);

            for (int var32 = var35; var32 < var36; var32++) {
               for (int var39 = var30; var39 < var38; var39++) {
                  for (int var33 = var37; var33 < var31; var33++) {
                     BlockPos var40 = new BlockPos(var32, var39, var33);
                     if (!var1.contains(var40)) {
                        var1.add(var40);
                     }
                  }
               }
            }
         }

         if (var1.isEmpty()) {
            var1.add(PositionUtil.a_BlockPos_Entity(var0));
         }

         return var1;
      } else {
         return Collections.emptyList();
      }
   }

   public static BlockPos b(Player var0) {
      if (var0 != null && mc.level != null) {
         BlockPos var1 = PositionUtil.a_BlockPos_Entity(var0);
         if (mc.level.getBlockState(var1.below()).canBeReplaced()) {
            for (int var2 = 2; var2 <= 3; var2++) {
               BlockPos var3 = var1.below(var2);
               if (!mc.level.getBlockState(var3).canBeReplaced()) {
                  return var3.above();
               }
            }
         }

         return var1;
      } else {
         return BlockPos.ZERO;
      }
   }

   public static HashSet<BlockPos> a_HashSet_Player_bool_bool_bool(Player var0, boolean var1, boolean var2, boolean var3) {
      return a(var0, var1, var2, var2, var3);
   }

   public static HashSet<BlockPos> a(Player var0, boolean var1, boolean var2, boolean var3, boolean var4) {
      HashSet var5 = new HashSet();
      HashSet var6 = new HashSet();
      HitboxDesync var7 = Night.moduleManager.getModule(HitboxDesync.class);
      AABB var8 = var0.getBoundingBox();
      int var9 = (int)(var8.minY - Math.floor(var8.minY) > 0.8 ? Math.floor(var8.minY) + 1.0 : Math.floor(var8.minY));
      int var10 = (int)Math.floor(var8.minX);
      int var11 = (int)Math.floor(var8.maxX);
      int var12 = (int)Math.floor(var8.minZ);
      int var13 = (int)Math.floor(var8.maxZ);
      BlockPos var14 = new BlockPos((int)Math.floor(var0.getX()), var9, (int)Math.floor(var0.getZ()));
      if (mc.level != null && !var0.onGround() && mc.level.getBlockState(var14.below()).canBeReplaced()) {
         for (int var15 = 2; var15 <= 3; var15++) {
            BlockPos var16 = var14.below(var15);
            if (!mc.level.getBlockState(var16).canBeReplaced()) {
               var9 = var16.above().getY();
               break;
            }
         }
      }

      BlockPos var34 = PositionUtil.a_BlockPos_Entity(var0);
      var6.add(new BlockPos(var34.getX(), var9, var34.getZ()));

      for (int var35 = var10; var35 <= var11; var35++) {
         for (int var17 = var12; var17 <= var13; var17++) {
            if (var8.intersects(new AABB(var35, var9, var17, var35 + 1, var9 + 1, var17 + 1))) {
               var6.add(new BlockPos(var35, var9, var17));
            }
         }
      }

      if (var1 && mc.level != null) {
         for (BlockPos var18 : (Iterable<BlockPos>)(Object)Set.copyOf(var6)) {
            for (Direction var22 : Direction.values()) {
               if (!var22.getAxis().isVertical()) {
                  BlockPos var23 = var18.relative(var22);
                  if (mc.level.getBlockState(var23).canBeReplaced()) {
                     List var24 = BlockInteractionUtil.g(var23);
                     if (!var24.isEmpty()) {
                        for (Player var26 : (Iterable<Player>)(Object)var24) {
                           if (var26 != var0
                              && (var26 != mc.player || var0 != mc.player)
                              && (!var4 || var26 == var0)
                              && (var0 != mc.player || Night.friendManager.isFriend(var26.getName().getString()))
                              && !(Math.abs(var26.getY() - var0.getY()) > 1.0)) {
                              AABB var27 = var26.getBoundingBox();
                              int var28 = (int)Math.floor(var27.minX + 0.001);
                              int var29 = (int)Math.floor(var27.maxX - 0.001);
                              int var30 = (int)Math.floor(var27.minZ + 0.001);
                              int var31 = (int)Math.floor(var27.maxZ - 0.001);

                              for (int var32 = var28; var32 <= var29; var32++) {
                                 for (int var33 = var30; var33 <= var31; var33++) {
                                    var6.add(new BlockPos(var32, var9, var33));
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

      for (BlockPos var40 : (Iterable<BlockPos>)(Object)var6) {
         if (var2 && var3) {
            var5.add(var40.below());
         }

         for (Direction var48 : Direction.values()) {
            if (var48.getAxis().isHorizontal()) {
               BlockPos var49 = var40.relative(var48);
               if (!var6.contains(var49)) {
                  var5.add(var49);
                  if (var2 && mc.level != null && mc.level.getBlockState(var49).canBeReplaced()) {
                     var5.add(var49.below());
                  }
               }
            }
         }
      }

      int var38 = var9;
      (( java.util.Collection<BlockPos> )var5).removeIf(var2x -> var2x.getY() == var38 && var0.getBoundingBox().intersects(new AABB(var2x)));
      if (var0 == mc.player && var7.isEnabled() && var7.close.getValue()) {
         ArrayList var41 = new ArrayList();
         Vec3 var43 = Vec3.atCenterOf(mc.player.blockPosition());
         boolean var45 = var43.x - mc.player.getX() > 0.0;
         boolean var47 = var43.z - mc.player.getZ() > 0.0;
         if (var45 && var47) {
            var41.add(mc.player.blockPosition().offset(-1, 0, 0));
            var41.add(mc.player.blockPosition().offset(0, 0, -1));
         }

         if (!var45 && var47) {
            var41.add(mc.player.blockPosition().offset(1, 0, 0));
            var41.add(mc.player.blockPosition().offset(0, 0, -1));
         }

         if (var45 && !var47) {
            var41.add(mc.player.blockPosition().offset(-1, 0, 0));
            var41.add(mc.player.blockPosition().offset(0, 0, 1));
         }

         if (!var45 && !var47) {
            var41.add(mc.player.blockPosition().offset(1, 0, 0));
            var41.add(mc.player.blockPosition().offset(0, 0, 1));
         }

         var5.removeIf(var41::contains);
      }

      return var5;
   }

   public static List<BlockPos> a(Player var0, BlockPos var1, boolean var2, boolean var3, boolean var4, boolean var5, boolean var6, boolean var7) {
      return a(var0, var1, var2, var3, var4, var5, var6, false, var7);
   }

   public static List<BlockPos> a(Player var0, boolean var1, boolean var2, boolean var3, boolean var4, boolean var5, boolean var6, boolean var7) {
      return a(var0, null, var1, var2, var3, var4, var5, var6, var7);
   }

   public static List<BlockPos> a(Player var0, BlockPos var1, boolean var2, boolean var3, boolean var4, boolean var5, boolean var6, boolean var7, boolean var8) {
      LinkedHashSet var9 = new LinkedHashSet();
      HashSet var10;
      if (var1 != null && !var0.onGround()) {
         var10 = new HashSet();
         var10.add(var1);
      } else {
         var10 = c(var0);
      }

      for (BlockPos var12 : (Iterable<BlockPos>)(Object)var10) {
         Direction var13 = null;

         for (Direction var17 : Direction.values()) {
            if (var17.getAxis().isHorizontal() && !var10.contains(var12.relative(var17))) {
               if (var13 == null) {
                  var13 = var17;
               }

               if (BlockInteractionUtil.b_Direction_BlockPos_bool(var12.relative(var17).above(), var6) != null) {
                  var13 = var17;
                  break;
               }
            }
         }

         if (var13 == null) {
            var13 = Direction.EAST;
         }

         if (var4) {
            a_void_Set_HashSet_BlockPos_Vec3i(var9, var10, var12, new Vec3i(1, 2, 0));
            a_void_Set_HashSet_BlockPos_Vec3i(var9, var10, var12, new Vec3i(-1, 2, 0));
            a_void_Set_HashSet_BlockPos_Vec3i(var9, var10, var12, new Vec3i(0, 2, 1));
            a_void_Set_HashSet_BlockPos_Vec3i(var9, var10, var12, new Vec3i(0, 2, -1));
         }

         if (var5) {
            var9.add(var12.offset(0, 3, 0));
         }

         if (var7) {
            for (Direction var26 : Direction.values()) {
               if (var26.getAxis().isHorizontal() && !var10.contains(var12.relative(var26))) {
                  var9.add(var12.relative(var26));
               }
            }
         }

         if (var2) {
            BlockPos var21 = var12.offset(0, 2, 0);
            if (mc.level != null && !mc.level.getBlockState(var21).canBeReplaced()) {
               var9.add(var21);
            } else if (!var8 && BlockInteractionUtil.b_Direction_BlockPos_bool(var21, var6) == null) {
               var9.add(var12.relative(var13).above());
               var9.add(var12.relative(var13).above(2));
               var9.add(var21);
            } else {
               var9.add(var21);
            }
         } else {
            for (Direction var27 : Direction.values()) {
               if (var27.getAxis().isHorizontal() && !var10.contains(var12.relative(var27))) {
                  var9.add(var12.relative(var27).above());
               }
            }

            if (var3) {
               BlockPos var20 = var12.offset(0, 2, 0);
               if (!var8 && (mc.level == null || mc.level.getBlockState(var20).canBeReplaced())) {
                  var9.add(var12.relative(var13).above(2));
               }

               var9.add(var20);
            }
         }
      }

      return new ArrayList<>(var9);
   }

   private static void a_void_Set_HashSet_BlockPos_Vec3i(Set<BlockPos> var0, HashSet<BlockPos> var1, BlockPos var2, Vec3i var3) {
      if (var3.getX() != 0 || var3.getZ() != 0) {
         BlockPos var4 = var2.offset(var3.getX(), 0, var3.getZ());
         if (var1.contains(var4)) {
            return;
         }
      }

      var0.add(var2.offset(var3));
   }

   private static HashSet<BlockPos> c(Player var0) {
      HashSet var1 = new HashSet();
      BlockPos var2 = PositionUtil.a_BlockPos_Entity(var0);
      var1.add(var2);

      for (Direction var6 : Direction.values()) {
         if (!var6.getAxis().isVertical()) {
            AABB var7 = var0.getBoundingBox();
            BlockPos var8 = var2.relative(var6);
            if (var7.minX < var8.getX() + 1 && var7.maxX > var8.getX() && var7.minZ < var8.getZ() + 1 && var7.maxZ > var8.getZ()) {
               for (int var9 = (int)Math.floor(var7.minX); var9 < Math.ceil(var7.maxX); var9++) {
                  for (int var10 = (int)Math.floor(var7.minZ); var10 < Math.ceil(var7.maxZ); var10++) {
                     var1.add(new BlockPos(var9, var2.getY(), var10));
                  }
               }
            }
         }
      }

      return var1;
   }

   private static HoleUtil.HoleSafety a_HoleSafety_BlockPos_Vec3iArr(BlockPos var0, Vec3i[] var1) {
      boolean var2 = true;
      boolean var3 = true;

      for (Vec3i var6 : var1) {
         BlockPos var7 = var0.offset(var6);
         Block var8 = mc.level.getBlockState(var7).getBlock();
         boolean var9 = var8.equals(Blocks.BEDROCK);
         boolean var10 = var8.equals(Blocks.OBSIDIAN) || var8.equals(Blocks.RESPAWN_ANCHOR) || var8.equals(Blocks.ENDER_CHEST);
         if (!var9 && !var10) {
            var3 = false;
            if (!(var8 instanceof ShulkerBoxBlock) && mc.level.getBlockState(var7).getCollisionShape(mc.level, var7).isEmpty()) {
               return null;
            }
         } else if (!var9) {
            var2 = false;
         }
      }

      if (!var3) {
         return HoleUtil.HoleSafety.NONE;
      } else {
         return var2 ? HoleUtil.HoleSafety.SAFE : HoleUtil.HoleSafety.UNSAFE;
      }
   }

   public static HoleUtil.HoleInfo a_HoleInfo_BlockPos_double(BlockPos var0, double var1) {
      return a_HoleInfo_BlockPos_double_bool(var0, var1, true);
   }

   public static HoleUtil.HoleInfo a_HoleInfo_BlockPos_double_bool(BlockPos var0, double var1, boolean var3) {
      if (!mc.level.getBlockState(var0).getBlock().equals(Blocks.AIR)) {
         return null;
      }

      if (!mc.level.getBlockState(var0.above()).getBlock().equals(Blocks.AIR) && var3) {
         return null;
      }

      if (!mc.level.getBlockState(var0.above().above()).getBlock().equals(Blocks.AIR) && var3) {
         return null;
      }

      HoleUtil.HoleSafety var4 = a_HoleSafety_BlockPos_Vec3iArr(var0, b);
      return var4 == null
         ? null
         : new HoleUtil.HoleInfo(
            new AABB(var0.getX(), var0.getY(), var0.getZ(), var0.getX() + 1, var0.getY() + var1, var0.getZ() + 1), HoleUtil.HoleType.SINGLE, var4
         );
   }

   public static HoleUtil.HoleInfo b(BlockPos var0, double var1) {
      if (!mc.level.getBlockState(var0).getBlock().equals(Blocks.AIR)) {
         return null;
      }

      if (!mc.level.getBlockState(var0.above()).getBlock().equals(Blocks.AIR)) {
         return null;
      }

      if (!mc.level.getBlockState(var0.above().above()).getBlock().equals(Blocks.AIR)) {
         return null;
      }

      boolean var3 = mc.level.getBlockState(var0.offset(new Vec3i(1, 0, 0))).getBlock().equals(Blocks.AIR)
         && mc.level.getBlockState(var0.offset(new Vec3i(1, 0, 0)).above()).getBlock().equals(Blocks.AIR)
         && mc.level.getBlockState(var0.offset(new Vec3i(1, 0, 0)).above().above()).getBlock().equals(Blocks.AIR);
      boolean var4 = mc.level.getBlockState(var0.offset(new Vec3i(0, 0, 1))).getBlock().equals(Blocks.AIR)
         && mc.level.getBlockState(var0.offset(new Vec3i(0, 0, 1)).above()).getBlock().equals(Blocks.AIR)
         && mc.level.getBlockState(var0.offset(new Vec3i(0, 0, 1)).above().above()).getBlock().equals(Blocks.AIR);
      if (!var3 && !var4) {
         return null;
      }

      AABB var5 = null;
      HoleUtil.HoleSafety var6 = null;
      if (var3) {
         HoleUtil.HoleSafety var7 = a_HoleSafety_BlockPos_Vec3iArr(var0, c);
         if (var7 != null) {
            var5 = new AABB(var0.getX(), var0.getY(), var0.getZ(), var0.getX() + 2, var0.getY() + var1, var0.getZ() + 1);
            var6 = var7;
         }
      }

      if (var4 && var5 == null) {
         HoleUtil.HoleSafety var8 = a_HoleSafety_BlockPos_Vec3iArr(var0, d);
         if (var8 != null) {
            var5 = new AABB(var0.getX(), var0.getY(), var0.getZ(), var0.getX() + 1, var0.getY() + var1, var0.getZ() + 2);
            var6 = var8;
         }
      }

      return var5 == null ? null : new HoleUtil.HoleInfo(var5, HoleUtil.HoleType.DOUBLE, var6);
   }

   public static HoleUtil.HoleInfo c(BlockPos var0, double var1) {
      if (!mc.level.getBlockState(var0).getBlock().equals(Blocks.AIR)) {
         return null;
      }

      if (!mc.level.getBlockState(var0.above()).getBlock().equals(Blocks.AIR)) {
         return null;
      }

      if (!mc.level.getBlockState(var0.offset(new Vec3i(1, 0, 0))).getBlock().equals(Blocks.AIR)) {
         return null;
      }

      if (!mc.level.getBlockState(var0.offset(new Vec3i(0, 0, 1))).getBlock().equals(Blocks.AIR)) {
         return null;
      }

      if (!mc.level.getBlockState(var0.offset(new Vec3i(1, 0, 1))).getBlock().equals(Blocks.AIR)) {
         return null;
      }

      if (!mc.level.getBlockState(var0.above().above()).getBlock().equals(Blocks.AIR)) {
         return null;
      }

      if (!mc.level.getBlockState(var0.offset(new Vec3i(1, 0, 0)).above()).getBlock().equals(Blocks.AIR)) {
         return null;
      }

      if (!mc.level.getBlockState(var0.offset(new Vec3i(1, 0, 0)).above().above()).getBlock().equals(Blocks.AIR)) {
         return null;
      }

      if (!mc.level.getBlockState(var0.offset(new Vec3i(0, 0, 1)).above()).getBlock().equals(Blocks.AIR)) {
         return null;
      }

      if (!mc.level.getBlockState(var0.offset(new Vec3i(0, 0, 1)).above().above()).getBlock().equals(Blocks.AIR)) {
         return null;
      }

      if (!mc.level.getBlockState(var0.offset(new Vec3i(1, 0, 1)).above()).getBlock().equals(Blocks.AIR)) {
         return null;
      }

      if (!mc.level.getBlockState(var0.offset(new Vec3i(1, 0, 1)).above().above()).getBlock().equals(Blocks.AIR)) {
         return null;
      }

      HoleUtil.HoleSafety var3 = a_HoleSafety_BlockPos_Vec3iArr(var0, e);
      return var3 == null
         ? null
         : new HoleUtil.HoleInfo(
            new AABB(var0.getX(), var0.getY(), var0.getZ(), var0.getX() + 2, var0.getY() + var1, var0.getZ() + 2), HoleUtil.HoleType.QUAD, var3
         );
   }

   public static HoleUtil.HoleInfo d(BlockPos var0, double var1) {
      if (mc.level == null) {
         return null;
      }

      if (!mc.level.getBlockState(var0).getBlock().equals(Blocks.AIR)) {
         return null;
      }

      if (!mc.level.getBlockState(var0.above()).getBlock().equals(Blocks.AIR)) {
         return null;
      }

      if (!mc.level.getBlockState(var0.above().above()).getBlock().equals(Blocks.AIR)) {
         return null;
      }

      if (a_HoleInfo_BlockPos_double(var0, var1) != null) {
         return null;
      }

      if (b(var0, var1) != null) {
         return null;
      }

      if (c(var0, var1) != null) {
         return null;
      }

      if (mc.level.getBlockState(var0.below()).canBeReplaced()) {
         return null;
      }

      int var3 = 0;
      BlockPos var4 = null;

      for (Direction var8 : Direction.values()) {
         if (var8.getAxis().isHorizontal()) {
            BlockPos var9 = var0.relative(var8);
            if (!mc.level.getBlockState(var9).canBeReplaced() && !mc.level.getBlockState(var9).getCollisionShape(mc.level, var9).isEmpty()) {
               var3++;
            } else {
               var4 = var9;
            }
         }
      }

      if (var3 != 3) {
         return null;
      } else {
         return var4 != null && !mc.level.getBlockState(var4.below()).canBeReplaced()
            ? new HoleUtil.HoleInfo(
               new AABB(var0.getX(), var0.getY(), var0.getZ(), var0.getX() + 1, var0.getY() + var1, var0.getZ() + 1),
               HoleUtil.HoleType.PARTIAL,
               HoleUtil.HoleSafety.PARTIAL
            )
            : null;
      }
   }

   public record HoleInfo(AABB box, HoleUtil.HoleType holeType, HoleUtil.HoleSafety holeSafety) {
   }

   public enum HoleSafety {
      SAFE,
      UNSAFE,
      PARTIAL,
      NONE;
   }

   public enum HoleType {
      SINGLE,
      DOUBLE,
      QUAD,
      PARTIAL;
   }
}
