package night.module.impl.player;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.ShulkerBoxScreen;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
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

@ModuleInfo(
   name = "ChestStealer",
   description = "Automatically steals, dumps, or drops items to and from an open container.",
   category = Module.Category.PLAYER
)
public class ChestStealer extends Module {
   public BooleanSetting steal = new BooleanSetting("Steal", "Move matching items FROM the open container INTO your inventory.", true);
   public ModeSetting mode;
   public WhitelistSetting list;
   public BooleanSetting dump;
   public ModeSetting mode2;
   public WhitelistSetting list2;
   public BooleanSetting drop;
   public ModeSetting dropMode;
   public WhitelistSetting dropList;
   public ModeSetting interact;
   public NumberSetting delay;
   public NumberSetting actionsPerTick;
   public BooleanSetting buttonMode;
   public BooleanSetting ignoreCustomName;
   private int t;
   public static volatile long s = 0L;
   private ChestStealer.ActionMode u;
   private int v;
   private int w;
   private int x;
   private int y;
   private int z;

   public ChestStealer() {
      BooleanSetting var2 = this.steal;
      BooleanSetting.BooleanCondition var1 = new BooleanSetting.BooleanCondition(var2, true);
      String[] var3 = new String[]{"WhiteList", "BlackList", "All"};
      this.mode = new ModeSetting(
         "Mode", "WhiteList = steal items IN the list. BlackList = steal items NOT in the list. All = steal everything.", var1, "All", var3
      );
      BooleanSetting var4 = this.steal;
      this.list = new WhitelistSetting(
         "List", "Items to compare against.", new BooleanSetting.BooleanCondition(var4, true), WhitelistSetting.WhitelistType.ITEMS
      );
      this.dump = new BooleanSetting("Dump", "Move matching items FROM your inventory INTO the open container.", false);
      BooleanSetting var6 = this.dump;
      BooleanSetting.BooleanCondition var5 = new BooleanSetting.BooleanCondition(var6, true);
      String[] var7 = new String[]{"WhiteList", "BlackList", "All"};
      this.mode2 = new ModeSetting(
         "Mode", "WhiteList = dump items IN the list. BlackList = dump items NOT in the list. All = dump everything.", var5, "WhiteList", var7
      );
      BooleanSetting var8 = this.dump;
      this.list2 = new WhitelistSetting(
         "List", "Items to compare against.", new BooleanSetting.BooleanCondition(var8, true), WhitelistSetting.WhitelistType.ITEMS
      );
      this.drop = new BooleanSetting("Drop", "Throw matching items OUT (Q / THROW).", false);
      BooleanSetting var10 = this.drop;
      BooleanSetting.BooleanCondition var9 = new BooleanSetting.BooleanCondition(var10, true);
      String[] var11 = new String[]{"WhiteList", "BlackList", "All"};
      this.dropMode = new ModeSetting(
         "DropMode", "Mode", "WhiteList = drop items IN the list. BlackList = drop items NOT in the list. All = drop everything.", var9, "All", var11
      );
      BooleanSetting var12 = this.drop;
      this.dropList = new WhitelistSetting(
         "DropList", "List", "Items to compare against for drop.", new BooleanSetting.BooleanCondition(var12, true), WhitelistSetting.WhitelistType.ITEMS
      );
      String[] var13 = new String[]{"Hotbar", "Inventory", "Both"};
      this.interact = new ModeSetting("Interact", "Which of YOUR slots Dump/Drop act on: Hotbar (0-8), Inventory (9-35), or Both.", "Both", var13);
      Integer var14 = 1;
      Integer var15 = 0;
      Integer var16 = 20;
      this.delay = new NumberSetting("Delay", "Tick delay between action passes.", var14, var15, var16);
      Integer var17 = 5;
      Integer var18 = 1;
      Integer var19 = 36;
      this.actionsPerTick = new NumberSetting("ActionsPerTick", "Max slot actions per pass.", var17, var18, var19);
      this.buttonMode = new BooleanSetting("ButtonMode", "Adds vanilla Steal/Dump/Drop buttons at the top-right of the container/inventory GUI.", false);
      this.ignoreCustomName = new BooleanSetting(
         "IgnoreCustomName", "Skip any container with a custom (non-vanilla) title such as a shop GUI, except shulker boxes.", false
      );
      this.t = 0;
      this.u = ChestStealer.ActionMode.NONE;
      this.v = 0;
      this.w = -1;
      this.x = 0;
      this.y = 0;
      this.z = 0;
   }

