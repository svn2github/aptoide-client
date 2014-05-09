package cm.aptoide.ptdev.fragments;

import android.accounts.*;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.*;
import android.view.animation.AnimationUtils;
import android.widget.*;
import cm.aptoide.ptdev.*;
import cm.aptoide.ptdev.adapters.RelatedBucketAdapter;
import cm.aptoide.ptdev.adapters.StoreSpinnerAdapter;
import cm.aptoide.ptdev.configuration.AccountGeneral;
import cm.aptoide.ptdev.dialogs.AptoideDialog;
import cm.aptoide.ptdev.dialogs.ProgressDialogFragment;
import cm.aptoide.ptdev.downloadmanager.PermissionsActivity;
import cm.aptoide.ptdev.events.AppViewRefresh;
import cm.aptoide.ptdev.events.BusProvider;
import cm.aptoide.ptdev.events.OnMultiVersionClick;
import cm.aptoide.ptdev.model.*;
import cm.aptoide.ptdev.services.HttpClientSpiceService;
import cm.aptoide.ptdev.utils.AptoideUtils;
import cm.aptoide.ptdev.webservices.AddCommentRequest;
import cm.aptoide.ptdev.webservices.AddLikeRequest;
import cm.aptoide.ptdev.webservices.ListRelatedApkRequest;
import cm.aptoide.ptdev.webservices.json.GetApkInfoJson;
import cm.aptoide.ptdev.webservices.json.RelatedApkJson;
import com.commonsware.cwac.merge.MergeAdapter;
import com.nostra13.universalimageloader.cache.disc.BaseDiscCache;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static cm.aptoide.ptdev.utils.AptoideUtils.withSuffix;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 15-11-2013
 * Time: 14:34
 * To change this template use File | Settings | File Templates.
 */
public abstract class FragmentAppView extends Fragment {


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onResume() {
        super.onResume();
        BusProvider.getInstance().register(this);
    }


    @Override
    public void onPause() {
        super.onPause();
        BusProvider.getInstance().unregister(this);
    }


    public static class FragmentAppViewDetails extends FragmentAppView{


        private TextView description;
        private TextView showAllDescription;
        private LinearLayout descriptionContainer;
//        private ScrollView scroller;

        private RelativeLayout layoutInfoDetails;
        private TextView store;
        private TextView downloads;
        private RatingBar rating;
        private TextView likes;
        private TextView dontLikes;
        private TextView size;
        private TextView publisher;



        private ProgressBar loadingPb;

        private TextView publisherWebsite;
        private TextView publisherEmail;
        private TextView publisherPrivacyPolicy;
        private View publisherContainer;
        private View whatsNewContainer;
        private TextView whatsNew;
        private boolean collapsed = true;
        private LinearLayout detailsContainer;
        private View row2;
        private View row3;
        private Spinner spinner;


