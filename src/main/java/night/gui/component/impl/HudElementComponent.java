package night.gui.component.impl;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import night.Night;
import night.gui.ClickGuiScreen;
import night.gui.component.CategoryFrame;
import night.gui.component.ExpandableComponent;
import night.gui.component.GuiComponent;
import night.manager.HudElementManager;
import night.module.impl.core.ClickGuiModule;
import night.setting.Setting;
import night.setting.impl.BooleanSetting;
import night.setting.impl.CategorySetting;
import night.setting.impl.ColorSetting;
import night.setting.impl.KeybindSetting;
import night.setting.impl.ModeSetting;
import night.setting.impl.NumberSetting;
import night.setting.impl.StringSetting;
import night.setting.impl.WhitelistSetting;
import night.util.animation.Animation;
import night.util.animation.EasingUtil;
import night.util.color.ColorUtil;
import night.util.render.NeekeriPatternShader;
import night.util.render.Render2DUtil;

public class HudElementComponent extends GuiComponent implements ExpandableComponent {
   private final HudElementManager.HudEntry a;
   private boolean b = false;
   private final ArrayList<GuiComponent> c = new ArrayList<>();
   private String d = "";
   private final Animation e = new Animation(Animation.a_int_int(180), EasingUtil.Easing.DECELERATE);
   private int horizontalPadding = 0;
   private final Animation g;

   @Override
   public boolean isExpanded() {
      return this.a.categorySetting() != null ? this.a.categorySetting().isOpen() : this.b;
   }

   @Override
   public float getExpandProgress() {
      return this.a.categorySetting() != null ? this.a.categorySetting().getAnimationProgress() : this.e.animateTo(this.b ? 1.0F : 0.0F);
   }

   @Override
   public String getSearchName() {
      return this.a.text();
   }

   @Override
   public List<GuiComponent> getSubComponents() {
      return this.c;
   }

   @Override
   public void setExpandedHeight(int var1) {
      this.horizontalPadding = var1;
   }

   @Override
   public void setSearchQuery(String var1) {
      this.d = var1;
   }

   public HudElementComponent(HudElementManager.HudEntry var1, List<Setting> var2, CategoryFrame var3, int var4) {
      super(var3, var4, var1.text());
      this.a = var1;
      float var5 = var1.enabledSetting().getValue() ? 1.0F : 0.0F;
      this.g = new Animation(var5, var5, 180, EasingUtil.Easing.DECELERATE);

      for (Setting var7 : (Iterable<Setting>)(Object)var2) {
         if (var7 instanceof BooleanSetting var8) {
            this.c.add(new BooleanComponent(var8, var3, var4));
         } else if (var7 instanceof NumberSetting var9) {
            this.c.add(new NumberComponent(var9, var3, var4));
         } else if (var7 instanceof CategorySetting var10) {
            this.c.add(new CategorySettingComponent(var10, null, var3, var4));
         } else if (var7 instanceof KeybindSetting var11) {
            this.c.add(new KeybindComponent(var11, var3, var4));
         } else if (var7 instanceof ModeSetting var12) {
            this.c.add(new ModeComponent(var12, var3, var4));
         } else if (var7 instanceof WhitelistSetting var13) {
            this.c.add(new WhitelistComponent(var13, var3, var4));
         } else if (var7 instanceof StringSetting var14) {
            this.c.add(new StringComponent(var14, var3, var4));
         } else if (var7 instanceof ColorSetting var15) {
            this.c.add(new ColorComponent(var15, var3, var4));
         }
      }
   }

   private void p() {
      int var1 = this.getY() + this.getHeight();

      for (GuiComponent var3 : (Iterable<GuiComponent>)(Object)this.c) {
         var3.setX(this.getX());
         var3.setY(var1);
         if (var3.isVisible()) {
            var1 += var3.getHeight();
         }
      }
   }

