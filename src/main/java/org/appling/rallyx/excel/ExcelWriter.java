package org.appling.rallyx.excel;

import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.appling.rallyx.rally.RallyNode;
import org.appling.rallyx.rally.StoryStats;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Set;

/**
 * Created by sappling on 7/23/2017.
 */
public class ExcelWriter {
    private StoryStats stats;
    private int rowNum = 0;
    CellStyle hlink_style;

    // add project
    String headers[] = {"ID", "Name", "Release", "Schedule State", "Initiative", "Feature", "Children Count"};

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

        writeRows(s, stats.getStoriesUnderInitiative());

        writeRows(s, stats.getStoriesNotInInitiative());

        column = 0;
        for (String header : headers) {
            s.autoSizeColumn(column++);
        }
        s.setAutoFilter(CellRangeAddress.valueOf("A1:F"+Integer.toString(rowNum-1)));
        wb.write(new FileOutputStream(outName));
    }

    private void initializeStyles(Workbook wb) {
        hlink_style = wb.createCellStyle();
        Font hlink_font = wb.createFont();
        hlink_font.setUnderline(Font.U_SINGLE);
        hlink_font.setColor(IndexedColors.BLUE.getIndex());
        hlink_style.setFont(hlink_font);
    }

    private void writeRows(Sheet s, Set<RallyNode> nodes) {
        CreationHelper createHelper = s.getWorkbook().getCreationHelper();
        for (RallyNode node : nodes) {
            int column = 0;
            Row row = s.createRow(rowNum++);
            Cell cell = row.createCell(column++);
            cell.setCellValue(node.getFormattedId());
            Hyperlink link = createHelper.createHyperlink(HyperlinkType.URL);
            link.setAddress(node.getURL());
            cell.setHyperlink(link);
            cell.setCellStyle(hlink_style);

            cell = row.createCell(column++);
            cell.setCellValue(node.getName());

            cell = row.createCell(column++);
            cell.setCellValue(node.getRelease());

            cell = row.createCell(column++);
            cell.setCellValue(node.getScheduleState());

            cell = row.createCell(column++);
            RallyNode initiative = node.getInitiative();
            cell.setCellValue(initiative != null ? initiative.toString() : "");

            cell = row.createCell(column++);
            RallyNode feature = node.getFeature();
            cell.setCellValue(feature!=null ? feature.toString() : "");

            cell = row.createCell(column++);
            cell.setCellValue(node.getChildren().size());
        }
    }
}
