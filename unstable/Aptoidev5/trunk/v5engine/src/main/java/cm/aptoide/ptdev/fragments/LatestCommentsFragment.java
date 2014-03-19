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
import android.widget.ListView;
import android.widget.TextView;
import cm.aptoide.ptdev.AppViewActivity;
import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.database.schema.Schema;
import cm.aptoide.ptdev.services.HttpClientSpiceService;
import cm.aptoide.ptdev.utils.AptoideUtils;
import cm.aptoide.ptdev.utils.Configs;
import cm.aptoide.ptdev.webservices.ListRelatedApkRequest;
import cm.aptoide.ptdev.webservices.ListRepositoryCommentsRequest;
import cm.aptoide.ptdev.webservices.ListRepositoryLikesRequest;
import cm.aptoide.ptdev.webservices.json.RelatedApkJson;
import cm.aptoide.ptdev.webservices.json.RepositoryChangeJson;
import cm.aptoide.ptdev.webservices.json.RepositoryCommentsJson;
import cm.aptoide.ptdev.webservices.json.RepositoryLikesJson;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

/**
 * Created by rmateus on 18-02-2014.
 */
public class LatestCommentsFragment extends ListFragment implements FragmentStore{

    SpiceManager spiceManager = new SpiceManager(HttpClientSpiceService.class);
    private String repoName;
    private Class appViewClass = Aptoide.getConfiguration().getAppViewActivityClass();


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

    RequestListener<RepositoryCommentsJson> request = new RequestListener<RepositoryCommentsJson>() {

        @Override
        public void onRequestFailure(SpiceException spiceException) {
            setListShown(true);
            setEmptyText(getString(R.string.connection_error));
        }

        @Override
        public void onRequestSuccess(RepositoryCommentsJson relatedApkJson) {
            if(relatedApkJson!=null){
                setListAdapter(new CommentsAdapter(getActivity(), relatedApkJson.getListing()));
            }else{
                setListShown(true);
                setEmptyText(getString(R.string.error_occured));
            }

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

        ListRepositoryCommentsRequest listRelatedApkRequest = new ListRepositoryCommentsRequest(repoName);

        Log.d("FragmentRelated", "onCreateView");

        spiceManager.execute(listRelatedApkRequest, repoName + "-latestComments", DurationInMillis.ONE_DAY, request);
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


    public class CommentsAdapter extends ArrayAdapter<RepositoryCommentsJson.Listing>{

        public CommentsAdapter(Context context, List<RepositoryCommentsJson.Listing> objects) {
            super(context, 0, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View view;

            if(convertView == null){
                view = LayoutInflater.from(getActivity()).inflate(R.layout.row_latest_comments, parent ,false);
            }else{
                view = convertView;
            }

            RepositoryCommentsJson.Listing item = getItem(position);

            TextView appName = (TextView) view.findViewById(R.id.comment_app_name);
            TextView time = (TextView) view.findViewById(R.id.comment_time);
            TextView comment = (TextView) view.findViewById(R.id.comment);
            TextView user = (TextView) view.findViewById(R.id.user_comment);


            appName.setText(item.getName());
            comment.setText(item.getText());
            if(user.equals("NOT_SIGNED_UP")){
                user.setText("");
            }else{
                user.setText(item.getUsername());
            }

            try {
                Date date = Configs.TIME_STAMP_FORMAT.parse(item.getTimestamp());
                time.setText(AptoideUtils.DateDiffUtils.getDiffDate(getContext(), date));
            } catch (ParseException e) {
                e.printStackTrace();
            }


            return view;
        }


    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);



        RepositoryCommentsJson.Listing item = (RepositoryCommentsJson.Listing) l.getItemAtPosition(position);

        id =  new Database(Aptoide.getDb()).getApkFromPackage(item.getApkid(), repoName);

        if(id>0){
            Intent i = new Intent(getActivity(), appViewClass);
            i.putExtra("id", id);
            startActivity(i);
        }










    }
}
