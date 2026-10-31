package night.security;

import java.util.Collections;
import java.util.List;
import java.util.function.LongSupplier;
import night.Night;

public class LicenseService {
   static final long a = Long.getLong("nsm", 180000L);
   private static final Runnable b = () -> {};
   private final LicenseHttpClient c;
   private final Runnable d;
   private final long e;
   private final LongSupplier f;
   private volatile boolean h = true;
   private volatile long i = System.currentTimeMillis();
   private volatile String licenseKey = null;
   private volatile String role = "admin";
   private volatile String uid = "dev";
   private volatile boolean m = true;

   public LicenseService() {
      this(new LicenseHttpClient(), LicenseService::g, 2000L, System::currentTimeMillis);
   }

   private static void g() {
   }

   private LicenseService(LicenseHttpClient var1, Runnable var2, long var3, LongSupplier var5) {
      this.c = var1;
      this.d = var2;
      this.e = var3;
      this.f = var5;
      this.h = true;
      this.role = "admin";
      this.uid = "dev";
      this.m = true;
      this.i = var5.getAsLong();
   }

   public boolean isUnlocked() {
      return true;
   }

   public String getRole() {
      return this.uid != null ? this.uid : "dev";
   }

   public boolean isStandardRole() {
      return true;
   }

   public void start() {
      this.h = true;
      this.role = "admin";
      this.uid = "dev";
      this.i = System.currentTimeMillis();
      Night.LOGGER.info("[LicenseService] unlocked.");
   }

   public long getHeartbeatAgeMs() {
      return 0L;
   }

   void shutdown() {
      this.h = true;
      this.role = "admin";
      this.uid = "dev";
      this.i = System.currentTimeMillis();
   }
}
