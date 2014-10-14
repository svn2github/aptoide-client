package cm.aptoide.ptdev.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.utils.AptoideUtils;
import cm.aptoide.ptdev.webservices.timeline.TimeLineManager;
import cm.aptoide.ptdev.webservices.timeline.json.TimelineListAPKsJson;

/**
 * Created by rmateus on 25-09-2014.
 */
public class TimelineAdapter extends ArrayAdapter<TimelineListAPKsJson.UserApk> {


    private TimeLineManager mTimeLineManager;
    private final LayoutInflater mInflater;

    public TimelineAdapter(Context context, ArrayList<TimelineListAPKsJson.UserApk> apks) {
        super(context,0,apks);
        mInflater = LayoutInflater.from(context);
        mTimeLineManager = (TimeLineManager)context;
    }


    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        final View v;
        final ViewHolder holder;

        if(convertView == null){
            convertView = mInflater.inflate(R.layout.row_timeline_post, parent, false);

            holder = new ViewHolder();

            holder.userPhoto = (ImageView) convertView.findViewById(R.id.timeline_post_user_photo);
            holder.userName = (TextView) convertView.findViewById(R.id.timeline_post_user_name);
//            holder.action = (TextView) convertView.findViewById(R.id.timeline_post_user_action);
            holder.time = (TextView) convertView.findViewById(R.id.timeline_post_timestamp);
            holder.popUpMenu = (ImageButton) convertView.findViewById(R.id.timeline_post_options);

            holder.centerLayout = (LinearLayout) convertView.findViewById(R.id.timeline_post_center);
            holder.appIcon = (ImageView) convertView.findViewById(R.id.timeline_post_app_icon);
            holder.appName = (TextView) convertView.findViewById(R.id.timeline_post_app_name);
            holder.appVersion = (TextView) convertView.findViewById(R.id.timeline_post_app_version);
            holder.appRepo = (TextView) convertView.findViewById(R.id.timeline_post_app_store);

//            holder.bottomLayout = (LinearLayout) convertView.findViewById(R.id.timeline_post_bottom);
            holder.likesandscomments = (TextView) convertView.findViewById(R.id.timeline_post_likes);


            holder.likeButton = (FrameLayout) convertView.findViewById(R.id.timeline_post_like_button) ;
            holder.likeButtonText = (TextView) convertView.findViewById(R.id.timeline_post_like_text);
            holder.commentButton = (FrameLayout) convertView.findViewById(R.id.timeline_post_comment) ;

            convertView.setTag(holder);
            v = convertView;
        }else{
            holder = (ViewHolder)convertView.getTag();
            v = convertView;
        }

        final TimelineListAPKsJson.UserApk entry = getItem(position);

        //addAppListener(holder.centerLayout, entry.getApk_id().longValue());
        //addLikeListener(holder.likeButton, entry.getPostID().longValue());
        //addCommentListener(holder.commentButton, entry.getPostID().longValue());
        //addOptionsListener(holder.popUpMenu, entry.getPostID().longValue());

