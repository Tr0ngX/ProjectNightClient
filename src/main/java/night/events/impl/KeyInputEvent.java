package night.events.impl;

import lombok.Generated;
import night.events.Event;

public class KeyInputEvent extends Event {
   private final int key;
   private final int modifiers;

   @Generated
   @Override
   public boolean equals(Object var1) {
      if (var1 == this) {
         return true;
      } else if (!(var1 instanceof KeyInputEvent var2)) {
         return false;
      } else {
         if (!var2.canEqual(this)) {
            return false;
         }

         if (!super.equals(var1)) {
            return false;
         }

         int var3 = this.getKey();
         int var4 = var2.getKey();
         if (var3 != var4) {
            return false;
         }

         int var5 = this.getModifiers();
         int var6 = var2.getModifiers();
         return var5 == var6;
      }
   }

   @Generated
   @Override
   protected boolean canEqual(Object var1) {
      return var1 instanceof KeyInputEvent;
   }

   @Generated
   @Override
   public int hashCode() {
      byte var1 = 59;
      int var2 = super.hashCode() * 59;
      int var3 = this.getKey();
      int var4 = (var2 + var3) * 59;
      int var5 = this.getModifiers();
      return var4 + var5;
   }

   @Generated
   public KeyInputEvent(int var1, int var2) {
      this.key = var1;
      this.modifiers = var2;
   }

   @Generated
   public int getKey() {
      return this.key;
   }

   @Generated
   public int getModifiers() {
      return this.modifiers;
   }

   @Generated
   @Override
   public String toString() {
      int var1 = this.getKey();
      int var2 = this.getModifiers();
      return "KeyInputEvent(key=" + var1 + ", modifiers=" + var2 + ")";
   }
}
