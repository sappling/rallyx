package org.appling.rallyx.excel;

import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.appling.rallyx.excel.columns.*;
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
    private static final String HDR_RANK = "Rank";
    private static final String HDR_ID = "ID";
    private static final String HDR_NAME = "Name";
    private static final String HDR_RELEASE = "Release";
    private static final String HDR_SCHEDS = "Schedule State";
    private static final String HDR_ITER = "Iteration";
    private static final String HDR_INIT = "Initiative";
    private static final String HDR_FEATURE = "Feature";
    private static final String HDR_CHILDC = "Children Count";
    private static final String HDR_PROJECT = "Project";
    private static final String HDR_TASKTOT = "Task Est Tot";
    private static final String HDR_DESCLEN = "Description Length";
    
    private StoryStats stats;
    private int rowNum = 0;
    CreationHelper createHelper;
    CellStyle hlink_style;
    int rank;

    // add project
    String headers[] = {HDR_RANK, HDR_ID, HDR_NAME, HDR_RELEASE, HDR_SCHEDS, HDR_ITER, HDR_INIT, HDR_FEATURE, HDR_CHILDC, HDR_PROJECT, HDR_TASKTOT, HDR_DESCLEN};
    ColumnWriter colunns[] = {
            new RankColumn(),
            new IdColumn(),
            new NameColumn(),
            new ReleaseColumn(),
            new ScheduleStateColumn(),
            new IterationColumn(),
            new InitiativeColumn(),
            new FeatureColumn(),
            new ChildCountColumn(),
            new ProjectColumn(),
            new TaskEstimateTotalColumn(),
            new DescriptionLengthColumn()
    };

    
    public ExcelWriter(StoryStats stats) {
        this.stats = stats;
        rank = 1;
    }

    public void write(String outName) throws IOException {
        Workbook wb = new XSSFWorkbook();
        Sheet s = wb.createSheet();
        createHelper = wb.getCreationHelper();

        initializeStyles(wb);

        RallyNode initiative = stats.getInitiative();
        String initiativeName = "";
        if (initiative != null) {
            initiativeName = initiative.getName();
        }

        Row headerRow = s.createRow(rowNum++);
        int columnNum = 0;
        for (ColumnWriter columnWriter : colunns) {
            Cell cell = headerRow.createCell(columnNum++);
            cell.setCellValue(columnWriter.getColumnHeader());
        }

        ArrayList<RallyNode> allStories = new ArrayList<>(stats.getAllStories());
        allStories.sort(new RankComparator());

        writeRows(s, allStories);


        columnNum = 0;
        for (String header : headers) {
            s.autoSizeColumn(columnNum++);
        }
        String lastColumn = CellReference.convertNumToColString(columnNum-1);
        s.setAutoFilter(CellRangeAddress.valueOf("A1:"+lastColumn+Integer.toString(rowNum-1)));
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
        ExcelWritingContext context = new ExcelWritingContext(s.getWorkbook());
        context.setLinkStyle(hlink_style);
        rank = 1;
        for (RallyNode node : nodes) {
            context.setRank(rank++);
            context.setNode(node);
            int column = 0;
            Row row = s.createRow(rowNum++);
            Cell cell;

            for (ColumnWriter columnWriter : colunns) {
                cell = row.createCell(column++);
                columnWriter.writeCell(cell, context);
            }
        }
    }
}
