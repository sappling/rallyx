package org.appling.rallyx.reports.checks;

import org.appling.rallyx.rally.RallyNode;
import org.appling.rallyx.rally.StoryStats;
import org.appling.rallyx.reports.Issue;
import org.appling.rallyx.reports.RallyCheck;
import org.jetbrains.annotations.Nullable;

/**
 * Created by sappling on 7/26/2017.
 */
public class UnestimatedStoryCheck implements RallyCheck {
    @Override
    @Nullable
    public Issue getIssue(RallyNode node, StoryStats stats) {
        Issue result = null;
        // only a problem for leaf node children.  Parents can't be assigned to a release
        if (node.getChildren().isEmpty()){
            if (node.getPlanEstimate() == 0) {
                result = new Issue(node, Issue.Severity.Error, "Unestimated Story");
            }
        }
        return result;
    }
}
