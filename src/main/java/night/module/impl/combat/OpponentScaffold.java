package night.module.impl.combat;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.events.EventTarget;
import night.events.impl.UpdateWalkingPlayerEvent;
import night.module.Module;
import night.module.ModuleInfo;
import night.module.impl.movement.HitboxDesync;
import night.setting.impl.BooleanSetting;
import night.setting.impl.ModeSetting;
import night.setting.impl.NumberSetting;
import night.util.concurrent.ThreadManager;
import night.util.inventory.InventoryUtil;
import night.util.world.BlockInteractionUtil;
import night.util.world.EntityUtil;
import night.util.world.PositionUtil;

@ModuleInfo(
   name = "OpponentScaffold",
   description = "Automatically places blocks under non-friended players to prevent them from falling.",
   category = Module.Category.COMBAT,
   proxySupported = true
)
public class OpponentScaffold extends Module {
   public ModeSetting switchSetting = new ModeSetting(
      "Switch", "The mode that will be used for automatically switching to necessary items.", "Silent", InventoryUtil.a
   );
   public BooleanSetting asynchronous = new BooleanSetting("Asynchronous", "Performs calculations on separate threads.", true);
   public NumberSetting limit;
   public NumberSetting delay;
   public NumberSetting range;
   public NumberSetting enemyRange;
   public NumberSetting extrapolation;
   public BooleanSetting await;
   public BooleanSetting rotate;
   public BooleanSetting strictDirection;
   public BooleanSetting crystalDestruction;
   public BooleanSetting whileEating;
   public BooleanSetting selfDisable;
   public BooleanSetting itemDisable;
   public BooleanSetting render;
   private List<BlockPos> t;
   private int u;
   private int v;

   public OpponentScaffold() {
      Integer var1 = 4;
      Integer var2 = 1;
      Integer var3 = 20;
      this.limit = new NumberSetting("Limit", "The number of blocks that can be placed per tick.", var1, var2, var3);
      Integer var4 = 0;
      Integer var5 = 0;
      Integer var6 = 20;
      this.delay = new NumberSetting("Delay", "The amount of ticks that have to be waited for between placements.", var4, var5, var6);
      Double var7 = 5.0;
      Double var8 = 0.0;
      Double var9 = 12.0;
      this.range = new NumberSetting("Range", "The maximum range at which blocks will be placed at.", var7, var8, var9);
      Float var10 = 10.0F;
      Float var11 = 0.0F;
      Float var12 = 20.0F;
      this.enemyRange = new NumberSetting("EnemyRange", "The maximum distance at which the target should be at.", var10, var11, var12);
      Integer var13 = 0;
      Integer var14 = 0;
      Integer var15 = 20;
      this.extrapolation = new NumberSetting("Extrapolation", "Extrapolates the target's position to predict where they will move.", var13, var14, var15);
      this.await = new BooleanSetting("Await", "Waits for blocks to be registered by the client before placing on them.", false);
      this.rotate = new BooleanSetting("Rotate", "Whether or not you should rotate when you place blocks.", true);
      this.strictDirection = new BooleanSetting("StrictDirection", "Only places using directions that face you.", false);
      this.crystalDestruction = new BooleanSetting("CrystalDestruction", "Destroys any crystals that interfere with block placement.", true);
      this.whileEating = new BooleanSetting("WhileEating", "Places blocks normally while eating.", true);
      this.selfDisable = new BooleanSetting("SelfDisable", "Toggles off the module once it is finished with placing.", false);
      this.itemDisable = new BooleanSetting("ItemDisable", "Toggles off the module whenever you run out of items to place with.", true);
      this.render = new BooleanSetting("Render", "Whether or not to render the place position.", true);
      this.t = new ArrayList<>();
      this.u = 0;
      this.v = 0;
   }

