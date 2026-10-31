package night.util.render;

import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.item.TrackingItemStackRenderState;
import net.minecraft.client.renderer.state.gui.GuiItemRenderState;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import night.mixins.accessors.GuiGraphicsExtractorAccessor;
import org.joml.Matrix3x2f;

public class ItemRenderCache {
   private static final ItemRenderCache.CachedItemState[] a = new ItemRenderCache.CachedItemState[10];

   public static void a() {
      ItemRenderCache.CachedItemState[] var0 = a;
      int var1 = var0.length;

      for (int var2 = 0; var2 < var1; var2++) {
         var0[var2].a();
      }
   }

   public static void a(GuiGraphicsExtractor var0, LivingEntity var1, ItemStack var2, int var3, int var4, int var5) {
      if (!var2.isEmpty()) {
         Minecraft var6 = Minecraft.getInstance();
         GuiRenderState var7 = ((GuiGraphicsExtractorAccessor)var0).night$getGuiRenderState();
         int var8 = var5 >= 1 && var5 <= 10 ? var5 - 1 : -1;
         Object var9 = null;
         TrackingItemStackRenderState var11;
         if (var8 >= 0 && var8 < a.length) {
            ItemRenderCache.CachedItemState var10 = a[var8];
            if (var10.a(var2, var5)) {
               var11 = var10.f;
            } else {
               var11 = new TrackingItemStackRenderState();
               if (var6.getItemModelResolver() != null && var1 != null && var1.level() != null) {
                  var6.getItemModelResolver().updateForTopItem(var11, var2, ItemDisplayContext.GUI, var1.level(), var1, var5);
               }

               var10.a(var2, var5, var11);
            }
         } else {
            var11 = new TrackingItemStackRenderState();
            if (var6.getItemModelResolver() != null && var1 != null && var1.level() != null) {
               var6.getItemModelResolver().updateForTopItem(var11, var2, ItemDisplayContext.GUI, var1.level(), var1, var5);
            }
         }

         if (var7 != null && var11 != null) {
            Matrix3x2f var12 = new Matrix3x2f(var0.pose());
            var7.addItem(new GuiItemRenderState(var12, var11, var3, var4, (ScreenRectangle)null));
         }
      }
   }

   static {
      for (int var0 = 0; var0 < a.length; var0++) {
         a[var0] = new ItemRenderCache.CachedItemState();
      }
   }

   public static class CachedItemState {
      public Item a;
      public int b;
      public int c;
      public Object d;
      public int e;
      public TrackingItemStackRenderState f;

      public boolean a(ItemStack var1, int var2) {
         if (var1 == null || var1.isEmpty() || this.f == null) {
            return false;
         } else {
            return this.f.isAnimated()
               ? false
               : var1.getItem() == this.a
                  && var1.getCount() == this.b
                  && var1.getDamageValue() == this.c
                  && Objects.equals(var1.getComponents(), this.d)
                  && this.e == var2;
         }
      }

      public void a(ItemStack var1, int var2, TrackingItemStackRenderState var3) {
         this.a = var1.getItem();
         this.b = var1.getCount();
         this.c = var1.getDamageValue();
         this.d = var1.getComponents();
         this.e = var2;
         this.f = var3;
      }

      public void a() {
         this.a = null;
         this.b = 0;
         this.c = 0;
         this.d = null;
         this.e = 0;
         this.f = null;
      }
   }
}
