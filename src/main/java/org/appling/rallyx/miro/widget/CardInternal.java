package org.appling.rallyx.miro.widget;

import java.util.ArrayList;

public class CardInternal
{
   private ArrayList<CustomField> customFields;

   public void addCustomField(CustomField customField) {
      if (customFields == null) {
         customFields = new ArrayList<>(  );
      }
      customFields.add(customField);
   }
}
