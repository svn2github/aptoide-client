package cm.aptoide.pt.dev;

import android.app.Activity;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.util.Log;
import cm.aptoide.pt.dev.database.Database;
import cm.aptoide.pt.dev.model.ApkTop;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {

    ExecutorService executorService = Executors.newFixedThreadPool(2);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        Runnable r = new Runnable() {
            public ApkTop apk;

            @Override
            public void run() {
                int i = 0;
                while(i<200){
                    i++;
                    long startTime = System.currentTimeMillis();
                    Database.getInstance().get("");
                    Log.d("TAG1", "Time:" +  (System.currentTimeMillis() - startTime) + "" );
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }


            }
        };
        Runnable r1 = new Runnable() {
            public ApkTop apk;

            @Override
            public void run() {
                int i = 0;
                while(i<20000){

                    i++;
                    long startTime = System.currentTimeMillis();
                    Database.getInstance().get("cm.aptoide.pt");
                    Log.d("TAG1", "Time:" +  (System.currentTimeMillis() - startTime) + "" );
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }


            }
        };

        Runnable r2 = new Runnable() {
            public ApkTop apk;

            @Override
            public void run() {
                long startTime = System.currentTimeMillis();

                try {
                        apk = new ApkTop();
                        SAXParser parser = SAXParserFactory.newInstance().newSAXParser();


                        Database.getInstance().startTransaction();
                        parser.parse(new URL("http://m3taxx.store.aptoide.com/info.xml").openStream(), new DefaultHandler2() {
                            final SQLiteStatement statements[] = Database.getInstance().compileStatement2(apk.getStatements());
                            StringBuilder sb = new StringBuilder();


                            @Override
                            public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                                super.startElement(uri, localName, qName, attributes);
                                sb.setLength(0);
                            }

                            @Override
                            public void characters(char[] ch, int start, int length) throws SAXException {
                                super.characters(ch, start, length);
                                sb.append(ch,start,length);
                            }

                            @Override
                            public void endElement(String uri, String localName, String qName) throws SAXException {
                                super.endElement(uri, localName, qName);

                                if (localName.equals("apkid")) {
                                    apk.setName(sb.toString());
                                    apk.setStoreId(String.valueOf(1));




                                    apk.insert(statements);

                                    apk = new ApkTop();
                                    Database.getInstance().yield();
                                }

                            }
                        });
                    Database.getInstance().endTransaction();

                } catch (Exception e) {
                    e.printStackTrace();
                }

                Log.e("TAG1", "End time: " + (System.currentTimeMillis() - startTime));
            }
        };
        Runnable r3 = new Runnable() {
            public ApkTop apk;

            @Override
            public void run() {
                long startTime = System.currentTimeMillis();

                try {
                    apk = new ApkTop();
                    SAXParser parser = SAXParserFactory.newInstance().newSAXParser();


                    Database.getInstance().startTransaction();
                    parser.parse(new URL("http://savou.store.aptoide.com/info.xml").openStream(), new DefaultHandler2() {
                        final SQLiteStatement statements[] = Database.getInstance().compileStatement2(apk.getStatements());

                        StringBuilder sb = new StringBuilder();


                        @Override
                        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                            super.startElement(uri, localName, qName, attributes);
                            sb.setLength(0);
                        }

                        @Override
                        public void characters(char[] ch, int start, int length) throws SAXException {
                            super.characters(ch, start, length);
                            sb.append(ch,start,length);
                        }
                        @Override
                        public void endElement(String uri, String localName, String qName) throws SAXException {
                            super.endElement(uri, localName, qName);

                            if (localName.equals("apkid")) {
                                apk.setName(sb.toString());

                                apk.setStoreId(String.valueOf(2));
                                apk.insert(statements);

                                apk = new ApkTop();
                                Database.getInstance().yield();
                            }

                        }
                    });
                    Database.getInstance().endTransaction();

                } catch (Exception e) {
                    e.printStackTrace();
                }

                Log.e("TAG1", "End time: " + (System.currentTimeMillis() - startTime));
            }
        };


        executorService.submit(r);
        executorService.submit(r1);
        executorService.submit(r2);
        executorService.submit(r3);





    }
}
