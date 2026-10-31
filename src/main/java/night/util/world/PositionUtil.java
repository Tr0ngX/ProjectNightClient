package night.util.world;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import night.util.MinecraftInstance;
import night.utils.minecraft.ImageShaderUtil;

public class PositionUtil implements MinecraftInstance {
   public static BlockPos a_BlockPos_Entity(Entity var0) {
      return new BlockPos(
         var0.getBlockX(), Mth.floor(var0.getY() - Math.floor(var0.getY()) > 0.8 ? Math.floor(var0.getY()) + 1.0 : Math.floor(var0.getY())), var0.getBlockZ()
      );
   }

   public static AABB a(Player var0, int var1) {
      return a(var0, var1, true);
   }

   public static AABB a(Player var0, int var1, boolean var2) {
      if (var0 == null) {
         return null;
      }

      if (var1 > 0 && mc.level != null) {
         double var3 = var0.getX() - var0.xo;
         double var5 = var2 ? var0.getY() - var0.yo : 0.0;
         double var7 = var0.getZ() - var0.zo;
         double var9 = 0.0;
         double var11 = 0.0;
         double var13 = 0.0;

         for (double var15 = 0.5; var15 <= var1; var15 += 0.5) {
            Vec3 var17 = new Vec3(var3 * var15, var5 * var15, var7 * var15);
            if (mc.level.noCollision(var0, var0.getBoundingBox().move(var17))) {
               var9 = var3 * var15;
               var11 = var5 * var15;
               var13 = var7 * var15;
            } else {
               if (!var2) {
                  break;
               }

               Vec3 var18 = new Vec3(var3 * var15, var11, var7 * var15);
               if (!mc.level.noCollision(var0, var0.getBoundingBox().move(var18))) {
                  break;
               }

               var9 = var3 * var15;
               var13 = var7 * var15;
            }
         }

         return var0.getBoundingBox().move(new Vec3(var9, var11, var13));
      } else {
         return var0.getBoundingBox();
      }
   }

   public static double a_double_int(int var0) {
      ImageShaderUtil.a();
      return ImageShaderUtil.glGetShaderConstant(var0);
   }
}
