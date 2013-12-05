package cm.aptoide.ptdev.parser.handlers;

import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.model.Apk;
import cm.aptoide.ptdev.model.ApkEditorsChoice;
import cm.aptoide.ptdev.model.Server;
import cm.aptoide.ptdev.utils.Configs;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.text.ParseException;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 29-11-2013
 * Time: 18:19
 * To change this template use File | Settings | File Templates.
 */
public class HandlerEditorsChoiceXml extends AbstractHandler {


    public HandlerEditorsChoiceXml(Database db, long repoId) {
        super(db, repoId);
    }

    @Override
    protected Server getServer() {
        return new Server();
    }

    @Override
    protected Apk getApk() {
        return new ApkEditorsChoice();
    }

    boolean insidePackage;

    @Override
    protected void loadSpecificElements() {



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

        elements.put("package", new ElementHandler() {
            @Override
            public void startElement(Attributes attributes) throws SAXException {
                insidePackage = true;

                apk = getApk();
                repoId = getDb().insertServer(server);

                ((ApkEditorsChoice)apk).setRepoId(getRepoId());
            }

            @Override
            public void endElement() throws SAXException {
                if (isRunning()) {
                    apk.databaseInsert(statements, categoriesIds);
                }
                insidePackage = false;
            }
        });

        elements.put("name", new ElementHandler() {
            @Override
            public void startElement(Attributes attributes) throws SAXException {
            }

            @Override
            public void endElement() throws SAXException {
                if(insidePackage){
                    apk.setName(sb.toString());
                }else{
                    server.setName(sb.toString());
                }

            }
        });


    }
}
