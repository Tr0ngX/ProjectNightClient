package night.pingbypass.network;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoop;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.PacketBundlePacker;
import net.minecraft.network.PacketBundleUnpacker;
import net.minecraft.network.PacketDecoder;
import net.minecraft.network.PacketEncoder;
import net.minecraft.network.PacketListener;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.protocol.BundlerInfo;
import net.minecraft.network.protocol.PacketFlow;
import night.mixins.accessors.ConnectionAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PingBypassSessionConnection extends Connection {
   private static final Logger a = LoggerFactory.getLogger(PingBypassSessionConnection.class);

   public PingBypassSessionConnection() {
      super(PacketFlow.SERVERBOUND);
   }

   public <T extends PacketListener> void setupInboundProtocol(ProtocolInfo<T> var1, T var2) {
      PacketFlow var3 = var1.flow();
      PacketFlow var4 = this.getReceiving();
      if (var3 != var4) {
         throw new IllegalStateException("Invalid inbound protocol: " + var1.id());
      }

      ConnectionAccessor var5 = (ConnectionAccessor)this;
      var5.setPacketListener(var2);
      Channel var6 = var5.getChannel();
      Runnable var7 = () -> {
         try {
            ChannelPipeline var2x = var6.pipeline();
            if (var2x.get("fabric:merger") != null) {
               var2x.remove("fabric:merger");
            }

            PacketDecoder var3x = new PacketDecoder(var1);
            if (var2x.get("decoder") != null) {
               ChannelPipeline var4x = var2x;
               PacketDecoder var5x = var3x;
               var4x.replace("decoder", "decoder", var5x);
            } else if (var2x.get("inbound_config") != null) {
               ChannelPipeline var6x = var2x;
               PacketDecoder var7x = var3x;
               var6x.replace("inbound_config", "decoder", var7x);
            }

            var6.config().setAutoRead(true);
            BundlerInfo var8x = var1.bundlerInfo();
            if (var8x != null) {
               BundlerInfo var9x = var8x;
               PacketBundlePacker var10 = new PacketBundlePacker(var9x);
               if (var2x.get("bundler") != null) {
                  ChannelPipeline var11x = var2x;
                  PacketBundlePacker var12 = var10;
                  var11x.replace("bundler", "bundler", var12);
               } else {
                  ChannelPipeline var13 = var2x;
                  PacketBundlePacker var14 = var10;
                  var13.addAfter("decoder", "bundler", var14);
               }
            }
         } catch (Exception var18) {
            ConnectionProtocol var16 = var1.id();
            a.error("[PbSession] Failed inbound transition to {}", var16, var18);
         }
      };
      if (var6.eventLoop().inEventLoop()) {
         var7.run();
      } else {
         try {
            EventLoop var8 = var6.eventLoop();
            Runnable var9 = var7;
            var8.submit(var9).sync();
         } catch (InterruptedException var11) {
            Thread.currentThread().interrupt();
         }
      }
   }

   public void setupOutboundProtocol(ProtocolInfo<?> var1) {
      PacketFlow var2 = var1.flow();
      PacketFlow var3 = this.getSending();
      if (var2 != var3) {
         throw new IllegalStateException("Invalid outbound protocol: " + var1.id());
      }

      ConnectionAccessor var4 = (ConnectionAccessor)this;
      Channel var5 = var4.getChannel();
      ConnectionAccessor var6 = var4;
      boolean var7;
      if (var1.id() == ConnectionProtocol.LOGIN) {
         var7 = true;
      } else {
         var7 = false;
      }

      var6.setSendLoginDisconnect(var7);
      Runnable var8 = () -> {
         try {
            ChannelPipeline var2x = var5.pipeline();
            if (var2x.get("fabric:splitter") != null) {
               var2x.remove("fabric:splitter");
            }

            PacketEncoder var3x = new PacketEncoder(var1);
            if (var2x.get("encoder") != null) {
               ChannelPipeline var4x = var2x;
               PacketEncoder var5x = var3x;
               var4x.replace("encoder", "encoder", var5x);
            } else if (var2x.get("outbound_config") != null) {
               ChannelPipeline var6x = var2x;
               PacketEncoder var7x = var3x;
               var6x.replace("outbound_config", "encoder", var7x);
            } else {
               ChannelPipeline var8x = var2x;
               PacketEncoder var9x = var3x;
               var8x.addAfter("prepender", "encoder", var9x);
            }

            BundlerInfo var10x = var1.bundlerInfo();
            if (var10x != null) {
               BundlerInfo var11 = var10x;
               PacketBundleUnpacker var12x = new PacketBundleUnpacker(var11);
               if (var2x.get("unbundler") != null) {
                  ChannelPipeline var13 = var2x;
                  PacketBundleUnpacker var14 = var12x;
                  var13.replace("unbundler", "unbundler", var14);
               } else {
                  ChannelPipeline var15 = var2x;
                  PacketBundleUnpacker var16 = var12x;
                  var15.addAfter("encoder", "unbundler", var16);
               }
            }
         } catch (Exception var20) {
            ConnectionProtocol var18 = var1.id();
            a.error("[PbSession] Failed outbound transition to {}", var18, var20);
         }
      };
      if (var5.eventLoop().inEventLoop()) {
         var8.run();
      } else {
         try {
            EventLoop var9 = var5.eventLoop();
            Runnable var10 = var8;
            var9.submit(var10).sync();
         } catch (InterruptedException var12) {
            Thread.currentThread().interrupt();
         }
      }
   }
}
