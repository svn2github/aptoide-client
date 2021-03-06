/*******************************************************************************
 * Copyright (c) 2012 rmateus.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package cm.aptoide.pt;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlSerializer;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.StatFs;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.CursorAdapter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Xml;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import cm.aptoide.com.nostra13.universalimageloader.core.DisplayImageOptions;
import cm.aptoide.com.nostra13.universalimageloader.core.ImageLoader;
import cm.aptoide.com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import cm.aptoide.com.viewpagerindicator.TitlePageIndicator;
import cm.aptoide.pt.Server.State;
import cm.aptoide.pt.adapters.InstalledAdapter;
import cm.aptoide.pt.adapters.UpdatesAdapter;
import cm.aptoide.pt.adapters.ViewPagerAdapter;
import cm.aptoide.pt.contentloaders.SimpleCursorLoader;
import cm.aptoide.pt.services.AIDLServiceDownloadManager;
import cm.aptoide.pt.services.MainService;
import cm.aptoide.pt.services.MainService.LocalBinder;
import cm.aptoide.pt.services.ServiceDownloadManager;
import cm.aptoide.pt.sharing.WebViewFacebook;
import cm.aptoide.pt.sharing.WebViewTwitter;
import cm.aptoide.pt.util.Algorithms;
import cm.aptoide.pt.util.Md5Handler;
import cm.aptoide.pt.util.NetworkUtils;
import cm.aptoide.pt.util.RepoUtils;
import cm.aptoide.pt.views.ViewApk;
import cm.aptoide.pt.views.ViewCache;
import cm.aptoide.pt.views.ViewDownloadManagement;
import cm.aptoide.pt.webservices.login.Login;

public class MainActivity extends FragmentActivity/*SherlockFragmentActivity */implements LoaderCallbacks<Cursor> {
	private Intent serviceDownloadManagerIntent;

	private final static int AVAILABLE_LOADER = 0;
	private final static int INSTALLED_LOADER = 1;
	private final static int UPDATES_LOADER = 2;

	private final static int LATEST_COMMENTS = -2;
	private final static int LATEST_LIKES = -1;

	private HashMap<String, String> updateParams = new HashMap<String, String>();
	private static final String LATEST_VERSION_CODE_URI = "http://imgs.aptoide.com/latest_version.xml";
	private static final String TMP_UPDATE_FILE = Environment
			.getExternalStorageDirectory().getPath()
			+ "/.aptoide/aptoideUpdate.apk";

	private final String SDCARD = Environment.getExternalStorageDirectory()
			.getPath();
	private String LOCAL_PATH = SDCARD + "/.aptoide";

	private HashMap<ListDepth, ListViewPosition> scrollMemory = new HashMap<ListDepth, ListViewPosition>();

	private final Dialog.OnClickListener addRepoListener = new Dialog.OnClickListener() {

		@Override
		public void onClick(DialogInterface arg0, int arg1) {
			storeUri = ((EditText) alertDialog.findViewById(R.id.edit_uri))
					.getText().toString();
			dialogAddStore(storeUri, null, null);
		}

	};
	int a = 0;

	private class ListViewPosition{

		int index;
		int top;
		public ListViewPosition(int top, int index) {
			this.top=top;
			this.index=index;
		}
	}




