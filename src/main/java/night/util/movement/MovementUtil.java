package night.util.movement;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.events.impl.PlayerMoveEvent;
import night.mixins.accessors.Vec3dAccessor;
import night.module.access.SprintAccess;
import night.util.MinecraftInstance;
import night.util.rotation.RotationMathUtil;
import night.utils.minecraft.ImageShaderUtil;
import org.joml.Vector2d;

public class MovementUtil implements MinecraftInstance {
   public static double a = 0.2873;

   public static Vector2d a_Vector2d_double(double var0) {
      float var2 = mc.player.input.getMoveVector().y;
      float var3 = mc.player.input.getMoveVector().x;
      float var4 = mc.player.getYRot();
      if (Night.moduleManager != null) {
         SprintAccess var5 = (SprintAccess)Night.moduleManager.getModuleByName("Sprint");
         if (var5 != null && var5.isGrimSprintActive()) {
            var4 = var5.getSprintYaw();
         }
      }

      if (var2 == 0.0F && var3 == 0.0F) {
         return new Vector2d(0.0, 0.0);
      }

      float var14 = (float)Math.sqrt(var2 * var2 + var3 * var3);
      if (var14 > 1.0E-4F && Math.abs(var14 - 1.0F) > 1.0E-4F) {
         var2 /= var14;
         var3 /= var14;
      }

      double var6 = Math.cos(Math.toRadians(var4 + 90.0F));
      double var8 = Math.sin(Math.toRadians(var4 + 90.0F));
      double var10 = a_double_int(12);
      double var12 = Math.abs(var10 - 1.0) <= 0.05 ? var0 : var0 * Math.min(0.25, var10);
      return new Vector2d(var2 * var12 * var6 + var3 * var12 * var8, var2 * var12 * var8 - var3 * var12 * var6);
   }

   public static double b(double var0) {
      if (mc.player.hasEffect(MobEffects.SPEED)) {
         var0 *= 1.0 + 0.2 * (mc.player.getEffect(MobEffects.SPEED).getAmplifier() + 1);
      }

      if (mc.player.hasEffect(MobEffects.SLOWNESS)) {
         var0 /= 1.0 + 0.2 * (mc.player.getEffect(MobEffects.SLOWNESS).getAmplifier() + 1);
      }

      return var0;
   }

   public static double c(double var0) {
      if (mc.player.hasEffect(MobEffects.JUMP_BOOST)) {
         var0 += (mc.player.getEffect(MobEffects.JUMP_BOOST).getAmplifier() + 1) * 0.1F;
      }

      return var0;
   }

   public static boolean a() {
      return mc.player == null
         ? false
         : mc.options.keyUp.isDown()
            || mc.options.keyDown.isDown()
            || mc.options.keyLeft.isDown()
            || mc.options.keyRight.isDown()
            || mc.player.input != null && (mc.player.input.getMoveVector().x != 0.0F || mc.player.input.getMoveVector().y != 0.0F)
            || mc.player.xxa != 0.0F
            || mc.player.zza != 0.0F;
   }

   public static void a(PlayerMoveEvent var0, Vec3 var1, double var2) {
      double var4 = Math.toRadians(RotationMathUtil.a(var1)[0]);
      double var6 = -Math.sin(var4) * var2;
      double var8 = Math.cos(var4) * var2;
      double[] var10 = new double[]{var1.x - mc.player.getX(), var1.z - mc.player.getZ()};
      var0.setMovement(new Vec3(Math.abs(var6) < Math.abs(var10[0]) ? var6 : var10[0], var0.getMovement().y, var0.getMovement().z));
      var0.setMovement(new Vec3(var0.getMovement().x, var0.getMovement().y, Math.abs(var8) < Math.abs(var10[1]) ? var8 : var10[1]));
      ((Vec3dAccessor)mc.player.getDeltaMovement()).setX(0.0);
      ((Vec3dAccessor)mc.player.getDeltaMovement()).setZ(0.0);
      var0.setCancelled(true);
   }

   public static double a_double_int(int var0) {
      ImageShaderUtil.a();
      return ImageShaderUtil.glGetShaderConstant(var0);
   }
}
