package night.module.impl.combat;

import com.mojang.blaze3d.vertex.PoseStack;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.PosRot;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket.Action;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.component.Weapon;
import net.minecraft.world.item.component.ItemAttributeModifiers.Entry;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import night.Night;
import night.events.EventTarget;
import night.events.impl.ClientDisconnectEvent;
import night.events.impl.MotionSyncEvent;
import night.events.impl.PacketReceiveEvent;
import night.events.impl.Render3DEvent;
import night.events.impl.TotemPopEvent;
import night.events.impl.UpdateWalkingPlayerEvent;
import night.mixins.accessors.ClientPlayerEntityAccessor;
import night.module.Module;
import night.module.ModuleInfo;
import night.module.access.SprintAccess;
import night.setting.impl.BooleanSetting;
import night.setting.impl.CategorySetting;
import night.setting.impl.ColorSetting;
import night.setting.impl.ModeSetting;
import night.setting.impl.NumberSetting;
import night.util.color.ColorUtil;
import night.util.inventory.EnchantmentUtil;
import night.util.inventory.InventoryUtil;
import night.util.network.PacketPredictionUtil;
import night.util.render.Render3DUtil;
import night.util.rotation.RotationMathUtil;
import night.util.world.EntityUtil;
import night.util.world.ExtrapolationUtil;
import night.util.world.PacketAndRenderUtil;

@ModuleInfo(
   name = "KillAura",
   description = "Automatically attacks optimal targets using Nami's combat engine.",
   category = Module.Category.COMBAT,
   proxySupported = true
)
public class KillAura extends Module {
   public NumberSetting range;
   public BooleanSetting stanceAbuse;
   public NumberSetting delay;
   public ModeSetting swap;
   public BooleanSetting onlyGapHold;
   public BooleanSetting onlyTotemHold;
   public BooleanSetting pauseOnEat;
   public ModeSetting tPS;
   public BooleanSetting multitask;
   public ModeSetting sprinting;
   public ModeSetting rotate;
   public BooleanSetting magnet;
   public BooleanSetting raytrace;
   public BooleanSetting swing;
   public BooleanSetting render;
   public ModeSetting renderMode;
   public CategorySetting entitiesCategory;
   public BooleanSetting players;
   public BooleanSetting friends;
   public BooleanSetting x;
   public BooleanSetting y;
   public BooleanSetting z;
   public BooleanSetting animals;
   public BooleanSetting projectiles;
   public ColorSetting targetColor;
   public Entity D;
   private Entity E;
   private long F;
   private static final long G = 300L;
   private long H;
   private float I;
   private float J;
   private Runnable K;
   private int L;
   private int M;
   private int N;

