package org.appling.rallyx.reports;

import org.appling.rallyx.rally.RallyNode;
import org.appling.rallyx.rally.StoryStats;
import org.jetbrains.annotations.Nullable;

/**
 * Created by sappling on 7/26/2017.
 */
public interface RallyCheck {
    @Nullable
    Issue getIssue(RallyNode node, StoryStats stats);
}
