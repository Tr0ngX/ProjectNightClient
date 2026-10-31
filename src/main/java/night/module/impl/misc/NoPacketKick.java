package night.module.impl.misc;

import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import night.Night;
import night.module.Module;
import night.module.ModuleInfo;
import night.setting.impl.BooleanSetting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ModuleInfo(
   name = "NoPacketKick",
   description = "Prevents you from being kicked from the server due to Netty, packet decoding, or codec exceptions.",
   category = Module.Category.MISCELLANEOUS
)
public class NoPacketKick extends Module {
   private static final Logger h = LoggerFactory.getLogger("Night/NoPacketKick");
   public BooleanSetting logChat = new BooleanSetting("LogChat", "Notifies you in chat when a corrupted or invalid packet is caught and suppressed.", true);
   public BooleanSetting logConsole = new BooleanSetting("LogConsole", "Prints the full exception stacktrace to the console log.", true);
   public BooleanSetting onlyDecoder = new BooleanSetting(
      "OnlyDecoder", "Only suppresses decoder and codec errors, allowing critical socket disconnects through.", false
   );
   private long i;
   private long j;
   private int k;

   public NoPacketKick() {
      long var1 = 0L;
      this.i = var1;
      long var3 = 0L;
      this.j = var3;
      this.k = 0;
   }

   public boolean a(Throwable var1) {
      if (!this.onlyDecoder.getValue()) {
         return true;
      }

      boolean var2;
      if (!(var1 instanceof DecoderException)
         && !(var1 instanceof EncoderException)
         && !(var1 instanceof IllegalArgumentException)
         && !(var1 instanceof IndexOutOfBoundsException)
         && !(var1 instanceof NullPointerException)) {
         var2 = false;
      } else {
         var2 = true;
      }

      return var2;
   }

   public void b(Throwable var1) {
      long var2 = System.currentTimeMillis();
      int var4 = this.k + 1;
      this.k = var4;
      if (this.logConsole.getValue()) {
         long var5 = var2;
         long var7 = this.j;
         long var9 = var5 - var7;
         long var11 = 1000L;
         if (var9 > var11) {
            long var13 = var2;
            this.j = var13;
            String var15;
            if (var1.getCause() != null) {
               var15 = var1.getCause().getMessage();
            } else {
               var15 = var1.getMessage();
            }

            String var16 = var15;
            if (var16 == null || var16.isBlank()) {
               var16 = var1.getClass().getSimpleName();
            }

            Integer var17 = this.k;
            String var18 = var16;
            h.warn("[NoPacketKick] Suppressed packet exception ({} occurrences in last sec): {}", var17, var18);
            this.k = 0;
         }
      }

      if (this.logChat.getValue() && mc.player != null && mc.level != null) {
         long var19 = var2;
         long var21 = this.i;
         long var23 = var19 - var21;
         long var25 = 1000L;
         if (var23 > var25) {
            long var27 = var2;
            this.i = var27;
            String var29;
            if (var1.getCause() != null) {
               var29 = var1.getCause().getMessage();
            } else {
               var29 = var1.getMessage();
            }

            String var30 = var29;
            if (var30 == null || var30.isBlank()) {
               var30 = var1.getClass().getSimpleName();
            }

            if (var30.length() > 100) {
               var30 = var30.substring(0, 100) + "...";
            }

            String var31 = this.getName();
            Night.chatManager.sendTaggedMessage("Suppressed packet error: " + var30, var31);
         }
      }
   }
}
