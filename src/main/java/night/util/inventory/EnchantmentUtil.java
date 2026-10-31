package night.util.inventory;

import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public class EnchantmentUtil {
   public static int a(ItemStack var0, ResourceKey<Enchantment> var1) {
      if (var0 != null && !var0.isEmpty()) {
         ItemEnchantments var2 = (ItemEnchantments)var0.get(DataComponents.ENCHANTMENTS);
         if (var2 != null && !var2.isEmpty()) {
            for (Entry var4 : (Iterable<Entry>)(Object)var2.entrySet()) {
               Optional var5 = ((Holder)var4.getKey()).unwrapKey();
               if (var5.isPresent() && ((ResourceKey)var5.get()).equals(var1)) {
                  return var4.getIntValue();
               }
            }

            return 0;
         } else {
            return 0;
         }
      } else {
         return 0;
      }
   }
}
