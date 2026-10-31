package night.module.impl.kingmc;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.events.EventTarget;
import night.events.impl.TickEvent;
import night.module.Module;
import night.module.ModuleInfo;
import night.module.access.AutoEnchantAccess;
import night.setting.Setting;
import night.setting.impl.BooleanSetting;
import night.setting.impl.ModeSetting;
import night.setting.impl.NumberSetting;
import night.util.rotation.RotationMathUtil;

@ModuleInfo(
   name = "AutoEnchant",
   description = "Automatically enchants pickaxes and armor at anvils and loots books from chests.",
   category = Module.Category.KINGMC
)
public class AutoEnchant extends Module implements AutoEnchantAccess {
   public NumberSetting delay;
   public NumberSetting actionTick;
   public NumberSetting range;
   public BooleanSetting silent;
   public ModeSetting rotate;
   private final Set<BlockPos> j;
   private final Set<BlockPos> k;
   private final Set<BlockPos> l;
   private final Set<BlockPos> m;
   private final Set<BlockPos> n;
   private boolean o;
   private BlockPos p;
   private BlockPos q;
   private int r;
   private int s;
   private AutoEnchant.GearType t;
   private String u;

   public AutoEnchant() {
      Integer var1 = 1;
      Integer var2 = 1;
      Integer var3 = 10;
      this.delay = new NumberSetting("Delay", "Delay (* 0.1s).", var1, var2, var3);
      Integer var4 = 1;
      Integer var5 = 1;
      Integer var6 = 10;
      this.actionTick = new NumberSetting("ActionTick", "Actions per tick.", var4, var5, var6);
      Setting.VisibilityCondition var7 = new Setting.VisibilityCondition();
      Double var8 = 3.5;
      Double var9 = 0.0;
      Double var10 = 4.0;
      Double var11 = 0.1;
      this.range = new NumberSetting("Range", "Range", "Scan range in blocks.", var7, var8, var9, var10, var11);
      this.silent = new BooleanSetting("Silent", "Suppress chest GUI popup.", true);
      String[] var12 = new String[]{"None", "Silent", "Client"};
      this.rotate = new ModeSetting("Rotate", "Rotation mode when opening chest/anvil.", "Silent", var12);
      this.j = new HashSet<>();
      this.k = new HashSet<>();
      this.l = new HashSet<>();
      this.m = new HashSet<>();
      this.n = new HashSet<>();
      this.o = false;
      this.p = null;
      this.q = null;
      this.r = 0;
      this.s = 0;
      this.t = null;
      this.u = "";
   }

   @Override
   public boolean isSuppressingGui() {
      boolean var1;
      if (this.silent.getValue() && this.o) {
         var1 = true;
      } else {
         var1 = false;
      }

      return var1;
   }

   @Override
   public boolean shouldSuppressScreen(Screen var1) {
      if (!this.silent.getValue() || !this.o) {
         return false;
      }

      if (var1 instanceof ContainerScreen var2) {
         if (a_bool_ContainerScreen(var2)) {
            this.o = false;
            return false;
         } else if (b_bool_ContainerScreen(var2)) {
            this.o = false;
            return false;
         } else if (c_bool_ContainerScreen(var2)) {
            this.o = false;
            return false;
         } else {
            return true;
         }
      } else {
         this.o = false;
         return false;
      }
   }

   public static boolean a_bool_ContainerScreen(ContainerScreen var0) {
      Component var1 = var0.getTitle();
      if (var1 != null) {
         String var2 = var1.getString().toLowerCase();
         if (var2.contains("shulker") || var2.contains("h\u1ed9p shulker")) {
            return true;
         }

         ComponentContents var3 = var1.getContents();
         if (var3 instanceof TranslatableContents && ((TranslatableContents)var3).getKey().toLowerCase().contains("shulker")) {
            return true;
         }
      }

      return false;
   }

   public static boolean b_bool_ContainerScreen(ContainerScreen var0) {
      Component var1 = var0.getTitle();
      if (var1 != null) {
         if (var1.getContents() instanceof TranslatableContents var3
            && (var3.getKey().equals("container.enderchest") || var3.getKey().toLowerCase().contains("ender"))) {
            return true;
         }

         String var4 = var1.getString().toLowerCase();
         if (var4.contains("ender") || var4.contains("echest") || var4.contains("e-chest") || var4.contains("r\u01b0\u01a1ng ender")) {
            return true;
         }
      }

      return false;
   }

   public static boolean c_bool_ContainerScreen(ContainerScreen var0) {
      Component var1 = var0.getTitle();
      if (var1 == null) {
         return true;
      }

      ComponentContents var2 = var1.getContents();
      if (var2 instanceof TranslatableContents) {
         String var3 = ((TranslatableContents)var2).getKey();
         if (var3.equals("container.chest") || var3.equals("container.chestDouble") || var3.equals("container.barrel")) {
            return false;
         }
      }

      String var4 = var1.getString().trim().toLowerCase();
      return !var4.equals("chest")
         && !var4.equals("large chest")
         && !var4.equals("barrel")
         && !var4.equals("r\u01b0\u01a1ng")
         && !var4.equals("r\u01b0\u01a1ng l\u1edbn")
         && !var4.equals("th\u00f9ng");
   }

   @Override
   public void onEnable() {
      this.v();
   }

   @Override
   public void onDisable() {
      this.w();
      this.v();
   }

   private void v() {
      this.o = false;
      this.p = null;
      this.q = null;
      this.r = 0;
      this.s = 0;
      this.t = null;
      this.u = "";
      this.j.clear();
      this.k.clear();
      this.l.clear();
      this.m.clear();
      this.n.clear();
   }

   private void w() {
      if (mc.player != null && mc.player.containerMenu != null) {
         AbstractContainerMenu var1 = mc.player.containerMenu;
         InventoryMenu var2 = mc.player.inventoryMenu;
         if (var1 != var2) {
            mc.player.closeContainer();
         }
      }

      this.o = false;
   }

   @EventTarget
   public void a_void_TickEvent(TickEvent var1) {
      if (mc.player != null && mc.level != null) {
         if (mc.player.containerMenu != null && !mc.player.containerMenu.getCarried().isEmpty()) {
            AbstractContainerMenu var2 = mc.player.containerMenu;
            this.a_void_AbstractContainerMenu(var2);
         } else {
            int var3 = this.delay.getValue().intValue() * 2;
            int var4 = this.r;
            int var5 = var3;
            if (var4 < var5) {
               int var6 = this.r + 1;
               this.r = var6;
            } else if (mc.player.containerMenu instanceof ChestMenu var8) {
               if (!(
                  mc.gui.screen() instanceof ContainerScreen var10
                     && (c_bool_ContainerScreen(var10) || b_bool_ContainerScreen(var10) || a_bool_ContainerScreen(var10))
               )) {
                  ChestMenu var11 = var8;
                  this.a_void_ChestMenu(var11);
               }
            } else if (mc.player.containerMenu instanceof AnvilMenu var13) {
               this.a_void_AnvilMenu(var13);
            } else {
               this.o = false;
               if (this.t == null) {
                  for (int var14 = 0; var14 < 36; var14++) {
                     Inventory var15 = mc.player.getInventory();
                     int var16 = var14;
                     ItemStack var17 = var15.getItem(var16);
                     if (this.f(var17)) {
                        ItemStack var18 = var17;
                        if (!this.c_bool_ItemStack(var18)) {
                           ItemStack var19 = var17;
                           AutoEnchant.GearType var20 = this.b_GearType_ItemStack(var19);
                           this.t = var20;
                           break;
                        }
                     }
                  }
               }

               if (this.C()) {
                  BlockPos var21 = this.E();
                  if (var21 != null) {
                     BlockPos var22 = var21;
                     this.q = var22;
                     BlockPos var23 = var21;
                     this.a_void_BlockPos(var23);
                     this.r = 0;
                     return;
                  }
               }

               if (this.z()) {
                  Set var24 = this.m;
                  BlockPos var25 = this.a_BlockPos_Set(var24);
                  if (var25 == null) {
                     this.m.clear();
                     Set var26 = this.m;
                     var25 = this.a_BlockPos_Set(var26);
                  }

                  if (var25 != null) {
                     BlockPos var27 = var25;
                     this.p = var27;
                     if (this.silent.getValue()) {
                        this.o = true;
                     }

                     BlockPos var28 = var25;
                     this.a_void_BlockPos(var28);
                     this.r = 0;
                     return;
                  }
               }

               if (this.A()) {
                  Map var29 = this.B();
                  boolean var30 = false;
                  Iterator var31 = var29.values().iterator();

                  while (var31.hasNext()) {
                     if ((Integer)var31.next() > 0) {
                        var30 = true;
                        break;
                     }
                  }

                  if (var30 && this.y() > 0) {
                     Set var32 = this.k;
                     Set var33 = this.n;
                     BlockPos var34 = this.a_BlockPos_Set_Set(var32, var33);
                     if (var34 == null) {
                        this.n.clear();
                        Set var35 = this.k;
                        Set var36 = this.n;
                        var34 = this.a_BlockPos_Set_Set(var35, var36);
                     }

                     if (var34 != null) {
                        BlockPos var37 = var34;
                        this.p = var37;
                        if (this.silent.getValue()) {
                           this.o = true;
                        }

                        BlockPos var38 = var34;
                        this.a_void_BlockPos(var38);
                        this.r = 0;
                        return;
                     }
                  }
               }

               if (!this.A() && !this.z() && !this.x()) {
                  Set var39 = this.l;
                  Set var40 = this.n;
                  BlockPos var41 = this.a_BlockPos_Set_Set(var39, var40);
                  if (var41 == null) {
                     this.n.clear();
                     Set var42 = this.l;
                     Set var43 = this.n;
                     var41 = this.a_BlockPos_Set_Set(var42, var43);
                  }

                  if (var41 != null) {
                     BlockPos var44 = var41;
                     this.p = var44;
                     if (this.silent.getValue()) {
                        this.o = true;
                     }

                     BlockPos var45 = var41;
                     this.a_void_BlockPos(var45);
                     this.r = 0;
                     return;
                  }
               }
            }
         }
      }
   }

