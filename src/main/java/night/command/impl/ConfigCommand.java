package night.command.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import night.Night;
import night.command.Command;
import night.command.CommandInfo;
import night.module.Module;
import night.module.Module.Category;
import night.module.impl.core.Capes;
import night.module.impl.core.ClickGuiModule;
import night.module.impl.core.ColorModule;
import night.module.impl.core.HudModule;
import night.setting.Setting;
import night.setting.impl.KeybindSetting;
import night.util.color.ChatColorUtil;
import night.util.input.KeyCodeParser;
import night.util.io.FileUtil;

@CommandInfo(
   name = "config",
   aliases = "cfg",
   displayName = "Config",
   description = "Allows you to manage the client's configuration system.",
   syntax = "<load|save|reset> <[name]> | <name> reset | global <[module]> <load|save|reset> <[name]> | bind <load|save|reset> <[name]> | visual <load|save|reset> <[name]> | modules <load|save|reset> <[name]> | <reload|save|current|reset>"
)
public class ConfigCommand extends Command {
   private static final List<Class<? extends Module>> a = List.of(ColorModule.class, ClickGuiModule.class, HudModule.class, Capes.class);

   public List<String> getSuggestions(String[] var1) {
      if (var1.length == 0) {
         return List.of("load", "save", "reset", "reload", "current", "global", "bind", "visual", "modules");
      }

      if (var1.length == 1) {
         if (var1[0].equalsIgnoreCase("load") || var1[0].equalsIgnoreCase("save") || var1[0].equalsIgnoreCase("reset")) {
            return this.h();
         } else if (var1[0].equalsIgnoreCase("global")) {
            return Night.moduleManager.getModules().stream().<String>map(Module::getName).toList();
         } else {
            return !var1[0].equalsIgnoreCase("bind") && !var1[0].equalsIgnoreCase("visual") && !var1[0].equalsIgnoreCase("modules")
               ? List.of("reset", "load", "save")
               : List.of("load", "save", "reset");
         }
      } else {
         if (var1.length == 2) {
            if (var1[0].equalsIgnoreCase("global")) {
               return List.of("load", "save", "reset");
            }

            if (var1[0].equalsIgnoreCase("bind") && (var1[1].equalsIgnoreCase("load") || var1[1].equalsIgnoreCase("save") || var1[1].equalsIgnoreCase("reset"))
               )
             {
               return this.i();
            }

            if (var1[0].equalsIgnoreCase("visual")
               && (var1[1].equalsIgnoreCase("load") || var1[1].equalsIgnoreCase("save") || var1[1].equalsIgnoreCase("reset"))) {
               return this.j();
            }

            if (var1[0].equalsIgnoreCase("modules")
               && (var1[1].equalsIgnoreCase("load") || var1[1].equalsIgnoreCase("save") || var1[1].equalsIgnoreCase("reset"))) {
               return this.k();
            }
         }

         if (var1.length != 3
            || !var1[0].equalsIgnoreCase("global")
            || !var1[2].equalsIgnoreCase("load") && !var1[2].equalsIgnoreCase("save") && !var1[2].equalsIgnoreCase("reset")) {
            return List.of();
         }

         String var2 = var1[1];
         return this.c(var2);
      }
   }

   private List<String> h() {
      File var1 = new File("Night/Configs");
      if (!var1.exists()) {
         var1.mkdirs();
      }

      File[] var2 = var1.listFiles((var0, var1x) -> var1x.endsWith(".json"));
      return var2 == null ? List.of() : Arrays.stream(var2).map(File::getName).map(var0 -> {
         int var1x = var0.length();
         int var2x = ".json".length();
         int var3 = var1x - var2x;
         return var0.substring(0, var3);
      }).sorted().toList();
   }

   private List<String> c(String var1) {
      File var2 = new File("Night/GlobalConfigs/" + var1);
      if (!var2.exists()) {
         var2.mkdirs();
      }

      File[] var3 = var2.listFiles((var0, var1x) -> var1x.endsWith(".json"));
      return var3 == null ? List.of() : Arrays.stream(var3).map(File::getName).map(var0 -> {
         int var1x = var0.length();
         int var2x = ".json".length();
         int var3x = var1x - var2x;
         return var0.substring(0, var3x);
      }).sorted().toList();
   }

