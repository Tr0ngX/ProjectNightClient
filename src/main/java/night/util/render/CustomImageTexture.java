package night.util.render;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import night.Night;

public class CustomImageTexture extends AbstractTexture {
   public static final CustomImageTexture a = new CustomImageTexture();
   private NativeImage b;

   public void a(NativeImage var1) {
      Minecraft var2 = Minecraft.getInstance();
      if (var2 != null && !var2.isSameThread()) {
         var2.execute(() -> this.b(var1));
      } else {
         this.b(var1);
      }
   }

   private synchronized void b(NativeImage var1) {
      if (this.b != null && this.b != var1) {
         try {
            this.b.close();
         } catch (Throwable var6) {
         }
      }

      this.b = var1;
      if (this.texture != null) {
         try {
            this.texture.close();
         } catch (Throwable var5) {
         }

         this.texture = null;
      }

      if (var1 == null) {
         this.textureView = null;
      } else {
         try {
            GpuDevice var2 = RenderSystem.getDevice();
            if (var2 == null) {
               return;
            }

            this.texture = var2.createTexture(() -> "night/custom_image", 5, GpuFormat.RGBA8_UNORM, var1.getWidth(), var1.getHeight(), 1, 1);
            this.sampler = RenderSystem.getSamplerCache().getRepeat(FilterMode.LINEAR);
            this.textureView = var2.createTextureView(this.texture);
            var2.createCommandEncoder().writeToTexture(this.texture, var1);
         } catch (Throwable var7) {
            Night.LOGGER.error("Failed to upload custom image texture to GPU", var7);
            if (this.texture != null) {
               try {
                  this.texture.close();
               } catch (Throwable var4) {
               }

               this.texture = null;
            }

            this.textureView = null;
         }
      }
   }

   public GpuTextureView getTextureView() {
      if (this.textureView == null) {
         if (Night.customImageManager != null) {
            String var1 = Night.customImageManager.getActiveImageName();
            if (var1 != null && !var1.equalsIgnoreCase("None") && !var1.isEmpty()) {
               Night.customImageManager.setActiveImage(var1);
            }
         }

         if (this.textureView == null) {
            try {
               NativeImage var3 = new NativeImage(1, 1, false);
               var3.setPixel(0, 0, 0);
               this.b(var3);
            } catch (Throwable var2) {
            }
         }
      }

      return super.getTextureView();
   }

   public synchronized void close() {
      if (this.b != null) {
         try {
            this.b.close();
         } catch (Throwable var3) {
         }

         this.b = null;
      }

      if (this.texture != null) {
         try {
            this.texture.close();
         } catch (Throwable var2) {
         }

         this.texture = null;
      }

      this.textureView = null;
   }
}
