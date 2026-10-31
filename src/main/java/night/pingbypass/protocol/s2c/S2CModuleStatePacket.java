package night.pingbypass.protocol.s2c;

import net.minecraft.network.FriendlyByteBuf;
import night.pingbypass.protocol.PingBypassPacket;

public class S2CModuleStatePacket extends PingBypassPacket {
   private final String moduleName;
   private final boolean enabled;

   public S2CModuleStatePacket(String var1, boolean var2) {
      this.moduleName = var1;
      this.enabled = var2;
   }

   public S2CModuleStatePacket(FriendlyByteBuf var1) {
      String var2 = var1.readUtf();
      this.moduleName = var2;
      boolean var3 = var1.readBoolean();
      this.enabled = var3;
   }

   @Override
   public int getPacketId() {
      return 5;
   }

   @Override
   public void write(FriendlyByteBuf var1) {
      String var2 = this.moduleName;
      var1.writeUtf(var2);
      boolean var3 = this.enabled;
      var1.writeBoolean(var3);
   }

   public String getModuleName() {
      return this.moduleName;
   }

   public boolean isEnabled() {
      return this.enabled;
   }
}
