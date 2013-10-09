/*******************************************************************************
 * Copyright (c) 2012 rmateus.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package cm.aptoide.pt;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.*;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.*;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.CursorAdapter;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.util.Xml;
import android.view.*;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import cm.aptoide.pt.Server.State;
import cm.aptoide.pt.adapters.InstalledAdapter;
import cm.aptoide.pt.adapters.UpdatesAdapter;
import cm.aptoide.pt.adapters.ViewPagerAdapter;
import cm.aptoide.pt.configuration.AptoideConfiguration;
import cm.aptoide.pt.contentloaders.SimpleCursorLoader;
import cm.aptoide.pt.services.MainService;
import cm.aptoide.pt.services.MainService.LocalBinder;
import cm.aptoide.pt.services.ServiceManagerDownload;
import cm.aptoide.pt.sharing.WebViewFacebook;
import cm.aptoide.pt.sharing.WebViewTwitter;
import cm.aptoide.pt.util.*;
import cm.aptoide.pt.views.ViewApk;
import cm.aptoide.pt.webservices.login.Login;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.viewpagerindicator.TitlePageIndicator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlSerializer;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends SherlockFragmentActivity implements LoaderCallbacks<Cursor> {
    private final static int AVAILABLE_LOADER = 0;
    private final static int INSTALLED_LOADER = 1;
    private final static int UPDATES_LOADER = 2;
    private final static int LATEST_COMMENTS = -2;
    private final static int LATEST_LIKES = -1;
    public static Context mContext;
    static private ExecutorService featuredEditorsChoiceExecutor = Executors.newSingleThreadExecutor();
    static private ExecutorService featuredTopExecutor = Executors.newSingleThreadExecutor();
    private final String SDCARD = Environment.getExternalStorageDirectory().getPath();
    private final Dialog.OnClickListener addRepoListener = new Dialog.OnClickListener() {

        @Override
        public void onClick(DialogInterface arg0, int arg1) {
            storeUri = ((EditText) alertDialog.findViewById(R.id.edit_uri)).getText().toString();
            dialogAddStore(storeUri, null, null);
        }

    };
    private final OnClickListener addStoreListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            showAddStoreDialog();
        }

    };
    private final ServiceConnection conn = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MainActivity.this.service = ((LocalBinder) service).getService();

            if (ApplicationAptoide.DEFAULTSTORENAME != null && db.getServer("http://" + ApplicationAptoide.DEFAULTSTORENAME + AptoideConfiguration.getInstance().getDomainAptoideStore()) == null) {
                MainActivity.this.service.addStore(Database.getInstance(), "http://" + ApplicationAptoide.DEFAULTSTORENAME + AptoideConfiguration.getInstance().getDomainAptoideStore(), null, null);
            }

            loadUi();
            getInstalled();
            getAllRepoStatus();
            loadFeatured();

            if (Login.isLoggedIn(mContext)) {
                loadRecommended();
            }

            if (getIntent().hasExtra("new_updates")) {
                pager.setCurrentItem(3);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    private final Dialog.OnClickListener searchStoresListener = new Dialog.OnClickListener() {

        @Override
        public void onClick(DialogInterface arg0, int arg1) {
            Uri uri = Uri.parse("http://m.aptoide.com/more/toprepos");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }

    };
    private final BroadcastReceiver statusReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (depth.equals(ListDepth.STORES)) {
                availableLoader.forceLoad();
            }
        }
    };
    private final BroadcastReceiver updatesReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                installedLoader.forceLoad();
                updatesLoader.forceLoad();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (!depth.equals(ListDepth.STORES)) {
                Long server_id = intent.getExtras().getLong("serverid");
                if (refreshClick && server_id == store_id) {
                    refreshClick = false;
                    availableView.findViewById(R.id.refresh_view_layout).setVisibility(View.VISIBLE);
                    availableView
                            .findViewById(R.id.refresh_view_layout)
                            .findViewById(R.id.refresh_view)
                            .startAnimation(AnimationUtils.loadAnimation(mContext, android.R.anim.fade_in));
                }
            }

        }
    };
    protected Order order;
    ArrayList<HashMap<String, String>> values;
    LinearLayout breadcrumbs;
    LinearLayout banner;
    ViewPager pager;
    ImageView bannerStoreAvatar;
    TextView bannerStoreName;
    AutoScaleTextView bannerStoreDescription;
    ImageLoader loader;
    Editor editor;
    int a = 0;
    private HashMap<ListDepth, ListViewPosition> scrollMemory = new HashMap<ListDepth, ListViewPosition>();
    private String LOCAL_PATH = AptoideConfiguration.getInstance().getPathCache();
    private View addStoreButton;
    private AlertDialog alertDialog;
    private View alertDialogView;
    private AvailableListAdapter availableAdapter;
    private ListView availableListView;
    private Loader<Cursor> availableLoader;
    private View availableView;
    private View updateView;
    private long category_id;
    private long category2_id;
    private Database db;
    private ListDepth depth = ListDepth.STORES;
    private View featuredView;
    private InstalledAdapter installedAdapter;
    private Loader<Cursor> installedLoader;
    private ListView installedView;
    private CheckBox joinStores;
    private boolean joinStores_boolean = false;
    private TextView pb;
    private boolean refreshClick = true;
    private MainService service;
    private BroadcastReceiver parseFailedReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            Builder dialogBuilder = new AlertDialog.Builder(mContext);
            final AlertDialog parseErrorDialog = dialogBuilder.create();
            parseErrorDialog.setTitle(getText(R.string.parse_error));
            parseErrorDialog.setIcon(android.R.drawable.ic_dialog_alert);
            parseErrorDialog.setMessage(getText(R.string.parse_error_loading));
            parseErrorDialog.setCancelable(false);
            parseErrorDialog.setButton(Dialog.BUTTON_POSITIVE, getString(android.R.string.yes), new Dialog.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int arg1) {
                    getAllRepoStatus();
                }
            });
            parseErrorDialog.setButton(Dialog.BUTTON_NEGATIVE, getString(android.R.string.no), new Dialog.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int arg1) {
                    dialog.cancel();
                }
            });
            parseErrorDialog.show();
        }
    };
    private long store_id;
    private CursorAdapter updatesAdapter;
    private Loader<Cursor> updatesLoader;
    private ListView updatesListView;
    private BroadcastReceiver loginReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            loadRecommended();
        }
    };
    private BroadcastReceiver redrawInstalledReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            try {
                installedLoader.forceLoad();
                updatesLoader.forceLoad();

                if (PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getBoolean("showUpdatesNotification", true)) {
                    if (service != null) {
                        service.setUpdatesNotification(Database.getInstance().getUpdates(Order.DATE));
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };
    private BroadcastReceiver openUpdatesReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            try {
                pager.setCurrentItem(3);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };
    private BroadcastReceiver newRepoReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("newrepo")) {
                ArrayList<String> repos = intent.getExtras().getStringArrayList("newrepo");
                for (final String uri2 : repos) {
                    if (Database.getInstance().getServer(RepoUtils.formatRepoUri(uri2)) != null) {
                        Toast.makeText(MainActivity.this, getString(R.string.store_already_added), Toast.LENGTH_LONG).show();
                    } else {

                        Builder dialogBuilder = new AlertDialog.Builder(mContext);
                        final AlertDialog addNewRepoDialog = dialogBuilder.create();
                        addNewRepoDialog.setTitle(getString(R.string.add_store));
                        addNewRepoDialog.setIcon(android.R.drawable.ic_menu_add);
                        addNewRepoDialog.setMessage((getString(R.string.newrepo_alrt) + uri2 + " ?"));

                        addNewRepoDialog.setCancelable(false);
                        addNewRepoDialog.setButton(Dialog.BUTTON_POSITIVE, getString(android.R.string.yes), new Dialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                dialogAddStore(uri2, null, null);
                            }
                        });
                        addNewRepoDialog.setButton(Dialog.BUTTON_NEGATIVE, getString(android.R.string.no), new Dialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int arg1) {
                                dialog.cancel();
                            }
                        });
                        addNewRepoDialog.show();
                    }
                }
            }

        }
    };
    private String storeUri = AptoideConfiguration.getInstance().getDefaultStore() + AptoideConfiguration.getInstance().getDomainAptoideStore();
    private ServiceManagerDownload serviceDownloadManager;
    private ServiceConnection serviceManagerConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the object we can use to
            // interact with the service. We are communicating with the
            // service using AIDL, so here we set the remote service interface.
            serviceDownloadManager = ((ServiceManagerDownload.LocalBinder) service).getService();
            ((UpdatesAdapter) updatesAdapter).setServiceDownloadManager(serviceDownloadManager);
            Log.v("Aptoide-UpdatesAdapter", "Connected to ServiceDownloadManager");

        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            Log.v("Aptoide-UpdatesAdapter", "Disconnected from ServiceDownloadManager");
        }
    };
    private boolean registered = false;
    private BroadcastReceiver storePasswordReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            System.out.println("server url " + intent.getExtras().getString("url"));
            showUpdateStoreCredentialsDialog(intent.getStringExtra("url"));
        }
    };
    private OnCheckedChangeListener adultCheckedListener = new OnCheckedChangeListener() {

        ProgressDialog pd;

        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {
            if (isChecked) {

                Builder dialogBuilder = new AlertDialog.Builder(mContext);
                final AlertDialog adultContentDialog = dialogBuilder.create();
                adultContentDialog.setTitle(getString(R.string.adult_content));
                adultContentDialog.setIcon(android.R.drawable.ic_menu_info_details);
                adultContentDialog.setMessage(getString(R.string.are_you_adult));
                adultContentDialog.setCancelable(false);
                adultContentDialog.setButton(Dialog.BUTTON_POSITIVE, getString(android.R.string.yes), new Dialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        editor.putBoolean("matureChkBox", false);
                        editor.commit();
                        pd = new ProgressDialog(mContext);
                        pd.setMessage(getString(R.string.please_wait));
                        pd.show();
                        new Thread(new Runnable() {
                            public void run() {
                                // loadUItopapps();
                                redrawAll();
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        pd.dismiss();
                                    }
                                });
                            }
                        }).start();
                    }
                });
                adultContentDialog.setButton(Dialog.BUTTON_NEGATIVE, getString(android.R.string.no), new Dialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        ((ToggleButton) featuredView.findViewById(R.id.toggleButton1)).setChecked(false);
                        // if(adult!=null){
                        // adult.setChecked(false);
                        // }
                    }
                });
                adultContentDialog.show();

            } else {
                editor.putBoolean("matureChkBox", true);
                editor.commit();
                pd = new ProgressDialog(mContext);
                pd.setMessage(getString(R.string.please_wait));
                pd.show();
                new Thread(new Runnable() {
                    public void run() {
                        // loadUItopapps();
                        redrawAll();
                        runOnUiThread(new Runnable() {
                            public void run() {
                                pd.dismiss();
                            }
                        });
                    }
                }).start();
            }

        }
    };
    private boolean isDisconnect;

    private void loadFeatured() {

        featuredEditorsChoiceExecutor.submit(new Runnable() {

            public void run() {
                loadUIEditorsApps();
                File f = null;

                try {
                    SAXParserFactory spf = SAXParserFactory.newInstance();
                    SAXParser sp = spf.newSAXParser();
                    NetworkUtils utils = new NetworkUtils();
                    String url;
                    f = File.createTempFile("tempFile", "");
                    String countryCode = Geolocation.getCountryCode(mContext);

                    Log.d("Aptoide-Geolocation", "Using countrycode: " + countryCode);

                    if (ApplicationAptoide.CUSTOMEDITORSCHOICE) {
                        url = getEditorsChoiceURL(ApplicationAptoide.DEFAULTSTORENAME, countryCode);

                        if (((HttpURLConnection) new URL(url).openConnection())
                                .getResponseCode() != 200) {
                            url = getEditorsChoiceURL(AptoideConfiguration.getInstance().getDefaultStore(), countryCode);
                        }

                    } else {
                        url = getEditorsChoiceURL(AptoideConfiguration.getInstance().getDefaultStore(), countryCode);
                    }

                    ApplicationAptoide.log.info("EditorsUrl is: " + url);

                    long date = utils.getLastModified(new URL(url));
                    long cachedDate = db.getEditorsChoiceHash();

                    Log.d("Editors", "Date is " + date);
                    Log.d("Editors", "CachedDate is " + cachedDate);

                    if (cachedDate < date) {

                        BufferedInputStream bis = new BufferedInputStream(utils.getInputStream(url, null, null, mContext), 8 * 1024);
                        OutputStream out = new FileOutputStream(f);
                        byte buf[] = new byte[1024];
                        int len;
                        while ((len = bis.read(buf)) > 0)
                            out.write(buf, 0, len);
                        out.close();
                        bis.close();
                        Server server = new Server();

                        Database.getInstance().beginTransaction();
                        try{
                            db.deleteEditorsChoice();
                            loadUIEditorsApps();
                            sp.parse(f, new HandlerEditorsChoice(server));
                        }catch (SAXException e){

                        }
                        db.insertEditorsChoiceHash(date);
                        Database.getInstance().endTransaction();

                        loadUIEditorsApps();

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (f != null) f.delete();
                }


            }

            private String getEditorsChoiceURL(String store, String countryCode) {

                if (countryCode.length() > 0) {
                    return "http://" + store + AptoideConfiguration.getInstance().getDomainAptoideStore() + AptoideConfiguration.getInstance().getEditorsPath() + "?country=" + countryCode;
                }
                return "http://" + store + AptoideConfiguration.getInstance().getDomainAptoideStore() + AptoideConfiguration.getInstance().getEditorsPath();
            }

        });

        featuredTopExecutor.submit(new Runnable() {

            public void run() {

                loadUItopapps();
                File f = null;
                try {
                    SAXParserFactory spf = SAXParserFactory.newInstance();
                    SAXParser sp = spf.newSAXParser();
                    Server server = new Server();
                    server.id = 1;
                    NetworkUtils utils = new NetworkUtils();

                    String url;
                    if (ApplicationAptoide.CUSTOMEDITORSCHOICE) {
                        url = "http://" + ApplicationAptoide.DEFAULTSTORENAME + AptoideConfiguration.getInstance().getDomainAptoideStore() + AptoideConfiguration.getInstance().getTopPath();

                        if (((HttpURLConnection) new URL(url).openConnection())
                                .getResponseCode() != 200) {
                            url = "http://" + AptoideConfiguration.getInstance().getDefaultStore() + AptoideConfiguration.getInstance().getDomainAptoideStore() + AptoideConfiguration.getInstance().getTopPath();
                        }

                    } else {
                        url = "http://" + AptoideConfiguration.getInstance().getDefaultStore() + AptoideConfiguration.getInstance().getDomainAptoideStore() + AptoideConfiguration.getInstance().getTopPath();
                    }

                    long date = utils.getLastModified(new URL(url));

                    long cachedDate;
                    try {
                        cachedDate = Long.parseLong(db.getRepoHash(server.id, Category.TOPFEATURED));
                    } catch (Exception e) {
                        cachedDate = 0;
                    }


                    if (cachedDate < date) {

                        BufferedInputStream bis = new BufferedInputStream(utils.getInputStream(url, null, null, mContext), 8 * 1024);
                        f = File.createTempFile("tempFile", "");
                        OutputStream out = new FileOutputStream(f);
                        byte buf[] = new byte[1024];
                        int len;
                        while ((len = bis.read(buf)) > 0)
                            out.write(buf, 0, len);
                        out.close();
                        bis.close();
                        Database.getInstance().beginTransaction();
                        try{
                            db.deleteFeaturedTopApps();
                            loadUItopapps();
                            sp.parse(f, new HandlerFeaturedTop(server));
                            db.insertFeaturedTopHash(date);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        Database.getInstance().endTransaction();
                        loadUItopapps();
                        f.delete();
                    }
                } catch (ParserConfigurationException e) {
                    e.printStackTrace();
                } catch (SAXException e) {
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (f != null) {
                        f.delete();
                    }
                }

            }

        });

    }

    private void loadUIEditorsApps() {

        final int[] res_ids = {R.id.central, R.id.topleft, R.id.topright, R.id.bottomleft, R.id.bottomcenter, R.id.bottomright};
        final ArrayList<HashMap<String, String>> image_urls = db.getFeaturedGraphics();
        final HashMap<String, String> image_url_highlight = db.getHighLightFeature();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (image_url_highlight.size() > 0) {
                    a = 1;
                    ImageView v = (ImageView) featuredView.findViewById(res_ids[0]);
                    // imageLoader.DisplayImage(-1, image_url_highlight.get("url"), v,
                    // mContext);
                    DisplayImageOptions options = new DisplayImageOptions.Builder().displayer(new FadeInBitmapDisplayer(1000)).imageScaleType(ImageScaleType.NONE).cacheOnDisc().cacheInMemory().build();
                    ImageLoader.getInstance().displayImage(image_url_highlight.get("url"), v, options);
                    v.setTag(image_url_highlight.get("id"));
                    v.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View arg0) {

                            Intent i = new Intent(MainActivity.this, ApkInfo.class);
                            i.putExtra("_id", Long.parseLong((String) arg0.getTag()));
                            i.putExtra("top", false);
                            i.putExtra("category", Category.EDITORSCHOICE.ordinal());
                            startActivity(i);

                        }
                    });
                    // v.setOnClickListener(featuredListener);
                }
            }
        });


        Collections.shuffle(image_urls);
        runOnUiThread(new Runnable() {

            public void run() {


                for (int i = a; i != res_ids.length && i < image_urls.size(); i++) {
                    try {
                        ImageView v = (ImageView) featuredView
                                .findViewById(res_ids[i]);

                        // imageLoader.DisplayImage(-1,
                        // image_urls.get(i).get("url"), v, mContext);
                        DisplayImageOptions options = new DisplayImageOptions.Builder().displayer(new FadeInBitmapDisplayer(1000)).imageScaleType(ImageScaleType.NONE).cacheOnDisc().cacheInMemory().build();

                        com.nostra13.universalimageloader.core.ImageLoader
                                .getInstance().displayImage(image_urls.get(i - a).get("url"), v, options);

                        v.setTag(image_urls.get(i - a).get("id"));
                        v.setOnClickListener(new OnClickListener() {

                            @Override
                            public void onClick(View arg0) {
                                Intent i = new Intent(MainActivity.this,
                                        ApkInfo.class);
                                i.putExtra("_id",
                                        Long.parseLong((String) arg0.getTag()));
                                i.putExtra("top", false);
                                i.putExtra("category",
                                        Category.EDITORSCHOICE.ordinal());
                                startActivity(i);
                            }
                        });
                        // v.setOnClickListener(featuredListener);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }


            }
        });
    }

    private void loadUItopapps() {
        ((ToggleButton) featuredView.findViewById(R.id.toggleButton1))
                .setOnCheckedChangeListener(null);
        Cursor c = db.getFeaturedTopApps();

        values = new ArrayList<HashMap<String, String>>();
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            HashMap<String, String> item = new HashMap<String, String>();
            item.put("name", c.getString(1));
            item.put("icon", db.getIconsPath(0, Category.TOPFEATURED) + c.getString(4));
            item.put("rating", c.getString(5));
            item.put("id", c.getString(0));
            item.put("apkid", c.getString(7));
            item.put("vercode", c.getString(8));
            item.put("vername", c.getString(2));
            item.put("downloads", c.getString(6));
            if (values.size() == 26) {
                break;
            }
            values.add(item);
        }
        c.close();

        runOnUiThread(new Runnable() {

            public void run() {

                LinearLayout ll = (LinearLayout) featuredView.findViewById(R.id.container);
                ll.removeAllViews();
                LinearLayout llAlso = new LinearLayout(MainActivity.this);
                llAlso.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
                llAlso.setOrientation(LinearLayout.HORIZONTAL);

                try {

                    for (int i = 0; i != values.size(); i++) {

                        LinearLayout txtSamItem = (LinearLayout) getLayoutInflater().inflate(R.layout.row_grid_item, null);
                        ((TextView) txtSamItem.findViewById(R.id.name)).setText(values.get(i).get("name"));
                        // ((TextView) txtSamItem.findViewById(R.id.version))
                        // .setText(getString(R.string.version) +" "+
                        // values.get(i).get("vername"));
                        ((TextView) txtSamItem.findViewById(R.id.downloads)).setText("(" + values.get(i).get("downloads") + " " + getString(R.string.downloads) + ")");

                        com.nostra13.universalimageloader.core.ImageLoader.getInstance().displayImage(values.get(i).get("icon"), (ImageView) txtSamItem.findViewById(R.id.icon));

                        // imageLoader.DisplayImage(-1, values.get(i).get("icon"),
                        // (ImageView) txtSamItem.findViewById(R.id.icon),
                        // mContext);
                        float stars;
                        try {
                            stars = Float.parseFloat(values.get(i).get("rating"));
                        } catch (Exception e) {
                            stars = 0f;
                        }
                        ((RatingBar) txtSamItem.findViewById(R.id.rating)).setRating(stars);
                        ((RatingBar) txtSamItem.findViewById(R.id.rating)).setIsIndicator(true);
                        txtSamItem.setPadding(10, 0, 0, 0);
                        txtSamItem.setTag(values.get(i).get("id"));
                        txtSamItem.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT, getPixels(80), 1));
                        // txtSamItem.setOnClickListener(featuredListener);
                        txtSamItem.setOnClickListener(new OnClickListener() {

                            @Override
                            public void onClick(View arg0) {
                                Intent i = new Intent(MainActivity.this, ApkInfo.class);
                                long id = Long.parseLong((String) arg0.getTag());
                                i.putExtra("_id", id);
                                i.putExtra("top", true);
                                i.putExtra("category", Category.TOPFEATURED.ordinal());
                                startActivity(i);
                            }
                        });

                        txtSamItem.measure(0, 0);

                        if (i % 2 == 0) {
                            ll.addView(llAlso);

                            llAlso = new LinearLayout(MainActivity.this);
                            llAlso.setLayoutParams(new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT, getPixels(80)));
                            llAlso.setOrientation(LinearLayout.HORIZONTAL);
                            llAlso.addView(txtSamItem);
                        } else {
                            llAlso.addView(txtSamItem);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                ll.addView(llAlso);
                SharedPreferences sPref = PreferenceManager
                        .getDefaultSharedPreferences(mContext);
                // System.out.println(sPref.getString("app_rating",
                // "All").equals(
                // "Mature"));
                ((ToggleButton) featuredView.findViewById(R.id.toggleButton1))
                        .setChecked(!sPref.getBoolean("matureChkBox", false));
                ((ToggleButton) featuredView.findViewById(R.id.toggleButton1))
                        .setOnCheckedChangeListener(adultCheckedListener);
            }

        });
    }

    public static int getPixels(int dipValue) {
        Resources r = mContext.getResources();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, r.getDisplayMetrics());
    }

    public void getAllRepoStatus() {
        final HashMap<String, Long> serversToParse = new HashMap<String, Long>();
        new Thread(new Runnable() {

            @Override
            public void run() {
                String repos = "";
                String hashes = "";
                Cursor cursor = db.getStores(false);
                int i = 0;
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    String repo;
                    if (i > 0) {
                        repos = repos + ",";
                        hashes = hashes + ",";
                    }
                    repo = cursor.getString(1);
                    repo = RepoUtils.split(repo);
                    repos = repos + repo;
                    hashes = hashes + cursor.getString(2);
                    i++;
                    serversToParse.put(repo, cursor.getLong(0));

                }
                cursor.close();

                if (!serversToParse.isEmpty()) {

                    String url = AptoideConfiguration.getInstance().getWebServicesUri() + "webservices/listRepositoryChange/"
                            + repos + "/" + hashes + "/json";

                    try {
                        HttpURLConnection connection = (HttpURLConnection) new URL(
                                url).openConnection();
                        connection.connect();
                        int rc = connection.getResponseCode();
                        if (rc == 200) {
                            NetworkUtils utils = new NetworkUtils();
                            JSONObject json = utils
                                    .getJsonObject(url, mContext);

                            JSONArray array = json.getJSONArray("listing");

                            for (int o = 0; o != array.length(); o++) {
                                boolean parse = Boolean.parseBoolean(array.getJSONObject(o).getString("hasupdates"));
                                long id = serversToParse.get(array.getJSONObject(o).getString("repo"));
                                Server server = db.getServer(id, false);
                                server.isBare = array.getJSONObject(o).has("appscount");

                                if (parse) {
                                    service.parseServer(db, server);
                                } else {
                                    service.parseTop(db, server);
                                    service.parseLatest(db, server);
                                    service.addStoreInfo(db, server);
                                }

                            }

                        }
                        connection.disconnect();

                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        if (!ApplicationAptoide.MULTIPLESTORES) {
                            getApplicationContext().sendBroadcast(new Intent("PARSE_FAILED"));
                        }
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

    }

    @Override
    protected void onActivityResult(int arg0, int arg1, Intent arg2) {
        super.onActivityResult(arg0, arg1, arg2);
        if (installedLoader != null)
            installedLoader.forceLoad();
        if (updatesLoader != null)
            updatesLoader.forceLoad();
        if (availableLoader != null)
            availableLoader.forceLoad();
        new Thread(new Runnable() {

            @Override
            public void run() {
                loadUItopapps();
            }
        }).start();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AptoideThemePicker.setAptoideTheme(this);
        super.onCreate(savedInstanceState);

        mContext = this;

        File sdcard_file = new File(SDCARD);
        if (!sdcard_file.exists() || !sdcard_file.canWrite()) {

            Builder dialogBuilder = new AlertDialog.Builder(this);
            final AlertDialog noSDDialog = dialogBuilder.create();
            noSDDialog.setTitle(getText(R.string.remote_in_noSD_title));
            noSDDialog.setIcon(android.R.drawable.ic_dialog_alert);
            noSDDialog.setMessage(getText(R.string.remote_in_noSD));
            noSDDialog.setCancelable(false);
            noSDDialog.setButton(Dialog.BUTTON_NEUTRAL, getString(android.R.string.ok), new Dialog.OnClickListener() {
                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    finish();
                }
            });
            noSDDialog.show();
        } else {
            StatFs stat = new StatFs(sdcard_file.getPath());
            long blockSize = stat.getBlockSize();
            long totalBlocks = stat.getBlockCount();
            long availableBlocks = stat.getAvailableBlocks();

            long total = (blockSize * totalBlocks) / 1024 / 1024;
            long avail = (blockSize * availableBlocks) / 1024 / 1024;
            Log.d("Aptoide", "* * * * * * * * * *");
            Log.d("Aptoide", "Total: " + total + " Mb");
            Log.d("Aptoide", "Available: " + avail + " Mb");

            if (avail < 10) {
                Log.d("Aptoide", "No space left on SDCARD...");
                Log.d("Aptoide", "* * * * * * * * * *");

                Builder dialogBuilder = new AlertDialog.Builder(this);

                final AlertDialog noSpaceDialog = dialogBuilder.create();
                noSpaceDialog.setIcon(android.R.drawable.ic_dialog_alert);
                noSpaceDialog.setTitle(getText(R.string.remote_in_noSD_title));
                noSpaceDialog.setMessage(getText(R.string.remote_in_noSDspace));
                noSpaceDialog.setButton(Dialog.BUTTON_NEUTRAL, getText(android.R.string.ok), new Dialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        finish();
                    }
                });
                noSpaceDialog.show();
            } else {

                SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(mContext);
                editor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();

                if (!sPref.contains("matureChkBox")) {

                    editor.putBoolean("matureChkBox", ApplicationAptoide.MATURECONTENTSWITCHVALUE);
                    SharedPreferences sPrefOld = getSharedPreferences("aptoide_prefs", MODE_PRIVATE);
                    if (sPrefOld.getString("app_rating", "none").equals("Mature")) {
                        editor.putBoolean("matureChkBox", false);
                    }

                }

                if (sPref.getString("myId", null) == null) {
                    String rand_id = UUID.randomUUID().toString();
                    editor.putString("myId", rand_id);
                }


                if (sPref.getInt("scW", 0) == 0 || sPref.getInt("scH", 0) == 0) {
                    DisplayMetrics dm = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(dm);
                    editor.putInt("scW", dm.widthPixels);
                    editor.putInt("scH", dm.heightPixels);
                }
                editor.commit();
                File file = new File(AptoideConfiguration.getInstance().getPathCacheApks());
                if (!file.exists()) {
                    file.mkdirs();
                }

                new Thread(new Runnable() {

                    @Override
                    public void run() {

                        // Note the L that tells the compiler to interpret the
                        // number as a Long
                        final long MAXFILEAGE = 2678400000L; // 1 month in
                        // milliseconds

                        // Get file handle to the directory. In this case the
                        // application files dir
                        File dir = new File(LOCAL_PATH);


                        if (dir.mkdir()) {
                            // Optain onGoingArrayList of files in the directory.
                            // listFiles() returns a onGoingArrayList of File objects to each
                            // file found.
                            File[] files = dir.listFiles();

                            // Loop through all files
                            for (File f : files) {

                                // Get the last modified date. Miliseconds since
                                // 1970
                                long lastmodified = f.lastModified();

                                // Do stuff here to deal with the file..
                                // For instance delete files older than 1 month
                                if (lastmodified + MAXFILEAGE < System
                                        .currentTimeMillis()) {
                                    f.delete();
                                }
                            }
                        }
                    }
                }).start();
                db = Database.getInstance();

                Intent i = new Intent(mContext, MainService.class);
                startService(i);
                bindService(i, conn, Context.BIND_AUTO_CREATE);
                order = Order.values()[PreferenceManager.getDefaultSharedPreferences(mContext).getInt("order_list", 0)];

                registerReceiver(updatesReceiver, new IntentFilter("update"));
                registerReceiver(statusReceiver, new IntentFilter("status"));
                registerReceiver(loginReceiver, new IntentFilter("login"));
                registerReceiver(storePasswordReceiver, new IntentFilter("401"));
                registerReceiver(redrawInstalledReceiver, new IntentFilter("pt.caixamagica.aptoide.REDRAW"));
                registerReceiver(openUpdatesReceiver, new IntentFilter("open_updates"));
                if (!ApplicationAptoide.MULTIPLESTORES) {
                    registerReceiver(parseFailedReceiver, new IntentFilter("PARSE_FAILED"));
                }

                registerReceiver(newRepoReceiver, new IntentFilter("pt.caixamagica.aptoide.NEWREPO"));
                registered = true;


                boolean serversFileIsEmpty = true;

                if (sPref.getBoolean("firstrun", true)) {

                    if (new File(LOCAL_PATH + "/servers.xml").exists()
                            && ApplicationAptoide.DEFAULTSTORENAME == null) {
                        try {

                            SAXParserFactory spf = SAXParserFactory.newInstance();
                            SAXParser sp = spf.newSAXParser();

                            MyappHandler handler = new MyappHandler();

                            sp.parse(new File(LOCAL_PATH + "/servers.xml"), handler);
                            ArrayList<String> server = handler.getServers();
                            if (server.isEmpty()) {
                                serversFileIsEmpty = true;
                            } else {
                                getIntent().putExtra("newrepo", server);
                            }


                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                    editor.putBoolean("firstrun", false);
                    editor.putBoolean("orderByCategory", true);
                    editor.commit();
                }


                if (getIntent().hasExtra("newrepo")) {
                    ArrayList<String> repos = getIntent().getExtras().getStringArrayList("newrepo");
                    for (final String uri2 : repos) {

                        if (Database.getInstance().getServer(RepoUtils.formatRepoUri(uri2)) != null) {
                            Toast.makeText(this, getString(R.string.store_already_added), Toast.LENGTH_LONG).show();
                        } else {

                            Builder dialogBuilder = new AlertDialog.Builder(mContext);
                            final AlertDialog addNewRepoDialog = dialogBuilder.create();
                            addNewRepoDialog.setTitle(getString(R.string.add_store));
                            addNewRepoDialog.setIcon(android.R.drawable.ic_menu_add);
                            addNewRepoDialog.setMessage((getString(R.string.newrepo_alrt) + uri2 + " ?"));


                            addNewRepoDialog.setCancelable(false);
                            addNewRepoDialog.setButton(Dialog.BUTTON_POSITIVE, getString(android.R.string.yes), new Dialog.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    dialogAddStore(uri2, null, null);
                                }
                            });
                            addNewRepoDialog.setButton(Dialog.BUTTON_NEGATIVE, getString(android.R.string.no), new Dialog.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int arg1) {
                                    dialog.cancel();
                                }
                            });
                            addNewRepoDialog.show();
                        }

                    }
                } else if (db.getStoresCount() == 0 && ApplicationAptoide.DEFAULTSTORENAME == null && serversFileIsEmpty) {

                    Builder dialogBuilder = new AlertDialog.Builder(mContext);
                    final AlertDialog addAppsRepoDialog = dialogBuilder.create();
                    addAppsRepoDialog.setTitle(getString(R.string.add_store));
                    addAppsRepoDialog.setIcon(android.R.drawable.ic_menu_add);
                    addAppsRepoDialog.setMessage(getString(R.string.myrepo_alrt) + "\n" + "http://apps.store.aptoide.com/");
                    //TextView message = (TextView) simpleView.findViewById(R.id.dialog_message);
                    //message.setText(getString(R.string.myrepo_alrt) + "\n" + "http://apps.store.aptoide.com/");
                    addAppsRepoDialog.setCancelable(false);
                    addAppsRepoDialog.setButton(Dialog.BUTTON_POSITIVE, getString(android.R.string.yes), new Dialog.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            dialogAddStore("http://apps.store.aptoide.com", null, null);
                        }
                    });
                    addAppsRepoDialog.setButton(Dialog.BUTTON_NEGATIVE, getString(android.R.string.no), new Dialog.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int arg1) {
                            dialog.cancel();
                        }
                    });
                    addAppsRepoDialog.show();
                }
                new AutoUpdate(this).execute();

            }


            featuredView = LayoutInflater.from(mContext).inflate(R.layout.page_featured, null);
            availableView = LayoutInflater.from(mContext).inflate(R.layout.page_available, null);
            updateView = LayoutInflater.from(mContext).inflate(R.layout.page_updates, null);
            banner = (LinearLayout) availableView.findViewById(R.id.banner);
            breadcrumbs = (LinearLayout) LayoutInflater.from(mContext).inflate(R.layout.breadcrumb_container, null);
            installedView = new ListView(mContext);
            updatesListView = (ListView) updateView.findViewById(R.id.updates_list);

            availableListView = (ListView) availableView.findViewById(R.id.available_list);
            joinStores = (CheckBox) availableView.findViewById(R.id.join_stores);

            availableAdapter = new AvailableListAdapter(mContext, null, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
            installedAdapter = new InstalledAdapter(mContext, null, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER, db);
            updatesAdapter = new UpdatesAdapter(mContext, null, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

            pb = (TextView) availableView.findViewById(R.id.loading_pb);
            addStoreButton = availableView.findViewById(R.id.add_store);

            bannerStoreAvatar = (ImageView) banner.findViewById(R.id.banner_store_avatar);
            bannerStoreName = (TextView) banner.findViewById(R.id.banner_store_name);
            bannerStoreDescription = (AutoScaleTextView) banner.findViewById(R.id.banner_store_description);

            try {

                if (PreferenceManager.getDefaultSharedPreferences(this).getInt("version", 0) < getPackageManager().getPackageInfo(getPackageName(), 0).versionCode) {
                    ApplicationAptoide.setRestartLauncher(true);
                    PreferenceManager.getDefaultSharedPreferences(this).edit().putInt("version", getPackageManager().getPackageInfo(getPackageName(), 0).versionCode).commit();
                }

                if (ApplicationAptoide.isRestartLauncher() && !ApplicationAptoide.BRAND.equals("brand_aptoide")) {

                    Builder dialogBuilder = new AlertDialog.Builder(this);
                    final AlertDialog restartLauncherDialog = dialogBuilder.create();
                    restartLauncherDialog.setTitle(getString(R.string.payment_warning_title));
                    restartLauncherDialog.setIcon(android.R.drawable.ic_menu_info_details);
                    restartLauncherDialog.setMessage(getString(R.string.restart_launcher, ApplicationAptoide.MARKETNAME));
                    restartLauncherDialog.setCancelable(false);

                    restartLauncherDialog.setButton(Dialog.BUTTON_NEUTRAL, getString(android.R.string.ok), new Dialog.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            ApplicationAptoide.restartLauncher(MainActivity.this);
                            ApplicationAptoide.setRestartLauncher(false);
                        }
                    });
                    restartLauncherDialog.show();
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(this, "ERROR", Toast.LENGTH_SHORT).show();
            }

        }
    }

    private void dialogAddStore(final String url, final String username, final String password) {
        final ProgressDialog pd = new ProgressDialog(mContext);
        pd.setMessage(getString(R.string.please_wait));
        pd.show();

        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    addStore(url, username, password);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            if (pd.isShowing()) {
                                pd.dismiss();
                            }
                            refreshAvailableList(true);
                        }
                    });

                }

            }
        }).start();
    }

    private void refreshAvailableList(boolean setAdapter) {
        if (depth.equals(ListDepth.STORES)) {
            availableView.findViewById(R.id.add_store_layout).setVisibility(View.VISIBLE);
            registerForContextMenu(availableListView);
            availableListView.setLongClickable(true);
            banner.setVisibility(View.GONE);
        } else {
            unregisterForContextMenu(availableListView);
            availableView.findViewById(R.id.add_store_layout).setVisibility(View.GONE);
            if (ApplicationAptoide.MULTIPLESTORES && !joinStores_boolean) {
                banner.setVisibility(View.VISIBLE);
                RelativeLayout background_layout = (RelativeLayout) banner.findViewById(R.id.banner_background_layout);
                setBackgroundLayoutStoreTheme(db.getStoreTheme(store_id), background_layout);
                bannerStoreName.setText(db.getStoreName(store_id));
                String avatarURL = db.getStoreAvatar(store_id);
                com.nostra13.universalimageloader.core.ImageLoader.getInstance().displayImage(avatarURL, bannerStoreAvatar);
                bannerStoreDescription.setText(db.getStoreDescription(store_id));
                bannerStoreDescription.setMovementMethod(new ScrollingMovementMethod());
            }
        }
        availableView.findViewById(R.id.refresh_view_layout).setVisibility(View.GONE);
        refreshClick = true;
        availableAdapter.changeCursor(null);
        pb.setVisibility(View.VISIBLE);
        pb.setText(R.string.please_wait);
        if (setAdapter) {
            availableListView.setAdapter(availableAdapter);
        }
        availableLoader.forceLoad();
    }

    public void setBackgroundLayoutStoreTheme(String theme, RelativeLayout bannerLayout) {
        EnumStoreTheme aptoideBackgroundTheme;
        String storeThemeString = "APTOIDE_STORE_THEME_" + theme.toUpperCase(Locale.ENGLISH);
        try {
            aptoideBackgroundTheme = EnumStoreTheme.valueOf(storeThemeString);
        } catch (Exception e) {
            aptoideBackgroundTheme = EnumStoreTheme.APTOIDE_STORE_THEME_DEFAULT;
        }

        switch (aptoideBackgroundTheme) {
            case APTOIDE_STORE_THEME_DEFAULT:
                bannerLayout.setBackgroundResource(R.drawable.actionbar_bgd_aptoide);
                break;
            case APTOIDE_STORE_THEME_BLUE:
                bannerLayout.setBackgroundResource(R.drawable.actionbar_bgd_blue);
                break;
            case APTOIDE_STORE_THEME_DIMGRAY:
                bannerLayout.setBackgroundResource(R.drawable.actionbar_bgd_dimgray);
                break;
            case APTOIDE_STORE_THEME_GOLD:
                bannerLayout.setBackgroundResource(R.drawable.actionbar_bgd_gold);
                break;
            case APTOIDE_STORE_THEME_LIGHTSKY:
                bannerLayout.setBackgroundResource(R.drawable.actionbar_bgd_lightsky);
                break;
            case APTOIDE_STORE_THEME_MAGENTA:
                bannerLayout.setBackgroundResource(R.drawable.actionbar_bgd_magenta);
                break;
            case APTOIDE_STORE_THEME_MAROON:
                bannerLayout.setBackgroundResource(R.drawable.actionbar_bgd_maroon);
                break;
            case APTOIDE_STORE_THEME_MIDNIGHT:
                bannerLayout.setBackgroundResource(R.drawable.actionbar_bgd_midnight);
                break;
            case APTOIDE_STORE_THEME_ORANGE:
                bannerLayout.setBackgroundResource(R.drawable.actionbar_bgd_orange);
                break;
            case APTOIDE_STORE_THEME_PINK:
                bannerLayout.setBackgroundResource(R.drawable.actionbar_bgd_pink);
                break;
            case APTOIDE_STORE_THEME_RED:
                bannerLayout.setBackgroundResource(R.drawable.actionbar_bgd_red);
                break;
            case APTOIDE_STORE_THEME_SEAGREEN:
                bannerLayout.setBackgroundResource(R.drawable.actionbar_bgd_seagreen);
                break;
            case APTOIDE_STORE_THEME_SILVER:
                bannerLayout.setBackgroundResource(R.drawable.actionbar_bgd_silver);
                break;
            case APTOIDE_STORE_THEME_SLATEGRAY:
                bannerLayout.setBackgroundResource(R.drawable.actionbar_bgd_slategray);
                break;
            case APTOIDE_STORE_THEME_SPRINGGREEN:
                bannerLayout.setBackgroundResource(R.drawable.actionbar_bgd_springgreen);
                break;

            default:
                bannerLayout.setBackgroundResource(R.drawable.actionbar_bgd_aptoide);
                break;
        }
    }

    protected void addStore(String uri_str, String username, String password) {


        if (!uri_str.contains(".")) {
            uri_str = uri_str.concat(".store.aptoide.com");
        }

        uri_str = RepoUtils.formatRepoUri(uri_str);

        if (uri_str.contains("bazaarandroid.com")) {
            uri_str = uri_str.replaceAll("bazaarandroid.com", "store.aptoide.com");
        }

        if (username != null && username.contains("@")) {
            try {
                password = Algorithms.computeSHA1sum(password);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        NetworkUtils utils = new NetworkUtils();
        final int response = utils.checkServerConnection(uri_str, username,
                password);
        final String uri = uri_str;
        switch (response) {
            case 0:
                service.addStore(db, uri, username, password);
                break;
            case 401:
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        showAddStoreCredentialsDialog(uri);
                    }
                });

                break;
            case 404:
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast toast = Toast.makeText(mContext,
                                mContext.getString(R.string.verify_store),
                                Toast.LENGTH_SHORT);
                        toast.show();
                        showAddStoreDialog();
                    }
                });
                break;
            case -1:
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast toast = Toast.makeText(mContext,
                                mContext.getString(R.string.an_error_check_net),
                                Toast.LENGTH_SHORT);
                        toast.show();
                        showAddStoreDialog();
                    }
                });
                break;
            default:
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast toast = Toast.makeText(mContext,
                                mContext.getString(R.string.error_occured) + " "
                                        + response, Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL,
                                0, 30);
                        toast.show();
                        showAddStoreDialog();
                    }
                });
                break;
        }

    }

    private void showAddStoreDialog() {

        ContextThemeWrapper wrapper = new ContextThemeWrapper(this, this.obtainStyledAttributes(new int[]{R.attr.alertDialog}).getResourceId(0, 0));

        alertDialogView = LayoutInflater.from(wrapper).inflate(R.layout.dialog_add_store, null);
        alertDialog = new AlertDialog.Builder(wrapper).setView(alertDialogView).create();
        alertDialog.setTitle(getString(R.string.new_store));
        alertDialog.setButton(Dialog.BUTTON_NEGATIVE, getString(R.string.new_store), addRepoListener);
        alertDialog.setButton(Dialog.BUTTON_POSITIVE, getString(R.string.search_for_stores), searchStoresListener);

        ((EditText) alertDialogView.findViewById(R.id.edit_uri)).setText(storeUri);
        if (!isFinishing()) {
            alertDialog.show();
        }
    }

    private void showAddStoreCredentialsDialog(String string) {

        ContextThemeWrapper wrapper = new ContextThemeWrapper(this, this.obtainStyledAttributes(new int[]{R.attr.alertDialog}).getResourceId(0, 0));


        View credentialsDialogView = LayoutInflater.from(wrapper).inflate(R.layout.dialog_add_pvt_store, null);
        AlertDialog credentialsDialog = new AlertDialog.Builder(wrapper).setView(credentialsDialogView).create();
        credentialsDialog.setTitle(getString(R.string.add_private_store) + " " + RepoUtils.split(string));
        credentialsDialog.setButton(Dialog.BUTTON_NEUTRAL, getString(R.string.new_store), new AddStoreCredentialsListener(string, credentialsDialogView));
        SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        if (RepoUtils.split(string).equals(sPref.getString(Configs.LOGIN_DEFAULT_REPO, ""))) {
            ((EditText) credentialsDialogView.findViewById(R.id.username)).setText(sPref.getString(Configs.LOGIN_USER_LOGIN, ""));
        }


        if (!isFinishing()) {
            credentialsDialog.show();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            View v = availableListView.getChildAt(0);
            scrollMemory.put(depth, new ListViewPosition((v == null) ? 0 : v.getTop(), availableListView.getFirstVisiblePosition()));

            if (!ApplicationAptoide.MULTIPLESTORES) {
                if (!depth.equals(ListDepth.CATEGORY1) && pager.getCurrentItem() == 1) {
                    if (depth.equals(ListDepth.TOPAPPS)
                            || depth.equals(ListDepth.LATEST_LIKES)
                            || depth.equals(ListDepth.LATESTAPPS)
                            || depth.equals(ListDepth.LATEST_COMMENTS)
                            || depth.equals(ListDepth.RECOMMENDED)
                            || depth.equals(ListDepth.ALLAPPLICATIONS)) {
                        depth = ListDepth.CATEGORY1;
                    } else {
                        depth = ListDepth.values()[depth.ordinal() - 1];
                    }
                    removeLastBreadCrumb();
                    refreshAvailableList(true);
                    return false;
                }
            } else {
                if (!depth.equals(ListDepth.STORES) && pager.getCurrentItem() == 1) {
                    if (depth.equals(ListDepth.TOPAPPS)
                            || depth.equals(ListDepth.LATEST_LIKES)
                            || depth.equals(ListDepth.LATESTAPPS)
                            || depth.equals(ListDepth.LATEST_COMMENTS)
                            || depth.equals(ListDepth.RECOMMENDED)
                            || depth.equals(ListDepth.ALLAPPLICATIONS)) {
                        depth = ListDepth.CATEGORY1;
                    } else {
                        depth = ListDepth.values()[depth.ordinal() - 1];
                    }
                    removeLastBreadCrumb();
                    refreshAvailableList(true);
                    return false;
                }

            }
        }


        return super.onKeyDown(keyCode, event);
    }

    private void removeLastBreadCrumb() {
        breadcrumbs.removeViewAt(breadcrumbs.getChildCount() - 1);
    }

    protected void onNewIntent(Intent intent) {
        if (intent.hasExtra("new_updates")) {
            pager.setCurrentItem(3);
        }
    }

    private void getInstalled() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                List<PackageInfo> system_installed_list = getPackageManager().getInstalledPackages(0);
                List<String> database_installed_list = db.getStartupInstalled();
                for (PackageInfo pkg : system_installed_list) {
                    if (!database_installed_list.contains(pkg.packageName)) {
                        try {
                            ViewApk apk = new ViewApk();
                            apk.setApkid(pkg.packageName);
                            apk.setVercode(pkg.versionCode);
                            apk.setVername(pkg.versionName);
                            apk.setName((String) pkg.applicationInfo.loadLabel(getPackageManager()));
                            db.insertInstalled(apk);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        installedLoader = getSupportLoaderManager().initLoader(INSTALLED_LOADER, null, MainActivity.this);
                        installedView.setAdapter(installedAdapter);
                        getUpdates();
                    }
                });
            }
        }).start();
    }

    private void getUpdates() {
        updatesLoader = getSupportLoaderManager().initLoader(UPDATES_LOADER, null, MainActivity.this);
        updatesListView.setAdapter(updatesAdapter);
        ((UpdatesAdapter) updatesAdapter).setLoader(updatesLoader);
    }

    private void loadUi() {
        setContentView(R.layout.activity_aptoide);
        TitlePageIndicator indicator = (TitlePageIndicator) findViewById(R.id.indicator);
        pager = (ViewPager) findViewById(R.id.viewpager);

        if (!ApplicationAptoide.MULTIPLESTORES) {
            depth = ListDepth.CATEGORY1;
            store_id = 1;
        }

        updateView.findViewById(R.id.update_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                updateAll();
            }
        });

        availableListView.setFastScrollEnabled(true);
        availableListView.addHeaderView(breadcrumbs, null, false);

        registerForContextMenu(updatesListView);
        updatesListView.setLongClickable(true);

        availableView.findViewById(R.id.refresh_view_layout).findViewById(R.id.refresh_view).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshClick = true;
                availableView.findViewById(R.id.refresh_view_layout)
                        .setVisibility(View.GONE);
                refreshAvailableList(false);

            }
        });

        joinStores.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                joinStores_boolean = isChecked;
                // if (isChecked) {
                // addBreadCrumb("All Stores", depth);
                // } else {
                // breadcrumbs.removeAllViews();
                // }
                refreshAvailableList(true);
            }
        });


        bindService(new Intent(this, ServiceManagerDownload.class), serviceManagerConnection, BIND_AUTO_CREATE);

        pb.setText(R.string.add_store_button_below);

        addStoreButton.setOnClickListener(addStoreListener);

        if (!ApplicationAptoide.MULTIPLESTORES) {
            addStoreButton.setVisibility(View.GONE);
        }


        availableListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i;
                View v = availableListView.getChildAt(0);
                scrollMemory.put(depth, new ListViewPosition((v == null) ? 0 : v.getTop(), availableListView.getFirstVisiblePosition()));
                switch (depth) {
                    case STORES:
                        depth = ListDepth.CATEGORY1;
                        store_id = id;
                        break;
                    case CATEGORY1:
                        String category = ((Cursor) parent.getItemAtPosition(position)).getString(1);
                        if (category.equals("Top Apps")) {
                            depth = ListDepth.TOPAPPS;
                        } else if (category.equals("Latest Apps")) {
                            depth = ListDepth.LATESTAPPS;
                        } else if (id == LATEST_LIKES) {
                            depth = ListDepth.LATEST_LIKES;
                        } else if (id == LATEST_COMMENTS) {
                            depth = ListDepth.LATEST_COMMENTS;
                        } else if (id == -3) {
                            if (!Login.isLoggedIn(mContext)) {
                                Toast toast = Toast.makeText(mContext, mContext.getString(R.string.you_need_to_login_toast), Toast.LENGTH_SHORT);
                                toast.show();
                                return;
                            } else {
                                depth = ListDepth.RECOMMENDED;
                            }
                        } else if (id == -4) {
                            depth = ListDepth.ALLAPPLICATIONS;
                        } else if (id == -10) {
                            Toast toast = Toast.makeText(mContext, mContext.getString(R.string.store_beginning_to_load), Toast.LENGTH_SHORT);
                            toast.show();
                            return;
                        } else {
                            depth = ListDepth.CATEGORY2;
                        }
                        category_id = id;
                        break;
                    case CATEGORY2:
                        depth = ListDepth.APPLICATIONS;
                        category2_id = id;
                        break;
                    case TOPAPPS:
                        i = new Intent(MainActivity.this, ApkInfo.class);
                        i.putExtra("_id", id);
                        i.putExtra("top", true);
                        i.putExtra("category", Category.TOP.ordinal());
                        startActivity(i);
                        return;
                    case LATESTAPPS:
                        i = new Intent(MainActivity.this, ApkInfo.class);
                        i.putExtra("_id", id);
                        i.putExtra("top", true);
                        i.putExtra("category", Category.LATEST.ordinal());
                        startActivity(i);
                        return;
                    case APPLICATIONS:
                    case ALLAPPLICATIONS:
                    case RECOMMENDED:
                        i = new Intent(MainActivity.this, ApkInfo.class);
                        i.putExtra("_id", id);
                        i.putExtra("top", false);
                        i.putExtra("category", Category.INFOXML.ordinal());
                        startActivity(i);
                        return;
                    case LATEST_COMMENTS:
                    case LATEST_LIKES:
                        String apkid = ((Cursor) parent.getItemAtPosition(position)).getString(1);
                        latestClick(apkid);
                        return;
                    default:
                        return;
                }
                String breadCrumbBare = ((Cursor) parent.getItemAtPosition(position)).getString(1);
                Integer breadcrumbRes = EnumCategories.categories.get(breadCrumbBare);
                String localizedBreadcrumb;
                if (breadcrumbRes != null) {
                    localizedBreadcrumb = getString(breadcrumbRes);
                } else {
                    localizedBreadcrumb = breadCrumbBare;
                }


                addBreadCrumb(localizedBreadcrumb, depth);
                refreshAvailableList(true);
            }
        });
        installedView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long id) {
                Intent i = new Intent(MainActivity.this, ApkInfo.class);
                i.putExtra("_id", id);
                i.putExtra("installed", true);
                i.putExtra("category", Category.INFOXML.ordinal());
                startActivity(i);
            }
        });


        ImageView brandIv = (ImageView) findViewById(R.id.brand);


        if (ApplicationAptoide.APTOIDETHEME.equalsIgnoreCase("jblow")) {
            brandIv.setImageResource(R.drawable.brand_jblow);
        } else if (ApplicationAptoide.APTOIDETHEME.equalsIgnoreCase("magalhaes")) {
            brandIv.setImageResource(R.drawable.brand_magalhaes);
        } else {
            brandIv.setImageResource(R.drawable.brand_aptoide);
        }


        findViewById(R.id.btsearch).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onSearchRequested();

            }
        });

        updatesListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long id) {
                Intent i = new Intent(MainActivity.this, ApkInfo.class);
                i.putExtra("_id", id);
                i.putExtra("updates", true);
                i.putExtra("category", Category.INFOXML.ordinal());
                startActivity(i);
            }
        });
        // LoaderManager.enableDebugLogging(true);
        availableLoader = getSupportLoaderManager().initLoader(AVAILABLE_LOADER, null, this);

        ArrayList<View> views = new ArrayList<View>();
        views.add(featuredView);
        views.add(availableView);
        views.add(installedView);
        views.add(updateView);

        pager.setAdapter(new ViewPagerAdapter(mContext, views));
        indicator.setViewPager(pager);
        refreshAvailableList(true);

        if (!ApplicationAptoide.MULTIPLESTORES) {
            addBreadCrumb(getString(R.string.store), ListDepth.CATEGORY1);
        } else {
            addBreadCrumb(getString(R.string.stores), ListDepth.STORES);
        }

        if (!ApplicationAptoide.MATURECONTENTSWITCH) {
            featuredView.findViewById(R.id.toggleButton1).setVisibility(View.GONE);
            featuredView.findViewById(R.id.adultcontent_label).setVisibility(View.GONE);
        }

    }

    void updateAll() {
        Toast toast = Toast.makeText(this, getString(R.string.updating), Toast.LENGTH_SHORT);
        toast.show();
        new Thread(new Runnable() {

            @Override
            public void run() {
                final ArrayList<Long> longs = new ArrayList<Long>();
                final Cursor c = db.getUpdates(order);
                for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                    longs.add(c.getLong(0));
                }
                c.close();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (Long aLong : longs) {
                            new GetApkWebserviceInfo(mContext, serviceDownloadManager, false).execute(aLong);
                        }
                    }
                });
            }
        }).start();
    }

    protected void latestClick(final String apkid) {

        new Thread(new Runnable() {

            @Override
            public void run() {
                final long id = db.getApkId(apkid, store_id);
                System.out.println("Getting Latest id" + id);
                if (id != -1) {
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            Intent i = new Intent(MainActivity.this, ApkInfo.class);
                            i.putExtra("_id", id);
                            i.putExtra("top", false);
                            i.putExtra("category", Category.INFOXML.ordinal());
                            startActivity(i);
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            Toast toast = Toast.makeText(mContext, mContext.getString(R.string.error_latest_apk), Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    });
                }

            }
        }).start();

    }

    protected void addBreadCrumb(String itemAtPosition, ListDepth depth2) {
        if (itemAtPosition.contains("http://")) {
            itemAtPosition = itemAtPosition.split("http://")[1];
            itemAtPosition = itemAtPosition.split(".store")[0];
        }
        Button bt = (Button) LayoutInflater.from(mContext).inflate(R.layout.breadcrumb, null);
        bt.setText(itemAtPosition);
        bt.setTag(new BreadCrumb(depth, breadcrumbs.getChildCount() + 1));
        bt.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                depth = ((BreadCrumb) v.getTag()).depth;
                breadcrumbs.removeViews(((BreadCrumb) v.getTag()).i, breadcrumbs.getChildCount() - ((BreadCrumb) v.getTag()).i);
                refreshAvailableList(true);
            }
        });
        breadcrumbs.addView(bt, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        breadcrumbs.setOnClickListener(null);
    }

    private int getBrandDrawableResource() {
        int brandDrawableResource;

        brandDrawableResource = this.getResources().getIdentifier(ApplicationAptoide.BRAND, "drawable", this.getPackageName());
        if (brandDrawableResource == 0) {
            brandDrawableResource = this.getResources().getIdentifier("brand_aptoide", "drawable", this.getPackageName());
            Log.d("MainActivity-brand", ApplicationAptoide.BRAND + ": resource not found, using default");
        }

        EnumAptoideThemes enumAptoideTheme;
        String aptoideThemeString = "APTOIDE_THEME_" + ApplicationAptoide.APTOIDETHEME.toUpperCase(Locale.ENGLISH);
        try {
            enumAptoideTheme = EnumAptoideThemes.valueOf(aptoideThemeString);
        } catch (Exception e) {
            enumAptoideTheme = EnumAptoideThemes.APTOIDE_THEME_DEFAULT;
        }
        switch (enumAptoideTheme) {
            case APTOIDE_THEME_DIGITALLYDIFFERENT:
                brandDrawableResource = this.getResources().getIdentifier("brand_digitallydifferent", "drawable", this.getPackageName());
                Log.d("MainActivity-brand", ApplicationAptoide.BRAND);
                break;
            case APTOIDE_THEME_EOCEAN:
                brandDrawableResource = this.getResources().getIdentifier("brand_eocean", "drawable", this.getPackageName());
                Log.d("MainActivity-brand", ApplicationAptoide.BRAND);
                break;
            case APTOIDE_THEME_JBLOW:
                brandDrawableResource = this.getResources().getIdentifier("brand_jblow", "drawable", this.getPackageName());
                Log.d("MainActivity-brand", ApplicationAptoide.BRAND);
                break;
            case APTOIDE_THEME_LAZERPLAY:
                brandDrawableResource = this.getResources().getIdentifier("brand_lazerplay", "drawable", this.getPackageName());
                Log.d("MainActivity-brand", ApplicationAptoide.BRAND);
                break;
            case APTOIDE_THEME_MAGALHAES:
                brandDrawableResource = this.getResources().getIdentifier("brand_magalhaes", "drawable", this.getPackageName());
                Log.d("MainActivity-brand", ApplicationAptoide.BRAND);
                break;
            case APTOIDE_THEME_TIMWE:
                brandDrawableResource = this.getResources().getIdentifier("brand_timwe", "drawable", this.getPackageName());
                Log.d("MainActivity-brand", ApplicationAptoide.BRAND);
                break;

        }
        return brandDrawableResource;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {

        switch (v.getId()) {
            case R.id.available_list:
                Integer tag = (Integer) ((AdapterContextMenuInfo) menuInfo).targetView.getTag();
                if (tag != null && tag == 1) {
                    menu.add(0, 1, 0, R.string.menu_context_reparse);
                }
                menu.add(0, 0, 0, R.string.menu_context_remove);
                break;
            case R.id.updates_list:
                Log.d("onCreateContextMenu", "onCreateContextMenu");
                menu.add(0, (int) ((AdapterContextMenuInfo) menuInfo).id, 0, mContext.getString(R.string.exclude_update)).setOnMenuItemClickListener(
                        new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                System.out.println(item.getItemId());
                                db.addToExcludeUpdate(item.getItemId());
                                updatesLoader.forceLoad();
                                Toast toast = Toast.makeText(mContext,
                                        mContext.getString(R.string.add_to_excluded_updates_list),
                                        Toast.LENGTH_SHORT);
                                toast.show();
                                return false;
                            }
                        });
                break;
        }

    }

    @Override
    public boolean onContextItemSelected(final MenuItem item) {
        final ProgressDialog pd;
        switch (item.getItemId()) {
            case 0:
                pd = new ProgressDialog(mContext);
                pd.setMessage(getString(R.string.please_wait));
                pd.show();
                pd.setCancelable(false);
                new Thread(new Runnable() {

                    private boolean result = false;

                    @Override
                    public void run() {
                        try {
                            result = service.deleteStore(db, ((AdapterContextMenuInfo) item.getMenuInfo()).id);
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    pd.dismiss();
                                    if (result) {
                                        refreshAvailableList(false);
                                        installedLoader.forceLoad();
                                        updatesLoader.forceLoad();
                                    } else {
                                        Toast toast = Toast.makeText(mContext, mContext.getString(R.string.error_delete_store), Toast.LENGTH_SHORT);
                                        toast.show();
                                    }

                                }
                            });
                        }
                    }
                }).start();
                break;
            case 1:
                pd = new ProgressDialog(mContext);
                pd.setMessage(getString(R.string.please_wait));
                pd.show();
                pd.setCancelable(false);
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            service.parseServer(db, db.getServer(((AdapterContextMenuInfo) item.getMenuInfo()).id, false));
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {

                                    pd.dismiss();
                                    refreshAvailableList(false);
                                }
                            });

                        }
                    }
                }).start();

                break;
        }

        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onSearchRequested() {

        if (Build.VERSION.SDK_INT > 7) {
            WebSocketSingleton.getInstance().connect();
            isDisconnect = false;
            android.app.SearchManager manager = (android.app.SearchManager) getSystemService(Context.SEARCH_SERVICE);
            manager.setOnCancelListener(new android.app.SearchManager.OnCancelListener() {
                @Override
                public void onCancel() {

                    isDisconnect = true;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            if (isDisconnect) {
                                WebSocketSingleton.getInstance().disconnect();
                            }

                        }
                    }, 5000);


                }
            });

            manager.setOnDismissListener(new android.app.SearchManager.OnDismissListener() {
                @Override
                public void onDismiss() {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            if (isDisconnect) {
                                WebSocketSingleton.getInstance().disconnect();
                            }

                        }
                    }, 5000);
                }
            });
        }
        return super.onSearchRequested();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        SimpleCursorLoader a;
        switch (id) {
            case AVAILABLE_LOADER:
                a = new SimpleCursorLoader(mContext) {

                    @Override
                    public Cursor loadInBackground() {
                        switch (depth) {
                            case STORES:
                                return db.getStores(joinStores_boolean);
                            case CATEGORY1:
                                return db.getCategory1(store_id, joinStores_boolean, !PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean("orderByCategory", true));
                            case CATEGORY2:
                                return db.getCategory2(category_id, store_id, joinStores_boolean);
                            case ALLAPPLICATIONS:
                            case APPLICATIONS:
                                return db.getApps(category2_id, store_id, joinStores_boolean, order, !PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean("orderByCategory", true));
                            case TOPAPPS:
                                return db.getTopApps(store_id, joinStores_boolean);
                            case LATESTAPPS:
                                return db.getLatestApps(store_id, joinStores_boolean);
                            case LATEST_LIKES:
                                return new LatestLikesComments(store_id, db, mContext).getLikes();
                            case LATEST_COMMENTS:
                                return new LatestLikesComments(store_id, db, mContext).getComments();
                            case RECOMMENDED:
                                final Cursor c = db.getUserBasedApk(store_id, joinStores_boolean);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (c.getCount() == 0) {
                                            Toast toast = Toast.makeText(mContext, mContext.getString(R.string.no_recommended_toast), Toast.LENGTH_SHORT);
                                            toast.show();
                                        }
                                    }
                                });
                                return c;

                            default:
                                return null;
                        }
                    }
                };
                return a;
            case INSTALLED_LOADER:
                a = new SimpleCursorLoader(mContext) {

                    @Override
                    public Cursor loadInBackground() {
                        return db.getInstalledApps(order);
                    }
                };
                return a;
            case UPDATES_LOADER:
                a = new SimpleCursorLoader(mContext) {

                    @Override
                    public Cursor loadInBackground() {
                        return db.getUpdates(order);
                    }
                };

                return a;
            default:
                break;
        }
        return null;

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case AVAILABLE_LOADER:
                availableAdapter.swapCursor(data);
                if (scrollMemory.get(depth) != null) {
                    ListViewPosition lvp = scrollMemory.get(depth);
                    availableListView.setSelectionFromTop(lvp.index, lvp.top);
                }
                break;
            case INSTALLED_LOADER:
                installedAdapter.swapCursor(data);
                break;
            case UPDATES_LOADER:
                updatesAdapter.swapCursor(data);
                if (data.getCount() == 1) {
                    updateView.findViewById(R.id.all_apps_up_to_date).setVisibility(View.GONE);
                    updateView.findViewById(R.id.update_all_view_layout).setVisibility(View.GONE);
                } else if (data.getCount() > 1) {
                    updateView.findViewById(R.id.update_all_view_layout).setVisibility(View.VISIBLE);
                    // updateView.findViewById(R.id.update_all_view_layout).startAnimation(AnimationUtils.loadAnimation(mContext,
                    // android.R.anim.fade_in));
                    updateView.findViewById(R.id.all_apps_up_to_date).setVisibility(View.GONE);
                } else {
                    updateView.findViewById(R.id.update_all_view_layout).setVisibility(View.GONE);
                    updateView.findViewById(R.id.all_apps_up_to_date).setVisibility(View.VISIBLE);
                    ((TextView) updateView.findViewById(R.id.all_apps_up_to_date)).setText(R.string.all_updated);
                }
                break;
            default:
                break;
        }
        pb.setVisibility(View.GONE);

        if (availableListView.getAdapter() != null) {
            if (availableListView.getAdapter().getCount() > 2 || joinStores_boolean) {
                joinStores.setVisibility(View.VISIBLE);
            } else {
                joinStores.setVisibility(View.INVISIBLE);
            }

            if (availableListView.getAdapter().getCount() > 1) {
                pb.setVisibility(View.GONE);
            } else if (depth == ListDepth.STORES) {
                pb.setVisibility(View.VISIBLE);
                pb.setText(R.string.add_store_button_below);
            }
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        availableAdapter.swapCursor(null);
    }

    @Override
    protected void onDestroy() {
        if (service != null) {
            unbindService(conn);
        }
        if (serviceDownloadManager != null) {
            unbindService(serviceManagerConnection);
        }
        if (registered) {
            unregisterReceiver(updatesReceiver);
            unregisterReceiver(statusReceiver);
            unregisterReceiver(redrawInstalledReceiver);
            unregisterReceiver(loginReceiver);
            unregisterReceiver(newRepoReceiver);
            unregisterReceiver(storePasswordReceiver);
            unregisterReceiver(openUpdatesReceiver);
            if (!ApplicationAptoide.MULTIPLESTORES) {
                unregisterReceiver(parseFailedReceiver);
            }
        }

        // stopService(serviceDownloadManagerIntent);
        generateXML();

        super.onDestroy();
    }

    @Override
    public boolean onPrepareOptionsMenu(com.actionbarsherlock.view.Menu menu) {
        menu.clear();
        // menu.add(Menu.NONE, EnumOptionsMenu.SEARCH.ordinal(),
        // EnumOptionsMenu.SEARCH.ordinal(), "Search")
        // .setIcon(R.drawable.ic_search)
        // .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS |
        // MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        menu.add(Menu.NONE, EnumOptionsMenu.LOGIN.ordinal(),
                EnumOptionsMenu.LOGIN.ordinal(), R.string.my_account).setIcon(
                android.R.drawable.ic_menu_edit);
        menu.add(Menu.NONE, EnumOptionsMenu.DISPLAY_OPTIONS.ordinal(),
                EnumOptionsMenu.DISPLAY_OPTIONS.ordinal(),
                R.string.menu_display_options).setIcon(
                android.R.drawable.ic_menu_sort_by_size);
        menu.add(Menu.NONE, EnumOptionsMenu.SCHEDULED_DOWNLOADS.ordinal(),
                EnumOptionsMenu.SCHEDULED_DOWNLOADS.ordinal(),
                R.string.setting_schdwntitle).setIcon(
                android.R.drawable.ic_menu_agenda);
        menu.add(Menu.NONE, EnumOptionsMenu.SETTINGS.ordinal(),
                EnumOptionsMenu.SETTINGS.ordinal(), R.string.settings_title_bar)
                .setIcon(android.R.drawable.ic_menu_manage);

        if (ApplicationAptoide.PARTNERID == null) {
            // menu.add(Menu.NONE, EnumOptionsMenu.ABOUT.ordinal(),
            // EnumOptionsMenu.ABOUT.ordinal(), R.string.about).setIcon(
            // android.R.drawable.ic_menu_help);
            menu.add(Menu.NONE, EnumOptionsMenu.FOLLOW.ordinal(),
                    EnumOptionsMenu.FOLLOW.ordinal(), R.string.social_networks)
                    .setIcon(android.R.drawable.ic_menu_share);
        }

        menu.add(Menu.NONE, EnumOptionsMenu.DOWNLOADMANAGER.ordinal(),
                EnumOptionsMenu.DOWNLOADMANAGER.ordinal(),
                R.string.download_manager).setIcon(
                android.R.drawable.ic_menu_save);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
        EnumOptionsMenu menuEntry = EnumOptionsMenu.reverseOrdinal(item.getItemId());
        Log.d("MainActivity-OptionsMenu", "menuOption: " + menuEntry + " itemid: " + item.getItemId());
        switch (menuEntry) {
            // case SEARCH:
            // onSearchRequested();
            // break;
            case LOGIN:
                Intent loginIntent = new Intent(this, Login.class);
                startActivity(loginIntent);
                break;
            case DISPLAY_OPTIONS:
                displayOptionsDialog();
                break;
            case SCHEDULED_DOWNLOADS:
                Intent scheduledIntent = new Intent(this, ScheduledDownloads.class);
                startActivity(scheduledIntent);
                break;
            case SETTINGS:
                Intent settingsIntent = new Intent(this, Settings.class);
                startActivityForResult(settingsIntent, 0);
                break;
            // case ABOUT:
            // showAbout();
            // break;
            case DOWNLOADMANAGER:
                startActivity(new Intent(this, DownloadManager.class));
                break;
            case FOLLOW:
                showFollow();
                break;
            default:
                break;
        }

        return true;
    }

    private void displayOptionsDialog() {

        final SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(this);
        final Editor editor = sPref.edit();
        ContextThemeWrapper wrapper = new ContextThemeWrapper(this, this.obtainStyledAttributes(new int[]{R.attr.alertDialog}).getResourceId(0, 0));
        View view = LayoutInflater.from(wrapper).inflate(R.layout.dialog_order_popup, null);
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(wrapper).setView(view);
        final AlertDialog orderDialog = dialogBuilder.create();
        orderDialog.setIcon(android.R.drawable.ic_menu_sort_by_size);
        orderDialog.setTitle(getString(R.string.menu_display_options));
        orderDialog.setCancelable(true);

        final RadioButton ord_rct = (RadioButton) view.findViewById(R.id.org_rct);
        final RadioButton ord_abc = (RadioButton) view.findViewById(R.id.org_abc);
        final RadioButton ord_rat = (RadioButton) view.findViewById(R.id.org_rat);
        final RadioButton ord_dwn = (RadioButton) view.findViewById(R.id.org_dwn);
        final RadioButton ord_price = (RadioButton) view.findViewById(R.id.org_price);
        final RadioButton btn1 = (RadioButton) view.findViewById(R.id.shw_ct);
        final RadioButton btn2 = (RadioButton) view.findViewById(R.id.shw_all);

        final ToggleButton adult = (ToggleButton) view.findViewById(R.id.adultcontent_toggle);

        orderDialog.setButton(Dialog.BUTTON_NEUTRAL, "Ok", new Dialog.OnClickListener() {
            boolean pop_change = false;
            private boolean pop_change_category = false;

            public void onClick(DialogInterface dialog, int which) {
                if (ord_rct.isChecked()) {
                    pop_change = true;
                    order = Order.DATE;
                } else if (ord_abc.isChecked()) {
                    pop_change = true;
                    order = Order.NAME;
                } else if (ord_rat.isChecked()) {
                    pop_change = true;
                    order = Order.RATING;
                } else if (ord_dwn.isChecked()) {
                    pop_change = true;
                    order = Order.DOWNLOADS;
                } else if (ord_price.isChecked()) {
                    pop_change = true;
                    order = Order.PRICE;
                }

                if (btn1.isChecked()) {
                    pop_change = true;
                    pop_change_category = true;
                    editor.putBoolean("orderByCategory", true);
                } else if (btn2.isChecked()) {
                    pop_change = true;
                    pop_change_category = true;
                    editor.putBoolean("orderByCategory", false);
                }
                if (adult.isChecked()) {
                    pop_change = true;
                    editor.putBoolean("matureChkBox", false);
                } else {
                    editor.putBoolean("matureChkBox", true);
                }
                if (pop_change) {
                    editor.putInt("order_list", order.ordinal());
                    editor.commit();
                    if (pop_change_category) {

                        if (!depth.equals(ListDepth.CATEGORY1)
                                && !depth.equals(ListDepth.STORES)) {
                            if (depth.equals(ListDepth.APPLICATIONS)) {
                                removeLastBreadCrumb();
                            }
                            removeLastBreadCrumb();
                            depth = ListDepth.CATEGORY1;
                        }

                    }
                    redrawAll();
                    refreshAvailableList(true);
                }
            }
        });

        if (sPref.getBoolean("orderByCategory", false)) {
            btn1.setChecked(true);
        } else {
            btn2.setChecked(true);
        }
        if (!ApplicationAptoide.MATURECONTENTSWITCH) {
            adult.setVisibility(View.GONE);
            view.findViewById(R.id.dialog_adult_content_label).setVisibility(View.GONE);
        }
        adult.setChecked(!sPref.getBoolean("matureChkBox", false));
        // adult.setOnCheckedChangeListener(adultCheckedListener);
        switch (order) {
            case DATE:
                ord_rct.setChecked(true);
                break;
            case DOWNLOADS:
                ord_dwn.setChecked(true);
                break;
            case NAME:
                ord_abc.setChecked(true);
                break;
            case RATING:
                ord_rat.setChecked(true);
                break;
            case PRICE:
                ord_price.setChecked(true);
                break;

            default:
                break;
        }

        orderDialog.show();

    }

    protected void redrawAll() {
        if (installedLoader != null)
            installedLoader.forceLoad();
        if (availableLoader != null)
            availableLoader.forceLoad();
        if (updatesLoader != null)
            updatesLoader.forceLoad();
        new Thread(new Runnable() {

            @Override
            public void run() {
                loadUItopapps();
                if (Login.isLoggedIn(mContext)) {
                    loadRecommended();
                }

            }
        }).start();
    }

    private void loadRecommended() {


        if (Login.isLoggedIn(mContext)) {
            featuredView.findViewById(R.id.recommended_text).setVisibility(View.GONE);
        } else {
            featuredView.findViewById(R.id.recommended_text).setVisibility(View.VISIBLE);
        }

        new Thread(new Runnable() {

            private ArrayList<HashMap<String, String>> valuesRecommended;

            public void run() {
                loadUIRecommendedApps();
                File f = null;
                try {
                    ArrayList<WebserviceOptions> options = new ArrayList<WebserviceOptions>();
                    options.add(new WebserviceOptions("limit", "10"));

                    options.add(new WebserviceOptions("q", Utils.filters(mContext)));
                    options.add(new WebserviceOptions("lang", Utils.getMyCountryCode(ApplicationAptoide.getContext())));


                    StringBuilder sb = new StringBuilder();
                    sb.append("(");
                    for (WebserviceOptions option : options) {
                        sb.append(option);
                        sb.append(";");
                    }
                    sb.append(")");
                    SAXParserFactory spf = SAXParserFactory.newInstance();
                    SAXParser sp = spf.newSAXParser();
                    NetworkUtils utils = new NetworkUtils();
                    BufferedInputStream bis = new BufferedInputStream(utils
                            .getInputStream(AptoideConfiguration.getInstance().getWebServicesUri() +
                                    "webservices/listUserBasedApks/"
                                    + Login.getToken(mContext)
                                    + "/options=" + sb.toString() + "/xml", null, null, mContext), 8 * 1024);
                    f = File.createTempFile("abc", "abc");
                    OutputStream out = new FileOutputStream(f);
                    byte buf[] = new byte[1024];
                    int len;
                    while ((len = bis.read(buf)) > 0)
                        out.write(buf, 0, len);
                    out.close();
                    bis.close();
                    String hash = Md5Handler.md5Calc(f);
                    ViewApk parent_apk = new ViewApk();
                    parent_apk.setApkid("recommended");
                    if (!hash.equals(db.getItemBasedApksHash(parent_apk
                            .getApkid()))) {
                        // Database.database.beginTransaction();
                        db.deleteItemBasedApks(parent_apk);
                        sp.parse(f, new HandlerItemBased(parent_apk));
                        db.insertItemBasedApkHash(hash, parent_apk.getApkid());
                        // Database.database.setTransactionSuccessful();
                        // Database.database.endTransaction();
                        loadUIRecommendedApps();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (f != null) f.delete();

            }

            private void loadUIRecommendedApps() {


                valuesRecommended = db.getItemBasedApksRecommended("recommended");

                runOnUiThread(new Runnable() {

                    public void run() {

                        LinearLayout ll = (LinearLayout) featuredView.findViewById(R.id.recommended_container);
                        ll.removeAllViews();
                        LinearLayout llAlso = new LinearLayout(MainActivity.this);
                        llAlso.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT));
                        llAlso.setOrientation(LinearLayout.HORIZONTAL);
                        if (valuesRecommended.isEmpty()) {
                            if (Login.isLoggedIn(mContext)) {
                                TextView tv = new TextView(mContext);
                                tv.setText(R.string.no_recommended_apps);
                                tv.setTextAppearance(mContext, android.R.attr.textAppearanceMedium);
                                tv.setPadding(10, 10, 10, 10);
                                ll.addView(tv);
                            }
                        } else {

                            for (int i = 0; i != valuesRecommended.size(); i++) {
                                LinearLayout txtSamItem = (LinearLayout) getLayoutInflater().inflate(R.layout.row_grid_item, null);
                                ((TextView) txtSamItem.findViewById(R.id.name)).setText(valuesRecommended.get(i).get("name"));
                                ImageLoader.getInstance().displayImage(
                                        valuesRecommended.get(i).get("icon"),
                                        (ImageView) txtSamItem.findViewById(R.id.icon));
                                float stars;
                                try {
                                    stars = Float.parseFloat(valuesRecommended.get(i).get("rating"));
                                } catch (Exception e) {
                                    stars = 0f;
                                }
                                ((RatingBar) txtSamItem.findViewById(R.id.rating)).setIsIndicator(true);
                                ((RatingBar) txtSamItem.findViewById(R.id.rating)).setRating(stars);
                                txtSamItem.setPadding(10, 0, 0, 0);
                                // ((TextView)
                                // txtSamItem.findViewById(R.id.version))
                                // .setText(getString(R.string.version) +" "+
                                // valuesRecommended.get(i).get("vername"));
                                ((TextView) txtSamItem.findViewById(R.id.downloads)).setText("(" + valuesRecommended.get(i).get("downloads") + " " + getString(R.string.downloads) + ")");
                                txtSamItem.setTag(valuesRecommended.get(i).get("_id"));
                                txtSamItem.setLayoutParams(new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        getPixels(80), 1));
                                // txtSamItem.setOnClickListener(featuredListener);
                                txtSamItem.setOnClickListener(new OnClickListener() {

                                    @Override
                                    public void onClick(View arg0) {
                                        Intent i = new Intent(MainActivity.this, ApkInfo.class);
                                        long id = Long.parseLong((String) arg0.getTag());
                                        i.putExtra("_id", id);
                                        i.putExtra("top", true);
                                        i.putExtra("category", Category.ITEMBASED.ordinal());
                                        startActivity(i);
                                    }
                                });

                                txtSamItem.measure(0, 0);

                                if (i % 2 == 0) {
                                    ll.addView(llAlso);

                                    llAlso = new LinearLayout(MainActivity.this);
                                    llAlso.setLayoutParams(new LinearLayout.LayoutParams(
                                            LinearLayout.LayoutParams.MATCH_PARENT,
                                            getPixels(80)));
                                    llAlso.setOrientation(LinearLayout.HORIZONTAL);
                                    llAlso.addView(txtSamItem);
                                } else {
                                    llAlso.addView(txtSamItem);
                                }
                            }

                            ll.addView(llAlso);
                        }
                    }
                });
            }
        }).start();

    }

    public void showFollow() {
        ContextThemeWrapper wrapper = new ContextThemeWrapper(this, this.obtainStyledAttributes(new int[]{R.attr.alertDialog}).getResourceId(0, 0));
        View socialNetworksView = LayoutInflater.from(wrapper).inflate(R.layout.dialog_social_networks, null);
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this).setView(socialNetworksView);
        final AlertDialog socialDialog = dialogBuilder.create();
        socialDialog.setIcon(android.R.drawable.ic_menu_share);
        socialDialog.setTitle(getString(R.string.social_networks));
        socialDialog.setCancelable(true);

        Button facebookButton = (Button) socialNetworksView.findViewById(R.id.find_facebook);
        facebookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAppInstalled("com.facebook.katana")) {
                    Intent sharingIntent;
                    try {
                        getPackageManager().getPackageInfo("com.facebook.katana", 0);
                        sharingIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("fb://profile/225295240870860"));
                        startActivity(sharingIntent);
                    } catch (NameNotFoundException e) {
                        e.printStackTrace();
                    }
                } else {
                    Intent intent = new Intent(mContext, WebViewFacebook.class);
                    startActivity(intent);
                }

            }
        });

        Button twitterButton = (Button) socialNetworksView.findViewById(R.id.follow_twitter);
        twitterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAppInstalled("com.twitter.android")) {
                    String url = "http://www.twitter.com/aptoide";
                    Intent twitterIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(twitterIntent);
                } else {
                    Intent intent = new Intent(mContext, WebViewTwitter.class);
                    startActivity(intent);
                }
            }
        });
        socialDialog.show();
    }

    private boolean isAppInstalled(String uri) {
        PackageManager pm = getPackageManager();
        boolean installed;
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            installed = false;
        }
        return installed;
    }

    protected void generateXML() {
        System.out.println("Generating servers.xml");
        File newxmlfile = new File(LOCAL_PATH + "servers.xml");
        try {
            newxmlfile.createNewFile();
        } catch (IOException e) {
            Log.e("IOException", "exception in createNewFile() method");
        }
        FileOutputStream fileos = null;
        try {
            fileos = new FileOutputStream(newxmlfile);
        } catch (FileNotFoundException e) {
            Log.e("FileNotFoundException", "can't create FileOutputStream");
        }
        XmlSerializer serializer = Xml.newSerializer();
        try {
            serializer.setOutput(fileos, "UTF-8");
            serializer.startDocument(null, true);
            serializer.startTag(null, "myapp");
            Cursor c = db.getStores(false);
            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                serializer.startTag(null, "newserver");
                serializer.startTag(null, "server");
                serializer.text(c.getString(1));
                serializer.endTag(null, "server");
                serializer.endTag(null, "newserver");
            }
            c.close();

            serializer.endTag(null, "myapp");
            serializer.endDocument();
            serializer.flush();
            if (fileos != null) {
                fileos.close();
            }
        } catch (Exception e) {
            Log.e("Exception", "error occurred while creating xml file");
        }

    }

    private void showUpdateStoreCredentialsDialog(String string) {
        ContextThemeWrapper wrapper = new ContextThemeWrapper(this, this.obtainStyledAttributes(new int[]{R.attr.alertDialog}).getResourceId(0, 0));

        View credentialsDialogView = LayoutInflater.from(wrapper).inflate(R.layout.dialog_add_pvt_store, null);
        AlertDialog credentialsDialog = new AlertDialog.Builder(wrapper).setView(credentialsDialogView).create();
        credentialsDialog.setTitle(getString(R.string.add_private_store) + " " + RepoUtils.split(string));
        credentialsDialog.setButton(Dialog.BUTTON_NEUTRAL, getString(R.string.new_store), new UpdateStoreCredentialsListener(string, credentialsDialogView));
        if (!isFinishing()) {
            credentialsDialog.show();
        }

    }

    public void setBackgroundDialogStoreTheme(String theme, LinearLayout store_background_dialog) {
        EnumStoreTheme aptoideThemeDefault;
        String storeThemeString = "APTOIDE_STORE_THEME_" + theme.toUpperCase(Locale.ENGLISH);
        try {
            aptoideThemeDefault = EnumStoreTheme.valueOf(storeThemeString);
        } catch (Exception e) {
            aptoideThemeDefault = EnumStoreTheme.APTOIDE_STORE_THEME_NONE;
        }

        switch (aptoideThemeDefault) {
            case APTOIDE_STORE_THEME_DEFAULT:
                store_background_dialog.setBackgroundResource(R.drawable.dialog_background_light_default);
                break;

            case APTOIDE_STORE_THEME_GOLD:
                store_background_dialog.setBackgroundResource(R.drawable.dialog_background_light_gold);
                break;
            case APTOIDE_STORE_THEME_MAROON:
                store_background_dialog.setBackgroundResource(R.drawable.dialog_background_light_maroon);
                break;
            case APTOIDE_STORE_THEME_MIDNIGHT:
                store_background_dialog.setBackgroundResource(R.drawable.dialog_background_light_midnight);
                break;
            case APTOIDE_STORE_THEME_ORANGE:
                store_background_dialog.setBackgroundResource(R.drawable.dialog_background_light_orange);
                break;
            case APTOIDE_STORE_THEME_SPRINGGREEN:
                store_background_dialog.setBackgroundResource(R.drawable.dialog_background_light_springgreen);
                break;
            case APTOIDE_STORE_THEME_MAGENTA:
                store_background_dialog.setBackgroundResource(R.drawable.dialog_background_light_magenta);
                break;
            case APTOIDE_STORE_THEME_BLUE:
                store_background_dialog.setBackgroundResource(R.drawable.dialog_background_light_blue);
                break;
            case APTOIDE_STORE_THEME_DIMGRAY:
                store_background_dialog.setBackgroundResource(R.drawable.dialog_background_light_dimgray);
                break;
            case APTOIDE_STORE_THEME_LIGHTSKY:
                store_background_dialog.setBackgroundResource(R.drawable.dialog_background_light_lightsky);
                break;
            case APTOIDE_STORE_THEME_PINK:
                store_background_dialog.setBackgroundResource(R.drawable.dialog_background_light_pink);
                break;
            case APTOIDE_STORE_THEME_RED:
                store_background_dialog.setBackgroundResource(R.drawable.dialog_background_light_red);
                break;
            case APTOIDE_STORE_THEME_SEAGREEN:
                store_background_dialog.setBackgroundResource(R.drawable.dialog_background_light_seagreen);
                break;
            case APTOIDE_STORE_THEME_SILVER:
                store_background_dialog.setBackgroundResource(R.drawable.dialog_background_light_silver);
                break;
            case APTOIDE_STORE_THEME_SLATEGRAY:
                store_background_dialog.setBackgroundResource(R.drawable.dialog_background_light_slategray);
                break;
            case APTOIDE_STORE_THEME_NONE:
                store_background_dialog.setBackgroundResource(R.drawable.dialog_background_light);
                break;
            default:
                store_background_dialog.setBackgroundResource(R.drawable.dialog_background_light);
                break;
        }

    }

    static class ViewHolder {
        ImageView icon;
        TextView name;
        TextView vername;
        RatingBar rating;
        TextView downloads;
    }

    public class WebserviceOptions {
        String key;
        String value;


        private WebserviceOptions(String key, String value) {
            this.value = value;
            this.key = key;
        }

        @Override
        protected void finalize() throws Throwable {

            Log.d("TAG", "Garbage Collecting WebserviceResponse");
            super.finalize();
        }

        /**
         * Returns a string containing a concise, human-readable description of this
         * object. Subclasses are encouraged to override this method and provide an
         * implementation that takes into account the object's type and data. The
         * default implementation is equivalent to the following expression:
         * <pre>
         *   getClass().getName() + '@' + Integer.toHexString(hashCode())</pre>
         * <p>See <a href="{@docRoot}reference/java/lang/Object.html#writing_toString">Writing a useful
         * {@code toString} method</a>
         * if you intend implementing your own {@code toString} method.
         *
         * @return a printable representation of this object.
         */
        @Override
        public String toString() {
            return key + "=" + value;    //To change body of overridden methods use File | Settings | File Templates.
        }
    }

    private class ListViewPosition {

        int index;
        int top;

        public ListViewPosition(int top, int index) {
            this.top = top;
            this.index = index;
        }
    }

    public class AddStoreCredentialsListener implements DialogInterface.OnClickListener {
        private String url;
        private View dialog;

        public AddStoreCredentialsListener(String string, View credentialsDialogView) {
            this.url = string;
            this.dialog = credentialsDialogView;
        }

        @Override
        public void onClick(DialogInterface arg0, int which) {
            dialogAddStore(url, ((EditText) dialog.findViewById(R.id.username))
                    .getText().toString(),
                    ((EditText) dialog.findViewById(R.id.password)).getText()
                            .toString());
        }

    }

    public class UpdateStoreCredentialsListener implements DialogInterface.OnClickListener {
        private String url;
        private View dialog;

        public UpdateStoreCredentialsListener(String string, View credentialsDialogView) {
            this.url = string;
            this.dialog = credentialsDialogView;
        }

        @Override
        public void onClick(DialogInterface arg0, int which) {
            db.updateServerCredentials(url, ((EditText) dialog
                    .findViewById(R.id.username)).getText().toString(),
                    ((EditText) dialog.findViewById(R.id.password)).getText()
                            .toString());
            try {
                service.parseServer(db, db.getServer(url));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private class BreadCrumb {
        ListDepth depth;
        int i;

        public BreadCrumb(ListDepth depth, int i) {
            this.depth = depth;
            this.i = i;
        }
    }

    public class AvailableListAdapter extends CursorAdapter {


        public AvailableListAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View v = null;
            switch (depth) {
                case STORES:
                    v = LayoutInflater.from(context).inflate(R.layout.row_stores, null);
                    break;
                case CATEGORY1:
                    v = LayoutInflater.from(context).inflate(R.layout.row_catg_list, null);
                    break;
                case CATEGORY2:
                    v = LayoutInflater.from(context).inflate(R.layout.row_catg_list, null);
                    break;
                case TOPAPPS:
                case LATESTAPPS:
                case ALLAPPLICATIONS:
                case APPLICATIONS:
                case RECOMMENDED:
                    v = LayoutInflater.from(context).inflate(R.layout.row_app, null);
                    break;
                case LATEST_LIKES:
                    v = LayoutInflater.from(context).inflate(R.layout.row_latest_likes, null);
                    break;
                case LATEST_COMMENTS:
                    v = LayoutInflater.from(context).inflate(R.layout.row_latest_comments, null);
                    break;
                default:
                    break;
            }
            Animation animation = AnimationUtils.loadAnimation(mContext, android.R.anim.fade_in);
            v.startAnimation(animation);
            return v;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {


            String categoryName = null;
            int categoryNameResource = 0;

            switch (depth) {
                case STORES:

                    LinearLayout store_background_dialog = (LinearLayout) view.findViewById(R.id.store_background_dialog);
                    if (!joinStores_boolean) {
                        setBackgroundDialogStoreTheme(cursor.getString(cursor.getColumnIndex(DbStructure.COLUMN_STORE_THEME)), store_background_dialog);
                    } else {
                        setBackgroundDialogStoreTheme("none", store_background_dialog);
                    }
                    Log.d("MainActivity-store_theme", cursor.getString(cursor.getColumnIndex(DbStructure.COLUMN_STORE_THEME)));


                    com.nostra13.universalimageloader.core.ImageLoader.getInstance().displayImage(
                            cursor.getString(cursor.getColumnIndex("avatar_url")),
                            (ImageView) view.findViewById(R.id.avatar));
                    ((TextView) view.findViewById(R.id.store_name)).setText(cursor.getString(cursor.getColumnIndex("name")));

                    if (cursor.getString(cursor.getColumnIndex("status")).equals("PARSED")) {
                        ((TextView) view.findViewById(R.id.store_dwn_number)).setText(cursor.getString(cursor.getColumnIndex("downloads")) + " " + getString(R.string.downloads));
                    }
                    if (cursor.getString(cursor.getColumnIndex("status")).equals("QUEUED")) {
                        ((TextView) view.findViewById(R.id.store_dwn_number)).setText(getString(R.string.preparing_to_load));
                    }
                    if (cursor.getString(cursor.getColumnIndex("status")).contains("PARSING")) {
                        ((TextView) view.findViewById(R.id.store_dwn_number)).setText(cursor.getString(cursor.getColumnIndex("downloads"))
                                + " " + getString(R.string.downloads)
                                + " - "
                                + getString(R.string.loading_store));
                    }

                    if (cursor.getString(cursor.getColumnIndex("status")).equals(State.FAILED.name())) {
                        ((TextView) view.findViewById(R.id.store_dwn_number)).setText(R.string.loading_failed);
                    }

                    if (cursor.getString(cursor.getColumnIndex("status")).equals(State.FAILED.name())
                            || cursor.getString(cursor.getColumnIndex("status")).equals(State.PARSED.name())) {
                        view.setTag(1);
                    }
                    break;
                case TOPAPPS:
                case APPLICATIONS:
                case LATESTAPPS:
                case ALLAPPLICATIONS:
                case RECOMMENDED:
                    ViewHolder holder = (ViewHolder) view.getTag();
                    if (holder == null) {
                        holder = new ViewHolder();
                        holder.name = (TextView) view.findViewById(R.id.app_name);
                        holder.icon = (ImageView) view.findViewById(R.id.app_icon);
                        holder.vername = (TextView) view.findViewById(R.id.installed_versionname);
                        holder.downloads = (TextView) view.findViewById(R.id.downloads);
                        holder.rating = (RatingBar) view.findViewById(R.id.stars);
                        view.setTag(holder);
                    }
                    holder.name.setText(cursor.getString(1));
                    com.nostra13.universalimageloader.core.ImageLoader
                            .getInstance()
                            .displayImage(cursor.getString(cursor.getColumnIndex("iconspath"))
                                    + cursor.getString(cursor.getColumnIndex("imagepath")),
                                    holder.icon);

                    holder.vername.setText(cursor.getString(2));
                    try {
                        holder.rating.setRating(Float.parseFloat(cursor.getString(5)));
                    } catch (Exception e) {
                        holder.rating.setRating(0);
                    }
                    holder.downloads.setText(cursor.getString(6));
                    break;
                case CATEGORY1:
                    try {
                        categoryNameResource = EnumCategories.categories.get(cursor.getString(1));
                    } catch (Exception e) {
                        categoryName = cursor.getString(1);
                        Log.d("MainActivity-CATEGORY1", "Untranslated Category Name: " + categoryName);
                    }
                    if (categoryName == null) {
                        categoryName = getString(categoryNameResource);
                    }

                    ((TextView) view.findViewById(R.id.category_name)).setText(categoryName);
                    break;
                case CATEGORY2:
                    try {
                        categoryNameResource = EnumCategories.categories.get(cursor.getString(1));
                    } catch (Exception e) {
                        categoryName = cursor.getString(1);
                        Log.d("MainActivity-CATEGORY2", "Untranslated Category Name: " + categoryName);
                    }
                    if (categoryName == null) {
                        categoryName = getString(categoryNameResource);
                    }
                    ((TextView) view.findViewById(R.id.category_name)).setText(categoryName);
                    break;
                case LATEST_LIKES:
                    ((TextView) view.findViewById(R.id.app_name)).setText(cursor.getString(cursor.getColumnIndex("name")));
                    ((TextView) view.findViewById(R.id.app_name)).setCompoundDrawablesWithIntrinsicBounds(0, 0, cursor.getString(cursor.getColumnIndex("like"))
                            .equals("TRUE") ? R.drawable.up
                            : R.drawable.down, 0);
                    ((TextView) view.findViewById(R.id.user_like)).setText(getString(R.string.like_or_comment_by_user, cursor.getString(cursor.getColumnIndex("username"))));
                    break;
                case LATEST_COMMENTS:
                    ((TextView) view.findViewById(R.id.comment_on_app)).setText(getString(R.string.comment_on_application, cursor.getString(cursor.getColumnIndex("name"))));
                    ((TextView) view.findViewById(R.id.comment)).setText(cursor.getString(cursor.getColumnIndex("text")));
                    ((TextView) view.findViewById(R.id.comment_owner)).setText(getString(R.string.like_or_comment_by_user, cursor.getString(cursor.getColumnIndex("username"))));
                    ((TextView) view.findViewById(R.id.time)).setText(cursor.getString(cursor.getColumnIndex("time")));
                    break;
                default:
                    break;
            }
        }
    }

}
