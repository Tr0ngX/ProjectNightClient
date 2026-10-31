package night.gui.component.impl;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import lombok.Generated;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import night.Night;
import night.gui.ClickGuiScreen;
import night.gui.component.CategoryFrame;
import night.gui.component.ExpandableComponent;
import night.gui.component.GuiComponent;
import night.module.Module;
import night.module.impl.core.ClickGuiModule;
import night.setting.Setting;
import night.setting.impl.BooleanSetting;
import night.setting.impl.CategorySetting;
import night.setting.impl.ColorSetting;
import night.setting.impl.ImageSetting;
import night.setting.impl.KeybindSetting;
import night.setting.impl.ModeSetting;
import night.setting.impl.NumberSetting;
import night.setting.impl.StringSetting;
import night.setting.impl.WhitelistSetting;
import night.util.animation.Animation;
import night.util.animation.EasingUtil;
import night.util.color.ColorUtil;
import night.util.input.KeyNameUtil;
import night.util.render.NeekeriPatternShader;
import night.util.render.Render2DUtil;

public class ModuleComponent extends GuiComponent implements ExpandableComponent {
   private final Module a;
   private boolean b = false;
   private final ArrayList<GuiComponent> c = new ArrayList<>();
   private String d = "";
   private final Animation e = new Animation(Animation.a_int_int(220), EasingUtil.Easing.DECELERATE);
   private int horizontalPadding = 0;
   private final Animation g;

   @Override
   public float getExpandProgress() {
      return this.e.animateTo(this.b ? 1.0F : 0.0F);
   }

   @Override
   public String getSearchName() {
      return this.a.getName();
   }

   public ModuleComponent(Module var1, CategoryFrame var2, int var3) {
      super(var2, var3, var1.getDescription());
      this.a = var1;
      float var4 = var1.isEnabled() ? 1.0F : 0.0F;
      this.g = new Animation(var4, var4, 200, EasingUtil.Easing.DECELERATE);
      List var5 = var1.getSettings();

      for (int var6 = 0; var6 < var5.size(); var6++) {
         Setting var7 = (Setting)var5.get(var6);
         if (var7 instanceof BooleanSetting var8) {
            if (!var8.getDisplayName().equals("Enabled")
               || !(var8.getVisibility() instanceof CategorySetting.CategoryCondition var10 && var6 > 0 && var5.get(var6 - 1) == var10.d())) {
               this.c.add(new BooleanComponent(var8, var2, var3));
            }
         } else if (var7 instanceof NumberSetting var11) {
            this.c.add(new NumberComponent(var11, var2, var3));
         } else if (var7 instanceof CategorySetting var12) {
            BooleanSetting var21 = null;
            if (var6 + 1 < var5.size() && var5.get(var6 + 1) instanceof BooleanSetting var20 && var20.getDisplayName().equals("Enabled")) {
               Setting.VisibilityCondition var22 = var20.getVisibility();
               if (var22 instanceof CategorySetting.CategoryCondition && ((CategorySetting.CategoryCondition)var22).d() == var12) {
                  var21 = var20;
               }
            }

            this.c.add(new CategorySettingComponent(var12, var21, var2, var3));
         } else if (var7 instanceof KeybindSetting var14) {
            this.c.add(new KeybindComponent(var14, var2, var3));
         } else if (var7 instanceof ModeSetting var15) {
            this.c.add(new ModeComponent(var15, var2, var3));
         } else if (var7 instanceof WhitelistSetting var16) {
            this.c.add(new WhitelistComponent(var16, var2, var3));
         } else if (var7 instanceof StringSetting var17) {
            this.c.add(new StringComponent(var17, var2, var3));
         } else if (var7 instanceof ImageSetting var18) {
            this.c.add(new ImageSettingComponent(var18, var2, var3));
         } else if (var7 instanceof ColorSetting var19) {
            this.c.add(new ColorComponent(var19, var2, var3));
         }
      }
   }

