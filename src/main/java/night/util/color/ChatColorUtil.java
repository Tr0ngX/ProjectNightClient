package night.util.color;

import night.Night;
import night.module.impl.core.CommandsModule;

public class ChatColorUtil {
   public static Object a() {
      return TextFormattingUtil.a_Object_String(Night.moduleManager.getModule(CommandsModule.class).primaryMessageColor.getValue());
   }

   public static Object b() {
      return TextFormattingUtil.a_Object_String(Night.moduleManager.getModule(CommandsModule.class).secondaryMessageColor.getValue());
   }
}
