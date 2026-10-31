package night.module.impl.combat;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import night.Night;
import night.events.EventTarget;
import night.events.impl.TotemPopEvent;
import night.events.impl.UpdateWalkingPlayerEvent;
import night.mixins.accessors.ClientPlayerInteractionManagerAccessor;
import night.module.Module;
import night.module.ModuleInfo;
import night.module.impl.misc.FakePlayer;
import night.pingbypass.PingBypassState;
import night.setting.impl.BooleanSetting;
import night.setting.impl.NumberSetting;
import night.setting.impl.RangeNumberSetting;
import night.util.inventory.InventoryUtil;
import night.util.world.EntityUtil;

@ModuleInfo(name = "Mainhand", description = "Automatically manages totems and golden apples in your mainhand.", category = Module.Category.COMBAT)
public class Mainhand extends Module {
   public RangeNumberSetting slot = new RangeNumberSetting(
      "Slot", "Hotbar slots (1-9) kept full of totems. Refilled all at once, only after every slot in the range has run out.", 1, 1, 1, 9
   );
   public NumberSetting delay;
   public BooleanSetting useGapple;
   public BooleanSetting lethalOverride;
   public NumberSetting health;
   public BooleanSetting elytraCheck;
   public NumberSetting fallDistance;
   public BooleanSetting antiMace;
   public NumberSetting maceRange;
   private long o;
   private boolean p;
   private int q;
   private boolean r;
   private int s;
   private int t;
   public static volatile boolean n = false;

   public Mainhand() {
      Integer var1 = 0;
      Integer var2 = 0;
      Integer var3 = 2000;
      this.delay = new NumberSetting("Delay", "Delay (ms) before refilling totems to hotbar.", var1, var2, var3);
      this.useGapple = new BooleanSetting("UseGapple", "Switches to golden apple when right clicking while holding a totem in mainhand.", true);
      BooleanSetting var4 = this.useGapple;
      this.lethalOverride = new BooleanSetting(
         "LethalOverride",
         "Allows eating a golden apple regardless of health when no enemies are in visual range.",
         new BooleanSetting.BooleanCondition(var4, true),
         false
      );
      Integer var5 = 16;
      Integer var6 = 0;
      Integer var7 = 36;
      this.health = new NumberSetting("Health", "The health at which a totem will be prioritized.", var5, var6, var7);
      this.elytraCheck = new BooleanSetting("ElytraCheck", "Prioritizes a totem whenever you're wearing an elytra.", true);
      Float var8 = 20.0F;
      Float var9 = 0.0F;
      Float var10 = 80.0F;
      this.fallDistance = new NumberSetting("FallDistance", "The fall distance at which the module will prioritize a totem.", var8, var9, var10);
      this.antiMace = new BooleanSetting("AntiMace", "Switches to a totem if a player near you is trying to smash attack you with a mace.", false);
      BooleanSetting var12 = this.antiMace;
      BooleanSetting.BooleanCondition var11 = new BooleanSetting.BooleanCondition(var12, true);
      Float var13 = 12.0F;
      Float var14 = 0.0F;
      Float var15 = 24.0F;
      this.maceRange = new NumberSetting(
         "MaceRange", "The distance at which an enemy has to be in with a mace in order to swap to a totem.", var11, var13, var14, var15
      );
      long var16 = 0L;
      this.o = var16;
      this.p = false;
      this.q = -1;
      this.r = false;
      this.s = -1;
      this.t = -1;
   }

   @Override
   public void onEnable() {
      long var1 = 0L;
      this.o = var1;
   }

   @EventTarget(priority = Integer.MAX_VALUE)
   public void a_void_TotemPopEvent(TotemPopEvent var1) {
      if (!PingBypassState.a()) {
         Player var2 = var1.getPlayer();
         LocalPlayer var3 = mc.player;
         if (var2 == var3 && !Night.moduleManager.getModule(Suicide.class).isEnabled()) {
            if (this.isEnabled()) {
               mc.execute(() -> {
                  if (mc.player != null && mc.level != null) {
                     if (this.v()) {
                        this.y();
                     }
                  }
               });
            }
         }
      }
   }

