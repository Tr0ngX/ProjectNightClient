package night.util.render;

import com.mojang.blaze3d.pipeline.TextureTarget;
import night.util.MinecraftInstance;
import night.util.world.PacketAndRenderUtil;

public class StarCaptureTarget implements MinecraftInstance {
   private static TextureTarget a;

   public static TextureTarget a() {
      int var0 = mc.getWindow().getWidth();
      int var1 = mc.getWindow().getHeight();
      if (a == null || a.width != var0 || a.height != var1) {
         if (a != null) {
            a.destroyBuffers();
         }

         a = PacketAndRenderUtil.a("night_star_capture", var0, var1);
      }

      return a;
   }

   public static TextureTarget b() {
      return a;
   }
}
