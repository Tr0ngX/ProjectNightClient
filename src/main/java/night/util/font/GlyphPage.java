package night.util.font;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.NativeImage.Format;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import it.unimi.dsi.fastutil.chars.Char2ObjectArrayMap;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.OptionalDouble;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Generated;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import night.mixins.accessors.AbstractTextureAccessor;
import night.mixins.accessors.NativeImageAccessor;
import org.lwjgl.system.MemoryUtil;

public class GlyphPage {
   private static final AtomicInteger b = new AtomicInteger();
   private DynamicTexture c;
   private Identifier d;
   private final Font[] e;
   private int f;
   private int g;
   private final char h;
   private final char i;
   private final int j;
   private final Char2ObjectArrayMap<GlyphMetrics> k = new Char2ObjectArrayMap();
   boolean a = false;

   public GlyphPage(Font[] var1, char var2, char var3, int var4) {
      this.e = var1;
      this.h = var2;
      this.i = var3;
      this.j = var4;
   }

   public void a() {
      synchronized (this) {
         this.g();
      }
   }

   private void g() {
      if (!this.a) {
         int var1 = this.i - this.h - 1;
         int var2 = (int)(Math.ceil(Math.sqrt(var1)) * 1.5);
         int var3 = 0;
         int var4 = 0;
         int var5 = 0;
         int var6 = 0;
         int var7 = 0;
         int var8 = 0;
         int var9 = 0;
         ArrayList var10 = new ArrayList();
         AffineTransform var11 = new AffineTransform();
         FontRenderContext var12 = new FontRenderContext(var11, true, true);

         while (var3 <= var1) {
            char var13 = (char)(this.h + var3);
            Font var14 = this.c(var13);
            Rectangle2D var15 = var14.getStringBounds(String.valueOf(var13), var12);
            int var16 = (int)Math.ceil(var15.getWidth());
            int var17 = (int)Math.ceil(var15.getHeight());
            var3++;
            var5 = Math.max(var5, var7 + var16);
            var6 = Math.max(var6, var8 + var17);
            if (var4 >= var2) {
               var7 = 0;
               var8 += var9 + this.j;
               var4 = 0;
               var9 = 0;
            }

            var9 = Math.max(var9, var17);
            var10.add(new GlyphMetrics(this, var7, var8, var16, var17, var13));
            var7 += var16 + this.j;
            var4++;
         }

         BufferedImage var24 = new BufferedImage(Math.max(var5 + this.j, 1), Math.max(var6 + this.j, 1), 2);
         this.f = var24.getWidth();
         this.g = var24.getHeight();
         Graphics2D var25 = var24.createGraphics();
         var25.setColor(new Color(255, 255, 255, 0));
         var25.fillRect(0, 0, this.f, this.g);
         var25.setColor(Color.WHITE);
         var25.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
         var25.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         var25.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
         var25.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
         var25.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

         for (GlyphMetrics var28 : (Iterable<GlyphMetrics>)(Object)var10) {
            var25.setFont(this.c(var28.value()));
            FontMetrics var30 = var25.getFontMetrics();
            var25.drawString(String.valueOf(var28.value()), var28.count(), var28.count2() + var30.getAscent());
            this.k.put(var28.value(), var28);
         }

         NativeImage var27 = new NativeImage(Format.RGBA, var24.getWidth(), var24.getHeight(), false);
         IntBuffer var29 = MemoryUtil.memIntBuffer(((NativeImageAccessor)(Object)var27).getPointer(), var27.getWidth() * var27.getHeight());
         WritableRaster var18 = var24.getRaster();
         ColorModel var19 = var24.getColorModel();
         int var20 = var18.getNumBands();
         int var21 = var18.getDataBuffer().getDataType();

         Object var31 = switch (var21) {
            case 0 -> new byte[var20];
            case 1 -> new short[var20];
            default -> throw new IllegalArgumentException("Unknown data buffer type: " + var21);
            case 3 -> new int[var20];
            case 4 -> new float[var20];
            case 5 -> new double[var20];
         };

         for (int var22 = 0; var22 < var24.getHeight(); var22++) {
            for (int var23 = 0; var23 < var24.getWidth(); var23++) {
               var18.getDataElements(var23, var22, var31);
               var29.put(var19.getAlpha(var31) << 24 | var19.getBlue(var31) << 16 | var19.getGreen(var31) << 8 | var19.getRed(var31));
            }
         }

         int var32 = b.getAndIncrement();
         DynamicTexture var33 = new DynamicTexture(() -> "night/glyph_page_" + var32, var27);
         this.c = var33;
         this.d = Identifier.fromNamespaceAndPath("night", "glyph_page_" + var32);
         Minecraft.getInstance().getTextureManager().register(this.d, var33);
         ((AbstractTextureAccessor)var33)
            .setSampler(
               RenderSystem.getDevice()
                  .createSampler(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE, FilterMode.LINEAR, FilterMode.LINEAR, 1, OptionalDouble.empty())
            );
         this.a = true;
      }
   }

   public GlyphMetrics a_GlyphMetrics_char(char var1) {
      synchronized (this) {
         if (!this.a) {
            this.g();
         }

         return (GlyphMetrics)this.k.get(var1);
      }
   }

   public void b() {
      synchronized (this) {
         this.a = false;
         if (this.d != null) {
            Minecraft.getInstance().getTextureManager().release(this.d);
            this.d = null;
         }

         if (this.c != null) {
            this.c.close();
         }

         this.k.clear();
         this.f = -1;
         this.g = -1;
      }
   }

   public boolean b(char var1) {
      return var1 >= this.h && var1 < this.i;
   }

   private Font c(char var1) {
      for (Font var5 : this.e) {
         if (var5.canDisplay(var1)) {
            return var5;
         }
      }

      return this.e[0];
   }

   @Generated
   public DynamicTexture c() {
      return this.c;
   }

   @Generated
   public Identifier d() {
      return this.d;
   }

   @Generated
   public int e() {
      return this.f;
   }

   @Generated
   public int f() {
      return this.g;
   }
}
