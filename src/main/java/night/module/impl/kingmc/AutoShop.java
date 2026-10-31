package night.module.impl.kingmc;

import java.util.Deque;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerScoreEntry;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import night.Night;
import night.events.EventTarget;
import night.events.impl.ClientDisconnectEvent;
import night.events.impl.PacketReceiveEvent;
import night.events.impl.PlayerDeathEvent;
import night.events.impl.UpdateWalkingPlayerEvent;
import night.module.Module;
import night.module.ModuleInfo;
import night.module.impl.combat.Mainhand;
import night.setting.impl.BooleanSetting;
import night.setting.impl.KeybindSetting;
import night.setting.impl.NumberSetting;
import night.setting.impl.SubBindSetting;
import night.util.inventory.InventoryUtil;

@ModuleInfo(
   name = "AutoShop",
   description = "Automates the server /shop GUI to buy totems, end crystals, or exp bottles.",
   category = Module.Category.KINGMC,
   persistent = true
)
public class AutoShop extends Module {
   public KeybindSetting totem;
   public NumberSetting totemAmount;
   public BooleanSetting belowThresold;
   public SubBindSetting belowThresold2;
   public NumberSetting thresold;
   public KeybindSetting crystal;
   public NumberSetting crystalStacks;
   public BooleanSetting crystalBelowThresold;
   public SubBindSetting crystalBelowThresold2;
   public NumberSetting crystalThresold;
   public KeybindSetting exp;
   public NumberSetting expStacks;
   public BooleanSetting expBelowThresold;
   public SubBindSetting expBelowThresold2;
   public NumberSetting expThresold;
   public KeybindSetting obsidian;
   public NumberSetting obsidianStacks;
   public BooleanSetting obsidianBelowThresold;
   public SubBindSetting obsidianBelowThresold2;
   public NumberSetting x;
   public KeybindSetting y;
   public NumberSetting z;
   public BooleanSetting enderPearlBelowThresold;
   public SubBindSetting enderPearlBelowThresold2;
   public NumberSetting enderPearlThresold;
   public BooleanSetting disableOnDeath;
   public BooleanSetting disableOnLog;
   public NumberSetting actionDelay;
   public BooleanSetting silent;
   public BooleanSetting debug;
   private static final int J = 13;
   private static final int K = 10;
   private static final int L = 16;
   private static final int M = 9;
   private static final int N = 14;
   private static final int O = 15;
   private static final int P = 16;
   private static final int Q = 23;
   private static final int R = 100;
   private static final int S = 10;
   private static final int T = 5;
   private static final int U = 10;
   private int V;
   private int W;
   private AutoShop.ShopState X;
   private final Deque<AutoShop.BuyOrder> Y;
   private boolean Z;
   private AutoShop.BuyOrder aa;
   private int ab;
   private int ac;
   private boolean ad;
   private static final long ae = 500L;
   private long af;
   private int ag;
   private int ah;
   static long I = 0L;
   private boolean ai;
   private static final String aj = "java.kingmc.vn or kingmc.vn";
   private static final Set<String> ak = Set.of("java.kingmc.vn", "kingmc.vn");
   private static final int al = 3;
   private static final int am = 5;
   private static final long an = 30000L;
   private Object ao;
   private Object ap;
   private long aq;
   private static final long ar = 2000L;
   private final Map<AutoShop.ShopItem, Long> as;
   private String at;

   private void b_void_String(String var1) {
      if (this.debug.getValue()) {
         long var2 = System.currentTimeMillis();
         long var4 = 100000L;
         long var6 = var2 % var4;
         String var8 = String.valueOf(this.X);
         Night.LOGGER.info("[AutoShop] " + var6 + " [" + var8 + "] " + var1);
      }
   }

   @EventTarget
   public void a_void_PacketReceiveEvent(PacketReceiveEvent var1) {
      if (this.debug.getValue()) {
         Packet var2 = var1.getPacket();
         if (var2 instanceof ClientboundSystemChatPacket var3) {
            String var4;
            if (var3.overlay()) {
               var4 = " (actionbar)";
            } else {
               var4 = "";
            }

            String var8 = var3.content().getString();
            this.b_void_String("server msg" + var4 + ": " + var8);
         } else if (var2 instanceof ClientboundSetActionBarTextPacket) {
            this.b_void_String("actionbar: " + ((ClientboundSetActionBarTextPacket)var2).text().getString());
         } else if (var2 instanceof ClientboundOpenScreenPacket var5) {
            int var6 = var5.getContainerId();
            String var7 = var5.getTitle().getString();
            this.b_void_String("open screen id=" + var6 + " title=" + var7);
         } else if (var2 instanceof ClientboundContainerClosePacket) {
            this.b_void_String("server closed window " + ((ClientboundContainerClosePacket)var2).getContainerId());
         }
      }
   }

   private int A() {
      AbstractContainerMenu var1 = mc.player.containerMenu;
      InventoryMenu var2 = mc.player.inventoryMenu;
      int var3;
      if (var1 != var2) {
         var3 = mc.player.containerMenu.containerId;
      } else {
         var3 = -1;
      }

      return var3;
   }

   private boolean B() {
      int var1 = this.A();
      if (var1 != -1) {
         int var2 = var1;
         int var3 = this.W;
         if (var2 != var3) {
            return true;
         }
      }

      return false;
   }

