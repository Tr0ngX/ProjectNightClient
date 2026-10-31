package meteordevelopment.discordipc.connection;

import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.function.Consumer;
import meteordevelopment.discordipc.Opcode;
import meteordevelopment.discordipc.Packet;

public class UnixConnection extends Connection {
   private final Selector s;
   private final SocketChannel sc;
   private final Consumer<Packet> callback;

   public UnixConnection(String var1, Consumer<Packet> var2) throws IOException {
      this.s = Selector.open();
      this.sc = SocketChannel.open(UnixDomainSocketAddress.of(var1));
      this.callback = var2;
      this.sc.configureBlocking(false);
      this.sc.register(this.s, 1);
      Thread var3 = new Thread(this::run);
      var3.setName("Discord IPC - Read thread");
      var3.start();
   }

   private void run() {
      UnixConnection.State var1 = UnixConnection.State.Opcode;
      ByteBuffer var2 = ByteBuffer.allocate(4);
      ByteBuffer var3 = null;
      Opcode var4 = null;

      try {
         while (true) {
            this.s.select();
            switch (var1) {
               case Opcode:
                  this.sc.read(var2);
                  if (!var2.hasRemaining()) {
                     var4 = Opcode.valueOf(Integer.reverseBytes(var2.getInt(0)));
                     var1 = UnixConnection.State.Length;
                     var2.rewind();
                  }
                  break;
               case Length:
                  this.sc.read(var2);
                  if (!var2.hasRemaining()) {
                     var3 = ByteBuffer.allocate(Integer.reverseBytes(var2.getInt(0)));
                     var1 = UnixConnection.State.Data;
                     var2.rewind();
                  }
                  break;
               case Data:
                  this.sc.read(var3);
                  if (!var3.hasRemaining()) {
                     String var5 = Charset.defaultCharset().decode(var3.rewind()).toString();
                     this.callback.accept(new Packet(var4, JsonParser.parseString(var5).getAsJsonObject()));
                     var3 = null;
                     var1 = UnixConnection.State.Opcode;
                  }
            }
         }
      } catch (Exception var6) {
      }
   }

   @Override
   protected void write(ByteBuffer var1) {
      try {
         this.sc.write(var1);
      } catch (IOException var2) {
         var2.printStackTrace();
      }
   }

   @Override
   public void close() {
      try {
         this.s.close();
         this.sc.close();
      } catch (IOException var1) {
         var1.printStackTrace();
      }
   }

   enum State {
      Opcode,
      Length,
      Data;
   }
}
