package night.command.impl;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import night.Night;
import night.command.Command;
import night.command.CommandInfo;
import night.manager.ChatManager;
import night.module.Module;
import night.setting.Setting;
import night.setting.impl.BooleanSetting;
import night.setting.impl.CategorySetting;
import night.setting.impl.ColorSetting;
import night.setting.impl.KeybindSetting;
import night.setting.impl.ModeSetting;
import night.setting.impl.NumberSetting;
import night.setting.impl.RangeNumberSetting;
import night.setting.impl.StringSetting;
import night.setting.impl.WhitelistSetting;
import night.setting.impl.WhitelistSetting.WhitelistType;
import night.util.color.ChatColorUtil;
import night.util.color.ColorUtil;
import night.util.input.KeyNameUtil;
import night.util.inventory.ShulkerSearchUtil;
import night.util.world.RegistryLookupUtil;

@CommandInfo(
   name = "module",
   displayName = "Module",
   description = "Allows you to view and change this module's settings.",
   syntax = "<[setting]> <[...]> | <list|reset>"
)
public class ModuleCommand extends Command {
   private final Module a;

   public ModuleCommand(Module var1) {
      this.a = var1;
      String var2 = var1.getName().toLowerCase();
      this.setName(var2);
      String var3 = var1.getName();
      this.setDisplayName(var3);
   }

   public List<String> getSuggestions(String[] var1) {
      if (var1.length == 0) {
         ArrayList var2 = new ArrayList();
         var2.add("list");
         var2.add("reset");

         for (Setting var4 : (Iterable<Setting>)(Object)this.a.getSettings()) {
            if (!(var4 instanceof CategorySetting)) {
               ArrayList var5 = var2;
               String var6 = var4.getName().toLowerCase();
               var5.add(var6);
            }
         }

         return var2;
      } else if (var1.length == 1) {
         Module var7 = this.a;
         String var8 = var1[0];
         Object var9 = var7.getSetting(var8);
         if (var9 == null) {
            return List.of();
         }

         return switch (var9) {
            case BooleanSetting var14 -> List.of("true", "false", "reset");
            case NumberSetting var21 -> List.of("reset");
            case ModeSetting var23 -> {
               List var18 = ((ModeSetting)var9).getModes();
               ArrayList var19 = new ArrayList(var18);
               var19.add("reset");
               var19.add("list");
               yield var19;
            }
            case StringSetting var22 -> List.of("force-reset");
            case KeybindSetting var20 -> List.of("reset");
            case ColorSetting var16 -> List.of("red", "green", "blue", "alpha", "sync", "rainbow", "code", "reset");
            case WhitelistSetting var17 -> List.of("add", "del", "clear", "list");
            default -> List.of();
         };
      } else {
         if (var1.length == 2) {
            Module var10 = this.a;
            String var11 = var1[0];
            if (var10.getSetting(var11) instanceof ColorSetting) {
               String var12 = var1[1].toLowerCase();
               if (var12.equals("sync") || var12.equals("rainbow")) {
                  return List.of("true", "false");
               }
            }
         }

         return List.of();
      }
   }

