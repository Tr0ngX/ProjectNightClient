package night.gui;

import java.awt.Color;
import java.awt.Desktop;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import night.Night;
import night.manager.CustomImageManager;
import night.setting.impl.ImageSetting;
import night.util.animation.Animation;
import night.util.animation.EasingUtil;
import night.util.math.TimerUtil;
import night.util.render.Render2DUtil;
import night.util.world.PacketAndRenderUtil;

public class ImageSelectorScreen extends Screen {
   private final ImageSetting a;
   private final Screen b;
   private List<File> c = new ArrayList<>();
   private String d = "";
   private String e = "";
   private long f = System.currentTimeMillis();
   private float g = 0.0F;
   private float h = 0.0F;
   private String i = null;
   private CustomImageManager.CachedImage j = null;
   private final Animation k = new Animation(320, EasingUtil.Easing.EASE_OUT_CUBIC);
   private boolean l = false;
   private final TimerUtil m = new TimerUtil();
   private boolean n = false;
   private int o;
   private int p;
   private int q;
   private int r;
   private long s = 0L;
   private String t = null;

   public ImageSelectorScreen(ImageSetting var1, Screen var2) {
      super(Component.literal("night-image-select"));
      this.a = var1;
      this.b = var2;
      this.i = var1.getValue();
      this.a();
   }

   public void a() {
      if (Night.customImageManager != null) {
         this.c = Night.customImageManager.listImageFiles();
         if ((this.i == null || this.i.equals("None")) && !this.c.isEmpty()) {
            this.i = this.c.get(0).getName();
         }

         this.c();
      }
   }

   private void c() {
      if (this.i != null && !this.i.equalsIgnoreCase("None") && Night.customImageManager != null) {
         this.j = Night.customImageManager.getOrLoadPreview(this.i);
      } else {
         this.j = null;
      }
   }

   public void b() {
      this.l = true;
      this.k.setEasing(EasingUtil.Easing.EASE_IN_CUBIC);
   }

   public void extractBackground(GuiGraphicsExtractor var1, int var2, int var3, float var4) {
      Render2DUtil.a(var1, 0.0F, 0.0F, this.width, this.height, new Color(10, 8, 18, 160));
   }

   public void extractRenderState(GuiGraphicsExtractor var1, int var2, int var3, float var4) {
      super.extractRenderState(var1, var2, var3, var4);
      if (this.m.passedMs(400L)) {
         this.n = !this.n;
         this.m.reset();
      }

      float var5 = this.k.animateTo(this.l ? 0.0F : 1.0F);
      if (this.l && var5 <= 0.001F) {
         Minecraft.getInstance().gui.setScreen(this.b);
      } else {
         this.o = (this.width - 364) / 2;
         this.p = this.o + 175 + 14;
         this.q = Math.max(15, (this.height - 240) / 2);
         this.r = Math.min(260, this.height - 30);
         var1.pose().pushMatrix();
         var1.pose().translate(0.0F, (1.0F - var5) * -160.0F);
         List var6 = this.d.isEmpty() ? this.c : this.a_List_List_String(this.c, this.d);
         this.a(var1, var2, var3, this.o, var6);
         this.a(var1, var2, var3, this.p);
         var1.pose().popMatrix();
      }
   }

   private List<File> a_List_List_String(List<File> var1, String var2) {
      String var3 = var2.toLowerCase().trim();
      return var1.stream().filter(var1x -> var1x.getName().toLowerCase().contains(var3)).toList();
   }

