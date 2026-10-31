package night.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import night.Night;
import night.module.impl.player.Swing;
import night.module.impl.visuals.ShadersModule;
import night.module.impl.visuals.ViewModel;
import night.util.world.WorldLoadState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {
   @Shadow
   private float mainHandHeight;
   @Shadow
   private float oMainHandHeight;
   @Shadow
   private float offHandHeight;
   @Shadow
   private float oOffHandHeight;
   @Shadow
   private ItemStack mainHandItem;
   @Shadow
   private ItemStack offHandItem;

   @Inject(method = "submitHandsWithItems", at = @At("HEAD"))
   private void night$startHands(float var1, PoseStack var2, SubmitNodeCollector var3, LocalPlayer var4, int var5, CallbackInfo var6) {
      WorldLoadState.a = true;
   }

   @Inject(method = "tick", at = @At("TAIL"))
   private void night$tickViewModel(CallbackInfo var1) {
      ViewModel var2 = Night.moduleManager != null ? Night.moduleManager.getModule(ViewModel.class) : null;
      if (var2 != null && var2.isEnabled()) {
         Minecraft var3 = Minecraft.getInstance();
         if (var3.player != null) {
            if (!var2.mainhandSwap.getValue()) {
               this.mainHandHeight = 1.0F;
               this.oMainHandHeight = 1.0F;
               this.mainHandItem = var3.player.getMainHandItem();
            }

            if (!var2.offhandSwap.getValue()) {
               this.offHandHeight = 1.0F;
               this.oOffHandHeight = 1.0F;
               this.offHandItem = var3.player.getOffhandItem();
            }
         }
      }
   }

   @Inject(method = "submitArmWithItem", at = @At("HEAD"))
   private void night$applyViewModelTransformHead(
      AbstractClientPlayer var1,
      float var2,
      float var3,
      InteractionHand var4,
      float var5,
      ItemStack var6,
      float var7,
      PoseStack var8,
      SubmitNodeCollector var9,
      int var10,
      CallbackInfo var11
   ) {
      ViewModel var12 = Night.moduleManager != null ? Night.moduleManager.getModule(ViewModel.class) : null;
      if (var12 != null && var12.isEnabled()) {
         var8.pushPose();
         if (var4 == InteractionHand.MAIN_HAND || var12.mode.getValue().equalsIgnoreCase("Both")) {
            var8.translate(
               var12.positionMainX.getValue().floatValue(), var12.positionMainY.getValue().floatValue(), var12.positionMainZ.getValue().floatValue()
            );
            var8.scale(var12.scaleMainX.getValue().floatValue(), var12.scaleMainY.getValue().floatValue(), var12.scaleMainZ.getValue().floatValue());
            var8.mulPose(Axis.XP.rotationDegrees(var12.rotationMainX.getValue().floatValue()));
            var8.mulPose(Axis.YP.rotationDegrees(var12.rotationMainY.getValue().floatValue()));
            var8.mulPose(Axis.ZP.rotationDegrees(var12.rotationMainZ.getValue().floatValue()));
         } else {
            var8.translate(var12.positionOffX.getValue().floatValue(), var12.positionOffY.getValue().floatValue(), var12.x.getValue().floatValue());
            var8.scale(var12.scaleOffX.getValue().floatValue(), var12.scaleOffY.getValue().floatValue(), var12.scaleOffZ.getValue().floatValue());
            var8.mulPose(Axis.XP.rotationDegrees(var12.y.getValue().floatValue()));
            var8.mulPose(Axis.YP.rotationDegrees(var12.z.getValue().floatValue()));
            var8.mulPose(Axis.ZP.rotationDegrees(var12.rotationOffZ.getValue().floatValue()));
         }
      }
   }

   @Inject(method = "submitArmWithItem", at = @At("RETURN"))
   private void night$applyViewModelTransformReturn(
      AbstractClientPlayer var1,
      float var2,
      float var3,
      InteractionHand var4,
      float var5,
      ItemStack var6,
      float var7,
      PoseStack var8,
      SubmitNodeCollector var9,
      int var10,
      CallbackInfo var11
   ) {
      ViewModel var12 = Night.moduleManager != null ? Night.moduleManager.getModule(ViewModel.class) : null;
      if (var12 != null && var12.isEnabled()) {
         var8.popPose();
      }
   }

   @Inject(method = "applyEatTransform", at = @At("HEAD"), cancellable = true)
   private void night$applyEatTransform(PoseStack var1, float var2, HumanoidArm var3, ItemStack var4, Player var5, CallbackInfo var6) {
      ViewModel var7 = Night.moduleManager != null ? Night.moduleManager.getModule(ViewModel.class) : null;
      if (var7 != null && var7.isEnabled()) {
         float var8 = var7.eatingMultiplier.getValue().floatValue();
         if (var8 < 1.0F) {
            float var9 = var5.getUseItemRemainingTicks() - var2 + 1.0F;
            float var10 = var9 / var4.getUseDuration(var5);
            if (var10 < 0.8F) {
               float var11 = Mth.abs(Mth.cos(var9 / 4.0F * (float) Math.PI) * 0.1F) * var8;
               var1.translate(0.0F, var11, 0.0F);
            }

            float var13 = 1.0F - (float)Math.pow(var10, 27.0);
            int var12 = var3 == HumanoidArm.RIGHT ? 1 : -1;
            var1.translate(var13 * 0.6F * var12, var13 * -0.5F, var13 * 0.0F);
            var1.mulPose(Axis.YP.rotationDegrees(var12 * var13 * 90.0F));
            var1.mulPose(Axis.XP.rotationDegrees(var13 * 10.0F));
            var1.mulPose(Axis.ZP.rotationDegrees(var12 * var13 * 30.0F));
            var6.cancel();
         }

         if (var7.eatAnimation.getValue()) {
            var1.translate(var7.eatX.getValue().floatValue(), var7.eatY.getValue().floatValue(), 0.0F);
         }
      }
   }

   @ModifyArg(
      method = "renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/renderer/item/ItemStackRenderState;submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;III)V"
      ),
      index = 4
   )
   private int night$handsOutline(int var1, @Local(argsOnly = true) ItemStack var2) {
      return computeHandsOutlineColor(var1);
   }

   private static int computeHandsOutlineColor(int var0) {
      Minecraft var1 = Minecraft.getInstance();
      if (var1.player == null) {
         return var0;
      }

      ShadersModule var2 = Night.moduleManager.getModule(ShadersModule.class);
      return var2.isEnabled() && var2.hands.getValue() ? var2.c(var1.player) : var0;
   }

   @Inject(method = "submitHandsWithItems", at = @At("RETURN"))
   private void night$flushHandOutline(float var1, PoseStack var2, SubmitNodeCollector var3, LocalPlayer var4, int var5, CallbackInfo var6) {
      WorldLoadState.a = false;
   }

   @Inject(method = "swingArm", at = @At("HEAD"), cancellable = true)
   private void night$swingArm(float var1, PoseStack var2, int var3, HumanoidArm var4, CallbackInfo var5) {
      Swing var6 = Night.moduleManager.getModule(Swing.class);
      if (var6.isEnabled()) {
         float var7 = var6.translateX.getValue() ? -0.4F * Mth.sin(Mth.sqrt(var1) * (float) Math.PI) : 0.0F;
         float var8 = var6.translateY.getValue() ? 0.2F * Mth.sin(Mth.sqrt(var1) * (float) (Math.PI * 2)) : 0.0F;
         float var9 = var6.translateZ.getValue() ? -0.2F * Mth.sin(var1 * (float) Math.PI) : 0.0F;
         var2.translate(var3 * var7, var8, var9);
         night$applyItemArmAttackTransform(var2, var4, var1, var6);
         var5.cancel();
      }
   }

   private static void night$applyItemArmAttackTransform(PoseStack var0, HumanoidArm var1, float var2, Swing var3) {
      int var4 = var1 == HumanoidArm.RIGHT ? 1 : -1;
      float var5 = var3.rotationY.getValue() ? Mth.sin(var2 * var2 * (float) Math.PI) : 0.0F;
      var0.mulPose(Axis.YP.rotationDegrees(var4 * (45.0F + var5 * -20.0F)));
      float var6 = Mth.sin(Mth.sqrt(var2) * (float) Math.PI);
      var0.mulPose(Axis.ZP.rotationDegrees(var4 * (var3.rotationZ.getValue() ? var6 : 0.0F) * -20.0F));
      var0.mulPose(Axis.XP.rotationDegrees((var3.rotationX.getValue() ? var6 : 0.0F) * -80.0F));
      var0.mulPose(Axis.YP.rotationDegrees(var4 * -45.0F));
   }
}
