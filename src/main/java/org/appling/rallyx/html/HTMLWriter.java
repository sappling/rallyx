package org.appling.rallyx.html;

import org.appling.rallyx.WalkAction;
import org.appling.rallyx.rally.RallyNode;
import org.appling.rallyx.rally.StoryStats;

import java.io.PrintWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

/**
 * Created by sappling on 7/30/2017.
 */
public class HTMLWriter  implements WalkAction {
    private StoryStats stats;
    private PrintWriter writer;

    public HTMLWriter(Writer w, StoryStats stats) {
        writer = new PrintWriter(w);
        this.stats = stats;
    }

    public void writeHeader() {
        writer.println("<html>");
        writer.println("<body>");
    }

    public void writeFooter() {
        writer.println("</body>");
        writer.println("</html>");
        writer.flush();
    }


    public void close() {
        writeOrphans();
        writeFooter();
        writer.close();
    }


    @Override
    // For this writer, there are no native objects, so we will just pass a Boolean indicating if it is a the root
    public Object act(RallyNode node, Object parentNative, int depth) {
        if (parentNative != null) {
            writeHeader();
        }

        if (node != null) {
            writeNode(node, depth);
        }

        return null;
    }

    private void writeNode(RallyNode node, int depth) {
        writer.print("<h"+depth+">");
        writer.print("<a href=\""+node.getURL()+"\">");
        writer.print(node.getFormattedId());
        writer.print("</a>");
        writer.print(" - ");
        writer.print(node.getName());
        writer.println("</h" + depth + ">");

        writer.println("<div style=\""+getMargin(depth)+"\">");
        writer.println(node.getDescription());
        writer.println("</div>");
    }


    private void writeOrphans() {
        writer.print("<h1>Not In Initiative</h1>");
        Set<RallyNode> orphans = stats.getStoriesNotInInitiative();
        for (RallyNode orphan : orphans) {
            writeNode(orphan, 2);
        }
    }

    private String getMargin(int depth) {
        StringBuilder result = new StringBuilder();
        if (depth > 0) {
            double val = depth * 0.25;
            result.append("margin-left:");
            result.append(Double.toString(val));
            result.append("in");
        }
        return result.toString();
    }

    public static String ensureExtention(String outName, String defaultName) {
        String result = outName;
        if (outName == null || outName.length()==0) {
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd-");
            result = fmt.format(new Date())+defaultName;
        } else if (!outName.endsWith(".html")) {
            result += ".html";
        }
        return result;
    }

}