   public KillAura() {
      Double var1 = 3.0;
      Double var2 = 1.0;
      Double var3 = 6.0;
      this.range = new NumberSetting("Range", "The reach distance to entities.", var1, var2, var3);
      this.stanceAbuse = new BooleanSetting("StanceAbuse", "Abuses Grim stance eye positions to optimize reach.", false);
      Double var4 = 0.92;
      Double var5 = 0.0;
      Double var6 = 1.0;
      this.delay = new NumberSetting("Delay", "Attack cooldown delay multiplier.", var4, var5, var6);
      String[] var7 = new String[]{"None", "Require", "Normal", "Silent"};
      this.swap = new ModeSetting("Swap", "Weapon switch mode.", "Require", var7);
      ModeSetting var8 = this.swap;
      String[] var9 = new String[]{"Silent"};
      this.onlyGapHold = new BooleanSetting(
         "OnlyGapHold", "Only attacks using silent swap when holding a golden apple in main hand.", new ModeSetting.ModeCondition(var8, var9), false
      );
      ModeSetting var10 = this.swap;
      String[] var11 = new String[]{"Silent"};
      this.onlyTotemHold = new BooleanSetting(
         "OnlyTotemHold", "Only attacks using silent swap when holding a totem in main hand.", new ModeSetting.ModeCondition(var10, var11), false
      );
      this.pauseOnEat = new BooleanSetting("PauseOnEat", "Pauses attacking while eating or using items.", false);
      String[] var12 = new String[]{"None", "Average"};
      this.tPS = new ModeSetting("TPS", "TPS synchronization mode for attack cooldown.", "Average", var12);
      this.multitask = new BooleanSetting("Multitask", "Allows attacking while eating or using items.", true);
      String[] var13 = new String[]{"None", "Packet"};
      this.sprinting = new ModeSetting("Sprinting", "Sprint management during attacks.", "None", var13);
      String[] var14 = new String[]{"None", "Normal", "Hold", "Silent"};
      this.rotate = new ModeSetting("Rotate", "Rotation mode.", "Normal", var14);
      ModeSetting var15 = this.rotate;
      String[] var16 = new String[]{"Normal", "Hold", "Silent"};
      this.magnet = new BooleanSetting(
         "Magnet",
         "Hits facing 180 degrees away so sprint knockback pulls the target toward you. Needs sprinting (Sprinting=None); GrimAC hitbox checks may flag.",
         new ModeSetting.ModeCondition(var15, var16),
         false
      );
      this.raytrace = new BooleanSetting("Raytrace", "Only targets entities you can see (no hitting through walls).", false);
      this.swing = new BooleanSetting("Swing", "Whether to swing your hand when attacking.", true);
      this.render = new BooleanSetting("Render", "Renders an indicator around the current target.", true);
      BooleanSetting var18 = this.render;
      BooleanSetting.BooleanCondition var17 = new BooleanSetting.BooleanCondition(var18, true);
      String[] var19 = new String[]{"Box", "Circle", "Both"};
      this.renderMode = new ModeSetting("RenderMode", "Target render mode.", var17, "Circle", var19);
      this.entitiesCategory = new CategorySetting("Entities", "Target entity filter settings.");
      CategorySetting var20 = this.entitiesCategory;
      this.players = new BooleanSetting("Players", "Target player entities.", new CategorySetting.CategoryCondition(var20), true);
      CategorySetting var21 = this.entitiesCategory;
      this.friends = new BooleanSetting("Friends", "Target friends.", new CategorySetting.CategoryCondition(var21), false);
      CategorySetting var22 = this.entitiesCategory;
      this.x = new BooleanSetting("IgnoreNaked", "Ignore Naked, even that player wears elytra only", new CategorySetting.CategoryCondition(var22), false);
      CategorySetting var23 = this.entitiesCategory;
      this.y = new BooleanSetting(
         "Hostiles", "Target hostile mobs (Zombies, Skeletons, Spiders, Piglins, etc.).", new CategorySetting.CategoryCondition(var23), false
      );
      CategorySetting var24 = this.entitiesCategory;
      this.z = new BooleanSetting("Neutrals", "Target neutral mobs (Endermen, Zombie Piglins, etc.).", new CategorySetting.CategoryCondition(var24), false);
      CategorySetting var25 = this.entitiesCategory;
      this.animals = new BooleanSetting("Animals", "Target passive animal mobs.", new CategorySetting.CategoryCondition(var25), false);
      CategorySetting var26 = this.entitiesCategory;
      this.projectiles = new BooleanSetting("Projectiles", "Target shulker bullets & fireballs.", new CategorySetting.CategoryCondition(var26), true);
      BooleanSetting var28 = this.render;
      BooleanSetting.BooleanCondition var27 = new BooleanSetting.BooleanCondition(var28, true);
      ColorSetting.ColorValue var29 = ColorUtil.b();
      this.targetColor = new ColorSetting("TargetColor", "Color of the target render box.", var27, var29);
      this.D = null;
      this.E = null;
      long var30 = 0L;
      this.F = var30;
      long var32 = 0L;
      this.H = var32;
      this.L = -1;
      this.M = -1;
      this.N = -1;
   }

   public Entity v() {
      return this.D;
   }

   private void x() {
      if (this.M != -1) {
         InventoryUtil.c_void_int(this.M);
         if (mc.player != null) {
            int var1 = this.M;
            int var2 = this.N;
            InventoryUtil.b("Silent", var1, var2);
         }

         this.M = -1;
         this.N = -1;
      }
   }

   private boolean y() {
      Mainhand var1 = Night.moduleManager.getModule(Mainhand.class);
      boolean var2;
      if (var1 != null && var1.x()) {
         var2 = true;
      } else {
         var2 = false;
      }

      return var2;
   }

   @EventTarget
   public void a_void_TotemPopEvent(TotemPopEvent var1) {
      Player var2 = var1.getPlayer();
      LocalPlayer var3 = mc.player;
      if (var2 == var3) {
         mc.execute(this::x);
      }
   }

   @EventTarget
   public void a_void_ClientDisconnectEvent(ClientDisconnectEvent var1) {
      if (this.M != -1) {
         InventoryUtil.c_void_int(this.M);
      }

      this.M = -1;
      this.N = -1;
      this.L = -1;
      this.D = null;
      this.E = null;
   }

   @Override
   public void onEnable() {
      this.D = null;
      this.E = null;
      this.L = -1;
      long var1 = 0L;
      this.H = var1;
      this.M = -1;
      this.N = -1;
   }

   @Override
   public void onDisable() {
      if (this.L != -1 && mc.player != null) {
         int var1 = this.L;
         int var2 = mc.player.getInventory().getSelectedSlot();
         InventoryUtil.a_bool_String_int_int("Normal", var1, var2);
         this.L = -1;
      }

      this.x();
      this.D = null;
      this.E = null;
      this.K = null;
   }

