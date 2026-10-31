package night.module.impl.combat;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import night.Night;
import night.events.EventTarget;
import night.events.impl.TickEvent;
import night.events.impl.TotemPopEvent;
import night.events.impl.UpdateWalkingPlayerEvent;
import night.module.Module;
import night.module.ModuleInfo;
import night.module.impl.player.MultiTask;
import night.module.impl.player.SpeedMine;
import night.pingbypass.PingBypassState;
import night.security.LicenseService;
import night.setting.impl.BooleanSetting;
import night.setting.impl.ModeSetting;
import night.setting.impl.NumberSetting;
import night.util.inventory.InventoryUtil;
import night.util.world.PositionUtil;

@ModuleInfo(name = "AutoTotem", description = "Automatically puts a specified item in your offhand slot.", category = Module.Category.COMBAT)
public class AutoTotem extends Module {
   public ModeSetting item;
   public NumberSetting health;
   public BooleanSetting elytraCheck;
   public NumberSetting fallDistance;
   public BooleanSetting useGapple;
   public BooleanSetting sword;
   public BooleanSetting pickaxe;
   public BooleanSetting totem;
   public BooleanSetting lethalOverride;
   public BooleanSetting noTotemGap;
   public BooleanSetting s117;
   public BooleanSetting smartMine;
   public BooleanSetting antiMace;
   public NumberSetting maceRange;
   public BooleanSetting debug;
   private int t;
   private int u;
   private boolean v;
   private String w;
   private ItemStack x;
   private ItemStack y;
   private float z;
   private volatile long A;

   public AutoTotem() {
      String[] var1 = new String[]{"Totem", "Crystal", "Gapple"};
      this.item = new ModeSetting("Item", "The item that will be placed in your offhand slot when safety conditions are met.", "Totem", var1);
      ModeSetting var3 = this.item;
      String[] var4 = new String[]{"Crystal", "Gapple"};
      ModeSetting.ModeCondition var2 = new ModeSetting.ModeCondition(var3, var4);
      Integer var5 = 16;
      Integer var6 = 0;
      Integer var7 = 36;
      this.health = new NumberSetting("Health", "The health at which a totem will be prioritized.", var2, var5, var6, var7);
      this.elytraCheck = new BooleanSetting("ElytraCheck", "Prioritizes a totem whenever you're wearing an elytra.", true);
      Float var8 = 20.0F;
      Float var9 = 0.0F;
      Float var10 = 80.0F;
      this.fallDistance = new NumberSetting("FallDistance", "The fall distance at which the module will prioritize a totem.", var8, var9, var10);
      this.useGapple = new BooleanSetting("UseGapple", "Switches to a golden apple in your offhand based on main hand items when health is safe.", true);
      BooleanSetting var11 = this.useGapple;
      this.sword = new BooleanSetting(
         "Sword", "Switches to a gapple when holding a sword or axe in main hand.", new BooleanSetting.BooleanCondition(var11, true), true
      );
      BooleanSetting var12 = this.useGapple;
      this.pickaxe = new BooleanSetting(
         "Pickaxe", "Switches to a gapple when holding a pickaxe in main hand.", new BooleanSetting.BooleanCondition(var12, true), true
      );
      BooleanSetting var13 = this.useGapple;
      this.totem = new BooleanSetting(
         "Totem", "Switches to a gapple when holding a totem in main hand.", new BooleanSetting.BooleanCondition(var13, true), false
      );
      BooleanSetting var14 = this.useGapple;
      this.lethalOverride = new BooleanSetting(
         "LethalOverride", "Overrides any necessity for a totem when gappling.", new BooleanSetting.BooleanCondition(var14, true), false
      );
      this.noTotemGap = new BooleanSetting("NoTotemGap", "Swap your gap into offhand when no totems left", false);
      this.s117 = new BooleanSetting("1.17+", "1.17+ uses the SWAP inventory action, while having it off uses inventory clicks.", true);
      ModeSetting var15 = this.item;
      String[] var16 = new String[]{"Crystal"};
      this.smartMine = new BooleanSetting(
         "SmartMine", "Switches to a crystal whenever you start mining and a totem when you aren't mining.", new ModeSetting.ModeCondition(var15, var16), false
      );
      this.antiMace = new BooleanSetting("AntiMace", "Switches to a totem if a player near you is trying to smash attack you with a mace.", false);
      BooleanSetting var18 = this.antiMace;
      BooleanSetting.BooleanCondition var17 = new BooleanSetting.BooleanCondition(var18, true);
      Float var19 = 12.0F;
      Float var20 = 0.0F;
      Float var21 = 24.0F;
      this.maceRange = new NumberSetting(
         "MaceRange", "The distance at which an enemy has to be in with a mace in order to swap to a totem.", var17, var19, var20, var21
      );
      this.debug = new BooleanSetting("Debug", "Logs why you died and why the totem swap failed at that moment.", false);
      this.t = 0;
      this.u = -1;
      this.v = true;
      this.w = "";
      this.x = ItemStack.EMPTY;
      this.y = ItemStack.EMPTY;
      this.z = 0.0F;
      long var22 = 0L;
      this.A = var22;
   }

