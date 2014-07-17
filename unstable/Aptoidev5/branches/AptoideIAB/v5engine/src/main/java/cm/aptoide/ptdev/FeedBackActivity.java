package cm.aptoide.ptdev;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

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
public class FeedBackActivity extends Activity {
    private static final String PATH="/";
    private static final String mPath = Environment.getExternalStorageDirectory().toString() + PATH;
    private static final String FeddBackSS="FeedBackSS.jpg";
    private static final String FeddBackLogs="logs.txt";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_back);
    }

    public static File screenshot(Activity a){
        Bitmap bitmap;
        View v1 = a.getWindow().getDecorView().getRootView();
        v1.setDrawingCacheEnabled(true);
        bitmap = Bitmap.createBitmap(v1.getDrawingCache());
        v1.setDrawingCacheEnabled(false);

        OutputStream fout = null;
        File imageFile = new File(mPath,FeddBackSS);
        try {
            fout = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fout);
            fout.flush();
            fout.close();
        } catch (FileNotFoundException e) {
            Log.e("what","FileNotFoundException: "+e.getMessage());
            return null;
        } catch (IOException e) {
            Log.e("what","IOException: "+e.getMessage());
            return null;
        }
        return imageFile;
    }
    private File readLogs(){

        Process process = null;
        try {
            process = Runtime.getRuntime().exec("logcat");
        } catch (IOException e) {
            return null;
        }
        FileOutputStream outputStream;
        File logsFile = new File(mPath,FeddBackLogs);
        try {
            outputStream = new FileOutputStream(logsFile);

            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            StringBuilder log=new StringBuilder();
            String line = null;
            int linecount =0;
            while (linecount<100 && (line = bufferedReader.readLine()) != null) {
                log.append(line);
                linecount++;
            }
            outputStream.write(log.toString().getBytes());
        } catch (IOException e) {
            return null;
        }
        return logsFile;
    }
    public void FeedBackSendMail(View view){
        String subject = ((EditText)findViewById(R.id.FeedBackSubject)).getText().toString();
        if(TextUtils.isEmpty(subject)){
            Toast.makeText(this,R.string.FeedBacknotvalid, Toast.LENGTH_SHORT).show();
            return;
        }
        String text = ((EditText)findViewById(R.id.FeedBacktext)).getText().toString();
        Boolean check = ((CheckBox)findViewById(R.id.FeedBackCheckBox)).isChecked();

        Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        emailIntent.setType("message/rfc822");

        emailIntent.putExtra(Intent.EXTRA_EMAIL ,new String[]{ "andre.santos@aptoide.com"});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "FeedBack: "+subject );
        emailIntent.putExtra(Intent.EXTRA_TEXT, text );
        //screenshot();
        if(check) {
            ArrayList<Uri> uris = new ArrayList<Uri>();
            File ss = new File(mPath,FeddBackSS);
            if (ss != null) {
                Uri urifile = Uri.fromFile(ss);
                uris.add(urifile);
                //emailIntent.putExtra(Intent.EXTRA_STREAM, urifile);
            }
            File logs= readLogs();
            if (logs != null) {
                Uri urifile = Uri.fromFile(logs);
                uris.add(urifile);
                //emailIntent.putExtra(Intent.EXTRA_STREAM, urifile);
            }
            emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM,uris);
        }
        try {
            startActivity(emailIntent);
            finish();
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "No email client installed.",Toast.LENGTH_LONG).show();
        }
    }

}