package night.module.impl.movement;

import com.mojang.blaze3d.vertex.PoseStack;
import java.awt.Color;
import java.util.ArrayList;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.Pos;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.PosRot;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import night.events.EventTarget;
import night.events.impl.PacketReceiveEvent;
import night.events.impl.PacketSendEvent;
import night.events.impl.Render3DEvent;
import night.events.impl.TickEvent;
import night.module.Module;
import night.module.ModuleInfo;
import night.setting.Setting;
import night.setting.impl.BooleanSetting;
import night.setting.impl.ColorSetting;
import night.setting.impl.ModeSetting;
import night.setting.impl.NumberSetting;
import night.util.color.ColorUtil;
import night.util.math.TimerUtil;
import night.util.render.Render3DUtil;
import night.util.world.EntityUtil;

@ModuleInfo(name = "FakeLag", description = "Chokes sent packets to look like you are lagging.", category = Module.Category.MOVEMENT)
public class FakeLag extends Module {
   public NumberSetting choke;
   public BooleanSetting onlyOnHoleLeave;
   public NumberSetting autoDisable;
   public ModeSetting renderMode;
   public ColorSetting fillColor;
   public ColorSetting outlineColor;
   private final ArrayList<ServerboundMovePlayerPacket> k;
   private final TimerUtil l;
   private final TimerUtil m;
   private final TimerUtil n;
   private final TimerUtil o;
   private boolean p;
   private boolean q;
   private Vec3 r;
   private boolean s;
   private BlockPos t;

   public FakeLag() {
      Integer var1 = 2;
      Integer var2 = 1;
      Integer var3 = 5;
      this.choke = new NumberSetting("Choke", "The delay to choke packets for.", var1, var2, var3);
      this.onlyOnHoleLeave = new BooleanSetting("OnlyOnHoleLeave", "Only choke packets when leaving a hole.", false);
      Setting.VisibilityCondition var4 = new Setting.VisibilityCondition();
      Integer var5 = 0;
      Integer var6 = 0;
      Integer var7 = 1000;
      Integer var8 = 100;
      this.autoDisable = new NumberSetting(
         "AutoDisable", "AutoDisable", "Delay (ms) after FakeLag is enabled before it disables itself. 0 = off.", var4, var5, var6, var7, var8
      );
      String[] var9 = new String[]{"None", "Fill", "Outline", "Both"};
      this.renderMode = new ModeSetting("RenderMode", "The rendering that will be applied to the target entity.", "Both", var9);
      ModeSetting var11 = this.renderMode;
      String[] var12 = new String[]{"Fill", "Both"};
      ModeSetting.ModeCondition var10 = new ModeSetting.ModeCondition(var11, var12);
      ColorSetting.ColorValue var13 = ColorUtil.b();
      this.fillColor = new ColorSetting("FillColor", "The color that will be used for the fill rendering.", var10, var13);
      ModeSetting var15 = this.renderMode;
      String[] var16 = new String[]{"Outline", "Both"};
      ModeSetting.ModeCondition var14 = new ModeSetting.ModeCondition(var15, var16);
      ColorSetting.ColorValue var17 = ColorUtil.c();
      this.outlineColor = new ColorSetting("OutlineColor", "The color that will be used for the outline rendering.", var14, var17);
      this.k = new ArrayList<>();
      this.l = new TimerUtil();
      this.m = new TimerUtil();
      this.n = new TimerUtil();
      this.o = new TimerUtil();
      this.p = false;
      this.q = false;
      this.r = null;
      this.s = false;
      this.t = null;
   }

   @EventTarget
   public void a_void_PacketReceiveEvent(PacketReceiveEvent var1) {
      if (!this.nullCheck()) {
         if (var1.getPacket() instanceof ClientboundPlayerPositionPacket var3) {
            double var4 = var3.change().position().x();
            double var6 = var3.change().position().y();
            double var8 = var3.change().position().z();
            this.r = new Vec3(var4, var6, var8);
            this.w();
            this.m.reset();
         } else {
            if (!(var1.getPacket() instanceof ClientboundDisconnectPacket)) {
               Packet var10 = var1.getPacket();
               if (!(var10 instanceof ClientboundSetHealthPacket) || !(((ClientboundSetHealthPacket)var10).getHealth() <= 0.0F)) {
                  return;
               }
            }

            this.w();
            this.m.reset();
         }
      }
   }

   @EventTarget
   public void a_void_PacketSendEvent(PacketSendEvent var1) {
      if (!this.nullCheck() && !this.q && var1.getPacket() instanceof ServerboundMovePlayerPacket var3 && this.a_bool_ServerboundMovePlayerPacket(var3)) {
         if (!this.x()) {
            Vec3 var4 = mc.player.position();
            this.r = var4;
         } else {
            synchronized (this.k) {
               var1.setCancelled(true);
               ArrayList var6 = this.k;
               ServerboundMovePlayerPacket var7 = var3;
               var6.add(var7);
            }
         }
      }
   }

