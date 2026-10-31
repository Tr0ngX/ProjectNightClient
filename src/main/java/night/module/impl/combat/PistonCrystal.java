package night.module.impl.combat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.Plane;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
import net.minecraft.world.phys.shapes.VoxelShape;
import night.Night;
import night.events.EventTarget;
import night.events.impl.TickEvent;
import night.module.Module;
import night.module.ModuleInfo;
import night.module.ModuleManager;
import night.module.impl.player.SpeedMine;
import night.setting.impl.BooleanSetting;
import night.setting.impl.ModeSetting;
import night.setting.impl.NumberSetting;
import night.util.inventory.InventoryUtil;
import night.util.network.PacketPredictionUtil;
import night.util.render.PlaceRenderEntry;
import night.util.rotation.RotationMathUtil;
import night.util.world.BlockInteractionUtil;
import night.util.world.BlockPlaceUtil;
import night.util.world.DamageUtil;
import night.util.world.EntityUtil;
import night.util.world.PacketAndRenderUtil;

@ModuleInfo(name = "PistonCrystal", description = "Places piston + redstone to push a crystal into targets.", category = Module.Category.COMBAT)
public class PistonCrystal extends Module {
   public NumberSetting minDamage;
   public NumberSetting maxLocalDamage;
   public NumberSetting delay;
   public ModeSetting sort;
   public NumberSetting targetRange;
   public BooleanSetting inAirTarget;
   public BooleanSetting breakCrystal;
   public BooleanSetting autoBreak;
   public NumberSetting placeRange;
   public NumberSetting breakRange;
   public ModeSetting rotate;
   public BooleanSetting grim;
   public ModeSetting swap;
   public BooleanSetting airPlace;
   public BooleanSetting safety;
   public BooleanSetting blockDestruction;
   public BooleanSetting selfExtrapolate;
   public NumberSetting extrapolationTicks;
   public BooleanSetting assumeBestArmor;
   public BooleanSetting x;
   public BooleanSetting y;
   public BooleanSetting z;
   private BlockPos A;
   private BlockPos B;
   private BlockPos C;
   private boolean D;
   private BlockPos E;
   private BlockPos F;
   private BlockPos G;
   private BlockPos H;
   private final Set<BlockPos> I;
   private final Set<BlockPos> J;
   private int K;
   private Direction L;
   private boolean M;
   private Player N;
   private long O;
   private long P;
   private int Q;

   public PistonCrystal() {
      Float var1 = 6.0F;
      Float var2 = 1.0F;
      Float var3 = 20.0F;
      this.minDamage = new NumberSetting("MinDamage", "Minimum damage to attempt a sequence", var1, var2, var3);
      Float var4 = 12.0F;
      Float var5 = 1.0F;
      Float var6 = 20.0F;
      this.maxLocalDamage = new NumberSetting("MaxLocalDamage", "Max self-damage allowed", var4, var5, var6);
      Integer var7 = 0;
      Integer var8 = 0;
      Integer var9 = 10;
      this.delay = new NumberSetting("Delay", "Delay between place actions (ticks)", var7, var8, var9);
      String[] var10 = new String[]{"LowestDistance", "LowestHealth", "CloseAngle"};
      this.sort = new ModeSetting("Sort", "How to pick target", "LowestDistance", var10);
      Float var11 = 6.0F;
      Float var12 = 1.0F;
      Float var13 = 20.0F;
      this.targetRange = new NumberSetting("TargetRange", "Max target range", var11, var12, var13);
      this.inAirTarget = new BooleanSetting("InAirTarget", "Target players in the air", true);
      this.breakCrystal = new BooleanSetting("BreakCrystal", "Attack crystal after placing it", true);
      this.autoBreak = new BooleanSetting("AutoBreak", "Automatically breaks leftover redstone/piston with pickaxe after crystal explosion.", true);
      Float var14 = 4.5F;
      Float var15 = 0.1F;
      Float var16 = 6.0F;
      this.placeRange = new NumberSetting("PlaceRange", "Range to place blocks", var14, var15, var16);
      Float var17 = 4.5F;
      Float var18 = 0.1F;
      Float var19 = 6.0F;
      this.breakRange = new NumberSetting("BreakRange", "Range to break crystals", var17, var18, var19);
      String[] var20 = new String[]{"None", "Normal", "Silent"};
      this.rotate = new ModeSetting("Rotate", "Silently rotate to placed blocks", "Silent", var20);
      this.grim = new BooleanSetting("Grim", "YawDeceive - filter dir by player facing & override piston yaw", true);
      this.swap = new ModeSetting("Swap", "How to swap to required items", "Silent", InventoryUtil.a);
      this.airPlace = new BooleanSetting("AirPlace", "Allow placing when not on ground", false);
      this.safety = new BooleanSetting("Safety", "Skip if crystal would kill self", true);
      this.blockDestruction = new BooleanSetting("BlockDestruction", "Account for explosion block destruction", false);
      this.selfExtrapolate = new BooleanSetting("SelfExtrapolate", "Extrapolate self position for self-damage calc", false);
      Integer var21 = 0;
      Integer var22 = 0;
      Integer var23 = 10;
      this.extrapolationTicks = new NumberSetting("ExtrapolationTicks", "Ticks to extrapolate enemy position", var21, var22, var23);
      this.assumeBestArmor = new BooleanSetting("AssumeBestArmor", "Assume target has Prot 4 netherite", false);
      this.x = new BooleanSetting("AutoSupport", "Automatically places obsidian support blocks when no platform exists.", true);
      this.y = new BooleanSetting("PauseOnEat", "Pauses the module while eating or using an item.", true);
      this.z = new BooleanSetting(
         "StrictDirection", "Use raytrace to verify line-of-sight before placing piston/redstone (required on servers that check raytrace).", false
      );
      this.I = new LinkedHashSet<>();
      this.J = new LinkedHashSet<>();
      this.K = -1;
      long var24 = 0L;
      this.O = var24;
      long var26 = 0L;
      this.P = var26;
      this.Q = -1;
   }

   @Override
   public void onEnable() {
      this.C();
      this.I.clear();
      this.J.clear();
      long var1 = System.currentTimeMillis();
      long var3 = 99999999L;
      long var5 = var1 + var3;
      this.P = var5;
   }

   @Override
   public void onDisable() {
      this.C();
      this.I.clear();
      this.J.clear();
   }

