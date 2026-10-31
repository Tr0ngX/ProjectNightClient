package night.pingbypass.world;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.stream.Stream;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySynchronization;
import net.minecraft.core.Registry.PendingTags;
import net.minecraft.core.RegistryAccess.Frozen;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundUpdateTagsPacket;
import net.minecraft.network.protocol.configuration.ClientboundRegistryDataPacket;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.KnownPack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.tags.TagLoader;
import net.minecraft.tags.TagNetworkSerialization;
import net.minecraft.world.level.WorldDataConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VanillaRegistryCache {
   private static final Logger a = LoggerFactory.getLogger(VanillaRegistryCache.class);
   private static final Executor b = Runnable::run;
   private final List<Packet<?>> c = new ArrayList<>();
   private final List<KnownPack> d = new ArrayList<>();
   private LayeredRegistryAccess<RegistryLayer> e;
   private RegistryAccess f;
   private boolean g;
   private volatile boolean h;

   public void a() {
      a.info("Loading vanilla registries for proxy...");
      long var1 = System.currentTimeMillis();

      try {
         PackRepository var3 = ServerPacksSource.createVanillaTrustedRepository();
         MinecraftServer.configurePackRepository(var3, WorldDataConfiguration.DEFAULT, true, true);
         List var4 = var3.openAllSelected();
         MultiPackResourceManager var5 = new MultiPackResourceManager(PackType.SERVER_DATA, var4);

         try {
            LayeredRegistryAccess var6 = RegistryLayer.createRegistryAccess();
            MultiPackResourceManager var7 = var5;
            Frozen var8 = var6.getLayer(RegistryLayer.STATIC);
            List var9 = TagLoader.loadTagsForExistingRegistries(var7, var8);
            Frozen var10 = var6.getAccessForLoading(RegistryLayer.WORLDGEN);
            List var11 = var9;
            List var12 = TagLoader.buildUpdatedLookups(var10, var11);
            MultiPackResourceManager var13 = var5;
            List var14 = var12;
            Frozen var15 = (Frozen)RegistryDataLoader.load(var13, var14, RegistryDataLoader.WORLDGEN_REGISTRIES, b).join();
            List var16 = Stream.concat(var12.stream(), var15.listRegistries()).toList();
            MultiPackResourceManager var17 = var5;
            List var18 = var16;
            Frozen var19 = (Frozen)RegistryDataLoader.load(var17, var18, RegistryDataLoader.DIMENSION_REGISTRIES, b).join();
            LayeredRegistryAccess var20 = var6;
            Frozen[] var21 = new Frozen[2];
            Frozen var22 = var15;
            var21[0] = var22;
            Frozen var23 = var19;
            var21[1] = var23;
            LayeredRegistryAccess var24 = var20.replaceFrom(RegistryLayer.WORLDGEN, var21);
            this.e = var24;
            Iterator var25 = var9.iterator();

            while (var25.hasNext()) {
               ((PendingTags)var25.next()).apply();
            }

            Frozen var26 = this.e.compositeAccess();
            this.f = var26;
            List var27 = this.d;
            List var28 = var5.listPacks().flatMap(var0 -> var0.knownPackInfo().stream()).toList();
            var27.addAll(var28);
            RegistryOps var29 = this.e.compositeAccess().createSerializationContext(NbtOps.INSTANCE);
            Frozen var30 = this.e.getAccessFrom(RegistryLayer.WORLDGEN);
            Set var31 = Set.of();
            RegistrySynchronization.packRegistries(var29, var30, var31, (var1x, var2) -> this.c.add(new ClientboundRegistryDataPacket(var1x, var2)));
            List var32 = this.c;
            Map var33 = TagNetworkSerialization.serializeTagsToNetwork(this.e);
            var32.add(new ClientboundUpdateTagsPacket(var33));
            this.g = true;
         } catch (Throwable var48) {
            try {
               var5.close();
            } catch (Throwable var47) {
               var48.addSuppressed(var47);
            }

            throw var48;
         }

         var5.close();
         long var38 = System.currentTimeMillis();
         long var40 = var1;
         long var42 = var38 - var40;
         Integer var44 = this.c.size();
         Long var45 = var42;
         a.info("Loaded {} registry packets in {}ms", var44, var45);
      } catch (Exception var49) {
         a.error("Failed to load vanilla registries", var49);
      }
   }

   public boolean b() {
      if (!this.g && !this.h) {
         this.h = true;
         this.a();
      }

      return this.g;
   }

   public List<Packet<?>> c() {
      return this.c;
   }

   public List<KnownPack> d() {
      return this.d;
   }

   public RegistryAccess e() {
      return this.f;
   }
}
