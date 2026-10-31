package night.security;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.security.CodeSource;
import java.security.MessageDigest;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.Locale;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import night.utils.minecraft.ImageShaderUtil;

public final class JarSignatureVerifier {
   private static volatile byte[] a;

   private JarSignatureVerifier() {
   }

   public static byte[] a() {
      if (a != null) {
         return a;
      }

      byte[] var0 = b();
      if (var0 == null) {
         String var1 = ImageShaderUtil.b();
         if (var1 != null && var1.length() == 64) {
            var0 = new byte[32];

            for (int var2 = 0; var2 < 32; var2++) {
               byte[] var3 = var0;
               int var4 = var2;
               String var5 = var1;
               int var6 = var2 * 2;
               int var7 = var2 * 2 + 2;
               byte var8 = (byte)Integer.parseInt(var5.substring(var6, var7), 16);
               var3[var4] = var8;
            }
         }
      }

      if (var0 == null) {
         var0 = new byte[32];
      }

      a = var0;
      return a;
   }

   private static byte[] b() {
      try {
         CodeSource var0 = JarSignatureVerifier.class.getProtectionDomain().getCodeSource();
         if (var0 == null) {
            return null;
         }

         URL var1 = var0.getLocation();
         if (var1 == null) {
            return null;
         }

         File var3;
         try {
            URI var2 = var1.toURI();
            var3 = new File(var2);
         } catch (Exception var33) {
            String var5 = var1.getPath();
            var3 = new File(var5);
         }

         if (var3.isFile() && var3.getName().toLowerCase(Locale.ROOT).endsWith(".jar")) {
            File var6 = var3;

            try (JarFile var7 = new JarFile(var6, true)) {
               Enumeration var8 = var7.entries();
               byte[] var9 = new byte[8192];

               while (var8.hasMoreElements()) {
                  JarEntry var10 = (JarEntry)var8.nextElement();
                  String var11 = var10.getName().toUpperCase(Locale.ROOT);
                  if (!var10.isDirectory() && !var11.startsWith("META-INF/")) {
                     JarFile var12 = var7;
                     JarEntry var13 = var10;

                     try (InputStream var14 = var12.getInputStream(var13)) {
                        while (var14.read(var9) != -1) {
                        }
                     }

                     Certificate[] var20 = var10.getCertificates();
                     if (var20 != null && var20.length > 0 && var20[0] instanceof X509Certificate var22) {
                        MessageDigest var23 = MessageDigest.getInstance("SHA-256");
                        byte[] var24 = var22.getEncoded();
                        return var23.digest(var24);
                     }
                  }
               }
            }
         }
      } catch (Throwable var36) {
      }

      return null;
   }
}
