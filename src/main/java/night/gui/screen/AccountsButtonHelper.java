package night.gui.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class AccountsButtonHelper {
   public static Button a(int var0, int var1, int var2, int var3, Screen var4) {
      return Button.builder(Component.literal("Accounts"), var1x -> {
         if (Minecraft.getInstance() != null) {
            Minecraft.getInstance().gui.setScreen(new AccountManagerScreen(var4));
         }
      }).bounds(var0, var1, var2, var3).build();
   }
}