   private List<String> i() {
      File var1 = new File("Night/Binds");
      if (!var1.exists()) {
         var1.mkdirs();
      }

      File[] var2 = var1.listFiles((var0, var1x) -> var1x.endsWith(".json"));
      return var2 == null ? List.of() : Arrays.stream(var2).map(File::getName).map(var0 -> {
         int var1x = var0.length();
         int var2x = ".json".length();
         int var3 = var1x - var2x;
         return var0.substring(0, var3);
      }).sorted().toList();
   }

   private List<String> j() {
      File var1 = new File("Night/Visuals");
      if (!var1.exists()) {
         var1.mkdirs();
      }

      File[] var2 = var1.listFiles((var0, var1x) -> var1x.endsWith(".json"));
      return var2 == null ? List.of() : Arrays.stream(var2).map(File::getName).map(var0 -> {
         int var1x = var0.length();
         int var2x = ".json".length();
         int var3 = var1x - var2x;
         return var0.substring(0, var3);
      }).sorted().toList();
   }

   private List<String> k() {
      File var1 = new File("Night/Modules");
      if (!var1.exists()) {
         var1.mkdirs();
      }

      File[] var2 = var1.listFiles((var0, var1x) -> var1x.endsWith(".json"));
      return var2 == null ? List.of() : Arrays.stream(var2).map(File::getName).map(var0 -> {
         int var1x = var0.length();
         int var2x = ".json".length();
         int var3 = var1x - var2x;
         return var0.substring(0, var3);
      }).sorted().toList();
   }

   private List<Module> l() {
      List var1 = Night.moduleManager.getModulesByCategory(Category.VISUALS);
      ArrayList var2 = new ArrayList(var1);

      for (Class var4 : (Iterable<Class>)(Object)a) {
         Module var5 = Night.moduleManager.getModule(var4);
         if (var5 != null) {
            ArrayList var6 = var2;
            Module var7 = var5;
            var6.add(var7);
         }
      }

      return var2;
   }

