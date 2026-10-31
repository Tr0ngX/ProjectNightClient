package night.events.impl;

import lombok.Generated;
import net.minecraft.world.phys.Vec3;
import night.events.Event;

public class UpdateVelocityEvent extends Event {
   private final Vec3 movementInput;
   private final float speed;
   private Vec3 velocity;

   @Generated
   public Vec3 getMovementInput() {
      return this.movementInput;
   }

   @Generated
   public float getSpeed() {
      return this.speed;
   }

   @Generated
   public Vec3 getVelocity() {
      return this.velocity;
   }

   @Generated
   public void setVelocity(Vec3 var1) {
      this.velocity = var1;
   }

   @Generated
   public UpdateVelocityEvent(Vec3 var1, float var2) {
      this.movementInput = var1;
      this.speed = var2;
   }
}
