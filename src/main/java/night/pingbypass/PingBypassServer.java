package night.pingbypass;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import java.net.InetAddress;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadFactory;
import java.util.function.Supplier;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import night.pingbypass.handler.ProxyHandshakePacketListener;
import night.pingbypass.network.PingBypassSessionConnection;
import night.pingbypass.protocol.PingBypassProtocol;
import night.pingbypass.world.VanillaRegistryCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PingBypassServer {
   private static final Logger LOGGER = LoggerFactory.getLogger(PingBypassServer.class);
   private static final PingBypassServer.LazyHolder<NioEventLoopGroup> b = new PingBypassServer.LazyHolder<>(() -> {
      ThreadFactory var0 = new ThreadFactoryBuilder().setNameFormat("PingBypass Server IO #%d").setDaemon(true).build();
      return new NioEventLoopGroup(0, var0);
   });
   private static final PingBypassServer.LazyHolder<EpollEventLoopGroup> c = new PingBypassServer.LazyHolder<>(() -> {
      ThreadFactory var0 = new ThreadFactoryBuilder().setNameFormat("PingBypass Epoll Server IO #%d").setDaemon(true).build();
      return new EpollEventLoopGroup(0, var0);
   });
   private final List<ChannelFuture> channels;
   final List<Connection> connections;
   private final PingBypassConfig config;
   private final PingBypassProtocol protocol;
   private volatile boolean stayConnected;
   private volatile boolean running;
   private volatile Connection backendConnection;
   private final VanillaRegistryCache registryCache;

   public PingBypassServer(PingBypassConfig var1) {
      List var2 = Collections.synchronizedList(Lists.newArrayList());
      this.channels = var2;
      List var3 = Collections.synchronizedList(Lists.newArrayList());
      this.connections = var3;
      this.config = var1;
      this.stayConnected = true;
      this.protocol = new PingBypassProtocol();
      this.registryCache = new VanillaRegistryCache();
      this.protocol.a(this);
   }

   public void bind(InetAddress var1, int var2) {
      Integer var3 = var2;
      LOGGER.info("PingBypass proxy binding to {}:{}", var1, var3);
      synchronized (this.channels) {
         Class<? extends io.netty.channel.ServerChannel> var5;
         EventLoopGroup var6;
         if (Epoll.isAvailable()) {
            var5 = EpollServerSocketChannel.class;
            var6 = (EventLoopGroup)c.a();
            LOGGER.info("Using epoll channel type");
         } else {
            var5 = NioServerSocketChannel.class;
            var6 = (EventLoopGroup)b.a();
            LOGGER.info("Using default NIO channel type");
         }

         List var7 = this.channels;
         Class<? extends io.netty.channel.ServerChannel> var8 = var5;
         ServerBootstrap var9 = ((ServerBootstrap)new ServerBootstrap().channel(var8)).childHandler(new ChannelInitializer<Channel>() {
            protected void initChannel(Channel var1) {
               try {
                  var1.config().setOption(ChannelOption.TCP_NODELAY, Boolean.TRUE);
               } catch (ChannelException var12) {
               }

               ChannelPipeline var3x = var1.pipeline().addLast("timeout", new ReadTimeoutHandler(30));
               Connection.configureSerialization(var3x, PacketFlow.SERVERBOUND, false, null);
               PingBypassSessionConnection var4 = new PingBypassSessionConnection();
               List var5x = PingBypassServer.this.connections;
               PingBypassSessionConnection var6x = var4;
               var5x.add(var6x);
               PingBypassSessionConnection var7x = var4;
               ChannelPipeline var8x = var3x;
               var7x.configurePacketHandler(var8x);
               PingBypassSessionConnection var9x = var4;
               PingBypassServer var10 = PingBypassServer.this;
               PingBypassSessionConnection var11 = var4;
               var9x.setListenerForServerboundHandshake(new ProxyHandshakePacketListener(var10, var11));
            }
         });
         EventLoopGroup var10 = var6;
         ChannelFuture var11 = ((ServerBootstrap)var9.group(var10).localAddress(var1, var2)).bind().syncUninterruptibly();
         var7.add(var11);
         Integer var12 = var2;
         LOGGER.info("PingBypass proxy is now listening on {}:{}", var1, var12);
      }
   }

   public void tick() {
      synchronized (this.connections) {
         Iterator var2 = this.connections.iterator();

         while (var2.hasNext()) {
            Connection var3 = (Connection)var2.next();
            if (!var3.isConnected()) {
               var2.remove();
               var3.handleDisconnection();
            } else {
               try {
                  var3.tick();
               } catch (Exception var12) {
                  String var5 = var3.getLoggableAddress(false);
                  LOGGER.warn("Failed to handle packet for {}", var5, var12);
                  Connection var7 = var3;
                  MutableComponent var8 = Component.literal("Internal server error");
                  var7.send(new ClientboundDisconnectPacket(var8));
                  Connection var9 = var3;
                  MutableComponent var10 = Component.literal("Internal server error");
                  var9.disconnect(var10);
               }
            }
         }
      }
   }

   public void shutdown() {
      this.stayConnected = false;
      LOGGER.info("Shutting down PingBypass proxy server...");

      for (ChannelFuture var2 : (Iterable<ChannelFuture>)(Object)this.channels) {
         try {
            var2.channel().close().sync();
         } catch (InterruptedException var9) {
            LOGGER.error("Interrupted whilst closing channel");
         }
      }

      this.channels.clear();
      synchronized (this.connections) {
         for (Connection var5 : (Iterable<Connection>)(Object)this.connections) {
            MutableComponent var6 = Component.literal("Proxy server shutting down");
            var5.disconnect(var6);
         }

         this.connections.clear();
      }

      LOGGER.info("PingBypass proxy server shut down.");
   }

   public boolean isStayConnected() {
      return this.stayConnected;
   }

   public List<Connection> getConnections() {
      return this.connections;
   }

   public void setStayConnected(boolean var1) {
      this.running = var1;
   }

   public Connection getBackendConnection() {
      return this.backendConnection;
   }

   public void setBackendConnection(Connection var1) {
      this.backendConnection = var1;
   }

   public VanillaRegistryCache getRegistryCache() {
      return this.registryCache;
   }

   static class LazyHolder<T> {
      private final Supplier<T> a;
      private volatile T b;

      LazyHolder(Supplier<T> var1) {
         this.a = var1;
      }

      T a() {
         if (this.b == null) {
            synchronized (this) {
               if (this.b == null) {
                  Object var1 = this.a.get();
                  this.b = (T)var1;
               }
            }
         }

         return this.b;
      }
   }
}