   public void execute(String[] var1) {
      if (var1.length > 0 && var1[0].equalsIgnoreCase("bind")) {
         if (var1.length == 3) {
            String var2 = var1[1].toLowerCase();
            String var3 = var1[2];
            switch (var2) {
               case "save":
                  String var91 = var3;
                  this.h(var91);
                  break;
               case "load":
                  String var89 = var3;
                  this.i(var89);
                  break;
               case "reset":
                  String var90 = var3;
                  this.e(var90);
                  break;
               default:
                  Night.chatManager.sendInfo("config bind <load|save|reset> <name>");
            }
         } else {
            Night.chatManager.sendInfo("config bind <load|save|reset> <name>");
         }
      } else if (var1.length > 0 && var1[0].equalsIgnoreCase("visual")) {
         if (var1.length == 3) {
            String var6 = var1[1].toLowerCase();
            String var7 = var1[2];
            switch (var6) {
               case "save":
                  String var11 = var7;
                  this.j(var11);
                  break;
               case "load":
                  String var10 = var7;
                  this.k(var10);
                  break;
               case "reset":
                  String var12 = var7;
                  this.f(var12);
                  break;
               default:
                  Night.chatManager.sendInfo("config visual <load|save|reset> <name>");
            }
         } else {
            Night.chatManager.sendInfo("config visual <load|save|reset> <name>");
         }
      } else if (var1.length > 0 && var1[0].equalsIgnoreCase("modules")) {
         if (var1.length == 3) {
            String var13 = var1[1].toLowerCase();
            String var14 = var1[2];
            switch (var13) {
               case "save":
                  String var18 = var14;
                  this.l(var18);
                  break;
               case "load":
                  String var19 = var14;
                  this.m(var19);
                  break;
               case "reset":
                  String var17 = var14;
                  this.g(var17);
                  break;
               default:
                  Night.chatManager.sendInfo("config modules <load|save|reset> <name>");
            }
         } else {
            Night.chatManager.sendInfo("config modules <load|save|reset> <name>");
         }
      } else if (var1.length > 0 && var1[0].equalsIgnoreCase("global")) {
         if (var1.length == 4) {
            String var20 = var1[1];
            Module var21 = Night.moduleManager.getModuleByName(var20);
            if (var21 == null) {
               String var22 = this.getDisplayName();
               String var23 = this.getName();
               Night.chatManager.sendReplaceableTaggedMessage("Could not find the module specified.", var22, var23);
               return;
            }

            String var79 = var1[2].toLowerCase();
            String var80 = var1[3];
            switch (var79) {
               case "save":
                  Module var87 = var21;
                  String var88 = var80;
                  this.b_void_Module_String(var87, var88);
                  break;
               case "load":
                  Module var85 = var21;
                  String var86 = var80;
                  this.c_void_Module_String(var85, var86);
                  break;
               case "reset":
                  Module var83 = var21;
                  String var84 = var80;
                  this.a_void_Module_String(var83, var84);
                  break;
               default:
                  Night.chatManager.sendInfo("config global <module> <load|save|reset> <name>");
            }
         } else {
            Night.chatManager.sendInfo("config global <module> <load|save|reset> <name>");
         }
      } else {
         if (var1.length == 2) {
            if (var1[1].equalsIgnoreCase("reset")) {
               String var24 = var1[0];
               this.d(var24);
               return;
            }

            switch (var1[0].toLowerCase()) {
               case "load":
                  if (!FileUtil.c("Night/Configs/" + var1[1] + ".json")) {
                     String var27 = this.getDisplayName();
                     String var28 = this.getName();
                     Night.chatManager.sendReplaceableTaggedMessage("The specified configuration does not exist.", var27, var28);
                     return;
                  }

                  try {
                     String var29 = var1[1];
                     Night.configManager.loadConfig(var29);
                     String var30 = String.valueOf(ChatColorUtil.a());
                     String var31 = var1[1];
                     String var32 = String.valueOf(ChatColorUtil.b());
                     String var33 = this.getDisplayName();
                     String var34 = this.getName();
                     Night.chatManager.sendReplaceableTaggedMessage("Successfully loaded the " + var30 + var31 + var32 + " configuration.", var33, var34);
                  } catch (Exception var94) {
                     String var36 = String.valueOf(ChatColorUtil.a());
                     String var37 = var1[1];
                     String var38 = String.valueOf(ChatColorUtil.b());
                     String var39 = this.getDisplayName();
                     String var40 = this.getName();
                     Night.chatManager.sendReplaceableTaggedMessage("Failed to load the " + var36 + var37 + var38 + " configuration.", var39, var40);
                  }
                  break;
               case "save":
                  try {
                     String var41 = var1[1];
                     Night.configManager.saveConfig(var41);
                     String var42 = String.valueOf(ChatColorUtil.a());
                     String var43 = var1[1];
                     String var44 = String.valueOf(ChatColorUtil.b());
                     String var45 = this.getDisplayName();
                     String var46 = this.getName();
                     Night.chatManager
                        .sendReplaceableTaggedMessage("Successfully saved the configuration to " + var42 + var43 + ".json" + var44 + ".", var45, var46);
                  } catch (Exception var93) {
                     String var48 = String.valueOf(ChatColorUtil.a());
                     String var49 = var1[1];
                     String var50 = String.valueOf(ChatColorUtil.b());
                     String var51 = this.getDisplayName();
                     String var52 = this.getName();
                     Night.chatManager.sendReplaceableTaggedMessage("Failed to save the " + var48 + var49 + var50 + " configuration.", var51, var52);
                  }
                  break;
               case "reset":
                  String var53 = var1[1];
                  this.d(var53);
                  break;
               default:
                  this.sendSyntax();
            }
         } else if (var1.length == 1) {
            switch (var1[0].toLowerCase()) {
               case "reload":
                  String var57 = Night.configManager.getCurrentConfig();
                  if (!FileUtil.c("Night/Configs/" + var57 + ".json")) {
                     String var58 = String.valueOf(ChatColorUtil.a());
                     String var59 = var57;
                     String var60 = String.valueOf(ChatColorUtil.b());
                     String var61 = this.getDisplayName();
                     String var62 = this.getName();
                     Night.chatManager
                        .sendReplaceableTaggedMessage("The current configuration (" + var58 + var59 + var60 + ") does not exist on disk.", var61, var62);
                     return;
                  }

                  try {
                     String var63 = var57;
                     Night.configManager.loadConfig(var63);
                     String var64 = this.getDisplayName();
                     String var65 = this.getName();
                     Night.chatManager.sendReplaceableTaggedMessage("Successfully reloaded the current configuration.", var64, var65);
                  } catch (Exception var92) {
                     String var67 = String.valueOf(ChatColorUtil.a());
                     String var68 = var57;
                     String var69 = String.valueOf(ChatColorUtil.b());
                     String var70 = this.getDisplayName();
                     String var71 = this.getName();
                     Night.chatManager.sendReplaceableTaggedMessage("Failed to reload the " + var67 + var68 + var69 + " configuration.", var70, var71);
                  }
                  break;
               case "save":
                  Night.configManager.saveAll();
                  String var72 = this.getDisplayName();
                  String var73 = this.getName();
                  Night.chatManager.sendReplaceableTaggedMessage("Successfully saved the current configuration.", var72, var73);
                  break;
               case "reset":
                  String var56 = Night.configManager.getCurrentConfig();
                  this.d(var56);
                  break;
               case "current":
                  String var74 = String.valueOf(ChatColorUtil.a());
                  String var75 = Night.configManager.getCurrentConfig();
                  String var76 = String.valueOf(ChatColorUtil.b());
                  String var77 = this.getDisplayName();
                  String var78 = this.getName();
                  Night.chatManager
                     .sendReplaceableTaggedMessage("The client is currently using the " + var74 + var75 + var76 + " configuration.", var77, var78);
                  break;
               default:
                  this.sendSyntax();
            }
         } else {
            this.sendSyntax();
         }
      }
   }

