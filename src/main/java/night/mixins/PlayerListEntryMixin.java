package night.mixins;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.core.ClientAsset.ResourceTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerSkin;
import night.Night;
import night.module.impl.core.Capes;
import night.util.MinecraftInstance;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerInfo.class)
public class PlayerListEntryMixin implements MinecraftInstance {
   @Shadow
   @Final
   private GameProfile profile;

   @Inject(method = "getSkin", at = @At("TAIL"), cancellable = true)
   private void getSkinTextures(CallbackInfoReturnable<PlayerSkin> var1) {
      if (this.profile.name().equals(mc.player.getGameProfile().name())
         && this.profile.id().equals(mc.player.getGameProfile().id())
         && Night.moduleManager.getModule(Capes.class).isEnabled()
         && Night.moduleManager.getModule(Capes.class).v() != null) {
         Identifier var2 = Night.moduleManager.getModule(Capes.class).v();
         PlayerSkin var3 = (PlayerSkin)var1.getReturnValue();
         var1.setReturnValue(new PlayerSkin(var3.body(), new ResourceTexture(var2), var3.elytra(), var3.model(), var3.secure()));
      }
   }
}
