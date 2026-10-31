package night.manager;

import it.unimi.dsi.fastutil.ints.IntListIterator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentHashMap.KeySetView;
import lombok.Generated;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSetHeldSlotPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.events.EventTarget;
import night.events.impl.PacketReceiveEvent;
import night.events.impl.PlayerDeathEvent;
import night.events.impl.TickEvent;
import night.events.impl.TotemPopEvent;
import night.util.MinecraftInstance;
import night.util.inventory.InventoryUtil;
import night.util.math.TimerUtil;
import night.util.world.EntityUtil;
import night.util.world.PacketAndRenderUtil;

public class WorldStateManager implements MinecraftInstance {
   private static final Vec3i[] SPHERE_OFFSETS = new Vec3i[4187707];
   private static final int[] SPHERE_RADII_INDICES = new int[101];
   private final Map<UUID, Integer> totemPops = new ConcurrentHashMap<>();
   private final Set<UUID> deadPlayers;
   private float timerMultiplier;
   private final TimerUtil switchTimer;
   private final Set<BlockPos> placedPositions;
   private final Map<Integer, Runnable> entityRemoveCallbacks;
   private volatile Vec3 lastVelocity;
   private Player l;
   public volatile long a;
   public volatile double b;
   private volatile int m;

   public void recordPlacedPosition(BlockPos var1) {
      this.placedPositions.add(var1);
   }

   public Set<BlockPos> getPlacedPositions() {
      return this.placedPositions;
   }

   public void onEntityRemoved(int var1, Runnable var2) {
      Map var3 = this.entityRemoveCallbacks;
      Integer var4 = var1;
      var3.put(var4, var2);
   }

   public WorldStateManager() {
      KeySetView var1 = ConcurrentHashMap.newKeySet();
      this.deadPlayers = var1;
      this.timerMultiplier = 1.0F;
      this.switchTimer = new TimerUtil();
      KeySetView var2 = ConcurrentHashMap.newKeySet();
      this.placedPositions = var2;
      this.entityRemoveCallbacks = new ConcurrentHashMap<>();
      this.lastVelocity = Vec3.ZERO;
      long var3 = 0L;
      this.a = var3;
      double var5 = 0.0;
      this.b = var5;
      this.m = 0;
      Night.EVENT_BUS.register(this);
      BlockPos var7 = BlockPos.ZERO;
      final BlockPos var9 = var7;
      Comparator var8 = new Comparator<BlockPos>() {
         public int compare(BlockPos var1, BlockPos var2x) {
            if (var1.equals(var2x)) {
               return 0;
            }

            double var3x = var9.distSqr(var1);
            double var5x = var9.distSqr(var2x);
            int var7x = Double.compare(var3x, var5x);
            if (var7x == 0) {
               int var8x = Math.abs(var1.getX());
               int var9x = Math.abs(var1.getY());
               int var10 = var8x + var9x;
               int var11 = Math.abs(var1.getZ());
               int var12 = var10 + var11;
               int var13 = Math.abs(var2x.getX());
               int var14 = Math.abs(var2x.getY());
               int var15 = var13 + var14;
               int var16 = Math.abs(var2x.getZ());
               int var17 = var15 + var16;
               var7x = Integer.compare(var12, var17);
            }

            int var18;
            if (var7x == 0) {
               var18 = 1;
            } else {
               var18 = var7x;
            }

            return var18;
         }
      };
      TreeSet var10 = new TreeSet(var8);
      int var11 = var7.getX() - 100;

      while (true) {
         int var12 = var11;
         int var13 = var7.getX() + 100;
         if (var12 > var13) {
            int var50 = 0;
            int var51 = 0;

            for (BlockPos var53 : (Iterable<BlockPos>)(Object)var10) {
               BlockPos var54 = var7;
               BlockPos var55 = var53;
               double var56 = Math.sqrt(var54.distSqr(var55));
               double var58 = var51;
               if (var56 > var58) {
                  int var60 = var51++;
                  int var61 = var50;
                  SPHERE_RADII_INDICES[var60] = var61;
               }

               int var62 = var50++;
               BlockPos var63 = var53;
               SPHERE_OFFSETS[var62] = var63;
            }

            return;
         }

         int var14 = var7.getZ() - 100;

         while (true) {
            int var15 = var14;
            int var16 = var7.getZ() + 100;
            if (var15 > var16) {
               var11++;
               break;
            }

            int var17 = var7.getY() - 100;

            while (true) {
               int var18 = var17;
               int var19 = var7.getY() + 100;
               if (var18 >= var19) {
                  var14++;
                  break;
               }

               int var20 = var7.getX();
               int var21 = var11;
               int var22 = var20 - var21;
               int var23 = var7.getX();
               int var24 = var11;
               int var25 = var23 - var24;
               int var26 = var22 * var25;
               int var27 = var7.getZ();
               int var28 = var14;
               int var29 = var27 - var28;
               int var30 = var7.getZ();
               int var31 = var14;
               int var32 = var30 - var31;
               int var33 = var29 * var32;
               int var34 = var26 + var33;
               int var35 = var7.getY();
               int var36 = var17;
               int var37 = var35 - var36;
               int var38 = var7.getY();
               int var39 = var17;
               int var40 = var38 - var39;
               int var41 = var37 * var40;
               double var42 = var34 + var41;
               double var44 = Mth.square(100);
               if (var42 < var44) {
                  TreeSet var46 = var10;
                  int var47 = var11;
                  int var48 = var17;
                  int var49 = var14;
                  var46.add(new BlockPos(var47, var48, var49));
               }

               var17++;
            }
         }
      }
   }

