package night.accounts;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpServer;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import night.Night;
import night.util.world.PacketAndRenderUtil;

public class MicrosoftAuth {
   public static final String CLIENT_ID = "54fd49e4-2103-4044-9603-2b028c814ec3";
   public static final String SCOPE = "XboxLive.signin XboxLive.offline_access";
   public static final String REDIRECT_PATH = "/in_game_account_switcher_long_enough_uri_to_prevent_accidental_leaks_on_screensharing_even_if_you_have_like_extremely_big_screen_though_it_might_not_mork_but_we_will_try_it_anyway_to_prevent_funny_things_from_happening_or_something";

   public static MicrosoftAuth.BrowserLoginSession startBrowserLogin(Consumer<String> var0) throws Exception {
      HttpServer var1 = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
      String var2 = "http://localhost:"
         + var1.getAddress().getPort()
         + "/in_game_account_switcher_long_enough_uri_to_prevent_accidental_leaks_on_screensharing_even_if_you_have_like_extremely_big_screen_though_it_might_not_mork_but_we_will_try_it_anyway_to_prevent_funny_things_from_happening_or_something";
      String var3 = UUID.randomUUID().toString().replace("-", "");
      String var4 = URLEncoder.encode("XboxLive.signin XboxLive.offline_access", StandardCharsets.UTF_8);
      String var5 = URLEncoder.encode(var2, StandardCharsets.UTF_8);
      String var6 = var3;
      String var7 = "https://login.live.com/oauth20_authorize.srf?client_id=54fd49e4-2103-4044-9603-2b028c814ec3&response_type=code&scope="
         + var4
         + "&redirect_uri="
         + var5
         + "&prompt=select_account&state="
         + var6;
      CompletableFuture var8 = new CompletableFuture();
      HttpServer var9 = var1;
      CompletableFuture var10 = var8;
      HttpServer var11 = var1;
      var9.createContext(
         "/in_game_account_switcher_long_enough_uri_to_prevent_accidental_leaks_on_screensharing_even_if_you_have_like_extremely_big_screen_though_it_might_not_mork_but_we_will_try_it_anyway_to_prevent_funny_things_from_happening_or_something",
         var2x -> {
            Map var3x = parseQueryParams(var2x.getRequestURI().getRawQuery());
            if (var3x.containsKey("code")) {
               String var4x = (String)var3x.get("code");
               byte[] var5x = "<!DOCTYPE html><html><head><meta charset=\"utf-8\"><title>Night Client</title><style>body{background:#120e1e;color:#fff;font-family:sans-serif;display:flex;justify-content:center;align-items:center;height:100vh;margin:0;}div{text-align:center;background:#181426;padding:40px;border-radius:12px;border:1px solid #6450a0;box-shadow:0 8px 24px rgba(0,0,0,0.5);}h1{color:#55ffff;margin-bottom:10px;}p{color:#a0a0b4;font-size:16px;}</style></head><body><div><h1>Login Successful!</h1><p>You can close this browser tab and return to Minecraft.</p></div></body></html>"
                  .getBytes(StandardCharsets.UTF_8);
               var2x.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
               long var6x = var5x.length;
               var2x.sendResponseHeaders(200, var6x);

               try (OutputStream var8x = var2x.getResponseBody()) {
                  byte[] var9x = var5x;
                  var8x.write(var9x);
               }

               String var14x = var4x;
               var10.complete(var14x);
            } else {
               Map var15 = var3x;
               String var16x = (String)var3x.getOrDefault("error", "Login was cancelled or failed");
               String var17x = (String)var15.getOrDefault("error_description", var16x);
               byte[] var18x = ("<!DOCTYPE html><html><head><meta charset=\"utf-8\"><title>Night Client</title><style>body{background:#120e1e;color:#fff;font-family:sans-serif;display:flex;justify-content:center;align-items:center;height:100vh;margin:0;}div{text-align:center;background:#181426;padding:40px;border-radius:12px;border:1px solid #a03030;}h1{color:#ff5555;margin-bottom:10px;}p{color:#a0a0b4;font-size:16px;}</style></head><body><div><h1>Login Cancelled</h1><p>"
                     + var17x
                     + "</p></div></body></html>")
                  .getBytes(StandardCharsets.UTF_8);
               var2x.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
               long var19x = var18x.length;
               var2x.sendResponseHeaders(400, var19x);

               try (OutputStream var21 = var2x.getResponseBody()) {
                  byte[] var22 = var18x;
                  var21.write(var22);
               }

               String var27 = var17x;
               var10.completeExceptionally(new RuntimeException(var27));
            }

            new Thread(() -> {
               try {
                  Thread.sleep(1000L);
               } catch (InterruptedException var2xx) {
               }

               var11.stop(0);
            }).start();
         }
      );
      var1.start();
      CompletableFuture var12 = var8;
      String var13 = var2;
      CompletableFuture var14 = var12.thenApplyAsync(var2x -> {
         try {
            var0.accept("Exchanging authorization code with Microsoft...");
            HashMap var3x = new HashMap();
            var3x.put("client_id", "54fd49e4-2103-4044-9603-2b028c814ec3");
            var3x.put("grant_type", "authorization_code");
            var3x.put("code", var2x);
            var3x.put("redirect_uri", var13);
            var3x.put("scope", "XboxLive.signin XboxLive.offline_access");
            String var4x = encodeParams(var3x);
            JsonObject var5x = postForm("https://login.live.com/oauth20_token.srf", var4x);
            if (!var5x.has("access_token")) {
               String var6x;
               if (var5x.has("error_description")) {
                  var6x = var5x.get("error_description").getAsString();
               } else {
                  var6x = "Failed to obtain Microsoft token.";
               }

               String var7x = var6x;
               String var9x = var7x;
               return new MicrosoftAuth.AuthResult(false, null, null, null, null, var9x);
            } else {
               String var10x = var5x.get("access_token").getAsString();
               String var11x;
               if (var5x.has("refresh_token")) {
                  var11x = var5x.get("refresh_token").getAsString();
               } else {
                  var11x = "";
               }

               String var12x = var11x;
               String var13x = var10x;
               String var14x = var12x;
               return completeMinecraftLogin(var13x, var14x, var0);
            }
         } catch (Exception var18x) {
            Night.LOGGER.error("Error during Microsoft login exchange", var18x);
            String var17x = var18x.getMessage();
            return new MicrosoftAuth.AuthResult(false, null, null, null, null, var17x);
         }
      });

      try {
         PacketAndRenderUtil.a_void_URI(URI.create(var7));
      } catch (Exception var20) {
      }

      String var16 = var7;
      CompletableFuture var17 = var14;
      CompletableFuture var18 = var8;
      HttpServer var19 = var1;
      return new MicrosoftAuth.BrowserLoginSession(var16, var17, () -> {
         var18.cancel(true);

         try {
            var19.stop(0);
         } catch (Exception var3x) {
         }
      });
   }

