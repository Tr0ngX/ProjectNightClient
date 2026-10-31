package night.module.impl.player;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import night.Night;
import night.events.EventTarget;
import night.events.impl.KeyInputEvent;
import night.events.impl.MouseButtonEvent;
import night.events.impl.UpdateWalkingPlayerEvent;
import night.module.Module;
import night.module.ModuleInfo;
import night.module.impl.movement.AirStuck;
import night.module.impl.movement.Sprint;
import night.pingbypass.network.RubberbandTracker;
import night.setting.impl.BooleanSetting;
import night.setting.impl.CategorySetting;
import night.setting.impl.KeybindSetting;
import night.setting.impl.ModeSetting;
import night.setting.impl.NumberSetting;
import night.util.input.KeyNameUtil;
import night.util.inventory.InventoryUtil;
import night.util.network.PacketPredictionUtil;
import night.util.world.PacketAndRenderUtil;

@ModuleInfo(
   name = "KeyAction",
   description = "Keybinds for throwing fireworks, pearls and experience bottles.",
   category = Module.Category.PLAYER,
   persistent = true,
   proxySupported = true
)
public class KeyAction extends Module {
   public CategorySetting fireWorkCategory = new CategorySetting("FireWork", "Keybind and settings for throwing a firework.");
   public KeybindSetting fireWorkBind;
   public ModeSetting fireWorkSwitch;
   public CategorySetting pearlsCategory;
   public KeybindSetting pearlsBind;
   public ModeSetting pearlsSwitch;
   public BooleanSetting pearlsRotate;
   public CategorySetting xPCategory;
   public KeybindSetting xPBind;
   public ModeSetting xPSwitch;
   public BooleanSetting xPRotate;
   public NumberSetting xPDelay;
   public NumberSetting xPRepeat;
   public ModeSetting xPAntiWaste;
   public BooleanSetting xPItemDisable;
   private boolean t;
   private int u;
   private long v;

   public KeyAction() {
      CategorySetting var1 = this.fireWorkCategory;
      this.fireWorkBind = new KeybindSetting(
         "FireWorkBind",
         "Bind",
         "The key that throws a firework. Hold/ReverseHold repeat it every tick the bind is (or isn't) held.",
         new CategorySetting.CategoryCondition(var1),
         0
      );
      CategorySetting var2 = this.fireWorkCategory;
      this.fireWorkSwitch = new ModeSetting(
         "FireWorkSwitch",
         "Switch",
         "The mode that will be used for automatically switching to fireworks.",
         new CategorySetting.CategoryCondition(var2),
         "Silent",
         InventoryUtil.a
      );
      this.pearlsCategory = new CategorySetting("Pearls", "Keybind and settings for throwing an ender pearl.");
      CategorySetting var3 = this.pearlsCategory;
      this.pearlsBind = new KeybindSetting(
         "PearlsBind",
         "Bind",
         "The key that throws an ender pearl. Hold/ReverseHold repeat it every tick the bind is (or isn't) held.",
         new CategorySetting.CategoryCondition(var3),
         0
      );
      CategorySetting var4 = this.pearlsCategory;
      this.pearlsSwitch = new ModeSetting(
         "PearlsSwitch",
         "Switch",
         "The mode that will be used for automatically switching to pearls.",
         new CategorySetting.CategoryCondition(var4),
         "Silent",
         InventoryUtil.a
      );
      CategorySetting var5 = this.pearlsCategory;
      this.pearlsRotate = new BooleanSetting(
         "PearlsRotate", "Rotate", "Sends a packet rotation right before throwing the pearl.", new CategorySetting.CategoryCondition(var5), true
      );
      this.xPCategory = new CategorySetting("XP", "Keybind and settings for throwing experience bottles.");
      CategorySetting var6 = this.xPCategory;
      this.xPBind = new KeybindSetting(
         "XPBind",
         "Bind",
         "The key that starts/stops throwing experience bottles. Hold/ReverseHold keep the loop matched to whether the bind is (or isn't) held instead of toggling it.",
         new CategorySetting.CategoryCondition(var6),
         0
      );
      CategorySetting var7 = this.xPCategory;
      this.xPSwitch = new ModeSetting(
         "XPSwitch",
         "Switch",
         "The mode that will be used for automatically switching to experience bottles.",
         new CategorySetting.CategoryCondition(var7),
         "Silent",
         InventoryUtil.a
      );
      CategorySetting var8 = this.xPCategory;
      this.xPRotate = new BooleanSetting(
         "XPRotate", "Rotate", "Rotates down to your feet when throwing experience bottles.", new CategorySetting.CategoryCondition(var8), true
      );
      CategorySetting var10 = this.xPCategory;
      CategorySetting.CategoryCondition var9 = new CategorySetting.CategoryCondition(var10);
      Integer var11 = 1;
      Integer var12 = 0;
      Integer var13 = 20;
      this.xPDelay = new NumberSetting("XPDelay", "Delay", "The delay in ticks between throwing experience bottles.", var9, var11, var12, var13);
      CategorySetting var15 = this.xPCategory;
      CategorySetting.CategoryCondition var14 = new CategorySetting.CategoryCondition(var15);
      Integer var16 = 1;
      Integer var17 = 1;
      Integer var18 = 15;
      this.xPRepeat = new NumberSetting("XPRepeat", "Repeat", "Allows you to throw a lot more XP bottles at once.", var14, var16, var17, var18);
      CategorySetting var20 = this.xPCategory;
      CategorySetting.CategoryCondition var19 = new CategorySetting.CategoryCondition(var20);
      String[] var21 = new String[]{"None", "Avoid"};
      this.xPAntiWaste = new ModeSetting("XPAntiWaste", "AntiWaste", "How wasting of experience bottles should be prevented.", var19, "Avoid", var21);
      CategorySetting var22 = this.xPCategory;
      this.xPItemDisable = new BooleanSetting(
         "XPItemDisable", "ItemDisable", "Automatically stops the XP loop when you run out of XP.", new CategorySetting.CategoryCondition(var22), true
      );
      this.t = false;
      this.u = 0;
      long var23 = 0L;
      this.v = var23;
   }

