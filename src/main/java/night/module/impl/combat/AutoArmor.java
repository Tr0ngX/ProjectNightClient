package night.module.impl.combat;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlot.Type;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ExperienceBottleItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.equipment.Equippable;
import night.Night;
import night.events.EventTarget;
import night.events.impl.KeyInputEvent;
import night.events.impl.UpdateWalkingPlayerEvent;
import night.module.Module;
import night.module.ModuleInfo;
import night.module.impl.player.KeyAction;
import night.setting.impl.BooleanSetting;
import night.setting.impl.ModeSetting;
import night.setting.impl.NumberSetting;
import night.setting.impl.SubBindSetting;
import night.util.inventory.InventoryUtil;
import night.util.world.EntityUtil;

@ModuleInfo(name = "AutoArmor", description = "Automatically equips the best armor.", category = Module.Category.COMBAT, proxySupported = true)
public class AutoArmor extends Module {
   public ModeSetting health;
   public BooleanSetting elytraPriority;
   public BooleanSetting preserve;
   public BooleanSetting unEquip;
   public NumberSetting preserveHealth;
   public NumberSetting safeRange;
   public BooleanSetting elytra;
   public SubBindSetting elytra2;
   public BooleanSetting smartElytra;
   public ModeSetting headMode;
   public ModeSetting chestMode;
   public ModeSetting legsMode;
   public ModeSetting feetMode;
   private int r;

   public AutoArmor() {
      String[] var1 = new String[]{"Highest", "Lowest", "Any"};
      this.health = new ModeSetting("Health", "The health priority to apply.", "Highest", var1);
      this.elytraPriority = new BooleanSetting("ElytraPriority", "Prioritizes elytra over armor pieces.", true);
      this.preserve = new BooleanSetting("Preserve", "Preserve low health armor to avoid it from breaking.", false);
      BooleanSetting var3 = this.preserve;
      BooleanSetting.BooleanCondition var2 = new BooleanSetting.BooleanCondition(var3, true);
      this.unEquip = new BooleanSetting("UnEquip", "Unequip low health armor into inventory when no replacement armor is available.", var2, false);
      BooleanSetting var5 = this.preserve;
      BooleanSetting.BooleanCondition var4 = new BooleanSetting.BooleanCondition(var5, true);
      Float var6 = 20.0F;
      Float var7 = 10.0F;
      Float var8 = 50.0F;
      this.preserveHealth = new NumberSetting("PreserveHealth", "The minimum health of armor to preserve it.", var4, var6, var7, var8);
      BooleanSetting var10 = this.preserve;
      BooleanSetting.BooleanCondition var9 = new BooleanSetting.BooleanCondition(var10, true);
      Integer var11 = 0;
      Integer var12 = 0;
      Integer var13 = 10;
      this.safeRange = new NumberSetting("SafeRange", "Range to check for nearby players before re-equipping preserved armor.", var9, var11, var12, var13);
      this.elytra = new BooleanSetting("Elytra", "Equips an elytra instead of a chestplate.", false);
      BooleanSetting var14 = this.elytra;
      this.elytra2 = new SubBindSetting("Elytra", "Equips an elytra instead of a chestplate. The bind toggles it on/off.", var14);
      this.smartElytra = new BooleanSetting("SmartElytra", "Chooses when to enable elytra in a more convenient way.", false);
      String[] var15 = new String[]{"Blast", "Prot", "Turtle"};
      this.headMode = new ModeSetting("HeadMode", "Which protection enchant Head armor is ranked by.", "Prot", var15);
      String[] var16 = new String[]{"Blast", "Prot"};
      this.chestMode = new ModeSetting("ChestMode", "Which protection enchant Chest armor is ranked by.", "Prot", var16);
      String[] var17 = new String[]{"Blast", "Prot"};
      this.legsMode = new ModeSetting("LegsMode", "Which protection enchant Legs armor is ranked by.", "Prot", var17);
      String[] var18 = new String[]{"Blast", "Prot", "Gold"};
      this.feetMode = new ModeSetting("FeetMode", "Which protection enchant Feet armor is ranked by.", "Prot", var18);
      this.r = 0;
   }