   @EventTarget
   public void a_void_UpdateWalkingPlayerEvent(UpdateWalkingPlayerEvent var1) {
      if (this.isProxyDelegated()) {
         this.x();
      } else if (mc.player != null && mc.level != null && !mc.player.isDeadOrDying()) {
         if (this.y()) {
            this.x();
         }

         AutoCrystal var2 = Night.moduleManager.getModule(AutoCrystal.class);
         if (var2 != null && var2.isEnabled() && var2.w() != null) {
            this.x();
         } else {
            boolean var3;
            if (!mc.options.keyUse.isDown() && !mc.player.isUsingItem() && mc.player.getUseItemRemainingTicks() <= 0) {
               var3 = false;
            } else {
               var3 = true;
            }

            boolean var4 = var3;
            if (this.pauseOnEat.getValue() && var4) {
               this.D = null;
               this.x();
            } else if (!this.multitask.getValue() && var4) {
               this.D = null;
               this.x();
            } else {
               ItemStack var5 = mc.player.getMainHandItem();
               if (this.swap.getValue().equalsIgnoreCase("Silent")) {
                  boolean var6 = this.onlyGapHold.getValue();
                  boolean var7 = this.onlyTotemHold.getValue();
                  if (var6 || var7) {
                     boolean var8;
                     if (!var5.is(Items.GOLDEN_APPLE) && !var5.is(Items.ENCHANTED_GOLDEN_APPLE)) {
                        var8 = false;
                     } else {
                        var8 = true;
                     }

                     boolean var9 = var8;
                     boolean var10 = var5.is(Items.TOTEM_OF_UNDYING);
                     boolean var11;
                     if ((!var6 || !var9) && (!var7 || !var10)) {
                        var11 = false;
                     } else {
                        var11 = true;
                     }

                     if (!var11) {
                        this.D = null;
                        this.x();
                        return;
                     }
                  }
               }

               Entity var12 = this.B();
               if (var12 != null && (!this.swap.getValue().equalsIgnoreCase("Require") || a_bool_ItemStack(var5))) {
                  Entity var13 = var12;
                  this.D = var13;
                  boolean var14 = false;
                  if (!(this.D instanceof ShulkerBullet) && !(this.D instanceof LargeFireball)) {
                     float var15 = this.D();
                     Entity var16 = this.D;
                     if (var16 instanceof LivingEntity) {
                        float var17 = ((LivingEntity)var16).getMaxHealth();
                        float var18 = var15;
                        if (var17 <= var18) {
                           var14 = true;
                        }
                     }
                  } else {
                     var14 = true;
                  }

                  int var19;
                  double var20;
                  long var22;
                  boolean var32;
                  label190: {
                     var19 = this.w();
                     var20 = this.c_double_int(var19);
                     var22 = System.currentTimeMillis();
                     if (!var14) {
                        long var24 = var22;
                        long var26 = this.H;
                        double var28 = var24 - var26;
                        double var30 = var20;
                        if (!(var28 >= var30)) {
                           var32 = false;
                           break label190;
                        }
                     }

                     var32 = true;
                  }

                  boolean var33 = var32;

                  label270: {
                     double var38 = switch (this.rotate.getValue()) {
                        case "Hold" -> 1000.0;
                        case "Normal" -> 100.0;
                        default -> 0.0;
                     };
                     if (!var14 && !var33) {
                        double var40 = var20;
                        long var42 = var22;
                        long var44 = this.H;
                        double var46 = var42 - var44;
                        double var48 = var40 - var46;
                        double var50 = var38;
                        if (!(var48 <= var50)) {
                           break label270;
                        }
                     }

                     Vec3 var52 = mc.player.getEyePosition(1.0F);
                     if (this.stanceAbuse.getValue()) {
                        double var53 = Double.MAX_VALUE;
                        Vec3[] var55 = ExtrapolationUtil.b(mc.player);
                        int var56 = var55.length;
                        int var57 = 0;

                        while (true) {
                           int var58 = var57;
                           int var59 = var56;
                           if (var58 >= var59) {
                              break;
                           }

                           Vec3[] var60 = var55;
                           int var61 = var57;
                           Vec3 var62 = var60[var61];
                           AABB var63 = this.D.getBoundingBox();
                           Vec3 var64 = RotationMathUtil.a_Vec3_Vec3_AABB(var62, var63);
                           Vec3 var65 = var62;
                           Vec3 var66 = var64;
                           double var67 = var65.distanceTo(var66);
                           double var69 = var53;
                           if (var67 < var69) {
                              var53 = var67;
                              var52 = var62;
                           }

                           var57++;
                        }
                     }

                     Vec3 var71 = this.D.getBoundingBox().getCenter();
                     if (this.D instanceof LivingEntity var73) {
                        double var74 = this.D.getBoundingBox().getYsize();
                        double var76 = 0.8;
                        if (var74 >= var76) {
                           double var78 = this.D.getX();
                           double var80 = this.D.getY();
                           double var82 = var73.getEyeHeight();
                           double var84 = 0.75;
                           double var86 = var82 * var84;
                           double var88 = var80 + var86;
                           double var90 = this.D.getZ();
                           var71 = new Vec3(var78, var88, var90);
                        }
                     }

                     if (this.raytrace.getValue()) {
                        Vec3 var92 = var52;
                        Entity var93 = this.D;
                        Vec3 var94 = var71;
                        Vec3 var95 = this.a_Vec3_Vec3_Entity_Vec3(var92, var93, var94);
                        if (var95 == null) {
                           this.D = null;
                           this.x();
                           return;
                        }

                        var71 = var95;
                     }

                     Vec3 var96 = var52;
                     Vec3 var97 = var71;
                     float var98 = RotationMathUtil.a_float_Vec3_Vec3(var96, var97);
                     Vec3 var99 = var52;
                     Vec3 var100 = var71;
                     float var101 = RotationMathUtil.b_float_Vec3_Vec3(var99, var100);
                     AABB var102 = this.D.getBoundingBox();
                     Vec3 var103 = var52;
                     boolean var104 = var102.contains(var103);
                     Vec3 var105 = var52;
                     Entity var106 = this.D;
                     double var107 = this.a_double_Vec3_Entity(var105, var106);
                     double var109 = this.range.getValue().doubleValue();
                     if (var107 > var109) {
                        this.D = null;
                        this.x();
                        return;
                     }

                     boolean var111;
                     if (!this.rotate.getValue().equalsIgnoreCase("None") && !var104) {
                        var111 = false;
                     } else {
                        var111 = true;
                     }

                     boolean var112 = var111;
                     float var113 = var98;
                     this.I = var113;
                     float var114 = var101;
                     this.J = var114;
                     if (!this.rotate.getValue().equalsIgnoreCase("None")) {
                        if (this.A()) {
                           var112 = true;
                        } else if (this.z()) {
                           boolean var121;
                           label151: {
                              if (!var104) {
                                 Vec3 var115 = var52;
                                 Entity var116 = this.D;
                                 double var117 = this.range.getValue().doubleValue();
                                 float var119 = Night.rotationManager.getServerYaw();
                                 float var120 = Night.rotationManager.getServerPitch();
                                 if (RotationMathUtil.a(var115, var116, var117, var119, var120) == null) {
                                    var121 = false;
                                    break label151;
                                 }
                              }

                              var121 = true;
                           }

                           var112 = var121;
                        } else if (this.rotate.getValue().equalsIgnoreCase("Silent")) {
                           float var122 = var98;
                           float var123 = var101;
                           Night.rotationManager.sendOrBatchPosRot(var122, var123);
                           var112 = true;
                        } else {
                           float var124 = var98;
                           float var125 = var101;
                           int var126 = Night.rotationManager.getModulePriority(this);
                           Night.rotationManager.rotate(var124, var125, this, var126);
                           Vec3 var127 = var52;
                           Entity var128 = this.D;
                           double var129 = this.range.getValue().doubleValue();
                           float var131 = Night.rotationManager.getServerYaw();
                           float var132 = Night.rotationManager.getServerPitch();
                           boolean var133;
                           if (RotationMathUtil.a(var127, var128, var129, var131, var132) == null && !var104) {
                              var133 = false;
                           } else {
                              var133 = true;
                           }

                           var112 = var133;
                        }
                     }

                     if (!var112) {
                        return;
                     }
                  }

                  if (var33) {
                     if (this.A()) {
                        float var134;
                        if (this.magnet.getValue()) {
                           var134 = Mth.wrapDegrees(this.I + 180.0F);
                        } else {
                           var134 = this.I;
                        }

                        float var136 = var134;
                        float var137 = this.J;
                        float var138 = var136;
                        float var139 = var137;
                        int var140 = var19;
                        this.K = () -> {
                           this.a_void_float_float(var138, var139);
                           this.b_void_int(var140);
                        };
                     } else {
                        int var135 = var19;
                        this.b_void_int(var135);
                     }
                  }
               } else {
                  this.D = null;
                  this.x();
               }
            }
         }
      } else {
         this.x();
      }
   }

