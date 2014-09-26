package cm.aptoide.ptdev.fragments;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.commonsware.cwac.merge.MergeAdapter;
import com.flurry.android.FlurryAgent;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.squareup.otto.Subscribe;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cm.aptoide.ptdev.AllCommentsActivity;
import cm.aptoide.ptdev.AppViewActivity;
import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.MoreRelatedActivity;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.ScreenshotsViewer;
import cm.aptoide.ptdev.VeredictReview;
import cm.aptoide.ptdev.adapters.RelatedBucketAdapter;
import cm.aptoide.ptdev.adapters.StoreSpinnerAdapter;
import cm.aptoide.ptdev.configuration.AccountGeneral;
import cm.aptoide.ptdev.dialogs.AptoideDialog;
import cm.aptoide.ptdev.dialogs.ProgressDialogFragment;
import cm.aptoide.ptdev.downloadmanager.PermissionsActivity;
import cm.aptoide.ptdev.events.AppViewRefresh;
import cm.aptoide.ptdev.events.BusProvider;
import cm.aptoide.ptdev.events.OnMultiVersionClick;
import cm.aptoide.ptdev.fragments.callbacks.AddCommentCallback;
import cm.aptoide.ptdev.fragments.callbacks.SuccessfullyPostCallback;
import cm.aptoide.ptdev.model.ApkPermission;
import cm.aptoide.ptdev.model.Comment;
import cm.aptoide.ptdev.model.MediaObject;
import cm.aptoide.ptdev.model.MultiStoreItem;
import cm.aptoide.ptdev.model.Screenshot;
import cm.aptoide.ptdev.model.Video;
import cm.aptoide.ptdev.services.HttpClientSpiceService;
import cm.aptoide.ptdev.utils.AptoideUtils;
import cm.aptoide.ptdev.webservices.AddLikeRequest;
import cm.aptoide.ptdev.webservices.ListRelatedApkRequest;
import cm.aptoide.ptdev.webservices.json.GetApkInfoJson;
import cm.aptoide.ptdev.webservices.json.RelatedApkJson;

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
        private boolean initializedView;

        @Subscribe
        public void refreshDetails(final AppViewActivity.DetailsEvent event) {
            //Log.d("Aptoide-AppView", "getting event");
            //Log.d("Aptoide-AppView", "Setting description");
            if(event == null) return;

            if (event.getDescription()!=null)
                description.setText(event.getDescription());

//            //Log.d("Aptoide-description", "lines "+ description.getLineCount() );
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
                            if(Build.VERSION.SDK_INT >= 10) FlurryAgent.logEvent("App_View_Expanded_Description");
                        } else {
                            collapsed = true;
                            TypedValue outValue = new TypedValue();
                            getActivity().getTheme().resolveAttribute(R.attr.icExpandDrawable, outValue, true);
                            showAllDescription.setCompoundDrawablesWithIntrinsicBounds(outValue.resourceId, 0, 0, 0);
                            description.setMaxLines(10);
//                            scroller.scrollTo(0, scrollPosition);
                            showAllDescription.setText(getString(R.string.show_more));
                            if(Build.VERSION.SDK_INT >= 10) FlurryAgent.logEvent("App_View_Colapsed_Description");
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
                            if(Build.VERSION.SDK_INT >= 10) FlurryAgent.logEvent("App_View_Clicked_On_Show_More_Description");
                        } else {
                            collapsed = true;
                            TypedValue outValue = new TypedValue();
                            getActivity().getTheme().resolveAttribute( R.attr.icExpandDrawable, outValue, true );
                            showAllDescription.setCompoundDrawablesWithIntrinsicBounds(outValue.resourceId, 0, 0, 0);
                            description.setMaxLines(10);
                            showAllDescription.setText(getString(R.string.show_more));
                            if(Build.VERSION.SDK_INT >= 10) FlurryAgent.logEvent("App_View_Clicked_On_Show_Less_Description");
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

                initializedView = false;

                spinner.setAdapter(adapter);

                        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                if(!initializedView){
                                    initializedView = true;
                                }else {
                                    spinner.setOnItemSelectedListener(null);
                                    MultiStoreItem item = (MultiStoreItem) parent.getAdapter().getItem(position);
                                    BusProvider.getInstance().post(new OnMultiVersionClick(item.getName(), item.getPackageName(), item.getVersion(), item.getVersionCode(), item.getDownloads()));
                                    if (Build.VERSION.SDK_INT >= 10)
                                        FlurryAgent.logEvent("App_View_Opened_Store_From_Spinner");
                                }
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {

                            }
                        });



            }


            publisher.setText(Html.fromHtml("<b>" + getString(R.string.publisher) + "</b>" + ": " + event.getPublisher()));
            size.setText(Html.fromHtml("<b>" + getString(R.string.size) + "</b>" + ": " + AptoideUtils.formatBytes(event.getSize())));

            if(((AppViewActivity)getActivity()).isMultipleStores()){

                if(event.getStore()!=null){
                    store.setVisibility(View.VISIBLE);
                    store.setText(Html.fromHtml("<b>" + getString(R.string.store) + "</b>" + ": "));
                }else{
                    store.setVisibility(View.INVISIBLE);
                }

            }else{
                spinner.setVisibility(View.GONE);
                if(event.getStore()!=null){
                    store.setVisibility(View.VISIBLE);
                    store.setText(Html.fromHtml("<b>" + getString(R.string.store) + "</b>" + ": "+event.getStore()));
                }else{
                    store.setVisibility(View.INVISIBLE);
                }
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
                //Log.d("FragmentAppView","media objects "+ Arrays.toString(mediaObjects.toArray()));
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
                        //Log.d("FragmentAppView", "VIDEOIMAGEPATH: " + imagePath);
                        mediaLayout.setForeground(getResources().getDrawable(R.color.overlay_black));
                        play.setVisibility(View.VISIBLE);
                        imageView.setOnClickListener(new VideoListener(getActivity(), ((Video) mediaObjects.get(i)).getVideoUrl()));
                        mediaLayout.setOnClickListener(new VideoListener(getActivity(), ((Video) mediaObjects.get(i)).getVideoUrl()));
                        //Log.d("FragmentAppView", "VIDEOURL: " + ((Video) mediaObjects.get(i)).getVideoUrl());
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
                        //Log.d("FragmentAppView", "IMAGEPATH: " + imagePath);
                        imageView.setOnClickListener(new ScreenShotsListener(getActivity(), new ArrayList<String>(event.getScreenshots()), i - screenshotIndexToAdd));
                        mediaLayout.setOnClickListener(new ScreenShotsListener(getActivity(), new ArrayList<String>(event.getScreenshots()), i - screenshotIndexToAdd));
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
                            //Log.d("onLoadingFailed", "Failed to load screenshot " + failReason.getCause());
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
                if(Build.VERSION.SDK_INT >= 10) FlurryAgent.logEvent("App_View_Clicked_On_Screenshot");
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
                if(Build.VERSION.SDK_INT >= 10) FlurryAgent.logEvent("App_View_Clicked_On_Video");
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
            //Log.d("FragmentRelated", "onCreate");
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
                    //Log.d("FragmentRelated", "Related was null");
                    return;
                }
                //Log.d("FragmentRelated", "onRequestSuccess");

                //Toast.makeText(getActivity(), "ItemBased size " + relatedApkJson.getItembased().size(), Toast.LENGTH_SHORT).show();
                //Toast.makeText(getActivity(), "DevelBased size " + relatedApkJson.getDevelbased().size(), Toast.LENGTH_SHORT).show();
                //Toast.makeText(getActivity(), "MultiVersion size " + relatedApkJson.getMultiversion().size(), Toast.LENGTH_SHORT).show();

                List<RelatedApkJson.Item> relaatedlist= relatedApkJson.getItembased();
                if(relaatedlist != null){

                    //Log.d("FragmentRelated", "items " +  " " + relatedApkJson.getItembased().toString());


                    if(relaatedlist.size()>0) {
                        //Log.d("FragmentRelated", "itembased: " + Arrays.toString(relatedApkJson.getItembased().toArray()));

                        itemBasedElements.clear();
                        if (PreferenceManager.getDefaultSharedPreferences(Aptoide.getContext()).getBoolean("matureChkBox", true)) {
                            for (RelatedApkJson.Item item : relatedApkJson.getItembased()) {
                                if (!item.getAge().equals("Mature")) {
                                    itemBasedElements.add(item);
                                }
                            }
                        } else {
                            itemBasedElements.addAll(relatedApkJson.getItembased());
                        }
                        View v = LayoutInflater.from(getActivity()).inflate(R.layout.separator_frag_related, null);
                        ((TextView) v.findViewById(R.id.separator_label)).setText(getString(R.string.related_apps));
                        v.findViewById(R.id.more).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if(Build.VERSION.SDK_INT >= 10) FlurryAgent.logEvent("App_View_Clicked_On_More_Related_Apps");
                                Intent i = new Intent(getActivity(), MoreRelatedActivity.class);
                                i.putExtra("item", true);
                                i.putExtra("packageName", ((AppViewActivity) getActivity()).getPackage_name());
                                i.putExtra("versionCode", ((AppViewActivity) getActivity()).getVersionCode());
                                i.putExtra("appName", ((AppViewActivity) getActivity()).getName());
                                i.putExtra("download_from", "app_view_related_apps");
                                startActivity(i);
                            }
                        });
                        adapter.addView(v);
                        adapter.addAdapter(itemBasedAdapter);
                    }
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
                            if(Build.VERSION.SDK_INT >= 10) FlurryAgent.logEvent("App_View_Clicked_On_More_From_Publisher");
                            Intent i = new Intent(getActivity(), MoreRelatedActivity.class);
                            i.putExtra("developer", true);
                            i.putExtra("packageName", ((AppViewActivity)getActivity()).getPackage_name());
                            i.putExtra("versionCode", ((AppViewActivity)getActivity()).getVersionCode());
                            i.putExtra("appName", ((AppViewActivity)getActivity()).getName());
                            i.putExtra("download_from", "app_view_more_from_publisher");
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
                            if(Build.VERSION.SDK_INT >= 10) FlurryAgent.logEvent("App_View_Clicked_On_More_Multiversion");
                            Intent i = new Intent(getActivity(), MoreRelatedActivity.class);
                            i.putExtra("version", true);
                            i.putExtra("packageName", ((AppViewActivity)getActivity()).getPackage_name());
                            i.putExtra("versionCode", ((AppViewActivity)getActivity()).getVersionCode());
                            i.putExtra("appName", ((AppViewActivity)getActivity()).getName());
                            i.putExtra("download_from","app_view_more_multiversion");
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
/*                if(getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    setListViewHeightBasedOnChildren(getListView());
                }*/
            }

        };
       /* private final void setListViewHeightBasedOnChildren(ListView listView) {

            ListAdapter listAdapter = listView.getAdapter();
            if (listAdapter == null){
                return;
            }

            if (listAdapter.getCount()<1){
                return;
            }

            int totalHeight = listView.getPaddingTop() + listView.getPaddingBottom();
            for (int i = 0; i < listAdapter.getCount(); i++) {
                View listItem = listAdapter.getView(i, null, listView);
                if (listItem instanceof ViewGroup)
                    listItem.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.WRAP_CONTENT, AbsListView.LayoutParams.WRAP_CONTENT));
                listItem.measure(0, 0);
                totalHeight += listItem.getMeasuredHeight();
            }

            ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
            listView.setLayoutParams(params);

        }*/


        @Override
        public void onStart() {
            super.onStart();
            BusProvider.getInstance().register(this);
            spiceManager.start(getActivity());
        }

        @Override
        public void onStop() {
            super.onStop();
            BusProvider.getInstance().unregister(this);
            if(spiceManager.isStarted()){
                spiceManager.shouldStop();
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = super.onCreateView(inflater, container, savedInstanceState);
            doPreStuff();

            return v;
        }

        @Subscribe
        public void refresh(AppViewActivity.RelatedEvent e){
            doPreStuff();
        }

        private final void doPreStuff(){
            ListRelatedApkRequest listRelatedApkRequest = new ListRelatedApkRequest(getActivity());

            if(!((AppViewActivity)getActivity()).isMultipleStores()){
                listRelatedApkRequest.setRepos(((AppViewActivity)getActivity()).getRepoName());
            }
            listRelatedApkRequest.setVercode(((AppViewActivity)getActivity()).getVersionCode());
            listRelatedApkRequest.setLimit(develBasedAdapter.getBucketSize());
            listRelatedApkRequest.setPackageName(((AppViewActivity)getActivity()).getPackage_name());
            String cacheKey = "portrait";
            if(getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
                cacheKey = "landscape";
            };
            spiceManager.execute(listRelatedApkRequest,((AppViewActivity)getActivity()).getPackage_name() + "-related" + cacheKey, DurationInMillis.ONE_DAY, request);
        }


        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            getListView().setDivider(null);
            getListView().setCacheColorHint(getResources().getColor(android.R.color.transparent));
            getListView().setItemsCanFocus(true);

            ////Log.d("FragmentRelated", "onViewCreated");
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
            ////Log.d("Aptoide-AppView-Permissions", "On Stop");
            if (task != null) {
                ////Log.d("Aptoide-AppView-Permissions", "Canceling task " + System.identityHashCode(task));
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
                //Log.d("Aptoide-AppView-Permissions", "onPostExecute " + System.identityHashCode(task));
                if(apkPermissions.size()==0){
                    TextView noPermissions = new TextView(getActivity());
                    noPermissions.setText(getString(R.string.no_permissions_required));
                    noPermissions.setPadding(5,5,5,5);
                    permissionsContainer.addView(noPermissions);
                }
                if(getActivity()!=null && permissionsContainer.getHeight()==0){
                    fillPermissions(getActivity(), permissionsContainer, apkPermissions);
                    permissionsContainer.startAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in));
                    permissionsContainer.setVisibility(View.VISIBLE);
                    loadingPb.startAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_out));
                    loadingPb.setVisibility(View.GONE);
                    setRetainInstance(false);
                }
            }

            private void fillPermissions(Context context, LinearLayout permissionsContainer, ArrayList<ApkPermission> permissions) {
                View v;
                String prevName=null;
                if (permissionsContainer != null) {

                    for (ApkPermission permission : permissions) {

                        v = LayoutInflater.from(context).inflate(R.layout.row_permission, permissionsContainer, false);
                        if(!permission.getName().equals(prevName)){
                            prevName=permission.getName();
                            TextView name = (TextView) v.findViewById(R.id.permission_name);
                            name.setText(permission.getName());
                        }
                        else
                            v.findViewById(R.id.permission_name).setVisibility(View.GONE);


                        TextView description = (TextView) v.findViewById(R.id.permission_description);

                        description.setText(permission.getDescription());
                        permissionsContainer.addView(v);
                    }

                }
            }
        }

        /*public static class FillPermissions {

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
        }*/
    }

    public static class FragmentAppViewRating extends FragmentAppView implements SuccessfullyPostCallback {

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
        private TextView goodVotes;
        private TextView licenseVotes;
        private TextView fakeVotes;
        private TextView freezeVotes;
        private TextView virusVotes;
        private TextView review;
        private Button flagThisApp;
        private LinearLayout flags_container;
        private ProgressBar loading_flags;
        private AddCommentCallback addCommentCallback;

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ////Log.d("likes","onAttach");
            addCommentCallback = (AddCommentCallback) activity;
            ((AppViewActivity) activity).setSuccessfullyPostCallback(this);
        }

        @Override
        public void onDetach() {
            super.onDetach();
            addCommentCallback = null;
            //Log.d("likes","onDetach");
            ((AppViewActivity) getActivity()).setSuccessfullyPostCallback(null);

        }

        @Subscribe
        public void refreshDetails(final AppViewActivity.RatingEvent event) {
            //Log.d("Aptoide-AppView", "getting event");

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
                            if(Build.VERSION.SDK_INT >= 10) FlurryAgent.logEvent("App_View_Opened_See_All_Comments_Button");
                            Intent intent = new Intent(getActivity(), AllCommentsActivity.class);
                            intent.putExtra("repoName", ((AppViewActivity) getActivity()).getRepoName());
                            intent.putExtra("versionName", ((AppViewActivity) getActivity()).getVersionName());
                            intent.putExtra("packageName", ((AppViewActivity) getActivity()).getPackage_name());
                            intent.putExtra("token", ((AppViewActivity) getActivity()).getToken());
                            getActivity().startActivityForResult(intent, 359);
                        }
                    });
                }else{
                    noComments.setVisibility(View.GONE);
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
                        getActivity().getTheme().resolveAttribute( R.attr.icRatingGoodDrawable, outValue, true );
                        likeBtn.setCompoundDrawablesWithIntrinsicBounds(outValue.resourceId, 0,0,0);
                    }

                }

                loadingPb.startAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_out));
                loadingPb.setVisibility(View.GONE);

            }

            if(event.getVeredict()!=null){
                loading_flags.setVisibility(View.GONE);
                flagThisApp.setVisibility(View.GONE);
                goodVotes.setVisibility(View.GONE);
                licenseVotes.setVisibility(View.GONE);
                fakeVotes.setVisibility(View.GONE);
                freezeVotes.setVisibility(View.GONE);
                virusVotes.setVisibility(View.GONE);

                flags_container.setVisibility(View.VISIBLE);

                int stringResource;
                int drawable=0;
                switch ( VeredictReview.reverseLookup( event.getVeredict().getFlag() )) {
                    case GOOD:
                        stringResource = VeredictReview.GOOD.getString();
                        drawable=R.drawable.ic_action_flag_good;
                        break;
                    case FAKE:
                        stringResource = VeredictReview.FAKE.getString();
                        drawable=R.drawable.ic_action_flag_fake;
                        break;
                    case LICENSE:
                        stringResource = VeredictReview.LICENSE.getString();
                        drawable=R.drawable.ic_action_flag_license;
                        break;
                    case FREEZE:
                        stringResource = VeredictReview.FREEZE.getString();
                        drawable=R.drawable.ic_action_flag_freeze;
                        break;
                    case VIRUS:
                        stringResource = VeredictReview.VIRUS.getString();
                        drawable=R.drawable.ic_action_flag_virus;
                        break;
                    default:
                        stringResource = VeredictReview.UNKNOWN.getString();
                        break;
                }
                //Log.d( "veredictReview", VeredictReview.reverseLookup(event.getVeredict().getFlag()).getString()+ " VS" +stringResource );
                //Log.d( "veredictReview", getString( VeredictReview.reverseLookup( event.getVeredict().getFlag() ).getString()) );


                if(stringResource != -1) {
                    review.setVisibility(View.VISIBLE);
                    review.setText( "" + getString( stringResource ) );
                    review.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(drawable), null, null, null);
                    //Log.d( "veredictReview", getString( VeredictReview.reverseLookup( event.getVeredict().getFlag() ).getString()) );
                }

