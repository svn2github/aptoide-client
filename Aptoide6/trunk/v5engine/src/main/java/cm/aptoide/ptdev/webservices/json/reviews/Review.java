package cm.aptoide.ptdev.webservices.json.reviews;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by rmateus on 23-02-2015.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)

@JsonPropertyOrder({
        "id",
        "repo_id",
        "performance",
        "stability",
        "usability",
        "addiction",
        "status",
        "apk",
        "user",
        "added_timestamp",
        "updated_timestamp",
        "title",
        "pros",
        "cons",
        "final_verdict",
        "lang"
})
public class Review {

    @JsonProperty("id")
    private Integer id;
    @JsonProperty("repo_id")
    private Integer repoId;
    @JsonProperty("performance")
    private Integer performance;
    @JsonProperty("stability")
    private Integer stability;
    @JsonProperty("usability")
    private Integer usability;
    @JsonProperty("addiction")
    private Integer addiction;
    @JsonProperty("status")
    private String status;
    @JsonProperty("apk")
    private Apk apk;
    @JsonProperty("user")
    private User user;
    @JsonProperty("added_timestamp")
    private String addedTimestamp;
    @JsonProperty("updated_timestamp")
    private String updatedTimestamp;
    @JsonProperty("title")
    private String title;
    @JsonProperty("final_verdict")
    private String finalVerdict;
    @JsonProperty("lang")
    private String lang;
    @JsonProperty("pros")
    private List<String> pros;
    @JsonProperty("cons")
    private List<String> cons;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

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
     * The repoId
     */
    @JsonProperty("repo_id")
    public Integer getRepoId() {
        return repoId;
    }

    /**
     *
     * @param repoId
     * The repo_id
     */
    @JsonProperty("repo_id")
    public void setRepoId(Integer repoId) {
        this.repoId = repoId;
    }

    /**
     *
     * @return
     * The performance
     */
    @JsonProperty("performance")
    public Integer getPerformance() {
        return performance;
    }

    /**
     *
     * @param performance
     * The performance
     */
    @JsonProperty("performance")
    public void setPerformance(Integer performance) {
        this.performance = performance;
    }

    /**
     *
     * @return
     * The stability
     */
    @JsonProperty("stability")
    public Integer getStability() {
        return stability;
    }

    /**
     *
     * @param stability
     * The stability
     */
    @JsonProperty("stability")
    public void setStability(Integer stability) {
        this.stability = stability;
    }

    /**
     *
     * @return
     * The usability
     */
    @JsonProperty("usability")
    public Integer getUsability() {
        return usability;
    }

    /**
     *
     * @param usability
     * The usability
     */
    @JsonProperty("usability")
    public void setUsability(Integer usability) {
        this.usability = usability;
    }

    /**
     *
     * @return
     * The addiction
     */
    @JsonProperty("addiction")
    public Integer getAddiction() {
        return addiction;
    }

    /**
     *
     * @param addiction
     * The addiction
     */
    @JsonProperty("addiction")
    public void setAddiction(Integer addiction) {
        this.addiction = addiction;
    }

    /**
     *
     * @return
     * The status
     */
    @JsonProperty("status")
    public String getStatus() {
        return status;
    }

    /**
     *
     * @param status
     * The status
     */
    @JsonProperty("status")
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     *
     * @return
     * The apk
     */
    @JsonProperty("apk")
    public Apk getApk() {
        return apk;
    }

    /**
     *
     * @param apk
     * The apk
     */
    @JsonProperty("apk")
    public void setApk(Apk apk) {
        this.apk = apk;
    }

    /**
     *
     * @return
     * The user
     */
    @JsonProperty("user")
    public User getUser() {
        return user;
    }

    /**
     *
     * @param user
     * The user
     */
    @JsonProperty("user")
    public void setUser(User user) {
        this.user = user;
    }

    /**
     *
     * @return
     * The addedTimestamp
     */
    @JsonProperty("added_timestamp")
    public String getAddedTimestamp() {
        return addedTimestamp;
    }

    /**
     *
     * @param addedTimestamp
     * The added_timestamp
     */
    @JsonProperty("added_timestamp")
    public void setAddedTimestamp(String addedTimestamp) {
        this.addedTimestamp = addedTimestamp;
    }

    /**
     *
     * @return
     * The updatedTimestamp
     */
    @JsonProperty("updated_timestamp")
    public String getUpdatedTimestamp() {
        return updatedTimestamp;
    }

    /**
     *
     * @param updatedTimestamp
     * The updated_timestamp
     */
    @JsonProperty("updated_timestamp")
    public void setUpdatedTimestamp(String updatedTimestamp) {
        this.updatedTimestamp = updatedTimestamp;
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
     * @param pros
     * The title
     */
    @JsonProperty("pros")
    public void setPros(List<String> pros) {
        this.pros = pros;
    }

    /**
     *
     * @param cons
     * The title
     */
    @JsonProperty("cons")
    public void setCons(List<String> cons) {
        this.cons = cons;
    }

    /**
     *
     * @return
     * The finalVerdict
     */
    @JsonProperty("final_verdict")
    public String getFinalVerdict() {
        return finalVerdict;
    }

    /**
     *
     * @param finalVerdict
     * The final_verdict
     */
    @JsonProperty("final_verdict")
    public void setFinalVerdict(String finalVerdict) {
        this.finalVerdict = finalVerdict;
    }

    /**
     *
     * @return
     * The lang
     */
    @JsonProperty("lang")
    public String getLang() {
        return lang;
    }

    /**
     *
     * @param lang
     * The lang
     */
    @JsonProperty("lang")
    public void setLang(String lang) {
        this.lang = lang;
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
        return "Review{" +
                "id=" + id +
                ", repoId=" + repoId +
                ", performance=" + performance +
                ", stability=" + stability +
                ", usability=" + usability +
                ", addiction=" + addiction +
                ", status='" + status + '\'' +
                ", apk=" + apk +
                ", user=" + user +
                ", addedTimestamp='" + addedTimestamp + '\'' +
                ", updatedTimestamp='" + updatedTimestamp + '\'' +
                ", title='" + title + '\'' +
                ", finalVerdict='" + finalVerdict + '\'' +
                ", lang='" + lang + '\'' +
                ", additionalProperties=" + additionalProperties +
                '}';
    }

    public List<String> getPros() {
        return pros;
    }

    public List<String> getCons() {
        return cons;
    }
}
