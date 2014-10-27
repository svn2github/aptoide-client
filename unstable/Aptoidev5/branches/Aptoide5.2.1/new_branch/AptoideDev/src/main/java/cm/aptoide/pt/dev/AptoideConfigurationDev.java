package cm.aptoide.pt.dev;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Locale;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import cm.aptoide.ptdev.configuration.AptoideConfiguration;
import cm.aptoide.ptdev.preferences.SecurePreferences;

/**
 * Created by tdeus on 12/23/13.
 */
public class AptoideConfigurationDev extends AptoideConfiguration {

    public static String PARTNERTYPE;
    public static String PARTNERID = "";
    public static String DEFAULTSTORENAME;
    public static boolean MATURECONTENTSWITCH = true;
    public static String SPLASHSCREEN;
    public static String SPLASHSCREENLAND;
    public static String BRAND = "";
    public static boolean MATURECONTENTSWITCHVALUE = true;
    public static boolean MULTIPLESTORES = true;
    public static boolean CUSTOMEDITORSCHOICE = false;
    public static boolean SEARCHSTORES = true;
    public static String APTOIDETHEME = "";
    public static String MARKETNAME = "AptoideDev";
    public static String ADUNITID = "";
    public static boolean CREATESHORTCUT = true;


    public static String THEME = null;
    public static String AVATAR = null;
    public static String DESCRIPTION = null;
    public static String VIEW = "list";
    public static String ITEMS = null;
    public static String SDCARD = Environment.getExternalStorageDirectory().getAbsolutePath();
    public static final String OEM_AUTO_UPDATE_URL = "http://%s.aptoide.com/latest_version_%s.xml";

    public static String RESTRICTIONLIST;

    public String getFallbackEditorsChoiceUrl() {
        return "http://"+DEFAULTSTORENAME+".store.aptoide.com/editors.xml";
    }

    static enum Elements { BOOTCONF, APTOIDECONF, PARTNERTYPE, PARTNERID, DEFAULTSTORENAME, BRAND, SPLASHSCREEN, MATURECONTENTSWITCH, MATURECONTENTSWITCHVALUE,SEARCHSTORES, MULTIPLESTORES, CUSTOMEDITORSCHOICE, APTOIDETHEME, SPLASHSCREENLAND, MARKETNAME, ADUNITID, CREATESHORTCUT,
        STORECONF, THEME, AVATAR, DESCRIPTION, VIEW, ITEMS, RESTRICTIONLIST
    }

    private static Context context = AptoideDev.getContext();

    @Override
    public String getAutoUpdateUrl(){
        return String.format(OEM_AUTO_UPDATE_URL, DEFAULTSTORENAME, DEFAULTSTORENAME);
    }


    public String getDefaultTopAppsUrl(){
        return super.getTopAppsUrl();
    }


    public String getDefaultEditorsUrl(){
        return super.getEditorsUrl();
    }

    @Override
    public String getTopAppsUrl(){

        if(CUSTOMEDITORSCHOICE){
            return "http://" + DEFAULTSTORENAME + ".store.aptoide.com/top.xml";
        }

        return super.getTopAppsUrl();
    }


    @Override
    public String getEditorsUrl(){

        if(CUSTOMEDITORSCHOICE){
            return "http://" + DEFAULTSTORENAME + ".store.aptoide.com/editors_more.xml";
        }

        return super.getEditorsUrl();
    }

    public String getPartnerType() { return PARTNERTYPE; }
    public static void setPartnerType(String partnerType){ AptoideConfigurationDev.PARTNERTYPE = partnerType; }

    public static String getPartnerId() { return PARTNERID; }
    public static void setPartnerId(String partnerId){ AptoideConfigurationDev.PARTNERID = partnerId; }

    @Override
    public String getDefaultStore() { return DEFAULTSTORENAME; }
    public static void setDefaultStoreName(String defaultStoreName){ AptoideConfigurationDev.DEFAULTSTORENAME = defaultStoreName; }

    public boolean getMatureContentSwitch() { return MATURECONTENTSWITCH; }
    public static void setMatureContentSwitch(boolean matureContentSwitch) { AptoideConfigurationDev.MATURECONTENTSWITCH = matureContentSwitch; }

