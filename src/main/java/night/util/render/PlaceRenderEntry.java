package night.util.render;

import lombok.Generated;
import net.minecraft.core.BlockPos;
import night.Night;
import night.module.impl.core.RendersModule;
import night.util.animation.EasingUtil;

public class PlaceRenderEntry {
   private BlockPos a;
   private long b;

   public PlaceRenderEntry(BlockPos var1) {
      this.a = var1;
      long var2 = System.currentTimeMillis();
      this.b = var2;
   }

   @Override
   public boolean equals(Object var1) {
      if (var1 instanceof PlaceRenderEntry) {
         BlockPos var2 = ((PlaceRenderEntry)var1).a;
         BlockPos var3 = this.a;
         return var2.equals(var3);
      } else {
         return false;
      }
   }

   public float a() {
      long var1 = this.b;
      int var3 = Night.moduleManager.getModule(RendersModule.class).duration.getValue().intValue();
      float var4 = EasingUtil.a(var1, var3);
      return EasingUtil.a(1.0F - var4, EasingUtil.Easing.EASE_IN_CUBIC);
   }

   @Generated
   public BlockPos b() {
      return this.a;
   }
}