   private void d(String var1) {
      for (Module var3 : (Iterable<Module>)(Object)Night.moduleManager.getModules()) {
         var3.setEnabled(false);
         var3.resetSettings();
      }

      try {
         Night.configManager.saveConfig(var1);
         String var4 = String.valueOf(ChatColorUtil.a());
         String var5 = String.valueOf(ChatColorUtil.b());
         String var6 = this.getDisplayName();
         String var7 = this.getName();
         Night.chatManager.sendReplaceableTaggedMessage("Successfully reset the " + var4 + var1 + var5 + " configuration to default.", var6, var7);
      } catch (Exception var13) {
         String var9 = String.valueOf(ChatColorUtil.a());
         String var10 = String.valueOf(ChatColorUtil.b());
         String var11 = this.getDisplayName();
         String var12 = this.getName();
         Night.chatManager.sendReplaceableTaggedMessage("Failed to reset the " + var9 + var1 + var10 + " configuration.", var11, var12);
      }
   }

   private void a_void_Module_String(Module var1, String var2) {
      var1.resetSettings();
      this.b_void_Module_String(var1, var2);
   }

   private void e(String var1) {
      for (Module var3 : (Iterable<Module>)(Object)Night.moduleManager.getModules()) {
         var3.bind.reset();

         for (Setting var5 : var3.getSettings()) {
            if (var5 instanceof KeybindSetting) {
               ((KeybindSetting)var5).reset();
            }
         }
      }

      this.h(var1);
   }

   private void f(String var1) {
      Iterator var2 = this.l().iterator();

      while (var2.hasNext()) {
         ((Module)var2.next()).resetSettings();
      }

      this.j(var1);
   }

   private void g(String var1) {
      for (Module var3 : (Iterable<Module>)(Object)Night.moduleManager.getModules()) {
         var3.setEnabled(false);
         var3.resetSettings();
      }

      this.l(var1);
   }

