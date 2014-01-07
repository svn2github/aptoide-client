package cm.aptoide.ptdev.widget;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.*;
import android.widget.*;
import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.SearchManager;
import cm.aptoide.ptdev.WebSocketSingleton;
import cm.aptoide.ptdev.adapters.WidgetSuggestionsAdapter;
import cm.aptoide.ptdev.downloadmanager.Utils;
import cm.aptoide.ptdev.R;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;


/**
 * Created by brutus on 02-01-2014.
 */
public class SearchWidgetActivity extends Activity {

    private AutoCompleteTextView searchAutoComplete;
    private WidgetSuggestionsAdapter suggestionAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Aptoide.getThemePicker().setAptoideTheme(this);
        super.onCreate(savedInstanceState);

//        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.search_widget_activity);

        searchAutoComplete = (AutoCompleteTextView) findViewById(R.id.search_text);
        searchAutoComplete.setThreshold(3);

        suggestionAdapter = new WidgetSuggestionsAdapter(this);

        searchAutoComplete.setAdapter(suggestionAdapter);

        searchAutoComplete.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().length() > 2) {
                    try {
                        searchAutoComplete.setAdapter(null);
                        WebSocketSingleton.getInstance().send(searchAutoComplete.getText().toString());
                        handler.post(runnable);
                    } catch (NullPointerException e) {

                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });


        ((Button) findViewById(R.id.search_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchQuery = searchAutoComplete.getText().toString();

                if (searchQuery.length() != 0) {
                    searchApp(searchQuery);
                    finish();

                } else {
                    Toast toast = Toast.makeText(SearchWidgetActivity.this, "Empty search, write an application name", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();

                }
            }
        });

        WebSocketSingleton webSocketSingleton = WebSocketSingleton.getInstance();
        webSocketSingleton.connect();
        webSocketSingleton.setBlockingQueue(blockingQueue);
    }

    final static Handler handler = new Handler();
    BlockingQueue<Cursor> blockingQueue = new ArrayBlockingQueue<Cursor>(1);
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try {
                Cursor matrix_cursor = blockingQueue.poll(5, TimeUnit.SECONDS);
                for(matrix_cursor.moveToFirst();!matrix_cursor.isAfterLast();matrix_cursor.moveToNext()){
                    Log.d("Cursor", matrix_cursor.getString(matrix_cursor.getColumnIndex(android.app.SearchManager.SUGGEST_COLUMN_TEXT_1)));
                }
                suggestionAdapter = new WidgetSuggestionsAdapter(SearchWidgetActivity.this);

                searchAutoComplete.setAdapter(suggestionAdapter);
                suggestionAdapter.swapCursor(matrix_cursor);
                suggestionAdapter.notifyDataSetChanged();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();

        WebSocketSingleton.getInstance().disconnect();
    }

    private void searchApp(String query) {
        String url = Aptoide.getConfiguration().getUriSearch() + query + "&q=" + Utils.filters(this);

        Intent i = new Intent(Intent.ACTION_VIEW);
        url = url.replaceAll(" ", "%20");
        i.setData(Uri.parse(url));
        startActivity(i);
    }







}
