package cm.aptoide.ptdev.fragments;

import android.accounts.*;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewPager;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.*;
import android.widget.*;
import cm.aptoide.ptdev.*;
import cm.aptoide.ptdev.adapters.GalleryPagerAdapter;
import cm.aptoide.ptdev.adapters.ImageGalleryAdapter;
import cm.aptoide.ptdev.configuration.AccountGeneral;
import cm.aptoide.ptdev.dialogs.AptoideDialog;
import cm.aptoide.ptdev.dialogs.ProgressDialogFragment;
import cm.aptoide.ptdev.downloadmanager.PermissionsActivity;
import cm.aptoide.ptdev.events.AppViewRefresh;
import cm.aptoide.ptdev.events.BusProvider;
import cm.aptoide.ptdev.model.ApkPermission;
import cm.aptoide.ptdev.model.Comment;
import cm.aptoide.ptdev.utils.AptoideUtils;
import cm.aptoide.ptdev.webservices.AddCommentRequest;
import cm.aptoide.ptdev.webservices.AddLikeRequest;
import cm.aptoide.ptdev.webservices.json.GetApkInfoJson;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.squareup.otto.Subscribe;

import java.io.IOException;
import java.util.ArrayList;

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
        private TextView store;
        private TextView downloads;
        private TextView rating;
        private TextView likes;
        private TextView dontLikes;
        private TextView size;
//        private TextView latestVersion;
        private TextView publisher;
        private Gallery screenshots;
        private ImageGalleryAdapter galleryAdapter;
        private LinearLayout mainLayout;
        private View cell;
        private ViewPager viewPager;
        private ImageLoader imageLoader;
        private TextView publisherWebsite;
        private TextView publisherEmail;
        private TextView publisherPrivacyPolicy;
        private View publisherContainer;
        private View whatsNewContainer;
        private TextView whatsNew;


        @Subscribe
        public void refreshDetails(final AppViewActivity.DetailsEvent event) {
            Log.d("Aptoide-AppView", "getting event");
            Log.d("Aptoide-AppView", "Setting description");
            description.setText(event.getDescription());
            publisher.setText(getString(R.string.publisher) +": " + event.getPublisher());
            size.setText(getString(R.string.size) + ": " + AptoideUtils.formatBytes(event.getSize()));
            store.setText(getString(R.string.store) + ": " + ((AppViewActivity) getActivity()).getRepoName());
            downloads.setText(getString(R.string.downloads) + ": " + event.getDownloads());
            rating.setText(getString(R.string.rating) +": "+ event.getRating()+ "/5");
            likes.setText("" + event.getLikes());
            dontLikes.setText("" + event.getDontLikes());

            if(event.getDeveloper() != null){
                publisherContainer.setVisibility(View.VISIBLE);

                if(((AppViewActivity)getActivity()).isUpdate()){
                    whatsNewContainer.setVisibility(View.VISIBLE);
                    whatsNew.setText(event.getNews());
                }

                publisherContainer.setVisibility(View.VISIBLE);
                publisherEmail.setText("E-Mail: " + event.getDeveloper().getInfo().getEmail());
                publisherPrivacyPolicy.setText("Privacy Policy: " + event.getDeveloper().getInfo().getPrivacy_policy());
                publisherWebsite.setText("Website: " + event.getDeveloper().getInfo().getWebsite());
            }






            if (event.getScreenshots() != null && event.getScreenshots().size() > 0) {
                galleryAdapter = new ImageGalleryAdapter(getActivity(), event.getScreenshots(), false);
                screenshots.setVisibility(View.VISIBLE);
                screenshots.setAdapter(galleryAdapter);
                screenshots.setSpacing(1);
                screenshots.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(android.widget.AdapterView<?> parent, View view, int position, long id) {
                        Intent intent = new Intent(getActivity(), ScreenshotsViewer.class);
                        intent.putStringArrayListExtra("url", new ArrayList<String>(event.getScreenshots()));
                        intent.putExtra("position", position);
                        startActivity(intent);
                    }
                });
                screenshots.setSelection(galleryAdapter.getCount() / 2);
            }

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_app_view_details, container, false);

            description = (TextView) v.findViewById(R.id.descript);
            store = (TextView) v.findViewById(R.id.store_label);
            downloads = (TextView) v.findViewById(R.id.downloads_label);
            rating = (TextView) v.findViewById(R.id.rating_label);
            likes = (TextView) v.findViewById(R.id.likes_label);
            dontLikes = (TextView) v.findViewById(R.id.dont_likes_label);
            size = (TextView) v.findViewById(R.id.size_label);
            publisher = (TextView) v.findViewById(R.id.publisher_label);
            screenshots = (Gallery) v.findViewById(R.id.gallery);