   private boolean z() {
      SprintAccess var1 = (SprintAccess)Night.moduleManager.getModuleByName("Sprint");
      boolean var2;
      if (var1 != null && var1.isGrimMode()) {
         var2 = true;
      } else {
         var2 = false;
      }

      return var2;
   }

   private boolean A() {
      if (this.rotate.getValue().equalsIgnoreCase("None")) {
         return false;
      }

      boolean var1;
      if (!this.magnet.getValue() && (!this.z() || !PacketPredictionUtil.c())) {
         var1 = false;
      } else {
         var1 = true;
      }

      return var1;
   }

   @EventTarget
   public void a_void_Post(MotionSyncEvent.Post var1) {
      Runnable var2 = this.K;
      this.K = null;
      if (var2 != null && this.D != null && mc.player != null && mc.getConnection() != null) {
         var2.run();
      }
   }

   private void a_void_float_float(float var1, float var2) {
      ClientPacketListener var3 = mc.getConnection();
      double var4 = Night.positionManager.getX();
      double var6 = Night.positionManager.getY();
      double var8 = Night.positionManager.getZ();
      boolean var10 = Night.positionManager.isOnGround();
      boolean var11 = mc.player.horizontalCollision;
      var3.send(new PosRot(var4, var6, var8, var1, var2, var10, var11));
      ClientPlayerEntityAccessor var12 = (ClientPlayerEntityAccessor)mc.player;
      var12.setLastYaw(var1);
      var12.setLastPitch(var2);
   }

