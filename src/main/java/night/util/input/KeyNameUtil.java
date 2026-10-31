package night.util.input;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import night.util.world.PacketAndRenderUtil;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.glfw.GLFW;

public class KeyNameUtil {
   public static String a_String_int(int var0) {
      return switch (var0) {
         case -5 -> "Button 4";
         case -4 -> "Button 3";
         case -3 -> "Middle Click";
         case -2 -> "Right Click";
         case -1 -> "Unknown";
         case 0 -> "None";
         case 32 -> "Space";
         case 39 -> "Apostrophe";
         case 96 -> "Grave Accent";
         case 161 -> "World 1";
         case 162 -> "World 2";
         case 256 -> "Esc";
         case 257 -> "Enter";
         case 258 -> "Tab";
         case 259 -> "Backspace";
         case 260 -> "Insert";
         case 261 -> "Delete";
         case 262 -> "Arrow Right";
         case 263 -> "Arrow Left";
         case 264 -> "Arrow Down";
         case 265 -> "Arrow Up";
         case 266 -> "Page Up";
         case 267 -> "Page Down";
         case 268 -> "Home";
         case 269 -> "End";
         case 280 -> "Caps Lock";
         case 282 -> "Num Lock";
         case 283 -> "Print Screen";
         case 284 -> "Pause";
         case 290 -> "F1";
         case 291 -> "F2";
         case 292 -> "F3";
         case 293 -> "F4";
         case 294 -> "F5";
         case 295 -> "F6";
         case 296 -> "F7";
         case 297 -> "F8";
         case 298 -> "F9";
         case 299 -> "F10";
         case 300 -> "F11";
         case 301 -> "F12";
         case 302 -> "F13";
         case 303 -> "F14";
         case 304 -> "F15";
         case 305 -> "F16";
         case 306 -> "F17";
         case 307 -> "F18";
         case 308 -> "F19";
         case 309 -> "F20";
         case 310 -> "F21";
         case 311 -> "F22";
         case 312 -> "F23";
         case 313 -> "F24";
         case 314 -> "F25";
         case 335 -> "Numpad Enter";
         case 340 -> "Left Shift";
         case 341 -> "Left Control";
         case 342 -> "Left Alt";
         case 343 -> "Left Super";
         case 344 -> "Right Shift";
         case 345 -> "Right Control";
         case 346 -> "Right Alt";
         case 347 -> "Right Super";
         case 348 -> "Menu";
         default -> {
            String var2 = GLFW.glfwGetKeyName(var0, 0);
            if (var2 != null && !var2.isEmpty()) {
               yield StringUtils.capitalize(var2);
            } else {
               yield "Unknown";
            }
         }
      };
   }

   public static int a_int_String(String var0) {
      return switch (var0.toLowerCase()) {
         case "esc" -> 256;
         case "grave" -> 96;
         case "world1" -> 161;
         case "world2" -> 162;
         case "prtscr" -> 283;
         case "pause" -> 284;
         case "insert" -> 260;
         case "delete" -> 261;
         case "home" -> 268;
         case "pgup" -> 266;
         case "pgdown" -> 267;
         case "end" -> 269;
         case "tab" -> 258;
         case "lctrl" -> 341;
         case "rctrl" -> 345;
         case "lalt" -> 342;
         case "ralt" -> 346;
         case "lshift" -> 340;
         case "rshift" -> 344;
         case "up" -> 265;
         case "down" -> 264;
         case "left" -> 263;
         case "right" -> 262;
         case "apostrophe" -> 39;
         case "backspace" -> 259;
         case "capslock" -> 280;
         case "menu" -> 348;
         case "lsuper" -> 343;
         case "rsuper" -> 347;
         case "enter" -> 257;
         case "numenter" -> 335;
         case "numlock" -> 282;
         case "space" -> 32;
         case "f1" -> 290;
         case "f2" -> 291;
         case "f3" -> 292;
         case "f4" -> 293;
         case "f5" -> 294;
         case "f6" -> 295;
         case "f7" -> 296;
         case "f8" -> 297;
         case "f9" -> 298;
         case "f10" -> 299;
         case "f11" -> 300;
         case "f12" -> 301;
         case "f13" -> 302;
         case "f14" -> 303;
         case "f15" -> 304;
         case "f16" -> 305;
         case "f17" -> 306;
         case "f18" -> 307;
         case "f19" -> 308;
         case "f20" -> 309;
         case "f21" -> 310;
         case "f22" -> 311;
         case "f23" -> 312;
         case "f24" -> 313;
         case "f25" -> 314;
         case "rclick" -> -2;
         case "mclick" -> -3;
         case "button3" -> -4;
         case "button4" -> -5;
         case "none" -> 0;
         default -> {
            try {
               yield InputConstants.getKey("key.keyboard." + var0).getValue();
            } catch (NumberFormatException var6) {
               yield 0;
            }
         }
      };
   }

   public static boolean b(int var0) {
      if (var0 == 0) {
         return false;
      } else {
         Minecraft var1 = Minecraft.getInstance();
         if (var1.gui != null && var1.gui.screen() != null) {
            return false;
         } else if (var1.getWindow() == null) {
            return false;
         } else {
            return var0 >= 0 ? PacketAndRenderUtil.a_bool_int(var0) : PacketAndRenderUtil.b_bool_int(-var0 - 1);
         }
      }
   }
}
