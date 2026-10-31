package night.events.impl;

import lombok.Generated;
import night.events.Event;

public class WindowResizeEvent extends Event {
   private final int width;
   private final int height;
   private final int framebufferWidth;
   private final int framebufferHeight;

   @Generated
   public WindowResizeEvent(int var1, int var2, int var3, int var4) {
      this.width = var1;
      this.height = var2;
      this.framebufferWidth = var3;
      this.framebufferHeight = var4;
   }
}
