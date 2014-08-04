package cm.aptoide.ptdev.fragments;

import android.accounts.*;
import android.app.Activity;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import cm.aptoide.ptdev.*;
import cm.aptoide.ptdev.configuration.AccountGeneral;
import cm.aptoide.ptdev.dialogs.AptoideDialog;
import cm.aptoide.ptdev.dialogs.ReplyCommentDialog;
import cm.aptoide.ptdev.fragments.callbacks.AddCommentVoteCallback;
import cm.aptoide.ptdev.model.*;
import cm.aptoide.ptdev.utils.AptoideUtils;
import cm.aptoide.ptdev.webservices.AddApkCommentVoteRequest;
import cm.aptoide.ptdev.webservices.AllCommentsRequest;
import cm.aptoide.ptdev.webservices.json.AllCommentsJson;

import com.flurry.android.FlurryAgent;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

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
            dateString = AptoideUtils.DateTimeUtils.getInstance(activity).getTimeDiffString(dateFormater.parse(comment.getTimestamp()).getTime());
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

        if(showReply) {
            TextView reply = (TextView) view.findViewById(R.id.reply_comment);
            reply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(isLoggedIn(activity)) {
                        if (!PreferenceManager.getDefaultSharedPreferences(Aptoide.getContext()).getString("username", "NOT_SIGNED_UP").equals("NOT_SIGNED_UP")) {

                            ReplyCommentDialog replyDialog = AptoideDialog.replyCommentDialog(comment.getId().intValue(), comment.getUsername());
                            replyDialog.show(((ActionBarActivity) activity).getSupportFragmentManager(), "replyCommentDialog");

                            if(Build.VERSION.SDK_INT >= 10) FlurryAgent.logEvent("App_View_Clicked_On_Reply_Comment");
                        } else {

                            AptoideDialog.updateUsernameDialog().show(((ActionBarActivity) activity).getSupportFragmentManager(), "updateNameDialog");
                        }
                    }
                }
            });
        }
        ((ImageView) view.findViewById(R.id.ic_action)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopup(activity, v, comment.getId().intValue(), comment.getUsername(), false);
                if(Build.VERSION.SDK_INT >= 10) FlurryAgent.logEvent("App_View_Clicked_On_Show_Popup");
            }
        });
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
                    if(Build.VERSION.SDK_INT >= 10) FlurryAgent.logEvent("App_View_Clicked_On_Show_More_Replies");
                } else {
                    visibility = View.GONE;
                    viewComments.setVisibility(View.VISIBLE);
                    comment.setShowingSubcomments(false);
                    if(Build.VERSION.SDK_INT >= 10) FlurryAgent.logEvent("App_View_Clicked_On_Show_Less_Replies");
                }
                subcommentsContainer.setVisibility(visibility);
            }
        };
    }


    private RequestListener<AllCommentsJson> requestListener = new RequestListener<AllCommentsJson>() {
        @Override
        public void onRequestFailure(SpiceException e) {
            Toast.makeText(Aptoide.getContext(), R.string.error_occured, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onRequestSuccess(AllCommentsJson allCommentsJson) {
            Log.d("subcomments", "total comments: " + allCommentsJson.getListing().size());
            setListAdapter(new AllCommentsAdapter(getActivity(), getCompoundedComments(allCommentsJson.getListing())));
        }
    };
    private SpiceManager spiceManager;

    @Override
    public void onAttach( Activity activity ) {
        super.onAttach( activity );
        spiceManager = ((AllCommentsActivity)activity).getSpice();
    }

    @Override
    public void onResume() {
        super.onResume();

        AllCommentsRequest request = new AllCommentsRequest();

        request.setRepoName(getActivity().getIntent().getStringExtra("repoName"));
        request.setVersionName(getActivity().getIntent().getStringExtra("versionName"));
        request.setPackageName(getActivity().getIntent().getStringExtra("packageName"));

        spiceManager.execute(request, requestListener);
    }

    public class AllCommentsAdapter extends ArrayAdapter<Comment> {
        final SimpleDateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        public AllCommentsAdapter(Context context, List<Comment> objects) {
            super(context, 0, objects);
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
                view = createCommentView(getActivity(), parent, comment, dateFormater);
            } else {
                fillViewCommentFields(getActivity(), view, comment, dateFormater, true);
                if(comment.hasSubComments()) {
                    LinearLayout ll = ((LinearLayout)view.findViewById(R.id.subcomments));
                    ll.removeAllViews();
                    fillViewSubcommentsFields(getActivity(), view, comment, dateFormater);
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
        getListView().setItemsCanFocus(true);
        getListView().setCacheColorHint(getResources().getColor(android.R.color.transparent));
    }

    public static void showPopup(Activity activity, View v, int commmentId, String author, boolean showReply) {
        android.support.v7.widget.PopupMenu popup = new android.support.v7.widget.PopupMenu(v.getContext(), v);
        popup.setOnMenuItemClickListener(new MenuListener(activity, commmentId, author));
        popup.inflate(R.menu.menu_comments);
        popup.show();
        if(!showReply) {
            popup.getMenu().findItem(R.id.menu_reply).setVisible(false);
        }
    }

    static class MenuListener implements android.support.v7.widget.PopupMenu.OnMenuItemClickListener{

        Activity activity;
        int commentId;
        String author;
        AddCommentVoteCallback voteCallback;

        MenuListener(Activity activity, int commentId, String author) {
            this.activity = activity;
            this.commentId = commentId;
            this.author = author;
            voteCallback = (AddCommentVoteCallback) activity;
        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            if (!isLoggedIn(activity)) return false;

            int i = menuItem.getItemId();

            if (i == R.id.menu_reply) {

//                getReplyListener();

                return true;
            } else if (i == R.id.menu_vote_up) {

                voteCallback.voteComment(commentId, AddApkCommentVoteRequest.CommentVote.up);
                if(Build.VERSION.SDK_INT >= 10) FlurryAgent.logEvent("App_View_Voted_Up");
                return true;

            } else if (i == R.id.menu_vote_down) {

                voteCallback.voteComment(commentId, AddApkCommentVoteRequest.CommentVote.down);
                if(Build.VERSION.SDK_INT >= 10) FlurryAgent.logEvent("App_View_Voted_Down");
                return true;
            }
            return false;
        }



    }

    public static boolean isLoggedIn(final Activity activity) {
        final AccountManager manager = AccountManager.get(activity);

        if (manager.getAccountsByType(Aptoide.getConfiguration().getAccountType()).length == 0) {

            manager.addAccount(Aptoide.getConfiguration().getAccountType(), AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS, null, null, activity, new AccountManagerCallback<Bundle>() {
                @Override
                public void run(AccountManagerFuture<Bundle> future) {



                    }

            }, null);

            return false;
        }
        return true;
    }

}
