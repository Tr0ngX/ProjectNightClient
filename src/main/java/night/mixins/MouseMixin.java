package night.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import night.Night;
import night.events.impl.MouseButtonEvent;
import night.events.impl.MouseUpdateEvent;
import night.module.impl.combat.Mainhand;
import night.module.impl.kingmc.KingMCTotem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseMixin {
   @Shadow
   @Final
   private Minecraft minecraft;

   @Inject(method = "onButton", at = @At("HEAD"))
   private void onMouseButton(long var1, MouseButtonInfo var3, int var4, CallbackInfo var5) {
      int var6 = var3.button();
      int var7 = var3.modifiers();
      Night.EVENT_BUS.post(new MouseUpdateEvent(var6, var4, var7));
      if (var1 == this.minecraft.getWindow().handle() && var4 == 1 && this.minecraft.gui.screen() == null) {
         Night.EVENT_BUS.post(new MouseButtonEvent(var6));
      }
   }

   @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
   private void night$onScroll(long var1, double var3, double var5, CallbackInfo var7) {
      if (this.minecraft.gui.screen() == null && var1 == this.minecraft.getWindow().handle() && Night.moduleManager != null) {
         Mainhand var8 = Night.moduleManager.getModule(Mainhand.class);
         if (var8 != null && var8.x()) {
            var7.cancel();
         }
      }
   }

   @WrapOperation(method = "onScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/ScrollWheelHandler;getNextScrollWheelSelection(DII)I"))
   private int night$lockScrollRange(double var1, int var3, int var4, Operation<Integer> var5) {
      int var6 = (Integer)var5.call(new Object[]{var1, var3, var4});
      if (Night.moduleManager == null) {
         return var6;
      }

      KingMCTotem var7 = Night.moduleManager.getModule(KingMCTotem.class);
      return var7 != null ? var7.a((int)Math.signum(var1), var3, var6) : var6;
   }
}
