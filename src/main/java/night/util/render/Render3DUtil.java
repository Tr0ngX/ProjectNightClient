package night.util.render;

import com.mojang.blaze3d.IndexType;
import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Builder;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.BlendFactor;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.RenderSystem.AutoStorageIndexBuffer;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import it.unimi.dsi.fastutil.ints.IntList;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.client.renderer.fog.FogRenderer.FogMode;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState.FoilType;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.rendertype.RenderSetup.OutlineProperty;
import net.minecraft.client.renderer.rendertype.RenderSetup.RenderSetupBuilder;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.BakedQuad.MaterialInfo;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.mixins.accessors.GameRendererAccessor;
import night.mixins.accessors.ItemStackRenderStateAccessor;
import night.mixins.accessors.LayerRenderStateAccessor;
import night.mixins.accessors.RenderPipelinesAccessor;
import night.util.MinecraftInstance;
import night.util.color.ColorUtil;
import night.util.world.EntityUtil;
import night.util.world.PacketAndRenderUtil;
import org.joml.Matrix4f;

public class Render3DUtil implements MinecraftInstance {
   public static boolean a = false;
   private static final Set<String> i = ConcurrentHashMap.newKeySet();
   private static final RenderType j = net.minecraft.client.renderer.rendertype.RenderTypeHelper.create(
      "night_no_depth_quads",
      RenderSetup.builder(
            RenderPipeline.builder(new Snippet[]{RenderPipelinesAccessor.getDebugFilledSnippet()})
               .withLocation("night/no_depth_quads")
               .withCull(false)
               .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
               .build()
         )
         .sortOnUpload()
         .createRenderSetup()
   );
   private static final RenderType k = net.minecraft.client.renderer.rendertype.RenderTypeHelper.create(
      "night_no_depth_lines",
      RenderSetup.builder(
            RenderPipeline.builder(new Snippet[]{RenderPipelinesAccessor.getLinesSnippet()})
               .withLocation("night/no_depth_lines")
               .withCull(false)
               .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
               .withColorTargetState(new ColorTargetState(new BlendFunction(BlendFactor.SRC_ALPHA, BlendFactor.ONE_MINUS_SRC_ALPHA)))
               .build()
         )
         .createRenderSetup()
   );
   private static final BlendFunction l = new BlendFunction(BlendFactor.SRC_ALPHA, BlendFactor.ONE_MINUS_CONSTANT_ALPHA);
   private static final RenderType m = net.minecraft.client.renderer.rendertype.RenderTypeHelper.create(
      "night_no_depth_quads_shine",
      RenderSetup.builder(
            RenderPipeline.builder(new Snippet[]{RenderPipelinesAccessor.getDebugFilledSnippet()})
               .withLocation("night/no_depth_quads_shine")
               .withCull(false)
               .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
               .withColorTargetState(new ColorTargetState(l))
               .build()
         )
         .sortOnUpload()
         .createRenderSetup()
   );
   private static final RenderType n = net.minecraft.client.renderer.rendertype.RenderTypeHelper.create(
      "night_no_depth_lines_shine",
      RenderSetup.builder(
            RenderPipeline.builder(new Snippet[]{RenderPipelinesAccessor.getLinesSnippet()})
               .withLocation("night/no_depth_lines_shine")
               .withCull(false)
               .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
               .withColorTargetState(new ColorTargetState(l))
               .build()
         )
         .createRenderSetup()
   );
   public static List<Render3DUtil.BufferedPrimitive> b = new ArrayList<>();
   public static List<Render3DUtil.BufferedPrimitive> c = new ArrayList<>();
   public static List<Render3DUtil.BufferedPrimitive> d = new ArrayList<>();
   public static List<Render3DUtil.BufferedPrimitive> e = new ArrayList<>();
   public static List<Render3DUtil.BufferedPrimitive> f = new ArrayList<>();
   public static List<Render3DUtil.BufferedPrimitive> g = new ArrayList<>();
   public static List<Render3DUtil.BufferedPrimitive> h = new ArrayList<>();
   private static final QuadInstance o = new QuadInstance();
   private static final ItemStackRenderState p = new ItemStackRenderState();
   private static final Map<Identifier, RenderType> q = new HashMap<>();
   private static final Map<Identifier, RenderType> r = new HashMap<>();
   private static final RenderType s = RenderTypes.glint();
   private static final Map<Identifier, RenderType> t = new HashMap<>();

   public static void a_void_RenderType_MeshData(RenderType var0, MeshData var1) {
      if (var1 != null) {
         if (var1.drawState().indexCount() == 0) {
            var1.close();
         } else {
            try {
               GpuBuffer var2 = RenderSystem.getDevice().createBuffer(() -> "Night immediate vertices", 32, var1.vertexBuffer());
               GpuBuffer var3 = null;
               GpuBuffer var5;
               IndexType var6;
               if (var1.indexBuffer() == null) {
                  AutoStorageIndexBuffer var4 = RenderSystem.getSequentialBuffer(var1.drawState().primitiveTopology());
                  var5 = var4.getBuffer(var1.drawState().indexCount());
                  var6 = var4.type();
               } else {
                  var5 = var3 = RenderSystem.getDevice().createBuffer(() -> "Night immediate indices", 64, var1.indexBuffer());
                  var6 = var1.drawState().indexType();
               }

               try {
                  var0.prepare().drawFromBuffer(var2, var5, var6, 0, 0, var1.drawState().indexCount());
               } catch (Exception var13) {
                  if (i.add(var0 + ": " + var13)) {
                     Night.LOGGER.error("Renderer3D draw failed for " + var0, var13);
                  }
               } finally {
                  var2.close();
                  if (var3 != null) {
                     var3.close();
                  }
               }
            } catch (Throwable var15) {
               if (var1 != null) {
                  try {
                     var1.close();
                  } catch (Throwable var12) {
                     var15.addSuppressed(var12);
                  }
               }

               throw var15;
            }

            if (var1 != null) {
               var1.close();
            }
         }
      }
   }

