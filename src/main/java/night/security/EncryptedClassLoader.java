package night.security;

import java.io.IOException;
import java.io.InputStream;

final class EncryptedClassLoader extends ClassLoader {
   private final ClassLoader a;
   private final byte[] b;

   EncryptedClassLoader(ClassLoader var1, ClassLoader var2, byte[] var3) {
      super(var1);
      this.a = var2;
      this.b = var3;
   }

   @Override
   protected Class<?> findClass(String var1) throws ClassNotFoundException {
      String var2 = var1.replace((char)46, (char)47) + ".enc";

      try {
         ClassLoader var3 = this.a;
         String var4 = var2;

         try (InputStream var5 = var3.getResourceAsStream(var4)) {
            if (var5 == null) {
               throw new ClassNotFoundException(var1);
            }

            byte[] var6 = var5.readAllBytes();
            byte[] var7 = this.b;
            byte[] var8 = AESGCMDecryptor.a(var6, var7);
            int var9 = var8.length;
            return this.defineClass(var1, var8, 0, var9);
         }
      } catch (IOException var18) {
         throw new ClassNotFoundException(var1, var18);
      }
   }
}
