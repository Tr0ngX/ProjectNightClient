package night.module.impl.player;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.mojang.blaze3d.vertex.PoseStack;
import java.awt.Color;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;
import lombok.Generated;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResult.Fail;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.events.EventTarget;
import night.events.impl.Render3DEvent;
import night.events.impl.TickEvent;
import night.manager.RotationManager;
import night.mixins.accessors.ClientPlayerInteractionManagerAccessor;
import night.module.Module;
import night.module.ModuleInfo;
import night.setting.impl.BooleanSetting;
import night.setting.impl.KeybindSetting;
import night.setting.impl.ModeSetting;
import night.setting.impl.NumberSetting;
import night.util.input.KeyNameUtil;
import night.util.inventory.InventoryUtil;
import night.util.io.FileUtil;
import night.util.render.Render3DUtil;
import night.util.rotation.RotationMathUtil;
import night.util.world.BlockInteractionUtil;
import night.util.world.PacketAndRenderUtil;

@ModuleInfo(name = "Rekit", description = "Automatically regears from an ender chest shulker using a saved kit.", category = Module.Category.PLAYER)
public class Rekit extends Module {
   public NumberSetting delay;
   public NumberSetting frequency;
   public BooleanSetting auto;
   public BooleanSetting silentContainer;
   public ModeSetting swapMode;
   public BooleanSetting considerShulkerName;
   public BooleanSetting ignoreCustomName;
   public KeybindSetting autoPlace;
   public BooleanSetting rotate;
   public BooleanSetting strictDirection;
   public ModeSetting pickaxe;
   public ModeSetting helmet;
   public ModeSetting chestplate;
   public ModeSetting leggings;
   public ModeSetting boots;
   public Map<Integer, Rekit.KitSlotEntry> t;
   public String u;
   private int w;
   private static final String x = "Night/Kits/";
   private static final String y = "Night/last_kit_save.txt";
   public static volatile long v = 0L;
   private Rekit.RekitState z;
   private int A;
   private int B;
   private int C;
   private BlockPos D;
   private long E;
   private BlockPos F;
   private boolean G;
   private int H;
   private static final int I = 3;
   private int J;
   private static final int K = 3;
   private int L;
   private static final int M = 20;
   private int N;
   private int O;
   private boolean P;
   private boolean Q;
   private int R;
   private static final long S = 400L;
   private static final long T = 100L;
   private static final int U = 2000;

   public boolean v() {
      boolean var1;
      if (this.z != Rekit.RekitState.IDLE) {
         var1 = true;
      } else {
         var1 = false;
      }

      return var1;
   }

   public Rekit() {
      Integer var1 = 1;
      Integer var2 = 0;
      Integer var3 = 10;
      this.delay = new NumberSetting("Delay", "Tick delay between pull actions.", var1, var2, var3);
      Integer var4 = 1;
      Integer var5 = 1;
      Integer var6 = 5;
      this.frequency = new NumberSetting("Frequency", "Max pull actions per tick.", var4, var5, var6);
      this.auto = new BooleanSetting("Auto", "Auto pull shulkers from the ender chest.", false);
      this.silentContainer = new BooleanSetting("SilentContainer", "Pull items without the container GUI showing on screen.", false);
      this.swapMode = new ModeSetting(
         "SwapMode", "Swap type used when placing shulkers and swapping to a breaking tool in Auto mode.", "AltSwap", InventoryUtil.a
      );
      this.considerShulkerName = new BooleanSetting(
         "ConsiderShulkerName", "Prefer the shulker whose custom name matches the active kit's name, overriding content-based scoring.", false
      );
      this.ignoreCustomName = new BooleanSetting(
         "IgnoreCustomName", "Skip any container with a custom (non-vanilla) title such as a shop GUI, except shulker boxes.", false
      );
      KeybindSetting var7 = new KeybindSetting(
            "AutoPlace",
            "Keybind: places the shulker in your inventory, opens it, pulls kit items, closes, then breaks it. No ender chest -- one-shot version of Auto.",
            0
         )
         .disableHoldMode();
      this.autoPlace = var7;
      this.rotate = new BooleanSetting("Rotate", "Rotates toward each block before placing it.", true);
      this.strictDirection = new BooleanSetting("StrictDirection", "Only places using directions that face you.", false);
      String[] var8 = new String[]{"Efficiency", "SilkTouch"};
      this.pickaxe = new ModeSetting("Pickaxe", "Preferred pickaxe enchant when choosing between candidates.", "Efficiency", var8);
      String[] var9 = new String[]{"Blast", "Prot"};
      this.helmet = new ModeSetting("Helmet", "Preferred helmet enchant when choosing between candidates.", "Prot", var9);
      String[] var10 = new String[]{"Blast", "Prot"};
      this.chestplate = new ModeSetting("Chestplate", "Preferred chestplate enchant when choosing between candidates.", "Prot", var10);
      String[] var11 = new String[]{"Blast", "Prot"};
      this.leggings = new ModeSetting("Leggings", "Preferred leggings enchant when choosing between candidates.", "Prot", var11);
      String[] var12 = new String[]{"Blast", "Prot"};
      this.boots = new ModeSetting("Boots", "Preferred boots enchant when choosing between candidates.", "Prot", var12);
      this.t = new HashMap<>();
      this.u = "";
      this.w = 0;
      this.z = Rekit.RekitState.IDLE;
      this.A = 0;
      this.B = 0;
      this.C = -1;
      this.D = null;
      long var13 = 0L;
      this.E = var13;
      this.F = null;
      this.G = false;
      this.H = 0;
      this.J = 0;
      this.L = 0;
      this.N = -1;
      this.O = -1;
      this.P = false;
      this.Q = false;
      this.R = -1;
      this.B();
   }

   private void B() {
      try {
         if (!FileUtil.c("Night/last_kit_save.txt")) {
            return;
         }

         String[] var1 = new String[0];
         String var2 = Files.readString(Paths.get("Night/last_kit_save.txt", var1)).trim();
         if (var2.isEmpty()) {
            return;
         }

         File var3 = new File("Night/Kits/", var2 + ".json");
         if (!var3.exists()) {
            return;
         }

         Gson var4 = new Gson();
         File var5 = var3;

         try (FileReader var6 = new FileReader(var5)) {
            Type var7 = (new TypeToken<Map<Integer, Rekit.KitSlotEntry>>() {}).getType();
            Gson var8 = var4;
            FileReader var9 = var6;
            Type var10 = var7;
            Map var11 = (Map)var8.fromJson(var9, var10);
            this.t = var11;
            String var12 = var2;
            this.u = var12;
         }
      } catch (Exception var20) {
      }
   }

   private void e_void_String(String var1) {
      String var2 = this.getName();
      Night.chatManager.sendTaggedMessage(var1, var2);
   }

   private void f(String var1) {
      String var2 = this.getName();
      Night.chatManager.sendTaggedMessage("\u00a7c" + var1, var2);
   }

   public List<String> w() {
      ArrayList var1 = new ArrayList();
      File[] var2 = new File("Night/Kits/").listFiles();
      if (var2 != null) {
         File[] var3 = var2;
         int var4 = var3.length;
         int var5 = 0;

         while (true) {
            int var6 = var5;
            int var7 = var4;
            if (var6 >= var7) {
               break;
            }

            File[] var8 = var3;
            int var9 = var5;
            File var10 = var8[var9];
            if (var10.getName().endsWith(".json")) {
               ArrayList var11 = var1;
               String var12 = var10.getName().replace(".json", "");
               var11.add(var12);
            }

            var5++;
         }
      }

      return var1;
   }

