/*******************************************************************************
 * Copyright (c) 2012 rmateus.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package cm.aptoide.pt.util;



import android.content.Context;
import cm.aptoide.pt.HWSpecifications;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

public class Utils {
    public static void CopyStream(InputStream is, OutputStream os)
    {
        final int buffer_size=1024;
        try
        {
            byte[] bytes=new byte[buffer_size];
            for(;;)
            {
              int count=is.read(bytes, 0, buffer_size);
              if(count==-1)
                  break;
              os.write(bytes, 0, count);
            }
        }
        catch(Exception ex){}
    }

    public static String filters(Context context) {

        int minSdk = HWSpecifications.getSdkVer();
        String minScreen = cm.aptoide.pt.Filters.Screens.values()
                [HWSpecifications.getScreenSize(context)]
                .name()
                .toLowerCase(Locale.ENGLISH);
        String minGlEs = HWSpecifications.getGlEsVer(context);


        final int density = HWSpecifications.getDensityDpi(context);

        String cpuAbi = HWSpecifications.getCpuAbi();

        if(HWSpecifications.getCpuAbi2().length()>0){
            cpuAbi += ","+HWSpecifications.getCpuAbi2();
        }

        String filters = "maxSdk="+minSdk+"&maxScreen="+minScreen+"&maxGles="+minGlEs+"&myCPU="+cpuAbi+"&myDensity="+density;

        return Base64.encodeToString(filters.getBytes(), 0).replace("=","").replace("/","*").replace("+","_").replace("\n", "");
    }

    public static String getMyCountryCode(Context context){
        return context.getResources().getConfiguration().locale.getLanguage()+"_"+context.getResources().getConfiguration().locale.getCountry();
    }

    public static String getMyCountry(Context context){
        return context.getResources().getConfiguration().locale.getLanguage();
    }
}
