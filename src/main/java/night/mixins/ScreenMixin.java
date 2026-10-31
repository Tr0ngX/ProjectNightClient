package night.mixins;

import java.awt.Color;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.inventory.Slot;
import night.Night;
import night.mixins.accessors.AbstractContainerScreenAccessor;
import night.module.impl.combat.Mainhand;
import night.module.impl.core.MainMenuModule;
import night.module.impl.misc.ShulkerInfo;
import night.module.impl.visuals.NoRender;
import night.util.MinecraftInstance;
import night.util.color.ColorUtil;
import night.util.render.Render2DUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public class ScreenMixin implements MinecraftInstance {
   @Shadow
   public int width;
   @Shadow
   public int height;

   @Inject(method = "extractBackground", at = @At("HEAD"), cancellable = true)
   private void renderBackground(GuiGraphicsExtractor var1, int var2, int var3, float var4, CallbackInfo var5) {
      if (Night.moduleManager.getModule(MainMenuModule.class).isEnabled()
         && Night.moduleManager.getModule(MainMenuModule.class).mainMenu.getValue()
         && mc.level == null) {
         Render2DUtil.a(var1, 0.0F, 0.0F, this.width, this.height, new Color(25, 25, 25, 255));

         for (int var6 = 0; var6 < this.width; var6++) {
            Color var7 = ColorUtil.a(2L, 0.7F, 1.0F, 255, var6 * 5L);
            Render2DUtil.a(var1, var6, 0.0F, var6 + 1, 1.0F, var7);
         }

         var5.cancel();
      } else {
         if (Night.moduleManager.getModule(NoRender.class).isEnabled()
            && Night.moduleManager.getModule(NoRender.class).y.getValue()
            && ((Screen)(Object)this).isInGameUi()) {
            var5.cancel();
         }
      }
   }

   @Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
   private void night$hideLegitInventory(GuiGraphicsExtractor var1, int var2, int var3, float var4, CallbackInfo var5) {
      if (Mainhand.n && (Object)(Object)this instanceof InventoryScreen) {
         var5.cancel();
      }
   }

   @Inject(method = "extractRenderStateWithTooltipAndSubtitles", at = @At("TAIL"))
   private void night$shulkerInfoOnTop(GuiGraphicsExtractor var1, int var2, int var3, float var4, CallbackInfo var5) {
      if ((Object)(Object)this instanceof AbstractContainerScreen var7) {
         ShulkerInfo var9 = Night.moduleManager.getModule(ShulkerInfo.class);
         if (var9 != null && var9.isEnabled()) {
            Slot var8 = ((AbstractContainerScreenAccessor)var7).getHoveredSlot();
            if (var8 != null && !var8.getItem().isEmpty() && var7.getMenu().getCarried().isEmpty() && var9.a(var8.getItem())) {
               var9.a(var1, var2, var3, var8.getItem());
            }
         }
      }
   }
}
