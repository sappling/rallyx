package org.appling.rallyx.rally;

public class RallyOptions {
   private static final String OPTION_HIDEBH = "hidebugholder"; // ignore stories with the "bugholder" tag
   private static final String OPTION_NII = "NotInInitiative";          // show the not in initiative bucket
   private static final String OPTION_NIAR = "NotInAnyRelease";        // show the not in any release bucket

   private boolean hideBugHolder = false;
   private boolean showNotInInitiative = false;
   private boolean showNotInAnyRelease = false;

   public RallyOptions(String optionString) {
      if (optionString != null) {
         String[] options = optionString.split(", *");
         for (String option : options) {
            if (OPTION_HIDEBH.equalsIgnoreCase(option)) {
               hideBugHolder = true;
            } else if (OPTION_NII.equalsIgnoreCase(option)) {
               showNotInInitiative = true;
            } else if (OPTION_NIAR.equalsIgnoreCase(option)) {
               showNotInAnyRelease = true;
            }
         }
      }
   }

   public boolean isHideBugHolder() {
      return hideBugHolder;
   }
   public boolean isShowNotInInitiative() { return showNotInInitiative; }
   public boolean isShowNotInAnyRelease() { return showNotInAnyRelease; }
}
