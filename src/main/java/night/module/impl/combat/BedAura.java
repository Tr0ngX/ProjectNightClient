package night.module.impl.combat;

import com.mojang.blaze3d.vertex.PoseStack;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import night.Night;
import night.events.EventTarget;
import night.events.impl.ClientConnectEvent;
import night.events.impl.ClientDisconnectEvent;
import night.events.impl.KeyInputEvent;
import night.events.impl.MouseButtonEvent;
import night.events.impl.Render3DEvent;
import night.events.impl.TickEvent;
import night.mixins.accessors.ClientPlayerEntityAccessor;
import night.module.Module;
import night.module.ModuleInfo;
import night.setting.impl.BooleanSetting;
import night.setting.impl.CategorySetting;
import night.setting.impl.ColorSetting;
import night.setting.impl.KeybindSetting;
import night.setting.impl.ModeSetting;
import night.setting.impl.NumberSetting;
import night.util.color.ColorUtil;
import night.util.inventory.InventoryUtil;
import night.util.render.Render3DUtil;
import night.util.rotation.RotationMathUtil;
import night.util.world.BlockInteractionUtil;
import night.util.world.DamageUtil;
import night.util.world.PacketAndRenderUtil;

@ModuleInfo(name = "BedAura", description = "Automatically places and detonates beds in the Nether and End.", category = Module.Category.COMBAT)
public class BedAura extends Module {
   public CategorySetting targetingCategory = new CategorySetting("Targeting", "Settings related to target selection.");
   public ModeSetting target;
   public NumberSetting targetRange;
   public NumberSetting maxTargets;
   public NumberSetting bedRange;
   public BooleanSetting ignoreFriends;
   public CategorySetting damageCategory;
   public NumberSetting minDamage;
   public NumberSetting maxSelfDamage;
   public NumberSetting minHealth;
   public BooleanSetting antiSuicide;
   public NumberSetting balance;
   public BooleanSetting predict;
   public CategorySetting behaviorCategory;
   public NumberSetting delay;
   public NumberSetting range;
   public NumberSetting wallsRange;
   public ModeSetting swapMode;
   public BooleanSetting rotate;
   public BooleanSetting x;
   public BooleanSetting y;
   public BooleanSetting z;
   public BooleanSetting pauseOnEat;
   public CategorySetting autoCraftCategory;
   public BooleanSetting autoCraft;
   public KeybindSetting autoCraftBind;
   public BooleanSetting autoPlace;
   public BooleanSetting autoClose;
   public CategorySetting renderCategory;
   public BooleanSetting render;
   public BooleanSetting interpolate;
   public ModeSetting renderMode;
   public ColorSetting fillColor;
   public ColorSetting outlineColor;
   private static final int M = 10;
   private int N;
   private BlockPos O;
   private Direction P;
   private Player Q;
   private float R;
   private BlockPos S;
   private boolean T;
   private long U;
   private boolean V;
   private BlockPos W;
   private BlockPos X;
   private BedAura.BedPlacement Y;
   private final List<Player> Z;
   private long aa;
   private static final long ab = 350L;
   private BlockPos ac;
   private boolean ad;
   private int ae;
   private BlockPos af;
   private boolean ag;
   private boolean ah;
   private static final int ai = 0;
   private static final int[] aj;
   private static final int[] ak;
   private static final int al = 10;

   public BedAura() {
      CategorySetting var2 = this.targetingCategory;
      CategorySetting.CategoryCondition var1 = new CategorySetting.CategoryCondition(var2);
      String[] var3 = new String[]{"Closest", "Furthest", "Health"};
      this.target = new ModeSetting("Target", "Mode", "How targets are sorted.", var1, "Closest", var3);
      CategorySetting var5 = this.targetingCategory;
      CategorySetting.CategoryCondition var4 = new CategorySetting.CategoryCondition(var5);
      Double var6 = 8.0;
      Double var7 = 1.0;
      Double var8 = 16.0;
      this.targetRange = new NumberSetting("TargetRange", "TargetRange", "The range within which players are considered targets.", var4, var6, var7, var8);
      CategorySetting var10 = this.targetingCategory;
      CategorySetting.CategoryCondition var9 = new CategorySetting.CategoryCondition(var10);
      Double var11 = 3.0;
      Double var12 = 1.0;
      Double var13 = 8.0;
      this.maxTargets = new NumberSetting("MaxTargets", "MaxTargets", "Maximum number of targets to evaluate simultaneously.", var9, var11, var12, var13);
      CategorySetting var15 = this.targetingCategory;
      CategorySetting.CategoryCondition var14 = new CategorySetting.CategoryCondition(var15);
      Double var16 = 4.0;
      Double var17 = 1.0;
      Double var18 = 8.0;
      this.bedRange = new NumberSetting("BedRange", "BedRange", "Search cube radius around the target to look for placement spots.", var14, var16, var17, var18);
      CategorySetting var20 = this.targetingCategory;
      CategorySetting.CategoryCondition var19 = new CategorySetting.CategoryCondition(var20);
      this.ignoreFriends = new BooleanSetting("IgnoreFriends", "Ignore friends when targeting.", var19, true);
      this.damageCategory = new CategorySetting("Damage", "Damage calculation and safety settings.");
      CategorySetting var22 = this.damageCategory;
      CategorySetting.CategoryCondition var21 = new CategorySetting.CategoryCondition(var22);
      Double var23 = 6.0;
      Double var24 = 0.0;
      Double var25 = 36.0;
      this.minDamage = new NumberSetting("MinDamage", "MinDamage", "Minimum predicted damage to target.", var21, var23, var24, var25);
      CategorySetting var27 = this.damageCategory;
      CategorySetting.CategoryCondition var26 = new CategorySetting.CategoryCondition(var27);
      Double var28 = 6.0;
      Double var29 = 0.0;
      Double var30 = 36.0;
      this.maxSelfDamage = new NumberSetting("MaxSelfDamage", "MaxSelfDamage", "Maximum allowed damage to yourself.", var26, var28, var29, var30);
      CategorySetting var32 = this.damageCategory;
      CategorySetting.CategoryCondition var31 = new CategorySetting.CategoryCondition(var32);
      Double var33 = 4.0;
      Double var34 = 0.0;
      Double var35 = 36.0;
      this.minHealth = new NumberSetting("MinHealth", "MinHealth", "Do not act if your health (+absorption) is at or below this.", var31, var33, var34, var35);
      CategorySetting var37 = this.damageCategory;
      CategorySetting.CategoryCondition var36 = new CategorySetting.CategoryCondition(var37);
      this.antiSuicide = new BooleanSetting("AntiSuicide", "Cancel if self damage drops health below MinHealth.", var36, true);
      CategorySetting var39 = this.damageCategory;
      CategorySetting.CategoryCondition var38 = new CategorySetting.CategoryCondition(var39);
      Double var40 = 0.0;
      Double var41 = 0.0;
      Double var42 = 10.0;
      this.balance = new NumberSetting("Balance", "Balance", "Minimum target to self damage ratio (0 = off).", var38, var40, var41, var42);
      CategorySetting var44 = this.damageCategory;
      CategorySetting.CategoryCondition var43 = new CategorySetting.CategoryCondition(var44);
      this.predict = new BooleanSetting("Predict", "Extrapolate target velocity for damage estimation.", var43, true);
      this.behaviorCategory = new CategorySetting("Behavior", "Settings for placement and detonation behavior.");
      CategorySetting var46 = this.behaviorCategory;
      CategorySetting.CategoryCondition var45 = new CategorySetting.CategoryCondition(var46);
      Integer var47 = 100;
      Integer var48 = 0;
      Integer var49 = 500;
      this.delay = new NumberSetting("Delay", "Delay", "Delay in milliseconds between actions.", var45, var47, var48, var49);
      CategorySetting var51 = this.behaviorCategory;
      CategorySetting.CategoryCondition var50 = new CategorySetting.CategoryCondition(var51);
      Double var52 = 4.5;
      Double var53 = 1.0;
      Double var54 = 6.0;
      this.range = new NumberSetting("Range", "Range", "Direct reach for placing and detonating beds.", var50, var52, var53, var54);
      CategorySetting var56 = this.behaviorCategory;
      CategorySetting.CategoryCondition var55 = new CategorySetting.CategoryCondition(var56);
      Double var57 = 4.0;
      Double var58 = 0.0;
      Double var59 = 6.0;
      this.wallsRange = new NumberSetting("WallsRange", "WallsRange", "Reach through walls / obstacles.", var55, var57, var58, var59);
      CategorySetting var61 = this.behaviorCategory;
      CategorySetting.CategoryCondition var60 = new CategorySetting.CategoryCondition(var61);
      String[] var62 = new String[]{"Silent", "Normal", "Alt"};
      this.swapMode = new ModeSetting("SwapMode", "SwapMode", "How to swap to the bed and non-bed item.", var60, "Silent", var62);
      CategorySetting var64 = this.behaviorCategory;
      CategorySetting.CategoryCondition var63 = new CategorySetting.CategoryCondition(var64);
      this.rotate = new BooleanSetting("Rotate", "Rotate towards bed before placing and detonating.", var63, true);
      CategorySetting var66 = this.behaviorCategory;
      CategorySetting.CategoryCondition var65 = new CategorySetting.CategoryCondition(var66);
      this.x = new BooleanSetting("StrictDirection", "Enforce strict raycast line of sight on placement face.", var65, false);
      CategorySetting var68 = this.behaviorCategory;
      CategorySetting.CategoryCondition var67 = new CategorySetting.CategoryCondition(var68);
      this.y = new BooleanSetting("AirPlace", "Allow placing beds without a solid supporting block below.", var67, false);
      CategorySetting var70 = this.behaviorCategory;
      CategorySetting.CategoryCondition var69 = new CategorySetting.CategoryCondition(var70);
      this.z = new BooleanSetting("PlaceOnFeet", "Allow bed head to overlap target entity hitbox for maximum damage.", var69, true);
      CategorySetting var72 = this.behaviorCategory;
      CategorySetting.CategoryCondition var71 = new CategorySetting.CategoryCondition(var72);
      this.pauseOnEat = new BooleanSetting("PauseOnEat", "Pause while eating or consuming items.", var71, true);
      this.autoCraftCategory = new CategorySetting("AutoCraft", "Automated bed crafting from wool and planks.");
      CategorySetting var74 = this.autoCraftCategory;
      CategorySetting.CategoryCondition var73 = new CategorySetting.CategoryCondition(var74);
      this.autoCraft = new BooleanSetting("AutoCraft", "Enable automated bed crafting.", var73, true);
      CategorySetting var76 = this.autoCraftCategory;
      CategorySetting.CategoryCondition var75 = new CategorySetting.CategoryCondition(var76);
      KeybindSetting var77 = new KeybindSetting("AutoCraftBind", "Keybind to toggle AutoCraft.", var75, -1).disableHoldMode();
      this.autoCraftBind = var77;
      CategorySetting var79 = this.autoCraftCategory;
      CategorySetting.CategoryCondition var78 = new CategorySetting.CategoryCondition(var79);
      this.autoPlace = new BooleanSetting("AutoPlace", "Place and open a crafting table when out of beds.", var78, true);
      CategorySetting var81 = this.autoCraftCategory;
      CategorySetting.CategoryCondition var80 = new CategorySetting.CategoryCondition(var81);
      this.autoClose = new BooleanSetting("AutoClose", "Close the crafting table GUI automatically when crafting is finished.", var80, true);
      this.renderCategory = new CategorySetting("Render", "Placement preview rendering.");
      CategorySetting var83 = this.renderCategory;
      CategorySetting.CategoryCondition var82 = new CategorySetting.CategoryCondition(var83);
      this.render = new BooleanSetting("Render", "Render preview box at target bed placement.", var82, true);
      CategorySetting var85 = this.renderCategory;
      CategorySetting.CategoryCondition var84 = new CategorySetting.CategoryCondition(var85);
      this.interpolate = new BooleanSetting("Interpolate", "Smooth linear grow-in animation for preview box.", var84, true);
      CategorySetting var87 = this.renderCategory;
      CategorySetting.CategoryCondition var86 = new CategorySetting.CategoryCondition(var87);
      String[] var88 = new String[]{"Fill", "Outline", "Both"};
      this.renderMode = new ModeSetting("RenderMode", "Mode", "Render mode for preview box.", var86, "Both", var88);
      CategorySetting var90 = this.renderCategory;
      CategorySetting.CategoryCondition var89 = new CategorySetting.CategoryCondition(var90);
      ColorSetting.ColorValue var91 = ColorUtil.b();
      this.fillColor = new ColorSetting("FillColor", "FillColor", "Color used for fill rendering.", var89, var91);
      CategorySetting var93 = this.renderCategory;
      CategorySetting.CategoryCondition var92 = new CategorySetting.CategoryCondition(var93);
      ColorSetting.ColorValue var94 = ColorUtil.c();
      this.outlineColor = new ColorSetting("OutlineColor", "OutlineColor", "Color used for outline rendering.", var92, var94);
      this.N = 0;
      this.O = null;
      this.P = null;
      this.Q = null;
      this.R = 0.0F;
      this.S = null;
      this.T = false;
      long var95 = 0L;
      this.U = var95;
      this.V = false;
      this.W = null;
      this.X = null;
      this.Y = null;
      this.Z = new ArrayList<>();
      long var97 = 0L;
      this.aa = var97;
      this.ac = null;
      this.ad = false;
      this.ae = 0;
      this.af = null;
      this.ag = false;
      this.ah = true;
   }

