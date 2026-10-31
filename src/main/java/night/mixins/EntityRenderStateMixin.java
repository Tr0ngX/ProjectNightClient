package night.mixins;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import night.util.duck.ChamsRenderStateDuck;
import night.util.duck.EntityRenderStateSelfDuck;
import night.util.render.GlintRenderLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(EntityRenderState.class)
public class EntityRenderStateMixin implements ChamsRenderStateDuck, EntityRenderStateSelfDuck {
   @Unique
   private boolean chamsFill = false;
   @Unique
   private int chamsFillColor = 0;
   @Unique
   private boolean chamsOutline = false;
   @Unique
   private int chamsOutlineColor = 0;
   @Unique
   private boolean chamsShine = false;
   @Unique
   private boolean chamsGlint = false;
   @Unique
   private int chamsGlintColor = -1;
   @Unique
   private GlintRenderLayer.GlintParams chamsGlintParams = GlintRenderLayer.b;
   @Unique
   private boolean chamsSuppressReal = false;
   @Unique
   private float chamsRealAlpha = 1.0F;
   @Unique
   private float chamsYOffset = 0.0F;
   @Unique
   private boolean isSelf = false;

   @Override
   public float night$chamsYOffset() {
      return this.chamsYOffset;
   }

   @Override
   public void night$setChamsYOffset(float var1) {
      this.chamsYOffset = var1;
   }

   @Override
   public boolean night$isSelf() {
      return this.isSelf;
   }

   @Override
   public void night$setSelf(boolean var1) {
      this.isSelf = var1;
   }

   @Override
   public boolean night$chamsFill() {
      return this.chamsFill;
   }

   @Override
   public int night$chamsFillColor() {
      return this.chamsFillColor;
   }

   @Override
   public boolean night$chamsOutline() {
      return this.chamsOutline;
   }

   @Override
   public int night$chamsOutlineColor() {
      return this.chamsOutlineColor;
   }

   @Override
   public boolean night$chamsShine() {
      return this.chamsShine;
   }

   @Override
   public boolean night$chamsGlint() {
      return this.chamsGlint;
   }

   @Override
   public int night$chamsGlintColor() {
      return this.chamsGlintColor;
   }

   @Override
   public GlintRenderLayer.GlintParams night$chamsGlintParams() {
      return this.chamsGlintParams;
   }

   @Override
   public boolean night$chamsSuppressReal() {
      return this.chamsSuppressReal;
   }

   @Override
   public float night$chamsRealAlpha() {
      return this.chamsRealAlpha;
   }

   @Override
   public void night$setChamsRealAlpha(float var1) {
      this.chamsRealAlpha = var1;
   }

   public void night$setChamsGlint(boolean var1, int var2) {
      this.chamsGlint = var1;
      this.chamsGlintColor = var2;
      this.chamsGlintParams = GlintRenderLayer.b;
   }

   @Override
   public void night$setChamsGlint(boolean var1, int var2, GlintRenderLayer.GlintParams var3) {
      this.chamsGlint = var1;
      this.chamsGlintColor = var2;
      this.chamsGlintParams = var3 != null ? var3 : GlintRenderLayer.b;
   }

   @Override
   public void night$setChams(boolean var1, int var2, boolean var3, int var4, boolean var5) {
      this.night$setChams(var1, var2, var3, var4, var5, false);
   }

   @Override
   public void night$setChams(boolean var1, int var2, boolean var3, int var4, boolean var5, boolean var6) {
      this.chamsFill = var1;
      this.chamsFillColor = var2;
      this.chamsOutline = var3;
      this.chamsOutlineColor = var4;
      this.chamsShine = var5;
      this.chamsSuppressReal = var6;
      this.chamsRealAlpha = 1.0F;
      this.chamsGlint = false;
      this.chamsGlintColor = -1;
      this.chamsGlintParams = GlintRenderLayer.b;
   }
}
