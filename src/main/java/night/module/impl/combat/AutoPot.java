package night.module.impl.combat;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import night.Night;
import night.events.EventTarget;
import night.events.impl.UpdateWalkingPlayerEvent;
import night.module.Module;
import night.module.ModuleInfo;
import night.setting.impl.BooleanSetting;
import night.setting.impl.ModeSetting;
import night.setting.impl.NumberSetting;
import night.setting.impl.WhitelistSetting;
import night.util.inventory.InventoryUtil;
import night.util.math.TimerUtil;

@ModuleInfo(
   name = "AutoPot",
   description = "Throws a splash potion under/above you to keep whitelisted potions' effects topped up.",
   category = Module.Category.COMBAT
)
public class AutoPot extends Module {
   public ModeSetting mode;
   public WhitelistSetting whitelist;
   public NumberSetting amplifier;
   public BooleanSetting rotate;
   public ModeSetting throwSetting;
   public ModeSetting swap;
   public BooleanSetting noTarget;
   public BooleanSetting onlyPhased;
   public BooleanSetting selfToggle;
   public NumberSetting range;
   private final TimerUtil o;
   private static final long p = 5000L;

   public AutoPot() {
      String[] var1 = new String[]{"WhiteList", "BlackList"};
      this.mode = new ModeSetting(
         "Mode", "WhiteList = keep these potions' effects topped up. BlackList = keep every potion's effects topped up except these.", "WhiteList", var1
      );
      this.whitelist = new WhitelistSetting("Whitelist", "Potions this mode's WhiteList/BlackList compares against.", WhitelistSetting.WhitelistType.POTIONS);
      Integer var2 = 1;
      Integer var3 = 0;
      Integer var4 = 4;
      this.amplifier = new NumberSetting("Amplifier", "Minimum amplifier level required before an effect counts as \"already have it\".", var2, var3, var4);
      this.rotate = new BooleanSetting(
         "Rotate", "Rotates to look straight up/down before throwing (nami: real rotation, waits for it to land before throwing).", false
      );
      BooleanSetting var6 = this.rotate;
      BooleanSetting.BooleanCondition var5 = new BooleanSetting.BooleanCondition(var6, true);
      String[] var7 = new String[]{"Above", "Under"};
      this.throwSetting = new ModeSetting("Throw", "Where the potion lands relative to you.", var5, "Under", var7);
      String[] var8 = new String[]{"Normal", "Silent"};
      this.swap = new ModeSetting("Swap", "How the potion slot gets selected before throwing.", "Silent", var8);
      this.noTarget = new BooleanSetting("NoTarget", "Only throws while no enemy is nearby.", false);
      this.onlyPhased = new BooleanSetting("OnlyPhased", "Only throws while phased into a block.", false);
      this.selfToggle = new BooleanSetting("SelfToggle", "Turns the module off once every whitelisted effect is already topped up.", true);
      BooleanSetting var10 = this.noTarget;
      BooleanSetting.BooleanCondition var9 = new BooleanSetting.BooleanCondition(var10, true);
      Integer var11 = 32;
      Integer var12 = 4;
      Integer var13 = 64;
      this.range = new NumberSetting("Range", "How close an enemy has to be for NoTarget to see them.", var9, var11, var12, var13);
      this.o = new TimerUtil();
   }

   @EventTarget
   public void a_void_UpdateWalkingPlayerEvent(UpdateWalkingPlayerEvent var1) {
      if (mc.player != null && mc.level != null) {
         Potion var2 = this.v();
         if (var2 == null) {
            if (this.selfToggle.getValue()) {
               this.setEnabled(false);
            }
         } else if (this.noTarget.getValue() && this.w()) {
            if (this.selfToggle.getValue()) {
               this.setEnabled(false);
            }
         } else if (this.onlyPhased.getValue() && !this.x()) {
            if (this.selfToggle.getValue()) {
               this.setEnabled(false);
            }
         } else {
            Potion var3 = var2;
            int var4 = this.a_int_Potion(var3);
            if (var4 == -1) {
               if (this.selfToggle.getValue()) {
                  this.setEnabled(false);
               }
            } else {
               TimerUtil var5 = this.o;
               Long var6 = 5000L;
               if (var5.passedMs(var6)) {
                  int var7;
                  if (var4 < 9) {
                     var7 = var4;
                  } else {
                     var7 = -1;
                  }

                  int var8 = (byte)var7;
                  int var9 = mc.player.getInventory().getSelectedSlot();
                  if (var8 == -1) {
                     int var10 = var4;
                     InventoryUtil.c("Pickup", var10, InventoryUtil.c);
                     var8 = InventoryUtil.c;
                  }

                  float var11;
                  if (this.throwSetting.getValue().equalsIgnoreCase("Above")) {
                     var11 = -90.0F;
                  } else {
                     var11 = 90.0F;
                  }

                  float var12 = var11;
                  if (this.rotate.getValue()) {
                     float[] var13 = new float[2];
                     float var14 = mc.player.getYRot();
                     var13[0] = var14;
                     float var15 = var12;
                     var13[1] = var15;
                     float[] var16 = var13;
                     int var17 = Night.rotationManager.getModulePriority(this);
                     Night.rotationManager.rotate(var16, this, var17);
                  }

                  this.o.reset();
                  String var18 = this.swap.getValue();
                  int var19 = var8;
                  int var20 = var9;
                  InventoryUtil.a_bool_String_int_int(var18, var19, var20);
                  MultiPlayerGameMode var21 = mc.gameMode;
                  LocalPlayer var22 = mc.player;
                  var21.useItem(var22, InteractionHand.MAIN_HAND);
                  String var23 = this.swap.getValue();
                  int var24 = var8;
                  int var25 = var9;
                  InventoryUtil.b(var23, var24, var25);
               }
            }
         }
      }
   }

