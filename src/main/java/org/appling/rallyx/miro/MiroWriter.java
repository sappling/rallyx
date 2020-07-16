package org.appling.rallyx.miro;

import org.appling.rallyx.miro.widget.*;
import org.appling.rallyx.rally.Iteration;
import org.appling.rallyx.rally.RallyNode;
import org.appling.rallyx.rally.StoryStats;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

public class MiroWriter
{
   static private final String MMF_TAG = "MMF";

   private MiroConnector connector;

   public MiroWriter(String authToken, String boardId, String frameId) {
      connector = new MiroConnector( authToken, boardId );
      connector.setTargetFrame( frameId );
   }

   public void writeMMF( StoryStats stats ) throws IOException
   {
      Set< RallyNode > mmfNodes = stats.getNodesWithTag( MMF_TAG );
      for ( RallyNode mmf : mmfNodes )
      {
         String link = "<A href=\"" + mmf.getURL() + "\">" + mmf.getFormattedId() + "</A> ";
         MiroSticker widget = new MiroSticker( link + mmf.getName() );
         widget.style = new MiroStickerStyle( StickerColors.PASTEL_BLUE);
         widget.scale = 1.07f;
         connector.addWidget( widget );
      }
   }

   public void writeNonMMF(StoryStats stats) throws IOException
   {
      Set< RallyNode > storiesInRelease = stats.getStoriesInRelease();
      Set< RallyNode > nonMMFStories = storiesInRelease.stream().filter( n -> !n.hasTag( MMF_TAG ) ).collect( Collectors.toSet() );

      for ( RallyNode nonMMF : nonMMFStories )
      {
         String link = "<A href=\""+nonMMF.getURL()+"\">"+nonMMF.getFormattedId()+"</A> ";
         MiroCard card = new MiroCard( link + nonMMF.getName() );
         String description = nonMMF.getDescription();
         if (description.length() > 20000) {
            description = "Description Too Long. View description in Rally.";
         }
         card.description = description;
         card.style = new MiroCardStyle( "#808080");
         card.addCustomField( new CustomField( (int)nonMMF.getPlanEstimate() + " Points", null ) );
         Iteration iteration = nonMMF.getIteration();
         if (iteration != null) {
            card.addCustomField( new CustomField( iteration.getName(), null ) );
         }
         RallyNode feature = nonMMF.getFeature();
         if (feature != null) {
            card.addCustomField( new CustomField( feature.getFormattedId(), null ) );
         }
         connector.addWidget( card );
      }

   }
}
