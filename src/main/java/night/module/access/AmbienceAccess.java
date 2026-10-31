package night.module.access;

import java.awt.Color;

public interface AmbienceAccess {
   boolean isEnabled();

   Color getAmbienceColor();

   boolean isLightmapEnabled();

   boolean isSkyEnabled();

   boolean isBlocksEnabled();
}
