package night.pingbypass.protocol.c2s;

import net.minecraft.network.FriendlyByteBuf;
import night.pingbypass.protocol.PingBypassPacket;

public class C2SSettingUpdatePacket extends PingBypassPacket {
   private final String moduleName;
   private final String settingName;
   private final String value;

   public C2SSettingUpdatePacket(String var1, String var2, String var3) {
      this.moduleName = var1;
      this.settingName = var2;
      this.value = var3;
   }

   public C2SSettingUpdatePacket(FriendlyByteBuf var1) {
      String var2 = var1.readUtf();
      this.moduleName = var2;
      String var3 = var1.readUtf();
      this.settingName = var3;
      String var4 = var1.readUtf();
      this.value = var4;
   }

   @Override
   public int getPacketId() {
      return 4;
   }

   @Override
   public void write(FriendlyByteBuf var1) {
      String var2 = this.moduleName;
      var1.writeUtf(var2);
      String var3 = this.settingName;
      var1.writeUtf(var3);
      String var4 = this.value;
      var1.writeUtf(var4);
   }

   public String getModuleName() {
      return this.moduleName;
   }

   public String getSettingName() {
      return this.settingName;
   }

   public String getValue() {
      return this.value;
   }
}
