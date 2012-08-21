package au.org.ala.fielddata.mobile.model;

public class Attribute extends SurveyProperty {
	
	public static enum AttributeType {
		
		INTEGER("IN", "Integer"),
	    INTEGER_WITH_RANGE("IR", "Integer Range"),
	    DECIMAL("DE", "Decimal"),
	    
	    BARCODE("BC", "Bar Code"),
	    REGEX("RE", "Regular Expression"),
	    
	    DATE("DA", "Date"),
	    TIME("TM", "Time"),

	    STRING("ST", "Short Text"),
	    STRING_AUTOCOMPLETE("SA", "Short Text (Auto Complete)"),
	    TEXT("TA", "Long Text"),
	    
	    HTML("HL", "HTML (Validated)"),
	    HTML_NO_VALIDATION("HV", "HTML (Not Validated)"),
	    HTML_COMMENT("CM", "Comment"),
	    HTML_HORIZONTAL_RULE("HR", "Horizontal Rule"),

	    STRING_WITH_VALID_VALUES("SV", "Selection"),
	    
	    SINGLE_CHECKBOX("SC", "Single Checkbox"),
	    MULTI_CHECKBOX("MC", "Multi Checkbox"),
	    MULTI_SELECT("MS", "Multi Select"),

	    IMAGE("IM", "Image File"),
	    AUDIO("AU", "Audio File"),
	    FILE("FI", "Data File"),

	    SPECIES("SP", "Species"),
	    
	    CENSUS_METHOD_ROW("CR", "Data Matrix Rows"),
	    CENSUS_METHOD_COL("CC", "Data Matrix Columns"),
		
		// record properties
		SPECIES_P("Species", "Species"),
		NUMBER("Number", "Number"),
		POINT("Point", "Point"),
		ACCURACY("Accuracy", "Accuracy"),
		WHEN("When", "When"),
		NOTES("Notes", "Notes");
		
		private String code;
		private String name;
		private AttributeType(String code, String name) {
			this.code = code;
			this.name = name;
		}
		
		public String getCode() {
			return code;
		}
		
		public String getName() {
			return name;
		}
		
		public static AttributeType fromCode(String code) {
			for (AttributeType type : AttributeType.values()) {
				if (type.code.equals(code)) {
					return type;
				}
			}
			return null;
		}
	}
	
	public static class AttributeOption {
		public Integer server_id;
		public Integer weight;
		public String value;
		
		public String toString() {
			return value;
		}
	}
	public String typeCode;
	public AttributeOption[] options;
	
	public AttributeType getType() {
		return AttributeType.fromCode(typeCode);
	}
	
	public boolean isModeratorAttribute() {
		return (scope != null && scope.contains("MODERATION"));
	}
}