   public AutoShop() {
      KeybindSetting var1 = new KeybindSetting("Totem", "Keybind to auto buy totems.", 0).disableHoldMode();
      this.totem = var1;
      Integer var2 = 1;
      Integer var3 = 1;
      Integer var4 = 10;
      this.totemAmount = new NumberSetting("TotemAmount", "How many totems to buy.", var2, var3, var4);
      this.belowThresold = new BooleanSetting("BelowThresold", "Auto buy totems for you if your inventory has less totems than set thresold.", false);
      BooleanSetting var5 = this.belowThresold;
      this.belowThresold2 = new SubBindSetting(
         "BelowThresold", "Auto buy totems for you if your inventory has less totems than set thresold. The bind toggles it on/off.", var5
      );
      BooleanSetting var7 = this.belowThresold;
      BooleanSetting.BooleanCondition var6 = new BooleanSetting.BooleanCondition(var7, true);
      Integer var8 = 3;
      Integer var9 = 1;
      Integer var10 = 36;
      this.thresold = new NumberSetting("Thresold", "Totem count (inventory + offhand) below which totems are bought automatically.", var6, var8, var9, var10);
      KeybindSetting var11 = new KeybindSetting("Crystal", "Keybind to auto buy end crystals.", 0).disableHoldMode();
      this.crystal = var11;
      Integer var12 = 1;
      Integer var13 = 1;
      Integer var14 = 10;
      this.crystalStacks = new NumberSetting("CrystalStacks", "How many stacks (64) of end crystals to buy.", var12, var13, var14);
      this.crystalBelowThresold = new BooleanSetting(
         "CrystalBelowThresold", "BelowThresold", "Auto buy end crystals for you if your inventory has less end crystals than set thresold.", false
      );
      BooleanSetting var15 = this.crystalBelowThresold;
      this.crystalBelowThresold2 = new SubBindSetting(
         "CrystalBelowThresold",
         "BelowThresold",
         "Auto buy end crystals for you if your inventory has less end crystals than set thresold. The bind toggles it on/off.",
         var15
      );
      BooleanSetting var17 = this.crystalBelowThresold;
      BooleanSetting.BooleanCondition var16 = new BooleanSetting.BooleanCondition(var17, true);
      Integer var18 = 200;
      Integer var19 = 100;
      Integer var20 = 500;
      Integer var21 = 10;
      this.crystalThresold = new NumberSetting(
         "CrystalThresold",
         "Thresold",
         "End crystal count (inventory + offhand) below which crystals are bought automatically.",
         var16,
         var18,
         var19,
         var20,
         var21
      );
      KeybindSetting var22 = new KeybindSetting("Exp", "Keybind to auto buy exp bottles.", 0).disableHoldMode();
      this.exp = var22;
      Integer var23 = 1;
      Integer var24 = 1;
      Integer var25 = 10;
      this.expStacks = new NumberSetting("ExpStacks", "How many stacks (64) of exp bottles to buy.", var23, var24, var25);
      this.expBelowThresold = new BooleanSetting(
         "ExpBelowThresold", "BelowThresold", "Auto buy exp bottles for you if your inventory has less exp bottles than set thresold.", false
      );
      BooleanSetting var26 = this.expBelowThresold;
      this.expBelowThresold2 = new SubBindSetting(
         "ExpBelowThresold",
         "BelowThresold",
         "Auto buy exp bottles for you if your inventory has less exp bottles than set thresold. The bind toggles it on/off.",
         var26
      );
      BooleanSetting var28 = this.expBelowThresold;
      BooleanSetting.BooleanCondition var27 = new BooleanSetting.BooleanCondition(var28, true);
      Integer var29 = 200;
      Integer var30 = 100;
      Integer var31 = 500;
      Integer var32 = 10;
      this.expThresold = new NumberSetting(
         "ExpThresold",
         "Thresold",
         "Exp bottle count (inventory + offhand) below which exp bottles are bought automatically.",
         var27,
         var29,
         var30,
         var31,
         var32
      );
      KeybindSetting var33 = new KeybindSetting("Obsidian", "Keybind to auto buy obsidian.", 0).disableHoldMode();
      this.obsidian = var33;
      Integer var34 = 1;
      Integer var35 = 1;
      Integer var36 = 10;
      this.obsidianStacks = new NumberSetting("ObsidianStacks", "How many stacks (64) of obsidian to buy.", var34, var35, var36);
      this.obsidianBelowThresold = new BooleanSetting(
         "ObsidianBelowThresold", "BelowThresold", "Auto buy obsidian for you if your inventory has less obsidian than set thresold.", false
      );
      BooleanSetting var37 = this.obsidianBelowThresold;
      this.obsidianBelowThresold2 = new SubBindSetting(
         "ObsidianBelowThresold",
         "BelowThresold",
         "Auto buy obsidian for you if your inventory has less obsidian than set thresold. The bind toggles it on/off.",
         var37
      );
      BooleanSetting var39 = this.obsidianBelowThresold;
      BooleanSetting.BooleanCondition var38 = new BooleanSetting.BooleanCondition(var39, true);
      Integer var40 = 200;
      Integer var41 = 100;
      Integer var42 = 500;
      Integer var43 = 10;
      this.x = new NumberSetting(
         "ObsidianThresold",
         "Thresold",
         "Obsidian count (inventory + offhand) below which obsidian is bought automatically.",
         var38,
         var40,
         var41,
         var42,
         var43
      );
      KeybindSetting var44 = new KeybindSetting("EnderPearl", "Keybind to auto buy ender pearls.", 0).disableHoldMode();
      this.y = var44;
      Integer var45 = 1;
      Integer var46 = 1;
      Integer var47 = 10;
      this.z = new NumberSetting("EnderPearlStacks", "How many stacks (64) of ender pearls to buy.", var45, var46, var47);
      this.enderPearlBelowThresold = new BooleanSetting(
         "EnderPearlBelowThresold", "BelowThresold", "Auto buy ender pearls for you if your inventory has less ender pearls than set thresold.", false
      );
      BooleanSetting var48 = this.enderPearlBelowThresold;
      this.enderPearlBelowThresold2 = new SubBindSetting(
         "EnderPearlBelowThresold",
         "BelowThresold",
         "Auto buy ender pearls for you if your inventory has less ender pearls than set thresold. The bind toggles it on/off.",
         var48
      );
      BooleanSetting var50 = this.enderPearlBelowThresold;
      BooleanSetting.BooleanCondition var49 = new BooleanSetting.BooleanCondition(var50, true);
      Integer var51 = 16;
      Integer var52 = 1;
      Integer var53 = 64;
      this.enderPearlThresold = new NumberSetting(
         "EnderPearlThresold",
         "Thresold",
         "Ender pearl count (inventory + offhand) below which ender pearls are bought automatically.",
         var49,
         var51,
         var52,
         var53
      );
      this.disableOnDeath = new BooleanSetting("DisableOnDeath", "Turns off every BelowThresold option when you die.", false);
      this.disableOnLog = new BooleanSetting("DisableOnLog", "Turns off every BelowThresold option when you leave the server.", false);
      Integer var54 = 4;
      Integer var55 = 0;
      Integer var56 = 20;
      this.actionDelay = new NumberSetting("ActionDelay", "Ticks between each shop GUI click.", var54, var55, var56);
      this.silent = new BooleanSetting("Silent", "Buys without showing the shop GUI, so you can keep moving and looking around.", true);
      this.debug = new BooleanSetting(
         "Debug", "Writes every shop step (and server messages) to the game log file -- for diagnosing slow or failed buys. No chat output.", false
      );
      this.V = 0;
      this.W = -1;
      this.X = AutoShop.ShopState.IDLE;
      this.Y = new ConcurrentLinkedDeque<>();
      this.Z = false;
      this.aa = null;
      this.ab = 0;
      this.ac = 0;
      this.ad = false;
      long var57 = 0L;
      this.af = var57;
      this.ag = -1;
      this.ah = 0;
      this.ai = false;
      this.ao = null;
      this.ap = null;
      long var59 = 0L;
      this.aq = var59;
      this.as = new HashMap<>();
      this.at = null;
      this.bind.disableHoldMode();
   }

