package night.gui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import lombok.Generated;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.util.Util.OS;
import night.Night;
import night.gui.component.CategoryFrame;
import night.gui.component.DescriptionPanel;
import night.gui.component.GuiComponent;
import night.gui.component.PingBypassFrame;
import night.gui.component.SearchBarComponent;
import night.gui.component.impl.KeybindComponent;
import night.module.Module;
import night.module.impl.core.ClickGuiModule;
import night.util.animation.Animation;
import night.util.animation.EasingUtil;
import night.util.color.ColorUtil;
import night.util.math.TimerUtil;
import night.util.render.ClickGuiBackgroundShader;
import night.util.render.Render2DUtil;
import night.util.world.PacketAndRenderUtil;

public class ClickGuiScreen extends Screen {
   private final ArrayList<CategoryFrame> a = new ArrayList<>();
   private final ArrayList<GuiComponent> b = new ArrayList<>();
   private final DescriptionPanel c;
   private final SearchBarComponent d;
   private final PingBypassFrame e;
   private final TimerUtil f = new TimerUtil();
   private boolean g = false;
   private Color h = null;
   private float i = 1.0F;
   private final Animation j = new Animation(320, EasingUtil.Easing.EASE_OUT_CUBIC);
   private boolean k = false;
   private boolean l = false;

   public boolean a() {
      return this.l;
   }

   public void b() {
      this.k = true;
      this.l = false;
      this.j.setEasing(EasingUtil.Easing.EASE_IN_CUBIC);
   }

   public void c() {
      this.k = false;
      this.l = false;
      this.j.setEasing(EasingUtil.Easing.EASE_OUT_CUBIC);
   }

   public ClickGuiScreen() {
      super(Component.literal("night-click-gui"));
      byte var1 = 6;

      for (Module.Category var5 : Module.Category.values()) {
         this.a.add(new CategoryFrame(var5, var1, 3, 101, 13));
         var1 += 105;
      }

      this.e = new PingBypassFrame(var1, 3, 101, 13);
      this.c = new DescriptionPanel(var1, 3, 200, 13);
      this.d = new SearchBarComponent();
   }

   private float h() {
      return Minecraft.getInstance().getWindow() != null ? Minecraft.getInstance().getWindow().getGuiScaledHeight() + 30.0F : 300.0F;
   }

   public void extractRenderState(GuiGraphicsExtractor var1, int var2, int var3, float var4) {
      super.extractRenderState(var1, var2, var3, var4);
      if (this.f.passedMs(400L)) {
         this.g = !this.g;
         this.f.reset();
      }

      float var5 = this.j.animateTo(this.k ? 0.0F : 1.0F);
      float var6 = this.a.isEmpty() ? 0.0F : this.a_float_int(this.a.size() - 1);
      if (this.k && var5 <= 0.005F && var6 <= 0.005F) {
         this.l = true;
         Minecraft.getInstance().gui.setScreen(null);
      } else {
         this.c.a("");
         String var7 = this.d.b();
         float var8 = this.h();
         int var9 = Math.round(var2 / this.i);
         int var10 = Math.round(var3 / this.i);
         var1.pose().pushMatrix();
         var1.pose().scale(this.i, this.i);

         for (int var11 = 0; var11 < this.a.size(); var11++) {
            CategoryFrame var12 = this.a.get(var11);
            float var13 = this.a_float_int(var11);
            float var14 = Math.max(var12.getY() + var12.getContentHeight() + 30.0F, var8);
            float var15 = Mth.lerp(var13, -var14, 0.0F);
            var1.pose().pushMatrix();
            var1.pose().translate(0.0F, var15);
            var12.render(var1, var9, var10, var4, var7);
            var1.pose().popMatrix();
         }

         float var16 = var8;
         float var17 = Mth.lerp(var5, -var16, 0.0F);
         var1.pose().pushMatrix();
         var1.pose().translate(0.0F, var17);
         this.c.a(var1, var9, var10, var4);
         this.d.a(var1, var9, var10, var4);
         var1.pose().popMatrix();
         var1.pose().popMatrix();
      }
   }

   private float a_float_int(int var1) {
      float var2 = EasingUtil.a(
         Mth.clamp((float)(System.currentTimeMillis() - this.j.getLastUpdateTime() - var1 * 35L) / 320.0F, 0.0F, 1.0F),
         this.k ? EasingUtil.Easing.EASE_IN_CUBIC : EasingUtil.Easing.EASE_OUT_CUBIC
      );
      return this.k ? 1.0F - var2 : var2;
   }

   public boolean mouseDragged(MouseButtonEvent var1, double var2, double var4) {
      double var6 = var1.x() / this.i;
      double var8 = var1.y() / this.i;
      int var10 = var1.button();
      Iterator var11 = this.a.iterator();

      while (var11.hasNext()) {
         ((CategoryFrame)var11.next()).mouseDragged(var6, var8, var10, var2 / this.i, var4 / this.i);
      }

      return super.mouseDragged(var1, var2, var4);
   }

   public boolean mouseClicked(MouseButtonEvent var1, boolean var2) {
      double var3 = var1.x() / this.i;
      double var5 = var1.y() / this.i;
      int var7 = var1.button();
      if (this.d.a(var3, var5, var7)) {
         return true;
      }

      Iterator var8 = this.a.iterator();

      while (var8.hasNext()) {
         ((CategoryFrame)var8.next()).mouseClicked(var3, var5, var7);
      }

      this.c.a(var3, var5, var7);
      return super.mouseClicked(var1, var2);
   }

