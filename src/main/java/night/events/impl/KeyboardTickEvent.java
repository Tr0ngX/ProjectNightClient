package night.events.impl;

import lombok.Generated;
import night.events.Event;

public class KeyboardTickEvent extends Event {
   private float movementForward;
   private float movementSideways;

   @Generated
   public KeyboardTickEvent(float var1, float var2) {
      this.movementForward = var1;
      this.movementSideways = var2;
   }

   @Generated
   public float getMovementForward() {
      return this.movementForward;
   }

   @Generated
   public float getMovementSideways() {
      return this.movementSideways;
   }

   @Generated
   public void setMovementForward(float var1) {
      this.movementForward = var1;
   }

   @Generated
   public void setMovementSideways(float var1) {
      this.movementSideways = var1;
   }
}
