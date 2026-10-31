package night.mixins;

import java.awt.Color;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.TotemParticle;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import night.Night;
import night.module.impl.visuals.NoRender;
import night.module.impl.visuals.ParticlesModule;
import night.util.MinecraftInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ParticleEngine.class)
public class ParticleManagerMixin implements MinecraftInstance {
   @Unique
   private static int night$totemCounter = 0;
   @Unique
   private static long night$lastTotemTime = 0L;

   @Inject(
      method = "createTrackingEmitter(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/particles/ParticleOptions;I)V",
      at = @At("HEAD"),
      cancellable = true
   )
   private void night$onCreateTrackingEmitter(Entity var1, ParticleOptions var2, int var3, CallbackInfo var4) {
      NoRender var5 = Night.moduleManager != null ? Night.moduleManager.getModule(NoRender.class) : null;
      if (var5 != null && var5.isEnabled() && var5.totemPop.getValue() && var2.getType() == ParticleTypes.TOTEM_OF_UNDYING && var1 == mc.player) {
         var4.cancel();
      }
   }

   @Inject(
      method = "createParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)Lnet/minecraft/client/particle/Particle;",
      at = @At("HEAD"),
      cancellable = true
   )
   private void addParticle(
      ParticleOptions var1, double var2, double var4, double var6, double var8, double var10, double var12, CallbackInfoReturnable<Particle> var14
   ) {
      NoRender var15 = Night.moduleManager != null ? Night.moduleManager.getModule(NoRender.class) : null;
      if (var15 != null && var15.isEnabled() && var15.explosions.getValue() && var1.getType() == ParticleTypes.EXPLOSION) {
         var14.cancel();
      } else {
         ParticlesModule var16 = Night.moduleManager != null ? Night.moduleManager.getModule(ParticlesModule.class) : null;
         if (var16 != null && var16.isEnabled() && var16.totem.getValue() && var1.getType() == ParticleTypes.TOTEM_OF_UNDYING) {
            if (var16.totemMode.getValue().equalsIgnoreCase("Custom")) {
               var14.cancel();
            } else if (var16.totemMode.getValue().equalsIgnoreCase("Vanilla")) {
               long var17 = System.currentTimeMillis();
               if (var17 - night$lastTotemTime > 400L) {
                  night$totemCounter = 0;
                  night$lastTotemTime = var17;
               }

               if (night$totemCounter >= var16.totemNumber.getValue().intValue()) {
                  var14.cancel();
                  return;
               }

               night$totemCounter++;
            }
         }
      }
   }

   @Inject(method = "createParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)Lnet/minecraft/client/particle/Particle;", at = @At("RETURN"))
   private void onCreatedParticle(
      ParticleOptions var1, double var2, double var4, double var6, double var8, double var10, double var12, CallbackInfoReturnable<Particle> var14
   ) {
      if (var1.getType() == ParticleTypes.TOTEM_OF_UNDYING) {
         ParticlesModule var15 = Night.moduleManager != null ? Night.moduleManager.getModule(ParticlesModule.class) : null;
         if (var15 != null && var15.isEnabled() && var15.totem.getValue() && var15.totemMode.getValue().equalsIgnoreCase("Vanilla")) {
            Particle var16 = (Particle)var14.getReturnValue();
            if (var16 instanceof TotemParticle var17) {
               Color var18 = var15.vanillaColorA.getColor();
               Color var19 = var15.vanillaColorB.getColor();
               var17.setColor(var18.getRGB());
               var17.setFadeColor(var19.getRGB());
            }
         }
      }
   }
}
