package night.mixins;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import night.Night;
import night.module.impl.core.ClickGuiModule;
import night.util.render.NeekeriPatternShader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractButton.class)
public abstract class ButtonWidgetMixin {
   @Inject(method = "extractDefaultSprite", at = @At("TAIL"))
   private void night$neekeriFill(GuiGraphicsExtractor var1, CallbackInfo var2) {
      ClickGuiModule var3 = Night.moduleManager.getModule(ClickGuiModule.class);
      if (!var3.fillMode.getValue().equalsIgnoreCase("Default")) {
         AbstractButton var4 = (AbstractButton)(Object)this;
         int var5 = Math.round(var3.neekeriOpacity.getValue().floatValue() / 100.0F * 255.0F);
         NeekeriPatternShader.a(var1, var4.getX(), var4.getY(), var4.getWidth(), var4.getHeight(), var5);
      }
   }
}
