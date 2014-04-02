package cm.aptoide.pt;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;
import cm.aptoide.pt.configuration.AptoideConfiguration;
import cm.aptoide.pt.webservices.login.Login;
import com.actionbarsherlock.app.SherlockActivity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;


public class CreditCardEducomp extends SherlockActivity {

	private boolean wait=false;
	Activity ctx = this;
	String paykey;

//	int server = PayPal.ENV_SANDBOX;

	String userMail;
	String token;
//	String url = ("http://webservices.aptoide.com/webservices/hasPurchaseAuthorization");
	String urlPay = AptoideConfiguration.getInstance().getWebServicesUri() + "webservices/payApk";

	//http://dev.aptoide.com/webservices/payApk/e8b1d6a4dd8b5351c823cd1af95243ed70e9ad3f4f5f2f9c0e89b/rui.mateus@caixamagica.pt/diogo/com.smedio.mediaplayer/1.05.7/completed_payment/json
	String urlRedirect="https://www.paypal.com/webscr?cmd=_ap-payment&paykey=";
//	String urlRedirect="https://www.paypal.com/webapps/adaptivepayment/flow/pay?expType=mini&paykey=";
	TextView tv;
	boolean canceled=false;
	WebView web;
	int operation=1;
	boolean failed=false;

	// 1 - Get pre-approval key
	// 2 - Validation status
	// 3 - Pay
	private String repo;
	private SharedPreferences sPref;
	private String versionName;
	private String apkid;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		AptoideThemePicker.setAptoideTheme(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.paypal_account);
		Bundle b = getIntent().getExtras();
		String url = b.getString("url");

//		product=10;

		web = (WebView)findViewById(R.id.webView1);
		web.getSettings().setJavaScriptEnabled(true);
		web.getSettings().setBuiltInZoomControls(true);

		web.setWebViewClient(new WebViewClient() {


            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                super.onReceivedSslError(view, handler, error);
                Log.d("educomp", "onPageStarted ");
                handler.proceed();
            }



            @Override
			public void onPageStarted(WebView view, final String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);

				Log.d("preapproval", "onPageStarted " + url);
				if(url.contains("aptoidepayments://") && !wait){
                    wait = true;
                    if(url.contains("ok")){
                        Toast.makeText(CreditCardEducomp.this, "Transaction complete", Toast.LENGTH_LONG).show();
                        setResult(RESULT_OK,null);
                        finish();
                    }else{
                        Toast.makeText(CreditCardEducomp.this, "Transaction failed", Toast.LENGTH_LONG).show();
                        setResult(RESULT_CANCELED,null);
                        finish();
                    }


                }
			}
		});
        web.loadUrl(url);
	}
    public ProgressDialog pd;


	public void send(final String url, final String params){

		Thread t = new Thread(){
			public void run() {

				String temp=null;

				HttpClient client = new DefaultHttpClient();
				HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000);
				HttpResponse response=null;
				HttpGet request = new HttpGet();

				try {
					request.setURI(new URI(url+"/"+params));
					System.out.println(request.getURI());
				} catch (URISyntaxException e) {
					Log.e("preapproval", "URISyntaxException");
					e.printStackTrace();
				}

				try {
					response = client.execute(request);
				} catch (ClientProtocolException e) {
					Log.e("preapproval", "ClientProtocolException");
					e.printStackTrace();
				} catch (IOException e) {
					Log.e("preapproval", "IOException on response");
					e.printStackTrace();
				}

				if(response!=null){
					try {
						temp = EntityUtils.toString(response.getEntity());
					} catch (ParseException e) {
						Log.e("preapproval", "ParseException");
						e.printStackTrace();
					} catch (IOException e) {
						Log.e("preapproval", "IOException on parse");
						e.printStackTrace();
					}
					Log.i("preapproval", temp);
				}
				else{
					Log.e("preapproval", "the response is null");
				}

				Bundle data=new Bundle();
				data.putString("response", temp);
				Message msg = new Message();
				msg.setData(data);

				handler2.sendMessage(msg);
			}
		};
		t.start();
	}


	private Handler handler2 = new Handler() {

        @Override
		public void handleMessage(Message msg) {

			Bundle data=msg.getData();
			String response=data.getString("response");

			Log.i("preapproval", "handler: "+response);

			JSONObject respJSON;

			try {
				respJSON = new JSONObject(response);

				if(respJSON.getString("status").equals("OK")){

					if(respJSON.has("pay_key")){
						paykey=respJSON.getString("pay_key");
						web.loadUrl(urlRedirect+paykey);

//                        String url = urlRedirect+paykey;
//                        Intent i = new Intent(Intent.ACTION_VIEW);
//                        i.setData(Uri.parse(url));
//                        startActivity(i);

					}
                    if(respJSON.has("paypalStatus")){
						if(respJSON.getString("paypalStatus").equals("completed")){
							System.out.println("Payed!");
							setResult(RESULT_OK,null);
							finish();
						}
					}
//					setContentView(R.layout.buy);
//					tv=(TextView)findViewById(R.id.logbuy);
//					tv.setText("Payment Completed!");
				}else {
//					setContentView(R.layout.buy);
//					tv=(TextView)findViewById(R.id.logbuy);
//					tv.setText("Payment Failed!");
				}

			} catch (Exception e) {
				Toast.makeText(ctx, "There was an error. Please try again", Toast.LENGTH_LONG).show();
				finish();
				Log.e("preapproval", "failed to create a JSON response object or get String");
			}
            if(pd.isShowing())pd.dismiss();
		}
	};

//	public void Download(){
//		Intent myIntent = new Intent(this, Download.class);
//		this.startActivity(myIntent);
//	}

}
