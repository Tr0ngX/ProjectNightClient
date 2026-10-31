package night.command.impl;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import night.command.Command;
import night.command.CommandInfo;

@CommandInfo(name = "itemdrop", aliases = "drop", displayName = "ItemDrop", description = "Drops items from your inventory.", syntax = "<all|item name>")
public class ItemDropCommand extends Command {
   @Override
   public List<String> getSuggestions(String[] var1) {
      if (var1.length != 0) {
         return List.of();
      }

      List var2 = List.of("all");
      ArrayList var3 = new ArrayList(var2);

      for (Item var5 : (Iterable<Item>)(Object)BuiltInRegistries.ITEM) {
         ArrayList var6 = var3;
         Item var7 = var5;
         String var8 = BuiltInRegistries.ITEM.getKey(var7).getPath();
         var6.add(var8);
      }

      return var3;
   }

   @Override
   public void execute(String[] var1) {
      if (var1.length == 0) {
         this.sendSyntax();
      } else if (var1[0].equalsIgnoreCase("all")) {
         this.h();
      } else {
         String var2 = String.join(" ", var1);
         this.c(var2);
      }
   }

   private void h() {
      if (mc.player != null && mc.gameMode != null) {
         int var1 = mc.player.containerMenu.containerId;

         for (int var2 = 0; var2 < 36; var2++) {
            Inventory var3 = mc.player.getInventory();
            int var4 = var2;
            if (!var3.getItem(var4).isEmpty()) {
               MultiPlayerGameMode var5 = mc.gameMode;
               int var6 = var1;
               int var7 = a_int_int(var2);
               LocalPlayer var8 = mc.player;
               var5.handleContainerInput(var6, var7, 1, ContainerInput.THROW, var8);
            }
         }
      }
   }

   private void c(String var1) {
      if (mc.player != null && mc.gameMode != null) {
         int var2 = mc.player.containerMenu.containerId;

         for (int var3 = 0; var3 < 36; var3++) {
            Inventory var4 = mc.player.getInventory();
            int var5 = var3;
            ItemStack var6 = var4.getItem(var5);
            if (!var6.isEmpty()) {
               Item var7 = var6.getItem();
               String var8 = BuiltInRegistries.ITEM.getKey(var7).toString();
               String var9 = var6.getHoverName().getString();
               if (!var8.equalsIgnoreCase(var1)) {
                  String var10 = var8;
                  int var11 = var8.indexOf(58) + 1;
                  if (!var10.substring(var11).equalsIgnoreCase(var1) && !var9.equalsIgnoreCase(var1)) {
                     continue;
                  }
               }

               MultiPlayerGameMode var12 = mc.gameMode;
               int var13 = var2;
               int var14 = a_int_int(var3);
               LocalPlayer var15 = mc.player;
               var12.handleContainerInput(var13, var14, 1, ContainerInput.THROW, var15);
            }
         }
      }
   }

   private static int a_int_int(int var0) {
      int var1;
      if (var0 <= 8) {
         var1 = 36 + var0;
      } else {
         var1 = var0;
      }

      return var1;
   }
}
