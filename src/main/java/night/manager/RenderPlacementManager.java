package night.manager;

import com.mojang.blaze3d.vertex.PoseStack;
import java.awt.Color;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.Generated;
import net.minecraft.core.BlockPos;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.events.EventTarget;
import night.events.impl.Render2DEvent;
import night.events.impl.Render3DEvent;
import night.module.impl.combat.AutoCrystal;
import night.module.impl.core.RendersModule;
import night.pingbypass.PingBypassState;
import night.pingbypass.protocol.PingBypassPayload;
import night.pingbypass.protocol.s2c.S2CRenderPlacePosPacket;
import night.util.MinecraftInstance;
import night.util.animation.EasingUtil;
import night.util.math.MathUtil;
import night.util.math.RollingCounter;
import night.util.render.EspShaderRenderer;
import night.util.render.PlaceRenderEntry;
import night.util.render.Render2DUtil;
import night.util.render.Render3DUtil;
import night.util.world.BlockInteractionUtil;
import night.util.world.PacketAndRenderUtil;

public class RenderPlacementManager implements MinecraftInstance {
   public static final List<Runnable> RENDER_QUEUE = new CopyOnWriteArrayList<>();
   private final RollingCounter crystalCounter = new RollingCounter();
   private int crystalsPerSecond;
   public CopyOnWriteArrayList<PlaceRenderEntry> placedBlocks = new CopyOnWriteArrayList<>();
   private RenderPlacementManager.AnimatedTarget crystalRenderTarget;
   private BlockPos lastTargetPos = null;
   private Vec3 slidePos = null;
   private long slideStartTime = 0L;

   public RenderPlacementManager() {
      Night.EVENT_BUS.register(this);
   }

   @EventTarget
   public void onRender2D(Render2DEvent var1) {
      this.crystalsPerSecond = this.crystalCounter.getCount();
      this.crystalCounter.increment();
   }

   @EventTarget
   public void onRender3DPlaceBlocks(Render3DEvent var1) {
      if (mc.player != null && mc.level != null && !this.placedBlocks.isEmpty()) {
         RendersModule var2 = Night.moduleManager.getModule(RendersModule.class);

         for (PlaceRenderEntry var4 : (Iterable<PlaceRenderEntry>)(Object)this.placedBlocks) {
            float var5 = var4.a();
            AABB var6 = new AABB(var4.b());
            if (var2.mode.getValue().equals("Shrink")) {
               var6 = new AABB(var4.b()).deflate(0.5).inflate(Mth.clamp(var5 / 2.0, 0.0, 0.5));
            }

            if (var2.renderMode.getValue().equalsIgnoreCase("Fill") || var2.renderMode.getValue().equalsIgnoreCase("Both")) {
               Render3DUtil.a_void_PoseStack_AABB_Color(var1.getMatrices(), var6, var2.a(var2.mode.getValue(), var2.fillColor.getColor(), var5));
            }

            if (var2.renderMode.getValue().equalsIgnoreCase("Outline") || var2.renderMode.getValue().equalsIgnoreCase("Both")) {
               Render3DUtil.b(var1.getMatrices(), var6, var2.a(var2.mode.getValue(), var2.outlineColor.getColor(), var5));
            }
         }

         this.placedBlocks.removeIf(var0 -> var0.a() <= 0.0F);
      }
   }

