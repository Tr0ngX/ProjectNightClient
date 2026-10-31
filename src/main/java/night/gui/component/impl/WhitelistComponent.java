package night.gui.component.impl;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Generated;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Util;
import net.minecraft.util.Util.OS;
import night.Night;
import night.gui.ClickGuiScreen;
import night.gui.WhitelistEditorScreen;
import night.gui.component.CategoryFrame;
import night.gui.component.GuiComponent;
import night.setting.impl.WhitelistSetting;
import night.util.render.Render2DUtil;
import night.util.world.PacketAndRenderUtil;
import night.util.world.RegistryLookupUtil;

public class WhitelistComponent extends GuiComponent {
   private final WhitelistSetting a;
   private boolean b = false;
   private String c = "";
   private boolean d = false;
   private boolean e = false;
   private int horizontalPadding = 0;
   private String g = "";
   private boolean h = false;
   private int i = 0;
   private float j = 0.0F;
   private final List<String> k;
   private List<String> l = new ArrayList<>();
   private final int m = 6;
   private float n = 0.0F;
   private float o = 0.0F;
   private final int p = 12;
   private final int q = 12;
   private final int r = 2;
   private final int s = 5;
   private final int t = 15;

   public WhitelistComponent(WhitelistSetting var1, CategoryFrame var2, int var3) {
      super(var1, var2, var3, var1.getDescription());
      this.a = var1;
      this.k = this.d();
   }

