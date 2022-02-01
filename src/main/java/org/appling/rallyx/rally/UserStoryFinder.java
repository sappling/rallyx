package org.appling.rallyx.rally;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.rallydev.rest.RallyRestApi;
import com.rallydev.rest.response.QueryResponse;

import java.io.IOException;
import java.util.*;

/**
 * Created by sappling on 7/21/2017.
 */
public class UserStoryFinder {
    private static final String INP_ITERATION_SEARCH = "I&P";

    private RallyRestApi restApi;
    private String releaseName;
    private boolean skipInP = false;
    private boolean findComplete = true;
    private Optional<String> project = Optional.empty();

    public UserStoryFinder(RallyRestApi restApi) {
        this.restApi = restApi;
    }

    public void setRelease(String releaseName) { this.releaseName = releaseName; }

    public void setFindComplete(boolean findComplete) {
        this.findComplete = findComplete;
    }
    public void setSkipInP(boolean skipInP) { this.skipInP = skipInP; }

    public void setProject(Optional<String> project) { this.project = project; }

    public List<RallyNode> getStories() throws IOException {
        ArrayList<RallyNode> result = new ArrayList<>();

        QueryResponse response = restApi.query(RallyQueryFactory.findStoriesInRelease(releaseName, getProjectRef()));
        if (response.wasSuccessful()) {
            JsonArray jsonElements = response.getResults();
            for (JsonElement element : jsonElements) {
                RallyNode next = new RallyNode(element.getAsJsonObject(), null, null, null );
                if (skipInP) {

                }
                if (findComplete == false) {
                    ScheduleState scheduleState = next.getScheduleState();
                    if ((scheduleState == ScheduleState.Completed) || (scheduleState == ScheduleState.Accepted)) {
                        continue;
                    }
                }
                if (skipInP && isInInP(next)) {
                    continue;
                }
                result.add(next);
            }
        }
        return result;
    }

    public List<RallyNode> getDefects() throws IOException {
        ArrayList<RallyNode> result = new ArrayList<>();

        QueryResponse response = restApi.query(RallyQueryFactory.findDefectsInRelease(releaseName, getProjectRef()));
        if (response.wasSuccessful()) {
            JsonArray jsonElements = response.getResults();
            for (JsonElement element : jsonElements) {
                RallyNode next = new RallyNode(element.getAsJsonObject(), null, null, null );
                if (findComplete == false) {
                    DefectState defectState = next.getDefectState();
                    if ((defectState == DefectState.Closed) || (defectState == DefectState.Fixed)) {
                        continue;
                    }
                }
                if (skipInP && isInInP(next)) {
                    continue;
                }
                result.add(next);
            }
        }
        return result;
    }


    private Optional<String> getProjectRef() throws IOException
    {
        if (project.isPresent()) {
            String ref = "";
            QueryResponse response = restApi.query( RallyQueryFactory.findProject( project.get() ) );
            if (response.wasSuccessful()) {
                JsonArray results = response.getResults();
                if (results.size() > 0) {
                    JsonObject first = results.get( 0 ).getAsJsonObject();
                    ref = first.get( "_ref" ).getAsString();
                }
            }
            return Optional.of(ref);
        } else {
            return project;
        }
    }

    public static boolean isInInP(RallyNode node) {
        boolean result = false;
        Iteration iteration = node.getIteration();
        if (iteration!=null && iteration.getName().contains("I&P")) {
            result = true;
        }
        return result;
    }
}
