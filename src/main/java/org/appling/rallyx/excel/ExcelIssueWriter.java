package org.appling.rallyx.excel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.appling.rallyx.excel.columns.*;
import org.appling.rallyx.rally.RallyNode;
import org.appling.rallyx.rally.RallyNodeRankComparator;
import org.appling.rallyx.rally.StoryStats;
import org.appling.rallyx.reports.Issue;
import org.appling.rallyx.reports.IssueChecker;
import org.appling.rallyx.reports.IssueRankComparator;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * Created by sappling on 7/29/2017.
 */
public class ExcelIssueWriter extends ExcelStoryWriter {

    //todo - appropriate columns for issues
    private ColumnWriter colunns[] = {
            new RankColumn(),
            new IdColumn(),
            new NameColumn(),
            new ProjectColumn(),
            new IssueSeverityColumn(),
            new IssueMessageColumn(),
            new ScheduleStateColumn(),
            new IterationColumn(),
            new FeatureColumn(),
    };


    public ExcelIssueWriter(StoryStats stats) {
        super(stats);
    }

    @Override
    public void write(String outName) throws IOException {
        IssueChecker checker = new IssueChecker(stats);
        List<Issue> issues = checker.doChecks();

        Workbook wb = new XSSFWorkbook();
        Sheet s = wb.createSheet();

        initializeStyles(wb);


        Row headerRow = s.createRow(rowNum++);
        int columnNum = 0;
        for (ColumnWriter columnWriter : getColunns()) {
            Cell cell = headerRow.createCell(columnNum++);
            cell.setCellValue(columnWriter.getColumnHeader());
        }

        //ArrayList<RallyNode> allStories = new ArrayList<>(stats.getAllStories());
        issues.sort(new IssueRankComparator());

        writeRows(s, issues);


        columnNum = 0;
        for (ColumnWriter columnWriter : getColunns()) {
            s.autoSizeColumn(columnNum++);
        }
        String lastColumn = CellReference.convertNumToColString(columnNum-1);
        s.setAutoFilter(CellRangeAddress.valueOf("A1:"+lastColumn+Integer.toString(rowNum-1)));
        FileOutputStream outStream = new FileOutputStream(ensureExtention(outName, "Check.xlsx"));
        wb.write(outStream);
        outStream.close();
    }

    private void writeRows(Sheet s, List<Issue> issues) {
        ExcelWritingContext context = new ExcelWritingContext(s.getWorkbook());
        context.setLinkStyle(hlink_style);
        context.setErrorStyle(error_style);
        int rank = 1;
        for (Issue issue : issues) {
            context.setRank(rank++);
            context.setNode(issue.getStory());
            context.setSingleIssue(issue);
            int column = 0;
            Row row = s.createRow(rowNum++);
            Cell cell;

            for (ColumnWriter columnWriter : colunns) {
                cell = row.createCell(column++);
                columnWriter.writeCell(cell, context);
            }
        }
    }


    @Override
    protected ColumnWriter[] getColunns() {
        return colunns;
    }
}
