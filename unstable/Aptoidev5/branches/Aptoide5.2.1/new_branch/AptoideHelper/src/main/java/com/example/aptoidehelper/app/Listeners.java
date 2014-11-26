package com.example.aptoidehelper.app;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.widget.EditText;

/**
 * Created by rmateus on 07-05-2014.
 */
public class Listeners {

    public static class OnDeleteCacheClick implements View.OnClickListener {
        private final Context context;

        public OnDeleteCacheClick(Context mainActivity) {
            this.context = mainActivity;
        }

        @Override
        public void onClick(View v) {


            switch(v.getId()){
                case R.id.delete_aptoide_cache:
                    ((MainActivity)context).deleteDirectoryAptoide();
                    break;
                case R.id.delete_aptoide_settings_cache:
                    ((MainActivity)context).deleteDirectoryAptoideSettings();
                    break;
            }


        }
    }


    public static class SetAptoideCountry implements View.OnClickListener {
        private final EditText editText;
        private final Context context;

        public SetAptoideCountry(EditText editText, Context context) {
            this.editText = editText;
            this.context = context;
        }

        @Override
        public void onClick(View v) {
            String base_url = "content://cm.aptoide.pt.StubProvider";
            Uri uri = Uri.parse(base_url + "/changePreference");

            ContentValues values = new ContentValues();
            values.put("forcecountry", editText.getText().toString());
            context.getContentResolver().update(uri, values, null, null);


        }
    }

    public static class SetNotificationType implements View.OnClickListener {
        private final EditText editText;
        private final Context context;

        public SetNotificationType(EditText editText, Context context) {
            this.editText = editText;
            this.context = context;
        }

        @Override
        public void onClick(View v) {
            String base_url = "content://cm.aptoide.pt.StubProvider";
            Uri uri = Uri.parse(base_url + "/changePreference");

            ContentValues values = new ContentValues();
            values.put("notificationtype", editText.getText().toString());
            context.getContentResolver().update(uri, values, null, null);


        }
    }

    public static class DebugMode implements View.OnClickListener {
        private final boolean b;
        private final Context context;

        public DebugMode(boolean b, Context context) {
            this.b = b;
            this.context = context;
        }

        @Override
        public void onClick(View v) {
            String base_url = "content://cm.aptoide.pt.StubProvider";
            Uri uri = Uri.parse(base_url + "/changePreference");

            ContentValues values = new ContentValues();
            values.put("debugmode", b);
            context.getContentResolver().update(uri, values, null, null);
        }
    }
}
