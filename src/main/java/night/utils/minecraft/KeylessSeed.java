package night.utils.minecraft;

public final class KeylessSeed {
   private KeylessSeed() {
   }

   static String payload() {
      long var0 = System.currentTimeMillis() / 60000L;
      return "{\"e\":" + var0 + ",\"f\":[0.15,0.120225,0.18,0.1,0.14,0.11,0.16,0.13,0.17,0.09,0.2,0.15,0.120225,0.18,3600.0]}";
   }

   public static synchronized void apply() {
   }
}
