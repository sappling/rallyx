package org.appling.rallyx.miro.widget;

public class MiroFrame extends MiroWidget
{
   public String title;
   public String[] children = new String[0];

   public MiroFrame() {
      this.type = "frame";
   }

   @Override
   public String getText() {
      return title;
   }

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
