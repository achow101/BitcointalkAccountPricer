package com.achow101.bctalkaccountpricer.server;

import java.util.concurrent.BlockingQueue;

import com.achow101.bctalkaccountpricer.shared.QueueRequest;

public class ProcessPricing implements Runnable {
	
	private static BlockingQueue<QueueRequest> requestsToProcess = Config.requestsToProcess;
	private static BlockingQueue<QueueRequest> processedRequests = Config.processedRequests;
	
	public void run()
	{
		System.out.println("Starting ProcessPricing thread for processing the requests");
		
		// Infinte loop so that it runs indefinitely
		while(true)
		{
			try {
				// Get request from PricingServiceImpl thread
				QueueRequest req = requestsToProcess.take();
				System.out.println("Processing request " + req.getToken());
				req.setProcessing(true);
				
				// Price the request
				AccountPricer pricer = new AccountPricer(req.getUid());
				req.setResult(pricer.getAccountData());
				req.setDone(true);
				System.out.println("Completed processing request " + req.getToken());
				
				//Pass the data back to PricingServiceImpl thread
				processedRequests.put(req);
			
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
}
