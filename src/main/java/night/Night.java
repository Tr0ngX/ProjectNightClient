package night;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import night.command.CommandManager;
import night.events.EventBus;
import night.gui.ClickGuiScreen;
import night.manager.AccountManager;
import night.manager.ChatManager;
import night.manager.ConfigManager;
import night.manager.CustomImageManager;
import night.manager.FontManager;
import night.manager.FriendManager;
import night.manager.MacroManager;
import night.manager.NightUserManager;
import night.manager.PositionManager;
import night.manager.RenderPlacementManager;
import night.manager.RotationManager;
import night.manager.ServerManager;
import night.manager.SpeedManager;
import night.manager.TargetManager;
import night.manager.TickTaskManager;
import night.manager.WaypointManager;
import night.manager.WorldStateManager;
import night.module.ModuleManager;
import night.pingbypass.PingBypassConfig;
import night.pingbypass.PingBypassServer;
import night.pingbypass.module.ProxyModuleManager;
import night.pingbypass.module.impl.ProxyAutoCrystal;
import night.pingbypass.module.impl.ProxyAutoTotem;
import night.pingbypass.network.BackendTransferRewirer;
import night.pingbypass.network.RubberbandTracker;
import night.pingbypass.protocol.PingBypassPayload;
import night.security.LicenseService;
import night.utils.minecraft.ImageShaderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Night implements ModInitializer {
   public static final long START_TIME = System.currentTimeMillis();
   public static final EventBus EVENT_BUS = new EventBus();
   public static final Logger LOGGER = LoggerFactory.getLogger("Night");
   private static final ExecutorService UPDATE_EXECUTOR = Executors.newSingleThreadExecutor();
   public static String updateStatus = "none";
   public static ChatManager chatManager;
   public static FontManager fontManager;
   public static FriendManager friendManager;
   public static WorldStateManager worldStateManager;
   public static PositionManager positionManager;
   public static RotationManager rotationManager;
   public static ServerManager serverManager;
   public static RenderPlacementManager renderPlacementManager;
   public static TargetManager targetManager;
   public static MacroManager macroManager;
   public static TickTaskManager tickTaskManager;
   public static WaypointManager waypointManager;
   public static SpeedManager speedManager;
   public static NightUserManager nightUserManager;
   public static CustomImageManager customImageManager;
   public static ModuleManager moduleManager;
   public static CommandManager commandManager;
   public static ConfigManager configManager;
   public static AccountManager accountManager;
   public static LicenseService licenseService;
   public static ClickGuiScreen clickGui;
   public static PingBypassConfig pingBypassConfig;
   public static PingBypassServer pingBypassServer;
   public static final ProxyModuleManager proxyModuleManager = new ProxyModuleManager();

   public void onInitialize() {
      ImageShaderUtil.c();
      System.setProperty("java.awt.headless", "false");

      try {
         licenseService = new LicenseService();
         licenseService.start();
      } catch (Throwable var15) {
         LOGGER.error("Failed to start LicenseService", var15);
      }

      try {
         PayloadTypeRegistry.serverboundPlay().register(PingBypassPayload.TYPE, PingBypassPayload.CODEC);
         PayloadTypeRegistry.clientboundPlay().register(PingBypassPayload.TYPE, PingBypassPayload.CODEC);
         ClientPlayNetworking.registerGlobalReceiver(PingBypassPayload.TYPE, (var0, var1) -> {});
      } catch (Throwable var14) {
         String var2 = var14.getMessage();
         LOGGER.warn("Fabric Networking API not available or optional: {}", var2);
      }

      try {
         Path var3 = FabricLoader.getInstance().getGameDir();
         pingBypassConfig = new PingBypassConfig(var3);
         pingBypassConfig.load();
         Logger var4 = LOGGER;
         String var5;
         if (pingBypassConfig.isServerMode()) {
            var5 = "server";
         } else {
            var5 = "client";
         }

         var4.info("PingBypass mode: {}", var5);
         if (pingBypassConfig.isServerMode()) {
            try {
               pingBypassServer = new PingBypassServer(pingBypassConfig);
               InetAddress var6 = InetAddress.getByName(pingBypassConfig.getBindIp());
               int var7 = pingBypassConfig.getPort();
               pingBypassServer.bind(var6, var7);
               EVENT_BUS.register(new RubberbandTracker(pingBypassServer));
               new BackendTransferRewirer();
               Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                  if (pingBypassServer != null) {
                     pingBypassServer.shutdown();
                  }
               }, "PingBypass-Shutdown"));
               String var8 = pingBypassConfig.getBindIp();
               Integer var9 = pingBypassConfig.getPort();
               LOGGER.info("PingBypass proxy server started on {}:{}", var8, var9);
            } catch (IOException var12) {
               LOGGER.error("Failed to start PingBypass proxy server", var12);
            }
         }
      } catch (Throwable var13) {
         LOGGER.error("Failed to initialize PingBypass config/server", var13);
      }

      initManagers();
   }

   public static synchronized void initManagers() {
      if (chatManager == null) {
         chatManager = new ChatManager();
         fontManager = new FontManager();
         friendManager = new FriendManager();
         worldStateManager = new WorldStateManager();
         positionManager = new PositionManager();
         rotationManager = new RotationManager();
         serverManager = new ServerManager();
         renderPlacementManager = new RenderPlacementManager();
         targetManager = new TargetManager();
         macroManager = new MacroManager();
         tickTaskManager = new TickTaskManager();
         waypointManager = new WaypointManager();
         speedManager = new SpeedManager();
         nightUserManager = new NightUserManager();
         customImageManager = new CustomImageManager();
         moduleManager = new ModuleManager();
         commandManager = new CommandManager();
         accountManager = new AccountManager();
         if (pingBypassServer != null) {
            proxyModuleManager.register(new ProxyAutoCrystal());
            proxyModuleManager.register(new ProxyAutoTotem());
         }
      }
   }

   public static void postInit() {
      initManagers();
      configManager = new ConfigManager();
      clickGui = new ClickGuiScreen();
      if (customImageManager != null) {
         customImageManager.registerTextures();
         String var0 = CustomImageManager.readSavedImageName();
         if (var0 != null && !var0.equalsIgnoreCase("None") && !var0.isEmpty()) {
            String var1 = var0;
            customImageManager.setActiveImage(var1);
         }
      }

      LOGGER.info("{} {} has been initialized.", "Night", "v26");
   }

   public static void checkForUpdates() {
      UPDATE_EXECUTOR.submit(() -> {
         if (!FabricLoader.getInstance().isDevelopmentEnvironment()) {
            if (FabricLoader.getInstance().isModLoaded("night-updater")) {
               try {
                  HttpURLConnection var0 = (HttpURLConnection)new URL("").openConnection();
                  var0.setRequestMethod("GET");
                  var0.connect();
                  if (var0.getResponseCode() == 200) {
                     InputStream var1 = var0.getInputStream();
                     JsonObject var2 = JsonParser.parseReader(new InputStreamReader(var1)).getAsJsonObject();
                     if (!var2.has("version")) {
                        return;
                     }

                     String var3 = var2.get("version").getAsString();
                     if (!"v26".equalsIgnoreCase(var3)) {
                        updateStatus = "update-available";
                     }
                  } else {
                     updateStatus = "failed-connection";
                  }
               } catch (IOException var5) {
                  updateStatus = "failed";
               }

               if (updateStatus.equalsIgnoreCase("none")) {
                  updateStatus = "up-to-date";
               }
            }
         }
      });
   }
}
