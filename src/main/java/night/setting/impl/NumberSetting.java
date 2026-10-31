package night.setting.impl;

import lombok.Generated;
import night.Night;
import night.events.impl.SettingChangeEvent;
import night.setting.Setting;

public class NumberSetting extends Setting {
   private Number value;
   private final Number defaultValue;
   private final Number minValue;
   private final Number maxValue;
   private final Number step;
   private final boolean allowZero;

   public NumberSetting(String var1, String var2, Number var3, Number var4, Number var5) {
      Setting.VisibilityCondition var6 = new Setting.VisibilityCondition();
      Integer var7 = 0;
      this(var1, var1, var2, var6, var3, var4, var5, var7);
   }

   public NumberSetting(String var1, String var2, String var3, Number var4, Number var5, Number var6) {
      Setting.VisibilityCondition var7 = new Setting.VisibilityCondition();
      Integer var8 = 0;
      this(var1, var2, var3, var7, var4, var5, var6, var8);
   }

   public NumberSetting(String var1, String var2, Setting.VisibilityCondition var3, Number var4, Number var5, Number var6) {
      Integer var7 = 0;
      this(var1, var1, var2, var3, var4, var5, var6, var7);
   }

   public NumberSetting(String var1, String var2, String var3, Setting.VisibilityCondition var4, Number var5, Number var6, Number var7) {
      Integer var8 = 0;
      this(var1, var2, var3, var4, var5, var6, var7, var8);
   }

   public NumberSetting(String var1, String var2, String var3, Setting.VisibilityCondition var4, Number var5, Number var6, Number var7, Number var8) {
      this(var1, var2, var3, var4, var5, var6, var7, var8, false);
   }

   public NumberSetting(
      String var1, String var2, String var3, Setting.VisibilityCondition var4, Number var5, Number var6, Number var7, Number var8, boolean var9
   ) {
      super(var1, var2, var3, var4);
      this.value = var5;
      this.defaultValue = var5;
      this.minValue = var6;
      this.maxValue = var7;
      this.step = var8;
      this.allowZero = var9;
   }

   public void setValue(Number var1) {
      switch (this.getNumberType()) {
         case LONG:
            long var20 = var1.longValue();
            long var22 = this.minValue.longValue();
            long var24 = this.maxValue.longValue();
            Long var26 = Math.clamp(var20, var22, var24);
            this.value = var26;
            break;
         case DOUBLE:
            double var2 = var1.doubleValue();
            double var4 = this.minValue.doubleValue();
            double var6 = this.maxValue.doubleValue();
            Double var8 = Math.clamp(var2, var4, var6);
            this.value = var8;
            break;
         case FLOAT:
            float var9 = var1.floatValue();
            float var10 = this.minValue.floatValue();
            float var11 = this.maxValue.floatValue();
            Float var12 = Math.clamp(var9, var10, var11);
            this.value = var12;
            break;
         default:
            int var13;
            if (this.getStep() == null) {
               var13 = 1;
            } else {
               var13 = this.getStep().intValue();
            }

            int var14 = var13;
            int var19;
            if (var14 > 1) {
               float var15 = var1.floatValue();
               float var16 = var14;
               int var17 = Math.round(var15 / var16);
               int var18 = var14;
               var19 = var17 * var18;
            } else {
               var19 = var1.intValue();
            }

            long var27 = var19;
            int var29 = this.minValue.intValue();
            int var30 = this.maxValue.intValue();
            Integer var31 = Math.clamp(var27, var29, var30);
            this.value = var31;
      }

      Night.EVENT_BUS.post(new SettingChangeEvent(this));
   }

   public void reset() {
      Number var1 = this.defaultValue;
      this.value = var1;
   }

   public NumberSetting.NumberType getNumberType() {
      if (this.defaultValue.getClass() == Long.class) {
         return NumberSetting.NumberType.LONG;
      } else if (this.defaultValue.getClass() == Double.class) {
         return NumberSetting.NumberType.DOUBLE;
      } else {
         return this.defaultValue.getClass() == Float.class ? NumberSetting.NumberType.FLOAT : NumberSetting.NumberType.INTEGER;
      }
   }

   @Generated
   public Number getValue() {
      return this.value;
   }

   @Generated
   public Number getDefaultValue() {
      return this.defaultValue;
   }

   @Generated
   public Number getMinValue() {
      return this.minValue;
   }

   @Generated
   public Number getMaxValue() {
      return this.maxValue;
   }

   @Generated
   public Number getStep() {
      return this.step;
   }

