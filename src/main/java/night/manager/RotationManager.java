package night.manager;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.PriorityBlockingQueue;
import lombok.Generated;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.PosRot;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.Rot;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import night.Night;
import night.events.EventTarget;
import night.events.impl.KeyboardTickEvent;
import night.events.impl.MotionSyncEvent;
import night.events.impl.PacketSendEvent;
import night.events.impl.PlayerJumpEvent;
import night.events.impl.RotationUpdateEvent;
import night.events.impl.UpdateVelocityEvent;
import night.events.impl.UpdateWalkingPlayerEvent;
import night.mixins.accessors.EntityAccessor;
import night.module.Module;
import night.module.impl.core.RotationsModule;
import night.pingbypass.PingBypassState;
import night.pingbypass.network.RubberbandTracker;
import night.util.MinecraftInstance;
import night.util.animation.EasingUtil;
import night.util.math.MathUtil;
import night.util.network.PacketPredictionUtil;
import night.util.rotation.Rotation;
import night.util.rotation.RotationMathUtil;
import night.util.rotation.RotationRequest;

public class RotationManager implements MinecraftInstance {
   private Rotation currentRotation = null;
   private Module activeModule = null;
   private float e;
   private float f;
   private float serverYaw;
   private float serverPitch;
   private float i = Float.NaN;
   private float j = Float.NaN;
   private float k = Float.NaN;
   private float l = Float.NaN;
   private double m;
   private double n;
   private double o;
   private float p;
   private float q;
   private float r;
   private float s;
   private long t;
   private boolean u;
   private boolean v;
   private float w;
   private float x;
   public static boolean spoofingRotation = false;
   private final PriorityBlockingQueue<RotationRequest> y;
   private static final HashMap<String, Integer> z = new HashMap<>();
   private float A;
   private final ArrayDeque<Long> B;
   private double C;
   private double D;
   private double E;
   private float F;
   private float G;
   private int H;
   public static volatile boolean flushingQueue = false;
   private boolean I;
   private boolean J;
   private boolean K;
   private float L;
   private float M;
   private final ArrayDeque<Packet<?>> N;
   private long O;
   private boolean P;

   public RotationManager() {
      double var1 = Double.NaN;
      this.m = var1;
      double var3 = Double.NaN;
      this.n = var3;
      double var5 = Double.NaN;
      this.o = var5;
      this.p = Float.NaN;
      this.q = Float.NaN;
      long var7 = 0L;
      this.t = var7;
      this.u = false;
      this.v = false;
      this.y = new PriorityBlockingQueue<>(11, this::a_int_RotationRequest_RotationRequest);
      this.B = new ArrayDeque<>();
      double var9 = Double.NaN;
      this.C = var9;
      this.H = -1;
      this.I = false;
      this.J = false;
      this.K = false;
      this.N = new ArrayDeque<>();
      long var11 = 0L;
      this.O = var11;
      this.P = false;
      Night.EVENT_BUS.register(this);
   }

