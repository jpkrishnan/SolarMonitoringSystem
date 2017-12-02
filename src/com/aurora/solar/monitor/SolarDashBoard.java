/**
 * 
 */
package com.aurora.solar.monitor;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author jkrishnan
 *
 */
public class SolarDashBoard {

	/**
	 * 
	 */
	public SolarDashBoard() {	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SolarPlantMonitor solarPM = new SolarPlantMonitor();
		System.out.println("--------------------------Component List-----------------------------");
		ArrayList<String> componentList = solarPM.getComponentList();
		Iterator<String> itr = componentList.iterator();
		while(itr.hasNext())
			System.out.println(itr.next());
		
		System.out.println("--------------------------Channel List-----------------------------");
		ArrayList<String> channelList = solarPM.gtChannelList("SI5048EH:1260016316");
		itr = channelList.iterator();
		while(itr.hasNext())
			System.out.println(itr.next());

		System.out.println("--------------------------Channel data point-----------------------------");
		System.out.println(solarPM.getChannelDataPoint("SI5048EH:1260016316", "AptTmRmg"));
	}

}
