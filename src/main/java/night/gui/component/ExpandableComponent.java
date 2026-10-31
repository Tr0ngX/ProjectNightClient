package night.gui.component;

import java.util.List;

public interface ExpandableComponent {
   String getSearchName();

   boolean isExpanded();

   float getExpandProgress();

   List<GuiComponent> getSubComponents();

   void setExpandedHeight(int var1);

   void setSearchQuery(String var1);
}