   public void b_void_int(int var1) {
      if (var1 != 0) {
         long var2 = System.currentTimeMillis();
         long var4 = I;
         if (var2 >= var4) {
            if (this.totem.getKey() != 0) {
               int var6 = this.totem.getKey();
               if (var1 == var6) {
                  int var7 = this.totemAmount.getValue().intValue();
                  this.a(AutoShop.ShopItem.Totem, var7);
                  return;
               }
            }

            if (this.crystal.getKey() != 0) {
               int var8 = this.crystal.getKey();
               if (var1 == var8) {
                  int var9 = this.crystalStacks.getValue().intValue();
                  this.a(AutoShop.ShopItem.Crystal, var9);
                  return;
               }
            }

            if (this.exp.getKey() != 0) {
               int var10 = this.exp.getKey();
               if (var1 == var10) {
                  int var11 = this.expStacks.getValue().intValue();
                  this.a(AutoShop.ShopItem.Exp, var11);
                  return;
               }
            }

            if (this.obsidian.getKey() != 0) {
               int var12 = this.obsidian.getKey();
               if (var1 == var12) {
                  int var13 = this.obsidianStacks.getValue().intValue();
                  this.a(AutoShop.ShopItem.Obsidian, var13);
                  return;
               }
            }

            if (this.y.getKey() != 0) {
               int var14 = this.y.getKey();
               if (var1 == var14) {
                  int var15 = this.z.getValue().intValue();
                  this.a(AutoShop.ShopItem.EnderPearl, var15);
               }
            }
         }
      }
   }

   private boolean C() {
      if (!mc.hasSingleplayerServer() && mc.getCurrentServer() != null) {
         String var1 = ServerAddress.parseString(mc.getCurrentServer().ip).getHost().toLowerCase(Locale.ROOT);
         return ak.contains(var1);
      } else {
         return false;
      }
   }

   public synchronized void a(AutoShop.ShopItem var1, int var2) {
      long var3 = System.currentTimeMillis();
      long var5 = I;
      if (var3 >= var5) {
         if (mc.player != null && mc.getConnection() != null) {
            if (this.C()) {
               if (!this.w()) {
                  if (var2 > 0) {
                     String var7 = this.getName();
                     Night.chatManager.sendTaggedMessage("Buying " + var1.name().toLowerCase() + "...", var7);
                     if (var1 == AutoShop.ShopItem.Totem) {
                        this.ai = true;
                     }

                     this.Y.add(new AutoShop.BuyOrder(var1, var2));
                     String var8 = String.valueOf(var1);
                     int var9 = this.b_int_Item(Items.TOTEM_OF_UNDYING);
                     this.b_void_String("enqueue " + var8 + " x" + var2 + " (totems now=" + var9 + ")");
                     if (this.X == AutoShop.ShopState.IDLE) {
                        InventoryUtil.g = this.silent.getValue();
                        InventoryUtil.i = true;
                        AutoShop.BuyOrder var10 = this.Y.poll();
                        this.aa = var10;
                        this.ab = 0;
                        this.ac = 0;
                        this.ad = false;
                        this.V = 0;
                        int var11 = this.A();
                        this.W = var11;
                        this.F();
                        this.X = AutoShop.ShopState.WAIT_SHOP;
                     }
                  }
               }
            }
         }
      }
   }

   public synchronized void v() {
      if (this.D()) {
         if (this.X == AutoShop.ShopState.IDLE) {
            this.a(AutoShop.ShopItem.Totem, 3);
         } else {
            long var1 = System.currentTimeMillis();
            long var3 = I;
            if (var1 >= var3) {
               if (this.aa != null && this.aa.a == AutoShop.ShopItem.Totem) {
                  AutoShop.BuyOrder var5 = this.aa;
                  int var6;
                  if (this.aa.b <= 0) {
                     var6 = 3;
                  } else {
                     var6 = Math.min(this.aa.b + 1, 5);
                  }

                  var5.b = var6;
                  this.b_void_String("buyTotemNow: running totem order -> remaining=" + this.aa.b);
               } else {
                  AutoShop.BuyOrder var7 = this.Y.peekFirst();
                  if (var7 != null && var7.a == AutoShop.ShopItem.Totem) {
                     AutoShop.BuyOrder var8 = var7;
                     int var9 = Math.min(var7.b + 1, 5);
                     var8.b = var9;
                     this.b_void_String("buyTotemNow: queued totem order -> remaining=" + var7.b);
                  } else {
                     String var10 = this.getName();
                     Night.chatManager.sendTaggedMessage("Buying totem...", var10);
                     this.ai = true;
                     AutoShop.BuyOrder var11 = new AutoShop.BuyOrder(AutoShop.ShopItem.Totem, 3);
                     if (this.X != AutoShop.ShopState.WAIT_SHOP && this.X != AutoShop.ShopState.CLICK_ITEM) {
                        Deque var15 = this.Y;
                        AutoShop.BuyOrder var16 = var11;
                        var15.addFirst(var16);
                        this.Z = true;
                        AutoShop.ShopItem var17;
                        if (this.aa != null) {
                           var17 = this.aa.a;
                        } else {
                           var17 = null;
                        }

                        this.b_void_String("buyTotemNow: preempting " + var17);
                     } else {
                        if (this.aa != null) {
                           Deque var12 = this.Y;
                           AutoShop.BuyOrder var13 = this.aa;
                           var12.addFirst(var13);
                        }

                        AutoShop.BuyOrder var14 = var11;
                        this.aa = var14;
                        this.ad = false;
                     }
                  }
               }
            }
         }
      }
   }