   @EventTarget(priority = Integer.MIN_VALUE)
   public void a_void_UpdateWalkingPlayerEvent(UpdateWalkingPlayerEvent var1) {
      if (mc.player != null) {
         long var2 = System.currentTimeMillis();
         float var4 = mc.player.getYRot();
         float var5 = mc.player.getXRot();
         float[] var6 = RotationMathUtil.a(var2, var4, var5);
         if (var6 != null && var6.length == 2) {
            LocalPlayer var7 = mc.player;
            float var8 = var6[0];
            var7.setYRot(var8);
            LocalPlayer var9 = mc.player;
            float var10 = var6[1];
            var9.setXRot(var10);
            LocalPlayer var11 = mc.player;
            float var12 = var6[0];
            var11.yRotO = var12;
            LocalPlayer var13 = mc.player;
            float var14 = var6[1];
            var13.xRotO = var14;
            EntityAccessor var15 = (EntityAccessor)mc.player;
            float var16 = var6[0];
            var15.setRawYRot(var16);
            EntityAccessor var17 = var15;
            float var18 = var6[1];
            var17.setRawXRot(var18);
         }

         this.y.removeIf(var0 -> {
            long var1x = System.currentTimeMillis();
            long var3 = var0.getTimestamp();
            long var5x = var1x - var3;
            long var7x = 100L;
            boolean var9x;
            if (var5x > var7x) {
               var9x = true;
            } else {
               var9x = false;
            }

            return var9x;
         });
         RotationRequest var19 = this.y.peek();
         LocalPlayer var20 = mc.player;
         Rotation var21 = new Rotation(var20);
         RotationUpdateEvent var22 = new RotationUpdateEvent(var21);
         Night.EVENT_BUS.post(var22);
         int var24;
         if (var22.isCancelled() && var22.getModule() != null) {
            Module var23 = var22.getModule();
            var24 = this.getModulePriority(var23);
         } else if (var22.isCancelled()) {
            var24 = 0;
         } else {
            var24 = -1;
         }

         byte var25 = (byte)var24;
         if (var19 != null) {
            int var26 = var19.getPriority();
            byte var27 = var25;
            if (var26 > var27) {
               float var28 = var19.getYaw();
               float var29 = var19.getPitch();
               this.currentRotation = new Rotation(var28, var29);
               Module var30 = var19.getModule();
               this.activeModule = var30;
               long var31 = System.currentTimeMillis();
               this.t = var31;
               return;
            }
         }

         if (var22.isCancelled()) {
            Rotation var33 = var21;
            this.currentRotation = var33;
            Module var34 = var22.getModule();
            this.activeModule = var34;
            long var35 = System.currentTimeMillis();
            this.t = var35;
         } else {
            this.currentRotation = null;
            this.activeModule = null;
         }
      }
   }

   @EventTarget(priority = Integer.MAX_VALUE)
   public void a_void_MotionSyncEvent(MotionSyncEvent var1) {
      if (this.currentRotation != null) {
         if (!PingBypassState.d || Night.pingBypassConfig == null || !Night.pingBypassConfig.isServerMode()) {
            EntityAccessor var2 = (EntityAccessor)mc.player;
            float var3 = var2.getRawYRot();
            this.e = var3;
            float var4 = var2.getRawXRot();
            this.f = var4;
            EntityAccessor var5 = var2;
            float var6 = this.currentRotation.getYaw();
            var5.setRawYRot(var6);
            EntityAccessor var7 = var2;
            float var8 = this.currentRotation.getPitch();
            var7.setRawXRot(var8);
            spoofingRotation = true;
         }
      }
   }

   @EventTarget(priority = Integer.MIN_VALUE)
   public void a_void_Post(MotionSyncEvent.Post var1) {
      if (this.currentRotation != null) {
         if (!PingBypassState.d || Night.pingBypassConfig == null || !Night.pingBypassConfig.isServerMode()) {
            spoofingRotation = false;
            EntityAccessor var2 = (EntityAccessor)mc.player;
            float var3 = this.e;
            var2.setRawYRot(var3);
            EntityAccessor var4 = var2;
            float var5 = this.f;
            var4.setRawXRot(var5);
         }
      }
   }

   public int getModulePriority(Module var1) {
      String var2 = var1.getName();
      Integer var3 = 0;
      return z.getOrDefault(var2, var3);
   }

   private int a_int_RotationRequest_RotationRequest(RotationRequest var1, RotationRequest var2) {
      int var3 = var1.getPriority();
      int var4 = var2.getPriority();
      if (var3 == var4) {
         long var5 = var1.getTimestamp();
         long var7 = var2.getTimestamp();
         return -Long.compare(var5, var7);
      } else {
         int var9 = var1.getPriority();
         int var10 = var2.getPriority();
         return -Integer.compare(var9, var10);
      }
   }

   public void rotate(float[] var1, int var2) {
      float var3 = var1[0];
      float var4 = var1[1];
      this.rotate(var3, var4, var2);
   }