   public static void a_void_PoseStack_AABB_Color(PoseStack var0, AABB var1, Color var2) {
      a_void_PoseStack_AABB_Color_Color(var0, var1, var2, var2);
   }

   public static void a_void_PoseStack_AABB_Color_Color(PoseStack var0, AABB var1, Color var2, Color var3) {
      a(b, var0, var1, var2, var3);
   }

   public static void a(List<Render3DUtil.BufferedPrimitive> var0, PoseStack var1, AABB var2, Color var3, Color var4) {
      if (a) {
         if (a_bool_AABB(var2)) {
            Matrix4f var5 = var1.last().pose();
            var2 = b_AABB_AABB(var2);
            var0.add(
               new Render3DUtil.BufferedPrimitive(
                  new Render3DUtil.BufferedVertex(var5, (float)var2.minX, (float)var2.maxY, (float)var2.minZ, var3.getRGB()),
                  new Render3DUtil.BufferedVertex(var5, (float)var2.minX, (float)var2.maxY, (float)var2.maxZ, var3.getRGB()),
                  new Render3DUtil.BufferedVertex(var5, (float)var2.maxX, (float)var2.maxY, (float)var2.maxZ, var3.getRGB()),
                  new Render3DUtil.BufferedVertex(var5, (float)var2.maxX, (float)var2.maxY, (float)var2.minZ, var3.getRGB())
               )
            );
            var0.add(
               new Render3DUtil.BufferedPrimitive(
                  new Render3DUtil.BufferedVertex(var5, (float)var2.minX, (float)var2.minY, (float)var2.maxZ, var4.getRGB()),
                  new Render3DUtil.BufferedVertex(var5, (float)var2.maxX, (float)var2.minY, (float)var2.maxZ, var4.getRGB()),
                  new Render3DUtil.BufferedVertex(var5, (float)var2.maxX, (float)var2.maxY, (float)var2.maxZ, var3.getRGB()),
                  new Render3DUtil.BufferedVertex(var5, (float)var2.minX, (float)var2.maxY, (float)var2.maxZ, var3.getRGB())
               )
            );
            var0.add(
               new Render3DUtil.BufferedPrimitive(
                  new Render3DUtil.BufferedVertex(var5, (float)var2.maxX, (float)var2.maxY, (float)var2.maxZ, var3.getRGB()),
                  new Render3DUtil.BufferedVertex(var5, (float)var2.maxX, (float)var2.minY, (float)var2.maxZ, var4.getRGB()),
                  new Render3DUtil.BufferedVertex(var5, (float)var2.maxX, (float)var2.minY, (float)var2.minZ, var4.getRGB()),
                  new Render3DUtil.BufferedVertex(var5, (float)var2.maxX, (float)var2.maxY, (float)var2.minZ, var3.getRGB())
               )
            );
            var0.add(
               new Render3DUtil.BufferedPrimitive(
                  new Render3DUtil.BufferedVertex(var5, (float)var2.maxX, (float)var2.maxY, (float)var2.minZ, var3.getRGB()),
                  new Render3DUtil.BufferedVertex(var5, (float)var2.maxX, (float)var2.minY, (float)var2.minZ, var4.getRGB()),
                  new Render3DUtil.BufferedVertex(var5, (float)var2.minX, (float)var2.minY, (float)var2.minZ, var4.getRGB()),
                  new Render3DUtil.BufferedVertex(var5, (float)var2.minX, (float)var2.maxY, (float)var2.minZ, var3.getRGB())
               )
            );
            var0.add(
               new Render3DUtil.BufferedPrimitive(
                  new Render3DUtil.BufferedVertex(var5, (float)var2.minX, (float)var2.maxY, (float)var2.minZ, var3.getRGB()),
                  new Render3DUtil.BufferedVertex(var5, (float)var2.minX, (float)var2.minY, (float)var2.minZ, var4.getRGB()),
                  new Render3DUtil.BufferedVertex(var5, (float)var2.minX, (float)var2.minY, (float)var2.maxZ, var4.getRGB()),
                  new Render3DUtil.BufferedVertex(var5, (float)var2.minX, (float)var2.maxY, (float)var2.maxZ, var3.getRGB())
               )
            );
            var0.add(
               new Render3DUtil.BufferedPrimitive(
                  new Render3DUtil.BufferedVertex(var5, (float)var2.minX, (float)var2.minY, (float)var2.minZ, var4.getRGB()),
                  new Render3DUtil.BufferedVertex(var5, (float)var2.maxX, (float)var2.minY, (float)var2.minZ, var4.getRGB()),
                  new Render3DUtil.BufferedVertex(var5, (float)var2.maxX, (float)var2.minY, (float)var2.maxZ, var4.getRGB()),
                  new Render3DUtil.BufferedVertex(var5, (float)var2.minX, (float)var2.minY, (float)var2.maxZ, var4.getRGB())
               )
            );
         }
      }
   }

