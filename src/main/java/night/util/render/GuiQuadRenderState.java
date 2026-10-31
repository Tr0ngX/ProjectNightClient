package night.util.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import org.joml.Matrix3x2fc;
import org.jspecify.annotations.Nullable;

public record GuiQuadRenderState(
   RenderPipeline renderPipeline,
   TextureSetup textureSetup,
   Matrix3x2fc pose,
   float[] value,
   float[] value2,
   int[] value3,
   float @Nullable [] value4,
   float @Nullable [] value5,
   @Nullable ScreenRectangle screenRectangle,
   @Nullable ScreenRectangle screenRectangle2
) implements GuiElementRenderState {
   public GuiQuadRenderState(Matrix3x2fc var1, float[] var2, float[] var3, int[] var4, @Nullable ScreenRectangle var5) {
      this(RenderPipelines.GUI, TextureSetup.noTexture(), var1, var2, var3, var4, null, null, var5, a(var2, var3, var1, var5));
   }

   public GuiQuadRenderState(
      TextureSetup var1, Matrix3x2fc var2, float[] var3, float[] var4, int[] var5, float[] var6, float[] var7, @Nullable ScreenRectangle var8
   ) {
      this(RenderPipelines.GUI_TEXTURED, var1, var2, var3, var4, var5, var6, var7, var8, a(var3, var4, var2, var8));
   }

   public void buildVertices(VertexConsumer var1) {
      if (this.value4 != null && this.value5 != null) {
         for (int var3 = 0; var3 < this.value.length; var3++) {
            var1.addVertexWith2DPose(this.pose, this.value[var3], this.value2[var3]).setUv(this.value4[var3], this.value5[var3]).setColor(this.value3[var3]);
         }
      } else {
         for (int var2 = 0; var2 < this.value.length; var2++) {
            var1.addVertexWith2DPose(this.pose, this.value[var2], this.value2[var2]).setColor(this.value3[var2]);
         }
      }
   }

   static @Nullable ScreenRectangle a(float[] var0, float[] var1, Matrix3x2fc var2, @Nullable ScreenRectangle var3) {
      if (var0.length == 0) {
         return var3;
      }

      float var4 = var0[0];
      float var5 = var0[0];
      float var6 = var1[0];
      float var7 = var1[0];

      for (int var8 = 1; var8 < var0.length; var8++) {
         if (var0[var8] < var4) {
            var4 = var0[var8];
         }

         if (var0[var8] > var5) {
            var5 = var0[var8];
         }

         if (var1[var8] < var6) {
            var6 = var1[var8];
         }

         if (var1[var8] > var7) {
            var7 = var1[var8];
         }
      }

      int var13 = (int)Math.floor(var4);
      int var9 = (int)Math.floor(var6);
      int var10 = (int)Math.ceil(var5);
      int var11 = (int)Math.ceil(var7);
      ScreenRectangle var12 = new ScreenRectangle(var13, var9, var10 - var13, var11 - var9).transformMaxBounds(var2);
      return var3 != null ? var3.intersection(var12) : var12;
   }

   public RenderPipeline pipeline() {
      return this.renderPipeline;
   }

   public @Nullable ScreenRectangle scissorArea() {
      return this.screenRectangle;
   }

   public @Nullable ScreenRectangle bounds() {
      return this.screenRectangle2;
   }
}
