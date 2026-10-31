package night.gui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import night.Night;
import night.gui.component.CategoryFrame;
import night.gui.component.ExpandableComponent;
import night.gui.component.GuiComponent;
import night.gui.component.impl.BooleanComponent;
import night.gui.component.impl.CategorySettingComponent;
import night.gui.component.impl.ColorComponent;
import night.gui.component.impl.HudElementComponent;
import night.gui.component.impl.KeybindComponent;
import night.gui.component.impl.ModeComponent;
import night.gui.component.impl.NumberComponent;
import night.gui.component.impl.StringComponent;
import night.gui.component.impl.WhitelistComponent;
import night.manager.HudElementManager;
import night.module.impl.core.HudEditorModule;
import night.module.impl.core.HudModule;
import night.module.impl.misc.Notifications;
import night.setting.Setting;
import night.setting.impl.BooleanSetting;
import night.setting.impl.CategorySetting;
import night.setting.impl.ColorSetting;
import night.setting.impl.KeybindSetting;
import night.setting.impl.ModeSetting;
import night.setting.impl.NumberSetting;
import night.setting.impl.StringSetting;
import night.setting.impl.WhitelistSetting;
import night.util.color.ColorUtil;
import night.util.render.Render2DUtil;
import night.util.world.PacketAndRenderUtil;

public class HudEditorScreen extends Screen {
   private String a = null;
   private float b;
   private float c;
   private final CategoryFrame d;

   public HudEditorScreen() {
      super(Component.literal("night-hud-editor"));
      int var1 = Minecraft.getInstance().getWindow().getGuiScaledWidth();
      int var2 = Minecraft.getInstance().getWindow().getGuiScaledHeight();
      int var3 = Math.round(var1 * 0.23F);
      int var4 = Math.round(var2 * 0.14F);
      this.d = new CategoryFrame("HUD", List.of(), var3, var4, 110, 13);
      List<Setting> var5 = Night.moduleManager.getModule(HudModule.class).getSettings();
      HashSet var6 = new HashSet();

      for (HudElementManager.HudEntry var8 : HudElementManager.a().values()) {
         if (var8.categorySetting() != null) {
            var6.add(var8.categorySetting());
         }

         List var9 = var8.categorySetting() == null
            ? List.of()
            : var5.stream()
               .filter(var1x -> {
                  Setting.VisibilityCondition var2x = var1x.getVisibility();
                  return var2x instanceof CategorySetting.CategoryCondition && ((CategorySetting.CategoryCondition)var2x).d() == var8.categorySetting();
               })
               .filter(var1x -> var1x != var8.enabledSetting())
               .filter(var0 -> !(var0 instanceof BooleanSetting) || !var0.getDisplayName().equals("Enabled"))
               .toList();
         this.d.getComponents().add(new HudElementComponent(var8, var9, this.d, 13));
      }

      for (Setting var12 : (Iterable<Setting>)(Object)var5) {
         if (var12 instanceof CategorySetting var13 && !var6.contains(var13)) {
            List var10 = var5.stream().filter(var1x -> {
               Setting.VisibilityCondition var2x = var1x.getVisibility();
               return var2x instanceof CategorySetting.CategoryCondition && ((CategorySetting.CategoryCondition)var2x).d() == var13;
            }).toList();
            this.d.getComponents().add(new HudEditorScreen.CategoryGroupButton(var13, var10, this.d, 13));
         }
      }
   }

   public void extractBackground(GuiGraphicsExtractor var1, int var2, int var3, float var4) {
   }

