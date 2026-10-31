package night.gui.component.impl;

import java.awt.Color;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import night.Night;
import night.gui.ImageSelectorScreen;
import night.gui.component.CategoryFrame;
import night.gui.component.GuiComponent;
import night.setting.impl.ImageSetting;
import night.util.animation.Animation;
import night.util.animation.EasingUtil;
import night.util.render.Render2DUtil;

public class ImageSettingComponent extends GuiComponent {
   private final ImageSetting a;
   private final Animation b = new Animation(150, EasingUtil.Easing.DECELERATE);
   private final Animation c = new Animation(150, EasingUtil.Easing.DECELERATE);

   public ImageSettingComponent(ImageSetting var1, CategoryFrame var2, int var3) {
      super(var1, var2, var3, var1.getDescription());
      this.a = var1;
   }

   @Override
   public void render(GuiGraphicsExtractor var1, int var2, int var3, float var4) {
      boolean var5 = this.isHovered(var2, var3);
      float var6 = this.b.animateTo(var5 ? 1.0F : 0.0F);
      float var7 = this.c.animateTo(var5 ? 1.0F : 0.0F);
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

      float var14 = this.getX() + this.getTextOffset() + 3 + var7 * 1.5F;
      this.beginHoverTransform(var1, var2, var3);
      Night.fontManager.drawShadowedText(var1, this.a.getDisplayName(), (int)var14, this.getY() + 2, Color.WHITE);
      String var9 = this.a.getValue();
      if (var9 == null || var9.isEmpty() || var9.equalsIgnoreCase("None")) {
         var9 = "[Select...]";
      }

      String var10 = var9;
      int var11 = this.getWidth() - Night.fontManager.getStringWidth(this.a.getDisplayName()) - this.getTextOffset() * 2 - 15;
      if (Night.fontManager.getStringWidth((String)var10) > var11) {
         while (Night.fontManager.getStringWidth(var10 + "...") > var11 && var10.length() > 3) {
            var10 = var10.substring(0, var10.length() - 1);
         }

         var10 = var10 + "...";
      }

      String var12 = ChatFormatting.GRAY + var10;
      int var13 = this.getX() + this.getWidth() - this.getTextOffset() - 1;
      Night.fontManager.drawShadowedText(var1, var12, var13 - Night.fontManager.getStringWidth(var12), this.getY() + 2, Color.LIGHT_GRAY);
      this.endHoverTransform(var1);
   }

   @Override
   public void mouseClicked(double var1, double var3, int var5) {
      if (this.isHovered(var1, var3) && (var5 == 0 || var5 == 1)) {
         Minecraft var6 = Minecraft.getInstance();
         var6.gui.setScreen(new ImageSelectorScreen(this.a, var6.gui.screen()));
         this.playClickSound();
      }
   }
}
