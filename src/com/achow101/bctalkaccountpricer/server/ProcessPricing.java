package com.achow101.bctalkaccountpricer.server;

import java.util.ArrayList;
import java.util.List;

import com.achow101.bctalkaccountpricer.shared.QueueRequest;

public class ProcessPricing {
	
	public static List<QueueRequest> requestList = new ArrayList<QueueRequest>();
	
	public void run()
	{
		//TODO: Receive request from PricingServiceImpl thread
		
		//TODO: Add received request to requestList
		
		// Infinite Loop for getting the data
		while(true)
		{
			// If the list isn't empty, process requests
			if(requestList.size() != 0)
			{
				// Get first item in list to process
				QueueRequest req = requestList.get(0);
				req.setProcessing(true);
				
				// Price the request
				AccountPricer pricer = new AccountPricer(req.getUid());
				req.setResult(pricer.getAccountData());
				req.setDone(true);
				
				//TODO: Pass the data back to PricingServiceImple thread
			}
			// Otherwise wait 2.5 seconds
			else
			{
				try {
					Thread.sleep(2500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		
		}
	}

}
