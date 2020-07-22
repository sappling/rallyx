package org.appling.rallyx.rally;

import org.appling.rallyx.rally.RallyNode;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by sappling on 7/23/2017.
 */
public class StoryStats {
    private Set<RallyNode> storiesInRelease = new HashSet<>();
    private Set<RallyNode> storiesUnderInitiative = new HashSet<>();
    private Set<RallyNode> storiesNotInProject = new HashSet<>();
    private Set<RallyNode> storiesNotInInitiative = new HashSet<>();
    private Set<RallyNode> storiesNotInRelease = new HashSet<>();
    private Set<RallyNode> storiesInNoRelease = new HashSet<>();
    private Set<RallyNode> allStories = new HashSet<>();
    private RallyNode initiative;

    public StoryStats(@Nullable List<RallyNode> storiesInReleaseList, @Nullable List<RallyNode> storiesUnderInitiativeList, RallyNode initiative) {
        this.initiative = initiative;
        if (storiesInReleaseList != null) {
            storiesInRelease = new HashSet<>(storiesInReleaseList);
        }
        if (storiesUnderInitiativeList != null) {
            storiesUnderInitiative = new HashSet<>(storiesUnderInitiativeList);
        }
        calculateSets();
    }

    public RallyNode getInitiative() {
        return initiative;
    }

    public Set<RallyNode> getStoriesInRelease() {
        return Collections.unmodifiableSet(storiesInRelease);
    }

    /**
     *
     * @return true if a release was specified when searching for stories
     */
    public boolean getReleaseSpecified() { return storiesInRelease != null; }

    public Set<RallyNode> getStoriesUnderInitiative() {
        return Collections.unmodifiableSet(storiesUnderInitiative);
    }

    public Set<RallyNode> getStoriesNotInInitiative() {
        return Collections.unmodifiableSet(storiesNotInInitiative);
    }

    public Set<RallyNode> getStoriesNotInProject() {
        return Collections.unmodifiableSet(storiesNotInProject);
    }

    public Set<RallyNode> getStoriesNotInRelease() {
        return Collections.unmodifiableSet(storiesNotInRelease);
    }

    public Set<RallyNode> getAllStories() {
        return Collections.unmodifiableSet(allStories);
    }

    /**
     * Gets all nodes (both Features under the Initiative and Stories) with the specified Tag
     * @return
     */
    public Set<RallyNode> getNodesWithTag(String tag) {
        Set<RallyNode> result = new HashSet<>(  );
        if (initiative != null) {
            result.addAll( initiative.getChildren().stream().filter( c -> c.hasTag( tag ) ).collect( Collectors.toSet() ));
        }
        result.addAll(getAllStories().stream().filter( c -> c.hasTag( tag ) ).collect( Collectors.toSet()));
        return result;
    }

    public void printStats() {
        int totalStoriesNotInProject = getStoriesNotInProject().size();
        System.out.format("%d stories total\n", allStories.size());
        if (totalStoriesNotInProject > 0) {
            System.out.format("%d stories were in the project and %d were not\n", allStories.size() - totalStoriesNotInProject, totalStoriesNotInProject);
        }
        System.out.format("%d stories not in initiative\n", storiesNotInInitiative.size());
        System.out.format("%d stories not in specified release\n", storiesNotInRelease.size());
        System.out.format("%d stories in no release\n", storiesInNoRelease.size());
    }

    private void calculateSets() {
        // find all stories in the release that are not in the initiative
        storiesNotInInitiative = new HashSet<>(storiesInRelease);
        storiesNotInInitiative.removeAll(storiesUnderInitiative);

        // If the InitiativeNodeFinder was set to includeNodesOutOfProject, then we may need to know which nodes are out of the project
        storiesNotInProject.addAll(storiesUnderInitiative.stream().filter(RallyNode::isOutOfProject).collect(Collectors.toSet()) );

        // find all stories in the initiative that are not in the release
        storiesNotInRelease = new HashSet<>(storiesUnderInitiative);
        storiesNotInRelease.removeAll(storiesInRelease);
        storiesNotInRelease = removeParents(storiesNotInRelease);

        // find stories in no release
        storiesInNoRelease = storiesNotInRelease.stream()
                .filter(s -> s.getRelease().isEmpty())
                .collect(Collectors.toSet());

        // all stories
        allStories = new HashSet<>(storiesUnderInitiative);
        allStories.addAll(storiesNotInInitiative);

    }

    private static Set<RallyNode> removeParents(Set<RallyNode> set) {
        HashSet<RallyNode> results = new HashSet<>();
        for (RallyNode rallyNode : set) {
            if (!rallyNode.hasChildren()) {
                results.add(rallyNode);
            }
        }
        return results;
    }


}
