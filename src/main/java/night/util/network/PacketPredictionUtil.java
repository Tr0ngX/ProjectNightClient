package night.util.network;

import java.lang.reflect.Method;
import java.util.function.Consumer;
import net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import night.mixins.accessors.ClientWorldAccessor;
import night.util.MinecraftInstance;

public class PacketPredictionUtil implements MinecraftInstance {
   private static Object a;
   private static Method b;
   private static Method c;
   private static boolean d;

   public static void a_void_SequencedPacketCreator(PacketPredictionUtil.SequencedPacketCreator var0) {
      a(var0, var0x -> mc.getConnection().send(var0x));
   }

   public static void a(PacketPredictionUtil.SequencedPacketCreator var0, Consumer<Packet<ServerGamePacketListener>> var1) {
      BlockStatePredictionHandler var2 = ((ClientWorldAccessor)mc.level).invokeGetPendingUpdateManager().startPredicting();

      try {
         Packet var3 = var0.predict(var2.currentSequence());
         var1.accept(var3);
      } catch (Throwable var6) {
         if (var2 != null) {
            try {
               var2.close();
            } catch (Throwable var5) {
               var6.addSuppressed(var5);
            }
         }

         throw var6;
      }

      if (var2 != null) {
         var2.close();
      }
   }

   public static int a() {
      if (d) {
         return -1;
      }

      try {
         if (a == null) {
            a = Class.forName("com.viaversion.viafabricplus.ViaFabricPlus").getMethod("getImpl").invoke(null);
            b = Class.forName("com.viaversion.viafabricplus.api.ViaFabricPlusBase").getMethod("getTargetVersion");
            c = Class.forName("com.viaversion.viaversion.api.protocol.version.ProtocolVersion").getMethod("getVersion");
         }

         Object var0 = b.invoke(a);
         return var0 == null ? -1 : (Integer)c.invoke(var0);
      } catch (ClassNotFoundException | NoSuchMethodException var1) {
         d = true;
         return -1;
      } catch (Throwable var2) {
         return -1;
      }
   }

   public static boolean b() {
      int var0 = a();
      return var0 != -1 && var0 < 768;
   }

   public static boolean c() {
      int var0 = a();
      return var0 >= 755 && var0 <= 766;
   }

   public interface SequencedPacketCreator {
      Packet<ServerGamePacketListener> predict(int var1);
   }
}
