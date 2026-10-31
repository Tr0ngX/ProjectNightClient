package night.module.impl.kingmc;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import night.Night;
import night.events.EventTarget;
import night.events.impl.TotemPopEvent;
import night.events.impl.UpdateWalkingPlayerEvent;
import night.mixins.accessors.ClientPlayerInteractionManagerAccessor;
import night.module.Module;
import night.module.ModuleInfo;
import night.module.impl.combat.Mainhand;
import night.module.impl.combat.Suicide;
import night.pingbypass.PingBypassState;
import night.setting.impl.BooleanSetting;
import night.setting.impl.NumberSetting;
import night.setting.impl.RangeNumberSetting;
import night.util.inventory.InventoryUtil;
import org.slf4j.Logger;

@ModuleInfo(
   name = "KingMCTotem",
   description = "Buys totems with AutoShop on every pop instead of swapping them out of the inventory (KingMC).",
   category = Module.Category.KINGMC
)
public class KingMCTotem extends Module {
   public BooleanSetting inventoryTotem = new BooleanSetting(
      "InventoryTotem", "Keeps only a set number of totems in the main inventory and throws the rest. Off = never throws totems.", true
   );
   public NumberSetting amount;
   public RangeNumberSetting slot;
   private static final long h = 2000L;
   private static final long i = 3000L;
   private static final int j = 2;
   private long k;
   private volatile boolean l;
   private volatile long m;

   public KingMCTotem() {
      BooleanSetting var2 = this.inventoryTotem;
      BooleanSetting.BooleanCondition var1 = new BooleanSetting.BooleanCondition(var2, true);
      Integer var3 = 3;
      Integer var4 = 0;
      Integer var5 = 10;
      this.amount = new NumberSetting("Amount", "How many totems to keep in the main inventory.", var1, var3, var4, var5);
      this.slot = new RangeNumberSetting(
         "Slot", "Hotbar slots (1-9) kept for totems: any non-totem item there is moved up to the inventory to make room.", 1, 1, 1, 9
      );
      long var6 = 0L;
      this.k = var6;
      this.l = false;
      long var8 = 0L;
      this.m = var8;
   }

   @Override
   public void onEnable() {
      long var1 = 0L;
      this.k = var1;
      long var3 = 0L;
      this.m = var3;
      this.l = false;
   }

   @EventTarget(priority = Integer.MAX_VALUE)
   public void a(TotemPopEvent var1) {
      if (!PingBypassState.a()) {
         Player var2 = var1.getPlayer();
         LocalPlayer var3 = mc.player;
         if (var2 == var3 && !Night.moduleManager.getModule(Suicide.class).isEnabled()) {
            if (this.isEnabled()) {
               if (mc.player.getMainHandItem().is(Items.TOTEM_OF_UNDYING)) {
                  long var4 = System.currentTimeMillis();
                  this.m = var4;
               }

               this.l = true;
            }
         }
      }
   }

   @EventTarget
   public void a(UpdateWalkingPlayerEvent var1) {
      if (mc.player != null && mc.level != null && mc.player.isAlive()) {
         if (!PingBypassState.a()) {
            this.x();
            this.y();
            this.z();
            if (!InventoryUtil.a() && mc.player.containerMenu.getCarried().isEmpty()) {
               this.A();
               AutoShop var2 = Night.moduleManager.getModule(AutoShop.class);
               if (var2 == null || !var2.w()) {
                  this.B();
               }
            }

            if (this.w() <= 2) {
               if (InventoryUtil.d(InventoryUtil.c, InventoryUtil.d) != -1) {
                  if (!this.l) {
                     long var3 = System.currentTimeMillis();
                     long var5 = this.k;
                     long var7 = var3 - var5;
                     long var9 = 2000L;
                     if (var7 < var9) {
                        return;
                     }
                  }

                  AutoShop var11 = Night.moduleManager.getModule(AutoShop.class);
                  if (var11 != null) {
                     if (this.l || !var11.x()) {
                        String var12;
                        if (this.l) {
                           var12 = "pop, hotbar totems=" + this.w();
                        } else {
                           var12 = "hotbar totems=" + this.w();
                        }

                        this.b_void_String(var12);
                     }
                  }
               }
            }
         }
      }
   }