   public void rotate(float var1, float var2, int var3) {
      this.y.removeIf(var1x -> {
         boolean var2x;
         if (var1x.getModule() == null && var1x.getPriority() == var3) {
            var2x = true;
         } else {
            var2x = false;
         }

         return var2x;
      });
      this.y.add(new RotationRequest(var1, var2, var3));
   }

   public void rotate(float[] var1, Module var2, int var3) {
      float var4 = var1[0];
      float var5 = var1[1];
      this.rotate(var4, var5, var2, var3);
   }

   public void rotate(float var1, float var2, Module var3, int var4) {
      this.y.removeIf(var1x -> {
         boolean var2x;
         if (var1x.getModule() == var3) {
            var2x = true;
         } else {
            var2x = false;
         }

         return var2x;
      });
      this.y.add(new RotationRequest(var1, var2, var3, var4));
   }

   @EventTarget
   public void a_void_UpdateVelocityEvent(UpdateVelocityEvent var1) {
      if (mc.player != null) {
         if (Night.moduleManager.getModule(RotationsModule.class).movementFix.getValue()) {
            if (this.currentRotation != null) {
               Vec3 var2 = var1.getMovementInput();
               float var3 = var1.getSpeed();
               float var4 = this.currentRotation.getYaw();
               Vec3 var5 = EntityAccessor.invokeMovementInputToVelocity(var2, var3, var4);
               var1.setVelocity(var5);
               var1.setCancelled(true);
            }
         }
      }
   }

   @EventTarget
   public void a_void_KeyboardTickEvent(KeyboardTickEvent var1) {
      if (mc.player != null && mc.level != null && !mc.player.isPassenger()) {
         if (Night.moduleManager.getModule(RotationsModule.class).movementFix.getValue()) {
            if (this.currentRotation != null) {
               float var2 = var1.getMovementForward();
               float var3 = var1.getMovementSideways();
               float var4 = mc.player.getYRot();
               float var5 = this.currentRotation.getYaw();
               float var6 = (var4 - var5) * (float) (Math.PI / 180.0);
               float var7 = Mth.cos(var6);
               float var8 = Mth.sin(var6);
               float var9 = var2;
               float var10 = var7;
               float var11 = var9 * var10;
               float var12 = var3;
               float var13 = var8;
               float var14 = var12 * var13;
               float var15 = Math.round(var11 + var14);
               var1.setMovementForward(var15);
               float var16 = var3;
               float var17 = var7;
               float var18 = var16 * var17;
               float var19 = var2;
               float var20 = var8;
               float var21 = var19 * var20;
               float var22 = Math.round(var18 - var21);
               var1.setMovementSideways(var22);
               var1.setCancelled(true);
            }
         }
      }
   }

   @EventTarget
   public void a_void_PlayerJumpEvent(PlayerJumpEvent var1) {
      if (mc.player != null && mc.level != null && !mc.player.isPassenger()) {
         if (Night.moduleManager.getModule(RotationsModule.class).movementFix.getValue()) {
            if (this.currentRotation != null) {
               float var2 = mc.player.getYRot();
               this.A = var2;
               LocalPlayer var3 = mc.player;
               float var4 = this.currentRotation.getYaw();
               var3.setYRot(var4);
            }
         }
      }
   }

   @EventTarget
   public void a_void_Post_6(PlayerJumpEvent.Post var1) {
      if (mc.player != null && mc.level != null && !mc.player.isPassenger()) {
         if (Night.moduleManager.getModule(RotationsModule.class).movementFix.getValue()) {
            if (this.currentRotation != null) {
               LocalPlayer var2 = mc.player;
               float var3 = this.A;
               var2.setYRot(var3);
            }
         }
      }
   }

