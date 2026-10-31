package night.module.impl.player;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import night.module.Module;
import night.module.ModuleInfo;
import night.setting.impl.BooleanSetting;
import night.setting.impl.NumberSetting;

@ModuleInfo(name = "FastPlace", description = "Allows you to customize the tick delay between using items.", category = Module.Category.PLAYER)
public class FastPlace extends Module {
   public NumberSetting ticks;
   public BooleanSetting ignoreBlocks;
   public BooleanSetting ignoreFireworks;
   public BooleanSetting ignorePearls;
   public BooleanSetting ignoreEquipment;

   public FastPlace() {
      Integer var1 = 1;
      Integer var2 = 0;
      Integer var3 = 20;
      this.ticks = new NumberSetting("Ticks", "The amount of ticks that have to be waited for before using items again.", var1, var2, var3);
      this.ignoreBlocks = new BooleanSetting("IgnoreBlocks", "Uses the default Minecraft delay when holding a block item.", true);
      this.ignoreFireworks = new BooleanSetting("IgnoreFireworks", "Uses the default Minecraft delay when holding fireworks.", true);
      this.ignorePearls = new BooleanSetting("IgnorePearls", "Uses the default Minecraft delay when holding pearls.", true);
      this.ignoreEquipment = new BooleanSetting("IgnoreEquipment", "Uses the default Minecraft delay when holding an equipment item.", true);
   }

   @Override
   public String getDisplayInfo() {
      return String.valueOf(this.ticks.getValue().intValue());
   }

   public boolean a(Item var1) {
      if (this.ignoreBlocks.getValue() && var1 instanceof BlockItem) {
         return false;
      }

      if (this.ignoreFireworks.getValue() && var1 == Items.FIREWORK_ROCKET) {
         return false;
      }

      if (this.ignorePearls.getValue() && var1 == Items.ENDER_PEARL) {
         return false;
      }

      boolean var2;
      if (!var1.builtInRegistryHolder().is(ItemTags.FOOT_ARMOR)
         && !var1.builtInRegistryHolder().is(ItemTags.LEG_ARMOR)
         && !var1.builtInRegistryHolder().is(ItemTags.CHEST_ARMOR)
         && !var1.builtInRegistryHolder().is(ItemTags.HEAD_ARMOR)) {
         var2 = false;
      } else {
         var2 = true;
      }

      boolean var3 = var2;
      boolean var4;
      if (this.ignoreEquipment.getValue() && (var3 || var1 == Items.ELYTRA)) {
         var4 = false;
      } else {
         var4 = true;
      }

      return var4;
   }
}
