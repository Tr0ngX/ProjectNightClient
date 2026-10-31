package night.module.impl.core;

import night.module.Module;
import night.module.ModuleInfo;
import night.setting.impl.BooleanSetting;
import night.setting.impl.CategorySetting;
import night.setting.impl.ModeSetting;
import night.setting.impl.StringSetting;
import night.util.color.TextFormattingUtil;

@ModuleInfo(
   name = "Commands",
   description = "Manages the client's command and chat message system.",
   category = Module.Category.CORE,
   persistent = true,
   drawn = false
)
public class CommandsModule extends Module {
   public CategorySetting watermarkCategory = new CategorySetting("Watermark", "The category that contains all of the settings for the chat watermark.");
   public BooleanSetting watermark;
   public StringSetting watermarkText;
   public StringSetting opening;
   public StringSetting closing;
   public ModeSetting primaryWatermarkColor;
   public ModeSetting secondaryWatermarkColor;
   public CategorySetting messageCategory;
   public ModeSetting primaryMessageColor;
   public ModeSetting secondaryMessageColor;

   public CommandsModule() {
      CategorySetting var1 = this.watermarkCategory;
      this.watermark = new BooleanSetting(
         "Watermark",
         "Enabled",
         "Whether or not to have the client's watermark show up before every chat message.",
         new CategorySetting.CategoryCondition(var1),
         true
      );
      CategorySetting var3 = this.watermarkCategory;
      CategorySetting.CategoryCondition var2 = new CategorySetting.CategoryCondition(var3);
      this.watermarkText = new StringSetting("WatermarkText", "Text", "The text that will be shown as the watermark.", var2, "Night");
      CategorySetting var4 = this.watermarkCategory;
      this.opening = new StringSetting("Opening", "The symbol that will be placed before the watermark text.", new CategorySetting.CategoryCondition(var4), "[");
      CategorySetting var5 = this.watermarkCategory;
      this.closing = new StringSetting("Closing", "The symbol that will be placed after the watermark text.", new CategorySetting.CategoryCondition(var5), "]");
      CategorySetting var7 = this.watermarkCategory;
      CategorySetting.CategoryCondition var6 = new CategorySetting.CategoryCondition(var7);
      this.primaryWatermarkColor = new ModeSetting(
         "PrimaryWatermarkColor", "Primary", "The primary color for the watermark.", var6, "Client", TextFormattingUtil.a
      );
      CategorySetting var9 = this.watermarkCategory;
      CategorySetting.CategoryCondition var8 = new CategorySetting.CategoryCondition(var9);
      this.secondaryWatermarkColor = new ModeSetting(
         "SecondaryWatermarkColor", "Secondary", "The secondary color for the watermark.", var8, "Dark Gray", TextFormattingUtil.a
      );
      this.messageCategory = new CategorySetting("Message", "The category that contains all of the settings for the chat messages themselves.");
      CategorySetting var10 = this.messageCategory;
      this.primaryMessageColor = new ModeSetting(
         "PrimaryMessageColor", "Primary", "The primary color for the message.", new CategorySetting.CategoryCondition(var10), "Client", TextFormattingUtil.a
      );
      CategorySetting var11 = this.messageCategory;
      this.secondaryMessageColor = new ModeSetting(
         "SecondaryMessageColor",
         "Secondary",
         "The secondary color for the message.",
         new CategorySetting.CategoryCondition(var11),
         "White",
         TextFormattingUtil.a
      );
   }
}