   @EventTarget
   public void a_void_PacketSendEvent(PacketSendEvent var1) {
      if (mc.player != null) {
         if (!var1.isCancelled()) {
            if (var1.getPacket() instanceof ServerboundMovePlayerPacket var3) {
               this.a_void_ServerboundMovePlayerPacket(var3);
               if (!var3.hasRotation()) {
                  return;
               }

               ServerboundMovePlayerPacket var4 = var3;
               float var5 = mc.player.getYRot();
               float var6 = var4.getYRot(var5);
               this.serverYaw = var6;
               ServerboundMovePlayerPacket var7 = var3;
               float var8 = mc.player.getXRot();
               float var9 = var7.getXRot(var8);
               this.serverPitch = var9;
            }
         }
      }
   }

   public boolean canRotate(float var1, float var2) {
      return this.canRotate(var1, var2, null, null);
   }

   public boolean canRotate(float var1, float var2, BlockPos var3, Direction var4) {
      if (mc.player == null) {
         return false;
      }

      if (PacketPredictionUtil.c()) {
         this.g(var1, var2);
         return true;
      }

      if (var3 != null) {
         float var5;
         if (this.J) {
            var5 = this.L;
         } else {
            var5 = this.serverYaw;
         }

         float var6 = var5;
         float var7;
         if (this.J) {
            var7 = this.M;
         } else {
            var7 = this.serverPitch;
         }

         float var8 = var7;
         float var9 = var6;
         float var10 = var8;
         if (this.b(var9, var10, var3, var4)) {
            return true;
         }
      }

      boolean var11;
      if (PingBypassState.d && Night.pingBypassConfig != null && Night.pingBypassConfig.isServerMode()) {
         var11 = true;
      } else {
         var11 = false;
      }

      boolean var12 = var11;
      if (this.u && this.I && !this.K && !var12 && mc.isSameThread()) {
         if (this.J) {
            float var13 = this.L;
            float var14 = this.M;
            if (!this.e(var13, var14)) {
               return false;
            }

            this.k();
         }

         this.L = var1;
         this.M = var2;
         this.J = true;
         return true;
      } else {
         if (this.e(var1, var2)) {
            return true;
         }

         int var15 = this.H;
         int var16 = mc.player.tickCount;
         if (var15 != var16) {
            int var17 = mc.player.tickCount;
            this.H = var17;
            this.rotate(var1, var2, 6);
         }

         return false;
      }
   }

   private boolean e(float var1, float var2) {
      boolean var3;
      if (PingBypassState.d && Night.pingBypassConfig != null && Night.pingBypassConfig.isServerMode()) {
         var3 = true;
      } else {
         var3 = false;
      }

      boolean var4 = var3;
      double var5;
      if (var4) {
         var5 = Night.positionManager.getX();
      } else {
         var5 = mc.player.getX();
      }

      double var7 = var5;
      double var9;
      if (var4) {
         var9 = Night.positionManager.getY();
      } else {
         var9 = mc.player.getY();
      }

      double var11 = var9;
      double var13;
      if (var4) {
         var13 = Night.positionManager.getZ();
      } else {
         var13 = mc.player.getZ();
      }

      double var15 = var13;
      double var17 = var7;
      double var19 = var11;
      double var21 = var15;
      if (this.a(var17, var19, var21, var1, var2) && !this.l()) {
         return false;
      }

      this.g(var1, var2);
      return true;
   }

   private boolean b(float var1, float var2, BlockPos var3, Direction var4) {
      try {
         Vec3 var5 = mc.player.getEyePosition();
         Vec3 var6 = Vec3.directionFromRotation(var2, var1);
         double var7 = 6.0;
         Vec3 var9 = var6.scale(var7);
         Vec3 var10 = var5.add(var9);
         ClientLevel var11 = mc.level;
         Vec3 var12 = var5;
         Vec3 var13 = var10;
         LocalPlayer var14 = mc.player;
         BlockHitResult var15 = var11.clip(new ClipContext(var12, var13, Block.OUTLINE, Fluid.NONE, var14));
         boolean var16;
         if (var15.getType() != Type.BLOCK || !var15.getBlockPos().equals(var3) || var4 != null && var15.getDirection() != var4) {
            var16 = false;
         } else {
            var16 = true;
         }

         return var16;
      } catch (RuntimeException var18) {
         return false;
      }
   }

