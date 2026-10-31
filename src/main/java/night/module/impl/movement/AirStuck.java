package night.module.impl.movement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundClientTickEndPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import night.events.EventTarget;
import night.events.impl.PacketSendEvent;
import night.events.impl.PlayerMoveEvent;
import night.module.Module;
import night.module.ModuleInfo;
import night.setting.impl.BooleanSetting;
import night.util.RenderQueue;
import night.util.world.PacketAndRenderUtil;

@ModuleInfo(name = "AirStuck", description = "Suspends movement in the air by freezing movement packets.", category = Module.Category.MOVEMENT)
public class AirStuck extends Module {
   public final BooleanSetting onlyWhenFalling = new BooleanSetting("Only When Falling", "Only captures velocity while falling.", false);
   private Vec3 f = Vec3.ZERO;
   private LocalPlayer g;
   private final Runnable h = this::w;
   private final Map<Object, Runnable> i = new LinkedHashMap<>();
   private boolean enabled;
   private boolean k;

   @Override
   public void onEnable() {
      RenderQueue.a(this.h);
   }

   @Override
   public void onDisable() {
      RenderQueue.b(this.h);
      if (!this.nullCheck()) {
         LocalPlayer var1 = mc.player;
         LocalPlayer var2 = this.g;
         if (var1 == var2 && !this.f.equals(Vec3.ZERO)) {
            LocalPlayer var3 = mc.player;
            Vec3 var4 = this.f;
            var3.setDeltaMovement(var4);
         }
      }

      this.f = Vec3.ZERO;
      this.g = null;
      this.enabled = false;
      if (!this.nullCheck()) {
         this.v();
      }

      this.i.clear();
   }

   public boolean a(Object var1, Runnable var2) {
      if (!this.x()) {
         return false;
      }

      this.i.putIfAbsent(var1, var2);
      return true;
   }

   private void v() {
      if (!this.i.isEmpty()) {
         Collection var1 = this.i.values();
         ArrayList<Runnable> var2 = new ArrayList<>(var1);
         this.i.clear();
         this.k = true;

         try {
            var2.forEach(Runnable::run);
         } catch (Throwable var4) {
            this.k = false;
            throw var4;
         }

         this.k = false;
      }
   }

   private void w() {
      if (this.isEnabled() && !this.nullCheck()) {
         if (this.g != null) {
            LocalPlayer var1 = mc.player;
            LocalPlayer var2 = this.g;
            if (var1 != var2) {
               this.setEnabled(false, false);
               return;
            }
         }

         if (this.enabled) {
            this.enabled = false;
            this.v();
         } else if (!this.i.isEmpty() && !this.f.equals(Vec3.ZERO)) {
            LocalPlayer var3 = mc.player;
            Vec3 var4 = this.f;
            var3.setDeltaMovement(var4);
            this.f = Vec3.ZERO;
            this.enabled = true;
            return;
         }

         if (this.onlyWhenFalling.getValue()) {
            double var5 = mc.player.fallDistance;
            double var7 = 0.0;
            if (var5 <= var7) {
               return;
            }
         }

         if (this.f.equals(Vec3.ZERO) && !mc.player.isSprinting()) {
            Vec3 var9 = mc.player.getDeltaMovement();
            this.f = var9;
            LocalPlayer var10 = mc.player;
            this.g = var10;
         }
      }
   }

   private boolean x() {
      if (this.isEnabled() && !this.nullCheck()) {
         LocalPlayer var1 = mc.player;
         LocalPlayer var2 = this.g;
         if (var1 == var2 && !this.f.equals(Vec3.ZERO)) {
            return true;
         }
      }

      return false;
   }

   @EventTarget(priority = Integer.MAX_VALUE)
   public void a_void_PacketSendEvent(PacketSendEvent var1) {
      Packet var2 = var1.getPacket();
      if (this.k && var2 instanceof ServerboundMovePlayerPacket) {
         var1.setCancelled(true);
      } else if (this.x()) {
         if (var2 instanceof ServerboundUseItemPacket var3) {
            LocalPlayer var4 = mc.player;
            InteractionHand var5 = PacketAndRenderUtil.a_InteractionHand_ServerboundUseItemPacket(var3);
            if (var4.getItemInHand(var5).is(Items.ENDER_PEARL)) {
               var1.setCancelled(true);
               Map<Object, Runnable> var6 = this.i;
               ServerboundUseItemPacket var7 = var3;
               var6.putIfAbsent(var7, () -> mc.getConnection().send(var3));
               return;
            }
         }

         if (var2 instanceof ServerboundClientTickEndPacket || var2 instanceof ServerboundPlayerCommandPacket || var2 instanceof ServerboundMovePlayerPacket) {
            var1.setCancelled(true);
         }
      }
   }

   @EventTarget(priority = Integer.MIN_VALUE)
   public void a_void_PlayerMoveEvent(PlayerMoveEvent var1) {
      if (var1.getType() == MoverType.SELF && this.x()) {
         double var2 = 0.0;
         double var4 = -1.0E-10;
         double var6 = 0.0;
         var1.setMovement(new Vec3(var2, var4, var6));
         var1.setCancelled(true);
      }
   }
}
