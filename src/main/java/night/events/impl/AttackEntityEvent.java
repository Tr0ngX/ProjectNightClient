package night.events.impl;

import lombok.Generated;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import night.events.Event;

public class AttackEntityEvent extends Event {
   private final Player player;
   private final Entity entity;

   @Generated
   public Player getPlayer() {
      return this.player;
   }

   @Generated
   public Entity getEntity() {
      return this.entity;
   }

   @Generated
   public AttackEntityEvent(Player var1, Entity var2) {
      this.player = var1;
      this.entity = var2;
   }
}
