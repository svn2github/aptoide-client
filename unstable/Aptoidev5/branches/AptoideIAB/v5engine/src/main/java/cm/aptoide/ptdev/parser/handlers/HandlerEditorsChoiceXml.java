package cm.aptoide.ptdev.parser.handlers;

import android.util.Log;
import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.model.Apk;
import cm.aptoide.ptdev.model.ApkEditorsChoice;
import cm.aptoide.ptdev.model.Server;
import cm.aptoide.ptdev.utils.AptoideUtils;
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


    private boolean insideCat;

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

        elements.put("featuregraphic", new ElementHandler() {
            public void startElement(Attributes atts) throws SAXException {

            }

            @Override
            public void endElement() throws SAXException {
                ((ApkEditorsChoice)apk).setFeaturedGraphic(server.getFeaturedGraphicPath() + sb.toString());
            }
        });

        elements.put("featuregraphicpath", new ElementHandler() {
            public void startElement(Attributes atts) throws SAXException {

            }

            @Override
            public void endElement() throws SAXException {
                server.setFeaturedGraphicPath(sb.toString());
            }
        });



        elements.put("package", new ElementHandler() {
            @Override
            public void startElement(Attributes attributes) throws SAXException {
                insidePackage = true;

                apk = getApk();
                apk.addCategoryId(510);
                repoId = getDb().insertServer(server);
                apk.setRepoId(getRepoId());
            }

            @Override
            public void endElement() throws SAXException {
                if (isRunning()) {
                    if (apk.getChildren() != null) {
                        for (Apk theApk : apk.getChildren()) {
                            Log.d("Aptoide-Multiple-Apk", "Inserting multipleApk " + theApk + " " + apk.getChildren());
                            theApk.databaseInsert(statements, categoriesIds);
                        }
                        apk.setChildren(null);
                    } else {
                        apk.databaseInsert(statements, categoriesIds);
                    }
                }
                insidePackage = false;
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