   private boolean v() {
      if (this.isEnabled() && mc.player != null) {
         AutoShop var1 = Night.moduleManager.getModule(AutoShop.class);
         boolean var2;
         if (var1 != null && var1.w()) {
            var2 = false;
         } else {
            var2 = true;
         }

         return var2;
      } else {
         return false;
      }
   }

   public boolean b_bool_int(int var1) {
      if (this.v()) {
         int var2 = this.slot.getFirstValue() - 1;
         if (var1 >= var2) {
            int var3 = this.slot.getSecondValue() - 1;
            if (var1 <= var3) {
               return true;
            }
         }

         return false;
      } else {
         return true;
      }
   }

   public int a(int var1, int var2, int var3) {
      if (!this.v()) {
         return var3;
      }

      int var4 = this.slot.getFirstValue() - 1;
      int var5 = this.slot.getSecondValue();
      int var6 = this.slot.getFirstValue();
      int var7 = var5 - var6 + 1;
      int var8 = var4;
      int var9 = var4;
      int var10 = var2 - var9 - var1;
      int var11 = var7;
      int var12 = Math.floorMod(var10, var11);
      return var8 + var12;
   }

   private int w() {
      int var1 = 0;

      for (int var2 = InventoryUtil.c; var2 <= InventoryUtil.d; var2++) {
         Inventory var3 = mc.player.getInventory();
         int var4 = var2;
         if (var3.getItem(var4).is(Items.TOTEM_OF_UNDYING)) {
            var1++;
         }
      }

      return var1;
   }

   private void x() {
      long var1 = this.m;
      long var3 = 0L;
      if (var1 != var3) {
         long var5 = System.currentTimeMillis();
         long var7 = this.m;
         long var9 = var5 - var7;
         long var11 = 3000L;
         if (var9 > var11) {
            long var13 = 0L;
            this.m = var13;
         } else {
            Inventory var15 = mc.player.getInventory();
            int var16 = mc.player.getInventory().getSelectedSlot();
            ItemStack var17 = var15.getItem(var16);
            if (!var17.is(Items.TOTEM_OF_UNDYING)) {
               if (!var17.isEmpty()) {
                  long var18 = 0L;
                  this.m = var18;
               } else {
                  int var20 = InventoryUtil.a_int_Item_int_int(Items.TOTEM_OF_UNDYING, InventoryUtil.c, InventoryUtil.d);
                  if (var20 != -1) {
                     Inventory var21 = mc.player.getInventory();
                     int var22 = var20;
                     var21.setSelectedSlot(var22);
                     ((ClientPlayerInteractionManagerAccessor)mc.gameMode).invokeSyncSelectedSlot();
                     long var23 = 0L;
                     this.m = var23;
                  }
               }
            }
         }
      }
   }

   private void y() {
      if (Night.moduleManager.getModule(Mainhand.class).x()) {
         if (!mc.player.getMainHandItem().is(Items.TOTEM_OF_UNDYING)) {
            int var1 = InventoryUtil.a_int_Item_int_int(Items.TOTEM_OF_UNDYING, InventoryUtil.c, InventoryUtil.d);
            if (var1 != -1) {
               Inventory var2 = mc.player.getInventory();
               int var3 = var1;
               var2.setSelectedSlot(var3);
               ((ClientPlayerInteractionManagerAccessor)mc.gameMode).invokeSyncSelectedSlot();
            }
         }
      }
   }

