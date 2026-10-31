package night.gui;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

public class PeekChestMenu extends ChestMenu {
   public PeekChestMenu(int var1, Inventory var2, Container var3) {
      super(MenuType.GENERIC_9x3, var1, var2, var3, 3);
   }

   public void clicked(int var1, int var2, ContainerInput var3, Player var4) {
   }

   public ItemStack quickMoveStack(Player var1, int var2) {
      return ItemStack.EMPTY;
   }
}