   @Override
   public void onDisable() {
      this.u = ChestStealer.ActionMode.NONE;
      this.v = 0;
      this.x = 0;
      this.y = 0;
      this.z = 0;
      this.w = -1;
      this.t = 0;
   }

   @EventTarget
   public void a_void_TickEvent(TickEvent var1) {
      if (mc.player != null && mc.level != null && mc.gameMode != null) {
         if (!mc.player.isCreative()) {
            if (mc.player.containerMenu != null) {
               if (mc.player.containerMenu.getCarried().isEmpty()) {
                  int var2 = mc.player.containerMenu.containerId;
                  int var3 = this.w;
                  if (var2 != var3) {
                     int var4 = var2;
                     this.w = var4;
                     this.v = 0;
                     this.x = 0;
                     this.y = 0;
                     this.z = 0;
                     this.u = ChestStealer.ActionMode.NONE;
                  }

                  long var5 = System.currentTimeMillis();
                  long var7 = Rekit.v;
                  long var9 = var5 - var7;
                  long var11 = 200L;
                  if (var9 >= var11) {
                     long var13 = var5;
                     long var15 = InventorySorter.h;
                     long var17 = var13 - var15;
                     long var19 = 200L;
                     if (var17 >= var19) {
                        int var21 = this.t;
                        int var22 = this.delay.getValue().intValue();
                        if (var21 < var22) {
                           int var23 = this.t + 1;
                           this.t = var23;
                           return;
                        }

                        this.t = 0;
                        int var24 = this.actionsPerTick.getValue().intValue();
                        if (this.u != ChestStealer.ActionMode.NONE) {
                           boolean var25;
                           if (mc.gui.screen() instanceof AbstractContainerScreen && !(mc.gui.screen() instanceof InventoryScreen)) {
                              var25 = true;
                           } else {
                              var25 = false;
                           }

                           boolean var59 = var25;
                           boolean var60 = mc.gui.screen() instanceof InventoryScreen;
                           if (!var59 && !var60) {
                              this.u = ChestStealer.ActionMode.NONE;
                              this.v = 0;
                              return;
                           }

                           switch (this.u) {
                              case STEAL:
                                 if (var59) {
                                    int var68 = mc.player.containerMenu.slots.size() - 36;
                                    int var69 = this.v;
                                    String var70 = this.mode.getValue();
                                    WhitelistSetting var71 = this.list;
                                    int var72 = var24;
                                    int var73 = this.v;
                                    int var74 = this.a(var70, var71, var72, var73);
                                    int var75 = var69 + var74;
                                    this.v = var75;
                                    int var76 = this.v;
                                    int var77 = var68;
                                    if (var76 >= var77) {
                                       this.u = ChestStealer.ActionMode.NONE;
                                       this.v = 0;
                                    }
                                 } else {
                                    this.u = ChestStealer.ActionMode.NONE;
                                    this.v = 0;
                                 }
                                 break;
                              case DUMP:
                                 if (var59) {
                                    int var61 = this.v;
                                    String var62 = this.mode2.getValue();
                                    WhitelistSetting var63 = this.list2;
                                    int var64 = var24;
                                    int var65 = this.v;
                                    int var66 = this.b(var62, var63, var64, var65);
                                    int var67 = var61 + var66;
                                    this.v = var67;
                                    if (this.v >= 36) {
                                       this.u = ChestStealer.ActionMode.NONE;
                                       this.v = 0;
                                    }
                                 } else {
                                    this.u = ChestStealer.ActionMode.NONE;
                                    this.v = 0;
                                 }
                                 break;
                              case DROP:
                                 int var78;
                                 if (var59) {
                                    var78 = mc.player.containerMenu.slots.size() - 36;
                                 } else {
                                    var78 = 0;
                                 }

                                 int var79 = var78 + 36;
                                 int var80 = this.v;
                                 String var81 = this.dropMode.getValue();
                                 WhitelistSetting var82 = this.dropList;
                                 int var83 = var24;
                                 int var84 = this.v;
                                 int var85 = this.c(var81, var82, var83, var84);
                                 int var86 = var80 + var85;
                                 this.v = var86;
                                 int var87 = this.v;
                                 int var88 = var79;
                                 if (var87 >= var88) {
                                    this.u = ChestStealer.ActionMode.NONE;
                                    this.v = 0;
                                 }
                           }

                           return;
                        }

                        if (this.buttonMode.getValue()) {
                           return;
                        }

                        boolean var26;
                        if (mc.gui.screen() instanceof AbstractContainerScreen && !(mc.gui.screen() instanceof InventoryScreen)) {
                           var26 = true;
                        } else {
                           var26 = false;
                        }

                        boolean var27 = var26;
                        if (var27 && this.ignoreCustomName.getValue() && this.z() && !this.A()) {
                           return;
                        }

                        if (this.steal.getValue() && var27 && var24 > 0) {
                           int var28 = mc.player.containerMenu.slots.size() - 36;
                           String var29 = this.mode.getValue();
                           WhitelistSetting var30 = this.list;
                           int var31 = var24;
                           int var32 = this.x;
                           int var33 = this.a(var29, var30, var31, var32);
                           int var34 = this.x;
                           int var35 = var33;
                           int var36 = var34 + var35;
                           this.x = var36;
                           int var37 = this.x;
                           int var38 = var28;
                           if (var37 >= var38) {
                              this.x = 0;
                           }
                        }

                        if (this.dump.getValue() && var27 && var24 > 0) {
                           String var39 = this.mode2.getValue();
                           WhitelistSetting var40 = this.list2;
                           int var41 = var24;
                           int var42 = this.y;
                           int var43 = this.b(var39, var40, var41, var42);
                           int var44 = this.y;
                           int var45 = var43;
                           int var46 = var44 + var45;
                           this.y = var46;
                           if (this.y >= 36) {
                              this.y = 0;
                           }
                        }

                        if (this.drop.getValue() && var24 > 0) {
                           int var47;
                           if (var27) {
                              var47 = mc.player.containerMenu.slots.size() - 36;
                           } else {
                              var47 = 0;
                           }

                           int var48 = var47 + 36;
                           String var49 = this.dropMode.getValue();
                           WhitelistSetting var50 = this.dropList;
                           int var51 = var24;
                           int var52 = this.z;
                           int var53 = this.c(var49, var50, var51, var52);
                           int var54 = this.z;
                           int var55 = var53;
                           int var56 = var54 + var55;
                           this.z = var56;
                           int var57 = this.z;
                           int var58 = var48;
                           if (var57 >= var58) {
                              this.z = 0;
                           }
                        }

                        return;
                     }
                  }
               }
            }
         }
      }
   }