   private void a_void_BlockPos(BlockPos var1) {
      if (mc.gameMode != null && mc.player != null && mc.level != null) {
         Vec3 var2 = mc.player.getEyePosition();
         Vec3 var3 = Vec3.atCenterOf(var1);
         Direction var4 = Direction.UP;
         double var5 = var2.y;
         double var7 = var3.y;
         double var9 = var5 - var7;
         double var11 = var2.x;
         double var13 = var3.x;
         double var15 = var11 - var13;
         double var17 = var2.z;
         double var19 = var3.z;
         double var21 = var17 - var19;
         double var23 = var9;
         double var25 = -0.4;
         if (var23 < var25) {
            var4 = Direction.DOWN;
         } else {
            label37: {
               double var27 = Math.abs(var15);
               double var29 = Math.abs(var21);
               if (var27 > var29) {
                  double var31 = Math.abs(var15);
                  double var33 = 0.4;
                  if (var31 > var33) {
                     double var35 = var15;
                     double var37 = 0.0;
                     Direction var39;
                     if (var35 > var37) {
                        var39 = Direction.EAST;
                     } else {
                        var39 = Direction.WEST;
                     }

                     var4 = var39;
                     break label37;
                  }
               }

               double var40 = Math.abs(var21);
               double var42 = 0.4;
               if (var40 > var42) {
                  double var44 = var21;
                  double var46 = 0.0;
                  Direction var48;
                  if (var44 > var46) {
                     var48 = Direction.SOUTH;
                  } else {
                     var48 = Direction.NORTH;
                  }

                  var4 = var48;
               }
            }
         }

         Vec3 var49 = var3;
         double var50 = var4.getStepX();
         double var52 = 0.45;
         double var54 = var50 * var52;
         double var56 = var4.getStepY();
         double var58 = 0.45;
         double var60 = var56 * var58;
         double var62 = var4.getStepZ();
         double var64 = 0.45;
         double var66 = var62 * var64;
         Vec3 var68 = var49.add(var54, var60, var66);
         float[] var69 = RotationMathUtil.a(var68);
         String var70 = this.rotate.getValue();
         if ("Client".equalsIgnoreCase(var70)) {
            LocalPlayer var71 = mc.player;
            float var72 = var69[0];
            var71.setYRot(var72);
            LocalPlayer var73 = mc.player;
            float var74 = var69[1];
            var73.setXRot(var74);
            float var75 = var69[0];
            float var76 = var69[1];
            Night.rotationManager.sendOrBatchPosRot(var75, var76);
         } else {
            String var77 = var70;
            if ("Silent".equalsIgnoreCase(var77)) {
               float var78 = var69[0];
               float var79 = var69[1];
               Night.rotationManager.sendOrBatchPosRot(var78, var79);
            }
         }

         Vec3 var80 = var68;
         Direction var81 = var4;
         BlockHitResult var82 = new BlockHitResult(var80, var81, var1, false);
         MultiPlayerGameMode var83 = mc.gameMode;
         LocalPlayer var84 = mc.player;
         BlockHitResult var85 = var82;
         var83.useItemOn(var84, InteractionHand.MAIN_HAND, var85);
      }
   }

   private void a_void_AbstractContainerMenu(AbstractContainerMenu var1) {
      if (mc.player != null && mc.gameMode != null && var1 != null && !var1.getCarried().isEmpty()) {
         int var2;
         if (var1 instanceof ChestMenu) {
            var2 = ((ChestMenu)var1).getContainer().getContainerSize();
         } else {
            var2 = 3;
         }

         int var3 = (byte)var2;

         while (true) {
            int var4 = var3;
            int var5 = var1.slots.size();
            if (var4 >= var5) {
               if (var1 instanceof ChestMenu) {
                  int var11 = ((ChestMenu)var1).getContainer().getContainerSize();
                  int var12 = 0;

                  while (true) {
                     int var13 = var12;
                     int var14 = var11;
                     if (var13 >= var14) {
                        break;
                     }

                     int var15 = var12;
                     if (var1.getSlot(var15).getItem().isEmpty()) {
                        MultiPlayerGameMode var16 = mc.gameMode;
                        int var17 = var1.containerId;
                        int var18 = var12;
                        LocalPlayer var19 = mc.player;
                        var16.handleContainerInput(var17, var18, 0, ContainerInput.PICKUP, var19);
                        return;
                     }

                     var12++;
                  }
               }

               this.w();
               return;
            }

            int var6 = var3;
            if (var1.getSlot(var6).getItem().isEmpty()) {
               MultiPlayerGameMode var7 = mc.gameMode;
               int var8 = var1.containerId;
               int var9 = var3;
               LocalPlayer var10 = mc.player;
               var7.handleContainerInput(var8, var9, 0, ContainerInput.PICKUP, var10);
               return;
            }

            var3++;
         }
      }
   }

