package night.utils.minecraft;

public final class ImageShaderUtil {
   private static volatile boolean a = true;
   private static volatile boolean b = false;

   private ImageShaderUtil() {
   }

   public static synchronized void a() {
      a = true;
   }

   public static String b() {
      return "51b0963ce34c49a8c2f78e0ab4c5925d3c1ea4bbde2396575d16c4165a5acaba";
   }

   public static boolean c() {
      return true;
   }

   public static boolean d() {
      return false;
   }

   public static void a_void_byteArr_byteArr(byte[] var0, byte[] var1) {
   }

   public static double glGetShaderConstant(int var0) {
      return 1.0;
   }

   public static float[] glGetFrustumOffset(long var0, float var2, float var3) {
      return new float[]{0.0F, 0.0F, 0.0F};
   }
}
