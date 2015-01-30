package com.aptoide.partners;

import android.content.Context;
import android.content.res.Resources;
import android.os.Environment;

import com.aptoide.openiab.IABPurchaseActivityPartners;
import com.aptoide.openiab.PaidAppPurchaseActivityPartners;

import java.util.Locale;

import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.configuration.AptoideConfiguration;

/**
 * Created by tdeus on 12/23/13.
 */
public class AptoideConfigurationPartners extends AptoideConfiguration {
    private static Context context = AptoidePartner.getContext();


    private static Resources resources = context.getResources();

    public static String PARTNERTYPE = resources.getString(R.string.partnertype);
    public static String PARTNERID = resources.getString(R.string.partnerid);
    public static String DEFAULTSTORENAME = resources.getString(R.string.defaultstorename);
    public static boolean MATURECONTENTSWITCH = resources.getBoolean(R.bool.maturecontentswitch);
    public static String SPLASHSCREEN = resources.getString(R.string.splashscreen);
    public static String SPLASHSCREENLAND = resources.getString(R.string.splashscreenland);
    public static String BRAND = "";
    public static boolean MATURECONTENTSWITCHVALUE = resources.getBoolean(R.bool.maturecontentswitchvalue);
    public static boolean MULTIPLESTORES = resources.getBoolean(R.bool.multiplestores);
    public static boolean CUSTOMEDITORSCHOICE = resources.getBoolean(R.bool.customeditorschoice);
    public static boolean SEARCHSTORES = resources.getBoolean(R.bool.searchstores);
    public static String APTOIDETHEME = resources.getString(R.string.aptoidetheme);
    public static String MARKETNAME = resources.getString(R.string.marketname);
    public static String ADUNITID = resources.getString(R.string.adunitid);
    public static boolean CREATESHORTCUT = resources.getBoolean(R.bool.createshortcut);


    public static String THEME = resources.getString(R.string.theme);
    public static String AVATAR = resources.getString(R.string.avatar);
    public static String DESCRIPTION = resources.getString(R.string.description);
    public static String VIEW = resources.getString(R.string.view);
    public static String ITEMS = resources.getString(R.string.items);
    public static String SDCARD = Environment.getExternalStorageDirectory().getAbsolutePath();
    public static final String OEM_AUTO_UPDATE_URL = "http://%s.aptoide.com/latest_version_%s.xml";

    public static String RESTRICTIONLIST = resources.getString(R.string.restritionlist);;
    public static String SPLASHCOLOR =  resources.getString(R.string.splashcolor);
    public static boolean SHOWSPLASH = resources.getBoolean(R.bool.showsplash);
    public static boolean SHOWADS = resources.getBoolean(R.bool.showads);
    public static boolean TIMELINE= resources.getBoolean(R.bool.timeline);

    public String getFallbackEditorsChoiceUrl() {
        return "http://"+DEFAULTSTORENAME+".store.aptoide.com/editors.xml";
    }

    static enum Elements { BOOTCONF, APTOIDECONF, PARTNERTYPE, PARTNERID, DEFAULTSTORENAME, BRAND, SPLASHSCREEN, MATURECONTENTSWITCH, MATURECONTENTSWITCHVALUE,SEARCHSTORES, MULTIPLESTORES, CUSTOMEDITORSCHOICE, APTOIDETHEME, SPLASHSCREENLAND, MARKETNAME, ADUNITID, CREATESHORTCUT, SPLASHCOLOR, SHOWSPLASH, SHOWADS, TIMELINE,
        STORECONF, THEME, AVATAR, DESCRIPTION, VIEW, ITEMS, RESTRICTIONLIST
    }


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
    public static void setPartnerType(String partnerType){ AptoideConfigurationPartners.PARTNERTYPE = partnerType; }

    public static String getPartnerId() { return PARTNERID; }
    public static void setPartnerId(String partnerId){ AptoideConfigurationPartners.PARTNERID = partnerId; }

    @Override
    public String getDefaultStore() { return DEFAULTSTORENAME; }
    public static void setDefaultStoreName(String defaultStoreName){ AptoideConfigurationPartners.DEFAULTSTORENAME = defaultStoreName; }

