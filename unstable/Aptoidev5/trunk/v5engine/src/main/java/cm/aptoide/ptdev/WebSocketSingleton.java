package cm.aptoide.ptdev;

import android.app.SearchManager;
import android.content.Context;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;
import com.codebutler.android_websockets.WebSocketClient;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.concurrent.BlockingQueue;

/**
 * Created with IntelliJ IDEA.
 * User: brutus
 * Date: 30-09-2013
 * Time: 11:33
 * To change this template use File | Settings | File Templates.
 */
public class WebSocketSingleton {

    private static WebSocketClient web_socket_client;
    String[] matrix_columns = new String [] {SearchManager.SUGGEST_COLUMN_ICON_1,
            SearchManager.SUGGEST_COLUMN_TEXT_1,
            SearchManager.SUGGEST_COLUMN_QUERY,
            "_id"};
    private String query;
    private String buffer;
    private WebSocketClient.Listener listener = new WebSocketClient.Listener() {
        @Override
        public void onConnect() {
            Log.d("TAG", "On Connect");
        }

        @Override
        public void onMessage(String message) {

            try {
                JSONArray array = new JSONArray(message);
                MatrixCursor mCursor = new MatrixCursor(matrix_columns);
                for (int i = 0; i < array.length(); i++) {
                    String suggestion = array.get(i).toString();
                    Log.d("TAG", "Suggestion " + suggestion);
                    addRow(mCursor, suggestion, i);
                }

                if(array.length()==0){
                    buffer = query;
                }

                blockingQueue.add(mCursor);

            } catch (JSONException e) {
                e.printStackTrace();
            }


        }

        @Override
        public void onMessage(byte[] data) {
            Log.d("TAG", data.toString());


        }

        @Override
        public void onDisconnect(int code, String reason) {
            //To change body of implemented methods use File | Settings | File Templates.
            Log.d("TAG", reason);

        }

        @Override
        public void onError(Exception error) {
            error.printStackTrace();

        }
    };
    private Uri mNotificationUri;
    private Context mContext;
    private BlockingQueue blockingQueue;

    private WebSocketSingleton() {}

    public void send(String query) {
        this.query = query;
        if (web_socket_client.isConnected() && query.length() > 2 && ( buffer==null || !query.startsWith(buffer))) {
            web_socket_client.send("{\"query\":\"" + query + "\"}");
            Log.d("TAG", "Sending " + query);
        }else{
            MatrixCursor mCursor = null;
            blockingQueue.add(mCursor);
        }

    }

    public static WebSocketSingleton getInstance() {
        return WebSocketHolder.INSTANCE;
    }

    void disconnect() {

        Log.d("TAG", "onDisconnect");

        if(web_socket_client!=null){
            web_socket_client.disconnect();
            web_socket_client = null;
        }

    }

    public void connect() {


        if(web_socket_client==null){
            web_socket_client = new WebSocketClient(java.net.URI.create("ws://webservices.aptoide.com:9000"), listener, null);
            web_socket_client.connect();
        }
        Log.d("TAG", "OnConnecting");
    }

    public WebSocketSingleton setNotificationUri(Uri uri) {
        this.mNotificationUri = uri;
        return this;
    }

    public WebSocketSingleton setContext(Context context) {
        this.mContext = context;
        return this;
    }

    private void addRow(MatrixCursor matrix_cursor, String string, int i) {
        matrix_cursor.newRow().add(null).add(string).add(string).add(i);
    }

    public void setBlockingQueue(BlockingQueue a) {
        this.blockingQueue = a;
    }

    private static class WebSocketHolder {
        public static final WebSocketSingleton INSTANCE = new WebSocketSingleton();
    }


}
