package org.appling.rallyx.rally;

import java.util.Properties;

public class RallyOptions {
   private static final String OPTION_HIDEBH = "hidebugholder"; // ignore stories with the "bugholder" tag

   private boolean hideBugHolder = false;

   public RallyOptions(String optionString) {
      if (optionString != null) {
         String[] options = optionString.split(", *");
         for (String option : options) {
            if (OPTION_HIDEBH.equalsIgnoreCase(option)) {
               hideBugHolder = true;
            }
         }
      }
   }

   public boolean isHideBugHolder() {
      return hideBugHolder;
   }
}
