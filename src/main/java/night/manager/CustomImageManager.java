package night.manager;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.NativeImage.Format;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.imageio.ImageIO;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import night.Night;
import night.util.render.CustomImageTexture;

public class CustomImageManager {
   public static final Path IMAGES_DIR;
   public static final Identifier EFFECT_CUSTOM_IMAGE_ID = Identifier.fromNamespaceAndPath("night", "textures/effect/custom_image.png");
   public static final Identifier CUSTOM_IMAGE_ID = Identifier.fromNamespaceAndPath("night", "textures/custom_image.png");
   private final Map<String, CustomImageManager.CachedImage> cachedImages = new ConcurrentHashMap<>();
   private String activeImageName = "None";

   public void registerTextures() {
      try {
         Minecraft.getInstance().getTextureManager().register(EFFECT_CUSTOM_IMAGE_ID, CustomImageTexture.a);
         Minecraft.getInstance().getTextureManager().register(CUSTOM_IMAGE_ID, CustomImageTexture.a);
      } catch (Throwable var2) {
      }
   }

   public CustomImageManager() {
      this.ensureDirectoryExists();
      this.registerTextures();
      String var1 = readSavedImageName();
      if (var1 != null && !var1.equalsIgnoreCase("None") && !var1.isEmpty()) {
         String var2 = var1;
         this.setActiveImage(var2);
      } else {
         this.f();
      }
   }

   public static String readSavedImageName() {
      try {
         String var0 = "default";
         String[] var1 = new String[]{"General.json"};
         Path var2 = Paths.get("Night", var1);
         LinkOption[] var3 = new LinkOption[0];
         if (Files.exists(var2, var3)) {
            Path var4 = var2;
            OpenOption[] var5 = new OpenOption[0];

            try (InputStream var6 = Files.newInputStream(var4, var5)) {
               JsonObject var7 = JsonParser.parseReader(new InputStreamReader(var6)).getAsJsonObject();
               if (var7.has("Config")) {
                  var0 = var7.get("Config").getAsString();
               }
            }
         }

         String[] var12 = new String[]{"Configs", var0 + ".json"};
         Path var13 = Paths.get("Night", var12);
         Path var14 = var13;
         LinkOption[] var15 = new LinkOption[0];
         if (!Files.exists(var14, var15)) {
            String[] var16 = new String[]{"Configs", "default.json"};
            var13 = Paths.get("Night", var16);
         }

         Path var17 = var13;
         LinkOption[] var18 = new LinkOption[0];
         if (Files.exists(var17, var18)) {
            Path var19 = var13;
            OpenOption[] var20 = new OpenOption[0];

            try (InputStream var21 = Files.newInputStream(var19, var20)) {
               JsonObject var22 = JsonParser.parseReader(new InputStreamReader(var21)).getAsJsonObject();
               if (var22.has("Shaders")) {
                  JsonObject var23 = var22.getAsJsonObject("Shaders");
                  if (var23.has("Settings")) {
                     JsonObject var24 = var23.getAsJsonObject("Settings");
                     if (var24.has("Image")) {
                        String var25 = var24.get("Image").getAsString();
                        if (var25 != null && !var25.isEmpty() && !var25.equalsIgnoreCase("None")) {
                           return var25;
                        }
                     }
                  }
               }
            }
         }
      } catch (Throwable var36) {
         String var31 = var36.getMessage();
         Night.LOGGER.debug("Could not pre-read saved image config: {}", var31);
      }

      return "None";
   }

   public void ensureDirectoryExists() {
      try {
         LinkOption[] var1 = new LinkOption[0];
         if (!Files.exists(IMAGES_DIR, var1)) {
            FileAttribute[] var2 = new FileAttribute[0];
            Files.createDirectories(IMAGES_DIR, var2);
         }
      } catch (Exception var4) {
         Night.LOGGER.error("Failed to create images directory", var4);
      }
   }