   private void a_void_ChestMenu(ChestMenu var1) {
      int var2 = var1.getContainer().getContainerSize();
      if (!var1.getCarried().isEmpty()) {
         AutoEnchant var3 = this;
         ChestMenu var4 = var1;
         var3.a_void_AbstractContainerMenu(var4);
      } else {
         int var5 = 0;
         int var6 = 0;
         int var7 = 0;
         int var8 = 0;
         int var9 = 0;
         int var10 = 0;

         while (true) {
            int var11 = var10;
            int var12 = var2;
            if (var11 >= var12) {
               if (var7 == 0) {
                  AutoEnchant var22 = this;
                  BlockPos var23 = this.p;
                  Set var24 = this.k;
                  var22.a_void_BlockPos_Set(var23, var24);
               }

               if (var8 == 0) {
                  AutoEnchant var25 = this;
                  BlockPos var26 = this.p;
                  Set var27 = this.l;
                  var25.a_void_BlockPos_Set(var26, var27);
               }

               if (var9 == 0) {
                  AutoEnchant var28 = this;
                  BlockPos var29 = this.p;
                  Set var30 = this.m;
                  var28.a_void_BlockPos_Set(var29, var30);
               }

               boolean var33;
               label510: {
                  if (var5 > 0) {
                     int var31 = var5;
                     int var32 = var6;
                     if (var31 == var32 && var7 == 0) {
                        var33 = true;
                        break label510;
                     }
                  }

                  var33 = false;
               }

               boolean var34 = var33;
               if (var34) {
                  if (var9 == 0) {
                     AutoEnchant var35 = this;
                     BlockPos var36 = this.p;
                     Set var37 = this.j;
                     var35.a_void_BlockPos_Set(var36, var37);
                     this.w();
                     AutoEnchant var38 = this;
                     byte var39 = 0;
                     var38.r = var39;
                     return;
                  }

                  if (!this.z()) {
                     this.w();
                     AutoEnchant var40 = this;
                     byte var41 = 0;
                     var40.r = var41;
                     return;
                  }
               }

               if (this.z()) {
                  ArrayList var42 = new ArrayList();
                  ArrayList var43 = var42;
                  int var44 = var2;

                  label500:
                  while (true) {
                     int var45 = var44;
                     int var46 = var1.slots.size();
                     if (var45 >= var46) {
                        if (!var43.isEmpty()) {
                           boolean var60 = false;
                           int var61 = 0;

                           while (true) {
                              int var62 = var61;
                              int var63 = var2;
                              if (var62 >= var63) {
                                 break;
                              }

                              ChestMenu var64 = var1;
                              int var65 = var61;
                              if (var64.getSlot(var65).getItem().isEmpty()) {
                                 var60 = true;
                                 break;
                              }

                              var61++;
                           }

                           if (!var60) {
                              AutoEnchant var66 = this;
                              BlockPos var67 = this.p;
                              Set var68 = this.m;
                              var66.a_void_BlockPos_Set(var67, var68);
                              if (var34) {
                                 AutoEnchant var69 = this;
                                 BlockPos var70 = this.p;
                                 Set var71 = this.j;
                                 var69.a_void_BlockPos_Set(var70, var71);
                              }

                              this.w();
                              AutoEnchant var72 = this;
                              byte var73 = 0;
                              var72.r = var73;
                              return;
                           }

                           byte var74 = 1;
                           int var75 = this.actionTick.getValue().intValue();
                           int var76 = Math.max(var74, var75);
                           int var77 = 0;

                           for (int var79 : (Iterable<Integer>)(Object)var43) {
                              int var80 = var77;
                              int var81 = var76;
                              if (var80 >= var81) {
                                 break;
                              }

                              MultiPlayerGameMode var82 = mc.gameMode;
                              int var83 = var1.containerId;
                              int var84 = var79;
                              byte var85 = 0;
                              ContainerInput var86 = ContainerInput.QUICK_MOVE;
                              LocalPlayer var87 = mc.player;
                              var82.handleContainerInput(var83, var84, var85, var86, var87);
                              var77++;
                           }

                           boolean var88 = false;
                           int var89 = var2;

                           label469: {
                              while (true) {
                                 int var90 = var89;
                                 int var91 = var1.slots.size();
                                 if (var90 >= var91) {
                                    break label469;
                                 }

                                 ChestMenu var92 = var1;
                                 int var93 = var89;
                                 ItemStack var94 = var92.getSlot(var93).getItem();
                                 AutoEnchant var95 = this;
                                 ItemStack var96 = var94;
                                 if (var95.f(var96)) {
                                    AutoEnchant var97 = this;
                                    ItemStack var98 = var94;
                                    if (var97.c_bool_ItemStack(var98)) {
                                       if (this.t == null) {
                                          break;
                                       }

                                       AutoEnchant var99 = this;
                                       ItemStack var100 = var94;
                                       AutoEnchant.GearType var101 = var99.b_GearType_ItemStack(var100);
                                       AutoEnchant.GearType var102 = this.t;
                                       if (var101 == var102) {
                                          break;
                                       }
                                    }
                                 }

                                 var89++;
                              }

                              var88 = true;
                           }

                           if (var88) {
                              return;
                           }

                           int var103 = 0;
                           int var104 = 0;
                           int var105 = 0;
                           int var106 = 0;
                           int var107 = 0;

                           while (true) {
                              int var108 = var107;
                              int var109 = var2;
                              if (var108 >= var109) {
                                 if (var103 > 0) {
                                    int var119 = var103;
                                    int var120 = var104;
                                    if (var119 == var120 && var105 == 0 && var106 == 0) {
                                       AutoEnchant var121 = this;
                                       BlockPos var122 = this.p;
                                       Set var123 = this.j;
                                       var121.a_void_BlockPos_Set(var122, var123);
                                    }
                                 }
                                 break label500;
                              }

                              ChestMenu var110 = var1;
                              int var111 = var107;
                              ItemStack var112 = var110.getSlot(var111).getItem();
                              if (!var112.isEmpty()) {
                                 var103++;
                                 Item var113 = var112.getItem();
                                 Item var114 = Items.ENCHANTED_BOOK;
                                 if (var113 == var114) {
                                    var105++;
                                 } else {
                                    AutoEnchant var115 = this;
                                    ItemStack var116 = var112;
                                    if (var115.f(var116)) {
                                       AutoEnchant var117 = this;
                                       ItemStack var118 = var112;
                                       if (var117.c_bool_ItemStack(var118)) {
                                          var104++;
                                       }
                                    }
                                 }
                              } else {
                                 var106++;
                              }

                              var107++;
                           }
                        }
                        break;
                     }

                     ChestMenu var47 = var1;
                     int var48 = var44;
                     ItemStack var49 = var47.getSlot(var48).getItem();
                     AutoEnchant var50 = this;
                     ItemStack var51 = var49;
                     if (var50.f(var51)) {
                        AutoEnchant var52 = this;
                        ItemStack var53 = var49;
                        label494:
                        if (var52.c_bool_ItemStack(var53)) {
                           if (this.t != null) {
                              AutoEnchant var54 = this;
                              ItemStack var55 = var49;
                              AutoEnchant.GearType var56 = var54.b_GearType_ItemStack(var55);
                              AutoEnchant.GearType var57 = this.t;
                              if (var56 != var57) {
                                 break label494;
                              }
                           }

                           ArrayList var58 = var43;
                           Integer var59 = var44;
                           var58.add(var59);
                        }
                     }

                     var44++;
                  }
               }

               if (this.A()) {
                  AutoEnchant var124 = this;
                  ChestMenu var125 = var1;
                  int var126 = var2;
                  Map var127 = var124.c(var125, var126);
                  boolean var128 = false;
                  Iterator var129 = var127.values().iterator();

                  while (var129.hasNext()) {
                     if ((Integer)var129.next() > 0) {
                        var128 = true;
                        break;
                     }
                  }

                  if (var128) {
                     AutoEnchant var269 = this;
                     ChestMenu var270 = var1;
                     int var271 = var2;
                     if (!var269.a_bool_ChestMenu_int(var270, var271)) {
                        ArrayList var274 = new ArrayList();
                        ArrayList var275 = var274;
                        byte var276 = 0;

                        while (true) {
                           byte var277 = var276;
                           int var278 = var2;
                           if (var277 >= var278) {
                              if (var275.isEmpty()) {
                                 if (var7 == 0) {
                                    AutoEnchant var293 = this;
                                    BlockPos var294 = this.p;
                                    Set var295 = this.k;
                                    var293.a_void_BlockPos_Set(var294, var295);
                                 } else {
                                    AutoEnchant var451 = this;
                                    AutoEnchant var452 = this;
                                    BlockPos var453 = var452.p;
                                    AutoEnchant var454 = this;
                                    Set var455 = var454.n;
                                    var451.a_void_BlockPos_Set(var453, var455);
                                 }

                                 AutoEnchant var456 = this;
                                 var456.w();
                                 AutoEnchant var457 = this;
                                 byte var458 = 0;
                                 var457.r = var458;
                                 return;
                              }

                              ArrayList var296 = var275;
                              AutoEnchant var297 = this;
                              ChestMenu var298 = var1;
                              Map var299 = var127;
                              Comparator<Integer> var300 = (var3x, var4x) -> {
                                 int var5x = var3x;
                                 ItemStack var6x = var298.getSlot(var5x).getItem();
                                 Set var7x = var299.keySet();
                                 int var8x = this.a_int_ItemStack_Set(var6x, var7x);
                                 int var9x = var4x;
                                 ItemStack var10x = var298.getSlot(var9x).getItem();
                                 Set var11x = var299.keySet();
                                 int var12x = this.a_int_ItemStack_Set(var10x, var11x);
                                 int var13x = var8x;
                                 return Integer.compare(var12x, var13x);
                              };
                              var296.sort(var300);
                              byte var301 = 1;
                              int var302 = this.actionTick.getValue().intValue();
                              int var303 = Math.max(var301, var302);
                              byte var304 = 0;

                              for (int var306 : (Iterable<Integer>)(Object)var275) {
                                 byte var307 = var304;
                                 int var308 = var303;
                                 if (var307 >= var308) {
                                    break;
                                 }

                                 AutoEnchant var309 = this;
                                 ChestMenu var310 = var1;
                                 int var311 = var2;
                                 if (var309.a_bool_ChestMenu_int(var310, var311)) {
                                    this.w();
                                    AutoEnchant var312 = this;
                                    byte var313 = 0;
                                    var312.r = var313;
                                    return;
                                 }

                                 ChestMenu var314 = var1;
                                 int var315 = var306;
                                 ItemStack var316 = var314.getSlot(var315).getItem();

                                 for (String var318 : (Iterable<String>)(Object)var127.keySet()) {
                                    AutoEnchant var319 = this;
                                    ItemStack var320 = var316;
                                    String var321 = var318;
                                    if (var319.a_bool_ItemStack_String(var320, var321)) {
                                       Map var322 = var127;
                                       String var323 = var318;
                                       byte var324 = 0;
                                       Map var325 = var127;
                                       String var326 = var318;
                                       int var327 = (Integer)var325.get(var326);
                                       int var328 = var316.getCount();
                                       int var329 = var327 - var328;
                                       Integer var330 = Math.max(var324, var329);
                                       var322.put(var323, var330);
                                    }
                                 }

                                 MultiPlayerGameMode var331 = mc.gameMode;
                                 int var332 = var1.containerId;
                                 int var333 = var306;
                                 byte var334 = 0;
                                 ContainerInput var335 = ContainerInput.QUICK_MOVE;
                                 LocalPlayer var336 = mc.player;
                                 var331.handleContainerInput(var332, var333, var334, var335, var336);
                                 var304 += 1;
                              }

                              if (var304 > 0) {
                                 this.n.clear();
                              }

                              boolean var337 = false;
                              Iterator var338 = var127.values().iterator();

                              while (true) {
                                 Iterator var339 = var338;
                                 boolean var340 = var339.hasNext();
                                 if (!var340) {
                                    break;
                                 }

                                 Iterator var341 = var338;
                                 Object var342 = var341.next();
                                 Integer var343 = (Integer)var342;
                                 int var344 = var343;
                                 int var345 = var344;
                                 int var346 = var345;
                                 if (var346 > 0) {
                                    boolean var347 = true;
                                    var337 = var347;
                                    break;
                                 }
                              }

                              boolean var348 = var337;
                              if (var348) {
                                 AutoEnchant var349 = this;
                                 ChestMenu var350 = var1;
                                 int var351 = var2;
                                 boolean var352 = var349.a_bool_ChestMenu_int(var350, var351);
                                 if (!var352) {
                                    boolean var356 = false;
                                    boolean var357 = var356;
                                    boolean var358 = false;
                                    boolean var359 = var358;
                                    byte var360 = 0;
                                    byte var361 = var360;

                                    while (true) {
                                       byte var362 = var361;
                                       int var363 = var2;
                                       if (var362 >= var363) {
                                          break;
                                       }

                                       ChestMenu var364 = var1;
                                       byte var365 = var361;
                                       Slot var366 = var364.getSlot(var365);
                                       ItemStack var367 = var366.getItem();
                                       ItemStack var368 = var367;
                                       AutoEnchant var369 = this;
                                       ItemStack var370 = var368;
                                       boolean var371 = var369.c_bool_ItemStack(var370);
                                       if (!var371) {
                                          ItemStack var372 = var368;
                                          Item var373 = var372.getItem();
                                          Item var374 = Items.ENCHANTED_BOOK;
                                          if (var373 == var374) {
                                             boolean var375 = true;
                                             var359 = var375;
                                             Map var376 = var127;
                                             Set var377 = var376.entrySet();
                                             Iterator var378 = var377.iterator();
                                             Iterator var379 = var378;

                                             while (true) {
                                                Iterator var380 = var379;
                                                boolean var381 = var380.hasNext();
                                                if (!var381) {
                                                   break;
                                                }

                                                Iterator var382 = var379;
                                                Object var383 = var382.next();
                                                Entry var384 = (Entry)var383;
                                                Entry var385 = var384;
                                                Entry var386 = var385;
                                                Object var387 = var386.getValue();
                                                Integer var388 = (Integer)var387;
                                                int var389 = var388;
                                                if (var389 > 0) {
                                                   AutoEnchant var390 = this;
                                                   ItemStack var391 = var368;
                                                   Entry var392 = var385;
                                                   Object var393 = var392.getKey();
                                                   String var394 = (String)var393;
                                                   boolean var395 = var390.a_bool_ItemStack_String(var391, var394);
                                                   if (var395) {
                                                      boolean var396 = true;
                                                      var357 = var396;
                                                      break;
                                                   }
                                                }
                                             }

                                             boolean var397 = var357;
                                             if (var397) {
                                                break;
                                             }
                                          }
                                       }

                                       var361 += 1;
                                    }

                                    boolean var398 = var357;
                                    if (!var398) {
                                       boolean var399 = var359;
                                       if (!var399) {
                                          AutoEnchant var400 = this;
                                          AutoEnchant var401 = this;
                                          BlockPos var402 = var401.p;
                                          AutoEnchant var403 = this;
                                          Set var404 = var403.k;
                                          var400.a_void_BlockPos_Set(var402, var404);
                                       } else {
                                          AutoEnchant var405 = this;
                                          AutoEnchant var406 = this;
                                          BlockPos var407 = var406.p;
                                          AutoEnchant var408 = this;
                                          Set var409 = var408.n;
                                          var405.a_void_BlockPos_Set(var407, var409);
                                       }

                                       byte var410 = 0;
                                       byte var411 = var410;
                                       byte var412 = 0;
                                       byte var413 = var412;
                                       byte var414 = 0;
                                       byte var415 = var414;
                                       byte var416 = 0;
                                       byte var417 = var416;
                                       byte var418 = 0;
                                       byte var419 = var418;

                                       while (true) {
                                          byte var420 = var419;
                                          int var421 = var2;
                                          if (var420 >= var421) {
                                             byte var438 = var411;
                                             if (var438 > 0) {
                                                byte var439 = var411;
                                                byte var440 = var413;
                                                if (var439 == var440) {
                                                   byte var441 = var415;
                                                   if (var441 == 0) {
                                                      byte var442 = var417;
                                                      if (var442 == 0) {
                                                         AutoEnchant var443 = this;
                                                         AutoEnchant var444 = this;
                                                         BlockPos var445 = var444.p;
                                                         AutoEnchant var446 = this;
                                                         Set var447 = var446.j;
                                                         var443.a_void_BlockPos_Set(var445, var447);
                                                      }
                                                   }
                                                }
                                             }

                                             AutoEnchant var448 = this;
                                             var448.w();
                                             AutoEnchant var449 = this;
                                             byte var450 = 0;
                                             var449.r = var450;
                                             break;
                                          }

                                          ChestMenu var422 = var1;
                                          byte var423 = var419;
                                          Slot var424 = var422.getSlot(var423);
                                          ItemStack var425 = var424.getItem();
                                          ItemStack var426 = var425;
                                          ItemStack var427 = var426;
                                          boolean var428 = var427.isEmpty();
                                          if (!var428) {
                                             var411 += 1;
                                             ItemStack var429 = var426;
                                             Item var430 = var429.getItem();
                                             Item var431 = Items.ENCHANTED_BOOK;
                                             if (var430 == var431) {
                                                var415 += 1;
                                             } else {
                                                AutoEnchant var432 = this;
                                                ItemStack var433 = var426;
                                                boolean var434 = var432.f(var433);
                                                if (var434) {
                                                   AutoEnchant var435 = this;
                                                   ItemStack var436 = var426;
                                                   boolean var437 = var435.c_bool_ItemStack(var436);
                                                   if (var437) {
                                                      var413 += 1;
                                                   }
                                                }
                                             }
                                          } else {
                                             var417 += 1;
                                          }

                                          var419 += 1;
                                       }
                                    }

                                    return;
                                 }
                              }

                              AutoEnchant var353 = this;
                              var353.w();
                              AutoEnchant var354 = this;
                              byte var355 = 0;
                              var354.r = var355;
                              return;
                           }

                           ChestMenu var279 = var1;
                           byte var280 = var276;
                           ItemStack var281 = var279.getSlot(var280).getItem();
                           if (!var281.isEmpty()) {
                              AutoEnchant var282 = this;
                              ItemStack var283 = var281;
                              if (!var282.c_bool_ItemStack(var283)) {
                                 Item var284 = var281.getItem();
                                 Item var285 = Items.ENCHANTED_BOOK;
                                 if (var284 == var285) {
                                    for (Entry var287 : (Iterable<Entry>)(Object)var127.entrySet()) {
                                       if ((Integer)var287.getValue() > 0) {
                                          AutoEnchant var288 = this;
                                          ItemStack var289 = var281;
                                          String var290 = (String)var287.getKey();
                                          if (var288.a_bool_ItemStack_String(var289, var290)) {
                                             ArrayList var291 = var275;
                                             Integer var292 = Integer.valueOf(var276);
                                             var291.add(var292);
                                             break;
                                          }
                                       }
                                    }
                                 }
                              }
                           }

                           var276 += 1;
                        }
                     }
                  }

                  this.w();
                  AutoEnchant var272 = this;
                  byte var273 = 0;
                  var272.r = var273;
                  return;
               } else {
                  label558: {
                     if (!this.A() && !this.z()) {
                        if (this.t == null) {
                           byte var130 = 0;

                           while (true) {
                              byte var131 = var130;
                              int var132 = var2;
                              if (var131 >= var132) {
                                 break;
                              }

                              ChestMenu var133 = var1;
                              byte var134 = var130;
                              ItemStack var135 = var133.getSlot(var134).getItem();
                              AutoEnchant var136 = this;
                              ItemStack var137 = var135;
                              if (!var136.c_bool_ItemStack(var137)) {
                                 AutoEnchant var138 = this;
                                 ItemStack var139 = var135;
                                 if (var138.f(var139)) {
                                    AutoEnchant var140 = this;
                                    AutoEnchant var141 = this;
                                    ItemStack var142 = var135;
                                    AutoEnchant.GearType var143 = var141.b_GearType_ItemStack(var142);
                                    var140.t = var143;
                                    break;
                                 }
                              }

                              var130 += 1;
                           }
                        }

                        if (this.t == null) {
                           AutoEnchant var144 = this;
                           BlockPos var145 = this.p;
                           Set var146 = this.l;
                           var144.a_void_BlockPos_Set(var145, var146);
                           if (var34 && var9 == 0) {
                              AutoEnchant var147 = this;
                              BlockPos var148 = this.p;
                              Set var149 = this.j;
                              var147.a_void_BlockPos_Set(var148, var149);
                           }

                           this.w();
                           AutoEnchant var150 = this;
                           byte var151 = 0;
                           var150.r = var151;
                           return;
                        }

                        AutoEnchant var152 = this;
                        ChestMenu var153 = var1;
                        int var154 = var2;
                        int var155 = var152.b(var153, var154);
                        AutoEnchant var156 = this;
                        ChestMenu var157 = var1;
                        int var158 = var2;
                        AutoEnchant.GearType var159 = this.t;
                        int var160 = var156.a_int_ChestMenu_int_GearType(var157, var158, var159);
                        byte var161 = 1;
                        int var162 = var155;
                        int var163 = var160;
                        int var164 = var162 + var163;
                        byte var165 = 2;
                        int var166 = var164 / var165;
                        int var167 = Math.max(var161, var166);
                        int var168 = var160;
                        int var169 = var167;
                        if (var168 >= var169 || var155 == 0) {
                           this.w();
                           AutoEnchant var170 = this;
                           byte var171 = 0;
                           var170.r = var171;
                           return;
                        }

                        ArrayList var172 = new ArrayList();
                        ArrayList var173 = var172;
                        byte var174 = 0;

                        while (true) {
                           byte var175 = var174;
                           int var176 = var2;
                           if (var175 >= var176) {
                              if (var173.isEmpty()) {
                                 if (var8 == 0) {
                                    AutoEnchant var188 = this;
                                    BlockPos var189 = this.p;
                                    Set var190 = this.l;
                                    var188.a_void_BlockPos_Set(var189, var190);
                                 } else {
                                    AutoEnchant var257 = this;
                                    BlockPos var258 = this.p;
                                    Set var259 = this.n;
                                    var257.a_void_BlockPos_Set(var258, var259);
                                 }

                                 this.w();
                                 AutoEnchant var260 = this;
                                 byte var261 = 0;
                                 var260.r = var261;
                                 return;
                              }

                              byte var191 = 1;
                              int var192 = this.actionTick.getValue().intValue();
                              int var193 = Math.max(var191, var192);
                              byte var194 = 0;

                              for (int var196 : (Iterable<Integer>)(Object)var173) {
                                 byte var197 = var194;
                                 int var198 = var193;
                                 if (var197 < var198) {
                                    int var199 = var160;
                                    int var200 = var167;
                                    if (var199 < var200) {
                                       AutoEnchant var201 = this;
                                       ChestMenu var202 = var1;
                                       int var203 = var2;
                                       if (var201.b(var202, var203) != 0) {
                                          MultiPlayerGameMode var206 = mc.gameMode;
                                          int var207 = var1.containerId;
                                          int var208 = var196;
                                          byte var209 = 0;
                                          ContainerInput var210 = ContainerInput.QUICK_MOVE;
                                          LocalPlayer var211 = mc.player;
                                          var206.handleContainerInput(var207, var208, var209, var210, var211);
                                          var160 += 1;
                                          var194 += 1;
                                          continue;
                                       }
                                    }

                                    this.w();
                                    AutoEnchant var204 = this;
                                    byte var205 = 0;
                                    var204.r = var205;
                                    return;
                                 }
                                 break;
                              }

                              if (var194 > 0) {
                                 this.n.clear();
                              }

                              int var212 = var160;
                              int var213 = var167;
                              if (var212 >= var213) {
                                 break label558;
                              }

                              AutoEnchant var214 = this;
                              ChestMenu var215 = var1;
                              int var216 = var2;
                              if (var214.b(var215, var216) == 0) {
                                 break label558;
                              }

                              boolean var219 = false;
                              boolean var220 = false;
                              byte var221 = 0;

                              while (true) {
                                 byte var222 = var221;
                                 int var223 = var2;
                                 if (var222 >= var223) {
                                    break;
                                 }

                                 ChestMenu var224 = var1;
                                 byte var225 = var221;
                                 ItemStack var226 = var224.getSlot(var225).getItem();
                                 AutoEnchant var227 = this;
                                 ItemStack var228 = var226;
                                 if (!var227.c_bool_ItemStack(var228)) {
                                    AutoEnchant var229 = this;
                                    ItemStack var230 = var226;
                                    if (var229.f(var230)) {
                                       var220 = true;
                                       AutoEnchant var231 = this;
                                       ItemStack var232 = var226;
                                       AutoEnchant.GearType var233 = var231.b_GearType_ItemStack(var232);
                                       AutoEnchant.GearType var234 = this.t;
                                       if (var233 == var234) {
                                          var219 = true;
                                          break;
                                       }
                                    }
                                 }

                                 var221 += 1;
                              }

                              if (!var219) {
                                 if (!var220) {
                                    AutoEnchant var235 = this;
                                    BlockPos var236 = this.p;
                                    Set var237 = this.l;
                                    var235.a_void_BlockPos_Set(var236, var237);
                                 } else {
                                    AutoEnchant var238 = this;
                                    BlockPos var239 = this.p;
                                    Set var240 = this.n;
                                    var238.a_void_BlockPos_Set(var239, var240);
                                 }

                                 byte var241 = 0;
                                 byte var242 = 0;
                                 byte var243 = 0;
                                 byte var244 = 0;
                                 byte var245 = 0;

                                 while (true) {
                                    byte var246 = var245;
                                    int var247 = var2;
                                    if (var246 >= var247) {
                                       if (var241 > 0) {
                                          byte var262 = var241;
                                          byte var263 = var242;
                                          if (var262 == var263 && var243 == 0 && var244 == 0) {
                                             AutoEnchant var264 = this;
                                             BlockPos var265 = this.p;
                                             Set var266 = this.j;
                                             var264.a_void_BlockPos_Set(var265, var266);
                                          }
                                       }

                                       this.w();
                                       AutoEnchant var267 = this;
                                       byte var268 = 0;
                                       var267.r = var268;
                                       return;
                                    }

                                    ChestMenu var248 = var1;
                                    byte var249 = var245;
                                    ItemStack var250 = var248.getSlot(var249).getItem();
                                    if (!var250.isEmpty()) {
                                       var241 += 1;
                                       Item var251 = var250.getItem();
                                       Item var252 = Items.ENCHANTED_BOOK;
                                       if (var251 == var252) {
                                          var243 += 1;
                                       } else {
                                          AutoEnchant var253 = this;
                                          ItemStack var254 = var250;
                                          if (var253.f(var254)) {
                                             AutoEnchant var255 = this;
                                             ItemStack var256 = var250;
                                             if (var255.c_bool_ItemStack(var256)) {
                                                var242 += 1;
                                             }
                                          }
                                       }
                                    } else {
                                       var244 += 1;
                                    }

                                    var245 += 1;
                                 }
                              }

                              return;
                           }

                           ChestMenu var177 = var1;
                           byte var178 = var174;
                           ItemStack var179 = var177.getSlot(var178).getItem();
                           AutoEnchant var180 = this;
                           ItemStack var181 = var179;
                           if (!var180.c_bool_ItemStack(var181)) {
                              AutoEnchant var182 = this;
                              ItemStack var183 = var179;
                              AutoEnchant.GearType var184 = var182.b_GearType_ItemStack(var183);
                              AutoEnchant.GearType var185 = this.t;
                              if (var184 == var185) {
                                 ArrayList var186 = var173;
                                 Integer var187 = Integer.valueOf(var174);
                                 var186.add(var187);
                              }
                           }

                           var174 += 1;
                        }
                     }

                     return;
                  }

                  this.w();
                  AutoEnchant var217 = this;
                  byte var218 = 0;
                  var217.r = var218;
                  return;
               }
            }

            ChestMenu var13 = var1;
            int var14 = var10;
            ItemStack var15 = var13.getSlot(var14).getItem();
            if (var15.isEmpty()) {
               var9++;
            } else {
               var5++;
               Item var16 = var15.getItem();
               Item var17 = Items.ENCHANTED_BOOK;
               if (var16 == var17) {
                  var7++;
               } else {
                  AutoEnchant var18 = this;
                  ItemStack var19 = var15;
                  if (var18.f(var19)) {
                     AutoEnchant var20 = this;
                     ItemStack var21 = var15;
                     if (var20.c_bool_ItemStack(var21)) {
                        var6++;
                     } else {
                        var8++;
                     }
                  }
               }
            }

            var10++;
         }
      }
   }

