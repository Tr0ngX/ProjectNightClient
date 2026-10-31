package night.module.impl.misc;

import java.awt.Color;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import night.Night;
import night.module.Module;
import night.module.ModuleInfo;
import night.util.render.Render2DUtil;

@ModuleInfo(name = "ShulkerInfo", description = "Renders a preview of the contents of the shulker you are hovering.", category = Module.Category.MISCELLANEOUS)
public class ShulkerInfo extends Module {
   public void a(GuiGraphicsExtractor var1, int var2, int var3, ItemStack var4) {
      try {
         ItemContainerContents var5 = (ItemContainerContents)var4.get(DataComponents.CONTAINER);
         Item var6 = var4.getItem();
         Color var7 = Color.WHITE;
         if (var6 instanceof BlockItem && ((BlockItem)var6).getBlock() instanceof ShulkerBoxBlock var9) {
            try {
               int var10 = var9.getColor().getTextureDiffuseColor();
               var7 = new Color(var10);
            } catch (Exception var53) {
            }
         }

         int var12 = var2 + 4;
         int var13 = Night.fontManager.getFontHeight();
         int var14 = 62 + var13;
         int var15 = var3 - var14;
         float var16 = var12;
         float var17 = var15;
         float var18 = var12 + 164;
         int var19 = var15 + 60;
         int var20 = Night.fontManager.getFontHeight();
         float var21 = var19 + var20;
         Color var22 = new Color(0, 0, 0, 200);
         Render2DUtil.a(var1, var16, var17, var18, var21, var22);
         float var23 = var12;
         float var24 = var15;
         float var25 = var12 + 164;
         int var26 = var15 + 60;
         int var27 = Night.fontManager.getFontHeight();
         float var28 = var26 + var27;
         Color var29 = var7;
         Render2DUtil.b(var1, var23, var24, var25, var28, var29);
         String var30 = var4.getHoverName().getString();
         int var31 = var12 + 3;
         int var32 = var15 + 3;
         Night.fontManager.drawShadowedText(var1, var30, var31, var32, Color.WHITE);
         int var33 = 0;
         int var34 = 0;

         for (ItemStack var36 : (Iterable<ItemStack>)(Object)var5.nonEmptyItemCopyStream().toList()) {
            int var37 = var12 + 1;
            int var38 = var34 * 18;
            int var39 = var37 + var38;
            int var40 = var15;
            int var41 = Night.fontManager.getFontHeight();
            int var42 = var40 + var41 + 5;
            int var43 = var33 * 18;
            int var44 = var42 + var43;
            ItemStack var45 = var36;
            int var46 = var39;
            int var47 = var44;
            var1.item(var45, var46, var47);
            Font var48 = mc.font;
            ItemStack var49 = var36;
            int var50 = var39;
            int var51 = var44;
            var1.itemDecorations(var48, var49, var50, var51);
            if (++var34 >= 9) {
               var34 = 0;
               var33++;
            }
         }
      } catch (Exception var54) {
      }
   }

   public boolean a(ItemStack var1) {
      ItemContainerContents var2 = (ItemContainerContents)var1.get(DataComponents.CONTAINER);
      boolean var3;
      if (var2 != null && !var2.nonEmptyItemCopyStream().toList().isEmpty()) {
         var3 = true;
      } else {
         var3 = false;
      }

      return var3;
   }
}
