package meteordevelopment.discordipc;

import com.google.gson.JsonObject;

public class RichPresence {
   private String details;
   private String state;
   private RichPresence.Assets assets;
   private RichPresence.Timestamps timestamps;

   public void setDetails(String var1) {
      this.details = var1;
   }

   public void setState(String var1) {
      this.state = var1;
   }

   public void setLargeImage(String var1, String var2) {
      if (this.assets == null) {
         this.assets = new RichPresence.Assets();
      }

      this.assets.large_image = var1;
      this.assets.large_text = var2;
   }

   public void setSmallImage(String var1, String var2) {
      if (this.assets == null) {
         this.assets = new RichPresence.Assets();
      }

      this.assets.small_image = var1;
      this.assets.small_text = var2;
   }

   public void setStart(long var1) {
      if (this.timestamps == null) {
         this.timestamps = new RichPresence.Timestamps();
      }

      this.timestamps.start = var1;
   }

   public void setEnd(long var1) {
      if (this.timestamps == null) {
         this.timestamps = new RichPresence.Timestamps();
      }

      this.timestamps.end = var1;
   }

   public JsonObject toJson() {
      JsonObject var1 = new JsonObject();
      if (this.details != null) {
         var1.addProperty("details", this.details);
      }

      if (this.state != null) {
         var1.addProperty("state", this.state);
      }

      if (this.assets != null) {
         JsonObject var2 = new JsonObject();
         if (this.assets.large_image != null) {
            var2.addProperty("large_image", this.assets.large_image);
         }

         if (this.assets.large_text != null) {
            var2.addProperty("large_text", this.assets.large_text);
         }

         if (this.assets.small_image != null) {
            var2.addProperty("small_image", this.assets.small_image);
         }

         if (this.assets.small_text != null) {
            var2.addProperty("small_text", this.assets.small_text);
         }

         var1.add("assets", var2);
      }

      if (this.timestamps != null) {
         JsonObject var3 = new JsonObject();
         if (this.timestamps.start != null) {
            var3.addProperty("start", this.timestamps.start);
         }

         if (this.timestamps.end != null) {
            var3.addProperty("end", this.timestamps.end);
         }

         var1.add("timestamps", var3);
      }

      return var1;
   }

   public static class Assets {
      public String large_image;
      public String large_text;
      public String small_image;
      public String small_text;
   }

   public static class Timestamps {
      public Long start;
      public Long end;
   }
}
