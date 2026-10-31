package night.module.impl.combat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.Plane;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.CarpetBlock;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.ScaffoldingBlock;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.TallFlowerBlock;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.TripWireBlock;
import net.minecraft.world.level.block.TripWireHookBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.WebBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.events.EventTarget;
import night.events.impl.TickEvent;
import night.module.Module;
import night.module.ModuleInfo;
import night.setting.impl.BooleanSetting;
import night.setting.impl.ModeSetting;
import night.setting.impl.NumberSetting;
import night.util.inventory.InventoryUtil;
import night.util.render.PlaceRenderEntry;
import night.util.rotation.RotationMathUtil;
import night.util.world.BlockInteractionUtil;
import night.util.world.BlockPlaceUtil;
import night.util.world.DamageUtil;
import night.util.world.EntityUtil;
import night.util.world.PacketAndRenderUtil;

@ModuleInfo(name = "PistonKick", description = "Pushes players out of holes or solid blocks using pistons.", category = Module.Category.COMBAT)
public final class PistonKick extends Module {
   public static PistonKick e;
   public NumberSetting delay;
   public NumberSetting pushDelay;
   public ModeSetting power;
   public BooleanSetting airPlace;
   public BooleanSetting s112;
   public NumberSetting targetRange;
   public BooleanSetting inAirTarget;
   public ModeSetting rotate;
   public BooleanSetting breakCrystal;
   public NumberSetting placeRange;
   public NumberSetting breakRange;
   public NumberSetting maxLocalDamage;
   public BooleanSetting safety;
   public ModeSetting swap;
   public BooleanSetting consecutive;
   public BooleanSetting merge;
   public BooleanSetting pauseOnEat;
   public BooleanSetting strictDirection;
   public BooleanSetting x;
   private BlockPos y;
   private BlockPos z;
   private BlockPos A;
   private BlockPos B;
   private final List<BlockPos> C;
   private Direction D;
   private Player E;
   private long F;
   private long G;
   private long H;

   public PistonKick() {
      Integer var1 = 1;
      Integer var2 = 0;
      Integer var3 = 10;
      this.delay = new NumberSetting("Delay", "Delay between pushes (ticks)", var1, var2, var3);
      Integer var4 = 150;
      Integer var5 = 50;
      Integer var6 = 500;
      this.pushDelay = new NumberSetting("PushDelay", "Duration to hold piston extended before retracting (ms)", var4, var5, var6);
      String[] var7 = new String[]{"Redstone", "Lever"};
      this.power = new ModeSetting("Power", "How the piston is powered.", "Redstone", var7);
      this.airPlace = new BooleanSetting("AirPlace", "Allow placing when not on ground.", false);
      this.s112 = new BooleanSetting("1.12", "Place piston and power in the same tick.", true);
      Float var8 = 5.0F;
      Float var9 = 1.0F;
      Float var10 = 6.0F;
      this.targetRange = new NumberSetting("TargetRange", "Target range.", var8, var9, var10);
      this.inAirTarget = new BooleanSetting("InAirTarget", "Target players in the air.", true);
      String[] var11 = new String[]{"None", "Normal", "Silent"};
      this.rotate = new ModeSetting("Rotate", "Silently rotate when placing.", "Silent", var11);
      this.breakCrystal = new BooleanSetting("BreakCrystal", "Break end crystals blocking piston path.", false);
      Float var12 = 5.0F;
      Float var13 = 1.0F;
      Float var14 = 6.0F;
      this.placeRange = new NumberSetting("PlaceRange", "Block place range.", var12, var13, var14);
      BooleanSetting var16 = this.breakCrystal;
      BooleanSetting.BooleanCondition var15 = new BooleanSetting.BooleanCondition(var16, true);
      Float var17 = 4.5F;
      Float var18 = 1.0F;
      Float var19 = 6.0F;
      this.breakRange = new NumberSetting("BreakRange", "Crystal break range.", var15, var17, var18, var19);
      BooleanSetting var21 = this.breakCrystal;
      BooleanSetting.BooleanCondition var20 = new BooleanSetting.BooleanCondition(var21, true);
      Float var22 = 8.0F;
      Float var23 = 0.0F;
      Float var24 = 20.0F;
      this.maxLocalDamage = new NumberSetting("MaxLocalDamage", "Max self-damage allowed to break a crystal.", var20, var22, var23, var24);
      BooleanSetting var26 = this.breakCrystal;
      BooleanSetting.BooleanCondition var25 = new BooleanSetting.BooleanCondition(var26, true);
      this.safety = new BooleanSetting("Safety", "Never break a crystal that would kill you.", var25, true);
      this.swap = new ModeSetting("Swap", "How to swap to required items.", "Silent", InventoryUtil.a);
      this.consecutive = new BooleanSetting("Consecutive", "Keep targeting the same player consecutively.", true);
      this.merge = new BooleanSetting("Merge", "Burst piston and power packets together.", false);
      this.pauseOnEat = new BooleanSetting("PauseOnEat", "Pause while eating or using items.", false);
      this.strictDirection = new BooleanSetting(
         "StrictDirection", "Use raytrace to verify line-of-sight before placing piston/redstone (required on servers that check raytrace).", false
      );
      this.x = new BooleanSetting("Render", "Render placed blocks using RendersModule.", true);
      this.C = new ArrayList<>();
      long var27 = 0L;
      this.F = var27;
      long var29 = 0L;
      this.G = var29;
      long var31 = 0L;
      this.H = var31;
      e = this;
   }

   public static PistonKick v() {
      return e;
   }

   @Override
   public void onEnable() {
      this.w();
   }

   @Override
   public void onDisable() {
      this.w();
   }

   private void w() {
      this.y = null;
      this.z = null;
      this.A = null;
      this.B = null;
      this.C.clear();
      this.D = null;
      this.E = null;
      long var1 = 0L;
      this.G = var1;
      long var3 = 0L;
      this.H = var3;
   }

