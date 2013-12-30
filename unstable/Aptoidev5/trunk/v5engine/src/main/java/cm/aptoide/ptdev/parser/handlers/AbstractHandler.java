package cm.aptoide.ptdev.parser.handlers;

import android.database.sqlite.SQLiteStatement;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.model.Apk;
import cm.aptoide.ptdev.model.Server;
import cm.aptoide.ptdev.parser.exceptions.ParseStoppedException;
import cm.aptoide.ptdev.utils.Filters;
import com.squareup.otto.Subscribe;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 22-10-2013
 * Time: 15:49
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractHandler extends DefaultHandler2 {


    protected final HashMap<Integer, Integer> categoriesIds = new HashMap<Integer, Integer>();


    public boolean isRunning() {
        return isRunning;
    }

    @Subscribe
    public void stopParse(){
        setRunning(false);
        statements.clear();
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }

    private boolean isRunning = true;

    public long getRepoId() {
        return repoId;
    }

    protected long repoId;

    public Database getDb() {
        return db;
    }

    private final Database db;

    ArrayList<SQLiteStatement> statements;
    private int i;


    public AbstractHandler(Database db, long repoId){
        this.db=db;
        this.repoId = repoId;
        loadCommonElements();
        loadSpecificElements();

        statements = new ArrayList<SQLiteStatement>(db.compileStatements(apk.getStatements()));
    }





    Apk apk = getApk();
    Server server = getServer();

    protected abstract Server getServer();

    static StringBuilder sb = new StringBuilder();
    HashMap<String, ElementHandler> elements = new HashMap<String, ElementHandler>();

    private boolean multipleApk;

    interface ElementHandler{

        void startElement(Attributes attributes) throws SAXException;
        void endElement() throws SAXException;

    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        sb.setLength(0);
        if(elements.get(localName)!=null){
            elements.get(localName).startElement(attributes);
        }

    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        super.characters(ch, start, length);
        sb.append(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);
        if(!isRunning()){
            throw new ParseStoppedException();
        }
        if(elements.get(localName)!=null){
            elements.get(localName).endElement();
        }

    }

    @Override
    public void endDocument() throws SAXException {
        super.endDocument();
    }

    void loadCommonElements(){



        elements.put("apklst", new ElementHandler() {
            public void startElement(Attributes atts) throws SAXException {

            }

            @Override
            public void endElement() throws SAXException {

            }
        });

        elements.put("localize", new ElementHandler() {
            public void startElement(Attributes atts) throws SAXException {
            }

            @Override
            public void endElement() throws SAXException {

            }
        });

        elements.put("cpu", new ElementHandler() {
            @Override
            public void startElement(Attributes atts) throws SAXException {

            }

            @Override
            public void endElement() throws SAXException {
                apk.setCpuAbi(sb.toString());
            }
        });

        elements.put("screenCompat", new ElementHandler() {
            @Override
            public void startElement(Attributes atts) throws SAXException {

            }

            @Override
            public void endElement() throws SAXException {
                apk.setScreenCompat(sb.toString());
            }
        });






        elements.put("basepath", new ElementHandler() {
            public void startElement(Attributes atts) throws SAXException {

            }

            @Override
            public void endElement() throws SAXException {
                server.setBasepath(sb.toString());
            }
        });

        elements.put("appscount", new ElementHandler() {
            public void startElement(Attributes atts) throws SAXException {

            }

            @Override
            public void endElement() throws SAXException {

            }
        });

        elements.put("iconspath", new ElementHandler() {
            public void startElement(Attributes atts) throws SAXException {

            }

            @Override
            public void endElement() throws SAXException {
                server.setIconspath(sb.toString());
            }
        });

        elements.put("screenspath", new ElementHandler() {
            public void startElement(Attributes atts) throws SAXException {

            }

            @Override
            public void endElement() throws SAXException {
                server.setScreenspath(sb.toString());
            }
        });

        elements.put("webservicespath", new ElementHandler() {
            public void startElement(Attributes atts) throws SAXException {

            }

            @Override
            public void endElement() throws SAXException {
                server.setWebservicespath(sb.toString());

            }
        });

		elements.put("apkpath", new ElementHandler() {
			public void startElement(Attributes atts) throws SAXException {

			}

			@Override
			public void endElement() throws SAXException {
                server.setApkpath(sb.toString());
			}
		});



        elements.put("name", new ElementHandler() {
            public void startElement(Attributes atts) throws SAXException {

            }

            @Override
            public void endElement() throws SAXException {
                apk.setName(sb.toString());
            }
        });

        elements.put("ver", new ElementHandler() {
            public void startElement(Attributes atts) throws SAXException {

            }

            @Override
            public void endElement() throws SAXException {
                apk.setVersionName(sb.toString());
            }
        });

        elements.put("vercode", new ElementHandler() {
            public void startElement(Attributes atts) throws SAXException {

            }

            @Override
            public void endElement() throws SAXException {
                apk.setVersionCode(Integer.parseInt(sb.toString()));

            }
        });

        elements.put("apkid", new ElementHandler() {
            public void startElement(Attributes atts) throws SAXException {
                i++;

            }

            @Override
            public void endElement() throws SAXException {

                apk.setPackageName(sb.toString());

            }
        });

        elements.put("icon", new ElementHandler() {
            public void startElement(Attributes atts) throws SAXException {

            }

            @Override
            public void endElement() throws SAXException {
                if(apk.getIconPath()==null) apk.setIconPath(sb.toString());
            }
        });

        elements.put("icon_hd", new ElementHandler() {
            public void startElement(Attributes atts) throws SAXException {

            }

            @Override
            public void endElement() throws SAXException {
                apk.setIconPath(sb.toString());
            }
        });



        elements.put("dwn", new ElementHandler() {
            public void startElement(Attributes atts) throws SAXException {

            }

            @Override
            public void endElement() throws SAXException {
                apk.setDownloads(Integer.parseInt(sb.toString()));
            }
        });

        elements.put("rat", new ElementHandler() {
            public void startElement(Attributes atts) throws SAXException {

            }

            @Override
            public void endElement() throws SAXException {
                apk.setRating(Double.parseDouble(sb.toString()));
            }
        });

        elements.put("catg", new ElementHandler() {
            public void startElement(Attributes atts) throws SAXException {

            }

            @Override
            public void endElement() throws SAXException {
                apk.setCategory1(sb.toString());
            }
        });

        elements.put("catg2", new ElementHandler() {
            public void startElement(Attributes atts) throws SAXException {

            }

            @Override
            public void endElement() throws SAXException {
                apk.setCategory2(sb.toString());
            }
        });



        elements.put("age", new ElementHandler() {
            public void startElement(Attributes atts) throws SAXException {

            }

            @Override
            public void endElement() throws SAXException {
                apk.setAge(Filters.Age.lookup(sb.toString()));
            }
        });

        elements.put("minSdk", new ElementHandler() {
            public void startElement(Attributes atts) throws SAXException {

            }

            @Override
            public void endElement() throws SAXException {
                apk.setMinSdk(Integer.parseInt(sb.toString()));
            }
        });



        elements.put("minScreen", new ElementHandler() {
            public void startElement(Attributes atts) throws SAXException {

            }

            @Override
            public void endElement() throws SAXException {
                apk.setMinScreen(Filters.Screen.valueOf(sb.toString()));
            }
        });

        elements.put("minGles", new ElementHandler() {
            public void startElement(Attributes atts) throws SAXException {

            }

            @Override
            public void endElement() throws SAXException {
                apk.setMinGlEs(sb.toString());
            }

        });

        elements.put("price", new ElementHandler() {


            public void startElement(Attributes atts) throws SAXException {

            }

            @Override
            public void endElement() throws SAXException {
                apk.setPrice(Double.parseDouble(sb.toString()));

            }
        });

        elements.put("md5", new ElementHandler() {


            public void startElement(Attributes atts) throws SAXException {

            }

            @Override
            public void endElement() throws SAXException {
                apk.setMd5h(sb.toString());

            }
        });
    }

    protected abstract Apk getApk();
    protected abstract void loadSpecificElements();


}
