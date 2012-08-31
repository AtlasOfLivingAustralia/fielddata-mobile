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

import java.io.Closeable;
import java.net.HttpURLConnection;

import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import android.content.Context;
import android.util.Log;
import au.org.ala.fielddata.mobile.model.Record.StringValue;
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
		// This shouldn't be necessary however I am seeing frequent failures
		// due to recycled closed connections (possibly something I am doing
		// wrong).  This is working around that.
		System.setProperty("http.keepAlive", "false");
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
		GsonBuilder builder = new GsonBuilder();
		builder.setFieldNamingPolicy(FieldNamingPolicy.IDENTITY);
		builder.registerTypeHierarchyAdapter(StringValue.class, new StringValueAdapter(ctx));
		return builder.create();
	}
	
	protected void close(Closeable stream) {
		if (stream != null) {
			try {
				stream.close();
			}
			catch (Exception e) {
				Log.e("WebServiceClient", "Error closing stream: ", e);
			}
		}
	}
	
	protected void close(HttpURLConnection connection) {
		if (connection != null) {
			try {
				connection.disconnect();
			}
			catch (Exception e) {
				Log.e("WebServiceClient", "Error closing connection: ", e);
			}
		}	
	}
}
