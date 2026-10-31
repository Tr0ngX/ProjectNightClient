package night.module.impl.visuals;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ExperienceBottleItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.LingeringPotionItem;
import net.minecraft.world.item.SplashPotionItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import night.events.EventTarget;
import night.events.impl.Render3DEvent;
import night.module.Module;
import night.module.ModuleInfo;
import night.setting.impl.CategorySetting;
import night.setting.impl.ColorSetting;
import night.setting.impl.ModeSetting;
import night.util.color.ColorUtil;
import night.util.render.Render3DUtil;
import night.util.world.EntityUtil;
import org.joml.Matrix4f;

@ModuleInfo(
   name = "Trajectories",
   description = "Draws a predicted trajectory of where throwables will end up when you throw them.",
   category = Module.Category.VISUALS
)
public class Trajectories extends Module {
   public ColorSetting lineColor = new ColorSetting("LineColor", "The color of the trajectory line.", ColorUtil.c());
   public CategorySetting entitiesCategory = new CategorySetting("Entities", "The rendering that will be applied to entities hit by the trajectory.");
   public ModeSetting entitiesMode = new ModeSetting(
      "EntitiesMode",
      "Mode",
      "The rendering that will be applied to the target entity.",
      new CategorySetting.CategoryCondition(this.entitiesCategory),
      "Both",
      new String[]{"None", "Fill", "Outline", "Both"}
   );
   public ColorSetting entitiesFillColor = new ColorSetting(
      "EntitiesFillColor",
      "FillColor",
      "The color that will be used for the fill rendering.",
      new ModeSetting.ModeCondition(this.entitiesMode, "Fill", "Both"),
      ColorUtil.b()
   );
   public ColorSetting entitiesOutlineColor = new ColorSetting(
      "EntitiesOutlineColor",
      "OutlineColor",
      "The color that will be used for the outline rendering.",
      new ModeSetting.ModeCondition(this.entitiesMode, "Outline", "Both"),
      ColorUtil.c()
   );
   public CategorySetting blocksCategory = new CategorySetting("Blocks", "The rendering that will be applied to blocks hit by the trajectory.");
   public ModeSetting blocksMode = new ModeSetting(
      "BlocksMode",
      "Mode",
      "The rendering that will be applied to the target block.",
      new CategorySetting.CategoryCondition(this.blocksCategory),
      "Both",
      new String[]{"None", "Fill", "Outline", "Both"}
   );
   public ColorSetting blocksFillColor = new ColorSetting(
      "BlocksFillColor",
      "FillColor",
      "The color that will be used for the fill rendering.",
      new ModeSetting.ModeCondition(this.blocksMode, "Fill", "Both"),
      ColorUtil.b()
   );
   public ColorSetting blocksOutlineColor = new ColorSetting(
      "BlocksOutlineColor",
      "OutlineColor",
      "The color that will be used for the outline rendering.",
      new ModeSetting.ModeCondition(this.blocksMode, "Outline", "Both"),
      ColorUtil.c()
   );
   private List<Entity> n = new ArrayList<>();

