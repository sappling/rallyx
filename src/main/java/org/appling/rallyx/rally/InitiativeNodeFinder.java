package org.appling.rallyx.rally;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.rallydev.rest.RallyRestApi;
import com.rallydev.rest.request.QueryRequest;
import com.rallydev.rest.response.QueryResponse;
import com.rallydev.rest.util.Fetch;
import com.rallydev.rest.util.QueryFilter;
import org.appling.rallyx.rally.RallyNode;
import org.appling.rallyx.rally.RallyQueryFactory;

import java.io.IOException;
import java.util.*;

/**
 * Created by sappling on 9/5/2016.
 */
public class InitiativeNodeFinder {
    private RallyRestApi restApi;
    private ArrayList<RallyNode> stories = new ArrayList<>();

    public InitiativeNodeFinder(RallyRestApi restApi) {
        this.restApi = restApi;
    }

    public RallyNode getInitiativeTree(String initiativeID) throws IOException {
        QueryResponse queryResponse = restApi.query(RallyQueryFactory.findInitiative(initiativeID));


        if (queryResponse.wasSuccessful()) {
            JsonArray resultArray = queryResponse.getResults();
            JsonElement jsonInitiative = resultArray.get(0);
            RallyNode node = new RallyNode(jsonInitiative.getAsJsonObject(), null, null);
            walk(node, node, null);
            return node;
        } else {
            throw new IOException("Error retrieving initiative '"+initiativeID+"'");
        }
    }

    public void walk(RallyNode parentNode, RallyNode initiative, RallyNode feature)  {
        //WalkAction action,
        //parentNative = action.act(rallyObject, parentNative, depth);


        String id = parentNode.getObjectID();
        if (parentNode.isUserStory()) {
            stories.add(parentNode);
        }

        // remove the stories that are found beneath the Initiative
        /*
        if (releaseStories.containsKey(id)) {
            releaseStories.remove(id);
        }
        */

        if (parentNode.hasChildren()) {
            QueryRequest queryRequest = RallyQueryFactory.getChildren(id, parentNode.getType());

            try {
                QueryResponse response = restApi.query(queryRequest);
                if (response.wasSuccessful()) {
                    JsonArray jsonArray = response.getResults();
                    for (JsonElement jsonEl : jsonArray) {
                        RallyNode next = new RallyNode(jsonEl.getAsJsonObject(), initiative, feature);
                        if (next.getType().equals("PortfolioItem/Feature")) {
                            feature = next;
                        }
                        parentNode.addChild(next);
                        walk(next, initiative, feature);
                    }
                }
            } catch (IOException e) {
                System.err.println("Exception getting children from: "+queryRequest.toUrl());
                e.printStackTrace();
            }
        }
    }

    public List<RallyNode> getStories() { return stories; }
}