   private boolean D() {
      if (this.w()) {
         return false;
      }

      ClientLevel var1 = mc.level;
      Object var2 = this.ap;
      if (var1 == var2) {
         long var3 = System.currentTimeMillis();
         long var5 = this.aq;
         if (var3 < var5) {
            return false;
         }
      }

      return true;
   }

   public boolean w() {
      if (mc.level == null) {
         return false;
      }

      Scoreboard var1 = mc.level.getScoreboard();
      Objective var2 = var1.getDisplayObjective(DisplaySlot.SIDEBAR);
      if (var2 == null) {
         return false;
      }

      Scoreboard var3 = var1;
      Objective var4 = var2;

      for (PlayerScoreEntry var6 : (Iterable<PlayerScoreEntry>)(Object)var3.listPlayerScores(var4)) {
         Scoreboard var7 = var1;
         String var8 = var6.owner();
         PlayerTeam var9 = var7.getPlayersTeam(var8);
         Component var10 = var6.ownerName();
         String var11 = PlayerTeam.formatNameForTeam(var9, var10).getString();
         if (var11.contains("/dk ") || var11.contains("/dn ")) {
            return true;
         }
      }

      return false;
   }

   private void E() {
      if (mc.player != null) {
         Screen var1 = mc.gui.screen();
         if (var1 != null && !(var1 instanceof AbstractContainerScreen)) {
            if (mc.getConnection() != null) {
               ClientPacketListener var2 = mc.getConnection();
               int var3 = mc.player.containerMenu.containerId;
               var2.send(new ServerboundContainerClosePacket(var3));
            }

            LocalPlayer var4 = mc.player;
            InventoryMenu var5 = mc.player.inventoryMenu;
            var4.containerMenu = var5;
         } else {
            mc.player.closeContainer();
         }
      }
   }

   private void F() {
      if (mc.getConnection() != null) {
         this.b_void_String("send /shop open gear");
         mc.getConnection().sendCommand("shop open gear");
      }
   }

   public boolean x() {
      boolean var1;
      if (this.X != AutoShop.ShopState.IDLE) {
         var1 = true;
      } else {
         var1 = false;
      }

      return var1;
   }

   private boolean a_bool_Item(Item var1) {
      for (int var2 = InventoryUtil.c; var2 <= InventoryUtil.f; var2++) {
         Inventory var3 = mc.player.getInventory();
         int var4 = var2;
         ItemStack var5 = var3.getItem(var4);
         if (var5.isEmpty()) {
            return true;
         }

         if (var5.is(var1)) {
            int var6 = var5.getMaxStackSize();
            int var7;
            if (var6 == 64) {
               var7 = 55;
            } else if (var6 == 16) {
               var7 = 11;
            } else {
               var7 = var6;
            }

            int var8 = var7;
            int var9 = var5.getCount();
            int var10 = var8;
            if (var9 < var10) {
               return true;
            }
         }
      }

      return false;
   }

   private static Item a_Item_ShopItem(AutoShop.ShopItem var0) {
      return switch (var0) {
         case Totem -> Items.TOTEM_OF_UNDYING;
         case Crystal -> Items.END_CRYSTAL;
         case Exp -> Items.EXPERIENCE_BOTTLE;
         case Obsidian -> Items.OBSIDIAN;
         case EnderPearl -> Items.ENDER_PEARL;
      };
   }

   private int b_int_Item(Item var1) {
      int var2;
      if (mc.player.getOffhandItem().is(var1)) {
         var2 = mc.player.getOffhandItem().getCount();
      } else {
         var2 = 0;
      }

      int var3 = var2;

      for (int var4 = InventoryUtil.c; var4 <= InventoryUtil.f; var4++) {
         Inventory var5 = mc.player.getInventory();
         int var6 = var4;
         if (var5.getItem(var6).is(var1)) {
            int var7 = var3;
            Inventory var8 = mc.player.getInventory();
            int var9 = var4;
            int var10 = var8.getItem(var9).getCount();
            var3 = var7 + var10;
         }
      }

      return var3;
   }

