package night.gui.component;

import java.awt.Color;
import lombok.Generated;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Util;
import net.minecraft.util.Util.OS;
import night.Night;
import night.gui.ClickGuiScreen;
import night.util.math.TimerUtil;
import night.util.render.Render2DUtil;
import night.util.world.PacketAndRenderUtil;

public class SearchBarComponent {
   private String a = "";
   private boolean b = false;
   private boolean c = false;
   private int d = 0;
   private final TimerUtil e = new TimerUtil();
   private boolean f = true;
   private int g = 190;
   private int h = 16;

   public void a(GuiGraphicsExtractor var1, int var2, int var3, float var4) {
      if (this.b) {
         int var5 = Minecraft.getInstance().getWindow().getGuiScaledWidth();
         int var6 = Minecraft.getInstance().getWindow().getGuiScaledHeight();
         int var7 = (var5 - this.g) / 2;
         int var8 = var6 - this.h - 8;
         if (this.e.passedMs(400L)) {
            this.f = !this.f;
            this.e.reset();
         }

         Render2DUtil.a(var1, var7, var8, var7 + this.g, var8 + this.h, new Color(14, 13, 22, 230));
         Color var9 = this.c ? ClickGuiScreen.a(var8, 220) : ClickGuiScreen.a(var8, 90);
         Render2DUtil.b(var1, var7, var8, var7 + this.g, var8 + this.h, var9);
         Night.fontManager.drawShadowedText(var1, "\u2315", var7 + 5, var8 + 4, Color.WHITE);
         if (this.a.isEmpty() && !this.c) {
            String var10 = "[ESC]";
            Night.fontManager.drawShadowedText(var1, var10, var7 + this.g - Night.fontManager.getStringWidth(var10) - 4, var8 + 4, new Color(120, 120, 130));
         }

         if (this.a.isEmpty() && !this.c) {
            String var15 = "Search modules...";
            Night.fontManager.drawShadowedText(var1, var15, var7 + 18, var8 + 4, new Color(140, 140, 150));
         } else {
            String var11 = this.c && this.f ? "|" : "";
            String var12 = this.a.substring(0, this.d);
            String var13 = this.a.substring(this.d);
            String var14 = var12 + var11 + var13;
            Night.fontManager.drawShadowedText(var1, var14, var7 + 18, var8 + 4, Color.WHITE);
         }
      }
   }

   public boolean a(double var1, double var3, int var5) {
      if (!this.b) {
         return false;
      } else {
         int var6 = Minecraft.getInstance().getWindow().getGuiScaledWidth();
         int var7 = Minecraft.getInstance().getWindow().getGuiScaledHeight();
         int var8 = (var6 - this.g) / 2;
         int var9 = var7 - this.h - 6;
         if (var5 == 0 && var1 >= var8 && var1 <= var8 + this.g && var3 >= var9 && var3 <= var9 + this.h) {
            this.c = true;
            return true;
         } else {
            this.c = false;
            return false;
         }
      }
   }

   public boolean a(int var1, int var2, int var3) {
      if (this.b && this.c) {
         Minecraft.getInstance().getWindow();
         boolean var4 = PacketAndRenderUtil.a_bool_int(Util.getPlatform() == OS.OSX ? 343 : 341);
         if (var1 == 256) {
            if (!this.a.isEmpty()) {
               this.a = "";
               this.d = 0;
               return true;
            } else {
               this.b = false;
               this.c = false;
               return true;
            }
         } else if (var1 == 259) {
            if (!this.a.isEmpty() && this.d > 0) {
               this.a = this.a.substring(0, this.d - 1) + this.a.substring(this.d);
               this.d--;
            }

            return true;
         } else if (var1 == 261) {
            if (this.d < this.a.length()) {
               this.a = this.a.substring(0, this.d) + this.a.substring(this.d + 1);
            }

            return true;
         } else if (var1 == 263) {
            if (this.d > 0) {
               this.d--;
            }

            return true;
         } else if (var1 == 262) {
            if (this.d < this.a.length()) {
               this.d++;
            }

            return true;
         } else {
            if (var1 == 268) {
               this.d = 0;
               return true;
            }

            if (var1 == 269) {
               this.d = this.a.length();
               return true;
            }

            if (var4 && var1 == 86) {
               try {
                  String var5 = Minecraft.getInstance().keyboardHandler.getClipboard();
                  this.a = this.a.substring(0, this.d) + var5 + this.a.substring(this.d);
                  this.d = this.d + var5.length();
               } catch (Exception var6) {
               }

               return true;
            } else if (var4 && var1 == 65) {
               this.d = this.a.length();
               return true;
            } else {
               return true;
            }
         }
      } else {
         return false;
      }
   }

   public boolean a(char var1, int var2) {
      if (!this.b || !this.c) {
         return false;
      }

      if (Character.isISOControl(var1)) {
         return false;
      }

      this.a = this.a.substring(0, this.d) + var1 + this.a.substring(this.d);
      this.d++;
      return true;
   }

   public void a() {
      this.b = !this.b;
      if (this.b) {
         this.c = true;
         this.a = "";
         this.d = 0;
      } else {
         this.c = false;
      }
   }

   @Generated
   public String b() {
      return this.a;
   }

   @Generated
   public void a(String var1) {
      this.a = var1;
   }

   @Generated
   public void a(boolean var1) {
      this.b = var1;
   }

   @Generated
   public void b(boolean var1) {
      this.c = var1;
   }

   @Generated
   public void a(int var1) {
      this.d = var1;
   }
}
