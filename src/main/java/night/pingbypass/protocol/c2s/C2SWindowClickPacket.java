package night.pingbypass.protocol.c2s;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.ContainerInput;
import night.pingbypass.protocol.PingBypassPacket;

public class C2SWindowClickPacket extends PingBypassPacket {
   private final int containerId;
   private final int slotNum;
   private final int buttonNum;
   private final String clickType;

   public C2SWindowClickPacket(int var1, int var2, int var3, ContainerInput var4) {
      this.containerId = var1;
      this.slotNum = var2;
      this.buttonNum = var3;
      String var5 = var4.name();
      this.clickType = var5;
   }

   public C2SWindowClickPacket(FriendlyByteBuf var1) {
      int var2 = var1.readVarInt();
      this.containerId = var2;
      int var3 = var1.readVarInt();
      this.slotNum = var3;
      int var4 = var1.readVarInt();
      this.buttonNum = var4;
      String var5 = var1.readUtf();
      this.clickType = var5;
   }

   @Override
   public int getPacketId() {
      return 16;
   }

   @Override
   public void write(FriendlyByteBuf var1) {
      int var2 = this.containerId;
      var1.writeVarInt(var2);
      int var3 = this.slotNum;
      var1.writeVarInt(var3);
      int var4 = this.buttonNum;
      var1.writeVarInt(var4);
      String var5 = this.clickType;
      var1.writeUtf(var5);
   }

   public int getContainerId() {
      return this.containerId;
   }

   public int getSlotNum() {
      return this.slotNum;
   }

   public int getButtonNum() {
      return this.buttonNum;
   }

   public ContainerInput getClickType() {
      return ContainerInput.valueOf(this.clickType);
   }
}
