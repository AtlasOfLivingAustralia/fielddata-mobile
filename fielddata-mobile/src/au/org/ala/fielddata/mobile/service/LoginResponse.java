package au.org.ala.fielddata.mobile.service;

import au.org.ala.fielddata.mobile.model.User;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {

	public String ident;
	@SerializedName("portal_id")
	public Integer portalId;

	public User user;

	public String toString() {

		StringBuilder loginResponse = new StringBuilder();
		loginResponse.append("Ident: ").append(ident).append(", portal: ").append(portalId);
		loginResponse.append(", user: "+user.toString());
		
		return loginResponse.toString();
	}
	
}
