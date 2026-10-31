package night.security;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.stream.Stream;

public final class ClasspathScanner {
   public static List<Class<?>> a_List_Class_Class(Class<?> var0, Class<?> var1) {
      ArrayList var2 = new ArrayList();
      ClassLoader var3 = var1.getClassLoader();
      URL var4;
      if (var1.getProtectionDomain().getCodeSource() != null) {
         var4 = var1.getProtectionDomain().getCodeSource().getLocation();
      } else {
         var4 = null;
      }

      URL var5 = var4;
      List var6 = new ArrayList();
      if (var5 != null) {
         try {
            File var8;
            try {
               URI var7 = var5.toURI();
               var8 = new File(var7);
            } catch (Throwable var38) {
               String var10 = var5.getPath();
               var8 = new File(var10);
            }

            if (var8.isDirectory()) {
               var6 = a_List_File(var8);
            }
         } catch (Throwable var39) {
         }

         if (var6.isEmpty()) {
            try (
               InputStream var12 = var5.openStream();
               JarInputStream var13 = new JarInputStream(var12);
            ) {
               while (true) {
                  JarEntry var14 = var13.getNextJarEntry();
                  if (var14 == null) {
                     break;
                  }

                  String var15 = var14.getName();
                  if (!var14.isDirectory() && var15.endsWith(".class") && !var15.contains("module-info")) {
                     List var16 = var6;
                     String var17 = a_String_String(var15);
                     var16.add(var17);
                  }
               }
            } catch (Throwable var43) {
            }
         }
      }

      for (String var28 : (Iterable<String>)(Object)var6) {
         Class var30;
         try {
            ClassLoader var29 = var3;
            var30 = Class.forName(var28, false, var29);
         } catch (Throwable var40) {
            continue;
         }

         if (var30 != var0) {
            Class var32 = var30;
            if (var0.isAssignableFrom(var32)) {
               int var33 = var30.getModifiers();
               if (!Modifier.isAbstract(var33) && !Modifier.isInterface(var33)) {
                  ArrayList var34 = var2;
                  Class var35 = var30;
                  var34.add(var35);
               }
            }
         }
      }

      return var2;
   }

   private static List<String> a_List_File(File var0) {
      Path var1 = var0.toPath();

      try {
         FileVisitOption[] var2 = new FileVisitOption[0];

         try (Stream<Path> var3 = Files.walk(var1, var2)) {
            return var3.filter(var0x -> {
               boolean var1x;
               if (var0x.toString().endsWith(".class") && !var0x.toString().contains("module-info")) {
                  var1x = true;
               } else {
                  var1x = false;
               }

               return var1x;
            }).map(var1x -> a_String_String(var1.relativize(var1x).toString().replace(File.separatorChar, (char)47))).toList();
         }
      } catch (Exception var12) {
         return List.of();
      }
   }

   private static String a_String_String(String var0) {
      int var1 = var0.length();
      int var2 = ".class".length();
      int var3 = var1 - var2;
      return var0.substring(0, var3).replace((char)47, (char)46);
   }
}
