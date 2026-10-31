package night.security;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Base64;
import java.util.Base64.Decoder;

public final class EncryptedAssetLoader {
   private static final byte[] a;

   private EncryptedAssetLoader() {
   }

   public static byte[] a(String var0) throws IOException {
      ClassLoader var1 = EncryptedAssetLoader.class.getClassLoader();
      return a(var0, var1);
   }

   public static byte[] a(String var0, ClassLoader var1) throws IOException {
      byte[] var2 = a(var1);

      try (InputStream var3 = var1.getResourceAsStream(var0)) {
         if (var3 == null) {
            throw new IOException("missing encrypted asset: " + var0);
         }

         byte[] var4 = var3.readAllBytes();
         int var5 = var4.length;
         int var6 = a.length;
         if (var5 >= var6) {
            byte[] var7 = var4;
            int var8 = a.length;
            int var9 = a.length;
            if (Arrays.equals(var7, 0, var8, a, 0, var9)) {
               byte[] var10 = var4;
               int var11 = a.length;
               int var12 = var4.length;
               byte[] var13 = Arrays.copyOfRange(var10, var11, var12);
               byte[] var14 = var2;
               return AESGCMDecryptor.a(var13, var14);
            }
         }

         throw new IOException("bad asset header: " + var0);
      }
   }

   public static InputStream b(String var0) throws IOException {
      byte[] var1 = a(var0);
      return new ByteArrayInputStream(var1);
   }

   private static byte[] a(ClassLoader var0) throws IOException {
      try (InputStream var1 = var0.getResourceAsStream("night/payload.key")) {
         if (var1 == null) {
            throw new IOException("missing resource: night/payload.key");
         }

         Decoder var2 = Base64.getDecoder();
         byte[] var3 = var1.readAllBytes();
         byte[] var4 = var2.decode(var3);
         byte[] var5 = JarSignatureVerifier.a();
         byte[] var6 = new byte[var4.length];
         int var7 = 0;

         while (true) {
            int var8 = var7;
            int var9 = var4.length;
            if (var8 >= var9) {
               return var6;
            }

            byte[] var10 = var6;
            int var11 = var7;
            byte[] var12 = var4;
            int var13 = var7;
            byte var14 = var12[var13];
            byte[] var15 = var5;
            int var16 = var7;
            int var17 = var5.length;
            int var18 = var16 % var17;
            byte var19 = var15[var18];
            byte var20 = (byte)(var14 ^ var19);
            var10[var11] = var20;
            var7++;
         }
      }
   }

   static {
      byte[] var0 = new byte[]{-119, 80, 78, 71, 13, 10, 26, 10};
      a = var0;
   }
}
