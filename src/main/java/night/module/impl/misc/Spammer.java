package night.module.impl.misc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.multiplayer.ClientPacketListener;
import night.events.EventTarget;
import night.events.impl.TickEvent;
import night.module.Module;
import night.module.ModuleInfo;
import night.setting.impl.BooleanSetting;
import night.setting.impl.NumberSetting;
import night.setting.impl.StringSetting;
import night.util.io.FileUtil;
import night.util.math.MathUtil;
import night.util.math.TimerUtil;

@ModuleInfo(name = "Spammer", description = "Spams messages in chat from a text file.", category = Module.Category.MISCELLANEOUS)
public class Spammer extends Module {
   public StringSetting fileName = new StringSetting("FileName", "The name of the spammer text file.", "spammer.txt");
   public NumberSetting delay;
   public BooleanSetting greenText;
   public BooleanSetting shuffled;
   private final TimerUtil i;
   private List<String> j;
   private int k;

   public Spammer() {
      Integer var1 = 5;
      Integer var2 = 0;
      Integer var3 = 30;
      this.delay = new NumberSetting("Delay", "The delay for the announcer.", var1, var2, var3);
      this.greenText = new BooleanSetting("GreenText", "Makes your message green.", false);
      this.shuffled = new BooleanSetting("Shuffled", "Sends the spammer messages out of order.", false);
      this.i = new TimerUtil();
      this.j = new ArrayList<>();
   }

   @Override
   public void onEnable() {
      this.k = 0;
   }

   @EventTarget
   public void a(TickEvent var1) {
      if (!this.nullCheck()) {
         List var2 = FileUtil.a_List_File(new File("Night/Client/" + this.fileName.getValue()));
         this.j = var2;
         if (!this.j.isEmpty()) {
            TimerUtil var3 = this.i;
            Integer var4 = this.delay.getValue().intValue() * 1000;
            if (var3.passedMs(var4)) {
               int var5 = this.k;
               int var6 = this.j.size();
               if (var5 >= var6) {
                  this.k = 0;
               }

               String var13;
               if (this.shuffled.getValue()) {
                  List var7 = this.j;
                  double var8 = this.j.size();
                  double var10 = 0.0;
                  int var12 = (int)MathUtil.a_double_double_double(var8, var10);
                  var13 = (String)var7.get(var12);
               } else {
                  List var14 = this.j;
                  int var15 = this.k;
                  var13 = (String)var14.get(var15);
               }

               String var16 = var13;
               ClientPacketListener var17 = mc.player.connection;
               String var18;
               if (this.greenText.getValue()) {
                  var18 = "> ";
               } else {
                  var18 = "";
               }

               String var19 = var16;
               var17.sendChat(var18 + var19);
               int var20 = this.k + 1;
               this.k = var20;
               this.i.reset();
            }
         }
      }
   }
}