   @EventTarget
   public void a_void_UpdateWalkingPlayerEvent(UpdateWalkingPlayerEvent var1) {
      if (!this.isProxyDelegated()) {
         if (mc.player != null && mc.level != null) {
            if (this.whileEating.getValue() || !EntityUtil.a()) {
               List var2 = mc.level.players();
               if (var2 != null && !var2.isEmpty()) {
                  List var3 = var2;
                  Runnable var4 = () -> {
                     if (mc.player != null && mc.level != null) {
                        this.v = 0;
                        int var2x = this.u;
                        int var3x = this.delay.getValue().intValue();
                        if (var2x < var3x) {
                           int var4x = this.u + 1;
                           this.u = var4x;
                        } else if (!this.switchSetting.getValue().equalsIgnoreCase("None")
                           || mc.player.getMainHandItem() != null && mc.player.getMainHandItem().getItem() instanceof BlockItem) {
                           byte var6;
                           if (!this.switchSetting.getValue().equalsIgnoreCase("AltSwap") && !this.switchSetting.getValue().equalsIgnoreCase("AltPickup")) {
                              var6 = 8;
                           } else {
                              var6 = 35;
                           }

                           int var7 = InventoryUtil.b(0, var6);
                           int var8;
                           if (mc.player.getInventory() != null) {
                              var8 = mc.player.getInventory().getSelectedSlot();
                           } else {
                              var8 = 0;
                           }

                           byte var9 = (byte)var8;
                           if (var7 == -1) {
                              if (this.itemDisable.getValue()) {
                                 String var10 = this.getName();
                                 Night.chatManager.sendTaggedMessage("No blocks could be found in your hotbar.", var10);
                                 this.setEnabled(false);
                              }

                              this.t = new ArrayList<>();
                           } else {
                              OpponentScaffold.ScaffoldTarget var11 = this.a_ScaffoldTarget_List(var3);
                              if (var11 == null) {
                                 if (this.selfDisable.getValue()) {
                                    this.setEnabled(false);
                                 }

                                 this.t = new ArrayList<>();
                              } else {
                                 List var12 = var11.a();
                                 this.t = var12;
                                 Night.rotationManager.beginBatch();

                                 try {
                                    String var13 = this.switchSetting.getValue();
                                    int var14 = var7;
                                    byte var15 = var9;
                                    InventoryUtil.a_bool_String_int_int(var13, var14, var15);
                                    ArrayList var16 = new ArrayList();

                                    for (BlockPos var18 : (Iterable<BlockPos>)(Object)this.t) {
                                       int var19 = this.v;
                                       int var20 = this.limit.getValue().intValue();
                                       if (var19 >= var20) {
                                          break;
                                       }

                                       BlockPos var21 = var18;
                                       ArrayList var22 = var16;
                                       boolean var23 = this.strictDirection.getValue();
                                       Direction var24 = BlockInteractionUtil.b_Direction_BlockPos_List_bool(var21, var22, var23);
                                       if (var24 == null) {
                                          BlockPos var25 = var18.offset(0, -1, 0);
                                          if (!BlockInteractionUtil.c_bool_BlockPos(var25)) {
                                             continue;
                                          }

                                          BlockPos var26 = var25;
                                          ArrayList var27 = var16;
                                          boolean var28 = this.strictDirection.getValue();
                                          Direction var29 = BlockInteractionUtil.b_Direction_BlockPos_List_bool(var26, var27, var28);
                                          if (var29 == null) {
                                             continue;
                                          }

                                          BlockPos var30 = var25;
                                          Direction var31 = var29;
                                          boolean var32 = this.rotate.getValue();
                                          boolean var33 = this.crystalDestruction.getValue();
                                          boolean var34 = this.render.getValue();
                                          BlockInteractionUtil.a(var30, var31, InteractionHand.MAIN_HAND, var32, var33, var34);
                                          ArrayList var35 = var16;
                                          BlockPos var36 = var25;
                                          var35.add(var36);
                                          int var37 = this.v + 1;
                                          this.v = var37;
                                          int var38 = this.v;
                                          int var39 = this.limit.getValue().intValue();
                                          if (var38 >= var39) {
                                             break;
                                          }

                                          if (this.await.getValue()) {
                                             continue;
                                          }

                                          BlockPos var40 = var18;
                                          ArrayList var41 = var16;
                                          boolean var42 = this.strictDirection.getValue();
                                          var24 = BlockInteractionUtil.b_Direction_BlockPos_List_bool(var40, var41, var42);
                                          if (var24 == null) {
                                             continue;
                                          }
                                       }

                                       BlockPos var43 = var18;
                                       Direction var44 = var24;
                                       boolean var45 = this.rotate.getValue();
                                       boolean var46 = this.crystalDestruction.getValue();
                                       boolean var47 = this.render.getValue();
                                       BlockInteractionUtil.a(var43, var44, InteractionHand.MAIN_HAND, var45, var46, var47);
                                       ArrayList var48 = var16;
                                       BlockPos var49 = var18;
                                       var48.add(var49);
                                       int var50 = this.v + 1;
                                       this.v = var50;
                                    }

                                    String var51 = this.switchSetting.getValue();
                                    int var52 = var7;
                                    byte var53 = var9;
                                    InventoryUtil.b(var51, var52, var53);
                                 } catch (Throwable var55) {
                                    Night.rotationManager.endBatch();
                                    throw var55;
                                 }

                                 Night.rotationManager.endBatch();
                                 this.u = 0;
                              }
                           }
                        } else {
                           if (this.itemDisable.getValue()) {
                              String var5 = this.getName();
                              Night.chatManager.sendTaggedMessage("You are currently not holding any blocks.", var5);
                              this.setEnabled(false);
                           }

                           this.t = new ArrayList<>();
                        }
                     }
                  };
                  if (this.asynchronous.getValue()) {
                     ThreadManager.a(var4);
                  } else {
                     var4.run();
                  }
               }
            }
         }
      }
   }