   public boolean v() {
      return this.t;
   }

   @EventTarget
   public void a(KeyInputEvent var1) {
      if (!this.isProxyDelegated() && !this.nullCheck() && (mc.gui == null || mc.gui.screen() == null)) {
         if (this.fireWorkBind.getKey() != 0) {
            int var2 = var1.getKey();
            int var3 = this.fireWorkBind.getKey();
            if (var2 == var3 && this.fireWorkBind.getMode().equals("Bind")) {
               this.x();
            }
         }

         if (this.pearlsBind.getKey() != 0) {
            int var4 = var1.getKey();
            int var5 = this.pearlsBind.getKey();
            if (var4 == var5 && this.pearlsBind.getMode().equals("Bind")) {
               this.y();
            }
         }

         if (this.xPBind.getKey() != 0) {
            int var6 = var1.getKey();
            int var7 = this.xPBind.getKey();
            if (var6 == var7 && this.xPBind.getMode().equals("Bind")) {
               boolean var8;
               if (!this.t) {
                  var8 = true;
               } else {
                  var8 = false;
               }

               this.t = var8;
            }
         }
      }
   }

   @EventTarget
   public void a(MouseButtonEvent var1) {
      if (!this.isProxyDelegated() && !this.nullCheck() && (mc.gui == null || mc.gui.screen() == null)) {
         int var2 = -var1.getButton() - 1;
         if (this.fireWorkBind.getKey() != 0) {
            int var3 = var2;
            int var4 = this.fireWorkBind.getKey();
            if (var3 == var4 && this.fireWorkBind.getMode().equals("Bind")) {
               this.x();
            }
         }

         if (this.pearlsBind.getKey() != 0) {
            int var5 = var2;
            int var6 = this.pearlsBind.getKey();
            if (var5 == var6 && this.pearlsBind.getMode().equals("Bind")) {
               this.y();
            }
         }

         if (this.xPBind.getKey() != 0) {
            int var7 = var2;
            int var8 = this.xPBind.getKey();
            if (var7 == var8 && this.xPBind.getMode().equals("Bind")) {
               boolean var9;
               if (!this.t) {
                  var9 = true;
               } else {
                  var9 = false;
               }

               this.t = var9;
            }
         }
      }
   }

