package night.module.impl.movement;

import net.minecraft.ChatFormatting;
import night.Night;
import night.events.EventTarget;
import night.events.impl.TickEvent;
import night.module.Module;
import night.module.ModuleInfo;
import night.module.access.SprintAccess;
import night.setting.impl.NumberSetting;
import night.util.world.EntityUtil;

@ModuleInfo(name = "TickShift", description = "Manipulates minecraft timer to give you a small speed boost.", category = Module.Category.MOVEMENT)
public class TickShift extends Module {
   public NumberSetting maxTicks;
   public NumberSetting delay;
   public NumberSetting speed;
   int h;
   int i;

   public TickShift() {
      Integer var1 = 1;
      Integer var2 = 1;
      Integer var3 = 40;
      this.maxTicks = new NumberSetting("MaxTicks", "The maximum amount of ticks that the boost will be charging for.", var1, var2, var3);
      Integer var4 = 4;
      Integer var5 = 1;
      Integer var6 = 10;
      this.delay = new NumberSetting("Delay", "The delay between each tick.", var4, var5, var6);
      Float var7 = 2.0F;
      Float var8 = 1.0F;
      Float var9 = 10.0F;
      this.speed = new NumberSetting("Speed", "The speed of charging each tick.", var7, var8, var9);
      this.h = 0;
      this.i = 0;
   }

   @Override
   public void onEnable() {
      if (!this.nullCheck()) {
         this.x();
      }
   }

   @Override
   public void onDisable() {
      if (!this.nullCheck()) {
         this.x();
      }
   }

   @EventTarget
   public void a(TickEvent var1) {
      if (!this.nullCheck()) {
         double var2 = mc.player.fallDistance;
         double var4 = 5.0;
         if (!(var2 >= var4)) {
            ElytraFly var6 = Night.moduleManager.getModule(ElytraFly.class);
            if (!var6.isEnabled() || !var6.mode.getValue().equalsIgnoreCase("Control") || !mc.player.isFallFlying()) {
               boolean var7;
               boolean var12;
               label57: {
                  var7 = ((SprintAccess)Night.moduleManager.getModuleByName("Sprint")).isInstantMode();
                  if (mc.player.xxa == 0.0F && mc.player.zza == 0.0F) {
                     double var8 = mc.player.fallDistance;
                     double var10 = 0.0;
                     if (var8 == var10) {
                        var12 = true;
                        break label57;
                     }
                  }

                  var12 = false;
               }

               boolean var13 = var12;
               if (!var7) {
                  boolean var14 = var13;
                  double var15 = EntityUtil.a_double_Entity_SpeedUnit(mc.player, EntityUtil.SpeedUnit.KILOMETERS);
                  double var17 = 5.0;
                  boolean var19;
                  if (var15 <= var17) {
                     var19 = true;
                  } else {
                     var19 = false;
                  }

                  var13 = var14 | var19;
               }

               if (var13) {
                  Night.worldStateManager.setTimerMultiplier(1.0F);
                  int var20 = this.i;
                  int var21 = this.delay.getValue().intValue();
                  if (var20 >= var21) {
                     int var22 = this.h;
                     int var23 = this.maxTicks.getValue().intValue();
                     if (var22 < var23) {
                        int var24 = this.h + 1;
                        this.h = var24;
                     }

                     this.i = 0;
                  }

                  int var25 = this.i + 1;
                  this.i = var25;
               } else if (this.h > 0) {
                  if (!Night.moduleManager.getModule(Speed.class).v() && !mc.options.keyJump.isDown()) {
                     float var26 = this.speed.getValue().floatValue();
                     Night.worldStateManager.setTimerMultiplier(var26);
                  }

                  int var27 = this.h - 1;
                  this.h = var27;
               } else {
                  this.x();
               }
            }
         }
      }
   }

   public int v() {
      return this.h;
   }

   public void b(int var1) {
      this.h = var1;
   }

   public boolean w() {
      boolean var1;
      if (this.isEnabled() && this.h > 0 && Night.worldStateManager.getTimerMultiplier() > 1.0F) {
         var1 = true;
      } else {
         var1 = false;
      }

      return var1;
   }

   public void x() {
      Night.worldStateManager.setTimerMultiplier(1.0F);
      this.h = 0;
      this.i = 0;
   }

   @Override
   public String getDisplayInfo() {
      int var1 = this.h;
      int var2 = this.maxTicks.getValue().intValue();
      String var3;
      if (var1 >= var2) {
         var3 = ChatFormatting.GREEN + "";
      } else {
         var3 = "";
      }

      int var4 = this.h;
      String var5 = String.valueOf(ChatFormatting.RESET);
      return var3 + var4 + var5;
   }
}
