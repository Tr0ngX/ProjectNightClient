package night.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.social.SocialInteractionsScreen;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import night.Night;
import night.events.impl.Render2DEvent;
import night.gui.ClickGuiScreen;
import night.gui.screen.NightMainMenuScreen;
import night.module.access.AutoEnchantAccess;
import night.module.impl.core.MainMenuModule;
import night.module.impl.misc.AutoRespawn;
import night.module.impl.misc.FakePlayer;
import night.util.inventory.InventoryUtil;
import night.util.world.BlockInteractionUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class InGameHudMixin {
   @Shadow
   @Final
   private Minecraft minecraft;

   @Inject(
      method = "extractRenderState",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/gui/Hud;extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V",
         shift = Shift.AFTER
      )
   )
   private void render(DeltaTracker var1, boolean var2, boolean var3, CallbackInfo var4, @Local(ordinal = 0) GuiGraphicsExtractor var5) {
      if (!this.minecraft.gui.hud.isHidden()) {
         Night.EVENT_BUS.post(new Render2DEvent(var5, var1.getGameTimeDeltaPartialTick(true)));
      }
   }

   @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
   private void night$setScreen(Screen var1, CallbackInfo var2) {
      if (var1 instanceof SocialInteractionsScreen) {
         var2.cancel();
      } else {
         boolean var3 = var1 instanceof AbstractContainerScreen && !(var1 instanceof InventoryScreen) && !(var1 instanceof CreativeModeInventoryScreen);
         if (InventoryUtil.g && var3) {
            var2.cancel();
         } else if ((var1 == null || var1 instanceof PauseScreen) && this.minecraft.gui.screen() instanceof ClickGuiScreen var5 && !var5.a()) {
            var5.b();
            var2.cancel();
         } else if (Night.moduleManager != null
            && Night.moduleManager.getModuleByName("AutoEnchant") instanceof AutoEnchantAccess var6
            && var6.isEnabled()
            && var6.isSuppressingGui()
            && var6.shouldSuppressScreen(var1)) {
            var2.cancel();
         } else if (var3 && System.currentTimeMillis() - BlockInteractionUtil.b < 500L) {
            var2.cancel();
            if (this.minecraft.getConnection() != null && this.minecraft.player != null && this.minecraft.player.containerMenu != null) {
               this.minecraft.getConnection().send(new ServerboundContainerClosePacket(this.minecraft.player.containerMenu.containerId));
            }
         } else if (Night.moduleManager != null) {
            if (var1 instanceof DeathScreen && this.minecraft.player != null) {
               FakePlayer var9 = Night.moduleManager.getModule(FakePlayer.class);
               if (var9 != null && var9.isEnabled()) {
                  var9.setEnabled(false);
               }

               AutoRespawn var7 = Night.moduleManager.getModule(AutoRespawn.class);
               if (var7 != null && var7.isEnabled()) {
                  this.minecraft.player.respawn();
                  var2.cancel();
                  return;
               }
            }

            if (var1 instanceof TitleScreen) {
               Night.checkForUpdates();
               MainMenuModule var10 = Night.moduleManager.getModule(MainMenuModule.class);
               if (var10 != null && var10.isEnabled() && var10.mainMenu.getValue()) {
                  this.minecraft.gui.setScreen(new NightMainMenuScreen());
                  var2.cancel();
               }
            }
         }
      }
   }
}
