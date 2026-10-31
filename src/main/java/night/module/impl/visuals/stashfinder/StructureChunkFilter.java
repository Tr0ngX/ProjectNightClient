package night.module.impl.visuals.stashfinder;

import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.structure.StructureStart;

public class StructureChunkFilter {
   public static boolean a(Minecraft var0, LevelChunk var1, Map<String, Integer> var2) {
      if (var0.level == null) {
         return false;
      }

      Integer var3 = 0;
      int var4 = (Integer)var2.getOrDefault("Shulkers", var3);
      Integer var5 = 0;
      int var6 = (Integer)var2.getOrDefault("Chests", var5);
      Integer var7 = 0;
      int var8 = (Integer)var2.getOrDefault("Hoppers", var7);
      Integer var9 = 0;
      int var10 = (Integer)var2.getOrDefault("Barrels", var9);
      Integer var11 = 0;
      int var12 = (Integer)var2.getOrDefault("Crafters", var11);
      Integer var13 = 0;
      int var14 = (Integer)var2.getOrDefault("Ender Chests", var13);
      Integer var15 = 0;
      int var16 = (Integer)var2.getOrDefault("Donkey", var15);
      Integer var17 = 0;
      int var18 = (Integer)var2.getOrDefault("Chest Boat", var17);
      if (var4 < 8 && var6 < 20 && var8 < 8 && var10 < 16 && var12 < 6 && var14 < 6 && var16 < 4 && var18 < 3) {
         if (var0.hasSingleplayerServer() && var0.getSingleplayerServer() != null) {
            IntegratedServer var19 = var0.getSingleplayerServer();
            ResourceKey var20 = var0.level.dimension();
            ServerLevel var21 = var19.getLevel(var20);
            if (var21 != null) {
               ChunkPos var22 = var1.getPos();
               ServerLevel var23 = var21;
               int var24 = var22.x();
               int var25 = var22.z();
               LevelChunk var26 = var23.getChunk(var24, var25);
               if (var26 != null) {
                  for (Entry var28 : (Iterable<Entry>)(Object)var26.getAllStarts().entrySet()) {
                     if (var28.getValue() != null && ((StructureStart)var28.getValue()).isValid()) {
                        return true;
                     }
                  }

                  for (LongSet var30 : (Iterable<LongSet>)(Object)var26.getAllReferences().values()) {
                     if (var30 != null && !var30.isEmpty()) {
                        return true;
                     }
                  }

                  return false;
               }
            }
         }

         return a(var0, var1);
      } else {
         return false;
      }
   }

   private static boolean a(Minecraft var0, LevelChunk var1) {
      ResourceKey var2;
      if (var0.level != null) {
         var2 = var0.level.dimension();
      } else {
         var2 = Level.OVERWORLD;
      }

      ResourceKey var3 = var2;
      int[] var4 = new int[StructureChunkFilter.StructureRule.values().length];
      LevelChunkSection[] var5 = var1.getSections();
      int var6 = 0;

      while (true) {
         int var7 = var6;
         int var8 = var5.length;
         if (var7 >= var8) {
            StructureChunkFilter.StructureRule[] var44 = StructureChunkFilter.StructureRule.values();
            int var45 = var44.length;
            int var46 = 0;

            while (true) {
               int var47 = var46;
               int var48 = var45;
               if (var47 >= var48) {
                  return false;
               }

               label57: {
                  StructureChunkFilter.StructureRule[] var49 = var44;
                  int var50 = var46;
                  StructureChunkFilter.StructureRule var51 = var49[var50];
                  if (var51.t != null) {
                     ResourceKey var52 = var51.t;
                     ResourceKey var53 = var3;
                     if (var52 != var53) {
                        break label57;
                     }
                  }

                  int[] var54 = var4;
                  int var55 = var51.ordinal();
                  int var56 = var54[var55];
                  int var57 = var51.r;
                  if (var56 >= var57) {
                     if (var51.s == null) {
                        break;
                     }

                     StructureChunkFilter.StructureRule var58 = var51;
                     if (a(var1, var58)) {
                        break;
                     }
                  }
               }

               var46++;
            }

            return true;
         }

         LevelChunkSection[] var9 = var5;
         int var10 = var6;
         LevelChunkSection var11 = var9[var10];
         if (var11 != null && !var11.hasOnlyAir()) {
            int var12 = var6;
            int var13 = var1.getSectionYFromSectionIndex(var12) << 4;

            for (int var14 = 0; var14 < 16; var14++) {
               int var15 = var13;
               int var16 = var14;
               int var17 = var15 + var16;

               for (int var18 = 0; var18 < 16; var18++) {
                  for (int var19 = 0; var19 < 16; var19++) {
                     LevelChunkSection var20 = var11;
                     int var21 = var18;
                     int var22 = var14;
                     int var23 = var19;
                     Block var24 = var20.getBlockState(var21, var22, var23).getBlock();
                     StructureChunkFilter.StructureRule[] var25 = StructureChunkFilter.StructureRule.values();
                     int var26 = var25.length;
                     int var27 = 0;

                     while (true) {
                        int var28 = var27;
                        int var29 = var26;
                        if (var28 >= var29) {
                           break;
                        }

                        label84: {
                           StructureChunkFilter.StructureRule[] var30 = var25;
                           int var31 = var27;
                           StructureChunkFilter.StructureRule var32 = var30[var31];
                           if (var32.t != null) {
                              ResourceKey var33 = var32.t;
                              ResourceKey var34 = var3;
                              if (var33 != var34) {
                                 break label84;
                              }
                           }

                           int var35 = var17;
                           int var36 = var32.u;
                           if (var35 >= var36) {
                              int var37 = var17;
                              int var38 = var32.v;
                              if (var37 <= var38) {
                                 Set var39 = var32.q;
                                 Block var40 = var24;
                                 if (var39.contains(var40)) {
                                    int[] var41 = var4;
                                    int var42 = var32.ordinal();
                                    int var43 = var41[var42] + 1;
                                    var41[var42] = var43;
                                 }
                              }
                           }
                        }

                        var27++;
                     }
                  }
               }
            }
         }

         var6++;
      }
   }

