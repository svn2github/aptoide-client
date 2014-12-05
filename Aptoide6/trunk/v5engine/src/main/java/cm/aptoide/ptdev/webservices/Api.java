package cm.aptoide.ptdev.webservices;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.utils.AptoideUtils;

/**
 * Created by rmateus on 13-11-2014.
 */
public class Api {

    @JsonProperty private ApiParams api_params = new ApiParams();
    @JsonProperty private ApiGlobalParams api_global_params = new ApiGlobalParams();

    public ApiParams getApi_params() {
        return api_params;
    }

    public ApiGlobalParams getApi_global_params() {
        return api_global_params;
    }

    public interface ApiParam{@JsonIgnore String getApiName();}

    public static class ApiParams {

        private Map<String, ApiParam> other = new HashMap<String, ApiParam>();

        @JsonAnyGetter
        public Map<String,ApiParam> getApiParamsMap() {
            return other;
        }

        @JsonAnySetter
        public void set(ApiParam value) {
            other.put(value.getApiName(), value);
        }

    }

    public static class ApiGlobalParams {

        public String mature;
        public String lang;
        public String store_name;
        public String q = AptoideUtils.filters(Aptoide.getContext());

        public void setLang(String lang) {
            this.lang = lang;
        }

        public void setStore_name(String store_name) {
            this.store_name = store_name;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)

    public static class CategoryParam implements DatasetParam{

        @JsonIgnore
        private final String category;

        private Number limit;

        @JsonIgnore
        public CategoryParam(String categoryName) {
            this.category = categoryName;
        }

        @Override
        public String getDatasetName() {
            return category;
        }

        public void setLimit(int limit) {
            this.limit = limit;
        }

        public Number getLimit() {
            return limit;
        }
    }


    public static class ListApps implements ApiParam{

        public int limit;

        public String order_by;
        public String order_dir;

        public DatasetParams datasets_params = new DatasetParams();

        public List<String> datasets = new ArrayList<String>();

        @Override
        public String getApiName() {
            return "listApps";
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class GetStore implements ApiParam {

        public String store_name;


        public List<String> datasets = new ArrayList<String>();

        public DatasetParams datasets_params = new DatasetParams();

        public List<String> getDatasets() {
            return datasets;
        }

        public void addDataset(String dataset) {
            datasets.add(dataset);
        }

        public DatasetParams getDatasets_params() {
            return datasets_params;
        }

        @Override
        public String getApiName() {
            return "getStore";
        }


        public static class CategoriesParams implements DatasetParam {

            public String parent_ref_id;



            public void setParent_ref_id(String parent_ref_id) {
                this.parent_ref_id = parent_ref_id;
            }

            @Override
            public String getDatasetName() {
                return "categories";
            }
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class WidgetParams implements DatasetParam {

            private String context;
            public String widgetid;
            public Number offset;
            public Number limit;

            public String getContext() {
                return context;
            }

            public void setContext(String context) {
                this.context = context;
            }

            @Override
            public String getDatasetName() {
                return "widgets";
            }

            public void setWidgetid(String widgetid) {
                this.widgetid = widgetid;
            }
        }

    }

    public interface DatasetParam{
        @JsonIgnore
        String getDatasetName();
    }

    public static class DatasetParams {


        private Map<String, DatasetParam> other = new HashMap<String, DatasetParam>();


        @JsonAnyGetter
        public Map<String,DatasetParam> any() {
            return other;
        }

        @JsonAnySetter
        public void set(DatasetParam value) {
            other.put(value.getDatasetName(), value);
        }

    }

}
