package night.command.impl;

import java.io.File;
import night.Night;
import night.command.Command;
import night.command.CommandInfo;
import night.util.world.PacketAndRenderUtil;

@CommandInfo(name = "folder", description = "Opens the clients folder.")
public class FolderCommand extends Command {
   @Override
   public void execute(String[] var1) {
      File var2 = new File("Night");
      if (var2.exists()) {
         PacketAndRenderUtil.a_void_File(var2);
      } else {
         Night.chatManager.sendInfo("Could not find the client's configuration folder.");
      }
   }
}