   @Override
   public void onEnable() {
      this.D();
      this.N = 0;
      this.V = false;
      this.W = null;
      this.X = null;
      this.ad = false;
      this.af = null;
      this.ag = false;
   }

   @Override
   public void onDisable() {
      this.D();
      this.ad = false;
      this.af = null;
   }

   @EventTarget
   public void a_void_TickEvent(TickEvent var1) {
      if (mc.player != null && mc.level != null) {
         if (this.autoCraft.getValue() && this.autoPlace.getValue()) {
            boolean var2;
            if (this.G() > 0) {
               var2 = true;
            } else {
               var2 = false;
            }

            boolean var3 = var2;
            if (!var3 && this.ah) {
               this.E();
            }

            boolean var4 = var3;
            this.ah = var4;
         }

         if (this.ad) {
            this.F();
         }

         this.v();
      }
   }

   @EventTarget
   public void a_void_KeyInputEvent(KeyInputEvent var1) {
      if (mc.gui.screen() == null) {
         if (this.autoCraftBind.getKey() != -1) {
            int var2 = var1.getKey();
            int var3 = this.autoCraftBind.getKey();
            if (var2 == var3) {
               this.E();
               Night.chatManager.sendTaggedMessage("AutoCraft triggered!", "BedAura");
            }
         }
      }
   }

   @EventTarget
   public void a_void_MouseButtonEvent(MouseButtonEvent var1) {
      if (mc.gui.screen() == null) {
         if (this.autoCraftBind.getKey() != -1) {
            int var2 = var1.getButton();
            int var3 = this.autoCraftBind.getKey();
            if (var2 == var3) {
               this.E();
               Night.chatManager.sendTaggedMessage("AutoCraft triggered!", "BedAura");
            }
         }
      }
   }

