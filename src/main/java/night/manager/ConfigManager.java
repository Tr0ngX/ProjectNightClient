package night.manager;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.Iterator;
import java.util.StringJoiner;
import java.util.stream.Stream;
import lombok.Generated;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.module.Module;
import night.module.impl.core.PingBypassModule;
import night.module.impl.misc.FakePlayer;
import night.pingbypass.PingBypassState;
import night.setting.Setting;
import night.setting.impl.BooleanSetting;
import night.setting.impl.ColorSetting;
import night.setting.impl.ImageSetting;
import night.setting.impl.KeybindSetting;
import night.setting.impl.ModeSetting;
import night.setting.impl.NumberSetting;
import night.setting.impl.PositionSetting;
import night.setting.impl.RangeNumberSetting;
import night.setting.impl.StringSetting;
import night.setting.impl.SubBindSetting;
import night.setting.impl.WhitelistSetting;
import night.util.input.KeyCodeParser;
import night.util.io.FileUtil;
import night.util.world.RegistryLookupUtil;

public class ConfigManager {
   private String currentConfig = "default";
   private boolean loading = false;

   public ConfigManager() {
      this.loadAll();
      Runtime.getRuntime().addShutdownHook(new Thread(this::saveOnShutdown));
   }

   public void loadAll() {
      try {
         this.m();
         FileUtil.b("Night");
         FileUtil.b("Night/Configs");
         FileUtil.b("Night/Client");
         FileUtil.b("Night/Modules");
         FileUtil.b("Night/Binds");
         FileUtil.b("Night/Visuals");
         FileUtil.b("Night/GlobalConfigs");
         this.loadGeneral();
         this.loadWaypoints();
         if (FileUtil.c("Night/Client/Active.json")) {
            this.loadActiveConfig();
         } else {
            this.loadConfig(this.currentConfig);
         }
      } catch (Exception var2) {
         Night.LOGGER.error("Failed to load the client's configuration!", var2);
         if (Night.chatManager != null) {
            Night.chatManager.queueMessage("The configuration has not been loaded properly. Read the stacktrace for more information.");
         }
      }
   }

   private void m() {
      try {
         Path var1 = Paths.get("EUClient");
         Path var2 = Paths.get("Night");
         if (Files.exists(var1) && (!Files.exists(var2) || !Files.exists(var2.resolve("Configs")))) {
            Night.LOGGER.info("Migrating configuration from EUClient to Night...");
            a(var1, var2);
         }
      } catch (Exception var3) {
         Night.LOGGER.warn("Failed to migrate old EUClient configuration: {}", var3.getMessage());
      }
   }

   private static void a(Path var0, Path var1) throws IOException {
      try (Stream<Path> var2 = Files.walk(var0)) {
         var2.forEach(var2x -> {
            try {
               Path var3 = var1.resolve(var0.relativize(var2x));
               if (Files.isDirectory(var2x)) {
                  if (!Files.exists(var3)) {
                     Files.createDirectories(var3);
                  }
               } else if (!Files.exists(var3)) {
                  if (var3.getParent() != null && !Files.exists(var3.getParent())) {
                     Files.createDirectories(var3.getParent());
                  }

                  Files.copy(var2x, var3, StandardCopyOption.COPY_ATTRIBUTES);
               }
            } catch (IOException var4) {
               Night.LOGGER.warn("Could not copy config file {}: {}", var2x, var4.getMessage());
            }
         });
      }
   }

   public void saveAll() {
      try {
         FileUtil.b("Night");
         FileUtil.b("Night/Configs");
         FileUtil.b("Night/Client");
         FileUtil.b("Night/Modules");
         FileUtil.b("Night/Binds");
         FileUtil.b("Night/Visuals");
         FileUtil.b("Night/GlobalConfigs");
         this.saveGeneral();
         this.saveWaypoints();
         this.saveConfig(this.currentConfig);
      } catch (IOException var2) {
         Night.LOGGER.error("Failed to save the client's configuration!", var2);
      }
   }

