package cm.aptoidetv.pt;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeoutException;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;



public class AutoUpdate extends AsyncTask<Void, Void, AutoUpdate.AutoUpdateInfo> {

    private static final String PREF_PATH_CACHE = "dev_mode_path_cache";
    public static final String PATH_SDCARD       = Environment.getExternalStorageDirectory().getAbsolutePath();
    public static final String PATH_CACHE        = PATH_SDCARD + "/.aptoide/";

    private final String TMP_UPDATE_FILE = PATH_CACHE + "aptoideUpdate.apk";
    private final String url;
    public static final String OEM_AUTO_UPDATE_URL = "http://%s.aptoide.com/latest_version_%s.xml";

    private Activity activity;
    private String TAG = "AutoUpdate";

    public AutoUpdate(Activity activity){
        this.activity=activity;
        url = getAutoUpdateUrl();
        //SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(activity);
    }

    public String getAutoUpdateUrl(){
        return String.format(OEM_AUTO_UPDATE_URL, activity.getString(R.string.defaultstorename), activity.getString(R.string.defaultstorename));
    }

    @Override
    protected AutoUpdateInfo doInBackground(Void... params) {

        try {

            SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
            AutoUpdateHandler autoUpdateHandler = new AutoUpdateHandler();

            Log.d(TAG, "Requesting auto-update from " +  url);
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();

            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            parser.parse(connection.getInputStream(),autoUpdateHandler);

            return autoUpdateHandler.getAutoUpdateInfo();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(AutoUpdateInfo autoUpdateInfo) {
        super.onPostExecute(autoUpdateInfo);

        if(autoUpdateInfo!=null){
            String packageName = activity.getPackageName();
            int vercode = autoUpdateInfo.vercode;
            int minsdk = autoUpdateInfo.minsdk;
            try {
                if(vercode > activity.getPackageManager().getPackageInfo(packageName,0).versionCode && Build.VERSION.SDK_INT >= minsdk){
                    requestUpdateSelf(autoUpdateInfo);
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

        }

    }

    private void requestUpdateSelf(final AutoUpdateInfo autoUpdateInfo) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        final AlertDialog updateSelfDialog = dialogBuilder.create();
        updateSelfDialog.setTitle(activity.getText(R.string.update_self_title));
        updateSelfDialog.setIcon(R.drawable.ic_launcher);
        updateSelfDialog.setMessage(activity.getString(R.string.update_self_msg, activity.getString(R.string.marketname)));
        updateSelfDialog.setCancelable(false);
        updateSelfDialog.setButton(Dialog.BUTTON_POSITIVE, activity.getString(android.R.string.yes), new Dialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                new DownloadSelfUpdate().execute(autoUpdateInfo);
            }
        });
        updateSelfDialog.setButton(Dialog.BUTTON_NEGATIVE, activity.getString(android.R.string.no), new Dialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int arg1) {
                dialog.dismiss();
            }
        });
        updateSelfDialog.show();
    }




    private class AutoUpdateHandler extends DefaultHandler2{


        AutoUpdateInfo info = new AutoUpdateInfo();

        private AutoUpdateInfo getAutoUpdateInfo() {
            return info;
        }

        private StringBuilder sb = new StringBuilder();


        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);
            sb.setLength(0);
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            super.characters(ch, start, length);
            sb.append(ch,start,length);

        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);

            if(localName.equals("versionCode")){
                info.vercode = Integer.parseInt(sb.toString());
            }else if(localName.equals("uri")){
                info.path = sb.toString();
            }else if(localName.equals("md5")){
                info.md5 = sb.toString();
            }else if(localName.equals("minSdk")){
                info.minsdk = Integer.parseInt(sb.toString());
            }

        }
    }

    static class AutoUpdateInfo{
        String md5;
        int vercode;
        String path;
        int minsdk = 0;
    }

    private class DownloadSelfUpdate extends AsyncTask<AutoUpdateInfo, Void, Void> {
        private final ProgressDialog dialog = new ProgressDialog(activity);
        String latestVersionUri;
        String referenceMd5;
        void retrieveUpdateParameters(AutoUpdateInfo autoUpdateInfo) {
            try {
                latestVersionUri = autoUpdateInfo.path;
                referenceMd5 = autoUpdateInfo.md5;
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "Update connection failed!  Keeping current version.");
            }
        }
        @Override
        protected void onPreExecute() {
            this.dialog.setMessage(activity.getString(R.string.retrieving_update));
            this.dialog.show();
            super.onPreExecute();
        }
        @Override
        protected Void doInBackground(AutoUpdateInfo... paramArrayOfParams) {
            try {
                retrieveUpdateParameters(paramArrayOfParams[0]);
                File f_chk = new File(TMP_UPDATE_FILE);
                if (f_chk.exists()) {
                    f_chk.delete();
                }
                FileOutputStream saveit = new FileOutputStream(TMP_UPDATE_FILE);
                DefaultHttpClient mHttpClient = new DefaultHttpClient();
                HttpGet mHttpGet = new HttpGet(latestVersionUri);
                HttpResponse mHttpResponse = mHttpClient.execute(mHttpGet);
                if (mHttpResponse == null) {
                    Log.d(TAG, "Problem in network... retry...");
                    mHttpResponse = mHttpClient.execute(mHttpGet);
                    if (mHttpResponse == null) {
                        Log.d(TAG, "Major network exception... Exiting!");
                        saveit.close();
                        throw new TimeoutException();
                    }
                }
                if (mHttpResponse.getStatusLine().getStatusCode() == 401) {
                    saveit.close();
                    throw new TimeoutException();
                } else {
                    InputStream getit = mHttpResponse.getEntity().getContent();
                    byte data[] = new byte[8096];
                    int bytesRead;
                    bytesRead = getit.read(data, 0, 8096);
                    while (bytesRead != -1) {
                        saveit.write(data, 0, bytesRead);
                        bytesRead = getit.read(data, 0, 8096);
                    }
                    Log.d(TAG, "Download done!");
                    saveit.flush();
                    saveit.close();
                    getit.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "Update connection failed!  Keeping current version.");
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }
            super.onPostExecute(result);
            if (!(referenceMd5 == null)) {
                try {
                    File apk = new File(TMP_UPDATE_FILE);
                    if (referenceMd5.equalsIgnoreCase(Utils.Algorithms.md5Calc(apk))) {
                        doUpdateSelf();
                    } else {
                        Log.d(TAG, referenceMd5 + " VS " + Utils.Algorithms.md5Calc(apk));
                        throw new Exception(referenceMd5 + " VS " + Utils.Algorithms.md5Calc(apk));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d(TAG, "Update package checksum failed!  Keeping current version.");
                    if (this.dialog.isShowing()) {
                        this.dialog.dismiss();
                    }
                    super.onPostExecute(result);
                }
            }
        }
    }
    private void doUpdateSelf() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.parse("file://" + TMP_UPDATE_FILE), "application/vnd.android.package-archive");
        activity.startActivityForResult(intent, 99);
    }


}