   private void b_void_Module_String(Module var1, String var2) {
      try {
         JsonObject var3 = Night.configManager.serializeModule(var1);
         File var4 = new File("Night/GlobalConfigs/" + var1.getName());
         if (!var4.exists()) {
            var4.mkdirs();
         }

         File var5 = var4;
         File var6 = new File(var5, var2 + ".json");

         try (FileWriter var7 = new FileWriter(var6)) {
            Gson var8 = new GsonBuilder().setPrettyPrinting().create();
            JsonObject var9 = var3;
            FileWriter var10 = var7;
            var8.toJson(var9, var10);
         }

         String var15 = String.valueOf(ChatColorUtil.a());
         String var16 = String.valueOf(ChatColorUtil.b());
         String var17 = String.valueOf(ChatColorUtil.a());
         String var18 = var1.getName();
         String var19 = String.valueOf(ChatColorUtil.b());
         String var20 = this.getDisplayName();
         String var21 = this.getName();
         Night.chatManager
            .sendReplaceableTaggedMessage(
               "Successfully saved global config " + var15 + var2 + var16 + " for module " + var17 + var18 + var19 + ".", var20, var21
            );
      } catch (Exception var27) {
         String var23 = this.getDisplayName();
         String var24 = this.getName();
         Night.chatManager.sendReplaceableTaggedMessage("Failed to save global config: " + var27.getMessage(), var23, var24);
         var27.printStackTrace();
      }
   }

   private void c_void_Module_String(Module var1, String var2) {
      File var3 = new File("Night/GlobalConfigs/" + var1.getName() + "/" + var2 + ".json");
      if (!var3.exists()) {
         String var4 = String.valueOf(ChatColorUtil.a());
         String var5 = String.valueOf(ChatColorUtil.b());
         String var6 = var1.getName();
         String var7 = this.getDisplayName();
         String var8 = this.getName();
         Night.chatManager.sendReplaceableTaggedMessage("Global config " + var4 + var2 + var5 + " does not exist for " + var6 + ".", var7, var8);
      } else {
         try {
            File var9 = var3;

            try (FileReader var10 = new FileReader(var9)) {
               JsonObject var11 = JsonParser.parseReader(var10).getAsJsonObject();
               File[] var12 = new File("Night/Configs").listFiles((var0, var1x) -> var1x.endsWith(".json"));
               if (var12 != null) {
                  Gson var13 = new GsonBuilder().setPrettyPrinting().create();
                  File[] var14 = var12;
                  int var15 = var14.length;
                  int var16 = 0;

                  while (true) {
                     int var17 = var16;
                     int var18 = var15;
                     if (var17 >= var18) {
                        break;
                     }

                     File[] var19 = var14;
                     int var20 = var16;
                     File var21 = var19[var20];
                     FileReader var22 = new FileReader(var21);

                     JsonObject var23;
                     try {
                        var23 = JsonParser.parseReader(var22).getAsJsonObject();
                     } catch (Throwable var62) {
                        try {
                           var22.close();
                        } catch (Throwable var60) {
                           var62.addSuppressed(var60);
                        }

                        throw var62;
                     }

                     var22.close();
                     JsonObject var28;
                     if (var23.has("Modules")) {
                        var28 = var23.getAsJsonObject("Modules");
                     } else {
                        var28 = new JsonObject();
                     }

                     JsonObject var29 = var28;
                     String var30 = var1.getName();
                     JsonObject var31 = var11;
                     var29.add(var30, var31);
                     JsonObject var32 = var23;
                     JsonObject var33 = var29;
                     var32.add("Modules", var33);
                     File var34 = var21;

                     try (FileWriter var35 = new FileWriter(var34)) {
                        Gson var36 = var13;
                        JsonObject var37 = var23;
                        FileWriter var38 = var35;
                        var36.toJson(var37, var38);
                     }

                     var16++;
                  }
               }

               String var43 = Night.configManager.getCurrentConfig();
               Night.configManager.loadConfig(var43);
               String var44 = String.valueOf(ChatColorUtil.a());
               String var45 = String.valueOf(ChatColorUtil.b());
               String var46 = String.valueOf(ChatColorUtil.a());
               String var47 = var1.getName();
               String var48 = String.valueOf(ChatColorUtil.b());
               String var49 = this.getDisplayName();
               String var50 = this.getName();
               Night.chatManager
                  .sendReplaceableTaggedMessage(
                     "Successfully loaded global config " + var44 + var2 + var45 + " for module " + var46 + var47 + var48 + " across all profiles.",
                     var49,
                     var50
                  );
            }
         } catch (Exception var64) {
            String var56 = this.getDisplayName();
            String var57 = this.getName();
            Night.chatManager.sendReplaceableTaggedMessage("Failed to load global config: " + var64.getMessage(), var56, var57);
            var64.printStackTrace();
         }
      }
   }

