package night.util.world;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import night.Night;
import night.util.MinecraftInstance;
import night.util.network.PacketPredictionUtil;
import night.util.rotation.RotationMathUtil;

public class BlockPlaceUtil implements MinecraftInstance {
   public static final CopyOnWriteArrayList<BlockPos> a = new CopyOnWriteArrayList<>();

   public static boolean a_bool_BlockPos_Direction(BlockPos var0, Direction var1) {
      if (mc.player != null && mc.level != null) {
         AABB var2 = e(var0);
         AABB var3 = new AABB(mc.player.getX(), mc.player.getY() + 0.4, mc.player.getZ(), mc.player.getX(), mc.player.getY() + 1.62, mc.player.getZ())
            .inflate(2.0E-4);
         if (a_bool_AABB_AABB(var3, var2)) {
            return true;
         }

         return switch (var1) {
            case NORTH -> var3.minZ <= var2.minZ;
            case SOUTH -> var3.maxZ >= var2.maxZ;
            case EAST -> var3.maxX >= var2.maxX;
            case WEST -> var3.minX <= var2.minX;
            case UP -> var3.maxY >= var2.maxY;
            case DOWN -> var3.minY <= var2.minY;
            default -> throw new MatchException(null, null);
         };
      } else {
         return false;
      }
   }

   public static Direction a(BlockPos var0) {
      if (mc.player == null) {
         return null;
      }

      double var1 = Double.MAX_VALUE;
      Direction var3 = null;

      for (Direction var7 : Direction.values()) {
         if (b_bool_BlockPos_Direction(var0, var7)) {
            double var8 = mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(var0.relative(var7)));
            if (var8 < var1) {
               var1 = var8;
               var3 = var7;
            }
         }
      }

      if (var3 != null) {
         return var3;
      }

      var1 = Double.MAX_VALUE;
      var3 = Direction.UP;