   public void saveOnShutdown() {
      try {
         FileUtil.b("Night");
         FileUtil.b("Night/Configs");
         FileUtil.b("Night/Client");
         FileUtil.b("Night/Modules");
         FileUtil.b("Night/Binds");
         FileUtil.b("Night/Visuals");
         FileUtil.b("Night/GlobalConfigs");
         this.saveGeneral();
         this.saveWaypoints();
         this.saveActiveConfig();
      } catch (IOException var2) {
         Night.LOGGER.error("Failed to save the client's active configuration on exit!", var2);
      }
   }

   public void saveActiveConfig() throws IOException {
      FileUtil.b("Night/Client");
      FileUtil.a_void_String("Night/Client/Active.json");
      JsonObject var1 = new JsonObject();
      var1.add("Config", new JsonPrimitive(this.currentConfig));
      var1.add("Modules", this.serializeModules());
      OutputStreamWriter var2 = new OutputStreamWriter(new FileOutputStream("Night/Client/Active.json"), StandardCharsets.UTF_8);

      try {
         var2.write(new GsonBuilder().setPrettyPrinting().create().toJson(JsonParser.parseString(var1.toString())));
      } finally {
         if (Collections.singletonList(var2).get(0) != null) {
            var2.close();
         }
      }
   }

   public void loadWaypoints() throws IOException {
      if (FileUtil.c("Night/Waypoints.json")) {
         InputStream var1 = Files.newInputStream(Paths.get("Night/Waypoints.json"));

         try {
            JsonObject var2;
            try {
               var2 = JsonParser.parseReader(new InputStreamReader(var1)).getAsJsonObject();
            } catch (IllegalStateException var8) {
               Night.LOGGER.error("Failed to load the client's Waypoint configuration!", var8);
               Night.chatManager.queueMessage("The Waypoint configuration has not been loaded properly. Read the stacktrace for more information.");
               return;
            }

            if (var2.has("Waypoints")) {
               Iterator var3 = var2.get("Waypoints").getAsJsonArray().iterator();

               while (var3.hasNext()) {
                  String[] var4 = ((JsonElement)var3.next()).getAsString().split(":");
                  if (!Night.waypointManager.hasWaypoint(var4[0])) {
                     Night.waypointManager
                        .addWaypoint(var4[0], new Vec3(Integer.parseInt(var4[1]), Integer.parseInt(var4[2]), Integer.parseInt(var4[3])), var4[4], var4[5]);
                  }
               }
            }
         } finally {
            if (Collections.singletonList(var1).get(0) != null) {
               var1.close();
            }
         }
      }
   }

   public void loadGeneral() throws IOException {
      if (FileUtil.c("Night/General.json")) {
         InputStream var1 = Files.newInputStream(Paths.get("Night/General.json"));

         try {
            JsonObject var3;
            try {
               JsonElement var2 = JsonParser.parseReader(new InputStreamReader(var1));
               if (var2 == null || !var2.isJsonObject()) {
                  Night.LOGGER.warn("General.json is empty or invalid JSON.");
                  return;
               }

               var3 = var2.getAsJsonObject();
            } catch (Exception var13) {
               Night.LOGGER.error("Failed to load the client's General configuration!", var13);
               if (Night.chatManager != null) {
                  Night.chatManager.queueMessage("The General configuration has not been loaded properly. Read the stacktrace for more information.");
               }

               return;
            }

            if (var3.has("Config")) {
               this.currentConfig = var3.get("Config").getAsString();
            }

            if (var3.has("Prefix") && Night.commandManager != null) {
               Night.commandManager.setPrefix(var3.get("Prefix").getAsString());
            }

            if (var3.has("Friends") && Night.friendManager != null) {
               for (JsonElement var4 : (Iterable<JsonElement>)(Object)var3.get("Friends").getAsJsonArray()) {
                  if (!Night.friendManager.isFriend(var4.getAsString())) {
                     Night.friendManager.addFriend(var4.getAsString());
                  }
               }
            }

            if (var3.has("Macros") && Night.macroManager != null) {
               for (JsonElement var17 : (Iterable<JsonElement>)(Object)var3.get("Macros").getAsJsonArray()) {
                  try {
                     String[] var5 = var17.getAsString().split(":", 2);
                     if (var5.length > 1) {
                        int var6 = KeyCodeParser.a_int_String(var5[0]);
                        String var7 = var5[1];
                        Night.macroManager.addMacro(var7, var6);
                     }
                  } catch (NumberFormatException var12) {
                     Night.LOGGER.error("Failed to load the " + var17.getAsString() + " macro!", var12);
                     if (Night.chatManager != null) {
                        Night.chatManager.queueMessage("The " + var17.getAsString() + " macro has failed to load. Read the stacktrace for more information.");
                     }
                  }
               }
            }
         } finally {
            if (Collections.singletonList(var1).get(0) != null) {
               var1.close();
            }
         }
      }
   }

