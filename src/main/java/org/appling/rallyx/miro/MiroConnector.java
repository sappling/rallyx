package org.appling.rallyx.miro;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;
import org.appling.rallyx.miro.widget.MiroWidget;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MiroConnector
{
   private String authToken;
   private String targetFrame;
   private String boardId;
   private Gson gson;
   private static int retryCount = 0;
   private Executor executor;
   private String proxyUrl = null;

   public MiroConnector(String authToken, String boardId) {
      this.authToken = authToken;
      this.boardId = boardId;
      executor = Executor.newInstance();

      GsonBuilder builder = new GsonBuilder();
      gson = builder.create();
   }

   public void setTargetFrame(String targetFrame) {
      this.targetFrame = targetFrame;
   }

   public void setProxy(String proxyUrl, @Nullable String proxyUser, @Nullable String proxyPass) {
      this.proxyUrl = proxyUrl;
      if (proxyUser!=null || proxyPass!=null) {
         executor = executor.auth(proxyUser, proxyPass);
      }
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

   public <W extends MiroWidget > W addWidget( W widget, String contextErrorMessage, boolean inRetry) throws IOException
   {
      if (targetFrame != null) {
         widget.parentFrameId = targetFrame;
      }
      String newContent = gson.toJson( widget );

       Request request = Request.Post( "https://api.miro.com/v1/boards/" + boardId + "/widgets" )
            .addHeader( "Authorization", "Bearer " + authToken )
            .addHeader( "Accept", "application/json, text/plain, */*" )
            .bodyString( newContent, ContentType.APPLICATION_JSON );
       if (proxyUrl != null) {
          request = request.viaProxy(proxyUrl);
       }
      HttpResponse httpResponse = executor.execute(request).returnResponse();

      boolean retry = checkForError( httpResponse, widget.getText(), contextErrorMessage);
      waitForLimitReset(httpResponse);

      if (retry) {
         rateLimitRetryWait(inRetry);
         return addWidget(widget, contextErrorMessage, true);
      }

      String resultString = EntityUtils.toString(httpResponse.getEntity());
      return (W) gson.fromJson( resultString, widget.getClass() );
   }

   public <W extends MiroWidget> W updateWidget(W widget, String contextErrorMessage, boolean inRetry) throws IOException
   {
      if (targetFrame != null) {
         widget.parentFrameId = targetFrame;
      }

      String newContent = "";
      JsonElement el = gson.toJsonTree(widget);
      if (el.isJsonObject()) {
         JsonObject jObj = (JsonObject) el;
         jObj.remove("id");
         jObj.remove("type");
         jObj.remove("x");
         jObj.remove("y");
         newContent = gson.toJson( jObj );
      }

      Request request = Request.Patch( "https://api.miro.com/v1/boards/" + boardId + "/widgets/" +widget.id)
            .addHeader( "Authorization", "Bearer " + authToken )
            .addHeader( "Accept", "application/json, text/plain, */*" )
            .bodyString( newContent, ContentType.APPLICATION_JSON );
      if (proxyUrl != null) {
         request.viaProxy(proxyUrl);
      }

      HttpResponse httpResponse = executor.execute(request).returnResponse();

      boolean retry = checkForError( httpResponse, widget.getText(), contextErrorMessage );
      waitForLimitReset(httpResponse);

      if (retry) {
         rateLimitRetryWait(inRetry);
         return updateWidget(widget, contextErrorMessage, true);
      }

      String resultString = EntityUtils.toString(httpResponse.getEntity());
      return (W) gson.fromJson( resultString, widget.getClass() );
   }

   public <W extends MiroWidget > W getWidget(String id, boolean inRetry) throws IOException
   {
      Request request = Request.Get( "https://api.miro.com/v1/boards/" + boardId + "/widgets/"+id )
            .addHeader( "Authorization", "Bearer " + authToken )
            .addHeader( "Accept", "application/json, text/plain, */*" );
      if (proxyUrl != null) {
         request = request.viaProxy(proxyUrl);
      }

      HttpResponse httpResponse =executor.execute(request).returnResponse();

      boolean retry = checkForError( httpResponse, "Unknown" , "Error getting widget with id "+id);
      waitForLimitReset(httpResponse);

      if (retry) {
         rateLimitRetryWait(inRetry);
         return getWidget(id, true);
      }
      String resultString = EntityUtils.toString( httpResponse.getEntity() );
      //System.out.println(resultString);

      MiroWidget widget = gson.fromJson( resultString, MiroWidget.class );
      return (W) gson.fromJson( resultString, widget.getClassOfType() );
   }

   public <W extends MiroWidget > List<W> getFrameContent(String id, boolean inRetry) throws IOException {
      List<W> result = new ArrayList<>();

      Request request = Request.Get( "https://api.miro.com/v1/boards/" + boardId + "/widgets/"+id )
            .addHeader( "Authorization", "Bearer " + authToken )
            .addHeader( "Accept", "application/json, text/plain, */*" );
      if (proxyUrl != null) {
         request = request.viaProxy(proxyUrl);
      }
      HttpResponse httpResponse =  executor.execute(request).returnResponse();
      boolean retry = checkForError( httpResponse, "Unknown", "Error reading content of frame "+id );
      waitForLimitReset(httpResponse);

      if (retry) {
         rateLimitRetryWait(inRetry);
         return getFrameContent(id, true);
      }

      return result;
   }

   /**
    * Check the httpResponse for errors.
    * @param httpResponse
    * @return true if rate limit was exceeded and we should retry
    * @throws IOException for any error other than rate limit exceeded
    */
   private boolean checkForError(HttpResponse httpResponse, String title, String errorContextMessage) throws IOException {
      StatusLine statusLine = httpResponse.getStatusLine();
      if (statusLine.getStatusCode() > 201) {

         String resultString = EntityUtils.toString(httpResponse.getEntity());
         ErrorResponse errorResponse = gson.fromJson( resultString, ErrorResponse.class);

         if (errorResponse.message != null) {
            if (errorResponse.message.contains("rate limit exceed")) {
               return true;
            } if (errorResponse.message.contains("change locked widget")) {
               System.out.println("Could not change locked widget with title \""+title+"\".  Continuing with others.");
               return false;
            }
         }
         throw new IOException( "Bad Response writing Miro: " +errorContextMessage+ " - " + statusLine.getReasonPhrase() + " - " + ((errorResponse.message != null) ? errorResponse.message : ""));
      }
      return false;
   }

   private void waitForLimitReset(HttpResponse httpResponse) throws IOException
   {
      String limitRemaining = getHeaderValue( httpResponse, "X-RateLimit-Remaining" );
      String limitReset = getHeaderValue( httpResponse, "X-RateLimit-Reset" );

      //System.out.printf("************ Limit - Remaining=%s, Reset=%s\n", limitRemaining, limitReset);
      if (!limitRemaining.isEmpty() && !limitReset.isEmpty())
      {

//         System.out.println("Limit Remaining: "+limitRemaining);

         long remaining = Long.parseLong( limitRemaining );

         if ( remaining <= 400 )
         {
            Date resetTime = new Date( (Long.parseLong( limitReset )+2) * 1000L );
            long remainingTime = resetTime.getTime() - new Date().getTime();

            sleepWithMessage((int) (remainingTime / 1000), "Rate limit reached");
            /*
            System.out.println("Rate limit reached. Waiting until: "+resetTime);
            for ( Date now = new Date(); now.getTime() < resetTime.getTime(); now = new Date() )
            {
               long remainingTime = resetTime.getTime() - now.getTime();
               if ( remainingTime > 0 )
               {
                  //Update to show progress on a single line like: u"\u001b[1000D" + str(i + 1) + "%"
                  System.out.printf( "%d seconds remaining.\n", remainingTime / 1000 );
                  try
                  {
                     Thread.sleep( 5000 );
                  } catch ( InterruptedException e )
                  {
                  } // intentionally ignore
               }
            }
             */
            System.out.println("Resuming Miro communications.");
         }
      }
   }

   private static void sleepWithMessage(int sleepSeconds, String message) {
      long start = new Date().getTime();
      long end = start + (1000L * sleepSeconds);

      try {
         System.out.println("\n");
         for (Date now = new Date(); now.getTime() < end; now = new Date()) {
            System.out.printf("\r%s: Waiting for %d seconds     ", message, ((end - now.getTime()) / 1000));
            Thread.sleep(500L);
         }
      } catch (InterruptedException e) { } // intentionally ignore

   }

   private static void rateLimitRetryWait(boolean inRetry) {
      if (!inRetry) {
         System.out.println();
         retryCount = 0;
      }
      System.out.printf("\rRate Limit Error: Retrying for %d seconds   ", ++retryCount);
      try {
         Thread.sleep(1000L);
      } catch (InterruptedException e) { } // intentionally ignore
   }
}