   public boolean v() {
      boolean var1;
      if (this.u != ChestStealer.ActionMode.NONE) {
         var1 = true;
      } else {
         var1 = false;
      }

      return var1;
   }

   public void w() {
      if (this.b(true)) {
         this.u = ChestStealer.ActionMode.STEAL;
         this.v = 0;
         int var1 = this.delay.getValue().intValue();
         this.t = var1;
      }
   }

   public void x() {
      if (this.b(true)) {
         this.u = ChestStealer.ActionMode.DUMP;
         this.v = 0;
         int var1 = this.delay.getValue().intValue();
         this.t = var1;
      }
   }

   public void y() {
      if (this.b(false)) {
         this.u = ChestStealer.ActionMode.DROP;
         this.v = 0;
         int var1 = this.delay.getValue().intValue();
         this.t = var1;
      }
   }

   public boolean z() {
      return mc.gui.screen() instanceof AbstractContainerScreen var2 ? a_bool_Component(var2.getTitle()) : false;
   }

   public boolean A() {
      if (mc.player != null && mc.player.containerMenu instanceof ShulkerBoxMenu) {
         return true;
      } else if (mc.gui.screen() instanceof ShulkerBoxScreen) {
         return true;
      } else if (mc.gui.screen() instanceof AbstractContainerScreen var2) {
         Component var3 = var2.getTitle();
         AbstractContainerScreen var4 = var2;
         AbstractContainerMenu var5 = var2.getMenu();
         return a(var3, var4, var5);
      } else {
         return false;
      }
   }

