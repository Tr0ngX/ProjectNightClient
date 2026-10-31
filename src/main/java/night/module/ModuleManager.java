package night.module;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Generated;
import night.Night;
import night.events.EventTarget;
import night.events.impl.KeyInputEvent;
import night.events.impl.MouseButtonEvent;
import night.events.impl.TickEvent;
import night.gui.component.CategoryFrame;
import night.gui.component.impl.ModuleComponent;
import night.module.impl.core.HudModule;
import night.module.impl.core.PingBypassModule;
import night.module.impl.kingmc.AutoShop;
import night.security.ClasspathScanner;
import night.security.EncryptedModuleBootstrap;
import night.setting.Setting;
import night.setting.impl.BooleanSetting;
import night.setting.impl.CategorySetting;
import night.setting.impl.KeybindSetting;
import night.setting.impl.PositionSetting;
import night.setting.impl.SubBindSetting;
import night.util.MinecraftInstance;
import night.util.input.KeyNameUtil;

public class ModuleManager implements MinecraftInstance {
   private final List<Module> modules = new ArrayList<>();
   private final Map<Class<? extends Module>, Module> modulesByClass = new Reference2ReferenceOpenHashMap();

   public ModuleManager() {
      Night.EVENT_BUS.register(this);

      try {
         for (Class var2 : (Iterable<Class>)(Object)ClasspathScanner.a_List_Class_Class(Module.class, Night.class)) {
            this.instantiateModule(var2);
         }
      } catch (Exception var8) {
         Night.LOGGER.error("Failed to register the client's modules!", var8);
      }

      try {
         for (Class var5 : (Iterable<Class>)(Object)EncryptedModuleBootstrap.a(ModuleManager.class.getClassLoader())) {
            this.instantiateModule(var5);
         }
      } catch (Exception var7) {
         Night.LOGGER.error("Failed to register encrypted modules!", var7);
      }

      this.modules.sort(Comparator.comparing(Module::getName));
   }

   private void instantiateModule(Class<?> var1) {
      try {
         if (var1.getAnnotation(ModuleInfo.class) == null) {
            return;
         }

         Class[] var2 = new Class[0];
         Constructor var3 = var1.getDeclaredConstructor(var2);
         Object[] var4 = new Object[0];
         Module var5 = (Module)var3.newInstance(var4);
         this.registerModule(var5);
      } catch (Throwable var7) {
         Night.LOGGER.error("Failed to register module " + var1.getName(), var7);
      }
   }

   public void registerModule(Module var1) {
      if (var1 != null) {
         try {
            Set var2 = Collections.newSetFromMap(new IdentityHashMap());
            Field[] var3 = var1.getClass().getDeclaredFields();
            int var4 = var3.length;
            int var5 = 0;

            label89:
            while (true) {
               int var6 = var5;
               int var7 = var4;
               if (var6 >= var7) {
                  Field[] var16 = var1.getClass().getDeclaredFields();
                  int var17 = var16.length;
                  int var18 = 0;

                  while (true) {
                     int var19 = var18;
                     int var20 = var17;
                     if (var19 >= var20) {
                        break label89;
                     }

                     Field[] var21 = var16;
                     int var22 = var18;
                     Field var23 = var21[var22];
                     Class var24 = var23.getType();
                     if (Setting.class.isAssignableFrom(var24)) {
                        if (!var23.canAccess(var1)) {
                           var23.setAccessible(true);
                        }

                        Setting var25 = (Setting)var23.get(var1);
                        if (var25 != null) {
                           Set var26 = var2;
                           Setting var27 = var25;
                           label76:
                           if (!var26.contains(var27)) {
                              if (var1 instanceof HudModule var28 && !HudModule.v()) {
                                 Setting var29 = var25;
                                 CategorySetting var30 = var28.musicHUDCategory;
                                 if (var29 == var30) {
                                    break label76;
                                 }

                                 Setting var31 = var25;
                                 PositionSetting var32 = var28.musicHUDPosition;
                                 if (var31 == var32) {
                                    break label76;
                                 }

                                 Setting.VisibilityCondition var33 = var25.getVisibility();
                                 if (var33 instanceof CategorySetting.CategoryCondition) {
                                    CategorySetting var34 = ((CategorySetting.CategoryCondition)var33).d();
                                    CategorySetting var35 = var28.musicHUDCategory;
                                    if (var34 == var35) {
                                       break label76;
                                    }
                                 }
                              }

                              List var36 = var1.getSettings();
                              Setting var37 = var25;
                              var36.add(var37);
                           }
                        }
                     }

                     var18++;
                  }
               }

               Field[] var8 = var3;
               int var9 = var5;
               Field var10 = var8[var9];
               Class var11 = var10.getType();
               if (SubBindSetting.class.isAssignableFrom(var11)) {
                  if (!var10.canAccess(var1)) {
                     var10.setAccessible(true);
                  }

                  if (var10.get(var1) instanceof SubBindSetting var13) {
                     Set var14 = var2;
                     BooleanSetting var15 = var13.getTargetSetting();
                     var14.add(var15);
                  }
               }

               var5++;
            }
         } catch (IllegalAccessException var54) {
            Night.LOGGER.error("Failed to register settings for module " + var1.getName(), var54);
         }

         List var39 = var1.getSettings();
         BooleanSetting var40 = var1.chatNotify;
         var39.add(var40);
         List var41 = var1.getSettings();
         BooleanSetting var42 = var1.drawn;
         var41.add(var42);
         if (!var1.isPersistent() && !(var1 instanceof AutoShop)) {
            List var43 = var1.getSettings();
            KeybindSetting var44 = var1.bind;
            var43.add(var44);
         }

         if (!this.modules.contains(var1)) {
            this.modules.add(var1);
            Map var45 = this.modulesByClass;
            Class var46 = var1.getClass();
            var45.put(var46, var1);
            this.modules.sort(Comparator.comparing(Module::getName));
            if (Night.clickGui != null) {
               for (CategoryFrame var48 : (Iterable<CategoryFrame>)(Object)Night.clickGui.d()) {
                  Module.Category var49 = var48.getCategory();
                  Module.Category var50 = var1.getCategory();
                  if (var49 == var50 && !(var1 instanceof PingBypassModule)) {
                     ArrayList var51 = var48.getComponents();
                     CategoryFrame var52 = var48;
                     int var53 = var48.getHeaderHeight();
                     var51.add(new ModuleComponent(var1, var52, var53));
                      var48.sortComponents();
                  }
               }
            }
         }
      }
   }

