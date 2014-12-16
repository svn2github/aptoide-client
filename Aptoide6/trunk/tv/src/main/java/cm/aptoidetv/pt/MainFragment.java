package cm.aptoidetv.pt;

import android.content.Intent;
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
import android.view.View;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import cm.aptoidetv.pt.Model.ApplicationAPK;
import cm.aptoidetv.pt.Model.BindInterface;
import cm.aptoidetv.pt.Model.EditorsChoice;
import cm.aptoidetv.pt.WebServices.HttpService;
import cm.aptoidetv.pt.WebServices.RequestTV;
import cm.aptoidetv.pt.WebServices.Response;

public class MainFragment extends BrowseFragment{
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
        final RequestTV request = new RequestTV("store");

        RequestListener<Response> requestListener = new RequestListener<Response>() {
            @Override
            public void onRequestFailure(SpiceException spiceException) {
                ((RequestsTvListener) getActivity()).onFailure();
            }

            @Override
            public void onRequestSuccess(Response response) {
                ArrayObjectAdapter mRowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
                CardPresenter cardPresenter = new CardPresenter();

                List<Response.GetStore.Widgets.Widget> categories = response.responses.getStore.datasets.widgets.data.list;

                {
                    List<EditorsChoice> mEditorsChoice = loadmEditorsChoice();
                    ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(cardPresenter);
                    for (EditorsChoice editorsChoice : mEditorsChoice) {
                        listRowAdapter.add(editorsChoice);
                    }
                    if (mEditorsChoice.size() > 0) {
                        HeaderItem header = new HeaderItem(mRowsAdapter.size() - 1, "Editors Choice", null);
                        mRowsAdapter.add(new ListRow(header, listRowAdapter));
                    }
                }
                for (Response.GetStore.Widgets.Widget widget : categories) {
                    if (!"apps_list".equals(widget.type)) {
                        continue;
                    }

                    final String ref_id = widget.data.ref_id;
                    final Response.ListApps.Category data = response.responses.listApps.datasets.getDataset().get(ref_id);

                    ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(cardPresenter);
                    for (Response.ListApps.Apk apk : data.data.list) {
                        ApplicationAPK storeApplication = new ApplicationAPK(apk, widget.name);
                        listRowAdapter.add(storeApplication);
                    }

                    if (listRowAdapter.size() > 0) {
                        HeaderItem header = new HeaderItem(mRowsAdapter.size() - 1, widget.name, null);

                        mRowsAdapter.add(new ListRow(header, listRowAdapter));
                    }
                }

                setAdapter(mRowsAdapter);
                ((RequestsTvListener) getActivity()).onSuccess();
            }
        };
        manager.execute(request, "store", DurationInMillis.ALWAYS_RETURNED, requestListener);
    }

    private void setupUIElements() {
        setTitle(getString(R.string.app_name));
        setHeadersState(HEADERS_ENABLED);
        TypedArray typedArray = getActivity().getTheme().obtainStyledAttributes(ThemePicker.getThemePicker(), new int[]{R.attr.brandColor, R.attr.searchColor});
        typedArray.recycle();

        int brandColorResourceId = typedArray.getResourceId(0, 0);
        int searchColorResourceId = typedArray.getResourceId(1, R.color.search_opaque);
        setBrandColor(getResources().getColor(brandColorResourceId));
        setSearchAffordanceColor(getResources().getColor(searchColorResourceId));
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
