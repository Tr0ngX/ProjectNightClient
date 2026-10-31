package night.setting;

import lombok.Generated;

public class Setting {
   private final String name;
   private final String displayName;
   private final String description;
   private final Setting.VisibilityCondition visibility;

   @Generated
   public String getName() {
      return this.name;
   }

   @Generated
   public String getDisplayName() {
      return this.displayName;
   }

   @Generated
   public String getDescription() {
      return this.description;
   }

   @Generated
   public Setting.VisibilityCondition getVisibility() {
      return this.visibility;
   }

   @Generated
   public Setting(String var1, String var2, String var3, Setting.VisibilityCondition var4) {
      this.name = var1;
      this.displayName = var2;
      this.description = var3;
      this.visibility = var4;
   }

   public static class VisibilityCondition {
      private final Setting parentSetting;
      private boolean visible = true;

      public VisibilityCondition() {
         this.parentSetting = null;
      }

      public void update() {
      }

      @Generated
      public Setting getParentSetting() {
         return this.parentSetting;
      }

      @Generated
      public boolean isVisible() {
         return this.visible;
      }

      @Generated
      public void setVisible(boolean var1) {
         this.visible = var1;
      }

      @Generated
      public VisibilityCondition(Setting var1) {
         this.parentSetting = var1;
      }
   }
}
