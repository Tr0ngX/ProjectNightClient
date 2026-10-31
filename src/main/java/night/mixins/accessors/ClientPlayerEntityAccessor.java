package night.mixins.accessors;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Input;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LocalPlayer.class)
public interface ClientPlayerEntityAccessor {
   @Invoker("isMoving")
   boolean invokeIsWalking();

   @Invoker("isSprintingPossible")
   boolean invokeCanSprint(boolean var1);

   @Invoker("sendPosition")
   void invokeSendMovementPackets();

   @Accessor("lastOnGround")
   void setLastOnGround(boolean var1);

   @Accessor("yRotLast")
   void setLastYaw(float var1);

   @Accessor("xRotLast")
   void setLastPitch(float var1);

   @Accessor("lastSentInput")
   void setLastSentInput(Input var1);
}
