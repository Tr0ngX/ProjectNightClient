package night.events.impl;

import lombok.Generated;
import net.minecraft.world.phys.Vec3;
import night.events.Event;

public class PushOutOfBlocksEvent extends Event {
   private final Vec3 pushVector;

   @Generated
   public PushOutOfBlocksEvent(Vec3 var1) {
      this.pushVector = var1;
   }
}
