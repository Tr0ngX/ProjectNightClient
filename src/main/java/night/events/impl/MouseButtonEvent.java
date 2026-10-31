package night.events.impl;

import lombok.Generated;
import night.events.Event;

public class MouseButtonEvent extends Event {
   private final int button;

   @Generated
   public int getButton() {
      return this.button;
   }

   @Generated
   public MouseButtonEvent(int var1) {
      this.button = var1;
   }
}
