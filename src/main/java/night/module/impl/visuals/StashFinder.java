package night.module.impl.visuals;

import java.awt.Color;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents.Load;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.equine.Donkey;
import net.minecraft.world.entity.animal.equine.Llama;
import net.minecraft.world.entity.vehicle.boat.ChestBoat;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BlastFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.CrafterBlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.entity.DropperBlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.entity.SmokerBlockEntity;
import net.minecraft.world.level.block.entity.TrappedChestBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import night.Night;
import night.events.EventTarget;
import night.events.impl.Render3DEvent;
import night.events.impl.TickEvent;
import night.module.Module;
import night.module.ModuleInfo;
import night.module.impl.visuals.stashfinder.StashFinderStorage;
import night.module.impl.visuals.stashfinder.StashWebhookNotifier;
import night.module.impl.visuals.stashfinder.StructureChunkFilter;
import night.setting.impl.BooleanSetting;
import night.setting.impl.CategorySetting;
import night.setting.impl.ColorSetting;
import night.setting.impl.NumberSetting;
import night.setting.impl.StringSetting;
import night.util.render.Render3DUtil;

@ModuleInfo(
   name = "StashFinder",
   description = "Scans loaded chunks for loot stashes and storage animals, alerting via chat and Discord webhook.",
   category = Module.Category.VISUALS
)
public class StashFinder extends Module {
   public CategorySetting containersCategory = new CategorySetting("Containers", "Threshold settings for containers.");
   public NumberSetting chests = new NumberSetting(
      "Chests", "Minimum chests required.", new CategorySetting.CategoryCondition(this.containersCategory), 10, 1, 64
   );
   public NumberSetting barrels = new NumberSetting(
      "Barrels", "Minimum barrels required.", new CategorySetting.CategoryCondition(this.containersCategory), 10, 1, 64
   );
   public NumberSetting shulkers = new NumberSetting(
      "Shulkers", "Minimum shulker boxes required.", new CategorySetting.CategoryCondition(this.containersCategory), 4, 1, 64
   );
   public NumberSetting enderChests = new NumberSetting(
      "EnderChests", "Minimum ender chests required.", new CategorySetting.CategoryCondition(this.containersCategory), 2, 1, 64
   );
   public NumberSetting hoppers = new NumberSetting(
      "Hoppers", "Minimum hoppers required.", new CategorySetting.CategoryCondition(this.containersCategory), 10, 1, 64
   );
   public NumberSetting dispensersDroppers = new NumberSetting(
      "Dispensers/Droppers", "Minimum dispensers or droppers required.", new CategorySetting.CategoryCondition(this.containersCategory), 10, 1, 64
   );
   public NumberSetting furnaces = new NumberSetting(
      "Furnaces", "Minimum furnaces required.", new CategorySetting.CategoryCondition(this.containersCategory), 10, 1, 64
   );
   public NumberSetting crafters = new NumberSetting(
      "Crafters", "Minimum crafters required.", new CategorySetting.CategoryCondition(this.containersCategory), 4, 1, 64
   );
   public CategorySetting animalsCategory = new CategorySetting("Animals", "Threshold settings for storage animals.");
   public NumberSetting donkeys = new NumberSetting(
      "Donkeys", "Minimum donkeys required.", new CategorySetting.CategoryCondition(this.animalsCategory), 2, 1, 16
   );
   public NumberSetting llamas = new NumberSetting("Llamas", "Minimum llamas required.", new CategorySetting.CategoryCondition(this.animalsCategory), 2, 1, 16);
   public NumberSetting chestBoats = new NumberSetting(
      "ChestBoats", "Minimum chest boats required.", new CategorySetting.CategoryCondition(this.animalsCategory), 1, 1, 16
   );
   public CategorySetting generalCategory = new CategorySetting("General", "General settings.");
   public BooleanSetting ignoreNatural = new BooleanSetting(
      "IgnoreNatural", "Ignores naturally generated structures.", new CategorySetting.CategoryCondition(this.generalCategory), true
   );
   public BooleanSetting chatNotify2 = new BooleanSetting(
      "ChatNotify", "Notifies in chat when a stash is found.", new CategorySetting.CategoryCondition(this.generalCategory), true
   );
   public BooleanSetting renderHighlight = new BooleanSetting(
      "RenderHighlight", "Highlights found stash chunks in the world.", new CategorySetting.CategoryCondition(this.generalCategory), true
   );
   public ColorSetting highlightColor = new ColorSetting(
      "HighlightColor",
      "Highlight color for stash chunks.",
      new CategorySetting.CategoryCondition(this.generalCategory),
      new ColorSetting.ColorValue(new Color(0, 230, 120, 100), false, false)
   );
   public CategorySetting webhookCategory = new CategorySetting("Webhook", "Discord webhook settings.");
   public BooleanSetting x = new BooleanSetting(
      "SendWebhook", "Sends stash alerts to Discord webhook.", new CategorySetting.CategoryCondition(this.webhookCategory), true
   );
   public StringSetting y = new StringSetting("WebhookURL", "Discord webhook URL.", new CategorySetting.CategoryCondition(this.webhookCategory), "");
   public StringSetting z = new StringSetting("UserID", "Discord User ID to ping.", new CategorySetting.CategoryCondition(this.webhookCategory), "");
   final StashFinderStorage A = new StashFinderStorage();
   private String B = null;
   final Map<Long, Integer> C = new HashMap<>();
   private static final int D = 25;
   private static final int E = 4;
   private static final int F = 12;
   private static boolean G = false;