   public static boolean a(Component var0, Object var1, Object var2) {
      if (var2 instanceof ShulkerBoxMenu) {
         return true;
      }

      if (var1 instanceof ShulkerBoxScreen) {
         return true;
      }

      if (var0 != null) {
         String var3 = ChatFormatting.stripFormatting(var0.getString()).trim().toLowerCase();
         if (var3.contains("shulker") || var3.contains("h\u1ed9p shulker")) {
            return true;
         }

         ComponentContents var4 = var0.getContents();
         if (var4 instanceof TranslatableContents && ((TranslatableContents)var4).getKey().toLowerCase().contains("shulker")) {
            return true;
         }
      }

      return false;
   }

   public static boolean a_bool_Component(Component var0) {
      if (var0 == null) {
         return false;
      }

      ComponentContents var1 = var0.getContents();
      return var1 instanceof TranslatableContents
         ? !b(((TranslatableContents)var1).getKey())
         : !c_bool_String(ChatFormatting.stripFormatting(var0.getString()).trim().toLowerCase());
   }

   public static boolean b(String var0) {
      if (var0 == null) {
         return false;
      }

      boolean var1;
      if (!var0.equals("container.chest")
         && !var0.equals("container.chestDouble")
         && !var0.equals("container.barrel")
         && !var0.equals("container.enderchest")
         && !var0.equals("container.shulkerBox")
         && !var0.startsWith("container.shulkerBox.")
         && !var0.equals("container.hopper")
         && !var0.equals("container.dispenser")
         && !var0.equals("container.dropper")
         && !var0.equals("container.furnace")
         && !var0.equals("container.blast_furnace")
         && !var0.equals("container.smoker")
         && !var0.equals("container.brewing")
         && !var0.equals("container.anvil")
         && !var0.equals("container.beacon")
         && !var0.equals("container.crafting")
         && !var0.equals("container.enchant")
         && !var0.equals("container.grindstone")
         && !var0.equals("container.lectern")
         && !var0.equals("container.loom")
         && !var0.equals("container.stonecutter")
         && !var0.equals("container.cartography_table")
         && !var0.equals("container.smithing")
         && !var0.equals("container.crafter")
         && !var0.equals("entity.minecraft.chest_minecart")
         && !var0.equals("entity.minecraft.hopper_minecart")) {
         var1 = false;
      } else {
         var1 = true;
      }

      return var1;
   }

   public static boolean c_bool_String(String var0) {
      if (var0 == null || var0.isEmpty()) {
         return false;
      } else {
         return var0.equals("chest")
               || var0.equals("large chest")
               || var0.equals("barrel")
               || var0.equals("ender chest")
               || var0.equals("shulker box")
               || var0.endsWith("shulker box")
               || var0.equals("hopper")
               || var0.equals("dispenser")
               || var0.equals("dropper")
               || var0.equals("furnace")
               || var0.equals("blast furnace")
               || var0.equals("smoker")
               || var0.equals("brewing stand")
               || var0.equals("anvil")
               || var0.equals("repair & name")
               || var0.equals("repair and name")
               || var0.equals("beacon")
               || var0.equals("crafting")
               || var0.equals("crafting table")
               || var0.equals("enchant")
               || var0.equals("enchanting table")
               || var0.equals("grindstone")
               || var0.equals("lectern")
               || var0.equals("loom")
               || var0.equals("stonecutter")
               || var0.equals("cartography table")
               || var0.equals("smithing table")
               || var0.equals("upgrade gear")
               || var0.equals("crafter")
               || var0.equals("minecart with chest")
               || var0.equals("minecart with hopper")
            ? true
            : var0.equals("r\u01b0\u01a1ng")
               || var0.equals("r\u01b0\u01a1ng l\u1edbn")
               || var0.equals("th\u00f9ng")
               || var0.equals("r\u01b0\u01a1ng ender")
               || var0.equals("h\u1ed9p shulker")
               || var0.contains("h\u1ed9p shulker")
               || var0.equals("ph\u1ec5u")
               || var0.equals("m\u00e1y ph\u00e2n ph\u00e1t")
               || var0.equals("m\u00e1y th\u1ea3")
               || var0.equals("l\u00f2 nung")
               || var0.equals("l\u00f2 luy\u1ec7n kim")
               || var0.equals("l\u00f2 hun kh\u00f3i")
               || var0.equals("gi\u00e0n pha thu\u1ed1c")
               || var0.equals("\u0111e")
               || var0.contains("s\u1eeda & \u0111\u1eb7t t\u00ean")
               || var0.contains("s\u1eeda v\u00e0 \u0111\u1eb7t t\u00ean")
               || var0.equals("\u0111\u00e8n hi\u1ec7u")
               || var0.equals("b\u00e0n ch\u1ebf t\u1ea1o")
               || var0.equals("ch\u1ebf t\u1ea1o")
               || var0.equals("b\u00e0n ph\u00f9 ph\u00e9p")
               || var0.equals("b\u00e0n m\u00e0i")
               || var0.equals("b\u1ee5c \u0111\u1ecdc s\u00e1ch")
               || var0.equals("khung c\u1eedi")
               || var0.equals("m\u00e1y c\u1eaft \u0111\u00e1")
               || var0.equals("b\u00e0n v\u1ebd b\u1ea3n \u0111\u1ed3")
               || var0.equals("b\u00e0n r\u00e8n")
               || var0.equals("b\u00e0n ch\u1ebf t\u1ea1o t\u1ef1 \u0111\u1ed9ng")
               || var0.equals("xe m\u1ecf c\u00f3 r\u01b0\u01a1ng")
               || var0.equals("xe m\u1ecf c\u00f3 ph\u1ec5u");
      }
   }

