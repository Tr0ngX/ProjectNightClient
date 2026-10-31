package night.util.io;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.FloatControl.Type;

public class FileUtil {
   public static void a_void_String(String var0) throws java.io.IOException {
      String[] var1 = new String[0];
      Path var2 = Paths.get(var0, var1);
      LinkOption[] var3 = new LinkOption[0];
      if (Files.exists(var2, var3)) {
         new File(var0).delete();
      }

      String[] var4 = new String[0];
      Path var5 = Paths.get(var0, var4);
      FileAttribute[] var6 = new FileAttribute[0];
      Files.createFile(var5, var6);
   }

   public static void b(String var0) throws java.io.IOException {
      String[] var1 = new String[0];
      Path var2 = Paths.get(var0, var1);
      LinkOption[] var3 = new LinkOption[0];
      if (!Files.exists(var2, var3)) {
         String[] var4 = new String[0];
         Path var5 = Paths.get(var0, var4);
         FileAttribute[] var6 = new FileAttribute[0];
         Files.createDirectories(var5, var6);
      }
   }

   public static boolean c(String var0) {
      String[] var1 = new String[0];
      Path var2 = Paths.get(var0, var1);
      LinkOption[] var3 = new LinkOption[0];
      return Files.exists(var2, var3);
   }

   public static void a_void_File_float(File var0, float var1) {
      if (var0.exists()) {
         try {
            Clip var2 = AudioSystem.getClip();
            AudioInputStream var3 = AudioSystem.getAudioInputStream(var0);
            var2.open(var3);
            FloatControl var4 = (FloatControl)var2.getControl(Type.MASTER_GAIN);
            float var5 = (float)Math.log10(var1);
            float var6 = 20.0F * var5;
            var4.setValue(var6);
            var2.start();
            var2.addLineListener(var1x -> {
               if (var1x.getType() == javax.sound.sampled.LineEvent.Type.STOP) {
                  var2.close();
               }
            });
         } catch (Exception var7) {
            var7.printStackTrace();
         }
      }
   }

   public static List<String> a_List_File(File var0) {
      List var1 = new ArrayList();
      if (var0.exists()) {
         try {
            String var2 = var0.getPath();
            String[] var3 = new String[0];

            try (Stream<String> var4 = Files.lines(Paths.get(var2, var3))) {
               var1 = var4.filter(var0x -> {
                  boolean var1x;
                  if (!var0x.isEmpty()) {
                     var1x = true;
                  } else {
                     var1x = false;
                  }

                  return var1x;
               }).toList();
            }
         } catch (Exception var12) {
         }
      }

      return var1;
   }
}
