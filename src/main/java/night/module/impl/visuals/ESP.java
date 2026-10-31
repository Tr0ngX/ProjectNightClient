package night.module.impl.visuals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents.Load;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents.Unload;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import night.Night;
import night.events.EventTarget;
import night.events.impl.Render3DEvent;
import night.events.impl.TickEvent;
import night.module.Module;
import night.module.ModuleInfo;
import night.setting.impl.BooleanSetting;
import night.setting.impl.ColorSetting;
import night.setting.impl.ModeSetting;
import night.setting.impl.WhitelistSetting;
import night.util.color.ColorUtil;
import night.util.render.Render3DUtil;
import night.util.world.EntityUtil;

@ModuleInfo(name = "ESP", description = "Renders a box ESP around entities that you have selected.", category = Module.Category.VISUALS)
public class ESP extends Module {
   public BooleanSetting players = new BooleanSetting("Players", "Renders the box ESP on player entities.", true);
   public BooleanSetting hostiles = new BooleanSetting("Hostiles", "Renders the box ESP on hostile entities.", true);
   public BooleanSetting animals = new BooleanSetting("Animals", "Renders the box ESP on animal entities.", true);
   public BooleanSetting ambient = new BooleanSetting("Ambient", "Renders the box ESP on ambient entities.", false);
   public BooleanSetting invisibles = new BooleanSetting("Invisibles", "Renders the box ESP on invisible entities.", true);
   public BooleanSetting items = new BooleanSetting("Items", "Renders the box ESP on item entities.", true);
   public BooleanSetting others = new BooleanSetting("Others", "Renders the box ESP on miscellaneous entities.", false);
   public BooleanSetting block = new BooleanSetting("Block", "Renders a box ESP on whitelisted blocks (portals, ores, ...).", false);
   public WhitelistSetting whitelist = new WhitelistSetting(
      "Whitelist", "Blocks to highlight.", new BooleanSetting.BooleanCondition(this.block, true), WhitelistSetting.WhitelistType.BLOCKS
   );
   public ModeSetting mode = new ModeSetting(
      "Mode", "The rendering that will be applied to the target entities.", "Both", new String[]{"None", "Fill", "Outline", "Both"}
   );
   public ColorSetting fillColor = new ColorSetting(
      "FillColor", "The color that will be used for the fill rendering.", new ModeSetting.ModeCondition(this.mode, "Fill", "Both"), ColorUtil.b()
   );
   public ColorSetting outlineColor = new ColorSetting(
      "OutlineColor", "The color that will be used for the outline rendering.", new ModeSetting.ModeCondition(this.mode, "Outline", "Both"), ColorUtil.c()
   );
   private List<Entity> q = new ArrayList<>();
   final Map<Long, List<BlockPos>> r = new HashMap<>();
   final Map<Long, Integer> s = new HashMap<>();
   private static final int t = 5;
   private static final int u = 2;
   private static boolean v = false;
   private Predicate<BlockState> w = var0 -> false;
   private String x = "";
   private boolean y = false;

   public ESP() {
      this.v();
   }

   private void v() {
      if (!v) {
         v = true;

         try {
            Class.forName("net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents");
            ESP.ChunkListener.a();
         } catch (Throwable var2) {
            Night.LOGGER.warn("Fabric Chunk Events API not available or optional: {}", var2.getMessage());
         }
      }
   }

   static long a_long_int_int(int var0, int var1) {
      return var0 & 4294967295L | (long)var1 << 32;
   }

   void b(int var1, int var2) {
      this.s.putIfAbsent(a_long_int_int(var1, var2), 5);
   }

   @Override
   public void onEnable() {
      this.r.clear();
      this.s.clear();
      this.y = this.block.getValue();
      if (this.block.getValue()) {
         this.x();
      }
   }

   @Override
   public void onDisable() {
      this.r.clear();
      this.s.clear();
      this.y = false;
   }

   private String w() {
      return String.join(",", this.whitelist.getEntryIds());
   }

   private void x() {
      this.x = this.w();
      this.w = var1x -> !var1x.isAir() && this.whitelist.contains(var1x.getBlock());
      this.r.clear();
      this.s.clear();
      if (mc.player != null && mc.level != null) {
         int var1 = mc.player.blockPosition().getX() >> 4;
         int var2 = mc.player.blockPosition().getZ() >> 4;
         int var3 = mc.options.getEffectiveRenderDistance();

         for (int var4 = -var3; var4 <= var3; var4++) {
            for (int var5 = -var3; var5 <= var3; var5++) {
               int var6 = var1 + var4;
               int var7 = var2 + var5;
               if (mc.level.hasChunk(var6, var7)) {
                  this.b(var6, var7);
               }
            }
         }
      }
   }