   public StashFinder() {
      this.w();
   }

   private void w() {
      if (!G) {
         G = true;

         try {
            Class.forName("net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents");
            StashFinder.ChunkScanListener.a();
         } catch (Throwable var2) {
            Night.LOGGER.warn("Fabric Chunk Events API not available or optional: {}", var2.getMessage());
         }
      }
   }

   @Override
   public void onEnable() {
      this.C.clear();
      this.A.b();
      this.B = StashFinderStorage.a();
      if (mc.player != null && mc.level != null) {
         int var1 = mc.player.blockPosition().getX() >> 4;
         int var2 = mc.player.blockPosition().getZ() >> 4;

         for (int var3 = -12; var3 <= 12; var3++) {
            for (int var4 = -12; var4 <= 12; var4++) {
               int var5 = var1 + var3;
               int var6 = var2 + var4;
               if (mc.level.hasChunk(var5, var6)) {
                  long var7 = StashFinderStorage.a(var5, var6);
                  if (!this.A.a_bool_long(var7)) {
                     this.C.putIfAbsent(var7, 25);
                  }
               }
            }
         }
      }
   }

   @Override
   public void onDisable() {
      this.A.e();
      this.C.clear();
   }

   private void x() {
      String var1 = StashFinderStorage.a();
      if (!var1.equals(this.B)) {
         this.A.b();
         this.B = var1;
         this.C.clear();
      }
   }

   @EventTarget
   public void a_void_TickEvent(TickEvent var1) {
      if (mc.player != null && mc.level != null) {
         this.x();
         this.A.d();
         if (!this.C.isEmpty()) {
            int var2 = 4;
            Iterator var3 = this.C.entrySet().iterator();

            while (var3.hasNext()) {
               Entry var4 = (Entry)var3.next();
               int var5 = (Integer)var4.getValue() - 1;
               if (var5 > 0) {
                  var4.setValue(var5);
               } else if (var2 == 0) {
                  var4.setValue(1);
               } else {
                  var2--;
                  var3.remove();
                  long var6 = (Long)var4.getKey();
                  int var8 = (int)(var6 & 4294967295L);
                  int var9 = (int)(var6 >>> 32);
                  this.a_void_int_int_long(var8, var9, var6);
               }
            }
         }
      }
   }

   @EventTarget
   public void a_void_Render3DEvent(Render3DEvent var1) {
      if (this.renderHighlight.getValue() && mc.level != null) {
         Color var2 = this.highlightColor.getColor();
         Color var3 = new Color(var2.getRed(), var2.getGreen(), var2.getBlue(), 255);

         for (StashFinderStorage.StashEntry var5 : (Iterable<StashFinderStorage.StashEntry>)(Object)this.A.c()) {
            AABB var6 = new AABB(var5.count(), mc.level.getMinY(), var5.count2(), var5.count() + 16, mc.level.getMaxY(), var5.count2() + 16);
            Render3DUtil.a_void_PoseStack_AABB_Color(var1.getMatrices(), var6, var2);
            Render3DUtil.b(var1.getMatrices(), var6, var3);
         }
      }
   }

   public void v() {
      this.A.b();
      this.A.f();
      this.C.clear();
      this.B = StashFinderStorage.a();
   }

   private void a_void_int_int_long(int var1, int var2, long var3) {
      if (mc.level != null && mc.level.hasChunk(var1, var2)) {
         LevelChunk var5 = mc.level.getChunk(var1, var2);
         this.A.b(var3);
         Map var6 = this.a_Map_LevelChunk(var5);
         var6.putAll(this.b(var5));
         if (this.a_bool_Map(var6)) {
            if (!this.ignoreNatural.getValue() || !StructureChunkFilter.a(mc, var5, var6)) {
               LinkedHashMap var7 = new LinkedHashMap();

               for (Entry var9 : (Iterable<Entry>)(Object)var6.entrySet()) {
                  if ((Integer)var9.getValue() > 0) {
                     var7.put((String)var9.getKey(), (Integer)var9.getValue());
                  }
               }

               ChunkPos var16 = var5.getPos();
               int var17 = var16.getMinBlockX();
               int var10 = var16.getMinBlockZ();
               String var11 = mc.level.dimension().identifier().toString();
               this.A.a(var17, var10, var11, var7);
               if (this.chatNotify2.getValue()) {
                  StringBuilder var12 = new StringBuilder();
                  var12.append("Stash found at X: ").append(var17).append(" Z: ").append(var10).append(" (");
                  int var13 = 0;

                  for (Entry var15 : (Iterable<Entry>)(Object)var7.entrySet()) {
                     if (var13++ > 0) {
                        var12.append(", ");
                     }

                     var12.append((String)var15.getKey()).append(": ").append(var15.getValue());
                  }

                  var12.append(")");
                  Night.chatManager.sendTaggedMessage(var12.toString(), "StashFinder");
               }

               if (this.x.getValue()) {
                  String var18 = this.y.getValue();
                  String var19 = this.z.getValue();
                  StashWebhookNotifier.a(var17, var10, var11, var7, var18, var19);
               }
            }
         }
      }
   }