   private void y() {
      long var1 = this.delay.getValue().longValue();
      long var3 = 0L;
      if (var1 <= var3) {
         this.z();
      } else {
         long var5 = System.currentTimeMillis();
         long var7 = this.o;
         long var9 = 0L;
         if (var7 == var9) {
            long var11 = var5;
            long var13 = var1;
            long var15 = var11 + var13;
            this.o = var15;
         } else {
            long var17 = var5;
            long var19 = this.o;
            if (var17 >= var19) {
               long var21 = 0L;
               this.o = var21;
               this.z();
            }
         }
      }
   }

   public boolean v() {
      int var1 = this.slot.getFirstValue() - 1;

      while (true) {
         int var2 = var1;
         int var3 = this.slot.getSecondValue() - 1;
         if (var2 > var3) {
            return true;
         }

         Inventory var4 = mc.player.getInventory();
         int var5 = var1;
         if (var4.getItem(var5).getItem() == Items.TOTEM_OF_UNDYING) {
            return false;
         }

         var1++;
      }
   }

   private void z() {
      if (mc.player != null && mc.level != null) {
         if (!PingBypassState.a()) {
            Screen var1 = mc.gui.screen();
            if (!(var1 instanceof AbstractContainerScreen) || var1 instanceof InventoryScreen || mc.player.containerMenu.getCarried().isEmpty()) {
               if (this.v()) {
                  if (!InventoryUtil.a()) {
                     boolean var2 = false;
                     int var3 = this.slot.getFirstValue() - 1;

                     while (true) {
                        int var4 = var3;
                        int var5 = this.slot.getSecondValue() - 1;
                        if (var4 > var5) {
                           break;
                        }

                        int var6 = this.C();
                        if (var6 == -1) {
                           break;
                        }

                        Inventory var7 = mc.player.getInventory();
                        int var8 = var3;
                        ItemStack var9 = var7.getItem(var8);
                        int var10 = var6;
                        int var11 = var3;
                        InventoryUtil.c("Swap", var10, var11);
                        Inventory var12 = mc.player.getInventory();
                        int var13 = var3;
                        ItemStack var14 = var12.getItem(var13);
                        ItemStack var15 = var9;
                        if (var14 != var15) {
                           var2 = true;
                        }

                        var3++;
                     }

                     if (var2) {
                        InventoryUtil.f();
                     }
                  }
               }
            }
         }
      }
   }

   public boolean w() {
      return this.isEnabled();
   }

   @EventTarget(priority = Integer.MAX_VALUE)
   public void a_void_UpdateWalkingPlayerEvent(UpdateWalkingPlayerEvent var1) {
      this.A();
   }

