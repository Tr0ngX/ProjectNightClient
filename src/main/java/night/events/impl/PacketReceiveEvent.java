package night.events.impl;

import lombok.Generated;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import night.events.Event;

public class PacketReceiveEvent extends Event {
   private final Packet<?> packet;
   private final Connection connection;

   @Generated
   public Packet<?> getPacket() {
      return this.packet;
   }

   @Generated
   public Connection getConnection() {
      return this.connection;
   }

   @Generated
   public PacketReceiveEvent(Packet<?> var1, Connection var2) {
      this.packet = var1;
      this.connection = var2;
   }
}
