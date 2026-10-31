package night.module.impl.misc;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import lombok.Generated;
import net.minecraft.client.multiplayer.chat.GuiMessage.Line;
import net.minecraft.util.FormattedCharSequence;
import night.module.Module;
import night.module.ModuleInfo;
import night.setting.impl.BooleanSetting;
import night.setting.impl.ColorSetting;
import night.setting.impl.ModeSetting;
import night.setting.impl.NumberSetting;
import night.setting.impl.StringSetting;

@ModuleInfo(name = "BetterChat", description = "Improves the default minecraft chat.", category = Module.Category.MISCELLANEOUS)
public class BetterChat extends Module {
   public BooleanSetting infinite = new BooleanSetting("Infinite", "Allows chat history to be saved indefinitely without being trimmed by Minecraft.", true);
   public BooleanSetting noIndicators = new BooleanSetting("NoIndicators", "Removes indicators from the chat.", false);
   public NumberSetting offset;
   public BooleanSetting animation2;
   public NumberSetting delay;
   public NumberSetting textAlpha;
   public BooleanSetting timestamps;
   public StringSetting opening;
   public StringSetting closing;
   public ModeSetting background;
   public ColorSetting color;
   public BooleanSetting highlight;
   public ColorSetting highlightColor;
   public BooleanSetting highlightSound;
   private final Map<Line, Long> s;

   public BetterChat() {
      Integer var1 = 0;
      Integer var2 = -100;
      Integer var3 = 100;
      this.offset = new NumberSetting("Offset", "The offset for the chat on the X axis.", var1, var2, var3);
      this.animation2 = new BooleanSetting("Animation", "Adds in an animation when adding new messages.", false);
      BooleanSetting var5 = this.animation2;
      BooleanSetting.BooleanCondition var4 = new BooleanSetting.BooleanCondition(var5, true);
      Integer var6 = 200;
      Integer var7 = 0;
      Integer var8 = 300;
      this.delay = new NumberSetting("Delay", "The delay of the animation.", var4, var6, var7, var8);
      Integer var9 = 255;
      Integer var10 = 0;
      Integer var11 = 255;
      this.textAlpha = new NumberSetting("TextAlpha", "The alpha of the chat text.", var9, var10, var11);
      this.timestamps = new BooleanSetting("Timestamps", "Adds timestamps to every chat message.", true);
      BooleanSetting var12 = this.timestamps;
      this.opening = new StringSetting(
         "Opening", "The symbol that will be placed before the timestamp text.", new BooleanSetting.BooleanCondition(var12, true), "<"
      );
      BooleanSetting var13 = this.timestamps;
      this.closing = new StringSetting(
         "Closing", "The symbol that will be placed after the timestamp text.", new BooleanSetting.BooleanCondition(var13, true), ">"
      );
      String[] var14 = new String[]{"Default", "Clear", "Custom"};
      this.background = new ModeSetting("Background", "The background mode for the chat.", "Default", var14);
      ModeSetting var16 = this.background;
      String[] var17 = new String[]{"Custom"};
      ModeSetting.ModeCondition var15 = new ModeSetting.ModeCondition(var16, var17);
      Color var18 = new Color(0, 0, 0, 127);
      this.color = new ColorSetting("Color", "The color of the chat background.", var15, new ColorSetting.ColorValue(var18, false, false));
      this.highlight = new BooleanSetting("Highlight", "Highlights your name in incoming chat messages.", true);
      BooleanSetting var20 = this.highlight;
      BooleanSetting.BooleanCondition var19 = new BooleanSetting.BooleanCondition(var20, true);
      Color var21 = new Color(85, 255, 255);
      this.highlightColor = new ColorSetting(
         "HighlightColor", "The color used to highlight your name in chat.", var19, new ColorSetting.ColorValue(var21, false, false)
      );
      BooleanSetting var22 = this.highlight;
      this.highlightSound = new BooleanSetting(
         "HighlightSound", "Plays a ding notification sound when your name is mentioned in chat.", new BooleanSetting.BooleanCondition(var22, true), true
      );
      this.s = new HashMap<>();
   }

   public Long a(FormattedCharSequence var1) {
      for (Entry var3 : (Iterable<Entry>)(Object)this.s.entrySet()) {
         if (((Line)var3.getKey()).content() == var1) {
            return (Long)var3.getValue();
         }
      }

      return null;
   }

   @Generated
   public Map<Line, Long> v() {
      return this.s;
   }
}
