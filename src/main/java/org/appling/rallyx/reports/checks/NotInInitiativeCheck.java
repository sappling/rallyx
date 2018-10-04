package org.appling.rallyx.reports.checks;

import org.appling.rallyx.rally.RallyNode;
import org.appling.rallyx.rally.ScheduleState;
import org.appling.rallyx.rally.StoryStats;
import org.appling.rallyx.reports.Issue;
import org.appling.rallyx.reports.RallyCheck;
import org.jetbrains.annotations.Nullable;

/**
 * Created by sappling on 7/26/2017.
 */
public class NotInInitiativeCheck implements RallyCheck {
    @Override
    @Nullable
    public Issue getIssue(RallyNode node, StoryStats stats) {
        Issue result = null;
        if (node.getInitiative() == null) {
            result = new Issue(node, Issue.Severity.Warning, "Story not under an Initiative");
        }
        return result;
    }
}
