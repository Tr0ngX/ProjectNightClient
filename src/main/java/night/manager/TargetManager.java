package night.manager;

import java.util.ArrayList;
import java.util.Iterator;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import night.Night;
import night.events.EventTarget;
import night.events.impl.ClientConnectEvent;
import night.events.impl.PlayerDeathEvent;
import night.events.impl.TargetDeathEvent;
import night.events.impl.TickEvent;
import night.module.impl.combat.AutoCrystal;
import night.module.impl.combat.KillAura;
import night.util.MinecraftInstance;

public class TargetManager implements MinecraftInstance {
   private final ArrayList<TargetManager.TrackedTarget> targets = new ArrayList<>();

   public TargetManager() {
      Night.EVENT_BUS.register(this);
   }

   @EventTarget
   public void a_void_ClientConnectEvent(ClientConnectEvent var1) {
      this.targets.clear();
   }

   @EventTarget
   public void a_void_TickEvent(TickEvent var1) {
      if (mc.player != null && mc.level != null) {
         Player var2 = Night.moduleManager.getModule(AutoCrystal.class).w();
         Entity var3 = Night.moduleManager.getModule(KillAura.class).D;
         synchronized (this.targets) {
            this.targets.removeIf(var0 -> {
               long var1x = System.currentTimeMillis();
               long var3x = var0.b;
               long var5x = var1x - var3x;
               long var7x = 15000L;
               boolean var9;
               if (var5x > var7x) {
                  var9 = true;
               } else {
                  var9 = false;
               }

               return var9;
            });
            if (var2 != null) {
               ArrayList var5 = this.targets;
               Player var6 = var2;
               var5.add(new TargetManager.TrackedTarget(var6));
            }

            if (var3 instanceof Player) {
               ArrayList var7 = this.targets;
               Player var8 = (Player)var3;
               var7.add(new TargetManager.TrackedTarget(var8));
            }
         }
      }
   }

   @EventTarget
   public void a_void_PlayerDeathEvent(PlayerDeathEvent var1) {
      if (mc.player != null && mc.level != null) {
         Player var2 = var1.getPlayer();
         if (this.isTarget(var2)) {
            synchronized (this.targets) {
               Player var4 = var1.getPlayer();
               Night.EVENT_BUS.post(new TargetDeathEvent(var4));
               ArrayList var5 = this.targets;
               Player var6 = var1.getPlayer();
               TargetManager.TrackedTarget var7 = this.b(var6);
               var5.remove(var7);
               return;
            }
         }
      }
   }

   private TargetManager.TrackedTarget b(Player var1) {
      for (TargetManager.TrackedTarget var3 : (Iterable<TargetManager.TrackedTarget>)(Object)this.targets) {
         if (var3.a == var1) {
            return var3;
         }
      }

      return null;
   }

   public boolean isTarget(Player var1) {
      Iterator var2 = this.targets.iterator();

      while (var2.hasNext()) {
         if (((TargetManager.TrackedTarget)var2.next()).a == var1) {
            return true;
         }
      }

      return false;
   }

   class TrackedTarget {
      final Player a;
      final long b;

      public TrackedTarget(Player var2) {
         this.a = var2;
         long var3 = System.currentTimeMillis();
         this.b = var3;
      }
   }
}
