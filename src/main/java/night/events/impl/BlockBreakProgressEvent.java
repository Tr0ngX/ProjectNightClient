package night.events.impl;

import lombok.Generated;
import net.minecraft.core.BlockPos;
import night.events.Event;

public class BlockBreakProgressEvent extends Event {
   private final int breakerId;
   private final BlockPos pos;

   @Generated
   public int getBreakerId() {
      return this.breakerId;
   }

   @Generated
   public BlockPos getPos() {
      return this.pos;
   }

   @Generated
   public BlockBreakProgressEvent(int var1, BlockPos var2) {
      this.breakerId = var1;
      this.pos = var2;
   }
}
