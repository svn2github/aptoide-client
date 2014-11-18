package cm.aptoidetv.pt;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.util.Log;

import org.xml.sax.SAXException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URL;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;

import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.AptoideThemePicker;
import cm.aptoide.ptdev.preferences.ManagerPreferences;
import cm.aptoide.ptdev.preferences.SecurePreferences;

/**
 * Created by tdeus on 12/23/13.
 */
public class AptoideTV extends Aptoide {

    @Override
    public void bootImpl(ManagerPreferences managerPreferences) {
        SharedPreferences sPref = getContext().getSharedPreferences("aptoide_settings", 0);
        if (sPref.contains("PARTNERID") && sPref.getString("PARTNERID", null) != null) {

            AptoideConfigurationTV.PARTNERID = sPref.getString("PARTNERID", null);
            AptoideConfigurationTV.PARTNERTYPE = sPref.getString("PARTNERTYPE", null);
            AptoideConfigurationTV.DEFAULTSTORENAME = sPref.getString("DEFAULTSTORE", null);
            AptoideConfigurationTV.MATURECONTENTSWITCH = sPref.getBoolean("MATURECONTENTSWITCH", true);
            AptoideConfigurationTV.BRAND = sPref.getString("BRAND", "");
            AptoideConfigurationTV.SPLASHSCREEN = sPref.getString("SPLASHSCREEN", null);
            AptoideConfigurationTV.SPLASHSCREENLAND = sPref.getString("SPLASHSCREENLAND", null);
            AptoideConfigurationTV.MATURECONTENTSWITCHVALUE = sPref.getBoolean("MATURECONTENTSWITCHVALUE", true);
            AptoideConfigurationTV.MULTIPLESTORES = sPref.getBoolean("MULTIPLESTORES", true);
            AptoideConfigurationTV.CUSTOMEDITORSCHOICE = sPref.getBoolean("CUSTOMEDITORSCHOICE", false);
            AptoideConfigurationTV.SEARCHSTORES = sPref.getBoolean("SEARCHSTORES", true);
            AptoideConfigurationTV.APTOIDETHEME = sPref.getString("APTOIDETHEME", "");
            AptoideConfigurationTV.MARKETNAME = sPref.getString("MARKETNAME", "Aptoide");
            AptoideConfigurationTV.ADUNITID = sPref.getString("ADUNITID", "18947d9a99e511e295fa123138070049");
            AptoideConfigurationTV.CREATESHORTCUT = sPref.getBoolean("CREATESHORTCUT", true);
            AptoideConfigurationTV.SPLASHCOLOR = sPref.getString("SPLASHCOLOR", "");
            AptoideConfigurationTV.SHOWSPLASH = sPref.getBoolean("SHOWSPLASH", true);
            AptoideConfigurationTV.ITEMS = sPref.getString("STOREITEMS", "applications,games,top_apps,latest_apps,latest_comments,latest_likes,favorites,recommended");
            AptoideConfigurationTV.DESCRIPTION = sPref.getString("STOREDESCRIPTION", "");
            AptoideConfigurationTV.THEME = sPref.getString("STORETHEME", "default");
            AptoideConfigurationTV.AVATAR = sPref.getString("STOREAVATAR", "https://www.aptoide.com/includes/themes/default/images/repo_default_icon.png");
            AptoideConfigurationTV.VIEW = sPref.getString("STOREVIEW", "list");

            AptoideConfigurationTV.RESTRICTIONLIST = SecurePreferences.getInstance().getString("RESTRICTIONLIST", null);


            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (isUpdate()) {
                            BufferedInputStream is = new BufferedInputStream(new URL("http://" + AptoideConfigurationTV.DEFAULTSTORENAME + ".store.aptoide.com/boot_config.xml").openStream());
                            AptoideConfigurationTV.parseBootConfigStream(is);
                        }
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ParserConfigurationException e) {
                        e.printStackTrace();
                    } catch (SAXException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();


        } else if (new File(AptoideConfigurationTV.SDCARD + "/.aptoide_settings/oem").exists() && !getPackageName().equals("cm.aptoide.pt")) {

            try {
                Log.d("AptoideTV", "Regenerating settings from SDCard.");
                HashMap<String, String> map = (HashMap<String, String>) new ObjectInputStream(new FileInputStream(new File(AptoideConfigurationTV.SDCARD + "/.aptoide_settings/oem"))).readObject();

                AptoideConfigurationTV.PARTNERID = map.get("PARTNERID");
                AptoideConfigurationTV.PARTNERTYPE = map.get("PARTNERTYPE");
                AptoideConfigurationTV.DEFAULTSTORENAME = map.get("DEFAULTSTORE");
                AptoideConfigurationTV.MATURECONTENTSWITCH = Boolean.parseBoolean(map.get("MATURECONTENTSWITCH"));
                AptoideConfigurationTV.BRAND = map.get("BRAND");
                AptoideConfigurationTV.SPLASHSCREEN = map.get("SPLASHSCREEN");
                AptoideConfigurationTV.SPLASHSCREENLAND = map.get("SPLASHSCREENLAND");
                AptoideConfigurationTV.MATURECONTENTSWITCHVALUE = Boolean.parseBoolean(map.get("MATURECONTENTSWITCHVALUE"));
                AptoideConfigurationTV.MULTIPLESTORES = Boolean.parseBoolean(map.get("MULTIPLESTORES"));
                AptoideConfigurationTV.CUSTOMEDITORSCHOICE = Boolean.parseBoolean(map.get("CUSTOMEDITORSCHOICE"));
                AptoideConfigurationTV.SEARCHSTORES = Boolean.parseBoolean(map.get("SEARCHSTORES"));
                AptoideConfigurationTV.APTOIDETHEME = map.get("APTOIDETHEME");
                AptoideConfigurationTV.MARKETNAME = map.get("MARKETNAME");
                AptoideConfigurationTV.ADUNITID = map.get("ADUNITID");
                AptoideConfigurationTV.CREATESHORTCUT = Boolean.parseBoolean(map.get("CREATESHORTCUT"));
                AptoideConfigurationTV.SPLASHCOLOR = map.get("SPLASHCOLOR");
                AptoideConfigurationTV.SHOWSPLASH = Boolean.parseBoolean(map.get("SHOWSPLASH"));
                AptoideConfigurationTV.ITEMS = map.get("STOREITEMS");
                AptoideConfigurationTV.DESCRIPTION = map.get("STOREDESCRIPTION");
                AptoideConfigurationTV.THEME = map.get("STORETHEME");
                AptoideConfigurationTV.AVATAR = map.get("STOREAVATAR");
                AptoideConfigurationTV.VIEW = map.get("STOREVIEW");

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (isUpdate()) {
                                BufferedInputStream is = new BufferedInputStream(new URL("http://" + AptoideConfigurationTV.DEFAULTSTORENAME + ".store.aptoide.com/boot_config.xml").openStream());
                                AptoideConfigurationTV.parseBootConfigStream(is);
                            }
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (ParserConfigurationException e) {
                            e.printStackTrace();
                        } catch (SAXException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();


                AptoideConfigurationTV.savePreferences();


            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();

            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            try {
                InputStream inputStream = getContext().getAssets().open("boot_config.xml");
                AptoideConfigurationTV.parseBootConfigStream(inputStream);

            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            }

        }
        
        if (managerPreferences.getAptoideClientUUID() == null) {
            managerPreferences.createLauncherShortcut(getContext(), R.drawable.ic_launcher);
        }

        PreferenceManager.getDefaultSharedPreferences(AptoideTV.getContext()).edit().putBoolean("matureChkBox", ((AptoideConfigurationTV)AptoideTV.getConfiguration()).getMatureContentSwitchValue()).commit();

    }

    @Override
    public AptoideConfigurationTV getAptoideConfiguration() {
        return new AptoideConfigurationTV();
    }

    @Override
    public AptoideThemePicker getNewThemePicker() {
        return new cm.aptoidetv.pt.AptoideThemePicker();
    }

}
