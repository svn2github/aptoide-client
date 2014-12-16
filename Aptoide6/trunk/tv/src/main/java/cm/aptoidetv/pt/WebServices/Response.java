package cm.aptoidetv.pt.WebServices;

import android.util.Log;

import com.fasterxml.jackson.annotation.JsonAnySetter;
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

    public static class Ticket {

        public Ticket() {
        }

        public String id;
        public String status;

        public Number getExpected_time() {
            return expected_time;
        }

        private Number expected_time;


        public void setExpected_time(Number expected_time) throws TicketException, InterruptedException {
            this.expected_time = expected_time;
            Log.d("pois", "Response expected in : " + expected_time);
            Thread.sleep(Math.min(expected_time.intValue(), 5000));
            throw new TicketException();
        }

    }

    public  static class TicketException extends Exception {}

    public static class Responses {

        public GetStore getStore;
        public ListApps listApps;
        public ListStores listStores;

    }


    public static class GetStore {

        public Ticket ticket;
        public Datasets datasets;


        public static class StoreMeta {
            public Ticket ticket;
            public Info info;
            public StoreMetaData data;

        }

        public static class Categories {
            public Info info;
            public Ticket ticket;
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
            public Ticket ticket;
            public Data<Widget> data;


            public static class Widget {

                public String type;
                public String name;
                public String widgetid;
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

            public Widgets widgets;
            public StoreMeta meta;

        }

    }

    public static class Data<T>{

        public int total;
        public int offset;
        public int next;
        public int limit;
        public List<T> list;

    }

    public static class ListStores {

        public Ticket ticket;
        public Info info;
        public Datasets datasets;

        public static class Datasets {

            private HashMap<String, StoreGroup> dataset = new HashMap<String, StoreGroup>();

            @JsonAnySetter
            public void setDynamicProperty(String name, StoreGroup object) {
                dataset.put(name, object);
            }

            public HashMap<String, StoreGroup> getDataset() {
                return dataset;
            }

            public void setDataset(HashMap<String, StoreGroup> dataset) {
                this.dataset = dataset;
            }


        }

        public static class StoreGroup{
            public Info info;
            public Ticket ticket;
            public Data<Store> data;


        }
        public static class Store{
            public Number id;
            public String name;
            public Number apps_count;
            public Number downloads;
            public String avatar;
        }
    }

    public static class ListApps {

        public Ticket ticket;
        public Info info;
        public Datasets datasets;

        public static class Category {
            public Info info;
            public Ticket ticket;
            public Data<Apk> data;

        }

        public static class Apk {
            public Number id;
            public String name;
            public Number store_id;

            public String store_name;

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
