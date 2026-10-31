package night.mixins;

import java.util.List;
import java.util.Map;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.FallbackResourceManager;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import night.util.resource.NightBuiltinResourcePack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiPackResourceManager.class)
public class MultiPackResourceManagerMixin {
   @Shadow
   @Final
   private Map<String, FallbackResourceManager> namespacedManagers;

   @Inject(method = "<init>", at = @At("RETURN"))
   private void night$injectClientPack(PackType var1, List<PackResources> var2, CallbackInfo var3) {
      if (var1 == PackType.CLIENT_RESOURCES) {
         this.namespacedManagers.computeIfAbsent("night", var1x -> new FallbackResourceManager(var1, "night")).push(NightBuiltinResourcePack.a);
      }
   }
}
