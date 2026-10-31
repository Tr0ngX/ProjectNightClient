package night.events.impl;

import lombok.Generated;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import night.events.Event;

public class ServerConnectEvent extends Event {
   private final ServerAddress address;
   private final ServerData info;

   @Generated
   public ServerConnectEvent(ServerAddress var1, ServerData var2) {
      this.address = var1;
      this.info = var2;
   }

   @Generated
   public ServerAddress getAddress() {
      return this.address;
   }

   @Generated
   public ServerData getInfo() {
      return this.info;
   }
}
