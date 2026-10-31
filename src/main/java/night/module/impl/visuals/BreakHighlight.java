package night.module.impl.visuals;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Generated;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import night.events.EventTarget;
import night.events.impl.BlockBreakProgressEvent;
import night.events.impl.Render3DEvent;
import night.module.Module;
import night.module.ModuleInfo;
import night.setting.impl.BooleanSetting;
import night.setting.impl.ColorSetting;
import night.setting.impl.ModeSetting;
import night.setting.impl.NumberSetting;
import night.util.animation.EasingUtil;
import night.util.color.ColorUtil;
import night.util.render.Render3DUtil;
import night.util.world.BlockInteractionUtil;
import night.util.world.EntityUtil;

@ModuleInfo(name = "BreakHighlight", description = "Renders blocks that are being mined by other players.", category = Module.Category.VISUALS)
public class BreakHighlight extends Module {
   public ModeSetting mode = new ModeSetting("Mode", "The rendering that will be applied to the mine esp.", "Outline", new String[]{"Fill", "Outline", "Both"});
   public ColorSetting fillColor = new ColorSetting(
      "FillColor", "The color used for the fill rendering.", new ModeSetting.ModeCondition(this.mode, "Fill", "Both"), ColorUtil.b()
   );
   public ColorSetting outlineColor = new ColorSetting(
      "OutlineColor", "The color used for the outline rendering.", new ModeSetting.ModeCondition(this.mode, "Outline", "Both"), ColorUtil.c()
   );
   public BooleanSetting name2 = new BooleanSetting("Name", "Renders the name of the player mining the block.", true);
   public ColorSetting nameColor = new ColorSetting(
      "NameColor",
      "The color used for the name text rendering.",
      new BooleanSetting.BooleanCondition(this.name2, true),
      new ColorSetting.ColorValue(Color.WHITE, false, false)
   );
   public BooleanSetting glow = new BooleanSetting(
      "Glow", "Renders a glow shader effect around the breaker name text.", new BooleanSetting.BooleanCondition(this.name2, true), false
   );
   public NumberSetting scale = new NumberSetting("Scale", "The scale of the name text.", new BooleanSetting.BooleanCondition(this.name2, true), 30, 10, 100);
   public NumberSetting assume = new NumberSetting("Assume", "Assumed mining speed of other players (0.7 = breaks at 70% of full break time).", 1.0, 0.7, 1.0);
   private static final int m = 2;
   private final Map<Integer, List<BreakHighlight.BreakingBlockEntry>> n = new HashMap<>();

   @EventTarget
   public void a_void_BlockBreakProgressEvent(BlockBreakProgressEvent var1) {
      if (!this.nullCheck() && !EntityUtil.a_bool_int(var1.getBreakerId())) {
         if (mc.level != null) {
            Block var2 = mc.level.getBlockState(var1.getPos()).getBlock();
            if (!var2.equals(Blocks.AIR)) {
               List var3 = this.n.computeIfAbsent(var1.getBreakerId(), var0 -> new ArrayList<>());

               for (BreakHighlight.BreakingBlockEntry var5 : (Iterable<BreakHighlight.BreakingBlockEntry>)(Object)var3) {
                  if (var5.a.equals(var1.getPos())) {
                     return;
                  }
               }

               Entity var7 = mc.level.getEntity(var1.getBreakerId());
               String var8 = var7 instanceof Player var6 ? var6.getName().getString() : "Player";
               float var9 = var7 instanceof Player
                  ? BlockInteractionUtil.a_float_Player_BlockState((Player)var7, mc.level.getBlockState(var1.getPos()))
                  : 1000.0F;
               if (var3.size() >= 2) {
                  var3.remove(0);
               }

               var3.add(new BreakHighlight.BreakingBlockEntry(var1.getPos(), var2, var9, System.currentTimeMillis(), var8));
            }
         }
      }
   }

   @EventTarget
   public void a_void_Render3DEvent(Render3DEvent var1) {
      if (!this.nullCheck() && !this.n.isEmpty()) {
         this.n.entrySet().removeIf(var1x -> {
            var1x.getValue().removeIf(var2 -> this.a_bool_int_BreakingBlockEntry(var1x.getKey(), var2));
            return var1x.getValue().isEmpty();
         });
         this.n
            .forEach(
               (var2, var3) -> var3.forEach(
                  var2x -> {
                     float var3x = EasingUtil.a(var2x.d, (int)(var2x.c * this.assume.getValue().floatValue()));
                     AABB var4 = new AABB(var2x.a).deflate(0.5).inflate(var3x / 2.0);
                     if (this.mode.getValue().equalsIgnoreCase("Fill") || this.mode.getValue().equalsIgnoreCase("Both")) {
                        Render3DUtil.a_void_PoseStack_AABB_Color(var1.getMatrices(), var4, this.fillColor.getColor());
                     }

                     if (this.mode.getValue().equalsIgnoreCase("Outline") || this.mode.getValue().equalsIgnoreCase("Both")) {
                        Render3DUtil.b(var1.getMatrices(), var4, this.outlineColor.getColor());
                     }

                     if (this.name2.getValue() && var2x.e != null) {
                        Vec3 var5 = Vec3.atCenterOf(var2x.a);
                        int var6 = Math.min(100, Math.max(0, (int)(var3x * 100.0F)));
                        Render3DUtil.b(
                           var1.getMatrices(),
                           var2x.e + "\n" + var6 + "%",
                           var5.x,
                           var5.y,
                           var5.z,
                           this.scale.getValue().intValue(),
                           false,
                           this.nameColor.getColor(),
                           this.glow.getValue()
                        );
                     }
                  }
               )
            );
      }
   }

   private boolean a_bool_int_BreakingBlockEntry(int var1, BreakHighlight.BreakingBlockEntry var2) {
      if (mc.level == null) {
         return true;
      }

      if (!mc.level.getBlockState(var2.a).getBlock().equals(var2.b)) {
         return true;
      }

      if ((float)(System.currentTimeMillis() - var2.d) > var2.c + 3000.0F) {
         return true;
      }

      Entity var3 = mc.level.getEntity(var1);
      return var3 != null && Math.sqrt(var3.distanceToSqr(Vec3.atCenterOf(var2.a))) > 8.0;
   }

   static class BreakingBlockEntry {
      final BlockPos a;
      final Block b;
      final float c;
      final long d;
      final String e;

      @Generated
      public BreakingBlockEntry(BlockPos var1, Block var2, float var3, long var4, String var6) {
         this.a = var1;
         this.b = var2;
         this.c = var3;
         this.d = var4;
         this.e = var6;
      }
   }
}
