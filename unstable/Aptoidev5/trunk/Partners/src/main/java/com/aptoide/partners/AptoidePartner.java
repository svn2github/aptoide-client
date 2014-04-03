package com.aptoide.partners;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import cm.aptoide.ptdev.*;
import cm.aptoide.ptdev.AptoideThemePicker;
import cm.aptoide.ptdev.preferences.ManagerPreferences;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.URL;
import java.util.HashMap;

/**
 * Created by tdeus on 12/23/13.
 */
public class AptoidePartner extends Aptoide {



    @Override
    public void bootImpl(ManagerPreferences managerPreferences) {
//        super.bootImpl(managerPreferences);

        SharedPreferences sPref = getContext().getSharedPreferences("aptoide_settings", 0);
        if (sPref.contains("PARTNERID") && sPref.getString("PARTNERID", null) != null) {

            AptoideConfigurationPartners.PARTNERID = sPref.getString("PARTNERID", null);
            AptoideConfigurationPartners.PARTNERTYPE = sPref.getString("PARTNERTYPE", null);
            AptoideConfigurationPartners.DEFAULTSTORENAME = sPref.getString("DEFAULTSTORE", null);
            AptoideConfigurationPartners.MATURECONTENTSWITCH = sPref.getBoolean("MATURECONTENTSWITCH", true);
            AptoideConfigurationPartners.BRAND = sPref.getString("BRAND", "");
            AptoideConfigurationPartners.SPLASHSCREEN = sPref.getString("SPLASHSCREEN", null);
            AptoideConfigurationPartners.SPLASHSCREENLAND = sPref.getString("SPLASHSCREENLAND", null);
            AptoideConfigurationPartners.MATURECONTENTSWITCHVALUE = sPref.getBoolean("MATURECONTENTSWITCHVALUE", true);
            AptoideConfigurationPartners.MULTIPLESTORES = sPref.getBoolean("MULTIPLESTORES", true);
            AptoideConfigurationPartners.CUSTOMEDITORSCHOICE = sPref.getBoolean("CUSTOMEDITORSCHOICE", false);
            AptoideConfigurationPartners.SEARCHSTORES = sPref.getBoolean("SEARCHSTORES", true);
            AptoideConfigurationPartners.APTOIDETHEME = sPref.getString("APTOIDETHEME", "");
            AptoideConfigurationPartners.MARKETNAME = sPref.getString("MARKETNAME", "Aptoide");
            AptoideConfigurationPartners.ADUNITID = sPref.getString("ADUNITID", "18947d9a99e511e295fa123138070049");
            AptoideConfigurationPartners.CREATESHORTCUT = sPref.getBoolean("CREATESHORTCUT", true);
            AptoideConfigurationPartners.ITEMS = sPref.getString("STOREITEMS", "applications,games,top_apps,latest_apps,latest_comments,latest_likes,favorites,recommended");
            AptoideConfigurationPartners.DESCRIPTION = sPref.getString("STOREDESCRIPTION", "");
            AptoideConfigurationPartners.THEME = sPref.getString("STORETHEME", "default");
            AptoideConfigurationPartners.AVATAR = sPref.getString("STOREAVATAR", "https://www.aptoide.com/includes/themes/default/images/repo_default_icon.png");
            AptoideConfigurationPartners.VIEW = sPref.getString("STOREVIEW", "list");

            if (AptoideConfigurationPartners.PARTNERID != null && !new File(AptoideConfigurationPartners.SDCARD + "/.aptoide_settings/oem").exists()) {
                AptoideConfigurationPartners.createSdCardBinary();
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (isUpdate()) {
                            BufferedInputStream is = new BufferedInputStream(new URL("http://" + AptoideConfigurationPartners.DEFAULTSTORENAME + ".store.aptoide.com/boot_config.xml").openStream());
                            AptoideConfigurationPartners.parseBootConfigStream(is);
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


        } else if (new File(AptoideConfigurationPartners.SDCARD + "/.aptoide_settings/oem").exists() && !getPackageName().equals("cm.aptoide.pt")) {

            try {
                Log.d("AptoidePartner", "Regenerating settings from SDCard.");
                HashMap<String, String> map = (HashMap<String, String>) new ObjectInputStream(new FileInputStream(new File(AptoideConfigurationPartners.SDCARD + "/.aptoide_settings/oem"))).readObject();

                AptoideConfigurationPartners.PARTNERID = map.get("PARTNERID");
                AptoideConfigurationPartners.PARTNERTYPE = map.get("PARTNERTYPE");
                AptoideConfigurationPartners.DEFAULTSTORENAME = map.get("DEFAULTSTORE");
                AptoideConfigurationPartners.MATURECONTENTSWITCH = Boolean.parseBoolean(map.get("MATURECONTENTSWITCH"));
                AptoideConfigurationPartners.BRAND = map.get("BRAND");
                AptoideConfigurationPartners.SPLASHSCREEN = map.get("SPLASHSCREEN");
                AptoideConfigurationPartners.SPLASHSCREENLAND = map.get("SPLASHSCREENLAND");
                AptoideConfigurationPartners.MATURECONTENTSWITCHVALUE = Boolean.parseBoolean(map.get("MATURECONTENTSWITCHVALUE"));
                AptoideConfigurationPartners.MULTIPLESTORES = Boolean.parseBoolean(map.get("MULTIPLESTORES"));
                AptoideConfigurationPartners.CUSTOMEDITORSCHOICE = Boolean.parseBoolean(map.get("CUSTOMEDITORSCHOICE"));
                AptoideConfigurationPartners.SEARCHSTORES = Boolean.parseBoolean(map.get("SEARCHSTORES"));
                AptoideConfigurationPartners.APTOIDETHEME = map.get("APTOIDETHEME");
                AptoideConfigurationPartners.MARKETNAME = map.get("MARKETNAME");
                AptoideConfigurationPartners.ADUNITID = map.get("ADUNITID");
                AptoideConfigurationPartners.CREATESHORTCUT = Boolean.parseBoolean(map.get("CREATESHORTCUT"));
                AptoideConfigurationPartners.ITEMS = map.get("STOREITEMS");
                AptoideConfigurationPartners.DESCRIPTION = map.get("STOREDESCRIPTION");
                AptoideConfigurationPartners.THEME = map.get("STORETHEME");
                AptoideConfigurationPartners.AVATAR = map.get("STOREAVATAR");
                AptoideConfigurationPartners.VIEW = map.get("STOREVIEW");

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (isUpdate()) {
                                BufferedInputStream is = new BufferedInputStream(new URL("http://" + AptoideConfigurationPartners.DEFAULTSTORENAME + ".store.aptoide.com/boot_config.xml").openStream());
                                AptoideConfigurationPartners.parseBootConfigStream(is);
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


                AptoideConfigurationPartners.savePreferences();


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
                AptoideConfigurationPartners.parseBootConfigStream(inputStream);

            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            }

        }


        PreferenceManager.getDefaultSharedPreferences(Aptoide.getContext()).edit().putBoolean("matureChkBox", ((AptoideConfigurationPartners)AptoidePartner.getConfiguration()).getMatureContentSwitchValue()).commit();

    }


    @Override
    public AptoideConfigurationPartners getAptoideConfiguration() {
        return new AptoideConfigurationPartners();
    }

    @Override
    public AptoideThemePicker getNewThemePicker() {
        return new com.aptoide.partners.AptoideThemePicker();
    }
}
