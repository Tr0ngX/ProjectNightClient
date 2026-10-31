package night.events.impl;

import lombok.Generated;
import net.minecraft.core.BlockPos;
import night.events.Event;

public class BreakBlockEvent extends Event {
   private final BlockPos pos;

   @Generated
   public BreakBlockEvent(BlockPos var1) {
      this.pos = var1;
   }
}