   private void z() {
      if (this.v() && !mc.options.keyUse.isDown() && !mc.player.isUsingItem()) {
         int var1 = mc.player.getInventory().getSelectedSlot();
         int var2 = this.slot.getFirstValue() - 1;
         int var3 = this.slot.getSecondValue() - 1;
         Inventory var4 = mc.player.getInventory();
         int var5 = var1;
         if (var4.getItem(var5).is(Items.TOTEM_OF_UNDYING)) {
            int var6 = var1;
            int var7 = var2;
            if (var6 >= var7) {
               int var8 = var1;
               int var9 = var3;
               if (var8 <= var9) {
                  return;
               }
            }
         }

         int var10 = var2;
         int var11 = var3;
         int var12 = InventoryUtil.a_int_Item_int_int(Items.TOTEM_OF_UNDYING, var10, var11);
         if (var12 == -1) {
            if (mc.player.getMainHandItem().is(Items.TOTEM_OF_UNDYING)) {
               return;
            }

            int var13 = var1;
            int var14 = var2;
            if (var13 >= var14) {
               int var15 = var1;
               int var16 = var3;
               if (var15 <= var16) {
                  return;
               }
            }

            var12 = var2;
         }

         Inventory var17 = mc.player.getInventory();
         int var18 = var12;
         var17.setSelectedSlot(var18);
         ((ClientPlayerInteractionManagerAccessor)mc.gameMode).invokeSyncSelectedSlot();
      }
   }

   private void A() {
      if (this.inventoryTotem.getValue()) {
         int var1 = this.amount.getValue().intValue();
         int var2 = 0;

         for (int var3 = InventoryUtil.e; var3 <= InventoryUtil.f; var3++) {
            Inventory var4 = mc.player.getInventory();
            int var5 = var3;
            if (var4.getItem(var5).is(Items.TOTEM_OF_UNDYING)) {
               int var6 = ++var2;
               int var7 = var1;
               if (var6 > var7) {
                  InventoryUtil.a_void_int_int_ContainerInput(InventoryUtil.e(var3), 1, ContainerInput.THROW);
               }
            }
         }
      }
   }

   private void B() {
      int var1 = this.slot.getFirstValue() - 1;

      while (true) {
         int var2 = var1;
         int var3 = this.slot.getSecondValue() - 1;
         if (var2 > var3) {
            return;
         }

         Inventory var4 = mc.player.getInventory();
         int var5 = var1;
         ItemStack var6 = var4.getItem(var5);
         if (!var6.isEmpty() && !var6.is(Items.TOTEM_OF_UNDYING)) {
            int var7 = InventoryUtil.d(InventoryUtil.e, InventoryUtil.f);
            if (var7 == -1) {
               return;
            }

            int var8 = var7;
            int var9 = var1;
            InventoryUtil.c("Swap", var8, var9);
         }

         var1++;
      }
   }

   private void b_void_String(String var1) {
      AutoShop var2 = Night.moduleManager.getModule(AutoShop.class);
      if (var2 != null) {
         long var3 = System.currentTimeMillis();
         this.k = var3;
         this.l = false;

         double var5;
         try {
            var5 = InventoryUtil.f(3);
         } catch (Throwable var29) {
            var5 = 0.0;
         }

         double var8 = var5;
         double var10 = 1.0;
         double var12 = Math.abs(var8 - var10);
         double var14 = 0.05;
         if (var12 > var14) {
            int var16 = mc.player.inventoryMenu.containerId;

            for (Slot var18 : (Iterable<Slot>)(Object)mc.player.inventoryMenu.slots) {
               if (var18 != null && var18.getItem().is(Items.TOTEM_OF_UNDYING)) {
                  MultiPlayerGameMode var19 = mc.gameMode;
                  int var20 = var16;
                  int var21 = var18.index;
                  LocalPlayer var22 = mc.player;
                  var19.handleContainerInput(var20, var21, 1, ContainerInput.THROW, var22);
               }
            }
         } else {
            if (var2.debug.getValue()) {
               Logger var23 = Night.LOGGER;
               long var24 = System.currentTimeMillis();
               long var26 = 100000L;
               String var28 = "[KingMCTotem] " + var24 % var26 + " buy: " + var1;
               var23.info(var28);
            }

            var2.v();
         }
      }
   }
}
