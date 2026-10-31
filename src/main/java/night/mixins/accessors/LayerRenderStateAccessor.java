package night.mixins.accessors;

import com.mojang.blaze3d.vertex.PoseStack.Pose;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.List;
import net.minecraft.client.renderer.item.ItemStackRenderState.FoilType;
import net.minecraft.client.renderer.item.ItemStackRenderState.LayerRenderState;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LayerRenderState.class)
public interface LayerRenderStateAccessor {
   @Accessor("quads")
   List<BakedQuad> night$getQuads();

   @Accessor("foilType")
   FoilType night$getFoilType();

   @Accessor("tintLayers")
   IntList night$getTintLayers();

   @Invoker("applyTransform")
   void night$applyTransform(Pose var1);
}
