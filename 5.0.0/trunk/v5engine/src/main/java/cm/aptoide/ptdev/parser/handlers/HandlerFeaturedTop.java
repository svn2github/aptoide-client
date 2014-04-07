package cm.aptoide.ptdev.parser.handlers;

import android.util.Log;
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

    private boolean insideCat;

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

        elements.put("cat", new ElementHandler() {
            public void startElement(Attributes atts) throws SAXException {
                category = new Category();
                insideCat = true;
            }

            @Override
            public void endElement() throws SAXException {
                getDb().insertCategory(category.name, category.parent, category.real_id, category.order, 0);
                insideCat = false;
                Log.d("Aptoide-", "Inserting category");
            }
        });

        elements.put("catids", new ElementHandler() {
            public void startElement(Attributes atts) throws SAXException {

            }

            @Override
            public void endElement() throws SAXException {
                for(String catid : sb.toString().split(",")){
                    apk.addCategoryId(Integer.parseInt(catid));
                }
            }
        });


        elements.put("name", new ElementHandler() {
            public void startElement(Attributes atts) throws SAXException {

            }

            @Override
            public void endElement() throws SAXException {
                if(insideCat){
                    category.name = sb.toString();
                }else if(insidePackage){
                    apk.setName(sb.toString());
                }else{
                    server.setName(sb.toString());
                }
            }
        });

        elements.put("parent", new ElementHandler() {
            public void startElement(Attributes atts) throws SAXException {
            }

            @Override
            public void endElement() throws SAXException {
                try{
                    category.parent= Integer.parseInt(sb.toString());
                }catch (NumberFormatException e){
                    category.parent= 0;
                }


            }
        });

        elements.put("order", new ElementHandler() {
            public void startElement(Attributes atts) throws SAXException {

            }

            @Override
            public void endElement() throws SAXException {
                category.order= Integer.parseInt(sb.toString());

            }
        });

        elements.put("id", new ElementHandler() {
            public void startElement(Attributes atts) throws SAXException {

            }

            @Override
            public void endElement() throws SAXException {
                category.real_id= Integer.parseInt(sb.toString());
            }
        });

    }
}
