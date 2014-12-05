package cm.aptoide.ptdev.fragments;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.services.HttpClientSpiceService;
import cm.aptoide.ptdev.utils.AptoideUtils;
import cm.aptoide.ptdev.webservices.Response;
import retrofit.http.Body;
import retrofit.http.POST;

/**
 * Created by rmateus on 04-12-2014.
 */
public class FragmentUpdates2 extends Fragment {


    SpiceManager manager = new SpiceManager(HttpClientSpiceService.class);


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        manager.start(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        manager.shouldStop();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list_apps, container, false);

    }





    public static class UpdatesResponse{

        public Response.Info info;
        public Response.Data<UpdateApk> data;


        public static class UpdateApk extends Response.ListApps.Apk{

            public Apk apk;

            public static class Apk{
                public String path;
                public String filesize;
            }

        }

    }

    public static class UpdatesApi{
        public List<String> store_names = new ArrayList<>();
        public List<Package> apks_data = new ArrayList<>();

        public static class Package{
            @JsonProperty("package")
            public String packageName;

            public Number vercode;
            public String signature;
        }
    }


    public interface Webservice{
        @POST("/ws2.aptoide.com/api/6/listAppsUpdates")
        UpdatesResponse getUpdates(@Body UpdatesApi api );
    }


    public static class UpdatesRequest extends RetrofitSpiceRequest<UpdatesResponse, Webservice>{


        public UpdatesRequest() {
            super(UpdatesResponse.class, Webservice.class);
        }

        @Override
        public UpdatesResponse loadDataFromNetwork() throws Exception {

            UpdatesApi api = new UpdatesApi();

            //aPackage.packageName = "cm.aptoide.pt";
            //aPackage.vercode = 447;
            //aPackage.signature = "D5:90:A7:D7:92:FD:03:31:54:2D:99:FA:F9:99:76:41:79:07:73:A9";

            api.store_names.add("apps");
            PackageManager packageManager = Aptoide.getContext().getPackageManager();
            List<PackageInfo> installedPackages = packageManager.getInstalledPackages(PackageManager.GET_SIGNATURES);

            for(PackageInfo anInstalledPackage: installedPackages){
                UpdatesApi.Package aPackage = new UpdatesApi.Package();

                aPackage.signature = AptoideUtils.Algorithms.computeSHA1sumFromBytes(anInstalledPackage.signatures[0].toByteArray()).toUpperCase(Locale.ENGLISH);
                aPackage.vercode = anInstalledPackage.versionCode;
                aPackage.packageName = anInstalledPackage.packageName;

                api.apks_data.add(aPackage);
            }



            return getService().getUpdates(api);
        }
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final cm.aptoide.ptdev.widget.RecyclerView viewById = (cm.aptoide.ptdev.widget.RecyclerView) view.findViewById(R.id.list);

        viewById.setLayoutManager(new LinearLayoutManager(view.getContext()));

        final ArrayList<UpdatesResponse.UpdateApk> list = new ArrayList<>();

        viewById.setAdapter(new UpdatesRecyclerView(list));

        UpdatesRequest request = new UpdatesRequest();

        manager.execute(request, new RequestListener<UpdatesResponse>() {
            @Override
            public void onRequestFailure(SpiceException spiceException) {

            }

            @Override
            public void onRequestSuccess(UpdatesResponse updatesResponse) {
                list.clear();
                list.addAll(updatesResponse.data.list);
                viewById.getAdapter().notifyDataSetChanged();
            }
        });

    }


    public static class UpdatesRecyclerView extends RecyclerView.Adapter<UpdatesRecyclerView.UpdatesViewHolder>{


        List<UpdatesResponse.UpdateApk> packageList;

        public UpdatesRecyclerView(List<UpdatesResponse.UpdateApk> packageList) {
            this.packageList = packageList;
        }

        @Override
        public UpdatesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_app_update, parent, false);

            return new UpdatesViewHolder(v);
        }

        @Override
        public void onBindViewHolder(UpdatesViewHolder holder, int position) {

            UpdatesResponse.UpdateApk item = packageList.get(position);
            holder.appName.setText(Html.fromHtml(item.name).toString());
            String icon1 = item.icon;
            holder.versionName.setText(item.vername);

            if(icon1.contains("_icon")){
                String[] splittedUrl = icon1.split("\\.(?=[^\\.]+$)");
                icon1 = splittedUrl[0] + "_" + "_96x96" + "."+ splittedUrl[1];
            }

            ImageLoader.getInstance().displayImage(icon1, holder.appIcon);

        }

        @Override
        public int getItemCount() {
            return packageList.size();
        }

        public static class UpdatesViewHolder extends RecyclerView.ViewHolder{
            ImageView appIcon;
            ImageView manageIcon;
            TextView appName;
            TextView versionName;
            TextView notsafe;
            public UpdatesViewHolder(View v) {
                super(v);

                appIcon = (ImageView) v.findViewById(R.id.app_icon);
                manageIcon = (ImageView) v.findViewById(R.id.manage_icon);
                appName = (TextView) v.findViewById(R.id.app_name);
                versionName = (TextView) v.findViewById(R.id.app_version);
            }
        }


    }

}
