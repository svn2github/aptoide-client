package cm.aptoidetv.pt.Model;

import cm.aptoidetv.pt.CardPresenter;
import cm.aptoidetv.pt.WebServices.Response;

public class EditorsChoice extends ApplicationAPK {
    public EditorsChoice(Response.ListApps.Apk apk) {
        super(apk);
    }
    @Override
    public int getWidth() {
        return CardPresenter.card_presenter_width;
    }

    @Override
    public int getHeight() {
        return CardPresenter.card_presenter_height;
    }

}
