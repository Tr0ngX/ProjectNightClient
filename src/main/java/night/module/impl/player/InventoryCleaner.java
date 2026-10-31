package night.module.impl.player;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import night.events.EventTarget;
import night.events.impl.TickEvent;
import night.module.Module;
import night.module.ModuleInfo;
import night.setting.impl.BooleanSetting;
import night.setting.impl.ModeSetting;
import night.setting.impl.NumberSetting;
import night.setting.impl.WhitelistSetting;

@ModuleInfo(name = "InventoryCleaner", description = "Auto-drops unwanted items from your inventory each tick.", category = Module.Category.PLAYER)
public class InventoryCleaner extends Module {
   public ModeSetting mode;
   public WhitelistSetting whitelist;
   public BooleanSetting ignoreHotbar;
   public BooleanSetting throwWorse;
   public BooleanSetting others;
   public BooleanSetting ignoreCustomName;
   public NumberSetting delay;
   public NumberSetting actionsPerTick;
   private int m;

   public InventoryCleaner() {
      String[] var1 = new String[]{"WhiteList", "BlackList", "All"};
      this.mode = new ModeSetting("Mode", "WhiteList = drop items IN list. BlackList = drop items NOT in list. All = drop everything.", "WhiteList", var1);
      this.whitelist = new WhitelistSetting("Whitelist", "Items this mode's WhiteList/BlackList compares against.", WhitelistSetting.WhitelistType.ITEMS);
      this.ignoreHotbar = new BooleanSetting("IgnoreHotbar", "Skip hotbar slots (0-8) when cleaning.", true);
      this.throwWorse = new BooleanSetting(
         "ThrowWorse", "Drop lower-tier duplicate tools/armor of the same type (e.g. diamond+iron pickaxe -> drop iron).", true
      );
      this.others = new BooleanSetting("Others", "Also drop from other open GUIs like Chest, Shulker, EnderChest.", false);
      this.ignoreCustomName = new BooleanSetting(
         "IgnoreCustomName",
         "(With Others on) skip acting on custom-titled containers such as a shop GUI, except shulker boxes. Does not affect individual items.",
         false
      );
      Integer var2 = 1;
      Integer var3 = 0;
      Integer var4 = 20;
      this.delay = new NumberSetting("Delay", "Tick delay between drop passes.", var2, var3, var4);
      Integer var5 = 5;
      Integer var6 = 1;
      Integer var7 = 20;
      this.actionsPerTick = new NumberSetting("ActionsPerTick", "Max items to drop per pass.", var5, var6, var7);
      this.m = 0;
   }

   @EventTarget
   public void a_void_TickEvent(TickEvent var1) {
      if (mc.player != null && mc.level != null && mc.gameMode != null) {
         if (!mc.player.isCreative()) {
            if (mc.player.containerMenu.getCarried().isEmpty()) {
               long var2 = System.currentTimeMillis();
               long var4 = Rekit.v;
               long var6 = var2 - var4;
               long var8 = 200L;
               if (var6 >= var8) {
                  long var10 = var2;
                  long var12 = InventorySorter.h;
                  long var14 = var10 - var12;
                  long var16 = 200L;
                  if (var14 >= var16) {
                     boolean var18;
                     if (mc.gui.screen() instanceof AbstractContainerScreen && !(mc.gui.screen() instanceof InventoryScreen)) {
                        var18 = true;
                     } else {
                        var18 = false;
                     }

                     boolean var19 = var18;
                     int var20 = this.m;
                     int var21 = this.delay.getValue().intValue();
                     if (var20 < var21) {
                        int var22 = this.m + 1;
                        this.m = var22;
                        return;
                     }

                     this.m = 0;
                     int var23 = mc.player.containerMenu.containerId;
                     int var24 = 0;
                     int var25 = this.actionsPerTick.getValue().intValue();

                     for (int var26 = 0; var26 < 36; var26++) {
                        int var27 = var24;
                        int var28 = var25;
                        if (var27 >= var28) {
                           break;
                        }

                        if (!this.ignoreHotbar.getValue() || var26 > 8) {
                           Inventory var29 = mc.player.getInventory();
                           int var30 = var26;
                           ItemStack var31 = var29.getItem(var30);
                           if (!var31.isEmpty()) {
                              ItemStack var32 = var31;
                              if (this.a_bool_ItemStack(var32)) {
                                 int var33 = var26;
                                 int var34 = this.b(var33);
                                 if (var34 >= 0) {
                                    int var35 = var23;
                                    int var36 = var34;
                                    this.a(var35, var36);
                                    var24++;
                                 }
                              }
                           }
                        }
                     }

                     if (this.throwWorse.getValue()) {
                        int var37 = var24;
                        int var38 = var25;
                        if (var37 < var38) {
                           int var39 = var24;
                           int var40 = var23;
                           int var41 = var25;
                           int var42 = var24;
                           int var43 = var41 - var42;
                           int var44 = this.b(var40, var43);
                           var24 = var39 + var44;
                        }
                     }

                     boolean var45;
                     if (this.ignoreCustomName.getValue() && this.v() && !(mc.player.containerMenu instanceof ShulkerBoxMenu)) {
                        var45 = true;
                     } else {
                        var45 = false;
                     }

                     boolean var46 = var45;
                     if (var19 && this.others.getValue() && !var46) {
                        int var47 = var24;
                        int var48 = var25;
                        if (var47 < var48) {
                           int var49 = mc.player.containerMenu.slots.size() - 36;
                           int var50 = 0;

                           while (true) {
                              int var51 = var50;
                              int var52 = var49;
                              if (var51 >= var52) {
                                 break;
                              }

                              int var53 = var24;
                              int var54 = var25;
                              if (var53 >= var54) {
                                 break;
                              }

                              NonNullList var55 = mc.player.containerMenu.slots;
                              int var56 = var50;
                              ItemStack var57 = ((Slot)var55.get(var56)).getItem();
                              if (!var57.isEmpty()) {
                                 ItemStack var58 = var57;
                                 if (this.a_bool_ItemStack(var58)) {
                                    int var59 = var23;
                                    int var60 = var50;
                                    this.a(var59, var60);
                                    var24++;
                                 }
                              }

                              var50++;
                           }
                        }
                     }

                     return;
                  }
               }
            }
         }
      }
   }