   private boolean x() {
      if (this.t == null) {
         return false;
      }

      AutoEnchant.GearType var1 = this.t;
      int var2 = this.a_int_GearType(var1);
      if (var2 == 0) {
         return false;
      }

      int var3 = this.y();
      int var4 = var2;
      int var5 = var3;
      int var6 = (var4 + var5) / 2;
      int var7 = Math.max(1, var6);
      int var8 = var2;
      int var9 = var7;
      if (var8 >= var9) {
         return true;
      }

      Set var10 = this.l;
      Set var11 = this.n;
      boolean var12;
      if (this.a_BlockPos_Set_Set(var10, var11) == null) {
         var12 = true;
      } else {
         var12 = false;
      }

      return var12;
   }

   private boolean a_bool_ChestMenu_int(ChestMenu var1, int var2) {
      boolean var3;
      if (this.b(var1, var2) == 0) {
         var3 = true;
      } else {
         var3 = false;
      }

      return var3;
   }

   private int b(ChestMenu var1, int var2) {
      int var3 = 0;
      int var4 = var2;

      while (true) {
         int var5 = var4;
         int var6 = var1.slots.size();
         if (var5 >= var6) {
            return var3;
         }

         int var7 = var4;
         if (var1.getSlot(var7).getItem().isEmpty()) {
            var3++;
         }

         var4++;
      }
   }

