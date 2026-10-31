package night.module.impl.movement;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.Pos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import night.events.EventTarget;
import night.events.impl.PlayerMoveEvent;
import night.module.Module;
import night.module.ModuleInfo;

@ModuleInfo(name = "AntiVoid", description = "Prevents you from falling into the void.", category = Module.Category.MOVEMENT)
public class AntiVoid extends Module {
   @EventTarget
   public void a(PlayerMoveEvent var1) {
      if (mc.player != null && mc.level != null) {
         if (this.v()) {
            var1.setCancelled(true);
            double var2 = var1.getMovement().x;
            double var4 = 0.0;
            double var6 = var1.getMovement().z;
            var1.setMovement(new Vec3(var2, var4, var6));
            ClientPacketListener var8 = mc.getConnection();
            double var9 = mc.player.getX();
            double var11 = mc.player.getY();
            double var13 = mc.player.getZ();
            boolean var15 = mc.player.horizontalCollision;
            var8.send(new Pos(var9, var11, var13, true, var15));
         }
      }
   }

   private boolean v() {
      if (mc.player != null && mc.level != null) {
         int var1 = mc.level.getMinY();
         int var2 = (int)mc.player.getY();

         while (true) {
            int var3 = var2;
            int var4 = var1;
            if (var3 < var4) {
               double var18 = mc.player.fallDistance;
               double var20 = 0.0;
               boolean var22;
               if (var18 > var20) {
                  var22 = true;
               } else {
                  var22 = false;
               }

               return var22;
            }

            double var5 = mc.player.getX();
            double var7 = var2;
            double var9 = mc.player.getZ();
            BlockPos var11 = BlockPos.containing(var5, var7, var9);
            ClientLevel var12 = mc.level;
            BlockPos var13 = var11;
            BlockState var14 = var12.getBlockState(var13);
            if (!var14.isAir()) {
               BlockState var15 = var14;
               ClientLevel var16 = mc.level;
               BlockPos var17 = var11;
               if (!var15.getCollisionShape(var16, var17).isEmpty()) {
                  return false;
               }
            }

            var2 += -1;
         }
      } else {
         return false;
      }
   }
}
