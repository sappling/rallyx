package org.appling.rallyx.excel.columns;

import org.apache.poi.ss.usermodel.Cell;
import org.appling.rallyx.excel.ExcelWritingContext;

/**
 * Created by sappling on 7/29/2017.
 */
public abstract class RedErrorColumnWriter implements ColumnWriter{
    protected void setStyle(Cell cell, ExcelWritingContext context){
        if (context.hasError()) {
            cell.setCellStyle(context.getErrorStyle());
        }
    }
}
