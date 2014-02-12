package com.aptoide.partners;

import android.util.Log;
import cm.aptoide.ptdev.AppViewActivity;
import cm.aptoide.ptdev.configuration.AptoideConfiguration;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

/**
 * Created by tdeus on 12/23/13.
 */
public class AptoideConfigurationPartners extends AptoideConfiguration {

    public static String PARTNERTYPE;
    public static String PARTNERID;
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
    public static String MARKETNAME = "";
    public static String ADUNITID = "";

    public static String THEME = null;
    public static String AVATAR = null;
    public static String DESCRIPTION = null;
    public static String VIEW = null;
    public static String ITEMS = null;

    static enum Elements { BOOTCONF, APTOIDECONF, PARTNERTYPE, PARTNERID, DEFAULTSTORENAME, BRAND, SPLASHSCREEN, MATURECONTENTSWITCH, MATURECONTENTSWITCHVALUE,SEARCHSTORES, MULTIPLESTORES, CUSTOMEDITORSCHOICE, APTOIDETHEME, SPLASHSCREENLAND, MARKETNAME, ADUNITID,
        STORECONF, THEME, AVATAR, DESCRIPTION, VIEW, ITEMS }


    public String getTheme() { return APTOIDETHEME.toUpperCase(Locale.ENGLISH); }

    public static void setTheme(String appTheme) {
        AptoideConfigurationPartners.APTOIDETHEME = appTheme;
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
                            Log.d("Mature content Switch", MATURECONTENTSWITCH+ "");
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
                            AptoideConfigurationPartners.setTheme(APTOIDETHEME);
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

                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

//        savePreferences();

//        createSdCardBinary();
    }


//    @Override
//    public ActivitiesClasses getClasses(){
//        return new ActivitiesClassesPartners();
//    }
//
//    public static class ActivitiesClassesPartners extends ActivitiesClasses {
//
//        @Override
//        public Class getAppViewActivity() {
//            return this.AppViewActivity;
//        }
//
//        private  final Class AppViewActivity = com.aptoide.partners.AppViewActivityPartners.class;
//
//    }

}
