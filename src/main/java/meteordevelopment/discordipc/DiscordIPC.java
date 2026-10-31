package meteordevelopment.discordipc;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.lang.management.ManagementFactory;
import java.util.function.BiConsumer;
import meteordevelopment.discordipc.connection.Connection;

public class DiscordIPC {
   private static final Gson GSON = new Gson();
   private static BiConsumer<Integer, String> onError = DiscordIPC::defaultErrorCallback;
   private static Connection c;
   private static Runnable onReady;
   private static boolean receivedDispatch;
   private static JsonObject queuedActivity;
   private static IPCUser user;

   public static void setOnError(BiConsumer<Integer, String> var0) {
      onError = var0;
   }

   public static boolean start(long var0, Runnable var2) {
      c = Connection.open(DiscordIPC::onPacket);
      if (c == null) {
         return false;
      }

      onReady = var2;
      JsonObject var3 = new JsonObject();
      var3.addProperty("v", 1);
      var3.addProperty("client_id", Long.toString(var0));
      c.write(Opcode.Handshake, var3);
      return true;
   }

   public static boolean isConnected() {
      return c != null;
   }

   public static IPCUser getUser() {
      return user;
   }

   public static void setActivity(RichPresence var0) {
      if (c != null) {
         queuedActivity = var0.toJson();
         if (receivedDispatch) {
            sendActivity();
         }
      }
   }

   public static void stop() {
      if (c != null) {
         c.close();
         c = null;
         onReady = null;
         receivedDispatch = false;
         queuedActivity = null;
         user = null;
      }
   }

   private static void sendActivity() {
      JsonObject var0 = new JsonObject();
      var0.addProperty("pid", getPID());
      var0.add("activity", queuedActivity);
      JsonObject var1 = new JsonObject();
      var1.addProperty("cmd", "SET_ACTIVITY");
      var1.add("args", var0);
      c.write(Opcode.Frame, var1);
      queuedActivity = null;
   }

   private static void onPacket(Packet var0) {
      if (var0.opcode() == Opcode.Close) {
         if (onError != null) {
            onError.accept(var0.data().get("code").getAsInt(), var0.data().get("message").getAsString());
         }

         stop();
      } else if (var0.opcode() == Opcode.Frame) {
         if (var0.data().has("evt") && var0.data().get("evt").getAsString().equals("ERROR")) {
            JsonObject var1 = var0.data().getAsJsonObject("data");
            if (onError != null) {
               onError.accept(var1.get("code").getAsInt(), var1.get("message").getAsString());
            }
         } else if (var0.data().has("cmd") && var0.data().get("cmd").getAsString().equals("DISPATCH")) {
            receivedDispatch = true;
            user = (IPCUser)GSON.fromJson(var0.data().getAsJsonObject("data").getAsJsonObject("user"), IPCUser.class);
            if (onReady != null) {
               onReady.run();
            }

            if (queuedActivity != null) {
               sendActivity();
            }
         }
      }
   }

   private static int getPID() {
      String var0 = ManagementFactory.getRuntimeMXBean().getName();
      return Integer.parseInt(var0.substring(0, var0.indexOf(64)));
   }

   private static void defaultErrorCallback(int var0, String var1) {
      System.err.println("Discord IPC error " + var0 + " with message: " + var1);
   }
}
