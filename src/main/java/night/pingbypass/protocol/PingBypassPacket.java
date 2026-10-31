package night.pingbypass.protocol;

import net.minecraft.network.FriendlyByteBuf;

public abstract class PingBypassPacket {
   public abstract int getPacketId();

   public abstract void write(FriendlyByteBuf var1);
}
