package night.events.impl;

import lombok.Generated;
import net.minecraft.world.entity.player.Player;
import night.events.Event;

public class TargetDeathEvent extends Event {
   private final Player player;

   @Generated
   public TargetDeathEvent(Player var1) {
      this.player = var1;
   }

   @Generated
   public Player getPlayer() {
      return this.player;
   }
}
