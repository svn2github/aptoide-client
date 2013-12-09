package cm.aptoide.ptdev.parser.handlers;

import android.util.Log;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.model.Apk;
import cm.aptoide.ptdev.model.ApkInfoXML;
import cm.aptoide.ptdev.model.Server;
import cm.aptoide.ptdev.utils.AptoideUtils;
import cm.aptoide.ptdev.utils.Configs;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.File;
import java.text.ParseException;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 22-10-2013
 * Time: 16:17
 * To change this template use File | Settings | File Templates.
 */
public class HandlerInfoXml extends AbstractHandler {


    private boolean isDelta;
    private File file;

    public HandlerInfoXml(Database db, long repoId) {
        super(db, repoId);
    }

    @Override
    protected Server getServer() {
        return new Server();
    }


    @Override
    protected Apk getApk() {
        return new ApkInfoXML();
    }



    @Override
    protected void loadSpecificElements() {
        elements.put("package", new ElementHandler() {
            @Override
            public void startElement(Attributes attributes) throws SAXException {
                apk = getApk();
                ((ApkInfoXML)apk).setRepoId(repoId);
            }

            @Override
            public void endElement() throws SAXException {
                if(isRunning()){
                    apk.databaseInsert(statements, categoriesIds);
                }

            }
        });

        elements.put("delta", new ElementHandler() {
            @Override
            public void startElement(Attributes attributes) throws SAXException {

                isDelta = true;

            }

            @Override
            public void endElement() throws SAXException {

                String delta = sb.toString();

                if(delta.length()>0){
                    server.setHash(delta);
                }


            }
        });

        elements.put("date", new ElementHandler() {
            public void startElement(Attributes atts) throws SAXException {

            }

            @Override
            public void endElement() throws SAXException {
                try {
                    apk.setDate(Configs.TIME_STAMP_FORMAT_INFO_XML.parse(sb.toString()));
                } catch (ParseException e) {
                    e.printStackTrace();
                    apk.setDate(new Date(0));
                }
            }
        });

        elements.put("repository", new ElementHandler() {
            public void startElement(Attributes atts) throws SAXException {

            }

            @Override
            public void endElement() throws SAXException {

                if(!isDelta){
                    getDb().clearStore(getRepoId());
                }
                getDb().putCategoriesIds(categoriesIds, getRepoId());
                getDb().updateServer(server, getRepoId());
            }
        });
    }

    @Override
    public void endDocument() throws SAXException {
        super.endDocument();
        getDb().updateAppsCount(repoId);

        if(!isDelta){
            Log.d("Aptoide-Parser", "Calculating md5");
            String md5 = AptoideUtils.Algorithms.md5Calc(file);
            server.setHash(md5);
        }
        getDb().updateServer(server, getRepoId());

    }

    public void setFile(File file) {

        this.file = file;

    }
}
