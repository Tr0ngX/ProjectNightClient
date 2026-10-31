package night.module.impl.combat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import night.Night;
import night.events.EventTarget;
import night.events.impl.EntitySpawnEvent;
import night.events.impl.UpdateWalkingPlayerEvent;
import night.module.Module;
import night.module.ModuleInfo;
import night.setting.impl.BooleanSetting;
import night.setting.impl.ModeSetting;
import night.setting.impl.NumberSetting;
import night.util.inventory.InventoryUtil;
import night.util.network.PacketPredictionUtil;
import night.util.rotation.RotationMathUtil;
import night.util.world.BlockPlaceUtil;
import night.util.world.PacketAndRenderUtil;

@ModuleInfo(
   name = "AntiPearl",
   description = "Places a crystal next to an enemy's ender pearl flight, then breaks it right before the pearl lands to knock it off course.",
   category = Module.Category.COMBAT
)
public class AntiPearl extends Module {
   public ModeSetting switchSetting = new ModeSetting("Switch", "The mode used for switching to end crystals.", "Silent", InventoryUtil.a);
   public NumberSetting range;
   public BooleanSetting raytrace;
   public BooleanSetting rotate;
   public NumberSetting breakTicks;
   public BooleanSetting debug;
   private static final int k = 200;
   private static final int l = 4;
   private static final int m = 4;
   private static final int n = 1;
   private static final double o = 1.0;
   private static final double p = 0.125;
   private static final double q = 0.2125;
   private static final double r = 12.0;
   private final Map<ThrownEnderpearl, AntiPearl.CandidateBlock> s;

   public AntiPearl() {
      Double var1 = 5.0;
      Double var2 = 0.0;
      Double var3 = 6.0;
      this.range = new NumberSetting("Range", "Max distance from you to the pearl's landing point and to the crystal.", var1, var2, var3);
      this.raytrace = new BooleanSetting("Raytrace", "Only places and breaks crystals you can see (no through walls).", false);
      this.rotate = new BooleanSetting("Rotate", "Rotates toward the crystal before placing and breaking it.", true);
      Integer var4 = 2;
      Integer var5 = 0;
      Integer var6 = 6;
      this.breakTicks = new NumberSetting(
         "BreakTicks",
         "Server ticks before the pearl lands at which the crystal blows. Lower = pearl closer = harder push, less time to fly off.",
         var4,
         var5,
         var6
      );
      this.debug = new BooleanSetting("Debug", "Logs prediction, place and break timing to chat.", false);
      this.s = new ConcurrentHashMap<>();
   }

   @Override
   public void onDisable() {
      this.s.clear();
   }

   @EventTarget
   public void a_void_EntitySpawnEvent(EntitySpawnEvent var1) {
      if (mc.player != null && mc.level != null) {
         if (var1.getEntity() instanceof ThrownEnderpearl var3) {
            Entity var4 = var3.getOwner();
            LocalPlayer var5 = mc.player;
            if (var4 != var5) {
               if (var4 == null) {
                  LocalPlayer var6 = mc.player;
                  Vec3 var7 = var3.position();
                  double var8 = var6.distanceToSqr(var7);
                  double var10 = 4.0;
                  if (var8 < var10) {
                     return;
                  }
               }

               if (var4 instanceof Player) {
                  String var12 = ((Player)var4).getName().getString();
                  if (Night.friendManager.isFriend(var12)) {
                     return;
                  }
               }

               Map var13 = this.s;
               ThrownEnderpearl var14 = var3;
               var13.put(var14, new AntiPearl.CandidateBlock());
            }
         }
      }
   }

