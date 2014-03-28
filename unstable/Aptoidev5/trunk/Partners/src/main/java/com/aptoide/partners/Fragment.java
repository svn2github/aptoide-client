package com.aptoide.partners;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentBreadCrumbsPartners;
import android.support.v4.app.FragmentManager;
import android.view.*;
import android.widget.Toast;
import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.CategoryCallback;
import cm.aptoide.ptdev.StoreActivity;
import cm.aptoide.ptdev.fragments.FragmentStore;
import cm.aptoide.ptdev.fragments.FragmentStoreGridCategories;
import cm.aptoide.ptdev.fragments.FragmentStoreListCategories;

/**
 * Created by rmateus on 20-03-2014.
 */
public class Fragment extends android.support.v4.app.Fragment implements FragmentStore {




    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(cm.aptoide.ptdev.R.menu.menu_categories, menu);
        StoreActivity.SortObject sort = ((CategoryCallback) getActivity()).getSort();
        switch(sort.getSort()){
            case NAME:
                menu.findItem(cm.aptoide.ptdev.R.id.name).setChecked(true);
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

        if(sort.isNoCategories()){
            menu.findItem(cm.aptoide.ptdev.R.id.show_all).setChecked(true);
        }

        menu.findItem(cm.aptoide.ptdev.R.id.show_all).setVisible(!PreferenceManager.getDefaultSharedPreferences(Aptoide.getContext()).getBoolean("mergeStores", false));

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();

        if (i == android.R.id.home) {

        } else if( i == cm.aptoide.ptdev.R.id.refresh_store){
            ((StartPartner)getActivity()).refreshList();
        }
        else if( i == cm.aptoide.ptdev.R.id.name){
            ((StartPartner)getActivity()).setSort(StoreActivity.Sort.NAME);
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

        }else if( i == cm.aptoide.ptdev.R.id.show_all){

            ((StartPartner)getActivity()).toggleCategories();
            getChildFragmentManager().popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            setSort(item);

        }

        return super.onOptionsItemSelected(item);
    }

    private void setSort(MenuItem item) {
        getActivity().supportInvalidateOptionsMenu();
        ((StartPartner)getActivity()).refreshList();
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

            if("grid".equals(((AptoideConfigurationPartners)Aptoide.getConfiguration()).getStoreView())){
                fragment= new FragmentStoreListCategories();
            }else{
                fragment= new FragmentStoreGridCategories();
            }

            Bundle args = new Bundle();
            args.putLong("storeid", -200);
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


    @Override
    public void onRefresh() {
        FragmentStore fragStore = (FragmentStore) getChildFragmentManager().findFragmentByTag("fragStore");
        if(fragStore!=null){
            fragStore.onRefresh();
        }
    }

    @Override
    public void onError() {
        FragmentStore fragStore = (FragmentStore) getChildFragmentManager().findFragmentByTag("fragStore");
        if(fragStore!=null){
            fragStore.onError();
        }
    }

    @Override
    public void setRefreshing(boolean bool) {
        FragmentStore fragStore = (FragmentStore) getChildFragmentManager().findFragmentByTag("fragStore");
        if(fragStore!=null){
            fragStore.setRefreshing(bool);
        }
    }

    @Override
    public void setListShown(boolean b) {
        FragmentStore fragStore = (FragmentStore) getChildFragmentManager().findFragmentByTag("fragStore");
        if(fragStore!=null){
            fragStore.setListShown(b);
        }

    }

}
