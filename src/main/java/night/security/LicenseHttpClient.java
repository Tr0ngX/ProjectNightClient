package night.security;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpClient.Builder;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.List;
import night.Night;

public class LicenseHttpClient {
   private static final String a = System.getProperty("night.license.baseUrl", "https://nightclient.info.vn");
   private static final String b = "http://nightclient.info.vn";
   private static final Gson c = new Gson();
   private final HttpClient d;

   public LicenseHttpClient() {
      Builder var1 = HttpClient.newBuilder();
      Duration var2 = Duration.ofSeconds(5L);
      HttpClient var3 = var1.connectTimeout(var2).build();
      this.d = var3;
   }

   public LicenseHttpClient.HeartbeatResponse a(String var1, String var2, String var3, List<String> var4, String var5, String var6) {
      LicenseHttpClient.HeartbeatResponse var7 = this.a(a, var1, var2, var3, var4, var5, var6);
      if (var7 == null) {
         var7 = this.a(a, var1, var2, var3, var4, var5, var6);
      }

      if (var7 == null && !a.equalsIgnoreCase(b)) {
         var7 = this.a(b, var1, var2, var3, var4, var5, var6);
      }

      return var7;
   }

   private LicenseHttpClient.HeartbeatResponse a(String var1, String var2, String var3, String var4, List<String> var5, String var6, String var7) {
      try {
         JsonObject var8 = new JsonObject();
         var8.addProperty("license_key", var2);
         var8.addProperty("hwid", var3);
         if (var4 != null) {
            var8.addProperty("build_sha256", var4);
         }

         JsonObject var9 = var8;
         JsonElement var10 = c.toJsonTree(var5);
         var9.add("tamper_flags", var10);
         if (var6 != null) {
            var8.addProperty("ign", var6);
         }

         if (var7 != null) {
            var8.addProperty("device_info", var7);
         }

         java.net.http.HttpRequest.Builder var11 = HttpRequest.newBuilder();
         URI var12 = URI.create(var1 + "/heartbeat");
         java.net.http.HttpRequest.Builder var13 = var11.uri(var12);
         Duration var14 = Duration.ofSeconds(8L);
         java.net.http.HttpRequest.Builder var15 = var13.timeout(var14).header("Content-Type", "application/json");
         JsonObject var16 = var8;
         BodyPublisher var17 = BodyPublishers.ofString(c.toJson(var16));
         HttpRequest var18 = var15.POST(var17).build();
         HttpClient var19 = this.d;
         HttpRequest var20 = var18;
         BodyHandler<String> var21 = BodyHandlers.ofString();
         String var22 = var19.<String>send(var20, var21).body();
         JsonObject var23 = (JsonObject)c.fromJson(var22, JsonObject.class);
         LicenseHttpClient.HeartbeatResponse var24 = new LicenseHttpClient.HeartbeatResponse();
         boolean var25;
         if (var23 != null && var23.has("allow") && var23.get("allow").getAsBoolean()) {
            var25 = true;
         } else {
            var25 = false;
         }

         var24.a = var25;
         LicenseHttpClient.HeartbeatResponse var26 = var24;
         String var27;
         if (var23 != null && var23.has("status")) {
            var27 = var23.get("status").getAsString();
         } else {
            var27 = "invalid_key";
         }

         var26.b = var27;
         LicenseHttpClient.HeartbeatResponse var28 = var24;
         String var29;
         if (var23 != null && var23.has("role")) {
            var29 = var23.get("role").getAsString();
         } else {
            var29 = null;
         }

         var28.c = var29;
         LicenseHttpClient.HeartbeatResponse var30 = var24;
         String var31;
         if (var23 != null && var23.has("uid")) {
            var31 = var23.get("uid").getAsString();
         } else {
            var31 = null;
         }

         var30.d = var31;
         LicenseHttpClient.HeartbeatResponse var32 = var24;
         String var33;
         if (var23 != null && var23.has("lp")) {
            var33 = var23.get("lp").getAsString();
         } else {
            var33 = null;
         }

         var32.e = var33;
         LicenseHttpClient.HeartbeatResponse var34 = var24;
         String var35;
         if (var23 != null && var23.has("lps")) {
            var35 = var23.get("lps").getAsString();
         } else {
            var35 = null;
         }

         var34.f = var35;
         return var24;
      } catch (Exception var38) {
         String var37 = var38.toString();
         Night.LOGGER.debug(var37);
         return null;
      }
   }

   public static class HeartbeatResponse {
      public boolean a;
      public String b;
      public String c;
      public String d;
      public String e;
      public String f;
   }
}
