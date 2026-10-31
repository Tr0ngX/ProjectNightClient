package night.module.impl.visuals;

import java.awt.Color;
import java.util.ArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import night.events.EventTarget;
import night.events.impl.Render3DEvent;
import night.events.impl.TickEvent;
import night.module.Module;
import night.module.ModuleInfo;
import night.setting.impl.BooleanSetting;
import night.setting.impl.CategorySetting;
import night.setting.impl.ColorSetting;
import night.setting.impl.ModeSetting;
import night.setting.impl.NumberSetting;
import night.util.color.ColorUtil;
import night.util.render.Render3DUtil;
import night.util.world.PositionUtil;

@ModuleInfo(name = "PhaseESP", description = "Renders ESP on blocks which have a safe base.", category = Module.Category.VISUALS)
public class PhaseESP extends Module {
   public ModeSetting fill = new ModeSetting("Fill", "The mode for the fill rendering on the phase blocks.", "Normal", new String[]{"None", "Normal"});
   public NumberSetting fillHeight = new NumberSetting(
      "FillHeight", "The height of the fill rendering on the phase blocks.", new ModeSetting.ModeCondition(this.fill, "Normal"), 1.0, -2.0, 2.0
   );
   public ModeSetting outline = new ModeSetting("Outline", "The mode for the outline rendering on the phase blocks.", "Normal", new String[]{"None", "Normal"});
   public NumberSetting outlineHeight = new NumberSetting(
      "OutlineHeight", "The height of the outline rendering on the phase blocks.", new ModeSetting.ModeCondition(this.outline, "Normal"), 1.0, -2.0, 2.0
   );
   public CategorySetting bedrockCategory = new CategorySetting("Bedrock", "The bedrock base category.");
   public BooleanSetting bedrock = new BooleanSetting(
      "Bedrock", "Enabled", "Renders blocks which have a bedrock base.", new CategorySetting.CategoryCondition(this.bedrockCategory), true
   );
   public ColorSetting bedrockFillColor = new ColorSetting(
      "BedrockFillColor",
      "Fill",
      "The color for the fill rendering of bedrock base blocks.",
      new CategorySetting.CategoryCondition(this.bedrockCategory),
      new ColorSetting.ColorValue(new Color(0, 255, 0, ColorUtil.b().getColor().getAlpha()), false, false)
   );
   public ColorSetting bedrockOutlineColor = new ColorSetting(
      "BedrockOutlineColor",
      "Outline",
      "The color for the outline rendering of bedrock base blocks.",
      new CategorySetting.CategoryCondition(this.bedrockCategory),
      new ColorSetting.ColorValue(new Color(0, 255, 0, ColorUtil.c().getColor().getAlpha()), false, false)
   );
   public CategorySetting obsidianCategory = new CategorySetting("Obsidian", "The obsidian base category.");
   public BooleanSetting obsidian = new BooleanSetting(
      "Obsidian", "Enabled", "Renders blocks which have a obsidian base.", new CategorySetting.CategoryCondition(this.obsidianCategory), true
   );
   public ColorSetting obsidianFillColor = new ColorSetting(
      "ObsidianFillColor",
      "Fill",
      "The color for the fill rendering of obsidian base blocks.",
      new CategorySetting.CategoryCondition(this.obsidianCategory),
      new ColorSetting.ColorValue(new Color(255, 255, 0, ColorUtil.b().getColor().getAlpha()), false, false)
   );
   public ColorSetting obsidianOutlineColor = new ColorSetting(
      "ObsidianOutlineColor",
      "Outline",
      "The color for the outline rendering of obsidian base blocks.",
      new CategorySetting.CategoryCondition(this.obsidianCategory),
      new ColorSetting.ColorValue(new Color(255, 255, 0, ColorUtil.c().getColor().getAlpha()), false, false)
   );
   public CategorySetting airCategory = new CategorySetting("Air", "The air base category.");
   public BooleanSetting air = new BooleanSetting(
      "Air", "Enabled", "Renders blocks which have a air base.", new CategorySetting.CategoryCondition(this.airCategory), true
   );
   public ColorSetting airFillColor = new ColorSetting(
      "AirFillColor",
      "Fill",
      "The color for the fill rendering of air base blocks.",
      new CategorySetting.CategoryCondition(this.airCategory),
      new ColorSetting.ColorValue(new Color(255, 0, 0, ColorUtil.b().getColor().getAlpha()), false, false)
   );
   public ColorSetting airOutlineColor = new ColorSetting(
      "AirOutlineColor",
      "Outline",
      "The color for the outline rendering of air base blocks.",
      new CategorySetting.CategoryCondition(this.airCategory),
      new ColorSetting.ColorValue(new Color(255, 0, 0, ColorUtil.c().getColor().getAlpha()), false, false)
   );
   private ArrayList<PhaseESP.PhaseBlock> u = new ArrayList<>();

