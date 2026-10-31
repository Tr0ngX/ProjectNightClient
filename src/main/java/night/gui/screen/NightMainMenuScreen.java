package night.gui.screen;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import night.Night;
import night.gui.ClickGuiScreen;
import night.module.impl.core.ClickGuiModule;
import night.util.render.MenuBackgroundShader;
import night.util.render.Render2DUtil;

public class NightMainMenuScreen extends Screen {
   private final List<NightMainMenuScreen.MenuButton> a = new ArrayList<>();
   private static long b = -1L;

   public NightMainMenuScreen() {
      super(Component.literal("Night"));
      if (b < 0L) {
         b = System.currentTimeMillis();
      }
   }

   protected void init() {
      if (b < 0L) {
         b = System.currentTimeMillis();
      }

      this.a.clear();
      if (Night.fontManager != null) {
         Night.fontManager.getStringWidth("Singleplayer Multiplayer Account Manager Options Quit Night v26");
         Night.fontManager.getFontHeight();
      }

      int var1 = this.width / 2 - 230 / 2;
      int var2 = this.height / 2 - 18;
      this.a.add(new NightMainMenuScreen.MenuButton("Singleplayer", var1, var2, 230, 22, () -> this.minecraft.gui.setScreen(new SelectWorldScreen(this))));
      var2 += 22 + 6;
      this.a.add(new NightMainMenuScreen.MenuButton("Multiplayer", var1, var2, 230, 22, () -> this.minecraft.gui.setScreen(new JoinMultiplayerScreen(this))));
      var2 += 22 + 6;
      this.a
         .add(new NightMainMenuScreen.MenuButton("Account Manager", var1, var2, 230, 22, () -> this.minecraft.gui.setScreen(new AccountManagerScreen(this))));
      var2 += 22 + 6;
      int var3 = (230 - 6) / 2;
      this.a
         .add(
            new NightMainMenuScreen.MenuButton(
               "Options", var1, var2, var3, 22, () -> this.minecraft.gui.setScreen(new OptionsScreen(this, this.minecraft.options, false))
            )
         );
      this.a.add(new NightMainMenuScreen.MenuButton("Quit", var1 + var3 + 6, var2, 230 - var3 - 6, 22, () -> this.minecraft.stop()));
   }

   public void extractRenderState(GuiGraphicsExtractor var1, int var2, int var3, float var4) {
      super.extractRenderState(var1, var2, var3, var4);
      long var5 = System.currentTimeMillis() - b;
      float var7 = Math.min(1.0F, (float)var5 / 4000.0F);
      float var8 = 0.0F;
      if (var7 >= 1.0F) {
         long var9 = var5 - 4000L;
         float var11 = Math.min(1.0F, (float)var9 / 800.0F);
         var8 = 1.0F - (float)Math.pow(1.0F - var11, 3.0);
      }

      if (var8 > 0.001F) {
         this.a(var1, var8);
         Iterator var12 = this.a.iterator();

         while (var12.hasNext()) {
            ((NightMainMenuScreen.MenuButton)var12.next()).a(var1, var2, var3, var4, var8);
         }

         this.b(var1, var8);
      }
   }

   public void extractBackground(GuiGraphicsExtractor var1, int var2, int var3, float var4) {
      long var5 = System.currentTimeMillis() - b;
      float var7 = Math.min(1.0F, (float)var5 / 4000.0F);
      MenuBackgroundShader.a(var1, this.width, this.height, 1.0F, var7);
   }

   private void a(GuiGraphicsExtractor var1, float var2) {
      int var3 = Night.fontManager.getStringWidth("Night");
      int var4 = this.width / 2 - var3 / 2;
      int var5 = this.height / 2 - 70;
      Color var6 = a((int)(220.0F * var2));
      Render2DUtil.a(
         var1,
         this.width / 2.0F - var3 / 2.0F - 6.0F,
         var5 + Night.fontManager.getFontHeight() + 3,
         this.width / 2.0F + var3 / 2.0F + 6.0F,
         var5 + Night.fontManager.getFontHeight() + 4,
         var6
      );
      if (b()) {
         Night.fontManager.drawRainbowText(var1, "Night", var4, var5, 6L);
      } else {
         Night.fontManager.drawShadowedText(var1, "Night", var4, var5, new Color(255, 255, 255, (int)(255.0F * var2)));
      }

      int var7 = Night.fontManager.getStringWidth("v26 \u2022 MC 26.2");
      Night.fontManager
         .drawShadowedText(
            var1, "v26 \u2022 MC 26.2", this.width / 2 - var7 / 2, var5 + Night.fontManager.getFontHeight() + 8, new Color(170, 170, 180, (int)(180.0F * var2))
         );
   }