   private int y() {
      if (mc.player == null) {
         return 0;
      }

      int var1 = 0;
      Inventory var2 = mc.player.getInventory();

      for (int var3 = 0; var3 < 36; var3++) {
         Inventory var4 = var2;
         int var5 = var3;
         if (var4.getItem(var5).isEmpty()) {
            var1++;
         }
      }

      return var1;
   }

   private int a_int_ChestMenu_int_GearType(ChestMenu var1, int var2, AutoEnchant.GearType var3) {
      if (var3 == null) {
         return 0;
      }

      int var4 = 0;
      int var5 = var2;

      while (true) {
         int var6 = var5;
         int var7 = var1.slots.size();
         if (var6 >= var7) {
            return var4;
         }

         int var8 = var5;
         ItemStack var9 = var1.getSlot(var8).getItem();
         if (this.b_GearType_ItemStack(var9) == var3) {
            var4++;
         }

         var5++;
      }
   }

   private int a_int_GearType(AutoEnchant.GearType var1) {
      if (mc.player != null && var1 != null) {
         int var2 = 0;
         Inventory var3 = mc.player.getInventory();

         for (int var4 = 0; var4 < 36; var4++) {
            Inventory var5 = var3;
            int var6 = var4;
            ItemStack var7 = var5.getItem(var6);
            if (this.b_GearType_ItemStack(var7) == var1) {
               var2++;
            }
         }

         return var2;
      } else {
         return 0;
      }
   }

   private int a_int_ChestMenu_int_String(ChestMenu var1, int var2, String var3) {
      if (var3 == null) {
         return 0;
      }

      int var4 = 0;
      int var5 = var2;

      while (true) {
         int var6 = var5;
         int var7 = var1.slots.size();
         if (var6 >= var7) {
            return var4;
         }

         int var8 = var5;
         ItemStack var9 = var1.getSlot(var8).getItem();
         if (this.a_bool_ItemStack_String(var9, var3)) {
            int var10 = var4;
            int var11 = var9.getCount();
            var4 = var10 + var11;
         }

         var5++;
      }
   }

   private int b_int_String(String var1) {
      if (mc.player != null && var1 != null) {
         int var2 = 0;
         Inventory var3 = mc.player.getInventory();

         for (int var4 = 0; var4 < 36; var4++) {
            Inventory var5 = var3;
            int var6 = var4;
            ItemStack var7 = var5.getItem(var6);
            if (this.a_bool_ItemStack_String(var7, var1)) {
               int var8 = var2;
               int var9 = var7.getCount();
               var2 = var8 + var9;
            }
         }

         return var2;
      } else {
         return 0;
      }
   }

   private int c_int_String(String var1) {
      if (mc.player != null && var1 != null) {
         int var2 = 0;
         Inventory var3 = mc.player.getInventory();

         for (int var4 = 0; var4 < 36; var4++) {
            Inventory var5 = var3;
            int var6 = var4;
            ItemStack var7 = var5.getItem(var6);
            if (this.f(var7)) {
               ItemStack var8 = var7;
               if (!this.c_bool_ItemStack(var8)) {
                  if (this.t != null) {
                     ItemStack var9 = var7;
                     AutoEnchant.GearType var10 = this.b_GearType_ItemStack(var9);
                     AutoEnchant.GearType var11 = this.t;
                     if (var10 != var11) {
                        continue;
                     }
                  }

                  ItemStack var12 = var7;
                  Map var13 = this.h(var12);
                  if (var1.equals("blast_protection")) {
                     Map var14 = var13;
                     Integer var15 = 0;
                     if (((Integer)var14.getOrDefault("blast_protection", var15)) == 0) {
                        Map var16 = var13;
                        Integer var17 = 0;
                        if (((Integer)var16.getOrDefault("protection", var17)) == 0) {
                           Map var18 = var13;
                           Integer var19 = 0;
                           if (((Integer)var18.getOrDefault("fire_protection", var19)) == 0) {
                              Map var20 = var13;
                              Integer var21 = 0;
                              if (((Integer)var20.getOrDefault("projectile_protection", var21)) == 0) {
                                 var2++;
                              }
                           }
                        }
                     }
                  } else {
                     Map var22 = var13;
                     Integer var23 = 0;
                     if (((Integer)var22.getOrDefault(var1, var23)) == 0) {
                        var2++;
                     }
                  }
               }
            }
         }

         return var2;
      } else {
         return 0;
      }
   }

   private boolean z() {
      if (mc.player == null) {
         return false;
      }

      Inventory var1 = mc.player.getInventory();
      int var2 = 0;

      while (true) {
         if (var2 >= 36) {
            return false;
         }

         Inventory var3 = var1;
         int var4 = var2;
         ItemStack var5 = var3.getItem(var4);
         if (this.f(var5)) {
            ItemStack var6 = var5;
            if (this.c_bool_ItemStack(var6)) {
               if (this.t == null) {
                  break;
               }

               ItemStack var7 = var5;
               AutoEnchant.GearType var8 = this.b_GearType_ItemStack(var7);
               AutoEnchant.GearType var9 = this.t;
               if (var8 == var9) {
                  break;
               }
            }
         }

         var2++;
      }

      return true;
   }

   private boolean A() {
      if (mc.player == null) {
         return false;
      }

      Inventory var1 = mc.player.getInventory();
      int var2 = 0;

      while (true) {
         if (var2 >= 36) {
            return false;
         }

         Inventory var3 = var1;
         int var4 = var2;
         ItemStack var5 = var3.getItem(var4);
         if (this.f(var5)) {
            ItemStack var6 = var5;
            if (!this.c_bool_ItemStack(var6)) {
               if (this.t == null) {
                  break;
               }

               ItemStack var7 = var5;
               AutoEnchant.GearType var8 = this.b_GearType_ItemStack(var7);
               AutoEnchant.GearType var9 = this.t;
               if (var8 == var9) {
                  break;
               }
            }
         }

         var2++;
      }

      return true;
   }

