package night.module.impl.visuals;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import night.events.EventTarget;
import night.events.impl.Render3DEvent;
import night.events.impl.UpdateWalkingPlayerEvent;
import night.module.Module;
import night.module.ModuleInfo;
import night.module.impl.visuals.chestscan.ChestScanStorage;
import night.module.impl.visuals.chestscan.HopperChainAnalyzer;
import night.setting.impl.BooleanSetting;
import night.setting.impl.CategorySetting;
import night.setting.impl.ColorSetting;
import night.setting.impl.ModeSetting;
import night.setting.impl.NumberSetting;
import night.util.render.Render3DUtil;

@ModuleInfo(
   name = "ChestScan",
   description = "Highlights opened chests by contents (empty/partial/full), with optional hopper-chain inference.",
   category = Module.Category.VISUALS
)
public class ChestScan extends Module {
   public NumberSetting scanRadius = new NumberSetting(
      "ScanRadius", "How far (in blocks) to render tracked chests and consider hopper chains.", 64.0, 8.0, 128.0
   );
   public BooleanSetting hopperChain = new BooleanSetting("HopperChain", "Smart mode to check chests linked to the bottom chest by hoppers.", false);
   public ModeSetting mode = new ModeSetting("Mode", "Render mode for chests.", "Both", new String[]{"Both", "Fill", "Outline"});
   public CategorySetting emptyCategory = new CategorySetting("Empty", "Color for empty chests.");
   public ColorSetting emptyFillColor = new ColorSetting(
      "EmptyFillColor",
      "Fill",
      "Fill color for empty chests.",
      new CategorySetting.CategoryCondition(this.emptyCategory),
      new ColorSetting.ColorValue(new Color(0, 200, 0, 120), false, false)
   );
   public ColorSetting emptyOutlineColor = new ColorSetting(
      "EmptyOutlineColor",
      "Outline",
      "Outline color for empty chests.",
      new CategorySetting.CategoryCondition(this.emptyCategory),
      new ColorSetting.ColorValue(new Color(0, 200, 0, 255), false, false)
   );
   public CategorySetting partialCategory = new CategorySetting("Partial", "Color for partially filled chests.");
   public ColorSetting partialFillColor = new ColorSetting(
      "PartialFillColor",
      "Fill",
      "Fill color for partially filled chests.",
      new CategorySetting.CategoryCondition(this.partialCategory),
      new ColorSetting.ColorValue(new Color(230, 200, 0, 120), false, false)
   );
   public ColorSetting partialOutlineColor = new ColorSetting(
      "PartialOutlineColor",
      "Outline",
      "Outline color for partially filled chests.",
      new CategorySetting.CategoryCondition(this.partialCategory),
      new ColorSetting.ColorValue(new Color(230, 200, 0, 255), false, false)
   );
   public CategorySetting fullCategory = new CategorySetting("Full", "Color for full chests.");
   public ColorSetting fullFillColor = new ColorSetting(
      "FullFillColor",
      "Fill",
      "Fill color for full chests.",
      new CategorySetting.CategoryCondition(this.fullCategory),
      new ColorSetting.ColorValue(new Color(220, 0, 0, 120), false, false)
   );
   public ColorSetting fullOutlineColor = new ColorSetting(
      "FullOutlineColor",
      "Outline",
      "Outline color for full chests.",
      new CategorySetting.CategoryCondition(this.fullCategory),
      new ColorSetting.ColorValue(new Color(220, 0, 0, 255), false, false)
   );
   private final ChestScanStorage q = new ChestScanStorage();
   private String r = null;
   private BlockPos s = null;
   private boolean t = false;
   private BlockPos u = null;
   private ChestScanStorage.ChestFillState v = null;
   private int w = 0;
   private Set<BlockPos> x = Collections.emptySet();
   private final Map<BlockPos, Long> y = new HashMap<>();
   private static final long z = 1500L;

   public ChestScanStorage v() {
      return this.q;
   }

   @Override
   public void onEnable() {
      this.q.b();
      this.r = ChestScanStorage.a();
   }

   @Override
   public void onDisable() {
      if (this.u != null && this.v != null) {
         this.a(mc, this.u, this.v);
      }

      this.r = null;
      this.s = null;
      this.t = false;
      this.u = null;
      this.v = null;
      this.y.clear();
   }

