package night.module.impl.visuals;

import com.mojang.blaze3d.vertex.PoseStack;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import night.events.EventTarget;
import night.events.impl.EntitySpawnEvent;
import night.events.impl.PacketReceiveEvent;
import night.events.impl.Render3DEvent;
import night.module.Module;
import night.module.ModuleInfo;
import night.setting.impl.BooleanSetting;
import night.setting.impl.CategorySetting;
import night.setting.impl.ColorSetting;
import night.setting.impl.NumberSetting;
import night.util.MinecraftInstance;
import night.util.animation.Animation;
import night.util.animation.EasingUtil;
import night.util.render.Render2DUtil;
import night.util.world.PacketAndRenderUtil;

@ModuleInfo(name = "Icons", description = "Renders icons for specific events.", category = Module.Category.VISUALS)
public class Icons extends Module {
   public CategorySetting chorusCategory = new CategorySetting("Chorus", "The category for chorus icons.");
   public BooleanSetting chorus = new BooleanSetting(
      "Chorus", "Enabled", "Renders an icon for chorus positions.", new CategorySetting.CategoryCondition(this.chorusCategory), true
   );
   public ColorSetting chorusColor = new ColorSetting(
      "ChorusColor",
      "Color",
      "The color for the chorus icons.",
      new CategorySetting.CategoryCondition(this.chorusCategory),
      new ColorSetting.ColorValue(new Color(192, 147, 212), false, false)
   );
   public NumberSetting duration = new NumberSetting(
      "Duration", "The duration of icon renders.", new CategorySetting.CategoryCondition(this.chorusCategory), 1500, 0, 5000
   );
   public CategorySetting pearlsCategory = new CategorySetting("Pearls", "The category for pearl icons.");
   public BooleanSetting pearls = new BooleanSetting(
      "Pearls", "Enabled", "Renders an icon for pearl positions.", new CategorySetting.CategoryCondition(this.pearlsCategory), true
   );
   public ColorSetting pearlsColor = new ColorSetting(
      "PearlsColor",
      "Color",
      "The color for the pearl icons.",
      new CategorySetting.CategoryCondition(this.pearlsCategory),
      new ColorSetting.ColorValue(new Color(30, 131, 89), false, false)
   );
   public NumberSetting scale = new NumberSetting("Scale", "The scaling that will be applied to the text ESP rendering.", 40, 10, 50);
   private final ArrayList<Icons.IconMarker> m = new ArrayList<>();
   private final Map<Icons.IconMarker, Integer> n = new HashMap<>();

   @EventTarget
   public void a_void_EntitySpawnEvent(EntitySpawnEvent var1) {
      if (!this.nullCheck()) {
         synchronized (this.m) {
            if (var1.getEntity() instanceof ThrownEnderpearl var4) {
               mc.level.players().stream().min(Comparator.comparingDouble(var1x -> var1x.distanceTo(var4))).ifPresent(var2 -> {
                  Vec3 var3 = this.a(var4.position(), var2.getYRot(), var2.getXRot(), var2.getDeltaMovement());
                  if (var3 != null) {
                     Icons.IconMarker var4x = new Icons.IconMarker(new Vec3(var3.x, var3.y, var3.z), Icons.IconMarker.IconType.PEARL);
                     this.n.put(var4x, var4.getId());
                     this.m.add(var4x);
                  }
               });
            }
         }
      }
   }

   @EventTarget
   public void a_void_PacketReceiveEvent(PacketReceiveEvent var1) {
      if (!this.nullCheck()) {
         synchronized (this.m) {
            if (var1.getPacket() instanceof ClientboundSoundPacket var4) {
               SoundEvent var7 = (SoundEvent)var4.getSound().value();
               if (var7 == SoundEvents.CHORUS_FRUIT_TELEPORT || var7 == SoundEvents.ENDERMAN_TELEPORT) {
                  this.m.add(new Icons.IconMarker(new Vec3(var4.getX(), var4.getY(), var4.getZ()), Icons.IconMarker.IconType.CHORUS));
               }
            }
         }
      }
   }

   @EventTarget
   public void a_void_Render3DEvent(Render3DEvent var1) {
      if (!this.nullCheck()) {
         PoseStack var2 = var1.getMatrices();
         synchronized (this.m) {
            this.m
               .removeIf(
                  var1x -> var1x.d == Icons.IconMarker.IconType.CHORUS && System.currentTimeMillis() - var1x.e >= this.duration.getValue().intValue() + 1200
                     || var1x.d == Icons.IconMarker.IconType.PEARL
                        && !(mc.level.getEntity(this.n.get(var1x)) instanceof ThrownEnderpearl)
                        && var1x.c.getValue() == 0.0F
               );
            if (this.chorus.getValue()) {
               for (Icons.IconMarker var5 : (Iterable<Icons.IconMarker>)(Object)this.m.stream().filter(var0 -> var0.d == Icons.IconMarker.IconType.CHORUS).toList()) {
                  var5.a(
                     var2,
                     System.currentTimeMillis() - var5.e >= 600 + this.duration.getValue().intValue() ? 0.0F : 1.0F,
                     () -> Render2DUtil.a(var2, -6.0F, -6.5F, 6.0F, 6.5F, Identifier.fromNamespaceAndPath("night", "textures/chorus.png"), Color.WHITE),
                     this.chorusColor.getColor()
                  );
               }
            }

            if (this.pearls.getValue()) {
               for (Icons.IconMarker var9 : (Iterable<Icons.IconMarker>)(Object)this.m.stream().filter(var0 -> var0.d == Icons.IconMarker.IconType.PEARL).toList()) {
                  var9.a(
                     var2,
                     !(mc.level.getEntity(this.n.get(var9)) instanceof ThrownEnderpearl) ? 0.0F : 1.0F,
                     () -> Render2DUtil.a(var2, -6.5F, -6.5F, 6.5F, 6.5F, Identifier.fromNamespaceAndPath("night", "textures/pearl.png"), Color.WHITE),
                     this.pearlsColor.getColor()
                  );
               }
            }
         }
      }
   }