    public String getSplashscreen(){ return SPLASHSCREEN; }
    public static void setSplashscreen(String splashscreen){ AptoideConfigurationDev.SPLASHSCREEN = splashscreen; }

    public String getSplashscreenLand(){ return SPLASHSCREENLAND; }
    public static void setSplashscreenLand(String splashscreenLand){ AptoideConfigurationDev.SPLASHSCREENLAND = splashscreenLand; }

    public String getBrand(){ return BRAND; }
    public static void setBrand(String stringBrand){ AptoideConfigurationDev.BRAND = stringBrand; }

    public boolean getMatureContentSwitchValue() { return MATURECONTENTSWITCHVALUE; }
    public void setMatureContentSwitchValue(boolean matureContentSwitchValue) { AptoideConfigurationDev.MATURECONTENTSWITCHVALUE = matureContentSwitchValue; }

    public boolean getMultistores() { return MULTIPLESTORES; }
    public static void setMultistores(boolean multistores) { AptoideConfigurationDev.MULTIPLESTORES = multistores; }

    public boolean getCustomEditorsChoice() { return CUSTOMEDITORSCHOICE; }
    public static void setCustomEditorsChoice(boolean customEditorsChoice) { AptoideConfigurationDev.CUSTOMEDITORSCHOICE = customEditorsChoice; }

    public boolean getSearchStores() { return SEARCHSTORES; }
    public static void setSearchStores(boolean searchStores) { AptoideConfigurationDev.SEARCHSTORES = searchStores; }

    public String getTheme() { return APTOIDETHEME.toUpperCase(Locale.ENGLISH); }
    public static void setTheme(String appTheme) { AptoideConfigurationDev.APTOIDETHEME = appTheme; }

    public String getMarketName(){ return MARKETNAME; }
    public static void setMarketName(String marketName){ AptoideConfigurationDev.MARKETNAME = marketName; }

    public String getAdUnitId(){ return ADUNITID; }
    public static void setAdUnitId(String adUnitId){ AptoideConfigurationDev.ADUNITID = adUnitId; }

    @Override
    public String getExtraId(){
        return PARTNERID;
    }

    public boolean getCreateShortcut() { return CREATESHORTCUT; }
    public static void setCreateShortcut(boolean createShortcut) { AptoideConfigurationDev.CREATESHORTCUT = createShortcut; }

    public String getStoreAvatar(){ return AVATAR; }
    public static void setStoreAvatar(String avatar){ AptoideConfigurationDev.AVATAR = avatar; }

    public String getStoreTheme(){ return THEME; }
    public static void setStoreTheme(String theme){ AptoideConfigurationDev.THEME = theme; }

    public String getStoreDescription(){ return DESCRIPTION; }
    public static void setDescription(String description){ AptoideConfigurationDev.DESCRIPTION = description; }

    public String getStoreView(){ return VIEW; }
    public static void setView(String view){ AptoideConfigurationDev.VIEW = view; }

    @Override
    public boolean isSaveOldRepos(){
        return false;
    }

    public String getStoreItems(){ return ITEMS; }
    public static void setItems(String items){ AptoideConfigurationDev.ITEMS = items; }

    public static String getRestrictionlist() {
        return RESTRICTIONLIST;
    }


    public static void parseBootConfigStream(InputStream inputStream) throws ParserConfigurationException, SAXException, IOException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = factory.newSAXParser();