   private void w() {
      String var1 = ChestScanStorage.a();
      if (!var1.equals(this.r)) {
         this.q.b();
         this.r = var1;
         this.y.clear();
      }
   }

   @EventTarget
   public void a_void_UpdateWalkingPlayerEvent(UpdateWalkingPlayerEvent var1) {
      if (mc.player != null && mc.level != null) {
         this.w();
         boolean var2 = mc.player.containerMenu instanceof ChestMenu;
         if (!var2 && mc.gui.screen() == null) {
            this.s = this.b_BlockPos_Minecraft(mc);
         }

         if (var2 && !this.t) {
            this.u = this.s;
            this.v = null;
         }

         if (var2) {
            ChestMenu var3 = (ChestMenu)mc.player.containerMenu;
            this.v = this.a_ChestFillState_Container(var3.getContainer());
         }

         if (!var2 && this.t) {
            this.a(mc, this.u, this.v);
            this.u = null;
            this.v = null;
         }

         this.t = var2;
         this.a_void_Minecraft(mc);
      }
   }

   private void a_void_Minecraft(Minecraft var1) {
      if (!this.hopperChain.getValue()) {
         this.x = Collections.emptySet();
      } else {
         this.w++;
         if (this.w >= 20) {
            this.w = 0;
            BlockPos var2 = var1.player.blockPosition();
            int var3 = this.scanRadius.getValue().intValue();
            HashSet var4 = new HashSet<>(this.q.c());
            Map var5 = HopperChainAnalyzer.a(var1.level, var4, var2, var3);
            HashMap var6 = new HashMap();

            for (BlockPos var8 : (Iterable<BlockPos>)(Object)var4) {
               var6.put(var8, this.q.a_ChestFillState_BlockPos(var8));
            }

            this.x = HopperChainAnalyzer.a(var5, var6);
         }
      }
   }

   @EventTarget
   public void a_void_Render3DEvent(Render3DEvent var1) {
      if (mc.player != null && mc.level != null) {
         double var2 = this.scanRadius.getValue().doubleValue();
         double var4 = var2 * var2;
         BlockPos var6 = mc.player.blockPosition();
         boolean var7 = this.mode.getValue().equalsIgnoreCase("Fill") || this.mode.getValue().equalsIgnoreCase("Both");
         boolean var8 = this.mode.getValue().equalsIgnoreCase("Outline") || this.mode.getValue().equalsIgnoreCase("Both");
         HashSet var9 = new HashSet();

         for (BlockPos var11 : new ArrayList<>(this.q.c())) {
            if (!(var6.distSqr(var11) > var4) && mc.level.hasChunk(var11.getX() >> 4, var11.getZ() >> 4)) {
               if (!(mc.level.getBlockState(var11).getBlock() instanceof ChestBlock)) {
                  long var12 = System.currentTimeMillis();
                  Long var14 = this.y.putIfAbsent(var11, var12);
                  if (var14 != null && var12 - var14 >= 1500L) {
                     this.q.b(var11);
                     this.y.remove(var11);
                  }
               } else {
                  this.y.remove(var11);
                  if (!var9.contains(var11)) {
                     ChestScanStorage.ChestFillState var19 = this.q.a_ChestFillState_BlockPos(var11);
                     if (var19 != null) {
                        AABB var15 = this.a(var11, var9);
                        Color var21 = this.a_Color_ChestFillState(var19);
                        Color var16 = this.b_Color_ChestFillState(var19);
                        if (var7) {
                           Render3DUtil.a_void_PoseStack_AABB_Color(var1.getMatrices(), var15, var21);
                        }

                        if (var8) {
                           Render3DUtil.b(var1.getMatrices(), var15, var16);
                        }
                     }
                  }
               }
            }
         }

         if (this.hopperChain.getValue()) {
            Color var17 = this.emptyFillColor.getColor();
            Color var18 = this.emptyOutlineColor.getColor();

            for (BlockPos var23 : (Iterable<BlockPos>)(Object)this.x) {
               if (this.q.a_ChestFillState_BlockPos(var23) == null
                  && !(var6.distSqr(var23) > var4)
                  && mc.level.hasChunk(var23.getX() >> 4, var23.getZ() >> 4)
                  && !var9.contains(var23)) {
                  BlockState var22 = mc.level.getBlockState(var23);
                  if (var22.getBlock() instanceof ChestBlock) {
                     AABB var24 = this.a(var23, var9);
                     if (var7) {
                        Render3DUtil.a_void_PoseStack_AABB_Color(var1.getMatrices(), var24, var17);
                     }

                     if (var8) {
                        Render3DUtil.b(var1.getMatrices(), var24, var18);
                     }
                  }
               }
            }
         }
      }
   }

