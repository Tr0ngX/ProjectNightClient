package night.mixins;

import java.awt.Color;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import night.gui.ClickGuiScreen;
import night.util.MinecraftInstance;
import night.util.render.MenuBackgroundShader;
import night.util.render.Render2DUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LoadingOverlay.class)
public abstract class LoadingOverlayMixin extends Overlay implements MinecraftInstance {
   @Shadow
   @Final
   private ReloadInstance reload;
   @Shadow
   @Final
   private Consumer<Optional<Throwable>> onFinish;
   @Shadow
   @Final
   private boolean fadeIn;
   @Shadow
   private float currentProgress;
   @Shadow
   private long fadeOutStart;
   @Shadow
   private long fadeInStart;

   @Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
   private void night$renderLoadingScreen(GuiGraphicsExtractor var1, int var2, int var3, float var4, CallbackInfo var5) {
      var5.cancel();
      int var6 = mc.getWindow().getGuiScaledWidth();
      int var7 = mc.getWindow().getGuiScaledHeight();
      long var8 = Util.getMillis();
      if (this.fadeIn && this.fadeInStart == -1L) {
         this.fadeInStart = var8;
      }

      float var10 = this.fadeOutStart > -1L ? (float)(var8 - this.fadeOutStart) / 1000.0F : -1.0F;
      float var11 = this.fadeInStart > -1L ? (float)(var8 - this.fadeInStart) / 500.0F : -1.0F;
      float var12 = 1.0F;
      if (var10 >= 1.0F) {
         if (mc.gui.screen() != null) {
            mc.gui.screen().init(mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight());
         }

         try {
            this.reload.checkExceptions();
            this.onFinish.accept(Optional.empty());
         } catch (Throwable var19) {
            this.onFinish.accept(Optional.of(var19));
         }

         mc.gui.setOverlay(null);
      } else {
         if (var10 >= 0.0F) {
            var12 = 1.0F - Mth.clamp(var10, 0.0F, 1.0F);
         } else if (this.fadeIn && var11 >= 0.0F) {
            var12 = Mth.clamp(var11, 0.0F, 1.0F);
         }

         MenuBackgroundShader.a(var1, var6, var7, var12, 0.0F, this.reload.isDone());
         float var13 = this.reload.getActualProgress();
         this.currentProgress = Mth.clamp(this.currentProgress * 0.95F + var13 * 0.05F, 0.0F, 1.0F);
         int var14 = Math.min(var6 - 40, 260);
         int var15 = var6 / 2 - var14 / 2;
         int var16 = var7 / 2 + 30;
         Render2DUtil.a(var1, var15 - 1, var16 - 1, var15 + var14 + 1, var16 + 4 + 1, new Color(20, 20, 25, (int)(200.0F * var12)));
         int var17 = (int)(var14 * this.currentProgress);
         if (var17 > 0) {
            Color var18 = ClickGuiScreen.a(0, (int)(240.0F * var12));
            Render2DUtil.a(var1, var15, var16, var15 + var17, var16 + 4, var18);
         }

         if (this.reload.isDone() && this.fadeOutStart == -1L) {
            this.fadeOutStart = var8;
         }
      }
   }
}
