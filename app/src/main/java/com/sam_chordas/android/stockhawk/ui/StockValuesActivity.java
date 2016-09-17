package com.sam_chordas.android.stockhawk.ui;

import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.data.StockValuesColumns;
import com.sam_chordas.android.stockhawk.rest.Utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.eazegraph.lib.charts.ValueLineChart;
import org.eazegraph.lib.models.LegendModel;
import org.eazegraph.lib.models.ValueLinePoint;
import org.eazegraph.lib.models.ValueLineSeries;

/**
 * Created by hussamelemmawi on 14/09/16.
 */
public class StockValuesActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{
    private static final String LOG_TAG = StockValuesActivity.class.getSimpleName();

    private String mSymbol;

    private OkHttpClient client = new OkHttpClient()
            .newBuilder()
            .addNetworkInterceptor(new StethoInterceptor())
            .build();

    static final int NOW = 0;
    static final int YEAR_LATER = -1;

    static final int LOADER_ID = 1;

    ValueLineChart mLineChart;
    ValueLineSeries series;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_values);

        mSymbol = getIntent().getStringExtra("symbol");
        if (!freshDataStoredInDatabase()){
            new FetchStockValuesTask().execute();
        }
        initializeChart();
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    protected void onRestart() {
        getLoaderManager().restartLoader(LOADER_ID, null, this);
        super.onRestart();
    }

    void initializeChart(){
        mLineChart = (ValueLineChart) findViewById(R.id.line_chart);

        series = new ValueLineSeries();
        series.setColor(R.color.material_green_A700);
    }

    boolean freshDataStoredInDatabase(){
        Cursor cursor = this.getContentResolver().query(QuoteProvider.StockValues.CONTENT_URI,
                null,
                StockValuesColumns.SYMBOL + " = ?",
                new String[]{mSymbol},
                null);
        if (cursor != null && cursor.getCount() > 0){
            cursor.moveToNext();
            String lastStoredDate = cursor.getString(cursor.getColumnIndex(StockValuesColumns.DATE));
            Log.d(LOG_TAG, lastStoredDate + " fasel " +  Utils.getDate(NOW));
            String[] parts = lastStoredDate.split("-");
            int storedYear = Integer.parseInt(parts[0]);
            int storedMonth = Integer.parseInt(parts[1]);
            int storedDay = Integer.parseInt(parts[2]);

            String rightNow = Utils.getDate(NOW);
            parts = rightNow.split("-");
            int currentYear = Integer.parseInt(parts[0]);
            int currentMonth = Integer.parseInt(parts[1]);
            int currentDay = Integer.parseInt(parts[2]) -1;

            if (storedDay == currentDay){
                if (storedMonth == currentMonth){
                    if (storedYear == currentYear)
                        return true;
                }
            }else {
                return false;
            }

        }
            return false;
    }

    private class FetchStockValuesTask extends AsyncTask<Void, Void, Void>{

        String fetchData(String url) throws IOException {
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            Response response = client.newCall(request).execute();
            return response.body().string();
        }

        @Override
        protected Void doInBackground(Void...params) {

            StringBuilder urlStringBuilder = new StringBuilder();

            Log.d(LOG_TAG, "ana ha fetch ahoo ...");

            try{
                urlStringBuilder.append("https://query.yahooapis.com/v1/public/yql?q=");

                urlStringBuilder.append(URLEncoder.encode(" select * from yahoo.finance.historicaldata" +
                        " where symbol = \"" + mSymbol + "\""
                        + " and startDate = \"" + Utils.getDate(YEAR_LATER) + "\""
                        + " and endDate = \"" + Utils.getDate(NOW) + "\"",
                        "UTF-8"));

                urlStringBuilder.append("&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables."
                        + "org%2Falltableswithkeys&callback=");

                String getResponse = fetchData(urlStringBuilder.toString());

                StockValuesActivity.this.getContentResolver().applyBatch(QuoteProvider.AUTHORITY,
                        Utils.stockValuesJsonToContentVals(getResponse));

                Log.d(LOG_TAG, mSymbol+mSymbol+mSymbol);

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

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null){
            data.moveToFirst();
            if (data.getCount() > 0){
                String date;
                do {
                    date = data.getString(data.getColumnIndex(StockValuesColumns.DATE));
                    series.addPoint(new ValueLinePoint(Utils.formateDateForLegend(date),
                            Float.parseFloat(data.getString(data.getColumnIndex(StockValuesColumns.HIGH)))));

                }while (data.moveToNext());
                mLineChart.addSeries(series);
                mLineChart.startAnimation();
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
