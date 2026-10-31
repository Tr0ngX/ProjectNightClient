package night.module.impl.visuals;

import java.awt.Color;
import java.util.ArrayList;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec3;
import night.events.EventTarget;
import night.events.impl.PacketReceiveEvent;
import night.events.impl.Render3DEvent;
import night.module.Module;
import night.module.ModuleInfo;
import night.setting.impl.BooleanSetting;
import night.setting.impl.ColorSetting;
import night.setting.impl.ModeSetting;
import night.setting.impl.NumberSetting;
import night.setting.impl.WhitelistSetting;
import night.util.render.Render3DUtil;
import night.util.world.EntityUtil;

@ModuleInfo(name = "TextESP", description = "Renders text ESP on the world.", category = Module.Category.VISUALS)
public class TextESP extends Module {
   public BooleanSetting items = new BooleanSetting("Items", "Renders text ESP on item entities.", true);
   public ModeSetting mode = new ModeSetting(
      "Mode",
      "WhiteList = only listed items. BlackList = items NOT in list. All = render on every item.",
      new BooleanSetting.BooleanCondition(this.items, true),
      "WhiteList",
      new String[]{"WhiteList", "BlackList", "All"}
   );
   public WhitelistSetting whitelist = new WhitelistSetting(
      "Whitelist",
      "Items the WhiteList/BlackList mode compares against.",
      new BooleanSetting.BooleanCondition(this.items, true),
      WhitelistSetting.WhitelistType.ITEMS
   );
   public BooleanSetting pearls = new BooleanSetting("Pearls", "Renders text ESP on pearl entities.", true);
   public BooleanSetting chorus = new BooleanSetting("Chorus", "Renders text ESP on chorus sounds.", true);
   public NumberSetting scale = new NumberSetting("Scale", "The scaling that will be applied to the text ESP rendering.", 30, 10, 100);
   public ColorSetting color = new ColorSetting(
      "Color", "The color that will be used for the text ESP rendering.", new ColorSetting.ColorValue(Color.WHITE, false, false)
   );
   public BooleanSetting glow = new BooleanSetting("Glow", "Renders a glow shader effect around text ESP.", false);
   private final ArrayList<TextESP.TemporaryLabel> m = new ArrayList<>();

   @EventTarget
   public void a_void_PacketReceiveEvent(PacketReceiveEvent var1) {
      if (!this.nullCheck()) {
         synchronized (this.m) {
            if (var1.getPacket() instanceof ClientboundSoundPacket var4) {
               SoundEvent var7 = (SoundEvent)var4.getSound().value();
               if (var7 == SoundEvents.CHORUS_FRUIT_TELEPORT || var7 == SoundEvents.ENDERMAN_TELEPORT) {
                  this.m
                     .add(
                        new TextESP.TemporaryLabel(
                           mc.getSoundManager().getSoundEvent(var7.location()).getSubtitle().getString(), new Vec3(var4.getX(), var4.getY(), var4.getZ())
                        )
                     );
               }
            }
         }
      }
   }

   @EventTarget
   public void a_void_Render3DEvent(Render3DEvent var1) {
      if (!this.nullCheck()) {
         for (Entity var3 : (Iterable<Entity>)(Object)mc.level.entitiesForRendering()) {
            if (Render3DUtil.a_bool_AABB(var3.getBoundingBox())) {
               if (var3 instanceof ItemEntity var4 && this.items.getValue() && this.a_bool_Item(var4.getItem().getItem())) {
                  Vec3 var13 = EntityUtil.a_Vec3_Entity_float(var4, var1.getTickDelta());
                  String var6 = var4.getName().getString() + (var4.getItem().getCount() > 1 ? " x" + var4.getItem().getCount() : "");
                  Render3DUtil.a(
                     var1.getMatrices(), var6, var13.x, var13.y, var13.z, this.scale.getValue().intValue(), false, this.color.getColor(), this.glow.getValue()
                  );
               } else if (var3 instanceof ThrownEnderpearl var7 && var7.getOwner() != null && this.pearls.getValue()) {
                  Vec3 var5 = EntityUtil.a_Vec3_Entity_float(var7, var1.getTickDelta());
                  Render3DUtil.a(
                     var1.getMatrices(),
                     var7.getOwner().getName().getString(),
                     var5.x,
                     var5.y,
                     var5.z,
                     this.scale.getValue().intValue(),
                     false,
                     this.color.getColor(),
                     this.glow.getValue()
                  );
               }
            }
         }

         if (this.chorus.getValue()) {
            synchronized (this.m) {
               this.m.removeIf(var0 -> System.currentTimeMillis() - var0.c >= 1500L);

               for (TextESP.TemporaryLabel var12 : (Iterable<TextESP.TemporaryLabel>)(Object)this.m) {
                  Render3DUtil.a(
                     var1.getMatrices(),
                     var12.a,
                     var12.b.x,
                     var12.b.y,
                     var12.b.z,
                     this.scale.getValue().intValue(),
                     false,
                     this.color.getColor(),
                     this.glow.getValue()
                  );
               }
            }
         }
      }
   }

   private boolean a_bool_Item(Item var1) {
      boolean var2 = this.whitelist.contains(var1);

      return switch (this.mode.getValue()) {
         case "WhiteList" -> var2;
         case "BlackList" -> !var2;
         default -> true;
      };
   }

   class TemporaryLabel {
      final String a;
      final Vec3 b;
      final long c;

      public TemporaryLabel(String var2, Vec3 var3) {
         this.a = var2;
         this.b = var3;
         this.c = System.currentTimeMillis();
      }
   }
}