   public void b_void_String(String var1) {
      if (mc.player != null) {
         if (!mc.player.isCreative() && !mc.player.isSpectator()) {
            HashMap var2 = new HashMap();

            for (int var3 = 0; var3 < 36; var3++) {
               int var4 = var3;
               int var5 = this.e_int_int(var4);
               InventoryMenu var6 = mc.player.inventoryMenu;
               int var7 = var5;
               ItemStack var8 = var6.getSlot(var7).getItem();
               if (!var8.isEmpty()) {
                  Rekit.KitSlotEntry var9 = new Rekit.KitSlotEntry();
                  Item var10 = var8.getItem();
                  String var11 = BuiltInRegistries.ITEM.getKey(var10).toString();
                  var9.a = var11;
                  Rekit.KitSlotEntry var12 = var9;
                  int var13 = var8.getMaxStackSize();
                  var12.c = var13;
                  if (var8.has(DataComponents.CUSTOM_NAME)) {
                     Rekit.KitSlotEntry var14 = var9;
                     String var15 = var8.getHoverName().getString();
                     var14.b = var15;
                  }

                  HashMap var16 = var2;
                  Integer var17 = var3;
                  Rekit.KitSlotEntry var18 = var9;
                  var16.put(var17, var18);
               }
            }

            try {
               FileUtil.b("Night/Kits/");
               Gson var19 = new GsonBuilder().setPrettyPrinting().create();

               try (FileWriter var20 = new FileWriter(new File("Night/Kits/", var1 + ".json"))) {
                  Gson var21 = var19;
                  HashMap var22 = var2;
                  FileWriter var23 = var20;
                  var21.toJson(var22, var23);
               }

               HashMap var28 = var2;
               this.t = var28;
               this.u = var1;
               String[] var29 = new String[0];
               Path var30 = Paths.get("Night/last_kit_save.txt", var29);
               OpenOption[] var31 = new OpenOption[0];
               Files.writeString(var30, var1, var31);
               this.e_void_String("Kit saved and activated: " + var1);
            } catch (Exception var35) {
               this.f("Error saving kit!");
            }
         }
      }
   }

   public void c_void_String(String var1) {
      try {
         File var2 = new File("Night/Kits/", var1 + ".json");
         if (!var2.exists()) {
            this.f("Kit not found: " + var1);
            return;
         }

         Gson var3 = new Gson();
         File var4 = var2;

         try (FileReader var5 = new FileReader(var4)) {
            Type var6 = (new TypeToken<Map<Integer, Rekit.KitSlotEntry>>() {}).getType();
            Gson var7 = var3;
            FileReader var8 = var5;
            Type var9 = var6;
            Map var10 = (Map)var7.fromJson(var8, var9);
            this.t = var10;
            this.u = var1;
         }

         String[] var15 = new String[0];
         Path var16 = Paths.get("Night/last_kit_save.txt", var15);
         OpenOption[] var17 = new OpenOption[0];
         Files.writeString(var16, var1, var17);
         this.e_void_String("Kit loaded: " + var1);
      } catch (Exception var21) {
         this.f("Error occurred while reading kit!");
      }
   }

   public void x() {
      File[] var1 = new File("Night/Kits/").listFiles();
      if (var1 != null && var1.length != 0) {
         this.e_void_String("Available kits:");
         File[] var2 = var1;
         int var3 = var2.length;
         int var4 = 0;

         while (true) {
            int var5 = var4;
            int var6 = var3;
            if (var5 >= var6) {
               return;
            }

            File[] var7 = var2;
            int var8 = var4;
            File var9 = var7[var8];
            if (var9.getName().endsWith(".json")) {
               String var10 = var9.getName().replace(".json", "");
               String var11 = this.u;
               if (var10.equals(var11)) {
                  String var12 = this.getName();
                  Night.chatManager.sendTaggedMessage("\u00a79- " + var10 + " [active]", var12);
               } else {
                  String var13 = this.getName();
                  Night.chatManager.sendTaggedMessage("\u00a77- " + var10, var13);
               }
            }

            var4++;
         }
      } else {
         this.e_void_String("You don't have any kits.");
      }
   }

   public void y() {
      if (this.u != null && !this.u.isEmpty()) {
         String var1 = this.getName();
         Night.chatManager.sendTaggedMessage("\u00a7aActive kit: \u00a79" + this.u, var1);
      } else {
         this.f("No kit is currently active.");
      }
   }

   public void d(String var1) {
      File var2 = new File("Night/Kits/", var1 + ".json");
      if (var2.exists() && var2.delete()) {
         String var3 = this.u;
         if (var1.equals(var3)) {
            this.t.clear();
            this.u = "";
         }

         this.e_void_String("Kit deleted: " + var1);
      } else {
         this.f("Failed to delete kit.");
      }
   }

   @Override
   public void onEnable() {
      this.z = Rekit.RekitState.IDLE;
      this.A = 0;
      this.B = 0;
      this.C = -1;
      this.D = null;
      this.F = null;
      this.G = false;
      this.L = 0;
      this.N = -1;
      this.O = -1;
      this.P = false;
      this.Q = false;
   }

   @EventTarget
   public void a_void_TickEvent(TickEvent var1) {
      if (mc.player != null) {
         if (mc.gui.screen() == null) {
            boolean var2 = KeyNameUtil.b(this.autoPlace.getKey());
            if (var2 && !this.Q && this.z == Rekit.RekitState.IDLE && !mc.player.isCreative() && !mc.player.isSpectator() && this.H() != -1) {
               this.P = true;
               this.z = Rekit.RekitState.PLACE_SHULKER;
               this.A = 0;
               this.G = false;
               this.H = 0;
            }

            boolean var3 = var2;
            this.Q = var3;
         } else {
            this.Q = false;
         }

         if (!mc.player.isCreative() && !mc.player.isSpectator()) {
            if (!this.auto.getValue() && !this.P) {
               this.C();
               if (mc.player.hasContainerOpen()) {
                  this.E();
               }
            } else {
               this.F();
            }
         }
      }
   }

   private void C() {
      if (this.silentContainer.getValue() && mc.gui.screen() instanceof AbstractContainerScreen && !(mc.gui.screen() instanceof InventoryScreen)) {
         mc.gui.setScreen(null);
      }
   }

   @EventTarget
   public void a_void_Render3DEvent(Render3DEvent var1) {
      if (this.auto.getValue() && this.D != null) {
         long var2 = System.currentTimeMillis();
         long var4 = this.E;
         float var6 = (float)(var2 - var4) / 400.0F;
         float var7 = Math.min(1.0F, var6);
         float var8 = 0.85F * var7;
         float var9 = 0.15F + var8;
         double var10 = this.D.getX();
         double var12 = 0.5;
         double var14 = var10 + var12;
         double var16 = this.D.getY();
         double var18 = 0.5;
         double var20 = var16 + var18;
         double var22 = this.D.getZ();
         double var24 = 0.5;
         double var26 = var22 + var24;
         double var28 = 0.5;
         double var30 = var9;
         double var32 = var28 * var30;
         double var34 = var14;
         double var36 = var32;
         double var38 = var34 - var36;
         double var40 = var20;
         double var42 = var32;
         double var44 = var40 - var42;
         double var46 = var26;
         double var48 = var32;
         double var50 = var46 - var48;
         double var52 = var14;
         double var54 = var32;
         double var56 = var52 + var54;
         double var58 = var20;
         double var60 = var32;
         double var62 = var58 + var60;
         double var64 = var26;
         double var66 = var32;
         double var68 = var64 + var66;
         AABB var70 = new AABB(var38, var44, var50, var56, var62, var68);
         PoseStack var71 = var1.getMatrices();
         AABB var72 = var70;
         Render3DUtil.b(var71, var72, new Color(255, 220, 0, 230));
      }
   }

   private boolean D() {
      Screen var1 = mc.gui.screen();
      return var1 instanceof AbstractContainerScreen ? ChestStealer.a_bool_Component(((AbstractContainerScreen)var1).getTitle()) : false;
   }

