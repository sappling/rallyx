package org.appling.rallyx.miro;

import org.appling.rallyx.miro.widget.*;
import org.appling.rallyx.rally.RallyNode;
import org.appling.rallyx.rally.RallyOptions;
import org.appling.rallyx.rally.StoryStats;
import org.appling.rallyx.rally.Tags;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class MiroWriter
{
   protected final MiroConnector connector;
   protected final StoryStats stats;
   private final Set< RallyNode > mmfNodes;
   private final Set< RallyNode > bugHolderNodes;
   private final Set< RallyNode > nonMMFStories;
   private final Set< RallyNode > nonMMFFeatures = new HashSet<>();
   private final CardFields cardFields;
   protected final String targetId;
   private final HashSet<RallyNode> ignore;
   private MiroWidget target;
   private RallyOptions rallyOptions;

   private double currentX = 0;
   private double currentY = 0;
   private double ySpacing = 20;
   private double xSpacing = 20;
   private Optional<RallyNode>lastNode = Optional.empty();

   public MiroWriter(StoryStats stats, String authToken, String boardId, String targetId, String fieldsToShow, String rallyOptionString, HashSet<RallyNode> ignore) {
      connector = new MiroConnector( authToken, boardId );
      this.ignore = ignore;
      this.targetId = targetId;
      //connector.setTargetFrame( frameId );
      rallyOptions = new RallyOptions(rallyOptionString);
      cardFields = new CardFields( fieldsToShow, rallyOptions );
      this.stats = stats;
      mmfNodes = stats.getNodesWithTag( Tags.MMF );
      bugHolderNodes = stats.getNodesWithTag(Tags.BUGHOLDER);
      nonMMFStories = stats.getAllStories().stream().filter( n -> !(n.hasTag( Tags.MMF ) || (rallyOptions.isHideBugHolder() && n.hasTag(Tags.BUGHOLDER)) ) ).collect( Collectors.toSet() );
      for (RallyNode initiative : stats.getInitiatives()) {
         nonMMFFeatures.addAll(initiative.getChildren().stream().filter(n -> !n.hasTag(Tags.MMF)).collect(Collectors.toSet()));
      }
   }

   public void setProxy(@NotNull String proxyUrl, @Nullable String proxyUser, @Nullable String proxyPass) {
      connector.setProxy(proxyUrl, proxyUser, proxyPass);
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
         target = connector.getFrameV2( targetId, false );
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

      for (RallyNode initiative : stats.getInitiatives()) {
         walk( initiative );
      }

      Set< RallyNode > storiesNotInInitiative = stats.getStoriesNotInInitiative();
      Set<RallyNode> defectsNotInInitiative = stats.getDefectsNotInInitiative();

      if (rallyOptions.isShowNotInInitiative() && (!storiesNotInInitiative.isEmpty() || !defectsNotInInitiative.isEmpty())) {
         MiroSticker widget = new MiroSticker("Not In Initiative");

         widget.style = new MiroStickerStyle(StickerColors.RED);
         widget.setFeature(true);
         widget.scale = 1.07f;
         updateWidgetPosition(widget);
         MiroSticker result = connector.addWidget(widget, "Error adding Not In Initiative sticker", false);
         connector.updateWidget(result, "Error updating Not In Initiative sticker", false);

         for ( RallyNode node : storiesNotInInitiative ) {
            handleNode( node, null, false);
         }

         for (RallyNode node: defectsNotInInitiative) {
            handleNode(node, null, false);
         }
      }

      /*
      if (rallyOptions.isShowNotInAnyRelease() && (!stats.getStoriesInNoRelease().isEmpty() || !stats.getDefectsInNoRelease().isEmpty())) {
         MiroSticker widget = new MiroSticker("Not In Any Release");

         widget.style = new MiroStickerStyle(StickerColors.RED);
         widget.setFeature(true);
         widget.scale = 1.07f;
         updateWidgetPosition(widget);
         MiroSticker result = connector.addWidget(widget, "Error adding Not In Any Release sticker", false);
         connector.updateWidget(result, "Error updating Not In Any Release  sticker", false);

         for ( RallyNode node : stats.getStoriesInNoRelease() ) {
            handleNode( node, null, true);
         }

         for (RallyNode node: stats.getDefectsInNoRelease()) {
            handleNode(node, null, true);
         }
      }
       */

   }

   private void walk(RallyNode node ) throws IOException
   {
      if (node != null) {
         ArrayList<RallyNode> mmfStories = new ArrayList<>();
         if (!(rallyOptions.isHideBugHolder() && node.hasTag(Tags.BUGHOLDER))) {
            handleNode(node, null, false);
         }
         List<RallyNode> defects = node.getDefects();
         for (RallyNode defect : defects) {
            handleNode(defect, null, false);
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

   protected void handleNode(RallyNode node, @Nullable String widgetId, boolean ignoreRelease) throws IOException
   {
      try {
         if (node.isInitiative()) {
         } // intentionally ignore
         else if (node.hasTag(Tags.MMF)) {
            if (shouldAddNode(node, ignoreRelease)) {
               writeMMF(node, widgetId);
            }
         } else if (node.isFeature()) {
            if (shouldAddNode(node, ignoreRelease)) {
               writeNonMMFFeature(node, widgetId);
            }
         } else if (node.isUserStory()) {
            if (shouldAddNode(node, ignoreRelease)) {
               boolean inRelease = node.hasChildren() || stats.getReleaseName().equals(node.getRelease()); //stats.getStoriesInRelease().contains( node );
               writeNonMMFStory(node, inRelease, widgetId);
            }
         } else if (node.isDefect()) {
            if (shouldAddNode(node, ignoreRelease)) {
               writeDefect(node, true, widgetId);   //todo - how to handle in release
            }
         }
      } catch (IOException ex) {
         System.err.println(ex.getMessage());
      }
   }

   /**
    * When walking the tree of initiatives, we will get nodes that are not in the specified release
    * If the cardFields option to highlight things not in the release is selected, then we want
    * to add it, otherwise, we shouldn't.
    * @param node
    * @return true if this node should be handled
    */
   private boolean shouldAddNode(RallyNode node, boolean ignoreRelease) {
      boolean result = true;
      //System.out.printf("%s: %b, %b, %f%n",node.getFormattedId(), node.hasDescendentsInProject(), !node.getDefects().isEmpty(), node.getPercentDone());
      if ( !ignoreRelease &&
           stats.getReleaseSpecified() &&
           !cardFields.isShowNotInRelease() &&
           !(node.hasSelfOrDescendentsInReleaseAndProject(stats.getReleaseName()) ) &&
           !(rallyOptions.isShowNotInAnyRelease() && node.hasSelfOrDescendentsInReleaseAndProject("")) ){
         result = false; // if this is a user story not in the release and we aren't highlighting missing, then don't add it
      }

      else if (node.isOutOfProject()) {            // if node is not in my team's project
         if (!node.hasDescendentsInProject() &&    // hide it if it has no descendents in my project
         (node.getDefects().size() == 0)) {        // and it has no defects
            result = false;
         }
      }

      // need to do this on the rally side, or at least honor the incomplete option, but doing this here for now since PIP is soon
      if ((node.getPercentDone() == 1.0) && node.hasChildren()){
         result = false;
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

   private void updateWidgetPosition(MiroWidget widget) {
      boolean moveToNewColumn = false;
      /*
      NII - false
      MMF - varies
      Feature - true
      others doesn't matter
       */
      if ("sticker".equalsIgnoreCase( widget.getType())) {
         MiroSticker sticker = (MiroSticker) widget;
         if (sticker.isFeature()) { // Feature or Not In Initiative
            currentY = target.getTop() + ySpacing;
            moveToNewColumn = true;
         } else if (sticker.style.backgroundColor.equals( StickerColors.PASTEL_BLUE )) { // MMF
            if (cardFields.isCompactMMF()) {
               if (lastNode.isPresent()) {
                  RallyNode lastRallyNode = lastNode.get();
                  moveToNewColumn = (lastRallyNode.isUserStory() && !lastRallyNode.hasTag(Tags.MMF)) || lastRallyNode.isDefect();//!lastNode.get().isFeature();
               }
            } else {
               moveToNewColumn = true;
            }
            if (moveToNewColumn) {
               currentY = target.getTop() + (ySpacing * 2) + sticker.getRealHeight();
            }
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
         text += " <span style=\"color:red\">NIP</span> ";      // Decided on NIP for Not In Project
      }
      if (cardFields.isShowUnassigned() && !node.hasChildren() && node.getDefects().isEmpty() && (node.getIteration() == null)) {
         text += " <span style=\"color:red\">Unassigned</span>";
      }
      if (cardFields.isShowIPPrep() && node.hasTag(Tags.IPPREP)) {
         text += " <span style=\"color:red\">I&P Prep</span> ";      // Highlight things to discuss in I&P
      }
      MiroSticker widget = new MiroSticker( text );
      widget.id = widgetId;   // note this is null for writing, but must be set for updating
      widget.style = new MiroStickerStyle( StickerColors.PASTEL_BLUE );
      widget.setFeature( node.isFeature());
      widget.scale = 1.07f;

      updateWidgetPosition(widget);
      handleWidget( widget, node);
      lastNode = Optional.of(node);
   }

   private void writeNonMMFFeature( RallyNode node, @Nullable String widgetId) throws IOException
   {
      String text = getLinkToNode( node )+ node.getName();
      if (cardFields.isShowIPPrep() && node.hasTag(Tags.IPPREP)) {
         text += " <span style=\"color:red\">I&P Prep</span> ";      // Highlight things to discuss in I&P
      }
      MiroSticker widget = new MiroSticker( text );
      widget.id = widgetId;   // note this is null for writing, but must be set for updating
      widget.style = new MiroStickerStyle( StickerColors.GREEN  );
      widget.setFeature( true );
      widget.scale = 1.07f;
      updateWidgetPosition(widget);
      handleWidget( widget, node);
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

      updateWidgetPosition(card);
      MiroWidget newCard = handleWidget( card, node);
   }

   private void writeDefect(RallyNode node, boolean inRelease, @Nullable String widgetId) throws IOException {
      MiroCard card = new MiroCard( getLinkToNode( node ) + node.getName(), inRelease );
      card.id = widgetId;   // note this is null for writing, but must be set for updating
      String description = node.getDescription();
      if (description.length() > 5700) {
         description = "Description Too Long. View description in Rally.";
      }

      card.description = description;
      card.style = new MiroCardStyle( StickerColors.ORANGE);
      cardFields.addFieldsToCard(card, node);


      updateWidgetPosition(card);
      MiroWidget newCard = handleWidget( card, node);
   }

   protected MiroWidget handleWidget(MiroWidget widget, RallyNode node) throws IOException {
      MiroWidget result = null;
      if (!ignore.contains(node)) {
         result = connector.addWidget(widget, "Error adding "+node.getFormattedId(), false);
         if (widget instanceof MiroSticker) {
            connector.updateWidget(result, "Error updating "+node.getFormattedId(), false);
         }
      }
      lastNode = Optional.of(node);
      return result;
   }
}
