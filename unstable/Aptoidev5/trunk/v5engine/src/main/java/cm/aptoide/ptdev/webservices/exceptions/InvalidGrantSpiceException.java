package cm.aptoide.ptdev.webservices.exceptions;

import com.octo.android.robospice.persistence.exception.SpiceException;

import java.io.IOException;

/**
 * Created by rmateus on 18-07-2014.
 */
public class InvalidGrantSpiceException extends SpiceException {
    private String error_description;

    public InvalidGrantSpiceException(String error_description) {
        super(error_description);
        this.error_description = error_description;
    }

    public String getError_description() {
        return error_description;
    }
}


