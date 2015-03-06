package cm.aptoide.ptdev;

import java.util.Locale;

/**
 * Created by jcosta on 16-07-2014.
 */

public enum VeredictReview {
    GOOD(R.string.flag_review_good),
    LICENSE(R.string.flag_license),
    FAKE(R.string.flag_review_fake),
    FREEZE(R.string.flag_review_freeze),
    VIRUS(R.string.flag_review_virus),
    UNKNOWN(-1);

    private int string;

    VeredictReview (int string){

        this.string = string;
    }

    public int getString() {
        return string;
    }

    public static VeredictReview reverseLookup (String string) {
        return VeredictReview.valueOf(string.toUpperCase(Locale.ENGLISH) );
    }
}

