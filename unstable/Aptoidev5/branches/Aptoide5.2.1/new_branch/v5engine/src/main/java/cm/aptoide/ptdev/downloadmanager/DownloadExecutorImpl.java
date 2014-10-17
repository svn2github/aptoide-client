package cm.aptoide.ptdev.downloadmanager;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.DisplayMetrics;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.UrlEncodedContent;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.Executors;

import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.model.RollBackItem;
import cm.aptoide.ptdev.preferences.Preferences;
import cm.aptoide.ptdev.preferences.SecurePreferences;
import cm.aptoide.ptdev.utils.AptoideUtils;
import cm.aptoide.ptdev.utils.Base64;
import cm.aptoide.ptdev.webservices.OAuthAccessTokenHandler;
import cm.aptoide.ptdev.webservices.OAuthRefreshAccessTokenHandler;
import cm.aptoide.ptdev.webservices.RegisterAdRequest;
import cm.aptoide.ptdev.webservices.WebserviceOptions;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 08-07-2013
 * Time: 14:53
 * To change this template use File | Settings | File Templates.
 */
public class DownloadExecutorImpl implements DownloadExecutor, Serializable {


    private final FinishedApk apk;
    private final String path;

    public DownloadExecutorImpl(FinishedApk apk) {
        this.apk = apk;
        this.path = apk.getPath();
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromResource(String file, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(file, options);
    }



    @Override
    public void execute() {

        final Context context = Aptoide.getContext();

        Database db = new Database(Aptoide.getDb());
        RollBackItem rollBackItem = null;

        try {

            PackageInfo pkgInfo = context.getPackageManager().getPackageInfo(apk.getApkid(), 0);

            // Update
            File apkFile = new File(pkgInfo.applicationInfo.sourceDir);
            String md5_sum = AptoideUtils.Algorithms.md5Calc(apkFile);

            db.insertRollbackAction(new RollBackItem(apk.getName(), apk.getApkid(), apk.getVersion(), pkgInfo.versionName, apk.getIconPath(), null, md5_sum, RollBackItem.Action.UPDATING, apk.getRepoName()));

        } catch (PackageManager.NameNotFoundException e) {

            // Check if its a downgrade
            if (!db.updateDowngradingAction(apk.getApkid())) {
                // New Installation
                db.insertRollbackAction(new RollBackItem(apk.getName(), apk.getApkid(), apk.getVersion(), null, apk.getIconPath(), null, null, RollBackItem.Action.INSTALLING.setReferrer(apk.getReferrer()), apk.getRepoName()));
            }
        }


        SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(Aptoide.getContext());

        if(apk.getCpiUrl() != null) {

            new Thread(new Runnable() {
                @Override
                public void run() {
                    RegisterAdRequest registerAdRequest = new RegisterAdRequest(context);
                    registerAdRequest.setUrl(apk.getCpiUrl());
                    registerAdRequest.setHttpRequestFactory(AndroidHttp.newCompatibleTransport().createRequestFactory());
                    try {
                        registerAdRequest.loadDataFromNetwork();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    apk.setCpiUrl(null);
                }
            }).start();

        }

        if(sPref.getBoolean(Preferences.SHARE_TIMELINE_DOWNLOAD_BOOL, false) && apk.getId() > 0){
            GenericUrl url = new GenericUrl(WebserviceOptions.WebServicesLink + "3/registerUserApkInstall");

            HashMap<String, String> parameters = new HashMap<String, String>();

            parameters.put("access_token", SecurePreferences.getInstance().getString("access_token", null));
            parameters.put("appid", String.valueOf(apk.getId()));
            HttpContent content = new UrlEncodedContent(parameters);
            try {
                HttpRequestFactory requestFactory = AndroidHttp.newCompatibleTransport().createRequestFactory();
                HttpRequest httpRequest = requestFactory.buildPostRequest(url, content);
                httpRequest.setUnsuccessfulResponseHandler(new OAuthRefreshAccessTokenHandler(parameters, requestFactory));
                httpRequest.executeAsync(Executors.newSingleThreadExecutor());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (Aptoide.IS_SYSTEM || (sPref.getBoolean("allowRoot", true) && canRunRootCommands() && !apk.getApkid().equals(context.getPackageName()))) {

            Intent i = new Intent(context, PermissionsActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            i.putExtra("apk", (Parcelable) apk);
            i.putStringArrayListExtra("permissions", apk.getPermissionsList());
            context.startActivity(i);

        } else {

            Intent install = new Intent(Intent.ACTION_VIEW);
            install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (Build.VERSION.SDK_INT >= 14)
                install.putExtra(Intent.EXTRA_INSTALLER_PACKAGE_NAME, context.getPackageName());
            install.setDataAndType(Uri.fromFile(new File(path)), "application/vnd.android.package-archive");
            Log.d("Aptoide", "Installing app: " + path);
            context.startActivity(install);

        }
    }

    public static boolean canRunRootCommands() {
        boolean retval;
        Process suProcess;

        try {
            suProcess = Runtime.getRuntime().exec("su");

            DataOutputStream os = new DataOutputStream(suProcess.getOutputStream());
            DataInputStream osRes = new DataInputStream(suProcess.getInputStream());

            // Getting the id of the current user to check if this is root
            os.writeBytes("id\n");
            os.flush();

            String currUid = osRes.readLine();
            boolean exitSu;
            if (null == currUid) {
                retval = false;
                exitSu = false;
                Log.d("ROOT", "Can't get root access or denied by user");
            } else if (currUid.contains("uid=0")) {
                retval = true;
                exitSu = true;
                Log.d("ROOT", "Root access granted");
            } else {
                retval = false;
                exitSu = true;
                Log.d("ROOT", "Root access rejected: " + currUid);
            }

            if (exitSu) {
                os.writeBytes("exit\n");
                os.flush();
            }
        } catch (Exception e) {
            // Can't get root !
            // Probably broken pipe exception on trying to write to output stream (os) after su failed, meaning that the device is not rooted

            retval = false;
            Log.d("ROOT", "Root access rejected [" + e.getClass().getName() + "] : " + e.getMessage());
        }

        return retval;
    }

    public static int dpToPixels(Context context, int dpi) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return (int) (dpi * dm.density);
    }




    public static void installWithRoot(final FinishedApk apk) {
        try {
            final Context context = Aptoide.getContext();
            final NotificationManager managerNotification = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            final Process p;
            DataOutputStream os;
            byte[] arrayOfByte = Base64.decode("cG0gaW5zdGFsbCAtciA=", 0);
            String install = new String(arrayOfByte, "UTF-8");
            if (!Aptoide.IS_SYSTEM) {
                p = Runtime.getRuntime().exec("su");
                os = new DataOutputStream(p.getOutputStream());
                // Execute commands that require root access
                os.writeBytes(install + "\"" + apk.getPath() + "\"\n");
                os.flush();
            } else {
                p = Runtime.getRuntime().exec(install   + apk.getPath());
                os = new DataOutputStream(p.getOutputStream());
            }


            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);

            Intent onClick = new Intent(Intent.ACTION_VIEW);
            onClick.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            onClick.setDataAndType(Uri.fromFile(new File(apk.getPath())), "application/vnd.android.package-archive");

            // The PendingIntent to launch our activity if the user selects this notification
            PendingIntent onClickAction = PendingIntent.getActivity(context, 0, onClick, 0);
            mBuilder.setContentTitle(Aptoide.getConfiguration().getMarketName())
                    .setContentText(context.getString(R.string.installing, apk.getName()));


            int size = dpToPixels(context, 36);
            Bitmap bitmap = decodeSampledBitmapFromResource(ImageLoader.getInstance().getDiscCache().get(apk.getIconPath()).getAbsolutePath(), size, size);

            if (bitmap != null) mBuilder.setLargeIcon(bitmap);

            mBuilder.setSmallIcon(android.R.drawable.stat_sys_download);
            mBuilder.setContentIntent(onClickAction);
            mBuilder.setAutoCancel(true);


            managerNotification.notify((int) apk.getAppHashId(), mBuilder.build());
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {

                        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                        int read;
                        char[] buffer = new char[4096];
                        StringBuilder output = new StringBuilder();
                        while ((read = reader.read(buffer)) > 0) {
                            output.append(buffer, 0, read);
                        }
                        reader.close();
                        p.waitFor();

                        String failure = output.toString();
                        if (p.exitValue() != 255 && !failure.toLowerCase(Locale.ENGLISH).contains("failure") && !failure.toLowerCase(Locale.ENGLISH).contains("segmentation")) {
                            // Sucess :-)
                            Log.e("MYTAG-CENAS", String.valueOf(p.exitValue() + " " + failure.toString()));

                            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);

                            Intent onClick = context.getPackageManager().getLaunchIntentForPackage(apk.getApkid());


                            if (onClick == null) {
//                                    onClick = new Intent(Intent.ACTION_VIEW);
//                                    onClick.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                                    onClick.setDataAndType(Uri.fromFile(new File(path)),"application/vnd.android.package-archive");
                                managerNotification.cancel((int) apk.getAppHashId());

                                onClick = new Intent();

                                try {
                                    context.getPackageManager().getPackageInfo(apk.getApkid(), 0);
                                } catch (PackageManager.NameNotFoundException e) {
                                    Log.e("MYTAG-CENAS", "Package not found");
                                    Intent install = new Intent(Intent.ACTION_VIEW);
                                    install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    if (Build.VERSION.SDK_INT >= 14)
                                        install.putExtra(Intent.EXTRA_INSTALLER_PACKAGE_NAME, context.getPackageName());
                                    install.setDataAndType(Uri.fromFile(new File(apk.getPath())), "application/vnd.android.package-archive");
                                    Log.d("Aptoide", "Installing app: " + apk.getPath());
                                    context.startActivity(install);
                                    return;
                                }
                            }

                            // The PendingIntent to launch our activity if the user selects this notification
                            PendingIntent onClickAction = PendingIntent.getActivity(context, 0, onClick, 0);
                            mBuilder.setContentTitle(Aptoide.getConfiguration().getMarketName())
                                    .setContentText(context.getString(R.string.finished_install, apk.getName()));

                            int size = dpToPixels(context, 36);
                            mBuilder.setLargeIcon(decodeSampledBitmapFromResource(ImageLoader.getInstance().getDiscCache().get(apk.getIconPath()).getAbsolutePath(), size, size));
                            mBuilder.setSmallIcon(android.R.drawable.stat_sys_download_done);
                            mBuilder.setContentIntent(onClickAction);
                            mBuilder.setAutoCancel(true);
                            managerNotification.notify((int) apk.getAppHashId(), mBuilder.build());
                            if (Build.VERSION.SDK_INT >= 11)
                                context.getPackageManager().setInstallerPackageName(apk.getApkid(), context.getPackageName());


                        } else {

                            managerNotification.cancel((int) apk.getAppHashId());
                            Intent install = new Intent(Intent.ACTION_VIEW);
                            install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            if (Build.VERSION.SDK_INT >= 14)
                                install.putExtra(Intent.EXTRA_INSTALLER_PACKAGE_NAME, context.getPackageName());
                            install.setDataAndType(Uri.fromFile(new File(apk.getPath())), "application/vnd.android.package-archive");
                            Log.d("Aptoide", "Installing app: " + apk.getPath());
                            context.startActivity(install);
                        }


                    } catch (Exception e) {
                        e.printStackTrace();
                        managerNotification.cancel((int) apk.getAppHashId());
                        Intent install = new Intent(Intent.ACTION_VIEW);
                        install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        if (Build.VERSION.SDK_INT >= 14)
                            install.putExtra(Intent.EXTRA_INSTALLER_PACKAGE_NAME, context.getPackageName());

                        install.setDataAndType(Uri.fromFile(new File(apk.getPath())), "application/vnd.android.package-archive");
                        Log.d("Aptoide", "Installing app: " + apk.getPath());
                        context.startActivity(install);
                    }


                }
            }).start();

            if (!Aptoide.IS_SYSTEM) {
                os.writeBytes("exit\n");
                os.flush();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
