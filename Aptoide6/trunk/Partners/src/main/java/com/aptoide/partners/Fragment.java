package com.aptoide.partners;

import android.os.Bundle;
import android.support.v4.app.FragmentBreadCrumbsPartners;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.octo.android.robospice.persistence.DurationInMillis;

import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.CategoryCallback;
import cm.aptoide.ptdev.StoreActivity;
import cm.aptoide.ptdev.fragments.FragmentListStore;

/**
 * Created by rmateus on 20-03-2014.
 */
public class Fragment extends android.support.v4.app.Fragment  {




    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        if(((AptoideConfigurationPartners) Aptoide.getConfiguration()).getMultistores()) {
            inflater.inflate(cm.aptoide.ptdev.R.menu.menu_categories, menu);
        } else{
            inflater.inflate(R.menu.partners_menu_categories, menu);
        }
        StoreActivity.SortObject sort = ((CategoryCallback) getActivity()).getSort();
        switch(sort.getSort()){
            case NAMEAZ:
                menu.findItem(cm.aptoide.ptdev.R.id.nameAZ).setChecked(true);
                break;
            case NAMEZA:
                menu.findItem(cm.aptoide.ptdev.R.id.nameZA).setChecked(true);
                break;
            case DOWNLOADS:
                menu.findItem(cm.aptoide.ptdev.R.id.download).setChecked(true);
                break;
            case DATE:
                menu.findItem(cm.aptoide.ptdev.R.id.date).setChecked(true);
                break;
            case PRICE:
                menu.findItem(cm.aptoide.ptdev.R.id.price).setChecked(true);
                break;
            case RATING:
                menu.findItem(cm.aptoide.ptdev.R.id.rating).setChecked(true);
                break;
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();

        if (i == android.R.id.home) {

        } else if( i == cm.aptoide.ptdev.R.id.refresh_store){

        }
        else if( i == cm.aptoide.ptdev.R.id.nameAZ) {
            ((StartPartner) getActivity()).setSort(StoreActivity.Sort.NAMEAZ);
            setSort(item);
        }else if( i == cm.aptoide.ptdev.R.id.nameZA){
                ((StartPartner)getActivity()).setSort(StoreActivity.Sort.NAMEZA);
                setSort(item);
        } else if( i == cm.aptoide.ptdev.R.id.date){
            ((StartPartner)getActivity()).setSort(StoreActivity.Sort.DATE);
            setSort(item);
        }else if( i == cm.aptoide.ptdev.R.id.download){
            ((StartPartner)getActivity()).setSort(StoreActivity.Sort.DOWNLOADS);
            setSort(item);
        }else if( i == cm.aptoide.ptdev.R.id.rating){
            ((StartPartner)getActivity()).setSort(StoreActivity.Sort.RATING);
            setSort(item);
        }else if( i == cm.aptoide.ptdev.R.id.price){
            ((StartPartner)getActivity()).setSort(StoreActivity.Sort.PRICE);
            setSort(item);
        }

        return super.onOptionsItemSelected(item);
    }

    private void setSort(MenuItem item) {
        getActivity().supportInvalidateOptionsMenu();
        FragmentListStore fragmentById = (FragmentListStore) getChildFragmentManager().findFragmentById(cm.aptoide.ptdev.R.id.content_layout);
        fragmentById.refresh(DurationInMillis.ONE_HOUR);
    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);

        if(isAdded()){
            android.support.v4.app.Fragment frag = getChildFragmentManager().findFragmentByTag("fragStore");

            if(frag!=null){
                frag.setMenuVisibility(menuVisible);
            }
        }


    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isAdded()) {

            android.support.v4.app.Fragment frag = getChildFragmentManager().findFragmentByTag("fragStore");

            if (frag != null) {
                frag.setUserVisibleHint(isVisibleToUser);
            }
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        FragmentBreadCrumbsPartners breadCrumbs = (FragmentBreadCrumbsPartners) getView().findViewById(cm.aptoide.ptdev.R.id.breadcrumbs);
        breadCrumbs.setFragment(this);
        breadCrumbs.setTitle("Home", null);

        if (savedInstanceState == null) {

            android.support.v4.app.Fragment fragment;
            String storeView = ((AptoideConfigurationPartners)Aptoide.getConfiguration()).getStoreView();

            fragment = new FragmentListStore();

            Bundle args = new Bundle();
            args.putLong("storeid", -200);
            args.putString("view", storeView);
            args.putString("storename", Aptoide.getConfiguration().getDefaultStore());
            args.putString("theme", ((AptoideConfigurationPartners) Aptoide.getConfiguration()).getTheme());
            fragment.setArguments(args);
            fragment.setMenuVisibility(false);
            fragment.setUserVisibleHint(false);
            getChildFragmentManager().beginTransaction().replace(R.id.content_layout, fragment, "fragStore").commit();

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.partner_page_store_list, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();



    }

}
