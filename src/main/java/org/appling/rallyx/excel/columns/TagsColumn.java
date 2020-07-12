package org.appling.rallyx.excel.columns;

import org.apache.poi.ss.usermodel.Cell;
import org.appling.rallyx.excel.BadContextException;
import org.appling.rallyx.excel.ExcelWritingContext;
import org.appling.rallyx.rally.RallyNode;

public class TagsColumn extends RedErrorColumnWriter {
    @Override
    public String getColumnHeader() {
        return "Tags";
    }

    @Override
    public void writeCell(Cell cell, ExcelWritingContext context) {
        RallyNode node = context.getNode();
        if (node == null) {
            throw new BadContextException("Missing RallyNode");
        }
        cell.setCellValue(node.getAllTagsAsString());
        setStyle(cell, context);
    }
}