   @EventTarget
   public void a_void_TickEvent(TickEvent var1) {
      if (mc.player != null && mc.level != null) {
         if (!this.pauseOnEat.getValue() || !EntityUtil.a()) {
            long var2 = System.currentTimeMillis();
            long var4 = this.F;
            long var6 = var2 - var4;
            long var8 = this.delay.getValue().longValue();
            long var10 = 50L;
            long var12 = var8 * var10;
            if (var6 >= var12) {
               int var14 = this.A();
               int var15 = this.y();
               if (var14 != -1) {
                  int var16 = var15;
                  byte var17 = -1;
                  if (var16 != var17) {
                     label293:
                     if (this.E != null) {
                        if (this.E.isAlive() && !this.E.isSpectator()) {
                           LocalPlayer var18 = mc.player;
                           Player var19 = this.E;
                           double var20 = var18.distanceToSqr(var19);
                           float var22 = this.targetRange.getValue().floatValue();
                           float var23 = this.targetRange.getValue().floatValue();
                           double var24 = var22 * var23;
                           if (!(var20 > var24)) {
                              Player var26 = this.E;
                              List var27 = this.a_List_Player(var26);
                              if (var27.isEmpty()) {
                                 this.w();
                              } else {
                                 if (this.y != null) {
                                    List var28 = var27;
                                    BlockPos var29 = this.y;
                                    if (var28.contains(var29)) {
                                       break label293;
                                    }
                                 }

                                 List var30 = var27;
                                 byte var31 = 0;
                                 BlockPos var32 = (BlockPos)var30.get(var31);
                                 this.y = var32;
                              }
                              break label293;
                           }
                        }

                        this.w();
                     }

                     if (this.E == null) {
                        for (Player var34 : (Iterable<Player>)(Object)this.B()) {
                           if (this.inAirTarget.getValue() || var34.onGround()) {
                              Player var35 = var34;
                              List var36 = this.a_List_Player(var35);
                              if (!var36.isEmpty()) {
                                 Player var37 = var34;
                                 this.E = var37;
                                 List var38 = var36;
                                 byte var39 = 0;
                                 BlockPos var40 = (BlockPos)var38.get(var39);
                                 this.y = var40;
                                 break;
                              }
                           }
                        }
                     }

                     if (this.E != null && this.y != null) {
                        if (this.breakCrystal.getValue()) {
                           Player var41 = this.E;
                           this.d_void_Player(var41);
                        }

                        label315: {
                           if (this.z != null && this.D != null) {
                              BlockPos var42 = this.z;
                              Direction var43 = this.D;
                              boolean var44 = this.a_bool_BlockPos_Direction(var42, var43);
                              BlockPos var45 = this.z;
                              boolean var46 = this.a_bool_BlockPos(var45);
                              BlockPos var47 = this.z;
                              boolean var48 = this.b_bool_BlockPos(var47);
                              String var49 = this.power.getValue();
                              String var50 = "Lever";
                              boolean var51 = var49.equalsIgnoreCase(var50);
                              if (var46) {
                                 long var52 = this.G;
                                 long var54 = 0L;
                                 if (var52 == var54) {
                                    PistonKick var56 = this;
                                    long var57 = var2;
                                    var56.G = var57;
                                 }

                                 long var59 = 50L;
                                 long var61 = this.pushDelay.getValue().longValue();
                                 long var63 = Math.max(var59, var61);
                                 long var65 = var2;
                                 long var67 = this.G;
                                 long var69 = var65 - var67;
                                 long var71 = var63;
                                 if (var69 < var71) {
                                    break label315;
                                 }

                                 long var73 = this.H;
                                 long var75 = 0L;
                                 if (var73 > var75) {
                                    long var77 = var2;
                                    long var79 = this.H;
                                    long var81 = var77 - var79;
                                    long var83 = 50L;
                                    if (var81 < var83) {
                                       break label315;
                                    }
                                 }

                                 if (var51 && this.A != null) {
                                    PistonKick var88 = this;
                                    BlockPos var89 = this.A;
                                    if (var88.e_bool_BlockPos(var89)) {
                                       PistonKick var90 = this;
                                       BlockPos var91 = this.A;
                                       if (var90.f(var91)) {
                                          PistonKick var92 = this;
                                          BlockPos var93 = this.A;
                                          var92.g(var93);
                                          PistonKick var94 = this;
                                          long var95 = 0L;
                                          var94.H = var95;
                                          PistonKick var97 = this;
                                          long var98 = var2;
                                          var97.F = var98;
                                          return;
                                       }
                                    }
                                 }

                                 if (var51 && this.A != null) {
                                    PistonKick var100 = this;
                                    BlockPos var101 = this.A;
                                    if (!var100.f(var101) && var46) {
                                       PistonKick var102 = this;
                                       long var103 = var2;
                                       var102.F = var103;
                                       return;
                                    }
                                 }

                                 PistonKick var105 = this;
                                 Player var106 = this.E;
                                 if (var105.a_List_Player(var106).isEmpty()) {
                                    this.w();
                                    return;
                                 }

                                 PistonKick var107 = this;
                                 Object var108 = null;
                                 var107.z = (BlockPos)var108;
                                 PistonKick var109 = this;
                                 Object var110 = null;
                                 var109.A = (BlockPos)var110;
                                 PistonKick var111 = this;
                                 Object var112 = null;
                                 var111.B = (BlockPos)var112;
                                 PistonKick var113 = this;
                                 Object var114 = null;
                                 var113.D = (Direction)var114;
                                 PistonKick var115 = this;
                                 long var116 = 0L;
                                 var115.G = var116;
                                 PistonKick var118 = this;
                                 long var119 = 0L;
                                 var118.H = var119;
                                 PistonKick var121 = this;
                                 long var122 = var2;
                                 var121.F = var122;
                              } else {
                                 PistonKick var124 = this;
                                 long var125 = 0L;
                                 var124.G = var125;
                              }

                              if (var44 && var48 && !var46) {
                                 PistonKick var127 = this;
                                 long var128 = var2;
                                 var127.F = var128;
                                 return;
                              }

                              boolean var130 = this.airPlace.getValue();
                              if (!var44) {
                                 BlockPos var131 = this.z;
                                 Object var132 = null;
                                 boolean var133 = var130;
                                 if (!BlockPlaceUtil.a(var131, (Predicate<Direction>)var132, var133, true)) {
                                    boolean var150;
                                    label248: {
                                       label311: {
                                          if (!var51 && this.A != null) {
                                             BlockPos var134 = this.A;
                                             BlockPos var135 = this.z.below();
                                             if (var134.equals(var135)) {
                                                BlockPos var136 = this.A;
                                                Object var137 = null;
                                                boolean var138 = var130;
                                                if (BlockPlaceUtil.a(var136, (Predicate<Direction>)var137, var138, true)) {
                                                   break label311;
                                                }

                                                PistonKick var139 = this;
                                                BlockPos var140 = this.A;
                                                if (var139.c_bool_BlockPos(var140)) {
                                                   break label311;
                                                }
                                             }
                                          }

                                          if (!this.C.isEmpty()) {
                                             Stream var141 = this.C.stream();
                                             PistonKick var142 = this;
                                             boolean var143 = var130;
                                             Predicate<BlockPos> var144 = var2x -> {
                                                boolean var3;
                                                if (!BlockPlaceUtil.a(var2x, null, var143, true) && !this.i(var2x)) {
                                                   var3 = false;
                                                } else {
                                                   var3 = true;
                                                }

                                                return var3;
                                             };
                                             if (var141.anyMatch(var144)) {
                                                break label311;
                                             }
                                          }

                                          if (this.B != null) {
                                             BlockPos var145 = this.B;
                                             Object var146 = null;
                                             boolean var147 = var130;
                                             if (BlockPlaceUtil.a(var145, (Predicate<Direction>)var146, var147, true)) {
                                                break label311;
                                             }

                                             PistonKick var148 = this;
                                             BlockPos var149 = this.B;
                                             if (var148.i(var149)) {
                                                break label311;
                                             }
                                          }

                                          var150 = false;
                                          break label248;
                                       }

                                       var150 = true;
                                    }

                                    if (!var150) {
                                       this.w();
                                    }
                                 }
                              }
                           }

                           if (this.z == null || this.D == null) {
                              PistonKick var151 = this;
                              Player var152 = this.E;
                              if (!var151.c_bool_Player(var152)) {
                                 return;
                              }
                           }

                           if (this.z != null && this.D != null) {
                              boolean var153 = this.airPlace.getValue();
                              String var154 = this.power.getValue();
                              String var155 = "Lever";
                              boolean var156 = var154.equalsIgnoreCase(var155);

                              for (BlockPos var158 : (Iterable<BlockPos>)(Object)this.C) {
                                 PistonKick var159 = this;
                                 BlockPos var160 = var158;
                                 if (!var159.i(var160)) {
                                    BlockPos var161 = var158;
                                    Object var162 = null;
                                    boolean var163 = var153;
                                    if (BlockPlaceUtil.a(var161, (Predicate<Direction>)var162, var163, true)) {
                                       int var164 = this.z();
                                       byte var165 = -1;
                                       if (var164 != var165) {
                                          PistonKick var166 = this;
                                          int var167 = var164;
                                          BlockPos var168 = var158;
                                          var166.a_void_int_BlockPos_Direction(var167, var168, null);
                                          PistonKick var169 = this;
                                          long var170 = var2;
                                          var169.F = var170;
                                          if (!this.s112.getValue() && !this.merge.getValue()) {
                                             return;
                                          }
                                       }
                                    }
                                 }
                              }

                              if (this.B != null) {
                                 PistonKick var172 = this;
                                 BlockPos var173 = this.B;
                                 if (!var172.i(var173)) {
                                    List var174 = this.C;
                                    BlockPos var175 = this.B;
                                    if (!var174.contains(var175)) {
                                       BlockPos var176 = this.B;
                                       Object var177 = null;
                                       boolean var178 = var153;
                                       if (BlockPlaceUtil.a(var176, (Predicate<Direction>)var177, var178, true)) {
                                          int var179 = this.z();
                                          byte var180 = -1;
                                          if (var179 != var180) {
                                             PistonKick var181 = this;
                                             int var182 = var179;
                                             BlockPos var183 = this.B;
                                             var181.a_void_int_BlockPos_Direction(var182, var183, null);
                                             PistonKick var184 = this;
                                             long var185 = var2;
                                             var184.F = var185;
                                             if (!this.s112.getValue() && !this.merge.getValue()) {
                                                return;
                                             }
                                          }
                                       }
                                    }
                                 }
                              }

                              boolean var189;
                              label214: {
                                 if (!var156 && this.A != null) {
                                    BlockPos var187 = this.A;
                                    BlockPos var188 = this.z.below();
                                    if (var187.equals(var188)) {
                                       var189 = true;
                                       break label214;
                                    }
                                 }

                                 var189 = false;
                              }

                              if (var189) {
                                 PistonKick var190 = this;
                                 BlockPos var191 = this.A;
                                 if (!var190.c_bool_BlockPos(var191)) {
                                    BlockPos var192 = this.A;
                                    Object var193 = null;
                                    boolean var194 = var153;
                                    if (BlockPlaceUtil.a(var192, (Predicate<Direction>)var193, var194, true)) {
                                       PistonKick var195 = this;
                                       int var196 = var15;
                                       BlockPos var197 = this.A;
                                       var195.a_void_int_BlockPos_Direction(var196, var197, null);
                                       PistonKick var198 = this;
                                       long var199 = var2;
                                       var198.F = var199;
                                       if (!this.s112.getValue() && !this.merge.getValue()) {
                                          return;
                                       }
                                    }
                                 }
                              }

                              PistonKick var201 = this;
                              BlockPos var202 = this.z;
                              Direction var203 = this.D;
                              boolean var204;
                              if (!var201.a_bool_BlockPos_Direction(var202, var203)) {
                                 var204 = true;
                              } else {
                                 var204 = false;
                              }

                              if (var204) {
                                 BlockPos var205 = this.z;
                                 Object var206 = null;
                                 boolean var207 = var153;
                                 if (BlockPlaceUtil.a(var205, (Predicate<Direction>)var206, var207, true)) {
                                    PistonKick var208 = this;
                                    int var209 = var14;
                                    BlockPos var210 = this.z;
                                    Direction var211 = this.D;
                                    var208.a_void_int_BlockPos_Direction(var209, var210, var211);
                                    PistonKick var212 = this;
                                    long var213 = var2;
                                    var212.F = var213;
                                    if (!this.s112.getValue() && !this.merge.getValue()) {
                                       return;
                                    }
                                 }
                              }

                              if (this.A != null) {
                                 PistonKick var215 = this;
                                 BlockPos var216 = this.z;
                                 if (!var215.b_bool_BlockPos(var216)) {
                                    PistonKick var217 = this;
                                    BlockPos var218 = this.A;
                                    if (!var217.c_bool_BlockPos(var218)) {
                                       boolean var222;
                                       label199: {
                                          if (var156) {
                                             PistonKick var219 = this;
                                             BlockPos var220 = this.z;
                                             Direction var221 = this.D;
                                             if (!var219.a_bool_BlockPos_Direction(var220, var221) && !var153) {
                                                var222 = false;
                                                break label199;
                                             }
                                          }

                                          var222 = true;
                                       }

                                       if (var222) {
                                          BlockPos var223 = this.A;
                                          Object var224 = null;
                                          boolean var225 = var153;
                                          if (BlockPlaceUtil.a(var223, (Predicate<Direction>)var224, var225, true)) {
                                             PistonKick var226 = this;
                                             int var227 = var15;
                                             BlockPos var228 = this.A;
                                             var226.a_void_int_BlockPos_Direction(var227, var228, null);
                                             PistonKick var229 = this;
                                             long var230 = var2;
                                             var229.F = var230;
                                             if (var156) {
                                                PistonKick var232 = this;
                                                BlockPos var233 = this.A;
                                                var232.g(var233);
                                                PistonKick var234 = this;
                                                long var235 = var2;
                                                var234.H = var235;
                                             }

                                             return;
                                          }
                                       }

                                       return;
                                    }
                                 }
                              }

                              if (var156 && this.A != null) {
                                 PistonKick var237 = this;
                                 BlockPos var238 = this.A;
                                 if (var237.e_bool_BlockPos(var238)) {
                                    PistonKick var239 = this;
                                    BlockPos var240 = this.A;
                                    if (!var239.f(var240)) {
                                       PistonKick var241 = this;
                                       BlockPos var242 = this.z;
                                       if (!var241.a_bool_BlockPos(var242)) {
                                          long var243 = this.G;
                                          long var245 = 0L;
                                          if (var243 == var245) {
                                             PistonKick var247 = this;
                                             BlockPos var248 = this.A;
                                             var247.g(var248);
                                             PistonKick var249 = this;
                                             long var250 = var2;
                                             var249.H = var250;
                                             PistonKick var252 = this;
                                             long var253 = var2;
                                             var252.F = var253;
                                          }
                                       }
                                    }
                                 }
                              }

                              return;
                           }

                           return;
                        }

                        PistonKick var85 = this;
                        long var86 = var2;
                        var85.F = var86;
                        return;
                     }

                     this.w();
                     return;
                  }
               }

               this.w();
            }
         }
      }
   }

