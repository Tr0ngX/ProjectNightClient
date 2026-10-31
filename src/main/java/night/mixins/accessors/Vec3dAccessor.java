package night.mixins.accessors;

import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Vec3.class)
public interface Vec3dAccessor {
   @Accessor("x")
   @Mutable
   void setX(double var1);

   @Accessor("y")
   @Mutable
   void setY(double var1);

   @Accessor("z")
   @Mutable
   void setZ(double var1);
}