   @EventTarget(priority = Integer.MIN_VALUE)
   public void b(PacketSendEvent var1) {
      if (this.J && mc.isSameThread()) {
         if (!(var1.getPacket() instanceof ServerboundMovePlayerPacket)) {
            var1.setCancelled(true);
            ArrayDeque var2 = this.N;
            Packet var3 = var1.getPacket();
            var2.addLast(var3);
         }
      }
   }

   private void k() {
      if (!this.N.isEmpty() && mc.getConnection() != null) {
         flushingQueue = true;

         try {
            while (!this.N.isEmpty()) {
               ClientPacketListener var1 = mc.getConnection();
               Packet var2 = this.N.pollFirst();
               var1.send(var2);
            }
         } catch (Throwable var4) {
            flushingQueue = false;
            throw var4;
         }

         flushingQueue = false;
      } else {
         this.N.clear();
      }
   }

   @EventTarget(priority = Integer.MAX_VALUE)
   public void b(UpdateWalkingPlayerEvent var1) {
      if (this.K) {
         this.K = false;
         this.k();
      }

      this.I = true;
   }

   @EventTarget(priority = Integer.MAX_VALUE)
   public void b(MotionSyncEvent var1) {
      this.I = false;
   }

   @EventTarget(priority = Integer.MIN_VALUE)
   public void b(MotionSyncEvent.Post var1) {
      if (this.K) {
         this.K = false;
         this.k();
      }
   }

   private boolean a(double var1, double var3, double var5, float var7, float var8) {
      if (Double.isNaN(this.C)) {
         return true;
      }

      double var9 = this.C;
      double var11 = var1;
      double var13 = Mth.square(var9 - var11);
      double var15 = this.D;
      double var17 = var3;
      double var19 = Mth.square(var15 - var17);
      double var21 = var13 + var19;
      double var23 = this.E;
      double var25 = var5;
      double var27 = Mth.square(var23 - var25);
      double var29 = var21 + var27;
      float var31 = Math.abs(Mth.wrapDegrees(this.F - var7));
      float var32 = Math.abs(this.G - var8);
      float var33 = var31 + var32;
      double var34 = var29;
      double var36 = 0.00390625;
      boolean var38;
      if (!(var34 > var36) && !(var33 > 10.0F)) {
         var38 = false;
      } else {
         var38 = true;
      }

      return var38;
   }

   private void a_void_ServerboundMovePlayerPacket(ServerboundMovePlayerPacket var1) {
      double var2;
      if (Double.isNaN(this.C)) {
         var2 = mc.player.getX();
      } else {
         var2 = this.C;
      }

      double var4 = var1.getX(var2);
      double var6;
      if (Double.isNaN(this.C)) {
         var6 = mc.player.getY();
      } else {
         var6 = this.D;
      }

      double var8 = var1.getY(var6);
      double var10;
      if (Double.isNaN(this.C)) {
         var10 = mc.player.getZ();
      } else {
         var10 = this.E;
      }

      double var12 = var1.getZ(var10);
      float var14;
      if (Double.isNaN(this.C)) {
         var14 = mc.player.getYRot();
      } else {
         var14 = this.F;
      }

      float var15 = var1.getYRot(var14);
      float var16;
      if (Double.isNaN(this.C)) {
         var16 = mc.player.getXRot();
      } else {
         var16 = this.G;
      }

      float var17 = var1.getXRot(var16);
      double var18 = var4;
      double var20 = var8;
      double var22 = var12;
      float var24 = var15;
      float var25 = var17;
      if (this.a(var18, var20, var22, var24, var25)) {
         double var26 = var4;
         this.C = var26;
         double var28 = var8;
         this.D = var28;
         double var30 = var12;
         this.E = var30;
         float var32 = var15;
         this.F = var32;
         float var33 = var17;
         this.G = var33;
         synchronized (this.B) {
            ArrayDeque var35 = this.B;
            Long var36 = System.currentTimeMillis();
            var35.addLast(var36);
         }
      }
   }

