package night.module.impl.visuals;

import java.awt.Color;
import lombok.Generated;
import net.minecraft.world.phys.Vec3;
import night.events.EventTarget;
import night.events.impl.AttackEntityEvent;
import night.events.impl.Render3DEvent;
import night.events.impl.TickEvent;
import night.events.impl.TotemPopEvent;
import night.module.Module;
import night.module.ModuleInfo;
import night.setting.impl.BooleanSetting;
import night.setting.impl.CategorySetting;
import night.setting.impl.ColorSetting;
import night.setting.impl.ModeSetting;
import night.setting.impl.NumberSetting;
import night.util.render.Particle3DRenderer;

@ModuleInfo(name = "Particles", description = "Custom particle effects for totem pops and entity hits.", category = Module.Category.VISUALS)
public class ParticlesModule extends Module {
   private final Particle3DRenderer y = new Particle3DRenderer();
   public CategorySetting totemCategory = new CategorySetting("Totem", "Settings for totem pop particle effects.");
   public BooleanSetting totem = new BooleanSetting(
      "Totem", "Enabled", "Enables totem particle customization.", new CategorySetting.CategoryCondition(this.totemCategory), true
   );
   public ModeSetting totemMode = new ModeSetting(
      "TotemMode",
      "Mode",
      "Particle mode for totem pops.",
      new CategorySetting.CategoryCondition(this.totemCategory),
      "Vanilla",
      new String[]{"Vanilla", "Custom"}
   );
   public NumberSetting totemNumber = new NumberSetting(
      "TotemNumber", "Number", "Amount of particles per totem pop.", new CategorySetting.CategoryCondition(this.totemCategory), 15, 1, 100
   );
   public ColorSetting vanillaColorA = new ColorSetting(
      "VanillaColorA",
      "Color A",
      "Primary particle color for vanilla totem pop.",
      new ModeSetting.ModeCondition(this.totemMode, "Vanilla"),
      new ColorSetting.ColorValue(new Color(255, 255, 255), false, false)
   );
   public ColorSetting vanillaColorB = new ColorSetting(
      "VanillaColorB",
      "Color B",
      "Fade particle color for vanilla totem pop.",
      new ModeSetting.ModeCondition(this.totemMode, "Vanilla"),
      new ColorSetting.ColorValue(new Color(255, 255, 255), false, false)
   );
   public ColorSetting customColorA = new ColorSetting(
      "CustomColorA",
      "Color A",
      "Primary particle color.",
      new ModeSetting.ModeCondition(this.totemMode, "Custom"),
      new ColorSetting.ColorValue(new Color(255, 255, 255, 255), false, false)
   );
   public ColorSetting customColorB = new ColorSetting(
      "CustomColorB",
      "Color B",
      "Secondary particle color.",
      new ModeSetting.ModeCondition(this.totemMode, "Custom"),
      new ColorSetting.ColorValue(new Color(255, 255, 255, 255), false, false)
   );
   public BooleanSetting totemGlow = new BooleanSetting(
      "TotemGlow", "Glow", "Adds a glow effect to totem particles.", new ModeSetting.ModeCondition(this.totemMode, "Custom"), true
   );
   public BooleanSetting totemPhysics = new BooleanSetting(
      "TotemPhysics", "Physics", "Enables gravity and bouncing for totem particles.", new ModeSetting.ModeCondition(this.totemMode, "Custom"), true
   );
   public NumberSetting totemVelocityX = new NumberSetting(
      "TotemVelocityX", "X", "X velocity multiplier.", new ModeSetting.ModeCondition(this.totemMode, "Custom"), 1.0, -5.0, 5.0
   );
   public NumberSetting totemVelocityY = new NumberSetting(
      "TotemVelocityY", "Y", "Y velocity multiplier.", new ModeSetting.ModeCondition(this.totemMode, "Custom"), 1.0, -5.0, 5.0
   );
   public NumberSetting totemVelocityZ = new NumberSetting(
      "TotemVelocityZ", "Z", "Z velocity multiplier.", new ModeSetting.ModeCondition(this.totemMode, "Custom"), 1.0, -5.0, 5.0
   );
   public CategorySetting hitCategory = new CategorySetting("Hit", "Settings for hit particle effects.");
   public BooleanSetting hit = new BooleanSetting("Hit", "Enabled", "Enables hit particles.", new CategorySetting.CategoryCondition(this.hitCategory), true);
   public NumberSetting hitNumber = new NumberSetting(
      "HitNumber", "Number", "Amount of particles per hit.", new CategorySetting.CategoryCondition(this.hitCategory), 15, 1, 100
   );
   public ColorSetting hitColorA = new ColorSetting(
      "HitColorA",
      "Color A",
      "Primary particle color.",
      new CategorySetting.CategoryCondition(this.hitCategory),
      new ColorSetting.ColorValue(new Color(255, 255, 255, 255), false, false)
   );
   public ColorSetting hitColorB = new ColorSetting(
      "HitColorB",
      "Color B",
      "Secondary particle color.",
      new CategorySetting.CategoryCondition(this.hitCategory),
      new ColorSetting.ColorValue(new Color(255, 255, 255, 255), false, false)
   );
   public BooleanSetting hitGlow = new BooleanSetting(
      "HitGlow", "Glow", "Adds a glow effect to hit particles.", new CategorySetting.CategoryCondition(this.hitCategory), true
   );
   public BooleanSetting x = new BooleanSetting(
      "HitPhysics", "Physics", "Enables gravity and bouncing for hit particles.", new CategorySetting.CategoryCondition(this.hitCategory), false
   );

