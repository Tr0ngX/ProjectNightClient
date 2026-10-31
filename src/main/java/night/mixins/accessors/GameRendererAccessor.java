package night.mixins.accessors;

import com.mojang.blaze3d.resource.CrossFrameResourcePool;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.fog.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GameRenderer.class)
public interface GameRendererAccessor {
   @Accessor("resourcePool")
   CrossFrameResourcePool getPool();

   @Accessor("fogRenderer")
   FogRenderer getFogRenderer();
}
