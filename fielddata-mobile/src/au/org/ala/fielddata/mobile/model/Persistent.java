package au.org.ala.fielddata.mobile.model;

import com.google.gson.Gson;

public class Persistent  {

	public long created;
	public long updated;
	public Integer id;
	public Integer server_id;
	public long lastSync;
	
	public String asJson() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}
	
	
}
