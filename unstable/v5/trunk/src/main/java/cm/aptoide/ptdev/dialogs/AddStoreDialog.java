package cm.aptoide.ptdev.dialogs;

import android.app.Activity;
import android.os.Bundle;
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
import com.octo.android.robospice.Jackson2GoogleHttpClientSpiceService;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
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
        public void startParse(Store s, Login login) {

        }
    };
    private String repoName;
    private String url;


    public interface Callback{
        public void startParse(Store store, Login login);
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
            spiceManager.getFromCache(Integer.class, (url+"rc").hashCode(), DurationInMillis.ONE_MINUTE, new CheckStoreListener());
            spiceManager.getFromCache(RepositoryInfoJson.class, (url+"repositoryInfo").hashCode(), DurationInMillis.ONE_MINUTE, new RepositoryRequestListener(url, login));
        }
    }



    private Login login;
    public final class CheckStoreListener implements RequestListener<Integer> {




        @Override
        public void onRequestFailure(SpiceException spiceException) {
            dismissDialog("Unable to check store");

        }

        @Override
        public void onRequestSuccess(Integer integer) {

            if (integer != null) {

                switch (integer) {
                    case 401:
                        //on401(url);
                        dismissDialog("Private store found");

                        break;
                    case -1:
                        dismissDialog("Invalid Store added.");
                        break;
                    default:

                        if (!url.endsWith(".store.aptoide.com/")) {

                            Store store = new Store();

                            store.setBaseUrl(url);
                            store.setName(url);

                            callback.startParse( store, login);

                        } else {

                            spiceManager.execute(new GetRepositoryInfoRequest(repoName), (url+"repositoryInfo").hashCode(), DurationInMillis.ONE_MINUTE, new RepositoryRequestListener(url, login));

                        }
                        break;
                }
            }
        }

    }

    @Override
    public void onStop() {
        if(spiceManager.isStarted()){
            spiceManager.shouldStop();
        }
        super.onStop();

    }

    public final class RepositoryRequestListener implements RequestListener<RepositoryInfoJson> {

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

            Log.d("Aptoide-", "success");
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


                    dismissDialog();
                    dismiss();
                    callback.startParse(store, login);

                }
            }
        }
    }

    void dismissDialog(){
        SherlockDialogFragment pd = (SherlockDialogFragment) getFragmentManager().findFragmentByTag("addStoreProgress");
            if(pd!=null)
                pd.dismiss();

    }

    void dismissDialog(String message){
        if(message!=null){
            Toast.makeText(getSherlockActivity(),message, Toast.LENGTH_LONG).show();
        }
        SherlockDialogFragment pd = (SherlockDialogFragment) getFragmentManager().findFragmentByTag("addStoreProgress");
        if(pd!=null)
            pd.dismiss();
    }


    public void get(String s, final Login login) {

        url = AptoideUtils.checkStoreUrl(s);
        repoName = AptoideUtils.RepoUtils.split(url);
        spiceManager.execute(new CheckServerRequest(url, login), (url+"rc").hashCode(), DurationInMillis.ONE_MINUTE, new CheckStoreListener());
        Log.d("Aptoide-", "Request:" +(url+"rc").hashCode() );

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

            Log.d("Aptoide-", "Canceling:" +(url+"rc").hashCode() );
            Log.d("Aptoide-", "Canceling:" +(url+"repositoryInfo").hashCode() );

            spiceManager.cancel(Integer.class, (url+"rc").hashCode());
            spiceManager.cancel(RepositoryInfoJson.class, (url+"repositoryInfo").hashCode());
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
