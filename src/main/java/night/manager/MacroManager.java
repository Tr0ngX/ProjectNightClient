package night.manager;

import java.util.HashMap;
import lombok.Generated;
import net.minecraft.client.multiplayer.ClientPacketListener;
import night.Night;
import night.events.EventTarget;
import night.events.impl.KeyInputEvent;
import night.events.impl.MouseButtonEvent;
import night.util.MinecraftInstance;
import night.util.color.ChatColorUtil;
import night.util.input.KeyNameUtil;

public class MacroManager implements MinecraftInstance {
   private final HashMap<String, Integer> macros = new HashMap<>();

   public MacroManager() {
      Night.EVENT_BUS.register(this);
   }

   @EventTarget
   public void onKeyInput(KeyInputEvent var1) {
      if (mc.player != null && mc.level != null && (mc.gui == null || mc.gui.screen() == null)) {
         for (String var3 : (Iterable<String>)(Object)this.macros.keySet()) {
            HashMap var4 = this.macros;
            String var5 = var3;
            int var6 = (Integer)var4.get(var5);
            int var7 = var1.getKey();
            int var8 = var6;
            if (var7 == var8) {
               if (var3 == null) {
                  String var9 = String.valueOf(ChatColorUtil.a());
                  String var10 = KeyNameUtil.a_String_int(var6);
                  String var11 = String.valueOf(ChatColorUtil.b());
                  Night.chatManager.sendError("An error happened while executing the " + var9 + var10 + var11 + " macro.");
               } else {
                  String[] var12 = var3.split(";");
                  int var13 = var12.length;
                  int var14 = 0;

                  while (true) {
                     int var15 = var14;
                     int var16 = var13;
                     if (var15 >= var16) {
                        break;
                     }

                     String[] var17 = var12;
                     int var18 = var14;
                     String var19 = var17[var18];
                     if (var19.startsWith("/")) {
                        ClientPacketListener var20 = mc.player.connection;
                        String var21 = var19.substring(1);
                        var20.sendCommand(var21);
                     } else {
                        String var22 = var19;
                        String var23 = Night.commandManager.getPrefix();
                        if (var22.startsWith(var23)) {
                           String var24 = var19;
                           Night.commandManager.executeCommand(var24);
                        } else {
                           ClientPacketListener var25 = mc.player.connection;
                           String var26 = var19;
                           var25.sendChat(var26);
                        }
                     }

                     var14++;
                  }
               }
            }
         }
      }
   }

   @EventTarget
   public void onMouseButton(MouseButtonEvent var1) {
      if (mc.player != null && mc.level != null && (mc.gui == null || mc.gui.screen() == null)) {
         for (String var3 : (Iterable<String>)(Object)this.macros.keySet()) {
            HashMap var4 = this.macros;
            String var5 = var3;
            int var6 = (Integer)var4.get(var5);
            int var7 = -var1.getButton() - 1;
            if (var6 == var7) {
               if (var3 == null) {
                  String var8 = String.valueOf(ChatColorUtil.a());
                  String var9 = KeyNameUtil.a_String_int(var6);
                  String var10 = String.valueOf(ChatColorUtil.b());
                  Night.chatManager.sendError("An error happened while executing the " + var8 + var9 + var10 + " macro.");
               } else if (var3.startsWith("/")) {
                  ClientPacketListener var11 = mc.player.connection;
                  String var12 = var3.substring(1);
                  var11.sendCommand(var12);
               } else {
                  ClientPacketListener var13 = mc.player.connection;
                  String var14 = var3;
                  var13.sendChat(var14);
               }
            }
         }
      }
   }

   public String getMacroByKey(int var1) {
      for (String var3 : (Iterable<String>)(Object)this.macros.keySet()) {
         HashMap var4 = this.macros;
         String var5 = var3;
         int var6 = (Integer)var4.get(var5);
         if (var1 == var6) {
            return var3;
         }
      }

      return null;
   }

   public int getKeyByMacro(String var1) {
      return this.macros.get(var1);
   }

   public boolean hasMacroForKey(int var1) {
      HashMap var2 = this.macros;
      Integer var3 = var1;
      return var2.containsValue(var3);
   }

   public void addMacro(String var1, int var2) {
      HashMap var3 = this.macros;
      Integer var4 = var2;
      var3.put(var1, var4);
   }

   public void removeMacro(String var1) {
      this.macros.remove(var1);
   }

   public void clearMacros() {
      this.macros.clear();
   }

   @Generated
   public HashMap<String, Integer> getMacros() {
      return this.macros;
   }
}
