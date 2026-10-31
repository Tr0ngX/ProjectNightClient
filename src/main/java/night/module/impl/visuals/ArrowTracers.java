package night.module.impl.visuals;

import java.awt.Color;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import night.events.EventTarget;
import night.events.impl.Render2DEvent;
import night.module.Module;
import night.module.ModuleInfo;
import night.setting.impl.BooleanSetting;
import night.setting.impl.ColorSetting;
import night.setting.impl.ModeSetting;
import night.setting.impl.NumberSetting;
import night.util.color.ColorUtil;
import night.util.render.Render2DUtil;
import night.util.render.Render3DUtil;
import night.util.world.EntityUtil;

@ModuleInfo(name = "ArrowTracers", description = "Renders arrows towards players who are off screen.", category = Module.Category.VISUALS)
public class ArrowTracers extends Module {
   public NumberSetting width = new NumberSetting("Width", "The width of the arrows being rendered.", 4.0F, 0.5F, 15.0F);
   public NumberSetting height = new NumberSetting("Height", "The height of the arrows being rendered.", 8.0F, 0.5F, 15.0F);
   public NumberSetting distance = new NumberSetting("Distance", "The distance that will be between the crosshair and the arrows.", 45, 5, 200);
   public BooleanSetting antiBot = new BooleanSetting("AntiBot", "Prevents bots from having arrow tracers rendered for them.", false);
   public BooleanSetting onScreen = new BooleanSetting("OnScreen", "Renders the arrow tracers even for players who are on screen.", false);
   public ModeSetting alpha = new ModeSetting("Alpha", "The alpha that will be used in the arrow rendering.", "Fade", new String[]{"Default", "Fade"});
   public NumberSetting fadeDistance = new NumberSetting(
      "FadeDistance", "The distance at which the arrows will start fading.", new ModeSetting.ModeCondition(this.alpha, "Fade"), 100.0F, 10.0F, 200.0F
   );
   public ModeSetting mode = new ModeSetting("Mode", "The rendering that will be applied to the arrows", "Both", new String[]{"Fill", "Outline", "Both"});
   public ColorSetting fillColor = new ColorSetting(
      "FillColor", "The color used for the fill rendering.", new ModeSetting.ModeCondition(this.mode, "Fill", "Both"), ColorUtil.c()
   );
   public ColorSetting outlineColor = new ColorSetting(
      "OutlineColor",
      "The color used for the outline rendering.",
      new ModeSetting.ModeCondition(this.mode, "Outline", "Both"),
      new ColorSetting.ColorValue(new Color(0, 0, 0), true, false)
   );

   @EventTarget
   public void a(Render2DEvent var1) {
      if (mc.player != null && mc.level != null) {
         GuiGraphicsExtractor var2 = var1.getContext();
         float var3 = mc.getWindow().getGuiScaledWidth() / 2.0F;
         float var4 = mc.getWindow().getGuiScaledHeight() / 2.0F;
         float var5 = this.distance.getValue().floatValue();
         float var6 = this.width.getValue().floatValue();
         float var7 = this.height.getValue().floatValue();
         Vec3 var8 = EntityUtil.a_Vec3_Entity_float(mc.player, var1.getTickDelta());

         for (Player var10 : (Iterable<Player>)(Object)mc.level.players()) {
            if (var10 != mc.player
               && !EntityUtil.c_bool_Entity(var10)
               && (!EntityUtil.a_bool_Player(var10) || !this.antiBot.getValue())
               && (this.onScreen.getValue() || !Render3DUtil.a_bool_AABB(var10.getBoundingBox()))) {
               Vec3 var11 = EntityUtil.a_Vec3_Entity_float(var10, var1.getTickDelta());
               double var12 = var11.x - var8.x;
               double var14 = Mth.wrapDegrees(Math.toDegrees(Math.atan2(var11.z - var8.z, var12)) - 90.0 - mc.player.getYRot());
               int var16 = (int)Mth.clamp(255.0F - 255.0F / this.fadeDistance.getValue().floatValue() * mc.player.distanceTo(var10), 80.0F, 255.0F);
               Color var17 = this.alpha.getValue().equalsIgnoreCase("Fade") ? ColorUtil.a(this.fillColor.getColor(), var16) : this.fillColor.getColor();
               Color var18 = this.alpha.getValue().equalsIgnoreCase("Fade") ? ColorUtil.a(this.outlineColor.getColor(), var16) : this.outlineColor.getColor();
               var2.pose().pushMatrix();
               var2.pose().translate(var3, var4);
               var2.pose().rotate((float)Math.toRadians(var14));
               var2.pose().translate(-var3, -var4);
               if (this.mode.getValue().equalsIgnoreCase("Fill") || this.mode.getValue().equalsIgnoreCase("Both")) {
                  Render2DUtil.c(var2, var3, var4 - var5, var6, var7, var17);
               }

               if (this.mode.getValue().equalsIgnoreCase("Outline") || this.mode.getValue().equalsIgnoreCase("Both")) {
                  Render2DUtil.d(var2, var3, var4 - var5, var6, var7, var18);
               }

               var2.pose().popMatrix();
            }
         }
      }
   }
}
