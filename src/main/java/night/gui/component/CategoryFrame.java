package night.gui.component;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Comparator;
import java.util.List;
import lombok.Generated;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;
import night.Night;
import night.gui.ClickGuiScreen;
import night.gui.component.impl.ModuleComponent;
import night.gui.component.impl.WhitelistComponent;
import night.module.Module;
import night.module.impl.core.ClickGuiModule;
import night.module.impl.core.PingBypassModule;
import night.setting.impl.BooleanSetting;
import night.setting.impl.CategorySetting;
import night.util.animation.Animation;
import night.util.animation.EasingUtil;
import night.util.render.Render2DUtil;

public class CategoryFrame {
   private final Module.Category category;
   private final String customTitle;
   private int x;
   private int y;
   private int width;
   private int headerHeight;
   private int contentHeight;
   private int visibleHeight;
   private int dragOffsetX = 0;
   private int dragOffsetY = 0;
   private int padding = 3;
   public boolean expanded = true;
   public boolean dragging = false;
   private final ArrayList<GuiComponent> components = new ArrayList<>();
   private float scrollOffset = 0.0F;
   private float targetScrollOffset = 0.0F;
   private long lastRenderTime = System.currentTimeMillis();
   private final Animation expandAnimation = new Animation(1.0F, 1.0F, 220, EasingUtil.Easing.DECELERATE);

   public CategoryFrame(Module.Category var1, int var2, int var3, int var4, int var5) {
      this.category = var1;
      this.customTitle = null;
      this.x = var2;
      this.y = var3;
      this.width = var4;
      this.headerHeight = var5;

      for (Module var7 : Night.moduleManager.getModulesByCategory(var1)) {
         if (!(var7 instanceof PingBypassModule)) {
            this.components.add(new ModuleComponent(var7, this, var5));
         }
      }
      this.sortComponents();
   }

   public CategoryFrame(String var1, List<GuiComponent> var2, int var3, int var4, int var5, int var6) {
      this.category = null;
      this.customTitle = var1;
      this.x = var3;
      this.y = var4;
      this.width = var5;
      this.headerHeight = var6;
      this.components.addAll(var2);
   }

   private String m() {
      return this.category != null ? this.category.getName() : this.customTitle;
   }

   public void render(GuiGraphicsExtractor var1, int var2, int var3, float var4) {
      this.render(var1, var2, var3, var4, "");
   }

