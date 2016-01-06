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

import com.achow101.bctalkaccountpricer.client.PricingService;
import com.achow101.bctalkaccountpricer.shared.QueueRequest;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;


/**
 * The server-side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class PricingServiceImpl extends RemoteServiceServlet implements
		PricingService {
	
	public static List<QueueRequest> waitingRequests = new ArrayList<QueueRequest>();
	public static List<QueueRequest> completedRequests = new ArrayList<QueueRequest>();

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
				else if(req.getIp().equals(request.getIp()) && request.getTime() - req.getTime() <= 600)
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
				// Check if ip already requested
				else if(req.getIp().equals(request.getIp()))
				{
					request.setQueuePos(-3);
					return request;
				}
			}
			
			request.setOldReq();
			waitingRequests.add(request);
			
			// add the token
			request.setToken(new BigInteger(130, random).toString(32));
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
				if(!req.isProcessing())
				{
					// TODO: Send request to ProcessPricing thread
				}
				req.setProcessing(true);
			}
			
			// Set position of request to position in list
			req.setQueuePos(waitingRequests.indexOf(req));
			
			// Set request to return to match actual request in list
			if(req.getIp().equals(request.getIp()) && req.getTime() == request.getTime())
			{
				request = req;
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
				request.setTime(System.currentTimeMillis() / 1000L);
				completedRequests.add(request);
				return true;
			}
			
		}
		return false;
	}
	
	// TODO: Receive the completed request from ProcessPricing thread and add to completed and remove from waiting
}
