package cm.aptoide.ptdev.webservices.exceptions;

import java.io.IOException;

/**
 * Created by rmateus on 18-07-2014.
 */
public class InvalidGrantException extends IOException {
    private final String error_description;

    public InvalidGrantException(String error_description) {
        this.error_description = error_description;
    }

    public String getError_description() {
        return error_description;
    }
}