   public static MicrosoftAuth.AuthResult refreshAccount(String var0) {
      try {
         HashMap var1 = new HashMap();
         var1.put("client_id", "54fd49e4-2103-4044-9603-2b028c814ec3");
         var1.put("grant_type", "refresh_token");
         var1.put("refresh_token", var0);
         var1.put("scope", "XboxLive.signin XboxLive.offline_access");
         String var2 = encodeParams(var1);
         JsonObject var3 = postForm("https://login.live.com/oauth20_token.srf", var2);
         if (!var3.has("access_token")) {
            String var4;
            if (var3.has("error_description")) {
               var4 = var3.get("error_description").getAsString();
            } else {
               var4 = "Failed to refresh Microsoft token.";
            }

            String var5 = var4;
            String var7 = var5;
            return new MicrosoftAuth.AuthResult(false, null, null, null, null, var7);
         } else {
            String var8 = var3.get("access_token").getAsString();
            String var9;
            if (var3.has("refresh_token")) {
               var9 = var3.get("refresh_token").getAsString();
            } else {
               var9 = var0;
            }

            String var10 = var9;
            String var11 = var8;
            String var12 = var10;
            return completeMinecraftLogin(var11, var12, var0x -> {});
         }
      } catch (Exception var16) {
         Night.LOGGER.error("Failed to refresh Microsoft account", var16);
         String var15 = var16.getMessage();
         return new MicrosoftAuth.AuthResult(false, null, null, null, null, var15);
      }
   }

