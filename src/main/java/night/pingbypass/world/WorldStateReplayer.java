package night.pingbypass.world;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundInitializeBorderPacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheCenterPacket;
import net.minecraft.network.protocol.game.ClientboundSetDefaultSpawnPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.network.protocol.game.ClientboundSetHeldSlotPacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.network.protocol.game.CommonPlayerSpawnInfo;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Action;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Entry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.storage.LevelData.RespawnData;
import net.minecraft.world.phys.Vec3;
import night.mixins.accessors.PlayerListS2CPacketAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorldStateReplayer {
   private static final Logger a = LoggerFactory.getLogger(WorldStateReplayer.class);
   private static final Random b = new Random();

   public static int a_int_Connection_RegistryAccess(Connection var0, RegistryAccess var1) {
      Minecraft var2 = Minecraft.getInstance();
      LocalPlayer var3 = var2.player;
      ClientLevel var4 = var2.level;
      ClientPacketListener var5 = var2.getConnection();
      if (var3 != null && var4 != null && var5 != null) {
         a.info("Replaying world state to client...");
         long var6 = System.currentTimeMillis();
         int var8 = -Math.abs(b.nextInt());
         if (var8 == 0) {
            var8 = -1;
         }

         GameType var9;
         if (var2.gameMode != null) {
            var9 = var2.gameMode.getPlayerMode();
         } else {
            var9 = GameType.SURVIVAL;
         }

         GameType var10 = var9;
         GameType var11;
         if (var2.gameMode != null) {
            var11 = var2.gameMode.getPreviousPlayerMode();
         } else {
            var11 = null;
         }

         GameType var12 = var11;
         Set var13 = var5.levels();
         ClientLevel var14 = var4;
         GameType var15 = var10;
         GameType var16 = var12;
         LocalPlayer var17 = var3;
         CommonPlayerSpawnInfo var18 = a(var14, var15, var16, var17, var1);
         int var19 = var3.getId();
         boolean var20 = var4.getLevelData().isHardcore();
         Set var21 = var13;
         CommonPlayerSpawnInfo var22 = var18;
         a_void_Connection_Packet(var0, new ClientboundLoginPacket(var19, var20, var21, 1, 16, 16, false, true, false, var22, false, false));
         CommonPlayerSpawnInfo var23 = var18;
         a_void_Connection_Packet(var0, new ClientboundRespawnPacket(var23, (byte)0));
         Difficulty var24 = var4.getLevelData().getDifficulty();
         boolean var25 = var4.getLevelData().isDifficultyLocked();
         a_void_Connection_Packet(var0, new ClientboundChangeDifficultyPacket(var24, var25));
         Abilities var26 = var3.getAbilities();
         a_void_Connection_Packet(var0, new ClientboundPlayerAbilitiesPacket(var26));
         int var27 = var3.getInventory().getSelectedSlot();
         a_void_Connection_Packet(var0, new ClientboundSetHeldSlotPacket(var27));
         ClientPacketListener var28 = var5;
         a_void_Connection_ClientPacketListener(var0, var28);
         ClientLevel var29 = var4;
         a_void_Connection_ClientLevel(var0, var29);
         a_void_Connection_Packet(var0, new ClientboundGameEventPacket(ClientboundGameEventPacket.LEVEL_CHUNKS_LOAD_START, 0.0F));
         LocalPlayer var30 = var3;
         ClientLevel var31 = var4;
         a(var0, var30, var31);
         float var32 = var3.getHealth();
         int var33 = var3.getFoodData().getFoodLevel();
         float var34 = var3.getFoodData().getSaturationLevel();
         a_void_Connection_Packet(var0, new ClientboundSetHealthPacket(var32, var33, var34));
         float var35 = var3.experienceProgress;
         int var36 = var3.totalExperience;
         int var37 = var3.experienceLevel;
         a_void_Connection_Packet(var0, new ClientboundSetExperiencePacket(var35, var36, var37));
         int var38 = var3.inventoryMenu.containerId;
         int var39 = var3.inventoryMenu.incrementStateId();
         NonNullList var40 = var3.inventoryMenu.getItems();
         ItemStack var41 = var3.inventoryMenu.getCarried();
         a_void_Connection_Packet(var0, new ClientboundContainerSetContentPacket(var38, var39, var40, var41));
         ClientLevel var42 = var4;
         LocalPlayer var43 = var3;
         a(var0, var42, var43);
         int var44 = var8;
         Vec3 var46 = var3.position();
         float var47 = var3.getYRot();
         float var48 = var3.getXRot();
         PositionMoveRotation var45 = new PositionMoveRotation(var46, Vec3.ZERO, var47, var48);
         Set var49 = Set.of();
         a_void_Connection_Packet(var0, new ClientboundPlayerPositionPacket(var44, var45, var49));
         LocalPlayer var50 = var3;
         a_void_Connection_Packet(var0, new ClientboundSetEntityMotionPacket(var50));
         long var51 = System.currentTimeMillis();
         long var53 = var6;
         Long var55 = var51 - var53;
         Integer var56 = var8;
         a.info("Level state replay completed in {}ms (teleportId={})", var55, var56);
         return var8;
      } else {
         a.error("Cannot replay world state: player/world/handler is null");
         return 0;
      }
   }

   private static void a_void_Connection_ClientLevel(Connection var0, ClientLevel var1) {
      WorldBorder var2 = var1.getWorldBorder();
      a_void_Connection_Packet(var0, new ClientboundInitializeBorderPacket(var2));
      long var3 = var1.getLevelData().getGameTime();
      Map var5 = Map.of();
      a_void_Connection_Packet(var0, new ClientboundSetTimePacket(var3, var5));
      RespawnData var6 = var1.getLevelData().getRespawnData();
      a_void_Connection_Packet(var0, new ClientboundSetDefaultSpawnPositionPacket(var6));
      if (var1.isRaining()) {
         a_void_Connection_Packet(var0, new ClientboundGameEventPacket(ClientboundGameEventPacket.START_RAINING, 0.0F));
         float var7 = var1.getRainLevel(1.0F);
         a_void_Connection_Packet(var0, new ClientboundGameEventPacket(ClientboundGameEventPacket.RAIN_LEVEL_CHANGE, var7));
         float var8 = var1.getThunderLevel(1.0F);
         a_void_Connection_Packet(var0, new ClientboundGameEventPacket(ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE, var8));
      }
   }

   private static void a_void_Connection_ClientPacketListener(Connection var0, ClientPacketListener var1) {
      Collection var2 = var1.getOnlinePlayers();
      if (!var2.isEmpty()) {
         ArrayList var3 = new ArrayList();

         for (PlayerInfo var5 : (Iterable<PlayerInfo>)(Object)var2) {
            ArrayList var6 = var3;
            UUID var7 = var5.getProfile().id();
            GameProfile var8 = var5.getProfile();
            Collection var9 = var1.getListedOnlinePlayers();
            PlayerInfo var10 = var5;
            boolean var11 = var9.contains(var10);
            int var12 = var5.getLatency();
            GameType var13 = var5.getGameMode();
            Component var14 = var5.getTabListDisplayName();
            var6.add(new Entry(var7, var8, var11, var12, var13, var14, false, 0, null));
         }

         EnumSet var15 = EnumSet.of(Action.ADD_PLAYER, Action.UPDATE_LISTED, Action.UPDATE_GAME_MODE, Action.UPDATE_LATENCY, Action.UPDATE_DISPLAY_NAME);
         EnumSet var16 = EnumSet.of(Action.ADD_PLAYER);
         List var17 = Collections.emptyList();
         ClientboundPlayerInfoUpdatePacket var18 = new ClientboundPlayerInfoUpdatePacket(var16, var17);
         PlayerListS2CPacketAccessor var19 = (PlayerListS2CPacketAccessor)var18;
         EnumSet var20 = var15;
         var19.setActions(var20);
         PlayerListS2CPacketAccessor var21 = (PlayerListS2CPacketAccessor)var18;
         ArrayList var22 = var3;
         var21.setEntries(var22);
         ClientboundPlayerInfoUpdatePacket var23 = var18;
         a_void_Connection_Packet(var0, var23);
         Integer var24 = var3.size();
         a.info("Sent {} player list entries to client", var24);
      }
   }

   private static CommonPlayerSpawnInfo a(ClientLevel var0, GameType var1, GameType var2, LocalPlayer var3, RegistryAccess var4) {
      ResourceKey var5 = (ResourceKey)var0.dimensionTypeRegistration().unwrapKey().orElseThrow();
      Registry var6 = var4.lookupOrThrow(Registries.DIMENSION_TYPE);
      ResourceKey var7 = var5;
      Reference var8 = var6.getOrThrow(var7);
      ResourceKey var9 = var0.dimension();
      long var10 = 0L;
      boolean var12 = var0.isDebug();
      int var13 = var3.getPortalCooldown();
      int var14 = var0.getSeaLevel();
      return new CommonPlayerSpawnInfo(var8, var9, var10, var1, var2, var12, false, var3.getLastDeathLocation(), var13, var14);
   }

   private static void a(Connection var0, ClientLevel var1, LocalPlayer var2) {
      int var3 = 0;

      for (Entity var5 : (Iterable<Entity>)(Object)var1.entitiesForRendering()) {
         if (var5 != var2) {
            try {
               int var7 = var5.getId();
               UUID var8 = var5.getUUID();
               double var9 = var5.getX();
               double var11 = var5.getY();
               double var13 = var5.getZ();
               float var15 = var5.getXRot();
               float var16 = var5.getYRot();
               EntityType var17 = var5.getType();
               Vec3 var18 = var5.getDeltaMovement();
               double var19 = var5.getYHeadRot();
               a_void_Connection_Packet(var0, new ClientboundAddEntityPacket(var7, var8, var9, var11, var13, var15, var16, var17, 0, var18, var19));
               List var21 = var5.getEntityData().getNonDefaultValues();
               if (var21 != null && !var21.isEmpty()) {
                  int var22 = var5.getId();
                  List var23 = var21;
                  a_void_Connection_Packet(var0, new ClientboundSetEntityDataPacket(var22, var23));
               }

               Entity var24 = var5;
               a_void_Connection_Packet(var0, new ClientboundSetEntityMotionPacket(var24));
               if (var5 instanceof LivingEntity var25) {
                  Entity var26 = var5;
                  byte var27 = (byte)(var25.getYHeadRot() * 256.0F / 360.0F);
                  a_void_Connection_Packet(var0, new ClientboundRotateHeadPacket(var26, var27));
                  ArrayList var28 = new ArrayList();
                  EquipmentSlot[] var29 = EquipmentSlot.values();
                  int var30 = var29.length;
                  int var31 = 0;

                  while (true) {
                     int var32 = var31;
                     int var33 = var30;
                     if (var32 >= var33) {
                        if (!var28.isEmpty()) {
                           int var43 = var5.getId();
                           ArrayList var44 = var28;
                           a_void_Connection_Packet(var0, new ClientboundSetEquipmentPacket(var43, var44));
                        }
                        break;
                     }

                     EquipmentSlot[] var34 = var29;
                     int var35 = var31;
                     EquipmentSlot var36 = var34[var35];
                     LivingEntity var37 = var25;
                     EquipmentSlot var38 = var36;
                     ItemStack var39 = var37.getItemBySlot(var38);
                     if (!var39.isEmpty()) {
                        ArrayList var40 = var28;
                        EquipmentSlot var41 = var36;
                        ItemStack var42 = var39;
                        var40.add(new Pair(var41, var42));
                     }

                     var31++;
                  }
               }

               var3++;
            } catch (Exception var50) {
               Object[] var46 = new Object[3];
               String var47 = var5.getType().getDescriptionId();
               var46[0] = var47;
               Integer var48 = var5.getId();
               var46[1] = var48;
               var46[2] = var50;
               a.warn("Failed to send entity {} (ID: {})", var46);
            }
         }
      }

      Integer var6 = var3;
      a.info("Sent {} entities to client", var6);
   }

   private static void a(Connection var0, LocalPlayer var1, ClientLevel var2) {
      int var3 = var1.chunkPosition().x();
      int var4 = var1.chunkPosition().z();
      int var5 = var3;
      int var6 = var4;
      a_void_Connection_Packet(var0, new ClientboundSetChunkCacheCenterPacket(var5, var6));
      int var7 = Math.min((Integer)Minecraft.getInstance().options.renderDistance().get(), 8);
      int var8 = 0;
      int var9 = var4;
      int var10 = var7;
      int var11 = var9 - var10;

      while (true) {
         int var12 = var11;
         int var13 = var4;
         int var14 = var7;
         int var15 = var13 + var14;
         if (var12 > var15) {
            Integer var29 = var8;
            a.info("Sent {} chunks to client", var29);
            return;
         }

         int var16 = var3;
         int var17 = var7;
         int var18 = var16 - var17;

         while (true) {
            int var19 = var18;
            int var20 = var3;
            int var21 = var7;
            int var22 = var20 + var21;
            if (var19 > var22) {
               var11++;
               break;
            }

            ClientChunkCache var23 = var2.getChunkSource();
            int var24 = var18;
            int var25 = var11;
            LevelChunk var26 = var23.getChunk(var24, var25, ChunkStatus.FULL, false);
            if (var26 != null) {
               LevelChunk var27 = var26;
               LevelLightEngine var28 = var2.getChunkSource().getLightEngine();
               a_void_Connection_Packet(var0, new ClientboundLevelChunkWithLightPacket(var27, var28, null, null));
               var8++;
            }

            var18++;
         }
      }
   }

   private static void a_void_Connection_Packet(Connection var0, Packet<?> var1) {
      if (var0.isConnected()) {
         var0.send(var1);
      }
   }
}
