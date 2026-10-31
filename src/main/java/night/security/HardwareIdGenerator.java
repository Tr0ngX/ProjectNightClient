package night.security;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.concurrent.TimeUnit;

public final class HardwareIdGenerator {
   private static volatile String a;
   private static volatile String b;

   public static synchronized String a() {
      if (a != null) {
         return a;
      }

      a = a(e());
      return a;
   }

   public static synchronized String b() {
      if (b != null) {
         return b;
      }

      b = m();
      return b;
   }

   private static String e() {
      if (d()) {
         String var0 = f();
         if (var0 != null && !var0.isBlank()) {
            return var0;
         }
      }

      String var1 = System.getProperty("os.name", "").toLowerCase();
      if (var1.contains("win")) {
         String var2 = j();
         if (var2 != null && !var2.isBlank()) {
            return var2;
         }
      } else if (var1.contains("linux")) {
         String var3 = k();
         if (var3 != null && !var3.isBlank()) {
            return var3;
         }
      } else if (var1.contains("mac")) {
         String var4 = l();
         if (var4 != null && !var4.isBlank()) {
            return var4;
         }
      }

      String var5 = System.getProperty("os.name");
      String var6 = System.getProperty("user.name");
      return "fallback-" + var5 + "-" + var6;
   }

   public static boolean c() {
      if (System.getProperty("java.vendor", "").toLowerCase().contains("android")) {
         return true;
      }

      if (System.getenv("POJAV_RENDERER") != null || System.getenv("POJAV_GAME_DIR") != null || System.getenv("POJAV_CONTEXT") != null) {
         return true;
      }

      if (System.getenv("ANDROID_ROOT") == null && System.getenv("ANDROID_DATA") == null) {
         if (!new File("/system/build.prop").exists() && !new File("/system/bin/sh").exists()) {
            try {
               Class.forName("android.os.Build");
               return true;
            } catch (Throwable var1) {
               return false;
            }
         } else {
            return true;
         }
      } else {
         return true;
      }
   }

   public static boolean d() {
      return c();
   }

   private static String f() {
      StringBuilder var0;
      String var1;
      label79: {
         var0 = new StringBuilder("android-");
         var1 = g();
         if (var1 != null && !var1.isBlank()) {
            String var2 = var1;
            if (!"unknown".equalsIgnoreCase(var2)) {
               break label79;
            }
         }

         var1 = b("ro.serialno");
      }

      label73: {
         if (var1 != null && !var1.isBlank()) {
            String var3 = var1;
            if (!"unknown".equalsIgnoreCase(var3)) {
               break label73;
            }
         }

         var1 = b("ro.boot.serialno");
      }

      if (var1 != null && !var1.isBlank()) {
         String var4 = var1;
         if (!"unknown".equalsIgnoreCase(var4)) {
            StringBuilder var5 = var0.append("serial:");
            String var6 = var1;
            var5.append(var6).append(";");
         }
      }

      String var7 = h();
      if (var7 != null && !var7.isBlank()) {
         StringBuilder var8 = var0.append("aid:");
         String var9 = var7;
         var8.append(var9).append(";");
      }

      String var10 = b("ro.build.fingerprint");
      if (var10 != null && !var10.isBlank()) {
         StringBuilder var11 = var0.append("fp:");
         String var12 = var10;
         var11.append(var12).append(";");
      }

      String var13 = b("ro.product.model");
      if (var13 != null && !var13.isBlank()) {
         StringBuilder var14 = var0.append("model:");
         String var15 = var13;
         var14.append(var15).append(";");
      }

      String var16 = b("ro.product.board");
      if (var16 != null && !var16.isBlank()) {
         StringBuilder var17 = var0.append("board:");
         String var18 = var16;
         var17.append(var18).append(";");
      }

      String var19 = b("ro.hardware");
      if (var19 != null && !var19.isBlank()) {
         StringBuilder var20 = var0.append("hw:");
         String var21 = var19;
         var20.append(var21).append(";");
      }

      String var22 = i();
      if (var22 != null && !var22.isBlank()) {
         String var23 = var22;
         if (!"0000000000000000".equals(var23)) {
            StringBuilder var24 = var0.append("cpusn:");
            String var25 = var22;
            var24.append(var25).append(";");
         }
      }

      int var26 = var0.length();
      int var27 = "android-".length();
      if (var26 > var27) {
         return var0.toString();
      }

      String var28 = k();
      return var28 != null && !var28.isBlank() ? "android-" + var28 : null;
   }

