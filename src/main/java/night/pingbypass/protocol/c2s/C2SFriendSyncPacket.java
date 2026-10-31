package night.pingbypass.protocol.c2s;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import night.pingbypass.protocol.PingBypassPacket;

public class C2SFriendSyncPacket extends PingBypassPacket {
   private final List<String> friends;

   public C2SFriendSyncPacket(List<String> var1) {
      this.friends = var1;
   }

   public C2SFriendSyncPacket(FriendlyByteBuf var1) {
      int var2 = var1.readVarInt();
      this.friends = new ArrayList<>(var2);
      int var3 = 0;

      while (true) {
         int var4 = var3;
         int var5 = var2;
         if (var4 >= var5) {
            return;
         }

         List var6 = this.friends;
         String var7 = var1.readUtf();
         var6.add(var7);
         var3++;
      }
   }

   @Override
   public int getPacketId() {
      return 14;
   }

   @Override
   public void write(FriendlyByteBuf var1) {
      int var2 = this.friends.size();
      var1.writeVarInt(var2);

      for (String var4 : (Iterable<String>)(Object)this.friends) {
         var1.writeUtf(var4);
      }
   }

   public List<String> getFriends() {
      return this.friends;
   }
}
