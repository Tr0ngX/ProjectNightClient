package night.module.impl.visuals.stashfinder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;

public class StashFinderStorage {
   private final Set<Long> a = new HashSet<>();
   private final List<StashFinderStorage.StashEntry> b = new ArrayList<>();
   private File c;
   private boolean d = false;
   private long e;

   public StashFinderStorage() {
      long var1 = 0L;
      this.e = var1;
   }

   public static String a() {
      Minecraft var0 = Minecraft.getInstance();
      String var1;
      if (var0.getCurrentServer() != null) {
         var1 = "server_" + var0.getCurrentServer().ip;
      } else if (var0.hasSingleplayerServer() && var0.getSingleplayerServer() != null) {
         var1 = "sp_" + var0.getSingleplayerServer().getWorldData().getLevelName();
      } else {
         var1 = "unknown";
      }

      String var2;
      if (var0.level != null) {
         var2 = var0.level.dimension().identifier().toString();
      } else {
         var2 = "no_dimension";
      }

      String var3 = var2;
      String var4 = a_String_String(var1);
      String var5 = a_String_String(var3);
      return var4 + "__" + var5;
   }

   private static String a_String_String(String var0) {
      return var0.replaceAll("[^a-zA-Z0-9_.-]", "_");
   }

   public static long a(int var0, int var1) {
      long var2 = var0;
      long var4 = 4294967295L;
      long var6 = var2 & var4;
      long var8 = var1;
      long var10 = 4294967295L;
      long var12 = (var8 & var10) << 32;
      return var6 | var12;
   }

   public void b() {
      this.e();
      this.a.clear();
      this.b.clear();
      File var1 = FabricLoader.getInstance().getGameDir().toFile();
      File var2 = new File(var1, "night/stashfinder");
      if (!var2.exists()) {
         var2.mkdirs();
      }

      File var3 = var2;
      this.c = new File(var3, a() + ".json");
      if (this.c.exists()) {
         try {
            File var4 = this.c;

            try (FileReader var5 = new FileReader(var4)) {
               Gson var6 = new Gson();
               Type var7 = (new TypeToken<StashFinderStorage.SavedStashData>() {}).getType();
               Gson var8 = var6;
               FileReader var9 = var5;
               Type var10 = var7;
               StashFinderStorage.SavedStashData var11 = (StashFinderStorage.SavedStashData)var8.fromJson(var9, var10);
               if (var11 != null) {
                  if (var11.a != null) {
                     Set var12 = this.a;
                     Set var13 = var11.a;
                     var12.addAll(var13);
                  }

                  if (var11.b != null) {
                     List var14 = this.b;
                     List var15 = var11.b;
                     var14.addAll(var15);
                  }
               }
            }
         } catch (Exception var23) {
         }
      }
   }

   private void g() {
      if (this.c != null) {
         try {
            File var1 = this.c;

            try (FileWriter var2 = new FileWriter(var1)) {
               StashFinderStorage.SavedStashData var3 = new StashFinderStorage.SavedStashData();
               Set var4 = this.a;
               var3.a = var4;
               StashFinderStorage.SavedStashData var5 = var3;
               List var6 = this.b;
               var5.b = var6;
               Gson var7 = new GsonBuilder().create();
               StashFinderStorage.SavedStashData var8 = var3;
               FileWriter var9 = var2;
               var7.toJson(var8, var9);
            }
         } catch (Exception var17) {
         }
      }
   }

   public boolean a_bool_long(long var1) {
      Set var3 = this.a;
      Long var4 = var1;
      return var3.contains(var4);
   }

   public void b(long var1) {
      Set var3 = this.a;
      Long var4 = var1;
      if (var3.add(var4)) {
         this.d = true;
      }
   }

   public void a(int var1, int var2, String var3, Map<String, Integer> var4) {
      List var5 = this.b;
      long var6 = System.currentTimeMillis();
      var5.add(new StashFinderStorage.StashEntry(var1, var2, var3, var4, var6));
      this.d = true;
   }

   public List<StashFinderStorage.StashEntry> c() {
      return Collections.unmodifiableList(this.b);
   }

   public void d() {
      if (this.d) {
         long var1 = System.currentTimeMillis();
         long var3 = this.e;
         long var5 = var1 - var3;
         long var7 = 10000L;
         if (var5 >= var7) {
            this.e();
         }
      }
   }

   public void e() {
      if (this.d) {
         this.g();
         this.d = false;
         long var1 = System.currentTimeMillis();
         this.e = var1;
      }
   }

   public void f() {
      this.a.clear();
      this.b.clear();
      this.d = true;
      this.e();
   }

   static class SavedStashData {
      @SerializedName("evaluated")
      Set<Long> a = new HashSet<>();
      @SerializedName("stashes")
      List<StashFinderStorage.StashEntry> b = new ArrayList<>();
   }

   public record StashEntry(
      @SerializedName("x") int count,
      @SerializedName("z") int count2,
      @SerializedName("dimension") String text,
      @SerializedName("counts") Map<String, Integer> map,
      @SerializedName("timestamp") long timestamp
   ) {
      public StashEntry(int count, int count2, String text, Map<String, Integer> map, long timestamp) {
         this.count = count;
         this.count2 = count2;
         this.text = text;
         this.map = map;
         long var7 = timestamp;
         this.timestamp = var7;
      }
   }
}
