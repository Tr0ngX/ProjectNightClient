package night.module.impl.combat;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.events.EventTarget;
import night.events.impl.UpdateWalkingPlayerEvent;
import night.manager.ChatManager;
import night.module.Module;
import night.module.ModuleInfo;
import night.setting.impl.BooleanSetting;
import night.setting.impl.ModeSetting;
import night.setting.impl.NumberSetting;
import night.util.concurrent.ThreadManager;
import night.util.inventory.InventoryUtil;
import night.util.module.ModuleRegistryHelper;
import night.util.rotation.RotationMathUtil;
import night.util.world.BlockInteractionUtil;
import night.util.world.EntityUtil;
import night.util.world.HoleUtil;
import night.util.world.PacketAndRenderUtil;
import night.util.world.PositionUtil;

@ModuleInfo(
   name = "HoleFill",
   description = "Automatically places blocks inside of holes to prevent others from getting inside of them.",
   category = Module.Category.COMBAT,
   proxySupported = true
)
public class HoleFill extends Module {
   public ModeSetting switchSetting = new ModeSetting(
      "Switch", "The mode that will be used for automatically switching to necessary items.", "Silent", InventoryUtil.a
   );
   public ModeSetting mode;
   public BooleanSetting webs;
   public BooleanSetting selfWeb;
   public BooleanSetting refill;
   public BooleanSetting waitUntilExited;
   public BooleanSetting asynchronous;
   public NumberSetting bPT;
   public NumberSetting range;
   public NumberSetting enemyRange;
   public NumberSetting smartRange;
   public BooleanSetting safety;
   public NumberSetting safetyRange;
   public BooleanSetting rotate;
   public BooleanSetting timer;
   public BooleanSetting strictDirection;
   public BooleanSetting attack;
   public BooleanSetting attackRotate;
   public NumberSetting attackRange;
   public NumberSetting x;
   public BooleanSetting y;
   public BooleanSetting z;
   public BooleanSetting doubleHoles;
   public BooleanSetting holeCheck;
   public BooleanSetting whileEating;
   public BooleanSetting selfDisable;
   public BooleanSetting itemDisable;
   public BooleanSetting render;
   private List<BlockPos> G;
   private int H;
   private BlockPos I;
   private BlockPos J;
   private boolean K;

