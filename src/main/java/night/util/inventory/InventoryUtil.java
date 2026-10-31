package night.util.inventory;

import io.netty.channel.Channel;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.ShulkerBoxScreen;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import night.Night;
import night.mixins.accessors.ClientPlayerInteractionManagerAccessor;
import night.mixins.accessors.ConnectionAccessor;
import night.pingbypass.PingBypassState;
import night.pingbypass.protocol.PingBypassPayload;
import night.pingbypass.protocol.c2s.C2SWindowClickPacket;
import night.pingbypass.protocol.s2c.S2CHeldSlotSyncPacket;
import night.util.MinecraftInstance;
import night.utils.minecraft.ImageShaderUtil;

public class InventoryUtil implements MinecraftInstance {
   public static String[] a = new String[]{"None", "Normal", "Silent", "AltPickup", "AltSwap"};
   public static String[] b = new String[]{"Pickup", "Swap"};
   public static int c = 0;
   public static int d = 8;
   public static int e = 9;
   public static int f = 35;
   public static volatile boolean g = false;
   public static volatile boolean h = false;
   public static volatile boolean i = false;
   private static final Map<Integer, Long> l = new ConcurrentHashMap<>();
   public static volatile int j = -1;
   public static volatile long k = 0L;
   private static final ArrayDeque<InventoryUtil.SwapRecord> m = new ArrayDeque<>();
   private static final Set<Integer> n = ConcurrentHashMap.newKeySet();
   private static final ThreadLocal<Boolean> o = ThreadLocal.withInitial(() -> false);
   private static volatile int p = -1;

   public static boolean a() {
      return i && mc.player != null && mc.player.containerMenu != mc.player.inventoryMenu;
   }

   public static boolean a_bool_int(int var0) {
      Long var1 = l.remove(var0);
      boolean var2 = var1 != null && System.currentTimeMillis() < var1;
      if (var2) {
         j = var0;
         k = System.currentTimeMillis();
      }

      return var2;
   }

   public static void b_void_int(int var0) {
      n.add(var0);
   }

   public static void c_void_int(int var0) {
      n.remove(var0);
   }

   public static boolean b() {
      synchronized (m) {
         for (InventoryUtil.SwapRecord var2 : (Iterable<InventoryUtil.SwapRecord>)(Object)m) {
            if (!n.contains(var2.count())) {
               return true;
            }
         }

         return false;
      }
   }

   private static int h() {
      if (mc != null && mc.player != null && mc.player.getInventory() != null) {
         return mc.player.getInventory().getSelectedSlot();
      } else {
         return Night.positionManager != null ? Night.positionManager.getHeldSlot() : -1;
      }
   }

   public static void c() {
      if (mc.player == null) {
         synchronized (m) {
            m.clear();
         }

         n.clear();
      } else {
         boolean var0 = mc.options.keyUse.isDown() || mc.player.isUsingItem();
         if (var0 || mc.options.keyAttack.isDown()) {
            d();
         }

         if (var0) {
            g(mc.player.getInventory().getSelectedSlot());
         }

         synchronized (m) {
            if (m.removeIf(var0x -> !n.contains(var0x.count()) && System.currentTimeMillis() - var0x.timestamp() >= 30000L)) {
               i();
            }

            if (!var0) {
               i();
            }
         }
      }
   }

   public static void d() {
      synchronized (m) {
         i();
      }
   }

   public static void e() {
      synchronized (m) {
         m.removeIf(var0 -> !n.contains(var0.count()));
         i();
      }
   }

   private static void i() {
      InventoryUtil.SwapRecord var0 = m.peek();
      g(var0 != null ? var0.count2() : h());
   }

   private static void g(int var0) {
      if (var0 >= 0 && mc != null && mc.getConnection() != null) {
         if (Night.positionManager == null || var0 != Night.positionManager.getHeldSlot()) {
            boolean var1 = o.get();
            o.set(true);

            try {
               mc.getConnection().send(new ServerboundSetCarriedItemPacket(var0));
            } finally {
               o.set(var1);
            }
         }
      }
   }

   public static void f() {
      if (mc != null && mc.player != null && mc.getConnection() != null) {
         if (mc.player.inventoryMenu.getCarried().isEmpty() && mc.player.inventoryMenu.getCraftSlots().isEmpty()) {
            if (mc.player.containerMenu == mc.player.inventoryMenu) {
               if (!i) {
                  int var0 = mc.player.tickCount;
                  if (var0 != p) {
                     p = var0;
                     Connection var1 = mc.getConnection().getConnection();
                     Runnable var2 = () -> {
                        boolean var1x = o.get();
                        o.set(true);

                        try {
                           var1.send(new ServerboundContainerClosePacket(0));
                        } finally {
                           o.set(var1x);
                        }
                     };
                     Channel var3 = ((ConnectionAccessor)var1).getChannel();
                     if (var3 != null && var3.eventLoop().inEventLoop()) {
                        var3.eventLoop().execute(var2);
                     } else {
                        var2.run();
                     }
                  }
               }
            }
         }
      }
   }

