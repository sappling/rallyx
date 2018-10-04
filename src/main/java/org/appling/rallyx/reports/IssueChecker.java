package org.appling.rallyx.reports;

import org.appling.rallyx.rally.RallyNode;
import org.appling.rallyx.rally.StoryStats;
import org.appling.rallyx.reports.checks.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by sappling on 7/27/2017.
 */
public class IssueChecker {
    private final StoryStats stats;
    ArrayList<RallyCheck> checkers;

    public IssueChecker(StoryStats stats) {
        this.stats = stats;
        checkers = new ArrayList<>();
        checkers.add(new NoReleaseCheck());
        checkers.add(new NoContentCheck());
        checkers.add(new OldSprintUnacceptedCheck());
        checkers.add(new InSprintNoTaskCheck());
        checkers.add(new UnestimatedStoryCheck());
        checkers.add(new NotAssignedToTeamCheck());
        checkers.add(new NotInInitiativeCheck());
    }

    public List<Issue> doChecks() {
        ArrayList<Issue> result = new ArrayList<>();
        Set<RallyNode> allStories = stats.getAllStories();
        for (RallyNode nextStory : allStories) {
            for (RallyCheck checker : checkers) {
                Issue newIssue = checker.getIssue(nextStory, stats);
                if (newIssue != null) {
                    result.add(newIssue);
                }
            }
        }
        return result;
    }
}
