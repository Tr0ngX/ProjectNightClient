package night.module.impl.movement;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.Pos;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.PosRot;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.Rot;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.StatusOnly;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket.Action;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import night.Night;
import night.events.EventTarget;
import night.events.impl.MotionSyncEvent;
import night.events.impl.PacketSendEvent;
import night.module.Module;
import night.module.ModuleInfo;
import night.setting.impl.BooleanSetting;
import night.setting.impl.ModeSetting;
import night.util.network.PacketPredictionUtil;

@ModuleInfo(name = "NoSlow", description = "Removes the slowness effect that you receive when doing certain actions.", category = Module.Category.MOVEMENT)
public class NoSlow extends Module {
   public BooleanSetting items = new BooleanSetting("Items", "Removes the slowness effect from eating or using items.", true);
   public ModeSetting mode;
   public BooleanSetting crawl;
   public BooleanSetting sneak;
   public BooleanSetting soulSand;
   public BooleanSetting slimeBlocks;
   public BooleanSetting honeyBlocks;

   public NoSlow() {
      String[] var1 = new String[]{"Normal", "Strict", "GrimV2", "GrimV3"};
      this.mode = new ModeSetting("Mode", "Item slowdown bypass mode.", "Normal", var1);
      this.crawl = new BooleanSetting("Crawl", "Removes the slowness effect from crawling.", false);
      this.sneak = new BooleanSetting("Sneak", "Removes the slowness effect from sneaking.", false);
      this.soulSand = new BooleanSetting("SoulSand", "Removes the slowness effect from walking on soul sand.", false);
      this.slimeBlocks = new BooleanSetting("SlimeBlocks", "Removes the slowness effect from walking on slime blocks.", false);
      this.honeyBlocks = new BooleanSetting("HoneyBlocks", "Removes the slowness effect from walking on honey blocks.", false);
   }

   @EventTarget
   public void a_void_Post(MotionSyncEvent.Post var1) {
      if (mc.player != null && mc.level != null) {
         if (this.items.getValue() && mc.player.isUsingItem() && !mc.player.isPassenger() && !mc.player.isFallFlying()) {
            float var2;
            if (Night.rotationManager.getCurrentRotation() != null) {
               var2 = Night.rotationManager.getCurrentRotation().getYaw();
            } else {
               var2 = mc.player.getYRot();
            }

            float var3 = var2;
            float var4;
            if (Night.rotationManager.getCurrentRotation() != null) {
               var4 = Night.rotationManager.getCurrentRotation().getPitch();
            } else {
               var4 = mc.player.getXRot();
            }

            float var5 = var4;
            if (this.mode.getValue().equals("GrimV2")) {
               if (!mc.player.isShiftKeyDown()) {
                  if (!mc.player.getUseItem().has(DataComponents.FOOD)) {
                     ItemStack var6 = mc.player.getUseItem();
                     if (!this.a_bool_ItemStack(var6)) {
                        return;
                     }
                  }

                  InteractionHand var7;
                  if (mc.player.getUsedItemHand() == InteractionHand.MAIN_HAND) {
                     var7 = InteractionHand.OFF_HAND;
                  } else {
                     var7 = InteractionHand.MAIN_HAND;
                  }

                  InteractionHand var13 = var7;
                  float var14 = var3;
                  float var15 = var5;
                  PacketPredictionUtil.a_void_SequencedPacketCreator(var3x -> new ServerboundUseItemPacket(var13, var3x, var14, var15));
               }
            } else if (this.mode.getValue().equals("Strict") && !PacketPredictionUtil.b()) {
               if (mc.player.getUsedItemHand() == InteractionHand.OFF_HAND) {
                  int var8 = mc.player.getInventory().getSelectedSlot();
                  ClientPacketListener var9 = mc.getConnection();
                  int var10 = (var8 + 1) % 9;
                  var9.send(new ServerboundSetCarriedItemPacket(var10));
                  ClientPacketListener var11 = mc.getConnection();
                  int var12 = var8;
                  var11.send(new ServerboundSetCarriedItemPacket(var12));
               } else {
                  float var16 = var3;
                  float var17 = var5;
                  PacketPredictionUtil.a_void_SequencedPacketCreator(var2x -> new ServerboundUseItemPacket(InteractionHand.OFF_HAND, var2x, var16, var17));
               }
            }
         }
      }
   }

   @EventTarget
   public void a_void_PacketSendEvent(PacketSendEvent var1) {
      if (mc.player != null && mc.level != null) {
         if (this.mode.getValue().equals("Strict")) {
            if (var1.getPacket() instanceof PosRot
               || var1.getPacket() instanceof Pos
               || var1.getPacket() instanceof Rot
               || var1.getPacket() instanceof StatusOnly) {
               ClientPacketListener var2 = mc.player.connection;
               int var3 = mc.player.getInventory().getSelectedSlot();
               var2.send(new ServerboundSetCarriedItemPacket(var3));
            }

            if (var1.getPacket() instanceof ServerboundContainerClickPacket) {
               if (mc.player.isUsingItem()) {
                  mc.player.stopUsingItem();
               }

               if (mc.player.isSprinting()) {
                  ClientPacketListener var4 = mc.player.connection;
                  LocalPlayer var5 = mc.player;
                  var4.send(new ServerboundPlayerCommandPacket(var5, Action.STOP_SPRINTING));
               }

               if (mc.player.isShiftKeyDown()) {
                  ClientPacketListener var6 = mc.player.connection;
                  Input var7 = new Input(false, false, false, false, false, false, false);
                  var6.send(new ServerboundPlayerInputPacket(var7));
               }
            }
         }
      }
   }

   @Override
   public void onDisable() {
      Night.worldStateManager.setTimerMultiplier(1.0F);
   }

   public boolean a_bool_ItemStack(ItemStack var1) {
      if (var1 != null && !var1.isEmpty()) {
         boolean var2;
         if (!var1.has(DataComponents.CONSUMABLE)
            && !var1.has(DataComponents.FOOD)
            && !var1.is(Items.BOW)
            && !var1.is(Items.CROSSBOW)
            && !var1.is(Items.SHIELD)
            && !var1.is(Items.TRIDENT)
            && !var1.is(Items.SPYGLASS)
            && !var1.is(Items.GOAT_HORN)) {
            var2 = false;
         } else {
            var2 = true;
         }

         return var2;
      } else {
         return false;
      }
   }

   public boolean v() {
      if (mc.player == null) {
         return false;
      }

      boolean var1;
      if (mc.player.getTicksUsingItem() > 1 && mc.player.getTicksUsingItem() % 2 != 0) {
         var1 = true;
      } else {
         var1 = false;
      }

      return var1;
   }

   public boolean w() {
      if (mc.player != null && this.items.getValue()) {
         if (this.mode.getValue().equals("GrimV3")) {
            boolean var1;
            if (!this.v()) {
               var1 = true;
            } else {
               var1 = false;
            }

            return var1;
         } else {
            return false;
         }
      } else {
         return true;
      }
   }
}
