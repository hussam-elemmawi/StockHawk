package com.sam_chordas.android.stockhawk.data;

import net.simonvt.schematic.annotation.AutoIncrement;
import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.NotNull;
import net.simonvt.schematic.annotation.PrimaryKey;

/**
 * Created by hussamelemmawi on 16/09/16.
 */
public class StockValuesColumns {
    @DataType(DataType.Type.INTEGER) @PrimaryKey
    @AutoIncrement
    public static final String _ID = "_id";
    @DataType(DataType.Type.TEXT) @NotNull
    public static final String SYMBOL = "symbol";
    @DataType(DataType.Type.TEXT) @NotNull
    public static final String DATE = "date";

    @DataType(DataType.Type.TEXT)
    public static final String CREATED = "created";

    @DataType(DataType.Type.TEXT) @NotNull
    public static final String HIGH = "high";
    @DataType(DataType.Type.TEXT) @NotNull
    public static final String LOW = "low";

    // Extra columns for extra data if I want to add in future
//    @DataType(DataType.Type.TEXT) @NotNull
//    public static final String OPEN = "open";
//    @DataType(DataType.Type.TEXT) @NotNull
//    public static final String CLOSE = "close";
//    @DataType(DataType.Type.TEXT) @NotNull
//    public static final String VOLUME = "volume";
//    @DataType(DataType.Type.TEXT) @NotNull
//    public static final String ADG_CLOSE = "adj_close";
}
