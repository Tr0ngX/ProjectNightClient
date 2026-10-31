package night.command;

import java.util.Arrays;
import java.util.List;
import lombok.Generated;
import night.Night;
import night.util.MinecraftInstance;

public abstract class Command implements MinecraftInstance {
   private String name;
   private String displayName;
   private final String description;
   private final String syntax;
   private final List<String> aliases;

   public Command() {
      CommandInfo var1 = this.getClass().getAnnotation(CommandInfo.class);
      String var2 = var1.name();
      this.name = var2;
      String var3;
      if (var1.displayName().isEmpty()) {
         var3 = var1.name();
      } else {
         var3 = var1.displayName();
      }

      this.displayName = var3;
      String var4 = var1.description();
      this.description = var4;
      String var5 = var1.syntax();
      this.syntax = var5;
      List var6 = Arrays.asList(var1.aliases());
      this.aliases = var6;
   }

   public abstract void execute(String[] var1);

   public List<String> getSuggestions(String[] var1) {
      return List.of();
   }

   protected List<String> getModuleNames() {
      return Night.moduleManager.getModules().stream().map(var0 -> var0.getName().toLowerCase()).toList();
   }

   public void sendSyntax() {
      String var1 = this.name;
      String var2 = this.syntax;
      Night.chatManager.sendInfo(var1 + " " + var2);
   }

   @Generated
   public String getName() {
      return this.name;
   }

   @Generated
   public String getDisplayName() {
      return this.displayName;
   }

   @Generated
   public String getDescription() {
      return this.description;
   }

   @Generated
   public String getSyntax() {
      return this.syntax;
   }

   @Generated
   public List<String> getAliases() {
      return this.aliases;
   }

   @Generated
   public void setName(String var1) {
      this.name = var1;
   }

   @Generated
   public void setDisplayName(String var1) {
      this.displayName = var1;
   }
}
