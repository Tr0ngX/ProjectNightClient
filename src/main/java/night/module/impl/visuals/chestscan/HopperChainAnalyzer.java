package night.module.impl.visuals.chestscan;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class HopperChainAnalyzer {
   public static Map<BlockPos, BlockPos> a(Level var0, Set<BlockPos> var1, BlockPos var2, int var3) {
      HashMap var4 = new HashMap();
      double var5 = var3;
      double var7 = var3;
      double var9 = var5 * var7;
      ArrayDeque var11 = new ArrayDeque(var1);
      HashSet var12 = new HashSet(var1);

      while (!var11.isEmpty()) {
         BlockPos var13 = (BlockPos)var11.poll();
         double var14 = var13.distSqr(var2);
         double var16 = var9;
         if (!(var14 > var16)) {
            BlockPos var18 = var13.below();
            BlockState var19 = var0.getBlockState(var18);
            if (var19.getBlock() instanceof HopperBlock) {
               Direction var20 = (Direction)var19.getValue(HopperBlock.FACING);
               BlockPos var21 = var18;
               Direction var22 = var20;
               BlockPos var23 = var21.relative(var22);
               if (a_bool_BlockState(var0.getBlockState(var23))) {
                  HashMap var24 = var4;
                  BlockPos var25 = var13;
                  BlockPos var26 = var23;
                  var24.put(var25, var26);
                  HashSet var27 = var12;
                  BlockPos var28 = var23;
                  if (var27.add(var28)) {
                     ArrayDeque var29 = var11;
                     BlockPos var30 = var23;
                     var29.add(var30);
                  }
               }
            }

            Direction[] var31 = Direction.values();
            int var32 = var31.length;
            int var33 = 0;

            while (true) {
               int var34 = var33;
               int var35 = var32;
               if (var34 >= var35) {
                  break;
               }

               Direction[] var36 = var31;
               int var37 = var33;
               Direction var38 = var36[var37];
               BlockPos var39 = var13;
               Direction var40 = var38;
               BlockPos var41 = var39.relative(var40);
               BlockState var42 = var0.getBlockState(var41);
               if (var42.getBlock() instanceof HopperBlock) {
                  Comparable var43 = var42.getValue(HopperBlock.FACING);
                  Direction var44 = var38.getOpposite();
                  if (var43 == var44) {
                     BlockPos var45 = var41.above();
                     if (a_bool_BlockState(var0.getBlockState(var45))) {
                        HashMap var46 = var4;
                        BlockPos var47 = var45;
                        BlockPos var48 = var13;
                        var46.put(var47, var48);
                        HashSet var49 = var12;
                        BlockPos var50 = var45;
                        if (var49.add(var50)) {
                           ArrayDeque var51 = var11;
                           BlockPos var52 = var45;
                           var51.add(var52);
                        }
                     }
                  }
               }

               var33++;
            }
         }
      }

      return var4;
   }

   private static boolean a_bool_BlockState(BlockState var0) {
      return var0.getBlock() instanceof ChestBlock;
   }

   public static Set<BlockPos> a(Map<BlockPos, BlockPos> var0, Map<BlockPos, ChestScanStorage.ChestFillState> var1) {
      HashMap var2 = new HashMap();

      for (Entry var4 : (Iterable<Entry>)(Object)var0.entrySet()) {
         HashMap var5 = var2;
         BlockPos var6 = (BlockPos)var4.getValue();
         List var7 = (List)((Map)var5).computeIfAbsent(var6, var0x -> new ArrayList());
         BlockPos var8 = (BlockPos)var4.getKey();
         var7.add(var8);
      }

      HashSet var9 = new HashSet();
      HashSet var10 = new HashSet();

      for (Entry var12 : (Iterable<Entry>)(Object)var1.entrySet()) {
         if (var12.getValue() != ChestScanStorage.ChestFillState.FULL) {
            BlockPos var13 = (BlockPos)var12.getKey();
            HashSet var14 = var10;
            BlockPos var15 = var13;
            if (var14.add(var15)) {
               ArrayDeque var16 = new ArrayDeque();
               BlockPos var17 = var13;
               var16.add(var17);

               while (!var16.isEmpty()) {
                  BlockPos var18 = (BlockPos)var16.poll();
                  HashMap var19 = var2;
                  BlockPos var20 = var18;
                  List var21 = List.of();

                  for (BlockPos var23 : (Iterable<BlockPos>)(Object)var19.getOrDefault(var20, var21)) {
                     HashSet var24 = var10;
                     BlockPos var25 = var23;
                     if (var24.add(var25)) {
                        BlockPos var26 = var23;
                        if (!var1.containsKey(var26)) {
                           HashSet var27 = var9;
                           BlockPos var28 = var23;
                           var27.add(var28);
                        }

                        ArrayDeque var29 = var16;
                        BlockPos var30 = var23;
                        var29.add(var30);
                     }
                  }
               }
            }
         }
      }

      return var9;
   }
}
