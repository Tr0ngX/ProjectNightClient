package night.mixins;

import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import night.Night;
import night.module.impl.visuals.Chams;
import night.module.impl.visuals.LogoutSpot;
import night.module.impl.visuals.NameTags;
import night.module.impl.visuals.NoRender;
import night.module.impl.visuals.PopChams;
import night.module.impl.visuals.ShadersModule;
import night.util.duck.ChamsRenderStateDuck;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin<T extends Entity, S extends EntityRenderState> {
   @Inject(method = "getNameTag", at = @At("HEAD"), cancellable = true)
   private void getDisplayName(T var1, CallbackInfoReturnable<Component> var2) {
      if (var1 instanceof RemotePlayer var3) {
         PopChams var4 = Night.moduleManager.getModule(PopChams.class);
         if (var4 != null && var4.isEnabled() && var4.a_bool_Entity(var3)) {
            var2.setReturnValue(null);
            return;
         }

         LogoutSpot var5 = Night.moduleManager.getModule(LogoutSpot.class);
         if (var5 != null && var5.isEnabled() && var5.a_bool_Entity(var3)) {
            var2.setReturnValue(null);
            return;
         }
      }

      if (var1 instanceof Player && Night.moduleManager.getModule(NameTags.class).isEnabled()) {
         var2.setReturnValue(null);
      }
   }

   @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
   private void night$limitItemRendering(T var1, Frustum var2, double var3, double var5, double var7, CallbackInfoReturnable<Boolean> var9) {
      if (var1 instanceof RemotePlayer var10) {
         PopChams var11 = Night.moduleManager != null ? Night.moduleManager.getModule(PopChams.class) : null;
         if (var11 != null && var11.isEnabled() && var11.a_bool_Entity(var10)) {
            var9.setReturnValue(true);
            return;
         }
      }

      NoRender var12 = Night.moduleManager != null ? Night.moduleManager.getModule(NoRender.class) : null;
      if (var12 != null && var12.isEnabled()) {
         if (var12.items.getValue() && var1 instanceof ItemEntity && !var12.w()) {
            var9.setReturnValue(false);
         }

         if (var12.displays.getValue() && var1 instanceof Display) {
            var9.setReturnValue(false);
         }

         if (var12.armorStand.getValue() && var1 instanceof ArmorStand) {
            var9.setReturnValue(false);
         }

         if (var12.minecart.getValue() && var1 instanceof AbstractMinecart) {
            var9.setReturnValue(false);
         }
      }
   }

   @Inject(method = "extractRenderState", at = @At("TAIL"))
   private void night$chams(T var1, S var2, float var3, CallbackInfo var4) {
      ChamsRenderStateDuck var5 = (ChamsRenderStateDuck)var2;
      var5.night$setChams(false, 0, false, 0, false);
      var5.night$setChamsYOffset(0.0F);
      PopChams var6 = Night.moduleManager.getModule(PopChams.class);
      LogoutSpot var7 = Night.moduleManager.getModule(LogoutSpot.class);
      boolean var9 = var1 instanceof RemotePlayer var8 && var6 != null && var6.a_bool_Entity(var8);
      boolean var13 = var1 instanceof RemotePlayer var10 && var7 != null && var7.a_bool_Entity(var10);
      boolean var14 = var9 || var13;
      if (!var14) {
         Chams var11 = Night.moduleManager.getModule(Chams.class);
         if (var11.isEnabled()) {
            if (var1 instanceof LivingEntity var12 && var11.b_bool_Entity(var12)) {
               var11.a(var12, var5);
            } else if (var11.crystals.getValue() && var1 instanceof EndCrystal && var11.a_bool_Entity(var1)) {
               var11.a_void_ChamsRenderStateDuck(var5);
            } else if (var11.vehicles.getValue() && var11.a_bool_Entity(var1) && (var1 instanceof AbstractBoat || var1 instanceof AbstractMinecart)) {
               var11.b_void_ChamsRenderStateDuck(var5);
            }
         }

         ShadersModule var17 = Night.moduleManager.getModule(ShadersModule.class);
         if (var17.isEnabled() && var17.b_bool_Entity(var1)) {
            var2.outlineColor = var17.c(var1);
         }
      }

      if (var6 != null && var6.isEnabled() && var9) {
         RemotePlayer var15 = (RemotePlayer)var1;
         var5.night$setChams(var6.v(), var6.a_Color_RemotePlayer(var15).getRGB(), var6.w(), var6.b(var15).getRGB(), false, true);
         var5.night$setChamsYOffset(var6.c(var15));
      }

      if (var7 != null && var7.isEnabled() && var13) {
         RemotePlayer var16 = (RemotePlayer)var1;
         var5.night$setChams(var7.w(), var7.b_Color_RemotePlayer(var16).getRGB(), var7.x(), var7.c_Color_RemotePlayer(var16).getRGB(), false, true);
      }
   }
}
