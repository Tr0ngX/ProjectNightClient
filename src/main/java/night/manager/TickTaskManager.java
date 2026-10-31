package night.manager;

import java.util.ArrayList;
import night.Night;
import night.events.EventTarget;
import night.events.impl.TickEvent;
import night.util.MinecraftInstance;

public class TickTaskManager implements MinecraftInstance {
   private final ArrayList<Runnable> queuedTasks = new ArrayList<>();

   public TickTaskManager() {
      Night.EVENT_BUS.register(this);
   }

   @EventTarget
   public void a(TickEvent var1) {
      if (!this.queuedTasks.isEmpty()) {
         this.queuedTasks.getFirst().run();
         this.queuedTasks.removeFirst();
      }
   }
}
