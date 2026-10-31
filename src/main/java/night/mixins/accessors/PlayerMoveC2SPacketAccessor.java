package night.mixins.accessors;

import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerboundMovePlayerPacket.class)
public interface PlayerMoveC2SPacketAccessor {
   @Accessor("yRot")
   @Mutable
   void setYaw(float var1);

   @Accessor("xRot")
   @Mutable
   void setPitch(float var1);

   @Accessor("onGround")
   @Mutable
   void setOnGround(boolean var1);

   @Accessor("hasRot")
   @Mutable
   void setChangeLook(boolean var1);
}
