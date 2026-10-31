package night.module.impl.combat;

import java.util.List;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.events.EventTarget;
import night.events.impl.RotationUpdateEvent;
import night.events.impl.UpdateWalkingPlayerEvent;
import night.module.Module;
import night.module.ModuleInfo;
import night.setting.impl.BooleanSetting;
import night.setting.impl.ModeSetting;
import night.setting.impl.NumberSetting;
import night.util.concurrent.ThreadManager;
import night.util.inventory.InventoryUtil;
import night.util.rotation.RotationMathUtil;
import night.util.world.BlockInteractionUtil;
import night.util.world.EntityUtil;
import night.util.world.HoleUtil;
import night.util.world.PositionUtil;

@ModuleInfo(name = "AutoWeb", description = "Automatically places webs on other people's feet.", category = Module.Category.COMBAT, proxySupported = true)
public class AutoWeb extends Module {
   public ModeSetting switchSetting = new ModeSetting(
      "Switch", "The mode that will be used for automatically switching to necessary items.", "Silent", InventoryUtil.a
   );
   public BooleanSetting asynchronous = new BooleanSetting("Asynchronous", "Performs calculations on separate threads.", true);
   public NumberSetting delay;
   public NumberSetting range;
   public NumberSetting enemyRange;
   public NumberSetting extrapolation;
   public BooleanSetting rotate;
   public BooleanSetting airPlace;
   public BooleanSetting strictDirection;
   public BooleanSetting holeCheck;
   public BooleanSetting whileEating;
   public BooleanSetting selfDisable;
   public BooleanSetting itemDisable;
   public BooleanSetting render;
   private Player s;
   private BlockPos t;
   private int u;

   public AutoWeb() {
      Integer var1 = 0;
      Integer var2 = 0;
      Integer var3 = 20;
      this.delay = new NumberSetting("Delay", "The amount of ticks that have to be waited for between placements.", var1, var2, var3);
      Double var4 = 5.0;
      Double var5 = 0.0;
      Double var6 = 12.0;
      this.range = new NumberSetting("Range", "The maximum range at which the blocks will be placed at.", var4, var5, var6);
      Float var7 = 8.0F;
      Float var8 = 0.0F;
      Float var9 = 16.0F;
      this.enemyRange = new NumberSetting("EnemyRange", "The maximum distance at which the target should be at.", var7, var8, var9);
      Integer var10 = 0;
      Integer var11 = 0;
      Integer var12 = 20;
      this.extrapolation = new NumberSetting("Extrapolation", "Extrapolates the target's position to calculate positions ahead of time.", var10, var11, var12);
      this.rotate = new BooleanSetting("Rotate", "Sends a packet rotation whenever placing a block.", true);
      this.airPlace = new BooleanSetting("AirPlace", "Lets you place webs on air.", false);
      this.strictDirection = new BooleanSetting("StrictDirection", "Only places using directions that face you.", false);
      this.holeCheck = new BooleanSetting("HoleCheck", "Checks if the target is in a hole or not before placing.", true);
      this.whileEating = new BooleanSetting("WhileEating", "Places blocks normally while eating.", true);
      this.selfDisable = new BooleanSetting("SelfDisable", "Toggles off the module once it is finished with placing.", false);
      this.itemDisable = new BooleanSetting("ItemDisable", "Toggles off the module whenever you run out of items to place with.", true);
      this.render = new BooleanSetting("Render", "Whether or not to render the place position.", true);
      this.s = null;
      this.t = null;
      this.u = 0;
   }