   private void b(GuiGraphicsExtractor var1, float var2) {
      String var3 = "Logged in as: \u00a7f" + (Night.accountManager != null ? Night.accountManager.getCurrentUsername() : "");
      Night.fontManager.drawShadowedText(var1, var3, 6, 6, new Color(200, 200, 200, (int)(200.0F * var2)));
      Night.fontManager
         .drawShadowedText(var1, "Night v26", 4, this.height - Night.fontManager.getFontHeight() - 4, new Color(160, 160, 160, (int)(160.0F * var2)));
      int var4 = Night.fontManager.getStringWidth("git-1c820e8a37");
      Night.fontManager
         .drawShadowedText(
            var1, "git-1c820e8a37", this.width - var4 - 4, this.height - Night.fontManager.getFontHeight() - 4, new Color(160, 160, 160, (int)(160.0F * var2))
         );
   }

   public boolean mouseClicked(MouseButtonEvent var1, boolean var2) {
      if (System.currentTimeMillis() - b < 4000L) {
         return false;
      }

      double var3 = var1.x();
      double var5 = var1.y();
      if (var1.button() == 0) {
         for (NightMainMenuScreen.MenuButton var8 : (Iterable<NightMainMenuScreen.MenuButton>)(Object)this.a) {
            if (var8.a(var3, var5)) {
               this.a();
               var8.f.run();
               return true;
            }
         }
      }

      return super.mouseClicked(var1, var2);
   }

   public boolean shouldCloseOnEsc() {
      return false;
   }

   private void a() {
      if (Night.moduleManager.getModule(ClickGuiModule.class).sounds.getValue()) {
         this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
      }
   }

   private static boolean b() {
      return Night.moduleManager.getModule(ClickGuiModule.class).v();
   }

   static Color a(int var0) {
      return ClickGuiScreen.a(0, var0);
   }

   static class MenuButton {
      private final String a;
      private final int b;
      private final int c;
      private final int d;
      private final int e;
      final Runnable f;
      private float g;

      MenuButton(String var1, int var2, int var3, int var4, int var5, Runnable var6) {
         this.a = var1;
         this.b = var2;
         this.c = var3;
         this.d = var4;
         this.e = var5;
         this.f = var6;
      }

      boolean a(double var1, double var3) {
         return var1 >= this.b && var1 <= this.b + this.d && var3 >= this.c && var3 <= this.c + this.e;
      }

      void a(GuiGraphicsExtractor var1, int var2, int var3, float var4, float var5) {
         float var6 = this.a(var2, var3) ? 1.0F : 0.0F;
         this.g = this.g + (var6 - this.g) * 0.2F;
         Color var7 = NightMainMenuScreen.a((int)(255.0F * var5));
         int var8 = (int)((this.g * 35.0F + 35.0F) * var5);
         Color var9 = new Color(8, 8, 12, var8);
         Render2DUtil.a(var1, this.b, this.c, this.b + this.d, this.c + this.e, var9);
         int var10 = (int)((this.g * 70.0F + 25.0F) * var5);
         Color var11 = this.g > 0.1F ? NightMainMenuScreen.a((int)(this.g * 180.0F * var5)) : new Color(255, 255, 255, var10);
         Render2DUtil.b(var1, this.b, this.c, this.b + this.d, this.c + this.e, var11);
         float var12 = 3.0F * this.g;
         if (var12 > 0.1F) {
            Render2DUtil.a(var1, this.b, this.c, this.b + var12, this.c + this.e, var7);
         }

         float var13 = this.d / 2.0F * this.g;
         if (var13 > 0.5F) {
            Render2DUtil.a(var1, this.b + this.d / 2.0F - var13, this.c + this.e - 1, this.b + this.d / 2.0F + var13, this.c + this.e, var7);
         }

         Color var14 = a(new Color(210, 210, 215), Color.WHITE, this.g);
         int var15 = this.c + this.e / 2 - Night.fontManager.getFontHeight() / 2;
         int var16 = Night.fontManager.getStringWidth(this.a);
         int var17 = this.b + this.d / 2 - var16 / 2;
         Night.fontManager.drawShadowedText(var1, this.a, var17, var15, new Color(var14.getRed(), var14.getGreen(), var14.getBlue(), (int)(255.0F * var5)));
      }

      private static Color a(Color var0, Color var1, float var2) {
         var2 = Math.max(0.0F, Math.min(1.0F, var2));
         return new Color(
            (int)(var0.getRed() + (var1.getRed() - var0.getRed()) * var2),
            (int)(var0.getGreen() + (var1.getGreen() - var0.getGreen()) * var2),
            (int)(var0.getBlue() + (var1.getBlue() - var0.getBlue()) * var2)
         );
      }
   }
}
