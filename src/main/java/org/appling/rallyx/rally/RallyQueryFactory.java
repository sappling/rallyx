package org.appling.rallyx.rally;

import com.rallydev.rest.request.QueryRequest;
import com.rallydev.rest.util.Fetch;
import com.rallydev.rest.util.QueryFilter;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Created by sappling on 7/19/2017.
 */
public class RallyQueryFactory {
    private static final Fetch standardFetch = new Fetch("FormattedID", "Name", "Children", "Defects", "Feature", "ObjectID", "DragAndDropRank", "PercentDoneByStoryCount","Project", "TaskEstimateTotal", "PlanEstimate", "State", "ScheduleState", "Iteration", "StartDate", "EndDate", "Description", "DirectChildrenCount", "Release", "_ref", "_type", "UserStories","Tags");

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

    public static QueryRequest getStory(String storyID) {
        QueryRequest request = new QueryRequest("HierarchicalRequirement");
        request.setQueryFilter(new QueryFilter("FormattedID", "=", storyID));
        request.setFetch(standardFetch);

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

    public static QueryRequest getDefectsForStory(String id) {
        QueryRequest request = new QueryRequest("Defect");
        request.setQueryFilter(new QueryFilter("Requirement.FormattedID", "=", id));
        request.setOrder("DragAndDropRank ASC");
        request.setFetch(standardFetch);

        request.setScopedDown(true);
        request.setScopedUp(true);
        request.setLimit(10000);

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

    public static QueryRequest findProject(String name) {
        QueryRequest request = new QueryRequest( "Project" );
        request.setQueryFilter(new QueryFilter("Name", "=", name));
        request.setFetch( new Fetch( "Name", "_ref" ) );
        request.setLimit(10000);
        request.setScopedDown(true);

        return request;
    }

    public static QueryRequest findStoriesInRelease(String name, Optional<String> projectRef) {
        QueryRequest request = new QueryRequest("HierarchicalRequirement");
        request.setScopedDown( true );
        request.setScopedUp( false );
        if (projectRef.isPresent()) {
            request.setProject(projectRef.get());
        }
        QueryFilter releaseFilter = new QueryFilter( "Release.Name", "=", name );
        QueryFilter filter = releaseFilter;

        request.setQueryFilter( filter);
        request.setFetch(standardFetch);
        request.setOrder("DragAndDropRank ASC");
        request.setLimit(10000);
        return request;
    }


    public static QueryRequest findDefectsInRelease(String name, Optional<String> projectRef) {
        QueryRequest request = new QueryRequest("Defect");
        request.setScopedDown( true );
        request.setScopedUp( false );
        if (projectRef.isPresent()) {
            request.setProject(projectRef.get());
        }
        QueryFilter releaseFilter = new QueryFilter( "Release.Name", "=", name );
        QueryFilter filter = releaseFilter;

        request.setQueryFilter( filter);
        request.setFetch(standardFetch);
        request.setOrder("DragAndDropRank ASC");
        request.setLimit(10000);
        return request;
    }
}
