package night.module.impl.movement;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.Pos;
import night.Night;
import night.events.EventTarget;
import night.events.impl.MotionSyncEvent;
import night.events.impl.UpdateWalkingPlayerEvent;
import night.module.Module;
import night.module.ModuleInfo;
import night.setting.impl.BooleanSetting;
import night.setting.impl.ModeSetting;
import night.setting.impl.NumberSetting;

@ModuleInfo(name = "Step", description = "Gives you the ability to instantly climb over a customizable amount of blocks.", category = Module.Category.MOVEMENT)
public class Step extends Module {
   public ModeSetting mode;
   public NumberSetting height;
   public BooleanSetting useTimer;
   private boolean persistent;

   public Step() {
      String[] var1 = new String[]{"Vanilla", "NCP"};
      this.mode = new ModeSetting("Mode", "The mode that will be used to climb over blocks.", "Vanilla", var1);
      Float var2 = 2.0F;
      Float var3 = 0.0F;
      Float var4 = 12.0F;
      this.height = new NumberSetting("Height", "The maximum height at which blocks can be climbed over.", var2, var3, var4);
      ModeSetting var6 = this.mode;
      String[] var7 = new String[]{"NCP"};
      ModeSetting.ModeCondition var5 = new ModeSetting.ModeCondition(var6, var7);
      this.useTimer = new BooleanSetting("UseTimer", "Uses timer to slow you down while climbing.", var5, true);
      this.persistent = false;
   }

   @Override
   public void onDisable() {
      Night.worldStateManager.setTimerMultiplier(1.0F);
   }

   @EventTarget
   public void a_void_UpdateWalkingPlayerEvent(UpdateWalkingPlayerEvent var1) {
      if (this.persistent) {
         Night.worldStateManager.setTimerMultiplier(1.0F);
         this.persistent = false;
      }
   }

   @EventTarget
   public void a_void_MotionSyncEvent(MotionSyncEvent var1) {
      if (mc.player != null && mc.level != null) {
         if (this.mode.getValue().equalsIgnoreCase("NCP")) {
            double var2 = mc.player.getY();
            double var4 = mc.player.yo;
            double var6 = var2 - var4;
            double var8 = 0.75;
            if (!(var6 <= var8)) {
               double var10 = var6;
               double var12 = this.height.getValue().doubleValue();
               if (!(var10 > var12)) {
                  double var14 = var6;
                  double[] var16 = this.a_doubleArr_double(var14);
                  if (var16 != null && var16.length > 1) {
                     if (this.useTimer.getValue()) {
                        float var17 = var16.length;
                        float var18 = 1.0F / var17;
                        Night.worldStateManager.setTimerMultiplier(var18);
                        this.persistent = true;
                     }

                     double[] var19 = var16;
                     int var20 = var19.length;
                     int var21 = 0;

                     while (true) {
                        int var22 = var21;
                        int var23 = var20;
                        if (var22 >= var23) {
                           break;
                        }

                        double[] var24 = var19;
                        int var25 = var21;
                        double var26 = var24[var25];
                        ClientPacketListener var28 = mc.getConnection();
                        double var29 = mc.player.xo;
                        double var31 = mc.player.yo;
                        double var33 = var26;
                        double var35 = var31 + var33;
                        double var37 = mc.player.zo;
                        boolean var39 = mc.player.horizontalCollision;
                        var28.send(new Pos(var29, var35, var37, false, var39));
                        var21++;
                     }
                  }

                  return;
               }
            }
         }
      }
   }

   @Override
   public String getDisplayInfo() {
      return String.valueOf(this.height.getValue().floatValue());
   }

   public double[] a_doubleArr_double(double var1) {
      double var3 = var1;
      double var5 = 10000.0;
      double[] var7;
      switch ((int)(var3 * var5)) {
         case 7500:
         case 10000:
            var7 = new double[2];
            double var40 = 0.42;
            var7[0] = var40;
            double var42 = 0.753;
            var7[1] = var42;
            break;
         case 8125:
         case 8750:
            var7 = new double[2];
            double var8 = 0.39;
            var7[0] = var8;
            double var10 = 0.7;
            var7[1] = var10;
            break;
         case 15000:
            var7 = new double[6];
            double var12 = 0.42;
            var7[0] = var12;
            double var14 = 0.75;
            var7[1] = var14;
            double var16 = 1.0;
            var7[2] = var16;
            double var18 = 1.16;
            var7[3] = var18;
            double var20 = 1.23;
            var7[4] = var20;
            double var22 = 1.2;
            var7[5] = var22;
            break;
         case 20000:
            var7 = new double[8];
            double var24 = 0.42;
            var7[0] = var24;
            double var26 = 0.78;
            var7[1] = var26;
            double var28 = 0.63;
            var7[2] = var28;
            double var30 = 0.51;
            var7[3] = var30;
            double var32 = 0.9;
            var7[4] = var32;
            double var34 = 1.21;
            var7[5] = var34;
            double var36 = 1.45;
            var7[6] = var36;
            double var38 = 1.43;
            var7[7] = var38;
            break;
         default:
            var7 = null;
      }

      return var7;
   }
}
