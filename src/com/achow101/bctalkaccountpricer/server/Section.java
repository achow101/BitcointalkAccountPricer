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

public class Section
	{
		private String name;
		private int numPosts = 0;
		private int totalPosts;
		private boolean merch;
		
		public Section(String name, boolean merch)
		{
			this.name = name;
			this.merch = merch;
		}
		
		public void incrementPostCount()
		{
			numPosts++;
		}
		
		public String getName()
		{
			return name;
		}
		
		public void setTotalPosts(int totalPosts)
		{
			this.totalPosts = totalPosts;
		}
		
		public String toString()
		{
			if(merch)
			{
				DecimalFormat df = new DecimalFormat("#0.00");
				return name + ": " + df.format(((double)numPosts / totalPosts) * 100) + "% of Posts";
			}
			else
				return name + ": " + numPosts + " Posts";
		}

		public int getNumPosts()
		{
			return numPosts;
		}
	}
