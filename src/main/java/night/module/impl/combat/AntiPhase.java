package night.module.impl.combat;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import night.Night;
import night.events.EventTarget;
import night.events.impl.UpdateWalkingPlayerEvent;
import night.module.Module;
import night.module.ModuleInfo;
import night.setting.impl.BooleanSetting;
import night.setting.impl.ModeSetting;
import night.setting.impl.NumberSetting;
import night.util.inventory.InventoryUtil;
import night.util.world.BlockInteractionUtil;
import night.util.world.EntityUtil;
import night.util.world.HoleUtil;

@ModuleInfo(
   name = "AntiPhase",
   description = "Places a climbable block (Scaffolding, Ladder, Vines) directly at a target enemy's feet.",
   category = Module.Category.COMBAT
)
public class AntiPhase extends Module {
   public ModeSetting block;
   public ModeSetting switchSetting;
   public BooleanSetting rotate;
   public BooleanSetting strictDirection;
   public BooleanSetting render;
   public NumberSetting range;
   public NumberSetting wallRange;
   public BooleanSetting onlyHole;
   public BooleanSetting pauseOnEat;
   public BooleanSetting kingMC;
   private Player o;

   public AntiPhase() {
      String[] var1 = new String[]{"Scaffolding", "Ladder", "Vines", "Rail", "LeafLitter"};
      this.block = new ModeSetting("Block", "The item placed at the enemy's feet.", "Scaffolding", var1);
      this.switchSetting = new ModeSetting("Switch", "The mode that will be used for automatically switching to the selected item.", "Silent", InventoryUtil.a);
      this.rotate = new BooleanSetting("Rotate", "Rotates toward each block before placing it.", true);
      this.strictDirection = new BooleanSetting("StrictDirection", "Only places using directions that face you.", false);
      this.render = new BooleanSetting("Render", "Whether or not to render the place position.", true);
      Float var2 = 5.0F;
      Float var3 = 1.0F;
      Float var4 = 6.0F;
      this.range = new NumberSetting("Range", "The maximum distance at which enemies will be targeted.", var2, var3, var4);
      Float var5 = 3.0F;
      Float var6 = 0.0F;
      Float var7 = 6.0F;
      this.wallRange = new NumberSetting(
         "WallRange", "The maximum distance at which enemies will be targeted through walls (no line of sight required).", var5, var6, var7
      );
      this.onlyHole = new BooleanSetting("OnlyHole", "Only places while the enemy is already standing in a hole (feet surrounded by blocks).", false);
      this.pauseOnEat = new BooleanSetting("PauseOnEat", "Pauses placing while eating or using items.", false);
      this.kingMC = new BooleanSetting("KingMC", "Only places on cells with bedrock directly below them.", false);
   }

   @Override
   public String getDisplayInfo() {
      String var1;
      if (this.o != null) {
         var1 = this.o.getName().getString();
      } else {
         var1 = "None";
      }

      return var1;
   }

   @Override
   public void onDisable() {
      this.o = null;
   }

   @EventTarget
   public void a_void_UpdateWalkingPlayerEvent(UpdateWalkingPlayerEvent var1) {
      if (mc.player != null && mc.level != null) {
         if (!this.pauseOnEat.getValue() || !mc.player.isUsingItem() && mc.player.getUseItemRemainingTicks() <= 0) {
            Player var2 = this.w();
            this.o = var2;
            if (this.o != null) {
               if (!this.onlyHole.getValue() || HoleUtil.a_bool_Player(this.o)) {
                  Item var3 = this.v();
                  int var4 = this.a_int_Item(var3);
                  if (var4 != -1) {
                     Player var5 = this.o;
                     Set var6 = this.b(var5);
                     int var7 = mc.player.getInventory().getSelectedSlot();
                     Night.rotationManager.beginBatch();

                     label74: {
                        try {
                           String var8 = this.switchSetting.getValue();
                           int var9 = var4;
                           int var10 = var7;
                           if (!InventoryUtil.a_bool_String_int_int(var8, var9, var10)) {
                              break label74;
                           }

                           for (BlockPos var12 : (Iterable<BlockPos>)(Object)var6) {
                              Player var13 = this.o;
                              if (this.a_bool_BlockPos_Player(var12, var13)) {
                                 if (this.kingMC.getValue()) {
                                    ClientLevel var14 = mc.level;
                                    BlockPos var15 = var12.below();
                                    if (!var14.getBlockState(var15).is(Blocks.BEDROCK)) {
                                       continue;
                                    }
                                 }

                                 if (var3 instanceof BlockItem var16) {
                                    ClientLevel var17 = mc.level;
                                    BlockPos var18 = var12;
                                    BlockState var19 = var17.getBlockState(var18);
                                    Block var20 = var16.getBlock();
                                    if (var19.is(var20)) {
                                       continue;
                                    }
                                 }

                                 boolean var21 = false;
                                 BlockPos var22 = var12;
                                 List var23 = List.of();
                                 boolean var24 = this.strictDirection.getValue();
                                 Iterator var25 = BlockInteractionUtil.a_List_BlockPos_List_bool(var22, var23, var24).iterator();

                                 while (true) {
                                    if (var25.hasNext()) {
                                       Direction var26 = (Direction)var25.next();
                                       BlockPos var27 = var12;
                                       Direction var28 = var26;
                                       boolean var29 = this.rotate.getValue();
                                       boolean var30 = this.render.getValue();
                                       if (!BlockInteractionUtil.a(var27, var28, InteractionHand.MAIN_HAND, var29, true, var30)) {
                                          continue;
                                       }

                                       var21 = true;
                                    }

                                    if (!var21) {
                                       BlockPos var31 = var12;
                                       boolean var32 = this.render.getValue();
                                       BlockInteractionUtil.a_void_BlockPos_InteractionHand_String_bool(var31, InteractionHand.MAIN_HAND, "None", var32);
                                    }
                                    break;
                                 }
                              }
                           }

                           String var33 = this.switchSetting.getValue();
                           int var34 = var4;
                           int var35 = var7;
                           InventoryUtil.b(var33, var34, var35);
                        } catch (Throwable var37) {
                           Night.rotationManager.endBatch();
                           throw var37;
                        }

                        Night.rotationManager.endBatch();
                        return;
                     }

                     Night.rotationManager.endBatch();
                  }
               }
            }
         }
      }
   }