   private static String g() {
      try {
         Class var0 = Class.forName("android.os.Build");

         try {
            if (var0.getField("SERIAL").get(null) instanceof String var2 && !var2.isBlank()) {
               String var3 = var2;
               if (!"unknown".equalsIgnoreCase(var3)) {
                  return var2.trim();
               }
            }
         } catch (Throwable var17) {
         }

         try {
            Class var6 = var0;
            Class[] var7 = new Class[0];
            Method var8 = var6.getMethod("getSerial", var7);
            Object[] var9 = new Object[0];
            if (var8.invoke(null, var9) instanceof String var11 && !var11.isBlank()) {
               String var12 = var11;
               if (!"unknown".equalsIgnoreCase(var12)) {
                  return var11.trim();
               }
            }
         } catch (Throwable var16) {
         }
      } catch (Throwable var18) {
      }

      return null;
   }

   private static String b(String var0) {
      try {
         Class var1 = Class.forName("android.os.SystemProperties");
         Class[] var2 = new Class[]{String.class};
         Method var3 = var1.getMethod("get", var2);
         Object[] var4 = new Object[]{var0};
         if (var3.invoke(null, var4) instanceof String var6 && !var6.isBlank()) {
            return var6.trim();
         }
      } catch (Throwable var24) {
      }

      try {
         String[] var9 = new String[]{"getprop", var0};
         Process var10 = new ProcessBuilder(var9).start();
         InputStream var11 = var10.getInputStream();

         try (BufferedReader var12 = new BufferedReader(new InputStreamReader(var11, StandardCharsets.UTF_8))) {
            String var13 = var12.readLine();
            if (var13 != null && !var13.isBlank()) {
               return var13.trim();
            }
         }

         Process var19 = var10;
         long var20 = 2L;
         var19.waitFor(var20, TimeUnit.SECONDS);
      } catch (Throwable var26) {
      }

      return null;
   }

   private static String h() {
      try {
         Class var0 = Class.forName("android.app.ActivityThread");
         Class[] var1 = new Class[0];
         Method var2 = var0.getMethod("currentApplication", var1);
         Object[] var3 = new Object[0];
         Object var4 = var2.invoke(null, var3);
         if (var4 != null) {
            Class var5 = var4.getClass();
            Class[] var6 = new Class[0];
            Method var7 = var5.getMethod("getContentResolver", var6);
            Object var8 = var4;
            Object[] var9 = new Object[0];
            Object var10 = var7.invoke(var8, var9);
            Class var11 = Class.forName("android.provider.Settings$Secure");
            Class[] var12 = new Class[2];
            Class var13 = Class.forName("android.content.ContentResolver");
            var12[0] = var13;
            var12[1] = String.class;
            Method var14 = var11.getMethod("getString", var12);
            Object[] var15 = new Object[2];
            Object var16 = var10;
            var15[0] = var16;
            var15[1] = "android_id";
            if (var14.invoke(null, var15) instanceof String var18 && !var18.isBlank()) {
               return var18.trim();
            }
         }
      } catch (Throwable var21) {
      }

      return null;
   }

   private static String i() {
      try {
         String[] var0 = new String[0];
         Path var1 = Path.of("/proc/cpuinfo", var0);
         LinkOption[] var2 = new LinkOption[0];
         if (Files.exists(var1, var2)) {
            for (String var4 : (Iterable<String>)(Object)Files.readAllLines(var1)) {
               if (var4.toLowerCase().startsWith("serial") && var4.contains(":")) {
                  String var5 = var4;
                  int var6 = var4.indexOf(58) + 1;
                  String var7 = var5.substring(var6).trim();
                  if (!var7.isBlank()) {
                     return var7;
                  }
               }
            }
         }
      } catch (Throwable var10) {
      }

      return null;
   }

