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
package au.org.ala.fielddata.mobile;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebView;
import au.org.ala.fielddata.mobile.model.Species;
import au.org.ala.fielddata.mobile.pref.Preferences;
import au.org.ala.fielddata.mobile.ui.SpeciesSelectionListener;

import com.actionbarsherlock.app.SherlockFragmentActivity;

/**
 * Presents a list of species to the user for information purposes.
 */
public class SpeciesListActivity extends SherlockFragmentActivity implements SpeciesSelectionListener {

	private Preferences preferences;
	private boolean fieldGuideLoaded = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.species_list_layout);
		preferences = new Preferences(this);
	}
    
	public void onSpeciesSelected(final Species species) {
		
		final CharSequence[] items = {"Record Observation", "View Field Guide"};
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Species Action");
		builder.setItems(items, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int item) {
		        if (item == 0) {
		        	recordObservation(species);
		        } else {
		        	showFieldGuide(species);
		        }
		    }
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	private void recordObservation(Species species) {
		Intent intent = new Intent(this, CollectSurveyData.class);
		intent.putExtra(CollectSurveyData.SURVEY_BUNDLE_KEY, preferences.getCurrentSurvey());
		intent.putExtra(CollectSurveyData.SPECIES, species.getId());
		startActivity(intent);
	}
	
	private void showFieldGuide(Species species) {
		String fieldDataUrl = new Preferences(this).getFieldDataServerUrl();
		fieldDataUrl += "/fieldguide/taxon.htm?id="+species.server_id;
		
		//Uri uri = Uri.parse(fieldDataUrl);
    	//Intent intent = new Intent(Intent.ACTION_VIEW, uri);
    	//startActivity(intent);
    	
		/*
		StringBuffer content = new StringBuffer("<html><body>");
		try {
			Document doc = Jsoup.connect(fieldDataUrl).get();
			Elements contentDiv = doc.select(".fieldguide_profile_item");
			for (int i=0; i < 20; i++) { //contentDiv.size()
				Element element = contentDiv.get(i);
				content.append(element.html());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		content.append("</body></html>");
		*/
    	WebView webview = new WebView(this);
    	setContentView(webview);
    	webview.loadUrl(fieldDataUrl);
    	//webview.loadData(content.toString(), "text/html", null);
    	fieldGuideLoaded = true;
	}
	
	@Override
	public void onBackPressed() {
		if (fieldGuideLoaded) {
			startActivity(getIntent()); 
			finish();
		} else {
			super.onBackPressed();
		}
	}

    
    
    
    
}
