package cm.aptoide.ptdev;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.*;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.*;
import android.provider.Settings;
import android.util.Log;
import cm.aptoide.ptdev.configuration.AccountGeneral;
import cm.aptoide.ptdev.configuration.Constants;
import cm.aptoide.ptdev.preferences.SecurePreferences;
import cm.aptoide.ptdev.services.HttpClientSpiceService;
import cm.aptoide.ptdev.utils.AptoideUtils;
import cm.aptoide.ptdev.utils.Filters;
import cm.aptoide.ptdev.webservices.CheckUserCredentialsRequest;
import com.octo.android.robospice.SpiceManager;
import com.rabbitmq.client.*;
import com.rabbitmq.client.impl.AMQConnection;
import com.rabbitmq.client.impl.ChannelN;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

/**
 * Created by j-pac on 27-01-2014.
 */
public class WebInstallSyncAdapter extends AbstractThreadedSyncAdapter {

    private Class appViewClass = Aptoide.getConfiguration().getAppViewActivityClass();

    public WebInstallSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }


    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d("Aptoide-WebInstall", "onPerformSync()");

        if(!Aptoide.isWebInstallServiceRunning()) {

            SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(getContext());
            String queueName = sPref.getString("queueName", "");
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(Constants.WEBINSTALL_HOST);
            factory.setConnectionTimeout(20000);
            factory.setVirtualHost("webinstall");
            factory.setUsername("public");
            factory.setPassword("public");
            AMQConnection connection = null;
            ChannelN channel = null;
            try {

                connection = (AMQConnection) factory.newConnection();
                channel = (ChannelN) connection.createChannel();
                channel.basicQos(0);

                GetResponse response;
                while ((response = channel.basicGet(queueName, false)) != null) {
                    String message = new String(response.getBody(), "UTF-8");
                    Log.d("syncAdapter", "MESSAGE: " + message);

                    handleMessage(message);
                    channel.basicAck(response.getEnvelope().getDeliveryTag(), false);
                }
                channel.close();
                connection.disconnectChannel(channel);
                connection.close();
                //sPref.edit().putBoolean(Constants.WEBINSTALL_QUEUE_EXCLUDED, false);
            } catch (ShutdownSignalException e) {
                e.printStackTrace();

                try {
                    if (connection != null && channel != null) {
                        connection.disconnectChannel(channel);
                    }

                    if (connection != null) {
                        connection.close();
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                } catch (ShutdownSignalException e1){
                    e1.printStackTrace();
                }

            } catch (Exception e) {
                e.printStackTrace();
                try {

                    if (connection != null && channel != null) {
                        connection.disconnectChannel(channel);
                    }

                    if (connection != null && connection.isOpen()) {
                        connection.close();
                    }

                } catch (IOException e1) {
                    e1.printStackTrace();
                } catch (ShutdownSignalException e1){
                    e1.printStackTrace();
                }
                //sPref.edit().putBoolean(Constants.WEBINSTALL_QUEUE_EXCLUDED, true).commit();
            }
        }


    }

    void handleMessage(String body) {
        try {

            JSONObject object = new JSONObject(body);

            Intent i = new Intent(getContext(), appViewClass);
            SecurePreferences securePreferences = new SecurePreferences(getContext());
            String authToken = securePreferences.getString("devtoken", "");

            String repo = object.getString("repo");
            long id = object.getLong("id");
            String md5sum = object.getString("md5sum");
            i.putExtra("fromMyapp", true);
            i.putExtra("repoName", repo);
            i.putExtra("id", id);
            i.putExtra("download_from", "webinstall");
            i.putExtra("md5sum", md5sum);
            String deviceId = android.provider.Settings.Secure.getString(getContext().getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            String hmac = object.getString("hmac");
            String calculatedHmac = AptoideUtils.Algorithms.computeHmacSha1(repo+id+md5sum, authToken+deviceId);
            if(hmac.equals(calculatedHmac)){
                getContext().startActivity(i);
            }
        } catch (JSONException e) {
            e.printStackTrace();

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
    }



}
