package org.appling.rallyx.excel.columns;

import org.apache.poi.ss.usermodel.Cell;
import org.appling.rallyx.excel.ExcelWritingContext;

/**
 * Created by sappling on 7/29/2017.
 * Rank order using DragAndDropRank field
 */
public class RankColumn extends RedErrorColumnWriter {
    @Override
    public String getColumnHeader() {
        return "Rank";
    }

    @Override
    public void writeCell(Cell cell, ExcelWritingContext context) {
        cell.setCellValue(context.getRank());
        setStyle(cell, context);
    }
}
