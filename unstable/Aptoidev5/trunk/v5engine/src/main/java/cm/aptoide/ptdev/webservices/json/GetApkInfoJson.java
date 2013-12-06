
package cm.aptoide.ptdev.webservices.json;

import cm.aptoide.ptdev.model.Comment;
import cm.aptoide.ptdev.model.Obb;
import com.google.api.client.util.Key;

import java.util.List;

public class GetApkInfoJson {

    @Key
    private Apk apk;

    @Key
    private List<Comment> comments;

    @Key
    private String latest;

    @Key
    private Likevotes likevotes;

    @Key
    private Meta meta;

    @Key
    private ObbJson obb;

    @Key
    private List<String> permissions;

    @Key
    private List<String> sshots;

    @Key
    private String status;

    //
    public Apk getApk() {
        return this.apk;
    }

    public void setApk(Apk apk) {
        this.apk = apk;
    }

    public List<Comment> getComments() {
        return this.comments;
    }

    public void setComments(List comments) {
        this.comments = comments;
    }

    public String getLatest() {
        return this.latest;
    }

    public void setLatest(String latest) {
        this.latest = latest;
    }

    public Likevotes getLikevotes() {
        return this.likevotes;
    }

    public void setLikevotes(Likevotes likevotes) {
        this.likevotes = likevotes;
    }

    public Meta getMeta() {
        return this.meta;
    }

    public void setMeta(Meta meta) {
        this.meta = meta;
    }

    public ObbJson getObb() {
        return this.obb;
    }

    public void setObb(ObbJson obb) {
        this.obb = obb;
    }

    public List<String> getPermissions() {
        return this.permissions;
    }

    public void setPermissions(List permissions) {
        this.permissions = permissions;
    }

    public List<String> getSshots() {
        return this.sshots;
    }

    public void setSshots(List sshots) {
        this.sshots = sshots;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public static class Apk {
        @Key
        private String md5sum;
        @Key
        private String path;
        @Key
        private Number size;
        @Key
        private Number vercode;
        @Key
        private String vername;

        public String getMd5sum() {
            return this.md5sum;
        }

        public void setMd5sum(String md5sum) {
            this.md5sum = md5sum;
        }

        public String getPath() {
            return this.path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public Number getSize() {
            return this.size;
        }

        public void setSize(Number size) {
            this.size = size;
        }

        public Number getVercode() {
            return this.vercode;
        }

        public void setVercode(Number vercode) {
            this.vercode = vercode;
        }

        public String getVername() {
            return this.vername;
        }

        public void setVername(String vername) {
            this.vername = vername;
        }
    }

    public static class Likevotes {
        @Key private Number dislikes;
        @Key private Number likes;

        public Number getDislikes() {
            return this.dislikes;
        }

        public void setDislikes(Number dislikes) {
            this.dislikes = dislikes;
        }

        public Number getLikes() {
            return this.likes;
        }

        public void setLikes(Number likes) {
            this.likes = likes;
        }
    }

    public static class Malware {
        @Key private String reason;
        @Key private String status;

        public String getReason() {
            return this.reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }

        public String getStatus() {
            return this.status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    public static class Meta {
        @Key private String description;
        @Key private String title;

        public String getDescription() {
            return this.description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getTitle() {
            return this.title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }

    public static class Payment {
        @Key private Number amount;
        @Key private String status;

        public Number getAmount() {
            return this.amount;
        }

        public void setAmount(Number amount) {
            this.amount = amount;
        }

        public String getStatus() {
            return this.status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }


    public static class ObbJson {

        @Key("main")
        private Obb main;
        @Key("patch")
        private Obb patch;

        public Obb getMain() {
            return main;
        }

        public void setMain(Obb main) {
            this.main = main;
        }

        public Obb getPatch() {
            return patch;
        }

        public void setPatch(Obb patch) {
            this.patch = patch;
        }
    }
}