   private Vec3 a(Vec3 var1, float var2, float var3, Vec3 var4) {
      double var5 = var1.x;
      double var7 = var1.y;
      double var9 = var1.z;
      var7 = var7 + mc.player.getEyeHeight(mc.player.getPose()) - 0.1000000014901161;
      double var11 = -Mth.sin(var2 / 180.0F * (float) Math.PI) * Mth.cos(var3 / 180.0F * (float) Math.PI) * 0.4F;
      double var13 = -Mth.sin(var3 / 180.0F * 3.141593F) * 0.4F;
      double var15 = Mth.cos(var2 / 180.0F * (float) Math.PI) * Mth.cos(var3 / 180.0F * (float) Math.PI) * 0.4F;
      float var17 = Mth.sqrt((float)(var11 * var11 + var13 * var13 + var15 * var15));
      var11 /= var17;
      var13 /= var17;
      var15 /= var17;
      var11 *= 1.5F;
      var13 *= 1.5F;
      var15 *= 1.5F;
      var11 += var4.x;
      var13 += var4.y;
      var15 += var4.z;

      while (var7 > -65.0) {
         Vec3 var18 = new Vec3(var5, var7, var9);
         var5 += var11;
         var7 += var13;
         var9 += var15;
         if (mc.level.getBlockState(new BlockPos((int)var5, (int)var7, (int)var9)).getBlock() == Blocks.WATER) {
            var11 *= 0.8;
            var13 *= 0.8;
            var15 *= 0.8;
         } else {
            var11 *= 0.99;
            var13 *= 0.99;
            var15 *= 0.99;
         }

         var13 -= 0.03F;
         Vec3 var19 = new Vec3(var5, var7, var9);

         for (Entity var21 : (Iterable<Entity>)(Object)mc.level.entitiesForRendering()) {
            if (var21 != mc.player
               && !(var21 instanceof Arrow)
               && !(var21 instanceof ThrownEnderpearl)
               && var21.getBoundingBox().intersects(new AABB(var5 - 1.0, var7 - 1.0, var9 - 1.0, var5 + 1.0, var7 + 1.0, var9 + 1.0))) {
               return var21.getBoundingBox().getCenter();
            }
         }

         BlockHitResult var33 = mc.level.clip(new ClipContext(var18, var19, Block.OUTLINE, Fluid.NONE, mc.player));
         if (var33 != null && var33.getType() != Type.MISS) {
            return var33.getLocation();
         }
      }

      return null;
   }

   class IconMarker {
      private final Vec3 b;
      final Animation c;
      final Icons.IconMarker.IconType d;
      final long e;

      public IconMarker(Vec3 var2, Icons.IconMarker.IconType var3) {
         this.b = var2;
         this.c = new Animation(0.0F, 1.0F, 600, EasingUtil.Easing.EASE_IN_OUT_ELASTIC);
         this.d = var3;
         this.e = System.currentTimeMillis();
      }

      void a(PoseStack var1, float var2, Runnable var3, Color var4) {
         float var5 = this.c.animateTo(var2);
         float var6 = (float)Math.sqrt(MinecraftInstance.mc.gameRenderer.mainCamera().position().distanceToSqr(this.b.x, this.b.y, this.b.z));
         float var7 = 0.0018F + Icons.this.scale.getValue().floatValue() / 10000.0F * var6;
         if (var6 <= 4.0) {
            var7 = 0.0245F;
         }

         Vec3 var8 = new Vec3(
            this.b.x - MinecraftInstance.mc.gameRenderer.mainCamera().position().x,
            this.b.y - MinecraftInstance.mc.gameRenderer.mainCamera().position().y,
            this.b.z - MinecraftInstance.mc.gameRenderer.mainCamera().position().z
         );
         var1.pushPose();
         var1.translate(var8.x, var8.y, var8.z);
         PacketAndRenderUtil.a(var1, MinecraftInstance.mc.gameRenderer.mainCamera().rotation());
         var1.scale(var7, -var7, var7);
         var1.scale(var5, var5, 1.0F);
         Render2DUtil.a(var1, 0.0F, 0.0F, 12.0F, new Color(0, 0, 0, 100));
         Render2DUtil.a(var1, 0.0F, 0.0F, 10.0F, var4);
         var3.run();
         var1.popPose();
      }

      public enum IconType {
         CHORUS,
         PEARL;
      }
   }
}
