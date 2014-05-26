package com.example.aptoidehelper.app;

import android.content.Context;
import android.os.Environment;
import android.view.View;

import java.io.File;

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



}
