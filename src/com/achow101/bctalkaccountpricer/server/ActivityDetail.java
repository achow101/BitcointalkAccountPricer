package com.achow101.bctalkaccountpricer.server;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class ActivityDetail
	{
		private long startTimestamp;
		private long endTimestamp;
		private int numPosts;
		
		public ActivityDetail(long startTimestamp, long endTimestamp, int numPosts)
		{
			this.startTimestamp = startTimestamp;
			this.endTimestamp = endTimestamp;
			this.numPosts = numPosts;
		}
		
		public String toString()
		{
			SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM d yyyy HH:mm:ss"); 
			sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
			
			// Convert start timestamp to date
			Date startDate = new Date(1000L*startTimestamp);
			String formattedStartDate = sdf.format(startDate);
			
			// Convert end timestamp to date
			String formattedEndDate = "";
			if(endTimestamp == -1)
			{
				formattedEndDate = "Present";
			}
			else
			{
				Date endDate = new Date(1000L*endTimestamp);
				formattedEndDate = sdf.format(endDate);
			}
			
			// Add to array
			return formattedStartDate + " - " + formattedEndDate + ": " + numPosts + " Posts" ;
		
		}
	}
