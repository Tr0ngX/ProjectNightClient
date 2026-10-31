package night.module.impl.visuals;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import night.Night;
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
import night.util.animation.EasingUtil;
import night.util.color.ColorUtil;
import night.util.render.EspShaderRenderer;
import night.util.render.Render3DUtil;
import night.util.world.HoleUtil;

@ModuleInfo(name = "HoleESP", description = "Highlights all holes in a specified radius.", category = Module.Category.VISUALS)
public class HoleESP extends Module {
   public NumberSetting range = new NumberSetting("Range", "The maximum range at which holes will be rendered.", 10, 1, 50);
   public BooleanSetting asynchronous = new BooleanSetting("Asynchronous", "Performs calculations on separate threads.", true);
   public BooleanSetting doubleHoles = new BooleanSetting("DoubleHoles", "Whether or not to render the ESP on double holes.", true);
   public BooleanSetting quadHoles = new BooleanSetting("QuadHoles", "Whether or not to render the ESP on quad holes.", true);
   public BooleanSetting partialHoles = new BooleanSetting("PartialHoles", "Whether or not to render the ESP on partial (incomplete) holes.", true);
   public BooleanSetting fade = new BooleanSetting("Fade", "Fades the holes in and out based on your distance to them.", false);
   public ModeSetting fill = new ModeSetting("Fill", "The mode for the fill rendering on the hole boxes.", "Normal", new String[]{"None", "Normal", "Gradient"});
   public NumberSetting fillHeight = new NumberSetting(
      "FillHeight", "The height of the fill rendering on the holes.", new ModeSetting.ModeCondition(this.fill, "Normal", "Gradient"), 1.0, -2.0, 2.0
   );
   public ModeSetting outline = new ModeSetting(
      "Outline", "The mode for the outline rendering on the hole boxes.", "Normal", new String[]{"None", "Normal", "Gradient"}
   );
   public NumberSetting outlineHeight = new NumberSetting(
      "OutlineHeight", "The height of the outline rendering on the holes.", new ModeSetting.ModeCondition(this.outline, "Normal", "Gradient"), 1.0, -2.0, 2.0
   );
   public BooleanSetting wireframe = new BooleanSetting("Wireframe", "Renders an X wireframe on the hole boxes.", false);
   public CategorySetting safeCategory = new CategorySetting("Safe", "The category that contains the settings for coloring of safe (all-bedrock) holes.");
   public ColorSetting safeFillColor = new ColorSetting(
      "SafeFillColor",
      "Fill",
      "The color for the fill rendering on safe holes.",
      new CategorySetting.CategoryCondition(this.safeCategory),
      new ColorSetting.ColorValue(new Color(0, 255, 0, ColorUtil.b().getColor().getAlpha()), false, false)
   );
   public ColorSetting safeOutlineColor = new ColorSetting(
      "SafeOutlineColor",
      "Outline",
      "The color for the outline rendering on safe holes.",
      new CategorySetting.CategoryCondition(this.safeCategory),
      new ColorSetting.ColorValue(new Color(0, 255, 0, ColorUtil.c().getColor().getAlpha()), false, false)
   );
   public ColorSetting safeWireframeColor = new ColorSetting(
      "SafeWireframeColor",
      "Wireframe",
      "The color for the wireframe rendering on safe holes.",
      new CategorySetting.CategoryCondition(this.safeCategory),
      new ColorSetting.ColorValue(new Color(0, 255, 0, 125), false, false)
   );
   public ModeSetting safeShader = new ModeSetting(
      "SafeShader", "Shader", "The animated shader for safe holes.", new CategorySetting.CategoryCondition(this.safeCategory), "None", EspShaderRenderer.a
   );
   public NumberSetting safeShaderOpacity = new NumberSetting(
      "SafeShaderOpacity", "Opacity", "Safe hole shader opacity.", new ModeSetting.ModeCondition(this.safeShader, EspShaderRenderer.b), 100, 0, 100
   );
   public NumberSetting safeShaderSpeed = new NumberSetting(
      "SafeShaderSpeed", "Speed", "Safe hole shader speed.", new ModeSetting.ModeCondition(this.safeShader, EspShaderRenderer.b), 1.0, 0.1, 10.0
   );
   public NumberSetting safeShaderStep = new NumberSetting(
      "SafeShaderStep", "Step", "Safe hole gradient step size.", new ModeSetting.ModeCondition(this.safeShader, "Gradient"), 50.0, 0.1, 200.0
   );
   public ColorSetting x = new ColorSetting(
      "SafeShaderColor1",
      "The first gradient color for safe holes.",
      new ModeSetting.ModeCondition(this.safeShader, "Gradient"),
      new ColorSetting.ColorValue(new Color(0, 255, 0, 255), false, false)
   );
   public ColorSetting y = new ColorSetting(
      "SafeShaderColor2",
      "The second gradient color for safe holes.",
      new ModeSetting.ModeCondition(this.safeShader, "Gradient"),
      new ColorSetting.ColorValue(new Color(0, 200, 100, 255), false, false)
   );
   public ColorSetting z = new ColorSetting(
      "SafeShaderColor3",
      "The third gradient color for safe holes.",
      new ModeSetting.ModeCondition(this.safeShader, "Gradient"),
      new ColorSetting.ColorValue(new Color(0, 255, 200, 255), false, false)
   );
   public ColorSetting safeShaderColor4 = new ColorSetting(
      "SafeShaderColor4",
      "The fourth gradient color for safe holes.",
      new ModeSetting.ModeCondition(this.safeShader, "Gradient"),
      new ColorSetting.ColorValue(new Color(100, 255, 0, 255), false, false)
   );
   public ColorSetting safeShaderGlowColor = new ColorSetting(
      "SafeShaderGlowColor",
      "GlowColor",
      "The glow tint color for safe holes.",
      new ModeSetting.ModeCondition(this.safeShader, "Glowing"),
      new ColorSetting.ColorValue(new Color(0, 255, 0, 255), false, false)
   );
   public CategorySetting unsafeCategory = new CategorySetting(
      "Unsafe", "The category that contains the settings for coloring of unsafe (blast-proof but not pure bedrock) holes."
   );
   public ColorSetting unsafeFillColor = new ColorSetting(
      "UnsafeFillColor",
      "Fill",
      "The color for the fill rendering on unsafe holes.",
      new CategorySetting.CategoryCondition(this.unsafeCategory),
      new ColorSetting.ColorValue(new Color(255, 0, 0, ColorUtil.b().getColor().getAlpha()), false, false)
   );
   public ColorSetting unsafeOutlineColor = new ColorSetting(
      "UnsafeOutlineColor",
      "Outline",
      "The color for the outline rendering on unsafe holes.",
      new CategorySetting.CategoryCondition(this.unsafeCategory),
      new ColorSetting.ColorValue(new Color(255, 0, 0, ColorUtil.c().getColor().getAlpha()), false, false)
   );
   public ColorSetting unsafeWireframeColor = new ColorSetting(
      "UnsafeWireframeColor",
      "Wireframe",
      "The color for the wireframe rendering on unsafe holes.",
      new CategorySetting.CategoryCondition(this.unsafeCategory),
      new ColorSetting.ColorValue(new Color(255, 0, 0, 125), false, false)
   );
   public ModeSetting unsafeShader = new ModeSetting(
      "UnsafeShader",
      "Shader",
      "The animated shader for unsafe holes.",
      new CategorySetting.CategoryCondition(this.unsafeCategory),
      "None",
      EspShaderRenderer.a
   );
   public NumberSetting unsafeShaderOpacity = new NumberSetting(
      "UnsafeShaderOpacity", "Opacity", "Unsafe hole shader opacity.", new ModeSetting.ModeCondition(this.unsafeShader, EspShaderRenderer.b), 100, 0, 100
   );
   public NumberSetting unsafeShaderSpeed = new NumberSetting(
      "UnsafeShaderSpeed", "Speed", "Unsafe hole shader speed.", new ModeSetting.ModeCondition(this.unsafeShader, EspShaderRenderer.b), 1.0, 0.1, 10.0
   );
   public NumberSetting unsafeShaderStep = new NumberSetting(
      "UnsafeShaderStep", "Step", "Unsafe hole gradient step size.", new ModeSetting.ModeCondition(this.unsafeShader, "Gradient"), 50.0, 0.1, 200.0
   );
   public ColorSetting unsafeShaderColor1 = new ColorSetting(
      "UnsafeShaderColor1",
      "The first gradient color for unsafe holes.",
      new ModeSetting.ModeCondition(this.unsafeShader, "Gradient"),
      new ColorSetting.ColorValue(new Color(255, 0, 0, 255), false, false)
   );
   public ColorSetting unsafeShaderColor2 = new ColorSetting(
      "UnsafeShaderColor2",
      "The second gradient color for unsafe holes.",
      new ModeSetting.ModeCondition(this.unsafeShader, "Gradient"),
      new ColorSetting.ColorValue(new Color(255, 100, 0, 255), false, false)
   );
   public ColorSetting unsafeShaderColor3 = new ColorSetting(
      "UnsafeShaderColor3",
      "The third gradient color for unsafe holes.",
      new ModeSetting.ModeCondition(this.unsafeShader, "Gradient"),
      new ColorSetting.ColorValue(new Color(255, 0, 100, 255), false, false)
   );
   public ColorSetting unsafeShaderColor4 = new ColorSetting(
      "UnsafeShaderColor4",
      "The fourth gradient color for unsafe holes.",
      new ModeSetting.ModeCondition(this.unsafeShader, "Gradient"),
      new ColorSetting.ColorValue(new Color(200, 0, 0, 255), false, false)
   );
   public ColorSetting unsafeShaderGlowColor = new ColorSetting(
      "UnsafeShaderGlowColor",
      "GlowColor",
      "The glow tint color for unsafe holes.",
      new ModeSetting.ModeCondition(this.unsafeShader, "Glowing"),
      new ColorSetting.ColorValue(new Color(255, 0, 0, 255), false, false)
   );
   public CategorySetting partialCategory = new CategorySetting(
      "Partial", "The category that contains the settings for coloring of partial (incomplete) holes."
   );
   public ColorSetting partialFillColor = new ColorSetting(
      "PartialFillColor",
      "Fill",
      "The color for the fill rendering on partial holes.",
      new CategorySetting.CategoryCondition(this.partialCategory),
      new ColorSetting.ColorValue(new Color(255, 255, 255, ColorUtil.b().getColor().getAlpha()), false, false)
   );
   public ColorSetting partialOutlineColor = new ColorSetting(
      "PartialOutlineColor",
      "Outline",
      "The color for the outline rendering on partial holes.",
      new CategorySetting.CategoryCondition(this.partialCategory),
      new ColorSetting.ColorValue(new Color(255, 255, 255, ColorUtil.c().getColor().getAlpha()), false, false)
   );
   public ColorSetting partialWireframeColor = new ColorSetting(
      "PartialWireframeColor",
      "Wireframe",
      "The color for the wireframe rendering on partial holes.",
      new CategorySetting.CategoryCondition(this.partialCategory),
      new ColorSetting.ColorValue(new Color(255, 255, 255, 125), false, false)
   );
   public ModeSetting partialShader = new ModeSetting(
      "PartialShader",
      "Shader",
      "The animated shader for partial holes.",
      new CategorySetting.CategoryCondition(this.partialCategory),
      "None",
      EspShaderRenderer.a
   );
   public NumberSetting partialShaderOpacity = new NumberSetting(
      "PartialShaderOpacity", "Opacity", "Partial hole shader opacity.", new ModeSetting.ModeCondition(this.partialShader, EspShaderRenderer.b), 100, 0, 100
   );
   public NumberSetting partialShaderSpeed = new NumberSetting(
      "PartialShaderSpeed", "Speed", "Partial hole shader speed.", new ModeSetting.ModeCondition(this.partialShader, EspShaderRenderer.b), 1.0, 0.1, 10.0
   );
   public NumberSetting partialShaderStep = new NumberSetting(
      "PartialShaderStep", "Step", "Partial hole gradient step size.", new ModeSetting.ModeCondition(this.partialShader, "Gradient"), 50.0, 0.1, 200.0
   );
   public ColorSetting partialShaderColor1 = new ColorSetting(
      "PartialShaderColor1",
      "The first gradient color for partial holes.",
      new ModeSetting.ModeCondition(this.partialShader, "Gradient"),
      new ColorSetting.ColorValue(new Color(255, 255, 255, 255), false, false)
   );
   public ColorSetting partialShaderColor2 = new ColorSetting(
      "PartialShaderColor2",
      "The second gradient color for partial holes.",
      new ModeSetting.ModeCondition(this.partialShader, "Gradient"),
      new ColorSetting.ColorValue(new Color(200, 200, 200, 255), false, false)
   );
   public ColorSetting partialShaderColor3 = new ColorSetting(
      "PartialShaderColor3",
      "The third gradient color for partial holes.",
      new ModeSetting.ModeCondition(this.partialShader, "Gradient"),
      new ColorSetting.ColorValue(new Color(220, 220, 220, 255), false, false)
   );
   public ColorSetting partialShaderColor4 = new ColorSetting(
      "PartialShaderColor4",
      "The fourth gradient color for partial holes.",
      new ModeSetting.ModeCondition(this.partialShader, "Gradient"),
      new ColorSetting.ColorValue(new Color(180, 180, 180, 255), false, false)
   );
   public ColorSetting partialShaderGlowColor = new ColorSetting(
      "PartialShaderGlowColor",
      "GlowColor",
      "The glow tint color for partial holes.",
      new ModeSetting.ModeCondition(this.partialShader, "Glowing"),
      new ColorSetting.ColorValue(new Color(255, 255, 255, 255), false, false)
   );
   private final ExecutorService ac = Executors.newSingleThreadExecutor();
   private final List<HoleUtil.HoleInfo> ad = Collections.synchronizedList(new ArrayList<>());

