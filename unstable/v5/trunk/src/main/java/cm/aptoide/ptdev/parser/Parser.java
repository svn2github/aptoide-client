package cm.aptoide.ptdev.parser;


import android.util.Log;
import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.configuration.AptoideConfiguration;
import cm.aptoide.ptdev.parser.callbacks.CompleteCallback;
import cm.aptoide.ptdev.parser.callbacks.ErrorCallback;
import cm.aptoide.ptdev.parser.handlers.AbstractHandler;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Parser{

    PriorityBlockingQueue<Runnable> pq = new PriorityBlockingQueue<Runnable>(5, new ComparePriority());

    ExecutorService service = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(),Runtime.getRuntime().availableProcessors(), 0L, TimeUnit.MILLISECONDS, pq);

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        Log.d("Aptoide-Parser", "GC on Parser");
    }

    public void parse(String url, final int priority, final AbstractHandler handler, final ErrorCallback errorCallback, CompleteCallback completeCallback) {

        File file = new File(AptoideConfiguration.getInstance().getPathCache()+url.hashCode()+".xml");

        Ion.with(Aptoide.getContext(), url).write(file).setCallback(new FutureCallback<File>() {
            @Override
            public void onCompleted(final Exception e, final File file) {
                if(e!=null){
                    e.printStackTrace();
                    if(errorCallback!=null)errorCallback.onError(e);
                }else{
                    service.submit(new RunnableWithPriority(priority) {
                        @Override
                        public void run() {
                            Aptoide.getDb().beginTransaction();

                            try {
                                SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
                                parser.parse(file, handler);

                            } catch (ParserConfigurationException e1) {
                                e1.printStackTrace();
                            } catch (SAXException e1) {
                                e1.printStackTrace();
                                if(errorCallback!=null)errorCallback.onError(e);
                            } catch (IOException e1) {
                                e1.printStackTrace();
                                if(errorCallback!=null)errorCallback.onError(e);
                            }

                            Aptoide.getDb().setTransactionSuccessful();
                            Aptoide.getDb().endTransaction();
                        }
                    });


                }
            }
        });





    }

    public void parse(String url, int priority, AbstractHandler handler) {
        parse(url, priority, handler, null, null);
    }

    private class ComparePriority<T extends RunnableWithPriority> implements Comparator<T> {
        @Override
        public int compare(T o1, T o2) {
            return o1.getPriority().compareTo(o2.getPriority());
        }
    }
}