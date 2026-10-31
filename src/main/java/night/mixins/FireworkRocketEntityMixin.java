package night.mixins;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import night.Night;
import night.events.impl.FireworkTickEvent;
import night.util.MinecraftInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FireworkRocketEntity.class)
public class FireworkRocketEntityMixin implements MinecraftInstance {
   @Shadow
   private int life;

   @Inject(
      method = "tick",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/FireworkRocketEntity;updateRotation()V", shift = Shift.AFTER),
      cancellable = true
   )
   private void tick(CallbackInfo var1) {
      FireworkRocketEntity var2 = (FireworkRocketEntity)(Object)this;
      FireworkTickEvent var3 = new FireworkTickEvent(var2);
      Night.EVENT_BUS.post(var3);
      if (var3.isCancelled()) {
         var1.cancel();
         if (this.life == 0 && !var2.isSilent()) {
            mc.level.playSound(null, var2.getX(), var2.getY(), var2.getZ(), SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.AMBIENT, 3.0F, 1.0F);
         }

         this.life++;
         if (this.life % 2 < 2) {
            mc.level
               .addParticle(
                  ParticleTypes.FIREWORK,
                  var2.getX(),
                  var2.getY(),
                  var2.getZ(),
                  mc.level.getRandom().nextGaussian() * 0.05,
                  -var2.getDeltaMovement().y * 0.5,
                  mc.level.getRandom().nextGaussian() * 0.05
               );
         }
      }
   }
}
