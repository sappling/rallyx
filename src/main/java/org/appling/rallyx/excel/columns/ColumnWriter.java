package org.appling.rallyx.excel.columns;

import org.apache.poi.ss.usermodel.Cell;
import org.appling.rallyx.excel.ExcelWritingContext;

/**
 * Created by sappling on 7/29/2017.
 */
public interface ColumnWriter {
    String getColumnHeader();
    void writeCell(Cell cell, ExcelWritingContext context);
}