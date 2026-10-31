package night.events.impl;

import lombok.Generated;
import net.minecraft.world.entity.Entity;
import night.events.Event;

public class EntitySpawnEvent extends Event {
   private final Entity entity;

   @Generated
   public EntitySpawnEvent(Entity var1) {
      this.entity = var1;
   }

   @Generated
   public Entity getEntity() {
      return this.entity;
   }
}
