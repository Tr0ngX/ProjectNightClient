package night.module.impl.core;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.net.http.HttpClient.Builder;
import java.net.http.WebSocket.Listener;
import java.time.Duration;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import night.Night;
import night.events.EventTarget;
import night.events.impl.ChatSendEvent;
import night.module.Module;
import night.module.ModuleInfo;
import night.security.LicenseCacheStorage;
import night.setting.impl.BooleanSetting;
import night.setting.impl.ModeSetting;
import night.setting.impl.StringSetting;
import night.util.color.ColorUtil;
import night.util.color.TextFormattingUtil;

@ModuleInfo(
   name = "IRC",
   description = "Internet Relay Chat with other Night Client users.",
   category = Module.Category.CORE,
   enabled = true,
   persistent = true,
   drawn = false
)
public class IRCModule extends Module {
   private static final Gson j = new Gson();
   private static final String[] k;
   public StringSetting prefix = new StringSetting("Prefix", "The prefix character for IRC messages in chat.", "$");
   public BooleanSetting showPrefix = new BooleanSetting("ShowPrefix", "Show [Night] tag on IRC messages.", true);
   public ModeSetting primaryColor = new ModeSetting("PrimaryColor", "Color of username and tag.", "Client", TextFormattingUtil.a);
   public ModeSetting secondaryColor = new ModeSetting("SecondaryColor", "Color of IRC message text.", "White", TextFormattingUtil.a);
   public BooleanSetting notifyConnect = new BooleanSetting("NotifyConnect", "Show connection status notifications in chat.", false);
   private WebSocket l;
   volatile boolean m = false;
   private volatile String n = null;
   private final ScheduledExecutorService o;
   private long p;
   private static final long q = 3000L;

   public IRCModule() {
      ScheduledExecutorService var1 = Executors.newSingleThreadScheduledExecutor(var0 -> {
         Thread var1x = new Thread(var0, "Night-IRC-Service");
         var1x.setDaemon(true);
         return var1x;
      });
      this.o = var1;
      long var2 = 0L;
      this.p = var2;
      this.v();
   }

   @Override
   public void onEnable() {
      if (!this.m && (this.l == null || this.l.isInputClosed() || this.l.isOutputClosed())) {
         this.v();
      }
   }

   @Override
   public void onDisable() {
      this.w();
   }

   public synchronized void v() {
      if (!this.m || this.l == null || this.l.isOutputClosed()) {
         this.o.execute(this::x);
      }
   }

   private void x() {
      try {
         String var1 = LicenseCacheStorage.a();
         if (var1 == null || var1.isBlank()) {
            long var2 = 5L;
            this.a_void_long(var2);
            return;
         }

         Builder var4 = HttpClient.newBuilder();
         Duration var5 = Duration.ofSeconds(6L);
         HttpClient var6 = var4.connectTimeout(var5).build();
         String[] var7 = k;
         int var8 = var7.length;
         int var9 = 0;

         while (true) {
            int var10 = var9;
            int var11 = var8;
            if (var10 >= var11) {
               long var22 = 5L;
               this.a_void_long(var22);
               break;
            }

            String[] var12 = var7;
            int var13 = var9;
            String var14 = var12[var13];

            try {
               String var15 = var1;
               java.net.http.WebSocket.Builder var16 = var6.newWebSocketBuilder();
               Duration var17 = Duration.ofSeconds(6L);
               java.net.http.WebSocket.Builder var18 = var16.connectTimeout(var17);
               URI var19 = URI.create(var14 + "?license_key=" + var15);
               WebSocket var20 = var18.buildAsync(var19, new IRCModule.IRCWebSocketListener()).join();
               this.l = var20;
               return;
            } catch (Exception var27) {
               var9++;
            }
         }
      } catch (Exception var28) {
         long var25 = 5L;
         this.a_void_long(var25);
      }
   }

   void a_void_long(long var1) {
      this.m = false;
      if (this.isEnabled()) {
         ScheduledExecutorService var3 = this.o;
         long var4 = var1;
         var3.schedule(this::v, var4, TimeUnit.SECONDS);
      }
   }

   public synchronized void w() {
      this.m = false;
      if (this.l != null) {
         try {
            this.l.sendClose(1000, "Module disabled");
         } catch (Exception var2) {
         }

         this.l = null;
      }
   }

