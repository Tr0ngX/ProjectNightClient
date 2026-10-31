package night.pingbypass.world;

import com.google.common.collect.Sets;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetHeldSlotPacket;
import net.minecraft.network.protocol.game.CommonPlayerSpawnInfo;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LobbyWorldSender {
   private static final Logger a = LoggerFactory.getLogger(LobbyWorldSender.class);

   private LobbyWorldSender() {
   }

   public static void a(Connection var0, RegistryAccess var1) {
      a.info("Sending lobby world to client...");

      try {
         Reference var2 = var1.lookupOrThrow(Registries.DIMENSION_TYPE).getOrThrow(BuiltinDimensionTypes.OVERWORLD);
         ResourceKey[] var3 = new ResourceKey[]{Level.OVERWORLD, Level.NETHER, Level.END};
         HashSet var4 = Sets.newHashSet(var3);
         Reference var5 = var2;
         long var6 = 0L;
         CommonPlayerSpawnInfo var8 = new CommonPlayerSpawnInfo(var5, Level.OVERWORLD, var6, GameType.SPECTATOR, null, false, true, Optional.empty(), 0, 63);
         HashSet var9 = var4;
         CommonPlayerSpawnInfo var10 = var8;
         a(var0, new ClientboundLoginPacket(1337, false, var9, 1, 16, 16, false, true, false, var10, false, false));
         Abilities var11 = a();
         a(var0, new ClientboundPlayerAbilitiesPacket(var11));
         a(var0, new ClientboundSetHeldSlotPacket(0));
         double var13 = 0.0;
         double var15 = 240.0;
         double var17 = 0.0;
         PositionMoveRotation var12 = new PositionMoveRotation(new Vec3(var13, var15, var17), Vec3.ZERO, 0.0F, 0.0F);
         Set var19 = Set.of();
         a(var0, new ClientboundPlayerPositionPacket(1, var12, var19));
         a(var0, new ClientboundGameEventPacket(ClientboundGameEventPacket.LEVEL_CHUNKS_LOAD_START, 0.0F));
         a.info("Lobby world sent successfully.");
      } catch (Exception var21) {
         a.error("Failed to send lobby world", var21);
      }
   }

   private static void a(Connection var0, Packet<?> var1) {
      if (var0.isConnected()) {
         var0.send(var1);
      }
   }

   private static Abilities a() {
      Abilities var0 = new Abilities();
      var0.mayfly = true;
      var0.flying = true;
      var0.invulnerable = true;
      return var0;
   }
}
