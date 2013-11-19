package cm.aptoide.ptdev.services;

import android.util.Base64;
import android.util.Log;
import cm.aptoide.ptdev.model.Login;
import com.octo.android.robospice.request.ProgressByteProcessor;
import com.octo.android.robospice.request.SpiceRequest;
import org.apache.commons.io.IOUtils;
import roboguice.util.temp.Ln;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 05-11-2013
 * Time: 15:59
 * To change this template use File | Settings | File Templates.
 */
public class FileRequest extends SpiceRequest<InputStream>{


    private final File cacheFile;
    private final String url;
    private final Login login;

    public FileRequest(final String url, final File cacheFile, final Login login) {
        super(InputStream.class);
        this.cacheFile = cacheFile;
        this.login = login;
        this.url = url;
    }


    @Override
    public InputStream loadDataFromNetwork() throws Exception {

        InputStream is = null;
        final byte[] buf = new byte[4096];
        File file = null;
        try{
            file = new File("/sdcard/.aptoide/" + url.hashCode());

            final HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(url).openConnection();

            if(login!=null){
                String basicAuth = "Basic "+ new String(Base64.encode(
                        (login.getUsername() + ":" + login.getPassword()).getBytes(),
                        Base64.NO_WRAP));
                httpURLConnection.setRequestProperty("Authorization", basicAuth);
            }

            is = httpURLConnection.getInputStream();

        } catch (Exception e){
            Log.d("Aptoide-Parser", "Error url");
        }





        return is;
    }

    protected void readBytes(final InputStream in, final ProgressByteProcessor processor) throws IOException {
        final byte[] buf = new byte[4096];
        try {
            int amt;
            do {
                amt = in.read(buf);
                if (amt == -1) {
                    break;
                }
            } while (processor.processBytes(buf, 0, amt));
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    public InputStream processStream(final int contentLength, final InputStream inputStream) throws IOException {
        OutputStream fileOutputStream = null;
        try {
            // touch
            boolean isTouchedNow = cacheFile.setLastModified(System.currentTimeMillis());
            if (!isTouchedNow) {
                Ln.d("Modification time of file %s could not be changed normally ", cacheFile.getAbsolutePath());
            }
            fileOutputStream = new FileOutputStream(cacheFile);
            readBytes(inputStream, new ProgressByteProcessor(this, fileOutputStream, contentLength));
            return new FileInputStream(cacheFile);
        } finally {
            IOUtils.closeQuietly(fileOutputStream);
        }
    }
}
