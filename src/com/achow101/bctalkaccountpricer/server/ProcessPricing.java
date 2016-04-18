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
				AccountPricer pricer = new AccountPricer(req);
				req.setResult(pricer.getAccountData());
				req.setDone(true);
				req.setCompletedTime(System.currentTimeMillis() / 1000L);
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
