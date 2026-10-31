package night.mixins.accessors;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.state.gui.GuiTextRenderState;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix3x2fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GuiTextRenderState.class)
public interface GuiTextRenderStateAccessor {
   @Accessor("text")
   FormattedCharSequence getText();

   @Accessor("x")
   int getX();

   @Accessor("y")
   int getY();

   @Accessor("color")
   int getColor();

   @Accessor("dropShadow")
   boolean isDropShadow();

   @Accessor("pose")
   Matrix3x2fc getPose();

   @Accessor("scissor")
   ScreenRectangle getScissor();
}
