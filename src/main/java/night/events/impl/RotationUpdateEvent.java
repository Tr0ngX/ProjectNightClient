package night.events.impl;

import lombok.Generated;
import night.events.Event;
import night.module.Module;
import night.util.rotation.Rotation;

public class RotationUpdateEvent extends Event {
   private final Rotation rotation;
   private Module module;

   public RotationUpdateEvent(Rotation var1) {
      this.rotation = var1;
   }

   public void setYaw(float var1) {
      this.setCancelled(true);
      this.rotation.setYaw(var1);
   }

   public void setPitch(float var1) {
      this.setCancelled(true);
      this.rotation.setPitch(var1);
   }

   @Generated
   public Module getModule() {
      return this.module;
   }

   @Generated
   public void setModule(Module var1) {
      this.module = var1;
   }
}