   @Generated
   public boolean isAllowZero() {
      return this.allowZero;
   }

   public static class NumberCondition extends Setting.VisibilityCondition {
      private final NumberSetting a;
      private final Number b;
      private final NumberSetting.NumberCondition.Comparison c;

      public NumberCondition(NumberSetting var1, Number var2, NumberSetting.NumberCondition.Comparison var3) {
         super(var1);
         this.a = var1;
         this.b = var2;
         this.c = var3;
      }

      @Override
      public void update() {
         if (this.a.getVisibility() != null) {
            this.a.getVisibility().update();
            if (!this.a.getVisibility().isVisible()) {
               this.setVisible(false);
               return;
            }
         }

         if (this.a.getNumberType() == NumberSetting.NumberType.INTEGER) {
            boolean var3;
            if (this.c == NumberSetting.NumberCondition.Comparison.EQUALS) {
               int var1 = this.a.getValue().intValue();
               int var2 = this.b.intValue();
               if (var1 == var2) {
                  var3 = true;
               } else {
                  var3 = false;
               }
            } else if (this.c == NumberSetting.NumberCondition.Comparison.SMALLER) {
               int var4 = this.a.getValue().intValue();
               int var5 = this.b.intValue();
               if (var4 < var5) {
                  var3 = true;
               } else {
                  var3 = false;
               }
            } else {
               int var6 = this.a.getValue().intValue();
               int var7 = this.b.intValue();
               if (var6 > var7) {
                  var3 = true;
               } else {
                  var3 = false;
               }
            }

            this.setVisible(var3);
         }

         if (this.a.getNumberType() == NumberSetting.NumberType.LONG) {
            boolean var12;
            if (this.c == NumberSetting.NumberCondition.Comparison.EQUALS) {
               long var8 = this.a.getValue().longValue();
               long var10 = this.b.longValue();
               if (var8 == var10) {
                  var12 = true;
               } else {
                  var12 = false;
               }
            } else if (this.c == NumberSetting.NumberCondition.Comparison.SMALLER) {
               long var13 = this.a.getValue().longValue();
               long var15 = this.b.longValue();
               if (var13 < var15) {
                  var12 = true;
               } else {
                  var12 = false;
               }
            } else {
               long var17 = this.a.getValue().longValue();
               long var19 = this.b.longValue();
               if (var17 > var19) {
                  var12 = true;
               } else {
                  var12 = false;
               }
            }

            this.setVisible(var12);
         }

         if (this.a.getNumberType() == NumberSetting.NumberType.DOUBLE) {
            boolean var25;
            if (this.c == NumberSetting.NumberCondition.Comparison.EQUALS) {
               double var21 = this.a.getValue().doubleValue();
               double var23 = this.b.doubleValue();
               if (var21 == var23) {
                  var25 = true;
               } else {
                  var25 = false;
               }
            } else if (this.c == NumberSetting.NumberCondition.Comparison.SMALLER) {
               double var26 = this.a.getValue().doubleValue();
               double var28 = this.b.doubleValue();
               if (var26 < var28) {
                  var25 = true;
               } else {
                  var25 = false;
               }
            } else {
               double var30 = this.a.getValue().doubleValue();
               double var32 = this.b.doubleValue();
               if (var30 > var32) {
                  var25 = true;
               } else {
                  var25 = false;
               }
            }

            this.setVisible(var25);
         }

         if (this.a.getNumberType() == NumberSetting.NumberType.FLOAT) {
            boolean var36;
            if (this.c == NumberSetting.NumberCondition.Comparison.EQUALS) {
               float var34 = this.a.getValue().floatValue();
               float var35 = this.b.floatValue();
               if (var34 == var35) {
                  var36 = true;
               } else {
                  var36 = false;
               }
            } else if (this.c == NumberSetting.NumberCondition.Comparison.SMALLER) {
               float var37 = this.a.getValue().floatValue();
               float var38 = this.b.floatValue();
               if (var37 < var38) {
                  var36 = true;
               } else {
                  var36 = false;
               }
            } else {
               float var39 = this.a.getValue().floatValue();
               float var40 = this.b.floatValue();
               if (var39 > var40) {
                  var36 = true;
               } else {
                  var36 = false;
               }
            }

            this.setVisible(var36);
         }
      }

      public enum Comparison {
         EQUALS,
         SMALLER,
         BIGGER;
      }
   }

   public enum NumberType {
      INTEGER,
      LONG,
      DOUBLE,
      FLOAT;
   }
}