   @EventTarget
   public void onRender3DCrystalTarget(Render3DEvent var1) {
      if (mc.player != null && mc.level != null) {
         if (this.crystalRenderTarget != null && this.crystalRenderTarget.a() != null) {
            AutoCrystal var2 = Night.moduleManager.getModule(AutoCrystal.class);
            float var3;
            if (this.crystalRenderTarget.b() == 1) {
               var3 = EasingUtil.a(EasingUtil.a(this.crystalRenderTarget.c(), var2.duration.getValue().intValue()), EasingUtil.Easing.EASE_OUT_CUBIC);
            } else {
               var3 = 1.0F - EasingUtil.a(EasingUtil.a(this.crystalRenderTarget.c(), var2.duration.getValue().intValue()), EasingUtil.Easing.EASE_IN_CUBIC);
            }

            AABB var4 = new AABB(this.crystalRenderTarget.a());
            if (var2.mode.getValue().equals("Shrink")) {
               var4 = new AABB(this.crystalRenderTarget.a()).deflate(0.5).inflate(Mth.clamp(var3 / 2.0, 0.0, 0.5));
            }

            if (var2.animation2.getValue().equals("Slide")) {
               if (this.slidePos == null) {
                  this.slidePos = MathUtil.a_Vec3_BlockPos(this.crystalRenderTarget.a());
               }

               if (!BlockInteractionUtil.a_bool_BlockPos_BlockPos(this.crystalRenderTarget.a(), this.lastTargetPos)) {
                  this.slideStartTime = System.currentTimeMillis();
                  this.lastTargetPos = this.crystalRenderTarget.a();
               }

               float var5 = EasingUtil.a(
                  EasingUtil.a(this.slideStartTime, (int)(Math.pow(var2.smoothness.getValue().doubleValue(), 1.4) * 1000.0)), EasingUtil.Easing.EASE_OUT_QUART
               );
               this.slidePos = this.slidePos
                  .add(MathUtil.a_Vec3_Vec3_float(MathUtil.a_Vec3_BlockPos(this.crystalRenderTarget.a()).subtract(this.slidePos), var5));
               var4 = MathUtil.a_AABB_Vec3(this.slidePos);
               if (var2.mode.getValue().equals("Shrink")) {
                  var4 = MathUtil.a_AABB_Vec3(this.slidePos).deflate(0.5).inflate(Mth.clamp(var3 / 2.0, 0.0, 0.5));
               }
            }

            RendersModule var15 = Night.moduleManager.getModule(RendersModule.class);
            boolean var6 = var2.renderMode.getValue().equalsIgnoreCase("Fill") || var2.renderMode.getValue().equalsIgnoreCase("Both");
            boolean var7 = var2.renderMode.getValue().equalsIgnoreCase("Outline") || var2.renderMode.getValue().equalsIgnoreCase("Both");
            int var8 = EspShaderRenderer.a_int_String(var2.shader.getValue());
            List var9 = var8 == 0 ? Render3DUtil.b : Render3DUtil.f;
            List var10 = var8 == 0 ? Render3DUtil.c : Render3DUtil.g;
            if (var8 != 0) {
               float var11 = var2.shaderSpeed.getValue().floatValue();
               float var12 = var2.shaderStep.getValue().floatValue();
               float var13 = (float)(System.currentTimeMillis() % 2000000L) / 1000.0F * var11 * (var8 == 1 ? var12 : 1.0F);
               float var14 = var2.shaderDistanceScaling.getValue() ? (float)Math.sqrt(mc.player.distanceToSqr(var4.getCenter())) : 1.0F;
               EspShaderRenderer.a_void_ShaderUniformConfig(
                  new EspShaderRenderer.ShaderUniformConfig(
                     var8,
                     var13,
                     var12,
                     var2.shaderOpacity.getValue().floatValue() / 100.0F,
                     var14,
                     var8 == 7 ? var2.shaderGlowColor.getColor() : var2.shaderColor1.getColor(),
                     var2.shaderColor2.getColor(),
                     var2.shaderColor3.getColor(),
                     var2.shaderColor4.getColor()
                  )
               );
            }

            if (var6) {
               Render3DUtil.a(
                  var9,
                  var1.getMatrices(),
                  var4,
                  var15.a(var2.mode.getValue(), var2.fillColorUp.getColor(), var3),
                  var15.a(var2.mode.getValue(), var2.fillColorDown.getColor(), var3)
               );
            }

            if (var7) {
               Render3DUtil.b(
                  var10,
                  var1.getMatrices(),
                  var4,
                  var15.a(var2.mode.getValue(), var2.outlineColorUp.getColor(), var3),
                  var15.a(var2.mode.getValue(), var2.outlineColorDown.getColor(), var3)
               );
            }
         }
      }
   }

