package org.appling.rallyx.miro;

import org.appling.rallyx.miro.widget.*;
import org.appling.rallyx.rally.RallyNode;
import org.appling.rallyx.rally.StoryStats;
import org.appling.rallyx.rally.Tags;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MiroWriter
{
   private final MiroConnector connector;
   private final StoryStats stats;
   private final Set< RallyNode > mmfNodes;
   private final Set< RallyNode > nonMMFStories;
   private final Set< RallyNode > nonMMFFeatures;
   private final CardFields cardFields;
   private final String targetId;
   private MiroWidget target;

   private double currentX = 0;
   private double currentY = 0;
   private double ySpacing = 20;
   private double xSpacing = 20;



   public MiroWriter( StoryStats stats, String authToken, String boardId, String targetId, String fieldsToShow) {
      connector = new MiroConnector( authToken, boardId );
      this.targetId = targetId;
      //connector.setTargetFrame( frameId );
      cardFields = new CardFields( fieldsToShow );
      this.stats = stats;
      mmfNodes = stats.getNodesWithTag( Tags.MMF );
      nonMMFStories = stats.getAllStories().stream().filter( n -> !n.hasTag( Tags.MMF ) ).collect( Collectors.toSet() );
      nonMMFFeatures = stats.getInitiative().getChildren().stream().filter( n -> !n.hasTag( Tags.MMF ) ).collect( Collectors.toSet() );


   }

   private void initializeTarget() throws IOException
   {
      if (target == null)
      {
         target = connector.getWidget( targetId );
         if ( "frame".equalsIgnoreCase( target.getType() ) )
         {
            //connector.setTargetFrame( targetId );
            currentY = target.getTop() + ySpacing;
            currentX = target.getLeft() + xSpacing;
         } else {
            currentX = target.x;
            currentY = target.y + target.getRealHeight()/2 + ySpacing;
         }
      }
   }



   private String getLinkToNode(RallyNode node) {
      return "<A href=\""+node.getURL()+"\">"+node.getFormattedId()+"</A> ";
   }

   public void writeAllMMF() throws IOException
   {
      initializeTarget();

      for ( RallyNode mmf : mmfNodes )
      {
         writeMMF( mmf );
      }
   }

   public void writeNonMMFFeatures() throws IOException
   {
      initializeTarget();

      for ( RallyNode feature : nonMMFFeatures )
      {
         writeNonMMFFeature( feature);
      }
   }

   public void writeNonMMFStories() throws IOException
   {
      initializeTarget();
      for ( RallyNode nonMMF : nonMMFStories )
      {
         boolean inRelease = stats.getStoriesInRelease().contains( nonMMF );

         writeNonMMFStory( nonMMF, inRelease );
         //if (--limit < 0) { break; }
      }
   }

   public void writeAllInOrder() throws IOException
   {
      initializeTarget();
      //Update to show progress on a single line like: u"\u001b[1000D" + str(i + 1) + "%"

      walk( stats.getInitiative());
      Set< RallyNode > storiesNotInInitiative = stats.getStoriesNotInInitiative();

      if (!storiesNotInInitiative.isEmpty())
      {
         MiroSticker widget = new MiroSticker( "Parent Not in Project" );
         widget.style = new MiroStickerStyle( StickerColors.RED );
         widget.scale = 1.07f;
         updateWidgetPosition( widget );
         connector.addWidget( widget );

         for ( RallyNode node : storiesNotInInitiative )
         {
            handleNode( node );
         }
      }
   }

   private void walk(RallyNode node ) throws IOException
   {
      ArrayList<RallyNode> mmfStories = new ArrayList<>(  );
      handleNode( node );

      List< RallyNode > children = node.getChildren();
      for ( RallyNode child : children ) {
         if (child.isUserStory() && child.hasTag( Tags.MMF )) {
            mmfStories.add(child);
         } else  {
            walk( child );
         }
      }

      for ( RallyNode story : mmfStories )
      {
         walk( story );
      }
   }

   private void handleNode(RallyNode node ) throws IOException
   {
      if (node.isInitiative()) {} // intentionally ignore
      else if (node.hasTag( Tags.MMF )) {
         writeMMF( node );
      }
      else if (node.isFeature()) {
         writeNonMMFFeature( node );
      } else if (node.isUserStory()) {
         boolean inRelease = stats.getStoriesInRelease().contains( node );
         writeNonMMFStory( node, inRelease );
      }
   }

   private void writeRemainingMMFStories(ArrayList<RallyNode> mmfStories ) throws IOException
   {
      for ( RallyNode mmfStory : mmfStories )
      {
         writeMMF( mmfStory );
      }
      mmfStories.clear();
   }

   private void updateWidgetPosition(MiroWidget widget) {
      if ("sticker".equalsIgnoreCase( widget.getType())) {
         MiroSticker sticker = (MiroSticker) widget;
         if (sticker.style.backgroundColor.equals( StickerColors.GREEN ) ||
               sticker.style.backgroundColor.equals( StickerColors.RED )) { // Feature or Not In Initiative
            currentY = target.getTop() + ySpacing;
         } else if (sticker.style.backgroundColor.equals( StickerColors.PASTEL_BLUE )) { // MMF
            currentY = target.getTop() + (ySpacing * 2) + sticker.getRealHeight();
         }
         currentX += MiroCard.getDefaultWidth() + xSpacing;
      }

      widget.x = currentX;
      widget.y = currentY + widget.getRealHeight()/2;

      currentY += widget.getRealHeight() + ySpacing;
   }

   private void writeMMF( RallyNode mmf) throws IOException
   {
      MiroSticker widget = new MiroSticker( getLinkToNode( mmf ) + mmf.getName() );
      widget.style = new MiroStickerStyle( StickerColors.PASTEL_BLUE );
      widget.scale = 1.07f;

      updateWidgetPosition(widget);
      connector.addWidget( widget );
   }

   private void writeNonMMFFeature( RallyNode feature) throws IOException
   {
      MiroSticker widget = new MiroSticker( getLinkToNode( feature ) + feature.getName() );
      widget.style = new MiroStickerStyle( StickerColors.GREEN  );
      widget.scale = 1.07f;
      updateWidgetPosition(widget);
      connector.addWidget( widget );
   }

   private void writeNonMMFStory(RallyNode node, boolean inRelease) throws IOException
   {
      MiroCard card = new MiroCard( getLinkToNode( node ) + node.getName(), inRelease );
      String description = node.getDescription();
      if (description.length() > 20000) {
         description = "Description Too Long. View description in Rally.";
      }
      card.description = description;
      card.style = new MiroCardStyle( "#808080");

      cardFields.addFieldsToCard(card, node);

      updateWidgetPosition(card);
      MiroCard newCard = connector.addWidget( card );
   }
}
