package cm.aptoide.ptdev.services;


import android.util.Log;
import cm.aptoide.ptdev.model.Login;
import cm.aptoide.ptdev.utils.Base64;
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
        try{


            final HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(url).openConnection();

            if (login != null) {

                String basicAuth = "Basic " + new String(Base64.encode(
                        (login.getUsername() + ":" + login.getPassword()).getBytes(),
                        Base64.NO_WRAP));
                httpURLConnection.addRequestProperty("Authorization", basicAuth);

            }



            is = processStream(httpURLConnection.getContentLength(), httpURLConnection.getInputStream());

        } catch (Exception e){
            e.printStackTrace();
            Log.d("Aptoide-Parser", "Error url" + e);
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

            fileOutputStream = new FileOutputStream(cacheFile);

            BufferedInputStream is = new BufferedInputStream(inputStream, 8*1024);

            IOUtils.copy(is, fileOutputStream);
            IOUtils.closeQuietly(is);
            Log.d("Aptoide-Parser", "Writed to " + cacheFile.getAbsolutePath());

        } finally {
            IOUtils.closeQuietly(fileOutputStream);
        }
        return new FileInputStream(cacheFile);

    }
}