   @Override
   public void render(GuiGraphicsExtractor var1, int var2, int var3, float var4) {
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

      ClickGuiModule var6 = Night.moduleManager.getModule(ClickGuiModule.class);
      if (!this.a.isEnabled()) {
         Color var7 = ColorUtil.a(var6.color.getColor(), 45);
         Render2DUtil.a(
            var1,
            this.getX() + this.getHorizontalPadding(),
            this.getY(),
            this.getX() + this.getWidth() - this.getHorizontalPadding(),
            this.getY() + this.getHeight() - 1,
            var7
         );
      }

      float var21 = this.g.animateTo(this.a.isEnabled() ? 1.0F : 0.0F);
      if (var21 > 0.001F) {
         boolean var8 = !var6.fillMode.getValue().equalsIgnoreCase("Default");
         int var9 = this.getX() + this.getHorizontalPadding() + Math.round((this.getWidth() - this.getHorizontalPadding() * 2) * var21);
         if (var8) {
            int var10 = Math.round(var6.neekeriOpacity.getValue().floatValue() / 100.0F * 255.0F);
            var1.enableScissor(this.getX() + this.getHorizontalPadding(), this.getY(), var9, this.getY() + this.getHeight() - 1);
            NeekeriPatternShader.a(
               var1, this.getX() + this.getHorizontalPadding(), this.getY(), this.getWidth() - this.getHorizontalPadding() * 2, this.getHeight() - 1, var10
            );
            var1.disableScissor();
         } else {
            Color var24 = ColorUtil.a(var6.color.getColor(), 90);
            Render2DUtil.a(var1, this.getX() + this.getHorizontalPadding(), this.getY(), var9, this.getY() + this.getHeight() - 1, var24);
         }
      }

      Color var22 = ClickGuiScreen.a(this.getY(), 60);
      Render2DUtil.a(
         var1,
         this.getX() + this.getHorizontalPadding(),
         this.getY() + this.getHeight() - 1,
         this.getX() + this.getWidth() - this.getHorizontalPadding(),
         this.getY() + this.getHeight(),
         var22
      );
      String var23 = this.a.getName();
      int var25 = this.getX() + this.getTextOffset() + (this.a.isEnabled() ? 1 : 0);
      int var11 = this.getY() + 2;
      this.beginHoverTransform(var1, var2, var3);
      if (this.d != null && !this.d.isEmpty()) {
         String var12 = var23.toLowerCase();
         String var13 = this.d.toLowerCase();
         int var14 = var12.indexOf(var13);
         if (var14 != -1) {
            String var15 = var23.substring(0, var14);
            String var16 = var23.substring(var14, var14 + this.d.length());
            String var17 = var23.substring(var14 + this.d.length());
            if (!var15.isEmpty()) {
               String var18 = this.a.isEnabled() ? "" : ChatFormatting.GRAY.toString();
               Night.fontManager.drawShadowedText(var1, var18 + var15, var25, var11, Color.WHITE);
               var25 += Night.fontManager.getStringWidth(var15);
            }

            Color var51 = ClickGuiScreen.a(this.getY(), 200);
            int var19 = Night.fontManager.getStringWidth(var16);
            Render2DUtil.a(var1, var25 - 1, var11 - 1, var25 + var19 + 1, var11 + Night.fontManager.getFontHeight(), var51);
            Night.fontManager.drawShadowedText(var1, var16, var25, var11, Color.WHITE);
            var25 += var19;
            if (!var17.isEmpty()) {
               String var20 = this.a.isEnabled() ? "" : ChatFormatting.GRAY.toString();
               Night.fontManager.drawShadowedText(var1, var20 + var17, var25, var11, Color.WHITE);
            }
         } else {
            Night.fontManager.drawShadowedText(var1, (this.a.isEnabled() ? "" : ChatFormatting.GRAY) + var23, var25, var11, Color.WHITE);
         }
      } else {
         Night.fontManager.drawShadowedText(var1, (this.a.isEnabled() ? "" : ChatFormatting.GRAY) + var23, var25, var11, Color.WHITE);
      }

      this.endHoverTransform(var1);
      int var27 = this.getX() + this.getWidth() - this.getTextOffset() - 1;
      if (!this.c.isEmpty()) {
         String var28 = var6.triangles.getValue() ? (this.b ? "\u25b2" : "\u25bc") : (this.b ? "..." : "+");
         int var33 = Night.fontManager.getStringWidth(var28);
         int var38 = var27 - var33;
         Color var43 = this.b ? ClickGuiScreen.a(this.getY(), 220) : new Color(255, 255, 255, 120);
         Night.fontManager.drawShadowedText(var1, var28, var38, this.getY() + 2, var43);
         var27 -= var33 + 4;
      }

      if (var6.switches.getValue()) {
         byte var29 = 14;
         byte var34 = 7;
         int var39 = var27 - var29;
         int var44 = this.getY() + (this.getHeight() - var34) / 2;
         Color var48 = this.a.isEnabled() ? ClickGuiScreen.a(this.getY(), Math.round(180.0F * var21)) : new Color(40, 40, 50, 160);
         Render2DUtil.a(var1, var39, var44, var39 + var29, var44 + var34, var48);
         Render2DUtil.b(var1, var39, var44, var39 + var29, var44 + var34, new Color(0, 0, 0, 80));
         byte var52 = 5;
         int var55 = var39 + Math.round(var21 * (var29 - var52));
         Color var57 = this.a.isEnabled() ? Color.WHITE : new Color(180, 180, 190);
         Render2DUtil.a(var1, var55, var44 - 1, var55 + var52, var44 + var34 + 1, var57);
         var27 -= var29 + 4;
      }

      if (this.a.getKeybind() != 0) {
         String var30 = KeyNameUtil.a_String_int(this.a.getKeybind()).toUpperCase();
         int var35 = Night.fontManager.getStringWidth(var30);
         int var40 = Night.fontManager.getStringWidth((String)var23);
         int var45 = this.getX() + this.getTextOffset() + (this.a.isEnabled() ? 1 : 0) + var40;
         int var49 = var27 - var45 - 4;
         if (var35 > var49 && var49 > 4) {
            float var54 = Math.max(0.55F, (float)var49 / var35);
            int var56 = var27 - Math.round(var35 * var54);
            var1.pose().pushMatrix();
            var1.pose().scale(var54, var54);
            Night.fontManager.drawShadowedText(var1, var30, Math.round(var56 / var54), Math.round((this.getY() + 2) / var54), new Color(220, 220, 230, 160));
            var1.pose().popMatrix();
         } else {
            int var53 = var27 - var35;
            Night.fontManager.drawShadowedText(var1, var30, var53, this.getY() + 2, new Color(220, 220, 230, 160));
         }
      }

      if (this.horizontalPadding > 0) {
         int var31 = this.getY() + this.getHeight();
         var1.enableScissor(this.getX(), var31, this.getX() + this.getWidth(), var31 + this.horizontalPadding);

         for (GuiComponent var41 : (Iterable<GuiComponent>)(Object)this.c) {
            if (!var41.isVisible()) {
               if (var41 instanceof BooleanComponent var46) {
                  var46.b(false);
               } else if (var41 instanceof NumberComponent var50) {
                  var50.b(false);
               }
            } else {
               var41.render(var1, var2, var3, var4);
               if (var41.isHovered(var2, var3) && Night.clickGui.e().a().isEmpty()) {
                  Night.clickGui.e().a(var41.getDescription());
               }
            }
         }

         var1.disableScissor();
      } else {
         for (GuiComponent var37 : (Iterable<GuiComponent>)(Object)this.c) {
            if (var37 instanceof BooleanComponent var42) {
               var42.b(false);
            } else if (var37 instanceof NumberComponent var47) {
               var47.b(false);
            }
         }
      }
   }