   private void b_void_int(int var1) {
      boolean var2 = false;
      if (this.sprinting.getValue().equalsIgnoreCase("Packet") && mc.player.isSprinting() && !mc.player.isShiftKeyDown()) {
         ClientPacketListener var3 = mc.getConnection();
         LocalPlayer var4 = mc.player;
         var3.send(new ServerboundPlayerCommandPacket(var4, Action.STOP_SPRINTING));
         var2 = true;
      }

      int var5 = mc.player.getInventory().getSelectedSlot();
      boolean var6 = false;
      if (this.swap.getValue().equalsIgnoreCase("Normal") && var1 != -1) {
         if (this.L == -1 && var5 != var1) {
            int var7 = var5;
            this.L = var7;
         }

         int var8 = var5;
         InventoryUtil.a_bool_String_int_int("Normal", var1, var8);
      } else if (this.swap.getValue().equalsIgnoreCase("Silent") && var1 != -1 && this.y()) {
         int var9 = var5;
         InventoryUtil.a_bool_String_int_int("Silent", var1, var9);
         var6 = true;
      } else if (this.swap.getValue().equalsIgnoreCase("Silent") && var1 != -1 && this.M != var1) {
         this.x();
         int var10 = var5;
         InventoryUtil.a_bool_String_int_int("Silent", var1, var10);
         InventoryUtil.b_void_int(var1);
         this.M = var1;
         int var11 = var5;
         this.N = var11;
      }

      boolean var12;
      if (this.magnet.getValue() && !var2 && mc.player.isSprinting() && mc.player.getAttackStrengthScale(0.5F) > 0.9F) {
         var12 = true;
      } else {
         var12 = false;
      }

      boolean var13 = var12;
      ClientPacketListener var14 = mc.getConnection();
      int var15 = this.D.getId();
      var14.send(new ServerboundAttackPacket(var15));
      if (var13) {
         mc.player.setSprinting(false);
      }

      mc.player.resetAttackStrengthTicker();
      if (this.swing.getValue()) {
         PacketAndRenderUtil.a(mc.player, InteractionHand.MAIN_HAND);
         ClientPacketListener var16 = mc.getConnection();
         Packet var17 = PacketAndRenderUtil.a_Packet_InteractionHand(InteractionHand.MAIN_HAND);
         var16.send(var17);
      }

      if (var6) {
         int var18 = var5;
         InventoryUtil.b("Silent", var1, var18);
      }

      if (var2) {
         ClientPacketListener var19 = mc.getConnection();
         LocalPlayer var20 = mc.player;
         var19.send(new ServerboundPlayerCommandPacket(var20, Action.START_SPRINTING));
      }

      long var21 = System.currentTimeMillis();
      this.H = var21;
   }

   @EventTarget
   public void a_void_PacketReceiveEvent(PacketReceiveEvent var1) {
      if (var1.getPacket() instanceof ServerboundSetCarriedItemPacket) {
         if (mc.player != null && mc.level != null) {
            mc.execute(() -> {
               long var1x = System.currentTimeMillis();
               this.H = var1x;
            });
         }
      }
   }

   @EventTarget
   public void a_void_Render3DEvent(Render3DEvent var1) {
      if (this.render.getValue() && mc.player != null && !mc.player.isDeadOrDying()) {
         Entity var2 = this.D;
         float var3 = 1.0F;
         if (var2 != null) {
            Entity var4 = var2;
            this.E = var4;
            long var5 = System.currentTimeMillis();
            this.F = var5;
         } else if (this.E != null) {
            long var7 = System.currentTimeMillis();
            long var9 = this.F;
            long var11 = var7 - var9;
            long var13 = 300L;
            if (var11 < var13) {
               var2 = this.E;
               float var15 = (float)var11 / 300.0F;
               var3 = 1.0F - var15;
            } else {
               this.E = null;
            }
         }

         if (var2 != null) {
            Color var16 = this.targetColor.getColor();
            float var17 = this.targetColor.getColor().getAlpha();
            float var18 = var3;
            int var19 = (int)(var17 * var18);
            Color var20 = ColorUtil.a(var16, var19);
            if (this.renderMode.getValue().equalsIgnoreCase("Box") || this.renderMode.getValue().equalsIgnoreCase("Both")) {
               Entity var21 = var2;
               float var22 = var1.getTickDelta();
               Vec3 var23 = EntityUtil.a_Vec3_Entity_float(var21, var22);
               AABB var24 = var2.getBoundingBox();
               double var25 = var23.x;
               double var27 = var2.getX();
               double var29 = var25 - var27;
               double var31 = var23.y;
               double var33 = var2.getY();
               double var35 = var31 - var33;
               double var37 = var23.z;
               double var39 = var2.getZ();
               double var41 = var37 - var39;
               AABB var43 = var24.move(var29, var35, var41);
               PoseStack var44 = var1.getMatrices();
               AABB var45 = var43;
               Color var46 = var20;
               Render3DUtil.a_void_PoseStack_AABB_Color(var44, var45, var46);
               PoseStack var47 = var1.getMatrices();
               AABB var48 = var43;
               Color var49 = var20;
               Render3DUtil.b(var47, var48, var49);
            }

            if (this.renderMode.getValue().equalsIgnoreCase("Circle") || this.renderMode.getValue().equalsIgnoreCase("Both")) {
               PoseStack var50 = var1.getMatrices();
               Entity var51 = var2;
               float var52 = var1.getTickDelta();
               Color var53 = var20;
               Render3DUtil.a_void_PoseStack_Entity_float_Color(var50, var51, var52, var53);
            }
         }
      }
   }

