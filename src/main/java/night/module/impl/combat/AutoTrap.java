package night.module.impl.combat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.events.EventTarget;
import night.events.impl.PacketReceiveEvent;
import night.events.impl.UpdateWalkingPlayerEvent;
import night.module.Module;
import night.module.ModuleInfo;
import night.module.impl.visuals.LogoutSpot;
import night.module.impl.visuals.PopChams;
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

@ModuleInfo(
   name = "AutoTrap",
   description = "Automatically places blocks around you to prevent other people from getting inside your hole.",
   category = Module.Category.COMBAT
)
public class AutoTrap extends Module {
   public ModeSetting switchSetting = new ModeSetting(
      "Switch", "The mode that will be used for automatically switching to necessary items.", "Silent", InventoryUtil.a
   );
   public ModeSetting mode;
   public BooleanSetting head;
   public BooleanSetting noFeet;
   public BooleanSetting disableAfterFeet;
   public BooleanSetting asynchronous;
   public NumberSetting bPT;
   public NumberSetting range;
   public NumberSetting enemyRange;
   public BooleanSetting await;
   public BooleanSetting rotate;
   public BooleanSetting timer;
   public BooleanSetting strictDirection;
   public BooleanSetting airPlace;
   public BooleanSetting attack;
   public BooleanSetting attackRotate;
   public NumberSetting attackRange;
   public NumberSetting attackAge;
   public BooleanSetting attackMultiTask;
   public BooleanSetting x;
   public BooleanSetting y;
   public BooleanSetting z;
   public BooleanSetting whileEating;
   public BooleanSetting ignoreNaked;
   public BooleanSetting selfDisable;
   public BooleanSetting itemDisable;
   public BooleanSetting render;
   private List<BlockPos> F;
   private int G;
   private boolean H;

   public AutoTrap() {
      String[] var1 = new String[]{"Partial", "Full"};
      this.mode = new ModeSetting("Mode", "The offsets that will be used when trapping.", "Full", var1);
      ModeSetting var3 = this.mode;
      String[] var4 = new String[]{"Full"};
      ModeSetting.ModeCondition var2 = new ModeSetting.ModeCondition(var3, var4);
      this.head = new BooleanSetting("Head", "Whether or not to cover the block on the players head.", var2, true);
      this.noFeet = new BooleanSetting(
         "NoFeet", "Skips the new floor-level ring around the target's feet entirely -- walls and head (if enabled) still trap as before.", false
      );
      BooleanSetting var6 = this.noFeet;
      BooleanSetting.BooleanCondition var5 = new BooleanSetting.BooleanCondition(var6, false);
      this.disableAfterFeet = new BooleanSetting(
         "DisableAfterFeet",
         "Stops re-trapping the floor-level feet ring once it has successfully placed there at least once since the module was enabled -- walls/head keep trapping normally.",
         var5,
         false
      );
      this.asynchronous = new BooleanSetting("Asynchronous", "Performs calculations on separate threads.", true);
      Integer var7 = 4;
      Integer var8 = 1;
      Integer var9 = 8;
      this.bPT = new NumberSetting("BPT", "The maximum number of blocks to place per tick.", var7, var8, var9);
      Double var10 = 5.0;
      Double var11 = 0.0;
      Double var12 = 12.0;
      this.range = new NumberSetting("Range", "The maximum range at which the blocks will be placed at.", var10, var11, var12);
      Float var13 = 8.0F;
      Float var14 = 0.0F;
      Float var15 = 16.0F;
      this.enemyRange = new NumberSetting("EnemyRange", "The maximum distance at which the target should be at.", var13, var14, var15);
      this.await = new BooleanSetting("Await", "Waits for blocks to be registered by the client before placing on them.", false);
      this.rotate = new BooleanSetting("Rotate", "Rotates toward each block before placing it.", true);
      this.timer = new BooleanSetting("Timer", "Runs the game at 1.08x while enabled -- pays GrimAC Timer balance back between placement bursts.", false);
      this.strictDirection = new BooleanSetting("StrictDirection", "Only places using directions that face you.", false);
      this.airPlace = new BooleanSetting(
         "AirPlace",
         "Places blocks directly in the air with no real neighbor needed, skipping the support-scaffold bootstrap entirely instead of only using it as a last resort.",
         false
      );
      this.attack = new BooleanSetting("Attack", "Attacks any crystal that interferes with your trap.", false);
      BooleanSetting var16 = this.attack;
      this.attackRotate = new BooleanSetting(
         "AttackRotate", "Rotate", "Rotates toward the crystal before attacking it.", new BooleanSetting.BooleanCondition(var16, true), true
      );
      BooleanSetting var18 = this.attack;
      BooleanSetting.BooleanCondition var17 = new BooleanSetting.BooleanCondition(var18, true);
      Float var19 = 3.0F;
      Float var20 = 1.0F;
      Float var21 = 6.0F;
      this.attackRange = new NumberSetting("AttackRange", "Range", "The maximum range at which crystals will be attacked.", var17, var19, var20, var21);
      BooleanSetting var23 = this.attack;
      BooleanSetting.BooleanCondition var22 = new BooleanSetting.BooleanCondition(var23, true);
      Integer var24 = 5;
      Integer var25 = 0;
      Integer var26 = 20;
      this.attackAge = new NumberSetting(
         "AttackAge", "Age", "The minimum age (in ticks) a crystal must reach before being attacked.", var22, var24, var25, var26
      );
      BooleanSetting var27 = this.attack;
      this.attackMultiTask = new BooleanSetting(
         "AttackMultiTask", "Multitask", "Allows attacking while an item is already in use.", new BooleanSetting.BooleanCondition(var27, true), true
      );
      BooleanSetting var28 = this.attack;
      this.x = new BooleanSetting(
         "AttackSwing", "Swing", "Sends a swing packet whenever attacking a crystal.", new BooleanSetting.BooleanCondition(var28, true), true
      );
      this.y = new BooleanSetting("HoleCheck", "Checks if the target is in a hole or not before placing.", true);
      this.z = new BooleanSetting("AntiStep", "Adds additional blocks that prevent the player from stepping out of the hole.", false);
      this.whileEating = new BooleanSetting("WhileEating", "Places blocks normally while eating.", true);
      this.ignoreNaked = new BooleanSetting("IgnoreNaked", "Ignore Naked, even that player wears elytra only", false);
      this.selfDisable = new BooleanSetting("SelfDisable", "Toggles off the module once it is finished with placing.", false);
      this.itemDisable = new BooleanSetting("ItemDisable", "Toggles off the module whenever you run out of items to place with.", true);
      this.render = new BooleanSetting("Render", "Whether or not to render the place position.", true);
      this.F = new ArrayList<>();
      this.G = 0;
      this.H = false;
   }

