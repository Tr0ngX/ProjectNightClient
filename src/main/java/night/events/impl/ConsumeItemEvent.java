package night.events.impl;

import lombok.Generated;
import net.minecraft.world.item.ItemStack;
import night.events.Event;

public class ConsumeItemEvent extends Event {
   private final ItemStack stack;

   @Generated
   public ItemStack getStack() {
      return this.stack;
   }

   @Generated
   public ConsumeItemEvent(ItemStack var1) {
      this.stack = var1;
   }
}