   private static void e(int var0, int var1) {
      synchronized (m) {
         m.push(new InventoryUtil.SwapRecord(var0, var1, System.currentTimeMillis()));
         g(var1);
      }
   }

   private static void h(int var0) {
      synchronized (m) {
         boolean var2 = false;
         Iterator var3 = m.iterator();

         while (var3.hasNext()) {
            if (((InventoryUtil.SwapRecord)var3.next()).count() == var0) {
               var3.remove();
               var2 = true;
               break;
            }
         }

         if (var2) {
            InventoryUtil.SwapRecord var6 = m.peek();
            if (var6 != null) {
               g(var6.count2());
            } else {
               i();
            }
         }
      }
   }

   public static boolean a_bool_String_int_int(String var0, int var1, int var2) {
      if (var0.equalsIgnoreCase("None")) {
         return true;
      }

      if (var1 != -1 && var2 != -1) {
         o.set(true);

         try {
            if (!mc.isSameThread() && !var0.equalsIgnoreCase("Silent")) {
               if (var1 > d) {
                  return false;
               }

               var0 = "Silent";
            }

            switch (var0) {
               case "Normal":
                  mc.player.getInventory().setSelectedSlot(var1);
                  ((ClientPlayerInteractionManagerAccessor)mc.gameMode).invokeSyncSelectedSlot();
                  i(var1);
                  break;
               case "Silent":
                  e(var1, var1);
                  break;
               case "AltPickup":
               case "AltSwap":
                  if (var1 <= d) {
                     e(var1, var1);
                  } else {
                     c(var0.equals("AltPickup") ? "Pickup" : "Swap", var1, var2);
                     e(var1, var2);
                  }
            }

            return true;
         } finally {
            o.set(false);
         }
      } else {
         return false;
      }
   }

   public static void d(int var0) {
      if (var0 != -1 && var0 != Night.positionManager.getHeldSlot()) {
         o.set(true);

         try {
            mc.player.getInventory().setSelectedSlot(var0);
            ((ClientPlayerInteractionManagerAccessor)mc.gameMode).invokeSyncSelectedSlot();
            i(var0);
         } finally {
            o.set(false);
         }
      }
   }

   private static void i(int var0) {
      if (Night.pingBypassConfig != null && Night.pingBypassConfig.isServerMode()) {
         if (PingBypassState.d && Night.pingBypassServer != null) {
            ClientboundCustomPayloadPacket var1 = new ClientboundCustomPayloadPacket(PingBypassPayload.fromPacket(new S2CHeldSlotSyncPacket(var0)));

            for (Connection var3 : (Iterable<Connection>)(Object)Night.pingBypassServer.getConnections()) {
               if (var3.isConnected()) {
                  var3.send(var1);
               }
            }
         }
      }
   }

   public static void b(String var0, int var1, int var2) {
      if (!var0.equalsIgnoreCase("None")) {
         if (var2 != -1) {
            o.set(true);

            try {
               if ((var0.equals("AltPickup") || var0.equals("AltSwap")) && var1 > d && mc.isSameThread()) {
                  c(var0.equals("AltPickup") ? "Pickup" : "Swap", var1, var2);
               }

               h(var1);
            } finally {
               o.set(false);
            }
         }
      }
   }

   public static void a(int var0, int var1) {
      if (var0 >= 0 && var0 <= 8) {
         a_void_int_int_ContainerInput(var1, var0, ContainerInput.SWAP);
      } else {
         a_void_int_int_ContainerInput(e(var0), 0, ContainerInput.PICKUP);
         a_void_int_int_ContainerInput(var1, 0, ContainerInput.PICKUP);
         a_void_int_int_ContainerInput(e(var0), 0, ContainerInput.PICKUP);
      }
   }

   public static void c(String var0, int var1, int var2) {
      switch (var0) {
         case "Pickup":
            a_void_int_int_ContainerInput(e(var1), 0, ContainerInput.PICKUP);
            a_void_int_int_ContainerInput(e(var2), 0, ContainerInput.PICKUP);
            a_void_int_int_ContainerInput(e(var1), 0, ContainerInput.PICKUP);
            break;
         case "Swap":
            a_void_int_int_ContainerInput(e(var1), var2, ContainerInput.SWAP);
      }
   }

   public static void a_void_int_int_ContainerInput(int var0, int var1, ContainerInput var2) {
      mc.gameMode.handleContainerInput(mc.player.containerMenu.containerId, var0, var1, var2, mc.player);
      b(var0, var1, var2);
   }