   private boolean a(BooleanSetting var1, NumberSetting var2, Item var3, AutoShop.ShopItem var4, int var5) {
      long var6 = System.currentTimeMillis();
      long var8 = I;
      if (var6 < var8) {
         return false;
      }

      if (!var1.getValue()) {
         return false;
      }

      int var10 = this.b_int_Item(var3);
      int var11 = var2.getValue().intValue();
      if (var10 >= var11) {
         return false;
      }

      if (!this.C()) {
         this.a(var4, "only works when joined through java.kingmc.vn or kingmc.vn");
         return false;
      }

      if (!this.D()) {
         this.a(var4, "in hub/login, or /shop didn't open in this world");
         return false;
      }

      if (var4 != AutoShop.ShopItem.Totem) {
         Mainhand var12 = Night.moduleManager.getModule(Mainhand.class);
         if (var12 != null && var12.isEnabled()) {
            int var13 = var12.slot.getValue().intValue() - 1;
            Inventory var14 = mc.player.getInventory();
            int var15 = var13;
            if (!var14.getItem(var15).is(Items.TOTEM_OF_UNDYING)) {
               this.a(var4, "Mainhand slot " + (var13 + 1) + " has no totem yet (would fill it)");
               return false;
            }
         }
      }

      if (!mc.player.containerMenu.getCarried().isEmpty()) {
         this.a(var4, "an item is on your cursor (dropping it first)");
         MultiPlayerGameMode var16 = mc.gameMode;
         int var17 = mc.player.containerMenu.containerId;
         LocalPlayer var18 = mc.player;
         var16.handleContainerInput(var17, -999, 0, ContainerInput.PICKUP, var18);
         return false;
      }

      if (!this.a_bool_Item(var3)) {
         this.a(var4, "inventory full (no free slot)");
         return false;
      }

      long var19 = System.currentTimeMillis();
      Map var21 = this.as;
      Long var22 = 0L;
      long var23 = (Long)var21.getOrDefault(var4, var22);
      long var25 = var19 - var23;
      long var27 = 2000L;
      if (var25 < var27) {
         return false;
      }

      Map var29 = this.as;
      Long var30 = var19;
      var29.put(var4, var30);
      this.at = null;
      this.a(var4, var5);
      return true;
   }

   private void a(AutoShop.ShopItem var1, String var2) {
      String var3 = "Can't buy " + var1.name().toLowerCase() + ": " + var2;
      String var4 = this.at;
      if (!var3.equals(var4)) {
         String var5 = var3;
         this.at = var5;
      }
   }

   private AutoShop.ShopItem G() {
      if (this.belowThresold.getValue()) {
         int var1 = this.b_int_Item(Items.TOTEM_OF_UNDYING);
         int var2 = this.thresold.getValue().intValue();
         if (var1 < var2) {
            return AutoShop.ShopItem.Totem;
         }
      }

      if (this.crystalBelowThresold.getValue()) {
         int var3 = this.b_int_Item(Items.END_CRYSTAL);
         int var4 = this.crystalThresold.getValue().intValue();
         if (var3 < var4) {
            return AutoShop.ShopItem.Crystal;
         }
      }

      if (this.expBelowThresold.getValue()) {
         int var5 = this.b_int_Item(Items.EXPERIENCE_BOTTLE);
         int var6 = this.expThresold.getValue().intValue();
         if (var5 < var6) {
            return AutoShop.ShopItem.Exp;
         }
      }

      if (this.obsidianBelowThresold.getValue()) {
         int var7 = this.b_int_Item(Items.OBSIDIAN);
         int var8 = this.x.getValue().intValue();
         if (var7 < var8) {
            return AutoShop.ShopItem.Obsidian;
         }
      }

      if (this.enderPearlBelowThresold.getValue()) {
         int var9 = this.b_int_Item(Items.ENDER_PEARL);
         int var10 = this.enderPearlThresold.getValue().intValue();
         if (var9 < var10) {
            return AutoShop.ShopItem.EnderPearl;
         }
      }

      return null;
   }

   @EventTarget
   public void a_void_PlayerDeathEvent(PlayerDeathEvent var1) {
      if (this.disableOnDeath.getValue()) {
         Player var2 = var1.getPlayer();
         LocalPlayer var3 = mc.player;
         if (var2 == var3) {
            this.I();
         }
      }
   }

   @EventTarget
   public void a_void_ClientDisconnectEvent(ClientDisconnectEvent var1) {
      if (this.disableOnLog.getValue()) {
         this.I();
      }
   }

   private boolean H() {
      Screen var1 = mc.gui.screen();
      boolean var2;
      if (var1 != null && (!this.silent.getValue() || var1 instanceof AbstractContainerScreen)) {
         var2 = false;
      } else {
         var2 = true;
      }

      return var2;
   }

   private void I() {
      this.belowThresold.setValue(false);
      this.crystalBelowThresold.setValue(false);
      this.expBelowThresold.setValue(false);
      this.obsidianBelowThresold.setValue(false);
      this.enderPearlBelowThresold.setValue(false);
   }

