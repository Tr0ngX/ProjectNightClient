package night.events.impl;

import lombok.Generated;
import net.minecraft.world.entity.player.Player;
import night.events.Event;

public class PlayerDeathEvent extends Event {
   private final Player player;

   @Generated
   public Player getPlayer() {
      return this.player;
   }

   @Generated
   public PlayerDeathEvent(Player var1) {
      this.player = var1;
   }
}
