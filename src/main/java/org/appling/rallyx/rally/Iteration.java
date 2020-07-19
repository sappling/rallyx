package org.appling.rallyx.rally;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

import java.util.Date;

/**
 * Created by sappling on 7/27/2017.
 */
public class Iteration extends RallyNode {
    private static final String FIELD_START = "StartDate";
    private static final String FIELD_END = "EndDate";

    public Iteration(JsonObject jsonObject) {
        //todo - need to pull out other subclasses of RallyNode so that I don't need these nulls
        super(jsonObject, null, null, null );
    }


    @Nullable
    public Date getStartDate() {
        return getDateField(FIELD_START);
    }

    @Nullable
    public Date getEndDate() {
        return getDateField(FIELD_END);
    }
}
