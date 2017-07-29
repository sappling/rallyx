package org.appling.rallyx.excel.columns;

import org.apache.poi.ss.usermodel.Cell;
import org.appling.rallyx.excel.ExcelWritingContext;
import org.appling.rallyx.reports.Issue;

import java.util.List;

/**
 * Created by sappling on 7/29/2017.
 * Story Name
 */
public class IssueMessageColumn extends RedErrorColumnWriter {
    @Override
    public String getColumnHeader() {
        return "Message";
    }

    @Override
    public void writeCell(Cell cell, ExcelWritingContext context) {
        List<Issue> issues = context.getIssues();
        String message = "";
        if (!issues.isEmpty()) {
            message = issues.get(0).getMessage();
        }
        cell.setCellValue(message);
        setStyle(cell, context);
    }
}
