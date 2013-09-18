package cm.aptoide.pt;

import android.content.Context;
import android.util.Log;
import cm.aptoide.pt.util.NetworkUtils;
import cm.aptoide.pt.util.Utils;
import cm.aptoide.pt.views.ViewApk;
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

    private static Database database = Database.getInstance();
    private Server server;
    private Context context;


    public RepoLocaleUpdater(Server server, Context context){
        this.server = server;
        this.context = context;
    }

    interface  EndElement{

        void endElement();

    }

    private static HashMap<String, EndElement> elementHashMap = new HashMap<String, EndElement>();
    final static ViewApk apk = new ViewApk();
    private final static StringBuilder sb = new StringBuilder();

    static {

        elementHashMap.put("name", new EndElement() {
            @Override
            public void endElement() {
                apk.setName(sb.toString());
            }
        });

        elementHashMap.put("apkid", new EndElement() {
            @Override
            public void endElement() {
                apk.setApkid(sb.toString());
            }
        });

        elementHashMap.put("entry", new EndElement() {
            @Override
            public void endElement() {
                database.updateApkName(apk);
            }
        });

    }

    public void parse(){

        NetworkUtils networkUtils = new NetworkUtils();
        try {

            BufferedInputStream is = networkUtils.getInputStream("http://webservices.aptoide.com/webservices/listRepositoryLocalApkNames/"+server.name+"/"+ Utils.getMyCountryCode(context)+"/xml", context);

            SAXParser parser = SAXParserFactory.newInstance().newSAXParser();

            long startParse = System.currentTimeMillis();
            apk.setRepo_id(server.id);
            parser.parse(is, new DefaultHandler2(){

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

                    if (elementHashMap.get(localName)!=null){
                        elementHashMap.get(localName).endElement();
                    }
                }
            });

            Log.w("TAG", "End time: " + (System.currentTimeMillis() - startParse));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }



    }







}
