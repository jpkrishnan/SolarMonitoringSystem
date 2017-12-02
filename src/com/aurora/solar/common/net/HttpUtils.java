/**
 * 
 */
package com.aurora.solar.common.net;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author jkrishnan
 *
 */
public class HttpUtils {

	public static final String GET_REQUEST_METHOD = "GET";
	public static final String POST_REQUEST_METHOD = "POST";
	public static final String PUT_REQUEST_METHOD = "PUT";
	public static final String DELETE_REQUEST_METHOD = "DELETE";
	
	private HttpUtils() { }
	
	public static HttpURLConnection createConnection(URL url, String requestMethod, int connectionTimeout) {
		return createConnection(url, requestMethod, connectionTimeout, 0);
	}
		
	public static HttpURLConnection createConnection(URL url, String requestMethod, 
			int connectionTimeout, int readTimeout) {
		
		StringBuilder errors = new StringBuilder();
		
		if (url == null) {
			errors.append("No URL argument provided! ");
		}
		
		if (requestMethod == null || requestMethod.isEmpty()) {
			errors.append("Request method argument not specified! ");
		}
		
		if (connectionTimeout < 0) {
			errors.append(String.format("Invalid connection timeout argument: [%s] ", connectionTimeout));
		}

		if (readTimeout < 0) {
			errors.append(String.format("Invalid read timeout argument: [%s] ", readTimeout));
		}
		
		if (errors.length() != 0) {
			throw new IllegalArgumentException(String.format(
				"Unable to connect to URL '%s': %s", url, errors.toString().trim()));
		}
		
		HttpURLConnection connection = null;
		
		try {
			connection = (HttpURLConnection)url.openConnection();
			connection.setRequestMethod(requestMethod);
			connection.setConnectTimeout(connectionTimeout);
			connection.setReadTimeout(readTimeout);
		} catch (Exception e) {
			throw new RuntimeException(String.format(
				"Failed to connect to URL '%s': %s", url, e.getMessage()), e);
		}
		
		return connection;
	}
	
	public static void disconnect(HttpURLConnection connection) {
		if (connection != null) {
			try {
				connection.disconnect();
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
	}
	
	public static String getServerResponseInfo(HttpURLConnection connection) {
		
		String responseInfo = null;
		
		if (connection != null) {
			try {
				int responseCode = connection.getResponseCode();
				String responseMessage = connection.getResponseMessage();
				responseInfo = String.format("Server Response Code: [%s] Response Message: [%s]", 
					responseCode, responseMessage);
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
		
		return responseInfo;
	}
	
	public static String getErrorMessage(Throwable error, HttpURLConnection connection) {
		String errorMessage = getServerResponseInfo(connection);
		return errorMessage != null ? errorMessage : error.getMessage();		
	}

}
