package night.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import lombok.Generated;
import night.Night;
import night.command.impl.ModuleCommand;
import night.events.EventTarget;
import night.events.impl.ChatSendEvent;
import night.module.Module;
import night.security.ClasspathScanner;

public class CommandManager {
   private final ArrayList<Command> commands = new ArrayList<>();
   private String prefix = ".";
   private final CommandDispatcher<CommandManager> dispatcher = new CommandDispatcher();

   public CommandManager() {
      Night.EVENT_BUS.register(this);

      try {
         for (Class var2 : (Iterable<Class>)(Object)ClasspathScanner.a_List_Class_Class(Command.class, Night.class)) {
            if (var2.getAnnotation(CommandInfo.class) != null && var2 != ModuleCommand.class) {
               ArrayList var3 = this.commands;
               Class var4 = var2;
               Class[] var5 = new Class[0];
               Constructor var6 = var4.getDeclaredConstructor(var5);
               Object[] var7 = new Object[0];
               Command var8 = (Command)var6.newInstance(var7);
               var3.add(var8);
            }
         }
      } catch (ReflectiveOperationException var27) {
         Night.LOGGER.error("Failed to register the client's modules!", var27);
      }

      HashSet var10 = new HashSet();

      for (Command var12 : (Iterable<Command>)(Object)this.commands) {
         HashSet var13 = var10;
         String var14 = var12.getName().toLowerCase();
         var13.add(var14);

         for (String var16 : (Iterable<String>)(Object)var12.getAliases()) {
            HashSet var17 = var10;
            String var18 = var16.toLowerCase();
            var17.add(var18);
         }
      }

      for (Module var20 : (Iterable<Module>)(Object)Night.moduleManager.getModules()) {
         HashSet var21 = var10;
         String var22 = var20.getName().toLowerCase();
         if (!var21.contains(var22)) {
            ArrayList var23 = this.commands;
            Module var24 = var20;
            var23.add(new ModuleCommand(var24));
         }
      }

      for (Command var26 : (Iterable<Command>)(Object)this.commands) {
         this.registerCommand(var26);
      }
   }

   private void registerCommand(Command var1) {
      ArrayList var2 = new ArrayList();
      String var3 = var1.getName();
      var2.add(var3);
      ArrayList var4 = var2;
      List var5 = var1.getAliases();
      var4.addAll(var5);
      Iterator var6 = var2.iterator();

      while (var6.hasNext()) {
         LiteralArgumentBuilder var7 = LiteralArgumentBuilder.literal((String)var6.next());
         var7.executes(var1x -> {
            String[] var2x = new String[0];
            var1.execute(var2x);
            return 1;
         });
         LiteralArgumentBuilder var8 = var7;
         StringArgumentType var9 = StringArgumentType.greedyString();
         ArgumentBuilder var10 = RequiredArgumentBuilder.argument("args", var9).suggests((var1x, var2x) -> {
            String var3x = var2x.getRemaining();
            String[] var4x = var3x.split(" ", -1);
            int var5x = var4x.length - 1;
            String var6x = var4x[var5x].toLowerCase();
            String[] var7x = var4x;
            int var8x = var4x.length - 1;
            String[] var9x = Arrays.copyOf(var7x, var8x);
            int var10x = var2x.getStart();
            int var11x = var3x.length();
            int var12x = var10x + var11x;
            int var13 = var6x.length();
            int var14 = var12x - var13;
            SuggestionsBuilder var15 = var2x.createOffset(var14);
            String[] var16 = var9x;

            for (String var18 : (Iterable<String>)(Object)var1.getSuggestions(var16)) {
               String var19 = var18.toLowerCase();
               String var20 = var6x;
               if (var19.startsWith(var20)) {
                  SuggestionsBuilder var21 = var15;
                  String var22 = var18;
                  var21.suggest(var22);
               }
            }

            return var15.buildFuture();
         }).executes(var1x -> {
            String var2x = StringArgumentType.getString(var1x, "args");
            String[] var3x;
            if (var2x.isEmpty()) {
               var3x = new String[0];
            } else {
               var3x = var2x.split(" ");
            }

            var1.execute(var3x);
            return 1;
         });
         var8.then(var10);
         CommandDispatcher var11 = this.dispatcher;
         LiteralArgumentBuilder var12 = var7;
         var11.register(var12);
      }
   }

   @EventTarget
   public void onChatSend(ChatSendEvent var1) {
      String var2 = var1.getMessage();
      String var3 = this.prefix;
      if (var2.startsWith(var3)) {
         var1.setCancelled(true);
         String var4 = var2;
         this.executeCommand(var4);
      }
   }

   public void executeCommand(String var1) {
      try {
         CommandDispatcher var2 = this.dispatcher;
         int var3 = this.prefix.length();
         String var4 = var1.substring(var3);
         var2.execute(var4, this);
      } catch (CommandSyntaxException var6) {
         Night.chatManager.sendWarning("Could not find the command specified.");
      }
   }

   public Command getCommand(String var1) {
      return this.commands.stream().filter(var1x -> {
         String var2 = var1x.getName();
         String var3 = var1.toLowerCase();
         if (!var2.equalsIgnoreCase(var3)) {
            List var4 = var1x.getAliases();
            String var5 = var1.toLowerCase();
            if (!var4.contains(var5)) {
               return false;
            }
         }

         return true;
      }).findFirst().orElse(null);
   }

   @Generated
   public ArrayList<Command> getCommands() {
      return this.commands;
   }

   @Generated
   public String getPrefix() {
      return this.prefix;
   }

   @Generated
   public void setPrefix(String var1) {
      this.prefix = var1;
   }

   @Generated
   public CommandDispatcher<CommandManager> getDispatcher() {
      return this.dispatcher;
   }
}
