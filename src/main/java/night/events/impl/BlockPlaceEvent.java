package night.events.impl;

import lombok.Generated;
import net.minecraft.core.BlockPos;
import night.events.Event;

public class BlockPlaceEvent extends Event {
   private final BlockPos pos;

   @Generated
   public BlockPlaceEvent(BlockPos var1) {
      this.pos = var1;
   }

   @Generated
   public BlockPos getPos() {
      return this.pos;
   }
}
