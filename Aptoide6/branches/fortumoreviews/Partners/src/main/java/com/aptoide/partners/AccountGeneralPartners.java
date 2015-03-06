package com.aptoide.partners;

import cm.aptoide.ptdev.Aptoide;

/**
 * Created by brutus on 10-12-2013.
 */
public class AccountGeneralPartners {

    /**
     * Account type id
     */
    public static final String ACCOUNT_TYPE = Aptoide.getContext().getString(R.string.account_type);

    /**
     * Account name
     */
    public static final String ACCOUNT_NAME = "aptoide";

    /**
     * Auth token types
     */
    public static final String AUTHTOKEN_TYPE_READ_ONLY = "Read only";
    public static final String AUTHTOKEN_TYPE_READ_ONLY_LABEL = "Read only access to an Aptoide account";

    public static final String AUTHTOKEN_TYPE_FULL_ACCESS = "Full access";
    public static final String AUTHTOKEN_TYPE_FULL_ACCESS_LABEL = "Full access to an Aptoide account";

}