   public void saveGeneral() throws IOException {
      FileUtil.a_void_String("Night/General.json");
      JsonObject var1 = new JsonObject();
      var1.add("Config", new JsonPrimitive(this.currentConfig));
      var1.add("Prefix", new JsonPrimitive(Night.commandManager.getPrefix()));
      JsonArray var2 = new JsonArray();
      Night.friendManager.getFriends().forEach(var2::add);
      var1.add("Friends", var2);
      JsonArray var3 = new JsonArray();
      Night.macroManager.getMacros().forEach((var1x, var2x) -> var3.add(KeyCodeParser.a_String_int(var2x) + ":" + var1x));
      var1.add("Macros", var3);
      OutputStreamWriter var4 = new OutputStreamWriter(new FileOutputStream("Night/General.json"), StandardCharsets.UTF_8);

      try {
         var4.write(new GsonBuilder().setPrettyPrinting().create().toJson(JsonParser.parseString(var1.toString())));
      } finally {
         if (Collections.singletonList(var4).get(0) != null) {
            var4.close();
         }
      }
   }

   public void saveWaypoints() throws IOException {
      FileUtil.a_void_String("Night/Waypoints.json");
      JsonObject var1 = new JsonObject();
      JsonArray var2 = new JsonArray();

      for (WaypointManager.Waypoint var4 : (Iterable<WaypointManager.Waypoint>)(Object)Night.waypointManager.getWaypoints()) {
         var2.add(
            var4.getName()
               + ":"
               + (int)var4.getPos().x
               + ":"
               + (int)var4.getPos().y
               + ":"
               + (int)var4.getPos().z
               + ":"
               + var4.getDimension()
               + ":"
               + var4.getServer()
         );
      }

      var1.add("Waypoints", var2);
      OutputStreamWriter var8 = new OutputStreamWriter(new FileOutputStream("Night/Waypoints.json"), StandardCharsets.UTF_8);

      try {
         var8.write(new GsonBuilder().setPrettyPrinting().create().toJson(JsonParser.parseString(var1.toString())));
      } finally {
         if (Collections.singletonList(var8).get(0) != null) {
            var8.close();
         }
      }
   }

   public void loadConfig(String var1) throws IOException {
      if (FileUtil.c("Night/Configs/" + var1 + ".json")) {
         InputStream var2 = Files.newInputStream(Paths.get("Night/Configs/" + var1 + ".json"));

         try {
            JsonObject var4;
            try {
               JsonElement var3 = JsonParser.parseReader(new InputStreamReader(var2));
               if (var3 == null || !var3.isJsonObject()) {
                  Night.LOGGER.warn("Module configuration is empty or invalid JSON: {}", var1);
                  return;
               }

               var4 = var3.getAsJsonObject();
            } catch (Exception var20) {
               Night.LOGGER.error("Failed to load the client's Module configuration!", var20);
               if (Night.chatManager != null) {
                  Night.chatManager.queueMessage("The configuration for the Modules has not been loaded properly. Read the stacktrace for more information.");
               }

               return;
            }

            if (var4.has("Modules")) {
               JsonObject var22 = var4.get("Modules").getAsJsonObject();
               this.loading = true;

               try {
                  this.a(var22);
               } finally {
                  this.loading = false;
               }

               this.currentConfig = var1;

               try {
                  this.saveGeneral();
               } catch (IOException var18) {
               }

               if (PingBypassState.d && Night.pingBypassConfig != null && !Night.pingBypassConfig.isServerMode()) {
                  PingBypassModule var5 = Night.moduleManager.getModule(PingBypassModule.class);
                  if (var5 != null && var5.isEnabled()) {
                     var5.v();
                  }
               }

               try {
                  this.saveActiveConfig();
               } catch (IOException var17) {
               }
            }
         } finally {
            if (Collections.singletonList(var2).get(0) != null) {
               var2.close();
            }
         }
      }
   }

