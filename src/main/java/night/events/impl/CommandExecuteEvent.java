package night.events.impl;

import lombok.Generated;
import night.events.Event;

public class CommandExecuteEvent extends Event {
   private final String command;

   @Generated
   public String getCommand() {
      return this.command;
   }

   @Generated
   public CommandExecuteEvent(String var1) {
      this.command = var1;
   }
}
