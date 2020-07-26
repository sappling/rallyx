package org.appling.rallyx.rally;

/**
 * Created by sappling on 7/27/2017.
 */
public enum DefectState {
    Submitted,Open,Fixed,Closed,Unknown ;

    public static DefectState fromString(String stringValue) {
        try {
            return valueOf(DefectState.class, stringValue);
        } catch (Exception e) {
            return Unknown;
        }
    }
}
