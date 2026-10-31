package night.gui.component.impl;

import java.awt.Color;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.util.Util.OS;
import night.Night;
import night.gui.ClickGuiScreen;
import night.gui.component.CategoryFrame;
import night.gui.component.GuiComponent;
import night.module.impl.core.ColorModule;
import night.setting.impl.ColorSetting;
import night.util.color.ColorUtil;
import night.util.render.Render2DUtil;
import night.util.world.PacketAndRenderUtil;

public class ColorComponent extends GuiComponent {
   private final ColorSetting a;
   private boolean b = false;
   private boolean c = false;
   private boolean d = false;
   private boolean e = false;
   private boolean f = false;
   private boolean g = false;
   private boolean h = false;
   private boolean visible = false;
   private boolean j = false;
   private boolean k = false;
   private boolean l = false;
   private boolean m = false;
   private boolean n = false;
   private String o = "";
   private int p = 0;
   private int q = 84;
   private int r = 0;
   private float[] s;

   public ColorComponent(ColorSetting var1, CategoryFrame var2, int var3) {
      super(var1, var2, var3, var1.getDescription());
      this.a = var1;
      this.s = Color.RGBtoHSB(var1.getColor().getRed(), var1.getColor().getGreen(), var1.getColor().getBlue(), null);
   }

