package org.appling.rallyx.rally;

import org.appling.rallyx.WalkAction;

import java.util.List;

/**
 * Created by sappling on 7/21/2017.
 */
public class RallyNodeWalker {
    private WalkAction action;

    public RallyNodeWalker(WalkAction action) {
        this.action = action;
    }

    public void walk(RallyNode node, Object parentNative, int depth) {
        Object newParent = action.act(node, parentNative, depth);

        List<RallyNode> children = node.getChildren();
        for (RallyNode child : children) {
            walk(child, newParent, depth+1);
        }
    }
}
