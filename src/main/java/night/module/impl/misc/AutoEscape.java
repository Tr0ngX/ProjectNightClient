package night.module.impl.misc;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import night.Night;
import night.events.EventTarget;
import night.events.impl.TickEvent;
import night.module.Module;
import night.module.ModuleInfo;
import night.setting.impl.BooleanSetting;
import night.setting.impl.NumberSetting;
import night.util.inventory.InventoryUtil;
import night.util.math.TimerUtil;

@ModuleInfo(
   name = "AutoEscape",
   description = "Eats a chorus fruit to escape as soon as you get trapped on all sides.",
   category = Module.Category.MISCELLANEOUS
)
public class AutoEscape extends Module {
   public BooleanSetting swapBack = new BooleanSetting("SwapBack", "Switches back to the item you were previously holding after eating the chorus fruit.", true);
   public NumberSetting cooldown;
   public BooleanSetting autoDisable;
   private static final long h = 1600L;
   private final TimerUtil i;
   private boolean enabled;
   private long k;
   private int l;

   public AutoEscape() {
      Integer var1 = 1000;
      Integer var2 = 0;
      Integer var3 = 5000;
      this.cooldown = new NumberSetting("Cooldown", "The amount of time that has to pass before escaping again.", var1, var2, var3);
      this.autoDisable = new BooleanSetting("AutoDisable", "Disables the module after it escapes once.", false);
      this.i = new TimerUtil();
      this.enabled = false;
      long var4 = 0L;
      this.k = var4;
      this.l = -1;
   }

   @EventTarget
   public void a_void_TickEvent(TickEvent var1) {
      if (!this.nullCheck()) {
         if (this.enabled) {
            long var2 = System.currentTimeMillis();
            long var4 = this.k;
            long var6 = var2 - var4;
            long var8 = 1600L;
            if (var6 >= var8) {
               if (this.swapBack.getValue()) {
                  InventoryUtil.d(this.l);
               }

               this.enabled = false;
               this.i.reset();
               String var10 = this.getName();
               Night.chatManager.sendTaggedMessage("Escaped by eating a chorus fruit.", var10);
               if (this.autoDisable.getValue()) {
                  this.setEnabled(false, true);
               }
            }
         } else {
            TimerUtil var11 = this.i;
            Long var12 = this.cooldown.getValue().longValue();
            if (var11.passedMs(var12)) {
               if (this.w()) {
                  int var13 = InventoryUtil.b_int_Item(Items.CHORUS_FRUIT);
                  if (var13 != -1) {
                     int var14 = mc.player.getInventory().getSelectedSlot();
                     this.l = var14;
                     int var15 = var13;
                     int var16 = this.l;
                     if (var15 != var16) {
                        int var17 = var13;
                        int var18 = this.l;
                        InventoryUtil.a_bool_String_int_int("Normal", var17, var18);
                     }

                     MultiPlayerGameMode var19 = mc.gameMode;
                     LocalPlayer var20 = mc.player;
                     var19.useItem(var20, InteractionHand.MAIN_HAND);
                     this.enabled = true;
                     long var21 = System.currentTimeMillis();
                     this.k = var21;
                  }
               }
            }
         }
      }
   }

   public boolean v() {
      return this.enabled;
   }

   @Override
   public void onDisable() {
      this.enabled = false;
   }

   private boolean w() {
      BlockPos var1 = mc.player.blockPosition().above();
      BlockPos var2 = var1.above();
      if (this.a_bool_BlockPos(var2)) {
         BlockPos var3 = var1.north();
         if (this.a_bool_BlockPos(var3)) {
            BlockPos var4 = var1.south();
            if (this.a_bool_BlockPos(var4)) {
               BlockPos var5 = var1.east();
               if (this.a_bool_BlockPos(var5)) {
                  BlockPos var6 = var1.west();
                  if (this.a_bool_BlockPos(var6)) {
                     return true;
                  }
               }
            }
         }
      }

      return false;
   }

   private boolean a_bool_BlockPos(BlockPos var1) {
      BlockState var2 = mc.level.getBlockState(var1);
      ClientLevel var3 = mc.level;
      boolean var4;
      if (!var2.getCollisionShape(var3, var1).isEmpty()) {
         var4 = true;
      } else {
         var4 = false;
      }

      return var4;
   }
}
