package org.appling.rallyx.miro.widget;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FoundWidget {
   private final MiroWidget miroWidget;
   private final static Pattern WIDGET_TITLES = Pattern.compile("<a href=\"(.+)\">(.+)</a> (.+)");
   private final String rallyId;
   private final String rallyLink;
   private final String title;

   public FoundWidget(MiroWidget miroWidget) {
      this.miroWidget = miroWidget;
      Matcher matcher = WIDGET_TITLES.matcher(miroWidget.getText());
      if (matcher.matches()) {
         rallyLink = matcher.group(1);
         rallyId = matcher.group(2);
         title = matcher.group(3);
      } else {
         rallyLink = "";
         rallyId = "";
         title = "";
      }
   }

   public boolean hasRallyId() {
      return (rallyId.length() > 0);
   }

   public String getRallyId() {
      return rallyId;
   }

   public String getRallyLink() {
      return rallyLink;
   }

   public String getTitle() {
      return title;
   }
}