/*
                int stringResource;
                switch ( VeredictReview.reverseLookup( event.getVeredict().getFlag() )) {
                    case GOOD:
                        stringResource = VeredictReview.GOOD.getString();
                        break;
                    case FAKE:
                        stringResource = VeredictReview.FAKE.getString();
                        break;
                    case LICENSE:
                        stringResource = VeredictReview.LICENSE.getString();
                        break;
                    case FREEZE:
                        stringResource = VeredictReview.FREEZE.getString();
                        break;
                    case VIRUS:
                        stringResource = VeredictReview.VIRUS.getString();
                        break;
                    default:
                        stringResource = VeredictReview.UNKNOWN.getString();
                        break;
                }


                if(stringResource != -1) {
                    review.setVisibility(View.VISIBLE);
                    review.setText( "" + getString( stringResource ) );
                    //Log.d( "veredictReview", getString( VeredictReview.reverseLookup( event.getVeredict().getFlag() ).getString()) );
                }

                if(event.getVeredict().getFlag().equals("good")) {
                    review.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_action_flag_good), null, null, null);
                }
                if(event.getVeredict().getFlag().equals("license")) {
                    review.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_action_flag_license), null, null, null);
                }
                if(event.getVeredict().getFlag().equals("fake")) {
                    review.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_action_flag_fake), null, null, null);
                }
                if(event.getVeredict().getFlag().equals("freeze")) {
                    review.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_action_flag_freeze), null, null, null);
                }
                if(event.getVeredict().getFlag().equals("virus")) {
                    review.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_action_flag_virus), null, null, null);
                }*/
            }else if(event.getFlagVotes()!=null){
                loading_flags.setVisibility(View.GONE);
                flags_container.setVisibility(View.VISIBLE);
                goodVotes.setText(getString(R.string.flag_good) +": "+event.getFlagVotes().getGood());
                licenseVotes.setText(getString(R.string.flag_license)+ ": "+ event.getFlagVotes().getLicense());
                fakeVotes.setText(getString(R.string.flag_fake)+": "+event.getFlagVotes().getFake());
                freezeVotes.setText(getString(R.string.flag_freeze)+": "+event.getFlagVotes().getFreeze());
                virusVotes.setText(getString(R.string.flag_virus)+": "+event.getFlagVotes().getVirus());
                flagThisApp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final AccountManager ac = AccountManager.get(getActivity());

                        if (ac.getAccountsByType(Aptoide.getConfiguration().getAccountType()).length > 0) {
                            AptoideDialog.flagAppDialog(event.getFlagUservote()).show(getFragmentManager(), "flagAppDialog");
                            if(Build.VERSION.SDK_INT >= 10)
                                FlurryAgent.logEvent("App_View_Opened_Flag_App_Dialog");
                        } else {
                            ac.addAccount(Aptoide.getConfiguration().getAccountType(), AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS, null, null, getActivity(), null, null);
                        }
                    }
                });
            }

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            //Log.d("likes","onCreateView");
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

            goodVotes = (TextView) v.findViewById(R.id.flag_good);
            licenseVotes = (TextView) v.findViewById(R.id.flag_license);
            fakeVotes = (TextView) v.findViewById(R.id.flag_fake);
            freezeVotes = (TextView) v.findViewById(R.id.flag_freeze);
            virusVotes = (TextView) v.findViewById(R.id.flag_virus);
            review = (TextView) v.findViewById(R.id.flag_review);

            flagThisApp = (Button) v.findViewById(R.id.button_flag);
            flags_container = (LinearLayout) v.findViewById(R.id.flags_container);
            loading_flags = (ProgressBar) v.findViewById(R.id.loading_flags);
            return v;
        }

        @Override
        public void clearState() {
            editText.setText("");
            //editText.setEnabled(false);
            editText.setEnabled(true);
        }

        public class AddLikeListener implements View.OnClickListener {

            private final boolean isLike;
            RequestListener<GenericResponse> requestListener = new RequestListener<GenericResponse>() {
                private final void dismiss(){
                    ProgressDialogFragment pd = (ProgressDialogFragment) getFragmentManager()
                                                    .findFragmentByTag("pleaseWaitDialog");
                    if(pd!=null)
                        pd.dismissAllowingStateLoss();
                }
                @Override
                public void onRequestFailure(SpiceException spiceException) {
                    //Log.d("likes","onRequestFailure");
                    dismiss();
                }

                @Override
                public void onRequestSuccess(GenericResponse genericResponse) {
                    //Log.d("likes","onRequestSuccess");
                    dismiss();
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

                final AccountManager accountManager = AccountManager.get(getActivity());

                if (accountManager.getAccountsByType(Aptoide.getConfiguration().getAccountType()).length > 0) {
                    addLike();
                } else {
                    accountManager.addAccount(Aptoide.getConfiguration().getAccountType(), AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS, null, null, getActivity(), null, null);
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

                if(Build.VERSION.SDK_INT >= 10) {
                    if (isLike) {
                        FlurryAgent.logEvent("App_View_Clicked_On_Like_Button");
                    } else {
                        FlurryAgent.logEvent("App_View_Clicked_On_Dont_Like_Button");
                    }
                }
                manager.execute(request, "1234" , DurationInMillis.ONE_SECOND, requestListener);




                AptoideDialog.pleaseWaitDialog().show(getFragmentManager(), "pleaseWaitDialog");
            }
        }

        public class AddCommentListener implements View.OnClickListener {

            @Override
            public void onClick(View v) {

                final AccountManager accountManager = AccountManager.get(getActivity());

                if (accountManager.getAccountsByType(Aptoide.getConfiguration().getAccountType()).length > 0) {
                    if(Build.VERSION.SDK_INT >= 10) FlurryAgent.logEvent("App_View_Added_A_Comment");
                    if(addCommentCallback != null) {
                        addCommentCallback.addComment(editText.getText().toString(), null);
                    }
                } else {

                    accountManager.addAccount(Aptoide.getConfiguration().getAccountType(), AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS, null, null, getActivity(), null, null);

                }

            }
        }


        public static class FillComments{

            public static void fillComments(Activity activity, LinearLayout commentsContainer, ArrayList<Comment> comments) {
                final SimpleDateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                View view;

                commentsContainer.removeAllViews();
                for (Comment comment : FragmentComments.getCompoundedComments(comments)) {
                    view = FragmentComments.createCommentView(activity, commentsContainer, comment, dateFormater);
                    commentsContainer.addView(view);
                }
            }
        }
    }

}