   public boolean mouseReleased(MouseButtonEvent var1) {
      double var2 = var1.x() / this.i;
      double var4 = var1.y() / this.i;
      int var6 = var1.button();
      Iterator var7 = this.a.iterator();

      while (var7.hasNext()) {
         ((CategoryFrame)var7.next()).mouseReleased(var2, var4, var6);
      }

      this.c.b(var2, var4, var6);
      return super.mouseReleased(var1);
   }

   public boolean mouseScrolled(double var1, double var3, double var5, double var7) {
      double var9 = var1 / this.i;
      double var11 = var3 / this.i;
      Iterator var13 = this.a.iterator();

      while (var13.hasNext()) {
         ((CategoryFrame)var13.next()).mouseScrolled(var9, var11, var5, var7);
      }

      return this.getChildAt(var1, var3).filter(var8 -> var8.mouseScrolled(var1, var3, var5, var7)).isPresent();
   }

   public boolean keyPressed(KeyEvent var1) {
      int var2 = var1.key();
      int var3 = var1.scancode();
      int var4 = var1.modifiers();
      if (var2 == 256 && !KeybindComponent.a()) {
         this.b();
         return true;
      }

      if (var2 == Night.moduleManager.getModule(ClickGuiModule.class).getKeybind()) {
         if (this.k) {
            this.c();
         }

         return true;
      } else {
         if (PacketAndRenderUtil.a_bool_int(Util.getPlatform() == OS.OSX ? 343 : 341) && var2 == 70) {
            this.d.a();
            return true;
         }

         if (this.d.a(var2, var3, var4)) {
            return true;
         }

         Iterator var5 = this.a.iterator();

         while (var5.hasNext()) {
            ((CategoryFrame)var5.next()).keyPressed(var2, var3, var4);
         }

         return var2 == 256 ? true : super.keyPressed(var1);
      }
   }

   public boolean charTyped(CharacterEvent var1) {
      char var2 = (char)var1.codepoint();
      if (this.d.a(var2, 0)) {
         return true;
      }

      Iterator var3 = this.a.iterator();

      while (var3.hasNext()) {
         ((CategoryFrame)var3.next()).charTyped(var2, 0);
      }

      return super.charTyped(var1);
   }

   public void extractBackground(GuiGraphicsExtractor var1, int var2, int var3, float var4) {
      ClickGuiModule var5 = Night.moduleManager.getModule(ClickGuiModule.class);
      if (var5.blur.getValue()) {
         var1.blurBeforeThisStratum();
      }

      Render2DUtil.a(var1, 0.0F, 0.0F, this.width, this.height, new Color(8, 7, 14, 140));
      String var6 = var5.background.getValue();
      int var7 = Arrays.asList(ClickGuiModule.m).indexOf(var6);
      if (var7 >= 0 && var7 < ClickGuiModule.m.length - 1) {
         float var8 = var5.bgSpeed.getValue().floatValue();
         float var9 = var5.bgOpacity.getValue().floatValue() / 100.0F;
         Color var10 = var5.color.getColor();
         boolean var11 = var5.v();
         ClickGuiBackgroundShader.a(var1, this.width, this.height, var7, var8, var9, var10, var11);
      }
   }

   protected void init() {
      super.init();
      PacketAndRenderUtil.a_void_Object(this);
      this.autoArrangeCategories();
   }

   public void removed() {
      PacketAndRenderUtil.b_void_Object(this);
      this.d.a("");
      this.d.a(0);
      this.d.b(false);
      this.d.a(false);
      super.removed();
      Night.moduleManager.getModule(ClickGuiModule.class).setEnabled(false);
   }

   public boolean isPauseScreen() {
      return false;
   }

   public static Color a(int var0, int var1) {
      return ColorUtil.a(
         Night.moduleManager.getModule(ClickGuiModule.class).v()
            ? ColorUtil.a_Color_long(var0 * 2L)
            : Night.moduleManager.getModule(ClickGuiModule.class).color.getColor(),
         var1
      );
   }

   @Generated
   
   public void autoArrangeCategories() {
      int screenWidth = Minecraft.getInstance().getWindow() != null ? Minecraft.getInstance().getWindow().getGuiScaledWidth() : 800;
      int frameWidth = 101;
      int gap = 6;
      int startY = 20;
      int currentX = 10;
      int currentY = startY;

      for (CategoryFrame frame : this.a) {
         if (currentX + frameWidth > screenWidth - 10 && currentX > 10) {
            currentX = 10;
            currentY += 260;
         }
         frame.setX(currentX);
         frame.setY(currentY);
         frame.sortComponents();
         currentX += frameWidth + gap;
      }
   }

   public ArrayList<CategoryFrame> d() {
      return this.a;
   }

   @Generated
   public DescriptionPanel e() {
      return this.c;
   }

   @Generated
   public boolean f() {
      return this.g;
   }

   @Generated
   public Color g() {
      return this.h;
   }

   @Generated
   public void a_void_Color(Color var1) {
      this.h = var1;
   }

   @Generated
   public void a_void_float(float var1) {
      this.i = var1;
   }
}