   @Override
   public void onDisable() {
      this.u = -1;
   }

   @EventTarget(priority = Integer.MAX_VALUE)
   public void a_void_TotemPopEvent(TotemPopEvent var1) {
      if (!PingBypassState.a()) {
         Player var2 = var1.getPlayer();
         LocalPlayer var3 = mc.player;
         if (var2 == var3 && !Night.moduleManager.getModule(Suicide.class).isEnabled()) {
            long var4 = System.currentTimeMillis();
            this.A = var4;
            if (this.debug.getValue()) {
               float var6 = mc.player.getHealth();
               float var7 = mc.player.getAbsorptionAmount();
               float var8 = var6 + var7;
               int var9 = this.t;
               Item var10 = mc.player.getOffhandItem().getItem();
               String var11 = this.b_String_Item(var10);
               this.c("TOTEM POPPED (survived). health+absorb=" + var8 + ", totemsLeft=" + var9 + ", offhandNow=" + var11);
            }

            this.v();
         }
      }
   }

   @EventTarget(priority = Integer.MAX_VALUE)
   public void a_void_UpdateWalkingPlayerEvent(UpdateWalkingPlayerEvent var1) {
      this.v();
   }

   private void v() {
      if (mc.player != null && mc.level != null) {
         if (PingBypassState.a()) {
            this.b_void_String("skip: PingBypass active");
         } else {
            Screen var1 = mc.gui.screen();
            boolean var2 = var1 instanceof InventoryScreen;
            boolean var3 = var1 instanceof AbstractContainerScreen;
            if (InventoryUtil.a()) {
               this.b_void_String("skip: AutoShop is using the open shop window");
            } else if (var3 && !mc.player.containerMenu.getCarried().isEmpty()) {
               this.b_void_String("skip: cursor is holding an item in an open container");
            } else {
               Item var4 = this.x();
               if (var4 == null) {
                  this.b_void_String("skip: getItem() returned null (no valid target item)");
               } else {
                  if (!mc.player.containerMenu.getCarried().isEmpty()) {
                     Item var5 = mc.player.containerMenu.getCarried().getItem();
                     Item var6 = var4;
                     if (var5 == var6) {
                        InventoryUtil.a_void_int_int_ContainerInput(45, 0, ContainerInput.PICKUP);
                        if (!var2 && !mc.player.containerMenu.getCarried().isEmpty()) {
                           int var7 = InventoryUtil.d(InventoryUtil.c, InventoryUtil.f);
                           if (var7 != -1) {
                              InventoryUtil.a_void_int_int_ContainerInput(InventoryUtil.e(var7), 0, ContainerInput.PICKUP);
                           }
                        }

                        if (this.debug.getValue()) {
                           Item var8 = var4;
                           this.c("RECOVERED stuck cursor -> placed " + this.b_String_Item(var8) + " into offhand");
                        }

                        return;
                     }

                     if (!var2) {
                        int var9 = InventoryUtil.d(InventoryUtil.c, InventoryUtil.f);
                        if (var9 != -1) {
                           InventoryUtil.a_void_int_int_ContainerInput(InventoryUtil.e(var9), 0, ContainerInput.PICKUP);
                        }

                        if (!mc.player.containerMenu.getCarried().isEmpty()) {
                           this.b_void_String("skip: cursor is holding an item and could not clear it");
                           return;
                        }
                     }
                  }

                  int var10;
                  if (var4 == Items.TOTEM_OF_UNDYING
                     && Night.moduleManager.getModule(Suicide.class).isEnabled()
                     && Night.moduleManager.getModule(Suicide.class).offhandOverride.getValue()) {
                     if (mc.player.getOffhandItem().isEmpty()) {
                        this.b_void_String("skip: Suicide offhandOverride, offhand already empty");
                        return;
                     }

                     var10 = InventoryUtil.d(InventoryUtil.c, InventoryUtil.f);
                     if (var10 == -1) {
                        this.b_void_String("Suicide override: no empty slot to move offhand item into");
                     }
                  } else {
                     Item var11 = mc.player.getOffhandItem().getItem();
                     Item var12 = var4;
                     if (var11 == var12) {
                        Item var13 = var4;
                        this.b_void_String("ok: offhand already holds target (" + this.b_String_Item(var13) + ")");
                        return;
                     }

                     if (this.u != -1 && var4 == Items.TOTEM_OF_UNDYING) {
                        Inventory var14 = mc.player.getInventory();
                        int var15 = this.u;
                        if (var14.getItem(var15).getItem() == Items.TOTEM_OF_UNDYING) {
                           var10 = this.u;
                        } else {
                           var10 = InventoryUtil.c_int_Item(var4);
                           if (var10 == -1) {
                              var10 = InventoryUtil.a_int_Item(var4);
                           }
                        }

                        this.u = -1;
                     } else if (var4 != Items.ENCHANTED_GOLDEN_APPLE && var4 != Items.GOLDEN_APPLE) {
                        var10 = InventoryUtil.c_int_Item(var4);
                        if (var10 == -1) {
                           var10 = InventoryUtil.a_int_Item(var4);
                        }
                     } else {
                        var10 = InventoryUtil.b_int_Item(var4);
                        if (var10 == -1) {
                           var10 = InventoryUtil.c_int_Item(var4);
                        }

                        if (var10 != -1) {
                           int var16 = var10;
                           this.u = var16;
                        }
                     }

                     if (var10 == -1) {
                        if (var4 != Items.TOTEM_OF_UNDYING || !this.a_bool_Item(Items.TOTEM_OF_UNDYING)) {
                           Item var17 = var4;
                           this.b_void_String("FAIL: target " + this.b_String_Item(var17) + " NOT FOUND in inventory (none left)");
                           return;
                        }

                        var10 = InventoryUtil.d(InventoryUtil.c, InventoryUtil.f);
                        if (var10 == -1) {
                           this.b_void_String("FAIL: have a totem but no empty slot to stage the swap");
                        }
                     }
                  }

                  if (var10 == -1) {
                     this.b_void_String("FAIL: no usable slot resolved (see previous reason)");
                  } else {
                     MultiTask var18 = Night.moduleManager.getModule(MultiTask.class);
                     boolean var19;
                     if (var18 != null && var18.isEnabled() && var18.autoTotem.getValue()) {
                        var19 = true;
                     } else {
                        var19 = false;
                     }

                     if (!var19 && mc.player.isUsingItem()) {
                        MultiPlayerGameMode var20 = mc.gameMode;
                        LocalPlayer var21 = mc.player;
                        var20.releaseUsingItem(var21);
                     }

                     ItemStack var22 = mc.player.getOffhandItem();
                     if (this.s117.getValue()) {
                        int var23 = var10;
                        InventoryUtil.c("Swap", var23, 40);
                     } else {
                        int var24 = var10;
                        InventoryUtil.c("Pickup", var24, 45);
                        if (!var2 && !mc.player.containerMenu.getCarried().isEmpty()) {
                           int var25 = InventoryUtil.d(InventoryUtil.c, InventoryUtil.f);
                           if (var25 != -1) {
                              InventoryUtil.a_void_int_int_ContainerInput(InventoryUtil.e(var25), 0, ContainerInput.PICKUP);
                           }
                        }
                     }

                     ItemStack var26 = mc.player.getOffhandItem();
                     ItemStack var27 = var22;
                     if (var26 != var27) {
                        InventoryUtil.f();
                     }

                     if (this.debug.getValue()) {
                        Item var28 = var4;
                        String var29 = this.b_String_Item(var28);
                        int var30 = var10;
                        String var31;
                        if (this.s117.getValue()) {
                           var31 = "1.17+ (Swap)";
                        } else {
                           var31 = "Clicks (Pickup)";
                        }

                        this.c("SWAP -> offhand: " + var29 + " (from slot " + var30 + ") [mode=" + var31 + "]");
                     }

                     this.w = "";
                  }
               }
            }
         }
      }
   }

