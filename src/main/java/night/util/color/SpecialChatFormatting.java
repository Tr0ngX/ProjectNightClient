package night.util.color;

import net.minecraft.util.StringRepresentable;

public enum SpecialChatFormatting implements StringRepresentable {
   CLIENT('z'),
   RAINBOW('y');

   private final char c;

   SpecialChatFormatting(char var3) {
      this.c = var3;
   }

   public static SpecialChatFormatting a(char var0) {
      char var1 = Character.toLowerCase(var0);

      for (SpecialChatFormatting var5 : values()) {
         if (var5.c == var1) {
            return var5;
         }
      }

      return null;
   }

   @Override
   public String toString() {
      return "\u00a7" + this.c;
   }

   public String getSerializedName() {
      return null;
   }
}
