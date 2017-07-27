package org.appling.rallyx.excel;

import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.appling.rallyx.rally.RallyNode;
import org.appling.rallyx.rally.RankComparator;
import org.appling.rallyx.rally.StoryStats;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sappling on 7/23/2017.
 */
public class ExcelWriter {
    private StoryStats stats;
    private int rowNum = 0;
    CellStyle hlink_style;

    // add project
    String headers[] = {"Rank", "ID", "Name", "Release", "Schedule State", "Iteration", "Initiative", "Feature", "Children Count", "Project", "Task Est Tot", "Description Length"};

    public ExcelWriter(StoryStats stats) {
        this.stats = stats;
    }

    public void write(String outName) throws IOException {
        Workbook wb = new XSSFWorkbook();
        Sheet s = wb.createSheet();

        initializeStyles(wb);

        RallyNode initiative = stats.getInitiative();
        String initiativeName = "";
        if (initiative != null) {
            initiativeName = initiative.getName();
        }

        Row headerRow = s.createRow(rowNum++);
        int column = 0;
        for (String header : headers) {
            Cell cell = headerRow.createCell(column++);
            cell.setCellValue(header);
        }

        ArrayList<RallyNode> allStories = new ArrayList<>(stats.getAllStories());
        allStories.sort(new RankComparator());

        writeRows(s, allStories);


        column = 0;
        for (String header : headers) {
            s.autoSizeColumn(column++);
        }
        s.setAutoFilter(CellRangeAddress.valueOf("A1:L"+Integer.toString(rowNum-1)));
        wb.write(new FileOutputStream(outName));
    }

    private void initializeStyles(Workbook wb) {
        hlink_style = wb.createCellStyle();
        Font hlink_font = wb.createFont();
        hlink_font.setUnderline(Font.U_SINGLE);
        hlink_font.setColor(IndexedColors.BLUE.getIndex());
        hlink_style.setFont(hlink_font);
    }

    private void writeRows(Sheet s, List<RallyNode> nodes) {
        int rank = 1;
        CreationHelper createHelper = s.getWorkbook().getCreationHelper();
        for (RallyNode node : nodes) {
            int column = 0;
            Row row = s.createRow(rowNum++);
            Cell cell;

            // Rank
            cell = row.createCell(column++);
            cell.setCellValue(rank++);

            // ID
            cell = row.createCell(column++);
            cell.setCellValue(node.getFormattedId());
            Hyperlink link = createHelper.createHyperlink(HyperlinkType.URL);
            link.setAddress(node.getURL());
            cell.setHyperlink(link);
            cell.setCellStyle(hlink_style);

            // Name
            cell = row.createCell(column++);
            cell.setCellValue(node.getName());

            // Release
            cell = row.createCell(column++);
            cell.setCellValue(node.getRelease());

            // Schedule State
            cell = row.createCell(column++);
            cell.setCellValue(node.getScheduleStateName());

            // Iteration
            cell = row.createCell(column++);
            cell.setCellValue(node.getIterationName());

            // Initiative
            cell = row.createCell(column++);
            RallyNode initiative = node.getInitiative();
            cell.setCellValue(initiative != null ? initiative.toString() : "");

            // Feature
            cell = row.createCell(column++);
            RallyNode feature = node.getFeature();
            cell.setCellValue(feature!=null ? feature.toString() : "");

            // Children Count
            cell = row.createCell(column++);
            cell.setCellValue(node.getChildren().size());

            // Project
            cell = row.createCell(column++);
            cell.setCellValue(node.getProjectName());

            // Task Estimate Total
            cell = row.createCell(column++);
            cell.setCellValue(node.getTaskEstimateTotal());

            // Description Length
            cell = row.createCell(column++);
            cell.setCellValue(node.getDescription().length());
        }
    }
}
