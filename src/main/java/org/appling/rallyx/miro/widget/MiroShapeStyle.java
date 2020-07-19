package org.appling.rallyx.miro.widget;

public class MiroShapeStyle
{
   public enum TextAlignHorizontal {center, right, left}
   public enum TextAlignVertical {middle, bottom, top}
   public enum Shape {rectangle, circle, triangle, rounded_rectangle, rhombus, callout, parallelogram, star,
      arrow, arrow_left, pentagon, hexagon, octagon, trapeze, predefined_process, arrow_left_right, cloud,
      brace_left, brace_right, cross, barrel}

   //todo - change many of these strings to enums
   public String backgroundColor = "#ffffff";
   public float backgroundOpacity = 1f;
   public String borderColor = "#1a1a1a";
   public float borderOpacity = 1f;
   public String borderStyle = "normal"; // normal, dashed, dotted
   public float borderWidth = 2f;
   public String fontFamily = "OpenSans";
   public int fontSize =14;

   public Shape shapeType = Shape.rectangle;
   public TextAlignHorizontal textAlign = TextAlignHorizontal.left;

   // allowed values: "middle", "bottom", "top"
   public TextAlignVertical textAlignVertical = TextAlignVertical.middle;
}
