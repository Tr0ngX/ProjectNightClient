package night.module.impl.visuals;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.minecraft.core.BlockPos;
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
import night.util.render.EspShaderRenderer;
import night.util.render.Render3DUtil;

@ModuleInfo(name = "VoidESP", description = "Highlights any non-bedrock blocks that can drop you into the void.", category = Module.Category.VISUALS)
public class VoidESP extends Module {
   public NumberSetting range = new NumberSetting("Range", "The maximum range at which void blocks will be rendered.", 10, 1, 50);
   public BooleanSetting asynchronous = new BooleanSetting("Asynchronous", "Performs calculations on separate threads.", true);
   public CategorySetting fillCategory = new CategorySetting("Fill", "The category for settings related to fill rendering.");
   public ModeSetting fill = new ModeSetting(
      "Fill",
      "Mode",
      "The mode for the fill rendering on the void blocks.",
      new CategorySetting.CategoryCondition(this.fillCategory),
      "Normal",
      new String[]{"None", "Normal", "Gradient"}
   );
   public NumberSetting fillHeight = new NumberSetting(
      "FillHeight",
      "Height",
      "The height of the fill rendering on the void blocks.",
      new ModeSetting.ModeCondition(this.fill, "Normal", "Gradient"),
      1.0,
      0.0,
      2.0
   );
   public ColorSetting fillColor = new ColorSetting(
      "FillColor",
      "Color",
      "The color for the fill rendering on the void blocks.",
      new ModeSetting.ModeCondition(this.fill, "Normal", "Gradient"),
      new ColorSetting.ColorValue(new Color(255, 0, 0, ColorUtil.b().getColor().getAlpha()), false, false)
   );
   public CategorySetting outlineCategory = new CategorySetting("Outline", "The category for settings related to outline rendering.");
   public ModeSetting outline = new ModeSetting(
      "Outline",
      "Mode",
      "The mode for the outline rendering on the void blocks.",
      new CategorySetting.CategoryCondition(this.outlineCategory),
      "Normal",
      new String[]{"None", "Normal", "Gradient"}
   );
   public NumberSetting outlineHeight = new NumberSetting(
      "OutlineHeight",
      "Height",
      "The height of the outline rendering on the void blocks.",
      new ModeSetting.ModeCondition(this.outline, "Normal", "Gradient"),
      1.0,
      0.0,
      2.0
   );
   public ColorSetting outlineColor = new ColorSetting(
      "OutlineColor",
      "Color",
      "The color for the outline rendering on the void blocks.",
      new ModeSetting.ModeCondition(this.outline, "Normal", "Gradient"),
      new ColorSetting.ColorValue(new Color(255, 0, 0, ColorUtil.c().getColor().getAlpha()), false, false)
   );
   public CategorySetting shaderCategory = new CategorySetting("Shader", "The category for settings related to shader rendering.");
   public ModeSetting shader = new ModeSetting(
      "Shader",
      "Mode",
      "The animated shader that will be rendered on the void blocks.",
      new CategorySetting.CategoryCondition(this.shaderCategory),
      "None",
      EspShaderRenderer.a
   );
   public NumberSetting shaderOpacity = new NumberSetting(
      "ShaderOpacity", "Opacity", "Void block shader opacity.", new ModeSetting.ModeCondition(this.shader, EspShaderRenderer.b), 100, 0, 100
   );
   public NumberSetting shaderSpeed = new NumberSetting(
      "ShaderSpeed", "Speed", "Void block shader animation speed.", new ModeSetting.ModeCondition(this.shader, EspShaderRenderer.b), 1.0, 0.1, 10.0
   );
   public NumberSetting shaderStep = new NumberSetting(
      "ShaderStep", "Step", "Void block gradient step size.", new ModeSetting.ModeCondition(this.shader, "Gradient"), 50.0, 0.1, 200.0
   );
   public ColorSetting shaderColor1 = new ColorSetting(
      "ShaderColor1",
      "The first gradient color.",
      new ModeSetting.ModeCondition(this.shader, "Gradient"),
      new ColorSetting.ColorValue(new Color(255, 0, 0, 255), false, false)
   );
   public ColorSetting shaderColor2 = new ColorSetting(
      "ShaderColor2",
      "The second gradient color.",
      new ModeSetting.ModeCondition(this.shader, "Gradient"),
      new ColorSetting.ColorValue(new Color(180, 0, 255, 255), false, false)
   );
   public ColorSetting shaderColor3 = new ColorSetting(
      "ShaderColor3",
      "The third gradient color.",
      new ModeSetting.ModeCondition(this.shader, "Gradient"),
      new ColorSetting.ColorValue(new Color(255, 0, 100, 255), false, false)
   );
   public ColorSetting shaderColor4 = new ColorSetting(
      "ShaderColor4",
      "The fourth gradient color.",
      new ModeSetting.ModeCondition(this.shader, "Gradient"),
      new ColorSetting.ColorValue(new Color(100, 0, 255, 255), false, false)
   );
   public ColorSetting x = new ColorSetting(
      "ShaderGlowColor",
      "GlowColor",
      "The glow tint color for void blocks.",
      new ModeSetting.ModeCondition(this.shader, "Glowing"),
      new ColorSetting.ColorValue(new Color(255, 0, 0, 255), false, false)
   );
   private final ExecutorService y = Executors.newSingleThreadExecutor();
   private final List<BlockPos> z = Collections.synchronizedList(new ArrayList<>());

