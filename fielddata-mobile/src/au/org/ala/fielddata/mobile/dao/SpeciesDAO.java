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