   private boolean b(boolean var1) {
      if (mc.player == null || mc.level == null || mc.gameMode == null) {
         return false;
      }

      if (mc.player.isCreative()) {
         return false;
      }

      if (mc.player.containerMenu == null) {
         return false;
      }

      if (!mc.player.containerMenu.getCarried().isEmpty()) {
         return false;
      }

      if (var1) {
         boolean var2;
         if (mc.gui.screen() instanceof AbstractContainerScreen && !(mc.gui.screen() instanceof InventoryScreen)) {
            var2 = true;
         } else {
            var2 = false;
         }

         boolean var3 = var2;
         boolean var4 = mc.gui.screen() instanceof InventoryScreen;
         if (!var3 && !var4) {
            return false;
         }
      }

      return true;
   }

   private int a(String var1, WhitelistSetting var2, int var3, int var4) {
      if (mc.gui.screen() instanceof InventoryScreen) {
         return 0;
      }

      int var5 = mc.player.containerMenu.containerId;
      int var6 = mc.player.containerMenu.slots.size() - 36;
      int var7 = 0;
      int var8 = var4;

      while (true) {
         int var9 = var8;
         int var10 = var6;
         if (var9 >= var10 || var7 >= var3) {
            int var17 = var8 - var4;
            return Math.max(1, var17);
         }

         NonNullList var11 = mc.player.containerMenu.slots;
         int var12 = var8;
         ItemStack var13 = ((Slot)var11.get(var12)).getItem();
         if (!var13.isEmpty()) {
            ItemStack var14 = var13;
            if (this.a(var14, var1, var2)) {
               int var15 = var5;
               int var16 = var8;
               this.a(var15, var16);
               var7++;
            }
         }

         var8++;
      }
   }

   private int b(String var1, WhitelistSetting var2, int var3, int var4) {
      if (mc.gui.screen() instanceof InventoryScreen) {
         return 0;
      }

      int var5 = mc.player.containerMenu.containerId;
      int var6 = 0;

      int var7;
      for (var7 = var4; var7 < 36 && var6 < var3; var7++) {
         int var8 = var7;
         if (this.b(var8)) {
            Inventory var9 = mc.player.getInventory();
            int var10 = var7;
            ItemStack var11 = var9.getItem(var10);
            if (!var11.isEmpty()) {
               ItemStack var12 = var11;
               if (this.a(var12, var1, var2)) {
                  int var13 = var7;
                  int var14 = this.c_int_int(var13);
                  if (var14 >= 0) {
                     int var15 = var5;
                     int var16 = var14;
                     this.a(var15, var16);
                     var6++;
                  }
               }
            }
         }
      }

      int var17 = var7 - var4;
      return Math.max(1, var17);
   }