   @EventTarget
   public void a_void_TickEvent(TickEvent var1) {
      this.placedPositions.clear();
      if (this.m > 0) {
         int var2 = this.m - 1;
         this.m = var2;
         if (var2 == 0 && mc.player != null) {
            Object[] var3 = new Object[2];
            double var4 = mc.player.getY();
            double var6 = this.b;
            Double var8 = var4 - var6;
            var3[0] = var8;
            Boolean var9 = mc.player.onGround();
            var3[1] = var9;
            String var10 = String.format("settled 1s after tp: feet dY=%.6f, onGround=%s", var3);
            Night.chatManager.sendTaggedMessage(var10, "Phase");
         }
      }

      LocalPlayer var11 = mc.player;
      Player var12 = this.l;
      if (var11 != var12) {
         LocalPlayer var13 = mc.player;
         this.l = var13;
         this.lastVelocity = Vec3.ZERO;
      } else if (mc.player != null) {
         Vec3 var14 = this.lastVelocity;
         if (mc.player.horizontalCollision) {
            double var15 = 0.0;
            double var17 = var14.y;
            double var19 = 0.0;
            var14 = new Vec3(var15, var17, var19);
         }

         if (mc.player.verticalCollision) {
            double var21 = var14.x;
            double var23 = 0.0;
            double var25 = var14.z;
            var14 = new Vec3(var21, var23, var25);
         }

         Vec3 var27 = var14;
         this.lastVelocity = var27;
      }

      InventoryUtil.c();
      if (mc.level != null) {
         List var28 = mc.level.players();
         ArrayList<Player> var29 = new ArrayList<>(var28);
         Set var30 = this.totemPops.keySet();

         for (UUID var32 : (Iterable<UUID>)(Object)new ArrayList(var30)) {
            if (var29.stream().noneMatch(var1x -> var1x.getUUID().equals(var32))) {
               Map var33 = this.totemPops;
               UUID var34 = var32;
               var33.remove(var34);
            }
         }

         for (Player var36 : (Iterable<Player>)(Object)var29) {
            if (var36.deathTime <= 0 && var36.getHealth() > 0.0F) {
               Set var37 = this.deadPlayers;
               UUID var38 = var36.getUUID();
               var37.remove(var38);
            } else {
               Set var39 = this.deadPlayers;
               UUID var40 = var36.getUUID();
               if (!var39.contains(var40)) {
                  Player var41 = var36;
                  Night.EVENT_BUS.post(new PlayerDeathEvent(var41));
                  Set var42 = this.deadPlayers;
                  UUID var43 = var36.getUUID();
                  var42.add(var43);
                  Map var44 = this.totemPops;
                  UUID var45 = var36.getUUID();
                  var44.remove(var45);
               }
            }
         }
      }
   }

