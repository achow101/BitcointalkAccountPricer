/*
 * Copyright (C) 2016  Andrew Chow
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
