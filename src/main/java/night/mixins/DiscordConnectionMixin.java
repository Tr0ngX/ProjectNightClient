package night.mixins;

import meteordevelopment.discordipc.connection.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Connection.class)
public class DiscordConnectionMixin {
   @ModifyArg(
      method = "open",
      at = @At(value = "INVOKE", target = "Lmeteordevelopment/discordipc/connection/WinConnection;<init>(Ljava/lang/String;Ljava/util/function/Consumer;)V"),
      index = 0
   )
   private static String night$fixWindowsPipePrefix(String var0) {
      return var0.replace("\\\\?\\pipe\\", "\\\\.\\pipe\\");
   }
}