   @EventTarget
   public void a_void_ChatSendEvent(ChatSendEvent var1) {
      if (this.isEnabled()) {
         String var2 = var1.getMessage();
         if (var2 != null) {
            String var3 = this.prefix.getValue();
            if (var3 == null || var3.isEmpty()) {
               var3 = "$";
            }

            String var4 = var2;
            String var5 = var3;
            if (var4.startsWith(var5)) {
               var1.setCancelled(true);
               String var6 = var2;
               int var7 = var3.length();
               String var8 = var6.substring(var7).trim();
               if (var8.isEmpty()) {
                  Night.chatManager.sendWarning("Usage: " + var3 + "<message>");
                  return;
               }

               String var9 = var8;
               this.b(var9);
            }
         }
      }
   }

   public void b(String var1) {
      if (this.m && this.l != null && !this.l.isOutputClosed()) {
         long var2 = System.currentTimeMillis();
         long var4 = this.p;
         long var6 = var2 - var4;
         long var8 = 3000L;
         if (var6 < var8) {
            long var10 = 3000L;
            long var12 = var6;
            double var14 = var10 - var12;
            double var16 = 1000.0;
            double var18 = var14 / var16;
            Object[] var20 = new Object[1];
            Double var21 = var18;
            var20[0] = var21;
            String var22 = String.format("Please wait %.1fs before sending another message", var20);
            Night.chatManager.sendWarning(var22);
         } else {
            try {
               JsonObject var23 = new JsonObject();
               var23.addProperty("type", "chat");
               var23.addProperty("content", var1);
               WebSocket var24 = this.l;
               String var25 = var23.toString();
               var24.sendText(var25, true);
               long var26 = var2;
               this.p = var26;
            } catch (Exception var31) {
               Night.chatManager.sendError("Failed to send IRC message: " + var31.getMessage());
               long var29 = 3L;
               this.a_void_long(var29);
            }
         }
      } else {
         Night.chatManager.sendWarning("IRC is currently not connected to server. Reconnecting...");
         this.v();
      }
   }

   void c(String var1) {
      try {
         JsonObject var2 = (JsonObject)j.fromJson(var1, JsonObject.class);
         if (var2 == null || !var2.has("type")) {
            return;
         }

         String var3 = var2.get("type").getAsString();
         if ("auth_success".equals(var3)) {
            this.m = true;
            String var4 = var2.get("username").getAsString();
            this.n = var4;
            if (this.notifyConnect.getValue()) {
               Night.chatManager.sendInfo("Connected to Night IRC as " + this.n);
            }
         } else {
            String var5 = var3;
            if ("chat".equals(var5)) {
               String var6 = var2.get("sender").getAsString();
               String var7 = var2.get("content").getAsString();
               String var8;
               if (var2.has("role") && !var2.get("role").isJsonNull()) {
                  var8 = var2.get("role").getAsString();
               } else {
                  var8 = "stable";
               }

               String var9 = var8;
               MutableComponent var10 = Component.empty();
               if (this.showPrefix.getValue()) {
                  MutableComponent var11 = var10;
                  MutableComponent var12 = Component.literal("[").withStyle(ChatFormatting.DARK_GRAY);
                  MutableComponent var13 = var11.append(var12);
                  MutableComponent var14 = Component.literal("Night");
                  Style var15 = e(this.primaryColor.getValue());
                  MutableComponent var16 = var14.withStyle(var15);
                  MutableComponent var17 = var13.append(var16);
                  MutableComponent var18 = Component.literal("] ").withStyle(ChatFormatting.DARK_GRAY);
                  var17.append(var18);
               }

               MutableComponent var19 = var10;
               String var20 = var9;
               Component var21 = this.d(var20);
               var19.append(var21);
               MutableComponent var22 = var10;
               MutableComponent var23 = Component.literal(var6);
               Style var24 = e(this.primaryColor.getValue());
               MutableComponent var25 = var23.withStyle(var24);
               MutableComponent var26 = var22.append(var25);
               MutableComponent var27 = Component.literal(": ").withStyle(ChatFormatting.DARK_GRAY);
               var26.append(var27);
               MutableComponent var28 = var10;
               MutableComponent var29 = Component.literal(var7);
               Style var30 = e(this.secondaryColor.getValue());
               MutableComponent var31 = var29.withStyle(var30);
               var28.append(var31);
               MutableComponent var32 = var10;
               Night.chatManager.addComponentMessage(var32);
            } else {
               String var33 = var3;
               if ("error".equals(var33)) {
                  String var34;
                  if (var2.has("message")) {
                     var34 = var2.get("message").getAsString();
                  } else {
                     var34 = var2.get("code").getAsString();
                  }

                  String var35 = var34;
                  Night.chatManager.sendWarning("IRC: " + var35);
               }
            }
         }
      } catch (Exception var37) {
      }
   }

