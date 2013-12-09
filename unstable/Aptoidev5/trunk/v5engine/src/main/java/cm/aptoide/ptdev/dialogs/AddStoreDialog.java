package cm.aptoide.ptdev.dialogs;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.model.Login;
import cm.aptoide.ptdev.model.Store;
import cm.aptoide.ptdev.services.CheckServerRequest;
import cm.aptoide.ptdev.utils.AptoideUtils;
import cm.aptoide.ptdev.webservices.GetRepositoryInfoRequest;
import cm.aptoide.ptdev.webservices.json.RepositoryInfoJson;
import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.internal.widget.PopupWindowCompat;
import com.octo.android.robospice.Jackson2GoogleHttpClientSpiceService;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestCancellationListener;
import com.octo.android.robospice.request.listener.RequestListener;


/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 18-10-2013
 * Time: 17:27
 * To change this template use File | Settings | File Templates.
 */
public class AddStoreDialog extends SherlockDialogFragment {
    private SpiceManager spiceManager = new SpiceManager(Jackson2GoogleHttpClientSpiceService.class);

    private String store;

    private Callback callback;
    public Callback dummyCallback = new Callback() {
        @Override
        public void startParse(Store s) {

        }
    };
    private String repoName;
    private String url;
    private GetRepositoryInfoRequest getRepoInfoRequest;
    private CheckServerRequest checkServerRequest;
    private Login login;


    public interface Callback{
        public void startParse(Store store);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        callback = (Callback) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callback = dummyCallback;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (getDialog() != null) {
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }
        return inflater.inflate(R.layout.dialog_add_store, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        spiceManager.start(getSherlockActivity());

        if(url!=null){
            spiceManager.getFromCache(Integer.class, (url+"rc"), DurationInMillis.ONE_MINUTE, new CheckStoreListener(login));
            spiceManager.getFromCache(RepositoryInfoJson.class, (url+"repositoryInfo"), DurationInMillis.ONE_MINUTE, new RepositoryRequestListener(url, login));
        }
    }




    public final class CheckStoreListener implements RequestListener<Integer>, RequestCancellationListener {


        private final Login login;

        public CheckStoreListener(Login login) {
            this.login = login;

        }

        @Override
        public void onRequestFailure(SpiceException spiceException) {


            dismissDialog("Unable to check store " + spiceException);

        }

        @Override
        public void onRequestSuccess(Integer integer) {

            if (integer != null) {

                switch (integer) {
                    case 401:
                        //on401(url);
                        dismissDialog("Private store found");
                        DialogFragment passDialog = AptoideDialog.passwordDialog();
                        passDialog.setTargetFragment(AddStoreDialog.this, 20);
                        passDialog.show(getFragmentManager(), "passDialog");
                        break;
                    case -1:
                        dismissDialog("Invalid Store added.");
                        break;
                    default:

                        if (!url.endsWith(".store.aptoide.com/")) {

                            Store store = new Store();

                            store.setBaseUrl(url);
                            store.setName(url);
                            store.setLogin(login);

                            callback.startParse(store);
                            dismissDialog();

                        } else {
                            Log.i("Aptoide-", "Request:" +(url+"repositoryInfo") );
                            getRepoInfoRequest = new GetRepositoryInfoRequest(repoName);
                            RepositoryRequestListener repositoryRequestListener = new RepositoryRequestListener(url, login);
                            getRepoInfoRequest.setRequestCancellationListener(repositoryRequestListener);
                            spiceManager.execute(getRepoInfoRequest, (url+"repositoryInfo"), DurationInMillis.ONE_MINUTE, repositoryRequestListener);

                        }
                        break;
                }
            }
        }

        @Override
        public void onRequestCancelled() {
            Toast.makeText(getSherlockActivity(), "Request2 was canceled", Toast.LENGTH_LONG).show();

        }
    }

    @Override
    public void onStop() {
        if(spiceManager.isStarted()){
            spiceManager.shouldStop();
        }
        super.onStop();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case 20:
                String username = data.getStringExtra("username");
                String password = data.getStringExtra("password");
                Login login = new Login();
                login.setUsername(username.trim());
                login.setPassword(password.trim());
                get(url, login);
                showDialog();
                break;
        }
    }

