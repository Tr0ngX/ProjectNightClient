package night.mixins;

import java.util.ArrayList;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.scores.PlayerTeam;
import night.Night;
import night.module.impl.misc.ExtraTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerTabOverlay.class)
public abstract class PlayerListHudMixin {
   @Shadow
   private Component decorateName(PlayerInfo var1, MutableComponent var2) {
      throw new UnsupportedOperationException();
   }

   @ModifyConstant(method = "getPlayerInfos", constant = @Constant(longValue = 80L), require = 0)
   private long extraTabLimit(long var1) {
      ExtraTab var3 = Night.moduleManager.getModule(ExtraTab.class);
      return var3.isEnabled() ? var3.limit.getValue().longValue() : var1;
   }

   @Inject(method = "getNameForDisplay", at = @At("HEAD"), cancellable = true)
   private void getPlayerName(PlayerInfo var1, CallbackInfoReturnable<Component> var2) {
      if (Night.moduleManager.getModule(ExtraTab.class).isEnabled()
         && Night.moduleManager.getModule(ExtraTab.class).friends.getValue()
         && Night.friendManager.isFriend(var1.getProfile().name())) {
         if (var1.getTabListDisplayName() != null) {
            MutableComponent var3 = Component.empty();
            ArrayList var4 = new ArrayList();
            var4.add(var1.getTabListDisplayName().plainCopy());
            var4.addAll(var1.getTabListDisplayName().getSiblings());

            for (Component var6 : (Iterable<Component>)(Object)var4) {
               if (var6.getString().equals(var1.getProfile().name())) {
                  var3.append(Component.literal(var1.getProfile().name()).withStyle(ChatFormatting.AQUA));
               } else if (var6.getString().equals("] " + var1.getProfile().name())) {
                  var3.append(
                     Component.literal("] ").withStyle(ChatFormatting.WHITE).append(Component.literal(var1.getProfile().name()).withStyle(ChatFormatting.AQUA))
                  );
               } else {
                  var3.append(var6);
               }
            }

            var2.setReturnValue(this.decorateName(var1, var3));
            return;
         }

         var2.setReturnValue(this.decorateName(var1, PlayerTeam.formatNameForTeam(var1.getTeam(), Component.literal(var1.getProfile().name()))));
      }
   }
}
