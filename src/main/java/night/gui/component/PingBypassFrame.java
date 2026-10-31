package night.gui.component;

import java.awt.Color;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import night.Night;
import night.gui.ClickGuiScreen;
import night.gui.component.impl.ProxyModuleComponent;
import night.module.impl.core.PingBypassModule;
import night.util.render.Render2DUtil;

public class PingBypassFrame extends CategoryFrame {
   private Set<String> c = Collections.emptySet();

   public PingBypassFrame(int var1, int var2, int var3, int var4) {
      super(null, var1, var2, var3, var4);
   }

   private boolean m() {
      PingBypassModule var1 = Night.moduleManager.getModule(PingBypassModule.class);
      return var1 != null && var1.isEnabled() && !var1.y().isEmpty();
   }

   private void n() {
      PingBypassModule var1 = Night.moduleManager.getModule(PingBypassModule.class);
      if (var1 != null) {
         TreeSet var2 = new TreeSet<>(var1.y().keySet());
         if (!var2.equals(this.c)) {
            this.c = var2;
            this.getComponents().clear();

            for (String var4 : (Iterable<String>)(Object)var2) {
               this.getComponents().add(new ProxyModuleComponent(var4, this, this.getHeaderHeight()));
            }
         }
      }
   }

   @Override
   public void render(GuiGraphicsExtractor var1, int var2, int var3, float var4, String var5) {
      if (this.m()) {
         this.n();
         if (this.isDragging()) {
            this.setX(var2 - this.getDragOffsetX());
            this.setY(var3 - this.getDragOffsetY());
         }

         boolean var6 = var5 != null && !var5.isEmpty();
         int var7 = this.getHeaderHeight();
         if (this.isExpanded()) {
            var7++;

            for (GuiComponent var9 : (Iterable<GuiComponent>)(Object)this.getComponents()) {
               if (var6 && var9 instanceof ProxyModuleComponent var10) {
                  if (!var10.a().toLowerCase().contains(var5.toLowerCase())) {
                     var9.setVisible(false);
                     continue;
                  }

                  var9.setVisible(true);
               } else {
                  var9.setVisible(true);
               }

               if (var9.isVisible()) {
                  var9.setX(this.getX());
                  var9.setY(this.getY() + var7);
                  var7 += var9.getHeight();
               }
            }
         }

         this.setContentHeight(var7);
         Render2DUtil.a(var1, this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeaderHeight(), new Color(20, 20, 25, 200));
         Color var11 = ClickGuiScreen.a(this.getY(), 200);
         Render2DUtil.a(var1, this.getX(), this.getY() + this.getHeaderHeight() - 1, this.getX() + this.getWidth(), this.getY() + this.getHeaderHeight(), var11);
         Night.fontManager.drawShadowedText(var1, "PingBypass", this.getX() + this.getPadding(), this.getY() + 2, Color.WHITE);
         if (this.isExpanded()) {
            Render2DUtil.a(
               var1, this.getX(), this.getY() + this.getHeaderHeight(), this.getX() + this.getWidth(), this.getY() + var7 + 1, new Color(15, 15, 20, 180)
            );

            for (GuiComponent var13 : (Iterable<GuiComponent>)(Object)this.getComponents()) {
               if (var13.isVisible()) {
                  var13.render(var1, var2, var3, var4);
               }
            }
         }
      }
   }

   @Override
   public void render(GuiGraphicsExtractor var1, int var2, int var3, float var4) {
      this.render(var1, var2, var3, var4, "");
   }

   @Override
   public void mouseClicked(double var1, double var3, int var5) {
      if (this.m()) {
         if (this.isHeaderHovered(var1, var3)) {
            if (var5 == 0) {
               this.setDragging(true);
               this.setDragOffsetX((int)(var1 - this.getX()));
               this.setDragOffsetY((int)(var3 - this.getY()));
            } else if (var5 == 1) {
               this.setExpanded(!this.isExpanded());
            }
         }

         if (this.isExpanded()) {
            for (GuiComponent var7 : (Iterable<GuiComponent>)(Object)this.getComponents()) {
               if (var7.isVisible()) {
                  var7.mouseClicked(var1, var3, var5);
               }
            }
         }
      }
   }

   @Override
   public void mouseReleased(double var1, double var3, int var5) {
      if (this.m()) {
         if (var5 == 0) {
            this.setDragging(false);
         }

         for (GuiComponent var7 : (Iterable<GuiComponent>)(Object)this.getComponents()) {
            if (var7.isVisible()) {
               var7.mouseReleased(var1, var3, var5);
            }
         }
      }
   }

   @Override
   public void mouseScrolled(double var1, double var3, double var5, double var7) {
      if (this.m()) {
         super.mouseScrolled(var1, var3, var5, var7);
      }
   }

   @Override
   public void keyPressed(int var1, int var2, int var3) {
      if (this.m()) {
         if (this.isExpanded()) {
            for (GuiComponent var5 : (Iterable<GuiComponent>)(Object)this.getComponents()) {
               if (var5.isVisible()) {
                  var5.keyPressed(var1, var2, var3);
               }
            }
         }
      }
   }

   @Override
   public void charTyped(char var1, int var2) {
      if (this.m()) {
         if (this.isExpanded()) {
            for (GuiComponent var4 : (Iterable<GuiComponent>)(Object)this.getComponents()) {
               if (var4.isVisible()) {
                  var4.charTyped(var1, var2);
               }
            }
         }
      }
   }
}