   @EventTarget
   public void a_void_TickEvent(TickEvent var1) {
      if (mc.player != null && mc.level != null) {
         if (!this.block.getValue()) {
            if (this.y) {
               this.y = false;
               this.r.clear();
               this.s.clear();
            }
         } else if (!this.y) {
            this.y = true;
            this.x();
         } else if (!this.w().equals(this.x)) {
            this.x();
         } else if (!this.s.isEmpty()) {
            int var2 = 2;
            Iterator var3 = this.s.entrySet().iterator();

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
                  int var8 = (int)var6;
                  int var9 = (int)(var6 >>> 32);
                  this.a(var8, var9, var6);
               }
            }
         }
      }
   }

   private void a(int var1, int var2, long var3) {
      if (mc.level != null && mc.level.hasChunk(var1, var2)) {
         LevelChunk var5 = mc.level.getChunk(var1, var2);
         ArrayList var6 = new ArrayList();
         LevelChunkSection[] var7 = var5.getSections();
         int var8 = mc.level.getMinSectionY();

         for (int var9 = 0; var9 < var7.length; var9++) {
            LevelChunkSection var10 = var7[var9];
            if (var10 != null && !var10.hasOnlyAir() && var10.maybeHas(this.w)) {
               int var11 = var8 + var9 << 4;
               int var12 = var1 << 4;
               int var13 = var2 << 4;

               for (int var14 = 0; var14 < 16; var14++) {
                  for (int var15 = 0; var15 < 16; var15++) {
                     for (int var16 = 0; var16 < 16; var16++) {
                        BlockState var17 = var10.getBlockState(var14, var15, var16);
                        if (this.w.test(var17)) {
                           var6.add(new BlockPos(var12 + var14, var11 + var15, var13 + var16));
                        }
                     }
                  }
               }
            }
         }

         if (var6.isEmpty()) {
            this.r.remove(var3);
         } else {
            this.r.put(var3, var6);
         }
      }
   }

   @EventTarget
   public void b_void_TickEvent(TickEvent var1) {
      if (mc.level != null) {
         ArrayList var2 = new ArrayList();

         for (Entity var4 : (Iterable<Entity>)(Object)mc.level.entitiesForRendering()) {
            if (var4 != mc.player && this.a_bool_Entity(var4)) {
               var2.add(var4);
            }
         }

         this.q = var2;
      }
   }

   @EventTarget
   public void a_void_Render3DEvent(Render3DEvent var1) {
      if (mc.level != null) {
         if (!this.q.isEmpty()) {
            for (Entity var3 : (Iterable<Entity>)(Object)this.q) {
               Vec3 var4 = EntityUtil.a_Vec3_Entity_float(var3, var1.getTickDelta());
               AABB var5 = new AABB(
                  var4.x - var3.getBoundingBox().getXsize() / 2.0,
                  var4.y,
                  var4.z - var3.getBoundingBox().getZsize() / 2.0,
                  var4.x + var3.getBoundingBox().getXsize() / 2.0,
                  var4.y + var3.getBoundingBox().getYsize(),
                  var4.z + var3.getBoundingBox().getZsize() / 2.0
               );
               if (this.mode.getValue().equalsIgnoreCase("Fill") || this.mode.getValue().equalsIgnoreCase("Both")) {
                  Render3DUtil.a_void_PoseStack_AABB_Color(var1.getMatrices(), var5, this.fillColor.getColor());
               }

               if (this.mode.getValue().equalsIgnoreCase("Outline") || this.mode.getValue().equalsIgnoreCase("Both")) {
                  Render3DUtil.b(var1.getMatrices(), var5, this.outlineColor.getColor());
               }
            }
         }

         this.b_void_Render3DEvent(var1);
      }
   }

   private void b_void_Render3DEvent(Render3DEvent var1) {
      if (mc.level != null && this.block.getValue() && !this.r.isEmpty()) {
         if (!this.mode.getValue().equalsIgnoreCase("None")) {
            boolean var2 = this.mode.getValue().equalsIgnoreCase("Fill") || this.mode.getValue().equalsIgnoreCase("Both");
            boolean var3 = this.mode.getValue().equalsIgnoreCase("Outline") || this.mode.getValue().equalsIgnoreCase("Both");
            if (var2 || var3) {
               Iterator var4 = this.r.values().iterator();

               while (var4.hasNext()) {
                  for (BlockPos var6 : (Iterable<BlockPos>)(Object)(List)var4.next()) {
                     BlockState var7 = mc.level.getBlockState(var6);
                     if (this.w.test(var7)) {
                        VoxelShape var8 = var7.getShape(mc.level, var6);

                        for (AABB var10 : var8.isEmpty() ? List.of(new AABB(var6)) : var8.toAabbs().stream().map(var1x -> var1x.move(var6)).toList()) {
                           if (var2) {
                              Render3DUtil.a_void_PoseStack_AABB_Color(var1.getMatrices(), var10, this.fillColor.getColor());
                           }

                           if (var3) {
                              Render3DUtil.b(var1.getMatrices(), var10, this.outlineColor.getColor());
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private boolean a_bool_Entity(Entity var1) {
      if (this.players.getValue() && var1.getType() == EntityTypes.PLAYER) {
         return true;
      } else if (this.hostiles.getValue() && EntityUtil.g(var1)) {
         return true;
      } else if (this.animals.getValue() && EntityUtil.h(var1) && !EntityUtil.g(var1)) {
         return true;
      } else if (this.ambient.getValue() && var1.getType().getCategory() == MobCategory.AMBIENT) {
         return true;
      } else if (this.invisibles.getValue() && var1.isInvisible()) {
         return true;
      } else {
         return !this.items.getValue() || var1.getType() != EntityTypes.ITEM && var1.getType() != EntityTypes.EXPERIENCE_BOTTLE ? this.others.getValue() : true;
      }
   }

   static class ChunkListener {
      static void a() {
         ClientChunkEvents.CHUNK_LOAD.register((Load)(var0, var1) -> {
            ESP var2 = Night.moduleManager != null ? Night.moduleManager.getModule(ESP.class) : null;
            if (var2 != null && var2.isEnabled() && var2.block.getValue()) {
               if (Minecraft.getInstance().level == var0) {
                  var2.b(var1.getPos().x(), var1.getPos().z());
               }
            }
         });
         ClientChunkEvents.CHUNK_UNLOAD.register((Unload)(var0, var1) -> {
            ESP var2 = Night.moduleManager != null ? Night.moduleManager.getModule(ESP.class) : null;
            if (var2 != null) {
               long var3 = ESP.a_long_int_int(var1.getPos().x(), var1.getPos().z());
               var2.r.remove(var3);
               var2.s.remove(var3);
            }
         });
      }
   }
}
