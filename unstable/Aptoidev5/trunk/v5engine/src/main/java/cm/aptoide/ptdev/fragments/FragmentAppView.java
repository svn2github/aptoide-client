package cm.aptoide.ptdev.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Gallery;
import android.widget.LinearLayout;
import android.widget.TextView;
import cm.aptoide.ptdev.AppViewActivity;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.ScreenshotsViewer;
import cm.aptoide.ptdev.adapters.ImageGalleryAdapter;
import cm.aptoide.ptdev.events.BusProvider;
import cm.aptoide.ptdev.model.Comment;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Arrays;

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
    public void onStop() {
        super.onStop();
        BusProvider.getInstance().unregister(this);
    }


    public static class FragmentAppViewDetails extends FragmentAppView{


        private TextView description;
        private TextView size;
        private TextView version;
        private TextView publisher;
        private Gallery screenshots;
        private ImageGalleryAdapter galleryAdapter;

        @Subscribe
        public void refreshDetails(final AppViewActivity.DetailsEvent event) {
            Log.d("Aptoide-AppView", "getting event");
            Log.d("Aptoide-AppView", "Setting description");
            description.setText(event.getDescription());
            publisher.setText("Publisher " + event.getPublisher());
            size.setText("Size: " + String.valueOf(event.getSize()));
            galleryAdapter = new ImageGalleryAdapter(getActivity(), event.getScreenshots(), false);

            if (event.getScreenshots() != null && event.getScreenshots().size() > 0) {
//                hashCode = (viewApk.getApkid() + "|" + viewApk.getVercode());
                screenshots.setVisibility(View.VISIBLE);
                screenshots.setAdapter(galleryAdapter);
                screenshots.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(android.widget.AdapterView<?> parent, View view, int position, long id) {
                        Intent intent = new Intent(getActivity(), ScreenshotsViewer.class);
                        intent.putStringArrayListExtra("url", new ArrayList<String>(event.getScreenshots()));
                        intent.putExtra("position", position);
//                        intent.putExtra("hashCode", hashCode + ".hd");
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
            size = (TextView) v.findViewById(R.id.size_label);
            publisher = (TextView) v.findViewById(R.id.publisher_label);
            screenshots = (Gallery) v.findViewById(R.id.gallery);

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
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            return inflater.inflate(R.layout.fragment_specifications, container, false);
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