   private boolean l() {
      long var1 = System.currentTimeMillis();
      synchronized (this.B) {
         while (!this.B.isEmpty()) {
            long var4 = var1;
            long var6 = this.B.peekFirst();
            long var8 = var4 - var6;
            long var10 = 6000L;
            if (var8 <= var10) {
               break;
            }

            this.B.pollFirst();
         }

         if (this.B.size() + 1 > 124) {
            return false;
         }

         int var12 = 0;

         for (Iterator var13 = this.B.descendingIterator(); var13.hasNext(); var12++) {
            long var14 = var1;
            long var16 = (Long)var13.next();
            long var18 = var14 - var16;
            long var20 = 500L;
            if (var18 > var20) {
               break;
            }
         }

         boolean var22;
         if (var12 + 1 <= 13) {
            var22 = true;
         } else {
            var22 = false;
         }

         return var22;
      }
   }

   public void sendRotationPacket(float[] var1) {
      float var2 = var1[0];
      float var3 = var1[1];
      this.sendRotationPacket(var2, var3);
   }

   private float[] f(float var1, float var2) {
      if (!(var2 <= -89.5F) && !(var2 >= 89.5F)) {
         String var6 = Night.moduleManager.getModule(RotationsModule.class).jitter.getValue();
         if ("Grim".equalsIgnoreCase(var6)) {
            double var7 = Math.random();
            double var9 = 2.0;
            double var11 = var7 * var9;
            double var13 = 1.0;
            double var15 = var11 - var13;
            double var17 = 0.001F;
            float var19 = (float)(var15 * var17);
            float var20 = var2;
            float var21 = var19;
            var2 = Mth.clamp(var20 + var21, -90.0F, 90.0F);
         } else {
            String var22 = var6;
            if ("Normal".equalsIgnoreCase(var22)) {
               double var23 = Math.random();
               double var25 = 0.35F - 0.15F;
               float var27 = (float)(var23 * var25);
               float var28 = 0.15F + var27;
               double var29 = Math.random();
               double var31 = 0.35F - 0.15F;
               float var33 = (float)(var29 * var31);
               float var34 = 0.15F + var33;
               float var35 = var28;
               double var36 = Math.random();
               double var38 = 0.5;
               float var40;
               if (var36 < var38) {
                  var40 = -1.0F;
               } else {
                  var40 = 1.0F;
               }

               float var41 = var35 * var40;
               float var42 = var34;
               double var43 = Math.random();
               double var45 = 0.5;
               float var47;
               if (var43 < var45) {
                  var47 = -1.0F;
               } else {
                  var47 = 1.0F;
               }

               float var48 = var42 * var47;
               float var49 = var1;
               float var50 = var41;
               var1 = var49 + var50;
               float var51 = var2;
               float var52 = var48;
               var2 = Mth.clamp(var51 + var52, -90.0F, 90.0F);
            }
         }

         float[] var53 = new float[2];
         float var54 = var1;
         var53[0] = var54;
         float var55 = var2;
         var53[1] = var55;
         return var53;
      } else {
         float[] var3 = new float[2];
         float var4 = var1;
         var3[0] = var4;
         float var5 = var2;
         var3[1] = var5;
         return var3;
      }
   }

   public boolean isRecentlyRotated() {
      long var1 = System.currentTimeMillis();
      long var3 = this.O;
      long var5 = var1 - var3;
      long var7 = 500L;
      boolean var9;
      if (var5 < var7) {
         var9 = true;
      } else {
         var9 = false;
      }

      return var9;
   }

   public void sendRotationPacket(float var1, float var2) {
      this.sendRotationPacket(var1, var2, false);
   }

