package night.util.world;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.ints.IntList;
import java.io.File;
import java.net.URI;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.network.protocol.game.GameProtocols.Context;
import net.minecraft.util.Util;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionfc;
import org.lwjgl.glfw.GLFW;

public final class PacketAndRenderUtil {
   public static Packet<?> a_Packet_InteractionHand(InteractionHand var0) {
      return new ServerboundSwingPacket(var0);
   }

   public static void a(LivingEntity var0, InteractionHand var1) {
      var0.swing(var1);
   }

   public static boolean a_bool_LivingEntity(LivingEntity var0) {
      return var0.swinging;
   }

   public static PacketAndRenderUtil.SwingSnapshot b_SwingSnapshot_LivingEntity(LivingEntity var0) {
      return new PacketAndRenderUtil.SwingSnapshot(var0);
   }

   public static void a(LivingEntity var0, LivingEntity var1) {
      b_SwingSnapshot_LivingEntity(var0).a(var1);
   }

   public static boolean a_bool_int(int var0) {
      return InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), var0);
   }

   public static void a_void_Object(Object var0) {
   }

   public static void b_void_Object(Object var0) {
   }

   public static boolean b_bool_int(int var0) {
      return GLFW.glfwGetMouseButton(Minecraft.getInstance().getWindow().handle(), var0) == 1;
   }

   public static boolean a_bool_BlockState(BlockState var0) {
      return var0.getBlock() != Blocks.COBWEB && var0.getBlock() != Blocks.BAMBOO_SAPLING && var0.isSolid();
   }

   public static void a(PoseStack var0, Quaternionfc var1) {
      var0.mulPose(var1);
   }

   public static void a_void_URI(URI var0) {
      Util.getPlatform().openUri(var0);
   }

   public static void a_void_File(File var0) {
      Util.getPlatform().openFile(var0);
   }

   public static Vec3 a_Vec3_Entity(Entity var0) {
      return var0.getInterpolation() != null ? var0.getInterpolation().position() : var0.position();
   }

   public static GpuTextureView a() {
      return RenderSystem.outputColorTextureOverride;
   }

   public static GpuTextureView b() {
      return RenderSystem.outputDepthTextureOverride;
   }

   public static void a(GpuTextureView var0, GpuTextureView var1) {
      RenderSystem.outputColorTextureOverride = var0;
      RenderSystem.outputDepthTextureOverride = var1;
   }

   public static GpuTextureView a_GpuTextureView_RenderTarget(RenderTarget var0) {
      return a() != null ? a() : var0.getColorTextureView();
   }

   public static GpuTextureView b_GpuTextureView_RenderTarget(RenderTarget var0) {
      if (!var0.useDepth) {
         return null;
      } else {
         return b() != null ? b() : var0.getDepthTextureView();
      }
   }

   public static void a(RenderPass var0, RenderPipeline var1) {
      var0.setPipeline(var1);
   }

   public static TextureTarget a(String var0, int var1, int var2) {
      return new TextureTarget(var0, var1, var2, true, GpuFormat.RGBA8_UNORM);
   }

   public static String a_String_Packet(Packet<?> var0) {
      return var0 instanceof ServerboundMovePlayerPacket var1 && a(var1.getX(0.0), var1.getY(0.0), var1.getZ(0.0), var1.getYRot(0.0F), var1.getXRot(0.0F))
         ? var0.getClass().getSimpleName()
            + " pos="
            + var1.getX(0.0)
            + ","
            + var1.getY(0.0)
            + ","
            + var1.getZ(0.0)
            + " rot="
            + var1.getYRot(0.0F)
            + ","
            + var1.getXRot(0.0F)
         : null;
   }

   private static boolean a(double var0, double var2, double var4, float var6, float var7) {
      return Double.isNaN(var0) || Double.isNaN(var2) || Double.isNaN(var4) || !Float.isFinite(var6) || !Float.isFinite(var7);
   }

   public static InteractionHand a_InteractionHand_ServerboundUseItemPacket(ServerboundUseItemPacket var0) {
      return var0.getHand();
   }

   public static InteractionHand a_InteractionHand_ServerboundUseItemOnPacket(ServerboundUseItemOnPacket var0) {
      return var0.getHand();
   }

   public static int a_int_ServerboundAcceptTeleportationPacket(ServerboundAcceptTeleportationPacket var0) {
      return var0.getId();
   }

   public static IntList a_IntList_ClientboundRemoveEntitiesPacket(ClientboundRemoveEntitiesPacket var0) {
      return var0.getEntityIds();
   }

   public static Context c() {
      return new Context() {
         public boolean hasInfiniteMaterials() {
            return false;
         }
      };
   }

   public static final class SwingSnapshot {
      private final boolean a;
      private final int b;
      private final InteractionHand c;
      private final float d;
      private final float e;

      SwingSnapshot(LivingEntity var1) {
         this.a = var1.swinging;
         this.b = var1.swingTime;
         this.c = var1.swingingArm;
         this.d = var1.attackAnim;
         this.e = var1.oAttackAnim;
      }

      public void a(LivingEntity var1) {
         var1.swinging = this.a;
         var1.swingTime = this.b;
         var1.swingingArm = this.c;
         var1.attackAnim = this.d;
         var1.oAttackAnim = this.e;
      }
   }
}
