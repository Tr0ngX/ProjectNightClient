package night.security;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Base64.Decoder;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public final class EncryptedModuleBootstrap {
   private EncryptedModuleBootstrap() {
   }

   public static List<Class<?>> a(ClassLoader var0) {
      try {
         if (var0.getResource("night/payload.key") == null) {
            return Collections.emptyList();
         }

         byte[] var2 = b(var0);
         EncryptedClassLoader var3 = new EncryptedClassLoader(var0, var0, var2);
         ArrayList var4 = new ArrayList();

         for (String var6 : (Iterable<String>)(Object)a()) {
            ArrayList var7 = var4;
            String var8 = var6;
            EncryptedClassLoader var9 = var3;
            Class var10 = Class.forName(var8, true, var9);
            var7.add(var10);
         }

         return var4;
      } catch (Exception var13) {
         throw new EncryptedModuleBootstrap.PayloadLoadException("failed to load encrypted module payload", var13);
      }
   }

   private static byte[] b(ClassLoader var0) throws IOException {
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

   private static List<String> a() throws IOException {
      URL var0 = EncryptedModuleBootstrap.class.getProtectionDomain().getCodeSource().getLocation();
      ArrayList var1 = new ArrayList();
      if (var0 == null) {
         return var1;
      }

      try (
         InputStream var2 = var0.openStream();
         JarInputStream var3 = new JarInputStream(var2);
      ) {
         while (true) {
            JarEntry var4 = var3.getNextJarEntry();
            if (var4 == null) {
               return var1;
            }

            String var5 = var4.getName();
            if (var5.endsWith(".enc")) {
               ArrayList var6 = var1;
               String var7 = var5;
               int var8 = var5.length();
               int var9 = ".enc".length();
               int var10 = var8 - var9;
               String var11 = var7.substring(0, var10).replace((char)47, (char)46);
               var6.add(var11);
            }
         }
      }
   }

   public static final class PayloadLoadException extends RuntimeException {
      PayloadLoadException(String var1, Throwable var2) {
         super(var1, var2);
      }
   }
}
