package night.mixins;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import night.Night;
import night.manager.ChatManager;
import night.module.impl.core.HudModule;
import night.module.impl.movement.ElytraFly;
import night.module.impl.visuals.NoRender;
import night.util.render.ItemRenderCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public class HudMixin {
   private static final Identifier POWDER_SNOW_OUTLINE_LOCATION = Identifier.withDefaultNamespace("textures/misc/powder_snow_outline.png");

   @Inject(method = "extractEffects", at = @At("HEAD"), cancellable = true)
   private void renderStatusEffectOverlay(GuiGraphicsExtractor var1, DeltaTracker var2, CallbackInfo var3) {
      if (Night.moduleManager != null
         && Night.moduleManager.getModule(HudModule.class).isEnabled()
         && Night.moduleManager.getModule(HudModule.class).vanillaPotions.getValue().equalsIgnoreCase("Hide")) {
         var3.cancel();
      }
   }

   @Inject(method = "extractPortalOverlay", at = @At("HEAD"), cancellable = true)
   private void renderPortalOverlay(GuiGraphicsExtractor var1, float var2, CallbackInfo var3) {
      if (Night.moduleManager.getModule(NoRender.class).isEnabled() && Night.moduleManager.getModule(NoRender.class).portalOverlay.getValue()) {
         var3.cancel();
      }
   }

   @Inject(method = "extractVignette", at = @At("HEAD"), cancellable = true)
   private void renderVignetteOverlay(GuiGraphicsExtractor var1, Entity var2, CallbackInfo var3) {
      if (Night.moduleManager.getModule(NoRender.class).isEnabled() && Night.moduleManager.getModule(NoRender.class).vignette.getValue()) {
         var3.cancel();
      }
   }

   @Inject(method = "extractSelectedItemName", at = @At("HEAD"), cancellable = true)
   private void renderSelectedItemName(GuiGraphicsExtractor var1, CallbackInfo var2) {
      if (Night.moduleManager.getModule(NoRender.class).isEnabled() && Night.moduleManager.getModule(NoRender.class).itemName.getValue()) {
         var2.cancel();
      }
   }

   @Inject(method = "extractScoreboardSidebar", at = @At("HEAD"), cancellable = true)
   private void renderScoreboardSidebar(GuiGraphicsExtractor var1, DeltaTracker var2, CallbackInfo var3) {
      if (Night.moduleManager != null
         && Night.moduleManager.getModule(NoRender.class).isEnabled()
         && Night.moduleManager.getModule(NoRender.class).scoreboard.getValue()) {
         var3.cancel();
      }
   }

   @WrapWithCondition(
      method = "extractCameraOverlays",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/gui/Hud;extractTextureOverlay(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/resources/Identifier;F)V"
      )
   )
   private boolean renderTextureOverlay(Hud var1, GuiGraphicsExtractor var2, Identifier var3, float var4) {
      if (var3.equals(POWDER_SNOW_OUTLINE_LOCATION)) {
         return !Night.moduleManager.getModule(NoRender.class).isEnabled() || !Night.moduleManager.getModule(NoRender.class).snowOverlay.getValue();
      } else {
         return !var3.getPath().contains("pumpkin")
            ? true
            : !Night.moduleManager.getModule(NoRender.class).isEnabled() || !Night.moduleManager.getModule(NoRender.class).pumpkinOverlay.getValue();
      }
   }

   @Inject(method = "extractChat", at = @At("HEAD"))
   private void night$onExtractChatHead(GuiGraphicsExtractor var1, DeltaTracker var2, CallbackInfo var3) {
      ChatManager.suppressSend = true;
   }

   @Inject(method = "extractChat", at = @At("RETURN"))
   private void night$onExtractChatReturn(GuiGraphicsExtractor var1, DeltaTracker var2, CallbackInfo var3) {
      ChatManager.suppressSend = false;
   }

   @Redirect(
      method = "extractItemHotbar",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;getItem(I)Lnet/minecraft/world/item/ItemStack;")
   )
   private ItemStack night$grimHotbarVisual(Inventory var1, int var2) {
      ItemStack var3 = var1.getItem(var2);
      if (Night.moduleManager == null) {
         return var3;
      }

      ElytraFly var4 = Night.moduleManager.getModule(ElytraFly.class);
      return var4.L() == var2 ? var4.M() : var3;
   }

   @Redirect(
      method = "extractSlot",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;item(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;III)V"
      )
   )
   private void night$extractSlotCached(GuiGraphicsExtractor var1, LivingEntity var2, ItemStack var3, int var4, int var5, int var6) {
      ItemRenderCache.a(var1, var2, var3, var4, var5, var6);
   }

   @Inject(method = "clearCache", at = @At("HEAD"))
   private void night$onClearCache(CallbackInfo var1) {
      ItemRenderCache.a();
   }

   @Inject(method = "onDisconnected", at = @At("HEAD"))
   private void night$onDisconnected(CallbackInfo var1) {
      ItemRenderCache.a();
   }
}
