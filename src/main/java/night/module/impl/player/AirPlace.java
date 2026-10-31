package night.module.impl.player;

import com.mojang.blaze3d.vertex.PoseStack;
import java.awt.Color;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ScaffoldingBlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import night.Night;
import night.events.EventTarget;
import night.events.impl.Render3DEvent;
import night.events.impl.TickEvent;
import night.module.Module;
import night.module.ModuleInfo;
import night.module.impl.visuals.BlockHighlight;
import night.setting.impl.BooleanSetting;
import night.util.inventory.InventoryUtil;
import night.util.render.Render3DUtil;
import night.util.world.BlockInteractionUtil;

@ModuleInfo(name = "AirPlace", description = "Lets you place blocks in the air in servers that allow it.", category = Module.Category.PLAYER)
public class AirPlace extends Module {
   public BooleanSetting grim = new BooleanSetting(
      "Grim",
      "Swaps the block into your offhand for just the instant of each placement -- GrimAC's whole scaffolding check group (AirLiquidPlace, RotationPlace, PositionPlace...) only runs for MAIN_HAND placements, verified against its own decompiled source.",
      false
   );
   private HitResult f;
   private boolean g;

   @EventTarget
   public void a(TickEvent var1) {
      if (!this.nullCheck()) {
         Entity var2 = mc.getCameraEntity();
         double var3 = mc.player.blockInteractionRange();
         HitResult var5 = var2.pick(var3, 0.0F, false);
         this.f = var5;
         this.g = false;
         if (this.f instanceof BlockHitResult var7 && mc.player.getMainHandItem().getItem() instanceof BlockItem var9) {
            BlockHitResult var10 = var7;
            boolean var11 = this.a(var9, var10);
            this.g = var11;
            if (this.g) {
               if (mc.options.keyUse.isDown()) {
                  ClientLevel var12 = mc.level;
                  BlockPos var13 = var7.getBlockPos();
                  if (var12.getBlockState(var13).isAir()) {
                     if (this.grim.getValue()) {
                        int var14 = mc.player.getInventory().getSelectedSlot();
                        InventoryUtil.a(var14, 45);
                        BlockInteractionUtil.a_void_BlockPos_InteractionHand_String_bool(var7.getBlockPos(), InteractionHand.OFF_HAND, "None", true);
                        InventoryUtil.a(var14, 45);
                     } else {
                        BlockInteractionUtil.a_void_BlockPos_InteractionHand_String_bool(var7.getBlockPos(), InteractionHand.MAIN_HAND, "None", true);
                     }
                  }
               }
            }
         }
      }
   }

   @EventTarget
   public void a(Render3DEvent var1) {
      if (!this.nullCheck()) {
         BlockHighlight var2 = Night.moduleManager.getModule(BlockHighlight.class);
         if (this.g && this.f instanceof BlockHitResult var4 && mc.player.getMainHandItem().getItem() instanceof BlockItem) {
            ClientLevel var5 = mc.level;
            BlockPos var6 = var4.getBlockPos();
            if (var5.getBlockState(var6).isAir()) {
               BlockPos var7 = var4.getBlockPos();
               AABB var8 = new AABB(var7);
               PoseStack var9 = var1.getMatrices();
               AABB var10 = var8;
               Color var11 = var2.fillColor.getColor();
               Render3DUtil.a_void_PoseStack_AABB_Color(var9, var10, var11);
               PoseStack var12 = var1.getMatrices();
               AABB var13 = var8;
               Color var14 = var2.outlineColor.getColor();
               Render3DUtil.b(var12, var13, var14);
            }
         }
      }
   }

   private boolean a(BlockItem var1, BlockHitResult var2) {
      LocalPlayer var3 = mc.player;
      ItemStack var4 = mc.player.getMainHandItem();
      BlockPlaceContext var5 = new BlockPlaceContext(var3, InteractionHand.MAIN_HAND, var4, var2);
      Block var6 = var1.getBlock();
      BlockPlaceContext var7 = var5;
      BlockState var8 = var6.getStateForPlacement(var7);
      if (var8 == null) {
         return false;
      }

      boolean var9;
      if (!(var1 instanceof ScaffoldingBlockItem)) {
         var9 = true;
      } else {
         var9 = false;
      }

      if (var9) {
         BlockState var10 = var8;
         ClientLevel var11 = mc.level;
         BlockPos var12 = var5.getClickedPos();
         if (!var10.canSurvive(var11, var12)) {
            return false;
         }
      }

      ClientLevel var13 = mc.level;
      BlockState var14 = var8;
      BlockPos var15 = var5.getClickedPos();
      CollisionContext var16 = CollisionContext.placementContext(mc.player);
      return var13.isUnobstructed(var14, var15, var16);
   }
}