   @Override
   public void render(GuiGraphicsExtractor var1, int var2, int var3, float var4) {
      boolean var5 = !this.c.equals(this.g);
      this.g = this.c;
      float var6 = 0.0F;
      if (var5) {
         var6 = this.b();
      }

      this.a();
      this.c();
      if (var5) {
         this.a_void_float(var6);
         this.c();
      }

      this.n = this.n + (this.o - this.n) * 0.5F;
      Render2DUtil.a(
         var1,
         this.getX() + this.getHorizontalPadding() + 1,
         this.getY(),
         this.getX() + this.getWidth() - this.getHorizontalPadding() - 1,
         this.getY() + 13 - 1,
         ClickGuiScreen.a(this.getY(), 100)
      );
      Render2DUtil.a(
         var1,
         this.getX() + this.getHorizontalPadding() + 1,
         this.getY(),
         this.getX() + this.getHorizontalPadding() + 2,
         this.getY() + this.getHeight() - 1,
         ClickGuiScreen.a(this.getY(), 30)
      );
      Night.fontManager.drawShadowedText(var1, this.a.getDisplayName(), this.getX() + this.getTextOffset() + 1, this.getY() + 2, Color.WHITE);
      Night.fontManager
         .drawShadowedText(
            var1,
            "" + ChatFormatting.GRAY + this.a.getEntries().size(),
            this.getX() + this.getWidth() - this.getTextOffset() - 1 - Night.fontManager.getStringWidth(this.a.getEntries().size() + ""),
            this.getY() + 2,
            Color.WHITE
         );
      if (this.b) {
         int var7 = this.getY() + this.getParentFrame().getHeaderHeight();
         Render2DUtil.a(
            var1,
            this.getX() + this.getHorizontalPadding() + 1 + 2,
            var7,
            this.getX() + this.getWidth() - this.getHorizontalPadding() - 1,
            var7 + 12,
            ClickGuiScreen.a(this.getY(), 30)
         );
         String var8 = this.c;
         if (this.d) {
            String var9 = Night.clickGui.f() ? "|" : " ";
            if (this.horizontalPadding <= var8.length()) {
               var8 = var8.substring(0, this.horizontalPadding) + var9 + var8.substring(this.horizontalPadding);
            }
         } else {
            var8 = "Search...";
         }

         if (this.d && this.c.isEmpty()) {
            String var34 = Night.clickGui.f() ? "|" : " ";
            Night.fontManager
               .drawShadowedText(var1, var34, this.getX() + this.getTextOffset() + 2 + 2, var7 + 6 - Night.fontManager.getFontHeight() / 2, Color.WHITE);
            Night.fontManager
               .drawShadowedText(
                  var1,
                  "Search...",
                  this.getX() + this.getTextOffset() + 2 + 2 + Night.fontManager.getStringWidth(" "),
                  var7 + 6 - Night.fontManager.getFontHeight() / 2,
                  Color.GRAY
               );
         } else {
            Night.fontManager
               .drawShadowedText(
                  var1,
                  var8,
                  this.getX() + this.getTextOffset() + 2 + 2,
                  var7 + 6 - Night.fontManager.getFontHeight() / 2,
                  this.d ? (this.e ? ClickGuiScreen.a(this.getY(), 255) : Color.WHITE) : Color.GRAY
               );
         }

         int var35 = var7 + 12;
         int var10 = Math.min(this.l.size(), 6);
         int var11 = this.getX() + this.getWidth() - this.getHorizontalPadding() - 5 - 1;
         List var12 = this.a.getEntryIds();
         String[] var13 = this.c.isEmpty() ? new String[0] : this.c.toLowerCase().split(" ");

         for (int var14 = 0; var14 < var10; var14++) {
            int var15 = (int)(var14 + this.n);
            if (var15 >= 0 && var15 < this.l.size()) {
               String var16 = this.l.get(var15);
               boolean var17 = var12.contains(var16);
               String var18 = var16.replace("minecraft:", "");
               Color var19 = var17 ? Color.WHITE : Color.GRAY;
               Color var20 = var17 ? Color.YELLOW : Color.LIGHT_GRAY;
               int var21 = var35 + var14 * 12;
               Render2DUtil.a(
                  var1,
                  this.getX() + this.getHorizontalPadding() + 1 + 2,
                  var21,
                  this.getX() + this.getWidth() - this.getHorizontalPadding() - 1,
                  var21 + 12,
                  ClickGuiScreen.a(this.getY(), 30)
               );
               int var22 = this.getX() + this.getTextOffset() + 2 + 2;
               int var23 = var11 - 15 + 5;
               boolean var24 = var2 >= var22 && var2 <= var23 && var3 >= var21 && var3 < var21 + 12;
               List var25 = this.a_List_String_StringArr_Color_Color(var18, var13, var19, var20);
               int var26 = 0;

               for (WhitelistComponent.ColoredSegment var28 : (Iterable<WhitelistComponent.ColoredSegment>)(Object)var25) {
                  var26 += Night.fontManager.getStringWidth(var28.a);
               }

               int var40 = var23 - var22;
               List var41;
               if (!var24 && var26 > var40) {
                  var41 = this.a_List_List_int(var25, var40);
               } else {
                  var41 = var25;
               }

               int var29 = var22;

               for (WhitelistComponent.ColoredSegment var31 : (Iterable<WhitelistComponent.ColoredSegment>)(Object)var41) {
                  Night.fontManager.drawShadowedText(var1, var31.a, var29, var21 + 2, var31.b);
                  var29 += Night.fontManager.getStringWidth(var31.a);
               }

               String var42 = this.d && !var17 ? "+" : "-";
               int var43 = var11 - 15 + 5;
               boolean var32 = var2 >= var43 && var2 <= var43 + 15 && var3 >= var21 && var3 < var21 + 12;
               Color var33;
               if (var42.equals("+")) {
                  var33 = var32 ? new Color(0, 255, 0) : Color.WHITE;
               } else {
                  var33 = var32 ? new Color(255, 0, 0) : Color.WHITE;
               }

               Night.fontManager.drawShadowedText(var1, var42, var43, var21 + 2, var33);
            }
         }

         if (this.l.size() > 6) {
            float var36 = Math.max(0, this.l.size() - 6);
            float var37 = var36 == 0.0F ? 0.0F : this.n / var36;
            int var38 = (int)(72 * (6.0F / this.l.size()));
            if (var38 < 20) {
               var38 = 20;
            }

            int var39 = (int)((72 - var38) * var37);
            Render2DUtil.a(var1, var11, var35, var11 + 5, var35 + 72, ClickGuiScreen.a(this.getY(), 40));
            Render2DUtil.a(var1, var11, var35 + var39, var11 + 5, var35 + var39 + var38, ClickGuiScreen.a(this.getY(), 200));
         }
      }
   }

