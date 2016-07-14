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

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class AccountPricer {
	
	// TODO: Change password before compiling
	final String ACCOUNT_NAME = "accountbot";
	final String ACCOUNT_PASS = "NOT THE RIGHT PASSWORD";
	
	private int userId = 3;
	private boolean merch = false;
	private int failCount = 0;
	private int failRetries = 5;
	private QueueRequestDB req;
	
	public AccountPricer(QueueRequestDB req)
	{
		this.userId = req.getUid();
		this.merch = req.isMerchant();
		this.req = req;
	}

	public String[] getAccountData() {
		
		// Output stuff
		String[] output = new String[8];
		
		// Summary vars
		String postsString = null;
		String username = null;
		String rank = "";
		
		
		while(true)
		{
			// Retrieve summary page
			try {
				
				// Wait a second to prevent ip bans
				Thread.sleep(1010);
				
				// Get the profile summary page
				Document profileSummary = Jsoup.connect("https://bitcointalk.org/index.php?action=profile;u=" + userId + ";sa=summary").get();
				
				// Select profile table element
				Element profileTable = profileSummary.select("table.bordercolor[align=center]").get(0);
				
				// Get elements of the profile
				Elements profileElements = profileTable.select("td.windowbg > table > tbody > tr > td");
				
				// Find the right elements
				Element lastProfileElem = profileElements.get(1);
				for(Element elem : profileElements)
				{
					// username
					if(lastProfileElem.text().contains("Name:"))
					{
						username = elem.text();
					}
					
					// position
					if(lastProfileElem.text().contains("Position:"))
					{
						rank = elem.text();
					}
					lastProfileElem = elem;
				}
				
				break;
				
			} catch (Exception e) {
				e.printStackTrace();
				try {
					failCount++;
					if(failCount >= failRetries)
					{
						System.out.println(req.getToken() + " has failed");
						output[1] = "Request Failed";
						return output;
					}
					Thread.sleep(20000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}

		// Wait one second
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
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
			case -2:
				System.out.println(req.getToken() + " has failed");
				output[1] = "Request Failed";
				return output;
		}

		// Get the number of pages
		int pages = 0;
		try {
			// Get the page
			Document onePostPage = Jsoup.connect("https://bitcointalk.org/index.php?action=profile;u=" + userId + ";sa=showPosts;").get();

			// Get the navpages elements
			Element headerNavBar = onePostPage.select("table[width=85%][cellspacing=1][cellpadding=4][align=center].bordercolor").first();
			Elements headerNavPages = headerNavBar.select("tr.catbg3 > td > a.navPages");

			// Get the page if there is only one page of posts
			if(headerNavPages.size() == 0)
			{
				headerNavPages = headerNavBar.select("tr.catbg3 > td > b");
			}

			// Get the last one and set the pages
			Element lastPage = headerNavPages.last();
			pages = Integer.parseInt(lastPage.text());

		} catch (Exception e) {
			e.printStackTrace();
			try {
				failCount++;
				if(failCount >= failRetries)
				{
					System.out.println(req.getToken() + " has failed");
					output[1] = "Request Failed";
					return output;
				}
				Thread.sleep(20000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		// Wait one second
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// other vars
		List<Long> dates = new ArrayList<Long>();
		int postcount = 0;
		int goodPosts = 0;
		List<Section> postsSections = new ArrayList<Section>();
		List<Address> allAddresses = new ArrayList<Address>();
		
		// get posts from each page of 20 posts
		for(int i = 0; i < pages; i++)
		{
			failCount = 0;
			while(true)
			{
				// get page
				try {
					
					// get post page
					Document postPage = Jsoup.connect("https://bitcointalk.org/index.php?action=profile;u=" + userId + ";sa=showPosts;start=" + (i * 20)).get();
					
					// get the table of each post
					Elements postTables = postPage.select("table[width=100%][cellpadding=4][align=center].bordercolor");
					
					// get the post sections of each post
					for(Element postTable : postTables)
					{					
						// get post header and get the text
						Element postHeader = postTable.select("tr.titlebg2").get(0);
						Element postDate = postHeader.select("td.middletext").get(1);
						String dateStr = postDate.text().substring(4);
						
						// Parse date string and get unix timestamp
						SimpleDateFormat fmt = new SimpleDateFormat("MMMM dd, yyyy, hh:mm:ss a");
						Date date;
						if(dateStr.contains("Today"))
						{
							date = new Date();
							String currentDateStr = fmt.format(date);
							dateStr = dateStr.replace("Today at", currentDateStr.substring(0, currentDateStr.lastIndexOf(",") + 1));
						}
						date = fmt.parse(dateStr);
						long unixtime = date.getTime() / 1000;
						dates.add(unixtime);
						
						// Get the board
						Element postBoard = postHeader.select("td.middletext").get(0);
                        Elements postLinks = postBoard.select("a[href]");
                        String boardString = "";
                        for(Element postLink : postLinks)
                        {
                            if(!postLink.attr("href").contains("index.php?topic="))
                            {
                                boardString += " / " + postLink.text();
                            }
                        }
						boolean sectionExists = false;
						int sectionIndex = -1;
						for(int j = 0; j < postsSections.size(); j++)
						{
							if(boardString.equals(postsSections.get(j).getName()))
							{
								sectionExists = true;
								sectionIndex = j;
								break;
							}
						}
						if(postsSections.size() == 0 || !sectionExists)
						{
							postsSections.add(new Section(boardString, merch));
							postsSections.get(postsSections.size() - 1).incrementPostCount();
						}
						else
						{
							postsSections.get(sectionIndex).incrementPostCount();
						}

						// TODO: Add the actual boards that are going to be not counted for activity
						boolean postIsCounted = true;
						if(boardString.contains("Games and rounds") && false) {
							dates.remove(postcount); // Remove the post from the dates list
							postIsCounted = true; // TODO: Remove this if posts in those sections are counted in post count
						}
						
						// get post body and get the html
						Element postBody = postTable.select("tr > td.windowbg2 > div.post").get(0);
						
						// Remove the quote classes
						postBody.select("div.quote, div.quoteheader").remove();
						String postString = postBody.text();
						
						// Count the post
						if(postIsCounted) {
							if (postString.length() >= 75)
								goodPosts++;
							postcount++;
						}
						
						// Get the post URL
						Element postURLElement = postHeader.select("td.middletext > a[href]").last();
						String postURL = postURLElement.attr("href");
						
						// retrieve addresses in the text
						Pattern pattern = Pattern.compile("[13][a-km-zA-HJ-NP-Z0-9]{26,33}");
						Matcher matcher = pattern.matcher(postString);
						while(matcher.find())
						{
							String address = matcher.group();
							boolean hasAddr = false;
							for(Address addr : allAddresses)
							{
								if(addr.getAddr().equals(address))
								{
									addr.setDateURL(dateStr, postURL);
									hasAddr = true;
									break;
								}
							}
							if(!hasAddr)
							{
								allAddresses.add(new Address(address, postURL, dateStr));
							}
						}
					}
					
					// wait so ip is not banned.
					Thread.sleep(1010);
					
					break;
					
				} catch (Exception e)
				{
					e.printStackTrace();
					try {
						failCount++;
						if(failCount >= failRetries)
						{
							System.out.println(req.getToken() + " has failed");
							output[1] = "Request Failed";
							return output;
						}
						Thread.sleep(20000);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
			
		}
		
		// Remove invalid addresses
		List<Address> addresses = new ArrayList<Address>();
		for(Address addr : allAddresses)
		{
			if(addr.isValid())
			{
				addresses.add(addr);
			}
		}
		
		// Put addresses into a string array
		String[] postedAddresses = new String[addresses.size() + 2];
		postedAddresses[0] = "<b>Addresses posted in non-quoted text</b>";
		postedAddresses[1] = "<b>(May inclue addresses not actually owned by user)</b>";
		for(int i = 2; i < postedAddresses.length; i++)
		{
			postedAddresses[i] = addresses.get(i - 2).toString();
		}

		// Sort posts sections by number of posts in descending order
		Collections.sort(postsSections, new Comparator<Section>() {
			public int compare(Section o1, Section o2) {
				return o1.getNumPosts() - o2.getNumPosts();
			}
		});
        Collections.reverse(postsSections);

		// Put posts info into a string array
		String[] postsBreakdown = new String[postsSections.size() + 1];
		postsBreakdown[0] = "<b>Post Sections Breakdown</b>";
		for(int i = 1; i < postsBreakdown.length; i++)
		{
			postsSections.get(i - 1).setTotalPosts(postcount);
			postsBreakdown[i] = postsSections.get(i - 1).toString();
		}
		
		// Calculate potential activity, and number of posts in each two week period
		int potActivity = 0;
		long cur2week = 0;
		long prev2week = 0;
		int postsInWeek = 0;
		List<ActivityDetail> activityDetail = new ArrayList<ActivityDetail>();
		for(int i = dates.size() - 1; i >= 0; i--) {
			if (dates.get(i) != 0) {
				cur2week = dates.get(i) - (dates.get(i) % 1210000);
				if (cur2week != prev2week) {
					potActivity += 14;
					activityDetail.add(new ActivityDetail(prev2week, prev2week + 1210000L, postsInWeek, postcount, merch));
					postsInWeek = 0;
				}

				prev2week = cur2week;
				postsInWeek++;
			}
		}
		
		// Add most recent 2 week period
		if(prev2week + 1210000 < System.currentTimeMillis() / 1000L)
			cur2week = prev2week + 1210000;
		else
			cur2week = -1;
		activityDetail.add(new ActivityDetail(prev2week, cur2week, postsInWeek, postcount, merch));
		
		// Remove extraneous first 2 week period
		activityDetail.remove(0);
		
		// Calculate activity
		int activity = Math.min(activityDetail.size() * 14, postcount);
		
		// Put detailed activity info into a string array
		String[] activityBreakdown = new String[activityDetail.size() + 4];
		activityBreakdown[0] = "<b>Activity periods breakdown</b>";
		for(int i = 1; i <= activityDetail.size(); i++)
		{
			activityBreakdown[i] = activityDetail.get(i - 1).toString();
		}
		
		// Get ranks
		String potRank = getRank(potActivity, true, rank.equals("Legendary"));
		if(!rank.equals("Legendary"))
		{
			rank = getRank(activity, false, false);
		}
		
		// Add next potential rank requirments to end of activity breakdown
		int potActToNext = 0;
		String nextPotRank = "None";
		switch(potRank)
		{
			case "Newbie": 
				potActToNext = 30 - potActivity;
				nextPotRank = "Jr. Member";
				break;
			case "Jr. Member": 
				potActToNext = 60 - potActivity;
				nextPotRank = "Member";				
				break;
			case "Member": potActToNext = 120 - potActivity;
				nextPotRank = "Full Member";
				break;
			case "Full Member": potActToNext = 240 - potActivity;
				nextPotRank = "Sr. Member";
				break;
			case "Sr. Member": potActToNext = 480 - potActivity;
				nextPotRank = "Hero Member";
				break;
			case "Hero Member": potActToNext = 775 - potActivity;
				nextPotRank = "Legendary";
				break;
			case "Legendary *Note: Not guaranteed; The account can become Legendary anywhere between 775 and 1030": potActToNext = 0;
				nextPotRank = "Already the highest Rank";
				break;
		}
		
		activityBreakdown[activityBreakdown.length - 3] = "<b>Next Potential Rank: </b>" + nextPotRank;
		activityBreakdown[activityBreakdown.length - 2] = "<b>Weeks to Next Potential Rank: </b>" + (int)(Math.ceil(potActToNext/7.0));
		activityBreakdown[activityBreakdown.length - 1] = "<b>Potential Activity to next Potential Rank: </b>" + potActToNext;
		
		// Get post quality
		double postRatio = (double)goodPosts / postcount;
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
		
		// Get price
		double price = estimatePrice(activity, potActivity, postRatio, trustScore);
		
		// Format price
		DecimalFormat dfmt = new DecimalFormat("##,###,##0.00000000");
		
		// Merchants see different stuff
		if(merch)
		{
			postcount = (postcount / 20) * 20;
			activity = (activity / 20) * 20;
			potActivity = (potActivity / 20) * 20;
			dfmt.applyPattern("##,###,##0.000");
			dfmt.setRoundingMode(RoundingMode.HALF_UP);
		}
		
		// Write to intial output
		output[0] = (merch) ? "" : "User Id: " + userId;
		output[1] = (merch) ? "" : "Name: " + username;
		output[2] = "Posts: " + postcount + ((merch) ? "+" : "");
		output[3] = "Activity: " + activity + ((merch) ? "+" : "") + " (" + rank + ")";
		output[4] = "Potential Activity: " + potActivity + ((merch) ? "+" : "") + " (Potential " + potRank + ")";
		output[5] = "Post Quality: " + postQuality + " (" + new DecimalFormat("##.00").format(postRatio * 100) + "%)";
		output[6] = "Trust: " + trust;
		output[7] = "Estimated Price: " + dfmt.format(price);
		
		// Combine output with activity breakdown
		output = combineArrays(output, activityBreakdown);
		
		// Combine output with posts Breakdown
		output = combineArrays(output, postsBreakdown);
		
		// Only output if it is not merchant
		if(!merch)
		{
			// Combine output with posted addresses
			output = combineArrays(output, postedAddresses);
		}
		
		return output;
	}
	
	private double estimatePrice(int activity, int potentialActivity, double ratio, int trust)
	{
		double price = 0.0003 * activity;
		
		// Extra potential activity
		int epa = potentialActivity - activity;
		price += epa * 0.00015;
		
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
		failCount = 0;
		while(true)
		{				
			try{
				
				Connection.Response res;
				
				res = Jsoup.connect("https://bitcointalk.org/index.php?action=login2")
	                    .followRedirects(true)
	                    .data("user", ACCOUNT_NAME)
	                    .data("passwrd", ACCOUNT_PASS)
	                    .data("cookielength", "-1")
	                    .method(Connection.Method.POST)
	                    .execute();
	            Document loggedInDocument = res.parse();
	            
	            String sessId = res.cookie("PHPSESSID");
	            
	            Document profileDoc = Jsoup.connect("https://bitcointalk.org/index.php?action=profile;u=" + userId).cookie("PHPSESSID", sessId).get();
			    
			    Element trustSpan = profileDoc.select("span.trustscore").get(0);
			    String trustColor = trustSpan.attr("style");
			    
			    // Neg trust
			    if(trustColor.contains("color:#DC143C"))
			    {
			    	return -1;
			    }
			    
			    // light green trust
			    if(trustColor.contains("color:#74C365"))
			    {
			    	return 1;
			    }
			    
			    // Dark green trust
			    if(trustColor.contains("color:#008000"))
			    {
			    	return 2;
			    }
			    break;
			}
			catch(Exception e)
			{
				e.printStackTrace();
				try {
					failCount++;
					if(failCount >= failRetries)
					{
						return 0;
					}
					Thread.sleep(20000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
		    
			return 0;
	}
	
	private String getRank(int activity, boolean potential, boolean isLegendary)
	{
		String rank = "";
		if(activity < 30)
		{
			rank = "Newbie";
		}
		else if(activity >=30 && activity < 60)
		{
			rank = "Jr. Member";
		}
		else if(activity >= 60 && activity < 120)
		{
			rank = "Member";
		}
		else if(activity >= 120 && activity < 240)
		{
			rank = "Full Member";
		}
		else if(activity >= 240 && activity < 480)
		{
			rank = "Sr. Member";
		}
		else if(activity >= 480)
		{
			rank = "Hero Member";
		}
		
		if(activity >= 775 && (isLegendary || potential))
		{
			rank = "Legendary";
			if(potential && !isLegendary)
			{
				rank = "Legendary *Note: Not guaranteed; The account can become Legendary anywhere between 775 and 1030";
			}
		}
		return rank;
	}
	
	private String[] combineArrays(String[] a, String[] b){
		int length = a.length + b.length;
		String[] result = new String[length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }
}