   private void x() {
      if (mc.player != null && mc.level != null) {
         if (this.fireWorkSwitch.getValue().equalsIgnoreCase("None") && mc.player.getMainHandItem().getItem() != Items.FIREWORK_ROCKET) {
            String var1 = this.getName();
            Night.chatManager.sendTaggedMessage("You are currently not holding any fireworks.", var1);
         } else if (!mc.player.getCooldowns().isOnCooldown(new ItemStack(Items.FIREWORK_ROCKET))) {
            Item var2 = Items.FIREWORK_ROCKET;
            byte var3;
            if (!this.fireWorkSwitch.getValue().equalsIgnoreCase("AltSwap") && !this.fireWorkSwitch.getValue().equalsIgnoreCase("AltPickup")) {
               var3 = 8;
            } else {
               var3 = 35;
            }

            int var4 = InventoryUtil.a_int_Item_int_int(var2, 0, var3);
            int var5 = mc.player.getInventory().getSelectedSlot();
            if (var4 == -1) {
               String var6 = this.getName();
               Night.chatManager.sendTaggedMessage("No fireworks could be found in your hotbar.", var6);
            } else {
               String var7 = this.fireWorkSwitch.getValue();
               int var8 = var4;
               int var9 = var5;
               InventoryUtil.a_bool_String_int_int(var7, var8, var9);
               PacketPredictionUtil.a_void_SequencedPacketCreator(var0 -> {
                  float var1x = mc.player.getYRot();
                  float var2x = mc.player.getXRot();
                  return new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, var0, var1x, var2x);
               });
               String var10 = this.fireWorkSwitch.getValue();
               int var11 = var4;
               int var12 = var5;
               InventoryUtil.b(var10, var11, var12);
            }
         }
      }
   }

   public boolean w() {
      long var1 = System.currentTimeMillis();
      long var3 = this.v;
      long var5 = var1 - var3;
      long var7 = 100L;
      boolean var9;
      if (var5 < var7) {
         var9 = true;
      } else {
         var9 = false;
      }

      return var9;
   }

   private void y() {
      if (mc.player != null && mc.level != null) {
         if (this.pearlsSwitch.getValue().equalsIgnoreCase("None") && mc.player.getMainHandItem().getItem() != Items.ENDER_PEARL) {
            String var1 = this.getName();
            Night.chatManager.sendTaggedMessage("You are currently not holding any pearls.", var1);
         } else if (!mc.player.getCooldowns().isOnCooldown(new ItemStack(Items.ENDER_PEARL))) {
            if (!Night.moduleManager.getModule(AirStuck.class).a("KeyAction.pearl", this::y)) {
               long var2 = System.currentTimeMillis();
               this.v = var2;
               MultiTask var4 = Night.moduleManager.getModule(MultiTask.class);
               boolean var5;
               if (var4 != null && var4.isEnabled() && var4.pearl.getValue() && mc.player.isUsingItem()) {
                  var5 = true;
               } else {
                  var5 = false;
               }

               String var6;
               if (var5) {
                  var6 = "AltSwap";
               } else {
                  var6 = this.pearlsSwitch.getValue();
               }

               String var7 = var6;
               Item var8 = Items.ENDER_PEARL;
               byte var9;
               if (!var7.equalsIgnoreCase("AltSwap") && !var7.equalsIgnoreCase("AltPickup")) {
                  var9 = 8;
               } else {
                  var9 = 35;
               }

               int var10 = InventoryUtil.a_int_Item_int_int(var8, 0, var9);
               int var11 = mc.player.getInventory().getSelectedSlot();
               if (var10 == -1) {
                  String var12 = this.getName();
                  Night.chatManager.sendTaggedMessage("No pearls could be found in your hotbar.", var12);
               } else {
                  if (this.pearlsRotate.getValue()) {
                     float var13 = mc.player.getYRot();
                     float var14 = mc.player.getXRot();
                     Night.rotationManager.sendRotationPacket(var13, var14);
                  }

                  String var15 = var7;
                  int var16 = var10;
                  int var17 = var11;
                  InventoryUtil.a_bool_String_int_int(var15, var16, var17);
                  PacketPredictionUtil.a(var0 -> {
                     float var1x = mc.player.getYRot();
                     float var2x = mc.player.getXRot();
                     return new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, var0, var1x, var2x);
                  }, this::a);
                  Packet var18 = PacketAndRenderUtil.a_Packet_InteractionHand(InteractionHand.MAIN_HAND);
                  this.a(var18);
                  String var19 = var7;
                  int var20 = var10;
                  int var21 = var11;
                  InventoryUtil.b(var19, var20, var21);
               }
            }
         }
      }
   }

   @EventTarget
   public void a(UpdateWalkingPlayerEvent var1) {
      if (!this.isProxyDelegated() && !this.nullCheck() && (mc.gui == null || mc.gui.screen() == null)) {
         KeybindSetting var2 = this.fireWorkBind;
         this.a_void_KeybindSetting_Runnable(var2, this::x);
         KeybindSetting var3 = this.pearlsBind;
         this.a_void_KeybindSetting_Runnable(var3, this::y);
         if (this.xPBind.getKey() != 0 && !this.xPBind.getMode().equals("Bind")) {
            boolean var4 = KeyNameUtil.b(this.xPBind.getKey());
            boolean var5 = this.xPBind.getMode().equals("Hold");
            boolean var6 = var4;
            boolean var7;
            if (var5 == var6) {
               var7 = true;
            } else {
               var7 = false;
            }

            this.t = var7;
         }

         this.z();
      }
   }

   private void a_void_KeybindSetting_Runnable(KeybindSetting var1, Runnable var2) {
      if (var1.getKey() != 0 && !var1.getMode().equals("Bind")) {
         boolean var3 = KeyNameUtil.b(var1.getKey());
         boolean var4 = var1.getMode().equals("Hold");
         boolean var5 = var3;
         if (var4 == var5) {
            var2.run();
         }
      }
   }

   private void z() {
      if (this.t) {
         if (!this.isProxyDelegated()) {
            if (mc.player != null && mc.level != null) {
               if (this.xPSwitch.getValue().equalsIgnoreCase("None") && !(mc.player.getMainHandItem().getItem() instanceof BlockItem)) {
                  String var1 = this.getName();
                  Night.chatManager.sendTaggedMessage("You are currently not holding any experience bottles.", var1);
                  this.t = false;
               } else {
                  Sprint var2;
                  if (Night.moduleManager != null) {
                     var2 = Night.moduleManager.getModule(Sprint.class);
                  } else {
                     var2 = null;
                  }

                  Sprint var3 = var2;
                  float var4;
                  if (var3 != null && var3.isEnabled() && var3.isGrimSprintActive() && var3.v() != null) {
                     var4 = var3.v();
                  } else {
                     var4 = mc.player.getYRot();
                  }

                  float var5;
                  if (this.xPRotate.getValue()) {
                     var5 = 90.0F;
                  } else {
                     var5 = mc.player.getXRot();
                  }

                  float var6 = var5;
                  if (this.xPRotate.getValue() && Night.rotationManager != null) {
                     float var7 = var4;
                     float var8 = var6;
                     Night.rotationManager.sendOrBatchPosRot(var7, var8);
                  }

                  int var9 = this.u;
                  int var10 = this.xPDelay.getValue().intValue();
                  if (var9 < var10) {
                     int var11 = this.u + 1;
                     this.u = var11;
                  } else if (this.A() || this.xPAntiWaste.getValue().equals("None")) {
                     Item var12 = Items.EXPERIENCE_BOTTLE;
                     byte var13;
                     if (!this.xPSwitch.getValue().equalsIgnoreCase("AltSwap") && !this.xPSwitch.getValue().equalsIgnoreCase("AltPickup")) {
                        var13 = 8;
                     } else {
                        var13 = 35;
                     }

                     int var14 = InventoryUtil.a_int_Item_int_int(var12, 0, var13);
                     int var15 = mc.player.getInventory().getSelectedSlot();
                     if (var14 == -1) {
                        String var16 = this.getName();
                        Night.chatManager.sendTaggedMessage("No experience bottles could be found in your hotbar.", var16);
                        this.t = false;
                     } else {
                        String var17 = this.xPSwitch.getValue();
                        int var18 = var14;
                        int var19 = var15;
                        InventoryUtil.a_bool_String_int_int(var17, var18, var19);
                        int var20 = 0;

                        while (true) {
                           int var21 = var20;
                           int var22 = this.xPRepeat.getValue().intValue();
                           if (var21 >= var22) {
                              Packet var25 = PacketAndRenderUtil.a_Packet_InteractionHand(InteractionHand.MAIN_HAND);
                              this.a(var25);
                              String var26 = this.xPSwitch.getValue();
                              int var27 = var14;
                              int var28 = var15;
                              InventoryUtil.b(var26, var27, var28);
                              this.u = 0;
                              return;
                           }

                           float var23 = var4;
                           float var24 = var6;
                           PacketPredictionUtil.a(var2x -> new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, var2x, var23, var24), this::a);
                           var20++;
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private void a(Packet<?> var1) {
      if (this.isRunningOnProxyServer() && Night.pingBypassServer != null) {
         Connection var2 = Night.pingBypassServer.getBackendConnection();
         if (var2 != null && var2.isConnected()) {
            RubberbandTracker.a(() -> var2.send(var1));
            return;
         }
      }

      mc.getConnection().send(var1);
   }

   private boolean A() {
      EquipmentSlot[] var1 = new EquipmentSlot[]{EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD};
      EquipmentSlot[] var2 = var1;
      int var3 = var2.length;
      int var4 = 0;

      while (true) {
         int var5 = var4;
         int var6 = var3;
         if (var5 >= var6) {
            return false;
         }

         EquipmentSlot[] var7 = var2;
         int var8 = var4;
         EquipmentSlot var9 = var7[var8];
         LocalPlayer var10 = mc.player;
         EquipmentSlot var11 = var9;
         ItemStack var12 = var10.getItemBySlot(var11);
         if (!var12.isEmpty()
            && (var12.is(ItemTags.FOOT_ARMOR) || var12.is(ItemTags.LEG_ARMOR) || var12.is(ItemTags.CHEST_ARMOR) || var12.is(ItemTags.HEAD_ARMOR))) {
            int var13 = var12.getMaxDamage();
            int var14 = var12.getDamageValue();
            float var15 = (var13 - var14) * 100.0F;
            float var16 = var12.getMaxDamage();
            if (Math.round(var15 / var16) < 100.0F) {
               return true;
            }
         }

         var4++;
      }
   }

   @Override
   public void onDisable() {
      this.t = false;
      this.u = 0;
   }
}
