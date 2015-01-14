package cm.aptoidetv.pt;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import java.lang.reflect.Type;
import java.util.ArrayList;
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
    private SpiceManager manager = new SpiceManager(HttpService.class);

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
        final RequestTV request = new RequestTV("store", getActivity());
        final HashMap<String,String> installed= new HashMap<>();
        for (PackageInfo pi : getActivity().getPackageManager().getInstalledPackages(PackageManager.GET_META_DATA)){
            if(!pi.packageName.startsWith("com.android"))
                installed.put(pi.packageName, pi.versionName);
        }
        RequestListener<Response> requestListener = new RequestListener<Response>() {
            @Override
            public void onRequestFailure(SpiceException spiceException) {
                Log.d("pois","spiceException msg:" + spiceException.getMessage());
                Log.d("pois","spiceException cause:"+spiceException.getCause());
                ((RequestsTvListener) getActivity()).onFailure();
            }

            @Override
            public void onRequestSuccess(Response response) {
                try {
                    ArrayObjectAdapter mRowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
                    CardPresenter cardPresenter = new CardPresenter();

                    List<Response.GetStore.Widgets.Widget> categories = response.responses.getStore.datasets.widgets.data.list;

                    {
                        List<EditorsChoice> mEditorsChoice = loadmEditorsChoice();
                        ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(cardPresenter);
                        int amount = 0;
                        for (EditorsChoice editorsChoice : mEditorsChoice) {
                            amount++;
                            listRowAdapter.add(editorsChoice);
                        }
                        if (mEditorsChoice.size() > 0) {
                            HeaderItem header = new HeaderItem(mRowsAdapter.size() - 1, addAmountToTitle("Editors Choice", amount), null);
                            mRowsAdapter.add(new ListRow(header, listRowAdapter));
                        }
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
                        for (Response.ListApps.Apk apk : data.data.list) {
                            if (installed.keySet().contains(apk.packageName)) {
/*                            Log.d("pois","Package: "+apk.packageName);
                            Log.d("pois","apk.vername: "+apk.vername);
                            Log.d("pois","installed: "+installed.get(apk.packageName));
                            Log.d("pois","###############################################");*/
                                if (apk.vername.compareTo(installed.get(apk.packageName)) > 0) {
                                    if (APKUpdates.containsKey(apk.packageName)) {
                                        Response.ListApps.Apk currentapk = APKUpdates.get(apk.packageName);
                                        if (apk.vername.compareTo(currentapk.vername) > 0) {
                                            APKUpdates.remove(apk.packageName);
                                            APKUpdates.put(apk.packageName, apk);
                                        }
                                    } else {
                                        APKUpdates.put(apk.packageName, apk);
                                    }
                                }
                            }

                            ApplicationAPK storeApplication = new ApplicationAPK(apk, widget.name);
                            amount++;
                            listRowAdapter.add(storeApplication);

                        }
                        if (listRowAdapter.size() > 0) {
                            HeaderItem header = new HeaderItem(mRowsAdapter.size() - 1, addAmountToTitle(widget.name, amount), null);

                            mRowsAdapter.add(new ListRow(header, listRowAdapter));
                        }
                    }

                    // Updates
                    if (APKUpdates.size() > 0) {
                        final String updates = getString(R.string.updates);
                        ArrayObjectAdapter listUpdatesAdapter = new ArrayObjectAdapter(cardPresenter);
                        int amount = 0;
                        for (Response.ListApps.Apk apk : APKUpdates.values()) {
                            ApplicationAPK storeApplication = new ApplicationAPK(apk, updates);
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
                        Log.d("pois","Fail: Time Passed > 10000");
                        ((RequestsTvListener) getActivity()).onFailure();
                    }
                    manager.execute(request, "store", DurationInMillis.ALWAYS_RETURNED, this);
                }
            }
        };
        timeof1strequest = System.currentTimeMillis();
        manager.execute(request, "store", DurationInMillis.ALWAYS_RETURNED, requestListener);
    }
    private String addAmountToTitle(String Title,int amount){
        return Title + " ("+amount+')';
    }
    private void setupUIElements() {
        setTitle(getString(R.string.app_name));
        setHeadersState(HEADERS_ENABLED);
        TypedArray typedArray = getActivity().getTheme().obtainStyledAttributes(ThemePicker.getThemePicker(), new int[]{R.attr.brandColor, R.attr.searchColor});

        int brandColorResourceId = typedArray.getResourceId(0, 0);
        int searchColorResourceId = typedArray.getResourceId(1, R.color.search_opaque);
        setBrandColor(getResources().getColor(brandColorResourceId));
        setSearchAffordanceColor(getResources().getColor(searchColorResourceId));

        typedArray.recycle();

    }

    private List<EditorsChoice> loadmEditorsChoice() {
        String json = Utils.loadJSONFromResource(getActivity(), R.raw.editorschoice);
        Gson gson = new Gson();
        Type collection = new TypeToken<ArrayList<EditorsChoice>>(){}.getType();
        return gson.fromJson( json, collection );
    }
    private void setupEventListeners() {
        setOnSearchClickedListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                startActivity(intent);
            }
        });

        setOnItemViewClickedListener(new ItemViewClickedListener());
    }

    private final class ItemViewClickedListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                  RowPresenter.ViewHolder rowViewHolder, Row row) {
            ((BindInterface)item).startActivity(getActivity());

        }
    }
}
