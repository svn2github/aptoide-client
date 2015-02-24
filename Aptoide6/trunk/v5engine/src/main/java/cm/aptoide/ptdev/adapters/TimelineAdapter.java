package cm.aptoide.ptdev.adapters;

import android.content.Context;
import android.content.Intent;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.ads.NativeAd;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.utils.AptoideUtils;
import cm.aptoide.ptdev.utils.IconSizes;
import cm.aptoide.ptdev.webservices.timeline.TimeLineManager;
import cm.aptoide.ptdev.webservices.timeline.json.TimelineListAPKsJson;

/**
 * Created by rmateus on 25-09-2014.
 */
public class TimelineAdapter extends ArrayAdapter<TimelineListAPKsJson.UserApk> {
    private static final int ACTIVE = 0;
    private static final int HIDDEN = 1;
    private static final int ADTYPE = 2;

    private NativeAd ad;
    private static final int AD_INDEX = 3;
    private View adView;

    private TimeLineManager mTimeLineManager;

    private final LayoutInflater mInflater;

    public TimelineAdapter(TimeLineManager callback, Context context, ArrayList<TimelineListAPKsJson.UserApk> apks) {
        super(context,0,apks);
        mInflater = LayoutInflater.from(context);
        mTimeLineManager = callback;
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public int getItemViewType(int position) {
        int type;
        if(getItem(position).getInfo()==null)
            type = ADTYPE;
        else type = getItem(position).getInfo().isStatusActive()?ACTIVE:HIDDEN;
        Log.d("pois","getItemViewType :"+type);
        return type;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        switch (getItemViewType(position)) {
            case ADTYPE:
                return adView;
            case ACTIVE:
                return getViewActive(position, convertView, parent);
            case HIDDEN:
                return getViewHidden(position, convertView, parent);
            default:
                return null;
        }

    }

    public View getViewHidden(final int position, View convertView, final ViewGroup parent) {
        final View v;
        final ViewHolderHidden holder;

        if(convertView == null) {
            convertView = mInflater.inflate(R.layout.row_timeline_post_hidden, parent, false);
            holder = new ViewHolderHidden();
            holder.text = (TextView) convertView.findViewById(R.id.timeline_post_text);
            holder.popUpMenu = (Button) convertView.findViewById(R.id.timeline_post_options);
            convertView.setTag(holder);
        }else {
            holder = (ViewHolderHidden)convertView.getTag();
        }

        v = convertView;
        final TimelineListAPKsJson.UserApk entry = getItem(position);
        final long id = entry.getInfo().getId().longValue();

        SpannableString unhidePost = new SpannableString(getContext().getString(R.string.unhide_post));
        unhidePost.setSpan(new UnderlineSpan(), 0, unhidePost.length(), 0);
        holder.popUpMenu.setText(unhidePost);

        if(entry.animate){
            v.startAnimation(AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in));
            entry.animate = false;
        }


        holder.text.setText(entry.getApk().getName());
        holder.popUpMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    mTimeLineManager.unHidePost(id);
                    entry.getInfo().setStatus("active");
                    entry.animate = true;
                    notifyDataSetChanged();
            }
        });

        return v;
    }

    Class appViewActivityClass = Aptoide.getConfiguration().getAppViewActivityClass();

    public View getViewActive(final int position, View convertView, final ViewGroup parent) {
        final View v;
        final ViewHolder holder;

        if(convertView == null){
            convertView = mInflater.inflate(R.layout.row_timeline_post, parent, false);

            holder = new ViewHolder();

            holder.userPhoto = (ImageView) convertView.findViewById(R.id.timeline_post_user_photo);
            holder.userName = (TextView) convertView.findViewById(R.id.timeline_post_user_name);
//            holder.action = (TextView) convertView.findViewById(R.id.timeline_post_user_action);
            holder.time = (TextView) convertView.findViewById(R.id.timeline_post_timestamp);
            holder.popUpMenu = (Button) convertView.findViewById(R.id.timeline_post_options);

            holder.appIcon = (ImageView) convertView.findViewById(R.id.timeline_post_app_icon);
            holder.appName = (TextView) convertView.findViewById(R.id.timeline_post_app_name);
            holder.appVersion = (TextView) convertView.findViewById(R.id.timeline_post_app_version);
            holder.appRepo = (TextView) convertView.findViewById(R.id.timeline_post_app_store);

//            holder.bottomLayout = (LinearLayout) convertView.findViewById(R.id.timeline_post_bottom);
            holder.likesandcomments = (TextView) convertView.findViewById(R.id.timeline_post_likes_and_comments);


            holder.likeButton = (FrameLayout) convertView.findViewById(R.id.timeline_post_like_button) ;
            holder.likeButtonText = (TextView) convertView.findViewById(R.id.timeline_post_like_text);
            holder.commentButton = (FrameLayout) convertView.findViewById(R.id.timeline_post_comment) ;
            holder.openAppViewLayout = (FrameLayout) convertView.findViewById(R.id.timeline_post_open) ;
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder)convertView.getTag();
        }
        v = convertView;
        final TimelineListAPKsJson.UserApk entry = getItem(position);
        final long id = entry.getInfo().getId().longValue();

        //addAppListener(holder.centerLayout, entry.getApk_id().longValue());
        //addLikeListener(holder.likeButton, entry.getPostID().longValue());
        //addCommentListener(holder.commentButton, entry.getPostID().longValue());
        //addOptionsListener(holder.popUpMenu, entry.getPostID().longValue());

        holder.appName.setText(entry.getApk().getName());

        String icon;
        if (entry.getApk().getIcon_hd() != null) {
            icon = entry.getApk().getIcon_hd();
            String sizeString = IconSizes.generateSizeString(getContext());
            String[] splittedUrl = icon.split("\\.(?=[^\\.]+$)");
            icon = splittedUrl[0] + "_" + sizeString + "." + splittedUrl[1];
        } else {
            icon = entry.getApk().getIcon();
        }



        ImageLoader.getInstance().displayImage(icon, holder.appIcon);
        holder.userName.setText(entry.getInfo().getUsername());
        ImageLoader.getInstance().displayImage(entry.getInfo().getAvatar(), holder.userPhoto);


        if(entry.getInfo().isOwned()){
            holder.popUpMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mTimeLineManager.hidePost(id);
                    entry.getInfo().setStatus("hidden");
                    Toast.makeText(getContext(), getContext().getString(R.string.hide_post_info), Toast.LENGTH_SHORT).show();
                    entry.animate = true;
                    notifyDataSetChanged();
                }
            });
            holder.popUpMenu.setVisibility(View.VISIBLE);
        } else {
            holder.popUpMenu.setVisibility(View.GONE);
        }

        if(entry.animate){
            v.startAnimation(AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in));
            entry.animate = false;
        }

        holder.openAppViewLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), appViewActivityClass);
                TimelineListAPKsJson.UserApk.APK apk = entry.getApk();
                i.putExtra("fromRelated", true);
                i.putExtra("appNameplusversion", apk.getName());

                i.putExtra("repoName", apk.getRepo());
                i.putExtra("md5sum", apk.getMd5sum());
                i.putExtra("download_from", "timeline");
                (getContext()).startActivity(i);
            }
        });



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
            if (likes > 0) {
                if (likes == 1) {
                    sb.append(String.valueOf(likes) + " " + getContext().getString(R.string.timeline_like));
                }else{
                    sb.append(String.valueOf(likes) + " " + getContext().getString(R.string.likes));
                }
            }
            if (likes > 0 && comments > 0) {
                sb.append("  ");
            }
            if (comments > 0) {
                if(comments == 1) {
                    sb.append(String.valueOf(comments) + " " + getContext().getString(R.string.comment));
                }else{
                    sb.append(String.valueOf(comments) + " " + getContext().getString(R.string.comments));
                }
            }

            holder.likesandcomments.setText(sb.toString());
            holder.likesandcomments.setVisibility(View.VISIBLE);
            holder.likesandcomments.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mTimeLineManager.openCommentsDialog(id, position);
                }
            });
        }else{
            holder.likesandcomments.setVisibility(View.GONE);
        }
        holder.time.setText(getTime(entry.getInfo().getTimestamp()));
        holder.appVersion.setText(entry.getApk().getVername());
        holder.appRepo.setText(getContext().getString(R.string.store)+": " + entry.getApk().getRepo());

        final boolean isLiked = entry.getInfo().getUserliked().equals("like");
        if(isLiked){
            holder.likeButtonText.setCompoundDrawablesWithIntrinsicBounds(getContext().getResources().getDrawable(R.drawable.ic_action_facebook_like), null, null, null);
            holder.likeButtonText.setTextColor(getContext().getResources().getColor(R.color.facebook));
        }else{
            TypedValue typedV = new TypedValue();
            getContext().getTheme().resolveAttribute( R.attr.icRatingGoodDrawable, typedV, true);
            holder.likeButtonText.setCompoundDrawablesWithIntrinsicBounds(typedV.resourceId, 0, 0, 0);

            getContext().getTheme().resolveAttribute(android.R.attr.textColorSecondary, typedV, true);
            int textColor = getContext().getResources().getColor(typedV.resourceId);
            holder.likeButtonText.setTextColor(textColor);
        }

        changeLikeButtonText(isLiked, holder.likeButtonText);

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
                mTimeLineManager.openCommentsDialog(id, position);
            }
        });
        return v;
    }
    private static void changeLikeButtonText(boolean isLiked, TextView likeButtonText){
        int textId = isLiked ?  R.string.unlike : R.string.like;
        likeButtonText.setText(likeButtonText.getContext().getString(textId));
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
        public Button popUpMenu;

        // CENTER
        public TextView appName;
        public ImageView appIcon;
        public TextView appVersion;
        public TextView appRepo;

        // BOTTOM
        public LinearLayout bottomLayout;
        public TextView likesandcomments;

        // Buttons
        public FrameLayout likeButton;
        public FrameLayout commentButton;
        public TextView likeButtonText;

        //Frame
        public FrameLayout openAppViewLayout;
    }

    static class ViewHolderHidden {
        public TextView text;
        public Button popUpMenu;
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }


    public synchronized void addNativeAd(NativeAd ad,Context context,ArrayList<TimelineListAPKsJson.UserApk> list) {
        if (ad == null) {
            Log.d("pois","addNativeAd , was null");
            return;
        }
        if (this.ad != null) {
            Log.d("pois","addNativeAd , was old");
            // Clean up the old ad before inserting the new one
            this.ad.unregisterView();
            list.remove(AD_INDEX);
            this.ad = null;
            this.notifyDataSetChanged();
        }
        Log.d("pois","addNativeAd , adding native ad");
        this.ad = ad;
        adView = mInflater.inflate(R.layout.row_timeline_ad_unit, null);
        TimelineListAPKsJson.UserApk ret = new UserAPKAd(ad);
        inflateAd(ad, adView, context);
        list.add(AD_INDEX, ret);
        Log.d("pois","addNativeAd , position on "+AD_INDEX);
        this.notifyDataSetChanged();
    }

    public void inflateAd(NativeAd nativeAd, View adView, Context context) {
        // Create native UI using the ad metadata.

        ImageView nativeAdIcon = (ImageView) adView.findViewById(R.id.nativeAdIcon);
        TextView nativeAdTitle = (TextView) adView.findViewById(R.id.nativeAdTitle);
        TextView nativeAdBody = (TextView) adView.findViewById(R.id.nativeAdBody);
        ImageView nativeAdImage = (ImageView) adView.findViewById(R.id.nativeAdImage);
        TextView nativeAdSocialContext = (TextView) adView.findViewById(R.id.nativeAdSocialContext);
        Button nativeAdCallToAction = (Button) adView.findViewById(R.id.nativeAdCallToAction);
        RatingBar nativeAdStarRating = (RatingBar) adView.findViewById(R.id.nativeAdStarRating);

        // Setting the Text
        nativeAdSocialContext.setText(nativeAd.getAdSocialContext());
        nativeAdCallToAction.setText(nativeAd.getAdCallToAction());
        nativeAdCallToAction.setVisibility(View.VISIBLE);
        nativeAdTitle.setText(nativeAd.getAdTitle());
        nativeAdBody.setText(nativeAd.getAdBody());

        // Downloading and setting the ad icon.
        NativeAd.Image adIcon = nativeAd.getAdIcon();
        NativeAd.downloadAndDisplayImage(adIcon, nativeAdIcon);

        // Downloading and setting the cover image.
        NativeAd.Image adCoverImage = nativeAd.getAdCoverImage();
        int bannerWidth = adCoverImage.getWidth();
        int bannerHeight = adCoverImage.getHeight();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        int screenWidth = metrics.widthPixels;
        int screenHeight = metrics.heightPixels;
        nativeAdImage.setLayoutParams(new LinearLayout.LayoutParams(
                screenWidth,
                Math.min((int) (((double) screenWidth / (double) bannerWidth) * bannerHeight), screenHeight / 3)
        ));
        NativeAd.downloadAndDisplayImage(adCoverImage, nativeAdImage);

        NativeAd.Rating rating = nativeAd.getAdStarRating();
        if (rating != null) {
            nativeAdStarRating.setVisibility(View.VISIBLE);
            nativeAdStarRating.setNumStars((int) rating.getScale());
            nativeAdStarRating.setRating((float) rating.getValue());
        } else {
            nativeAdStarRating.setVisibility(View.GONE);
        }

        // Wire up the View with the native ad, the whole nativeAdContainer will be clickable
        nativeAd.registerViewForInteraction(adView);

        // Or you can replace the above call with the following function to specify the clickable areas.
        // nativeAd.registerViewForInteraction(nativeAdContainer, Arrays.asList(nativeAdCallToAction, nativeAdImage));
    }

    public static class UserAPKAd extends TimelineListAPKsJson.UserApk{
        NativeAd ad;
        public UserAPKAd(NativeAd ad){
            this.ad = ad;
        }
    }
}
