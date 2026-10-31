package night.module.impl.core;

import lombok.Generated;
import net.minecraft.resources.Identifier;
import night.module.Module;
import night.module.ModuleInfo;

@ModuleInfo(
   name = "Capes",
   description = "Applies the Night cape to yourself and to other users.",
   category = Module.Category.CORE,
   enabled = true,
   drawn = false
)
public class Capes extends Module {
   private final Identifier e;

   public Capes() {
      Identifier var1 = Identifier.fromNamespaceAndPath("night", "cape");
      this.e = var1;
   }

   @Generated
   public Identifier v() {
      return this.e;
   }
}
