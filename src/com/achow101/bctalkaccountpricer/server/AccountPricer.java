/*******************************************************************************
 * Copyright (C) 2015  Andrew Chow
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
 ******************************************************************************/
package com.achow101.bctalkaccountpricer.server;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.gargoylesoftware.htmlunit.CookieManager;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlSpan;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;

public class AccountPricer {
	
	// TODO: Change password before compiling
	final String ACCOUNT_NAME = "accountbot";
	final String ACCOUNT_PASS = "NOT THE RIGHT PASSWORD";
	
	int userId = 3;
	
	public AccountPricer(int uid)
	{
		userId = uid;
	}

	public String[] getAccountData() {
		
		// Output stuff
		String[] output = new String[8];
		
		// Summary vars
		String postsString = null;
		String activityString = null;
		String line;
		String username = null;
		
		// Retrieve summary page
		try {
			URL url = new URL("https://bitcointalk.org/index.php?action=profile;u=" + userId + ";sa=summary");
			InputStream is = url.openStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String prevLine = null;
			
			while((line = br.readLine()) != null)
			{
				// Record number of posts
				if(prevLine != null && prevLine.contains("Posts:"))
				{
					int end = line.indexOf("<", 10);
					postsString = line.substring(9, end);
				}
				
				// record activity
				if(prevLine != null && prevLine.contains("Activity:"))
				{
					int end = line.indexOf("<", 10);
					activityString = line.substring(9, end);
				}
				
				// record username
				if(prevLine != null && prevLine.contains("Name: "))
				{
					int end = line.indexOf("<", 10);
					username = line.substring(9, end);
				}
				
				prevLine = line;
			}
			
			is.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// strings to numbers
		int posts = Integer.parseInt(postsString);
		int activity = Integer.parseInt(activityString);
		
		// get number of pages
		int pages = posts / 20;
		if((posts + 1) % 20 != 0)
			pages++;
		
		// other vars
		long[] dates = new long[posts];
		int postcount = 0;
		int goodPosts = 0;
		
		// get posts from each page of 20 posts
		for(int i = 0; i < pages; i++)
		{
			// get page
			try {
				URL url = new URL("https://bitcointalk.org/index.php?action=profile;u=" + userId + ";sa=showPosts;start=" + (i * 20));
				InputStream is = url.openStream();
				BufferedReader br = new BufferedReader(new InputStreamReader(is));
				
				while((line = br.readLine()) != null)
				{					
					// get date of post
					SimpleDateFormat fmt = new SimpleDateFormat("MMMM dd, yyyy, hh:mm:ss a");
					Date date;
					
					if(line.startsWith("\t\t\t\t\t\t\t\ton:"))
					{
						String dateStr = line.substring(12);
						if(dateStr.contains("Today"))
						{
							date = new Date();
							String currentDateStr = fmt.format(date);
							dateStr = dateStr.replace("<b>Today</b> at", currentDateStr.substring(0, currentDateStr.lastIndexOf(",") + 1));
						}
						date = fmt.parse(dateStr);
						long unixtime = date.getTime() / 1000;
						dates[postcount] = unixtime;
						
					}
					
					// get nonquoted text of post
					if(line.contains("class=\"post\""))
					{
						postcount++;
						int startIndex = line.indexOf(">");
						if(line.contains("class=\"quote"))
						{
							startIndex = line.lastIndexOf("</div>", line.lastIndexOf("</div>") - 1);
						}
						
						String post = line.substring(startIndex, line.lastIndexOf("</div>"));
						
						if(post.length() >= 75)
							goodPosts++;					
					}
				}
				
				is.close();
				
				// wait so ip is not banned.
				Thread.sleep(1500);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		// Calculate potential activity
		int potActivity = 1;
		long cur2week = 0;
		long prev2week = 0;
		for(int i = dates.length - 1; i >= 0; i--)
		{
			cur2week = dates[i] - (dates[i] % 1210000);
			if(cur2week != prev2week)
			{
				potActivity += 14;
			}
			
			prev2week = cur2week;
		}
		
		// Remove initial 1 so that potential activity is correct.
		potActivity--;
		
		// Get post quality
		double postRatio = (double)goodPosts / posts;
		String postQuality = null;
		
		// Excellent
		if(postRatio >= 0.90)
		{
			postQuality = "Excellent";
		}
		
		// Good
		else if(postRatio >= 0.80)
		{
			postQuality = "Good";
		}
		
		// Fair
		else if(postRatio >= 0.65)
		{
			postQuality = "Fair";
		}
		
		// Poor
		else if(postRatio >= 0.50)
		{
			postQuality = "Poor";
		}
		
		// Very Poor
		else if(postRatio < 0.50)
		{
			postQuality = "Very Poor";
		}
		
		// get trust rating
		int trustScore = checkForTrust();
		String trust = null;
		
		switch(trustScore)
		{
			case -1: trust = "Negative, Red Trust";
				break;
			case 0: trust = "Neutral";
				break;
			case 1: trust = "Positive, Light Green Trust";
				break;
			case 2: trust = "Positive, Dark Green Trust";
				break;
		}
		
		// Get price
		double price = estimatePrice(activity, potActivity, postRatio, trustScore);
		
		// Format price
		DecimalFormat dfmt = new DecimalFormat("##,###,##0.00000000");
		
		// Write to output
		output[0] = "User Id: " + userId;
		output[1] = "Name: " + username;
		output[2] = "Posts: " + posts;
		output[3] = "Activity: " + activity;
		output[4] = "Potential Activity: " + potActivity;
		output[5] = "Post Quality: " + postQuality;
		output[6] = "Trust: " + trust;
		output[7] = "Estimated Price: " + dfmt.format(price);
		
		return output;
	}
	
	private double estimatePrice(int activity, int potentialActivity, double ratio, int trust)
	{
		double price = 0.0006 * activity;
		
		// Extra potential activity
		int epa = potentialActivity - activity;
		price += epa * 0.0003;
		
		// Post quality multipliers
		
		// Excellent
		if(ratio >= 0.90)
		{
			price = price * 1.05;
		}
		
		// Good
		else if(ratio >= 0.80)
		{
			price = price * 1.025;
		}
		
		// Fair
		else if(ratio >= 0.65)
		{
			price = price * 1.00;
		}
		
		// Poor
		else if(ratio >= 0.50)
		{
			price = price * 0.975;
		}
		
		// Very Poor
		else if(ratio < 0.50)
		{
			price = price * 0.95;
		}
		
		// Trust Multipliers		
		switch(trust)
		{
			// Neg trust
			case -1: price = price * 0.15;
				break;
			// Neutral trust
			case 0: price = price * 1.00;
				break;
			// positive light green
			case 1: price = price * 1.10;
				break;
			//positive dark green
			case 2: price = price * 1.20;
				break;
		}
		
		return price;
	}
	
	private int checkForTrust()
	{
		try{
			// Get initial web page
			final WebClient webClient = new WebClient();
			CookieManager cookieMan = new CookieManager();
			cookieMan = webClient.getCookieManager();
			cookieMan.setCookiesEnabled(true);
			final HtmlPage loginPage = webClient.getPage("https://bitcointalk.org/index.php");
			final HtmlForm form = (HtmlForm) loginPage.getFirstByXPath("//form[@action='https://bitcointalk.org/index.php?action=login2']");
	
			// login as accountbot
		    final HtmlSubmitInput button = (HtmlSubmitInput) form.getInputsByValue("Login").get(0);
		    final HtmlTextInput textField = form.getInputByName("user");
		    textField.setValueAttribute(ACCOUNT_NAME);
		    final HtmlPasswordInput textField2 = form.getInputByName("passwrd");
		    textField2.setValueAttribute(ACCOUNT_PASS);
		    final HtmlPage postLoginPage = button.click();
		    
		    // Get profile page
		    final HtmlPage profilePage = webClient.getPage("https://bitcointalk.org/index.php?action=profile;u=" + userId);
		    
		    // Get trust rating
		    final HtmlSpan trustSpan = (HtmlSpan)profilePage.getFirstByXPath("/html/body/div/table/tbody/tr/td/table/tbody/tr/td/table/tbody/tr/td//span[@class='trustscore']");
		    
		    String trustString = trustSpan.toString();
		    
		    // Neg trust
		    if(trustString.contains("color:#DC143C"))
		    {
		    	return -1;
		    }
		    
		    // light green trust
		    if(trustString.contains("color:#74C365"))
		    {
		    	return 1;
		    }
		    
		    // Dark green trust
		    if(trustString.contains("color:#008000"))
		    {
		    	return 2;
		    }
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	    
		return 0;
	}
}
