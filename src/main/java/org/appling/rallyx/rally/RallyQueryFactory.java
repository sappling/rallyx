package org.appling.rallyx.rally;

import com.rallydev.rest.request.QueryRequest;
import com.rallydev.rest.util.Fetch;
import com.rallydev.rest.util.QueryFilter;

/**
 * Created by sappling on 7/19/2017.
 */
public class RallyQueryFactory {
    private static final Fetch standardFetch = new Fetch("FormattedID", "Name", "Children", "Feature", "ObjectID", "DragAndDropRank", "Project", "TaskEstimateTotal", "PlanEstimate", "ScheduleState", "Iteration", "StartDate", "EndDate", "Description", "DirectChildrenCount", "Release", "_ref", "_type", "UserStories");

    public static QueryRequest findInitiative(String initiativeID) {
        QueryRequest request = new QueryRequest("PortfolioItem/Initiative");

        request.setFetch(new Fetch("FormattedID", "Name", "Children", "Description", "ObjectID", "DirectChildrenCount", "Project", "_ref", "_type"));

        request.setQueryFilter(new QueryFilter("FormattedID", "=", initiativeID));

        //request.setPageSize(1);
        request.setLimit(10000);

        return request;
    }

    public static QueryRequest getProjects() {
        QueryRequest request = new QueryRequest("Project");
        request.setFetch(new Fetch("Name", "_ref", "State", "Children", "ObjectID", "Description", "Parent"));
        request.setScopedDown(true);
        request.setScopedUp(true);
        request.setLimit(10000);

        return request;
    }

    public static QueryRequest getChildren(String id, String parentType) {
        QueryRequest request;

        // Unfortunately rally denotes parentage differently for different types
        if (parentType.equals("PortfolioItem/Feature")) {
            request = new QueryRequest("HierarchicalRequirement");
            // Has this feature, but no parent.  Only want the direct feature children, not grandchildren
            request.setQueryFilter(new QueryFilter("Feature.ObjectID", "=", id)
                    .and(new QueryFilter("Parent", "=", "null")));
        } else if (parentType.equals("HierarchicalRequirement")) {
            request = new QueryRequest("HierarchicalRequirement");
            request.setQueryFilter(new QueryFilter("Parent.ObjectID", " = ", id));
        } else { // must be an Initiative
            request = new QueryRequest("PortfolioItem/Feature");
            request.setQueryFilter(new QueryFilter("Parent.ObjectID", "=", id));
        }
        request.setOrder("DragAndDropRank ASC");
        request.setFetch(standardFetch);
        return request;
    }

    public static QueryRequest findRelease(String name) {
        QueryRequest request = new QueryRequest("Release");
        request.setQueryFilter(new QueryFilter("Name", "=", name));
        request.setFetch(new Fetch("Name", "Project", "State", "ObjectID"));
        request.setLimit(10000);
        request.setScopedDown(true);
        return request;
    }

    public static QueryRequest findStoriesInRelease(String name) {
        QueryRequest request = new QueryRequest("HierarchicalRequirement");
        request.setQueryFilter(new QueryFilter("Release.Name", "=", name));
        request.setFetch(standardFetch);
        request.setOrder("DragAndDropRank ASC");
        request.setLimit(10000);
        return request;
    }
}
