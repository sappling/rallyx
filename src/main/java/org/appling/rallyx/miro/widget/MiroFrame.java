package org.appling.rallyx.miro.widget;

public class MiroFrame extends MiroWidget
{
   public String title;
   public String id;
   public String[] children = new String[0];

   @Override
   public double getRealWidth()
   {
      return width;
   }

   @Override
   public double getRealHeight()
   {
      return height;
   }
}
