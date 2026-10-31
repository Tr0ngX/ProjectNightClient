package night.gui.component.impl;

import java.awt.Color;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import night.Night;
import night.gui.ClickGuiScreen;
import night.gui.component.CategoryFrame;
import night.gui.component.GuiComponent;
import night.setting.impl.BooleanSetting;
import night.setting.impl.CategorySetting;
import night.util.animation.Animation;
import night.util.animation.EasingUtil;
import night.util.render.Render2DUtil;

public class CategorySettingComponent extends GuiComponent {
   private final CategorySetting a;
   private final BooleanSetting b;
   private final Animation c;

   public CategorySettingComponent(CategorySetting var1, BooleanSetting var2, CategoryFrame var3, int var4) {
      super(var1, var3, var4, var1.getDescription());
      this.a = var1;
      this.b = var2;
      float var5 = var2 != null && var2.getValue() ? 1.0F : 0.0F;
      this.c = new Animation(var5, var5, 180, EasingUtil.Easing.DECELERATE);
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
      float var5 = this.c.animateTo(this.b != null && this.b.getValue() ? 1.0F : 0.0F);
      if (var5 > 0.001F) {
         int var6 = this.getX() + this.getHorizontalPadding() + 1 + Math.round((this.getWidth() - (this.getHorizontalPadding() + 1) * 2) * var5);
         Color var7 = ClickGuiScreen.a(this.getY(), Math.round(100.0F * var5));
         Render2DUtil.a(var1, this.getX() + this.getHorizontalPadding() + 1, this.getY(), var6, this.getY() + this.getHeight() - 1, var7);
      }

      Color var8 = this.b != null && this.b.getValue() ? Color.WHITE : ClickGuiScreen.a(this.getY(), 180);
      this.beginHoverTransform(var1, var2, var3);
      Night.fontManager.drawShadowedText(var1, this.a.getDisplayName(), this.getX() + this.getTextOffset() + 4, this.getY() + 2, var8);
      String var9 = this.a.isOpen() ? "..." : "+";
      Night.fontManager
         .drawShadowedText(
            var1, var9, this.getX() + this.getWidth() - this.getTextOffset() - 1 - Night.fontManager.getStringWidth(var9), this.getY() + 2, Color.WHITE
         );
      this.endHoverTransform(var1);
   }

   @Override
   public void mouseClicked(double var1, double var3, int var5) {
      if (this.isHovered(var1, var3)) {
         if (this.b != null && var5 == 0) {
            this.b.setValue(!this.b.getValue());
            this.playClickSound();
         } else if (var5 == 1 || this.b == null && var5 == 0) {
            this.a.setOpen(!this.a.isOpen());
            this.playClickSound();
         }
      }
   }
}
