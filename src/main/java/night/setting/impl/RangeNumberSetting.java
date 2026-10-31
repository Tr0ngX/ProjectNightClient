package night.setting.impl;

import night.Night;
import night.events.impl.SettingChangeEvent;

public class RangeNumberSetting extends NumberSetting {
   private int secondValue;
   private final int defaultSecondValue;

   public RangeNumberSetting(String var1, String var2, int var3, int var4, int var5, int var6) {
      Integer var7 = var3;
      Integer var8 = var5;
      Integer var9 = var6;
      super(var1, var2, var7, var8, var9);
      this.secondValue = var4;
      this.defaultSecondValue = var4;
   }

   public int getFirstValue() {
      return this.getValue().intValue();
   }

   public int getSecondValue() {
      return this.secondValue;
   }

   @Override
   public void setValue(Number var1) {
      int var2 = var1.intValue();
      int var3 = this.secondValue;
      Integer var4 = Math.min(var2, var3);
      super.setValue(var4);
   }

   public void setSecondValue(int var1) {
      long var2 = var1;
      int var4 = this.getFirstValue();
      int var5 = this.getMaxValue().intValue();
      int var6 = Math.clamp(var2, var4, var5);
      this.secondValue = var6;
      Night.EVENT_BUS.post(new SettingChangeEvent(this));
   }

   public void setRange(int var1, int var2) {
      int var3 = Math.min(var1, var2);
      long var4 = Math.max(var1, var2);
      int var6 = this.getMinValue().intValue();
      int var7 = this.getMaxValue().intValue();
      int var8 = Math.clamp(var4, var6, var7);
      this.secondValue = var8;
      Integer var9 = var3;
      this.setValue(var9);
   }

   @Override
   public void reset() {
      int var1 = this.defaultSecondValue;
      this.secondValue = var1;
      super.reset();
   }

   public String getRangeString() {
      int var1 = this.getFirstValue();
      int var2 = this.secondValue;
      return var1 + "-" + var2;
   }

   public void parseRange(String var1) {
      String[] var2 = var1.trim().split("-", 2);
      int var3 = Integer.parseInt(var2[0].trim());
      int var4;
      if (var2.length > 1) {
         var4 = Integer.parseInt(var2[1].trim());
      } else {
         var4 = var3;
      }

      this.setRange(var3, var4);
   }
}