   public double a_double_Vec3_Entity(Vec3 var1, Entity var2) {
      if (mc.player != null && var2 != null) {
         if (this.stanceAbuse.getValue()) {
            double var3 = Double.MAX_VALUE;
            Vec3[] var5 = ExtrapolationUtil.b(mc.player);
            int var6 = var5.length;
            int var7 = 0;

            while (true) {
               int var8 = var7;
               int var9 = var6;
               if (var8 >= var9) {
                  return var3;
               }

               Vec3[] var10 = var5;
               int var11 = var7;
               Vec3 var12 = var10[var11];
               AABB var13 = var2.getBoundingBox();
               Vec3 var14 = RotationMathUtil.a_Vec3_Vec3_AABB(var12, var13);
               double var15 = var12.distanceTo(var14);
               double var17 = var3;
               if (var15 < var17) {
                  var3 = var15;
               }

               var7++;
            }
         } else {
            AABB var19 = var2.getBoundingBox();
            Vec3 var20 = RotationMathUtil.a_Vec3_Vec3_AABB(var1, var19);
            return var1.distanceTo(var20);
         }
      } else {
         return Double.MAX_VALUE;
      }
   }

   private Vec3 a_Vec3_Vec3_Entity_Vec3(Vec3 var1, Entity var2, Vec3 var3) {
      if (this.a_bool_Vec3_Vec3(var1, var3)) {
         return var3;
      }

      AABB var4 = var2.getBoundingBox();
      double var5 = 0.05;
      AABB var7 = var4.deflate(var5);
      Vec3 var8 = null;

      for (int var9 = 0; var9 <= 2; var9++) {
         for (int var10 = 0; var10 <= 2; var10++) {
            for (int var11 = 0; var11 <= 2; var11++) {
               double var12 = var9;
               double var14 = 2.0;
               double var16 = var12 / var14;
               double var18 = var7.minX;
               double var20 = var7.maxX;
               double var22 = Mth.lerp(var16, var18, var20);
               double var24 = var10;
               double var26 = 2.0;
               double var28 = var24 / var26;
               double var30 = var7.minY;
               double var32 = var7.maxY;
               double var34 = Mth.lerp(var28, var30, var32);
               double var36 = var11;
               double var38 = 2.0;
               double var40 = var36 / var38;
               double var42 = var7.minZ;
               double var44 = var7.maxZ;
               double var46 = Mth.lerp(var40, var42, var44);
               Vec3 var48 = new Vec3(var22, var34, var46);
               if (var8 != null) {
                  double var49 = var48.distanceToSqr(var3);
                  double var51 = var8.distanceToSqr(var3);
                  if (!(var49 < var51)) {
                     continue;
                  }
               }

               Vec3 var53 = var48;
               if (this.a_bool_Vec3_Vec3(var1, var53)) {
                  var8 = var48;
               }
            }
         }
      }

      return var8;
   }

   private boolean a_bool_Vec3_Vec3(Vec3 var1, Vec3 var2) {
      ClientLevel var3 = mc.level;
      LocalPlayer var4 = mc.player;
      boolean var5;
      if (var3.clip(new ClipContext(var1, var2, Block.COLLIDER, Fluid.NONE, var4)).getType() == Type.MISS) {
         var5 = true;
      } else {
         var5 = false;
      }

      return var5;
   }

