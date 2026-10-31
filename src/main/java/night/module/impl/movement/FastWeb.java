package night.module.impl.movement;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action;
import net.minecraft.world.level.block.WebBlock;
import night.events.EventTarget;
import night.events.impl.UpdateWalkingPlayerEvent;
import night.module.Module;
import night.module.ModuleInfo;
import night.setting.impl.BooleanSetting;
import night.setting.impl.ModeSetting;
import night.setting.impl.NumberSetting;
import night.util.world.EntityUtil;

@ModuleInfo(name = "FastWeb", description = "Allows you to move quickly through webs.", category = Module.Category.MOVEMENT)
public class FastWeb extends Module {
   public ModeSetting mode;
   public NumberSetting speed;
   public NumberSetting horizontal;
   public NumberSetting vertical;
   public BooleanSetting sneak;
   public BooleanSetting grim;

   public FastWeb() {
      String[] var1 = new String[]{"Normal", "Ignore", "Strong"};
      this.mode = new ModeSetting("Mode", "The method that will be used to speed you through webs.", "Normal", var1);
      ModeSetting var3 = this.mode;
      String[] var4 = new String[]{"Normal"};
      ModeSetting.ModeCondition var2 = new ModeSetting.ModeCondition(var3, var4);
      Double var5 = 3.0;
      Double var6 = 0.1;
      Double var7 = 10.0;
      this.speed = new NumberSetting("Speed", "The speed at which you will fall through webs.", var2, var5, var6, var7);
      ModeSetting var9 = this.mode;
      String[] var10 = new String[]{"Strong"};
      ModeSetting.ModeCondition var8 = new ModeSetting.ModeCondition(var9, var10);
      Double var11 = 2.5;
      Double var12 = 0.1;
      Double var13 = 5.0;
      this.horizontal = new NumberSetting("Horizontal", "The speed at which you will move horizontally.", var8, var11, var12, var13);
      ModeSetting var15 = this.mode;
      String[] var16 = new String[]{"Strong"};
      ModeSetting.ModeCondition var14 = new ModeSetting.ModeCondition(var15, var16);
      Double var17 = 2.5;
      Double var18 = 0.1;
      Double var19 = 5.0;
      this.vertical = new NumberSetting("Vertical", "The speed at which you will move vertically.", var14, var17, var18, var19);
      ModeSetting var20 = this.mode;
      String[] var21 = new String[]{"Normal"};
      this.sneak = new BooleanSetting("Sneak", "Only bypasses web slowdown when sneaking.", new ModeSetting.ModeCondition(var20, var21), false);
      this.grim = new BooleanSetting("Grim", "Includes bypasses for the Grim anticheat.", false);
   }

   @EventTarget
   public void a(UpdateWalkingPlayerEvent var1) {
      if (mc.player != null && mc.level != null) {
         if (mc.options.keyShift.isDown() || !this.sneak.getValue()) {
            if (this.mode.getValue().equals("Normal") && !mc.player.onGround() && EntityUtil.d_bool_Entity(mc.player)) {
               LocalPlayer var2 = mc.player;
               double var3 = mc.player.getDeltaMovement().x;
               double var5 = mc.player.getDeltaMovement().y;
               double var7 = this.speed.getValue().doubleValue();
               double var9 = var5 - var7;
               double var11 = mc.player.getDeltaMovement().z;
               var2.setDeltaMovement(var3, var9, var11);
            }

            if (this.grim.getValue()) {
               for (BlockPos var14 : (Iterable<BlockPos>)(Object)this.v()) {
                  ClientPacketListener var15 = mc.getConnection();
                  BlockPos var16 = var14;
                  var15.send(new ServerboundPlayerActionPacket(Action.ABORT_DESTROY_BLOCK, var16, Direction.DOWN));
                  ClientPacketListener var17 = mc.getConnection();
                  BlockPos var18 = var14;
                  var17.send(new ServerboundPlayerActionPacket(Action.STOP_DESTROY_BLOCK, var18, Direction.DOWN));
               }
            }
         }
      }
   }

   @Override
   public String getDisplayInfo() {
      return this.mode.getValue();
   }

   public List<BlockPos> v() {
      ArrayList var1 = new ArrayList();

      for (byte var2 = 2; var2 > -2; var2 += -1) {
         for (byte var3 = 2; var3 > -2; var3 += -1) {
            for (byte var4 = 2; var4 > -2; var4 += -1) {
               BlockPos var5 = mc.player.blockPosition();
               byte var6 = var2;
               byte var7 = var3;
               byte var8 = var4;
               BlockPos var9 = var5.offset(var6, var7, var8);
               ClientLevel var10 = mc.level;
               BlockPos var11 = var9;
               if (var10.getBlockState(var11).getBlock() instanceof WebBlock) {
                  ArrayList var12 = var1;
                  BlockPos var13 = var9;
                  var12.add(var13);
               }
            }
         }
      }

      return var1;
   }
}
