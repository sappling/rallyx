package org.appling.rallyx.miro.widget;

public class MiroWidget
{
   protected String type;
   public double x = 0;
   public double y = 0;

   // these are objects so they can be optional - gson won't serialize null objects
   public Double width;
   public Double height;

   public String parentFrameId = null;

   public double getRealWidth()  { return width; }
   public double getRealHeight() { return height; }
   public double getLeft()       { return x - (getRealWidth()/2); }
   public double getRight()      { return x + (getRealWidth()/2); }
   public double getTop()        { return y - (getRealHeight()/2); }
   public double getBottom()     { return y + (getRealHeight()/2); }

   @Override
   public String toString()
   {
      return "MiroWidget{" +
            "type=" + type +
            ",x=" + x +
            ", y=" + y +
            "width="+(width != null ? width :"")+","+
            "height="+(height != null ? height :"")+","+
            ", parentFrameId='" + parentFrameId + '\'' +
            '}';
   }

   public Class getClassOfType() {
      Class result = MiroWidget.class;
      if ("CARD".equalsIgnoreCase( type )) {
         result = MiroCard.class;
      } else if ("STICKER".equalsIgnoreCase( type )) {
         result = MiroSticker.class;
      }
      return result;
   }

   public String getType() {
      return type;
   }
}