        @Subscribe
        public void refreshDetails(final AppViewActivity.DetailsEvent event) {
            Log.d("Aptoide-AppView", "getting event");
            Log.d("Aptoide-AppView", "Setting description");
            if(event == null) return;

            if (event.getDescription()!=null)
                description.setText(event.getDescription());

//            Log.d("Aptoide-description", "lines "+ description.getLineCount() );
            if (event.getDescription()!=null && event.getDescription().length() > 250) {

                description.setMaxLines(10);
                showAllDescription.setVisibility(View.VISIBLE);
                showAllDescription.setOnClickListener(new View.OnClickListener() {


                    @Override
                    public void onClick(View v) {

                        if (collapsed) {
                            collapsed = false;
                            description.setMaxLines(Integer.MAX_VALUE);
                            TypedValue outValue = new TypedValue();
                            getActivity().getTheme().resolveAttribute(R.attr.icCollapseDrawable, outValue, true);
                            showAllDescription.setCompoundDrawablesWithIntrinsicBounds(outValue.resourceId, 0, 0, 0);
                            showAllDescription.setText(getString(R.string.show_less));
                        } else {
                            collapsed = true;
                            TypedValue outValue = new TypedValue();
                            getActivity().getTheme().resolveAttribute(R.attr.icExpandDrawable, outValue, true);
                            showAllDescription.setCompoundDrawablesWithIntrinsicBounds(outValue.resourceId, 0, 0, 0);
                            description.setMaxLines(10);
//                            scroller.scrollTo(0, scrollPosition);
                            showAllDescription.setText(getString(R.string.show_more));
                        }
                    }
                });
                descriptionContainer.setOnClickListener(new View.OnClickListener() {


                    @Override
                    public void onClick(View v) {

                        if (collapsed) {
                            collapsed = false;
                            description.setMaxLines(Integer.MAX_VALUE);
                            TypedValue outValue = new TypedValue();
                            getActivity().getTheme().resolveAttribute(R.attr.icCollapseDrawable, outValue, true);
                            showAllDescription.setCompoundDrawablesWithIntrinsicBounds(outValue.resourceId, 0, 0, 0);
                            showAllDescription.setText(getString(R.string.show_less));
                        } else {
                            collapsed = true;
                            TypedValue outValue = new TypedValue();
                            getActivity().getTheme().resolveAttribute( R.attr.icExpandDrawable, outValue, true );
                            showAllDescription.setCompoundDrawablesWithIntrinsicBounds(outValue.resourceId, 0, 0, 0);
                            description.setMaxLines(10);
                            showAllDescription.setText(getString(R.string.show_more));
                        }
                    }
                });

            }

            if(event.getDescription() != null){
                row2.startAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in));
                row3.startAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in));
                row2.setVisibility(View.VISIBLE);
                row3.setVisibility(View.VISIBLE);
                detailsContainer.setVisibility(View.VISIBLE);
                detailsContainer.startAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in));
                loadingPb.setVisibility(View.GONE);
                loadingPb.startAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_out));
            }


            MultiStoreItem[] items = event.getOtherVersions();

            if(items != null) {
                StoreSpinnerAdapter adapter = new StoreSpinnerAdapter(getActivity(), items);

                spinner.setAdapter(adapter);
                spinner.post(new Runnable() {
                    @Override
                    public void run() {
                        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                spinner.setOnItemSelectedListener(null);
                                MultiStoreItem item = (MultiStoreItem) parent.getAdapter().getItem(position);
                                BusProvider.getInstance().post(new OnMultiVersionClick(item.getName(), item.getPackageName(), item.getVersion(), item.getVersionCode()));
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {

                            }
                        });
                    }
                });

            }


            publisher.setText(Html.fromHtml("<b>" + getString(R.string.publisher) + "</b>" + ": " + event.getPublisher()));
            size.setText(Html.fromHtml("<b>" + getString(R.string.size) + "</b>" + ": " + AptoideUtils.formatBytes(event.getSize())));
            if(event.getStore()!=null){
                store.setVisibility(View.VISIBLE);
                store.setText(Html.fromHtml("<b>" + getString(R.string.store) + "</b>" + ": "));
            }else{
                store.setVisibility(View.INVISIBLE);
            }

            downloads.setText(Html.fromHtml("<b>" + getString(R.string.downloads) + "</b>" + ": " + withSuffix(String.valueOf(event.getDownloads()))));
            rating.setRating(event.getRating());
            rating.setOnRatingBarChangeListener(null);
            rating.setVisibility(View.VISIBLE);
            likes.setText(Html.fromHtml("<b>" + getString(R.string.likes) + "</b>" +  ": " + event.getLikes()));
            dontLikes.setText(Html.fromHtml("<b>" + getString(R.string.dont_likes) + "</b>" + ": " + event.getDontLikes()));

            if(event.getDeveloper() != null){
                publisherContainer.setVisibility(View.VISIBLE);

                if(((AppViewActivity)getActivity()).isUpdate()){
                    if(event.getNews().length()>0){
                        whatsNewContainer.setVisibility(View.VISIBLE);
                        whatsNew.setText(event.getNews());
                    }
                }

                publisherContainer.setVisibility(View.VISIBLE);


                String email;
                if(event.getDeveloper().getInfo().getEmail()!=null){
                    email=getString(R.string.username) +": " + event.getDeveloper().getInfo().getEmail();
                }else{
                    email=getString(R.string.username) +": " + getString(R.string.not_found);
                }
                publisherEmail.setText(email);

                String privacyPolicy;
                if(event.getDeveloper().getInfo().getPrivacy_policy()!=null){
                    privacyPolicy=getString(R.string.privacy_policy) +": " + event.getDeveloper().getInfo().getPrivacy_policy();
                }else{
                    privacyPolicy=getString(R.string.privacy_policy) +": " + getString(R.string.not_found);
                }
                publisherPrivacyPolicy.setText(privacyPolicy);

                String website;
                if(event.getDeveloper().getInfo().getWebsite()!=null){
                    website=getString(R.string.website) +": " + event.getDeveloper().getInfo().getWebsite();
                }else{
                    website=getString(R.string.website) +": " + getString(R.string.not_found);
                }
                publisherWebsite.setText(website);
            }

            LinearLayout mainLayout = (LinearLayout) getView().findViewById(R.id.layout_screenshots).findViewById(R.id._linearLayout);

            mainLayout.removeAllViews();
            ArrayList<MediaObject> mediaObjects;
            View cell;

            if (event.getScreenshotsAndThumbVideo() != null){
                mediaObjects = event.getScreenshotsAndThumbVideo();
                Log.d("FragmentAppView","media objects "+ Arrays.toString(mediaObjects.toArray()));
                String imagePath = "";
                DisplayImageOptions options = new DisplayImageOptions.Builder()
                        .showImageForEmptyUri(android.R.drawable.sym_def_app_icon)
                        .cacheOnDisc(true)
                        .build();
                int screenshotIndexToAdd = 0;
                for (int i=0; i!=mediaObjects.size(); i++) {
                    cell = getActivity().getLayoutInflater().inflate(R.layout.row_item_screenshots_gallery, null);
                    final ImageView imageView = (ImageView) cell.findViewById(R.id.screenshot_image_item);
                    final ProgressBar progress = (ProgressBar) cell.findViewById(R.id.screenshot_loading_item);
                    final ImageView play = (ImageView) cell.findViewById(R.id.play_button);
                    final FrameLayout mediaLayout = (FrameLayout) cell.findViewById(R.id.media_layout);

                    if(mediaObjects.get(i) instanceof Video){
                        screenshotIndexToAdd++;
                        imagePath = mediaObjects.get(i).getImageUrl();
                        Log.d("FragmentAppView", "VIDEOIMAGEPATH: " + imagePath);
                        mediaLayout.setForeground(getResources().getDrawable(R.color.overlay_black));
                        play.setVisibility(View.VISIBLE);
                        imageView.setOnClickListener(new VideoListener(getActivity(), ((Video) mediaObjects.get(i)).getVideoUrl()));
                        Log.d("FragmentAppView", "VIDEOURL: " + ((Video) mediaObjects.get(i)).getVideoUrl());
                        options = new DisplayImageOptions.Builder()
                                .showImageForEmptyUri(android.R.drawable.sym_def_app_icon)
                                .cacheOnDisc(false)
                                .build();

                    } else if (mediaObjects.get(i) instanceof Screenshot) {
                        options = new DisplayImageOptions.Builder()
                                .showImageForEmptyUri(android.R.drawable.sym_def_app_icon)
                                .cacheOnDisc(true)
                                .build();
                        imagePath = AptoideUtils.screenshotToThumb(getActivity(), mediaObjects.get(i).getImageUrl(), ((Screenshot) mediaObjects.get(i)).getOrient());
                        Log.d("FragmentAppView", "IMAGEPATH: " + imagePath);
                        imageView.setOnClickListener(new ScreenShotsListener(getActivity(), new ArrayList<String>(event.getScreenshots()), i - screenshotIndexToAdd));
                    }

                    mainLayout.addView(cell);
                    ImageLoader.getInstance().displayImage(imagePath, imageView, options, new ImageLoadingListener() {

                        @Override
                        public void onLoadingStarted(String uri, View v) {
                            progress.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onLoadingFailed(String uri, View v, FailReason failReason) {
                            imageView.setImageResource(android.R.drawable.ic_delete);
                            progress.setVisibility(View.GONE);
                            Log.d("onLoadingFailed", "Failed to load screenshot " + failReason.getCause());
                        }

                        @Override
                        public void onLoadingComplete(String uri, View v, Bitmap loadedImage) {
                            progress.setVisibility(View.GONE);
                        }

                        @Override
                        public void onLoadingCancelled(String uri, View v) {
                        }
                    });

                }

            }

        }

        public static class ScreenShotsListener implements View.OnClickListener {

            private Context context;
            private final int position;
            private ArrayList<String> urls;

            public ScreenShotsListener(Context context, ArrayList<String> urls, int position) {
                this.context = context;
                this.position = position;
                this.urls = urls;
            }

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ScreenshotsViewer.class);
                intent.putStringArrayListExtra("url", urls);
                intent.putExtra("position", position);
                context.startActivity(intent);
            }
        }

        public static class VideoListener implements View.OnClickListener {

            private Context context;
            private String videoUrl;

            public VideoListener(Context context, String videoUrl) {
                this.context = context;
                this.videoUrl = videoUrl;
            }

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl));
                context.startActivity(intent);
            }
        }


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_app_view_details, container, false);

            description = (TextView) v.findViewById(R.id.descript);
            showAllDescription= (TextView) v.findViewById(R.id.show_all_description);
            descriptionContainer = (LinearLayout) v.findViewById(R.id.description_container);
            layoutInfoDetails = (RelativeLayout) v.findViewById(R.id.layout_info_details);
            store = (TextView) layoutInfoDetails.findViewById(R.id.store_label);
            downloads = (TextView) layoutInfoDetails.findViewById(R.id.downloads_label);
            rating = (RatingBar) layoutInfoDetails.findViewById(R.id.rating_label);
            likes = (TextView) layoutInfoDetails.findViewById(R.id.likes_label);
            dontLikes = (TextView) layoutInfoDetails.findViewById(R.id.dont_likes_label);
            size = (TextView) layoutInfoDetails.findViewById(R.id.size_label);
            publisher = (TextView) layoutInfoDetails.findViewById(R.id.publisher_label);


            publisherContainer = v.findViewById(R.id.publisher_container);
            publisherWebsite = (TextView) v.findViewById(R.id.publisher_website);
            publisherEmail = (TextView) v.findViewById(R.id.publisher_email);
            publisherPrivacyPolicy = (TextView) v.findViewById(R.id.publisher_privacy_policy);
            whatsNew = (TextView) v.findViewById(R.id.whats_new_descript);
            whatsNewContainer = v.findViewById(R.id.whats_new_container);
            detailsContainer = (LinearLayout) v.findViewById(R.id.detailsContainer);
            loadingPb = (ProgressBar) v.findViewById(R.id.loadingPb);

            row2 = v.findViewById(R.id.row2);
            row3 = v.findViewById(R.id.row3);

            spinner = (Spinner) v.findViewById(R.id.store_spinner);

            return v;
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

        }

    }

    public static class FragmentAppViewRelated extends ListFragment {


        private RelatedBucketAdapter multiVersionAdapter;
        private RelatedBucketAdapter develBasedAdapter;
        private RelatedBucketAdapter itemBasedAdapter;
        private MergeAdapter adapter;
        private List<RelatedApkJson.Item> itemBasedElements = new ArrayList<RelatedApkJson.Item>();
        private List<RelatedApkJson.Item> develBasedElements = new ArrayList<RelatedApkJson.Item>();
        private List<RelatedApkJson.Item> multiVersionElements = new ArrayList<RelatedApkJson.Item>();

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Log.d("FragmentRelated", "onCreate");
            adapter = new MergeAdapter();
            itemBasedAdapter = new RelatedBucketAdapter(getActivity(), itemBasedElements);
            develBasedAdapter = new RelatedBucketAdapter(getActivity(), develBasedElements);
            multiVersionAdapter = new RelatedBucketAdapter(getActivity(), multiVersionElements);
        }

        SpiceManager spiceManager = new SpiceManager(HttpClientSpiceService.class);

        RequestListener<RelatedApkJson> request = new RequestListener<RelatedApkJson>() {
            @Override
            public void onRequestFailure(SpiceException spiceException) {
                final View errorView = LayoutInflater.from(getActivity()).inflate(R.layout.page_error, null);
                Button errorBtn = (Button) errorView.findViewById(R.id.errorButton);

                errorBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getListView().removeHeaderView(errorView);
                        setListAdapter(null);
                        ListRelatedApkRequest listRelatedApkRequest = new ListRelatedApkRequest(getActivity());

                        listRelatedApkRequest.setVercode(((AppViewActivity) getActivity()).getVersionCode());
                        listRelatedApkRequest.setLimit(develBasedAdapter.getBucketSize());
                        listRelatedApkRequest.setPackageName(((AppViewActivity) getActivity()).getPackage_name());

                        spiceManager.execute(listRelatedApkRequest, ((AppViewActivity) getActivity()).getPackage_name() + "-related", DurationInMillis.ONE_DAY, request);

                    }
                });



                setEmptyText(getString(R.string.connection_error));
                setListAdapter(new ArrayAdapter<String>(getActivity(), 0));

            }

            @Override
            public void onRequestSuccess(RelatedApkJson relatedApkJson) {
                setEmptyText(getString(R.string.no_related));

                if(relatedApkJson == null){
                    Log.d("FragmentRelated", "Related was null");
                    return;
                }
                Log.d("FragmentRelated", "onRequestSuccess");

                //Toast.makeText(getActivity(), "ItemBased size " + relatedApkJson.getItembased().size(), Toast.LENGTH_SHORT).show();
                //Toast.makeText(getActivity(), "DevelBased size " + relatedApkJson.getDevelbased().size(), Toast.LENGTH_SHORT).show();
                //Toast.makeText(getActivity(), "MultiVersion size " + relatedApkJson.getMultiversion().size(), Toast.LENGTH_SHORT).show();


                if(relatedApkJson.getItembased() != null && relatedApkJson.getItembased().size()>0){
                    Log.d("FragmentRelated", "itembased: "+ Arrays.toString(relatedApkJson.getItembased().toArray()));

                    itemBasedElements.clear();
                    if (PreferenceManager.getDefaultSharedPreferences(Aptoide.getContext()).getBoolean("matureChkBox",true)){


                        for (RelatedApkJson.Item item : relatedApkJson.getItembased()) {
                            if(!item.getAge().equals("Mature")){
                                itemBasedElements.add(item);
                            }
                        }

                    }else{
                        itemBasedElements.addAll(relatedApkJson.getItembased());
                    }
                    View v = LayoutInflater.from(getActivity()).inflate(R.layout.separator_frag_related, null);
                    ((TextView)v.findViewById(R.id.separator_label)).setText(getString(R.string.related_apps));
                    v.findViewById(R.id.more).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(getActivity(), MoreRelatedActivity.class);
                            i.putExtra("item", true);
                            i.putExtra("packageName", ((AppViewActivity)getActivity()).getPackage_name());
                            i.putExtra("versionCode", ((AppViewActivity)getActivity()).getVersionCode());
                            i.putExtra("appName", ((AppViewActivity)getActivity()).getName());
                            startActivity(i);
                        }
                    });
                    adapter.addView(v);
                    adapter.addAdapter(itemBasedAdapter);
                }

                if(relatedApkJson.getDevelbased() != null && relatedApkJson.getDevelbased().size()>0){
                    develBasedElements.clear();
                    if (PreferenceManager.getDefaultSharedPreferences(Aptoide.getContext()).getBoolean("matureChkBox", true)){


                        for (RelatedApkJson.Item item : relatedApkJson.getDevelbased()) {
                            if(!item.getAge().equals("Mature")){
                                develBasedElements.add(item);
                            }
                        }

                    }else{
                        develBasedElements.addAll(relatedApkJson.getDevelbased());
                    }

                    View v = LayoutInflater.from(getActivity()).inflate(R.layout.separator_frag_related, null);
                    ((TextView)v.findViewById(R.id.separator_label)).setText(getString(R.string.more_from_publisher));
                    v.findViewById(R.id.more).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(getActivity(), MoreRelatedActivity.class);
                            i.putExtra("developer", true);
                            i.putExtra("packageName", ((AppViewActivity)getActivity()).getPackage_name());
                            i.putExtra("versionCode", ((AppViewActivity)getActivity()).getVersionCode());
                            i.putExtra("appName", ((AppViewActivity)getActivity()).getName());
                            startActivity(i);
                        }
                    });
                    adapter.addView(v);
                    adapter.addAdapter(develBasedAdapter);
                }

                if(relatedApkJson.getMultiversion()!=null && relatedApkJson.getMultiversion().size()>0){
                    multiVersionElements.clear();

                    if (PreferenceManager.getDefaultSharedPreferences(Aptoide.getContext()).getBoolean("matureChkBox",true)){


                        for (RelatedApkJson.Item item : relatedApkJson.getMultiversion()) {
                            if(!item.getAge().equals("Mature")){
                                multiVersionElements.add(item);
                            }
                        }


                    }else{
                        multiVersionElements.addAll(relatedApkJson.getMultiversion());
                    }

                    View v = LayoutInflater.from(getActivity()).inflate(R.layout.separator_frag_related, null);
                    ((TextView)v.findViewById(R.id.separator_label)).setText(getString(R.string.multiversion));
                    v.findViewById(R.id.more).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(getActivity(), MoreRelatedActivity.class);
                            i.putExtra("version", true);
                            i.putExtra("packageName", ((AppViewActivity)getActivity()).getPackage_name());
                            i.putExtra("versionCode", ((AppViewActivity)getActivity()).getVersionCode());
                            i.putExtra("appName", ((AppViewActivity)getActivity()).getName());
                            startActivity(i);
                        }
                    });
                    adapter.addView(v);
                    adapter.addAdapter(multiVersionAdapter);
                }

                itemBasedAdapter.notifyDataSetChanged();
                develBasedAdapter.notifyDataSetChanged();
                multiVersionAdapter.notifyDataSetChanged();

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
            if(spiceManager.isStarted()){
                spiceManager.shouldStop();
            }
        }


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            View v = super.onCreateView(inflater, container, savedInstanceState);
            ListRelatedApkRequest listRelatedApkRequest = new ListRelatedApkRequest(getActivity());
            Log.d("FragmentRelated", "onCreateView");

            if(((AppViewActivity)getActivity()).isMultipleStores()){
                listRelatedApkRequest.setRepos(((AppViewActivity)getActivity()).getRepoName());
            }

            listRelatedApkRequest.setVercode(((AppViewActivity)getActivity()).getVersionCode());
            listRelatedApkRequest.setLimit(develBasedAdapter.getBucketSize());
            listRelatedApkRequest.setPackageName(((AppViewActivity)getActivity()).getPackage_name());
            spiceManager.execute(listRelatedApkRequest,((AppViewActivity)getActivity()).getPackage_name() + "-related", DurationInMillis.ONE_DAY, request);
            return v;
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            getListView().setDivider(null);
            getListView().setCacheColorHint(getResources().getColor(android.R.color.transparent));

            Log.d("FragmentRelated", "onViewCreated");


        }
    }

    public static class FragmentAppViewSpecs extends FragmentAppView{

        private LinearLayout permissionsContainer;
        private TextView min_sdk;
        private TextView min_screen;
        private AsyncTask<ArrayList<String>, Void, ArrayList<ApkPermission>> task;
        private View loadingPb;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_specifications, container, false);

            permissionsContainer = (LinearLayout) v.findViewById(R.id.permissionsContainer);
            min_sdk = (TextView) v.findViewById(R.id.min_sdk);
            min_screen = (TextView) v.findViewById(R.id.min_screen);
            loadingPb = v.findViewById(R.id.loadingPb);
            return v;
        }

        @Override
        public void onStop() {
            super.onStop();
            Log.d("Aptoide-AppView-Permissions", "On Stop");
            if (task != null) {
                Log.d("Aptoide-AppView-Permissions", "Canceling task " + System.identityHashCode(task));
                task.cancel(true);
            }
        }



        @Subscribe
        public void refreshDetails(final AppViewActivity.SpecsEvent event) {


            if (event.getPermissions() != null) {

                min_sdk.setText(getString(R.string.min_sdk) + ": " + event.getMinSdk());
                min_screen.setText(getString(R.string.min_screen) + ": " + event.getMinScreen().name());
                if(task!=null){
                    task.cancel(true);
                }
                task = new PermissionGetter().execute(event.getPermissions());

            }



        }

        public class PermissionGetter extends AsyncTask<ArrayList<String>, Void, ArrayList<ApkPermission>>{

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                setRetainInstance(true);
            }

            @Override
            protected ArrayList<ApkPermission> doInBackground(ArrayList<String>... params) {
                return PermissionsActivity.permissions(getActivity(), params[0]);
            }

            @Override
            protected void onPostExecute(ArrayList<ApkPermission> apkPermissions) {
                super.onPostExecute(apkPermissions);
                Log.d("Aptoide-AppView-Permissions", "onPostExecute " + System.identityHashCode(task));
                if(apkPermissions.size()==0){
                    TextView noPermissions = new TextView(getActivity());
                    noPermissions.setText(getString(R.string.no_permissions_required));
                    noPermissions.setPadding(5,5,5,5);
                    permissionsContainer.addView(noPermissions);
                }
                if(getActivity()!=null){
                    FillPermissions.fillPermissions(getActivity(), permissionsContainer, apkPermissions);
                    permissionsContainer.startAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in));
                    permissionsContainer.setVisibility(View.VISIBLE);
                    loadingPb.startAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_out));
                    loadingPb.setVisibility(View.GONE);
                    setRetainInstance(false);
                }
            }
        }

        public static class FillPermissions {

            public static void fillPermissions(Context context, LinearLayout permissionsContainer, ArrayList<ApkPermission> permissions) {

                View v;

                if (permissionsContainer != null) {

                    for (ApkPermission permission : permissions) {

                        v = LayoutInflater.from(context).inflate(R.layout.row_permission, permissionsContainer, false);

                        TextView name = (TextView) v.findViewById(R.id.permission_name);
                        TextView description = (TextView) v.findViewById(R.id.permission_description);

                        name.setText(permission.getName());
                        description.setText(permission.getDescription());
                        permissionsContainer.addView(v);
                    }

                }
            }
        }
    }

    public static class FragmentAppViewRating extends FragmentAppView {

        private TextView commentsTitle;
        private LinearLayout commentsLayout;
        private TextView noComments;
        private LinearLayout commentsContainer;
        private Button seeAllButton;
        private EditText editText;
        private Button addComment;
        private Button dontLikeBtn;
        private Button likeBtn;
        private View loadingPb;

        @Subscribe
        public void refreshDetails(final AppViewActivity.RatingEvent event) {
            Log.d("Aptoide-AppView", "getting event");

            if(event.getComments() != null) {
                FillComments.fillComments(getActivity(), commentsContainer, event.getComments());

                if (event.getComments().size() == 0) {
                    commentsLayout.setVisibility(View.GONE);
                    noComments.startAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in));
                    noComments.setVisibility(View.VISIBLE);
                }else if (event.getComments().size() > 4) {
                    noComments.setVisibility(View.GONE);
                    commentsTitle.setVisibility(View.VISIBLE);
                    commentsLayout.setVisibility(View.VISIBLE);
                    commentsLayout.startAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in));
                    seeAllButton.setVisibility(View.VISIBLE);
                    seeAllButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(getActivity(), AllCommentsActivity.class);
                            intent.putExtra("repoName", ((AppViewActivity) getActivity()).getRepoName());
                            intent.putExtra("versionName", ((AppViewActivity) getActivity()).getVersionName());
                            intent.putExtra("packageName", ((AppViewActivity) getActivity()).getPackage_name());
                            startActivity(intent);
                        }
                    });
                }else{
                    commentsLayout.startAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in));
                    commentsLayout.setVisibility(View.VISIBLE);
                }
                if(event.getUservote()!=null){

                    if(event.getUservote().equals("like")){
                        likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_action_good_pressed, 0, 0, 0);

                        TypedValue outValue = new TypedValue();
                        getActivity().getTheme().resolveAttribute( R.attr.icRatingBadDrawable, outValue, true );

                        dontLikeBtn.setCompoundDrawablesWithIntrinsicBounds(outValue.resourceId, 0, 0, 0);
                    }else if(event.getUservote().equals("dislike")){
                        dontLikeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_action_bad_pressed, 0,0,0);
                        TypedValue outValue = new TypedValue();
                        getActivity().getTheme().resolveAttribute( R.attr.icRatingBadDrawable, outValue, true );
                        likeBtn.setCompoundDrawablesWithIntrinsicBounds(outValue.resourceId, 0,0,0);
                    }

                }

                loadingPb.startAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_out));
                loadingPb.setVisibility(View.GONE);

            }




        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            View v = inflater.inflate(R.layout.fragment_app_rating, container, false);

            commentsTitle = (TextView) v.findViewById(R.id.title_comments);
            commentsLayout = (LinearLayout) v.findViewById(R.id.layout_comments);
            noComments = (TextView) v.findViewById(R.id.no_comments);
            commentsContainer = (LinearLayout) v.findViewById(R.id.commentContainer);
            seeAllButton = (Button) v.findViewById(R.id.more_comments);
            editText = (EditText) v.findViewById(R.id.editText_addcomment);
            addComment = (Button) v.findViewById(R.id.button_add_comment);
            dontLikeBtn = (Button) v.findViewById(R.id.button_dont_like);
            dontLikeBtn.setOnClickListener(new AddLikeListener(false));
            likeBtn = (Button) v.findViewById(R.id.button_like);
            likeBtn.setOnClickListener(new AddLikeListener(true));
            addComment.setOnClickListener(new AddCommentListener());
            loadingPb = v.findViewById(R.id.loadingPb);

            return v;

        }
        public class AddLikeListener implements View.OnClickListener {

            private final boolean isLike;
            RequestListener<GenericResponse> requestListener = new RequestListener<GenericResponse>() {
                @Override
                public void onRequestFailure(SpiceException spiceException) {
                    Toast.makeText(Aptoide.getContext(), getString(R.string.error_occured), Toast.LENGTH_LONG).show();
                    ProgressDialogFragment pd = (ProgressDialogFragment) getFragmentManager().findFragmentByTag("pleaseWaitDialog");
                    if(pd!=null && pd.isAdded()) pd.dismiss();
                }

                @Override
                public void onRequestSuccess(GenericResponse genericResponse) {

                    ProgressDialogFragment pd = (ProgressDialogFragment) getFragmentManager().findFragmentByTag("pleaseWaitDialog");
                    if(pd!=null && pd.isAdded()){
                        pd.dismiss();
                    }


                    if(genericResponse.getStatus().equals("OK")){
                        Toast.makeText(Aptoide.getContext(), getString(R.string.opinion_success), Toast.LENGTH_LONG).show();
                        manager.removeDataFromCache(GetApkInfoJson.class, ((AppViewActivity)getActivity()).getCacheKey());
                        BusProvider.getInstance().post(new AppViewRefresh());
                    }else{
                        for(String error :  genericResponse.getErrors()){
                            Toast.makeText(getActivity(), error, Toast.LENGTH_LONG).show();
                        }
                    }

                }
            };
            private SpiceManager manager;

            public AddLikeListener(boolean isLike) {
                this.isLike = isLike;
            }


            @Override
            public void onClick(View v) {
                final AccountManager manager = AccountManager.get(getActivity());

                if (manager.getAccountsByType(Aptoide.getConfiguration().getAccountType()).length > 0) {
                    addLike();
                } else {
                    manager.addAccount(Aptoide.getConfiguration().getAccountType(), AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS, null, null, getActivity(), new AccountManagerCallback<Bundle>() {
                        @Override
                        public void run(AccountManagerFuture<Bundle> future) {

                            if (LoginActivity.isLoggedIn(getActivity())) {
                                Account account = manager.getAccountsByType(Aptoide.getConfiguration().getAccountType())[0];
                                manager.getAuthToken(account, AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS, null, getActivity(), new AccountManagerCallback<Bundle>() {
                                    @Override
                                    public void run(AccountManagerFuture<Bundle> future) {
                                        try {
                                            ((AppViewActivity) getActivity()).setToken(future.getResult().getString(AccountManager.KEY_AUTHTOKEN));
                                        } catch (OperationCanceledException e) {
                                            e.printStackTrace();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        } catch (AuthenticatorException e) {
                                            e.printStackTrace();
                                        }

                                    }
                                }, null);
                            }


                        }
                    }, null);
                }

            }

            private void addLike() {
                manager = ((AppViewActivity)getActivity()).getSpice();

                AddLikeRequest request = new AddLikeRequest(getActivity());
                request.setApkversion(((AppViewActivity)getActivity()).getVersionName());
                request.setPackageName(((AppViewActivity) getActivity()).getPackage_name());
                request.setRepo(((AppViewActivity) getActivity()).getRepoName());
                request.setToken(((AppViewActivity) getActivity()).getToken());
                request.setLike(isLike);


                manager.execute(request, requestListener);
                AptoideDialog.pleaseWaitDialog().show(getFragmentManager(), "pleaseWaitDialog");
            }
        }

        public class AddCommentListener implements View.OnClickListener {

            RequestListener<GenericResponse> requestListener = new RequestListener<GenericResponse>() {
                @Override
                public void onRequestFailure(SpiceException spiceException) {
                    Toast.makeText(getActivity(), getString(R.string.error_occured), Toast.LENGTH_LONG).show();
                    ProgressDialogFragment pd = (ProgressDialogFragment) getFragmentManager().findFragmentByTag("pleaseWaitDialog");
                    if(pd!=null){
                        pd.dismissAllowingStateLoss();
                    }

                }

                @Override
                public void onRequestSuccess(GenericResponse genericResponse) {

                    ProgressDialogFragment pd = (ProgressDialogFragment) getFragmentManager().findFragmentByTag("pleaseWaitDialog");
                    if(pd!=null){
                        pd.dismissAllowingStateLoss();
                    }


                    if(genericResponse.getStatus().equals("OK")){
                        Toast.makeText(getActivity(), getString(R.string.comment_submitted), Toast.LENGTH_LONG).show();
                        editText.setText("");
                        editText.setEnabled(false);
                        editText.setEnabled(true);
                        manager.removeDataFromCache(GetApkInfoJson.class, ((AppViewActivity)getActivity()).getCacheKey());
                        BusProvider.getInstance().post(new AppViewRefresh());
                    }else{
                        for(String error :  genericResponse.getErrors()){
                            Toast.makeText(getActivity(), error, Toast.LENGTH_LONG).show();
                        }
                    }

                }
            };
            private SpiceManager manager;


            @Override
            public void onClick(View v) {


                final AccountManager manager = AccountManager.get(getActivity());

                if (manager.getAccountsByType(Aptoide.getConfiguration().getAccountType()).length > 0) {
                    addComment();
                } else {

                    manager.addAccount(Aptoide.getConfiguration().getAccountType(), AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS, null, null, getActivity(), new AccountManagerCallback<Bundle>() {
                        @Override
                        public void run(AccountManagerFuture<Bundle> future) {
                            if (LoginActivity.isLoggedIn(getActivity())) {

                                Account account = manager.getAccountsByType(Aptoide.getConfiguration().getAccountType())[0];
                                manager.getAuthToken(account, AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS, null, getActivity(), new AccountManagerCallback<Bundle>() {
                                    @Override
                                    public void run(AccountManagerFuture<Bundle> future) {
                                        try {

                                            ((AppViewActivity) getActivity()).setToken(future.getResult().getString(AccountManager.KEY_AUTHTOKEN));
                                            //addComment();
                                        } catch (OperationCanceledException e) {
                                            e.printStackTrace();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        } catch (AuthenticatorException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }, null);
                            }
                        }
                    }, null);
                }
            }

            private void addComment() {


                if (!PreferenceManager.getDefaultSharedPreferences(Aptoide.getContext()).getString("username", "NOT_SIGNED_UP").equals("NOT_SIGNED_UP")) {
                    manager = ((AppViewActivity) getActivity()).getSpice();

                    AddCommentRequest request = new AddCommentRequest(getActivity());
                    request.setApkversion(((AppViewActivity) getActivity()).getVersionName());
                    request.setPackageName(((AppViewActivity) getActivity()).getPackage_name());
                    request.setRepo(((AppViewActivity) getActivity()).getRepoName());
                    request.setToken(((AppViewActivity) getActivity()).getToken());

                    request.setText(editText.getText().toString());

                    manager.execute(request, requestListener);
                    AptoideDialog.pleaseWaitDialog().show(getFragmentManager(), "pleaseWaitDialog");
                } else {

                    AptoideDialog.updateUsernameDialog().show(getFragmentManager(), "updateNameDialog");

                }
            }

        }

        public static class FillComments{

            public static void fillComments(Context context, LinearLayout commentsContainer, ArrayList<Comment> comments) {
                final DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(context);
                final DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(context);

                final SimpleDateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                View v;
                commentsContainer.removeAllViews();

                for(Comment comment : comments){

                    v = LayoutInflater.from(context).inflate(R.layout.row_comment, commentsContainer, false);

                    TextView content = (TextView) v.findViewById(R.id.content);
                    TextView dateTv = (TextView) v.findViewById(R.id.date);
                    TextView author = (TextView) v.findViewById(R.id.author);

                    content.setText(comment.getText());

                    try {
                        dateTv.setText(AptoideUtils.DateTimeUtils.getInstance(context).getTimeDiffString(dateFormater.parse(comment.getTimestamp()).getTime()));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    author.setText(comment.getUsername());
                    commentsContainer.addView(v);
                }

            }
        }


    }




}

