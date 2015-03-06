package cm.aptoidetv.pt;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v17.leanback.app.BrowseFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.util.Log;
import android.view.View;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import cm.aptoidetv.pt.Model.ApplicationAPK;
import cm.aptoidetv.pt.Model.BindInterface;
import cm.aptoidetv.pt.Model.EditorsChoice;
import cm.aptoidetv.pt.Model.Settingitem.MyaccountItem;
import cm.aptoidetv.pt.Model.Settingitem.PreferencesItem;
import cm.aptoidetv.pt.WebServices.HttpService;
import cm.aptoidetv.pt.WebServices.RequestTV;
import cm.aptoidetv.pt.WebServices.Response;

public class MainFragment extends BrowseFragment{
    private static long timeof1strequest;
    public static boolean Reload = true;
    private SpiceManager manager = new SpiceManager(HttpService.class);
    private RequestListener<Response> requestListener = new RequestListener<Response>() {
        @Override
        public void onRequestFailure(SpiceException spiceException) {
            ((RequestsTvListener) getActivity()).onFailure();
        }

        @Override
        public void onRequestSuccess(Response response) {
            try {
                ArrayObjectAdapter mRowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
                CardPresenter cardPresenter = new CardPresenter();
                List<Response.GetStore.Widgets.Widget> categories = response.responses.getStore.datasets.widgets.data.list;

                final HashMap<String,String> installed= new HashMap<>();
                for (PackageInfo pi : getActivity().getPackageManager().getInstalledPackages(PackageManager.GET_META_DATA)){
                    if(!pi.packageName.startsWith("com.android"))
                        installed.put(pi.packageName, pi.versionName);
                }
                HashMap<String, Response.ListApps.Apk> APKUpdates = new HashMap<>();

                for (Response.GetStore.Widgets.Widget widget : categories) {
                    if (!"apps_list".equals(widget.type)) {
                        continue;
                    }
                    final String ref_id = widget.data.ref_id;
                    final Response.ListApps.Category data = response.responses.listApps.datasets.getDataset().get(ref_id);
                    ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(cardPresenter);
                    int amount = 0;
                    int adapterSize = mRowsAdapter.size() - 1;
                    int position = "Editors Choice".equals(widget.name) ?
                            0 : adapterSize == 0 ? 1:adapterSize;
                    for (Response.ListApps.Apk apk : data.data.list) {
                        String installedapk = installed.get(apk.packageName);
                        if(installedapk!=null) {
                            if (apk.vername.compareTo(installedapk) > 0) {
                                Response.ListApps.Apk currentapk = APKUpdates.get(apk.packageName);
                                if (currentapk!=null) {
                                    if (apk.vername.compareTo(currentapk.vername) > 0) {
                                        APKUpdates.remove(apk.packageName);
                                        APKUpdates.put(apk.packageName, apk);
                                    }
                                } else {
                                    APKUpdates.put(apk.packageName, apk);
                                }
                            }
                        }
                        ApplicationAPK storeApplication = position == 0 ?
                                        new EditorsChoice(apk):
                                        new ApplicationAPK(apk);
                        amount++;
                        listRowAdapter.add(storeApplication);

                    }
                    if (listRowAdapter.size() > 0) {

                        HeaderItem header = new HeaderItem(position, addAmountToTitle(widget.name, amount), null);
                        mRowsAdapter.add(new ListRow(header, listRowAdapter));
                    }
                }

                // Updates
                if (APKUpdates.size() > 0) {
                    final String updates = getString(R.string.updates);
                    ArrayObjectAdapter listUpdatesAdapter = new ArrayObjectAdapter(cardPresenter);
                    int amount = 0;
                    for (Response.ListApps.Apk apk : APKUpdates.values()) {
                        ApplicationAPK storeApplication = new ApplicationAPK(apk);
                        amount++;
                        listUpdatesAdapter.add(storeApplication);

                    }
                    HeaderItem header = new HeaderItem(mRowsAdapter.size() - 1, addAmountToTitle(updates, amount), null);
                    mRowsAdapter.add(new ListRow(header, listUpdatesAdapter));
                }
                // Settings
                {
                    final String settings = getString(R.string.settings);
                    ArrayObjectAdapter listsettingsAdapter = new ArrayObjectAdapter(cardPresenter);
                    listsettingsAdapter.add(new MyaccountItem());
                    listsettingsAdapter.add(new PreferencesItem());
                    HeaderItem header = new HeaderItem(mRowsAdapter.size() - 1, settings, null);
                    mRowsAdapter.add(new ListRow(header, listsettingsAdapter));
                }

                setAdapter(mRowsAdapter);
                ((RequestsTvListener) getActivity()).onSuccess();
            } catch (Exception e) {
                long timepassed = System.currentTimeMillis()-timeof1strequest;
                if(timepassed>10000){
                    ((RequestsTvListener) getActivity()).onFailure();
                }

            }
        }
    };
    private String addAmountToTitle(String Title,int amount){
        return Title + " ("+amount+')';
    }

    @Override
    public void onResume() {
        super.onResume();
        if(Reload) {
            manager.execute(new RequestTV(getActivity()), "store", DurationInMillis.ALWAYS_RETURNED, requestListener);
            Reload=false;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        manager.start(getActivity());
    }

    @Override
    public void onStop() {
        super.onStop();
        manager.shouldStop();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setupUIElements();
        setupEventListeners();
        final RequestTV request = new RequestTV(getActivity());


        timeof1strequest = System.currentTimeMillis();
        manager.execute(request, "store", DurationInMillis.ALWAYS_RETURNED, requestListener);
    }

    private void setupUIElements() {
        if(getString(R.string.imgtitle).length()>0) {
            new UpdateTitleFromURL().execute(getString(R.string.imgtitle));
        }else{
            setTitle(getString(R.string.app_name));
        }
        setHeadersState(HEADERS_ENABLED);
        TypedArray typedArray = getActivity().getTheme().obtainStyledAttributes(ThemePicker.getThemePicker(), new int[]{R.attr.brandColor, R.attr.searchColor});
        int brandColorResourceId = typedArray.getResourceId(0, 0);
        int searchColorResourceId = typedArray.getResourceId(1, R.color.search_opaque);
        setBrandColor(getResources().getColor(brandColorResourceId));
        setSearchAffordanceColor(getResources().getColor(searchColorResourceId));
        typedArray.recycle();
    }

    private void setupEventListeners() {
        setOnSearchClickedListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                startActivity(intent);
            }
        });
        setOnItemViewClickedListener(new OnItemViewClickedListener() {
            @Override
            public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
                ((BindInterface)item).startActivity(getActivity());
            }
        });
    }

    public class UpdateTitleFromURL extends AsyncTask<String, Void, BitmapDrawable> {
        @Override
        protected void onPostExecute(BitmapDrawable d) {
            super.onPostExecute(d);
            if(d==null)
                setTitle(getString(R.string.app_name));
            else
                setBadgeDrawable(d);
        }

        @Override
        protected BitmapDrawable doInBackground(String... params) {
            Bitmap x;

            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) new URL(params[0]).openConnection();
                connection.connect();
                InputStream input = connection.getInputStream();
                x= BitmapFactory.decodeStream(input);
                return new BitmapDrawable(x);
            } catch (IOException e) {
                return null;
            }
        }
    }
}
