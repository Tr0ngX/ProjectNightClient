package night.gui.component.impl;

import java.awt.Color;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Util;
import net.minecraft.util.Util.OS;
import night.Night;
import night.gui.ClickGuiScreen;
import night.gui.component.CategoryFrame;
import night.gui.component.GuiComponent;
import night.setting.impl.StringSetting;
import night.util.render.Render2DUtil;
import night.util.world.PacketAndRenderUtil;

public class StringComponent extends GuiComponent {
   private final StringSetting a;
   private String b = "";
   private boolean c = false;
   private boolean d = false;
   private int height = 0;

   public StringComponent(StringSetting var1, CategoryFrame var2, int var3) {
      super(var1, var2, var3, var1.getDescription());
      this.a = var1;
   }

   @Override
   public void render(GuiGraphicsExtractor var1, int var2, int var3, float var4) {
      Render2DUtil.a(
         var1,
         this.getX() + this.getHorizontalPadding() + 1,
         this.getY(),
         this.getX() + this.getWidth() - this.getHorizontalPadding() - 1,
         this.getY() + this.getHeight() - 1,
         new Color(0, 0, 0, 40)
      );
      Render2DUtil.a(
         var1,
         this.getX() + this.getHorizontalPadding() + 1,
         this.getY(),
         this.getX() + this.getWidth() - this.getHorizontalPadding() - 1,
         this.getY() + this.getHeight() - 1,
         ClickGuiScreen.a(this.getY(), 100)
      );
      String var5 = this.b;
      if (this.c) {
         if (this.d) {
            var5 = this.b;
         } else {
            int var6 = Math.max(0, Math.min(this.height, this.b.length()));
            String var7 = this.b.substring(0, var6);
            String var8 = this.b.substring(var6);
            String var9 = Night.clickGui.f() ? "|" : " ";
            var5 = var7 + var9 + var8;
         }
      } else {
         var5 = this.a.getDisplayName() + " " + ChatFormatting.GRAY + this.a.getValue();
      }

      this.beginHoverTransform(var1, var2, var3);
      Night.fontManager
         .drawShadowedText(var1, var5, this.getX() + this.getTextOffset() + 1, this.getY() + 2, this.d ? ClickGuiScreen.a(this.getY(), 255) : Color.WHITE);
      this.endHoverTransform(var1);
   }

   @Override
   public void mouseClicked(double var1, double var3, int var5) {
      if (var5 == 0) {
         if (this.isHovered(var1, var3) && !this.c) {
            this.c = true;
            this.b = this.a.getValue();
            this.height = this.b.length();
            this.d = false;
            this.playClickSound();
         } else if (!this.isHovered(var1, var3)) {
            this.c = false;
            this.d = false;
         }
      }
   }

   @Override
   public void keyPressed(int var1, int var2, int var3) {
      if (this.c) {
         mc.getWindow();
         boolean var4 = PacketAndRenderUtil.a_bool_int(Util.getPlatform() == OS.OSX ? 343 : 341);
         if (var1 == 256) {
            this.d = false;
         } else if (var1 == 257) {
            this.a.setValue(this.b);
            this.d = false;
            this.c = false;
         } else if (var1 == 263 && !this.b.isEmpty()) {
            if (this.d) {
               this.d = false;
               this.height = 0;
            } else if (this.height > 0) {
               this.height--;
            }
         } else if (var1 == 262 && !this.b.isEmpty()) {
            if (this.d) {
               this.d = false;
               this.height = this.b.length();
            } else if (this.height < this.b.length()) {
               this.height++;
            }
         } else if (var1 == 259) {
            if (this.d) {
               this.b = "";
               this.height = 0;
               this.d = false;
            } else if (this.height > 0) {
               this.b = this.b.substring(0, this.height - 1) + this.b.substring(this.height);
               this.height--;
            }
         } else if (var1 == 261) {
            if (this.d) {
               this.b = "";
               this.height = 0;
               this.d = false;
            } else if (this.height < this.b.length()) {
               this.b = this.b.substring(0, this.height) + this.b.substring(this.height + 1);
            }
         } else {
            if (var4) {
               if (var1 == 86) {
                  try {
                     String var5 = mc.keyboardHandler.getClipboard();
                     if (this.d) {
                        this.b = var5;
                        this.height = this.b.length();
                        this.d = false;
                     } else {
                        this.b = this.b.substring(0, this.height) + var5 + this.b.substring(this.height);
                        this.height = this.height + var5.length();
                     }
                  } catch (Exception var6) {
                     Night.LOGGER.error("{}: Failed to process clipboard paste", var6.getClass().getName(), var6);
                  }

                  return;
               }

               if (var1 == 67 && this.d) {
                  try {
                     mc.keyboardHandler.setClipboard(this.b);
                  } catch (Exception var7) {
                     Night.LOGGER.error("{}: Failed to process clipboard change", var7.getClass().getName(), var7);
                  }

                  return;
               }

               if (var1 == 65 && !this.b.isEmpty()) {
                  this.d = true;
                  this.height = this.b.length();
               }
            }
         }
      }
   }

   @Override
   public void charTyped(char var1, int var2) {
      if (this.c) {
         if (!Character.isISOControl(var1)) {
            if (this.d) {
               this.b = String.valueOf(var1);
               this.height = 1;
               this.d = false;
            } else {
               this.b = this.b.substring(0, this.height) + var1 + this.b.substring(this.height);
               this.height++;
            }
         }
      }
   }
}
