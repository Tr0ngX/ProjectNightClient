package night.module.impl.combat;

import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.PosRot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TippedArrowItem;
import night.Night;
import night.events.EventTarget;
import night.events.impl.UpdateWalkingPlayerEvent;
import night.module.Module;
import night.module.ModuleInfo;
import night.pingbypass.network.RubberbandTracker;
import night.setting.impl.BooleanSetting;
import night.setting.impl.ModeSetting;
import night.setting.impl.NumberSetting;
import night.util.inventory.InventoryUtil;

@ModuleInfo(name = "SelfBow", description = "Automatically shoots arrows at you in order to give yourself potion effects.", category = Module.Category.COMBAT)
public class SelfBow extends Module {
   public BooleanSetting manual = new BooleanSetting("Manual", "Whether or not to do the self bow manually.", false);
   public ModeSetting switchSetting;
   public NumberSetting chargeTime;
   public BooleanSetting effectCycle;
   private boolean proxySupported;
   private boolean enabled;
   private boolean k;
   private int l;
   private int m;
   private int n;
   private final ConcurrentLinkedQueue<Integer> o;

   public SelfBow() {
      String[] var1 = new String[]{"None", "Normal"};
      this.switchSetting = new ModeSetting("Switch", "The mode that will be used for switching slots.", "Normal", var1);
      Integer var2 = 4;
      Integer var3 = 0;
      Integer var4 = 20;
      this.chargeTime = new NumberSetting("ChargeTime", "The amount of ticks that the module will be charging the bow for.", var2, var3, var4);
      this.effectCycle = new BooleanSetting("EffectCycle", "Fires multiple arrows in case of having more than one arrow type.", false);
      this.proxySupported = false;
      this.enabled = false;
      this.k = false;
      this.l = -1;
      this.m = 0;
      this.n = -1;
      this.o = new ConcurrentLinkedQueue<>();
   }

   @EventTarget
   public void a(UpdateWalkingPlayerEvent var1) {
      if (mc.player != null && mc.level != null) {
         int var2 = InventoryUtil.b_int_Item(Items.BOW);
         if (this.manual.getValue()) {
            boolean var3;
            if (mc.player.isUsingItem() && mc.player.getInventory().getSelectedItem().getItem() == Items.BOW) {
               var3 = true;
            } else {
               var3 = false;
            }

            if (var3 && !this.enabled) {
               if (this.effectCycle.getValue()) {
                  this.v();
               }

               this.enabled = true;
               this.k = true;
            }

            if (this.enabled) {
               if (mc.player.getInventory().getSelectedItem().getItem() != Items.BOW) {
                  this.enabled = false;
                  mc.options.keyUse.setDown(false);
                  return;
               }

               mc.options.keyUse.setDown(true);
            }

            if (!this.enabled) {
               return;
            }
         } else {
            if (this.switchSetting.getValue().equals("None") && mc.player.getInventory().getSelectedItem().getItem() != Items.BOW) {
               String var4 = this.getName();
               Night.chatManager.sendTaggedMessage("You are currently not holding a bow.", var4);
               this.setEnabled(false);
               return;
            }

            if (!this.switchSetting.getValue().equals("None") && var2 == -1) {
               String var5 = this.getName();
               Night.chatManager.sendTaggedMessage("Could not find a bow in your hotbar.", var5);
               this.setEnabled(false);
               return;
            }

            if (mc.player.getMainHandItem().getItem() != Items.BOW) {
               String var6 = this.switchSetting.getValue();
               int var7 = var2;
               int var8 = this.l;
               InventoryUtil.a_bool_String_int_int(var6, var7, var8);
               this.proxySupported = true;
            }

            mc.options.keyUse.setDown(true);
         }

         int var9 = this.m;
         int var10 = this.chargeTime.getValue().intValue();
         byte var11;
         if (this.k) {
            var11 = 1;
         } else {
            var11 = 0;
         }

         int var12 = var10 - var11;
         if (var9 < var12) {
            int var13 = this.m + 1;
            this.m = var13;
         } else {
            if (this.effectCycle.getValue() && !this.o.isEmpty()) {
               int var14 = this.o.poll();
               int var15 = this.n;
               if (var14 != var15) {
                  int var16 = var14;
                  int var17 = this.n;
                  InventoryUtil.c("Pickup", var16, var17);
               }
            }

            if (mc.getConnection() != null) {
               RubberbandTracker.a(() -> {
                  ClientPacketListener var0 = mc.getConnection();
                  double var1x = Night.positionManager.getX();
                  double var3x = Night.positionManager.getY();
                  double var5x = Night.positionManager.getZ();
                  float var7x = mc.player.getYRot();
                  boolean var8x = Night.positionManager.isOnGround();
                  boolean var9x = mc.player.horizontalCollision;
                  var0.send(new PosRot(var1x, var3x, var5x, var7x, -90.0F, var8x, var9x));
               });
            }

            mc.options.keyUse.setDown(false);
            MultiPlayerGameMode var18 = mc.gameMode;
            LocalPlayer var19 = mc.player;
            var18.releaseUsingItem(var19);
            this.m = 0;
            this.k = false;
            if (!this.effectCycle.getValue() || this.o.isEmpty()) {
               if (this.manual.getValue()) {
                  this.enabled = false;
                  mc.options.keyUse.setDown(false);
               } else {
                  this.setEnabled(false);
               }
            }
         }
      }
   }

   @Override
   public void onEnable() {
      if (!this.nullCheck()) {
         if (this.effectCycle.getValue() && !this.manual.getValue()) {
            this.v();
         }

         int var1 = mc.player.getInventory().getSelectedSlot();
         this.l = var1;
      }
   }

   @Override
   public void onDisable() {
      if (mc.player != null && mc.level != null) {
         if (this.proxySupported) {
            String var1 = this.switchSetting.getValue();
            int var2 = this.l;
            int var3 = this.l;
            InventoryUtil.a_bool_String_int_int(var1, var2, var3);
         }

         mc.options.keyUse.setDown(false);
      }
   }

   private void v() {
      this.n = -1;

      for (int var1 = 9; var1 < 36; var1++) {
         Inventory var2 = mc.player.getInventory();
         int var3 = var1;
         if (!var2.getItem(var3).isEmpty()) {
            Inventory var4 = mc.player.getInventory();
            int var5 = var1;
            if (var4.getItem(var5).getItem() instanceof TippedArrowItem) {
               if (this.n == -1) {
                  int var6 = var1;
                  this.n = var6;
               }

               ConcurrentLinkedQueue var7 = this.o;
               Integer var8 = var1;
               var7.add(var8);
            }
         }
      }
   }

   @Override
   public String getDisplayInfo() {
      int var1;
      if (mc.player.getInventory().getSelectedItem().getItem() == Items.BOW) {
         var1 = mc.player.getTicksUsingItem();
      } else {
         var1 = 0;
      }

      return String.valueOf(var1);
   }
}
