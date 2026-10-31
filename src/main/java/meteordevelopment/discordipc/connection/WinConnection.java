package meteordevelopment.discordipc.connection;

import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.function.Consumer;
import meteordevelopment.discordipc.Opcode;
import meteordevelopment.discordipc.Packet;

public class WinConnection extends Connection {
   private final RandomAccessFile raf;
   private final Consumer<Packet> callback;

   WinConnection(String var1, Consumer<Packet> var2) throws IOException {
      this.raf = new RandomAccessFile(var1, "rw");
      this.callback = var2;
      Thread var3 = new Thread(this::run);
      var3.setName("Discord IPC - Read thread");
      var3.start();
   }

   @Override
   protected void write(ByteBuffer var1) {
      try {
         this.raf.write(var1.array());
      } catch (IOException var2) {
         var2.printStackTrace();
      }
   }

   private void run() {
      ByteBuffer var1 = ByteBuffer.allocate(4);

      try {
         while (true) {
            this.readFully(var1);
            Opcode var2 = Opcode.valueOf(Integer.reverseBytes(var1.getInt(0)));
            this.readFully(var1);
            ByteBuffer var3 = ByteBuffer.allocate(Integer.reverseBytes(var1.getInt(0)));
            this.readFully(var3);
            String var4 = Charset.defaultCharset().decode(var3.rewind()).toString();
            this.callback.accept(new Packet(var2, JsonParser.parseString(var4).getAsJsonObject()));
         }
      } catch (Exception var5) {
      }
   }

   private void readFully(ByteBuffer var1) throws IOException {
      var1.rewind();

      while (this.raf.length() < var1.remaining()) {
         Thread.onSpinWait();

         try {
            Thread.sleep(100L);
         } catch (InterruptedException var2) {
            var2.printStackTrace();
         }
      }

      while (var1.hasRemaining()) {
         this.raf.getChannel().read(var1);
      }
   }

   @Override
   public void close() {
      try {
         this.raf.close();
      } catch (IOException var1) {
         var1.printStackTrace();
      }
   }
}
