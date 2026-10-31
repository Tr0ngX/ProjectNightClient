package night.events.impl;

import lombok.Generated;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import night.events.Event;
import org.joml.Matrix3x2fStack;

public class Render2DEvent extends Event {
   private final GuiGraphicsExtractor context;
   private final float tickDelta;

   public Matrix3x2fStack getMatrices() {
      return this.context.pose();
   }

   @Generated
   public GuiGraphicsExtractor getContext() {
      return this.context;
   }

   @Generated
   public float getTickDelta() {
      return this.tickDelta;
   }

   @Generated
   public Render2DEvent(GuiGraphicsExtractor var1, float var2) {
      this.context = var1;
      this.tickDelta = var2;
   }
}