   @EventTarget
   public void a_void_TickEvent(TickEvent var1) {
      if (mc.player != null && mc.level != null) {
         if (!this.y.getValue() || !EntityUtil.a()) {
            long var2 = System.currentTimeMillis();
            long var4 = var2;
            long var6 = this.O;
            long var8 = var4 - var6;
            long var10 = this.delay.getValue().longValue();
            long var12 = 50L;
            long var14 = var10 * var12;
            if (var8 >= var14) {
               PistonCrystal var16 = this;
               Item var17 = Items.END_CRYSTAL;
               int var18 = var16.a_int_Item(var17);
               int var19 = this.y();
               int var20 = this.z();
               int var21 = var18;
               byte var22 = -1;
               if (var21 != var22) {
                  int var23 = var19;
                  byte var24 = -1;
                  if (var23 != var24) {
                     int var25 = var20;
                     byte var26 = -1;
                     if (var25 != var26) {
                        PistonCrystal var27 = this;
                        Player var28 = this.A();
                        var27.N = var28;
                        if (this.N == null) {
                           this.C();
                           return;
                        }

                        if (this.A != null && this.B != null && this.C != null) {
                           boolean var39;
                           label332: {
                              label331: {
                                 ClientLevel var29 = mc.level;
                                 BlockPos var30 = this.A;
                                 if (var29.getBlockState(var30).getBlock() instanceof PistonBaseBlock) {
                                    ClientLevel var31 = mc.level;
                                    BlockPos var32 = this.B;
                                    Block var33 = var31.getBlockState(var32).getBlock();
                                    Block var34 = Blocks.REDSTONE_BLOCK;
                                    if (var33 == var34) {
                                       break label331;
                                    }

                                    ClientLevel var35 = mc.level;
                                    BlockPos var36 = this.B;
                                    Block var37 = var35.getBlockState(var36).getBlock();
                                    Block var38 = Blocks.REDSTONE_TORCH;
                                    if (var37 == var38) {
                                       break label331;
                                    }
                                 }

                                 var39 = false;
                                 break label332;
                              }

                              var39 = true;
                           }

                           if (!var39) {
                              boolean var40;
                              boolean var53;
                              label362: {
                                 var40 = this.airPlace.getValue();
                                 BlockPos var41 = this.A;
                                 Object var42 = null;
                                 boolean var43 = var40;
                                 boolean var44 = true;
                                 if (!BlockPlaceUtil.a(var41, (Predicate<Direction>)var42, var43, var44)) {
                                    ClientLevel var45 = mc.level;
                                    BlockPos var46 = this.A;
                                    label318:
                                    if (!(var45.getBlockState(var46).getBlock() instanceof PistonBaseBlock)) {
                                       if (!var40 && this.F != null) {
                                          BlockPos var47 = this.F;
                                          Object var48 = null;
                                          boolean var49 = var40;
                                          boolean var50 = true;
                                          if (BlockPlaceUtil.a(var47, (Predicate<Direction>)var48, var49, var50)) {
                                             break label318;
                                          }

                                          PistonCrystal var51 = this;
                                          BlockPos var52 = this.F;
                                          if (var51.f(var52)) {
                                             break label318;
                                          }
                                       }

                                       var53 = false;
                                       break label362;
                                    }
                                 }

                                 var53 = true;
                              }

                              boolean var54;
                              boolean var73;
                              label308: {
                                 var54 = var53;
                                 BlockPos var55 = this.B;
                                 Object var56 = null;
                                 boolean var57 = var40;
                                 boolean var58 = true;
                                 if (!BlockPlaceUtil.a(var55, (Predicate<Direction>)var56, var57, var58)) {
                                    ClientLevel var59 = mc.level;
                                    BlockPos var60 = this.B;
                                    Block var61 = var59.getBlockState(var60).getBlock();
                                    Block var62 = Blocks.REDSTONE_BLOCK;
                                    if (var61 != var62) {
                                       ClientLevel var63 = mc.level;
                                       BlockPos var64 = this.B;
                                       Block var65 = var63.getBlockState(var64).getBlock();
                                       Block var66 = Blocks.REDSTONE_TORCH;
                                       if (var65 != var66) {
                                          BlockPos var67 = this.B;
                                          BlockPos var68 = this.A.above();
                                          if (!var67.equals(var68)) {
                                             BlockPos var69 = this.B;
                                             BlockPos var70 = this.A;
                                             Direction var71 = this.L;
                                             BlockPos var72 = var70.relative(var71);
                                             if (!var69.equals(var72)) {
                                                var73 = false;
                                                break label308;
                                             }
                                          }
                                       }
                                    }
                                 }

                                 var73 = true;
                              }

                              boolean var74;
                              boolean var85;
                              label300: {
                                 label353: {
                                    var74 = var73;
                                    if (this.D) {
                                       BlockPos var75 = this.C;
                                       boolean var76 = var40;
                                       if (BlockPlaceUtil.c(var75, var76)) {
                                          break label353;
                                       }
                                    } else {
                                       BlockPos var77 = this.C;
                                       boolean var78 = var40;
                                       if (BlockPlaceUtil.b_bool_BlockPos_bool(var77, var78)) {
                                          break label353;
                                       }
                                    }

                                    if (this.E != null) {
                                       BlockPos var79 = this.E;
                                       Object var80 = null;
                                       boolean var81 = false;
                                       boolean var82 = true;
                                       if (BlockPlaceUtil.a(var79, (Predicate<Direction>)var80, var81, var82)) {
                                          break label353;
                                       }

                                       PistonCrystal var83 = this;
                                       BlockPos var84 = this.E;
                                       if (var83.e_bool_BlockPos(var84)) {
                                          break label353;
                                       }
                                    }

                                    var85 = false;
                                    break label300;
                                 }

                                 var85 = true;
                              }

                              boolean var86 = var85;
                              if (!var54 && !var40 && this.x.getValue() && this.F == null) {
                                 PistonCrystal var87 = this;
                                 BlockPos var88 = this.A;
                                 Direction var89 = this.L;
                                 BlockPos var90 = var87.b_BlockPos_BlockPos_Direction(var88, var89);
                                 if (var90 != null) {
                                    PistonCrystal var91 = this;
                                    BlockPos var92 = var90;
                                    var91.F = var92;
                                    var54 = true;
                                 }
                              }

                              if (!var86 && this.x.getValue() && this.E == null) {
                                 BlockPos var93 = this.C.below();
                                 PistonCrystal var94 = this;
                                 BlockPos var95 = this.C;
                                 BlockPos var96 = var93;
                                 if (var94.a_bool_BlockPos_BlockPos(var95, var96)) {
                                    PistonCrystal var97 = this;
                                    BlockPos var98 = var93;
                                    var97.E = var98;
                                    var86 = true;
                                 }
                              }

                              if (!var54 || !var74 || !var86) {
                                 this.C();
                              }
                           }
                        }

                        if (this.A == null && this.C == null && this.B == null) {
                           PistonCrystal var99 = this;
                           Player var100 = this.N;
                           var99.a_void_Player(var100);
                        }

                        if (this.x.getValue() && this.E != null) {
                           PistonCrystal var101 = this;
                           BlockPos var102 = this.E;
                           if (!var101.e_bool_BlockPos(var102)) {
                              BlockPos var103 = this.E;
                              Object var104 = null;
                              boolean var105 = false;
                              boolean var106 = true;
                              if (BlockPlaceUtil.a(var103, (Predicate<Direction>)var104, var105, var106)) {
                                 int var107 = this.v();
                                 int var108 = var107;
                                 byte var109 = -1;
                                 if (var108 != var109) {
                                    PistonCrystal var110 = this;
                                    int var111 = var107;
                                    BlockPos var112 = this.E;
                                    Object var113 = null;
                                    if (!var110.a_bool_int_BlockPos_Direction(var111, var112, (Direction)var113)) {
                                       this.C();
                                       return;
                                    }

                                    PistonCrystal var114 = this;
                                    long var115 = var2;
                                    var114.O = var115;
                                    return;
                                 }
                              }
                           }
                        }

                        if (!this.airPlace.getValue() && this.x.getValue() && this.F != null) {
                           PistonCrystal var117 = this;
                           BlockPos var118 = this.F;
                           if (!var117.f(var118)) {
                              BlockPos var119 = this.F;
                              Object var120 = null;
                              boolean var121 = false;
                              boolean var122 = true;
                              if (BlockPlaceUtil.a(var119, (Predicate<Direction>)var120, var121, var122)) {
                                 int var123 = this.v();
                                 int var124 = var123;
                                 byte var125 = -1;
                                 if (var124 != var125) {
                                    PistonCrystal var126 = this;
                                    int var127 = var123;
                                    BlockPos var128 = this.F;
                                    Object var129 = null;
                                    if (!var126.a_bool_int_BlockPos_Direction(var127, var128, (Direction)var129)) {
                                       this.C();
                                       return;
                                    }

                                    PistonCrystal var130 = this;
                                    long var131 = var2;
                                    var130.O = var131;
                                    return;
                                 }
                              }
                           }
                        }

                        if (this.A != null) {
                           BlockPos var133 = this.A;
                           Object var134 = null;
                           boolean var135 = this.airPlace.getValue();
                           boolean var136 = true;
                           if (BlockPlaceUtil.a(var133, (Predicate<Direction>)var134, var135, var136)) {
                              PistonCrystal var137 = this;
                              int var138 = var19;
                              BlockPos var139 = this.A;
                              Direction var140 = this.L;
                              if (!var137.a_bool_int_BlockPos_Direction(var138, var139, var140) && this.z.getValue()) {
                                 this.C();
                                 return;
                              }

                              PistonCrystal var141 = this;
                              BlockPos var142 = this.A;
                              var141.G = var142;
                              Set var143 = this.J;
                              BlockPos var144 = this.A;
                              var143.add(var144);
                              PistonCrystal var145 = this;
                              long var146 = var2;
                              var145.O = var146;
                           }
                        }

                        if (this.B != null) {
                           BlockPos var148 = this.B;
                           Object var149 = null;
                           boolean var150 = this.airPlace.getValue();
                           boolean var151 = true;
                           if (BlockPlaceUtil.a(var148, (Predicate<Direction>)var149, var150, var151)) {
                              if (this.M) {
                                 PistonCrystal var152 = this;
                                 int var153 = var20;
                                 BlockPos var154 = this.B;
                                 var152.b_void_int_BlockPos(var153, var154);
                              } else {
                                 PistonCrystal var155 = this;
                                 int var156 = var20;
                                 BlockPos var157 = this.B;
                                 Object var158 = null;
                                 var155.a_bool_int_BlockPos_Direction(var156, var157, (Direction)var158);
                              }

                              PistonCrystal var159 = this;
                              BlockPos var160 = this.B;
                              var159.H = var160;
                              Set var161 = this.I;
                              BlockPos var162 = this.B;
                              var161.add(var162);
                              PistonCrystal var163 = this;
                              long var164 = var2;
                              var163.O = var164;
                           }
                        }

                        label366: {
                           if (this.C != null) {
                              if (this.D) {
                                 BlockPos var166 = this.C;
                                 boolean var167 = this.airPlace.getValue();
                                 if (BlockPlaceUtil.c(var166, var167)) {
                                    break label366;
                                 }
                              } else {
                                 BlockPos var168 = this.C;
                                 boolean var169 = this.airPlace.getValue();
                                 if (BlockPlaceUtil.b_bool_BlockPos_bool(var168, var169)) {
                                    break label366;
                                 }
                              }
                           }

                           if (this.breakCrystal.getValue()) {
                              long var179 = var2;
                              long var181 = this.P;
                              long var183 = var179 - var181;
                              long var185 = this.delay.getValue().longValue();
                              long var187 = 50L;
                              long var189 = var185 * var187;
                              if (var183 >= var189) {
                                 byte var191 = -1;

                                 while (true) {
                                    byte var192 = var191;
                                    byte var193 = 4;
                                    if (var192 > var193) {
                                       break;
                                    }

                                    BlockPos var194 = this.N.blockPosition();
                                    byte var195 = var191;
                                    BlockPos var196 = var194.above(var195);
                                    label255:
                                    if (BlockPlaceUtil.d(var196)) {
                                       if (this.C != null) {
                                          BlockPos var197 = this.C;
                                          BlockPos var198 = var196;
                                          if (var197.equals(var198)) {
                                             break label255;
                                          }
                                       }

                                       PistonCrystal var199 = this;
                                       BlockPos var200 = var196;
                                       var199.b_void_BlockPos(var200);
                                       PistonCrystal var201 = this;
                                       long var202 = var2;
                                       var201.O = var202;
                                       PistonCrystal var204 = this;
                                       long var205 = var2;
                                       var204.P = var205;
                                       this.C();
                                       return;
                                    }

                                    var191 += 1;
                                 }
                              }
                           }

                           SpeedMine var209;
                           if (Night.moduleManager != null) {
                              ModuleManager var207 = Night.moduleManager;
                              Class<SpeedMine> var208 = SpeedMine.class;
                              var209 = var207.getModule(var208);
                           } else {
                              var209 = null;
                           }

                           SpeedMine var210 = var209;
                           boolean var211;
                           if (var210 != null && var210.isEnabled() && var210.C()) {
                              var211 = true;
                           } else {
                              var211 = false;
                           }

                           boolean var212 = var211;
                           long var213 = this.O;
                           long var215 = var2;
                           boolean var217;
                           if (var213 == var215) {
                              var217 = true;
                           } else {
                              var217 = false;
                           }

                           boolean var218 = var217;
                           if (this.autoBreak.getValue() && !var212 && !var218) {
                              Iterator var219 = this.I.iterator();

                              while (var219.hasNext()) {
                                 BlockPos var220 = (BlockPos)var219.next();
                                 PistonCrystal var221 = this;
                                 BlockPos var222 = var220;
                                 if (!var221.g(var222)) {
                                    var219.remove();
                                 } else {
                                    BlockPos var223 = var220;
                                    BlockPos var224 = this.B;
                                    if (!var223.equals(var224)) {
                                       PistonCrystal var225 = this;
                                       BlockPos var226 = var220;
                                       double var227 = this.placeRange.getValue().floatValue();
                                       if (var225.a_bool_BlockPos_double(var226, var227)) {
                                          PistonCrystal var244 = this;
                                          BlockPos var245 = var220;
                                          var244.c_void_BlockPos(var245);
                                          var219.remove();
                                          PistonCrystal var246 = this;
                                          long var247 = var2;
                                          var246.O = var247;
                                          return;
                                       }
                                    }
                                 }
                              }

                              Iterator var229 = this.J.iterator();

                              while (var229.hasNext()) {
                                 BlockPos var230 = (BlockPos)var229.next();
                                 ClientLevel var231 = mc.level;
                                 BlockPos var232 = var230;
                                 if (!(var231.getBlockState(var232).getBlock() instanceof PistonBaseBlock)) {
                                    var229.remove();
                                 } else {
                                    BlockPos var233 = var230;
                                    BlockPos var234 = this.A;
                                    if (!var233.equals(var234)) {
                                       PistonCrystal var235 = this;
                                       BlockPos var236 = var230;
                                       double var237 = this.placeRange.getValue().floatValue();
                                       if (var235.a_bool_BlockPos_double(var236, var237)) {
                                          PistonCrystal var239 = this;
                                          BlockPos var240 = var230;
                                          var239.c_void_BlockPos(var240);
                                          var229.remove();
                                          PistonCrystal var241 = this;
                                          long var242 = var2;
                                          var241.O = var242;
                                          return;
                                       }
                                    }
                                 }
                              }
                           }

                           long var249 = 2L;
                           long var251 = this.delay.getValue().longValue();
                           long var253 = var249 + var251;
                           long var255 = 50L;
                           long var257 = var253 * var255;
                           if (this.autoBreak.getValue() && !var212) {
                              long var259 = var2;
                              long var261 = this.O;
                              long var263 = var259 - var261;
                              long var265 = var257;
                              if (var263 >= var265) {
                                 Iterator var267 = this.I.iterator();

                                 while (var267.hasNext()) {
                                    BlockPos var268 = (BlockPos)var267.next();
                                    PistonCrystal var269 = this;
                                    BlockPos var270 = var268;
                                    if (var269.g(var270)) {
                                       PistonCrystal var271 = this;
                                       BlockPos var272 = var268;
                                       double var273 = this.placeRange.getValue().floatValue();
                                       if (var271.a_bool_BlockPos_double(var272, var273)) {
                                          PistonCrystal var275 = this;
                                          BlockPos var276 = var268;
                                          var275.c_void_BlockPos(var276);
                                          var267.remove();
                                          PistonCrystal var277 = this;
                                          long var278 = var2;
                                          var277.O = var278;
                                          this.C();
                                          return;
                                       }
                                    } else {
                                       var267.remove();
                                    }
                                 }

                                 PistonCrystal var280 = this;
                                 Set var281 = var280.J;
                                 Iterator var282 = var281.iterator();
                                 Iterator var283 = var282;

                                 while (true) {
                                    Iterator var284 = var283;
                                    boolean var285 = var284.hasNext();
                                    if (!var285) {
                                       break;
                                    }

                                    Iterator var286 = var283;
                                    Object var287 = var286.next();
                                    BlockPos var288 = (BlockPos)var287;
                                    BlockPos var289 = var288;
                                    Minecraft var290 = mc;
                                    ClientLevel var291 = var290.level;
                                    BlockPos var292 = var289;
                                    BlockState var293 = var291.getBlockState(var292);
                                    Block var294 = var293.getBlock();
                                    boolean var295 = var294 instanceof PistonBaseBlock;
                                    if (var295) {
                                       PistonCrystal var296 = this;
                                       BlockPos var297 = var289;
                                       PistonCrystal var298 = this;
                                       NumberSetting var299 = var298.placeRange;
                                       Number var300 = var299.getValue();
                                       float var301 = var300.floatValue();
                                       double var302 = var301;
                                       boolean var304 = var296.a_bool_BlockPos_double(var297, var302);
                                       if (var304) {
                                          PistonCrystal var305 = this;
                                          BlockPos var306 = var289;
                                          var305.c_void_BlockPos(var306);
                                          Iterator var307 = var283;
                                          var307.remove();
                                          PistonCrystal var308 = this;
                                          long var309 = var2;
                                          var308.O = var309;
                                          PistonCrystal var311 = this;
                                          var311.C();
                                          return;
                                       }
                                    } else {
                                       Iterator var312 = var283;
                                       var312.remove();
                                    }
                                 }
                              }
                           }

                           return;
                        }

                        PistonCrystal var170 = this;
                        int var171 = var18;
                        BlockPos var172 = this.C;
                        var170.a_void_int_BlockPos(var171, var172);
                        PistonCrystal var173 = this;
                        long var174 = var2;
                        var173.O = var174;
                        PistonCrystal var176 = this;
                        long var177 = var2;
                        var176.P = var177;
                        return;
                     }
                  }
               }

               this.C();
            }
         }
      }
   }

