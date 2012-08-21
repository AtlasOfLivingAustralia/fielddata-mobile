/*******************************************************************************
 * Copyright (C) 2010 Atlas of Living Australia
 * All Rights Reserved.
 *  
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *  
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 ******************************************************************************/
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