   @EventTarget
   public void a_void_PacketReceiveEvent(PacketReceiveEvent var1) {
      if (mc.level != null) {
         if (var1.getPacket() instanceof ClientboundSetHeldSlotPacket var3 && mc.player != null) {
            int var4 = var3.slot();
            int var5 = mc.player.getInventory().getSelectedSlot();
            if (var4 != var5) {
               var1.setCancelled(true);
               return;
            }
         }

         if (var1.getPacket() instanceof ClientboundSetEntityMotionPacket var7 && EntityUtil.a_bool_int(var7.id())) {
            Vec3 var8 = var7.movement();
            this.lastVelocity = var8;
         }

         Packet var9 = var1.getPacket();
         if (var9 instanceof ClientboundExplodePacket) {
            ((ClientboundExplodePacket)var9).playerKnockback().ifPresent(var1x -> {
               Vec3 var2 = this.lastVelocity.add(var1x);
               this.lastVelocity = var2;
            });
         }

         if (var1.getPacket() instanceof ClientboundPlayerPositionPacket var11) {
            long var12 = System.currentTimeMillis();
            long var14 = this.a;
            if (var12 < var14) {
               long var16 = 0L;
               this.a = var16;
               Vec3 var18 = var11.change().position();
               boolean var19 = var11.relatives().contains(Relative.Y);
               double var20 = this.b;
               Vec3 var22 = var18;
               boolean var23 = var19;
               double var24 = var20;
               mc.execute(() -> {
                  ChatManager var4x = Night.chatManager;
                  Object[] var5x = new Object[5];
                  Double var6 = var22.x;
                  var5x[0] = var6;
                  Double var7x = var22.y;
                  var5x[1] = var7x;
                  String var8x;
                  if (var23) {
                     var8x = " rel";
                  } else {
                     var8x = "";
                  }

                  var5x[2] = var8x;
                  Double var9x = var22.z;
                  var5x[3] = var9x;
                  double var10;
                  if (var23) {
                     var10 = var22.y;
                  } else {
                     double var12x = var22.y;
                     double var14x = var24;
                     var10 = var12x - var14x;
                  }

                  Double var16x = var10;
                  var5x[4] = var16x;
                  String var17 = String.format("server tp: (%.6f, %.6f%s, %.6f) dY vs feet=%.6f", var5x);
                  var4x.sendTaggedMessage(var17, "Phase");
               });
               this.m = 20;
            }
         }

         if (var1.getPacket() instanceof ClientboundContainerSetSlotPacket var27
            && var27.getContainerId() == 0
            && var27.getItem().isEmpty()
            && InventoryUtil.a_bool_int(var27.getSlot())) {
            var1.setCancelled(true);
         } else {
            if (var1.getPacket() instanceof ClientboundEntityEventPacket var29 && var29.getEventId() == 35) {
               ClientboundEntityEventPacket var30 = var29;
               ClientLevel var31 = mc.level;
               if (!(var30.getEntity(var31) instanceof Player var33)) {
                  return;
               }

               Map var34 = this.totemPops;
               UUID var35 = var33.getUUID();
               Integer var36 = 0;
               int var37 = (Integer)var34.getOrDefault(var35, var36);
               Map var38 = this.totemPops;
               UUID var39 = var33.getUUID();
               int var40 = var37 + 1;
               Integer var41 = var40;
               var38.put(var39, var41);
               Player var42 = var33;
               int var43 = var40;
               Night.EVENT_BUS.post(new TotemPopEvent(var42, var43));
            }

            if (!this.entityRemoveCallbacks.isEmpty()) {
               Packet var44 = var1.getPacket();
               if (var44 instanceof ClientboundRemoveEntitiesPacket) {
                  IntListIterator var45 = PacketAndRenderUtil.a_IntList_ClientboundRemoveEntitiesPacket((ClientboundRemoveEntitiesPacket)var44).iterator();

                  while (var45.hasNext()) {
                     int var46 = (Integer)var45.next();
                     Map var47 = this.entityRemoveCallbacks;
                     Integer var48 = var46;
                     Runnable var49 = (Runnable)var47.remove(var48);
                     if (var49 != null) {
                        var49.run();
                     }
                  }
               }
            }
         }
      }
   }

   public int getSpherePointCount(double var1) {
      int var3 = (int)Math.ceil(var1);
      int var4 = SPHERE_RADII_INDICES.length;
      int var5 = Mth.clamp(var3, 0, var4);
      return SPHERE_RADII_INDICES[var5];
   }

   public Vec3i getSphereOffset(int var1) {
      return SPHERE_OFFSETS[var1];
   }

   @Generated
   public Map<UUID, Integer> getTotemPops() {
      return this.totemPops;
   }

   @Generated
   public float getTimerMultiplier() {
      return this.timerMultiplier;
   }

   @Generated
   public void setTimerMultiplier(float var1) {
      this.timerMultiplier = var1;
   }

   @Generated
   public TimerUtil getSwitchTimer() {
      return this.switchTimer;
   }

   @Generated
   public Vec3 getLastVelocity() {
      return this.lastVelocity;
   }
}
