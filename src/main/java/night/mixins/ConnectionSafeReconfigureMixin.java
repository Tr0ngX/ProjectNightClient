package night.mixins;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.PacketBundlePacker;
import net.minecraft.network.PacketBundleUnpacker;
import net.minecraft.network.PacketDecoder;
import net.minecraft.network.PacketEncoder;
import net.minecraft.network.PacketListener;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.protocol.BundlerInfo;
import night.mixins.accessors.ConnectionAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public class ConnectionSafeReconfigureMixin {
   private static final Logger LOGGER = LoggerFactory.getLogger("Night/SafeReconfigure");

   @Inject(method = "setupOutboundProtocol", at = @At("HEAD"), cancellable = true)
   private void setupOutboundProtocol$safe(ProtocolInfo<?> var1, CallbackInfo var2) {
      Connection var3 = (Connection)(Object)(Object)this;
      if (var1.flow() != var3.getSending()) {
         throw new IllegalStateException("Invalid outbound protocol: " + var1.id());
      }

      ConnectionAccessor var4 = (ConnectionAccessor)(Object)this;
      Channel var5 = var4.getChannel();
      var4.setSendLoginDisconnect(var1.id() == ConnectionProtocol.LOGIN);
      Runnable var6 = () -> {
         try {
            ChannelPipeline var2x = var5.pipeline();
            if (var2x.get("fabric:splitter") != null) {
               var2x.remove("fabric:splitter");
            }

            PacketEncoder var3x = new PacketEncoder(var1);
            if (var2x.get("encoder") != null) {
               var2x.replace("encoder", "encoder", var3x);
            } else if (var2x.get("outbound_config") != null) {
               var2x.replace("outbound_config", "encoder", var3x);
            } else {
               var2x.addAfter("prepender", "encoder", var3x);
            }

            BundlerInfo var4x = var1.bundlerInfo();
            if (var4x != null) {
               PacketBundleUnpacker var5x = new PacketBundleUnpacker(var4x);
               if (var2x.get("unbundler") != null) {
                  var2x.replace("unbundler", "unbundler", var5x);
               } else {
                  var2x.addAfter("encoder", "unbundler", var5x);
               }
            }
         } catch (Exception var6x) {
            LOGGER.error("[PB] Failed safe outbound transition to {}", var1.id(), var6x);
         }
      };
      if (var5.eventLoop().inEventLoop()) {
         var6.run();
      } else {
         try {
            var5.eventLoop().submit(var6).sync();
         } catch (InterruptedException var8) {
            Thread.currentThread().interrupt();
         }
      }

      var2.cancel();
   }

   @Inject(method = "setupInboundProtocol", at = @At("HEAD"), cancellable = true)
   private <T extends PacketListener> void setupInboundProtocol$safe(ProtocolInfo<T> var1, T var2, CallbackInfo var3) {
      Connection var4 = (Connection)(Object)(Object)this;
      if (var1.flow() != var4.getReceiving()) {
         throw new IllegalStateException("Invalid inbound protocol: " + var1.id());
      }

      ConnectionAccessor var5 = (ConnectionAccessor)(Object)this;
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
               var2x.replace("decoder", "decoder", var3x);
            } else if (var2x.get("inbound_config") != null) {
               var2x.replace("inbound_config", "decoder", var3x);
            }

            var6.config().setAutoRead(true);
            BundlerInfo var4x = var1.bundlerInfo();
            if (var4x != null) {
               PacketBundlePacker var5x = new PacketBundlePacker(var4x);
               if (var2x.get("bundler") != null) {
                  var2x.replace("bundler", "bundler", var5x);
               } else {
                  var2x.addAfter("decoder", "bundler", var5x);
               }
            }
         } catch (Exception var6x) {
            LOGGER.error("[PB] Failed safe inbound transition to {}", var1.id(), var6x);
         }
      };
      if (var6.eventLoop().inEventLoop()) {
         var7.run();
      } else {
         try {
            var6.eventLoop().submit(var7).sync();
         } catch (InterruptedException var9) {
            Thread.currentThread().interrupt();
         }
      }

      var3.cancel();
   }
}
