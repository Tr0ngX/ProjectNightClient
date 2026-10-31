package night.gui.screen;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import night.Night;
import night.accounts.Account;
import night.module.impl.core.ClickGuiModule;
import night.util.render.Render2DUtil;

public class AccountManagerScreen extends Screen {
   private final Screen a;
   private Account b = null;
   private float c = 0.0F;
   private float d = 0.0F;
   private String e = "";
   private long f = 0L;
   private long g = 0L;
   private Account h = null;
   private final Map<Account, Float> i = new HashMap<>();
   private Button j;
   private Button k;

   public AccountManagerScreen(Screen var1) {
      super(Component.literal("Account Manager"));
      this.a = var1;
   }

   protected void init() {
      int var1 = this.width / 2;
      int var2 = this.height - 55;
      this.j = Button.builder(Component.literal("Login"), var1x -> this.a()).bounds(var1 - 160, var2, 102, 20).build();
      this.addRenderableWidget(this.j);
      this.addRenderableWidget(
         Button.builder(Component.literal("Add Microsoft"), var1x -> Minecraft.getInstance().gui.setScreen(new MicrosoftLoginScreen(this)))
            .bounds(var1 - 52, var2, 104, 20)
            .build()
      );
      this.addRenderableWidget(
         Button.builder(Component.literal("Add Offline"), var1x -> Minecraft.getInstance().gui.setScreen(new AddOfflineAccountScreen(this)))
            .bounds(var1 + 58, var2, 102, 20)
            .build()
      );
      this.addRenderableWidget(Button.builder(Component.literal("Random Offline"), var1x -> {
         String[] var2x = new String[]{"Shadow", "Night", "Ghost", "Viper", "Phantom", "Raven", "Frost", "Nova", "Dark", "Storm"};
         String var3 = var2x[new Random().nextInt(var2x.length)] + "_" + (100 + new Random().nextInt(900));
         Account var4 = Night.accountManager.loginOffline(var3);
         this.b = var4;
         this.a_void_String("Logged in as " + var3);
      }).bounds(var1 - 160, var2 + 25, 102, 20).build());
      this.k = Button.builder(Component.literal("Delete"), var1x -> {
         if (this.b != null) {
            Night.accountManager.removeAccount(this.b);
            this.b = null;
            this.a_void_String("Account deleted");
         }
      }).bounds(var1 - 52, var2 + 25, 104, 20).build();
      this.addRenderableWidget(this.k);
      this.addRenderableWidget(Button.builder(Component.literal("Back"), var1x -> this.onClose()).bounds(var1 + 58, var2 + 25, 102, 20).build());
      this.b();
   }

   private void a() {
      if (this.b != null) {
         this.a_void_String("Logging in to " + this.b.getName() + "...");
         CompletableFuture.runAsync(() -> {
            if (Night.accountManager.login(this.b)) {
               this.a_void_String("Logged in as " + this.b.getName());
            } else {
               this.a_void_String("\u00a7cFailed to login to " + this.b.getName());
            }
         });
      }
   }

   private void b() {
      if (this.j != null) {
         this.j.active = this.b != null;
      }

      if (this.k != null) {
         this.k.active = this.b != null;
      }
   }

   private void a_void_String(String var1) {
      this.e = var1;
      this.f = System.currentTimeMillis();
   }

   public void extractBackground(GuiGraphicsExtractor var1, int var2, int var3, float var4) {
      Render2DUtil.a(var1, 0.0F, 0.0F, this.width, this.height, new Color(24, 24, 24), new Color(12, 12, 12));
   }