   public void loadActiveConfig() throws IOException {
      if (!FileUtil.c("Night/Client/Active.json")) {
         this.loadConfig(this.currentConfig);
      } else {
         JsonObject var3;
         try {
            label126: {
               try (InputStream var1 = Files.newInputStream(Paths.get("Night/Client/Active.json"))) {
                  JsonElement var2 = JsonParser.parseReader(new InputStreamReader(var1));
                  if (var2 != null && var2.isJsonObject()) {
                     var3 = var2.getAsJsonObject();
                     break label126;
                  }

                  Night.LOGGER.warn("Active configuration is empty or invalid JSON.");
                  this.loadConfig(this.currentConfig);
               }

               return;
            }
         } catch (Exception var13) {
            Night.LOGGER.error("Failed to load the client's Active configuration!", var13);
            this.loadConfig(this.currentConfig);
            return;
         }

         if (!var3.has("Modules")) {
            this.loadConfig(this.currentConfig);
         } else {
            JsonObject var14 = var3.get("Modules").getAsJsonObject();
            this.loading = true;

            try {
               this.a(var14);
            } finally {
               this.loading = false;
            }

            if (var3.has("Config")) {
               this.currentConfig = var3.get("Config").getAsString();
            }

            if (PingBypassState.d && Night.pingBypassConfig != null && !Night.pingBypassConfig.isServerMode()) {
               PingBypassModule var15 = Night.moduleManager.getModule(PingBypassModule.class);
               if (var15 != null && var15.isEnabled()) {
                  var15.v();
               }
            }
         }
      }
   }

   private void a(JsonObject var1) {
      for (Module var3 : (Iterable<Module>)(Object)Night.moduleManager.getModules()) {
         if (!var1.has(var3.getName())) {
            var3.setEnabled(false);
            var3.resetSettings();
         } else {
            this.a(var3, var1.get(var3.getName()).getAsJsonObject());
         }
      }
   }

   public void a(Module var1, JsonObject var2) {
      this.a_void_Module_JsonObject_bool(var1, var2, true);
   }

