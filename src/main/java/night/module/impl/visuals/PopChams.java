package night.module.impl.visuals;

import com.mojang.authlib.GameProfile;
import java.awt.Color;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import night.events.EventTarget;
import night.events.impl.TickEvent;
import night.events.impl.TotemPopEvent;
import night.mixins.accessors.LimbAnimatorAccessor;
import night.module.Module;
import night.module.ModuleInfo;
import night.setting.impl.ColorSetting;
import night.setting.impl.ModeSetting;
import night.setting.impl.NumberSetting;
import night.util.animation.EasingUtil;
import night.util.color.ColorUtil;
import night.util.world.PacketAndRenderUtil;

@ModuleInfo(
   name = "PopChams",
   description = "Renders chams on a frozen snapshot of the entity's pose the moment they pop a totem.",
   category = Module.Category.VISUALS
)
public class PopChams extends Module {
   public NumberSetting duration = new NumberSetting("Duration", "The duration for the pop chams fade.", 1500, 0, 5000);
   public NumberSetting yOffset = new NumberSetting("YOffset", "The vertical float/rise offset for pop chams.", 0, -5, 5);
   public ModeSetting mode = new ModeSetting("Mode", "The rendering that will be applied to the pop chams.", "Both", new String[]{"Fill", "Outline", "Both"});
   public ColorSetting fillColor = new ColorSetting(
      "FillColor", "The color used for the fill rendering.", new ModeSetting.ModeCondition(this.mode, "Fill", "Both"), ColorUtil.b()
   );
   public ColorSetting outlineColor = new ColorSetting(
      "OutlineColor", "The color used for the outline rendering.", new ModeSetting.ModeCondition(this.mode, "Outline", "Both"), ColorUtil.c()
   );
   private static final EquipmentSlot[] j = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
   private final Map<RemotePlayer, Long> k = new ConcurrentHashMap<>();
   private int l = -90000;

   @EventTarget
   public void a_void_TotemPopEvent(TotemPopEvent var1) {
      if (this.isEnabled() && mc.level != null && var1.getPlayer() != mc.player) {
         Player var2 = var1.getPlayer();
         mc.execute(() -> {
            if (mc.level != null) {
               GameProfile var2x = new GameProfile(UUID.randomUUID(), "");
               RemotePlayer var3 = new RemotePlayer(mc.level, var2x);
               var3.setId(this.l--);
               var3.setPos(var2.getX(), var2.getY(), var2.getZ());
               var3.xo = var2.getX();
               var3.yo = var2.getY();
               var3.zo = var2.getZ();
               var3.xOld = var2.getX();
               var3.yOld = var2.getY();
               var3.zOld = var2.getZ();
               var3.setYRot(var2.getYRot());
               var3.setXRot(var2.getXRot());
               var3.yRotO = var2.getYRot();
               var3.xRotO = var2.getXRot();
               var3.setYHeadRot(var2.getYHeadRot());
               var3.yHeadRotO = var2.getYHeadRot();
               var3.yBodyRot = var2.yBodyRot;
               var3.yBodyRotO = var2.yBodyRot;
               var3.setPose(var2.getPose());
               var3.setShiftKeyDown(var2.isShiftKeyDown());
               var3.setSwimming(var2.isSwimming());
               var3.setSprinting(var2.isSprinting());
               var3.refreshDimensions();
               var3.walkAnimation.setSpeed(var2.walkAnimation.speed());
               ((LimbAnimatorAccessor)var3.walkAnimation).setPos(var2.walkAnimation.position());
               PacketAndRenderUtil.a(var2, var3);
               var3.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
               var3.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);

               for (EquipmentSlot var7 : j) {
                  var3.setItemSlot(var7, var2.getItemBySlot(var7).copy());
               }

               mc.level.addEntity(var3);
               this.k.put(var3, System.currentTimeMillis());
            }
         });
      }
   }

   @EventTarget
   public void a_void_TickEvent(TickEvent var1) {
      long var2 = System.currentTimeMillis();
      this.k.entrySet().removeIf(var3 -> {
         if (var2 - var3.getValue() <= this.duration.getValue().intValue()) {
            return false;
         }

         this.d(var3.getKey());
         return true;
      });
   }

   @Override
   public void onDisable() {
      this.k.keySet().forEach(this::d);
      this.k.clear();
   }

   private void d(RemotePlayer var1) {
      if (mc.level != null) {
         mc.level.removeEntity(var1.getId(), RemovalReason.DISCARDED);
      }
   }

   public boolean a_bool_Entity(Entity var1) {
      return this.k.containsKey(var1);
   }

   public boolean v() {
      return this.mode.getValue().equals("Fill") || this.mode.getValue().equals("Both");
   }

   public boolean w() {
      return this.mode.getValue().equals("Outline") || this.mode.getValue().equals("Both");
   }

   public Color a_Color_RemotePlayer(RemotePlayer var1) {
      return this.a_Color_Color_RemotePlayer(this.fillColor.getColor(), var1);
   }

   public Color b(RemotePlayer var1) {
      return this.a_Color_Color_RemotePlayer(this.outlineColor.getColor(), var1);
   }

   public float c(RemotePlayer var1) {
      if (this.yOffset.getValue().intValue() == 0) {
         return 0.0F;
      }

      Long var2 = this.k.get(var1);
      if (var2 == null) {
         return 0.0F;
      }

      int var3 = this.duration.getValue().intValue();
      if (var3 <= 0) {
         return 0.0F;
      }

      float var4 = EasingUtil.a(var2, var3);
      return this.yOffset.getValue().floatValue() * var4;
   }

   private Color a_Color_Color_RemotePlayer(Color var1, RemotePlayer var2) {
      Long var3 = this.k.get(var2);
      if (var3 == null) {
         return var1;
      }

      float var4 = 1.0F - EasingUtil.a(var3, this.duration.getValue().intValue());
      return ColorUtil.a(var1, (int)(var1.getAlpha() * var4));
   }
}
