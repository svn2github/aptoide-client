package cm.aptoide.ptdev;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.*;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.model.Download;
import cm.aptoide.ptdev.services.DownloadService;
import cm.aptoide.ptdev.services.RabbitMqService;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 10-10-2013
 * Time: 17:59
 * To change this template use File | Settings | File Templates.
 */
public class IntentReceiver extends ActionBarActivity implements DialogInterface.OnDismissListener {

    private ArrayList<String> server;
    String TMP_MYAPP_FILE;
    private HashMap<String, String> app;
    final Database db = new Database(Aptoide.getDb());
    long id;

//    private RabbitMqService rabbitMqService;
//
//    private ServiceConnection wConnection = new ServiceConnection() {
//        @Override
//        public void onServiceConnected(ComponentName name, IBinder service) {
//            RabbitMqService.RabbitMqBinder wBinder = (RabbitMqService.RabbitMqBinder) service;
//            rabbitMqService = wBinder.getService();
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName name) {
//        }
//    };


    private DownloadService service;
    private ServiceConnection downloadConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder downloadService) {
            service = ((DownloadService.LocalBinder)downloadService).getService();
            if(service.getDownload(id).getDownload()!=null){
//                onDownloadUpdate(service.getDownload(id).getDownload());
            }

            continueLoading();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        if(savedInstanceState==null){
//            Intent i = new Intent(this, Start.class);
//            i.putExtra("newrepo", "");
//            startActivity(i);
//        }

//        Intent a2 = new Intent(this, RabbitMqService.class);
//        bindService(a2, wConnection, Context.BIND_AUTO_CREATE);
        bindService(new Intent(this, DownloadService.class), downloadConnection, BIND_AUTO_CREATE);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

//        if(isFinishing()){
//            Log.d("RabbitMqService", "onDestroy");
//            unbindService(wConnection);
//        }

        unbindService(downloadConnection);

    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        proceed();
    }


    private void proceed() {
        if(server!=null){
            Intent i = new Intent(IntentReceiver.this,Start.class);
            i.putExtra("newrepo", server);
            i.addFlags(12345);
            startActivity(i);
            finish();
        }else{
            Toast.makeText(this, getString(R.string.error_occured), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void continueLoading(){
        TMP_MYAPP_FILE = getCacheDir()+"/myapp.myapp";
        String uri = getIntent().getDataString();
        System.out.println(uri);
        if(uri.startsWith("aptoiderepo")){

            ArrayList<String> repo = new ArrayList<String>();
            repo.add(uri.substring(14));
            Intent i = new Intent(IntentReceiver.this, Start.class);
            i.putExtra("newrepo", repo);
            i.addFlags(12345);
            startActivity(i);
            finish();

        }else if(uri.startsWith("aptoidexml")){

            String repo = uri.substring(13);
            parseXmlString(repo);
            Intent i = new Intent(IntentReceiver.this,Start.class);
            i.putExtra("newrepo", repo);
            i.addFlags(12345);
            startActivity(i);
            finish();

        }else if(uri.startsWith("aptoidesearch://")){

            startMarketIntent(uri.split("aptoidesearch://")[1]);

        }else if(uri.startsWith("market")){
            String params = uri.split("&")[0];
            String param = params.split("=")[1];
            if (param.contains("pname:")) {
                param = param.substring(6);
            } else if (param.contains("pub:")) {
                param = param.substring(4);
            }
            startMarketIntent(param);

        }else if(uri.startsWith("http://market.android.com/details?id=")){
            String param = uri.split("=")[1];
            startMarketIntent(param);

        }else if(uri.startsWith("https://market.android.com/details?id=")){
            String param = uri.split("=")[1];
            startMarketIntent(param);

        }else if(uri.startsWith("https://play.google.com/store/apps/details?id=")){
            String param = uri.split("=")[1];
            startMarketIntent(param);

        }else if(uri.contains("imgs.aptoide.com")){


            String[] strings = uri.split("-");
            long id = Long.parseLong(strings[strings.length-1].split("\\.myapp")[0]);

            Intent i = new Intent(this, AppViewActivity.class);
            i.putExtra("fromMyapp", true);
            i.putExtra("id", id);

            startActivity(i);
            finish();

        } else if (uri.startsWith("http://webservices.aptoide.com")) {

            List<NameValuePair> params = URLEncodedUtils.parse(URI.create(uri), "UTF-8");

            String uid = null;
            for (NameValuePair param : params) {

                if(param.getName().equals("uid")){
                    uid = param.getValue();
                }

                System.out.println(param.getName() + " : " + param.getValue());
            }

            try {

                long id = Long.parseLong(uid);
                Intent i = new Intent(this, AppViewActivity.class);
                i.putExtra("fromMyapp", true);
                i.putExtra("id", id);
                startActivity(i);

            } catch (Exception e){
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), R.string.error_occured, Toast.LENGTH_LONG).show();
            }


            finish();


        } else if (uri.startsWith("file://")) {
            new MyAppDownloader().execute(getIntent().getDataString());
        } else if (uri.startsWith("aptoideinstall://")) {
            long id = Long.parseLong(uri.substring("aptoideinstall://".length()));

            Intent i = new Intent(this, AppViewActivity.class);
            i.putExtra("fromMyapp", true);
            i.putExtra("id", id);

            startActivity(i);
            finish();
        } else {
            finish();
        }

    }



    private void downloadMyappFile(String myappUri) throws Exception{
        try{
            URL url = new URL(myappUri);
            URLConnection connection;
            if(!myappUri.startsWith("file://")){
                connection = (HttpURLConnection) url.openConnection();
                connection.setReadTimeout(5000);
                connection.setConnectTimeout(5000);
            }else{
                connection = url.openConnection();
            }

            BufferedInputStream getit = new BufferedInputStream(connection.getInputStream(),1024);

            File file_teste = new File(TMP_MYAPP_FILE);
            if(file_teste.exists())
                file_teste.delete();

            FileOutputStream saveit = new FileOutputStream(TMP_MYAPP_FILE);
            BufferedOutputStream bout = new BufferedOutputStream(saveit,1024);
            byte data[] = new byte[1024];

            int readed = getit.read(data,0,1024);
            while(readed != -1) {
                bout.write(data,0,readed);
                readed = getit.read(data,0,1024);
            }


            bout.close();
            getit.close();
            saveit.close();
        } catch(Exception e){
//			AlertDialog p = new AlertDialog.Builder(this).create();
//			p.setTitle(getText(R.string.top_error));
//			p.setMessage(getText(R.string.aptoide_error));
//			p.setButton(getText(R.string.btn_ok), new DialogInterface.OnClickListener() {
//			      public void onClick(DialogInterface dialog, int which) {
//			          return;
//			        } });
//			p.show();
            e.printStackTrace();
        }
    }

    private void parseXmlMyapp(String file) throws Exception{
        try {

            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXParser sp = spf.newSAXParser();

            MyappHandler handler = new MyappHandler();

            sp.parse(new File(file),handler);
            server = handler.getServers();
            app = handler.getApp();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    private void parseXmlString(String file){

        SAXParserFactory spf = SAXParserFactory.newInstance();

        try {

            SAXParser sp = spf.newSAXParser();
            XMLReader xr = sp.getXMLReader();
            MyappHandler handler = new MyappHandler();
            xr.setContentHandler(handler);

            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(file));
            xr.parse(is);
            server = handler.getServers();
            app = handler.getApp();


        } catch (IOException e) {
            e.printStackTrace();

        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    private void startMarketIntent(String param) {
        System.out.println(param);
        long id = db.getApkFromPackage(param);
        Intent i;
        if(id > 0){
            i = new Intent(this,AppViewActivity.class);
            i.putExtra("id", id);
        }else{
            i = new Intent(this,SearchManager.class);
            i.putExtra("search", param);
        }

        startActivity(i);
        finish();
    }

    class MyAppDownloader extends AsyncTask<String, Void, Void> {
        ProgressDialog pd;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(IntentReceiver.this);
            pd.show();
            pd.setCancelable(false);
            pd.setMessage(getString(R.string.please_wait));
        }

        @Override
        protected Void doInBackground(String... params) {

            try{
                System.out.println(params[0]);
                downloadMyappFile(params[0]);
                parseXmlMyapp(TMP_MYAPP_FILE);
            }catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(pd.isShowing()&&!isFinishing())pd.dismiss();
            if(app!=null&&!app.isEmpty()){

                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(IntentReceiver.this);
                final AlertDialog installAppDialog = dialogBuilder.create();
//                installAppDialog.setTitle(ApplicationAptoide.MARKETNAME);
                installAppDialog.setIcon(android.R.drawable.ic_menu_more);
                installAppDialog.setCancelable(false);


                installAppDialog.setMessage(getString(R.string.installapp_alrt) +app.get("name")+"?");

                installAppDialog.setButton(Dialog.BUTTON_POSITIVE, getString(android.R.string.yes), new Dialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
//                        Download download = new Download();
//                        Log.d("Aptoide-IntentReceiver", "getapk id: " + id);
//                        download.setId(id);
//                        ((Start)getApplicationContext()).installApp(0);

                        Toast toast = Toast.makeText(IntentReceiver.this, getString(R.string.starting_download), Toast.LENGTH_SHORT);
                        toast.show();
                    }
                });

                installAppDialog.setButton(Dialog.BUTTON_NEGATIVE, getString(android.R.string.no), neutralListener);
                installAppDialog.setOnDismissListener(IntentReceiver.this);
                installAppDialog.show();

            }else{
                proceed();
            }
        }
    }

    private DialogInterface.OnClickListener neutralListener =  new DialogInterface.OnClickListener() {

        public void onClick(DialogInterface dialog, int which) {
            return;
        }
    };


}
