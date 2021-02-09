package org.appling.rallyx.miro.widget;

public class CustomField
{
   private String value;
   private String iconUrl;
   private String mainColor = null;

   public CustomField(String value, String iconUrl) {
      this.value = value;
      this.iconUrl = iconUrl;
   }

   public CustomField(String value, String iconUrl, String color) {
      this(value, iconUrl);
      this.mainColor = color;
   }

}
