package night.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import night.Night;
import night.module.impl.movement.ElytraFly;
import night.module.impl.visuals.NoRender;
import night.util.MinecraftInstance;
import night.util.duck.EntityRenderStateSelfDuck;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>>
   extends EntityRenderer<T, S>
   implements MinecraftInstance {
   private static final double SAME_SPOT_RANGE_SQ = 1.0;

   public LivingEntityRendererMixin(Context var1) {
      super(var1);
   }

   @Inject(method = "submit", at = @At("HEAD"), cancellable = true)
   private void submit(S var1, PoseStack var2, SubmitNodeCollector var3, CameraRenderState var4, CallbackInfo var5) {
      NoRender var6 = Night.moduleManager.getModule(NoRender.class);
      if (var6.isEnabled()) {
         if (var6.corpses.getValue() && var1.deathTime > 0.0F) {
            var5.cancel();
         } else {
            if (var6.x.getValue() && var1 instanceof AvatarRenderState && !((EntityRenderStateSelfDuck)var1).night$isSelf() && mc.player != null) {
               double var7 = var1.x - mc.player.getX();
               double var9 = var1.z - mc.player.getZ();
               if (var7 * var7 + var9 * var9 <= 1.0) {
                  var5.cancel();
               }
            }
         }
      }
   }

   @Inject(method = "extractRenderState", at = @At("TAIL"))
   private void night$extractRenderState(T var1, S var2, float var3, CallbackInfo var4) {
      ((EntityRenderStateSelfDuck)var2).night$setSelf(var1 == mc.player);
      if (var1 == mc.player && Night.rotationManager.hasRecentRotation()) {
         var2.bodyRot = Night.rotationManager.getInterpolatedRotation()[0];
         var2.yRot = Mth.wrapDegrees(Night.rotationManager.getInterpolatedRotation()[0] - var2.bodyRot);
         var2.xRot = Night.rotationManager.getInterpolatedRotation()[1];
      } else {
         if (var1 == mc.player && Night.moduleManager != null) {
            ElytraFly var5 = Night.moduleManager.getModule(ElytraFly.class);
            if (var5 != null && var5.isEnabled() && var5.mode.getValue().equalsIgnoreCase("Bounce") && var5.autoPitch.getValue()) {
               var2.xRot = var5.pitch.getValue().floatValue();
            }
         }
      }
   }
}
