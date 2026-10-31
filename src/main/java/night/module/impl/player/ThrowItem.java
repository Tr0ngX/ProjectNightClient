package night.module.impl.player;

import java.util.Map;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import night.Night;
import night.events.EventTarget;
import night.events.impl.TickEvent;
import night.module.Module;
import night.module.ModuleInfo;
import night.setting.impl.BooleanSetting;
import night.setting.impl.NumberSetting;

@ModuleInfo(
   name = "ThrowItem",
   description = "Throw out items that arent belong to your loaded kit. Useful when you need to loot gears during combat",
   category = Module.Category.PLAYER
)
public class ThrowItem extends Module {
   public NumberSetting delay;
   public NumberSetting actionsPerTick;
   public BooleanSetting autoDisable;
   public BooleanSetting keepKitItems;
   public BooleanSetting minimumCount;
   public NumberSetting minCount;
   public BooleanSetting ignoreHotbar;
   public BooleanSetting onlyInventory;
   private int m;

   public ThrowItem() {
      Integer var1 = 1;
      Integer var2 = 0;
      Integer var3 = 20;
      this.delay = new NumberSetting("Delay", "Tick delay between throw actions.", var1, var2, var3);
      Integer var4 = 3;
      Integer var5 = 1;
      Integer var6 = 20;
      this.actionsPerTick = new NumberSetting("ActionsPerTick", "Maximum items to drop per pass.", var4, var5, var6);
      this.autoDisable = new BooleanSetting("AutoDisable", "Automatically disables the module when no items are left to throw.", false);
      this.keepKitItems = new BooleanSetting("KeepKitItems", "Keep items that belong to the loaded kit even if in a different slot.", true);
      this.minimumCount = new BooleanSetting("MinimumCount", "Keep matching items only if their stack count reaches minimum count.", false);
      BooleanSetting var8 = this.minimumCount;
      BooleanSetting.BooleanCondition var7 = new BooleanSetting.BooleanCondition(var8, true);
      Integer var9 = 16;
      Integer var10 = 1;
      Integer var11 = 64;
      this.minCount = new NumberSetting("MinCount", "The minimum stack count to keep an item.", var7, var9, var10, var11);
      this.ignoreHotbar = new BooleanSetting("IgnoreHotbar", "Do not throw items from the hotbar (slots 0-8).", false);
      this.onlyInventory = new BooleanSetting("OnlyInventory", "Only throw items when in regular gameplay or InventoryScreen (not in Chests/Shulkers).", true);
      this.m = 0;
   }

   @Override
   public void onEnable() {
      this.m = 0;
   }

   @EventTarget
   public void a(TickEvent var1) {
      if (mc.player != null && mc.level != null && mc.gameMode != null) {
         if (!mc.player.isCreative() && !mc.player.isSpectator()) {
            if (mc.player.containerMenu.getCarried().isEmpty()) {
               Rekit var2 = Night.moduleManager.getModule(Rekit.class);
               if (var2 != null) {
                  Map var3 = var2.z();
                  if (var3 != null && !var3.isEmpty()) {
                     if (!var2.v()) {
                        long var4 = System.currentTimeMillis();
                        long var6 = Rekit.v;
                        long var8 = var4 - var6;
                        long var10 = 200L;
                        if (var8 >= var10) {
                           if (mc.gui.screen() != null && !(mc.gui.screen() instanceof InventoryScreen)) {
                              return;
                           }

                           boolean var12;
                           if (mc.gui.screen() instanceof AbstractContainerScreen && !(mc.gui.screen() instanceof InventoryScreen)) {
                              var12 = true;
                           } else {
                              var12 = false;
                           }

                           boolean var13 = var12;
                           if (this.onlyInventory.getValue() && var13) {
                              return;
                           }

                           int var14 = this.m;
                           int var15 = this.delay.getValue().intValue();
                           if (var14 < var15) {
                              int var16 = this.m + 1;
                              this.m = var16;
                              return;
                           }

                           this.m = 0;
                           int var17 = mc.player.containerMenu.containerId;
                           int var18 = 0;
                           int var19 = this.actionsPerTick.getValue().intValue();
                           int var20 = 0;

                           for (int var21 = 0; var21 < 36; var21++) {
                              if (!this.ignoreHotbar.getValue() || var21 > 8) {
                                 Inventory var22 = mc.player.getInventory();
                                 int var23 = var21;
                                 ItemStack var24 = var22.getItem(var23);
                                 if (!var24.isEmpty()) {
                                    boolean var30;
                                    label102: {
                                       Map var25 = var3;
                                       Integer var26 = var21;
                                       Rekit.KitSlotEntry var27 = (Rekit.KitSlotEntry)var25.get(var26);
                                       Item var28 = var24.getItem();
                                       String var29 = BuiltInRegistries.ITEM.getKey(var28).toString();
                                       var30 = false;
                                       if (var27 != null && var27.a != null) {
                                          String var31 = var29;
                                          String var32 = var27.a;
                                          if (var31.equalsIgnoreCase(var32)) {
                                             var30 = true;
                                             break label102;
                                          }
                                       }

                                       if (this.keepKitItems.getValue()) {
                                          for (Rekit.KitSlotEntry var34 : (Iterable<Rekit.KitSlotEntry>)(Object)var3.values()) {
                                             if (var34 != null && var34.a != null) {
                                                String var35 = var29;
                                                String var36 = var34.a;
                                                if (var35.equalsIgnoreCase(var36)) {
                                                   var30 = true;
                                                   break;
                                                }
                                             }
                                          }
                                       }
                                    }

                                    boolean var37 = false;
                                    if (!var30) {
                                       var37 = true;
                                    } else if (this.minimumCount.getValue() && var24.getMaxStackSize() > 1) {
                                       int var38 = var24.getCount();
                                       int var39 = this.minCount.getValue().intValue();
                                       if (var38 < var39) {
                                          var37 = true;
                                       }
                                    }

                                    if (var37) {
                                       var20++;
                                       int var40 = var18;
                                       int var41 = var19;
                                       if (var40 < var41) {
                                          int var42 = var21;
                                          int var43 = this.b(var42);
                                          if (var43 >= 0) {
                                             int var44 = var17;
                                             int var45 = var43;
                                             this.a(var44, var45);
                                             var18++;
                                          }
                                       }
                                    }
                                 }
                              }
                           }

                           if (var20 == 0 && this.autoDisable.getValue()) {
                              this.setEnabled(false);
                           }

                           return;
                        }
                     }
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
