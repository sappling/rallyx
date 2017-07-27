package org.appling.rallyx.rally;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by sappling on 7/20/2017.
 */
public class RallyNode {
    private static final String FIELD_TYPE = "_type";
    private static final String FIELD_FMT_ID = "FormattedID";
    static final String FIELD_NAME = "Name";
    private static final String FIELD_SSTATE = "ScheduleState";
    private static final String FIELD_OBJID = "ObjectID";
    private static final String FIELD_CHILDCOUNT = "DirectChildrenCount";
    private static final String FIELD_RELEASE = "Release";
    private static final String FIELD_RANK = "DragAndDropRank";
    private static final String FIELD_PROJECT = "Project";
    private static final String FIELD_TASKESTTOT = "TaskEstimateTotal";
    private static final String FIELD_PLANESTIMATE = "PlanEstimate";
    private static final String FIELD_ITERATION = "Iteration";
    private static final String FIELD_DESCRIPTION = "Description";

    private static final String TYPE_US = "HierarchicalRequirement";

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

    private final JsonObject jsonObject;
    private final ArrayList<RallyNode> children;
    private final String id;
    private final RallyNode initiative;
    private final RallyNode feature;

    public RallyNode(JsonObject jsonObject, RallyNode initiative, RallyNode feature) {
        this.jsonObject = jsonObject;
        this.initiative = initiative;
        this.feature = feature;
        id = getStringField(FIELD_OBJID);
        children = new ArrayList<>();
    }

    @NotNull
    protected String getStringField(String fieldName) {
        String result = "";
        JsonElement element = jsonObject.get(fieldName);
        if (element != null && !element.isJsonNull()) {
            result = element.getAsString();
        }
        return result;
    }

    protected float getNumericField(String fieldName) {
        float result = 0;
        String stringField = getStringField(fieldName);
        if (stringField.length() > 0) {
            result = Float.parseFloat(stringField);
        }
        return result;
    }

    @Nullable
    protected Date getDateField(String fieldName) {
        Date result = null;
        JsonElement element = jsonObject.get(fieldName);
        if (element != null) {
            String dateString = element.getAsString();
            try {
                result = dateFormat.parse(dateString);
            } catch (ParseException e) {
                e.printStackTrace();
                // intentionally ignore and fall through to null result
            }
        }
        return result;
    }

    protected String getFieldInObjectAsString(String objectName, String fieldName) {
        String result = "";
        JsonElement el = jsonObject.get(objectName);
        if (el != null && !el.isJsonNull()) {
            JsonObject obj = el.getAsJsonObject();
            if (obj != null) {
                result = obj.get(fieldName).getAsString();
            }
        }
        return result;

    }

    @NotNull
    public String getType() { return getStringField(FIELD_TYPE); }

    @NotNull
    public String getObjectID() { return id; }

    @NotNull
    public String getFormattedId() { return getStringField(FIELD_FMT_ID); }

    @NotNull
    public String getName() { return getStringField(FIELD_NAME); }

    @NotNull
    public String getRank() { return getStringField(FIELD_RANK); }

    @NotNull
    public String getProjectName() { return getFieldInObjectAsString(FIELD_PROJECT, "Name"); }

    @Nullable
    public Project getProject() {
        Project result = null;
        if (jsonObject.has(FIELD_PROJECT)) {
            JsonElement jsonElement = jsonObject.get(FIELD_PROJECT);
            if (!jsonElement.isJsonNull()) {
                result = new Project(jsonElement.getAsJsonObject());
            }
        }
        return result;
    }

    public float getTaskEstimateTotal() { return getNumericField(FIELD_TASKESTTOT); }

    public float getPlanEstimate() { return getNumericField(FIELD_PLANESTIMATE); }

    @NotNull
    public String getIterationName() { return getFieldInObjectAsString(FIELD_ITERATION, "Name"); }

    @Nullable
    public Iteration getIteration() {
        Iteration result = null;
        if (jsonObject.has(FIELD_ITERATION)) {
            JsonElement jsonElement = jsonObject.get(FIELD_ITERATION);
            if (!jsonElement.isJsonNull()) {
                result = new Iteration(jsonElement.getAsJsonObject());
            }
        }
        return result;
    }

    @NotNull
    public String getDescription() { return getStringField(FIELD_DESCRIPTION); }

    @NotNull
    public String getScheduleStateName() { return getStringField(FIELD_SSTATE); }

    @Nullable
    public ScheduleState getScheduleState() {
        ScheduleState result = null;
        String ssName = getStringField(FIELD_SSTATE);
        if (ssName.length() > 0) {
            result = ScheduleState.fromString(ssName);
        }
        return result;
    }

    @Nullable
    public RallyNode getFeature() { return feature; }

    @NotNull
    public String getFeatureName() {
        String result = "";
        if (feature != null) {
            result = feature.getName();
        }
        return result;
    }

    @Nullable
    public RallyNode getInitiative() { return initiative; }

    @NotNull
    public String getInitiativeName() {
        String result = "";
        if (initiative != null) {
            result = initiative.getName();
        }
        return result;
    }

    @NotNull
    public String getRelease() {
        return getFieldInObjectAsString(FIELD_RELEASE, "Name");
    }

    public void addChild(RallyNode child) {
        children.add(child);
    }

    public List<RallyNode> getChildren() {
        return Collections.unmodifiableList(children);
    }

    //todo - this technique is specific to user stories I think had to override in Project
    public boolean hasChildren() {
        String countString = getStringField(FIELD_CHILDCOUNT);
        return (countString.length() > 0 && !countString.equals("0"));
    }

    public boolean isUserStory() {
        return TYPE_US.equals(getType());
    }

    public String getURL() {
        String objid = getObjectID();
        String type = getType().toLowerCase();
        if (isUserStory()) {
            type = "userstory";
        }

        return "https://rally1.rallydev.com/#/detail/"+type+"/"+objid;
    }


    public String toString() {
        return getFormattedId()+": "+getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RallyNode rallyNode = (RallyNode) o;

        return id.equals(rallyNode.id);

    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
