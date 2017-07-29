package org.appling.rallyx.excel.columns;

import org.apache.poi.ss.usermodel.Cell;
import org.appling.rallyx.excel.ExcelWritingContext;
import org.appling.rallyx.reports.Issue;

import java.util.List;

/**
 * Created by sappling on 7/29/2017.
 * Story Name
 */
public class IssueSeverityColumn extends RedErrorColumnWriter {
    @Override
    public String getColumnHeader() {
        return "Severity";
    }

    @Override
    public void writeCell(Cell cell, ExcelWritingContext context) {
        List<Issue> issues = context.getIssues();
        String issueName = "";
        if (!issues.isEmpty()) {
            issueName = issues.get(0).getSeverity().toString();
        }
        cell.setCellValue(issueName);
        setStyle(cell, context);
    }
}
