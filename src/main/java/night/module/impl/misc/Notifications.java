package night.module.impl.misc;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.item.ItemStack;
import night.Night;
import night.events.EventTarget;
import night.events.impl.ClientConnectEvent;
import night.events.impl.PlayerDeathEvent;
import night.events.impl.Render2DEvent;
import night.events.impl.TickEvent;
import night.events.impl.TotemPopEvent;
import night.gui.HudEditorScreen;
import night.manager.ChatManager;
import night.module.Module;
import night.module.ModuleInfo;
import night.module.impl.core.HudEditorModule;
import night.setting.impl.BooleanSetting;
import night.setting.impl.ModeSetting;
import night.util.animation.EasingUtil;
import night.util.color.ChatColorUtil;
import night.util.color.ColorUtil;
import night.util.render.Render2DUtil;
import night.util.world.EntityUtil;
import org.joml.Matrix3x2fStack;

@ModuleInfo(name = "Notifications", description = "Notifies you in chat whenever something significant happens.", category = Module.Category.MISCELLANEOUS)
public class Notifications extends Module {
   public BooleanSetting totemPops = new BooleanSetting("TotemPops", "Notifies you whenever a player pops a totem.", true);
   public BooleanSetting visualRange = new BooleanSetting("VisualRange", "Notifies you whenever a player enters your render distance.", false);
   public BooleanSetting pearlThrows = new BooleanSetting("PearlThrows", "Notifies you whenever a player throws a pearl.", true);
   public BooleanSetting lowArmor = new BooleanSetting("LowArmor", "Warns you when armor durability is below 20%.", true);
   public ModeSetting mode;
   private final Map<UUID, String> j;
   private final ArrayList<Integer> k;
   private static final List<Notifications.ToastNotification> l = new CopyOnWriteArrayList<>();
   private static final Map<Object, Notifications.ToastNotification> m = new ConcurrentHashMap<>();
   private long n;
   private long o;
   private static final EquipmentSlot[] p;

   public Notifications() {
      BooleanSetting var2 = this.lowArmor;
      BooleanSetting.BooleanCondition var1 = new BooleanSetting.BooleanCondition(var2, true);
      String[] var3 = new String[]{"Chat", "Toasts", "Both"};
      this.mode = new ModeSetting("Mode", "How low armor notifications are delivered.", var1, "Both", var3);
      this.j = new LinkedHashMap<>();
      this.k = new ArrayList<>();
      long var4 = 0L;
      this.n = var4;
      long var6 = 0L;
      this.o = var6;
   }

   public boolean v() {
      boolean var1;
      if (!this.mode.getValue().equalsIgnoreCase("Chat") && !this.mode.getValue().equalsIgnoreCase("Both")) {
         var1 = false;
      } else {
         var1 = true;
      }

      return var1;
   }

   public boolean w() {
      boolean var1;
      if (!this.mode.getValue().equalsIgnoreCase("Toasts") && !this.mode.getValue().equalsIgnoreCase("Both")) {
         var1 = false;
      } else {
         var1 = true;
      }

      return var1;
   }

   @Override
   public void onDisable() {
      this.j.clear();
      this.k.clear();
      l.clear();
      m.clear();
      long var1 = 0L;
      this.o = var1;
      long var3 = 0L;
      this.n = var3;
   }

   @EventTarget
   public void a_void_ClientConnectEvent(ClientConnectEvent var1) {
      this.j.clear();
      this.k.clear();
      l.clear();
      m.clear();
      long var2 = 0L;
      this.o = var2;
      long var4 = 0L;
      this.n = var4;
   }