   public HoleFill() {
      String[] var1 = new String[]{"Normal", "Smart"};
      this.mode = new ModeSetting("Mode", "The offsets that will be used when trapping.", "Normal", var1);
      this.webs = new BooleanSetting("Webs", "Use webs to holefill instead of blocks.", false);
      this.selfWeb = new BooleanSetting("SelfWeb", "Includes your own hole when holefilling with webs.", false);
      this.refill = new BooleanSetting("Refill", "Fills the hole you just exited.", false);
      BooleanSetting var2 = this.refill;
      this.waitUntilExited = new BooleanSetting(
         "WaitUntilExited", "Waits until your hitbox completely leaves the hole before refilling.", new BooleanSetting.BooleanCondition(var2, true), true
      );
      this.asynchronous = new BooleanSetting("Asynchronous", "Performs calculations on separate threads.", true);
      Integer var3 = 4;
      Integer var4 = 1;
      Integer var5 = 8;
      this.bPT = new NumberSetting("BPT", "The maximum number of blocks to place per tick.", var3, var4, var5);
      Double var6 = 5.0;
      Double var7 = 0.0;
      Double var8 = 12.0;
      this.range = new NumberSetting("Range", "The maximum range at which the blocks will be placed at.", var6, var7, var8);
      ModeSetting var10 = this.mode;
      String[] var11 = new String[]{"Smart"};
      ModeSetting.ModeCondition var9 = new ModeSetting.ModeCondition(var10, var11);
      Float var12 = 8.0F;
      Float var13 = 0.0F;
      Float var14 = 16.0F;
      this.enemyRange = new NumberSetting("EnemyRange", "The maximum distance at which the target should be at.", var9, var12, var13, var14);
      ModeSetting var16 = this.mode;
      String[] var17 = new String[]{"Smart"};
      ModeSetting.ModeCondition var15 = new ModeSetting.ModeCondition(var16, var17);
      Float var18 = 3.0F;
      Float var19 = 0.0F;
      Float var20 = 6.0F;
      this.smartRange = new NumberSetting("SmartRange", "The distance at which the holes will have to be away from the target.", var15, var18, var19, var20);
      this.safety = new BooleanSetting("Safety", "Prevents holes close to you from getting filled.", true);
      BooleanSetting var22 = this.safety;
      BooleanSetting.BooleanCondition var21 = new BooleanSetting.BooleanCondition(var22, true);
      Float var23 = 2.0F;
      Float var24 = 0.0F;
      Float var25 = 6.0F;
      this.safetyRange = new NumberSetting(
         "SafetyRange", "The maximum distance the holes can be at to be prevented from getting filled.", var21, var23, var24, var25
      );
      this.rotate = new BooleanSetting("Rotate", "Sends a packet rotation whenever placing a block.", true);
      this.timer = new BooleanSetting("Timer", "Runs the game at 1.08x while enabled -- pays GrimAC Timer balance back between placement bursts.", false);
      this.strictDirection = new BooleanSetting("StrictDirection", "Only places using directions that face you.", false);
      this.attack = new BooleanSetting("Attack", "Attacks any crystal that interferes with hole filling.", false);
      BooleanSetting var26 = this.attack;
      this.attackRotate = new BooleanSetting(
         "AttackRotate", "Rotate", "Rotates toward the crystal before attacking it.", new BooleanSetting.BooleanCondition(var26, true), true
      );
      BooleanSetting var28 = this.attack;
      BooleanSetting.BooleanCondition var27 = new BooleanSetting.BooleanCondition(var28, true);
      Float var29 = 3.0F;
      Float var30 = 1.0F;
      Float var31 = 6.0F;
      this.attackRange = new NumberSetting("AttackRange", "Range", "The maximum range at which crystals will be attacked.", var27, var29, var30, var31);
      BooleanSetting var33 = this.attack;
      BooleanSetting.BooleanCondition var32 = new BooleanSetting.BooleanCondition(var33, true);
      Integer var34 = 5;
      Integer var35 = 0;
      Integer var36 = 20;
      this.x = new NumberSetting("AttackAge", "Age", "The minimum age (in ticks) a crystal must reach before being attacked.", var32, var34, var35, var36);
      BooleanSetting var37 = this.attack;
      this.y = new BooleanSetting(
         "AttackMultiTask", "Multitask", "Allows attacking while an item is already in use.", new BooleanSetting.BooleanCondition(var37, true), true
      );
      BooleanSetting var38 = this.attack;
      this.z = new BooleanSetting(
         "AttackSwing", "Swing", "Sends a swing packet whenever attacking a crystal.", new BooleanSetting.BooleanCondition(var38, true), true
      );
      this.doubleHoles = new BooleanSetting("DoubleHoles", "Whether or not to fill double holes.", false);
      this.holeCheck = new BooleanSetting("HoleCheck", "Checks if the target isn't in a hole before placing.", true);
      this.whileEating = new BooleanSetting("WhileEating", "Places blocks normally while eating.", true);
      this.selfDisable = new BooleanSetting("SelfDisable", "Toggles off the module once it is finished with placing.", false);
      this.itemDisable = new BooleanSetting("ItemDisable", "Toggles off the module whenever you run out of items to place with.", true);
      this.render = new BooleanSetting("Render", "Whether or not to render the place position.", true);
      this.G = new ArrayList<>();
      this.H = 0;
      this.I = null;
      this.J = null;
      this.K = false;
   }

   @Override
   public void onEnable() {
      if (this.timer.getValue()) {
         ModuleRegistryHelper.a(this);
      }

      this.I = null;
      this.J = null;
      this.K = false;
      if (!this.nullCheck() && mc.player != null) {
         BlockPos var1 = PositionUtil.a_BlockPos_Entity(mc.player);
         if (this.a_bool_BlockPos(var1)) {
            BlockPos var2 = var1.immutable();
            this.I = var2;
            this.K = true;
         }
      }
   }

