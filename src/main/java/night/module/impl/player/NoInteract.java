package night.module.impl.player;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import night.module.Module;
import night.module.ModuleInfo;
import night.setting.impl.BooleanSetting;
import night.setting.impl.ModeSetting;

@ModuleInfo(name = "NoInteract", description = "Prevents you from interacting with right-clickable blocks.", category = Module.Category.PLAYER)
public class NoInteract extends Module {
   public ModeSetting mode;
   public BooleanSetting gapple;

   public NoInteract() {
      String[] var1 = new String[]{"Sneak", "Disable"};
      this.mode = new ModeSetting("Mode", "The way that right-clickable blocks will be ignored.", "Sneak", var1);
      this.gapple = new BooleanSetting("Gapple", "Only disables interactions when holding a golden apple in your main hand.", false);
   }

   @Override
   public String getDisplayInfo() {
      return this.mode.getValue();
   }

   public boolean v() {
      if (!this.gapple.getValue()) {
         return true;
      }

      Item var1 = mc.player.getMainHandItem().getItem();
      boolean var2;
      if (!var1.equals(Items.ENCHANTED_GOLDEN_APPLE) && !var1.equals(Items.GOLDEN_APPLE)) {
         var2 = false;
      } else {
         var2 = true;
      }

      return var2;
   }
}
