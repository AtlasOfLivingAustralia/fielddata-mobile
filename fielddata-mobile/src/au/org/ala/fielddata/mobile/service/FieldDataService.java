package au.org.ala.fielddata.mobile.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import android.content.Context;
import au.org.ala.fielddata.mobile.model.Record;
import au.org.ala.fielddata.mobile.model.Survey;
import au.org.ala.fielddata.mobile.pref.Preferences;

import com.google.gson.Gson;


public class FieldDataService extends WebServiceClient {

	private String syncUrl = "/webservice/application/clientSync.htm";
	
	private String surveyUrl = "/webservice/survey/surveysForUser.htm?ident=";
	
	private String surveyDetails = "/webservice/application/survey.htm?ident=%s&sid=%d";
	
	private String ident;
	
	public FieldDataService(Context ctx) {
		super(ctx);
		this.ident = new Preferences(ctx).getFieldDataSessionKey();
	}
	
	public void sync(List<Record> records) throws Exception {
		
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.set("ident", ident);
		params.set("inFrame", "false"); // Setting this parameter prevents the server from assuming a jsonp request.
		params.set("syncData", new Gson().toJson(records));
		
		
		String url = serverUrl + syncUrl;
		
		RestTemplate restTemplate = getRestTemplate();
		restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
		SyncRecordsResponse result = restTemplate.postForObject(url, params, SyncRecordsResponse.class);
		System.out.println(result);
	}
		
	public List<Survey> downloadSurveys() {
		
		String url = serverUrl + surveyUrl + ident;
		
		RestTemplate restTemplate = getRestTemplate();
		restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
		UserSurveyResponse[] result = restTemplate.getForObject(url, UserSurveyResponse[].class);
		
		url = serverUrl + surveyDetails;
		List<Survey> surveys = new ArrayList<Survey>();
		for (UserSurveyResponse userSurvey : result) {
			Survey survey = restTemplate.getForObject(String.format(url, ident, userSurvey.id), Survey.class);
			survey.server_id = survey.details.id;
			survey.lastSync = System.currentTimeMillis();
			survey.name = survey.details.name;
			
			surveys.add(survey);
		}
		
		return surveys;
	}
	
	
}