   @Override
   public void render(GuiGraphicsExtractor var1, int var2, int var3, float var4) {
      if (!this.k && !this.l && !this.n) {
         Color var5 = this.a.getColorValue().getColor();
         this.s = Color.RGBtoHSB(var5.getRed(), var5.getGreen(), var5.getBlue(), null);
      }

      Color var22 = Color.BLACK;
      Color var6 = Color.getHSBColor(this.s[0], 1.0F, 1.0F);
      Render2DUtil.a(
         var1,
         this.getX() + this.getHorizontalPadding() + 1,
         this.getY(),
         this.getX() + this.getWidth() - this.getHorizontalPadding() - 1,
         this.getY() + this.getParentFrame().getHeaderHeight() - 1,
         new Color(0, 0, 0, 40)
      );
      this.beginHoverTransform(var1, var2, var3);
      Night.fontManager.drawShadowedText(var1, this.a.getDisplayName(), this.getX() + this.getTextOffset() + 3, this.getY() + 2, Color.WHITE);
      this.endHoverTransform(var1);
      Render2DUtil.a(
         var1,
         this.getX() + this.getWidth() - this.getHorizontalPadding() - 9,
         this.getY() + 2,
         this.getX() + this.getWidth() - this.getHorizontalPadding() - 1,
         this.getY() + this.getParentFrame().getHeaderHeight() - 3,
         ColorUtil.a(this.a.getColor(), 255)
      );
      Render2DUtil.b(
         var1,
         this.getX() + this.getWidth() - this.getHorizontalPadding() - 9,
         this.getY() + 2,
         this.getX() + this.getWidth() - this.getHorizontalPadding() - 1,
         this.getY() + this.getParentFrame().getHeaderHeight() - 3,
         var22
      );
      if (this.b) {
         int var7 = this.getParentFrame().getHeaderHeight();
         int var8 = this.getX() + this.getHorizontalPadding() + 1;
         int var9 = this.getX() + this.getWidth() - this.getHorizontalPadding() - 1;
         int var10 = this.getY() + var7;
         int var11 = var10 + 13;
         Render2DUtil.a(var1, var8, var10, var9, var11, 4.0F, new Color(210, 210, 210), Color.WHITE);
         Render2DUtil.b(var1, var8, var10, var9, var11, var22);
         String var12 = Night.clickGui.f() ? "|" : "";
         if (this.n) {
            if (this.o.isEmpty()) {
               String var13 = this.a_String_Color_int(this.a.getColorValue().getColor(), this.a.getAlpha());
               Night.fontManager.drawText(var1, "Hex: " + var12, var8 + 3, var10 + 2, Color.BLACK);
               int var14 = Night.fontManager.getStringWidth("Hex: ");
               Night.fontManager.drawText(var1, var13, var8 + 3 + var14, var10 + 2, new Color(130, 130, 130));
            } else {
               int var27 = Math.max(0, Math.min(this.p, this.o.length()));
               String var30 = this.o.substring(0, var27);
               String var15 = this.o.substring(var27);
               String var16 = "Hex: " + var30 + var12 + var15;
               Night.fontManager.drawText(var1, var16, var8 + 3, var10 + 2, Color.BLACK);
            }
         } else {
            String var28 = "Hex: " + this.a_String_Color_int(this.a.getColorValue().getColor(), this.a.getAlpha());
            Night.fontManager.drawText(var1, var28, var8 + 3, var10 + 2, Color.BLACK);
         }

         this.c = this.a(var2, var3, var8, var10, var9, var11);
         var7 += 13 + 2;
         int var29 = Mth.clamp(var2 - this.getX() - this.getHorizontalPadding() - 1, 0, this.q);
         int var31 = Mth.clamp(var3 - this.getY() - var7, 0, this.q);
         float var32 = this.q * this.s[0];
         float var33 = this.q * this.s[1];
         float var17 = this.q * (1.0F - this.s[2]);
         float var18 = this.q * (this.a.getAlpha() / 255.0F);

         for (float var19 = 0.0F; var19 < this.q; var19 += 0.5F) {
            Render2DUtil.a(
               var1,
               this.getX() + this.getWidth() - this.getHorizontalPadding() - 9,
               this.getY() + var7 + var19,
               this.getX() + this.getWidth() - this.getHorizontalPadding() - 1,
               this.getY() + var7 + var19 + 0.5F,
               Color.getHSBColor(var19 / this.q, 1.0F, 1.0F)
            );
         }

         Render2DUtil.b(
            var1,
            this.getX() + this.getWidth() - this.getHorizontalPadding() - 9,
            this.getY() + var7,
            this.getX() + this.getWidth() - this.getHorizontalPadding() - 1,
            this.getY() + var7 + this.q,
            var22
         );
         this.d = this.a(
            var2,
            var3,
            this.getX() + this.getWidth() - this.getHorizontalPadding() - 9,
            this.getY() + var7,
            this.getX() + this.getWidth() - this.getHorizontalPadding() - 1,
            this.getY() + var7 + this.q
         );
         Render2DUtil.a(
            var1,
            this.getX() + this.getWidth() - this.getHorizontalPadding() - 10,
            this.getY() + var7 + var32 - 1.5F,
            this.getX() + this.getWidth() - this.getHorizontalPadding(),
            this.getY() + var7 + var32 + 1.5F,
            var22
         );
         Render2DUtil.a(
            var1,
            this.getX() + this.getWidth() - this.getHorizontalPadding() - 9,
            this.getY() + var7 + var32 - 0.5F,
            this.getX() + this.getWidth() - this.getHorizontalPadding() - 1,
            this.getY() + var7 + var32 + 0.5F,
            Color.WHITE
         );
         if (this.k) {
            this.a.setSync(false);
            this.s[0] = Math.clamp((float)var31 / this.q, 0.0F, 1.0F);
            Color var34 = new Color(Color.HSBtoRGB(this.s[0], this.s[1], this.s[2]));
            Color var20 = new Color(var34.getRed(), var34.getGreen(), var34.getBlue(), this.a.getAlpha());
            this.a.setColor(var20);
         }

         Render2DUtil.b(
            var1,
            this.getX() + this.getHorizontalPadding() + 1,
            this.getY() + var7,
            this.getX() + this.getHorizontalPadding() + 1 + this.q,
            this.getY() + var7 + this.q,
            Color.WHITE,
            var6
         );
         Render2DUtil.a(
            var1,
            this.getX() + this.getHorizontalPadding() + 1,
            this.getY() + var7,
            this.getX() + this.getHorizontalPadding() + 1 + this.q,
            this.getY() + var7 + this.q,
            new Color(0, 0, 0, 0),
            Color.BLACK
         );
         Render2DUtil.b(
            var1,
            this.getX() + this.getHorizontalPadding() + 1,
            this.getY() + var7,
            this.getX() + this.getHorizontalPadding() + 1 + this.q,
            this.getY() + var7 + this.q,
            var22
         );
         this.e = this.a(
            var2,
            var3,
            this.getX() + this.getHorizontalPadding() + 1,
            this.getY() + var7,
            this.getX() + this.getHorizontalPadding() + 1 + this.q,
            this.getY() + var7 + this.q
         );
         Render2DUtil.a(
            var1,
            this.getX() + this.getHorizontalPadding() + 1 + var33 - 1.5F,
            this.getY() + var7 + var17 - 1.5F,
            this.getX() + this.getHorizontalPadding() + 1 + var33 + 1.5F,
            this.getY() + var7 + var17 + 1.5F,
            var22
         );
         Render2DUtil.a(
            var1,
            this.getX() + this.getHorizontalPadding() + 1 + var33 - 0.5F,
            this.getY() + var7 + var17 - 0.5F,
            this.getX() + this.getHorizontalPadding() + 1 + var33 + 0.5F,
            this.getY() + var7 + var17 + 0.5F,
            Color.WHITE
         );
         if (this.l) {
            this.a.setSync(false);
            this.s[1] = Math.clamp((float)var29 / this.q, 0.0F, 1.0F);
            this.s[2] = Math.clamp(1.0F - (float)var31 / this.q, 0.0F, 1.0F);
            Color var35 = new Color(Color.HSBtoRGB(this.s[0], this.s[1], this.s[2]));
            Color var37 = new Color(var35.getRed(), var35.getGreen(), var35.getBlue(), this.a.getAlpha());
            this.a.setColor(var37);
         }

         var7 += this.q + 2;
         Render2DUtil.b(
            var1,
            this.getX() + this.getHorizontalPadding() + 1,
            this.getY() + var7,
            this.getX() + this.getHorizontalPadding() + 1 + this.q,
            this.getY() + var7 + 8,
            Color.BLACK,
            ColorUtil.a(this.a.getColor(), 255)
         );
         Render2DUtil.b(
            var1,
            this.getX() + this.getHorizontalPadding() + 1,
            this.getY() + var7,
            this.getX() + this.getHorizontalPadding() + 1 + this.q,
            this.getY() + var7 + 8,
            var22
         );
         this.f = this.a(
            var2,
            var3,
            this.getX() + this.getHorizontalPadding() + 1,
            this.getY() + var7,
            this.getX() + this.getHorizontalPadding() + 1 + this.q,
            this.getY() + var7 + 8
         );
         Render2DUtil.a(
            var1,
            this.getX() + this.getHorizontalPadding() + 1 + var18 - 1.5F,
            this.getY() + var7 - 1,
            this.getX() + this.getHorizontalPadding() + 1 + var18 + 1.5F,
            this.getY() + var7 + 9,
            var22
         );
         Render2DUtil.a(
            var1,
            this.getX() + this.getHorizontalPadding() + 1 + var18 - 0.5F,
            this.getY() + var7,
            this.getX() + this.getHorizontalPadding() + 1 + var18 + 0.5F,
            this.getY() + var7 + 8,
            Color.WHITE
         );
         if (this.m) {
            int var36 = Math.clamp((int)(255.0F * var29 / this.q), 0, 255);
            Color var38 = this.a.getColorValue().getColor();
            Color var21 = new Color(var38.getRed(), var38.getGreen(), var38.getBlue(), var36);
            this.a.setColor(var21);
         }

         var7 += 10;
         Render2DUtil.a(
            var1,
            this.getX() + this.getHorizontalPadding() + 1,
            this.getY() + var7,
            this.getX() + this.getWidth() / 2.0F - 0.5F,
            this.getY() + var7 + this.getParentFrame().getHeaderHeight(),
            ClickGuiScreen.a(this.getY(), 100)
         );
         Night.fontManager
            .drawShadowedText(
               var1,
               "Copy",
               this.getX() + this.getHorizontalPadding() + this.getWidth() / 4 - 1 - Night.fontManager.getStringWidth("Copy") / 2,
               this.getY() + var7 + 2,
               Color.WHITE
            );
         this.g = this.a(
            var2,
            var3,
            this.getX() + this.getHorizontalPadding() + 1,
            this.getY() + var7,
            this.getX() + this.getWidth() / 2.0F - 0.5F,
            this.getY() + var7 + this.getParentFrame().getHeaderHeight()
         );
         Render2DUtil.a(
            var1,
            this.getX() + this.getWidth() / 2.0F + 0.5F,
            this.getY() + var7,
            this.getX() + this.getWidth() - this.getHorizontalPadding() - 1,
            this.getY() + var7 + this.getParentFrame().getHeaderHeight(),
            ClickGuiScreen.a(this.getY(), 100)
         );
         Night.fontManager
            .drawShadowedText(
               var1,
               "Paste",
               this.getX() + this.getWidth() / 2 + this.getWidth() / 4 - 1 - Night.fontManager.getStringWidth("Paste") / 2,
               this.getY() + var7 + 2,
               Color.WHITE
            );
         this.h = this.a(
            var2,
            var3,
            this.getX() + this.getWidth() / 2.0F + 0.5F,
            this.getY() + var7,
            this.getX() + this.getWidth() - this.getHorizontalPadding() - 1,
            this.getY() + var7 + this.getParentFrame().getHeaderHeight()
         );
         var7 += this.getParentFrame().getHeaderHeight() + 1;
         if (this.b()) {
            if (this.a.isSync()) {
               Render2DUtil.a(
                  var1,
                  this.getX() + this.getHorizontalPadding() + 1,
                  this.getY() + var7,
                  this.getX() + this.getWidth() - this.getHorizontalPadding() - 1,
                  this.getY() + var7 + this.getParentFrame().getHeaderHeight(),
                  ClickGuiScreen.a(this.getY(), 100)
               );
            }

            Night.fontManager
               .drawShadowedText(
                  var1, "Sync", this.getX() + this.getWidth() / 2 - Night.fontManager.getStringWidth("Sync") / 2, this.getY() + var7 + 2, Color.WHITE
               );
            this.visible = this.a(
               var2,
               var3,
               this.getX() + this.getHorizontalPadding() + 1,
               this.getY() + var7,
               this.getX() + this.getWidth() - this.getHorizontalPadding() - 1,
               this.getY() + var7 + this.getParentFrame().getHeaderHeight()
            );
            var7 += this.getParentFrame().getHeaderHeight() + 1;
         } else {
            this.visible = false;
         }

         if (this.a.isRainbow()) {
            Render2DUtil.a(
               var1,
               this.getX() + this.getHorizontalPadding() + 1,
               this.getY() + var7,
               this.getX() + this.getWidth() - this.getHorizontalPadding() - 1,
               this.getY() + var7 + this.getParentFrame().getHeaderHeight(),
               ClickGuiScreen.a(this.getY(), 100)
            );
         }

         Night.fontManager
            .drawShadowedText(
               var1, "Rainbow", this.getX() + this.getWidth() / 2 - Night.fontManager.getStringWidth("Rainbow") / 2, this.getY() + var7 + 2, Color.WHITE
            );
         this.j = this.a(
            var2,
            var3,
            this.getX() + this.getHorizontalPadding() + 1,
            this.getY() + var7,
            this.getX() + this.getWidth() - this.getHorizontalPadding() - 1,
            this.getY() + var7 + this.getParentFrame().getHeaderHeight()
         );
      }
   }

