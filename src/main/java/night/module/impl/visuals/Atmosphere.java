package night.module.impl.visuals;

import java.awt.Color;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import night.events.EventTarget;
import night.events.impl.TickEvent;
import night.module.Module;
import night.module.ModuleInfo;
import night.setting.impl.BooleanSetting;
import night.setting.impl.CategorySetting;
import night.setting.impl.ColorSetting;
import night.setting.impl.ModeSetting;
import night.setting.impl.NumberSetting;
import night.util.color.ColorUtil;

@ModuleInfo(name = "Atmosphere", description = "Modifies the world's atmosphere, such as time and color.", category = Module.Category.VISUALS)
public class Atmosphere extends Module {
   public BooleanSetting modifyTime = new BooleanSetting("ModifyTime", "Modifies the world's time.", true);
   public NumberSetting time = new NumberSetting(
      "Time", "The time that the world will be set to.", new BooleanSetting.BooleanCondition(this.modifyTime, true), 200, -200, 200
   );
   public BooleanSetting modifyFog = new BooleanSetting("ModifyFog", "Modifies certain things about the world's fog.", false);
   public NumberSetting fogStart = new NumberSetting(
      "FogStart", "The start value of the world's fog.", new BooleanSetting.BooleanCondition(this.modifyFog, true), 50, 0, 300
   );
   public NumberSetting fogEnd = new NumberSetting(
      "FogEnd", "The end value of the world's fog.", new BooleanSetting.BooleanCondition(this.modifyFog, true), 150, 0, 300
   );
   public ColorSetting fogColor = new ColorSetting(
      "FogColor", "Modifies the color of the world's fog.", new BooleanSetting.BooleanCondition(this.modifyFog, true), ColorUtil.c()
   );
   public BooleanSetting modifySky = new BooleanSetting("ModifySky", "Modifies the sky rendering.", false);
   public ModeSetting skyMode = new ModeSetting(
      "SkyMode", "The sky rendering mode.", new BooleanSetting.BooleanCondition(this.modifySky, true), "Vanilla", new String[]{"Vanilla", "Custom"}
   );
   public ColorSetting skyColor = new ColorSetting(
      "SkyColor",
      "The color of the vanilla sky.",
      new ModeSetting.ModeCondition(this.skyMode, "Vanilla"),
      new ColorSetting.ColorValue(new Color(0, 0, 0, 255), true, false)
   );
   public ModeSetting customSky = new ModeSetting(
      "CustomSky", "The custom sky shader to use.", new ModeSetting.ModeCondition(this.skyMode, "Custom"), "Nebula", new String[]{"Nebula", "Smoke"}
   );
   public NumberSetting intensity = new NumberSetting(
      "Intensity", "The intensity of the sky shader.", new ModeSetting.ModeCondition(this.skyMode, "Custom"), 1.0F, 0.0F, 10.0F
   );
   public NumberSetting speed = new NumberSetting(
      "Speed", "The speed of the sky shader animation.", new ModeSetting.ModeCondition(this.skyMode, "Custom"), 1.0F, 0.0F, 5.0F
   );
   public ColorSetting skyTint = new ColorSetting(
      "SkyTint",
      "The tint of the sky shader.",
      new ModeSetting.ModeCondition(this.skyMode, "Custom"),
      new ColorSetting.ColorValue(new Color(255, 255, 255), false, false)
   );
   public ModeSetting weather = new ModeSetting(
      "Weather",
      "Overrides the world's weather (visual only, not sent to the server).",
      "Unchanged",
      new String[]{"Unchanged", "Clear", "Rain", "Thunder", "Snow", "Dust"}
   );
   public CategorySetting starGlowCategory = new CategorySetting("StarGlow", "The category for the star glow effect.");
   public BooleanSetting starGlow = new BooleanSetting(
      "StarGlow", "Enabled", "Adds a soft glow to vanilla sky stars.", new CategorySetting.CategoryCondition(this.starGlowCategory), false
   );
   public NumberSetting starGlowIntensity = new NumberSetting(
      "StarGlowIntensity", "Intensity", "How strong the glow is.", new CategorySetting.CategoryCondition(this.starGlowCategory), 3.0F, 0.0F, 1000.0F
   );

   @EventTarget
   public void a(TickEvent var1) {
      if (mc.player != null && mc.level != null) {
         if (this.weather.getValue().equalsIgnoreCase("Snow") || this.weather.getValue().equalsIgnoreCase("Dust")) {
            boolean var2 = this.weather.getValue().equalsIgnoreCase("Snow");
            SimpleParticleType var3 = var2 ? ParticleTypes.SNOWFLAKE : ParticleTypes.MYCELIUM;

            for (int var4 = 0; var4 < 6; var4++) {
               double var5 = mc.player.getX() + (mc.player.getRandom().nextDouble() - 0.5) * 20.0;
               double var7 = mc.player.getY() + (var2 ? 8.0 + mc.player.getRandom().nextDouble() * 4.0 : mc.player.getRandom().nextDouble() * 6.0 - 1.0);
               double var9 = mc.player.getZ() + (mc.player.getRandom().nextDouble() - 0.5) * 20.0;
               mc.level.addParticle(var3, var5, var7, var9, 0.0, var2 ? -0.15 : 0.01, 0.0);
            }
         }
      }
   }
}
