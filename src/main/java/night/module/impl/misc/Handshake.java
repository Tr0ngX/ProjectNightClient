package night.module.impl.misc;

import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.BrandPayload;
import net.minecraft.resources.Identifier;
import night.events.EventTarget;
import night.events.impl.PacketSendEvent;
import night.mixins.accessors.CustomPayloadC2SPacketAccessor;
import night.module.Module;
import night.module.ModuleInfo;
import night.setting.impl.StringSetting;

@ModuleInfo(
   name = "Handshake",
   description = "Spoofs your client handshake to make the server think that you are playing on a different client.",
   category = Module.Category.MISCELLANEOUS
)
public class Handshake extends Module {
   public StringSetting brand = new StringSetting("Brand", "The brand that the server will think you are playing on.", "vanilla");

   @EventTarget
   public void a(PacketSendEvent var1) {
      if (mc.player != null && mc.level != null) {
         if (var1.getPacket() instanceof ServerboundCustomPayloadPacket var3) {
            Identifier var4 = var3.payload().type().id();
            Identifier var5 = BrandPayload.TYPE.id();
            if (!var4.equals(var5)) {
               return;
            }

            CustomPayloadC2SPacketAccessor var6 = (CustomPayloadC2SPacketAccessor)(Object)var3;
            String var7 = this.brand.getValue();
            var6.setPayload(new BrandPayload(var7));
         }
      }
   }
}
