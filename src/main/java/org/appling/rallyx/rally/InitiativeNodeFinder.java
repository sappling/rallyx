package org.appling.rallyx.rally;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.rallydev.rest.RallyRestApi;
import com.rallydev.rest.request.QueryRequest;
import com.rallydev.rest.response.QueryResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by sappling on 9/5/2016.
 */
public class InitiativeNodeFinder {
    private RallyRestApi restApi;
    private ArrayList<RallyNode> stories = new ArrayList<>();
    private boolean findComplete = true;
    private Optional<String> project = Optional.empty();

    public InitiativeNodeFinder(RallyRestApi restApi) {
        this.restApi = restApi;
    }

    public void setFindComplete(boolean findComplete) {
        this.findComplete = findComplete;
    }

    public void setProject(Optional<String> project) {
        this.project = project;
    }

    public RallyNode getInitiativeTree(String initiativeID) throws IOException {
        QueryResponse queryResponse = restApi.query(RallyQueryFactory.findInitiative(initiativeID));


        if (queryResponse.wasSuccessful()) {
            JsonArray resultArray = queryResponse.getResults();
            JsonElement jsonInitiative = resultArray.get(0);
            RallyNode node = new RallyNode(jsonInitiative.getAsJsonObject(), null, null, null );
            walk(node, node, null, null );
            return node;
        } else {
            throw new IOException("Error retrieving initiative '"+initiativeID+"'");
        }
    }

    public void walk( RallyNode parentNode, RallyNode initiative, RallyNode feature, RallyNode mmf )  {
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
                        RallyNode next = new RallyNode(jsonEl.getAsJsonObject(), initiative, feature, mmf );
                        if (next.getType().equals("PortfolioItem/Feature")) {
                            feature = next;
                        }
                        if (shouldAddChild(next)) {
                            RallyNode parentMMf = null;
                            if (next.hasTag( Tags.MMF )) {
                                parentMMf = next;
                            }
                            parentNode.addChild(next);
                            walk(next, initiative, feature, parentMMf );
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("Exception getting children from: "+queryRequest.toUrl());
                e.printStackTrace();
            }
        }
    }

    private boolean shouldAddChild(RallyNode next) {
        boolean result = true;
//        if (next.isUserStory()) {
//        }
        if (!findComplete) {
            ScheduleState scheduleState = next.getScheduleState();
            if ((scheduleState == ScheduleState.Completed) || (scheduleState == ScheduleState.Accepted)) {
                result = false;
            }
        }
        if (project.isPresent()) {
            if (!next.getProject().getName().equals( project.get() ) ) {
                result = false;
            }
        }

        return result;
    }

    public List<RallyNode> getStories() { return stories; }
}
