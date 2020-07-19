package org.appling.rallyx.miro.widget;

public class MiroSticker extends MiroWidget
{
   private transient float defaultWidth = 199f;
   private transient float defaultHeight = 228f;

   public Float scale = null;
   public String text = "";
   public MiroStickerStyle style = new MiroStickerStyle();

   public MiroSticker(String text) {
      type = "sticker";
      this.text = text;
   }

   @Override
   public double getRealWidth() {
      return (width == null ? defaultWidth : width) * scale;
   }

   @Override
   public double getRealHeight() {
      return (height == null ? defaultHeight : height) * scale;
   }

   @Override
   public String toString()
   {
      return "MiroSticker{" +
            "scale=" + scale +
            ", text='" + text + '\'' +
            ", style=" + style +
            "} " + super.toString();
   }
}
