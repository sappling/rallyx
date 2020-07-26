package org.appling.rallyx.miro.widget;

public class MiroCard extends MiroWidget
{
   private static final transient float defaultWidth = 320f;
   private static final transient float defaultHeight = 100f;  // was 120

   public float scale = 1f;
   public String title = "";
   public MiroCardStyle style = new MiroCardStyle();
   public String description;
   public CardInternal card;
   private transient boolean inRelease = true;

   public MiroCard( String text) {
      type = "card";
      this.title = text;
   }
   public MiroCard(String text, boolean inRelease) {
      this(text);
      this.inRelease = inRelease;
   }

   public void addCustomField(CustomField customField) {
      if (card == null) {
         card = new CardInternal();
      }
      card.addCustomField( customField );
   }

   public boolean isInRelease() { return  inRelease; }

   public double getRealWidth() {
      return (width == null ? defaultWidth : width) * scale;
   }

   public double getRealHeight() {

      double textHeight  = ((title.length() / 44) +1)* 10;
      return ((height == null ? defaultHeight : height) + textHeight )* scale;
   }



   public static double getDefaultWidth() { return defaultWidth; }
   public static double getDefaultHeight() { return defaultHeight; }

   @Override
   public String toString()
   {
      return "MiroCard{" +
            "scale=" + scale +
            ", title='" + title + '\'' +
            ", style=" + style +
            ", description length='" + description.length() + '\'' +
            ", card=" + card +
            ", inRelease=" + inRelease +
            "} " + super.toString();
   }
}