   private static boolean a(LevelChunk var0, StructureChunkFilter.StructureRule var1) {
      if (var1.s == null) {
         return true;
      }

      ChunkPos var2 = var0.getPos();
      int var3 = var1.u;
      int var4 = var1.v;
      long var5 = (var3 + var4) / 2;
      int var7 = var0.getMinY();
      int var8 = var0.getMaxY();
      int var9 = Math.clamp(var5, var7, var8) >> 2;
      int var10 = var2.getMinBlockX() >> 2;
      int var11 = var2.getMinBlockZ() >> 2;
      int[][] var12 = new int[5][];
      int[] var13 = new int[]{0, 0};
      var12[0] = var13;
      int[] var14 = new int[]{3, 0};
      var12[1] = var14;
      int[] var15 = new int[]{0, 3};
      var12[2] = var15;
      int[] var16 = new int[]{3, 3};
      var12[3] = var16;
      int[] var17 = new int[]{2, 2};
      var12[4] = var17;
      int[][] var18 = var12;
      int var19 = var18.length;
      int var20 = 0;

      while (true) {
         int var21 = var20;
         int var22 = var19;
         if (var21 >= var22) {
            return false;
         }

         int[][] var23 = var18;
         int var24 = var20;
         int[] var25 = var23[var24];
         int var26 = var10;
         int var27 = var25[0];
         int var28 = var26 + var27;
         int var29 = var9;
         int var30 = var11;
         int var31 = var25[1];
         int var32 = var30 + var31;
         Holder var33 = var0.getNoiseBiome(var28, var29, var32);
         if (var33 != null) {
            Holder var34 = var33;
            TagKey var35 = var1.s;
            if (var34.is(var35)) {
               return true;
            }
         }

         var20++;
      }
   }

