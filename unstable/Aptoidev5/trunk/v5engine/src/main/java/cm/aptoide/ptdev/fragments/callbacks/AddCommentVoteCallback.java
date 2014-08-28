package cm.aptoide.ptdev.fragments.callbacks;

import cm.aptoide.ptdev.webservices.AddApkCommentVoteRequest;

/**
 * Created by jcosta on 04-07-2014.
 */
public interface AddCommentVoteCallback {
    void voteComment(int commentId, AddApkCommentVoteRequest.CommentVote vote);
}
