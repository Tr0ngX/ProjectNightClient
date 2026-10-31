package night.module.impl.combat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.events.EventTarget;
import night.events.impl.UpdateWalkingPlayerEvent;
import night.module.Module;
import night.module.ModuleInfo;
import night.setting.impl.BooleanSetting;
import night.setting.impl.ModeSetting;
import night.setting.impl.NumberSetting;
import night.util.inventory.InventoryUtil;
import night.util.world.BlockInteractionUtil;
import night.util.world.EntityUtil;
import night.util.world.PacketAndRenderUtil;

@ModuleInfo(
   name = "CombatPlace",
   description = "Automatically places blocks in front of moving targets to block them.",
   category = Module.Category.COMBAT,
   proxySupported = true
)
public class CombatPlace extends Module {
   public BooleanSetting flatten = new BooleanSetting("Flatten", "Place blocks under the target.", true);
   public ModeSetting mode;
   public ModeSetting priority;
   public NumberSetting predictTicks;
   public NumberSetting minKMH;
   public NumberSetting limit;
   public NumberSetting delay;
   public BooleanSetting strict;
   public BooleanSetting safety;
   public NumberSetting range;
   public BooleanSetting rotate;
   public BooleanSetting crystalDestruction;
   private int q;

   public CombatPlace() {
      String[] var1 = new String[]{"None", "One", "Two", "Three"};
      this.mode = new ModeSetting("Mode", "Prediction mode.", "Two", var1);
      String[] var2 = new String[]{"Closest", "Health"};
      this.priority = new ModeSetting("Priority", "Target selection priority.", "Closest", var2);
      Integer var3 = 2;
      Integer var4 = 1;
      Integer var5 = 8;
      this.predictTicks = new NumberSetting("PredictTicks", "Prediction scale.", var3, var4, var5);
      Integer var6 = 20;
      Integer var7 = 1;
      Integer var8 = 40;
      this.minKMH = new NumberSetting("MinKMH", "Min speed to trigger blocker.", var6, var7, var8);
      Integer var9 = 4;
      Integer var10 = 1;
      Integer var11 = 20;
      this.limit = new NumberSetting("Limit", "The maximum number of blocks that can be placed per tick.", var9, var10, var11);
      Integer var12 = 0;
      Integer var13 = 0;
      Integer var14 = 20;
      this.delay = new NumberSetting("Delay", "The delay in ticks between placements.", var12, var13, var14);
      this.strict = new BooleanSetting("Strict", "Prevent placement if entities intersect.", true);
      this.safety = new BooleanSetting("Safety", "Prevent trapping yourself.", true);
      Double var15 = 5.0;
      Double var16 = 0.0;
      Double var17 = 12.0;
      this.range = new NumberSetting("Range", "The maximum range at which blocks will be placed.", var15, var16, var17);
      this.rotate = new BooleanSetting("Rotate", "Sends a packet rotation whenever placing a block.", true);
      this.crystalDestruction = new BooleanSetting("CrystalDestruction", "Destroys any crystals that interfere with block placement.", true);
      this.q = 0;
   }

   @Override
   public void onEnable() {
      this.q = 0;
   }

   @EventTarget
   public void a_void_UpdateWalkingPlayerEvent(UpdateWalkingPlayerEvent var1) {
      if (!this.isProxyDelegated()) {
         if (!this.nullCheck()) {
            Player var2 = this.v();
            if (var2 != null) {
               int var3 = this.q;
               int var4 = this.delay.getValue().intValue();
               if (var3 < var4) {
                  int var5 = this.q + 1;
                  this.q = var5;
               } else {
                  Player var6 = var2;
                  List var7 = this.a_List_Player(var6);
                  if (!var7.isEmpty()) {
                     double var8 = this.range.getValue().doubleValue();
                     double var10 = 2.0;
                     double var12 = Math.pow(var8, var10);
                     var7.removeIf(var2x -> {
                        Vec3 var3x = mc.player.getEyePosition();
                        Vec3 var4x = Vec3.atCenterOf((BlockPos)var2x);
                        double var5x = var3x.distanceToSqr(var4x);
                        double var7x = var12;
                        boolean var9;
                        if (var5x > var7x) {
                           var9 = true;
                        } else {
                           var9 = false;
                        }

                        return var9;
                     });
                     if (!var7.isEmpty()) {
                        List var14 = var7;
                        this.a_void_List(var14);
                        this.q = 0;
                     }
                  }
               }
            }
         }
      }
   }

   private void a_void_List(List<BlockPos> var1) {
      if (mc.player != null) {
         int var2 = InventoryUtil.b(0, 8);
         if (var2 != -1) {
            int var3 = mc.player.getInventory().getSelectedSlot();
            int var4 = var2;
            int var5 = var3;
            InventoryUtil.a_bool_String_int_int("Silent", var4, var5);
            int var6 = 0;

            for (BlockPos var8 : var1) {
               int var9 = var6;
               int var10 = this.limit.getValue().intValue();
               if (var9 >= var10) {
                  break;
               }

               Direction var11 = BlockInteractionUtil.b_Direction_BlockPos_bool(var8, false);
               if (var11 != null) {
                  BlockPos var12 = var8;
                  Direction var13 = var11;
                  boolean var14 = this.rotate.getValue();
                  boolean var15 = this.crystalDestruction.getValue();
                  BlockInteractionUtil.a(var12, var13, InteractionHand.MAIN_HAND, var14, var15, false);
                  var6++;
               }
            }

            int var16 = var2;
            int var17 = var3;
            InventoryUtil.b("Silent", var16, var17);
         }
      }
   }

