package au.org.ala.fielddata.mobile.service;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import android.content.Context;
import au.org.ala.fielddata.mobile.pref.Preferences;


public class LoginService extends WebServiceClient {

	
	private String loginUrl = "/webservice/user/validate.htm";
	
	
	public LoginService(Context ctx) {
		super(ctx);
	}
	
	public LoginResponse login() {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.set("portalName", "Condamine NRM");
		params.set("username", "admin");
		params.set("password", "cambia401FD");
		
		
		String url = serverUrl + loginUrl;
		
		
		LoginResponse result = getRestTemplate().postForObject(url, params, LoginResponse.class);
		System.out.println(result);
		
		new Preferences(ctx).setFieldDataSessionKey(result.ident);
		
		return result;
	}

}
