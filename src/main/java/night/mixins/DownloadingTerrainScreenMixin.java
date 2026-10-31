package night.mixins;

import java.awt.Color;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.LevelLoadingScreen.Reason;
import net.minecraft.network.chat.Component;
import night.Night;
import night.module.impl.core.MainMenuModule;
import night.util.color.ColorUtil;
import night.util.render.Render2DUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelLoadingScreen.class)
public class DownloadingTerrainScreenMixin extends Screen {
   @Shadow
   @Final
   private Reason reason;

   protected DownloadingTerrainScreenMixin(Component var1) {
      super(var1);
   }

   @Inject(method = "extractBackground", at = @At("HEAD"), cancellable = true)
   private void renderBackground(GuiGraphicsExtractor var1, int var2, int var3, float var4, CallbackInfo var5) {
      if (this.reason.equals(Reason.OTHER)
         && Night.moduleManager.getModule(MainMenuModule.class).isEnabled()
         && Night.moduleManager.getModule(MainMenuModule.class).mainMenu.getValue()) {
         Render2DUtil.a(var1, 0.0F, 0.0F, this.width, this.height, new Color(25, 25, 25, 255));

         for (int var6 = 0; var6 < this.width; var6++) {
            Color var7 = ColorUtil.a(2L, 0.7F, 1.0F, 255, var6 * 5L);
            Render2DUtil.a(var1, var6, 0.0F, var6 + 1, 1.0F, var7);
         }

         var5.cancel();
      }
   }
}
