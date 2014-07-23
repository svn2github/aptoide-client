package cm.aptoide.ptdev;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Created by asantos on 17-07-2014.
 */
public class FeedBackActivity extends ActionBarActivity {
    private static final String PATH = "/";
    private static final String mPath = Environment.getExternalStorageDirectory().toString() + PATH;
    private static final String FeddBackSS = "FeedBackSS.jpg";
    private static final String FeddBackLogs = "logs.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Aptoide.getThemePicker().setAptoideTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_back);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.feedback_send_button));
    }

    public static File screenshot(Activity a) {
        Bitmap bitmap;
        View v1 = a.getWindow().getDecorView().getRootView();
        v1.setDrawingCacheEnabled(true);
        bitmap = Bitmap.createBitmap(v1.getDrawingCache());
        v1.setDrawingCacheEnabled(false);

        OutputStream fout = null;
        File imageFile = new File(mPath, FeddBackSS);
        try {
            fout = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fout);
            fout.flush();
            fout.close();
        } catch (FileNotFoundException e) {
            Log.e("FeedBackActivity-screenshot", "FileNotFoundException: " + e.getMessage());
            return null;
        } catch (IOException e) {
            Log.e("FeedBackActivity-screenshot", "IOException: " + e.getMessage());
            return null;
        }
        return imageFile;
    }

    private File readLogs() {

        Process process = null;
        try {
            process = Runtime.getRuntime().exec("logcat");
        } catch (IOException e) {
            Log.e("FeedBackActivity-readLogs", "IOException: " + e.getMessage());
            return null;
        }
        FileOutputStream outputStream;
        File logsFile = new File(mPath, FeddBackLogs);
        StringBuilder log = new StringBuilder();
        log.append("Android Build Version: " + Build.VERSION.SDK_INT + "\n");
        log.append("Build Model: " + Build.MODEL + "\n");
        log.append("Device: " + Build.DEVICE + "\n");
        log.append("Brand: " + Build.BRAND + "\n");
        log.append("CPU: " + Build.CPU_ABI + "\n");
        log.append("\nLogs:\n");
        try {
            outputStream = new FileOutputStream(logsFile);
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            String line = null;
            int linecount = 0;
            while (linecount < 100 && (line = bufferedReader.readLine()) != null) {

                log.append(line + "\n");
                linecount++;
            }
            outputStream.write(log.toString().getBytes());

        } catch (IOException e) {
            return logsFile;
        }

        return logsFile;
    }

    public void FeedBackSendMail(View view) {
        String subject = ((EditText) findViewById(R.id.FeedBackSubject)).getText().toString();
        if (TextUtils.isEmpty(subject)) {
            Toast.makeText(this, R.string.feedback_not_valid, Toast.LENGTH_SHORT).show();
            return;
        }
        String text = ((EditText) findViewById(R.id.FeedBacktext)).getText().toString();
        Boolean check = ((CheckBox) findViewById(R.id.FeedBackCheckBox)).isChecked();

        Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        emailIntent.setType("message/rfc822");

        //emailIntent.putExtra(Intent.EXTRA_EMAIL ,new String[]{ "andre.santos@aptoide.com"});
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"aptoide@aptoide.com"});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "[FeedBack]: " + subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, text);
        //screenshot();
        if (check) {
            ArrayList<Uri> uris = new ArrayList<Uri>();
            File ss = new File(mPath, FeddBackSS);
            if (ss != null) {
                Uri urifile = Uri.fromFile(ss);
                uris.add(urifile);
                //emailIntent.putExtra(Intent.EXTRA_STREAM, urifile);
            }
            File logs = readLogs();
            if (logs != null) {
                Uri urifile = Uri.fromFile(logs);
                uris.add(urifile);
                //emailIntent.putExtra(Intent.EXTRA_STREAM, urifile);
            }
            emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        }
        try {
            startActivity(emailIntent);
            finish();
            if (Build.VERSION.SDK_INT >= 10)
                FlurryAgent.logEvent("Feedback_Sended_Clicked_On_Send_Feedback_Button");
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, R.string.feedback_no_email, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Build.VERSION.SDK_INT >= 10) FlurryAgent.onStartSession(this, "X89WPPSKWQB2FT6B8F3X");
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (Build.VERSION.SDK_INT >= 10) FlurryAgent.onEndSession(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int i = item.getItemId();

        if (i == android.R.id.home) {
            finish();
        } else if (i == R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}