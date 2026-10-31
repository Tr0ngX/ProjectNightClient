package night.gui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.block.Block;
import night.Night;
import night.setting.impl.WhitelistSetting;
import night.util.animation.Animation;
import night.util.animation.EasingUtil;
import night.util.math.TimerUtil;
import night.util.render.Render2DUtil;
import night.util.world.PacketAndRenderUtil;
import night.util.world.RegistryLookupUtil;

public class WhitelistEditorScreen extends Screen {
   private final WhitelistSetting a;
   private final Screen b;
   private final List<WhitelistEditorScreen.EntryItem> c;
   private String d = "";
   private String e = "";
   private long f = System.currentTimeMillis();
   private float g = 0.0F;
   private float h = 0.0F;
   private float i = 0.0F;
   private float j = 0.0F;
   private final Animation k = new Animation(320, EasingUtil.Easing.EASE_OUT_CUBIC);
   private boolean l = false;
   private final TimerUtil m = new TimerUtil();
   private boolean n = false;
   private int o;
   private int p;
   private int q;
   private int r;
   private static final String[] s = new String[]{
      "white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray", "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black"
   };

   public WhitelistEditorScreen(WhitelistSetting var1, Screen var2) {
      super(Component.literal("night-whitelist-editor"));
      this.a = var1;
      this.b = var2;
      this.c = a_List_WhitelistSetting(var1);
   }

   private static String a_String_String(String var0) {
      for (String var4 : s) {
         if (var0.equals(var4)) {
            return "";
         }

         if (var0.startsWith(var4 + "_")) {
            return var0.substring(var4.length() + 1);
         }
      }

      return null;
   }

   private static List<WhitelistEditorScreen.EntryItem> a_List_WhitelistSetting(WhitelistSetting var0) {
      ArrayList<WhitelistEditorScreen.EntryItem> var1 = new ArrayList<>();
      LinkedHashMap<String, List<WhitelistEditorScreen.EntryItem>> var2 = new LinkedHashMap<>();
      if (var0.getType() == WhitelistSetting.WhitelistType.BLOCKS) {
         BuiltInRegistries.BLOCK.entrySet().forEach(var2x -> {
            Block var3 = (Block)var2x.getValue();
            String var4x = ((ResourceKey)var2x.getKey()).identifier().toString();
            Item var5x = var3.asItem();
            ItemStack var6x = var5x == Items.AIR ? ItemStack.EMPTY : new ItemStack(var5x);
            WhitelistEditorScreen.EntryItem var7 = new WhitelistEditorScreen.EntryItem(var4x, var4x.replace("minecraft:", ""), var6x, var3);
            var1.add(var7);
            String var8 = a_String_String(var4x.substring("minecraft:".length()));
            if (var8 != null && !var8.isEmpty()) {
               ((java.util.List)var2.computeIfAbsent(var8, var0xx -> new ArrayList<>())).add(var7);
            }
         });
      } else if (var0.getType() == WhitelistSetting.WhitelistType.POTIONS) {
         BuiltInRegistries.POTION.entrySet().forEach(var1x -> {
            Potion var2x = (Potion)var1x.getValue();
            String var3 = ((ResourceKey)var1x.getKey()).identifier().toString();
            Holder var4x = BuiltInRegistries.POTION.wrapAsHolder(var2x);
            ItemStack var5x = new ItemStack(Items.SPLASH_POTION);
            var5x.set(DataComponents.POTION_CONTENTS, new PotionContents(var4x));
            String var6x = "Splash Potion of " + var2x.name().replace("_", " ");
            var1.add(new WhitelistEditorScreen.EntryItem(var3, var6x, var5x, var2x));
         });
      } else {
         BuiltInRegistries.ITEM.entrySet().forEach(var2x -> {
            Item var3 = (Item)var2x.getValue();
            String var4x = ((ResourceKey)var2x.getKey()).identifier().toString();
            WhitelistEditorScreen.EntryItem var5x = new WhitelistEditorScreen.EntryItem(var4x, var4x.replace("minecraft:", ""), new ItemStack(var3), var3);
            var1.add(var5x);
            String var6x = a_String_String(var4x.substring("minecraft:".length()));
            if (var6x != null && !var6x.isEmpty()) {
               ((java.util.List)var2.computeIfAbsent(var6x, var0xx -> new ArrayList<>())).add(var5x);
            }
         });
      }

      for (Entry var4 : (Iterable<Entry>)(Object)var2.entrySet()) {
         if (((List)var4.getValue()).size() >= 8) {
            WhitelistEditorScreen.EntryItem var5 = (WhitelistEditorScreen.EntryItem)((List)var4.getValue()).get(0);
            String var6 = ((String)var4.getKey()).replace("_", " ");
            var1.add(new WhitelistEditorScreen.EntryItem("group:" + (String)var4.getKey(), var6 + " (all colors)", var5.c, null));
         }
      }

      var1.sort((var0x, var1x) -> var0x.b.compareToIgnoreCase(var1x.b));
      return var1;
   }

