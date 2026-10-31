package night.gui.screen;

import java.awt.Color;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import night.Night;
import night.util.render.Render2DUtil;

public class AddOfflineAccountScreen extends Screen {
   private final Screen a;
   private EditBox b;
   private String c = "";

   public AddOfflineAccountScreen(Screen var1) {
      super(Component.literal("Add Offline Account"));
      this.a = var1;
   }

   protected void init() {
      int var1 = this.width / 2;
      int var2 = this.height / 2 - 175 / 2;
      this.b = new EditBox(this.font, var1 - 115, var2 + 42, 230, 20, Component.literal("Username"));
      this.b.setMaxLength(16);
      this.b.setHint(Component.literal("Enter username..."));
      this.addRenderableWidget(this.b);
      this.setInitialFocus(this.b);
      this.addRenderableWidget(Button.builder(Component.literal("Add & Login"), var1x -> {
         String var2x = this.b.getValue().trim();
         if (var2x.isEmpty()) {
            this.c = "Username cannot be empty!";
         } else {
            if (Night.accountManager.loginOffline(var2x) != null) {
               Minecraft.getInstance().gui.setScreen(this.a);
            }
         }
      }).bounds(var1 - 115, var2 + 68, 230, 20).build());
      this.addRenderableWidget(Button.builder(Component.literal("Random Name"), var1x -> {
         String[] var2x = new String[]{"Shadow", "Night", "Ghost", "Viper", "Phantom", "Raven", "Frost", "Nova", "Dark", "Storm"};
         String var3 = var2x[new Random().nextInt(var2x.length)] + "_" + (100 + new Random().nextInt(900));
         this.b.setValue(var3);
      }).bounds(var1 - 115, var2 + 94, 230, 20).build());
      this.addRenderableWidget(Button.builder(Component.literal("Cancel"), var1x -> this.onClose()).bounds(var1 - 115, var2 + 120, 230, 20).build());
   }

   public void extractBackground(GuiGraphicsExtractor var1, int var2, int var3, float var4) {
      Render2DUtil.a(var1, 0.0F, 0.0F, this.width, this.height, new Color(18, 14, 30), new Color(8, 6, 14));
   }

   public void extractRenderState(GuiGraphicsExtractor var1, int var2, int var3, float var4) {
      int var5 = this.width / 2;
      int var6 = this.height / 2;
      int var7 = var5 - 270 / 2;
      int var8 = var6 - 175 / 2;
      Render2DUtil.a(var1, var7, var8, var7 + 270, var8 + 175, new Color(28, 28, 28, 245));
      Render2DUtil.b(var1, var7, var8, var7 + 270, var8 + 175, new Color(70, 70, 70, 200));
      Night.fontManager
         .drawShadowedText(
            var1, "Add Offline / Cracked Account", var5 - Night.fontManager.getStringWidth("Add Offline / Cracked Account") / 2, var8 + 12, Color.WHITE
         );
      if (!this.c.isEmpty()) {
         Night.fontManager.drawShadowedText(var1, "\u00a7c" + this.c, var5 - Night.fontManager.getStringWidth(this.c) / 2, var8 + 28, Color.RED);
      }

      super.extractRenderState(var1, var2, var3, var4);
   }

   public void onClose() {
      Minecraft.getInstance().gui.setScreen(this.a);
   }
}
