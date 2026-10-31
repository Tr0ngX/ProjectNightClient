package night.util.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PackResources.ResourceOutput;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.IoSupplier;
import night.Night;
import night.manager.CustomImageManager;
import org.jspecify.annotations.Nullable;

public class NightBuiltinResourcePack extends AbstractPackResources implements PackResources {
   public static final NightBuiltinResourcePack a = new NightBuiltinResourcePack();
   private static final Set<String> b = Set.of("night");
   private static final List<String> c = List.of(
      "icon.png",
      "post_effect/clickgui_bg.json",
      "post_effect/glow_blur.json",
      "post_effect/menu_bg.json",
      "post_effect/neekeri_ui.json",
      "post_effect/outline_bloom.json",
      "post_effect/outline_both_100.json",
      "post_effect/outline_both_20.json",
      "post_effect/outline_both_40.json",
      "post_effect/outline_both_60.json",
      "post_effect/outline_both_80.json",
      "post_effect/outline_fill_100.json",
      "post_effect/outline_fill_20.json",
      "post_effect/outline_fill_40.json",
      "post_effect/outline_fill_60.json",
      "post_effect/outline_fill_80.json",
      "post_effect/outline_glow.json",
      "post_effect/outline_outline.json",
      "post_effect/star_glow.json",
      "post_effect/text_glow.json",
      "shaders/core/esp.fsh",
      "shaders/core/killeffect_meme.fsh",
      "shaders/core/menu_shader.fsh",
      "shaders/core/sky.fsh",
      "shaders/program/clickgui_bg.fsh",
      "shaders/program/glow_blur.fsh",
      "shaders/program/menu_bg.fsh",
      "shaders/program/neekeri_ui.fsh",
      "shaders/program/outline.fsh",
      "shaders/program/outline_bloom.fsh",
      "shaders/program/outline_glow.fsh",
      "shaders/program/outline_glow_blur.fsh",
      "shaders/program/sky/screen_blit.vsh",
      "shaders/program/sky/sky_shader.fsh",
      "shaders/program/sky/sky_shader.json",
      "shaders/program/sky/sky_shader.vsh",
      "shaders/program/smoke/smoke.fsh",
      "shaders/program/smoke/smoke.json",
      "shaders/program/smoke/smoke.vsh",
      "shaders/program/star_glow.fsh",
      "shaders/program/star_glow_blur.fsh",
      "shaders/program/text_glow_blur.fsh",
      "shaders/program/text_glow_composite.fsh",
      "sounds.json",
      "sounds/vine.ogg",
      "splash.txt",
      "textures/cape.png",
      "textures/chorus.png",
      "textures/crystal.png",
      "textures/custom_image.png",
      "textures/effect/custom_image.png",
      "textures/effect/meme_arrow.png",
      "textures/effect/meme_circle.png",
      "textures/gear.png",
      "textures/glint.png",
      "textures/gui/logo.png",
      "textures/gui/logo_mask.png",
      "textures/gui/moonlight_bg.png",
      "textures/pearl.png"
   );

   public NightBuiltinResourcePack() {
      super(new PackLocationInfo("night", Component.literal("Night Client Resources"), PackSource.BUILT_IN, Optional.empty()));
   }

   public @Nullable IoSupplier<InputStream> getRootResource(String... var1) {
      String var2 = String.join("/", var1);
      return NightBuiltinResourcePack.class.getClassLoader().getResource(var2) != null
         ? () -> NightBuiltinResourcePack.class.getClassLoader().getResourceAsStream(var2)
         : null;
   }

   public @Nullable IoSupplier<InputStream> getResource(PackType var1, Identifier var2) {
      if (var1 == PackType.CLIENT_RESOURCES && "night".equals(var2.getNamespace())) {
         if ((var2.getPath().equals("textures/effect/custom_image.png") || var2.getPath().equals("textures/custom_image.png"))
            && Night.customImageManager != null) {
            String var3 = Night.customImageManager.getActiveImageName();
            if (var3 != null && !var3.isEmpty() && !var3.equalsIgnoreCase("None")) {
               File var4 = CustomImageManager.resolveImageFile(var3);
               if (var4 != null && var4.exists() && var4.isFile()) {
                  return () -> new FileInputStream(var4);
               }
            }
         }

         String var5 = "assets/night/" + var2.getPath();
         return NightBuiltinResourcePack.class.getClassLoader().getResource(var5) != null
            ? () -> NightBuiltinResourcePack.class.getClassLoader().getResourceAsStream(var5)
            : null;
      } else {
         return null;
      }
   }

   public void listResources(PackType var1, String var2, String var3, ResourceOutput var4) {
      if (var1 == PackType.CLIENT_RESOURCES && "night".equals(var2)) {
         HashSet var5 = new HashSet();
         String var6 = var3.isEmpty() ? "" : (var3.endsWith("/") ? var3 : var3 + "/");

         try {
            URL var7 = NightBuiltinResourcePack.class.getProtectionDomain().getCodeSource().getLocation();
            if (var7 != null) {
               String var8 = "assets/night/" + var6;

               JarEntry var11;
               try (
                  InputStream var9 = var7.openStream();
                  JarInputStream var10 = new JarInputStream(var9);
               ) {
                  while ((var11 = var10.getNextJarEntry()) != null) {
                     String var12 = var11.getName();
                     if (!var11.isDirectory() && var12.startsWith(var8)) {
                        String var13 = var12.substring("assets/night/".length());
                        if (var5.add(var13)) {
                           Identifier var14 = Identifier.tryBuild("night", var13);
                           if (var14 != null) {
                              var4.accept(var14, (IoSupplier)() -> NightBuiltinResourcePack.class.getClassLoader().getResourceAsStream(var12));
                           }
                        }
                     }
                  }
               }
            }
         } catch (Throwable var19) {
         }

         for (String var21 : (Iterable<String>)(Object)c) {
            if (var21.startsWith(var6) && var5.add(var21)) {
               String var22 = "assets/night/" + var21;
               if (NightBuiltinResourcePack.class.getClassLoader().getResource(var22) != null) {
                  Identifier var23 = Identifier.tryBuild("night", var21);
                  if (var23 != null) {
                     var4.accept(var23, (IoSupplier)() -> NightBuiltinResourcePack.class.getClassLoader().getResourceAsStream(var22));
                  }
               }
            }
         }
      }
   }

   public Set<String> getNamespaces(PackType var1) {
      return var1 == PackType.CLIENT_RESOURCES ? b : Set.of();
   }

   public void close() {
   }
}
