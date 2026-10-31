package night.module.impl.combat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.events.EventTarget;
import night.events.impl.BlockBreakProgressEvent;
import night.events.impl.UpdateWalkingPlayerEvent;
import night.module.Module;
import night.module.ModuleInfo;
import night.module.impl.player.SpeedMine;
import night.setting.impl.BooleanSetting;
import night.setting.impl.ModeSetting;
import night.setting.impl.NumberSetting;
import night.util.concurrent.ThreadManager;
import night.util.inventory.InventoryUtil;
import night.util.math.TimerUtil;
import night.util.world.BlockInteractionUtil;
import night.util.world.EntityUtil;
import night.util.world.HoleUtil;

@ModuleInfo(name = "Blocker", description = "Places blocks to stop enemies from placing crystals.", category = Module.Category.COMBAT, proxySupported = true)
public class Blocker extends Module {
   public ModeSetting switchSetting = new ModeSetting(
      "Switch", "The mode that will be used for automatically switching to necessary items.", "Silent", InventoryUtil.a
   );
   public BooleanSetting asynchronous = new BooleanSetting("Asynchronous", "Performs calculations on separate threads.", true);
   public NumberSetting limit;
   public NumberSetting delay;
   public NumberSetting range;
   public BooleanSetting rotate;
   public BooleanSetting strictDirection;
   public BooleanSetting crystalDestruction;
   public BooleanSetting whileEating;
   public BooleanSetting feet;
   public BooleanSetting head;
   public BooleanSetting render;
   private final CopyOnWriteArrayList<Blocker.CrystalBlockPair> q;
   private Blocker.QueuedBlock r;
   private int s;

   public Blocker() {
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
      this.range = new NumberSetting("Range", "The maximum range at which the blocks will be placed at.", var7, var8, var9);
      this.rotate = new BooleanSetting("Rotate", "Sends a packet rotation whenever placing a block.", true);
      this.strictDirection = new BooleanSetting("StrictDirection", "Only places using directions that face you.", false);
      this.crystalDestruction = new BooleanSetting("CrystalDestruction", "Destroys any crystals that interfere with block placement.", true);
      this.whileEating = new BooleanSetting("WhileEating", "Places blocks normally while eating.", true);
      this.feet = new BooleanSetting("Feet", "Places on feet level blocks.", true);
      this.head = new BooleanSetting("Head", "Places on head level blocks.", true);
      this.render = new BooleanSetting("Render", "Whether or not to render the place position.", true);
      this.q = new CopyOnWriteArrayList<>();
      this.r = null;
      this.s = 0;
   }

