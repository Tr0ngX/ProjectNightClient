package night.mixins.accessors;

import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerboundCustomPayloadPacket.class)
public interface CustomPayloadC2SPacketAccessor {
   @Mutable
   @Accessor("payload")
   void setPayload(CustomPacketPayload var1);
}
