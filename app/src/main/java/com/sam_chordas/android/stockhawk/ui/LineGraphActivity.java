package com.sam_chordas.android.stockhawk.ui;

import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.db.chart.Tools;
import com.db.chart.model.LineSet;
import com.db.chart.view.LineChartView;
import com.db.chart.view.animation.Animation;
import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.data.StockValuesColumns;
import com.sam_chordas.android.stockhawk.rest.Utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by hussame_lemmawi on 14/09/16.
 */
public class LineGraphActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOG_TAG = LineGraphActivity.class.getSimpleName();

    private String mSymbol;

    private OkHttpClient client = new OkHttpClient()
            .newBuilder()
            .addNetworkInterceptor(new StethoInterceptor())
            .build();

    static final int CURRENT_YEAR = 0;
    static final int YEAR_LATER = -1;

    static final int LOADER_ID = 1;

    @BindView(R.id.currency_textview)
    TextView currencyTextView;
    @BindView(R.id.years_high_low_textview)
    TextView yearsHighLowTextView;
    @BindView(R.id.days_high_low_textview)
    TextView daysHighLowTextView;
    @BindView(R.id.epse_current_year_textview)
    TextView epseCurrentYearTextView;
    @BindView(R.id.epse_current_year_price_textview)
    TextView epseCurrentYearPriceTextView;
    @BindView(R.id.epse_next_year_textview)
    TextView epseNextYearTextView;
    @BindView(R.id.epse_next_year_price_textview)
    TextView epseNextYearPriceTextView;
    @BindView(R.id.epse_next_quarter_textview)
    TextView epseNextQuarterTextView;
    @BindView(R.id.change_from_year_high_low_textview)
    TextView changeYearHighLowTextView;
    @BindView(R.id.percent_change_from_year_high_low_textview)
    TextView percentChangeYearHighLowTextView;
    @BindView(R.id.fifty_day_moving_avg_textview)
    TextView fiftyDayMovingAvgTextView;
    @BindView(R.id.change_from_50_day_moving_avg_textview)
    TextView change50DayMovingAvgTextView;
    @BindView(R.id.percent_change_from_50_day_moving_avg_textview)
    TextView percentChange50DayMovingAvgTextView;
    @BindView(R.id.two_hundred_day_moving_avg_textview)
    TextView twoHundredDayMovingAvgTextView;
    @BindView(R.id.change_from_200_day_moving_avg_textview)
    TextView change200DayMovingAvgTextView;
    @BindView(R.id.percent_change_from_200_day_moving_avg_textview)
    TextView percentChange200DayMovingAvgTextView;


    @BindView(R.id.line_chart)
    LineChartView mLineChart;
    Animation anim;

    LineSet highSet, lowSet;
    float[] highs, lows;
    String[] xAxisLabels;
    String[] mCurrentMonths = new String[13];

    int axisColor, highDataColor, lowDataColor;
    float maxValue;

    String date;
    int counter1 = 0;
    int counter2 = 0;
    int size;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_graph);

        // Set the action bar title for the current selected stock
        mSymbol = getIntent().getStringExtra("symbol");
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(mSymbol + "'Stock Values");
        }

        // Check first if there is a fresh data for this stock.
        // Preventing unnecessary server queries.
        if (!freshDataStoredInDatabase()) {
            // If there is no fresh data, delete old data then query for fresh one.
            // Preventing overgrowth of DB
            deleteOldData();
            new FetchStockValuesTask().execute();
        }
        ButterKnife.bind(this);
        initializeChart();
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    void initializeChart() {
        if (Build.VERSION.SDK_INT > 23){
            axisColor = ContextCompat.getColor(this, R.color.material_blue_700);
            highDataColor = ContextCompat.getColor(this, R.color.material_green_A700);
            lowDataColor = ContextCompat.getColor(this, R.color.material_red_A700);
        }else {
            axisColor = getResources().getColor(R.color.material_blue_700);
            highDataColor = getResources().getColor(R.color.material_green_A700);
            lowDataColor = getResources().getColor(R.color.material_red_A700);
        }

        mLineChart.setBorderSpacing(2)
                .setBorderSpacing(Tools.fromDpToPx(5))
                .setTopSpacing(4)
                .setAxisThickness(3)
                .setAxisColor(axisColor)
                .setLabelsColor(axisColor)
                .setAxisLabelsSpacing(10);

        anim = new Animation();
        anim.setAlpha(150)
            .setDuration(800);
    }

    boolean freshDataStoredInDatabase() {
        Cursor cursor = this.getContentResolver().query(QuoteProvider.StockValues.CONTENT_URI,
                null,
                StockValuesColumns.SYMBOL + " = ?",
                new String[]{mSymbol},
                null);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            String lastStoredDate = cursor.getString(cursor.getColumnIndex(StockValuesColumns.CREATED));
            String[] parts = lastStoredDate.split("-");
            int storedYear = Integer.parseInt(parts[0]);
            int storedMonth = Integer.parseInt(parts[1]);
            int storedDay = Integer.parseInt(parts[2]);

            String rightNow = Utils.getDate(CURRENT_YEAR);
            parts = rightNow.split("-");
            int currentYear = Integer.parseInt(parts[0]);
            int currentMonth = Integer.parseInt(parts[1]);
            int currentDay = Integer.parseInt(parts[2]);

            // If it is the same days, Check months!
            if (storedDay == currentDay) {
                // If it is the same months, Check years!
                if (storedMonth == currentMonth) {
                    // If different years, update needed indeed.
                    if (storedYear == currentYear)
                        return true;
                }
            } else {
                return false;
            }

        }
        return false;
    }

    void deleteOldData() {
        getContentResolver().delete(QuoteProvider.StockValues.CONTENT_URI,
                StockValuesColumns.SYMBOL + " = ?",
                new String[]{mSymbol});
    }

    // Private class for fetching stock values over time (1 year from now).
    private class FetchStockValuesTask extends AsyncTask<Void, Void, Void> {

        String fetchData(String url) throws IOException {
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            Response response = client.newCall(request).execute();
            return response.body().string();
        }

        @Override
        protected Void doInBackground(Void... params) {
            StringBuilder urlStringBuilder = new StringBuilder();

            try {
                urlStringBuilder.append("https://query.yahooapis.com/v1/public/yql?q=");

                urlStringBuilder.append(URLEncoder.encode(" select * from yahoo.finance.historicaldata" +
                                " where symbol = \"" + mSymbol + "\""
                                + " and startDate = \"" + Utils.getDate(YEAR_LATER) + "\""
                                + " and endDate = \"" + Utils.getDate(CURRENT_YEAR) + "\"",
                        "UTF-8"));

                urlStringBuilder.append("&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables."
                        + "org%2Falltableswithkeys&callback=");

                String getResponse = fetchData(urlStringBuilder.toString());

                LineGraphActivity.this.getContentResolver().applyBatch(QuoteProvider.AUTHORITY,
                        Utils.stockValuesJsonToContentVals(getResponse));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (OperationApplicationException | RemoteException e) {
                Log.e(LOG_TAG, "Error applying batch insert", e);
            }
            return null;
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
                QuoteProvider.StockValues.CONTENT_URI,
                null,
                StockValuesColumns.SYMBOL + " = ?",
                new String[]{mSymbol},
                StockValuesColumns.DATE + " ASC");
    }

    // For easy coding rather than switching to many to the DB columns class.
    public static final int COL_DATE = 2;
    public static final int COL_HIGH = 4;
    public static final int COL_LOW = 5;

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null) {
            data.moveToFirst();
            if (data.getCount() > 0) {

                // Initializing this fields
                date = "";
                counter1 = 0;
                counter2 = 0;
                size = data.getCount() + 1;
                highs = new float[size];
                lows = new float[size];
                xAxisLabels = new String[size];
                // Max stock value will be one of the High values for sure.
                // Initialize Max with first high value
                maxValue = Float.parseFloat(data.getString(COL_HIGH));

                // Initialize xAxisLabels
                for (int k = 0; k < size; k++)
                    xAxisLabels[k] = "";

                do {
                    date = data.getString(COL_DATE);
                    highs[counter2] =
                            Float.parseFloat(data.getString(COL_HIGH));
                    lows[counter2] =
                            Float.parseFloat(data.getString(COL_LOW));
                    counter2++;

                    if (maxValue < highs[counter2])
                        maxValue = highs[counter2];

                    if (counter1 == 0) {
                        // Add current months (i.e from now to the same month last year)
                        // with no duplicate, duplicates exists as stock values assigned with days.
                        mCurrentMonths[counter1] = Utils.formateXLabels(date);
                        counter1++;
                    } else if (!mCurrentMonths[counter1 - 1].equals(Utils.formateXLabels(date))) {
                        // If month already stored, skip it
                        mCurrentMonths[counter1] = Utils.formateXLabels(date);
                        counter1++;
                    }
                } while (data.moveToNext());

                // just reuse counter1 rather than adding another object
                counter1 = 0;
                // Divide the stock values size by 7
                // which 7 = 12month/2everyTwomonths + 1
                // ex: sep16, jul16, may16, march16, jan16, nov15, sep15
                //     09-16, 07-16, 05-16, 03-16,   01-16, 11-15, 09-15
                //         9,    7,      5,     3,       1,    11,     9
                for (int j = 13; j < size; j += (size / 7)) {
                    xAxisLabels[j] = mCurrentMonths[counter1];
                    counter1+=2;
                    counter1 %= 13;
                }

                highSet = new LineSet(xAxisLabels, highs);
                highSet.setColor(highDataColor);
                highSet.setThickness(Tools.fromDpToPx(1));
                // Since last value cause a line drawing from the Y to X
                highSet.endAt(size-1);

                lowSet = new LineSet(xAxisLabels, lows);
                lowSet.setColor(lowDataColor);
                lowSet.setThickness(Tools.fromDpToPx(1));
                // Since last value cause a line drawing from the Y to X
                lowSet.endAt(size-1);

                mLineChart.addData(highSet);
                mLineChart.addData(lowSet);

                mLineChart.setStep((int) Utils.getProperSteps(maxValue));
                mLineChart.show(anim);

                // Emptying fields to be reused
                lows = null;
                lowSet = null;
                highs = null;
                highSet = null;
            }
        }
        // Load other data into detailed views
        loadExtraDetails();
    }

    // Easing coding rather than to many switching to DB columns class.
    public static final int COL_CURRENCY = 4;
    public static final int COL_ESPE_CURRENT_YEAR = 5;
    public static final int COL_ESPE_CURRENT_YEAR_PRICE = 6;
    public static final int COL_ESPE_NEXT_YEAR = 7;
    public static final int COL_ESPE_NEXT_YEAR_PRICE = 8;
    public static final int COL_ESPE_NEXT_QUARTER = 9;
    public static final int COL_DAYS_LOW = 10;
    public static final int COL_DAYS_HIGH = 11;
    public static final int COL_YEARS_LOW = 12;
    public static final int COL_YEARS_HIGH = 13;
    public static final int COL_CHANGE_FROM_YEAR_LOW = 14;
    public static final int COL_CHANGE_FROM_YEAR_HIGH = 15;
    public static final int COL_PERCENT_CHANGE_FROM_YEAR_LOW = 16;
    public static final int COL_PERCENT_CHANGE_FROM_YEAR_HIGH = 17;
    public static final int COL_FIFTY_DAY_MOVING_AVG = 18;
    public static final int COL_CHANGE_FROM_50_DAY_MOVING_AVG = 19;
    public static final int COL_PERCENT_CHANGE_FROM_50_DAY_MOVING_AVG = 20;
    public static final int COL_TWO_HUNDRED_DAY_MOVING_AVG = 21;
    public static final int COL_CHANGE_FROM_200_DAY_MOVING_AVG = 22;
    public static final int COL_PERCENT_CHANGE_FROM_200_DAY_MOVING_AVG = 23;

    void loadExtraDetails() {
        Cursor extraData = getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                null,
                QuoteColumns.SYMBOL + " =?",
                new String[]{mSymbol},
                null);

        if (extraData != null && extraData.getCount() > 0) {
            extraData.moveToFirst();
            currencyTextView.setText(extraData.getString(COL_CURRENCY));
            yearsHighLowTextView.setText(extraData.getString(COL_YEARS_HIGH)
                    + "/" + extraData.getString(COL_YEARS_LOW));
            daysHighLowTextView.setText(extraData.getString(COL_DAYS_HIGH)
                    + "/" + extraData.getString(COL_DAYS_LOW));
            epseCurrentYearTextView.setText(extraData.getString(COL_ESPE_CURRENT_YEAR));
            epseCurrentYearPriceTextView.setText(extraData.getString(COL_ESPE_CURRENT_YEAR_PRICE));
            epseNextQuarterTextView.setText(extraData.getString(COL_ESPE_NEXT_QUARTER));
            epseNextYearTextView.setText(extraData.getString(COL_ESPE_NEXT_YEAR));
            epseNextYearPriceTextView.setText(extraData.getString(COL_ESPE_NEXT_YEAR_PRICE));
            changeYearHighLowTextView.setText(extraData.getString(COL_CHANGE_FROM_YEAR_HIGH)
                    + "/" + extraData.getString(COL_CHANGE_FROM_YEAR_LOW));
            percentChangeYearHighLowTextView.setText(extraData.getString(COL_PERCENT_CHANGE_FROM_YEAR_HIGH)
                    + "/" + extraData.getString(COL_PERCENT_CHANGE_FROM_YEAR_LOW));
            fiftyDayMovingAvgTextView.setText(extraData.getString(COL_FIFTY_DAY_MOVING_AVG));
            change50DayMovingAvgTextView.setText(extraData.getString(COL_CHANGE_FROM_50_DAY_MOVING_AVG));
            percentChange50DayMovingAvgTextView.setText(extraData.getString(COL_PERCENT_CHANGE_FROM_50_DAY_MOVING_AVG));
            twoHundredDayMovingAvgTextView.setText(extraData.getString(COL_TWO_HUNDRED_DAY_MOVING_AVG));
            change200DayMovingAvgTextView.setText(extraData.getString(COL_CHANGE_FROM_200_DAY_MOVING_AVG));
            percentChange200DayMovingAvgTextView.setText(extraData.getString(COL_PERCENT_CHANGE_FROM_200_DAY_MOVING_AVG));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
