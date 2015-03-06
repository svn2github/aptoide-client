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
        "status",
        "reviews",
        "paging_details"
})

public class ReviewListJson {




    @JsonProperty("status")
    private String status;
    @JsonProperty("reviews")
    private List<Review> reviews = new ArrayList<Review>();
    @JsonProperty("paging_details")
    private PagingDetails pagingDetails;
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
     * The reviews
     */
    @JsonProperty("reviews")
    public List<Review> getReviews() {
        return reviews;
    }

    /**
     *
     * @param reviews
     * The reviews
     */
    @JsonProperty("reviews")
    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }

    /**
     *
     * @return
     * The pagingDetails
     */
    @JsonProperty("paging_details")
    public PagingDetails getPagingDetails() {
        return pagingDetails;
    }

    /**
     *
     * @param pagingDetails
     * The paging_details
     */
    @JsonProperty("paging_details")
    public void setPagingDetails(PagingDetails pagingDetails) {
        this.pagingDetails = pagingDetails;
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
        return "ReviewListJson{" +
                "status='" + status + '\'' +
                ", reviews=" + reviews +
                ", pagingDetails=" + pagingDetails +
                ", additionalProperties=" + additionalProperties +
                '}';
    }



    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({
            "total",
            "offset",
            "limit"
    })
    public static class PagingDetails {

        @JsonProperty("total")
        private Integer total;
        @JsonProperty("offset")
        private Integer offset;
        @JsonProperty("limit")
        private Integer limit;
        @JsonIgnore
        private Map<String, Object> additionalProperties = new HashMap<String, Object>();

        /**
         *
         * @return
         * The total
         */
        @JsonProperty("total")
        public Integer getTotal() {
            return total;
        }

        /**
         *
         * @param total
         * The total
         */
        @JsonProperty("total")
        public void setTotal(Integer total) {
            this.total = total;
        }

        /**
         *
         * @return
         * The offset
         */
        @JsonProperty("offset")
        public Integer getOffset() {
            return offset;
        }

        /**
         *
         * @param offset
         * The offset
         */
        @JsonProperty("offset")
        public void setOffset(Integer offset) {
            this.offset = offset;
        }

        /**
         *
         * @return
         * The limit
         */
        @JsonProperty("limit")
        public Integer getLimit() {
            return limit;
        }

        /**
         *
         * @param limit
         * The limit
         */
        @JsonProperty("limit")
        public void setLimit(Integer limit) {
            this.limit = limit;
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
            return "PagingDetails{" +
                    "total=" + total +
                    ", offset=" + offset +
                    ", limit=" + limit +
                    ", additionalProperties=" + additionalProperties +
                    '}';
        }
    }







}
