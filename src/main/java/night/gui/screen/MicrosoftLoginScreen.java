package night.gui.screen;

import java.awt.Color;
import java.net.URI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import night.Night;
import night.accounts.Account;
import night.accounts.AccountType;
import night.accounts.MicrosoftAuth;
import night.util.render.Render2DUtil;
import night.util.world.PacketAndRenderUtil;

public class MicrosoftLoginScreen extends Screen {
   private final Screen a;
   private volatile MicrosoftAuth.BrowserLoginSession b;
   private volatile String c = "Opening browser for Microsoft login...";
   private volatile boolean d = false;

   public MicrosoftLoginScreen(Screen var1) {
      super(Component.literal("Microsoft Login"));
      this.a = var1;
   }

   protected void init() {
      int var1 = this.width / 2;
      int var2 = this.height / 2 - 160 / 2;
      if (this.b == null && !this.d) {
         this.a();
      }

      this.addRenderableWidget(Button.builder(Component.literal("Open Browser Again"), var1x -> {
         if (this.b != null && this.b.loginUrl() != null) {
            try {
               PacketAndRenderUtil.a_void_URI(URI.create(this.b.loginUrl()));
            } catch (Exception var3) {
               this.c = "\u00a7cFailed to open browser: " + var3.getMessage();
            }
         }
      }).bounds(var1 - 105, var2 + 85, 210, 20).build());
      this.addRenderableWidget(Button.builder(Component.literal("Cancel"), var1x -> this.onClose()).bounds(var1 - 105, var2 + 112, 210, 20).build());
   }

   private void a() {
      try {
         this.b = MicrosoftAuth.startBrowserLogin(var1 -> this.c = var1);
         this.b.future().thenAccept(var1 -> {
            if (var1.success()) {
               Account var2x = new Account(var1.username(), var1.uuid(), AccountType.MICROSOFT, var1.refreshToken(), var1.accessToken());
               Night.accountManager.addAccount(var2x);
               Night.accountManager.login(var2x);
               Minecraft.getInstance().execute(this::onClose);
            } else {
               this.c = "\u00a7c" + var1.errorMessage();
               this.d = true;
            }
         });
      } catch (Exception var2) {
         this.c = "\u00a7cFailed to start login: " + var2.getMessage();
         this.d = true;
      }
   }

   public void extractBackground(GuiGraphicsExtractor var1, int var2, int var3, float var4) {
      Render2DUtil.a(var1, 0.0F, 0.0F, this.width, this.height, new Color(18, 14, 30), new Color(8, 6, 14));
   }

   public void extractRenderState(GuiGraphicsExtractor var1, int var2, int var3, float var4) {
      int var5 = this.width / 2;
      int var6 = this.height / 2;
      int var7 = var5 - 290 / 2;
      int var8 = var6 - 160 / 2;
      Render2DUtil.a(var1, var7, var8, var7 + 290, var8 + 160, new Color(28, 28, 28, 245));
      Render2DUtil.b(var1, var7, var8, var7 + 290, var8 + 160, new Color(70, 70, 70, 200));
      Night.fontManager
         .drawShadowedText(var1, "Microsoft Account Login", var5 - Night.fontManager.getStringWidth("Microsoft Account Login") / 2, var8 + 12, Color.WHITE);
      Night.fontManager
         .drawShadowedText(
            var1,
            "A browser tab has been opened.",
            var5 - Night.fontManager.getStringWidth("A browser tab has been opened.") / 2,
            var8 + 32,
            new Color(180, 180, 180)
         );
      Night.fontManager
         .drawShadowedText(
            var1,
            "Log in to your account and approve to continue.",
            var5 - Night.fontManager.getStringWidth("Log in to your account and approve to continue.") / 2,
            var8 + 45,
            new Color(180, 180, 180)
         );
      Night.fontManager.drawShadowedText(var1, this.c, var5 - Night.fontManager.getStringWidth(this.c) / 2, var8 + 66, new Color(85, 255, 255));
      super.extractRenderState(var1, var2, var3, var4);
   }

   public void onClose() {
      if (this.b != null && this.b.cancelAction() != null) {
         this.b.cancelAction().run();
      }

      Minecraft.getInstance().gui.setScreen(this.a);
   }
}
