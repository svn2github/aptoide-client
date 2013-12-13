package cm.aptoide.ptdev.parser.handlers;

import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.model.Apk;
import cm.aptoide.ptdev.model.ApkFeaturedTopXml;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 03-12-2013
 * Time: 15:13
 * To change this template use File | Settings | File Templates.
 */
public class HandlerFeaturedTop extends HandlerTopXml {

    public HandlerFeaturedTop(Database db) {
        super(db, 0);
    }

    @Override
    protected Apk getApk() {
        return new ApkFeaturedTopXml();
    }

    @Override
    protected void loadSpecificElements() {
        super.loadSpecificElements();

        elements.put("repository", new ElementHandler() {
            @Override
            public void startElement(Attributes attributes) throws SAXException {

            }

            @Override
            public void endElement() throws SAXException {
                repoId = getDb().insertServer(server);
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
