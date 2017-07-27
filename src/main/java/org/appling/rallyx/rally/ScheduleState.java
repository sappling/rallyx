package org.appling.rallyx.rally;

/**
 * Created by sappling on 7/27/2017.
 */
public enum ScheduleState {
    Idea, Defined, InProgress, Completed, Accepted;

    public static ScheduleState fromString(String stringValue) {
        if (stringValue.equals("In-Progress")) {
            return InProgress;
        } else {
            return valueOf(ScheduleState.class, stringValue);
        }
    }

    public String toString() {
        if (this == InProgress) {
            return "In-Progress";
        } else  {
            return super.toString();
        }
    }
}
