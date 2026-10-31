package night.manager;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import java.awt.Color;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import lombok.Generated;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Font.DisplayMode;
import net.minecraft.client.gui.Font.GlyphVisitor;
import net.minecraft.client.gui.Font.PreparedText;
import net.minecraft.client.gui.font.TextRenderable;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.FormattedCharSequence;
import night.Night;
import night.module.impl.core.FontModule;
import night.module.impl.core.HudModule;
import night.util.MinecraftInstance;
import night.util.color.ColorUtil;
import night.util.font.CustomFontRenderer;
import night.util.render.Render3DUtil;
import night.util.render.TextGlowRenderer;

public class FontManager implements MinecraftInstance {
   private CustomFontRenderer customFont;
   private static final ThreadLocal<Boolean> hudGlowContext = ThreadLocal.withInitial(() -> false);
   private static final ThreadLocal<Integer> renderDepth = ThreadLocal.withInitial(() -> 0);

   public static void enterRender() {
      renderDepth.set(renderDepth.get() + 1);
   }

   public static void exitRender() {
      int var0 = renderDepth.get() - 1;
      renderDepth.set(Math.max(0, var0));
   }

   public static boolean isInRender() {
      return renderDepth.get() > 0;
   }

   public static void setHudGlowContext(boolean var0) {
      hudGlowContext.set(var0);
   }

   public static boolean isHudGlowContext() {
      return hudGlowContext.get();
   }

   public boolean isGlowEnabled() {
      FontModule var1 = Night.moduleManager != null ? Night.moduleManager.getModule(FontModule.class) : null;
      HudModule var2 = Night.moduleManager != null ? Night.moduleManager.getModule(HudModule.class) : null;
      boolean var3 = var1 != null && var1.isEnabled() && var1.glow.getValue();
      boolean var4 = var2 != null && var2.isEnabled() && var2.textGlow.getValue();
      return var3 || var4;
   }

   private boolean i() {
      return this.b(false);
   }

   private boolean b(boolean var1) {
      FontModule var2 = Night.moduleManager != null ? Night.moduleManager.getModule(FontModule.class) : null;
      if (var2 != null && var2.isEnabled()) {
         boolean var3 = var2.customFont.getValue();
         if (this.customFont == null && var3) {
            var2.v();
         }

         return this.customFont != null && var3;
      } else {
         return false;
      }
   }

   private boolean j() {
      FontModule var1 = Night.moduleManager != null ? Night.moduleManager.getModule(FontModule.class) : null;
      return var1 != null && !var1.shadowMode.getValue().equalsIgnoreCase("None");
   }

   public void drawText(GuiGraphicsExtractor var1, String var2, int var3, int var4, Color var5) {
      this.drawText(var1, var2, var3, var4, var5, false);
   }

   public void drawText(GuiGraphicsExtractor var1, String var2, int var3, int var4, Color var5, boolean var6) {
      enterRender();

      try {
         boolean var7 = var6 || isHudGlowContext() && this.isGlowEnabled();
         if (this.b(var7)) {
            if (var7) {
               this.customFont.a(var1, var2, var3, var4, var5.getRGB(), false, true, false);
            }

            this.customFont.a(var1, var2, var3, var4, var5.getRGB(), false, false, true);
         } else {
            if (var7) {
               TextGlowRenderer.a(var1, var2, var3, var4, var5.getRGB());
            }

            var1.text(mc.font, var2, var3, var4, var5.getRGB(), false);
         }
      } finally {
         exitRender();
      }
   }

   public void drawShadowedText(GuiGraphicsExtractor var1, String var2, int var3, int var4, Color var5) {
      this.drawShadowedText(var1, var2, var3, var4, var5, false);
   }

   public void drawShadowedText(GuiGraphicsExtractor var1, String var2, int var3, int var4, Color var5, boolean var6) {
      enterRender();

      try {
         boolean var7 = var6 || isHudGlowContext() && this.isGlowEnabled();
         if (this.b(var7)) {
            if (var7) {
               this.customFont.a(var1, var2, var3, var4, var5.getRGB(), false, true, false);
            }

            if (this.j()) {
               this.customFont.a(var1, var2, var3 + this.getShadowOffset(), var4 + this.getShadowOffset(), var5.getRGB(), true, false, true);
            }

            this.customFont.a(var1, var2, var3, var4, var5.getRGB(), false, false, true);
         } else {
            if (var7) {
               TextGlowRenderer.a(var1, var2, var3, var4, var5.getRGB());
            }

            var1.text(mc.font, var2, var3, var4, var5.getRGB(), this.j());
         }
      } finally {
         exitRender();
      }
   }

