package night.module.impl.visuals;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import night.events.EventTarget;
import night.events.impl.Render3DEvent;
import night.module.Module;
import night.module.ModuleInfo;
import night.setting.impl.ColorSetting;
import night.setting.impl.ModeSetting;
import night.setting.impl.NumberSetting;
import night.util.animation.EasingUtil;
import night.util.color.ColorUtil;
import night.util.math.MathUtil;
import night.util.render.Render3DUtil;
import night.util.world.BlockInteractionUtil;

@ModuleInfo(
   name = "BlockHighlight",
   description = "Replaces the default Minecraft block highlight with a more customizable one.",
   category = Module.Category.VISUALS
)
public class BlockHighlight extends Module {
   public ModeSetting animation2 = new ModeSetting(
      "Animation", "The animation that will be applied to the rendering.", "Static", new String[]{"Static", "Slide"}
   );
   public ModeSetting mode = new ModeSetting(
      "Mode", "The rendering that will be applied to the target block.", "Outline", new String[]{"None", "Fill", "Outline", "Both", "Complex"}
   );
   public NumberSetting smoothness = new NumberSetting("Smoothness", "The smoothness for the slide while target block is changing.", 1, 0, 20);
   public ColorSetting fillColor = new ColorSetting(
      "FillColor", "The color that will be used for the fill rendering.", new ModeSetting.ModeCondition(this.mode, "Fill", "Both"), ColorUtil.b()
   );
   public ColorSetting outlineColor = new ColorSetting(
      "OutlineColor", "The color that will be used for the outline rendering.", new ModeSetting.ModeCondition(this.mode, "Outline", "Both"), ColorUtil.c()
   );
   private BlockPos j = null;
   private Vec3 k = null;
   private long l = 0L;

   @EventTarget
   public void a(Render3DEvent var1) {
      if (mc.player != null && mc.level != null) {
         if (!this.mode.getValue().equalsIgnoreCase("None")) {
            if (mc.hitResult instanceof BlockHitResult var3) {
               BlockPos var9 = var3.getBlockPos();
               if (this.animation2.getValue().equals("Slide") && var9 != null) {
                  if (this.k == null) {
                     this.k = MathUtil.a_Vec3_BlockPos(var9);
                  }

                  if (!BlockInteractionUtil.a_bool_BlockPos_BlockPos(var9, this.j)) {
                     this.l = System.currentTimeMillis();
                     this.j = var9;
                  }
               }

               Vec3 var4 = MathUtil.a_Vec3_BlockPos(var9);
               if (this.animation2.getValue().equalsIgnoreCase("Slide") && this.k != null) {
                  float var5 = EasingUtil.a(
                     EasingUtil.a(this.l, (int)(Math.pow(this.smoothness.getValue().doubleValue(), 1.4) * 1000.0)), EasingUtil.Easing.EASE_OUT_QUART
                  );
                  this.k = this.k.add(MathUtil.a_Vec3_Vec3_float(MathUtil.a_Vec3_BlockPos(var9).subtract(this.k), var5));
                  var4 = this.k;
               }

               BlockState var10 = mc.level.getBlockState(var9);
               if (!var10.isAir() && mc.level.getWorldBorder().isWithinBounds(var9)) {
                  VoxelShape var6 = var10.getShape(mc.level, var9);
                  if (!var6.isEmpty()) {
                     if (this.mode.getValue().equalsIgnoreCase("Complex")) {
                        for (AABB var8 : (Iterable<AABB>)(Object)var6.toAabbs()) {
                           Render3DUtil.a_void_PoseStack_AABB_Color(var1.getMatrices(), var8.move(var4), this.fillColor.getColor());
                           Render3DUtil.b(var1.getMatrices(), var8.move(var4), this.outlineColor.getColor());
                        }
                     } else {
                        if (this.mode.getValue().equalsIgnoreCase("Fill") || this.mode.getValue().equalsIgnoreCase("Both")) {
                           Render3DUtil.a_void_PoseStack_AABB_Color(var1.getMatrices(), var6.bounds().move(var4), this.fillColor.getColor());
                        }

                        if (this.mode.getValue().equalsIgnoreCase("Outline") || this.mode.getValue().equalsIgnoreCase("Both")) {
                           Render3DUtil.b(var1.getMatrices(), var6.bounds().move(var4), this.outlineColor.getColor());
                        }
                     }
                  }
               }
            }
         }
      }
   }

   @Override
   public String getDisplayInfo() {
      return this.mode.getValue();
   }
}