   @EventTarget
   public void a_void_KeyInputEvent(KeyInputEvent var1) {
      if (!this.isProxyDelegated()) {
         if (!this.nullCheck() && this.smartElytra.getValue()) {
            if (var1.getKey() == 32 && !this.elytra.getValue() && !mc.player.onGround() && !EntityUtil.d_bool_Entity(mc.player)) {
               this.elytra.setValue(true);
            }
         }
      }
   }

   @EventTarget
   public void a_void_UpdateWalkingPlayerEvent(UpdateWalkingPlayerEvent var1) {
      if (!this.isProxyDelegated()) {
         if (mc.player != null && mc.level != null) {
            if (this.smartElytra.getValue()
               && this.elytra.getValue()
               && (mc.player.onGround() && !(mc.player.getMainHandItem().getItem() instanceof ExperienceBottleItem) || EntityUtil.d_bool_Entity(mc.player))) {
               this.elytra.setValue(false);
            }

            if (this.r <= 0) {
               if (InventoryUtil.g()) {
                  return;
               }

               KeyAction var2 = Night.moduleManager.getModule(KeyAction.class);
               if (var2 != null && var2.v() && !this.v()) {
                  ItemStack var3 = mc.player.getInventory().getItem(39);
                  if (this.d(var3) && this.w()) {
                     MultiPlayerGameMode var4 = mc.gameMode;
                     int var5 = mc.player.inventoryMenu.containerId;
                     LocalPlayer var6 = mc.player;
                     var4.handleContainerInput(var5, 5, 0, ContainerInput.QUICK_MOVE, var6);
                     int var7 = Night.serverManager.getPingTicks();
                     int var8 = 2 + var7;
                     this.r = var8;
                  }
               } else {
                  this.a_void_EquipmentSlot_int(EquipmentSlot.HEAD, 5);
               }

               if (!this.elytra.getValue() || !this.elytraPriority.getValue() || mc.player.getInventory().getItem(38).getItem() != Items.ELYTRA) {
                  this.a_void_EquipmentSlot_int(EquipmentSlot.CHEST, 6);
               }

               this.a_void_EquipmentSlot_int(EquipmentSlot.LEGS, 7);
               this.a_void_EquipmentSlot_int(EquipmentSlot.FEET, 8);
            }

            int var9 = this.r - 1;
            this.r = var9;
         }
      }
   }

   private boolean a_bool_ItemStack(ItemStack var1) {
      String var2 = var1.getItem().toString().toLowerCase();
      boolean var3;
      if (!var2.contains("diamond") && !var2.contains("netherite")) {
         var3 = false;
      } else {
         var3 = true;
      }

      return var3;
   }

   private boolean a(ItemStack var1, String var2, EquipmentSlot var3) {
      if (!this.d(var1)) {
         return false;
      }

      if (var3 == EquipmentSlot.HEAD && this.headMode.getValue().equalsIgnoreCase("Turtle") && var1.getItem() == Items.TURTLE_HELMET) {
         return true;
      }

      if (var3 == EquipmentSlot.FEET
         && (this.feetMode.getValue().equalsIgnoreCase("Gold") || this.feetMode.getValue().equalsIgnoreCase("GoldMode"))
         && var1.getItem() == Items.GOLDEN_BOOTS) {
         return true;
      }

      boolean var4;
      if (!this.a_bool_ItemStack(var1) && this.a_int_ItemStack_String(var1, var2) <= 0 && this.b_int_ItemStack(var1) <= 0) {
         var4 = false;
      } else {
         var4 = true;
      }

      return var4;
   }

   private boolean v() {
      for (int var1 = 36; var1 <= 38; var1++) {
         Inventory var2 = mc.player.getInventory();
         int var3 = var1;
         ItemStack var4 = var2.getItem(var3);
         if (!var4.isEmpty() && var4.isDamageableItem()) {
            int var5 = var1;
            if (this.b_float_int(var5) <= 90.0F) {
               return false;
            }
         }
      }

      return true;
   }

