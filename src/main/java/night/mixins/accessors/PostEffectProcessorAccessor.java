package night.mixins.accessors;

import com.mojang.blaze3d.pipeline.RenderTarget;
import java.util.List;
import java.util.Map;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PostChain.class)
public interface PostEffectProcessorAccessor {
   @Accessor("passes")
   List<PostPass> getPasses();

   @Accessor("persistentTargets")
   Map<Identifier, RenderTarget> getPersistentTargets();
}
