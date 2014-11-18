package com.example.aptoidehelper.app;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.app.DialogFragment;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;


public class MainActivity extends ActionBarActivity {
    String path = Environment.getExternalStorageDirectory().getAbsolutePath();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.delete_aptoide_cache).setOnClickListener(new Listeners.OnDeleteCacheClick(this));
        findViewById(R.id.disable_debug_mode).setOnClickListener(new Listeners.DebugMode(false ,this));
        findViewById(R.id.enable_debug_mode).setOnClickListener(new Listeners.DebugMode(true, this));
        findViewById(R.id.delete_aptoide_settings_cache).setOnClickListener(new Listeners.OnDeleteCacheClick(this));
        findViewById(R.id.set_country).setOnClickListener(new Listeners.SetAptoideCountry((android.widget.EditText) findViewById(R.id.iso_field),this));

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void deleteDirectoryAptoide() {

        DeleteDialog deleteDialog = new DeleteDialog();
        Bundle bundle = new Bundle();
        bundle.putString("path", path + "/.aptoide");
        deleteDialog.setArguments(bundle);
        deleteDialog.show(getSupportFragmentManager(), "deleteDialog");
    }

    public void deleteDirectoryAptoideSettings() {
        DeleteDialog deleteDialog = new DeleteDialog();
        Bundle bundle = new Bundle();
        bundle.putString("path", path + "/.aptoide_settings");
        deleteDialog.setArguments(bundle);
        deleteDialog.show(getSupportFragmentManager(), "deleteDialog");    }


    public static class DeleteDialog extends DialogFragment{
        String path;
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
            path = getArguments().getString("path");
        }

        @Override
        public void onResume() {
            super.onResume();
            new Task(this).execute(path);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            ProgressDialog pd = new ProgressDialog(getActivity());
            pd.setMessage("Please wait");
            pd.setCancelable(false);
            return pd;
        }

        public static class Task extends AsyncTask<String, Void, Boolean>{

            private final DialogFragment dialog;

            public Task(DialogFragment dialog){
                this.dialog = dialog;
            }


            @Override
            protected Boolean doInBackground(String... params) {


                boolean result = false;

                try{
                    FileUtils.deleteDirectory(new File(params[0]));
                    result = true;
                } catch (IOException e){
                    e.printStackTrace();
                }

                return result;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                super.onPostExecute(result);

                if(!result && dialog.isAdded()){
                    Toast.makeText(dialog.getActivity(), "Error deleting files", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(dialog.getActivity(), "Success", Toast.LENGTH_LONG).show();
                }
                dialog.dismissAllowingStateLoss();

            }
        }


    }

}
