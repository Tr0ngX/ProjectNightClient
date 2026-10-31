package night.module.impl.player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import night.Night;
import night.events.EventTarget;
import night.events.impl.TickEvent;
import night.module.Module;
import night.module.ModuleInfo;
import night.setting.impl.BooleanSetting;
import night.setting.impl.NumberSetting;

@ModuleInfo(name = "InventorySorter", description = "Auto-sorts your inventory into the active Rekit kit's layout.", category = Module.Category.PLAYER)
public class InventorySorter extends Module {
   public NumberSetting delay;
   public NumberSetting actionsPerTick;
   public BooleanSetting ignoreHotbar;
   private int i;
   public static volatile long h = 0L;
   private int j;
   private static final int k = 20;

   public InventorySorter() {
      Integer var1 = 1;
      Integer var2 = 0;
      Integer var3 = 10;
      this.delay = new NumberSetting("Delay", "Tick delay.", var1, var2, var3);
      Integer var4 = 1;
      Integer var5 = 1;
      Integer var6 = 5;
      this.actionsPerTick = new NumberSetting("ActionsPerTick", "Max actions per tick.", var4, var5, var6);
      this.ignoreHotbar = new BooleanSetting("IgnoreHotbar", "Skip hotbar slots (0-8) when sorting.", false);
      this.i = 0;
      this.j = 0;
   }

   @EventTarget
   public void a_void_TickEvent(TickEvent var1) {
      if (mc.player != null) {
         Rekit var2 = Night.moduleManager.getModule(Rekit.class);
         if (!var2.z().isEmpty()) {
            if (!mc.player.isCreative()) {
               if (!(mc.gui.screen() instanceof AbstractContainerScreen) || mc.gui.screen() instanceof InventoryScreen) {
                  ChestStealer var3 = Night.moduleManager.getModule(ChestStealer.class);
                  if (var3 != null && var3.isEnabled()) {
                     if (var3.v()) {
                        return;
                     }

                     long var4 = System.currentTimeMillis();
                     long var6 = ChestStealer.s;
                     long var8 = var4 - var6;
                     long var10 = 200L;
                     if (var8 < var10) {
                        return;
                     }
                  }

                  int var12 = this.i;
                  int var13 = this.delay.getValue().intValue();
                  if (var12 < var13) {
                     int var14 = this.i + 1;
                     this.i = var14;
                  } else {
                     this.i = 0;
                     int var15 = 0;

                     while (true) {
                        int var16 = var15;
                        int var17 = this.actionsPerTick.getValue().intValue();
                        if (var16 >= var17) {
                           break;
                        }

                        Rekit var18 = var2;
                        if (!this.a_bool_Rekit(var18)) {
                           break;
                        }

                        var15++;
                     }
                  }
               }
            }
         }
      }
   }

