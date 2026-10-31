package night.module.impl.player;

import java.util.Arrays;
import java.util.Objects;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import night.Night;
import night.events.EventTarget;
import night.events.impl.UpdateWalkingPlayerEvent;
import night.module.Module;
import night.module.ModuleInfo;
import night.module.impl.combat.Mainhand;
import night.setting.impl.BooleanSetting;
import night.setting.impl.ModeSetting;
import night.setting.impl.NumberSetting;
import night.util.inventory.InventoryUtil;

@ModuleInfo(
   name = "Replenish",
   description = "Automatically replenishes stacks in your hotbar with new ones when they meet a specified threshold.",
   category = Module.Category.PLAYER
)
public class Replenish extends Module {
   public ModeSetting switchSetting;
   public NumberSetting threshold;
   public BooleanSetting unstackable;
   public BooleanSetting inventory;
   private int i;
   private final ItemStack[] j;

   public Replenish() {
      String[] var1 = new String[]{"Pickup", "Swap", "Quick"};
      this.switchSetting = new ModeSetting("Switch", "The mode that will be used for switching items.", "Swap", var1);
      Integer var2 = 11;
      Integer var3 = 1;
      Integer var4 = 64;
      this.threshold = new NumberSetting("Threshold", "The minimum amount of items in a stack before that stack is replaced.", var2, var3, var4);
      this.unstackable = new BooleanSetting("Unstackable", "Replenishes items that cannot be stacked (like Totems).", false);
      this.inventory = new BooleanSetting("Inventory", "Allows replenishing items while an inventory, container, or custom GUI is open.", false);
      ItemStack[] var5 = new ItemStack[9];
      this.j = var5;
   }

   @Override
   public void onEnable() {
      if (mc.player != null) {
         for (int var1 = 0; var1 <= 8; var1++) {
            ItemStack[] var2 = this.j;
            int var3 = var1;
            Inventory var4 = mc.player.getInventory();
            int var5 = var1;
            ItemStack var6 = var4.getItem(var5).copy();
            var2[var3] = var6;
         }
      }
   }

   @Override
   public void onDisable() {
      Arrays.fill(this.j, ItemStack.EMPTY);
   }

