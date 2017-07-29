package org.appling.rallyx.excel;

/**
 * Created by sappling on 7/29/2017.
 * Only should happen in the event of programmer error, but if so, this is better than an NPE
 */
public class BadContextException extends RuntimeException {
    public BadContextException() {
        super();
    }

    public BadContextException(String msg) {
        super("Problem with ExcelWritingContext:"+msg);
    }
}
