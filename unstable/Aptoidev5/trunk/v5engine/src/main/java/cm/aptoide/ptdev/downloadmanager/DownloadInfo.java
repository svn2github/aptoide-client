package cm.aptoide.ptdev.downloadmanager;

import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import android.widget.ListView;
import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.downloadmanager.event.DownloadEvent;
import cm.aptoide.ptdev.downloadmanager.event.DownloadStatusEvent;
import cm.aptoide.ptdev.downloadmanager.state.*;
import cm.aptoide.ptdev.events.BusProvider;
import cm.aptoide.ptdev.model.Download;
import com.squareup.otto.Produce;

import java.io.File;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 02-07-2013
 * Time: 10:49
 * To change this template use File | Settings | File Templates.
 */
public class DownloadInfo implements Runnable, Serializable {


    public DownloadInfo(DownloadManager manager, long id) {
        this.id = id;
        this.downloadManager = manager;
        mStatusState = new NoState(this);
    }

    public Download getDownload() {
        return download;
    }

    private Download download;
    private long id;

    //    private final DownloadConnection mConnection;
    private List<DownloadModel> mFilesToDownload;
    private long mDownloadedSize;
    private long mSize;
    private double mSpeed;
    private long mETA;


    private StatusState mStatusState;
    private String mDestination;
    private static final int UPDATE_INTERVAL_MILLISECONDS = 1000;

    private ArrayList<DownloadThread> threads = new ArrayList<DownloadThread>();


    private DownloadExecutor downloadExecutor;
    private long mProgress = 0;
    private boolean isPaused = false;
    private ArrayList<String> downloadingFilenames = new ArrayList<String>();

    public void setDownloadManager(DownloadManager downloadManager) {
        this.downloadManager = downloadManager;
    }

    private DownloadManager downloadManager;


    public void setFilesToDownload(List<DownloadModel> mFilesToDownload) {
        this.mFilesToDownload = mFilesToDownload;
    }

    public DownloadInfo(DownloadManager manager, List<DownloadModel> filesToDownload, int id, Download download) {
        this.downloadManager = manager;
        this.mFilesToDownload = filesToDownload;
        this.id = id;
        this.download = download;
        this.download.setId(id);
        this.download.setParent(this);
        mStatusState = new NoState(this);
    }

    public int getPercentDownloaded() {
        if (mSize == 0) {
            return 0;
        }

        return (int) ((mProgress) * 100 / mSize);
    }


    @Override
    public void run() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Timer timer = new Timer();

        try {

            for (DownloadModel file : mFilesToDownload) {
                DownloadThread thread = new DownloadThread(file, this);
                executor.submit(thread);
                threads.add(thread);
            }


            checkDirectorySize(Environment.getExternalStorageDirectory().getAbsolutePath()+"/.aptoide/apks");


            mSize = getAllThreadSize();
            TimerTask task = new TimerTask() {


                /** How much was downloaded last time. */
                private long iMLastDownloadedSize = mDownloadedSize;
                /** The nanoTime last time. */
                private long iMLastTime = System.currentTimeMillis();
                private long iMFirstTime = System.currentTimeMillis();
                public long mAvgSpeed;

                @Override
                public void run() {
                    long mReaminingSize = getAllSizeRemaining();
                    mDownloadedSize = getAllDownloadedSize();
                    mProgress = getAllProgress();

                    long timeElapsedSinceLastTime = System.currentTimeMillis() - iMLastTime;
                    long timeElapsed = System.currentTimeMillis() - iMFirstTime;
                    iMLastTime = System.currentTimeMillis();
                    // Difference between last time and this time = how much was downloaded since last run.
                    long downloadedSinceLastTime = mDownloadedSize - iMLastDownloadedSize;
                    iMLastDownloadedSize = mDownloadedSize;
                    if (timeElapsedSinceLastTime > 0 && timeElapsed > 0) {
                        // Speed (bytes per second) = downloaded bytes / time in seconds (nanoseconds / 1000000000)
                        mAvgSpeed = (mDownloadedSize) * 1000 / timeElapsed;
                        mSpeed = downloadedSinceLastTime * 1000 / timeElapsedSinceLastTime;
                    }


                    if (mAvgSpeed > 0) {
                        // ETA (milliseconds) = remaining byte size / bytes per millisecond (bytes per second * 1000)
                        mETA = (mReaminingSize - mDownloadedSize) * 1000 / mAvgSpeed;
                    }
//                    else {
//                        mETA = 0;
//                    }

//                    Log.d("DownloadManager", "ETA: " + mETA + " AvgSpeed: " + mAvgSpeed / 1000 + " RemainingSize: " + mReaminingSize + " Downloaded: " + mDownloadedSize + " Status: " + mStatusState.toString());
                    Log.d("DownloadManager", "ETA: " + mETA + " Speed: " + mSpeed / 1000 + " Size: " + Utils.formatBytes(mSize) + " Downloaded: " + Utils.formatBytes(mDownloadedSize) + " Status: " + mStatusState + " TotalDownloaded: " + Utils.formatBytes(mProgress) + " " + System.identityHashCode(DownloadInfo.this));
//                    Log.d("DownloadManager", threads.size() + " on queue.");

//                    notifyListeners(new DownloadProgressEvent(DownloadInfo.this));


                    download.setSpeed(getSpeed());
                    download.setTimeLeft(mETA);
                    download.setProgress(getPercentDownloaded());
                    BusProvider.getInstance().post(getDownloadEvent());
//                    mSpeed = 0;
//                    mETA = 0;
                }
            };
            // Schedule above task for every (UPDATE_INTERVAL_MILLISECONDS) milliseconds.
            timer.schedule(task, 0, UPDATE_INTERVAL_MILLISECONDS);
            executor.shutdown();
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);

