package night.module.impl.movement;

import lombok.Generated;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import night.events.EventTarget;
import night.events.impl.TickEvent;
import night.module.Module;
import night.module.ModuleInfo;
import night.setting.impl.BooleanSetting;
import night.util.math.TimerUtil;

@ModuleInfo(
   name = "HitboxDesync",
   description = "Precisely offsets your position to glitch out Minecraft hitbox calculations.",
   category = Module.Category.MOVEMENT
)
public class HitboxDesync extends Module {
   public BooleanSetting alternating = new BooleanSetting("Alternating", "Modifies your position in such a way that it messes with other clients.", false);
   public BooleanSetting minimal;
   public BooleanSetting specific;
   public BooleanSetting close;
   public BooleanSetting selfDisable;
   public BooleanSetting jumpDisable;
   private final TimerUtil k;
   private double l;

   public HitboxDesync() {
      BooleanSetting var1 = this.alternating;
      this.minimal = new BooleanSetting(
         "Minimal", "Makes alternating minimal, only required on certain servers.", new BooleanSetting.BooleanCondition(var1, true), false
      );
      BooleanSetting var2 = this.alternating;
      this.specific = new BooleanSetting(
         "Specific", "Specific alternating mode, only required against certain clients.", new BooleanSetting.BooleanCondition(var2, true), false
      );
      this.close = new BooleanSetting("Close", "Whether or not to have the surround place blocks on desynced positions.", false);
      BooleanSetting var3 = this.alternating;
      this.selfDisable = new BooleanSetting(
         "SelfDisable", "Toggles off the module after having desynced your hitbox.", new BooleanSetting.BooleanCondition(var3, false), true
      );
      BooleanSetting var4 = this.alternating;
      this.jumpDisable = new BooleanSetting(
         "JumpDisable", "Toggles off the module whenever your Y level changes.", new BooleanSetting.BooleanCondition(var4, true), true
      );
      this.k = new TimerUtil();
   }

   @EventTarget(priority = Integer.MAX_VALUE)
   public void a(TickEvent var1) {
      label84:
      if (!this.nullCheck()) {
         if (this.jumpDisable.getValue()) {
            double var2 = mc.player.getY();
            double var4 = this.l;
            if (var2 != var4) {
               break label84;
            }
         }

         Vec3 var6 = Vec3.atCenterOf(mc.player.blockPosition());
         double var7;
         if (this.minimal.getValue()) {
            var7 = 0.001;
         } else {
            var7 = 0.002;
         }

         double var9 = var7;
         double var11;
         if (this.specific.getValue()) {
            var11 = 500.0;
         } else {
            var11 = 1500.0;
         }

         double var13 = var11;
         TimerUtil var15 = this.k;
         Double var16 = var13;
         boolean var17;
         if (var15.passedMs(var16) && this.alternating.getValue() && !mc.player.isShiftKeyDown()) {
            var17 = true;
         } else {
            var17 = false;
         }

         boolean var18 = var17;
         double var19 = var6.x;
         double var21 = mc.player.getX();
         double var23 = var19 - var21;
         double var25 = 0.0;
         boolean var27;
         if (var23 > var25) {
            var27 = true;
         } else {
            var27 = false;
         }

         boolean var28 = var27;
         double var29 = var6.z;
         double var31 = mc.player.getZ();
         double var33 = var29 - var31;
         double var35 = 0.0;
         boolean var37;
         if (var33 > var35) {
            var37 = true;
         } else {
            var37 = false;
         }

         boolean var38 = var37;
         double var39 = var6.x;
         double var41;
         if (var18) {
            var41 = var9;
         } else {
            var41 = 0.0;
         }

         byte var43;
         if (var28) {
            var43 = 1;
         } else {
            var43 = -1;
         }

         double var44 = var43;
         double var46 = var41 * var44;
         double var48 = var39 + var46;
         double var50 = 0.20000000009497754;
         byte var52;
         if (var28) {
            var52 = -1;
         } else {
            var52 = 1;
         }

         double var53 = var52;
         double var55 = var50 * var53;
         double var57 = var48 + var55;
         double var59 = var6.z;
         double var61;
         if (var18) {
            var61 = var9;
         } else {
            var61 = 0.0;
         }

         byte var63;
         if (var38) {
            var63 = 1;
         } else {
            var63 = -1;
         }

         double var64 = var63;
         double var66 = var61 * var64;
         double var68 = var59 + var66;
         double var70 = 0.2000000000949811;
         byte var72;
         if (var38) {
            var72 = -1;
         } else {
            var72 = 1;
         }

         double var73 = var72;
         double var75 = var70 * var73;
         double var77 = var68 + var75;
         LocalPlayer var79 = mc.player;
         double var80 = var57;
         double var82 = mc.player.getY();
         double var84 = var77;
         var79.setPos(var80, var82, var84);
         TimerUtil var86 = this.k;
         Double var87 = var13;
         if (var86.passedMs(var87)) {
            this.k.reset();
         }

         if (this.selfDisable.getValue() && !this.alternating.getValue()) {
            this.setEnabled(false);
         }

         return;
      }

      this.setEnabled(false);
   }

   @Override
   public void onEnable() {
      if (mc.player != null && mc.level != null) {
         double var1 = mc.player.getY();
         this.l = var1;
      } else {
         this.setEnabled(false);
      }
   }

   @Generated
   public TimerUtil v() {
      return this.k;
   }
}
