package night.module.impl.misc;

import com.mojang.authlib.GameProfile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.Generated;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Action;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Entry;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.events.EventTarget;
import night.events.impl.ClientConnectEvent;
import night.events.impl.PacketReceiveEvent;
import night.events.impl.PlayerDeathEvent;
import night.events.impl.Render3DEvent;
import night.events.impl.TickEvent;
import night.events.impl.UpdateWalkingPlayerEvent;
import night.mixins.accessors.PlayerListS2CPacketAccessor;
import night.module.Module;
import night.module.ModuleInfo;
import night.setting.impl.BooleanSetting;
import night.setting.impl.CategorySetting;
import night.setting.impl.ModeSetting;
import night.setting.impl.NumberSetting;
import night.setting.impl.StringSetting;
import night.util.math.MathUtil;
import night.util.math.TimerUtil;
import night.util.rotation.RotationMathUtil;
import night.util.world.BlockInteractionUtil;

@ModuleInfo(
   name = "FakePlayer",
   description = "Spawns in a fake player entity that you can use to test modules on.",
   category = Module.Category.MISCELLANEOUS,
   proxySupported = true
)
public class FakePlayer extends Module {
   public StringSetting name2 = new StringSetting("Name", "The name that will be assigned to the fake player.", "Dummy");
   public NumberSetting health;
   public NumberSetting absorption;
   public CategorySetting movementCategory;
   public ModeSetting movement;
   public NumberSetting velocity;
   public BooleanSetting changeDirection;
   public NumberSetting timeout;
   private RemotePlayer m;
   private double[] n;
   private final TimerUtil o;
   private boolean p;
   private final TimerUtil q;
   private FakePlayer.RecordState r;
   private final List<FakePlayer.RecordedFrame> s;
   private int t;

   public FakePlayer() {
      Float var1 = 20.0F;
      Float var2 = 1.0F;
      Float var3 = 20.0F;
      this.health = new NumberSetting("Health", "The amount of health that will be assigned to the fake player.", var1, var2, var3);
      Integer var4 = 16;
      Integer var5 = 0;
      Integer var6 = 16;
      this.absorption = new NumberSetting("Absorption", "The amount of absorption that will be assigned to the fake player.", var4, var5, var6);
      this.movementCategory = new CategorySetting("Movement", "The category that contains settings related to movement.");
      CategorySetting var8 = this.movementCategory;
      CategorySetting.CategoryCondition var7 = new CategorySetting.CategoryCondition(var8);
      String[] var9 = new String[]{"None", "Random"};
      this.movement = new ModeSetting("Movement", "Mode", "The mode that will be used for the fake player's movement.", var7, "None", var9);
      CategorySetting var11 = this.movementCategory;
      CategorySetting.CategoryCondition var10 = new CategorySetting.CategoryCondition(var11);
      Float var12 = 0.3F;
      Float var13 = 0.1F;
      Float var14 = 0.4F;
      this.velocity = new NumberSetting("Velocity", "Velocity", "The velocity to apply on the fake player.", var10, var12, var13, var14);
      CategorySetting var16 = this.movementCategory;
      CategorySetting.CategoryCondition var15 = new CategorySetting.CategoryCondition(var16);
      this.changeDirection = new BooleanSetting("ChangeDirection", "Changes the player's direction when a specified amount of time has passed.", var15, false);
      BooleanSetting var18 = this.changeDirection;
      BooleanSetting.BooleanCondition var17 = new BooleanSetting.BooleanCondition(var18, true);
      Integer var19 = 5;
      Integer var20 = 1;
      Integer var21 = 15;
      this.timeout = new NumberSetting("Timeout", "The amount of time that it takes for the player to change direction.", var17, var19, var20, var21);
      this.m = null;
      double[] var22 = this.y();
      this.n = var22;
      this.o = new TimerUtil();
      this.p = false;
      this.q = new TimerUtil();
      this.r = FakePlayer.RecordState.NONE;
      this.s = new ArrayList<>();
      this.t = 0;
   }

