package com.sam_chordas.android.stockhawk.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

import java.util.ArrayList;

/**
 * Created by hussamelemmawi on 20/09/16.
 */
public class StockHawkWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new StockHawkWidgetListProvider(this.getApplicationContext(), intent);
    }
}

class StockHawkWidgetListProvider implements RemoteViewsService.RemoteViewsFactory {

    private Context context = null;
    private int appWidgetId;
    Cursor mData;

    private static final String[] QUOTE_COLUMNS = {
            QuoteColumns.SYMBOL,
            QuoteColumns.PERCENT_CHANGE,
            QuoteColumns.BIDPRICE,
            QuoteColumns.ISUP
    };

    private static final int COL_SYMBOL = 1;
    private static final int COL_BID_PRICE = 2;
    private static final int COL_PERCENT_CHANGE = 3;
    private static final int COL_ISUP = 4;


    public StockHawkWidgetListProvider(Context context, Intent intent){
        this.context = context;
        appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);

    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDataSetChanged() {
        if (mData != null){
            mData.close();
        }

        // Retrieve data from db
        final long identityToken = Binder.clearCallingIdentity();
        mData = context.getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                new String[]{QuoteColumns._ID, QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE,
                        QuoteColumns.PERCENT_CHANGE, QuoteColumns.ISUP},
                QuoteColumns.ISCURRENT + " = ?",
                new String[]{"1"},
                null);
        Binder.restoreCallingIdentity(identityToken);

        if (mData != null && mData.getCount() > 0){
            mData.moveToFirst();
            do {

            }while (mData.moveToNext());
        }
    }

    @Override
    public void onDestroy() {
        if (mData != null){
            mData.close();
            mData = null;
        }
    }

    @Override
    public int getCount() {
        return mData == null ? 0 : mData.getCount();
    }

    @Override
    public RemoteViews getViewAt(int position) {

        if (position == AdapterView.INVALID_POSITION || mData == null ||
                ! mData.moveToPosition(position)){
            return null;
        }

        String symbol = mData.getString(COL_SYMBOL);
        String bidPrice = mData.getString(COL_BID_PRICE);
        String percentChange = mData.getString(COL_PERCENT_CHANGE);

        int isUp = mData.getInt(COL_ISUP);

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                R.layout.list_item_widget);

        remoteViews.setTextViewText(R.id.stock_symbol, symbol);
        remoteViews.setTextViewText(R.id.bid_price, bidPrice);
        remoteViews.setTextViewText(R.id.change, percentChange);

        if (isUp == 1){
                remoteViews.setTextColor(R.id.change,
                        context.getResources().getColor(R.color.material_green_A700));
        }else if (isUp == 0){
            remoteViews.setTextColor(R.id.change,
                    context.getResources().getColor(R.color.material_red_A700));
        }else {
            remoteViews.setTextColor(R.id.change,
                    context.getResources().getColor(R.color.material_orange_A700));
        }

        // Next, we set a fill-intent which will be used to fill-in the pending intent template
        // which is set on the collection view in StackWidgetProvider.
        Bundle extras = new Bundle();
        extras.putInt(StockHawkWidgetProvider.EXTRA_ITEM, position);
        Intent fillInIntent = new Intent();
        fillInIntent.putExtras(extras);
        remoteViews.setOnClickFillInIntent(R.id.stock_symbol, fillInIntent);

        return remoteViews;
    }

    @Override
    public RemoteViews getLoadingView() {
        return new RemoteViews(context.getPackageName(), R.layout.list_item_widget);
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}