   @EventTarget
   public void a_void_TickEvent(TickEvent var1) {
      if (!this.nullCheck()) {
         if (this.visualRange.getValue()) {
            HashSet var2 = new HashSet();

            for (Entity var4 : (Iterable<Entity>)(Object)mc.level.entitiesForRendering()) {
               if (var4 instanceof Player var5) {
                  Entity var6 = var4;
                  LocalPlayer var7 = mc.player;
                  if (var6 != var7 && !EntityUtil.c_bool_Entity(var5)) {
                     FakePlayer var8;
                     if (Night.moduleManager != null) {
                        var8 = Night.moduleManager.getModule(FakePlayer.class);
                     } else {
                        var8 = null;
                     }

                     FakePlayer var9 = var8;
                     if (var9 != null && var9.isEnabled()) {
                        Entity var10 = var4;
                        RemotePlayer var11 = var9.z();
                        if (var10 == var11) {
                           continue;
                        }
                     }

                     HashSet var12 = var2;
                     UUID var13 = var5.getUUID();
                     var12.add(var13);
                     Map var14 = this.j;
                     UUID var15 = var5.getUUID();
                     if (!var14.containsKey(var15)) {
                        Map var16 = this.j;
                        UUID var17 = var5.getUUID();
                        String var18 = var5.getName().getString();
                        var16.put(var17, var18);
                        String var19 = String.valueOf(ChatColorUtil.a());
                        String var20 = var5.getName().getString();
                        String var21 = String.valueOf(ChatColorUtil.b());
                        Night.chatManager
                           .sendReplaceableMessage(var19 + var20 + var21 + " has entered your visual range.", "visual-range-" + var5.getName().getString());
                     }
                  }
               }
            }

            if (!this.j.isEmpty()) {
               Iterator var22 = this.j.entrySet().iterator();

               while (var22.hasNext()) {
                  Entry var23 = (Entry)var22.next();
                  HashSet var24 = var2;
                  Object var25 = var23.getKey();
                  if (!var24.contains(var25)) {
                     var22.remove();
                     String var26 = String.valueOf(ChatColorUtil.a());
                     String var27 = (String)var23.getValue();
                     String var28 = String.valueOf(ChatColorUtil.b());
                     Night.chatManager
                        .sendReplaceableMessage(var26 + var27 + var28 + " has left your visual range.", "visual-range-" + (String)var23.getValue());
                  }
               }
            }
         }

         if (this.pearlThrows.getValue()) {
            for (Entity var30 : (Iterable<Entity>)(Object)mc.level.entitiesForRendering()) {
               if (var30 instanceof ThrownEnderpearl var31 && var31.getOwner() != null) {
                  ArrayList var32 = this.k;
                  Integer var33 = var31.getId();
                  if (!var32.contains(var33)) {
                     String var34 = var31.getOwner().getName().getString();
                     String var35 = EntityUtil.a_Direction_ThrownEnderpearl(var31).toString();
                     String var36 = String.valueOf(ChatColorUtil.a());
                     String var37 = var34;
                     String var38 = String.valueOf(ChatColorUtil.b());
                     String var39 = var35;
                     Night.chatManager.sendReplaceableMessage(var36 + var37 + var38 + " threw a pearl towards " + var39 + ".", "pearl-throws-" + var34);
                     ArrayList var40 = this.k;
                     Integer var41 = var31.getId();
                     var40.add(var41);
                  }
               }
            }

            this.k.removeIf(var0 -> {
               ClientLevel var1x = mc.level;
               int var2x = var0;
               boolean var3;
               if (!(var1x.getEntity(var2x) instanceof ThrownEnderpearl)) {
                  var3 = true;
               } else {
                  var3 = false;
               }

               return var3;
            });
         }

         if (this.lowArmor.getValue()) {
            this.x();
         }
      }
   }