   private AABB a(BlockPos var1, Set<BlockPos> var2) {
      var2.add(var1);
      BlockState var3 = mc.level.getBlockState(var1);
      if (var3.getBlock() instanceof ChestBlock && var3.hasProperty(ChestBlock.TYPE) && var3.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
         Direction var4 = (Direction)var3.getValue(ChestBlock.FACING);
         Direction var5 = (ChestType)var3.getValue(ChestBlock.TYPE) == ChestType.LEFT ? var4.getClockWise() : var4.getCounterClockWise();
         BlockPos var6 = var1.relative(var5);
         BlockState var7 = mc.level.getBlockState(var6);
         if (var7.getBlock() instanceof ChestBlock && var7.hasProperty(ChestBlock.TYPE) && var7.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
            var2.add(var6);
            return new AABB(
               Math.min(var1.getX(), var6.getX()),
               Math.min(var1.getY(), var6.getY()),
               Math.min(var1.getZ(), var6.getZ()),
               Math.max(var1.getX(), var6.getX()) + 1.0,
               Math.max(var1.getY(), var6.getY()) + 1.0,
               Math.max(var1.getZ(), var6.getZ()) + 1.0
            );
         }
      }

      return new AABB(var1);
   }

   private Color a_Color_ChestFillState(ChestScanStorage.ChestFillState var1) {
      return switch (var1) {
         case EMPTY -> this.emptyFillColor.getColor();
         case PARTIAL -> this.partialFillColor.getColor();
         case FULL -> this.fullFillColor.getColor();
      };
   }

   private Color b_Color_ChestFillState(ChestScanStorage.ChestFillState var1) {
      return switch (var1) {
         case EMPTY -> this.emptyOutlineColor.getColor();
         case PARTIAL -> this.partialOutlineColor.getColor();
         case FULL -> this.fullOutlineColor.getColor();
      };
   }

   private BlockPos b_BlockPos_Minecraft(Minecraft var1) {
      if (var1.hitResult instanceof BlockHitResult var3 && var3.getType() == Type.BLOCK) {
         BlockPos var4 = var3.getBlockPos();
         return var1.level.getBlockState(var4).getBlock() instanceof ChestBlock ? var4 : null;
      } else {
         return null;
      }
   }

   private ChestScanStorage.ChestFillState a_ChestFillState_Container(Container var1) {
      int var2 = var1.getContainerSize();
      if (var2 == 0) {
         return ChestScanStorage.ChestFillState.EMPTY;
      }

      int var3 = 0;

      for (int var4 = 0; var4 < var2; var4++) {
         if (!var1.getItem(var4).isEmpty()) {
            var3++;
         }
      }

      if (var3 == 0) {
         return ChestScanStorage.ChestFillState.EMPTY;
      } else {
         return var3 == var2 ? ChestScanStorage.ChestFillState.FULL : ChestScanStorage.ChestFillState.PARTIAL;
      }
   }

   private void a(Minecraft var1, BlockPos var2, ChestScanStorage.ChestFillState var3) {
      if (var2 != null && var3 != null) {
         this.q.a(var2, var3);
         if (var1 != null && var1.level != null) {
            BlockState var4 = var1.level.getBlockState(var2);
            if (var4.getBlock() instanceof ChestBlock && var4.hasProperty(ChestBlock.TYPE) && var4.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
               Direction var5 = (Direction)var4.getValue(ChestBlock.FACING);
               Direction var6 = (ChestType)var4.getValue(ChestBlock.TYPE) == ChestType.LEFT ? var5.getClockWise() : var5.getCounterClockWise();
               BlockPos var7 = var2.relative(var6);
               if (var1.level.getBlockState(var7).getBlock() instanceof ChestBlock) {
                  this.q.a(var7, var3);
               }
            }
         }
      }
   }
}
