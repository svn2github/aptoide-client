package cm.aptoide.ptdev.fragments;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import cm.aptoide.ptdev.AppViewActivity;
import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.database.schema.Schema;
import cm.aptoide.ptdev.services.HttpClientSpiceService;
import cm.aptoide.ptdev.utils.AptoideUtils;
import cm.aptoide.ptdev.webservices.ListRelatedApkRequest;
import cm.aptoide.ptdev.webservices.ListRepositoryLikesRequest;
import cm.aptoide.ptdev.webservices.json.RelatedApkJson;
import cm.aptoide.ptdev.webservices.json.RepositoryChangeJson;
import cm.aptoide.ptdev.webservices.json.RepositoryLikesJson;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import java.util.List;

/**
 * Created by rmateus on 18-02-2014.
 */
public class LatestLikesFragment extends ListFragment implements FragmentStore{

    SpiceManager spiceManager = new SpiceManager(HttpClientSpiceService.class);
    private String repoName;


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

    RequestListener<RepositoryLikesJson> request = new RequestListener<RepositoryLikesJson>() {

        @Override
        public void onRequestFailure(SpiceException spiceException) {

        }

        @Override
        public void onRequestSuccess(RepositoryLikesJson relatedApkJson) {
            setListAdapter(new LikesAdapter(getActivity(), relatedApkJson.getListing()));
        }

    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = super.onCreateView(inflater, container, savedInstanceState);

        Cursor c = new Database(Aptoide.getDb()).getStore(getArguments().getLong("storeid"));
        repoName = null;
        if(c.moveToFirst()){
            repoName = c.getString(c.getColumnIndex(Schema.Repo.COLUMN_NAME));

            if(repoName==null){
                repoName = AptoideUtils.RepoUtils.split(c.getString(c.getColumnIndex(Schema.Repo.COLUMN_URL)));
            }
        }
        c.close();

        ListRepositoryLikesRequest listRelatedApkRequest = new ListRepositoryLikesRequest(repoName);


        Log.d("FragmentRelated", "onCreateView");

        spiceManager.execute(listRelatedApkRequest, repoName + "-latestLikes", DurationInMillis.ONE_DAY, request);
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getListView().setDivider(null);
        getListView().setCacheColorHint(getResources().getColor(android.R.color.transparent));

        Log.d("FragmentRelated", "onViewCreated");


    }

    @Override
    public void onRefresh() {

    }

    @Override
    public void onError() {

    }

    @Override
    public void setRefreshing(boolean bool) {

    }


    public class LikesAdapter extends ArrayAdapter<RepositoryLikesJson.Listing>{

        public LikesAdapter(Context context, List<RepositoryLikesJson.Listing> objects) {
            super(context, 0, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View view;

            if(convertView == null){
                view = LayoutInflater.from(getActivity()).inflate(R.layout.row_latest_likes, parent ,false);
            }else{
                view = convertView;
            }

            TextView user_name = (TextView) view.findViewById(R.id.username);
            TextView app_name = (TextView) view.findViewById(R.id.app_name);
            TextView app_version = (TextView) view.findViewById(R.id.app_version);
            ImageView taste = (ImageView) view.findViewById(R.id.user_taste);

            user_name.setText(getItem(position).getUsername());
            app_name.setText(getItem(position).getName());
            app_version.setText(getItem(position).getVer());

            boolean like = Boolean.parseBoolean(getItem(position).getLike());

            if(like){
//                app_name.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_action_good_pressed, 0);
                taste.setImageResource(R.drawable.like);
            }else {
//                app_name.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_action_bad_pressed,0);
                taste.setImageResource(R.drawable.dont_like);
            }

            return view;
        }


    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);



        RepositoryLikesJson.Listing item = (RepositoryLikesJson.Listing) l.getItemAtPosition(position);

        id =  new Database(Aptoide.getDb()).getApkFromPackage(item.getApkid(), repoName);

        if(id>0){
            Intent i = new Intent(getActivity(), AppViewActivity.class);
            i.putExtra("id", id);
            startActivity(i);
        }










    }
}