   @Override
   public void onEnable() {
      if (mc.player == null || mc.level == null) {
         this.setEnabled(false);
      }
   }

   @Override
   public void onDisable() {
      this.t = new ArrayList<>();
   }

   @Override
   public String getDisplayInfo() {
      return String.valueOf(this.t.size());
   }

   private OpponentScaffold.ScaffoldTarget a_ScaffoldTarget_List(List<AbstractClientPlayer> var1) {
      if (mc.player == null) {
         return null;
      }

      AbstractClientPlayer var2 = null;
      double var3 = Double.MAX_VALUE;

      for (AbstractClientPlayer var6 : (Iterable<AbstractClientPlayer>)(Object)var1) {
         LocalPlayer var7 = mc.player;
         if (var6 != var7 && !var6.isInvisible()) {
            String var8 = var6.getName().getString();
            if (!Night.friendManager.isFriend(var8) && !(var6.getHealth() <= 0.0F)) {
               LocalPlayer var9 = mc.player;
               AbstractClientPlayer var10 = var6;
               double var11 = var9.distanceToSqr(var10);
               double var13 = Mth.square(this.enemyRange.getValue().floatValue());
               if (!(var11 > var13)) {
                  double var15 = var11;
                  double var17 = var3;
                  if (var15 < var17) {
                     var3 = var11;
                     var2 = var6;
                  }
               }
            }
         }
      }

      if (var2 == null) {
         return null;
      }

      AbstractClientPlayer var19 = var2;
      BlockPos var20 = this.a_BlockPos_Player(var19);
      if (var20 == null) {
         return null;
      }

      ArrayList var22;
      BlockPos var23;
      boolean var25;
      label53: {
         HitboxDesync var21 = Night.moduleManager.getModule(HitboxDesync.class);
         var22 = new ArrayList();
         var23 = var20.below();
         if (var21 != null && var21.isEnabled()) {
            Boolean var24 = var21.close.getValue();
            if (!Boolean.TRUE.equals(var24)) {
               var25 = true;
               break label53;
            }
         }

         var25 = false;
      }

      boolean var26 = var25;
      BlockPos var27 = var23;
      boolean var28 = var26;
      if (BlockInteractionUtil.a_bool_BlockPos_bool(var27, var28)) {
         LocalPlayer var29 = mc.player;
         Vec3 var30 = Vec3.atCenterOf(var23);
         double var31 = var29.distanceToSqr(var30);
         double var33 = Mth.square(this.range.getValue().doubleValue());
         if (var31 <= var33) {
            ArrayList var35 = var22;
            BlockPos var36 = var23;
            var35.add(var36);
         }
      }

      if (var22.isEmpty()) {
         return null;
      }

      AbstractClientPlayer var37 = var2;
      ArrayList var38 = var22;
      return new OpponentScaffold.ScaffoldTarget(var37, var38);
   }

   private BlockPos a_BlockPos_Player(Player var1) {
      if (var1 == null) {
         return null;
      } else {
         int var2 = this.extrapolation.getValue().intValue();
         if (var2 > 0) {
            double var3 = var1.getX();
            double var5 = var1.xo;
            double var7 = var3 - var5;
            double var9 = var1.getZ();
            double var11 = var1.zo;
            double var13 = var9 - var11;
            double var15 = var1.getX();
            double var17 = var7;
            double var19 = var2;
            double var21 = var17 * var19;
            double var23 = var15 + var21;
            double var25 = var1.getZ();
            double var27 = var13;
            double var29 = var2;
            double var31 = var27 * var29;
            double var33 = var25 + var31;
            int var35 = (int)Math.floor(var23);
            int var36 = (int)Math.floor(var1.getY());
            int var37 = (int)Math.floor(var33);
            return new BlockPos(var35, var36, var37);
         } else {
            return PositionUtil.a_BlockPos_Entity(var1);
         }
      }
   }

   record ScaffoldTarget(Player player, List<BlockPos> list) {
      public List<BlockPos> a() {
         return this.list;
      }
   }
}
