package night.gui.component.impl;

import java.awt.Color;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import night.Night;
import night.gui.ClickGuiScreen;
import night.gui.component.CategoryFrame;
import night.gui.component.GuiComponent;
import night.module.impl.core.PingBypassModule;
import night.pingbypass.protocol.PingBypassPayload;
import night.pingbypass.protocol.c2s.C2SModuleTogglePacket;
import night.util.render.Render2DUtil;

public class ProxyModuleComponent extends GuiComponent {
   private final String a;

   public ProxyModuleComponent(String var1, CategoryFrame var2, int var3) {
      super(var2, var3, "PingBypass proxy module: " + var1);
      this.a = var1;
   }

   public String a() {
      return this.a;
   }

   private boolean b() {
      PingBypassModule var1 = Night.moduleManager.getModule(PingBypassModule.class);
      return var1 == null ? false : var1.b_bool_String(this.a);
   }

   @Override
   public void render(GuiGraphicsExtractor var1, int var2, int var3, float var4) {
      if (this.isHovered(var2, var3) && Night.clickGui.e().a().isEmpty()) {
         Night.clickGui.e().a(this.getDescription());
      }

      Color var5 = this.isHovered(var2, var3) ? new Color(255, 255, 255, 15) : new Color(0, 0, 0, 0);
      if (var5.getAlpha() > 0) {
         Render2DUtil.a(
            var1,
            this.getX() + this.getHorizontalPadding(),
            this.getY(),
            this.getX() + this.getWidth() - this.getHorizontalPadding(),
            this.getY() + this.getHeight() - 1,
            var5
         );
      }

      boolean var6 = this.b();
      if (var6) {
         Color var7 = ClickGuiScreen.a(this.getY(), 255);
         Render2DUtil.a(
            var1,
            this.getX() + this.getHorizontalPadding(),
            this.getY() + 1,
            this.getX() + this.getHorizontalPadding() + 2,
            this.getY() + this.getHeight() - 2,
            var7
         );
      }

      String var10 = this.a;
      int var8 = this.getX() + this.getTextOffset() + (var6 ? 1 : 0);
      int var9 = this.getY() + 2;
      this.beginHoverTransform(var1, var2, var3);
      Night.fontManager.drawShadowedText(var1, (var6 ? "" : ChatFormatting.GRAY) + var10, var8, var9, Color.WHITE);
      this.endHoverTransform(var1);
   }

   @Override
   public void mouseClicked(double var1, double var3, int var5) {
      if (this.isHovered(var1, var3) && var5 == 0) {
         boolean var6 = !this.b();
         this.b(var6);
         this.playClickSound();
      }
   }

   private void b(boolean var1) {
      if (mc.getConnection() != null) {
         C2SModuleTogglePacket var2 = new C2SModuleTogglePacket(this.a, var1);
         mc.getConnection().getConnection().send(new ServerboundCustomPayloadPacket(PingBypassPayload.fromPacket(var2)));
      }
   }
}
