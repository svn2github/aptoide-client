package cm.aptoide.ptdev.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import cm.aptoide.ptdev.AppViewActivity;
import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.InstalledApkEvent;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.Start;
import cm.aptoide.ptdev.UnInstalledApkEvent;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.events.BusProvider;
import cm.aptoide.ptdev.events.RepoAddedEvent;
import cm.aptoide.ptdev.fragments.callbacks.RepoCompleteEvent;
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
    private RequestListener<UpdatesResponse> requestListener;
    private cm.aptoide.ptdev.widget.RecyclerView viewById;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        BusProvider.getInstance().register(this);
        manager.start(activity);
    }


    @Override
    public void onDetach() {
        super.onDetach();
        BusProvider.getInstance().unregister(this);
        manager.shouldStop();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list_apps, container, false);

    }

    public static class UpdatesResponse{

        public Response.Info info;
        public Response.Ticket ticket;
        public Response.Data<UpdateApk> data;

        public static class UpdateApk extends Response.ListApps.Apk{

            public Apk apk;
            public int vercode;

            public static class Apk{
                public String path;
                public String path_alt;

                public Number filesize;
            }

        }

    }

    public static class UpdatesApi{
        public List<String> store_names = new ArrayList<>();
        public List<Package> apks_data = new ArrayList<>();

        public String q = AptoideUtils.filters(Aptoide.getContext());
        public boolean mature;


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


            api.mature = !PreferenceManager.getDefaultSharedPreferences(Aptoide.getContext()).getBoolean("matureChkBox", true);

            //aPackage.packageName = "cm.aptoide.pt";
            //aPackage.vercode = 447;
            //aPackage.signature = "D5:90:A7:D7:92:FD:03:31:54:2D:99:FA:F9:99:76:41:79:07:73:A9";

            Database database = new Database(Aptoide.getDb());

            Cursor servers = database.getServers();

            for(servers.moveToFirst(); !servers.isAfterLast(); servers.moveToNext()){
                api.store_names.add(servers.getString(servers.getColumnIndex("name")));
            }


            servers.close();


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

    public void setLoading(View view){
        view.findViewById(R.id.please_wait).setVisibility(View.VISIBLE);
        view.findViewById(R.id.list).setVisibility(View.GONE);
        view.findViewById(R.id.error).setVisibility(View.GONE);

    }

    private void setError(final View view, final SpiceManager manager, final RequestListener requestListener){
        view.findViewById(R.id.error).setVisibility(View.VISIBLE);
        view.findViewById(R.id.swipe_container).setVisibility(View.GONE);
        view.findViewById(R.id.empty).setVisibility(View.GONE);
        view.findViewById(R.id.retry).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLoading(view);
                manager.execute(request, requestListener);

            }
        });
    }



    private void setUINoUpdates(View view){
        TextView tv = (TextView)view.findViewById(R.id.empty);
        tv.setVisibility(View.VISIBLE);
        tv.setText(R.string.no_updates);
        view.findViewById(R.id.please_wait).setVisibility(View.GONE);
        view.findViewById(R.id.swipe_container).setVisibility(View.GONE);
        view.findViewById(R.id.error).setVisibility(View.GONE);

    }

    final UpdatesRequest request = new UpdatesRequest();

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewById = (cm.aptoide.ptdev.widget.RecyclerView) view.findViewById(R.id.list);

        viewById.setLayoutManager(new LinearLayoutManager(view.getContext()));

        final SwipeRefreshLayout layout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);


        final ArrayList<UpdatesResponse.UpdateApk> list = new ArrayList<>();
        viewById.setAdapter(new UpdatesRecyclerView(list));


        setLoading(view);

        requestListener = new RequestListener<UpdatesResponse>() {

            @Override
            public void onRequestFailure(SpiceException spiceException) {
                Log.d("pois","onRequestFailure");
                setError(view, manager, requestListener);
                layout.setRefreshing(false);
            }

            @Override
            public void onRequestSuccess(UpdatesResponse updatesResponse) {
                Log.d("pois","onRequestSuccess");
                registerForContextMenu(viewById);
                if(updatesResponse==null || updatesResponse.data==null){
                    setError(view, manager, requestListener);
                    return;
                }

                list.clear();
                ArrayList<String> excludedPackages = new ArrayList<>();
                Database database = new Database(Aptoide.getDb());
                Cursor c = database.getExcludedApks();

                for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                    excludedPackages.add(c.getString(c.getColumnIndex("package_name")));
                }

                c.close();

                list.add(new UpdatesResponse.UpdateApk());

                try {

                    for (UpdatesResponse.UpdateApk updateApk : updatesResponse.data.list) {

                        if (!excludedPackages.contains(updateApk.packageName)) {
                            list.add(updateApk);

                        }

                    }

                    if(list.size()>1){
                        viewById.getAdapter().notifyDataSetChanged();
                        view.findViewById(R.id.please_wait).setVisibility(View.GONE);
                        view.findViewById(R.id.error).setVisibility(View.GONE);
                        view.findViewById(R.id.list).setVisibility(View.VISIBLE);
                    }else{
                        setUINoUpdates(view);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    setError(view, manager, requestListener);
                }

                layout.setRefreshing(false);

            }
        };
        manager.execute(request, requestListener);

        layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                manager.execute(request, requestListener);
            }
        });

    }

    @Subscribe
    public void repoAddedEvent(RepoAddedEvent event){
        refreshStoresEvent(null);
    }

    @Subscribe
    public void newAppEvent(InstalledApkEvent event) {
        refreshStoresEvent(null);
    }

    @Subscribe
    public void removedAppEvent(UnInstalledApkEvent event) {
        refreshStoresEvent(null);
    }

    @Subscribe
    public void refreshStoresEvent(RepoCompleteEvent event) {



        manager.execute(request, requestListener);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        MenuInflater inflater = this.getActivity().getMenuInflater();
        inflater.inflate(R.menu.menu_updates_context, menu);

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {


        cm.aptoide.ptdev.widget.RecyclerView.RecyclerContextMenuInfo info = (cm.aptoide.ptdev.widget.RecyclerView.RecyclerContextMenuInfo) item.getMenuInfo();

        int position = info.position;

        UpdatesResponse.UpdateApk updateApk = ((UpdatesRecyclerView) viewById.getAdapter()).packageList.get(position);
        Database database = new Database(Aptoide.getDb());
        database.addToExcludeUpdate(updateApk);



        manager.execute(request, requestListener);

        return super.onContextItemSelected(item);
    }

    public static class UpdatesRecyclerView extends RecyclerView.Adapter<UpdatesRecyclerView.UpdatesViewHolder>{


        List<UpdatesResponse.UpdateApk> packageList;

        public UpdatesRecyclerView(List<UpdatesResponse.UpdateApk> packageList) {
            this.packageList = packageList;
        }

        @Override
        public UpdatesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = null;
            switch (viewType){
                case 0:
                    v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_app_update, parent, false);
                    break;
                case 1:
                    v = LayoutInflater.from(parent.getContext()).inflate(R.layout.separator_updates, parent, false);
                    break;
            }

            return new UpdatesViewHolder(v, viewType);
        }

        @Override
        public void onBindViewHolder(final UpdatesViewHolder holder, final int position) {

            switch (getItemViewType(position)) {
                case 0:


                final UpdatesResponse.UpdateApk item = packageList.get(position);
                holder.appName.setText(Html.fromHtml(item.name).toString());
                String icon1 = item.icon;
                holder.versionName.setText(item.vername);

                if (icon1.contains("_icon")) {
                    String[] splittedUrl = icon1.split("\\.(?=[^\\.]+$)");
                    icon1 = splittedUrl[0] + "_" + Aptoide.iconSize + "." + splittedUrl[1];
                }

                ImageLoader.getInstance().displayImage(icon1, holder.appIcon);

                holder.itemView.setLongClickable(true);

                holder.manageIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Toast.makeText(holder.itemView.getContext(), holder.itemView.getContext().getString(R.string.starting_download), Toast.LENGTH_LONG).show();
                        ((Start)holder.itemView.getContext()).installApp(packageList.get(position));

                    }
                });
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent i = new Intent(v.getContext(), AppViewActivity.class);
                        i.putExtra("fromRelated", true);
                        i.putExtra("md5sum", item.md5sum);
                        i.putExtra("repoName", item.store_name);
                        i.putExtra("download_from", "recommended_apps");
                        v.getContext().startActivity(i);

                    }
                });
                    break;

                case 1:

                    holder.more.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ((Start)holder.itemView.getContext()).installApp(packageList);
                            Toast.makeText(holder.itemView.getContext(), holder.itemView.getContext().getString(R.string.starting_download), Toast.LENGTH_LONG).show();

                        }
                    });

                    holder.label.setText(holder.itemView.getContext().getString(R.string.updates_tab));

                    break;
            }


        }


        @Override
        public int getItemViewType(int position) {

            if(position == 0){
                return 1;
            }

            return super.getItemViewType(position);
        }

        @Override
        public int getItemCount() {
            return packageList.size();
        }

        public static class UpdatesViewHolder extends RecyclerView.ViewHolder{
            TextView label;
            View more;
            ImageView appIcon;
            ImageView manageIcon;
            TextView appName;
            TextView versionName;
            TextView notsafe;
            public UpdatesViewHolder(View v, int viewType) {
                super(v);

                switch (viewType){

                    case 0:

                        appIcon = (ImageView) v.findViewById(R.id.app_icon);
                        manageIcon = (ImageView) v.findViewById(R.id.manage_icon);
                        appName = (TextView) v.findViewById(R.id.app_name);
                        versionName = (TextView) v.findViewById(R.id.app_version);

                        break;

                    case 1:
                        more = v.findViewById(R.id.more);
                        label = (TextView) v.findViewById(R.id.separator_label);

                        break;

                }


            }
        }


    }

}