   @EventTarget
   public void a_void_TickEvent(TickEvent var1) {
      if (mc.player != null && mc.level != null) {
         BlockPos var2 = mc.player.blockPosition();
         int var3 = Night.worldStateManager.getSpherePointCount(this.range.getValue().doubleValue());
         Runnable var4 = () -> {
            ArrayList var3x = new ArrayList();

            for (int var4x = 0; var4x < var3; var4x++) {
               BlockPos var5 = var2.offset(Night.worldStateManager.getSphereOffset(var4x));
               HoleUtil.HoleInfo var6 = HoleUtil.a_HoleInfo_BlockPos_double(var5, 0.0);
               if (var6 != null) {
                  var3x.add(var6);
               } else {
                  if (this.doubleHoles.getValue()) {
                     HoleUtil.HoleInfo var7 = HoleUtil.b(var5, 0.0);
                     if (var7 != null) {
                        var3x.add(var7);
                        continue;
                     }
                  }

                  if (this.quadHoles.getValue()) {
                     HoleUtil.HoleInfo var11 = HoleUtil.c(var5, 0.0);
                     if (var11 != null) {
                        var3x.add(var11);
                        continue;
                     }
                  }

                  if (this.partialHoles.getValue()) {
                     HoleUtil.HoleInfo var12 = HoleUtil.d(var5, 0.0);
                     if (var12 != null) {
                        var3x.add(var12);
                     }
                  }
               }
            }

            synchronized (this.ad) {
               this.ad.clear();
               this.ad.addAll(var3x);
            }
         };
         if (this.asynchronous.getValue()) {
            this.ac.submit(var4);
         } else {
            var4.run();
         }
      }
   }

