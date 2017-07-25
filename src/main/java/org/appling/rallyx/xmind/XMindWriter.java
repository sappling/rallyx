package org.appling.rallyx.xmind;

import org.appling.rallyx.WalkAction;
import org.appling.rallyx.rally.RallyNode;
import org.xmind.core.*;
import org.xmind.core.marker.IMarker;
import org.xmind.core.marker.IMarkerGroup;
import org.xmind.core.marker.IMarkerSheet;
import org.xmind.core.style.IStyle;
import org.xmind.core.style.IStyleSheet;
import org.xmind.ui.style.Styles;

import java.io.*;
import java.util.Collection;
import java.util.Set;

/**
 * Created by sappling on 9/5/2016.
 */
public class XMindWriter implements WalkAction {
    private IWorkbook workbook;
    private ISheet sheet;
    private String filePath;
    private IMarker greenCheck;
    private IStyle markthroughStyle;
    private Set<RallyNode> releaseNodes;

    public XMindWriter(String filePath, Set<RallyNode> releaseNodes) {
        this.filePath = filePath;
        this.releaseNodes = releaseNodes;
        IWorkbookBuilder builder = Core.getWorkbookBuilder();
        workbook = builder.createWorkbook();
        sheet = workbook.getPrimarySheet();
        sheet.setTitleText("Initiative");

        IMarkerSheet markerSheet = workbook.getMarkerSheet();
        IMarkerGroup markerGroup = markerSheet.createMarkerGroup(true);
        markerGroup.setName("schedule");
        try {
            greenCheck = createMarker("completed", "markers/GreenCheck.png", workbook, markerGroup);
        } catch (IOException e) {
            e.printStackTrace();
        }

        markthroughStyle = addMarkthroughStyle();
    }

    private IStyle addMarkthroughStyle() {
        IStyleSheet ss = workbook.getStyleSheet();
        IStyle markthrough = ss.createStyle("topic");
        markthrough.setProperty(Styles.TextDecoration, Styles.TEXT_DECORATION_LINE_THROUGH);
        markthrough.setProperty(Styles.TEXT_DECORATION_LINE_THROUGH, "true");
        ss.addStyle(markthrough, IStyleSheet.NORMAL_STYLES);
        return markthrough;
    }

    private ITopic getRootTopic() {
        return sheet.getRootTopic();
    }

    public void save() throws IOException, CoreException {
        workbook.save(filePath);
    }


    @Override
    public Object act(RallyNode node, Object parentNative, int depth) {
        ITopic parent = (ITopic) parentNative;
        ITopic newTopic;

        if (parent == null) {
            newTopic = getRootTopic();
            newTopic.setStructureClass("org.xmind.ui.map.clockwise");

        } else {
            newTopic = workbook.createTopic();
        }

        populateTopic(newTopic, node);

        if (parent != null) {
            parent.add(newTopic, ITopic.ATTACHED);
        }


        /*
        IMarkerSheet markerSheet = workbook.getMarkerSheet();
        markerSheet.
        markerSheet.findMarkerGroup()
        */
        /*
        IHtmlNotesContent noteContent = (IHtmlNotesContent) workbook.createNotesContent(INotes.HTML);
        noteContent.
        //obj.get("Description")
        */
        return newTopic;
    }

    public void addOrphans(Collection<RallyNode> orphans) {
        ISheet orphanSheet = workbook.createSheet();
        orphanSheet.setTitleText("orphans");
        workbook.addSheet(orphanSheet);
        ITopic root = orphanSheet.getRootTopic();
        root.setTitleText("orphans");
        root.setStructureClass("org.xmind.ui.tree.right");

        for (RallyNode orphan : orphans) {
            ITopic topic = workbook.createTopic();
            populateTopic(topic, orphan);
            root.add(topic, ITopic.ATTACHED);
        }
    }

    private ITopic populateTopic(ITopic newTopic, RallyNode node) {
        String title = node.getName() + " [" +
                node.getFormattedId() + "]";
        newTopic.setTitleText(title);
        newTopic.setHyperlink(node.getURL());

        String scheduleState = node.getScheduleState();
        if (scheduleState.equalsIgnoreCase("Accepted") || (scheduleState.equalsIgnoreCase("Completed") )) {
            newTopic.addMarker(greenCheck.getId());
        }

        // mark through any leaf nodes that aren't in release
        if (!node.hasChildren() && !releaseNodes.contains(node)) {
            newTopic.setStyleId(markthroughStyle.getId());
        }
        return newTopic;
    }


    public static IMarker createMarker(String markerName, String iconPath, IWorkbook workbook, IMarkerGroup group) throws IOException {
        IMarkerSheet ims = workbook.getMarkerSheet();
        File iconFile = new File(iconPath);
        InputStream is = new FileInputStream(iconFile);
        if (is == null) {
            throw new IOException("Error reading "+iconFile.getPath());
        }
        IMarker marker = ims.createMarker(iconFile.getName());
        marker.setName(markerName);
        OutputStream os = marker.getResource().getOutputStream();
        org.xmind.core.util.FileUtils.transfer(is, os, true);
        group.addMarker(marker);
        return marker;
    }

}
