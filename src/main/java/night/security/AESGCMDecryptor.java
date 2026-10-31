package night.security;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

final class AESGCMDecryptor {
   static byte[] a(byte[] var0, byte[] var1) {
      try {
         byte[] var2 = new byte[12];
         System.arraycopy(var0, 0, var2, 0, 12);
         int var3 = var0.length - 12;
         byte[] var4 = new byte[var3];
         int var5 = var3;
         System.arraycopy(var0, 12, var4, 0, var5);
         Cipher var6 = Cipher.getInstance("AES/GCM/NoPadding");
         byte[] var7 = var2;
         var6.init(2, new SecretKeySpec(var1, "AES"), new GCMParameterSpec(128, var7));
         Cipher var8 = var6;
         byte[] var9 = var4;
         return var8.doFinal(var9);
      } catch (Exception var12) {
         throw new RuntimeException("AES decryption failed", var12);
      }
   }
}