   public void a_void_Module_JsonObject_bool(Module var1, JsonObject var2, boolean var3) {
      boolean var4 = var2.has("Status") && var2.get("Status").getAsBoolean();
      if (!var2.has("Settings")) {
         if (var3) {
            var1.resetSettings();
         }

         var1.setEnabled(var4, false);
      } else {
         JsonObject var5 = var2.get("Settings").getAsJsonObject();

         for (Setting var7 : (Iterable<Setting>)(Object)var1.getSettings()) {
            if (var3 || !(var7 instanceof KeybindSetting)) {
               JsonElement var8 = var5.get(var7.getName());
               if (var8 != null && var8.isJsonPrimitive()) {
                  Setting var29 = var7;
                  switch (var29) {
                     case BooleanSetting var31:
                        if (var8.getAsJsonPrimitive().isBoolean()) {
                           var31.setValue(var8.getAsBoolean());
                        } else {
                           var31.reset();
                        }
                        break;
                     case RangeNumberSetting var32:
                        try {
                           var32.parseRange(var8.getAsString());
                        } catch (NumberFormatException var28) {
                           var32.reset();
                        }
                        break;
                     case NumberSetting var33:
                        if (var8.getAsJsonPrimitive().isNumber()) {
                           var33.setValue(var8.getAsNumber());
                        } else {
                           var33.reset();
                        }
                        break;
                     case ModeSetting var34:
                        var34.setValue(var8.getAsString());
                        break;
                     case StringSetting var35:
                        String var37 = var8.getAsString();
                        if (var37 != null && var37.equalsIgnoreCase("EUClient")) {
                           var37 = "Night";
                        }

                        var35.setValue(var37);
                        break;
                     case ImageSetting var36:
                        var36.setValue(var8.getAsString());
                        if (Night.customImageManager != null) {
                           Night.customImageManager.setActiveImage(var8.getAsString());
                        }
                        break;
                     case SubBindSetting var38:
                        if (var8.isJsonPrimitive() && var8.getAsJsonPrimitive().isBoolean()) {
                           var38.getTargetSetting().setValue(var8.getAsBoolean());
                        } else {
                           String[] var40 = var8.getAsString().split(",");
                           var38.setKey(KeyCodeParser.a_int_String(var40[0]));
                           if (var40.length > 2) {
                              var38.getTargetSetting().setValue(Boolean.parseBoolean(var40[2]));
                           }
                        }
                        break;
                     case KeybindSetting var39:
                        if (var8.isJsonPrimitive() && var8.getAsJsonPrimitive().isNumber()) {
                           var39.setKey(KeyCodeParser.a_int_String(var8.getAsString()));
                        } else {
                           String[] var42 = var8.getAsString().split(",", 2);
                           var39.setKey(KeyCodeParser.a_int_String(var42[0]));
                           if (var42.length > 1) {
                              var39.setMode(var42[1]);
                           }
                        }
                        break;
                     case ColorSetting var41:
                        String[] var43 = var8.getAsString().split(",");
                        if (var43.length == 6) {
                           var41.setColor(
                              new Color(
                                 Math.clamp(Integer.parseInt(var43[0]), 0, 255),
                                 Math.clamp(Integer.parseInt(var43[1]), 0, 255),
                                 Math.clamp(Integer.parseInt(var43[2]), 0, 255),
                                 Math.clamp(Integer.parseInt(var43[3]), 0, 255)
                              )
                           );
                           var41.setSync(Boolean.parseBoolean(var43[4]));
                           var41.setRainbow(Boolean.parseBoolean(var43[5]));
                        }
                        break;
                     case PositionSetting var20:
                        String[] var44 = var8.getAsString().split(",");
                        if (var44.length == 2) {
                           var20.setPosition(Float.parseFloat(var44[0]), Float.parseFloat(var44[1]));
                        }
                        break;
                     case WhitelistSetting var46:
                        WhitelistSetting var21 = (WhitelistSetting)var29;
                        var21.clear();
                        String[] var22 = var8.getAsString().split(",");
                        if (var22.length != 0) {
                           for (String var26 : var22) {
                              if (var21.getType() == WhitelistSetting.WhitelistType.ITEMS) {
                                 Item var27 = RegistryLookupUtil.a(var26);
                                 if (var27 != null) {
                                    var21.add(var27);
                                 }
                              } else if (var21.getType() == WhitelistSetting.WhitelistType.BLOCKS) {
                                 Block var45 = RegistryLookupUtil.b(var26);
                                 if (var45 != null) {
                                    var21.add(var45);
                                 }
                              }
                           }
                        }
                        break;
                     default:
                  }
               } else {
                  switch (var7) {
                     case BooleanSetting var11:
                        var11.reset();
                        break;
                     case NumberSetting var12:
                        var12.reset();
                        break;
                     case ModeSetting var13:
                        var13.reset();
                        break;
                     case StringSetting var14:
                        var14.reset();
                        break;
                     case ImageSetting var15:
                        var15.reset();
                        break;
                     case KeybindSetting var16:
                        var16.reset();
                        break;
                     case ColorSetting var17:
                        var17.reset();
                        break;
                     case WhitelistSetting var18:
                        var18.clear();
                        break;
                     case PositionSetting var19:
                        var19.reset();
                        break;
                     default:
                  }
               }
            }
         }

         var1.setEnabled(var4, false);
      }
   }

   public JsonObject serializeModules() {
      JsonObject var1 = new JsonObject();

      for (Module var3 : (Iterable<Module>)(Object)Night.moduleManager.getModules()) {
         JsonObject var4 = this.serializeModule(var3);
         if (var3 instanceof FakePlayer) {
            var4.add("Status", new JsonPrimitive(false));
         }

         var1.add(var3.getName(), var4);
      }

      return var1;
   }

