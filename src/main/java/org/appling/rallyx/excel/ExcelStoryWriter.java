package org.appling.rallyx.excel;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.appling.rallyx.excel.columns.*;
import org.appling.rallyx.rally.RallyNode;
import org.appling.rallyx.rally.RallyNodeRankComparator;
import org.appling.rallyx.rally.StoryStats;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by sappling on 7/23/2017.
 */
public class ExcelStoryWriter {

    protected StoryStats stats;
    protected int rowNum = 0;
    protected CellStyle hlink_style;
    protected CellStyle error_style;

    private ColumnWriter colunns[] = {
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

    
    public ExcelStoryWriter(StoryStats stats) {
        this.stats = stats;
    }

    public void write(String outName) throws IOException {
        Workbook wb = new XSSFWorkbook();
        Sheet s = wb.createSheet();

        initializeStyles(wb);


        Row headerRow = s.createRow(rowNum++);
        int columnNum = 0;
        for (ColumnWriter columnWriter : getColunns()) {
            Cell cell = headerRow.createCell(columnNum++);
            cell.setCellValue(columnWriter.getColumnHeader());
        }

        ArrayList<RallyNode> allStories = new ArrayList<>(stats.getAllStories());
        allStories.sort(new RallyNodeRankComparator());

        writeRows(s, allStories);


        columnNum = 0;
        for (ColumnWriter columnWriter : getColunns()) {
            s.autoSizeColumn(columnNum++);
        }
        String lastColumn = CellReference.convertNumToColString(columnNum-1);
        s.setAutoFilter(CellRangeAddress.valueOf("A1:"+lastColumn+Integer.toString(rowNum-1)));
        FileOutputStream outStream = new FileOutputStream(ensureExtention(outName, "Report.xlsx"));
        wb.write(outStream);
        outStream.close();
    }

    protected ColumnWriter[] getColunns() {
        return colunns;
    }

    protected void initializeStyles(Workbook wb) {
        hlink_style = wb.createCellStyle();
        Font hlink_font = wb.createFont();
        hlink_font.setUnderline(Font.U_SINGLE);
        hlink_font.setColor(IndexedColors.BLUE.getIndex());
        hlink_style.setFont(hlink_font);

        error_style = wb.createCellStyle();
        Font errorFont = wb.createFont();
        errorFont.setColor(IndexedColors.RED.getIndex());
        error_style.setFont(errorFont);
    }

    private void writeRows(Sheet s, List<RallyNode> nodes) {
        ExcelWritingContext context = new ExcelWritingContext(s.getWorkbook());
        context.setLinkStyle(hlink_style);
        context.setErrorStyle(error_style);

        int rank = 1;
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

    protected String ensureExtention(String outName, String defaultName) {
        String result = outName;
        if (outName == null || outName.length()==0) {
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd-");
            result = fmt.format(new Date())+defaultName;
        } else if (!outName.endsWith(".xlsx")) {
            result += ".xlsx";
        }
        return result;
    }
}