   private void E() {
      if (!this.t.isEmpty()) {
         if (!this.ignoreCustomName.getValue() || !this.D() || mc.player.containerMenu instanceof ShulkerBoxMenu) {
            int var1 = this.w;
            int var2 = this.delay.getValue().intValue();
            if (var1 < var2) {
               int var3 = this.w + 1;
               this.w = var3;
            } else {
               this.w = 0;
               int var4 = 0;

               while (true) {
                  int var5 = var4;
                  int var6 = this.frequency.getValue().intValue();
                  if (var5 >= var6 || !this.M()) {
                     return;
                  }

                  var4++;
               }
            }
         }
      }
   }

   private void F() {
      this.C();
      boolean var1 = mc.player.hasContainerOpen();
      if (this.z == Rekit.RekitState.IDLE) {
         this.P = false;
         if (var1) {
            if (mc.player.containerMenu instanceof ShulkerBoxMenu) {
               this.E();
               return;
            }

            this.G();
            if (this.F == null) {
               this.E();
               return;
            }

            if (this.t.isEmpty()) {
               this.setEnabled(false);
               return;
            }

            this.z = Rekit.RekitState.FIND_SHULKER;
            this.A = 0;
            this.B = 0;
         }
      } else {
         Rekit.RekitState var2 = this.z;
         Rekit.RekitState var3 = Rekit.RekitState.FIND_SHULKER;
         if (var2 == var3) {
            if (var1) {
               if (this.L()) {
                  mc.player.closeContainer();
                  Rekit.RekitState var296 = Rekit.RekitState.IDLE;
                  this.z = var296;
               } else {
                  int var297 = mc.player.containerMenu.slots.size();
                  byte var298 = 36;
                  int var299 = var297 - var298;
                  if (var299 > 0) {
                     Rekit var300 = this;
                     int var301 = var299;
                     int var302 = var300.b_int_int(var301);
                     this.C = var302;
                     int var303 = this.C;
                     byte var304 = -1;
                     if (var303 == var304) {
                        String var305 = "Auto stopped: no shulker has the items this kit still needs.";
                        this.f(var305);
                        mc.player.closeContainer();
                        Rekit.RekitState var306 = Rekit.RekitState.IDLE;
                        this.z = var306;
                        boolean var307 = false;
                        this.setEnabled(var307);
                     } else {
                        Rekit.RekitState var308 = Rekit.RekitState.GRAB_SHULKER;
                        this.z = var308;
                        byte var309 = 0;
                        this.A = var309;
                     }
                  }
               }
            }
         } else {
            Rekit.RekitState var4 = this.z;
            Rekit.RekitState var5 = Rekit.RekitState.GRAB_SHULKER;
            if (var4 == var5) {
               if (var1) {
                  int var6 = mc.player.containerMenu.slots.size();
                  byte var7 = 36;
                  if (var6 - var7 > 0) {
                     AbstractContainerMenu var8 = mc.player.containerMenu;
                     int var9 = this.C;
                     ItemStack var10 = var8.getSlot(var9).getItem();
                     Rekit var11 = this;
                     ItemStack var12 = var10;
                     if (!var11.c_bool_ItemStack(var12)) {
                        this.z = Rekit.RekitState.FIND_SHULKER;
                     } else {
                        int var13 = this.I();
                        if (var13 != -1) {
                           MultiPlayerGameMode var14 = mc.gameMode;
                           int var15 = var8.containerId;
                           int var16 = this.C;
                           int var17 = var13;
                           ContainerInput var18 = ContainerInput.SWAP;
                           LocalPlayer var19 = mc.player;
                           var14.handleContainerInput(var15, var16, var17, var18, var19);
                        } else {
                           byte var20;
                           if (mc.player.getInventory().getSelectedSlot() == 0) {
                              var20 = 1;
                           } else {
                              var20 = 0;
                           }

                           byte var21 = var20;
                           MultiPlayerGameMode var22 = mc.gameMode;
                           int var23 = var8.containerId;
                           int var24 = this.C;
                           byte var25 = var21;
                           ContainerInput var26 = ContainerInput.SWAP;
                           LocalPlayer var27 = mc.player;
                           var22.handleContainerInput(var23, var24, var25, var26, var27);
                           Rekit var28 = this;
                           byte var29 = var21;
                           var28.N = var29;
                           Rekit var30 = this;
                           int var31 = this.C;
                           var30.O = var31;
                        }

                        this.z = Rekit.RekitState.PLACE_SHULKER;
                        this.A = 0;
                        this.G = false;
                        this.H = 0;
                     }
                  }
               }
            } else {
               Rekit.RekitState var32 = this.z;
               Rekit.RekitState var33 = Rekit.RekitState.PLACE_SHULKER;
               if (var32 == var33) {
                  if (var1) {
                     mc.player.closeContainer();
                  } else {
                     int var220 = this.A;
                     byte var221 = 1;
                     if (var220 < var221) {
                        Rekit var222 = this;
                        int var223 = var222.A + 1;
                        var222.A = var223;
                     } else {
                        int var224 = this.H();
                        byte var225 = -1;
                        if (var224 == var225) {
                           Rekit var226 = this;
                           Rekit.RekitState var227 = Rekit.RekitState.IDLE;
                           var226.z = var227;
                        } else {
                           int var228 = var224;
                           byte var229 = 8;
                           if (var228 > var229 && !this.G) {
                              Rekit var230 = this;
                              int var231 = var224;
                              var230.c_int_int(var231);
                              Rekit var232 = this;
                              boolean var233 = true;
                              var232.G = var233;
                           } else {
                              int var234 = var224;
                              byte var235 = 8;
                              if (var234 > var235) {
                                 Rekit var236 = this;
                                 int var237 = var236.H + 1;
                                 var236.H = var237;
                                 int var238 = this.H;
                                 byte var239 = 3;
                                 if (var238 > var239) {
                                    Rekit var240 = this;
                                    int var241 = var240.J + 1;
                                    var240.J = var241;
                                    Rekit var242 = this;
                                    Rekit.RekitState var243 = Rekit.RekitState.RETURN_SHULKER;
                                    var242.z = var243;
                                 } else {
                                    Rekit var294 = this;
                                    boolean var295 = false;
                                    var294.G = var295;
                                 }
                              } else {
                                 BlockPos var244 = this.K();
                                 if (var244 == null) {
                                    Rekit var245 = this;
                                    int var246 = var245.J + 1;
                                    var245.J = var246;
                                    Rekit var247 = this;
                                    Rekit.RekitState var248 = Rekit.RekitState.RETURN_SHULKER;
                                    var247.z = var248;
                                 } else {
                                    Rekit var249 = this;
                                    BlockPos var250 = var244;
                                    BlockHitResult var251 = var249.b_BlockHitResult_BlockPos(var250);
                                    if (var251 == null) {
                                       Rekit var252 = this;
                                       int var253 = var252.J + 1;
                                       var252.J = var253;
                                       Rekit var254 = this;
                                       Rekit.RekitState var255 = Rekit.RekitState.RETURN_SHULKER;
                                       var254.z = var255;
                                    } else {
                                       int var256 = this.H();
                                       if (var256 >= 0) {
                                          int var257 = var256;
                                          byte var258 = 8;
                                          if (var257 <= var258) {
                                             if (this.rotate.getValue()) {
                                                float[] var263 = RotationMathUtil.a(var251.getLocation());
                                                RotationManager var264 = Night.rotationManager;
                                                float var265 = var263[0];
                                                float var266 = var263[1];
                                                if (!var264.canRotate(var265, var266)) {
                                                   return;
                                                }
                                             }

                                             Night.rotationManager.beginBatch();

                                             boolean var279;
                                             try {
                                                boolean var278;
                                                label240: {
                                                   Rekit var267 = this;
                                                   int var268 = var256;
                                                   var267.d(var268);
                                                   Rekit var269 = this;
                                                   BlockPos var270 = var244;
                                                   if (var269.a_bool_BlockPos(var270)) {
                                                      MultiPlayerGameMode var271 = mc.gameMode;
                                                      LocalPlayer var272 = mc.player;
                                                      BlockHitResult var273 = var251;
                                                      InteractionResult var274 = var271.useItemOn(var272, InteractionHand.MAIN_HAND, var273);
                                                      Fail var275 = InteractionResult.FAIL;
                                                      if (var274 != var275) {
                                                         ClientLevel var276 = mc.level;
                                                         BlockPos var277 = var244;
                                                         if (!var276.getBlockState(var277).canBeReplaced()) {
                                                            var278 = true;
                                                            break label240;
                                                         }
                                                      }
                                                   }

                                                   var278 = false;
                                                }

                                                var279 = var278;
                                             } catch (Throwable var310) {
                                                Night.rotationManager.endBatch();
                                                throw var310;
                                             }

                                             Night.rotationManager.endBatch();
                                             if (!var279) {
                                                this.N();
                                                Rekit var281 = this;
                                                int var282 = var281.J + 1;
                                                var281.J = var282;
                                                Rekit var283 = this;
                                                Rekit.RekitState var284 = Rekit.RekitState.RETURN_SHULKER;
                                                var283.z = var284;
                                                return;
                                             }

                                             Rekit var285 = this;
                                             BlockPos var286 = var244;
                                             var285.D = var286;
                                             Rekit var287 = this;
                                             long var288 = System.currentTimeMillis();
                                             var287.E = var288;
                                             Rekit var290 = this;
                                             Rekit.RekitState var291 = Rekit.RekitState.OPEN_SHULKER;
                                             var290.z = var291;
                                             Rekit var292 = this;
                                             byte var293 = 0;
                                             var292.A = var293;
                                             return;
                                          }
                                       }

                                       Rekit var259 = this;
                                       int var260 = var259.J + 1;
                                       var259.J = var260;
                                       Rekit var261 = this;
                                       Rekit.RekitState var262 = Rekit.RekitState.RETURN_SHULKER;
                                       var261.z = var262;
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               } else {
                  Rekit.RekitState var34 = this.z;
                  Rekit.RekitState var35 = Rekit.RekitState.OPEN_SHULKER;
                  if (var34 == var35) {
                     if (var1) {
                        this.N();
                        Rekit.RekitState var36 = Rekit.RekitState.PULL_ITEMS;
                        this.z = var36;
                        byte var37 = 0;
                        this.A = var37;
                        byte var38 = 0;
                        this.B = var38;
                        byte var39 = 0;
                        this.J = var39;
                     } else {
                        label265: {
                           if (this.A != 0) {
                              int var40 = this.A;
                              byte var41 = 4;
                              if (var40 % var41 != 0) {
                                 break label265;
                              }
                           }

                           Vec3 var43 = Vec3.atCenterOf(this.D);
                           BlockPos var44 = this.D;
                           BlockHitResult var42 = new BlockHitResult(var43, Direction.UP, var44, false);
                           BlockHitResult var45 = var42;
                           MultiPlayerGameMode var46 = mc.gameMode;
                           LocalPlayer var47 = mc.player;
                           BlockHitResult var48 = var45;
                           var46.useItemOn(var47, InteractionHand.MAIN_HAND, var48);
                        }

                        int var49 = this.A + 1;
                        this.A = var49;
                        int var50 = this.A;
                        byte var51 = 60;
                        if (var50 > var51) {
                           this.N();
                           Rekit.RekitState var52 = Rekit.RekitState.BREAK_SHULKER;
                           this.z = var52;
                           byte var53 = 0;
                           this.A = var53;
                        }
                     }
                  } else {
                     Rekit.RekitState var54 = this.z;
                     Rekit.RekitState var55 = Rekit.RekitState.PULL_ITEMS;
                     if (var54 == var55) {
                        if (!var1) {
                           Rekit var56 = this;
                           Rekit.RekitState var57 = Rekit.RekitState.BREAK_SHULKER;
                           var56.z = var57;
                           Rekit var58 = this;
                           byte var59 = 0;
                           var58.A = var59;
                        } else if (this.t.isEmpty()) {
                           Rekit var60 = this;
                           Rekit.RekitState var61 = Rekit.RekitState.BREAK_SHULKER;
                           var60.z = var61;
                           Rekit var62 = this;
                           byte var63 = 0;
                           var62.A = var63;
                        } else {
                           int var64 = this.w;
                           int var65 = this.delay.getValue().intValue();
                           if (var64 < var65) {
                              Rekit var66 = this;
                              int var67 = var66.w + 1;
                              var66.w = var67;
                           } else {
                              Rekit var68 = this;
                              byte var69 = 0;
                              var68.w = var69;
                              boolean var70 = false;
                              int var71 = mc.player.containerMenu.slots.size();
                              byte var72 = 36;
                              if (var71 - var72 > 0) {
                                 int var73 = 0;

                                 while (true) {
                                    int var74 = var73;
                                    int var75 = this.frequency.getValue().intValue();
                                    if (var74 >= var75 || !this.M()) {
                                       break;
                                    }

                                    var73++;
                                    var70 = true;
                                 }
                              }

                              if (var70) {
                                 Rekit var76 = this;
                                 byte var77 = 0;
                                 var76.B = var77;
                              } else {
                                 Rekit var78 = this;
                                 int var79 = var78.B + 1;
                                 var78.B = var79;
                              }

                              int var80 = this.B;
                              byte var81 = 5;
                              if (var80 >= var81) {
                                 Rekit var82 = this;
                                 Rekit.RekitState var83 = Rekit.RekitState.BREAK_SHULKER;
                                 var82.z = var83;
                                 Rekit var84 = this;
                                 byte var85 = 0;
                                 var84.A = var85;
                              }
                           }
                        }
                     } else {
                        Rekit.RekitState var86 = this.z;
                        Rekit.RekitState var87 = Rekit.RekitState.BREAK_SHULKER;
                        if (var86 == var87) {
                           if (var1) {
                              mc.player.closeContainer();
                           } else {
                              int var192 = this.A;
                              byte var193 = 2;
                              if (var192 < var193) {
                                 Rekit var194 = this;
                                 int var195 = var194.A + 1;
                                 var194.A = var195;
                              } else if (this.D != null && mc.level != null) {
                                 ClientLevel var198 = mc.level;
                                 BlockPos var199 = this.D;
                                 if (var198.getBlockState(var199).canBeReplaced()) {
                                    int var200 = this.H();
                                    byte var201 = -1;
                                    if (var200 == var201) {
                                       int var202 = this.A;
                                       byte var203 = 100;
                                       if (var202 < var203) {
                                          Rekit var204 = this;
                                          int var205 = var204.A + 1;
                                          var204.A = var205;
                                          return;
                                       }
                                    }

                                    Rekit var216 = this;
                                    Rekit.RekitState var217 = Rekit.RekitState.RETURN_SHULKER;
                                    var216.z = var217;
                                    Rekit var218 = this;
                                    byte var219 = 0;
                                    var218.A = var219;
                                 } else {
                                    int var206 = this.A;
                                    short var207 = 200;
                                    if (var206 > var207) {
                                       Rekit var208 = this;
                                       Rekit.RekitState var209 = Rekit.RekitState.RETURN_SHULKER;
                                       var208.z = var209;
                                       Rekit var210 = this;
                                       byte var211 = 0;
                                       var210.A = var211;
                                    } else {
                                       Rekit var212 = this;
                                       BlockPos var213 = this.D;
                                       var212.c_void_BlockPos(var213);
                                       Rekit var214 = this;
                                       int var215 = var214.A + 1;
                                       var214.A = var215;
                                    }
                                 }
                              } else {
                                 Rekit var196 = this;
                                 Rekit.RekitState var197 = Rekit.RekitState.IDLE;
                                 var196.z = var197;
                              }
                           }
                        } else {
                           Rekit.RekitState var88 = this.z;
                           Rekit.RekitState var89 = Rekit.RekitState.RETURN_SHULKER;
                           if (var88 == var89) {
                              if (this.F == null) {
                                 Rekit var90 = this;
                                 Rekit.RekitState var91 = Rekit.RekitState.IDLE;
                                 var90.z = var91;
                                 return;
                              }

                              if (!var1) {
                                 int var92 = this.A;
                                 byte var93 = 2;
                                 if (var92 < var93) {
                                    Rekit var94 = this;
                                    int var95 = var94.A + 1;
                                    var94.A = var95;
                                    return;
                                 }

                                 int var96 = this.A;
                                 byte var97 = 2;
                                 if (var96 == var97) {
                                    Vec3 var99 = Vec3.atCenterOf(this.F);
                                    BlockPos var100 = this.F;
                                    boolean var101 = false;
                                    BlockHitResult var98 = new BlockHitResult(var99, Direction.UP, var100, var101);
                                    BlockHitResult var102 = var98;
                                    MultiPlayerGameMode var103 = mc.gameMode;
                                    LocalPlayer var104 = mc.player;
                                    BlockHitResult var105 = var102;
                                    var103.useItemOn(var104, InteractionHand.MAIN_HAND, var105);
                                 }

                                 Rekit var106 = this;
                                 int var107 = var106.A + 1;
                                 var106.A = var107;
                                 int var108 = this.A;
                                 byte var109 = 40;
                                 if (var108 > var109) {
                                    Rekit var110 = this;
                                    Rekit.RekitState var111 = Rekit.RekitState.IDLE;
                                    var110.z = var111;
                                 }

                                 return;
                              }

                              Rekit var112 = this;
                              byte var113 = 0;
                              var112.A = var113;
                              int var114 = this.H();
                              byte var115 = -1;
                              if (var114 == var115) {
                                 if (this.D != null && mc.level != null) {
                                    ClientLevel var116 = mc.level;
                                    BlockPos var117 = this.D;
                                    if (!var116.getBlockState(var117).canBeReplaced()) {
                                       Rekit var118 = this;
                                       Rekit.RekitState var119 = Rekit.RekitState.BREAK_SHULKER;
                                       var118.z = var119;
                                       Rekit var120 = this;
                                       byte var121 = 0;
                                       var120.A = var121;
                                       return;
                                    }
                                 }

                                 Rekit var188 = this;
                                 Rekit.RekitState var189 = Rekit.RekitState.FIND_SHULKER;
                                 var188.z = var189;
                                 Rekit var190 = this;
                                 byte var191 = 0;
                                 var190.A = var191;
                                 return;
                              }

                              int var122 = mc.player.containerMenu.slots.size();
                              byte var123 = 36;
                              int var124 = var122 - var123;
                              if (var124 <= 0) {
                                 return;
                              }

                              AbstractContainerMenu var125 = mc.player.containerMenu;
                              int var126 = this.N;
                              byte var127 = -1;
                              if (var126 != var127) {
                                 Rekit var128 = this;
                                 int var129 = var124;
                                 int var130 = var114;
                                 int var131 = var128.a_int_int_int(var129, var130);
                                 Rekit var132 = this;
                                 int var133 = var125.containerId;
                                 int var134 = var131;
                                 var132.a(var133, var134, 0, ContainerInput.PICKUP);
                                 Rekit var135 = this;
                                 int var136 = var125.containerId;
                                 int var137 = this.O;
                                 var135.a(var136, var137, 0, ContainerInput.PICKUP);
                                 Rekit var138 = this;
                                 int var139 = var124;
                                 int var140 = this.N;
                                 int var141 = var138.a_int_int_int(var139, var140);
                                 Rekit var142 = this;
                                 int var143 = var125.containerId;
                                 int var144 = var141;
                                 ContainerInput var145 = ContainerInput.PICKUP;
                                 var142.a(var143, var144, 0, var145);
                                 Rekit var146 = this;
                                 byte var147 = -1;
                                 var146.N = var147;
                                 Rekit var148 = this;
                                 byte var149 = -1;
                                 var148.O = var149;
                              } else {
                                 byte var150 = -1;
                                 byte var151 = 0;

                                 while (true) {
                                    byte var152 = var151;
                                    int var153 = var124;
                                    if (var152 >= var153) {
                                       break;
                                    }

                                    AbstractContainerMenu var154 = var125;
                                    byte var155 = var151;
                                    if (var154.getSlot(var155).getItem().isEmpty()) {
                                       var150 = var151;
                                       break;
                                    }

                                    var151 += 1;
                                 }

                                 byte var156 = var150;
                                 byte var157 = -1;
                                 if (var156 == var157) {
                                    Rekit var158 = this;
                                    Rekit.RekitState var159 = Rekit.RekitState.IDLE;
                                    var158.z = var159;
                                    return;
                                 }

                                 Rekit var160 = this;
                                 int var161 = var124;
                                 int var162 = var114;
                                 int var163 = var160.a_int_int_int(var161, var162);
                                 Rekit var164 = this;
                                 int var165 = var125.containerId;
                                 int var166 = var163;
                                 byte var167 = var150;
                                 var164.a_void_int_int_int(var165, var166, var167);
                              }

                              Rekit var168 = this;
                              Object var169 = null;
                              var168.D = (BlockPos)var169;
                              Rekit var170 = this;
                              byte var171 = -1;
                              var170.C = var171;
                              int var172 = this.J;
                              byte var173 = 3;
                              if (var172 >= var173) {
                                 Rekit var174 = this;
                                 String var175 = "Auto stopped: could not place/open a shulker after " + this.J + " attempts.";
                                 var174.f(var175);
                                 mc.player.closeContainer();
                                 Rekit var176 = this;
                                 Rekit.RekitState var177 = Rekit.RekitState.IDLE;
                                 var176.z = var177;
                                 Rekit var178 = this;
                                 boolean var179 = false;
                                 var178.setEnabled(var179);
                                 Rekit var180 = this;
                                 byte var181 = 0;
                                 var180.J = var181;
                              } else if (this.L()) {
                                 mc.player.closeContainer();
                                 Rekit var182 = this;
                                 Rekit.RekitState var183 = Rekit.RekitState.IDLE;
                                 var182.z = var183;
                              } else {
                                 Rekit var184 = this;
                                 Rekit.RekitState var185 = Rekit.RekitState.FIND_SHULKER;
                                 var184.z = var185;
                              }

                              Rekit var186 = this;
                              byte var187 = 0;
                              var186.A = var187;
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private void G() {
      if (mc.hitResult instanceof BlockHitResult var2) {
         ClientLevel var3 = mc.level;
         BlockPos var4 = var2.getBlockPos();
         if (var3.getBlockState(var4).is(Blocks.ENDER_CHEST)) {
            BlockPos var5 = var2.getBlockPos();
            this.F = var5;
            return;
         }
      }

      BlockPos var6 = mc.player.blockPosition();

      for (int var7 = -3; var7 <= 3; var7++) {
         for (int var8 = -1; var8 <= 3; var8++) {
            for (int var9 = -3; var9 <= 3; var9++) {
               BlockPos var10 = var6;
               int var11 = var7;
               int var12 = var8;
               int var13 = var9;
               BlockPos var14 = var10.offset(var11, var12, var13);
               ClientLevel var15 = mc.level;
               BlockPos var16 = var14;
               if (var15.getBlockState(var16).is(Blocks.ENDER_CHEST)) {
                  BlockPos var17 = var14;
                  this.F = var17;
                  return;
               }
            }
         }
      }
   }

   private int b_int_int(int var1) {
      AbstractContainerMenu var2 = mc.player.containerMenu;
      HashSet var3 = new HashSet();

      for (Entry var5 : (Iterable<Entry>)(Object)this.t.entrySet()) {
         Inventory var6 = mc.player.getInventory();
         int var7 = (Integer)var5.getKey();
         ItemStack var8 = var6.getItem(var7);
         if (var5.getValue() != null && ((Rekit.KitSlotEntry)var5.getValue()).a != null && !((Rekit.KitSlotEntry)var5.getValue()).a.isEmpty()) {
            ItemStack var9 = var8;
            Rekit.KitSlotEntry var10 = (Rekit.KitSlotEntry)var5.getValue();
            if (!this.a_bool_ItemStack_KitSlotEntry(var9, var10)) {
               Identifier var11 = Identifier.tryParse(((Rekit.KitSlotEntry)var5.getValue()).a);
               if (var11 != null) {
                  Identifier var12 = var11;
                  Item var13 = (Item)BuiltInRegistries.ITEM.getValue(var12);
                  if (var13 != null) {
                     HashSet var14 = var3;
                     Item var15 = var13;
                     var14.add(var15);
                  }
               }
            }
         }
      }

      int var16 = -1;
      int var17 = -1;

      for (int var18 = 0; var18 < var1; var18++) {
         AbstractContainerMenu var19 = var2;
         int var20 = var18;
         ItemStack var21 = var19.getSlot(var20).getItem();
         if (this.c_bool_ItemStack(var21)) {
            int var22 = 0;
            ItemContainerContents var23 = (ItemContainerContents)var21.get(DataComponents.CONTAINER);
            if (var23 != null) {
               Stream var24 = var23.nonEmptyItemCopyStream();

               for (ItemStack var26 : (Iterable<ItemStack>)var24::iterator) {
                  HashSet var27 = var3;
                  Item var28 = var26.getItem();
                  if (var27.contains(var28)) {
                     int var29 = var22;
                     byte var30;
                     if (var26.getMaxStackSize() == 1) {
                        var30 = 100;
                     } else {
                        var30 = 1;
                     }

                     var22 = var29 + var30;
                  }
               }
            }

            if (this.considerShulkerName.getValue() && !this.u.isEmpty() && var21.has(DataComponents.CUSTOM_NAME)) {
               String var31 = g(var21.getHoverName().getString());
               String var32 = g(this.u);
               if (var31.equalsIgnoreCase(var32)) {
                  var22 += 1000000;
               }
            }

            int var33 = var22;
            int var34 = var17;
            if (var33 > var34) {
               var17 = var22;
               var16 = var18;
            }
         }
      }

      return var17 <= 0 ? -1 : var16;
   }

   private int c_int_int(int var1) {
      if (var1 >= 0 && var1 <= 8) {
         return var1;
      }

      int var2 = mc.player.getInventory().getSelectedSlot();
      MultiPlayerGameMode var3 = mc.gameMode;
      int var4 = mc.player.inventoryMenu.containerId;
      int var5 = this.e_int_int(var1);
      int var6 = var2;
      LocalPlayer var7 = mc.player;
      var3.handleContainerInput(var4, var5, var6, ContainerInput.SWAP, var7);
      return var2;
   }

   private int H() {
      for (int var1 = 0; var1 < 36; var1++) {
         Inventory var2 = mc.player.getInventory();
         int var3 = var1;
         ItemStack var4 = var2.getItem(var3);
         if (this.c_bool_ItemStack(var4)) {
            return var1;
         }
      }

      return -1;
   }

   private int I() {
      for (int var1 = 0; var1 <= 8; var1++) {
         Inventory var2 = mc.player.getInventory();
         int var3 = var1;
         if (var2.getItem(var3).isEmpty()) {
            return var1;
         }
      }

      return -1;
   }

   private Player J() {
      if (mc.player.getLastHurtByMob() instanceof Player var2) {
         long var3 = mc.level.getGameTime();
         long var5 = mc.player.getLastHurtByMobTimestamp();
         long var7 = var3 - var5;
         long var9 = 100L;
         Player var11;
         if (var7 <= var9) {
            var11 = var2;
         } else {
            var11 = null;
         }

         return var11;
      } else {
         return null;
      }
   }

   private boolean a_bool_BlockPos(BlockPos var1) {
      ClientLevel var2 = mc.level;
      Entity var3 = (Entity)null;
      return var2.getEntities(var3, new AABB(var1), Entity::isAlive).isEmpty();
   }

   private BlockPos K() {
      BlockPos var1 = mc.player.blockPosition();
      Player var2 = this.J();
      int var3 = (int)Math.ceil(mc.player.blockInteractionRange());
      BlockPos var4 = null;
      double var5 = Double.MAX_VALUE;
      int var7 = -var3;

      while (true) {
         int var8 = var7;
         int var9 = var3;
         if (var8 > var9) {
            return var4;
         }

         int var10 = -var3;

         while (true) {
            int var11 = var10;
            int var12 = var3;
            if (var11 > var12) {
               var7++;
               break;
            }

            BlockPos var13 = var1;
            int var14 = var7;
            int var15 = var10;
            BlockPos var16 = var13.offset(var14, 0, var15);
            BlockPos var17 = var1;
            double var18 = var16.distSqr(var17);
            double var20 = var3;
            double var22 = var3;
            double var24 = var20 * var22;
            if (!(var18 > var24)) {
               BlockPos var26 = var16.below();
               BlockPos var27 = this.F;
               if (!var26.equals(var27)) {
                  BlockPos var28 = var16;
                  if (this.a_bool_BlockPos(var28)) {
                     ClientLevel var29 = mc.level;
                     BlockPos var30 = var16;
                     if (var29.getBlockState(var30).canBeReplaced()) {
                        ClientLevel var31 = mc.level;
                        BlockPos var32 = var16.below();
                        if (!var31.getBlockState(var32).canBeReplaced()) {
                           ClientLevel var33 = mc.level;
                           BlockPos var34 = var16.below();
                           if (!var33.getBlockState(var34).is(Blocks.AIR)) {
                              ClientLevel var35 = mc.level;
                              BlockPos var36 = var16.above();
                              if (var35.getBlockState(var36).canBeReplaced()) {
                                 double var39;
                                 if (var2 != null) {
                                    BlockPos var37 = var16;
                                    BlockPos var38 = var2.blockPosition();
                                    var39 = -var37.distSqr(var38);
                                 } else {
                                    int var41 = var7;
                                    int var42 = var7;
                                    int var43 = var41 * var42;
                                    int var44 = var10;
                                    int var45 = var10;
                                    int var46 = var44 * var45;
                                    var39 = var43 + var46;
                                 }

                                 double var47 = var39;
                                 double var49 = var5;
                                 if (var47 < var49) {
                                    var5 = var47;
                                    var4 = var16;
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }

            var10++;
         }
      }
   }

   private BlockHitResult b_BlockHitResult_BlockPos(BlockPos var1) {
      boolean var2 = this.strictDirection.getValue();
      Direction var3 = BlockInteractionUtil.b_Direction_BlockPos_List_bool(var1, null, var2);
      if (var3 == null) {
         return null;
      }

      Direction var4 = var3;
      BlockPos var5 = var1.relative(var4);
      Vec3 var6 = Vec3.atCenterOf(var5);
      double var7 = -var3.getStepX();
      double var9 = 0.48;
      double var11 = var7 * var9;
      double var13 = -var3.getStepY();
      double var15 = 0.48;
      double var17 = var13 * var15;
      double var19 = -var3.getStepZ();
      double var21 = 0.48;
      double var23 = var19 * var21;
      Vec3 var25 = var6.add(var11, var17, var23);
      Direction var26 = var3.getOpposite();
      BlockPos var27 = var5;
      return new BlockHitResult(var25, var26, var27, false);
   }

   private boolean L() {
      if (this.t.isEmpty()) {
         return true;
      }

      for (Entry var2 : (Iterable<Entry>)(Object)this.t.entrySet()) {
         Inventory var3 = mc.player.getInventory();
         int var4 = (Integer)var2.getKey();
         ItemStack var5 = var3.getItem(var4);
         Rekit.KitSlotEntry var6 = (Rekit.KitSlotEntry)var2.getValue();
         if (!this.a_bool_ItemStack_KitSlotEntry(var5, var6)) {
            return false;
         }
      }

      return true;
   }

   private boolean M() {
      AbstractContainerMenu var1 = mc.player.containerMenu;
      int var2 = var1.slots.size() - 36;
      if (var2 <= 0) {
         return false;
      }

      if (var1.getCarried().isEmpty()) {
         this.L = 0;

         for (int var18 = 0; var18 < 36; var18++) {
            Map var19 = this.t;
            Integer var20 = var18;
            Rekit.KitSlotEntry var21 = (Rekit.KitSlotEntry)var19.get(var20);
            if (var21 != null) {
               int var22 = var2;
               int var23 = var18;
               int var24 = this.a_int_int_int(var22, var23);
               AbstractContainerMenu var25 = var1;
               int var26 = var24;
               ItemStack var27 = var25.getSlot(var26).getItem();
               Rekit.KitSlotEntry var28 = var21;
               if (!this.a_bool_ItemStack_KitSlotEntry(var27, var28)) {
                  ItemStack var29 = var27;
                  if (!this.c_bool_ItemStack(var29)) {
                     AbstractContainerMenu var50 = var1;
                     int var51 = var2;
                     Rekit.KitSlotEntry var52 = var21;
                     int var53 = this.a_int_AbstractContainerMenu_int_KitSlotEntry(var50, var51, var52);
                     if (var53 != -1) {
                        int var54 = var1.containerId;
                        int var55 = var53;
                        int var56 = var24;
                        this.a_void_int_int_int(var54, var55, var56);
                        return true;
                     }
                  }
               } else {
                  int var30 = var27.getCount();
                  int var31 = var27.getMaxStackSize();
                  if (var30 < var31) {
                     AbstractContainerMenu var32 = var1;
                     int var33 = var2;
                     ItemStack var34 = var27;
                     int var35 = this.a_int_AbstractContainerMenu_int_ItemStack(var32, var33, var34);
                     if (var35 != -1) {
                        int var36 = var1.containerId;
                        int var37 = var35;
                        int var38 = var24;
                        this.a_void_int_int_int(var36, var37, var38);
                        return true;
                     }

                     AbstractContainerMenu var39 = var1;
                     int var40 = var2;
                     Rekit.KitSlotEntry var41 = var21;
                     int var42 = this.a_int_AbstractContainerMenu_int_KitSlotEntry(var39, var40, var41);
                     if (var42 != -1) {
                        AbstractContainerMenu var43 = var1;
                        int var44 = var42;
                        int var45 = var43.getSlot(var44).getItem().getCount();
                        int var46 = var27.getCount();
                        if (var45 > var46) {
                           int var47 = var1.containerId;
                           int var48 = var42;
                           int var49 = var24;
                           this.a_void_int_int_int(var47, var48, var49);
                           return true;
                        }
                     }
                  }
               }
            }
         }

         for (int var57 = 0; var57 < 36; var57++) {
            Map var58 = this.t;
            Integer var59 = var57;
            Rekit.KitSlotEntry var60 = (Rekit.KitSlotEntry)var58.get(var59);
            if (var60 != null) {
               int var61 = var2;
               int var62 = var57;
               int var63 = this.a_int_int_int(var61, var62);
               AbstractContainerMenu var64 = var1;
               int var65 = var63;
               ItemStack var66 = var64.getSlot(var65).getItem();
               if (this.c_bool_ItemStack(var66)) {
                  AbstractContainerMenu var67 = var1;
                  int var68 = var2;
                  Rekit.KitSlotEntry var69 = var60;
                  if (!this.b(var67, var68, var69)) {
                     AbstractContainerMenu var70 = var1;
                     int var71 = var2;
                     Rekit.KitSlotEntry var72 = var60;
                     int var73 = this.a_int_AbstractContainerMenu_int_KitSlotEntry(var70, var71, var72);
                     AbstractContainerMenu var74 = var1;
                     int var75 = var2;
                     int var76 = this.a_int_AbstractContainerMenu_int(var74, var75);
                     if (var73 != -1 && var76 != -1) {
                        int var77 = var1.containerId;
                        int var78 = var73;
                        int var79 = var76;
                        this.a_void_int_int_int(var77, var78, var79);
                        return true;
                     }
                  }
               }
            }
         }

         return false;
      } else {
         int var3 = this.L + 1;
         this.L = var3;
         if (this.L < 20) {
            return false;
         }

         this.L = 0;
         int var4 = -1;
         int var5 = 0;

         while (true) {
            int var6 = var5;
            int var7 = var2;
            if (var6 >= var7) {
               break;
            }

            AbstractContainerMenu var8 = var1;
            int var9 = var5;
            if (var8.getSlot(var9).getItem().isEmpty()) {
               var4 = var5;
               break;
            }

            var5++;
         }

         if (var4 == -1) {
            for (int var10 = 0; var10 < 36; var10++) {
               int var11 = var2;
               int var12 = var10;
               int var13 = this.a_int_int_int(var11, var12);
               AbstractContainerMenu var14 = var1;
               int var15 = var13;
               if (var14.getSlot(var15).getItem().isEmpty()) {
                  var4 = var13;
                  break;
               }
            }
         }

         if (var4 != -1) {
            int var16 = var1.containerId;
            int var17 = var4;
            this.a(var16, var17, 0, ContainerInput.PICKUP);
            return true;
         } else {
            return false;
         }
      }
   }

   private void a_void_int_int_int(int var1, int var2, int var3) {
      this.a(var1, var2, 0, ContainerInput.PICKUP);
      this.a(var1, var3, 0, ContainerInput.PICKUP);
      this.a(var1, var2, 0, ContainerInput.PICKUP);
   }

   private void a(int var1, int var2, int var3, ContainerInput var4) {
      v = System.currentTimeMillis();
      MultiPlayerGameMode var5 = mc.gameMode;
      LocalPlayer var6 = mc.player;
      var5.handleContainerInput(var1, var2, var3, var4, var6);
   }

   private void d(int var1) {
      int var2 = mc.player.getInventory().getSelectedSlot();
      this.R = var2;
      mc.player.getInventory().setSelectedSlot(var1);
      ((ClientPlayerInteractionManagerAccessor)mc.gameMode).invokeSyncSelectedSlot();
   }

   private void N() {
      if (this.R != -1) {
         Inventory var1 = mc.player.getInventory();
         int var2 = this.R;
         var1.setSelectedSlot(var2);
         ((ClientPlayerInteractionManagerAccessor)mc.gameMode).invokeSyncSelectedSlot();
         this.R = -1;
      }
   }

   private void c_void_BlockPos(BlockPos var1) {
      Direction var2 = Direction.UP;
      SpeedMine var3 = Night.moduleManager.getModule(SpeedMine.class);
      boolean var4;
      if (var3 != null && var3.farReach.getValue()) {
         var4 = true;
      } else {
         var4 = false;
      }

      boolean var5 = var4;
      if (this.rotate.getValue()) {
         Direction var6 = var2;
         float[] var7 = RotationMathUtil.a(BlockInteractionUtil.b_Vec3_BlockPos_Direction(var1, var6));
         float var8 = var7[0];
         float var9 = var7[1];
         if (!Night.rotationManager.canRotate(var8, var9)) {
            return;
         }
      }

      Night.rotationManager.beginBatch();

      try {
         ClientPacketListener var10 = mc.getConnection();
         Direction var11 = var2;
         var10.send(new ServerboundPlayerActionPacket(Action.START_DESTROY_BLOCK, var1, var11));
         if (var5) {
            BlockPos var12 = var1.below(2000);
            ClientPacketListener var13 = mc.getConnection();
            BlockPos var14 = var12;
            Direction var15 = BlockInteractionUtil.c(var12, true);
            var13.send(new ServerboundPlayerActionPacket(Action.START_DESTROY_BLOCK, var14, var15));
         }

         ClientPacketListener var16 = mc.getConnection();
         Direction var17 = var2;
         var16.send(new ServerboundPlayerActionPacket(Action.STOP_DESTROY_BLOCK, var1, var17));
         ClientPacketListener var18 = mc.getConnection();
         Packet var19 = PacketAndRenderUtil.a_Packet_InteractionHand(InteractionHand.MAIN_HAND);
         var18.send(var19);
      } catch (Throwable var21) {
         Night.rotationManager.endBatch();
         throw var21;
      }

      Night.rotationManager.endBatch();
   }

   private boolean a_bool_ItemStack_KitSlotEntry(ItemStack var1, Rekit.KitSlotEntry var2) {
      if (var2 == null) {
         return true;
      }

      if (var1.isEmpty()) {
         return false;
      }

      if (var2.a != null && !var2.a.isEmpty()) {
         Identifier var3 = Identifier.tryParse(var2.a);
         if (var3 == null) {
            return false;
         }

         Identifier var4 = var3;
         Item var5 = (Item)BuiltInRegistries.ITEM.getValue(var4);
         if (var5 != null) {
            Item var6 = var1.getItem();
            Item var7 = var5;
            if (var6 == var7) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private int a_int_AbstractContainerMenu_int_KitSlotEntry(AbstractContainerMenu var1, int var2, Rekit.KitSlotEntry var3) {
      if (var3 != null && var3.a != null && !var3.a.isEmpty()) {
         Identifier var4 = Identifier.tryParse(var3.a);
         if (var4 == null) {
            return -1;
         }

         Identifier var5 = var4;
         Item var6 = (Item)BuiltInRegistries.ITEM.getValue(var5);
         if (var6 == null) {
            return -1;
         }

         int var7 = -1;
         long var8 = -1L;

         for (int var10 = 0; var10 < var2; var10++) {
            int var11 = var10;
            ItemStack var12 = var1.getSlot(var11).getItem();
            if (!var12.isEmpty()) {
               Item var13 = var12.getItem();
               Item var14 = var6;
               if (var13 == var14) {
                  ItemStack var15 = var12;
                  long var16 = this.a_long_ItemStack(var15);
                  long var18 = var8;
                  if (var16 > var18) {
                     var8 = var16;
                     var7 = var10;
                  }
               }
            }
         }

         return var7;
      } else {
         return -1;
      }
   }

   private long a_long_ItemStack(ItemStack var1) {
      if (var1.getMaxStackSize() > 1) {
         return var1.getCount();
      }

      long var2 = 0L;
      String var4 = this.b_String_ItemStack(var1);
      if (var4 != null) {
         String var5 = var4;
         if (this.a_bool_ItemStack_String(var1, var5)) {
            long var6 = var2;
            long var8 = 1000000L;
            var2 = var6 + var8;
         }
      }

      if (var1.isDamageableItem()) {
         long var10 = var2;
         int var12 = var1.getMaxDamage();
         int var13 = var1.getDamageValue();
         long var14 = var12 - var13;
         var2 = var10 + var14;
      }

      return var2;
   }

   private String b_String_ItemStack(ItemStack var1) {
      Item var2 = var1.getItem();
      String var3 = BuiltInRegistries.ITEM.getKey(var2).getPath();
      if (var3.endsWith("_pickaxe")) {
         String var4;
         if (this.pickaxe.getValue().equalsIgnoreCase("SilkTouch")) {
            var4 = "silk_touch";
         } else {
            var4 = "efficiency";
         }

         return var4;
      } else {
         String var5;
         if (var3.endsWith("_helmet") || var3.equals("turtle_helmet")) {
            var5 = this.helmet.getValue();
         } else if (var3.endsWith("_chestplate")) {
            var5 = this.chestplate.getValue();
         } else if (var3.endsWith("_leggings")) {
            var5 = this.leggings.getValue();
         } else {
            if (!var3.endsWith("_boots")) {
               return null;
            }

            var5 = this.boots.getValue();
         }

         String var6;
         if (var5.equalsIgnoreCase("Blast")) {
            var6 = "blast_protection";
         } else {
            var6 = "protection";
         }

         return var6;
      }
   }

   private boolean a_bool_ItemStack_String(ItemStack var1, String var2) {
      ItemEnchantments var3 = (ItemEnchantments)var1.get(DataComponents.ENCHANTMENTS);
      if (var3 != null && !var3.isEmpty()) {
         Identifier var4 = Identifier.tryParse("minecraft:" + var2);
         if (var4 == null) {
            return false;
         }

         for (Holder var6 : var3.keySet()) {
            Identifier var7 = var4;
            if (var6.is(var7)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private int a_int_AbstractContainerMenu_int_ItemStack(AbstractContainerMenu var1, int var2, ItemStack var3) {
      for (int var4 = 0; var4 < var2; var4++) {
         int var5 = var4;
         ItemStack var6 = var1.getSlot(var5).getItem();
         if (!var6.isEmpty() && ItemStack.isSameItemSameComponents(var6, var3)) {
            return var4;
         }
      }

      return -1;
   }

   private int a_int_int_int(int var1, int var2) {
      if (var2 >= 0 && var2 <= 8) {
         return var1 + 27 + var2;
      } else if (var2 >= 9 && var2 <= 35) {
         int var3 = var2 - 9;
         return var1 + var3;
      } else {
         return -1;
      }
   }

   private int e_int_int(int var1) {
      if (var1 >= 0 && var1 <= 8) {
         return 36 + var1;
      } else {
         return var1 >= 9 && var1 <= 35 ? var1 : -1;
      }
   }

   private static String g(String var0) {
      return Normalizer.normalize(var0, Form.NFKC).trim();
   }

   private boolean c_bool_ItemStack(ItemStack var1) {
      if (!var1.isEmpty()) {
         Item var2 = var1.getItem();
         if (var2 instanceof BlockItem && ((BlockItem)var2).getBlock() instanceof ShulkerBoxBlock) {
            return true;
         }
      }

      return false;
   }

   private boolean b(AbstractContainerMenu var1, int var2, Rekit.KitSlotEntry var3) {
      if (var3 != null && var3.a != null && !var3.a.isEmpty()) {
         Identifier var4 = Identifier.tryParse(var3.a);
         if (var4 == null) {
            return false;
         }

         Identifier var5 = var4;
         Item var6 = (Item)BuiltInRegistries.ITEM.getValue(var5);
         if (var6 == null) {
            return false;
         }

         for (int var7 = 0; var7 < 36; var7++) {
            Map var8 = this.t;
            Integer var9 = var7;
            if (var8.get(var9) == null) {
               int var10 = var7;
               int var11 = this.a_int_int_int(var2, var10);
               Item var12 = var1.getSlot(var11).getItem().getItem();
               Item var13 = var6;
               if (var12 == var13) {
                  return true;
               }
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private int a_int_AbstractContainerMenu_int(AbstractContainerMenu var1, int var2) {
      for (int var3 = 0; var3 < 36; var3++) {
         Map var4 = this.t;
         Integer var5 = var3;
         if (var4.get(var5) == null) {
            int var6 = var3;
            int var7 = this.a_int_int_int(var2, var6);
            if (var1.getSlot(var7).getItem().isEmpty()) {
               return var7;
            }
         }
      }

      return -1;
   }

   @Generated
   public Map<Integer, Rekit.KitSlotEntry> z() {
      return this.t;
   }

   @Generated
   public String A() {
      return this.u;
   }

   public static class KitSlotEntry {
      @SerializedName("id")
      public String a;
      @SerializedName("name")
      public String b;
      @SerializedName("maxCount")
      public int c;
   }

   enum RekitState {
      IDLE,
      FIND_SHULKER,
      GRAB_SHULKER,
      PLACE_SHULKER,
      OPEN_SHULKER,
      PULL_ITEMS,
      BREAK_SHULKER,
      RETURN_SHULKER;
   }
}