   public static void b(PoseStack var0, AABB var1, Color var2) {
      b(var0, var1, var2, var2);
   }

   public static void b(PoseStack var0, AABB var1, Color var2, Color var3) {
      b(c, var0, var1, var2, var3);
   }

   public static void b(List<Render3DUtil.BufferedPrimitive> var0, PoseStack var1, AABB var2, Color var3, Color var4) {
      if (a) {
         if (a_bool_AABB(var2)) {
            Matrix4f var5 = var1.last().pose();
            var2 = b_AABB_AABB(var2);
            var0.add(
               new Render3DUtil.BufferedPrimitive(
                  new Render3DUtil.BufferedVertex(var5, (float)var2.minX, (float)var2.minY, (float)var2.minZ, var4.getRGB()),
                  new Render3DUtil.BufferedVertex(var5, (float)var2.minX, (float)var2.minY, (float)var2.maxZ, var4.getRGB()),
                  new Render3DUtil.BufferedVertex(var5, (float)var2.minX, (float)var2.minY, (float)var2.maxZ, var4.getRGB()),
                  new Render3DUtil.BufferedVertex(var5, (float)var2.maxX, (float)var2.minY, (float)var2.maxZ, var4.getRGB())
               )
            );
            var0.add(
               new Render3DUtil.BufferedPrimitive(
                  new Render3DUtil.BufferedVertex(var5, (float)var2.maxX, (float)var2.minY, (float)var2.maxZ, var4.getRGB()),
                  new Render3DUtil.BufferedVertex(var5, (float)var2.maxX, (float)var2.minY, (float)var2.minZ, var4.getRGB()),
                  new Render3DUtil.BufferedVertex(var5, (float)var2.maxX, (float)var2.minY, (float)var2.minZ, var4.getRGB()),
                  new Render3DUtil.BufferedVertex(var5, (float)var2.minX, (float)var2.minY, (float)var2.minZ, var4.getRGB())
               )
            );
            var0.add(
               new Render3DUtil.BufferedPrimitive(
                  new Render3DUtil.BufferedVertex(var5, (float)var2.minX, (float)var2.maxY, (float)var2.minZ, var3.getRGB()),
                  new Render3DUtil.BufferedVertex(var5, (float)var2.minX, (float)var2.maxY, (float)var2.maxZ, var3.getRGB()),
                  new Render3DUtil.BufferedVertex(var5, (float)var2.minX, (float)var2.maxY, (float)var2.maxZ, var3.getRGB()),
                  new Render3DUtil.BufferedVertex(var5, (float)var2.maxX, (float)var2.maxY, (float)var2.maxZ, var3.getRGB())
               )
            );
            var0.add(
               new Render3DUtil.BufferedPrimitive(
                  new Render3DUtil.BufferedVertex(var5, (float)var2.maxX, (float)var2.maxY, (float)var2.maxZ, var3.getRGB()),
                  new Render3DUtil.BufferedVertex(var5, (float)var2.maxX, (float)var2.maxY, (float)var2.minZ, var3.getRGB()),
                  new Render3DUtil.BufferedVertex(var5, (float)var2.maxX, (float)var2.maxY, (float)var2.minZ, var3.getRGB()),
                  new Render3DUtil.BufferedVertex(var5, (float)var2.minX, (float)var2.maxY, (float)var2.minZ, var3.getRGB())
               )
            );
            var0.add(
               new Render3DUtil.BufferedPrimitive(
                  new Render3DUtil.BufferedVertex(var5, (float)var2.minX, (float)var2.minY, (float)var2.minZ, var4.getRGB()),
                  new Render3DUtil.BufferedVertex(var5, (float)var2.minX, (float)var2.maxY, (float)var2.minZ, var3.getRGB()),
                  new Render3DUtil.BufferedVertex(var5, (float)var2.maxX, (float)var2.minY, (float)var2.minZ, var4.getRGB()),
                  new Render3DUtil.BufferedVertex(var5, (float)var2.maxX, (float)var2.maxY, (float)var2.minZ, var3.getRGB())
               )
            );
            var0.add(
               new Render3DUtil.BufferedPrimitive(
                  new Render3DUtil.BufferedVertex(var5, (float)var2.maxX, (float)var2.minY, (float)var2.maxZ, var4.getRGB()),
                  new Render3DUtil.BufferedVertex(var5, (float)var2.maxX, (float)var2.maxY, (float)var2.maxZ, var3.getRGB()),
                  new Render3DUtil.BufferedVertex(var5, (float)var2.minX, (float)var2.minY, (float)var2.maxZ, var4.getRGB()),
                  new Render3DUtil.BufferedVertex(var5, (float)var2.minX, (float)var2.maxY, (float)var2.maxZ, var3.getRGB())
               )
            );
         }
      }
   }

   public static void a_void_PoseStack_Entity_float_Color(PoseStack var0, Entity var1, float var2, Color var3) {
      a(d, e, var0, var1, var2, var3);
   }