   public void v() {
      if (this.m == null) {
         Night.chatManager.sendWarning("Spawn a fake player first before recording!");
      } else {
         this.s.clear();
         this.r = FakePlayer.RecordState.RECORDING;
         Night.chatManager.sendTaggedMessage("Started recording movements...", "FakePlayer");
      }
   }

   public void w() {
      this.r = FakePlayer.RecordState.NONE;
      Night.chatManager.sendTaggedMessage("Record!", "FakePlayer");
   }

   public void x() {
      if (this.s.isEmpty()) {
         Night.chatManager.sendWarning("No recorded frames to play! Use '.fakeplayer record' first.");
      } else {
         if (this.m == null) {
            this.setEnabled(true);
         }

         this.r = FakePlayer.RecordState.PLAYING;
         this.t = 0;
         Night.chatManager.sendTaggedMessage("Playing recorded movements in a loop...", "FakePlayer");
      }
   }

   @EventTarget
   public void a_void_ClientConnectEvent(ClientConnectEvent var1) {
      if (this.isEnabled()) {
         this.setEnabled(false);
      }
   }

   @EventTarget
   public void a_void_PlayerDeathEvent(PlayerDeathEvent var1) {
      Player var2 = var1.getPlayer();
      LocalPlayer var3 = mc.player;
      if (var2 == var3) {
         if (this.isEnabled()) {
            this.setEnabled(false);
         }
      }
   }

   @EventTarget
   public void a_void_PacketReceiveEvent(PacketReceiveEvent var1) {
      if (mc.level != null && mc.player != null) {
         if (var1.getPacket() instanceof ClientboundEntityEventPacket var3 && var3.getEventId() == 3) {
            ClientboundEntityEventPacket var4 = var3;
            ClientLevel var5 = mc.level;
            Entity var6 = var4.getEntity(var5);
            LocalPlayer var7 = mc.player;
            if (var6 == var7 && this.isEnabled()) {
               mc.execute(() -> {
                  if (this.isEnabled()) {
                     this.setEnabled(false);
                  }
               });
            }
         }
      }
   }

   @EventTarget
   public void a_void_TickEvent(TickEvent var1) {
      if (mc.player != null
         && (mc.player.isDeadOrDying() || mc.player.getHealth() <= 0.0F || !mc.player.isAlive() || mc.gui != null && mc.gui.screen() instanceof DeathScreen)
         && this.isEnabled()) {
         this.setEnabled(false);
      }
   }

