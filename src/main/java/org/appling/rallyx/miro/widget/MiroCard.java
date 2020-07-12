package org.appling.rallyx.miro.widget;

public class MiroCard extends MiroWidget
{
   private String type = "card";
   public Float scale = null;
   public String title = "";
   public MiroCardStyle style = new MiroCardStyle();
   public String description;
   public CardInternal card;

   public MiroCard( String text) {
      this.title = text;
   }
   public void addCustomField(CustomField customField) {
      if (card == null) {
         card = new CardInternal();
      }
      card.addCustomField( customField );
   }
}