   @EventTarget
   public void a_void_UpdateWalkingPlayerEvent(UpdateWalkingPlayerEvent var1) {
      long var2 = System.currentTimeMillis();
      long var4 = I;
      if (var2 < var4) {
         AutoShop.ShopItem var6;
         if (mc.player != null) {
            var6 = this.G();
         } else {
            var6 = null;
         }

         AutoShop.ShopItem var209 = var6;
         if (var209 != null) {
            AutoShop.ShopItem var210 = var209;
            String var211 = "AutoShop is locked for a few seconds";
            this.a(var210, var211);
         }
      } else if (this.X == AutoShop.ShopState.IDLE) {
         AutoShop.ShopItem var7;
         if (mc.player != null) {
            var7 = this.G();
         } else {
            var7 = null;
         }

         AutoShop.ShopItem var179 = var7;
         if (var179 == null) {
            this.at = null;
         } else if (!mc.player.isAlive()) {
            AutoShop.ShopItem var180 = var179;
            String var181 = "you are dead";
            this.a(var180, var181);
         } else if (!this.H()) {
            AutoShop.ShopItem var182 = var179;
            String var183 = "a screen is open (" + mc.gui.screen().getClass().getSimpleName() + "), close it";
            this.a(var182, var183);
         }

         if (mc.player != null && mc.player.isAlive() && this.H()) {
            BooleanSetting var184 = this.belowThresold;
            NumberSetting var185 = this.thresold;
            int var186 = this.totemAmount.getValue().intValue();
            if (!this.a(var184, var185, Items.TOTEM_OF_UNDYING, AutoShop.ShopItem.Totem, var186)) {
               BooleanSetting var187 = this.crystalBelowThresold;
               NumberSetting var188 = this.crystalThresold;
               Item var189 = Items.END_CRYSTAL;
               AutoShop.ShopItem var190 = AutoShop.ShopItem.Crystal;
               int var191 = this.crystalStacks.getValue().intValue();
               if (!this.a(var187, var188, var189, var190, var191)) {
                  BooleanSetting var192 = this.expBelowThresold;
                  NumberSetting var193 = this.expThresold;
                  Item var194 = Items.EXPERIENCE_BOTTLE;
                  AutoShop.ShopItem var195 = AutoShop.ShopItem.Exp;
                  int var196 = this.expStacks.getValue().intValue();
                  if (!this.a(var192, var193, var194, var195, var196)) {
                     BooleanSetting var197 = this.obsidianBelowThresold;
                     NumberSetting var198 = this.x;
                     Item var199 = Items.OBSIDIAN;
                     AutoShop.ShopItem var200 = AutoShop.ShopItem.Obsidian;
                     int var201 = this.obsidianStacks.getValue().intValue();
                     if (!this.a(var197, var198, var199, var200, var201)) {
                        BooleanSetting var202 = this.enderPearlBelowThresold;
                        NumberSetting var203 = this.enderPearlThresold;
                        Item var204 = Items.ENDER_PEARL;
                        AutoShop.ShopItem var205 = AutoShop.ShopItem.EnderPearl;
                        int var206 = this.z.getValue().intValue();
                        if (!this.a(var202, var203, var204, var205, var206)) {
                           boolean var212 = false;
                           return;
                        }
                     }
                  }
               }
            }

            boolean var207 = true;
         }
      } else if (mc.player != null && mc.level != null) {
         if (this.X == AutoShop.ShopState.WAIT_SHOP) {
            if (!this.B()) {
               int var11 = this.ac;
               byte var12 = 1;
               int var13 = var11 + var12;
               this.ac = var13;
               byte var14 = 10;
               if (var13 < var14) {
                  return;
               }

               byte var15 = 0;
               this.ac = var15;
               ClientLevel var16 = mc.level;
               Object var17 = this.ao;
               boolean var18;
               if (var16 == var17) {
                  var18 = true;
               } else {
                  var18 = false;
               }

               boolean var19 = var18;
               int var20 = this.V;
               byte var21 = 1;
               int var22 = var20 + var21;
               int var23 = this.A();
               int var24 = this.W;
               String var25 = "shop not open after 10 ticks, retry #" + var22 + " (open window=" + var23 + ", stale=" + var24 + ")";
               this.b_void_String(var25);
               int var26 = this.V;
               byte var27 = 1;
               int var28 = var26 + var27;
               this.V = var28;
               byte var29;
               if (var19) {
                  var29 = 5;
               } else {
                  var29 = 1;
               }

               if (var28 > var29) {
                  if (!var19) {
                     ClientLevel var30 = mc.level;
                     this.ap = var30;
                     long var31 = System.currentTimeMillis();
                     long var33 = 30000L;
                     long var35 = var31 + var33;
                     this.aq = var35;
                  }

                  String var37 = "Shop GUI never opened after 5 tries (server refused /shop? e.g. combat tag, check chat).";
                  this.c(var37);
                  return;
               }

               this.E();
               int var38 = this.A();
               this.W = var38;
               this.F();
               return;
            }

            String var9 = "shop window open id=" + this.A();
            this.b_void_String(var9);
            ClientLevel var10 = mc.level;
            this.ao = var10;
            this.X = AutoShop.ShopState.CLICK_ITEM;
            this.ac = 0;
            this.ab = 0;
            if (this.actionDelay.getValue().intValue() > 0) {
               return;
            }
         }

         AutoShop.ShopState var39 = this.X;
         AutoShop.ShopState var40 = AutoShop.ShopState.WAIT_PAGE;
         if (var39 == var40) {
            if (!this.B()) {
               int var45 = this.ac;
               byte var46 = 1;
               int var47 = var45 + var46;
               this.ac = var47;
               byte var48 = 10;
               if (var47 < var48) {
                  return;
               }

               byte var49 = 0;
               this.ac = var49;
               int var50 = this.V;
               byte var51 = 1;
               int var52 = var50 + var51;
               int var53 = this.A();
               int var54 = this.W;
               String var55 = "item page not open after 10 ticks, retry #" + var52 + " (open window=" + var53 + ", stale=" + var54 + ")";
               this.b_void_String(var55);
               int var56 = this.V;
               byte var57 = 1;
               int var58 = var56 + var57;
               this.V = var58;
               byte var59 = 5;
               if (var58 > var59) {
                  AbstractContainerMenu var60 = mc.player.containerMenu;
                  ItemStack var61 = ItemStack.EMPTY;
                  var60.setCarried(var61);
                  String var62 = "Item page never opened after 5 tries.";
                  this.c(var62);
                  return;
               }

               AbstractContainerMenu var63 = mc.player.containerMenu;
               ItemStack var64 = ItemStack.EMPTY;
               var63.setCarried(var64);
               this.E();
               int var65 = this.A();
               this.W = var65;
               this.F();
               AutoShop.ShopState var66 = AutoShop.ShopState.WAIT_SHOP;
               this.X = var66;
               return;
            }

            String var41 = "item page open id=" + this.A();
            this.b_void_String(var41);
            AutoShop.ShopState var42 = AutoShop.ShopState.DECIDE;
            this.X = var42;
            byte var43 = 0;
            this.ac = var43;
            byte var44 = 0;
            this.ab = var44;
            if (this.actionDelay.getValue().intValue() > 0) {
               return;
            }
         }

         AutoShop.ShopState var67 = this.X;
         AutoShop.ShopState var68 = AutoShop.ShopState.DECIDE;
         if (var67 != var68) {
            int var69 = this.ab;
            int var70 = this.actionDelay.getValue().intValue();
            if (var69 < var70) {
               int var71 = this.ab;
               byte var72 = 1;
               int var73 = var71 + var72;
               this.ab = var73;
               return;
            }
         }

         byte var74 = 0;
         this.ab = var74;
         AbstractContainerMenu var75 = mc.player.containerMenu;
         InventoryMenu var76 = mc.player.inventoryMenu;
         boolean var77;
         if (var75 != var76) {
            var77 = true;
         } else {
            var77 = false;
         }

         boolean var78 = var77;
         AutoShop.ShopState var79 = this.X;
         AutoShop.ShopState var80 = AutoShop.ShopState.WAIT_SHOP;
         if (var79 != var80) {
            AutoShop.ShopState var81 = this.X;
            AutoShop.ShopState var82 = AutoShop.ShopState.WAIT_PAGE;
            if (var81 != var82 && !var78) {
               String var83 = "Shop GUI closed unexpectedly.";
               this.c(var83);
               return;
            }
         }

         label253:
         if (this.Z) {
            AutoShop.ShopState var84 = this.X;
            AutoShop.ShopState var85 = AutoShop.ShopState.SET_11;
            if (var84 != var85) {
               AutoShop.ShopState var86 = this.X;
               AutoShop.ShopState var87 = AutoShop.ShopState.CONFIRM;
               if (var86 != var87) {
                  break label253;
               }
            }

            AutoShop.ShopState var88 = AutoShop.ShopState.DECIDE;
            this.X = var88;
         }

         AutoShop.ShopState var89 = this.X;
         switch (this.X) {
            case WAIT_SHOP:
            case WAIT_PAGE:
            default:
               break;
            case CLICK_ITEM:
               if (this.aa == null) {
                  AutoShop.ShopState var129 = AutoShop.ShopState.DECIDE;
                  this.X = var129;
                  return;
               }
               byte var134 = switch (this.aa.a) {
                  case Totem -> 13;
                  case Crystal -> 10;
                  case Exp -> 16;
                  case Obsidian -> 9;
                  case EnderPearl -> 14;
                  default -> {
                     Object var132 = null;
                     Object var133 = null;
                     MatchException var131 = new MatchException((String)var132, (Throwable)var133);
                     throw var131;
                  }
               };
               AbstractContainerMenu var135 = mc.player.containerMenu;
               byte var136 = var134;
               if (var135.getSlot(var136).getItem().isEmpty()) {
                  int var137 = this.ac;
                  byte var138 = 1;
                  int var139 = var137 + var138;
                  this.ac = var139;
                  byte var140 = 10;
                  if (var139 < var140) {
                     return;
                  }
               }

               int var141 = this.A();
               this.W = var141;
               byte var142 = var134;
               this.c(var142);
               byte var143 = 0;
               this.ac = var143;
               AutoShop.ShopState var144 = AutoShop.ShopState.WAIT_PAGE;
               this.X = var144;
               break;
            case DECIDE:
               if (this.Z) {
                  boolean var90 = false;
                  this.Z = var90;
                  if (this.aa != null && this.aa.b > 0) {
                     AutoShop.BuyOrder var91 = this.Y.pollFirst();
                     Deque var92 = this.Y;
                     AutoShop.BuyOrder var93 = this.aa;
                     var92.addFirst(var93);
                     if (var91 != null) {
                        Deque var94 = this.Y;
                        AutoShop.BuyOrder var95 = var91;
                        var94.addFirst(var95);
                     }

                     Object var96 = null;
                     this.aa = (AutoShop.BuyOrder)var96;
                  }
               }

               if (this.aa != null) {
                  long var97 = this.af;
                  long var99 = 0L;
                  if (var97 != var99) {
                     long var101 = System.currentTimeMillis();
                     long var103 = this.af;
                     long var105 = var101 - var103;
                     long var107 = 500L;
                     if (var105 < var107) {
                        if (this.aa.b <= 0) {
                           Item var109 = a_Item_ShopItem(this.aa.a);
                           int var110 = this.b_int_Item(var109);
                           int var111 = this.ah;
                           if (var110 <= var111) {
                              return;
                           }
                        }

                        if (this.aa.b > 0) {
                           int var112 = mc.player.containerMenu.getStateId();
                           int var113 = this.ag;
                           if (var112 == var113) {
                              return;
                           }
                        }
                     }
                  }
               }

               if (this.aa == null || this.aa.b <= 0) {
                  if (!this.Y.isEmpty()) {
                     AutoShop.BuyOrder var114 = this.Y.poll();
                     this.aa = var114;
                     long var115 = 0L;
                     this.af = var115;
                     boolean var117 = false;
                     this.ad = var117;
                     byte var118 = 0;
                     this.V = var118;
                     byte var119 = 0;
                     this.ac = var119;
                     int var120 = this.A();
                     this.W = var120;
                     this.F();
                     AutoShop.ShopState var121 = AutoShop.ShopState.WAIT_SHOP;
                     this.X = var121;
                     return;
                  } else {
                     this.J();
                     return;
                  }
               }

               Item var122 = a_Item_ShopItem(this.aa.a);
               if (!this.a_bool_Item(var122)) {
                  String var123 = "Inventory full.";
                  this.c(var123);
                  return;
               }

               AutoShop.ShopItem var124 = this.aa.a;
               AutoShop.ShopItem var125 = AutoShop.ShopItem.Totem;
               if (var124 == var125) {
                  AutoShop.ShopState var126 = AutoShop.ShopState.CONFIRM;
                  this.X = var126;
               } else if (!this.ad) {
                  AutoShop.ShopState var127 = AutoShop.ShopState.SET_11;
                  this.X = var127;
               } else {
                  AutoShop.ShopState var128 = AutoShop.ShopState.CONFIRM;
                  this.X = var128;
               }
               break;
            case SET_11:
               byte var169;
               label234: {
                  if (this.aa != null) {
                     AutoShop.ShopItem var167 = this.aa.a;
                     AutoShop.ShopItem var168 = AutoShop.ShopItem.EnderPearl;
                     if (var167 == var168) {
                        var169 = 15;
                        break label234;
                     }
                  }

                  var169 = 16;
               }

               this.c(var169);
               boolean var170 = true;
               this.ad = var170;
               AutoShop.ShopState var171 = AutoShop.ShopState.CONFIRM;
               this.X = var171;
               break;
            case CONFIRM:
               AbstractContainerMenu var145 = mc.player.containerMenu;
               byte var146 = 23;
               if (var145.getSlot(var146).getItem().isEmpty()) {
                  int var147 = this.ac;
                  byte var148 = 1;
                  int var149 = var147 + var148;
                  this.ac = var149;
                  byte var150 = 10;
                  if (var149 < var150) {
                     return;
                  }
               }

               byte var151 = 0;
               this.ac = var151;
               if (this.aa != null) {
                  AutoShop var152 = this;
                  Item var153 = a_Item_ShopItem(this.aa.a);
                  int var154 = var152.b_int_Item(var153);
                  this.ah = var154;
               }

               byte var155 = 23;
               this.c(var155);
               long var156 = System.currentTimeMillis();
               this.af = var156;
               int var158 = mc.player.containerMenu.getStateId();
               this.ag = var158;
               if (this.aa != null) {
                  AutoShop.ShopItem var159 = this.aa.a;
                  AutoShop.ShopItem var160 = AutoShop.ShopItem.Totem;
                  if (var159 == var160) {
                     boolean var161 = true;
                     this.ai = var161;
                  }

                  AutoShop.BuyOrder var162 = this.aa;
                  int var163 = var162.b;
                  byte var164 = 1;
                  int var165 = var163 - var164;
                  var162.b = var165;
               }

               AutoShop.ShopState var166 = AutoShop.ShopState.DECIDE;
               this.X = var166;
         }

         AutoShop.ShopState var172 = var89;
         AutoShop.ShopState var173 = AutoShop.ShopState.DECIDE;
         if (var172 == var173 && this.actionDelay.getValue().intValue() == 0) {
            AutoShop.ShopState var174 = this.X;
            AutoShop.ShopState var175 = AutoShop.ShopState.CONFIRM;
            if (var174 != var175) {
               AutoShop.ShopState var176 = this.X;
               AutoShop.ShopState var177 = AutoShop.ShopState.SET_11;
               if (var176 != var177) {
                  return;
               }
            }

            UpdateWalkingPlayerEvent var178 = var1;
            this.a_void_UpdateWalkingPlayerEvent(var178);
         }
      } else {
         String var8 = "Player left world";
         this.c(var8);
      }
   }

