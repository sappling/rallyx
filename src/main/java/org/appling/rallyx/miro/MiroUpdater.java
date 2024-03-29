package org.appling.rallyx.miro;

import org.appling.rallyx.miro.widget.*;
import org.appling.rallyx.rally.RallyNode;
import org.appling.rallyx.rally.StoryStats;

import java.io.IOException;
import java.util.*;

public class MiroUpdater extends MiroWriter {
   private final HashSet<RallyNode> updatedNodes = new HashSet<>();

   public MiroUpdater(StoryStats stats, String authToken, String boardId, String targetId, String fieldsToShow, String rallyOptions) {
      super(stats, authToken, boardId, targetId, fieldsToShow, rallyOptions, new HashSet<>());
   }

   public void update() throws IOException {
      MiroFrame frame = connector.getWidget(targetId, false);

      HashMap<String,MiroWidget> foundWidgets = new HashMap<>();
      for (String child : frame.children) {
         MiroWidget widget = connector.getWidget(sanitizeFrameChild(child), false);
         FoundWidget w = new FoundWidget(widget);
         if (w.hasRallyId()) {
            foundWidgets.put(w.getRallyId(), widget);
         }
      }

      initializeTarget();

      for (Map.Entry<String, MiroWidget> nextEntry : foundWidgets.entrySet()) {
         RallyNode node = stats.getNodeByFormattedId(nextEntry.getKey());
         if (node != null) {
            updatedNodes.add(node);
            handleNode(node, nextEntry.getValue().id, false);
         } else {
            System.out.println("Card with id of'"+nextEntry.getKey()+"' found, but may have been removed in Rally");
         }
      }
   }

   /*
   A bug was introduced in Miro's V1 REST API in early February 2022.
   Instead of returning an array of the IDs of each child of the frame, the children array contains entries that look like:
   WidgetId(value=3458764517398979354)
    */
   private String sanitizeFrameChild(String child) {
      String result = child;
      if (child.startsWith("WidgetId")) {
         result = child.substring(15, child.length()-1);
      }
      return result;
   }

   @Override
   protected MiroWidget handleWidget(MiroWidget widget, RallyNode node) throws IOException {
      return connector.updateWidget(widget, "Error updating "+node.getFormattedId(), false);
   }

   public HashSet<RallyNode> getUpdatedNodes() {
      return updatedNodes;
   }
}
