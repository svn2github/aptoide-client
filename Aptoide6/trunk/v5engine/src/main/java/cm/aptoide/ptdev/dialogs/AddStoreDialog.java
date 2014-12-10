package cm.aptoide.ptdev.dialogs;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;

import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.downloadmanager.Utils;
import cm.aptoide.ptdev.events.BusProvider;
import cm.aptoide.ptdev.events.RepoAddedEvent;
import cm.aptoide.ptdev.model.Login;
import cm.aptoide.ptdev.model.Store;
import cm.aptoide.ptdev.services.CheckServerRequest;
import cm.aptoide.ptdev.services.HttpClientSpiceService;
import cm.aptoide.ptdev.utils.AptoideUtils;
import cm.aptoide.ptdev.utils.IconSizes;
import cm.aptoide.ptdev.webservices.Api;
import cm.aptoide.ptdev.webservices.Response;
import retrofit.http.Body;
import retrofit.http.POST;


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
            //spiceManager.addListenerIfPending(ResponseCode.class, (url+"rc"),new CheckStoreListener(login));
            //spiceManager.getFromCache(ResponseCode.class, (url+"rc"), DurationInMillis.ONE_MINUTE, new CheckStoreListener(login));
        }

    }




    public final class CheckStoreListener implements RequestListener<Response.GetStore> {


        private final Login login;

        public CheckStoreListener(Login login) {
            this.login = login;

        }

        @Override
        public void onRequestFailure(SpiceException spiceException) {
            dismissDialog();
        }

        @Override
        public void onRequestSuccess(Response.GetStore response) {

            try{

                final Store store = new Store();
                Response.GetStore.StoreMetaData data = response.datasets.meta.data;
                store.setId(data.id.longValue());
                store.setName(response.datasets.meta.data.name);
                store.setDownloads(response.datasets.meta.data.downloads.intValue() + "");


                String sizeString = IconSizes.generateSizeStringAvatar(getActivity());


                String avatar = data.avatar;

                if(avatar!=null) {
                    String[] splittedUrl = avatar.split("\\.(?=[^\\.]+$)");
                    avatar = splittedUrl[0] + "_" + sizeString + "." + splittedUrl[1];
                }

                store.setAvatar(avatar);
                store.setDescription(data.description);
                store.setTheme(data.theme);
                store.setView(data.view);
                store.setBaseUrl(data.name);

                Database database = new Database(Aptoide.getDb());

                database.insertStore(store);
                database.updateStore(store);

                BusProvider.getInstance().post(new RepoAddedEvent());
                dismissDialog();
                dismiss();

            }catch (Exception e){
                Toast.makeText(Aptoide.getContext(), R.string.error_occured, Toast.LENGTH_LONG).show();
                dismissDialog();

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



    void dismissDialog(){
        setRetainInstance(false);
        DialogFragment pd = (DialogFragment) getFragmentManager().findFragmentByTag("addStoreProgress");
        if(pd!=null)
            pd.dismissAllowingStateLoss();

    }

    void dismissDialog(String message){
        if(message!=null){
            //Toast.makeText(getActivity(),message, Toast.LENGTH_LONG).show();
        }

        dismissDialog();
    }

    public interface TestServerWebservice{
        @POST("/ws2.aptoide.com/api/6/getStore")
        Response.GetStore checkServer(@Body Api.GetStore body);
    }

    public class TestServerRequest extends RetrofitSpiceRequest<Response.GetStore, TestServerWebservice>{


        private String store_name;
        public TestServerRequest() {
            super(Response.GetStore.class, TestServerWebservice.class);
        }

        public void setStore_name(String store_name){

            this.store_name = store_name;
        }

        @Override
        public Response.GetStore loadDataFromNetwork() throws Exception {

            Api.GetStore api = new Api.GetStore();
            api.addDataset("meta");
            api.datasets_params = null;
            api.store_name = store_name;

            return getService().checkServer(api);
        }
    }

    public void get(String s, final Login login) {


        TestServerRequest request = new TestServerRequest();
        CheckStoreListener checkStoreListener = new CheckStoreListener(login);

        url = AptoideUtils.checkStoreUrl(s);
//        this.login = login;

        repoName = AptoideUtils.RepoUtils.split(url);
        request.setStore_name(repoName);
        spiceManager.execute(request, checkStoreListener);

//        checkServerRequest = new CheckServerRequest(url, login);

//        setRetainInstance(true);
//        spiceManager.execute(checkServerRequest, checkStoreListener);
//
//        checkServerRequest.setRequestCancellationListener(new RequestCancellationListener() {
//            @Override
//            public void onRequestCancelled() {
//                Handler handler = new Handler(Looper.getMainLooper());
//                handler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText(getActivity(), "Request2 was canceled", Toast.LENGTH_LONG).show();
//                    }
//                });
//
//            }
//        });

        Log.i("Aptoide-", "Request:" +(url+"rc") );

    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.button_dialog_add_store).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FlurryAgent.logEvent("Store_View_Dialog_Clicked_Add_Store");
                String url = ((EditText)view.findViewById(R.id.edit_store_uri)).getText().toString();
                if(url!=null&&url.length()>0){
                    get(url, null);
                    showDialog();
                }
            }
        });

        view.findViewById(R.id.button_top_stores).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FlurryAgent.logEvent("Store_View_Dialog_Clicked_See_Top_Stores");
                Uri uri = Uri.parse("http://m.aptoide.com/more/toprepos/q=" + Utils.filters(getActivity()));
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                if(isAdded())dismiss();
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