   public void saveConfig(String var1) throws IOException {
      FileUtil.a_void_String("Night/Configs/" + var1 + ".json");
      JsonObject var2 = new JsonObject();
      var2.add("Config", new JsonPrimitive(var1));
      var2.add("Modules", this.serializeModules());
      OutputStreamWriter var3 = new OutputStreamWriter(new FileOutputStream("Night/Configs/" + var1 + ".json"), StandardCharsets.UTF_8);

      try {
         var3.write(new GsonBuilder().setPrettyPrinting().create().toJson(JsonParser.parseString(var2.toString())));
         this.currentConfig = var1;

         try {
            this.saveGeneral();
         } catch (IOException var10) {
         }

         try {
            this.saveActiveConfig();
         } catch (IOException var9) {
         }
      } finally {
         if (Collections.singletonList(var3).get(0) != null) {
            var3.close();
         }
      }
   }

   public JsonObject serializeModule(Module var1) {
      return this.serializeModule(var1, true);
   }

   public JsonObject serializeModule(Module var1, boolean var2) {
      JsonObject var3 = new JsonObject();
      var3.add("Status", new JsonPrimitive(var1.isEnabled()));
      JsonObject var4 = new JsonObject();

      for (Setting var6 : (Iterable<Setting>)(Object)var1.getSettings()) {
         if (var2 || !(var6 instanceof KeybindSetting)) {
            Setting var7 = var6;
            switch (var7) {
               case BooleanSetting var9:
                  var4.add(var9.getName(), new JsonPrimitive(var9.getValue()));
                  break;
               case RangeNumberSetting var10:
                  var4.add(var10.getName(), new JsonPrimitive(var10.getRangeString()));
                  break;
               case NumberSetting var11:
                  var4.add(var11.getName(), new JsonPrimitive(var11.getValue()));
                  break;
               case ModeSetting var12:
                  var4.add(var12.getName(), new JsonPrimitive(var12.getValue()));
                  break;
               case StringSetting var13:
                  var4.add(var13.getName(), new JsonPrimitive(var13.getValue()));
                  break;
               case ImageSetting var14:
                  var4.add(var14.getName(), new JsonPrimitive(var14.getValue()));
                  break;
               case SubBindSetting var15:
                  var4.add(var15.getName(), new JsonPrimitive(KeyCodeParser.a_String_int(var15.getKey()) + ",Bind," + var15.getTargetSetting().getValue()));
                  break;
               case KeybindSetting var16:
                  var4.add(var16.getName(), new JsonPrimitive(KeyCodeParser.a_String_int(var16.getKey()) + "," + var16.getMode()));
                  break;
               case ColorSetting var17:
                  var4.add(
                     var17.getName(),
                     new JsonPrimitive(
                        var17.getColorValue().getColor().getRed()
                           + ","
                           + var17.getColorValue().getColor().getGreen()
                           + ","
                           + var17.getColorValue().getColor().getBlue()
                           + ","
                           + var17.getColorValue().getColor().getAlpha()
                           + ","
                           + var17.isSync()
                           + ","
                           + var17.isRainbow()
                     )
                  );
                  break;
               case WhitelistSetting var18:
                  StringJoiner var22 = new StringJoiner(",");

                  for (String var21 : (Iterable<String>)(Object)var18.getEntryIds()) {
                     var22.add(var21);
                  }

                  var4.add(var18.getName(), new JsonPrimitive(var22.toString()));
                  break;
               case PositionSetting var23:
                  PositionSetting var19 = (PositionSetting)var7;
                  var4.add(var19.getName(), new JsonPrimitive(var19.getX() + "," + var19.getY()));
                  break;
               default:
            }
         }
      }

      var3.add("Settings", var4);
      return var3;
   }

   @Generated
   public String getCurrentConfig() {
      return this.currentConfig;
   }

   @Generated
   public boolean isLoading() {
      return this.loading;
   }
}
