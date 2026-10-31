package night.events.impl;

import lombok.Generated;
import night.events.Event;
import night.module.Module;

public class ModuleToggleEvent extends Event {
   private final Module module;
   private final boolean enabled;

   @Generated
   public ModuleToggleEvent(Module var1, boolean var2) {
      this.module = var1;
      this.enabled = var2;
   }
}
