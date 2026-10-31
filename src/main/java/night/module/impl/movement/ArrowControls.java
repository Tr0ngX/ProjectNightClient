package night.module.impl.movement;

import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.JigsawBlockEditScreen;
import net.minecraft.client.gui.screens.inventory.SignEditScreen;
import net.minecraft.client.gui.screens.inventory.StructureBlockEditScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.CreativeModeTab.Type;
import night.events.EventTarget;
import night.events.impl.Render3DEvent;
import night.mixins.accessors.CreativeInventoryScreenAccessor;
import night.module.Module;
import night.module.ModuleInfo;
import night.setting.impl.BooleanSetting;
import night.setting.impl.NumberSetting;
import night.util.world.PacketAndRenderUtil;

@ModuleInfo(name = "ArrowControls", description = "Allows you to control the camera using your arrow keys.", category = Module.Category.MOVEMENT)
public class ArrowControls extends Module {
   public NumberSetting speed;
   public BooleanSetting inventory;
   private long g;

   public ArrowControls() {
      Float var1 = 1.0F;
      Float var2 = 0.1F;
      Float var3 = 10.0F;
      this.speed = new NumberSetting("Speed", "The speed at which the camera will be moving at.", var1, var2, var3);
      this.inventory = new BooleanSetting("Inventory", "Allows you to control the camera when a screen is opened.", true);
   }

   @EventTarget
   public void a(Render3DEvent var1) {
      if (mc.player != null && mc.level != null) {
         if (mc.gui.screen() == null
            || this.inventory.getValue()
               && !(mc.gui.screen() instanceof ChatScreen)
               && !(mc.gui.screen() instanceof BookEditScreen)
               && !(mc.gui.screen() instanceof SignEditScreen)
               && !(mc.gui.screen() instanceof JigsawBlockEditScreen)
               && !(mc.gui.screen() instanceof StructureBlockEditScreen)
               && !(mc.gui.screen() instanceof AnvilScreen)
               && (!(mc.gui.screen() instanceof CreativeModeInventoryScreen) || CreativeInventoryScreenAccessor.getSelectedTab().getType() != Type.SEARCH)) {
            float var2 = 0.0F;
            float var3 = 0.0F;
            long var4 = System.currentTimeMillis();
            long var6 = this.g;
            float var8 = (float)(var4 - var6) / 10.0F;
            float var9 = this.speed.getValue().floatValue();
            float var10 = var8 * var9;
            long var11 = System.currentTimeMillis();
            this.g = var11;
            if (PacketAndRenderUtil.a_bool_int(263)) {
               float var13 = var2;
               float var14 = var10;
               var2 = var13 - var14;
            }

            if (PacketAndRenderUtil.a_bool_int(262)) {
               float var15 = var2;
               float var16 = var10;
               var2 = var15 + var16;
            }

            if (PacketAndRenderUtil.a_bool_int(265)) {
               float var17 = var3;
               float var18 = var10;
               var3 = var17 - var18;
            }

            if (PacketAndRenderUtil.a_bool_int(264)) {
               float var19 = var3;
               float var20 = var10;
               var3 = var19 + var20;
            }

            LocalPlayer var21 = mc.player;
            float var22 = mc.player.getYRot();
            float var23 = var2;
            float var24 = var22 + var23;
            var21.setYRot(var24);
            LocalPlayer var25 = mc.player;
            float var26 = mc.player.getXRot();
            float var27 = var3;
            float var28 = Mth.clamp(var26 + var27, -90.0F, 90.0F);
            var25.setXRot(var28);
         }
      }
   }

   @Override
   public String getDisplayInfo() {
      return String.valueOf(this.speed.getValue().floatValue());
   }
}