   private void a_void_Player(Player var1) {
      if (!this.b_bool_Player(var1)) {
         int var2 = this.extrapolationTicks.getValue().intValue();
         double var3 = var1.getX();
         double var5 = var1.xOld;
         double var7 = var3 - var5;
         double var9 = var2;
         double var11 = var7 * var9;
         double var13 = var1.getY();
         double var15 = var1.yOld;
         double var17 = var13 - var15;
         double var19 = var2;
         double var21 = var17 * var19;
         double var23 = 0.3;
         double var25 = var21 * var23;
         double var27 = var1.getZ();
         double var29 = var1.zOld;
         double var31 = var27 - var29;
         double var33 = var2;
         double var35 = var31 * var33;
         double var37 = var1.getX();
         double var39 = var11;
         double var41 = var37 + var39;
         double var43 = var1.getY();
         double var45 = var25;
         double var47 = var43 + var45;
         double var49 = var1.getZ();
         double var51 = var35;
         double var53 = var49 + var51;
         double var55 = var41;
         double var57 = var47;
         double var59 = var53;
         BlockPos var61 = a(var1, var55, var57, var59);
         AABB var62 = var1.getBoundingBox();
         double var63 = var11;
         double var65 = var25;
         double var67 = var35;
         AABB var69 = var62.move(var63, var65, var67);
         AABB var105;
         if (this.selfExtrapolate.getValue()) {
            AABB var70 = mc.player.getBoundingBox();
            double var71 = mc.player.getX();
            double var73 = mc.player.xOld;
            double var75 = var71 - var73;
            double var77 = var2;
            double var79 = var75 * var77;
            double var81 = mc.player.getY();
            double var83 = mc.player.yOld;
            double var85 = var81 - var83;
            double var87 = var2;
            double var89 = var85 * var87;
            double var91 = 0.3;
            double var93 = var89 * var91;
            double var95 = mc.player.getZ();
            double var97 = mc.player.zOld;
            double var99 = var95 - var97;
            double var101 = var2;
            double var103 = var99 * var101;
            var105 = var70.move(var79, var93, var103);
         } else {
            var105 = mc.player.getBoundingBox();
         }

         AABB var106;
         boolean var111;
         label266: {
            var106 = var105;
            if (var1.getPose() != Pose.SWIMMING && !var1.isVisuallyCrawling() && !(var1.getEyeHeight() <= 1.0F)) {
               double var107 = var1.getBoundingBox().getYsize();
               double var109 = 1.0;
               if (!(var107 <= var109)) {
                  var111 = false;
                  break label266;
               }
            }

            var111 = true;
         }

         boolean var112 = var111;

         int var113;
         for (var113 = 0; var113 <= 4; var113++) {
            BlockPos var114 = var61;
            int var115 = var113;
            BlockPos var116 = var114.above(var115);
            if (this.a_bool_BlockPos(var116)) {
               break;
            }
         }

         boolean var120;
         label251: {
            if (var112 && mc.level != null) {
               ClientLevel var117 = mc.level;
               BlockPos var118 = var61.above(1);
               if (!var117.getBlockState(var118).isAir()) {
                  BlockPos var119 = var61.above(0);
                  if (!this.a_bool_BlockPos(var119)) {
                     var120 = true;
                     break label251;
                  }
               }
            }

            var120 = false;
         }

         boolean var121;
         boolean var125;
         label244: {
            var121 = var120;
            if (!var112 && mc.level != null) {
               ClientLevel var122 = mc.level;
               BlockPos var123 = var61.above(2);
               if (!var122.getBlockState(var123).isAir()) {
                  BlockPos var124 = var61.above(1);
                  if (!this.a_bool_BlockPos(var124)) {
                     var125 = true;
                     break label244;
                  }
               }
            }

            var125 = false;
         }

         boolean var126 = var125;
         boolean var127;
         if (!var112 && var113 >= 2) {
            var127 = true;
         } else {
            var127 = false;
         }

         boolean var128 = var127;
         ClientLevel var129 = mc.level;
         BlockPos var130 = var61.above(2);
         boolean var131;
         if (!var129.getBlockState(var130).isAir()) {
            var131 = true;
         } else {
            var131 = false;
         }

         boolean var132 = var131;
         BlockPos var133 = var61.above(2);
         boolean var134;
         if (!this.a_bool_BlockPos(var133)) {
            var134 = true;
         } else {
            var134 = false;
         }

         boolean var137;
         label231: {
            boolean var135 = var134;
            if (!var112 && mc.level != null && (var132 || var135)) {
               BlockPos var136 = var61.above(1);
               if (this.a_bool_BlockPos(var136)) {
                  var137 = true;
                  break label231;
               }
            }

            var137 = false;
         }

         boolean var138 = var137;
         int[] var140;
         if (var121) {
            int[] var139 = new int[]{1, 0, 2};
            var140 = var139;
         } else if (var113 >= 3) {
            int[] var141 = new int[5];
            int var142 = var113;
            var141[0] = var142;
            int var143 = var113 - 1;
            var141[1] = var143;
            var141[2] = 2;
            var141[3] = 1;
            var141[4] = 0;
            var140 = var141;
         } else if (var126 || var113 == 2) {
            int[] var144 = new int[]{2, 1, 0, 3};
            var140 = var144;
         } else if (var138) {
            int[] var145 = new int[]{1, 0, 2, 3};
            var140 = var145;
         } else {
            int[] var146 = new int[]{0, 1, 2, 3};
            var140 = var146;
         }

         boolean var147 = false;
         int[] var148 = var140;
         int var149 = var148.length;
         byte var150 = 0;

         while (true) {
            byte var151 = var150;
            int var152 = var149;
            if (var151 >= var152) {
               break;
            }

            int[] var153 = var148;
            byte var154 = var150;
            int var155 = var153[var154];
            int var156 = this.K;
            if (var155 == var156) {
               var147 = true;
               break;
            }

            var150 += 1;
         }

         if (this.K != -1 && var147) {
            int var157 = this.K;
            int var158 = var140[0];
            if (var157 != var158) {
               int[] var159 = new int[var140.length];
               int var160 = this.K;
               var159[0] = var160;
               byte var161 = 1;
               int[] var162 = var140;
               int var163 = var162.length;
               byte var164 = 0;

               while (true) {
                  byte var165 = var164;
                  int var166 = var163;
                  if (var165 >= var166) {
                     var140 = var159;
                     break;
                  }

                  int[] var167 = var162;
                  byte var168 = var164;
                  int var169 = var167[var168];
                  int var170 = this.K;
                  if (var169 != var170) {
                     int[] var171 = var159;
                     byte var172 = var161;
                     var161 += 1;
                     int var173 = var169;
                     var171[var172] = var173;
                  }

                  var164 += 1;
               }
            }
         }

         int[] var174 = var140;
         int var175 = var174.length;
         byte var176 = 0;

         while (true) {
            byte var177 = var176;
            int var178 = var175;
            if (var177 >= var178) {
               return;
            }

            int var181;
            BlockPos var184;
            BlockPos var185;
            int[] var179 = var174;
            byte var180 = var176;
            var181 = var179[var180];
            BlockPos var182 = var61;
            int var183 = var181;
            var184 = var182.above(var183);
            label191:
            if ((!var121 || var181 != 1) && (!var138 || var181 != 1)) {
               label291: {
                  if (var126 || var128) {
                     if (var181 == 2) {
                        break label291;
                     }

                     int var186 = var181;
                     int var187 = var113;
                     if (var186 == var187) {
                        break label291;
                     }
                  }

                  var185 = null;
                  break label191;
               }

               BlockPos var188 = var61;
               int var189 = var181;
               var185 = var188.above(var189);
            } else {
               var185 = var61.above(1);
            }

            float var195;
            float var200;
            boolean var203;
            label292: {
               BlockPos var190 = var185;
               AABB var191 = var69;
               BlockPos var192 = var184;
               BlockPos var193 = var190;
               boolean var194 = this.blockDestruction.getValue();
               var195 = DamageUtil.a(var1, var191, var192, var193, var194);
               LocalPlayer var196 = mc.player;
               AABB var197 = var106;
               BlockPos var198 = var184;
               boolean var199 = this.blockDestruction.getValue();
               var200 = DamageUtil.a(var196, var197, var198, null, var199);
               if ((var181 != 1 || !var121) && (var181 != 1 || !var138) && (var181 != 2 || !var126)) {
                  int var201 = var181;
                  int var202 = var113;
                  if (var201 != var202 || !var128) {
                     var203 = false;
                     break label292;
                  }
               }

               var203 = true;
            }

            if (var203) {
               float var204 = var195;
               float var205 = this.minDamage.getValue().floatValue();
               if (!(var204 <= var205)) {
                  float var206 = var200;
                  float var207 = this.maxLocalDamage.getValue().floatValue();
                  label154:
                  if (!(var206 > var207)) {
                     if (this.safety.getValue()) {
                        float var208 = var200;
                        float var209 = mc.player.getHealth();
                        float var210 = mc.player.getAbsorptionAmount();
                        float var211 = var209 + var210;
                        if (var208 >= var211) {
                           break label154;
                        }
                     }

                     BlockPos var212 = var61;
                     int var213 = var181;
                     if (this.a_bool_BlockPos_int_bool(var212, var213, true)) {
                        int var229 = var181;
                        this.K = var229;
                        return;
                     }

                     BlockPos var214 = var61;
                     int var215 = var181;
                     this.a_bool_BlockPos_int(var214, var215);
                  }
               }
            } else {
               float var216 = var195;
               float var217 = this.minDamage.getValue().floatValue();
               if (!(var216 <= var217)) {
                  float var218 = var200;
                  float var219 = this.maxLocalDamage.getValue().floatValue();
                  label148:
                  if (!(var218 > var219)) {
                     if (this.safety.getValue()) {
                        float var220 = var200;
                        float var221 = mc.player.getHealth();
                        float var222 = mc.player.getAbsorptionAmount();
                        float var223 = var221 + var222;
                        if (var220 >= var223) {
                           break label148;
                        }
                     }

                     BlockPos var224 = var61;
                     int var225 = var181;
                     boolean var226;
                     if (!var126 && !var121 && !var128) {
                        var226 = false;
                     } else {
                        var226 = true;
                     }

                     if (this.a_bool_BlockPos_int_bool(var224, var225, var226)) {
                        int var230 = var181;
                        this.K = var230;
                        return;
                     }

                     BlockPos var227 = var61;
                     int var228 = var181;
                     if (this.a_bool_BlockPos_int(var227, var228)) {
                        return;
                     }
                  }
               }
            }

            var176 += 1;
         }
      }
   }