   private void J() {
      int var1;
      if (mc.player != null) {
         var1 = this.b_int_Item(Items.TOTEM_OF_UNDYING);
      } else {
         var1 = -1;
      }

      long var2 = System.currentTimeMillis();
      long var4 = this.af;
      long var6 = var2 - var4;
      this.b_void_String("finish (totems now=" + var1 + ", " + var6 + "ms after last CONFIRM)");
      long var8 = 0L;
      this.af = var8;
      boolean var10 = this.ai;
      this.ai = false;
      this.Z = false;
      this.X = AutoShop.ShopState.IDLE;
      InventoryUtil.g = false;
      InventoryUtil.i = false;
      this.Y.clear();
      this.aa = null;
      if (mc.player != null) {
         this.E();
      }

      if (var10 && this.y()) {
         double var11 = Math.random();
         double var13 = 0.5;
         if (var11 < var13) {
            this.z();
            long var15 = System.currentTimeMillis();
            long var17 = 10000L;
            I = var15 + var17;
         }
      }
   }

   private void c(String var1) {
      this.b_void_String("ABORT: " + var1);
      long var2 = 0L;
      this.af = var2;
      this.ai = false;
      this.Z = false;
      this.X = AutoShop.ShopState.IDLE;
      InventoryUtil.g = false;
      InventoryUtil.i = false;
      this.Y.clear();
      this.aa = null;
      if (mc.player != null) {
         this.E();
      }
   }

