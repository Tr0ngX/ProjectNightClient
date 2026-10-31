package meteordevelopment.discordipc.connection;

import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.function.Consumer;
import meteordevelopment.discordipc.Opcode;
import meteordevelopment.discordipc.Packet;

public abstract class Connection {
   private static final String[] UNIX_TEMP_PATHS = new String[]{"XDG_RUNTIME_DIR", "TMPDIR", "TMP", "TEMP"};

   public static Connection open(Consumer<Packet> var0) {
      if (System.getProperty("os.name").toLowerCase().contains("win")) {
         for (int var8 = 0; var8 < 10; var8++) {
            try {
               return new WinConnection("\\\\?\\pipe\\discord-ipc-" + var8, var0);
            } catch (Exception var5) {
            }
         }
      } else {
         String var1 = null;
         String[] var2 = UNIX_TEMP_PATHS;
         int var3 = var2.length;

         for (int var4 = 0; var4 < var3; var4++) {
            var1 = System.getenv(var2[var4]);
            if (var1 != null) {
               break;
            }
         }

         if (var1 == null) {
            var1 = "/tmp";
         }

         var1 = var1 + "/discord-ipc-";

         for (int var9 = 0; var9 < 10; var9++) {
            try {
               return new UnixConnection(var1 + var9, var0);
            } catch (Exception var6) {
            }
         }
      }

      return null;
   }

   public void write(Opcode var1, JsonObject var2) {
      var2.addProperty("nonce", UUID.randomUUID().toString());
      byte[] var3 = var2.toString().getBytes();
      ByteBuffer var4 = ByteBuffer.allocate(var3.length + 8);
      var4.putInt(Integer.reverseBytes(var1.ordinal()));
      var4.putInt(Integer.reverseBytes(var3.length));
      var4.put(var3);
      var4.rewind();
      this.write(var4);
   }

   protected abstract void write(ByteBuffer var1);

   public abstract void close();
}
