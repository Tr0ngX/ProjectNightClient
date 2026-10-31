package night.pingbypass.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.Identifier;

public record PingBypassPayload(byte[] data) implements CustomPacketPayload {
   public static final Identifier CHANNEL_ID = Identifier.fromNamespaceAndPath("night", "pingbypass");
   public static final Type<PingBypassPayload> TYPE = new Type(CHANNEL_ID);
   public static final StreamCodec<RegistryFriendlyByteBuf, PingBypassPayload> CODEC = new StreamCodec<RegistryFriendlyByteBuf, PingBypassPayload>() {
      public PingBypassPayload decode(RegistryFriendlyByteBuf var1) {
         byte[] var2 = new byte[var1.readableBytes()];
         var1.readBytes(var2);
         byte[] var3 = var2;
         return new PingBypassPayload(var3);
      }

      public void encode(RegistryFriendlyByteBuf var1, PingBypassPayload var2) {
         byte[] var3 = var2.data();
         var1.writeBytes(var3);
      }
   };

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public FriendlyByteBuf toBuffer() {
      ByteBuf var1 = Unpooled.wrappedBuffer(this.data);
      return new FriendlyByteBuf(var1);
   }

   public static PingBypassPayload fromPacket(PingBypassPacket var0) {
      ByteBuf var1 = Unpooled.buffer();
      FriendlyByteBuf var2 = new FriendlyByteBuf(var1);
      int var3 = var0.getPacketId();
      var2.writeVarInt(var3);
      FriendlyByteBuf var4 = var2;
      var0.write(var4);
      byte[] var5 = new byte[var2.readableBytes()];
      FriendlyByteBuf var6 = var2;
      byte[] var7 = var5;
      var6.readBytes(var7);
      var2.release();
      byte[] var8 = var5;
      return new PingBypassPayload(var8);
   }

   public static PingBypassPayload empty() {
      ByteBuf var0 = Unpooled.buffer();
      FriendlyByteBuf var1 = new FriendlyByteBuf(var0);
      var1.writeVarInt(1);
      byte[] var2 = new byte[var1.readableBytes()];
      FriendlyByteBuf var3 = var1;
      byte[] var4 = var2;
      var3.readBytes(var4);
      var1.release();
      byte[] var5 = var2;
      return new PingBypassPayload(var5);
   }
}
