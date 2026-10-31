package night.util.inventory;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import night.Night;
import night.events.EventTarget;
import night.events.impl.PacketReceiveEvent;

public final class EnderChestMemory {
   private static final EnderChestMemory a = new EnderChestMemory();
   private static boolean b = false;
   private static NonNullList<ItemStack> c = null;
   private int d = -1;

   private EnderChestMemory() {
   }

   public static void a() {
      if (!b) {
         b = true;
         Night.EVENT_BUS.register(a);
      }
   }

   public static boolean b() {
      return c != null;
   }

   public static NonNullList<ItemStack> c() {
      return c;
   }

   @EventTarget
   public void a(PacketReceiveEvent var1) {
      if (var1.getPacket() instanceof ClientboundOpenScreenPacket var10) {
         if (this.d()) {
            this.d = var10.getContainerId();
            c = NonNullList.withSize(27, ItemStack.EMPTY);
         } else {
            this.d = -1;
         }
      } else if (this.d != -1) {
         if (!(var1.getPacket() instanceof ClientboundContainerSetContentPacket var3)) {
            if (var1.getPacket() instanceof ClientboundContainerSetSlotPacket var9) {
               if (var9.getContainerId() != this.d) {
                  return;
               }

               int var8 = var9.getSlot();
               if (c != null && var8 >= 0 && var8 < 27) {
                  c.set(var8, var9.getItem().copy());
               }
            }
         } else if (var3.containerId() == this.d) {
            NonNullList var6 = NonNullList.withSize(27, ItemStack.EMPTY);

            for (int var4 = 0; var4 < 27 && var4 < var3.items().size(); var4++) {
               var6.set(var4, ((ItemStack)var3.items().get(var4)).copy());
            }

            c = var6;
         }
      }
   }

   private boolean d() {
      Minecraft var1 = Minecraft.getInstance();
      if (var1.level == null) {
         return false;
      } else if (var1.hitResult instanceof BlockHitResult var3) {
         BlockPos var4 = var3.getBlockPos();
         return var1.level.getBlockState(var4).is(Blocks.ENDER_CHEST);
      } else {
         return false;
      }
   }
}