   @EventTarget
   public void a_void_UpdateWalkingPlayerEvent(UpdateWalkingPlayerEvent var1) {
      if (!this.isProxyDelegated()) {
         if (this.whileEating.getValue() || !EntityUtil.a()) {
            List var2 = mc.level.players();
            Runnable var3 = () -> {
               this.t = null;
               int var2x = this.u;
               int var3x = this.delay.getValue().intValue();
               if (var2x < var3x) {
                  int var4 = this.u + 1;
                  this.u = var4;
               } else if (this.switchSetting.getValue().equalsIgnoreCase("None") && mc.player.getMainHandItem().getItem() != Items.COBWEB) {
                  if (this.itemDisable.getValue()) {
                     String var5 = this.getName();
                     Night.chatManager.sendTaggedMessage("You are currently not holding any cobwebs.", var5);
                     this.setEnabled(false);
                  }

                  this.s = null;
               } else {
                  Item var6 = Items.COBWEB;
                  byte var7;
                  if (!this.switchSetting.getValue().equalsIgnoreCase("AltSwap") && !this.switchSetting.getValue().equalsIgnoreCase("AltPickup")) {
                     var7 = 8;
                  } else {
                     var7 = 35;
                  }

                  int var8 = InventoryUtil.a_int_Item_int_int(var6, 0, var7);
                  int var9 = mc.player.getInventory().getSelectedSlot();
                  if (var8 == -1) {
                     if (this.itemDisable.getValue()) {
                        String var10 = this.getName();
                        Night.chatManager.sendTaggedMessage("No cobwebs could be found in your hotbar.", var10);
                        this.setEnabled(false);
                     }

                     this.s = null;
                  } else {
                     Player var11 = this.a_Player_List(var2);
                     this.s = var11;
                     if (this.s == null) {
                        if (this.selfDisable.getValue()) {
                           this.setEnabled(false);
                        }
                     } else {
                        Player var12 = this.s;
                        int var13 = this.extrapolation.getValue().intValue();
                        Vec3 var14 = PositionUtil.a(var12, var13).getCenter();
                        int var15 = (int)Math.floor(var14.x);
                        int var16 = (int)var14.y;
                        int var17 = (int)Math.floor(var14.z);
                        BlockPos var18 = new BlockPos(var15, var16, var17);
                        if (this.s.getItemBySlot(EquipmentSlot.CHEST).getItem().equals(Items.ELYTRA)) {
                           ClientLevel var19 = mc.level;
                           BlockPos var20 = var18;
                           if (var19.getBlockState(var20).getBlock().equals(Blocks.COBWEB)) {
                              var18 = var18.above();
                           }
                        }

                        ClientLevel var21 = mc.level;
                        BlockPos var22 = var18;
                        if (var21.getBlockState(var22).canBeReplaced()) {
                           LocalPlayer var23 = mc.player;
                           Vec3 var24 = Vec3.atCenterOf(var18);
                           double var25 = var23.distanceToSqr(var24);
                           double var27 = Mth.square(this.range.getValue().doubleValue());
                           if (!(var25 > var27)) {
                              BlockPos var29 = PositionUtil.a_BlockPos_Entity(mc.player);
                              BlockPos var30 = var18;
                              if (!var29.equals(var30) || !HoleUtil.a_bool_Player(mc.player)) {
                                 BlockPos var31 = var18;
                                 boolean var32 = this.strictDirection.getValue();
                                 Direction var33 = BlockInteractionUtil.b_Direction_BlockPos_bool(var31, var32);
                                 if (var33 != null || this.airPlace.getValue()) {
                                    if (this.rotate.getValue() && this.airPlace.getValue()) {
                                       BlockPos var34 = var18;
                                       this.t = var34;
                                    }

                                    String var35 = this.switchSetting.getValue();
                                    int var36 = var8;
                                    int var37 = var9;
                                    InventoryUtil.a_bool_String_int_int(var35, var36, var37);
                                    BlockPos var38 = var18;
                                    Direction var39 = var33;
                                    boolean var40 = this.rotate.getValue();
                                    boolean var41 = this.render.getValue();
                                    BlockInteractionUtil.a(var38, var39, InteractionHand.MAIN_HAND, var40, false, var41);
                                    String var42 = this.switchSetting.getValue();
                                    int var43 = var8;
                                    int var44 = var9;
                                    InventoryUtil.b(var42, var43, var44);
                                    this.u = 0;
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            };
            if (this.asynchronous.getValue()) {
               ThreadManager.a(var3);
            } else {
               var3.run();
            }
         }
      }
   }

   @EventTarget
   public void a_void_RotationUpdateEvent(RotationUpdateEvent var1) {
      if (this.t != null && !var1.isCancelled()) {
         float[] var2 = RotationMathUtil.a(Vec3.atCenterOf(this.t));
         float var3 = var2[0];
         var1.setYaw(var3);
         float var4 = var2[1];
         var1.setPitch(var4);
      }
   }

   @Override
   public void onEnable() {
      if (mc.player == null || mc.level == null) {
         this.setEnabled(false);
      }
   }

   @Override
   public String getDisplayInfo() {
      return this.s == null ? "None" : this.s.getName().getString();
   }

   private Player a_Player_List(List<AbstractClientPlayer> var1) {
      Player var2 = null;

      for (Player var4 : (Iterable<Player>)(Object)var1) {
         LocalPlayer var5 = mc.player;
         if (var4 != var5 && var4.isAlive() && !(var4.getHealth() <= 0.0F)) {
            LocalPlayer var6 = mc.player;
            Player var7 = var4;
            double var8 = var6.distanceToSqr(var7);
            double var10 = Mth.square(this.enemyRange.getValue().doubleValue());
            if (!(var8 > var10)) {
               String var12 = var4.getName().getString();
               if (!Night.friendManager.isFriend(var12) && (!this.holeCheck.getValue() || HoleUtil.a_bool_Player(var4))) {
                  if (var2 == null) {
                     var2 = var4;
                  } else {
                     LocalPlayer var13 = mc.player;
                     Player var14 = var4;
                     double var15 = var13.distanceToSqr(var14);
                     LocalPlayer var17 = mc.player;
                     Player var18 = var2;
                     double var19 = var17.distanceToSqr(var18);
                     if (var15 < var19) {
                        var2 = var4;
                     }
                  }
               }
            }
         }
      }

      return var2;
   }
}