   @EventTarget
   public void a_void_Render3DEvent(Render3DEvent var1) {
      if (!this.nullCheck() && this.r != null && !this.renderMode.getValue().equalsIgnoreCase("None")) {
         double var2 = this.r.x;
         double var4 = mc.player.getBoundingBox().getXsize();
         double var6 = 2.0;
         double var8 = var4 / var6;
         double var10 = var2 - var8;
         double var12 = this.r.y;
         double var14 = this.r.z;
         double var16 = mc.player.getBoundingBox().getZsize();
         double var18 = 2.0;
         double var20 = var16 / var18;
         double var22 = var14 - var20;
         double var24 = this.r.x;
         double var26 = mc.player.getBoundingBox().getXsize();
         double var28 = 2.0;
         double var30 = var26 / var28;
         double var32 = var24 + var30;
         double var34 = this.r.y;
         double var36 = mc.player.getBoundingBox().getYsize();
         double var38 = var34 + var36;
         double var40 = this.r.z;
         double var42 = mc.player.getBoundingBox().getZsize();
         double var44 = 2.0;
         double var46 = var42 / var44;
         double var48 = var40 + var46;
         AABB var50 = new AABB(var10, var12, var22, var32, var38, var48);
         if (this.renderMode.getValue().equalsIgnoreCase("Fill") || this.renderMode.getValue().equalsIgnoreCase("Both")) {
            PoseStack var51 = var1.getMatrices();
            AABB var52 = var50;
            Color var53 = this.fillColor.getColor();
            Render3DUtil.a_void_PoseStack_AABB_Color(var51, var52, var53);
         }

         if (this.renderMode.getValue().equalsIgnoreCase("Outline") || this.renderMode.getValue().equalsIgnoreCase("Both")) {
            PoseStack var54 = var1.getMatrices();
            AABB var55 = var50;
            Color var56 = this.outlineColor.getColor();
            Render3DUtil.b(var54, var55, var56);
         }
      }
   }

   @EventTarget
   public void a_void_TickEvent(TickEvent var1) {
      if (!this.nullCheck()) {
         if (this.onlyOnHoleLeave.getValue()) {
            BlockPos var2 = this.t;
            BlockPos var3 = BlockPos.containing(mc.player.position());
            this.t = var3;
            BlockPos var4;
            if (var2 == null) {
               var4 = this.t;
            } else {
               var4 = var2;
            }

            BlockPos var5 = var4;
            BlockPos var6 = this.t;
            BlockPos var7 = var5;
            if (!var6.equals(var7)) {
               boolean var8 = HoleSnap.a_bool_BlockPos(this.t);
               boolean var9 = HoleSnap.a_bool_BlockPos(var5);
               if (!var8 && var9) {
                  this.s = true;
                  this.n.reset();
               }
            }

            if (this.s) {
               TimerUtil var10 = this.n;
               Integer var11 = (int)(this.choke.getValue().floatValue() * 100.0F);
               if (var10.passedMs(var11)) {
                  this.w();
                  this.s = false;
               }
            }
         } else if (!this.k.isEmpty()) {
            TimerUtil var12 = this.l;
            Integer var13 = (int)(this.choke.getValue().floatValue() * 100.0F);
            if (var12.passedMs(var13)) {
               this.w();
               this.l.reset();
            }
         }

         if (this.p) {
            TimerUtil var14 = this.o;
            Integer var15 = this.autoDisable.getValue().intValue();
            if (var14.passedMs(var15)) {
               this.p = false;
               this.setEnabled(false);
            }
         }
      }
   }

   private void v() {
      this.p = true;
      this.o.reset();
   }

   @Override
   public void onEnable() {
      this.l.reset();
      this.s = false;
      if (this.autoDisable.getValue().intValue() > 0) {
         this.v();
      } else {
         this.p = false;
      }

      this.t = null;
      Vec3 var1;
      if (mc.player != null) {
         var1 = mc.player.position();
      } else {
         var1 = null;
      }

      this.r = var1;
   }

   @Override
   public void onDisable() {
      if (!this.nullCheck()) {
         this.w();
         this.s = false;
         this.t = null;
      }
   }

   private void w() {
      synchronized (this.k) {
         this.q = true;

         for (ServerboundMovePlayerPacket var3 : (Iterable<ServerboundMovePlayerPacket>)(Object)this.k) {
            ClientPacketListener var4 = mc.player.connection;
            ServerboundMovePlayerPacket var5 = var3;
            var4.send(var5);
         }

         if (!this.k.isEmpty()) {
            Vec3 var6 = mc.player.position();
            this.r = var6;
         }

         this.k.clear();
         this.q = false;
      }
   }

   private boolean x() {
      boolean var1;
      if (this.onlyOnHoleLeave.getValue()) {
         var1 = this.s;
      } else {
         label17: {
            double var2 = EntityUtil.a_double_Entity_SpeedUnit(mc.player, EntityUtil.SpeedUnit.KILOMETERS);
            double var4 = 5.0;
            if (!(var2 >= var4)) {
               double var6 = mc.player.fallDistance;
               double var8 = 0.0;
               if (!(var6 > var8)) {
                  break label17;
               }
            }

            TimerUtil var10 = this.m;
            Integer var11 = 1000;
            if (var10.passedMs(var11)) {
               var1 = true;
               return var1;
            }
         }

         var1 = false;
      }

      return var1;
   }

   private boolean a_bool_ServerboundMovePlayerPacket(ServerboundMovePlayerPacket var1) {
      boolean var2;
      if (!(var1 instanceof Pos) && !(var1 instanceof PosRot)) {
         var2 = false;
      } else {
         var2 = true;
      }

      return var2;
   }

   @Override
   public String getDisplayInfo() {
      ChatFormatting var1;
      if (this.x()) {
         var1 = ChatFormatting.GREEN;
      } else {
         var1 = ChatFormatting.RED;
      }

      return var1 + "Choke";
   }
}