   @EventTarget
   public void a_void_Render3DEvent(Render3DEvent var1) {
      if (mc.player != null && mc.level != null) {
         if (!mc.gui.hud.isHidden() && mc.options.getCameraType().isFirstPerson()) {
            InteractionHand var2;
            if (!(mc.player.getMainHandItem().getItem() instanceof BowItem)
               && !(mc.player.getMainHandItem().getItem() instanceof CrossbowItem)
               && !EntityUtil.a_bool_Item(mc.player.getMainHandItem().getItem())) {
               if (!(mc.player.getOffhandItem().getItem() instanceof BowItem)
                  && !(mc.player.getOffhandItem().getItem() instanceof CrossbowItem)
                  && !EntityUtil.a_bool_Item(mc.player.getOffhandItem().getItem())) {
                  return;
               }

               var2 = InteractionHand.OFF_HAND;
            } else {
               var2 = InteractionHand.MAIN_HAND;
            }

            this.n.clear();
            float var3 = Mth.lerp(var1.getTickDelta(), mc.player.yRotO, mc.player.getYRot());
            if (mc.player.getOffhandItem().getItem() instanceof CrossbowItem
                  && EnchantmentHelper.getItemEnchantmentLevel(
                        mc.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.MULTISHOT), mc.player.getOffhandItem()
                     )
                     != 0
               || mc.player.getMainHandItem().getItem() instanceof CrossbowItem
                  && EnchantmentHelper.getItemEnchantmentLevel(
                        mc.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.MULTISHOT), mc.player.getMainHandItem()
                     )
                     != 0) {
               this.a(
                  var1.getMatrices(),
                  var2 == InteractionHand.OFF_HAND ? mc.player.getOffhandItem().getItem() : mc.player.getMainHandItem().getItem(),
                  var3 - 10.0F,
                  var1.getTickDelta()
               );
               this.a(
                  var1.getMatrices(),
                  var2 == InteractionHand.OFF_HAND ? mc.player.getOffhandItem().getItem() : mc.player.getMainHandItem().getItem(),
                  var3,
                  var1.getTickDelta()
               );
               this.a(
                  var1.getMatrices(),
                  var2 == InteractionHand.OFF_HAND ? mc.player.getOffhandItem().getItem() : mc.player.getMainHandItem().getItem(),
                  var3 + 10.0F,
                  var1.getTickDelta()
               );
            } else {
               this.a(
                  var1.getMatrices(),
                  var2 == InteractionHand.OFF_HAND ? mc.player.getOffhandItem().getItem() : mc.player.getMainHandItem().getItem(),
                  var3,
                  var1.getTickDelta()
               );
            }
         }
      }
   }

   private void a(PoseStack var1, Item var2, float var3, float var4) {
      double var5 = Mth.lerp(var4, mc.player.xo, mc.player.getX());
      double var7 = Mth.lerp(var4, mc.player.yo, mc.player.getY());
      double var9 = Mth.lerp(var4, mc.player.zo, mc.player.getZ());
      var7 = var7 + mc.player.getEyeHeight(mc.player.getPose()) - 0.1000000014901161;
      float var11 = var2 instanceof BowItem ? 1.0F : 0.4F;
      float var12 = Mth.lerp(var4, mc.player.xRotO, mc.player.getXRot());
      double var13 = -Mth.sin(var3 / 180.0F * (float) Math.PI) * Mth.cos(var12 / 180.0F * (float) Math.PI) * var11;
      double var15 = -Mth.sin((var12 - this.b(var2)) / 180.0F * 3.141593F) * var11;
      double var17 = Mth.cos(var3 / 180.0F * (float) Math.PI) * Mth.cos(var12 / 180.0F * (float) Math.PI) * var11;
      float var19 = mc.player.getTicksUsingItem() / 20.0F;
      var19 = (var19 * var19 + var19 * 2.0F) / 3.0F;
      if (var19 > 1.0F || var19 == 0.0F) {
         var19 = 1.0F;
      }

      float var20 = Mth.sqrt((float)(var13 * var13 + var15 * var15 + var17 * var17));
      var13 /= var20;
      var15 /= var20;
      var17 /= var20;
      float var21 = (var2 instanceof BowItem ? var19 * 2.0F : (var2 instanceof CrossbowItem ? 2.2F : 1.0F)) * this.a_float_Item(var2);
      var13 *= var21;
      var15 *= var21;
      var17 *= var21;
      if (!mc.player.onGround()) {
         var15 += mc.player.getDeltaMovement().y;
      }

      Matrix4f var22 = var1.last().pose();
      boolean var23 = false;
      BlockHitResult var24 = null;
      Entity var25 = null;

      while (!var23 && var7 > -65.0) {
         Vec3 var26 = new Vec3(var5, var7, var9);
         var5 += var13;
         var7 += var15;
         var9 += var17;
         if (mc.level.getBlockState(new BlockPos((int)var5, (int)var7, (int)var9)).getBlock() == Blocks.WATER) {
            var13 *= 0.8;
            var15 *= 0.8;
            var17 *= 0.8;
         } else {
            var13 *= 0.99;
            var15 *= 0.99;
            var17 *= 0.99;
         }

         if (var2 instanceof BowItem) {
            var15 -= 0.05F;
         } else if (mc.player.getMainHandItem().getItem() instanceof CrossbowItem) {
            var15 -= 0.05F;
         } else {
            var15 -= 0.03F;
         }

         Vec3 var27 = new Vec3(var5, var7, var9);
         Entity var28 = this.a_Entity_AABB(new AABB(var5 - 1.0, var7 - 1.0, var9 - 1.0, var5 + 1.0, var7 + 1.0, var9 + 1.0));
         BlockHitResult var29 = mc.level.clip(new ClipContext(var26, var27, Block.OUTLINE, Fluid.NONE, mc.player));
         if (var28 != null) {
            var25 = var28;
            var23 = true;
         } else if (var29 != null && var29.getType() != Type.MISS) {
            var24 = var29;
            var23 = true;
         }

         Render3DUtil.c
            .add(
               new Render3DUtil.BufferedPrimitive(
                  new Render3DUtil.BufferedVertex(
                     var22,
                     (float)(var26.x - mc.gameRenderer.mainCamera().position().x),
                     (float)(var26.y - mc.gameRenderer.mainCamera().position().y),
                     (float)(var26.z - mc.gameRenderer.mainCamera().position().z),
                     this.lineColor.getColor().getRGB()
                  ),
                  new Render3DUtil.BufferedVertex(
                     var22,
                     (float)(var27.x - mc.gameRenderer.mainCamera().position().x),
                     (float)(var27.y - mc.gameRenderer.mainCamera().position().y),
                     (float)(var27.z - mc.gameRenderer.mainCamera().position().z),
                     this.lineColor.getColor().getRGB()
                  )
               )
            );
      }

      if (var24 != null && var24.getType() == Type.BLOCK) {
         AABB var39 = new AABB(
            var24.getLocation().x - 0.15,
            var24.getLocation().y - 0.15,
            var24.getLocation().z - 0.15,
            var24.getLocation().x + 0.15,
            var24.getLocation().y + 0.15,
            var24.getLocation().z + 0.15
         );
         if (this.blocksMode.getValue().equalsIgnoreCase("Fill") || this.blocksMode.getValue().equalsIgnoreCase("Both")) {
            Render3DUtil.a_void_PoseStack_AABB_Color(var1, var39, this.blocksFillColor.getColor());
         }

         if (this.blocksMode.getValue().equalsIgnoreCase("Outline") || this.blocksMode.getValue().equalsIgnoreCase("Both")) {
            Render3DUtil.b(var1, var39, this.blocksOutlineColor.getColor());
         }
      }

      if (var25 != null && !this.n.contains(var25)) {
         if (this.entitiesMode.getValue().equalsIgnoreCase("Fill") || this.entitiesMode.getValue().equalsIgnoreCase("Both")) {
            Render3DUtil.a_void_PoseStack_AABB_Color(var1, var25.getBoundingBox(), this.entitiesFillColor.getColor());
         }

         if (this.entitiesMode.getValue().equalsIgnoreCase("Outline") || this.entitiesMode.getValue().equalsIgnoreCase("Both")) {
            Render3DUtil.b(var1, var25.getBoundingBox(), this.entitiesOutlineColor.getColor());
         }

         this.n.add(var25);
      }
   }

   private Entity a_Entity_AABB(AABB var1) {
      for (Entity var3 : (Iterable<Entity>)(Object)mc.level.entitiesForRendering()) {
         if (var3 != mc.player && !(var3 instanceof Arrow) && var3.getBoundingBox().intersects(var1)) {
            return var3;
         }
      }

      return null;
   }

   private float a_float_Item(Item var1) {
      if (var1 instanceof SplashPotionItem || var1 instanceof LingeringPotionItem) {
         return 0.5F;
      } else if (var1 instanceof ExperienceBottleItem) {
         return 0.59F;
      } else {
         return var1 instanceof TridentItem ? 2.0F : 1.5F;
      }
   }

   private int b(Item var1) {
      return !(var1 instanceof SplashPotionItem) && !(var1 instanceof LingeringPotionItem) && !(var1 instanceof ExperienceBottleItem) ? 0 : 20;
   }
}
