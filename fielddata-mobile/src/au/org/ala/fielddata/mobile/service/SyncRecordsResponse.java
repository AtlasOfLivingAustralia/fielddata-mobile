package au.org.ala.fielddata.mobile.service;

public class SyncRecordsResponse {
	
	public Integer serverRecordCount;
	public Integer status;
	
	public static class Error {
		public String message;
		public String type;
	}
	
	public String toString() {
		StringBuilder response = new StringBuilder();
		response.append("Status: ").append(status).append(", Count: ").append(serverRecordCount);
		return response.toString();
	}
}