   private void h(String var1) {
      try {
         JsonObject var2 = new JsonObject();

         for (Module var4 : (Iterable<Module>)(Object)Night.moduleManager.getModules()) {
            JsonObject var5 = new JsonObject();
            String var6 = var4.bind.getName();
            String var7 = KeyCodeParser.a_String_int(var4.bind.getKey());
            String var8 = var4.bind.getMode();
            var5.add(var6, new JsonPrimitive(var7 + "," + var8));

            for (Setting var10 : (Iterable<Setting>)(Object)var4.getSettings()) {
               KeybindSetting var11 = var4.bind;
               if (var10 != var11 && var10 instanceof KeybindSetting var12) {
                  JsonObject var13 = var5;
                  String var14 = var12.getName();
                  String var15 = KeyCodeParser.a_String_int(var12.getKey());
                  String var16 = var12.getMode();
                  var13.add(var14, new JsonPrimitive(var15 + "," + var16));
               }
            }

            JsonObject var17 = var2;
            String var18 = var4.getName();
            JsonObject var19 = var5;
            var17.add(var18, var19);
         }

         File var20 = new File("Night/Binds");
         if (!var20.exists()) {
            var20.mkdirs();
         }

         File var21 = var20;
         File var22 = new File(var21, var1 + ".json");

         try (FileWriter var23 = new FileWriter(var22)) {
            Gson var24 = new GsonBuilder().setPrettyPrinting().create();
            JsonObject var25 = var2;
            FileWriter var26 = var23;
            var24.toJson(var25, var26);
         }

         String var31 = String.valueOf(ChatColorUtil.a());
         String var32 = String.valueOf(ChatColorUtil.b());
         String var33 = this.getDisplayName();
         String var34 = this.getName();
         Night.chatManager.sendReplaceableTaggedMessage("Successfully saved the current binds to " + var31 + var1 + ".json" + var32 + ".", var33, var34);
      } catch (Exception var40) {
         String var36 = this.getDisplayName();
         String var37 = this.getName();
         Night.chatManager.sendReplaceableTaggedMessage("Failed to save bind profile: " + var40.getMessage(), var36, var37);
         var40.printStackTrace();
      }
   }

   private void i(String var1) {
      File var2 = new File("Night/Binds/" + var1 + ".json");
      if (!var2.exists()) {
         String var3 = String.valueOf(ChatColorUtil.a());
         String var4 = String.valueOf(ChatColorUtil.b());
         String var5 = this.getDisplayName();
         String var6 = this.getName();
         Night.chatManager.sendReplaceableTaggedMessage("Bind profile " + var3 + var1 + var4 + " does not exist.", var5, var6);
      } else {
         try {
            File var7 = var2;

            try (FileReader var8 = new FileReader(var7)) {
               JsonObject var9 = JsonParser.parseReader(var8).getAsJsonObject();

               for (String var11 : (Iterable<String>)(Object)var9.keySet()) {
                  Module var12 = Night.moduleManager.getModuleByName(var11);
                  if (var12 != null) {
                     JsonObject var13 = var9;
                     String var14 = var11;
                     JsonObject var15 = var13.getAsJsonObject(var14);

                     for (String var17 : (Iterable<String>)(Object)var15.keySet()) {
                        String var18 = var12.bind.getName();
                        KeybindSetting var19;
                        if (var17.equals(var18)) {
                           var19 = var12.bind;
                        } else {
                           Module var20 = var12;
                           String var21 = var17;
                           var19 = this.d_KeybindSetting_Module_String(var20, var21);
                        }

                        KeybindSetting var22 = var19;
                        if (var22 != null) {
                           JsonObject var23 = var15;
                           String var24 = var17;
                           String[] var25 = var23.get(var24).getAsString().split(",", 2);
                           KeybindSetting var26 = var22;
                           int var27 = KeyCodeParser.a_int_String(var25[0]);
                           var26.setKey(var27);
                           if (var25.length > 1) {
                              KeybindSetting var28 = var22;
                              String var29 = var25[1];
                              var28.setMode(var29);
                           }
                        }
                     }
                  }
               }

               String var30 = String.valueOf(ChatColorUtil.a());
               String var31 = String.valueOf(ChatColorUtil.b());
               String var32 = this.getDisplayName();
               String var33 = this.getName();
               Night.chatManager.sendReplaceableTaggedMessage("Successfully loaded the " + var30 + var1 + var31 + " bind profile.", var32, var33);
            }
         } catch (Exception var43) {
            String var39 = this.getDisplayName();
            String var40 = this.getName();
            Night.chatManager.sendReplaceableTaggedMessage("Failed to load bind profile: " + var43.getMessage(), var39, var40);
            var43.printStackTrace();
         }
      }
   }

