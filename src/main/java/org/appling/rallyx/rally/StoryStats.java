package org.appling.rallyx.rally;

import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by sappling on 7/23/2017.
 */
public class StoryStats {
    private Set<RallyNode> storiesInRelease = new HashSet<>();
    private Set<RallyNode> storiesUnderInitiative = new HashSet<>();
    private Set<RallyNode> defectsInRelease = new HashSet<>();
    private Set<RallyNode> defectsUnderInitiative = new HashSet<>();
    private Set<RallyNode> storiesNotInProject = new HashSet<>();
    private Set<RallyNode> storiesNotInInitiative = new HashSet<>();
    private Set<RallyNode> defectsNotInInitiative = new HashSet<>();
    private Set<RallyNode> storiesNotInRelease = new HashSet<>();
    private Set<RallyNode> storiesInNoRelease = new HashSet<>();
    private Set<RallyNode> defectsInNoRelease = new HashSet<>();
    private Set<RallyNode> allStories = new HashSet<>();
    private Set<RallyNode> allDefects = new HashSet<>();
    private HashMap<String,RallyNode> allNodesByFormattedId = new HashMap<>();
    private String releaseName;
    private boolean hideBugHolder;

    private List<RallyNode> initiatives;

    public StoryStats(@Nullable List<RallyNode> storiesInReleaseList, @Nullable List<RallyNode> storiesUnderInitiativeList, @Nullable List<RallyNode> defectsInReleaseList, @Nullable List<RallyNode> defectsUnderInitiativeList,
                      boolean hideBugHolder, List<RallyNode> initiatives, String releaseName) {
        this.initiatives = initiatives;
        this.releaseName = releaseName;
        if (storiesInReleaseList != null) {
            storiesInRelease = new HashSet<>(storiesInReleaseList);
        }
        if (storiesUnderInitiativeList != null) {
            storiesUnderInitiative = new HashSet<>(storiesUnderInitiativeList);
        }
        if (defectsInReleaseList != null) {
            defectsInRelease = new HashSet<>(defectsInReleaseList);
        }
        if (defectsUnderInitiativeList != null) {
            defectsUnderInitiative = new HashSet<>(defectsUnderInitiativeList);
        }
        this.hideBugHolder = hideBugHolder;
        calculateSets();
    }

    public RallyNode getFirstInitiative() {
        return initiatives.get(0);
    }

    public List<RallyNode> getInitiatives() {
        return initiatives;
    }
    public String getReleaseName() { return releaseName; }

    public List<RallyNode> getFeatures() {
        ArrayList<RallyNode> features = new ArrayList<>();
        for (RallyNode initiative : initiatives) {
            features.addAll(initiative.getChildren());
        }
        return features;
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
    public Set<RallyNode> getStoriesInNoRelease() {
        return Collections.unmodifiableSet(storiesInNoRelease);
    }

    public Set<RallyNode> getAllStories() {
        return Collections.unmodifiableSet(allStories);
    }

    public Set<RallyNode> getDefectsUnderInitiative() {
        return Collections.unmodifiableSet(defectsUnderInitiative);
    }
    public Set<RallyNode> getDefectsInRelease() {
        return Collections.unmodifiableSet(defectsInRelease);
    }

    public Set<RallyNode> getDefectsInNoRelease() {
        return Collections.unmodifiableSet(defectsInNoRelease);
    }
    public Set<RallyNode> getDefectsNotInInitiative() {
        return Collections.unmodifiableSet(defectsNotInInitiative);
    }
    public Set<RallyNode> getAllDefects() {
        return Collections.unmodifiableSet(allDefects);
    }

    @Nullable
    public RallyNode getNodeByFormattedId(String formattedId) {
        return allNodesByFormattedId.get(formattedId);
    }

    /**
     * Gets all nodes (both Features under the Initiative and Stories) with the specified Tag
     * @return
     */
    public Set<RallyNode> getNodesWithTag(String tag) {
        Set<RallyNode> result = new HashSet<>(  );
        for (RallyNode initiative : initiatives) {
            result.addAll( initiative.getChildren().stream().filter( c -> c.hasTag( tag ) ).collect( Collectors.toSet() ));
        }
        result.addAll(getAllStories().stream().filter( c -> c.hasTag( tag ) ).collect( Collectors.toSet()));
        return result;
    }

    public void printStats() {
        int totalStoriesNotInProject = getStoriesNotInProject().size();
//        System.out.format("%d stories total\n", allStories.size());
//        if (totalStoriesNotInProject > 0) {
//            System.out.format("%d stories were in the project and %d were not\n", allStories.size() - totalStoriesNotInProject, totalStoriesNotInProject);
//        }
        System.out.format("%d stories total\n", allStories.size() - getStoriesNotInProject().size());
        System.out.format("%d stories not in initiative\n", storiesNotInInitiative.size());
        /*
        System.out.format("%d stories not in specified release\n", storiesNotInRelease.size());
        if (storiesNotInRelease.size() > 0) {
            System.out.println("   "+storiesNotInRelease.stream().map(RallyNode::getFormattedId).collect(Collectors.joining(",")));
        }
        */
        System.out.format("%d stories in no release\n", storiesInNoRelease.size());
        System.out.format("%d defects in total\n", allDefects.size());
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

        defectsNotInInitiative = new HashSet<>(defectsInRelease);
        defectsNotInInitiative.removeAll(defectsUnderInitiative);

        allDefects = new HashSet<>(defectsUnderInitiative);
        allDefects.addAll(defectsNotInInitiative);

        // find defects in no release
        defectsInNoRelease = defectsUnderInitiative.stream()
                .filter(s -> s.getRelease().isEmpty())
                .collect(Collectors.toSet());

        // add to map of nodes by formatted ID
        allStories.forEach(this::updateFormattedIdMap);
        allDefects.forEach(this::updateFormattedIdMap);
        getFeatures().forEach(this::updateFormattedIdMap);
        for (RallyNode initiative : initiatives) {
            updateFormattedIdMap(initiative);
        }
    }

    private void updateFormattedIdMap(RallyNode node) {
        allNodesByFormattedId.put(node.getFormattedId(), node);
    }


    private Set<RallyNode> removeParents(Set<RallyNode> set) {
        HashSet<RallyNode> results = new HashSet<>();
        for (RallyNode rallyNode : set) {
            if (!rallyNode.hasChildren() && !(hideBugHolder && rallyNode.hasTag(Tags.BUGHOLDER))) {
                results.add(rallyNode);
            }
        }
        return results;
    }


}
