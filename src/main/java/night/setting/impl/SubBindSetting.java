package night.setting.impl;

import lombok.Generated;
import net.minecraft.ChatFormatting;
import night.Night;
import night.manager.ChatManager;
import night.setting.Setting;
import night.util.color.ChatColorUtil;
import night.util.input.KeyNameUtil;

public class SubBindSetting extends KeybindSetting {
   private final BooleanSetting targetSetting;

   public SubBindSetting(String var1, String var2, BooleanSetting var3) {
      super(var1, var2, 0);
      this.targetSetting = var3;
      this.disableHoldMode();
   }

   public SubBindSetting(String var1, String var2, String var3, BooleanSetting var4) {
      super(var1, var2, var3, 0);
      this.targetSetting = var4;
      this.disableHoldMode();
   }

   public SubBindSetting(String var1, String var2, Setting.VisibilityCondition var3, BooleanSetting var4) {
      super(var1, var2, var3, 0);
      this.targetSetting = var4;
      this.disableHoldMode();
   }

   public void trigger() {
      BooleanSetting var1 = this.targetSetting;
      boolean var2;
      if (!this.targetSetting.getValue()) {
         var2 = true;
      } else {
         var2 = false;
      }

      var1.setValue(var2);
      ChatManager var3 = Night.chatManager;
      String var4 = String.valueOf(ChatColorUtil.a());
      String var5 = this.targetSetting.getDisplayName();
      String var6 = String.valueOf(ChatColorUtil.b());
      String var7;
      if (this.targetSetting.getValue()) {
         var7 = ChatFormatting.GREEN + "true";
      } else {
         var7 = ChatFormatting.RED + "false";
      }

      String var8 = String.valueOf(ChatColorUtil.b());
      var3.sendReplaceableMessage(var4 + var5 + var6 + " = " + var7 + var8 + ";", "toggle-subbind-" + this.getName().toLowerCase());
   }

   public void notifyBindChange() {
      if (this.getKey() == 0) {
         String var1 = String.valueOf(ChatColorUtil.a());
         String var2 = this.targetSetting.getDisplayName();
         String var3 = String.valueOf(ChatColorUtil.b());
         Night.chatManager
            .sendReplaceableTaggedMessage("Successfully reset the " + var1 + var2 + var3 + " keybind.", "Bind", "subbind-" + this.getName().toLowerCase());
      } else {
         String var4 = String.valueOf(ChatColorUtil.a());
         String var5 = this.targetSetting.getDisplayName();
         String var6 = String.valueOf(ChatColorUtil.b());
         String var7 = String.valueOf(ChatColorUtil.a());
         String var8 = KeyNameUtil.a_String_int(this.getKey());
         String var9 = String.valueOf(ChatColorUtil.b());
         Night.chatManager
            .sendReplaceableTaggedMessage(
               "Successfully bound " + var4 + var5 + var6 + " to the " + var7 + var8 + var9 + " key.", "Bind", "subbind-" + this.getName().toLowerCase()
            );
      }
   }

   @Generated
   public BooleanSetting getTargetSetting() {
      return this.targetSetting;
   }
}
