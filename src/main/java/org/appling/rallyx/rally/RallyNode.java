package org.appling.rallyx.rally;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by sappling on 7/20/2017.
 */
public class RallyNode {
    private static final String FIELD_TYPE = "_type";
    private static final String FIELD_FMT_ID = "FormattedID";
    private static final String FIELD_NAME = "Name";
    private static final String FIELD_SSTATE = "ScheduleState";
    private static final String FIELD_OBJID = "ObjectID";
    private static final String FIELD_CHILDCOUNT = "DirectChildrenCount";
    private static final String FIELD_RELEASE = "Release";
    private static final String TYPE_US = "HierarchicalRequirement";

    private JsonObject jsonObject;
    private ArrayList<RallyNode> children;
    private String id;

    public RallyNode(JsonObject jsonObject) {
        this.jsonObject = jsonObject;
        id = getStringField(FIELD_OBJID);
        children = new ArrayList<>();
    }

    @NotNull
    private String getStringField(String fieldName) {
        String result = "";
        JsonElement element = jsonObject.get(fieldName);
        if (element != null) {
            result = element.getAsString();
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
    public String getScheduleState() { return getStringField(FIELD_SSTATE); }

    @NotNull
    public String getRelease() {
        String result = "";
        JsonElement el = jsonObject.get(FIELD_RELEASE);
        if (el != null && !el.isJsonNull()) {
            JsonObject releaseObj = el.getAsJsonObject();
            if (releaseObj != null) {
                result = releaseObj.get("Name").getAsString();
            }
        }
        return result;
    }

    public void addChild(RallyNode child) {
        children.add(child);
    }

    public List<RallyNode> getChildren() {
        return Collections.unmodifiableList(children);
    }

    public boolean hasChildren() {
        String countString = getStringField(FIELD_CHILDCOUNT);
        return (countString.length() > 0 && !countString.equals("0"));
    }

    public boolean isUserStory() {
        return TYPE_US.equals(getType());
    }

    public String toString() {
        return getFormattedId()+": "+getName() + " |rel:"+getRelease();
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
