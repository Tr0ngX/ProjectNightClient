package night.mixins.accessors;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Entity.class)
public interface EntityAccessor {
   @Invoker("getInputVector")
   static Vec3 invokeMovementInputToVelocity(Vec3 var0, float var1, float var2) {
      throw new AssertionError();
   }

   @Invoker("setSharedFlag")
   void invokeSetSharedFlag(int var1, boolean var2);

   @Accessor("yRot")
   float getRawYRot();

   @Accessor("xRot")
   float getRawXRot();

   @Accessor("yRot")
   void setRawYRot(float var1);

   @Accessor("xRot")
   void setRawXRot(float var1);
}
