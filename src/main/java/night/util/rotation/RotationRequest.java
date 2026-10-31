package night.util.rotation;

import lombok.Generated;
import night.module.Module;

public class RotationRequest {
   private float yaw;
   private float pitch;
   private final Module module;
   private final int priority;
   private long timestamp;

   public RotationRequest(float var1, float var2, int var3) {
      this.yaw = var1;
      this.pitch = var2;
      this.module = null;
      this.priority = var3;
      long var4 = System.currentTimeMillis();
      this.timestamp = var4;
   }

   public RotationRequest(float var1, float var2, Module var3, int var4) {
      this.yaw = var1;
      this.pitch = var2;
      this.module = var3;
      this.priority = var4;
      long var5 = System.currentTimeMillis();
      this.timestamp = var5;
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
   public Module getModule() {
      return this.module;
   }

   @Generated
   public int getPriority() {
      return this.priority;
   }

   @Generated
   public long getTimestamp() {
      return this.timestamp;
   }
}
