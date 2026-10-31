package night.pingbypass.protocol.c2s;

import net.minecraft.network.FriendlyByteBuf;
import night.pingbypass.protocol.PingBypassPacket;

public class C2SModuleTogglePacket extends PingBypassPacket {
   private final String moduleName;
   private final boolean enabled;

   public C2SModuleTogglePacket(String var1, boolean var2) {
      this.moduleName = var1;
      this.enabled = var2;
   }

   public C2SModuleTogglePacket(FriendlyByteBuf var1) {
      String var2 = var1.readUtf();
      this.moduleName = var2;
      boolean var3 = var1.readBoolean();
      this.enabled = var3;
   }

   @Override
   public int getPacketId() {
      return 3;
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
