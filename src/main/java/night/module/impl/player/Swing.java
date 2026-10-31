package night.module.impl.player;

import night.module.Module;
import night.module.ModuleInfo;
import night.setting.impl.BooleanSetting;
import night.setting.impl.CategorySetting;
import night.setting.impl.ModeSetting;
import night.setting.impl.NumberSetting;

@ModuleInfo(
   name = "Swing",
   description = "Allows you to modify the swing animation and the hand that will be used for swinging.",
   category = Module.Category.PLAYER
)
public class Swing extends Module {
   public ModeSetting hand;
   public BooleanSetting noPacket;
   public CategorySetting speedCategory;
   public BooleanSetting modifySpeed;
   public NumberSetting speed;
   public CategorySetting translationCategory;
   public BooleanSetting translateX;
   public BooleanSetting translateY;
   public BooleanSetting translateZ;
   public CategorySetting rotationCategory;
   public BooleanSetting rotationX;
   public BooleanSetting rotationY;
   public BooleanSetting rotationZ;

   public Swing() {
      String[] var1 = new String[]{"Default", "None", "Packet", "Mainhand", "Offhand", "Both"};
      this.hand = new ModeSetting("Hand", "The hand that will be used for swinging.", "Default", var1);
      ModeSetting var3 = this.hand;
      String[] var4 = new String[]{"Default", "Mainhand", "Offhand", "Both"};
      ModeSetting.ModeCondition var2 = new ModeSetting.ModeCondition(var3, var4);
      this.noPacket = new BooleanSetting("NoPacket", "Only swings clientside, meaning nobody will be able to tell if you're swinging or not.", var2, false);
      this.speedCategory = new CategorySetting("Speed", "The category containing all settings related to swing speed.");
      CategorySetting var5 = this.speedCategory;
      this.modifySpeed = new BooleanSetting(
         "ModifySpeed", "Enabled", "Modifies the speed of your swing animation.", new CategorySetting.CategoryCondition(var5), false
      );
      BooleanSetting var7 = this.modifySpeed;
      BooleanSetting.BooleanCondition var6 = new BooleanSetting.BooleanCondition(var7, true);
      Integer var8 = 15;
      Integer var9 = 1;
      Integer var10 = 20;
      this.speed = new NumberSetting("Speed", "Amount", "The speed at which your hand will be swinging.", var6, var8, var9, var10);
      this.translationCategory = new CategorySetting("Translation", "The category related to the swing translations.");
      CategorySetting var11 = this.translationCategory;
      this.translateX = new BooleanSetting(
         "TranslateX", "X", "Enables the swing translation on the X axis.", new CategorySetting.CategoryCondition(var11), true
      );
      CategorySetting var12 = this.translationCategory;
      this.translateY = new BooleanSetting(
         "TranslateY", "Y", "Enables the swing translation on the Y axis.", new CategorySetting.CategoryCondition(var12), true
      );
      CategorySetting var13 = this.translationCategory;
      this.translateZ = new BooleanSetting(
         "TranslateZ", "Z", "Enables the swing translation on the Z axis.", new CategorySetting.CategoryCondition(var13), true
      );
      this.rotationCategory = new CategorySetting("Rotation", "The category related to the swing rotations.");
      CategorySetting var14 = this.rotationCategory;
      this.rotationX = new BooleanSetting("RotationX", "X", "Enables the swing rotation on the X axis.", new CategorySetting.CategoryCondition(var14), true);
      CategorySetting var15 = this.rotationCategory;
      this.rotationY = new BooleanSetting("RotationY", "Y", "Enables the swing rotation on the Y axis.", new CategorySetting.CategoryCondition(var15), true);
      CategorySetting var16 = this.rotationCategory;
      this.rotationZ = new BooleanSetting("RotationZ", "Z", "Enables the swing rotation on the Z axis.", new CategorySetting.CategoryCondition(var16), true);
   }
}
