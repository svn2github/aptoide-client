package cm.aptoide.ptdev.downloadmanager;


import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

public class DownloadModel implements Serializable {

    private final String destination;
    private final String url;
    private final String md5;
    private boolean isAutoExecute = false;
    private long size;
    private String fallbackUrl;

    public DownloadFile getFile() {
        return file;
    }

    private DownloadFile file;

    public DownloadConnection createConnection() throws IOException {

        return new DownloadConnectionImpl(new URL(url));
    }

    public DownloadFile createFile() throws FileNotFoundException, CompletedDownloadException {
        this.file = new DownloadFile(destination,md5);
        return file;
    }

    public DownloadModel(String url, String destination, String md5, long size) {
        this.url = url;
        this.destination = destination;
        this.md5 = md5;
        this.size = size;

    }

    public String getDestination() {

        File file = new File(destination +"--downloading");

        if (file.exists()) {
            return destination + "--downloading";
        } else {
            return destination;
        }

    }

    public boolean isAutoExecute() {
        return isAutoExecute;
    }

    public void setAutoExecute(boolean autoExecute) {
        isAutoExecute = autoExecute;
    }

    @Override
    protected void finalize() throws Throwable {
        Log.d("Garbage Collector", "DownloadModel with destination " + destination + " beeing destroyed.");
        super.finalize();
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public DownloadConnection createFallbackConnection() throws IOException {

        if(fallbackUrl!=null){
            return new DownloadConnectionImpl(new URL(fallbackUrl));
        }else{
            return new DownloadConnectionImpl(new URL(url));
        }

    }

    public void setFallbackUrl(String fallbackUrl) {
        this.fallbackUrl = fallbackUrl;
    }
}
