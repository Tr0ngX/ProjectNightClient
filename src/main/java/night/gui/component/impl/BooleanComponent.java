package night.gui.component.impl;

import java.awt.Color;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import night.Night;
import night.gui.ClickGuiScreen;
import night.gui.component.CategoryFrame;
import night.gui.component.GuiComponent;
import night.module.impl.core.ClickGuiModule;
import night.setting.impl.BooleanSetting;
import night.util.animation.Animation;
import night.util.animation.EasingUtil;
import night.util.render.Render2DUtil;

public class BooleanComponent extends GuiComponent {
   private final BooleanSetting a;
   private final Animation b;
   private final Animation c;
   private final Animation d;
   private final Animation e;
   private boolean f = false;

   public BooleanComponent(BooleanSetting var1, CategoryFrame var2, int var3) {
      super(var1, var2, var3, var1.getDescription());
      this.a = var1;
      float var4 = var1.getValue() ? 1.0F : 0.0F;
      this.b = new Animation(var4, var4, 180, EasingUtil.Easing.DECELERATE);
      this.c = new Animation(150, EasingUtil.Easing.DECELERATE);
      this.d = new Animation(var4, var4, 150, EasingUtil.Easing.DECELERATE);
      this.e = new Animation(0.0F, 0.0F, Animation.a_int_int(220), EasingUtil.Easing.DECELERATE);
   }

   public void b(boolean var1) {
      this.f = var1;
   }

   public void a() {
      this.e.setValue(0.0F);
      this.e.setTarget(0.0F);
      this.e.b(Animation.a_int_int(220));
      this.e.a_void_long(System.currentTimeMillis());
   }

   @Override
   public void render(GuiGraphicsExtractor var1, int var2, int var3, float var4) {
      if (!this.f) {
         this.a();
         this.f = true;
      }

      boolean var5 = this.isHovered(var2, var3);
      float var6 = this.c.animateTo(var5 ? 1.0F : 0.0F);
      float var7 = this.b.animateTo(this.a.getValue() ? 1.0F : 0.0F);
      float var8 = this.d.animateTo(!this.a.getValue() && !var5 ? 0.0F : 1.0F);
      float var9 = this.e.animateTo(1.0F);
      int var10 = this.getX() + this.getHorizontalPadding() + 1;
      int var11 = this.getX() + this.getWidth() - this.getHorizontalPadding() - 1;
      int var12 = var10 + Math.round((var11 - var10) * var9);
      if (var12 > var10) {
         var1.enableScissor(var10, this.getY(), var12, this.getY() + this.getHeight());
         Render2DUtil.a(var1, var10, this.getY(), var12, this.getY() + this.getHeight() - 1, new Color(0, 0, 0, 40));
         if (var6 > 0.001F) {
            Color var13 = new Color(255, 255, 255, (int)(20.0F * var6));
            Render2DUtil.a(var1, var10, this.getY(), var12, this.getY() + this.getHeight() - 1, var13);
         }

         if (var7 > 0.001F) {
            int var20 = Math.min(var12, var10 + Math.round((var11 - var10) * var7));
            Color var14 = ClickGuiScreen.a(this.getY(), Math.round(100.0F * var7));
            Render2DUtil.a(var1, var10, this.getY(), var20, this.getY() + this.getHeight() - 1, var14);
         }

         float var21 = this.getX() + this.getTextOffset() + 3 + var8 * 1.5F;
         this.beginHoverTransform(var1, var2, var3);
         Night.fontManager
            .drawShadowedText(var1, (this.a.getValue() ? "" : ChatFormatting.GRAY) + this.a.getDisplayName(), (int)var21, this.getY() + 2, Color.WHITE);
         this.endHoverTransform(var1);
         ClickGuiModule var22 = Night.moduleManager.getModule(ClickGuiModule.class);
         if (var22 != null && var22.switches.getValue()) {
            int var15 = var11 - 14 - 1;
            int var16 = this.getY() + (this.getHeight() - 7) / 2;
            Color var17 = this.a.getValue() ? ClickGuiScreen.a(this.getY(), Math.round(180.0F * var7)) : new Color(40, 40, 50, 160);
            Render2DUtil.a(var1, var15, var16, var15 + 14, var16 + 7, var17);
            Render2DUtil.b(var1, var15, var16, var15 + 14, var16 + 7, new Color(0, 0, 0, 80));
            int var18 = var15 + Math.round(var7 * (14 - 5));
            Color var19 = this.a.getValue() ? Color.WHITE : new Color(180, 180, 190);
            Render2DUtil.a(var1, var18, var16 - 1, var18 + 5, var16 + 7 + 1, var19);
         }

         var1.disableScissor();
      }
   }

   @Override
   public void mouseClicked(double var1, double var3, int var5) {
      if (this.isHovered(var1, var3) && var5 == 0) {
         this.a.setValue(!this.a.getValue());
         this.playClickSound();
      }
   }
}
