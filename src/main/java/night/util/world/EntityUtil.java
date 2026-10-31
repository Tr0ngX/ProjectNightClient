package night.util.world;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.bee.Bee;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.animal.panda.Panda;
import net.minecraft.world.entity.animal.polarbear.PolarBear;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinArmPose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.item.EggItem;
import net.minecraft.world.item.EnderpearlItem;
import net.minecraft.world.item.ExperienceBottleItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.LingeringPotionItem;
import net.minecraft.world.item.SnowballItem;
import net.minecraft.world.item.SplashPotionItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import night.Night;
import night.module.impl.misc.FakePlayer;
import night.module.impl.visuals.LogoutSpot;
import night.module.impl.visuals.PopChams;
import night.util.MinecraftInstance;
import night.utils.minecraft.ImageShaderUtil;

public class EntityUtil implements MinecraftInstance {
   public static Map<MobEffect, Color> a = new HashMap<>();
   private static final Identifier b = Identifier.withDefaultNamespace("attacking");

   public static boolean a_bool_int(int var0) {
      LocalPlayer var1 = mc.player;
      if (var1 == null) {
         return false;
      }

      try {
         return var1.getId() == var0;
      } catch (IllegalStateException var3) {
         return false;
      }
   }

   public static boolean a_bool_Entity(Entity var0) {
      if (var0 != null && mc.level != null) {
         AABB var1 = var0.getBoundingBox();
         int var2 = Mth.floor(var1.minX);
         int var3 = Mth.ceil(var1.maxX);
         int var4 = Mth.floor(var1.minY);
         int var5 = Mth.ceil(var1.maxY);
         int var6 = Mth.floor(var1.minZ);
         int var7 = Mth.ceil(var1.maxZ);

         for (int var8 = var2; var8 < var3; var8++) {
            for (int var9 = var4; var9 < var5; var9++) {
               for (int var10 = var6; var10 < var7; var10++) {
                  BlockPos var11 = new BlockPos(var8, var9, var10);
                  VoxelShape var12 = mc.level.getBlockState(var11).getCollisionShape(mc.level, var11);
                  if (!var12.isEmpty() && var12.bounds().move(var11).intersects(var1)) {
                     return true;
                  }
               }
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public static List<BlockPos> b_List_Entity(Entity var0) {
      ArrayList var1 = new ArrayList();
      if (var0 != null && mc.level != null) {
         AABB var2 = var0.getBoundingBox();
         int var3 = Mth.floor(var2.minX);
         int var4 = Mth.ceil(var2.maxX);
         int var5 = Mth.floor(var2.minY);
         int var6 = Mth.ceil(var2.maxY);
         int var7 = Mth.floor(var2.minZ);
         int var8 = Mth.ceil(var2.maxZ);

         for (int var9 = var3; var9 < var4; var9++) {
            for (int var10 = var5; var10 < var6; var10++) {
               for (int var11 = var7; var11 < var8; var11++) {
                  BlockPos var12 = new BlockPos(var9, var10, var11);
                  VoxelShape var13 = mc.level.getBlockState(var12).getCollisionShape(mc.level, var12);
                  if (!var13.isEmpty() && var13.bounds().move(var12).intersects(var2)) {
                     var1.add(var12);
                  }
               }
            }
         }

         return var1;
      } else {
         return var1;
      }
   }

   public static boolean a() {
      if (mc.player == null) {
         return false;
      }

      if (mc.player.isUsingItem()) {
         return true;
      }

      if (mc.options.keyUse.isDown()) {
         ItemStack var0 = mc.player.getMainHandItem();
         ItemStack var1 = mc.player.getOffhandItem();
         if (!var0.isEmpty() && var0.getUseDuration(mc.player) > 0) {
            return true;
         }

         if (!var1.isEmpty() && var1.getUseDuration(mc.player) > 0) {
            return true;
         }
      }

      return false;
   }

   public static boolean a_bool_Player(Player var0) {
      if (Night.moduleManager.getModule(FakePlayer.class).isEnabled() && var0 == Night.moduleManager.getModule(FakePlayer.class).z()) {
         return false;
      }

      PlayerInfo var1 = mc.getConnection().getPlayerInfo(var0.getUUID());
      return var1 == null
         || var1.getProfile() == null
         || var0.getUUID().toString().startsWith(var0.getName().getString())
         || !var0.getGameProfile().name().equals(var0.getName().getString());
   }

   public static boolean b_bool_Player(Player var0) {
      if (var0 == null) {
         return false;
      }

      for (EquipmentSlot var4 : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
         ItemStack var5 = var0.getItemBySlot(var4);
         if (!var5.isEmpty() && (var4 != EquipmentSlot.CHEST || var5.getItem() != Items.ELYTRA)) {
            return false;
         }
      }

      return true;
   }

   public static boolean c_bool_Entity(Entity var0) {
      if (var0 != null && Night.moduleManager != null) {
         PopChams var1 = Night.moduleManager.getModule(PopChams.class);
         if (var1 != null && var1.a_bool_Entity(var0)) {
            return true;
         }

         LogoutSpot var2 = Night.moduleManager.getModule(LogoutSpot.class);
         return var2 != null && var2.a_bool_Entity(var0);
      } else {
         return false;
      }
   }

   public static int c_int_Player(Player var0) {
      PlayerInfo var1 = mc.getConnection().getPlayerInfo(var0.getUUID());
      return var1 == null ? 0 : var1.getLatency();
   }

   public static GameType d_GameType_Player(Player var0) {
      if (var0 != null && mc.getConnection() != null) {
         PlayerInfo var1 = mc.getConnection().getPlayerInfo(var0.getUUID());
         return var1 == null ? GameType.SURVIVAL : var1.getGameMode();
      } else {
         return GameType.SURVIVAL;
      }
   }

   public static String a_String_GameType(GameType var0) {
      return switch (var0) {
         case CREATIVE -> "C";
         case ADVENTURE -> "A";
         case SPECTATOR -> "SP";
         default -> "S";
      };
   }

   public static double a_double_Entity_SpeedUnit(Entity var0, EntityUtil.SpeedUnit var1) {
      double var2 = Math.sqrt(Mth.square(Math.abs(var0.getX() - var0.xo)) + Mth.square(Math.abs(var0.getZ() - var0.zo)));
      return var1 == EntityUtil.SpeedUnit.KILOMETERS
         ? var2 * 3.6 * Night.worldStateManager.getTimerMultiplier() * 20.0
         : var2 / 0.05 * Night.worldStateManager.getTimerMultiplier();
   }

   public static boolean e(Player var0) {
      Iterator var1 = new ArrayList(var0.getActiveEffects()).iterator();

      while (var1.hasNext()) {
         if (!((MobEffect)((MobEffectInstance)var1.next()).getEffect().value()).isBeneficial()) {
            return true;
         }
      }

      return false;
   }

   public static Vec3 a_Vec3_Entity_float(Entity var0, float var1) {
      double var2 = Mth.lerp(var1, var0.xo, var0.getX());
      double var4 = Mth.lerp(var1, var0.yo, var0.getY());
      double var6 = Mth.lerp(var1, var0.zo, var0.getZ());
      return new Vec3(var2, var4, var6);
   }

   public static Direction a_Direction_ThrownEnderpearl(ThrownEnderpearl var0) {
      Direction var1 = var0.getDirection();
      if (var1.equals(Direction.WEST)) {
         return Direction.EAST;
      } else {
         return var1.equals(Direction.EAST) ? Direction.WEST : var1;
      }
   }

   public static boolean a_bool_Item(Item var0) {
      return var0 instanceof EnderpearlItem
         || var0 instanceof TridentItem
         || var0 instanceof ExperienceBottleItem
         || var0 instanceof SnowballItem
         || var0 instanceof EggItem
         || var0 instanceof SplashPotionItem
         || var0 instanceof LingeringPotionItem;
   }

   public static boolean d_bool_Entity(Entity var0) {
      for (float var4 : new float[]{0.0F, 0.3F, -0.3F}) {
         for (float var8 : new float[]{0.0F, 0.3F, -0.3F}) {
            for (int var12 : new int[]{-1, 0, 1, 2}) {
               BlockPos var13 = BlockPos.containing(var0.getX() + var4, var0.getY(), var0.getZ() + var8).above(var12);
               if (new AABB(var13).intersects(var0.getBoundingBox()) && mc.level.getBlockState(var13).getBlock() == Blocks.COBWEB) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   public static boolean e(Entity var0) {
      if (var0 == null) {
         return false;
      }

      if (var0.getType() == EntityTypes.PIGLIN || var0.getType() == EntityTypes.PIGLIN_BRUTE) {
         return false;
      }

      if (var0 instanceof NeutralMob) {
         return true;
      }

      EntityType var1 = var0.getType();
      return var1 == EntityTypes.ZOMBIFIED_PIGLIN
         || var1 == EntityTypes.ENDERMAN
         || var1 == EntityTypes.IRON_GOLEM
         || var1 == EntityTypes.WOLF
         || var1 == EntityTypes.POLAR_BEAR
         || var1 == EntityTypes.BEE
         || var1 == EntityTypes.DOLPHIN
         || var1 == EntityTypes.GOAT
         || var1 == EntityTypes.PANDA
         || var1 == EntityTypes.LLAMA
         || var1 == EntityTypes.TRADER_LLAMA;
   }

   public static boolean f(Entity var0) {
      if (var0 instanceof Mob var1) {
         if (var1.isAggressive()) {
            return true;
         } else {
            AttributeInstance var2 = var1.getAttribute(Attributes.MOVEMENT_SPEED);
            if (var2 != null && var2.hasModifier(b)) {
               return true;
            } else if (var0 instanceof NeutralMob var3 && var3.isAngry()) {
               return true;
            } else if (var0 instanceof EnderMan var5 && (var5.isCreepy() || var5.hasBeenStaredAt())) {
               return true;
            } else if (var0 instanceof PolarBear var6 && var6.isStanding()) {
               return true;
            } else if (var0 instanceof IronGolem var7 && var7.getAttackAnimationTick() > 0) {
               return true;
            } else if (var0 instanceof Bee var8 && (var8.hasStung() || var8.isAngry())) {
               return true;
            } else if (var0 instanceof Panda var9 && var9.isAggressive()) {
               return true;
            } else {
               if (var0 instanceof Piglin var10) {
                  PiglinArmPose var4 = var10.getArmPose();
                  if (var4 == PiglinArmPose.ATTACKING_WITH_MELEE_WEAPON || var4 == PiglinArmPose.CROSSBOW_HOLD || var4 == PiglinArmPose.CROSSBOW_CHARGE) {
                     return true;
                  }
               }

               if (mc.player != null) {
                  DamageSource var11 = mc.player.getLastDamageSource();
                  if (var11 != null && var11.getEntity() == var0) {
                     return true;
                  }

                  DamageSource var13 = var1.getLastDamageSource();
                  if (var13 != null && var13.getEntity() == mc.player) {
                     return true;
                  }
               }

               return var1.getTarget() != null ? true : var0 instanceof NeutralMob var12 && var12.getPersistentAngerTarget() != null;
            }
         }
      } else {
         return false;
      }
   }

   public static boolean g(Entity var0) {
      if (var0 == null) {
         return false;
      } else if (f(var0)) {
         return true;
      } else if (e(var0)) {
         return false;
      } else if (!(var0 instanceof Enemy) && !(var0 instanceof Monster) && !(var0 instanceof Endermite)) {
         return var0.getType() != EntityTypes.ENDERMITE && var0.getType() != EntityTypes.SILVERFISH
            ? var0.getType().getCategory() == MobCategory.MONSTER
            : true;
      } else {
         return true;
      }
   }

   public static boolean h(Entity var0) {
      if (var0 == null || e(var0) || g(var0)) {
         return false;
      }

      if (var0 instanceof Animal) {
         return true;
      }

      MobCategory var1 = var0.getType().getCategory();
      return var1 == MobCategory.CREATURE
         || var1 == MobCategory.WATER_CREATURE
         || var1 == MobCategory.WATER_AMBIENT
         || var1 == MobCategory.UNDERGROUND_WATER_CREATURE
         || var1 == MobCategory.AXOLOTLS
         || var1 == MobCategory.AMBIENT;
   }

   public static double b_double_int(int var0) {
      ImageShaderUtil.a();
      return ImageShaderUtil.glGetShaderConstant(var0);
   }

   static {
      a.put((MobEffect)MobEffects.SPEED.value(), new Color(124, 175, 198));
      a.put((MobEffect)MobEffects.SLOWNESS.value(), new Color(90, 108, 129));
      a.put((MobEffect)MobEffects.HASTE.value(), new Color(217, 192, 67));
      a.put((MobEffect)MobEffects.MINING_FATIGUE.value(), new Color(74, 66, 23));
      a.put((MobEffect)MobEffects.STRENGTH.value(), new Color(147, 36, 35));
      a.put((MobEffect)MobEffects.INSTANT_HEALTH.value(), new Color(67, 10, 9));
      a.put((MobEffect)MobEffects.INSTANT_DAMAGE.value(), new Color(67, 10, 9));
      a.put((MobEffect)MobEffects.JUMP_BOOST.value(), new Color(34, 255, 76));
      a.put((MobEffect)MobEffects.NAUSEA.value(), new Color(85, 29, 74));
      a.put((MobEffect)MobEffects.REGENERATION.value(), new Color(205, 92, 171));
      a.put((MobEffect)MobEffects.RESISTANCE.value(), new Color(153, 69, 58));
      a.put((MobEffect)MobEffects.FIRE_RESISTANCE.value(), new Color(228, 154, 58));
      a.put((MobEffect)MobEffects.WATER_BREATHING.value(), new Color(46, 82, 153));
      a.put((MobEffect)MobEffects.INVISIBILITY.value(), new Color(127, 131, 146));
      a.put((MobEffect)MobEffects.BLINDNESS.value(), new Color(31, 31, 35));
      a.put((MobEffect)MobEffects.NIGHT_VISION.value(), new Color(31, 31, 161));
      a.put((MobEffect)MobEffects.HUNGER.value(), new Color(88, 118, 83));
      a.put((MobEffect)MobEffects.WEAKNESS.value(), new Color(72, 77, 72));
      a.put((MobEffect)MobEffects.POISON.value(), new Color(78, 147, 49));
      a.put((MobEffect)MobEffects.WITHER.value(), new Color(53, 42, 39));
      a.put((MobEffect)MobEffects.HEALTH_BOOST.value(), new Color(248, 125, 35));
      a.put((MobEffect)MobEffects.ABSORPTION.value(), new Color(37, 82, 165));
      a.put((MobEffect)MobEffects.SATURATION.value(), new Color(248, 36, 35));
      a.put((MobEffect)MobEffects.GLOWING.value(), new Color(148, 160, 97));
      a.put((MobEffect)MobEffects.LEVITATION.value(), new Color(206, 255, 255));
      a.put((MobEffect)MobEffects.LUCK.value(), new Color(51, 153, 0));
      a.put((MobEffect)MobEffects.UNLUCK.value(), new Color(192, 164, 77));
   }

   public enum SpeedUnit {
      METERS,
      KILOMETERS;
   }
}