   private static void b(int var0, int var1, ContainerInput var2) {
      if (Night.pingBypassConfig != null && Night.pingBypassConfig.isServerMode()) {
         if (PingBypassState.d && Night.pingBypassServer != null) {
            ClientboundCustomPayloadPacket var3 = new ClientboundCustomPayloadPacket(
               PingBypassPayload.fromPacket(new C2SWindowClickPacket(mc.player.containerMenu.containerId, var0, var1, var2))
            );

            for (Connection var5 : (Iterable<Connection>)(Object)Night.pingBypassServer.getConnections()) {
               if (var5.isConnected()) {
                  var5.send(var3);
               }
            }
         }
      }
   }

   public static int e(int var0) {
      AbstractContainerMenu var1 = mc.player != null ? mc.player.containerMenu : null;
      if (var1 != null && mc.player.inventoryMenu != var1 && var0 >= 0 && var0 <= 35) {
         int var2 = var1.slots.size() - 36;
         if (var2 >= 0) {
            return var0 <= 8 ? var2 + 27 + var0 : var2 + (var0 - 9);
         }
      }

      return var0 >= 0 && var0 <= 8 ? 36 + var0 : var0;
   }

   public static int a_int_Item(Item var0) {
      return a_int_Item_int_int(var0, c, f);
   }

   public static int b_int_Item(Item var0) {
      return a_int_Item_int_int(var0, c, d);
   }

   public static int c_int_Item(Item var0) {
      return a_int_Item_int_int(var0, e, f);
   }

   public static int a_int_Item_int_int(Item var0, int var1, int var2) {
      for (int var3 = var2; var3 >= var1; var3--) {
         if (mc.player.getInventory().getItem(var3).getItem() == var0) {
            return var3;
         }
      }

      return -1;
   }

   public static int b(int var0, int var1) {
      float var2 = -1.0F;
      int var3 = -1;

      for (int var4 = var0; var4 <= var1; var4++) {
         Item var5 = mc.player.getInventory().getItem(var4).getItem();
         if (var5 instanceof BlockItem) {
            float var6 = ((BlockItem)var5).getBlock().defaultDestroyTime();
            if (var6 == -1.0F) {
               return var4;
            }

            if (var6 > var2) {
               var2 = var6;
               var3 = var4;
            }
         }
      }

      return var3;
   }

   public static int a_int_BlockState_int_int(BlockState var0, int var1, int var2) {
      double var3 = -1.0;
      int var5 = -1;

      for (int var6 = var1; var6 <= var2; var6++) {
         double var7 = mc.player.getInventory().getItem(var6).getItem().getDestroySpeed(mc.player.getInventory().getItem(var6), var0);
         if (var7 > var3) {
            var3 = var7;
            var5 = var6;
         }
      }

      return var5;
   }

   public static int c(int var0, int var1) {
      int var2 = -1;
      int var3 = -1;
      int var4 = -1;
      int var5 = -1;
      int var6 = -1;
      int var7 = -1;

      for (int var8 = var1; var8 >= var0; var8--) {
         ItemStack var9 = mc.player.getInventory().getItem(var8);
         if (var9.getItem() == Items.NETHERITE_SWORD) {
            var2 = var8;
         }

         if (var9.getItem() == Items.DIAMOND_SWORD) {
            var3 = var8;
         }

         if (var9.getItem() == Items.IRON_SWORD) {
            var4 = var8;
         }

         if (var9.getItem() == Items.GOLDEN_SWORD) {
            var5 = var8;
         }

         if (var9.getItem() == Items.STONE_SWORD) {
            var6 = var8;
         }

         if (var9.getItem() == Items.WOODEN_SWORD) {
            var7 = var8;
         }
      }

      if (var2 != -1) {
         return var2;
      } else if (var3 != -1) {
         return var3;
      } else if (var4 != -1) {
         return var4;
      } else if (var5 != -1) {
         return var5;
      } else {
         return var6 != -1 ? var6 : var7;
      }
   }

   public static int d(int var0, int var1) {
      for (int var2 = var1; var2 >= var0; var2--) {
         if (mc.player.getInventory().getItem(var2).isEmpty()) {
            return var2;
         }
      }

      return -1;
   }

   public static boolean g() {
      return mc.gui.screen() instanceof InventoryScreen
         || mc.gui.screen() instanceof CreativeModeInventoryScreen
         || mc.gui.screen() instanceof ContainerScreen
         || mc.gui.screen() instanceof ShulkerBoxScreen;
   }

   public static double f(int var0) {
      ImageShaderUtil.a();
      return ImageShaderUtil.glGetShaderConstant(var0);
   }

   record SwapRecord(int count, int count2, long timestamp) {
   }
}
