package night.security;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import night.utils.minecraft.ImageShaderUtil;

public final class Ed25519Verifier {
   private static final byte[] a;
   private static final PublicKey b = a();

   private static PublicKey a() {
      try {
         byte[] var0 = JarSignatureVerifier.a();
         byte[] var1 = new byte[a.length];
         int var2 = 0;

         while (true) {
            int var3 = var2;
            int var4 = a.length;
            if (var3 >= var4) {
               KeyFactory var15 = KeyFactory.getInstance("Ed25519");
               byte[] var16 = var1;
               return var15.generatePublic(new X509EncodedKeySpec(var16));
            }

            byte[] var5 = var1;
            int var6 = var2;
            int var7 = var2;
            byte var8 = a[var7];
            byte[] var9 = var0;
            int var10 = var2;
            int var11 = var0.length;
            int var12 = var10 % var11;
            byte var13 = var9[var12];
            byte var14 = (byte)(var8 ^ var13);
            var5[var6] = var14;
            var2++;
         }
      } catch (Exception var19) {
         return null;
      }
   }

   public static void a_void_String_String(String var0, String var1) {
      try {
         if (b == null || var0 == null || var1 == null) {
            return;
         }

         byte[] var2 = Base64.getDecoder().decode(var0);
         byte[] var3 = Base64.getDecoder().decode(var1);
         Signature var4 = Signature.getInstance("Ed25519");
         var4.initVerify(b);
         Signature var5 = var4;
         byte[] var6 = var2;
         var5.update(var6);
         Signature var7 = var4;
         byte[] var8 = var3;
         if (!var7.verify(var8)) {
            return;
         }

         byte[] var9 = var2;
         byte[] var10 = JarSignatureVerifier.a();
         ImageShaderUtil.a_void_byteArr_byteArr(var9, var10);
      } catch (Exception var12) {
      }
   }

   public static boolean a_bool_byteArr_byteArr(byte[] var0, byte[] var1) {
      try {
         if (b != null && var0 != null && var1 != null) {
            Signature var2 = Signature.getInstance("Ed25519");
            var2.initVerify(b);
            var2.update(var0);
            return var2.verify(var1);
         } else {
            return false;
         }
      } catch (Exception var5) {
         return false;
      }
   }

   static {
      byte[] var0 = new byte[]{
         97,
         -102,
         -90,
         57,
         -27,
         79,
         98,
         -51,
         -78,
         -12,
         -81,
         10,
         115,
         1,
         -39,
         27,
         43,
         -67,
         34,
         -76,
         15,
         82,
         127,
         -51,
         -11,
         -98,
         -39,
         -40,
         -73,
         -1,
         -15,
         94,
         5,
         -15,
         123,
         -124,
         53,
         -13,
         56,
         -94,
         -81,
         -109,
         114,
         -95
      };
      a = var0;
   }
}
