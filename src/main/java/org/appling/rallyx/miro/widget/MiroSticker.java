package org.appling.rallyx.miro.widget;

public class MiroSticker extends MiroWidget
{
   public String type = "sticker";
   public Float scale = null;
   public String text = "";
   public MiroStickerStyle style = new MiroStickerStyle();

   public MiroSticker(String text) {
      this.text = text;
   }
}
