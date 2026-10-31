package night.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec2;
import night.Night;
import night.events.impl.InputUpdateEvent;
import night.events.impl.KeyboardTickEvent;
import night.module.access.AutoWalkAccess;
import night.module.access.FreecamAccess;
import night.module.access.SprintAccess;
import night.module.impl.movement.HighJump;
import night.utils.minecraft.BaritoneUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public class KeyboardInputMixin extends ClientInput {
   @Inject(method = "tick", at = @At("TAIL"))
   private void tick$TAIL(CallbackInfo var1) {
      KeyboardTickEvent var2 = new KeyboardTickEvent(this.moveVector.y, this.moveVector.x);
      Night.EVENT_BUS.post(var2);
      if (var2.isCancelled()) {
         this.moveVector = new Vec2(var2.getMovementSideways(), var2.getMovementForward());
      }

      if (Night.moduleManager != null) {
         FreecamAccess var3 = (FreecamAccess)Night.moduleManager.getModuleByName("Freecam");
         if (var3 != null && var3.isEnabled()) {
            this.keyPresses = new Input(false, false, false, false, false, false, false);
            this.moveVector = new Vec2(0.0F, 0.0F);
            return;
         }

         AutoWalkAccess var4 = (AutoWalkAccess)Night.moduleManager.getModuleByName("AutoWalk");
         if (var4 != null && var4.isEnabled() && !BaritoneUtils.isActive()) {
            boolean var5 = var4.isBackward();
            boolean var6 = !var5;
            this.keyPresses = new Input(
               var6, var5, this.keyPresses.left(), this.keyPresses.right(), this.keyPresses.jump(), this.keyPresses.shift(), this.keyPresses.sprint()
            );
            this.moveVector = new Vec2(this.moveVector.x, var5 ? -1.0F : 1.0F);
         }

         InputUpdateEvent var10 = new InputUpdateEvent(
            this.keyPresses.forward(),
            this.keyPresses.backward(),
            this.keyPresses.left(),
            this.keyPresses.right(),
            this.keyPresses.jump(),
            this.keyPresses.shift(),
            this.keyPresses.sprint()
         );
         Night.EVENT_BUS.post(var10);
         this.keyPresses = new Input(var10.up, var10.down, var10.left, var10.right, var10.jumping, var10.sneaking, var10.sprinting);
      }

      SprintAccess var7 = Night.moduleManager == null ? null : (SprintAccess)Night.moduleManager.getModuleByName("Sprint");
      if (var7 != null && var7.isGrimSprintActive()) {
         int var8 = var7.getStrafeSide();
         this.keyPresses = new Input(true, false, var8 > 0, var8 < 0, this.keyPresses.jump(), this.keyPresses.shift(), this.keyPresses.sprint());
         this.moveVector = var8 == 0 ? new Vec2(0.0F, 1.0F) : new Vec2(var8 > 0 ? 1.0F : -1.0F, 1.0F);
         Minecraft.getInstance().player.setSprinting(true);
      }

      if (HighJump.g) {
         Input var9 = this.keyPresses;
         this.keyPresses = new Input(var9.forward(), var9.backward(), var9.left(), var9.right(), false, var9.shift(), var9.sprint());
      }
   }
}
