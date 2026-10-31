package night.module.impl.visuals;

import java.awt.Color;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.events.EventTarget;
import night.events.impl.Render3DEvent;
import night.module.Module;
import night.module.ModuleInfo;
import night.module.access.FreecamAccess;
import night.module.impl.core.IgnoreNaked;
import night.setting.impl.BooleanSetting;
import night.setting.impl.ColorSetting;
import night.setting.impl.ModeSetting;
import night.util.color.ColorUtil;
import night.util.render.Render3DUtil;
import night.util.world.EntityUtil;

@ModuleInfo(name = "Tracers", description = "Renders a line showing where other players are located.", category = Module.Category.VISUALS)
public class Tracers extends Module {
   public BooleanSetting antiBot = new BooleanSetting("AntiBot", "Prevents bots from having arrow tracers rendered for them.", false);
   public ModeSetting mode = new ModeSetting("Mode", "The mode for the tracers color.", "Distance", new String[]{"Distance", "Custom"});
   public ColorSetting color = new ColorSetting("Color", "The color used for the fill rendering.", ColorUtil.c());

   @EventTarget
   public void a(Render3DEvent var1) {
      if (!this.nullCheck()) {
         if (mc.player != null) {
            FreecamAccess var2 = (FreecamAccess)Night.moduleManager.getModuleByName("Freecam");
            boolean var3 = var2 != null && var2.isEnabled();
            Vec3 var13;
            if (var3) {
               float var4 = var2.getCameraPitch();
               float var5 = var2.getCameraYaw();
               float var6 = var4 * (float) (Math.PI / 180.0);
               float var7 = -var5 * (float) (Math.PI / 180.0);
               float var8 = Mth.cos(var7);
               float var9 = Mth.sin(var7);
               float var10 = Mth.cos(var6);
               float var11 = Mth.sin(var6);
               Vec3 var12 = new Vec3(var9 * var10, -var11, var8 * var10);
               var13 = new Vec3(var2.getCameraX(), var2.getCameraY(), var2.getCameraZ()).add(var12);
            } else {
               Vec3 var14 = mc.gameRenderer.mainCamera().position();
               Vec3 var16 = Vec3.directionFromRotation(mc.gameRenderer.mainCamera().xRot(), mc.gameRenderer.mainCamera().yRot());
               var13 = var14.add(var16);
            }

            for (Player var17 : (Iterable<Player>)(Object)mc.level.players()) {
               if ((var17 != mc.player || var3)
                  && !EntityUtil.c_bool_Entity(var17)
                  && (!EntityUtil.a_bool_Player(var17) || !this.antiBot.getValue())
                  && (!Night.moduleManager.getModule(IgnoreNaked.class).isEnabled() || !EntityUtil.b_bool_Player(var17))) {
                  Vec3 var18 = EntityUtil.a_Vec3_Entity_float(var17, var1.getTickDelta());
                  Render3DUtil.a(Render3DUtil.h, var1.getMatrices(), var13, var18, this.a(var17, var13));
               }
            }
         }
      }
   }

   private Color a(Player var1, Vec3 var2) {
      if (Night.friendManager.isFriend(var1.getName().getString())) {
         return Night.friendManager.getFriendColor(this.color.getColor().getAlpha());
      }

      if (this.mode.getValue().equals("Custom")) {
         return this.color.getColor();
      }

      float var3 = Mth.clamp((float)Math.sqrt(var2.distanceToSqr(var1.getX(), var1.getY(), var1.getZ())), 0.0F, 80.0F);
      return new Color((80.0F - var3) / 80.0F, 1.0F - (80.0F - var3) / 80.0F, 0.0F, this.color.getColor().getAlpha() / 255.0F);
   }
}