	private void loadUIEditorsApps() {

		final int[] res_ids = { R.id.central, R.id.topleft, R.id.topright,
				R.id.bottomleft, R.id.bottomcenter, R.id.bottomright };
		final ArrayList<HashMap<String, String>> image_urls = db
				.getFeaturedGraphics();
		HashMap<String, String> image_url_highlight = db.getHighLightFeature();
		if (image_url_highlight != null) {
			a = 1;
			ImageView v = (ImageView) featuredView.findViewById(res_ids[0]);
//			imageLoader.DisplayImage(-1, image_url_highlight.get("url"), v,
//					mContext);
			DisplayImageOptions options = new DisplayImageOptions.Builder()
			 .displayer(new FadeInBitmapDisplayer(1000))
			 .cacheOnDisc()
			 .cacheInMemory()
			 .build();
			cm.aptoide.com.nostra13.universalimageloader.core.ImageLoader.getInstance().displayImage(image_url_highlight.get("url"), v,options);
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

		Collections.shuffle(image_urls);
		runOnUiThread(new Runnable() {

			public void run() {
				try {
					for (int i = a; i != res_ids.length; i++) {
						ImageView v = (ImageView) featuredView
								.findViewById(res_ids[i]);

//						imageLoader.DisplayImage(-1,
//								image_urls.get(i).get("url"), v, mContext);
						DisplayImageOptions options = new DisplayImageOptions.Builder()
						 .displayer(new FadeInBitmapDisplayer(1000))
						 .cacheOnDisc()
						 .cacheInMemory()
						 .build();
						cm.aptoide.com.nostra13.universalimageloader.core.ImageLoader.getInstance().displayImage(image_urls.get(i).get("url"), v,options);

						v.setTag(image_urls.get(i).get("id"));
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
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	private void loadRecommended() {

		if(Login.isLoggedIn(mContext)){
			((TextView) featuredView
					.findViewById(R.id.recommended_text))
					.setVisibility(View.GONE);
		}else{

			((TextView) featuredView
					.findViewById(R.id.recommended_text))
					.setVisibility(View.VISIBLE);
		}

		new Thread(new Runnable() {

			private ArrayList<HashMap<String, String>> valuesRecommended;

			public void run() {
				loadUIRecommendedApps();
				try {
					SAXParserFactory spf = SAXParserFactory.newInstance();
					SAXParser sp = spf.newSAXParser();
					NetworkUtils utils = new NetworkUtils();
					BufferedInputStream bis = new BufferedInputStream(
							utils.getInputStream(
									"http://webservices.aptoide.com/webservices/listUserBasedApks/"
											+ Login.getToken(mContext)
											+ "/10/xml", null, null, mContext),
							8 * 1024);
					File f = File.createTempFile("abc", "abc");
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
					if (!hash.equals(db.getItemBasedApksHash(parent_apk.getApkid()))) {
//						Database.database.beginTransaction();
						db.deleteItemBasedApks(parent_apk);
						sp.parse(f, new HandlerItemBased(parent_apk));
						db.insertItemBasedApkHash(hash, parent_apk.getApkid());
//						Database.database.setTransactionSuccessful();
//						Database.database.endTransaction();
						loadUIRecommendedApps();
					}
					f.delete();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			private void loadUIRecommendedApps() {
				valuesRecommended = db
						.getItemBasedApksRecommended("recommended");

				runOnUiThread(new Runnable() {

					public void run() {

						LinearLayout ll = (LinearLayout) featuredView
								.findViewById(R.id.recommended_container);
						ll.removeAllViews();
						LinearLayout llAlso = new LinearLayout(
								MainActivity.this);
						llAlso.setLayoutParams(new LayoutParams(
								LayoutParams.MATCH_PARENT,
								LayoutParams.WRAP_CONTENT));
						llAlso.setOrientation(LinearLayout.HORIZONTAL);
						if (valuesRecommended.isEmpty()) {
							if(Login.isLoggedIn(mContext)){
								TextView tv = new TextView(mContext);
								tv.setText(R.string.no_recommended_apps);
								tv.setTextAppearance(mContext, android.R.attr.textAppearanceMedium);
								tv.setPadding(10, 10, 10, 10);
								ll.addView(tv);
							}
						} else {

							for (int i = 0; i != valuesRecommended.size(); i++) {
								LinearLayout txtSamItem = (LinearLayout) getLayoutInflater()
										.inflate(R.layout.row_grid_item, null);
								((TextView) txtSamItem.findViewById(R.id.name))
										.setText(valuesRecommended.get(i).get(
												"name"));
								ImageLoader.getInstance().displayImage(valuesRecommended
										.get(i).get("icon"),
										(ImageView) txtSamItem
												.findViewById(R.id.icon));
								float stars = 0f;
								try {
									stars = Float.parseFloat(valuesRecommended
											.get(i).get("rating"));
								} catch (Exception e) {
									stars = 0f;
								}
								((RatingBar) txtSamItem
										.findViewById(R.id.rating))
										.setRating(stars);
								txtSamItem.setPadding(10, 0, 0, 0);
//								((TextView) txtSamItem.findViewById(R.id.version))
//									.setText(getString(R.string.version) +" "+ valuesRecommended.get(i).get("vername"));
								((TextView) txtSamItem.findViewById(R.id.downloads))
									.setText("(" + valuesRecommended.get(i).get("downloads") + " " + getString(R.string.downloads) + ")");
								txtSamItem.setTag(valuesRecommended.get(i).get(
										"_id"));
								txtSamItem.setLayoutParams(new LayoutParams(
										LayoutParams.MATCH_PARENT, 100, 1));
								// txtSamItem.setOnClickListener(featuredListener);
								txtSamItem
										.setOnClickListener(new OnClickListener() {

											@Override
											public void onClick(View arg0) {
												Intent i = new Intent(
														MainActivity.this,
														ApkInfo.class);
												long id = Long
														.parseLong((String) arg0
																.getTag());
												i.putExtra("_id", id);
												i.putExtra("top", true);
												i.putExtra("category",
														Category.ITEMBASED
																.ordinal());
												startActivity(i);
											}
										});

								txtSamItem.measure(0, 0);

								if (i % 2 == 0) {
									ll.addView(llAlso);

									llAlso = new LinearLayout(MainActivity.this);
									llAlso.setLayoutParams(new LayoutParams(
											LayoutParams.MATCH_PARENT, 100));
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

	private void loadFeatured() {
		new Thread(new Runnable() {

			public void run() {
				loadUIEditorsApps();
				try {
					SAXParserFactory spf = SAXParserFactory.newInstance();
					SAXParser sp = spf.newSAXParser();
					NetworkUtils utils = new NetworkUtils();
                    String url;

                    if(ApplicationAptoide.CUSTOMEDITORSCHOICE){
                        url = "http://" + ApplicationAptoide.DEFAULTSTORE +  ".store.aptoide.com/editors.xml";

                        if(((HttpURLConnection)new URL(url).openConnection()).getResponseCode()!=200){
                            url ="http://apps.store.aptoide.com/editors.xml";
                        }

                    }else{
                        url = "http://apps.store.aptoide.com/editors.xml";
                    }


					BufferedInputStream bis = new BufferedInputStream(
							utils
									.getInputStream(
											url,
											null, null, mContext), 8 * 1024);
					File f = File.createTempFile("tempFile","");
					OutputStream out = new FileOutputStream(f);
					byte buf[] = new byte[1024];
					int len;
					while ((len = bis.read(buf)) > 0)
						out.write(buf, 0, len);
					out.close();
					bis.close();
					Server server = new Server();
					String hash = Md5Handler.md5Calc(f);
					if (!hash.equals(db.getEditorsChoiceHash())) {
//						Database.database.beginTransaction();
						db.deleteEditorsChoice();
						sp.parse(f, new HandlerEditorsChoice(server));
						db.insertEditorsChoiceHash(hash);
//						Database.database.setTransactionSuccessful();
//						Database.database.endTransaction();
						loadUIEditorsApps();

					}
					f.delete();
				} catch (SAXException e){
//					Database.database.setTransactionSuccessful();
//					Database.database.endTransaction();
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}).start();

		new Thread(new Runnable() {

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
					if(ApplicationAptoide.CUSTOMEDITORSCHOICE){
                        url = "http://" + ApplicationAptoide.DEFAULTSTORE +  ".store.aptoide.com/top.xml";

                        if(((HttpURLConnection)new URL(url).openConnection()).getResponseCode()!=200){
                            url = "http://apps.store.aptoide.com/top.xml";
                        }

                    }else{
                        url = "http://apps.store.aptoide.com/top.xml";
                    }

					BufferedInputStream bis = new BufferedInputStream(
							utils
									.getInputStream(
											url,
											null, null, mContext), 8 * 1024);
					f = File.createTempFile("tempFile","");
					OutputStream out = new FileOutputStream(f);
					byte buf[] = new byte[1024];
					int len;
					while ((len = bis.read(buf)) > 0)
						out.write(buf, 0, len);
					out.close();
					bis.close();
//					Database.database.beginTransaction();
					sp.parse(f, new HandlerFeaturedTop(server));
					loadUItopapps();
					f.delete();
				} catch (ParserConfigurationException e) {
					e.printStackTrace();
				} catch (SAXException e) {
				} catch (IOException e) {
					e.printStackTrace();
				}
				if(f!=null)f.delete();
			}

		}).start();

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

				LinearLayout ll = (LinearLayout) featuredView
						.findViewById(R.id.container);
				ll.removeAllViews();
				LinearLayout llAlso = new LinearLayout(MainActivity.this);
				llAlso.setLayoutParams(new LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
				llAlso.setOrientation(LinearLayout.HORIZONTAL);
				for (int i = 0; i != values.size(); i++) {
					LinearLayout txtSamItem = (LinearLayout) getLayoutInflater()
							.inflate(R.layout.row_grid_item, null);
					((TextView) txtSamItem.findViewById(R.id.name))
							.setText(values.get(i).get("name"));
//					((TextView) txtSamItem.findViewById(R.id.version))
//							.setText(getString(R.string.version) +" "+ values.get(i).get("vername"));
					((TextView) txtSamItem.findViewById(R.id.downloads))
							.setText("(" + values.get(i).get("downloads") + " " + getString(R.string.downloads) + ")");
					String hashCode = (values.get(i).get("apkid") +"|"+values.get(i).get("vercode"))+"";
					cm.aptoide.com.nostra13.universalimageloader.core.ImageLoader.getInstance().displayImage(values.get(i).get("icon"), (ImageView) txtSamItem.findViewById(R.id.icon),hashCode);

//					imageLoader.DisplayImage(-1, values.get(i).get("icon"),
//							(ImageView) txtSamItem.findViewById(R.id.icon),
//							mContext);
					float stars = 0f;
					try {
						stars = Float.parseFloat(values.get(i).get("rating"));
					} catch (Exception e) {
						stars = 0f;
					}
					((RatingBar) txtSamItem.findViewById(R.id.rating))
							.setRating(stars);
					txtSamItem.setPadding(10, 0, 0, 0);
					txtSamItem.setTag(values.get(i).get("id"));
					txtSamItem.setLayoutParams(new LayoutParams(
							LayoutParams.MATCH_PARENT, 100, 1));
					// txtSamItem.setOnClickListener(featuredListener);
					txtSamItem.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View arg0) {
							Intent i = new Intent(MainActivity.this,
									ApkInfo.class);
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
						llAlso.setLayoutParams(new LayoutParams(
								LayoutParams.MATCH_PARENT, 100));
						llAlso.setOrientation(LinearLayout.HORIZONTAL);
						llAlso.addView(txtSamItem);
					} else {
						llAlso.addView(txtSamItem);
					}
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

	ArrayList<HashMap<String, String>> values;

	private void dialogAddStore(final String url, final String username,
			final String password) {
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
							if(pd.isShowing()){
								pd.dismiss();
							}
							refreshAvailableList(true);
						}
					});

				}

			}
		}).start();
	}

	private View addStoreButton;

	private final OnClickListener addStoreListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			showAddStoreDialog();
		}

	};

	private AlertDialog alertDialog;

	private View alertDialogView;

	private AvailableListAdapter availableAdapter;
	private ListView availableListView;
	private Loader<Cursor> availableLoader;
	private View availableView;
	private View updateView;
	private long category_id;
	private long category2_id;
	private final ServiceConnection conn = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			MainActivity.this.service = ((LocalBinder) service).getService();
            if(ApplicationAptoide.DEFAULTSTORE!=null){
                MainActivity.this.service.addStore(Database.getInstance(), "http://" + ApplicationAptoide.DEFAULTSTORE + ".store.aptoide.com/", null, null);
            }
            loadUi();
			getInstalled();
			getAllRepoStatus();
			loadFeatured();


			if(Login.isLoggedIn(mContext)){
				loadRecommended();
			}




		}

		@Override
		public void onServiceDisconnected(ComponentName name) {

		}
	};

	private Database db;

	private ListDepth depth = ListDepth.STORES;

	private View featuredView;

	private InstalledAdapter installedAdapter;

	private Loader<Cursor> installedLoader;
	private ListView installedView;

	private CheckBox joinStores;
	private boolean joinStores_boolean = false;
	public static Context mContext;

	private TextView pb;
	private boolean refreshClick = true;
	private final Dialog.OnClickListener searchStoresListener = new Dialog.OnClickListener() {

		@Override
		public void onClick(DialogInterface arg0, int arg1) {
			Uri uri = Uri.parse("http://m.aptoide.com/more/toprepos");
			Intent intent = new Intent(Intent.ACTION_VIEW, uri);
			startActivity(intent);
		}

	};

	private MainService service;
	private final BroadcastReceiver statusReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (depth.equals(ListDepth.STORES)) {
				availableLoader.forceLoad();
				System.out.println("Status broadcast received");
			}
		}
	};
private BroadcastReceiver parseFailedReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			View simpleMessageView = LayoutInflater.from(mContext).inflate(R.layout.dialog_simple_message, null);
			Builder dialogBuilder = new AlertDialog.Builder(mContext).setView(simpleMessageView);
			final AlertDialog failedDialog = dialogBuilder.create();
			failedDialog.setIcon(android.R.drawable.ic_dialog_alert);
			failedDialog.setTitle(getText(R.string.parse_error));
			failedDialog.setCancelable(true);
//			failedDialog.setMessage(getText(R.string.parse_error_loading));
			((TextView) simpleMessageView.findViewById(R.id.dialog_message)).setText(getText(R.string.parse_error_loading));
			failedDialog.setButton(Dialog.BUTTON_POSITIVE, getString(R.string.btn_yes), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					getAllRepoStatus();
				}
			});
			failedDialog.setButton(Dialog.BUTTON_NEGATIVE, getString(R.string.btn_cancel), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					return;
				}

			});
			failedDialog.setCancelable(false);
			failedDialog.show();
		}
	};
	private long store_id;

	private CursorAdapter updatesAdapter;

	private Loader<Cursor> updatesLoader;

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
					availableView.findViewById(R.id.refresh_view_layout)
							.setVisibility(View.VISIBLE);
					availableView
							.findViewById(R.id.refresh_view_layout)
							.findViewById(R.id.refresh_view)
							.startAnimation(
									AnimationUtils.loadAnimation(mContext,
											android.R.anim.fade_in));
				}
			}

		}
	};

	private ListView updatesListView;

	public class AddStoreCredentialsListener implements
			DialogInterface.OnClickListener {
		private String url;
		private View dialog;

		public AddStoreCredentialsListener(String string,
				View credentialsDialogView) {
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

	public class UpdateStoreCredentialsListener implements
	DialogInterface.OnClickListener {
		private String url;
		private View dialog;

		public UpdateStoreCredentialsListener(String string,
				View credentialsDialogView) {
			this.url = string;
			this.dialog = credentialsDialogView;
		}

		@Override
		public void onClick(DialogInterface arg0, int which) {
			db.updateServerCredentials(url, ((EditText) dialog.findViewById(R.id.username))
					.getText().toString(),
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

	public void getAllRepoStatus() {
		final HashMap<String, Long> serversToParse = new HashMap<String, Long>();
		new Thread(new Runnable() {

			@Override
			public void run() {
				String repos = "";
				String hashes = "";
				Cursor cursor = db.getStores(false);
				int i = 0;
				for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor
						.moveToNext()) {
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

					String url = "http://webservices.aptoide.com/webservices/listRepositoryChange/"
							+ repos + "/" + hashes + "/json";
					System.out.println(url);
					try {
						HttpURLConnection connection = (HttpURLConnection) new URL(
								url).openConnection();
						connection.connect();
						int rc = connection.getResponseCode();
						if (rc == 200) {
							NetworkUtils utils = new NetworkUtils();
							JSONObject json = utils.getJsonObject(
									url, mContext);

							JSONArray array = json.getJSONArray("listing");

							for (int o = 0; o != array.length(); o++) {
								boolean parse = Boolean.parseBoolean(array
										.getJSONObject(o).getString(
												"hasupdates"));
								long id = serversToParse.get(array
										.getJSONObject(o).getString("repo"));
								Server server = db.getServer(id, false);

								if (parse) {
									service.parseServer(db, server);
								}else{
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
						if(!ApplicationAptoide.MULTIPLESTORES){

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

	protected void redrawAll() {
		if(installedLoader!=null)installedLoader.forceLoad();
		if(availableLoader!=null)availableLoader.forceLoad();
		if(updatesLoader!=null)updatesLoader.forceLoad();
		new Thread(new Runnable() {

			@Override
			public void run() {
				loadUItopapps();
				if(Login.isLoggedIn(mContext)){
					loadRecommended();
				}

			}
		}).start();
	}

	protected void addStore(String uri_str, String username, String password) {

		if (uri_str.contains("http//")) {
			uri_str = uri_str.replaceFirst("http//", "http://");
		}

		if (uri_str.length() != 0
				&& uri_str.charAt(uri_str.length() - 1) != '/') {
			uri_str = uri_str + '/';
			Log.d("Aptoide-ManageRepo", "repo uri: " + uri_str);
		}
		if (!uri_str.startsWith("http://")) {
			uri_str = "http://" + uri_str;
			Log.d("Aptoide-ManageRepo", "repo uri: " + uri_str);
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
		final int response = utils.checkServerConnection(uri_str,
				username, password);
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
					Toast toast= Toast.makeText(mContext, mContext.getString(R.string.verify_store), Toast.LENGTH_SHORT);
					toast.show();
					showAddStoreDialog();
				}
			});
			break;
		case -1:
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					Toast toast= Toast.makeText(mContext, mContext.getString(R.string.an_error_check_net), Toast.LENGTH_SHORT);
					toast.show();
					showAddStoreDialog();
				}
			});
			break;
		default:
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					Toast toast= Toast.makeText(mContext, mContext.getString(R.string.error_occured)+" "+response, Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 30);
					toast.show();
					showAddStoreDialog();
				}
			});
			break;
		}

	}


	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
//		menu.add(Menu.NONE, EnumOptionsMenu.SEARCH.ordinal(),
//				EnumOptionsMenu.SEARCH.ordinal(), "Search")
//				.setIcon(R.drawable.ic_search)
//				.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
		menu.add(Menu.NONE, EnumOptionsMenu.LOGIN.ordinal(),
				EnumOptionsMenu.LOGIN.ordinal(), R.string.my_account)
				.setIcon(android.R.drawable.ic_menu_edit);
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

        if(ApplicationAptoide.PARTNERID==null){
		    menu.add(Menu.NONE, EnumOptionsMenu.ABOUT.ordinal(),
	    			EnumOptionsMenu.ABOUT.ordinal(), R.string.about).setIcon(
	    			android.R.drawable.ic_menu_help);
        }
		menu.add(Menu.NONE, EnumOptionsMenu.FOLLOW.ordinal(),
				EnumOptionsMenu.FOLLOW.ordinal(), R.string.social_networks).setIcon(
				android.R.drawable.ic_menu_share);
		menu.add(Menu.NONE, EnumOptionsMenu.DOWNLOADMANAGER.ordinal(),
				EnumOptionsMenu.DOWNLOADMANAGER.ordinal(), R.string.download_manager).setIcon(
				android.R.drawable.ic_menu_save);

		return super.onPrepareOptionsMenu(menu);
	}



	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		EnumOptionsMenu menuEntry = EnumOptionsMenu.reverseOrdinal(item
				.getItemId());
		Log.d("AptoideUploader-OptionsMenu", "menuOption: " + menuEntry
				+ " itemid: " + item.getItemId());
		switch (menuEntry) {
//		case SEARCH:
//			onSearchRequested();
//			break;
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
		case ABOUT:
			showAbout();
			break;
		case DOWNLOADMANAGER:
			startActivity(new Intent(this, DownloadManager.class));
			break;
		case FOLLOW:
			showFollow();
			break;
		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	void updateAll(){

		new Thread(new Runnable() {

			@Override
			public void run() {
				Cursor c = db.getUpdates(order);

				for(c.moveToFirst();!c.isAfterLast();c.moveToNext()){
					ViewApk apk = db.getApk(c.getLong(0), Category.INFOXML);
					try {
						ViewCache cache = new ViewCache(apk.hashCode(), apk.getMd5(),apk.getApkid(),apk.getVername());
						ViewDownloadManagement download = new ViewDownloadManagement(
								apk.getPath(),
								apk,
								cache,
								db.getServer(apk.getRepo_id(), false).getLogin());

						serviceDownloadManager.callStartDownload(download);
						Thread.sleep(1000);
					} catch (RemoteException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

				}

				c.close();
			}
		}).start();
	}

	@Override
	protected void onActivityResult(int arg0, int arg1, Intent arg2) {
		super.onActivityResult(arg0, arg1, arg2);
		if(installedLoader!=null)installedLoader.forceLoad();
		if(updatesLoader!=null)updatesLoader.forceLoad();
		if(availableLoader!=null)availableLoader.forceLoad();
		new Thread(new Runnable() {

			@Override
			public void run() {
				loadUItopapps();
			}
		}).start();

	}

	public void showAbout() {
		View aboutView = LayoutInflater.from(this).inflate(R.layout.dialog_about, null);
		Builder dialogBuilder = new AlertDialog.Builder(this).setView(aboutView);
		final AlertDialog aboutDialog = dialogBuilder.create();
		aboutDialog.setIcon(android.R.drawable.ic_menu_help);
		aboutDialog.setTitle(getString(R.string.about));
		aboutDialog.setCancelable(true);

		WindowManager.LayoutParams params = aboutDialog.getWindow().getAttributes();
		params.width=WindowManager.LayoutParams.MATCH_PARENT;
		aboutDialog.getWindow().setAttributes(params);

//		Button changelog = (Button) aboutView.findViewById(R.id.change_log_button);
//		changelog.setOnClickListener(new View.OnClickListener(){
//			@Override
//			public void onClick(View v) {
//
//			}
//		});

		aboutDialog.setButton(DialogInterface.BUTTON_POSITIVE,getString(R.string.btn_chlog), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				Uri uri = Uri.parse(getString(R.string.change_log_url));
				startActivity(new Intent( Intent.ACTION_VIEW, uri));
			}
		});

		aboutDialog.show();

	}

	private void displayOptionsDialog() {

		final SharedPreferences sPref = PreferenceManager
				.getDefaultSharedPreferences(this);
		final Editor editor = sPref.edit();

		View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_order_popup,
				null);
		Builder dialogBuilder = new AlertDialog.Builder(mContext).setView(view);
		final AlertDialog orderDialog = dialogBuilder.create();
		orderDialog.setIcon(android.R.drawable.ic_menu_sort_by_size);
		orderDialog.setTitle(getString(R.string.menu_display_options));
		orderDialog.setCancelable(true);

		final RadioButton ord_rct = (RadioButton) view
				.findViewById(R.id.org_rct);
		final RadioButton ord_abc = (RadioButton) view
				.findViewById(R.id.org_abc);
		final RadioButton ord_rat = (RadioButton) view
				.findViewById(R.id.org_rat);
		final RadioButton ord_dwn = (RadioButton) view
				.findViewById(R.id.org_dwn);
        final RadioButton ord_price = (RadioButton) view
                .findViewById(R.id.org_price);
		final RadioButton btn1 = (RadioButton) view.findViewById(R.id.shw_ct);
		final RadioButton btn2 = (RadioButton) view.findViewById(R.id.shw_all);

		final ToggleButton adult = (ToggleButton) view
				.findViewById(R.id.adultcontent_toggle);

		orderDialog.setButton(Dialog.BUTTON_NEUTRAL, "Ok",
				new Dialog.OnClickListener() {
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
							pop_change_category  = true;
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
							if(pop_change_category){

								if(!depth.equals(ListDepth.CATEGORY1)&&!depth.equals(ListDepth.STORES)){
									if(depth.equals(ListDepth.APPLICATIONS)){
										removeLastBreadCrumb();
									}
									removeLastBreadCrumb();
									depth=ListDepth.CATEGORY1;
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
        if(!ApplicationAptoide.MATURECONTENTSWITCH){
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

	private void getInstalled() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				List<PackageInfo> system_installed_list = getPackageManager()
						.getInstalledPackages(0);
				List<String> database_installed_list = db.getStartupInstalled();
				for (PackageInfo pkg : system_installed_list) {
					if (!database_installed_list.contains(pkg.packageName)) {
						try {
							ViewApk apk = new ViewApk();
							apk.setApkid(pkg.packageName);
							apk.setVercode(pkg.versionCode);
							apk.setVername(pkg.versionName);
							apk.setName((String) pkg.applicationInfo
									.loadLabel(getPackageManager()));
							db.insertInstalled(apk);
						} catch (Exception e) {
							e.printStackTrace();
						} finally {

						}
					}
				}

				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						installedLoader = getSupportLoaderManager().initLoader(
								INSTALLED_LOADER, null, MainActivity.this);
						installedView.setAdapter(installedAdapter);
						getUpdates();
					}
				});
			}
		}).start();
	}

	private void getUpdates() {
		updatesLoader = getSupportLoaderManager().initLoader(UPDATES_LOADER,
				null, MainActivity.this);
		updatesListView.setAdapter(updatesAdapter);
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
						result = service
								.deleteStore(db, ((AdapterContextMenuInfo) item
										.getMenuInfo()).id);
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
									Toast toast= Toast.makeText(mContext, mContext.getString(R.string.error_delete_store), Toast.LENGTH_SHORT);
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
						service.parseServer(db,
								db.getServer(((AdapterContextMenuInfo) item
										.getMenuInfo()).id, false));
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

	LinearLayout breadcrumbs;
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
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	};
	protected Order order;
	private BroadcastReceiver newRepoReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d("Aptoide-MainActivity","onNewRepoReceive");
			if (intent.hasExtra("newrepo")) {
				ArrayList<String> repos = (ArrayList<String>) intent.getSerializableExtra("newrepo");
				for (final String uri2 : repos) {
					View simpleMessageView = LayoutInflater.from(mContext).inflate(R.layout.dialog_simple_message, null);
					Builder dialogBuilder = new AlertDialog.Builder(mContext).setView(simpleMessageView);
					final AlertDialog alertDialog = dialogBuilder.create();
					alertDialog.setIcon(android.R.drawable.ic_menu_add);
					alertDialog.setTitle(getString(R.string.add_store));
					((TextView) simpleMessageView.findViewById(R.id.dialog_message)).setText(getString(R.string.newrepo_alrt)
							+ uri2 + " ?");
					alertDialog.setButton(Dialog.BUTTON_POSITIVE,
							getString(android.R.string.yes),
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialogAddStore(uri2, null, null);
								}

							});
					alertDialog.setButton(Dialog.BUTTON_NEGATIVE,
							getString(android.R.string.no),
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									return;
								}

							});
					alertDialog.show();

				}
			}

		}
	};
	private String storeUri = "apps.store.aptoide.com";

	ViewPager pager;
	private AIDLServiceDownloadManager serviceDownloadManager;
	private ServiceConnection serviceManagerConnection = new ServiceConnection() {


		public void onServiceConnected(ComponentName className, IBinder service) {
			// This is called when the connection with the service has been
			// established, giving us the object we can use to
			// interact with the service.  We are communicating with the
			// service using AIDL, so here we set the remote service interface.
			serviceDownloadManager = AIDLServiceDownloadManager.Stub.asInterface(service);
			((UpdatesAdapter)updatesAdapter).setServiceDownloadManager(serviceDownloadManager);
			Log.v("Aptoide-UpdatesAdapter", "Connected to ServiceDownloadManager");

		}

		public void onServiceDisconnected(ComponentName className) {
			// This is called when the connection with the service has been
			// unexpectedly disconnected -- that is, its process crashed.
			serviceDownloadManagerIntent = null;

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


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		SetAptoideTheme.setAptoideTheme(this);
		super.onCreate(savedInstanceState);

		serviceDownloadManagerIntent = new Intent(this, ServiceDownloadManager.class);
		startService(serviceDownloadManagerIntent);

		File sdcard_file = new File(SDCARD);
		if (!sdcard_file.exists() || !sdcard_file.canWrite()) {

			View simpleMessageView = LayoutInflater.from(this).inflate(R.layout.dialog_simple_message, null);
			Builder dialogBuilder = new AlertDialog.Builder(this).setView(simpleMessageView);
			final AlertDialog upd_alrt = dialogBuilder.create();
			upd_alrt.setIcon(android.R.drawable.ic_dialog_alert);
			upd_alrt.setTitle(getText(R.string.remote_in_noSD_title));
			((TextView) simpleMessageView.findViewById(R.id.dialog_message)).setText(getText(R.string.remote_in_noSD));
			upd_alrt.setButton(Dialog.BUTTON_NEUTRAL, getText(android.R.string.ok),
					new Dialog.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							finish();
						}
					});
			upd_alrt.show();

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

				final AlertDialog upd_alrt = new AlertDialog.Builder(this)
						.create();
				upd_alrt.setIcon(android.R.drawable.ic_dialog_alert);
				upd_alrt.setTitle(getText(R.string.remote_in_noSD_title));
				upd_alrt.setMessage(getText(R.string.remote_in_noSDspace));
				upd_alrt.setButton(Dialog.BUTTON_NEUTRAL,
						getText(android.R.string.ok), new Dialog.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								finish();
							}
						});
				upd_alrt.show();
			} else {

				mContext = this;
				SharedPreferences sPref = PreferenceManager
						.getDefaultSharedPreferences(mContext);
				editor = PreferenceManager
						.getDefaultSharedPreferences(mContext).edit();

				if (!sPref.contains("matureChkBox")) {

					editor.putBoolean("matureChkBox", ApplicationAptoide.MATURECONTENTSWITCHVALUE);
					SharedPreferences sPrefOld = getSharedPreferences("aptoide_prefs", MODE_PRIVATE);
					if(sPrefOld.getString("app_rating", "none").equals("Mature")){
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
				File file = new File(LOCAL_PATH + "/apks");
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
						File dir = new File(LOCAL_PATH + "/apks");

						// Optain list of files in the directory.
						// listFiles() returns a list of File objects to each
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
				}).start();
				db = Database.getInstance();

				Intent i = new Intent(mContext, MainService.class);
				startService(i);
				bindService(i, conn, Context.BIND_AUTO_CREATE);
				order = Order.values()[PreferenceManager
						.getDefaultSharedPreferences(mContext).getInt(
								"order_list", 0)];

				registerReceiver(updatesReceiver, new IntentFilter("update"));
				registerReceiver(statusReceiver, new IntentFilter("status"));
				registerReceiver(loginReceiver, new IntentFilter("login"));
				registerReceiver(storePasswordReceiver , new IntentFilter("401"));
				registerReceiver(redrawInstalledReceiver, new IntentFilter(
						"pt.caixamagica.aptoide.REDRAW"));
				if(!ApplicationAptoide.MULTIPLESTORES){
					registerReceiver(parseFailedReceiver  , new IntentFilter("PARSE_FAILED"));
				}

				registerReceiver(newRepoReceiver, new IntentFilter(
						"pt.caixamagica.aptoide.NEWREPO"));
				registered=true;


				if (sPref.getBoolean("firstrun", true)) {
					// Intent shortcutIntent = new Intent(Intent.ACTION_MAIN);
					// shortcutIntent.setClassName("cm.aptoide.pt",
					// "cm.aptoide.pt.Start");
					// final Intent intent = new Intent();
					// intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT,
					// shortcutIntent);
					//
					// intent.putExtra(Intent.EXTRA_SHORTCUT_NAME,
					// getString(R.string.app_name));
					// Parcelable iconResource =
					// Intent.ShortcutIconResource.fromContext(this,
					// R.drawable.ic_launcher);
					//
					// intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
					// iconResource);
					// intent.putExtra("duplicate", false);
					// intent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
					// sendBroadcast(intent);

					if (new File(LOCAL_PATH + "/servers.xml").exists() && ApplicationAptoide.DEFAULTSTORE==null) {
						try {

							SAXParserFactory spf = SAXParserFactory
									.newInstance();
							SAXParser sp = spf.newSAXParser();

							MyappHandler handler = new MyappHandler();

							sp.parse(new File(LOCAL_PATH + "/servers.xml"),
									handler);
							ArrayList<String> server = handler.getServers();
							getIntent().putExtra("newrepo", server);

						} catch (Exception e) {
							e.printStackTrace();
						}

					}
					editor.putBoolean("firstrun", false);
					editor.putBoolean("orderByCategory", true);
					editor.commit();

				}



				if (getIntent().hasExtra("newrepo")) {
					ArrayList<String> repos = (ArrayList<String>) getIntent()
							.getSerializableExtra("newrepo");
					for (final String uri2 : repos) {
						View simpleMessageView = LayoutInflater.from(mContext).inflate(R.layout.dialog_simple_message, null);
						Builder dialogBuilder = new AlertDialog.Builder(mContext).setView(simpleMessageView);
						final AlertDialog alertDialog = dialogBuilder.create();
						alertDialog.setIcon(android.R.drawable.ic_menu_add);
						alertDialog.setTitle(getString(R.string.add_store));
						((TextView) simpleMessageView.findViewById(R.id.dialog_message)).setText(getString(R.string.newrepo_alrt)
								+ uri2 + " ?");
						alertDialog.setButton(Dialog.BUTTON_POSITIVE,
								getString(android.R.string.yes),
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										dialogAddStore(uri2, null, null);
									}

								});
						alertDialog.setButton(Dialog.BUTTON_NEGATIVE,
								getString(android.R.string.no),
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										return;
									}

								});
						alertDialog.show();

					}
				} else if (db.getStores(false).getCount() == 0 && ApplicationAptoide.DEFAULTSTORE==null) {


					View simpleMessageView = LayoutInflater.from(mContext).inflate(R.layout.dialog_simple_message, null);
					Builder dialogBuilder = new AlertDialog.Builder(mContext).setView(simpleMessageView);
					final AlertDialog alertDialog = dialogBuilder.create();
					alertDialog.setIcon(android.R.drawable.ic_menu_add);
					alertDialog.setTitle(getString(R.string.add_store));
					((TextView) simpleMessageView.findViewById(R.id.dialog_message)).setText(getString(R.string.myrepo_alrt) + "\n"
							+ "http://apps.store.aptoide.com/");
					alertDialog.setIcon(android.R.drawable.ic_menu_add);
					alertDialog.setButton(Dialog.BUTTON_POSITIVE, "Yes",
							new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog,
								int which) {
							dialogAddStore(
									"http://apps.store.aptoide.com",
									null, null);
						}
					});

					alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
							getString(android.R.string.no),
							new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog,
								int which) {
							return;
						}

					});
					alertDialog.show();

				}

				new Thread(new Runnable() {

					@Override
					public void run() {
						try{
							getUpdateParameters();

							if(getPackageManager().getPackageInfo(getPackageName(), 0).versionCode<
									Integer.parseInt(updateParams.get("versionCode"))){
								runOnUiThread(new Runnable() {

									@Override
									public void run() {
										requestUpdateSelf();
									}
								});

							}

						}catch (Exception e){
							e.printStackTrace();
						}

					}
				}).start();






			}

		}

	}

    private void loadUi() {
        setContentView(R.layout.activity_aptoide);
        TitlePageIndicator indicator = (TitlePageIndicator) findViewById(R.id.indicator);
        pager = (ViewPager) findViewById(R.id.viewpager);

        if(!ApplicationAptoide.MULTIPLESTORES){
            depth = ListDepth.CATEGORY1;
            store_id = 1;
        }

        featuredView = LayoutInflater.from(mContext).inflate(
                R.layout.page_featured, null);

        availableView = LayoutInflater.from(mContext).inflate(
                R.layout.page_available, null);
        updateView = LayoutInflater.from(mContext).inflate(
                R.layout.page_updates, null);
        breadcrumbs = (LinearLayout) availableView
                .findViewById(R.id.breadcrumb_container);
        installedView = new ListView(mContext);
        updatesListView = (ListView) updateView.findViewById(R.id.updates_list);

        updateView.findViewById(R.id.update_button).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                updateAll();
            }
        });
        availableListView = (ListView) availableView
                .findViewById(R.id.available_list);
        availableListView.setFastScrollEnabled(true);
        updatesListView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener(
        ) {

            @Override
            public void onCreateContextMenu(ContextMenu menu, View v,
                                            ContextMenuInfo menuInfo) {
                menu.add(0, (int) ((AdapterContextMenuInfo) menuInfo).id, 0, mContext.getString(R.string.exclude_update)).setOnMenuItemClickListener(new OnMenuItemClickListener() {

                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        System.out.println(item.getItemId());
//									((UpdatesAdapter.ViewHolder)view.getTag()).updateExcluded = true;
                        db.addToExcludeUpdate(item.getItemId());
                        updatesLoader.forceLoad();
                        return false;
                    }
                });
            }
        });
        availableView.findViewById(R.id.refresh_view_layout)
                .findViewById(R.id.refresh_view)
                .setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        refreshClick = true;
                        availableView.findViewById(
                                R.id.refresh_view_layout)
                                .setVisibility(View.GONE);
                        refreshAvailableList(false);

                    }
                });
        joinStores = (CheckBox) availableView
                .findViewById(R.id.join_stores);
        joinStores
                .setOnCheckedChangeListener(new OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(
                            CompoundButton buttonView, boolean isChecked) {
                        joinStores_boolean = isChecked;
                        // if (isChecked) {
                        // addBreadCrumb("All Stores", depth);
                        // } else {
                        // breadcrumbs.removeAllViews();
                        // }
                        refreshAvailableList(true);
                    }
                });

        availableAdapter = new AvailableListAdapter(mContext, null,
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        installedAdapter = new InstalledAdapter(mContext, null,
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER, db);
        updatesAdapter = new UpdatesAdapter(mContext, null,
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        bindService(serviceDownloadManagerIntent, serviceManagerConnection, BIND_AUTO_CREATE);
        pb = (TextView) availableView.findViewById(R.id.loading_pb);
        pb.setText(R.string.add_store_button_below);
        addStoreButton = availableView.findViewById(R.id.add_store);
        addStoreButton.setOnClickListener(addStoreListener);

        if(!ApplicationAptoide.MULTIPLESTORES){
            addStoreButton.setVisibility(View.GONE);

        }

        availableListView
                .setOnItemClickListener(new OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent,
                                            View view, int position, long id) {
                        Intent i;
                        View v = availableListView.getChildAt(0);
                        scrollMemory.put(depth, new ListViewPosition((v == null) ? 0 : v.getTop(), availableListView.getFirstVisiblePosition()));
                        switch (depth) {
                            case STORES:
                                depth = ListDepth.CATEGORY1;
                                store_id = id;
                                break;
                            case CATEGORY1:
                                String category = ((Cursor) parent
                                        .getItemAtPosition(position))
                                        .getString(1);
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

                                i = new Intent(MainActivity.this,
                                        ApkInfo.class);
                                i.putExtra("_id", id);
                                i.putExtra("top", true);
                                i.putExtra("category",
                                        Category.TOP.ordinal());
                                startActivity(i);
                                return;
                            case LATESTAPPS:
                                i = new Intent(MainActivity.this,
                                        ApkInfo.class);
                                i.putExtra("_id", id);
                                i.putExtra("top", true);
                                i.putExtra("category",
                                        Category.LATEST.ordinal());
                                startActivity(i);
                                return;
                            case APPLICATIONS:
                            case ALLAPPLICATIONS:
                            case RECOMMENDED:
                                i = new Intent(MainActivity.this,
                                        ApkInfo.class);
                                i.putExtra("_id", id);
                                i.putExtra("top", false);
                                i.putExtra("category",
                                        Category.INFOXML.ordinal());
                                startActivity(i);
                                return;
                            case LATEST_COMMENTS:
                            case LATEST_LIKES:

                                String apkid = ((Cursor) parent
                                        .getItemAtPosition(position))
                                        .getString(1);

                                latestClick(apkid);
                                return;
                            default:
                                return;
                        }
                        addBreadCrumb(((Cursor) parent
                                .getItemAtPosition(position))
                                .getString(1), depth);
                        refreshAvailableList(true);
                    }
                });
        installedView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                    int arg2, long id) {
                Intent i = new Intent(MainActivity.this, ApkInfo.class);
                i.putExtra("_id", id);
                i.putExtra("installed", true);
                i.putExtra("category", Category.INFOXML.ordinal());
                startActivity(i);
            }
        });
