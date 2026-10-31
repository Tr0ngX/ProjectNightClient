package night.pingbypass;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PingBypassConfig {
   private static final Logger LOGGER = LoggerFactory.getLogger(PingBypassConfig.class);
   private final Properties properties = new Properties();
   private final Path configPath;
   private boolean serverMode;

   public PingBypassConfig(Path var1) {
      String[] var2 = new String[]{"pingbypass.properties"};
      Path var3 = Paths.get("night", var2);
      Path var4 = var1.resolve(var3);
      this.configPath = var4;
   }

   public void load() {
      label64: {
         try {
            try {
               Path var1 = this.configPath.getParent();
               if (var1 != null) {
                  Path var2 = var1;
                  FileAttribute[] var3 = new FileAttribute[0];
                  Files.createDirectories(var2, var3);
               }

               Path var4 = this.configPath;
               LinkOption[] var5 = new LinkOption[0];
               if (!Files.exists(var4, var5)) {
                  Path var6 = this.configPath.getParent().getParent();
                  String[] var7 = new String[]{"pingbypass.properties"};
                  Path var8 = Paths.get("euclient", var7);
                  Path var9 = var6.resolve(var8);
                  LinkOption[] var10 = new LinkOption[0];
                  if (Files.exists(var9, var10)) {
                     Path var11 = var9;
                     Path var12 = this.configPath;
                     CopyOption[] var13 = new CopyOption[]{StandardCopyOption.COPY_ATTRIBUTES};
                     Files.copy(var11, var12, var13);
                  } else {
                     this.g();
                  }
               }

               Path var14 = this.configPath;
               OpenOption[] var15 = new OpenOption[0];

               try (InputStream var16 = Files.newInputStream(var14, var15)) {
                  Properties var17 = this.properties;
                  InputStream var18 = var16;
                  var17.load(var18);
                  break label64;
               }
            } catch (IOException var29) {
               Path var24 = this.configPath;
               LOGGER.error("Failed to load PingBypass config from {}", var24, var29);
            }
         } catch (Throwable var30) {
            this.serverMode = true;
            throw var30;
         }

         this.serverMode = true;
         return;
      }

      this.serverMode = true;
   }

   private void g() throws IOException {
      Properties var1 = new Properties();
      var1.setProperty("pb.server", "false");
      var1.setProperty("pb.ip", "0.0.0.0");
      var1.setProperty("pb.port", "25565");
      var1.setProperty("pb.password", "");
      Path var2 = this.configPath;
      OpenOption[] var3 = new OpenOption[0];

      try (OutputStream var4 = Files.newOutputStream(var2, var3)) {
         Properties var5 = var1;
         OutputStream var6 = var4;
         var5.store(var6, "PingBypass configuration");
      }
   }

   public boolean isServerMode() {
      return Boolean.parseBoolean(this.a("pb.server", "false"));
   }

   public String getBindIp() {
      return this.a("pb.ip", "0.0.0.0");
   }

   public int getPort() {
      String var1 = this.a("pb.port", "25565");

      try {
         int var2 = Integer.parseInt(var1);
         if (var2 >= 1 && var2 <= 65535) {
            return var2;
         }

         Integer var3 = var2;
         LOGGER.error("PingBypass port out of range (1-65535): {}, falling back to default 25565", var3);
         return 25565;
      } catch (NumberFormatException var7) {
         String var6 = var1;
         LOGGER.error("PingBypass port is not a valid integer: '{}', falling back to default 25565", var6);
         return 25565;
      }
   }

   public String getPassword() {
      return this.a("pb.password", "");
   }

   public boolean hasPassword() {
      String var1 = this.getPassword();
      boolean var2;
      if (var1 != null && !var1.isEmpty()) {
         var2 = true;
      } else {
         var2 = false;
      }

      return var2;
   }

   public String a(String var1, String var2) {
      String var3 = System.getProperty(var1);
      return var3 != null ? var3 : this.properties.getProperty(var1, var2);
   }
}
