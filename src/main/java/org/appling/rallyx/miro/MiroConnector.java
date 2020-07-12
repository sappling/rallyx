package org.appling.rallyx.miro;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.appling.rallyx.miro.widget.MiroFrame;
import org.appling.rallyx.miro.widget.MiroWidget;

import java.io.IOException;

public class MiroConnector
{
   private String authToken;
   private String targetFrame;
   private String boardId;

   public MiroConnector(String authToken, String boardId) {
      this.authToken = authToken;
      this.boardId = boardId;
   }

   public void setTargetFrame(String targetFrame) {
      this.targetFrame = targetFrame;
   }

   public <W extends MiroWidget > W addWidget( W widget) throws IOException
   {
      GsonBuilder builder = new GsonBuilder();
      Gson gson = builder.create();

      if (targetFrame != null) {
         widget.parentFrameId = targetFrame;
      }
      String newContent = gson.toJson( widget );
      //System.out.println("new:"+newContent);
      String resultString = Request.Post( "https://api.miro.com/v1/boards/"+boardId+"/widgets" )
            .addHeader( "Authorization", "Bearer "+authToken )
            .addHeader( "Accept", "application/json, text/plain, */*" )
            .bodyString( newContent, ContentType.APPLICATION_JSON )
            .execute().returnContent().asString();

      return (W) gson.fromJson( resultString, widget.getClass() );
   }
}