   private String a_String_Color_int(Color var1, int var2) {
      return String.format("%02X%02X%02X%02X", var2, var1.getRed(), var1.getGreen(), var1.getBlue());
   }

   private Color a(String var1) {
      if (var1 == null) {
         return null;
      }

      String var2 = var1.trim().replace("#", "");

      try {
         if (var2.length() == 6) {
            int var8 = Integer.parseInt(var2.substring(0, 2), 16);
            int var9 = Integer.parseInt(var2.substring(2, 4), 16);
            int var10 = Integer.parseInt(var2.substring(4, 6), 16);
            return new Color(var8, var9, var10, this.a.getAlpha());
         }

         if (var2.length() == 8) {
            int var3 = Integer.parseInt(var2.substring(0, 2), 16);
            int var4 = Integer.parseInt(var2.substring(2, 4), 16);
            int var5 = Integer.parseInt(var2.substring(4, 6), 16);
            int var6 = Integer.parseInt(var2.substring(6, 8), 16);
            return new Color(var4, var5, var6, var3);
         }
      } catch (Exception var7) {
      }

      return null;
   }

   private void a() {
      if (this.o != null && !this.o.isEmpty()) {
         Color var1 = this.a(this.o);
         if (var1 != null) {
            this.a.setSync(false);
            this.a.setColor(var1);
            this.s = Color.RGBtoHSB(var1.getRed(), var1.getGreen(), var1.getBlue(), null);
         }
      }
   }

