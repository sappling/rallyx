package org.appling.rallyx.miro.widget;

public class MiroShape extends MiroWidget
{
   public MiroShapeStyle style = new MiroShapeStyle();
   public String text;



   public MiroShape(String text) {
      type = "shape";
      if (text != null) {
         this.text = text;
      }
   }

   @Override
   public String getText() {
      return text;
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
