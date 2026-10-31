package night.mixins;

import java.awt.Color;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import night.Night;
import night.gui.screen.AccountsButtonHelper;
import night.util.MinecraftInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen implements MinecraftInstance {
   @Shadow
   private boolean fading;
   @Shadow
   private long fadeInStart;

   protected TitleScreenMixin(Component var1) {
      super(var1);
   }

   @Inject(method = "init", at = @At("TAIL"))
   private void init(CallbackInfo var1) {
      this.addRenderableWidget(AccountsButtonHelper.a(8, 8, 75, 20, this));
   }

   @Inject(method = "extractRenderState", at = @At("TAIL"))
   private void render$drawTextWithShadow(GuiGraphicsExtractor var1, int var2, int var3, float var4, CallbackInfo var5) {
      if (!Night.updateStatus.equalsIgnoreCase("none")) {
         String var6 = "";
         String var7 = "";
         Color var8 = Color.WHITE;
         if (Night.updateStatus.equalsIgnoreCase("update-available")) {
            var7 = "An update is available for Night.";
            var6 = "Please restart the game to apply changes.";
            var8 = Color.ORANGE;
         }

         if (Night.updateStatus.equalsIgnoreCase("failed-connection")) {
            var7 = "Failed to connect to Night's servers.";
            var6 = "Please make sure you have a working internet connection.";
            var8 = Color.RED;
         }

         if (Night.updateStatus.equalsIgnoreCase("failed")) {
            var7 = "Failed to update Night.";
            var6 = "Please make sure the auto-updater is working properly.";
            var8 = Color.RED;
         }

         if (Night.updateStatus.equalsIgnoreCase("up-to-date")) {
            var6 = "Night is on the latest version.";
         }

         if (!var6.isEmpty()) {
            float var9 = 1.0F;
            if (this.fading) {
               float var10 = (float)(Util.getMillis() - this.fadeInStart) / 2000.0F;
               if (var10 <= 1.0F) {
                  var10 = Mth.clamp(var10, 0.0F, 1.0F);
                  var9 = Mth.clampedMap(var10, 0.5F, 1.0F, 0.0F, 1.0F);
               }
            }

            int var12 = Mth.ceil(var9 * 255.0F) << 24;
            if ((var12 & -67108864) != 0) {
               if (!var7.isEmpty()) {
                  var1.text(mc.font, var7, this.width / 2 - mc.font.width(var7) / 2, this.height - 30, var8.getRGB() | var12, true);
               }

               var1.text(mc.font, var6, this.width / 2 - mc.font.width(var6) / 2, this.height - 20, var8.getRGB() | var12, true);
            }
         }
      }
   }
}
