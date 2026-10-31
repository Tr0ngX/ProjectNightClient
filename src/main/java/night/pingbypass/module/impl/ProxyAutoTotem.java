package night.pingbypass.module.impl;

import java.util.List;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import night.Night;
import night.events.EventTarget;
import night.events.impl.TotemPopEvent;
import night.module.impl.combat.KillAura;
import night.module.impl.combat.Suicide;
import night.module.impl.player.SpeedMine;
import night.pingbypass.module.ProxyModule;
import night.setting.Setting;
import night.setting.impl.BooleanSetting;
import night.setting.impl.ModeSetting;
import night.setting.impl.NumberSetting;
import night.util.MinecraftInstance;
import night.util.inventory.InventoryUtil;

public class ProxyAutoTotem extends ProxyModule implements MinecraftInstance {
   public ModeSetting item;
   public NumberSetting health;
   public BooleanSetting elytraCheck;
   public NumberSetting fallDistance;
   public BooleanSetting useGapple;
   public BooleanSetting sword;
   public BooleanSetting pickaxe;
   public BooleanSetting totem;
   public BooleanSetting lethalOverride;
   public BooleanSetting s117;
   public BooleanSetting smartMine;
   public BooleanSetting antiMace;
   public NumberSetting maceRange;
   private int n;
   private int o;

   public ProxyAutoTotem() {
      super("AutoTotem");
      String[] var1 = new String[]{"Totem", "Crystal", "Gapple"};
      this.item = new ModeSetting("Item", "The item that will be placed in your offhand slot when safety conditions are met.", "Totem", var1);
      Integer var2 = 16;
      Integer var3 = 0;
      Integer var4 = 36;
      this.health = new NumberSetting("Health", "The health at which a totem will be prioritized.", var2, var3, var4);
      this.elytraCheck = new BooleanSetting("ElytraCheck", "Prioritizes a totem whenever you're wearing an elytra.", true);
      Float var5 = 20.0F;
      Float var6 = 0.0F;
      Float var7 = 80.0F;
      this.fallDistance = new NumberSetting("FallDistance", "The fall distance at which the module will prioritize a totem.", var5, var6, var7);
      this.useGapple = new BooleanSetting("UseGapple", "Switches to a golden apple in your offhand based on main hand items when health is safe.", true);
      this.sword = new BooleanSetting("Sword", "Switches to a gapple when holding a sword or axe in main hand.", true);
      this.pickaxe = new BooleanSetting("Pickaxe", "Switches to a gapple when holding a pickaxe in main hand.", true);
      this.totem = new BooleanSetting("Totem", "Switches to a gapple when holding a totem in main hand.", false);
      this.lethalOverride = new BooleanSetting("LethalOverride", "Overrides any necessity for a totem when gappling.", false);
      this.s117 = new BooleanSetting("1.17+", "1.17+ uses the SWAP inventory action, while having it off uses inventory clicks.", true);
      this.smartMine = new BooleanSetting("SmartMine", "Switches to a crystal whenever you start mining and a totem when you aren't mining.", false);
      this.antiMace = new BooleanSetting("AntiMace", "Switches to a totem if a player near you is trying to smash attack you with a mace.", false);
      Float var8 = 12.0F;
      Float var9 = 0.0F;
      Float var10 = 24.0F;
      this.maceRange = new NumberSetting("MaceRange", "The distance at which an enemy has to be in with a mace in order to swap to a totem.", var8, var9, var10);
      this.n = 0;
      this.o = -1;
   }

   @Override
   public void onEnable() {
      Night.EVENT_BUS.register(this);
   }

   @Override
   public void onDisable() {
      Night.EVENT_BUS.unregister(this);
      this.o = -1;
   }

