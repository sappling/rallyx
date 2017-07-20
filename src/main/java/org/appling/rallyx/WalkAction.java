package org.appling.rallyx;

import com.google.gson.JsonObject;

/**
 * Created by sappling on 9/5/2016.
 */
public interface WalkAction {
    Object act(JsonObject RallyObject, Object parentNative, int depth);
}