   private Item v() {
      return switch (this.block.getValue()) {
         case "Ladder" -> Items.LADDER;
         case "Vines" -> Items.VINE;
         case "Rail" -> Items.RAIL;
         case "LeafLitter" -> Items.LEAF_LITTER;
         default -> Items.SCAFFOLDING;
      };
   }

   private int a_int_Item(Item var1) {
      for (int var2 = 0; var2 <= 8; var2++) {
         Inventory var3 = mc.player.getInventory();
         int var4 = var2;
         if (var3.getItem(var4).is(var1)) {
            return var2;
         }
      }

      return -1;
   }

   private Player w() {
      return mc.level.players().stream().filter(this::a_bool_Player).min(Comparator.comparingDouble(var0 -> mc.player.distanceToSqr(var0))).orElse(null);
   }

   private boolean a_bool_Player(Player var1) {
      LocalPlayer var2 = mc.player;
      if (var1 == var2 || !var1.isAlive() || var1.isRemoved() || var1.isSpectator()) {
         return false;
      }

      if (EntityUtil.c_bool_Entity(var1)) {
         return false;
      }

      String var3 = var1.getName().getString();
      if (Night.friendManager.isFriend(var3)) {
         return false;
      }

      double var4 = mc.player.distanceToSqr(var1);
      double var6 = Mth.square(this.range.getValue().doubleValue());
      if (var4 > var6) {
         return false;
      }

      double var8 = var4;
      double var10 = Mth.square(this.wallRange.getValue().doubleValue());
      boolean var12;
      if (var8 > var10) {
         var12 = true;
      } else {
         var12 = false;
      }

      boolean var13;
      if (var12 && !BlockInteractionUtil.a_bool_Entity(var1)) {
         var13 = false;
      } else {
         var13 = true;
      }

      return var13;
   }

   private boolean a_bool_BlockPos_Player(BlockPos var1, Player var2) {
      if (mc.level == null) {
         return false;
      }

      if (mc.level.isOutsideBuildHeight(var1)) {
         return false;
      }

      if (!mc.level.getBlockState(var1).canBeReplaced()) {
         return false;
      }

      ClientLevel var3 = mc.level;
      Entity var4 = (Entity)null;
      return var3.getEntities(var4, new AABB(var1), var0 -> true)
         .stream()
         .noneMatch(
            var1x -> {
               boolean var2x;
               if (var1x != var2
                  && !(var1x instanceof ExperienceOrb)
                  && !(var1x instanceof ItemEntity)
                  && !(var1x instanceof AbstractArrow)
                  && !EntityUtil.c_bool_Entity(var1x)) {
                  var2x = true;
               } else {
                  var2x = false;
               }

               return var2x;
            }
         );
   }

   private Set<BlockPos> b(Player var1) {
      HashSet var2 = new HashSet();
      AABB var3 = var1.getBoundingBox();
      int var4 = var1.blockPosition().getY();
      int var5 = (int)Math.floor(var3.minX);

      while (true) {
         double var6 = var5;
         double var8 = Math.ceil(var3.maxX);
         if (!(var6 < var8)) {
            return var2;
         }

         int var10 = (int)Math.floor(var3.minZ);

         while (true) {
            double var11 = var10;
            double var13 = Math.ceil(var3.maxZ);
            if (!(var11 < var13)) {
               var5++;
               break;
            }

            HashSet var15 = var2;
            int var16 = var5;
            int var17 = var4;
            int var18 = var10;
            var15.add(new BlockPos(var16, var17, var18));
            var10++;
         }
      }
   }
}