//				getSupportActionBar().setIcon(R.drawable.brand_padding);
//				getSupportActionBar().setTitle("");
//				getSupportActionBar().setHomeButtonEnabled(false);
        findViewById(R.id.btsearch).setOnClickListener(
                new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        onSearchRequested();

                    }
                });
        updatesListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                    int arg2, long id) {
                Intent i = new Intent(MainActivity.this, ApkInfo.class);
                i.putExtra("_id", id);
                i.putExtra("updates", true);
                i.putExtra("category", Category.INFOXML.ordinal());
                startActivity(i);
            }
        });
        // LoaderManager.enableDebugLogging(true);
        availableLoader = getSupportLoaderManager().initLoader(
                AVAILABLE_LOADER, null, this);

        ArrayList<View> views = new ArrayList<View>();
        views.add(featuredView);
        views.add(availableView);
        views.add(installedView);
        views.add(updateView);

        pager.setAdapter(new ViewPagerAdapter(mContext, views));
        indicator.setViewPager(pager);
        refreshAvailableList(true);

        if(!ApplicationAptoide.MULTIPLESTORES){
            addBreadCrumb("Store", ListDepth.CATEGORY1);
        }else{
            addBreadCrumb(getString(R.string.stores), ListDepth.STORES);
        }

        if(!ApplicationAptoide.MATURECONTENTSWITCH){
            featuredView.findViewById(R.id.toggleButton1).setVisibility(View.GONE);
            featuredView.findViewById(R.id.adultcontent_label).setVisibility(View.GONE);
        }
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
							Intent i = new Intent(MainActivity.this,
									ApkInfo.class);
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
							Toast toast= Toast.makeText(mContext, mContext.getString(R.string.error_latest_apk), Toast.LENGTH_SHORT);
							toast.show();
						}
					});
				}

			}
		}).start();

	}

	private class BreadCrumb {
		ListDepth depth;
		int i;

		public BreadCrumb(ListDepth depth, int i) {
			this.depth = depth;
			this.i = i;
		}
	}

	protected void addBreadCrumb(String itemAtPosition, ListDepth depth2) {
		if (itemAtPosition.contains("http://")) {
			itemAtPosition = itemAtPosition.split("http://")[1];
			itemAtPosition = itemAtPosition.split(".store")[0];
		}
		Button bt = (Button) LayoutInflater.from(mContext).inflate(
				R.layout.breadcrumb, null);
		bt.setText(itemAtPosition);
		bt.setTag(new BreadCrumb(depth, breadcrumbs.getChildCount() + 1));
		System.out.println(breadcrumbs.getChildCount() + 1);
		bt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				depth = ((BreadCrumb) v.getTag()).depth;
				breadcrumbs.removeViews(((BreadCrumb) v.getTag()).i,
						breadcrumbs.getChildCount()
								- ((BreadCrumb) v.getTag()).i);
				refreshAvailableList(true);
			}
		});
		breadcrumbs.addView(bt, new LinearLayout.LayoutParams(-2,
				LayoutParams.WRAP_CONTENT, 1f));
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		Integer tag = (Integer) ((AdapterContextMenuInfo) menuInfo).targetView
				.getTag();
		if (tag != null && tag == 1) {
			menu.add(0, 1, 0, R.string.menu_context_reparse);
		}
		menu.add(0, 0, 0, R.string.menu_context_remove);

	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		SimpleCursorLoader a = null;
		switch (id) {
		case AVAILABLE_LOADER:
			a = new SimpleCursorLoader(mContext) {

				@Override
				public Cursor loadInBackground() {
					switch (depth) {
					case STORES:
						return db.getStores(joinStores_boolean);
					case CATEGORY1:
						return db.getCategory1(
								store_id,
								joinStores_boolean,
								!PreferenceManager.getDefaultSharedPreferences(
										mContext).getBoolean("orderByCategory",
										true));
					case CATEGORY2:
						return db.getCategory2(category_id, store_id,
								joinStores_boolean);
					case ALLAPPLICATIONS:
					case APPLICATIONS:
						return db.getApps(category2_id, store_id,
								joinStores_boolean, order, !PreferenceManager
										.getDefaultSharedPreferences(mContext)
										.getBoolean("orderByCategory", true));
					case TOPAPPS:
						return db.getTopApps(store_id,
								joinStores_boolean);
					case LATESTAPPS:
						return db.getLatestApps(store_id,
								joinStores_boolean);
					case LATEST_LIKES:
						return new LatestLikesComments(store_id, db, mContext)
								.getLikes();
					case LATEST_COMMENTS:
						return new LatestLikesComments(store_id, db, mContext)
								.getComments();
					case RECOMMENDED:
						final Cursor c = db.getUserBasedApk(store_id,joinStores_boolean);
						runOnUiThread(new Runnable() {

							@Override
							public void run() {
								if (c.getCount() == 0) {
									Toast toast= Toast.makeText(mContext, mContext.getString(R.string.no_recommended_toast), Toast.LENGTH_SHORT);
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
	protected void onDestroy() {
		if(service!=null){
			unbindService(conn);
		}
		if(serviceDownloadManager!=null){
			unbindService(serviceManagerConnection);
		}
		if(registered ){
			unregisterReceiver(updatesReceiver);
			unregisterReceiver(statusReceiver);
			unregisterReceiver(redrawInstalledReceiver);
			unregisterReceiver(loginReceiver);
			unregisterReceiver(newRepoReceiver);
			unregisterReceiver(storePasswordReceiver);
            if(!ApplicationAptoide.MULTIPLESTORES){
                unregisterReceiver(parseFailedReceiver);
            }
        }

//		stopService(serviceDownloadManagerIntent);
		generateXML();
		super.onDestroy();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			View v = availableListView.getChildAt(0);
			scrollMemory.put(depth, new ListViewPosition((v == null) ? 0 : v.getTop(),availableListView.getFirstVisiblePosition()));

            if(!ApplicationAptoide.MULTIPLESTORES){
                if (!depth.equals(ListDepth.CATEGORY1)&&pager.getCurrentItem()==1) {
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
            }else {
                if (!depth.equals(ListDepth.STORES)&&pager.getCurrentItem()==1) {
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

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		((CursorAdapter) availableListView.getAdapter()).swapCursor(null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		switch (loader.getId()) {
		case AVAILABLE_LOADER:
			availableAdapter.swapCursor(data);
			if(scrollMemory.get(depth)!=null){
				ListViewPosition lvp = scrollMemory.get(depth);
				availableListView.setSelectionFromTop(lvp.index, lvp.top);
			}
			break;
		case INSTALLED_LOADER:
			installedAdapter.swapCursor(data);
			break;
		case UPDATES_LOADER:
			updatesAdapter.swapCursor(data);
			if(data.getCount()==1){
				updateView.findViewById(R.id.all_apps_up_to_date).setVisibility(View.GONE);
				updateView.findViewById(R.id.update_all_view_layout).setVisibility(View.GONE);
			}else if(data.getCount()>1){
				updateView.findViewById(R.id.update_all_view_layout).setVisibility(View.VISIBLE);
//				updateView.findViewById(R.id.update_all_view_layout).startAnimation(AnimationUtils.loadAnimation(mContext, android.R.anim.fade_in));
				updateView.findViewById(R.id.all_apps_up_to_date).setVisibility(View.GONE);
			}else {
				updateView.findViewById(R.id.update_all_view_layout).setVisibility(View.GONE);
				updateView.findViewById(R.id.all_apps_up_to_date).setVisibility(View.VISIBLE);
				((TextView) updateView.findViewById(R.id.all_apps_up_to_date)).setText(R.string.all_updated);
			}
			break;
		default:
			break;
		}
		pb.setVisibility(View.GONE);
		if (availableListView.getAdapter().getCount() > 1 || joinStores_boolean) {
			joinStores.setVisibility(View.VISIBLE);
		} else {
			joinStores.setVisibility(View.INVISIBLE);
		}

		if (availableListView.getAdapter().getCount() > 0){
			pb.setVisibility(View.GONE);
		}else if(depth==ListDepth.STORES){
			pb.setVisibility(View.VISIBLE);
			pb.setText(R.string.add_store_button_below);
		}


	}

	private void refreshAvailableList(boolean setAdapter) {
		if (depth.equals(ListDepth.STORES)) {
			availableView.findViewById(R.id.add_store_layout).setVisibility(
					View.VISIBLE);
			registerForContextMenu(availableListView);
		} else {
			unregisterForContextMenu(availableListView);
			availableListView.setLongClickable(false);
			availableView.findViewById(R.id.add_store_layout).setVisibility(
					View.GONE);
		}
		availableView.findViewById(R.id.refresh_view_layout).setVisibility(
				View.GONE);
		refreshClick = true;
		availableAdapter.changeCursor(null);
		pb.setVisibility(View.VISIBLE);
		pb.setText(R.string.please_wait);
		if (setAdapter) {
			availableListView.setAdapter(availableAdapter);
		}
		availableLoader.forceLoad();

	}

	private void showAddStoreDialog() {
		alertDialogView = LayoutInflater.from(mContext).inflate(
				R.layout.dialog_add_store, null);
		alertDialog = new AlertDialog.Builder(mContext)
				.setView(alertDialogView).create();
		alertDialog.setTitle(getString(R.string.new_store));
		alertDialog.setButton(Dialog.BUTTON_NEGATIVE,
				getString(R.string.new_store), addRepoListener);
		alertDialog.setButton(Dialog.BUTTON_POSITIVE,
				getString(R.string.search_for_stores), searchStoresListener);
		((EditText) alertDialogView.findViewById(R.id.edit_uri))
				.setText(storeUri);
		alertDialog.show();
	}

	private void showAddStoreCredentialsDialog(String string) {
		View credentialsDialogView = LayoutInflater.from(mContext).inflate(
				R.layout.dialog_add_pvt_store, null);
		AlertDialog credentialsDialog = new AlertDialog.Builder(mContext)
				.setView(credentialsDialogView).create();
		credentialsDialog.setTitle(getString(R.string.add_private_store) + " " + RepoUtils.split(string));
		credentialsDialog.setButton(Dialog.BUTTON_NEUTRAL,
				getString(R.string.new_store), new AddStoreCredentialsListener(
						string, credentialsDialogView));
		credentialsDialog.show();
	}

	private void showUpdateStoreCredentialsDialog(String string) {
		View credentialsDialogView = LayoutInflater.from(mContext).inflate(
				R.layout.dialog_add_pvt_store, null);
		AlertDialog credentialsDialog = new AlertDialog.Builder(mContext)
				.setView(credentialsDialogView).create();
		credentialsDialog.setTitle(getString(R.string.add_private_store) + " " + RepoUtils.split(string));
		credentialsDialog.setButton(Dialog.BUTTON_NEUTRAL,
				getString(R.string.new_store), new UpdateStoreCredentialsListener(
						string, credentialsDialogView));
		credentialsDialog.show();
	}

	ImageLoader loader;

	public class AvailableListAdapter extends CursorAdapter {

		public AvailableListAdapter(Context context, Cursor c, int flags) {
			super(context, c, flags);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			switch (depth) {
			case STORES:
				String hashcode = cursor.getString(cursor.getColumnIndex("avatar_url"));
				cm.aptoide.com.nostra13.universalimageloader.core.ImageLoader.getInstance().displayImage(
						cursor.getString(cursor.getColumnIndex("avatar_url")),
						(ImageView) view.findViewById(R.id.avatar),
						hashcode);
				((TextView) view.findViewById(R.id.store_name)).setText(cursor.getString(cursor.getColumnIndex("name")));

				if (cursor.getString(cursor.getColumnIndex("status")).equals("PARSED")) {
					((TextView) view.findViewById(R.id.store_dwn_number)).setText(cursor.getString(cursor.getColumnIndex("downloads")) + " downloads");
				}
				if (cursor.getString(cursor.getColumnIndex("status")).equals("QUEUED")) {
					((TextView) view.findViewById(R.id.store_dwn_number)).setText(getString(R.string.preparing_to_load));
				}
				if (cursor.getString(cursor.getColumnIndex("status")).contains("PARSING")) {
					((TextView) view.findViewById(R.id.store_dwn_number)).setText(cursor.getString(cursor.getColumnIndex("downloads")) + " downloads" + " - " + getString(R.string.loading_store));
				}

//                Log.d("TAAAAG",cursor.getString(cursor.getColumnIndex("status")));
//                Log.d("TAAAAG",cursor.getString(cursor.getColumnIndex("url")));
//
//                if (cursor.getString(cursor.getColumnIndex("status")).equals("PARSINGTOP")) {
//                    ((TextView) view.findViewById(R.id.store_dwn_number)).setText(cursor.getString(cursor.getColumnIndex("downloads")) + " downloads" + " - " + "Updating Top");
//                }
//
//                if (cursor.getString(cursor.getColumnIndex("status")).equals("PARSINGLATEST")) {
//                    ((TextView) view.findViewById(R.id.store_dwn_number)).setText(cursor.getString(cursor.getColumnIndex("downloads")) + " downloads" + " - " + "Updating Latest");
//                }

				if (cursor.getString(cursor.getColumnIndex("status")).equals(State.FAILED.name())){
					((TextView) view.findViewById(R.id.store_dwn_number)).setText(R.string.loading_failed);
				}

				if (cursor.getString(cursor.getColumnIndex("status")).equals(State.FAILED.name()) || cursor.getString(cursor.getColumnIndex("status")).equals(State.PARSED.name())) {
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
					holder.vername = (TextView) view
							.findViewById(R.id.installed_versionname);
					holder.downloads = (TextView) view
							.findViewById(R.id.downloads);
					holder.rating = (RatingBar) view.findViewById(R.id.stars);
					view.setTag(holder);
				}
				holder.name.setText(cursor.getString(1));
				cm.aptoide.com.nostra13.universalimageloader.core.ImageLoader.getInstance().displayImage(
						cursor.getString(cursor.getColumnIndex("iconspath"))
								+ cursor.getString(cursor
										.getColumnIndex("imagepath")),
						holder.icon,
						(cursor.getString(cursor.getColumnIndex("apkid")) + "|" + cursor
								.getString(cursor.getColumnIndex("vercode"))).hashCode()+"");

				holder.vername.setText(cursor.getString(2));
				try {
					holder.rating.setRating(Float.parseFloat(cursor
							.getString(5)));
				} catch (Exception e) {
					holder.rating.setRating(0);
				}
				holder.downloads.setText(cursor.getString(6));
				break;
			case CATEGORY1:
				((TextView) view.findViewById(R.id.category_name))
						.setText(cursor.getString(1));
				break;
			case CATEGORY2:
				((TextView) view.findViewById(R.id.category_name))
						.setText(cursor.getString(1));
				break;
			case LATEST_LIKES:
				((TextView) view.findViewById(R.id.app_name)).setText(cursor
						.getString(cursor.getColumnIndex("name")));
				((TextView) view.findViewById(R.id.app_name))
						.setCompoundDrawablesWithIntrinsicBounds(0, 0, cursor
								.getString(cursor.getColumnIndex("like"))
								.equals("TRUE") ? R.drawable.up
								: R.drawable.down, 0);
				((TextView) view.findViewById(R.id.user_like)).setText(getString(R.string.by) +" "
						+ cursor.getString(cursor.getColumnIndex("username")));
				break;
			case LATEST_COMMENTS:
				((TextView) view.findViewById(R.id.comment_on_app))
						.setText(getString(R.string.on) +" "
								+ cursor.getString(cursor
										.getColumnIndex("name")));
				((TextView) view.findViewById(R.id.comment)).setText(cursor
						.getString(cursor.getColumnIndex("text")));
				((TextView) view.findViewById(R.id.comment_owner))
						.setText(getString(R.string.by) +": "
								+ cursor.getString(cursor
										.getColumnIndex("username")));
				((TextView) view.findViewById(R.id.time)).setText(cursor
						.getString(cursor.getColumnIndex("time")));
				break;
			default:
				break;
			}
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View v = null;
			switch (depth) {
			case STORES:
				v = LayoutInflater.from(context).inflate(R.layout.row_stores,
						null);
				break;
			case CATEGORY1:
				v = LayoutInflater.from(context).inflate(R.layout.row_catg_list,
						null);
				break;
			case CATEGORY2:
				v = LayoutInflater.from(context).inflate(R.layout.row_catg_list,
						null);
				break;
			case TOPAPPS:
			case LATESTAPPS:
			case ALLAPPLICATIONS:
			case APPLICATIONS:
			case RECOMMENDED:
				v = LayoutInflater.from(context)
						.inflate(R.layout.row_app, null);
				break;
			case LATEST_LIKES:
				v = LayoutInflater.from(context).inflate(
						R.layout.row_latest_likes, null);
				break;
			case LATEST_COMMENTS:
				v = LayoutInflater.from(context).inflate(
						R.layout.row_latest_comments, null);
				break;
			default:
				break;
			}
			Animation animation = AnimationUtils.loadAnimation(mContext,
					android.R.anim.fade_in);
			v.startAnimation(animation);
			return v;
		}
	}

	static class ViewHolder {
		ImageView icon;
		TextView name;
		TextView vername;
		RatingBar rating;
		TextView downloads;
	}

	Editor editor;
	private OnCheckedChangeListener adultCheckedListener = new OnCheckedChangeListener() {

		ProgressDialog pd;

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			if (isChecked) {
				View simpleMessageView = LayoutInflater.from(mContext).inflate(R.layout.dialog_simple_message, null);
				Builder dialogBuilder = new AlertDialog.Builder(mContext).setView(simpleMessageView);
				final AlertDialog alertDialog = dialogBuilder.create();
				alertDialog.setIcon(android.R.drawable.ic_menu_info_details);
				alertDialog.setTitle(getString(R.string.adult_content));
				((TextView) simpleMessageView.findViewById(R.id.dialog_message)).setText(getString(R.string.are_you_adult));
				alertDialog.setButton(Dialog.BUTTON_POSITIVE,
						getString(android.R.string.yes),
						new Dialog.OnClickListener() {

							public void onClick(DialogInterface dialog,
									int which) {
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
				alertDialog.setButton(Dialog.BUTTON_NEGATIVE,
						getString(android.R.string.no),
						new Dialog.OnClickListener() {

							public void onClick(DialogInterface dialog,
									int which) {
								((ToggleButton) featuredView
										.findViewById(R.id.toggleButton1))
										.setChecked(false);
								// if(adult!=null){
								// adult.setChecked(false);
								// }

							}
						});
				alertDialog.show();
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

	protected void generateXML() {
		System.out.println("Generating servers.xml");
		File newxmlfile = new File(Environment.getExternalStorageDirectory()
				+ "/.aptoide/servers.xml");
		try {
			newxmlfile.createNewFile();
		} catch (IOException e) {
			Log.e("IOException", "exception in createNewFile() method");
		}
		// we have to bind the new file with a FileOutputStream
		FileOutputStream fileos = null;
		try {
			fileos = new FileOutputStream(newxmlfile);
		} catch (FileNotFoundException e) {
			Log.e("FileNotFoundException", "can't create FileOutputStream");
		}
		// we create a XmlSerializer in order to write xml data
		XmlSerializer serializer = Xml.newSerializer();
		try {
			// we set the FileOutputStream as output for the serializer, using
			// UTF-8 encoding
			serializer.setOutput(fileos, "UTF-8");
			// Write <?xml declaration with encoding (if encoding not null) and
			// standalone flag (if standalone not null)
			serializer.startDocument(null, Boolean.valueOf(true));
			// set indentation option
			// serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output",
			// true);
			// start a tag called "root"
			serializer.startTag(null, "myapp");
			// i indent code just to have a view similar to xml-tree
			Cursor c = db.getStores(false);
			for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
				serializer.startTag(null, "newserver");
				// serializer.endTag(null, "child1");

				serializer.startTag(null, "server");
				serializer.text(c.getString(1));
				serializer.endTag(null, "server");

				// write some text inside <child3>

				serializer.endTag(null, "newserver");
			}
			c.close();

			serializer.endTag(null, "myapp");
			serializer.endDocument();
			// write xml data into the FileOutputStream
			serializer.flush();
			// finally we close the file stream
			fileos.close();

			// <newserver><server>http://islafenice.bazaarandroid.com/</server></newserver></myapp>

			// TextView tv = (TextView)this.findViewById(R.id.result);
			// tv.setText("file has been created on SD card");
		} catch (Exception e) {
			Log.e("Exception", "error occurred while creating xml file");
		}

	}

	private void getUpdateParameters() {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();

			URLConnection url = new URL(LATEST_VERSION_CODE_URI)
					.openConnection();
			url.setReadTimeout(3000);
			url.setConnectTimeout(3000);

			InputStream a = new BufferedInputStream(url.getInputStream());
			Document dom = builder.parse(new InputSource(a));
			dom.getDocumentElement().normalize();

			NodeList items = dom.getElementsByTagName("versionCode");
			if (items.getLength() > 0) {
				Node item = items.item(0);
				Log.d("Aptoide-XmlElement Name", item.getNodeName());
				Log.d("Aptoide-XmlElement Value", item.getFirstChild()
						.getNodeValue().trim());
				updateParams.put("versionCode", item.getFirstChild()
						.getNodeValue().trim());
			}

			items = dom.getElementsByTagName("uri");
			if (items.getLength() > 0) {
				Node item = items.item(0);
				Log.d("Aptoide-XmlElement Name", item.getNodeName());
				Log.d("Aptoide-XmlElement Value", item.getFirstChild()
						.getNodeValue().trim());
				updateParams.put("uri", item.getFirstChild().getNodeValue()
						.trim());
			}

			items = dom.getElementsByTagName("md5");
			if (items.getLength() > 0) {
				Node item = items.item(0);
				Log.d("Aptoide-XmlElement Name", item.getNodeName());
				Log.d("Aptoide-XmlElement Value", item.getFirstChild()
						.getNodeValue().trim());
				updateParams.put("md5", item.getFirstChild().getNodeValue()
						.trim());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void requestUpdateSelf() {
		View simpleMessageView = LayoutInflater.from(mContext).inflate(R.layout.dialog_simple_message, null);
		Builder dialogBuilder = new AlertDialog.Builder(mContext).setView(simpleMessageView);
		final AlertDialog requestUpdateDialog = dialogBuilder.create();
		requestUpdateDialog.setIcon(R.drawable.ic_launcher);
		requestUpdateDialog.setTitle(getText(R.string.update_self_title));
		requestUpdateDialog.setCancelable(true);
		((TextView) simpleMessageView.findViewById(R.id.dialog_message)).setText(getString(R.string.update_self_msg, ApplicationAptoide.MARKETNAME));
		requestUpdateDialog.setButton(Dialog.BUTTON_POSITIVE, getString(R.string.btn_yes), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
				new DownloadSelfUpdate().execute();
			}
		});
		requestUpdateDialog.setButton(Dialog.BUTTON_NEGATIVE, getString(R.string.btn_cancel), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				return;
			}

		});
		requestUpdateDialog.setCancelable(false);
		requestUpdateDialog.show();

	}

	private class DownloadSelfUpdate extends AsyncTask<Void, Void, Void> {
		private final ProgressDialog dialog = new ProgressDialog(mContext);

		String latestVersionUri;
		String referenceMd5;

		void retrieveUpdateParameters() {
			try {
				latestVersionUri = updateParams.get("uri");
				referenceMd5 = updateParams.get("md5");
			} catch (Exception e) {
				e.printStackTrace();
				Log.d("Aptoide-Auto-Update",
						"Update connection failed!  Keeping current version.");
			}
		}

		@Override
		protected void onPreExecute() {
			this.dialog.setMessage("Retrieving update...");
			this.dialog.show();
			super.onPreExecute();
			retrieveUpdateParameters();
		}

		@Override
		protected Void doInBackground(Void... paramArrayOfParams) {
			try {
				if (latestVersionUri == null) {
					retrieveUpdateParameters();
				}
				// Message msg_al = new Message();
				// If file exists, removes it...
				File f_chk = new File(TMP_UPDATE_FILE);
				if (f_chk.exists()) {
					f_chk.delete();
				}
				f_chk = null;

				FileOutputStream saveit = new FileOutputStream(TMP_UPDATE_FILE);
				DefaultHttpClient mHttpClient = new DefaultHttpClient();
				HttpGet mHttpGet = new HttpGet(latestVersionUri);

				HttpResponse mHttpResponse = mHttpClient.execute(mHttpGet);

				if (mHttpResponse == null) {
					Log.d("Aptoide", "Problem in network... retry...");
					mHttpResponse = mHttpClient.execute(mHttpGet);
					if (mHttpResponse == null) {
						Log.d("Aptoide", "Major network exception... Exiting!");
						/*
						 * msg_al.arg1= 1;
						 * download_error_handler.sendMessage(msg_al);
						 */
						saveit.close();
						throw new TimeoutException();
					}
				}

				if (mHttpResponse.getStatusLine().getStatusCode() == 401) {
					saveit.close();
					throw new TimeoutException();
				} else {
					InputStream getit = mHttpResponse.getEntity().getContent();
					byte data[] = new byte[8096];
					int bytesRead;
					bytesRead = getit.read(data, 0, 8096);
					while (bytesRead != -1) {
						// download_tick.sendEmptyMessage(readed);
						saveit.write(data, 0, bytesRead);
						bytesRead = getit.read(data, 0, 8096);
					}
					Log.d("Aptoide", "Download done!");
					saveit.flush();
					saveit.close();
					getit.close();
				}
			} catch (Exception e) {
				// download_error_handler.sendMessage(msg_al);
				e.printStackTrace();
				Log.d("Aptoide-Auto-Update",
						"Update connection failed!  Keeping current version.");
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {

			if (this.dialog.isShowing()) {
				this.dialog.dismiss();
			}
			super.onPostExecute(result);

			if (!(referenceMd5 == null)) {
				try {
					File apk = new File(TMP_UPDATE_FILE);
					if (referenceMd5.equalsIgnoreCase(Md5Handler.md5Calc(apk))) {
						// msg_al.arg1 = 1;
						// download_handler.sendMessage(msg_al);

						doUpdateSelf();

					} else {
						Log.d("Aptoide",
								referenceMd5 + " VS " + Md5Handler.md5Calc(apk));
						// msg_al.arg1 = 0;
						// download_error_handler.sendMessage(msg_al);doUpdateSelf();

                        throw new Exception(referenceMd5 + " VS "
								+ Md5Handler.md5Calc(apk));
					}
				} catch (Exception e) {
					e.printStackTrace();
					Log.d("Aptoide-Auto-Update",
							"Update package checksum failed!  Keeping current version.");
					if (this.dialog.isShowing()) {
						this.dialog.dismiss();
					}
					finish();
					super.onPostExecute(result);

				}
			}

		}

	}

	private void doUpdateSelf() {
		Intent intent = new Intent();
		intent.setAction(android.content.Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.parse("file://" + TMP_UPDATE_FILE),
				"application/vnd.android.package-archive");

		startActivityForResult(intent, 99);
	}

	public void showFollow(){
		View socialNetworksView = LayoutInflater.from(this).inflate(R.layout.dialog_social_networks, null);
		Builder dialogBuilder = new AlertDialog.Builder(this).setView(socialNetworksView);
		final AlertDialog socialDialog = dialogBuilder.create();
		socialDialog.setIcon(android.R.drawable.ic_menu_share);
		socialDialog.setTitle(getString(R.string.social_networks));
		socialDialog.setCancelable(true);

		Button facebookButton = (Button) socialNetworksView.findViewById(R.id.find_facebook);
		facebookButton.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				if(isAppInstalled("com.facebook.katana")){
					Intent sharingIntent;
					try {
						getPackageManager().getPackageInfo("com.facebook.katana", 0);
						sharingIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("fb://profile/225295240870860"));
						startActivity(sharingIntent);
					} catch (NameNotFoundException e) {
						e.printStackTrace();
					}
				}else{
					Intent intent = new Intent(mContext, WebViewFacebook.class);
					startActivity(intent);
				}

			}
		});

		Button twitterButton = (Button) socialNetworksView.findViewById(R.id.follow_twitter);
		twitterButton.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				if(isAppInstalled("com.twitter.android")){
					String url = "http://www.twitter.com/aptoide";
					Intent twitterIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
					startActivity(twitterIntent);
				}else{
					Intent intent = new Intent(mContext, WebViewTwitter.class);
					startActivity(intent);
				}
			}
		});
		socialDialog.show();
	}

	private boolean isAppInstalled(String uri) {
		PackageManager pm = getPackageManager();
		boolean installed = false;
		try {
			pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
			installed = true;
		} catch (PackageManager.NameNotFoundException e) {
			installed = false;
		}
		return installed;
	}

}
