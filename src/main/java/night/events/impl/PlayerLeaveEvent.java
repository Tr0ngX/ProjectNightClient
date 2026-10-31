package night.events.impl;

import java.util.UUID;
import lombok.Generated;
import night.events.Event;

public class PlayerLeaveEvent extends Event {
   private final UUID uuid;
   private final String name;

   public PlayerLeaveEvent(UUID var1, String var2) {
      this.uuid = var1;
      this.name = var2;
   }

   @Generated
   public UUID getUuid() {
      return this.uuid;
   }

   @Generated
   public String getName() {
      return this.name;
   }
}
