package night.gui.component.impl;

import java.awt.Color;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;
import night.Night;
import night.gui.ClickGuiScreen;
import night.gui.component.CategoryFrame;
import night.gui.component.GuiComponent;
import night.setting.impl.ModeSetting;
import night.util.animation.Animation;
import night.util.animation.EasingUtil;
import night.util.render.Render2DUtil;

public class ModeComponent extends GuiComponent {
   private final ModeSetting a;
   private boolean b = false;
   private final Animation c = new Animation(Animation.a_int_int(200), EasingUtil.Easing.DECELERATE);
   private final Animation d = new Animation(150, EasingUtil.Easing.DECELERATE);
   private final Animation e = new Animation(150, EasingUtil.Easing.DECELERATE);
   private String f;
   private String g;
   private long h;

   public ModeComponent(ModeSetting var1, CategoryFrame var2, int var3) {
      super(var1, var2, var3, var1.getDescription());
      this.a = var1;
      this.f = var1.getValue();
   }

   private float a() {
      return this.c.animateTo(this.b ? 1.0F : 0.0F);
   }

   @Override
   public void render(GuiGraphicsExtractor var1, int var2, int var3, float var4) {
      boolean var5 = this.isHovered(var2, var3);
      float var6 = this.d.animateTo(var5 ? 1.0F : 0.0F);
      float var7 = this.e.animateTo(var5 ? 1.0F : 0.0F);
      Render2DUtil.a(
         var1,
         this.getX() + this.getHorizontalPadding() + 1,
         this.getY(),
         this.getX() + this.getWidth() - this.getHorizontalPadding() - 1,
         this.getY() + this.getParentFrame().getHeaderHeight() - 1,
         new Color(0, 0, 0, 40)
      );
      if (var6 > 0.001F) {
         Color var8 = new Color(255, 255, 255, (int)(20.0F * var6));
         Render2DUtil.a(
            var1,
            this.getX() + this.getHorizontalPadding() + 1,
            this.getY(),
            this.getX() + this.getWidth() - this.getHorizontalPadding() - 1,
            this.getY() + this.getParentFrame().getHeaderHeight() - 1,
            var8
         );
      }

      float var25 = this.getX() + this.getTextOffset() + 3 + var7 * 1.5F;
      this.beginHoverTransform(var1, var2, var3);
      Night.fontManager.drawShadowedText(var1, this.a.getDisplayName(), (int)var25, this.getY() + 2, Color.WHITE);
      String var9 = this.a.getValue();
      if (!var9.equals(this.f)) {
         this.g = this.f;
         this.f = (String)var9;
         this.h = System.currentTimeMillis();
      }

      float var10 = this.h == 0L ? 1.0F : EasingUtil.a(EasingUtil.a(this.h, 180), EasingUtil.Easing.DECELERATE);
      int var11 = this.getX() + this.getWidth() - this.getTextOffset() - 1;
      if (var10 < 1.0F && this.g != null) {
         int var12 = Math.round(var10 * 10.0F);
         int var13 = Math.round(255.0F * (1.0F - var10));
         String var14 = ChatFormatting.GRAY + this.g;
         Night.fontManager
            .drawShadowedText(var1, var14, var11 - Night.fontManager.getStringWidth(var14) - var12, this.getY() + 2, new Color(255, 255, 255, var13));
      }

      int var26 = Math.round((1.0F - var10) * 10.0F);
      int var27 = Math.round(255.0F * var10);
      String var28 = ChatFormatting.GRAY + var9;
      Night.fontManager
         .drawShadowedText(var1, var28, var11 - Night.fontManager.getStringWidth(var28) + var26, this.getY() + 2, new Color(255, 255, 255, var27));
      this.endHoverTransform(var1);
      float var15 = this.a();
      if (var15 > 0.001F) {
         int var16 = Mth.clamp(Math.round(this.a.getModes().size() * var15), 0, this.a.getModes().size());
         int var17 = var16 * this.getParentFrame().getHeaderHeight();
         int var18 = this.getY() + this.getParentFrame().getHeaderHeight();
         Render2DUtil.a(
            var1,
            this.getX() + this.getHorizontalPadding() + 2,
            var18,
            this.getX() + this.getWidth() - this.getHorizontalPadding() - 2,
            var18 + var17,
            new Color(10, 10, 15, 210)
         );
         Render2DUtil.a(
            var1,
            this.getX() + this.getHorizontalPadding() + 2,
            var18,
            this.getX() + this.getHorizontalPadding() + 4,
            var18 + var17,
            ClickGuiScreen.a(this.getY(), 160)
         );

         for (int var19 = 0; var19 < var16; var19++) {
            String var20 = this.a.getModes().get(var19);
            int var21 = var18 + var19 * this.getParentFrame().getHeaderHeight();
            boolean var22 = this.a.getValue().equals(var20);
            boolean var23 = var2 >= this.getX() + this.getHorizontalPadding()
               && var2 <= this.getX() + this.getWidth() - this.getHorizontalPadding()
               && var3 >= var21
               && var3 < var21 + this.getParentFrame().getHeaderHeight();
            if (var23) {
               Render2DUtil.a(
                  var1,
                  this.getX() + this.getHorizontalPadding() + 4,
                  var21,
                  this.getX() + this.getWidth() - this.getHorizontalPadding() - 2,
                  var21 + this.getParentFrame().getHeaderHeight() - 1,
                  new Color(255, 255, 255, 18)
               );
            }

            if (var22) {
               Render2DUtil.a(
                  var1,
                  this.getX() + this.getHorizontalPadding() + 4,
                  var21,
                  this.getX() + this.getWidth() - this.getHorizontalPadding() - 2,
                  var21 + this.getParentFrame().getHeaderHeight() - 1,
                  ClickGuiScreen.a(this.getY(), 45)
               );
            }

            Color var24 = var22 ? Color.WHITE : (var23 ? new Color(220, 220, 230) : new Color(150, 150, 160));
            Night.fontManager.drawShadowedText(var1, (var22 ? "\u2022 " : "  ") + var20, this.getX() + this.getTextOffset() + 5, var21 + 2, var24);
         }
      }
   }

   @Override
   public void mouseClicked(double var1, double var3, int var5) {
      if (this.isHovered(var1, var3)) {
         if (var5 == 0) {
            int var6 = this.a.getModes().indexOf(this.a.getValue());
            if (var6 < this.a.getModes().size() - 1) {
               this.a.setValue(this.a.getModes().get(var6 + 1));
            } else {
               this.a.setValue(this.a.getModes().get(0));
            }

            this.playClickSound();
         } else if (var5 == 1) {
            this.b = !this.b;
            this.playClickSound();
         }
      }

      if (this.b) {
         float var11 = this.a();
         int var7 = Mth.clamp(Math.round(this.a.getModes().size() * var11), 0, this.a.getModes().size());

         for (int var8 = 0; var8 < var7; var8++) {
            String var9 = this.a.getModes().get(var8);
            int var10 = this.getY() + this.getParentFrame().getHeaderHeight() + var8 * this.getParentFrame().getHeaderHeight();
            if (var1 >= this.getX() + this.getHorizontalPadding()
               && var1 <= this.getX() + this.getWidth() - this.getHorizontalPadding()
               && var3 >= var10
               && var3 < var10 + this.getParentFrame().getHeaderHeight()
               && var5 == 0) {
               this.a.setValue(var9);
               this.playClickSound();
            }
         }
      }
   }

   @Override
   public int getHeight() {
      return Math.round(this.getParentFrame().getHeaderHeight() + this.a.getModes().size() * this.getParentFrame().getHeaderHeight() * this.a());
   }
}