   private boolean c_bool_Player(Player var1) {
      List<BlockPos> var2 = this.a_List_Player(var1);
      if (var2.isEmpty()) {
         return false;
      }

      if (var2.size() >= 2) {
         Axis var3 = mc.player.getDirection().getAxis();
         var2.sort((var1x, var2x) -> {
            boolean var7;
            label24: {
               label27: {
                  if (var3 == Axis.Z) {
                     int var3x = var1x.getZ();
                     int var4 = var2x.getZ();
                     if (var3x != var4) {
                        break label27;
                     }
                  }

                  if (var3 == Axis.X) {
                     int var5x = var1x.getX();
                     int var6x = var2x.getX();
                     if (var5x != var6x) {
                        break label27;
                     }
                  }

                  var7 = false;
                  break label24;
               }

               var7 = true;
            }

            byte var8;
            if (var7) {
               var8 = -1;
            } else {
               var8 = 1;
            }

            return var8;
         });
      }

      for (BlockPos var5 : var2) {
         if (this.a_bool_Player_BlockPos(var1, var5)) {
            BlockPos var6 = var5;
            this.y = var6;
            return true;
         }
      }

      return false;
   }

   private boolean a_bool_Player_BlockPos(Player var1, BlockPos var2) {
      ArrayList var3 = new ArrayList();
      ArrayList var4 = var3;
      Direction var5 = mc.player.getDirection();
      ArrayList var6 = var4;
      Direction var7 = var5;
      var6.add(var7);

      for (Direction var9 : Plane.HORIZONTAL) {
         Direction var10 = var5;
         if (var9 != var10) {
            ArrayList var11 = var4;
            Direction var12 = var9;
            var11.add(var12);
         }
      }

      BlockPos var13 = var2.above();
      boolean var14 = this.airPlace.getValue();
      String var15 = this.power.getValue();
      String var16 = "Lever";
      boolean var17 = var15.equalsIgnoreCase(var16);
      Iterator var18 = var4.iterator();

      while (true) {
         Direction var19;
         BlockPos var24;
         while (true) {
            if (!var18.hasNext()) {
               return false;
            }

            var19 = (Direction)var18.next();
            BlockPos var20 = var2;
            Direction var21 = var19;
            if (this.b_bool_BlockPos_Direction(var20, var21)) {
               BlockPos var22 = var13;
               Direction var23 = var19.getOpposite();
               var24 = var22.relative(var23);
               ClientLevel var25 = mc.level;
               BlockPos var26 = var13;
               if (var25.getBlockState(var26).isAir()) {
                  break;
               }

               ClientLevel var27 = mc.level;
               BlockPos var28 = var13;
               if (var27.getBlockState(var28).canBeReplaced()) {
                  break;
               }
            }
         }

         PistonKick var29 = this;
         BlockPos var30 = var24;
         if (var29.h(var30)) {
            boolean var34;
            boolean var39;
            label252: {
               PistonKick var31 = this;
               BlockPos var32 = var24;
               Direction var33 = var19;
               var34 = var31.a_bool_BlockPos_Direction(var32, var33);
               ClientLevel var35 = mc.level;
               BlockPos var36 = var24;
               if (!var35.getBlockState(var36).isAir()) {
                  ClientLevel var37 = mc.level;
                  BlockPos var38 = var24;
                  if (!var37.getBlockState(var38).canBeReplaced()) {
                     var39 = false;
                     break label252;
                  }
               }

               var39 = true;
            }

            boolean var40 = var39;
            if (var34 || var40) {
               boolean var44;
               label263: {
                  if (!var34) {
                     BlockPos var41 = var24;
                     Object var42 = null;
                     boolean var43 = var14;
                     if (!BlockPlaceUtil.a(var41, (Predicate<Direction>)var42, var43, true)) {
                        var44 = false;
                        break label263;
                     }
                  }

                  var44 = true;
               }

               if (var44) {
                  PistonKick var45 = this;
                  BlockPos var46 = var24;
                  if (var45.b_bool_BlockPos(var46)) {
                     PistonKick var47 = this;
                     Object var48 = null;
                     var47.B = (BlockPos)var48;
                     PistonKick var49 = this;
                     BlockPos var50 = var24;
                     var49.z = var50;
                     PistonKick var51 = this;
                     Object var52 = null;
                     var51.A = (BlockPos)var52;
                     PistonKick var53 = this;
                     Direction var54 = var19;
                     var53.D = var54;
                     return true;
                  }

                  if (var17) {
                     BlockPos var55;
                     boolean var58;
                     label397: {
                        var55 = var24.below();
                        if (!var14) {
                           PistonKick var56 = this;
                           BlockPos var57 = var55;
                           if (!var56.i(var57)) {
                              var58 = true;
                              break label397;
                           }
                        }

                        var58 = false;
                     }

                     boolean var131 = var58;
                     ArrayList var132 = new ArrayList();
                     ArrayList var133 = var132;
                     BlockPos var134 = var24;
                     Direction var135 = var19.getOpposite();
                     BlockPos var136 = var134.relative(var135);
                     BlockPos var137 = var136.below();
                     ArrayList var138 = new ArrayList();
                     ArrayList var139 = var138;
                     if (var131) {
                        ArrayList var140 = var139;
                        BlockPos var141 = var55;
                        var140.add(var141);
                     }

                     if (!var14) {
                        PistonKick var142 = this;
                        BlockPos var143 = var137;
                        if (!var142.i(var143)) {
                           ArrayList var144 = var139;
                           BlockPos var145 = var137;
                           var144.add(var145);
                        }
                     }

                     ArrayList var146 = var133;
                     BlockPos var148 = var136;
                     ArrayList var149 = var139;
                     PistonKick.PushPlan var147 = new PistonKick.PushPlan(var148, var149);
                     var146.add(var147);
                     BlockPos var150 = var24;
                     Direction var151 = var19.getCounterClockWise();
                     BlockPos var152 = var150.relative(var151);
                     BlockPos var153 = var152.below();
                     ArrayList var154 = new ArrayList();
                     ArrayList var155 = var154;
                     if (var131) {
                        ArrayList var156 = var155;
                        BlockPos var157 = var55;
                        var156.add(var157);
                     }

                     if (!var14) {
                        PistonKick var158 = this;
                        BlockPos var159 = var153;
                        if (!var158.i(var159)) {
                           ArrayList var160 = var155;
                           BlockPos var161 = var153;
                           var160.add(var161);
                        }
                     }

                     ArrayList var162 = var133;
                     BlockPos var164 = var152;
                     ArrayList var165 = var155;
                     PistonKick.PushPlan var163 = new PistonKick.PushPlan(var164, var165);
                     var162.add(var163);
                     PistonKick var166 = this;
                     BlockPos var167 = var152;
                     if (var166.i(var167)) {
                        BlockPos var168 = var152.above();
                        ArrayList var169 = new ArrayList();
                        ArrayList var170 = var169;
                        if (var131) {
                           ArrayList var171 = var170;
                           BlockPos var172 = var55;
                           var171.add(var172);
                        }

                        ArrayList var173 = var133;
                        BlockPos var175 = var168;
                        ArrayList var176 = var170;
                        PistonKick.PushPlan var174 = new PistonKick.PushPlan(var175, var176);
                        var173.add(var174);
                     }

                     BlockPos var177 = var24;
                     Direction var178 = var19.getClockWise();
                     BlockPos var179 = var177.relative(var178);
                     BlockPos var180 = var179.below();
                     ArrayList var181 = new ArrayList();
                     ArrayList var182 = var181;
                     if (var131) {
                        ArrayList var183 = var182;
                        BlockPos var184 = var55;
                        var183.add(var184);
                     }

                     if (!var14) {
                        PistonKick var185 = this;
                        BlockPos var186 = var180;
                        if (!var185.i(var186)) {
                           ArrayList var187 = var182;
                           BlockPos var188 = var180;
                           var187.add(var188);
                        }
                     }

                     ArrayList var189 = var133;
                     BlockPos var191 = var179;
                     ArrayList var192 = var182;
                     PistonKick.PushPlan var190 = new PistonKick.PushPlan(var191, var192);
                     var189.add(var190);
                     PistonKick var193 = this;
                     BlockPos var194 = var179;
                     if (var193.i(var194)) {
                        BlockPos var195 = var179.above();
                        ArrayList var196 = new ArrayList();
                        ArrayList var197 = var196;
                        if (var131) {
                           ArrayList var198 = var197;
                           BlockPos var199 = var55;
                           var198.add(var199);
                        }

                        ArrayList var200 = var133;
                        BlockPos var202 = var195;
                        ArrayList var203 = var197;
                        PistonKick.PushPlan var201 = new PistonKick.PushPlan(var202, var203);
                        var200.add(var201);
                     }

                     PistonKick var204 = this;
                     BlockPos var205 = var136;
                     if (var204.i(var205)) {
                        BlockPos var206 = var24;
                        Direction var207 = var19.getOpposite();
                        byte var208 = 2;
                        BlockPos var209 = var206.relative(var207, var208);
                        ArrayList var210 = new ArrayList();
                        ArrayList var211 = var210;
                        if (var131) {
                           ArrayList var212 = var211;
                           BlockPos var213 = var55;
                           var212.add(var213);
                        }

                        ArrayList var214 = var133;
                        BlockPos var216 = var209;
                        ArrayList var217 = var211;
                        PistonKick.PushPlan var215 = new PistonKick.PushPlan(var216, var217);
                        var214.add(var215);
                        BlockPos var218 = var136.above();
                        ArrayList var219 = new ArrayList();
                        ArrayList var220 = var219;
                        if (var131) {
                           ArrayList var221 = var220;
                           BlockPos var222 = var55;
                           var221.add(var222);
                        }

                        ArrayList var223 = var133;
                        BlockPos var225 = var218;
                        ArrayList var226 = var220;
                        PistonKick.PushPlan var224 = new PistonKick.PushPlan(var225, var226);
                        var223.add(var224);
                     }

                     BlockPos var227 = var24;
                     Direction var228 = var19.getOpposite();
                     BlockPos var229 = var227.relative(var228).below();
                     ArrayList var230 = new ArrayList();
                     ArrayList var231 = var230;
                     if (var131) {
                        ArrayList var232 = var231;
                        BlockPos var233 = var55;
                        var232.add(var233);
                     }

                     ArrayList var234 = var133;
                     BlockPos var236 = var229;
                     ArrayList var237 = var231;
                     PistonKick.PushPlan var235 = new PistonKick.PushPlan(var236, var237);
                     var234.add(var235);

                     for (PistonKick.PushPlan var239 : (Iterable<PistonKick.PushPlan>)(Object)var133) {
                        BlockPos var240 = var239.pos;
                        BlockPos var241 = var240;
                        BlockPos var242 = var24;
                        Direction var243 = var19;
                        BlockPos var244 = var242.relative(var243);
                        if (!var241.equals(var244)) {
                           BlockPos var245 = var240;
                           BlockPos var246 = var13;
                           if (!var245.equals(var246)) {
                              BlockPos var247 = var240;
                              BlockPos var248 = var13.below();
                              if (!var247.equals(var248)) {
                                 BlockPos var249 = var240;
                                 BlockPos var250 = var2;
                                 Direction var251 = var19;
                                 BlockPos var252 = var250.relative(var251);
                                 if (!var249.equals(var252)) {
                                    BlockPos var253 = var240;
                                    BlockPos var254 = var13;
                                    Direction var255 = var19;
                                    BlockPos var256 = var254.relative(var255);
                                    if (!var253.equals(var256)) {
                                       PistonKick var257 = this;
                                       BlockPos var258 = var240;
                                       if (var257.h(var258)) {
                                          boolean var265;
                                          label365: {
                                             PistonKick var259 = this;
                                             BlockPos var260 = var240;
                                             if (!var259.e_bool_BlockPos(var260)) {
                                                ClientLevel var261 = mc.level;
                                                BlockPos var262 = var240;
                                                if (!var261.getBlockState(var262).isAir()) {
                                                   ClientLevel var263 = mc.level;
                                                   BlockPos var264 = var240;
                                                   if (!var263.getBlockState(var264).canBeReplaced()) {
                                                      var265 = false;
                                                      break label365;
                                                   }
                                                }
                                             }

                                             var265 = true;
                                          }

                                          if (var265) {
                                             boolean var266 = true;
                                             Iterator var267 = var239.list.iterator();

                                             label389: {
                                                while (true) {
                                                   if (!var267.hasNext()) {
                                                      break label389;
                                                   }

                                                   BlockPos var268 = (BlockPos)var267.next();
                                                   BlockPos var269 = var268;
                                                   BlockPos var270 = var13;
                                                   if (var269.equals(var270)) {
                                                      break;
                                                   }

                                                   BlockPos var271 = var268;
                                                   BlockPos var272 = var13.below();
                                                   if (var271.equals(var272)) {
                                                      break;
                                                   }

                                                   BlockPos var273 = var268;
                                                   BlockPos var274 = var2;
                                                   Direction var275 = var19;
                                                   BlockPos var276 = var274.relative(var275);
                                                   if (var273.equals(var276)) {
                                                      break;
                                                   }

                                                   BlockPos var277 = var268;
                                                   BlockPos var278 = var13;
                                                   Direction var279 = var19;
                                                   BlockPos var280 = var278.relative(var279);
                                                   if (var277.equals(var280)) {
                                                      break;
                                                   }

                                                   PistonKick var281 = this;
                                                   BlockPos var282 = var268;
                                                   if (!var281.h(var282)) {
                                                      var266 = false;
                                                      break label389;
                                                   }

                                                   PistonKick var283 = this;
                                                   BlockPos var284 = var268;
                                                   if (!var283.i(var284)) {
                                                      BlockPos var285 = var268;
                                                      Object var286 = null;
                                                      boolean var287 = var14;
                                                      boolean var288 = true;
                                                      if (!BlockPlaceUtil.a(var285, (Predicate<Direction>)var286, var287, var288)) {
                                                         var266 = false;
                                                         break label389;
                                                      }
                                                   }
                                                }

                                                var266 = false;
                                             }

                                             if (var266) {
                                                this.C.clear();
                                                List var418 = this.C;
                                                List var419 = var239.list;
                                                var418.addAll(var419);
                                                PistonKick var420 = this;
                                                BlockPos var421;
                                                if (var239.list.isEmpty()) {
                                                   var421 = null;
                                                } else {
                                                   List var422 = var239.list;
                                                   byte var423 = 0;
                                                   var421 = (BlockPos)var422.get(var423);
                                                }

                                                var420.B = var421;
                                                PistonKick var424 = this;
                                                BlockPos var425 = var24;
                                                var424.z = var425;
                                                PistonKick var426 = this;
                                                BlockPos var427 = var240;
                                                var426.A = var427;
                                                PistonKick var428 = this;
                                                Direction var429 = var19;
                                                var428.D = var429;
                                                return true;
                                             }
                                          }
                                       }
                                    }
                                 }
                              }
                           }
                        }
                     }
                  } else {
                     PistonKick var59 = this;
                     BlockPos var60 = var24;
                     BlockPos var61 = var13;
                     BlockPos var62 = var2;
                     Direction var63 = var19;
                     BlockPos var64 = var62.relative(var63);
                     Direction var65 = var19;
                     BlockPos var66 = var59.a(var60, var61, var64, var65);
                     if (var66 != null) {
                        PistonKick var67 = this;
                        Object var68 = null;
                        var67.B = (BlockPos)var68;
                        PistonKick var69 = this;
                        BlockPos var70 = var24;
                        var69.z = var70;
                        PistonKick var71 = this;
                        BlockPos var72 = var66;
                        var71.A = var72;
                        PistonKick var73 = this;
                        Direction var74 = var19;
                        var73.D = var74;
                        return true;
                     }

                     Direction[] var75 = new Direction[5];
                     byte var76 = 0;
                     Direction var77 = Direction.UP;
                     var75[var76] = var77;
                     byte var78 = 1;
                     Direction var79 = var19.getOpposite();
                     var75[var78] = var79;
                     byte var80 = 2;
                     Direction var81 = var19.getClockWise();
                     var75[var80] = var81;
                     byte var82 = 3;
                     Direction var83 = var19.getCounterClockWise();
                     var75[var82] = var83;
                     byte var84 = 4;
                     Direction var85 = Direction.DOWN;
                     var75[var84] = var85;
                     Direction[] var86 = var75;
                     int var87 = var86.length;
                     int var88 = 0;

                     while (true) {
                        int var89 = var88;
                        int var90 = var87;
                        if (var89 >= var90) {
                           break;
                        }

                        BlockPos var96;
                        label466: {
                           Direction[] var91 = var86;
                           int var92 = var88;
                           Direction var93 = var91[var92];
                           BlockPos var94 = var24;
                           Direction var95 = var93;
                           var96 = var94.relative(var95);
                           BlockPos var97 = var96;
                           BlockPos var98 = var24;
                           Direction var99 = var19;
                           BlockPos var100 = var98.relative(var99);
                           if (!var97.equals(var100)) {
                              BlockPos var101 = var96;
                              BlockPos var102 = var13;
                              if (!var101.equals(var102)) {
                                 BlockPos var103 = var96;
                                 BlockPos var104 = var13.below();
                                 if (!var103.equals(var104)) {
                                    BlockPos var105 = var96;
                                    BlockPos var106 = var2;
                                    Direction var107 = var19;
                                    BlockPos var108 = var106.relative(var107);
                                    if (!var105.equals(var108)) {
                                       BlockPos var109 = var96;
                                       BlockPos var110 = var13;
                                       Direction var111 = var19;
                                       BlockPos var112 = var110.relative(var111);
                                       if (!var109.equals(var112)) {
                                          PistonKick var113 = this;
                                          BlockPos var114 = var96;
                                          label409:
                                          if (var113.h(var114)) {
                                             ClientLevel var115 = mc.level;
                                             BlockPos var116 = var96;
                                             if (!var115.getBlockState(var116).isAir()) {
                                                ClientLevel var117 = mc.level;
                                                BlockPos var118 = var96;
                                                if (!var117.getBlockState(var118).canBeReplaced()) {
                                                   break label409;
                                                }
                                             }

                                             if (var14) {
                                                break label466;
                                             }

                                             BlockPos var119 = var96;
                                             Object var120 = null;
                                             boolean var121 = false;
                                             boolean var122 = true;
                                             if (BlockPlaceUtil.a(var119, (Predicate<Direction>)var120, var121, var122)) {
                                                break label466;
                                             }
                                          }
                                       }
                                    }
                                 }
                              }
                           }

                           var88++;
                           continue;
                        }

                        PistonKick var123 = this;
                        Object var124 = null;
                        var123.B = (BlockPos)var124;
                        PistonKick var125 = this;
                        BlockPos var126 = var24;
                        var125.z = var126;
                        PistonKick var127 = this;
                        BlockPos var128 = var96;
                        var127.A = var128;
                        PistonKick var129 = this;
                        Direction var130 = var19;
                        var129.D = var130;
                        return true;
                     }
                  }
               }

               if (!var14 && var40) {
                  BlockPos var289 = var24.below();
                  PistonKick var290 = this;
                  BlockPos var291 = var289;
                  if (var290.h(var291)) {
                     if (!var17) {
                        BlockPos var404 = var289;
                        Object var405 = null;
                        boolean var406 = false;
                        boolean var407 = true;
                        if (!BlockPlaceUtil.a(var404, (Predicate<Direction>)var405, var406, var407)) {
                           PistonKick var408 = this;
                           BlockPos var409 = var289;
                           if (!var408.c_bool_BlockPos(var409)) {
                              continue;
                           }
                        }

                        this.C.clear();
                        PistonKick var410 = this;
                        Object var411 = null;
                        var410.B = (BlockPos)var411;
                        PistonKick var412 = this;
                        BlockPos var413 = var24;
                        var412.z = var413;
                        PistonKick var414 = this;
                        BlockPos var415 = var289;
                        var414.A = var415;
                        PistonKick var416 = this;
                        Direction var417 = var19;
                        var416.D = var417;
                        return true;
                     } else {
                        BlockPos var292 = var289;
                        Object var293 = null;
                        boolean var294 = false;
                        boolean var295 = true;
                        if (!BlockPlaceUtil.a(var292, (Predicate<Direction>)var293, var294, var295)) {
                           PistonKick var296 = this;
                           BlockPos var297 = var289;
                           if (!var296.i(var297)) {
                              continue;
                           }
                        }

                        ArrayList var298 = new ArrayList();
                        ArrayList var299 = var298;
                        BlockPos var300 = var24;
                        Direction var301 = var19.getOpposite();
                        BlockPos var302 = var300.relative(var301);
                        BlockPos var303 = var302.below();
                        ArrayList var304 = new ArrayList();
                        ArrayList var305 = var304;
                        PistonKick var306 = this;
                        BlockPos var307 = var289;
                        if (!var306.i(var307)) {
                           ArrayList var308 = var305;
                           BlockPos var309 = var289;
                           var308.add(var309);
                        }

                        PistonKick var310 = this;
                        BlockPos var311 = var303;
                        if (!var310.i(var311)) {
                           ArrayList var312 = var305;
                           BlockPos var313 = var303;
                           var312.add(var313);
                        }

                        ArrayList var314 = var299;
                        BlockPos var316 = var302;
                        ArrayList var317 = var305;
                        PistonKick.PushPlan var315 = new PistonKick.PushPlan(var316, var317);
                        var314.add(var315);
                        BlockPos var318 = var24;
                        Direction var319 = var19.getCounterClockWise();
                        BlockPos var320 = var318.relative(var319);
                        BlockPos var321 = var320.below();
                        ArrayList var322 = new ArrayList();
                        ArrayList var323 = var322;
                        PistonKick var324 = this;
                        BlockPos var325 = var289;
                        if (!var324.i(var325)) {
                           ArrayList var326 = var323;
                           BlockPos var327 = var289;
                           var326.add(var327);
                        }

                        PistonKick var328 = this;
                        BlockPos var329 = var321;
                        if (!var328.i(var329)) {
                           ArrayList var330 = var323;
                           BlockPos var331 = var321;
                           var330.add(var331);
                        }

                        ArrayList var332 = var299;
                        BlockPos var334 = var320;
                        ArrayList var335 = var323;
                        PistonKick.PushPlan var333 = new PistonKick.PushPlan(var334, var335);
                        var332.add(var333);
                        BlockPos var336 = var24;
                        Direction var337 = var19.getClockWise();
                        BlockPos var338 = var336.relative(var337);
                        BlockPos var339 = var338.below();
                        ArrayList var340 = new ArrayList();
                        ArrayList var341 = var340;
                        PistonKick var342 = this;
                        BlockPos var343 = var289;
                        if (!var342.i(var343)) {
                           ArrayList var344 = var341;
                           BlockPos var345 = var289;
                           var344.add(var345);
                        }

                        PistonKick var346 = this;
                        BlockPos var347 = var339;
                        if (!var346.i(var347)) {
                           ArrayList var348 = var341;
                           BlockPos var349 = var339;
                           var348.add(var349);
                        }

                        ArrayList var350 = var299;
                        BlockPos var352 = var338;
                        ArrayList var353 = var341;
                        PistonKick.PushPlan var351 = new PistonKick.PushPlan(var352, var353);
                        var350.add(var351);

                        for (PistonKick.PushPlan var355 : (Iterable<PistonKick.PushPlan>)(Object)var299) {
                           BlockPos var356 = var355.pos;
                           BlockPos var357 = var24;
                           Direction var358 = var19;
                           BlockPos var359 = var357.relative(var358);
                           if (!var356.equals(var359)) {
                              BlockPos var360 = var356;
                              BlockPos var361 = var13;
                              if (!var360.equals(var361)) {
                                 BlockPos var362 = var356;
                                 BlockPos var363 = var13.below();
                                 if (!var362.equals(var363)) {
                                    BlockPos var364 = var356;
                                    BlockPos var365 = var2;
                                    Direction var366 = var19;
                                    BlockPos var367 = var365.relative(var366);
                                    if (!var364.equals(var367)) {
                                       BlockPos var368 = var356;
                                       BlockPos var369 = var13;
                                       Direction var370 = var19;
                                       BlockPos var371 = var369.relative(var370);
                                       if (!var368.equals(var371)) {
                                          PistonKick var372 = this;
                                          BlockPos var373 = var356;
                                          if (var372.h(var373)) {
                                             boolean var380;
                                             label309: {
                                                PistonKick var374 = this;
                                                BlockPos var375 = var356;
                                                if (!var374.e_bool_BlockPos(var375)) {
                                                   ClientLevel var376 = mc.level;
                                                   BlockPos var377 = var356;
                                                   if (!var376.getBlockState(var377).isAir()) {
                                                      ClientLevel var378 = mc.level;
                                                      BlockPos var379 = var356;
                                                      if (!var378.getBlockState(var379).canBeReplaced()) {
                                                         var380 = false;
                                                         break label309;
                                                      }
                                                   }
                                                }

                                                var380 = true;
                                             }

                                             if (var380) {
                                                boolean var381 = true;

                                                for (BlockPos var383 : (Iterable<BlockPos>)(Object)var355.list) {
                                                   BlockPos var384 = var383;
                                                   BlockPos var385 = var13;
                                                   if (!var384.equals(var385)) {
                                                      BlockPos var386 = var383;
                                                      BlockPos var387 = var13.below();
                                                      if (!var386.equals(var387)) {
                                                         BlockPos var388 = var383;
                                                         BlockPos var389 = var2;
                                                         Direction var390 = var19;
                                                         BlockPos var391 = var389.relative(var390);
                                                         if (!var388.equals(var391)) {
                                                            BlockPos var392 = var383;
                                                            BlockPos var393 = var13;
                                                            Direction var394 = var19;
                                                            BlockPos var395 = var393.relative(var394);
                                                            if (!var392.equals(var395)) {
                                                               PistonKick var396 = this;
                                                               BlockPos var397 = var383;
                                                               if (var396.h(var397)) {
                                                                  PistonKick var398 = this;
                                                                  BlockPos var399 = var383;
                                                                  if (var398.i(var399)) {
                                                                     continue;
                                                                  }

                                                                  BlockPos var400 = var383;
                                                                  Object var401 = null;
                                                                  boolean var402 = false;
                                                                  boolean var403 = true;
                                                                  if (BlockPlaceUtil.a(var400, (Predicate<Direction>)var401, var402, var403)) {
                                                                     continue;
                                                                  }
                                                               }

                                                               var381 = false;
                                                               break;
                                                            }
                                                         }
                                                      }
                                                   }

                                                   var381 = false;
                                                   break;
                                                }

                                                if (var381) {
                                                   this.C.clear();
                                                   List var430 = this.C;
                                                   List var431 = var355.list;
                                                   var430.addAll(var431);
                                                   PistonKick var432 = this;
                                                   BlockPos var433;
                                                   if (var355.list.isEmpty()) {
                                                      var433 = null;
                                                   } else {
                                                      List var434 = var355.list;
                                                      byte var435 = 0;
                                                      var433 = (BlockPos)var434.get(var435);
                                                   }

                                                   var432.B = var433;
                                                   PistonKick var436 = this;
                                                   BlockPos var437 = var24;
                                                   var436.z = var437;
                                                   PistonKick var438 = this;
                                                   BlockPos var439 = var356;
                                                   var438.A = var439;
                                                   PistonKick var440 = this;
                                                   Direction var441 = var19;
                                                   var440.D = var441;
                                                   return true;
                                                }
                                             }
                                          }
                                       }
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private BlockPos a(BlockPos var1, BlockPos var2, BlockPos var3, Direction var4) {
      BlockPos var5 = null;
      double var6 = Double.MAX_VALUE;
      boolean var8 = this.airPlace.getValue();
      Direction[] var9 = new Direction[5];
      var9[0] = Direction.DOWN;
      Direction var10 = var4.getOpposite();
      var9[1] = var10;
      Direction var11 = var4.getClockWise();
      var9[2] = var11;
      Direction var12 = var4.getCounterClockWise();
      var9[3] = var12;
      var9[4] = Direction.UP;
      Direction[] var13 = var9;
      int var14 = var13.length;
      int var15 = 0;

      while (true) {
         int var16 = var15;
         int var17 = var14;
         if (var16 >= var17) {
            return var5;
         }

         Direction[] var18 = var13;
         int var19 = var15;
         Direction var20 = var18[var19];
         BlockPos var21 = var1.relative(var20);
         if (!var21.equals(var2)) {
            BlockPos var22 = var21;
            BlockPos var23 = var2.below();
            if (!var22.equals(var23) && !var21.equals(var3)) {
               BlockPos var24 = var21;
               BlockPos var25 = var3.above();
               if (!var24.equals(var25)) {
                  BlockPos var26 = var21;
                  BlockPos var27 = var1.relative(var4);
                  if (!var26.equals(var27)) {
                     BlockPos var28 = var21;
                     if (this.c_bool_BlockPos(var28)) {
                        return var21;
                     }

                     BlockPos var29 = var21;
                     boolean var30 = var8;
                     if (BlockPlaceUtil.a(var29, null, var30, true)) {
                        BlockPos var31 = var21;
                        if (this.h(var31)) {
                           LocalPlayer var32 = mc.player;
                           Vec3 var33 = Vec3.atCenterOf(var21);
                           double var34 = var32.distanceToSqr(var33);
                           double var36 = var6;
                           if (var34 < var36) {
                              var6 = var34;
                              var5 = var21;
                           }
                        }
                     }
                  }
               }
            }
         }

         var15++;
      }
   }

   private void a_void_int_BlockPos_Direction(int var1, BlockPos var2, Direction var3) {
      if (var1 != -1 && mc.player != null) {
         Direction var4;
         if (this.strictDirection.getValue()) {
            var4 = BlockInteractionUtil.b_Direction_BlockPos_bool(var2, true);
         } else {
            var4 = BlockPlaceUtil.a_Direction_BlockPos_Predicate(var2, var0 -> true);
         }

         Direction var5;
         var5 = var4;
         label41:
         if (var5 == null) {
            if (!this.airPlace.getValue()) {
               return;
            }

            if (this.power.getValue().equalsIgnoreCase("Lever") && this.z != null && this.D != null) {
               BlockPos var6 = this.z;
               Direction var7 = this.D.getOpposite();
               BlockPos var8 = var6.relative(var7);
               if (var2.equals(var8)) {
                  var5 = this.D;
                  break label41;
               }
            }

            var5 = BlockPlaceUtil.a(var2);
            if (var5 == null) {
               var5 = Direction.UP;
            }
         }

         int var9 = mc.player.getInventory().getSelectedSlot();
         this.a_void_int_int(var1, var9);
         if (this.x()) {
            Direction var10 = var5;
            this.c(var2, var10);
            if (var3 != null) {
               this.a_void_Direction(var3);
            }
         }

         Direction var11 = var5;
         BlockPlaceUtil.b_void_BlockPos_Direction_bool(var2, var11, false);
         int var12 = var9;
         this.b_void_int_int(var1, var12);
         if (this.x.getValue()) {
            Night.renderPlacementManager.getPlacedBlocks().removeIf(var1x -> var1x.b().equals(var2));
            Night.renderPlacementManager.getPlacedBlocks().add(new PlaceRenderEntry(var2));
         }
      }
   }

   private void a_void_Direction(Direction var1) {
      float var3 = switch (var1) {
         case SOUTH -> 180.0F;
         case NORTH -> 0.0F;
         case EAST -> 90.0F;
         case WEST -> -90.0F;
         default -> 0.0F;
      };
      Night.rotationManager.sendOrBatchPosRot(var3, 0.0F);
   }

   private boolean a_bool_BlockPos_Direction(BlockPos var1, Direction var2) {
      if (mc.level == null) {
         return false;
      }

      BlockState var3 = mc.level.getBlockState(var1);
      if (var3.getBlock() instanceof PistonBaseBlock) {
         boolean var4;
         if (var3.getValue(PistonBaseBlock.FACING) == var2) {
            var4 = true;
         } else {
            var4 = false;
         }

         return var4;
      } else {
         return false;
      }
   }

   private boolean a_bool_BlockPos(BlockPos var1) {
      if (mc.level == null) {
         return false;
      }

      BlockState var2 = mc.level.getBlockState(var1);
      return var2.getBlock() instanceof PistonBaseBlock ? (Boolean)var2.getValue(PistonBaseBlock.EXTENDED) : false;
   }

   private boolean b_bool_BlockPos(BlockPos var1) {
      if (mc.level == null) {
         return false;
      }

      if (this.power.getValue().equalsIgnoreCase("Lever")) {
         Direction[] var2 = Direction.values();
         int var3 = var2.length;
         int var4 = 0;

         while (true) {
            int var5 = var4;
            int var6 = var3;
            if (var5 >= var6) {
               return mc.level.hasNeighborSignal(var1);
            }

            Direction[] var7 = var2;
            int var8 = var4;
            Direction var9 = var7[var8];
            BlockPos var10 = var1.relative(var9);
            if (this.e_bool_BlockPos(var10)) {
               BlockPos var11 = var10;
               if (this.f(var11)) {
                  return true;
               }
            }

            var4++;
         }
      } else {
         Direction[] var12 = Direction.values();
         int var13 = var12.length;
         int var14 = 0;

         while (true) {
            int var15 = var14;
            int var16 = var13;
            if (var15 >= var16) {
               return false;
            }

            Direction[] var17 = var12;
            int var18 = var14;
            Direction var19 = var17[var18];
            BlockPos var20 = var1.relative(var19);
            if (this.d_bool_BlockPos(var20)) {
               return true;
            }

            var14++;
         }
      }
   }

   private boolean c_bool_BlockPos(BlockPos var1) {
      if (mc.level != null && var1 != null) {
         Block var2 = mc.level.getBlockState(var1).getBlock();
         if (this.power.getValue().equalsIgnoreCase("Lever")) {
            boolean var3;
            if (var2 == Blocks.LEVER) {
               var3 = true;
            } else {
               var3 = false;
            }

            return var3;
         } else {
            boolean var4;
            if (var2 != Blocks.REDSTONE_BLOCK && var2 != Blocks.REDSTONE_TORCH) {
               var4 = false;
            } else {
               var4 = true;
            }

            return var4;
         }
      } else {
         return false;
      }
   }

   private boolean d_bool_BlockPos(BlockPos var1) {
      if (mc.level == null) {
         return false;
      }

      Block var2 = mc.level.getBlockState(var1).getBlock();
      boolean var3;
      if (var2 != Blocks.REDSTONE_BLOCK && var2 != Blocks.REDSTONE_TORCH) {
         var3 = false;
      } else {
         var3 = true;
      }

      return var3;
   }

   private boolean e_bool_BlockPos(BlockPos var1) {
      if (mc.level != null && var1 != null) {
         boolean var2;
         if (mc.level.getBlockState(var1).getBlock() == Blocks.LEVER) {
            var2 = true;
         } else {
            var2 = false;
         }

         return var2;
      } else {
         return false;
      }
   }

   private boolean f(BlockPos var1) {
      if (mc.level != null && var1 != null) {
         BlockState var2 = mc.level.getBlockState(var1);
         return var2.getBlock() == Blocks.LEVER ? (Boolean)var2.getValue(LeverBlock.POWERED) : false;
      } else {
         return false;
      }
   }

   private void g(BlockPos var1) {
      if (mc.player != null && mc.level != null) {
         Direction var2 = BlockPlaceUtil.a(var1);
         if (var2 == null) {
            var2 = Direction.UP;
         }

         Direction var3 = var2;
         boolean var4 = this.x();
         BlockPlaceUtil.a_void_BlockPos_Direction_bool(var1, var3, var4);
      }
   }

   private boolean b_bool_BlockPos_Direction(BlockPos var1, Direction var2) {
      if (mc.level == null) {
         return false;
      }

      BlockPos var3 = var1.relative(var2);
      BlockPos var4 = var3.above();
      ClientLevel var5 = mc.level;
      BlockPos var6 = var3;
      BlockState var7 = var5.getBlockState(var6);
      ClientLevel var8 = mc.level;
      BlockPos var9 = var4;
      BlockState var10 = var8.getBlockState(var9);
      if (!var7.isAir()) {
         if (!var7.canBeReplaced() || PacketAndRenderUtil.a_bool_BlockState(var7)) {
            return false;
         }

         BlockState var11 = var7;
         if (this.a_bool_BlockState(var11)) {
            return false;
         }
      }

      if (!var10.isAir()) {
         if (!var10.canBeReplaced() || PacketAndRenderUtil.a_bool_BlockState(var10)) {
            return false;
         }

         BlockState var12 = var10;
         if (this.a_bool_BlockState(var12)) {
            return false;
         }
      }

      return true;
   }

   private boolean a_bool_BlockState(BlockState var1) {
      Block var2 = var1.getBlock();
      boolean var3;
      if (var2 != Blocks.OBSIDIAN
         && var2 != Blocks.BEDROCK
         && var2 != Blocks.CRYING_OBSIDIAN
         && var2 != Blocks.RESPAWN_ANCHOR
         && var2 != Blocks.ENDER_CHEST
         && !var1.isSolid()) {
         var3 = false;
      } else {
         var3 = true;
      }

      return var3;
   }

   public List<BlockPos> a_List_Player(Player var1) {
      ArrayList var2 = new ArrayList();
      if (var1 != null && mc.level != null) {
         for (BlockPos var4 : (Iterable<BlockPos>)(Object)EntityUtil.b_List_Entity(var1)) {
            ClientLevel var5 = mc.level;
            BlockPos var6 = var4;
            BlockState var7 = var5.getBlockState(var6);
            if (this.c_bool_BlockState(var7)) {
               ArrayList var8 = var2;
               BlockPos var9 = var4;
               var8.add(var9);
            }
         }

         if (var2.isEmpty()) {
            double var10 = var1.getX();
            double var12 = var1.getY();
            double var14 = 0.1;
            double var16 = var12 + var14;
            double var18 = var1.getZ();
            BlockPos var20 = BlockPos.containing(var10, var16, var18);
            ClientLevel var21 = mc.level;
            BlockPos var22 = var20;
            BlockState var23 = var21.getBlockState(var22);
            if (this.c_bool_BlockState(var23)) {
               ArrayList var24 = var2;
               BlockPos var25 = var20;
               var24.add(var25);
            } else {
               BlockPos var26 = var1.blockPosition();
               ClientLevel var27 = mc.level;
               BlockPos var28 = var26;
               BlockState var29 = var27.getBlockState(var28);
               if (this.c_bool_BlockState(var29)) {
                  ArrayList var30 = var2;
                  BlockPos var31 = var26;
                  var30.add(var31);
               } else {
                  BlockPos var32 = var20.above();
                  ClientLevel var33 = mc.level;
                  BlockPos var34 = var32;
                  BlockState var35 = var33.getBlockState(var34);
                  if (this.c_bool_BlockState(var35)) {
                     ArrayList var36 = var2;
                     BlockPos var37 = var32;
                     var36.add(var37);
                  }
               }
            }
         }

         return var2;
      } else {
         return var2;
      }
   }

   public BlockPos b_BlockPos_Player(Player var1) {
      List<BlockPos> var2 = this.a_List_Player(var1);
      BlockPos var3;
      if (var2.isEmpty()) {
         var3 = null;
      } else {
         var3 = (BlockPos)var2.get(0);
      }

      return var3;
   }

   private boolean b_bool_BlockState(BlockState var1) {
      if (var1 != null && !var1.isAir() && !var1.canBeReplaced()) {
         Block var2 = var1.getBlock();
         boolean var3;
         if (!(var2 instanceof WebBlock)
            && !(var2 instanceof ButtonBlock)
            && !(var2 instanceof MushroomBlock)
            && !(var2 instanceof LadderBlock)
            && !(var2 instanceof ScaffoldingBlock)
            && !(var2 instanceof FlowerBlock)
            && !(var2 instanceof FlowerPotBlock)
            && !(var2 instanceof TallFlowerBlock)
            && !(var2 instanceof VineBlock)
            && !(var2 instanceof TorchBlock)
            && !(var2 instanceof LeverBlock)
            && !(var2 instanceof PressurePlateBlock)
            && !(var2 instanceof SignBlock)
            && !(var2 instanceof BannerBlock)
            && !(var2 instanceof CarpetBlock)
            && !(var2 instanceof TripWireBlock)
            && !(var2 instanceof TripWireHookBlock)
            && !(var2 instanceof BaseRailBlock)
            && var2 != Blocks.COBWEB
            && var2 != Blocks.LADDER
            && var2 != Blocks.SCAFFOLDING
            && var2 != Blocks.VINE
            && var2 != Blocks.BROWN_MUSHROOM
            && var2 != Blocks.RED_MUSHROOM
            && var2 != Blocks.TORCH
            && var2 != Blocks.SOUL_TORCH
            && var2 != Blocks.REDSTONE_TORCH) {
            var3 = false;
         } else {
            var3 = true;
         }

         return var3;
      } else {
         return true;
      }
   }

   private boolean c_bool_BlockState(BlockState var1) {
      if (this.b_bool_BlockState(var1)) {
         return false;
      }

      Block var2 = var1.getBlock();
      if (var2 != Blocks.OBSIDIAN
         && var2 != Blocks.BEDROCK
         && var2 != Blocks.ENDER_CHEST
         && var2 != Blocks.RESPAWN_ANCHOR
         && var2 != Blocks.CRYING_OBSIDIAN
         && var2 != Blocks.ANVIL
         && var2 != Blocks.CHIPPED_ANVIL
         && var2 != Blocks.DAMAGED_ANVIL
         && var2 != Blocks.REINFORCED_DEEPSLATE
         && var2 != Blocks.NETHERITE_BLOCK
         && var2 != Blocks.ANCIENT_DEBRIS) {
         boolean var3;
         if (var1.isSolid() && PacketAndRenderUtil.a_bool_BlockState(var1)) {
            var3 = true;
         } else {
            var3 = false;
         }

         return var3;
      } else {
         return true;
      }
   }

   private boolean h(BlockPos var1) {
      double var2 = this.placeRange.getValue().floatValue();
      LocalPlayer var4 = mc.player;
      Vec3 var5 = Vec3.atCenterOf(var1);
      double var6 = var4.distanceToSqr(var5);
      double var8 = var2;
      double var10 = var2;
      double var12 = var8 * var10;
      boolean var14;
      if (var6 <= var12) {
         var14 = true;
      } else {
         var14 = false;
      }

      return var14;
   }

   private void d_void_Player(Player var1) {
      double var2 = this.breakRange.getValue().floatValue();
      AABB var4 = var1.getBoundingBox();
      double var5 = var2;
      AABB var7 = var4.inflate(var5);
      ClientLevel var8 = mc.level;
      AABB var9 = var7;
      var8.getEntitiesOfClass(EndCrystal.class, var9, var2x -> {
         if (var2x.isAlive()) {
            double var3 = mc.player.distanceToSqr(var2x);
            double var5x = var2;
            double var7x = var2;
            double var9x = var5x * var7x;
            if (var3 <= var9x) {
               return true;
            }
         }

         return false;
      }).stream().min(Comparator.comparingDouble(var0 -> mc.player.distanceToSqr(var0))).ifPresent(var1x -> {
         float var2x = DamageUtil.a_float_Entity_AABB_EndCrystal_bool(mc.player, null, var1x, false);
         float var3 = this.maxLocalDamage.getValue().floatValue();
         if (!(var2x > var3)) {
            if (this.safety.getValue()) {
               float var4x = var2x;
               float var5x = mc.player.getHealth();
               float var6 = mc.player.getAbsorptionAmount();
               float var7x = var5x + var6;
               if (var4x >= var7x) {
                  return;
               }
            }

            ClientPacketListener var8x = mc.getConnection();
            int var9x = var1x.getId();
            var8x.send(new ServerboundAttackPacket(var9x));
            ClientPacketListener var10 = mc.getConnection();
            Packet var11 = PacketAndRenderUtil.a_Packet_InteractionHand(InteractionHand.MAIN_HAND);
            var10.send(var11);
         }
      });
   }

   private boolean x() {
      boolean var1;
      if (!this.rotate.getValue().equalsIgnoreCase("None")) {
         var1 = true;
      } else {
         var1 = false;
      }

      return var1;
   }

   private void c(BlockPos var1, Direction var2) {
      Vec3 var3 = Vec3.atCenterOf(var1);
      double var4 = var2.getStepX();
      double var6 = 0.5;
      double var8 = var4 * var6;
      double var10 = var2.getStepY();
      double var12 = 0.5;
      double var14 = var10 * var12;
      double var16 = var2.getStepZ();
      double var18 = 0.5;
      double var20 = var16 * var18;
      float[] var22 = RotationMathUtil.a(var3.add(var8, var14, var20));
      float var23 = var22[0];
      float var24 = var22[1];
      Night.rotationManager.sendOrBatchPosRot(var23, var24);
   }

   private void a_void_int_int(int var1, int var2) {
      InventoryUtil.a_bool_String_int_int(this.swap.getValue(), var1, var2);
   }

   private void b_void_int_int(int var1, int var2) {
      InventoryUtil.b(this.swap.getValue(), var1, var2);
   }

   private int y() {
      if (this.power.getValue().equalsIgnoreCase("Lever")) {
         return this.a_int_Block(Blocks.LEVER);
      }

      int var1 = this.a_int_Block(Blocks.REDSTONE_BLOCK);
      return var1 != -1 ? var1 : this.a_int_Block(Blocks.REDSTONE_TORCH);
   }

   private int z() {
      int var1 = this.a_int_Block(Blocks.OBSIDIAN);
      if (var1 != -1) {
         return var1;
      }

      int var2 = this.a_int_Block(Blocks.CRYING_OBSIDIAN);
      if (var2 != -1) {
         return var2;
      }

      int var3 = this.a_int_Block(Blocks.ENDER_CHEST);
      if (var3 != -1) {
         return var3;
      }

      int var4 = this.a_int_Block(Blocks.NETHERITE_BLOCK);
      if (var4 != -1) {
         return var4;
      }

      int var5 = this.a_int_Block(Blocks.COBBLESTONE);
      return var5 != -1 ? var5 : this.y();
   }

   private boolean i(BlockPos var1) {
      if (mc.level != null && var1 != null) {
         BlockState var2 = mc.level.getBlockState(var1);
         boolean var3;
         if (!var2.isAir() && !var2.canBeReplaced() && var2.isSolid()) {
            var3 = true;
         } else {
            var3 = false;
         }

         return var3;
      } else {
         return false;
      }
   }

   private int A() {
      int var1 = this.a_int_Block(Blocks.PISTON);
      return var1 != -1 ? var1 : this.a_int_Block(Blocks.STICKY_PISTON);
   }

   private int a_int_Block(Block var1) {
      if (this.swap.getValue().equalsIgnoreCase("None")) {
         ItemStack var2 = mc.player.getMainHandItem();
         if (!var2.isEmpty()) {
            Item var3 = var2.getItem();
            if (var3 instanceof BlockItem && ((BlockItem)var3).getBlock() == var1) {
               return mc.player.getInventory().getSelectedSlot();
            }
         }

         return -1;
      } else {
         int var4;
         if (!this.swap.getValue().equalsIgnoreCase("AltSwap") && !this.swap.getValue().equalsIgnoreCase("AltPickup")) {
            var4 = InventoryUtil.d;
         } else {
            var4 = InventoryUtil.f;
         }

         int var5 = var4;
         int var6 = 0;

         while (true) {
            int var7 = var6;
            int var8 = var5;
            if (var7 > var8) {
               return -1;
            }

            Inventory var9 = mc.player.getInventory();
            int var10 = var6;
            ItemStack var11 = var9.getItem(var10);
            if (!var11.isEmpty()) {
               Item var12 = var11.getItem();
               if (var12 instanceof BlockItem && ((BlockItem)var12).getBlock() == var1) {
                  return var6;
               }
            }

            var6++;
         }
      }
   }

   private List<Player> B() {
      double var1 = this.targetRange.getValue().floatValue();
      return mc.level.players().stream().filter(var0 -> {
         LocalPlayer var1x = mc.player;
         boolean var2;
         if (var0 != var1x) {
            var2 = true;
         } else {
            var2 = false;
         }

         return var2;
      }).filter(var0 -> {
         boolean var1x;
         if (!EntityUtil.c_bool_Entity(var0)) {
            var1x = true;
         } else {
            var1x = false;
         }

         return var1x;
      }).filter(var0 -> {
         boolean var1x;
         if (!var0.isSpectator() && var0.isAlive()) {
            var1x = true;
         } else {
            var1x = false;
         }

         return var1x;
      }).filter(var0 -> {
         String var1x = var0.getName().getString();
         boolean var2;
         if (!Night.friendManager.isFriend(var1x)) {
            var2 = true;
         } else {
            var2 = false;
         }

         return var2;
      }).filter(var2 -> {
         double var3 = mc.player.distanceToSqr(var2);
         double var5 = var1;
         double var7 = var1;
         double var9 = var5 * var7;
         boolean var11;
         if (var3 <= var9) {
            var11 = true;
         } else {
            var11 = false;
         }

         return var11;
      }).sorted(Comparator.comparingDouble(var0 -> mc.player.distanceToSqr(var0))).collect(Collectors.toList());
   }

   record PushPlan(BlockPos pos, List<BlockPos> list) {
   }
}
