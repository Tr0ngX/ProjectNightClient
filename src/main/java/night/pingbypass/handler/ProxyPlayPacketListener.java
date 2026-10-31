package night.pingbypass.handler;

import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import java.awt.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.HashedStack;
import net.minecraft.network.TickablePacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ServerboundClientInformationPacket;
import net.minecraft.network.protocol.common.ServerboundCustomClickActionPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ServerboundPongPacket;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.network.protocol.cookie.ServerboundCookieResponsePacket;
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
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action;
import net.minecraft.network.protocol.ping.ServerboundPingRequestPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import night.Night;
import night.events.impl.AttackBlockEvent;
import night.events.impl.PlayerJumpEvent;
import night.mixins.accessors.ClientPlayerEntityAccessor;
import night.module.Module;
import night.module.impl.player.SpeedMine;
import night.pingbypass.PingBypassServer;
import night.pingbypass.PingBypassState;
import night.pingbypass.module.ProxyModule;
import night.pingbypass.module.ProxyModuleSyncHelper;
import night.pingbypass.network.RubberbandTracker;
import night.pingbypass.network.S2CPacketForwarder;
import night.pingbypass.protocol.PingBypassPayload;
import night.pingbypass.protocol.c2s.C2SFriendSyncPacket;
import night.pingbypass.protocol.c2s.C2SModuleTogglePacket;
import night.pingbypass.protocol.c2s.C2SPlayerInputPacket;
import night.pingbypass.protocol.c2s.C2SSettingUpdatePacket;
import night.setting.Setting;
import night.setting.impl.BooleanSetting;
import night.setting.impl.ColorSetting;
import night.setting.impl.ModeSetting;
import night.setting.impl.NumberSetting;
import night.setting.impl.StringSetting;
import night.util.inventory.InventoryUtil;
import night.util.world.PacketAndRenderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProxyPlayPacketListener implements TickablePacketListener, ServerGamePacketListener {
   private static final Logger a = LoggerFactory.getLogger(ProxyPlayPacketListener.class);
   private final PingBypassServer b;
   private final Connection c;
   private final GameProfile d;
   private final S2CPacketForwarder e;
   private boolean f = true;
   private long g;

   public ProxyPlayPacketListener(PingBypassServer var1, Connection var2, GameProfile var3, S2CPacketForwarder var4, int var5) {
      this.b = var1;
      this.c = var2;
      this.d = var3;
      this.e = var4;
      String var6 = var3.name();
      a.info("Play handler initialized for {} \u2014 dumb pipe mode", var6);
   }

   public GameProfile a() {
      return this.d;
   }

   public void tick() {
      long var1 = System.currentTimeMillis();
      long var3 = this.g;
      long var5 = var1 - var3;
      long var7 = 15000L;
      if (var5 >= var7) {
         long var9 = var1;
         this.g = var9;
         Connection var11 = this.c;
         long var12 = var1;
         var11.send(new ClientboundKeepAlivePacket(var12));
      }
   }

   public void onDisconnect(DisconnectionDetails var1) {
      String var2 = this.d.name();
      Component var3 = var1.reason();
      a.info("Client {} disconnected: {}", var2, var3);
      this.e.b();
      Minecraft var4 = Minecraft.getInstance();
      var4.execute(() -> {
         if (var4.level != null) {
            ClientLevel var1x = var4.level;
            MutableComponent var2x = Component.literal("Client disconnected");
            var1x.disconnect(var2x);
         }

         if (var4.getConnection() != null) {
            Connection var3x = var4.getConnection().getConnection();
            MutableComponent var4x = Component.literal("Client disconnected");
            var3x.disconnect(var4x);
         }
      });
   }

   public boolean isAcceptingMessages() {
      return this.c.isConnected();
   }

   private void b() {
      InventoryUtil.d();
      SpeedMine var1 = Night.moduleManager.getModule(SpeedMine.class);
      if (var1 != null && var1.isEnabled() && var1.isRunningOnProxyServer()) {
         Minecraft var2 = Minecraft.getInstance();
         if (var2.player != null) {
            synchronized (var1.H) {
               int var4 = var2.player.getInventory().getSelectedSlot();
               this.a_void_Packet(new ServerboundSetCarriedItemPacket(var4));
               var1.c_void_bool(true);
               return;
            }
         }
      }

      Minecraft var6 = Minecraft.getInstance();
      if (var6.player != null) {
         int var7 = var6.player.getInventory().getSelectedSlot();
         int var8 = Night.positionManager.getHeldSlot();
         if (var7 != var8) {
            int var9 = var7;
            this.a_void_Packet(new ServerboundSetCarriedItemPacket(var9));
         }
      }
   }

   private void a_void_Packet(Packet<?> var1) {
      Connection var2 = this.b.getBackendConnection();
      if (var2 != null && var2.isConnected()) {
         RubberbandTracker.a(() -> var2.send(var1));
      }
   }

   public void handleMovePlayer(ServerboundMovePlayerPacket var1) {
      Minecraft.getInstance().execute(() -> this.a_void_ServerboundMovePlayerPacket(var1));
   }

   private void a_void_ServerboundMovePlayerPacket(ServerboundMovePlayerPacket var1) {
      Minecraft var2 = Minecraft.getInstance();
      if (var2.player != null) {
         double var3 = var2.player.getY();
         if (var1.hasPosition()) {
            LocalPlayer var5 = var2.player;
            double var6 = var2.player.getX();
            double var8 = var1.getX(var6);
            double var10 = var2.player.getY();
            double var12 = var1.getY(var10);
            double var14 = var2.player.getZ();
            double var16 = var1.getZ(var14);
            var5.setPos(var8, var12, var16);
            double var18 = var2.player.getY();
            double var20 = var3;
            if (var18 < var20 && !var1.isOnGround()) {
               LocalPlayer var22 = var2.player;
               double var23 = var22.fallDistance;
               double var25 = var3;
               double var27 = var18;
               double var29 = (float)(var25 - var27);
               double var31 = var23 + var29;
               var22.fallDistance = var31;
            } else if (var1.isOnGround()) {
               LocalPlayer var33 = var2.player;
               double var34 = 0.0;
               var33.fallDistance = var34;
            }

            double var36 = var18;
            double var38 = var3;
            if (var36 > var38 && !var1.isOnGround() && this.f) {
               Night.EVENT_BUS.post(new PlayerJumpEvent());
            }

            boolean var40 = var1.isOnGround();
            this.f = var40;
         }

         if (var1.hasRotation()) {
            LocalPlayer var41 = var2.player;
            float var42 = var2.player.getYRot();
            float var43 = var1.getYRot(var42);
            var41.setYRot(var43);
            LocalPlayer var44 = var2.player;
            float var45 = var2.player.getXRot();
            float var46 = var1.getXRot(var45);
            var44.setXRot(var46);
         }

         LocalPlayer var47 = var2.player;
         boolean var48 = var1.isOnGround();
         var47.setOnGround(var48);
         LocalPlayer var49 = var2.player;
         boolean var50 = var1.horizontalCollision();
         var49.horizontalCollision = var50;
         RubberbandTracker.a(() -> ((ClientPlayerEntityAccessor)var2.player).invokeSendMovementPackets());
      }

      PingBypassState.e = var1.isOnGround();
      PingBypassState.f = var1.horizontalCollision();
   }

   public void handleUseItemOn(ServerboundUseItemOnPacket var1) {
      this.b();
      InteractionHand var2 = PacketAndRenderUtil.a_InteractionHand_ServerboundUseItemOnPacket(var1);
      this.a_void_InteractionHand(var2);
      this.a_void_Packet(var1);
   }

   public void handleUseItem(ServerboundUseItemPacket var1) {
      this.b();
      InteractionHand var2 = PacketAndRenderUtil.a_InteractionHand_ServerboundUseItemPacket(var1);
      this.a_void_InteractionHand(var2);
      this.a_void_Packet(var1);
   }

   private void a_void_InteractionHand(InteractionHand var1) {
      Minecraft var2 = Minecraft.getInstance();
      if (var2.player != null) {
         ItemStack var3 = var2.player.getItemInHand(var1);
         LocalPlayer var4 = var2.player;
         if (var3.getUseDuration(var4) > 0) {
            var2.player.startUsingItem(var1);
         }
      }
   }

   public void handleInteract(ServerboundInteractPacket var1) {
      this.a_void_Packet(var1);
   }

   public void handlePlayerAction(ServerboundPlayerActionPacket var1) {
      if (var1.getAction() == Action.RELEASE_USE_ITEM) {
         Minecraft var2 = Minecraft.getInstance();
         if (var2.player != null) {
            var2.player.stopUsingItem();
         }

         SpeedMine var3 = Night.moduleManager.getModule(SpeedMine.class);
         if (var3 != null && var3.y()) {
            var3.c_void_bool(false);
         }

         this.a_void_Packet(var1);
      } else {
         if (var1.getAction() == Action.START_DESTROY_BLOCK) {
            SpeedMine var4 = Night.moduleManager.getModule(SpeedMine.class);
            if (var4 != null && var4.isEnabled()) {
               Minecraft var5 = Minecraft.getInstance();
               if (var5.player != null) {
                  var5.execute(() -> {
                     BlockPos var2x = var1.getPos();
                     Direction var3x = var1.getDirection();
                     AttackBlockEvent var4x = new AttackBlockEvent(var2x, var3x);
                     Night.EVENT_BUS.post(var4x);
                     if (!var4x.isCancelled()) {
                        this.a_void_Packet(var1);
                     }
                  });
                  return;
               }
            }
         }

         this.a_void_Packet(var1);
      }
   }

   public void handleAnimate(ServerboundSwingPacket var1) {
      this.a_void_Packet(var1);
   }

   public void handleContainerClick(ServerboundContainerClickPacket var1) {
      Minecraft var2 = Minecraft.getInstance();
      if (var2.player != null) {
         Minecraft var3 = var2;
         Minecraft var4 = var2;
         var3.execute(() -> {
            try {
               int var3x = var1.containerId();
               int var4x = var4.player.containerMenu.containerId;
               net.minecraft.world.inventory.AbstractContainerMenu var5;
               if (var3x == var4x) {
                  var5 = var4.player.containerMenu;
               } else {
                  var5 = var4.player.inventoryMenu;
               }

               net.minecraft.world.inventory.AbstractContainerMenu var6 = var5;
               short var7 = var1.slotNum();
               byte var8 = var1.buttonNum();
               ContainerInput var9 = var1.containerInput();
               LocalPlayer var10 = var4.player;
               var6.clicked(var7, var8, var9, var10);
               int var11 = var1.containerId();
               int var12 = var6.getStateId();
               short var13 = var1.slotNum();
               byte var14 = var1.buttonNum();
               ContainerInput var15 = var1.containerInput();
               Int2ObjectMap var16 = Int2ObjectMaps.emptyMap();
               ServerboundContainerClickPacket var17 = new ServerboundContainerClickPacket(var11, var12, var13, var14, var15, var16, HashedStack.EMPTY);
               this.a_void_Packet(var17);
            } catch (Exception var19) {
            }
         });
      }
   }

   public void handleContainerClose(ServerboundContainerClosePacket var1) {
      this.a_void_Packet(var1);
   }

   public void handleSetCarriedItem(ServerboundSetCarriedItemPacket var1) {
      Minecraft var2 = Minecraft.getInstance();
      SpeedMine var3 = Night.moduleManager.getModule(SpeedMine.class);
      boolean var4;
      if (var3 == null || !var3.isEnabled() || !var3.isRunningOnProxyServer() || var3.v() == null && var3.w() == null) {
         var4 = false;
      } else {
         var4 = true;
      }

      boolean var5 = var4;
      if (var2.player != null) {
         Inventory var6 = var2.player.getInventory();
         int var7 = var1.getSlot();
         var6.setSelectedSlot(var7);
      }

      if (!var5 && !InventoryUtil.b()) {
         this.a_void_Packet(var1);
      }
   }

   public void handleClientCommand(ServerboundClientCommandPacket var1) {
      this.a_void_Packet(var1);
   }

   public void handleChat(ServerboundChatPacket var1) {
      this.a_void_Packet(var1);
   }

   public void handleChatCommand(ServerboundChatCommandPacket var1) {
      this.a_void_Packet(var1);
   }

   public void handleSignedChatCommand(ServerboundChatCommandSignedPacket var1) {
      this.a_void_Packet(var1);
   }

   public void handleChatAck(ServerboundChatAckPacket var1) {
      this.a_void_Packet(var1);
   }

   public void handleChatSessionUpdate(ServerboundChatSessionUpdatePacket var1) {
      this.a_void_Packet(var1);
   }

   public void handleCustomCommandSuggestions(ServerboundCommandSuggestionPacket var1) {
      this.a_void_Packet(var1);
   }

   public void handleSeenAdvancements(ServerboundSeenAdvancementsPacket var1) {
      this.a_void_Packet(var1);
   }

   public void handleSpectatorAction(ServerboundSpectatorActionPacket var1) {
      this.a_void_Packet(var1);
   }

   public void handleResourcePackResponse(ServerboundResourcePackPacket var1) {
      this.a_void_Packet(var1);
   }

   public void handleCookieResponse(ServerboundCookieResponsePacket var1) {
      this.a_void_Packet(var1);
   }

   public void handleAcceptTeleportPacket(ServerboundAcceptTeleportationPacket var1) {
      this.a_void_Packet(var1);
   }

   public void handleChunkBatchReceived(ServerboundChunkBatchReceivedPacket var1) {
      this.a_void_Packet(var1);
   }

   public void handleClientTickEnd(ServerboundClientTickEndPacket var1) {
      this.a_void_Packet(var1);
   }

   public void handleConfigurationAcknowledged(ServerboundConfigurationAcknowledgedPacket var1) {
      this.a_void_Packet(var1);
   }

   public void handleClientInformation(ServerboundClientInformationPacket var1) {
      this.a_void_Packet(var1);
   }

   public void handleBlockEntityTagQuery(ServerboundBlockEntityTagQueryPacket var1) {
      this.a_void_Packet(var1);
   }

   public void handleChangeDifficulty(ServerboundChangeDifficultyPacket var1) {
      this.a_void_Packet(var1);
   }

   public void handleContainerSlotStateChanged(ServerboundContainerSlotStateChangedPacket var1) {
      this.a_void_Packet(var1);
   }

   public void handleDebugSubscriptionRequest(ServerboundDebugSubscriptionRequestPacket var1) {
      this.a_void_Packet(var1);
   }

   public void handleEditBook(ServerboundEditBookPacket var1) {
      this.a_void_Packet(var1);
   }

   public void handleEntityTagQuery(ServerboundEntityTagQueryPacket var1) {
      this.a_void_Packet(var1);
   }

   public void handleJigsawGenerate(ServerboundJigsawGeneratePacket var1) {
      this.a_void_Packet(var1);
   }

   public void handleLockDifficulty(ServerboundLockDifficultyPacket var1) {
      this.a_void_Packet(var1);
   }

   public void handleMoveVehicle(ServerboundMoveVehiclePacket var1) {
      this.a_void_Packet(var1);
   }

   public void handlePaddleBoat(ServerboundPaddleBoatPacket var1) {
      this.a_void_Packet(var1);
   }

   public void handlePickItemFromBlock(ServerboundPickItemFromBlockPacket var1) {
      this.a_void_Packet(var1);
   }

   public void handlePickItemFromEntity(ServerboundPickItemFromEntityPacket var1) {
      this.a_void_Packet(var1);
   }

   public void handlePlaceRecipe(ServerboundPlaceRecipePacket var1) {
      this.a_void_Packet(var1);
   }

   public void handlePlayerAbilities(ServerboundPlayerAbilitiesPacket var1) {
      this.a_void_Packet(var1);
   }

   public void handlePlayerCommand(ServerboundPlayerCommandPacket var1) {
      Minecraft var2 = Minecraft.getInstance();
      if (var2.player != null) {
         switch (var1.getAction()) {
            case START_SPRINTING:
               var2.player.setSprinting(true);
               break;
            case STOP_SPRINTING:
               var2.player.setSprinting(false);
         }
      }

      this.a_void_Packet(var1);
   }

   public void handlePlayerInput(ServerboundPlayerInputPacket var1) {
      Minecraft var2 = Minecraft.getInstance();
      if (var2.player != null) {
         LocalPlayer var3 = var2.player;
         boolean var4 = var1.input().shift();
         var3.setShiftKeyDown(var4);
      }

      this.a_void_Packet(var1);
   }

   public void handleAcceptPlayerLoad(ServerboundPlayerLoadedPacket var1) {
      this.a_void_Packet(var1);
   }

   public void handleRecipeBookSeenRecipePacket(ServerboundRecipeBookSeenRecipePacket var1) {
      this.a_void_Packet(var1);
   }

   public void handleRenameItem(ServerboundRenameItemPacket var1) {
      this.a_void_Packet(var1);
   }

   public void handleBundleItemSelectedPacket(ServerboundSelectBundleItemPacket var1) {
      this.a_void_Packet(var1);
   }

   public void handleSelectTrade(ServerboundSelectTradePacket var1) {
      this.a_void_Packet(var1);
   }

   public void handleSetBeaconPacket(ServerboundSetBeaconPacket var1) {
      this.a_void_Packet(var1);
   }

   public void handleSetCommandBlock(ServerboundSetCommandBlockPacket var1) {
      this.a_void_Packet(var1);
   }

   public void handleSetCommandMinecart(ServerboundSetCommandMinecartPacket var1) {
      this.a_void_Packet(var1);
   }

   public void handleSetCreativeModeSlot(ServerboundSetCreativeModeSlotPacket var1) {
      this.a_void_Packet(var1);
   }

   public void handleSetGameRule(ServerboundSetGameRulePacket var1) {
      this.a_void_Packet(var1);
   }

   public void handleSetJigsawBlock(ServerboundSetJigsawBlockPacket var1) {
      this.a_void_Packet(var1);
   }

   public void handleSetStructureBlock(ServerboundSetStructureBlockPacket var1) {
      this.a_void_Packet(var1);
   }

   public void handleSetTestBlock(ServerboundSetTestBlockPacket var1) {
      this.a_void_Packet(var1);
   }

   public void handleSignUpdate(ServerboundSignUpdatePacket var1) {
      this.a_void_Packet(var1);
   }

   public void handleTeleportToEntityPacket(ServerboundTeleportToEntityPacket var1) {
      this.a_void_Packet(var1);
   }

   public void handleTestInstanceBlockAction(ServerboundTestInstanceBlockActionPacket var1) {
      this.a_void_Packet(var1);
   }

   public void handleChangeGameMode(ServerboundChangeGameModePacket var1) {
      this.a_void_Packet(var1);
   }

   public void handleAttack(ServerboundAttackPacket var1) {
      this.a_void_Packet(var1);
   }

   public void handleContainerButtonClick(ServerboundContainerButtonClickPacket var1) {
      this.a_void_Packet(var1);
   }

   public void handleRecipeBookChangeSettingsPacket(ServerboundRecipeBookChangeSettingsPacket var1) {
      this.a_void_Packet(var1);
   }

   public void handleCustomClickAction(ServerboundCustomClickActionPacket var1) {
      this.a_void_Packet(var1);
   }

   public void handleKeepAlive(ServerboundKeepAlivePacket var1) {
   }

   public void handlePong(ServerboundPongPacket var1) {
      this.a_void_Packet(var1);
   }

   public void handlePingRequest(ServerboundPingRequestPacket var1) {
   }

   public void handleCustomPayload(ServerboundCustomPayloadPacket var1) {
      if (var1.payload() instanceof PingBypassPayload var3) {
         this.a_void_PingBypassPayload(var3);
      } else {
         this.a_void_Packet(var1);
      }
   }

   private void a_void_PingBypassPayload(PingBypassPayload var1) {
      FriendlyByteBuf var2 = var1.toBuffer();

      label43: {
         try {
            try {
               int var3 = var2.readVarInt();
               switch (var3) {
                  case 3:
                     FriendlyByteBuf var4 = var2;
                     C2SModuleTogglePacket var5 = new C2SModuleTogglePacket(var4);
                     String var6 = var5.getModuleName();
                     ProxyModule var7 = Night.proxyModuleManager.getModule(var6);
                     if (var7 != null) {
                        Minecraft var8 = Minecraft.getInstance();
                        ProxyModule var9 = var7;
                        C2SModuleTogglePacket var10 = var5;
                        var8.execute(() -> {
                           boolean var2x = var10.isEnabled();
                           ProxyModuleSyncHelper.a(var9, var2x);
                           String var3x = var10.getModuleName();
                           Boolean var4x = var10.isEnabled();
                           a.info("[PB] PbModule {} toggled to {}", var3x, var4x);
                        });
                     } else {
                        String var11 = var5.getModuleName();
                        Module var12 = Night.moduleManager.getModuleByName(var11);
                        if (var12 != null) {
                           Minecraft var13 = Minecraft.getInstance();
                           Module var14 = var12;
                           C2SModuleTogglePacket var15 = var5;
                           var13.execute(() -> {
                              boolean var2x = var15.isEnabled();
                              var14.setEnabled(var2x, false);
                              String var3x = var15.getModuleName();
                              Boolean var4x = var15.isEnabled();
                              a.info("[PB] Module {} toggled to {}", var3x, var4x);
                           });
                        }
                     }
                     break label43;
                  case 4:
                     FriendlyByteBuf var16 = var2;
                     C2SSettingUpdatePacket var17 = new C2SSettingUpdatePacket(var16);
                     String var18 = var17.getModuleName();
                     ProxyModule var19 = Night.proxyModuleManager.getModule(var18);
                     if (var19 != null) {
                        Minecraft var20 = Minecraft.getInstance();
                        ProxyModule var21 = var19;
                        C2SSettingUpdatePacket var22 = var17;
                        var20.execute(() -> {
                           String var2x = var22.getSettingName();
                           String var3x = var22.getValue();
                           ProxyModuleSyncHelper.a(var21, var2x, var3x);
                        });
                     } else {
                        C2SSettingUpdatePacket var23 = var17;
                        this.a_void_C2SSettingUpdatePacket(var23);
                     }
                     break label43;
                  case 14:
                     FriendlyByteBuf var26 = var2;
                     C2SFriendSyncPacket var27 = new C2SFriendSyncPacket(var26);
                     Minecraft.getInstance().execute(() -> {
                        Night.friendManager.clearFriends();

                        for (String var2x : (Iterable<String>)(Object)var27.getFriends()) {
                           Night.friendManager.addFriend(var2x);
                        }

                        Integer var3x = var27.getFriends().size();
                        a.info("[PB] Synced {} friend(s) from client", var3x);
                     });
                     break label43;
                  case 15:
                     FriendlyByteBuf var24 = var2;
                     C2SPlayerInputPacket var25 = new C2SPlayerInputPacket(var24);
                     Minecraft.getInstance().execute(() -> {
                        Minecraft var1x = Minecraft.getInstance();
                        if (var1x.player != null) {
                           if (var25.isInventoryOpen()) {
                              Gui var2x = var1x.gui;
                              LocalPlayer var3x = var1x.player;
                              var2x.setScreen(new InventoryScreen(var3x));
                           } else {
                              Screen var4x = var1x.gui.screen();
                              if (var4x instanceof InventoryScreen) {
                                 AbstractContainerMenu var5x = ((InventoryScreen)var4x).getMenu();
                                 InventoryMenu var6x = var1x.player.inventoryMenu;
                                 if (var5x == var6x) {
                                    var1x.gui.setScreen(null);
                                 }
                              }
                           }
                        }
                     });
                     break label43;
                  default:
                     Integer var28 = var3;
                     a.debug("[PB] Unknown packet ID: {}", var28);
                     break label43;
               }
            } catch (Exception var31) {
               a.warn("[PB] Failed to handle payload", var31);
            }
         } catch (Throwable var32) {
            var2.release();
            throw var32;
         }

         var2.release();
         return;
      }

      var2.release();
   }

   private void a_void_C2SSettingUpdatePacket(C2SSettingUpdatePacket var1) {
      String var2 = var1.getModuleName();
      Module var3 = Night.moduleManager.getModuleByName(var2);
      if (var3 != null) {
         Module var4 = var3;
         String var5 = var1.getSettingName();
         Setting var6 = var4.getSetting(var5);
         if (var6 != null) {
            Minecraft var7 = Minecraft.getInstance();
            Setting var8 = var6;
            var7.execute(() -> {
               try {
                  String var2x = var1.getValue();
                  if (var8 instanceof BooleanSetting var3x) {
                     boolean var4x = Boolean.parseBoolean(var2x);
                     var3x.setValue(var4x);
                  } else if (var8 instanceof NumberSetting var5x) {
                     switch (var5x.getNumberType()) {
                        case INTEGER:
                           NumberSetting var6x = var5x;
                           Integer var7x = Integer.parseInt(var2x);
                           var6x.setValue(var7x);
                           break;
                        case LONG:
                           NumberSetting var8x = var5x;
                           Long var9 = Long.parseLong(var2x);
                           var8x.setValue(var9);
                           break;
                        case FLOAT:
                           NumberSetting var10 = var5x;
                           Float var11 = Float.parseFloat(var2x);
                           var10.setValue(var11);
                           break;
                        case DOUBLE:
                           NumberSetting var12 = var5x;
                           Double var13 = Double.parseDouble(var2x);
                           var12.setValue(var13);
                     }
                  } else if (var8 instanceof ModeSetting var14) {
                     String var15 = var2x;
                     var14.setValue(var15);
                  } else if (var8 instanceof StringSetting var16) {
                     String var17 = var2x;
                     var16.setValue(var17);
                  } else if (var8 instanceof ColorSetting var18) {
                     String[] var19 = var2x.split(",");
                     if (var19.length >= 4) {
                        ColorSetting var20 = var18;
                        int var21 = Integer.parseInt(var19[0]);
                        int var22 = Integer.parseInt(var19[1]);
                        int var23 = Integer.parseInt(var19[2]);
                        int var24 = Integer.parseInt(var19[3]);
                        var20.setColor(new Color(var21, var22, var23, var24));
                        if (var19.length >= 5) {
                           ColorSetting var25 = var18;
                           boolean var26 = Boolean.parseBoolean(var19[4]);
                           var25.setSync(var26);
                        }

                        if (var19.length >= 6) {
                           ColorSetting var27 = var18;
                           boolean var28 = Boolean.parseBoolean(var19[5]);
                           var27.setRainbow(var28);
                        }
                     }
                  }

                  Object[] var29 = new Object[3];
                  String var30 = var1.getModuleName();
                  var29[0] = var30;
                  String var31 = var1.getSettingName();
                  var29[1] = var31;
                  String var32 = var2x;
                  var29[2] = var32;
                  a.info("[PB] Setting {}.{} = {}", var29);
               } catch (Exception var38) {
                  Object[] var34 = new Object[3];
                  String var35 = var1.getModuleName();
                  var34[0] = var35;
                  String var36 = var1.getSettingName();
                  var34[1] = var36;
                  var34[2] = var38;
                  a.warn("[PB] Failed to apply setting {}.{}", var34);
               }
            });
         }
      }
   }
}