   public Set<String> a_Set_ItemStack(ItemStack var1) {
      HashSet var2 = new HashSet();
      AutoEnchant.GearType var3 = this.b_GearType_ItemStack(var1);
      if (var3 == null) {
         return var2;
      }

      Map var4 = this.h(var1);
      if (var3 == AutoEnchant.GearType.PICKAXE) {
         Map var5 = var4;
         Integer var6 = 0;
         if (((Integer)var5.getOrDefault("efficiency", var6)) == 0) {
            var2.add("efficiency");
         }

         Map var7 = var4;
         Integer var8 = 0;
         if (((Integer)var7.getOrDefault("unbreaking", var8)) == 0) {
            var2.add("unbreaking");
         }

         Map var9 = var4;
         Integer var10 = 0;
         if (((Integer)var9.getOrDefault("mending", var10)) == 0) {
            var2.add("mending");
         }
      } else {
         Map var11 = var4;
         Integer var12 = 0;
         if (((Integer)var11.getOrDefault("blast_protection", var12)) == 0) {
            Map var13 = var4;
            Integer var14 = 0;
            if (((Integer)var13.getOrDefault("protection", var14)) == 0) {
               Map var15 = var4;
               Integer var16 = 0;
               if (((Integer)var15.getOrDefault("fire_protection", var16)) == 0) {
                  Map var17 = var4;
                  Integer var18 = 0;
                  if (((Integer)var17.getOrDefault("projectile_protection", var18)) == 0) {
                     var2.add("blast_protection");
                  }
               }
            }
         }

         Map var19 = var4;
         Integer var20 = 0;
         if (((Integer)var19.getOrDefault("unbreaking", var20)) == 0) {
            var2.add("unbreaking");
         }

         Map var21 = var4;
         Integer var22 = 0;
         if (((Integer)var21.getOrDefault("mending", var22)) == 0) {
            var2.add("mending");
         }
      }

      return var2;
   }

   private Map<String, Integer> B() {
      HashMap var1 = new HashMap();
      if (mc.player == null) {
         return var1;
      }

      Inventory var2 = mc.player.getInventory();

      for (int var3 = 0; var3 < 36; var3++) {
         Inventory var4 = var2;
         int var5 = var3;
         ItemStack var6 = var4.getItem(var5);
         if (this.f(var6)) {
            ItemStack var7 = var6;
            if (!this.c_bool_ItemStack(var7)) {
               if (this.t != null) {
                  ItemStack var8 = var6;
                  AutoEnchant.GearType var9 = this.b_GearType_ItemStack(var8);
                  AutoEnchant.GearType var10 = this.t;
                  if (var9 != var10) {
                     continue;
                  }
               }

               ItemStack var11 = var6;

               for (String var13 : (Iterable<String>)(Object)this.a_Set_ItemStack(var11)) {
                  HashMap var14 = var1;
                  String var15 = var13;
                  Integer var16 = 1;
                  ((Map<String, Integer>)var14).merge(var15, var16, Integer::sum);
               }
            }
         }
      }

      for (int var17 = 0; var17 < 36; var17++) {
         Inventory var18 = var2;
         int var19 = var17;
         ItemStack var20 = var18.getItem(var19);
         if (!var20.isEmpty() && var20.getItem() == Items.ENCHANTED_BOOK) {
            for (String var22 : (Iterable<String>)(Object)var1.keySet()) {
               ItemStack var23 = var20;
               String var24 = var22;
               if (this.a_bool_ItemStack_String(var23, var24)) {
                  HashMap var25 = var1;
                  String var26 = var22;
                  int var27 = (Integer)var25.get(var26);
                  int var28 = var20.getCount();
                  int var29 = var27 - var28;
                  HashMap var30 = var1;
                  String var31 = var22;
                  int var32 = var29;
                  Integer var33 = Math.max(0, var32);
                  var30.put(var31, var33);
               }
            }
         }
      }

      return var1;
   }

   private Map<String, Integer> c(ChestMenu var1, int var2) {
      HashMap var3 = new HashMap();
      if (mc.player == null) {
         return var3;
      }

      int var4 = var2;

      while (true) {
         int var5 = var4;
         int var6 = var1.slots.size();
         if (var5 >= var6) {
            int var19 = var2;

            while (true) {
               int var20 = var19;
               int var21 = var1.slots.size();
               if (var20 >= var21) {
                  return var3;
               }

               int var22 = var19;
               ItemStack var23 = var1.getSlot(var22).getItem();
               if (var23.getItem() == Items.ENCHANTED_BOOK) {
                  for (String var25 : (Iterable<String>)(Object)var3.keySet()) {
                     ItemStack var26 = var23;
                     String var27 = var25;
                     if (this.a_bool_ItemStack_String(var26, var27)) {
                        HashMap var28 = var3;
                        String var29 = var25;
                        int var30 = (Integer)var28.get(var29);
                        int var31 = var23.getCount();
                        int var32 = var30 - var31;
                        HashMap var33 = var3;
                        String var34 = var25;
                        int var35 = var32;
                        Integer var36 = Math.max(0, var35);
                        var33.put(var34, var36);
                     }
                  }
               }

               var19++;
            }
         }

         int var7 = var4;
         ItemStack var8 = var1.getSlot(var7).getItem();
         if (this.f(var8)) {
            ItemStack var9 = var8;
            label50:
            if (!this.c_bool_ItemStack(var9)) {
               if (this.t != null) {
                  ItemStack var10 = var8;
                  AutoEnchant.GearType var11 = this.b_GearType_ItemStack(var10);
                  AutoEnchant.GearType var12 = this.t;
                  if (var11 != var12) {
                     break label50;
                  }
               }

               ItemStack var13 = var8;

               for (String var15 : (Iterable<String>)(Object)this.a_Set_ItemStack(var13)) {
                  HashMap var16 = var3;
                  String var17 = var15;
                  Integer var18 = 1;
                  ((Map<String, Integer>)var16).merge(var17, var18, Integer::sum);
               }
            }
         }

         var4++;
      }
   }

   private int a_int_ItemStack_Set(ItemStack var1, Set<String> var2) {
      if (!var1.isEmpty() && var1.getItem() == Items.ENCHANTED_BOOK) {
         Map var3 = this.h(var1);
         int var4 = 0;

         for (String var6 : (Iterable<String>)(Object)var2) {
            if (!"blast_protection".equals(var6)) {
               String var7 = var6;
               if (!"protection".equals(var7)) {
                  int var16 = var4;
                  Map var17 = var3;
                  String var18 = var6;
                  Integer var19 = 0;
                  int var20 = (Integer)var17.getOrDefault(var18, var19);
                  var4 = Math.max(var16, var20);
                  continue;
               }
            }

            int var8 = var4;
            Map var9 = var3;
            Integer var10 = 0;
            int var11 = (Integer)var9.getOrDefault("blast_protection", var10);
            int var12 = Math.max(var8, var11);
            Map var13 = var3;
            Integer var14 = 0;
            int var15 = (Integer)var13.getOrDefault("protection", var14);
            var4 = Math.max(var12, var15);
         }

         return var4;
      } else {
         return 0;
      }
   }

   private void a_void_BlockPos_Set(BlockPos var1, Set<BlockPos> var2) {
      if (var1 != null) {
         var2.add(var1);
         if (mc.level != null) {
            BlockState var3 = mc.level.getBlockState(var1);
            if (var3.getBlock() instanceof ChestBlock && var3.hasProperty(ChestBlock.TYPE) && var3.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
               Direction var4 = (Direction)var3.getValue(ChestBlock.FACING);
               Direction var5;
               if ((ChestType)var3.getValue(ChestBlock.TYPE) == ChestType.LEFT) {
                  var5 = var4.getClockWise();
               } else {
                  var5 = var4.getCounterClockWise();
               }

               Direction var6 = var5;
               BlockPos var7 = var1.relative(var6);
               ClientLevel var8 = mc.level;
               BlockPos var9 = var7;
               if (var8.getBlockState(var9).getBlock() instanceof ChestBlock) {
                  BlockPos var10 = var7;
                  var2.add(var10);
               }
            }
         }
      }
   }

   private void a_void_AnvilMenu(AnvilMenu var1) {
      if (!var1.getCarried().isEmpty()) {
         this.a_void_AbstractContainerMenu(var1);
      } else if (!var1.getSlot(2).getItem().isEmpty()) {
         int var2 = var1.getCost();
         if (var2 > 0) {
            int var3 = mc.player.experienceLevel;
            int var4 = var2;
            if (var3 < var4) {
               return;
            }
         }

         MultiPlayerGameMode var5 = mc.gameMode;
         int var6 = var1.containerId;
         LocalPlayer var7 = mc.player;
         var5.handleContainerInput(var6, 2, 0, ContainerInput.QUICK_MOVE, var7);
         this.n.clear();
         this.s = 0;
         this.r = 0;
      } else {
         ItemStack var8 = var1.getSlot(0).getItem();
         ItemStack var9 = var1.getSlot(1).getItem();
         if (var8.isEmpty()) {
            int var10 = this.b_int_AnvilMenu(var1);
            if (var10 != -1) {
               MultiPlayerGameMode var11 = mc.gameMode;
               int var12 = var1.containerId;
               int var13 = var10;
               LocalPlayer var14 = mc.player;
               var11.handleContainerInput(var12, var13, 0, ContainerInput.QUICK_MOVE, var14);
               this.s = 0;
            } else {
               this.w();
               this.q = null;
               this.r = 0;
            }
         } else if (var9.isEmpty()) {
            ItemStack var15 = var8;
            int var16 = this.a_int_AnvilMenu_ItemStack(var1, var15);
            if (var16 != -1) {
               MultiPlayerGameMode var17 = mc.gameMode;
               int var18 = var1.containerId;
               int var19 = var16;
               LocalPlayer var20 = mc.player;
               var17.handleContainerInput(var18, var19, 0, ContainerInput.QUICK_MOVE, var20);
               this.s = 0;
            } else {
               MultiPlayerGameMode var21 = mc.gameMode;
               int var22 = var1.containerId;
               LocalPlayer var23 = mc.player;
               var21.handleContainerInput(var22, 0, 0, ContainerInput.QUICK_MOVE, var23);
               this.s = 0;
            }
         } else {
            int var24 = this.s + 1;
            this.s = var24;
            if (this.s > 20) {
               MultiPlayerGameMode var25 = mc.gameMode;
               int var26 = var1.containerId;
               LocalPlayer var27 = mc.player;
               var25.handleContainerInput(var26, 0, 0, ContainerInput.QUICK_MOVE, var27);
               MultiPlayerGameMode var28 = mc.gameMode;
               int var29 = var1.containerId;
               LocalPlayer var30 = mc.player;
               var28.handleContainerInput(var29, 1, 0, ContainerInput.QUICK_MOVE, var30);
               this.s = 0;
            }
         }
      }
   }

