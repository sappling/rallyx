package org.appling.rallyx.rally;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.rallydev.rest.RallyRestApi;
import com.rallydev.rest.response.QueryResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * Created by sappling on 7/21/2017.
 */
public class UserStoryFinder {
    private RallyRestApi restApi;
    private String releaseName;
    private boolean findComplete = true;

    public UserStoryFinder(RallyRestApi restApi) {
        this.restApi = restApi;
    }

    public void setRelease(String releaseName) { this.releaseName = releaseName; }

    public void setFindComplete(boolean findComplete) {
        this.findComplete = findComplete;
    }

    public List<RallyNode> getStories() throws IOException {
        ArrayList<RallyNode> result = new ArrayList<>();

        QueryResponse response = restApi.query(RallyQueryFactory.findStoriesInRelease(releaseName));
        if (response.wasSuccessful()) {
            JsonArray jsonElements = response.getResults();
            for (JsonElement element : jsonElements) {
                RallyNode next = new RallyNode(element.getAsJsonObject(), null, null);
                if (findComplete == false) {
                    ScheduleState scheduleState = next.getScheduleState();
                    if ((scheduleState == ScheduleState.Completed) || (scheduleState == ScheduleState.Accepted)) {
                        continue;
                    }
                }
                result.add(next);
            }
        }
        return result;
    }
}
