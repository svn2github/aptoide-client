package cm.aptoide.ptdev;


import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import cm.aptoide.ptdev.adapters.RelatedBucketAdapter;
import cm.aptoide.ptdev.services.HttpClientSpiceService;
import cm.aptoide.ptdev.webservices.ListRelatedApkRequest;
import cm.aptoide.ptdev.webservices.json.RelatedApkJson;
import com.commonsware.cwac.merge.MergeAdapter;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rmateus on 22-01-2014.
 */
public class MoreRelatedActivity extends ActionBarActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Aptoide.getThemePicker().setAptoideTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_store);
        Fragment fragment = new FragmentRelatedMore();
        fragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
    }

    public static class FragmentRelatedMore extends ListFragment {

        private RelatedBucketAdapter listAdapter;

        private List<RelatedApkJson.Item> elements = new ArrayList<RelatedApkJson.Item>();
        private String mode;
        private MergeAdapter adapter;
        private String packageName;
        private int versionCode;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            packageName = getArguments().getString("packageName");
            versionCode = getArguments().getInt("versionCode");

            if (getArguments().containsKey("version")) {
                mode = "multiversion";
            } else if (getArguments().containsKey("developer")) {
                mode = "develbased";
            } else if (getArguments().containsKey("item")) {
                mode = "itembased";
            }
            adapter = new MergeAdapter();
            listAdapter = new RelatedBucketAdapter(getActivity(), elements);

        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            if (getArguments().containsKey("version")) {
                ((ActionBarActivity)getActivity()).getSupportActionBar().setTitle(getString(R.string.multiversion));
            } else if (getArguments().containsKey("developer")) {

                ((ActionBarActivity)getActivity()).getSupportActionBar().setTitle(getString(R.string.more_from_publisher));
            } else if (getArguments().containsKey("item")) {

                ((ActionBarActivity)getActivity()).getSupportActionBar().setTitle(getString(R.string.related_apps));
            }
        }

        SpiceManager spiceManager = new SpiceManager(HttpClientSpiceService.class);

        RequestListener<RelatedApkJson> request = new RequestListener<RelatedApkJson>() {
            @Override
            public void onRequestFailure(SpiceException spiceException) {
                Toast.makeText(getActivity(), "Error listRelated", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onRequestSuccess(RelatedApkJson relatedApkJson) {


                //Toast.makeText(getActivity(), "ItemBased size " + relatedApkJson.getItembased().size(), Toast.LENGTH_SHORT).show();
                //Toast.makeText(getActivity(), "DevelBased size " + relatedApkJson.getDevelbased().size(), Toast.LENGTH_SHORT).show();
                //Toast.makeText(getActivity(), "MultiVersion size " + relatedApkJson.getMultiversion().size(), Toast.LENGTH_SHORT).show();


                ArrayList<RelatedApkJson.Item> items;

                if (mode.equals("multiversion")) {
                    items = new ArrayList<RelatedApkJson.Item>(relatedApkJson.getMultiversion());
                } else if (mode.equals("itembased")) {
                    items = new ArrayList<RelatedApkJson.Item>(relatedApkJson.getItembased());
                } else {
                    items = new ArrayList<RelatedApkJson.Item>(relatedApkJson.getDevelbased());
                }


                if (items.size() > 0) {
                    elements.clear();
                    if (PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("matureChkBox", true)) {


                        for (RelatedApkJson.Item item : items) {
                            if (!item.getAge().equals("Mature")) {
                                elements.add(item);
                            }
                        }

                    } else {
                        elements.addAll(items);
                    }
                    adapter.addAdapter(listAdapter);
                }


                listAdapter.notifyDataSetChanged();

                setListAdapter(adapter);
            }
        };

        @Override
        public void onStart() {
            super.onStart();
            spiceManager.start(getActivity());
        }

        @Override
        public void onStop() {
            super.onStop();
            if (spiceManager.isStarted()) {
                spiceManager.shouldStop();
            }
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            ListRelatedApkRequest listRelatedApkRequest = new ListRelatedApkRequest(getActivity());

            //listRelatedApkRequest.setRepos("apps");
            listRelatedApkRequest.setMode(mode);
            listRelatedApkRequest.setPackageName(packageName);
            listRelatedApkRequest.setVercode(versionCode);
            listRelatedApkRequest.setLimit(listAdapter.getBucketSize() * 5);
            View header = LayoutInflater.from(getActivity()).inflate(R.layout.header_frag_more, null);
            if (header != null) {
                TextView title = (TextView) header.findViewById(R.id.separator_label);
                title.setText(getArguments().getString("appName"));
            }
            getListView().addHeaderView(header);
            spiceManager.execute(listRelatedApkRequest, packageName + "-related-" + mode, DurationInMillis.ONE_DAY, request);

            getListView().setDivider(null);
            getListView().setCacheColorHint(getResources().getColor(android.R.color.transparent));



        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int i = item.getItemId();

        if (i == android.R.id.home) {
            finish();
        } else if (i == R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