   private boolean w() {
      for (int var1 = 0; var1 < 36; var1++) {
         Inventory var2 = mc.player.getInventory();
         int var3 = var1;
         if (var2.getItem(var3).isEmpty()) {
            return true;
         }
      }

      return false;
   }

   private void a_void_EquipmentSlot_int(EquipmentSlot var1, int var2) {
      int var3 = this.y();
      boolean var4;
      if (this.elytra.getValue() && var1 == EquipmentSlot.CHEST) {
         var4 = true;
      } else {
         var4 = false;
      }

      boolean var5 = var4;
      byte var6;
      if (var1 == EquipmentSlot.HEAD) {
         var6 = 39;
      } else if (var1 == EquipmentSlot.CHEST) {
         var6 = 38;
      } else if (var1 == EquipmentSlot.LEGS) {
         var6 = 37;
      } else {
         var6 = 36;
      }

      byte var7 = var6;
      int var8;
      if (var5) {
         var8 = var3;
      } else {
         var8 = this.a_int_EquipmentSlot(var1);
      }

      int var9 = var8;
      int var11;
      if (var5) {
         int var10 = var9;
         var11 = this.a_int_int_int(38, var10);
      } else {
         byte var12 = var7;
         int var13 = var9;
         var11 = this.a(var12, var13, true, var1);
      }

      int var14 = var11;
      if (var9 != -1) {
         int var15 = var14;
         byte var16 = var7;
         if (var15 != var16) {
            MultiPlayerGameMode var17 = mc.gameMode;
            int var18 = mc.player.inventoryMenu.containerId;
            LocalPlayer var19 = mc.player;
            var17.handleContainerInput(var18, var2, 0, ContainerInput.PICKUP, var19);
            MultiPlayerGameMode var20 = mc.gameMode;
            int var21 = mc.player.inventoryMenu.containerId;
            int var22 = InventoryUtil.e(var9);
            LocalPlayer var23 = mc.player;
            var20.handleContainerInput(var21, var22, 0, ContainerInput.PICKUP, var23);
            MultiPlayerGameMode var24 = mc.gameMode;
            int var25 = mc.player.inventoryMenu.containerId;
            LocalPlayer var26 = mc.player;
            var24.handleContainerInput(var25, var2, 0, ContainerInput.PICKUP, var26);
            int var27 = Night.serverManager.getPingTicks();
            int var28 = 2 + var27;
            this.r = var28;
            return;
         }
      }

      if (var9 == -1 && !var5 && this.preserve.getValue() && this.unEquip.getValue() && !this.x()) {
         Inventory var29 = mc.player.getInventory();
         byte var30 = var7;
         ItemStack var31 = var29.getItem(var30);
         if (!var31.isEmpty()) {
            ItemStack var32 = var31;
            if (this.d(var32)) {
               byte var33 = var7;
               float var34 = this.b_float_int(var33);
               float var35 = this.preserveHealth.getValue().floatValue();
               if (var34 < var35 && this.w()) {
                  MultiPlayerGameMode var36 = mc.gameMode;
                  int var37 = mc.player.inventoryMenu.containerId;
                  LocalPlayer var38 = mc.player;
                  var36.handleContainerInput(var37, var2, 0, ContainerInput.QUICK_MOVE, var38);
                  int var39 = Night.serverManager.getPingTicks();
                  int var40 = 2 + var39;
                  this.r = var40;
               }
            }
         }
      }
   }

