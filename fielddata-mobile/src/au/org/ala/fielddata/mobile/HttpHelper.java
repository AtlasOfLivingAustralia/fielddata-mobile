package au.org.ala.fielddata.mobile;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import android.os.Build;

public abstract class HttpHelper {
	
	private String serverUrl = "http://localhost:8080/bdrs-core";
	private String syncUrl = "/webservice/application/clientSync.htm";
	
	public HttpHelper() {
		disableConnectionReuseIfNecessary();
	}
	
	public void doPost() throws Exception {
		BufferedWriter out = null;
		// Turn record to JSON.
		try {
			URL url = new URL(serverUrl+syncUrl);
			HttpURLConnection connection = (HttpURLConnection)url.openConnection();
			try {
				connection.setDoOutput(true);
				connection.setChunkedStreamingMode(0);
				
				connection.setReadTimeout(10000);
				connection.connect();
				
				connection.getOutputStream();
				
				
			}
			finally {
				connection.disconnect();
			}
		}
		catch (Exception e) {
			
		}
		finally {
			if (out != null) {
				out.close();
			}
			
		
		
		}
	}
	
	protected void send(HttpURLConnection connection) {
		
	}
	
	protected void receive(HttpURLConnection connection) {
		
	}
	
	protected abstract void doSend(BufferedOutputStream out);
	
	protected abstract void doReceive(BufferedInputStream in);
	
	private void disableConnectionReuseIfNecessary() {
		  //  Work around pre-Froyo bugs in HTTP connection reuse.
		   if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
		     System.setProperty("http.keepAlive", "false");
		   
		 }
	}
	
}
