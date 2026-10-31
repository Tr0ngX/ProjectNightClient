package night.module.access;

import net.minecraft.client.gui.screens.Screen;

public interface AutoEnchantAccess {
   boolean isEnabled();

   boolean isSuppressingGui();

   boolean shouldSuppressScreen(Screen var1);
}
