package night.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.TransferState;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import night.Night;
import night.events.impl.ServerConnectEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ConnectScreen.class)
public class ConnectScreenMixin {
   @Inject(method = "startConnecting", at = @At("HEAD"))
   private static void connect(Screen var0, Minecraft var1, ServerAddress var2, ServerData var3, boolean var4, @Nullable TransferState var5, CallbackInfo var6) {
      Night.EVENT_BUS.post(new ServerConnectEvent(var2, var3));
   }
}
