package cm.aptoidetv.pt.WebServices;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.List;

/**
 * Created by rmateus on 13-11-2014.
 */
public class Response {

    public String status;
    public Responses responses;

    public static class Info {
        public String status;
        public Number time_taken;
    }

    public static class Responses {

        public GetStore getStore;
        public ListApps listApps;

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GetStore {

        public Datasets datasets;


        public static class StoreMeta {

            public Info info;
            public StoreMetaData data;

        }

        public static class Categories {
            public Info info;

            public Data<Category> data;

            public static class Category{

                public Number id;
                public String ref_id;
                public String parent_ref_id;
                public Number apps_count;
                public Number parent_id;
                public String name;
                public String graphic;

            }

        }

        public static class Widgets {
            public Info info;
            public Data<Widget> data;


            public static class Widget {

                public String type;
                public String name;
                public WidgetData data;


                public static class WidgetCategory {
                    public Number id;
                    public String ref_id;
                    public Number parent_id;
                    public String parent_ref_id;
                    public Number apps_count;
                    public String name;
                    public String graphic;
                }


                public static class WidgetData {
                    public String ref_id;
                    public List<WidgetCategory> categories;

                }

            }

        }

        public static class StoreMetaData {

            public Number id;
            public String name;
            public Number apps_count;
            public Number downloads;
            public String avatar;
            public String theme;
            public String description;
            public String view;

        }

        public static class Datasets {

            public Categories categories;
            public Widgets widgets;
            public StoreMeta meta;

        }

    }

    public static class Data<T>{

        public int total;
        public int offset;
        public int limit;
        public List<T> list;

    }

    public static class ListApps {


        public Info info;
        public Datasets datasets;

        public static class Category {
            public Info info;
            public Data<Apk> data;

        }

        public static class Apk {
            public Number id;
            public String name;

            @JsonProperty("package")
            public String packageName;
            public String vername;
            public String md5sum;
            public Number downloads;
            public Number rating;
            public String icon;
            public String graphic;

            @Override
            public String toString() {
                return name + " " + vername;
            }


        }

        public static class Datasets {

            private HashMap<String, Category> dataset = new HashMap<String, Category>();

            @JsonAnySetter
            public void setDynamicProperty(String name, Category object) {
                dataset.put(name, object);
            }

            public HashMap<String, Category> getDataset() {
                return dataset;
            }

            public void setDataset(HashMap<String, Category> dataset) {
                this.dataset = dataset;
            }


        }

    }
}
