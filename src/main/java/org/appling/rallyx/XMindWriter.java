package org.appling.rallyx;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.xmind.core.*;
import org.xmind.core.marker.IMarker;
import org.xmind.core.marker.IMarkerGroup;
import org.xmind.core.marker.IMarkerResource;
import org.xmind.core.marker.IMarkerSheet;

import java.io.*;
import java.util.Collection;
import java.util.List;

/**
 * Created by sappling on 9/5/2016.
 */
public class XMindWriter implements WalkAction {
    private IWorkbook workbook;
    private ISheet sheet;
    private String filePath;
    private IMarker greenCheck;


    public XMindWriter(String filePath) {
        this.filePath = filePath;
        IWorkbookBuilder builder = Core.getWorkbookBuilder();
        workbook = builder.createWorkbook(filePath);
        sheet = workbook.getPrimarySheet();
        sheet.setTitleText("Initiative");

        IMarkerSheet markerSheet = workbook.getMarkerSheet();
        try {
            markerSheet.importFrom("markers");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CoreException e) {
            e.printStackTrace();
        }

        IMarkerGroup markerGroup = markerSheet.createMarkerGroup(true);
        markerGroup.setName("test");
        try {
            greenCheck = createMarker("greencheck", "markers/GreenCheck.png", workbook, markerGroup);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ITopic getRootTopic() {
        return sheet.getRootTopic();
    }

    public void close() throws IOException, CoreException {
        workbook.save(filePath);
    }

    public String getURL(JsonObject obj) {
        String objid = obj.get("ObjectID").getAsString();
        String type = obj.get("_type").getAsString().toLowerCase();
        if (type.equals("hierarchicalrequirement")) {
            type = "userstory";
        }

        return "https://rally1.rallydev.com/#/detail/"+type+"/"+objid;
    }


    @Override
    public Object act(JsonObject obj, Object parentNative, int depth) {
        ITopic parent = (ITopic) parentNative;
        ITopic newTopic;

        if (parent == null) {
            newTopic = getRootTopic();
            newTopic.setStructureClass("org.xmind.ui.map.clockwise");

        } else {
            newTopic = workbook.createTopic();
        }

        populateTopic(newTopic, obj);

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

    public void addOrphans(Collection<JsonObject> orphans) {
        ISheet orphanSheet = workbook.createSheet();
        orphanSheet.setTitleText("orphans");
        workbook.addSheet(orphanSheet);
        ITopic root = orphanSheet.getRootTopic();
        root.setTitleText("orphans");
        root.setStructureClass("org.xmind.ui.tree.right");

        for (JsonObject orphan : orphans) {
            ITopic topic = workbook.createTopic();
            populateTopic(topic, orphan);
            root.add(topic, ITopic.ATTACHED);
        }
    }

    private ITopic populateTopic(ITopic newTopic, JsonObject obj) {
        String title = obj.get("Name").getAsString() + " [" +
                obj.get("FormattedID").getAsString() + "]";
        newTopic.setTitleText(title);
        newTopic.setHyperlink(getURL(obj));

        String scheduleState = "";
        JsonElement ssObj = obj.get("ScheduleState");
        if (ssObj!=null) {
            scheduleState = ssObj.getAsString();
        }
        if (scheduleState.equalsIgnoreCase("Accepted") || (scheduleState.equalsIgnoreCase("Completed") )) {
            newTopic.addMarker(greenCheck.getId());
        }
        return newTopic;
    }


    public static IMarker createMarker(String markerName, String iconPath, IWorkbook workbook, IMarkerGroup group) throws IOException {
        IMarker marker = null;
        IMarkerSheet ims = workbook.getMarkerSheet();
        File iconFile = new File(iconPath);
        InputStream is = new FileInputStream(iconFile);
        if (is != null) {
            marker = ims.createMarker(iconFile.getName());
            marker.setName(markerName);
            IMarkerResource resource = marker.getResource();
            OutputStream os = resource.getOutputStream();
            org.xmind.core.util.FileUtils.transfer(is, os, false);
        }
        group.addMarker(marker);
        return marker;
    }

}
