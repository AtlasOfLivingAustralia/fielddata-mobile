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

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import android.content.Context;
import android.util.Log;
import au.org.ala.fielddata.mobile.dao.GenericDAO;
import au.org.ala.fielddata.mobile.model.Record;
import au.org.ala.fielddata.mobile.model.Species;
import au.org.ala.fielddata.mobile.model.Survey;
import au.org.ala.fielddata.mobile.pref.Preferences;

public class FieldDataService extends WebServiceClient {

	private String syncUrl = "/survey/upload";

	private String surveyUrl = "/survey/list?ident=";

	private String surveyDetails = "/survey/get/%d?ident=%s&surveysOnDevice=%s";
	
	private String pingUrl = "/survey/ping";
	
	private String downloadUrl = "/survey/download?uuid=";
	

	private String ident;

	public FieldDataService(Context ctx) {
		super(ctx);
		this.ident = new Preferences(ctx).getFieldDataSessionKey();
	}

	public void sync(List<Record> records) throws Exception {

		MultiValueMap<String, Object> params = new LinkedMultiValueMap<String, Object>();
		params.set("ident", ident);
		params.set("inFrame", "false"); // Setting this parameter prevents the
										// server from assuming a jsonp request.
	    params.set("syncData", records);

		String url = serverUrl + syncUrl;

		RestTemplate restTemplate = getRestTemplate();
		SyncRecordsResponse result = restTemplate.postForObject(url, params,
				SyncRecordsResponse.class);
		System.out.println(result);
	}

	public List<Survey> downloadSurveys() {

		String url = serverUrl + surveyUrl + ident;

		RestTemplate restTemplate = getRestTemplate();
		restTemplate.getMessageConverters().add(
				new StringHttpMessageConverter());
		UserSurveyResponse[] result = restTemplate.getForObject(url,
				UserSurveyResponse[].class);

		url = serverUrl + surveyDetails;
		List<Survey> surveys = new ArrayList<Survey>();
		List<Species> speciesList;
		
		for (UserSurveyResponse userSurvey : result) {
			String downloadedSurveys = jsonSurveyIds(surveys);
			DownloadSurveyResponse surveyResponse = restTemplate.getForObject(
					String.format(url, userSurvey.id, ident, downloadedSurveys),
					DownloadSurveyResponse.class);
			Survey survey = new Survey();

			survey.server_id = surveyResponse.details.id;
			survey.lastSync = System.currentTimeMillis();
			survey.name = surveyResponse.details.name;
			survey.attributes = surveyResponse.attributes;
			survey.recordProperties = surveyResponse.recordProperties;
			survey.map = surveyResponse.map;
			survey.description = surveyResponse.details.description;

			surveys.add(survey);

			speciesList = surveyResponse.indicatorSpecies;
			GenericDAO<Species> dao = new GenericDAO<Species>(ctx);
			StorageManager manager = new StorageManager(ctx);
			for (Species species : speciesList) {
				dao.save(species);
				try {
					// Instruct the cache manager to download and cache the file
					manager.getProfileImage(species);
				} catch (Exception e) {
					Log.e("Service", "Error downloading profile image", e);
				}

			}
		}

		return surveys;
	}
	
	private String jsonSurveyIds(List<Survey> surveys) {
		JSONArray ids = new JSONArray();
		for (Survey survey : surveys) {
			ids.put(survey.server_id);
		}
		return ids.toString();
	}
	
	public boolean ping(int timeoutInMillis) {
		
		InputStream in = null;
		HttpURLConnection conn = null;
		boolean canPing = true;
		try {
			URL url = new URL(serverUrl + pingUrl);
			conn = (HttpURLConnection)url.openConnection();
			conn.setConnectTimeout(timeoutInMillis);
			//conn.setReadTimeout(timeoutInMillis);
			in = conn.getInputStream();
			while (in.read() != -1) {}
			
		} catch (Exception e) {
			canPing = false;
		} finally {
			try {
				close(in);
				close(conn);
			} catch (Exception e) {
				canPing = false;
			}
		}
		return canPing;	
	}

	public void downloadSpeciesProfileImage(String uuid, File destinationFile) {
		InputStream in = null;
		OutputStream out = null;
		HttpURLConnection conn = null;
		
		final int BUFFER_SIZE = 8192;
		byte[] buffer = new byte[BUFFER_SIZE];
		try {
			URL url = new URL(serverUrl + downloadUrl+uuid);
			conn = (HttpURLConnection)url.openConnection();
			in = conn.getInputStream();
			out = new FileOutputStream(destinationFile);
			int count = in.read(buffer);
			while (count >= 0) {
				out.write(buffer, 0, count);
				count = in.read(buffer);
			}
		} catch (Exception e) {
			throw new ServiceException(e);
		} finally {
			try {

				
				close(in);
				close(conn);
				close(out);
				
			} catch (Exception e) {
				Log.e("FieldDataService", "Error downloadSpeciesProfileImage: ", e);
				throw new ServiceException(e);
			}
		}
	}

}
