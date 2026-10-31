package night.module.impl.player;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.item.Item;
import night.module.Module;
import night.module.ModuleInfo;
import night.setting.impl.BooleanSetting;
import night.setting.impl.WhitelistSetting;

@ModuleInfo(
   name = "NoEntityTrace",
   description = "Allows you to interact with blocks, bypassing the entities between you and the block.",
   category = Module.Category.PLAYER
)
public class NoEntityTrace extends Module {
   public BooleanSetting pickaxeOnly = new BooleanSetting("PickaxeOnly", "Only bypasses entities if you're holding a pickaxe.", false);
   public BooleanSetting angryMobs = new BooleanSetting(
      "AngryMobs", "Only bypasses non-living entities (Crystals, Boats, Minecarts) and neutral/angerable mobs (Endermen, Piglins, Golems, etc.).", false
   );
   public WhitelistSetting ignoredItem = new WhitelistSetting(
      "IgnoredItem", "Do not bypasses entities if you're holding these items.", WhitelistSetting.WhitelistType.ITEMS
   );

   public boolean v() {
      return this.a(null);
   }

   public boolean a(Entity var1) {
      if (mc.player == null) {
         return false;
      }

      if (this.pickaxeOnly.getValue() && !mc.player.getMainHandItem().is(ItemTags.PICKAXES)) {
         return false;
      }

      WhitelistSetting var2 = this.ignoredItem;
      Item var3 = mc.player.getMainHandItem().getItem();
      if (var2.contains(var3)) {
         return false;
      }

      if (this.angryMobs.getValue() && var1 != null) {
         boolean var4;
         if (!(var1 instanceof EndCrystal)
            && !(var1 instanceof VehicleEntity)
            && !(var1 instanceof ItemFrame)
            && !(var1 instanceof ArmorStand)
            && var1 instanceof LivingEntity) {
            var4 = false;
         } else {
            var4 = true;
         }

         boolean var5 = var4;
         boolean var6 = var1 instanceof NeutralMob;
         boolean var7;
         if (!var5 && !var6) {
            var7 = false;
         } else {
            var7 = true;
         }

         return var7;
      } else {
         return true;
      }
   }
}
