package night.module.impl.misc;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.CompletableFuture;
import javax.imageio.ImageIO;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.ClickEvent.OpenFile;
import night.Night;
import night.module.Module;
import night.module.ModuleInfo;
import night.setting.impl.BooleanSetting;
import night.util.color.ChatColorUtil;
import night.util.io.ClipboardImageTransferable;
import night.util.world.PacketAndRenderUtil;

@ModuleInfo(
   name = "Screenshot",
   description = "Automatically copies screenshots to clipboard when pressing F2.",
   category = Module.Category.MISCELLANEOUS,
   enabled = true
)
public class ScreenshotModule extends Module {
   public BooleanSetting clipboard = new BooleanSetting("Clipboard", "Copies screenshot image directly to your clipboard.", true);
   public BooleanSetting copyFile = new BooleanSetting("CopyFile", "Also includes file flavor for pasting into file explorers.", true);
   public BooleanSetting notify = new BooleanSetting("Notify", "Sends a confirmation message in chat when copied.", true);
   public BooleanSetting openFolder = new BooleanSetting("OpenFolder", "Automatically opens the screenshots folder.", false);

   public void a_void_Component(Component var1) {
      if (this.isEnabled()) {
         File var2 = this.b(var1);
         if (var2 == null || !var2.exists()) {
            var2 = this.v();
         }

         if (var2 != null && var2.exists()) {
            File var3 = var2;
            CompletableFuture.runAsync(() -> {
               try {
                  boolean var2x = false;
                  if (this.clipboard.getValue()) {
                     BufferedImage var3x = ImageIO.read(var3);
                     if (var3x != null) {
                        BufferedImage var4 = var3x;
                        boolean var5 = this.copyFile.getValue();
                        ClipboardImageTransferable var6 = new ClipboardImageTransferable(var4, var3, var5);
                        Clipboard var7 = Toolkit.getDefaultToolkit().getSystemClipboard();
                        ClipboardImageTransferable var8 = var6;
                        var7.setContents(var8, null);
                        var2x = true;
                     }
                  }

                  if (!var2x && this.openFolder.getValue()) {
                     PacketAndRenderUtil.a_void_File(var3.getParentFile());
                  }

                  if (this.notify.getValue()) {
                     String var9 = String.valueOf(ChatColorUtil.a());
                     String var10 = var3.getName();
                     Night.chatManager.sendReplaceableMessage("Screenshot copied to clipboard: " + var9 + var10, "screenshot-copied");
                  }
               } catch (Throwable var12) {
                  Night.LOGGER.error("Failed to copy screenshot to clipboard", var12);
               }
            });
         }
      }
   }

   private File b(Component var1) {
      if (var1 == null) {
         return null;
      }

      Style var2 = var1.getStyle();
      if (var2 != null) {
         ClickEvent var3 = var2.getClickEvent();
         if (var3 instanceof OpenFile) {
            return ((OpenFile)var3).file();
         }
      }

      for (Component var5 : (Iterable<Component>)(Object)var1.getSiblings()) {
         File var6 = this.b(var5);
         if (var6 != null) {
            return var6;
         }
      }

      return null;
   }

   private File v() {
      try {
         File var1 = mc.gameDirectory;
         File var2 = new File(var1, "screenshots");
         if (var2.exists() && var2.isDirectory()) {
            File[] var3 = var2.listFiles((var0, var1x) -> var1x.toLowerCase().endsWith(".png"));
            return var3 != null && var3.length != 0 ? Arrays.stream(var3).max(Comparator.comparingLong(File::lastModified)).orElse(null) : null;
         } else {
            return null;
         }
      } catch (Throwable var6) {
         return null;
      }
   }
}
