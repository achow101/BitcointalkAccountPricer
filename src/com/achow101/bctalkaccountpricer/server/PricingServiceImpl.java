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

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import com.achow101.bctalkaccountpricer.client.PricingService;
import com.achow101.bctalkaccountpricer.shared.QueueRequest;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;


/**
 * The server-side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class PricingServiceImpl extends RemoteServiceServlet implements
		PricingService, Runnable {
	
	public static List<QueueRequest> waitingRequests = new ArrayList<QueueRequest>();
	public static List<QueueRequest> completedRequests = new ArrayList<QueueRequest>();

	private static BlockingQueue<QueueRequest> requestsToProcess = Config.requestsToProcess;
	private static BlockingQueue<QueueRequest> processedRequests = Config.processedRequests;

	private static SecureRandom random = new SecureRandom();

	@Override
	public QueueRequest queueServer(QueueRequest request)
	{
		if(request.isNew())
		{
			// Set remaining fields
			request.setIp(getThreadLocalRequest().getRemoteAddr());
			request.setTime(System.currentTimeMillis() / 1000L);
			if(request.getToken() == null && request.getUid() == 0)
			{
				request.setQueuePos(-4);
				return request;
			}
			else if(request.getToken() == null)
			{
				request.setToken("NO TOKEN");
			}
			
			for(QueueRequest req : completedRequests)
			{
				// If request is done
				if(req.getToken().equals(request.getToken()))
				{
					return req;
				}
				// Check if Ip needs to wait
				// TODO: Remove negative before publishing!
				if(req.getIp().equals(request.getIp()) && request.getTime() - req.getTime() <= -120)
				{
					request.setQueuePos(-2);
					return request;
				}
			}
			
			for(QueueRequest req : waitingRequests)
			{
				// If still processing request
				if(req.getToken().equals(request.getToken()))
				{
					return req;
				}
				/*// Check if ip already requested
				if(req.getIp().equals(request.getIp()))
				{
					request.setQueuePos(-3);
					return request;
				}*/
			}
			
			for(QueueRequest req : completedRequests)
			{
				// Get the right one that is done
				if(req.getToken().equals(request.getToken()))
				{
					return req;
				}
			}

			// add the token
			request.setToken(new BigInteger(64, random).toString(32));
			request.setOldReq();
			request.setGo(false);
			waitingRequests.add(request);
			try {
				requestsToProcess.put(request);
				System.out.println("Added request " + request.getToken() + " to queue.");
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		for(QueueRequest req : waitingRequests)
		{
			// Remove -1 position requests
			if(req.getQueuePos() == -1)
			{
				waitingRequests.remove(req);
			}
			
			// Set first request to go
			if(req.getQueuePos() == 0 && waitingRequests.indexOf(req) == 0)
			{
				req.setProcessing(true);
			}
			
			// Set position of request to position in list
			req.setQueuePos(waitingRequests.indexOf(req));
			
			// Set request to return to match actual request in list
			if(req.getToken().equals(request.getToken()))
			{
				request = req;
			}
		}
		
		for(QueueRequest req : completedRequests)
		{
			// Get the right one that is done
			if(req.getToken().equals(request.getToken()))
			{
				return req;
			}
		}
		return request;
	}

	@Override
	public boolean removeRequest(QueueRequest request){
		for(QueueRequest req : waitingRequests)
		{
			if(req.getToken().equals(request.getToken()))
			{
				waitingRequests.remove(req);
				System.out.println("Removed request " + req.getToken() + " from queue");
				request.setTime(System.currentTimeMillis() / 1000L);
				completedRequests.add(request);
				System.out.println("Adding request " + req.getToken() + " to completed request list");
				return true;
			}
			
		}
		return false;
	}
	
	public void run()
	{
		System.out.println("Starting PricingServiceImpl thread for receiving processed requests");
		
		// Loop infintely to get processed requests
		while(true)
		{
			try {
				QueueRequest req = processedRequests.take();
				removeRequest(req);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
