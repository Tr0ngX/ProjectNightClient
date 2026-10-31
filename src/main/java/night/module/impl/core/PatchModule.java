package night.module.impl.core;

import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import night.Night;
import night.events.EventTarget;
import night.events.impl.PacketReceiveEvent;
import night.module.Module;
import night.module.ModuleInfo;
import night.module.access.PatchAccess;
import night.setting.impl.BooleanSetting;

@ModuleInfo(name = "Patch", description = "Server/anticheat-specific hotfixes.", category = Module.Category.CORE, persistent = true, drawn = false)
public class PatchModule extends Module implements PatchAccess {
   public BooleanSetting grimAttackVelocity = new BooleanSetting(
      "GrimAttackVelocity", "Stops your own sprint and movement from being cut after landing extra knockback on a hit.", false
   );
   public BooleanSetting silentSwapFix = new BooleanSetting("SilentSwapFix", "Stops hotbar slot desync and ghost item duplication during silent switch.", true);

   @Override
   public boolean isGrimAttackVelocityEnabled() {
      return this.grimAttackVelocity.getValue();
   }

   @EventTarget
   public void a(PacketReceiveEvent var1) {
      if (mc.player != null) {
         if (this.silentSwapFix.getValue()) {
            if (var1.getPacket() instanceof ClientboundContainerSetSlotPacket var3) {
               if (var3.getContainerId() == 0) {
                  int var4 = var3.getSlot();
                  if (var4 >= 36 && var4 <= 44) {
                     int var5 = var4 - 36;
                     ItemStack var6 = var3.getItem();
                     Inventory var7 = mc.player.getInventory();
                     int var8 = var5;
                     ItemStack var9 = var7.getItem(var8);
                     ItemStack var10 = mc.player.getMainHandItem();
                     if (!var6.isEmpty() && !var9.isEmpty()) {
                        Item var11 = var6.getItem();
                        Item var12 = var9.getItem();
                        if (var11 == var12) {
                           int var13 = var6.getCount();
                           int var14 = var9.getCount();
                           if (var13 == var14) {
                              var1.setCancelled(true);
                              return;
                           }
                        }
                     }

                     label49: {
                        if (mc.gui.screen() == null && !var9.isEmpty() && !var6.isEmpty()) {
                           Item var15 = var9.getItem();
                           Item var16 = var6.getItem();
                           if (var15 != var16) {
                              int var17 = mc.player.getInventory().getSelectedSlot();
                              int var18 = Night.positionManager.getHeldSlot();
                              int var19 = var5;
                              int var20 = var17;
                              if (var19 != var20) {
                                 Item var21 = var6.getItem();
                                 Item var22 = var10.getItem();
                                 if (var21 == var22) {
                                    break label49;
                                 }

                                 if (var18 >= 0 && var18 <= 8) {
                                    Item var23 = var6.getItem();
                                    Inventory var24 = mc.player.getInventory();
                                    int var25 = var18;
                                    Item var26 = var24.getItem(var25).getItem();
                                    if (var23 == var26) {
                                       break label49;
                                    }
                                 }
                              }
                           }
                        }

                        return;
                     }

                     var1.setCancelled(true);
                  }
               }
            }
         }
      }
   }
}
