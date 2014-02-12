package com.aptoide.partners;

import cm.aptoide.ptdev.*;
import cm.aptoide.ptdev.AptoideThemePicker;
import cm.aptoide.ptdev.preferences.ManagerPreferences;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by tdeus on 12/23/13.
 */
public class AptoidePartner extends Aptoide {



    @Override
    public void bootImpl(ManagerPreferences managerPreferences) {
//        super.bootImpl(managerPreferences);
        managerPreferences.createLauncherShortcut(this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    InputStream inputStream = getContext().getAssets().open("boot_config.xml");
                    AptoideConfigurationPartners.parseBootConfigStream(inputStream);

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ParserConfigurationException e) {
                    e.printStackTrace();
                } catch (SAXException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    @Override
    public AptoideConfigurationPartners getAptoideConfiguration() {
        return new AptoideConfigurationPartners();
    }

    @Override
    public AptoideThemePicker getNewThemePicker() {
        return new com.aptoide.partners.AptoideThemePicker();
    }
}
