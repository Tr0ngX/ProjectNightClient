package night.gui.component.impl;

import java.awt.Color;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import night.Night;
import night.gui.ClickGuiScreen;
import night.gui.component.CategoryFrame;
import night.gui.component.GuiComponent;
import night.setting.impl.KeybindSetting;
import night.setting.impl.SubBindSetting;
import night.util.animation.Animation;
import night.util.animation.EasingUtil;
import night.util.input.KeyNameUtil;
import night.util.render.Render2DUtil;

public class KeybindComponent extends GuiComponent {
   private final KeybindSetting a;
   private boolean b = false;
   private final Animation c = new Animation(150, EasingUtil.Easing.DECELERATE);
   private final Animation d = new Animation(150, EasingUtil.Easing.DECELERATE);
   private static int height = 0;

   public static boolean a() {
      return height > 0;
   }

   public KeybindComponent(KeybindSetting var1, CategoryFrame var2, int var3) {
      super(var1, var2, var3, var1.getDescription());
      this.a = var1;
   }

   @Override
   public void render(GuiGraphicsExtractor var1, int var2, int var3, float var4) {
      boolean var5 = this.isHovered(var2, var3);
      float var6 = this.c.animateTo(var5 ? 1.0F : 0.0F);
      float var7 = this.d.animateTo(this.b ? 1.0F : 0.0F);
      Render2DUtil.a(
         var1,
         this.getX() + this.getHorizontalPadding() + 1,
         this.getY(),
         this.getX() + this.getWidth() - this.getHorizontalPadding() - 1,
         this.getY() + this.getHeight() - 1,
         new Color(0, 0, 0, 40)
      );
      if (var6 > 0.001F || var7 > 0.001F) {
         int var8 = Math.min(255, (int)(20.0F * var6 + 50.0F * var7));
         Color var9 = this.b ? ClickGuiScreen.a(this.getY(), var8) : new Color(255, 255, 255, var8);
         Render2DUtil.a(
            var1,
            this.getX() + this.getHorizontalPadding() + 1,
            this.getY(),
            this.getX() + this.getWidth() - this.getHorizontalPadding() - 1,
            this.getY() + this.getHeight() - 1,
            var9
         );
      }

      String var13 = this.a.getMode().equals("Bind") ? this.a.getDisplayName() : this.a.getMode();
      float var14 = this.getX() + this.getTextOffset() + 3 + var6 * 1.5F;
      this.beginHoverTransform(var1, var2, var3);
      Night.fontManager.drawShadowedText(var1, var13, (int)var14, this.getY() + 2, Color.WHITE);
      String var10 = this.b ? "..." : KeyNameUtil.a_String_int(this.a.getKey());
      Color var11 = this.b ? Color.YELLOW : Color.WHITE;
      int var12 = this.getX() + this.getWidth() - this.getTextOffset() - 1 - Night.fontManager.getStringWidth((String)var10) - (int)(var7 * 2.0F);
      Night.fontManager.drawShadowedText(var1, (this.b ? ChatFormatting.YELLOW : ChatFormatting.GRAY) + var10, var12, this.getY() + 2, var11);
      this.endHoverTransform(var1);
   }

   @Override
   public void mouseClicked(double var1, double var3, int var5) {
      if (this.isHovered(var1, var3)) {
         if (this.b) {
            if (var5 >= 0 && var5 <= 4) {
               this.a.setKey(-var5 - 1);
               this.b();
               this.d();
            }
         } else {
            if (var5 == 0) {
               this.c();
               this.playClickSound();
            } else if (var5 == 1) {
               this.p();
               this.playClickSound();
            }
         }
      }
   }

   @Override
   public void keyPressed(int var1, int var2, int var3) {
      if (this.b) {
         if (var1 != 261 && var1 != 259 && var1 != 256) {
            this.a.setKey(var1);
            this.b();
            this.d();
         } else {
            this.a.setKey(0);
            this.b();
            this.d();
         }
      }
   }

   private void b() {
      KeybindSetting var1 = this.a;
      if (var1 instanceof SubBindSetting) {
         ((SubBindSetting)var1).notifyBindChange();
      }
   }

   private void c() {
      this.b = true;
      height++;
   }

   private void d() {
      if (this.b) {
         this.b = false;
         height = Math.max(0, height - 1);
      }
   }

   private void p() {
      switch (this.a.getMode()) {
         case "Bind":
            this.a.setMode("Hold");
            break;
         case "Hold":
            this.a.setMode("ReverseHold");
            break;
         case "ReverseHold":
            this.a.setMode("Bind");
      }
   }
}
