/**
 * @author jkrishnan
 *
 */
/**
 * SolarPlantMonitor is a library that exposes interfaces to 
 * 	1. Return the list of components for an installation
 *	2. Return the list of channels for a given component
 *	3. Return a channel data point for a given device name and channel name
 *
 */
package com.aurora.solar.monitor;

import java.io.InputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;

import com.aurora.solar.common.io.StreamUtils;
import com.aurora.solar.common.net.HttpUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class SolarPlantMonitor implements SolarPlantProvider {
	private static final String CONTENT_TYPE_HTTP_HEADER = "Content-Type";
	private static final String WWW_FORM_ENCODED_URL = "application/x-www-form-urlencoded";
	private static final long REQUEST_TIMEOUT = 10000;
	public static final String UTF_8_ENCODING = "UTF-8";
	
	/*	NOTE :
	 * 	The following three properties should be read from a configuration file or any other secure persistent storage. 
	 *	Due to time constraints it has been hard coded here.
	*/
	private static final String MONITORING_URI = "https://aurorasolar-monitoring-task.herokuapp.com";
	private static final String m_userName = "8143AAC";
	private static final String m_strPasswd = "winterIsComing";
	
	/**
	 * 
	 */
	public SolarPlantMonitor() {	}
	
	/*
	 * This method provides the password hashed using the MD5 algorithm as a Hex Digest  
	 */
	private String getMD5HashOfPasswd() {
		MessageDigest m = null;
		try {
			m = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		m.update(m_strPasswd.getBytes(),0,m_strPasswd.length());
		return new BigInteger(1,m.digest()).toString(16);		
	}

	/*
	 * A generic method to execute the queries required to get monitoring data from the REST server
	 */
	private String executeQuery(String strQuery,String strQueryParams) {
		
		if(strQuery == null || strQuery.isEmpty()) {
			System.out.println("ERROR : Cannot execute without a query!!");
			return "";
		}
		
		final String methodName = "executeQuery() - ";
		HttpURLConnection connection = null;
		InputStream inputStream = null;
		String strRresponse = "";
		
		//Construct the body of the POST
		String strContentBody = "candidate=" +  m_userName + "&password=" + getMD5HashOfPasswd();
		
		//Add any extra query parameters that is passed
		if(strQueryParams != null && !strQueryParams.isEmpty()) {
			strContentBody = strContentBody.concat(strQueryParams);
		}
		
		//Query for Plant overview 
		String requestURI = MONITORING_URI + strQuery;
		try {
			byte[] data = strContentBody.getBytes(UTF_8_ENCODING);
			URL url = new URL( requestURI );
			
			connection = HttpUtils.createConnection(url , HttpUtils.POST_REQUEST_METHOD, (int)REQUEST_TIMEOUT, (int)REQUEST_TIMEOUT);

			connection.setDoOutput(true);
			connection.setDoInput(true);

			connection.setRequestProperty(CONTENT_TYPE_HTTP_HEADER, WWW_FORM_ENCODED_URL);

			//Here goes the query!!
			connection.getOutputStream().write(data);
			strRresponse =  StreamUtils.streamToString(connection.getInputStream());

		}catch (Exception e) {
			System.out.println(methodName + String.format("requestURI [%s] FAILED.", requestURI));
			throw new RuntimeException(methodName + HttpUtils.getErrorMessage(e, connection));
		} finally {
			StreamUtils.close(inputStream);
			HttpUtils.disconnect(connection);
		}
		
		inputStream = null;
		
		return strRresponse;
	}
	
	/*
	 * Helper function to return the component entries of a plant
	 */
	private JSONArray getComponents() {
		String strRequestURI = "/plant_overview";
		String strAdditionalQueryParams="";
		JSONArray componentEntries = null;
		
		String strQueryResponse = executeQuery(strRequestURI,strAdditionalQueryParams);
		if(strQueryResponse.isEmpty()) {
			System.out.println("ERROR : Could not execute the query to get components!!");
			return componentEntries;			
		}
		
		//Lets parse the JSON output from our query
		JSONParser parser = new JSONParser();

		try {
			//We received an array of component entries
			componentEntries = (JSONArray) parser.parse(strQueryResponse);
		} catch (ParseException e) {
			e.printStackTrace();
		} 
		
		return componentEntries;
	}
	
	/* (non-Javadoc)
	 * @see com.aurora.solar.monitor.SolarPlantProvider#getComponentList()
	 * 
	 * This method queries the aurora solar monitoring server and returns a list of components 
	 * for that specific installation
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ArrayList<String> getComponentList() {
		//Prepare an ArrayList to return to the caller
		ArrayList componentList = new ArrayList();

		JSONArray componentEntries = getComponents();
		
		if(componentEntries == null) {
			//Error log is printed by the getComponents() method. No need to repeat here.
			return componentList;			
		}

		Iterator itr = componentEntries.iterator();
		while(itr.hasNext()) {
			JSONObject json = (JSONObject)itr.next();
			componentList.add(json.get("device_name")); //Add each device to the list
		}

		return componentList; //We are done!!
	}

	/* (non-Javadoc)
	 * @see com.aurora.solar.monitor.SolarPlantProvider#gtChannelList(java.lang.String)
	 * 
	 * This method returns the channel name list for a given component
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ArrayList<String> gtChannelList(String strComponentName) {
		//Prepare an ArrayList to return to the caller
		ArrayList<String> channelList = new ArrayList();
		
		if( strComponentName == null || strComponentName.isEmpty()) {
			System.out.println("ERROR : Cannot get channels without a component!!");
			return channelList;			
		}
		
		JSONArray componentEntries = getComponents();
		
		if(componentEntries == null) {
			//Error log is printed by the getComponents() method. No need to repeat here.
			return channelList;			
		}
		
		Iterator itr = componentEntries.iterator();
		while(itr.hasNext()) {
			JSONObject json = (JSONObject)itr.next();
			String strDeviceName = (String)json.get("device_name");
			if(strDeviceName.equalsIgnoreCase(strComponentName)) {
				JSONArray channelArray = (JSONArray)json.get("channels");
				for(int i=0; i<channelArray.size(); i++) {
					JSONObject channelObj = (JSONObject)channelArray.get(i);
					channelList.add(channelObj.get("name").toString());
				}
			}
		}
		return channelList;
	}

	/* (non-Javadoc)
	 * @see com.aurora.solar.monitor.SolarPlantProvider#getChannelDataPoint(java.lang.String, java.lang.String)
	 * 
	 * This method returns a channel data point for a given device name and channel name
	 */
	public String getChannelDataPoint(String strDeviceName, String strChannelName) {
		String strRequestURI = "/entity_data";
		String strAdditionalQueryParams="&entity=" + strDeviceName + "&channel=" + strChannelName;
		String strQueryResponse = executeQuery(strRequestURI,strAdditionalQueryParams);

		return strQueryResponse;
	}
}