   public void render(GuiGraphicsExtractor var1, int var2, int var3, float var4, String var5) {
      if (this.dragging) {
         this.setX(var2 - this.dragOffsetX);
         this.setY(var3 - this.dragOffsetY);
      }

      boolean var6 = var5 != null && !var5.isEmpty();
      this.contentHeight = this.headerHeight;
      this.visibleHeight = 0;
      int var7 = 0;
      this.contentHeight++;

      for (GuiComponent var9 : this.components) {
         if (var6 && var9 instanceof ExpandableComponent var10) {
            if (!var10.getSearchName().toLowerCase().contains(var5.toLowerCase())) {
               var9.setVisible(false);
               continue;
            }

            var9.setVisible(true);
            var10.setSearchQuery(var5);
         } else if (!var6 && var9 instanceof ExpandableComponent var11) {
            var9.setVisible(true);
            var11.setSearchQuery("");
         }

         if (var9.isVisible()) {
            this.visibleHeight = this.visibleHeight + var9.getHeight();
            var9.setX(this.x);
            var9.setY(this.y + this.contentHeight);
            this.contentHeight = this.contentHeight + var9.getHeight();
            if (var9 instanceof ExpandableComponent var31) {
               float var33 = var31.getExpandProgress();
               List<GuiComponent> var12 = var31.getSubComponents();
               float var13 = 0.0F;
               float var14 = 0.0F;

               for (GuiComponent var16 : (List<GuiComponent>)var12) {
                  var16.getSetting().getVisibility().update();
                  boolean var17 = var16.getSetting().getVisibility().isVisible();
                  var16.setVisible(var17);
                  if (var17) {
                     float var18 = 1.0F;
                     if (var16.getSetting().getVisibility() instanceof CategorySetting.CategoryCondition var20) {
                        var18 = var20.d().getAnimationProgress();
                     } else if (var16.getSetting().getVisibility() instanceof BooleanSetting.BooleanCondition var21) {
                        var18 = var21.d();
                     }

                     float var47 = var16.getHeight() * var18;
                     var16.setX(this.x);
                     var16.setY(this.y + this.contentHeight + Math.round(var13));
                     var13 += var47;
                     var14 += var47;
                  }
               }

               int var37 = Math.round(var14 * var33);
               var31.setExpandedHeight(var37);
               this.contentHeight += var37;
               if (var37 > 0) {
                  var7 = Math.max(var7, this.contentHeight - this.headerHeight - 1);
               }

               if (var6) {
                  this.visibleHeight += var37;
               }
            }
         }
      }

      if (!var6 && this.visibleHeight < this.headerHeight * 5 && var7 > this.visibleHeight) {
         this.visibleHeight = this.contentHeight - this.headerHeight - 1;
      }

      float var29 = this.contentHeight - this.headerHeight - 1;
      float var30 = this.visibleHeight;
      float var32 = 0.0F;
      float var34 = -Math.max(0.0F, var29 - var30);
      long var35 = System.currentTimeMillis();
      float var36 = Math.min((float)(var35 - this.lastRenderTime) / 1000.0F, 0.05F);
      this.lastRenderTime = var35;
      if (var36 <= 1.0E-4F) {
         var36 = 0.016F;
      }

      if (this.targetScrollOffset > var32) {
         this.targetScrollOffset = this.targetScrollOffset + (var32 - this.targetScrollOffset) * Math.min(1.0F, 8.0F * var36);
      } else if (this.targetScrollOffset < var34) {
         this.targetScrollOffset = this.targetScrollOffset + (var34 - this.targetScrollOffset) * Math.min(1.0F, 8.0F * var36);
      }

      float var38 = 1.0F - (float)Math.exp(-9.0 * var36);
      this.scrollOffset = Mth.lerp(var38, this.scrollOffset, this.targetScrollOffset);
      if (Math.abs(this.scrollOffset - this.targetScrollOffset) < 0.01F && this.targetScrollOffset >= var34 && this.targetScrollOffset <= var32) {
         this.scrollOffset = this.targetScrollOffset;
      }

      int var39 = Math.round(this.scrollOffset);
      if (var39 != 0) {
         for (GuiComponent var42 : this.components) {
            if (var42.isVisible()) {
               var42.setY(var42.getY() + var39);
               if (var42 instanceof ExpandableComponent var48) {
                  for (GuiComponent var45 : var48.getSubComponents()) {
                     if (var45.isVisible()) {
                        var45.setY(var45.getY() + var39);
                     }
                  }
               }
            }
         }
      }

      ClickGuiModule var41 = Night.moduleManager.getModule(ClickGuiModule.class);
      boolean var43 = var41.categoryIcons.getValue();
      boolean var49 = var41.glowAccent.getValue();
      Render2DUtil.a(var1, this.x, this.y, this.x + this.width, this.y + this.headerHeight, ClickGuiScreen.a(this.y, 55));
      if (var49) {
         Render2DUtil.a(var1, this.x, this.y, this.x + this.width, this.y + 1, ClickGuiScreen.a(this.y, 220));
      }

      Color var51 = ClickGuiScreen.a(this.y, 220);
      Render2DUtil.a(var1, this.x, this.y + this.headerHeight - 1, this.x + this.width, this.y + this.headerHeight, var51);
      String var46 = var43 ? this.n() + " " : "";
      Night.fontManager.drawShadowedText(var1, var46 + this.m(), this.x + this.padding, this.y + 2, Color.WHITE);
      if (var41.triangles.getValue()) {
         String var22 = this.expanded ? "\u25b2" : "\u25bc";
         int var23 = this.x + this.width - this.padding - Night.fontManager.getStringWidth(var22);
         Night.fontManager.drawShadowedText(var1, var22, var23, this.y + 2, new Color(255, 255, 255, 180));
      }

      float var52 = this.expandAnimation.animateTo(this.expanded ? 1.0F : 0.0F);
      int var53 = this.visibleHeight;
      int var24 = Math.round(var53 * var52);
      if (var24 > 0) {
         int var25 = this.y + this.headerHeight;
         int var26 = var25 + var24 + 4;
         var1.enableScissor(this.x, var25, this.x + this.width, var26);
         Render2DUtil.a(var1, this.x, this.y + this.headerHeight, this.x + this.width, var26, new Color(14, 13, 20, 195));

         for (GuiComponent var28 : this.components) {
            if (var28.isVisible()) {
               var28.render(var1, var2, var3, var4);
            }
         }

         var1.disableScissor();
      }

      Color var54 = ClickGuiScreen.a(this.y, 130);
      int var55 = this.expanded ? this.y + this.headerHeight + var24 + 4 : this.y + this.headerHeight;
      Render2DUtil.b(var1, this.x, this.y, this.x + this.width, var55, var54);
   }

   private String n() {
      if (this.category == null) {
         return "\u2726";
      }

      return switch (this.category) {
         case COMBAT -> "\u2694";
         case MOVEMENT -> "\u26a1";
         case VISUALS -> "\u25c9";
         case PLAYER -> "\u265f";
         case MISCELLANEOUS -> "\u2699";
         case CORE -> "\u25c6";
         case KINGMC -> "\u265b";
      };
   }

   public void mouseClicked(double var1, double var3, int var5) {
      if (this.isHeaderHovered(var1, var3)) {
         if (var5 == 0) {
            this.dragging = true;
            this.dragOffsetX = (int)(var1 - this.getX());
            this.dragOffsetY = (int)(var3 - this.getY());
         } else if (var5 == 1) {
            this.expanded = !this.expanded;
         }
      }

      if (this.expanded) {
         int var6 = this.y + this.headerHeight;
         int var7 = var6 + Math.round(this.visibleHeight * this.expandAnimation.animateTo(this.expanded ? 1.0F : 0.0F)) + 4;
         if (var1 >= this.x && var1 <= this.x + this.width && var3 >= var6 && var3 <= var7) {
            for (GuiComponent var9 : this.components) {
               if (var9.isVisible()) {
                  var9.mouseClicked(var1, var3, var5);
               }
            }
         }
      }
   }