   private void A() {
      if (mc.player != null && mc.level != null) {
         if (!PingBypassState.a()) {
            Screen var1 = mc.gui.screen();
            boolean var2;
            if (var1 instanceof AbstractContainerScreen && !(var1 instanceof InventoryScreen)) {
               var2 = true;
            } else {
               var2 = false;
            }

            if (!var2 || mc.player.containerMenu.getCarried().isEmpty()) {
               boolean var3;
               if (!mc.options.keyUse.isDown() && !mc.player.isUsingItem()) {
                  var3 = false;
               } else {
                  var3 = true;
               }

               boolean var4 = var3;
               if (this.p) {
                  if (!var4) {
                     if (this.q != -1) {
                        Inventory var5 = mc.player.getInventory();
                        int var6 = this.q;
                        var5.setSelectedSlot(var6);
                        ((ClientPlayerInteractionManagerAccessor)mc.gameMode).invokeSyncSelectedSlot();
                     }

                     this.p = false;
                     this.q = -1;
                  }
               } else if (this.r) {
                  if (!var4) {
                     if (this.s != -1 && this.t != -1) {
                        int var7 = this.s;
                        int var8 = this.t;
                        InventoryUtil.c("Swap", var7, var8);
                     }

                     this.r = false;
                     this.s = -1;
                     this.t = -1;
                  }
               } else {
                  boolean var9;
                  if (this.F() && (!this.lethalOverride.getValue() || this.G())) {
                     var9 = false;
                  } else {
                     var9 = true;
                  }

                  boolean var10 = var9;
                  if (this.useGapple.getValue() && mc.options.keyUse.isDown() && mc.player.getMainHandItem().is(Items.TOTEM_OF_UNDYING) && var10) {
                     int var11 = this.D();
                     if (var11 != -1) {
                        int var12 = mc.player.getInventory().getSelectedSlot();
                        this.q = var12;
                        Inventory var13 = mc.player.getInventory();
                        int var14 = var11;
                        var13.setSelectedSlot(var14);
                        ((ClientPlayerInteractionManagerAccessor)mc.gameMode).invokeSyncSelectedSlot();
                        this.p = true;
                        return;
                     }

                     int var15 = this.E();
                     if (var15 != -1) {
                        int var16 = var15;
                        this.s = var16;
                        int var17 = mc.player.getInventory().getSelectedSlot();
                        this.t = var17;
                        int var18 = var15;
                        int var19 = this.t;
                        InventoryUtil.c("Swap", var18, var19);
                        this.r = true;
                        return;
                     }
                  }

                  if (this.v()) {
                     this.y();
                  } else {
                     long var20 = 0L;
                     this.o = var20;
                  }

                  if (this.F()) {
                     int var22 = mc.player.getInventory().getSelectedSlot();
                     Inventory var23 = mc.player.getInventory();
                     int var24 = var22;
                     if (var23.getItem(var24).getItem() != Items.TOTEM_OF_UNDYING) {
                        int var25 = this.B();
                        if (var25 == -1) {
                           var25 = InventoryUtil.a_int_Item_int_int(Items.TOTEM_OF_UNDYING, InventoryUtil.c, InventoryUtil.d);
                        }

                        if (var25 != -1) {
                           Inventory var26 = mc.player.getInventory();
                           int var27 = var25;
                           var26.setSelectedSlot(var27);
                           ((ClientPlayerInteractionManagerAccessor)mc.gameMode).invokeSyncSelectedSlot();
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private int B() {
      int var1 = this.slot.getFirstValue() - 1;

      while (true) {
         int var2 = var1;
         int var3 = this.slot.getSecondValue() - 1;
         if (var2 > var3) {
            return -1;
         }

         Inventory var4 = mc.player.getInventory();
         int var5 = var1;
         if (var4.getItem(var5).getItem() == Items.TOTEM_OF_UNDYING) {
            return var1;
         }

         var1++;
      }
   }

   private int C() {
      int var1 = InventoryUtil.c_int_Item(Items.TOTEM_OF_UNDYING);
      if (var1 != -1) {
         return var1;
      }

      for (int var2 = InventoryUtil.c; var2 <= InventoryUtil.d; var2++) {
         int var3 = var2;
         int var4 = this.slot.getFirstValue() - 1;
         if (var3 >= var4) {
            int var5 = var2;
            int var6 = this.slot.getSecondValue() - 1;
            if (var5 <= var6) {
               continue;
            }
         }

         Inventory var7 = mc.player.getInventory();
         int var8 = var2;
         if (var7.getItem(var8).getItem() == Items.TOTEM_OF_UNDYING) {
            return var2;
         }
      }

      return -1;
   }

   private int D() {
      int var1 = InventoryUtil.a_int_Item_int_int(Items.ENCHANTED_GOLDEN_APPLE, InventoryUtil.c, InventoryUtil.d);
      return var1 != -1 ? var1 : InventoryUtil.a_int_Item_int_int(Items.GOLDEN_APPLE, InventoryUtil.c, InventoryUtil.d);
   }

   private int E() {
      int var1 = InventoryUtil.a_int_Item_int_int(Items.ENCHANTED_GOLDEN_APPLE, InventoryUtil.e, InventoryUtil.f);
      return var1 != -1 ? var1 : InventoryUtil.a_int_Item_int_int(Items.GOLDEN_APPLE, InventoryUtil.e, InventoryUtil.f);
   }

   public boolean x() {
      if (!this.isEnabled()) {
         return false;
      } else if (mc.player == null) {
         return false;
      } else if (!this.p && !this.r) {
         return InventoryUtil.a_int_Item(Items.TOTEM_OF_UNDYING) == -1 ? false : this.F();
      } else {
         return false;
      }
   }

   private boolean F() {
      if (Night.moduleManager.getModule(Suicide.class).isEnabled() && Night.moduleManager.getModule(Suicide.class).offhandOverride.getValue()) {
         return false;
      }

      float var1 = mc.player.getHealth();
      float var2 = mc.player.getAbsorptionAmount();
      float var3 = var1 + var2;
      float var4 = this.health.getValue().floatValue();
      float var5 = (float)EntityUtil.b_double_int(6);
      float var6 = var4 * var5;
      if (var3 <= var6) {
         return true;
      }

      double var7 = mc.player.fallDistance;
      double var9 = this.fallDistance.getValue().floatValue();
      if (var7 > var9) {
         return true;
      }

      if (this.elytraCheck.getValue() && mc.player.getItemBySlot(EquipmentSlot.CHEST).getItem() == Items.ELYTRA) {
         return true;
      }

      boolean var11;
      if (this.antiMace.getValue() && mc.level.players().stream().anyMatch(var1x -> {
         LocalPlayer var2x = mc.player;
         if (var1x != var2x) {
            String var3x = var1x.getName().getString();
            if (!Night.friendManager.isFriend(var3x)) {
               double var4x = mc.player.distanceToSqr(var1x);
               double var6x = Mth.square(this.maceRange.getValue().floatValue());
               if (var4x <= var6x) {
                  double var8 = var1x.fallDistance;
                  double var10 = 1.5;
                  if (var8 >= var10 && var1x.getMainHandItem().getItem().equals(Items.MACE)) {
                     return true;
                  }
               }
            }
         }

         return false;
      })) {
         var11 = true;
      } else {
         var11 = false;
      }

      return var11;
   }

   private boolean G() {
      if (mc.level != null && mc.player != null) {
         FakePlayer var1;
         if (Night.moduleManager != null) {
            var1 = Night.moduleManager.getModule(FakePlayer.class);
         } else {
            var1 = null;
         }

         FakePlayer var2 = var1;
         return mc.level.players().stream().anyMatch(var1x -> {
            LocalPlayer var2x = mc.player;
            if (var1x != var2x && !var1x.isDeadOrDying() && !EntityUtil.c_bool_Entity(var1x)) {
               if (var2 != null && var2.isEnabled()) {
                  RemotePlayer var3 = var2.z();
                  if (var1x == var3) {
                     return false;
                  }
               }

               String var4 = var1x.getName().getString();
               if (!Night.friendManager.isFriend(var4)) {
                  return true;
               }
            }

            return false;
         });
      } else {
         return false;
      }
   }

   @Override
   public void onDisable() {
      if (this.p && mc.player != null && this.q != -1) {
         Inventory var1 = mc.player.getInventory();
         int var2 = this.q;
         var1.setSelectedSlot(var2);
         ((ClientPlayerInteractionManagerAccessor)mc.gameMode).invokeSyncSelectedSlot();
      }

      if (this.r && mc.player != null && this.s != -1 && this.t != -1) {
         int var3 = this.s;
         int var4 = this.t;
         InventoryUtil.c("Swap", var3, var4);
      }

      this.p = false;
      this.q = -1;
      this.r = false;
      this.s = -1;
      this.t = -1;
      long var5 = 0L;
      this.o = var5;
   }

   @Override
   public String getDisplayInfo() {
      int var1 = this.slot.getFirstValue();
      int var2 = this.slot.getSecondValue();
      String var3;
      if (var1 == var2) {
         var3 = String.valueOf(this.slot.getFirstValue());
      } else {
         var3 = this.slot.getRangeString();
      }

      return "Slot " + var3;
   }
}
