package night.manager;

import lombok.Generated;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.events.EventTarget;
import night.events.impl.PacketSendEvent;
import night.util.MinecraftInstance;

public class PositionManager implements MinecraftInstance {
   private double serverX;
   private double serverY;
   private double serverZ;
   private boolean positionInitialized;
   private boolean onGround;
   private boolean sprinting;
   private boolean sneaking;
   private int heldSlot;

   public PositionManager() {
      Night.EVENT_BUS.register(this);
   }

   @EventTarget
   public void onPacketSend(PacketSendEvent var1) {
      if (mc.player != null) {
         if (var1.getPacket() instanceof ServerboundMovePlayerPacket var3) {
            if (var3.hasPosition()) {
               ServerboundMovePlayerPacket var4 = var3;
               double var5 = mc.player.getX();
               double var7 = var4.getX(var5);
               this.serverX = var7;
               ServerboundMovePlayerPacket var9 = var3;
               double var10 = mc.player.getY();
               double var12 = var9.getY(var10);
               this.serverY = var12;
               ServerboundMovePlayerPacket var14 = var3;
               double var15 = mc.player.getZ();
               double var17 = var14.getZ(var15);
               this.serverZ = var17;
               this.positionInitialized = true;
            }

            boolean var19 = var3.isOnGround();
            this.onGround = var19;
         }

         Packet var20 = var1.getPacket();
         if (var20 instanceof ServerboundSetCarriedItemPacket) {
            int var21 = ((ServerboundSetCarriedItemPacket)var20).getSlot();
            this.heldSlot = var21;
         }

         Packet var22 = var1.getPacket();
         if (var22 instanceof ServerboundPlayerCommandPacket) {
            switch (((ServerboundPlayerCommandPacket)var22).getAction()) {
               case START_SPRINTING:
                  this.sprinting = true;
                  break;
               case STOP_SPRINTING:
                  this.sprinting = false;
            }
         }

         Packet var23 = var1.getPacket();
         if (var23 instanceof ServerboundPlayerInputPacket) {
            boolean var24 = ((ServerboundPlayerInputPacket)var23).input().shift();
            this.sneaking = var24;
         }
      }
   }

   public Vec3 getServerEyePosition() {
      if (!this.positionInitialized) {
         return mc.player.getEyePosition();
      }

      double var1 = this.serverX;
      double var3 = this.serverY;
      double var5 = mc.player.getEyeHeight();
      double var7 = var3 + var5;
      double var9 = this.serverZ;
      return new Vec3(var1, var7, var9);
   }

   public double getX() {
      double var1;
      if (mc.player != null) {
         var1 = mc.player.getX();
      } else {
         var1 = this.serverX;
      }

      return var1;
   }

   public double getY() {
      double var1;
      if (mc.player != null) {
         var1 = mc.player.getY();
      } else {
         var1 = this.serverY;
      }

      return var1;
   }

   public double getZ() {
      double var1;
      if (mc.player != null) {
         var1 = mc.player.getZ();
      } else {
         var1 = this.serverZ;
      }

      return var1;
   }

   public boolean isOnGround() {
      boolean var1;
      if (mc.player != null) {
         var1 = mc.player.onGround();
      } else {
         var1 = this.onGround;
      }

      return var1;
   }

   @Generated
   public int getHeldSlot() {
      return this.heldSlot;
   }
}
