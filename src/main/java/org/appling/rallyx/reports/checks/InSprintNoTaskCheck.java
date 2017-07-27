package org.appling.rallyx.reports.checks;

import org.appling.rallyx.rally.Iteration;
import org.appling.rallyx.rally.RallyNode;
import org.appling.rallyx.rally.ScheduleState;
import org.appling.rallyx.rally.StoryStats;
import org.appling.rallyx.reports.Issue;
import org.appling.rallyx.reports.RallyCheck;
import org.jetbrains.annotations.Nullable;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by sappling on 7/26/2017.
 */
public class InSprintNoTaskCheck implements RallyCheck {
    private Date twoDaysAgo;

    public InSprintNoTaskCheck() {
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -2); // back two days
        twoDaysAgo = calendar.getTime();
    }

    @Override
    @Nullable
    public Issue getIssue(RallyNode node, StoryStats stats) {
        Issue result = null;
        // only a problem for leaf nodes
        if (node.getChildren().isEmpty()){
            ScheduleState scheduleState = node.getScheduleState();
            Iteration iteration = node.getIteration();
            if (iteration != null) {
                if(node.getTaskEstimateTotal() == 0) {
                    Date startDate = iteration.getStartDate();
                    if (startDate != null) {
                        if (startDate.compareTo(twoDaysAgo) < 0) { // if start date is before two days ago
                            if (node.getScheduleState() != ScheduleState.Accepted) { // if not yet accepted
                                result = new Issue(node, Issue.Severity.Warning, "Story in sprint with no tasks");
                            }
                        }
                    }
                }
            }
        }
        return result;
    }
}
