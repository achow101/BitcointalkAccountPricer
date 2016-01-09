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
	}