   private boolean a_bool_Rekit(Rekit var1) {
      AbstractContainerMenu var2 = mc.player.containerMenu;
      if (!var2.getCarried().isEmpty()) {
         int var3 = this.j + 1;
         this.j = var3;
         if (this.j >= 20) {
            this.j = 0;
            AbstractContainerMenu var4 = var2;
            int var5 = this.a_int_AbstractContainerMenu(var4);
            if (var5 != -1) {
               int var6 = var5;
               this.a_void_int_int_ContainerInput(var6, 0, ContainerInput.PICKUP);
            }
         }

         return false;
      } else {
         this.j = 0;
         HashMap var7 = new HashMap();

         for (int var8 = 0; var8 < 36; var8++) {
            if (!this.ignoreHotbar.getValue() || var8 > 8) {
               int var9 = var8;
               int var10 = this.b_int_int(var9);
               AbstractContainerMenu var11 = var2;
               int var12 = var10;
               ItemStack var13 = var11.getSlot(var12).getItem();
               if (!var13.isEmpty()) {
                  ItemStack var14 = var13;
                  if (!this.a_bool_ItemStack(var14)) {
                     int var15 = var13.getCount();
                     int var16 = var13.getItem().getDefaultMaxStackSize();
                     if (var15 < var16) {
                        Map var17 = var1.z();
                        Integer var18 = var8;
                        Rekit.KitSlotEntry var19 = (Rekit.KitSlotEntry)var17.get(var18);
                        ItemStack var20 = var13;
                        Rekit.KitSlotEntry var21 = var19;
                        if (!this.a_bool_ItemStack_KitSlotEntry(var20, var21)) {
                           Item var22 = var13.getItem();
                           String var23 = BuiltInRegistries.ITEM.getKey(var22).toString();
                           HashMap var24 = var7;
                           String var25 = var23;
                           List var26 = (List)((Map)var24).computeIfAbsent(var25, var0 -> new ArrayList());
                           Integer var27 = var10;
                           var26.add(var27);
                        }
                     }
                  }
               }
            }
         }

         for (List var29 : (Iterable<List>)(Object)var7.values()) {
            if (var29.size() > 1) {
               int var30 = 0;

               while (true) {
                  int var31 = var30;
                  int var32 = var29.size();
                  if (var31 >= var32) {
                     break;
                  }

                  List var33 = var29;
                  int var34 = var30;
                  int var35 = (Integer)var33.get(var34);
                  AbstractContainerMenu var36 = var2;
                  int var37 = var35;
                  ItemStack var38 = var36.getSlot(var37).getItem();
                  int var39 = var30 + 1;

                  while (true) {
                     int var40 = var39;
                     int var41 = var29.size();
                     if (var40 >= var41) {
                        var30++;
                        break;
                     }

                     List var42 = var29;
                     int var43 = var39;
                     int var44 = (Integer)var42.get(var43);
                     AbstractContainerMenu var45 = var2;
                     int var46 = var44;
                     ItemStack var47 = var45.getSlot(var46).getItem();
                     ItemStack var48 = var38;
                     ItemStack var49 = var47;
                     if (ItemStack.isSameItemSameComponents(var48, var49)) {
                        int var50 = var44;
                        int var51 = var35;
                        this.a_void_int_int(var50, var51);
                        return true;
                     }

                     var39++;
                  }
               }
            }
         }

         for (int var52 = 0; var52 < 36; var52++) {
            if (!this.ignoreHotbar.getValue() || var52 > 8) {
               int var53 = var52;
               int var54 = this.b_int_int(var53);
               AbstractContainerMenu var55 = var2;
               int var56 = var54;
               ItemStack var57 = var55.getSlot(var56).getItem();
               if (!this.a_bool_ItemStack(var57)) {
                  Map var58 = var1.z();
                  Integer var59 = var52;
                  Rekit.KitSlotEntry var60 = (Rekit.KitSlotEntry)var58.get(var59);
                  ItemStack var61 = var57;
                  Rekit.KitSlotEntry var62 = var60;
                  if (!this.a_bool_ItemStack_KitSlotEntry(var61, var62)) {
                     AbstractContainerMenu var63 = var2;
                     Rekit.KitSlotEntry var64 = var60;
                     int var65 = this.a_int_AbstractContainerMenu_Rekit_KitSlotEntry(var63, var1, var64);
                     if (var65 != -1) {
                        int var66 = var65;
                        int var67 = this.b_int_int(var66);
                        if (var52 <= 8 && var65 >= 9) {
                           int var68 = var67;
                           int var69 = var52;
                           this.a_void_int_int_ContainerInput(var68, var69, ContainerInput.SWAP);
                           return true;
                        }

                        if (var65 <= 8 && var52 >= 9) {
                           int var70 = var54;
                           int var71 = var65;
                           this.a_void_int_int_ContainerInput(var70, var71, ContainerInput.SWAP);
                           return true;
                        }

                        int var72 = var67;
                        int var73 = var54;
                        this.a_void_int_int(var72, var73);
                        return true;
                     }
                  }
               }
            }
         }

         return false;
      }
   }

   private void a_void_int_int(int var1, int var2) {
      this.a_void_int_int_ContainerInput(var1, 0, ContainerInput.PICKUP);
      this.a_void_int_int_ContainerInput(var2, 0, ContainerInput.PICKUP);
      this.a_void_int_int_ContainerInput(var1, 0, ContainerInput.PICKUP);
   }

