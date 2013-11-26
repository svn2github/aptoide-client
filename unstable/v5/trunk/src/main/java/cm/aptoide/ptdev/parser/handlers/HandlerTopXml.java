package cm.aptoide.ptdev.parser.handlers;

import android.util.Log;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.events.BusProvider;
import cm.aptoide.ptdev.model.Apk;
import cm.aptoide.ptdev.model.ApkLatestXml;
import cm.aptoide.ptdev.model.ApkTopXML;
import cm.aptoide.ptdev.parser.events.StopParseEvent;
import cm.aptoide.ptdev.parser.handlers.AbstractHandler;
import cm.aptoide.ptdev.utils.Configs;
import com.squareup.otto.Subscribe;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 06-11-2013
 * Time: 14:37
 * To change this template use File | Settings | File Templates.
 */
public class HandlerTopXml extends AbstractHandler {
    private final HashMap<String, Long> categoriesIds = new HashMap<String, Long>();



    public HandlerTopXml(Database db, long repoId) {
        super(db, repoId);
        BusProvider.getInstance().register(this);


    }

    @Subscribe
    public void stopParse(StopParseEvent event){
        Log.d("Aptoide-Parser", "Received stopparseevent for repo " + event.getRepoId());
        if(event.getRepoId()==repoId){
            setRunning(false);
        }
    }

    @Override
    protected Apk getApk() {
        return new ApkLatestXml();
    }

    @Override
    protected void loadSpecificElements() {
        elements.put("package", new ElementHandler() {
            @Override
            public void startElement(Attributes attributes) throws SAXException {
                apk = getApk();
                ((ApkLatestXml)apk).setRepoId(repoId);
                apk.setCategory1("Top Apps");
            }

            @Override
            public void endElement() throws SAXException {
                apk.databaseInsert(statements, categoriesIds);
            }
        });

        elements.put("timestamp", new ElementHandler() {
            public void startElement(Attributes atts) throws SAXException {

            }

            @Override
            public void endElement() throws SAXException {
                try {
                    apk.setDate(Configs.TIME_STAMP_FORMAT.parse(sb.toString()));
                } catch (ParseException e) {
                    e.printStackTrace();
                    apk.setDate(new Date(0));
                }
            }
        });
    }
}