   @EventTarget
   public void a_void_UpdateWalkingPlayerEvent(UpdateWalkingPlayerEvent var1) {
      if (!this.isProxyDelegated()) {
         if (this.whileEating.getValue() || !EntityUtil.a()) {
            List var2 = mc.level.players();
            Runnable var3 = () -> {
               this.H = 0;
               boolean var2x;
               if (this.webs.getValue()) {
                  if (!mc.player.getMainHandItem().getItem().equals(Items.COBWEB)) {
                     var2x = true;
                  } else {
                     var2x = false;
                  }
               } else if (!(mc.player.getMainHandItem().getItem() instanceof BlockItem)) {
                  var2x = true;
               } else {
                  var2x = false;
               }

               boolean var3x = var2x;
               if (this.switchSetting.getValue().equalsIgnoreCase("None") && var3x) {
                  if (this.itemDisable.getValue()) {
                     ChatManager var4 = Night.chatManager;
                     String var5;
                     if (this.webs.getValue()) {
                        var5 = "cobwebs.";
                     } else {
                        var5 = "blocks.";
                     }

                     String var51 = this.getName();
                     var4.sendTaggedMessage("You are currently not holding any " + var5, var51);
                     this.setEnabled(false);
                  }

                  this.G = new ArrayList<>();
               } else {
                  int var9;
                  if (this.webs.getValue()) {
                     Item var6 = Items.COBWEB;
                     byte var7;
                     if (!this.switchSetting.getValue().equalsIgnoreCase("AltSwap") && !this.switchSetting.getValue().equalsIgnoreCase("AltPickup")) {
                        var7 = 8;
                     } else {
                        var7 = 35;
                     }

                     var9 = InventoryUtil.a_int_Item_int_int(var6, 0, var7);
                  } else {
                     byte var8;
                     if (!this.switchSetting.getValue().equalsIgnoreCase("AltSwap") && !this.switchSetting.getValue().equalsIgnoreCase("AltPickup")) {
                        var8 = 8;
                     } else {
                        var8 = 35;
                     }

                     var9 = InventoryUtil.b(0, var8);
                  }

                  int var10 = mc.player.getInventory().getSelectedSlot();
                  if (var9 == -1) {
                     if (this.itemDisable.getValue()) {
                        ChatManager var11 = Night.chatManager;
                        String var12;
                        if (this.webs.getValue()) {
                           var12 = "cobwebs";
                        } else {
                           var12 = "blocks";
                        }

                        String var50 = this.getName();
                        var11.sendTaggedMessage("No " + var12 + " could be found in your hotbar.", var50);
                        this.setEnabled(false);
                     }

                     this.G = new ArrayList<>();
                  } else {
                     if (this.mode.getValue().equalsIgnoreCase("Smart")) {
                        HoleFill.FillCalculation var13 = this.a_FillCalculation_List(var2);
                        if (var13 == null) {
                           this.G = new ArrayList<>();
                        } else {
                           List var14 = var13.list();
                           this.G = var14;
                        }
                     } else {
                        List var15 = this.a_List_Player(null);
                        this.G = var15;
                     }

                     if (this.refill.getValue()) {
                        LocalPlayer var16 = mc.player;
                        this.a_void_LocalPlayer(var16);
                        if (this.J != null) {
                           if (!BlockInteractionUtil.c_bool_BlockPos(this.J)) {
                              this.J = null;
                           } else {
                              label118: {
                                 if (this.waitUntilExited.getValue()) {
                                    BlockPos var17 = this.J;
                                    LocalPlayer var18 = mc.player;
                                    if (this.a(var17, var18)) {
                                       break label118;
                                    }
                                 }

                                 LocalPlayer var19 = mc.player;
                                 Vec3 var20 = Vec3.atCenterOf(this.J);
                                 double var21 = var19.distanceToSqr(var20);
                                 double var23 = Mth.square(this.range.getValue().doubleValue());
                                 if (var21 <= var23) {
                                    List var25 = this.G;
                                    BlockPos var26 = this.J;
                                    if (!var25.contains(var26)) {
                                       List var27 = this.G;
                                       BlockPos var28 = this.J;
                                       var27.add(0, var28);
                                    }
                                 } else {
                                    this.J = null;
                                 }
                              }
                           }
                        }
                     } else {
                        this.J = null;
                        this.K = false;
                        this.I = null;
                     }

                     if (this.attack.getValue()) {
                        this.v();
                     }

                     if (this.G.isEmpty()) {
                        if (this.selfDisable.getValue() && !this.mode.getValue().equalsIgnoreCase("Smart")) {
                           this.setEnabled(false);
                        }
                     } else {
                        Night.rotationManager.beginBatch();

                        try {
                           String var29 = this.switchSetting.getValue();
                           int var30 = var9;
                           int var31 = var10;
                           InventoryUtil.a_bool_String_int_int(var29, var30, var31);

                           for (BlockPos var33 : (Iterable<BlockPos>)(Object)this.G) {
                              int var34 = this.H;
                              int var35 = this.bPT.getValue().intValue();
                              if (var34 >= var35) {
                                 break;
                              }

                              BlockPos var36 = var33;
                              boolean var37 = this.strictDirection.getValue();
                              Direction var38 = BlockInteractionUtil.b_Direction_BlockPos_bool(var36, var37);
                              if (var38 != null) {
                                 BlockPos var39 = var33;
                                 Direction var40 = var38;
                                 boolean var41 = this.rotate.getValue();
                                 boolean var42 = this.render.getValue();
                                 if (BlockInteractionUtil.a(var39, var40, InteractionHand.MAIN_HAND, var41, true, var42)) {
                                    int var43 = this.H + 1;
                                    this.H = var43;
                                    BlockPos var44 = var33;
                                    BlockPos var45 = this.J;
                                    if (var44.equals(var45)) {
                                       this.J = null;
                                    }
                                 }
                              }
                           }

                           String var46 = this.switchSetting.getValue();
                           int var47 = var9;
                           int var48 = var10;
                           InventoryUtil.b(var46, var47, var48);
                        } catch (Throwable var52) {
                           Night.rotationManager.endBatch();
                           throw var52;
                        }

                        Night.rotationManager.endBatch();
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

   private void v() {
      Night.rotationManager.beginBatch();
      ClientLevel var1 = mc.level;
      BlockPos var3 = mc.player.blockPosition();
      AABB var2 = new AABB(var3);
      double var4 = this.attackRange.getValue().doubleValue();
      double var6 = 3.0;
      double var8 = var4 + var6;
      AABB var10 = var2.inflate(var8);

      for (EndCrystal var12 : var1.getEntitiesOfClass(EndCrystal.class, var10)) {
         if (var12.isAlive() && !var12.isRemoved()) {
            int var13 = var12.tickCount;
            int var14 = this.x.getValue().intValue();
            if (var13 >= var14) {
               AABB var15 = var12.getBoundingBox();
               boolean var16 = false;

               for (BlockPos var18 : (Iterable<BlockPos>)(Object)this.G) {
                  ClientLevel var19 = mc.level;
                  BlockPos var20 = var18;
                  if (var19.getBlockState(var20).canBeReplaced()) {
                     BlockPos var21 = var18;
                     AABB var22 = var15;
                     if (new AABB(var21).intersects(var22)) {
                        var16 = true;
                        break;
                     }
                  }
               }

               if (var16) {
                  AABB var23 = var15;
                  Vec3 var24 = mc.player.getEyePosition();
                  double var25 = var23.distanceToSqr(var24);
                  double var27 = this.attackRange.getValue().doubleValue();
                  double var29 = this.attackRange.getValue().doubleValue();
                  double var31 = var27 * var29;
                  if (!(var25 > var31) && (this.y.getValue() || !mc.player.isUsingItem())) {
                     if (this.attackRotate.getValue()) {
                        float[] var33 = RotationMathUtil.a(var12.getBoundingBox().getCenter());
                        float var34 = var33[0];
                        float var35 = var33[1];
                        Night.rotationManager.sendOrBatchPosRot(var34, var35);
                        AABB var36 = var15;
                        Vec3 var37 = mc.player.getEyePosition();
                        if (!var36.contains(var37) && !BlockInteractionUtil.a_bool_Vec3(var12.getBoundingBox().getCenter())) {
                           continue;
                        }
                     }

                     ClientPacketListener var38 = mc.getConnection();
                     int var39 = var12.getId();
                     var38.send(new ServerboundAttackPacket(var39));
                     if (this.z.getValue()) {
                        ClientPacketListener var40 = mc.getConnection();
                        Packet var41 = PacketAndRenderUtil.a_Packet_InteractionHand(InteractionHand.MAIN_HAND);
                        var40.send(var41);
                     }
                  }
               }
            }
         }
      }

      Night.rotationManager.endBatch();
   }

   private void a_void_LocalPlayer(LocalPlayer var1) {
      BlockPos var2 = PositionUtil.a_BlockPos_Entity(var1);
      if (this.a_bool_BlockPos(var2)) {
         BlockPos var3 = var2.immutable();
         this.I = var3;
         this.K = true;
      } else if (this.K && this.I != null) {
         BlockPos var4 = var2;
         BlockPos var5 = this.I;
         if (!var4.equals(var5)) {
            BlockPos var6 = this.I.immutable();
            this.J = var6;
         }

         this.K = false;
      } else {
         this.K = false;
      }
   }

   private boolean a_bool_BlockPos(BlockPos var1) {
      if (mc.level != null && var1 != null) {
         double var2 = 1.0;
         if (HoleUtil.a_HoleInfo_BlockPos_double_bool(var1, var2, false) != null) {
            return true;
         }

         if (this.doubleHoles.getValue()) {
            double var4 = 1.0;
            if (HoleUtil.b(var1, var4) != null) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private boolean a(BlockPos var1, Player var2) {
      return var2.getBoundingBox().intersects(new AABB(var1));
   }

   @Override
   public void onDisable() {
      ModuleRegistryHelper.b(this);
      this.G = new ArrayList<>();
      this.I = null;
      this.J = null;
      this.K = false;
   }

   @Override
   public String getDisplayInfo() {
      return String.valueOf(this.G.size());
   }

   private HoleFill.FillCalculation a_FillCalculation_List(List<AbstractClientPlayer> var1) {
      HoleFill.FillCalculation var2 = null;

      for (Player var4 : var1) {
         LocalPlayer var5 = mc.player;
         if (var4 != var5 && var4.isAlive() && !(var4.getHealth() <= 0.0F)) {
            LocalPlayer var6 = mc.player;
            Player var7 = var4;
            double var8 = var6.distanceToSqr(var7);
            double var10 = Mth.square(this.enemyRange.getValue().doubleValue());
            if (!(var8 > var10)) {
               String var12 = var4.getName().getString();
               if (!Night.friendManager.isFriend(var12) && (!this.holeCheck.getValue() || !HoleUtil.a_bool_Player(var4))) {
                  Player var13 = var4;
                  List var14 = this.a_List_Player(var13);
                  if (!var14.isEmpty()) {
                     if (var2 == null) {
                        Player var15 = var4;
                        List var16 = var14;
                        var2 = new HoleFill.FillCalculation(var15, var16);
                     } else {
                        LocalPlayer var17 = mc.player;
                        Player var18 = var4;
                        double var19 = var17.distanceToSqr(var18);
                        LocalPlayer var21 = mc.player;
                        Player var22 = var2.player();
                        double var23 = var21.distanceToSqr(var22);
                        if (var19 < var23) {
                           Player var25 = var4;
                           List var26 = var14;
                           var2 = new HoleFill.FillCalculation(var25, var26);
                        }
                     }
                  }
               }
            }
         }
      }

      return var2;
   }

   private List<BlockPos> a_List_Player(Player var1) {
      ArrayList var2 = new ArrayList();
      int var3 = this.range.getValue().intValue();
      BlockPos var4 = mc.player.blockPosition();
      int var5 = -var3;
      int var6 = -var3;
      int var7 = -var3;
      BlockPos var8 = var4.offset(var5, var6, var7);
      BlockPos var9 = var4;
      int var10 = var3;
      int var11 = var3;
      int var12 = var3;
      BlockPos var13 = var9.offset(var10, var11, var12);

      for (BlockPos var15 : (Iterable<BlockPos>)(Object)BlockPos.betweenClosed(var8, var13)) {
         LocalPlayer var16 = mc.player;
         Vec3 var17 = Vec3.atCenterOf(var15);
         double var18 = var16.distanceToSqr(var17);
         double var20 = Mth.square(this.range.getValue().doubleValue());
         if (!(var18 > var20) && BlockInteractionUtil.c_bool_BlockPos(var15)) {
            if (this.safety.getValue()) {
               LocalPlayer var22 = mc.player;
               Vec3 var23 = Vec3.atCenterOf(var15);
               double var24 = var22.distanceToSqr(var23);
               double var26 = Mth.square(this.safetyRange.getValue().doubleValue());
               if (var24 <= var26) {
                  continue;
               }
            }

            if (this.selfWeb.getValue() && HoleUtil.a_bool_Player(mc.player)) {
               BlockPos var28 = mc.player.blockPosition();
               BlockPos var29 = var15;
               if (var28.equals(var29)) {
                  ArrayList var30 = var2;
                  BlockPos var31 = var15.immutable();
                  var30.add(var31);
                  continue;
               }
            }

            BlockPos var32 = var15;
            double var33 = 1.0;
            if (HoleUtil.a_HoleInfo_BlockPos_double(var32, var33) != null) {
               if (var1 != null) {
                  Vec3 var35 = Vec3.atCenterOf(var15);
                  double var36 = var1.distanceToSqr(var35);
                  double var38 = Mth.square(this.smartRange.getValue().doubleValue());
                  if (var36 > var38) {
                     continue;
                  }
               }

               ArrayList var50 = var2;
               BlockPos var51 = var15.immutable();
               var50.add(var51);
            } else if (this.doubleHoles.getValue()) {
               BlockPos var40 = var15;
               double var41 = 1.0;
               if (HoleUtil.b(var40, var41) != null) {
                  if (var1 != null) {
                     Vec3 var43 = Vec3.atCenterOf(var15);
                     double var44 = var1.distanceToSqr(var43);
                     double var46 = Mth.square(this.smartRange.getValue().doubleValue());
                     if (var44 > var46) {
                        continue;
                     }
                  }

                  ArrayList var48 = var2;
                  BlockPos var49 = var15.immutable();
                  var48.add(var49);
               }
            }
         }
      }

      return var2;
   }

   record FillCalculation(Player player, List<BlockPos> list) {
   }
}
