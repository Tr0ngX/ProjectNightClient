package night.manager;

import com.google.gson.Gson;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Builder;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ConcurrentHashMap.KeySetView;
import net.minecraft.world.entity.player.Player;
import night.util.MinecraftInstance;

public class NightUserManager implements MinecraftInstance {
   private static final String BASE_URL = System.getProperty("night.license.baseUrl", "https://nightclient.info.vn");
   private static final Gson GSON = new Gson();
   private final HttpClient httpClient;
   private final Set<String> nightUsers;
   private final ScheduledExecutorService pollExecutor;

   public NightUserManager() {
      Builder var1 = HttpClient.newBuilder();
      Duration var2 = Duration.ofSeconds(5L);
      HttpClient var3 = var1.connectTimeout(var2).build();
      this.httpClient = var3;
      KeySetView var4 = ConcurrentHashMap.newKeySet();
      this.nightUsers = var4;
      ScheduledExecutorService var5 = Executors.newSingleThreadScheduledExecutor(var0 -> {
         Thread var1x = new Thread(var0, "Night-NightUserManager-Poll");
         var1x.setDaemon(true);
         return var1x;
      });
      this.pollExecutor = var5;
      ScheduledExecutorService var6 = this.pollExecutor;
      long var7 = 0L;
      long var9 = 30L;
      var6.scheduleWithFixedDelay(this::a, var7, var9, TimeUnit.SECONDS);
   }

   public boolean isNightUser(Player var1) {
      return true;
   }

   public boolean isNightUser(String var1) {
      return true;
   }

   private void a() {
   }

   private boolean a_bool_String_String(String var1, String var2) {
      return true;
   }
}
