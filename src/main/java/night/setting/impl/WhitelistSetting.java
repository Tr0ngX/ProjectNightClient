package night.setting.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Generated;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.block.Block;
import night.setting.Setting;

public class WhitelistSetting extends Setting {
   private final WhitelistSetting.WhitelistType type;
   private final Set<Object> entries = new HashSet<>();

   public WhitelistSetting(String var1, String var2, WhitelistSetting.WhitelistType var3) {
      super(var1, var1, var2, new Setting.VisibilityCondition());
      this.type = var3;
   }

   public WhitelistSetting(String var1, String var2, String var3, WhitelistSetting.WhitelistType var4) {
      super(var1, var2, var3, new Setting.VisibilityCondition());
      this.type = var4;
   }

   public WhitelistSetting(String var1, String var2, Setting.VisibilityCondition var3, WhitelistSetting.WhitelistType var4) {
      super(var1, var1, var2, var3);
      this.type = var4;
   }

   public WhitelistSetting(String var1, String var2, String var3, Setting.VisibilityCondition var4, WhitelistSetting.WhitelistType var5) {
      super(var1, var2, var3, var4);
      this.type = var5;
   }

   public void add(Object var1) {
      this.entries.add(var1);
   }

   public void remove(Object var1) {
      this.entries.remove(var1);
   }

   public boolean contains(Object var1) {
      return this.entries.contains(var1);
   }

   public List<String> getEntryIds() {
      return this.entries.stream().map(var0 -> {
         if (var0 instanceof Item var1) {
            return BuiltInRegistries.ITEM.getKey(var1).toString();
         } else if (var0 instanceof Block var2) {
            return BuiltInRegistries.BLOCK.getKey(var2).toString();
         } else {
            return var0 instanceof Potion var3 ? BuiltInRegistries.POTION.getKey(var3).toString() : null;
         }
      }).toList();
   }

   public List<Potion> getPotions() {
      return this.entries.stream().filter(var0 -> var0 instanceof Potion).map(var0 -> (Potion)var0).toList();
   }

   public void clear() {
      this.entries.clear();
   }

   @Generated
   public WhitelistSetting.WhitelistType getType() {
      return this.type;
   }

   @Generated
   public Set<Object> getEntries() {
      return this.entries;
   }

   public enum WhitelistType {
      ITEMS,
      BLOCKS,
      POTIONS;
   }
}
