package night.module.impl.misc;

import java.awt.Color;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.events.EventTarget;
import night.events.impl.PacketReceiveEvent;
import night.events.impl.Render2DEvent;
import night.manager.FontManager;
import night.module.Module;
import night.module.ModuleInfo;
import night.setting.impl.BooleanSetting;
import night.setting.impl.ColorSetting;
import night.util.color.ColorUtil;
import night.util.math.MathUtil;
import night.util.math.TimerUtil;

@ModuleInfo(name = "LagNotify", description = "Notifies you when you lag.", category = Module.Category.MISCELLANEOUS)
public class LagNotify extends Module {
   BooleanSetting server = new BooleanSetting("Server", "Notifies you when the server stops responding.", true);
   BooleanSetting lagback = new BooleanSetting("Lagback", "Notifies you when you lagback.", true);
   ColorSetting color;
   Vec3 h;
   double i;
   long j;

   public LagNotify() {
      ColorSetting.ColorValue var1 = ColorUtil.c();
      this.color = new ColorSetting("Color", "The color of the notification text.", var1);
      this.h = null;
      long var2 = System.currentTimeMillis();
      this.j = var2;
   }

   @EventTarget
   public void a(PacketReceiveEvent var1) {
      if (!this.nullCheck()) {
         if (var1.getPacket() instanceof ClientboundPlayerPositionPacket var3) {
            double var4 = var3.change().position().x();
            double var6 = var3.change().position().y();
            double var8 = var3.change().position().z();
            this.h = new Vec3(var4, var6, var8);
            Vec3 var10 = mc.player.position();
            Vec3 var11 = this.h;
            double var12 = var10.distanceTo(var11);
            this.i = var12;
            long var14 = System.currentTimeMillis();
            this.j = var14;
         }
      }
   }

   @EventTarget
   public void a(Render2DEvent var1) {
      if (!this.nullCheck()) {
         int var2 = mc.getWindow().getGuiScaledWidth() / 2;
         int var3 = mc.getWindow().getGuiScaledHeight() / 4;
         boolean var4 = false;
         if (this.server.getValue()) {
            TimerUtil var5 = Night.serverManager.getPacketTimer();
            Integer var6 = 1000;
            if (var5.passedMs(var6)) {
               String var7 = "Detected server not responding for "
                  + MathUtil.a_double_double_int((float)Night.serverManager.getPacketTimer().getElapsedMs() / 1000.0F, 1)
                  + "s.";
               GuiGraphicsExtractor var8 = var1.getContext();
               String var9 = var7;
               int var10 = var2;
               String var11 = var7;
               int var12 = Night.fontManager.getStringWidth(var11) / 2;
               int var13 = var10 - var12;
               int var14 = var3;
               int var15 = Night.fontManager.getFontHeight();
               int var16 = var14 - var15;
               Color var17 = this.color.getColor();
               Night.fontManager.drawShadowedText(var8, var9, var13, var16, var17);
               var4 = true;
            }
         }

         if (this.lagback.getValue()) {
            long var18 = System.currentTimeMillis();
            long var20 = this.j;
            long var22 = var18 - var20;
            long var24 = 3000L;
            if (var22 < var24) {
               double var26 = MathUtil.a_double_double_int(this.i, 1);
               long var28 = System.currentTimeMillis();
               long var30 = this.j;
               double var32 = MathUtil.a_double_double_int((float)(var28 - var30) / 1000.0F, 1);
               String var34 = "Detected lagback of " + var26 + " blocks " + var32 + "s.";
               FontManager var35 = Night.fontManager;
               GuiGraphicsExtractor var36 = var1.getContext();
               String var37 = var34;
               int var38 = var2;
               String var39 = var34;
               int var40 = Night.fontManager.getStringWidth(var39) / 2;
               int var41 = var38 - var40;
               int var42 = var3;
               int var43 = Night.fontManager.getFontHeight();
               int var44 = var42 - var43;
               int var45;
               if (var4) {
                  var45 = Night.fontManager.getFontHeight();
               } else {
                  var45 = 0;
               }

               int var46 = var44 + var45;
               Color var47 = this.color.getColor();
               var35.drawShadowedText(var36, var37, var41, var46, var47);
            }
         }
      }
   }
}
