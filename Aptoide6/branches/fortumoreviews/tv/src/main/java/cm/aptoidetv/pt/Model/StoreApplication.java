package cm.aptoidetv.pt.Model;

import java.io.Serializable;
import java.util.List;

public class StoreApplication implements Serializable {


    private Repository repository;

    public List<ApplicationAPK> getPackagenames() {
        return packages;
    }

    private List<ApplicationAPK> packages;

    public StoreApplication() {

    }


    public Repository getRepository() { return repository; }
    public void setRepository(Repository repository) {
        this.repository = repository;
    }


    public static class Repository implements Serializable {
        private String basepath;
        private String iconspath;
        private String webservicespath;
        private String apkpath;
        private String categories;


        public String getBasepath() {
            return basepath;
        }

        public void setBasepath(String basepath) {
            this.basepath = basepath;
        }

        public String getIconspath() {
            return iconspath;
        }

        public void setIconspath(String iconspath) {
            this.iconspath = iconspath;
        }

        public String getWebservicespath() {
            return webservicespath;
        }

        public void setWebservicespath(String webservicespath) {
            this.webservicespath = webservicespath;
        }

        public String getApkpath() {
            return apkpath;
        }

        public void setApkpath(String apkpath) {
            this.apkpath = apkpath;
        }

        public String getCategories() {
            return categories;
        }

        public void setCategories(String categories) {
            this.categories = categories;
        }
    }
}
