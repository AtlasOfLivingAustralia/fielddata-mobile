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

import java.util.List;

import android.content.Context;
import android.util.Log;
import au.org.ala.fielddata.mobile.dao.GenericDAO;
import au.org.ala.fielddata.mobile.model.Survey;
import au.org.ala.fielddata.mobile.pref.Preferences;

public class FieldDataService {

	private FieldDataServiceClient webServiceClient;
	private Context ctx;
	
	public FieldDataService(Context ctx) {
		this.ctx = ctx;
		webServiceClient = new FieldDataServiceClient(ctx);
	}

	
	/**
	 * Downloads the Surveys configured in the current Portal and saves
	 * them to the database.
	 * @return a List of Surveys configured for the current Portal.
	 */
	public List<Survey> downloadSurveys() {
		
		List<Survey> surveys = webServiceClient.downloadSurveys();

		if (surveys.size() > 0) {
			Preferences prefs = new Preferences(ctx);
			prefs.setCurrentSurvey(surveys.get(0).server_id);
			prefs.setCurrentSurveyName(surveys.get(0).name);
			
		}
		GenericDAO<Survey> surveyDAO = new GenericDAO<Survey>(ctx);
		for (Survey survey : surveys) {
			
			// If we already have a survey with the same id, replace it.
			Survey existingSurvey = surveyDAO.findByServerId(Survey.class, survey.server_id);
			if (existingSurvey != null) {
				Log.i("FieldDataService", "Replacing survey with id: "+existingSurvey.server_id);
				survey.setId(existingSurvey.getId());
			}
			surveyDAO.save(survey);
		}
		
		return surveys;
	}
	
}
