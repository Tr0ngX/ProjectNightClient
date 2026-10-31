package night.util.animation;

import net.minecraft.util.Mth;

public class EasingUtil {
   public static float a(float var0, EasingUtil.Easing var1) {
      return switch (var1) {
         case EASE_IN_SINE -> b(var0);
         case EASE_OUT_SINE -> c(var0);
         case EASE_IN_CUBIC -> d(var0);
         case EASE_OUT_CUBIC -> e(var0);
         case EASE_IN_QUAD -> f(var0);
         case EASE_OUT_QUAD -> g(var0);
         case EASE_IN_QUART -> h(var0);
         case EASE_OUT_QUART -> i(var0);
         case EASE_IN_OUT_SINE -> j(var0);
         case EASE_IN_OUT_QUAD -> k(var0);
         case EASE_IN_OUT_CUBIC -> l(var0);
         case EASE_IN_OUT_QUART -> m(var0);
         case EASE_IN_ELASTIC -> n(var0);
         case EASE_OUT_ELASTIC -> o(var0);
         case EASE_IN_OUT_ELASTIC -> p(var0);
         case DECELERATE -> q(var0);
         default -> a_float_float(var0);
      };
   }

   public static float a(long var0, int var2) {
      return Mth.clamp((float)a_long_long(var0) / var2, 0.0F, 1.0F);
   }

   public static long a_long_long(long var0) {
      return System.currentTimeMillis() - var0;
   }

   private static float a_float_float(float var0) {
      return var0;
   }

   private static float b(float var0) {
      return (float)(1.0 - Math.cos(var0 * Math.PI / 2.0));
   }

   private static float c(float var0) {
      return (float)Math.sin(var0 * Math.PI / 2.0);
   }

   private static float d(float var0) {
      return var0 * var0 * var0;
   }

   private static float e(float var0) {
      return (float)(1.0 - Math.pow(1.0F - var0, 3.0));
   }

   private static float f(float var0) {
      return var0 * var0;
   }

   private static float g(float var0) {
      return 1.0F - (1.0F - var0) * (1.0F - var0);
   }

   private static float h(float var0) {
      return var0 * var0 * var0 * var0;
   }

   private static float i(float var0) {
      return (float)(1.0 - Math.pow(1.0F - var0, 4.0));
   }

   private static float j(float var0) {
      return (float)(-(Math.cos(Math.PI * var0) - 1.0) / 2.0);
   }

   private static float k(float var0) {
      return var0 < 0.5F ? 2.0F * var0 * var0 : (float)(1.0 - Math.pow(-2.0F * var0 + 2.0F, 2.0) / 2.0);
   }

   private static float l(float var0) {
      return var0 < 0.5F ? 4.0F * var0 * var0 * var0 : (float)(1.0 - Math.pow(-2.0F * var0 + 2.0F, 3.0) / 2.0);
   }

   private static float m(float var0) {
      return var0 < 0.5F ? 8.0F * var0 * var0 * var0 * var0 : (float)(1.0 - Math.pow(-2.0F * var0 + 2.0F, 4.0) / 2.0);
   }

   private static float n(float var0) {
      return var0 == 0.0F
         ? 0.0F
         : (float)(var0 == 1.0F ? 1.0 : -Math.pow(2.0, 10.0F * var0 - 10.0F) * Math.sin((var0 * 10.0F - 10.75) * (float) (Math.PI * 2.0 / 3.0)));
   }

   private static float o(float var0) {
      return var0 == 0.0F
         ? 0.0F
         : (float)(var0 == 1.0F ? 1.0 : Math.pow(2.0, -10.0F * var0) * Math.sin((var0 * 10.0F - 0.75) * (float) (Math.PI * 2.0 / 3.0)) + 1.0);
   }

   private static float p(float var0) {
      return var0 == 0.0F
         ? 0.0F
         : (float)(
            var0 == 1.0F
               ? 1.0
               : (
                  var0 < 0.5
                     ? -(Math.pow(2.0, 20.0F * var0 - 10.0F) * Math.sin((20.0F * var0 - 11.125) * ((float) Math.PI * 4.0F / 9.0F))) / 2.0
                     : Math.pow(2.0, -20.0F * var0 + 10.0F) * Math.sin((20.0F * var0 - 11.125) * ((float) Math.PI * 4.0F / 9.0F)) / 2.0 + 1.0
               )
         );
   }

   private static float q(float var0) {
      return (float)Math.sin(var0 * Math.PI / 2.0);
   }

   public enum Easing {
      LINEAR,
      EASE_IN_SINE,
      EASE_OUT_SINE,
      EASE_IN_CUBIC,
      EASE_OUT_CUBIC,
      EASE_IN_QUAD,
      EASE_OUT_QUAD,
      EASE_IN_QUART,
      EASE_OUT_QUART,
      EASE_IN_OUT_SINE,
      EASE_IN_OUT_QUAD,
      EASE_IN_OUT_CUBIC,
      EASE_IN_OUT_QUART,
      EASE_IN_ELASTIC,
      EASE_OUT_ELASTIC,
      EASE_IN_OUT_ELASTIC,
      DECELERATE;
   }
}
