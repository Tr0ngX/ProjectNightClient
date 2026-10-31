package night.events.impl;

import lombok.Generated;
import net.minecraft.network.protocol.Packet;
import night.events.Event;

public class PacketSendEvent extends Event {
   private final Packet<?> packet;

   @Generated
   public Packet<?> getPacket() {
      return this.packet;
   }

   @Generated
   public PacketSendEvent(Packet<?> var1) {
      this.packet = var1;
   }

   public static class Post extends Event {
      private final Packet<?> packet;

      @Generated
      public Packet<?> getPacket() {
         return this.packet;
      }

      @Generated
      public Post(Packet<?> var1) {
         this.packet = var1;
      }
   }
}