    public boolean getMatureContentSwitch() { return MATURECONTENTSWITCH; }
    public static void setMatureContentSwitch(boolean matureContentSwitch) { AptoideConfigurationPartners.MATURECONTENTSWITCH = matureContentSwitch; }

    public String getSplashscreen(){ return SPLASHSCREEN; }
    public static void setSplashscreen(String splashscreen){ AptoideConfigurationPartners.SPLASHSCREEN = splashscreen; }

    public String getSplashscreenLand(){ return SPLASHSCREENLAND; }
    public static void setSplashscreenLand(String splashscreenLand){ AptoideConfigurationPartners.SPLASHSCREENLAND = splashscreenLand; }

    public String getBrand(){ return BRAND; }
    public static void setBrand(String stringBrand){ AptoideConfigurationPartners.BRAND = stringBrand; }

    public boolean getMatureContentSwitchValue() { return MATURECONTENTSWITCHVALUE; }
    public void setMatureContentSwitchValue(boolean matureContentSwitchValue) { AptoideConfigurationPartners.MATURECONTENTSWITCHVALUE = matureContentSwitchValue; }

    public boolean getMultistores() { return MULTIPLESTORES; }
    public static void setMultistores(boolean multistores) { AptoideConfigurationPartners.MULTIPLESTORES = multistores; }

    public boolean getCustomEditorsChoice() { return CUSTOMEDITORSCHOICE; }
    public static void setCustomEditorsChoice(boolean customEditorsChoice) { AptoideConfigurationPartners.CUSTOMEDITORSCHOICE = customEditorsChoice; }

    public boolean getSearchStores() { return SEARCHSTORES; }
    public static void setSearchStores(boolean searchStores) { AptoideConfigurationPartners.SEARCHSTORES = searchStores; }

    public String getTheme() { return APTOIDETHEME.toUpperCase(Locale.ENGLISH); }
    public static void setTheme(String appTheme) { AptoideConfigurationPartners.APTOIDETHEME = appTheme; }

    public String getMarketName(){ return MARKETNAME; }
    public static void setMarketName(String marketName){ AptoideConfigurationPartners.MARKETNAME = marketName; }

    public String getAdUnitId(){ return ADUNITID; }
    public static void setAdUnitId(String adUnitId){ AptoideConfigurationPartners.ADUNITID = adUnitId; }

    public String getSplashColor(){ return SPLASHCOLOR; }
    public static void setSplashColor(String color){ AptoideConfigurationPartners.SPLASHCOLOR = color; }

    public boolean getShowSplash() { return SHOWSPLASH; }
    public static void setShowSplash(boolean showSplash) { AptoideConfigurationPartners.SHOWSPLASH = showSplash; }

    public boolean getShowAds() { return SHOWADS; }
    public static void setShowAds(boolean showAds) { AptoideConfigurationPartners.SHOWADS = showAds; }

    public boolean getShowTimeline() { return TIMELINE; }
    public static void setShowTimeline(boolean showTimeline) { AptoideConfigurationPartners.TIMELINE = showTimeline; }

    @Override
    public String getExtraId(){
        return PARTNERID;
    }

    public boolean getCreateShortcut() { return CREATESHORTCUT; }
    public static void setCreateShortcut(boolean createShortcut) { AptoideConfigurationPartners.CREATESHORTCUT = createShortcut; }

    public String getStoreAvatar(){ return AVATAR; }
    public static void setStoreAvatar(String avatar){ AptoideConfigurationPartners.AVATAR = avatar; }

    public String getStoreTheme(){ return THEME; }
    public static void setStoreTheme(String theme){ AptoideConfigurationPartners.THEME = theme; }

    public String getStoreDescription(){ return DESCRIPTION; }
    public static void setDescription(String description){ AptoideConfigurationPartners.DESCRIPTION = description; }

    public String getStoreView(){ return VIEW; }
    public static void setView(String view){ AptoideConfigurationPartners.VIEW = view; }

    @Override
    public boolean isSaveOldRepos(){
        return false;
    }

    public String getStoreItems(){ return ITEMS; }
    public static void setItems(String items){ AptoideConfigurationPartners.ITEMS = items; }

