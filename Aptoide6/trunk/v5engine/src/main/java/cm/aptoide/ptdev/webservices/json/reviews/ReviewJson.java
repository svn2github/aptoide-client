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
        "status",
        "review"
})
public class ReviewJson {

    @JsonProperty("status")
    private String status;
    @JsonProperty("review")
    private Review review;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

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
     * The review
     */
    @JsonProperty("review")
    public Review getReview() {
        return review;
    }

    /**
     *
     * @param review
     * The review
     */
    @JsonProperty("review")
    public void setReview(Review review) {
        this.review = review;
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
        return "ReviewJson{" +
                "status='" + status + '\'' +
                ", review=" + review +
                ", additionalProperties=" + additionalProperties +
                '}';
    }
}
