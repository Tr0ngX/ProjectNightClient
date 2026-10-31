package night.mixins.accessors;

import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import net.minecraft.client.renderer.RenderPipelines;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderPipelines.class)
public interface RenderPipelinesAccessor {
   @Accessor("LINES_SNIPPET")
   static Snippet getLinesSnippet() {
      throw new AssertionError();
   }

   @Accessor("DEBUG_FILLED_SNIPPET")
   static Snippet getDebugFilledSnippet() {
      throw new AssertionError();
   }

   @Accessor("ITEM_SNIPPET")
   static Snippet getItemSnippet() {
      throw new AssertionError();
   }

   @Accessor("ENTITY_SNIPPET")
   static Snippet getEntitySnippet() {
      throw new AssertionError();
   }

   @Accessor("GLOBALS_SNIPPET")
   static Snippet getGlobalsSnippet() {
      throw new AssertionError();
   }

   @Accessor("GUI_TEXTURED_SNIPPET")
   static Snippet getGuiTexturedSnippet() {
      throw new AssertionError();
   }
}