   private static String j() {
      try {
         String[] var0 = new String[]{
            "powershell",
            "-NoProfile",
            "-NonInteractive",
            "-Command",
            "(Get-ItemProperty 'HKLM:\\SOFTWARE\\Microsoft\\Cryptography').MachineGuid; (Get-CimInstance Win32_ComputerSystemProduct | Select-Object -First 1).UUID; (Get-CimInstance Win32_DiskDrive | Select-Object -First 1).SerialNumber; (Get-CimInstance Win32_Processor | Select-Object -First 1).ProcessorId; (Get-CimInstance Win32_BaseBoard | Select-Object -First 1).SerialNumber"
         };
         ProcessBuilder var1 = new ProcessBuilder(var0);
         var1.redirectErrorStream(true);
         Process var2 = var1.start();
         StringBuilder var3 = new StringBuilder();
         InputStream var5 = var2.getInputStream();
         InputStreamReader var4 = new InputStreamReader(var5, StandardCharsets.UTF_8);
         BufferedReader var6 = new BufferedReader(var4);

         try {
            while (true) {
               String var7 = var6.readLine();
               if (var7 == null) {
                  break;
               }

               String var8 = var7.trim();
               if (!var8.isEmpty()) {
                  StringBuilder var9 = var3;
                  String var10 = var8;
                  var9.append(var10);
               }
            }
         } catch (Throwable var42) {
            try {
               var6.close();
            } catch (Throwable var39) {
               var42.addSuppressed(var39);
            }

            throw var42;
         }

         var6.close();
         Process var15 = var2;
         long var16 = 10L;
         if (!var15.waitFor(var16, TimeUnit.SECONDS)) {
            var2.destroyForcibly();
         }

         String var18 = var3.toString();
         if (!var18.isBlank()) {
            return var18;
         }
      } catch (Exception var43) {
      }

      try {
         String[] var21 = new String[]{"reg", "query", "HKLM\\SOFTWARE\\Microsoft\\Cryptography", "/v", "MachineGuid"};
         Process var22 = new ProcessBuilder(var21).start();
         InputStream var23 = var22.getInputStream();

         try (BufferedReader var24 = new BufferedReader(new InputStreamReader(var23, StandardCharsets.UTF_8))) {
            while (true) {
               String var25 = var24.readLine();
               if (var25 == null) {
                  break;
               }

               if (var25.contains("MachineGuid")) {
                  String[] var26 = var25.trim().split("\\s+");
                  if (var26.length >= 3) {
                     String[] var27 = var26;
                     int var28 = var26.length - 1;
                     return var27[var28];
                  }
               }
            }
         }

         Process var34 = var22;
         long var35 = 3L;
         var34.waitFor(var35, TimeUnit.SECONDS);
      } catch (Exception var41) {
      }

      return null;
   }

   private static String k() {
      String[] var0 = new String[]{"/etc/machine-id", "/var/lib/dbus/machine-id", "/sys/class/dmi/id/product_uuid"};
      String[] var1 = var0;
      int var2 = var1.length;
      int var3 = 0;

      while (true) {
         int var4 = var3;
         int var5 = var2;
         if (var4 >= var5) {
            return null;
         }

         String[] var6 = var1;
         int var7 = var3;
         String var8 = var6[var7];

         try {
            String[] var9 = new String[0];
            Path var10 = Path.of(var8, var9);
            LinkOption[] var11 = new LinkOption[0];
            if (Files.exists(var10, var11)) {
               String var12 = Files.readString(var10).trim();
               if (!var12.isEmpty()) {
                  return var12;
               }
            }
         } catch (Exception var15) {
         }

         var3++;
      }
   }

   private static String l() {
      try {
         String[] var0 = new String[]{"ioreg", "-rd1", "-c", "IOPlatformExpertDevice"};
         Process var1 = new ProcessBuilder(var0).start();
         InputStream var3 = var1.getInputStream();
         InputStreamReader var2 = new InputStreamReader(var3, StandardCharsets.UTF_8);

         try (BufferedReader var4 = new BufferedReader(var2)) {
            while (true) {
               String var5 = var4.readLine();
               if (var5 == null) {
                  break;
               }

               if (var5.contains("IOPlatformUUID")) {
                  int var6 = var5.indexOf(34);
                  int var7 = var5.lastIndexOf(34);
                  if (var6 != -1) {
                     int var8 = var7;
                     int var9 = var6;
                     if (var8 > var9) {
                        String var10 = var5;
                        int var11 = var6 + 1;
                        int var12 = var7;
                        return var10.substring(var11, var12);
                     }
                  }
               }
            }
         }

         Process var18 = var1;
         long var19 = 5L;
         var18.waitFor(var19, TimeUnit.SECONDS);
      } catch (Exception var24) {
      }

      return null;
   }

   public static String a(String var0) {
      try {
         MessageDigest var1 = MessageDigest.getInstance("SHA-256");
         byte[] var2 = var0.getBytes(StandardCharsets.UTF_8);
         byte[] var3 = var1.digest(var2);
         int var4 = var3.length * 2;
         StringBuilder var5 = new StringBuilder(var4);
         byte[] var6 = var3;
         int var7 = var6.length;
         int var8 = 0;

         while (true) {
            int var9 = var8;
            int var10 = var7;
            if (var9 >= var10) {
               return var5.toString();
            }

            byte[] var11 = var6;
            int var12 = var8;
            byte var13 = var11[var12];
            StringBuilder var14 = var5;
            Object[] var15 = new Object[1];
            Byte var16 = var13;
            var15[0] = var16;
            String var17 = String.format("%02x", var15);
            var14.append(var17);
            var8++;
         }
      } catch (Exception var20) {
         throw new IllegalStateException("SHA-256 not available", var20);
      }
   }

