package org.appling.rallyx.reports;

import org.appling.rallyx.rally.RallyNode;

/**
 * Created by sappling on 7/26/2017.
 */
public class Issue {
    private final RallyNode story;
    private final String message;
    private final Severity severity;

    public Issue(RallyNode story, Severity severity, String message) {
        this.story = story;
        this.severity = severity;
        this.message = message;
    }

    public RallyNode getStory() {
        return story;
    }

    public String getMessage() {
        return message;
    }

    public Severity getSeverity() {
        return severity;
    }

    public enum Severity { Warning, Error };
}
