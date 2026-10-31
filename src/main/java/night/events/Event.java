package night.events;

import lombok.Generated;

public class Event {
   private boolean cancelled;

   public void cancel() {
      this.cancelled = true;
   }

   @Generated
   public boolean isCancelled() {
      return this.cancelled;
   }

   @Generated
   public void setCancelled(boolean var1) {
      this.cancelled = var1;
   }

   @Generated
   @Override
   public boolean equals(Object var1) {
      if (var1 == this) {
         return true;
      } else if (!(var1 instanceof Event var2)) {
         return false;
      } else {
         if (!var2.canEqual(this)) {
            return false;
         }

         boolean var3 = this.isCancelled();
         boolean var4 = var2.isCancelled();
         return var3 == var4;
      }
   }

   @Generated
   protected boolean canEqual(Object var1) {
      return var1 instanceof Event;
   }

   @Generated
   @Override
   public int hashCode() {
      byte var1 = 59;
      int var2 = 1 * 59;
      byte var3;
      if (this.isCancelled()) {
         var3 = 79;
      } else {
         var3 = 97;
      }

      return var2 + var3;
   }

   @Generated
   @Override
   public String toString() {
      return "Event(cancelled=" + this.isCancelled() + ")";
   }
}