   @EventTarget
   public void a(TickEvent var1) {
      if (mc.player != null && mc.level != null) {
         Runnable var2 = () -> {
            ArrayList var1x = new ArrayList();

            for (int var2x = (int)(mc.player.getX() - this.range.getValue().intValue()); var2x < mc.player.getX() + this.range.getValue().intValue(); var2x++) {
               for (int var3 = (int)(mc.player.getZ() - this.range.getValue().intValue()); var3 < mc.player.getZ() + this.range.getValue().intValue(); var3++) {
                  BlockPos var4 = BlockPos.containing(var2x, mc.level.getMinY(), var3);
                  if (mc.level.getBlockState(var4).getBlock() != Blocks.BEDROCK && mc.level.getWorldBorder().isWithinBounds(var4)) {
                     var1x.add(var4);
                  }
               }
            }

            synchronized (this.z) {
               this.z.clear();
               this.z.addAll(var1x);
            }
         };
         if (this.asynchronous.getValue()) {
            this.y.submit(var2);
         } else {
            var2.run();
         }
      }
   }

   @EventTarget
   public void a(Render3DEvent var1) {
      if (mc.level != null) {
         synchronized (this.z) {
            if (!this.z.isEmpty()) {
               boolean var3 = !this.shader.getValue().equalsIgnoreCase("None");
               ArrayList var4 = new ArrayList();
               ArrayList var5 = new ArrayList();
               boolean var6 = this.fill.getValue().equalsIgnoreCase("Normal");
               boolean var7 = this.fill.getValue().equalsIgnoreCase("Gradient");
               boolean var8 = this.outline.getValue().equalsIgnoreCase("Normal");
               boolean var9 = this.outline.getValue().equalsIgnoreCase("Gradient");

               for (BlockPos var11 : (Iterable<BlockPos>)(Object)this.z) {
                  AABB var12 = new AABB(var11);
                  AABB var13 = new AABB(var12.minX, var12.minY, var12.minZ, var12.maxX, var12.minY + this.fillHeight.getValue().doubleValue(), var12.maxZ);
                  AABB var14 = new AABB(var12.minX, var12.minY, var12.minZ, var12.maxX, var12.minY + this.outlineHeight.getValue().doubleValue(), var12.maxZ);
                  if (var3) {
                     if (var6) {
                        Render3DUtil.a(var4, var1.getMatrices(), var13, this.fillColor.getColor(), this.fillColor.getColor());
                     }

                     if (var7) {
                        Render3DUtil.a(
                           var4,
                           var1.getMatrices(),
                           var13,
                           this.fillHeight.getValue().floatValue() < 0.0F ? this.fillColor.getColor() : new Color(0, 0, 0, 0),
                           this.fillHeight.getValue().floatValue() < 0.0F ? new Color(0, 0, 0, 0) : this.fillColor.getColor()
                        );
                     }

                     if (var8) {
                        Render3DUtil.b(var5, var1.getMatrices(), var14, this.outlineColor.getColor(), this.outlineColor.getColor());
                     }

                     if (var9) {
                        Render3DUtil.b(
                           var5,
                           var1.getMatrices(),
                           var14,
                           this.outlineHeight.getValue().floatValue() < 0.0F ? this.outlineColor.getColor() : new Color(0, 0, 0, 0),
                           this.outlineHeight.getValue().floatValue() < 0.0F ? new Color(0, 0, 0, 0) : this.outlineColor.getColor()
                        );
                     }
                  } else {
                     if (var6) {
                        Render3DUtil.a_void_PoseStack_AABB_Color(var1.getMatrices(), var13, this.fillColor.getColor());
                     }

                     if (var7) {
                        Render3DUtil.a_void_PoseStack_AABB_Color_Color(
                           var1.getMatrices(),
                           var13,
                           this.fillHeight.getValue().floatValue() < 0.0F ? this.fillColor.getColor() : new Color(0, 0, 0, 0),
                           this.fillHeight.getValue().floatValue() < 0.0F ? new Color(0, 0, 0, 0) : this.fillColor.getColor()
                        );
                     }

                     if (var8) {
                        Render3DUtil.b(var1.getMatrices(), var14, this.outlineColor.getColor());
                     }

                     if (var9) {
                        Render3DUtil.b(
                           var1.getMatrices(),
                           var14,
                           this.outlineHeight.getValue().floatValue() < 0.0F ? this.outlineColor.getColor() : new Color(0, 0, 0, 0),
                           this.outlineHeight.getValue().floatValue() < 0.0F ? new Color(0, 0, 0, 0) : this.outlineColor.getColor()
                        );
                     }
                  }
               }

               if (var3 && (!var4.isEmpty() || !var5.isEmpty())) {
                  EspShaderRenderer.ShaderUniformConfig var17 = EspShaderRenderer.a(
                     this.shader.getValue(),
                     this.shaderOpacity.getValue().floatValue(),
                     this.shaderSpeed.getValue().floatValue(),
                     this.shaderStep.getValue().floatValue(),
                     this.shaderColor1.getColor(),
                     this.shaderColor2.getColor(),
                     this.shaderColor3.getColor(),
                     this.shaderColor4.getColor(),
                     this.x.getColor(),
                     null
                  );
                  EspShaderRenderer.a(var4, var5, var17);
               }
            }
         }
      }
   }

   @Override
   public String getDisplayInfo() {
      return String.valueOf(this.z.size());
   }
}