   @Override
   public void keyPressed(int var1, int var2, int var3) {
      if (this.n) {
         mc.getWindow();
         boolean var4 = PacketAndRenderUtil.a_bool_int(Util.getPlatform() == OS.OSX ? 343 : 341);
         if (var1 == 256 || var1 == 257) {
            this.a();
            this.n = false;
            this.o = "";
         } else if (var1 == 263) {
            if (this.p > 0) {
               this.p--;
            }
         } else if (var1 == 262) {
            if (this.p < this.o.length()) {
               this.p++;
            }
         } else if (var1 == 259) {
            if (this.p > 0 && !this.o.isEmpty()) {
               this.o = this.o.substring(0, this.p - 1) + this.o.substring(this.p);
               this.p--;
               this.a();
            }
         } else if (var1 == 261) {
            if (this.p < this.o.length()) {
               this.o = this.o.substring(0, this.p) + this.o.substring(this.p + 1);
               this.a();
            }
         } else {
            if (var4) {
               if (var1 == 86) {
                  try {
                     String var5 = mc.keyboardHandler.getClipboard();
                     if (var5 != null) {
                        var5 = var5.trim().replace("#", "").toUpperCase();
                        StringBuilder var6 = new StringBuilder();

                        for (char var10 : var5.toCharArray()) {
                           if (var10 >= '0' && var10 <= '9' || var10 >= 'A' && var10 <= 'F') {
                              var6.append(var10);
                           }
                        }

                        String var14 = var6.toString();
                        if (this.o.length() + var14.length() <= 8) {
                           this.o = this.o.substring(0, this.p) + var14 + this.o.substring(this.p);
                           this.p = this.p + var14.length();
                        } else {
                           this.o = var14.substring(0, Math.min(8, var14.length()));
                           this.p = this.o.length();
                        }

                        this.a();
                     }
                  } catch (Exception var12) {
                  }

                  return;
               }

               if (var1 == 67) {
                  try {
                     mc.keyboardHandler.setClipboard(this.o);
                  } catch (Exception var11) {
                  }

                  return;
               }
            }
         }
      }
   }

