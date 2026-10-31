package night.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.main.GameConfig;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import night.Night;
import night.events.impl.ItemUseEvent;
import night.events.impl.TickEvent;
import night.module.access.FreecamAccess;
import night.module.impl.combat.Mainhand;
import night.module.impl.core.NoMiddleClick;
import night.module.impl.kingmc.KingMCTotem;
import night.module.impl.misc.AutoEscape;
import night.module.impl.player.FastPlace;
import night.module.impl.player.MultiTask;
import night.util.MinecraftInstance;
import night.util.world.BlockInteractionUtil;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftClientMixin implements MinecraftInstance {
   @Shadow
   @Nullable
   public LocalPlayer player;
   @Shadow
   private int rightClickDelay;
   @Shadow
   @Final
   public Options options;
   @Shadow
   @Nullable
   public HitResult hitResult;
   @Shadow
   @Nullable
   public Entity crosshairPickEntity;

   @Inject(method = "<init>", at = @At("TAIL"))
   private void init(GameConfig var1, CallbackInfo var2) {
      Night.postInit();
   }

   @Inject(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;runAllTasks()V", shift = Shift.AFTER))
   private void runTickHook(boolean var1, CallbackInfo var2) {
      Night.EVENT_BUS.post(new ItemUseEvent());
   }

   @Inject(method = "tick", at = @At("HEAD"))
   private void tick(CallbackInfo var1) {
      Night.EVENT_BUS.post(new TickEvent());
   }

   @Inject(method = "tick", at = @At("TAIL"))
   private void tick$post(CallbackInfo var1) {
      Night.EVENT_BUS.post(new TickEvent.Post());
   }

   @Inject(method = "startUseItem", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;rightClickDelay:I", shift = Shift.AFTER))
   private void doItemUse(CallbackInfo var1) {
      if (Night.moduleManager != null
         && Night.moduleManager.getModule(FastPlace.class).isEnabled()
         && Night.moduleManager.getModule(FastPlace.class).a(this.player.getMainHandItem().getItem())) {
         this.rightClickDelay = Night.moduleManager.getModule(FastPlace.class).ticks.getValue().intValue();
      }
   }

   @ModifyExpressionValue(method = "continueAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z"))
   private boolean handleBlockBreaking(boolean var1) {
      return Night.moduleManager != null && Night.moduleManager.getModule(MultiTask.class).isEnabled() ? false : var1;
   }

   @ModifyExpressionValue(
      method = "startUseItem",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;isDestroying()Z")
   )
   private boolean handleInputEvents(boolean var1) {
      return Night.moduleManager != null && Night.moduleManager.getModule(MultiTask.class).isEnabled() ? false : var1;
   }

   @WrapOperation(
      method = "handleKeybinds",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;releaseUsingItem(Lnet/minecraft/world/entity/player/Player;)V"
      )
   )
   private void night$dontReleaseFakedUse(MultiPlayerGameMode var1, Player var2, Operation<Void> var3) {
      if (!Night.moduleManager.getModule(AutoEscape.class).v()) {
         var3.call(new Object[]{var1, var2});
      }
   }

   @WrapOperation(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;setSelectedSlot(I)V"))
   private void night$redirectSetSelectedSlot(Inventory var1, int var2, Operation<Void> var3) {
      if (Night.moduleManager != null) {
         Mainhand var4 = Night.moduleManager.getModule(Mainhand.class);
         if (var4 != null && var4.x()) {
            return;
         }

         KingMCTotem var5 = Night.moduleManager.getModule(KingMCTotem.class);
         if (var5 != null && !var5.b_bool_int(var2)) {
            return;
         }
      }

      var3.call(new Object[]{var1, var2});
   }

   @Inject(method = "pick", at = @At("HEAD"), cancellable = true)
   private void pick(float var1, CallbackInfo var2) {
      if (mc.level != null && this.player != null) {
         FreecamAccess var3 = (FreecamAccess)Night.moduleManager.getModuleByName("Freecam");
         if (var3.isEnabled()) {
            this.hitResult = BlockInteractionUtil.a_HitResult_float_float_double_double_double(
               var3.getCameraYaw(), var3.getCameraPitch(), var3.getCameraX(), var3.getCameraY(), var3.getCameraZ()
            );
            HitResult var4 = this.hitResult;
            this.crosshairPickEntity = var4 instanceof EntityHitResult ? ((EntityHitResult)var4).getEntity() : null;
            var2.cancel();
         }
      }
   }

   @Inject(method = "pickBlockOrEntity", at = @At("HEAD"), cancellable = true, require = 1)
   private void night$onPickBlock(CallbackInfo var1) {
      if (Night.moduleManager != null) {
         NoMiddleClick var2 = Night.moduleManager.getModule(NoMiddleClick.class);
         if (var2 != null && var2.isEnabled()) {
            var1.cancel();
         }
      }
   }
}