   @EventTarget
   public void a_void_TickEvent(TickEvent var1) {
      if (!PingBypassState.a()) {
         if (mc.player != null && mc.level != null) {
            boolean var2;
            if (mc.player.getHealth() > 0.0F && !mc.player.isDeadOrDying()) {
               var2 = true;
            } else {
               var2 = false;
            }

            boolean var3 = var2;
            if (this.debug.getValue() && this.v && !var3) {
               this.w();
            }

            boolean var4 = var3;
            this.v = var4;
            if (var3) {
               int var5 = mc.player.getInventory().countItem(Items.TOTEM_OF_UNDYING);
               int var6;
               if (!mc.player.containerMenu.getCarried().isEmpty() && mc.player.containerMenu.getCarried().getItem() == Items.TOTEM_OF_UNDYING) {
                  var6 = mc.player.containerMenu.getCarried().getCount();
               } else {
                  var6 = 0;
               }

               int var7 = var5 + var6;
               this.t = var7;
               ItemStack var8 = mc.player.getOffhandItem().copy();
               this.x = var8;
               ItemStack var9 = mc.player.getMainHandItem().copy();
               this.y = var9;
               float var10 = mc.player.getHealth();
               float var11 = mc.player.getAbsorptionAmount();
               float var12 = var10 + var11;
               this.z = var12;
            }
         }
      }
   }

