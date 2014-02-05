package cm.aptoide.ptdev.downloadmanager;

import android.util.Log;
import cm.aptoide.ptdev.utils.AptoideUtils;


import java.io.*;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 02-07-2013
 * Time: 11:17
 * To change this template use File | Settings | File Templates.
 */
public class DownloadFile implements Serializable{

    private final File file;

    public RandomAccessFile getmFile() throws FileNotFoundException {
        return new RandomAccessFile(file, "rw");
    }

    public void delete(){

        new File(mDestination).delete();

    }


    private String mDestination;
    private String md5;



    public DownloadFile(String destination, String md5) throws FileNotFoundException {
        this.md5 = md5;
        this.mDestination = destination;
        file = new File(this.mDestination);

        File dir = file.getParentFile();
        if ((dir != null) && (!dir.isDirectory())) {
            dir.mkdirs();
        }

    }

    public static long getFileLength(String path)
    {
        File f = new File(path);
        if (f.exists()) {
            return f.length();
        }

        return 0L;
    }

    public String getDestination()
    {
        return this.mDestination;
    }


    public void close(RandomAccessFile file) {
        try {
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void setDownloadedSize(RandomAccessFile file, long downloadedSize) throws IOException {

        Log.d("DownloadFile", "Position is: " + downloadedSize);
        file.getChannel().position(downloadedSize);

    }

    public String getMd5() {
        return md5;
    }

    public synchronized void checkMd5() throws Md5FailedException {

        String md5 = getMd5();

        if(md5.length()>0){

            String calculatedMd5 = AptoideUtils.Algorithms.md5Calc(new File(mDestination));

            if(!calculatedMd5.equals(md5)){

                Log.d("TAG", "Failed Md5: " + mDestination + "   calculated " + calculatedMd5 + " vs " + md5);
                throw new Md5FailedException();
            }

        }


    }
}
