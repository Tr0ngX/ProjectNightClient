package night.module.impl.visuals.stashfinder;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;

public class StashWebhookNotifier {
   private static volatile String a = "";
   private static volatile String b = "";
   private static volatile boolean c = false;
   private static volatile long d = 0L;

   private static File c() {
      File var0 = FabricLoader.getInstance().getGameDir().toFile();
      File var1 = new File(var0, "night/stashfinder");
      if (!var1.exists()) {
         var1.mkdirs();
      }

      File var2 = var1;
      return new File(var2, "webhook.json");
   }

   private static synchronized void d() {
      if (!c) {
         c = true;
         File var0 = c();
         if (var0.exists()) {
            try {
               File var1 = var0;

               try (FileReader var2 = new FileReader(var1)) {
                  JsonObject var3 = JsonParser.parseReader(var2).getAsJsonObject();
                  if (var3.has("url")) {
                     a = var3.get("url").getAsString();
                  }

                  if (var3.has("userId")) {
                     b = var3.get("userId").getAsString();
                  }
               }
            } catch (Exception var11) {
            }
         }
      }
   }

   private static synchronized void e() {
      try {
         File var0 = c();

         try (FileWriter var1 = new FileWriter(var0)) {
            JsonObject var2 = new JsonObject();
            var2.addProperty("url", a);
            var2.addProperty("userId", b);
            FileWriter var3 = var1;
            String var4 = var2.toString();
            var3.write(var4);
         }
      } catch (Exception var12) {
      }
   }

   public static String a() {
      d();
      return a;
   }

   public static String b() {
      d();
      return b;
   }

   public static void a(String var0) {
      d();
      a = var0;
      e();
   }

   public static void b(String var0) {
      d();
      b = var0;
      e();
   }

   private static synchronized boolean f() {
      long var0 = System.currentTimeMillis();
      long var2 = d;
      long var4 = var0 - var2;
      long var6 = 5000L;
      if (var4 < var6) {
         return false;
      }

      d = var0;
      return true;
   }

   private static String g() {
      Minecraft var0 = Minecraft.getInstance();
      if (var0.getCurrentServer() != null) {
         return var0.getCurrentServer().ip;
      } else {
         return var0.hasSingleplayerServer() && var0.getSingleplayerServer() != null
            ? "Singleplayer: " + var0.getSingleplayerServer().getWorldData().getLevelName()
            : "Unknown";
      }
   }

   public static void a(int var0, int var1, String var2, Map<String, Integer> var3, String var4, String var5) {
      String var6;
      if (var4 != null && !var4.isBlank()) {
         var6 = var4;
      } else {
         var6 = a();
      }

      String var7 = var6;
      if (var7 != null && !var7.isBlank()) {
         if (f()) {
            String var8 = g();
            String var9;
            if (var5 != null && !var5.isBlank()) {
               var9 = var5;
            } else {
               var9 = b();
            }

            String var10 = var9;
            String var11 = var8;
            String var12 = var10;
            String var13 = var7;
            CompletableFuture.runAsync(() -> {
               try {
                  JsonObject var7x = new JsonObject();
                  var7x.addProperty("title", "Stash Found!");
                  var7x.addProperty("description", "Coordinates: **X: " + var0 + " Z: " + var1 + "** (" + var2 + ")");
                  JsonObject var8x = var7x;
                  Integer var9x = 3066993;
                  var8x.addProperty("color", var9x);
                  JsonArray var10x = new JsonArray();
                  JsonObject var11x = new JsonObject();
                  var11x.addProperty("name", "Server");
                  var11x.addProperty("value", var11);
                  JsonObject var12x = var11x;
                  Boolean var13x = false;
                  var12x.addProperty("inline", var13x);
                  JsonArray var14 = var10x;
                  JsonObject var15 = var11x;
                  var14.add(var15);

                  for (Entry var17 : (Iterable<Entry>)(Object)var3.entrySet()) {
                     JsonObject var18 = new JsonObject();
                     String var19 = (String)var17.getKey();
                     var18.addProperty("name", var19);
                     JsonObject var20 = var18;
                     String var21 = String.valueOf(var17.getValue());
                     var20.addProperty("value", var21);
                     JsonObject var22 = var18;
                     Boolean var23 = true;
                     var22.addProperty("inline", var23);
                     JsonArray var24 = var10x;
                     JsonObject var25 = var18;
                     var24.add(var25);
                  }

                  JsonObject var26 = var7x;
                  JsonArray var27 = var10x;
                  var26.add("fields", var27);
                  JsonArray var28 = new JsonArray();
                  JsonObject var29 = var7x;
                  var28.add(var29);
                  JsonObject var30 = new JsonObject();
                  String var31;
                  if (var12 != null && !var12.isBlank()) {
                     var31 = "<@" + var12 + ">";
                  } else {
                     var31 = "";
                  }

                  var30.addProperty("content", var31);
                  JsonObject var32 = var30;
                  JsonArray var33 = var28;
                  var32.add("embeds", var33);
                  byte[] var34 = var30.toString().getBytes(StandardCharsets.UTF_8);
                  HttpURLConnection var35 = (HttpURLConnection)URI.create(var13).toURL().openConnection();
                  var35.setRequestMethod("POST");
                  var35.setRequestProperty("Content-Type", "application/json");
                  var35.setRequestProperty("User-Agent", "Mozilla/5.0");
                  var35.setDoOutput(true);
                  var35.setConnectTimeout(8000);
                  var35.setReadTimeout(8000);

                  try (OutputStream var36 = var35.getOutputStream()) {
                     byte[] var37 = var34;
                     var36.write(var37);
                     var36.flush();
                  }

                  var35.getResponseCode();
                  var35.disconnect();
               } catch (Exception var45) {
               }
            });
         }
      }
   }
}
