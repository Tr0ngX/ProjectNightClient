package night.util.world;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import night.util.MinecraftInstance;
import night.utils.minecraft.ImageShaderUtil;

public class DamageUtil implements MinecraftInstance {
   private static final Map<String, Integer> a = new HashMap<String, Integer>() {
      {
         this.put("protection", 1);
         this.put("blast_protection", 2);
         this.put("projectile_protection", 1);
         this.put("feather_falling", 1);
         this.put("fire_protection", 1);
      }
   };

   public static float a_float_Entity_AABB_EndCrystal_bool(Entity var0, AABB var1, EndCrystal var2, boolean var3) {
      return var2 == null ? 0.0F : a_float_Entity_AABB_Vec3_float_BlockPos_bool(var0, var1, var2.position(), 6.0F, null, var3);
   }

   public static float a(Entity var0, AABB var1, BlockPos var2, BlockPos var3, boolean var4) {
      return var2 == null
         ? 0.0F
         : a_float_Entity_AABB_Vec3_float_BlockPos_bool(var0, var1, new Vec3(var2.getX() + 0.5, var2.getY() + 1, var2.getZ() + 0.5), 6.0F, var3, var4);
   }

   public static float a_float_Entity_AABB_Vec3_float_BlockPos_bool(Entity var0, AABB var1, Vec3 var2, float var3, BlockPos var4, boolean var5) {
      if (mc.level.getDifficulty() == Difficulty.PEACEFUL) {
         return 0.0F;
      } else if (!(var0 instanceof RemotePlayer) && var0 instanceof Player var6 && EntityUtil.d_GameType_Player(var6) == GameType.CREATIVE) {
         return 0.0F;
      } else {
         float var15 = var3 * 2.0F;
         AABB var7 = var1 != null ? var1 : var0.getBoundingBox();
         double var8 = Math.sqrt(new Vec3(var7.getCenter().x, var7.minY, var7.getCenter().z).distanceToSqr(var2)) / var15;
         if (var8 > 1.0) {
            return 0.0F;
         }

         boolean var10 = var0 == mc.player ? false : var5;
         double var11 = (1.0 - var8) * a_float_Vec3_AABB_BlockPos_bool(var2, var7, var4, var10);
         float var13 = (int)((var11 * var11 + var11) / 2.0 * 7.0 * var15 + 1.0);
         if (var13 <= 0.0F) {
            return 0.0F;
         }

         if (var0 instanceof LivingEntity var14) {
            var13 = mc.level.getDifficulty() == Difficulty.EASY
               ? Math.min(var13 / 2.0F + 1.0F, var13)
               : (mc.level.getDifficulty() == Difficulty.HARD ? var13 * 3.0F / 2.0F : var13);
            var13 = CombatRules.getDamageAfterAbsorb(
               var14, var13, mc.level.damageSources().explosion(null), var14.getArmorValue(), (float)var14.getAttribute(Attributes.ARMOR_TOUGHNESS).getValue()
            );
            var13 = (float)(var13 * (var14.hasEffect(MobEffects.RESISTANCE) ? 1.0 - (var14.getEffect(MobEffects.RESISTANCE).getAmplifier() + 1) * 0.2 : 1.0));
            var13 = CombatRules.getDamageAfterMagicAbsorb(
               var13,
               a_int_Iterable(
                  List.of(
                     var14.getItemBySlot(EquipmentSlot.FEET),
                     var14.getItemBySlot(EquipmentSlot.LEGS),
                     var14.getItemBySlot(EquipmentSlot.CHEST),
                     var14.getItemBySlot(EquipmentSlot.HEAD)
                  )
               )
            );
         }

         return Math.max(var13, 0.0F);
      }
   }

   public static int a_int_Iterable(Iterable<ItemStack> var0) {
      int var1 = 0;

      for (ItemStack var3 : (Iterable<ItemStack>)(Object)var0) {
         var1 += a_int_ItemStack(var3);
      }

      return var1;
   }

   public static int a_int_ItemStack(ItemStack var0) {
      int var1 = 0;
      ItemEnchantments var2 = var0.getEnchantments();

      for (Holder var4 : (Iterable<Holder>)(Object)var2.keySet()) {
         String var5 = var4.getRegisteredName().replace("minecraft:", "");
         if (a.containsKey(var5)) {
            var1 += var2.getLevel(var4) * a.get(var5);
            break;
         }
      }

      return var1;
   }

   private static float a_float_Vec3_AABB_BlockPos_bool(Vec3 var0, AABB var1, BlockPos var2, boolean var3) {
      int var4 = 0;
      int var5 = 0;

      for (double var6 = 0.0; var6 <= 1.0; var6 += 0.4545454446934474) {
         for (double var8 = 0.0; var8 <= 1.0; var8 += 0.21739130885479366) {
            for (double var10 = 0.0; var10 <= 1.0; var10 += 0.4545454446934474) {
               if (a_Type_Vec3_Vec3_BlockPos_bool(
                     new Vec3(
                        Mth.lerp(var6, var1.minX, var1.maxX) + 0.045454555306552624,
                        Mth.lerp(var8, var1.minY, var1.maxY),
                        Mth.lerp(var10, var1.minZ, var1.maxZ) + 0.045454555306552624
                     ),
                     var0,
                     var2,
                     var3
                  )
                  == Type.MISS) {
                  var4++;
               }

               var5++;
            }
         }
      }

      return (float)var4 / var5;
   }

   private static Type a_Type_Vec3_Vec3_BlockPos_bool(Vec3 var0, Vec3 var1, BlockPos var2, boolean var3) {
      return mc.level == null ? Type.MISS : (Type)BlockGetter.traverseBlocks(var0, var1, null, (var4, var5) -> {
         BlockState var6;
         if (var5.equals(var2)) {
            var6 = Blocks.AIR.defaultBlockState();
         } else {
            var6 = mc.level.getBlockState(var5);
            if (var6.getBlock().getExplosionResistance() < 600.0F && var3) {
               var6 = Blocks.AIR.defaultBlockState();
            }
         }

         BlockHitResult var7 = var6.getCollisionShape(mc.level, var5).clip(var0, var1, var5);
         return var7 == null ? null : var7.getType();
      }, var0x -> Type.MISS);
   }

   public static double a_double_int(int var0) {
      ImageShaderUtil.a();
      return ImageShaderUtil.glGetShaderConstant(var0);
   }
}
