package cm.aptoide.ptdev.webservices.json;



import java.util.List;

/**
 * Created by rmateus on 18-02-2014.
 */
public class RepositoryCommentsJson {

    public List<Listing> listing;

    public String status;

    public List<Listing> getListing() {
        return this.listing;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public static class Listing {

        public Number answerto;

        public String apkid;

        public Number id;

        public String lang;

        public String name;

        public String text;

        public String timestamp;

        public String useridhash;

        public String username;

        public String ver;

        public Number vercode;

        public Number votes;

        public Number getAnswerto() {
            return this.answerto;
        }

        public void setAnswerto(Number answerto) {
            this.answerto = answerto;
        }

        public String getApkid() {
            return this.apkid;
        }

        public void setApkid(String apkid) {
            this.apkid = apkid;
        }

        public Number getId() {
            return this.id;
        }

        public void setId(Number id) {
            this.id = id;
        }

        public String getLang() {
            return this.lang;
        }

        public void setLang(String lang) {
            this.lang = lang;
        }

        public String getName() {
            return this.name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getText() {
            return this.text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getTimestamp() {
            return this.timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }

        public String getUseridhash() {
            return this.useridhash;
        }

        public void setUseridhash(String useridhash) {
            this.useridhash = useridhash;
        }

        public String getUsername() {
            return this.username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getVer() {
            return this.ver;
        }

        public void setVer(String ver) {
            this.ver = ver;
        }

        public Number getVercode() {
            return this.vercode;
        }

        public void setVercode(Number vercode) {
            this.vercode = vercode;
        }

        public Number getVotes() {
            return this.votes;
        }

        public void setVotes(Number votes) {
            this.votes = votes;
        }
    }
}
