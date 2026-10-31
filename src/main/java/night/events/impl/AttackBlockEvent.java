package night.events.impl;

import lombok.Generated;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import night.events.Event;

public class AttackBlockEvent extends Event {
   private final BlockPos pos;
   private final Direction direction;

   @Generated
   public AttackBlockEvent(BlockPos var1, Direction var2) {
      this.pos = var1;
      this.direction = var2;
   }

   @Generated
   public BlockPos getPos() {
      return this.pos;
   }
}