   @EventTarget
   public void a(TotemPopEvent var1) {
      if (var1.getPlayer() != null && this.totem.getValue()) {
         if (this.totemMode.getValue().equalsIgnoreCase("Custom")) {
            Vec3 var2 = var1.getPlayer().getBoundingBox().getCenter();
            Vec3 var3 = new Vec3(
               this.totemVelocityX.getValue().doubleValue(), this.totemVelocityY.getValue().doubleValue(), this.totemVelocityZ.getValue().doubleValue()
            );
            this.a(var2, var3);
         }
      }
   }

   @EventTarget
   public void a(AttackEntityEvent var1) {
      if (var1.getEntity() != null && this.hit.getValue()) {
         Vec3 var2 = var1.getEntity().getBoundingBox().getCenter();
         this.a(var2);
      }
   }

   @EventTarget
   public void a(TickEvent var1) {
      if (mc.level != null && mc.player != null) {
         this.y.a();
      } else {
         this.y.b();
      }
   }

   @EventTarget
   public void a(Render3DEvent var1) {
      if (!this.y.c()) {
         this.y.a_void_PoseStack_float(var1.getMatrices(), var1.getTickDelta());
      }
   }

   private void a(Vec3 var1, Vec3 var2) {
      int var3 = this.customColorA.getColor().getRGB();
      int var4 = this.customColorB.getColor().getRGB();
      boolean var5 = this.totemGlow.getValue();
      boolean var6 = this.totemPhysics.getValue();
      int var7 = this.totemNumber.getValue().intValue();

      for (int var8 = 0; var8 < var7; var8++) {
         Vec3 var9 = Particle3DRenderer.a(0.5 * (0.75 + Math.random() * 0.25));
         var9 = new Vec3(var9.x * var2.x, var9.y * var2.y, var9.z * var2.z);
         if (var6) {
            this.y.a(var1, var9, 1.5, var3, var4, var5);
         } else {
            this.y.a(var1, var9, 0.9, 1.5, var3, var4, var5);
         }
      }
   }

   private void a(Vec3 var1) {
      int var2 = this.hitColorA.getColor().getRGB();
      int var3 = this.hitColorB.getColor().getRGB();
      boolean var4 = this.hitGlow.getValue();
      boolean var5 = this.x.getValue();
      int var6 = this.hitNumber.getValue().intValue();

      for (int var7 = 0; var7 < var6; var7++) {
         Vec3 var8 = Particle3DRenderer.a(0.5 * (0.75 + Math.random() * 0.25));
         if (var5) {
            this.y.a(var1, var8, 1.0, var2, var3, var4);
         } else {
            this.y.a(var1, var8, 0.9, 1.0, var2, var3, var4);
         }
      }
   }

   @Generated
   public Particle3DRenderer v() {
      return this.y;
   }
}
