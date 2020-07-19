package org.appling.rallyx.rally;

import com.google.gson.JsonObject;

/**
 * Created by sappling on 7/27/2017.
 */
public class Project extends RallyNode  {
    private static final String FIELD_CHILDREN = "Children";
    private static final String FIELD_COUNT = "Count";

    public Project(JsonObject jsonObject) {
        //todo - need to pull out other subclasses of RallyNode so that I don't need these nulls
        super(jsonObject, null, null, null );
    }

    @Override
    public boolean hasChildren() {
        boolean result = false;

        String childCountString = getFieldInObjectAsString(FIELD_CHILDREN, FIELD_COUNT);
        if (childCountString!=null && childCountString.length()> 0) {
            int count = Integer.parseInt(childCountString);
            result = (count > 0);
        }
        return result;
    }
}
