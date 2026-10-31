package night.events.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Generated;
import night.events.Event;

public class Render3DEvent extends Event {
   private final PoseStack matrices;
   private final float tickDelta;

   @Generated
   public PoseStack getMatrices() {
      return this.matrices;
   }

   @Generated
   public float getTickDelta() {
      return this.tickDelta;
   }

   @Generated
   public Render3DEvent(PoseStack var1, float var2) {
      this.matrices = var1;
      this.tickDelta = var2;
   }

   public static class Post extends Event {
      private final PoseStack matrices;
      private final float tickDelta;

      @Generated
      public PoseStack getMatrices() {
         return this.matrices;
      }

      @Generated
      public float getTickDelta() {
         return this.tickDelta;
      }

      @Generated
      public Post(PoseStack var1, float var2) {
         this.matrices = var1;
         this.tickDelta = var2;
      }
   }
}
