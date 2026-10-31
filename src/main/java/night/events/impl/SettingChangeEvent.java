package night.events.impl;

import lombok.Generated;
import night.events.Event;
import night.setting.Setting;

public class SettingChangeEvent extends Event {
   private final Setting setting;

   @Generated
   public Setting getSetting() {
      return this.setting;
   }

   @Generated
   public SettingChangeEvent(Setting var1) {
      this.setting = var1;
   }
}
