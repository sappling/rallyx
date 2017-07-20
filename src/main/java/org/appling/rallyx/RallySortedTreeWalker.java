package org.appling.rallyx;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.rallydev.rest.RallyRestApi;
import com.rallydev.rest.request.QueryRequest;
import com.rallydev.rest.response.QueryResponse;
import com.rallydev.rest.util.Fetch;
import com.rallydev.rest.util.QueryFilter;
import org.appling.rallyx.rally.RallyQueryFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by sappling on 9/5/2016.
 */
public class RallySortedTreeWalker {
    private RallyRestApi restApi;
    private HashMap<String, JsonObject> releaseStories;

    public RallySortedTreeWalker(RallyRestApi restApi, HashMap<String, JsonObject> releaseStories) {
        this.restApi = restApi;
        this.releaseStories = releaseStories;
    }

    public void walk(JsonObject rallyObject, Object parentNative, WalkAction action, int depth)  {
        parentNative = action.act(rallyObject, parentNative, depth);

        String id = rallyObject.get("ObjectID").getAsString();
        // remove the stories that are found beneath the Initiative
        if (releaseStories.containsKey(id)) {
            releaseStories.remove(id);
        }

        if (rallyObject.getAsJsonPrimitive("DirectChildrenCount").getAsInt() > 0) {
            String parentType = rallyObject.get("_type").getAsString();


            QueryRequest queryRequest = RallyQueryFactory.getChildren(id, parentType);

            QueryResponse response = null;
            try {
                response = restApi.query(queryRequest);
                if (response.wasSuccessful()) {
                    JsonArray jsonArray = response.getResults();
                    Iterator<JsonElement> it = jsonArray.iterator();
                    while (it.hasNext()) {
                        walk(it.next().getAsJsonObject(), parentNative, action, depth+1);
                    }
                }
            } catch (IOException e) {
                System.err.println("Exception getting children from: "+queryRequest.toUrl());
                e.printStackTrace();
            }
        }
    }
}