   @EventTarget
   public void a_void_TickEvent(TickEvent var1) {
      if (!this.nullCheck()) {
         ArrayList var2 = new ArrayList();
         BlockPos var3 = PositionUtil.a_BlockPos_Entity(mc.player);

         for (Direction var7 : Direction.values()) {
            if (var7.getAxis().isHorizontal()) {
               BlockPos var8 = var3.relative(var7);
               if (mc.level.getBlockState(var8).getBlock() == Blocks.BEDROCK || mc.level.getBlockState(var8).getBlock() == Blocks.OBSIDIAN) {
                  PhaseESP.BlockSafety var9 = this.a_BlockSafety_BlockPos(var8);
                  if (var9 != null) {
                     var2.add(new PhaseESP.PhaseBlock(new AABB(var8), var9));
                  }
               }
            }
         }

         this.u.clear();
         this.u.addAll(var2);
      }
   }

   @EventTarget
   public void a_void_Render3DEvent(Render3DEvent var1) {
      if (!this.nullCheck() && !this.u.isEmpty()) {
         for (PhaseESP.PhaseBlock var3 : (Iterable<PhaseESP.PhaseBlock>)(Object)this.u) {
            AABB var4 = new AABB(
               var3.box().minX, var3.box().minY, var3.box().minZ, var3.box().maxX, var3.box().minY + this.fillHeight.getValue().doubleValue(), var3.box().maxZ
            );
            AABB var5 = new AABB(
               var3.box().minX,
               var3.box().minY,
               var3.box().minZ,
               var3.box().maxX,
               var3.box().minY + this.outlineHeight.getValue().doubleValue(),
               var3.box().maxZ
            );
            if (this.fill.getValue().equalsIgnoreCase("Normal")) {
               Render3DUtil.a_void_PoseStack_AABB_Color(var1.getMatrices(), var4, this.a_Color_BlockSafety(var3.blockSafety()));
            }

            if (this.outline.getValue().equalsIgnoreCase("Normal")) {
               Render3DUtil.b(var1.getMatrices(), var5, this.b(var3.blockSafety()));
            }
         }
      }
   }

   private Color a_Color_BlockSafety(PhaseESP.BlockSafety var1) {
      return switch (var1) {
         case BEDROCK -> this.bedrockFillColor.getColor();
         case OBSIDIAN -> this.obsidianFillColor.getColor();
         default -> this.airFillColor.getColor();
      };
   }

   private Color b(PhaseESP.BlockSafety var1) {
      return switch (var1) {
         case BEDROCK -> this.bedrockOutlineColor.getColor();
         case OBSIDIAN -> this.obsidianOutlineColor.getColor();
         default -> this.airOutlineColor.getColor();
      };
   }

   private PhaseESP.BlockSafety a_BlockSafety_BlockPos(BlockPos var1) {
      if (mc.level.getBlockState(var1.below()).getBlock() == Blocks.BEDROCK && this.bedrock.getValue()) {
         return PhaseESP.BlockSafety.BEDROCK;
      } else if (mc.level.getBlockState(var1.below()).getBlock() == Blocks.OBSIDIAN && this.obsidian.getValue()) {
         return PhaseESP.BlockSafety.OBSIDIAN;
      } else {
         return mc.level.getBlockState(var1.below()).getBlock() == Blocks.AIR && this.air.getValue() ? PhaseESP.BlockSafety.AIR : null;
      }
   }

   enum BlockSafety {
      BEDROCK,
      OBSIDIAN,
      AIR;
   }

   public record PhaseBlock(AABB box, PhaseESP.BlockSafety blockSafety) {
   }
}
