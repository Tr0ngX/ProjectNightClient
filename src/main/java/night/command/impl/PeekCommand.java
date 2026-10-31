package night.command.impl;

import java.util.List;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import night.Night;
import night.command.Command;
import night.command.CommandInfo;
import night.gui.PeekChestMenu;
import night.gui.PeekContainerScreen;
import night.util.inventory.EnderChestMemory;

@CommandInfo(
   name = "peek",
   displayName = "Peek",
   description = "Peeks a shulker box held in hand, or your last-opened ender chest, without opening it.",
   syntax = ""
)
public class PeekCommand extends Command {
   @Override
   public List<String> getSuggestions(String[] var1) {
      return List.of();
   }

   @Override
   public void execute(String[] var1) {
      if (mc.player != null) {
         EnderChestMemory.a();
         ItemStack var2 = mc.player.getMainHandItem();
         ItemContainerContents var3 = (ItemContainerContents)var2.get(DataComponents.CONTAINER);
         if (var3 == null) {
            var2 = mc.player.getOffhandItem();
            var3 = (ItemContainerContents)var2.get(DataComponents.CONTAINER);
         }

         if (var3 != null) {
            NonNullList var4 = NonNullList.withSize(27, ItemStack.EMPTY);
            ItemContainerContents var5 = var3;
            NonNullList var6 = var4;
            var5.copyInto(var6);
            NonNullList var7 = var4;
            Component var8 = var2.getHoverName();
            this.a_void_NonNullList_Component(var7, var8);
         } else if (EnderChestMemory.b()) {
            NonNullList var9 = EnderChestMemory.c();
            MutableComponent var10 = Component.literal("Ender Chest (remembered)");
            this.a_void_NonNullList_Component(var9, var10);
         } else {
            String var11 = this.getDisplayName();
            String var12 = this.getName();
            Night.chatManager
               .sendReplaceableTaggedMessage("Hold a shulker box to peek it, or open your ender chest once so it can be remembered.", var11, var12);
         }
      }
   }

   private void a_void_NonNullList_Component(NonNullList<ItemStack> var1, Component var2) {
      SimpleContainer var3 = new SimpleContainer(27);

      for (int var4 = 0; var4 < 27; var4++) {
         int var5 = var4;
         int var6 = var1.size();
         if (var5 >= var6) {
            break;
         }

         SimpleContainer var7 = var3;
         int var8 = var4;
         int var9 = var4;
         ItemStack var10 = ((ItemStack)var1.get(var9)).copy();
         var7.setItem(var8, var10);
      }

      mc.execute(() -> {
         Inventory var2x = mc.player.getInventory();
         PeekChestMenu var3x = new PeekChestMenu(0, var2x, var3);
         LocalPlayer var4x = mc.player;
         PeekChestMenu var5x = var3x;
         var4x.containerMenu = var5x;
         Gui var6x = mc.gui;
         PeekChestMenu var7x = var3x;
         Inventory var8x = mc.player.getInventory();
         var6x.setScreen(new PeekContainerScreen(var7x, var8x, var2));
      });
   }
}
