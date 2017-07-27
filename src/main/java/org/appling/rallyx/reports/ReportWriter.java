package org.appling.rallyx.reports;

import org.appling.rallyx.rally.RallyNode;
import org.appling.rallyx.rally.StoryStats;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Created by sappling on 7/26/2017.
 */
public class ReportWriter {
    private StoryStats stats;
    private IssueChecker checker;

    public ReportWriter(StoryStats stats) {
        this.stats = stats;
        checker = new IssueChecker(stats);
    }

    public void write(String outName) throws FileNotFoundException {
        File outFile = new File(outName);

        PrintWriter out = new PrintWriter(outFile);

        List<Issue> issues = checker.doChecks();
        writeHeader(out, issues);
        writeBody(out, issues);
        writeFooter(out, issues);
        out.close();
    }

    private void writeHeader(PrintWriter out, List<Issue> issues) {
        out.println("<html><body>");
    }

    private void writeBody(PrintWriter out, List<Issue> issues) {
        //todo - need Java escaping - apache commons string processing
        //todo - add project
        if (issues.isEmpty()) {
            out.println("<div>No issues found</div>");
        } else{
            out.println("<table>");
            out.println("<tr>" +
                    "<th>ID</th>" +
                    "<th>Name</th>" +
                    "<th>Project</th>" +
                    "<th>Severity</th>" +
                    "<th>Issue</th>" +
                    "</tr>");
            for (Issue issue : issues) {
                StringBuilder row = new StringBuilder();
                RallyNode story = issue.getStory();
                row.append("<tr>");
                addCell(row, "<a target=\"_new\" href=\""+ story.getURL()+"\">"+ story.getFormattedId()+"</a>");
                addCell(row, story.getName());
                addCell(row, story.getProjectName());
                addCell(row, issue.getSeverity().toString());
                addCell(row, issue.getMessage());
                row.append("</tr>");
                out.println(row.toString());
            }
            out.println("</table>");
        }
    }

    private void addCell(StringBuilder builder, String content) {
        builder.append("<td>");
        builder.append(content);
        builder.append("</td>");
    }

    private void writeFooter(PrintWriter out, List<Issue> issues) {
        out.println("</body></html>");
    }

}