   @Override
   public void mouseClicked(double var1, double var3, int var5) {
      if (this.isHovered(var1, var3) && var5 == 1) {
         this.b = !this.b;
      }

      if (!this.isHovered(var1, var3) || var5 != 0 || this.b && !(var3 < this.getY() + this.getParentFrame().getHeaderHeight())) {
         if (this.b) {
            if (var5 == 0) {
               int var6 = this.getY() + this.getParentFrame().getHeaderHeight();
               if (var1 >= this.getX() + this.getHorizontalPadding() + 1 + 2
                  && var1 <= this.getX() + this.getWidth() - this.getHorizontalPadding() - 1
                  && var3 >= var6
                  && var3 <= var6 + 12) {
                  this.d = true;
                  this.e = false;
                  this.horizontalPadding = this.c.length();
                  return;
               }

               if (!this.c(var1, var3)) {
                  this.d = false;
                  this.e = false;
                  this.c = "";
                  this.horizontalPadding = 0;
               }

               if (this.l.size() > 6 && this.d(var1, var3)) {
                  this.h = true;
                  this.i = (int)var3;
                  this.j = this.n;
                  return;
               }

               if (!this.c(var1, var3)) {
                  return;
               }

               int var7 = this.getX() + this.getHorizontalPadding() + 1 + 2;
               int var8 = this.getX() + this.getWidth() - this.getHorizontalPadding() - 1;
               if (var1 < var7 || var1 > var8) {
                  return;
               }

               int var9 = this.getY() + this.getParentFrame().getHeaderHeight() + 12;
               if (var3 >= var9 && var3 < var9 + 72) {
                  int var10 = (int)(var3 - var9);
                  int var11 = (int)this.n + var10 / 12;
                  if (var11 >= 0 && var11 < this.l.size()) {
                     String var12 = this.l.get(var11);
                     int var13 = this.getX() + this.getWidth() - this.getHorizontalPadding() - 5 - 1 - 15 + 5;
                     int var14 = var9 + var10 / 12 * 12;
                     if (var1 >= var13 && var1 <= var13 + 15 && var3 >= var14 && var3 < var14 + 12) {
                        if (this.a.getEntryIds().contains(var12)) {
                           if (this.a.getType() == WhitelistSetting.WhitelistType.BLOCKS) {
                              this.a.remove(RegistryLookupUtil.b(var12));
                           } else {
                              this.a.remove(RegistryLookupUtil.a(var12));
                           }

                           this.a.remove(var12);
                        } else if (this.a.getType() == WhitelistSetting.WhitelistType.BLOCKS) {
                           this.a.add(RegistryLookupUtil.b(var12));
                        } else {
                           this.a.add(RegistryLookupUtil.a(var12));
                        }
                     }
                  }
               }
            }
         }
      } else {
         mc.gui.setScreen(new WhitelistEditorScreen(this.a, mc.gui.screen()));
      }
   }

   @Override
   public void mouseReleased(double var1, double var3, int var5) {
      if (var5 == 0 && this.h) {
         this.h = false;
      }
   }

   @Override
   public void mouseDragged(double var1, double var3, int var5, double var6, double var8) {
      if (this.h && this.l.size() > 6) {
         float var10 = Math.max(0, this.l.size() - 6);
         float var11 = (float)(var3 - this.i) / 72;
         this.o = this.j + var10 * var11;
         this.c();
      }
   }

