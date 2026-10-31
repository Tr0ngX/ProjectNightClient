package night.module.impl.combat;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.Pos;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.PosRot;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import night.Night;
import night.events.EventTarget;
import night.events.impl.RotationUpdateEvent;
import night.events.impl.UpdateWalkingPlayerEvent;
import night.manager.ChatManager;
import night.module.Module;
import night.module.ModuleInfo;
import night.pingbypass.network.RubberbandTracker;
import night.setting.impl.BooleanSetting;
import night.setting.impl.ModeSetting;
import night.util.inventory.InventoryUtil;
import night.util.rotation.RotationMathUtil;
import night.util.world.BlockInteractionUtil;
import night.util.world.PositionUtil;

@ModuleInfo(
   name = "SelfFill",
   description = "Automatically places a block in the spot that you were previously in.",
   category = Module.Category.COMBAT,
   proxySupported = true
)
public class SelfFill extends Module {
   public ModeSetting switchSetting = new ModeSetting(
      "Switch", "The mode that will be used for automatically switching to necessary items.", "Silent", InventoryUtil.a
   );
   public ModeSetting jumpMode;
   public ModeSetting burrow;
   public BooleanSetting obsidianOnly;
   public BooleanSetting rotate;
   public BooleanSetting strictDirection;
   public BooleanSetting crystalDestruction;
   public BooleanSetting render;
   private BlockPos m;
   private boolean n;
   private boolean o;
   private int p;

   public SelfFill() {
      String[] var1 = new String[]{"Normal", "Packet"};
      this.jumpMode = new ModeSetting("JumpMode", "The mode that will be used for jumping.", "Normal", var1);
      ModeSetting var3 = this.jumpMode;
      String[] var4 = new String[]{"Packet"};
      ModeSetting.ModeCondition var2 = new ModeSetting.ModeCondition(var3, var4);
      String[] var5 = new String[]{"None", "Bypass"};
      this.burrow = new ModeSetting("Burrow", "Teleports you inside of the block that was placed.", var2, "None", var5);
      this.obsidianOnly = new BooleanSetting("ObsidianOnly", "Only places using obsidian.", false);
      this.rotate = new BooleanSetting("Rotate", "Whether or not to rotate when placing the block.", true);
      this.strictDirection = new BooleanSetting("StrictDirection", "Only places using directions that face you.", false);
      this.crystalDestruction = new BooleanSetting("CrystalDestruction", "Whether or not to destroy crystals that obstruct the block's placement.", true);
      this.render = new BooleanSetting("Render", "Whether or not to render the place position.", true);
      this.m = null;
      this.n = false;
      this.o = false;
   }

