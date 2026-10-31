package night.mixins;

import net.minecraft.client.multiplayer.resolver.AddressCheck;
import net.minecraft.client.multiplayer.resolver.ResolvedServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AddressCheck.class)
public interface AddressCheckMixin {
   @Inject(method = "createFromService", at = @At("HEAD"), cancellable = true)
   private static void onCreateFromService(CallbackInfoReturnable<AddressCheck> var0) {
      var0.setReturnValue(new AddressCheck() {
         public boolean isAllowed(ResolvedServerAddress var1) {
            return true;
         }

         public boolean isAllowed(ServerAddress var1) {
            return true;
         }
      });
   }
}
