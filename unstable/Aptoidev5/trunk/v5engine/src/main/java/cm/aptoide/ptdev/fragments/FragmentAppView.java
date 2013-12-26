package cm.aptoide.ptdev.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewPager;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.*;
import android.widget.*;
import cm.aptoide.ptdev.AppViewActivity;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.ScreenshotsViewer;
import cm.aptoide.ptdev.adapters.GalleryPagerAdapter;
import cm.aptoide.ptdev.adapters.ImageGalleryAdapter;
import cm.aptoide.ptdev.downloadmanager.PermissionsActivity;
import cm.aptoide.ptdev.events.BusProvider;
import cm.aptoide.ptdev.model.ApkPermission;
import cm.aptoide.ptdev.model.Comment;
import cm.aptoide.ptdev.utils.AptoideUtils;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

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
        private TextView downloads;
        private TextView likes;
        private TextView dontLikes;
        private TextView size;
        private TextView latestVersion;
        private TextView publisher;
//        private Gallery screenshots;
//        private ImageGalleryAdapter galleryAdapter;
        private LinearLayout mainLayout;
        private View cell;
        private ViewPager viewPager;
        private ImageLoader imageLoader;


        @Subscribe
        public void refreshDetails(final AppViewActivity.DetailsEvent event) {
            Log.d("Aptoide-AppView", "getting event");
            Log.d("Aptoide-AppView", "Setting description");
            description.setText(event.getDescription());
            if (event.getLatestVersion() != null) {
                latestVersion.setVisibility(View.VISIBLE);
                SpannableString spanString = new SpannableString(getString(R.string.get_latest));
                spanString.setSpan(new UnderlineSpan(), 0, spanString.length(), 0);
                latestVersion.setText(spanString);
                latestVersion.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String url = event.getLatestVersion();
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        url = url.replaceAll(" ", "%20");
                        i.setData(Uri.parse(url));
                        startActivity(i);
                    }
                });
            }else{
                latestVersion.setVisibility(View.GONE);
            }
            publisher.setText(getString(R.string.publisher) +": " + event.getPublisher());
            size.setText(getString(R.string.size) + ": " + AptoideUtils.formatBytes(event.getSize()));
            downloads.setText(getString(R.string.downloads) + ": " + event.getDownloads());
            likes.setText(getString(R.string.likes) + ": " + event.getLikes());
            dontLikes.setText(getString(R.string.dont_likes) + ": " + event.getDontLikes());
//            galleryAdapter = new ImageGalleryAdapter(getActivity(), event.getScreenshots(), false);
//
//            if (event.getScreenshots() != null && event.getScreenshots().size() > 0) {
//                screenshots.setVisibility(View.VISIBLE);
//                screenshots.setAdapter(galleryAdapter);
//                screenshots.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                    @Override
//                    public void onItemClick(android.widget.AdapterView<?> parent, View view, int position, long id) {
//                        Intent intent = new Intent(getActivity(), ScreenshotsViewer.class);
//                        intent.putStringArrayListExtra("url", new ArrayList<String>(event.getScreenshots()));
//                        intent.putExtra("position", position);
//                        startActivity(intent);
//                    }
//                });
//                screenshots.setSelection(galleryAdapter.getCount() / 2);
//            }

            if (event.getScreenshots() != null && event.getScreenshots().size() > 0) {

                for (int i = 0; i < event.getScreenshots().size(); i++) {

                    cell = getActivity().getLayoutInflater().inflate(R.layout.row_item_screenshots_gallery, null);

                    final ImageView imageView = (ImageView) cell.findViewById(R.id.screenshot_image_item);
                    imageView.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {

                            viewPager.setVisibility(View.VISIBLE);
                            viewPager.setAdapter
                                    (new GalleryPagerAdapter(getActivity(), event.getScreenshots()));
                            viewPager.setCurrentItem(v.getId());

                        }
                    });

                    imageView.setId(i);

                    Log.d("screenshots","ss: "+event.getScreenshots().get(i).toString());
                    imageLoader.displayImage(event.getScreenshots().get(i).toString(), imageView);

                    imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(getActivity(), ScreenshotsViewer.class);
                            intent.putStringArrayListExtra("url", new ArrayList<String>(event.getScreenshots()));
                            intent.putExtra("position", 0);
                            startActivity(intent);
                        }
                    });

                    mainLayout.addView(cell);
                }
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_app_view_details, container, false);

            description = (TextView) v.findViewById(R.id.descript);
            downloads = (TextView) v.findViewById(R.id.downloads_label);
            likes = (TextView) v.findViewById(R.id.likes_label);
            dontLikes = (TextView) v.findViewById(R.id.dont_likes_label);
            size = (TextView) v.findViewById(R.id.size_label);
            publisher = (TextView) v.findViewById(R.id.publisher_label);
//            screenshots = (Gallery) v.findViewById(R.id.gallery);
            latestVersion = (TextView) v.findViewById(R.id.app_get_latest);
            viewPager = (ViewPager) v.findViewById(R.id._viewPager);
            mainLayout = (LinearLayout) v.findViewById(R.id._linearLayout);
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

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_specifications, container, false);

            permissionsContainer = (LinearLayout) v.findViewById(R.id.permissionsContainer);
            min_sdk = (TextView) v.findViewById(R.id.min_sdk);
            min_screen = (TextView) v.findViewById(R.id.min_screen);

            return v;
        }

        @Subscribe
        public void refreshDetails(final AppViewActivity.SpecsEvent event) {


            if (event.getPermissions() != null) {

                min_sdk.setText(getString(R.string.min_sdk) + ": " + event.getMinSdk());
                min_screen.setText(getString(R.string.min_screen) + ": " + event.getMinScreen().name());

                        final ArrayList<ApkPermission> permissions = PermissionsActivity.permissions(getActivity(), event.getPermissions());
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                FillPermissions.fillPermissions(getActivity(), permissionsContainer, permissions);
                            }
                        });
            }



        }

        public static class FillPermissions{

            public static void fillPermissions(Context context, LinearLayout permissionsContainer, ArrayList<ApkPermission> permissions) {

                View v;

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

        @Subscribe
        public void refreshDetails(AppViewActivity.RatingEvent event) {
            Log.d("Aptoide-AppView", "getting event");

            if(event.getComments()!=null) FillComments.fillComments(getActivity(), commentsContainer, event.getComments());

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            View v = inflater.inflate(R.layout.fragment_app_rating, container, false);

            commentsContainer = (LinearLayout) v.findViewById(R.id.commentContainer);

            return v;
        }

        public static class FillComments{

            public static void fillComments(Context context, LinearLayout commentsContainer, ArrayList<Comment> comments) {

                View v;

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