   private boolean a_bool_BlockPos_int(BlockPos var1, int var2) {
      if (!this.x.getValue()) {
         return false;
      }

      Iterator var3 = Plane.HORIZONTAL.iterator();

      BlockPos var10;
      while (true) {
         if (!var3.hasNext()) {
            return false;
         }

         Direction var4 = (Direction)var3.next();
         BlockPos var5 = var1.relative(var4).above(var2);
         double var6 = this.placeRange.getValue().floatValue();
         if (this.a_bool_BlockPos_double(var5, var6)) {
            if (BlockPlaceUtil.b_bool_BlockPos_bool(var5, false)) {
               BlockPos var8 = var5;
               Direction var9 = var4;
               var10 = var8.relative(var9).below();
               BlockPos var11 = var10;
               double var12 = this.placeRange.getValue().floatValue();
               if (this.a_bool_BlockPos_double(var11, var12) && BlockPlaceUtil.a(var10, null, false, true)) {
                  break;
               }
            } else {
               var10 = var5.below();
               BlockPos var14 = var10;
               if (!this.e_bool_BlockPos(var14)) {
                  BlockPos var15 = var5;
                  BlockPos var16 = var10;
                  if (!this.a_bool_BlockPos_BlockPos(var15, var16)) {
                     continue;
                  }
                  break;
               }
            }
         }
      }

      int var17 = this.v();
      if (var17 == -1) {
         return false;
      }

      int var18 = var17;
      BlockPos var19 = var10;
      this.a_bool_int_BlockPos_Direction(var18, var19, null);
      long var20 = System.currentTimeMillis();
      this.O = var20;
      return true;
   }

