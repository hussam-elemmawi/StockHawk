package com.sam_chordas.android.stockhawk.data;

import net.simonvt.schematic.annotation.Database;
import net.simonvt.schematic.annotation.Table;

/**
 * Created by sam_chordas on 10/5/15.
 * Edited by hussam_elemmawi on 13/9/16
 */
@Database(version = QuoteDatabase.VERSION)
public class QuoteDatabase {
    private QuoteDatabase() {
    }

    public static final int VERSION = 2;

    @Table(QuoteColumns.class)
    public static final String QUOTES = "quotes";
    @Table(StockValuesColumns.class)
    public static final String STOCK_VALUES = "stock_values";
}