   public void drawFormattedText(GuiGraphicsExtractor var1, FormattedCharSequence var2, int var3, int var4, Color var5) {
      this.drawFormattedText(var1, var2, var3, var4, var5, false);
   }

   public void drawFormattedText(GuiGraphicsExtractor var1, FormattedCharSequence var2, int var3, int var4, Color var5, boolean var6) {
      enterRender();

      try {
         boolean var7 = var6 || isHudGlowContext() && this.isGlowEnabled();
         if (this.b(var7)) {
            if (var7) {
               this.customFont.a(var1, var2, var3, var4, var5.getRGB(), false, true, false);
            }

            if (this.j()) {
               this.customFont.a(var1, var2, var3 + this.getShadowOffset(), var4 + this.getShadowOffset(), var5.getRGB(), true, false, true);
            }

            this.customFont.a(var1, var2, var3, var4, var5.getRGB(), false, false, true);
         } else {
            if (var7) {
               TextGlowRenderer.a(var1, var2, var3, var4, var5.getRGB());
            }

            var1.text(mc.font, var2, var3, var4, var5.getRGB(), this.j());
         }
      } finally {
         exitRender();
      }
   }

   public void drawWorldText(PoseStack var1, String var2, int var3, int var4, Color var5) {
      this.drawWorldText(var1, var2, var3, var4, var5, false);
   }

   public void drawWorldText(PoseStack var1, String var2, float var3, float var4, Color var5, boolean var6) {
      enterRender();

      try {
         boolean var7 = var6 || isHudGlowContext() && this.isGlowEnabled();
         if (this.b(var7)) {
            if (var7) {
               this.customFont.a(var1, var2, var3, var4, var5.getRGB(), false, true, false);
            }

            if (this.j() && !var6) {
               this.customFont.a(var1, var2, var3 + this.getShadowOffset(), var4 + this.getShadowOffset(), var5.getRGB(), true, false, true);
            }

            this.customFont.a(var1, var2, var3, var4, var5.getRGB(), false, false, true);
         } else {
            if (var7) {
               TextGlowRenderer.a_void_Runnable(() -> this.a_void_PoseStack_String_float_float_int_bool(var1, var2, var3, var4, var5.getRGB(), false));
            }

            this.a_void_PoseStack_String_float_float_int_bool(var1, var2, var3, var4, var5.getRGB(), this.j() && !var6);
         }
      } finally {
         exitRender();
      }
   }

   // $VF: Could not verify finally blocks. A semaphore variable has been added to preserve control flow.
   // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
   private void a_void_PoseStack_String_float_float_int_bool(final PoseStack var1, String var2, float var3, float var4, int var5, boolean var6) {
      PreparedText var7 = mc.font.prepareText(var2, var3, var4, var5, var6, 0);
      final HashMap<RenderType, ByteBufferBuilder> var8 = new HashMap<>();
      final HashMap<RenderType, BufferBuilder> var9 = new HashMap<>();
      boolean var16 = false /* VF: Semaphore variable */;

      try {
         var16 = true;
         var7.visit(new GlyphVisitor() {
            public void acceptRenderable(TextRenderable var1x) {
               RenderType var2x = var1x.renderType(DisplayMode.SEE_THROUGH);
               BufferBuilder var3x = var9.computeIfAbsent(var2x, var1xxx -> {
                  ByteBufferBuilder var2xx = new ByteBufferBuilder(256);
                  var8.put(var1xxx, var2xx);
                  return new BufferBuilder(var2xx, var1xxx.primitiveTopology(), var1xxx.format());
               });
               var1x.render(var1.last().pose(), var3x, 15728880, false);
            }
         });

         for (Entry var11 : (Iterable<Entry>)(Object)var9.entrySet()) {
            MeshData var12 = ((BufferBuilder)var11.getValue()).build();
            Render3DUtil.a_void_RenderType_MeshData((RenderType)var11.getKey(), var12);
         }

         var16 = false;
      } finally {
         if (var16) {
            Iterator var14 = var8.values().iterator();

            while (var14.hasNext()) {
               ((ByteBufferBuilder)var14.next()).close();
            }
         }
      }

      for (ByteBufferBuilder var19 : var8.values()) {
         var19.close();
      }
   }

