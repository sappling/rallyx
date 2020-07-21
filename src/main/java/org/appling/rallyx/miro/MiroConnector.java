package org.appling.rallyx.miro;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;
import org.appling.rallyx.miro.widget.MiroCard;
import org.appling.rallyx.miro.widget.MiroFrame;
import org.appling.rallyx.miro.widget.MiroWidget;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class MiroConnector
{
   private String authToken;
   private String targetFrame;
   private String boardId;
   private Gson gson;

   public MiroConnector(String authToken, String boardId) {
      this.authToken = authToken;
      this.boardId = boardId;

      GsonBuilder builder = new GsonBuilder();
      gson = builder.create();
   }

   public void setTargetFrame(String targetFrame) {
      this.targetFrame = targetFrame;
   }

   private String getHeaderValue( HttpResponse response, String headerName) throws IOException
   {
      String result = "";
      Header header = response.getFirstHeader(headerName );
      if (header != null) {
         result = header.getValue();
      }
      return result;
   }

   public <W extends MiroWidget > W addWidget( W widget) throws IOException
   {
      if (targetFrame != null) {
         widget.parentFrameId = targetFrame;
      }
      String newContent = gson.toJson( widget );

      HttpResponse httpResponse = Request.Post( "https://api.miro.com/v1/boards/" + boardId + "/widgets" )
            .addHeader( "Authorization", "Bearer " + authToken )
            .addHeader( "Accept", "application/json, text/plain, */*" )
            .bodyString( newContent, ContentType.APPLICATION_JSON )
            .execute().returnResponse();

      checkForError( httpResponse );
      waitForLimitReset(httpResponse);

      String resultString = EntityUtils.toString(httpResponse.getEntity());
      return (W) gson.fromJson( resultString, widget.getClass() );
   }

   public <W extends MiroWidget > W getWidget(String id) throws IOException
   {
      HttpResponse httpResponse = Request.Get( "https://api.miro.com/v1/boards/" + boardId + "/widgets/"+id )
            .addHeader( "Authorization", "Bearer " + authToken )
            .addHeader( "Accept", "application/json, text/plain, */*" )
            .execute().returnResponse();

      checkForError( httpResponse );
      waitForLimitReset(httpResponse);

      String resultString = EntityUtils.toString( httpResponse.getEntity() );
      //System.out.println(resultString);

      MiroWidget widget = gson.fromJson( resultString, MiroWidget.class );
      return (W) gson.fromJson( resultString, widget.getClassOfType() );
   }

   private void checkForError(HttpResponse httpResponse) throws IOException {
      StatusLine statusLine = httpResponse.getStatusLine();
      if (statusLine.getStatusCode() > 201) {

         String resultString = EntityUtils.toString(httpResponse.getEntity());
         ErrorResponse errorResponse = gson.fromJson( resultString, ErrorResponse.class);
         throw new IOException( "Bad Response writing Miro:" + statusLine.getReasonPhrase() + " - " + errorResponse.message);
      }
   }

   private void waitForLimitReset(HttpResponse httpResponse) throws IOException
   {
      String limitRemaining = getHeaderValue( httpResponse, "X-RateLimit-Remaining" );
      String limitReset = getHeaderValue( httpResponse, "X-RateLimit-Reset" );

      if (!limitRemaining.isEmpty() && !limitReset.isEmpty())
      {
         long remaining = Long.parseLong( limitRemaining );

         if ( remaining < 200 )
         {
            Date resetTime = new Date( Long.parseLong( limitReset ) * 1000L );
            for ( Date now = new Date(); now.getTime() < resetTime.getTime(); now = new Date() )
            {
               long remainingTime = resetTime.getTime() - now.getTime();
               if ( remainingTime > 0 )
               {
                  //Update to show progress on a single line like: u"\u001b[1000D" + str(i + 1) + "%"
                  System.out.printf( "Rate limit reached.  Pausing for %d more seconds.\n", remainingTime / 1000 );
                  try
                  {
                     Thread.sleep( 5000 );
                  } catch ( InterruptedException e )
                  {
                  } // intentionally ignore
               }
            }
         }
      }
   }
}
