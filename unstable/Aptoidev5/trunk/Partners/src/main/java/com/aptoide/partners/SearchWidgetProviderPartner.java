package com.aptoide.partners;

import cm.aptoide.ptdev.widget.SearchWidgetProvider;

/**
 * Created by tdeus on 4/3/14.
 */
public class SearchWidgetProviderPartner extends SearchWidgetProvider {

    @Override
    public int getLayoutTextId() {
        return R.id.search_widget_text;
    }

    @Override
    public int getLayout() {
        return R.layout.partner_search_widget;
    }
}
