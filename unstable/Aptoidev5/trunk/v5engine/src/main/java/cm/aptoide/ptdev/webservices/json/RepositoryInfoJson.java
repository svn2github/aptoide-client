
package cm.aptoide.ptdev.webservices.json;

import com.google.api.client.util.Key;

import java.util.List;

public class RepositoryInfoJson{
    @Key("listing")
   	private RepositoryInfoListing listing;
    @Key
   	private String status;

    public List<String> getErrors() {
        return errors;
    }

    @Key
    private List<String> errors;

 	public RepositoryInfoListing getListing(){
		return this.listing;
	}
	public void setListing(RepositoryInfoListing listing){
		this.listing = listing;
	}
 	public String getStatus(){
		return this.status;
	}
	public void setStatus(String status){
		this.status = status;
	}


}