   @Override
   public void onEnable() {
      if (mc.player == null || mc.level == null) {
         this.setEnabled(false);
      }

      if (this.timer.getValue()) {
         ModuleRegistryHelper.a(this);
      }

      this.H = false;
   }

   @EventTarget
   public void a_void_UpdateWalkingPlayerEvent(UpdateWalkingPlayerEvent var1) {
      if (this.whileEating.getValue() || !EntityUtil.a()) {
         List var2 = mc.level.players();
         Runnable var3 = () -> {
            this.G = 0;
            if (this.switchSetting.getValue().equalsIgnoreCase("None") && !(mc.player.getMainHandItem().getItem() instanceof BlockItem)) {
               if (this.itemDisable.getValue()) {
                  String var2x = this.getName();
                  Night.chatManager.sendTaggedMessage("You are currently not holding any blocks.", var2x);
                  this.setEnabled(false);
               }

               this.F = new ArrayList<>();
            } else {
               byte var3x;
               if (!this.switchSetting.getValue().equalsIgnoreCase("AltSwap") && !this.switchSetting.getValue().equalsIgnoreCase("AltPickup")) {
                  var3x = 8;
               } else {
                  var3x = 35;
               }

               int var4 = InventoryUtil.b(0, var3x);
               int var5 = mc.player.getInventory().getSelectedSlot();
               if (var4 == -1) {
                  if (this.itemDisable.getValue()) {
                     String var6 = this.getName();
                     Night.chatManager.sendTaggedMessage("No blocks could be found in your hotbar.", var6);
                     this.setEnabled(false);
                  }

                  this.F = new ArrayList<>();
               } else {
                  AutoTrap.TrapCalculation var7 = this.a_TrapCalculation_List(var2);
                  if (var7 == null) {
                     if (this.selfDisable.getValue()) {
                        this.setEnabled(false);
                     }

                     this.F = new ArrayList<>();
                  } else {
                     List var8 = var7.list();
                     this.F = var8;
                     if (this.attack.getValue()) {
                        this.v();
                     }

                     int var9 = var7.player().getBlockY();
                     Night.rotationManager.beginBatch();

                     try {
                        String var10 = this.switchSetting.getValue();
                        int var11 = var4;
                        int var12 = var5;
                        InventoryUtil.a_bool_String_int_int(var10, var11, var12);
                        AutoCrystal var13;
                        if (Night.moduleManager != null) {
                           var13 = Night.moduleManager.getModule(AutoCrystal.class);
                        } else {
                           var13 = null;
                        }

                        AutoCrystal var14 = var13;
                        int var15 = Mth.floor(var7.player().getY()) + 2;
                        ArrayList var16 = new ArrayList();
                        Iterator var17 = this.F.iterator();

                        label101:
                        while (true) {
                           BlockPos var18;
                           AutoCrystal var23;
                           BlockPos var24;
                           do {
                              if (!var17.hasNext()) {
                                 break label101;
                              }

                              var18 = (BlockPos)var17.next();
                              int var19 = this.G;
                              int var20 = this.bPT.getValue().intValue();
                              if (var19 >= var20) {
                                 break label101;
                              }

                              int var21 = var18.getY();
                              int var22 = var15;
                              if (var21 != var22 || var14 == null || !var14.isEnabled()) {
                                 break;
                              }

                              var23 = var14;
                              var24 = var18.above();
                           } while (var23.a_bool_BlockPos(var24));

                           if (this.attack.getValue()) {
                              ClientLevel var25 = mc.level;
                              BlockPos var27 = var18;
                              AABB var26 = new AABB(var27);
                              double var28 = 0.5;
                              AABB var30 = var26.inflate(var28);

                              for (EndCrystal var32 : (Iterable<EndCrystal>)(Object)var25.getEntitiesOfClass(EndCrystal.class, var30)) {
                                 if (var32.isAlive()) {
                                    ClientPacketListener var33 = mc.getConnection();
                                    int var34 = var32.getId();
                                    var33.send(new ServerboundAttackPacket(var34));
                                    if (this.x.getValue()) {
                                       ClientPacketListener var35 = mc.getConnection();
                                       Packet var36 = PacketAndRenderUtil.a_Packet_InteractionHand(InteractionHand.MAIN_HAND);
                                       var35.send(var36);
                                    }
                                 }
                              }
                           }

                           BlockPos var37 = var18;
                           ArrayList var38 = var16;
                           boolean var39 = this.strictDirection.getValue();
                           Direction var40 = BlockInteractionUtil.b_Direction_BlockPos_List_bool(var37, var38, var39);
                           if (var40 == null) {
                              if (this.airPlace.getValue()) {
                                 BlockPos var41 = var18;
                                 boolean var42 = this.rotate.getValue();
                                 boolean var43 = this.render.getValue();
                                 BlockInteractionUtil.a_void_BlockPos_InteractionHand_bool_bool(var41, InteractionHand.MAIN_HAND, var42, var43);
                                 ArrayList var44 = var16;
                                 BlockPos var45 = var18;
                                 var44.add(var45);
                                 int var46 = this.G + 1;
                                 this.G = var46;
                                 int var47 = var18.getY();
                                 int var48 = var9;
                                 if (var47 == var48) {
                                    this.H = true;
                                 }
                              } else {
                                 BlockPos var49 = var18;
                                 ArrayList var50 = var16;
                                 boolean var51 = this.strictDirection.getValue();
                                 BlockPos var52 = BlockInteractionUtil.a_BlockPos_BlockPos_List_bool_int(var49, var50, var51, 2);
                                 if (var52 != null) {
                                    BlockPos var53 = var52;
                                    ArrayList var54 = var16;
                                    boolean var55 = this.strictDirection.getValue();
                                    Direction var56 = BlockInteractionUtil.b_Direction_BlockPos_List_bool(var53, var54, var55);
                                    if (var56 != null) {
                                       BlockPos var57 = var52;
                                       Direction var58 = var56;
                                       boolean var59 = this.rotate.getValue();
                                       boolean var60 = this.render.getValue();
                                       if (BlockInteractionUtil.a(var57, var58, InteractionHand.MAIN_HAND, var59, true, var60)) {
                                          ArrayList var61 = var16;
                                          BlockPos var62 = var52;
                                          var61.add(var62);
                                          int var63 = this.G + 1;
                                          this.G = var63;
                                       }
                                    }
                                 }
                              }
                           } else {
                              BlockPos var64 = var18;
                              Direction var65 = var40;
                              boolean var66 = this.rotate.getValue();
                              boolean var67 = this.render.getValue();
                              BlockInteractionUtil.a(var64, var65, InteractionHand.MAIN_HAND, var66, true, var67);
                              ArrayList var68 = var16;
                              BlockPos var69 = var18;
                              var68.add(var69);
                              int var70 = this.G + 1;
                              this.G = var70;
                              int var71 = var18.getY();
                              int var72 = var9;
                              if (var71 == var72) {
                                 this.H = true;
                              }
                           }
                        }

                        String var73 = this.switchSetting.getValue();
                        int var74 = var4;
                        int var75 = var5;
                        InventoryUtil.b(var73, var74, var75);
                     } catch (Throwable var77) {
                        Night.rotationManager.endBatch();
                        throw var77;
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

   @EventTarget
   public void a_void_PacketReceiveEvent(PacketReceiveEvent var1) {
      if (mc.player != null && mc.level != null) {
         if (this.attack.getValue() && var1.getPacket() instanceof ClientboundAddEntityPacket var3 && var3.getType() == EntityTypes.END_CRYSTAL) {
            double var4 = var3.getX();
            double var6 = var3.getY();
            double var8 = var3.getZ();
            Vec3 var10 = new Vec3(var4, var6, var8);
            double var11 = var10.x;
            double var13 = 1.0;
            double var15 = var11 - var13;
            double var17 = var10.y;
            double var19 = var10.z;
            double var21 = 1.0;
            double var23 = var19 - var21;
            double var25 = var10.x;
            double var27 = 1.0;
            double var29 = var25 + var27;
            double var31 = var10.y;
            double var33 = 2.0;
            double var35 = var31 + var33;
            double var37 = var10.z;
            double var39 = 1.0;
            double var41 = var37 + var39;
            AABB var43 = new AABB(var15, var17, var23, var29, var35, var41);
            boolean var44 = false;
            Iterator var45 = this.F.iterator();

            label37: {
               double var51;
               double var53;
               do {
                  if (!var45.hasNext()) {
                     break label37;
                  }

                  BlockPos var46 = (BlockPos)var45.next();
                  AABB var47 = var43;
                  BlockPos var48 = var46;
                  if (var47.intersects(new AABB(var48))) {
                     break;
                  }

                  Vec3 var49 = var10;
                  Vec3 var50 = Vec3.atCenterOf(var46);
                  var51 = var49.distanceToSqr(var50);
                  var53 = 4.0;
               } while (!(var51 <= var53));

               var44 = true;
            }

            if (var44) {
               Vec3 var55 = mc.player.getEyePosition();
               Vec3 var56 = var10;
               double var57 = var55.distanceToSqr(var56);
               double var59 = Mth.square(this.attackRange.getValue().doubleValue());
               if (var57 <= var59 && (this.attackMultiTask.getValue() || !mc.player.isUsingItem())) {
                  ClientPacketListener var61 = mc.getConnection();
                  int var62 = var3.getId();
                  var61.send(new ServerboundAttackPacket(var62));
                  if (this.x.getValue()) {
                     ClientPacketListener var63 = mc.getConnection();
                     Packet var64 = PacketAndRenderUtil.a_Packet_InteractionHand(InteractionHand.MAIN_HAND);
                     var63.send(var64);
                  }
               }
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
            int var14 = this.attackAge.getValue().intValue();
            if (var13 >= var14) {
               AABB var15 = var12.getBoundingBox();
               boolean var16 = false;

               for (BlockPos var18 : (Iterable<BlockPos>)(Object)this.F) {
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
                  if (!(var25 > var31) && (this.attackMultiTask.getValue() || !mc.player.isUsingItem())) {
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
                     if (this.x.getValue()) {
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

   @Override
   public void onDisable() {
      ModuleRegistryHelper.b(this);
      this.F = new ArrayList<>();
   }

   @Override
   public String getDisplayInfo() {
      return String.valueOf(this.F.size());
   }

   private AutoTrap.TrapCalculation a_TrapCalculation_List(List<AbstractClientPlayer> var1) {
      AutoTrap.TrapCalculation var2 = null;
      LogoutSpot var3;
      if (Night.moduleManager != null) {
         var3 = Night.moduleManager.getModule(LogoutSpot.class);
      } else {
         var3 = null;
      }

      LogoutSpot var4 = var3;
      PopChams var5;
      if (Night.moduleManager != null) {
         var5 = Night.moduleManager.getModule(PopChams.class);
      } else {
         var5 = null;
      }

      PopChams var6 = var5;
      ArrayList var7 = new ArrayList(var1);
      if (var4 != null && var4.isEnabled()) {
         for (Player var9 : var4.v()) {
            if (var9 != null) {
               ArrayList var10 = var7;
               Player var11 = var9;
               if (!var10.contains(var11)) {
                  ArrayList var12 = var7;
                  Player var13 = var9;
                  var12.add(var13);
               }
            }
         }
      }

      for (Player var15 : (Iterable<Player>)(Object)var7) {
         LocalPlayer var16 = mc.player;
         if (var15 != var16) {
            if (var6 != null) {
               PopChams var17 = var6;
               Player var18 = var15;
               if (var17.a_bool_Entity(var18)) {
                  continue;
               }
            }

            boolean var19 = EntityUtil.c_bool_Entity(var15);
            if (var19 || var15.isAlive() && !(var15.getHealth() <= 0.0F)) {
               LocalPlayer var20 = mc.player;
               Player var21 = var15;
               double var22 = var20.distanceToSqr(var21);
               double var24 = Mth.square(this.enemyRange.getValue().doubleValue());
               if (!(var22 > var24) && (!this.ignoreNaked.getValue() || !EntityUtil.b_bool_Player(var15))) {
                  label97: {
                     if (var4 != null) {
                        LogoutSpot var26 = var4;
                        Player var27 = var15;
                        if (var26.a_bool_Entity(var27)) {
                           LogoutSpot var28 = var4;
                           RemotePlayer var29 = (RemotePlayer)var15;
                           LogoutSpot.LogoutEntry var30 = var28.a_LogoutEntry_RemotePlayer(var29);
                           if (var30 != null) {
                              String var31 = var30.b.a;
                              if (Night.friendManager.isFriend(var31)) {
                                 continue;
                              }
                           }
                           break label97;
                        }
                     }

                     String var32 = var15.getName().getString();
                     if (Night.friendManager.isFriend(var32)) {
                        continue;
                     }
                  }

                  if (!this.y.getValue() || HoleUtil.a_bool_Player(var15)) {
                     boolean var33;
                     if (this.noFeet.getValue() || this.disableAfterFeet.getValue() && this.H) {
                        var33 = false;
                     } else {
                        var33 = true;
                     }

                     boolean var34 = var33;
                     Player var35 = var15;
                     boolean var36 = this.mode.getValue().equalsIgnoreCase("Partial");
                     boolean var37 = this.head.getValue();
                     boolean var38 = this.z.getValue();
                     boolean var39 = this.strictDirection.getValue();
                     boolean var40 = var34;
                     boolean var41 = this.airPlace.getValue();
                     List var42 = HoleUtil.a(var35, var36, var37, var38, false, var39, var40, var41).stream().filter(var1x -> {
                        LocalPlayer var2x = mc.player;
                        Vec3 var3x = Vec3.atCenterOf(var1x);
                        double var4x = var2x.distanceToSqr(var3x);
                        double var6x = Mth.square(this.range.getValue().doubleValue());
                        boolean var8;
                        if (var4x <= var6x) {
                           var8 = true;
                        } else {
                           var8 = false;
                        }

                        return var8;
                     }).filter(BlockInteractionUtil::c_bool_BlockPos).toList();
                     if (!var42.isEmpty()) {
                        if (var2 == null) {
                           Player var43 = var15;
                           List var44 = var42;
                           var2 = new AutoTrap.TrapCalculation(var43, var44);
                        } else {
                           boolean var45 = EntityUtil.c_bool_Entity(var2.player());
                           boolean var46 = var19;
                           if (var45 != var46) {
                              if (var45) {
                                 Player var47 = var15;
                                 List var48 = var42;
                                 var2 = new AutoTrap.TrapCalculation(var47, var48);
                              }
                           } else {
                              LocalPlayer var49 = mc.player;
                              Player var50 = var15;
                              double var51 = var49.distanceToSqr(var50);
                              LocalPlayer var53 = mc.player;
                              Player var54 = var2.player();
                              double var55 = var53.distanceToSqr(var54);
                              if (var51 < var55) {
                                 Player var57 = var15;
                                 List var58 = var42;
                                 var2 = new AutoTrap.TrapCalculation(var57, var58);
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      return var2;
   }

   record TrapCalculation(Player player, List<BlockPos> list) {
   }
}