   private KeybindSetting d_KeybindSetting_Module_String(Module var1, String var2) {
      for (Setting var4 : var1.getSettings()) {
         if (var4 instanceof KeybindSetting var5 && var5.getName().equals(var2)) {
            return var5;
         }
      }

      return null;
   }

   private void j(String var1) {
      try {
         JsonObject var2 = new JsonObject();

         for (Module var4 : (Iterable<Module>)(Object)this.l()) {
            JsonObject var5 = var2;
            String var6 = var4.getName();
            Module var7 = var4;
            JsonObject var8 = Night.configManager.serializeModule(var7);
            var5.add(var6, var8);
         }

         File var9 = new File("Night/Visuals");
         if (!var9.exists()) {
            var9.mkdirs();
         }

         File var10 = var9;
         File var11 = new File(var10, var1 + ".json");

         try (FileWriter var12 = new FileWriter(var11)) {
            Gson var13 = new GsonBuilder().setPrettyPrinting().create();
            JsonObject var14 = var2;
            FileWriter var15 = var12;
            var13.toJson(var14, var15);
         }

         String var20 = String.valueOf(ChatColorUtil.a());
         String var21 = String.valueOf(ChatColorUtil.b());
         String var22 = this.getDisplayName();
         String var23 = this.getName();
         Night.chatManager
            .sendReplaceableTaggedMessage("Successfully saved the current visual settings to " + var20 + var1 + ".json" + var21 + ".", var22, var23);
      } catch (Exception var29) {
         String var25 = this.getDisplayName();
         String var26 = this.getName();
         Night.chatManager.sendReplaceableTaggedMessage("Failed to save visual config: " + var29.getMessage(), var25, var26);
         var29.printStackTrace();
      }
   }

   private void k(String var1) {
      File var2 = new File("Night/Visuals/" + var1 + ".json");
      if (!var2.exists()) {
         String var3 = String.valueOf(ChatColorUtil.a());
         String var4 = String.valueOf(ChatColorUtil.b());
         String var5 = this.getDisplayName();
         String var6 = this.getName();
         Night.chatManager.sendReplaceableTaggedMessage("Visual config " + var3 + var1 + var4 + " does not exist.", var5, var6);
      } else {
         Set var7 = this.l().stream().map(Module::getName).collect(Collectors.toSet());

         try {
            File var8 = var2;

            try (FileReader var9 = new FileReader(var8)) {
               JsonObject var10 = JsonParser.parseReader(var9).getAsJsonObject();

               for (String var12 : (Iterable<String>)(Object)var10.keySet()) {
                  Module var13 = Night.moduleManager.getModuleByName(var12);
                  if (var13 != null) {
                     Set var14 = var7;
                     String var15 = var13.getName();
                     if (var14.contains(var15)) {
                        Module var16 = var13;
                        JsonObject var17 = var10;
                        String var18 = var12;
                        JsonObject var19 = var17.getAsJsonObject(var18);
                        Night.configManager.a(var16, var19);
                     }
                  }
               }

               String var20 = String.valueOf(ChatColorUtil.a());
               String var21 = String.valueOf(ChatColorUtil.b());
               String var22 = this.getDisplayName();
               String var23 = this.getName();
               Night.chatManager.sendReplaceableTaggedMessage("Successfully loaded the " + var20 + var1 + var21 + " visual config.", var22, var23);
            }
         } catch (Exception var33) {
            String var29 = this.getDisplayName();
            String var30 = this.getName();
            Night.chatManager.sendReplaceableTaggedMessage("Failed to load visual config: " + var33.getMessage(), var29, var30);
            var33.printStackTrace();
         }
      }
   }