   public void execute(String[] var1) {
      if (var1.length < 1) {
         this.sendSyntax();
      } else if (var1[0].equalsIgnoreCase("list")) {
         if (this.a.getSettings().isEmpty()) {
            String var2 = this.a.getName();
            Night.chatManager.sendReplaceableTaggedMessage("This module currently has no registered settings.", var2, "module-cmd-" + this.getName() + "-list");
         } else {
            StringBuilder var3 = new StringBuilder();
            int var4 = 0;

            for (Setting var6 : (Iterable<Setting>)(Object)this.a.getSettings()) {
               if (!(var6 instanceof CategorySetting)) {
                  StringBuilder var7 = var3;
                  Object var8 = ChatColorUtil.b();
                  StringBuilder var9 = var7.append(var8);
                  String var10 = var6.getName();
                  StringBuilder var11 = var9.append(var10);
                  Object var12 = ChatColorUtil.a();
                  StringBuilder var13 = var11.append(var12).append(" [");
                  Object var14 = ChatColorUtil.b();
                  var13.append(var14);
                  if (var6 instanceof BooleanSetting) {
                     StringBuilder var15 = var3;
                     boolean var16 = ((BooleanSetting)var6).getValue();
                     var15.append(var16);
                  }

                  if (var6 instanceof RangeNumberSetting var17) {
                     StringBuilder var18 = var3;
                     String var19 = var17.getRangeString();
                     var18.append(var19);
                  } else if (var6 instanceof NumberSetting) {
                     StringBuilder var20 = var3;
                     Number var21 = ((NumberSetting)var6).getValue();
                     var20.append(var21);
                  }

                  if (var6 instanceof ModeSetting) {
                     StringBuilder var22 = var3;
                     String var23 = ((ModeSetting)var6).getValue();
                     var22.append(var23);
                  }

                  if (var6 instanceof StringSetting) {
                     StringBuilder var24 = var3;
                     String var25 = ((StringSetting)var6).getValue();
                     var24.append(var25);
                  }

                  if (var6 instanceof KeybindSetting) {
                     StringBuilder var26 = var3;
                     String var27 = KeyNameUtil.a_String_int(((KeybindSetting)var6).getKey()).toUpperCase();
                     var26.append(var27);
                  }

                  if (var6 instanceof WhitelistSetting) {
                     var3.append("...");
                  }

                  if (var6 instanceof ColorSetting) {
                     StringBuilder var28 = var3.append("RGBA(");
                     int var29 = ((ColorSetting)var6).getColor().getRed();
                     StringBuilder var30 = var28.append(var29);
                     Object var31 = ChatColorUtil.a();
                     StringBuilder var32 = var30.append(var31).append(", ");
                     Object var33 = ChatColorUtil.b();
                     StringBuilder var34 = var32.append(var33);
                     int var35 = ((ColorSetting)var6).getColor().getGreen();
                     StringBuilder var36 = var34.append(var35);
                     Object var37 = ChatColorUtil.a();
                     StringBuilder var38 = var36.append(var37).append(", ");
                     Object var39 = ChatColorUtil.b();
                     StringBuilder var40 = var38.append(var39);
                     int var41 = ((ColorSetting)var6).getColor().getBlue();
                     StringBuilder var42 = var40.append(var41);
                     Object var43 = ChatColorUtil.a();
                     StringBuilder var44 = var42.append(var43).append(", ");
                     Object var45 = ChatColorUtil.b();
                     StringBuilder var46 = var44.append(var45);
                     int var47 = ((ColorSetting)var6).getColor().getAlpha();
                     StringBuilder var48 = var46.append(var47).append(")");
                     Object var49 = ChatColorUtil.a();
                     StringBuilder var50 = var48.append(var49).append(",");
                     Object var51 = ChatColorUtil.b();
                     StringBuilder var52 = var50.append(var51).append(" SyRa(");
                     boolean var53 = ((ColorSetting)var6).isSync();
                     StringBuilder var54 = var52.append(var53);
                     Object var55 = ChatColorUtil.a();
                     StringBuilder var56 = var54.append(var55).append(", ");
                     Object var57 = ChatColorUtil.b();
                     StringBuilder var58 = var56.append(var57);
                     boolean var59 = ((ColorSetting)var6).isRainbow();
                     var58.append(var59).append(")");
                  }

                  StringBuilder var60 = var3;
                  Object var61 = ChatColorUtil.a();
                  StringBuilder var62 = var60.append(var61).append("]");
                  Object var63 = ChatColorUtil.b();
                  StringBuilder var64 = var62.append(var63);
                  int var65 = var4 + 1;
                  int var66 = this.a.getSettings().size();
                  String var67;
                  if (var65 == var66) {
                     var67 = "";
                  } else {
                     var67 = ", ";
                  }

                  var64.append(var67);
                  var4++;
               }
            }

            String var68 = String.valueOf(ChatColorUtil.b());
            String var69 = this.a.getName();
            String var70 = String.valueOf(ChatColorUtil.a());
            String var71 = String.valueOf(ChatColorUtil.b());
            int var72 = this.a.getSettings().size();
            String var73 = String.valueOf(ChatColorUtil.a());
            String var74 = String.valueOf(ChatColorUtil.b());
            String var75 = String.valueOf(var3);
            Night.chatManager
               .sendReplaceableMessage(
                  var68 + var69 + " " + var70 + "[" + var71 + var72 + var73 + "]: " + var74 + var75, "module-cmd-" + this.getName() + "-list"
               );
         }
      } else if (var1[0].equalsIgnoreCase("reset")) {
         this.a.resetSettings();
         String var76 = this.a.getName();
         Night.chatManager.sendReplaceableTaggedMessage("Successfully reset all of the module's settings.", var76, "module-cmd-" + this.getName());
      } else {
         Module var77 = this.a;
         String var78 = var1[0];
         Object var79 = var77.getSetting(var78);
         if (var79 == null) {
            String var80 = this.a.getName();
            Night.chatManager.sendReplaceableTaggedMessage("Could not find the setting specified.", var80, "module-cmd-" + this.getName());
            return;
         }

         int var81 = var1.length;
         String[] var82 = Arrays.copyOfRange(var1, 1, var81);
         switch (var79) {
            case BooleanSetting var266:
               BooleanSetting var267 = this.a.chatNotify;
               if (var266 == var267) {
                  String var268 = String.valueOf(ChatColorUtil.a());
                  String var269 = String.valueOf(ChatColorUtil.b());
                  String var270 = this.a.getName();
                  String var271 = this.getName();
                  Night.chatManager
                     .sendReplaceableTaggedMessage(
                        "This setting is the module's main ChatNotify setting. Please use the " + var268 + "chatnotify" + var269 + " command instead.",
                        var270,
                        var271
                     );
                  return;
               }

               BooleanSetting var272 = var266;
               BooleanSetting var273 = this.a.drawn;
               if (var272 == var273) {
                  String var274 = String.valueOf(ChatColorUtil.a());
                  String var275 = String.valueOf(ChatColorUtil.b());
                  String var276 = this.a.getName();
                  String var277 = this.getName();
                  Night.chatManager
                     .sendReplaceableTaggedMessage(
                        "This setting is the module's main Drawn setting. Please use the " + var274 + "drawn" + var275 + " command instead.", var276, var277
                     );
                  return;
               }

               if (var82.length == 1) {
                  if (var82[0].equalsIgnoreCase("reset")) {
                     var266.reset();
                     String var278 = String.valueOf(ChatColorUtil.a());
                     String var279 = var266.getName();
                     String var280 = String.valueOf(ChatColorUtil.b());
                     String var281 = this.a.getName();
                     Night.chatManager
                        .sendReplaceableTaggedMessage(
                           "Successfully reset the " + var278 + var279 + var280 + " setting.", var281, "module-cmd-" + this.getName()
                        );
                  } else {
                     BooleanSetting var282 = var266;
                     boolean var283 = Boolean.parseBoolean(var82[0]);
                     var282.setValue(var283);
                     String var284 = String.valueOf(ChatColorUtil.a());
                     String var285 = var266.getName();
                     String var286 = String.valueOf(ChatColorUtil.b());
                     String var287 = String.valueOf(ChatColorUtil.a());
                     boolean var288 = var266.getValue();
                     String var289 = String.valueOf(ChatColorUtil.b());
                     String var290 = this.a.getName();
                     Night.chatManager
                        .sendReplaceableTaggedMessage(
                           "Successfully set " + var284 + var285 + var286 + " to " + var287 + var288 + var289 + ".", var290, "module-cmd-" + this.getName()
                        );
                  }
               } else {
                  String var291 = this.a.getName().toLowerCase();
                  String var292 = var266.getName().toLowerCase();
                  Night.chatManager.sendInfo(var291 + " " + var292 + " <[value]|reset>");
               }
               break;
            case RangeNumberSetting var293:
               if (var82.length == 1) {
                  if (var82[0].equalsIgnoreCase("reset")) {
                     var293.reset();
                  } else {
                     try {
                        RangeNumberSetting var296 = var293;
                        String var297 = var82[0];
                        var296.parseRange(var297);
                     } catch (NumberFormatException var444) {
                        String var306 = String.valueOf(ChatColorUtil.a());
                        String var307 = String.valueOf(ChatColorUtil.b());
                        String var308 = this.a.getName();
                        Night.chatManager
                           .sendReplaceableTaggedMessage("Please input a range like " + var306 + "7-9" + var307 + ".", var308, "module-cmd-" + this.getName());
                        return;
                     }
                  }

                  String var298 = String.valueOf(ChatColorUtil.a());
                  String var299 = var293.getName();
                  String var300 = String.valueOf(ChatColorUtil.b());
                  String var301 = String.valueOf(ChatColorUtil.a());
                  String var302 = var293.getRangeString();
                  String var303 = String.valueOf(ChatColorUtil.b());
                  String var304 = this.a.getName();
                  Night.chatManager
                     .sendReplaceableTaggedMessage(
                        "Successfully set " + var298 + var299 + var300 + " to " + var301 + var302 + var303 + ".", var304, "module-cmd-" + this.getName()
                     );
               } else {
                  String var294 = this.a.getName().toLowerCase();
                  String var295 = var293.getName().toLowerCase();
                  Night.chatManager.sendInfo(var294 + " " + var295 + " <[lo-hi]|reset>");
               }
               break;
            case NumberSetting var171:
               if (var82.length == 1) {
                  if (var82[0].equalsIgnoreCase("reset")) {
                     var171.reset();
                     String var172 = String.valueOf(ChatColorUtil.a());
                     String var173 = var171.getName();
                     String var174 = String.valueOf(ChatColorUtil.b());
                     String var175 = this.a.getName();
                     Night.chatManager
                        .sendReplaceableTaggedMessage(
                           "Successfully reset the " + var172 + var173 + var174 + " setting.", var175, "module-cmd-" + this.getName()
                        );
                  } else {
                     try {
                        switch (var171.getNumberType()) {
                           case LONG:
                              NumberSetting var178 = var171;
                              Long var179 = Long.parseLong(var82[0]);
                              var178.setValue(var179);
                              break;
                           case DOUBLE:
                              NumberSetting var180 = var171;
                              Double var181 = Double.parseDouble(var82[0]);
                              var180.setValue(var181);
                              break;
                           case FLOAT:
                              NumberSetting var182 = var171;
                              Float var183 = Float.parseFloat(var82[0]);
                              var182.setValue(var183);
                              break;
                           default:
                              NumberSetting var184 = var171;
                              Integer var185 = Integer.parseInt(var82[0]);
                              var184.setValue(var185);
                        }

                        String var186 = String.valueOf(ChatColorUtil.a());
                        String var187 = var171.getName();
                        String var188 = String.valueOf(ChatColorUtil.b());
                        String var189 = String.valueOf(ChatColorUtil.a());
                        String var190 = String.valueOf(var171.getValue());
                        String var191 = String.valueOf(ChatColorUtil.b());
                        String var192 = this.a.getName();
                        Night.chatManager
                           .sendReplaceableTaggedMessage(
                              "Successfully set " + var186 + var187 + var188 + " to " + var189 + var190 + var191 + ".", var192, "module-cmd-" + this.getName()
                           );
                     } catch (NumberFormatException var445) {
                        String var194 = String.valueOf(ChatColorUtil.a());
                        String var195 = var171.getNumberType().name().toLowerCase();
                        String var196 = String.valueOf(ChatColorUtil.b());
                        String var197 = this.a.getName();
                        Night.chatManager
                           .sendReplaceableTaggedMessage(
                              "Please input a valid " + var194 + var195 + var196 + " number.", var197, "module-cmd-" + this.getName()
                           );
                     }
                  }
               } else {
                  String var176 = this.a.getName().toLowerCase();
                  String var177 = var171.getName().toLowerCase();
                  Night.chatManager.sendInfo(var176 + " " + var177 + " <[value]|reset>");
               }
               break;
            case ModeSetting var198:
               if (var82.length >= 1) {
                  if ((!var82[0].equalsIgnoreCase("reset") || !var198.getModes().stream().noneMatch("reset"::equalsIgnoreCase))
                     && !var82[0].equalsIgnoreCase("force-reset")) {
                     if ((!var82[0].equalsIgnoreCase("list") || !var198.getModes().stream().noneMatch("list"::equalsIgnoreCase))
                        && !var82[0].equalsIgnoreCase("force-list")) {
                        if (var82[0].equalsIgnoreCase("reset") && var198.getModes().stream().anyMatch("reset"::equalsIgnoreCase)) {
                           String var230 = String.valueOf(ChatColorUtil.a());
                           String var231 = String.valueOf(ChatColorUtil.b());
                           Night.chatManager
                              .sendInfo("If you would like to reset this setting's value, write \"" + var230 + "force-reset" + var231 + "\" instead.");
                        }

                        if (var82[0].equalsIgnoreCase("list") && var198.getModes().stream().anyMatch("list"::equalsIgnoreCase)) {
                           String var232 = String.valueOf(ChatColorUtil.a());
                           String var233 = String.valueOf(ChatColorUtil.b());
                           Night.chatManager
                              .sendInfo(
                                 "If you would like to view a list of valid values for this setting, write \"" + var232 + "force-list" + var233 + "\" instead."
                              );
                        }

                        StringBuilder var234 = new StringBuilder();
                        byte var235 = 0;
                        String[] var236 = var82;
                        int var237 = var236.length;
                        byte var238 = 0;

                        while (true) {
                           byte var239 = var238;
                           int var240 = var237;
                           if (var239 >= var240) {
                              Stream<String> var250 = var198.getModes().stream();
                              String var251 = var234.toString();
                              if (var250.anyMatch(var251::equalsIgnoreCase)) {
                                 ModeSetting var252 = var198;
                                 List var253 = var198.getModes();
                                 List var254 = var198.getModes();
                                 String var255 = var234.toString();
                                 int var256 = ShulkerSearchUtil.a(var254, var255);
                                 String var257 = (String)var253.get(var256);
                                 var252.setValue(var257);
                                 String var258 = String.valueOf(ChatColorUtil.a());
                                 String var259 = var198.getName();
                                 String var260 = String.valueOf(ChatColorUtil.b());
                                 String var261 = String.valueOf(ChatColorUtil.a());
                                 String var262 = var198.getValue();
                                 String var263 = String.valueOf(ChatColorUtil.b());
                                 String var264 = this.a.getName();
                                 Night.chatManager
                                    .sendReplaceableTaggedMessage(
                                       "Successfully set " + var258 + var259 + var260 + " to " + var261 + var262 + var263 + ".",
                                       var264,
                                       "module-cmd-" + this.getName()
                                    );
                              } else {
                                 String var265 = this.a.getName();
                                 Night.chatManager
                                    .sendReplaceableTaggedMessage("Please input a valid value for this setting.", var265, "module-cmd-" + this.getName());
                              }

                              return;
                           }

                           String[] var241 = var236;
                           byte var242 = var238;
                           String var243 = var241[var242];
                           StringBuilder var244 = var234;
                           String var245 = var243;
                           StringBuilder var246 = var244.append(var245);
                           int var247 = var235 + 1;
                           int var248 = var82.length;
                           String var249;
                           if (var247 == var248) {
                              var249 = "";
                           } else {
                              var249 = " ";
                           }

                           var246.append(var249);
                           var235 += 1;
                           var238 += 1;
                        }
                     } else if (var198.getModes().isEmpty()) {
                        String var205 = String.valueOf(ChatColorUtil.a());
                        String var206 = var198.getName();
                        String var207 = String.valueOf(ChatColorUtil.b());
                        String var208 = this.a.getName();
                        Night.chatManager
                           .sendReplaceableTaggedMessage(
                              var205 + var206 + var207 + " currently has no values registered.", var208, "module-cmd-" + this.getName()
                           );
                     } else {
                        StringBuilder var209 = new StringBuilder();
                        byte var210 = 0;

                        for (String var212 : (Iterable<String>)(Object)var198.getModes()) {
                           StringBuilder var213 = var209;
                           Object var214 = ChatColorUtil.b();
                           StringBuilder var215 = var213.append(var214);
                           String var216 = var212;
                           StringBuilder var217 = var215.append(var216);
                           int var218 = var210 + 1;
                           int var219 = var198.getModes().size();
                           String var220;
                           if (var218 == var219) {
                              var220 = "";
                           } else {
                              var220 = ", ";
                           }

                           var217.append(var220);
                           var210 += 1;
                        }

                        String var221 = String.valueOf(ChatColorUtil.b());
                        String var222 = var198.getName();
                        String var223 = String.valueOf(ChatColorUtil.a());
                        String var224 = String.valueOf(ChatColorUtil.b());
                        int var225 = var198.getModes().size();
                        String var226 = String.valueOf(ChatColorUtil.a());
                        String var227 = String.valueOf(ChatColorUtil.b());
                        String var228 = String.valueOf(var209);
                        String var229 = this.a.getName();
                        Night.chatManager
                           .sendReplaceableTaggedMessage(
                              var221 + var222 + " " + var223 + "[" + var224 + var225 + var226 + "]: " + var227 + var228, var229, "module-cmd-" + this.getName()
                           );
                     }
                  } else {
                     var198.reset();
                     String var199 = String.valueOf(ChatColorUtil.a());
                     String var200 = var198.getName();
                     String var201 = String.valueOf(ChatColorUtil.b());
                     String var202 = this.a.getName();
                     Night.chatManager
                        .sendReplaceableTaggedMessage(
                           "Successfully reset the " + var199 + var200 + var201 + " setting.", var202, "module-cmd-" + this.getName()
                        );
                  }
               } else {
                  String var203 = this.a.getName().toLowerCase();
                  String var204 = var198.getName().toLowerCase();
                  Night.chatManager.sendInfo(var203 + " " + var204 + " <[value]|reset|list>");
               }
               break;
            case StringSetting var309:
               if (var82.length >= 1) {
                  if (var82[0].equalsIgnoreCase("force-reset")) {
                     StringSetting var310 = var309;
                     String var311 = var309.getDefaultValue();
                     var310.setValue(var311);
                     String var312 = String.valueOf(ChatColorUtil.a());
                     String var313 = var309.getName();
                     String var314 = String.valueOf(ChatColorUtil.b());
                     String var315 = this.a.getName();
                     Night.chatManager
                        .sendReplaceableTaggedMessage(
                           "Successfully reset the " + var312 + var313 + var314 + " setting.", var315, "module-cmd-" + this.getName()
                        );
                  } else {
                     if (var82[0].equalsIgnoreCase("reset")) {
                        String var318 = String.valueOf(ChatColorUtil.a());
                        String var319 = String.valueOf(ChatColorUtil.b());
                        Night.chatManager
                           .sendInfo("If you would like to reset this setting's value, write \"" + var318 + "force-reset" + var319 + "\" instead.");
                     }

                     StringBuilder var320 = new StringBuilder();
                     byte var321 = 0;
                     String[] var322 = var82;
                     int var323 = var322.length;
                     byte var324 = 0;

                     while (true) {
                        byte var325 = var324;
                        int var326 = var323;
                        if (var325 >= var326) {
                           StringSetting var336 = var309;
                           String var337 = var320.toString();
                           var336.setValue(var337);
                           String var338 = String.valueOf(ChatColorUtil.a());
                           String var339 = var309.getName();
                           String var340 = String.valueOf(ChatColorUtil.b());
                           String var341 = String.valueOf(ChatColorUtil.a());
                           String var342 = var309.getValue();
                           String var343 = String.valueOf(ChatColorUtil.b());
                           String var344 = this.a.getName();
                           Night.chatManager
                              .sendReplaceableTaggedMessage(
                                 "Successfully set " + var338 + var339 + var340 + " to " + var341 + var342 + var343 + ".",
                                 var344,
                                 "module-cmd-" + this.getName()
                              );
                           return;
                        }

                        String[] var327 = var322;
                        byte var328 = var324;
                        String var329 = var327[var328];
                        StringBuilder var330 = var320;
                        String var331 = var329;
                        StringBuilder var332 = var330.append(var331);
                        int var333 = var321 + 1;
                        int var334 = var82.length;
                        String var335;
                        if (var333 == var334) {
                           var335 = "";
                        } else {
                           var335 = " ";
                        }

                        var332.append(var335);
                        var321 += 1;
                        var324 += 1;
                     }
                  }
               } else {
                  String var316 = this.a.getName().toLowerCase();
                  String var317 = var309.getName().toLowerCase();
                  Night.chatManager.sendInfo(var316 + " " + var317 + " <[value]|reset>");
               }
               break;
            case KeybindSetting var84:
               Object var85 = var79;
               KeybindSetting var86 = this.a.bind;
               if (var85 == var86) {
                  String var87 = String.valueOf(ChatColorUtil.a());
                  String var88 = String.valueOf(ChatColorUtil.b());
                  String var89 = this.a.getName();
                  Night.chatManager
                     .sendReplaceableTaggedMessage(
                        "This setting is the module's main toggle keybind. Please use the " + var87 + "bind" + var88 + " command instead.",
                        var89,
                        "module-cmd-" + this.getName()
                     );
                  return;
               }

               if (var82.length == 1) {
                  if (var82[0].equalsIgnoreCase("reset")) {
                     var84.reset();
                     String var90 = String.valueOf(ChatColorUtil.a());
                     String var91 = var84.getName();
                     String var92 = String.valueOf(ChatColorUtil.b());
                     String var93 = this.a.getName();
                     Night.chatManager
                        .sendReplaceableTaggedMessage("Successfully reset the " + var90 + var91 + var92 + " setting.", var93, "module-cmd-" + this.getName());
                  } else {
                     int var96 = 0;

                     try {
                        var96 = KeyNameUtil.a_int_String(var82[0]);
                     } catch (IllegalArgumentException var448) {
                     }

                     KeybindSetting var98 = var84;
                     int var99 = var96;
                     var98.setKey(var99);
                     String var100 = String.valueOf(ChatColorUtil.a());
                     String var101 = var84.getName();
                     String var102 = String.valueOf(ChatColorUtil.b());
                     String var103 = String.valueOf(ChatColorUtil.a());
                     String var104 = KeyNameUtil.a_String_int(var84.getKey()).toUpperCase();
                     String var105 = String.valueOf(ChatColorUtil.b());
                     String var106 = this.a.getName();
                     Night.chatManager
                        .sendReplaceableTaggedMessage(
                           "Successfully set " + var100 + var101 + var102 + " to " + var103 + var104 + var105 + ".", var106, "module-cmd-" + this.getName()
                        );
                  }
               } else {
                  String var94 = this.a.getName().toLowerCase();
                  String var95 = var84.getName().toLowerCase();
                  Night.chatManager.sendInfo(var94 + " " + var95 + " <[value]|reset>");
               }
               break;
            case ColorSetting var107:
               if (var82.length == 2) {
                  int var108 = 0;
                  if (var82[0].equalsIgnoreCase("red")
                     || var82[0].equalsIgnoreCase("green")
                     || var82[0].equalsIgnoreCase("blue")
                     || var82[0].equalsIgnoreCase("alpha")) {
                     try {
                        var108 = Math.clamp(Integer.parseInt(var82[1]), 0, 255);
                     } catch (NumberFormatException var447) {
                        String var110 = String.valueOf(ChatColorUtil.a());
                        String var111 = var82[0].toLowerCase();
                        String var112 = String.valueOf(ChatColorUtil.b());
                        String var113 = this.a.getName();
                        Night.chatManager.sendTaggedMessage("Please input a valid number for the " + var110 + var111 + var112 + " value.", var113);
                        return;
                     }
                  }

                  String var122 = "";
                  String var123 = "";
                  if (var82[0].equalsIgnoreCase("red")) {
                     ColorSetting var124 = var107;
                     int var125 = var108;
                     int var126 = var107.getColorValue().getColor().getGreen();
                     int var127 = var107.getColorValue().getColor().getBlue();
                     int var128 = var107.getColorValue().getColor().getAlpha();
                     var124.setColor(new Color(var125, var126, var127, var128));
                     var122 = "red";
                     var123 = String.valueOf(var107.getColorValue().getColor().getRed());
                  } else if (var82[0].equalsIgnoreCase("green")) {
                     ColorSetting var129 = var107;
                     int var130 = var107.getColorValue().getColor().getRed();
                     int var131 = var108;
                     int var132 = var107.getColorValue().getColor().getBlue();
                     int var133 = var107.getColorValue().getColor().getAlpha();
                     var129.setColor(new Color(var130, var131, var132, var133));
                     var122 = "green";
                     var123 = String.valueOf(var107.getColorValue().getColor().getGreen());
                  } else if (var82[0].equalsIgnoreCase("blue")) {
                     ColorSetting var134 = var107;
                     int var135 = var107.getColorValue().getColor().getRed();
                     int var136 = var107.getColorValue().getColor().getGreen();
                     int var137 = var108;
                     int var138 = var107.getColorValue().getColor().getAlpha();
                     var134.setColor(new Color(var135, var136, var137, var138));
                     var122 = "blue";
                     var123 = String.valueOf(var107.getColorValue().getColor().getBlue());
                  } else if (var82[0].equalsIgnoreCase("alpha")) {
                     ColorSetting var139 = var107;
                     int var140 = var107.getColorValue().getColor().getRed();
                     int var141 = var107.getColorValue().getColor().getGreen();
                     int var142 = var107.getColorValue().getColor().getBlue();
                     int var143 = var108;
                     var139.setColor(new Color(var140, var141, var142, var143));
                     var122 = "alpha";
                     var123 = String.valueOf(var107.getColorValue().getColor().getAlpha());
                  } else if (var82[0].equalsIgnoreCase("sync")) {
                     ColorSetting var144 = var107;
                     boolean var145 = Boolean.parseBoolean(var82[1]);
                     var144.setSync(var145);
                     var122 = "sync";
                     var123 = String.valueOf(var107.isSync());
                  } else if (var82[0].equalsIgnoreCase("rainbow")) {
                     ColorSetting var146 = var107;
                     boolean var147 = Boolean.parseBoolean(var82[1]);
                     var146.setRainbow(var147);
                     var122 = "rainbow";
                     var123 = String.valueOf(var107.isRainbow());
                  } else if (var82[0].equalsIgnoreCase("code")) {
                     if (!ColorUtil.b_bool_String(var82[1])) {
                        String var148 = this.a.getName();
                        Night.chatManager.sendReplaceableTaggedMessage("Please input a valid color code.", var148, "module-cmd-" + this.getName());
                        return;
                     }

                     try {
                        String var149;
                        if (var82[1].startsWith("#")) {
                           var149 = "";
                        } else {
                           var149 = "#";
                        }

                        String var150 = var82[1];
                        Color var151 = Color.decode(var149 + var150);
                        ColorSetting var152 = var107;
                        int var153 = var151.getRed();
                        int var154 = var151.getGreen();
                        int var155 = var151.getBlue();
                        var152.setColor(new Color(var153, var154, var155));
                     } catch (NumberFormatException var446) {
                        String var157 = this.a.getName();
                        Night.chatManager.sendReplaceableTaggedMessage("Please input a valid color code.", var157, "module-cmd-" + this.getName());
                        return;
                     }

                     var122 = "color";
                     int var158 = var107.getColorValue().getColor().getRed();
                     int var159 = var107.getColorValue().getColor().getGreen();
                     int var160 = var107.getColorValue().getColor().getBlue();
                     int var161 = var107.getColorValue().getColor().getAlpha();
                     var123 = "rgba(" + var158 + ", " + var159 + ", " + var160 + ", " + var161 + ")";
                  } else {
                     String var162 = this.a.getName().toLowerCase();
                     String var163 = var107.getName().toLowerCase();
                     Night.chatManager.sendInfo(var162 + " " + var163 + " <red|green|blue|alpha|sync|rainbow|code> <[input]> | <reset>");
                  }

                  String var164 = String.valueOf(ChatColorUtil.a());
                  String var165 = var122;
                  String var166 = String.valueOf(ChatColorUtil.b());
                  String var167 = String.valueOf(ChatColorUtil.a());
                  String var168 = var123;
                  String var169 = String.valueOf(ChatColorUtil.b());
                  String var170 = this.a.getName();
                  Night.chatManager
                     .sendReplaceableTaggedMessage(
                        "Successfully set the " + var164 + var165 + var166 + " value to " + var167 + var168 + var169 + ".",
                        var170,
                        "module-cmd-" + this.getName()
                     );
               } else if (var82.length == 1) {
                  if (var82[0].equalsIgnoreCase("reset")) {
                     var107.reset();
                     String var114 = String.valueOf(ChatColorUtil.a());
                     String var115 = var107.getName();
                     String var116 = String.valueOf(ChatColorUtil.b());
                     String var117 = this.a.getName();
                     Night.chatManager
                        .sendReplaceableTaggedMessage(
                           "Successfully reset the " + var114 + var115 + var116 + " setting.", var117, "module-cmd-" + this.getName()
                        );
                  } else {
                     String var118 = this.a.getName().toLowerCase();
                     String var119 = var107.getName().toLowerCase();
                     Night.chatManager.sendInfo(var118 + " " + var119 + " <red|green|blue|alpha|sync|rainbow|code> <[input]> | <reset>");
                  }
               } else {
                  String var120 = this.a.getName().toLowerCase();
                  String var121 = var107.getName().toLowerCase();
                  Night.chatManager.sendInfo(var120 + " " + var121 + " <red|green|blue|alpha|sync|rainbow|code> <[input]> | <reset>");
               }
               break;
            case WhitelistSetting var345:
               if (var82.length == 2) {
                  if (var82[0].equalsIgnoreCase("add")) {
                     if (var345.getType() == WhitelistType.ITEMS) {
                        Item var346 = RegistryLookupUtil.a(var82[1]);
                        if (var346 == null) {
                           String var347 = this.a.getName();
                           Night.chatManager.sendReplaceableTaggedMessage("Please input a valid item ID.", var347, "module-cmd-" + this.getName());
                           return;
                        }

                        WhitelistSetting var348 = var345;
                        Item var349 = var346;
                        if (var348.contains(var349)) {
                           String var350 = String.valueOf(ChatColorUtil.a());
                           Item var351 = var346;
                           ItemStack var352 = var346.getDefaultInstance();
                           String var353 = var351.getName(var352).getString();
                           String var354 = String.valueOf(ChatColorUtil.b());
                           String var355 = this.a.getName();
                           Night.chatManager
                              .sendReplaceableTaggedMessage(var350 + var353 + var354 + " is already on the whitelist.", var355, "module-cmd-" + this.getName());
                        } else {
                           WhitelistSetting var356 = var345;
                           Item var357 = var346;
                           var356.add(var357);
                           String var358 = String.valueOf(ChatColorUtil.a());
                           Item var359 = var346;
                           ItemStack var360 = var346.getDefaultInstance();
                           String var361 = var359.getName(var360).getString();
                           String var362 = String.valueOf(ChatColorUtil.b());
                           String var363 = this.a.getName();
                           Night.chatManager
                              .sendReplaceableTaggedMessage(
                                 "Successfully added " + var358 + var361 + var362 + " to the whitelist.", var363, "module-cmd-" + this.getName()
                              );
                        }
                     } else if (var345.getType() == WhitelistType.BLOCKS) {
                        Block var364 = RegistryLookupUtil.b(var82[1]);
                        if (var364 == null) {
                           String var365 = this.a.getName();
                           Night.chatManager.sendReplaceableTaggedMessage("Please input a valid block ID.", var365, "module-cmd-" + this.getName());
                           return;
                        }

                        WhitelistSetting var366 = var345;
                        Block var367 = var364;
                        if (var366.contains(var367)) {
                           String var368 = String.valueOf(ChatColorUtil.a());
                           String var369 = var364.getName().getString();
                           String var370 = String.valueOf(ChatColorUtil.b());
                           String var371 = this.a.getName();
                           Night.chatManager
                              .sendReplaceableTaggedMessage(var368 + var369 + var370 + " is already on the whitelist.", var371, "module-cmd-" + this.getName());
                        } else {
                           WhitelistSetting var372 = var345;
                           Block var373 = var364;
                           var372.add(var373);
                           String var374 = String.valueOf(ChatColorUtil.a());
                           String var375 = var364.getName().getString();
                           String var376 = String.valueOf(ChatColorUtil.b());
                           String var377 = this.a.getName();
                           Night.chatManager
                              .sendReplaceableTaggedMessage(
                                 "Successfully added " + var374 + var375 + var376 + " to the whitelist.", var377, "module-cmd-" + this.getName()
                              );
                        }
                     } else {
                        Night.chatManager.sendError("Something went wrong while detecting the setting's type.");
                     }
                  } else if (var82[0].equalsIgnoreCase("del")) {
                     if (var345.getType() == WhitelistType.ITEMS) {
                        Item var378 = RegistryLookupUtil.a(var82[1]);
                        if (var378 != null) {
                           WhitelistSetting var379 = var345;
                           Item var380 = var378;
                           if (var379.contains(var380)) {
                              WhitelistSetting var381 = var345;
                              Item var382 = var378;
                              var381.remove(var382);
                              String var383 = String.valueOf(ChatColorUtil.a());
                              Item var384 = var378;
                              ItemStack var385 = var378.getDefaultInstance();
                              String var386 = var384.getName(var385).getString();
                              String var387 = String.valueOf(ChatColorUtil.b());
                              String var388 = this.a.getName();
                              Night.chatManager
                                 .sendReplaceableTaggedMessage(
                                    "Successfully removed " + var383 + var386 + var387 + " from the whitelist.", var388, "module-cmd-" + this.getName()
                                 );
                              break;
                           }
                        }
                     }

                     if (var345.getType() == WhitelistType.BLOCKS) {
                        Block var391 = RegistryLookupUtil.b(var82[1]);
                        if (var391 != null) {
                           WhitelistSetting var392 = var345;
                           Block var393 = var391;
                           if (var392.contains(var393)) {
                              WhitelistSetting var394 = var345;
                              Block var395 = var391;
                              var394.remove(var395);
                              String var396 = String.valueOf(ChatColorUtil.a());
                              String var397 = var391.getName().getString();
                              String var398 = String.valueOf(ChatColorUtil.b());
                              String var399 = this.a.getName();
                              Night.chatManager
                                 .sendReplaceableTaggedMessage(
                                    "Successfully removed " + var396 + var397 + var398 + " from the whitelist.", var399, "module-cmd-" + this.getName()
                                 );
                              break;
                           }
                        }
                     }

                     String var400 = String.valueOf(ChatColorUtil.a());
                     String var401 = var82[1];
                     String var402 = String.valueOf(ChatColorUtil.b());
                     String var403 = this.a.getName();
                     Night.chatManager
                        .sendReplaceableTaggedMessage(var400 + var401 + var402 + " is not on the whitelist.", var403, "module-cmd-" + this.getName());
                  } else {
                     String var389 = this.a.getName().toLowerCase();
                     String var390 = var345.getName().toLowerCase();
                     Night.chatManager.sendInfo(var389 + " " + var390 + " <add|del> <[id]> | <list|clear>");
                  }
               } else if (var82.length == 1) {
                  if (var82[0].equalsIgnoreCase("clear")) {
                     var345.getEntries().clear();
                     String var404 = String.valueOf(ChatColorUtil.a());
                     String var405 = var345.getName();
                     String var406 = String.valueOf(ChatColorUtil.b());
                     String var407 = this.a.getName();
                     Night.chatManager
                        .sendReplaceableTaggedMessage(
                           "Successfully cleared the " + var404 + var405 + var406 + " whitelist.", var407, "module-cmd-" + this.getName()
                        );
                  } else if (var82[0].equalsIgnoreCase("list")) {
                     if (var345.getEntries().isEmpty()) {
                        ChatManager var410 = Night.chatManager;
                        String var411;
                        if (var345.getType() == WhitelistType.ITEMS) {
                           var411 = "items";
                        } else {
                           var411 = "blocks";
                        }

                        String var412 = this.a.getName();
                        var410.sendReplaceableTaggedMessage("There are currently no " + var411 + " on the whitelist.", var412, "module-cmd-" + this.getName());
                     } else {
                        StringBuilder var415 = new StringBuilder();
                        byte var416 = 0;

                        for (Object var418 : (Iterable<Object>)(Object)var345.getEntries()) {
                           StringBuilder var419 = var415;
                           Object var420 = ChatColorUtil.b();
                           var419.append(var420);
                           switch (var418) {
                              case Block var427:
                                 StringBuilder var428 = var415;
                                 String var429 = var427.getName().getString();
                                 var428.append(var429);
                                 break;
                              case Item var422:
                                 StringBuilder var423 = var415;
                                 Item var424 = var422;
                                 ItemStack var425 = var422.getDefaultInstance();
                                 String var426 = var424.getName(var425).getString();
                                 var423.append(var426);
                                 break;
                              default:
                                 var415.append("Invalid");
                           }

                           StringBuilder var430 = var415;
                           Object var431 = ChatColorUtil.a();
                           StringBuilder var432 = var430.append(var431);
                           int var433 = var416 + 1;
                           int var434 = var345.getEntries().size();
                           String var435;
                           if (var433 == var434) {
                              var435 = "";
                           } else {
                              var435 = ", ";
                           }

                           var432.append(var435);
                           var416 += 1;
                        }

                        String var436 = String.valueOf(ChatColorUtil.b());
                        String var437 = var345.getName();
                        String var438 = String.valueOf(ChatColorUtil.a());
                        String var439 = String.valueOf(ChatColorUtil.b());
                        int var440 = var345.getEntries().size();
                        String var441 = String.valueOf(ChatColorUtil.a());
                        String var442 = String.valueOf(ChatColorUtil.b());
                        String var443 = String.valueOf(var415);
                        Night.chatManager
                           .sendReplaceableMessage(
                              var436 + var437 + " " + var438 + "[" + var439 + var440 + var441 + "]: " + var442 + var443, "module-cmd-" + this.getName()
                           );
                     }
                  } else {
                     String var413 = this.a.getName().toLowerCase();
                     String var414 = var345.getName().toLowerCase();
                     Night.chatManager.sendInfo(var413 + " " + var414 + " <add|del> <[id]> | <clear|list>");
                  }
               } else {
                  String var408 = this.a.getName().toLowerCase();
                  String var409 = var345.getName().toLowerCase();
                  Night.chatManager.sendInfo(var408 + " " + var409 + " <add|del> <[id]> | <clear|list>");
               }
               break;
            default:
               this.sendSyntax();
         }
      }
   }
}
