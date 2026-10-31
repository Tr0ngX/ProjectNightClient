package night.module.impl.movement;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.JigsawBlockEditScreen;
import net.minecraft.client.gui.screens.inventory.SignEditScreen;
import net.minecraft.client.gui.screens.inventory.StructureBlockEditScreen;
import net.minecraft.world.item.CreativeModeTab.Type;
import night.events.EventTarget;
import night.events.impl.TickEvent;
import night.mixins.accessors.CreativeInventoryScreenAccessor;
import night.module.Module;
import night.module.ModuleInfo;
import night.setting.impl.BooleanSetting;
import night.util.world.PacketAndRenderUtil;

@ModuleInfo(
   name = "InventoryControl",
   description = "Allows you to control things such as movement and your camera while a GUI is open.",
   category = Module.Category.MOVEMENT
)
public class InventoryControl extends Module {
   public BooleanSetting movement = new BooleanSetting("Movement", "Allows you to control movement.", true);
   public BooleanSetting portals = new BooleanSetting("Portals", "Allows you to interact with GUIs while inside of a portal.", true);
   public BooleanSetting dragClick = new BooleanSetting("DragClick", "Hold shift + left-click and drag over slots to move each item you pass over.", true);
   public BooleanSetting grimV2 = new BooleanSetting("GrimV2", "Bypasses inventory-while-moving checks on GrimV2/NCP servers.", false);

   @EventTarget
   public void a(TickEvent var1) {
      if (mc.player != null) {
         if (this.movement.getValue()
            && mc.gui.screen() != null
            && !(mc.gui.screen() instanceof ChatScreen)
            && !(mc.gui.screen() instanceof BookEditScreen)
            && !(mc.gui.screen() instanceof SignEditScreen)
            && !(mc.gui.screen() instanceof JigsawBlockEditScreen)
            && !(mc.gui.screen() instanceof StructureBlockEditScreen)
            && !(mc.gui.screen() instanceof AnvilScreen)
            && (!(mc.gui.screen() instanceof CreativeModeInventoryScreen) || CreativeInventoryScreenAccessor.getSelectedTab().getType() != Type.SEARCH)) {
            KeyMapping[] var2 = new KeyMapping[7];
            KeyMapping var3 = mc.options.keyUp;
            var2[0] = var3;
            KeyMapping var4 = mc.options.keyDown;
            var2[1] = var4;
            KeyMapping var5 = mc.options.keyRight;
            var2[2] = var5;
            KeyMapping var6 = mc.options.keyLeft;
            var2[3] = var6;
            KeyMapping var7 = mc.options.keySprint;
            var2[4] = var7;
            KeyMapping var8 = mc.options.keyShift;
            var2[5] = var8;
            KeyMapping var9 = mc.options.keyJump;
            var2[6] = var9;
            KeyMapping[] var10 = var2;
            int var11 = var10.length;
            int var12 = 0;

            while (true) {
               int var13 = var12;
               int var14 = var11;
               if (var13 >= var14) {
                  break;
               }

               KeyMapping[] var15 = var10;
               int var16 = var12;
               KeyMapping var17 = var15[var16];
               boolean var18 = PacketAndRenderUtil.a_bool_int(InputConstants.getKey(var17.saveString()).getValue());
               var17.setDown(var18);
               var12++;
            }
         }
      }
   }
}