   private List<BlockPos> a_List_Player(Player var1) {
      ArrayList var2 = new ArrayList();
      if (this.flatten.getValue()) {
         BlockPos var3 = BlockPos.containing(var1.position()).below();
         if (this.a_bool_BlockPos(var3)) {
            ArrayList var4 = var2;
            BlockPos var5 = var3;
            var4.add(var5);
         }
      }

      String var6 = this.mode.getValue();
      if (!var6.equals("None")) {
         double var7 = EntityUtil.a_double_Entity_SpeedUnit(var1, EntityUtil.SpeedUnit.KILOMETERS);
         double var9 = this.minKMH.getValue().doubleValue();
         if (var7 >= var9) {
            Vec3 var11 = PacketAndRenderUtil.a_Vec3_Entity(var1);
            double var12 = var11.x;
            double var14 = var11.z;
            double var16 = var12;
            double var18 = var1.getX();
            double var20 = var16 - var18;
            double var22 = var14;
            double var24 = var1.getZ();
            double var26 = var22 - var24;
            double var28 = var20;
            double var30 = var20;
            double var32 = var28 * var30;
            double var34 = var26;
            double var36 = var26;
            double var38 = var34 * var36;
            double var40 = Math.sqrt(var32 + var38);
            double var42 = 0.001;
            if (var40 > var42) {
               double var44 = var20;
               double var46 = var40;
               double var48 = var44 / var46;
               double var50 = var26;
               double var52 = var40;
               double var54 = var50 / var52;
               double var56 = var1.getX();
               double var58 = var1.xo;
               double var60 = var56 - var58;
               double var62 = 2.0;
               double var64 = Math.pow(var60, var62);
               double var66 = var1.getZ();
               double var68 = var1.zo;
               double var70 = var66 - var68;
               double var72 = 2.0;
               double var74 = Math.pow(var70, var72);
               double var76 = Math.sqrt(var64 + var74);
               double var78 = this.predictTicks.getValue().doubleValue();
               double var80 = var12;
               double var82 = var48;
               double var84 = var76;
               double var86 = var82 * var84;
               double var88 = var78;
               double var90 = var86 * var88;
               double var92 = var80 + var90;
               double var94 = var1.getY();
               double var96 = var14;
               double var98 = var54;
               double var100 = var76;
               double var102 = var98 * var100;
               double var104 = var78;
               double var106 = var102 * var104;
               double var108 = var96 + var106;
               BlockPos var110 = BlockPos.containing(var92, var94, var108);
               if (this.a_bool_BlockPos(var110)) {
                  ArrayList var111 = var2;
                  BlockPos var112 = var110;
                  var111.add(var112);
                  if (var6.equals("Two") || var6.equals("Three")) {
                     BlockPos var113 = var110.above();
                     if (this.a_bool_BlockPos(var113)) {
                        ArrayList var114 = var2;
                        BlockPos var115 = var113;
                        var114.add(var115);
                     }
                  }

                  if (var6.equals("Three")) {
                     BlockPos var116 = var110.above(2);
                     if (this.a_bool_BlockPos(var116)) {
                        ArrayList var117 = var2;
                        BlockPos var118 = var116;
                        var117.add(var118);
                     }
                  }
               }
            }
         }
      }

      return var2;
   }

   private boolean a_bool_BlockPos(BlockPos var1) {
      if (mc.level == null) {
         return false;
      }

      if (mc.level.isOutsideBuildHeight(var1)) {
         return false;
      }

      if (!mc.level.getBlockState(var1).canBeReplaced()) {
         return false;
      }

      if (this.strict.getValue()) {
         AABB var2 = new AABB(var1);
         ClientLevel var3 = mc.level;
         Entity var4 = (Entity)null;
         AABB var5 = var2;
         if (var3.getEntities(var4, var5, var0 -> true).stream().anyMatch(var0 -> var0 instanceof Player)) {
            return false;
         }
      }

      return !this.safety.getValue() || mc.player == null || !mc.player.getBoundingBox().intersects(new AABB(var1));
   }

   private Player v() {
      return mc.level != null && mc.player != null ? mc.level.players().stream().filter(var0 -> {
         LocalPlayer var1 = mc.player;
         if (var0 != var1 && !var0.isRemoved()) {
            String var2 = var0.getName().getString();
            if (!Night.friendManager.isFriend(var2)) {
               return true;
            }
         }

         return false;
      }).filter(var1 -> {
         double var2 = mc.player.distanceTo(var1);
         double var4 = this.range.getValue().doubleValue();
         double var6 = 4.0;
         double var8 = var4 + var6;
         boolean var10;
         if (var2 <= var8) {
            var10 = true;
         } else {
            var10 = false;
         }

         return var10;
      }).min(this.w()).orElse(null) : null;
   }

   private Comparator<Player> w() {
      return this.priority.getValue().equals("Health") ? Comparator.comparingDouble(var0 -> {
         float var1 = var0.getHealth();
         float var2 = var0.getAbsorptionAmount();
         return var1 + var2;
      }) : Comparator.comparingDouble(var0 -> {
         double var1;
         if (mc.player != null) {
            var1 = mc.player.distanceTo(var0);
         } else {
            var1 = 0.0;
         }

         return var1;
      });
   }
}