   private void a_void_int_int_ContainerInput(int var1, int var2, ContainerInput var3) {
      h = System.currentTimeMillis();
      MultiPlayerGameMode var4 = mc.gameMode;
      int var5 = mc.player.containerMenu.containerId;
      LocalPlayer var6 = mc.player;
      var4.handleContainerInput(var5, var1, var2, var3, var6);
   }

   private boolean a_bool_ItemStack_KitSlotEntry(ItemStack var1, Rekit.KitSlotEntry var2) {
      if (var2 == null) {
         return true;
      }

      if (var1.isEmpty()) {
         return false;
      }

      if (var2.a != null && !var2.a.isEmpty()) {
         Identifier var3 = Identifier.tryParse(var2.a);
         if (var3 == null) {
            return false;
         }

         Identifier var4 = var3;
         Item var5 = (Item)BuiltInRegistries.ITEM.getValue(var4);
         if (var5 != null) {
            Item var6 = var1.getItem();
            Item var7 = var5;
            if (var6 == var7) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private int a_int_AbstractContainerMenu_Rekit_KitSlotEntry(AbstractContainerMenu var1, Rekit var2, Rekit.KitSlotEntry var3) {
      if (var3 != null && var3.a != null && !var3.a.isEmpty()) {
         Identifier var4 = Identifier.tryParse(var3.a);
         if (var4 == null) {
            return -1;
         }

         Identifier var5 = var4;
         Item var6 = (Item)BuiltInRegistries.ITEM.getValue(var5);
         if (var6 == null) {
            return -1;
         }

         for (int var7 = 0; var7 < 36; var7++) {
            if (!this.ignoreHotbar.getValue() || var7 > 8) {
               int var8 = var7;
               int var9 = this.b_int_int(var8);
               ItemStack var10 = var1.getSlot(var9).getItem();
               if (!var10.isEmpty()) {
                  ItemStack var11 = var10;
                  if (!this.a_bool_ItemStack(var11)) {
                     Item var12 = var10.getItem();
                     Item var13 = var6;
                     if (var12 == var13) {
                        Map var14 = var2.z();
                        Integer var15 = var7;
                        Rekit.KitSlotEntry var16 = (Rekit.KitSlotEntry)var14.get(var15);
                        ItemStack var17 = var10;
                        Rekit.KitSlotEntry var18 = var16;
                        if (!this.a_bool_ItemStack_KitSlotEntry(var17, var18)) {
                           return var7;
                        }
                     }
                  }
               }
            }
         }

         return -1;
      } else {
         return -1;
      }
   }

   private int a_int_AbstractContainerMenu(AbstractContainerMenu var1) {
      Rekit var2 = Night.moduleManager.getModule(Rekit.class);

      for (int var3 = 0; var3 < 36; var3++) {
         if (!this.ignoreHotbar.getValue() || var3 > 8) {
            int var7 = var3;
            int var8 = this.b_int_int(var7);
            if (var1.getSlot(var8).getItem().isEmpty()) {
               Map var9 = var2.z();
               Integer var10 = var3;
               if (var9.get(var10) == null) {
                  return var8;
               }
            }
         }
      }

      for (int var4 = 0; var4 < 36; var4++) {
         if (!this.ignoreHotbar.getValue() || var4 > 8) {
            int var5 = var4;
            int var6 = this.b_int_int(var5);
            if (var1.getSlot(var6).getItem().isEmpty()) {
               return var6;
            }
         }
      }

      return -1;
   }

   private int b_int_int(int var1) {
      if (var1 >= 0 && var1 <= 8) {
         return 36 + var1;
      } else {
         return var1 >= 9 && var1 <= 35 ? var1 : -1;
      }
   }

   private boolean a_bool_ItemStack(ItemStack var1) {
      if (!var1.isEmpty()) {
         Item var2 = var1.getItem();
         if (var2 instanceof BlockItem && ((BlockItem)var2).getBlock() instanceof ShulkerBoxBlock) {
            return true;
         }
      }

      return false;
   }
}