   private int b_int_AnvilMenu(AnvilMenu var1) {
      int var2 = 3;

      while (true) {
         int var3 = var2;
         int var4 = var1.slots.size();
         if (var3 >= var4) {
            return -1;
         }

         int var5 = var2;
         ItemStack var6 = var1.getSlot(var5).getItem();
         if (this.f(var6)) {
            ItemStack var7 = var6;
            label22:
            if (!this.c_bool_ItemStack(var7)) {
               if (this.t != null) {
                  ItemStack var8 = var6;
                  AutoEnchant.GearType var9 = this.b_GearType_ItemStack(var8);
                  AutoEnchant.GearType var10 = this.t;
                  if (var9 != var10) {
                     break label22;
                  }
               }

               ItemStack var11 = var6;
               if (this.a_int_AnvilMenu_ItemStack(var1, var11) != -1) {
                  return var2;
               }
            }
         }

         var2++;
      }
   }

   private int a_int_AnvilMenu_ItemStack(AnvilMenu var1, ItemStack var2) {
      int var3 = -1;
      int var4 = -1;
      int var5 = 3;

      while (true) {
         int var6 = var5;
         int var7 = var1.slots.size();
         if (var6 >= var7) {
            return var3;
         }

         int var8 = var5;
         ItemStack var9 = var1.getSlot(var8).getItem();
         int var10 = this.b(var2, var9);
         int var11 = var4;
         if (var10 > var11) {
            var4 = var10;
            var3 = var5;
         }

         var5++;
      }
   }

   private boolean C() {
      if (mc.player == null) {
         return false;
      }

      Inventory var1 = mc.player.getInventory();

      for (int var2 = 0; var2 < 36; var2++) {
         Inventory var3 = var1;
         int var4 = var2;
         ItemStack var5 = var3.getItem(var4);
         if (this.f(var5)) {
            ItemStack var6 = var5;
            if (!this.c_bool_ItemStack(var6)) {
               if (this.t != null) {
                  ItemStack var7 = var5;
                  AutoEnchant.GearType var8 = this.b_GearType_ItemStack(var7);
                  AutoEnchant.GearType var9 = this.t;
                  if (var8 != var9) {
                     continue;
                  }
               }

               for (int var10 = 0; var10 < 36; var10++) {
                  Inventory var11 = var1;
                  int var12 = var10;
                  ItemStack var13 = var11.getItem(var12);
                  ItemStack var14 = var5;
                  ItemStack var15 = var13;
                  if (this.a_bool_ItemStack_ItemStack(var14, var15)) {
                     return true;
                  }
               }
            }
         }
      }

      return false;
   }

   private int D() {
      if (mc.player == null) {
         return 0;
      }

      int var1 = 0;
      Inventory var2 = mc.player.getInventory();

      for (int var3 = 0; var3 < 36; var3++) {
         Inventory var4 = var2;
         int var5 = var3;
         ItemStack var6 = var4.getItem(var5);
         if (!var6.isEmpty() && var6.getItem() == Items.ENCHANTED_BOOK) {
            boolean var7 = false;

            for (int var8 = 0; var8 < 36; var8++) {
               Inventory var9 = var2;
               int var10 = var8;
               ItemStack var11 = var9.getItem(var10);
               if (this.f(var11)) {
                  ItemStack var12 = var11;
                  if (!this.c_bool_ItemStack(var12)) {
                     if (this.t != null) {
                        ItemStack var13 = var11;
                        AutoEnchant.GearType var14 = this.b_GearType_ItemStack(var13);
                        AutoEnchant.GearType var15 = this.t;
                        if (var14 != var15) {
                           continue;
                        }
                     }

                     ItemStack var16 = var11;
                     ItemStack var17 = var6;
                     if (this.a_bool_ItemStack_ItemStack(var16, var17)) {
                        var7 = true;
                        break;
                     }
                  }
               }
            }

            if (var7) {
               int var18 = var1;
               int var19 = var6.getCount();
               var1 = var18 + var19;
            }
         }
      }

      return var1;
   }

   public AutoEnchant.GearType b_GearType_ItemStack(ItemStack var1) {
      if (var1.isEmpty()) {
         return null;
      } else {
         Item var2 = var1.getItem();
         if (var2 == Items.DIAMOND_HELMET || var2 == Items.NETHERITE_HELMET || var2 == Items.TURTLE_HELMET) {
            return AutoEnchant.GearType.HELMET;
         } else if (var2 == Items.DIAMOND_CHESTPLATE || var2 == Items.NETHERITE_CHESTPLATE) {
            return AutoEnchant.GearType.CHESTPLATE;
         } else if (var2 == Items.DIAMOND_LEGGINGS || var2 == Items.NETHERITE_LEGGINGS) {
            return AutoEnchant.GearType.LEGGINGS;
         } else if (var2 == Items.DIAMOND_BOOTS || var2 == Items.NETHERITE_BOOTS) {
            return AutoEnchant.GearType.BOOTS;
         } else {
            return var2 != Items.DIAMOND_PICKAXE && var2 != Items.NETHERITE_PICKAXE && !var1.is(ItemTags.PICKAXES) ? null : AutoEnchant.GearType.PICKAXE;
         }
      }
   }

   private boolean d(ItemStack var1) {
      boolean var2;
      if (this.b_GearType_ItemStack(var1) == AutoEnchant.GearType.PICKAXE) {
         var2 = true;
      } else {
         var2 = false;
      }

      return var2;
   }

   private boolean e(ItemStack var1) {
      AutoEnchant.GearType var2 = this.b_GearType_ItemStack(var1);
      boolean var3;
      if (var2 != null && var2 != AutoEnchant.GearType.PICKAXE) {
         var3 = true;
      } else {
         var3 = false;
      }

      return var3;
   }

   private boolean f(ItemStack var1) {
      boolean var2;
      if (this.b_GearType_ItemStack(var1) != null) {
         var2 = true;
      } else {
         var2 = false;
      }

      return var2;
   }

   public boolean c_bool_ItemStack(ItemStack var1) {
      AutoEnchant.GearType var2 = this.b_GearType_ItemStack(var1);
      if (var2 == null) {
         return false;
      }

      Map var3 = this.h(var1);
      if (var2 == AutoEnchant.GearType.PICKAXE) {
         Map var4 = var3;
         Integer var5 = 0;
         if (((Integer)var4.getOrDefault("efficiency", var5)) > 0) {
            Map var6 = var3;
            Integer var7 = 0;
            if (((Integer)var6.getOrDefault("unbreaking", var7)) > 0) {
               Map var8 = var3;
               Integer var9 = 0;
               if (((Integer)var8.getOrDefault("mending", var9)) > 0) {
                  return true;
               }
            }
         }

         return false;
      } else {
         Map var11 = var3;
         Integer var12 = 0;
         if (((Integer)var11.getOrDefault("blast_protection", var12)) <= 0) {
            Map var13 = var3;
            Integer var14 = 0;
            if (((Integer)var13.getOrDefault("protection", var14)) <= 0) {
               return false;
            }
         }

         Map var15 = var3;
         Integer var16 = 0;
         if (((Integer)var15.getOrDefault("unbreaking", var16)) > 0) {
            Map var17 = var3;
            Integer var18 = 0;
            if (((Integer)var17.getOrDefault("mending", var18)) > 0) {
               return true;
            }
         }

         return false;
      }
   }

   private String g(ItemStack var1) {
      AutoEnchant.GearType var2 = this.b_GearType_ItemStack(var1);
      if (var2 == null) {
         return null;
      }

      Map var3 = this.h(var1);
      if (var2 == AutoEnchant.GearType.PICKAXE) {
         Map var4 = var3;
         Integer var5 = 0;
         if (((Integer)var4.getOrDefault("efficiency", var5)) == 0) {
            return "efficiency";
         }

         Map var6 = var3;
         Integer var7 = 0;
         if (((Integer)var6.getOrDefault("unbreaking", var7)) == 0) {
            return "unbreaking";
         }

         Map var8 = var3;
         Integer var9 = 0;
         if (((Integer)var8.getOrDefault("mending", var9)) == 0) {
            return "mending";
         }
      } else {
         Map var10 = var3;
         Integer var11 = 0;
         if (((Integer)var10.getOrDefault("blast_protection", var11)) == 0) {
            Map var12 = var3;
            Integer var13 = 0;
            if (((Integer)var12.getOrDefault("protection", var13)) == 0) {
               Map var14 = var3;
               Integer var15 = 0;
               if (((Integer)var14.getOrDefault("fire_protection", var15)) == 0) {
                  Map var16 = var3;
                  Integer var17 = 0;
                  if (((Integer)var16.getOrDefault("projectile_protection", var17)) == 0) {
                     return "blast_protection";
                  }
               }
            }
         }

         Map var18 = var3;
         Integer var19 = 0;
         if (((Integer)var18.getOrDefault("unbreaking", var19)) == 0) {
            return "unbreaking";
         }

         Map var20 = var3;
         Integer var21 = 0;
         if (((Integer)var20.getOrDefault("mending", var21)) == 0) {
            return "mending";
         }
      }

      return null;
   }

