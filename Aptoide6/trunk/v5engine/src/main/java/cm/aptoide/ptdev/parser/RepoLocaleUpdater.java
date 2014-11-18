package cm.aptoide.ptdev.parser;

import android.content.Context;
import android.database.sqlite.SQLiteStatement;

import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.model.Apk;
import cm.aptoide.ptdev.model.ApkInfoXML;
import cm.aptoide.ptdev.utils.AptoideUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 13-09-2013
 * Time: 11:29
 * To change this template use File | Settings | File Templates.
 */
public class RepoLocaleUpdater {


    private final Database database;
    ;
    private long repoid;
    private String serverName;
    private Context context;


    public RepoLocaleUpdater(long repoid, String serverName, Context context, final Database database){
        this.repoid = repoid;
        this.serverName = serverName;
        this.context = context;
        this.database = database;
        statement = database.getDatabaseInstance().compileStatement("UPDATE apk " +
                "SET name=? " +
                "WHERE package_name=? and id_repo = ?");

        elementHashMap.put("name", new EndElement() {
            @Override
            public void endElement() {
                apk.setName(sb.toString());
            }
        });

        elementHashMap.put("apkid", new EndElement() {
            @Override
            public void endElement() {
                apk.setPackageName(sb.toString());
            }
        });

        elementHashMap.put("entry", new EndElement() {
            @Override
            public void endElement() {
                database.updateApkName(apk, statement);
            }
        });
    }

    interface  EndElement{

        void endElement();

    }

    private HashMap<String, EndElement> elementHashMap = new HashMap<String, EndElement>();
    final Apk apk = new ApkInfoXML();
    private final StringBuilder sb = new StringBuilder();

    private SQLiteStatement statement;

    static {



    }

    public void parse(){


        try {
            AptoideUtils.NetworkUtils networkUtils = new AptoideUtils.NetworkUtils();
            BufferedInputStream is = networkUtils.getInputStream(Aptoide.getConfiguration().getWebServicesUri() + "webservices/listRepositoryLocalApkNames/"+serverName+"/"+ AptoideUtils.getMyCountryCode(context)+"/xml", context);

            SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
            apk.setRepoId(repoid);
            parser.parse(is, new DefaultHandler2() {

                @Override
                public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                    super.startElement(uri, localName, qName, attributes);
                    sb.setLength(0);
                }

                @Override
                public void characters(char[] ch, int start, int length) throws SAXException {
                    super.characters(ch, start, length);
                    sb.append(ch, start, length);
                }

                @Override
                public void endElement(String uri, String localName, String qName) throws SAXException {
                    super.endElement(uri, localName, qName);

                    if (elementHashMap.get(localName) != null) {
                        elementHashMap.get(localName).endElement();
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }



    }







}