   private int a(int var1, int var2, boolean var3, EquipmentSlot var4) {
      if (var2 == -1) {
         return var1;
      }

      if (var1 == -1) {
         return var2;
      }

      ItemStack var5 = mc.player.getInventory().getItem(var1);
      ItemStack var6 = mc.player.getInventory().getItem(var2);
      ItemStack var7 = var5;
      boolean var8 = this.d(var7);
      ItemStack var9 = var6;
      boolean var10 = this.d(var9);
      if (!var8 && !var10) {
         return var1;
      }

      if (!var8) {
         return var2;
      }

      if (!var10) {
         return var1;
      }

      if (var4 == EquipmentSlot.HEAD && this.headMode.getValue().equalsIgnoreCase("Turtle")) {
         boolean var11;
         if (var5.getItem() == Items.TURTLE_HELMET) {
            var11 = true;
         } else {
            var11 = false;
         }

         boolean var12 = var11;
         boolean var13;
         if (var6.getItem() == Items.TURTLE_HELMET) {
            var13 = true;
         } else {
            var13 = false;
         }

         boolean var14 = var13;
         boolean var15 = var12;
         boolean var16 = var14;
         if (var15 != var16) {
            int var17;
            if (var12) {
               var17 = var1;
            } else {
               var17 = var2;
            }

            return var17;
         }
      }

      if (var4 == EquipmentSlot.FEET && (this.feetMode.getValue().equalsIgnoreCase("Gold") || this.feetMode.getValue().equalsIgnoreCase("GoldMode"))) {
         boolean var18;
         if (var5.getItem() == Items.GOLDEN_BOOTS) {
            var18 = true;
         } else {
            var18 = false;
         }

         boolean var19 = var18;
         boolean var20;
         if (var6.getItem() == Items.GOLDEN_BOOTS) {
            var20 = true;
         } else {
            var20 = false;
         }

         boolean var21 = var20;
         boolean var22 = var19;
         boolean var23 = var21;
         if (var22 != var23) {
            int var24;
            if (var19) {
               var24 = var1;
            } else {
               var24 = var2;
            }

            return var24;
         }
      }
      String var26;
      if ((switch (var4) {
         case HEAD -> this.headMode.getValue();
         case CHEST -> this.chestMode.getValue();
         case LEGS -> this.legsMode.getValue();
         default -> this.feetMode.getValue();
      }).equalsIgnoreCase("Blast")) {
         var26 = "blast_protection";
      } else {
         var26 = "protection";
      }

      String var27 = var26;
      ItemStack var28 = var5;
      String var29 = var27;
      int var30 = this.a_int_ItemStack_String(var28, var29);
      ItemStack var31 = var6;
      String var32 = var27;
      int var33 = this.a_int_ItemStack_String(var31, var32);
      int var34 = var30;
      int var35 = var33;
      if (var34 != var35) {
         int var36 = var30;
         int var37 = var33;
         int var38;
         if (var36 > var37) {
            var38 = var1;
         } else {
            var38 = var2;
         }

         return var38;
      } else {
         if (var3 && this.preserve.getValue()) {
            ItemStack var39 = var5;
            if (this.a_int_ItemStack_String(var39, "mending") > 0 && var5.getDamageValue() > 0 && this.x()) {
               return var1;
            }
         }

         if (var30 == 0 && var33 == 0) {
            ItemStack var40 = var5;
            int var41 = this.b_int_ItemStack(var40);
            ItemStack var42 = var6;
            int var43 = this.b_int_ItemStack(var42);
            int var44 = var41;
            int var45 = var43;
            if (var44 != var45) {
               int var46 = var41;
               int var47 = var43;
               int var48;
               if (var46 > var47) {
                  var48 = var1;
               } else {
                  var48 = var2;
               }

               return var48;
            }

            int var49 = var5.getEnchantments().size();
            int var50 = var6.getEnchantments().size();
            int var51 = var49;
            int var52 = var50;
            if (var51 != var52) {
               int var53 = var49;
               int var54 = var50;
               int var55;
               if (var53 > var54) {
                  var55 = var1;
               } else {
                  var55 = var2;
               }

               return var55;
            }
         }

         if (this.preserve.getValue()) {
            if (this.x()) {
               float var56 = this.b_float_int(var2);
               float var57 = this.preserveHealth.getValue().floatValue();
               if (var56 < var57) {
                  float var58 = this.b_float_int(var1);
                  float var59 = this.preserveHealth.getValue().floatValue();
                  if (var58 >= var59) {
                     ItemStack var60 = var6;
                     String var61 = var27;
                     if (this.a(var60, var61, var4)) {
                        return var2;
                     }
                  }
               }
            } else {
               float var62 = this.b_float_int(var1);
               float var63 = this.b_float_int(var2);
               float var64 = this.preserveHealth.getValue().floatValue();
               float var65 = var62;
               float var66 = var64;
               if (var65 < var66) {
                  float var67 = var63;
                  float var68 = var64;
                  if (var67 >= var68) {
                     return var2;
                  }
               }

               float var69 = var63;
               float var70 = var64;
               if (var69 < var70) {
                  float var71 = var62;
                  float var72 = var64;
                  if (var71 >= var72) {
                     return var1;
                  }
               }
            }
         }

         float var73 = this.b_float_int(var1);
         float var74 = this.b_float_int(var2);
         if (this.health.getValue().equals("Highest")) {
            float var75 = var73;
            float var76 = var74;
            if (var75 != var76) {
               float var77 = var73;
               float var78 = var74;
               int var79;
               if (var77 > var78) {
                  var79 = var1;
               } else {
                  var79 = var2;
               }

               return var79;
            }
         } else if (this.health.getValue().equals("Lowest")) {
            float var80 = var73;
            float var81 = var74;
            if (var80 != var81) {
               float var82 = var73;
               float var83 = var74;
               int var84;
               if (var82 < var83) {
                  var84 = var1;
               } else {
                  var84 = var2;
               }

               return var84;
            }
         }

         return var1;
      }
   }