   private boolean a_bool_Map(Map<String, Integer> var1) {
      return this.a_bool_Map_String_NumberSetting(var1, "Chests", this.chests)
         || this.a_bool_Map_String_NumberSetting(var1, "Barrels", this.barrels)
         || this.a_bool_Map_String_NumberSetting(var1, "Shulkers", this.shulkers)
         || this.a_bool_Map_String_NumberSetting(var1, "Ender Chests", this.enderChests)
         || this.a_bool_Map_String_NumberSetting(var1, "Hoppers", this.hoppers)
         || this.a_bool_Map_String_NumberSetting(var1, "Dispensers/Droppers", this.dispensersDroppers)
         || this.a_bool_Map_String_NumberSetting(var1, "Furnaces", this.furnaces)
         || this.a_bool_Map_String_NumberSetting(var1, "Crafters", this.crafters)
         || this.a_bool_Map_String_NumberSetting(var1, "Donkey", this.donkeys)
         || this.a_bool_Map_String_NumberSetting(var1, "Llama", this.llamas)
         || this.a_bool_Map_String_NumberSetting(var1, "Chest Boat", this.chestBoats);
   }

   private boolean a_bool_Map_String_NumberSetting(Map<String, Integer> var1, String var2, NumberSetting var3) {
      int var4 = var3.getValue().intValue();
      return var4 <= 0 ? false : var1.getOrDefault(var2, 0) >= var4;
   }

   private Map<String, Integer> a_Map_LevelChunk(LevelChunk var1) {
      LinkedHashMap var2 = new LinkedHashMap();
      int var3 = 0;
      int var4 = 0;
      int var5 = 0;
      int var6 = 0;
      int var7 = 0;
      int var8 = 0;
      int var9 = 0;
      int var10 = 0;

      for (BlockEntity var12 : (Iterable<BlockEntity>)(Object)var1.getBlockEntities().values()) {
         if (var12 instanceof ChestBlockEntity || var12 instanceof TrappedChestBlockEntity) {
            var3++;
         } else if (var12 instanceof BarrelBlockEntity) {
            var4++;
         } else if (var12 instanceof ShulkerBoxBlockEntity) {
            var5++;
         } else if (var12 instanceof EnderChestBlockEntity) {
            var6++;
         } else if (var12 instanceof HopperBlockEntity) {
            var7++;
         } else if (var12 instanceof DispenserBlockEntity || var12 instanceof DropperBlockEntity) {
            var8++;
         } else if (var12 instanceof FurnaceBlockEntity || var12 instanceof BlastFurnaceBlockEntity || var12 instanceof SmokerBlockEntity) {
            var9++;
         } else if (var12 instanceof CrafterBlockEntity) {
            var10++;
         }
      }

      var2.put("Chests", var3);
      var2.put("Barrels", var4);
      var2.put("Shulkers", var5);
      var2.put("Ender Chests", var6);
      var2.put("Hoppers", var7);
      var2.put("Dispensers/Droppers", var8);
      var2.put("Furnaces", var9);
      var2.put("Crafters", var10);
      return var2;
   }

   private Map<String, Integer> b(LevelChunk var1) {
      LinkedHashMap var2 = new LinkedHashMap();
      int var3 = 0;
      int var4 = 0;
      int var5 = 0;
      if (mc.level == null) {
         return var2;
      }

      ChunkPos var6 = var1.getPos();
      AABB var7 = new AABB(var6.getMinBlockX(), mc.level.getMinY(), var6.getMinBlockZ(), var6.getMaxBlockX() + 1, mc.level.getMaxY(), var6.getMaxBlockZ() + 1);

      for (Entity var9 : (Iterable<Entity>)(Object)mc.level.getEntities((Entity)null, var7, var0 -> true)) {
         if (var9 instanceof Donkey) {
            var3++;
         } else if (var9 instanceof Llama) {
            var4++;
         } else if (var9 instanceof ChestBoat) {
            var5++;
         }
      }

      var2.put("Donkey", var3);
      var2.put("Llama", var4);
      var2.put("Chest Boat", var5);
      return var2;
   }

   static class ChunkScanListener {
      static void a() {
         ClientChunkEvents.CHUNK_LOAD.register((Load)(var0, var1) -> {
            StashFinder var2 = Night.moduleManager != null ? Night.moduleManager.getModule(StashFinder.class) : null;
            if (var2 != null && var2.isEnabled()) {
               if (Minecraft.getInstance().level == var0) {
                  long var3 = StashFinderStorage.a(var1.getPos().x(), var1.getPos().z());
                  if (!var2.A.a_bool_long(var3)) {
                     var2.C.putIfAbsent(var3, 25);
                  }
               }
            }
         });
      }
   }
}
