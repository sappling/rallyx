package org.appling.rallyx.excel.columns;

import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.appling.rallyx.excel.BadContextException;
import org.appling.rallyx.excel.ExcelWritingContext;
import org.appling.rallyx.rally.RallyNode;

/**
 * Created by sappling on 7/29/2017.
 * Shows the Formatted ID as a hyperlink to the story
 */
public class IdColumn implements ColumnWriter {
    @Override
    public String getColumnHeader() {
        return "ID";
    }

    @Override
    public void writeCell(Cell cell, ExcelWritingContext context) {
        RallyNode node = context.getNode();
        if (node == null) {
            throw new BadContextException("Missing RallyNode");
        }
        cell.setCellValue(node.getFormattedId());
        Hyperlink link = context.createHyperlink();
        link.setAddress(node.getURL());
        cell.setHyperlink(link);
        cell.setCellStyle(context.getLinkStyle());
    }
}