   private boolean a_bool_ItemStack_String(ItemStack var1, String var2) {
      if (!var1.isEmpty() && var1.getItem() == Items.ENCHANTED_BOOK) {
         Map var3 = this.h(var1);
         if (!"blast_protection".equals(var2) && !"protection".equals(var2)) {
            return var3.containsKey(var2);
         }

         boolean var4;
         if (!var3.containsKey("blast_protection") && !var3.containsKey("protection")) {
            var4 = false;
         } else {
            var4 = true;
         }

         return var4;
      } else {
         return false;
      }
   }

   public boolean a_bool_ItemStack_String_int(ItemStack var1, String var2, int var3) {
      if (!var1.isEmpty() && var3 > 0) {
         AutoEnchant.GearType var4 = this.b_GearType_ItemStack(var1);
         if (var4 == null) {
            return false;
         }

         Map var5 = this.h(var1);
         if (var4 == AutoEnchant.GearType.PICKAXE) {
            if (var2.equals("efficiency")) {
               Map var6 = var5;
               Integer var7 = 0;
               boolean var8;
               if (((Integer)var6.getOrDefault("efficiency", var7)) == 0) {
                  var8 = true;
               } else {
                  var8 = false;
               }

               return var8;
            }

            if (var2.equals("unbreaking")) {
               Map var9 = var5;
               Integer var10 = 0;
               boolean var11;
               if (((Integer)var9.getOrDefault("unbreaking", var10)) == 0) {
                  var11 = true;
               } else {
                  var11 = false;
               }

               return var11;
            }

            if (var2.equals("mending")) {
               Map var12 = var5;
               Integer var13 = 0;
               boolean var14;
               if (((Integer)var12.getOrDefault("mending", var13)) == 0) {
                  var14 = true;
               } else {
                  var14 = false;
               }

               return var14;
            }
         } else {
            if (var2.equals("blast_protection") || var2.equals("protection")) {
               Map var15 = var5;
               Integer var16 = 0;
               if (((Integer)var15.getOrDefault("blast_protection", var16)) == 0) {
                  Map var17 = var5;
                  Integer var18 = 0;
                  if (((Integer)var17.getOrDefault("protection", var18)) == 0) {
                     Map var19 = var5;
                     Integer var20 = 0;
                     if (((Integer)var19.getOrDefault("fire_protection", var20)) == 0) {
                        Map var21 = var5;
                        Integer var22 = 0;
                        if (((Integer)var21.getOrDefault("projectile_protection", var22)) == 0) {
                           return true;
                        }
                     }
                  }
               }

               return false;
            }

            if (var2.equals("unbreaking")) {
               Map var24 = var5;
               Integer var25 = 0;
               boolean var26;
               if (((Integer)var24.getOrDefault("unbreaking", var25)) == 0) {
                  var26 = true;
               } else {
                  var26 = false;
               }

               return var26;
            }

            if (var2.equals("mending")) {
               Map var27 = var5;
               Integer var28 = 0;
               boolean var29;
               if (((Integer)var27.getOrDefault("mending", var28)) == 0) {
                  var29 = true;
               } else {
                  var29 = false;
               }

               return var29;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private boolean a_bool_ItemStack_ItemStack(ItemStack var1, ItemStack var2) {
      if (!var1.isEmpty() && !var2.isEmpty() && var2.getItem() == Items.ENCHANTED_BOOK) {
         for (Entry var4 : (Iterable<Entry>)(Object)this.h(var2).entrySet()) {
            String var5 = (String)var4.getKey();
            int var6 = (Integer)var4.getValue();
            if (this.a_bool_ItemStack_String_int(var1, var5, var6)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private int b(ItemStack var1, ItemStack var2) {
      if (!this.a_bool_ItemStack_ItemStack(var1, var2)) {
         return -1;
      }

      Map var3 = this.h(var2);
      int var4 = 0;

      for (Entry var6 : (Iterable<Entry>)(Object)var3.entrySet()) {
         String var7 = (String)var6.getKey();
         int var8 = (Integer)var6.getValue();
         String var9 = var7;
         int var10 = var8;
         if (this.a_bool_ItemStack_String_int(var1, var9, var10)) {
            int var11 = var8;
            int var12 = var4;
            if (var11 > var12) {
               var4 = var8;
            }
         }
      }

      return var4;
   }

   private Map<String, Integer> h(ItemStack var1) {
      HashMap var2 = new HashMap();
      if (var1 != null && !var1.isEmpty()) {
         ItemEnchantments var3 = (ItemEnchantments)var1.get(DataComponents.STORED_ENCHANTMENTS);
         if (var3 != null) {
            for (Holder var5 : (Iterable<Holder>)(Object)var3.keySet()) {
               String var6 = var5.getRegisteredName();
               if (var6 != null) {
                  int var7 = var6.lastIndexOf(58);
                  String var10;
                  if (var7 != -1) {
                     String var8 = var6;
                     int var9 = var7 + 1;
                     var10 = var8.substring(var9);
                  } else {
                     var10 = var6;
                  }

                  String var11 = var10;
                  HashMap var12 = var2;
                  String var13 = var11;
                  ItemEnchantments var14 = var3;
                  Holder var15 = var5;
                  Integer var16 = var14.getLevel(var15);
                  var12.put(var13, var16);
               }
            }
         }

         ItemEnchantments var17 = var1.getEnchantments();
         if (var17 != null) {
            for (Holder var19 : (Iterable<Holder>)(Object)var17.keySet()) {
               String var20 = var19.getRegisteredName();
               if (var20 != null) {
                  int var21 = var20.lastIndexOf(58);
                  String var24;
                  if (var21 != -1) {
                     String var22 = var20;
                     int var23 = var21 + 1;
                     var24 = var22.substring(var23);
                  } else {
                     var24 = var20;
                  }

                  String var25 = var24;
                  HashMap var26 = var2;
                  String var27 = var25;
                  ItemEnchantments var28 = var17;
                  Holder var29 = var19;
                  Integer var30 = var28.getLevel(var29);
                  var26.put(var27, var30);
               }
            }
         }

         return var2;
      } else {
         return var2;
      }
   }

   private BlockPos a_BlockPos_Set_Set(Set<BlockPos> var1, Set<BlockPos> var2) {
      if (mc.player != null && mc.level != null) {
         double var3 = this.range.getValue().doubleValue();
         double var5 = this.range.getValue().doubleValue();
         double var7 = var3 * var5;
         Vec3 var9 = mc.player.getEyePosition();
         BlockPos var10 = mc.player.blockPosition();
         int var11 = (int)Math.ceil(this.range.getValue().doubleValue());
         BlockPos var12 = null;
         double var13 = Double.MAX_VALUE;
         int var15 = -var11;

         while (true) {
            int var16 = var15;
            int var17 = var11;
            if (var16 > var17) {
               return var12;
            }

            int var18 = -var11;

            while (true) {
               int var19 = var18;
               int var20 = var11;
               if (var19 > var20) {
                  var15++;
                  break;
               }

               int var21 = -var11;

               while (true) {
                  int var22 = var21;
                  int var23 = var11;
                  if (var22 > var23) {
                     var18++;
                     break;
                  }

                  BlockPos var24 = var10;
                  int var25 = var15;
                  int var26 = var18;
                  int var27 = var21;
                  BlockPos var28 = var24.offset(var25, var26, var27);
                  Set var29 = this.j;
                  BlockPos var30 = var28;
                  label49:
                  if (!var29.contains(var30)) {
                     if (var1 != null) {
                        BlockPos var31 = var28;
                        if (var1.contains(var31)) {
                           break label49;
                        }
                     }

                     if (var2 != null) {
                        BlockPos var32 = var28;
                        if (var2.contains(var32)) {
                           break label49;
                        }
                     }

                     BlockPos var33 = var28;
                     Vec3 var34 = var9;
                     double var35 = var33.distToCenterSqr(var34);
                     double var37 = var7;
                     if (!(var35 > var37)) {
                        ClientLevel var39 = mc.level;
                        BlockPos var40 = var28;
                        if (var39.getBlockState(var40).getBlock() instanceof ChestBlock) {
                           double var41 = var35;
                           double var43 = var13;
                           if (var41 < var43) {
                              var13 = var35;
                              var12 = var28;
                           }
                        }
                     }
                  }

                  var21++;
               }
            }
         }
      } else {
         return null;
      }
   }

   private BlockPos a_BlockPos_Set(Set<BlockPos> var1) {
      return this.a_BlockPos_Set_Set(var1, null);
   }

   private BlockPos E() {
      if (mc.player != null && mc.level != null) {
         double var1 = this.range.getValue().doubleValue();
         double var3 = this.range.getValue().doubleValue();
         double var5 = var1 * var3;
         Vec3 var7 = mc.player.getEyePosition();
         BlockPos var8 = mc.player.blockPosition();
         int var9 = (int)Math.ceil(this.range.getValue().doubleValue());
         BlockPos var10 = null;
         double var11 = Double.MAX_VALUE;
         int var13 = -var9;

         while (true) {
            int var14 = var13;
            int var15 = var9;
            if (var14 > var15) {
               return var10;
            }

            int var16 = -var9;

            while (true) {
               int var17 = var16;
               int var18 = var9;
               if (var17 > var18) {
                  var13++;
                  break;
               }

               int var19 = -var9;

               while (true) {
                  int var20 = var19;
                  int var21 = var9;
                  if (var20 > var21) {
                     var16++;
                     break;
                  }

                  BlockPos var22 = var8;
                  int var23 = var13;
                  int var24 = var16;
                  int var25 = var19;
                  BlockPos var26 = var22.offset(var23, var24, var25);
                  Vec3 var27 = var7;
                  double var28 = var26.distToCenterSqr(var27);
                  double var30 = var5;
                  if (!(var28 > var30)) {
                     ClientLevel var32 = mc.level;
                     BlockPos var33 = var26;
                     if (var32.getBlockState(var33).getBlock() instanceof AnvilBlock) {
                        double var34 = var28;
                        double var36 = var11;
                        if (var34 < var36) {
                           var11 = var28;
                           var10 = var26;
                        }
                     }
                  }

                  var19++;
               }
            }
         }
      } else {
         return null;
      }
   }

   public enum GearType {
      HELMET,
      CHESTPLATE,
      LEGGINGS,
      BOOTS,
      PICKAXE;
   }
}
