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
package au.org.ala.fielddata.mobile.dao;

import java.util.Arrays;
import java.util.List;

import au.org.ala.fielddata.mobile.model.Species;

public class SpeciesDAO {

	public List<Species> speciesForSurvey(int surveyId) {
		Species[] species = new Species[2];
        species[0] = new Species("Species 1", "Common name 1", 1);
        species[1] = new Species("Species 2", "Common name 2", 3);
        
        return Arrays.asList(species);
	}
	
}
