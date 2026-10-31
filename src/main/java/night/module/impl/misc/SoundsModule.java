package night.module.impl.misc;

import java.io.File;
import java.io.IOException;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import night.events.EventTarget;
import night.events.impl.AttackEntityEvent;
import night.events.impl.TargetDeathEvent;
import night.module.Module;
import night.module.ModuleInfo;
import night.setting.impl.BooleanSetting;
import night.setting.impl.CategorySetting;
import night.setting.impl.NumberSetting;
import night.setting.impl.StringSetting;
import night.util.io.FileUtil;

@ModuleInfo(
   name = "Sounds",
   description = "Plays custom sounds when something happens. Put .wav files in .minecraft/Night/Client/",
   category = Module.Category.MISCELLANEOUS
)
public class SoundsModule extends Module {
   private static final String m = "Night/Client/";
   CategorySetting killsCategory = new CategorySetting("Kills", "The kills category for the sounds.");
   BooleanSetting killSound;
   NumberSetting killVolume;
   StringSetting killName;
   CategorySetting hitsCategory;
   BooleanSetting hitSound;
   NumberSetting hitVolume;
   StringSetting hitName;

   public SoundsModule() {
      CategorySetting var1 = this.killsCategory;
      this.killSound = new BooleanSetting("KillSound", "Play sounds when you kill a player.", new CategorySetting.CategoryCondition(var1), true);
      CategorySetting var3 = this.killsCategory;
      CategorySetting.CategoryCondition var2 = new CategorySetting.CategoryCondition(var3);
      Float var4 = 1.0F;
      Float var5 = 0.0F;
      Float var6 = 1.0F;
      this.killVolume = new NumberSetting("KillVolume", "Volume", "The volume for the kill sounds.", var2, var4, var5, var6);
      CategorySetting var7 = this.killsCategory;
      this.killName = new StringSetting(
         "KillName",
         "Name",
         "The name of the kill sound file. Put it in .minecraft/Night/Client/",
         new CategorySetting.CategoryCondition(var7),
         "killsound.wav"
      );
      this.hitsCategory = new CategorySetting("Hits", "The hits category for the sounds.");
      CategorySetting var8 = this.hitsCategory;
      this.hitSound = new BooleanSetting("HitSound", "Play sounds when you hit an entity.", new CategorySetting.CategoryCondition(var8), true);
      CategorySetting var10 = this.hitsCategory;
      CategorySetting.CategoryCondition var9 = new CategorySetting.CategoryCondition(var10);
      Float var11 = 1.0F;
      Float var12 = 0.0F;
      Float var13 = 1.0F;
      this.hitVolume = new NumberSetting("HitVolume", "Volume", "The volume for the hit sounds.", var9, var11, var12, var13);
      CategorySetting var14 = this.hitsCategory;
      this.hitName = new StringSetting(
         "HitName", "Name", "The name of the hit sound file. Put it in .minecraft/Night/Client/", new CategorySetting.CategoryCondition(var14), "hitsound.wav"
      );
   }

   @Override
   public void onEnable() {
      try {
         FileUtil.b("Night/Client/");
      } catch (IOException var2) {
      }
   }

   @EventTarget
   public void a(TargetDeathEvent var1) {
      if (!this.nullCheck()) {
         if (this.killSound.getValue()) {
            File var2 = new File("Night/Client/" + this.killName.getValue());
            float var3 = this.killVolume.getValue().floatValue();
            FileUtil.a_void_File_float(var2, var3);
         }
      }
   }

   @EventTarget
   public void a(AttackEntityEvent var1) {
      Player var2 = var1.getPlayer();
      LocalPlayer var3 = mc.player;
      if (var2 == var3) {
         if (this.hitSound.getValue()) {
            File var4 = new File("Night/Client/" + this.hitName.getValue());
            float var5 = this.hitVolume.getValue().floatValue();
            FileUtil.a_void_File_float(var4, var5);
         }
      }
   }
}
