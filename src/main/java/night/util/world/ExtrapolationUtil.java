package night.util.world;

import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class ExtrapolationUtil {
   public static double[] a(Player var0) {
      float var1 = (float)var0.getAttributeValue(Attributes.SCALE);
      double var2 = 1.62 * var1;
      double var4 = 1.27 * var1;
      double var6 = 0.4 * var1;
      Pose var8 = var0.getPose();

      return switch (var8) {
         case FALL_FLYING, SPIN_ATTACK, SWIMMING -> new double[]{var6, var2, var4};
         case CROUCHING -> new double[]{var4, var2, var6};
         default -> new double[]{var2, var4, var6};
      };
   }

   public static Vec3[] b(Player var0) {
      Vec3 var1 = var0.position();
      double[] var2 = a(var0);
      Vec3[] var3 = new Vec3[var2.length];

      for (int var4 = 0; var4 < var2.length; var4++) {
         var3[var4] = var1.add(0.0, var2[var4], 0.0);
      }

      return var3;
   }
}