   @Override
   public void mouseClicked(double var1, double var3, int var5) {
      if (this.isHovered(var1, var3)) {
         if (var5 == 0) {
            this.a.setEnabled(!this.a.isEnabled());
            this.playClickSound();
         } else if (var5 == 1) {
            this.b = !this.b;
            this.e.setEasing(EasingUtil.Easing.DECELERATE);
            if (this.b) {
               for (GuiComponent var7 : (Iterable<GuiComponent>)(Object)this.c) {
                  if (var7 instanceof BooleanComponent) {
                     ((BooleanComponent)var7).a();
                  } else if (var7 instanceof NumberComponent) {
                     ((NumberComponent)var7).a();
                  }
               }
            }

            this.playClickSound();
         }
      }

      if (this.b) {
         for (GuiComponent var9 : (Iterable<GuiComponent>)(Object)this.c) {
            if (var9.isVisible()) {
               var9.mouseClicked(var1, var3, var5);
            }
         }
      }
   }

   @Override
   public void mouseReleased(double var1, double var3, int var5) {
      Iterator var6 = this.c.iterator();

      while (var6.hasNext()) {
         ((GuiComponent)var6.next()).mouseReleased(var1, var3, var5);
      }
   }

   @Override
   public void mouseDragged(double var1, double var3, int var5, double var6, double var8) {
      Iterator var10 = this.c.iterator();

      while (var10.hasNext()) {
         ((GuiComponent)var10.next()).mouseDragged(var1, var3, var5, var6, var8);
      }
   }

   @Override
   public void keyPressed(int var1, int var2, int var3) {
      if (this.b) {
         for (GuiComponent var5 : (Iterable<GuiComponent>)(Object)this.c) {
            if (var5.isVisible()) {
               var5.keyPressed(var1, var2, var3);
            }
         }
      }
   }

   @Override
   public void charTyped(char var1, int var2) {
      if (this.b) {
         for (GuiComponent var4 : (Iterable<GuiComponent>)(Object)this.c) {
            if (var4.isVisible()) {
               var4.charTyped(var1, var2);
            }
         }
      }
   }

   @Generated
   @Override
   public boolean isExpanded() {
      return this.b;
   }

   @Generated
   public ArrayList<GuiComponent> getSubComponents() {
      return this.c;
   }

   @Generated
   @Override
   public void setSearchQuery(String var1) {
      this.d = var1;
   }

   @Generated
   @Override
   public void setExpandedHeight(int var1) {
      this.horizontalPadding = var1;
   }
}
