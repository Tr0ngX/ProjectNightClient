package night.module.impl.visuals;

import com.mojang.blaze3d.vertex.PoseStack;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.hurtingprojectile.DragonFireball;
import net.minecraft.world.entity.projectile.hurtingprojectile.Fireball;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.entity.projectile.hurtingprojectile.WitherSkull;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEgg;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.phys.Vec3;
import night.events.EventTarget;
import night.events.impl.ClientConnectEvent;
import night.events.impl.ClientDisconnectEvent;
import night.events.impl.Render3DEvent;
import night.events.impl.ServerConnectEvent;
import night.events.impl.TickEvent;
import night.module.Module;
import night.module.ModuleInfo;
import night.setting.impl.BooleanSetting;
import night.setting.impl.CategorySetting;
import night.setting.impl.ColorSetting;
import night.setting.impl.NumberSetting;
import night.util.color.ColorUtil;
import night.util.render.Render3DUtil;
import night.util.world.EntityUtil;

@ModuleInfo(name = "Trails", description = "Renders smooth trails behind projectiles and throwables.", category = Module.Category.VISUALS)
public class Trails extends Module {
   public CategorySetting targetsCategory = new CategorySetting("Targets", "The projectiles to render trails for.");
   public BooleanSetting pearls = new BooleanSetting(
      "Pearls", "Renders trails behind ender pearls.", new CategorySetting.CategoryCondition(this.targetsCategory), true
   );
   public BooleanSetting arrows = new BooleanSetting(
      "Arrows", "Renders trails behind arrows.", new CategorySetting.CategoryCondition(this.targetsCategory), true
   );
   public BooleanSetting witherSkull = new BooleanSetting(
      "WitherSkull", "Renders trails behind wither skulls.", new CategorySetting.CategoryCondition(this.targetsCategory), true
   );
   public BooleanSetting egg = new BooleanSetting("Egg", "Renders trails behind eggs.", new CategorySetting.CategoryCondition(this.targetsCategory), true);
   public BooleanSetting snowball = new BooleanSetting(
      "Snowball", "Renders trails behind snowballs.", new CategorySetting.CategoryCondition(this.targetsCategory), true
   );
   public BooleanSetting fireCharge = new BooleanSetting(
      "FireCharge", "Renders trails behind fire charges.", new CategorySetting.CategoryCondition(this.targetsCategory), true
   );
   public NumberSetting fade = new NumberSetting("Fade", "The duration in milliseconds before a trail segment fades away.", 1000, 100, 5000);
   public ColorSetting color = new ColorSetting("Color", "The color used for the trails.", ColorUtil.c());
   private final Map<UUID, List<Trails.TrailPoint>> n = new ConcurrentHashMap<>();

   @EventTarget
   public void a_void_TickEvent(TickEvent var1) {
      if (mc.level != null && mc.player != null) {
         long var2 = System.currentTimeMillis();

         for (Entity var5 : (Iterable<Entity>)(Object)mc.level.entitiesForRendering()) {
            if (this.a_bool_Entity(var5) && var5.isAlive()) {
               UUID var6 = var5.getUUID();
               Vec3 var7 = var5.position().add(0.0, var5.getBbHeight() * 0.5, 0.0);
               List var8 = this.n.computeIfAbsent(var6, var0 -> new ArrayList<>());
               synchronized (var8) {
                  if (var8.isEmpty()) {
                     if (var5 instanceof Projectile && ((Projectile)var5).getOwner() instanceof LivingEntity var11) {
                        var8.add(new Trails.TrailPoint(var11.getEyePosition().subtract(0.0, 0.1, 0.0), var2));
                     } else if (var5.distanceTo(mc.player) <= 3.5 && mc.player.isAlive()) {
                        var8.add(new Trails.TrailPoint(mc.player.getEyePosition().subtract(0.0, 0.1, 0.0), var2));
                     }
                  }

                  if (var8.isEmpty() || ((Trails.TrailPoint)var8.get(var8.size() - 1)).vec().distanceToSqr(var7) > 1.0E-4) {
                     var8.add(new Trails.TrailPoint(var7, var2));
                  }
               }
            }
         }
      }
   }

