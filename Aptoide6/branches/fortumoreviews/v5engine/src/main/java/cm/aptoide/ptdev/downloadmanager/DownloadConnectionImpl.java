package cm.aptoide.ptdev.downloadmanager;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.util.Log;

import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.configuration.AccountGeneral;
import cm.aptoide.ptdev.preferences.SecurePreferences;
import cm.aptoide.ptdev.utils.AptoideUtils;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 02-07-2013
 * Time: 15:44
 * To change this template use File | Settings | File Templates.
 */
public class DownloadConnectionImpl extends DownloadConnection implements Serializable {

    HttpURLConnection connection;
    private BufferedInputStream mStream;
    private final static int TIME_OUT = 30000;
    private boolean paidApp;


    public DownloadConnectionImpl(URL url) throws IOException {
        super(url);
    }


    private String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException
    {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (NameValuePair pair : params)
        {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
        }

        return result.toString();
    }

    @Override
    public void connect(long downloaded, boolean update) throws IOException, CompletedDownloadException, NotFoundException, IPBlackListedException, ContentTypeNotApkException {
        connection = (HttpURLConnection) this.mURL.openConnection();

        connection.setConnectTimeout(TIME_OUT);
        connection.setReadTimeout(TIME_OUT);

        connection.setRequestProperty("User-Agent", AptoideUtils.NetworkUtils.getUserAgentString(Aptoide.getContext(), update));

        if(paidApp){
            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.setDoOutput(true);

            List<NameValuePair> params = new ArrayList<NameValuePair>();

            try{
                refreshToken();
            }catch (Exception ignored){}

            String token = SecurePreferences.getInstance().getString("access_token", null);
            params.add(new BasicNameValuePair("access_token", token));

            OutputStream os = connection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(getQuery(params));
            writer.flush();
            writer.close();
            os.close();
        }

        Log.d("DownloadManager", "Downloading from: " + mURL.toString() + " with " + AptoideUtils.NetworkUtils.getUserAgentString(Aptoide.getContext(), update));
        if (downloaded > 0L) {
            // server must support partial content for resume
            connection.addRequestProperty("Range", "bytes=" + downloaded + "-");
            int responseCode = connection.getResponseCode();
            Log.d("DownloadManager", "Response Code is: " + responseCode);
            if(responseCode == HttpStatus.SC_REQUESTED_RANGE_NOT_SATISFIABLE){
                throw new CompletedDownloadException();
            }else if (responseCode != HttpStatus.SC_PARTIAL_CONTENT) {
                throw new IOException("Server doesn't support partial content.");
            }
        } else if (connection.getResponseCode() != HttpStatus.SC_OK) {
            int responseCode = connection.getResponseCode();
            if(responseCode == 404){
                throw new NotFoundException();
            }else if(responseCode == 403){

                throw new IPBlackListedException();
            }
            // response not ok
            throw new IOException("Cannot retrieve file from server.");
        }

        if("application/json".equals(connection.getHeaderField("Content-Type"))){
            throw new ContentTypeNotApkException();
        }

        mStream = new BufferedInputStream(connection.getInputStream(), 8 * 1024);

    }

    private void refreshToken() throws IOException {
        Account account = AccountManager.get(Aptoide.getContext()).getAccountsByType(Aptoide.getConfiguration().getAccountType())[0];
        String refreshToken = "";
        try {
            refreshToken = AccountManager.get(Aptoide.getContext()).blockingGetAuthToken(account, AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS, false);
        } catch (OperationCanceledException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (AuthenticatorException e) {
            e.printStackTrace();
        }


        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("grant_type", "refresh_token");
        parameters.put("client_id", "Aptoide");
        parameters.put("refresh_token", refreshToken);
//        HttpContent content = new UrlEncodedContent(parameters);
//        GenericUrl url = new GenericUrl(WebserviceOptions.WebServicesLink+"/3/oauth2Authentication");
//        HttpRequest oauth2RefresRequest = AndroidHttp.newCompatibleTransport().createRequestFactory().buildPostRequest(url, content);
//        oauth2RefresRequest.setParser(new JacksonFactory().createJsonObjectParser());
//        OAuth responseJson = oauth2RefresRequest.execute().parseAs(OAuth.class);
//
//        SharedPreferences preferences = SecurePreferences.getInstance();
//
//        preferences.edit().putString("access_token", responseJson.getAccess_token()).commit();
    }


    @Override
    public void close() {
        connection.disconnect();
    }

    @Override
    public BufferedInputStream getStream() {
        return mStream;
    }

    @Override
    public long getShallowSize() throws IOException {
        return mURL.openConnection().getContentLength();  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setPaidApp(boolean paidApp) {
        this.paidApp = paidApp;
    }
}