   enum StructureRule {
      TRIAL_CHAMBERS(
         Set.of(
            Blocks.VAULT,
            Blocks.TRIAL_SPAWNER,
            Blocks.HEAVY_CORE,
            (Block)Blocks.CHISELED_COPPER.weathering().unaffected(),
            (Block)Blocks.COPPER_GRATE.weathering().unaffected(),
            Blocks.TUFF_BRICKS,
            Blocks.CHISELED_TUFF,
            Blocks.POLISHED_TUFF
         ),
         2,
         BiomeTags.HAS_TRIAL_CHAMBERS,
         Level.OVERWORLD,
         -60,
         20
      ),
      ANCIENT_CITY(
         Set.of(Blocks.REINFORCED_DEEPSLATE, Blocks.SCULK_CATALYST, Blocks.SCULK_SHRIEKER, Blocks.SCULK_SENSOR, Blocks.SOUL_LANTERN, Blocks.SCULK),
         8,
         BiomeTags.HAS_ANCIENT_CITY,
         Level.OVERWORLD,
         -64,
         -10
      ),
      END_CITY(
         Set.of(Blocks.PURPUR_BLOCK, Blocks.PURPUR_PILLAR, Blocks.PURPUR_STAIRS, Blocks.END_STONE_BRICKS, Blocks.END_ROD),
         15,
         BiomeTags.HAS_END_CITY,
         Level.END,
         40,
         255
      ),
      NETHER_FORTRESS(
         Set.of(Blocks.NETHER_BRICKS, Blocks.NETHER_BRICK_FENCE, Blocks.NETHER_BRICK_STAIRS, Blocks.NETHER_WART),
         25,
         BiomeTags.HAS_NETHER_FORTRESS,
         Level.NETHER,
         30,
         110
      ),
      BASTION_REMNANT(
         Set.of(
            Blocks.GILDED_BLACKSTONE,
            Blocks.POLISHED_BLACKSTONE_BRICKS,
            Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS,
            Blocks.CHISELED_POLISHED_BLACKSTONE,
            Blocks.CRYING_OBSIDIAN,
            Blocks.BLACKSTONE
         ),
         25,
         BiomeTags.HAS_BASTION_REMNANT,
         Level.NETHER,
         0,
         128
      ),
      OCEAN_MONUMENT(
         Set.of(Blocks.PRISMARINE, Blocks.PRISMARINE_BRICKS, Blocks.DARK_PRISMARINE, Blocks.SEA_LANTERN, Blocks.WET_SPONGE),
         35,
         BiomeTags.HAS_OCEAN_MONUMENT,
         Level.OVERWORLD,
         30,
         70
      ),
      WOODLAND_MANSION(Set.of(Blocks.DARK_OAK_PLANKS, Blocks.DARK_OAK_LOG, Blocks.COBBLESTONE), 120, BiomeTags.HAS_WOODLAND_MANSION, Level.OVERWORLD, 50, 150),
      DESERT_PYRAMID(
         Set.of(
            (Block)Blocks.DYED_TERRACOTTA.pick(DyeColor.ORANGE),
            (Block)Blocks.DYED_TERRACOTTA.pick(DyeColor.BLUE),
            Blocks.CUT_SANDSTONE,
            Blocks.CHISELED_SANDSTONE
         ),
         6,
         BiomeTags.HAS_DESERT_PYRAMID,
         Level.OVERWORLD,
         40,
         100
      ),
      JUNGLE_TEMPLE(
         Set.of(Blocks.MOSSY_COBBLESTONE, Blocks.CHISELED_STONE_BRICKS, Blocks.TRIPWIRE_HOOK, Blocks.LEVER, Blocks.STICKY_PISTON),
         8,
         BiomeTags.HAS_JUNGLE_TEMPLE,
         Level.OVERWORLD,
         50,
         100
      ),
      VILLAGE(Set.of(Blocks.BELL, Blocks.COMPOSTER, Blocks.DIRT_PATH, Blocks.HAY_BLOCK, Blocks.FARMLAND), 8, null, Level.OVERWORLD, 50, 150),
      MINESHAFT(
         Set.of(
            Blocks.RAIL,
            Blocks.POWERED_RAIL,
            Blocks.DETECTOR_RAIL,
            Blocks.ACTIVATOR_RAIL,
            Blocks.COBWEB,
            Blocks.OAK_FENCE,
            Blocks.DARK_OAK_FENCE,
            Blocks.SPRUCE_FENCE
         ),
         15,
         BiomeTags.HAS_MINESHAFT,
         Level.OVERWORLD,
         -60,
         60
      ),
      STRONGHOLD(
         Set.of(
            Blocks.END_PORTAL_FRAME,
            Blocks.INFESTED_STONE_BRICKS,
            Blocks.INFESTED_COBBLESTONE,
            Blocks.INFESTED_CRACKED_STONE_BRICKS,
            Blocks.INFESTED_MOSSY_STONE_BRICKS,
            Blocks.IRON_BARS
         ),
         12,
         BiomeTags.HAS_STRONGHOLD,
         Level.OVERWORLD,
         -60,
         40
      ),
      SHIPWRECK(
         Set.of(
            Blocks.STRIPPED_OAK_LOG,
            Blocks.STRIPPED_SPRUCE_LOG,
            Blocks.STRIPPED_DARK_OAK_LOG,
            Blocks.STRIPPED_JUNGLE_LOG,
            Blocks.STRIPPED_BIRCH_LOG,
            Blocks.STRIPPED_ACACIA_LOG,
            Blocks.STRIPPED_MANGROVE_LOG,
            Blocks.STRIPPED_CHERRY_LOG
         ),
         15,
         BiomeTags.HAS_SHIPWRECK,
         Level.OVERWORLD,
         20,
         80
      ),
      PILLAGER_OUTPOST(Set.of(Blocks.DARK_OAK_LOG, Blocks.DARK_OAK_PLANKS, Blocks.BIRCH_PLANKS, Blocks.TARGET), 30, null, Level.OVERWORLD, 50, 150),
      DUNGEON(Set.of(Blocks.SPAWNER, Blocks.MOSSY_COBBLESTONE), 6, null, Level.OVERWORLD, -60, 60),
      RUINED_PORTAL(Set.of(Blocks.CRYING_OBSIDIAN, Blocks.MAGMA_BLOCK, Blocks.GOLD_BLOCK), 4, BiomeTags.HAS_RUINED_PORTAL_STANDARD, null, 30, 120);

      final Set<Block> q;
      final int r;
      final TagKey<Biome> s;
      final ResourceKey<Level> t;
      final int u;
      final int v;

      StructureRule(Set<Block> var3, int var4, TagKey<Biome> var5, ResourceKey<Level> var6, int var7, int var8) {
         this.q = var3;
         this.r = var4;
         this.s = var5;
         this.t = var6;
         this.u = var7;
         this.v = var8;
      }
   }
}
