package org.appling.rallyx.miro;

import org.appling.rallyx.miro.widget.*;
import org.appling.rallyx.rally.RallyNode;
import org.appling.rallyx.rally.StoryStats;
import org.appling.rallyx.rally.Tags;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class MiroWriter
{
   protected final MiroConnector connector;
   protected final StoryStats stats;
   private final Set< RallyNode > mmfNodes;
   private final Set< RallyNode > nonMMFStories;
   private final Set< RallyNode > nonMMFFeatures;
   private final CardFields cardFields;
   protected final String targetId;
   private final HashSet<RallyNode> ignore;
   private MiroWidget target;

   private double currentX = 0;
   private double currentY = 0;
   private double ySpacing = 20;
   private double xSpacing = 20;
   private boolean lastNodeWasFeature = false;



   public MiroWriter(StoryStats stats, String authToken, String boardId, String targetId, String fieldsToShow, HashSet<RallyNode> ignore) {
      connector = new MiroConnector( authToken, boardId );
      this.ignore = ignore;
      this.targetId = targetId;
      //connector.setTargetFrame( frameId );
      cardFields = new CardFields( fieldsToShow );
      this.stats = stats;
      mmfNodes = stats.getNodesWithTag( Tags.MMF );
      nonMMFStories = stats.getAllStories().stream().filter( n -> !n.hasTag( Tags.MMF ) ).collect( Collectors.toSet() );
      if (stats.getInitiative() != null) {
         nonMMFFeatures = stats.getInitiative().getChildren().stream().filter(n -> !n.hasTag(Tags.MMF)).collect(Collectors.toSet());
      } else {
         nonMMFFeatures = Collections.emptySet();
      }
   }

   public Set<RallyNode> getMmfNodes() {
      return Collections.unmodifiableSet(mmfNodes);
   }

   public Set<RallyNode> getNonMMFStories() {
      return Collections.unmodifiableSet(nonMMFStories);
   }

   public Set<RallyNode> getNonMMFFeatures() {
      return Collections.unmodifiableSet(nonMMFFeatures);
   }

   protected void initializeTarget() throws IOException
   {
      if (target == null)
      {
         target = connector.getWidget( targetId, false );
         if ( "frame".equalsIgnoreCase( target.getType() ) )
         {
            //connector.setTargetFrame( targetId );
            currentY = target.getTop() + (4*ySpacing);
            currentX = target.getLeft(); // + xSpacing/2;
         } else {
            currentX = target.x;
            currentY = target.y + target.getRealHeight()/2 + ySpacing;
         }
      }
   }



   public String getLinkToNode(RallyNode node) {
      return "<A href=\""+node.getURL()+"\">"+node.getFormattedId()+"</A> ";
   }

   /*
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
   */

   public void writeAllInOrder() throws IOException
   {
      initializeTarget();
      //Update to show progress on a single line like: u"\u001b[1000D" + str(i + 1) + "%"

      walk( stats.getInitiative());
      Set< RallyNode > storiesNotInInitiative = stats.getStoriesNotInInitiative();
      Set<RallyNode> defectsNotInInitiative = stats.getDefectsNotInInitiative();

      if (!storiesNotInInitiative.isEmpty() || !defectsNotInInitiative.isEmpty()) {
         MiroSticker widget = new MiroSticker("Not In Initiative");

         widget.style = new MiroStickerStyle(StickerColors.RED);
         widget.setFeature(true);
         widget.scale = 1.07f;
         updateWidgetPosition(widget, true);
         connector.addWidget(widget, false);
      }

      for ( RallyNode node : storiesNotInInitiative ) {
         handleNode( node, null );
      }

      for (RallyNode node: defectsNotInInitiative) {
         handleNode(node, null);
      }
   }

   private void walk(RallyNode node ) throws IOException
   {
      if (node != null) {
         ArrayList<RallyNode> mmfStories = new ArrayList<>();
         handleNode(node, null);
         List<RallyNode> defects = node.getDefects();
         for (RallyNode defect : defects) {
            handleNode(defect, null);
         }
         List<RallyNode> children = node.getChildren();
         for (RallyNode child : children) {
            if (child.isUserStory() && child.hasTag(Tags.MMF)) {
               mmfStories.add(child);
            } else {
               walk(child);
            }
         }

         for (RallyNode story : mmfStories) {
            walk(story);
         }
      }
   }

   protected void handleNode(RallyNode node, @Nullable String widgetId ) throws IOException
   {
      if (node.isInitiative()) {} // intentionally ignore
      else if (node.hasTag( Tags.MMF )) {
         if (shouldAddNode( node )) {
            writeMMF( node, widgetId );
         }
      }
      else if (node.isFeature()) {
         if (shouldAddNode(node)) {
            writeNonMMFFeature(node, widgetId);
         }
      } else if (node.isUserStory()) {
         if (shouldAddNode( node )) {
            boolean inRelease = node.hasChildren() || stats.getStoriesInRelease().contains( node );
            writeNonMMFStory( node, inRelease, widgetId );
         }
      } else if (node.isDefect()) {
         if (shouldAddNode(node)) {
            writeDefect(node, true, widgetId);   //todo - how to handle in release
         }
      }
   }

   /**
    * When walking the tree of initiatives, we will get nodes that are not in the specified release
    * If the cardFields option to highlight things not in the release is selected, then we want
    * to add it, otherwise, we shouldn't.
    * @param node
    * @return true if this node should be handled
    */
   private boolean shouldAddNode(RallyNode node) {
      boolean result = true;
      if (node.isUserStory() &&
            stats.getReleaseSpecified() &&
            !cardFields.isShowNotInRelease() &&
            !node.hasChildren() &&
            !(node.getNumberOfDefects() > 0) &&
            !stats.getStoriesInRelease().contains( node )) {
         result = false; // if this is a user story not in the release and we aren't highlighting missing, then don't add it
      } else if (node.isOutOfProject()) {
         if (!node.hasDescendentsInProject() &&
         !(node.getDefects().size() > 0)) {
            result = false;
         }
      }
      return result;
   }

/*
   private void writeRemainingMMFStories(ArrayList<RallyNode> mmfStories ) throws IOException
   {
      for ( RallyNode mmfStory : mmfStories )
      {
         writeMMF( mmfStory );
      }
      mmfStories.clear();
   }
*/

   private void updateWidgetPosition(MiroWidget widget, boolean moveToNewColumn) {
      if ("sticker".equalsIgnoreCase( widget.getType())) {
         MiroSticker sticker = (MiroSticker) widget;
         if (sticker.isFeature()) { // Feature or Not In Initiative
            currentY = target.getTop() + ySpacing;
         } else if (sticker.style.backgroundColor.equals( StickerColors.PASTEL_BLUE )) { // MMF
            currentY = target.getTop() + (ySpacing * 2) + sticker.getRealHeight();
         }
         if (moveToNewColumn) {
            currentX += MiroCard.getDefaultWidth() + xSpacing;
         }
      }

      widget.x = currentX;
      widget.y = currentY + widget.getRealHeight()/2;

      currentY += widget.getRealHeight() + ySpacing;
   }

   private void writeMMF( RallyNode node, @Nullable String widgetId) throws IOException
   {
      String text = getLinkToNode( node ) + node.getName();
      if (node.isOutOfProject()) {
         text += " <span style=\"color:red\">NIP</span>";      // Decided on NIP for Not In Project
      }
      if (cardFields.isShowUnassigned() && !node.hasChildren() && node.getDefects().isEmpty() && (node.getIteration() == null)) {
         text += " <span style=\"color:red\">Unassigned</span>";
      }
      MiroSticker widget = new MiroSticker( text );
      widget.id = widgetId;   // note this is null for writing, but must be set for updating
      widget.style = new MiroStickerStyle( StickerColors.PASTEL_BLUE );
      widget.setFeature( node.isFeature());
      widget.scale = 1.07f;

      updateWidgetPosition(widget, !lastNodeWasFeature);
      handleWidget( widget, node);
      lastNodeWasFeature = false;
   }

   private void writeNonMMFFeature( RallyNode node, @Nullable String widgetId) throws IOException
   {
      MiroSticker widget = new MiroSticker( getLinkToNode( node ) + node.getName() );
      widget.id = widgetId;   // note this is null for writing, but must be set for updating
      widget.style = new MiroStickerStyle( StickerColors.GREEN  );
      widget.setFeature( true );
      widget.scale = 1.07f;
      updateWidgetPosition(widget, true);
      handleWidget( widget, node);
      lastNodeWasFeature = true;
   }

   private void writeNonMMFStory(RallyNode node, boolean inRelease, @Nullable String widgetId) throws IOException
   {
      MiroCard card = new MiroCard( getLinkToNode( node ) + node.getName(), inRelease );
      card.id = widgetId;   // note this is null for writing, but must be set for updating
      String description = node.getDescription();
      if (description.length() > 5000) {
         description = "Description Too Long. View description in Rally.";
      }
      card.description = description;
      card.style = new MiroCardStyle( "#808080");

      cardFields.addFieldsToCard(card, node);

      updateWidgetPosition(card, true);
      MiroWidget newCard = handleWidget( card, node);
      lastNodeWasFeature = false;
   }

   private void writeDefect(RallyNode node, boolean inRelease, @Nullable String widgetId) throws IOException {
      MiroCard card = new MiroCard( getLinkToNode( node ) + node.getName(), inRelease );
      card.id = widgetId;   // note this is null for writing, but must be set for updating
      String description = node.getDescription();
      if (description.length() > 5700) {
         description = "Description Too Long. View description in Rally.";
      }

      cardFields.addFieldsToCard(card, node);

      card.description = description;
      card.style = new MiroCardStyle( StickerColors.ORANGE);

      updateWidgetPosition(card, true);
      MiroWidget newCard = handleWidget( card, node);
   }

   protected MiroWidget handleWidget(MiroWidget widget, RallyNode node) throws IOException {
      MiroWidget result = null;
      if (!ignore.contains(node)) {
         result = connector.addWidget(widget, false);
      }
      return result;
   }
}