   boolean y() {
      try {
         double var1 = InventoryUtil.f(3);
         double var3 = 1.0;
         double var5 = Math.abs(var1 - var3);
         double var7 = 0.05;
         return var5 > var7;
      } catch (Throwable var10) {
         return true;
      }
   }

   void z() {
      if (mc.player != null && mc.gameMode != null) {
         int var1 = mc.player.inventoryMenu.containerId;

         for (Slot var3 : (Iterable<Slot>)(Object)mc.player.inventoryMenu.slots) {
            if (var3 != null && var3.getItem() != null && var3.getItem().is(Items.TOTEM_OF_UNDYING)) {
               MultiPlayerGameMode var4 = mc.gameMode;
               int var5 = var1;
               int var6 = var3.index;
               LocalPlayer var7 = mc.player;
               var4.handleContainerInput(var5, var6, 1, ContainerInput.THROW, var7);
            }
         }
      }
   }

   private void c(int var1) {
      if (this.debug.getValue()) {
         AbstractContainerMenu var2 = mc.player.containerMenu;
         int var3 = var2.slots.size();
         String var4;
         if (var1 < var3) {
            var4 = var2.getSlot(var1).getItem().getHoverName().getString();
         } else {
            var4 = "?";
         }

         String var5 = var4;
         int var6 = var2.containerId;
         int var7 = var2.getStateId();
         this.b_void_String("click slot " + var1 + " [" + var5 + "] window=" + var6 + " stateId=" + var7);
      }

      InventoryUtil.h = true;

      try {
         InventoryUtil.a_void_int_int_ContainerInput(var1, 0, ContainerInput.PICKUP);
      } catch (Throwable var9) {
         InventoryUtil.h = false;
         throw var9;
      }

      InventoryUtil.h = false;
   }

   @Override
   public String getDisplayInfo() {
      if (this.aa != null) {
         String var1 = this.aa.a.name();
         int var2 = this.aa.b;
         String var3;
         if (this.Y.isEmpty()) {
            var3 = "";
         } else {
            var3 = " (+" + this.Y.size() + ")";
         }

         return var1 + " " + var2 + var3;
      } else {
         return "";
      }
   }

   public static class BuyOrder {
      public final AutoShop.ShopItem a;
      public int b;

      public BuyOrder(AutoShop.ShopItem var1, int var2) {
         this.a = var1;
         this.b = var2;
      }
   }

   public enum ShopItem {
      Totem,
      Crystal,
      Exp,
      Obsidian,
      EnderPearl;
   }

   enum ShopState {
      IDLE,
      WAIT_SHOP,
      CLICK_ITEM,
      WAIT_PAGE,
      DECIDE,
      SET_11,
      CONFIRM;
   }
}
