package night.util.duck;

import night.util.render.GlintRenderLayer;

public interface ChamsRenderStateDuck {
   boolean night$chamsFill();

   int night$chamsFillColor();

   boolean night$chamsOutline();

   int night$chamsOutlineColor();

   boolean night$chamsShine();

   boolean night$chamsGlint();

   int night$chamsGlintColor();

   GlintRenderLayer.GlintParams night$chamsGlintParams();

   boolean night$chamsSuppressReal();

   float night$chamsRealAlpha();

   void night$setChamsRealAlpha(float var1);

   float night$chamsYOffset();

   void night$setChamsYOffset(float var1);

   void night$setChams(boolean var1, int var2, boolean var3, int var4, boolean var5);

   void night$setChams(boolean var1, int var2, boolean var3, int var4, boolean var5, boolean var6);

   void night$setChamsGlint(boolean var1, int var2, GlintRenderLayer.GlintParams var3);
}