   public static void a(
      List<Render3DUtil.BufferedPrimitive> var0, List<Render3DUtil.BufferedPrimitive> var1, PoseStack var2, Entity var3, float var4, Color var5
   ) {
      if (a && var3 != null && mc.player != null) {
         Vec3 var6 = EntityUtil.a_Vec3_Entity_float(var3, var4);
         double var7 = Math.max(0.5, var3.getBoundingBox().getYsize());
         double var9 = Math.max(0.4, var3.getBbWidth() / 2.0 + 0.25);
         if (a_bool_AABB(new AABB(var6.x - var9, var6.y - 0.5, var6.z - var9, var6.x + var9, var6.y + var7 + 0.5, var6.z + var9))) {
            Matrix4f var11 = var2.last().pose();
            double var12 = System.currentTimeMillis() / 420.0;
            double var14 = Math.sin(var12);
            double var16 = Math.cos(var12);
            double var18 = (var14 + 1.0) / 2.0;
            double var20 = var6.y + var18 * var7;
            double var22 = -var16 * 0.225;
            double var24 = var20 + var22;
            float var26 = var5.getAlpha() / 255.0F;
            if (!(var26 <= 0.001F)) {
               int var27 = (int)(180.0F * var26);
               Color var28 = ColorUtil.a(var5, var27);
               Color var29 = ColorUtil.a(var5, 0);
               Color var30 = ColorUtil.a(var5, (int)(255.0F * var26));

               for (int var31 = 0; var31 < 48; var31++) {
                  double var32 = var31 * 2.0 * Math.PI / 48;
                  double var34 = (var31 + 1) * 2.0 * Math.PI / 48;
                  double var36 = var6.x + Math.cos(var32) * var9;
                  double var38 = var6.z + Math.sin(var32) * var9;
                  double var40 = var6.x + Math.cos(var34) * var9;
                  double var42 = var6.z + Math.sin(var34) * var9;
                  Vec3 var44 = a_Vec3_Vec3(new Vec3(var36, var20, var38));
                  Vec3 var45 = a_Vec3_Vec3(new Vec3(var40, var20, var42));
                  Vec3 var46 = a_Vec3_Vec3(new Vec3(var40, var24, var42));
                  Vec3 var47 = a_Vec3_Vec3(new Vec3(var36, var24, var38));
                  var0.add(
                     new Render3DUtil.BufferedPrimitive(
                        new Render3DUtil.BufferedVertex(var11, (float)var44.x, (float)var44.y, (float)var44.z, var28.getRGB()),
                        new Render3DUtil.BufferedVertex(var11, (float)var45.x, (float)var45.y, (float)var45.z, var28.getRGB()),
                        new Render3DUtil.BufferedVertex(var11, (float)var46.x, (float)var46.y, (float)var46.z, var29.getRGB()),
                        new Render3DUtil.BufferedVertex(var11, (float)var47.x, (float)var47.y, (float)var47.z, var29.getRGB())
                     )
                  );
                  var0.add(
                     new Render3DUtil.BufferedPrimitive(
                        new Render3DUtil.BufferedVertex(var11, (float)var47.x, (float)var47.y, (float)var47.z, var29.getRGB()),
                        new Render3DUtil.BufferedVertex(var11, (float)var46.x, (float)var46.y, (float)var46.z, var29.getRGB()),
                        new Render3DUtil.BufferedVertex(var11, (float)var45.x, (float)var45.y, (float)var45.z, var28.getRGB()),
                        new Render3DUtil.BufferedVertex(var11, (float)var44.x, (float)var44.y, (float)var44.z, var28.getRGB())
                     )
                  );
                  var1.add(
                     new Render3DUtil.BufferedPrimitive(
                        new Render3DUtil.BufferedVertex(var11, (float)var44.x, (float)var44.y, (float)var44.z, var30.getRGB(), 2.0F),
                        new Render3DUtil.BufferedVertex(var11, (float)var45.x, (float)var45.y, (float)var45.z, var30.getRGB(), 2.0F)
                     )
                  );
               }
            }
         }
      }
   }

   public static void c(PoseStack var0, AABB var1, Color var2) {
      c(c, var0, var1, var2, var2);
   }

   public static void c(List<Render3DUtil.BufferedPrimitive> var0, PoseStack var1, AABB var2, Color var3, Color var4) {
      if (a) {
         if (a_bool_AABB(var2)) {
            Matrix4f var5 = var1.last().pose();
            var2 = b_AABB_AABB(var2);
            var0.add(
               new Render3DUtil.BufferedPrimitive(
                  new Render3DUtil.BufferedVertex(var5, (float)var2.minX, (float)var2.minY, (float)var2.minZ, var4.getRGB()),
                  new Render3DUtil.BufferedVertex(var5, (float)var2.maxX, (float)var2.minY, (float)var2.maxZ, var4.getRGB())
               )
            );
            var0.add(
               new Render3DUtil.BufferedPrimitive(
                  new Render3DUtil.BufferedVertex(var5, (float)var2.maxX, (float)var2.minY, (float)var2.minZ, var4.getRGB()),
                  new Render3DUtil.BufferedVertex(var5, (float)var2.minX, (float)var2.minY, (float)var2.maxZ, var4.getRGB())
               )
            );
            var0.add(
               new Render3DUtil.BufferedPrimitive(
                  new Render3DUtil.BufferedVertex(var5, (float)var2.minX, (float)var2.maxY, (float)var2.minZ, var3.getRGB()),
                  new Render3DUtil.BufferedVertex(var5, (float)var2.maxX, (float)var2.maxY, (float)var2.maxZ, var3.getRGB())
               )
            );
            var0.add(
               new Render3DUtil.BufferedPrimitive(
                  new Render3DUtil.BufferedVertex(var5, (float)var2.maxX, (float)var2.maxY, (float)var2.minZ, var3.getRGB()),
                  new Render3DUtil.BufferedVertex(var5, (float)var2.minX, (float)var2.maxY, (float)var2.maxZ, var3.getRGB())
               )
            );
         }
      }
   }