        holder.appName.setText(entry.getApk().getName());
        ImageLoader.getInstance().displayImage(entry.getApk().getIcon(), holder.appIcon);
        holder.userName.setText(entry.getInfo().getUsername() + " " + getContext().getString(R.string.installed_tab) + " " + entry.getApk().getName());
        ImageLoader.getInstance().displayImage(entry.getInfo().getAvatar(), holder.userPhoto);
//        holder.action.setText("installed:");

//        if (AccountManager.get(ctx).getAccountsByType(
//                Aptoide.getConfiguration().getAccountType())[0].name.equals(entry.getUserMail())) {
//            holder.popUpMenu.setVisibility( View.VISIBLE );
//        } else {
//            holder.popUpMenu.setVisibility( View.GONE );
//        }
//        holder.likes.setText(NumberFormat.getIntegerInstance().format(entry.getLikes())
//                +((entry.getLikes().longValue()==1)?" Like" :" Likes"));
//        holder.comments.setText(NumberFormat.getIntegerInstance().format(entry.getFriends())
//                +((entry.getFriends().longValue()==1)?" Comment" :" Comments"));
//
//        boolean hasLikes = !(entry.getLikes().longValue()==0);
//        boolean hasComments = !(entry.getFriends().longValue() == 0);
//        if (hasLikes || hasComments) {
//            holder.bottomLayout.setVisibility(View.VISIBLE);
//
//            holder.likes.setVisibility( hasLikes ? View.VISIBLE : View.GONE);
//            holder.comments.setVisibility( hasComments? View.VISIBLE : View.GONE);
//
//            if (hasLikes && hasComments)    holder.and.setVisibility(View.VISIBLE);
//            else                            holder.and.setVisibility(View.GONE);
//        } else {
//            holder.bottomLayout.setVisibility(View.GONE);
//        }

        StringBuilder sb= new StringBuilder();
        final int likes = entry.getInfo().getLikes().intValue();
        int comments = entry.getInfo().getComments().intValue();

        if(likes > 0 || comments > 0) {
            if (likes > 0)
                sb.append(String.valueOf(likes) + " " + getContext().getString(R.string.likes));
            if (likes > 0 && comments > 0) {
                sb.append(" and ");
            }
            if (comments > 0)
                sb.append(String.valueOf(comments) + " " + getContext().getString(R.string.comments));

            holder.likesandscomments.setText(sb.toString());
            holder.likesandscomments.setVisibility(View.VISIBLE);
        }else{
            holder.likesandscomments.setVisibility(View.GONE);
        }
        holder.time.setText(getTime(entry.getInfo().getTimestamp()));
        holder.appVersion.setText(entry.getApk().getVername());
        holder.appRepo.setText(getContext().getString(R.string.store)+": " + entry.getApk().getRepo());

        final boolean isLiked = entry.getInfo().isUserliked().equals("like");
        final long id = entry.getInfo().getId().longValue();
        ChangeLikeButtonText(isLiked,holder.likeButtonText);

        final View myCv = convertView;

        holder.likeButton.setOnClickListener(new View.OnClickListener() {
            boolean selfIsLiked = isLiked;
            int selfLikes = likes;

            @Override
            public void onClick(View v) {
                if(selfIsLiked) {
                    mTimeLineManager.unlikePost(id);
                    selfIsLiked =false;
                    entry.getInfo().setUserliked("nolike");
                    entry.getInfo().setLikes(--selfLikes);
                }else{
                    mTimeLineManager.likePost(id);
                    selfIsLiked =true;
                    entry.getInfo().setUserliked("like");
                    entry.getInfo().setLikes(++selfLikes);
                }
                getView(position,myCv,parent);
            }
        });
        holder.commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTimeLineManager.openCommentsDialog(id);
            }
        });
        return v;
    }
    private static void ChangeLikeButtonText(boolean isLiked, TextView likeButtonText){
        likeButtonText.setText(isLiked ? "Unlike" : "Like");
    }


    private static String getTime(String time) {
        String result = "";
        final SimpleDateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        try {
            result = AptoideUtils.DateTimeUtils.getInstance(Aptoide.getContext()).getTimeDiffString(dateFormater.parse(time).getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return result;
    }

    static class ViewHolder {
        // TOP
        public ImageView userPhoto;
        public TextView userName;
//        public TextView action;
        public TextView time;
        public ImageButton popUpMenu;

        // CENTER
        public LinearLayout centerLayout;
        public TextView appName;
        public ImageView appIcon;
        public TextView appVersion;
        public TextView appRepo;

        // BOTTOM
        public LinearLayout bottomLayout;
        public TextView likesandscomments;

        // Buttons
        public FrameLayout likeButton;
        public FrameLayout commentButton;
        public TextView likeButtonText;
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }
}