   private void l(String var1) {
      try {
         JsonObject var2 = new JsonObject();

         for (Module var4 : (Iterable<Module>)(Object)Night.moduleManager.getModules()) {
            JsonObject var5 = var2;
            String var6 = var4.getName();
            Module var7 = var4;
            JsonObject var8 = Night.configManager.serializeModule(var7, false);
            var5.add(var6, var8);
         }

         File var9 = new File("Night/Modules");
         if (!var9.exists()) {
            var9.mkdirs();
         }

         File var10 = var9;
         File var11 = new File(var10, var1 + ".json");

         try (FileWriter var12 = new FileWriter(var11)) {
            Gson var13 = new GsonBuilder().setPrettyPrinting().create();
            JsonObject var14 = var2;
            FileWriter var15 = var12;
            var13.toJson(var14, var15);
         }

         String var20 = String.valueOf(ChatColorUtil.a());
         String var21 = String.valueOf(ChatColorUtil.b());
         String var22 = this.getDisplayName();
         String var23 = this.getName();
         Night.chatManager
            .sendReplaceableTaggedMessage("Successfully saved the current module states to " + var20 + var1 + ".json" + var21 + ".", var22, var23);
      } catch (Exception var29) {
         String var25 = this.getDisplayName();
         String var26 = this.getName();
         Night.chatManager.sendReplaceableTaggedMessage("Failed to save module states: " + var29.getMessage(), var25, var26);
         var29.printStackTrace();
      }
   }

   private void m(String var1) {
      File var2 = new File("Night/Modules/" + var1 + ".json");
      if (!var2.exists()) {
         String var3 = String.valueOf(ChatColorUtil.a());
         String var4 = String.valueOf(ChatColorUtil.b());
         String var5 = this.getDisplayName();
         String var6 = this.getName();
         Night.chatManager.sendReplaceableTaggedMessage("Module state profile " + var3 + var1 + var4 + " does not exist.", var5, var6);
      } else {
         try {
            File var7 = var2;

            try (FileReader var8 = new FileReader(var7)) {
               JsonObject var9 = JsonParser.parseReader(var8).getAsJsonObject();

               for (String var11 : (Iterable<String>)(Object)var9.keySet()) {
                  Module var12 = Night.moduleManager.getModuleByName(var11);
                  if (var12 != null) {
                     JsonObject var13 = var9;
                     String var14 = var11;
                     JsonElement var15 = var13.get(var14);
                     if (var15.isJsonObject()) {
                        Module var16 = var12;
                        JsonObject var17 = var15.getAsJsonObject();
                        Night.configManager.a_void_Module_JsonObject_bool(var16, var17, false);
                     } else if (var15.isJsonPrimitive() && var15.getAsJsonPrimitive().isBoolean()) {
                        Module var18 = var12;
                        boolean var19 = var15.getAsBoolean();
                        var18.setEnabled(var19);
                     }
                  }
               }

               String var20 = String.valueOf(ChatColorUtil.a());
               String var21 = String.valueOf(ChatColorUtil.b());
               String var22 = this.getDisplayName();
               String var23 = this.getName();
               Night.chatManager.sendReplaceableTaggedMessage("Successfully loaded the " + var20 + var1 + var21 + " module states.", var22, var23);
            }
         } catch (Exception var33) {
            String var29 = this.getDisplayName();
            String var30 = this.getName();
            Night.chatManager.sendReplaceableTaggedMessage("Failed to load module states: " + var33.getMessage(), var29, var30);
            var33.printStackTrace();
         }
      }
   }
}