   @EventTarget
   public void a_void_UpdateWalkingPlayerEvent(UpdateWalkingPlayerEvent var1) {
      if (mc.player != null && mc.level != null) {
         if (!this.inventory.getValue() && mc.gui.screen() instanceof AbstractContainerScreen) {
            for (int var2 = 0; var2 <= 8; var2++) {
               Inventory var3 = mc.player.getInventory();
               int var4 = var2;
               if (var3.getItem(var4).isEmpty()) {
                  ItemStack[] var5 = this.j;
                  int var6 = var2;
                  var5[var6] = ItemStack.EMPTY;
               }
            }
         }

         if (!this.inventory.getValue()) {
            boolean var9;
            label152: {
               label124:
               if (mc.gui == null || mc.gui.screen() == null) {
                  if (mc.player.containerMenu != null) {
                     AbstractContainerMenu var7 = mc.player.containerMenu;
                     InventoryMenu var8 = mc.player.inventoryMenu;
                     if (var7 != var8) {
                        break label124;
                     }
                  }

                  var9 = false;
                  break label152;
               }

               var9 = true;
            }

            if (var9) {
               for (int var10 = 0; var10 <= 8; var10++) {
                  Inventory var11 = mc.player.getInventory();
                  int var12 = var10;
                  ItemStack var13 = var11.getItem(var12);
                  if (!var13.isEmpty()) {
                     ItemStack[] var14 = this.j;
                     int var15 = var10;
                     ItemStack var16 = var13.copy();
                     var14[var15] = var16;
                  }
               }

               return;
            }
         }

         if (mc.player.containerMenu != null) {
            AbstractContainerMenu var17 = mc.player.containerMenu;
            InventoryMenu var18 = mc.player.inventoryMenu;
            if (var17 != var18 && mc.player.containerMenu.slots.size() < 36) {
               return;
            }
         }

         if (this.i <= 0) {
            boolean var19 = false;

            for (int var20 = 0; var20 <= 8; var20++) {
               Inventory var21 = mc.player.getInventory();
               int var22 = var20;
               ItemStack var23 = var21.getItem(var22);
               int var24 = var23.getCount();
               if (!var23.isEmpty()) {
                  ItemStack[] var25 = this.j;
                  int var26 = var20;
                  ItemStack var27 = var23.copy();
                  var25[var26] = var27;
               }

               ItemStack[] var28 = this.j;
               int var29 = var20;
               ItemStack var30 = var28[var29];
               if (var30 != null
                  && !var30.isEmpty()
                  && (var30.isStackable() || this.unstackable.getValue())
                  && (!var30.is(Items.TOTEM_OF_UNDYING) || !Night.moduleManager.getModule(Mainhand.class).w())) {
                  if (var30.isStackable()) {
                     int var31 = var24;
                     int var32 = var30.getMaxStackSize();
                     if (var31 >= var32) {
                        continue;
                     }

                     float var33 = this.threshold.getValue().floatValue() / 64.0F;
                     float var34 = var30.getMaxStackSize();
                     int var35 = (int)(var33 * var34);
                     int var36 = var24;
                     int var37 = var35;
                     if (var36 > var37) {
                        continue;
                     }
                  } else if (var24 > 0) {
                     continue;
                  }

                  ItemStack var38 = var30;
                  int var39 = this.a_int_ItemStack(var38);
                  if (var39 != -1) {
                     String var40 = this.switchSetting.getValue();
                     Inventory var41 = mc.player.getInventory();
                     int var42 = var39;
                     int var43 = var41.getItem(var42).getCount();
                     if (var24 > 0) {
                        int var44 = var43;
                        int var45 = var24;
                        if (var44 <= var45 && var40.equalsIgnoreCase("Swap")) {
                           continue;
                        }
                     }

                     if (var40.equalsIgnoreCase("Quick")) {
                        MultiPlayerGameMode var46 = mc.gameMode;
                        int var47 = mc.player.containerMenu.containerId;
                        int var48 = InventoryUtil.e(var39);
                        LocalPlayer var49 = mc.player;
                        var46.handleContainerInput(var47, var48, 0, ContainerInput.QUICK_MOVE, var49);
                     } else {
                        String var50 = var40;
                        int var51 = var39;
                        int var52 = var20;
                        InventoryUtil.c(var50, var51, var52);
                     }

                     var19 = true;
                  }
               }
            }

            if (var19) {
               int var53 = Night.serverManager.getPingTicks();
               int var54 = 2 + var53;
               this.i = var54;
            }
         }

         int var55 = this.i - 1;
         this.i = var55;
      }
   }

   private int a_int_ItemStack(ItemStack var1) {
      int var2 = -1;
      int var3 = 0;

      for (int var4 = InventoryUtil.f; var4 >= InventoryUtil.e; var4 += -1) {
         Inventory var5 = mc.player.getInventory();
         int var6 = var4;
         ItemStack var7 = var5.getItem(var6);
         int var8 = var7.getCount();
         int var9 = var3;
         if (var8 > var9 && a(var7, var1)) {
            var3 = var7.getCount();
            var2 = var4;
         }
      }

      return var2;
   }

   private static boolean a(ItemStack var0, ItemStack var1) {
      PotionContents var2 = (PotionContents)var0.get(DataComponents.POTION_CONTENTS);
      PotionContents var3 = (PotionContents)var1.get(DataComponents.POTION_CONTENTS);
      if (var2 != null || var3 != null) {
         Item var4 = var1.getItem();
         if (var0.is(var4)) {
            PotionContents var5 = var2;
            PotionContents var6 = var3;
            if (Objects.equals(var5, var6)) {
               return true;
            }
         }

         return false;
      } else {
         if (ItemStack.isSameItemSameComponents(var0, var1)) {
            return true;
         }

         if (var0.isDamageableItem()) {
            Item var8 = var1.getItem();
            if (var0.is(var8)) {
               ItemStack var9 = var0.copy();
               ItemStack var10 = var1.copy();
               var9.setDamageValue(0);
               var10.setDamageValue(0);
               ItemStack var11 = var9;
               ItemStack var12 = var10;
               return ItemStack.isSameItemSameComponents(var11, var12);
            }
         }

         return false;
      }
   }

   @Override
   public String getDisplayInfo() {
      return String.valueOf(this.threshold.getValue().intValue());
   }
}
