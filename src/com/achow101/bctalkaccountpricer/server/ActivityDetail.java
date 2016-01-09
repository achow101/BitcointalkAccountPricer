package com.achow101.bctalkaccountpricer.server;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class ActivityDetail
	{
		private long startTimestamp;
		private long endTimestamp;
		private int numPosts;
		private int totalPosts;
		private boolean merch;
		
		public ActivityDetail(long startTimestamp, long endTimestamp, int numPosts, int totalPosts, boolean merch)
		{
			this.startTimestamp = startTimestamp;
			this.endTimestamp = endTimestamp;
			this.numPosts = numPosts;
			this.totalPosts = totalPosts;
			this.merch = merch;
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
			
			if(merch)
			{
				DecimalFormat df = new DecimalFormat("#0.00");
				return formattedStartDate + " - " + formattedEndDate + ": " + df.format(((double)numPosts / totalPosts) * 100) + "% of activity";
			}
			else		
				return formattedStartDate + " - " + formattedEndDate + ": " + numPosts + " Posts" ;
		
		}
	}
