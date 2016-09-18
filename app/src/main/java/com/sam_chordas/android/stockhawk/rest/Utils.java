package com.sam_chordas.android.stockhawk.rest;

import android.content.ContentProviderOperation;
import android.util.FloatProperty;
import android.util.Log;

import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.data.StockValuesColumns;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Stack;

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

    final static String[] MONTHS = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep",
                                        "Oct", "Nov", "Dec"};
    public static final int NOW = 0;

    public static String formateDateForLegend(String date){
        StringBuilder ret = new StringBuilder();
        String[] parts = date.split("-");
        ret.append(parts[2] + " ");
        ret.append(MONTHS[Integer.parseInt(parts[1]) -1] + " ");
        ret.append(parts[0].substring(2,4));
        return ret.toString();
    }

    public static String getDate(int afterYears){
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        calendar.add(Calendar.YEAR, afterYears);
        return df.format(calendar.getTime());
    }

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
                    batchOperations.add(buildBatchOperationForQuotes(jsonObject));
                } else {
                    resultsArray = jsonObject.getJSONObject("results").getJSONArray("quote");

                    if (resultsArray != null && resultsArray.length() != 0) {
                        for (int i = 0; i < resultsArray.length(); i++) {
                            jsonObject = resultsArray.getJSONObject(i);
                            batchOperations.add(buildBatchOperationForQuotes(jsonObject));
                        }
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "String to JSON failed: " + e);
        }
        return batchOperations;
    }

    public static ArrayList stockValuesJsonToContentVals(String JSON){
        ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();
        JSONObject jsonObject = null;
        JSONArray resultArray = null;
        try{
            jsonObject = new JSONObject(JSON);
            if(jsonObject != null && jsonObject.length() != 0){
                jsonObject = jsonObject.getJSONObject("query");
                jsonObject = jsonObject.getJSONObject("results");
                resultArray = jsonObject.getJSONArray("quote");
                int resultLenght = resultArray.length();
                for(int i=0; i<resultLenght; i++){
                    jsonObject = resultArray.getJSONObject(i);
                    batchOperations.add(buildBatchOperationsForStockVals(jsonObject));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
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
        return "null";
    }

    public static ContentProviderOperation buildBatchOperationForQuotes(JSONObject jsonObject) {
        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
                QuoteProvider.Quotes.CONTENT_URI);
        try {
            String change = jsonObject.getString("Change");
            builder.withValue(QuoteColumns.SYMBOL, jsonObject.getString("symbol"));
            builder.withValue(QuoteColumns.BIDPRICE, truncateBidPrice(jsonObject.getString("Bid")));
            builder.withValue(QuoteColumns.PERCENT_CHANGE, truncateChange(
                    jsonObject.getString("ChangeinPercent"), true));
            builder.withValue(QuoteColumns.CHANGE, truncateChange(change, false));
            builder.withValue(QuoteColumns.CURRENCY, jsonObject.getString("Currency"));

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

    public static ContentProviderOperation buildBatchOperationsForStockVals(JSONObject jsonObject){
        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
                QuoteProvider.StockValues.CONTENT_URI);
        try{
            builder.withValue(StockValuesColumns.SYMBOL, jsonObject.getString("Symbol"));
            builder.withValue(StockValuesColumns.DATE, jsonObject.getString("Date"));
            builder.withValue(StockValuesColumns.CREATED, Utils.getDate(NOW));
            builder.withValue(StockValuesColumns.OPEN, jsonObject.getString("Open"));
            builder.withValue(StockValuesColumns.HIGH, jsonObject.getString("High"));
            builder.withValue(StockValuesColumns.LOW, jsonObject.getString("Low"));
            builder.withValue(StockValuesColumns.CLOSE, jsonObject.getString("Close"));
            builder.withValue(StockValuesColumns.VOLUME, jsonObject.getString("Volume"));
            builder.withValue(StockValuesColumns.ADG_CLOSE, jsonObject.getString("Adj_Close"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return builder.build();
    }
}