   private void a(GuiGraphicsExtractor var1, int var2, int var3, int var4, List<File> var5) {
      Render2DUtil.a(var1, var4, this.q, var4 + 175, this.q + 15, new Color(20, 20, 25, 220));
      Render2DUtil.a(var1, var4, this.q + 15 - 1, var4 + 175, this.q + 15, ClickGuiScreen.a(this.q, 200));
      if (this.d.isEmpty()) {
         String var6 = "Images (search...)";
         int var7 = Night.fontManager.getStringWidth(" ");
         if (this.n) {
            Night.fontManager.drawShadowedText(var1, "|", var4 + 3, this.q + 3, Color.WHITE);
         }

         Night.fontManager.drawShadowedText(var1, var6, var4 + 3 + var7, this.q + 3, Color.GRAY);
      } else {
         String var14 = this.d + (this.n ? "|" : " ");
         Night.fontManager.drawShadowedText(var1, var14, var4 + 3, this.q + 3, Color.WHITE);
      }

      int var15 = this.q + 15;
      int var16 = this.r - 15;
      Render2DUtil.a(var1, var4, var15, var4 + 175, var15 + var16, new Color(15, 15, 20, 190));
      Render2DUtil.b(var1, var4, this.q, var4 + 175, var15 + var16, ClickGuiScreen.a(this.q, 120));
      if (!var5.isEmpty()) {
         var1.enableScissor(var4, var15, var4 + 175, var15 + var16);
         int var17 = var16 / 18 + 2;
         int var18 = (int)this.g;

         for (int var19 = 0; var19 < var17; var19++) {
            int var20 = var18 + var19;
            if (var20 >= 0 && var20 < var5.size()) {
               File var21 = (File)var5.get(var20);
               int var13 = var15 + Math.round(var19 * 18 - (this.g - var18) * 18.0F);
               if (var13 + 18 >= var15 && var13 <= var15 + var16) {
                  this.a(var1, var2, var3, var4, var13, var21);
               }
            }
         }

         var1.disableScissor();
      } else {
         Night.fontManager.drawShadowedText(var1, "No images found in:", var4 + 10, var15 + 20, Color.GRAY);
         Night.fontManager.drawShadowedText(var1, "Night/images", var4 + 10, var15 + 32, ClickGuiScreen.a(var15, 220));
         int var8 = var15 + 55;
         byte var9 = 16;
         boolean var10 = var2 >= var4 + 10 && var2 <= var4 + 175 - 10 && var3 >= var8 && var3 <= var8 + var9;
         Color var11 = var10 ? ClickGuiScreen.a(var8, 200) : new Color(30, 30, 40, 200);
         Render2DUtil.a(var1, var4 + 10, var8, var4 + 175 - 10, var8 + var9, var11);
         Render2DUtil.b(var1, var4 + 10, var8, var4 + 175 - 10, var8 + var9, ClickGuiScreen.a(var8, 150));
         int var12 = Night.fontManager.getStringWidth("Open Folder");
         Night.fontManager.drawShadowedText(var1, "Open Folder", var4 + (175 - var12) / 2, var8 + 4, Color.WHITE);
      }
   }

   private void a(GuiGraphicsExtractor var1, int var2, int var3, int var4, int var5, File var6) {
      String var7 = var6.getName();
      boolean var8 = var7.equals(this.i);
      boolean var9 = var7.equals(this.a.getValue());
      boolean var10 = var2 >= var4 && var2 < var4 + 175 && var3 >= var5 && var3 < var5 + 18 - 1;
      Color var11 = var8 ? new Color(255, 255, 255, 30) : (var10 ? new Color(255, 255, 255, 15) : new Color(0, 0, 0, 0));
      if (var11.getAlpha() > 0) {
         Render2DUtil.a(var1, var4, var5, var4 + 175, var5 + 18 - 1, var11);
      }

      if (var9) {
         Render2DUtil.a(var1, var4, var5, var4 + 3, var5 + 18 - 1, ClickGuiScreen.a(var5, 255));
      }

      Color var12 = var9 ? Color.YELLOW : (var8 ? ClickGuiScreen.a(var5, 255) : Color.WHITE);
      String var13 = var7;
      if (Night.fontManager.getStringWidth(var13) > 155) {
         while (Night.fontManager.getStringWidth(var13 + "...") > 155 && var13.length() > 3) {
            var13 = var13.substring(0, var13.length() - 1);
         }

         var13 = var13 + "...";
      }

      Night.fontManager.drawShadowedText(var1, var13, var4 + 8, var5 + (18 - Night.fontManager.getFontHeight()) / 2, var12);
   }