   @Override
   public void mouseScrolled(double var1, double var3, double var5, double var7) {
      if (this.b && this.c(var1, var3)) {
         this.o -= (float)var7;
         this.c();
      }
   }

   @Override
   public void keyPressed(int var1, int var2, int var3) {
      if (this.d) {
         mc.getWindow();
         boolean var4 = PacketAndRenderUtil.a_bool_int(Util.getPlatform() == OS.OSX ? 343 : 341);
         if (var1 == 256) {
            this.d = false;
            this.e = false;
            this.c = "";
            this.horizontalPadding = 0;
         } else if (var1 == 257) {
            this.d = false;
            this.e = false;
            this.c = "";
            this.horizontalPadding = 0;
         } else if (var1 == 259) {
            if (this.e) {
               this.c = "";
               this.horizontalPadding = 0;
               this.e = false;
            } else if (this.horizontalPadding > 0) {
               this.c = this.c.substring(0, this.horizontalPadding - 1) + this.c.substring(this.horizontalPadding);
               this.horizontalPadding--;
            }
         } else if (var1 == 261) {
            if (this.horizontalPadding < this.c.length()) {
               this.c = this.c.substring(0, this.horizontalPadding) + this.c.substring(this.horizontalPadding + 1);
            }
         } else if (var1 == 263) {
            if (this.horizontalPadding > 0) {
               this.horizontalPadding--;
            }
         } else if (var1 == 262) {
            if (this.horizontalPadding < this.c.length()) {
               this.horizontalPadding++;
            }
         } else if (var4) {
            if (var1 == 86) {
               try {
                  String var5 = mc.keyboardHandler.getClipboard();
                  if (var5 != null) {
                     this.c = this.c.substring(0, this.horizontalPadding) + var5 + this.c.substring(this.horizontalPadding);
                     this.horizontalPadding = this.horizontalPadding + var5.length();
                  }
               } catch (Exception var7) {
                  Night.LOGGER.error("{}: Failed to process clipboard paste", var7.getClass().getName(), var7);
               }
            } else if (var1 == 67 && this.e) {
               try {
                  mc.keyboardHandler.setClipboard(this.c);
               } catch (Exception var6) {
                  Night.LOGGER.error("{}: Failed to process clipboard change", var6.getClass().getName(), var6);
               }
            } else if (var1 == 65) {
               this.e = true;
               this.horizontalPadding = this.c.length();
            }
         }
      }
   }

   @Override
   public void charTyped(char var1, int var2) {
      if (this.d && !Character.isISOControl(var1)) {
         if (this.e) {
            this.c = String.valueOf(var1);
            this.e = false;
            this.horizontalPadding = 1;
         } else {
            this.c = this.c.substring(0, this.horizontalPadding) + var1 + this.c.substring(this.horizontalPadding);
            this.horizontalPadding++;
         }
      }
   }

   @Override
   public int getHeight() {
      int var1 = this.getParentFrame().getHeaderHeight();
      return !this.b ? var1 : var1 + 12 + 72;
   }

   @Override
   public boolean isHovered(double var1, double var3) {
      return this.getX() + this.getHorizontalPadding() <= var1
         && this.getY() <= var3
         && this.getX() + this.getWidth() - this.getHorizontalPadding() > var1
         && this.getY() + this.getParentFrame().getHeaderHeight() > var3;
   }

   public boolean b_bool_double_double(double var1, double var3) {
      return this.b && (this.c(var1, var3) || this.h);
   }

   private boolean c(double var1, double var3) {
      int var5 = this.getY() + this.getParentFrame().getHeaderHeight();
      return var1 >= this.getX() + this.getHorizontalPadding() + 1 + 2
         && var1 <= this.getX() + this.getWidth() - this.getHorizontalPadding() - 1
         && var3 >= var5
         && var3 <= var5 + 84;
   }