   @Override
   public List<Setting> getSettings() {
      Setting[] var1 = new Setting[13];
      ModeSetting var2 = this.item;
      var1[0] = var2;
      NumberSetting var3 = this.health;
      var1[1] = var3;
      BooleanSetting var4 = this.elytraCheck;
      var1[2] = var4;
      NumberSetting var5 = this.fallDistance;
      var1[3] = var5;
      BooleanSetting var6 = this.useGapple;
      var1[4] = var6;
      BooleanSetting var7 = this.sword;
      var1[5] = var7;
      BooleanSetting var8 = this.pickaxe;
      var1[6] = var8;
      BooleanSetting var9 = this.totem;
      var1[7] = var9;
      BooleanSetting var10 = this.lethalOverride;
      var1[8] = var10;
      BooleanSetting var11 = this.s117;
      var1[9] = var11;
      BooleanSetting var12 = this.smartMine;
      var1[10] = var12;
      BooleanSetting var13 = this.antiMace;
      var1[11] = var13;
      NumberSetting var14 = this.maceRange;
      var1[12] = var14;
      return List.of(var1);
   }

   @EventTarget(priority = Integer.MAX_VALUE)
   public void a_void_TotemPopEvent(TotemPopEvent var1) {
      Player var2 = var1.getPlayer();
      LocalPlayer var3 = mc.player;
      if (var2 == var3 && !Night.moduleManager.getModule(Suicide.class).isEnabled()) {
         this.onTick();
      }
   }

   @Override
   public void onTick() {
      if (mc.player != null && mc.level != null) {
         int var1 = mc.player.getInventory().countItem(Items.TOTEM_OF_UNDYING);
         this.n = var1;
         if (mc.gui.screen() instanceof InventoryScreen || !(mc.gui.screen() instanceof AbstractContainerScreen)) {
            Item var2 = this.h();
            if (var2 != null) {
               int var3;
               if (var2 == Items.TOTEM_OF_UNDYING
                  && Night.moduleManager.getModule(Suicide.class).isEnabled()
                  && Night.moduleManager.getModule(Suicide.class).offhandOverride.getValue()) {
                  if (mc.player.getOffhandItem().isEmpty()) {
                     return;
                  }

                  var3 = InventoryUtil.d(InventoryUtil.c, InventoryUtil.f);
               } else {
                  Item var4 = mc.player.getOffhandItem().getItem();
                  Item var5 = var2;
                  if (var4 == var5) {
                     return;
                  }

                  if (this.o != -1 && var2 == Items.TOTEM_OF_UNDYING) {
                     Inventory var6 = mc.player.getInventory();
                     int var7 = this.o;
                     if (var6.getItem(var7).getItem() == Items.TOTEM_OF_UNDYING) {
                        var3 = this.o;
                     } else {
                        var3 = InventoryUtil.c_int_Item(var2);
                        if (var3 == -1) {
                           var3 = InventoryUtil.a_int_Item(var2);
                        }
                     }

                     this.o = -1;
                  } else if (var2 != Items.ENCHANTED_GOLDEN_APPLE && var2 != Items.GOLDEN_APPLE) {
                     var3 = InventoryUtil.c_int_Item(var2);
                     if (var3 == -1) {
                        var3 = InventoryUtil.a_int_Item(var2);
                     }
                  } else {
                     var3 = InventoryUtil.b_int_Item(var2);
                     if (var3 == -1) {
                        var3 = InventoryUtil.c_int_Item(var2);
                     }

                     if (var3 != -1) {
                        int var8 = var3;
                        this.o = var8;
                     }
                  }

                  if (var3 == -1) {
                     if (var2 != Items.TOTEM_OF_UNDYING) {
                        return;
                     }

                     var3 = InventoryUtil.d(InventoryUtil.c, InventoryUtil.f);
                  }
               }

               if (var3 != -1) {
                  if (this.s117.getValue()) {
                     int var9 = var3;
                     InventoryUtil.c("Swap", var9, 40);
                  } else {
                     int var10 = var3;
                     InventoryUtil.c("Pickup", var10, 45);
                  }
               }
            }
         }
      }
   }