   public static MicrosoftAuth.AuthResult completeMinecraftLogin(String var0, String var1, Consumer<String> var2) throws Exception {
      var2.accept("Authenticating with Xbox Live...");
      JsonObject var3 = new JsonObject();
      JsonObject var4 = new JsonObject();
      var4.addProperty("AuthMethod", "RPS");
      var4.addProperty("SiteName", "user.auth.xboxlive.com");
      var4.addProperty("RpsTicket", "d=" + var0);
      JsonObject var5 = var3;
      JsonObject var6 = var4;
      var5.add("Properties", var6);
      var3.addProperty("RelyingParty", "http://auth.xboxlive.com");
      var3.addProperty("TokenType", "JWT");
      JsonObject var7 = var3;
      JsonObject var8 = postJson("https://user.auth.xboxlive.com/user/authenticate", var7);
      String var9 = var8.get("Token").getAsString();
      String var10 = var8.getAsJsonObject("DisplayClaims").getAsJsonArray("xui").get(0).getAsJsonObject().get("uhs").getAsString();
      var2.accept("Requesting XSTS authorization...");
      JsonObject var11 = new JsonObject();
      JsonObject var12 = new JsonObject();
      var12.addProperty("SandboxId", "RETAIL");
      JsonArray var13 = new JsonArray();
      String var14 = var9;
      var13.add(var14);
      JsonObject var15 = var12;
      JsonArray var16 = var13;
      var15.add("UserTokens", var16);
      JsonObject var17 = var11;
      JsonObject var18 = var12;
      var17.add("Properties", var18);
      var11.addProperty("RelyingParty", "rp://api.minecraftservices.com/");
      var11.addProperty("TokenType", "JWT");
      JsonObject var19 = var11;
      JsonObject var20 = postJson("https://xsts.auth.xboxlive.com/xsts/authorize", var19);
      if (var20.has("XErr")) {
         long var21 = var20.get("XErr").getAsLong();
         long var23 = 2148916233L;
         if (var21 == var23) {
            return new MicrosoftAuth.AuthResult(false, null, null, null, null, "This Microsoft account does not have an Xbox Live account.");
         }

         long var25 = var21;
         long var27 = 2148916238L;
         return var25 == var27
            ? new MicrosoftAuth.AuthResult(false, null, null, null, null, "This account is a child account and must be added to a Family.")
            : new MicrosoftAuth.AuthResult(false, null, null, null, null, "XSTS Error: " + var21);
      } else {
         String var29 = var20.get("Token").getAsString();
         var2.accept("Logging into Minecraft Services...");
         JsonObject var30 = new JsonObject();
         String var31 = var10;
         String var32 = var29;
         var30.addProperty("identityToken", "XBL3.0 x=" + var31 + ";" + var32);
         JsonObject var33 = var30;
         JsonObject var34 = postJson("https://api.minecraftservices.com/authentication/login_with_xbox", var33);
         if (!var34.has("access_token")) {
            String var35 = "Failed to obtain Minecraft access token.";
            if (var34.has("errorMessage")) {
               var35 = var34.get("errorMessage").getAsString();
            } else if (var34.has("error")) {
               var35 = var34.get("error").getAsString();
            }

            String var36 = var35;
            return new MicrosoftAuth.AuthResult(false, null, null, null, null, var36);
         } else {
            String var37 = var34.get("access_token").getAsString();
            var2.accept("Retrieving Minecraft profile...");
            String var38 = var37;
            JsonObject var39 = getJson("https://api.minecraftservices.com/minecraft/profile", var38);
            if (var39.has("name") && var39.has("id")) {
               String var40 = var39.get("name").getAsString();
               String var41 = var39.get("id").getAsString();
               var2.accept("Logged in as " + var40 + "!");
               String var42 = var40;
               String var43 = var41;
               String var44 = var37;
               return new MicrosoftAuth.AuthResult(true, var42, var43, var44, var1, null);
            } else {
               return new MicrosoftAuth.AuthResult(false, null, null, null, null, "You do not own Minecraft on this Microsoft account.");
            }
         }
      }
   }

   private static JsonObject postForm(String var0, String var1) throws Exception {
      HttpURLConnection var2 = (HttpURLConnection)new URI(var0).toURL().openConnection();
      var2.setRequestMethod("POST");
      var2.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
      var2.setRequestProperty("Accept", "application/json");
      var2.setDoOutput(true);
      var2.setConnectTimeout(10000);
      var2.setReadTimeout(15000);

      try (OutputStream var3 = var2.getOutputStream()) {
         byte[] var4 = var1.getBytes(StandardCharsets.UTF_8);
         var3.write(var4);
      }

      InputStreamReader var6;
      if (var2.getResponseCode() >= 200 && var2.getResponseCode() < 400) {
         InputStream var5 = var2.getInputStream();
         var6 = new InputStreamReader(var5, StandardCharsets.UTF_8);
      } else {
         InputStream var7 = var2.getErrorStream();
         var6 = new InputStreamReader(var7, StandardCharsets.UTF_8);
      }

      return JsonParser.parseReader(var6).getAsJsonObject();
   }