   @EventTarget
   public void a_void_UpdateWalkingPlayerEvent(UpdateWalkingPlayerEvent var1) {
      if (mc.player != null && mc.level != null) {
         if (this.r == FakePlayer.RecordState.RECORDING) {
            List var2 = this.s;
            double var3 = mc.player.getX();
            double var5 = mc.player.getY();
            double var7 = mc.player.getZ();
            float var9 = mc.player.getYRot();
            float var10 = mc.player.getXRot();
            float var11 = mc.player.getYHeadRot();
            Pose var12 = mc.player.getPose();
            var2.add(new FakePlayer.RecordedFrame(var3, var5, var7, var9, var10, var11, var12));
         } else if (this.r == FakePlayer.RecordState.PLAYING) {
            if (this.m != null && !this.s.isEmpty()) {
               int var92 = this.t + 1;
               this.t = var92;
               int var93 = this.t;
               int var94 = this.s.size();
               if (var93 >= var94) {
                  this.t = 0;
               }

               this.B();
            }
         } else if (this.m != null) {
            if (this.movement.getValue().equalsIgnoreCase("Random")) {
               BlockPos var13 = this.m.blockPosition();
               boolean var14 = false;
               if (this.changeDirection.getValue()) {
                  TimerUtil var15 = this.o;
                  long var16 = this.timeout.getValue().longValue();
                  long var18 = 1000L;
                  Long var20 = var16 * var18;
                  if (var15.passedMs(var20)) {
                     var14 = true;
                     this.o.reset();
                  }
               }

               BlockPos var21 = var13;
               if (this.a_bool_BlockPos(var21)) {
                  Direction[] var22 = Direction.values();
                  int var23 = var22.length;
                  int var24 = 0;

                  while (true) {
                     int var25 = var24;
                     int var26 = var23;
                     if (var25 >= var26) {
                        break;
                     }

                     label62: {
                        Direction[] var27 = var22;
                        int var28 = var24;
                        Direction var29 = var27[var28];
                        BlockPos var30 = var13;
                        Direction var31 = var29;
                        BlockPos var32 = var30.relative(var31);
                        ClientLevel var33 = mc.level;
                        BlockPos var34 = var32;
                        if (!BlockInteractionUtil.b_bool_BlockState(var33.getBlockState(var34))) {
                           ClientLevel var35 = mc.level;
                           BlockPos var36 = var32.above();
                           if (!BlockInteractionUtil.b_bool_BlockState(var35.getBlockState(var36))) {
                              break label62;
                           }
                        }

                        ClientLevel var37 = mc.level;
                        BlockPos var38 = var32.above().above();
                        if (BlockInteractionUtil.b_bool_BlockState(var37.getBlockState(var38))) {
                           Direction var39 = this.m.getDirection();
                           Direction var40 = var29;
                           if (var39.equals(var40)) {
                              var14 = true;
                              this.o.reset();
                           }
                        }
                     }

                     var24++;
                  }
               }

               if (var14) {
                  double[] var41 = this.y();
                  double[] var42 = this.n;
                  double[] var43 = var41;
                  if (var42 == var43) {
                     var41 = this.y();
                  }

                  double[] var44 = var41;
                  this.n = var44;
               }

               if (this.p) {
                  TimerUtil var45 = this.q;
                  Long var46 = 500L;
                  if (var45.passedMs(var46)) {
                     this.p = false;
                     this.q.reset();
                  }
               }

               this.m.jumpFromGround();
               RemotePlayer var47 = this.m;
               double var48 = this.m.getX();
               double var50 = this.n[0];
               double var52 = var48 + var50;
               double var54 = this.m.getY();
               RemotePlayer var56 = this.m;
               double var57 = this.a_float_Player(var56);
               double var59 = var54 + var57;
               double var61 = this.m.getZ();
               double var63 = this.n[1];
               double var65 = var61 + var63;
               float[] var67 = RotationMathUtil.a(var47, var52, var59, var65);
               RemotePlayer var68 = this.m;
               float var69 = var67[0];
               var68.setYRot(var69);
               RemotePlayer var70 = this.m;
               float var71 = var67[0];
               var70.yHeadRot = var71;
               this.m.setXRot(0.0F);
               RemotePlayer var72 = this.m;
               double var73 = this.m.getX();
               double var75 = this.n[0];
               double var77 = var73 + var75;
               double var79 = this.m.getY();
               RemotePlayer var81 = this.m;
               double var82 = this.a_float_Player(var81);
               double var84 = var79 + var82;
               double var86 = this.m.getZ();
               double var88 = this.n[1];
               double var90 = var86 + var88;
               var72.setPos(var77, var84, var90);
               this.B();
            }
         }
      }
   }

