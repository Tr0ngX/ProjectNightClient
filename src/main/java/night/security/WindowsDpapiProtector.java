package night.security;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import java.util.concurrent.TimeUnit;

final class WindowsDpapiProtector {
   private static final boolean a = System.getProperty("os.name", "").toLowerCase().contains("win");

   static String a(String var0) {
      if (var0 == null || var0.isBlank()) {
         return "";
      }

      if (!a) {
         Encoder var1 = Base64.getEncoder();
         byte[] var2 = var0.getBytes(StandardCharsets.UTF_8);
         return var1.encodeToString(var2);
      }

      try {
         return a(
            "Add-Type -AssemblyName System.Security;$bytes=[Text.Encoding]::UTF8.GetBytes([Console]::In.ReadToEnd());$enc=[Security.Cryptography.ProtectedData]::Protect($bytes,$null,[Security.Cryptography.DataProtectionScope]::CurrentUser);[Convert]::ToBase64String($enc)",
            var0
         );
      } catch (Exception var7) {
         Encoder var5 = Base64.getEncoder();
         byte[] var6 = var0.getBytes(StandardCharsets.UTF_8);
         return var5.encodeToString(var6);
      }
   }

   static String b(String var0) {
      if (var0 != null && !var0.isBlank()) {
         String var1 = var0.trim();
         if (!var1.startsWith("NIGHT-") && !var1.startsWith("eyJ")) {
            if (a) {
               try {
                  String var2 = var1;
                  String var3 = a(
                     "Add-Type -AssemblyName System.Security;$enc=[Convert]::FromBase64String([Console]::In.ReadToEnd().Trim());$bytes=[Security.Cryptography.ProtectedData]::Unprotect($enc,$null,[Security.Cryptography.DataProtectionScope]::CurrentUser);[Text.Encoding]::UTF8.GetString($bytes)",
                     var2
                  );
                  if (var3 != null && !var3.isBlank()) {
                     return var3;
                  }
               } catch (Exception var12) {
               }
            }

            try {
               Decoder var6 = Base64.getDecoder();
               String var7 = var1;
               byte[] var8 = var6.decode(var7);
               String var9 = new String(var8, StandardCharsets.UTF_8).trim();
               if (var9.startsWith("NIGHT-") || var9.startsWith("eyJ") || !var9.isEmpty()) {
                  return var9;
               }
            } catch (Exception var13) {
            }

            return var1;
         } else {
            return var1;
         }
      } else {
         return null;
      }
   }

   private static String a(String var0, String var1) throws Exception {
      String[] var2 = new String[]{"powershell", "-NoProfile", "-NonInteractive", "-Command", var0};
      Process var3 = new ProcessBuilder(var2).start();
      OutputStream var4 = var3.getOutputStream();

      try (OutputStreamWriter var5 = new OutputStreamWriter(var4, StandardCharsets.UTF_8)) {
         var5.write(var1);
      }

      InputStream var6 = var3.getInputStream();
      BufferedReader var7 = new BufferedReader(new InputStreamReader(var6, StandardCharsets.UTF_8));

      String var8;
      try {
         var8 = var7.lines().reduce("", (var0x, var1x) -> var0x + var1x);
      } catch (Throwable var31) {
         try {
            var7.close();
         } catch (Throwable var28) {
            var31.addSuppressed(var28);
         }

         throw var31;
      }

      var7.close();
      Process var9 = var3;
      long var10 = 10L;
      if (!var9.waitFor(var10, TimeUnit.SECONDS)) {
         var3.destroyForcibly();
         throw new IOException("DPAPI PowerShell call timed out");
      }

      if (var3.exitValue() != 0) {
         InputStream var12 = var3.getErrorStream();

         String var14;
         try (BufferedReader var13 = new BufferedReader(new InputStreamReader(var12, StandardCharsets.UTF_8))) {
            var14 = var13.lines().reduce("", (var0x, var1x) -> var0x + var1x);
         }

         throw new IOException("DPAPI PowerShell call failed: " + var14);
      } else {
         return var8;
      }
   }
}