   @EventTarget
   public void onKeyInput(KeyInputEvent var1) {
      if (mc.gui == null || mc.gui.screen() == null) {
         this.modules.stream().filter(var1x -> {
            int var2x = var1x.getKeybind();
            int var3x = var1.getKey();
            boolean var4x;
            if (var2x == var3x && var1x.bind.getMode().equals("Bind")) {
               var4x = true;
            } else {
               var4x = false;
            }

            return var4x;
         }).forEach(var0 -> {
            boolean var1x;
            if (!var0.isEnabled()) {
               var1x = true;
            } else {
               var1x = false;
            }

            var0.setEnabled(var1x);
         });
         int var2 = var1.getKey();
         this.handleSubBinds(var2);
         AutoShop var3 = this.getModule(AutoShop.class);
         if (var3 != null) {
            AutoShop var4 = var3;
            int var5 = var1.getKey();
            var4.b_void_int(var5);
         }
      }
   }

   private void handleSubBinds(int var1) {
      if (var1 != 0) {
         for (Module var3 : (Iterable<Module>)(Object)this.modules) {
            if (var3.isEnabled() || var3.isPersistent()) {
               for (Setting var5 : var3.getSettings()) {
                  if (var5 instanceof SubBindSetting var6 && var6.getKey() == var1) {
                     var6.trigger();
                  }
               }
            }
         }
      }
   }

   @EventTarget
   public void onMouseButton(MouseButtonEvent var1) {
      if (mc.gui == null || mc.gui.screen() == null) {
         this.modules.stream().filter(var1x -> {
            int var2x = var1x.getKeybind();
            int var3x = -var1.getButton() - 1;
            boolean var4x;
            if (var2x == var3x && var1x.bind.getMode().equals("Bind")) {
               var4x = true;
            } else {
               var4x = false;
            }

            return var4x;
         }).forEach(var0 -> {
            boolean var1x;
            if (!var0.isEnabled()) {
               var1x = true;
            } else {
               var1x = false;
            }

            var0.setEnabled(var1x);
         });
         int var2 = -var1.getButton() - 1;
         this.handleSubBinds(var2);
         AutoShop var3 = this.getModule(AutoShop.class);
         if (var3 != null) {
            AutoShop var4 = var3;
            int var5 = -var1.getButton() - 1;
            var4.b_void_int(var5);
         }
      }
   }

   @EventTarget
   public void onTick(TickEvent var1) {
      if (mc.gui == null || mc.gui.screen() == null) {
         for (Module var3 : (Iterable<Module>)(Object)this.modules) {
            String var4 = var3.bind.getMode();
            if (!var4.equals("Bind") && var3.getKeybind() != 0) {
               boolean var5 = KeyNameUtil.b(var3.getKeybind());
               boolean var6 = var4.equals("Hold");
               boolean var7 = var5;
               boolean var8;
               if (var6 == var7) {
                  var8 = true;
               } else {
                  var8 = false;
               }

               boolean var9 = var8;
               boolean var10 = var3.isEnabled();
               boolean var11 = var9;
               if (var10 != var11) {
                  Module var12 = var3;
                  boolean var13 = var9;
                  var12.setEnabled(var13);
               }
            }
         }
      }
   }

   public List<Module> getModulesByCategory(Module.Category var1) {
      return this.modules.stream().filter(var1x -> {
         boolean var2;
         if (var1x.getCategory() == var1) {
            var2 = true;
         } else {
            var2 = false;
         }

         return var2;
      }).toList();
   }

   public Module getModuleByName(String var1) {
      return this.modules.stream().filter(var1x -> var1x.getName().equalsIgnoreCase(var1)).findFirst().orElse(null);
   }

   public <T extends Module> T getModule(Class<T> var1) {
      return (T)this.modulesByClass.get(var1);
   }

   @Generated
   public List<Module> getModules() {
      return this.modules;
   }
}