   @EventTarget
   public void a_void_Render3DEvent(Render3DEvent var1) {
      if (this.r == FakePlayer.RecordState.PLAYING && this.m != null && !this.s.isEmpty()) {
         int var2 = this.t;
         int var3 = var2 + 1;
         int var4 = this.s.size();
         int var5 = var3 % var4;
         List var6 = this.s;
         int var7 = var2;
         FakePlayer.RecordedFrame var8 = (FakePlayer.RecordedFrame)var6.get(var7);
         List var9 = this.s;
         int var10 = var5;
         FakePlayer.RecordedFrame var11 = (FakePlayer.RecordedFrame)var9.get(var10);
         float var12 = var1.getTickDelta();
         if (var5 == 0) {
            var12 = 0.0F;
         }

         double var13 = var12;
         double var15 = var8.distance();
         double var17 = var11.distance();
         double var19 = Mth.lerp(var13, var15, var17);
         double var21 = var12;
         double var23 = var8.distance2();
         double var25 = var11.distance2();
         double var27 = Mth.lerp(var21, var23, var25);
         double var29 = var12;
         double var31 = var8.distance3();
         double var33 = var11.distance3();
         double var35 = Mth.lerp(var29, var31, var33);
         float var37 = var12;
         float var38 = var8.value();
         float var39 = var11.value();
         float var40 = Mth.rotLerp(var37, var38, var39);
         float var41 = var12;
         float var42 = var8.value2();
         float var43 = var11.value2();
         float var44 = Mth.lerp(var41, var42, var43);
         float var45 = var12;
         float var46 = var8.value3();
         float var47 = var11.value3();
         float var48 = Mth.rotLerp(var45, var46, var47);
         RemotePlayer var49 = this.m;
         double var50 = var19;
         double var52 = var27;
         double var54 = var35;
         var49.setPos(var50, var52, var54);
         RemotePlayer var56 = this.m;
         float var57 = var40;
         var56.setYRot(var57);
         RemotePlayer var58 = this.m;
         float var59 = var44;
         var58.setXRot(var59);
         RemotePlayer var60 = this.m;
         float var61 = var48;
         var60.yHeadRot = var61;
         RemotePlayer var62 = this.m;
         float var63 = var40;
         var62.yBodyRot = var63;
         this.m.setOldPosAndRot();
         RemotePlayer var64 = this.m;
         float var65 = var48;
         var64.yHeadRotO = var65;
         RemotePlayer var66 = this.m;
         float var67 = var40;
         var66.yBodyRotO = var67;
         RemotePlayer var68 = this.m;
         Pose var69 = var8.pose();
         var68.setPose(var69);
         this.m.refreshDimensions();
      }
   }

   private void B() {
      if (this.isRunningOnProxyServer() && Night.pingBypassServer != null) {
         if (this.m != null) {
            int var1 = this.m.getId();
            PositionMoveRotation var2 = PositionMoveRotation.of(this.m);
            Set var3 = Set.of();
            boolean var4 = this.m.onGround();
            ClientboundTeleportEntityPacket var5 = ClientboundTeleportEntityPacket.teleport(var1, var2, var3, var4);

            for (Connection var7 : (Iterable<Connection>)(Object)Night.pingBypassServer.getConnections()) {
               if (var7.isConnected()) {
                  Connection var8 = var7;
                  ClientboundTeleportEntityPacket var9 = var5;
                  var8.send(var9);
               }
            }
         }
      }
   }

   @Override
   public void onEnable() {
      if (!this.isProxyDelegated()) {
         if (mc.level != null && mc.player != null) {
            ClientLevel var1 = mc.level;
            UUID var3 = UUID.randomUUID();
            String var4 = this.name2.getValue();
            GameProfile var2 = new GameProfile(var3, var4);
            this.m = new RemotePlayer(var1, var2);
            RemotePlayer var5 = this.m;
            LocalPlayer var6 = mc.player;
            var5.copyPosition(var6);
            this.m.setId(-673);
            RemotePlayer var7 = this.m;
            LocalPlayer var8 = mc.player;
            var7.restoreFrom(var8);
            RemotePlayer var9 = this.m;
            float var10 = this.health.getValue().floatValue();
            var9.setHealth(var10);
            RemotePlayer var11 = this.m;
            float var12 = this.absorption.getValue().floatValue();
            var11.setAbsorptionAmount(var12);
            EquipmentSlot[] var13 = EquipmentSlot.values();
            int var14 = var13.length;
            int var15 = 0;

            while (true) {
               int var16 = var15;
               int var17 = var14;
               if (var16 >= var17) {
                  ClientLevel var26 = mc.level;
                  RemotePlayer var27 = this.m;
                  var26.addEntity(var27);
                  this.m.tick();
                  this.o.reset();
                  this.C();
                  Night.chatManager.sendTaggedMessage("Spawned fake player " + this.name2.getValue(), "FakePlayer");
                  return;
               }

               EquipmentSlot[] var18 = var13;
               int var19 = var15;
               EquipmentSlot var20 = var18[var19];
               RemotePlayer var21 = this.m;
               EquipmentSlot var22 = var20;
               LocalPlayer var23 = mc.player;
               EquipmentSlot var24 = var20;
               ItemStack var25 = var23.getItemBySlot(var24).copy();
               var21.setItemSlot(var22, var25);
               var15++;
            }
         } else {
            this.setEnabled(false);
         }
      }
   }