   public void extractRenderState(GuiGraphicsExtractor var1, int var2, int var3, float var4) {
      float var5 = Minecraft.getInstance().getWindow().getGuiScaledWidth();
      float var6 = Math.max(140.0F, 12.0F + Night.fontManager.getStringWidth("Helmet is low (15%)!") + 8.0F);
      float var7 = Math.max(22.0F, Night.fontManager.getFontHeight() * 2.2F);
      float var8 = var5 - var6 - 8.0F;
      HudElementManager.a("LowArmor", var8, 8.0F, var8 + var6, 8.0F + var7);

      for (Entry var10 : (Iterable<Entry>)(Object)HudElementManager.a().entrySet()) {
         String var11 = (String)var10.getKey();
         HudElementManager.HudEntry var12 = (HudElementManager.HudEntry)var10.getValue();
         float[] var13 = HudElementManager.a(var11);
         if (var13 == null) {
            float var14 = var12.positionSetting().getX();
            float var15 = var12.positionSetting().getY();
            var13 = new float[]{var14, var15, var14 + 60.0F, var15 + 15.0F};
         }

         boolean var25 = var2 >= var13[0] && var2 <= var13[2] && var3 >= var13[1] && var3 <= var13[3];
         boolean var26 = var12.enabledSetting().getValue();
         boolean var16 = var11.equals(this.a);
         Color var17 = var26 ? ColorUtil.d() : new Color(150, 150, 150);
         int var18 = var16 ? 70 : (var25 ? 45 : 25);
         Render2DUtil.a(
            var1, var13[0] - 1.0F, var13[1] - 1.0F, var13[2] + 1.0F, var13[3] + 1.0F, new Color(var17.getRed(), var17.getGreen(), var17.getBlue(), var18)
         );
         Render2DUtil.b(
            var1,
            var13[0] - 1.0F,
            var13[1] - 1.0F,
            var13[2] + 1.0F,
            var13[3] + 1.0F,
            new Color(var17.getRed(), var17.getGreen(), var17.getBlue(), var26 ? 220 : 120)
         );
         if (var11.equals("LowArmor") && var26) {
            long var19 = System.currentTimeMillis() % 3500L;
            float var21 = var13[0];
            float var22 = var13[1];
            float var23 = var13[2] - var13[0];
            Notifications.a(var1, var21, var22, var23, "Helmet is low (15%)!", new Color(255, 65, 65), var19, 2200L);
         }

         if (var25 || var16) {
            String var27 = var11 + (var26 ? "" : " (disabled)");
            float var24 = var13[0];
            float var28 = var13[1] - Night.fontManager.getFontHeight() - 2.0F;
            Render2DUtil.a(
               var1,
               var24 - 1.0F,
               var28 - 1.0F,
               var24 + Night.fontManager.getStringWidth(var27) + 1.0F,
               var28 + Night.fontManager.getFontHeight(),
               new Color(0, 0, 0, 180)
            );
            Night.fontManager.drawShadowedText(var1, var27, (int)var24, (int)var28, var17);
         }
      }

      this.d.render(var1, var2, var3, var4);
   }

   private boolean a_bool_double_double(double var1, double var3) {
      return var1 >= this.d.getX()
         && var1 <= this.d.getX() + this.d.getWidth()
         && var3 >= this.d.getY()
         && var3 <= this.d.getY() + this.d.getContentHeight() + 4;
   }

   public boolean mouseClicked(MouseButtonEvent var1, boolean var2) {
      double var3 = var1.x();
      double var5 = var1.y();
      if (this.a_bool_double_double(var3, var5)) {
         this.d.mouseClicked(var3, var5, var1.button());
         return true;
      }

      for (Entry var8 : (Iterable<Entry>)(Object)HudElementManager.a().entrySet()) {
         String var9 = (String)var8.getKey();
         HudElementManager.HudEntry var10 = (HudElementManager.HudEntry)var8.getValue();
         float[] var11 = HudElementManager.a(var9);
         if (var11 == null) {
            float var12 = var10.positionSetting().getX();
            float var13 = var10.positionSetting().getY();
            var11 = new float[]{var12, var13, var12 + 60.0F, var13 + 15.0F};
         }

         if (!(var3 < var11[0]) && !(var3 > var11[2]) && !(var5 < var11[1]) && !(var5 > var11[3])) {
            if (var1.button() == 1) {
               var10.enabledSetting().setValue(!var10.enabledSetting().getValue());
            } else if (var1.button() == 0) {
               this.a = var9;
               this.b = (float)var3 - var10.positionSetting().getX();
               this.c = (float)var5 - var10.positionSetting().getY();
            }

            return true;
         }
      }

      return super.mouseClicked(var1, var2);
   }

   public boolean mouseDragged(MouseButtonEvent var1, double var2, double var4) {
      if (this.a != null) {
         HudElementManager.HudEntry var6 = HudElementManager.a().get(this.a);
         if (var6 != null) {
            var6.positionSetting().setPosition((float)var1.x() - this.b, (float)var1.y() - this.c);
            HudElementManager.a(this.a, this.width, this.height);
         }

         return true;
      } else {
         this.d.mouseDragged(var1.x(), var1.y(), var1.button(), var2, var4);
         return super.mouseDragged(var1, var2, var4);
      }
   }

   public boolean mouseReleased(MouseButtonEvent var1) {
      this.a = null;
      this.d.mouseReleased(var1.x(), var1.y(), var1.button());
      return super.mouseReleased(var1);
   }

   public boolean mouseScrolled(double var1, double var3, double var5, double var7) {
      if (this.a_bool_double_double(var1, var3)) {
         this.d.mouseScrolled(var1, var3, var5, var7);
         return true;
      } else {
         return super.mouseScrolled(var1, var3, var5, var7);
      }
   }

   public boolean keyPressed(KeyEvent var1) {
      int var2 = var1.key();
      int var3 = var1.scancode();
      int var4 = var1.modifiers();
      this.d.keyPressed(var2, var3, var4);
      if (var2 == 256 && !KeybindComponent.a()) {
         this.onClose();
         return true;
      } else {
         return var2 == 256 ? true : super.keyPressed(var1);
      }
   }

   protected void init() {
      super.init();
      PacketAndRenderUtil.a_void_Object(this);
   }

