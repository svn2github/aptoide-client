package cm.aptoide.ptdev.downloadmanager;

import android.os.StatFs;
import android.util.Log;
import cm.aptoide.ptdev.downloadmanager.state.ActiveState;
import cm.aptoide.ptdev.downloadmanager.state.ErrorState;


import java.io.*;

import java.net.UnknownHostException;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 02-07-2013
 * Time: 15:22
 * To change this template use File | Settings | File Templates.
 */
public class DownloadThread implements Runnable, Serializable {

    private long mFullSize;
    private long mProgress;
    private DownloadModel download;
    private long fileSize;

    public long getmDownloadedSize() {
        return mDownloadedSize;
    }

    public long getmProgress() {
        if(mProgress > 0){
            return mProgress;
        }else{
            return 0;
        }

    }




    private long mDownloadedSize = 0;


    public long getmFullSize() {
        return mFullSize;
    }

    public long getmRemainingSize() {
        return mRemainingSize;
    }

    private long mRemainingSize;
    private DownloadInfo parent;


    public DownloadThread(DownloadModel download, DownloadInfo parent) throws IOException {

        this.download = download;
        this.parent = parent;
        this.mConnection = download.createConnection();
        this.mProgress = DownloadFile.getFileLength(download.getDestination());
        this.mFullSize = download.getSize();
        this.mRemainingSize = mFullSize;
//        parent.addAlreadyDownloadedSize(mProgress);

    }

    DownloadConnection mConnection = null;
    DownloadFile mDownloadFile = null;

    transient RandomAccessFile file = null;

    @Override
    public void run() {
        try{
            if(!(parent.getStatusState() instanceof ActiveState)){
                return;
            }
            mDownloadFile = download.createFile();
            file = mDownloadFile.getmFile();

            this.mConnection = download.createConnection();
            this.mDownloadedSize = 0;
            fileSize = DownloadFile.getFileLength(download.getDestination());
            mDownloadFile.setDownloadedSize(file, fileSize);
            this.mRemainingSize =  mFullSize - fileSize;

            download();

//            Log.d("DownloadManager", "Download done with " + new Md5Handler().md5Calc(new File(mDestination)));
        }catch (NotFoundException exception){
            exception.printStackTrace();
            parent.changeStatusState(new ErrorState(parent, EnumDownloadFailReason.NOT_FOUND));
        }catch (FileNotFoundException exception){
            exception.printStackTrace();
            parent.changeStatusState(new ErrorState(parent, EnumDownloadFailReason.SD_ERROR));
        }catch (ContentTypeNotApkException e){
            parent.changeStatusState(new ErrorState(parent, EnumDownloadFailReason.PAIDAPP_NOTFOUND));
        } catch (IPBlackListedException e) {

            if(mConnection!=null){
                mConnection.close();
            }

            try {
                mConnection = download.createFallbackConnection();
                download();
            } catch (NotFoundException exception) {
                exception.printStackTrace();
                parent.changeStatusState(new ErrorState(parent, EnumDownloadFailReason.NOT_FOUND));
            } catch (FileNotFoundException exception) {
                exception.printStackTrace();
                parent.changeStatusState(new ErrorState(parent, EnumDownloadFailReason.SD_ERROR));
            } catch (ContentTypeNotApkException e1) {
                parent.changeStatusState(new ErrorState(parent, EnumDownloadFailReason.PAIDAPP_NOTFOUND));
            } catch (IPBlackListedException e1) {
                parent.changeStatusState(new ErrorState(parent, EnumDownloadFailReason.CONNECTION_ERROR));
            } catch (Md5FailedException e1) {
                e1.printStackTrace();
                mDownloadFile.delete();
                parent.changeStatusState(new ErrorState(parent, EnumDownloadFailReason.MD5_CHECK_FAILED));
            } catch (UnknownHostException e1) {
                e1.printStackTrace();
                parent.changeStatusState(new ErrorState(parent, EnumDownloadFailReason.CONNECTION_ERROR));
            } catch (IOException e1) {
                e1.printStackTrace();
                parent.changeStatusState(new ErrorState(parent, EnumDownloadFailReason.CONNECTION_ERROR));
            } catch (CompletedDownloadException e1) {
                mFullSize = mProgress = fileSize;
                mRemainingSize = 0;
                e1.printStackTrace();
            } catch (Exception e1) {
                e1.printStackTrace();
                parent.changeStatusState(new ErrorState(parent, EnumDownloadFailReason.CONNECTION_ERROR));
            }

        }catch (Md5FailedException e){
            e.printStackTrace();
            mDownloadFile.delete();
            parent.changeStatusState(new ErrorState(parent, EnumDownloadFailReason.MD5_CHECK_FAILED));
        } catch (UnknownHostException e){
            e.printStackTrace();
            parent.changeStatusState(new ErrorState(parent, EnumDownloadFailReason.CONNECTION_ERROR));
        } catch (IOException e){
            e.printStackTrace();

            parent.changeStatusState(new ErrorState(parent, EnumDownloadFailReason.CONNECTION_ERROR));
        } catch (CompletedDownloadException e) {

            mFullSize = mProgress = fileSize;
            mRemainingSize = 0;

            e.printStackTrace();


        } catch (Exception e){
            e.printStackTrace();
            parent.changeStatusState(new ErrorState(parent, EnumDownloadFailReason.CONNECTION_ERROR));
        }

        if(mDownloadFile!=null && file!=null){
            mDownloadFile.close(file);
        }

        if(mConnection!=null){
            mConnection.close();
        }

//        BusProvider.getInstance().post(parent);

    }

    private void download() throws IOException, CompletedDownloadException, NotFoundException, IPBlackListedException, ContentTypeNotApkException, Md5FailedException {
        mConnection.connect(fileSize);

        Log.d("DownloadManager", "Starting Download " + (parent.getStatusState() instanceof ActiveState) + " " + this.mDownloadedSize + fileSize + " " + this.mRemainingSize);
        byte[] bytes = new byte[1024];
        int bytesRead;
        BufferedInputStream mStream = mConnection.getStream();

        if( parent.getStatusState() instanceof ActiveState){
            StatFs stat = new StatFs(download.getDestination());

            long blockSize = stat.getBlockSize();
            long availableBlocks = stat.getAvailableBlocks();

            long avail = (blockSize * availableBlocks);

            if( mRemainingSize > avail){
                parent.changeStatusState(new ErrorState(parent, EnumDownloadFailReason.NO_FREE_SPACE));
            }
        }

        while ( (bytesRead = mStream.read(bytes)) != -1 && parent.getStatusState() instanceof ActiveState) {
            file.write(bytes, 0, bytesRead);
            this.mDownloadedSize += bytesRead;
            this.mProgress += bytesRead;
        }

        if(parent.getStatusState() instanceof ActiveState){
            mDownloadFile.checkMd5();
            mDownloadFile.rename();
        }
    }


}
