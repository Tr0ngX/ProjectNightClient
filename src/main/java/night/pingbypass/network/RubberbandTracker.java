package night.pingbypass.network;

import java.util.Set;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ServerboundClientInformationPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ServerboundClientTickEndPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ServerboundPaddleBoatPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerLoadedPacket;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.events.EventTarget;
import night.events.impl.PacketReceiveEvent;
import night.events.impl.PacketSendEvent;
import night.events.impl.TickEvent;
import night.pingbypass.PingBypassServer;
import night.pingbypass.PingBypassState;
import night.util.world.PacketAndRenderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RubberbandTracker {
   private static final Logger a = LoggerFactory.getLogger("Night/Rubberband");
   private final PingBypassServer b;
   private static final ThreadLocal<Boolean> c = ThreadLocal.withInitial(() -> false);

   public RubberbandTracker(PingBypassServer var1) {
      this.b = var1;
   }

   @EventTarget
   public void a(TickEvent var1) {
      Night.proxyModuleManager.onTick();
      if (this.b.isStayConnected()) {
         this.b.tick();
      }
   }

   public static void a(Runnable var0) {
      Boolean var1 = true;
      c.set(var1);

      try {
         var0.run();
      } catch (Throwable var5) {
         Boolean var4 = false;
         c.set(var4);
         throw var5;
      }

      Boolean var2 = false;
      c.set(var2);
   }

   @EventTarget
   public void a(PacketReceiveEvent var1) {
      if (PingBypassState.d) {
         if (var1.getPacket() instanceof ClientboundPlayerPositionPacket var3) {
            Object[] var4 = new Object[4];
            Integer var5 = var3.id();
            var4[0] = var5;
            Vec3 var6 = var3.change().position();
            var4[1] = var6;
            Set var7 = var3.relatives();
            var4[2] = var7;
            Boolean var8 = PingBypassState.e;
            var4[3] = var8;
            a.info("[PB] Real server sent teleport/correction id={} pos={} rel={} onGround(from client, stale on ghost until next move)={}", var4);
         }
      }
   }

   @EventTarget
   public void a(PacketSendEvent var1) {
      Packet var2 = var1.getPacket();
      if (var2 instanceof ServerboundAcceptTeleportationPacket) {
         Integer var3 = PacketAndRenderUtil.a_int_ServerboundAcceptTeleportationPacket((ServerboundAcceptTeleportationPacket)var2);
         Object var4 = c.get();
         a.info("[PB] AcceptTeleportation id={} isRealClientConfirm(allowSend)={}", var3, var4);
      }

      if (PingBypassState.d) {
         if (!c.get()) {
            Packet var5 = var1.getPacket();
            if (var5 instanceof ServerboundClientTickEndPacket
               || var5 instanceof ServerboundPlayerInputPacket
               || var5 instanceof ServerboundMoveVehiclePacket
               || var5 instanceof ServerboundPaddleBoatPacket
               || var5 instanceof ServerboundClientInformationPacket
               || var5 instanceof ServerboundPlayerAbilitiesPacket
               || var5 instanceof ServerboundContainerClosePacket
               || var5 instanceof ServerboundPlayerLoadedPacket
               || var5 instanceof ServerboundMovePlayerPacket
               || var5 instanceof ServerboundAcceptTeleportationPacket
               || var5 instanceof ServerboundPlayerCommandPacket) {
               var1.setCancelled(true);
            }
         }
      }
   }
}