   private boolean d(double var1, double var3) {
      if (this.b && this.l.size() > 6) {
         int var5 = this.getY() + this.getParentFrame().getHeaderHeight() + 12;
         int var6 = this.getX() + this.getWidth() - this.getHorizontalPadding() - 5 - 1;
         return var1 >= var6 && var1 <= var6 + 5 && var3 >= var5 && var3 <= var5 + 72;
      } else {
         return false;
      }
   }

   private void a() {
      List<String> var1;
      if (this.c.isEmpty()) {
         if (this.d) {
            var1 = new ArrayList<>(this.k);
         } else {
            List var2 = this.a.getEntryIds();
            var1 = this.k.stream().filter(var2::contains).collect(Collectors.toList());
         }

         ((List<String>)var1).sort((var0, var1x) -> {
            int var2x = var0.compareToIgnoreCase(var1x);
            return var2x == 0 ? Integer.compare(var0.length(), var1x.length()) : var2x;
         });
      } else {
         String[] var3 = this.c.toLowerCase().split(" ");
         var1 = this.k.stream().filter(var1x -> {
            String var2x = var1x.toLowerCase();

            for (String var5 : var3) {
               if (!var5.isEmpty() && !var2x.contains(var5)) {
                  return false;
               }
            }

            return true;
         }).collect(Collectors.toList());
         var1.sort((var2x, var3x) -> {
            int var4 = this.a_int_String_String_StringArr(var2x, var3x, var3);
            if (var4 != 0) {
               return var4;
            }

            int var5 = var2x.compareToIgnoreCase(var3x);
            return var5 == 0 ? Integer.compare(var2x.length(), var3x.length()) : var5;
         });
      }

      this.l = var1;
   }

   private int a_int_String_String_StringArr(String var1, String var2, String[] var3) {
      int var4 = this.a_int_String_StringArr(var1, var3);
      int var5 = this.a_int_String_StringArr(var2, var3);
      return Integer.compare(var4, var5);
   }

   private int a_int_String_StringArr(String var1, String[] var2) {
      int var3 = var1.length();
      String var4 = var1.toLowerCase();

      for (String var7 : var2) {
         if (!var7.isEmpty() && var4.contains(var7.toLowerCase())) {
            int var8 = var4.split(var7.toLowerCase())[0].length();
            if (var8 < var3) {
               var3 = var8;
            }
         }
      }

      return var3;
   }

   private float b() {
      int var1 = this.l.size();
      int var2 = Math.max(0, var1 - 6);
      return var2 == 0 ? 0.0F : this.n / var2;
   }

   private void a_void_float(float var1) {
      int var2 = this.l.size();
      int var3 = Math.max(0, var2 - 6);
      float var4 = var1 * var3;
      this.n = var4;
      this.o = var4;
   }

   private void c() {
      int var1 = this.l.size();
      int var2 = Math.max(0, var1 - 6);
      if (this.o < 0.0F) {
         this.o = 0.0F;
      }

      if (this.o > var2) {
         this.o = var2;
      }

      if (this.n < 0.0F) {
         this.n = 0.0F;
      }

      if (this.n > var2) {
         this.n = var2;
      }
   }

   private List<String> d() {
      ArrayList var1 = new ArrayList();
      if (this.a.getType() == WhitelistSetting.WhitelistType.BLOCKS) {
         BuiltInRegistries.BLOCK.entrySet().forEach(var1x -> var1.add(((ResourceKey)var1x.getKey()).identifier().toString()));
      } else {
         BuiltInRegistries.ITEM.entrySet().forEach(var1x -> var1.add(((ResourceKey)var1x.getKey()).identifier().toString()));
      }

      ((List<String>)var1).sort((var0, var1x) -> {
         int var2 = var0.compareToIgnoreCase(var1x);
         return var2 == 0 ? Integer.compare(var0.length(), var1x.length()) : var2;
      });
      return var1;
   }

