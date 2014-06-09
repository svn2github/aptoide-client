
package cm.aptoide.ptdev.webservices.json;

import cm.aptoide.ptdev.model.*;
import cm.aptoide.ptdev.model.Error;
import com.google.api.client.util.Key;

import java.util.List;

public class RepositoryInfoJson{
    @Key("listing")
   	private RepositoryInfoListing listing;
    @Key
   	private String status;

    @Key
    private List<Error> errors;

    public List<cm.aptoide.ptdev.model.Error> getErrors() {
        return errors;
    }

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
