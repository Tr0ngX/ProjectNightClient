package night.events.impl;

import lombok.Generated;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
import night.events.Event;

public class PlayerMoveEvent extends Event {
   private final MoverType type;
   private Vec3 movement;

   public void setY(double var1) {
      double var3 = var1;
      double var5 = this.movement.y();
      double var7 = this.movement.z();
      this.movement = new Vec3(var3, var5, var7);
   }

   public void setX(double var1) {
      double var3 = this.movement.x();
      double var5 = this.movement.y();
      double var7 = var1;
      this.movement = new Vec3(var3, var5, var7);
   }

   @Generated
   public PlayerMoveEvent(MoverType var1, Vec3 var2) {
      this.type = var1;
      this.movement = var2;
   }

   @Generated
   public MoverType getType() {
      return this.type;
   }

   @Generated
   public Vec3 getMovement() {
      return this.movement;
   }

   @Generated
   public void setMovement(Vec3 var1) {
      this.movement = var1;
   }
}
