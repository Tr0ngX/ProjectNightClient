package night.util.io;

import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.util.Collections;

public class ClipboardImageTransferable implements Transferable {
   private final Image a;
   private final File b;
   private final boolean c;

   public ClipboardImageTransferable(Image var1, File var2, boolean var3) {
      this.a = var1;
      this.b = var2;
      this.c = var3;
   }

   @Override
   public DataFlavor[] getTransferDataFlavors() {
      return this.c && this.b != null ? new DataFlavor[]{DataFlavor.imageFlavor, DataFlavor.javaFileListFlavor} : new DataFlavor[]{DataFlavor.imageFlavor};
   }

   @Override
   public boolean isDataFlavorSupported(DataFlavor var1) {
      if (DataFlavor.imageFlavor.equals(var1)) {
         boolean var2;
         if (this.a != null) {
            var2 = true;
         } else {
            var2 = false;
         }

         return var2;
      } else {
         return this.c && this.b != null && DataFlavor.javaFileListFlavor.equals(var1);
      }
   }

   @Override
   public Object getTransferData(DataFlavor var1) throws UnsupportedFlavorException {
      if (DataFlavor.imageFlavor.equals(var1) && this.a != null) {
         return this.a;
      } else if (this.c && this.b != null && DataFlavor.javaFileListFlavor.equals(var1)) {
         return Collections.singletonList(this.b);
      } else {
         throw new UnsupportedFlavorException(var1);
      }
   }
}
