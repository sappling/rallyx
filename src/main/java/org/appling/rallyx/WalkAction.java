package org.appling.rallyx;

import com.google.gson.JsonObject;
import org.appling.rallyx.rally.RallyNode;

/**
 * Created by sappling on 9/5/2016.
 */
public interface WalkAction {
    Object act(RallyNode node, Object parentNative, int depth);
}
