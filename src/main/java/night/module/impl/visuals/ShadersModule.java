package night.module.impl.visuals;

import java.awt.Color;
import java.util.Arrays;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import night.Night;
import night.module.Module;
import night.module.ModuleInfo;
import night.module.impl.core.ColorModule;
import night.setting.impl.BooleanSetting;
import night.setting.impl.CategorySetting;
import night.setting.impl.ColorSetting;
import night.setting.impl.ImageSetting;
import night.setting.impl.ModeSetting;
import night.setting.impl.NumberSetting;
import night.util.color.ColorUtil;
import night.util.render.EspShaderRenderer;

@ModuleInfo(name = "Shaders", description = "Overlays specified entities with a customizable shader.", category = Module.Category.VISUALS)
public class ShadersModule extends Module {
   public NumberSetting range = new NumberSetting("Range", "The maximum range at which shaders will be rendered.", 64.0F, 1.0F, 256.0F);
   public CategorySetting targetsCategory = new CategorySetting("Targets", "The things that the shader rendering will be applied onto.");
   public BooleanSetting players = new BooleanSetting(
      "Players", "Renders the shader effect on player entities.", new CategorySetting.CategoryCondition(this.targetsCategory), true
   );
   public BooleanSetting hands = new BooleanSetting(
      "Hands", "Renders the shader effect on first-person hands.", new CategorySetting.CategoryCondition(this.targetsCategory), true
   );
   public BooleanSetting hostiles = new BooleanSetting(
      "Hostiles", "Renders the shader effect on hostile entities.", new CategorySetting.CategoryCondition(this.targetsCategory), true
   );
   public BooleanSetting animals = new BooleanSetting(
      "Animals", "Renders the shader effect on animal entities.", new CategorySetting.CategoryCondition(this.targetsCategory), true
   );
   public BooleanSetting ambient = new BooleanSetting(
      "Ambient", "Renders the shader effect on ambient entities.", new CategorySetting.CategoryCondition(this.targetsCategory), false
   );
   public BooleanSetting invisibles = new BooleanSetting(
      "Invisibles", "Renders the shader effect on invisible entities.", new CategorySetting.CategoryCondition(this.targetsCategory), true
   );
   public BooleanSetting items = new BooleanSetting(
      "Items", "Renders the shader effect on item entities.", new CategorySetting.CategoryCondition(this.targetsCategory), true
   );
   public BooleanSetting crystals = new BooleanSetting(
      "Crystals", "Renders the shader effect on crystal entities.", new CategorySetting.CategoryCondition(this.targetsCategory), true
   );
   public BooleanSetting pearl = new BooleanSetting(
      "Pearl", "Renders the shader effect on thrown ender pearls.", new CategorySetting.CategoryCondition(this.targetsCategory), true
   );
   public BooleanSetting boat = new BooleanSetting(
      "Boat", "Renders the shader effect on all boat types.", new CategorySetting.CategoryCondition(this.targetsCategory), true
   );
   public BooleanSetting minecart = new BooleanSetting(
      "Minecart", "Renders the shader effect on all minecart types.", new CategorySetting.CategoryCondition(this.targetsCategory), true
   );
   public BooleanSetting others = new BooleanSetting(
      "Others", "Renders the shader effect on miscellaneous entities.", new CategorySetting.CategoryCondition(this.targetsCategory), false
   );
   public ModeSetting mode = new ModeSetting(
      "Mode", "The rendering that will be applied to the target entities.", "Both", new String[]{"Fill", "Outline", "Both", "Glow", "Bloom"}
   );
   public NumberSetting glowIntensity = new NumberSetting(
      "GlowIntensity",
      "Glow intensity",
      "How wide and how bright the glow around and over the target entities is.",
      new ModeSetting.ModeCondition(this.mode, "Glow"),
      50,
      0,
      100
   );
   public ColorSetting glowColor = new ColorSetting(
      "GlowColor",
      "The color that will be used for the outer glow effect.",
      new ModeSetting.ModeCondition(this.mode, "Glow"),
      new ColorSetting.ColorValue(new Color(255, 0, 255, 255), false, false)
   );
   public NumberSetting bloomWidth = new NumberSetting(
      "BloomWidth", "Width", "Bloom outline width/radius.", new ModeSetting.ModeCondition(this.mode, "Bloom"), 3.0, 1.0, 10.0
   );
   public BooleanSetting bloomGlowInside = new BooleanSetting(
      "BloomGlowInside", "GlowInside", "Enables bloom/glow on inside of silhouette.", new ModeSetting.ModeCondition(this.mode, "Bloom"), true
   );
   public NumberSetting x = new NumberSetting(
      "BloomQuality", "Quality", "Glow sample step quality.", new ModeSetting.ModeCondition(this.mode, "Bloom"), 1, 1, 4
   );
   public NumberSetting y = new NumberSetting(
      "BloomMultiplier", "Multiplier", "Glow brightness multiplier.", new ModeSetting.ModeCondition(this.mode, "Bloom"), 1.0, 0.1, 5.0
   );
   public NumberSetting z = new NumberSetting(
      "BloomFillAlpha", "FillAlpha", "Alpha opacity of fill.", new ModeSetting.ModeCondition(this.mode, "Bloom"), 100, 0, 100
   );
   public NumberSetting bloomOutlineAlpha = new NumberSetting(
      "BloomOutlineAlpha", "OutlineAlpha", "Alpha opacity of outline/glow.", new ModeSetting.ModeCondition(this.mode, "Bloom"), 100, 0, 100
   );
   public ModeSetting bloomFillMode = new ModeSetting(
      "BloomFillMode",
      "FillMode",
      "Fill pattern mode.",
      new ModeSetting.ModeCondition(this.mode, "Bloom"),
      "Color",
      new String[]{"Color", "Gradient", "Rainbow"}
   );
   public ColorSetting bloomGradientColor = new ColorSetting(
      "BloomGradientColor",
      "GradientColor",
      "Gradient target color.",
      new ModeSetting.ModeCondition(this.mode, "Bloom"),
      new ColorSetting.ColorValue(new Color(255, 0, 0, 255), false, false)
   );
   public NumberSetting bloomGradientFactor = new NumberSetting(
      "BloomGradientFactor", "GradientFactor", "Scale factor for gradient fill.", new ModeSetting.ModeCondition(this.mode, "Bloom"), 200.0, 10.0, 500.0
   );
   public ColorSetting color = new ColorSetting("Color", "The color that will be used for the fill rendering.", ColorUtil.a());
   public ModeSetting friends = new ModeSetting(
      "Friends", "The color that will be applied to friended entities.", "Default", new String[]{"Default", "Custom", "Sync"}
   );
   public ColorSetting friendColor = new ColorSetting(
      "FriendColor",
      "The color that will be used for the shader effect on friends.",
      new ModeSetting.ModeCondition(this.friends, "Custom"),
      new ColorSetting.ColorValue(new Color(85, 255, 255, ColorUtil.b().getColor().getAlpha()), false, false)
   );
   private static final String[] R = EspShaderRenderer.a;
   private static final String[] S = Arrays.copyOfRange(R, 1, R.length);
   public ModeSetting shader = new ModeSetting("Shader", "The animated shader that will be drawn on the target entities instead of a flat color.", "None", R);
   public NumberSetting shaderOpacity = new NumberSetting(
      "ShaderOpacity",
      "Opacity",
      "The opacity of the animated shader pattern itself, independent of Color's own alpha.",
      new ModeSetting.ModeCondition(this.shader, S),
      100,
      0,
      100
   );
   public NumberSetting shaderSpeed = new NumberSetting(
      "ShaderSpeed", "Speed", "The speed at which the shader animates.", new ModeSetting.ModeCondition(this.shader, S), 1.0F, 0.1F, 10.0F
   );
   public NumberSetting shaderStep = new NumberSetting(
      "ShaderStep", "Step", "The size of the gradient bands.", new ModeSetting.ModeCondition(this.shader, "Gradient"), 50.0F, 0.1F, 200.0F
   );
   public ColorSetting shaderColor1 = new ColorSetting(
      "ShaderColor1",
      "The first gradient color.",
      new ModeSetting.ModeCondition(this.shader, "Gradient"),
      new ColorSetting.ColorValue(new Color(255, 0, 255, 255), false, false)
   );
   public ColorSetting shaderColor2 = new ColorSetting(
      "ShaderColor2",
      "The second gradient color.",
      new ModeSetting.ModeCondition(this.shader, "Gradient"),
      new ColorSetting.ColorValue(new Color(255, 0, 0, 255), false, false)
   );
   public ColorSetting shaderColor3 = new ColorSetting(
      "ShaderColor3",
      "The third gradient color.",
      new ModeSetting.ModeCondition(this.shader, "Gradient"),
      new ColorSetting.ColorValue(new Color(0, 255, 0, 255), false, false)
   );
   public ColorSetting shaderColor4 = new ColorSetting(
      "ShaderColor4",
      "The fourth gradient color.",
      new ModeSetting.ModeCondition(this.shader, "Gradient"),
      new ColorSetting.ColorValue(new Color(0, 0, 255, 255), false, false)
   );
   public ColorSetting shaderGlowColor = new ColorSetting(
      "ShaderGlowColor",
      "GlowColor",
      "The color that the glow shader will be tinted with.",
      new ModeSetting.ModeCondition(this.shader, "Glowing"),
      new ColorSetting.ColorValue(new Color(255, 0, 255, 255), false, false)
   );
   public ImageSetting image = new ImageSetting(
      "Image", "Select", "Select an image from the images directory to project onto targets.", new ModeSetting.ModeCondition(this.shader, "Image"), "None"
   );