   public static void a(PoseStack var0, float var1, float var2, float var3, float var4, Color var5) {
      if (a) {
         Matrix4f var6 = var0.last().pose();
         int var7 = var5.getRGB();
         b.add(
            new Render3DUtil.BufferedPrimitive(
               new Render3DUtil.BufferedVertex(var6, var1, var2, 0.0F, var7),
               new Render3DUtil.BufferedVertex(var6, var1, var4, 0.0F, var7),
               new Render3DUtil.BufferedVertex(var6, var3, var4, 0.0F, var7),
               new Render3DUtil.BufferedVertex(var6, var3, var2, 0.0F, var7)
            )
         );
      }
   }

   public static void b(PoseStack var0, float var1, float var2, float var3, float var4, Color var5) {
      if (a) {
         Matrix4f var6 = var0.last().pose();
         int var7 = var5.getRGB();
         c.add(
            new Render3DUtil.BufferedPrimitive(
               new Render3DUtil.BufferedVertex(var6, var1, var2, 0.0F, var7), new Render3DUtil.BufferedVertex(var6, var3, var2, 0.0F, var7)
            )
         );
         c.add(
            new Render3DUtil.BufferedPrimitive(
               new Render3DUtil.BufferedVertex(var6, var3, var2, 0.0F, var7), new Render3DUtil.BufferedVertex(var6, var3, var4, 0.0F, var7)
            )
         );
         c.add(
            new Render3DUtil.BufferedPrimitive(
               new Render3DUtil.BufferedVertex(var6, var3, var4, 0.0F, var7), new Render3DUtil.BufferedVertex(var6, var1, var4, 0.0F, var7)
            )
         );
         c.add(
            new Render3DUtil.BufferedPrimitive(
               new Render3DUtil.BufferedVertex(var6, var1, var4, 0.0F, var7), new Render3DUtil.BufferedVertex(var6, var1, var2, 0.0F, var7)
            )
         );
      }
   }

   public static void a_void_PoseStack_Vec3_Vec3_Color(PoseStack var0, Vec3 var1, Vec3 var2, Color var3) {
      a(c, var0, var1, var2, var3);
   }

   public static void a(List<Render3DUtil.BufferedPrimitive> var0, PoseStack var1, Vec3 var2, Vec3 var3, Color var4) {
      a(var0, var1, var2, var3, var4, var4);
   }

   public static void a(PoseStack var0, Vec3 var1, Vec3 var2, Color var3, Color var4) {
      a(c, var0, var1, var2, var3, var4);
   }

   public static void a(List<Render3DUtil.BufferedPrimitive> var0, PoseStack var1, Vec3 var2, Vec3 var3, Color var4, Color var5) {
      if (a) {
         if (mc.gameRenderer != null && mc.gameRenderer.mainCamera() != null) {
            Vec3 var6 = mc.gameRenderer.mainCamera().position();
            Vec3 var7 = Vec3.directionFromRotation(mc.gameRenderer.mainCamera().xRot(), mc.gameRenderer.mainCamera().yRot());
            double var8 = (var2.x - var6.x) * var7.x + (var2.y - var6.y) * var7.y + (var2.z - var6.z) * var7.z;
            double var10 = (var3.x - var6.x) * var7.x + (var3.y - var6.y) * var7.y + (var3.z - var6.z) * var7.z;
            double var12 = 0.2;
            if (!(var8 < var12) || !(var10 < var12)) {
               if (var8 >= var12 && var10 < var12) {
                  double var18 = (var8 - var12) / (var8 - var10);
                  var3 = new Vec3(var2.x + (var3.x - var2.x) * var18, var2.y + (var3.y - var2.y) * var18, var2.z + (var3.z - var2.z) * var18);
                  if (!var4.equals(var5)) {
                     var5 = ColorUtil.a(var5, (int)(var5.getAlpha() * (1.0 - var18) + var4.getAlpha() * var18));
                  }
               } else if (var8 < var12 && var10 >= var12) {
                  double var14 = (var12 - var8) / (var10 - var8);
                  var2 = new Vec3(var2.x + (var3.x - var2.x) * var14, var2.y + (var3.y - var2.y) * var14, var2.z + (var3.z - var2.z) * var14);
                  if (!var4.equals(var5)) {
                     var4 = ColorUtil.a(var4, (int)(var4.getAlpha() * (1.0 - var14) + var5.getAlpha() * var14));
                  }
               }

               Matrix4f var19 = var1.last().pose();
               var2 = a_Vec3_Vec3(var2);
               var3 = a_Vec3_Vec3(var3);
               var0.add(
                  new Render3DUtil.BufferedPrimitive(
                     new Render3DUtil.BufferedVertex(var19, (float)var2.x, (float)var2.y, (float)var2.z, var4.getRGB()),
                     new Render3DUtil.BufferedVertex(var19, (float)var3.x, (float)var3.y, (float)var3.z, var5.getRGB())
                  )
               );
            }
         }
      }
   }

