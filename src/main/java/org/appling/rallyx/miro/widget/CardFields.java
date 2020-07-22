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

   private boolean showSize = false;
   private boolean showIteration = false;
   private boolean showFeature = false;
   private boolean showMMF = false;
   private boolean showTeam = false;
   private boolean showNotInRelease = false;



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
         }
      }
   }

   public void addFieldsToCard( MiroCard card, RallyNode node )
   {
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

      if (showNotInRelease)
      {
         if ( !card.isInRelease() )
         {
            card.addCustomField( new CustomField( "NIR", "https://www.pikpng.com/pngl/m/98-988476_clipart-no-sign-x-red-circle-with-line.png" ) );
            card.style = new MiroCardStyle( "#E00000" );
         }
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
}
