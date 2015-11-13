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
import com.achow101.bctalkaccountpricer.server.AccountPricer.AccountPricerCallback;
import com.achow101.bctalkaccountpricer.shared.QueueRequest;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;


/**
 * The server-side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class PricingServiceImpl extends RemoteServiceServlet implements
		PricingService, AccountPricerCallback {
	
	public static List<QueueRequest> requestList = Config.requestList;
	public static List<QueueRequest> ipWait = Config.ipWait;

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
			
			// Check if Ip needs to wait
			for(QueueRequest req : ipWait)
			{
				if(req.getToken().equals(request.getToken()))
				{
					return req;
				}
				else if(req.getIp().equals(request.getIp()) && request.getTime() - req.getTime() <= 600)
				{
					request.setQueuePos(-2);
					return request;
				}
			}
			
			for(QueueRequest req : requestList)
			{
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
			request.setGo(false);
			requestList.add(request);
			
			// add the token
			request.setToken(new BigInteger(130, random).toString(32));
		}
		
		if(request.getQueuePos() == 0 && requestList.indexOf(request) == 0)
		{
			request.setGo(true);
		}
		if(request.getQueuePos() == -1)
		{
			requestList.remove(request);
		}
		
		for(QueueRequest req : requestList)
		{
			
			// Remove -1 position requests
			if(req.getQueuePos() == -1)
			{
				requestList.remove(req);
			}
			
			// Set first request to go
			if(req.getQueuePos() == 0 && requestList.indexOf(req) == 0)
			{
				req.setGo(true);
				if(!req.isProcessing())
				{
					// TODO: Get pricer to somehow pass the data back to static list from its thread
					AccountPricer pricer = new AccountPricer(req.getUid(), req.getToken(), this);
					Thread thread = new Thread(pricer);
					thread.start();
				}
			}
			
			// Set position of request to position in list
			req.setQueuePos(requestList.indexOf(req));
			
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
		for(QueueRequest req : requestList)
		{
			if(req.getToken().equals(request.getToken()))
			{
				requestList.remove(req);
				request.setTime(System.currentTimeMillis() / 1000L);
				ipWait.add(request);
				return true;
			}
			
		}
		return false;
	}

	@Override
	public synchronized void onDataRequestComplete(String[] result, String token) {
		for(QueueRequest req : requestList)
		{
			if(req.getToken().equals(token))
			{
				req.setDone(true);
				req.setResult(result);
				req.setTime(System.currentTimeMillis() / 1000L);
				requestList.remove(req);
				ipWait.add(req);
			}
			
		}		
	}

	@Override
	public synchronized void setRequestProcessing(boolean processing, String token) {
		for(QueueRequest req : requestList)
		{
			if(req.getToken().equals(token))
			{
				req.setProcessing(processing);
			}
			
		}	
		
	}
}