   private Component d(String var1) {
      if (var1 == null || var1.isBlank()) {
         var1 = "stable";
      }

      String var2 = var1.toLowerCase();
      if (var2.equals("user") || var2.equals("member")) {
         var2 = "stable";
      }
      TextColor var6 = switch (var2) {
         case "admin", "owner" -> TextColor.fromRgb(15277667);
         case "beta", "tester", "vip" -> TextColor.fromRgb(15277667);
         case "stable" -> TextColor.fromRgb(12616956);
         case "dev", "developer" -> TextColor.fromRgb(5636095);
         case "moderator", "mod" -> TextColor.fromRgb(5635925);
         default -> TextColor.fromRgb(12616956);
      };
      MutableComponent var7 = Component.literal("<").withStyle(ChatFormatting.DARK_GRAY);
      MutableComponent var8 = Component.literal(var2);
      TextColor var9 = var6;
      Style var10 = Style.EMPTY.withColor(var9);
      MutableComponent var11 = var8.withStyle(var10);
      MutableComponent var12 = var7.append(var11);
      MutableComponent var13 = Component.literal("> ").withStyle(ChatFormatting.DARK_GRAY);
      return var12.append(var13);
   }

   private static Style e(String var0) {
      if (var0 == null) {
         return Style.EMPTY;
      }

      if (!"client".equalsIgnoreCase(var0) && !"rainbow".equalsIgnoreCase(var0)) {
         ChatFormatting var5 = switch (var0.toLowerCase()) {
            case "black" -> ChatFormatting.BLACK;
            case "blue" -> ChatFormatting.BLUE;
            case "dark blue" -> ChatFormatting.DARK_BLUE;
            case "green" -> ChatFormatting.GREEN;
            case "dark green" -> ChatFormatting.DARK_GREEN;
            case "aqua" -> ChatFormatting.AQUA;
            case "dark aqua" -> ChatFormatting.DARK_AQUA;
            case "red" -> ChatFormatting.RED;
            case "dark red" -> ChatFormatting.DARK_RED;
            case "light purple" -> ChatFormatting.LIGHT_PURPLE;
            case "dark purple" -> ChatFormatting.DARK_PURPLE;
            case "yellow" -> ChatFormatting.YELLOW;
            case "gold" -> ChatFormatting.GOLD;
            case "gray" -> ChatFormatting.GRAY;
            case "dark gray" -> ChatFormatting.DARK_GRAY;
            default -> ChatFormatting.WHITE;
         };
         return Style.EMPTY.withColor(var5);
      } else {
         TextColor var1 = TextColor.fromRgb(ColorUtil.d().getRGB());
         return Style.EMPTY.withColor(var1);
      }
   }

   static {
      String[] var0 = new String[]{"wss://nightclient.info.vn/irc", "ws://nightclient.info.vn/irc", "ws://103.78.2.201/irc"};
      k = var0;
   }

   class IRCWebSocketListener implements Listener {
      private final StringBuilder b = new StringBuilder();

      @Override
      public void onOpen(WebSocket var1) {
         long var2 = 1L;
         var1.request(var2);
      }

      @Override
      public CompletionStage<?> onText(WebSocket var1, CharSequence var2, boolean var3) {
         this.b.append(var2);
         if (var3) {
            String var4 = this.b.toString();
            this.b.setLength(0);
            IRCModule var5 = IRCModule.this;
            String var6 = var4;
            var5.c(var6);
         }

         long var7 = 1L;
         var1.request(var7);
         return null;
      }

      @Override
      public CompletionStage<?> onClose(WebSocket var1, int var2, String var3) {
         IRCModule.this.m = false;
         IRCModule var4 = IRCModule.this;
         long var5 = 3L;
         var4.a_void_long(var5);
         return null;
      }

      @Override
      public void onError(WebSocket var1, Throwable var2) {
         IRCModule.this.m = false;
         IRCModule var3 = IRCModule.this;
         long var4 = 5L;
         var3.a_void_long(var4);
      }
   }
}