   private void v() {
      ResourceKey var1 = mc.player.level().dimension();
      if (var1 != Level.NETHER && var1 != Level.END) {
         this.D();
      } else if (!this.pauseOnEat.getValue()
         || !mc.player.isUsingItem()
         || !mc.player.getUseItem().has(DataComponents.FOOD) && !mc.player.getUseItem().has(DataComponents.POTION_CONTENTS)) {
         float var2 = mc.player.getHealth();
         float var3 = mc.player.getAbsorptionAmount();
         float var4 = var2 + var3;
         float var5 = this.minHealth.getValue().floatValue();
         if (!(var4 <= var5)) {
            long var6 = System.currentTimeMillis();
            long var8 = this.U;
            long var10 = var6 - var8;
            long var12 = this.delay.getValue().longValue();
            if (var10 >= var12) {
               if (this.V) {
                  this.w();
               } else {
                  boolean var14;
                  if (this.B() != -1) {
                     var14 = true;
                  } else {
                     var14 = false;
                  }

                  if (!var14) {
                     if (this.autoCraft.getValue() && this.G() == 0 && !this.ad) {
                        this.E();
                     }

                     this.D();
                  } else {
                     this.A();
                     if (this.Z.isEmpty()) {
                        this.D();
                     } else {
                        if (this.N <= 0) {
                           this.N = 10;
                           BedAura.BedTarget var15 = this.z();
                           if (var15 != null) {
                              BlockPos var16 = var15.pos();
                              this.O = var16;
                              BlockPos var17 = var15.pos();
                              BlockPos var18 = this.e(var17);
                              this.S = var18;
                              this.P = null;
                              Player var19 = var15.player();
                              this.Q = var19;
                              this.T = true;
                              float var20 = var15.value();
                              this.R = var20;
                              this.V = true;
                              BlockPos var21 = this.O;
                              this.W = var21;
                              BlockPos var22 = this.S;
                              this.X = var22;
                              this.Y = null;
                              this.w();
                              return;
                           }

                           BedAura.DirectionalBedTarget var23 = this.x();
                           if (var23 == null) {
                              this.D();
                              return;
                           }

                           BlockPos var24 = var23.pos();
                           this.O = var24;
                           Direction var25 = var23.direction();
                           this.P = var25;
                           Player var26 = var23.player();
                           this.Q = var26;
                           float var27 = var23.value();
                           this.R = var27;
                           this.T = false;
                           BlockPos var28 = this.O;
                           Direction var29 = this.P;
                           BlockPos var30 = var28.relative(var29);
                           this.S = var30;
                        } else {
                           int var31 = this.N - 1;
                           this.N = var31;
                           if (this.O != null && !this.T && !this.y()) {
                              this.N = 0;
                           }
                        }

                        if (this.O != null && this.P != null && this.Q != null) {
                           BlockPos var32 = this.O;
                           Direction var33 = this.P;
                           float var34 = this.R;
                           this.Y = new BedAura.BedPlacement(var32, var33, var34);
                           BlockPos var35 = this.O;
                           Direction var36 = this.P;
                           this.a_void_BlockPos_Direction(var35, var36);
                        } else {
                           this.D();
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private void a_void_BlockPos_Direction(BlockPos var1, Direction var2) {
      BlockHitResult var3 = this.a_BlockHitResult_BlockPos(var1);
      if (var3 == null) {
         this.D();
         this.N = 4;
      } else {
         float var4 = var2.toYRot();
         float var5 = RotationMathUtil.a(var3.getLocation())[1];
         float var6 = var4;
         float var7 = var5;
         Night.rotationManager.sendOrBatchPosRot(var6, var7);
         BlockHitResult var8 = var3;
         if (this.a_bool_BlockPos_BlockHitResult(var1, var8)) {
            this.V = true;
            this.W = var1;
            BlockPos var9 = var1.relative(var2);
            this.X = var9;
            long var10 = System.currentTimeMillis();
            this.U = var10;
         }
      }
   }

   private void w() {
      if (this.W == null) {
         this.V = false;
      } else {
         BlockPos var1 = this.W;
         if (this.X != null && this.Q != null) {
            Vec3 var2 = Vec3.atCenterOf(this.W);
            Player var3 = this.Q;
            float var4 = this.a_float_Vec3_Player(var2, var3);
            Vec3 var5 = Vec3.atCenterOf(this.X);
            Player var6 = this.Q;
            float var7 = this.a_float_Vec3_Player(var5, var6);
            float var8 = var4;
            if (var7 > var8) {
               var1 = this.X;
            }
         }

         BlockPos var9 = var1;
         BlockHitResult var10 = this.b_BlockHitResult_BlockPos(var9);
         if (var10 == null) {
            this.V = false;
            this.W = null;
            this.X = null;
            this.Y = null;
            this.N = 4;
         } else {
            int var11 = this.C();
            float[] var12 = RotationMathUtil.a(var10.getLocation());
            float var13 = var12[0];
            float var14 = var12[1];
            Night.rotationManager.sendOrBatchPosRot(var13, var14);
            int var15 = mc.player.getInventory().getSelectedSlot();
            boolean var16 = false;
            String var17;
            if (this.swapMode.getValue().equalsIgnoreCase("Alt")) {
               var17 = "AltPickup";
            } else {
               var17 = this.swapMode.getValue();
            }

            String var18 = var17;
            if (var11 != Integer.MIN_VALUE) {
               String var19 = var18;
               int var20 = var11;
               int var21 = var15;
               var16 = InventoryUtil.a_bool_String_int_int(var19, var20, var21);
            }

            try {
               MultiPlayerGameMode var22 = mc.gameMode;
               LocalPlayer var23 = mc.player;
               BlockHitResult var24 = var10;
               var22.useItemOn(var23, InteractionHand.MAIN_HAND, var24);
               ClientPacketListener var25 = mc.getConnection();
               Packet var26 = PacketAndRenderUtil.a_Packet_InteractionHand(InteractionHand.MAIN_HAND);
               var25.send(var26);
            } catch (Throwable var36) {
               if (var16) {
                  String var31 = var18;
                  int var32 = var11;
                  int var33 = var15;
                  InventoryUtil.b(var31, var32, var33);
               }

               throw var36;
            }

            if (var16) {
               String var27 = var18;
               int var28 = var11;
               int var29 = var15;
               InventoryUtil.b(var27, var28, var29);
            }

            this.V = false;
            this.W = null;
            this.X = null;
            this.Y = null;
            long var34 = System.currentTimeMillis();
            this.U = var34;
         }
      }
   }

   private boolean a_bool_BlockPos_BlockHitResult(BlockPos var1, BlockHitResult var2) {
      int var3 = this.B();
      if (var3 == -1) {
         return false;
      }

      String var4;
      if (this.swapMode.getValue().equalsIgnoreCase("Alt")) {
         var4 = "AltPickup";
      } else {
         var4 = this.swapMode.getValue();
      }

      String var5 = var4;
      int var6 = mc.player.getInventory().getSelectedSlot();
      String var7 = var5;
      int var8 = var3;
      int var9 = var6;
      boolean var10 = InventoryUtil.a_bool_String_int_int(var7, var8, var9);
      if (!var10 && mc.getConnection() != null) {
         ClientPacketListener var11 = mc.getConnection();
         int var12 = mc.player.getInventory().getSelectedSlot();
         var11.send(new ServerboundSetCarriedItemPacket(var12));
      }

      ClientLevel var13 = mc.level;
      BlockPos var14 = var2.getBlockPos();
      boolean var15;
      if (BlockInteractionUtil.a_bool_BlockState(var13.getBlockState(var14)) && !mc.player.isShiftKeyDown()) {
         var15 = true;
      } else {
         var15 = false;
      }

      boolean var16 = var15;
      if (var16) {
         Input var17;
         if (mc.player.input != null) {
            var17 = mc.player.input.keyPresses;
         } else {
            var17 = new Input(false, false, false, false, false, false, false);
         }

         Input var18 = var17;
         boolean var19 = var18.forward();
         boolean var20 = var18.backward();
         boolean var21 = var18.left();
         boolean var22 = var18.right();
         boolean var23 = var18.jump();
         boolean var24 = var18.sprint();
         Input var25 = new Input(var19, var20, var21, var22, var23, true, var24);
         ClientPacketListener var26 = mc.getConnection();
         Input var27 = var25;
         var26.send(new ServerboundPlayerInputPacket(var27));
         if (mc.player instanceof ClientPlayerEntityAccessor var29) {
            Input var30 = var25;
            var29.setLastSentInput(var30);
         }
      }

      try {
         MultiPlayerGameMode var31 = mc.gameMode;
         LocalPlayer var32 = mc.player;
         var31.useItemOn(var32, InteractionHand.MAIN_HAND, var2);
         ClientPacketListener var33 = mc.getConnection();
         Packet var34 = PacketAndRenderUtil.a_Packet_InteractionHand(InteractionHand.MAIN_HAND);
         var33.send(var34);
      } catch (Throwable var56) {
         if (var16) {
            Input var37;
            if (mc.player.input != null) {
               var37 = mc.player.input.keyPresses;
            } else {
               var37 = new Input(false, false, false, false, false, false, false);
            }

            Input var38 = var37;
            ClientPacketListener var39 = mc.getConnection();
            Input var40 = var38;
            var39.send(new ServerboundPlayerInputPacket(var40));
            if (mc.player instanceof ClientPlayerEntityAccessor var42) {
               Input var43 = var38;
               var42.setLastSentInput(var43);
            }
         }

         if (var10) {
            String var44 = var5;
            int var45 = var3;
            int var46 = var6;
            InventoryUtil.b(var44, var45, var46);
         }

         throw var56;
      }

      if (var16) {
         Input var35;
         if (mc.player.input != null) {
            var35 = mc.player.input.keyPresses;
         } else {
            var35 = new Input(false, false, false, false, false, false, false);
         }

         Input var47 = var35;
         ClientPacketListener var48 = mc.getConnection();
         Input var49 = var47;
         var48.send(new ServerboundPlayerInputPacket(var49));
         if (mc.player instanceof ClientPlayerEntityAccessor var51) {
            Input var52 = var47;
            var51.setLastSentInput(var52);
         }
      }

      if (var10) {
         String var53 = var5;
         int var54 = var3;
         int var55 = var6;
         InventoryUtil.b(var53, var54, var55);
      }

      return true;
   }

   private boolean a_bool_Vec3_Vec3_BlockPos(Vec3 var1, Vec3 var2, BlockPos var3) {
      ClientLevel var4 = mc.level;
      LocalPlayer var5 = mc.player;
      BlockHitResult var6 = var4.clip(new ClipContext(var1, var2, Block.COLLIDER, Fluid.NONE, var5));
      if (var6.getType() == Type.MISS) {
         return true;
      } else {
         return var6 instanceof BlockHitResult ? var6.getBlockPos().equals(var3) : false;
      }
   }

   private BlockHitResult a_BlockHitResult_BlockPos(BlockPos var1) {
      double var2 = this.range.getValue().doubleValue();
      double var4 = this.wallsRange.getValue().doubleValue();
      double var6 = Math.max(var2, var4);
      if (!this.b(var1, var6)) {
         return null;
      }

      BlockPos var8 = var1.below();
      ClientLevel var9 = mc.level;
      BlockPos var10 = var8;
      boolean var11;
      if (!var9.getBlockState(var10).canBeReplaced()) {
         var11 = true;
      } else {
         var11 = false;
      }

      boolean var12 = var11;
      Vec3 var13 = mc.player.getEyePosition();
      if (var12) {
         double var14 = var1.getX();
         double var16 = 0.5;
         double var18 = var14 + var16;
         double var20 = var1.getY();
         double var22 = var1.getZ();
         double var24 = 0.5;
         double var26 = var22 + var24;
         Vec3 var28 = new Vec3(var18, var20, var26);
         if (this.x.getValue()) {
            double var29 = var13.y;
            double var31 = var1.getY();
            if (var29 < var31) {
               return null;
            }

            Vec3 var33 = var13;
            Vec3 var34 = var28;
            BlockPos var35 = var8;
            if (!this.a_bool_Vec3_Vec3_BlockPos(var33, var34, var35)) {
               return null;
            }
         }

         Vec3 var116 = var28;
         BlockPos var117 = var8;
         return new BlockHitResult(var116, Direction.UP, var117, false);
      } else {
         if (!this.y.getValue()) {
            return null;
         }

         Direction[] var36 = Direction.values();
         int var37 = var36.length;
         int var38 = 0;

         BlockPos var44;
         Direction var47;
         Vec3 var90;
         while (true) {
            int var39 = var38;
            int var40 = var37;
            if (var39 >= var40) {
               if (this.x.getValue()) {
                  return null;
               }

               double var94 = var1.getX();
               double var96 = 0.5;
               double var98 = var94 + var96;
               double var100 = var1.getY();
               double var102 = 0.5;
               double var104 = var100 + var102;
               double var106 = var1.getZ();
               double var108 = 0.5;
               double var110 = var106 + var108;
               Vec3 var112 = new Vec3(var98, var104, var110);
               return new BlockHitResult(var112, Direction.UP, var1, false);
            }

            Direction[] var41 = var36;
            int var42 = var38;
            Direction var43 = var41[var42];
            var44 = var1.relative(var43);
            ClientLevel var45 = mc.level;
            BlockPos var46 = var44;
            if (!var45.getBlockState(var46).canBeReplaced()) {
               var47 = var43.getOpposite();
               double var48 = var1.getX();
               double var50 = 0.5;
               double var52 = var48 + var50;
               double var54 = var43.getStepX();
               double var56 = 0.5;
               double var58 = var54 * var56;
               double var60 = var52 - var58;
               double var62 = var1.getY();
               double var64 = 0.5;
               double var66 = var62 + var64;
               double var68 = var43.getStepY();
               double var70 = 0.5;
               double var72 = var68 * var70;
               double var74 = var66 - var72;
               double var76 = var1.getZ();
               double var78 = 0.5;
               double var80 = var76 + var78;
               double var82 = var43.getStepZ();
               double var84 = 0.5;
               double var86 = var82 * var84;
               double var88 = var80 - var86;
               var90 = new Vec3(var60, var74, var88);
               if (!this.x.getValue()) {
                  break;
               }

               Vec3 var91 = var13;
               Vec3 var92 = var90;
               BlockPos var93 = var44;
               if (this.a_bool_Vec3_Vec3_BlockPos(var91, var92, var93)) {
                  break;
               }
            }

            var38++;
         }

         Vec3 var113 = var90;
         Direction var114 = var47;
         BlockPos var115 = var44;
         return new BlockHitResult(var113, var114, var115, false);
      }
   }

   private BlockHitResult b_BlockHitResult_BlockPos(BlockPos var1) {
      Vec3 var2 = mc.player.getEyePosition();
      double var3 = this.range.getValue().doubleValue();
      double var5 = this.wallsRange.getValue().doubleValue();
      double var7 = Math.max(var3, var5);
      Vec3 var9 = Vec3.atCenterOf(var1);
      BlockHitResult var10 = null;
      double var11 = Double.MAX_VALUE;
      BlockHitResult var13 = null;
      double var14 = Double.MAX_VALUE;
      Direction[] var16 = Direction.values();
      int var17 = var16.length;
      int var18 = 0;

      while (true) {
         int var19 = var18;
         int var20 = var17;
         if (var19 >= var20) {
            BlockHitResult var73;
            if (var10 != null) {
               var73 = var10;
            } else {
               var73 = var13;
            }

            return var73;
         }

         Direction[] var21 = var16;
         int var22 = var18;
         Direction var23 = var21[var22];
         double var24 = var23.getStepX();
         double var26 = var23.getStepY();
         double var28 = var23.getStepZ();
         Vec3 var30 = new Vec3(var24, var26, var28);
         Vec3 var31 = var9;
         Vec3 var32 = var30;
         double var33 = 0.5;
         Vec3 var35 = var32.scale(var33);
         Vec3 var36 = var31.add(var35);
         Vec3 var37 = var2;
         Vec3 var38 = var36;
         Vec3 var39 = var37.subtract(var38);
         Vec3 var40 = var30;
         double var41 = var39.dot(var40);
         double var43 = 0.0;
         if (!(var41 <= var43)) {
            Vec3 var45 = var36;
            Vec3 var46 = var2;
            double var47 = var45.distanceTo(var46);
            double var49 = var7;
            label46:
            if (!(var47 > var49)) {
               double var51 = var47;
               double var53 = var14;
               if (var51 < var53) {
                  var14 = var47;
                  Vec3 var55 = var36;
                  Direction var56 = var23;
                  var13 = new BlockHitResult(var55, var56, var1, false);
               }

               ClientLevel var57 = mc.level;
               Vec3 var58 = var2;
               Vec3 var59 = var36;
               LocalPlayer var60 = mc.player;
               BlockHitResult var61 = var57.clip(new ClipContext(var58, var59, Block.COLLIDER, Fluid.NONE, var60));
               if (var61.getType() == Type.BLOCK && var61 instanceof BlockHitResult) {
                  BlockPos var62 = var61.getBlockPos();
                  label41:
                  if (!var62.equals(var1)) {
                     if (this.W != null) {
                        BlockPos var63 = var62;
                        BlockPos var64 = this.W;
                        if (var63.equals(var64)) {
                           break label41;
                        }
                     }

                     if (this.X == null) {
                        break label46;
                     }

                     BlockPos var65 = var62;
                     BlockPos var66 = this.X;
                     if (!var65.equals(var66)) {
                        break label46;
                     }
                  }
               }

               double var67 = var47;
               double var69 = var11;
               if (var67 < var69) {
                  var11 = var47;
                  Vec3 var71 = var36;
                  Direction var72 = var23;
                  var10 = new BlockHitResult(var71, var72, var1, false);
               }
            }
         }

         var18++;
      }
   }

   private BedAura.DirectionalBedTarget x() {
      int var1 = (int)Math.ceil(this.bedRange.getValue().doubleValue());
      double var2 = this.minDamage.getValue().doubleValue();
      double var4 = this.maxSelfDamage.getValue().doubleValue();
      double var6 = this.range.getValue().doubleValue();
      double var8 = this.wallsRange.getValue().doubleValue();
      double var10 = Math.max(var6, var8);
      BedAura.DirectionalBedTarget var12 = null;
      double var13 = -1.0;
      Vec3 var15 = mc.player.getEyePosition();
      double var16 = var10;
      double var18 = 0.5;
      double var20 = var16 + var18;

      for (Player var23 : (Iterable<Player>)(Object)this.Z) {
         BlockPos var24 = var23.blockPosition();
         Vec3 var25 = var23.getBoundingBox().getCenter();
         Vec3 var26 = Vec3.atCenterOf(var24);
         Vec3 var27 = var15;
         double var28 = var26.distanceTo(var27);
         double var30 = var1;
         double var32 = Math.sqrt(3.0);
         double var34 = var30 * var32;
         double var36 = var20;
         double var38 = var34 + var36;
         if (!(var28 > var38)) {
            int var40 = -var1;
            double var41 = var15.x;
            double var43 = var20;
            double var45 = var41 - var43;
            double var47 = var24.getX();
            int var49 = (int)Math.floor(var45 - var47);
            int var50 = Math.max(var40, var49);
            int var51 = var1;
            double var52 = var15.x;
            double var54 = var20;
            double var56 = var52 + var54;
            double var58 = var24.getX();
            int var60 = (int)Math.ceil(var56 - var58);
            int var61 = Math.min(var51, var60);
            int var62 = var50;
            int var63 = var61;
            if (var62 <= var63) {
               int var64 = -var1;
               double var65 = var15.y;
               double var67 = var20;
               double var69 = var65 - var67;
               double var71 = var24.getY();
               int var73 = (int)Math.floor(var69 - var71);
               int var74 = Math.max(var64, var73);
               int var75 = var1;
               double var76 = var15.y;
               double var78 = var20;
               double var80 = var76 + var78;
               double var82 = var24.getY();
               int var84 = (int)Math.ceil(var80 - var82);
               int var85 = Math.min(var75, var84);
               int var86 = var74;
               int var87 = var85;
               if (var86 <= var87) {
                  int var88 = -var1;
                  double var89 = var15.z;
                  double var91 = var20;
                  double var93 = var89 - var91;
                  double var95 = var24.getZ();
                  int var97 = (int)Math.floor(var93 - var95);
                  int var98 = Math.max(var88, var97);
                  int var99 = var1;
                  double var100 = var15.z;
                  double var102 = var20;
                  double var104 = var100 + var102;
                  double var106 = var24.getZ();
                  int var108 = (int)Math.ceil(var104 - var106);
                  int var109 = Math.min(var99, var108);
                  int var110 = var98;
                  int var111 = var109;
                  if (var110 <= var111) {
                     int var112 = var50;

                     while (true) {
                        int var113 = var112;
                        int var114 = var61;
                        if (var113 > var114) {
                           break;
                        }

                        int var115 = var74;

                        while (true) {
                           int var116 = var115;
                           int var117 = var85;
                           if (var116 > var117) {
                              var112++;
                              break;
                           }

                           int var118 = var98;

                           while (true) {
                              int var119 = var118;
                              int var120 = var109;
                              if (var119 > var120) {
                                 var115++;
                                 break;
                              }

                              BlockPos var121 = var24;
                              int var122 = var112;
                              int var123 = var115;
                              int var124 = var118;
                              BlockPos var125 = var121.offset(var122, var123, var124);
                              Vec3 var126 = Vec3.atCenterOf(var125);
                              Vec3 var127 = var15;
                              double var128 = var126.distanceTo(var127);
                              double var130 = var20;
                              if (!(var128 > var130)) {
                                 BlockPos var132 = var125;
                                 double var133 = var10;
                                 if (this.a_bool_BlockPos_double(var132, var133)) {
                                    for (Direction var136 : Plane.HORIZONTAL) {
                                       BlockPos var137 = var125;
                                       Direction var138 = var136;
                                       BlockPos var139 = var137.relative(var138);
                                       if (this.c_bool_BlockPos(var139)) {
                                          Vec3 var140 = Vec3.atCenterOf(var125);
                                          Vec3 var141 = Vec3.atCenterOf(var139);
                                          Vec3 var142 = var140;
                                          Player var143 = var23;
                                          float var144 = this.a_float_Vec3_Player(var142, var143);
                                          Vec3 var145 = var141;
                                          Player var146 = var23;
                                          float var147 = this.a_float_Vec3_Player(var145, var146);
                                          float var148 = var144;
                                          boolean var149;
                                          if (var147 >= var148) {
                                             var149 = true;
                                          } else {
                                             var149 = false;
                                          }

                                          Vec3 var150;
                                          if (var149) {
                                             var150 = var141;
                                          } else {
                                             var150 = var140;
                                          }

                                          Vec3 var151 = var150;
                                          float var152 = var144;
                                          float var153 = var147;
                                          float var154 = Math.max(var152, var153);
                                          double var155 = var154;
                                          double var157 = var2;
                                          if (!(var155 < var157)) {
                                             Vec3 var159 = var151;
                                             LocalPlayer var160 = mc.player;
                                             float var161 = this.a_float_Vec3_Player(var159, var160);
                                             double var162 = var161;
                                             double var164 = var4;
                                             if (!(var162 > var164)) {
                                                float var166 = mc.player.getHealth();
                                                float var167 = mc.player.getAbsorptionAmount();
                                                float var168 = var166 + var167;
                                                if (this.antiSuicide.getValue()) {
                                                   float var169 = var168;
                                                   float var170 = var161;
                                                   float var171 = var169 - var170;
                                                   float var172 = this.minHealth.getValue().floatValue();
                                                   if (var171 <= var172) {
                                                      continue;
                                                   }
                                                }

                                                double var173 = this.balance.getValue().doubleValue();
                                                double var175 = 0.0;
                                                if (var173 > var175 && var161 > 0.0F) {
                                                   float var177 = var154;
                                                   float var178 = var161;
                                                   double var179 = var177 / var178;
                                                   double var181 = this.balance.getValue().doubleValue();
                                                   if (var179 < var181) {
                                                      continue;
                                                   }
                                                }

                                                double var183 = var154;
                                                Vec3 var185 = var141;
                                                Vec3 var186 = var25;
                                                double var187 = var185.distanceTo(var186);
                                                Vec3 var189 = var140;
                                                Vec3 var190 = var25;
                                                double var191 = var189.distanceTo(var190);
                                                if (var187 < var191) {
                                                   double var193 = var183;
                                                   double var195 = 0.01;
                                                   var183 = var193 + var195;
                                                }

                                                if (var12 != null) {
                                                   double var197 = var183;
                                                   double var199 = var13;
                                                   if (!(var197 > var199)) {
                                                      continue;
                                                   }
                                                }

                                                BlockPos var201 = var125;
                                                Direction var202 = var136;
                                                Player var203 = var23;
                                                float var204 = var154;
                                                var12 = new BedAura.DirectionalBedTarget(var201, var202, var203, var204);
                                                var13 = var183;
                                             }
                                          }
                                       }
                                    }
                                 }
                              }

                              var118++;
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      return var12;
   }

   private boolean a_bool_BlockPos_double(BlockPos var1, double var2) {
      double var4 = var2;
      if (!this.b(var1, var4)) {
         return false;
      }

      if (!mc.level.getBlockState(var1).canBeReplaced()) {
         return false;
      }

      if (!this.y.getValue()) {
         ClientLevel var6 = mc.level;
         BlockPos var7 = var1.below();
         if (var6.getBlockState(var7).canBeReplaced()) {
            return false;
         }
      }

      return !this.x.getValue() || this.a_BlockHitResult_BlockPos(var1) != null;
   }

   private boolean c_bool_BlockPos(BlockPos var1) {
      if (!mc.level.getBlockState(var1).canBeReplaced()) {
         return false;
      }

      if (this.z.getValue()) {
         return true;
      }

      boolean var2;
      if (!this.d_bool_BlockPos(var1)) {
         var2 = true;
      } else {
         var2 = false;
      }

      return var2;
   }

   private boolean d_bool_BlockPos(BlockPos var1) {
      AABB var2 = new AABB(var1);

      for (Entity var4 : (Iterable<Entity>)(Object)mc.level.entitiesForRendering()) {
         LocalPlayer var5 = mc.player;
         if (var4 != var5 && !(var4 instanceof ItemEntity) && var4.isAlive()) {
            AABB var6 = var4.getBoundingBox();
            AABB var7 = var2;
            if (var6.intersects(var7)) {
               return true;
            }
         }
      }

      return false;
   }

   private boolean y() {
      if (this.O != null && this.P != null && this.Q != null) {
         if (this.Q.isAlive() && !(this.Q.getHealth() <= 0.0F)) {
            BlockPos var1 = this.O;
            Direction var2 = this.P;
            BlockPos var3 = var1.relative(var2);
            double var4 = this.range.getValue().doubleValue();
            double var6 = this.wallsRange.getValue().doubleValue();
            double var8 = Math.max(var4, var6);
            BlockPos var10 = this.O;
            double var11 = var8;
            if (!this.b(var10, var11)) {
               BlockPos var13 = var3;
               double var14 = var8;
               if (!this.b(var13, var14)) {
                  return false;
               }
            }

            if (this.x.getValue()) {
               BlockPos var16 = this.O;
               if (this.a_BlockHitResult_BlockPos(var16) == null) {
                  return false;
               }
            }

            Vec3 var17 = Vec3.atCenterOf(this.O);
            Vec3 var18 = Vec3.atCenterOf(var3);
            Vec3 var19 = var17;
            Player var20 = this.Q;
            float var21 = this.a_float_Vec3_Player(var19, var20);
            Vec3 var22 = var18;
            Player var23 = this.Q;
            float var24 = this.a_float_Vec3_Player(var22, var23);
            float var25 = var21;
            float var26 = var24;
            float var27 = Math.max(var25, var26);
            double var28 = var27;
            double var30 = this.minDamage.getValue().doubleValue();
            if (var28 < var30) {
               return false;
            }

            float var32 = var24;
            float var33 = var21;
            Vec3 var34;
            if (var32 >= var33) {
               var34 = var18;
            } else {
               var34 = var17;
            }

            Vec3 var35 = var34;
            LocalPlayer var36 = mc.player;
            double var37 = this.a_float_Vec3_Player(var35, var36);
            double var39 = this.maxSelfDamage.getValue().doubleValue();
            if (var37 > var39) {
               return false;
            }

            float var41 = var27;
            this.R = var41;
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private BedAura.BedTarget z() {
      double var1 = this.range.getValue().doubleValue();
      double var3 = this.wallsRange.getValue().doubleValue();
      double var5 = Math.max(var1, var3);
      int var7 = (int)Math.ceil(this.bedRange.getValue().doubleValue());
      BedAura.BedTarget var8 = null;

      for (Player var10 : (Iterable<Player>)(Object)this.Z) {
         BlockPos var11 = var10.blockPosition();
         Vec3 var12 = var10.getBoundingBox().getCenter();
         int var13 = -var7;

         while (true) {
            int var14 = var13;
            int var15 = var7;
            if (var14 > var15) {
               break;
            }

            int var16 = -var7;

            while (true) {
               int var17 = var16;
               int var18 = var7;
               if (var17 > var18) {
                  var13++;
                  break;
               }

               int var19 = -var7;

               while (true) {
                  int var20 = var19;
                  int var21 = var7;
                  if (var20 > var21) {
                     var16++;
                     break;
                  }

                  BlockPos var22 = var11;
                  int var23 = var13;
                  int var24 = var16;
                  int var25 = var19;
                  BlockPos var26 = var22.offset(var23, var24, var25);
                  ClientLevel var27 = mc.level;
                  BlockPos var28 = var26;
                  if (var27.getBlockState(var28).getBlock() instanceof BedBlock) {
                     Vec3 var29 = Vec3.atCenterOf(var26);
                     Vec3 var30 = var12;
                     double var31 = var29.distanceTo(var30);
                     double var33 = 6.0;
                     if (!(var31 > var33)) {
                        BlockPos var35 = var26;
                        double var36 = var5;
                        if (this.b(var35, var36)) {
                           Vec3 var38 = Vec3.atCenterOf(var26);
                           LocalPlayer var39 = mc.player;
                           double var40 = this.a_float_Vec3_Player(var38, var39);
                           double var42 = this.maxSelfDamage.getValue().doubleValue();
                           if (!(var40 > var42)) {
                              Vec3 var44 = Vec3.atCenterOf(var26);
                              Player var45 = var10;
                              float var46 = this.a_float_Vec3_Player(var44, var45);
                              double var47 = var46;
                              double var49 = this.minDamage.getValue().doubleValue();
                              label44:
                              if (!(var47 < var49)) {
                                 if (var8 != null) {
                                    float var51 = var46;
                                    float var52 = var8.value();
                                    if (!(var51 > var52)) {
                                       break label44;
                                    }
                                 }

                                 BlockPos var53 = var26;
                                 Player var54 = var10;
                                 float var55 = var46;
                                 var8 = new BedAura.BedTarget(var53, var54, var55);
                              }
                           }
                        }
                     }
                  }

                  var19++;
               }
            }
         }
      }

      return var8;
   }

   private float a_float_Vec3_Player(Vec3 var1, Player var2) {
      if (this.predict.getValue()) {
         Vec3 var3 = var2.position();
         Vec3 var4 = var2.getDeltaMovement();
         Vec3 var5 = var3.add(var4);
         AABB var6 = var2.getBoundingBox();
         Vec3 var7 = var5;
         Vec3 var8 = var2.position();
         Vec3 var9 = var7.subtract(var8);
         AABB var10 = var6.move(var9);
         return DamageUtil.a_float_Entity_AABB_Vec3_float_BlockPos_bool(var2, var10, var1, 5.0F, null, false);
      } else {
         AABB var11 = var2.getBoundingBox();
         return DamageUtil.a_float_Entity_AABB_Vec3_float_BlockPos_bool(var2, var11, var1, 5.0F, null, false);
      }
   }

   private boolean b(BlockPos var1, double var2) {
      Vec3 var4 = Vec3.atCenterOf(var1);
      Vec3 var5 = mc.player.getEyePosition();
      double var6 = var4.distanceTo(var5);
      double var8 = var2;
      double var10 = 0.5;
      double var12 = var8 + var10;
      boolean var14;
      if (var6 <= var12) {
         var14 = true;
      } else {
         var14 = false;
      }

      return var14;
   }

   private void A() {
      this.Z.clear();
      Vec3 var1 = mc.player.getEyePosition();
      ArrayList var2 = new ArrayList();

      for (Entity var4 : (Iterable<Entity>)(Object)mc.level.entitiesForRendering()) {
         if (var4 instanceof Player var5) {
            LocalPlayer var6 = mc.player;
            if (var5 != var6 && var5.isAlive() && !(var5.getHealth() <= 0.0F)) {
               if (this.ignoreFriends.getValue()) {
                  String var7 = var5.getName().getString();
                  if (Night.friendManager.isFriend(var7)) {
                     continue;
                  }
               }

               Vec3 var8 = Vec3.atCenterOf(var5.blockPosition());
               Vec3 var9 = var1;
               double var10 = var8.distanceTo(var9);
               double var12 = this.targetRange.getValue().doubleValue();
               if (var10 > var12) {
                  Player var14 = var5;
                  LocalPlayer var15 = mc.player;
                  double var16 = var14.distanceTo(var15);
                  double var18 = this.targetRange.getValue().doubleValue();
                  if (var16 > var18) {
                     continue;
                  }
               }

               ArrayList var20 = var2;
               Player var21 = var5;
               var20.add(var21);
            }
         }
      }
      Comparator var25 = switch (this.target.getValue()) {
         case "Furthest" -> Comparator.<Player>comparingDouble(var0 -> {
            LocalPlayer var1x = mc.player;
            return var0.distanceTo(var1x);
         }).reversed();
         case "Health" -> Comparator.comparingDouble(LivingEntity::getHealth);
         default -> Comparator.<Player>comparingDouble(var0 -> {
            LocalPlayer var1x = mc.player;
            return var0.distanceTo(var1x);
         });
      };
      ArrayList var26 = var2;
      Comparator var27 = var25;
      var26.sort(var27);
      int var28 = this.maxTargets.getValue().intValue();

      for (Player var30 : (Iterable<Player>)(Object)var2) {
         int var31 = this.Z.size();
         int var32 = var28;
         if (var31 >= var32) {
            break;
         }

         List var33 = this.Z;
         Player var34 = var30;
         var33.add(var34);
      }
   }

   private BlockPos e(BlockPos var1) {
      for (Direction var3 : Plane.HORIZONTAL) {
         BlockPos var4 = var1.relative(var3);
         ClientLevel var5 = mc.level;
         BlockPos var6 = var4;
         if (var5.getBlockState(var6).getBlock() instanceof BedBlock) {
            return var4;
         }
      }

      return null;
   }

   private int B() {
      boolean var1 = this.swapMode.getValue().equalsIgnoreCase("Alt");
      Predicate<ItemStack> var2 = var0 -> {
         boolean var1x;
         if (!var0.isEmpty() && a_bool_Item(var0.getItem())) {
            var1x = true;
         } else {
            var1x = false;
         }

         return var1x;
      };
      if (var1) {
         for (int var3 = 0; var3 < 36; var3++) {
            Predicate var4 = var2;
            Inventory var5 = mc.player.getInventory();
            int var6 = var3;
            ItemStack var7 = var5.getItem(var6);
            if (var4.test(var7)) {
               return var3;
            }
         }

         return -1;
      } else {
         for (int var8 = 0; var8 < 9; var8++) {
            Predicate var9 = var2;
            Inventory var10 = mc.player.getInventory();
            int var11 = var8;
            ItemStack var12 = var10.getItem(var11);
            if (var9.test(var12)) {
               return var8;
            }
         }

         return -1;
      }
   }

   private int C() {
      boolean var1 = this.swapMode.getValue().equalsIgnoreCase("Alt");
      Predicate<ItemStack> var2 = var0 -> {
         boolean var1x;
         if (!var0.isEmpty() && !a_bool_Item(var0.getItem()) && !(var0.getItem() instanceof BlockItem)) {
            var1x = true;
         } else {
            var1x = false;
         }

         return var1x;
      };
      if (var1) {
         for (int var3 = 0; var3 < 36; var3++) {
            Predicate var4 = var2;
            Inventory var5 = mc.player.getInventory();
            int var6 = var3;
            ItemStack var7 = var5.getItem(var6);
            if (var4.test(var7)) {
               return var3;
            }
         }
      } else {
         for (int var8 = 0; var8 < 9; var8++) {
            Predicate var9 = var2;
            Inventory var10 = mc.player.getInventory();
            int var11 = var8;
            ItemStack var12 = var10.getItem(var11);
            if (var9.test(var12)) {
               return var8;
            }
         }
      }

      for (int var13 = 0; var13 < 9; var13++) {
         Inventory var14 = mc.player.getInventory();
         int var15 = var13;
         if (var14.getItem(var15).isEmpty()) {
            return var13;
         }
      }

      for (int var16 = 0; var16 < 9; var16++) {
         Inventory var17 = mc.player.getInventory();
         int var18 = var16;
         if (!a_bool_Item(var17.getItem(var18).getItem())) {
            return var16;
         }
      }

      return Integer.MIN_VALUE;
   }

   private void D() {
      this.O = null;
      this.P = null;
      this.Q = null;
      this.R = 0.0F;
      this.S = null;
      this.T = false;
      this.Y = null;
   }

   @EventTarget
   public void a_void_Render3DEvent(Render3DEvent var1) {
      if (this.render.getValue() && mc.level != null && mc.player != null) {
         BlockPos var2;
         BlockPos var5;
         if (this.V && this.W != null) {
            var2 = this.W;
            BlockPos var3;
            if (this.X != null) {
               var3 = this.X;
            } else {
               BlockPos var4 = var2;
               var3 = this.e(var4);
            }

            var5 = var3;
            if (var5 != null) {
               BlockPos var6 = var5;
               this.X = var6;
            }
         } else if (this.Y != null) {
            var2 = this.Y.pos();
            BlockPos var7 = var2;
            Direction var8 = this.Y.direction();
            var5 = var7.relative(var8);
         } else {
            if (this.O == null) {
               this.ac = null;
               return;
            }

            var2 = this.O;
            BlockPos var9;
            if (this.S != null) {
               var9 = this.S;
            } else if (this.P != null) {
               BlockPos var10 = this.O;
               Direction var11 = this.P;
               var9 = var10.relative(var11);
            } else {
               BlockPos var12 = var2;
               var9 = this.e(var12);
            }

            var5 = var9;
         }

         BlockPos var13 = var2;
         BlockPos var14 = this.ac;
         if (!var13.equals(var14)) {
            BlockPos var15 = var2;
            this.ac = var15;
            long var16 = System.currentTimeMillis();
            this.aa = var16;
         }

         float var18 = 1.0F;
         if (this.interpolate.getValue()) {
            long var19 = System.currentTimeMillis();
            long var21 = this.aa;
            float var23 = (float)(var19 - var21) / 350.0F;
            float var24 = Math.min(1.0F, var23);
            float var25 = 0.85F * var24;
            var18 = 0.15F + var25;
         }

         double var26 = 0.5625;
         PoseStack var28 = var1.getMatrices();
         double var29 = var2.getX();
         double var31 = var2.getY();
         double var33 = var2.getZ();
         double var35 = var2.getX();
         double var37 = 1.0;
         double var39 = var35 + var37;
         double var41 = var2.getY();
         double var43 = 0.5625;
         double var45 = var41 + var43;
         double var47 = var2.getZ();
         double var49 = 1.0;
         double var51 = var47 + var49;
         if (var5 != null) {
            int var53 = var2.getX();
            int var54 = var5.getX();
            var29 = Math.min(var53, var54);
            int var55 = var2.getY();
            int var56 = var5.getY();
            var31 = Math.min(var55, var56);
            int var57 = var2.getZ();
            int var58 = var5.getZ();
            var33 = Math.min(var57, var58);
            int var59 = var2.getX();
            int var60 = var5.getX();
            double var61 = Math.max(var59, var60);
            double var63 = 1.0;
            var39 = var61 + var63;
            int var65 = var2.getY();
            int var66 = var5.getY();
            double var67 = Math.max(var65, var66);
            double var69 = 0.5625;
            var45 = var67 + var69;
            int var71 = var2.getZ();
            int var72 = var5.getZ();
            double var73 = Math.max(var71, var72);
            double var75 = 1.0;
            var51 = var73 + var75;
         }

         double var77 = var29;
         double var79 = var39;
         double var81 = var77 + var79;
         double var83 = 0.5;
         double var85 = var81 * var83;
         double var87 = var31;
         double var89 = var45;
         double var91 = var87 + var89;
         double var93 = 0.5;
         double var95 = var91 * var93;
         double var97 = var33;
         double var99 = var51;
         double var101 = var97 + var99;
         double var103 = 0.5;
         double var105 = var101 * var103;
         double var107 = var39;
         double var109 = var29;
         double var111 = var107 - var109;
         double var113 = 0.5;
         double var115 = var111 * var113;
         double var117 = var18;
         double var119 = var115 * var117;
         double var121 = var45;
         double var123 = var31;
         double var125 = var121 - var123;
         double var127 = 0.5;
         double var129 = var125 * var127;
         double var131 = var18;
         double var133 = var129 * var131;
         double var135 = var51;
         double var137 = var33;
         double var139 = var135 - var137;
         double var141 = 0.5;
         double var143 = var139 * var141;
         double var145 = var18;
         double var147 = var143 * var145;
         double var149 = var85;
         double var151 = var119;
         double var153 = var149 - var151;
         double var155 = var95;
         double var157 = var133;
         double var159 = var155 - var157;
         double var161 = var105;
         double var163 = var147;
         double var165 = var161 - var163;
         double var167 = var85;
         double var169 = var119;
         double var171 = var167 + var169;
         double var173 = var95;
         double var175 = var133;
         double var177 = var173 + var175;
         double var179 = var105;
         double var181 = var147;
         double var183 = var179 + var181;
         AABB var185 = new AABB(var153, var159, var165, var171, var177, var183);
         Color var186 = this.fillColor.getColor();
         Color var187 = this.outlineColor.getColor();
         boolean var188;
         if (!this.renderMode.getValue().equalsIgnoreCase("Fill") && !this.renderMode.getValue().equalsIgnoreCase("Both")) {
            var188 = false;
         } else {
            var188 = true;
         }

         boolean var189 = var188;
         boolean var190;
         if (!this.renderMode.getValue().equalsIgnoreCase("Outline") && !this.renderMode.getValue().equalsIgnoreCase("Both")) {
            var190 = false;
         } else {
            var190 = true;
         }

         boolean var191 = var190;
         if (var189) {
            PoseStack var192 = var28;
            AABB var193 = var185;
            Color var194 = var186;
            Render3DUtil.a_void_PoseStack_AABB_Color(var192, var193, var194);
         }

         if (var191) {
            PoseStack var195 = var28;
            AABB var196 = var185;
            Color var197 = var187;
            Render3DUtil.b(var195, var196, var197);
         }
      }
   }

   private void E() {
      if (!this.ad) {
         this.ad = true;
         this.ae = 0;
         this.af = null;
         this.ag = false;
      }
   }

   private void F() {
      int var1 = this.ae + 1;
      this.ae = var1;
      if (this.ae > 200) {
         this.ad = false;
      } else if (!(mc.player.containerMenu instanceof CraftingMenu)) {
         if (!this.autoPlace.getValue()) {
            this.ad = false;
         } else if (this.af == null) {
            BlockPos var2 = this.I();
            this.af = var2;
            if (this.af == null) {
               this.ad = false;
            } else {
               this.ag = false;
               this.ae = 0;
            }
         } else {
            ClientLevel var3 = mc.level;
            BlockPos var4 = this.af;
            boolean var5;
            if (var3.getBlockState(var4).getBlock() == Blocks.CRAFTING_TABLE) {
               var5 = true;
            } else {
               var5 = false;
            }

            if (var5 && !this.ag) {
               if (this.rotate.getValue()) {
                  float[] var6 = RotationMathUtil.a(Vec3.atCenterOf(this.af));
                  float var7 = var6[0];
                  float var8 = var6[1];
                  Night.rotationManager.sendOrBatchPosRot(var7, var8);
               }

               MultiPlayerGameMode var9 = mc.gameMode;
               LocalPlayer var10 = mc.player;
               Vec3 var11 = Vec3.atCenterOf(this.af);
               BlockPos var12 = this.af;
               var9.useItemOn(var10, InteractionHand.MAIN_HAND, new BlockHitResult(var11, Direction.UP, var12, false));
               this.ag = true;
               this.ae = 0;
            } else {
               if (this.ae > 40) {
                  this.ad = false;
                  this.af = null;
                  this.ag = false;
               }
            }
         }
      } else {
         this.af = null;
         this.ag = false;
         AbstractContainerMenu var13 = mc.player.containerMenu;
         if (!this.a_bool_AbstractContainerMenu(var13)) {
            AbstractContainerMenu var14 = var13;
            if (!this.b_bool_AbstractContainerMenu(var14)) {
               if (this.autoClose.getValue()) {
                  mc.player.closeContainer();
               }

               this.ad = false;
               return;
            }
         }

         MultiPlayerGameMode var15 = mc.gameMode;
         int var16 = var13.containerId;
         LocalPlayer var17 = mc.player;
         var15.handleContainerInput(var16, 0, 0, ContainerInput.QUICK_MOVE, var17);
         if (this.H()) {
            if (this.autoClose.getValue()) {
               mc.player.closeContainer();
            }

            this.ad = false;
         }
      }
   }

   private int G() {
      int var1 = 0;

      for (int var2 = 0; var2 < 36; var2++) {
         Inventory var3 = mc.player.getInventory();
         int var4 = var2;
         ItemStack var5 = var3.getItem(var4);
         if (a_bool_Item(var5.getItem())) {
            int var6 = var1;
            int var7 = var5.getCount();
            var1 = var6 + var7;
         }
      }

      return var1;
   }

   private boolean H() {
      for (int var1 = 0; var1 < 36; var1++) {
         Inventory var2 = mc.player.getInventory();
         int var3 = var1;
         if (var2.getItem(var3).isEmpty()) {
            return false;
         }
      }

      return true;
   }

   private boolean a_bool_AbstractContainerMenu(AbstractContainerMenu var1) {
      int[] var2 = aj;
      int var3 = var2.length;
      int var4 = 0;

      while (true) {
         int var5 = var4;
         int var6 = var3;
         if (var5 >= var6) {
            int[] var11 = ak;
            int var12 = var11.length;
            int var13 = 0;

            while (true) {
               int var14 = var13;
               int var15 = var12;
               if (var14 >= var15) {
                  return true;
               }

               int[] var16 = var11;
               int var17 = var13;
               int var18 = var16[var17];
               ItemStack var19 = var1.getSlot(var18).getItem();
               if (!this.b_bool_ItemStack(var19)) {
                  return false;
               }

               var13++;
            }
         }

         int[] var7 = var2;
         int var8 = var4;
         int var9 = var7[var8];
         ItemStack var10 = var1.getSlot(var9).getItem();
         if (!this.a_bool_ItemStack(var10)) {
            return false;
         }

         var4++;
      }
   }

   private boolean a_bool_ItemStack(ItemStack var1) {
      boolean var2;
      if (!var1.isEmpty() && var1.is(ItemTags.WOOL)) {
         var2 = true;
      } else {
         var2 = false;
      }

      return var2;
   }

   private boolean b_bool_ItemStack(ItemStack var1) {
      boolean var2;
      if (!var1.isEmpty() && var1.is(ItemTags.PLANKS)) {
         var2 = true;
      } else {
         var2 = false;
      }

      return var2;
   }

   private boolean b_bool_AbstractContainerMenu(AbstractContainerMenu var1) {
      int var2 = this.a_int_AbstractContainerMenu_Predicate(var1, this::a_bool_ItemStack);
      int var3 = this.a_int_AbstractContainerMenu_Predicate(var1, this::b_bool_ItemStack);
      if (var2 != -1 && var3 != -1) {
         int[] var4 = aj;
         int var5 = var4.length;
         int var6 = 0;

         while (true) {
            int var7 = var6;
            int var8 = var5;
            if (var7 >= var8) {
               int[] var16 = ak;
               int var17 = var16.length;
               int var18 = 0;

               while (true) {
                  int var19 = var18;
                  int var20 = var17;
                  if (var19 >= var20) {
                     return true;
                  }

                  int[] var21 = var16;
                  int var22 = var18;
                  int var23 = var21[var22];
                  ItemStack var24 = var1.getSlot(var23).getItem();
                  if (!this.b_bool_ItemStack(var24)) {
                     int var25 = this.a_int_AbstractContainerMenu_Predicate(var1, this::b_bool_ItemStack);
                     if (var25 == -1) {
                        return false;
                     }

                     int var26 = var25;
                     int var27 = var23;
                     this.a_void_AbstractContainerMenu_int_int(var1, var26, var27);
                  }

                  var18++;
               }
            }

            int[] var9 = var4;
            int var10 = var6;
            int var11 = var9[var10];
            ItemStack var12 = var1.getSlot(var11).getItem();
            if (!this.a_bool_ItemStack(var12)) {
               int var13 = this.a_int_AbstractContainerMenu_Predicate(var1, this::a_bool_ItemStack);
               if (var13 == -1) {
                  return false;
               }

               int var14 = var13;
               int var15 = var11;
               this.a_void_AbstractContainerMenu_int_int(var1, var14, var15);
            }

            var6++;
         }
      } else {
         return false;
      }
   }

   private void a_void_AbstractContainerMenu_int_int(AbstractContainerMenu var1, int var2, int var3) {
      MultiPlayerGameMode var4 = mc.gameMode;
      int var5 = var1.containerId;
      LocalPlayer var6 = mc.player;
      var4.handleContainerInput(var5, var2, 1, ContainerInput.PICKUP, var6);
      MultiPlayerGameMode var7 = mc.gameMode;
      int var8 = var1.containerId;
      LocalPlayer var9 = mc.player;
      var7.handleContainerInput(var8, var3, 1, ContainerInput.PICKUP, var9);
      MultiPlayerGameMode var10 = mc.gameMode;
      int var11 = var1.containerId;
      LocalPlayer var12 = mc.player;
      var10.handleContainerInput(var11, var2, 0, ContainerInput.PICKUP, var12);
   }

   private int a_int_AbstractContainerMenu_Predicate(AbstractContainerMenu var1, Predicate<ItemStack> var2) {
      int var3 = 10;

      while (true) {
         int var4 = var3;
         int var5 = var1.slots.size();
         if (var4 >= var5) {
            return -1;
         }

         int var6 = var3;
         ItemStack var7 = var1.getSlot(var6).getItem();
         if (var2.test(var7)) {
            return var3;
         }

         var3++;
      }
   }

   private BlockPos I() {
      int var1 = -1;

      for (int var2 = 0; var2 < 9; var2++) {
         Inventory var3 = mc.player.getInventory();
         int var4 = var2;
         Item var5 = var3.getItem(var4).getItem();
         Item var6 = Blocks.CRAFTING_TABLE.asItem();
         if (var5 == var6) {
            var1 = var2;
            break;
         }
      }

      if (var1 == -1) {
         return null;
      }

      BlockPos var7 = mc.player.blockPosition();
      BlockPos var8 = null;
      double var9 = Double.MAX_VALUE;
      int var11 = (int)Math.ceil(this.range.getValue().doubleValue());
      int var12 = -var11;

      while (true) {
         int var13 = var12;
         int var14 = var11;
         if (var13 > var14) {
            if (var8 == null) {
               return null;
            }

            BlockPos var64 = var8.below();
            double var65 = var8.getX();
            double var67 = 0.5;
            double var69 = var65 + var67;
            double var71 = var8.getY();
            double var73 = var8.getZ();
            double var75 = 0.5;
            double var77 = var73 + var75;
            BlockPos var79 = var64;
            BlockHitResult var80 = new BlockHitResult(new Vec3(var69, var71, var77), Direction.UP, var79, false);
            int var81 = mc.player.getInventory().getSelectedSlot();
            int var82 = var1;
            int var83 = var81;
            InventoryUtil.a_bool_String_int_int("Normal", var82, var83);
            MultiPlayerGameMode var84 = mc.gameMode;
            LocalPlayer var85 = mc.player;
            BlockHitResult var86 = var80;
            var84.useItemOn(var85, InteractionHand.MAIN_HAND, var86);
            int var87 = var1;
            int var88 = var81;
            InventoryUtil.b("Normal", var87, var88);
            return var8;
         }

         int var15 = -var11;

         while (true) {
            int var16 = var15;
            int var17 = var11;
            if (var16 > var17) {
               var12++;
               break;
            }

            int var18 = -var11;

            while (true) {
               int var19 = var18;
               int var20 = var11;
               if (var19 > var20) {
                  var15++;
                  break;
               }

               BlockPos var21 = var7;
               int var22 = var12;
               int var23 = var15;
               int var24 = var18;
               BlockPos var25 = var21.offset(var22, var23, var24);
               LocalPlayer var26 = mc.player;
               double var27 = var25.getX();
               double var29 = 0.5;
               double var31 = var27 + var29;
               double var33 = var25.getY();
               double var35 = 0.5;
               double var37 = var33 + var35;
               double var39 = var25.getZ();
               double var41 = 0.5;
               double var43 = var39 + var41;
               double var45 = var26.distanceToSqr(var31, var37, var43);
               double var47 = this.range.getValue().doubleValue();
               double var49 = this.range.getValue().doubleValue();
               double var51 = var47 * var49;
               if (!(var45 > var51)) {
                  ClientLevel var53 = mc.level;
                  BlockPos var54 = var25;
                  if (var53.getBlockState(var54).canBeReplaced()) {
                     ClientLevel var55 = mc.level;
                     BlockPos var56 = var25.below();
                     if (!var55.getBlockState(var56).canBeReplaced()) {
                        BlockPos var58 = var25;
                        AABB var57 = new AABB(var58);
                        AABB var59 = mc.player.getBoundingBox();
                        if (!var57.intersects(var59)) {
                           double var60 = var45;
                           double var62 = var9;
                           if (var60 < var62) {
                              var9 = var45;
                              var8 = var25;
                           }
                        }
                     }
                  }
               }

               var18++;
            }
         }
      }
   }

   @EventTarget
   public void a_void_ClientDisconnectEvent(ClientDisconnectEvent var1) {
      this.D();
      this.ad = false;
   }

   @EventTarget
   public void a_void_ClientConnectEvent(ClientConnectEvent var1) {
      this.D();
      this.ad = false;
   }

   private static boolean a_bool_Item(Item var0) {
      boolean var1;
      if (var0 instanceof BlockItem && ((BlockItem)var0).getBlock() instanceof BedBlock) {
         var1 = true;
      } else {
         var1 = false;
      }

      return var1;
   }

   static {
      int[] var0 = new int[]{1, 2, 3};
      aj = var0;
      int[] var1 = new int[]{4, 5, 6};
      ak = var1;
   }

   public record BedPlacement(BlockPos pos, Direction direction, float value) {
   }

   record BedTarget(BlockPos pos, Player player, float value) {
   }

   public record DirectionalBedTarget(BlockPos pos, Direction direction, Player player, float value) {
   }
}
