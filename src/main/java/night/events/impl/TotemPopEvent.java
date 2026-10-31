package night.events.impl;

import lombok.Generated;
import net.minecraft.world.entity.player.Player;
import night.events.Event;

public class TotemPopEvent extends Event {
   private final Player player;
   private final int pops;

   @Generated
   public Player getPlayer() {
      return this.player;
   }

   @Generated
   public int getPops() {
      return this.pops;
   }

   @Generated
   public TotemPopEvent(Player var1, int var2) {
      this.player = var1;
      this.pops = var2;
   }
}
