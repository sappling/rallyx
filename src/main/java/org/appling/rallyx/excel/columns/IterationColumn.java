package org.appling.rallyx.excel.columns;

import org.apache.poi.ss.usermodel.Cell;
import org.appling.rallyx.excel.BadContextException;
import org.appling.rallyx.excel.ExcelWritingContext;
import org.appling.rallyx.rally.RallyNode;

/**
 * Created by sappling on 7/29/2017.
 */
public class IterationColumn extends RedErrorColumnWriter {
    @Override
    public String getColumnHeader() {
        return "Iteration";
    }

    @Override
    public void writeCell(Cell cell, ExcelWritingContext context) {
        RallyNode node = context.getNode();
        if (node == null) {
            throw new BadContextException("Missing RallyNode");
        }
        cell.setCellValue(node.getIterationName());
        setStyle(cell, context);
    }
}
