package cm.aptoide.ptdev.dialogs;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.model.Login;
import cm.aptoide.ptdev.model.ResponseCode;
import cm.aptoide.ptdev.model.Store;
import cm.aptoide.ptdev.services.CheckServerRequest;
import cm.aptoide.ptdev.services.HttpClientSpiceService;
import cm.aptoide.ptdev.utils.AptoideUtils;
import cm.aptoide.ptdev.utils.IconSizes;
import cm.aptoide.ptdev.webservices.GetRepositoryInfoRequest;
import cm.aptoide.ptdev.webservices.json.RepositoryInfoJson;
import com.octo.android.robospice.Jackson2GoogleHttpClientSpiceService;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.CachedSpiceRequest;
import com.octo.android.robospice.request.listener.PendingRequestListener;
import com.octo.android.robospice.request.listener.RequestCancellationListener;
import com.octo.android.robospice.request.listener.RequestListener;


/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 18-10-2013
 * Time: 17:27
 * To change this template use File | Settings | File Templates.
 */
public class AddStoreDialog extends DialogFragment {
    private SpiceManager spiceManager = new SpiceManager(HttpClientSpiceService.class);



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
//            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            getDialog().getWindow().setTitle(getString(R.string.add_store));

        }
        return inflater.inflate(R.layout.dialog_add_store, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        spiceManager.start(getActivity());

        if(url!=null){
            spiceManager.addListenerIfPending(ResponseCode.class, (url+"rc"),new CheckStoreListener(login));
            //spiceManager.getFromCache(ResponseCode.class, (url+"rc"), DurationInMillis.ONE_MINUTE, new CheckStoreListener(login));
        }

    }




    public final class CheckStoreListener implements RequestListener<ResponseCode>, PendingRequestListener<ResponseCode> {


        private final Login login;

        public CheckStoreListener(Login login) {
            this.login = login;

        }

        @Override
        public void onRequestFailure(SpiceException spiceException) {
            dismissDialog();
            Toast.makeText(getActivity(), R.string.error_occured, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onRequestSuccess(ResponseCode integer) {

            if (integer != null) {

                switch (integer.responseCode) {
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

                            Store store = new Store();
                            store.setLogin(login);
                            store.setBaseUrl(url);
                            store.setName(AptoideUtils.RepoUtils.split(url));
                            callback.startParse(store);
                            dismissDialog();
                            dismiss();
                        }
                        break;
                }
            }
        }


        @Override
        public void onRequestNotFound() {

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
            dismissDialog("Unable to add store.");
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

                    store.setName(repositoryInfoJson.getListing().getName());
                    store.setDownloads(repositoryInfoJson.getListing().getDownloads());


                    if(repositoryInfoJson.getListing().getAvatar_hd()!=null){

                        String sizeString = IconSizes.generateSizeStringAvatar(getActivity());


                        String avatar = repositoryInfoJson.getListing().getAvatar_hd();
                        String[] splittedUrl = avatar.split("\\.(?=[^\\.]+$)");
                        avatar = splittedUrl[0] + "_" + sizeString + "."+ splittedUrl[1];

                        store.setAvatar(avatar);

                    }else{
                        store.setAvatar(repositoryInfoJson.getListing().getAvatar());
                    }

                    store.setDescription(repositoryInfoJson.getListing().getDescription());
                    store.setTheme(repositoryInfoJson.getListing().getTheme());
                    store.setView(repositoryInfoJson.getListing().getView());
                    store.setItems(repositoryInfoJson.getListing().getItems());



                    callback.startParse(store);

                }
            }
        }

        @Override
        public void onRequestCancelled() {
            Toast.makeText(getActivity(), "Request was canceled", Toast.LENGTH_LONG).show();
        }
    }

    void dismissDialog(){
        setRetainInstance(false);
        DialogFragment pd = (DialogFragment) getFragmentManager().findFragmentByTag("addStoreProgress");
            if(pd!=null)
                pd.dismissAllowingStateLoss();

    }

    void dismissDialog(String message){
        if(message!=null){
            Toast.makeText(getActivity(),message, Toast.LENGTH_LONG).show();
        }

        dismissDialog();
    }


    public void get(String s, final Login login) {

        url = AptoideUtils.checkStoreUrl(s);
        this.login = login;
        repoName = AptoideUtils.RepoUtils.split(url);
        checkServerRequest = new CheckServerRequest(url, login);
        CheckStoreListener checkStoreListener = new CheckStoreListener(login);
        setRetainInstance(true);
        spiceManager.execute(checkServerRequest, checkStoreListener);

        checkServerRequest.setRequestCancellationListener(new RequestCancellationListener() {
            @Override
            public void onRequestCancelled() {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), "Request2 was canceled", Toast.LENGTH_LONG).show();
                    }
                });

            }
        });

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

        view.findViewById(R.id.button_top_stores).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("http://m.aptoide.com/more/toprepos");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                dismiss();

                startActivity(intent);
            }
        });
    }

    private void showDialog() {

        AptoideDialog.pleaseWaitDialog().show(getFragmentManager(), "addStoreProgress");

    }

    public ProgressDialogFragment.OnCancelListener cancelListener = new ProgressDialogFragment.OnCancelListener() {

        @Override
        public void onCancel() {

            Log.i("Aptoide-", "Canceling:" +(url+"rc") );
            Log.i("Aptoide-", "Canceling:" + (url + "repositoryInfo"));

            if(checkServerRequest!=null){
                checkServerRequest.cancel();
            }


            //Toast.makeText(getActivity(), "Canceled", Toast.LENGTH_LONG).show();

        }

    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState!=null){
            url = savedInstanceState.getString("url");

            ProgressDialogFragment pd = (ProgressDialogFragment) getFragmentManager().findFragmentByTag("addStoreProgress");

        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("url", url);
    }
}
