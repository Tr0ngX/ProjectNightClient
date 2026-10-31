package night.events.impl;

import lombok.Generated;
import night.events.Event;

public class CameraBobEvent extends Event {
   private final float bobMultiplier;

   @Generated
   public CameraBobEvent(float var1) {
      this.bobMultiplier = var1;
   }
}