//            viewPager = (ViewPager) v.findViewById(R.id._viewPager);
//            mainLayout = (LinearLayout) v.findViewById(R.id._linearLayout);
            publisherContainer = v.findViewById(R.id.publisher_container);
            publisherWebsite = (TextView) v.findViewById(R.id.publisher_website);
            publisherEmail = (TextView) v.findViewById(R.id.publisher_email);
            publisherPrivacyPolicy = (TextView) v.findViewById(R.id.publisher_privacy_policy);
            whatsNew = (TextView) v.findViewById(R.id.whats_new_descript);
            whatsNewContainer = v.findViewById(R.id.whats_new_container);
            imageLoader = ImageLoader.getInstance();


            return v;
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

        }

    }



    public static class FragmentAppViewRelated extends ListFragment {



    }

    public static class FragmentAppViewSpecs extends FragmentAppView{

        private LinearLayout permissionsContainer;
        private TextView min_sdk;
        private TextView min_screen;
        private AsyncTask<ArrayList<String>, Void, ArrayList<ApkPermission>> task;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_specifications, container, false);

            permissionsContainer = (LinearLayout) v.findViewById(R.id.permissionsContainer);
            min_sdk = (TextView) v.findViewById(R.id.min_sdk);
            min_screen = (TextView) v.findViewById(R.id.min_screen);

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
                FillPermissions.fillPermissions(getActivity(), permissionsContainer, apkPermissions);
                setRetainInstance(false);
            }
        }

        public static class FillPermissions{

            public static void fillPermissions(Context context, LinearLayout permissionsContainer, ArrayList<ApkPermission> permissions) {

                View v;

                assert context != null: "Context is null";
                assert permissionsContainer != null: "container is null";

                for(ApkPermission permission : permissions){

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

    public static class FragmentAppViewRating extends FragmentAppView{
        private LinearLayout commentsContainer;
        private Button seeAllButton;
        private EditText editText;
        private Button addComment;
        private Button dontLikeBtn;
        private Button likeBtn;

        @Subscribe
        public void refreshDetails(final AppViewActivity.RatingEvent event) {
            Log.d("Aptoide-AppView", "getting event");

            if(event.getComments()!=null){
                FillComments.fillComments(getActivity(), commentsContainer, event.getComments());
                seeAllButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                       Intent intent = new Intent(getActivity(), AllComments.class);
                        intent.putExtra("repoName", ((AppViewActivity) getActivity()).getRepoName());
                        intent.putExtra("versionName", ((AppViewActivity)getActivity()).getVersionName());
                        intent.putExtra("packageName", ((AppViewActivity)getActivity()).getPackage_name());
                        startActivity(intent);
                    }
                });

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
            }




        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            View v = inflater.inflate(R.layout.fragment_app_rating, container, false);

            commentsContainer = (LinearLayout) v.findViewById(R.id.commentContainer);
            seeAllButton = (Button) v.findViewById(R.id.more_comments);
            editText = (EditText) v.findViewById(R.id.editText_addcomment);
            addComment = (Button) v.findViewById(R.id.button_add_comment);
            dontLikeBtn = (Button) v.findViewById(R.id.button_dont_like);
            dontLikeBtn.setOnClickListener(new AddLikeListener(false));
            likeBtn = (Button) v.findViewById(R.id.button_like);
            likeBtn.setOnClickListener(new AddLikeListener(true));
            addComment.setOnClickListener(new AddCommentListener());


            return v;

        }
        public class AddLikeListener implements View.OnClickListener {

            private final boolean isLike;
            RequestListener<GenericResponse> requestListener = new RequestListener<GenericResponse>() {
                @Override
                public void onRequestFailure(SpiceException spiceException) {
                    Toast.makeText(getActivity(), "Post failed", Toast.LENGTH_LONG).show();
                    ProgressDialogFragment pd = (ProgressDialogFragment) getFragmentManager().findFragmentByTag("pleaseWaitDialog");
                    pd.dismiss();
                }

                @Override
                public void onRequestSuccess(GenericResponse genericResponse) {

                    ProgressDialogFragment pd = (ProgressDialogFragment) getFragmentManager().findFragmentByTag("pleaseWaitDialog");
                    pd.dismiss();

                    if(genericResponse.getStatus().equals("OK")){
                        Toast.makeText(getActivity(), "Add Review success", Toast.LENGTH_LONG).show();
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

                if (manager.getAccountsByType(AccountGeneral.ACCOUNT_TYPE).length > 0) {
                    addLike();
                } else {
                    manager.addAccount(AccountGeneral.ACCOUNT_TYPE, AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS, null, null, getActivity(), new AccountManagerCallback<Bundle>() {
                        @Override
                        public void run(AccountManagerFuture<Bundle> future) {

                            if (LoginActivity.isLoggedIn(getActivity())) {
                                Account account = manager.getAccountsByType(AccountGeneral.ACCOUNT_TYPE)[0];
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
                    Toast.makeText(getActivity(), "Comment failed", Toast.LENGTH_LONG).show();
                    ProgressDialogFragment pd = (ProgressDialogFragment) getFragmentManager().findFragmentByTag("pleaseWaitDialog");
                    pd.dismiss();
                }

                @Override
                public void onRequestSuccess(GenericResponse genericResponse) {

                    ProgressDialogFragment pd = (ProgressDialogFragment) getFragmentManager().findFragmentByTag("pleaseWaitDialog");
                    pd.dismiss();

                    if(genericResponse.getStatus().equals("OK")){
                        Toast.makeText(getActivity(), "Comment success", Toast.LENGTH_LONG).show();
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

                if (manager.getAccountsByType(AccountGeneral.ACCOUNT_TYPE).length > 0) {
                    addComment();
                } else {

                    manager.addAccount(AccountGeneral.ACCOUNT_TYPE, AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS, null, null, getActivity(), new AccountManagerCallback<Bundle>() {
                        @Override
                        public void run(AccountManagerFuture<Bundle> future) {
                            if (LoginActivity.isLoggedIn(getActivity())) {

                                Account account = manager.getAccountsByType(AccountGeneral.ACCOUNT_TYPE)[0];
                                manager.getAuthToken(account, AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS, null, getActivity(), new AccountManagerCallback<Bundle>() {
                                    @Override
                                    public void run(AccountManagerFuture<Bundle> future) {
                                        try {

                                            ((AppViewActivity) getActivity()).setToken(future.getResult().getString(AccountManager.KEY_AUTHTOKEN));
                                            addComment();
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
                manager = ((AppViewActivity)getActivity()).getSpice();

                AddCommentRequest request = new AddCommentRequest(getActivity());
                request.setApkversion(((AppViewActivity)getActivity()).getVersionName());
                request.setPackageName(((AppViewActivity) getActivity()).getPackage_name());
                request.setRepo(((AppViewActivity) getActivity()).getRepoName());
                request.setToken(((AppViewActivity) getActivity()).getToken());

                request.setText(editText.getText().toString());

                manager.execute(request, requestListener);
                AptoideDialog.pleaseWaitDialog().show(getFragmentManager(), "pleaseWaitDialog");
            }

        }

        public static class FillComments{

            public static void fillComments(Context context, LinearLayout commentsContainer, ArrayList<Comment> comments) {

                View v;
                commentsContainer.removeAllViews();
                for(Comment comment : comments){

                    v = LayoutInflater.from(context).inflate(R.layout.row_comment, commentsContainer, false);

                    TextView content = (TextView) v.findViewById(R.id.content);
                    TextView date = (TextView) v.findViewById(R.id.date);
                    TextView author = (TextView) v.findViewById(R.id.author);

                    content.setText(comment.getText());
                    date.setText(comment.getTimestamp());
                    author.setText(comment.getUsername());
                    commentsContainer.addView(v);
                }



            }
        }


    }




}
