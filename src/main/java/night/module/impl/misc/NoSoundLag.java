package night.module.impl.misc;

import com.google.common.collect.Sets;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import night.events.EventTarget;
import night.events.impl.PacketReceiveEvent;
import night.module.Module;
import night.module.ModuleInfo;
import night.setting.impl.BooleanSetting;

@ModuleInfo(name = "NoSoundLag", description = "Prevents lagging caused by a large amount of sounds being played.", category = Module.Category.MISCELLANEOUS)
public class NoSoundLag extends Module {
   public BooleanSetting armor = new BooleanSetting("Armor", "Prevents lagging caused by armor sounds.", true);
   public BooleanSetting withers = new BooleanSetting("Withers", "Prevents lagging caused by wither sounds.", true);
   public BooleanSetting ghasts = new BooleanSetting("Ghasts", "Prevents lagging caused by ghast sounds.", true);
   public BooleanSetting piston = new BooleanSetting("Piston", "Prevents lagging caused by piston sounds.", true);
   public BooleanSetting dispenser = new BooleanSetting("Dispenser", "Prevents lagging caused by dispenser sounds.", true);
   public static final Set<Holder<SoundEvent>> j;
   public static final Set<SoundEvent> k;
   public Set<SoundEvent> l;
   public static final Set<SoundEvent> m;
   public static final Set<SoundEvent> n;

   public NoSoundLag() {
      SoundEvent[] var1 = new SoundEvent[]{
         SoundEvents.GHAST_AMBIENT, SoundEvents.GHAST_DEATH, SoundEvents.GHAST_HURT, SoundEvents.GHAST_SCREAM, SoundEvents.GHAST_SHOOT, SoundEvents.GHAST_WARN
      };
      HashSet var2 = Sets.newHashSet(var1);
      this.l = var2;
   }

   @EventTarget
   public void a(PacketReceiveEvent var1) {
      label52:
      if (var1.getPacket() instanceof ClientboundSoundPacket var3) {
         label55: {
            if (this.armor.getValue()) {
               Holder var4 = var3.getSound();
               if (j.contains(var4)) {
                  break label55;
               }
            }

            if (this.withers.getValue()) {
               Object var5 = var3.getSound().value();
               if (k.contains(var5)) {
                  break label55;
               }
            }

            if (this.ghasts.getValue()) {
               Set var6 = this.l;
               Object var7 = var3.getSound().value();
               if (var6.contains(var7)) {
                  break label55;
               }
            }

            if (this.piston.getValue()) {
               Object var8 = var3.getSound().value();
               if (m.contains(var8)) {
                  break label55;
               }
            }

            if (!this.dispenser.getValue()) {
               break label52;
            }

            Object var9 = var3.getSound().value();
            if (!n.contains(var9)) {
               break label52;
            }
         }

         var1.setCancelled(true);
      }

      if (var1.getPacket() instanceof ClientboundLevelEventPacket var11
         && this.dispenser.getValue()
         && (var11.getType() == 1000 || var11.getType() == 1001 || var11.getType() == 1002)) {
         var1.setCancelled(true);
      }
   }

   static {
      Holder[] var0 = new Holder[]{
         SoundEvents.ARMOR_EQUIP_GENERIC,
         SoundEvents.ARMOR_EQUIP_ELYTRA,
         SoundEvents.ARMOR_EQUIP_DIAMOND,
         SoundEvents.ARMOR_EQUIP_IRON,
         SoundEvents.ARMOR_EQUIP_GOLD,
         SoundEvents.ARMOR_EQUIP_CHAIN,
         SoundEvents.ARMOR_EQUIP_LEATHER
      };
      j = Sets.newHashSet(var0);
      SoundEvent[] var1 = new SoundEvent[]{
         SoundEvents.WITHER_AMBIENT,
         SoundEvents.WITHER_DEATH,
         SoundEvents.WITHER_BREAK_BLOCK,
         SoundEvents.WITHER_HURT,
         SoundEvents.WITHER_SPAWN,
         SoundEvents.WITHER_SHOOT
      };
      k = Sets.newHashSet(var1);
      SoundEvent[] var2 = new SoundEvent[]{SoundEvents.PISTON_EXTEND, SoundEvents.PISTON_CONTRACT};
      m = Sets.newHashSet(var2);
      SoundEvent[] var3 = new SoundEvent[]{SoundEvents.DISPENSER_DISPENSE, SoundEvents.DISPENSER_FAIL, SoundEvents.DISPENSER_LAUNCH};
      n = Sets.newHashSet(var3);
   }
}
