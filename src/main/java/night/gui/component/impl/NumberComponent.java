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
import night.setting.impl.NumberSetting;
import night.setting.impl.RangeNumberSetting;
import night.util.animation.Animation;
import night.util.animation.EasingUtil;
import night.util.color.ChatColorUtil;
import night.util.math.MathUtil;
import night.util.render.Render2DUtil;
import night.util.world.PacketAndRenderUtil;

public class NumberComponent extends GuiComponent {
   private final NumberSetting a;
   private boolean b = false;
   private boolean c = false;
   private float d = -1.0F;
   private float e = -1.0F;
   private long f = 0L;
   private String g = "";
   private boolean h = false;
   private boolean visible = false;
   private final Animation hoverAnimation;
   private final Animation k;
   private boolean l = false;

   public NumberComponent(NumberSetting var1, CategoryFrame var2, int var3) {
      super(var1, var2, var3, var1.getDescription());
      this.a = var1;
      this.hoverAnimation = new Animation(0.0F, 0.0F, Animation.a_int_int(220), EasingUtil.Easing.DECELERATE);
      this.k = new Animation(0.0F, 0.0F, 220, EasingUtil.Easing.DECELERATE);
   }

   public void b(boolean var1) {
      this.l = var1;
   }

   public void a() {
      this.hoverAnimation.setValue(0.0F);
      this.hoverAnimation.setTarget(0.0F);
      this.hoverAnimation.b(Animation.a_int_int(220));
      this.hoverAnimation.a_void_long(System.currentTimeMillis());
      this.k.setValue(0.0F);
      this.k.setTarget(0.0F);
      this.k.b(220);
      this.k.a_void_long(System.currentTimeMillis());
   }

   @Override
   public void render(GuiGraphicsExtractor var1, int var2, int var3, float var4) {
      if (!this.l) {
         this.a();
         this.l = true;
      }

      double var5 = 0.0;
      double var7 = this.getWidth() - 2 - this.getHorizontalPadding() * 2;
      double var9 = Math.min(var7, Math.max(0, var2 - this.getX() - 1 - this.getHorizontalPadding()));
      double var15;
      if (this.a instanceof RangeNumberSetting var12) {
         int var25 = var12.getMinValue().intValue();
         int var13 = var12.getMaxValue().intValue() - var25;
         if (this.b) {
            int var14 = (int)MathUtil.a_double_double_int(var9 / var7 * var13 + var25, 0);
            if (this.c) {
               var12.setSecondValue(var14);
            } else {
               var12.setValue(var14);
            }
         }

         var5 = var7 * (var12.getFirstValue() - var25) / var13;
         var15 = var7 * (var12.getSecondValue() - var25) / var13;
      } else if (this.a.getNumberType() == NumberSetting.NumberType.INTEGER) {
         var15 = var7 * (this.a.getValue().intValue() - this.a.getMinValue().intValue()) / (this.a.getMaxValue().intValue() - this.a.getMinValue().intValue());
         if (this.b) {
            int var26 = (int)MathUtil.a_double_double_int(
               var9 / var7 * (this.a.getMaxValue().intValue() - this.a.getMinValue().intValue()) + this.a.getMinValue().intValue(), 0
            );
            int var32 = this.a.getStep().intValue();
            this.a.setValue(var32 > 0 ? Math.round((float)var26 / var32) * var32 : var26);
         }
      } else if (this.a.getNumberType() == NumberSetting.NumberType.FLOAT) {
         var15 = var7
            * (this.a.getValue().floatValue() - this.a.getMinValue().floatValue())
            / (this.a.getMaxValue().floatValue() - this.a.getMinValue().floatValue());
         if (this.b) {
            float var27 = (float)MathUtil.a_double_double_int(
               var9 / var7 * (this.a.getMaxValue().floatValue() - this.a.getMinValue().floatValue()) + this.a.getMinValue().floatValue(), 2
            );
            float var33 = this.a.getStep().floatValue();
            float var35 = var33 > 0.0F ? (float)MathUtil.a_double_double_int(Math.round(var27 / var33) * var33, 2) : var27;
            this.a.setValue(var35);
         }
      } else if (this.a.getNumberType() == NumberSetting.NumberType.DOUBLE) {
         var15 = var7
            * (this.a.getValue().doubleValue() - this.a.getMinValue().doubleValue())
            / (this.a.getMaxValue().doubleValue() - this.a.getMinValue().doubleValue());
         if (this.b) {
            double var28 = MathUtil.a_double_double_int(
               var9 / var7 * (this.a.getMaxValue().doubleValue() - this.a.getMinValue().doubleValue()) + this.a.getMinValue().doubleValue(), 2
            );
            double var36 = this.a.getStep().doubleValue();
            double var17 = var36 > 0.0 ? MathUtil.a_double_double_int(Math.round(var28 / var36) * var36, 2) : var28;
            this.a.setValue(var17);
         }
      } else {
         var15 = var7
            * (this.a.getValue().longValue() - this.a.getMinValue().longValue())
            / (this.a.getMaxValue().longValue() - this.a.getMinValue().longValue());
         if (this.b) {
            long var29 = (long)MathUtil.a_double_double_int(
               var9 / var7 * (this.a.getMaxValue().longValue() - this.a.getMinValue().longValue()) + this.a.getMinValue().longValue(), 0
            );
            this.a.setValue(var29);
         }
      }

      float var31 = this.hoverAnimation.animateTo(1.0F);
      int var30 = this.getX() + this.getHorizontalPadding() + 1;
      int var34 = this.getX() + this.getWidth() - this.getHorizontalPadding() - 1;
      int var37 = var30 + Math.round((var34 - var30) * var31);
      if (var37 > var30) {
         var1.enableScissor(var30, this.getY(), var37, this.getY() + this.getHeight());
         Render2DUtil.a(var1, var30, this.getY(), var37, this.getY() + this.getHeight() - 1, new Color(0, 0, 0, 45));
         if (this.isHovered(var2, var3)) {
            Render2DUtil.a(var1, var30, this.getY(), var37, this.getY() + this.getHeight() - 1, new Color(255, 255, 255, 12));
         }

         float var19 = (float)Math.max(0.0, Math.min(var7, var15));
         float var38;
         if (this.b) {
            long var20 = System.currentTimeMillis();
            float var22 = this.d < 0.0F ? 1.0F : Math.min(1.0F, (float)(var20 - this.f) / 40.0F);
            this.f = var20;
            this.d = (float)MathUtil.a(this.d < 0.0F ? var19 : this.d, var19, var22);
            this.e = (float)MathUtil.a(this.e < 0.0F ? var5 : this.e, var5, var22);
            var5 = this.e;
            this.k.setValue(this.d);
            this.k.setTarget(this.d);
            var38 = this.d;
         } else {
            this.d = -1.0F;
            this.e = -1.0F;
            var38 = this.k.animateTo(var19);
         }

         if (this.a instanceof RangeNumberSetting) {
            float var39 = var30 + (float)var5;
            Render2DUtil.a(
               var1, var39, this.getY(), Math.max(var39 + 2.0F, var30 + var38), this.getY() + this.getHeight() - 1, ClickGuiScreen.a(this.getY(), 95)
            );
         } else if (var38 > 0.5F) {
            Render2DUtil.a(var1, var30, this.getY(), var30 + var38, this.getY() + this.getHeight() - 1, ClickGuiScreen.a(this.getY(), 95));
         }

         boolean var40 = System.currentTimeMillis() / 500L % 2L == 0L;
         String var23 = this.visible ? "" : (var40 ? "|" : "");
         this.beginHoverTransform(var1, var2, var3);
         Night.fontManager
            .drawShadowedText(
               var1,
               this.h ? this.g + var23 : this.a.getDisplayName(),
               var30 + this.getTextOffset() - this.getHorizontalPadding() + 2,
               this.getY() + 2,
               Color.WHITE
            );
         if (!this.h) {
            NumberSetting var24 = this.a;
            String var41 = var24 instanceof RangeNumberSetting
               ? ((RangeNumberSetting)var24).getRangeString()
               : (this.a.isAllowZero() && this.a.getValue().doubleValue() == 0.0 ? "Ignore" : this.a.getValue() + "");
            Night.fontManager
               .drawShadowedText(
                  var1,
                  ChatFormatting.GRAY + var41,
                  var34 - this.getTextOffset() + this.getHorizontalPadding() - Night.fontManager.getStringWidth((String)var41),
                  this.getY() + 2,
                  Color.WHITE
               );
         }

         this.endHoverTransform(var1);
         var1.disableScissor();
      }
   }