   private Entity B() {
      if (mc.player != null && mc.level != null) {
         ArrayList<Entity> var1 = new ArrayList<>();
         Vec3 var2 = mc.player.getEyePosition(1.0F);
         double var3 = this.range.getValue().doubleValue();

         for (Entity var6 : (Iterable<Entity>)(Object)mc.level.entitiesForRendering()) {
            LocalPlayer var7 = mc.player;
            if (var6 != var7 && var6.isAlive() && !EntityUtil.c_bool_Entity(var6)) {
               Vec3 var8 = var2;
               Entity var9 = var6;
               double var10 = this.a_double_Vec3_Entity(var8, var9);
               double var12 = var3;
               if (!(var10 > var12)) {
                  if (this.raytrace.getValue()) {
                     Vec3 var14 = var2;
                     Entity var15 = var6;
                     Vec3 var16 = var6.getBoundingBox().getCenter();
                     if (this.a_Vec3_Vec3_Entity_Vec3(var14, var15, var16) == null) {
                        continue;
                     }
                  }

                  if (var6 instanceof Player var17) {
                     if (this.players.getValue()) {
                        if (!this.friends.getValue()) {
                           String var18 = var17.getName().getString();
                           if (Night.friendManager.isFriend(var18)) {
                              continue;
                           }
                        }

                        if (!this.x.getValue() || !EntityUtil.b_bool_Player(var17)) {
                           ArrayList var19 = var1;
                           Entity var20 = var6;
                           var19.add(var20);
                        }
                     }
                  } else if (!this.projectiles.getValue() || !(var6 instanceof ShulkerBullet) && !(var6 instanceof LargeFireball)) {
                     if (EntityUtil.e(var6)) {
                        if (this.z.getValue() || EntityUtil.f(var6)) {
                           ArrayList var23 = var1;
                           Entity var24 = var6;
                           var23.add(var24);
                        }
                     } else if (this.y.getValue() && EntityUtil.g(var6)) {
                        ArrayList var25 = var1;
                        Entity var26 = var6;
                        var25.add(var26);
                     } else if (this.animals.getValue() && EntityUtil.h(var6)) {
                        ArrayList var27 = var1;
                        Entity var28 = var6;
                        var27.add(var28);
                     }
                  } else {
                     ArrayList var21 = var1;
                     Entity var22 = var6;
                     var21.add(var22);
                  }
               }
            }
         }

         Vec3 var29 = var2;
         List var30 = var1.stream()
            .filter(var0 -> var0 instanceof Player)
            .sorted(Comparator.comparingDouble(var2x -> this.a_double_Vec3_Entity(var29, var2x)))
            .toList();
         if (!var30.isEmpty()) {
            return (Entity)var30.get(0);
         }

         Vec3 var31 = var2;
         Vec3 var32 = var2;
         List var33 = var1.stream().filter(var2x -> {
            if (var2x instanceof Creeper) {
               double var3x = this.a_double_Vec3_Entity(var31, var2x);
               double var5 = 3.0;
               if (var3x <= var5) {
                  return true;
               }
            }

            return false;
         }).sorted(Comparator.comparingDouble(var2x -> this.a_double_Vec3_Entity(var32, var2x))).toList();
         if (!var33.isEmpty()) {
            return (Entity)var33.get(0);
         }

         Vec3 var34 = var2;
         List var35 = var1.stream().filter(var0 -> {
            boolean var1x;
            if (!(var0 instanceof ShulkerBullet) && !(var0 instanceof LargeFireball)) {
               var1x = false;
            } else {
               var1x = true;
            }

            return var1x;
         }).sorted(Comparator.comparingDouble(var2x -> this.a_double_Vec3_Entity(var34, var2x))).toList();
         if (!var35.isEmpty()) {
            return (Entity)var35.get(0);
         }

         Vec3 var36 = var2;
         return var1.stream().min(Comparator.comparingDouble(var2x -> this.a_double_Vec3_Entity(var36, var2x))).orElse(null);
      } else {
         return null;
      }
   }

   private float C() {
      return this.tPS.getValue().equalsIgnoreCase("None") ? 20.0F : Night.serverManager.getTps();
   }

   private double c_double_int(int var1) {
      if (mc.player == null) {
         return 500.0;
      }

      int var2;
      if (var1 == -1) {
         var2 = mc.player.getInventory().getSelectedSlot();
      } else {
         var2 = var1;
      }

      int var3 = var2;
      Inventory var4 = mc.player.getInventory();
      int var5 = var3;
      ItemStack var6 = var4.getItem(var5);
      double var7 = mc.player.getAttributeBaseValue(Attributes.ATTACK_SPEED);
      if (var6.has(DataComponents.ATTRIBUTE_MODIFIERS)) {
         ItemAttributeModifiers var9 = (ItemAttributeModifiers)var6.get(DataComponents.ATTRIBUTE_MODIFIERS);
         if (var9 != null) {
            for (Entry var11 : var9.modifiers()) {
               if (var11.attribute().is(Attributes.ATTACK_SPEED)) {
                  double var12 = var7;
                  double var14 = var11.modifier().amount();
                  var7 = var12 + var14;
               }
            }
         }
      }

      double var16 = 1.0;
      double var18 = 0.1;
      double var20 = var7;
      double var22 = Math.max(var18, var20);
      double var24 = var16 / var22;
      double var26 = 20.0;
      double var28 = var24 * var26;
      double var30 = 50.0;
      double var32 = var28 * var30;
      double var34 = this.delay.getValue().doubleValue();
      double var36 = var32 * var34;
      if (this.tPS.getValue().equalsIgnoreCase("Average")) {
         float var38 = Night.serverManager.getTps();
         float var39 = Math.max(1.0F, var38);
         double var40 = var36;
         float var42 = var39;
         double var43 = 20.0F / var42;
         var36 = var40 * var43;
      }

      return var36;
   }

