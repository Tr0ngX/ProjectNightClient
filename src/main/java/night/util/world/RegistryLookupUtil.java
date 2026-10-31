package night.util.world;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.block.Block;

public class RegistryLookupUtil {
   public static Item a(String var0) {
      try {
         Item var1 = a(BuiltInRegistries.ITEM, var0);
         if (var1 != null) {
            return var1;
         }
      } catch (Exception var2) {
      }

      return null;
   }

   public static Block b(String var0) {
      try {
         Block var1 = a(BuiltInRegistries.BLOCK, var0);
         if (var1 != null) {
            return var1;
         }
      } catch (Exception var2) {
      }

      return null;
   }

   public static Potion c(String var0) {
      try {
         Potion var1 = a(BuiltInRegistries.POTION, var0);
         if (var1 != null) {
            return var1;
         }
      } catch (Exception var2) {
      }

      return null;
   }

   public static <T> T a(Registry<T> var0, String var1) {
      if (var1 != null && !var1.trim().isEmpty()) {
         var1 = var1.trim();
         Identifier var2 = Identifier.tryParse(var1.contains(":") ? var1 : "minecraft:" + var1);
         if (var2 == null) {
            return null;
         } else {
            return (T)(var0.containsKey(var2) ? var0.getValue(var2) : null);
         }
      } else {
         return null;
      }
   }
}