    public final class RepositoryRequestListener implements RequestListener<RepositoryInfoJson>, RequestCancellationListener {

        private final String url;
        private final Login login;


        public RepositoryRequestListener(String url, Login login){
            this.url = url;
            this.login = login;
        }

        @Override
        public void onRequestFailure(SpiceException spiceException) {
            dismissDialog("Unable to add store " + spiceException);

        }

        @Override
        public void onRequestSuccess(RepositoryInfoJson repositoryInfoJson) {
            String message = null;

            Log.i("Aptoide-", "success");
            if (repositoryInfoJson != null) {


                if ("FAIL".equals(repositoryInfoJson.getStatus())) {




                    message = "Store doesn't exist.";
                    dismissDialog(message);

                } else {
                    final Store store = new Store();

                    store.setBaseUrl(url);
                    store.setName(repositoryInfoJson.getListing().getName());
                    store.setDownloads(repositoryInfoJson.getListing().getDownloads());
                    store.setAvatar(repositoryInfoJson.getListing().getAvatar());
                    store.setDescription(repositoryInfoJson.getListing().getDescription());
                    store.setTheme(repositoryInfoJson.getListing().getTheme());
                    store.setView(repositoryInfoJson.getListing().getView());
                    store.setItems(repositoryInfoJson.getListing().getItems());
                    store.setLogin(login);

                    dismissDialog();
                    dismiss();
                    callback.startParse(store);

                }
            }
        }

        @Override
        public void onRequestCancelled() {
            Toast.makeText(getSherlockActivity(), "Request was canceled", Toast.LENGTH_LONG).show();
        }
    }

    void dismissDialog(){
        setRetainInstance(false);
        SherlockDialogFragment pd = (SherlockDialogFragment) getFragmentManager().findFragmentByTag("addStoreProgress");
            if(pd!=null)
                pd.dismiss();

    }

    void dismissDialog(String message){
        if(message!=null){
            Toast.makeText(getSherlockActivity(),message, Toast.LENGTH_LONG).show();
        }

        dismissDialog();
    }


    public void get(String s, final Login login) {
        setRetainInstance(true);
        url = AptoideUtils.checkStoreUrl(s);
        this.login = login;
        repoName = AptoideUtils.RepoUtils.split(url);
        checkServerRequest = new CheckServerRequest(url, login);
        CheckStoreListener checkStoreListener = new CheckStoreListener(login);
        checkServerRequest.setRequestCancellationListener(checkStoreListener);
        spiceManager.execute(checkServerRequest, (url+"rc"), DurationInMillis.ONE_MINUTE, checkStoreListener);
        Log.i("Aptoide-", "Request:" +(url+"rc") );

    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.button_dialog_add_store).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                String url = ((EditText)view.findViewById(R.id.edit_store_uri)).getText().toString();
                get(url, null);
                showDialog();
            }
        });
    }

    private void showDialog() {

        ProgressDialogFragment pd = (ProgressDialogFragment) AptoideDialog.pleaseWaitDialog();

        pd.setOnCancelListener(cancelListener);
        pd.show(getFragmentManager(), "addStoreProgress");

    }

    ProgressDialogFragment.OnCancelListener cancelListener = new ProgressDialogFragment.OnCancelListener() {


        @Override
        public void onCancel() {

            Log.i("Aptoide-", "Canceling:" +(url+"rc") );
            Log.i("Aptoide-", "Canceling:" +(url+"repositoryInfo") );

            if(checkServerRequest!=null)checkServerRequest.cancel();
            if(getRepoInfoRequest!=null)getRepoInfoRequest.cancel();
            setRetainInstance(false);
            Toast.makeText(getSherlockActivity(), "Canceled", Toast.LENGTH_LONG).show();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState!=null){
            url = savedInstanceState.getString("url");

            ProgressDialogFragment pd = (ProgressDialogFragment) getFragmentManager().findFragmentByTag("addStoreProgress");
            if(pd!=null){
                pd.setOnCancelListener(cancelListener);
            }
        }
        //setRetainInstance(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("url", url);
    }
}