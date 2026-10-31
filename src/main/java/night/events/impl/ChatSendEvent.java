package night.events.impl;

import lombok.Generated;
import night.events.Event;

public class ChatSendEvent extends Event {
   private String message;

   @Generated
   public String getMessage() {
      return this.message;
   }

   @Generated
   public ChatSendEvent(String var1) {
      this.message = var1;
   }
}