   public static NativeImage readNativeImage(File var0) throws Exception {
      BufferedImage var1 = ImageIO.read(var0);
      if (var1 == null) {
         throw new Exception("ImageIO failed to read image file: " + var0.getName());
      }

      int var2 = var1.getWidth();
      int var3 = var1.getHeight();
      NativeImage var4 = new NativeImage(Format.RGBA, var2, var3, false);
      int var5 = 0;

      while (true) {
         int var6 = var5;
         int var7 = var1.getHeight();
         if (var6 >= var7) {
            return var4;
         }

         int var8 = 0;

         while (true) {
            int var9 = var8;
            int var10 = var1.getWidth();
            if (var9 >= var10) {
               var5++;
               break;
            }

            NativeImage var11 = var4;
            int var12 = var8;
            int var13 = var5;
            BufferedImage var14 = var1;
            int var15 = var8;
            int var16 = var5;
            int var17 = var14.getRGB(var15, var16);
            var11.setPixel(var12, var13, var17);
            var8++;
         }
      }
   }

   private void f() {
      try {
         NativeImage var1 = new NativeImage(1, 1, false);
         var1.setPixel(0, 0, 0);
         NativeImage var2 = var1;
         CustomImageTexture.a.a(var2);
      } catch (Throwable var4) {
         Night.LOGGER.warn("Failed to load default custom_image fallback", var4);
      }
   }

   public static File resolveImageFile(String var0) {
      if (var0 != null && !var0.isEmpty() && !var0.equalsIgnoreCase("None")) {
         ArrayList var1 = new ArrayList();
         var1.add(IMAGES_DIR);
         ArrayList var2 = var1;
         String[] var3 = new String[]{"Images"};
         Path var4 = Paths.get("Night", var3);
         var2.add(var4);
         ArrayList var5 = var1;
         String[] var6 = new String[]{"images"};
         Path var7 = Paths.get("Night", var6);
         var5.add(var7);
         ArrayList var8 = var1;
         String[] var9 = new String[]{"Images"};
         Path var10 = Paths.get("Night", var9);
         var8.add(var10);
         ArrayList var11 = var1;
         String[] var12 = new String[]{"images"};
         Path var13 = Paths.get("night", var12);
         var11.add(var13);

         try {
            Path var14 = FabricLoader.getInstance().getGameDir();
            ArrayList var15 = var1;
            Path var16 = var14.resolve("Night").resolve("images");
            var15.add(var16);
            ArrayList var17 = var1;
            Path var18 = var14.resolve("Night").resolve("Images");
            var17.add(var18);
            ArrayList var19 = var1;
            Path var20 = var14.resolve("Night").resolve("images");
            var19.add(var20);
         } catch (Throwable var24) {
         }

         Iterator var22 = var1.iterator();

         while (var22.hasNext()) {
            File var23 = ((Path)var22.next()).resolve(var0).toFile();
            if (var23.exists() && var23.isFile()) {
               return var23;
            }
         }

         return IMAGES_DIR.resolve(var0).toFile();
      } else {
         return null;
      }
   }

   public List<File> listImageFiles() {
      this.ensureDirectoryExists();
      LinkedHashMap var1 = new LinkedHashMap();
      ArrayList var2 = new ArrayList();
      var2.add(IMAGES_DIR);
      ArrayList var3 = var2;
      String[] var4 = new String[]{"Images"};
      Path var5 = Paths.get("Night", var4);
      var3.add(var5);
      ArrayList var6 = var2;
      String[] var7 = new String[]{"images"};
      Path var8 = Paths.get("Night", var7);
      var6.add(var8);

      try {
         Path var9 = FabricLoader.getInstance().getGameDir();
         ArrayList var10 = var2;
         Path var11 = var9.resolve("Night").resolve("images");
         var10.add(var11);
         ArrayList var12 = var2;
         Path var13 = var9.resolve("Night").resolve("Images");
         var12.add(var13);
      } catch (Throwable var31) {
      }

      Iterator var15 = var2.iterator();

      while (var15.hasNext()) {
         File var16 = ((Path)var15.next()).toFile();
         if (var16.exists() && var16.isDirectory()) {
            File[] var17 = var16.listFiles((var0, var1x) -> {
               String var2x = var1x.toLowerCase();
               boolean var3x;
               if (!var2x.endsWith(".png") && !var2x.endsWith(".jpg") && !var2x.endsWith(".jpeg") && !var2x.endsWith(".bmp") && !var2x.endsWith(".webp")) {
                  var3x = false;
               } else {
                  var3x = true;
               }

               return var3x;
            });
            if (var17 != null) {
               File[] var18 = var17;
               int var19 = var18.length;
               int var20 = 0;

               while (true) {
                  int var21 = var20;
                  int var22 = var19;
                  if (var21 >= var22) {
                     break;
                  }

                  File[] var23 = var18;
                  int var24 = var20;
                  File var25 = var23[var24];
                  LinkedHashMap var26 = var1;
                  String var27 = var25.getName();
                  File var28 = var25;
                  var26.putIfAbsent(var27, var28);
                  var20++;
               }
            }
         }
      }

      Collection var29 = var1.values();
      ArrayList<File> var30 = new ArrayList<>(var29);
      var30.sort((var0, var1x) -> {
         String var2x = var0.getName();
         String var3x = var1x.getName();
         return var2x.compareToIgnoreCase(var3x);
      });
      return var30;
   }

