package org.appling.rallyx.rally;

import org.appling.rallyx.rally.RallyNode;

import java.util.Comparator;

/**
 * Created by sappling on 7/27/2017.
 */
public class RankComparator  implements Comparator<RallyNode> {
    @Override
    public int compare(RallyNode n1, RallyNode n2) {
        byte[] bytes1 = n1.getRank().getBytes();
        byte[] bytes2 = n2.getRank().getBytes();

        int length = Math.max(bytes1.length, bytes2.length);
        for (int i=0; i<length; i++) {
            int diff = 0;
            try {
                diff = bytes1[i] - bytes2[i];
            } catch (ArrayIndexOutOfBoundsException e) {
                // strings should be the same length, but assume shorter one comes first
                return bytes1.length - bytes2.length;
            }
            if (diff != 0) {
                return diff;
            }
        }
        return 0;
    }
}