   private static String m() {
      if (c()) {
         String var0 = b("ro.product.model");
         String var1 = b("ro.product.brand");
         String var2 = b("ro.build.version.release");
         StringBuilder var3 = new StringBuilder("Android (Blocked");
         if (var2 != null && !var2.isBlank()) {
            StringBuilder var4 = var3.append(" ");
            String var5 = var2;
            var4.append(var5);
         }

         var3.append(")");
         if (var1 != null && !var1.isBlank()) {
            StringBuilder var6 = var3.append(" | ");
            String var7 = var1;
            var6.append(var7);
         }

         if (var0 != null && !var0.isBlank()) {
            StringBuilder var8 = var3.append(" ");
            String var9 = var0;
            var8.append(var9);
         }

         StringBuilder var10 = var3.append(" | Arch: ");
         String var11 = System.getProperty("os.arch", "arm64");
         var10.append(var11);
         return var3.toString();
      } else {
         String var12 = System.getenv("COMPUTERNAME");
         if (var12 == null || var12.isBlank()) {
            var12 = System.getenv("HOSTNAME");
         }

         if (var12 == null || var12.isBlank()) {
            try {
               var12 = InetAddress.getLocalHost().getHostName();
            } catch (Throwable var29) {
            }
         }

         if (var12 == null || var12.isBlank()) {
            var12 = "Unknown-PC";
         }

         String var14 = System.getProperty("user.name", "Unknown-User");
         String var15 = System.getProperty("os.name", "Unknown-OS");
         String var16 = n();
         String var17 = o();
         StringBuilder var18 = new StringBuilder();
         StringBuilder var19 = var18.append("PC: ");
         String var20 = var12;
         var19.append(var20);
         StringBuilder var21 = var18.append(" | User: ");
         String var22 = var14;
         var21.append(var22);
         if (var16 != null && !var16.isBlank()) {
            StringBuilder var23 = var18.append(" | CPU: ");
            String var24 = var16;
            var23.append(var24);
         }

         if (var17 != null && !var17.isBlank()) {
            StringBuilder var25 = var18.append(" | GPU: ");
            String var26 = var17;
            var25.append(var26);
         }

         StringBuilder var27 = var18.append(" | OS: ");
         String var28 = var15;
         var27.append(var28);
         return var18.toString();
      }
   }

   private static String n() {
      if (System.getProperty("os.name", "").toLowerCase().contains("win")) {
         try {
            String[] var0 = new String[]{"reg", "query", "HKLM\\HARDWARE\\DESCRIPTION\\System\\CentralProcessor\\0", "/v", "ProcessorNameString"};
            Process var1 = new ProcessBuilder(var0).start();
            InputStream var3 = var1.getInputStream();
            InputStreamReader var2 = new InputStreamReader(var3, StandardCharsets.UTF_8);

            try (BufferedReader var4 = new BufferedReader(var2)) {
               while (true) {
                  String var5 = var4.readLine();
                  if (var5 == null) {
                     break;
                  }

                  if (var5.contains("ProcessorNameString")) {
                     int var6 = var5.indexOf("REG_SZ");
                     if (var6 != -1) {
                        String var7 = var5;
                        int var8 = var6 + 6;
                        return var7.substring(var8).trim();
                     }
                  }
               }
            }

            Process var14 = var1;
            long var15 = 2L;
            var14.waitFor(var15, TimeUnit.SECONDS);
         } catch (Throwable var21) {
         }

         String var18 = System.getenv("PROCESSOR_IDENTIFIER");
         if (var18 != null && !var18.isBlank()) {
            return var18;
         }
      }

      return null;
   }

   private static String o() {
      if (System.getProperty("os.name", "").toLowerCase().contains("win")) {
         for (int var0 = 0; var0 <= 3; var0++) {
            Object[] var1 = new Object[1];
            Integer var2 = var0;
            var1[0] = var2;
            String var3 = String.format("%04d", var1);

            try {
               String[] var4 = new String[]{
                  "reg", "query", "HKLM\\SYSTEM\\CurrentControlSet\\Control\\Class\\{4d36e968-e325-11ce-bfc1-08002be10318}\\" + var3, "/v", "DriverDesc"
               };
               Process var5 = new ProcessBuilder(var4).start();
               InputStream var6 = var5.getInputStream();

               try (BufferedReader var7 = new BufferedReader(new InputStreamReader(var6, StandardCharsets.UTF_8))) {
                  while (true) {
                     String var8 = var7.readLine();
                     if (var8 == null) {
                        break;
                     }

                     if (var8.contains("DriverDesc")) {
                        int var9 = var8.indexOf("REG_SZ");
                        if (var9 != -1) {
                           String var10 = var8;
                           int var11 = var9 + 6;
                           String var12 = var10.substring(var11).trim();
                           if (!var12.isBlank()) {
                              return var12;
                           }
                        }
                     }
                  }
               }

               Process var18 = var5;
               long var19 = 2L;
               var18.waitFor(var19, TimeUnit.SECONDS);
            } catch (Throwable var24) {
            }
         }
      }

      return null;
   }
}