   private boolean x() {
      double var1 = this.safeRange.getValue().doubleValue();
      double var3 = 0.0;
      if (var1 <= var3) {
         return false;
      }

      if (mc.level != null && mc.player != null) {
         double var5 = this.safeRange.getValue().doubleValue();
         double var7 = this.safeRange.getValue().doubleValue();
         double var9 = var5 * var7;

         for (Player var12 : (Iterable<Player>)(Object)mc.level.players()) {
            LocalPlayer var13 = mc.player;
            if (var12 != var13 && !var12.isSpectator() && var12.isAlive()) {
               if (Night.friendManager != null) {
                  String var14 = var12.getName().getString();
                  if (Night.friendManager.isFriend(var14)) {
                     continue;
                  }
               }

               LocalPlayer var15 = mc.player;
               Player var16 = var12;
               double var17 = var15.distanceToSqr(var16);
               double var19 = var9;
               if (var17 <= var19) {
                  return false;
               }
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private int a_int_int_int(int var1, int var2) {
      if (var2 == -1) {
         return var1;
      }

      if (mc.player.getInventory().getItem(var1).getItem() != Items.ELYTRA) {
         return var2;
      }

      if (this.health.getValue().equals("Highest")) {
         float var3 = this.b_float_int(var1);
         float var4 = this.b_float_int(var2);
         if (var3 < var4) {
            return var2;
         }
      }

      if (this.health.getValue().equals("Lowest")) {
         float var5 = this.b_float_int(var1);
         float var6 = this.b_float_int(var2);
         if (var5 > var6) {
            return var2;
         }
      }

      return var1;
   }

   private int a_int_EquipmentSlot(EquipmentSlot var1) {
      String var3;
      if ((switch (var1) {
         case HEAD -> this.headMode.getValue();
         case CHEST -> this.chestMode.getValue();
         case LEGS -> this.legsMode.getValue();
         default -> this.feetMode.getValue();
      }).equalsIgnoreCase("Blast")) {
         var3 = "blast_protection";
      } else {
         var3 = "protection";
      }

      String var4 = var3;
      byte var5;
      if (var1 == EquipmentSlot.HEAD) {
         var5 = 39;
      } else if (var1 == EquipmentSlot.CHEST) {
         var5 = 38;
      } else if (var1 == EquipmentSlot.LEGS) {
         var5 = 37;
      } else {
         var5 = 36;
      }

      byte var6 = var5;
      Inventory var7 = mc.player.getInventory();
      byte var8 = var6;
      ItemStack var9 = var7.getItem(var8);
      boolean var10 = this.d(var9);
      int var11 = -1;

      for (int var12 = InventoryUtil.c; var12 <= InventoryUtil.f; var12++) {
         Inventory var13 = mc.player.getInventory();
         int var14 = var12;
         ItemStack var15 = var13.getItem(var14);
         if (this.d(var15)) {
            ItemStack var16 = var15;
            if (this.c(var16).equals(var1)) {
               if (this.preserve.getValue() && !this.x()) {
                  int var17 = var12;
                  float var18 = this.b_float_int(var17);
                  float var19 = this.preserveHealth.getValue().floatValue();
                  if (var18 < var19) {
                     continue;
                  }

                  if (var10) {
                     ItemStack var20 = var15;
                     String var21 = var4;
                     if (!this.a(var20, var21, var1)) {
                        continue;
                     }
                  }
               }

               if (var11 == -1) {
                  var11 = var12;
               } else {
                  int var22 = var12;
                  int var23 = var11;
                  var11 = this.a(var22, var23, false, var1);
               }
            }
         }
      }

      return var11;
   }

   private int y() {
      int var1 = -1;

      for (int var2 = InventoryUtil.c; var2 <= InventoryUtil.f; var2++) {
         Inventory var3 = mc.player.getInventory();
         int var4 = var2;
         if (var3.getItem(var4).getItem() == Items.ELYTRA) {
            int var5 = var2;
            int var6 = var1;
            var1 = this.a_int_int_int(var5, var6);
         }
      }

      return var1;
   }

   private int a_int_ItemStack_String(ItemStack var1, String var2) {
      for (Holder var4 : var1.getEnchantments().keySet()) {
         if (var4.getRegisteredName().replace("minecraft:", "").equals(var2)) {
            ItemEnchantments var5 = var1.getEnchantments();
            Holder var6 = var4;
            return var5.getLevel(var6);
         }
      }

      return 0;
   }

   private int b_int_ItemStack(ItemStack var1) {
      int var2 = 0;

      for (Holder var4 : var1.getEnchantments().keySet()) {
         String var5 = var4.getRegisteredName().replace("minecraft:", "");
         if (var5.equals("protection") || var5.equals("blast_protection") || var5.equals("fire_protection") || var5.equals("projectile_protection")) {
            ItemEnchantments var6 = var1.getEnchantments();
            Holder var7 = var4;
            int var8 = var6.getLevel(var7);
            int var9 = var2;
            if (var8 > var9) {
               var2 = var8;
            }
         }
      }

      return var2;
   }

   private float b_float_int(int var1) {
      ItemStack var2 = mc.player.getInventory().getItem(var1);
      int var3 = var2.getMaxDamage();
      int var4 = var2.getDamageValue();
      float var5 = (var3 - var4) * 100.0F;
      float var6 = var2.getMaxDamage();
      return var5 / var6;
   }

   private EquipmentSlot c(ItemStack var1) {
      return var1.has(DataComponents.GLIDER) ? EquipmentSlot.CHEST : ((Equippable)var1.get(DataComponents.EQUIPPABLE)).slot();
   }

   private boolean d(ItemStack var1) {
      if (var1.getItem() == Items.ELYTRA) {
         return false;
      }

      if (!((ItemAttributeModifiers)var1.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY))
         .modifiers()
         .stream()
         .anyMatch(var0 -> var0.attribute().is(Attributes.ARMOR))) {
         return false;
      }

      Equippable var2 = (Equippable)var1.get(DataComponents.EQUIPPABLE);
      boolean var3;
      if (var2 != null && var2.slot().getType() == Type.HUMANOID_ARMOR) {
         var3 = true;
      } else {
         var3 = false;
      }

      return var3;
   }
}
