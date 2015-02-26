package cm.aptoide.ptdev.webservices.json.reviews;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by rmateus on 23-02-2015.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)

@JsonPropertyOrder({
        "id",
        "package",
        "vercode",
        "title",
        "icon",
        "screenshots"
})
public class Apk {

    @JsonProperty("id")
    private Integer id;
    @JsonProperty("package")
    private String _package;
    @JsonProperty("vercode")
    private Integer vercode;
    @JsonProperty("vername")
    private String vername;
    @JsonProperty("title")
    private String title;
    @JsonProperty("icon")
    private String icon;
    @JsonProperty("screenshots")
    private List<Screenshot> screenshots = new ArrayList<Screenshot>();
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     *
     * @return
     */
    public String getVername() {
        return vername;
    }


    /**
     *
     * @param vername
     */
    public void setVername(String vername) {
        this.vername = vername;
    }

    /**
     *
     * @return
     * The id
     */
    @JsonProperty("id")
    public Integer getId() {
        return id;
    }

    /**
     *
     * @param id
     * The id
     */
    @JsonProperty("id")
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     *
     * @return
     * The _package
     */
    @JsonProperty("package")
    public String getPackage() {
        return _package;
    }

    /**
     *
     * @param _package
     * The package
     */
    @JsonProperty("package")
    public void setPackage(String _package) {
        this._package = _package;
    }

    /**
     *
     * @return
     * The vercode
     */
    @JsonProperty("vercode")
    public Integer getVercode() {
        return vercode;
    }

    /**
     *
     * @param vercode
     * The vercode
     */
    @JsonProperty("vercode")
    public void setVercode(Integer vercode) {
        this.vercode = vercode;
    }

    /**
     *
     * @return
     * The title
     */
    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

    /**
     *
     * @param title
     * The title
     */
    @JsonProperty("title")
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     *
     * @return
     * The icon
     */
    @JsonProperty("icon")
    public String getIcon() {
        return icon;
    }

    /**
     *
     * @param icon
     * The icon
     */
    @JsonProperty("icon")
    public void setIcon(String icon) {
        this.icon = icon;
    }

    /**
     *
     * @return
     * The screenshots
     */
    @JsonProperty("screenshots")
    public List<Screenshot> getScreenshots() {
        return screenshots;
    }

    /**
     *
     * @param screenshots
     * The screenshots
     */
    @JsonProperty("screenshots")
    public void setScreenshots(List<Screenshot> screenshots) {
        this.screenshots = screenshots;
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
        return "Apk{" +
                "id=" + id +
                ", _package='" + _package + '\'' +
                ", vercode=" + vercode +
                ", title='" + title + '\'' +
                ", icon='" + icon + '\'' +
                ", screenshots=" + screenshots +
                ", additionalProperties=" + additionalProperties +
                '}';
    }
}