   private void w() {
      String var1;
      try {
         var1 = mc.player.getCombatTracker().getDeathMessage().getString();
      } catch (Throwable var26) {
         var1 = "(unknown)";
      }

      ItemStack var3 = this.x;
      boolean var4;
      if (var3.getItem() == Items.TOTEM_OF_UNDYING) {
         var4 = true;
      } else {
         var4 = false;
      }

      boolean var5 = var4;
      this.c("================ DEATH ================");
      this.c("Cause: " + var1);
      String var6;
      if (var3.isEmpty()) {
         var6 = "EMPTY";
      } else {
         Item var7 = var3.getItem();
         String var8 = this.b_String_Item(var7);
         int var9 = var3.getCount();
         var6 = var8 + " x" + var9;
      }

      String var10;
      if (var5) {
         var10 = " (totem WAS present!)";
      } else {
         var10 = "";
      }

      this.c("Offhand last tick: " + var6 + var10);
      String var11;
      if (this.y.isEmpty()) {
         var11 = "EMPTY";
      } else {
         Item var12 = this.y.getItem();
         var11 = this.b_String_Item(var12);
      }

      this.c("Mainhand last tick: " + var11);
      this.c("Totems last tick: " + this.t);
      this.c("Health+Absorb last tick: " + this.z);
      long var13 = this.A;
      long var15 = 0L;
      String var17;
      if (var13 == var15) {
         var17 = "(none)";
      } else {
         long var18 = System.currentTimeMillis();
         long var20 = this.A;
         var17 = String.valueOf(var18 - var20);
      }

      this.c("ms since last pop: " + var17);
      this.c("needsTotem()=" + this.y());
      String var22 = this.item.getValue();
      Item var23 = this.x();
      String var24 = this.b_String_Item(var23);
      this.c("Item mode=" + var22 + ", target now=" + var24);
      String var25;
      if (this.w.isEmpty()) {
         var25 = "(none)";
      } else {
         var25 = this.w;
      }

      this.c("Last swap reason: " + var25);
      if (var5) {
         this.c("=> A totem WAS in offhand but you still died (one-shot exceeding totem heal, or totem pop not registered before death).");
      } else if (this.t == 0) {
         this.c("=> No totems left in inventory -- nothing to place.");
      } else {
         this.c("=> Totems available but offhand didn't have one -- see 'Last swap reason' above for why the swap didn't happen.");
      }

      this.c("=======================================");
   }