   @EventTarget
   public void a_void_Render3DEvent(Render3DEvent var1) {
      if (mc.level != null) {
         synchronized (this.ad) {
            if (!this.ad.isEmpty()) {
               boolean var3 = !this.safeShader.getValue().equalsIgnoreCase("None");
               boolean var4 = !this.unsafeShader.getValue().equalsIgnoreCase("None");
               boolean var5 = !this.partialShader.getValue().equalsIgnoreCase("None");
               ArrayList var6 = new ArrayList();
               ArrayList var7 = new ArrayList();
               ArrayList var8 = new ArrayList();
               ArrayList var9 = new ArrayList();
               ArrayList var10 = new ArrayList();
               ArrayList var11 = new ArrayList();
               boolean var12 = this.fill.getValue().equalsIgnoreCase("Normal");
               boolean var13 = this.fill.getValue().equalsIgnoreCase("Gradient");
               boolean var14 = this.outline.getValue().equalsIgnoreCase("Normal");
               boolean var15 = this.outline.getValue().equalsIgnoreCase("Gradient");

               for (HoleUtil.HoleInfo var17 : (Iterable<HoleUtil.HoleInfo>)(Object)this.ad) {
                  if (Render3DUtil.a_bool_AABB(var17.box())) {
                     boolean var18 = var17.holeSafety() == HoleUtil.HoleSafety.SAFE;
                     boolean var19 = var17.holeSafety() == HoleUtil.HoleSafety.PARTIAL;
                     boolean var20 = var18 ? var3 : (var19 ? var5 : var4);
                     AABB var21 = new AABB(
                        var17.box().minX,
                        var17.box().minY,
                        var17.box().minZ,
                        var17.box().maxX,
                        var17.box().minY + this.fillHeight.getValue().doubleValue(),
                        var17.box().maxZ
                     );
                     AABB var22 = new AABB(
                        var17.box().minX,
                        var17.box().minY,
                        var17.box().minZ,
                        var17.box().maxX,
                        var17.box().minY + this.outlineHeight.getValue().doubleValue(),
                        var17.box().maxZ
                     );
                     AABB var23 = new AABB(var17.box().minX, var17.box().minY, var17.box().minZ, var17.box().maxX, var17.box().minY, var17.box().maxZ);
                     Color var24 = this.a_Color_HoleInfo(var17);
                     Color var25 = this.b(var17);
                     Color var26 = this.c(var17);
                     if (var20) {
                        ArrayList var27 = var18 ? var6 : (var19 ? var10 : var8);
                        ArrayList var28 = var18 ? var7 : (var19 ? var11 : var9);
                        if (var12) {
                           Render3DUtil.a(var27, var1.getMatrices(), var21, var24, var24);
                        }

                        if (var13) {
                           Render3DUtil.a(
                              var27,
                              var1.getMatrices(),
                              var21,
                              this.fillHeight.getValue().floatValue() < 0.0F ? var24 : new Color(0, 0, 0, 0),
                              this.fillHeight.getValue().floatValue() < 0.0F ? new Color(0, 0, 0, 0) : var24
                           );
                        }

                        if (var14) {
                           Render3DUtil.b(var28, var1.getMatrices(), var22, var25, var25);
                        }

                        if (var15) {
                           Render3DUtil.b(
                              var28,
                              var1.getMatrices(),
                              var22,
                              this.outlineHeight.getValue().floatValue() < 0.0F ? var25 : new Color(0, 0, 0, 0),
                              this.outlineHeight.getValue().floatValue() < 0.0F ? new Color(0, 0, 0, 0) : var25
                           );
                        }

                        if (this.wireframe.getValue()) {
                           Render3DUtil.c(var28, var1.getMatrices(), var23, var26, var26);
                        }
                     } else {
                        if (var12) {
                           Render3DUtil.a_void_PoseStack_AABB_Color(var1.getMatrices(), var21, var24);
                        }

                        if (var13) {
                           Render3DUtil.a_void_PoseStack_AABB_Color_Color(
                              var1.getMatrices(),
                              var21,
                              this.fillHeight.getValue().floatValue() < 0.0F ? var24 : new Color(0, 0, 0, 0),
                              this.fillHeight.getValue().floatValue() < 0.0F ? new Color(0, 0, 0, 0) : var24
                           );
                        }

                        if (var14) {
                           Render3DUtil.b(var1.getMatrices(), var22, var25);
                        }

                        if (var15) {
                           Render3DUtil.b(
                              var1.getMatrices(),
                              var22,
                              this.outlineHeight.getValue().floatValue() < 0.0F ? var25 : new Color(0, 0, 0, 0),
                              this.outlineHeight.getValue().floatValue() < 0.0F ? new Color(0, 0, 0, 0) : var25
                           );
                        }

                        if (this.wireframe.getValue()) {
                           Render3DUtil.c(var1.getMatrices(), var23, var26);
                        }
                     }
                  }
               }

               if (var3 && (!var6.isEmpty() || !var7.isEmpty())) {
                  EspShaderRenderer.ShaderUniformConfig var31 = EspShaderRenderer.a(
                     this.safeShader.getValue(),
                     this.safeShaderOpacity.getValue().floatValue(),
                     this.safeShaderSpeed.getValue().floatValue(),
                     this.safeShaderStep.getValue().floatValue(),
                     this.x.getColor(),
                     this.y.getColor(),
                     this.z.getColor(),
                     this.safeShaderColor4.getColor(),
                     this.safeShaderGlowColor.getColor(),
                     null
                  );
                  EspShaderRenderer.a(var6, var7, var31);
               }

               if (var4 && (!var8.isEmpty() || !var9.isEmpty())) {
                  EspShaderRenderer.ShaderUniformConfig var32 = EspShaderRenderer.a(
                     this.unsafeShader.getValue(),
                     this.unsafeShaderOpacity.getValue().floatValue(),
                     this.unsafeShaderSpeed.getValue().floatValue(),
                     this.unsafeShaderStep.getValue().floatValue(),
                     this.unsafeShaderColor1.getColor(),
                     this.unsafeShaderColor2.getColor(),
                     this.unsafeShaderColor3.getColor(),
                     this.unsafeShaderColor4.getColor(),
                     this.unsafeShaderGlowColor.getColor(),
                     null
                  );
                  EspShaderRenderer.a(var8, var9, var32);
               }

               if (var5 && (!var10.isEmpty() || !var11.isEmpty())) {
                  EspShaderRenderer.ShaderUniformConfig var33 = EspShaderRenderer.a(
                     this.partialShader.getValue(),
                     this.partialShaderOpacity.getValue().floatValue(),
                     this.partialShaderSpeed.getValue().floatValue(),
                     this.partialShaderStep.getValue().floatValue(),
                     this.partialShaderColor1.getColor(),
                     this.partialShaderColor2.getColor(),
                     this.partialShaderColor3.getColor(),
                     this.partialShaderColor4.getColor(),
                     this.partialShaderGlowColor.getColor(),
                     null
                  );
                  EspShaderRenderer.a(var10, var11, var33);
               }
            }
         }
      }
   }

