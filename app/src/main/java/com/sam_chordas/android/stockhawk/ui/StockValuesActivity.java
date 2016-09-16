package com.sam_chordas.android.stockhawk.ui;

import android.content.OperationApplicationException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.Utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by hussamelemmawi on 14/09/16.
 */
public class StockValuesActivity extends AppCompatActivity {
    private static final String LOG_TAG = StockValuesActivity.class.getSimpleName();

    private String mSymbol;

    private OkHttpClient client = new OkHttpClient()
            .newBuilder()
            .addNetworkInterceptor(new StethoInterceptor())
            .build();

    static final int NOW = 0;
    static final int YEAR_LATER = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSymbol = getIntent().getStringExtra("symbol");
        new FetchStockValuesTask().execute();
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
}
