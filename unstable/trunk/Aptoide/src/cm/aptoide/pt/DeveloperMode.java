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

        final EditText path_editors = (EditText) findViewById(R.id.path_editors);
        path_editors.setText(AptoideConfiguration.getInstance().getEditorsPath());

        findViewById(R.id.path_editors_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AptoideConfiguration.getInstance().setEditorsPath(path_editors.getText().toString());
            }
        });

        final EditText path_top = (EditText) findViewById(R.id.path_top);
        path_top.setText(AptoideConfiguration.getInstance().getTopPath());

        findViewById(R.id.path_top_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AptoideConfiguration.getInstance().setTopPath(path_top.getText().toString());
            }
        });

        final EditText default_store = (EditText) findViewById(R.id.default_store);
        default_store.setText(AptoideConfiguration.getInstance().getDefaultStore());

        findViewById(R.id.default_store_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AptoideConfiguration.getInstance().setDefaultStore(default_store.getText().toString());
            }
        });
        
        

        CheckBox alwaysUpdate = (CheckBox) findViewById(R.id.alwaysupdate);
        alwaysUpdate.setChecked(AptoideConfiguration.getInstance().isAlwaysUpdate());

        alwaysUpdate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                AptoideConfiguration.getInstance().setAlwaysUpdate(isChecked);
            }
        });

        findViewById(R.id.invalidate_apk).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Database.getInstance().invalidateApkCache();
            }
        });
        findViewById(R.id.invalidate_timestamp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Database.getInstance().invalidateTimestampCache();
            }
        });
        findViewById(R.id.invalidate_featured).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Database.getInstance().invalidateFeatured();
            }
        });





    }
}
