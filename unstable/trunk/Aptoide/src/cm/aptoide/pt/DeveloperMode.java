package cm.aptoide.pt;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import cm.aptoide.pt.configuration.AptoideConfiguration;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 09-09-2013
 * Time: 13:06
 * To change this template use File | Settings | File Templates.
 */
public class DeveloperMode extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.devmode);


        final EditText auto_update_url = (EditText) findViewById(R.id.url_auto_update);
        auto_update_url.setText(AptoideConfiguration.getInstance().getAutoUpdateUrl());
        findViewById(R.id.url_auto_update_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AptoideConfiguration.getInstance().setAutoUpdateUrl(auto_update_url.getText().toString());
            }
        });

        final EditText webservices_url = (EditText) findViewById(R.id.url_webservices);
        webservices_url.setText(AptoideConfiguration.getInstance().getWebServicesUri());

        findViewById(R.id.url_webservices_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AptoideConfiguration.getInstance().setWebservicesUri(webservices_url.getText().toString());
            }
        });

        final EditText store_domain = (EditText) findViewById(R.id.url_domain);
        store_domain.setText(AptoideConfiguration.getInstance().getDomainAptoideStore());

        findViewById(R.id.url_domain_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AptoideConfiguration.getInstance().setDomainStore(store_domain.getText().toString());
            }
        });


        final EditText search_url = (EditText) findViewById(R.id.url_search);
        search_url.setText(AptoideConfiguration.getInstance().getUriSearch());

        findViewById(R.id.url_search_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AptoideConfiguration.getInstance().setUriSearch(search_url.getText().toString());
            }
        });

        final EditText path_cache = (EditText) findViewById(R.id.path_cache);
        path_cache.setText(AptoideConfiguration.getInstance().getPathCache());

        findViewById(R.id.path_cache_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AptoideConfiguration.getInstance().setPathCache(path_cache.getText().toString());
            }
        });

        EditText path_cache_apks = (EditText) findViewById(R.id.path_cache_apk);
        path_cache_apks.setText(AptoideConfiguration.getInstance().getPathCacheApks());

        EditText path_cache_icons = (EditText) findViewById(R.id.path_cache_icons);
        path_cache_icons.setText(AptoideConfiguration.getInstance().getPathCacheIcons());



        CheckBox alwaysUpdate = (CheckBox) findViewById(R.id.alwaysupdate);
        alwaysUpdate.setChecked(AptoideConfiguration.getInstance().isAlwaysUpdate());

        alwaysUpdate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                AptoideConfiguration.getInstance().setAlwaysUpdate(isChecked);
            }
        });




    }
}
