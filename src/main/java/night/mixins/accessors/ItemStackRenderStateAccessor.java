package night.mixins.accessors;

import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState.LayerRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemStackRenderState.class)
public interface ItemStackRenderStateAccessor {
   @Accessor("layers")
   LayerRenderState[] night$getLayers();

   @Accessor("activeLayerCount")
   int night$getActiveLayerCount();
}