   private int c(String var1, WhitelistSetting var2, int var3, int var4) {
      int var5 = mc.player.containerMenu.containerId;
      boolean var6;
      if (mc.gui.screen() instanceof AbstractContainerScreen && !(mc.gui.screen() instanceof InventoryScreen)) {
         var6 = true;
      } else {
         var6 = false;
      }

      int var7;
      if (var6) {
         var7 = mc.player.containerMenu.slots.size() - 36;
      } else {
         var7 = 0;
      }

      byte var8 = (byte)var7;
      int var9 = var8 + 36;
      int var10 = 0;
      int var11 = var4;

      while (true) {
         int var12 = var11;
         int var13 = var9;
         if (var12 >= var13 || var10 >= var3) {
            int var33 = var11 - var4;
            return Math.max(1, var33);
         }

         int var14 = var11;
         byte var15 = var8;
         if (var14 < var15) {
            NonNullList var16 = mc.player.containerMenu.slots;
            int var17 = var11;
            ItemStack var18 = ((Slot)var16.get(var17)).getItem();
            if (!var18.isEmpty()) {
               ItemStack var19 = var18;
               if (this.a(var19, var1, var2)) {
                  int var20 = var5;
                  int var21 = var11;
                  this.b(var20, var21);
                  var10++;
               }
            }
         } else {
            int var22 = var11;
            byte var23 = var8;
            int var24 = var22 - var23;
            if (this.b(var24)) {
               Inventory var25 = mc.player.getInventory();
               int var26 = var24;
               ItemStack var27 = var25.getItem(var26);
               if (!var27.isEmpty()) {
                  ItemStack var28 = var27;
                  if (this.a(var28, var1, var2)) {
                     int var29 = var24;
                     int var30 = this.c_int_int(var29);
                     if (var30 >= 0) {
                        int var31 = var5;
                        int var32 = var30;
                        this.b(var31, var32);
                        var10++;
                     }
                  }
               }
            }
         }

         var11++;
      }
   }

   private void a(int var1, int var2) {
      MultiPlayerGameMode var3 = mc.gameMode;
      LocalPlayer var4 = mc.player;
      var3.handleContainerInput(var1, var2, 0, ContainerInput.QUICK_MOVE, var4);
      s = System.currentTimeMillis();
   }

   private void b(int var1, int var2) {
      MultiPlayerGameMode var3 = mc.gameMode;
      LocalPlayer var4 = mc.player;
      var3.handleContainerInput(var1, var2, 1, ContainerInput.THROW, var4);
      s = System.currentTimeMillis();
   }

   private boolean a(ItemStack var1, String var2, WhitelistSetting var3) {
      if (var1 != null && !var1.isEmpty()) {
         boolean var5;
         label34: {
            if (var3 != null) {
               Item var4 = var1.getItem();
               if (var3.contains(var4)) {
                  var5 = true;
                  break label34;
               }
            }

            var5 = false;
         }

         boolean var6 = var5;

         return switch (var2) {
            case "All" -> true;
            case "BlackList" -> {
               if (!var6) {
                  yield true;
               } else {
                  yield false;
               }
            }
            default -> var6;
         };
      } else {
         return false;
      }
   }

   private boolean b(int var1) {
      return switch (this.interact.getValue()) {
         case "Hotbar" -> {
            if (var1 <= 8) {
               yield true;
            } else {
               yield false;
            }
         }
         case "Inventory" -> {
            if (var1 >= 9) {
               yield true;
            } else {
               yield false;
            }
         }
         default -> true;
      };
   }

   private int c_int_int(int var1) {
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

   @Override
   public String getDisplayInfo() {
      StringBuilder var1 = new StringBuilder();
      if (this.steal.getValue()) {
         var1.append("Steal");
      }

      if (this.dump.getValue()) {
         StringBuilder var2 = var1;
         String var3;
         if (var1.length() > 0) {
            var3 = "/Dump";
         } else {
            var3 = "Dump";
         }

         var2.append(var3);
      }

      if (this.drop.getValue()) {
         StringBuilder var4 = var1;
         String var5;
         if (var1.length() > 0) {
            var5 = "/Drop";
         } else {
            var5 = "Drop";
         }

         var4.append(var5);
      }

      return var1.toString();
   }

   enum ActionMode {
      NONE,
      STEAL,
      DUMP,
      DROP;
   }
}
