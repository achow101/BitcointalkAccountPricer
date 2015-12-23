package com.achow101.bctalkaccountpricer.server;

public class Section
	{
		private String name;
		private int numPosts = 0;
		
		public Section(String name)
		{
			this.name = name;
		}
		
		public void incrementPostCount()
		{
			numPosts++;
		}
		
		public String getName()
		{
			return name;
		}
		
		public String toString()
		{
			return name + ": " + numPosts + " Posts";
		}
	}