   private static JsonObject postJson(String var0, JsonObject var1) throws Exception {
      HttpURLConnection var2 = (HttpURLConnection)new URI(var0).toURL().openConnection();
      var2.setRequestMethod("POST");
      var2.setRequestProperty("Content-Type", "application/json");
      var2.setRequestProperty("Accept", "application/json");
      var2.setDoOutput(true);
      var2.setConnectTimeout(10000);
      var2.setReadTimeout(15000);

      try (OutputStream var3 = var2.getOutputStream()) {
         byte[] var4 = var1.toString().getBytes(StandardCharsets.UTF_8);
         var3.write(var4);
      }

      InputStreamReader var6;
      if (var2.getResponseCode() >= 200 && var2.getResponseCode() < 400) {
         InputStream var5 = var2.getInputStream();
         var6 = new InputStreamReader(var5, StandardCharsets.UTF_8);
      } else {
         InputStream var7 = var2.getErrorStream();
         var6 = new InputStreamReader(var7, StandardCharsets.UTF_8);
      }

      return JsonParser.parseReader(var6).getAsJsonObject();
   }

   private static JsonObject getJson(String var0, String var1) throws Exception {
      HttpURLConnection var2 = (HttpURLConnection)new URI(var0).toURL().openConnection();
      var2.setRequestMethod("GET");
      var2.setRequestProperty("Authorization", "Bearer " + var1);
      var2.setRequestProperty("Accept", "application/json");
      var2.setConnectTimeout(10000);
      var2.setReadTimeout(15000);
      InputStreamReader var4;
      if (var2.getResponseCode() >= 200 && var2.getResponseCode() < 400) {
         InputStream var3 = var2.getInputStream();
         var4 = new InputStreamReader(var3, StandardCharsets.UTF_8);
      } else {
         InputStream var5 = var2.getErrorStream();
         var4 = new InputStreamReader(var5, StandardCharsets.UTF_8);
      }

      return JsonParser.parseReader(var4).getAsJsonObject();
   }

   private static String encodeParams(Map<String, String> var0) {
      StringBuilder var1 = new StringBuilder();
      boolean var2 = true;

      for (Entry var4 : (Iterable<Entry>)(Object)var0.entrySet()) {
         if (var2) {
            var2 = false;
         } else {
            var1.append("&");
         }

         StringBuilder var5 = var1;
         String var6 = URLEncoder.encode((String)var4.getKey(), StandardCharsets.UTF_8);
         var5.append(var6);
         var1.append("=");
         StringBuilder var7 = var1;
         String var8 = URLEncoder.encode((String)var4.getValue(), StandardCharsets.UTF_8);
         var7.append(var8);
      }

      return var1.toString();
   }

   private static Map<String, String> parseQueryParams(String var0) {
      HashMap var1 = new HashMap();
      if (var0 != null && !var0.isEmpty()) {
         String[] var2 = var0.split("&");
         int var3 = var2.length;
         int var4 = 0;

         while (true) {
            int var5 = var4;
            int var6 = var3;
            if (var5 >= var6) {
               return var1;
            }

            String[] var7 = var2;
            int var8 = var4;
            String[] var9 = var7[var8].split("=", 2);
            if (var9.length == 2) {
               HashMap var10 = var1;
               String var11 = URLDecoder.decode(var9[0], StandardCharsets.UTF_8);
               String var12 = URLDecoder.decode(var9[1], StandardCharsets.UTF_8);
               var10.put(var11, var12);
            } else if (var9.length == 1) {
               HashMap var13 = var1;
               String var14 = URLDecoder.decode(var9[0], StandardCharsets.UTF_8);
               var13.put(var14, "");
            }

            var4++;
         }
      } else {
         return var1;
      }
   }

   public record AuthResult(boolean success, String username, String uuid, String accessToken, String refreshToken, String errorMessage) {
   }

   public record BrowserLoginSession(String loginUrl, CompletableFuture<MicrosoftAuth.AuthResult> future, Runnable cancelAction) {
   }
}
