package night.mixins;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import night.Night;
import night.module.impl.core.NoMiddleClick;
import night.module.impl.misc.ShulkerInfo;
import night.module.impl.movement.ElytraFly;
import night.module.impl.movement.InventoryControl;
import night.module.impl.player.ChestStealer;
import night.util.MinecraftInstance;
import night.util.inventory.InventoryUtil;
import night.util.world.PacketAndRenderUtil;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(AbstractContainerScreen.class)
public abstract class HandledScreenMixin extends Screen implements MinecraftInstance {
   @Shadow
   @Nullable
   protected Slot hoveredSlot;
   @Shadow
   protected AbstractContainerMenu menu;
   @Shadow
   protected int leftPos;
   @Shadow
   protected int topPos;
   @Shadow
   protected int imageWidth;
   private Slot night$lastDragSlot = null;

   @Shadow
   protected abstract void slotClicked(Slot var1, int var2, int var3, ContainerInput var4);

   protected HandledScreenMixin(Component var1) {
      super(var1);
   }

   @Inject(method = "getTooltipFromContainerItem", at = @At("RETURN"), cancellable = true)
   private void night$trimShulkerName(ItemStack var1, CallbackInfoReturnable<List<Component>> var2) {
      ShulkerInfo var3 = Night.moduleManager.getModule(ShulkerInfo.class);
      if (var3 != null && var3.isEnabled() && var3.a(var1)) {
         List var4 = (List)var2.getReturnValue();
         if (var4 != null && var4.size() > 1) {
            var2.setReturnValue(new ArrayList(var4.subList(1, var4.size())));
         }
      }
   }

   @ModifyArgs(
      method = "extractTooltip",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;setTooltipForNextFrame(Lnet/minecraft/client/gui/Font;Ljava/util/List;Ljava/util/Optional;IILnet/minecraft/resources/Identifier;)V"
      )
   )
   private void night$pushVanillaTooltipBelowShulkerInfo(Args var1) {
      ShulkerInfo var2 = Night.moduleManager.getModule(ShulkerInfo.class);
      if (var2 != null && var2.isEnabled()) {
         if (this.hoveredSlot != null && !this.hoveredSlot.getItem().isEmpty() && var2.a(this.hoveredSlot.getItem())) {
            int var3 = (Integer)var1.get(4);
            var1.set(4, var3 + 20);
         }
      }
   }

   @Inject(method = "mouseDragged", at = @At("HEAD"), cancellable = true)
   private void night$dragClick(MouseButtonEvent var1, double var2, double var4, CallbackInfoReturnable<Boolean> var6) {
      InventoryControl var7 = Night.moduleManager.getModule(InventoryControl.class);
      if (var7.isEnabled() && var7.dragClick.getValue()) {
         boolean var8 = PacketAndRenderUtil.a_bool_int(340) || PacketAndRenderUtil.a_bool_int(344);
         if (var1.button() == 0 && var8) {
            if (this.hoveredSlot != null && !this.hoveredSlot.getItem().isEmpty() && this.hoveredSlot != (Object)(Object)this.night$lastDragSlot) {
               this.night$lastDragSlot = this.hoveredSlot;
               this.slotClicked(this.hoveredSlot, this.hoveredSlot.index, 0, ContainerInput.QUICK_MOVE);
               var6.setReturnValue(true);
               var6.cancel();
            }
         }
      }
   }

   @Inject(method = "mouseReleased", at = @At("HEAD"))
   private void night$dragClickReset(MouseButtonEvent var1, CallbackInfoReturnable<Boolean> var2) {
      this.night$lastDragSlot = null;
   }

   @Inject(method = "init", at = @At("TAIL"))
   private void night$chestStealerButtons(CallbackInfo var1) {
      if (Night.moduleManager != null) {
         ChestStealer var2 = Night.moduleManager.getModule(ChestStealer.class);
         if (var2 != null && var2.isEnabled() && var2.buttonMode.getValue()) {
            boolean var3 = (Object)(Object)this instanceof InventoryScreen;
            boolean var4 = (Object)(Object)this instanceof CraftingScreen;
            if (var3) {
               int var5 = this.leftPos + this.imageWidth - 60;
               int var6 = this.topPos - 22;
               this.addRenderableWidget(Button.builder(Component.literal("Drop"), var1x -> var2.y()).bounds(var5, var6, 60, 18).build());
            } else if (var4) {
               byte var9 = 46;
               byte var11 = 18;
               int var7 = this.leftPos + this.imageWidth - var9;
               int var8 = this.topPos - 22;
               this.addRenderableWidget(Button.builder(Component.literal("Drop"), var1x -> var2.y()).bounds(var7, var8, var9, var11).build());
               var7 -= var9 + 3;
               this.addRenderableWidget(Button.builder(Component.literal("Dump"), var1x -> var2.x()).bounds(var7, var8, var9, var11).build());
               var7 -= var9 + 3;
               this.addRenderableWidget(Button.builder(Component.literal("Steal"), var1x -> var2.w()).bounds(var7, var8, var9, var11).build());
            } else {
               int var10 = this.leftPos + this.imageWidth + 4;
               int var12 = this.topPos;
               this.addRenderableWidget(Button.builder(Component.literal("Steal"), var1x -> var2.w()).bounds(var10, var12, 60, 20).build());
               this.addRenderableWidget(Button.builder(Component.literal("Dump"), var1x -> var2.x()).bounds(var10, var12 + 22, 60, 20).build());
               this.addRenderableWidget(Button.builder(Component.literal("Drop"), var1x -> var2.y()).bounds(var10, var12 + 44, 60, 20).build());
            }
         }
      }
   }

   @Redirect(method = "extractSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/Slot;getItem()Lnet/minecraft/world/item/ItemStack;"))
   private ItemStack night$grimSwapVisual(Slot var1) {
      ItemStack var2 = var1.getItem();
      if (Night.moduleManager == null) {
         return var2;
      }

      ElytraFly var3 = Night.moduleManager.getModule(ElytraFly.class);
      int var4 = var3.L();
      if (var4 == -1) {
         return var2;
      }

      if (var1.index == 6) {
         ItemStack var5 = var3.N();
         if (var5 != null) {
            return var5;
         }
      }

      return var1.index == InventoryUtil.e(var4) ? var3.M() : var2;
   }

   @Inject(method = "slotClicked(Lnet/minecraft/world/inventory/Slot;IILnet/minecraft/world/inventory/ContainerInput;)V", at = @At("HEAD"), cancellable = true)
   private void night$onSlotClicked(Slot var1, int var2, int var3, ContainerInput var4, CallbackInfo var5) {
      if (var4 == ContainerInput.CLONE && Night.moduleManager != null) {
         NoMiddleClick var6 = Night.moduleManager.getModule(NoMiddleClick.class);
         if (var6 != null && var6.isEnabled()) {
            var5.cancel();
         }
      }
   }
}
