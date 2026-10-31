package night.events.impl;

import lombok.Generated;
import night.events.Event;

public class ReachDistanceEvent extends Event {
   private final float reach;

   @Generated
   public ReachDistanceEvent(float var1) {
      this.reach = var1;
   }
}
