package cm.aptoide.ptdev.webservices.json.reviews;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by rmateus on 23-02-2015.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)

@JsonPropertyOrder({
        "url",
        "stype"
})
public class Screenshot {

    @JsonProperty("url")
    private String url;
    @JsonProperty("stype")
    private String stype;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     *
     * @return
     * The url
     */
    @JsonProperty("url")
    public String getUrl() {
        return url;
    }

    /**
     *
     * @param url
     * The url
     */
    @JsonProperty("url")
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     *
     * @return
     * The stype
     */
    @JsonProperty("stype")
    public String getStype() {
        return stype;
    }

    /**
     *
     * @param stype
     * The stype
     */
    @JsonProperty("stype")
    public void setStype(String stype) {
        this.stype = stype;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @Override
    public String toString() {
        return "Screenshot{" +
                "url='" + url + '\'' +
                ", stype='" + stype + '\'' +
                ", additionalProperties=" + additionalProperties +
                '}';
    }
}