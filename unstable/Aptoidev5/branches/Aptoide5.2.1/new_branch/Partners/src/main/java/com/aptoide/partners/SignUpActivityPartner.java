package com.aptoide.partners;

import android.widget.TextView;
import cm.aptoide.ptdev.SignUpActivity;

/**
 * Created by tdeus on 3/25/14.
 */
public class SignUpActivityPartner extends SignUpActivity {

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(R.layout.partner_form_create_user);

        ((TextView)findViewById(R.id.accept_terms)).setText(getString(R.string.accept_oem_terms, AptoidePartner.getConfiguration().getMarketName()));
    }

}
