package au.org.ala.fielddata.mobile.service;

import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import android.content.Context;
import au.org.ala.fielddata.mobile.pref.Preferences;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class WebServiceClient {
	protected String serverUrl;
	protected Context ctx;
	
	public WebServiceClient(Context ctx) {
		this.ctx = ctx;
		serverUrl = new Preferences(ctx).getFieldDataServerUrl();
	}
	
	/**
	 * The FieldData web services tend to accept regular HTTP parameters, some of which are JSON encoded.
	 * 
	 */
	protected RestTemplate getRestTemplate() {
		
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getMessageConverters().add(new FormHttpMessageConverter());
		restTemplate.getMessageConverters().add(new GsonHttpMessageConverter(getGson()));
		
		return restTemplate;
	}
	
	protected Gson getGson() {
		return new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.IDENTITY).create();
	}
}