   @Override
   public void charTyped(char var1, int var2) {
      if (this.n) {
         char var3 = Character.toUpperCase(var1);
         if ((var3 >= '0' && var3 <= '9' || var3 >= 'A' && var3 <= 'F') && this.o.length() < 8) {
            this.o = this.o.substring(0, this.p) + var3 + this.o.substring(this.p);
            this.p++;
            this.a();
         }
      }
   }

   private boolean b() {
      ColorModule var1 = Night.moduleManager.getModule(ColorModule.class);
      return var1 == null || this.a != var1.color;
   }

   @Override
   public void mouseClicked(double var1, double var3, int var5) {
      if (this.isHovered(var1, var3) && var5 == 1) {
         this.b = !this.b;
         if (this.n) {
            this.a();
            this.n = false;
            this.o = "";
         }

         this.playClickSound();
      }

      if (var5 == 0) {
         if (this.b && this.c) {
            this.n = true;
            this.o = "";
            this.p = 0;
            this.playClickSound();
            return;
         }

         if (this.n) {
            this.a();
            this.n = false;
            this.o = "";
         }

         if (this.d) {
            this.k = true;
         }

         if (this.e) {
            this.l = true;
         }

         if (this.f) {
            this.m = true;
         }

         if (this.g) {
            Night.clickGui.a_void_Color(this.a.getColorValue().getColor());
            this.playClickSound();
         }

         if (this.h && Night.clickGui.g() != null) {
            Color var6 = Night.clickGui.g();
            this.a.setColor(var6);
            this.s = Color.RGBtoHSB(var6.getRed(), var6.getGreen(), var6.getBlue(), null);
            this.playClickSound();
         }

         if (this.b() && this.visible) {
            this.a.setSync(!this.a.isSync());
            this.playClickSound();
         }

         if (this.j) {
            this.a.setRainbow(!this.a.isRainbow());
            this.playClickSound();
         }
      }
   }

   @Override
   public void mouseReleased(double var1, double var3, int var5) {
      this.k = false;
      this.l = false;
      this.m = false;
   }

   @Override
   public int getHeight() {
      return this.b ? (this.b() ? 166 : 152) : this.getParentFrame().getHeaderHeight();
   }

   @Override
   public boolean isHovered(double var1, double var3) {
      return this.getX() + this.getHorizontalPadding() <= var1
         && this.getY() <= var3
         && this.getX() + this.getWidth() - this.getHorizontalPadding() > var1
         && this.getY() + this.getParentFrame().getHeaderHeight() > var3;
   }

   private boolean a(double var1, double var3, double var5, double var7, double var9, double var11) {
      return var5 <= var1 && var7 <= var3 && var9 > var1 && var11 > var3;
   }
}
