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
public class OldSprintUnacceptedCheck implements RallyCheck {
    private Date twoWeeksAgo;

    public OldSprintUnacceptedCheck() {
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -14); // back two weeks
        twoWeeksAgo = calendar.getTime();
    }

    @Override
    @Nullable
    public Issue getIssue(RallyNode node, StoryStats stats) {
        Issue result = null;
        // only a problem for leaf nodes
        if (node.getChildren().isEmpty()){
            ScheduleState scheduleState = node.getScheduleState();
            if (scheduleState != null) {
                if (scheduleState != ScheduleState.Accepted) {
                    Iteration iteration = node.getIteration();
                    if (iteration != null) {
                        Date endDate = iteration.getEndDate();
                        if (endDate != null) {
                            if (endDate.compareTo(twoWeeksAgo) < 0) { // if end date is before two weeks ago
                                result = new Issue(node, Issue.Severity.Warning, "Story incomplete in old Sprint");
                            }
                        }
                    } else {
                        if (scheduleState == ScheduleState.Completed) {
                            result = new Issue(node, Issue.Severity.Warning, "Story unaccepted, but completed and in no Sprint");
                        }
                    }
                }
            }
        }
        return result;
    }
}