      for (Direction var15 : Direction.values()) {
         if (a_bool_BlockPos_Direction(var0, var15)) {
            double var16 = mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(var0.relative(var15)));
            if (var16 < var1) {
               var1 = var16;
               var3 = var15;
            }
         }
      }

      return var3;
   }

   public static Direction a_Direction_BlockPos_Predicate(BlockPos var0, Predicate<Direction> var1) {
      return a_Direction_BlockPos_Predicate_bool(var0, var1, false);
   }

   public static Direction a_Direction_BlockPos_Predicate_bool(BlockPos var0, Predicate<Direction> var1, boolean var2) {
      if (var0 != null && mc.player != null && mc.level != null) {
         double var3 = Double.MAX_VALUE;
         Direction var5 = null;

         for (Direction var9 : Direction.values()) {
            if (var1 == null || var1.test(var9)) {
               BlockPos var10 = var0.relative(var9);
               BlockState var11 = mc.level.getBlockState(var10);
               if (a_bool_BlockPos_bool(var10, var2) && !var11.canBeReplaced() && a_bool_BlockPos_Direction(var10, var9.getOpposite())) {
                  Vec3 var12 = Vec3.atCenterOf(var0).add(var9.getStepX() * 0.5, var9.getStepY() * 0.5, var9.getStepZ() * 0.5);
                  double var13 = mc.player.getEyePosition().distanceToSqr(var12);
                  if (var13 < var3) {
                     var3 = var13;
                     var5 = var9;
                  }
               }
            }
         }

         if (var5 != null) {
            return var5;
         }

         for (Direction var18 : Direction.values()) {
            if (var1 == null || var1.test(var18)) {
               BlockPos var19 = var0.relative(var18);
               BlockState var20 = mc.level.getBlockState(var19);
               if (a_bool_BlockPos_bool(var19, var2) && !var20.canBeReplaced()) {
                  Vec3 var21 = Vec3.atCenterOf(var0).add(var18.getStepX() * 0.5, var18.getStepY() * 0.5, var18.getStepZ() * 0.5);
                  double var22 = mc.player.getEyePosition().distanceToSqr(var21);
                  if (var22 < var3) {
                     var3 = var22;
                     var5 = var18;
                  }
               }
            }
         }

         return var5;
      } else {
         return null;
      }
   }

   public static List<Direction> b_List_BlockPos_Predicate(BlockPos var0, Predicate<Direction> var1) {
      return b_List_BlockPos_Predicate_bool(var0, var1, false);
   }

   public static List<Direction> b_List_BlockPos_Predicate_bool(BlockPos var0, Predicate<Direction> var1, boolean var2) {
      ArrayList var3 = new ArrayList();
      if (var0 != null && mc.level != null) {
         for (Direction var7 : Direction.values()) {
            if (var1 == null || var1.test(var7)) {
               BlockPos var8 = var0.relative(var7);
               BlockState var9 = mc.level.getBlockState(var8);
               if (a_bool_BlockPos_bool(var8, var2) && !var9.canBeReplaced() && a_bool_BlockPos_Direction(var8, var7.getOpposite())) {
                  var3.add(var7);
               }
            }
         }

         return var3;
      } else {
         return var3;
      }
   }

   public static boolean a_bool_BlockPos_bool(BlockPos var0, boolean var1) {
      if (mc.level != null && mc.player != null) {
         BlockState var2 = mc.level.getBlockState(var0);
         boolean var3 = BlockInteractionUtil.a_bool_BlockState(var2);
         return var2.isSolid() && (!var3 || mc.player.isShiftKeyDown() || var1);
      } else {
         return false;
      }
   }

   public static boolean b(BlockPos var0) {
      return mc.level != null && var0 != null && var0.getY() < 320 && var0.getY() >= -64 ? mc.level.getBlockState(var0).canBeReplaced() : false;
   }

   public static boolean a(BlockPos var0, Predicate<Direction> var1, boolean var2, boolean var3) {
      if (!b(var0)) {
         return false;
      } else if (d(var0, var3)) {
         return false;
      } else {
         return var2 ? true : a_Direction_BlockPos_Predicate(var0, var1) != null;
      }
   }

   public static boolean c(BlockPos var0) {
      return b_bool_BlockPos_bool(var0, false);
   }

   public static boolean b_bool_BlockPos_bool(BlockPos var0, boolean var1) {
      if (mc.level == null) {
         return false;
      }

      if (!mc.level.getBlockState(var0).isAir()) {
         return false;
      }

      BlockPos var2 = var0.above();
      if (!mc.level.getBlockState(var2).isAir()) {
         return false;
      }

      if (!e(var0, false) && !e(var2, false)) {
         BlockPos var3 = var0.below();
         Block var4 = mc.level.getBlockState(var3).getBlock();
         if (var4 != Blocks.OBSIDIAN && var4 != Blocks.BEDROCK) {
            return false;
         } else {
            return var1 ? true : a(var3) != null;
         }
      } else {
         return false;
      }
   }

   public static boolean c(BlockPos var0, boolean var1) {
      if (mc.level == null) {
         return false;
      } else if (!mc.level.getBlockState(var0).isAir()) {
         return false;
      } else if (e(var0, false)) {
         return false;
      } else {
         BlockPos var2 = var0.below();
         Block var3 = mc.level.getBlockState(var2).getBlock();
         if (var3 != Blocks.OBSIDIAN && var3 != Blocks.BEDROCK) {
            return false;
         } else {
            return var1 ? true : a(var2) != null;
         }
      }
   }

   public static boolean d(BlockPos var0, boolean var1) {
      if (mc.level == null) {
         return false;
      }

      AABB var2 = new AABB(var0).deflate(0.01);

      for (Entity var4 : (Iterable<Entity>)(Object)mc.level.getEntitiesOfClass(Entity.class, var2)) {
         if (var4.isAlive()
            && !(var4 instanceof ItemEntity)
            && !(var4 instanceof ExperienceOrb)
            && !(var4 instanceof Projectile)
            && (!var1 || !(var4 instanceof EndCrystal))) {
            return true;
         }
      }

      return false;
   }

   public static boolean e(BlockPos var0, boolean var1) {
      if (mc.level == null) {
         return false;
      }

      AABB var2 = new AABB(var0);

      for (Entity var4 : (Iterable<Entity>)(Object)mc.level.getEntitiesOfClass(Entity.class, var2)) {
         if (var4.isAlive() && (!var1 || !(var4 instanceof EndCrystal))) {
            return true;
         }
      }

      return false;
   }

   public static boolean d(BlockPos var0) {
      if (mc.level == null) {
         return false;
      }

      AABB var1 = new AABB(var0);
      Iterator var2 = mc.level.getEntitiesOfClass(EndCrystal.class, var1).iterator();

      while (var2.hasNext()) {
         if (((EndCrystal)var2.next()).isAlive()) {
            return true;
         }
      }

      return false;
   }

   public static void a_void_BlockPos_Direction_bool(BlockPos var0, Direction var1, boolean var2) {
      if (mc.player != null && mc.getConnection() != null) {
         Vec3 var3 = new Vec3(var0.getX() + 0.5 + var1.getStepX() * 0.5, var0.getY() + 0.5 + var1.getStepY() * 0.5, var0.getZ() + 0.5 + var1.getStepZ() * 0.5);
         if (var2) {
            float[] var4 = RotationMathUtil.a(var3);
            Night.rotationManager.sendOrBatchPosRot(var4[0], var4[1]);
         }

         BlockHitResult var5 = new BlockHitResult(var3, var1, var0, false);
         PacketPredictionUtil.a_void_SequencedPacketCreator(var1x -> new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, var5, var1x));
         mc.getConnection().send(PacketAndRenderUtil.a_Packet_InteractionHand(InteractionHand.MAIN_HAND));
         a.add(var0);
      }
   }

   public static void b_void_BlockPos_Direction_bool(BlockPos var0, Direction var1, boolean var2) {
      a_void_BlockPos_Direction_bool(var0.relative(var1), var1.getOpposite(), var2);
   }

   public static boolean b_bool_BlockPos_Direction(BlockPos var0, Direction var1) {
      if (mc.level != null && mc.player != null) {
         Vec3 var2 = Vec3.atCenterOf(var0).add(var1.getStepX() * 0.5, var1.getStepY() * 0.5, var1.getStepZ() * 0.5);
         BlockHitResult var3 = mc.level
            .clip(new ClipContext(mc.player.getEyePosition(), var2, net.minecraft.world.level.ClipContext.Block.COLLIDER, Fluid.NONE, mc.player));
         return var3 == null || var3.getType() == Type.MISS;
      } else {
         return false;
      }
   }

   private static AABB e(BlockPos var0) {
      if (mc.level == null) {
         return new AABB(var0);
      }

      AABB var1 = new AABB(var0);
      Iterator var2 = mc.level.getBlockState(var0).getCollisionShape(mc.level, var0).toAabbs().iterator();

      while (var2.hasNext()) {
         AABB var3 = ((AABB)var2.next()).move(var0.getX(), var0.getY(), var0.getZ());
         double var4 = Math.max(var3.minX, var1.minX);
         double var6 = Math.max(var3.minY, var1.minY);
         double var8 = Math.max(var3.minZ, var1.minZ);
         double var10 = Math.min(var3.maxX, var1.maxX);
         double var12 = Math.min(var3.maxY, var1.maxY);
         double var14 = Math.min(var3.maxZ, var1.maxZ);
         var1 = new AABB(var4, var6, var8, var10, var12, var14);
      }

      return var1;
   }

   private static boolean a_bool_AABB_AABB(AABB var0, AABB var1) {
      return var1.maxX - 1.0E-7 > var0.minX
         && var1.minX + 1.0E-7 < var0.maxX
         && var1.maxY - 1.0E-7 > var0.minY
         && var1.minY + 1.0E-7 < var0.maxY
         && var1.maxZ - 1.0E-7 > var0.minZ
         && var1.minZ + 1.0E-7 < var0.maxZ;
   }
}