   @EventTarget
   public void a_void_UpdateWalkingPlayerEvent(UpdateWalkingPlayerEvent var1) {
      if (!this.isProxyDelegated()) {
         if (this.whileEating.getValue() || !EntityUtil.a()) {
            Runnable var2 = () -> {
               if (mc.player != null && mc.level != null) {
                  label136: {
                     SpeedMine var1x = Night.moduleManager.getModule(SpeedMine.class);
                     if (this.r != null && var1x.v() != null) {
                        BlockPos var2x = this.r.pos();
                        BlockPos var3 = var1x.v().a();
                        if (var2x.equals(var3)) {
                           break label136;
                        }
                     }

                     if (var1x.w() != null) {
                        BlockPos var4 = this.r.pos();
                        BlockPos var5 = var1x.w().a();
                        if (var4.equals(var5)) {
                           break label136;
                        }
                     }

                     int var6 = 0;
                     int var7 = this.s;
                     int var8 = this.delay.getValue().intValue();
                     if (var7 < var8) {
                        int var9 = this.s + 1;
                        this.s = var9;
                        return;
                     }

                     HashSet var10 = HoleUtil.a_HashSet_Player_bool_bool_bool(mc.player, true, false, false);
                     List var11 = HoleUtil.a_List_Entity(mc.player);
                     if (this.r != null) {
                        TimerUtil var12 = this.r.timer();
                        Float var13 = Math.max(this.r.value() - 200.0F, 0.0F);
                        if (var12.passedMs(var13)) {
                           BlockPos var14 = this.r.pos();
                           if (this.r.protectArea() == Blocker.ProtectArea.FEET && this.feet.getValue()) {
                              HashSet var15 = var10;
                              BlockPos var16 = this.r.pos();
                              if (var15.contains(var16)) {
                                 CopyOnWriteArrayList var17 = this.q;
                                 BlockPos var18 = var14;
                                 BlockPos var19 = var14.above();
                                 var17.add(new Blocker.CrystalBlockPair(var18, var19));
                                 Direction[] var20 = Direction.values();
                                 int var21 = var20.length;
                                 int var22 = 0;

                                 while (true) {
                                    int var23 = var22;
                                    int var24 = var21;
                                    if (var23 >= var24) {
                                       break;
                                    }

                                    Direction[] var25 = var20;
                                    int var26 = var22;
                                    Direction var27 = var25[var26];
                                    if (var27.getAxis().isHorizontal()) {
                                       CopyOnWriteArrayList var28 = this.q;
                                       BlockPos var29 = var14;
                                       BlockPos var30 = var14;
                                       Direction var31 = var27;
                                       BlockPos var32 = var30.relative(var31);
                                       var28.add(new Blocker.CrystalBlockPair(var29, var32));
                                    }

                                    var22++;
                                 }
                              }

                              this.r = null;
                           } else if ((this.r.protectArea() == Blocker.ProtectArea.HEAD || this.r.protectArea() == Blocker.ProtectArea.SIDE)
                              && this.head.getValue()) {
                              label90: {
                                 label130: {
                                    if (this.r.protectArea() == Blocker.ProtectArea.HEAD) {
                                       List var33 = var11;
                                       BlockPos var34 = this.r.pos().below().below();
                                       if (var33.contains(var34)) {
                                          break label130;
                                       }
                                    }

                                    if (this.r.protectArea() != Blocker.ProtectArea.SIDE) {
                                       break label90;
                                    }

                                    HashSet var35 = var10;
                                    BlockPos var36 = this.r.pos().below();
                                    if (!var35.contains(var36)) {
                                       break label90;
                                    }
                                 }

                                 CopyOnWriteArrayList var37 = this.q;
                                 BlockPos var38 = var14;
                                 BlockPos var39 = var14.above();
                                 var37.add(new Blocker.CrystalBlockPair(var38, var39));
                              }

                              this.r = null;
                           }
                        }
                     }

                     this.q.removeIf(var0 -> {
                        boolean var1xx;
                        if (!BlockInteractionUtil.c_bool_BlockPos(var0.pos2())) {
                           var1xx = true;
                        } else {
                           var1xx = false;
                        }

                        return var1xx;
                     });
                     this.q.removeIf(var1xx -> {
                        LocalPlayer var2xx = mc.player;
                        Vec3 var3x = Vec3.atCenterOf(var1xx.pos2());
                        double var4x = var2xx.distanceToSqr(var3x);
                        double var6x = Mth.square(this.range.getValue().doubleValue());
                        boolean var8x;
                        if (var4x > var6x) {
                           var8x = true;
                        } else {
                           var8x = false;
                        }

                        return var8x;
                     });
                     CopyOnWriteArrayList<Blocker.CrystalBlockPair> var40 = this.q;
                     HashSet var41 = var10;
                     List var42 = var11;
                     var40.removeIf(var2xx -> {
                        BlockPos var3x = var2xx.pos();
                        if (!var41.contains(var3x)) {
                           BlockPos var4x = var2xx.pos().below();
                           if (!var41.contains(var4x)) {
                              BlockPos var5x = var2xx.pos().below().below();
                              if (!var42.contains(var5x)) {
                                 return true;
                              }
                           }
                        }

                        return false;
                     });
                     if (this.q.isEmpty()) {
                        return;
                     }

                     byte var43;
                     if (!this.switchSetting.getValue().equalsIgnoreCase("AltSwap") && !this.switchSetting.getValue().equalsIgnoreCase("AltPickup")) {
                        var43 = 8;
                     } else {
                        var43 = 35;
                     }

                     int var44 = InventoryUtil.b(0, var43);
                     int var45 = mc.player.getInventory().getSelectedSlot();
                     if (var44 == -1) {
                        this.q.clear();
                        return;
                     }

                     Night.rotationManager.beginBatch();

                     try {
                        String var46 = this.switchSetting.getValue();
                        int var47 = var44;
                        int var48 = var45;
                        InventoryUtil.a_bool_String_int_int(var46, var47, var48);
                        CopyOnWriteArrayList var49 = this.q;

                        for (Blocker.CrystalBlockPair var51 : (Iterable<Blocker.CrystalBlockPair>)(Object)new ArrayList(var49)) {
                           int var52 = var6;
                           int var53 = this.limit.getValue().intValue();
                           if (var52 >= var53) {
                              break;
                           }

                           BlockPos var54 = var51.pos2();
                           boolean var55 = this.strictDirection.getValue();
                           Direction var56 = BlockInteractionUtil.b_Direction_BlockPos_List_bool(var54, null, var55);
                           if (var56 != null) {
                              BlockPos var57 = var51.pos2();
                              Direction var58 = var56;
                              boolean var59 = this.rotate.getValue();
                              boolean var60 = this.crystalDestruction.getValue();
                              boolean var61 = this.render.getValue();
                              BlockInteractionUtil.a(var57, var58, InteractionHand.MAIN_HAND, var59, var60, var61);
                              CopyOnWriteArrayList var62 = this.q;
                              Blocker.CrystalBlockPair var63 = var51;
                              var62.remove(var63);
                              var6++;
                           }
                        }

                        String var64 = this.switchSetting.getValue();
                        int var65 = var44;
                        int var66 = var45;
                        InventoryUtil.b(var64, var65, var66);
                     } catch (Throwable var68) {
                        Night.rotationManager.endBatch();
                        throw var68;
                     }

                     Night.rotationManager.endBatch();
                     this.s = 0;
                     return;
                  }

                  this.r = null;
               }
            };
            if (this.asynchronous.getValue()) {
               ThreadManager.a(var2);
            } else {
               var2.run();
            }
         }
      }
   }

