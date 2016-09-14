package com.sam_chordas.android.stockhawk.data;

import net.simonvt.schematic.annotation.AutoIncrement;
import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.DefaultValue;
import net.simonvt.schematic.annotation.NotNull;
import net.simonvt.schematic.annotation.PrimaryKey;

/**
 * Created by sam_chordas on 10/5/15.
 * Edited by hussam_elemmawi on 13/9/16
 */
public class QuoteColumns {
  @DataType(DataType.Type.INTEGER) @PrimaryKey @AutoIncrement
  public static final String _ID = "_id";
  @DataType(DataType.Type.TEXT) @NotNull
  public static final String SYMBOL = "symbol";

  @DataType(DataType.Type.TEXT) @NotNull
  public static final String PERCENT_CHANGE = "percent_change";
  @DataType(DataType.Type.TEXT) @NotNull
  public static final String CHANGE = "change";
/*
  @DataType(DataType.Type.TEXT) @NotNull
  public static final String CURRENCY = "currency";

  @DataType(DataType.Type.TEXT) @NotNull
  public static final String EPSE_CURRENT_YEAR = "epse_current_year";
  @DataType(DataType.Type.TEXT) @NotNull
  public static final String EPSE_CURRENT_YEAR_PRICE = "epse_current_year_price";
  @DataType(DataType.Type.TEXT) @NotNull
  public static final String EPSE_NEXT_YEAR = "epse_next_year";
  @DataType(DataType.Type.TEXT) @NotNull
  public static final String EPSE_NEXT_YEAR_PRICE = "epse_next_year_price";
  @DataType(DataType.Type.TEXT) @NotNull
  public static final String EPSE_NEXT_QUARTER = "epse_next_quarter";

  @DataType(DataType.Type.TEXT) @NotNull
  public static final String DAYS_LOW = "days_low";
  @DataType(DataType.Type.TEXT) @NotNull
  public static final String DAYS_HIGH = "days_high";
  @DataType(DataType.Type.TEXT) @NotNull
  public static final String YEARS_LOW = "years_low";
  @DataType(DataType.Type.TEXT) @NotNull
  public static final String YEARS_HIGH = "years_high";

  @DataType(DataType.Type.TEXT) @NotNull
  public static final String CHANGE_FROM_YEAR_LOW = "change_from_year_low";
  @DataType(DataType.Type.TEXT) @NotNull
  public static final String CHANGE_FROM_YEAR_HIGH = "change_from_year_high";
  @DataType(DataType.Type.TEXT) @NotNull
  public static final String PERCENT_CHANGE_FROM_YEAR_LOW = "percent_change_from_year_low";
  @DataType(DataType.Type.TEXT) @NotNull
  public static final String PERCENT_CHANGE_FROM_YEAR_HIGH = "percent_change_from_year_high";

  @DataType(DataType.Type.TEXT) @NotNull
  public static final String FIFTY_DAY_MOVING_AVG = "fifty_day_moving_avg";
  @DataType(DataType.Type.TEXT) @NotNull
  public static final String TWO_HUNDRED_DAY_MOVING_AVG = "two_hundred_day_moving_avg";
  @DataType(DataType.Type.TEXT) @NotNull
  public static final String CHANGE_FROM_50_DAY_MOVING_AVG = "change_from_50_day_moving_avg";
  @DataType(DataType.Type.TEXT) @NotNull
  public static final String CHANGE_FROM_200_DAY_MOVING_AVG = "change_from_200_day_moving_avg";
  @DataType(DataType.Type.TEXT) @NotNull
  public static final String PERCENT_CHANGE_FROM_50_DAY_MOVING_AVG =
          "percent_change_from_50_day_moving_avg";
  @DataType(DataType.Type.TEXT) @NotNull
  public static final String PERCENT_CHANGE_FROM_200_DAY_MOVING_AVG =
          "percent_change_from_200_day_moving_avg";
*/

  @DataType(DataType.Type.TEXT) @NotNull
  public static final String BIDPRICE = "bid_price";
  @DataType(DataType.Type.TEXT)
  public static final String CREATED = "created";
  @DataType (DataType.Type.INTEGER) @NotNull
  public static final String ISUP = "is_up";
  @DataType(DataType.Type.INTEGER) @NotNull
  public static final String ISCURRENT = "is_current";
}
