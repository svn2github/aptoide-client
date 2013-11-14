package cm.aptoide.ptdev.parser;


import android.util.Log;
import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.configuration.AptoideConfiguration;
import cm.aptoide.ptdev.parser.callbacks.CompleteCallback;
import cm.aptoide.ptdev.parser.callbacks.ErrorCallback;
import cm.aptoide.ptdev.parser.callbacks.PoolEndedCallback;
import cm.aptoide.ptdev.parser.handlers.AbstractHandler;
import cm.aptoide.ptdev.services.FileRequest;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.concurrent.*;

public class Parser{

    private int totalNumberOfTasks;

    private SpiceManager spiceManager;
    private int a;
    private int i;

    public Parser(SpiceManager manager){
        spiceManager = manager;
        Log.d("Aptoide-Parser", "newParser");
    }

    public ExecutorService getExecutor() {
        return service;
    }

    PoolEndedCallback poolEndedCallback;

    PriorityBlockingQueue<Runnable> pq = new PriorityBlockingQueue<Runnable>(10, new ComparePriority());



    ExecutorService service = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, pq, new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {

            Thread t = new Thread(r);
            t.setPriority(3);

            return t;
        }
    });

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        Log.d("Aptoide-Parser", "GC on Parser");
    }

    public void parse(final String url, final int priority, final AbstractHandler handler, final ErrorCallback errorCallback, final CompleteCallback completeCallback) {
        int key = url.hashCode();
        final File file = new File(AptoideConfiguration.getInstance().getPathCache()+key+".xml");
        i++;
        final long repoId = handler.getRepoId();

        spiceManager.execute(new FileRequest(url,file), Math.abs(key), DurationInMillis.ONE_WEEK, new RequestListener<InputStream>() {
            @Override
            public void onRequestFailure(SpiceException spiceException) {
                Log.d("Aptoide-Parser", "onRequestFailure");
                i--;
                if(threadPoolIsIdle() && poolEndedCallback!=null){
                    poolEndedCallback.onEnd();
                }
                if (errorCallback != null) errorCallback.onError(spiceException, repoId);
            }

            @Override
            public void onRequestSuccess(final InputStream inputStream) {

                Log.d("Aptoide-Parser", "onRequestSuccess " + url);

                service.execute(new RunnableWithPriority(priority) {
                    @Override
                    public void run() {

                        Log.d("Aptoide-Parser", "Starting parse " + url);
                        long startTime = System.currentTimeMillis();
                        Aptoide.getDb().beginTransaction();

                        try {
                            SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
                            Log.d("Aptoide-Parser", "New SaxParser");
                            parser.parse(inputStream, handler);
                            if(completeCallback!=null)completeCallback.onComplete(repoId);
                        } catch (ParserConfigurationException e1) {
                            e1.printStackTrace();
                            if (errorCallback != null) errorCallback.onError(e1, repoId);
                            Log.d("Aptoide-Parser", "Error");
                        } catch (SAXException e1) {
                            e1.printStackTrace();
                            if (errorCallback != null) errorCallback.onError(e1, repoId);
                            Log.d("Aptoide-Parser", "Error");
                        } catch (IOException e1) {
                            e1.printStackTrace();
                            if (errorCallback != null) errorCallback.onError(e1, repoId);
                            Log.d("Aptoide-Parser", "Error");
                        } catch (Exception e1){
                            e1.printStackTrace();
                            if (errorCallback != null) errorCallback.onError(e1, repoId);
                            Log.d("Aptoide-Parser", "Error");

                        }



                        Aptoide.getDb().setTransactionSuccessful();
                        Aptoide.getDb().endTransaction();
                        file.delete();
                        i--;
                        Log.d("Aptoide-Parser", url + " Took : " + (System.currentTimeMillis() - startTime) + " ms" + " i=" + i);
                        if(threadPoolIsIdle() && poolEndedCallback!=null){
                            poolEndedCallback.onEnd();
                        }

                    }
                });
            }
        });
    }

    private boolean threadPoolIsIdle() {
        return i==0;
    }

    public void parse(String url, int priority, AbstractHandler handler) {
        parse(url, priority, handler, null, null);
    }

    public void setPoolEndCallback(PoolEndedCallback callback) {
        this.poolEndedCallback = callback;
    }

    private class ComparePriority<T extends RunnableWithPriority> implements Comparator<T> {
        @Override
        public int compare(T o1, T o2) {
            return o1.getPriority().compareTo(o2.getPriority());
        }
    }
}