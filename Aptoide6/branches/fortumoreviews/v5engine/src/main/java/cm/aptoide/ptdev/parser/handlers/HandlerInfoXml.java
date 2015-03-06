package cm.aptoide.ptdev.parser.handlers;

import android.util.Log;

import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.model.Apk;
import cm.aptoide.ptdev.model.ApkInfoXML;
import cm.aptoide.ptdev.model.Server;
import cm.aptoide.ptdev.parser.exceptions.InvalidVersionException;
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


    private String countriesPermitted;

    public boolean isDelta() {
        return isDelta;
    }

    private boolean isDelta;
    private File file;
    private boolean isRemove;
    private boolean insideCat;

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

    public String getCountriesPermitted() {
        return countriesPermitted;
    }




    @Override
    protected void loadSpecificElements() {



        elements.put("package", new ElementHandler() {
            @Override
            public void startElement(Attributes attributes) throws SAXException {
                apk = getApk();
                apk.setRepoId(repoId);
            }

            @Override
            public void endElement() throws SAXException {
                if (isRunning()) {

                    if (isRemove) {
                        apk.databaseDelete(getDb());
                        isRemove = false;
                    } else if (apk.getChildren() != null) {
                        for (Apk theApk : apk.getChildren()) {
                            //Log.d("Aptoide-Multiple-Apk", "Inserting multipleApk");
                            theApk.databaseInsert(statements, categoriesIds);
                        }
                        apk.setChildren(null);
                    } else {
                        apk.databaseInsert(statements, categoriesIds);
                    }
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

                if (delta.length() > 0) {
                    server.setHash(delta);
                }


            }
        });

        elements.put("version", new ElementHandler() {
            @Override
            public void startElement(Attributes attributes) throws SAXException {



            }

            @Override
            public void endElement() throws SAXException {

                if(Integer.parseInt(sb.toString())<7){
                    //Log.d("Aptoide-Parser", "Throwing exception");
                    throw new InvalidVersionException();

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

        elements.put("localize", new ElementHandler() {
            public void startElement(Attributes atts) throws SAXException {

            }

            @Override
            public void endElement() throws SAXException {
               countriesPermitted = sb.toString();
            }
        });

        elements.put("repository", new ElementHandler() {
            public void startElement(Attributes atts) throws SAXException {

            }

            @Override
            public void endElement() throws SAXException {


                //getDb().putCategoriesIds(categoriesIds, getRepoId());
                getDb().updateServer(server, getRepoId());
            }
        });



        elements.put("del", new ElementHandler() {
            public void startElement(Attributes atts) throws SAXException {
                isRemove = true;
            }

            @Override
            public void endElement() throws SAXException {

            }
        });

        elements.put("categories", new ElementHandler() {
            public void startElement(Attributes atts) throws SAXException {

                if (!isDelta) {
                    getDb().clearStore(getRepoId());
                }

                getDb().clearCategories(getRepoId());
            }

            @Override
            public void endElement() throws SAXException {

            }
        });

        elements.put("cat", new ElementHandler() {
            public void startElement(Attributes atts) throws SAXException {
                category = new Category();
                insideCat = true;
            }

            @Override
            public void endElement() throws SAXException {
                getDb().insertCategory(category.name, category.parent, category.real_id, category.order, getRepoId());
                insideCat = false;
                //Log.d("Aptoide-", "Inserting category");
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
                }else{
                    apk.setName(sb.toString());
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

    @Override
    public void endDocument() throws SAXException {
        super.endDocument();
        getDb().updateAppsCount(repoId);

        if(!isDelta){
            //Log.d("Aptoide-Parser", "Calculating md5");
            String md5 = AptoideUtils.Algorithms.md5Calc(file);
            //Log.d("Aptoide-Parser", "md5 is " + md5);
            server.setHash(md5);
        }
        getDb().updateServer(server, getRepoId());

    }

    public void setFile(File file) {
        this.file = file;
    }

}