   public void a() {
      this.l = true;
      this.k.setEasing(EasingUtil.Easing.EASE_IN_CUBIC);
   }

   public void extractBackground(GuiGraphicsExtractor var1, int var2, int var3, float var4) {
      Render2DUtil.a(var1, 0.0F, 0.0F, this.width, this.height, new Color(10, 8, 18, 150));
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
         this.o = (this.width - 310) / 2;
         this.p = this.o + 150 + 10;
         this.q = 20;
         this.r = this.height - 40;
         var1.pose().pushMatrix();
         var1.pose().translate(0.0F, (1.0F - var5) * -160.0F);
         List var6 = this.a.getEntryIds();
         List var7 = this.c.stream().filter(var1x -> var6.contains(var1x.a)).toList();
         List var8 = this.c.stream().filter(var1x -> !var6.contains(var1x.a)).toList();
         List var9 = this.d.isEmpty() ? var8 : this.a_List_List_String(var8, this.d);
         this.a(var1, var2, var3, this.o, var9, var6);
         this.a(var1, var2, var3, this.p, "Whitelist (" + var7.size() + ")", var7, var6, this.i, 2);
         var1.pose().popMatrix();
      }
   }

   private List<WhitelistEditorScreen.EntryItem> a_List_List_String(List<WhitelistEditorScreen.EntryItem> var1, String var2) {
      String[] var3 = var2.toLowerCase().split(" ");
      return var1.stream().filter(var1x -> {
         String var2x = var1x.b.toLowerCase();

         for (String var5 : var3) {
            if (!var5.isEmpty() && !var2x.contains(var5)) {
               return false;
            }
         }

         return true;
      }).toList();
   }

   private void a(
      GuiGraphicsExtractor var1, int var2, int var3, int var4, String var5, List<WhitelistEditorScreen.EntryItem> var6, List<String> var7, float var8, int var9
   ) {
      Render2DUtil.a(var1, var4, this.q, var4 + 150, this.q + 13, new Color(20, 20, 25, 200));
      Render2DUtil.a(var1, var4, this.q + 13 - 1, var4 + 150, this.q + 13, ClickGuiScreen.a(this.q, 200));
      Night.fontManager.drawShadowedText(var1, var5, var4 + 3, this.q + 2, Color.WHITE);
      int var10 = this.q + 13;
      int var11 = this.r - 13;
      Render2DUtil.a(var1, var4, var10, var4 + 150, var10 + var11, new Color(15, 15, 20, 180));
      Render2DUtil.b(var1, var4, this.q, var4 + 150, var10 + var11, ClickGuiScreen.a(this.q, 120));
      var1.enableScissor(var4, var10, var4 + 150, var10 + var11);
      int var12 = var11 / 18 + 2;
      int var13 = (int)var8;

      for (int var14 = 0; var14 < var12; var14++) {
         int var15 = var13 + var14;
         if (var15 >= 0 && var15 < var6.size()) {
            WhitelistEditorScreen.EntryItem var16 = (WhitelistEditorScreen.EntryItem)var6.get(var15);
            int var17 = var10 + Math.round(var14 * 18 - (var8 - var13) * 18.0F);
            if (var17 + 18 >= var10 && var17 <= var10 + var11) {
               this.a(var1, var2, var3, var4, var17, var16, var7.contains(var16.a), var9);
            }
         }
      }

      var1.disableScissor();
   }

   private void a(GuiGraphicsExtractor var1, int var2, int var3, int var4, List<WhitelistEditorScreen.EntryItem> var5, List<String> var6) {
      Render2DUtil.a(var1, var4, this.q, var4 + 150, this.q + 13, new Color(20, 20, 25, 200));
      Render2DUtil.a(var1, var4, this.q + 13 - 1, var4 + 150, this.q + 13, ClickGuiScreen.a(this.q, 200));
      if (this.d.isEmpty()) {
         String var7 = "Items (search...)";
         int var8 = Night.fontManager.getStringWidth(" ");
         if (this.n) {
            Night.fontManager.drawShadowedText(var1, "|", var4 + 3, this.q + 2, Color.WHITE);
         }

         Night.fontManager.drawShadowedText(var1, var7, var4 + 3 + var8, this.q + 2, Color.GRAY);
      } else {
         String var19 = this.d + (this.n ? "|" : " ");
         Night.fontManager.drawShadowedText(var1, var19, var4 + 3, this.q + 2, Color.WHITE);
      }

      int var20 = this.q + 13;
      int var21 = this.r - 13;
      Render2DUtil.a(var1, var4, var20, var4 + 150, var20 + var21, new Color(15, 15, 20, 180));
      Render2DUtil.b(var1, var4, this.q, var4 + 150, var20 + var21, ClickGuiScreen.a(this.q, 120));
      var1.enableScissor(var4, var20, var4 + 150, var20 + var21);
      long var9 = System.currentTimeMillis();
      int var11 = var21 / 18 + 2;
      int var12 = (int)this.g;

      for (int var13 = 0; var13 < var11; var13++) {
         int var14 = var12 + var13;
         if (var14 >= 0 && var14 < var5.size()) {
            WhitelistEditorScreen.EntryItem var15 = (WhitelistEditorScreen.EntryItem)var5.get(var14);
            int var16 = var20 + Math.round(var13 * 18 - (this.g - var12) * 18.0F);
            if (var16 + 18 >= var20 && var16 <= var20 + var21) {
               float var17 = EasingUtil.a(Mth.clamp((float)(var9 - this.f - var14 * 40) / 220.0F, 0.0F, 1.0F), EasingUtil.Easing.DECELERATE);
               int var18 = Math.round((1.0F - var17) * 40.0F);
               var1.pose().pushMatrix();
               var1.pose().translate(-var18, 0.0F);
               this.a(var1, var2, var3, var4, var16, var15, var6.contains(var15.a), 1);
               var1.pose().popMatrix();
            }
         }
      }

      var1.disableScissor();
   }

   private void a(GuiGraphicsExtractor var1, int var2, int var3, int var4, int var5, WhitelistEditorScreen.EntryItem var6, boolean var7, int var8) {
      Color var9 = var2 >= var4 && var2 < var4 + 150 && var3 >= var5 && var3 < var5 + 18 - 1
         ? new Color(255, 255, 255, 15)
         : (var7 ? new Color(255, 255, 255, 8) : new Color(0, 0, 0, 0));
      if (var9.getAlpha() > 0) {
         Render2DUtil.a(var1, var4, var5, var4 + 150, var5 + 18 - 1, var9);
      }

      if (!var6.c.isEmpty()) {
         var1.item(var6.c, var4 + 2, var5 + 0);
      }

      Color var10 = var7 ? Color.YELLOW : Color.WHITE;
      Night.fontManager.drawShadowedText(var1, var6.b, var4 + 16 + 6, var5 + 8 - Night.fontManager.getFontHeight() / 2, var10);
   }

   public boolean mouseClicked(MouseButtonEvent var1, boolean var2) {
      double var3 = var1.x();
      double var5 = var1.y();
      if (var1.button() != 0) {
         return super.mouseClicked(var1, var2);
      }

      List var7 = this.a.getEntryIds();
      int var8 = this.q + 13;
      int var9 = this.r - 13;
      if (var5 < var8 || var5 >= var8 + var9) {
         return super.mouseClicked(var1, var2);
      }

      if (var3 >= this.o && var3 < this.o + 150) {
         List var13 = this.c.stream().filter(var1x -> !var7.contains(var1x.a)).toList();
         List var14 = this.d.isEmpty() ? var13 : this.a_List_List_String(var13, this.d);
         WhitelistEditorScreen.EntryItem var12 = this.a(var3, var5, var8, this.g, var14);
         if (var12 != null) {
            this.a_void_EntryItem(var12);
         }

         return true;
      } else if (var3 >= this.p && var3 < this.p + 150) {
         List var10 = this.c.stream().filter(var1x -> var7.contains(var1x.a)).toList();
         WhitelistEditorScreen.EntryItem var11 = this.a(var3, var5, var8, this.i, var10);
         if (var11 != null) {
            this.b(var11);
         }

         return true;
      } else {
         return super.mouseClicked(var1, var2);
      }
   }

   private WhitelistEditorScreen.EntryItem a(double var1, double var3, int var5, float var6, List<WhitelistEditorScreen.EntryItem> var7) {
      int var8 = (int)(var3 - var5);
      int var9 = (int)var6 + var8 / 18;
      return var9 >= 0 && var9 < var7.size() ? (WhitelistEditorScreen.EntryItem)var7.get(var9) : null;
   }

   private void a_void_EntryItem(WhitelistEditorScreen.EntryItem var1) {
      if (var1.a.startsWith("group:")) {
         String var2 = var1.a.substring("group:".length());
         if (this.a.getType() == WhitelistSetting.WhitelistType.BLOCKS) {
            BuiltInRegistries.BLOCK.entrySet().forEach(var2x -> {
               String var3 = ((ResourceKey)var2x.getKey()).identifier().toString().substring("minecraft:".length());
               if (var2.equals(a_String_String(var3)) || var3.equals(var2)) {
                  this.a.add(var2x.getValue());
               }
            });
         } else {
            BuiltInRegistries.ITEM.entrySet().forEach(var2x -> {
               String var3 = ((ResourceKey)var2x.getKey()).identifier().toString().substring("minecraft:".length());
               if (var2.equals(a_String_String(var3)) || var3.equals(var2)) {
                  this.a.add(var2x.getValue());
               }
            });
         }
      } else {
         if (this.a.getType() == WhitelistSetting.WhitelistType.BLOCKS) {
            this.a.add(RegistryLookupUtil.b(var1.a));
         } else if (this.a.getType() == WhitelistSetting.WhitelistType.POTIONS) {
            this.a.add(RegistryLookupUtil.c(var1.a));
         } else {
            this.a.add(RegistryLookupUtil.a(var1.a));
         }
      }
   }

   private void b(WhitelistEditorScreen.EntryItem var1) {
      if (this.a.getType() == WhitelistSetting.WhitelistType.BLOCKS) {
         this.a.remove(RegistryLookupUtil.b(var1.a));
      } else if (this.a.getType() == WhitelistSetting.WhitelistType.POTIONS) {
         this.a.remove(RegistryLookupUtil.c(var1.a));
      } else {
         this.a.remove(RegistryLookupUtil.a(var1.a));
      }
   }

   public boolean mouseScrolled(double var1, double var3, double var5, double var7) {
      if (var1 >= this.o && var1 < this.o + 150) {
         List var9 = this.a.getEntryIds();
         List var10 = this.c.stream().filter(var1x -> !var9.contains(var1x.a)).toList();
         List var11 = this.d.isEmpty() ? var10 : this.a_List_List_String(var10, this.d);
         this.h = this.a_float_float_int(this.h - (float)var7, var11.size());
      } else if (var1 >= this.p && var1 < this.p + 150) {
         this.j = this.a_float_float_int(this.j - (float)var7, this.a.getEntryIds().size());
      }

      this.g = this.g + (this.h - this.g) * 0.5F;
      this.i = this.i + (this.j - this.i) * 0.5F;
      return true;
   }

   private float a_float_float_int(float var1, int var2) {
      int var3 = this.r - 13;
      float var4 = Math.max(0.0F, var2 - var3 / 18.0F);
      return Math.max(0.0F, Math.min(var1, var4));
   }

   public boolean keyPressed(KeyEvent var1) {
      int var2 = var1.key();
      if (var2 == 256) {
         this.a();
         return true;
      } else if (var2 == 259 && !this.d.isEmpty()) {
         this.b(this.d.substring(0, this.d.length() - 1));
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
         this.b(this.d + var2);
         return true;
      } else {
         return super.charTyped(var1);
      }
   }

   private void b(String var1) {
      if (!var1.equals(this.e)) {
         this.f = System.currentTimeMillis();
         this.e = var1;
         this.g = 0.0F;
         this.h = 0.0F;
      }

      this.d = var1;
   }

   public boolean isPauseScreen() {
      return false;
   }

   static class EntryItem {
      final String a;
      final String b;
      final ItemStack c;

      EntryItem(String var1, String var2, ItemStack var3, Object var4) {
         this.a = var1;
         this.b = var2;
         this.c = var3;
      }
   }
}
