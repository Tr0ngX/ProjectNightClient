package night.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.world.entity.LivingEntity;
import night.Night;
import night.module.impl.movement.ElytraFly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidMobRenderer.class)
public abstract class HumanoidMobRendererMixin {
   @Inject(method = "extractHumanoidRenderState", at = @At("TAIL"))
   private static void night$hideControlRocketGlide(LivingEntity var0, HumanoidRenderState var1, float var2, ItemModelResolver var3, CallbackInfo var4) {
      if (var0 == Minecraft.getInstance().player) {
         ElytraFly var5 = Night.moduleManager.getModule(ElytraFly.class);
         if (var5.isEnabled() && var5.mode.getValue().equalsIgnoreCase("ControlRocket")) {
            var1.isFallFlying = false;
            var1.speedValue = 1.0F;
         }
      }
   }
}
