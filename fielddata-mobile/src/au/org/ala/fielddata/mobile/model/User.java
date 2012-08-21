package au.org.ala.fielddata.mobile.model;

import com.google.gson.annotations.SerializedName;

public class User {

	public String firstName;
	public String lastName;
	public String emailAddress;
	@SerializedName("server_id")
	public Integer serverId;
	
	public String toString() {
		StringBuilder user = new StringBuilder();
		user.append("Name: ").append(firstName).append(lastName).append(", email: ").append(emailAddress);
		return user.toString();
	}
	
}
