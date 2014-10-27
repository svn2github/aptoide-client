package cm.aptoide.ptdev.services;

import android.accounts.Account;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.graphics.Bitmap;
import android.net.http.AndroidHttpClient;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.RemoteViews;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.UrlEncodedContent;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.Start;
import cm.aptoide.ptdev.preferences.SecurePreferences;
import cm.aptoide.ptdev.webservices.OAuthRefreshAccessTokenHandler;
import cm.aptoide.ptdev.webservices.TimelineCheckRequestSync;
import cm.aptoide.ptdev.webservices.WebserviceOptions;
import cm.aptoide.ptdev.webservices.json.TimelineActivityJson;
import cm.aptoide.ptdev.webservices.timeline.json.ListUserFriendsJson;

/**
 * Created by j-pac on 28-01-2014.
 */
public class TimelineActivitySyncService extends Service  {

    private static TimelineActivitySyncAdapter syncAdapter = null;

    private static final Object wiSyncAdapterLock = new Object();

    @Override
    public void onCreate() {

        synchronized (wiSyncAdapterLock) {
            if(syncAdapter == null) {
                syncAdapter = new TimelineActivitySyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return syncAdapter.getSyncAdapterBinder();
    }

    public class TimelineActivitySyncAdapter extends AbstractThreadedSyncAdapter{

        public TimelineActivitySyncAdapter(Context context, boolean autoInitialize) {

            super(context, autoInitialize);

        }

        @Override
        public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {

            try {

                TimelineActivityJson timelineActivityJson = TimelineCheckRequestSync.getRequest("owned_activity, related_activity");
                int total_likes = timelineActivityJson.getOwned_activity().getTotal_likes().intValue();
                int total_comments = timelineActivityJson.getRelated_activity().getTotal_comments().intValue() + timelineActivityJson.getOwned_activity().getTotal_comments().intValue();

                String notificationText;

                if(total_comments == 0){
                    notificationText = getString(R.string.notification_timeline_new_likes, total_likes);
                }else if(total_likes == 0){
                    notificationText = getString(R.string.notification_timeline_new_comments, total_comments);
                }else {
                    notificationText = getString(R.string.notification_timeline_activity, total_comments, total_likes);
                }


                Intent intent = new Intent(getContext(), Start.class);
                intent.putExtra("fromTimeline", true);

                intent.setClassName(getPackageName(), Aptoide.getConfiguration().getStartActivityClass().getName());
                intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setAction(Intent.ACTION_VIEW);

                PendingIntent resultPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                Notification notification = new NotificationCompat.Builder(getContext())
                        .setSmallIcon(R.drawable.ic_stat_aptoide_fb_notification)
                        .setContentIntent(resultPendingIntent)
                        .setOngoing(false)
                        .setContentTitle(notificationText)
                        .setContentText(getString(R.string.notification_social_timeline)).build();
                ArrayList<String> avatarLinks = new ArrayList<String>();

                try {

                    if ( timelineActivityJson.getOwned_activity().getFriends() != null) setAvatares(avatarLinks, timelineActivityJson.getOwned_activity().getFriends());
                    if ( timelineActivityJson.getRelated_activity().getFriends() != null) setAvatares(avatarLinks, timelineActivityJson.getRelated_activity().getFriends());

                    if (Build.VERSION.SDK_INT >= 16) {
                        RemoteViews expandedView = new RemoteViews(getContext().getPackageName(), R.layout.push_notification_timeline_activity);
                        expandedView.setTextViewText(R.id.description, getString(R.string.notification_timeline_activity, total_comments, total_likes));
                        expandedView.removeAllViews(R.id.linearLayout2);

                        for(String avatar: avatarLinks){
                            Bitmap loadedImage = ImageLoader.getInstance().loadImageSync(avatar);
                            RemoteViews imageView = new RemoteViews(getContext().getPackageName(), R.layout.timeline_friend_iv);
                            imageView.setImageViewBitmap(R.id.friend_avatar, loadedImage);
                            expandedView.addView(R.id.linearLayout2, imageView);
                        }

                        notification.bigContentView = expandedView;
                    }

                    if(!avatarLinks.isEmpty()) {
                        final NotificationManager managerNotification = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
                        managerNotification.notify(86458, notification);
                    }

                }catch (NullPointerException ignored) {ignored.printStackTrace();}

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }


    public void setAvatares(List<String> avatares, List<TimelineActivityJson.Friend> friends){
        for(TimelineActivityJson.Friend friend : friends){
            if(!avatares.contains(friend.getAvatar())){
                avatares.add(friend.getAvatar());

            }
        }
    }
}