   @EventTarget
   public void a_void_UpdateWalkingPlayerEvent(UpdateWalkingPlayerEvent var1) {
      if (mc.player != null && mc.level != null && !this.s.isEmpty()) {
         double var2 = Mth.square(this.range.getValue().doubleValue());
         double var4 = Night.serverManager.getPing();
         double var6 = 50.0;
         int var8 = Mth.ceil(var4 / var6);
         boolean var9 = PacketPredictionUtil.b();
         int var10 = this.breakTicks.getValue().intValue();

         for (Entry var12 : (Iterable<Entry>)(Object)this.s.entrySet()) {
            ThrownEnderpearl var13 = (ThrownEnderpearl)var12.getKey();
            AntiPearl.CandidateBlock var14 = (AntiPearl.CandidateBlock)var12.getValue();
            if (var13.isRemoved()) {
               Map var15 = this.s;
               ThrownEnderpearl var16 = var13;
               var15.remove(var16);
            } else {
               Vec3 var17 = var13.position();
               Vec3 var18 = var13.getDeltaMovement();
               ThrownEnderpearl var19 = var13;
               boolean var20 = var9;
               AntiPearl.PearlPrediction var21 = this.a(var17, var18, var19, var20);
               if (var21 != null) {
                  LocalPlayer var22 = mc.player;
                  Vec3 var23 = var21.vec();
                  double var24 = var22.distanceToSqr(var23);
                  double var26 = var2;
                  if (!(var24 > var26)) {
                     int var28 = var21.count();
                     int var29 = var8;
                     int var30 = var28 - var29;
                     if (var14.a == null) {
                        int var31 = var21.count();
                        int var32 = var10;
                        int var33 = var31 - var32;
                        if (var33 < 1) {
                           this.b("too late to place (" + var21.count() + "t left)");
                           Map var34 = this.s;
                           ThrownEnderpearl var35 = var13;
                           var34.remove(var35);
                           continue;
                        }

                        AntiPearl.PearlPrediction var36 = var21;
                        int var37 = var33;
                        double var38 = var2;
                        ThrownEnderpearl var40 = var13;
                        boolean var41 = var9;
                        BlockPos var42 = this.a(var36, var37, var38, var40, var41);
                        if (var42 == null) {
                           Map var43 = this.s;
                           ThrownEnderpearl var44 = var13;
                           var43.remove(var44);
                           continue;
                        }

                        BlockPos var45 = var42;
                        if (!this.a_bool_BlockPos(var45)) {
                           this.b("place failed (no crystal / switch)");
                           continue;
                        }

                        AntiPearl.CandidateBlock var46 = var14;
                        BlockPos var47 = var42;
                        var46.a = var47;
                        String var48 = var42.toShortString();
                        String var49 = a_String_Vec3(var21.vec());
                        int var50 = var21.count();
                        int var51 = var8;
                        this.b("place " + var48 + " landing " + var49 + " in " + var50 + "t, ping " + var51 + "t");
                     }

                     int var52 = var30;
                     int var53 = var10;
                     if (var52 <= var53) {
                        ClientLevel var54 = mc.level;
                        BlockPos var56 = var14.a;
                        AABB var55 = new AABB(var56);
                        double var57 = 1.0;
                        AABB var59 = var55.inflate(var57);
                        EndCrystal var60 = var54.getEntitiesOfClass(EndCrystal.class, var59, Entity::isAlive)
                           .stream()
                           .min(Comparator.comparingDouble(var1x -> {
                              Vec3 var2x = Vec3.atBottomCenterOf(var14.a);
                              return var1x.distanceToSqr(var2x);
                           }))
                           .orElse(null);
                        if (var60 == null) {
                           if (var30 < 0) {
                              this.b("crystal never showed up, too late");
                              Map var61 = this.s;
                              ThrownEnderpearl var62 = var13;
                              var61.remove(var62);
                           }
                        } else {
                           LocalPlayer var63 = mc.player;
                           EndCrystal var64 = var60;
                           double var65 = var63.distanceToSqr(var64);
                           double var67 = var2;
                           if (var65 > var67) {
                              Map var69 = this.s;
                              ThrownEnderpearl var70 = var13;
                              var69.remove(var70);
                           } else {
                              Vec3 var71 = var60.getBoundingBox().getCenter();
                              if (this.raytrace.getValue()) {
                                 Vec3 var72 = mc.player.getEyePosition();
                                 Vec3 var73 = var71;
                                 LocalPlayer var74 = mc.player;
                                 if (!this.b(var72, var73, var74)) {
                                    continue;
                                 }
                              }

                              if (this.rotate.getValue()) {
                                 float[] var75 = RotationMathUtil.a(var71);
                                 Night.rotationManager.sendPosRotPacket(var75);
                              }

                              ClientPacketListener var76 = mc.getConnection();
                              int var77 = var60.getId();
                              var76.send(new ServerboundAttackPacket(var77));
                              ClientPacketListener var78 = mc.getConnection();
                              Packet var79 = PacketAndRenderUtil.a_Packet_InteractionHand(InteractionHand.MAIN_HAND);
                              var78.send(var79);
                              int var80 = var21.count();
                              int var81 = var30;
                              String var82 = a_String_Vec3(var13.position());
                              this.b("break, client " + var80 + "t / server ~" + var81 + "t before landing, pearl " + var82);
                              Map var83 = this.s;
                              ThrownEnderpearl var84 = var13;
                              var83.remove(var84);
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private AntiPearl.PearlPrediction a(Vec3 var1, Vec3 var2, Entity var3, boolean var4) {
      double var5 = var2.lengthSqr();
      double var7 = 1.0E-4;
      if (var5 < var7) {
         return null;
      }

      ArrayList var9 = new ArrayList();
      ArrayList var10 = new ArrayList();

      for (int var11 = 1; var11 <= 200; var11++) {
         ArrayList var12 = var9;
         Vec3 var13 = var1;
         var12.add(var13);
         ArrayList var14 = var10;
         Vec3 var15 = var2;
         var14.add(var15);
         if (!var4) {
            Vec3 var16 = var2;
            double var17 = 0.0;
            double var19 = 0.03;
            double var21 = 0.0;
            Vec3 var23 = var16.subtract(var17, var19, var21);
            double var24 = 0.99;
            var2 = var23.scale(var24);
         }

         Vec3 var26 = var1;
         Vec3 var27 = var2;
         Vec3 var28 = var26.add(var27);
         ClientLevel var29 = mc.level;
         Vec3 var30 = var1;
         Vec3 var31 = var28;
         BlockHitResult var32 = var29.clip(new ClipContext(var30, var31, Block.COLLIDER, Fluid.NONE, var3));
         if (var32.getType() != Type.MISS) {
            ArrayList var33 = var9;
            ArrayList var34 = var10;
            Vec3 var35 = var32.getLocation();
            int var36 = var11;
            return new AntiPearl.PearlPrediction(var33, var34, var35, var36);
         }

         var1 = var28;
         if (var4) {
            Vec3 var37 = var2;
            double var38 = 0.99;
            Vec3 var40 = var37.scale(var38);
            double var41 = 0.0;
            double var43 = 0.03;
            double var45 = 0.0;
            var2 = var40.subtract(var41, var43, var45);
         }
      }

      return null;
   }

   private BlockPos a(AntiPearl.PearlPrediction var1, int var2, double var3, Entity var5, boolean var6) {
      Vec3 var7 = var1.list().get(var2);
      Vec3 var8 = var1.list2().get(var2);
      BlockPos var9 = BlockPos.containing(var7);
      BlockPos var10 = null;
      double var11 = 1.0;
      int var13 = 0;
      BlockPos var14 = var9.offset(-4, -4, -4);
      BlockPos var15 = var9.offset(4, 1, 4);

      for (BlockPos var17 : (Iterable<BlockPos>)(Object)BlockPos.betweenClosed(var14, var15)) {
         if (BlockPlaceUtil.c(var17)) {
            Vec3 var27 = Vec3.atBottomCenterOf(var17);
            LocalPlayer var28 = mc.player;
            Vec3 var29 = var27;
            double var30 = var28.distanceToSqr(var29);
            double var32 = var3;
            if (!(var30 > var32)) {
               Vec3 var34 = var27;
               List var35 = var1.list();
               if (!this.a_bool_Vec3_List_int(var34, var35, var2)) {
                  var13++;
                  Vec3 var36 = var27;
                  Vec3 var37 = var7;
                  Vec3 var38 = this.a_Vec3_Vec3_Vec3_Entity(var36, var37, var5);
                  double var39 = var38.lengthSqr();
                  double var41 = 1.0E-4;
                  if (!(var39 < var41)) {
                     Vec3 var43 = var7;
                     Vec3 var44 = var8;
                     Vec3 var45 = var38;
                     Vec3 var46 = var44.add(var45);
                     AntiPearl.PearlPrediction var47 = this.a(var43, var46, var5, var6);
                     double var48;
                     if (var47 == null) {
                        var48 = Double.MAX_VALUE;
                     } else {
                        Vec3 var50 = var47.vec();
                        Vec3 var51 = var1.vec();
                        var48 = var50.distanceTo(var51);
                     }

                     double var52 = var48;
                     double var54 = var11;
                     if (!(var52 <= var54) && (!this.raytrace.getValue() || CrystalRaytraceHelper.b(var17.below()))) {
                        var11 = var52;
                        var10 = var17.immutable();
                     }
                  }
               }
            }
         }
      }

      if (var10 == null) {
         String var18;
         if (var13 == 0) {
            var18 = "no obsidian/bedrock spot off the path near " + a_String_Vec3(var7);
         } else {
            var18 = "no spot bends the pearl >= 1.0 (blocked / too far)";
         }

         this.b(var18);
      } else {
         Object[] var19 = new Object[1];
         double var20 = var11;
         double var22 = Double.MAX_VALUE;
         double var24;
         if (var20 == var22) {
            var24 = -1.0;
         } else {
            var24 = var11;
         }

         Double var26 = var24;
         var19[0] = var26;
         this.b("spot bends landing by " + String.format("%.1f", var19) + " blocks");
      }

      return var10;
   }

   private Vec3 a_Vec3_Vec3_Vec3_Entity(Vec3 var1, Vec3 var2, Entity var3) {
      double var4 = var2.distanceTo(var1);
      double var6 = 12.0;
      double var8 = var4 / var6;
      double var10 = 1.0;
      if (var8 > var10) {
         return Vec3.ZERO;
      }

      double var12 = var2.x;
      double var14 = 0.125;
      double var16 = var12 - var14;
      double var18 = var2.y;
      double var20 = var2.z;
      double var22 = 0.125;
      double var24 = var20 - var22;
      double var26 = var2.x;
      double var28 = 0.125;
      double var30 = var26 + var28;
      double var32 = var2.y;
      double var34 = 0.25;
      double var36 = var32 + var34;
      double var38 = var2.z;
      double var40 = 0.125;
      double var42 = var38 + var40;
      AABB var44 = new AABB(var16, var18, var24, var30, var36, var42);
      double var45 = this.a_float_Vec3_AABB_Entity(var1, var44, var3);
      double var47 = 0.0;
      double var49 = 0.2125;
      double var51 = 0.0;
      Vec3 var53 = var2.add(var47, var49, var51).subtract(var1).normalize();
      double var54 = 1.0;
      double var56 = var8;
      double var58 = var54 - var56;
      double var60 = var45;
      double var62 = var58 * var60;
      return var53.scale(var62);
   }

   private float a_float_Vec3_AABB_Entity(Vec3 var1, AABB var2, Entity var3) {
      double var4 = 1.0;
      double var6 = var2.maxX;
      double var8 = var2.minX;
      double var10 = var6 - var8;
      double var12 = 2.0;
      double var14 = var10 * var12;
      double var16 = 1.0;
      double var18 = var14 + var16;
      double var20 = var4 / var18;
      double var22 = 1.0;
      double var24 = var2.maxY;
      double var26 = var2.minY;
      double var28 = var24 - var26;
      double var30 = 2.0;
      double var32 = var28 * var30;
      double var34 = 1.0;
      double var36 = var32 + var34;
      double var38 = var22 / var36;
      double var40 = 1.0;
      double var42 = var2.maxZ;
      double var44 = var2.minZ;
      double var46 = var42 - var44;
      double var48 = 2.0;
      double var50 = var46 * var48;
      double var52 = 1.0;
      double var54 = var50 + var52;
      double var56 = var40 / var54;
      double var58 = 1.0;
      double var60 = 1.0;
      double var62 = var20;
      double var64 = Math.floor(var60 / var62);
      double var66 = var20;
      double var68 = var64 * var66;
      double var70 = var58 - var68;
      double var72 = 2.0;
      double var74 = var70 / var72;
      double var76 = 1.0;
      double var78 = 1.0;
      double var80 = var56;
      double var82 = Math.floor(var78 / var80);
      double var84 = var56;
      double var86 = var82 * var84;
      double var88 = var76 - var86;
      double var90 = 2.0;
      double var92 = var88 / var90;
      int var94 = 0;
      int var95 = 0;
      double var96 = 0.0;

      while (true) {
         double var98 = var96;
         double var100 = 1.0;
         if (!(var98 <= var100)) {
            float var155;
            if (var95 == 0) {
               var155 = 0.0F;
            } else {
               float var156 = var94;
               float var157 = var95;
               var155 = var156 / var157;
            }

            return var155;
         }

         double var102 = 0.0;

         while (true) {
            double var104 = var102;
            double var106 = 1.0;
            if (!(var104 <= var106)) {
               double var151 = var96;
               double var153 = var20;
               var96 = var151 + var153;
               break;
            }

            double var108 = 0.0;

            while (true) {
               double var110 = var108;
               double var112 = 1.0;
               if (!(var110 <= var112)) {
                  double var158 = var102;
                  double var160 = var38;
                  var102 = var158 + var160;
                  break;
               }

               double var114 = var96;
               double var116 = var2.minX;
               double var118 = var2.maxX;
               double var120 = Mth.lerp(var114, var116, var118);
               double var122 = var74;
               double var124 = var120 + var122;
               double var126 = var102;
               double var128 = var2.minY;
               double var130 = var2.maxY;
               double var132 = Mth.lerp(var126, var128, var130);
               double var134 = var108;
               double var136 = var2.minZ;
               double var138 = var2.maxZ;
               double var140 = Mth.lerp(var134, var136, var138);
               double var142 = var92;
               double var144 = var140 + var142;
               Vec3 var146 = new Vec3(var124, var132, var144);
               if (this.b(var146, var1, var3)) {
                  var94++;
               }

               var95++;
               double var147 = var108;
               double var149 = var56;
               var108 = var147 + var149;
            }
         }
      }
   }

   private boolean a_bool_Vec3_List_int(Vec3 var1, List<Vec3> var2, int var3) {
      double var5 = var1.x;
      double var7 = 1.0;
      double var9 = var5 - var7;
      double var11 = var1.y;
      double var13 = var1.z;
      double var15 = 1.0;
      double var17 = var13 - var15;
      double var19 = var1.x;
      double var21 = 1.0;
      double var23 = var19 + var21;
      double var25 = var1.y;
      double var27 = 2.0;
      double var29 = var25 + var27;
      double var31 = var1.z;
      double var33 = 1.0;
      double var35 = var31 + var33;
      AABB var4 = new AABB(var9, var11, var17, var23, var29, var35);
      double var37 = 0.125;
      AABB var39 = var4.inflate(var37);
      int var40 = 0;

      while (true) {
         int var41 = var40 + 1;
         int var42 = var2.size();
         if (var41 < var42 && var40 <= var3) {
            int var43 = var40;
            Vec3 var44 = (Vec3)var2.get(var43);
            AABB var45 = var39;
            Vec3 var46 = var44;
            if (!var45.contains(var46)) {
               AABB var47 = var39;
               Vec3 var48 = var44;
               int var49 = var40 + 1;
               Vec3 var50 = (Vec3)var2.get(var49);
               if (!var47.clip(var48, var50).isPresent()) {
                  var40++;
                  continue;
               }
            }

            return true;
         }

         return false;
      }
   }

   private boolean a_bool_BlockPos(BlockPos var1) {
      String var2 = this.switchSetting.getValue();
      int var4;
      if (var2.equalsIgnoreCase("None")) {
         int var3;
         if (mc.player.getMainHandItem().is(Items.END_CRYSTAL)) {
            var3 = mc.player.getInventory().getSelectedSlot();
         } else {
            var3 = -1;
         }

         var4 = var3;
      } else {
         boolean var5;
         if (!var2.equalsIgnoreCase("AltSwap") && !var2.equalsIgnoreCase("AltPickup")) {
            var5 = false;
         } else {
            var5 = true;
         }

         boolean var6 = var5;
         Item var7 = Items.END_CRYSTAL;
         int var8 = InventoryUtil.c;
         int var9;
         if (var6) {
            var9 = InventoryUtil.f;
         } else {
            var9 = InventoryUtil.d;
         }

         var4 = InventoryUtil.a_int_Item_int_int(var7, var8, var9);
      }

      if (var4 == -1) {
         return false;
      }

      BlockPos var10 = var1.below();
      Direction var11 = BlockPlaceUtil.a(var10);
      if (var11 == null) {
         var11 = Direction.UP;
      }

      int var12 = mc.player.getInventory().getSelectedSlot();
      String var13 = var2;
      int var14 = var4;
      int var15 = var12;
      if (!InventoryUtil.a_bool_String_int_int(var13, var14, var15)) {
         return false;
      }

      if (this.rotate.getValue()) {
         Vec3 var16 = Vec3.atCenterOf(var10);
         double var17 = var11.getStepX();
         double var19 = 0.5;
         double var21 = var17 * var19;
         double var23 = var11.getStepY();
         double var25 = 0.5;
         double var27 = var23 * var25;
         double var29 = var11.getStepZ();
         double var31 = 0.5;
         double var33 = var29 * var31;
         float[] var35 = RotationMathUtil.a(var16.add(var21, var27, var33));
         Night.rotationManager.sendPosRotPacket(var35);
      }

      BlockPos var36 = var10;
      Direction var37 = var11;
      BlockPlaceUtil.a_void_BlockPos_Direction_bool(var36, var37, false);
      String var38 = var2;
      int var39 = var4;
      int var40 = var12;
      InventoryUtil.b(var38, var39, var40);
      return true;
   }

   private boolean b(Vec3 var1, Vec3 var2, Entity var3) {
      boolean var4;
      if (mc.level.clip(new ClipContext(var1, var2, Block.COLLIDER, Fluid.NONE, var3)).getType() == Type.MISS) {
         var4 = true;
      } else {
         var4 = false;
      }

      return var4;
   }

   private void b(String var1) {
      if (this.debug.getValue()) {
         String var2 = this.getName();
         Night.chatManager.sendTaggedMessage(var1, var2);
      }
   }

   private static String a_String_Vec3(Vec3 var0) {
      Object[] var1 = new Object[3];
      Double var2 = var0.x;
      var1[0] = var2;
      Double var3 = var0.y;
      var1[1] = var3;
      Double var4 = var0.z;
      var1[2] = var4;
      return String.format("%.1f %.1f %.1f", var1);
   }

   static final class CandidateBlock {
      BlockPos a;
   }

   record PearlPrediction(List<Vec3> list, List<Vec3> list2, Vec3 vec, int count) {
   }
}
