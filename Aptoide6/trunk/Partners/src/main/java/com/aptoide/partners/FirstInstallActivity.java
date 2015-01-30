package com.aptoide.partners;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.internal.view.SupportMenuInflater;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.aptoide.partners.firstinstall.DividerItemDecoration;
import com.aptoide.partners.firstinstall.FirstInstallRow;
import com.aptoide.partners.firstinstall.SelectableAdapter;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;

import java.util.ArrayList;
import java.util.List;

import javax.xml.datatype.Duration;

import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.services.HttpClientSpiceService;
import cm.aptoide.ptdev.utils.AptoideUtils;
import cm.aptoide.ptdev.webservices.Api;
import cm.aptoide.ptdev.webservices.GetAdsRequest;
import cm.aptoide.ptdev.webservices.Response;
import cm.aptoide.ptdev.webservices.json.ApkSuggestionJson;
import retrofit.http.Body;
import retrofit.http.POST;

/**
 * Created by rmateus on 27-01-2015.
 */
public class FirstInstallActivity extends ActionBarActivity {


    SpiceManager spice = new SpiceManager(HttpClientSpiceService.class);
    ArrayList<FirstInstallRow> rowList = new ArrayList<FirstInstallRow>();

    private static boolean DEFAULT_SELECTED_VALUE = true;
    private int count;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        Aptoide.getThemePicker().setAptoideTheme(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.partner_firstinstall);


        StartPartner.TestRequest request = new StartPartner.TestRequest();

        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.first_install_list);
        findViewById(R.id.install_selected).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishWithResult();
            }
        });
        findViewById(R.id.select_all).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DEFAULT_SELECTED_VALUE = !DEFAULT_SELECTED_VALUE;

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

                for (FirstInstallRow row : rowList) {
                    row.animate(true);
                    row.setSelected(DEFAULT_SELECTED_VALUE);

                }

                recyclerView.getAdapter().notifyDataSetChanged();


            }
        });
        final TextView viewById = (TextView) findViewById(R.id.header);
        SelectableAdapter selectableAdapter = new SelectableAdapter(rowList, new SelectableAdapter.OnItemSelectedListener() {
            @Override
            public void onSelect() {
                count = 0;
                for (FirstInstallRow row : rowList) {
                    if(row.isSelected()){
                        count++;
                    }
                }

                viewById.setText("Total items selected  - " + count);

            }
        });


        recyclerView.setAdapter(selectableAdapter);
        recyclerView.setHasFixedSize(true);
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
        recyclerView.addItemDecoration(new DividerItemDecoration((int) px));

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        spice.execute(request, "firstInstallRequest", DurationInMillis.ONE_MINUTE, new RequestListener<Response>() {
            @Override
            public void onRequestFailure(SpiceException spiceException) {

            }

            @Override
            public void onRequestSuccess(Response response) {

                try {

                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                    findViewById(R.id.first_install_list).setVisibility(View.VISIBLE);

                    for (Response.GetStore.Widgets.Widget widget : response.responses.getStore.datasets.widgets.data.list) {

                        List<Response.ListApps.Apk> list = response.responses.listApps.datasets.getDataset().get(widget.data.ref_id).data.list;


                        for (Response.ListApps.Apk apk : list) {


                            FirstInstallRow row = new FirstInstallRow();

                            row.setAppName(apk.name);
                            row.setDownloads(apk.downloads.longValue());
                            row.setIcon(apk.icon);
                            row.setSelected(true);
                            row.setId(apk.id.longValue());
                            rowList.add(row);
                            recyclerView.getAdapter().notifyDataSetChanged();


                        }

                        GetAdsRequest request = new GetAdsRequest(FirstInstallActivity.this);

                        request.setLimit(widget.data.options.ads_count.intValue());
                        request.setLocation("homepage");
                        request.setKeyword("__NULL__");

                        spice.execute(request, "firstInstallRequest", DurationInMillis.ONE_MINUTE, new RequestListener<ApkSuggestionJson>() {
                            @Override
                            public void onRequestFailure(SpiceException spiceException) {

                            }

                            @Override
                            public void onRequestSuccess(ApkSuggestionJson apkSuggestionJson) {


                                for (ApkSuggestionJson.Ads ads : apkSuggestionJson.getAds()) {
                                    FirstInstallRow row = new FirstInstallRow();

                                    row.setAppName(ads.getData().getName());
                                    row.setDownloads(ads.getData().getDownloads().longValue());
                                    row.setIcon(ads.getData().getIcon());
                                    row.setSelected(true);
                                    row.setId(ads.getData().id.longValue());
                                    int previousCount = rowList.size();
                                    rowList.add(row);
                                    recyclerView.getAdapter().notifyItemRangeInserted(previousCount, rowList.size());

                                }

                            }
                        });



                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    public void finishWithResult(){
        Intent intent = new Intent();

        ArrayList<Integer> rtn = new ArrayList<>();

        for (FirstInstallRow row : rowList) {
            if(row.isSelected()){
                rtn.add((int) row.getId());
            }
        }

        intent.putIntegerArrayListExtra("ids", rtn );

        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        finish();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        SupportMenuInflater inflater = new SupportMenuInflater(this);

        inflater.inflate(R.menu.menu_wizard, menu);


        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onStart() {
        super.onStart();
        spice.start(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        spice.shouldStop();
    }



}