   public static BlockPos a(Entity var0, double var1, double var3, double var5) {
      if (mc.level == null) {
         int var7 = Mth.floor(var1);
         int var8 = Mth.floor(var3);
         int var9 = Mth.floor(var5);
         return new BlockPos(var7, var8, var9);
      }

      int var10 = Mth.floor(var3);
      int var11 = Mth.floor(var1);
      int var12 = var10;
      int var13 = Mth.floor(var5);
      BlockPos var14 = new BlockPos(var11, var12, var13);
      ClientLevel var15 = mc.level;
      BlockPos var16 = var14;
      BlockState var17 = var15.getBlockState(var16);
      if (!var17.isAir() && !var17.canBeReplaced()) {
         BlockState var18 = var17;
         ClientLevel var19 = mc.level;
         BlockPos var20 = var14;
         VoxelShape var21 = var18.getCollisionShape(var19, var20);
         if (!var21.isEmpty()) {
            double var22 = var14.getY();
            double var24 = var21.max(Axis.Y);
            double var26 = var22 + var24;
            double var28 = var3;
            double var30 = var26;
            double var32 = 0.05;
            double var34 = var30 - var32;
            if (var28 >= var34) {
               return var14.above();
            }
         }
      }

      return var14;
   }

   private boolean b_bool_Player(Player var1) {
      if (mc.level != null && var1 != null) {
         double var2 = var1.getX();
         double var4 = var1.getY();
         double var6 = var1.getZ();
         BlockPos var8 = a(var1, var2, var4, var6);
         ClientLevel var9 = mc.level;
         BlockPos var10 = var8;
         BlockState var11 = var9.getBlockState(var10);
         return this.b_bool_BlockState(var11);
      } else {
         return false;
      }
   }

   private boolean a_bool_BlockPos(BlockPos var1) {
      if (mc.level == null) {
         return true;
      }

      for (Direction var3 : Plane.HORIZONTAL) {
         ClientLevel var4 = mc.level;
         Direction var5 = var3;
         BlockPos var6 = var1.relative(var5);
         BlockState var7 = var4.getBlockState(var6);
         if (var7.isAir() || var7.canBeReplaced()) {
            return true;
         }
      }

      return false;
   }

