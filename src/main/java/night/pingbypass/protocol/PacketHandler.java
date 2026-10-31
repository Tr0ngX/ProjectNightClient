package night.pingbypass.protocol;

import net.minecraft.network.Connection;

@FunctionalInterface
public interface PacketHandler<T extends PingBypassPacket> {
   void handle(T var1, Connection var2);
}