   public void drawPlainWorldText(PoseStack var1, String var2, int var3, int var4, Color var5) {
      this.drawPlainWorldText(var1, var2, var3, var4, var5, false);
   }

   public void drawPlainWorldText(PoseStack var1, String var2, float var3, float var4, Color var5, boolean var6) {
      enterRender();

      try {
         boolean var7 = var6 || isHudGlowContext() && this.isGlowEnabled();
         if (this.b(var7)) {
            if (var7) {
               this.customFont.a(var1, var2, var3, var4, var5.getRGB(), false, true, false);
            }

            this.customFont.a(var1, var2, var3, var4, var5.getRGB(), false, false, true);
         } else {
            if (var7) {
               TextGlowRenderer.a_void_Runnable(() -> this.a_void_PoseStack_String_float_float_int_bool(var1, var2, var3, var4, var5.getRGB(), false));
            }

            this.a_void_PoseStack_String_float_float_int_bool(var1, var2, var3, var4, var5.getRGB(), false);
         }
      } finally {
         exitRender();
      }
   }

   public void drawOutlinedText(GuiGraphicsExtractor var1, String var2, int var3, int var4, Color var5, Color var6) {
      if (this.i()) {
         this.customFont.a(var1, CustomFontRenderer.b_String_String(var2), var3 + 0.5F, var4 - 0.5F, var6.getRGB(), false);
         this.customFont.a(var1, CustomFontRenderer.b_String_String(var2), var3 - 0.5F, var4 + 0.5F, var6.getRGB(), false);
         this.customFont.a(var1, CustomFontRenderer.b_String_String(var2), var3 + 0.5F, var4 + 0.5F, var6.getRGB(), false);
         this.customFont.a(var1, CustomFontRenderer.b_String_String(var2), var3 - 0.5F, var4 - 0.5F, var6.getRGB(), false);
         this.customFont.a(var1, var2, var3, var4, var5.getRGB(), false);
      } else {
         for (int var7 = -1; var7 <= 1; var7++) {
            for (int var8 = -1; var8 <= 1; var8++) {
               if (var7 != 0 || var8 != 0) {
                  var1.text(mc.font, var2, var3 + var7, var4 + var8, var6.getRGB(), false);
               }
            }
         }

         var1.text(mc.font, var2, var3, var4, var5.getRGB(), false);
      }
   }

   public void drawRainbowText(GuiGraphicsExtractor var1, String var2, int var3, int var4, long var5) {
      MutableComponent var7 = Component.empty();
      int[] var8 = new int[]{0};
      Component.literal(var2).getVisualOrderText().accept((var4x, var5x, var6) -> {
         MutableComponent var7x = Component.empty();
         if (var5x.getColor() == null) {
            Color var8x = ColorUtil.a_Color_long(var8[0] * var5);
            var7x.append(Component.literal(String.valueOf(Character.toChars(var6))).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(var8x.getRGB()))));
         } else {
            var7x.append(Component.literal(String.valueOf(Character.toChars(var6))).setStyle(var5x));
         }

         var7.append(var7x);
         var8[0]++;
         return true;
      });
      Night.fontManager.drawFormattedText(var1, var7.getVisualOrderText(), var3, var4, Color.WHITE);
   }

   public int getStringWidth(String var1) {
      return this.i() ? (int)this.customFont.a_float_String(var1) : mc.font.width(var1);
   }

   public int getFontHeight() {
      if (this.i()) {
         FontModule var1 = Night.moduleManager != null ? Night.moduleManager.getModule(FontModule.class) : null;
         int var2 = var1 != null ? var1.heightOffset.getValue().intValue() : 0;
         return (int)this.customFont.a() + var2;
      } else {
         return 9;
      }
   }

   public float getShadowOffset() {
      FontModule var1 = Night.moduleManager != null ? Night.moduleManager.getModule(FontModule.class) : null;
      if (var1 == null || var1.shadowMode.getValue().equalsIgnoreCase("None")) {
         return 0.0F;
      } else {
         return var1.shadowMode.getValue().equalsIgnoreCase("Custom") ? var1.shadowOffset.getValue().floatValue() : 1.0F;
      }
   }

   @Generated
   public CustomFontRenderer getCustomFont() {
      return this.customFont;
   }

   @Generated
   public void setCustomFont(CustomFontRenderer var1) {
      this.customFont = var1;
   }
}