   private Item h() {
      boolean var1 = this.a_bool_Item(Items.TOTEM_OF_UNDYING);
      Item var2;
      if (this.a_bool_Item(Items.ENCHANTED_GOLDEN_APPLE)) {
         var2 = Items.ENCHANTED_GOLDEN_APPLE;
      } else if (this.a_bool_Item(Items.GOLDEN_APPLE)) {
         var2 = Items.GOLDEN_APPLE;
      } else {
         var2 = null;
      }

      Item var3 = var2;
      boolean var4;
      if (var3 != null) {
         var4 = true;
      } else {
         var4 = false;
      }

      boolean var5 = var4;
      boolean var6;
      if (!KillAura.b_bool_ItemStack(mc.player.getMainHandItem()) && !KillAura.c_bool_ItemStack(mc.player.getMainHandItem())) {
         var6 = false;
      } else {
         var6 = true;
      }

      boolean var7 = var6;
      boolean var8 = mc.player.getMainHandItem().is(ItemTags.PICKAXES);
      boolean var9 = mc.player.getMainHandItem().is(Items.TOTEM_OF_UNDYING);
      boolean var10;
      if ((!this.sword.getValue() || !var7) && (!this.pickaxe.getValue() || !var8) && (!this.totem.getValue() || !var9)) {
         var10 = false;
      } else {
         var10 = true;
      }

      boolean var11 = var10;
      if (!this.useGapple.getValue() || !mc.options.keyUse.isDown() || !var11 || !var5 || !this.lethalOverride.getValue() && this.i() && var1) {
         if (this.a_bool_Item(Items.TOTEM_OF_UNDYING)) {
            if (this.i()) {
               return Items.TOTEM_OF_UNDYING;
            }

            if (this.item.getValue().equalsIgnoreCase("Crystal") && this.smartMine.getValue()) {
               SpeedMine var12 = Night.moduleManager.getModule(SpeedMine.class);
               if ((var12.v() == null || !var12.v().e()) && (var12.w() == null || !var12.w().c())) {
                  return Items.TOTEM_OF_UNDYING;
               }
            }
         }

         switch (this.item.getValue()) {
            case "Crystal":
               if (!this.a_bool_Item(Items.END_CRYSTAL)) {
                  return Items.TOTEM_OF_UNDYING;
               }

               return Items.END_CRYSTAL;
            case "Gapple":
               if (!var5) {
                  return Items.TOTEM_OF_UNDYING;
               }

               return var3;
            default:
               return Items.TOTEM_OF_UNDYING;
         }
      } else {
         return var3;
      }
   }

   private boolean i() {
      if (Night.moduleManager.getModule(Suicide.class).isEnabled() && Night.moduleManager.getModule(Suicide.class).offhandOverride.getValue()) {
         return false;
      }

      float var1 = mc.player.getHealth();
      float var2 = mc.player.getAbsorptionAmount();
      float var3 = var1 + var2;
      float var4 = this.health.getValue().floatValue();
      if (var3 <= var4) {
         return true;
      }

      double var5 = mc.player.fallDistance;
      double var7 = this.fallDistance.getValue().floatValue();
      if (var5 > var7) {
         return true;
      }

      if (this.elytraCheck.getValue() && mc.player.getItemBySlot(EquipmentSlot.CHEST).getItem() == Items.ELYTRA) {
         return true;
      }

      boolean var9;
      if (this.antiMace.getValue() && mc.level.players().stream().anyMatch(var1x -> {
         LocalPlayer var2x = mc.player;
         if (var1x != var2x) {
            String var3x = var1x.getName().getString();
            if (!Night.friendManager.isFriend(var3x)) {
               double var4x = mc.player.distanceToSqr(var1x);
               double var6 = Mth.square(this.maceRange.getValue().floatValue());
               if (var4x <= var6) {
                  double var8 = var1x.fallDistance;
                  double var10 = 1.5;
                  if (var8 >= var10 && var1x.getMainHandItem().getItem().equals(Items.MACE)) {
                     return true;
                  }
               }
            }
         }

         return false;
      })) {
         var9 = true;
      } else {
         var9 = false;
      }

      return var9;
   }

   private boolean a_bool_Item(Item var1) {
      boolean var2;
      if (InventoryUtil.a_int_Item(var1) == -1 && mc.player.getOffhandItem().getItem() != var1) {
         var2 = false;
      } else {
         var2 = true;
      }

      return var2;
   }

   public String g() {
      return String.valueOf(this.n);
   }
}
