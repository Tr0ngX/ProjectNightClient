package night.module.impl.combat;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.Pos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.Items;
import night.events.EventTarget;
import night.events.impl.AttackEntityEvent;
import night.events.impl.PacketSendEvent;
import night.mixins.accessors.PlayerMoveC2SPacketAccessor;
import night.module.Module;
import night.module.ModuleInfo;
import night.setting.impl.BooleanSetting;
import night.setting.impl.ModeSetting;
import night.setting.impl.NumberSetting;
import night.util.inventory.InventoryUtil;

@ModuleInfo(name = "MaceSpoof", description = "Spoof fall height for mace damage.", category = Module.Category.COMBAT)
public class MaceSpoof extends Module {
   public ModeSetting mode;
   public BooleanSetting noCrystal;
   public BooleanSetting inventorySwap;
   public BooleanSetting onlyGround;
   public NumberSetting height;
   private boolean enabled;

   public MaceSpoof() {
      String[] var1 = new String[]{"Vanilla", "NCP", "Swap"};
      this.mode = new ModeSetting("Mode", "Mode of spoofing.", "Vanilla", var1);
      ModeSetting var3 = this.mode;
      String[] var4 = new String[]{"Swap"};
      ModeSetting.ModeCondition var2 = new ModeSetting.ModeCondition(var3, var4);
      this.noCrystal = new BooleanSetting("NoCrystal", "Don't apply on crystal attack.", var2, true);
      ModeSetting var6 = this.mode;
      String[] var7 = new String[]{"Swap"};
      ModeSetting.ModeCondition var5 = new ModeSetting.ModeCondition(var6, var7);
      this.inventorySwap = new BooleanSetting("InventorySwap", "Use inventory swap (vs hotbar swap).", var5, false);
      ModeSetting var8 = this.mode;
      String[] var9 = new String[]{"Vanilla"};
      this.onlyGround = new BooleanSetting("OnlyGround", "Only spoof when on ground or flying.", new ModeSetting.ModeCondition(var8, var9), true);
      ModeSetting var11 = this.mode;
      String[] var12 = new String[]{"Vanilla"};
      ModeSetting.ModeCondition var10 = new ModeSetting.ModeCondition(var11, var12);
      Double var13 = 25.0;
      Double var14 = 1.0;
      Double var15 = 2000.0;
      this.height = new NumberSetting("Height", "Fake fall height.", var10, var13, var14, var15);
      this.enabled = false;
   }

   @EventTarget
   public void a(AttackEntityEvent var1) {
      if (!this.enabled) {
         if (mc.player != null && mc.level != null && mc.getConnection() != null) {
            if (this.mode.getValue().equalsIgnoreCase("Vanilla")) {
               if (!mc.player.getMainHandItem().is(Items.MACE)) {
                  return;
               }

               Entity var7 = var1.getEntity();
               if (var7 == null || var7 instanceof EndCrystal) {
                  return;
               }

               if (this.onlyGround.getValue() && !mc.player.onGround() && !mc.player.getAbilities().flying) {
                  return;
               }

               if (mc.player.isInLava() || mc.player.isUnderWater() || mc.player.isInWater()) {
                  return;
               }

               for (int var8 = 0; var8 < 4; var8++) {
                  double var9 = 0.0;
                  this.a(var9);
               }

               double var11 = this.height.getValue().doubleValue();
               this.a(var11);
               double var13 = 0.0;
               this.a(var13);
            } else if (this.mode.getValue().equalsIgnoreCase("Swap")) {
               Entity var2 = var1.getEntity();
               if (this.noCrystal.getValue() && var2 instanceof EndCrystal) {
                  return;
               }

               int var3 = this.v();
               if (var3 == -1) {
                  return;
               }

               try {
                  this.enabled = true;
                  Entity var4 = var2;
                  int var5 = var3;
                  this.a(var4, var5);
               } catch (Throwable var15) {
                  this.enabled = false;
                  throw var15;
               }

               this.enabled = false;
            }
         }
      }
   }

   @EventTarget
   public void a(PacketSendEvent var1) {
      if (mc.player != null && mc.level != null) {
         if (this.mode.getValue().equalsIgnoreCase("NCP")) {
            Packet var2 = var1.getPacket();
            if (var2 instanceof ServerboundMovePlayerPacket) {
               ((PlayerMoveC2SPacketAccessor)((ServerboundMovePlayerPacket)var2)).setOnGround(false);
            }
         }
      }
   }

   private void a(Entity var1, int var2) {
      int var3 = mc.player.getInventory().getSelectedSlot();
      if (this.inventorySwap.getValue()) {
         int var4 = var3;
         InventoryUtil.a_void_int_int_ContainerInput(var2, var4, ContainerInput.SWAP);
         MultiPlayerGameMode var5 = mc.gameMode;
         LocalPlayer var6 = mc.player;
         var5.attack(var6, var1);
         mc.player.resetAttackStrengthTicker();
         int var7 = var3;
         InventoryUtil.a_void_int_int_ContainerInput(var2, var7, ContainerInput.SWAP);
      } else {
         int var8 = var3;
         InventoryUtil.a_bool_String_int_int("Silent", var2, var8);
         MultiPlayerGameMode var9 = mc.gameMode;
         LocalPlayer var10 = mc.player;
         var9.attack(var10, var1);
         mc.player.resetAttackStrengthTicker();
         int var11 = var3;
         InventoryUtil.b("Silent", var2, var11);
      }
   }

   private int v() {
      if (mc.player == null) {
         return -1;
      }

      if (this.inventorySwap.getValue()) {
         for (int var1 = 9; var1 <= 35; var1++) {
            Inventory var2 = mc.player.getInventory();
            int var3 = var1;
            if (var2.getItem(var3).is(Items.MACE)) {
               return var1;
            }
         }
      } else {
         for (int var4 = 0; var4 < 9; var4++) {
            Inventory var5 = mc.player.getInventory();
            int var6 = var4;
            if (var5.getItem(var6).is(Items.MACE)) {
               return var4;
            }
         }
      }

      return -1;
   }

   private void a(double var1) {
      ClientPacketListener var3 = mc.getConnection();
      double var4 = mc.player.getX();
      double var6 = mc.player.getY();
      double var8 = var1;
      double var10 = var6 + var8;
      double var12 = mc.player.getZ();
      boolean var14 = mc.player.horizontalCollision;
      var3.send(new Pos(var4, var10, var12, false, var14));
   }

   @Override
   public String getDisplayInfo() {
      return this.mode.getValue();
   }
}
