package night.events.impl;

import lombok.Generated;
import night.events.Event;

public class MouseUpdateEvent extends Event {
   private final int button;
   private final int action;
   private final int modifiers;

   @Generated
   public MouseUpdateEvent(int var1, int var2, int var3) {
      this.button = var1;
      this.action = var2;
      this.modifiers = var3;
   }
}