   private Item x() {
      double var1 = PositionUtil.a_double_int(4);
      double var3 = 1.0;
      double var5 = Math.abs(var1 - var3);
      double var7 = 0.05;
      if (var5 > var7) {
         return mc.player.getOffhandItem().getItem();
      }

      boolean var9 = this.a_bool_Item(Items.TOTEM_OF_UNDYING);
      Item var10;
      if (this.a_bool_Item(Items.ENCHANTED_GOLDEN_APPLE)) {
         var10 = Items.ENCHANTED_GOLDEN_APPLE;
      } else if (this.a_bool_Item(Items.GOLDEN_APPLE)) {
         var10 = Items.GOLDEN_APPLE;
      } else {
         var10 = null;
      }

      Item var11 = var10;
      boolean var12;
      if (var11 != null) {
         var12 = true;
      } else {
         var12 = false;
      }

      boolean var13 = var12;
      boolean var14;
      if (!KillAura.b_bool_ItemStack(mc.player.getMainHandItem()) && !KillAura.c_bool_ItemStack(mc.player.getMainHandItem())) {
         var14 = false;
      } else {
         var14 = true;
      }

      boolean var15 = var14;
      boolean var16 = mc.player.getMainHandItem().is(ItemTags.PICKAXES);
      boolean var17 = mc.player.getMainHandItem().is(Items.TOTEM_OF_UNDYING);
      boolean var18;
      if ((!this.sword.getValue() || !var15) && (!this.pickaxe.getValue() || !var16) && (!this.totem.getValue() || !var17)) {
         var18 = false;
      } else {
         var18 = true;
      }

      boolean var19 = var18;
      if (!this.useGapple.getValue() || !mc.options.keyUse.isDown() || !var19 || !var13 || !this.lethalOverride.getValue() && this.y() && var9) {
         if (this.y()) {
            if (var9) {
               return Items.TOTEM_OF_UNDYING;
            }

            if (this.noTotemGap.getValue() && var13) {
               return var11;
            }
         }

         if (var9 && this.item.getValue().equalsIgnoreCase("Crystal") && this.smartMine.getValue()) {
            SpeedMine var20 = Night.moduleManager.getModule(SpeedMine.class);
            if ((var20.v() == null || !var20.v().e()) && (var20.w() == null || !var20.w().c())) {
               return Items.TOTEM_OF_UNDYING;
            }
         }

         switch (this.item.getValue()) {
            case "Crystal":
               if (this.a_bool_Item(Items.END_CRYSTAL)) {
                  return Items.END_CRYSTAL;
               }
               break;
            case "Gapple":
               if (var13) {
                  return var11;
               }
               break;
            default:
               if (var9) {
                  return Items.TOTEM_OF_UNDYING;
               }
         }

         if (var9) {
            return Items.TOTEM_OF_UNDYING;
         } else {
            return this.noTotemGap.getValue() && var13 ? var11 : mc.player.getOffhandItem().getItem();
         }
      } else {
         return var11;
      }
   }

   private boolean y() {
      if (Night.moduleManager.getModule(Suicide.class).isEnabled() && Night.moduleManager.getModule(Suicide.class).offhandOverride.getValue()) {
         return false;
      }

      float var1 = this.health.getValue().floatValue();
      float var2 = (float)PositionUtil.a_double_int(4);
      float var3 = var1 * var2;
      LicenseService var4 = Night.licenseService;
      long var5;
      if (var4 != null && var4.getRole() != null) {
         var5 = var4.getHeartbeatAgeMs();
      } else {
         var5 = Long.MAX_VALUE;
      }

      long var7 = var5;
      long var9 = 300000L;
      long var11 = var7 - var9;
      long var13 = -1L;
      int var15 = (int)((var11 ^ var13) >>> 63);
      float var16 = var3;
      float var17 = var15 * 0.8F;
      float var18 = 1.0F - var17;
      float var19 = var16 * var18;
      float var20 = mc.player.getHealth();
      float var21 = mc.player.getAbsorptionAmount();
      float var22 = var20 + var21;
      float var23 = var19;
      if (var22 <= var23) {
         return true;
      }

      double var24 = mc.player.fallDistance;
      double var26 = this.fallDistance.getValue().floatValue();
      if (var24 > var26) {
         return true;
      }

      if (this.elytraCheck.getValue() && mc.player.getItemBySlot(EquipmentSlot.CHEST).getItem() == Items.ELYTRA) {
         return true;
      }

      boolean var28;
      if (this.antiMace.getValue() && mc.level.players().stream().anyMatch(var1x -> {
         LocalPlayer var2x = mc.player;
         if (var1x != var2x) {
            String var3x = var1x.getName().getString();
            if (!Night.friendManager.isFriend(var3x)) {
               double var4x = mc.player.distanceToSqr(var1x);
               double var6 = Mth.square(this.maceRange.getValue().floatValue());
               if (var4x <= var6) {
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
         var28 = true;
      } else {
         var28 = false;
      }

      return var28;
   }

   private boolean a_bool_Item(Item var1) {
      boolean var2;
      if (InventoryUtil.a_int_Item(var1) == -1
         && mc.player.getOffhandItem().getItem() != var1
         && (mc.player.containerMenu.getCarried().isEmpty() || mc.player.containerMenu.getCarried().getItem() != var1)) {
         var2 = false;
      } else {
         var2 = true;
      }

      return var2;
   }

   private void b_void_String(String var1) {
      if (this.debug.getValue()) {
         String var2 = this.w;
         if (!var1.equals(var2)) {
            this.w = var1;
            String var3 = this.getName();
            Night.chatManager.sendTaggedMessage(var1, var3);
         }
      }
   }

   private void c(String var1) {
      String var2 = this.getName();
      Night.chatManager.sendTaggedMessage(var1, var2);
   }

   private String b_String_Item(Item var1) {
      return var1 != null && var1 != Items.AIR ? BuiltInRegistries.ITEM.getKey(var1).getPath() : "none";
   }

   @Override
   public String getDisplayInfo() {
      return String.valueOf(this.t);
   }
}