   @Override
   public void mouseClicked(double var1, double var3, int var5) {
      if (this.isHovered(var1, var3) && var5 == 0) {
         this.b = true;
         if (this.a instanceof RangeNumberSetting var7) {
            double var10 = this.getWidth() - 2 - this.getHorizontalPadding() * 2;
            double var8 = (var1 - this.getX() - 1.0 - this.getHorizontalPadding()) / var10 * (var7.getMaxValue().intValue() - var7.getMinValue().intValue())
               + var7.getMinValue().intValue();
            this.c = var8 > (var7.getFirstValue() + var7.getSecondValue()) / 2.0;
         }
      }

      if (var5 == 1) {
         if (this.isHovered(var1, var3) && !this.h) {
            this.h = true;
            this.g = "";
         } else {
            this.h = false;
            this.visible = false;
         }
      }
   }

   @Override
   public void mouseReleased(double var1, double var3, int var5) {
      if (var5 == 0) {
         if (this.b) {
            this.playClickSound();
         }

         this.b = false;
      }
   }

   @Override
   public void keyPressed(int var1, int var2, int var3) {
      if (this.h) {
         if (PacketAndRenderUtil.a_bool_int(256)) {
            this.visible = false;
         } else if (PacketAndRenderUtil.a_bool_int(257)) {
            try {
               if (this.a instanceof RangeNumberSetting var5) {
                  var5.parseRange(this.g);
               } else {
                  switch (this.a.getNumberType()) {
                     case LONG:
                        this.a.setValue(Long.parseLong(this.g));
                        break;
                     case DOUBLE:
                        this.a.setValue(Double.parseDouble(this.g));
                        break;
                     case FLOAT:
                        this.a.setValue(Float.parseFloat(this.g));
                        break;
                     default:
                        this.a.setValue(Integer.parseInt(this.g));
                  }
               }
            } catch (NumberFormatException var6) {
               Night.chatManager
                  .sendWarning("Please input a valid " + ChatColorUtil.a() + this.a.getNumberType().name().toLowerCase() + ChatColorUtil.b() + " number.");
            }

            this.visible = false;
            this.h = false;
         } else if (PacketAndRenderUtil.a_bool_int(259)) {
            this.g = this.visible ? "" : (!this.g.isEmpty() ? this.g.substring(0, this.g.length() - 1) : this.g);
            this.visible = false;
         } else {
            if (PacketAndRenderUtil.a_bool_int(Util.getPlatform() == OS.OSX ? 343 : 341) && PacketAndRenderUtil.a_bool_int(65)) {
               this.visible = true;
            }
         }
      }
   }

   @Override
   public void charTyped(char var1, int var2) {
      if (this.h) {
         this.g = this.visible ? String.valueOf(var1) : this.g + var1;
         if (this.visible) {
            this.visible = false;
         }
      }
   }
}
