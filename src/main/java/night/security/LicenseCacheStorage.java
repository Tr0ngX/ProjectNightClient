package night.security;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpClient.Builder;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import java.util.concurrent.TimeUnit;
import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import net.fabricmc.loader.api.FabricLoader;
import night.Night;

public final class LicenseCacheStorage {
   private static final byte[] a;
   private static final String b = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) NightLauncher/1.0";
   private static volatile String c;

   public static synchronized String a() {
      return b();
   }

   public static synchronized String b() {
      if (c != null) {
         return c;
      }

      String var0 = System.getProperty("night.license");
      if (var0 == null || var0.isBlank()) {
         var0 = System.getProperty("night.licenseKey");
      }

      if (var0 != null && !var0.isBlank()) {
         c = var0.trim();
         return c;
      }

      String var1 = System.getenv("NIGHT_LICENSE_KEY");
      if (var1 == null || var1.isBlank()) {
         var1 = System.getenv("NIGHT_LICENSE");
      }

      if (var1 != null && !var1.isBlank()) {
         c = var1.trim();
         return c;
      } else {
         String var2 = f();
         if (var2 != null && !var2.isBlank()) {
            c = var2;
            return c;
         } else {
            return null;
         }
      }
   }

   private static List<Path> d() {
      LinkedHashSet var0 = new LinkedHashSet();

      try {
         Path var1 = FabricLoader.getInstance().getGameDir();
         if (var1 != null) {
            LinkedHashSet var2 = var0;
            Path var3 = var1.resolve("Night").resolve("cache.dat");
            var2.add(var3);
            LinkedHashSet var4 = var0;
            Path var5 = var1;
            String var6 = "Night".toLowerCase();
            Path var7 = var5.resolve(var6).resolve("cache.dat");
            var4.add(var7);
            LinkedHashSet var8 = var0;
            Path var9 = var1.resolve("Night").resolve("license.key");
            var8.add(var9);
            LinkedHashSet var10 = var0;
            Path var11 = var1.resolve("Night").resolve("license.txt");
            var10.add(var11);
            LinkedHashSet var12 = var0;
            Path var13 = var1.resolve("cache.dat");
            var12.add(var13);
            LinkedHashSet var14 = var0;
            Path var15 = var1.resolve("license.key");
            var14.add(var15);
            LinkedHashSet var16 = var0;
            Path var17 = var1.resolve("license.txt");
            var16.add(var17);
            LinkedHashSet var18 = var0;
            Path var19 = var1.resolve("config").resolve("night").resolve("cache.dat");
            var18.add(var19);
            LinkedHashSet var20 = var0;
            Path var21 = var1.resolve("config").resolve("Night").resolve("cache.dat");
            var20.add(var21);
            Path var22 = var1.getParent();
            if (var22 != null) {
               LinkedHashSet var23 = var0;
               Path var24 = var22.resolve("Night").resolve("cache.dat");
               var23.add(var24);
               LinkedHashSet var25 = var0;
               Path var26 = var22.resolve("Night").resolve("cache.dat");
               var25.add(var26);
               LinkedHashSet var27 = var0;
               Path var28 = var22.resolve("night").resolve("cache.dat");
               var27.add(var28);
               LinkedHashSet var29 = var0;
               Path var30 = var22.resolve("license.key");
               var29.add(var30);
               LinkedHashSet var31 = var0;
               Path var32 = var22.resolve("license.txt");
               var31.add(var32);
               LinkedHashSet var33 = var0;
               Path var34 = var22.resolve("cache.dat");
               var33.add(var34);
            }
         }
      } catch (Throwable var150) {
      }

      LinkedHashSet var36 = var0;
      String[] var37 = new String[]{"cache.dat"};
      Path var38 = Paths.get("Night", var37);
      var36.add(var38);
      LinkedHashSet var39 = var0;
      String var40 = "Night".toLowerCase();
      String[] var41 = new String[]{"cache.dat"};
      Path var42 = Paths.get(var40, var41);
      var39.add(var42);
      LinkedHashSet var43 = var0;
      String[] var44 = new String[]{"license.key"};
      Path var45 = Paths.get("Night", var44);
      var43.add(var45);
      LinkedHashSet var46 = var0;
      String[] var47 = new String[]{"license.txt"};
      Path var48 = Paths.get("Night", var47);
      var46.add(var48);
      LinkedHashSet var49 = var0;
      String[] var50 = new String[0];
      Path var51 = Paths.get("cache.dat", var50);
      var49.add(var51);
      LinkedHashSet var52 = var0;
      String[] var53 = new String[0];
      Path var54 = Paths.get("license.key", var53);
      var52.add(var54);
      LinkedHashSet var55 = var0;
      String[] var56 = new String[0];
      Path var57 = Paths.get("license.txt", var56);
      var55.add(var57);
      String var58 = System.getProperty("user.home", ".");

      try {
         LinkedHashSet var59 = var0;
         String var60 = var58;
         String[] var61 = new String[]{".minecraft", "Night", "cache.dat"};
         Path var62 = Paths.get(var60, var61);
         var59.add(var62);
         LinkedHashSet var63 = var0;
         String var64 = var58;
         String[] var65 = new String[]{".minecraft", null, null};
         String var66 = "Night".toLowerCase();
         var65[1] = var66;
         var65[2] = "cache.dat";
         Path var67 = Paths.get(var64, var65);
         var63.add(var67);
         LinkedHashSet var68 = var0;
         String var69 = var58;
         String[] var70 = new String[]{".minecraft", "Night", "license.key"};
         Path var71 = Paths.get(var69, var70);
         var68.add(var71);
         LinkedHashSet var72 = var0;
         String var73 = var58;
         String[] var74 = new String[]{".minecraft", "Night", "license.txt"};
         Path var75 = Paths.get(var73, var74);
         var72.add(var75);
         LinkedHashSet var76 = var0;
         String var77 = var58;
         String[] var78 = new String[]{".minecraft", "cache.dat"};
         Path var79 = Paths.get(var77, var78);
         var76.add(var79);
      } catch (Throwable var149) {
      }

      try {
         String var81 = System.getenv("APPDATA");
         if (var81 != null) {
            LinkedHashSet var82 = var0;
            String var83 = var81;
            String[] var84 = new String[]{".minecraft", "Night", "cache.dat"};
            Path var85 = Paths.get(var83, var84);
            var82.add(var85);
            LinkedHashSet var86 = var0;
            String var87 = var81;
            String[] var88 = new String[]{".minecraft", null, null};
            String var89 = "Night".toLowerCase();
            var88[1] = var89;
            var88[2] = "cache.dat";
            Path var90 = Paths.get(var87, var88);
            var86.add(var90);
         }

         String var91 = System.getenv("LOCALAPPDATA");
         if (var91 != null) {
            LinkedHashSet var92 = var0;
            String var93 = var91;
            String[] var94 = new String[]{"NightLauncher", "license.dat"};
            Path var95 = Paths.get(var93, var94);
            var92.add(var95);
            LinkedHashSet var96 = var0;
            String var97 = var91;
            String[] var98 = new String[]{"NightLauncher", "cache.dat"};
            Path var99 = Paths.get(var97, var98);
            var96.add(var99);
         }
      } catch (Throwable var148) {
      }

      try {
         LinkedHashSet var101 = var0;
         String var102 = var58;
         String[] var103 = new String[]{".config", "night", "cache.dat"};
         Path var104 = Paths.get(var102, var103);
         var101.add(var104);
         LinkedHashSet var105 = var0;
         String var106 = var58;
         String[] var107 = new String[]{".config", "Night", "cache.dat"};
         Path var108 = Paths.get(var106, var107);
         var105.add(var108);
         LinkedHashSet var109 = var0;
         String var110 = var58;
         String[] var111 = new String[]{".config", "night", "license.key"};
         Path var112 = Paths.get(var110, var111);
         var109.add(var112);
         LinkedHashSet var113 = var0;
         String var114 = var58;
         String[] var115 = new String[]{".config", "night", "license.txt"};
         Path var116 = Paths.get(var114, var115);
         var113.add(var116);
         LinkedHashSet var117 = var0;
         String var118 = var58;
         String[] var119 = new String[]{".config", "NightLauncher", "license.dat"};
         Path var120 = Paths.get(var118, var119);
         var117.add(var120);
         LinkedHashSet var121 = var0;
         String var122 = var58;
         String[] var123 = new String[]{".config", "NightLauncher", "cache.dat"};
         Path var124 = Paths.get(var122, var123);
         var121.add(var124);
         LinkedHashSet var125 = var0;
         String var126 = var58;
         String[] var127 = new String[]{".local", "share", "NightLauncher", "license.dat"};
         Path var128 = Paths.get(var126, var127);
         var125.add(var128);
         LinkedHashSet var129 = var0;
         String var130 = var58;
         String[] var131 = new String[]{".local", "share", "NightLauncher", "cache.dat"};
         Path var132 = Paths.get(var130, var131);
         var129.add(var132);
         LinkedHashSet var133 = var0;
         String var134 = var58;
         String[] var135 = new String[]{".night", "cache.dat"};
         Path var136 = Paths.get(var134, var135);
         var133.add(var136);
         LinkedHashSet var137 = var0;
         String var138 = var58;
         String[] var139 = new String[]{".night", "license.key"};
         Path var140 = Paths.get(var138, var139);
         var137.add(var140);
         LinkedHashSet var141 = var0;
         String var142 = var58;
         String[] var143 = new String[]{".night", "license.txt"};
         Path var144 = Paths.get(var142, var143);
         var141.add(var144);
      } catch (Throwable var147) {
      }

      LinkedHashSet var146 = var0;
      return new ArrayList<>(var146);
   }

   private static Path e() {
      try {
         return FabricLoader.getInstance().getGameDir().resolve("Night").resolve("cache.dat");
      } catch (Throwable var3) {
         String[] var2 = new String[]{"cache.dat"};
         return Paths.get("Night", var2);
      }
   }

   private static String f() {
      for (Path var1 : (Iterable<Path>)(Object)d()) {
         try {
            LinkOption[] var2 = new LinkOption[0];
            if (Files.exists(var1, var2)) {
               String var3 = Files.readString(var1, StandardCharsets.UTF_8).trim();
               if (!var3.isEmpty()) {
                  if (var3.startsWith("NIGHT-")) {
                     return var3;
                  }

                  String var5 = b(var3);
                  if (var5 != null && !var5.isBlank()) {
                     String var6 = var5.trim();
                     if (var6.startsWith("NIGHT-")) {
                        return var6;
                     }

                     if (var6.length() > 20) {
                        String var8 = d(var6);
                        if (var8 != null) {
                           return var8;
                        }
                     }
                  }

                  try {
                     Decoder var10 = Base64.getDecoder();
                     String var11 = var3;
                     byte[] var12 = var10.decode(var11);
                     String var13 = new String(var12, StandardCharsets.UTF_8).trim();
                     if (var13.startsWith("NIGHT-")) {
                        return var13;
                     }
                  } catch (Exception var22) {
                  }

                  String var16 = WindowsDpapiProtector.b(var3);
                  if (var16 != null && !var16.isBlank()) {
                     String var17 = var16.trim();
                     if (var17.startsWith("NIGHT-")) {
                        return var17;
                     }

                     if (var17.length() > 20) {
                        String var19 = d(var17);
                        if (var19 != null) {
                           return var19;
                        }
                     }
                  }
               }
            }
         } catch (Exception var23) {
         }
      }

      return null;
   }

   private static String d(String var0) {
      if (var0 != null && !var0.isBlank()) {
         String[] var1 = new String[]{"https://nightclient.info.vn", "http://nightclient.info.vn", "http://103.78.2.201"};
         String[] var2 = var1;
         int var3 = var2.length;
         int var4 = 0;

         while (true) {
            int var5 = var4;
            int var6 = var3;
            if (var5 >= var6) {
               return null;
            }

            String[] var7 = var2;
            int var8 = var4;
            String var9 = var7[var8];

            try {
               Builder var10 = HttpClient.newBuilder();
               Duration var11 = Duration.ofSeconds(6L);
               HttpClient var12 = var10.connectTimeout(var11).build();
               java.net.http.HttpRequest.Builder var13 = HttpRequest.newBuilder();
               URI var14 = URI.create(var9 + "/me");
               java.net.http.HttpRequest.Builder var15 = var13.uri(var14);
               Duration var16 = Duration.ofSeconds(6L);
               HttpRequest var17 = var15.timeout(var16).header("User-Agent", b).header("Authorization", "Bearer " + var0).GET().build();
               HttpClient var18 = var12;
               HttpRequest var19 = var17;
               BodyHandler var20 = BodyHandlers.ofString();
               HttpResponse var21 = var18.send(var19, var20);
               if (var21.statusCode() == 200) {
                  Gson var22 = new Gson();
                  String var23 = (String)var21.body();
                  JsonObject var24 = (JsonObject)var22.fromJson(var23, JsonObject.class);
                  if (var24 != null && var24.has("licenseKey") && !var24.get("licenseKey").isJsonNull()) {
                     String var25 = var24.get("licenseKey").getAsString();
                     if (var25 != null && !var25.isBlank()) {
                        a_void_String(var25);
                        return var25;
                     }
                  }
               }
            } catch (Exception var28) {
            }

            var4++;
         }
      } else {
         return null;
      }
   }

   public static synchronized void c() {
      c = null;

      for (Path var1 : (Iterable<Path>)(Object)d()) {
         try {
            Files.deleteIfExists(var1);
         } catch (Exception var3) {
         }
      }
   }

   public static synchronized void a_void_String(String var0) {
      try {
         String var1 = c(var0);
         if (var1 == null || var1.isBlank()) {
            var1 = WindowsDpapiProtector.a(var0);
         }

         Path var2 = e();
         if (var2.getParent() != null) {
            Path var3 = var2.getParent();
            LinkOption[] var4 = new LinkOption[0];
            if (!Files.exists(var3, var4)) {
               Path var5 = var2.getParent();
               FileAttribute[] var6 = new FileAttribute[0];
               Files.createDirectories(var5, var6);
            }
         }

         Path var7 = var2;
         String var8 = var1;
         OpenOption[] var9 = new OpenOption[0];
         Files.writeString(var7, var8, StandardCharsets.UTF_8, var9);

         try {
            Path var10 = FabricLoader.getInstance().getGameDir();
            if (var10 != null) {
               Path var11 = var10.resolve("cache.dat");
               String var12 = var1;
               OpenOption[] var13 = new OpenOption[0];
               Files.writeString(var11, var12, StandardCharsets.UTF_8, var13);
            }
         } catch (Exception var51) {
         }

         try {
            String var15 = System.getenv("LOCALAPPDATA");
            if (var15 != null) {
               String var16 = var15;
               String[] var17 = new String[]{"NightLauncher", "license.dat"};
               Path var18 = Paths.get(var16, var17);
               Path var19 = var18.getParent();
               LinkOption[] var20 = new LinkOption[0];
               if (!Files.exists(var19, var20)) {
                  Path var21 = var18.getParent();
                  FileAttribute[] var22 = new FileAttribute[0];
                  Files.createDirectories(var21, var22);
               }

               Path var23 = var18;
               String var24 = var1;
               OpenOption[] var25 = new OpenOption[0];
               Files.writeString(var23, var24, StandardCharsets.UTF_8, var25);
            }
         } catch (Exception var50) {
         }

         try {
            String var27 = System.getProperty("user.home", ".");
            String[] var28 = new String[]{".config", "NightLauncher", "license.dat"};
            Path var29 = Paths.get(var27, var28);
            Path var30 = var29.getParent();
            LinkOption[] var31 = new LinkOption[0];
            if (!Files.exists(var30, var31)) {
               Path var32 = var29.getParent();
               FileAttribute[] var33 = new FileAttribute[0];
               Files.createDirectories(var32, var33);
            }

            Path var34 = var29;
            String var35 = var1;
            OpenOption[] var36 = new OpenOption[0];
            Files.writeString(var34, var35, StandardCharsets.UTF_8, var36);
            String var37 = var27;
            String[] var38 = new String[]{".config", "night", "cache.dat"};
            Path var39 = Paths.get(var37, var38);
            Path var40 = var39.getParent();
            LinkOption[] var41 = new LinkOption[0];
            if (!Files.exists(var40, var41)) {
               Path var42 = var39.getParent();
               FileAttribute[] var43 = new FileAttribute[0];
               Files.createDirectories(var42, var43);
            }

            Path var44 = var39;
            String var45 = var1;
            OpenOption[] var46 = new OpenOption[0];
            Files.writeString(var44, var45, StandardCharsets.UTF_8, var46);
         } catch (Exception var49) {
         }

         c = var0;
      } catch (Exception var52) {
         String var48 = var52.toString();
         Night.LOGGER.debug(var48);
      }
   }

   private static SecretKey g() {
      return a_SecretKey_bool(true);
   }

   private static SecretKey a_SecretKey_bool(boolean var0) {
      try {
         MessageDigest var1 = MessageDigest.getInstance("SHA-256");
         byte[] var2 = b(var0).getBytes(StandardCharsets.UTF_8);
         byte[] var3 = var1.digest(var2);
         byte[] var5 = var3;
         return new SecretKeySpec(var5, "AES");
      } catch (Exception var7) {
         throw new RuntimeException(var7);
      }
   }

   private static String b(boolean var0) {
      try {
         StringBuilder var1 = new StringBuilder();
         String var2 = System.getProperty("user.name", "");
         var1.append(var2).append(":");
         StringBuilder var3 = var1;
         String var4 = System.getProperty("os.name", "");
         var3.append(var4).append(":");
         StringBuilder var5 = var1;
         String var6 = System.getProperty("os.arch", "");
         var5.append(var6).append(":");
         StringBuilder var7 = var1;
         int var8 = Runtime.getRuntime().availableProcessors();
         var7.append(var8).append(":");
         String var9 = System.getenv("COMPUTERNAME");
         if (var9 != null) {
            StringBuilder var10 = var1;
            String var11 = var9;
            var10.append(var11).append(":");
         }

         String var12 = System.getenv("PROCESSOR_IDENTIFIER");
         if (var12 != null) {
            StringBuilder var13 = var1;
            String var14 = var12;
            var13.append(var14).append(":");
         }

         if (var0) {
            try {
               String[] var15 = new String[]{"reg", "query", "HKLM\\SOFTWARE\\Microsoft\\Cryptography", "/v", "MachineGuid"};
               Process var16 = new ProcessBuilder(var15).start();
               InputStream var18 = var16.getInputStream();
               InputStreamReader var17 = new InputStreamReader(var18, StandardCharsets.UTF_8);

               try (BufferedReader var19 = new BufferedReader(var17)) {
                  while (true) {
                     String var20 = var19.readLine();
                     if (var20 == null) {
                        break;
                     }

                     if (var20.contains("MachineGuid")) {
                        String[] var21 = var20.trim().split("\\s+");
                        if (var21.length >= 3) {
                           StringBuilder var22 = var1;
                           String[] var23 = var21;
                           int var24 = var21.length - 1;
                           String var25 = var23[var24];
                           var22.append(var25).append(":");
                        }
                     }
                  }
               }

               Process var30 = var16;
               long var31 = 2L;
               var30.waitFor(var31, TimeUnit.SECONDS);
            } catch (Throwable var67) {
            }
         }

         try {
            String[] var34 = new String[0];
            Path var35 = Paths.get("/etc/machine-id", var34);
            LinkOption[] var36 = new LinkOption[0];
            if (Files.exists(var35, var36)) {
               StringBuilder var37 = var1;
               String var38 = Files.readString(var35).trim();
               var37.append(var38).append(":");
            } else {
               String[] var39 = new String[0];
               Path var40 = Paths.get("/var/lib/dbus/machine-id", var39);
               LinkOption[] var41 = new LinkOption[0];
               if (Files.exists(var40, var41)) {
                  StringBuilder var42 = var1;
                  String var43 = Files.readString(var40).trim();
                  var42.append(var43).append(":");
               }
            }
         } catch (Throwable var64) {
         }

         MessageDigest var45 = MessageDigest.getInstance("SHA-256");
         byte[] var46 = var1.toString().getBytes(StandardCharsets.UTF_8);
         byte[] var47 = var45.digest(var46);
         int var48 = var47.length * 2;
         StringBuilder var49 = new StringBuilder(var48);
         byte[] var50 = var47;
         int var51 = var50.length;
         int var52 = 0;

         while (true) {
            int var53 = var52;
            int var54 = var51;
            if (var53 >= var54) {
               return var49.toString();
            }

            byte[] var55 = var50;
            int var56 = var52;
            byte var57 = var55[var56];
            StringBuilder var58 = var49;
            Object[] var59 = new Object[1];
            Byte var60 = var57;
            var59[0] = var60;
            String var61 = String.format("%02x", var59);
            var58.append(var61);
            var52++;
         }
      } catch (Exception var68) {
         return "DEFAULT_HWID_KEY";
      }
   }

   static String b(String var0) {
      if (var0 != null && !var0.isBlank()) {
         String var1 = var0.trim();

         try {
            Decoder var2 = Base64.getDecoder();
            String var3 = var1;
            byte[] var4 = var2.decode(var3);
            if (var4.length > 20) {
               byte var5 = var4[0];
               byte var6 = a[0];
               if (var5 == var6) {
                  byte var7 = var4[1];
                  byte var8 = a[1];
                  if (var7 == var8) {
                     byte var9 = var4[2];
                     byte var10 = a[2];
                     if (var9 == var10) {
                        byte var11 = var4[3];
                        byte var12 = a[3];
                        if (var11 == var12) {
                           ByteBuffer var13 = ByteBuffer.wrap(var4);
                           var13.getInt();
                           byte[] var14 = new byte[12];
                           ByteBuffer var15 = var13;
                           byte[] var16 = var14;
                           var15.get(var16);
                           byte[] var17 = new byte[var13.remaining()];
                           ByteBuffer var18 = var13;
                           byte[] var19 = var17;
                           var18.get(var19);
                           boolean[] var20 = new boolean[]{true, false};
                           boolean[] var21 = var20;
                           int var22 = var21.length;
                           int var23 = 0;

                           while (true) {
                              int var24 = var23;
                              int var25 = var22;
                              if (var24 >= var25) {
                                 break;
                              }

                              boolean[] var26 = var21;
                              int var27 = var23;
                              boolean var28 = var26[var27];

                              try {
                                 Cipher var29 = Cipher.getInstance("AES/GCM/NoPadding");
                                 SecretKey var30 = a_SecretKey_bool(var28);
                                 byte[] var31 = var14;
                                 var29.init(2, var30, new GCMParameterSpec(128, var31));
                                 Cipher var32 = var29;
                                 byte[] var33 = var17;
                                 byte[] var34 = var32.doFinal(var33);
                                 return new String(var34, StandardCharsets.UTF_8).trim();
                              } catch (AEADBadTagException var38) {
                                 var23++;
                              }
                           }
                        }
                     }
                  }
               }
            }
         } catch (Exception var39) {
         }

         return null;
      } else {
         return null;
      }
   }

   static String c(String var0) {
      if (var0 != null && !var0.isBlank()) {
         try {
            byte[] var1 = new byte[12];
            new SecureRandom().nextBytes(var1);
            Cipher var2 = Cipher.getInstance("AES/GCM/NoPadding");
            SecretKey var3 = g();
            byte[] var4 = var1;
            var2.init(1, var3, new GCMParameterSpec(128, var4));
            Cipher var5 = var2;
            byte[] var6 = var0.trim().getBytes(StandardCharsets.UTF_8);
            byte[] var7 = var5.doFinal(var6);
            int var8 = a.length;
            int var9 = var1.length;
            int var10 = var8 + var9;
            int var11 = var7.length;
            ByteBuffer var12 = ByteBuffer.allocate(var10 + var11);
            var12.put(a);
            ByteBuffer var13 = var12;
            byte[] var14 = var1;
            var13.put(var14);
            ByteBuffer var15 = var12;
            byte[] var16 = var7;
            var15.put(var16);
            Encoder var17 = Base64.getEncoder();
            byte[] var18 = var12.array();
            return var17.encodeToString(var18);
         } catch (Exception var21) {
            return null;
         }
      } else {
         return null;
      }
   }

   static {
      byte[] var0 = new byte[]{-98, -89, 67, 17};
      a = var0;
   }
}
