package org.appling.rallyx.reports.checks;

import org.appling.rallyx.rally.Project;
import org.appling.rallyx.rally.RallyNode;
import org.appling.rallyx.rally.ScheduleState;
import org.appling.rallyx.rally.StoryStats;
import org.appling.rallyx.reports.Issue;
import org.appling.rallyx.reports.RallyCheck;
import org.jetbrains.annotations.Nullable;

/**
 * Created by sappling on 7/27/2017.
 */
public class NotAssignedToTeamCheck implements RallyCheck {
    @Override
    @Nullable
    public Issue getIssue(RallyNode node, StoryStats stats) {
        Issue result = null;
        // only a problem for leaf nodes stories.
        if (!node.hasChildren()){
            Project project = node.getProject();
            // if project is not a leaf
            if (project!=null && project.hasChildren()) {
                if (node.getScheduleState() != ScheduleState.Accepted) {
                    result = new Issue(node, Issue.Severity.Warning, "Story not assigned to a team");
                }
            }
        }
        return result;
    }

}