   private boolean a_bool_BlockState(BlockState var1) {
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

   private boolean b_bool_BlockState(BlockState var1) {
      if (this.a_bool_BlockState(var1)) {
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

   private boolean a_bool_BlockPos_int_bool(BlockPos var1, int var2, boolean var3) {
      ArrayList var4 = new ArrayList();
      Direction var5 = mc.player.getDirection();
      ArrayList var6 = var4;
      Direction var7 = var5;
      var6.add(var7);

      for (Direction var9 : Plane.HORIZONTAL) {
         Direction var10 = var5;
         if (var9 != var10) {
            ArrayList var113 = var4;
            Direction var114 = var9;
            var113.add(var114);
         }
      }

      boolean var11 = this.airPlace.getValue();
      Iterator var12 = var4.iterator();

      while (true) {
         Direction var13;
         BlockPos var14;
         BlockPos var17;
         while (true) {
            if (!var12.hasNext()) {
               return false;
            }

            var13 = (Direction)var12.next();
            var14 = var1.relative(var13).above(var2);
            double var15 = this.placeRange.getValue().floatValue();
            if (this.a_bool_BlockPos_double(var14, var15) && (!this.z.getValue() || BlockInteractionUtil.f(var14))) {
               var17 = null;
               BlockPos var18 = var14;
               boolean var19 = var11;
               if (BlockPlaceUtil.b_bool_BlockPos_bool(var18, var19)) {
                  break;
               }

               if (var3) {
                  BlockPos var20 = var14;
                  boolean var21 = var11;
                  if (BlockPlaceUtil.c(var20, var21)) {
                     break;
                  }
               }

               if (!var11 && this.x.getValue()) {
                  BlockPos var22 = var14.below();
                  BlockPos var23 = var14;
                  BlockPos var24 = var22;
                  if (this.a_bool_BlockPos_BlockPos(var23, var24)) {
                     var17 = var22;
                     break;
                  }
               }
            }
         }

         BlockPos var25 = null;
         BlockPos var26 = null;
         BlockPos var27 = null;
         ArrayList var28;
         if (var3) {
            var28 = new ArrayList(4);
            ArrayList var29 = var28;
            Direction var30 = var13;
            var29.add(var30);
            ArrayList var31 = var28;
            Direction var32 = var13.getClockWise();
            var31.add(var32);
            ArrayList var33 = var28;
            Direction var34 = var13.getCounterClockWise();
            var33.add(var34);

            for (Direction var36 : Plane.HORIZONTAL) {
               Direction var37 = var13.getOpposite();
               if (var36 != var37) {
                  ArrayList var38 = var28;
                  Direction var39 = var36;
                  if (!var38.contains(var39)) {
                     ArrayList var40 = var28;
                     Direction var41 = var36;
                     var40.add(var41);
                  }
               }
            }
         } else {
            var28 = new ArrayList(3);

            for (Direction var43 : Plane.HORIZONTAL) {
               Direction var44 = var13.getOpposite();
               if (var43 != var44) {
                  ArrayList var45 = var28;
                  Direction var46 = var43;
                  var45.add(var46);
               }
            }
         }

         Iterator var47 = var28.iterator();

         label208:
         while (true) {
            BlockPos var53;
            while (true) {
               Direction var48;
               ClientLevel var61;
               BlockPos var62;
               do {
                  if (!var47.hasNext()) {
                     break label208;
                  }

                  var48 = (Direction)var47.next();
                  BlockPos var49 = var14;
                  Direction var50 = var13;
                  BlockPos var51 = var49.relative(var50);
                  Direction var52 = var48;
                  var53 = var51.relative(var52);
                  if (var3) {
                     Direction var54 = var48;
                     Direction var55 = var13;
                     if (var54 != var55) {
                        break;
                     }
                  }

                  BlockPos var56 = var53;
                  Direction var57 = var13.getOpposite();
                  BlockPos var58 = var56.relative(var57);
                  ClientLevel var59 = mc.level;
                  BlockPos var60 = var58;
                  if (var59.getBlockState(var60).isAir()) {
                     break;
                  }

                  var61 = mc.level;
                  var62 = var58;
               } while (!var61.getBlockState(var62).canBeReplaced());

               ClientLevel var63 = mc.level;
               BlockPos var64 = var53;
               boolean var65 = var63.getBlockState(var64).getBlock() instanceof PistonBaseBlock;
               if (var65 || !this.z.getValue() || BlockInteractionUtil.f(var53)) {
                  var27 = null;
                  BlockPos var66 = var53;
                  boolean var67 = var11;
                  boolean var68;
                  if (!BlockPlaceUtil.a(var66, null, var67, true) && !var65) {
                     var68 = false;
                  } else {
                     var68 = true;
                  }

                  boolean var69;
                  boolean var72;
                  label158: {
                     var69 = var68;
                     if (!var11 && var17 != null) {
                        BlockPos var70 = var53;
                        BlockPos var71 = var17;
                        if (var70.distManhattan(var71) == 1) {
                           var72 = true;
                           break label158;
                        }
                     }

                     var72 = false;
                  }

                  boolean var73 = var72;
                  if (var69 || var73) {
                     break;
                  }

                  if (!var11 && this.x.getValue()) {
                     BlockPos var74 = var53;
                     Direction var75 = var13;
                     Direction var76 = var48;
                     BlockPos var77 = var17;
                     BlockPos var78 = this.a(var74, var75, var76, var77);
                     if (var78 != null) {
                        var27 = var78;
                        break;
                     }
                  }
               }
            }

            BlockPos var79 = var53;
            double var80 = this.placeRange.getValue().floatValue();
            if (this.a_bool_BlockPos_double(var79, var80)) {
               var25 = var53;
               Direction[] var82 = Direction.values();
               int var83 = var82.length;
               int var84 = 0;

               while (true) {
                  int var85 = var84;
                  int var86 = var83;
                  if (var85 >= var86) {
                     break;
                  }

                  BlockPos var93;
                  label239: {
                     Direction[] var87 = var82;
                     int var88 = var84;
                     Direction var89 = var87[var88];
                     Direction var90 = var13.getOpposite();
                     label201:
                     if (var89 != var90) {
                        BlockPos var91 = var53;
                        Direction var92 = var89;
                        var93 = var91.relative(var92);
                        if (var27 != null) {
                           BlockPos var94 = var93;
                           BlockPos var95 = var27;
                           if (var94.equals(var95)) {
                              break label201;
                           }
                        }

                        if (var17 != null) {
                           BlockPos var96 = var93;
                           BlockPos var97 = var17;
                           if (var96.equals(var97)) {
                              break label201;
                           }
                        }

                        ClientLevel var98 = mc.level;
                        BlockPos var99 = var93;
                        if (var98.getBlockState(var99).getBlock() == Blocks.REDSTONE_BLOCK) {
                           break label239;
                        }

                        ClientLevel var100 = mc.level;
                        BlockPos var101 = var93;
                        if (var100.getBlockState(var101).getBlock() == Blocks.REDSTONE_TORCH) {
                           break label239;
                        }

                        BlockPos var102 = var93;
                        boolean var103 = var11;
                        if (BlockPlaceUtil.a(var102, null, var103, true)) {
                           BlockPos var104 = var93;
                           double var105 = this.placeRange.getValue().floatValue();
                           if (this.a_bool_BlockPos_double(var104, var105) && (!this.z.getValue() || BlockInteractionUtil.f(var93))) {
                              var26 = var93;
                              break label208;
                           }
                        }
                     }

                     var84++;
                     continue;
                  }

                  var26 = var93;
                  break label208;
               }
            }
         }

         if (var25 != null && var26 != null) {
            Direction var107 = var13;
            this.L = var107;
            BlockPos var108 = var14;
            this.C = var108;
            this.D = var3;
            BlockPos var109 = var25;
            this.A = var109;
            BlockPos var110 = var26;
            this.B = var110;
            BlockPos var111 = var17;
            this.E = var111;
            BlockPos var112 = var27;
            this.F = var112;
            return true;
         }
      }
   }

   private boolean a_bool_int_BlockPos_Direction(int var1, BlockPos var2, Direction var3) {
      if (var1 == -1) {
         return false;
      }

      Direction var6;
      if (var3 != null) {
         if (this.z.getValue()) {
            List var4 = BlockInteractionUtil.a_List_BlockPos_List_bool(var2, null, true);
            Direction var5;
            if (var4.contains(var3)) {
               var5 = var3;
            } else if (var4.isEmpty()) {
               var5 = null;
            } else {
               var5 = (Direction)var4.get(0);
            }

            var6 = var5;
            if (var6 == null && !this.airPlace.getValue()) {
               return false;
            }
         } else {
            var6 = BlockPlaceUtil.a_Direction_BlockPos_Predicate(var2, var0 -> true);
            if (var6 == null && !this.airPlace.getValue()) {
               return false;
            }
         }
      } else if (this.z.getValue()) {
         var6 = BlockInteractionUtil.b_Direction_BlockPos_bool(var2, true);
         if (var6 == null) {
            return false;
         }
      } else {
         var6 = BlockPlaceUtil.a_Direction_BlockPos_Predicate(var2, var0 -> true);
         if (var6 == null && !this.airPlace.getValue()) {
            return false;
         }
      }

      int var7 = mc.player.getInventory().getSelectedSlot();
      this.a_void_int_int(var1, var7);
      if (this.x()) {
         if (var3 != null) {
            if (var6 != null && this.z.getValue()) {
               Direction var8 = var6;
               this.a_void_BlockPos_Direction(var2, var8);
            }

            this.a_void_Direction(var3);
         } else {
            Direction var9;
            if (var6 != null) {
               var9 = var6;
            } else {
               var9 = Direction.UP;
            }

            Direction var10 = var9;
            this.a_void_BlockPos_Direction(var2, var10);
         }
      }

      if (var6 != null) {
         Direction var11 = var6;
         BlockPlaceUtil.b_void_BlockPos_Direction_bool(var2, var11, false);
      } else {
         BlockPlaceUtil.a_void_BlockPos_Direction_bool(var2, Direction.UP, false);
      }

      int var12 = var7;
      this.b_void_int_int(var1, var12);
      Night.renderPlacementManager.getPlacedBlocks().removeIf(var1x -> var1x.b().equals(var2));
      Night.renderPlacementManager.getPlacedBlocks().add(new PlaceRenderEntry(var2));
      return true;
   }

   private void a_void_int_BlockPos(int var1, BlockPos var2) {
      BlockPos var3 = var2.below();
      Direction var4 = BlockPlaceUtil.a(var3);
      if (var4 == null) {
         if (this.airPlace.getValue()) {
            var3 = var2;
            var4 = Direction.UP;
         } else {
            var4 = Direction.UP;
         }
      }

      if (var1 != -1) {
         int var5 = mc.player.getInventory().getSelectedSlot();
         this.a_void_int_int(var1, var5);
         if (this.x()) {
            BlockPos var6 = var3;
            Direction var7 = var4;
            this.a_void_BlockPos_Direction(var6, var7);
         }

         BlockPos var8 = var3;
         Direction var9 = var4;
         BlockPlaceUtil.a_void_BlockPos_Direction_bool(var8, var9, false);
         int var10 = var5;
         this.b_void_int_int(var1, var10);
         Night.renderPlacementManager.getPlacedBlocks().removeIf(var1x -> var1x.b().equals(var2));
         Night.renderPlacementManager.getPlacedBlocks().add(new PlaceRenderEntry(var2));
      }
   }

   private void b_void_int_BlockPos(int var1, BlockPos var2) {
      boolean var3 = this.airPlace.getValue();
      if (BlockPlaceUtil.a(var2, null, var3, true) && var1 != -1) {
         List var4 = BlockPlaceUtil.b_List_BlockPos_Predicate(var2, null);
         if (var4.isEmpty()) {
            if (this.airPlace.getValue()) {
               int var5 = mc.player.getInventory().getSelectedSlot();
               this.a_void_int_int(var1, var5);
               if (this.x()) {
                  this.a_void_BlockPos_Direction(var2, Direction.UP);
               }

               BlockPlaceUtil.a_void_BlockPos_Direction_bool(var2, Direction.UP, false);
               int var6 = var5;
               this.b_void_int_int(var1, var6);
               Night.renderPlacementManager.getPlacedBlocks().removeIf(var1x -> var1x.b().equals(var2));
               Night.renderPlacementManager.getPlacedBlocks().add(new PlaceRenderEntry(var2));
            }
         } else {
            int var7 = mc.player.getInventory().getSelectedSlot();

            for (Direction var9 : (Iterable<Direction>)(Object)var4) {
               ClientLevel var10 = mc.level;
               Direction var11 = var9;
               BlockPos var12 = var2.relative(var11);
               if (!(var10.getBlockState(var12).getBlock() instanceof PistonBaseBlock) && var9 != Direction.UP) {
                  int var13 = var7;
                  this.a_void_int_int(var1, var13);
                  if (this.x()) {
                     Direction var14 = var9;
                     this.a_void_BlockPos_Direction(var2, var14);
                  }

                  Direction var15 = var9;
                  BlockPlaceUtil.b_void_BlockPos_Direction_bool(var2, var15, false);
                  int var16 = var7;
                  this.b_void_int_int(var1, var16);
                  Night.renderPlacementManager.getPlacedBlocks().removeIf(var1x -> var1x.b().equals(var2));
                  Night.renderPlacementManager.getPlacedBlocks().add(new PlaceRenderEntry(var2));
                  return;
               }
            }
         }
      }
   }

   private void b_void_BlockPos(BlockPos var1) {
      ClientLevel var2 = mc.level;
      AABB var3 = new AABB(var1);
      double var4 = 1.0;
      AABB var6 = var3.inflate(var4);
      List<EndCrystal> var7 = var2.getEntitiesOfClass(EndCrystal.class, var6, var0 -> var0.isAlive());
      if (!var7.isEmpty()) {
         float var8 = this.breakRange.getValue().floatValue();
         float var9 = this.breakRange.getValue().floatValue();
         double var10 = var8 * var9;
         EndCrystal var12 = var7.stream().filter(var2x -> {
            double var3x = mc.player.distanceToSqr(var2x);
            double var5 = var10;
            boolean var7x;
            if (var3x <= var5) {
               var7x = true;
            } else {
               var7x = false;
            }

            return var7x;
         }).min(Comparator.comparingDouble(var0 -> mc.player.distanceToSqr(var0))).orElse(null);
         if (var12 != null) {
            if (this.x()) {
               Vec3 var13 = var12.position();
               double var14 = 0.0;
               double var16 = 0.5;
               double var18 = 0.0;
               float[] var20 = RotationMathUtil.a(var13.add(var14, var16, var18));
               float var21 = var20[0];
               float var22 = var20[1];
               Night.rotationManager.sendOrBatchPosRot(var21, var22);
            }

            ClientPacketListener var23 = mc.getConnection();
            int var24 = var12.getId();
            var23.send(new ServerboundAttackPacket(var24));
            ClientPacketListener var25 = mc.getConnection();
            Packet var26 = PacketAndRenderUtil.a_Packet_InteractionHand(InteractionHand.MAIN_HAND);
            var25.send(var26);
         }
      }
   }

   private void c_void_BlockPos(BlockPos var1) {
      if (mc.player != null && mc.getConnection() != null && mc.level != null && var1 != null) {
         Direction var2 = BlockPlaceUtil.a(var1);
         if (var2 == null) {
            var2 = Direction.UP;
         }

         int var3 = this.w();
         int var4 = mc.player.getInventory().getSelectedSlot();
         if (var3 != -1) {
            int var5 = var3;
            int var6 = var4;
            this.a_void_int_int(var5, var6);
         }

         if (this.x()) {
            Direction var7 = var2;
            this.a_void_BlockPos_Direction(var1, var7);
         }

         Direction var8 = var2;
         PacketPredictionUtil.a_void_SequencedPacketCreator(var2x -> new ServerboundPlayerActionPacket(Action.START_DESTROY_BLOCK, var1, var8, var2x));
         Direction var9 = var8;
         PacketPredictionUtil.a_void_SequencedPacketCreator(var2x -> new ServerboundPlayerActionPacket(Action.STOP_DESTROY_BLOCK, var1, var9, var2x));
         ClientPacketListener var10 = mc.getConnection();
         Packet var11 = PacketAndRenderUtil.a_Packet_InteractionHand(InteractionHand.MAIN_HAND);
         var10.send(var11);
         if (var3 != -1) {
            int var12 = var3;
            int var13 = var4;
            this.b_void_int_int(var12, var13);
         }
      }
   }

   private int w() {
      int var1 = this.a_int_Item(Items.NETHERITE_PICKAXE);
      if (var1 != -1) {
         return var1;
      }

      int var2 = this.a_int_Item(Items.DIAMOND_PICKAXE);
      if (var2 != -1) {
         return var2;
      }

      int var3 = this.a_int_Item(Items.IRON_PICKAXE);
      return var3 != -1 ? var3 : -1;
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

   private void a_void_BlockPos_Direction(BlockPos var1, Direction var2) {
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

   private void a_void_Direction(Direction var1) {
      if (var1 == Direction.EAST) {
         Night.rotationManager.sendOrBatchPosRot(-90.0F, 0.0F);
      } else if (var1 == Direction.WEST) {
         Night.rotationManager.sendOrBatchPosRot(90.0F, 0.0F);
      } else if (var1 == Direction.NORTH) {
         Night.rotationManager.sendOrBatchPosRot(180.0F, 0.0F);
      } else {
         Night.rotationManager.sendOrBatchPosRot(0.0F, 0.0F);
      }
   }

   private void a_void_int_int(int var1, int var2) {
      InventoryUtil.a_bool_String_int_int(this.swap.getValue(), var1, var2);
   }

   private void b_void_int_int(int var1, int var2) {
      InventoryUtil.b(this.swap.getValue(), var1, var2);
   }

   private int a_int_Item(Item var1) {
      if (this.swap.getValue().equalsIgnoreCase("None")) {
         ItemStack var2 = mc.player.getMainHandItem();
         int var3;
         if (!var2.isEmpty() && var2.getItem() == var1) {
            var3 = mc.player.getInventory().getSelectedSlot();
         } else {
            var3 = -1;
         }

         return var3;
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
            if (!var11.isEmpty() && var11.getItem() == var1) {
               return var6;
            }

            var6++;
         }
      }
   }

   private int y() {
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

   private int z() {
      int var1 = this.a_int_Item(Items.REDSTONE_BLOCK);
      if (var1 != -1) {
         this.M = false;
         return var1;
      } else {
         int var2 = this.a_int_Item(Items.REDSTONE_TORCH);
         if (var2 != -1) {
            this.M = true;
            return var2;
         } else {
            return -1;
         }
      }
   }

   private Player A() {
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
      }).filter(var0 -> {
         String var1x = var0.getName().getString();
         boolean var2;
         if (!Night.friendManager.isFriend(var1x)) {
            var2 = true;
         } else {
            var2 = false;
         }

         return var2;
      }).filter(var1x -> {
         boolean var2;
         if (!this.inAirTarget.getValue() && !var1x.onGround()) {
            var2 = false;
         } else {
            var2 = true;
         }

         return var2;
      }).min(this.B()).orElse(null);
   }

   private Comparator<Player> B() {
      return switch (this.sort.getValue()) {
         case "LowestHealth" -> Comparator.comparingDouble(var0 -> {
            float var1 = var0.getHealth();
            float var2 = var0.getAbsorptionAmount();
            return var1 + var2;
         });
         default -> Comparator.comparingDouble(var0 -> mc.player.distanceToSqr(var0));
      };
   }

   private boolean a_bool_BlockPos_double(BlockPos var1, double var2) {
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

   private boolean a_bool_BlockPos_BlockPos(BlockPos var1, BlockPos var2) {
      if (mc.level != null && var2 != null && var1 != null) {
         double var3 = this.placeRange.getValue().floatValue();
         if (!this.a_bool_BlockPos_double(var2, var3)) {
            return false;
         }

         if (!mc.level.getBlockState(var1).isAir() && !mc.level.getBlockState(var1).canBeReplaced()) {
            return false;
         }

         ClientLevel var5 = mc.level;
         BlockPos var6 = var1.above();
         if (!var5.getBlockState(var6).isAir()) {
            ClientLevel var7 = mc.level;
            BlockPos var8 = var1.above();
            if (!var7.getBlockState(var8).canBeReplaced()) {
               return false;
            }
         }

         if (BlockPlaceUtil.e(var1, false) || BlockPlaceUtil.e(var1.above(), false)) {
            return false;
         }

         if (!mc.level.getBlockState(var2).isAir() && !mc.level.getBlockState(var2).canBeReplaced()) {
            return false;
         }

         if (!BlockPlaceUtil.a(var2, null, false, true)) {
            return false;
         }

         boolean var9;
         if (this.z.getValue() && BlockInteractionUtil.b_Direction_BlockPos_bool(var2, true) == null) {
            var9 = false;
         } else {
            var9 = true;
         }

         return var9;
      } else {
         return false;
      }
   }

   private BlockPos a(BlockPos var1, Direction var2, Direction var3, BlockPos var4) {
      if (mc.level != null && var1 != null) {
         if (var4 != null && var1.distManhattan(var4) == 1) {
            return null;
         }

         Direction[] var5 = new Direction[]{Direction.DOWN, var3, null};
         Direction var6 = var3.getOpposite();
         var5[2] = var6;
         Direction[] var7 = var5;
         int var8 = var7.length;
         int var9 = 0;

         while (true) {
            int var10 = var9;
            int var11 = var8;
            if (var10 >= var11) {
               return null;
            }

            Direction[] var12 = var7;
            int var13 = var9;
            Direction var14 = var12[var13];
            BlockPos var15 = var1.relative(var14);
            double var16 = this.placeRange.getValue().floatValue();
            label37:
            if (this.a_bool_BlockPos_double(var15, var16)) {
               ClientLevel var18 = mc.level;
               BlockPos var19 = var15;
               if (!var18.getBlockState(var19).isAir()) {
                  ClientLevel var20 = mc.level;
                  BlockPos var21 = var15;
                  if (!var20.getBlockState(var21).canBeReplaced()) {
                     break label37;
                  }
               }

               if (BlockPlaceUtil.a(var15, null, false, true) && (!this.z.getValue() || BlockInteractionUtil.b_Direction_BlockPos_bool(var15, true) != null)) {
                  return var15;
               }
            }

            var9++;
         }
      } else {
         return null;
      }
   }

   private BlockPos b_BlockPos_BlockPos_Direction(BlockPos var1, Direction var2) {
      if (mc.level != null && var1 != null) {
         BlockPos var3 = var1.below();
         if (!this.d_bool_BlockPos(var3)) {
            BlockPos var4 = var3;
            double var5 = this.placeRange.getValue().floatValue();
            label49:
            if (this.a_bool_BlockPos_double(var4, var5)) {
               ClientLevel var7 = mc.level;
               BlockPos var8 = var3;
               if (!var7.getBlockState(var8).isAir()) {
                  ClientLevel var9 = mc.level;
                  BlockPos var10 = var3;
                  if (!var9.getBlockState(var10).canBeReplaced()) {
                     break label49;
                  }
               }

               if (BlockPlaceUtil.a(var3, null, false, true)) {
                  return var3;
               }
            }
         }

         for (Direction var12 : Plane.HORIZONTAL) {
            if (var2 != null) {
               Direction var13 = var12;
               Direction var14 = var2.getOpposite();
               if (var13 == var14) {
                  continue;
               }
            }

            Direction var15 = var12;
            BlockPos var16 = var1.relative(var15);
            if (!this.d_bool_BlockPos(var16)) {
               BlockPos var17 = var16;
               double var18 = this.placeRange.getValue().floatValue();
               if (this.a_bool_BlockPos_double(var17, var18)) {
                  ClientLevel var20 = mc.level;
                  BlockPos var21 = var16;
                  if (!var20.getBlockState(var21).isAir()) {
                     ClientLevel var22 = mc.level;
                     BlockPos var23 = var16;
                     if (!var22.getBlockState(var23).canBeReplaced()) {
                        continue;
                     }
                  }

                  if (BlockPlaceUtil.a(var16, null, false, true)) {
                     return var16;
                  }
               }
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private boolean d_bool_BlockPos(BlockPos var1) {
      BlockPos var2 = this.B;
      if (!var1.equals(var2)) {
         BlockPos var3 = this.C;
         if (!var1.equals(var3)) {
            BlockPos var4 = this.E;
            if (!var1.equals(var4)) {
               return false;
            }
         }
      }

      return true;
   }

   public int v() {
      int var1 = this.a_int_Block(Blocks.OBSIDIAN);
      if (var1 == -1) {
         var1 = this.a_int_Block(Blocks.CRYING_OBSIDIAN);
      }

      if (var1 == -1) {
         var1 = this.a_int_Block(Blocks.ENDER_CHEST);
      }

      if (var1 == -1) {
         var1 = this.a_int_Block(Blocks.NETHERITE_BLOCK);
      }

      if (var1 == -1) {
         var1 = this.a_int_Block(Blocks.COBBLESTONE);
      }

      if (var1 == -1) {
         var1 = this.a_int_Block(Blocks.STONE);
      }

      return var1;
   }

   private boolean e_bool_BlockPos(BlockPos var1) {
      if (mc.level != null && var1 != null) {
         Block var2 = mc.level.getBlockState(var1).getBlock();
         boolean var3;
         if (var2 != Blocks.OBSIDIAN && var2 != Blocks.CRYING_OBSIDIAN && var2 != Blocks.BEDROCK && var2 != Blocks.ENDER_CHEST) {
            var3 = false;
         } else {
            var3 = true;
         }

         return var3;
      } else {
         return false;
      }
   }

   private boolean f(BlockPos var1) {
      if (mc.level != null && var1 != null) {
         BlockState var2 = mc.level.getBlockState(var1);
         boolean var3;
         if (!var2.isAir() && var2.isSolid()) {
            var3 = true;
         } else {
            var3 = false;
         }

         return var3;
      } else {
         return false;
      }
   }

   private boolean g(BlockPos var1) {
      if (mc.level != null && var1 != null) {
         Block var2 = mc.level.getBlockState(var1).getBlock();
         boolean var3;
         if (var2 != Blocks.REDSTONE_BLOCK && var2 != Blocks.REDSTONE_TORCH) {
            var3 = false;
         } else {
            var3 = true;
         }

         return var3;
      } else {
         return false;
      }
   }

   private void C() {
      this.A = null;
      this.B = null;
      this.C = null;
      this.D = false;
      this.E = null;
      this.F = null;
      this.G = null;
      this.L = null;
      this.M = false;
   }
}
