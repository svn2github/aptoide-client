
package cm.aptoidetv.pt.Model;

import com.google.api.client.util.Key;

import java.util.ArrayList;


public class Comment {

    @Key
    private Number id;
    @Key
    private String lang;
    @Key
    private String reponame;
    @Key
    private String subject;
    @Key
    private String text;
    @Key
    private String timestamp;
    @Key
    private String useridhash;
    @Key
    private String username;
    @Key
    private Number answerto;
    @Key
    private Number votes;

    private ArrayList<Comment> subComments = new ArrayList<Comment>();
    private boolean isShowingSubcomments;

 	public Number getId(){
		return this.id;
	}
	public void setId(Number id){
		this.id = id;
	}
 	public String getLang(){
		return this.lang;
	}
	public void setLang(String lang){
		this.lang = lang;
	}
 	public String getReponame(){
		return this.reponame;
	}
	public void setReponame(String reponame){
		this.reponame = reponame;
	}
 	public String getSubject(){
		return this.subject;
	}
	public void setSubject(String subject){
		this.subject = subject;
	}
 	public String getText(){
		return this.text;
	}
	public void setText(String text){
		this.text = text;
	}
 	public String getTimestamp(){
		return this.timestamp;
	}
	public void setTimestamp(String timestamp){
		this.timestamp = timestamp;
	}
 	public String getUseridhash(){
		return this.useridhash;
	}
	public void setUseridhash(String useridhash){
		this.useridhash = useridhash;
	}
 	public String getUsername(){
		return this.username;
	}
	public void setUsername(String username){ this.username = username;	}
    public Number getAnswerTo() { return answerto; }
    public ArrayList<Comment> getSubComments() {
        return subComments;
    }
    public void addSubComment(Comment subComment) {
        subComments.add(subComment);
    }
    public boolean hasSubComments() {
        return subComments.size() != 0;
    }
    public boolean isShowingSubcomments() {
        return isShowingSubcomments;
    }
    public void setShowingSubcomments(boolean isShowingSubcomments) { this.isShowingSubcomments = isShowingSubcomments; }
    public void clearSubcomments() { subComments.clear(); }
    public Number getVotes() {  return votes; }
}
