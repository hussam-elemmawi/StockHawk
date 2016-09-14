package com.sam_chordas.android.stockhawk.rest;

import android.content.ContentProviderOperation;
import android.util.Log;
import android.widget.Toast;

import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

import net.simonvt.schematic.annotation.NotNull;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by sam_chordas on 10/8/15.
 * Edited by hussam_elemmawi on 13/9/16
 */
public class Utils {

    private static String LOG_TAG = Utils.class.getSimpleName();

    public static boolean showPercent = true;

    public static ArrayList quoteJsonToContentVals(String JSON) {
        ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();
        JSONObject jsonObject = null;
        JSONArray resultsArray = null;
        try {
            jsonObject = new JSONObject(JSON);
            if (jsonObject != null && jsonObject.length() != 0) {
                jsonObject = jsonObject.getJSONObject("query");
                int count = Integer.parseInt(jsonObject.getString("count"));
                if (count == 1) {
                    jsonObject = jsonObject.getJSONObject("results")
                            .getJSONObject("quote");
                    batchOperations.add(buildBatchOperation(jsonObject));
                } else {
                    resultsArray = jsonObject.getJSONObject("results").getJSONArray("quote");

                    if (resultsArray != null && resultsArray.length() != 0) {
                        for (int i = 0; i < resultsArray.length(); i++) {
                            jsonObject = resultsArray.getJSONObject(i);
                            batchOperations.add(buildBatchOperation(jsonObject));
                        }
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "String to JSON failed: " + e);
        }
        return batchOperations;
    }

    public static String truncateBidPrice(String bidPrice) {
        try {
            bidPrice = String.format("%.2f", Float.parseFloat(bidPrice));
            return bidPrice;
        } catch (NumberFormatException ne) {
            ne.printStackTrace();
        }
        return "empty";
    }

    public static String truncateChange(String change, boolean isPercentChange) {
        String weight = change.substring(0, 1);
        String ampersand = "";
        if (isPercentChange) {
            ampersand = change.substring(change.length() - 1, change.length());
            change = change.substring(0, change.length() - 1);
        }
        change = change.substring(1, change.length());
        try {
            double round = (double) Math.round(Double.parseDouble(change) * 100) / 100;
            change = String.format("%.2f", round);
            StringBuffer changeBuffer = new StringBuffer(change);
            changeBuffer.insert(0, weight);
            changeBuffer.append(ampersand);
            change = changeBuffer.toString();
            return change;
        } catch (NumberFormatException ne) {
            ne.printStackTrace();
        }
        return "";
    }

    public static ContentProviderOperation buildBatchOperation(JSONObject jsonObject) {
        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
                QuoteProvider.Quotes.CONTENT_URI);
        try {
            String change = jsonObject.getString("Change");
            builder.withValue(QuoteColumns.SYMBOL, jsonObject.getString("symbol"));
            builder.withValue(QuoteColumns.BIDPRICE, truncateBidPrice(jsonObject.getString("Bid")));
            builder.withValue(QuoteColumns.PERCENT_CHANGE, truncateChange(
                    jsonObject.getString("ChangeinPercent"), true));
            builder.withValue(QuoteColumns.CHANGE, truncateChange(change, false));
   /*         builder.withValue(QuoteColumns.CURRENCY, jsonObject.getString("Currency"));

            builder.withValue(QuoteColumns.EPSE_CURRENT_YEAR, jsonObject.getString("EPSEstimateCurrentYear"));
            builder.withValue(QuoteColumns.EPSE_CURRENT_YEAR_PRICE,
                    jsonObject.getString("PriceEPSEstimateCurrentYear"));
            builder.withValue(QuoteColumns.EPSE_NEXT_YEAR, jsonObject.getString("EPSEstimateNextYear"));
            builder.withValue(QuoteColumns.EPSE_NEXT_YEAR_PRICE,
                    jsonObject.getString("PriceEPSEstimateNextYear"));
            builder.withValue(QuoteColumns.EPSE_NEXT_QUARTER,
                    jsonObject.getString("EPSEstimateNextQuarter"));

            builder.withValue(QuoteColumns.DAYS_LOW, jsonObject.getString("DaysLow"));
            builder.withValue(QuoteColumns.DAYS_HIGH, jsonObject.getString("DaysHigh"));
            builder.withValue(QuoteColumns.YEARS_LOW, jsonObject.getString("YearLow"));
            builder.withValue(QuoteColumns.YEARS_HIGH, jsonObject.getString("YearHigh"));

            builder.withValue(QuoteColumns.CHANGE_FROM_YEAR_LOW,
                    jsonObject.getString("ChangeFromYearLow"));
            builder.withValue(QuoteColumns.PERCENT_CHANGE_FROM_YEAR_LOW,
                    jsonObject.getString("PercentChangeFromYearLow"));
            builder.withValue(QuoteColumns.CHANGE_FROM_YEAR_HIGH,
                    jsonObject.getString("ChangeFromYearHigh"));
            builder.withValue(QuoteColumns.PERCENT_CHANGE_FROM_YEAR_HIGH,
                    jsonObject.getString("PercebtChangeFromYearHigh"));

            builder.withValue(QuoteColumns.FIFTY_DAY_MOVING_AVG,
                    jsonObject.getString("FiftydayMovingAverage"));
            builder.withValue(QuoteColumns.TWO_HUNDRED_DAY_MOVING_AVG,
                    jsonObject.getString("TwoHundreddayMovingAverage"));
            builder.withValue(QuoteColumns.CHANGE_FROM_50_DAY_MOVING_AVG,
                    jsonObject.getString("ChangeFromFiftydayMovingAverage"));
            builder.withValue(QuoteColumns.CHANGE_FROM_200_DAY_MOVING_AVG,
                    jsonObject.getString("ChangeFromTwoHundreddayMovingAverage"));
            builder.withValue(QuoteColumns.PERCENT_CHANGE_FROM_50_DAY_MOVING_AVG,
                    jsonObject.getString("PercentChangeFromFiftydayMovingAverage"));
            builder.withValue(QuoteColumns.PERCENT_CHANGE_FROM_200_DAY_MOVING_AVG,
                    jsonObject.getString("PercentChangeFromTwoHundreddayMovingAverage"));
*/
            builder.withValue(QuoteColumns.ISCURRENT, 1);
            if (change.charAt(0) == '-') {
                builder.withValue(QuoteColumns.ISUP, 0);
            } else if (change.charAt(0) == 'n') {
                builder.withValue(QuoteColumns.ISUP, -1);
            } else {
                builder.withValue(QuoteColumns.ISUP, 1);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return builder.build();
    }
}
