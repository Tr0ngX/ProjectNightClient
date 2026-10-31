package night.module.impl.misc;

import lombok.Generated;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundCommandSuggestionsPacket;
import net.minecraft.network.protocol.game.ServerboundCommandSuggestionPacket;
import night.Night;
import night.events.EventTarget;
import night.events.impl.PacketReceiveEvent;
import night.events.impl.TickEvent;
import night.module.Module;
import night.module.ModuleInfo;
import night.setting.impl.BooleanSetting;
import night.setting.impl.NumberSetting;
import night.util.color.ChatColorUtil;
import night.util.math.NanoTimer;
import night.util.math.TimerUtil;

@ModuleInfo(name = "FastLatency", description = "Makes ping resolving much faster.", category = Module.Category.MISCELLANEOUS)
public class FastLatency extends Module {
   public NumberSetting delay;
   public BooleanSetting spikeNotifier;
   public NumberSetting threshold;
   private final TimerUtil h;
   private final NanoTimer i;
   private long j;
   private int k;

   public FastLatency() {
      Long var1 = 100L;
      Integer var2 = 0;
      Long var3 = 1000L;
      this.delay = new NumberSetting("Delay", "The amount of milliseconds that have to be waited for before resolving your ping again.", var1, var2, var3);
      this.spikeNotifier = new BooleanSetting("SpikeNotifier", "Notifies you in chat whenever your ping spikes.", false);
      BooleanSetting var5 = this.spikeNotifier;
      BooleanSetting.BooleanCondition var4 = new BooleanSetting.BooleanCondition(var5, true);
      Integer var6 = 30;
      Integer var7 = 0;
      Integer var8 = 1000;
      this.threshold = new NumberSetting(
         "Threshold", "The amount of milliseconds that your ping has to increase for before notifying you.", var4, var6, var7, var8
      );
      this.h = new TimerUtil();
      this.i = new NanoTimer();
   }

   @EventTarget
   public void a(TickEvent var1) {
      if (mc.player != null) {
         NanoTimer var2 = this.i;
         long var3 = 1000L;
         if (var2.passedNs(var3)) {
            TimerUtil var5 = this.h;
            Long var6 = this.delay.getValue().longValue();
            if (var5.passedMs(var6)) {
               mc.getConnection().send(new ServerboundCommandSuggestionPacket(1000, "/w "));
               long var7 = System.currentTimeMillis();
               this.j = var7;
               this.i.clear();
               this.h.reset();
            }
         }
      }
   }

   @EventTarget
   public void a(PacketReceiveEvent var1) {
      Packet var2 = var1.getPacket();
      if (var2 instanceof ClientboundCommandSuggestionsPacket && ((ClientboundCommandSuggestionsPacket)var2).id() == 1000) {
         long var3 = System.currentTimeMillis();
         long var5 = this.j;
         int var7 = (int)(var3 - var5);
         if (this.spikeNotifier.getValue()) {
            int var8 = var7;
            int var9 = this.k;
            int var10 = var8 - var9;
            int var11 = this.threshold.getValue().intValue();
            if (var10 > var11) {
               String var12 = String.valueOf(ChatColorUtil.a());
               int var13 = var7;
               String var14 = String.valueOf(ChatColorUtil.b());
               String var15 = String.valueOf(ChatColorUtil.a());
               int var16 = this.k;
               String var17 = String.valueOf(ChatColorUtil.b());
               Night.chatManager
                  .sendReplaceableMessage(
                     "Your ping has spiked to " + var12 + var13 + "ms" + var14 + " from " + var15 + var16 + "ms" + var17 + "!",
                     "module-" + this.getName().toLowerCase() + "-spike"
                  );
            }
         }

         int var18 = var7;
         this.k = var18;
         this.i.reset();
      }
   }

   @Override
   public String getDisplayInfo() {
      return this.k + "ms";
   }

   @Generated
   public int v() {
      return this.k;
   }
}