        parser.parse(inputStream,new DefaultHandler(){
            StringBuilder sb = new StringBuilder();

            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                super.startElement(uri, localName, qName, attributes);
                sb.setLength(0);
            }


            @Override
            public void characters(char[] ch, int start, int length) throws SAXException {
                super.characters(ch, start, length);    //To change body of overridden methods use File | Settings | File Templates.
                sb.append(ch,start,length);
            }

            @Override
            public void endElement(String uri, String localName, String qName) throws SAXException {
                super.endElement(uri, localName, qName);
                try{
                    Elements element = Elements.valueOf(localName.toUpperCase(Locale.ENGLISH));
                    switch (element) {
                        case PARTNERTYPE:
                            PARTNERTYPE = sb.toString();
                            Log.d("Partner type", PARTNERTYPE + "");
                            break;
                        case PARTNERID:
                            PARTNERID = sb.toString();
                            Log.d("Partner ID", PARTNERID + "");
                            break;
                        case DEFAULTSTORENAME:
                            DEFAULTSTORENAME = sb.toString();
                            Log.d("Default store", DEFAULTSTORENAME + "");
                            break;
                        case BRAND:
                            BRAND = sb.toString();
                            Log.d("Brand", BRAND+ "");
                            break;
                        case SPLASHSCREEN:
                            SPLASHSCREEN = sb.toString();
                            Log.d("Splashscreen", SPLASHSCREEN+ "");
                            break;
                        case SPLASHSCREENLAND:
                            SPLASHSCREENLAND = sb.toString();
                            Log.d("Splashscreen landscape", SPLASHSCREENLAND+ "");
                            break;
                        case MATURECONTENTSWITCH:
                            MATURECONTENTSWITCH = Boolean.parseBoolean(sb.toString());
                            Log.d("Mature content Switch", MATURECONTENTSWITCH + "");
                            break;
                        case MATURECONTENTSWITCHVALUE:
                            MATURECONTENTSWITCHVALUE = Boolean.parseBoolean(sb.toString());
                            Log.d("Mature content value", MATURECONTENTSWITCHVALUE+ "");
                            break;
                        case MULTIPLESTORES:
                            MULTIPLESTORES = Boolean.parseBoolean(sb.toString());
                            Log.d("Multiple stores", MULTIPLESTORES+ "");
                            break;
                        case CUSTOMEDITORSCHOICE:
                            CUSTOMEDITORSCHOICE = Boolean.parseBoolean(sb.toString());
                            Log.d("Custom editors choice", CUSTOMEDITORSCHOICE+ "");
                            break;
                        case APTOIDETHEME:
                            APTOIDETHEME = sb.toString();
                            Log.d("APTOIDETHEME", APTOIDETHEME+ "");
                            break;
                        case MARKETNAME:
                            MARKETNAME = sb.toString();
                            Log.d("Market name", MARKETNAME+ "");
                            break;
                        case SEARCHSTORES:
                            SEARCHSTORES = Boolean.parseBoolean(sb.toString());
                            Log.d("Search stores", SEARCHSTORES+ "");
                            break;
                        case ADUNITID:
                            ADUNITID = sb.toString();
                            Log.d("AdUnitId", ADUNITID+ "");
                            break;
                        case CREATESHORTCUT:
                            CREATESHORTCUT = Boolean.parseBoolean(sb.toString());
                            Log.d("Create Shortcut", CREATESHORTCUT+ "");
                            break;

                        case THEME:
                            THEME = sb.toString();
                            Log.d("Store Theme", THEME+ "");
                            break;
                        case AVATAR:
                            AVATAR = sb.toString();
                            Log.d("Store avatar", AVATAR+ "");
                            break;
                        case DESCRIPTION:
                            DESCRIPTION = sb.toString();
                            Log.d("Store description", DESCRIPTION+ "");
                            break;
                        case ITEMS:
                            ITEMS = sb.toString();
                            Log.d("Store items", ITEMS+ "");
                            break;
                        case VIEW:
                            VIEW = sb.toString();
                            Log.d("Store view", VIEW+ "");
                            break;
                        case RESTRICTIONLIST:
                            RESTRICTIONLIST = sb.toString();
                            Log.d("Restriction list", RESTRICTIONLIST + "");
                            break;

                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        savePreferences();

        createSdCardBinary();
    }

    public static void createSdCardBinary() {
        if (PARTNERID != null) {

            HashMap<String, String> map = new HashMap<String, String>();

            map.put("PARTNERID", PARTNERID);
            map.put("PARTNERTYPE", PARTNERTYPE);
            map.put("DEFAULTSTORE", DEFAULTSTORENAME);
            map.put("MATURECONTENTSWITCH", MATURECONTENTSWITCH + "");
            map.put("BRAND", BRAND);
            map.put("SPLASHSCREENLAND", SPLASHSCREENLAND);
            map.put("SPLASHSCREEN", SPLASHSCREEN);
            map.put("MATURECONTENTSWITCHVALUE", MATURECONTENTSWITCHVALUE + "");
            map.put("MULTIPLESTORES", MULTIPLESTORES + "");
            map.put("CUSTOMEDITORSCHOICE", CUSTOMEDITORSCHOICE + "");
            map.put("SEARCHSTORES", SEARCHSTORES + "");
            map.put("APTOIDETHEME", APTOIDETHEME);
            map.put("MARKETNAME", MARKETNAME);
            map.put("ADUNITID", ADUNITID);
            map.put("CREATESHORTCUT", CREATESHORTCUT + "");
            map.put("STOREDESCRIPTION", DESCRIPTION);
            map.put("STORETHEME", THEME);
            map.put("STOREAVATAR", AVATAR);
            map.put("STOREITEMS", ITEMS);
            map.put("STOREVIEW", VIEW);

            try {
                File fileDir = new File(SDCARD + "/.aptoide_settings");
                if (fileDir.mkdir()) {
                    ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(SDCARD + "/.aptoide_settings/oem")));
                    oos.writeObject(map);
                    oos.close();

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void savePreferences() {

        SharedPreferences sPref = context.getSharedPreferences("aptoide_settings", 0);

        sPref.edit().putString("PARTNERID", PARTNERID)
                .putString("DEFAULTSTORE", DEFAULTSTORENAME)
                .putString("PARTNERTYPE", PARTNERTYPE)
                .putBoolean("MATURECONTENTSWITCH", MATURECONTENTSWITCH)
                .putString("BRAND", BRAND)
                .putString("SPLASHSCREENLAND", SPLASHSCREENLAND)
                .putString("SPLASHSCREEN", SPLASHSCREEN)
                .putBoolean("MATURECONTENTSWITCHVALUE", MATURECONTENTSWITCHVALUE)
                .putBoolean("MULTIPLESTORES", MULTIPLESTORES)
                .putBoolean("CUSTOMEDITORSCHOICE", CUSTOMEDITORSCHOICE)
                .putBoolean("SEARCHSTORES", SEARCHSTORES)
                .putString("APTOIDETHEME", APTOIDETHEME)
                .putString("MARKETNAME", MARKETNAME)
                .putString("ADUNITID", ADUNITID)
                .putBoolean("CREATESHORTCUT", CREATESHORTCUT)
                .putString("STOREDESCRIPTION", DESCRIPTION)
                .putString("STOREAVATAR", AVATAR)
                .putString("STORETHEME", THEME)
                .putString("STOREITEMS", ITEMS)
                .putString("STOREVIEW", VIEW)
                .commit();

        if(RESTRICTIONLIST != null && !RESTRICTIONLIST.equals("")) {
            SharedPreferences ssPref = SecurePreferences.getInstance();
            if (ssPref.contains("RESTRICTIONLIST")) {
                RESTRICTIONLIST += ", " + ssPref.getString("RESTRICTIONLIST", "");
            }
            Log.d("Restriction List", "Restriction List saved: " + RESTRICTIONLIST);
            ssPref.edit().putString("RESTRICTIONLIST", RESTRICTIONLIST).commit();
        }


    }

    @Override
    public Class getStartActivityClass(){
        return super.getStartActivityClass();
    }

    @Override
    public Class getAppViewActivityClass() {
        return super.getAppViewActivityClass();
    }

    @Override
    public Class getSettingsActivityClass() {
        return super.getSettingsActivityClass();
    }

    @Override
    public Class getSignUpActivityClass() {
        return super.getSignUpActivityClass();
    }

    @Override
    public String getUpdatesSyncAdapterAuthority(){
        return "cm.aptoide.pt.dev.UpdatesProvider";
    }

    @Override
    public String getAutoUpdatesSyncAdapterAuthority(){
        return "cm.aptoide.pt.dev.AutoUpdateProvider";
    }

    @Override
    public String getTimelineActivitySyncAdapterAuthority(){
        return "cm.aptoide.pt.dev.TimelineActivity";
    }

    @Override
    public String getTimeLinePostsSyncAdapterAuthority(){
        return "cm.aptoide.pt.dev.TimelinePosts";
    }

    @Override
    public String getSearchAuthority() {
        return "cm.aptoide.pt.dev.SuggestionProvider";
    }

    @Override
    public String getAccountType() { return AccountGeneralDev.ACCOUNT_TYPE;
    };

    @Override
    public Class<?> getSearchActivityClass() {
        return super.getSearchActivityClass();
    }
}
