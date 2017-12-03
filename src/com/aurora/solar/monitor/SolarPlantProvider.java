/**
 * 
 */
package com.aurora.solar.monitor;

import java.util.ArrayList;

/**
 * @author jkrishnan
 *
 */
public interface SolarPlantProvider {
	public ArrayList<String> getComponentList();
	public ArrayList<String> getChannelList(String strComponentName);
	public String getChannelDataPoint(String strDeviceName, String strChannelName);
}