   private List<WhitelistComponent.ColoredSegment> a_List_List_int(List<WhitelistComponent.ColoredSegment> var1, int var2) {
      ArrayList var3 = new ArrayList();
      int var4 = 0;

      for (WhitelistComponent.ColoredSegment var6 : (Iterable<WhitelistComponent.ColoredSegment>)(Object)var1) {
         int var7 = Night.fontManager.getStringWidth(var6.a);
         if (var4 + var7 > var2) {
            int var8 = var2 - var4;
            if (var8 > 0) {
               StringBuilder var9 = new StringBuilder();

               for (int var10 = 0; var10 < var6.a.length(); var10++) {
                  char var11 = var6.a.charAt(var10);
                  int var12 = Night.fontManager.getStringWidth(String.valueOf(var11));
                  if (var12 > var8 - Night.fontManager.getStringWidth("\u2026")) {
                     break;
                  }

                  var9.append(var11);
                  var8 -= var12;
               }

               var9.append("\u2026");
               var3.add(new WhitelistComponent.ColoredSegment(var9.toString(), var6.b));
            } else {
               var3.add(new WhitelistComponent.ColoredSegment("\u2026", Color.GRAY));
            }
            break;
         }

         var3.add(var6);
         var4 += var7;
      }

      return var3;
   }

   private List<WhitelistComponent.ColoredSegment> a_List_String_StringArr_Color_Color(String var1, String[] var2, Color var3, Color var4) {
      if (var2.length == 0) {
         ArrayList var13 = new ArrayList();
         var13.add(new WhitelistComponent.ColoredSegment(var1, var3));
         return var13;
      }

      String var5 = var1.toLowerCase();
      ArrayList var6 = new ArrayList();

      for (String var10 : var2) {
         if (!var10.isEmpty()) {
            int var11 = 0;

            while (true) {
               int var12 = var5.indexOf(var10, var11);
               if (var12 == -1) {
                  break;
               }

               var6.add(new WhitelistComponent.MatchSpan(var12, var12 + var10.length()));
               var11 = var12 + var10.length();
            }
         }
      }

      List var14 = this.a_List_List(var6);
      ArrayList var15 = new ArrayList();
      int var16 = 0;

      for (WhitelistComponent.MatchSpan var18 : (Iterable<WhitelistComponent.MatchSpan>)(Object)var14) {
         if (var18.a > var16) {
            var15.add(new WhitelistComponent.ColoredSegment(var1.substring(var16, var18.a), var3));
         }

         var15.add(new WhitelistComponent.ColoredSegment(var1.substring(var18.a, var18.b), var4));
         var16 = var18.b;
      }

      if (var16 < var1.length()) {
         var15.add(new WhitelistComponent.ColoredSegment(var1.substring(var16), var3));
      }

      return var15;
   }

   private List<WhitelistComponent.MatchSpan> a_List_List(List<WhitelistComponent.MatchSpan> var1) {
      if (var1.isEmpty()) {
         return var1;
      }

      var1.sort(Comparator.comparingInt(var0 -> var0.a));
      ArrayList var2 = new ArrayList();
      WhitelistComponent.MatchSpan var3 = (WhitelistComponent.MatchSpan)var1.getFirst();

      for (int var4 = 1; var4 < var1.size(); var4++) {
         WhitelistComponent.MatchSpan var5 = (WhitelistComponent.MatchSpan)var1.get(var4);
         if (var5.a <= var3.b) {
            var3.b = Math.max(var3.b, var5.b);
         } else {
            var2.add(var3);
            var3 = var5;
         }
      }

      var2.add(var3);
      return var2;
   }

   static class ColoredSegment {
      String a;
      Color b;

      @Generated
      public ColoredSegment(String var1, Color var2) {
         this.a = var1;
         this.b = var2;
      }
   }

   static class MatchSpan {
      int a;
      int b;

      @Generated
      public MatchSpan(int var1, int var2) {
         this.a = var1;
         this.b = var2;
      }
   }
}