   public void removed() {
      PacketAndRenderUtil.b_void_Object(this);
      super.removed();
   }

   public boolean charTyped(CharacterEvent var1) {
      char var2 = (char)var1.codepoint();
      this.d.charTyped(var2, 0);
      return super.charTyped(var1);
   }

   public void onClose() {
      super.onClose();
      Night.moduleManager.getModule(HudEditorModule.class).setEnabled(false);
   }

   public boolean isPauseScreen() {
      return false;
   }

   static class CategoryGroupButton extends CategorySettingComponent implements ExpandableComponent {
      private final List<GuiComponent> a = new ArrayList<>();
      private final CategorySetting b;
      private int x = 0;

      public CategoryGroupButton(CategorySetting var1, List<Setting> var2, CategoryFrame var3, int var4) {
         super(var1, null, var3, var4);
         this.b = var1;

         for (Setting var6 : (Iterable<Setting>)(Object)var2) {
            if (var6 instanceof BooleanSetting var7) {
               this.a.add(new BooleanComponent(var7, var3, var4));
            } else if (var6 instanceof NumberSetting var8) {
               this.a.add(new NumberComponent(var8, var3, var4));
            } else if (var6 instanceof CategorySetting var9) {
               this.a.add(new CategorySettingComponent(var9, null, var3, var4));
            } else if (var6 instanceof KeybindSetting var10) {
               this.a.add(new KeybindComponent(var10, var3, var4));
            } else if (var6 instanceof ModeSetting var11) {
               this.a.add(new ModeComponent(var11, var3, var4));
            } else if (var6 instanceof WhitelistSetting var12) {
               this.a.add(new WhitelistComponent(var12, var3, var4));
            } else if (var6 instanceof StringSetting var13) {
               this.a.add(new StringComponent(var13, var3, var4));
            } else if (var6 instanceof ColorSetting var14) {
               this.a.add(new ColorComponent(var14, var3, var4));
            }
         }
      }

      @Override
      public String getSearchName() {
         return this.b.getDisplayName();
      }

      @Override
      public boolean isExpanded() {
         return this.b.isOpen();
      }

      @Override
      public float getExpandProgress() {
         return this.b.getAnimationProgress();
      }

      @Override
      public List<GuiComponent> getSubComponents() {
         return this.a;
      }

      @Override
      public void setExpandedHeight(int var1) {
         this.x = var1;
      }

      @Override
      public void setSearchQuery(String var1) {
      }

      @Override
      public void render(GuiGraphicsExtractor var1, int var2, int var3, float var4) {
         super.render(var1, var2, var3, var4);
         if (this.x > 0) {
            int var5 = this.getY() + this.getHeight();
            var1.enableScissor(this.getX(), var5, this.getX() + this.getWidth(), var5 + this.x);

            for (GuiComponent var7 : (Iterable<GuiComponent>)(Object)this.a) {
               if (var7.isVisible()) {
                  var7.render(var1, var2, var3, var4);
               }
            }

            var1.disableScissor();
         }
      }

      @Override
      public void mouseClicked(double var1, double var3, int var5) {
         super.mouseClicked(var1, var3, var5);
         if (this.b.isOpen()) {
            for (GuiComponent var7 : (Iterable<GuiComponent>)(Object)this.a) {
               if (var7.isVisible()) {
                  var7.mouseClicked(var1, var3, var5);
               }
            }
         }
      }

      @Override
      public void mouseReleased(double var1, double var3, int var5) {
         super.mouseReleased(var1, var3, var5);
         if (this.b.isOpen()) {
            for (GuiComponent var7 : (Iterable<GuiComponent>)(Object)this.a) {
               if (var7.isVisible()) {
                  var7.mouseReleased(var1, var3, var5);
               }
            }
         }
      }

      @Override
      public void mouseDragged(double var1, double var3, int var5, double var6, double var8) {
         super.mouseDragged(var1, var3, var5, var6, var8);
         if (this.b.isOpen()) {
            for (GuiComponent var11 : (Iterable<GuiComponent>)(Object)this.a) {
               if (var11.isVisible()) {
                  var11.mouseDragged(var1, var3, var5, var6, var8);
               }
            }
         }
      }

      @Override
      public void keyPressed(int var1, int var2, int var3) {
         super.keyPressed(var1, var2, var3);
         if (this.b.isOpen()) {
            for (GuiComponent var5 : (Iterable<GuiComponent>)(Object)this.a) {
               if (var5.isVisible()) {
                  var5.keyPressed(var1, var2, var3);
               }
            }
         }
      }

      @Override
      public void charTyped(char var1, int var2) {
         super.charTyped(var1, var2);
         if (this.b.isOpen()) {
            for (GuiComponent var4 : (Iterable<GuiComponent>)(Object)this.a) {
               if (var4.isVisible()) {
                  var4.charTyped(var1, var2);
               }
            }
         }
      }
   }
}
