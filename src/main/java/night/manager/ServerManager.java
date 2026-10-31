package night.manager;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;
import java.util.Arrays;
import java.util.UUID;
import lombok.Generated;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import night.Night;
import night.events.EventTarget;
import night.events.impl.ClientConnectEvent;
import night.events.impl.PacketReceiveEvent;
import night.events.impl.ServerConnectEvent;
import night.module.impl.misc.FastLatency;
import night.pingbypass.PingBypassState;
import night.util.MinecraftInstance;
import night.util.math.TimerUtil;

public class ServerManager implements MinecraftInstance {
   private final TimerUtil setbackTimer = new TimerUtil();
   private final TimerUtil packetTimer = new TimerUtil();
   private final float[] tpsSamples;
   private int sampleIndex;
   private long lastTimePacketMs;
   private long connectTimeMs;
   private Pair<ServerAddress, ServerData> lastServerConnection;

   public ServerManager() {
      float[] var1 = new float[20];
      this.tpsSamples = var1;
      this.sampleIndex = 0;
      long var2 = -1L;
      this.lastTimePacketMs = var2;
      Night.EVENT_BUS.register(this);
   }

   @EventTarget
   public void a(PacketReceiveEvent var1) {
      this.packetTimer.reset();
      if (var1.getPacket() instanceof ClientboundPlayerPositionPacket) {
         this.setbackTimer.reset();
      }

      if (var1.getPacket() instanceof ClientboundSetTimePacket) {
         float[] var2 = this.tpsSamples;
         int var3 = this.sampleIndex;
         long var4 = System.currentTimeMillis();
         long var6 = this.lastTimePacketMs;
         float var8 = (float)(var4 - var6) / 1000.0F;
         float var9 = Math.clamp(20.0F / var8, 0.0F, 20.0F);
         var2[var3] = var9;
         int var10 = this.sampleIndex + 1;
         int var11 = this.tpsSamples.length;
         int var12 = var10 % var11;
         this.sampleIndex = var12;
         long var13 = System.currentTimeMillis();
         this.lastTimePacketMs = var13;
      }
   }

   @EventTarget
   public void a(ClientConnectEvent var1) {
      Arrays.fill(this.tpsSamples, 0.0F);
      this.sampleIndex = 0;
      long var2 = System.currentTimeMillis();
      this.connectTimeMs = var2;
      long var4 = System.currentTimeMillis();
      this.lastTimePacketMs = var4;
   }

   @EventTarget
   public void a(ServerConnectEvent var1) {
      ServerAddress var2 = var1.getAddress();
      ServerData var3 = var1.getInfo();
      this.lastServerConnection = new ObjectObjectImmutablePair(var2, var3);
   }

   public float getTps() {
      if (mc.player == null) {
         return 0.0F;
      }

      long var1 = System.currentTimeMillis();
      long var3 = this.connectTimeMs;
      long var5 = var1 - var3;
      long var7 = 4000L;
      if (var5 < var7) {
         return 20.0F;
      }

      int var9 = 0;
      float var10 = 0.0F;
      float[] var11 = this.tpsSamples;
      int var12 = var11.length;
      int var13 = 0;

      while (true) {
         int var14 = var13;
         int var15 = var12;
         if (var14 >= var15) {
            float var21 = var10;
            float var22 = var9;
            return var21 / var22;
         }

         float[] var16 = var11;
         int var17 = var13;
         float var18 = var16[var17];
         if (var18 > 0.0F) {
            float var19 = var10;
            float var20 = var18;
            var10 = var19 + var20;
            var9++;
         }

         var13++;
      }
   }

   public int getPingTicks() {
      return (int)(this.getPing() / 25.0F);
   }

   public int getPing() {
      if (Night.moduleManager.getModule(FastLatency.class).isEnabled()) {
         return Night.moduleManager.getModule(FastLatency.class).v();
      }

      if (mc.getConnection() != null && mc.player != null) {
         ClientPacketListener var1 = mc.getConnection();
         UUID var2 = mc.player.getUUID();
         PlayerInfo var3 = var1.getPlayerInfo(var2);
         if (var3 != null) {
            return var3.getLatency();
         }

         if (PingBypassState.d) {
            for (PlayerInfo var5 : (Iterable<PlayerInfo>)(Object)mc.getConnection().getOnlinePlayers()) {
               String var6 = var5.getProfile().name();
               String var7 = mc.player.getName().getString();
               if (var6.equals(var7)) {
                  return var5.getLatency();
               }
            }

            for (PlayerInfo var9 : (Iterable<PlayerInfo>)(Object)mc.getConnection().getOnlinePlayers()) {
               if (var9.getLatency() > 0) {
                  return var9.getLatency();
               }
            }
         }

         return 0;
      } else {
         return 0;
      }
   }

   public String getServerBrand() {
      return mc.getCurrentServer() != null && mc.getConnection() != null && mc.getConnection().serverBrand() != null
         ? mc.getConnection().serverBrand()
         : "Vanilla";
   }

   public String getServerHost() {
      String var1;
      if (mc.hasSingleplayerServer()) {
         var1 = "Singleplayer";
      } else {
         var1 = ServerAddress.parseString(mc.getCurrentServer().ip).getHost();
      }

      return var1;
   }

   @Generated
   public TimerUtil getSetbackTimer() {
      return this.setbackTimer;
   }

   @Generated
   public TimerUtil getPacketTimer() {
      return this.packetTimer;
   }

   @Generated
   public Pair<ServerAddress, ServerData> getLastServerConnection() {
      return this.lastServerConnection;
   }
}