   public void mouseReleased(double var1, double var3, int var5) {
      if (var5 == 0) {
         this.dragging = false;
      }

      for (GuiComponent var7 : this.components) {
         if (var7.isVisible()) {
            var7.mouseReleased(var1, var3, var5);
         }
      }
   }

   public void mouseScrolled(double var1, double var3, double var5, double var7) {
      int var9 = this.y;
      int var10 = this.y
         + this.headerHeight
         + (this.expanded ? Math.round(this.visibleHeight * this.expandAnimation.animateTo(this.expanded ? 1.0F : 0.0F)) + 4 : 0);
      if (var1 >= this.x && var1 <= this.x + this.width && var3 >= var9 && var3 <= var10) {
         boolean var11 = false;

         for (GuiComponent var13 : this.components) {
            if (var13 instanceof ExpandableComponent var14 && var14.isExpanded()) {
               for (GuiComponent var17 : var14.getSubComponents().stream().filter(var0 -> var0 instanceof WhitelistComponent).toList()) {
                  if (var17 instanceof WhitelistComponent var18 && var18.b_bool_double_double(var1, var3)) {
                     var18.mouseScrolled(var1, var3, var5, var7);
                     var11 = true;
                     break;
                  }
               }
            }
         }

         if (!var11 && var7 != 0.0) {
            float var19 = Night.moduleManager.getModule(ClickGuiModule.class).scrollSpeed.getValue().floatValue();
            float var20 = this.contentHeight - this.headerHeight - 1;
            float var21 = this.visibleHeight;
            float var22 = 0.0F;
            float var23 = -Math.max(0.0F, var20 - var21);
            float var24 = (float)(var7 * var19 * 12.0);
            float var25 = 1.0F;
            if (this.targetScrollOffset > var22) {
               var25 = 1.0F / (1.0F + (this.targetScrollOffset - var22) * 0.05F);
            } else if (this.targetScrollOffset < var23) {
               var25 = 1.0F / (1.0F + (var23 - this.targetScrollOffset) * 0.05F);
            }

            this.targetScrollOffset += var24 * var25;
         }
      }
   }

   public void mouseDragged(double var1, double var3, int var5, double var6, double var8) {
      Iterator var10 = this.components.iterator();

      while (var10.hasNext()) {
         ((GuiComponent)var10.next()).mouseDragged(var1, var3, var5, var6, var8);
      }
   }

   public void keyPressed(int var1, int var2, int var3) {
      if (this.expanded) {
         for (GuiComponent var5 : this.components) {
            if (var5.isVisible()) {
               var5.keyPressed(var1, var2, var3);
            }
         }
      }
   }

   public void charTyped(char var1, int var2) {
      if (this.expanded) {
         for (GuiComponent var4 : this.components) {
            if (var4.isVisible()) {
               var4.charTyped(var1, var2);
            }
         }
      }
   }

   public boolean isHeaderHovered(double var1, double var3) {
      return this.x <= var1 && this.y <= var3 && this.x + this.width > var1 && this.y + this.headerHeight > var3;
   }

   @Generated
   public Module.Category getCategory() {
      return this.category;
   }

   @Generated
   public int getX() {
      return this.x;
   }

   @Generated
   public int getY() {
      return this.y;
   }

   @Generated
   public int getWidth() {
      return this.width;
   }

   @Generated
   public int getHeaderHeight() {
      return this.headerHeight;
   }

   @Generated
   public int getContentHeight() {
      return this.contentHeight;
   }

   @Generated
   public int getDragOffsetX() {
      return this.dragOffsetX;
   }

   @Generated
   public int getDragOffsetY() {
      return this.dragOffsetY;
   }

   @Generated
   public int getPadding() {
      return this.padding;
   }

   @Generated
   public boolean isExpanded() {
      return this.expanded;
   }

   @Generated
   public boolean isDragging() {
      return this.dragging;
   }

   @Generated
   public ArrayList<GuiComponent> getComponents() {
      return this.components;
   }

   @Generated
   public void setX(int var1) {
      this.x = var1;
   }

   @Generated
   public void setY(int var1) {
      this.y = var1;
   }

   @Generated
   public void setContentHeight(int var1) {
      this.contentHeight = var1;
   }

   @Generated
   public void setDragOffsetX(int var1) {
      this.dragOffsetX = var1;
   }

   @Generated
   public void setDragOffsetY(int var1) {
      this.dragOffsetY = var1;
   }

   @Generated
   public void setExpanded(boolean var1) {
      this.expanded = var1;
   }

   @Generated
   public void setDragging(boolean var1) {
      this.dragging = var1;
   }

   public void sortComponents() {
      this.components.sort(Comparator.comparing(c -> {
         if (c instanceof ModuleComponent mc) {
            return mc.getSearchName();
         }
         return "";
      }));
   }

}
