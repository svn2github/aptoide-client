package cm.aptoide.ptdev.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;
import com.squareup.otto.Subscribe;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

import cm.aptoide.ptdev.AppViewActivity;
import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.InstalledApkEvent;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.Start;
import cm.aptoide.ptdev.UnInstalledApkEvent;
import cm.aptoide.ptdev.UninstallRetainFragment;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.database.schema.Schema;
import cm.aptoide.ptdev.events.BusProvider;
import cm.aptoide.ptdev.events.RepoAddedEvent;
import cm.aptoide.ptdev.fragments.callbacks.RepoCompleteEvent;
import cm.aptoide.ptdev.services.HttpClientSpiceService;
import cm.aptoide.ptdev.services.UpdatesService;
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
    private SwipeRefreshLayout layout;
    private View fullLayout;


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

        public static class UpdateApk extends Response.ListApps.Apk implements UpdateDisplayable{

            public ApplicationInfo info;

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
    final ArrayList<UpdateDisplayable> list = new ArrayList<>();

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fullLayout = view;
        viewById = (cm.aptoide.ptdev.widget.RecyclerView) view.findViewById(R.id.list);

        viewById.setLayoutManager(new LinearLayoutManager(view.getContext()));

        layout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);

        viewById.setAdapter(new UpdatesRecyclerView(view.getContext(), list));


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
                Log.d("pois", "onRequestSuccess");
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

                //list.add(new UpdatesResponse.UpdateApk());

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
                        view.findViewById(R.id.swipe_container).setVisibility(View.VISIBLE);
                        view.findViewById(R.id.list).setVisibility(View.VISIBLE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    setError(view, manager, requestListener);
                }

                layout.setRefreshing(false);

            }
        };

        registerForContextMenu(viewById);

        refreshUi(view, list);

        Aptoide.getContext().startService(new Intent(Aptoide.getContext(), UpdatesService.class));

        layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Database database = new Database(Aptoide.getDb());
                database.invalidateUpdates();
                Intent intent = new Intent(Aptoide.getContext(), UpdatesService.class);
                intent.putExtra("force", true);
                Aptoide.getContext().startService(intent);
            }
        });

    }

    private void refreshUi(final View view, final ArrayList<UpdateDisplayable> list) {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {

                Database database = new Database(Aptoide.getDb());
                Cursor updatesTabList = database.getUpdatesTabList();
                final PackageManager pm = view.getContext().getPackageManager();
                final ArrayList<UpdatesResponse.UpdateApk> updatesToAdd = new ArrayList<>();
                final ArrayList<InstalledApp> installsToAdd = new ArrayList<>();

                //listtoAdd.add(new UpdatesResponse.UpdateApk());

                Cursor c = database.getExcludedApks();
                ArrayList<String> excludedPackages = new ArrayList<>();

                for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                    excludedPackages.add(c.getString(c.getColumnIndex("package_name")));
                }

                c.close();

                for(updatesTabList.moveToFirst(); !updatesTabList.isAfterLast(); updatesTabList.moveToNext()){

                    UpdatesResponse.UpdateApk apk = new UpdatesResponse.UpdateApk();

                    int package_name = updatesTabList.getColumnIndex("package_name");
                    int filesize = updatesTabList.getColumnIndex("filesize");
                    int alt_path = updatesTabList.getColumnIndex("alt_url");
                    int path = updatesTabList.getColumnIndex("url");
                    int icon = updatesTabList.getColumnIndex("icon");
                    int vername = updatesTabList.getColumnIndex("update_vername");
                    int md5sum = updatesTabList.getColumnIndex("md5");
                    int repo = updatesTabList.getColumnIndex(Schema.Updates.COLUMN_REPO);

                    String path_url = updatesTabList.getString(path);

                    if(path_url!=null) {
                        apk.packageName = updatesTabList.getString(package_name);
                        try {
                            apk.name = (String) pm.getPackageInfo(apk.packageName, 0).applicationInfo.loadLabel(pm);
                            try {
                                PackageInfo packageInfo = pm.getPackageInfo(updatesTabList.getString(package_name), 0);
                                apk.info = packageInfo.applicationInfo;
                            } catch (PackageManager.NameNotFoundException e) {
                                e.printStackTrace();
                            }
                            apk.icon = updatesTabList.getString(icon);
                            apk.md5sum = updatesTabList.getString(md5sum);
                            apk.store_name = updatesTabList.getString(repo);
                            apk.apk = new UpdatesResponse.UpdateApk.Apk();
                            apk.apk.filesize = updatesTabList.getInt(filesize);
                            apk.apk.path_alt = updatesTabList.getString(alt_path);
                            apk.apk.path = updatesTabList.getString(path);
                            apk.vercode = updatesTabList.getInt(updatesTabList.getColumnIndex(Schema.Updates.COLUMN_UPDATE_VERCODE));

                            String string = updatesTabList.getString(vername);
                            if(string == null){
                                apk.vername =  pm.getPackageInfo(apk.packageName, 0).versionName;
                            }else{
                                apk.vername = string;
                            }

                            if (!excludedPackages.contains(apk.packageName)) {
                                updatesToAdd.add(apk);
                            }

                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        }

                    } else {

                        try {
                            PackageInfo packageInfo = pm.getPackageInfo(updatesTabList.getString(package_name), 0);
                            ApplicationInfo ai = packageInfo.applicationInfo;

                            if ((ai.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                                InstalledApp app = new InstalledApp(packageInfo);
                                installsToAdd.add(app);
                            }

                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        }

                    }

                    Collections.sort(installsToAdd, new Comparator<InstalledApp>() {
                        @Override
                        public int compare(InstalledApp lhs, InstalledApp rhs) {

                            DateTime lhsTime = new DateTime(lhs.getPackageInfo().firstInstallTime);
                            DateTime rhsTime = new DateTime(rhs.getPackageInfo().firstInstallTime);
                            try {
                                return rhsTime.compareTo(lhsTime);
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                    });

                    Collections.sort(updatesToAdd, new Comparator<UpdatesResponse.UpdateApk>() {
                        @Override
                        public int compare(UpdatesResponse.UpdateApk lhs, UpdatesResponse.UpdateApk rhs) {
                            return lhs.name.compareToIgnoreCase(rhs.name);
                        }
                    });


                }
                Handler handler = new Handler(Looper.getMainLooper());

                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        if(isAdded()) {
                            layout.setRefreshing(false);

                            list.clear();

                            if (!updatesToAdd.isEmpty()) {
                                UpdatesHeader updatesHeader = new UpdatesHeader();
                                updatesHeader.label = getString(R.string.updates_tab);
                                updatesHeader.type = "updates";
                                list.add(updatesHeader);
                                list.addAll(updatesToAdd);
                            }

                            if (!installsToAdd.isEmpty()) {
                                UpdatesHeader updatesHeader = new UpdatesHeader();
                                updatesHeader.label = getString(R.string.installed_tab);
                                updatesHeader.type = "installed";
                                list.add(updatesHeader);
                                list.addAll(installsToAdd);
                            }

                            viewById.getAdapter().notifyDataSetChanged();

                            if (!list.isEmpty()) {
                                viewById.getAdapter().notifyDataSetChanged();
                                view.findViewById(R.id.please_wait).setVisibility(View.GONE);
                                view.findViewById(R.id.empty).setVisibility(View.GONE);
                                view.findViewById(R.id.error).setVisibility(View.GONE);
                                view.findViewById(R.id.list).setVisibility(View.VISIBLE);
                            }
                        }
                    }
                });

            }
        });
    }

    @Subscribe
    public void repoAddedEvent(RepoAddedEvent event){
        refreshStoresEvent(null);
    }

    @Subscribe
    public void newAppEvent(InstalledApkEvent event) {
        Aptoide.getContext().startService(new Intent(Aptoide.getContext(), UpdatesService.class));
        refreshUi(fullLayout, list);
    }

    @Subscribe
    public void removedAppEvent(UnInstalledApkEvent event) {
        refreshUi(fullLayout, list);
        layout.setRefreshing(false);
    }

    @Subscribe
    public void refreshStoresEvent(RepoCompleteEvent event) {
        Database database = new Database(Aptoide.getDb());
        Aptoide.getContext().startService(new Intent(Aptoide.getContext(), UpdatesService.class));

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        MenuInflater inflater = this.getActivity().getMenuInflater();

        cm.aptoide.ptdev.widget.RecyclerView.RecyclerContextMenuInfo info = (cm.aptoide.ptdev.widget.RecyclerView.RecyclerContextMenuInfo) menuInfo;

        switch (( viewById.getAdapter()).getItemViewType(info.position)) {

            case 0:
                inflater.inflate(R.menu.menu_updates_context, menu);
                break;

            case 2:
                inflater.inflate(R.menu.menu_installed_context, menu);
                break;
        }




    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {


        cm.aptoide.ptdev.widget.RecyclerView.RecyclerContextMenuInfo info = (cm.aptoide.ptdev.widget.RecyclerView.RecyclerContextMenuInfo) item.getMenuInfo();

        int position = info.position;

        switch (((UpdatesRecyclerView) viewById.getAdapter()).getItemViewType(position)){
            case 0:

                UpdatesResponse.UpdateApk updateApk = (UpdatesResponse.UpdateApk) ((UpdatesRecyclerView) viewById.getAdapter()).packageList.get(position);
                Database database = new Database(Aptoide.getDb());
                database.addToExcludeUpdate(updateApk);
                break;
            case 2:

                UninstallRetainFragment uninstallRetainFragment = new UninstallRetainFragment();
                Bundle arg = new Bundle();
                InstalledApp installedApp = (InstalledApp) ((UpdatesRecyclerView) viewById.getAdapter()).packageList.get(position);

                arg.putString("name", (String) installedApp.loadLabel(getActivity().getPackageManager()));
                arg.putString( "package",  installedApp.packageInfo.packageName);
                arg.putString( "version",  installedApp.packageInfo.versionName);
                arg.putString( "icon",  "");


                uninstallRetainFragment.setArguments( arg );
                getFragmentManager().beginTransaction().add(uninstallRetainFragment, "UnistallTask").commit();

                break;
        }


        //manager.execute(request, requestListener);

        refreshUi(fullLayout, list);

        return super.onContextItemSelected(item);
    }

    public interface UpdateDisplayable{}

    public static class UpdatesHeader implements UpdateDisplayable{
        public String label;
        public String type;
    }

    public static class InstalledApp extends ApplicationInfo implements UpdateDisplayable {


        public PackageInfo getPackageInfo() {
            return packageInfo;
        }

        private final PackageInfo packageInfo;

        public InstalledApp(PackageInfo packageInfo) {
            super(packageInfo.applicationInfo);
            this.packageInfo = packageInfo;
        }

    }



    public static class UpdatesRecyclerView extends RecyclerView.Adapter<RecyclerView.ViewHolder>{


        List<UpdateDisplayable> packageList;

        PackageManager pm;

        public UpdatesRecyclerView(Context context, List<UpdateDisplayable> packageList) {
            this.packageList = packageList;
            this.pm = context.getPackageManager();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v;
            switch (viewType){
                case 0:
                    v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_app_update, parent, false);
                    return new UpdatesViewHolder(v);
                case 1:
                    v = LayoutInflater.from(parent.getContext()).inflate(R.layout.separator_updates, parent, false);
                    return new UpdatesHeaderViewHolder(v);

                case 2:
                    v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_app_installed, parent, false);
                    return new InstalledViewHolder(v);

                default:
                    return null;

            }

        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder superHolder, final int position) {

            switch (getItemViewType(position)) {
                case 0: {
                    final UpdatesViewHolder holder = (UpdatesViewHolder) superHolder;

                    final UpdatesResponse.UpdateApk item = (UpdatesResponse.UpdateApk) packageList.get(position);

                    if (item.name != null) {
                        holder.appName.setText(Html.fromHtml(item.name).toString());
                    } else {
                        holder.appName.setText("");
                    }

                    //String icon1 = item.icon;
                    holder.versionName.setText(item.vername);

                    holder.appIcon.setImageDrawable(item.info.loadIcon(pm));
                    //if (icon1 != null && icon1.contains("_icon")) {
                    //    String[] splittedUrl = icon1.split("\\.(?=[^\\.]+$)");
                    //    icon1 = splittedUrl[0] + "_" + Aptoide.iconSize + "." + splittedUrl[1];
                    //}

                    //ImageLoader.getInstance().displayImage(icon1, holder.appIcon);

                    holder.itemView.setLongClickable(true);

                    if (item.apk != null && item.apk.path != null) {
                        holder.manageIcon.setVisibility(View.VISIBLE);
                    } else {
                        holder.manageIcon.setVisibility(View.INVISIBLE);
                    }

                    holder.manageIcon.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            Toast.makeText(holder.itemView.getContext(), holder.itemView.getContext().getString(R.string.starting_download), Toast.LENGTH_LONG).show();
                            ((Start) holder.itemView.getContext()).installApp((UpdatesResponse.UpdateApk) packageList.get(position));

                        }
                    });
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            Intent i = new Intent(v.getContext(), Aptoide.getConfiguration().getAppViewActivityClass());
                            i.putExtra("fromRelated", true);
                            i.putExtra("md5sum", item.md5sum);
                            i.putExtra("repoName", item.store_name);
                            i.putExtra("download_from", "recommended_apps");
                            v.getContext().startActivity(i);

                        }
                    });
                }
                break;

                case 1: {
                    final UpdatesHeaderViewHolder holder = (UpdatesHeaderViewHolder) superHolder;

                    final UpdatesHeader item = (UpdatesHeader) packageList.get(position);


                    if(item.type.equals("installed")){
                        holder.more.setVisibility(View.GONE);
                    }else{
                        holder.more.setVisibility(View.VISIBLE);

                        holder.more.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                ArrayList<UpdatesResponse.UpdateApk> listToSend = new ArrayList<UpdatesResponse.UpdateApk>();


                                for (UpdateDisplayable updateDisplayable : packageList) {

                                    if(updateDisplayable instanceof UpdatesResponse.UpdateApk){
                                        listToSend.add((UpdatesResponse.UpdateApk) updateDisplayable);
                                    }

                                }


                                ((Start) holder.itemView.getContext()).installApp(listToSend);
                                Toast.makeText(holder.itemView.getContext(), holder.itemView.getContext().getString(R.string.starting_download), Toast.LENGTH_LONG).show();

                            }
                        });
                    }



                    holder.label.setText(item.label);
                }
                break;

                case 2: {


                    final InstalledViewHolder holder = (InstalledViewHolder) superHolder;

                    holder.itemView.setLongClickable(true);

                    final InstalledApp item = (InstalledApp) packageList.get(position);

                    holder.appName.setText(item.loadLabel(pm));
                    holder.appIcon.setImageDrawable(item.loadIcon(pm));
                    holder.versionName.setText(item.getPackageInfo().versionName);

                }
                break;
            }


        }


        @Override
        public int getItemViewType(int position) {

            if(packageList.get(position) instanceof InstalledApp){
                return 2;
            }else if(packageList.get(position) instanceof UpdatesHeader){
                return 1;
            }

            return 0;
        }

        @Override
        public int getItemCount() {
            return packageList.size();
        }

        public static class UpdatesHeaderViewHolder extends RecyclerView.ViewHolder{

            public View more;
            public TextView label;

            public UpdatesHeaderViewHolder(View v) {
                super(v);
                more = v.findViewById(R.id.more);
                label = (TextView) v.findViewById(R.id.separator_label);
            }
        }

        public static class InstalledViewHolder extends RecyclerView.ViewHolder{

            ImageView appIcon;
            TextView appName;
            TextView versionName;

            public InstalledViewHolder(View v) {
                super(v);
                appIcon = (ImageView) v.findViewById(R.id.app_icon);
                appName = (TextView) v.findViewById(R.id.app_name);
                versionName = (TextView) v.findViewById(R.id.app_version);
            }


        }

        public static class UpdatesViewHolder extends RecyclerView.ViewHolder{

            ImageView appIcon;
            ImageView manageIcon;
            TextView appName;
            TextView versionName;

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



