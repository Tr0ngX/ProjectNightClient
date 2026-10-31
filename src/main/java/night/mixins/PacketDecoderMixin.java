package night.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketDecoder;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import night.Night;
import night.module.impl.misc.NoPacketKick;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PacketDecoder.class)
public class PacketDecoderMixin {
   private static final Logger LOGGER = LoggerFactory.getLogger("Night/PacketDecoder");

   @Inject(
      method = "decode",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/util/profiling/jfr/JvmProfiler;onPacketReceived(Lnet/minecraft/network/ConnectionProtocol;Lnet/minecraft/network/protocol/PacketType;Ljava/net/SocketAddress;I)V",
         shift = Shift.AFTER
      )
   )
   private void skipExtraBytes(ChannelHandlerContext var1, ByteBuf var2, List<Object> var3, CallbackInfo var4, @Local Packet<?> var5, @Local PacketType<?> var6) {
      if (var2.readableBytes() > 0) {
         int var7 = var2.readableBytes();
         String var8 = var5 != null ? var5.getClass().getSimpleName() : (var6 != null ? var6.toString() : "unknown");
         LOGGER.warn("[PacketDecoder] Packet {} had {} extra unread byte(s) -- skipped to prevent disconnect", var8, var7);
         var2.skipBytes(var7);
         NoPacketKick var9 = Night.moduleManager != null ? Night.moduleManager.getModule(NoPacketKick.class) : null;
         if (var9 != null && var9.isEnabled() && var9.logChat.getValue()) {
            Minecraft var10 = Minecraft.getInstance();
            if (var10 != null) {
               var10.execute(() -> {
                  if (Night.chatManager != null && var10.player != null) {
                     Night.chatManager.sendTaggedMessage("Skipped " + var7 + " extra byte(s) on " + var8, "NoPacketKick");
                  }
               });
            }
         }
      }
   }
}
