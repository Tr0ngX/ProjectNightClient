package night.mixins;

import java.net.InetSocketAddress;
import java.util.Optional;
import net.minecraft.client.multiplayer.resolver.ResolvedServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerNameResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerNameResolver.class)
public class ServerNameResolverMixin {
   private static final Logger LOGGER = LoggerFactory.getLogger("Night/DnsFix");

   @Inject(method = "resolveAddress", at = @At("RETURN"), cancellable = true)
   private void resolveAddress(ServerAddress var1, CallbackInfoReturnable<Optional<ResolvedServerAddress>> var2) {
      if (!((Optional)var2.getReturnValue()).isPresent()) {
         try {
            InetSocketAddress var3 = new InetSocketAddress(var1.getHost(), var1.getPort());
            if (var3.isUnresolved()) {
               LOGGER.warn("[PB] java.net fallback resolve of {} also unresolved", var1.getHost());
               return;
            }

            LOGGER.info("[PB] Resolved {} via java.net fallback: {}", var1.getHost(), var3);
            var2.setReturnValue(Optional.of(ResolvedServerAddress.from(var3)));
         } catch (Exception var4) {
            LOGGER.warn("[PB] java.net fallback resolve of {} failed", var1.getHost(), var4);
         }
      }
   }
}