   @Override
   public void onEnable() {
      if (Night.customImageManager != null && this.image != null) {
         Night.customImageManager.setActiveImage(this.image.getValue());
      }
   }

   public boolean a(Entity var1) {
      if (var1 == null || mc.player == null) {
         return true;
      } else {
         return var1 == mc.player ? true : mc.player.distanceToSqr(var1) <= this.range.getValue().floatValue() * this.range.getValue().floatValue();
      }
   }

   public boolean b_bool_Entity(Entity var1) {
      if (!this.a(var1)) {
         return false;
      }

      if (this.players.getValue() && var1.getType() == EntityTypes.PLAYER) {
         return true;
      }

      if (this.hostiles.getValue() && var1.getType().getCategory() == MobCategory.MONSTER) {
         return true;
      }

      if (!this.animals.getValue()
         || var1.getType().getCategory() != MobCategory.CREATURE
            && var1.getType().getCategory() != MobCategory.WATER_CREATURE
            && var1.getType().getCategory() != MobCategory.WATER_AMBIENT
            && var1.getType().getCategory() != MobCategory.UNDERGROUND_WATER_CREATURE
            && var1.getType().getCategory() != MobCategory.AXOLOTLS) {
         if (this.ambient.getValue() && var1.getType().getCategory() == MobCategory.AMBIENT) {
            return true;
         }

         if (this.invisibles.getValue() && var1.isInvisible()) {
            return true;
         }

         if (!this.items.getValue() || var1.getType() != EntityTypes.ITEM && var1.getType() != EntityTypes.EXPERIENCE_BOTTLE) {
            if (this.crystals.getValue() && var1.getType() == EntityTypes.END_CRYSTAL) {
               return true;
            }

            if (!this.pearl.getValue() || !(var1 instanceof ThrownEnderpearl) && var1.getType() != EntityTypes.ENDER_PEARL) {
               if (this.boat.getValue() && var1 instanceof AbstractBoat) {
                  return true;
               } else {
                  return this.minecart.getValue() && var1 instanceof AbstractMinecart ? true : this.others.getValue();
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

   public boolean v() {
      return this.mode.getValue().equalsIgnoreCase("Outline") || this.mode.getValue().equalsIgnoreCase("Both");
   }

   public boolean w() {
      return this.mode.getValue().equalsIgnoreCase("Fill") || this.mode.getValue().equalsIgnoreCase("Both");
   }

   public int c(Entity var1) {
      return this.d(var1).getRGB() | 0xFF000000;
   }

   public Color d(Entity var1) {
      if (var1 instanceof Player var2 && Night.friendManager.isFriend(var2.getName().getString()) && !this.friends.getValue().equals("Sync")) {
         return this.friends.getValue().equals("Default") ? Night.friendManager.getFriendColor() : this.friendColor.getColor();
      } else {
         return this.color.getColor();
      }
   }

   public static int b_int_String(String var0) {
      return switch (var0) {
         case "Gradient" -> 1;
         case "Rainbow" -> 4;
         default -> 0;
      };
   }

   public static Identifier x() {
      ShadersModule var0 = Night.moduleManager.getModule(ShadersModule.class);
      if (var0.isEnabled()) {
         if (var0.mode.getValue().equalsIgnoreCase("Glow")) {
            return Identifier.fromNamespaceAndPath("night", "outline_glow");
         } else {
            return var0.mode.getValue().equalsIgnoreCase("Bloom")
               ? Identifier.fromNamespaceAndPath("night", "outline_bloom")
               : a(var0.w(), var0.v(), var0.color.getColor().getAlpha());
         }
      } else {
         PopChams var1 = Night.moduleManager.getModule(PopChams.class);
         if (!var1.isEnabled()) {
            return null;
         }

         boolean var2 = var1.mode.getValue().equalsIgnoreCase("Fill") || var1.mode.getValue().equalsIgnoreCase("Both");
         boolean var3 = var1.mode.getValue().equalsIgnoreCase("Outline") || var1.mode.getValue().equalsIgnoreCase("Both");
         return a(var2, var3, var1.fillColor.getColor().getAlpha());
      }
   }

   public static EspShaderRenderer.ShaderUniformConfig y() {
      ShadersModule var0 = Night.moduleManager.getModule(ShadersModule.class);
      if (var0 != null && var0.isEnabled()) {
         int var1 = EspShaderRenderer.a_int_String(var0.shader.getValue());
         float var2 = var0.color.getColor().getAlpha() / 255.0F;
         float var3 = var0.shaderOpacity.getValue().floatValue() / 100.0F;
         ColorModule var4 = Night.moduleManager != null ? Night.moduleManager.getModule(ColorModule.class) : null;
         float var5 = var4 != null ? var4.rainbowSpeed.getValue().floatValue() : 6.0F;
         float var6 = var4 != null ? var4.rainbowBrightness.getValue().floatValue() / 100.0F : 1.0F;
         float var7 = var4 != null && var4.rainbowScale != null ? var4.rainbowScale.getValue().floatValue() / 100.0F : 1.0F;
         float var8 = var1 == 1 ? var0.shaderStep.getValue().floatValue() : var7;
         float var9 = (float)(1000.0 / Math.max(200.0, 10500.0 - 500.0 * var5));
         float var10 = var1 != 0 && var1 != 2 && var1 != 10
            ? var0.shaderSpeed.getValue().floatValue()
            : var9 * (var1 == 2 ? var0.shaderSpeed.getValue().floatValue() : 1.0F);
         float var11 = (float)(System.currentTimeMillis() % 20000000L / 1000.0) * var10 * (var1 == 1 ? var8 : 1.0F);
         return var1 != 0
            ? new EspShaderRenderer.ShaderUniformConfig(
               var1,
               var11,
               var8,
               var3,
               var6,
               var1 == 7 ? var0.shaderGlowColor.getColor() : var0.color.getColor(),
               var0.shaderColor2.getColor(),
               var0.shaderColor3.getColor(),
               var0.shaderColor4.getColor()
            )
            : new EspShaderRenderer.ShaderUniformConfig(0, var11, var8, var2, var6, var0.color.getColor(), Color.WHITE, Color.WHITE, Color.WHITE);
      } else {
         return new EspShaderRenderer.ShaderUniformConfig(0, 0.0F, 1.0F, 1.0F, 1.0F, Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE);
      }
   }

   public static float z() {
      ShadersModule var0 = Night.moduleManager.getModule(ShadersModule.class);
      return var0.isEnabled() && var0.mode.getValue().equalsIgnoreCase("Glow") ? var0.glowIntensity.getValue().floatValue() / 100.0F : 0.0F;
   }

   public static Identifier a(boolean var0, boolean var1, int var2) {
      if (var1 && !var0) {
         return Identifier.fromNamespaceAndPath("night", "outline_outline");
      }

      int var3 = Math.max(20, Math.min(100, (int)Math.round(var2 / 255.0 * 5.0) * 20));
      String var4 = var0 && var1 ? "outline_both_" : "outline_fill_";
      return Identifier.fromNamespaceAndPath("night", var4 + var3);
   }
}
