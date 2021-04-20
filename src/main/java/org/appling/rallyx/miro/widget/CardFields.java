package org.appling.rallyx.miro.widget;

import org.appling.rallyx.rally.Iteration;
import org.appling.rallyx.rally.Project;
import org.appling.rallyx.rally.RallyNode;

public class CardFields
{
   private static final String FIELD_SIZE = "size";
   private static final String FIELD_ITERATION = "iteration";
   private static final String FIELD_FEATURE = "feature";
   private static final String FIELD_MMF = "mmf";
   private static final String FIELD_TEAM = "team";
   private static final String FIELD_NOT_IN_RELEASE = "notinrelease";
   private static final String FIELD_UNASSIGNED = "unassigned";
   private static final String FIELD_COMPACTMMF = "compactmmf";

   private boolean showSize = false;
   private boolean showIteration = false;
   private boolean showFeature = false;
   private boolean showMMF = false;
   private boolean showTeam = false;
   private boolean showNotInRelease = false;
   private boolean showUnassigned = false;
   private boolean compactMMF = false;



   public CardFields(String fieldsToShow) {
      setCardOptions( fieldsToShow );
   }

   private void setCardOptions( String fieldsToShow )
   {
      if (fieldsToShow != null) {
         String[] options = fieldsToShow.split( ", *" );
         for ( String option : options )
         {
            if (FIELD_SIZE.equalsIgnoreCase(option)) {
               showSize = true;
            }
            if (FIELD_ITERATION.equalsIgnoreCase(option)) {
               showIteration = true;
            }
            if (FIELD_FEATURE.equalsIgnoreCase(option)) {
               showFeature = true;
            }
            if (FIELD_MMF.equalsIgnoreCase(option)) {
               showMMF = true;
            }
            if (FIELD_TEAM.equalsIgnoreCase(option)) {
               showTeam = true;
            }
            if ( FIELD_NOT_IN_RELEASE.equalsIgnoreCase(option)) {
               showNotInRelease = true;
            }
            if (FIELD_UNASSIGNED.equalsIgnoreCase(option)) {
               showUnassigned = true;
            }
            if (FIELD_COMPACTMMF.equalsIgnoreCase(option)) {
               compactMMF = true;
            }
         }
      }
   }

   public void addFieldsToCard( MiroCard card, RallyNode node )
   {
      if (node.isDefect()) {
         card.addCustomField(new CustomField("Defect", "https://cdn.imgbin.com/3/0/15/imgbin-software-bug-bug-WQF2cN5hSmKNTvrwr1aRKEEBf.jpg"));

      }
      if (showSize)
      {
         int points = (int) node.getPlanEstimate();
         if ( points > 0 )
         {
            card.addCustomField( new CustomField( points + ((points==0)?" Point":" Points"), null ) );
         }
      }

      if (showIteration)
      {
         Iteration iteration = node.getIteration();
         if ( iteration != null )
         {
            card.addCustomField( new CustomField( iteration.getName(), null ) );
         }
      }

      if (showFeature)
      {
         RallyNode feature = node.getFeature();
         if ( feature != null )
         {
            card.addCustomField( new CustomField( feature.getFormattedId(), null ) );
         }
      }

      if (showMMF)
      {
         RallyNode mmf = node.getMmf();
         if ( mmf != null )
         {
            card.addCustomField( new CustomField( "MMF:" + mmf.getFormattedId(), null ) );
         }
      }

      if (showTeam) {
         Project project = node.getProject();
         card.addCustomField( new CustomField( project.getName(), null ) );
      }

      if (node.isUserStory() && node.hasChildren()) { // epic
         card.addCustomField(new CustomField("Epic", null));
      } else if (showNotInRelease)  // epics can't have a release assigned
      {
         if ( !card.isInRelease() )
         {
            card.addCustomField( new CustomField( "NIR", ""));
            card.style = new MiroCardStyle( "#E00000" );
         }
      }
      if (showUnassigned && (node.getIteration()==null)) {
         card.addCustomField(new CustomField("Unassigned", null, "#f24726"));
      }

   }

   public boolean isShowSize()
   {
      return showSize;
   }

   public boolean isShowIteration()
   {
      return showIteration;
   }

   public boolean isShowFeature()
   {
      return showFeature;
   }

   public boolean isShowMMF()
   {
      return showMMF;
   }

   public boolean isShowTeam()
   {
      return showTeam;
   }

   public boolean isShowNotInRelease()
   {
      return showNotInRelease;
   }

   public boolean isShowUnassigned() { return showUnassigned; }

   public boolean isCompactMMF() { return  compactMMF; }
}
