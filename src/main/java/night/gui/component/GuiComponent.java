package night.gui.component;

import lombok.Generated;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import night.Night;
import night.module.impl.core.ClickGuiModule;
import night.setting.Setting;
import night.util.MinecraftInstance;
import night.util.animation.Animation;
import night.util.animation.EasingUtil;

public class GuiComponent implements MinecraftInstance {
   private final Setting setting;
   private final CategoryFrame parentFrame;
   private int x;
   private int y;
   private int height;
   private int horizontalPadding = 2;
   private int textOffset = 5;
   private final String description;
   private boolean visible = true;
   private final Animation hoverAnimation = new Animation(0.0F, 0.0F, 120, EasingUtil.Easing.LINEAR);

   public GuiComponent(CategoryFrame var1, int var2, String var3) {
      this.setting = null;
      this.parentFrame = var1;
      this.height = var2;
      this.description = var3;
   }

   public GuiComponent(Setting var1, CategoryFrame var2, int var3, String var4) {
      this.setting = var1;
      this.parentFrame = var2;
      this.height = var3;
      this.description = var4;
   }

   public void render(GuiGraphicsExtractor var1, int var2, int var3, float var4) {
   }

   public void mouseClicked(double var1, double var3, int var5) {
   }

   public void mouseReleased(double var1, double var3, int var5) {
   }

   public void keyPressed(int var1, int var2, int var3) {
   }

   public void mouseScrolled(double var1, double var3, double var5, double var7) {
   }

   public void mouseDragged(double var1, double var3, int var5, double var6, double var8) {
   }

   public void charTyped(char var1, int var2) {
   }

   public int getWidth() {
      return this.parentFrame.getWidth();
   }

   public boolean isHovered(double var1, double var3) {
      return this.x + this.horizontalPadding <= var1
         && this.y <= var3
         && this.x + this.getWidth() - this.horizontalPadding > var1
         && this.y + this.getHeight() > var3;
   }

   protected void beginHoverTransform(GuiGraphicsExtractor var1, double var2, double var4) {
      float var6 = this.hoverAnimation.animateTo(this.isHovered(var2, var4) ? 1.0F : 0.0F) * 1.5F;
      var1.pose().pushMatrix();
      var1.pose().translate(0.0F, -var6);
   }

   protected void endHoverTransform(GuiGraphicsExtractor var1) {
      var1.pose().popMatrix();
   }

   public void playClickSound() {
      if (Night.moduleManager.getModule(ClickGuiModule.class).sounds.getValue()) {
         mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
      }
   }

   @Generated
   public Setting getSetting() {
      return this.setting;
   }

   @Generated
   public CategoryFrame getParentFrame() {
      return this.parentFrame;
   }

   @Generated
   public int getX() {
      return this.x;
   }

   @Generated
   public int getY() {
      return this.y;
   }

   @Generated
   public int getHeight() {
      return this.height;
   }

   @Generated
   public int getHorizontalPadding() {
      return this.horizontalPadding;
   }

   @Generated
   public int getTextOffset() {
      return this.textOffset;
   }

   @Generated
   public String getDescription() {
      return this.description;
   }

   @Generated
   public boolean isVisible() {
      return this.visible;
   }

   @Generated
   public void setX(int var1) {
      this.x = var1;
   }

   @Generated
   public void setY(int var1) {
      this.y = var1;
   }

   @Generated
   public void setVisible(boolean var1) {
      this.visible = var1;
   }
}
