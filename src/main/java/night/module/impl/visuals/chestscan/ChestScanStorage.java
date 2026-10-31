package night.module.impl.visuals.chestscan;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

public class ChestScanStorage {
   private final Map<BlockPos, ChestScanStorage.ChestFillState> a = new HashMap<>();
   private File b;

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

   public void b() {
      this.a.clear();
      File var1 = FabricLoader.getInstance().getGameDir().toFile();
      File var2 = new File(var1, "night/chestscan");
      if (!var2.exists()) {
         var2.mkdirs();
      }

      File var3 = var2;
      this.b = new File(var3, a() + ".json");
      if (this.b.exists()) {
         try {
            File var4 = this.b;

            try (FileReader var5 = new FileReader(var4)) {
               Gson var6 = new Gson();
               Type var7 = (new TypeToken<List<ChestScanStorage.SavedChestEntry>>() {}).getType();
               Gson var8 = var6;
               FileReader var9 = var5;
               Type var10 = var7;
               List var11 = (List)var8.fromJson(var9, var10);
               if (var11 != null) {
                  for (ChestScanStorage.SavedChestEntry var13 : (Iterable<ChestScanStorage.SavedChestEntry>)(Object)var11) {
                     try {
                        Map var14 = this.a;
                        int var16 = var13.a;
                        int var17 = var13.b;
                        int var18 = var13.c;
                        BlockPos var15 = new BlockPos(var16, var17, var18);
                        ChestScanStorage.ChestFillState var19 = ChestScanStorage.ChestFillState.valueOf(var13.d);
                        var14.put(var15, var19);
                     } catch (Exception var27) {
                     }
                  }
               }
            }
         } catch (Exception var29) {
         }
      }
   }

   private void d() {
      if (this.b != null) {
         try {
            File var1 = this.b;

            try (FileWriter var2 = new FileWriter(var1)) {
               ArrayList var3 = new ArrayList();

               for (Entry var5 : (Iterable<Entry>)(Object)this.a.entrySet()) {
                  ChestScanStorage.SavedChestEntry var6 = new ChestScanStorage.SavedChestEntry();
                  int var7 = ((BlockPos)var5.getKey()).getX();
                  var6.a = var7;
                  ChestScanStorage.SavedChestEntry var8 = var6;
                  int var9 = ((BlockPos)var5.getKey()).getY();
                  var8.b = var9;
                  ChestScanStorage.SavedChestEntry var10 = var6;
                  int var11 = ((BlockPos)var5.getKey()).getZ();
                  var10.c = var11;
                  ChestScanStorage.SavedChestEntry var12 = var6;
                  String var13 = ((ChestScanStorage.ChestFillState)var5.getValue()).name();
                  var12.d = var13;
                  ArrayList var14 = var3;
                  ChestScanStorage.SavedChestEntry var15 = var6;
                  var14.add(var15);
               }

               Gson var16 = new GsonBuilder().setPrettyPrinting().create();
               ArrayList var17 = var3;
               FileWriter var18 = var2;
               var16.toJson(var17, var18);
            }
         } catch (Exception var26) {
         }
      }
   }

   public void a(BlockPos var1, ChestScanStorage.ChestFillState var2) {
      Map var3 = this.a;
      BlockPos var4 = var1.immutable();
      var3.put(var4, var2);
      this.d();
   }

   public ChestScanStorage.ChestFillState a_ChestFillState_BlockPos(BlockPos var1) {
      return this.a.get(var1);
   }

   public void b(BlockPos var1) {
      if (this.a.remove(var1) != null) {
         this.d();
      }
   }

   public Set<BlockPos> c() {
      return this.a.keySet();
   }

   public enum ChestFillState {
      EMPTY,
      PARTIAL,
      FULL;
   }

   static class SavedChestEntry {
      @SerializedName("x")
      int a;
      @SerializedName("y")
      int b;
      @SerializedName("z")
      int c;
      @SerializedName("status")
      String d;
   }
}