   public void sendRotationPacket(float var1, float var2, boolean var3) {
      if (var3 || this.i != var1 || this.j != var2) {
         this.i = var1;
         this.j = var2;
         float[] var4 = this.f(var1, var2);
         float var5 = var4[0];
         float var6 = var4[1];
         long var7 = System.currentTimeMillis();
         this.O = var7;
         float var9 = var5;
         this.serverYaw = var9;
         float var10 = var6;
         this.serverPitch = var10;
         if (PingBypassState.d && Night.pingBypassConfig != null && Night.pingBypassConfig.isServerMode()) {
            double var28 = Night.positionManager.getX();
         } else {
            double var11 = mc.player.getX();
         }

         if (PingBypassState.d && Night.pingBypassConfig != null && Night.pingBypassConfig.isServerMode()) {
            double var29 = Night.positionManager.getY();
         } else {
            double var15 = mc.player.getY();
         }

         if (PingBypassState.d && Night.pingBypassConfig != null && Night.pingBypassConfig.isServerMode()) {
            double var30 = Night.positionManager.getZ();
         } else {
            double var19 = mc.player.getZ();
         }

         boolean var23;
         if (PingBypassState.d && Night.pingBypassConfig != null && Night.pingBypassConfig.isServerMode()) {
            var23 = Night.positionManager.isOnGround();
         } else {
            var23 = mc.player.onGround();
         }

         boolean var24 = var23;
         float var25 = var5;
         float var26 = var6;
         boolean var27 = var24;
         RubberbandTracker.a(() -> {
            ClientPacketListener var3x = mc.getConnection();
            boolean var4x = mc.player.horizontalCollision;
            var3x.send(new Rot(var25, var26, var27, var4x));
         });
      }
   }

   public void sendPosRotPacket(float[] var1) {
      float var2 = var1[0];
      float var3 = var1[1];
      this.sendOrBatchPosRot(var2, var3);
   }

   public void beginBatch() {
      this.u = true;
      this.v = false;
   }

   public void setBatchRotation(float var1, float var2) {
      this.w = var1;
      this.x = var2;
      this.v = true;
      float[] var3 = this.f(var1, var2);
      float var4 = var3[0];
      this.serverYaw = var4;
      float var5 = var3[1];
      this.serverPitch = var5;
      this.P = true;
   }

   public void endBatch() {
      this.u = false;
      if (this.J) {
         this.J = false;
         this.K = true;
         this.v = false;
         float var1 = this.L;
         float var2 = this.M;
         this.rotate(var1, var2, 6);
      } else if (this.v) {
         this.v = false;
         float var3 = this.w;
         float var4 = this.x;
         this.sendOrBatchPosRot(var3, var4);
      }
   }

   public void sendOrBatchPosRot(float var1, float var2) {
      if (this.u) {
         this.setBatchRotation(var1, var2);
      } else {
         this.g(var1, var2);
      }
   }

