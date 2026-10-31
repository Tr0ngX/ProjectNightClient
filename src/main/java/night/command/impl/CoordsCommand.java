package night.command.impl;

import java.util.List;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.multiplayer.ClientPacketListener;
import night.Night;
import night.command.Command;
import night.command.CommandInfo;
import night.util.color.ChatColorUtil;

@CommandInfo(name = "coords", displayName = "Coords", description = "Copies your position, or whispers it to a player.", syntax = "| <[player]>")
public class CoordsCommand extends Command {
   @Override
   public List<String> getSuggestions(String[] var1) {
      if (var1.length == 0 && mc.getConnection() != null) {
         String var2;
         if (mc.player != null) {
            var2 = mc.player.getGameProfile().name();
         } else {
            var2 = "";
         }

         return mc.getConnection().getOnlinePlayers().stream().map(var0 -> var0.getProfile().name()).filter(var1x -> {
            boolean var2x;
            if (var1x != null && !var1x.isEmpty() && !var1x.equalsIgnoreCase(var2)) {
               var2x = true;
            } else {
               var2x = false;
            }

            return var2x;
         }).distinct().toList();
      } else {
         return List.of();
      }
   }

   @Override
   public void execute(String[] var1) {
      if (mc.player != null && mc.level != null) {
         String var2 = mc.player.level().dimension().identifier().toString().replace("minecraft:", "");
         int var3 = (int)mc.player.getX();
         int var4 = (int)mc.player.getY();
         int var5 = (int)mc.player.getZ();
         String var6 = var2;
         String var7 = var3 + " " + var4 + " " + var5 + " (" + var6 + ")";
         if (var1.length == 0) {
            KeyboardHandler var8 = mc.keyboardHandler;
            String var9 = var7;
            var8.setClipboard(var9);
            String var10 = String.valueOf(ChatColorUtil.a());
            String var11 = var7;
            String var12 = String.valueOf(ChatColorUtil.b());
            String var13 = this.getDisplayName();
            String var14 = this.getName();
            Night.chatManager.sendReplaceableTaggedMessage("Copied your position (" + var10 + var11 + var12 + ") to the clipboard.", var13, var14);
         } else if (var1.length == 1) {
            String var15 = var1[0];
            if (mc.getConnection() != null) {
               ClientPacketListener var16 = mc.getConnection();
               String var17 = var15;
               String var18 = var7;
               var16.sendCommand("w " + var17 + " " + var18);
               String var19 = String.valueOf(ChatColorUtil.a());
               String var20 = var15;
               String var21 = String.valueOf(ChatColorUtil.b());
               String var22 = this.getDisplayName();
               String var23 = this.getName();
               Night.chatManager.sendReplaceableTaggedMessage("Sent your position to " + var19 + var20 + var21 + ".", var22, var23);
            }
         } else {
            this.sendSyntax();
         }
      }
   }
}
