package night.pingbypass.handler;

import com.mojang.authlib.GameProfile;
import java.util.concurrent.ThreadLocalRandom;
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
import night.Night;
import night.pingbypass.PingBypassServer;
import night.pingbypass.protocol.PingBypassPayload;
import night.pingbypass.world.LobbyWorldSender;
import night.util.world.PacketAndRenderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProxyPasswordPacketListener implements TickablePacketListener, ServerGamePacketListener {
   private static final Logger a = LoggerFactory.getLogger(ProxyPasswordPacketListener.class);
   private final PingBypassServer b;
   private final Connection c;
   private final GameProfile d;
   private final RegistryAccess e;
   private boolean f;
   private boolean g;
   private boolean h;
   private long i;

   public ProxyPasswordPacketListener(PingBypassServer var1, Connection var2, GameProfile var3, RegistryAccess var4) {
      this.b = var1;
      this.c = var2;
      this.d = var3;
      this.e = var4;
      long var5 = 0L;
      this.i = var5;
      String var7 = var3.name();
      a.info("Password handler initialized for {}", var7);
   }

   public void tick() {
      long var1 = System.currentTimeMillis();
      if (!this.g) {
         this.g = true;
         Connection var3 = this.c;
         RegistryAccess var4 = this.e;
         LobbyWorldSender.a(var3, var4);
      }

      long var5 = var1;
      long var7 = this.i;
      long var9 = var5 - var7;
      long var11 = 15000L;
      if (var9 >= var11) {
         long var13 = var1;
         this.i = var13;
         Connection var15 = this.c;
         long var16 = var1;
         var15.send(new ClientboundKeepAlivePacket(var16));
      }

      if (!this.f && this.h) {
         this.f = true;
         Connection var18 = this.c;
         PingBypassPayload var19 = PingBypassPayload.empty();
         var18.send(new ClientboundCustomPayloadPacket(var19));
         String var20 = this.d.name();
         a.info("Sent password request to {}", var20);
      }
   }

   public void handleCustomPayload(ServerboundCustomPayloadPacket var1) {
      CustomPacketPayload var2 = var1.payload();
      Identifier var3 = var2.type().id();
      if (PingBypassPayload.CHANNEL_ID.equals(var3)) {
         if (var2 instanceof PingBypassPayload) {
            FriendlyByteBuf var4 = ((PingBypassPayload)var2).toBuffer();

            try {
               int var5 = var4.readVarInt();
               if (var5 == 2) {
                  String var6 = var4.readUtf();
                  this.a(var6);
               } else {
                  Integer var7 = var5;
                  String var8 = this.d.name();
                  a.warn("Unexpected packet ID {} from {} during password verification", var7, var8);
               }
            } catch (Throwable var10) {
               var4.release();
               throw var10;
            }

            var4.release();
         }
      }
   }

   private void a(String var1) {
      if (Night.pingBypassConfig.getPassword().equals(var1)) {
         String var2 = this.d.name();
         a.info("Password accepted for {}", var2);
         Connection var3 = this.c;
         Context var4 = PacketAndRenderUtil.c();
         ProtocolInfo var5 = GameProtocols.SERVERBOUND_TEMPLATE.bind(RegistryFriendlyByteBuf.decorator(RegistryAccess.EMPTY), var4);
         PingBypassServer var6 = this.b;
         Connection var7 = this.c;
         GameProfile var8 = this.d;
         RegistryAccess var9 = this.e;
         var3.setupInboundProtocol(var5, new ProxyWaitingPacketListener(var6, var7, var8, var9));
      } else {
         String var10 = this.d.name();
         a.warn("Wrong password from {}", var10);

         try {
            ThreadLocalRandom var11 = ThreadLocalRandom.current();
            long var12 = 1L;
            long var14 = 11L;
            Thread.sleep(var11.nextLong(var12, var14));
         } catch (InterruptedException var19) {
            Thread.currentThread().interrupt();
         }

         Connection var17 = this.c;
         MutableComponent var18 = Component.literal("Wrong password");
         var17.disconnect(var18);
      }
   }

   public void onDisconnect(DisconnectionDetails var1) {
      String var2 = this.d.name();
      a.info("Client {} disconnected during password verification", var2);
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

   public void handleKeepAlive(ServerboundKeepAlivePacket var1) {
      this.h = true;
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
