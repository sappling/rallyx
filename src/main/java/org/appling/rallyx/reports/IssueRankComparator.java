package org.appling.rallyx.reports;

import org.appling.rallyx.rally.RallyNode;

import java.util.Comparator;

/**
 * Created by sappling on 7/27/2017.
 */
public class IssueRankComparator  implements Comparator<Issue> {
    @Override
    public int compare(Issue i1, Issue i2) {
        RallyNode n1 = i1.getStory();
        RallyNode n2 = i2.getStory();
        byte[] bytes1 = n1.getRank().getBytes();
        byte[] bytes2 = n2.getRank().getBytes();

        int length = Math.max(bytes1.length, bytes2.length);
        for (int i = 0; i < length; i++) {
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