   private void g(float var1, float var2) {
      boolean var3;
      if (PingBypassState.d && Night.pingBypassConfig != null && Night.pingBypassConfig.isServerMode()) {
         var3 = true;
      } else {
         var3 = false;
      }

      boolean var4 = var3;
      double var5;
      if (var4) {
         var5 = Night.positionManager.getX();
      } else {
         var5 = mc.player.getX();
      }

      double var7 = var5;
      double var9;
      if (var4) {
         var9 = Night.positionManager.getY();
      } else {
         var9 = mc.player.getY();
      }

      double var11 = var9;
      double var13;
      if (var4) {
         var13 = Night.positionManager.getZ();
      } else {
         var13 = mc.player.getZ();
      }

      double var15 = var13;
      boolean var17;
      if (var4) {
         var17 = Night.positionManager.isOnGround();
      } else {
         var17 = mc.player.onGround();
      }

      boolean var18 = var17;
      if (this.k == var1 && this.l == var2) {
         double var19 = this.m;
         double var21 = var7;
         if (var19 == var21) {
            double var23 = this.n;
            double var25 = var11;
            if (var23 == var25) {
               double var27 = this.o;
               double var29 = var15;
               if (var27 == var29) {
                  float var31 = this.serverYaw;
                  float var32 = this.p;
                  if (var31 == var32) {
                     float var33 = this.serverPitch;
                     float var34 = this.q;
                     if (var33 == var34) {
                        return;
                     }
                  }
               }
            }
         }
      }

      this.k = var1;
      this.l = var2;
      double var35 = var7;
      this.m = var35;
      double var37 = var11;
      this.n = var37;
      double var39 = var15;
      this.o = var39;
      float[] var41 = this.f(var1, var2);
      float var42 = var41[0];
      float var43 = var41[1];
      float var44 = var42;
      this.p = var44;
      float var45 = var43;
      this.q = var45;
      long var46 = System.currentTimeMillis();
      this.O = var46;
      this.P = true;
      float var48 = var42;
      this.serverYaw = var48;
      float var49 = var43;
      this.serverPitch = var49;
      double var50 = var7;
      double var52 = var11;
      double var54 = var15;
      float var56 = var42;
      float var57 = var43;
      boolean var58 = var18;
      RubberbandTracker.a(() -> {
         ClientPacketListener var9x = mc.getConnection();
         double var10 = var50;
         double var12 = var52;
         double var14 = var54;
         boolean var16 = mc.player.horizontalCollision;
         var9x.send(new PosRot(var10, var12, var14, var56, var57, var58, var16));
      });
   }

   public void rotateByMode(String var1, float[] var2) {
      if ("Silent".equalsIgnoreCase(var1)) {
         this.sendPosRotPacket(var2);
      } else {
         this.sendRotationPacket(var2);
      }
   }

   public boolean hasRecentRotation() {
      long var1 = System.currentTimeMillis();
      long var3 = this.t;
      long var5 = var1 - var3;
      long var7 = 1000L;
      boolean var9;
      if (var5 < var7) {
         var9 = true;
      } else {
         var9 = false;
      }

      return var9;
   }

   public float[] getInterpolatedRotation() {
      float var1 = MathUtil.a_float_float(this.r);
      float var2;
      if (this.currentRotation == null) {
         var2 = mc.player.getYRot();
      } else {
         var2 = this.getServerYaw();
      }

      float var3 = MathUtil.a_float_float(var2);
      float var4 = var1;
      float var5 = var3 - var4;
      if (var5 > 180.0F) {
         var5 -= 380.0F;
      } else if (var5 < -180.0F) {
         var5 += 360.0F;
      }

      float var6 = EasingUtil.a(this.t, 1000);
      float var7 = var1;
      float var8 = var1;
      float var9 = var5;
      float var10 = var8 + var9;
      float var11 = Mth.lerp(var6, var7, var10);
      float var12 = EasingUtil.a(this.t, 1000);
      float var13 = this.s;
      float var14;
      if (this.currentRotation == null) {
         var14 = mc.player.getXRot();
      } else {
         var14 = this.getServerPitch();
      }

      float var15 = Mth.lerp(var12, var13, var14);
      float var16 = var11;
      this.r = var16;
      float var17 = var15;
      this.s = var17;
      float[] var18 = new float[2];
      float var19 = var11;
      var18[0] = var19;
      float var20 = var15;
      var18[1] = var20;
      return var18;
   }

   @Generated
   public Rotation getCurrentRotation() {
      return this.currentRotation;
   }

   @Generated
   public Module getActiveModule() {
      return this.activeModule;
   }

   @Generated
   public float getServerYaw() {
      return this.serverYaw;
   }

   @Generated
   public float getServerPitch() {
      return this.serverPitch;
   }

   @Generated
   public boolean isBatching() {
      return this.u;
   }

   static {
      z.put("KillAura", 1);
      z.put("AutoCrystal", 2);
      z.put("SpeedMine", 3);
      z.put("Sprint", 4);
      z.put("SelfFill", 5);
   }
}
