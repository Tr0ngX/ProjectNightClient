package night.pingbypass;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import night.Night;
import night.events.EventTarget;
import night.events.impl.MouseButtonEvent;
import night.events.impl.TickEvent;
import night.pingbypass.protocol.PingBypassPayload;
import night.pingbypass.protocol.c2s.C2SInventoryActionPacket;
import night.pingbypass.protocol.c2s.C2SPlayerInputPacket;

public class ClientInputForwarder {
   private boolean a = false;

   @EventTarget
   public void a(TickEvent var1) {
      if (PingBypassState.d) {
         if (Night.pingBypassConfig == null || !Night.pingBypassConfig.isServerMode()) {
            Minecraft var2 = Minecraft.getInstance();
            if (var2.player != null && var2.getConnection() != null) {
               boolean var6;
               label35: {
                  Screen var3 = var2.gui.screen();
                  if (var3 instanceof InventoryScreen) {
                     AbstractContainerMenu var4 = ((InventoryScreen)var3).getMenu();
                     InventoryMenu var5 = var2.player.inventoryMenu;
                     if (var4 == var5) {
                        var6 = true;
                        break label35;
                     }
                  }

                  var6 = false;
               }

               boolean var7 = var6;
               boolean var8 = this.a;
               if (var7 != var8) {
                  boolean var9 = var7;
                  this.a = var9;

                  try {
                     boolean var10 = var7;
                     PingBypassPayload var11 = PingBypassPayload.fromPacket(new C2SPlayerInputPacket(var10));
                     Connection var12 = var2.getConnection().getConnection();
                     PingBypassPayload var13 = var11;
                     var12.send(new ServerboundCustomPayloadPacket(var13));
                  } catch (Exception var15) {
                     Night.LOGGER.warn("[PingBypass] Failed to send inventory-open state", var15);
                  }
               }
            }
         }
      }
   }

   @EventTarget
   public void a(MouseButtonEvent var1) {
      if (PingBypassState.d) {
         if (Night.pingBypassConfig == null || !Night.pingBypassConfig.isServerMode()) {
            Minecraft var2 = Minecraft.getInstance();
            if (var2.getConnection() != null) {
               int var3 = var1.getButton();
               byte var4;
               if (var3 == 0) {
                  var4 = 0;
               } else if (var3 == 1) {
                  var4 = 1;
               } else {
                  var4 = -1;
               }

               byte var5 = var4;
               if (var5 >= 0) {
                  Minecraft var6 = var2;
                  byte var7 = var5;
                  this.a(var6, 0, var7, 1);
               }
            }
         }
      }
   }

   private void a(Minecraft var1, int var2, int var3, int var4) {
      try {
         PingBypassPayload var5 = PingBypassPayload.fromPacket(new C2SInventoryActionPacket(var2, var3, var4));
         Connection var6 = var1.getConnection().getConnection();
         PingBypassPayload var7 = var5;
         var6.send(new ServerboundCustomPayloadPacket(var7));
      } catch (Exception var9) {
         Night.LOGGER.warn("[PingBypass] Failed to send input", var9);
      }
   }

   public void a() {
      Night.EVENT_BUS.register(this);
      Night.LOGGER.info("[PingBypass] Client input forwarder started");
   }

   public void b() {
      Night.EVENT_BUS.unregister(this);
   }
}
