package cm.aptoide.ptdev;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import java.util.ArrayList;
import java.util.List;

import cm.aptoide.ptdev.fragments.FragmentListApps;
import cm.aptoide.ptdev.services.HttpClientSpiceService;
import cm.aptoide.ptdev.webservices.GetAdsRequest;
import cm.aptoide.ptdev.webservices.json.ApkSuggestionJson;

/**
 * Created by asantos on 01-12-2014.
 */
public class MoreHighlightedActivity extends ActionBarActivity {


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home || item.getItemId() == R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Aptoide.getThemePicker().setAptoideTheme(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_store);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("Highlighted");

        Fragment fragment = new MoreFriendsInstallsFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
    }

    public static class MoreFriendsInstallsFragment extends Fragment {
        private RecyclerView recyclerView;

        SpiceManager spiceManager = new SpiceManager(HttpClientSpiceService.class);

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            spiceManager.start(activity);

        }


        @Override
        public void onDetach() {
            super.onDetach();
            spiceManager.shouldStop();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_list_apps, container, false);
        }

        List<FragmentListApps.AdRow> list = new ArrayList<>();

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            recyclerView = (RecyclerView) view.findViewById(R.id.list);

            recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));

            GetAdsRequest request = new GetAdsRequest(getActivity());
            recyclerView.setAdapter(new HighlightedAdapter(list));
            final SwipeRefreshLayout swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);

            swipeLayout.setEnabled(false);

            request.setLimit(10);
            request.setLocation("homepage");
            request.setKeyword("__NULL__");

            spiceManager.execute(request, new RequestListener<ApkSuggestionJson>() {
                @Override
                public void onRequestFailure(SpiceException spiceException) {

                }

                @Override
                public void onRequestSuccess(ApkSuggestionJson apkSuggestionJson) {
                    if (apkSuggestionJson != null && apkSuggestionJson.getAds() != null && apkSuggestionJson.getAds().size() > 0) {

                        ArrayList<ApkSuggestionJson.Ads> inElements = new ArrayList<>(apkSuggestionJson.getAds());

                        while (!inElements.isEmpty()) {
                            FragmentListApps.AdRow row = new FragmentListApps.AdRow();
                            for (int i = 0; i < 3 && !inElements.isEmpty(); i++) {
                                row.ads.add(inElements.remove(0));
                            }
                            row.header = "Highlighted";
                            row.widgetid = "highlighted";

                            list.add(row);
                        }

                        recyclerView.getAdapter().notifyDataSetChanged();

                    }


                }
            });
        }

    }



public static class HighlightedAdapter extends RecyclerView.Adapter<FragmentListApps.RecyclerAdapter.RowViewHolder>{

    private final List<FragmentListApps.AdRow> list;

    public HighlightedAdapter(List<FragmentListApps.AdRow> list) {
        this.list = list;
    }


    @Override
    public FragmentListApps.RecyclerAdapter.RowViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LinearLayout inflate = new LinearLayout(parent.getContext());
        inflate.setOrientation(LinearLayout.HORIZONTAL);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        inflate.setLayoutParams(params);



        return new FragmentListApps.RecyclerAdapter.RowViewHolder(inflate, viewType, parent.getContext()) ;
    }

    @Override
    public void onBindViewHolder(FragmentListApps.RecyclerAdapter.RowViewHolder holder, int position) {
        list.get(position).bindView(holder);
    }

    @Override
    public int getItemViewType(int position) {
        return list.get(position).getViewType();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


}





}