   @Override
   public void onDisable() {
      this.r = FakePlayer.RecordState.NONE;
      if (!this.isProxyDelegated()) {
         if (this.m != null) {
            this.D();
            if (mc.level != null) {
               ClientLevel var1 = mc.level;
               int var2 = this.m.getId();
               var1.removeEntity(var2, RemovalReason.DISCARDED);
            }

            this.m = null;
            Night.chatManager.sendTaggedMessage("Removed fake player.", "FakePlayer");
         }
      }
   }

   private void C() {
      if (this.isRunningOnProxyServer() && Night.pingBypassServer != null) {
         if (this.m != null) {
            UUID var1 = this.m.getUUID();
            GameProfile var2 = this.m.getGameProfile();
            List var3 = List.of(new Entry(var1, var2, false, 0, GameType.SURVIVAL, null, false, 0, null));
            EnumSet var4 = EnumSet.of(Action.ADD_PLAYER);
            List var5 = Collections.emptyList();
            ClientboundPlayerInfoUpdatePacket var6 = new ClientboundPlayerInfoUpdatePacket(var4, var5);
            PlayerListS2CPacketAccessor var7 = (PlayerListS2CPacketAccessor)var6;
            EnumSet var8 = EnumSet.of(Action.ADD_PLAYER);
            var7.setActions(var8);
            PlayerListS2CPacketAccessor var9 = (PlayerListS2CPacketAccessor)var6;
            List var10 = var3;
            var9.setEntries(var10);
            int var11 = this.m.getId();
            UUID var12 = this.m.getUUID();
            double var13 = this.m.getX();
            double var15 = this.m.getY();
            double var17 = this.m.getZ();
            float var19 = this.m.getXRot();
            float var20 = this.m.getYRot();
            EntityType var21 = this.m.getType();
            Vec3 var22 = this.m.getDeltaMovement();
            double var23 = this.m.getYHeadRot();
            ClientboundAddEntityPacket var25 = new ClientboundAddEntityPacket(var11, var12, var13, var15, var17, var19, var20, var21, 0, var22, var23);

            for (Connection var27 : (Iterable<Connection>)(Object)Night.pingBypassServer.getConnections()) {
               if (var27.isConnected()) {
                  Connection var28 = var27;
                  ClientboundPlayerInfoUpdatePacket var29 = var6;
                  var28.send(var29);
                  Connection var30 = var27;
                  ClientboundAddEntityPacket var31 = var25;
                  var30.send(var31);
               }
            }
         }
      }
   }

   private void D() {
      if (this.isRunningOnProxyServer() && Night.pingBypassServer != null) {
         if (this.m != null) {
            int[] var1 = new int[1];
            int var2 = this.m.getId();
            var1[0] = var2;
            ClientboundRemoveEntitiesPacket var3 = new ClientboundRemoveEntitiesPacket(var1);
            List var4 = List.of(this.m.getUUID());
            ClientboundPlayerInfoRemovePacket var5 = new ClientboundPlayerInfoRemovePacket(var4);

            for (Connection var7 : (Iterable<Connection>)(Object)Night.pingBypassServer.getConnections()) {
               if (var7.isConnected()) {
                  Connection var8 = var7;
                  ClientboundRemoveEntitiesPacket var9 = var3;
                  var8.send(var9);
                  Connection var10 = var7;
                  ClientboundPlayerInfoRemovePacket var11 = var5;
                  var10.send(var11);
               }
            }
         }
      }
   }

   public boolean a_bool_BlockPos(BlockPos var1) {
      Direction[] var2 = Direction.values();
      int var3 = var2.length;
      int var4 = 0;

      while (true) {
         int var5 = var4;
         int var6 = var3;
         if (var5 >= var6) {
            return false;
         }

         Direction[] var7 = var2;
         int var8 = var4;
         Direction var9 = var7[var8];
         BlockPos var10 = var1.relative(var9);
         ClientLevel var11 = mc.level;
         BlockPos var12 = var10;
         if (BlockInteractionUtil.b_bool_BlockState(var11.getBlockState(var12))) {
            return true;
         }

         ClientLevel var13 = mc.level;
         BlockPos var14 = var10.above();
         if (BlockInteractionUtil.b_bool_BlockState(var13.getBlockState(var14))) {
            return true;
         }

         var4++;
      }
   }

