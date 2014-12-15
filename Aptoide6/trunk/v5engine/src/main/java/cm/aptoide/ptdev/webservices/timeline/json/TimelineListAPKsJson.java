package cm.aptoide.ptdev.webservices.timeline.json;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import cm.aptoide.ptdev.fragments.GenericResponse;

/**
 * Created by asantos on 24-09-2014.
 */
public class TimelineListAPKsJson extends GenericResponse {


    public List<UserApk> getUsersapks() {
        return usersapks;
    }

    
    public List<UserApk> usersapks;



        public static class UserApk {
            public boolean animate;

            public Info getInfo() {
                return info;
            }
            public APK getApk() {
                return apk;
            }
            
            Info info;
            
            APK apk;

            public static class Info {
                public Number getId() {
                    return id;
                }

                public String getUsername() {
                    return username;
                }

                public String getStatus() {
                    return status;
                }
                public boolean isStatusActive() {
                    return status.equals("active");
                }
                public void setStatus(String status) {
                    this.status=status;
                }
                public boolean isOwned() {
                    return owned;
                }
                public void setLikes(Number likes) {
                    this.likes = likes;
                }
                public Number getLikes() {
                    return likes;
                }

                public Number getComments() {
                    return comments;
                }

                public String getAvatar() {
                    return avatar;
                }

                public String getTimestamp() {
                    return timestamp;
                }

                public String getUserliked() {
                    return userliked;
                }
                public void setUserliked(String userliked) {
                    this.userliked=userliked;
                }
                
                Number id;
                
                String username;
                
                String status;


                 boolean owned;


                
                Number likes;
                
                Number comments;
                
                String avatar;
                
                String timestamp;
                
                String userliked;
            }

            public static class APK {
                public String getName() {
                    return name;
                }

                public String getRepo() {
                    return repo;
                }

                public String getPackageName() {
                    return packageName;
                }

                public String getVername() {
                    return vername;
                }

                public Number getVercode() {
                    return vercode;
                }

                public String getMd5sum() {
                    return md5sum;
                }

                public String getTimestamp() {
                    return timestamp;
                }

                public String getAge() {
                    return age;
                }

                public String getIcon() {
                    return icon;
                }

                public String getIcon_hd() {
                    return icon_hd;
                }

                public String getSignature() {
                    return signature;
                }
                
                String name;
                
                String repo;
                @JsonProperty("package")
                String packageName;
                
                String vername;
                
                Number vercode;
                
                String md5sum;
                
                String timestamp;
                
                String age;
                
                String icon;
                
                String icon_hd;
                
                String signature;
            }
        }

}