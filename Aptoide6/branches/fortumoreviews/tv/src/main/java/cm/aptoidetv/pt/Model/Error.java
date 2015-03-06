package cm.aptoidetv.pt.Model;

import com.google.api.client.util.Key;

/**
 * Created by rmateus on 03-01-2014.
 */
public class Error {

    @Key
    private String code;

    @Key
    private String msg;

    public String getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
