package night.pingbypass.handler;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerData.Type;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.TickablePacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ClientboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ServerboundClientInformationPacket;
import net.minecraft.network.protocol.common.ServerboundCustomClickActionPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ServerboundPongPacket;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.cookie.ServerboundCookieResponsePacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.GameProtocols;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.network.protocol.game.ServerboundBlockEntityTagQueryPacket;
import net.minecraft.network.protocol.game.ServerboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ServerboundChangeGameModePacket;
import net.minecraft.network.protocol.game.ServerboundChatAckPacket;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.network.protocol.game.ServerboundChatCommandSignedPacket;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.network.protocol.game.ServerboundChatSessionUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundChunkBatchReceivedPacket;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.network.protocol.game.ServerboundClientTickEndPacket;
import net.minecraft.network.protocol.game.ServerboundCommandSuggestionPacket;
import net.minecraft.network.protocol.game.ServerboundConfigurationAcknowledgedPacket;
import net.minecraft.network.protocol.game.ServerboundContainerButtonClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundContainerSlotStateChangedPacket;
import net.minecraft.network.protocol.game.ServerboundDebugSubscriptionRequestPacket;
import net.minecraft.network.protocol.game.ServerboundEditBookPacket;
import net.minecraft.network.protocol.game.ServerboundEntityTagQueryPacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundJigsawGeneratePacket;
import net.minecraft.network.protocol.game.ServerboundLockDifficultyPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ServerboundPaddleBoatPacket;
import net.minecraft.network.protocol.game.ServerboundPickItemFromBlockPacket;
import net.minecraft.network.protocol.game.ServerboundPickItemFromEntityPacket;
import net.minecraft.network.protocol.game.ServerboundPlaceRecipePacket;
import net.minecraft.network.protocol.game.ServerboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerLoadedPacket;
import net.minecraft.network.protocol.game.ServerboundRecipeBookChangeSettingsPacket;
import net.minecraft.network.protocol.game.ServerboundRecipeBookSeenRecipePacket;
import net.minecraft.network.protocol.game.ServerboundRenameItemPacket;
import net.minecraft.network.protocol.game.ServerboundSeenAdvancementsPacket;
import net.minecraft.network.protocol.game.ServerboundSelectBundleItemPacket;
import net.minecraft.network.protocol.game.ServerboundSelectTradePacket;
import net.minecraft.network.protocol.game.ServerboundSetBeaconPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundSetCommandBlockPacket;
import net.minecraft.network.protocol.game.ServerboundSetCommandMinecartPacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.network.protocol.game.ServerboundSetGameRulePacket;
import net.minecraft.network.protocol.game.ServerboundSetJigsawBlockPacket;
import net.minecraft.network.protocol.game.ServerboundSetStructureBlockPacket;
import net.minecraft.network.protocol.game.ServerboundSetTestBlockPacket;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundSpectatorActionPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundTeleportToEntityPacket;
import net.minecraft.network.protocol.game.ServerboundTestInstanceBlockActionPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.network.protocol.game.GameProtocols.Context;
import net.minecraft.network.protocol.ping.ServerboundPingRequestPacket;
import net.minecraft.resources.Identifier;
import night.mixins.accessors.DisconnectedScreenAccessor;
import night.pingbypass.PingBypassServer;
import night.pingbypass.network.S2CPacketForwarder;
import night.pingbypass.protocol.PingBypassPayload;
import night.pingbypass.protocol.s2c.S2CProxyDisconnectPacket;
import night.pingbypass.protocol.s2c.S2CStatusMessagePacket;
import night.pingbypass.world.LobbyWorldSender;
import night.pingbypass.world.WorldStateReplayer;
import night.util.world.PacketAndRenderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProxyWaitingPacketListener implements TickablePacketListener, ServerGamePacketListener {
   private static final Logger a = LoggerFactory.getLogger(ProxyWaitingPacketListener.class);
   private final PingBypassServer b;
   private final Connection c;
   private final GameProfile d;
   private final RegistryAccess e;
   private long f;
   private long g;
   private boolean h;
   private String i;
   private int j;
   private volatile boolean k;
   private int l;

   public ProxyWaitingPacketListener(PingBypassServer var1, Connection var2, GameProfile var3, RegistryAccess var4) {
      this.b = var1;
      this.c = var2;
      this.d = var3;
      this.e = var4;
      long var5 = System.currentTimeMillis();
      this.f = var5;
      long var7 = 0L;
      this.g = var7;
      String var9 = var3.name();
      a.info("Waiting handler initialized for {}", var9);
   }

   public void tick() {
      long var1 = System.currentTimeMillis();
      if (!this.h) {
         this.h = true;
         Connection var3 = this.c;
         RegistryAccess var4 = this.e;
         LobbyWorldSender.a(var3, var4);
      }

      long var5 = var1;
      long var7 = this.f;
      long var9 = var5 - var7;
      long var11 = 30000L;
      if (var9 > var11) {
         String var13 = this.d.name();
         a.warn("Client {} timed out (no keep-alive for 30s)", var13);
         Connection var14 = this.c;
         MutableComponent var15 = Component.literal("Timed out");
         var14.disconnect(var15);
      }

      long var16 = var1;
      long var18 = this.g;
      long var20 = var16 - var18;
      long var22 = 15000L;
      if (var20 >= var22) {
         long var24 = var1;
         this.g = var24;
         Connection var26 = this.c;
         long var27 = var1;
         var26.send(new ClientboundKeepAlivePacket(var27));
      }

      if (this.k) {
         int var29 = this.l + 1;
         this.l = var29;
         Minecraft var30 = Minecraft.getInstance();
         if (this.l == 20) {
            this.b("\u00a77[PingBypass] Resolving address...");
         } else if (this.l == 40 && var30.getConnection() == null) {
            this.b("\u00a77[PingBypass] Establishing connection...");
         } else if (this.l == 60 && var30.getConnection() == null) {
            this.b("\u00a77[PingBypass] Encrypting...");
         }

         if (var30.getConnection() != null && var30.player == null) {
            if (this.l % 40 == 0) {
               this.b("\u00a77[PingBypass] Configuring...");
            }
         } else if (var30.getConnection() == null || var30.player == null || var30.level == null) {
            if (var30.gui.screen() instanceof DisconnectedScreen var35) {
               this.k = false;
               String var36 = ((DisconnectedScreenAccessor)var35).night$getDetails().reason().getString();
               Object[] var37 = new Object[4];
               String var38 = this.i;
               var37[0] = var38;
               Integer var39 = this.j;
               var37[1] = var39;
               String var40 = this.d.name();
               var37[2] = var40;
               String var41 = var36;
               var37[3] = var41;
               a.warn("Connection to {}:{} failed for {}: {}", var37);
               String var42 = this.i;
               int var43 = this.j;
               String var44 = var36;
               this.a("Failed to connect to " + var42 + ":" + var43 + ": " + var44);
               String var45 = this.i;
               int var46 = this.j;
               String var47 = var36;
               this.b("\u00a7c[PingBypass] Failed to connect to " + var45 + ":" + var46 + " (" + var47 + ")");
            } else if (this.l > 600) {
               this.k = false;
               Object[] var48 = new Object[3];
               String var49 = this.i;
               var48[0] = var49;
               Integer var50 = this.j;
               var48[1] = var50;
               String var51 = this.d.name();
               var48[2] = var51;
               a.warn("Connection to {}:{} timed out for {}", var48);
               String var52 = this.i;
               int var53 = this.j;
               this.a("Connection to " + var52 + ":" + var53 + " timed out");
               this.b("\u00a7c[PingBypass] Connection timed out");
            }
         } else if (var30.getConnection().registryAccess() != null) {
            this.k = false;
            this.b("\u00a7a[PingBypass] Connected! Loading world...");
            Minecraft var31 = var30;
            String var32 = this.i;
            int var33 = this.j;
            this.a(var31, var32, var33);
         } else if (this.l % 40 == 0) {
            this.b("\u00a77[PingBypass] Loading terrain...");
         }
      }
   }

   public void handleCustomPayload(ServerboundCustomPayloadPacket var1) {
      CustomPacketPayload var2 = var1.payload();
      Identifier var3 = var2.type().id();
      if (PingBypassPayload.CHANNEL_ID.equals(var3)) {
         if (var2 instanceof PingBypassPayload) {
            FriendlyByteBuf var4 = ((PingBypassPayload)var2).toBuffer();

            label42: {
               try {
                  try {
                     int var5 = var4.readVarInt();
                     if (var5 == 0) {
                        String var6 = var4.readUtf();
                        int var7 = var4.readVarInt();
                        String var8 = var6;
                        int var9 = var7;
                        this.a(var8, var9);
                     } else {
                        Integer var10 = var5;
                        String var11 = this.d.name();
                        a.warn("Unexpected packet ID {} from {} during waiting state", var10, var11);
                     }
                     break label42;
                  } catch (Exception var16) {
                     String var13 = this.d.name();
                     a.warn("Malformed custom payload from {}", var13, var16);
                  }
               } catch (Throwable var17) {
                  var4.release();
                  throw var17;
               }

               var4.release();
               return;
            }

            var4.release();
         }
      }
   }

   void a(String var1, int var2) {
      Object[] var3 = new Object[3];
      String var4 = this.d.name();
      var3[0] = var4;
      var3[1] = var1;
      Integer var5 = var2;
      var3[2] = var5;
      a.info("Client {} requested join to {}:{}", var3);
      Minecraft var6 = Minecraft.getInstance();
      if (var6.getConnection() != null && var6.player != null && var6.level != null && var6.getConnection().registryAccess() != null) {
         String var7 = this.d.name();
         a.info("Proxy already connected to a server, replaying world state for {}", var7);
         this.b("\u00a7a[PingBypass] Proxy already connected! Resuming session...");
         Minecraft var8 = var6;
         this.a(var8, var1, var2);
      } else {
         this.b("\u00a7d[PingBypass] \u00a7fConnecting to \u00a7a" + var1 + ":" + var2 + "\u00a7f...");
         String var9 = var1 + ":" + var2;
         ServerAddress var10 = ServerAddress.parseString(var9);
         String var11 = var9;
         ServerData var12 = new ServerData("PingBypass Target", var11, Type.OTHER);
         this.i = var1;
         this.j = var2;
         this.k = true;
         this.l = 0;
         Minecraft var13 = var6;
         String var14 = var9;
         Minecraft var15 = var6;
         ServerAddress var16 = var10;
         ServerData var17 = var12;
         var13.execute(() -> {
            try {
               String var5x = this.d.name();
               a.info("Initiating connection to {} for {}", var14, var5x);
               ConnectScreen.startConnecting(new TitleScreen(), var15, var16, var17, false, null);
            } catch (Exception var11x) {
               Object[] var7x = new Object[]{var14, null, null};
               String var8x = this.d.name();
               var7x[1] = var8x;
               var7x[2] = var11x;
               a.error("Failed to initiate connection to {} for {}", var7x);
               String var10x = var11x.getMessage();
               this.a("Failed to connect to " + var14 + ": " + var10x);
               this.k = false;
            }
         });
      }
   }

   private void a(Minecraft var1, String var2, int var3) {
      Object[] var4 = new Object[]{var2, null, null};
      Integer var5 = var3;
      var4[1] = var5;
      String var6 = this.d.name();
      var4[2] = var6;
      a.info("Server connection established to {}:{} for {}", var4);

      try {
         Connection var7 = var1.getConnection().getConnection();
         PingBypassServer var8 = this.b;
         Connection var9 = var7;
         var8.setBackendConnection(var9);
         RegistryAccess var10 = var1.level.registryAccess();
         Connection var11 = this.c;
         ProtocolInfo var12 = GameProtocols.CLIENTBOUND_TEMPLATE.bind(RegistryFriendlyByteBuf.decorator(var10));
         var11.setupOutboundProtocol(var12);
         String var13 = this.d.name();
         a.info("Re-transitioned outbound encoder to real server registry for {}", var13);
         RegistryAccess var14 = var10;
         Connection var15 = this.c;
         String var16 = var2 + ":" + var3;
         PingBypassPayload var17 = PingBypassPayload.fromPacket(new S2CProxyDisconnectPacket(var16));
         var15.send(new ClientboundCustomPayloadPacket(var17));
         Connection var18 = this.c;
         S2CPacketForwarder var19 = new S2CPacketForwarder(var18);
         var19.a();
         Connection var20 = this.c;
         RegistryAccess var21 = var10;
         WorldStateReplayer.a_int_Connection_RegistryAccess(var20, var21);
         PingBypassServer var22 = this.b;
         Connection var23 = this.c;
         GameProfile var24 = this.d;
         S2CPacketForwarder var25 = var19;
         ProxyPlayPacketListener var26 = new ProxyPlayPacketListener(var22, var23, var24, var25, 0);
         Connection var27 = this.c;
         Context var28 = PacketAndRenderUtil.c();
         ProtocolInfo var29 = GameProtocols.SERVERBOUND_TEMPLATE.bind(RegistryFriendlyByteBuf.decorator(var14), var28);
         ProxyPlayPacketListener var30 = var26;
         var27.setupInboundProtocol(var29, var30);
         Object[] var31 = new Object[3];
         String var32 = this.d.name();
         var31[0] = var32;
         var31[1] = var2;
         Integer var33 = var3;
         var31[2] = var33;
         a.info("Proxy fully active for {} on {}:{} (awaiting teleport confirm)", var31);
      } catch (Exception var37) {
         String var35 = this.d.name();
         a.error("Failed to set up forwarding for {}", var35, var37);
         this.a("Failed to set up forwarding: " + var37.getMessage());
      }
   }

   private void a(String var1) {
      if (this.c.isConnected()) {
         Connection var2 = this.c;
         PingBypassPayload var3 = PingBypassPayload.fromPacket(new S2CStatusMessagePacket(var1));
         var2.send(new ClientboundCustomPayloadPacket(var3));
      }
   }

   private void b(String var1) {
      if (this.c.isConnected()) {
         Connection var2 = this.c;
         MutableComponent var3 = Component.literal(var1);
         var2.send(new ClientboundSystemChatPacket(var3, false));
      }
   }

   public void handleKeepAlive(ServerboundKeepAlivePacket var1) {
      long var2 = System.currentTimeMillis();
      this.f = var2;
   }

   public void onDisconnect(DisconnectionDetails var1) {
      String var2 = this.d.name();
      a.info("Client {} disconnected during waiting state", var2);
   }

   public boolean isAcceptingMessages() {
      return this.c.isConnected();
   }

   public void handleAnimate(ServerboundSwingPacket var1) {
   }

   public void handleChat(ServerboundChatPacket var1) {
   }

   public void handleChatCommand(ServerboundChatCommandPacket var1) {
   }

   public void handleSignedChatCommand(ServerboundChatCommandSignedPacket var1) {
   }

   public void handleChatAck(ServerboundChatAckPacket var1) {
   }

   public void handleClientCommand(ServerboundClientCommandPacket var1) {
   }

   public void handleContainerButtonClick(ServerboundContainerButtonClickPacket var1) {
   }

   public void handleContainerClick(ServerboundContainerClickPacket var1) {
   }

   public void handlePlaceRecipe(ServerboundPlaceRecipePacket var1) {
   }

   public void handleContainerClose(ServerboundContainerClosePacket var1) {
   }

   public void handleAttack(ServerboundAttackPacket var1) {
   }

   public void handleInteract(ServerboundInteractPacket var1) {
   }

   public void handleSpectatorAction(ServerboundSpectatorActionPacket var1) {
   }

   public void handleMovePlayer(ServerboundMovePlayerPacket var1) {
   }

   public void handlePlayerAbilities(ServerboundPlayerAbilitiesPacket var1) {
   }

   public void handlePlayerAction(ServerboundPlayerActionPacket var1) {
   }

   public void handlePlayerCommand(ServerboundPlayerCommandPacket var1) {
   }

   public void handlePlayerInput(ServerboundPlayerInputPacket var1) {
   }

   public void handleSetCarriedItem(ServerboundSetCarriedItemPacket var1) {
   }

   public void handleSetCreativeModeSlot(ServerboundSetCreativeModeSlotPacket var1) {
   }

   public void handleSignUpdate(ServerboundSignUpdatePacket var1) {
   }

   public void handleUseItemOn(ServerboundUseItemOnPacket var1) {
   }

   public void handleUseItem(ServerboundUseItemPacket var1) {
   }

   public void handleTeleportToEntityPacket(ServerboundTeleportToEntityPacket var1) {
   }

   public void handlePaddleBoat(ServerboundPaddleBoatPacket var1) {
   }

   public void handleMoveVehicle(ServerboundMoveVehiclePacket var1) {
   }

   public void handleAcceptTeleportPacket(ServerboundAcceptTeleportationPacket var1) {
   }

   public void handleAcceptPlayerLoad(ServerboundPlayerLoadedPacket var1) {
   }

   public void handleRecipeBookSeenRecipePacket(ServerboundRecipeBookSeenRecipePacket var1) {
   }

   public void handleBundleItemSelectedPacket(ServerboundSelectBundleItemPacket var1) {
   }

   public void handleRecipeBookChangeSettingsPacket(ServerboundRecipeBookChangeSettingsPacket var1) {
   }

   public void handleSeenAdvancements(ServerboundSeenAdvancementsPacket var1) {
   }

   public void handleCustomCommandSuggestions(ServerboundCommandSuggestionPacket var1) {
   }

   public void handleSetCommandBlock(ServerboundSetCommandBlockPacket var1) {
   }

   public void handleSetCommandMinecart(ServerboundSetCommandMinecartPacket var1) {
   }

   public void handlePickItemFromBlock(ServerboundPickItemFromBlockPacket var1) {
   }

   public void handlePickItemFromEntity(ServerboundPickItemFromEntityPacket var1) {
   }

   public void handleRenameItem(ServerboundRenameItemPacket var1) {
   }

   public void handleSetBeaconPacket(ServerboundSetBeaconPacket var1) {
   }

   public void handleSetGameRule(ServerboundSetGameRulePacket var1) {
   }

   public void handleSetStructureBlock(ServerboundSetStructureBlockPacket var1) {
   }

   public void handleSetTestBlock(ServerboundSetTestBlockPacket var1) {
   }

   public void handleTestInstanceBlockAction(ServerboundTestInstanceBlockActionPacket var1) {
   }

   public void handleSelectTrade(ServerboundSelectTradePacket var1) {
   }

   public void handleEditBook(ServerboundEditBookPacket var1) {
   }

   public void handleEntityTagQuery(ServerboundEntityTagQueryPacket var1) {
   }

   public void handleContainerSlotStateChanged(ServerboundContainerSlotStateChangedPacket var1) {
   }

   public void handleBlockEntityTagQuery(ServerboundBlockEntityTagQueryPacket var1) {
   }

   public void handleSetJigsawBlock(ServerboundSetJigsawBlockPacket var1) {
   }

   public void handleJigsawGenerate(ServerboundJigsawGeneratePacket var1) {
   }

   public void handleChangeDifficulty(ServerboundChangeDifficultyPacket var1) {
   }

   public void handleChangeGameMode(ServerboundChangeGameModePacket var1) {
   }

   public void handleLockDifficulty(ServerboundLockDifficultyPacket var1) {
   }

   public void handleChatSessionUpdate(ServerboundChatSessionUpdatePacket var1) {
   }

   public void handleConfigurationAcknowledged(ServerboundConfigurationAcknowledgedPacket var1) {
   }

   public void handleChunkBatchReceived(ServerboundChunkBatchReceivedPacket var1) {
   }

   public void handleDebugSubscriptionRequest(ServerboundDebugSubscriptionRequestPacket var1) {
   }

   public void handleClientTickEnd(ServerboundClientTickEndPacket var1) {
   }

   public void handleClientInformation(ServerboundClientInformationPacket var1) {
   }

   public void handlePong(ServerboundPongPacket var1) {
   }

   public void handleResourcePackResponse(ServerboundResourcePackPacket var1) {
   }

   public void handleCookieResponse(ServerboundCookieResponsePacket var1) {
   }

   public void handleCustomClickAction(ServerboundCustomClickActionPacket var1) {
   }

   public void handlePingRequest(ServerboundPingRequestPacket var1) {
   }
}