   @EventTarget
   public void a_void_BlockBreakProgressEvent(BlockBreakProgressEvent var1) {
      if (!this.isProxyDelegated()) {
         if (mc.player != null && mc.level != null) {
            if (this.r != null) {
               BlockPos var2 = this.r.pos();
               BlockPos var3 = var1.getPos();
               if (var2.equals(var3)) {
                  return;
               }
            }

            SpeedMine var4 = Night.moduleManager.getModule(SpeedMine.class);
            if (var4.v() != null) {
               BlockPos var5 = var1.getPos();
               BlockPos var6 = var4.v().a();
               if (var5.equals(var6)) {
                  return;
               }
            }

            if (var4.w() != null) {
               BlockPos var7 = var1.getPos();
               BlockPos var8 = var4.w().a();
               if (var7.equals(var8)) {
                  return;
               }
            }

            ClientLevel var9 = mc.level;
            int var10 = var1.getBreakerId();
            Entity var11 = var9.getEntity(var10);
            LocalPlayer var12 = mc.player;
            if (var11 != var12) {
               if (var11 instanceof Player var13) {
                  String var14 = var13.getName().getString();
                  if (!Night.friendManager.isFriend(var14)) {
                     HashSet var15 = HoleUtil.a_HashSet_Player_bool_bool_bool(mc.player, true, false, false);
                     List var16 = HoleUtil.a_List_Entity(mc.player);
                     if (this.feet.getValue()) {
                        HashSet var17 = var15;
                        BlockPos var18 = var1.getPos();
                        if (var17.contains(var18)) {
                           BlockPos var19 = var1.getPos();
                           TimerUtil var20 = new TimerUtil();
                           Player var21 = var13;
                           ClientLevel var22 = mc.level;
                           BlockPos var23 = var1.getPos();
                           BlockState var24 = var22.getBlockState(var23);
                           float var25 = BlockInteractionUtil.a_float_Player_BlockState(var21, var24);
                           this.r = new Blocker.QueuedBlock(var19, var20, var25, Blocker.ProtectArea.FEET);
                           return;
                        }
                     }

                     if (this.head.getValue()) {
                        HashSet var26 = var15;
                        BlockPos var27 = var1.getPos().below();
                        if (var26.contains(var27)) {
                           BlockPos var28 = var1.getPos();
                           TimerUtil var29 = new TimerUtil();
                           Player var30 = var13;
                           ClientLevel var31 = mc.level;
                           BlockPos var32 = var1.getPos();
                           BlockState var33 = var31.getBlockState(var32);
                           float var34 = BlockInteractionUtil.a_float_Player_BlockState(var30, var33);
                           this.r = new Blocker.QueuedBlock(var28, var29, var34, Blocker.ProtectArea.SIDE);
                        }

                        List var35 = var16;
                        BlockPos var36 = var1.getPos().below().below();
                        if (var35.contains(var36)) {
                           BlockPos var37 = var1.getPos();
                           TimerUtil var38 = new TimerUtil();
                           Player var39 = var13;
                           ClientLevel var40 = mc.level;
                           BlockPos var41 = var1.getPos();
                           BlockState var42 = var40.getBlockState(var41);
                           float var43 = BlockInteractionUtil.a_float_Player_BlockState(var39, var42);
                           this.r = new Blocker.QueuedBlock(var37, var38, var43, Blocker.ProtectArea.HEAD);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   @Override
   public void onDisable() {
      this.q.clear();
   }

   @Override
   public String getDisplayInfo() {
      return this.q == null ? "0" : String.valueOf(this.q.size());
   }

   record CrystalBlockPair(BlockPos pos, BlockPos pos2) {
   }

   enum ProtectArea {
      FEET,
      HEAD,
      SIDE;
   }

   record QueuedBlock(BlockPos pos, TimerUtil timer, float value, Blocker.ProtectArea protectArea) {
   }
}
