package night.gui.component;

import java.awt.Color;
import java.util.List;
import lombok.Generated;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import night.Night;
import night.gui.ClickGuiScreen;
import night.module.impl.core.ClickGuiModule;
import night.util.color.TextFormattingUtil;
import night.util.render.Render2DUtil;

public class DescriptionPanel {
   private String a = "";
   private int b;
   private int c;
   private int d;
   private int e;
   private int f = 0;
   private int g = 0;
   private int h = 3;
   private boolean i = false;

   public DescriptionPanel(int var1, int var2, int var3, int var4) {
      this.b = var1;
      this.c = var2;
      this.d = var3;
      this.e = var4;
   }

   public void a(GuiGraphicsExtractor var1, int var2, int var3, float var4) {
      if (this.i) {
         this.a(var2 - this.f);
         this.b(var3 - this.g);
      }

      boolean var5 = Night.moduleManager.getModule(ClickGuiModule.class).glowAccent.getValue();
      Render2DUtil.a(var1, this.b, this.c, this.b + this.d, this.c + this.e, ClickGuiScreen.a(this.c, 55));
      if (var5) {
         Render2DUtil.a(var1, this.b, this.c, this.b + this.d, this.c + 1, ClickGuiScreen.a(this.c, 220));
      }

      Color var6 = ClickGuiScreen.a(this.c, 220);
      Render2DUtil.a(var1, this.b, this.c + this.e - 1, this.b + this.d, this.c + this.e, var6);
      Night.fontManager.drawShadowedText(var1, "\u2139 Description", this.b + this.h, this.c + 2, Color.WHITE);
      Night.fontManager
         .drawShadowedText(var1, "[Ctrl+F]", this.b + this.d - this.h - Night.fontManager.getStringWidth("[Ctrl+F]"), this.c + 2, new Color(130, 130, 140));
      List var7;
      if (this.a.isEmpty()) {
         var7 = List.of("Hover over a module or setting to view details.", "Ctrl+F to search");
      } else {
         var7 = TextFormattingUtil.a_List_String_int(this.a, this.d - this.h * 2);
      }

      int var8 = var7.size() * Night.fontManager.getFontHeight() + 6;
      Render2DUtil.a(var1, this.b, this.c + this.e, this.b + this.d, this.c + this.e + var8, new Color(14, 13, 20, 195));
      int var9 = 0;

      for (String var11 : (Iterable<String>)(Object)var7) {
         Color var12;
         if (this.a.isEmpty()) {
            var12 = var9 == 1 ? new Color(125, 125, 135) : new Color(140, 140, 150);
         } else {
            var12 = Color.WHITE;
         }

         Night.fontManager.drawShadowedText(var1, var11, this.b + this.h, this.c + this.e + 3 + Night.fontManager.getFontHeight() * var9, var12);
         var9++;
      }

      Color var13 = ClickGuiScreen.a(this.c, 130);
      Render2DUtil.b(var1, this.b, this.c, this.b + this.d, this.c + this.e + var8, var13);
   }

   public void a(double var1, double var3, int var5) {
      if (this.a(var1, var3) && var5 == 0) {
         this.i = true;
         this.f = (int)(var1 - this.b());
         this.g = (int)(var3 - this.c());
      }
   }

   public void b(double var1, double var3, int var5) {
      if (var5 == 0) {
         this.i = false;
      }
   }

   public boolean a(double var1, double var3) {
      return this.b <= var1 && this.c <= var3 && this.b + this.d > var1 && this.c + this.e > var3;
   }

   @Generated
   public String a() {
      return this.a;
   }

   @Generated
   public int b() {
      return this.b;
   }

   @Generated
   public int c() {
      return this.c;
   }

   @Generated
   public void a(String var1) {
      this.a = var1;
   }

   @Generated
   public void a(int var1) {
      this.b = var1;
   }

   @Generated
   public void b(int var1) {
      this.c = var1;
   }
}
