package night.util.color;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import night.Night;
import night.mixins.accessors.StyleAccessor;
import night.mixins.accessors.TextColorAccessor;

public class TextFormattingUtil {
   public static String[] a = new String[]{
      "White",
      "Black",
      "Blue",
      "Dark Blue",
      "Green",
      "Dark Green",
      "Aqua",
      "Dark Aqua",
      "Red",
      "Dark Red",
      "Light Purple",
      "Dark Purple",
      "Yellow",
      "Gold",
      "Gray",
      "Dark Gray",
      "Client",
      "Rainbow"
   };

   public static Style a_Style_Style_SpecialChatFormatting(Style var0, SpecialChatFormatting var1) {
      TextColor var2 = var0.getColor();
      if (var1 == SpecialChatFormatting.CLIENT) {
         var2 = TextColorAccessor.create(ColorUtil.d().getRGB(), "CLIENT");
      } else if (var1 == SpecialChatFormatting.RAINBOW) {
         var2 = TextColorAccessor.create(ColorUtil.d().getRGB(), "RAINBOW");
      }

      return StyleAccessor.create(
         var2, null, false, false, false, false, false, var0.getClickEvent(), var0.getHoverEvent(), var0.getInsertion(), var0.getFont()
      );
   }

   public static Object a_Object_String(String var0) {
      return switch (var0.toLowerCase()) {
         case "black" -> ChatFormatting.BLACK;
         case "blue" -> ChatFormatting.BLUE;
         case "dark blue" -> ChatFormatting.DARK_BLUE;
         case "green" -> ChatFormatting.GREEN;
         case "dark green" -> ChatFormatting.DARK_GREEN;
         case "aqua" -> ChatFormatting.AQUA;
         case "dark aqua" -> ChatFormatting.DARK_AQUA;
         case "red" -> ChatFormatting.RED;
         case "dark red" -> ChatFormatting.DARK_RED;
         case "light purple" -> ChatFormatting.LIGHT_PURPLE;
         case "dark purple" -> ChatFormatting.DARK_PURPLE;
         case "yellow" -> ChatFormatting.YELLOW;
         case "gold" -> ChatFormatting.GOLD;
         case "gray" -> ChatFormatting.GRAY;
         case "dark gray" -> ChatFormatting.DARK_GRAY;
         case "client" -> SpecialChatFormatting.CLIENT;
         case "rainbow" -> SpecialChatFormatting.RAINBOW;
         default -> ChatFormatting.WHITE;
      };
   }

   public static List<String> a_List_String_int(String var0, int var1) {
      ArrayList var2 = new ArrayList();
      String[] var3 = var0.split(" ");
      String var4 = "";

      for (String var8 : var3) {
         if (Night.fontManager.getStringWidth(var4) + Night.fontManager.getStringWidth(var8) <= var1) {
            var4 = var4 + var8 + " ";
         } else {
            var2.add(var4);
            var4 = var8 + " ";
         }
      }

      if (Night.fontManager.getStringWidth(var4) > 0) {
         var2.add(var4);
      }

      return var2;
   }

   public static String[] a_StringArr_long(long var0) {
      String var2 = String.format("%02d", var0 / 3600L);
      String var3 = String.format("%02d", var0 % 3600L / 60L);
      String var4 = String.format("%02d", var0 % 60L);
      return new String[]{var2, var3, var4};
   }
}
