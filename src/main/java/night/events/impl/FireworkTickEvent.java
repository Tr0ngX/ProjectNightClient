package night.events.impl;

import lombok.Generated;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import night.events.Event;

public class FireworkTickEvent extends Event {
   private final FireworkRocketEntity firework;

   @Generated
   public FireworkTickEvent(FireworkRocketEntity var1) {
      this.firework = var1;
   }
}
