package org.appling.rallyx;

import org.xmind.core.*;
import org.xmind.core.marker.IMarker;
import org.xmind.core.marker.IMarkerGroup;
import org.xmind.core.marker.IMarkerResource;
import org.xmind.core.marker.IMarkerSheet;

import java.io.*;
import java.util.List;

/**
 * Created by sappling on 7/18/2017.
 */
public class SimpleTestMain {
    public static void main(String args[]) {

        if (args.length < 1) {
            System.err.println("missing argument for file name");
            System.exit(-1);
        }

        IWorkbookBuilder builder = Core.getWorkbookBuilder();
        IWorkbook wb = builder.createWorkbook(args[0]);
        ISheet sh = wb.getPrimarySheet();

        ITopic rootTopic = sh.getRootTopic();
        rootTopic.setTitleText("This is a test");

        ITopic one = wb.createTopic();
        one.setTitleText("one");
        rootTopic.add(one, ITopic.ATTACHED);

        ITopic two = wb.createTopic();
        two.setTitleText("two");
        one.add(two, ITopic.ATTACHED);
        IMarkerSheet markerSheet = wb.getMarkerSheet();

        ITopic floating = wb.createTopic();
        floating.setTitleText("floater");
        floating.setPosition(100, 100);
        rootTopic.add(floating, -1, ITopic.DETACHED);


        try {
            markerSheet.importFrom("markers");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CoreException e) {
            e.printStackTrace();
        }


        /*
        IMarker greenCheck = null;
        List<IMarkerGroup> groups = markerSheet.getMarkerGroups();
        for (IMarkerGroup group : groups) {
            String groupName = group.getName();
            List<IMarker> markers = group.getMarkers();
            for (IMarker marker : markers) {
                System.out.println(group.getName()+":"+marker.getName());
                greenCheck = marker;
            }
        }
        */
        IMarkerGroup markerGroup = markerSheet.createMarkerGroup(true);
        markerGroup.setName("test");
        try {
            IMarker greenCheck = createMarker("greencheck", "markers/GreenCheck.png", wb, markerGroup);

            two.addMarker(greenCheck.getId());
            wb.save(args[0]);
        }
        catch (IOException e) {
            e.printStackTrace();
        } catch (CoreException e) {
            e.printStackTrace();
        }
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