   private void x() {
      if (mc.player != null && !mc.player.isDeadOrDying()) {
         boolean var1;
         long var2;
         boolean var12;
         label58: {
            var1 = false;
            var2 = System.currentTimeMillis();
            if (this.v()) {
               long var4 = var2;
               long var6 = this.o;
               long var8 = var4 - var6;
               long var10 = 3500L;
               if (var8 > var10) {
                  var12 = true;
                  break label58;
               }
            }

            var12 = false;
         }

         boolean var13 = var12;
         EquipmentSlot[] var14 = p;
         int var15 = var14.length;
         int var16 = 0;

         while (true) {
            int var17 = var16;
            int var18 = var15;
            if (var17 >= var18) {
               if (var1) {
                  if (var13) {
                     long var47 = var2;
                     this.o = var47;
                  }

                  long var49 = var2;
                  long var51 = this.n;
                  long var53 = var49 - var51;
                  long var55 = 3500L;
                  if (var53 > var55) {
                     SoundManager var57 = mc.getSoundManager();
                     SimpleSoundInstance var58 = SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_PLING, 1.8F);
                     var57.play(var58);
                     long var59 = var2;
                     this.n = var59;
                  }
               }

               return;
            }

            EquipmentSlot[] var19 = var14;
            int var20 = var16;
            EquipmentSlot var21 = var19[var20];
            LocalPlayer var22 = mc.player;
            EquipmentSlot var23 = var21;
            ItemStack var24 = var22.getItemBySlot(var23);
            if (!var24.isEmpty() && var24.isDamageableItem()) {
               int var25 = var24.getMaxDamage();
               if (var25 > 0) {
                  int var26 = var24.getDamageValue();
                  int var27 = var25;
                  int var28 = var26;
                  int var29 = (var27 - var28) * 100;
                  int var30 = var25;
                  int var31 = var29 / var30;
                  if (var31 < 20) {
                     var1 = true;

                     String var33 = switch (var21) {
                        case HEAD -> "Helmet";
                        case CHEST -> "Chestplate";
                        case LEGS -> "Leggings";
                        case FEET -> "Boots";
                        default -> var24.getHoverName().getString();
                     };
                     if (this.w()) {
                        String var34 = "low_armor_" + var21.name();
                        String var35 = var33;
                        int var36 = var31;
                        String var37 = var35 + " is low (" + var36 + "%)!";
                        long var38 = 3500L;
                        Color var40 = new Color(255, 65, 65);
                        a(var34, var37, var38, var40);
                     }

                     if (var13) {
                        String var41 = String.valueOf(ChatColorUtil.a());
                        String var42 = var33;
                        String var43 = String.valueOf(ChatColorUtil.b());
                        String var44 = String.valueOf(ChatColorUtil.a());
                        int var45 = var31;
                        String var46 = String.valueOf(ChatColorUtil.b());
                        Night.chatManager
                           .sendReplaceableMessage(var41 + var42 + var43 + " is low (" + var44 + var45 + "%" + var46 + ")!", "low_armor_" + var21.name());
                     }
                  }
               }
            }

            var16++;
         }
      }
   }

   @EventTarget
   public void a_void_TotemPopEvent(TotemPopEvent var1) {
      if (this.totemPops.getValue()) {
         String var2 = var1.getPlayer().getName().getString();
         ChatManager var3 = Night.chatManager;
         String var4 = String.valueOf(ChatColorUtil.a());
         String var5 = var2;
         String var6 = String.valueOf(ChatColorUtil.b());
         String var7 = String.valueOf(ChatColorUtil.a());
         int var8 = var1.getPops();
         String var9 = String.valueOf(ChatColorUtil.b());
         String var10;
         if (var1.getPops() > 1) {
            var10 = "s";
         } else {
            var10 = "";
         }

         var3.sendReplaceableMessage(var4 + var5 + var6 + " has popped " + var7 + var8 + var9 + " totem" + var10 + ".", "totem-pop-" + var2);
      }
   }

   @EventTarget
   public void a_void_PlayerDeathEvent(PlayerDeathEvent var1) {
      Map var2 = Night.worldStateManager.getTotemPops();
      UUID var3 = var1.getPlayer().getUUID();
      Integer var4 = 0;
      int var5 = (Integer)var2.getOrDefault(var3, var4);
      if (this.totemPops.getValue() && var5 > 0) {
         String var6 = var1.getPlayer().getName().getString();
         ChatManager var7 = Night.chatManager;
         String var8 = String.valueOf(ChatColorUtil.a());
         String var9 = var6;
         String var10 = String.valueOf(ChatColorUtil.b());
         String var11 = String.valueOf(ChatColorUtil.a());
         int var12 = var5;
         String var13 = String.valueOf(ChatColorUtil.b());
         String var14;
         if (var5 > 1) {
            var14 = "s";
         } else {
            var14 = "";
         }

         var7.sendReplaceableMessage(var8 + var9 + var10 + " has died after popping " + var11 + var12 + var13 + " totem" + var14 + ".", "totem-pop-" + var6);
      }
   }

   @EventTarget
   public void a_void_Render2DEvent(Render2DEvent var1) {
      if (!this.nullCheck()) {
         HudEditorModule var2;
         if (Night.moduleManager != null) {
            var2 = Night.moduleManager.getModule(HudEditorModule.class);
         } else {
            var2 = null;
         }

         HudEditorModule var3 = var2;
         float var4;
         if (var3 != null) {
            var4 = var3.lowArmorPosition.getX();
         } else {
            var4 = 0.0F;
         }

         float var5 = var4;
         float var6;
         if (var3 != null) {
            var6 = var3.lowArmorPosition.getY();
         } else {
            var6 = 0.0F;
         }

         float var7 = var6;
         boolean var8;
         if (var3 != null && var3.lowArmor.getValue() && !(mc.gui.screen() instanceof HudEditorScreen)) {
            var8 = true;
         } else {
            var8 = false;
         }

         boolean var9 = var8;
         if (!l.isEmpty() || var9) {
            float var10 = mc.getWindow().getGuiScaledWidth();
            float[] var11 = new float[1];
            float var12 = var7;
            float var13 = 8.0F + var12;
            var11[0] = var13;
            float[] var14 = var11;
            Matrix3x2fStack var15 = var1.getMatrices();
            if (!l.isEmpty()) {
               float var16 = var10;
               float var17 = var5;
               Matrix3x2fStack var18 = var15;
               float[] var19 = var14;
               l.removeIf(var6x -> {
                  float var7x = var6x.c();
                  float var8x = var16 - var7x - 8.0F + var17;
                  var18.pushMatrix();
                  float var9x = var8x;
                  float var10x = var19[0];
                  var18.translate(var9x, var10x);
                  GuiGraphicsExtractor var11x = var1.getContext();
                  float var12x = var7x;
                  float var13x = var6x.a(var11x, var12x);
                  var18.popMatrix();
                  if (var13x == -1.0F) {
                     Object var14x = var6x.a();
                     m.remove(var14x, var6x);
                     return true;
                  } else {
                     float var15x = var19[0];
                     float var16x = var13x;
                     float var17x = var15x + var16x;
                     var19[0] = var17x;
                     return false;
                  }
               });
            }

            if (var9) {
               float var20 = Night.fontManager.getStringWidth("Helmet is low (15%)!");
               float var21 = 12.0F + var20 + 8.0F;
               float var22 = Math.max(140.0F, var21);
               float var23 = var10;
               float var24 = var22;
               float var25 = var23 - var24 - 8.0F;
               float var26 = var5;
               float var27 = var25 + var26;
               long var28 = System.currentTimeMillis();
               long var30 = 3500L;
               long var32 = var28 % var30;
               var15.pushMatrix();
               Matrix3x2fStack var34 = var15;
               float var35 = var27;
               float var36 = var14[0];
               var34.translate(var35, var36);
               GuiGraphicsExtractor var37 = var1.getContext();
               float var38 = var22;
               Color var39 = new Color(255, 65, 65);
               long var40 = var32;
               long var42 = 2200L;
               a(var37, 0.0F, 0.0F, var38, "Helmet is low (15%)!", var39, var40, var42);
               var15.popMatrix();
            }
         }
      }
   }

   public static void b(String var0) {
      Integer var1 = var0.hashCode();
      long var2 = 3000L;
      a(var1, var0, var2, null);
   }

   public static void a(Object var0, String var1, long var2, Color var4) {
      Notifications.ToastNotification var5 = m.get(var0);
      if (var5 != null && !var5.b()) {
         Notifications.ToastNotification var6 = var5;
         long var7 = var2;
         var6.a(var1, var7, var4);
      } else {
         long var9 = var2;
         Notifications.ToastNotification var11 = new Notifications.ToastNotification(var0, var1, var9, var4);
         m.put(var0, var11);
         Notifications.ToastNotification var12 = var11;
         l.add(var12);
      }
   }

   public static float a(GuiGraphicsExtractor var0, float var1, float var2, float var3, String var4, Color var5, long var6, long var8) {
      float var10 = Night.fontManager.getFontHeight() * 2.2F;
      float var11 = Math.max(22.0F, var10);
      Color var12;
      if (var5 != null) {
         var12 = var5;
      } else {
         var12 = ColorUtil.a_Color_int(255);
      }

      Color var13 = var12;
      Color var14 = new Color(18, 18, 18, 205);
      float var15 = 4.0F * 2.0F + 2.0F;
      float var16 = var11;
      float var17 = Night.fontManager.getFontHeight();
      float var18 = (var16 - var17) / 2.0F;
      long var19 = var6;
      long var21 = 300L;
      if (var19 < var21) {
         float var23 = EasingUtil.a((float)var6 / 300.0F, EasingUtil.Easing.EASE_OUT_CUBIC);
         float var24 = 1.0F - var23;
         float var25 = var3 * var24;
         float var26 = var1 + var25;
         float var27 = var1 + var3;
         float var28 = var11;
         float var29 = var2 + var28;
         Color var30 = var13;
         Render2DUtil.a(var0, var26, var2, var27, var29, var30);
         float var31 = var11 + 4.0F;
         float var32 = var23;
         return var31 * var32;
      } else {
         long var33 = var6;
         long var35 = 500L;
         if (var33 < var35) {
            long var37 = var6;
            long var39 = 300L;
            float var41 = EasingUtil.a((float)(var37 - var39) / 200.0F, EasingUtil.Easing.EASE_OUT_CUBIC);
            float var42 = var1 + var3;
            float var43 = var11;
            float var44 = var2 + var43;
            Color var45 = var14;
            Render2DUtil.a(var0, var1, var2, var42, var44, var45);
            float var46 = var41;
            int var47 = (int)(255.0F * var46);
            int var48 = Math.min(255, var47);
            int var49 = Math.max(0, var48);
            Color var50 = new Color(255, 255, 255, var49);
            float var51 = var15;
            int var52 = (int)(var1 + var51);
            float var53 = var18;
            int var54 = (int)(var2 + var53);
            Color var55 = var50;
            Night.fontManager.drawShadowedText(var0, var4, var52, var54, var55);
            float var56 = var1 + 4.0F;
            float var57 = var3 - 4.0F;
            float var58 = var41;
            float var59 = 1.0F - var58;
            float var60 = var57 * var59;
            float var61 = var56 + var60;
            float var62 = var11;
            float var63 = var2 + var62;
            Color var64 = var13;
            Render2DUtil.a(var0, var1, var2, var61, var63, var64);
            return var11 + 4.0F;
         } else {
            long var65 = var6;
            long var67 = var8;
            if (var65 < var67) {
               float var69 = var1 + var3;
               float var70 = var11;
               float var71 = var2 + var70;
               Color var72 = var14;
               Render2DUtil.a(var0, var1, var2, var69, var71, var72);
               float var73 = var1 + 4.0F;
               float var74 = var11;
               float var75 = var2 + var74;
               Color var76 = var13;
               Render2DUtil.a(var0, var1, var2, var73, var75, var76);
               float var77 = var15;
               int var78 = (int)(var1 + var77);
               float var79 = var18;
               int var80 = (int)(var2 + var79);
               Night.fontManager.drawShadowedText(var0, var4, var78, var80, Color.WHITE);
               return var11 + 4.0F;
            } else {
               long var81 = var6;
               long var83 = var8;
               long var85 = var81 - var83;
               long var87 = 200L;
               if (var85 < var87) {
                  float var89 = EasingUtil.a((float)var85 / 200.0F, EasingUtil.Easing.EASE_OUT_CUBIC);
                  float var90 = 1.0F - var89;
                  float var91 = var1 + var3;
                  float var92 = var11;
                  float var93 = var2 + var92;
                  Color var94 = var14;
                  Render2DUtil.a(var0, var1, var2, var91, var93, var94);
                  float var95 = var90;
                  int var96 = (int)(255.0F * var95);
                  int var97 = Math.min(255, var96);
                  int var98 = Math.max(0, var97);
                  Color var99 = new Color(255, 255, 255, var98);
                  float var100 = var15;
                  int var101 = (int)(var1 + var100);
                  float var102 = var18;
                  int var103 = (int)(var2 + var102);
                  Color var104 = var99;
                  Night.fontManager.drawShadowedText(var0, var4, var101, var103, var104);
                  float var105 = var1 + 4.0F;
                  float var106 = var3 - 4.0F;
                  float var107 = var90;
                  float var108 = 1.0F - var107;
                  float var109 = var106 * var108;
                  float var110 = var105 + var109;
                  float var111 = var11;
                  float var112 = var2 + var111;
                  Color var113 = var13;
                  Render2DUtil.a(var0, var1, var2, var110, var112, var113);
                  return var11 + 4.0F;
               } else {
                  long var114 = var85;
                  long var116 = 500L;
                  if (var114 < var116) {
                     long var118 = var85;
                     long var120 = 200L;
                     float var122 = EasingUtil.a((float)(var118 - var120) / 300.0F, EasingUtil.Easing.EASE_OUT_CUBIC);
                     float var123 = 1.0F - var122;
                     float var124 = 1.0F - var123;
                     float var125 = var3 * var124;
                     float var126 = var1 + var125;
                     float var127 = var1 + var3;
                     float var128 = var11;
                     float var129 = var2 + var128;
                     Color var130 = var13;
                     Render2DUtil.a(var0, var126, var2, var127, var129, var130);
                     float var131 = var11 + 4.0F;
                     float var132 = var123;
                     return var131 * var132;
                  } else {
                     return -1.0F;
                  }
               }
            }
         }
      }
   }

   static {
      EquipmentSlot[] var0 = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
      p = var0;
   }

   static class ToastNotification {
      private final Object a;
      private String b;
      private long c;
      private Color d;
      private final long e;

      public ToastNotification(Object var1, String var2, long var3, Color var5) {
         this.a = var1;
         this.b = var2;
         long var6 = var3;
         this.c = var6;
         this.d = var5;
         long var8 = System.currentTimeMillis();
         this.e = var8;
      }

      public Object a() {
         return this.a;
      }

      public boolean b() {
         long var1 = System.currentTimeMillis();
         long var3 = this.e;
         long var5 = var1 - var3;
         long var7 = this.c;
         boolean var9;
         if (var5 > var7) {
            var9 = true;
         } else {
            var9 = false;
         }

         return var9;
      }

      public void a(String var1, long var2, Color var4) {
         this.b = var1;
         long var5 = System.currentTimeMillis();
         long var7 = this.e;
         long var9 = var5 - var7;
         long var11 = var2;
         long var13 = var9 + var11;
         this.c = var13;
         if (var4 != null) {
            this.d = var4;
         }
      }

      public float c() {
         String var1 = this.b;
         float var2 = Night.fontManager.getStringWidth(var1);
         float var3 = 4.0F * 3.0F;
         float var4 = var2;
         float var5 = var3 + var4 + 8.0F;
         return Math.max(140.0F, var5);
      }

      public float a(GuiGraphicsExtractor var1, float var2) {
         String var3 = this.b;
         Color var4 = this.d;
         long var5 = System.currentTimeMillis();
         long var7 = this.e;
         long var9 = var5 - var7;
         long var11 = this.c;
         return Notifications.a(var1, 0.0F, 0.0F, var2, var3, var4, var9, var11);
      }
   }
}