   @Override
   public void render(GuiGraphicsExtractor var1, int var2, int var3, float var4) {
      this.p();
      if (this.isHovered(var2, var3) && Night.clickGui.e().a().isEmpty()) {
         Night.clickGui.e().a(this.getDescription());
      }

      Color var5 = this.isHovered(var2, var3) ? new Color(255, 255, 255, 15) : new Color(0, 0, 0, 0);
      if (var5.getAlpha() > 0) {
         Render2DUtil.a(
            var1,
            this.getX() + this.getHorizontalPadding(),
            this.getY(),
            this.getX() + this.getWidth() - this.getHorizontalPadding(),
            this.getY() + this.getHeight() - 1,
            var5
         );
      }

      float var6 = this.g.animateTo(this.a.enabledSetting().getValue() ? 1.0F : 0.0F);
      if (var6 > 0.001F) {
         ClickGuiModule var7 = Night.moduleManager.getModule(ClickGuiModule.class);
         boolean var8 = !var7.fillMode.getValue().equalsIgnoreCase("Default");
         int var9 = this.getX() + this.getHorizontalPadding() + Math.round((this.getWidth() - this.getHorizontalPadding() * 2) * var6);
         if (var8) {
            int var10 = Math.round(var7.neekeriOpacity.getValue().floatValue() / 100.0F * 255.0F);
            var1.enableScissor(this.getX() + this.getHorizontalPadding(), this.getY(), var9, this.getY() + this.getHeight() - 1);
            NeekeriPatternShader.a(
               var1, this.getX() + this.getHorizontalPadding(), this.getY(), this.getWidth() - this.getHorizontalPadding() * 2, this.getHeight() - 1, var10
            );
            var1.disableScissor();
         } else {
            Color var18 = ColorUtil.a(var7.color.getColor(), 90);
            Render2DUtil.a(var1, this.getX() + this.getHorizontalPadding(), this.getY(), var9, this.getY() + this.getHeight() - 1, var18);
         }
      }

      Color var15 = ClickGuiScreen.a(this.getY(), 60);
      Render2DUtil.a(
         var1,
         this.getX() + this.getHorizontalPadding(),
         this.getY() + this.getHeight() - 1,
         this.getX() + this.getWidth() - this.getHorizontalPadding(),
         this.getY() + this.getHeight(),
         var15
      );
      int var16 = this.getX() + this.getTextOffset() + (this.a.enabledSetting().getValue() ? 1 : 0);
      int var17 = this.getY() + 2;
      String var19 = this.a.enabledSetting().getValue() ? "" : ChatFormatting.GRAY.toString();
      this.beginHoverTransform(var1, var2, var3);
      Night.fontManager.drawShadowedText(var1, var19 + this.a.text(), var16, var17, Color.WHITE);
      if (!this.c.isEmpty()) {
         String var11 = this.isExpanded() ? "..." : "+";
         int var12 = this.getX() + this.getWidth() - this.getTextOffset() - Night.fontManager.getStringWidth(var11) - 1;
         Night.fontManager.drawShadowedText(var1, var11, var12, this.getY() + 2, Color.WHITE);
      }

      this.endHoverTransform(var1);
      if (this.horizontalPadding > 0) {
         int var20 = this.getY() + this.getHeight();
         var1.enableScissor(this.getX(), var20, this.getX() + this.getWidth(), var20 + this.horizontalPadding);

         for (GuiComponent var13 : (Iterable<GuiComponent>)(Object)this.c) {
            if (!var13.isVisible()) {
               if (var13 instanceof BooleanComponent var14) {
                  var14.b(false);
               } else if (var13 instanceof NumberComponent) {
                  ((NumberComponent)var13).b(false);
               }
            } else {
               var13.render(var1, var2, var3, var4);
               if (var13.isHovered(var2, var3) && Night.clickGui.e().a().isEmpty()) {
                  Night.clickGui.e().a(var13.getDescription());
               }
            }
         }

         var1.disableScissor();
      } else {
         for (GuiComponent var23 : (Iterable<GuiComponent>)(Object)this.c) {
            if (var23 instanceof BooleanComponent var24) {
               var24.b(false);
            } else if (var23 instanceof NumberComponent var25) {
               var25.b(false);
            }
         }
      }
   }

   @Override
   public void mouseClicked(double var1, double var3, int var5) {
      this.p();
      if (this.isHovered(var1, var3)) {
         if (var5 == 0) {
            this.a.enabledSetting().setValue(!this.a.enabledSetting().getValue());
            this.playClickSound();
         } else if (var5 == 1) {
            if (this.a.categorySetting() != null) {
               this.a.categorySetting().setOpen(!this.a.categorySetting().isOpen());
            } else {
               this.b = !this.b;
               this.e.setEasing(EasingUtil.Easing.DECELERATE);
            }

            for (GuiComponent var7 : (Iterable<GuiComponent>)(Object)this.c) {
               if (var7 instanceof BooleanComponent) {
                  ((BooleanComponent)var7).a();
               } else if (var7 instanceof NumberComponent) {
                  ((NumberComponent)var7).a();
               }
            }

            this.playClickSound();
         }
      }

      if (this.isExpanded()) {
         for (GuiComponent var9 : (Iterable<GuiComponent>)(Object)this.c) {
            if (var9.isVisible()) {
               var9.mouseClicked(var1, var3, var5);
            }
         }
      }
   }

   @Override
   public void mouseReleased(double var1, double var3, int var5) {
      this.p();
      if (this.isExpanded()) {
         for (GuiComponent var7 : (Iterable<GuiComponent>)(Object)this.c) {
            if (var7.isVisible()) {
               var7.mouseReleased(var1, var3, var5);
            }
         }
      }
   }

   @Override
   public void mouseDragged(double var1, double var3, int var5, double var6, double var8) {
      this.p();
      if (this.isExpanded()) {
         for (GuiComponent var11 : (Iterable<GuiComponent>)(Object)this.c) {
            if (var11.isVisible()) {
               var11.mouseDragged(var1, var3, var5, var6, var8);
            }
         }
      }
   }

   @Override
   public void keyPressed(int var1, int var2, int var3) {
      if (this.isExpanded()) {
         for (GuiComponent var5 : (Iterable<GuiComponent>)(Object)this.c) {
            if (var5.isVisible()) {
               var5.keyPressed(var1, var2, var3);
            }
         }
      }
   }

   @Override
   public void charTyped(char var1, int var2) {
      if (this.isExpanded()) {
         for (GuiComponent var4 : (Iterable<GuiComponent>)(Object)this.c) {
            if (var4.isVisible()) {
               var4.charTyped(var1, var2);
            }
         }
      }
   }
}
