package night.pingbypass.handler;

import com.mojang.authlib.GameProfile;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.util.Random;
import java.util.UUID;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.configuration.ConfigurationProtocols;
import net.minecraft.network.protocol.cookie.ServerboundCookieResponsePacket;
import net.minecraft.network.protocol.login.ClientboundHelloPacket;
import net.minecraft.network.protocol.login.ClientboundLoginDisconnectPacket;
import net.minecraft.network.protocol.login.ClientboundLoginFinishedPacket;
import net.minecraft.network.protocol.login.ServerLoginPacketListener;
import net.minecraft.network.protocol.login.ServerboundCustomQueryAnswerPacket;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import net.minecraft.network.protocol.login.ServerboundKeyPacket;
import net.minecraft.network.protocol.login.ServerboundLoginAcknowledgedPacket;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;
import night.pingbypass.PingBypassServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProxyLoginPacketListener implements ServerLoginPacketListener {
   private static final Logger a = LoggerFactory.getLogger(ProxyLoginPacketListener.class);
   private static final Random b = new Random();
   private final PingBypassServer c;
   private final Connection d;
   private final KeyPair e;
   private final byte[] f;
   private ProxyLoginPacketListener.LoginState g;
   private String h;
   private GameProfile i;

   public ProxyLoginPacketListener(PingBypassServer var1, Connection var2) {
      byte[] var3 = new byte[4];
      this.f = var3;
      this.g = ProxyLoginPacketListener.LoginState.HELLO;
      this.c = var1;
      this.d = var2;

      try {
         KeyPair var4 = Crypt.generateKeyPair();
         this.e = var4;
      } catch (CryptException var7) {
         throw new IllegalStateException("Failed to generate RSA keypair", var7);
      }

      byte[] var6 = this.f;
      b.nextBytes(var6);
   }

   public void handleHello(ServerboundHelloPacket var1) {
      if (this.g != ProxyLoginPacketListener.LoginState.HELLO) {
         throw new IllegalStateException("Unexpected hello packet");
      }

      String var2 = var1.name();
      this.h = var2;
      String var3 = this.h;
      a.info("Login attempt from {}", var3);
      this.g = ProxyLoginPacketListener.LoginState.KEY;
      Connection var4 = this.d;
      byte[] var5 = this.e.getPublic().getEncoded();
      byte[] var6 = this.f;
      var4.send(new ClientboundHelloPacket("", var5, var6, false));
   }

   public void handleKey(ServerboundKeyPacket var1) {
      if (this.g != ProxyLoginPacketListener.LoginState.KEY) {
         throw new IllegalStateException("Unexpected key packet");
      }

      PrivateKey var2 = this.e.getPrivate();
      byte[] var3 = this.f;
      PrivateKey var4 = var2;
      if (!var1.isChallengeValid(var3, var4)) {
         throw new IllegalStateException("Protocol error: invalid nonce!");
      }

      SecretKey var6;
      try {
         PrivateKey var5 = var2;
         var6 = var1.getSecretKey(var5);
      } catch (CryptException var17) {
         throw new IllegalStateException("Protocol error", var17);
      }

      this.g = ProxyLoginPacketListener.LoginState.READY_TO_ACCEPT;

      try {
         SecretKey var7 = var6;
         Cipher var8 = Crypt.getCipher(2, var7);
         SecretKey var9 = var6;
         Cipher var10 = Crypt.getCipher(1, var9);
         Connection var11 = this.d;
         Cipher var12 = var8;
         Cipher var13 = var10;
         var11.setEncryptionKey(var12, var13);
      } catch (CryptException var16) {
         throw new IllegalStateException("Protocol error", var16);
      }

      this.a();
   }

   private void a() {
      UUID var1 = UUIDUtil.createOfflinePlayerUUID(this.h);
      String var2 = this.h;
      GameProfile var3 = new GameProfile(var1, var2);
      this.i = var3;
      this.g = ProxyLoginPacketListener.LoginState.PROTOCOL_SWITCHING;
      Connection var4 = this.d;
      GameProfile var5 = var3;
      UUID var6 = UUID.randomUUID();
      var4.send(new ClientboundLoginFinishedPacket(var5, var6));
      String var7 = var3.name();
      UUID var8 = var3.id();
      a.info("Login success for {} ({}), waiting for LOGIN_ACKNOWLEDGED", var7, var8);
   }

   public void handleLoginAcknowledgement(ServerboundLoginAcknowledgedPacket var1) {
      if (this.g != ProxyLoginPacketListener.LoginState.PROTOCOL_SWITCHING) {
         throw new IllegalStateException("Unexpected login acknowledgement packet");
      }

      this.d.setupOutboundProtocol(ConfigurationProtocols.CLIENTBOUND);
      Connection var2 = this.d;
      PingBypassServer var3 = this.c;
      Connection var4 = this.d;
      GameProfile var5 = this.i;
      var2.setupInboundProtocol(ConfigurationProtocols.SERVERBOUND, new ProxyConfigurationPacketListener(var3, var4, var5));
      this.g = ProxyLoginPacketListener.LoginState.ACCEPTED;
      String var6 = this.h;
      a.info("Transitioning {} to configuration phase", var6);
   }

   public void handleCustomQueryPacket(ServerboundCustomQueryAnswerPacket var1) {
      MutableComponent var2 = Component.literal("Unexpected query response");
      this.a(var2);
   }

   public void handleCookieResponse(ServerboundCookieResponsePacket var1) {
      MutableComponent var2 = Component.literal("Unexpected cookie response");
      this.a(var2);
   }

   public void onDisconnect(DisconnectionDetails var1) {
      Logger var2 = a;
      String var3;
      if (this.h != null) {
         var3 = this.h;
      } else {
         var3 = "unknown";
      }

      Component var4 = var1.reason();
      var2.info("Client {} disconnected during login: {}", var3, var4);
   }

   public boolean isAcceptingMessages() {
      return this.d.isConnected();
   }

   private void a(Component var1) {
      try {
         this.d.send(new ClientboundLoginDisconnectPacket(var1));
         this.d.disconnect(var1);
      } catch (Exception var3) {
         a.error("Error disconnecting client", var3);
      }
   }

   enum LoginState {
      HELLO,
      KEY,
      READY_TO_ACCEPT,
      PROTOCOL_SWITCHING,
      ACCEPTED;
   }
}