   public int w() {
      if (mc.player == null) {
         return -1;
      }

      int var1 = -1;
      float var2 = -1.0F;
      boolean var3;
      if (!mc.player.getAbilities().flying && !mc.player.onGround()) {
         var3 = true;
      } else {
         var3 = false;
      }

      boolean var4 = var3;

      for (int var5 = 0; var5 < 9; var5++) {
         Inventory var6 = mc.player.getInventory();
         int var7 = var5;
         ItemStack var8 = var6.getItem(var7);
         if (!var8.isEmpty()) {
            boolean var9 = b_bool_ItemStack(var8);
            boolean var10 = c_bool_ItemStack(var8);
            boolean var11 = var8.getItem() instanceof TridentItem;
            boolean var12 = var8.getItem() instanceof MaceItem;
            if (var9 || var10 || var11 || var12) {
               if (var12 && var4) {
                  return var5;
               }

               float var13 = 0.0F;
               if (var8.has(DataComponents.ATTRIBUTE_MODIFIERS)) {
                  ItemAttributeModifiers var14 = (ItemAttributeModifiers)var8.get(DataComponents.ATTRIBUTE_MODIFIERS);
                  if (var14 != null) {
                     for (Entry var16 : var14.modifiers()) {
                        if (var16.attribute().is(Attributes.ATTACK_DAMAGE)) {
                           float var17 = var13;
                           float var18 = (float)var16.modifier().amount();
                           var13 = var17 + var18;
                        }
                     }
                  }
               }

               if (var9) {
                  var13 += 5.0F;
               }

               int var19 = EnchantmentUtil.a(var8, Enchantments.SHARPNESS);
               int var20 = EnchantmentUtil.a(var8, Enchantments.SMITE);
               int var21 = EnchantmentUtil.a(var8, Enchantments.BANE_OF_ARTHROPODS);
               float var22 = var13;
               float var23 = var19 * 1.25F;
               float var24 = var20 * 2.5F;
               float var25 = var23 + var24;
               float var26 = var21 * 2.5F;
               float var27 = var25 + var26;
               float var28 = var22 + var27;
               float var29 = var2;
               if (var28 > var29) {
                  var2 = var28;
                  var1 = var5;
               }
            }
         }
      }

      return var1;
   }

   private float D() {
      if (mc.player == null) {
         return 1.0F;
      }

      float var1 = 1.0F;
      if (mc.player.hasEffect(MobEffects.STRENGTH)) {
         MobEffectInstance var2 = mc.player.getEffect(MobEffects.STRENGTH);
         if (var2 != null) {
            float var3 = var1;
            float var4 = var2.getAmplifier() + 1;
            float var5 = 3.0F * var4;
            var1 = var3 + var5;
         }
      }

      if (mc.player.hasEffect(MobEffects.WEAKNESS)) {
         MobEffectInstance var6 = mc.player.getEffect(MobEffects.WEAKNESS);
         if (var6 != null) {
            float var7 = var1;
            float var8 = var6.getAmplifier() + 1;
            float var9 = 4.0F * var8;
            var1 = var7 - var9;
         }
      }

      return Math.max(var1, 0.0F);
   }

   public static boolean a_bool_ItemStack(ItemStack var0) {
      if (var0 != null && !var0.isEmpty()) {
         boolean var1;
         if (!b_bool_ItemStack(var0) && !c_bool_ItemStack(var0) && !(var0.getItem() instanceof TridentItem) && !(var0.getItem() instanceof MaceItem)) {
            var1 = false;
         } else {
            var1 = true;
         }

         return var1;
      } else {
         return false;
      }
   }

   public static boolean b_bool_ItemStack(ItemStack var0) {
      if (var0.is(ItemTags.SWORDS)) {
         return true;
      }

      Tool var1 = (Tool)var0.get(DataComponents.TOOL);
      boolean var2;
      if (var1 != null && !var1.canDestroyBlocksInCreative() && !(var0.getItem() instanceof MaceItem)) {
         var2 = true;
      } else {
         var2 = false;
      }

      return var2;
   }

   public static boolean c_bool_ItemStack(ItemStack var0) {
      if (var0.is(ItemTags.AXES)) {
         return true;
      }

      Weapon var1 = (Weapon)var0.get(DataComponents.WEAPON);
      boolean var2;
      if (var1 != null && var1.disableBlockingForSeconds() > 0.0F) {
         var2 = true;
      } else {
         var2 = false;
      }

      return var2;
   }

   @Override
   public String getDisplayInfo() {
      String var1;
      if (this.D == null) {
         var1 = "None";
      } else {
         var1 = this.D.getName().getString();
      }

      return var1;
   }
}