   private void a(int var1, int var2) {
      MultiPlayerGameMode var3 = mc.gameMode;
      LocalPlayer var4 = mc.player;
      var3.handleContainerInput(var1, var2, 1, ContainerInput.THROW, var4);
   }

   private boolean v() {
      return mc.gui.screen() instanceof AbstractContainerScreen var2 ? ChestStealer.a_bool_Component(var2.getTitle()) : false;
   }

   private boolean a_bool_ItemStack(ItemStack var1) {
      WhitelistSetting var2 = this.whitelist;
      Item var3 = var1.getItem();
      boolean var4 = var2.contains(var3);

      return switch (this.mode.getValue()) {
         case "All" -> true;
         case "BlackList" -> {
            if (!var4) {
               yield true;
            } else {
               yield false;
            }
         }
         default -> var4;
      };
   }

   private int b(int var1, int var2) {
      String[] var3 = new String[]{"_pickaxe", "_axe", "_shovel", "_hoe", "_sword", "_helmet", "_chestplate", "_leggings", "_boots"};
      String[] var4 = var3;
      int var5 = var2;
      String[] var6 = var4;
      int var7 = var6.length;
      int var8 = 0;

      while (true) {
         int var9 = var8;
         int var10 = var7;
         if (var9 >= var10) {
            break;
         }

         String[] var11 = var6;
         int var12 = var8;
         String var13 = var11[var12];
         if (var5 <= 0) {
            break;
         }

         ArrayList var14 = new ArrayList();

         for (int var15 = 0; var15 < 36; var15++) {
            if (!this.ignoreHotbar.getValue() || var15 > 8) {
               Inventory var16 = mc.player.getInventory();
               int var17 = var15;
               ItemStack var18 = var16.getItem(var17);
               if (!var18.isEmpty()) {
                  Item var19 = var18.getItem();
                  String var20 = BuiltInRegistries.ITEM.getKey(var19).toString();
                  String var21 = var13;
                  if (var20.endsWith(var21)) {
                     ArrayList var22 = var14;
                     int[] var23 = new int[2];
                     int var24 = var15;
                     var23[0] = var24;
                     int var25 = var18.getMaxDamage();
                     var23[1] = var25;
                     var22.add(var23);
                  }
               }
            }
         }

         if (var14.size() >= 2) {
            int var26 = ((List<int[]>)var14).stream().mapToInt(var0 -> var0[1]).max().orElse(0);

            for (int[] var28 : (Iterable<int[]>)(Object)var14) {
               if (var5 <= 0) {
                  break;
               }

               int var29 = var28[1];
               int var30 = var26;
               if (var29 < var30) {
                  int var31 = var28[0];
                  int var32 = this.b(var31);
                  if (var32 >= 0) {
                     int var33 = var32;
                     this.a(var1, var33);
                     var5 += -1;
                  }
               }
            }
         }

         var8++;
      }

      int var34 = var5;
      return var2 - var34;
   }

   private int b(int var1) {
      Inventory var2 = mc.player.getInventory();

      for (Slot var4 : (Iterable<Slot>)(Object)mc.player.containerMenu.slots) {
         Container var5 = var4.container;
         Inventory var6 = var2;
         if (var5 == var6 && var4.getContainerSlot() == var1) {
            return var4.index;
         }
      }

      return -1;
   }
}