   public float a_float_Player(Player var1) {
      ClientLevel var2 = mc.level;
      BlockPos var3 = var1.blockPosition().below();
      if (var2.getBlockState(var3).getBlock() == Blocks.AIR && !this.p) {
         return -1.0F;
      }

      BlockPos var4 = var1.blockPosition();
      if (this.a_bool_BlockPos(var4)) {
         Direction[] var5 = Direction.values();
         int var6 = var5.length;
         int var7 = 0;

         while (true) {
            int var8 = var7;
            int var9 = var6;
            if (var8 >= var9) {
               return 0.0F;
            }

            Direction[] var10 = var5;
            int var11 = var7;
            Direction var12 = var10[var11];
            BlockPos var13 = var1.blockPosition();
            Direction var14 = var12;
            BlockPos var15 = var13.relative(var14);
            ClientLevel var16 = mc.level;
            BlockPos var17 = var15;
            if (BlockInteractionUtil.b_bool_BlockState(var16.getBlockState(var17))) {
               ClientLevel var18 = mc.level;
               BlockPos var19 = var15.above();
               if (!BlockInteractionUtil.b_bool_BlockState(var18.getBlockState(var19))) {
                  Direction var20 = var1.getDirection();
                  Direction var21 = var12;
                  if (var20.equals(var21)) {
                     ClientLevel var22 = mc.level;
                     BlockPos var23 = var15.above().above();
                     if (!BlockInteractionUtil.b_bool_BlockState(var22.getBlockState(var23))) {
                        this.p = true;
                        this.q.reset();
                        return 1.0F;
                     }
                  }
               }
            }

            ClientLevel var24 = mc.level;
            BlockPos var25 = var15.above();
            if (BlockInteractionUtil.b_bool_BlockState(var24.getBlockState(var25))) {
               Direction var26 = var1.getDirection();
               Direction var27 = var12;
               if (var26.equals(var27)) {
                  ClientLevel var28 = mc.level;
                  BlockPos var29 = var15.above().above();
                  if (!BlockInteractionUtil.b_bool_BlockState(var28.getBlockState(var29))) {
                     this.p = true;
                     this.q.reset();
                     return 2.0F;
                  }
               }
            }

            var7++;
         }
      } else {
         return 0.0F;
      }
   }

   public double[] y() {
      double var1 = Math.PI * 2;
      double var3 = 0.0;
      double var5 = MathUtil.a_double_double_double(var1, var3);
      double[] var7 = new double[2];
      double var8 = -Math.sin(var5);
      var7[0] = var8;
      double var10 = Math.cos(var5);
      var7[1] = var10;
      double[] var12 = var7;
      double[] var13 = new double[2];
      double var14 = var12[0];
      double var16 = this.velocity.getValue().floatValue();
      double var18 = var14 * var16;
      var13[0] = var18;
      double var20 = var12[1];
      double var22 = this.velocity.getValue().floatValue();
      double var24 = var20 * var22;
      var13[1] = var24;
      return var13;
   }

   @Generated
   public RemotePlayer z() {
      return this.m;
   }

   @Generated
   public FakePlayer.RecordState A() {
      return this.r;
   }

   enum RecordState {
      NONE,
      RECORDING,
      PLAYING;
   }

   public record RecordedFrame(double distance, double distance2, double distance3, float value, float value2, float value3, Pose pose) {
      public RecordedFrame(double distance, double distance2, double distance3, float value, float value2, float value3, Pose pose) {
         double var11 = distance;
         this.distance = var11;
         double var13 = distance2;
         this.distance2 = var13;
         double var15 = distance3;
         this.distance3 = var15;
         this.value = value;
         this.value2 = value2;
         this.value3 = value3;
         this.pose = pose;
      }
   }
}
