package cm.aptoidetv.pt;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import cm.aptoidetv.pt.Model.Comment;


/**
 * Created by rmateus on 26-12-2013.
 */
public class FragmentComments extends ListFragment {

    public static View createCommentView(Activity activity, ViewGroup commentsContainer, Comment comment, SimpleDateFormat dateFormater) {
        View view;
        if(comment.hasSubComments()) {
            view = LayoutInflater.from(activity).inflate(R.layout.row_expandable_comment, commentsContainer, false);
            fillViewCommentFields(activity, view, comment, dateFormater, true);
            fillViewSubcommentsFields(activity, view, comment, dateFormater);
        } else {
            view = LayoutInflater.from(activity).inflate(R.layout.row_comment, commentsContainer, false);
            fillViewCommentFields(activity, view, comment, dateFormater, true);
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
                if(comment.hasSubComments()) {
                    comment.clearSubcomments();
                }
            } else {
                lastComment.addSubComment(comment);
            }
        }
        return principalComments;
    }

    private static void fillViewCommentFields(final Activity activity, View view, final Comment comment, SimpleDateFormat dateFormater, final boolean showReply) {
        TextView content = (TextView) view.findViewById(R.id.content);
        TextView author = (TextView) view.findViewById(R.id.author);
        String dateString = "";
        String votesString = "";

        try {
            dateString = Utils.DateTimeUtils.getInstance(activity).getTimeDiffString(dateFormater.parse(comment.getTimestamp()).getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if(comment.getVotes() != null && comment.getVotes().intValue() != 0){
            votesString = " | " + activity.getString(R.string.votes, comment.getVotes());
        }else{
            votesString = "";
        }

        content.setText(Html.fromHtml(comment.getText()+ " &bull; " + "<i>" + dateString + votesString  + "</i>"));

        author.setText(comment.getUsername());

    }

    private static void fillViewSubcommentsFields(Activity activity, View view, final Comment comment, SimpleDateFormat dateFormater) {
        final LinearLayout subcommentsContainer = ((LinearLayout) view.findViewById(R.id.subcomments));
        final TextView viewComments = (TextView) view.findViewById(R.id.hasComments);

        for (Comment subComment : comment.getSubComments()) {
            View subview = LayoutInflater.from(activity).inflate(R.layout.row_subcomment, subcommentsContainer, false);
            fillViewCommentFields(activity, subview, subComment, dateFormater, false);
            subcommentsContainer.addView(subview);
        }

        viewComments.setText(activity.getString(R.string.view_more_comments, comment.getSubComments().size()));
        viewComments.setOnClickListener(getMoreCommentsListener(comment, subcommentsContainer, viewComments));
        view.setOnClickListener(getMoreCommentsListener(comment, subcommentsContainer, viewComments));
    }

    private static View.OnClickListener getMoreCommentsListener(final Comment comment, final LinearLayout subcommentsContainer, final TextView viewComments) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int visibility;
                if (subcommentsContainer.getVisibility() == View.GONE) {
                    visibility = View.VISIBLE;
                    viewComments.setVisibility(View.GONE);
                    comment.setShowingSubcomments(true);
                } else {
                    visibility = View.GONE;
                    viewComments.setVisibility(View.VISIBLE);
                    comment.setShowingSubcomments(false);
                }
                subcommentsContainer.setVisibility(visibility);
            }
        };
    }
}