    public static String getRestrictionlist() {
        return RESTRICTIONLIST;
    }

/*
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
                        case BOOTCONF:
                            break;
                        case APTOIDECONF:
                            break;
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
                        case SPLASHCOLOR:
                            SPLASHCOLOR = sb.toString();
                            Log.d("Splash color", SPLASHCOLOR + "");
                            break;
                        case SHOWSPLASH:
                            SHOWSPLASH = Boolean.parseBoolean(sb.toString());
                            Log.d("Show splashscreen", SHOWSPLASH+ "");
                            break;
                        case SHOWADS:
                            SHOWADS = Boolean.parseBoolean(sb.toString());
                            Log.d("Show ads", SHOWADS+ "");
                            break;
                        case TIMELINE:
                            TIMELINE = Boolean.parseBoolean(sb.toString());
                            Log.d("Show timeline", TIMELINE+ "");
                            break;

                        case STORECONF:
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
            map.put("SPLASHCOLOR", SPLASHCOLOR);
            map.put("SHOWSPLASH", SHOWSPLASH + "");
            map.put("SHOWADS", SHOWADS + "");
            map.put("TIMELINE", TIMELINE + "");
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
                .putString("SPLASHCOLOR", SPLASHCOLOR)
                .putBoolean("CREATESHORTCUT", CREATESHORTCUT)
                .putBoolean("SHOWSPLASH", SHOWSPLASH)
                .putBoolean("SHOWADS", SHOWADS)
                .putBoolean("TIMELINE", TIMELINE)
                .putString("STOREDESCRIPTION", DESCRIPTION)
                .putString("STOREAVATAR", AVATAR)
                .putString("STORETHEME", THEME)
                .putString("STOREITEMS", ITEMS)
                .putString("STOREVIEW", VIEW)
                .apply();

        if(RESTRICTIONLIST != null && !RESTRICTIONLIST.equals("")) {
            SharedPreferences ssPref = SecurePreferences.getInstance();
            if (ssPref.contains("RESTRICTIONLIST")) {
                RESTRICTIONLIST += ", " + ssPref.getString("RESTRICTIONLIST", "");
            }
            Log.d("Restriction List", "Restriction List saved: " + RESTRICTIONLIST);
            ssPref.edit().putString("RESTRICTIONLIST", RESTRICTIONLIST).apply();
        }


    }*/

    @Override
    public Class getStartActivityClass(){
        return StartPartner.class;
    }

    @Override
    public Class getAppViewActivityClass() {
        return com.aptoide.partners.AppViewActivityPartner.class;
    }

    @Override
    public Class getSettingsActivityClass() {
        return com.aptoide.partners.SettingsPartner.class;
    }

    @Override
    public Class getSignUpActivityClass() {
        return com.aptoide.partners.SignUpActivityPartner.class;
    }

    @Override
    public String getUpdatesSyncAdapterAuthority(){
        return Aptoide.getContext().getPackageName() + ".UpdatesProvider";
    }

    @Override
    public String getAutoUpdatesSyncAdapterAuthority(){
        return Aptoide.getContext().getPackageName() + ".AutoUpdateProvider";
    }

    @Override
    public String getSearchAuthority() {
        return Aptoide.getContext().getPackageName() + ".SuggestionProvider";
    }

    @Override
    public String getAccountType() { return AccountGeneralPartners.ACCOUNT_TYPE;
    };

    @Override
    public Class<?> getSearchActivityClass() {
        return com.aptoide.partners.SearchManagerPartners.class;
    }

    @Override
    public Class getIABPurchaseActivityClass(){
        return IABPurchaseActivityPartners.class;
    }

    @Override
    public Class getPaidAppPurchaseActivityClass(){
        return PaidAppPurchaseActivityPartners.class;
    }

    @Override
    public String getTrackUrl() {
        return "com.aptoide.partners.PushNotificationTrackUrl";
    }

    @Override
    public String getAction() {
        return "com.aptoide.partners.PushNotification";
    }

    @Override
    public String getActionFirstTime() {
        return "com.aptoide.partners.PushNotificationFirstTime";
    }

    @Override
    public Class getNotificationsReceiver() {
        return PushNotificationReceiverPartner.class;
    }

    @Override
    public int getIcon() { return R.drawable.ic_launcher; }


}