   public void extractRenderState(GuiGraphicsExtractor var1, int var2, int var3, float var4) {
      int var5 = this.width / 2;
      Night.fontManager.drawShadowedText(var1, "Account Manager", var5 - Night.fontManager.getStringWidth("Account Manager") / 2, 12, Color.WHITE);
      String var6 = Night.accountManager.getCurrentUsername();
      if (System.currentTimeMillis() - this.f < 5000L && !this.e.isEmpty()) {
         Color var25 = this.e.startsWith("\u00a7c")
            ? new Color(255, 100, 100)
            : (this.e.startsWith("Logging") ? new Color(200, 200, 200) : new Color(255, 255, 100));
         Night.fontManager.drawShadowedText(var1, this.e, var5 - Night.fontManager.getStringWidth(this.e) / 2, 28, var25);
      } else {
         String var7 = "Logged in as: \u00a7f" + var6;
         Night.fontManager.drawShadowedText(var1, var7, var5 - Night.fontManager.getStringWidth(var7) / 2, 28, new Color(180, 180, 180));
      }

      int var26 = var5 - 160;
      int var8 = this.height - 120;
      Render2DUtil.a(var1, var26, 55, var26 + 320, 55 + var8, new Color(24, 24, 24, 230));
      Render2DUtil.b(var1, var26, 55, var26 + 320, 55 + var8, new Color(60, 60, 60, 160));
      this.c = this.c + (this.d - this.c) * 0.3F;
      List var9 = Night.accountManager.getAccounts();
      int var10 = Math.max(0, var9.size() * 32 - var8);
      this.d = Mth.clamp(this.d, -var10, 0.0F);
      var1.enableScissor(var26, 55, var26 + 320, 55 + var8);
      float var11 = 55 + this.c;

      for (Account var13 : (Iterable<Account>)(Object)var9) {
         if (var11 + 32 >= 55 && var11 <= 55 + var8) {
            boolean var14 = var13 == this.b;
            boolean var15 = var13.getName().equalsIgnoreCase(var6);
            float var16 = var2 >= var26 + 4 && var2 <= var26 + 320 - 4 && var3 >= var11 + 2.0F && var3 <= var11 + 32 - 2.0F && var3 >= 55 && var3 <= 55 + var8
               ? 1.0F
               : 0.0F;
            float var17 = this.i.getOrDefault(var13, 0.0F);
            var17 += (var16 - var17) * 0.22F;
            if (Math.abs(var16 - var17) < 0.005F) {
               var17 = var16;
            }

            this.i.put(var13, var17);
            Color var18 = var14 ? new Color(50, 50, 50, 220) : (var15 ? new Color(40, 40, 40, 180) : new Color(30, 30, 30, 150));
            Render2DUtil.a(var1, var26 + 4, var11 + 2.0F, var26 + 320 - 4, var11 + 32 - 2.0F, var18);
            Color var19 = this.c();
            if (var17 > 0.001F) {
               float var20 = (320 - 8) * var17;
               Color var21 = new Color(var19.getRed(), var19.getGreen(), var19.getBlue(), (int)(90.0F * var17));
               Color var22 = new Color(var19.getRed(), var19.getGreen(), var19.getBlue(), (int)(20.0F * var17));
               Render2DUtil.b(var1, var26 + 4, var11 + 2.0F, var26 + 4 + var20, var11 + 32 - 2.0F, var21, var22);
               Color var23 = new Color(var19.getRed(), var19.getGreen(), var19.getBlue(), (int)(200.0F * var17));
               Render2DUtil.a(var1, var26 + 3 + var20, var11 + 2.0F, var26 + 5 + var20, var11 + 32 - 2.0F, var23);
            }

            float var29 = var14 ? 1.0F : var17;
            if (var29 > 0.001F) {
               float var30 = var14 ? 3.5F : 3.0F * var17;
               Color var32 = new Color(var19.getRed(), var19.getGreen(), var19.getBlue(), (int)(255.0F * var29));
               Render2DUtil.a(var1, var26 + 4, var11 + 2.0F, var26 + 4 + var30, var11 + 32 - 2.0F, var32);
            }

            float var31 = 3.0F * var17;
            String var33 = (var15 ? "\u00a7a\u2713 " : "") + var13.getName();
            Night.fontManager.drawShadowedText(var1, var33, (int)(var26 + 14 + var31), (int)(var11 + 6.0F), var15 ? Color.GREEN : Color.WHITE);
            String var34 = "[" + var13.getType().getDisplayName() + "]";

            Color var24 = switch (var13.getType()) {
               case MICROSOFT -> new Color(0, 220, 255);
               case OFFLINE -> new Color(255, 200, 80);
               case SESSION -> new Color(220, 120, 255);
            };
            Night.fontManager.drawShadowedText(var1, var34, (int)(var26 + 14 + var31), (int)(var11 + 18.0F), var24);
            if (var15) {
               Night.fontManager
                  .drawShadowedText(
                     var1, "\u00a7aACTIVE", var26 + 320 - Night.fontManager.getStringWidth("\u00a7aACTIVE") - 12, (int)(var11 + 11.0F), Color.GREEN
                  );
            }
         }

         var11 += 32;
      }

      var1.disableScissor();
      if (var9.isEmpty()) {
         String var27 = "No saved accounts. Click 'Add Offline' or 'Add Microsoft' below.";
         Night.fontManager.drawShadowedText(var1, var27, var5 - Night.fontManager.getStringWidth(var27) / 2, 55 + var8 / 2 - 5, new Color(140, 140, 160));
      }

      this.b();
      super.extractRenderState(var1, var2, var3, var4);
   }

   public boolean mouseClicked(MouseButtonEvent var1, boolean var2) {
      double var3 = var1.x();
      double var5 = var1.y();
      int var7 = var1.button();
      int var8 = this.width / 2 - 160;
      int var9 = this.height - 120;
      if (var7 == 0 && var3 >= var8 && var3 <= var8 + 320 && var5 >= 55 && var5 <= 55 + var9) {
         List var10 = Night.accountManager.getAccounts();
         int var11 = (int)((float)(var5 - 55 - this.c) / 32);
         if (var11 >= 0 && var11 < var10.size()) {
            Account var12 = (Account)var10.get(var11);
            long var13 = System.currentTimeMillis();
            if (var12 != this.h || var13 - this.g >= 350L && !var2) {
               this.b = var12;
            } else {
               this.b = var12;
               this.a();
            }

            this.h = var12;
            this.g = var13;
            this.b();
            return true;
         }
      }

      return super.mouseClicked(var1, var2);
   }

   public boolean mouseScrolled(double var1, double var3, double var5, double var7) {
      List var9 = Night.accountManager.getAccounts();
      int var10 = this.height - 120;
      int var11 = Math.max(0, var9.size() * 32 - var10);
      this.d += (float)(var7 * 24.0);
      this.d = Mth.clamp(this.d, -var11, 0.0F);
      return true;
   }

   private Color c() {
      ClickGuiModule var1 = Night.moduleManager.getModule(ClickGuiModule.class);
      return var1 != null ? var1.color.getColor() : new Color(130, 90, 240);
   }

   public void onClose() {
      Minecraft.getInstance().gui.setScreen(this.a);
   }
}