            timer.cancel();
            timer.purge();
            mSize = getAllThreadSize();
            mProgress = getAllProgress();


            Log.d("TAG", "Downloads done " + mSize + " " + mProgress + " " + mStatusState.getEnumState().name());
            download.setSpeed(getSpeed());
            download.setProgress(getPercentDownloaded());

            if (mStatusState instanceof ActiveState) {
                changeStatusState(new CompletedState(this));
                autoExecute();
            }


        } catch (RuntimeException e) {
            changeStatusState(new ErrorState(this, EnumDownloadFailReason.NO_REASON));
            e.printStackTrace();
        } catch (Exception e) {
            changeStatusState(new ErrorState(this, EnumDownloadFailReason.NO_REASON));
            e.printStackTrace();
        }

        BusProvider.getInstance().post(new DownloadEvent(getId(), mStatusState));

        downloadManager.updatePendingList();

        threads.clear();
        mDownloadedSize = 0;
        mSpeed = 0;
        mETA = 0;

    }

    @Produce
    private Download getDownloadEvent() {
        return download;
    }

    double getDirSize(File dir) {
        double size = 0;
        if (dir.isFile()) {
            if (!downloadingFilenames.contains(dir.getName())) {
                size = dir.length();
            }
        } else {
            File[] subFiles = dir.listFiles();
            for (File file : subFiles) {
                if (file.isFile()) {
                    if (!downloadingFilenames.contains(file.getName())) {
                        size += file.length();
                    }
                } else {
                    size += this.getDirSize(file);
                }

            }
        }
        return size;
    }

    private void checkDirectorySize(String dirPath) {


        File dir = new File(dirPath);


        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                return;
            }
        }
        double size = getDirSize(dir) / 1024 / 1024;
        SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(Aptoide.getContext());
        long maxFileCache = Long.parseLong((sPref.getString("maxFileCache", "200")));
        if (maxFileCache < 50) maxFileCache = 50;
        if (maxFileCache > 0 && size > maxFileCache) {
            File[] files = dir.listFiles();
            long latestTime = System.currentTimeMillis();
            long currentTime = 0;
            File fileToDelete = null;
            for (File file : files) {
                currentTime = file.lastModified();

                if (currentTime < latestTime && !downloadingFilenames.contains(file.getName())) {
                    latestTime = currentTime;
                    fileToDelete = file;
                }


            }
            if (fileToDelete != null) {
                Log.d("TAG", "Deleting " + fileToDelete.getName());
                if(!fileToDelete.delete()){
                    return;
                }
                checkDirectorySize(dirPath);
            }
        }
    }

    public EnumDownloadFailReason getFailReason() {
        return ((ErrorState) mStatusState).getErrorMessage();
    }

    public void autoExecute() {
        if (downloadExecutor != null) {
            for (DownloadModel file : mFilesToDownload) {
                if (file.isAutoExecute()) {
                    downloadExecutor.execute();
                }
            }
        }
    }


    public void setDownloadExecutor(DownloadExecutor executor) {
        this.downloadExecutor = executor;
    }


    private long getAllDownloadedSize() {

        long sum = 0;
        for (DownloadThread thread : threads) {
            sum = sum + thread.getmDownloadedSize();
//            Log.d("DownloadManagerThread", "Downloaded: " + thread.getmDownloadedSize());
        }
        return sum;
    }

    private long getAllProgress() {

        long sum = 0;
        for (DownloadThread thread : threads) {
            sum = sum + thread.getmProgress();
//            Log.d("DownloadManagerThread", "Downloaded: " + thread.getmDownloadedSize());
        }
        return sum;
    }

    private long getAllThreadSize() {
        long sum = 0;
        for (DownloadThread thread : threads) {
            sum = sum + thread.getmFullSize();
//            Log.d("DownloadManagerThread", "Size: " + thread.getmRemainingSize());
        }
        return sum;
    }


    public void pause() {
        this.isPaused = true;
        this.mStatusState.pause();

    }

    public void remove() {
        changeStatusState(new CompletedState(this));

        if (mFilesToDownload == null) return;
        for (DownloadModel model : mFilesToDownload) {
            new File(model.getDestination()).delete();
        }
        mProgress = 0;
        mSize = 0;
        mFilesToDownload.clear();
        downloadManager.removeDownload(this);
        BusProvider.getInstance().post(new DownloadEvent(getId(), mStatusState));
    }


    public void download() {
        mProgress = 0;
        BusProvider.getInstance().post(new DownloadEvent(getId(), mStatusState));
        this.mStatusState.download();
    }

    public String getDestination() {
        return mDestination;
    }

    public void setDestination(String mDestination) {
        this.mDestination = mDestination;
    }

    public void setStatusState(StatusState statusState) {
        this.mStatusState = statusState;
    }

    public StatusState getStatusState() {
        return this.mStatusState;
    }

    public double getSpeed() {
        return mSpeed * 8;
    }

    public long getEta() {
        return mETA;
    }

    public void changeStatusState(StatusState state) {
        mStatusState.changeTo(state);
    }


    public long getId() {
        return id;
    }

    public long getAllSizeRemaining() {
        long sum = 0;
        for (DownloadThread thread : threads) {
            sum = sum + thread.getmRemainingSize();
//            Log.d("DownloadManagerThread", "Size: " + thread.getmRemainingSize());
        }
        return sum;
    }

    public boolean isPaused() {
        return isPaused;
    }


    public DownloadManager getDownloadManager() {
        return downloadManager;
    }

    public void setDownload(Download download) {
        this.download = download;
        this.download.setParent(this);
    }
}
