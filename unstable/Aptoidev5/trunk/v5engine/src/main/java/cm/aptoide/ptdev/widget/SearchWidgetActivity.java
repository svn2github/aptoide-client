package cm.aptoide.ptdev.widget;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.*;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.downloadmanager.Utils;
import cm.aptoide.ptdev.R;


/**
 * Created by brutus on 02-01-2014.
 */
public class SearchWidgetActivity extends Activity {

    private EditText searchEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.search_widget_activity);

        //DisplayMetrics metrics = new DisplayMetrics();
        //getWindowManager().getDefaultDisplay().getMetrics(metrics);

        //WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        //layoutParams.width = (int) (metrics.widthPixels*0.80);
        //layoutParams.height = (int) (metrics.heightPixels*0.50);

        //getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        //getWindow().setBackgroundDrawable(new ColorDrawable(0x7f000000));
        //getWindow().setAttributes(layoutParams);



        searchEditText = (EditText) findViewById(R.id.search_text);

        ((Button) findViewById(R.id.search_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchQuery = searchEditText.getText().toString().trim();

                if (searchQuery.length() != 0) {
                    searchApp(searchQuery);
                    finish();
                } else {
                    Toast toast = new Toast(SearchWidgetActivity.this);
                    toast.setText("Empty search, write an app name");
                    toast.setDuration(Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
            }
        });

        setFinishOnTouchOutside(true);

    }



    private void searchApp(String query) {
        String url = Aptoide.getConfiguration().getUriSearch() + query + "&q=" + Utils.filters(this);

        Intent i = new Intent(Intent.ACTION_VIEW);
        url = url.replaceAll(" ", "%20");
        i.setData(Uri.parse(url));
        startActivity(i);
    }



}