   @EventTarget
   public void onRender3DPost(Render3DEvent.Post var1) {
      if (mc.player != null && mc.level != null) {
         if (this.crystalRenderTarget != null && this.crystalRenderTarget.a() != null) {
            if (this.crystalRenderTarget.b() == 1) {
               PoseStack var2 = var1.getMatrices();
               AutoCrystal var3 = Night.moduleManager.getModule(AutoCrystal.class);
               Vec3 var4 = Vec3.atCenterOf(this.crystalRenderTarget.a());
               Vec3 var5 = new Vec3(
                  var4.x - mc.gameRenderer.mainCamera().position().x,
                  var4.y - mc.gameRenderer.mainCamera().position().y,
                  var4.z - mc.gameRenderer.mainCamera().position().z
               );
               if (var3.animation2.getValue().equals("Slide")) {
                  var5 = new Vec3(
                     this.slidePos.x + 0.5 - mc.gameRenderer.mainCamera().position().x,
                     this.slidePos.y + 0.5 - mc.gameRenderer.mainCamera().position().y,
                     this.slidePos.z + 0.5 - mc.gameRenderer.mainCamera().position().z
                  );
               }

               if (var3.icon.getValue()) {
                  float var6 = var3.iconScale.getValue().floatValue() / 100.0F;
                  var2.pushPose();
                  var2.translate(var5.x, var5.y, var5.z);
                  PacketAndRenderUtil.a(var2, mc.gameRenderer.mainCamera().rotation());
                  var2.scale(var6, -var6, var6);
                  Render2DUtil.a(var2, 0.0F, 0.0F, 12.0F, new Color(0, 0, 0, 100));
                  Render2DUtil.a(var2, 0.0F, 0.0F, 12.0F - var3.iconRadius.getValue().floatValue(), var3.iconColor.getColor());
                  if (var3.renderDamage.getValue()) {
                     Render2DUtil.a(var2, -5.5F, -8.5F, 5.5F, 2.5F, Identifier.fromNamespaceAndPath("night", "textures/crystal.png"), Color.WHITE);
                     var2.pushPose();
                     var2.scale(0.45F, 0.45F, 0.45F);
                     String var7 = var3.x();
                     Night.fontManager.drawWorldText(var2, var7, -Night.fontManager.getStringWidth(var7) / 2 - 1, 7, Color.WHITE);
                     var2.popPose();
                  } else {
                     Render2DUtil.a(var2, -6.5F, -6.5F, 6.5F, 6.5F, Identifier.fromNamespaceAndPath("night", "textures/crystal.png"), Color.WHITE);
                  }

                  var2.popPose();
               } else if (var3.renderDamage.getValue()) {
                  var2.pushPose();
                  var2.translate(var5.x, var5.y, var5.z);
                  PacketAndRenderUtil.a(var2, mc.gameRenderer.mainCamera().rotation());
                  var2.scale(0.025F, -0.025F, 0.025F);
                  String var8 = var3.x();
                  Night.fontManager.drawWorldText(var2, var8, -Night.fontManager.getStringWidth(var8) / 2, -Night.fontManager.getFontHeight() / 2, Color.WHITE);
                  var2.popPose();
               }
            }
         }
      }
   }

   public void setCrystalTarget(BlockPos var1) {
      if (!Night.moduleManager.getModule(AutoCrystal.class).isEnabled()) {
         var1 = null;
      }

      if (var1 == null) {
         if (this.crystalRenderTarget != null) {
            if (this.crystalRenderTarget.b() != 0) {
               this.crystalRenderTarget.a(0);
               this.crystalRenderTarget.a(System.currentTimeMillis());
            }
         } else {
            this.crystalRenderTarget = new RenderPlacementManager.AnimatedTarget(null, 0, System.currentTimeMillis());
         }
      } else if (this.crystalRenderTarget != null && this.crystalRenderTarget.b() != 0) {
         this.crystalRenderTarget.a(var1);
      } else {
         this.crystalRenderTarget = new RenderPlacementManager.AnimatedTarget(var1, 1, System.currentTimeMillis());
      }

      if (PingBypassState.d && Night.pingBypassConfig != null && Night.pingBypassConfig.isServerMode() && Night.pingBypassServer != null) {
         this.b(var1);
      }
   }

   private void b(BlockPos var1) {
      ClientboundCustomPayloadPacket var2 = new ClientboundCustomPayloadPacket(PingBypassPayload.fromPacket(new S2CRenderPlacePosPacket(var1)));

      for (Connection var4 : (Iterable<Connection>)(Object)Night.pingBypassServer.getConnections()) {
         if (var4.isConnected()) {
            var4.send(var2);
         }
      }
   }

   @Generated
   public int getCrystalsPerSecond() {
      return this.crystalsPerSecond;
   }

   @Generated
   public CopyOnWriteArrayList<PlaceRenderEntry> getPlacedBlocks() {
      return this.placedBlocks;
   }

   public static class AnimatedTarget {
      private BlockPos a;
      private int b;
      private long c;

      @Generated
      public AnimatedTarget(BlockPos var1, int var2, long var3) {
         this.a = var1;
         this.b = var2;
         this.c = var3;
      }

      @Generated
      public BlockPos a() {
         return this.a;
      }

      @Generated
      public int b() {
         return this.b;
      }

      @Generated
      public long c() {
         return this.c;
      }

      @Generated
      public void a(BlockPos var1) {
         this.a = var1;
      }

      @Generated
      public void a(int var1) {
         this.b = var1;
      }

      @Generated
      public void a(long var1) {
         this.c = var1;
      }
   }
}