   @Override
   public String getDisplayInfo() {
      return String.valueOf(this.ad.size());
   }

   private Color a_Color_HoleInfo(HoleUtil.HoleInfo var1) {
      Color var2 = switch (var1.holeSafety()) {
         case SAFE -> this.safeFillColor.getColor();
         case PARTIAL -> this.partialFillColor.getColor();
         default -> this.unsafeFillColor.getColor();
      };
      return !this.fade.getValue() ? var2 : ColorUtil.a(var2, (int)(var2.getAlpha() * this.d(var1)));
   }

   private Color b(HoleUtil.HoleInfo var1) {
      Color var2 = switch (var1.holeSafety()) {
         case SAFE -> this.safeOutlineColor.getColor();
         case PARTIAL -> this.partialOutlineColor.getColor();
         default -> this.unsafeOutlineColor.getColor();
      };
      return !this.fade.getValue() ? var2 : ColorUtil.a(var2, (int)(var2.getAlpha() * this.d(var1)));
   }

   private Color c(HoleUtil.HoleInfo var1) {
      Color var2 = switch (var1.holeSafety()) {
         case SAFE -> this.safeWireframeColor.getColor();
         case PARTIAL -> this.partialWireframeColor.getColor();
         default -> this.unsafeWireframeColor.getColor();
      };
      return !this.fade.getValue() ? var2 : ColorUtil.a(var2, (int)(var2.getAlpha() * this.d(var1)));
   }

   private float d(HoleUtil.HoleInfo var1) {
      return EasingUtil.a(
         (float)(1.0 - Mth.clamp(Math.sqrt(mc.player.distanceToSqr(var1.box().getCenter())) / this.range.getValue().doubleValue(), 0.0, 1.0)),
         EasingUtil.Easing.EASE_OUT_CUBIC
      );
   }
}
