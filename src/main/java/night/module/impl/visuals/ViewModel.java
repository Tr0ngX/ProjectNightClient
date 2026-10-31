package night.module.impl.visuals;

import night.module.Module;
import night.module.ModuleInfo;
import night.setting.impl.BooleanSetting;
import night.setting.impl.CategorySetting;
import night.setting.impl.ModeSetting;
import night.setting.impl.NumberSetting;

@ModuleInfo(name = "ViewModel", description = "Modifies the position, scale and rotation of the player viewmodel.", category = Module.Category.VISUALS)
public class ViewModel extends Module {
   public final ModeSetting mode = new ModeSetting("Mode", "Hand modification mode.", "Each", new String[]{"Each", "Both"});
   public final BooleanSetting mainhandSwap = new BooleanSetting("MainhandSwap", "Disables the drop down animation when switching mainhand items.", true);
   public final BooleanSetting offhandSwap = new BooleanSetting("OffhandSwap", "Disables the drop down animation when switching offhand items.", true);
   public final CategorySetting mainHandCategory = new CategorySetting("MainHand", "Settings for main hand item rendering.");
   public final NumberSetting scaleMainX = new NumberSetting(
      "ScaleMainX", "Scale X", "Scale on X axis.", new CategorySetting.CategoryCondition(this.mainHandCategory), 1.0F, 0.1F, 5.0F
   );
   public final NumberSetting scaleMainY = new NumberSetting(
      "ScaleMainY", "Scale Y", "Scale on Y axis.", new CategorySetting.CategoryCondition(this.mainHandCategory), 1.0F, 0.1F, 5.0F
   );
   public final NumberSetting scaleMainZ = new NumberSetting(
      "ScaleMainZ", "Scale Z", "Scale on Z axis.", new CategorySetting.CategoryCondition(this.mainHandCategory), 1.0F, 0.1F, 5.0F
   );
   public final NumberSetting positionMainX = new NumberSetting(
      "PositionMainX", "Pos X", "Translation on X axis.", new CategorySetting.CategoryCondition(this.mainHandCategory), 0.0F, -3.0F, 3.0F
   );
   public final NumberSetting positionMainY = new NumberSetting(
      "PositionMainY", "Pos Y", "Translation on Y axis.", new CategorySetting.CategoryCondition(this.mainHandCategory), 0.0F, -3.0F, 3.0F
   );
   public final NumberSetting positionMainZ = new NumberSetting(
      "PositionMainZ", "Pos Z", "Translation on Z axis.", new CategorySetting.CategoryCondition(this.mainHandCategory), 0.0F, -3.0F, 3.0F
   );
   public final NumberSetting rotationMainX = new NumberSetting(
      "RotationMainX", "Rot X", "Rotation on X axis.", new CategorySetting.CategoryCondition(this.mainHandCategory), 0.0F, -180.0F, 180.0F
   );
   public final NumberSetting rotationMainY = new NumberSetting(
      "RotationMainY", "Rot Y", "Rotation on Y axis.", new CategorySetting.CategoryCondition(this.mainHandCategory), 0.0F, -180.0F, 180.0F
   );
   public final NumberSetting rotationMainZ = new NumberSetting(
      "RotationMainZ", "Rot Z", "Rotation on Z axis.", new CategorySetting.CategoryCondition(this.mainHandCategory), 0.0F, -180.0F, 180.0F
   );
   public final CategorySetting offHandCategory = new CategorySetting(
      "OffHand", "Settings for off hand item rendering.", new ModeSetting.ModeCondition(this.mode, "Each")
   );
   public final NumberSetting scaleOffX = new NumberSetting(
      "ScaleOffX", "Scale X", "Scale on X axis.", new CategorySetting.CategoryCondition(this.offHandCategory), 1.0F, 0.1F, 5.0F
   );
   public final NumberSetting scaleOffY = new NumberSetting(
      "ScaleOffY", "Scale Y", "Scale on Y axis.", new CategorySetting.CategoryCondition(this.offHandCategory), 1.0F, 0.1F, 5.0F
   );
   public final NumberSetting scaleOffZ = new NumberSetting(
      "ScaleOffZ", "Scale Z", "Scale on Z axis.", new CategorySetting.CategoryCondition(this.offHandCategory), 1.0F, 0.1F, 5.0F
   );
   public final NumberSetting positionOffX = new NumberSetting(
      "PositionOffX", "Pos X", "Translation on X axis.", new CategorySetting.CategoryCondition(this.offHandCategory), 0.0F, -3.0F, 3.0F
   );
   public final NumberSetting positionOffY = new NumberSetting(
      "PositionOffY", "Pos Y", "Translation on Y axis.", new CategorySetting.CategoryCondition(this.offHandCategory), 0.0F, -3.0F, 3.0F
   );
   public final NumberSetting x = new NumberSetting(
      "PositionOffZ", "Pos Z", "Translation on Z axis.", new CategorySetting.CategoryCondition(this.offHandCategory), 0.0F, -3.0F, 3.0F
   );
   public final NumberSetting y = new NumberSetting(
      "RotationOffX", "Rot X", "Rotation on X axis.", new CategorySetting.CategoryCondition(this.offHandCategory), 0.0F, -180.0F, 180.0F
   );
   public final NumberSetting z = new NumberSetting(
      "RotationOffY", "Rot Y", "Rotation on Y axis.", new CategorySetting.CategoryCondition(this.offHandCategory), 0.0F, -180.0F, 180.0F
   );
   public final NumberSetting rotationOffZ = new NumberSetting(
      "RotationOffZ", "Rot Z", "Rotation on Z axis.", new CategorySetting.CategoryCondition(this.offHandCategory), 0.0F, -180.0F, 180.0F
   );
   public final CategorySetting eatingCategory = new CategorySetting("Eating", "Settings for eating animation.");
   public final BooleanSetting eatAnimation = new BooleanSetting(
      "EatAnimation", "Modify eating animation position.", new CategorySetting.CategoryCondition(this.eatingCategory), false
   );
   public final NumberSetting eatX = new NumberSetting(
      "EatX", "Eat X", "Eating offset X.", new BooleanSetting.BooleanCondition(this.eatAnimation, true), 1.0F, -1.0F, 2.0F
   );
   public final NumberSetting eatY = new NumberSetting(
      "EatY", "Eat Y", "Eating offset Y.", new BooleanSetting.BooleanCondition(this.eatAnimation, true), 1.0F, -1.0F, 2.0F
   );
   public final NumberSetting eatingMultiplier = new NumberSetting(
      "EatingMultiplier",
      "Eating Multiplier",
      "Scales the eating bob: 1 = vanilla, 0 = no bobbing (only the bring-to-mouth motion stays).",
      new CategorySetting.CategoryCondition(this.eatingCategory),
      1.0F,
      0.0F,
      1.0F,
      0.1F
   );
}
