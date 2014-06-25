package cm.aptoide.ptdev.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import cm.aptoide.ptdev.AllCommentsActivity;
import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.model.Comment;
import cm.aptoide.ptdev.utils.AptoideUtils;
import cm.aptoide.ptdev.webservices.AllCommentsRequest;
import cm.aptoide.ptdev.webservices.json.AllCommentsJson;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rmateus on 26-12-2013.
 */
public class FragmentComments extends ListFragment {


    public static View createCommentView(Context context, ViewGroup commentsContainer, Comment comment, SimpleDateFormat dateFormater) {
        View view;
        if(comment.hasSubComments()) {
            view = LayoutInflater.from(context).inflate(R.layout.row_expandable_comment, commentsContainer, false);
            fillViewCommentFields(context, view, comment, dateFormater);
            fillViewSubcommentsFields(context, view, comment, dateFormater);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.row_comment, commentsContainer, false);
            fillViewCommentFields(context, view, comment, dateFormater);
        }
        return view;
    }

    public static List<Comment> getCompoundedComments(List<Comment> allComents) {
        List<Comment> principalComments = new ArrayList<Comment>();
        Comment lastComment = null;

        for (Comment comment : allComents) {

            if (comment.getAnswerTo() == null) {
                lastComment = comment;
                principalComments.add(comment);
            } else {
                lastComment.addSubComment(comment);
            }
        }
        return principalComments;
    }

    private static void fillViewCommentFields(Context context, View view, Comment comment, SimpleDateFormat dateFormater) {
        TextView content = (TextView) view.findViewById(R.id.content);
        TextView date = (TextView) view.findViewById(R.id.date);
        TextView author = (TextView) view.findViewById(R.id.author);

        content.setText(comment.getText());
//        comment.getSubComments().size();
        try {
            date.setText(AptoideUtils.DateTimeUtils.getInstance(context).getTimeDiffString(dateFormater.parse(comment.getTimestamp()).getTime()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        author.setText(comment.getUsername());
    }

    private static void fillViewSubcommentsFields(Context context, View view, final Comment comment, SimpleDateFormat dateFormater) {
        final LinearLayout subcommentsContainer = ((LinearLayout) view.findViewById(R.id.subcomments));

        for (Comment subComment : comment.getSubComments()) {
            View subview = LayoutInflater.from(context).inflate(R.layout.row_subcomment, subcommentsContainer, false);
            fillViewCommentFields(context, subview, subComment, dateFormater);
            subcommentsContainer.addView(subview);
        }

        ((TextView) view.findViewById(R.id.hasComments)).setVisibility(View.VISIBLE);
        ((TextView) view.findViewById(R.id.hasComments)).setText(""+comment.getSubComments().size());
        ((TextView) view.findViewById(R.id.hasComments)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int visibility;
                if (subcommentsContainer.getVisibility() == View.GONE) {
                    visibility = View.VISIBLE;
                    comment.setShowingSubcomments(true);
                } else {
                    visibility = View.GONE;
                    comment.setShowingSubcomments(false);
                }
                subcommentsContainer.setVisibility(visibility);
            }
        });
    }


    private RequestListener<AllCommentsJson> requestListener = new RequestListener<AllCommentsJson>() {
        @Override
        public void onRequestFailure(SpiceException e) {
            Toast.makeText(Aptoide.getContext(), R.string.error_occured, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onRequestSuccess(AllCommentsJson allCommentsJson) {
            Log.d("subcomments", "total comments: " + allCommentsJson.getListing().size());
            setListAdapter(new AllCommentsAdapter(getActivity(), R.layout.all_comments, getCompoundedComments(allCommentsJson.getListing())));
        }
    };
    private SpiceManager spiceManager;


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        spiceManager = ((AllCommentsActivity)getActivity()).getSpice();

        AllCommentsRequest request = new AllCommentsRequest();

        request.setRepoName(getActivity().getIntent().getStringExtra("repoName"));
        request.setVersionName(getActivity().getIntent().getStringExtra("versionName"));
        request.setPackageName(getActivity().getIntent().getStringExtra("packageName"));

        spiceManager.execute(request, requestListener);
    }

    public class AllCommentsAdapter extends ArrayAdapter<Comment> {
        final SimpleDateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd HH:mm");


        public AllCommentsAdapter(Context context, int resourceId, List<Comment> objects) {
            super(context, resourceId, objects);
        }



        @Override
        public int getPosition(Comment item) {
            return super.getPosition(item);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            Comment comment = getItem(position);

            if (view == null) {
                view = createCommentView(getContext(), parent, comment, dateFormater);
            } else {
                fillViewCommentFields(getContext(), view, comment, dateFormater);
                if(comment.hasSubComments()) {
                    LinearLayout ll = ((LinearLayout)view.findViewById(R.id.subcomments));
                    ll.removeAllViews();
                    fillViewSubcommentsFields(getContext(), view, comment, dateFormater);
                    if(comment.isShowingSubcomments()) {
                        ll.setVisibility(View.VISIBLE);
                    } else {
                        ll.setVisibility(View.GONE);
                    }
                }
            }

            return view;
        }

        @Override
        public int getItemViewType(int position) {
            if(getItem(position).hasSubComments()) {
                return 1;
            }
            return 0;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getListView().setDivider(null);
        getListView().setCacheColorHint(getResources().getColor(android.R.color.transparent));
    }


}