   @EventTarget
   public void a_void_UpdateWalkingPlayerEvent(UpdateWalkingPlayerEvent var1) {
      if (!this.isProxyDelegated()) {
         if (this.m == null) {
            this.setEnabled(false);
         } else {
            if (this.jumpMode.getValue().equalsIgnoreCase("Normal")) {
               if (!this.n) {
                  mc.player.jumpFromGround();
                  this.n = true;
                  this.p = 0;
                  return;
               }

               int var2 = this.p;
               int var3 = var2 + 1;
               this.p = var3;
               if (var2 < 3) {
                  return;
               }
            }

            BlockPos var4 = this.m;
            boolean var5 = this.strictDirection.getValue();
            Direction var6 = BlockInteractionUtil.b_Direction_BlockPos_bool(var4, var5);
            if (var6 == null) {
               this.setEnabled(false);
            } else {
               if (this.jumpMode.getValue().equalsIgnoreCase("Packet") && this.burrow.getValue().equalsIgnoreCase("Bypass")) {
                  if (!this.o) {
                     this.o = true;
                     this.p = 0;
                     return;
                  }

                  int var7 = this.p;
                  int var8 = var7 + 1;
                  this.p = var8;
                  if (var7 < 3) {
                     return;
                  }
               }

               if (!this.switchSetting.getValue().equalsIgnoreCase("None")
                  || mc.player.getMainHandItem().getItem() instanceof BlockItem
                     && (!this.obsidianOnly.getValue() || mc.player.getMainHandItem().getItem() == Items.OBSIDIAN)) {
                  ClientLevel var12 = mc.level;
                  BlockPos var13 = this.m;
                  if (var12.getBlockState(var13).canBeReplaced()) {
                     ClientLevel var14 = mc.level;
                     Entity var15 = (Entity)null;
                     BlockPos var16 = this.m;
                     if (var14.getEntities(var15, new AABB(var16), var0 -> {
                        LocalPlayer var1x = mc.player;
                        boolean var2x;
                        if (var0 != var1x && !(var0 instanceof ExperienceOrb) && !(var0 instanceof ItemEntity) && !(var0 instanceof EndCrystal)) {
                           var2x = true;
                        } else {
                           var2x = false;
                        }

                        return var2x;
                     }).isEmpty()) {
                        byte var17;
                        if (!this.switchSetting.getValue().equalsIgnoreCase("AltSwap") && !this.switchSetting.getValue().equalsIgnoreCase("AltPickup")) {
                           var17 = 8;
                        } else {
                           var17 = 35;
                        }

                        int var18 = InventoryUtil.b(0, var17);
                        int var19 = mc.player.getInventory().getSelectedSlot();
                        if (this.obsidianOnly.getValue()) {
                           Item var20 = Items.OBSIDIAN;
                           byte var21;
                           if (!this.switchSetting.getValue().equalsIgnoreCase("AltSwap") && !this.switchSetting.getValue().equalsIgnoreCase("AltPickup")) {
                              var21 = 8;
                           } else {
                              var21 = 35;
                           }

                           var18 = InventoryUtil.a_int_Item_int_int(var20, 0, var21);
                        }

                        if (!this.switchSetting.getValue().equalsIgnoreCase("None") && var18 == -1) {
                           ChatManager var22 = Night.chatManager;
                           String var23;
                           if (this.obsidianOnly.getValue()) {
                              var23 = "valid ";
                           } else {
                              var23 = "";
                           }

                           String var24 = this.getName();
                           var22.sendTaggedMessage("There are currently no " + var23 + "blocks in your hotbar.", var24);
                           this.setEnabled(false);
                        } else {
                           if (this.jumpMode.getValue().equals("Packet")) {
                              RubberbandTracker.a(() -> {
                                 ClientPacketListener var1x = mc.player.connection;
                                 double var2x = mc.player.getX();
                                 double var4x = mc.player.getY();
                                 double var6x = 0.4;
                                 double var8x = var4x + var6x;
                                 double var10x = mc.player.getZ();
                                 boolean var12x = mc.player.horizontalCollision;
                                 var1x.send(new Pos(var2x, var8x, var10x, false, var12x));
                                 ClientPacketListener var13x = mc.player.connection;
                                 double var14x = mc.player.getX();
                                 double var16x = mc.player.getY();
                                 double var18x = 0.75;
                                 double var20x = var16x + var18x;
                                 double var22x = mc.player.getZ();
                                 boolean var24x = mc.player.horizontalCollision;
                                 var13x.send(new Pos(var14x, var20x, var22x, false, var24x));
                                 ClientPacketListener var25x = mc.player.connection;
                                 double var26x = mc.player.getX();
                                 double var28x = mc.player.getY();
                                 double var30x = 1.01;
                                 double var32x = var28x + var30x;
                                 double var34x = mc.player.getZ();
                                 boolean var36x = mc.player.horizontalCollision;
                                 var25x.send(new Pos(var26x, var32x, var34x, false, var36x));
                                 ClientPacketListener var37x = mc.player.connection;
                                 double var38x = mc.player.getX();
                                 double var40 = mc.player.getY();
                                 double var42;
                                 if (this.jumpMode.getValue().equalsIgnoreCase("Packet") && this.burrow.getValue().equalsIgnoreCase("Bypass")) {
                                    var42 = 0.99999992;
                                 } else {
                                    var42 = 1.15;
                                 }

                                 double var44 = var40 + var42;
                                 double var46 = mc.player.getZ();
                                 boolean var48 = mc.player.horizontalCollision;
                                 var37x.send(new Pos(var38x, var44, var46, false, var48));
                              });
                           }

                           String var25 = this.switchSetting.getValue();
                           int var26 = var18;
                           int var27 = var19;
                           InventoryUtil.a_bool_String_int_int(var25, var26, var27);
                           BlockPos var28 = this.m;
                           Direction var29 = var6;
                           InteractionHand var30 = InteractionHand.MAIN_HAND;
                           boolean var31;
                           if ((!this.jumpMode.getValue().equalsIgnoreCase("Packet") || !this.burrow.getValue().equalsIgnoreCase("Bypass"))
                              && this.rotate.getValue()) {
                              var31 = true;
                           } else {
                              var31 = false;
                           }

                           boolean var32 = this.crystalDestruction.getValue();
                           boolean var33 = this.render.getValue();
                           boolean var34 = BlockInteractionUtil.a(var28, var29, var30, var31, var32, var33);
                           String var35 = this.switchSetting.getValue();
                           int var36 = var18;
                           int var37 = var19;
                           InventoryUtil.b(var35, var36, var37);
                           if (var34) {
                              if (this.jumpMode.getValue().equals("Packet") && this.burrow.getValue().equalsIgnoreCase("Bypass")) {
                                 RubberbandTracker.a(() -> {
                                    ClientPacketListener var0 = mc.player.connection;
                                    double var1x = mc.player.getX();
                                    double var3x = mc.player.getY();
                                    double var5x = 1.15;
                                    double var7x = var3x + var5x;
                                    double var9x = mc.player.getZ();
                                    boolean var11x = mc.player.onGround();
                                    boolean var12x = mc.player.horizontalCollision;
                                    var0.send(new Pos(var1x, var7x, var9x, var11x, var12x));
                                    ClientPacketListener var13x = mc.player.connection;
                                    double var14x = mc.player.getX();
                                    double var16x = mc.player.getY();
                                    double var18x = mc.player.getZ();
                                    float var20x = mc.player.getYRot();
                                    float var21x = mc.player.getXRot();
                                    boolean var22x = mc.player.onGround();
                                    boolean var23x = mc.player.horizontalCollision;
                                    var13x.send(new PosRot(var14x, var16x, var18x, var20x, var21x, var22x, var23x));
                                 });
                                 LocalPlayer var38 = mc.player;
                                 double var39 = mc.player.getX();
                                 double var41 = mc.player.getY();
                                 double var43 = mc.player.getZ();
                                 var38.setPos(var39, var41, var43);
                              }

                              this.setEnabled(false);
                              this.n = false;
                              this.o = false;
                           }
                        }
                     }
                  }
               } else {
                  ChatManager var9 = Night.chatManager;
                  String var10;
                  if (this.obsidianOnly.getValue()) {
                     var10 = "valid ";
                  } else {
                     var10 = "";
                  }

                  String var11 = this.getName();
                  var9.sendTaggedMessage("You are currently not holding any " + var10 + "blocks.", var11);
                  this.setEnabled(false);
               }
            }
         }
      }
   }

   @EventTarget(priority = 4)
   public void a_void_RotationUpdateEvent(RotationUpdateEvent var1) {
      if (this.o && this.m != null && !var1.isCancelled()) {
         BlockPos var2 = this.m;
         boolean var3 = this.strictDirection.getValue();
         Direction var4 = BlockInteractionUtil.b_Direction_BlockPos_bool(var2, var3);
         if (var4 != null) {
            BlockPos var5 = this.m;
            Direction var6 = var4;
            float[] var7 = RotationMathUtil.a(BlockInteractionUtil.b_Vec3_BlockPos_Direction(var5, var6));
            float var8 = var7[0];
            var1.setYaw(var8);
            float var9 = var7[1];
            var1.setPitch(var9);
         }
      }
   }

   @Override
   public void onEnable() {
      if (mc.player == null || mc.level == null) {
         this.setEnabled(false);
      } else if (!mc.player.onGround()) {
         String var1 = this.getName();
         Night.chatManager.sendTaggedMessage("You are currently in the air.", var1);
         this.setEnabled(false);
      } else {
         BlockPos var2 = PositionUtil.a_BlockPos_Entity(mc.player);
         this.m = var2;
      }
   }
}