   private static RenderType a_RenderType_MaterialInfo_bool(MaterialInfo var0, boolean var1) {
      boolean var2 = var0.layer().translucent();
      Identifier var3 = var0.sprite().atlasLocation();
      Map<Identifier, RenderType> var4 = var2 ? r : q;
      Snippet[] var5 = new Snippet[]{RenderPipelinesAccessor.getItemSnippet()};
      return var4.computeIfAbsent(
         var3,
         var3x -> {
            Builder var4x = RenderPipeline.builder(var5)
               .withLocation(var2 ? "night/no_depth_item_translucent" : "night/no_depth_item_cutout")
               .withShaderDefine("ALPHA_CUTOUT", 0.1F)
               .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, true));
            var4x.withColorTargetState(var2 ? new ColorTargetState(BlendFunction.TRANSLUCENT) : ColorTargetState.DEFAULT);
            RenderSetupBuilder var5x = RenderSetup.builder(var4x.build())
               .withTexture("Sampler0", var3x)
               .useOverlay()
               .useLightmap()
               .affectsCrumbling()
               .setOutline(OutlineProperty.AFFECTS_OUTLINE);
            return net.minecraft.client.renderer.rendertype.RenderTypeHelper.create(
               (var2 ? "night_no_depth_item_translucent" : "night_no_depth_item_cutout") + (var1 ? "_glint" : ""), var5x.createRenderSetup()
            );
         }
      );
   }

   private static BufferBuilder a_BufferBuilder_Map_Map_RenderType(
      Map<RenderType, ByteBufferBuilder> var0, Map<RenderType, BufferBuilder> var1, RenderType var2
   ) {
      return var1.computeIfAbsent(var2, var1x -> {
         ByteBufferBuilder var2x = new ByteBufferBuilder(256);
         var0.put(var1x, var2x);
         return new BufferBuilder(var2x, var1x.primitiveTopology(), var1x.format());
      });
   }

   // $VF: Could not verify finally blocks. A semaphore variable has been added to preserve control flow.
   // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
   public static void a_void_PoseStack_ItemStack_ItemOwner(PoseStack var0, ItemStack var1, ItemOwner var2) {
      if (a && !var1.isEmpty()) {
         p.clear();
         mc.getItemModelResolver().updateForTopItem(p, var1, ItemDisplayContext.GUI, mc.level, var2, 0);
         ItemStackRenderStateAccessor var3 = (ItemStackRenderStateAccessor)p;
         o.setLightCoords(15728880);
         o.setOverlayCoords(OverlayTexture.NO_OVERLAY);
         FogRenderer var4 = ((GameRendererAccessor)mc.gameRenderer).getFogRenderer();
         RenderSystem.setShaderFog(var4.getBuffer(FogMode.NONE));
         HashMap var5 = new HashMap();
         HashMap var6 = new HashMap();
         boolean var20 = false /* VF: Semaphore variable */;

         try {
            var20 = true;

            for (int var7 = 0; var7 < var3.night$getActiveLayerCount(); var7++) {
               LayerRenderStateAccessor var8 = (LayerRenderStateAccessor)var3.night$getLayers()[var7];
               var0.pushPose();
               var8.night$applyTransform(var0.last());
               IntList var9 = var8.night$getTintLayers();
               int[] var10 = var9 != null ? var9.toArray(new int[0]) : new int[0];
               boolean var11 = var8.night$getFoilType() != FoilType.NONE;

               for (BakedQuad var13 : (Iterable<BakedQuad>)(Object)var8.night$getQuads()) {
                  MaterialInfo var14 = var13.materialInfo();
                  RenderType var15 = a_RenderType_MaterialInfo_bool(var14, false);
                  int var16 = var14.isTinted() ? var14.tintIndex() : -1;
                  o.setColor(var16 >= 0 && var16 < var10.length ? var10[var16] : -1);
                  if (var11) {
                     a_BufferBuilder_Map_Map_RenderType(var5, var6, s).putBakedQuad(var0.last(), var13, o);
                  }

                  a_BufferBuilder_Map_Map_RenderType(var5, var6, var15).putBakedQuad(var0.last(), var13, o);
               }

               var0.popPose();
            }

            for (Entry var25 : (Iterable<Entry>)(Object)var6.entrySet()) {
               if (var25.getKey() != s) {
                  a_void_RenderType_MeshData((RenderType)var25.getKey(), ((BufferBuilder)var25.getValue()).build());
               }
            }

            BufferBuilder var23 = (BufferBuilder)var6.get(s);
            if (var23 != null) {
               a_void_RenderType_MeshData(s, var23.build());
               var20 = false;
            } else {
               var20 = false;
            }
         } finally {
            if (var20) {
               Iterator var18 = var5.values().iterator();

               while (var18.hasNext()) {
                  ((ByteBufferBuilder)var18.next()).close();
               }

               RenderSystem.setShaderFog(var4.getBuffer(FogMode.WORLD));
            }
         }

         for (ByteBufferBuilder var26 : (Iterable<ByteBufferBuilder>)(Object)var5.values()) {
            var26.close();
         }

         RenderSystem.setShaderFog(var4.getBuffer(FogMode.WORLD));
      }
   }

   public static void a(PoseStack var0, String var1, double var2, double var4, double var6, int var8, boolean var9, Color var10, boolean var11) {
      a(var0, var1, var2, var4, var6, var8, var9, false, var10, var11);
   }

   public static void b(PoseStack var0, String var1, double var2, double var4, double var6, int var8, boolean var9, Color var10, boolean var11) {
      a(var0, var1, var2, var4, var6, var8, var9, true, var10, var11);
   }

   public static void a(PoseStack var0, String var1, double var2, double var4, double var6, int var8, boolean var9, boolean var10, Color var11, boolean var12) {
      float var13 = (float)Math.sqrt(mc.gameRenderer.mainCamera().position().distanceToSqr(var2, var4, var6));
      float var14 = 0.0018F + var8 / 10000.0F * Math.max(var13, 8.0F);
      a(var0, var1, var2, var4, var6, var14, var9, var10, var11, var12);
   }

   public static void a(PoseStack var0, String var1, double var2, double var4, double var6, float var8, boolean var9, boolean var10, Color var11, boolean var12) {
      Vec3 var13 = mc.gameRenderer.mainCamera().position();
      Vec3 var14 = new Vec3(var2 - var13.x, var4 - var13.y, var6 - var13.z);
      var0.pushPose();
      var0.translate(var14.x, var14.y, var14.z);
      PacketAndRenderUtil.a(var0, mc.gameRenderer.mainCamera().rotation());
      var0.scale(var8, -var8, var8);
      String[] var15 = var1.split("\n");
      float var16 = Night.fontManager.getFontHeight();
      float var17 = var16 + 2.0F;
      float var18 = (var15.length - 1) * var17 + var16;
      float var19 = var10 ? -var18 / 2.0F : -var15.length * var17 + 1.0F;
      if (var9) {
         float var20 = 0.0F;

         for (String var24 : var15) {
            float var25 = Night.fontManager.getStringWidth(var24);
            if (var25 > var20) {
               var20 = var25;
            }
         }

         float var33 = -var20 / 2.0F - 2.0F;
         float var35 = var19 - 2.0F;
         float var37 = var20 / 2.0F + 2.0F;
         float var39 = var19 + var18 + 2.0F;
         int var40 = new Color(0, 0, 0, 100).getRGB();
         Matrix4f var26 = var0.last().pose();
         ByteBufferBuilder var27 = ByteBufferBuilder.exactlySized(4 * DefaultVertexFormat.POSITION_COLOR.getVertexSize());

         try {
            BufferBuilder var28 = new BufferBuilder(var27, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_COLOR);
            var28.addVertex(var26, var33, var35, 0.0F).setColor(var40);
            var28.addVertex(var26, var33, var39, 0.0F).setColor(var40);
            var28.addVertex(var26, var37, var39, 0.0F).setColor(var40);
            var28.addVertex(var26, var37, var35, 0.0F).setColor(var40);
            MeshData var29 = var28.build();
            a_void_RenderType_MeshData(j, var29);
         } catch (Throwable var31) {
            if (var27 != null) {
               try {
                  var27.close();
               } catch (Throwable var30) {
                  var31.addSuppressed(var30);
               }
            }

            throw var31;
         }

         if (var27 != null) {
            var27.close();
         }
      }

      for (String var38 : var15) {
         if (var12) {
            Night.fontManager.drawPlainWorldText(var0, var38, -Night.fontManager.getStringWidth(var38) / 2.0F, var19, var11, true);
         } else {
            Night.fontManager.drawWorldText(var0, var38, -Night.fontManager.getStringWidth(var38) / 2.0F, var19, var11, false);
         }

         var19 += var17;
      }

      var0.popPose();
   }

   public static RenderType a_RenderType_Identifier(Identifier var0) {
      return t.computeIfAbsent(
         var0,
         var0x -> net.minecraft.client.renderer.rendertype.RenderTypeHelper.create(
            "night_no_depth_tex_" + var0x.getNamespace() + "_" + var0x.getPath().replace('/', '_'),
            RenderSetup.builder(
                  RenderPipeline.builder(new Snippet[]{RenderPipelinesAccessor.getGuiTexturedSnippet()})
                     .withLocation("night/no_depth_tex")
                     .withCull(false)
                     .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
                     .withColorTargetState(new ColorTargetState(new BlendFunction(BlendFactor.SRC_ALPHA, BlendFactor.ONE_MINUS_SRC_ALPHA)))
                     .build()
               )
               .withTexture("Sampler0", var0x)
               .sortOnUpload()
               .createRenderSetup()
         )
      );
   }

   public static void a(PoseStack var0, Identifier var1, float var2, float var3, float var4, float var5, int var6) {
      Matrix4f var7 = var0.last().pose();
      RenderType var8 = a_RenderType_Identifier(var1);
      ByteBufferBuilder var9 = ByteBufferBuilder.exactlySized(4 * DefaultVertexFormat.POSITION_TEX_COLOR.getVertexSize());

      try {
         BufferBuilder var10 = new BufferBuilder(var9, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
         var10.addVertex(var7, var2, var5, 0.0F).setUv(0.0F, 1.0F).setColor(var6);
         var10.addVertex(var7, var4, var5, 0.0F).setUv(1.0F, 1.0F).setColor(var6);
         var10.addVertex(var7, var4, var3, 0.0F).setUv(1.0F, 0.0F).setColor(var6);
         var10.addVertex(var7, var2, var3, 0.0F).setUv(0.0F, 0.0F).setColor(var6);
         MeshData var11 = var10.build();
         a_void_RenderType_MeshData(var8, var11);
      } catch (Throwable var13) {
         if (var9 != null) {
            try {
               var9.close();
            } catch (Throwable var12) {
               var13.addSuppressed(var12);
            }
         }

         throw var13;
      }

      if (var9 != null) {
         var9.close();
      }
   }

   public static void a() {
      b.clear();
      c.clear();
      d.clear();
      e.clear();
      f.clear();
      g.clear();
      h.clear();
      GlintRenderLayer.a.clear();
      a = true;
   }

   public static void a_void_List_List_bool(List<Render3DUtil.BufferedPrimitive> var0, List<Render3DUtil.BufferedPrimitive> var1, boolean var2) {
      if (!var0.isEmpty()) {
         int var3 = a_int_List(var0);
         ByteBufferBuilder var4 = ByteBufferBuilder.exactlySized(var3 * DefaultVertexFormat.POSITION_COLOR.getVertexSize());

         try {
            BufferBuilder var5 = new BufferBuilder(var4, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_COLOR);
            Iterator var6 = var0.iterator();

            while (var6.hasNext()) {
               ((Render3DUtil.BufferedPrimitive)var6.next()).a(var5);
            }

            MeshData var14 = var5.build();
            a_void_RenderType_MeshData(var2 ? m : j, var14);
         } catch (Throwable var10) {
            if (var4 != null) {
               try {
                  var4.close();
               } catch (Throwable var8) {
                  var10.addSuppressed(var8);
               }
            }

            throw var10;
         }

         if (var4 != null) {
            var4.close();
         }
      }

      if (!var1.isEmpty()) {
         int var11 = a_int_List(var1) * 2;
         ByteBufferBuilder var12 = ByteBufferBuilder.exactlySized(var11 * DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH.getVertexSize());

         try {
            BufferBuilder var13 = new BufferBuilder(var12, PrimitiveTopology.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH);
            a_void_BufferBuilder_List(var13, var1);
            MeshData var15 = var13.build();
            a_void_RenderType_MeshData(var2 ? n : k, var15);
         } catch (Throwable var9) {
            if (var12 != null) {
               try {
                  var12.close();
               } catch (Throwable var7) {
                  var9.addSuppressed(var7);
               }
            }

            throw var9;
         }

         if (var12 != null) {
            var12.close();
         }
      }

      RenderSystem.getDevice();
      var0.clear();
      var1.clear();
   }

   static int a_int_List(List<Render3DUtil.BufferedPrimitive> var0) {
      int var1 = 0;

      for (Render3DUtil.BufferedPrimitive var3 : (Iterable<Render3DUtil.BufferedPrimitive>)(Object)var0) {
         var1 += var3.value().length;
      }

      return var1;
   }

   public static void a_void_BufferBuilder_List(BufferBuilder var0, List<Render3DUtil.BufferedPrimitive> var1) {
      Iterator var2 = var1.iterator();

      while (var2.hasNext()) {
         Render3DUtil.BufferedVertex[] var3 = ((Render3DUtil.BufferedPrimitive)var2.next()).value();

         for (byte var4 = 0; var4 + 1 < var3.length; var4 += 2) {
            Render3DUtil.BufferedVertex var5 = var3[var4];
            Render3DUtil.BufferedVertex var6 = var3[var4 + 1];
            float var7 = var6.value - var5.value;
            float var8 = var6.value2 - var5.value2;
            float var9 = var6.value3 - var5.value3;
            float var10 = var7 * var7 + var8 * var8 + var9 * var9;
            float var11 = 0.05F;
            if (var10 > 1.0E-6F) {
               float var12 = (float)Math.sqrt(var10);
               float var13 = 1.0F / var12;
               var7 *= var13;
               var8 *= var13;
               var9 *= var13;
               var11 = Math.min(0.05F, var12 * 0.45F);
            } else {
               var7 = 0.0F;
               var8 = 1.0F;
               var9 = 0.0F;
            }

            var0.addVertex(var5.matrix, var5.value, var5.value2, var5.value3)
               .setColor(var5.count)
               .setNormal(var7 * var11, var8 * var11, var9 * var11)
               .setLineWidth(var5.value4);
            var0.addVertex(var6.matrix, var6.value, var6.value2, var6.value3)
               .setColor(var6.count)
               .setNormal(-var7 * var11, -var8 * var11, -var9 * var11)
               .setLineWidth(var6.value4);
         }
      }
   }

   public static boolean a_bool_AABB(AABB var0) {
      return mc.gameRenderer.mainCamera().getCullFrustum().isVisible(var0);
   }

   private static Vec3 a_Vec3_Vec3(Vec3 var0) {
      Vec3 var1 = mc.gameRenderer.mainCamera().position();
      return new Vec3(var0.x - var1.x, var0.y - var1.y, var0.z - var1.z);
   }

   private static AABB b_AABB_AABB(AABB var0) {
      Vec3 var1 = mc.gameRenderer.mainCamera().position();
      return new AABB(var0.minX - var1.x, var0.minY - var1.y, var0.minZ - var1.z, var0.maxX - var1.x, var0.maxY - var1.y, var0.maxZ - var1.z);
   }

   public record BufferedPrimitive(Render3DUtil.BufferedVertex... value) {
      public void a(BufferBuilder var1) {
         for (Render3DUtil.BufferedVertex var5 : this.value) {
            var1.addVertex(var5.matrix, var5.value, var5.value2, var5.value3).setColor(var5.count);
         }
      }
   }

   public record BufferedVertex(Matrix4f matrix, float value, float value2, float value3, int count, float value4) {
      public BufferedVertex(Matrix4f var1, float var2, float var3, float var4, int var5) {
         this(var1, var2, var3, var4, var5, 1.75F);
      }
   }
}
