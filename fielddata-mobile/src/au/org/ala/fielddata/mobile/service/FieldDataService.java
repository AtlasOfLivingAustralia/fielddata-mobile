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
import au.org.ala.fielddata.mobile.model.Species;
import au.org.ala.fielddata.mobile.model.Survey;
import au.org.ala.fielddata.mobile.model.Attribute.AttributeType;
import au.org.ala.fielddata.mobile.pref.Preferences;
import au.org.ala.fielddata.mobile.service.FieldDataServiceClient.SurveysAndSpecies;

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
		
		SurveysAndSpecies results = webServiceClient.downloadSurveys(); 
		List<Survey> surveys = results.surveys;

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
			if ("The Great Koala Count".equals(survey.name)) {
				survey.propertyByType(AttributeType.POINT).addOption("No Map");
			}
			surveyDAO.save(survey);
		}
		
		List<Species> speciesList = results.species;
		GenericDAO<Species> dao = new GenericDAO<Species>(ctx);
		StorageManager manager = new StorageManager(ctx);
		for (Species species : speciesList) {
			Log.d("FieldDataService", "Have species with id: "+species.server_id);
			// If we already have a species with the same id, replace it.
			Species existingSpecies = dao.findByServerId(Species.class, species.server_id);
			if (existingSpecies != null) {
				Log.i("FieldDataService", "Replacing species with id: "+existingSpecies.server_id);
				species.setId(existingSpecies.getId());
			}
			dao.save(species);
			try {
				// Instruct the cache manager to download and cache the file
				manager.getProfileImage(species);
			} catch (Exception e) {
				Log.e("Service", "Error downloading profile image", e);
			}

		}
		
		return surveys;
	}
	
}