   private Potion v() {
      int var1 = this.amplifier.getValue().intValue();

      for (Potion var3 : (Iterable<Potion>)(Object)this.whitelist.getPotions()) {
         boolean var4 = false;

         for (MobEffectInstance var6 : (Iterable<MobEffectInstance>)(Object)var3.getEffects()) {
            boolean var7 = false;

            for (MobEffectInstance var9 : (Iterable<MobEffectInstance>)(Object)mc.player.getActiveEffects()) {
               Holder var10 = var9.getEffect();
               Holder var11 = var6.getEffect();
               if (var10 == var11) {
                  int var12 = var9.getAmplifier();
                  int var13 = var1;
                  if (var12 >= var13) {
                     var7 = true;
                     break;
                  }
               }
            }

            if (!var7) {
               var4 = true;
               break;
            }
         }

         if (var4) {
            return var3;
         }
      }

      return null;
   }

   private int a_int_Potion(Potion var1) {
      for (int var2 = 0; var2 < 36; var2++) {
         Inventory var3 = mc.player.getInventory();
         int var4 = var2;
         ItemStack var5 = var3.getItem(var4);
         if (!var5.isEmpty() && var5.getItem() == Items.SPLASH_POTION) {
            PotionContents var6 = (PotionContents)var5.get(DataComponents.POTION_CONTENTS);
            if (var6 != null && !var6.potion().isEmpty() && ((Holder)var6.potion().get()).value() == var1) {
               return var2;
            }
         }
      }

      return -1;
   }

   private boolean w() {
      double var1 = Mth.square(this.range.getValue().doubleValue());

      for (Player var4 : (Iterable<Player>)(Object)mc.level.players()) {
         LocalPlayer var5 = mc.player;
         if (var4 != var5) {
            String var6 = var4.getName().getString();
            if (!Night.friendManager.isFriend(var6)) {
               LocalPlayer var7 = mc.player;
               Player var8 = var4;
               double var9 = var7.distanceToSqr(var8);
               double var11 = var1;
               if (var9 <= var11) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   private boolean x() {
      AABB var1 = mc.player.getBoundingBox();
      int var2 = Mth.floor(var1.minX);
      int var3 = Mth.ceil(var1.maxX);
      int var4 = Mth.floor(var1.minY);
      int var5 = Mth.ceil(var1.maxY);
      int var6 = Mth.floor(var1.minZ);
      int var7 = Mth.ceil(var1.maxZ);
      int var8 = var2;

      while (true) {
         int var9 = var8;
         int var10 = var3;
         if (var9 >= var10) {
            return false;
         }

         int var11 = var4;

         while (true) {
            int var12 = var11;
            int var13 = var5;
            if (var12 >= var13) {
               var8++;
               break;
            }

            int var14 = var6;

            while (true) {
               int var15 = var14;
               int var16 = var7;
               if (var15 >= var16) {
                  var11++;
                  break;
               }

               int var17 = var8;
               int var18 = var11;
               int var19 = var14;
               BlockPos var20 = new BlockPos(var17, var18, var19);
               ClientLevel var21 = mc.level;
               BlockPos var22 = var20;
               BlockState var23 = var21.getBlockState(var22);
               ClientLevel var24 = mc.level;
               BlockPos var25 = var20;
               VoxelShape var26 = var23.getCollisionShape(var24, var25);
               if (!var26.isEmpty()) {
                  AABB var27 = var26.bounds();
                  BlockPos var28 = var20;
                  AABB var29 = var27.move(var28);
                  AABB var30 = var1;
                  if (var29.intersects(var30)) {
                     return true;
                  }
               }

               var14++;
            }
         }
      }
   }
}
