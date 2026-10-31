package night.util.rotation;

import lombok.Generated;
import net.minecraft.world.entity.Entity;

public class Rotation {
   private float yaw;
   private float pitch;

   public Rotation(float var1, float var2) {
      this.yaw = var1;
      this.pitch = var2;
   }

   public Rotation(Entity var1) {
      float var2 = var1.getYRot();
      float var3 = var1.getXRot();
      this(var2, var3);
   }

   @Generated
   public float getYaw() {
      return this.yaw;
   }

   @Generated
   public float getPitch() {
      return this.pitch;
   }

   @Generated
   public void setYaw(float var1) {
      this.yaw = var1;
   }

   @Generated
   public void setPitch(float var1) {
      this.pitch = var1;
   }
}