   @EventTarget
   public void a_void_Render3DEvent(Render3DEvent var1) {
      if (mc.level != null && mc.player != null && !this.n.isEmpty()) {
         long var2 = System.currentTimeMillis();
         long var4 = this.fade.getValue().longValue();
         PoseStack var6 = var1.getMatrices();
         Color var7 = this.color.getColor();
         HashMap var8 = new HashMap();

         for (Entity var10 : (Iterable<Entity>)(Object)mc.level.entitiesForRendering()) {
            if (var10.isAlive()) {
               var8.put(var10.getUUID(), var10);
            }
         }

         Iterator var24 = this.n.entrySet().iterator();

         while (var24.hasNext()) {
            Entry var25 = (Entry)var24.next();
            UUID var11 = (UUID)var25.getKey();
            List<Trails.TrailPoint> var12 = (List<Trails.TrailPoint>)var25.getValue();
            Entity var13 = (Entity)var8.get(var11);
            synchronized (var12) {
               var12.removeIf(var4x -> var2 - var4x.timestamp() > var4);
               if (var12.isEmpty() && var13 == null) {
                  var24.remove();
               } else if (var12.size() >= 1 || var13 != null) {
                  for (int var15 = 0; var15 < var12.size() - 1; var15++) {
                     Trails.TrailPoint var16 = (Trails.TrailPoint)var12.get(var15);
                     Trails.TrailPoint var17 = (Trails.TrailPoint)var12.get(var15 + 1);
                     float var18 = var4 > 0L ? Mth.clamp(1.0F - (float)(var2 - var16.timestamp()) / (float)var4, 0.0F, 1.0F) : 1.0F;
                     float var19 = var4 > 0L ? Mth.clamp(1.0F - (float)(var2 - var17.timestamp()) / (float)var4, 0.0F, 1.0F) : 1.0F;
                     Color var20 = ColorUtil.a(var7, Math.round(var7.getAlpha() * var18));
                     Color var21 = ColorUtil.a(var7, Math.round(var7.getAlpha() * var19));
                     Render3DUtil.a(var6, var16.vec(), var17.vec(), var20, var21);
                  }

                  if (var13 != null && !var12.isEmpty()) {
                     Trails.TrailPoint var26 = (Trails.TrailPoint)var12.get(var12.size() - 1);
                     Vec3 var27 = EntityUtil.a_Vec3_Entity_float(var13, var1.getTickDelta()).add(0.0, var13.getBbHeight() * 0.5, 0.0);
                     float var28 = var4 > 0L ? Mth.clamp(1.0F - (float)(var2 - var26.timestamp()) / (float)var4, 0.0F, 1.0F) : 1.0F;
                     Color var29 = ColorUtil.a(var7, Math.round(var7.getAlpha() * var28));
                     Color var30 = ColorUtil.a(var7, var7.getAlpha());
                     Render3DUtil.a(var6, var26.vec(), var27, var29, var30);
                  }
               }
            }
         }
      }
   }

   private boolean a_bool_Entity(Entity var1) {
      if (!this.pearls.getValue() || !(var1 instanceof ThrownEnderpearl) && var1.getType() != EntityTypes.ENDER_PEARL) {
         if (!this.arrows.getValue() || !(var1 instanceof AbstractArrow) && var1.getType() != EntityTypes.ARROW && var1.getType() != EntityTypes.SPECTRAL_ARROW
            )
          {
            if (!this.witherSkull.getValue() || !(var1 instanceof WitherSkull) && var1.getType() != EntityTypes.WITHER_SKULL) {
               if (!this.egg.getValue() || !(var1 instanceof ThrownEgg) && var1.getType() != EntityTypes.EGG) {
                  return !this.snowball.getValue() || !(var1 instanceof Snowball) && var1.getType() != EntityTypes.SNOWBALL
                     ? this.fireCharge.getValue()
                        && (
                           var1 instanceof SmallFireball
                              || var1 instanceof LargeFireball
                              || var1 instanceof Fireball
                              || var1 instanceof DragonFireball
                              || var1.getType() == EntityTypes.SMALL_FIREBALL
                              || var1.getType() == EntityTypes.FIREBALL
                              || var1.getType() == EntityTypes.DRAGON_FIREBALL
                        )
                     : true;
               } else {
                  return true;
               }
            } else {
               return true;
            }
         } else {
            return true;
         }
      } else {
         return true;
      }
   }

   @Override
   public void onDisable() {
      this.n.clear();
   }

   @EventTarget
   public void a_void_ClientDisconnectEvent(ClientDisconnectEvent var1) {
      this.n.clear();
   }

   @EventTarget
   public void a_void_ClientConnectEvent(ClientConnectEvent var1) {
      this.n.clear();
   }

   @EventTarget
   public void a_void_ServerConnectEvent(ServerConnectEvent var1) {
      this.n.clear();
   }

   public record TrailPoint(Vec3 vec, long timestamp) {
   }
}