   public CustomImageManager.CachedImage getOrLoadPreview(String var1) {
      if (var1 != null && !var1.isEmpty() && !var1.equalsIgnoreCase("None")) {
         if (this.cachedImages.containsKey(var1)) {
            CustomImageManager.CachedImage var2 = this.cachedImages.get(var1);
            if (var2.b.exists()) {
               return var2;
            }

            var2.a();
            this.cachedImages.remove(var1);
         }

         File var3 = resolveImageFile(var1);
         if (var3 != null && var3.exists() && var3.isFile()) {
            try {
               NativeImage var4 = readNativeImage(var3);
               int var5 = Math.abs(var1.hashCode());
               long var6 = System.currentTimeMillis();
               Identifier var8 = Identifier.fromNamespaceAndPath("night", "preview_" + var5 + "_" + var6);
               NativeImage var9 = var4;
               DynamicTexture var10 = new DynamicTexture(() -> "night/" + var1, var9);
               var10.upload();
               TextureManager var11 = Minecraft.getInstance().getTextureManager();
               Identifier var12 = var8;
               DynamicTexture var13 = var10;
               var11.register(var12, var13);
               File var14 = var3;
               Identifier var15 = var8;
               DynamicTexture var16 = var10;
               int var17 = var4.getWidth();
               int var18 = var4.getHeight();
               CustomImageManager.CachedImage var19 = new CustomImageManager.CachedImage(var1, var14, var15, var16, var17, var18);
               Map var20 = this.cachedImages;
               CustomImageManager.CachedImage var21 = var19;
               var20.put(var1, var21);
               return var19;
            } catch (Throwable var24) {
               Night.LOGGER.error("Failed to load image: " + var1, var24);
               return null;
            }
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   public void setActiveImage(String var1) {
      String var2;
      if (var1 != null) {
         var2 = var1;
      } else {
         var2 = "None";
      }

      this.activeImageName = var2;
      if (!this.activeImageName.equalsIgnoreCase("None") && !this.activeImageName.isEmpty()) {
         File var3 = resolveImageFile(this.activeImageName);
         if (var3 != null && var3.exists()) {
            try {
               NativeImage var4 = readNativeImage(var3);
               CustomImageTexture.a.a(var4);

               try {
                  Minecraft.getInstance().getTextureManager().register(EFFECT_CUSTOM_IMAGE_ID, CustomImageTexture.a);
                  Minecraft.getInstance().getTextureManager().register(CUSTOM_IMAGE_ID, CustomImageTexture.a);
               } catch (Throwable var7) {
               }
            } catch (Throwable var8) {
               Night.LOGGER.error("Failed to set active custom image: " + var1, var8);
               this.f();
            }
         } else {
            this.f();
         }
      } else {
         this.f();
      }
   }

   public String getActiveImageName() {
      return this.activeImageName;
   }

   static {
      String[] var0 = new String[]{"images"};
      IMAGES_DIR = Paths.get("Night", var0);
   }

   public static class CachedImage {
      public final String a;
      public final File b;
      public final Identifier c;
      public final DynamicTexture d;
      public final int e;
      public final int f;

      public CachedImage(String var1, File var2, Identifier var3, DynamicTexture var4, int var5, int var6) {
         this.a = var1;
         this.b = var2;
         this.c = var3;
         this.d = var4;
         this.e = var5;
         this.f = var6;
      }

      public void a() {
         try {
            if (this.d != null) {
               this.d.close();
            }

            if (this.c != null) {
               TextureManager var1 = Minecraft.getInstance().getTextureManager();
               Identifier var2 = this.c;
               var1.release(var2);
            }
         } catch (Throwable var4) {
         }
      }
   }
}
