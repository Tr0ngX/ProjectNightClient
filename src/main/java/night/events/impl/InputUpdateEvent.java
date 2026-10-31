package night.events.impl;

import night.events.Event;

public class InputUpdateEvent extends Event {
   public boolean up;
   public boolean down;
   public boolean left;
   public boolean right;
   public boolean jumping;
   public boolean sneaking;
   public boolean sprinting;

   public InputUpdateEvent(boolean var1, boolean var2, boolean var3, boolean var4, boolean var5, boolean var6, boolean var7) {
      this.up = var1;
      this.down = var2;
      this.left = var3;
      this.right = var4;
      this.jumping = var5;
      this.sneaking = var6;
      this.sprinting = var7;
   }
}