   private void a(GuiGraphicsExtractor var1, int var2, int var3, int var4) {
      Render2DUtil.a(var1, var4, this.q, var4 + 175, this.q + 15, new Color(20, 20, 25, 220));
      Render2DUtil.a(var1, var4, this.q + 15 - 1, var4 + 175, this.q + 15, ClickGuiScreen.a(this.q, 200));
      String var5 = "Preview" + (this.i != null ? ": " + this.i : "");
      if (Night.fontManager.getStringWidth(var5) > 165) {
         var5 = "Preview";
      }

      Night.fontManager.drawShadowedText(var1, var5, var4 + 5, this.q + 3, Color.WHITE);
      int var6 = this.q + 15;
      int var7 = this.r - 15;
      Render2DUtil.a(var1, var4, var6, var4 + 175, var6 + var7, new Color(15, 15, 20, 190));
      Render2DUtil.b(var1, var4, this.q, var4 + 175, var6 + var7, ClickGuiScreen.a(this.q, 120));
      if (this.j != null && this.j.d != null && this.j.c != null) {
         byte var22 = 12;
         int var23 = var4 + var22;
         int var10 = var6 + var22;
         int var11 = 175 - var22 * 2;
         int var12 = var7 - var22 * 2 - 28;
         Render2DUtil.a(var1, var23 - 1, var10 - 1, var23 + var11 + 1, var10 + var12 + 1, new Color(5, 5, 10, 220));
         Render2DUtil.b(var1, var23 - 1, var10 - 1, var23 + var11 + 1, var10 + var12 + 1, new Color(40, 40, 50, 150));
         float var13 = (float)this.j.e / Math.max(1, this.j.f);
         float var14 = (float)var11 / var12;
         int var15;
         int var16;
         if (var13 > var14) {
            var15 = var11;
            var16 = Math.max(1, Math.round(var11 / var13));
         } else {
            var16 = var12;
            var15 = Math.max(1, Math.round(var12 * var13));
         }

         int var17 = var23 + (var11 - var15) / 2;
         int var18 = var10 + (var12 - var16) / 2;
         var1.blit(RenderPipelines.GUI_TEXTURED, this.j.c, var17, var18, 0.0F, 0.0F, var15, var16, var15, var16, var15, var16, -1);
         String var19 = this.j.e + " x " + this.j.f + " px";
         int var20 = Night.fontManager.getStringWidth(var19);
         Night.fontManager.drawShadowedText(var1, var19, var4 + (175 - var20) / 2, var10 + var12 + 4, Color.LIGHT_GRAY);
         int var21 = Night.fontManager.getStringWidth("Double-click item to select");
         Night.fontManager.drawShadowedText(var1, "Double-click item to select", var4 + (175 - var21) / 2, var10 + var12 + 15, Color.GRAY);
      } else {
         String var8 = "Select an image on the left";
         int var9 = Night.fontManager.getStringWidth(var8);
         Night.fontManager.drawShadowedText(var1, var8, var4 + (175 - var9) / 2, var6 + var7 / 2 - 6, Color.GRAY);
      }
   }

   public boolean mouseClicked(MouseButtonEvent var1, boolean var2) {
      double var3 = var1.x();
      double var5 = var1.y();
      if (var1.button() != 0) {
         return super.mouseClicked(var1, var2);
      }

      int var7 = this.q + 15;
      int var8 = this.r - 15;
      if (this.c.isEmpty() && var3 >= this.o + 10 && var3 <= this.o + 175 - 10 && var5 >= var7 + 55 && var5 <= var7 + 71) {
         try {
            Desktop.getDesktop().open(CustomImageManager.IMAGES_DIR.toFile());
         } catch (Exception var15) {
         }

         return true;
      } else {
         if (var3 >= this.o && var3 < this.o + 175 && var5 >= var7 && var5 < var7 + var8) {
            List var9 = this.d.isEmpty() ? this.c : this.a_List_List_String(this.c, this.d);
            int var10 = (int)(var5 - var7);
            int var11 = (int)this.g + var10 / 18;
            if (var11 >= 0 && var11 < var9.size()) {
               String var12 = ((File)var9.get(var11)).getName();
               long var13 = System.currentTimeMillis();
               if (var2 || var12.equals(this.t) && var13 - this.s < 350L) {
                  this.a.setValue(var12);
                  if (Night.customImageManager != null) {
                     Night.customImageManager.setActiveImage(var12);
                  }

                  this.b();
                  return true;
               }

               this.i = var12;
               this.t = var12;
               this.s = var13;
               this.a.setValue(var12);
               if (Night.customImageManager != null) {
                  Night.customImageManager.setActiveImage(var12);
               }

               this.c();
               return true;
            }
         }

         return super.mouseClicked(var1, var2);
      }
   }

   public boolean mouseScrolled(double var1, double var3, double var5, double var7) {
      if (var1 >= this.o && var1 < this.o + 175) {
         List var9 = this.d.isEmpty() ? this.c : this.a_List_List_String(this.c, this.d);
         int var10 = this.r - 15;
         float var11 = Math.max(0.0F, var9.size() - var10 / 18.0F);
         this.h = Math.max(0.0F, Math.min(this.h - (float)var7, var11));
         this.g = this.g + (this.h - this.g) * 0.5F;
         return true;
      } else {
         return super.mouseScrolled(var1, var3, var5, var7);
      }
   }

   public boolean keyPressed(KeyEvent var1) {
      int var2 = var1.key();
      if (var2 == 256) {
         this.b();
         return true;
      } else if (var2 == 259 && !this.d.isEmpty()) {
         this.d = this.d.substring(0, this.d.length() - 1);
         this.g = 0.0F;
         this.h = 0.0F;
         return true;
      } else {
         return super.keyPressed(var1);
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
      if (!Character.isISOControl(var2)) {
         this.d = this.d + var2;
         this.g = 0.0F;
         this.h = 0.0F;
         return true;
      } else {
         return super.charTyped(var1);
      }
   }

   public boolean isPauseScreen() {
      return false;
   }
}
