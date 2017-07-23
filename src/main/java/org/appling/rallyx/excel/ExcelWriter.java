package org.appling.rallyx.excel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
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
    // add project
    String headers[] = {"ID", "Name", "Release", "Schedule State", "Initiative", "Children Count"};

    public ExcelWriter(StoryStats stats) {
        this.stats = stats;
    }

    public void write(String outName) throws IOException {
        Workbook wb = new XSSFWorkbook();
        Sheet s = wb.createSheet();

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

        writeRows(s, stats.getStoriesUnderInitiative(), initiativeName);

        writeRows(s, stats.getStoriesNotInInitiative(), "");

        column = 0;
        for (String header : headers) {
            s.autoSizeColumn(column++);
        }
        wb.write(new FileOutputStream(outName));
    }

    private void writeRows(Sheet s, Set<RallyNode> nodes, String initiativeName) {
        for (RallyNode node : nodes) {
            int column = 0;
            Row row = s.createRow(rowNum++);
            Cell cell = row.createCell(column++);
            cell.setCellValue(node.getFormattedId());

            cell = row.createCell(column++);
            cell.setCellValue(node.getName());

            cell = row.createCell(column++);
            cell.setCellValue(node.getRelease());

            cell = row.createCell(column++);
            cell.setCellValue(node.getScheduleState());

            cell = row.createCell(column++);
            cell.setCellValue(initiativeName);

            cell = row.createCell(column++);
            cell.setCellValue(node.getChildren().size());
        }
    }
}
