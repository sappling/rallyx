package org.appling.rallyx.miro.widget;

public class MiroCardStyle
{
   public String backgroundColor = "#ffffff";

   public MiroCardStyle() {
   }

   public MiroCardStyle( String backgroundColor) {
      this.backgroundColor = backgroundColor;
   }

   @Override
   public String toString()
   {
      return "MiroCardStyle{" +
            "backgroundColor='" + backgroundColor + '\'' +
            '}';
   }
}
